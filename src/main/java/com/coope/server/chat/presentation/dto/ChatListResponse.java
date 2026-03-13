package com.coope.server.chat.presentation.dto;

import com.coope.server.chat.domain.ChatRoom;
import com.coope.server.chat.domain.RoomType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ChatListResponse {
    private final Long roomId;
    private final String title;
    private final RoomType type;
    private final String lastMessage;
    private final LocalDateTime lastMessageTime;

    @Builder
    private ChatListResponse(Long roomId, String title, RoomType type,
                              String lastMessage, LocalDateTime lastMessageTime) {
        this.roomId = roomId;
        this.title = title;
        this.type = type;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
    }

    public static ChatListResponse of(ChatRoom room, String lastMessage, LocalDateTime lastMessageTime) {
        return ChatListResponse.builder()
                .roomId(room.getId())
                .title(room.getTitle())
                .type(room.getType())
                .lastMessage(lastMessage != null ? lastMessage : "대화 내용이 없습니다.")
                .lastMessageTime(lastMessageTime != null ? lastMessageTime : room.getCreatedAt())
                .build();
    }
}
