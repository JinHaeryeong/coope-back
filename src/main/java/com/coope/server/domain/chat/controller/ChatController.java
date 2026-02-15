package com.coope.server.domain.chat.controller;

import com.coope.server.domain.chat.dto.*;
import com.coope.server.domain.chat.service.ChatService;
import com.coope.server.global.infra.FileService;
import com.coope.server.global.infra.ImageCategory;
import com.coope.server.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final FileService fileService;

    // POST /api/chat/room/individual?friendId=5
    @PostMapping("/room/individual")
    public ResponseEntity<ChatRoomResponse> getOrCreateIndividualRoom(
            @RequestParam Long friendId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        ChatRoomResponse room = chatService.createOrGet1on1Room(userId, friendId);

        return ResponseEntity.ok(room);
    }

    @GetMapping("/rooms")
    public ResponseEntity<Page<ChatListResponse>> getMyChatRooms(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PageableDefault Pageable pageable
    ) {
        Long userId = userDetails.getUser().getId();
        Page<ChatListResponse> rooms = chatService.getMyChatRooms(userId, pageable);
        return ResponseEntity.ok(rooms);
    }

    @PostMapping("/room/group")
    public ResponseEntity<ChatRoomResponse> createGroupRoom(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody CreateGroupRequest request
    ) {
        Long userId = userDetails.getUser().getId();
        ChatRoomResponse room = chatService.createGroupRoom(userId, request.getUserIds(), request.getRoomName());
        return ResponseEntity.ok(room);
    }

    @GetMapping("/room/{roomId}/messages")
    public ResponseEntity<List<MessageResponse>> getChatMessages(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        List<MessageResponse> messages = chatService.getChatMessages(roomId, userId);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/upload")
    public ResponseEntity<ChatUploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        String url = fileService.upload(file, ImageCategory.CHAT);

        ChatUploadResponse response = ChatUploadResponse.builder()
                .fileUrl(url)
                .fileName(file.getOriginalFilename())
                .fileFormat(file.getContentType())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(
            @RequestParam String fileUrl,
            @RequestParam String fileName,
            @RequestParam ImageCategory category
    ) {
        Resource resource = fileService.loadAsResource(fileUrl, category);

        String encodedFileName = UriUtils.encode(fileName, StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + encodedFileName + "\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }
}