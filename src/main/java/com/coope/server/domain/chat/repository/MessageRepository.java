package com.coope.server.domain.chat.repository;

import com.coope.server.domain.chat.entity.ChatRoom;
import com.coope.server.domain.chat.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m " +
            "JOIN FETCH m.user " +
            "WHERE m.chatRoom = :chatRoom " +
            "ORDER BY m.createdAt ASC")
    List<Message> findByChatRoomWithUser(@Param("chatRoom") ChatRoom chatRoom);

    @Query("SELECT m FROM Message m " +
            "JOIN FETCH m.user " +
            "WHERE m.chatRoom.id = :roomId " +
            "AND (:lastMessageId IS NULL OR m.id < :lastMessageId) " +
            "ORDER BY m.id DESC")
    Slice<Message> findByChatRoomIdCursor(
            @Param("roomId") Long roomId,
            @Param("lastMessageId") Long lastMessageId,
            Pageable pageable
    );
}
