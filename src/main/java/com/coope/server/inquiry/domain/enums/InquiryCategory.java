package com.coope.server.inquiry.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InquiryCategory {
    ACCOUNT("계정문의"),
    ERROR("오류문의"),
    SUGGESTION("건의사항"),
    ETC("기타");

    private final String description;
}