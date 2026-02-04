package com.coope.server.domain.user.repository;

import com.coope.server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 찾기 (로그인 시 사용)
    Optional<User> findByEmail(String email);

    // 이메일 중복 여부 확인 (회원가입 시 사용)
    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);
}