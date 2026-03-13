package com.coope.server.global.config;

import com.coope.server.domain.auth.handler.OAuth2AuthenticationSuccessHandler;
import com.coope.server.domain.auth.service.CustomOAuth2UserService;
import com.coope.server.global.security.CustomAuthenticationEntryPoint;
import com.coope.server.global.security.JwtAuthenticationFilter;
import com.coope.server.global.security.JwtProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Value("${client.url}")
    private String clientUrl;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(AbstractHttpConfigurer::disable)
                // CORS 설정 연결
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // CSRF 비활성화 (Stateless한 API 서버이므로 필수)
                .csrf(AbstractHttpConfigurer::disable)
                // 비동기 요청 시에도 인증 정보를 유지하도록 설정
                // 이 설정이 있어야 스트리밍 중에도 사용자가 누군지 잊어버리지 않음
                .securityContext(context -> context.requireExplicitSave(false))
                // 예외 핸들링
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                )
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()
                        .requestMatchers("/api/auth/login", "/api/auth/refresh", "/api/auth/email/**", "/images/**").permitAll()
                        .requestMatchers("/oauth2/**", "/login/oauth2/**", "/ws-stomp/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/notices").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/notices/{id}").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/notices/{id}").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/inquiries").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/inquiries/*/answers").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/notices", "/api/notices/{id}").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/notices/{id}/views").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/user").permitAll()
                        .requestMatchers("/api/inquiries/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/inquiries/{id}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/inquiries/{id}").authenticated()

                        .requestMatchers(HttpMethod.GET, "/api/friends").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/friends/received").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/friends/{friendId}").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/friends/{friendId}/accept").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/friends/{friendId}").authenticated()

                        .requestMatchers(HttpMethod.GET, "/api/documents/sidebar").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/documents/trash").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/documents/{documentId}").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/documents").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/documents/{documentId}/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/documents/{documentId}/archive").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/documents/{documentId}/restore").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/documents/{documentId}/snapshots").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/documents/{documentId}").authenticated()

                        // 워크스페이스 및 기타
                        .requestMatchers("/api/workspaces/**").authenticated()
                        .requestMatchers("/api/liveblocks-auth").authenticated()
                        .requestMatchers("/api/chat/rooms/**").authenticated()
                        .requestMatchers("/api/chat/**").authenticated()
                        .requestMatchers("/api/ai-chat/**").authenticated()
                        .requestMatchers("/api/ai/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/notices/*/comments").permitAll()
                        .requestMatchers("/api/notices/*/comments/**").authenticated()
                        .requestMatchers("/api/user/me/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/user").authenticated()

                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                                .userInfoEndpoint(userInfo -> userInfo
                                        .userService(customOAuth2UserService)
                                )
                         .successHandler(oAuth2AuthenticationSuccessHandler)
                )
                // 필터 추가
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider, redisTemplate, objectMapper), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(clientUrl, "http://localhost:4173")); // 프론트엔드 주소
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("*"));
        configuration.setAllowCredentials(true); // 쿠키 및 인증 헤더 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}