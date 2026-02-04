package com.coope.server.domain.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass // 테이블로 생성되지 않고, 상속받는 자식 클래스에게 매핑 정보만 제공
@EntityListeners(AuditingEntityListener.class) // JPA Auditing 기능을 포함
public abstract class BaseTimeEntity {

    @CreatedDate // 엔티티가 생성되어 저장될 때 시간이 자동 저장
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate // 조회한 엔티티의 값을 변경할 때 시간이 자동 저장
    private LocalDateTime updatedAt;
}
