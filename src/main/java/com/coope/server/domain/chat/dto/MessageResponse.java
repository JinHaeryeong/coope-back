package com.coope.server.domain.chat.dto;

import com.coope.server.domain.chat.entity.Message;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MessageResponse {
    private Long id;
    private Long roomId;
    private Long senderId;
    private String senderNickname;
    private String senderProfile;
    private String content;
    private String fileUrl;
    private String fileName;
    private String fileFormat;
    private LocalDateTime createdAt;

    public static MessageResponse of(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .roomId(message.getChatRoom().getId())
                .senderId(message.getUser().getId())
                .senderNickname(message.getUser().getNickname())
                .senderProfile(message.getUser().getUserIcon())
                .content(message.getContent())
                .fileUrl(message.getFileUrl())
                .fileName(message.getFileName())
                .fileFormat(message.getFileFormat())
                .createdAt(message.getCreatedAt())
                .build();
    }
}