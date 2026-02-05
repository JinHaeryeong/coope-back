package com.coope.server.domain.notice.dto;

import com.coope.server.domain.notice.entity.Notice;
import com.coope.server.domain.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;


@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 빈 생성자 막기
public class NoticeWriteRequest {

    @NotBlank(message = "공지사항 제목은 필수입니다.")
    private String title;

    @NotBlank(message = "공지사항 내용은 필수입니다.")
    private String content;

    private MultipartFile file;

    public NoticeWriteRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }

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