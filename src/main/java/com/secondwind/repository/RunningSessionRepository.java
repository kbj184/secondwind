package com.secondwind.repository;

import com.secondwind.entity.RunningSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RunningSessionRepository extends JpaRepository<RunningSession, Long> {

    // 사용자 ID로 모든 세션 조회
    List<RunningSession> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByCourseId(Long courseId);

    // 세션 ID로 조회
    Optional<RunningSession> findBySessionId(String sessionId);

    // 사용자 ID와 세션 ID로 조회
    Optional<RunningSession> findByUserIdAndSessionId(Long userId, String sessionId);

    // 완료된 세션만 조회
    List<RunningSession> findByUserIdAndIsCompleteTrueOrderByCreatedAtDesc(Long userId);

    // 크루원 총 이동거리 집계
    @Query("SELECT SUM(r.distance) FROM RunningSession r " +
            "WHERE r.userId IN (SELECT cm.userId FROM CrewMember cm " +
            "WHERE cm.crewId = :crewId AND cm.status = 'APPROVED') " +
            "AND r.isComplete = true")
    Double sumDistanceByCrewMembers(@Param("crewId") Long crewId);

    // 특정 코스의 따라 달리기 기록 조회
    List<RunningSession> findByCourseIdAndUserIdOrderByCreatedAtDesc(Long courseId, Long userId);
}
