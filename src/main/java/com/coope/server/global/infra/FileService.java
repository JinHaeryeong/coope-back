package com.coope.server.global.infra;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

public interface FileService {
    String upload(MultipartFile file, ImageCategory category);

    boolean deleteFile(String imageUrl, ImageCategory category);

    Resource loadAsResource(String fileUrl, ImageCategory category);

    default String extractExtension(MultipartFile file, ImageCategory category) {
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

        Set<String> imageExtensions = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

        Set<String> chatExtraExtensions = Set.of(".pdf", ".zip", ".docx", ".xlsx", ".txt");

        if (category == ImageCategory.CHAT) {
            // 채팅은 이미지 + 일반 파일 모두 허용
            if (!imageExtensions.contains(extension) && !chatExtraExtensions.contains(extension)) {
                throw new IllegalArgumentException("채팅에서 허용되지 않는 파일 형식입니다: " + extension);
            }
        } else {
            if (!imageExtensions.contains(extension)) {
                throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다: " + extension);
            }
        }

        return extension;
    }
}