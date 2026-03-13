package com.coope.server.aichat.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class AIChatStreamRequest {
    @NotBlank(message = "메시지는 비어있을 수 없습니다.")
    private String message;

    private List<com.coope.server.aichat.presentation.dto.AIChatMessage> previousMessages;
}
