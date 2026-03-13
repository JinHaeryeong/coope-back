package com.coope.server.user.presentation.dto;

import com.coope.server.user.domain.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserSearchResponse {
    private final Long id;
    private final String nickname;
    private final String userIcon;
    private final String status;

    @Builder
    private UserSearchResponse(Long id, String nickname, String userIcon, String status) {
        this.id = id;
        this.nickname = nickname;
        this.userIcon = userIcon;
        this.status = status;
    }

    public static UserSearchResponse of(User user, String status) {
        return UserSearchResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .userIcon(user.getUserIcon())
                .status(status)
                .build();
    }
}