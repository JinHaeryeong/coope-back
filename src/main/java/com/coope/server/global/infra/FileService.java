package com.coope.server.global.infra;

import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

public interface FileService {
    String upload(MultipartFile file, ImageCategory category);

    boolean deleteFile(String imageUrl, ImageCategory category);

    default String extractExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("파일명이 존재하지 않습니다.");
        }

        String pureFileName = originalFilename.replaceAll("^.*[\\\\/]", "");
        int dotIndex = pureFileName.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == pureFileName.length() - 1) {
            throw new IllegalArgumentException("확장자가 없는 파일은 업로드할 수 없습니다.");
        }

        String extension = pureFileName.substring(dotIndex).toLowerCase();
        if (!Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp").contains(extension)) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다: " + extension);
        }

        return extension;
    }
}