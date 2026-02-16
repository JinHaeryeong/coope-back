package com.coope.server.domain.chat.dto;

import com.coope.server.domain.chat.entity.Message;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MessageResponse {
    private final Long id;
    private final Long roomId;
    private final Long senderId;
    private final String senderNickname;
    private final String senderProfile;
    private final String content;
    private final String fileUrl;
    private final String fileName;
    private final String fileFormat;
    private final LocalDateTime createdAt;

    @Builder
    private MessageResponse(Long id, Long roomId, Long senderId, String senderNickname, String senderProfile, String content, String fileUrl, String fileName, String fileFormat, LocalDateTime createdAt) {
        this.id = id;
        this.roomId = roomId;
        this.senderId = senderId;
        this.senderNickname = senderNickname;
        this.senderProfile = senderProfile;
        this.content = content;
        this.fileUrl = fileUrl;
        this.fileName = fileName;
        this.fileFormat = fileFormat;
        this.createdAt = createdAt;
    }

    public static MessageResponse from(Message message) {
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