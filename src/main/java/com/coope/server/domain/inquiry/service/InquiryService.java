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
import com.coope.server.global.error.exception.AccessDeniedException;
import com.coope.server.global.error.exception.UserNotFoundException;
import com.coope.server.global.infra.file.FileService;
import com.coope.server.global.infra.file.ImageCategory;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;
    private final FileService fileService;


    @Transactional
    public Long createInquiry(Long userId, InquiryCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 계정입니다."));

        Inquiry inquiry = Inquiry.createInquiry(
                user,
                request.getTitle(),
                request.getContent(),
                request.getCategory(),
                request.getEnvironment()
        );

        if (request.getFiles() != null && !request.getFiles().isEmpty()) {
            request.getFiles().forEach(file -> {
                String fileUrl = fileService.upload(file, ImageCategory.INQUIRY);
                inquiry.addFile(fileUrl, file.getOriginalFilename());
            });
        }

        return inquiryRepository.save(inquiry).getId();
    }

    public InquiryResponse getInquiry(Long inquiryId, Long userId, Role userRole) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 문의사항입니다.")); // 404로 변경

        if (!inquiry.getUser().getId().equals(userId) && userRole != Role.ROLE_ADMIN) {
            throw new AccessDeniedException("해당 문의를 조회할 권한이 없습니다.");
        }

        return InquiryResponse.from(inquiry);
    }

    @Transactional
    public void deleteInquiry(Long inquiryId, Long userId, Role userRole) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 문의사항입니다."));

        if (!inquiry.getUser().getId().equals(userId) && userRole != Role.ROLE_ADMIN) {
            throw new AccessDeniedException("해당 문의를 삭제할 권한이 없습니다.");
        }

        if (inquiry.getFiles() != null && !inquiry.getFiles().isEmpty()) {
            boolean allDeleted = inquiry.getFiles().stream()
                    .allMatch(file -> fileService.deleteFile(file.getFileUrl(), ImageCategory.INQUIRY));

            if (!allDeleted) {
                throw new IllegalStateException("일부 첨부파일 삭제에 실패하여 문의 삭제를 중단합니다.");
            }
        }

        inquiryRepository.delete(inquiry);
    }

    @Transactional
    public void createAnswer(Long inquiryId, Long adminId, InquiryAnswerRequest request) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 문의사항입니다."));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 관리자입니다."));

        InquiryAnswer answer = InquiryAnswer.createInquiryAnswer(inquiry, admin, request.getContent());

        inquiry.setAnswer(answer);

        inquiryRepository.save(inquiry);
    }

    public Page<InquiryResponse> getAllInquiries(Pageable pageable) {
        return inquiryRepository.findAll(pageable)
                .map(InquiryResponse::from);
    }

    public Page<InquiryResponse> getMyInquiries(Long userId, Pageable pageable) {
        Page<Inquiry> inquiryPage = inquiryRepository.findAllByUserId(userId, pageable);

        return inquiryPage.map(InquiryResponse::from);
    }
}