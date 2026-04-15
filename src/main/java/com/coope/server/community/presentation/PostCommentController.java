package com.coope.server.community.presentation;

import com.coope.server.community.application.PostCommentService;
import com.coope.server.community.presentation.dto.CommentCreateRequest;
import com.coope.server.community.presentation.dto.CommentResponse;
import com.coope.server.community.presentation.dto.CommentUpdateRequest;
import com.coope.server.shared.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Community Comment", description = "커뮤니티 댓글 관리 API")
@RestController
@RequestMapping("/api/community/posts/{postId}/comments")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "BearerAuth")
public class PostCommentController {

    private final PostCommentService postCommentService;

    @Operation(summary = "댓글 작성", description = "게시글에 댓글을 작성합니다. 비밀 댓글 여부를 설정할 수 있습니다.")
    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postCommentService.createComment(postId, request, userDetails.getUser()));
    }

    @Operation(summary = "댓글 수정", description = "작성한 댓글의 내용을 수정합니다.")
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(postCommentService.updateComment(postId, commentId, request, userDetails.getUser()));
    }

    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다. 작성자 본인만 가능합니다.")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        postCommentService.deleteComment(postId, commentId, userDetails.getUser());
        return ResponseEntity.noContent().build();
    }
}