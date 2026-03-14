package com.coope.server.inquiry.domain;

import com.coope.server.shared.domain.BaseTimeEntity;
import com.coope.server.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryAnswer extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "inquiry_id", unique = true)
    private Inquiry inquiry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private User admin;

    private String content;

    @Builder
    public InquiryAnswer(Inquiry inquiry, User admin, String content) {
        this.inquiry = inquiry;
        this.admin = admin;
        this.content = content;
    }

    public void initInquiry(Inquiry inquiry) {
        this.inquiry = inquiry;
    }

    public static InquiryAnswer createInquiryAnswer(Inquiry inquiry, User admin, String content) {
        return InquiryAnswer.builder()
                .inquiry(inquiry)
                .admin(admin)
                .content(content)
                .build();
    }
}