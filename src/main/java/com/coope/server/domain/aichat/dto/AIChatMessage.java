package com.coope.server.domain.aichat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AIChatMessage {
    private String role;
    private String content;
}