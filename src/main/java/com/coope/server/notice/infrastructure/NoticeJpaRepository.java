package com.coope.server.notice.infrastructure;

import com.coope.server.notice.domain.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoticeJpaRepository extends JpaRepository<Notice, Long> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notice n SET n.views = n.views + :views WHERE n.id = :id")
    int updateViews(@Param("id") Long id, @Param("views") int views);
}
