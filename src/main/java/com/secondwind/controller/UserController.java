package com.secondwind.controller;

import com.secondwind.dto.UserDTO;
import com.secondwind.dto.UserProfileDTO;
import com.secondwind.entity.UserAuth;
import com.secondwind.entity.UserActivityArea;
import com.secondwind.entity.RunningSession;
import com.secondwind.repository.UserRepository;
import com.secondwind.repository.CrewMemberRepository;
import com.secondwind.repository.CrewRepository;
import com.secondwind.repository.RunningSessionRepository;
import com.secondwind.service.RunnerGradeService;
import com.secondwind.entity.RunnerGrade;
import com.secondwind.repository.UserActivityAreaRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserRepository userRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final CrewRepository crewRepository;
    private final RunnerGradeService runnerGradeService;
    private final UserActivityAreaRepository activityAreaRepository;
    private final RunningSessionRepository runningSessionRepository;

    public UserController(UserRepository userRepository,
            CrewMemberRepository crewMemberRepository,
            CrewRepository crewRepository,
            RunnerGradeService runnerGradeService,
            UserActivityAreaRepository activityAreaRepository,
            RunningSessionRepository runningSessionRepository) {
        this.userRepository = userRepository;
        this.crewMemberRepository = crewMemberRepository;
        this.crewRepository = crewRepository;
        this.runnerGradeService = runnerGradeService;
        this.activityAreaRepository = activityAreaRepository;
        this.runningSessionRepository = runningSessionRepository;
    }

    @PostMapping("/profile")
    public UserDTO updateProfile(@RequestBody UserDTO userDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("UserController: Profile update requested by " + email);

        UserAuth userAuth = userRepository.findByEmail(email);

        if (userAuth == null) {
            throw new RuntimeException("User not found");
        }

        // 닉네임 검증
        String nickname = userDTO.getNickname();
        validateNickname(nickname, userAuth.getId());

        userAuth.setNickname(nickname);
        userAuth.setNicknameImage(userDTO.getNicknameImage());
        userRepository.save(userAuth);

        // 전체 사용자 정보 반환 (MyController와 동일)
        UserDTO response = new UserDTO();
        response.setId(userAuth.getId());
        response.setEmail(userAuth.getEmail());
        response.setNickname(userAuth.getNickname());
        response.setNicknameImage(userAuth.getNicknameImage());
        response.setRole(userAuth.getRole());

        // Runner Grade
        Long userId = userAuth.getId();
        if (userId != null) {
            RunnerGrade correctedGrade = runnerGradeService.refreshUserGrade((long) userId);
            response.setRunnerGrade(correctedGrade != null ? correctedGrade.name() : "BEGINNER");
        } else {
            response.setRunnerGrade("BEGINNER");
        }

        // Crew Info
        var crewMember = crewMemberRepository.findByUserId(userAuth.getId());
        if (crewMember.isPresent()) {
            Long crewId = crewMember.get().getCrewId();
            if (crewId != null) {
                var crew = crewRepository.findById(crewId);
                if (crew.isPresent()) {
                    response.setCrewId(crew.get().getId());
                    response.setCrewName(crew.get().getName());
                    response.setCrewImage(crew.get().getImageUrl());
                }
            }
        }

        // Activity Area Status
        response.setActivityAreaRegistered(activityAreaRepository.findByUserId(userAuth.getId()).isPresent());

        return response;
    }

    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileDTO> getUserProfile(@PathVariable Long userId) {
        try {
            // 1. 사용자 기본 정보 조회
            UserAuth userAuth = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            UserProfileDTO profile = new UserProfileDTO();
            profile.setId(userAuth.getId());
            profile.setNickname(userAuth.getNickname());
            profile.setNicknameImage(userAuth.getNicknameImage());

            // 2. Runner Grade
            if (userId != null) {
                RunnerGrade grade = runnerGradeService.refreshUserGrade(userId);
                profile.setRunnerGrade(grade != null ? grade.name() : "BEGINNER");
            } else {
                profile.setRunnerGrade("BEGINNER");
            }

            // 3. Activity Area
            if (userId != null) {
                var activityArea = activityAreaRepository.findByUserId(userId);
                if (activityArea.isPresent()) {
                    UserActivityArea area = activityArea.get();
                    profile.setActivityAreaLevel2(area.getAdminLevel2());
                    profile.setActivityAreaFull(area.getAdminLevelFull());
                }
            }

            // 4. Running Statistics
            List<RunningSession> completedSessions = runningSessionRepository
                    .findByUserIdAndIsCompleteTrueOrderByCreatedAtDesc(userId);

            UserProfileDTO.RunningStats stats = new UserProfileDTO.RunningStats();
            stats.setTotalRuns(completedSessions.size());

            if (!completedSessions.isEmpty()) {
                // 총 거리
                double totalDistance = completedSessions.stream()
                        .mapToDouble(s -> s.getDistance() != null ? s.getDistance() : 0.0)
                        .sum();
                stats.setTotalDistance(totalDistance);

                // 최장 거리
                double bestDistance = completedSessions.stream()
                        .mapToDouble(s -> s.getDistance() != null ? s.getDistance() : 0.0)
                        .max()
                        .orElse(0.0);
                stats.setBestDistance(bestDistance);

                // 최고 페이스 (가장 낮은 pace 값)
                double bestPace = completedSessions.stream()
                        .filter(s -> s.getPace() != null && s.getPace() > 0)
                        .mapToDouble(RunningSession::getPace)
                        .min()
                        .orElse(0.0);
                stats.setBestPace(bestPace);

                // 최장 시간
                int bestDuration = completedSessions.stream()
                        .mapToInt(s -> s.getDuration() != null ? s.getDuration() : 0)
                        .max()
                        .orElse(0);
                stats.setBestDuration(bestDuration);
            } else {
                stats.setTotalDistance(0.0);
                stats.setBestDistance(0.0);
                stats.setBestPace(0.0);
                stats.setBestDuration(0);
            }
            profile.setStats(stats);

            // 5. Recent Activities (최근 5개)
            List<UserProfileDTO.RecentActivity> recentActivities = completedSessions.stream()
                    .limit(5)
                    .map(session -> {
                        UserProfileDTO.RecentActivity activity = new UserProfileDTO.RecentActivity();
                        activity.setSessionId(session.getSessionId());
                        activity.setDate(session.getCreatedAt());
                        activity.setDistance(session.getDistance() != null ? session.getDistance() : 0.0);
                        activity.setDuration(session.getDuration() != null ? session.getDuration() : 0);
                        activity.setPace(session.getPace() != null ? session.getPace() : 0.0);
                        activity.setThumbnail(session.getThumbnail());
                        return activity;
                    })
                    .collect(Collectors.toList());
            profile.setRecentActivities(recentActivities);

            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            System.err.println("Error fetching user profile: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/check-nickname")
    public boolean checkNickname(@RequestParam String nickname) {
        // 닉네임 중복 여부 반환 (true = 사용 가능, false = 중복)
        return !userRepository.existsByNickname(nickname);
    }

    private void validateNickname(String nickname, Long currentUserId) {
        // 1. null 또는 빈 값 체크
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임을 입력해주세요.");
        }

        // 2. 길이 체크 (2-10자)
        if (nickname.length() < 2) {
            throw new IllegalArgumentException("닉네임은 최소 2자 이상이어야 합니다.");
        }
        if (nickname.length() > 10) {
            throw new IllegalArgumentException("닉네임은 최대 10자까지 가능합니다.");
        }

        // 3. 특수문자 체크 (한글, 영문, 숫자만 허용)
        if (!nickname.matches("^[가-힣a-zA-Z0-9]+$")) {
            throw new IllegalArgumentException("닉네임은 한글, 영문, 숫자만 사용 가능합니다.");
        }

        // 4. 중복 체크 (자신의 닉네임은 제외)
        UserAuth existingUser = userRepository.findByNickname(nickname);
        if (existingUser != null && !existingUser.getId().equals(currentUserId)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // 5. 금지어 체크
        String[] bannedWords = { "관리자", "운영자", "admin", "root", "system" };
        String lowerNickname = nickname.toLowerCase();
        for (String banned : bannedWords) {
            if (lowerNickname.contains(banned)) {
                throw new IllegalArgumentException("사용할 수 없는 닉네임입니다.");
            }
        }
    }
}
