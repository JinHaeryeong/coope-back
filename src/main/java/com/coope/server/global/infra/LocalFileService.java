package com.coope.server.global.infra;

import com.coope.server.global.error.exception.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Component
@Slf4j
public class LocalFileService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.access-url}")
    private String accessUrl;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");
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

            String extension = extractExtension(file);
            String fileName = UUID.randomUUID() + extension;

            Path targetFile = targetDir.resolve(fileName).normalize();
            file.transferTo(targetFile.toFile());

            return accessUrl + category.dir() + "/" + fileName;

        } catch (IOException e) {
            throw new FileStorageException("파일 저장 실패", e);
        }
    }

    public boolean deleteFile(String imageUrl, String subDir) {
        if (imageUrl == null || imageUrl.isEmpty()) return false;

        try {
            String fileName = Paths.get(imageUrl).getFileName().toString();

            if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
                log.error("보안 위협 감지: 유효하지 않은 파일명 접근 시도 ({})", fileName);
                return false;
            }

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

    private String extractExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("파일명이 존재하지 않습니다.");
        }

        // 경로 제거 (IE, 악성 입력 대비)
        String pureFileName = originalFilename.replaceAll("^.*[\\\\/]", "");

        int dotIndex = pureFileName.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == pureFileName.length() - 1) {
            throw new IllegalArgumentException("확장자가 없는 파일은 업로드할 수 없습니다.");
        }

        String extension = pureFileName.substring(dotIndex).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다: " + extension);
        }

        return extension;
    }


}