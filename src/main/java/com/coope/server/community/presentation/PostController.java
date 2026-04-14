package com.coope.server.community.presentation;

import com.coope.server.community.application.PostService;
import com.coope.server.community.domain.post.enums.PostCategory;
import com.coope.server.community.presentation.dto.*;
import com.coope.server.shared.security.UserDetailsImpl;
import com.coope.server.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Community Post", description = "커뮤니티 게시글 관리 API")
@RestController
@RequestMapping("/api/community/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시글 목록 조회", description = "카테고리별로 게시글 목록을 조회합니다. 카테고리가 없으면 전체를 조회합니다.")
    @GetMapping
    public ResponseEntity<Page<PostResponse>> getPosts(
            @RequestParam(required = false) PostCategory category,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(postService.getPosts(category, pageable));
    }

    @Operation(summary = "모집 카드 목록 조회", description = "팀원 모집 카테고리의 글만 카드 UI 렌더링용 응답 규격으로 조회합니다.")
    @GetMapping("/recruitment")
    public ResponseEntity<Page<RecruitmentCardResponse>> getRecruitmentCards(
            @PageableDefault(size = 12, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(postService.getRecruitmentCards(pageable));
    }

    @Operation(summary = "게시글 상세 조회", description = "특정 게시글의 상세 내용과 댓글 목록을 조회합니다. 비밀 댓글은 권한에 따라 마스킹됩니다.")
    @GetMapping("/{id}")
    public ResponseEntity<PostDetailResponse> getPostDetail(
            @PathVariable("id") Long postId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        User viewer = (userDetails != null) ? userDetails.getUser() : null;
        return ResponseEntity.ok(postService.getPostDetail(postId, viewer));
    }

    @Operation(summary = "조회수 증가", description = "게시글 상세 페이지 진입 시 해당 게시글의 조회수를 1 증가시킵니다.")
    @PostMapping("/{id}/views")
    public ResponseEntity<Void> increaseViewCount(@PathVariable("id") Long postId) {
        postService.increaseViewCount(postId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "게시글 작성", description = "새로운 커뮤니티 게시글을 작성합니다. 모집글인 경우 기술 스택 및 인원 정보가 필요합니다.")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody PostCreateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postService.createPost(request, userDetails.getUser()));
    }

    @Operation(summary = "게시글 수정", description = "게시글의 제목이나 내용을 수정합니다. 작성자 본인만 가능합니다.")
    @SecurityRequirement(name = "BearerAuth")
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable("id") Long postId,
            @Valid @RequestBody PostUpdateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(postService.updatePost(postId, request, userDetails.getUser()));
    }

    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다. 작성자 본인만 가능합니다.")
    @SecurityRequirement(name = "BearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable("id") Long postId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        postService.deletePost(postId, userDetails.getUser());
        return ResponseEntity.noContent().build();
    }
}