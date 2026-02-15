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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Transactional
    public ChatRoomResponse createOrGet1on1Room(Long myId, Long friendId) {
        if (myId.equals(friendId)) {
            throw new IllegalArgumentException("자신과 대화할 수 없습니다.");
        }

        ChatRoom room = participantRepository.find1on1RoomBetween(myId, friendId)
                .orElseGet(() -> {
                    User me = userRepository.findById(myId)
                            .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
                    User friend = userRepository.findById(friendId)
                            .orElseThrow(() -> new RuntimeException("친구를 찾을 수 없습니다."));

                    ChatRoom newRoom = ChatRoom.createIndividual(friend.getNickname() + ", " + me.getNickname() + "의 대화");
                    chatRoomRepository.save(newRoom);

                    participantRepository.save(ChatParticipant.builder().chatRoom(newRoom).user(me).build());
                    participantRepository.save(ChatParticipant.builder().chatRoom(newRoom).user(friend).build());

                    return newRoom;
                });

        return ChatRoomResponse.from(room);
    }

    @Transactional
    public ChatRoomResponse createGroupRoom(Long creatorId, List<Long> friendIds, String roomName) {
        Set<Long> participantIds = new HashSet<>(friendIds);
        participantIds.add(creatorId);

        List<User> participants = userRepository.findAllById(participantIds);

        String finalTitle = roomName;
        if (roomName == null || roomName.trim().isEmpty()) {
            finalTitle = participants.stream()
                    .map(User::getNickname)
                    .limit(3)
                    .collect(Collectors.joining(", ")) + (participantIds.size() > 3 ? " 외 " + (participantIds.size() - 3) + "명" : "의 대화");
        }

        ChatRoom groupRoom = ChatRoom.createGroup(finalTitle);
        chatRoomRepository.save(groupRoom);

        List<ChatParticipant> chatParticipants = participants.stream()
                .map(user -> ChatParticipant.of(groupRoom, user))
                .toList();

        participantRepository.saveAll(chatParticipants);

        return ChatRoomResponse.from(groupRoom);
    }


    public List<MessageResponse> getChatMessages(Long roomId, Long userId) {
        if (!participantRepository.existsByChatRoomIdAndUserId(roomId, userId)) {
            throw new AccessDeniedException("채팅방 접근 권한이 없습니다.");
        }

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다"));

        return messageRepository.findByChatRoomWithUser(chatRoom)
                .stream()
                .map(MessageResponse::of)
                .collect(Collectors.toList());
    }

    @Transactional
    public MessageResponse saveMessage(MessageRequest request) {
        ChatRoom chatRoom = chatRoomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다."));

        User sender = userRepository.findById(request.getSenderId())
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));

        Message message = request.toEntity(chatRoom, sender);
        messageRepository.save(message);

        return MessageResponse.of(message);
    }

    public Page<ChatListResponse> getMyChatRooms(Long userId, Pageable pageable) {
        Page<ChatRoom> rooms = participantRepository.findAllRoomsByUserId(userId, pageable);

        return rooms.map(ChatListResponse::of);
    }
}