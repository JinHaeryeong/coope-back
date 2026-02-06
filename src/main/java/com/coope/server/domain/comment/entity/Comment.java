package com.coope.server.domain.comment.entity;

import com.coope.server.domain.common.entity.BaseTimeEntity;
import com.coope.server.domain.notice.entity.Notice;
import com.coope.server.domain.user.entity.User;
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

    public void update(String content) {
        if (content != null && !content.isBlank()) {
            this.content = content;
        }
    }

    // Comment.java 엔티티 내부
    public void updateImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}