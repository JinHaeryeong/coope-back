package com.coope.server.domain.chat.controller;

import com.coope.server.domain.chat.dto.*;
import com.coope.server.domain.chat.service.ChatService;
import com.coope.server.global.infra.file.ImageCategory;
import com.coope.server.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/rooms")
    public ResponseEntity<Slice<ChatListResponse>> getMyChatRooms(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PageableDefault Pageable pageable
    ) {
        return ResponseEntity.ok(chatService.getMyChatRooms(userDetails.getUser().getId(), pageable));
    }

    @PostMapping("/rooms/individual")
    public ResponseEntity<ChatRoomResponse> getOrCreateIndividualRoom(
            @RequestParam Long friendId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(chatService.createOrGet1on1Room(userDetails.getUser().getId(), friendId));
    }

    @PostMapping("/rooms/group")
    public ResponseEntity<ChatRoomResponse> createGroupRoom(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody CreateGroupRequest request
    ) {
        return ResponseEntity.ok(chatService.createGroupRoom(userDetails.getUser().getId(), request.getUserIds(), request.getRoomName()));
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<Slice<MessageResponse>> getChatMessages(
            @PathVariable Long roomId,
            @RequestParam(required = false) Long lastMessageId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(chatService.getChatMessages(roomId, userDetails.getUser().getId(), lastMessageId, pageable));
    }

    @DeleteMapping("/rooms/{roomId}/leave")
    public ResponseEntity<Void> leaveChatRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        chatService.leaveRoom(roomId, userDetails.getUser().getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/rooms/{roomId}/files/upload")
    public ResponseEntity<ChatUploadResponse> uploadFile(
            @PathVariable Long roomId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(chatService.uploadChatFile(roomId, userDetails.getUser().getId(), file));
    }

    @GetMapping("/rooms/{roomId}/files/download")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long roomId,
            @RequestParam String fileUrl,
            @RequestParam String fileName,
            @RequestParam ImageCategory category,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return chatService.downloadChatFile(roomId, userDetails.getUser().getId(), fileUrl, fileName, category);
    }
}