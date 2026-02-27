package com.coope.server.domain.chat.dto;

import com.coope.server.domain.chat.entity.Message;
import com.coope.server.domain.chat.entity.MessageType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class MessageResponse {
    private Long id;
    private Long roomId;
    private Long senderId;
    private String senderNickname;
    private String senderProfile;
    private String content;
    private MessageType type;
    private String fileUrl;
    private String fileName;
    private String fileFormat;
    private LocalDateTime createdAt;

    @Builder
    private MessageResponse(Long id, Long roomId, Long senderId, String senderNickname, String senderProfile, String content, MessageType type, String fileUrl, String fileName, String fileFormat, LocalDateTime createdAt) {
        this.id = id;
        this.roomId = roomId;
        this.senderId = senderId;
        this.senderNickname = senderNickname;
        this.senderProfile = senderProfile;
        this.content = content;
        this.type = type;
        this.fileUrl = fileUrl;
        this.fileName = fileName;
        this.fileFormat = fileFormat;
        this.createdAt = createdAt;
    }

    public static MessageResponse from(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .roomId(message.getChatRoom().getId())
                .senderId(message.getSenderId())
                .senderNickname(message.getUser() != null ? message.getUser().getNickname() : "시스템")
                .senderProfile(message.getUser() != null ? message.getUser().getUserIcon() : null)
                .content(message.getContent())
                .type(message.getType())
                .fileUrl(message.getFileUrl())
                .fileName(message.getFileName())
                .fileFormat(message.getFileFormat())
                .createdAt(message.getCreatedAt())
                .build();
    }
}