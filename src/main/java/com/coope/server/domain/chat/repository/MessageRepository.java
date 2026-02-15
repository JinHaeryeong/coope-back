package com.coope.server.domain.chat.repository;

import com.coope.server.domain.chat.entity.ChatRoom;
import com.coope.server.domain.chat.entity.Message;
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
}
