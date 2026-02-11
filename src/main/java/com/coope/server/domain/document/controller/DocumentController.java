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
}