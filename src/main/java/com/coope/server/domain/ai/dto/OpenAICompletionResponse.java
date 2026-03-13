package com.coope.server.domain.ai.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class OpenAICompletionResponse {
    private List<Choice> choices;

    @Getter
    @NoArgsConstructor
    public static class Choice {
        private Message message;
    }

    @Getter
    @NoArgsConstructor
    public static class Message {
        private String content;
    }

    public String getContent() {
        if (choices == null || choices.isEmpty()) return null;
        return choices.get(0).getMessage().getContent();
    }
}