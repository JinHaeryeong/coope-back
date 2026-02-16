package com.coope.server.domain.chat.dto;

import com.coope.server.domain.chat.entity.ChatRoom;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ChatRoomResponse {
    private final Long roomId;
    private final String title;
    private final String type;

    @Builder
    private ChatRoomResponse(Long roomId, String title, String type) {
        this.roomId = roomId;
        this.title = title;
        this.type = type;
    }
    public static ChatRoomResponse from(ChatRoom room) {
        return ChatRoomResponse.builder()
                .roomId(room.getId())
                .title(room.getTitle())
                .type(room.getType().name())
                .build();
    }
}