package com.coope.server.domain.friend.repository;

import com.coope.server.domain.friend.entity.Friend;
import com.coope.server.domain.friend.entity.FriendStatus;
import com.coope.server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    List<Friend> findAllByFriendIdAndStatus(Long friendId, FriendStatus status);

    // 나랑 특정 유저 사이에 이미 관계 데이터가 있는지 확인 (중복 신청 방지)
    Optional<Friend> findByUserAndFriend(User user, User friend);

    @Query("SELECT f.status FROM Friend f " +
            "WHERE ((f.user.id = :u1 AND f.friend.id = :u2) " +
            "OR (f.user.id = :u2 AND f.friend.id = :u1)) " +
            "AND f.status IN (com.coope.server.domain.friend.entity.FriendStatus.PENDING, " +
            "com.coope.server.domain.friend.entity.FriendStatus.ACCEPTED)")
    Optional<FriendStatus> findStatusBetweenUsers(@Param("u1") Long u1, @Param("u2") Long u2);

    @Modifying
    @Query("DELETE FROM Friend f WHERE (f.user = :me AND f.friend = :friend) OR (f.user = :friend AND f.friend = :me)")
    int deleteFriendship(@Param("me") User me, @Param("friend") User friend);

    @Query("SELECT f FROM Friend f " +
            "JOIN FETCH f.user " +
            "JOIN FETCH f.friend " +
            "WHERE f.user.id = :userId OR f.friend.id = :userId")
    List<Friend> findAllByUserIdOrFriendIdWithFetchJoin(@Param("userId") Long userId);

    @Query("SELECT COUNT(f) > 0 FROM Friend f WHERE (f.user = :u1 AND f.friend = :u2) OR (f.user = :u2 AND f.friend = :u1)")
    boolean existsFriendship(@Param("u1") User u1, @Param("u2") User u2);

    @Query("SELECT f FROM Friend f JOIN FETCH f.friend WHERE f.user.id = :userId AND f.status = :status")
    List<Friend> findFriendsByMe(@Param("userId") Long userId, @Param("status") FriendStatus status);
}