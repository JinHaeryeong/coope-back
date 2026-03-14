package com.coope.server.notice.domain;

import com.coope.server.comment.domain.Comment;
import com.coope.server.shared.domain.BaseTimeEntity;
import com.coope.server.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "notices")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = "user")
public class Notice extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private String imageUrl;

    private int views = 0;

    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Builder
    public Notice(String title, String content, String imageUrl, Integer views, User user) {
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.views = (views != null) ? views : 0;
        this.user = user;
    }

    public static Notice createNotice(String title, String content, String imageUrl, User user) {
        return Notice.builder()
                .title(title)
                .content(content)
                .imageUrl(imageUrl)
                .user(user)
                .views(0)
                .build();
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void changeImage(String newImageUrl) {
        this.imageUrl = newImageUrl;
    }

    public void removeImage() {
        this.imageUrl = null;
    }
}
