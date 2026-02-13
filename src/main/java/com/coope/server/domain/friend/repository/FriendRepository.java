package com.coope.server.domain.friend.repository;

import com.coope.server.domain.friend.entity.Friend;
import com.coope.server.domain.friend.entity.FriendStatus;
import com.coope.server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {


    List<Friend> findAllByFriendIdAndStatus(Long friendId, FriendStatus status);

    // 나랑 특정 유저 사이에 이미 관계 데이터가 있는지 확인 (중복 신청 방지)
    Optional<Friend> findByUserAndFriend(User user, User friend);

    // 수락 시 혹은 삭제 시 양방향 데이터를 한꺼번에 지우거나 찾기 위해 필요
    void deleteByUserAndFriend(User user, User friend);

    // 내가 신청했거나(userId), 내가 받았거나(friendId) 한 모든 관계 조회
    List<Friend> findAllByUserIdOrFriendId(Long userId, Long friendId);


}