package com.coope.server.domain.user.dto;

import com.coope.server.domain.user.entity.User;
import com.coope.server.domain.user.enums.Provider;
import com.coope.server.domain.user.enums.Role;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.*;

@Getter
public class SignupRequest {

    @NotBlank @Email
    private final String email;

    @NotBlank @Size(min = 8)
    private final String password;

    @NotBlank
    private final String name;

    @NotBlank
    private final String nickname;

    private final MultipartFile userIcon;

    public SignupRequest(String email, String password, String name, String nickname, MultipartFile userIcon) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.userIcon = userIcon;
    }

    // 서비스에서 호출할 엔티티 변환 메서드
    public User toEntity(String encodedPassword, String profileImageUrl) {
        return User.builder()
                .email(this.email)
                .password(encodedPassword)
                .name(this.name)
                .nickname(this.nickname)
                .userIcon(profileImageUrl) // 저장된 경로 (null일 수 있음)
                .provider(Provider.LOCAL)
                .role(Role.ROLE_USER)
                .build();
    }
}