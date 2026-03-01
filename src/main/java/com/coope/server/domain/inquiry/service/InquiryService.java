package com.coope.server.domain.inquiry.service;

import com.coope.server.domain.inquiry.dto.InquiryCreateRequest;
import com.coope.server.domain.inquiry.dto.InquiryResponse;
import com.coope.server.domain.inquiry.entity.Inquiry;
import com.coope.server.domain.inquiry.repository.InquiryRepository;
import com.coope.server.domain.user.entity.User;
import com.coope.server.domain.user.repository.UserRepository;
import com.coope.server.global.error.exception.UserNotFoundException;
import com.coope.server.global.infra.file.FileService;
import com.coope.server.global.infra.file.ImageCategory;
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

    public InquiryResponse getInquiry(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의사항입니다."));

        return InquiryResponse.from(inquiry);
    }

    @Transactional
    public void deleteInquiry(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의사항입니다."));

        if (inquiry.getFiles() != null) {
            inquiry.getFiles().forEach(file -> {
                fileService.deleteFile(file.getFileUrl(), ImageCategory.INQUIRY);
            });
        }

        inquiryRepository.delete(inquiry);
    }

    public Page<InquiryResponse> getMyInquiries(Long userId, Pageable pageable) {
        Page<Inquiry> inquiryPage = inquiryRepository.findAllByUserId(userId, pageable);

        return inquiryPage.map(InquiryResponse::from);
    }
}