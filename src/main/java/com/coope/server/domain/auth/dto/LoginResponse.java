package com.coope.server.domain.auth.dto;

import com.coope.server.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;

@Getter
public class LoginResponse {
    private final String accessToken;
    private final String email;
    private final String nickname;
    private final String userIcon;

    @JsonIgnore
    private final String refreshToken;

    @Builder
    private LoginResponse(String accessToken, String email, String nickname, String userIcon, String refreshToken) {
        this.accessToken = accessToken;
        this.email = email;
        this.nickname = nickname;
        this.userIcon = userIcon;
        this.refreshToken = refreshToken;
    }

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