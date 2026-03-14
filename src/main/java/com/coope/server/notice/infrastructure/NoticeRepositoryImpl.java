package com.coope.server.notice.infrastructure;

import com.coope.server.notice.domain.Notice;
import com.coope.server.notice.domain.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NoticeRepositoryImpl implements NoticeRepository {

    private final NoticeJpaRepository noticeJpaRepository;

    @Override
    public Page<Notice> findAll(Pageable pageable) {
        return noticeJpaRepository.findAll(pageable);
    }

    @Override
    public Optional<Notice> findById(Long id) {
        return noticeJpaRepository.findById(id);
    }

    @Override
    public Notice save(Notice notice) {
        return noticeJpaRepository.save(notice);
    }

    @Override
    public void delete(Notice notice) {
        noticeJpaRepository.delete(notice);
    }

    @Override
    public int updateViews(Long id, int views) {
        return noticeJpaRepository.updateViews(id, views);
    }
}
