package com.coope.server.domain.user.dto;

import com.coope.server.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSearchResponse {
    private final Long id;
    private final String nickname;
    private final String userIcon;
    private final String status;

    public static UserSearchResponse from(User user, String status) {
        return UserSearchResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .userIcon(user.getUserIcon())
                .status(status)
                .build();
    }
}