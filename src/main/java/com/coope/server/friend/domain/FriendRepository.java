package com.coope.server.friend.domain;

import com.coope.server.user.domain.User;

import java.util.List;
import java.util.Optional;

public interface FriendRepository {
    List<Friend> findAllByFriendIdAndStatus(Long friendId, FriendStatus status);
    Optional<Friend> findByUserAndFriend(User user, User friend);
    Optional<FriendStatus> findStatusBetweenUsers(Long u1, Long u2);
    int deleteFriendship(User me, User friend);
    boolean existsFriendship(User u1, User u2);
    boolean existsByUserAndFriend(User user, User friend);
    List<Friend> findFriendsByMe(Long userId, FriendStatus status);
    Friend save(Friend friend);
}
