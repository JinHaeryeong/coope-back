package com.coope.server.community.infrastructure;

import com.coope.server.community.domain.comment.PostComment;
import com.coope.server.community.domain.comment.PostCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * {@link PostCommentRepository} 도메인 인터페이스의 JPA 구현체
 */
@Repository
@RequiredArgsConstructor
public class PostCommentRepositoryImpl implements PostCommentRepository {

    private final PostCommentJpaRepository postCommentJpaRepository;

    @Override
    public Optional<PostComment> findById(Long id) {
        return postCommentJpaRepository.findById(id);
    }

    @Override
    public PostComment save(PostComment comment) {
        return postCommentJpaRepository.save(comment);
    }

    @Override
    public void delete(PostComment comment) {
        postCommentJpaRepository.delete(comment);
    }

    @Override
    public List<PostComment> findByPostIdWithAuthor(Long postId) {
        return postCommentJpaRepository.findByPostIdWithAuthor(postId);
    }
}
