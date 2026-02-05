package com.coope.server.domain.notice.repository;

import com.coope.server.domain.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface NoticeRepository extends JpaRepository<Notice, Long> {
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notice n SET n.views = n.views + 1 WHERE n.id = :id")
    int updateViews(@Param("id") Long id);
}