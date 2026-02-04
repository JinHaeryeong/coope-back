package com.coope.server.domain.auth.dto;

import com.coope.server.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private String accessToken; // JWT 토큰
    private String email;
    private String nickname;
    private String userIcon;

    @JsonIgnore // JSON 결과창에는 안 보이게 설정 (보안 및 깔끔한 응답)
    private String refreshToken;

    public static LoginResponse of(User user, String accessToken, String refreshToken) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .email(user.getEmail())
                .nickname(user.getNickname())
                .userIcon(user.getUserIcon())
                .refreshToken(refreshToken)
                .build();
    }
}