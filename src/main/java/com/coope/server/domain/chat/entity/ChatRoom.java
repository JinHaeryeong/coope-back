package com.coope.server.domain.chat.entity;

import com.coope.server.domain.common.entity.BaseTimeEntity;
import com.coope.server.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public static ChatRoom createIndividual(String title, User me, User friend) {
        ChatRoom room = ChatRoom.builder()
                .title(title)
                .type(RoomType.INDIVIDUAL)
                .build();
        room.addParticipant(me);
        room.addParticipant(friend);
        return room;
    }

    public void updateLastMessage(Message message) {
        String summary = message.getSummaryContent();

        this.participants.forEach(participant ->
                participant.updateLastMessage(message.getCreatedAt(), summary)
        );
    }

    public void updateTitleByParticipants(List<User> participants) {
        if (this.type != RoomType.GROUP) {
            return;
        }
        if (participants == null || participants.isEmpty()) {
            return;
        }

        String calculatedTitle = participants.stream()
                .map(User::getNickname)
                .limit(3)
                .collect(Collectors.joining(", "));

        if (participants.size() > 3) {
            calculatedTitle += " 외 " + (participants.size() - 3) + "명";
        } else {
            calculatedTitle += "의 대화";
        }

        this.title = calculatedTitle;
    }

    public static ChatRoom createGroup(String roomName, List<User> users) {
        ChatRoom room = ChatRoom.builder()
                .title(roomName)
                .type(RoomType.GROUP)
                .build();

        users.forEach(room::addParticipant);

        if (roomName == null || roomName.trim().isEmpty()) {
            room.updateTitleByParticipants(users);
        }

        return room;
    }

    public void addParticipant(User user) {
        this.participants.add(ChatParticipant.of(this, user));
    }
}