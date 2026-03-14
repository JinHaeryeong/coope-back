package com.coope.server.comment.infrastructure;

import com.coope.server.comment.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentJpaRepository extends JpaRepository<Comment, Long> {

    @Query("select c from Comment c join fetch c.user where c.notice.id = :noticeId order by c.createdAt desc")
    List<Comment> findAllByNoticeIdWithUser(@Param("noticeId") Long noticeId);
}
