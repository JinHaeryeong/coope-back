package com.coope.server.domain.comment.repository;

import com.coope.server.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByNoticeIdOrderByCreatedAtDesc(Long noticeId);
}