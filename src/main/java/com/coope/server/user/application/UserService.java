package com.coope.server.user.application;


import com.coope.server.auth.application.EmailAuthService;
import com.coope.server.domain.friend.service.FriendService;
import com.coope.server.user.presentation.dto.ProfileUpdateFullRequest;
import com.coope.server.user.presentation.dto.SignupRequest;
import com.coope.server.user.presentation.dto.UserResponse;
import com.coope.server.user.presentation.dto.UserSearchResponse;
import com.coope.server.user.domain.User;
import com.coope.server.user.domain.UserRepository;
import com.coope.server.global.error.exception.BadRequestException;
import com.coope.server.global.error.exception.ConflictException;
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
        validateSignup(request);

        String userIconUrl = fileService.upload(request.getUserIcon(), ImageCategory.PROFILE);
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.createLocalUser(request, encodedPassword, userIconUrl);
        return userRepository.save(user).getId();
    }

    private void validateSignup(SignupRequest request) {
        if (!emailAuthService.isVerified(request.getEmail())) {
            throw new BadRequestException("이메일 인증이 완료되지 않았습니다.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("이미 존재하는 이메일입니다.");
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new ConflictException("이미 존재하는 닉네임입니다.");
        }
    }

    public void checkPassword(Long userId, String password) {
        User user = findUserOrThrow(userId);
        user.authenticate(password, passwordEncoder);
    }

    public User validateUser(String email, String password) {
        User user = findByEmail(email);
        user.authenticate(password, passwordEncoder);
        return user;
    }

    public UserResponse getMyInfo(Long userId) {
        return UserResponse.from(findUserOrThrow(userId));
    }

    @Transactional
    public UserResponse updateProfile(Long userId, ProfileUpdateFullRequest request) {
        User user = findUserOrThrow(userId);

        handleNicknameUpdate(user, request.getNickname());

        handleProfileImageUpdate(user, request);

        if (StringUtils.hasText(request.getNewPassword())) {
            user.changePassword(
                    passwordEncoder.encode(request.getNewPassword()),
                    request.getCurrentPassword(),
                    passwordEncoder
            );
        }

        return UserResponse.from(user);
    }

    public UserSearchResponse searchUserByNickname(Long currentUserId, String nickname) {
        User targetUser = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new UserNotFoundException("유저를 찾을 수 없습니다."));

        String status = friendService.getRelationStatus(currentUserId, targetUser.getId());
        return UserSearchResponse.of(targetUser, status);
    }

    private void handleNicknameUpdate(User user, String newNickname) {
        if (!StringUtils.hasText(newNickname) || user.isSameNickname(newNickname)) {
            return;
        }
        if (userRepository.existsByNickname(newNickname)) {
            throw new ConflictException("이미 사용 중인 닉네임입니다.");
        }
        user.changeNickname(newNickname);
    }

    private void handleProfileImageUpdate(User user, ProfileUpdateFullRequest request) {
        boolean hasNewImage = request.getProfileImage() != null && !request.getProfileImage().isEmpty();

        if (!hasNewImage && !request.isDeleteProfileImage()) return;

        String oldIcon = user.getUserIcon();
        String newUrl = hasNewImage ? uploadNewImage(request.getProfileImage()) : null;

        user.updateProfileImage(newUrl);
        deleteOldImageIfExists(oldIcon);
    }

    private String uploadNewImage(MultipartFile image) {
        if (image.getSize() > 10 * 1024 * 1024) {
            throw new BadRequestException("프로필 이미지는 10MB를 초과할 수 없습니다.");
        }
        return fileService.upload(image, ImageCategory.PROFILE);
    }

    private void deleteOldImageIfExists(String oldIcon) {
        if (!StringUtils.hasText(oldIcon)) return;

        if (!fileService.deleteFile(oldIcon, ImageCategory.PROFILE)) {
            log.warn("기존 프로필 이미지 삭제 실패: {}", oldIcon);
            throw new BadRequestException("기존 이미지 삭제 실패로 인해 업데이트를 중단합니다.");
        }
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 계정입니다."));
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 계정입니다."));
    }
}