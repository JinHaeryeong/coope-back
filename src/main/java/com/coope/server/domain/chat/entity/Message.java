package com.coope.server.domain.chat.entity;

import com.coope.server.domain.chat.dto.MessageRequest;
import com.coope.server.domain.common.entity.BaseTimeEntity;
import com.coope.server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;


    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User user;

    private String fileUrl;
    private String fileName;
    private String fileFormat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'TALK'")
    private MessageType type = MessageType.TALK;

    @Builder
    public Message(ChatRoom chatRoom, User user, String content, String fileUrl, String fileName, String fileFormat, MessageType type) {
        this.chatRoom = chatRoom;
        this.user = user;
        this.content = content;
        this.fileUrl = fileUrl;
        this.fileName = fileName;
        this.fileFormat = fileFormat;
        this.type = (type != null) ? type : MessageType.TALK;
    }

    public Long getSenderId() {
        return user != null ? user.getId() : null;
    }

    public static Message createTalkMessage(ChatRoom chatRoom, User sender, MessageRequest request) {
        return Message.builder()
                .chatRoom(chatRoom)
                .user(sender)
                .content(request.getContent())
                .fileUrl(request.getFileUrl())
                .fileName(request.getFileName())
                .fileFormat(request.getFileFormat())
                .type(MessageType.TALK)
                .build();
    }

    public static Message createLeaveMessage(ChatRoom room, User user) {
        return Message.builder()
                .chatRoom(room)
                .user(user)
                .content(user.getNickname() + "님이 퇴장하셨습니다.")
                .type(MessageType.LEAVE)
                .build();
    }
}