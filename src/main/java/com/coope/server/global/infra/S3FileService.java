package com.coope.server.global.infra;

import com.coope.server.global.error.exception.FileStorageException;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
@Profile("prod")
@RequiredArgsConstructor
@Slf4j
public class S3FileService implements FileService {

    private final S3Template s3Template;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;


    @Override
    public String upload(MultipartFile file, ImageCategory category) {
        if (file == null || file.isEmpty()) return null;

        String extension = extractExtension(file, category);
        String fileName = UUID.randomUUID() + extension;
        String s3Key = category.dir() + "/" + fileName;

        try {
            // S3에 파일 업로드
            s3Template.upload(bucket, s3Key, file.getInputStream());

            return String.format("https://%s.s3.amazonaws.com/%s", bucket, s3Key);

        } catch (IOException e) {
            throw new FileStorageException("S3 파일 업로드 실패", e);
        }
    }

    @Override
    public Resource loadAsResource(String fileUrl, ImageCategory category) {
        try {

            String decodedUrl = java.net.URLDecoder.decode(fileUrl, StandardCharsets.UTF_8);
            java.net.URI uri = new java.net.URI(decodedUrl);
            String path = uri.getPath();

            String s3Key = path.startsWith("/") ? path.substring(1) : path;
            return s3Template.download(bucket, s3Key);
        } catch (Exception e) {
            throw new FileStorageException("유효하지 않은 S3 URL 형식입니다: " + fileUrl, e);
        }
    }

    @Override
    public boolean deleteFile(String imageUrl, ImageCategory category) {
        if (imageUrl == null || imageUrl.isEmpty()) return true;

        try {
            String decodedUrl = java.net.URLDecoder.decode(imageUrl, StandardCharsets.UTF_8);
            java.net.URI uri = new java.net.URI(decodedUrl);
            String path = uri.getPath();
            String s3Key = path.startsWith("/") ? path.substring(1) : path;

            s3Template.deleteObject(bucket, s3Key);
            log.info("S3 파일 삭제 완료: {}", s3Key);
            return true;
        } catch (Exception e) {
            log.error("S3 파일 삭제 중 에러 발생: {}", e.getMessage());
            return false;
        }
    }
}