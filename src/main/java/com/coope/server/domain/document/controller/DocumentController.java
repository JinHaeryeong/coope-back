package com.coope.server.domain.document.controller;

import com.coope.server.domain.document.dto.DocumentCreateRequest;
import com.coope.server.domain.document.dto.DocumentResponse;
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
            @RequestParam(value = "parentId", required = false) Long parentId) {

        List<DocumentResponse> responses = documentService.getSidebarDocuments(workspaceCode, parentId);

        return ResponseEntity.ok(responses);
    }

    // 휴지통 목록 조회
    @GetMapping("/trash")
    public ResponseEntity<List<DocumentResponse>> getTrash(
            @RequestParam("workspaceCode") String workspaceCode) {

        List<DocumentResponse> responses = documentService.getTrashDocuments(workspaceCode);
        return ResponseEntity.ok(responses);
    }

    // 문서 아카이브
    @PatchMapping("/{documentId}/archive")
    public ResponseEntity<Void> archive(
            @PathVariable("documentId") Long documentId) {

        documentService.archiveDocument(documentId);
        log.info("문서 휴지통 이동 성공 - ID: {}", documentId);
        return ResponseEntity.noContent().build();
    }

    // 문서 복구
    @PatchMapping("/{documentId}/restore")
    public ResponseEntity<DocumentResponse> restore(
            @PathVariable("documentId") Long documentId) {

        DocumentResponse response = documentService.restoreDocument(documentId);
        log.info("문서 복구 성공 - ID: {}, 제목: {}", documentId, response.getTitle());
        return ResponseEntity.ok(response);
    }

    // 문서 영구 삭제
    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> hardDelete(
            @PathVariable("documentId") Long documentId) {

        documentService.hardDeleteDocument(documentId);
        log.info("문서 영구 삭제 성공 - ID: {}", documentId);
        return ResponseEntity.noContent().build();
    }

}