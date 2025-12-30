package com.secondwind.service;

import com.secondwind.entity.RunnerGrade;
import com.secondwind.entity.RunningSession;
import com.secondwind.entity.UserAuth;
import com.secondwind.repository.RunningSessionRepository;
import com.secondwind.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RunnerGradeService {

    private final UserRepository userRepository;
    private final RunningSessionRepository runningSessionRepository;

    public RunnerGradeService(UserRepository userRepository,
            RunningSessionRepository runningSessionRepository) {
        this.userRepository = userRepository;
        this.runningSessionRepository = runningSessionRepository;
    }

    /**
     * 러닝 세션 완료 후 등급 자동 승급 체크
     */
    @Transactional
    public RunnerGrade checkAndUpgradeGrade(Long userId, double distance, int duration) {
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
            return achievedGrade;
        }

        return null; // 승급 없음
    }

    /**
     * 사용자의 현재 등급 조회
     */
    public RunnerGrade getUserGrade(Long userId) {
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
    public boolean setUserGrade(Long userId, RunnerGrade grade) {
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
}
