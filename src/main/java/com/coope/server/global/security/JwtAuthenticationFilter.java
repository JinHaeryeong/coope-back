package com.coope.server.global.security;

import com.coope.server.global.error.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // 요청 헤더에서 Bearer 토큰 추출
        String token = resolveToken(request);

        try {
            if (token != null && jwtProvider.validateToken(token)) {
                if (isBlacklisted(token)) {
                    sendErrorResponse(response, "이미 로그아웃된 토큰입니다.");
                    return;
                }
                Authentication authentication = jwtProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            // 토큰 파싱 중 에러(만료, 위조 등)가 발생했을 때 공통 응답 처리
            sendErrorResponse(response, "유효하지 않은 인증 토큰입니다.");
            return;
        }

        filterChain.doFilter(request, response);
    }


    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        String json = objectMapper.writeValueAsString(ErrorResponse.fail(message));
        response.getWriter().write(json);
    }

    private boolean isBlacklisted(String token) {
        Object logoutIndicator = redisTemplate.opsForValue().get(token);
        return logoutIndicator != null;
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}