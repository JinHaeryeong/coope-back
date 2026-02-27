package com.coope.server.domain.user.service;

import com.coope.server.domain.auth.dto.LoginRequest;
import com.coope.server.domain.auth.service.EmailAuthService;
import com.coope.server.domain.friend.service.FriendService;
import com.coope.server.domain.user.dto.ProfileUpdateFullRequest;
import com.coope.server.domain.user.dto.SignupRequest;
import com.coope.server.domain.user.dto.UserResponse;
import com.coope.server.domain.user.dto.UserSearchResponse;
import com.coope.server.domain.user.entity.User;
import com.coope.server.domain.user.repository.UserRepository;
import com.coope.server.global.error.exception.AuthenticationException;
import com.coope.server.global.error.exception.BadRequestException;
import com.coope.server.global.error.exception.UserNotFoundException;
import com.coope.server.global.infra.file.FileService;
import com.coope.server.global.infra.file.ImageCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;
    private final FriendService friendService;
    private final EmailAuthService emailAuthService;

    @Transactional
    public Long signup(SignupRequest request) {

        if (!emailAuthService.isVerified(request.getEmail())) {
            throw new BadRequestException("이메일 인증이 완료되지 않았습니다.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        if (userRepository.existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }

        String userIconUrl = fileService.upload(request.getUserIcon(), ImageCategory.PROFILE);
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = request.toEntity(encodedPassword, userIconUrl);
        return userRepository.save(user).getId();
    }


    public void checkPassword(Long userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 계정입니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthenticationException("비밀번호가 일치하지 않습니다.");
        }
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 계정입니다.")
                );
    }

    public User validateUser(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 계정입니다.")
                );

        if (!user.matchesPassword(request.getPassword(), passwordEncoder)) {
            throw new AuthenticationException("비밀번호가 일치하지 않습니다.");
        }
        return user;
    }

    public UserResponse getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 계정입니다."));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateProfile(Long userId, ProfileUpdateFullRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 계정입니다."));

        if (StringUtils.hasText(request.getNickname())) {
            if (!request.getNickname().equals(user.getNickname()) && userRepository.existsByNickname(request.getNickname())) {
                throw new BadRequestException("이미 사용 중인 닉네임입니다.");
            }
            user.updateNickname(request.getNickname());
        }

        if (request.getProfileImage() != null && !request.getProfileImage().isEmpty()) {
            if (request.getProfileImage().getSize() > 10 * 1024 * 1024) {
                throw new BadRequestException("프로필 이미지는 10MB를 초과할 수 없습니다.");
            }

            if (StringUtils.hasText(user.getUserIcon()) && !user.getUserIcon().equals("default-icon.webp")) {
                try {
                    fileService.deleteFile(user.getUserIcon(), ImageCategory.PROFILE);
                } catch (Exception e) {
                    log.error("기존 프로필 이미지 삭제 실패: {}", user.getUserIcon());
                }
            }

            String url = fileService.upload(request.getProfileImage(), ImageCategory.PROFILE);
            user.updateProfileImage(url);
        }

        if (StringUtils.hasText(request.getNewPassword())) {
            if (!user.matchesPassword(request.getCurrentPassword(), passwordEncoder)) {
                throw new AuthenticationException("현재 비밀번호가 일치하지 않아 수정을 완료할 수 없습니다.");
            }
            user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        }

        return UserResponse.from(user);
    }


    public UserSearchResponse searchUserByNickname(Long currentUserId, String nickname) {
        User targetUser = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new UserNotFoundException("유저를 찾을 수 없습니다."));

        String status = friendService.getRelationStatus(currentUserId, targetUser.getId());

        return UserSearchResponse.of(targetUser, status);
    }
}
