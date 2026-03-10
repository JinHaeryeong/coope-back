package com.coope.server.domain.document.controller;

import com.coope.server.domain.document.dto.DocumentCreateRequest;
import com.coope.server.domain.document.dto.DocumentResponse;
import com.coope.server.domain.document.dto.DocumentUpdateRequest;
import com.coope.server.domain.document.service.DocumentService;
import com.coope.server.global.security.UserDetailsImpl; // CommentController 스타일 반영
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "BearerAuth")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    public ResponseEntity<DocumentResponse> create(
            @Valid @RequestBody DocumentCreateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        DocumentResponse response = documentService.createDocument(request, userDetails.getUser());

        log.info("문서 생성 성공 - 제목: {}, 작성자: {}, 워크스페이스: {}",
                response.getTitle(), userDetails.getUser().getNickname(), request.getWorkspaceCode());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 사이드바 문서 목록 조회 (계층형)

    @GetMapping("/sidebar")
    public ResponseEntity<List<DocumentResponse>> getSidebar(
            @RequestParam("workspaceCode") String workspaceCode,
            @RequestParam(value = "parentId", required = false) Long parentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        List<DocumentResponse> responses = documentService.getSidebarDocuments(workspaceCode, parentId, userDetails.getUser());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<DocumentResponse> getDocument(
            @PathVariable("documentId") Long documentId,
            @RequestParam("workspaceCode") String workspaceCode,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        // 문서 상세 정보를 가져오는 서비스 로직 호출
        DocumentResponse response = documentService.getDocumentDetail(documentId, workspaceCode, userDetails.getUser());

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{documentId}/content")
    public ResponseEntity<Void> updateContent(
            @PathVariable Long documentId,
            @RequestBody String content,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        documentService.updateContentOptimized(documentId, content, userDetails.getUser());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{documentId}")
    public ResponseEntity<DocumentResponse> update(
            @PathVariable Long documentId,
            @RequestBody DocumentUpdateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(documentService.update(documentId, request, userDetails.getUser()));
    }

    // 휴지통 목록 조회
    @GetMapping("/trash")
    public ResponseEntity<List<DocumentResponse>> getTrash(
            @RequestParam("workspaceCode") String workspaceCode,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        List<DocumentResponse> responses = documentService.getTrashDocuments(workspaceCode, userDetails.getUser());
        return ResponseEntity.ok(responses);
    }

    // 문서 아카이브
    @PatchMapping("/{documentId}/archive")
    public ResponseEntity<Void> archive(
            @PathVariable("documentId") Long documentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        documentService.archiveDocument(documentId, userDetails.getUser());
        log.info("문서 휴지통 이동 성공 - ID: {}", documentId);
        return ResponseEntity.noContent().build();
    }

    // 문서 복구
    @PatchMapping("/{documentId}/restore")
    public ResponseEntity<DocumentResponse> restore(
            @PathVariable("documentId") Long documentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        DocumentResponse response = documentService.restoreDocument(documentId, userDetails.getUser());
        log.info("문서 복구 성공 - ID: {}, 제목: {}", documentId, response.getTitle());
        return ResponseEntity.ok(response);
    }

    // 문서 영구 삭제
    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> hardDelete(
            @PathVariable("documentId") Long documentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        documentService.hardDeleteDocument(documentId, userDetails.getUser());
        log.info("문서 영구 삭제 성공 - ID: {}", documentId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{documentId}/redis-snapshot")
    public ResponseEntity<Void> saveRedisSnapshot(
            @PathVariable Long documentId,
            @RequestBody String content,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        if (userDetails == null || userDetails.getUser() == null) {
            log.warn("Redis 스냅샷 요청 - 인증되지 않은 사용자");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        documentService.saveToRedisSnapshot(documentId, content, userDetails.getUser());

        log.debug("Redis 스냅샷 저장 성공 - 문서 ID: {}, 저장자: {}",
                documentId, userDetails.getUser().getEmail());

        return ResponseEntity.ok().build();
    }

}