package com.secondwind.repository;

import com.secondwind.entity.RunningSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RunningSessionRepository extends JpaRepository<RunningSession, Long> {

    // 사용자 ID로 모든 세션 조회
    List<RunningSession> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 세션 ID로 조회
    Optional<RunningSession> findBySessionId(String sessionId);

    // 사용자 ID와 세션 ID로 조회
    Optional<RunningSession> findByUserIdAndSessionId(Long userId, String sessionId);

    // 완료된 세션만 조회
    List<RunningSession> findByUserIdAndIsCompleteTrueOrderByCreatedAtDesc(Long userId);
}
