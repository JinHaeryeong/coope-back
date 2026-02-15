package com.coope.server.domain.friend.dto;

import com.coope.server.domain.friend.entity.Friend;
import com.coope.server.domain.friend.entity.FriendStatus;
import com.coope.server.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FriendResponse {
    private Long id;
    private Long friendId;
    private String nickname;
    private String userIcon;
    private FriendStatus status;
    private Long roomId;


    public static FriendResponse of(Friend friend) {
        return FriendResponse.builder()
                .id(friend.getId())
                .friendId(friend.getUser().getId())
                .nickname(friend.getUser().getNickname())
                .userIcon(friend.getUser().getUserIcon())
                .status(friend.getStatus())
                .build();
    }

    public static FriendResponse of(Friend friend, User targetUser) {
        return FriendResponse.builder()
                .id(friend.getId())
                .friendId(targetUser.getId())
                .nickname(targetUser.getNickname())
                .userIcon(targetUser.getUserIcon())
                .status(friend.getStatus())
                .build();
    }
}