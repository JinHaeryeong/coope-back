package com.coope.server.inquiry.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InquiryAnswerRequest {
    @NotBlank(message = "답변 내용은 필수입니다.")
    private String content;
}