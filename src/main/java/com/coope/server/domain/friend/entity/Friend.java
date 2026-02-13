package com.coope.server.domain.friend.entity;

import com.coope.server.domain.common.entity.BaseTimeEntity;
import com.coope.server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "friends", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "friend_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Friend extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 나
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 친구
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id", nullable = false)
    private User friend;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendStatus status; // PENDING, ACCEPTED, REJECTED

    @Builder
    public Friend(User user, User friend, FriendStatus status) {
        this.user = user;
        this.friend = friend;
        this.status = status != null ? status : FriendStatus.PENDING;
    }

    public static Friend createFriendship(User user, User friend, FriendStatus status) {
        return Friend.builder()
                .user(user)
                .friend(friend)
                .status(status)
                .build();
    }


    public static Friend sendRequest(User me, User targetFriend) {
        return Friend.builder()
                .user(me)
                .friend(targetFriend)
                .status(FriendStatus.PENDING)
                .build();
    }

    // 상태 변경 편의 메서드
    public void updateStatus(FriendStatus newStatus) {
        this.status = newStatus;
    }
}