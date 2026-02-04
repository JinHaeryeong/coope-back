package com.coope.server.global.error;

import com.coope.server.domain.user.dto.SignupResponse;
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
    @ExceptionHandler({IllegalArgumentException.class, RuntimeException.class})
    public ResponseEntity<SignupResponse> handleBadRequestException(Exception e) {
        log.warn("비즈니스 로직 에러: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(SignupResponse.builder()
                        .success(false)
                        .message(e.getMessage())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<SignupResponse> handleValidationExceptions(MethodArgumentNotValidException e) {
        // 유효성 검사 에러 메시지 중 첫 번째를 가져옴
        String errorMessage = e.getBindingResult().getAllErrors().getFirst().getDefaultMessage();

        log.warn("유효성 검사 실패: {}", errorMessage);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(SignupResponse.builder()
                        .success(false)
                        .message(errorMessage)
                        .build());
    }

    // 파일 업로드 실패나 DB 장애 등 서버 내부 에러 (500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<SignupResponse> handleAllException(Exception e) {
        log.error("예상치 못한 서버 에러 발생!", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(SignupResponse.builder()
                        .success(false)
                        .message("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.")
                        .build());
    }
}