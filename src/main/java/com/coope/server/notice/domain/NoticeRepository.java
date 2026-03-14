package com.coope.server.notice.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface NoticeRepository {
    Page<Notice> findAll(Pageable pageable);
    Optional<Notice> findById(Long id);
    Notice save(Notice notice);
    void delete(Notice notice);
    int updateViews(Long id, int views);
}
