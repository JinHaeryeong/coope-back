package com.coope.server.domain.comment.repository;

import com.coope.server.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("select c from Comment c join fetch c.user where c.notice.id = :noticeId order by c.createdAt desc")
    List<Comment> findAllByNoticeIdWithUser(@Param("noticeId") Long noticeId);
}