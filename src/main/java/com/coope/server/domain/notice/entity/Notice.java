package com.coope.server.domain.notice.entity;

import com.coope.server.domain.common.entity.BaseTimeEntity;
import com.coope.server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

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
    private User user; // 작성자 관리자

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;


    private String imageUrl;

    private int views = 0;

    @Builder
    public Notice(String title, String content, String imageUrl, Integer views, User user) {
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.views = (views != null) ? views : 0;
        this.user = user;
    }

}