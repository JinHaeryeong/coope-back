package com.coope.server.inquiry.application;

import com.coope.server.inquiry.domain.Inquiry;
import com.coope.server.inquiry.domain.InquiryAnswer;
import com.coope.server.inquiry.domain.InquiryRepository;
import com.coope.server.inquiry.domain.enums.InquiryCategory;
import com.coope.server.inquiry.presentation.dto.InquiryResponse;
import com.coope.server.shared.file.FileRollbackDeleteEvent;
import com.coope.server.user.domain.User;
import com.coope.server.user.domain.UserRepository;
import com.coope.server.user.domain.enums.Role;
import com.coope.server.shared.error.exception.UserNotFoundException;
import com.coope.server.shared.file.FileService;
import com.coope.server.shared.file.ImageCategory;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Long createInquiry(Long userId, String title, String content,
                              InquiryCategory category, String environment, List<MultipartFile> files) {
        User user = findUserOrThrow(userId);

        List<String> uploadedUrls = (files != null)
                ? files.stream()
                .filter(f -> f != null && !f.isEmpty())
                .map(this::uploadWithRollback)
                .toList()
                : List.of();

        Inquiry inquiry = Inquiry.createInquiry(user, title, content, category, environment, uploadedUrls);
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
    public void createAnswer(Long inquiryId, Long adminId, String content) {
        Inquiry inquiry = findInquiryOrThrow(inquiryId);
        User admin = findUserOrThrow(adminId);

        InquiryAnswer answer = InquiryAnswer.createInquiryAnswer(inquiry, admin, content);
        inquiry.reply(answer);
    }

    public Page<InquiryResponse> getAllInquiries(Pageable pageable) {
        return inquiryRepository.findAll(pageable).map(InquiryResponse::from);
    }

    public Page<InquiryResponse> getMyInquiries(Long userId, Pageable pageable) {
        return inquiryRepository.findAllByUserId(userId, pageable).map(InquiryResponse::from);
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 계정입니다."));
    }

    private Inquiry findInquiryOrThrow(Long inquiryId) {
        return inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 문의사항입니다."));
    }

    private String uploadWithRollback(MultipartFile file) {
        String url = fileService.upload(file, ImageCategory.INQUIRY);

        eventPublisher.publishEvent(
                new FileRollbackDeleteEvent(url, ImageCategory.INQUIRY)
        );

        return url;
    }
}
