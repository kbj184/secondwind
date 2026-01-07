package com.secondwind.repository;

import com.secondwind.entity.UserFcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFcmTokenRepository extends JpaRepository<UserFcmToken, Long> {

    Optional<UserFcmToken> findByToken(String token);

    List<UserFcmToken> findByUserId(Long userId);

    void deleteByToken(String token);

    void deleteByUserId(Long userId);

    boolean existsByToken(String token);
}
