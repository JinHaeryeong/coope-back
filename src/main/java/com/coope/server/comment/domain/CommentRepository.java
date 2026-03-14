package com.coope.server.comment.domain;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {
    Optional<Comment> findById(Long id);
    List<Comment> findAllByNoticeIdWithUser(Long noticeId);
    Comment save(Comment comment);
    void delete(Comment comment);
}
