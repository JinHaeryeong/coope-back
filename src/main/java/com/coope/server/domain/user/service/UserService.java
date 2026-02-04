package com.coope.server.domain.user.service;

import com.coope.server.domain.auth.dto.LoginRequest;
import com.coope.server.domain.user.dto.SignupRequest;
import com.coope.server.domain.user.entity.User;
import com.coope.server.domain.user.repository.UserRepository;
import com.coope.server.global.infra.LocalFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LocalFileService localFileService;

    @Transactional // 쓰기 작업이므로 별도의 트랜잭션 적용
    public Long signup(SignupRequest request) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 닉네임 중복 확인
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new RuntimeException("이미 존재하는 닉네임입니다.");
        }

        String userIconUrl = localFileService.upload(request.getUserIcon(), "profiles");

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // Entity 변환 및 저장
        User user = request.toEntity(encodedPassword, userIconUrl);
        return userRepository.save(user).getId();
    }

    // 비밀번호만 확인하고 유저 객체를 돌려주는 메서드
    public User validateUser(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return user;
    }
}
