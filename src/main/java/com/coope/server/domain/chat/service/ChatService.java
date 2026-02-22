package com.coope.server.domain.chat.service;

import com.coope.server.domain.chat.dto.ChatListResponse;
import com.coope.server.domain.chat.dto.ChatRoomResponse;
import com.coope.server.domain.chat.dto.MessageRequest;
import com.coope.server.domain.chat.dto.MessageResponse;
import com.coope.server.domain.chat.entity.ChatParticipant;
import com.coope.server.domain.chat.entity.ChatRoom;
import com.coope.server.domain.chat.entity.Message;
import com.coope.server.domain.chat.repository.ChatParticipantRepository;
import com.coope.server.domain.chat.repository.ChatRoomRepository;
import com.coope.server.domain.chat.repository.MessageRepository;
import com.coope.server.domain.user.entity.User;
import com.coope.server.domain.user.repository.UserRepository;
import com.coope.server.global.error.exception.AccessDeniedException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final RedisTemplate<String, String> redisTemplate;


    @Transactional
    public ChatRoomResponse createOrGet1on1Room(Long myId, Long friendId) {
        if (myId.equals(friendId)) {
            throw new IllegalArgumentException("자신과 대화할 수 없습니다.");
        }

        ChatRoom room = participantRepository.find1on1RoomBetween(myId, friendId)
                .orElseGet(() -> {
                    User me = userRepository.findById(myId)
                            .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
                    User friend = userRepository.findById(friendId)
                            .orElseThrow(() -> new EntityNotFoundException("친구를 찾을 수 없습니다."));

                    ChatRoom newRoom = ChatRoom.createIndividual(
                            friend.getNickname() + ", " + me.getNickname() + "의 대화",
                            me,
                            friend
                    );

                    return chatRoomRepository.save(newRoom);
                });

        return ChatRoomResponse.from(room);
    }

    @Transactional
    public ChatRoomResponse createGroupRoom(Long creatorId, List<Long> friendIds, String roomName) {
        Set<Long> participantIds = new HashSet<>(friendIds);
        participantIds.add(creatorId);
        List<User> users = userRepository.findAllById(participantIds);

        String finalTitle = determineTitle(roomName, users);

        ChatRoom groupRoom = ChatRoom.createGroup(finalTitle, users);

        chatRoomRepository.save(groupRoom);

        return ChatRoomResponse.from(groupRoom);
    }


    public Slice<MessageResponse> getChatMessages(
            Long roomId,
            Long userId,
            Long lastMessageId,
            Pageable pageable) {

        boolean exists = participantRepository.existsByChatRoomIdAndUserId(roomId, userId);
        if (!exists) {
            throw new AccessDeniedException("채팅방 접근 권한이 없습니다.");
        }

        return messageRepository.findByChatRoomIdCursor(roomId, lastMessageId, pageable)
                .map(MessageResponse::from);
    }



    @Transactional
    public MessageResponse saveMessage(MessageRequest request, Long authenticatedUserId) {
        if (!participantRepository.existsByChatRoomIdAndUserId(request.getRoomId(), authenticatedUserId)) {
            throw new AccessDeniedException("채팅방 접근 권한이 없습니다.");
        }
        ChatRoom chatRoom = chatRoomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다."));

        User sender = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));

        Message message = request.toEntity(chatRoom, sender);
        messageRepository.save(message);

        Message saved = messageRepository.save(message);

        participantRepository.updateLastMessageInfoByRoom(
                chatRoom.getId(),
                saved.getCreatedAt(),
                saved.getContent()
        );

        String cacheKey = "chat:room:" + request.getRoomId() + ":page:0";
        redisTemplate.delete(cacheKey);

        return MessageResponse.from(message);
    }

    public Slice<ChatListResponse> getMyChatRooms(Long userId, Pageable pageable) {
        Slice<ChatParticipant> slice =
                participantRepository.findAllByUserId(userId, pageable);

        return slice.map(cp -> ChatListResponse.of(
                cp.getChatRoom(),
                cp.getLastMessageContent(),
                cp.getLastMessageTime()
        ));
    }

    private String determineTitle(String roomName, List<User> participants) {
        if (roomName != null && !roomName.trim().isEmpty()) {
            return roomName;
        }

        return participants.stream()
                .map(User::getNickname)
                .limit(3)
                .collect(Collectors.joining(", "))
                + (participants.size() > 3 ? " 외 " + (participants.size() - 3) + "명" : "의 대화");
    }
}