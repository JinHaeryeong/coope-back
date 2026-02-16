package com.coope.server.domain.friend.dto;

import com.coope.server.domain.friend.entity.Friend;
import com.coope.server.domain.friend.entity.FriendStatus;
import com.coope.server.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class FriendResponse {
    private final Long id;
    private final Long friendId;
    private final String nickname;
    private final String userIcon;
    private final FriendStatus status;
    private final Long roomId;

    @Builder
    private FriendResponse(Long id, Long friendId, String nickname, String userIcon, FriendStatus status, Long roomId) {
        this.id = id;
        this.friendId = friendId;
        this.nickname = nickname;
        this.userIcon = userIcon;
        this.status = status;
        this.roomId = roomId;
    }

    // 친구 엔티티만으로 만들 때
    public static FriendResponse from(Friend friend) {
        return FriendResponse.builder()
                .id(friend.getId())
                .friendId(friend.getUser().getId())
                .nickname(friend.getUser().getNickname())
                .userIcon(friend.getUser().getUserIcon())
                .status(friend.getStatus())
                .build();
    }

    // 상대방 유저 정보가 명확히 분리되어 있을 때
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