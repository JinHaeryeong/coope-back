package com.coope.server.community.domain.post;

import com.coope.server.community.domain.post.enums.TechStack;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게시글-기술스택 연관 엔티티
 * community_post_tech_stacks 테이블과 매핑
 * Post(1) : PostTechStack(N) 관계
 */
@Entity
@Table(name = "community_post_tech_stacks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostTechStack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Enumerated(EnumType.STRING)
    @Column(name = "tech_stack", nullable = false, length = 30)
    private TechStack techStack;

    private PostTechStack(Post post, TechStack techStack) {
        this.post = post;
        this.techStack = techStack;
    }

    public static PostTechStack of(Post post, TechStack techStack) {
        return new PostTechStack(post, techStack);
    }
}
