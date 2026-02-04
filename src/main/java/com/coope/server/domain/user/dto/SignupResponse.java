package com.coope.server.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SignupResponse {
    private final boolean success;
    private final String message;
    private final String email;

    @Builder
    private SignupResponse(boolean success, String message, String email) {
        this.success = success;
        this.message = message;
        this.email = email;
    }

    // 성공 응답을 위한 정적 팩토리 메서드
    public static SignupResponse success(String email) {
        return SignupResponse.builder()
                .success(true)
                .message("회원가입이 완료되었습니다.")
                .email(email)
                .build();
    }
}