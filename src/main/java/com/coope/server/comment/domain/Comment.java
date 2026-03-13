package com.coope.server.comment.domain;

import com.coope.server.domain.common.entity.BaseTimeEntity;
import com.coope.server.notice.domain.Notice;
import com.coope.server.user.domain.User;
import com.coope.server.user.domain.enums.Role;
import com.coope.server.global.error.exception.AccessDeniedException;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "comments", indexes = {
        @Index(name = "idx_notice_id_created_at", columnList = "notice_id, createdAt")
})
@ToString(exclude = "user")
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false)
    private Notice notice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public Comment(String content, String imageUrl, Notice notice, User user) {
        this.content = content;
        this.imageUrl = imageUrl;
        this.notice = notice;
        this.user = user;
    }

    public static Comment createComment(Notice notice, User user, String content, String imageUrl) {
        return Comment.builder()
                .notice(notice)
                .user(user)
                .content(content)
                .imageUrl(imageUrl)
                .build();
    }

    public void update(String content) {
        if (content != null && !content.isBlank()) {
            this.content = content;
        }
    }

    public void updateImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void validateOwner(User user) {
        if (!this.user.getId().equals(user.getId())) {
            throw new AccessDeniedException("댓글에 대한 권한이 없습니다.");
        }
    }

    public void validateDeletionAuthority(User user) {
        if (this.user.getId().equals(user.getId()) || user.getRole() == Role.ROLE_ADMIN) {
            return;
        }
        throw new AccessDeniedException("댓글 삭제 권한이 없습니다.");
    }

    public void validateNoticeOwnership(Long noticeId) {
        if (!this.notice.getId().equals(noticeId)) {
            throw new AccessDeniedException("해당 공지사항의 댓글이 아닙니다.");
        }
    }
}
