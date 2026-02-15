package com.coope.server.domain.chat.entity;

import com.coope.server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "chat_participants",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_chat_room_user",
                        columnNames = {"chat_room_id", "user_id"}
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
}