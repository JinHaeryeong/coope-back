package com.coope.server.domain.comment.controller;

import com.coope.server.domain.comment.dto.CommentRequest;
import com.coope.server.domain.comment.dto.CommentResponse;
import com.coope.server.domain.comment.service.CommentService;
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

    @Operation(summary = "댓글 작성", description = "해당 공지사항에 새로운 댓글을 작성합니다.")
    @PostMapping
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long noticeId, // PathVariable로 변경
            @Valid @ModelAttribute CommentRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        CommentResponse response = commentService.createComment(noticeId, request, userDetails.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "댓글 목록 조회", description = "해당 공지사항의 모든 댓글을 조회합니다.")
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

        return ResponseEntity.ok(commentService.updateComment(noticeId, commentId, request, userDetails.getUser()));
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