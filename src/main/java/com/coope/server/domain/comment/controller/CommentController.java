package com.coope.server.domain.comment.controller;

import com.coope.server.domain.comment.dto.CommentRequest;
import com.coope.server.domain.comment.dto.CommentResponse;
import com.coope.server.domain.comment.service.CommentService;
import com.coope.server.global.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> addComment(
            @RequestParam("noticeId") Long noticeId,
            @Valid @ModelAttribute CommentRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        CommentResponse response = commentService.createComment(noticeId, request, userDetails.getUser());

        log.info("댓글 작성 성공 - 작성자: {}, 공지사항 ID: {}", userDetails.getUser().getNickname(), noticeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(@RequestParam("noticeId") Long noticeId) {
        List<CommentResponse> responses = commentService.getComments(noticeId);
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable("id") Long id,
            @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        CommentResponse response = commentService.updateComment(id, request, userDetails.getUser());

        log.info("댓글 수정 성공 - 댓글 ID: {}, 수정자: {}", id, userDetails.getUser().getNickname());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        commentService.deleteComment(id, userDetails.getUser());

        log.info("댓글 삭제 성공 - 댓글 ID: {}, 삭제자: {}", id, userDetails.getUser().getNickname());
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}