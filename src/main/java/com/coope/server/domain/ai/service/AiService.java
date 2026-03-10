package com.coope.server.domain.ai.service;

import com.coope.server.domain.ai.dto.VoiceProcessResponse;
import com.coope.server.global.annotation.AiLimit;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AiService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    private static final long MAX_FILE_SIZE = 25 * 1024 * 1024;
    private static final List<String> SUPPORTED_EXTENSIONS = List.of("webm", "mp3", "wav", "m4a");

    @AiLimit(type = "STT", maxCount = 2)
    public VoiceProcessResponse processVoice(MultipartFile file) {
        validateFile(file);
        WebClient webClient = webClientBuilder.baseUrl("https://api.openai.com/v1").build();

        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "audio.webm";

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", file.getResource()).filename(filename);
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
                                Mono.error(new com.coope.server.global.error.exception.BadRequestException("OpenAI API 에러: " + errorBody)))
                )
                .bodyToMono(String.class)
                .block(java.time.Duration.ofSeconds(60));

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

        Map<String, Object> result = webClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
                .block(java.time.Duration.ofSeconds(60));

        if (result == null || !result.containsKey("choices")) {
            return new VoiceProcessResponse(rawTranscript, "AI 요약 서비스 응답 오류");
        }

        Object choicesObj = result.get("choices");
        if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) {
            return new VoiceProcessResponse(rawTranscript, "AI 요약 생성 실패 (choices 누락)");
        }
        Object firstChoiceObj = choices.get(0); // 인텔리제이에선 getFirst 쓰라고하지만 java 21보다 낮을땐 안되니까 get(0)으로
        if (!(firstChoiceObj instanceof Map<?, ?> choice)) {
            return new VoiceProcessResponse(rawTranscript, "AI 응답 형식이 올바르지 않습니다.");
        }
        Object messageObj = choice.get("message");
        if (!(messageObj instanceof Map<?, ?> message)) {
            return new VoiceProcessResponse(rawTranscript, "AI 메시지 데이터가 누락되었습니다.");
        }
        Object contentObj = message.get("content");
        if (!(contentObj instanceof String content)) {
            return new VoiceProcessResponse(rawTranscript, "AI 요약 내용이 비어있습니다.");
        }

        try {
            Map<String, String> resultMap = objectMapper.readValue(content, new com.fasterxml.jackson.core.type.TypeReference<>() {});
            return new VoiceProcessResponse(resultMap.get("labeled_transcript"), resultMap.get("summary"));
        } catch (Exception e) {
            log.error("AI 응답 파싱 중 오류 발생: {}", e.getMessage(), e);
            return new VoiceProcessResponse(rawTranscript, "요약 파싱 실패");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("파일이 비어있습니다.");
        if (file.getSize() > MAX_FILE_SIZE) throw new IllegalArgumentException("파일 크기 초과 (최대 25MB)");
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("유효하지 않은 파일 형식입니다.");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();

        if (!SUPPORTED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("지원하지 않는 확장자입니다: " + extension +
                    " (지원 목록: " + String.join(", ", SUPPORTED_EXTENSIONS) + ")");
        }
    }
}