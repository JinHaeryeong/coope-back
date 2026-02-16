package com.coope.server.domain.chat.dto;

import com.coope.server.domain.chat.entity.ChatRoom;
import com.coope.server.domain.chat.entity.RoomType;
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
    private ChatListResponse(Long roomId, String title, RoomType type, String lastMessage, LocalDateTime lastMessageTime) {
        this.roomId = roomId;
        this.title = title;
        this.type = type;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
    }

    public static ChatListResponse from(ChatRoom room) {
        return ChatListResponse.builder()
                .roomId(room.getId())
                .title(room.getTitle())
                .type(room.getType())
//                .lastMessage(lastMessage) // 나중에 해줄것
//                .lastMessageTime(lastMessageTime) // 나중에 해줄것
                .build();
    }
}