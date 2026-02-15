package com.coope.server.domain.chat.dto;

import com.coope.server.domain.chat.entity.ChatRoom;
import com.coope.server.domain.chat.entity.RoomType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatListResponse {
    private Long roomId;
    private String title;
    private RoomType type;
    private String lastMessage;
    private LocalDateTime lastMessageTime;

    public static ChatListResponse of(ChatRoom room) {
        return ChatListResponse.builder()
                .roomId(room.getId())
                .title(room.getTitle())
                .type(room.getType())
//                .lastMessage(lastMessage) // 나중에 해줄것
//                .lastMessageTime(lastMessageTime) // 나중에 해줄것
                .build();
    }
}