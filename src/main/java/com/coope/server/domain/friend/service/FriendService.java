package com.coope.server.domain.friend.service;

import com.coope.server.domain.friend.dto.FriendResponse;
import com.coope.server.domain.friend.entity.Friend;
import com.coope.server.domain.friend.entity.FriendStatus;
import com.coope.server.domain.friend.repository.FriendRepository;
import com.coope.server.domain.user.entity.User;
import com.coope.server.domain.user.repository.UserRepository;
import com.coope.server.global.error.exception.FriendException;
import com.coope.server.global.error.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendService {
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    @Transactional
    public void sendFriendRequest(Long currentUserId, Long friendId) {
        if (currentUserId.equals(friendId)) {
            throw new FriendException("본인에게는 친구 신청을 할 수 없습니다.");
        }

        User me = findUserById(currentUserId);
        User targetFriend = findUserById(friendId);

        if (friendRepository.existsFriendship(me, targetFriend)) {
            throw new FriendException("이미 신청 중이거나 친구 관계입니다.");
        }

        Friend request = Friend.sendRequest(me, targetFriend);
        friendRepository.save(request);
    }

    // 친구 수락 (양방향 데이터 생성)
    @Transactional
    public void acceptFriendRequest(Long currentUserId, Long friendId) {
        User me = findUserById(currentUserId);
        User friend = findUserById(friendId);

        // 상대방이 나에게 보낸 PENDING 요청 찾기
        Friend request = friendRepository.findByUserAndFriend(friend, me)
                .orElseThrow(() -> new FriendException("받은 친구 신청 내역이 없습니다."));

        if (request.getStatus() != FriendStatus.PENDING) {
            throw new FriendException("이미 처리된 요청입니다.");
        }

        request.updateStatus(FriendStatus.ACCEPTED);

        // 반대 방향 데이터 생성 (나 -> 상대방)
        createInverseFriendshipIfAbsent(me, friend);
    }

    public List<FriendResponse> getReceivedRequests(Long currentUserId) {
        return friendRepository.findAllByFriendIdAndStatus(currentUserId, FriendStatus.PENDING)
                .stream()
                .map(FriendResponse::of)
                .collect(Collectors.toList());
    }

    // 친구 삭제 또는 거절
    @Transactional
    public void deleteFriend(Long currentUserId, Long friendId) {
        User me = findUserById(currentUserId);
        User friend = findUserById(friendId);

        // 양방향 모두 삭제
        int deletedCount = friendRepository.deleteFriendship(me, friend);

        if (deletedCount == 0) {
            throw new FriendException("삭제할 친구 관계가 존재하지 않습니다.");
        }
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 계정입니다."));
    }

    private void createInverseFriendshipIfAbsent(User me, User friend) {
        if (friendRepository.findByUserAndFriend(me, friend).isEmpty()) {
            Friend inverseFriend = Friend.createFriendship(me, friend, FriendStatus.ACCEPTED);
            friendRepository.save(inverseFriend);
        }
    }

    public List<FriendResponse> getFriends(Long userId, FriendStatus status) {
        return friendRepository.findAllByUserIdOrFriendIdWithFetchJoin(userId).stream()
                .filter(f -> f.getStatus() == status)
                .map(f -> {
                    User targetUser = f.getUser().getId().equals(userId) ? f.getFriend() : f.getUser();
                    return FriendResponse.of(f, targetUser);
                })
                .collect(Collectors.toList());
    }

    public String getRelationStatus(Long userId, Long targetId) {
        // 양방향 중 하나라도 있으면 해당 상태 반환, 없으면 "NONE"
        return friendRepository.findStatusBetweenUsers(userId, targetId)
                .map(Enum::name)
                .orElse("NONE");
    }
}