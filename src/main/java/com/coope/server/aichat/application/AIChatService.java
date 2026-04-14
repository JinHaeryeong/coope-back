package com.coope.server.aichat.application;

import com.coope.server.aichat.presentation.dto.AIChatRequest;
import com.coope.server.aichat.presentation.dto.AIChatMessage;
import com.coope.server.shared.ai.AiLimit;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AIChatService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    @AiLimit(maxCount = 5)
    public Flux<String> getAIStreamResponse(String userPrompt, List<AIChatMessage> history) {
        AIChatRequest requestBody = AIChatRequest.of(userPrompt, history);

        return webClient.post()
                .uri("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .timeout(Duration.ofSeconds(30))
                .filter(data -> !data.equals("[DONE]"))
                .map(this::parseDeltaContent)
                .filter(content -> !content.isEmpty())
                .onErrorResume(e -> Flux.just(" 에러: AI 응답 중 오류가 발생했습니다"));
    }

    private String parseDeltaContent(String json) {
        try {
            if (json.startsWith("data: ")) {
                json = json.substring(6);
            }
            JsonNode root = objectMapper.readTree(json);
            return root.path("choices").get(0)
                    .path("delta").path("content").asText("");
        } catch (Exception e) {
            return "";
        }
    }
}
