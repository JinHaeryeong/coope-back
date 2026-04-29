package com.coope.server.community.domain.like;

import com.coope.server.community.domain.post.Post;
import com.coope.server.shared.domain.BaseTimeEntity;
import com.coope.server.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "post_likes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_post_likes_user_post",
                columnNames = {"user_id", "post_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLike extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Builder
    private PostLike(User user, Post post) {
        this.user = user;
        this.post = post;
    }

    public static PostLike of(User user, Post post) {
        return PostLike.builder()
                .user(user)
                .post(post)
                .build();
    }
}
