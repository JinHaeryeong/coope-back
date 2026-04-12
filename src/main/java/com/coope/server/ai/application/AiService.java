package com.coope.server.ai.application;

import com.coope.server.ai.infrastructure.OpenAICompletionResponse;
import com.coope.server.ai.application.dto.VoiceProcessResponse;
import com.coope.server.shared.ai.AiLimit;
import com.coope.server.shared.error.exception.BadRequestException;
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

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

        String rawTranscript = requestWhisperStt(webClient, file);

        return requestGptSummary(webClient, rawTranscript);
    }

    private String requestWhisperStt(WebClient webClient, MultipartFile file) {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "audio.webm";

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", file.getResource()).filename(filename);
        builder.part("model", "whisper-1");
        builder.part("language", "ko");
        builder.part("response_format", "text");

        String response = webClient.post()
                .uri("/audio/transcriptions")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header("Authorization", "Bearer " + apiKey)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .onStatus(HttpStatusCode::isError, res -> res.bodyToMono(String.class)
                        .flatMap(error -> Mono.error(new BadRequestException("OpenAI API 에러: " + error))))
                .bodyToMono(String.class)
                .block(Duration.ofSeconds(60));

        return Objects.requireNonNull(response, "STT 결과가 null입니다.");
    }

    private VoiceProcessResponse requestGptSummary(WebClient webClient, String rawTranscript) {
        Map<String, Object> requestBody = Map.of(
                "model", "gpt-3.5-turbo-0125",
                "messages", List.of(
                        Map.of("role", "system", "content", getSystemPrompt()),
                        Map.of("role", "user", "content", rawTranscript)
                ),
                "response_format", Map.of("type", "json_object"),
                "temperature", 0.3
        );

        OpenAICompletionResponse response = webClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(OpenAICompletionResponse.class)
                .block(Duration.ofSeconds(60));

        return parseGptResponse(response, rawTranscript);
    }

    private VoiceProcessResponse parseGptResponse(OpenAICompletionResponse response, String rawTranscript) {
        String content = (response != null) ? response.getContent() : null;

        if (content == null) {
            return new VoiceProcessResponse(rawTranscript, "AI 요약 서비스 응답 오류");
        }

        try {
            return objectMapper.readValue(content, VoiceProcessResponse.class);
        } catch (Exception e) {
            log.error("[AI] 응답 파싱 실패: {}", e.getMessage());
            return new VoiceProcessResponse(rawTranscript, "요약 파싱 실패");
        }
    }

    private String getSystemPrompt() {
        return """
            당신은 회의록 작성 전문가입니다.
            제공된 텍스트를 분석하여 다음 JSON 형식으로만 응답하세요:
            {
              "labeled_transcript": "문맥을 파악해 [화자 1]: 내용 형식으로 재구성한 전체 대화",
              "summary": "회의 내용 요약본"
            }
            반드시 한국어를 사용하세요.
            """;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BadRequestException("파일이 비어있습니다.");
        if (file.getSize() > MAX_FILE_SIZE) throw new BadRequestException("파일 크기 초과 (최대 25MB)");

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new BadRequestException("유효하지 않은 파일 형식입니다.");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!SUPPORTED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("지원하지 않는 확장자입니다: " + extension);
        }
    }
}
