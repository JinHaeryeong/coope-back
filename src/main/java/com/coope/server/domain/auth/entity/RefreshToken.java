package com.coope.server.domain.auth.entity;

import com.coope.server.domain.common.entity.BaseTimeEntity;
import com.coope.server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY) // 유저 한 명당 토큰 하나
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 500)
    private String tokenValue;

    @Column(nullable = false)
    private java.time.LocalDateTime expiryDate;

    @Builder
    public RefreshToken(User user, String tokenValue, java.time.LocalDateTime expiryDate) {
        this.user = user;
        this.tokenValue = tokenValue;
        this.expiryDate = expiryDate;
    }

    // 토큰 갱신 로직
    public void updateToken(String newTokenValue, java.time.LocalDateTime newExpiryDate) {
        this.tokenValue = newTokenValue;
        this.expiryDate = newExpiryDate;
    }
}