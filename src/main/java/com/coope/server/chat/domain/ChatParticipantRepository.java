package com.coope.server.chat.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatParticipantRepository {
    Slice<ChatParticipant> findAllByUserId(Long userId, Pageable pageable);
    Optional<ChatRoom> find1on1RoomBetween(Long myId, Long friendId);
    boolean existsByChatRoomIdAndUserId(Long roomId, Long userId);
    void updateLastMessageInfoByRoom(Long roomId, LocalDateTime time, String content);
    List<ChatParticipant> findByChatRoomId(Long roomId);
    long countByChatRoomId(Long roomId);
    Optional<ChatParticipant> findByChatRoomIdAndUserId(Long roomId, Long userId);
    void delete(ChatParticipant participant);
}
