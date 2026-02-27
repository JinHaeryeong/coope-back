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
import org.springframework.web.multipart.MultipartFile;

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
        User user = findUserOrThrow(userId);

        updateNickname(user, request.getNickname());

        updateProfileImage(user, request);

        updatePassword(user, request.getNewPassword(), request.getCurrentPassword());

        return UserResponse.from(user);
    }


    public UserSearchResponse searchUserByNickname(Long currentUserId, String nickname) {
        User targetUser = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new UserNotFoundException("유저를 찾을 수 없습니다."));

        String status = friendService.getRelationStatus(currentUserId, targetUser.getId());

        return UserSearchResponse.of(targetUser, status);
    }


    private void updateNickname(User user, String newNickname) {
        if (!StringUtils.hasText(newNickname)) return;

        if (!newNickname.equals(user.getNickname()) && userRepository.existsByNickname(newNickname)) {
            throw new BadRequestException("이미 사용 중인 닉네임입니다.");
        }
        user.updateNickname(newNickname);
    }

    private void updateProfileImage(User user, ProfileUpdateFullRequest request) {
        String oldIcon = user.getUserIcon();
        boolean hasNewImage = request.getProfileImage() != null && !request.getProfileImage().isEmpty();

        if (hasNewImage || request.isDeleteProfileImage()) {
            String newUrl = hasNewImage ? uploadNewImage(request.getProfileImage()) : null;
            user.updateProfileImage(newUrl);

            deleteOldImageIfExists(oldIcon);
        }
    }

    private String uploadNewImage(MultipartFile image) {
        if (image.getSize() > 10 * 1024 * 1024) {
            throw new BadRequestException("프로필 이미지는 10MB를 초과할 수 없습니다.");
        }
        return fileService.upload(image, ImageCategory.PROFILE);
    }

    private void deleteOldImageIfExists(String oldIcon) {
        if (StringUtils.hasText(oldIcon)) {
            boolean deleted = fileService.deleteFile(oldIcon, ImageCategory.PROFILE);
            if (!deleted) {
                log.warn("기존 프로필 이미지 삭제 실패: {}", oldIcon);
                throw new BadRequestException("기존 이미지 삭제 실패로 인해 업데이트를 중단합니다.");
            }
        }
    }

    private void updatePassword(User user, String newPassword, String currentPassword) {
        if (!StringUtils.hasText(newPassword)) return;

        if (!StringUtils.hasText(currentPassword)) {
            throw new BadRequestException("비밀번호 변경을 위해 현재 비밀번호를 입력해주세요.");
        }

        if (!user.matchesPassword(currentPassword, passwordEncoder)) {
            throw new AuthenticationException("현재 비밀번호가 일치하지 않아 수정을 완료할 수 없습니다.");
        }
        user.updatePassword(passwordEncoder.encode(newPassword));
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 계정입니다."));
    }
}
