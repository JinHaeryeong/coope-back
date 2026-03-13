package com.coope.server.domain.notice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;


@Getter
@AllArgsConstructor
public class NoticeWriteRequest {

    @NotBlank(message = "공지사항 제목은 필수입니다.")
    private final String title;

    @NotBlank(message = "공지사항 내용은 필수입니다.")
    private final String content;

    private final MultipartFile file;
    private final Boolean deleteImage;
}