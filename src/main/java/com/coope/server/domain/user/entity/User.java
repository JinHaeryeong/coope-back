package com.coope.server.domain.user.entity;

import com.coope.server.domain.common.entity.BaseTimeEntity;
import com.coope.server.domain.user.enums.Provider;
import com.coope.server.domain.user.enums.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String password;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false, unique = true, length = 20)
    private String nickname;

    private String userIcon;

    @Enumerated(EnumType.STRING) // 숫자 대신 문자열로 저장하여 가독성 확보
    private Provider provider; // LOCAL, GOOGLE 등

    private String providerId;

    @Enumerated(EnumType.STRING)
    private Role role; // ROLE_USER, ROLE_ADMIN

    // 생성자 대신 빌더 패턴 사용
    @Builder
    public User(String email, String password, String name, String nickname, String userIcon, Provider provider, String providerId, Role role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.userIcon = userIcon;
        this.provider = provider != null ? provider : Provider.LOCAL;
        this.providerId = providerId;
        this.role = role != null ? role : Role.ROLE_USER;
    }

    public void updateProfile(String name, String userIcon) {
        this.name = name;
        this.userIcon = userIcon;
    }

    public boolean matchesPassword(
            String rawPassword,
            PasswordEncoder passwordEncoder
    ) {
        return passwordEncoder.matches(rawPassword, this.password);
    }
}