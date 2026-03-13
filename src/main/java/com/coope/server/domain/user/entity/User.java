package com.coope.server.domain.user.entity;

import com.coope.server.domain.auth.oauth.OAuth2UserInfo;
import com.coope.server.domain.common.entity.BaseTimeEntity;
import com.coope.server.domain.user.dto.SignupRequest;
import com.coope.server.domain.user.enums.Provider;
import com.coope.server.domain.user.enums.Role;
import com.coope.server.global.error.exception.AuthenticationException;
import com.coope.server.global.error.exception.BadRequestException;
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

    public static User createSocialUser(OAuth2UserInfo userInfo) {
        return User.builder()
                .email(userInfo.getEmail())
                .name(userInfo.getName())
                .nickname(userInfo.getName())
                .userIcon(userInfo.getPicture())
                .role(Role.ROLE_USER)
                .provider(userInfo.getProvider())
                .providerId(userInfo.getProviderId())
                .build();
    }

    public void changeNickname(String newNickname) {
        if (!StringUtils.hasText(newNickname)) {
            throw new IllegalArgumentException("닉네임은 필수입니다.");
        }
        if (this.nickname.equals(newNickname)) {
            return;
        }
        this.nickname = newNickname;
    }

    public void updateProfileImage(String newUserIcon) {
        this.userIcon = newUserIcon;
    }

    public void changePassword(String newEncodedPassword, String currentRawPassword, PasswordEncoder passwordEncoder) {
        if (isLocalUser()) { // 로컬 유저일 때만 체크
            if (!StringUtils.hasText(currentRawPassword)) {
                throw new BadRequestException("현재 비밀번호 입력은 필수입니다.");
            }
            authenticate(currentRawPassword, passwordEncoder);
        }
        this.password = newEncodedPassword;
    }

    public boolean matchesPassword(String rawPassword, PasswordEncoder passwordEncoder) {
        if (!StringUtils.hasText(rawPassword) || !StringUtils.hasText(this.password)) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, this.password);
    }

    public void authenticate(String rawPassword, PasswordEncoder passwordEncoder) {
        if (!matchesPassword(rawPassword, passwordEncoder)) {
            throw new AuthenticationException("비밀번호가 일치하지 않습니다.");
        }
    }
    public boolean isLocalUser() {
        return Provider.LOCAL.equals(this.provider);
    }

    public boolean isSameNickname(String nickname) {
        return this.nickname.equals(nickname);
    }
}