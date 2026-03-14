package com.coope.server.auth.application;

import com.coope.server.shared.error.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class EmailAuthService {

    private final RedisTemplate<String, String> redisTemplate;
    private final SecureRandom secureRandom;
    private final MailService mailService;

    private static final String AUTH_KEY_PREFIX     = "AUTH:";
    private static final String COMPLETE_KEY_PREFIX = "AUTH_COMPLETE:";

    public void sendAuthCode(String email) {
        String authCode = String.valueOf(secureRandom.nextInt(900000) + 100000);
        redisTemplate.opsForValue().set(AUTH_KEY_PREFIX + email, authCode, Duration.ofMinutes(5));

        String content = "안녕하세요! Coope입니다.\n\n인증번호는 [" + authCode + "] 입니다.\n5분 내에 입력해 주세요.";
        mailService.sendPlainText(email, "[Coope] 회원가입 인증번호 안내", content);
    }

    public void verifyCode(String email, String code) {
        String savedCode = redisTemplate.opsForValue().get(AUTH_KEY_PREFIX + email);
        if (savedCode == null || !savedCode.equals(code)) {
            throw new BadRequestException("인증번호가 일치하지 않거나 만료되었습니다.");
        }
        redisTemplate.delete(AUTH_KEY_PREFIX + email);
        redisTemplate.opsForValue().set(COMPLETE_KEY_PREFIX + email, "true", Duration.ofMinutes(10));
    }

    public boolean isVerified(String email) {
        return "true".equals(redisTemplate.opsForValue().get(COMPLETE_KEY_PREFIX + email));
    }
}
