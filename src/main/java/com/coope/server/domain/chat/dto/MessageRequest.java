package com.coope.server.domain.chat.dto;

import com.coope.server.domain.chat.entity.ChatRoom;
import com.coope.server.domain.chat.entity.Message;
import com.coope.server.domain.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MessageRequest {

    private Long roomId;

    private Long senderId;

    @NotBlank(message = "메시지 내용을 입력해주세요.")
    private String content;

    private String fileUrl;
    private String fileName;
    private String fileFormat;

    public Message toEntity(ChatRoom chatRoom, User sender) {
        return Message.builder()
                .chatRoom(chatRoom)
                .user(sender)
                .content(this.content)
                .fileUrl(this.fileUrl)
                .fileName(this.fileName)
                .fileFormat(this.fileFormat)
                .build();
    }
}