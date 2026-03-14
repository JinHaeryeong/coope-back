package com.coope.server.chat.infrastructure;

import com.coope.server.chat.domain.ChatParticipant;
import com.coope.server.chat.domain.ChatParticipantRepository;
import com.coope.server.chat.domain.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ChatParticipantRepositoryImpl implements ChatParticipantRepository {

    private final ChatParticipantJpaRepository chatParticipantJpaRepository;

    @Override
    public Slice<ChatParticipant> findAllByUserId(Long userId, Pageable pageable) {
        return chatParticipantJpaRepository.findAllByUserId(userId, pageable);
    }

    @Override
    public Optional<ChatRoom> find1on1RoomBetween(Long myId, Long friendId) {
        return chatParticipantJpaRepository.find1on1RoomBetween(myId, friendId);
    }

    @Override
    public boolean existsByChatRoomIdAndUserId(Long roomId, Long userId) {
        return chatParticipantJpaRepository.existsByChatRoomIdAndUserId(roomId, userId);
    }

    @Override
    public void updateLastMessageInfoByRoom(Long roomId, LocalDateTime time, String content) {
        chatParticipantJpaRepository.updateLastMessageInfoByRoom(roomId, time, content);
    }

    @Override
    public List<ChatParticipant> findByChatRoomId(Long roomId) {
        return chatParticipantJpaRepository.findByChatRoomId(roomId);
    }

    @Override
    public long countByChatRoomId(Long roomId) {
        return chatParticipantJpaRepository.countByChatRoomId(roomId);
    }

    @Override
    public Optional<ChatParticipant> findByChatRoomIdAndUserId(Long roomId, Long userId) {
        return chatParticipantJpaRepository.findByChatRoomIdAndUserId(roomId, userId);
    }

    @Override
    public void delete(ChatParticipant participant) {
        chatParticipantJpaRepository.delete(participant);
    }
}
