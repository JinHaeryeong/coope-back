package com.coope.server.community.application;

import com.coope.server.community.domain.like.PostLike;
import com.coope.server.community.domain.like.PostLikeRepository;
import com.coope.server.community.domain.post.Post;
import com.coope.server.community.domain.post.PostRepository;
import com.coope.server.community.domain.post.enums.PostCategory;
import com.coope.server.shared.error.exception.PostNotFoundException;
import com.coope.server.community.presentation.dto.*;
import com.coope.server.shared.error.exception.AccessDeniedException;
import com.coope.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final PostCommentService postCommentService;
    private final PostLikeRepository postLikeRepository;

    /**
     * 게시글 목록 페이징 조회 (카테고리 필터 선택적 적용)
     */
    public Page<PostResponse> getPosts(PostCategory category, Pageable pageable) {
        if (category == null) {
            return postRepository.findAllWithAuthor(pageable).map(PostResponse::from);
        }
        return postRepository.findByCategoryWithAuthor(category, pageable).map(PostResponse::from);
    }

    /**
     * 게시글 키워드 검색 (제목 + 내용 대상, 카테고리 필터 선택적 적용)
     * @param keyword  검색어 (공백, null이면 일반 목록 조회로 폴백)
     * @param category null이면 전체 카테고리 검색
     */
    public Page<PostResponse> searchPosts(String keyword, PostCategory category, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return getPosts(category, pageable);
        }
        if (category == null) {
            return postRepository.searchByKeyword(keyword.trim(), pageable).map(PostResponse::from);
        }
        return postRepository.searchByCategoryAndKeyword(category, keyword.trim(), pageable).map(PostResponse::from);
    }

    /**
     * 모집 카드 목록 전용 조회 (RECRUITMENT 카테고리만 반환)
     */
    public Page<RecruitmentCardResponse> getRecruitmentCards(Pageable pageable) {
        return postRepository.findByCategoryWithAuthor(PostCategory.RECRUITMENT, pageable)
                .map(RecruitmentCardResponse::from);
    }

    /**
     * 인기글 목록 조회 (좋아요 수 내림차순)
     */
    public Page<PostResponse> getTopPosts(Pageable pageable) {
        return postRepository.findTopByLikeCount(pageable).map(PostResponse::from);
    }

    /**
     * 게시글 상세 조회 - viewer가 있으면 isLiked 여부도 함께 반환
     */
    public PostDetailResponse getPostDetail(Long postId, User viewer) {
        Post post = findPostOrThrow(postId);
        List<CommentResponse> comments = postCommentService.getComments(postId, viewer);

        boolean isLiked = viewer != null &&
                postLikeRepository.existsByUserIdAndPostId(viewer.getId(), postId);

        return PostDetailResponse.from(post, comments, isLiked);
    }

    @Transactional
    public PostResponse createPost(PostCreateRequest request, User author) {
        Post post = buildPost(request, author);
        return PostResponse.from(postRepository.save(post));
    }

    @Transactional
    public PostResponse updatePost(Long postId, PostUpdateRequest request, User requester) {
        Post post = findPostOrThrow(postId);
        validateAuthor(post, requester);

        post.update(
                request.getTitle(),
                request.getContent(),
                request.getTechStack(),
                request.getCurrentMembers(),
                request.getTargetMembers()
        );
        return PostResponse.from(post);
    }

    @Transactional
    public void deletePost(Long postId, User requester) {
        Post post = findPostOrThrow(postId);
        validateAuthor(post, requester);
        postRepository.delete(post);
    }

    @Transactional
    public void increaseViewCount(Long postId) {
        Post post = findPostOrThrow(postId);
        post.increaseViewCount();
    }

    /**
     * 좋아요 토글 - 이미 눌렀으면 취소, 아니면 추가
     * @return true = 좋아요 추가됨, false = 좋아요 취소됨
     */
    @Transactional
    public boolean toggleLike(Long postId, User user) {
        findPostOrThrow(postId); // 게시글 존재 여부 검증

        Optional<PostLike> existing = postLikeRepository.findByUserIdAndPostId(user.getId(), postId);

        if (existing.isPresent()) {
            postLikeRepository.delete(existing.get());
            postRepository.decrementLikeCount(postId);
            return false;
        } else {
            Post post = findPostOrThrow(postId);
            postLikeRepository.save(PostLike.of(user, post));
            postRepository.incrementLikeCount(postId);
            return true;
        }
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private Post findPostOrThrow(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("해당 게시글을 찾을 수 없습니다."));
    }

    private void validateAuthor(Post post, User requester) {
        if (!post.isAuthor(requester)) {
            throw new AccessDeniedException("게시글 작성자만 수정·삭제할 수 있습니다.");
        }
    }

    private Post buildPost(PostCreateRequest request, User author) {
        if (PostCategory.RECRUITMENT.equals(request.getCategory())) {
            validateRecruitmentFields(request);
            return Post.createRecruitmentPost(
                    request.getTitle(),
                    request.getContent(),
                    request.getTechStack(),
                    request.getCurrentMembers(),
                    request.getTargetMembers(),
                    author
            );
        }
        return Post.createGeneralPost(request.getCategory(), request.getTitle(), request.getContent(), author);
    }

    private void validateRecruitmentFields(PostCreateRequest request) {
        if (request.getTechStack() == null || request.getTechStack().isBlank()) {
            throw new IllegalArgumentException("모집 게시글에는 기술 스택을 입력해야 합니다.");
        }
        if (request.getCurrentMembers() == null || request.getTargetMembers() == null) {
            throw new IllegalArgumentException("모집 게시글에는 현재 인원과 목표 인원을 입력해야 합니다.");
        }
        if (request.getCurrentMembers() > request.getTargetMembers()) {
            throw new IllegalArgumentException("현재 인원은 목표 인원보다 클 수 없습니다.");
        }
    }
}
