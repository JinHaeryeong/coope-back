package com.coope.server.community.domain.comment;

import java.util.List;
import java.util.Optional;

public interface PostCommentRepository {
    Optional<PostComment> findById(Long id);
    PostComment save(PostComment comment);
    void delete(PostComment comment);
    List<PostComment> findByPostIdWithAuthor(Long postId);
}
