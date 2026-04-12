package com.coope.server.chat.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequest {

    private Long roomId;

    private Long senderId;

    @NotBlank(message = "메시지 내용을 입력해주세요.")
    private String content;

    private String fileUrl;
    private String fileName;
    private String fileFormat;
}