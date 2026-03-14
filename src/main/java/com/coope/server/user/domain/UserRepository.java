package com.coope.server.user.domain;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    Optional<User> findByNickname(String nickname);
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    User save(User user);
    List<User> findAllById(Iterable<Long> ids);
    List<User> findAllByNameAndNickname(String name, String nickname);
}
