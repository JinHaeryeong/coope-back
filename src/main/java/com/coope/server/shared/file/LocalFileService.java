package com.coope.server.shared.file;

import com.coope.server.shared.error.exception.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
@Profile("dev")
@Slf4j
public class LocalFileService implements FileService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.access-url}")
    private String accessUrl;

    @Override
    public String upload(MultipartFile file, ImageCategory category) {
        if (file == null || file.isEmpty()) return null;
        try {
            Path basePath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path targetDir = basePath.resolve(category.dir()).normalize();
            if (!targetDir.startsWith(basePath)) throw new IllegalStateException("Path traversal detected");

            Files.createDirectories(targetDir);
            String extension = extractExtension(file, category);
            String fileName = UUID.randomUUID() + extension;
            file.transferTo(targetDir.resolve(fileName).normalize().toFile());

            return "http://localhost:8080" + accessUrl + category.dir() + "/" + fileName;
        } catch (IOException e) {
            throw new FileStorageException("파일 저장 실패", e);
        }
    }

    @Override
    public Resource loadAsResource(String fileUrl, ImageCategory category) {
        try {
            String decodedUrl = java.net.URLDecoder.decode(fileUrl, StandardCharsets.UTF_8);
            String path = new java.net.URI(decodedUrl).getPath();
            String fileName = path.substring(path.lastIndexOf("/") + 1);
            Path file = Paths.get(uploadDir).resolve(category.dir()).resolve(fileName);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) return resource;
            throw new FileStorageException("파일을 찾을 수 없거나 읽기 권한이 없습니다: " + fileName);
        } catch (Exception e) {
            throw new FileStorageException("파일 로드 중 오류 발생: " + fileUrl, e);
        }
    }

    @Override
    public boolean deleteFile(String imageUrl, ImageCategory category) {
        if (imageUrl == null || imageUrl.isEmpty()) return true;
        try {
            String decodedUrl = java.net.URLDecoder.decode(imageUrl, StandardCharsets.UTF_8);
            String path = new java.net.URI(decodedUrl).getPath();
            String fileName = path.substring(path.lastIndexOf("/") + 1);

            Path basePath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path targetPath = basePath.resolve(category.dir()).resolve(fileName).normalize();
            if (!targetPath.startsWith(basePath)) {
                log.error("보안 위협 감지: 허용되지 않은 경로 삭제 시도 -> {}", targetPath);
                return false;
            }

            boolean deleted = Files.deleteIfExists(targetPath);
            if (deleted) log.info("로컬 파일 삭제 완료: {}", targetPath);
            else log.warn("삭제할 파일이 존재하지 않습니다: {}", targetPath);
            return true;
        } catch (Exception e) {
            log.error("파일 삭제 도중 예상치 못한 에러 발생: {}", e.getMessage(), e);
            return false;
        }
    }
}
