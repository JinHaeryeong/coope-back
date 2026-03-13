package com.coope.server.domain.chat.entity;

import com.coope.server.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "chat_participants",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_chat_room_user",
                        columnNames = {"chat_room_id", "user_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_cp_user_lastmsg",
                        columnList = "user_id, last_message_time DESC"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "last_message_time")
    private LocalDateTime lastMessageTime;

    @Column(length = 1000)
    private String lastMessageContent;

    @Builder
    public ChatParticipant(ChatRoom chatRoom, User user) {
        this.chatRoom = chatRoom;
        this.user = user;
    }

    public static ChatParticipant of(ChatRoom chatRoom, User user) {
        return ChatParticipant.builder()
                .chatRoom(chatRoom)
                .user(user)
                .build();
    }

    public void updateLastMessage(LocalDateTime time, String content) {
        this.lastMessageTime = time;
        this.lastMessageContent = content;
    }
}