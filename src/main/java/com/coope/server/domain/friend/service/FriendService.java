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
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void sendFriendRequest(Long currentUserId, Long friendId) {
        validateNotSelf(currentUserId, friendId);

        User me = findUserById(currentUserId);
        User targetFriend = findUserById(friendId);

        if (friendRepository.existsFriendship(me, targetFriend)) {
            throw new FriendException("이미 신청 중이거나 친구 관계입니다.");
        }

        Friend request = Friend.sendRequest(me, targetFriend);
        friendRepository.save(request);

        notifyFriendUpdate(friendId);
    }

    @Transactional
    public void acceptFriendRequest(Long currentUserId, Long friendId) {
        User me = findUserById(currentUserId);
        User friend = findUserById(friendId);

        Friend request = friendRepository.findByUserAndFriend(friend, me)
                .orElseThrow(() -> new FriendException("받은 친구 신청 내역이 없습니다."));

        request.accept();

        createInverseFriendshipIfAbsent(me, friend);
        notifyFriendUpdate(friendId);
    }

    public List<FriendResponse> getReceivedRequests(Long currentUserId) {
        return friendRepository.findAllByFriendIdAndStatus(currentUserId, FriendStatus.PENDING)
                .stream()
                .map(FriendResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteFriend(Long currentUserId, Long friendId) {
        User me = findUserById(currentUserId);
        User friend = findUserById(friendId);

        int deletedCount = friendRepository.deleteFriendship(me, friend);
        if (deletedCount == 0) {
            throw new FriendException("삭제할 친구 관계가 존재하지 않습니다.");
        }

        notifyFriendUpdate(friendId);
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
        return friendRepository.findFriendsByMe(userId, status).stream()
                .map(f -> FriendResponse.of(f, f.getFriend()))
                .collect(Collectors.toList());
    }

    public String getRelationStatus(Long userId, Long targetId) {
        return friendRepository.findStatusBetweenUsers(userId, targetId)
                .map(Enum::name)
                .orElse("NONE");
    }

    private void validateNotSelf(Long currentUserId, Long friendId) {
        if (currentUserId.equals(friendId)) {
            throw new FriendException("본인에게는 친구 신청을 할 수 없습니다.");
        }
    }

    private void notifyFriendUpdate(Long targetUserId) {
        messagingTemplate.convertAndSendToUser(
                targetUserId.toString(),
                "/queue/friend-update",
                "REFRESH"
        );
    }
}