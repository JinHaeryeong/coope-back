package com.coope.server.domain.aichat.service;

import com.coope.server.domain.aichat.dto.AIChatMessage;
import com.coope.server.domain.aichat.dto.AIChatRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AIChatService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final ObjectMapper objectMapper;
    private final WebClient webClient = WebClient.builder().build();

    public Flux<String> getAIStreamResponse(String userPrompt, List<AIChatMessage> history) {

        AIChatMessage systemMsg = AIChatRequest.createSystemMessage();

        // 전체 메시지 조립 (System -> History -> Current User Prompt)
        List<AIChatMessage> allMessages = new ArrayList<>();
        allMessages.add(systemMsg);
        allMessages.addAll(history);
        allMessages.add(new AIChatMessage("user", userPrompt));

        // 요청 객체 생성
        AIChatRequest requestBody = AIChatRequest.createDefault(allMessages);

        // OpenAI 스트리밍 호출
        return webClient.post()
                .uri("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class) // 조각(chunk) 단위로 받음
                .filter(data -> !data.equals("[DONE]")) // 끝 신호 제외
                .map(this::parseDeltaContent) // 텍스트만 추출
                .filter(content -> !content.isEmpty())
                .onErrorResume(e -> Flux.just(" 에러: AI 응답 중 오류가 발생했습니다"));
    }


    private String parseDeltaContent(String json) {
        try {
            // OpenAI의 스트리밍 데이터는 "data: {" 로 시작함
            if (json.startsWith("data: ")) {
                json = json.substring(6);
            }
            JsonNode root = objectMapper.readTree(json);
            // choices[0].delta.content 경로의 텍스트 추출
            return root.path("choices").get(0)
                    .path("delta").path("content").asText("");
        } catch (Exception e) {
            return ""; // 파싱 실패 시 빈 문자열 반환
        }
    }
}