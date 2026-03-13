package com.coope.server.domain.inquiry.service;

import com.coope.server.domain.inquiry.dto.InquiryAnswerRequest;
import com.coope.server.domain.inquiry.dto.InquiryCreateRequest;
import com.coope.server.domain.inquiry.dto.InquiryResponse;
import com.coope.server.domain.inquiry.entity.Inquiry;
import com.coope.server.domain.inquiry.entity.InquiryAnswer;
import com.coope.server.domain.inquiry.repository.InquiryRepository;
import com.coope.server.domain.user.entity.User;
import com.coope.server.domain.user.enums.Role;
import com.coope.server.domain.user.repository.UserRepository;
import com.coope.server.global.error.exception.UserNotFoundException;
import com.coope.server.global.infra.file.FileService;
import com.coope.server.global.infra.file.ImageCategory;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;
    private final FileService fileService;

    @Transactional
    public Long createInquiry(Long userId, InquiryCreateRequest request) {
        User user = findUserOrThrow(userId);

        List<String> uploadedUrls = (request.getFiles() != null) ?
                request.getFiles().stream().map(f -> fileService.upload(f, ImageCategory.INQUIRY)).toList() : null;

        Inquiry inquiry = Inquiry.createInquiry(
                user, request.getTitle(), request.getContent(),
                request.getCategory(), request.getEnvironment(), uploadedUrls
        );

        return inquiryRepository.save(inquiry).getId();
    }

    public InquiryResponse getInquiry(Long inquiryId, Long userId, Role userRole) {
        Inquiry inquiry = findInquiryOrThrow(inquiryId);
        inquiry.validateAccess(userId, userRole);
        return InquiryResponse.from(inquiry);
    }

    @Transactional
    public void deleteInquiry(Long inquiryId, Long userId, Role userRole) {
        Inquiry inquiry = findInquiryOrThrow(inquiryId);
        inquiry.validateAccess(userId, userRole);
        inquiry.softDelete();
        inquiryRepository.delete(inquiry);
    }

    @Transactional
    public void createAnswer(Long inquiryId, Long adminId, InquiryAnswerRequest request) {
        Inquiry inquiry = findInquiryOrThrow(inquiryId);
        User admin = findUserOrThrow(adminId);

        InquiryAnswer answer = InquiryAnswer.createInquiryAnswer(inquiry, admin, request.getContent());
        inquiry.reply(answer);
    }

    public Page<InquiryResponse> getAllInquiries(Pageable pageable) {
        return inquiryRepository.findAll(pageable)
                .map(InquiryResponse::from);
    }

    public Page<InquiryResponse> getMyInquiries(Long userId, Pageable pageable) {
        return inquiryRepository.findAllByUserId(userId, pageable)
                .map(InquiryResponse::from);
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 계정입니다."));
    }

    private Inquiry findInquiryOrThrow(Long inquiryId) {
        return inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 문의사항입니다."));
    }
}