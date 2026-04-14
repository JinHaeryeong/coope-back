package com.coope.server.community.application;

import com.coope.server.community.domain.comment.PostComment;
import com.coope.server.community.domain.comment.PostCommentRepository;
import com.coope.server.community.domain.post.Post;
import com.coope.server.community.domain.post.PostRepository;
import com.coope.server.shared.error.exception.PostCommentNotFoundException;
import com.coope.server.shared.error.exception.PostNotFoundException;
import com.coope.server.community.presentation.dto.CommentCreateRequest;
import com.coope.server.community.presentation.dto.CommentResponse;
import com.coope.server.shared.error.exception.AccessDeniedException;
import com.coope.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostCommentService {

    private final PostCommentRepository postCommentRepository;
    private final PostRepository postRepository;

    /**
     * 특정 게시글의 댓글 목록 조회
     * 비밀 댓글 처리:
     * 각 댓글에 대해 {@link PostComment#isReadableBy(User)}를 호출하고,
     * 열람 권한이 없으면 내용을 "비밀 댓글입니다."로 마스킹합니다.
     * 마스킹 여부(isMasked)는 클라이언트가 잠금 UI를 표현하는 데 활용됩니다.
     *
     * @param postId 게시글 ID
     * @param viewer 현재 요청 사용자
     */
    public List<CommentResponse> getComments(Long postId, User viewer) {
        List<PostComment> comments = postCommentRepository.findByPostIdWithAuthor(postId);

        return comments.stream()
                .map(comment -> CommentResponse.from(comment, viewer))
                .toList();
    }

    @Transactional
    public CommentResponse createComment(Long postId, CommentCreateRequest request, User author) {
        Post post = findPostOrThrow(postId);
        PostComment comment = PostComment.create(request.getContent(), request.isPrivate(), post, author);
        PostComment saved = postCommentRepository.save(comment);

        return CommentResponse.from(saved, author);
    }

    @Transactional
    public CommentResponse updateComment(Long commentId, String newContent, User requester) {
        PostComment comment = findCommentOrThrow(commentId);
        validateCommentAuthor(comment, requester);

        comment.update(newContent);
        return CommentResponse.from(comment, requester);
    }

    @Transactional
    public void deleteComment(Long commentId, User requester) {
        PostComment comment = findCommentOrThrow(commentId);

        boolean isCommentAuthor = comment.isAuthor(requester);
        boolean isPostOwner = comment.getPost().isAuthor(requester);

        if (!isCommentAuthor && !isPostOwner) {
            throw new AccessDeniedException("댓글 작성자 또는 게시글 작성자만 삭제할 수 있습니다.");
        }
        postCommentRepository.delete(comment);
    }

    private Post findPostOrThrow(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("해당 게시글을 찾을 수 없습니다."));
    }

    private PostComment findCommentOrThrow(Long commentId) {
        return postCommentRepository.findById(commentId)
                .orElseThrow(() -> new PostCommentNotFoundException("해당 댓글을 찾을 수 없습니다."));
    }

    private void validateCommentAuthor(PostComment comment, User requester) {
        if (!comment.isAuthor(requester)) {
            throw new AccessDeniedException("댓글 작성자만 수정할 수 있습니다.");
        }
    }
}
