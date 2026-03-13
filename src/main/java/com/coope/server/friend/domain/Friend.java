package com.coope.server.friend.domain;

import com.coope.server.domain.common.entity.BaseTimeEntity;
import com.coope.server.user.domain.User;
import com.coope.server.global.error.exception.FriendException;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "friends", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "friend_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"user", "friend"})
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

    public Friend createInverse() {
        if (this.status != FriendStatus.ACCEPTED) {
            throw new FriendException("승인된 관계에서만 역방향 관계를 생성할 수 있습니다.");
        }
        return Friend.builder()
                .user(this.friend)
                .friend(this.user)
                .status(FriendStatus.ACCEPTED)
                .build();
    }

    public static Friend sendRequest(User me, User targetFriend) {
        return Friend.builder()
                .user(me)
                .friend(targetFriend)
                .status(FriendStatus.PENDING)
                .build();
    }
    public void accept() {
        if (this.status != FriendStatus.PENDING) {
            throw new FriendException("이미 처리되었거나 대기 중이 아닌 요청입니다.");
        }
        this.status = FriendStatus.ACCEPTED;
    }
}