package com.coope.server.domain.chat.repository;

import com.coope.server.domain.chat.entity.ChatParticipant;
import com.coope.server.domain.chat.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
    // 1:1 방이 이미 있는지 확인
    @Query("SELECT cp1.chatRoom FROM ChatParticipant cp1 " +
            "JOIN ChatParticipant cp2 ON cp1.chatRoom = cp2.chatRoom " +
            "WHERE cp1.user.id = :myId AND cp2.user.id = :friendId " +
            "AND cp1.chatRoom.type = 'INDIVIDUAL'")
    Optional<ChatRoom> find1on1RoomBetween(@Param("myId") Long myId, @Param("friendId") Long friendId);

    // ChatParticipantRepository.java
    @Query("SELECT cp.chatRoom FROM ChatParticipant cp WHERE cp.user.id = :userId")
    Page<ChatRoom> findAllRoomsByUserId(@Param("userId") Long userId, Pageable pageable);

    boolean existsByChatRoomIdAndUserId(Long roomId, Long userId);
}