package com.coope.server.global.infra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
public class LocalFileService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.access-url}")
    private String accessUrl;

    public String upload(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) return null;

        try {
            // 물리적 저장 경로 설정 (OS별 구분자 자동 대응)
            String fullPath = uploadDir + (uploadDir.endsWith("/") ? "" : "/") + subDir + "/";
            File dir = new File(fullPath);
            if (!dir.exists()) {
                dir.mkdirs(); // 폴더가 없으면 생성
            }

            // Copilot 조언 반영: 원본 파일명 대신 확장자만 추출하여 보안 강화
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null) {
                int dotIndex = originalFilename.lastIndexOf('.');
                if (dotIndex != -1 && dotIndex < originalFilename.length() - 1) {
                    extension = originalFilename.substring(dotIndex); // 확장자 추출
                }
            }

            // UUID 기반의 안전한 저장 파일명 생성
            String storeFilename = UUID.randomUUID() + extension;

            // 파일 물리 저장
            file.transferTo(new File(fullPath + storeFilename));

            // 브라우저 접근 URL 반환
            return accessUrl + subDir + "/" + storeFilename;

        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다.", e);
        }
    }
}