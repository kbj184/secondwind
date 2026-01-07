package com.secondwind.service;

import com.secondwind.entity.RunnerGrade;
import com.secondwind.entity.RunningSession;
import com.secondwind.entity.UserAuth;
import com.secondwind.repository.RunningSessionRepository;
import com.secondwind.repository.UserRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class RunnerGradeService {

    private final UserRepository userRepository;
    private final RunningSessionRepository runningSessionRepository;
    private final FcmService fcmService;

    public RunnerGradeService(UserRepository userRepository,
            RunningSessionRepository runningSessionRepository,
            FcmService fcmService) {
        this.userRepository = userRepository;
        this.runningSessionRepository = runningSessionRepository;
        this.fcmService = fcmService;
    }

    /**
     * 러닝 세션 완료 후 등급 자동 승급 체크
     */
    @Transactional
    public RunnerGrade checkAndUpgradeGrade(@NonNull Long userId, double distance, int duration) {
        UserAuth user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            System.err.println("❌ User not found: " + userId);
            return null;
        }

        RunnerGrade currentGrade = user.getRunnerGrade();
        if (currentGrade == null) {
            currentGrade = RunnerGrade.BEGINNER;
            user.setRunnerGrade(currentGrade);
        }

        // Legend는 관리자만 부여 가능하므로 자동 승급 대상에서 제외
        if (currentGrade == RunnerGrade.LEGEND_MARATHONER) {
            return currentGrade;
        }

        // 이번 러닝으로 획득 가능한 등급 계산
        RunnerGrade achievedGrade = RunnerGrade.calculateGrade(distance, duration);

        // 현재 등급보다 높은 등급을 달성했는지 확인
        if (achievedGrade.isHigherThan(currentGrade)) {
            System.out.println("🎉 Grade upgraded!");
            System.out.println("   User: " + user.getNickname() + " (ID: " + userId + ")");
            System.out.println("   " + currentGrade.getDisplayName() + " → " + achievedGrade.getDisplayName());
            System.out.println("   Distance: " + distance + "km, Duration: " + duration + "s");

            user.setRunnerGrade(achievedGrade);
            userRepository.save(user);

            // Send FCM notification
            try {
                fcmService.sendToUser(
                        userId,
                        "러너 등급 승급!",
                        "축하합니다! " + achievedGrade.getDisplayName() + " 등급으로 승급했습니다!",
                        com.secondwind.entity.NotificationType.RUNNER_GRADE_UPGRADE,
                        Map.of());
            } catch (Exception e) {
                System.err.println("Failed to send grade upgrade notification: " + e.getMessage());
            }

            return achievedGrade;
        }

        return null; // 승급 없음
    }

    /**
     * 사용자의 현재 등급 조회
     */
    public RunnerGrade getUserGrade(@NonNull Long userId) {
        UserAuth user = userRepository.findById(userId).orElse(null);
        if (user == null)
            return RunnerGrade.BEGINNER;

        RunnerGrade grade = user.getRunnerGrade();
        return grade != null ? grade : RunnerGrade.BEGINNER;
    }

    /**
     * 관리자 전용: 사용자 등급 수동 설정
     */
    @Transactional
    public boolean setUserGrade(@NonNull Long userId, RunnerGrade grade) {
        UserAuth user = userRepository.findById(userId).orElse(null);
        if (user == null)
            return false;

        RunnerGrade oldGrade = user.getRunnerGrade();
        user.setRunnerGrade(grade);
        userRepository.save(user);

        System.out.println("👑 Admin grade change:");
        System.out.println("   User: " + user.getNickname() + " (ID: " + userId + ")");
        System.out.println(
                "   " + (oldGrade != null ? oldGrade.getDisplayName() : "None") + " → " + grade.getDisplayName());

        return true;
    }

    /**
     * 사용자의 최고 기록 조회
     */
    public RunningSession getBestRecord(Long userId) {
        List<RunningSession> sessions = runningSessionRepository
                .findByUserIdAndIsCompleteTrueOrderByCreatedAtDesc(userId);

        if (sessions.isEmpty())
            return null;

        // 가장 긴 거리 기록 찾기
        return sessions.stream()
                .max((s1, s2) -> Double.compare(s1.getDistance(), s2.getDistance()))
                .orElse(null);
    }

    /**
     * 사용자의 실제 기록을 기반으로 등급을 재계산하고 동기화함 (데이터 정합성 유지용)
     */
    @Transactional
    public RunnerGrade refreshUserGrade(@NonNull Long userId) {
        UserAuth user = userRepository.findById(userId).orElse(null);
        if (user == null)
            return RunnerGrade.BEGINNER;

        // 실제 최고 기록 조회
        RunningSession bestSession = getBestRecord(userId);

        RunnerGrade realGrade = RunnerGrade.BEGINNER;
        if (bestSession != null) {
            // 최고 기록 기준 등급 계산
            realGrade = RunnerGrade.calculateGrade(bestSession.getDistance(), bestSession.getDuration());
        }

        // 현재 등급과 다르면 업데이트 (다운그레이드 포함)
        // Legend 등급은 관리자 부여이므로 자동 강등에서 제외할 수도 있으나,
        // 여기서는 데이터 오염 복구가 목적이므로 실제 기록 기준으로 덮어씀 (필요 시 로직 조정 가능)
        if (user.getRunnerGrade() != realGrade && user.getRunnerGrade() != RunnerGrade.LEGEND_MARATHONER) {
            System.out.println(
                    "♻️ Grade Recalculated for user " + userId + ": " + user.getRunnerGrade() + " -> " + realGrade);
            user.setRunnerGrade(realGrade);
            userRepository.save(user);
        }

        return user.getRunnerGrade() != null ? user.getRunnerGrade() : RunnerGrade.BEGINNER;
    }
}
