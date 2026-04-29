package com.coope.server.community.domain.like;

import java.util.Optional;

public interface PostLikeRepository {
    Optional<PostLike> findByUserIdAndPostId(Long userId, Long postId);
    PostLike save(PostLike postLike);
    void delete(PostLike postLike);
    boolean existsByUserIdAndPostId(Long userId, Long postId);
}
