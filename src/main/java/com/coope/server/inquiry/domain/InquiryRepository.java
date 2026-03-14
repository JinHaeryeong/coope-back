package com.coope.server.inquiry.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InquiryRepository {
    Optional<Inquiry> findById(Long id);
    Page<Inquiry> findAll(Pageable pageable);
    Page<Inquiry> findAllByUserId(Long userId, Pageable pageable);
    Inquiry save(Inquiry inquiry);
    void delete(Inquiry inquiry);
    void hardDeleteById(Long id);
    List<Inquiry> findExpiredInquiriesWithLimit(LocalDateTime threshold, int limit);
}
