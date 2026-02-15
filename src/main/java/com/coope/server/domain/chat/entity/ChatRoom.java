package com.coope.server.domain.chat.entity;

import com.coope.server.domain.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType type; // INDIVIDUAL(1:1), GROUP(단체)

    @Builder
    public ChatRoom(String title, RoomType type) {
        this.title = title;
        this.type = type != null ? type : RoomType.INDIVIDUAL;
    }

    // 1:1 방 생성 편의 메서드
    public static ChatRoom createIndividual(String title) {
        return ChatRoom.builder()
                .title(title)
                .type(RoomType.INDIVIDUAL)
                .build();
    }

    // 그룹 방 생성 편의 메서드
    public static ChatRoom createGroup(String title) {
        return ChatRoom.builder()
                .title(title)
                .type(RoomType.GROUP)
                .build();
    }
}