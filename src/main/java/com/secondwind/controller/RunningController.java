package com.secondwind.controller;

import com.secondwind.dto.RunningSessionDTO;
import com.secondwind.entity.RunningSession;
import com.secondwind.repository.RunningSessionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/running")
public class RunningController {

    private final RunningSessionRepository runningSessionRepository;

    public RunningController(RunningSessionRepository runningSessionRepository) {
        this.runningSessionRepository = runningSessionRepository;
    }

    /**
     * 러닝 세션 동기화 (생성 또는 업데이트)
     */
    @PostMapping("/session/sync")
    public ResponseEntity<?> syncRunningSession(@RequestBody RunningSessionDTO dto) {
        try {
            // 기존 세션 찾기
            RunningSession session = runningSessionRepository
                    .findBySessionId(dto.getSessionId())
                    .orElse(new RunningSession());

            // 데이터 업데이트
            session.setUserId(dto.getUserId());
            session.setSessionId(dto.getSessionId());
            session.setDistance(dto.getDistance());
            session.setDuration(dto.getDuration());
            session.setSpeed(dto.getSpeed());
            session.setPace(dto.getPace());
            session.setCurrentElevation(dto.getCurrentElevation());
            session.setTotalAscent(dto.getTotalAscent());
            session.setTotalDescent(dto.getTotalDescent());
            session.setRoute(dto.getRoute());
            session.setWateringSegments(dto.getWateringSegments());
            session.setSplits(dto.getSplits());
            session.setIsComplete(dto.getIsComplete());

            // 저장
            RunningSession saved = runningSessionRepository.save(session);

            System.out.println("✅ Running session synced: " + saved.getSessionId() +
                    " (Complete: " + saved.getIsComplete() + ")");

            return ResponseEntity.ok().body(saved);

        } catch (Exception e) {
            System.err.println("❌ Error syncing running session: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Sync failed: " + e.getMessage());
        }
    }

    /**
     * 사용자의 모든 러닝 세션 조회
     */
    @GetMapping("/sessions")
    public ResponseEntity<List<RunningSessionDTO>> getUserSessions(@RequestParam Long userId) {
        try {
            List<RunningSession> sessions = runningSessionRepository
                    .findByUserIdOrderByCreatedAtDesc(userId);

            List<RunningSessionDTO> dtos = sessions.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);

        } catch (Exception e) {
            System.err.println("❌ Error fetching sessions: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 완료된 세션만 조회
     */
    @GetMapping("/sessions/completed")
    public ResponseEntity<List<RunningSessionDTO>> getCompletedSessions(@RequestParam Long userId) {
        try {
            List<RunningSession> sessions = runningSessionRepository
                    .findByUserIdAndIsCompleteTrueOrderByCreatedAtDesc(userId);

            List<RunningSessionDTO> dtos = sessions.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);

        } catch (Exception e) {
            System.err.println("❌ Error fetching completed sessions: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 세션 조회
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<RunningSessionDTO> getSession(@PathVariable String sessionId) {
        try {
            RunningSession session = runningSessionRepository
                    .findBySessionId(sessionId)
                    .orElse(null);

            if (session == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(convertToDTO(session));

        } catch (Exception e) {
            System.err.println("❌ Error fetching session: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 세션 삭제
     */
    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<?> deleteSession(@PathVariable String sessionId) {
        try {
            RunningSession session = runningSessionRepository
                    .findBySessionId(sessionId)
                    .orElse(null);

            if (session == null) {
                return ResponseEntity.notFound().build();
            }

            runningSessionRepository.delete(session);
            System.out.println("🗑️ Session deleted: " + sessionId);

            return ResponseEntity.ok().body("Session deleted");

        } catch (Exception e) {
            System.err.println("❌ Error deleting session: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // Entity -> DTO 변환
    private RunningSessionDTO convertToDTO(RunningSession session) {
        RunningSessionDTO dto = new RunningSessionDTO();
        dto.setId(session.getId());
        dto.setUserId(session.getUserId());
        dto.setSessionId(session.getSessionId());
        dto.setDistance(session.getDistance());
        dto.setDuration(session.getDuration());
        dto.setSpeed(session.getSpeed());
        dto.setPace(session.getPace());
        dto.setCurrentElevation(session.getCurrentElevation());
        dto.setTotalAscent(session.getTotalAscent());
        dto.setTotalDescent(session.getTotalDescent());
        dto.setRoute(session.getRoute());
        dto.setWateringSegments(session.getWateringSegments());
        dto.setSplits(session.getSplits());
        dto.setIsComplete(session.getIsComplete());
        return dto;
    }
}
