package com.secondwind.controller;

import com.secondwind.dto.RunningSessionDTO;
import com.secondwind.entity.RunnerGrade;
import com.secondwind.entity.RunningSession;
import com.secondwind.repository.RunningSessionRepository;
import com.secondwind.service.RunnerGradeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/running")
public class RunningController {

    private final RunningSessionRepository runningSessionRepository;
    private final RunnerGradeService runnerGradeService;

    public RunningController(RunningSessionRepository runningSessionRepository,
            RunnerGradeService runnerGradeService) {
        this.runningSessionRepository = runningSessionRepository;
        this.runnerGradeService = runnerGradeService;
    }

    /**
     * 러닝 세션 동기화 (생성 또는 업데이트)
     */
    @PostMapping("/session/sync")
    public ResponseEntity<?> syncRunningSession(@RequestBody RunningSessionDTO dto) {
        try {
            System.out.println("📥 Sync request received:");
            System.out.println("   User ID: " + dto.getUserId());
            System.out.println("   Session ID: " + dto.getSessionId());
            System.out.println("   Distance: " + dto.getDistance() + "km");
            System.out.println("   Duration: " + dto.getDuration() + "s");
            System.out.println("   Is Complete: " + dto.getIsComplete());

            // 기존 세션 찾기
            RunningSession session = runningSessionRepository
                    .findBySessionId(dto.getSessionId())
                    .orElse(new RunningSession());

            boolean isNewSession = (session.getId() == null);
            System.out.println("   " + (isNewSession ? "🆕 Creating new session"
                    : "♻️ Updating existing session (ID: " + session.getId() + ")"));

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
            session.setThumbnail(dto.getThumbnail());

            // 저장
            RunningSession saved = runningSessionRepository.save(session);

            System.out.println("✅ Running session saved successfully!");
            System.out.println("   DB ID: " + saved.getId());
            System.out.println("   Session ID: " + saved.getSessionId());
            System.out.println("   Complete: " + saved.getIsComplete());
            System.out.println("   Created At: " + saved.getCreatedAt());

            // 세션 완료 시 등급 자동 승급 체크
            RunnerGrade upgradedGrade = null;
            if (dto.getIsComplete() != null && dto.getIsComplete()) {
                // 거리 단위 보정 (m -> km)
                // 만약 거리가 200.0보다 크다면 미터 단위로 간주하고 km로 변환 (일반적인 러닝 범위 고려)
                double distanceKm = dto.getDistance();
                if (distanceKm > 200.0) {
                    distanceKm = distanceKm / 1000.0;
                    System.out.println(
                            "⚠️ Distance unit correction applied: " + dto.getDistance() + " -> " + distanceKm + "km");
                }

                if (dto.getUserId() != null) {
                    upgradedGrade = runnerGradeService.checkAndUpgradeGrade(
                            (long) dto.getUserId(),
                            distanceKm,
                            dto.getDuration());
                }
            }

            // 응답에 등급 정보 포함
            Map<String, Object> response = new HashMap<>();
            response.put("session", saved);
            if (upgradedGrade != null) {
                response.put("gradeUpgraded", true);
                response.put("newGrade", upgradedGrade.getDisplayName());
                response.put("gradeLevel", upgradedGrade.getLevel());
                response.put("gradeDescription", upgradedGrade.getDescription());
            } else {
                response.put("gradeUpgraded", false);
            }

            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            System.err.println("❌ Error syncing running session:");
            System.err.println("   Message: " + e.getMessage());
            System.err.println("   Class: " + e.getClass().getName());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Sync failed: " + e.getMessage());
        }
    }

    /**
     * 테스트용 엔드포인트 - DB 연결 및 데이터 확인
     */
    @GetMapping("/test/count")
    public ResponseEntity<?> getSessionCount() {
        try {
            long count = runningSessionRepository.count();
            System.out.println("📊 Total sessions in DB: " + count);
            return ResponseEntity.ok().body("Total sessions: " + count);
        } catch (Exception e) {
            System.err.println("❌ Error counting sessions: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
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
        dto.setThumbnail(session.getThumbnail());

        // createdAt을 timestamp(epoch milliseconds)로 변환
        if (session.getCreatedAt() != null) {
            dto.setTimestamp(session.getCreatedAt()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli());
        }

        return dto;
    }
}
