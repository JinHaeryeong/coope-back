package com.coope.server.domain.auth.service;

import com.coope.server.global.error.exception.BadRequestException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class EmailAuthService {

    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;
    private final SecureRandom secureRandom;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendAuthCode(String email) {
        String authCode = String.valueOf(secureRandom.nextInt(900000) + 100000);

        redisTemplate.opsForValue().set(
                "AUTH:" + email,
                authCode,
                Duration.ofMinutes(5)
        );

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Coope");
            helper.setTo(email);
            helper.setSubject("[Coope] 회원가입 인증번호 안내");

            String content = "안녕하세요! Coope입니다.\n\n인증번호는 [" + authCode + "] 입니다.\n5분 내에 입력해 주세요.";
            helper.setText(content, false);

            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException("메일 전송에 실패했습니다.");
        }
    }

    public void verifyCode(String email, String code) {
        String savedCode = redisTemplate.opsForValue().get("AUTH:" + email);

        if (savedCode == null || !savedCode.equals(code)) {
            throw new BadRequestException("인증번호가 일치하지 않거나 만료되었습니다.");
        }

        redisTemplate.delete("AUTH:" + email);

        redisTemplate.opsForValue().set(
                "AUTH_COMPLETE:" + email,
                "true",
                Duration.ofMinutes(10)
        );
    }

    public boolean isVerified(String email) {
        String isComplete = redisTemplate.opsForValue().get("AUTH_COMPLETE:" + email);
        return "true".equals(isComplete);
    }
}