package com.coope.server.domain.friend.controller;

import com.coope.server.domain.friend.dto.FriendResponse;
import com.coope.server.domain.friend.entity.FriendStatus;
import com.coope.server.domain.friend.service.FriendService;
import com.coope.server.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    /**
     * 친구 목록 조회 (ACCEPTED, PENDING 등)
     * GET /api/friends?status=ACCEPTED
     */
    // FriendController.java
    @GetMapping
    public ResponseEntity<List<FriendResponse>> getFriends(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false, defaultValue = "ACCEPTED") FriendStatus status) {

        // 💡 프론트엔드 친구 목록 페이지에서는 인자 없이 호출하면 ACCEPTED인 친구만 싹 가져옵니다.
        List<FriendResponse> responses = friendService.getFriends(userDetails.getUser().getId(), status);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/requests/received")
    public ResponseEntity<List<FriendResponse>> getReceivedRequests(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        List<FriendResponse> responses = friendService.getReceivedRequests(userDetails.getUser().getId());
        return ResponseEntity.ok(responses);
    }

    /**
     * 친구 요청 보내기
     * POST /api/friends/request?friendId=2
     */
    @PostMapping("/request")
    public ResponseEntity<Void> sendRequest(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam Long friendId) {

        friendService.sendFriendRequest(userDetails.getUser().getId(), friendId);
        return ResponseEntity.ok().build();
    }

    /**
     * 친구 요청 수락
     * POST /api/friends/accept?friendId=2
     */
    @PostMapping("/accept")
    public ResponseEntity<Void> acceptRequest(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam Long friendId) {

        friendService.acceptFriendRequest(userDetails.getUser().getId(), friendId);
        return ResponseEntity.ok().build();
    }

    /**
     * 4. 친구 삭제 또는 요청 거절
     * DELETE /api/friends?friendId=2
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteFriend(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam Long friendId) {

        friendService.deleteFriend(userDetails.getUser().getId(), friendId);
        return ResponseEntity.ok().build();
    }
}