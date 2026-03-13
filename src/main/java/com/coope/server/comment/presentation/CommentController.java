package com.coope.server.comment.presentation;

import com.coope.server.comment.application.CommentService;
import com.coope.server.comment.presentation.dto.CommentRequest;
import com.coope.server.comment.presentation.dto.CommentResponse;
import com.coope.server.global.security.UserDetailsImpl;
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

@Tag(name = "Comment", description = "공지사항 댓글 관리 API")
@RestController
@RequestMapping("/api/notices/{noticeId}/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 작성")
    @PostMapping
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long noticeId,
            @Valid @ModelAttribute CommentRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.createComment(noticeId, request.getContent(), request.getFile(), userDetails.getUser()));
    }

    @Operation(summary = "댓글 목록 조회")
    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long noticeId) {
        return ResponseEntity.ok(commentService.getComments(noticeId));
    }

    @Operation(summary = "댓글 수정")
    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long noticeId,
            @PathVariable Long commentId,
            @Valid @ModelAttribute CommentRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(commentService.updateComment(
                noticeId, commentId, request.getContent(), request.getFile(), request.getDeleteImage(), userDetails.getUser()));
    }

    @Operation(summary = "댓글 삭제")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long noticeId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        commentService.deleteComment(noticeId, commentId, userDetails.getUser());
        return ResponseEntity.noContent().build();
    }
}
