package com.coope.server.domain.friend.dto; // 패키지 경로는 프로젝트에 맞게!

import com.coope.server.domain.friend.entity.Friend;
import com.coope.server.domain.friend.entity.FriendStatus;
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


    public static FriendResponse of(Friend friend) {
        return FriendResponse.builder()
                .id(friend.getId())
                .friendId(friend.getFriend().getId())
                .nickname(friend.getFriend().getNickname())
                .userIcon(friend.getFriend().getUserIcon())
                .status(friend.getStatus())
                .build();
    }

    public static FriendResponse of(Friend friend, com.coope.server.domain.user.entity.User targetUser) {
        return FriendResponse.builder()
                .id(friend.getId())
                .friendId(targetUser.getId())
                .nickname(targetUser.getNickname())
                .userIcon(targetUser.getUserIcon())
                .status(friend.getStatus())
                .build();
    }
}