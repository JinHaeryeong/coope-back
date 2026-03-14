package com.coope.server.shared.file;

import com.coope.server.shared.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload/{category}")
    public ResponseEntity<Map<String, String>> uploadFile(
            @PathVariable ImageCategory category,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        log.info("파일 업로드 시도 - 유저: {}, 카테고리: {}", userDetails.getUsername(), category);
        String url = fileService.upload(file, category);

        Map<String, String> response = new HashMap<>();
        response.put("fileUrl", url);
        return ResponseEntity.ok(response);
    }
}
