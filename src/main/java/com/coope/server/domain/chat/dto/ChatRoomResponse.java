package com.coope.server.domain.chat.dto;

import com.coope.server.domain.chat.entity.ChatRoom;
import lombok.Builder;
import lombok.Getter;

// ChatRoomResponse.java
@Getter
@Builder
public class ChatRoomResponse {
    private Long roomId;
    private String title;
    private String type; // INDIVIDUAL, GROUP

    public static ChatRoomResponse from(ChatRoom room) {
        return ChatRoomResponse.builder()
                .roomId(room.getId())
                .title(room.getTitle())
                .type(room.getType().name())
                .build();
    }
}