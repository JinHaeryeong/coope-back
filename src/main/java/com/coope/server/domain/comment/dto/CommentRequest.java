package com.coope.server.domain.comment.dto;


import com.coope.server.domain.comment.entity.Comment;
import com.coope.server.domain.notice.entity.Notice;
import com.coope.server.domain.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
public class CommentRequest {

    @NotBlank(message = "댓글 내용을 입력해주세요.")
    private String content;

    private MultipartFile file;

    public Comment toEntity(Notice notice, User user, String savedImageUrl) {
        return Comment.builder()
                .content(this.content)
                .imageUrl(savedImageUrl)
                .notice(notice)
                .user(user)
                .build();
    }
}
