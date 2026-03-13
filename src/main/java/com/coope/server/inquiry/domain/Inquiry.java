package com.coope.server.inquiry.domain;

import com.coope.server.domain.common.entity.BaseTimeEntity;
import com.coope.server.inquiry.domain.enums.InquiryCategory;
import com.coope.server.inquiry.domain.enums.InquiryStatus;
import com.coope.server.user.domain.User;
import com.coope.server.user.domain.enums.Role;
import com.coope.server.global.error.exception.AccessDeniedException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SoftDelete;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@SoftDelete(columnName = "deleted")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "inquiry")
public class Inquiry extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private InquiryCategory category;

    private String environment;

    @Enumerated(EnumType.STRING)
    private InquiryStatus status = InquiryStatus.PENDING;

    @OneToOne(mappedBy = "inquiry", cascade = CascadeType.ALL, orphanRemoval = true)
    private com.coope.server.inquiry.domain.InquiryAnswer answer;

    @OneToMany(mappedBy = "inquiry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<com.coope.server.inquiry.domain.InquiryFile> files = new ArrayList<>();

    private LocalDateTime deletedAt;

    @Builder
    public Inquiry(User user, String title, String content, InquiryCategory category, String environment) {
        this.user = user;
        this.title = title;
        this.content = content;
        this.category = category;
        this.environment = environment;
    }

    public static Inquiry createInquiry(User user, String title, String content,
                                        InquiryCategory category, String environment) {
        return Inquiry.builder()
                .user(user)
                .title(title)
                .content(content)
                .category(category)
                .environment(environment)
                .build();
    }

    public void reply(com.coope.server.inquiry.domain.InquiryAnswer answer) {
        if (answer == null) return;
        this.answer = answer;
        this.status = InquiryStatus.ANSWERED;
        answer.initInquiry(this);
    }

    public static Inquiry createInquiry(User user, String title, String content,
                                        InquiryCategory category, String environment,
                                        List<String> fileUrls) { // 파일 리스트 추가
        Inquiry inquiry = Inquiry.builder()
                .user(user)
                .title(title)
                .content(content)
                .category(category)
                .environment(environment)
                .build();

        if (fileUrls != null && !fileUrls.isEmpty()) {
            inquiry.addFiles(fileUrls);
        }

        return inquiry;
    }

    public void addFiles(List<String> fileUrls) {
        if (fileUrls == null || fileUrls.isEmpty()) return;

        fileUrls.forEach(url -> {
            com.coope.server.inquiry.domain.InquiryFile inquiryFile = com.coope.server.inquiry.domain.InquiryFile.builder()
                    .inquiry(this)
                    .fileUrl(url)
                    .build();
            this.files.add(inquiryFile);
        });
    }

    public void validateAccess(Long accessorId, Role userRole) {
        if (userRole == Role.ROLE_ADMIN) return;
        if (!this.user.getId().equals(accessorId)) {
            throw new AccessDeniedException("해당 문의사항에 접근할 권한이 없습니다.");
        }
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    @PreRemove
    public void preRemove() {
        if (this.deletedAt == null) {
            this.softDelete();
        }
    }

    public boolean isEditable() {
        return this.status == InquiryStatus.PENDING;
    }
}