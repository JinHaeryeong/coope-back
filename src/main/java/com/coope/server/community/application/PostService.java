package com.coope.server.community.application;

import com.coope.server.community.domain.comment.PostCommentRepository;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final PostCommentService postCommentService;

    /**
     * 게시글 목록 페이징 조회 (카테고리 필터 선택적 적용)
     *
     * @param category null이면 전체 조회, 값이 있으면 해당 카테고리만 조회
     */
    public Page<PostResponse> getPosts(PostCategory category, Pageable pageable) {
        if (category == null) {
            return postRepository.findAllWithAuthor(pageable).map(PostResponse::from);
        }
        return postRepository.findByCategoryWithAuthor(category, pageable).map(PostResponse::from);
    }

    /**
     * 모집 카드 목록 전용 조회 (RECRUITMENT 카테고리만 반환)
     */
    public Page<RecruitmentCardResponse> getRecruitmentCards(Pageable pageable) {
        return postRepository.findByCategoryWithAuthor(PostCategory.RECRUITMENT, pageable)
                .map(RecruitmentCardResponse::from);
    }

    public PostDetailResponse getPostDetail(Long postId, User viewer) {
        Post post = findPostOrThrow(postId);
        List<CommentResponse> comments = postCommentService.getComments(postId, viewer);
        return PostDetailResponse.from(post, comments);
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
