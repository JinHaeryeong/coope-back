package com.coope.server.domain.user.entity;

import com.coope.server.domain.common.entity.BaseTimeEntity;
import com.coope.server.domain.user.dto.SignupRequest;
import com.coope.server.domain.user.enums.Provider;
import com.coope.server.domain.user.enums.Role;
import com.coope.server.global.error.exception.AuthenticationException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

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

    @Enumerated(EnumType.STRING)
    private Provider provider;

    private String providerId;

    @Enumerated(EnumType.STRING)
    private Role role;

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

    public static User createLocalUser(SignupRequest request, String encodedPassword, String profileImageUrl) {
        return User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .name(request.getName())
                .nickname(request.getNickname())
                .userIcon(profileImageUrl)
                .provider(Provider.LOCAL)
                .role(Role.ROLE_USER)
                .build();
    }

    public void changeNickname(String newNickname) {
        if (!StringUtils.hasText(newNickname)) return;
        this.nickname = newNickname;
    }

    public void changeProfileImage(String newUserIcon) {
        this.userIcon = newUserIcon;
    }

    public void changePassword(String newEncodedPassword, String currentRawPassword, PasswordEncoder passwordEncoder) {
        // 소셜 가입이 아닌 로컬 가입자의 경우 비밀번호 검증 필요
        if (StringUtils.hasText(this.password)) {
            if (!matchesPassword(currentRawPassword, passwordEncoder)) {
                throw new AuthenticationException("현재 비밀번호가 일치하지 않아 수정을 완료할 수 없습니다.");
            }
        }
        this.password = newEncodedPassword;
    }

    public boolean matchesPassword(String rawPassword, PasswordEncoder passwordEncoder) {
        if (!StringUtils.hasText(rawPassword) || !StringUtils.hasText(this.password)) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, this.password);
    }
}