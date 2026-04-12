package com.coope.server.friend.presentation;

import com.coope.server.friend.application.FriendService;
import com.coope.server.friend.domain.FriendStatus;
import com.coope.server.friend.application.dto.FriendResponse;
import com.coope.server.shared.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Friend", description = "친구 관리 API")
@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "BearerAuth")
public class FriendController {

    private final FriendService friendService;

    @Operation(summary = "친구 및 요청 목록 조회")
    @GetMapping
    public ResponseEntity<List<FriendResponse>> getFriends(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false, defaultValue = "ACCEPTED") FriendStatus status) {
        return ResponseEntity.ok(friendService.getFriends(userDetails.getUser().getId(), status));
    }

    @Operation(summary = "받은 친구 요청 목록 조회")
    @GetMapping("/received")
    public ResponseEntity<List<FriendResponse>> getReceivedRequests(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(friendService.getReceivedRequests(userDetails.getUser().getId()));
    }

    @Operation(summary = "친구 요청 보내기")
    @PostMapping("/{friendId}")
    public ResponseEntity<Void> sendRequest(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long friendId) {
        friendService.sendFriendRequest(userDetails.getUser().getId(), friendId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "친구 요청 수락")
    @PatchMapping("/{friendId}/accept")
    public ResponseEntity<Void> acceptRequest(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long friendId) {
        friendService.acceptFriendRequest(userDetails.getUser().getId(), friendId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "친구 삭제 및 요청 거절")
    @DeleteMapping("/{friendId}")
    public ResponseEntity<Void> deleteFriend(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long friendId) {
        friendService.deleteFriend(userDetails.getUser().getId(), friendId);
        return ResponseEntity.noContent().build();
    }
}
