package com.coope.server.domain.user.dto;

import com.coope.server.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserResponse {
    private final Long id;
    private final String email;
    private final String nickname;
    private final String userIcon;
    private final String role;
    private final String provider;

    @Builder
    private UserResponse(Long id, String email, String nickname, String userIcon, String role, String provider) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.userIcon = userIcon;
        this.role = role;
        this.provider = provider;
    }

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .userIcon(user.getUserIcon())
                .role(user.getRole().name())
                .provider(user.getProvider().name())
                .build();
    }
}