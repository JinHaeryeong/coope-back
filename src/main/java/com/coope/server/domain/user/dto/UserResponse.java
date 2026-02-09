package com.coope.server.domain.user.dto;

import com.coope.server.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {
    private final Long id;
    private final String email;
    private final String nickname;
    private final String userIcon;
    private final String role;

    public static UserResponse of(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .userIcon(user.getUserIcon())
                .role(user.getRole().name())
                .build();
    }
}