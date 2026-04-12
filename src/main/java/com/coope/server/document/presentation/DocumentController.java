package com.coope.server.document.presentation;

import com.coope.server.document.application.DocumentService;
import com.coope.server.document.application.dto.DocumentCreateRequest;
import com.coope.server.document.application.dto.DocumentResponse;
import com.coope.server.document.application.dto.DocumentUpdateRequest;
import com.coope.server.shared.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Document", description = "문서 관리 API")
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "BearerAuth")
public class DocumentController {

    private final DocumentService documentService;

    @Operation(summary = "문서 생성", description = "새로운 문서를 생성합니다. 부모 문서 ID가 있으면 하위 문서로 생성됩니다.")
    @PostMapping
    public ResponseEntity<DocumentResponse> create(
            @Valid @RequestBody DocumentCreateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        DocumentResponse response = documentService.createDocument(
                request.getWorkspaceCode(), request.getParentId(),
                request.getTitle(), request.getIcon(), request.getContent(),
                userDetails.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "사이드바 문서 목록 조회", description = "워크스페이스 내의 문서 목록을 계층형으로 조회합니다.")
    @GetMapping("/sidebar")
    public ResponseEntity<List<DocumentResponse>> getSidebar(
            @RequestParam String workspaceCode,
            @RequestParam(required = false) Long parentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(documentService.getSidebarDocuments(workspaceCode, parentId, userDetails.getUser()));
    }

    @Operation(summary = "문서 상세 조회", description = "특정 문서의 상세 내용(Redis 스냅샷 포함)을 조회합니다.")
    @GetMapping("/{documentId}")
    public ResponseEntity<DocumentResponse> getDocument(
            @PathVariable Long documentId,
            @RequestParam String workspaceCode,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(documentService.getDocumentDetail(documentId, workspaceCode, userDetails.getUser()));
    }

    @Operation(summary = "문서 내용 업데이트 (최적화)", description = "문서의 내용(Content)만 별도로 빠르게 업데이트합니다.")
    @PatchMapping("/{documentId}/content")
    public ResponseEntity<Void> updateContent(
            @PathVariable Long documentId,
            @RequestBody String content,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        documentService.updateContentOptimized(documentId, content, userDetails.getUser());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "문서 기본 정보 수정", description = "문서의 제목, 아이콘, 커버 이미지 등을 수정합니다.")
    @PatchMapping("/{documentId}")
    public ResponseEntity<DocumentResponse> update(
            @PathVariable Long documentId,
            @RequestBody DocumentUpdateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(documentService.update(
                documentId, request.getTitle(), request.getIcon(), request.getCoverImage(),
                userDetails.getUser()));
    }

    @Operation(summary = "휴지통 목록 조회", description = "워크스페이스 내 삭제(아카이브)된 문서 목록을 조회합니다.")
    @GetMapping("/trash")
    public ResponseEntity<List<DocumentResponse>> getTrash(
            @RequestParam String workspaceCode,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(documentService.getTrashDocuments(workspaceCode, userDetails.getUser()));
    }

    @Operation(summary = "문서 아카이브 (휴지통 이동)", description = "문서를 휴지통으로 이동시킵니다. 하위 문서도 함께 처리됩니다.")
    @PutMapping("/{documentId}/archive")
    public ResponseEntity<Void> archive(
            @PathVariable Long documentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        documentService.archiveDocument(documentId, userDetails.getUser());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "문서 복구", description = "휴지통에 있는 문서를 다시 활성화합니다.")
    @PutMapping("/{documentId}/restore")
    public ResponseEntity<DocumentResponse> restore(
            @PathVariable Long documentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(documentService.restoreDocument(documentId, userDetails.getUser()));
    }

    @Operation(summary = "문서 영구 삭제", description = "문서를 DB에서 완전히 삭제합니다.")
    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> hardDelete(
            @PathVariable Long documentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        documentService.hardDeleteDocument(documentId, userDetails.getUser());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Redis 스냅샷 수동 저장", description = "실시간 편집 중인 내용을 Redis에 임시 저장합니다.")
    @PutMapping("/{documentId}/snapshots")
    public ResponseEntity<Void> saveRedisSnapshot(
            @PathVariable Long documentId,
            @RequestBody String content,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        documentService.saveToRedisSnapshot(documentId, content, userDetails.getUser());
        return ResponseEntity.ok().build();
    }
}
