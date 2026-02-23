package com.coope.server.domain.chat.repository;

import com.coope.server.domain.chat.entity.ChatParticipant;
import com.coope.server.domain.chat.entity.ChatRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {


    @Query("""
SELECT cp
FROM ChatParticipant cp
JOIN FETCH cp.chatRoom
WHERE cp.user.id = :userId
ORDER BY COALESCE(cp.lastMessageTime, cp.chatRoom.createdAt) DESC
""")
    Slice<ChatParticipant> findAllByUserId(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query("SELECT cp1.chatRoom FROM ChatParticipant cp1 " +
            "JOIN ChatParticipant cp2 ON cp1.chatRoom = cp2.chatRoom " +
            "WHERE cp1.user.id = :myId AND cp2.user.id = :friendId " +
            "AND cp1.chatRoom.type = 'INDIVIDUAL'")
    Optional<ChatRoom> find1on1RoomBetween(@Param("myId") Long myId, @Param("friendId") Long friendId);

    boolean existsByChatRoomIdAndUserId(Long roomId, Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE ChatParticipant cp
        SET cp.lastMessageTime = :time,
            cp.lastMessageContent = :content
        WHERE cp.chatRoom.id = :roomId
        AND (cp.lastMessageTime IS NULL OR cp.lastMessageTime < :time)
    """)
    void updateLastMessageInfoByRoom(
            @Param("roomId") Long roomId,
            @Param("time") LocalDateTime time,
            @Param("content") String content
    );

    @Query("SELECT cp FROM ChatParticipant cp JOIN FETCH cp.user WHERE cp.chatRoom.id = :roomId")
    List<ChatParticipant> findByChatRoomId(@Param("roomId") Long roomId);
}