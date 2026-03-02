package com.coope.server.domain.inquiry.repository;

import com.coope.server.domain.inquiry.entity.Inquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    @EntityGraph(attributePaths = {"user", "files"})
    Page<Inquiry> findAllByUserId(Long userId, Pageable pageable);

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"user", "files"})
    Page<Inquiry> findAll(@NonNull Pageable pageable);
}