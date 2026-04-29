package com.coope.server.community.domain.post;

import com.coope.server.community.domain.comment.PostComment;
import com.coope.server.community.domain.post.enums.PostCategory;
import com.coope.server.shared.domain.BaseTimeEntity;
import com.coope.server.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 커뮤니티 게시글 엔티티
 * 카테고리에 따라 일반 게시글과 모집 카드(Recruitment) 두 가지 형태로 활용
 * 모집 카드 전용 필드(techStack, currentMembers, targetMembers)는
 * category == RECRUITMENT 일 때만 유의미한 값을 가짐
 */
@Entity
@Table(name = "community_posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 게시글 카테고리 (필터링의 핵심 기준)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostCategory category;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // [모집 카드 전용] 사용 기술 스택 (예: "Spring Boot, React, PostgreSQL")
    @Column(length = 200)
    private String techStack;

    // [모집 카드 전용] 현재 참여 인원
    private Integer currentMembers;

    // [모집 카드 전용] 목표 참여 인원
    private Integer targetMembers;

    // 조회수
    @Column(nullable = false)
    private int viewCount = 0;

    // 댓글수 (댓글 생성/삭제 시 JPQL 벌크 업데이트로 관리)
    @Column(nullable = false)
    private int commentCount = 0;

    // 좋아요수 (좋아요 생성/삭제 시 JPQL 벌크 업데이트로 관리)
    @Column(nullable = false)
    private int likeCount = 0;

    // 게시글 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // 댓글 목록 (orphanRemoval로 게시글 삭제 시 댓글 자동 삭제)
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostComment> comments = new ArrayList<>();

    @Builder
    private Post(PostCategory category, String title, String content,
                 String techStack, Integer currentMembers, Integer targetMembers,
                 User author) {
        this.category = category;
        this.title = title;
        this.content = content;
        this.techStack = techStack;
        this.currentMembers = currentMembers;
        this.targetMembers = targetMembers;
        this.author = author;
    }

    // 일반 게시글(SHOWCASE / QNA / GENERAL)
    public static Post createGeneralPost(PostCategory category, String title,
                                         String content, User author) {
        return Post.builder()
                .category(category)
                .title(title)
                .content(content)
                .author(author)
                .build();
    }

    // 모집 카드(RECRUITMENT) 생성
    public static Post createRecruitmentPost(String title, String content,
                                              String techStack,
                                              int currentMembers, int targetMembers,
                                              User author) {
        return Post.builder()
                .category(PostCategory.RECRUITMENT)
                .title(title)
                .content(content)
                .techStack(techStack)
                .currentMembers(currentMembers)
                .targetMembers(targetMembers)
                .author(author)
                .build();
    }

    // 게시글 내용 수정
    public void update(String title, String content,
                       String techStack, Integer currentMembers, Integer targetMembers) {
        this.title = title;
        this.content = content;
        this.techStack = techStack;
        this.currentMembers = currentMembers;
        this.targetMembers = targetMembers;
    }

    // 조회수 증가
    public void increaseViewCount() {
        this.viewCount++;
    }

    // 작성자 본인 여부 확인
    public boolean isAuthor(User user) {
        return this.author.getId().equals(user.getId());
    }

    // 모집 게시글 여부 확인
    public boolean isRecruitment() {
        return PostCategory.RECRUITMENT.equals(this.category);
    }
}
