package com.coope.server.domain.aichat.controller;

import com.coope.server.domain.aichat.dto.AIChatMessage;
import com.coope.server.domain.aichat.dto.AIChatStreamRequest;
import com.coope.server.domain.aichat.service.AIChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/ai-chat")
@RequiredArgsConstructor
public class AIChatController {

    private final AIChatService aiChatService;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamAIChat(@Valid @RequestBody AIChatStreamRequest request) {

        String message = request.getMessage();

        List<AIChatMessage> history = request.getPreviousMessages() != null
                ? request.getPreviousMessages()
                : List.of();

        return aiChatService.getAIStreamResponse(message, history);
    }
}