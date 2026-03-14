package com.coope.server.inquiry.infrastructure;

import com.coope.server.inquiry.domain.Inquiry;
import com.coope.server.inquiry.domain.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class InquiryRepositoryImpl implements InquiryRepository {

    private final InquiryJpaRepository inquiryJpaRepository;

    @Override
    public Optional<Inquiry> findById(Long id) {
        return inquiryJpaRepository.findById(id);
    }

    @Override
    public Page<Inquiry> findAll(Pageable pageable) {
        return inquiryJpaRepository.findAll(pageable);
    }

    @Override
    public Page<Inquiry> findAllByUserId(Long userId, Pageable pageable) {
        return inquiryJpaRepository.findAllByUserId(userId, pageable);
    }

    @Override
    public Inquiry save(Inquiry inquiry) {
        return inquiryJpaRepository.save(inquiry);
    }

    @Override
    public void delete(Inquiry inquiry) {
        inquiryJpaRepository.delete(inquiry);
    }

    @Override
    public void hardDeleteById(Long id) {
        inquiryJpaRepository.hardDeleteById(id);
    }

    @Override
    public List<Inquiry> findExpiredInquiriesWithLimit(LocalDateTime threshold, int limit) {
        return inquiryJpaRepository.findExpiredInquiriesWithLimit(threshold, limit);
    }
}
