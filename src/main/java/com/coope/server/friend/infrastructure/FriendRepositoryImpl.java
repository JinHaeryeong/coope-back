package com.coope.server.friend.infrastructure;

import com.coope.server.friend.domain.Friend;
import com.coope.server.friend.domain.FriendRepository;
import com.coope.server.friend.domain.FriendStatus;
import com.coope.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FriendRepositoryImpl implements FriendRepository {

    private final FriendJpaRepository friendJpaRepository;

    @Override
    public List<Friend> findAllByFriendIdAndStatus(Long friendId, FriendStatus status) {
        return friendJpaRepository.findAllByFriendIdAndStatus(friendId, status);
    }

    @Override
    public Optional<Friend> findByUserAndFriend(User user, User friend) {
        return friendJpaRepository.findByUserAndFriend(user, friend);
    }

    @Override
    public Optional<FriendStatus> findStatusBetweenUsers(Long u1, Long u2) {
        return friendJpaRepository.findStatusBetweenUsers(u1, u2);
    }

    @Override
    public int deleteFriendship(User me, User friend) {
        return friendJpaRepository.deleteFriendship(me, friend);
    }

    @Override
    public boolean existsFriendship(User u1, User u2) {
        return friendJpaRepository.existsFriendship(u1, u2);
    }

    @Override
    public boolean existsByUserAndFriend(User user, User friend) {
        return friendJpaRepository.existsByUserAndFriend(user, friend);
    }

    @Override
    public List<Friend> findFriendsByMe(Long userId, FriendStatus status) {
        return friendJpaRepository.findFriendsByMe(userId, status);
    }

    @Override
    public Friend save(Friend friend) {
        return friendJpaRepository.save(friend);
    }
}
