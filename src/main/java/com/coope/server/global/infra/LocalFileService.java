package com.coope.server.global.infra;

import com.coope.server.global.error.exception.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
@Profile("dev")
@Slf4j
public class LocalFileService implements FileService{

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.access-url}")
    private String accessUrl;


    @Override
    public String upload(MultipartFile file, ImageCategory category) {
        if (file == null || file.isEmpty()) return null;

        try {
            Path basePath = Paths.get(uploadDir)
                    .toAbsolutePath()
                    .normalize();

            Path targetDir = basePath
                    .resolve(category.dir())
                    .normalize();

            if (!targetDir.startsWith(basePath)) {
                throw new IllegalStateException("Path traversal detected");
            }

            Files.createDirectories(targetDir);

            String extension = extractExtension(file, category);
            String fileName = UUID.randomUUID() + extension;

            Path targetFile = targetDir.resolve(fileName).normalize();
            file.transferTo(targetFile.toFile());

            return "http://localhost:8080" + accessUrl + category.dir() + "/" + fileName;

        } catch (IOException e) {
            throw new FileStorageException("파일 저장 실패", e);
        }
    }

    @Override
    public Resource loadAsResource(String fileUrl, ImageCategory category) {
        try {
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            Path file = Paths.get(uploadDir).resolve(category.dir()).resolve(fileName);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) return resource;
            throw new RuntimeException("파일을 읽을 수 없습니다.");
        } catch (MalformedURLException e) {
            throw new RuntimeException("파일 경로 오류", e);
        }
    }

    @Override
    public boolean deleteFile(String imageUrl, ImageCategory category) {
        if (imageUrl == null || imageUrl.isEmpty()) return false;

        try {
            String fileName = Paths.get(imageUrl).getFileName().toString();

            if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
                log.error("보안 위협 감지: 유효하지 않은 파일명 접근 시도 ({})", fileName);
                return false;
            }

            String subDir = category.dir();
            String separator = uploadDir.endsWith("/") ? "" : "/";
            String fullPath = uploadDir + separator + subDir + "/";
            File file = new File(fullPath + fileName);

            if (file.exists()) {
                if (file.delete()) {
                    log.info("파일 삭제 완료: {}/{}", subDir, fileName);
                    return true;
                } else {
                    log.warn("파일 삭제 실패 (권한 등): {}/{}", subDir, fileName);
                    return false;
                }
            } else {
                log.warn("삭제할 파일이 존재하지 않습니다: {}", file.getPath());
                return true;
            }

        } catch (Exception e) {
            log.error("파일 삭제 도중 예상치 못한 에러 발생: {}", e.getMessage());
            return false;
        }
    }

}