package com.coope.server.domain.inquiry.dto;

import com.coope.server.domain.inquiry.entity.Inquiry;
import com.coope.server.domain.inquiry.entity.InquiryFile;
import com.coope.server.domain.inquiry.enums.InquiryCategory;
import com.coope.server.domain.inquiry.enums.InquiryStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class InquiryResponse {

    private final Long id;
    private final Long userId;
    private final String userName;
    private final String title;
    private final String content;
    private final InquiryCategory category;
    private final String environment;
    private final InquiryStatus status;
    private final String answer;
    private final List<String> imageUrls;
    private final LocalDateTime createdAt;

    public static InquiryResponse from(final Inquiry inquiry) {
        return InquiryResponse.builder()
                .id(inquiry.getId())
                .userId(inquiry.getUser().getId())
                .userName(inquiry.getUser().getName())
                .title(inquiry.getTitle())
                .content(inquiry.getContent())
                .category(inquiry.getCategory())
                .environment(inquiry.getEnvironment())
                .status(inquiry.getStatus())
                .answer(inquiry.getAnswer() != null ? inquiry.getAnswer().getContent() : null)
                .imageUrls(inquiry.getFiles().stream()
                        .map(InquiryFile::getFileUrl)
                        .toList())
                .createdAt(inquiry.getCreatedAt())
                .build();
    }
}