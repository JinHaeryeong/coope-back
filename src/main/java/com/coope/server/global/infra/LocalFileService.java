package com.coope.server.global.infra;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j
public class LocalFileService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.access-url}")
    private String accessUrl;

    public String upload(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) return null;

        try {
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

    public void deleteFile(String imageUrl, String subDir) {
        if (imageUrl == null || imageUrl.isEmpty()) return;

        try {
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

            String fullPath = uploadDir + (uploadDir.endsWith("/") ? "" : "/") + subDir + "/";
            File file = new File(fullPath + fileName);

            if (file.exists()) {
                if (file.delete()) {
                    log.info("파일 삭제 완료: {}/{}", subDir, fileName);
                } else {
                    log.warn("파일 삭제 실패 (권한 등): {}/{}", subDir, fileName);
                }
            } else {
                log.warn("삭제할 파일이 존재하지 않습니다: {}", file.getPath());
            }

        } catch (Exception e) {
            log.error("파일 삭제 도중 예상치 못한 에러 발생: {}", e.getMessage());
        }
    }
}