package com.coope.server.domain.ai.service;

import com.coope.server.domain.ai.dto.VoiceProcessResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final WebClient.Builder webClientBuilder;

    private final ObjectMapper objectMapper;

    public VoiceProcessResponse processVoice(MultipartFile file) {
        WebClient webClient = webClientBuilder.baseUrl("https://api.openai.com/v1").build();

        // STT (Whisper API) 호출
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", file.getResource()).filename("audio.webm");
        builder.part("model", "whisper-1");
        builder.part("language", "ko");
        builder.part("response_format", "text");

        String rawTranscript = webClient.post()
                .uri("/audio/transcriptions")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header("Authorization", "Bearer " + apiKey)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class).flatMap(errorBody ->
                                Mono.error(new RuntimeException("OpenAI API 에러: " + errorBody)))
                )
                .bodyToMono(String.class)
                .block();

        java.util.Objects.requireNonNull(rawTranscript, "STT 결과가 null입니다.");

        String systemPrompt = """
            당신은 회의록 작성 전문가입니다.
            제공된 텍스트를 분석하여 다음 JSON 형식으로만 응답하세요:
            {
              "labeled_transcript": "문맥을 파악해 [화자 1]: 내용 형식으로 재구성한 전체 대화",
              "summary": "회의 내용 요약본"
            }
            반드시 한국어를 사용하세요.
            """;

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-3.5-turbo-0125",
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", rawTranscript)
                ),
                "response_format", Map.of("type", "json_object"),
                "temperature", 0.3
        );

        Map result = webClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (result == null || !result.containsKey("choices")) {
            return new VoiceProcessResponse(rawTranscript, "AI 요약 서비스 응답 오류");
        }
        // GPT 응답 파싱
        List<?> choices = (List<?>) result.get("choices");
        if (choices == null || choices.isEmpty()) {
            return new VoiceProcessResponse(rawTranscript, "AI 요약 생성 실패");
        }
        Map<?, ?> choice = (Map<?, ?>) choices.getFirst();
        Map<?, ?> message = (Map<?, ?>) choice.get("message");
        String content = (String) message.get("content");

        try {
            Map<String, String> resultMap = objectMapper.readValue(content, new com.fasterxml.jackson.core.type.TypeReference<>() {});

            // 프론트엔드 형식에 맞춰 리턴
            return new VoiceProcessResponse(
                    resultMap.get("labeled_transcript"), // 화자가 표시된 버전
                    resultMap.get("summary")              // 요약본
            );
        } catch (Exception e) {
            // 파싱 실패 시 원본이라도 리턴
            return new VoiceProcessResponse(rawTranscript, "요약 생성 실패");
        }
    }
}