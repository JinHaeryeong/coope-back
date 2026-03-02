package com.coope.server.domain.inquiry.entity;

import com.coope.server.domain.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryFile extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id")
    private Inquiry inquiry;

    private String fileUrl;
    private String originalName;

    @Builder
    public InquiryFile(Inquiry inquiry, String fileUrl, String originalName) {
        this.inquiry = inquiry;
        this.fileUrl = fileUrl;
        this.originalName = originalName;
    }

    public void initInquiry(Inquiry inquiry) {
        this.inquiry = inquiry;
    }
}