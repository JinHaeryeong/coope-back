package com.coope.server.domain.auth.dto;

import com.coope.server.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;

@Getter
public class LoginResponse {
    private final String accessToken;
    private final Long id;
    private final String email;
    private final String nickname;
    private final String userIcon;
    private final String role;
    private final String provider;

    @JsonIgnore
    private final String refreshToken;

    @Builder
    private LoginResponse(String accessToken, Long id, String email, String nickname, String userIcon, String role, String provider, String refreshToken) {
        this.accessToken = accessToken;
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.userIcon = userIcon;
        this.role = role;
        this.provider = provider;
        this.refreshToken = refreshToken;
    }

    public static LoginResponse of(User user, String accessToken, String refreshToken) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .userIcon(user.getUserIcon())
                .role(user.getRole().name())
                .provider(user.getProvider().name())
                .refreshToken(refreshToken)
                .build();
    }
}