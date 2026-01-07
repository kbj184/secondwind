package com.secondwind.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserProfileDTO {
    // Basic User Info
    private Long id;
    private String nickname;
    private String nicknameImage;
    private String runnerGrade;

    // Activity Area
    private String activityAreaLevel2; // 시/도
    private String activityAreaFull; // 전체 주소

    // Running Statistics
    private RunningStats stats;

    // Recent Activities
    private List<RecentActivity> recentActivities;

    @Data
    public static class RunningStats {
        private int totalRuns; // 총 러닝 횟수
        private double totalDistance; // 총 거리 (km)
        private double bestDistance; // 최장 거리 (km)
        private double bestPace; // 최고 페이스 (min/km)
        private int bestDuration; // 최장 시간 (seconds)
    }

    @Data
    public static class RecentActivity {
        private String sessionId;
        private LocalDateTime date;
        private double distance; // km
        private int duration; // seconds
        private double pace; // min/km
        private String thumbnail; // 지도 썸네일 URL
    }
}
