package com.coope.server.domain.comment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@AllArgsConstructor // ModelAttribute가 생성자 바인딩을 할 수 있게 함
public class CommentRequest {

    @NotBlank(message = "댓글 내용을 입력해주세요.")
    private final String content;

    private final MultipartFile file;

    private final Boolean deleteImage;
}