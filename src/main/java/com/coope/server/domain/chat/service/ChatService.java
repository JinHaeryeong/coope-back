package com.coope.server.domain.chat.service;

import com.coope.server.domain.chat.dto.*;
import com.coope.server.domain.chat.entity.ChatParticipant;
import com.coope.server.domain.chat.entity.ChatRoom;
import com.coope.server.domain.chat.entity.Message;
import com.coope.server.domain.chat.repository.ChatParticipantRepository;
import com.coope.server.domain.chat.repository.ChatRoomRepository;
import com.coope.server.domain.chat.repository.MessageRepository;
import com.coope.server.domain.user.entity.User;
import com.coope.server.domain.user.repository.UserRepository;
import com.coope.server.global.error.exception.AccessDeniedException;
import com.coope.server.global.infra.file.FileService;
import com.coope.server.global.infra.file.ImageCategory;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final FileService fileService;

    @Transactional
    public ChatRoomResponse createOrGet1on1Room(Long myId, Long friendId) {
        validateSelfChat(myId, friendId);

        ChatRoom room = participantRepository.find1on1RoomBetween(myId, friendId)
                .orElseGet(() -> createNew1on1Room(myId, friendId));

        return ChatRoomResponse.from(room);
    }

    @Transactional
    public ChatRoomResponse createGroupRoom(Long creatorId, List<Long> friendIds, String roomName) {
        if (friendIds == null || friendIds.isEmpty()) {
            throw new IllegalArgumentException("참여자가 없습니다.");
        }

        Set<Long> participantIds = new HashSet<>(friendIds);
        participantIds.add(creatorId);
        List<User> users = userRepository.findAllById(participantIds);

        ChatRoom groupRoom = ChatRoom.createGroup(roomName, users);

        chatRoomRepository.save(groupRoom);
        return ChatRoomResponse.from(groupRoom);
    }

    public Slice<MessageResponse> getChatMessages(Long roomId, Long userId, Long lastMessageId, Pageable pageable) {
        validateParticipant(roomId, userId);

        return messageRepository.findByChatRoomIdCursor(roomId, lastMessageId, pageable)
                .map(MessageResponse::from);
    }

    @Transactional
    public MessageResponse saveMessage(MessageRequest request, Long authenticatedUserId) {
        if ((request.getContent() == null || request.getContent().trim().isEmpty())
                && (request.getFileUrl() == null || request.getFileUrl().isEmpty())) {
            log.warn("[Chat] 텍스트와 파일이 모두 없는 메시지 전송 시도 차단");
            return null;
        }

        ChatRoom chatRoom = findChatRoom(request.getRoomId());
        User sender = findUser(authenticatedUserId);

        validateParticipant(chatRoom.getId(), sender.getId());
        Message message = Message.createTalkMessage(chatRoom, sender, request);
        Message saved = messageRepository.save(message);

        String lastContent = (saved.getContent() != null && !saved.getContent().isEmpty())
                ? saved.getContent() : "사진을 보냈습니다.";

        participantRepository.updateLastMessageInfoByRoom(chatRoom.getId(), saved.getCreatedAt(), lastContent);

        sendChatUpdateNotifications(chatRoom, saved);
        return MessageResponse.from(saved);
    }

    public Slice<ChatListResponse> getMyChatRooms(Long userId, Pageable pageable) {
        Slice<ChatParticipant> slice = participantRepository.findAllByUserId(userId, pageable);

        return slice.map(cp -> ChatListResponse.of(
                cp.getChatRoom(),
                cp.getLastMessageContent(),
                cp.getLastMessageTime()
        ));
    }

    @Transactional
    public void leaveRoom(Long roomId, Long userId) {
        ChatParticipant participant = participantRepository.findByChatRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new AccessDeniedException("채팅방 참여 정보가 없습니다."));

        ChatRoom room = participant.getChatRoom();
        User user = participant.getUser();

        Message leaveMsg = messageRepository.save(Message.createLeaveMessage(room, user));
        participantRepository.updateLastMessageInfoByRoom(roomId, leaveMsg.getCreatedAt(), leaveMsg.getContent());

        broadcastLeaveInfo(room, leaveMsg, userId);
        participantRepository.delete(participant);

        if (participantRepository.countByChatRoomId(roomId) == 0) {
            chatRoomRepository.delete(room);
        }
    }

    @Transactional
    public ChatUploadResponse uploadChatFile(Long roomId, Long userId, MultipartFile file) {
        validateParticipant(roomId, userId);

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        String url = fileService.upload(file, ImageCategory.CHAT);

        return ChatUploadResponse.builder()
                .fileUrl(url)
                .fileName(file.getOriginalFilename())
                .fileFormat(file.getContentType())
                .build();
    }

    public ResponseEntity<Resource> downloadChatFile(Long roomId, Long userId, String fileUrl, String fileName, ImageCategory category) {
        validateParticipant(roomId, userId);

        Resource resource = fileService.loadAsResource(fileUrl, category);

        String encodedFileName = UriUtils.encode(fileName, StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + encodedFileName + "\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }

    private void validateParticipant(Long roomId, Long userId) {
        if (!participantRepository.existsByChatRoomIdAndUserId(roomId, userId)) {
            throw new AccessDeniedException("채팅방 접근 권한이 없습니다.");
        }
    }

    private void sendChatUpdateNotifications(ChatRoom room, Message msg) {
        ChatListResponse updateDto = ChatListResponse.of(room, msg.getContent(), msg.getCreatedAt());

        participantRepository.findByChatRoomId(room.getId()).forEach(participant ->
                messagingTemplate.convertAndSendToUser(
                        participant.getUser().getId().toString(),
                        "/queue/chat/updates",
                        updateDto
                )
        );
    }

    private void validateSelfChat(Long myId, Long friendId) {
        if (myId.equals(friendId)) {
            throw new IllegalArgumentException("자신과 대화할 수 없습니다.");
        }
    }

    private ChatRoom createNew1on1Room(Long myId, Long friendId) {
        User me = findUser(myId);
        User friend = findUser(friendId);

        ChatRoom newRoom = ChatRoom.createIndividual(
                String.format("%s, %s의 대화", friend.getNickname(), me.getNickname()),
                me,
                friend
        );

        return chatRoomRepository.save(newRoom);
    }

    private ChatRoom findChatRoom(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다."));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
    }

    private void broadcastLeaveInfo(ChatRoom room, Message leaveMsg, Long userId) {
        MessageResponse response = MessageResponse.from(leaveMsg);
        messagingTemplate.convertAndSend("/topic/chat/" + room.getId(), response);
        sendChatUpdateNotifications(room, leaveMsg);
        messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/chat/updates", response);
    }
}