package com.coope.server.auth.application;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void send(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, "Coope");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("[Mail] 발송 완료 - to: {}, subject: {}", to, subject);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("[Mail] 발송 실패 - to: {}, error: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendPlainText(String to, String subject, String textContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, "Coope");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(textContent, false);
            mailSender.send(message);
            log.info("[Mail] 발송 완료 - to: {}, subject: {}", to, subject);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("[Mail] 발송 실패 - to: {}, error: {}", to, e.getMessage());
        }
    }
}
