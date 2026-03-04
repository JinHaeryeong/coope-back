package com.coope.server.domain.inquiry.entity;

import com.coope.server.domain.common.entity.BaseTimeEntity;
import com.coope.server.domain.inquiry.enums.InquiryCategory;
import com.coope.server.domain.inquiry.enums.InquiryStatus;
import com.coope.server.domain.user.entity.User;
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

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private InquiryAnswer answer;

    @OneToMany(mappedBy = "inquiry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InquiryFile> files = new ArrayList<>();

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

    public void addFile(String fileUrl, String originalName) {
        if (fileUrl != null && !fileUrl.isBlank()) {
            InquiryFile inquiryFile = InquiryFile.builder()
                    .inquiry(this)
                    .fileUrl(fileUrl)
                    .originalName(originalName)
                    .build();
            this.files.add(inquiryFile);
        }
    }

    public void setAnswer(InquiryAnswer answer) {
        this.answer = answer;
        this.status = InquiryStatus.ANSWERED;

        if (answer != null) {
            answer.initInquiry(this);
        }
    }

    public boolean isEditable() {
        return this.status == InquiryStatus.PENDING;
    }


    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}