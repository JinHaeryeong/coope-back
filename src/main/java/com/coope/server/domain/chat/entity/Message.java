package com.coope.server.domain.chat.entity;

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
    private MessageType type;

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
}