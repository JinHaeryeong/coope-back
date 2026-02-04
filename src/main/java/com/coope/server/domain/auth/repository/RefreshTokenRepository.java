package com.coope.server.domain.auth.repository;

import com.coope.server.domain.auth.entity.RefreshToken;
import com.coope.server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByUser(User user);
    Optional<RefreshToken> findByTokenValue(String tokenValue);
    void deleteByTokenValue(String tokenValue);
}
