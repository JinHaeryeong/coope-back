package com.coope.server.community.infrastructure;

import com.coope.server.community.domain.like.PostLike;
import com.coope.server.community.domain.like.PostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostLikeRepositoryImpl implements PostLikeRepository {

    private final PostLikeJpaRepository postLikeJpaRepository;

    @Override
    public Optional<PostLike> findByUserIdAndPostId(Long userId, Long postId) {
        return postLikeJpaRepository.findByUserIdAndPostId(userId, postId);
    }

    @Override
    public PostLike save(PostLike postLike) {
        return postLikeJpaRepository.save(postLike);
    }

    @Override
    public void delete(PostLike postLike) {
        postLikeJpaRepository.delete(postLike);
    }

    @Override
    public boolean existsByUserIdAndPostId(Long userId, Long postId) {
        return postLikeJpaRepository.existsByUserIdAndPostId(userId, postId);
    }
}
