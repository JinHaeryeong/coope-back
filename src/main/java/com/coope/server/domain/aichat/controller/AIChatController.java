package com.coope.server.domain.aichat.controller;

import com.coope.server.domain.aichat.dto.AIChatRequest;
import com.coope.server.domain.aichat.service.AIChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai-chat")
@RequiredArgsConstructor
public class AIChatController {

    private final AIChatService aiChatService;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE) // SSE 스트리밍 설정
    public Flux<String> streamAIChat(@RequestBody Map<String, Object> body) {
        String message = (String) body.get("message");

        List<Map<String, String>> prevList = (List<Map<String, String>>) body.get("previousMessages");

        List<AIChatRequest.Message> history = prevList.stream()
                .map(m -> new AIChatRequest.Message(m.get("role"), m.get("content")))
                .collect(Collectors.toList());

        // 서비스 호출하여 스트림 반환
        return aiChatService.getAIStreamResponse(message, history);
    }
}