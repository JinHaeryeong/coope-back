package com.coope.server.domain.inquiry.repository;

import com.coope.server.domain.inquiry.entity.InquiryAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryAnswerRepository extends JpaRepository<InquiryAnswer, Long> {
}
