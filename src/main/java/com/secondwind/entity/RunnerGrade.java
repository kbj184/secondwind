package com.secondwind.entity;

/**
 * 러너 등급 시스템
 */
public enum RunnerGrade {
    BEGINNER("Beginner", 0, 5.0, null, 0),
    RUNNER_5K("5K Runner", 1, 10.0, 3600, 5.0),
    RUNNER_10K("10K Runner", 2, 21.0, 5400, 10.0),
    HALF_MARATHONER("Half Marathoner", 3, 42.0, 9000, 21.0),
    FULL_MARATHONER("Full Marathoner", 4, Double.MAX_VALUE, 19800, 42.0),
    SUB3_MARATHONER("Sub-3 Marathoner", 5, Double.MAX_VALUE, 10800, 42.0),
    ELITE_MARATHONER("Elite Marathoner", 6, Double.MAX_VALUE, 9000, 42.0),
    LEGEND_MARATHONER("Legend Marathoner", 7, Double.MAX_VALUE, 19800, 42.0); // 관리자 전용

    private final String displayName;
    private final int level;
    private final double maxDistance; // 최대 거리 (km)
    private final Integer maxTime; // 최대 시간 (초), null이면 제한 없음
    private final double minDistance; // 최소 거리 (km)

    RunnerGrade(String displayName, int level, double maxDistance, Integer maxTime, double minDistance) {
        this.displayName = displayName;
        this.level = level;
        this.maxDistance = maxDistance;
        this.maxTime = maxTime;
        this.minDistance = minDistance;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getLevel() {
        return level;
    }

    public double getMaxDistance() {
        return maxDistance;
    }

    public Integer getMaxTime() {
        return maxTime;
    }

    public double getMinDistance() {
        return minDistance;
    }

    /**
     * 러닝 기록으로 획득 가능한 최고 등급 계산 (Legend 제외)
     */
    public static RunnerGrade calculateGrade(double distance, int duration) {
        // Legend는 관리자만 부여 가능하므로 제외
        RunnerGrade[] grades = {
                ELITE_MARATHONER,
                SUB3_MARATHONER,
                FULL_MARATHONER,
                HALF_MARATHONER,
                RUNNER_10K,
                RUNNER_5K,
                BEGINNER
        };

        for (RunnerGrade grade : grades) {
            if (grade == LEGEND_MARATHONER)
                continue; // Legend는 스킵

            // 거리 조건 확인
            if (distance < grade.minDistance)
                continue;
            if (distance >= grade.maxDistance)
                continue;

            // 시간 조건 확인
            if (grade.maxTime != null && duration > grade.maxTime)
                continue;

            return grade;
        }

        return BEGINNER;
    }

    /**
     * 현재 등급보다 높은 등급인지 확인
     */
    public boolean isHigherThan(RunnerGrade other) {
        return this.level > other.level;
    }

    /**
     * 등급 설명 반환
     */
    public String getDescription() {
        switch (this) {
            case BEGINNER:
                return "5km 미만 (시간 제한 없음)";
            case RUNNER_5K:
                return "10km 미만 및 1시간 이내";
            case RUNNER_10K:
                return "21km 미만 및 1시간 30분 이내";
            case HALF_MARATHONER:
                return "42km 미만 및 2시간 30분 이내";
            case FULL_MARATHONER:
                return "42km 이상 및 5시간 30분 이내";
            case SUB3_MARATHONER:
                return "42km 이상 및 3시간 이내";
            case ELITE_MARATHONER:
                return "42km 이상 및 2시간 30분 이내";
            case LEGEND_MARATHONER:
                return "전설의 러너 (관리자 승급)";
            default:
                return "";
        }
    }
}
