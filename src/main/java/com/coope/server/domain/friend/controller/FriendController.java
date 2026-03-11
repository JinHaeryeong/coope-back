package com.coope.server.domain.friend.controller;

import com.coope.server.domain.friend.dto.FriendResponse;
import com.coope.server.domain.friend.entity.FriendStatus;
import com.coope.server.domain.friend.service.FriendService;
import com.coope.server.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Friend", description = "친구 관리 API")
@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "BearerAuth")
public class FriendController {

    private final FriendService friendService;

    @Operation(summary = "친구 및 요청 목록 조회", description = "상태(ACCEPTED, PENDING)에 따라 친구 목록 또는 내가 보낸 요청 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<FriendResponse>> getFriends(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false, defaultValue = "ACCEPTED") FriendStatus status) {
        return ResponseEntity.ok(friendService.getFriends(userDetails.getUser().getId(), status));
    }

    @Operation(summary = "받은 친구 요청 목록 조회", description = "다른 사용자가 나에게 보낸 친구 신청 목록을 조회합니다.")
    @GetMapping("/received")
    public ResponseEntity<List<FriendResponse>> getReceivedRequests(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(friendService.getReceivedRequests(userDetails.getUser().getId()));
    }

    @Operation(summary = "친구 요청 보내기", description = "특정 사용자에게 친구 신청을 보냅니다.")
    @PostMapping("/{friendId}")
    public ResponseEntity<Void> sendRequest(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long friendId) {
        friendService.sendFriendRequest(userDetails.getUser().getId(), friendId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "친구 요청 수락", description = "나에게 온 친구 신청을 수락하여 양방향 친구 관계를 맺습니다.")
    @PatchMapping("/{friendId}/accept")
    public ResponseEntity<Void> acceptRequest(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long friendId) {
        friendService.acceptFriendRequest(userDetails.getUser().getId(), friendId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "친구 삭제 및 요청 거절", description = "친구 관계를 끊거나, 나에게 온 요청을 거절하거나, 내가 보낸 요청을 취소합니다.")
    @DeleteMapping("/{friendId}")
    public ResponseEntity<Void> deleteFriend(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long friendId) {
        friendService.deleteFriend(userDetails.getUser().getId(), friendId);
        return ResponseEntity.noContent().build();
    }
}