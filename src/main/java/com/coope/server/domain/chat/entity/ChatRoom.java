package com.coope.server.domain.chat.entity;

import com.coope.server.domain.common.entity.BaseTimeEntity;
import com.coope.server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages = new ArrayList<>();

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatParticipant> participants = new ArrayList<>();

    @Builder
    public ChatRoom(String title, RoomType type) {
        this.title = title;
        this.type = type != null ? type : RoomType.INDIVIDUAL;
    }

    // 1:1 방 생성 편의 메서드
    public static ChatRoom createIndividual(String title, User me, User friend) {
        ChatRoom room = ChatRoom.builder()
                .title(title)
                .type(RoomType.INDIVIDUAL)
                .build();
        room.addParticipant(me);
        room.addParticipant(friend);
        return room;
    }

    // 그룹 방 생성 편의 메서드
    public static ChatRoom createGroup(String title, List<User> users) {
        ChatRoom room = ChatRoom.builder()
                .title(title)
                .type(RoomType.GROUP)
                .build();
        users.forEach(room::addParticipant);
        return room;
    }

    public void addParticipant(User user) {
        this.participants.add(ChatParticipant.of(this, user));
    }
}