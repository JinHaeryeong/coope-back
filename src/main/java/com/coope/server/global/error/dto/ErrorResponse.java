package com.coope.server.global.error.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {
    private final boolean success; // 프론트엔드 판단용
    private final String message;  // 에러 메시지
}