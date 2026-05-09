package com.coope.server.community.domain.post;

import com.coope.server.community.domain.comment.PostComment;
import com.coope.server.community.domain.post.enums.PostCategory;
import com.coope.server.community.domain.post.enums.TechStack;
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
 * 모집 카드 전용 필드(techStacks, currentMembers, targetMembers)는
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

    // [모집 카드 전용] 현재 참여 인원
    private Integer currentMembers;

    // [모집 카드 전용] 목표 참여 인원
    private Integer targetMembers;

    // 조회수
    @Column(nullable = false)
    private int viewCount = 0;

    @Column(nullable = false)
    private int commentCount = 0;

    @Column(nullable = false)
    private int likeCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // 댓글 목록 (orphanRemoval로 게시글 삭제 시 댓글 자동 삭제)
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostComment> comments = new ArrayList<>();

    // [모집 카드 전용] 기술 스택 목록 (별도 테이블)
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostTechStack> techStacks = new ArrayList<>();

    @Builder
    private Post(PostCategory category, String title, String content,
                 Integer currentMembers, Integer targetMembers, User author) {
        this.category = category;
        this.title = title;
        this.content = content;
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
                                              int currentMembers, int targetMembers,
                                              User author) {
        return Post.builder()
                .category(PostCategory.RECRUITMENT)
                .title(title)
                .content(content)
                .currentMembers(currentMembers)
                .targetMembers(targetMembers)
                .author(author)
                .build();
    }

    // 기술 스택 추가 (Post 저장 후 호출)
    public void addTechStack(TechStack techStack) {
        this.techStacks.add(PostTechStack.of(this, techStack));
    }

    // 기술 스택 전체 교체 (수정 시 사용)
    public void updateTechStacks(List<TechStack> newTechStacks) {
        this.techStacks.clear();
        if (newTechStacks != null) {
            newTechStacks.forEach(this::addTechStack);
        }
    }

    // 편의 메서드: TechStack enum 목록 반환
    public List<TechStack> getTechStackValues() {
        return techStacks.stream()
                .map(PostTechStack::getTechStack)
                .toList();
    }

    // 게시글 내용 수정
    public void update(String title, String content,
                       List<TechStack> techStacks, Integer currentMembers, Integer targetMembers) {
        this.title = title;
        this.content = content;
        this.currentMembers = currentMembers;
        this.targetMembers = targetMembers;
        updateTechStacks(techStacks);
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
