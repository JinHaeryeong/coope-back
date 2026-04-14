package com.coope.server.community.domain.comment;

import com.coope.server.community.domain.post.Post;
import com.coope.server.shared.domain.BaseTimeEntity;
import com.coope.server.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "community_post_comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostComment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 비밀 댓글 여부
     * true  → 게시글 작성자·댓글 작성자만 열람 가능
     * false → 모든 사용자에게 공개
     */
    @Column(nullable = false)
    private boolean isPrivate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Builder
    private PostComment(String content, boolean isPrivate, Post post, User author) {
        this.content = content;
        this.isPrivate = isPrivate;
        this.post = post;
        this.author = author;
    }

    public static PostComment create(String content, boolean isPrivate, Post post, User author) {
        return PostComment.builder()
                .content(content)
                .isPrivate(isPrivate)
                .post(post)
                .author(author)
                .build();
    }

    public void update(String newContent) {
        this.content = newContent;
    }

    /**
     * 현재 사용자가 이 댓글을 열람할 수 있는지 확인
     *
     * @param viewer 열람 요청 사용자
     * @return 공개 댓글이거나, 게시글 작성자이거나, 댓글 작성자일 때 true
     */
    public boolean isReadableBy(User viewer) {
        if (!this.isPrivate) {
            return true;
        }
        // 비밀 댓글: 게시글 작성자 또는 댓글 작성자만 허용
        Long viewerId = viewer.getId();
        return viewerId.equals(this.post.getAuthor().getId())
                || viewerId.equals(this.author.getId());
    }

    public boolean isAuthor(User user) {
        return this.author.getId().equals(user.getId());
    }
}
