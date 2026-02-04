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
            // 물리적 저장 경로 설정 (예: C:/Users/.../uploads/profiles/)
            String fullPath = uploadDir + (uploadDir.endsWith("/") ? "" : "/") + subDir + "/";
            File dir = new File(fullPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 저장 파일명 생성 (UUID 활용)
            String originalFilename = file.getOriginalFilename();
            String storeFilename = UUID.randomUUID() + "_" + originalFilename;

            // 파일 물리 저장
            file.transferTo(new File(fullPath + storeFilename));

            // 브라우저 접근 URL 반환 (예: /images/profiles/uuid_name.png)
            return accessUrl + subDir + "/" + storeFilename;

        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다.", e);
        }
    }
}