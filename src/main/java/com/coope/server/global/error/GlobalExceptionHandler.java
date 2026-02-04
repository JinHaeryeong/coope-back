package com.coope.server.global.error;

import com.coope.server.global.error.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice // 모든 컨트롤러에서 발생하는 예외를 여기서 가로챔
public class GlobalExceptionHandler {

    // 이미 존재하는 이메일/닉네임 등 비즈니스 예외 (400 Bad Request)
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponse> handleBadRequestException(Exception e) {
        log.warn("비즈니스 로직 에러: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .success(false)
                        .message(e.getMessage())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException e) {
        // 유효성 검사 에러 메시지 중 첫 번째를 가져옴
        String errorMessage = e.getBindingResult().getAllErrors().getFirst().getDefaultMessage();

        log.warn("유효성 검사 실패: {}", errorMessage);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .success(false)
                        .message(errorMessage)
                        .build());
    }

    // 파일 업로드 실패나 DB 장애 등 서버 내부 에러 (500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleInternalServerError(Exception e) {
        log.error("서버 내부 에러 발생: ", e); // 500 에러는 stack trace를 남기기
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .success(false)
                        .message("서버 내부에 문제가 발생했습니다.")
                        .build());
    }
}