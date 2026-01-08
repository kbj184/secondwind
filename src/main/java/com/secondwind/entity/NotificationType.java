package com.secondwind.entity;

public enum NotificationType {
    // Crew notifications
    CREW_JOIN_REQUEST("크루 가입 요청", "/crew/detail/{crewId}/members"),
    CREW_JOIN_APPROVED("크루 가입 승인", "/crew/detail/{crewId}"),
    CREW_JOIN_REJECTED("크루 가입 거절", "/crew"),
    CREW_INVITATION("크루 초대", "/crew/detail/{crewId}"),
    CREW_ANNOUNCEMENT("크루 공지", "/crew/detail/{crewId}/notice"),

    // Running notifications
    RUNNER_GRADE_UPGRADE("러너 등급 승급", "/profile/info"),
    FRIEND_RUNNING_RECORD("친구 러닝 기록", "/user/{userId}/profile"),
    COURSE_RECOMMENDATION("코스 추천", "/crew/detail/{crewId}/course/{courseId}"),

    // Social notifications
    FOLLOW("팔로우", "/user/{userId}/profile"),

    // System notifications
    SYSTEM_UPDATE("시스템 업데이트", "/"),
    EVENT_ANNOUNCEMENT("이벤트 공지", "/");

    private final String displayName;
    private final String routeTemplate;

    NotificationType(String displayName, String routeTemplate) {
        this.displayName = displayName;
        this.routeTemplate = routeTemplate;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRouteTemplate() {
        return routeTemplate;
    }

    public String getRoute(Object... params) {
        String route = routeTemplate;
        for (int i = 0; i < params.length; i++) {
            route = route.replaceFirst("\\{[^}]+\\}", String.valueOf(params[i]));
        }
        return route;
    }
}
