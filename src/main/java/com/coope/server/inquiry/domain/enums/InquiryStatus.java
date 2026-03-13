package com.coope.server.inquiry.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InquiryStatus {
    PENDING("답변대기"),
    ANSWERED("답변완료");

    private final String description;
}