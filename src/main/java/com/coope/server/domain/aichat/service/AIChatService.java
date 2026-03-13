package com.coope.server.domain.aichat.service;

import com.coope.server.domain.aichat.dto.AIChatMessage;
import com.coope.server.domain.aichat.dto.AIChatRequest;
import com.coope.server.global.annotation.AiLimit;
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
                .bodyToFlux(String.class) // 조각(chunk) 단위로 받음
                .timeout(Duration.ofSeconds(30))
                .filter(data -> !data.equals("[DONE]")) // 끝 신호 제외
                .map(this::parseDeltaContent) // 텍스트만 추출
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
            return ""; // 파싱 실패 시 빈 문자열 반환
        }
    }
}