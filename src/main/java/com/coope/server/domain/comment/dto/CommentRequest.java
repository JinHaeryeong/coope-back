package com.coope.server.domain.comment.dto;


import com.coope.server.domain.comment.entity.Comment;
import com.coope.server.domain.notice.entity.Notice;
import com.coope.server.domain.user.entity.User;
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

    public Comment toEntity(Notice notice, User user, String savedImageUrl) {
        return Comment.builder()
                .content(this.content)
                .imageUrl(savedImageUrl)
                .notice(notice)
                .user(user)
                .build();
    }
}