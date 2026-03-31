package com.coope.server.shared.file;

import com.coope.server.shared.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileService fileService;

    // BlockNote 등 에디터에서 사용하는 공용 파일 업로드 API
    // DB 트랜잭션과 무관, 고아 파일 정리 정책 필요 (추후 개선)
    @PostMapping("/{category}")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @PathVariable ImageCategory category,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        log.info("파일 업로드 시도 - 유저: {}, 카테고리: {}", userDetails.getUsername(), category);
        String url = fileService.upload(file, category);

        return ResponseEntity.ok(new FileUploadResponse(url));
    }
}
