package com.coope.server.domain.notice.dto;

import com.coope.server.domain.notice.entity.Notice;
import com.coope.server.domain.user.entity.User;
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


    public Notice toEntity(User user, String savedImageUrl) {
        return Notice.builder()
                .title(this.title)
                .content(this.content)
                .imageUrl(savedImageUrl)
                .user(user)   // 작성자 정보 매핑
                .views(0)
                .build();
    }
}