package com.secondwind.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class CrewDTO {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private Long captainId;
    private String joinType; // "AUTO" or "APPROVAL"
    private String createdAt;
    private long memberCount; // 멤버 수 추가
    private List<ActivityAreaDTO> activityAreas; // 활동 지역 목록

    // 활동 지역 정보 (대표 지역)
    private String activityAreaLevel1; // 시/도
    private String activityAreaLevel2; // 시/군/구
    private String activityAreaLevel3; // 읍/면/동

    // 크루 통계
    private Double totalDistance; // 크루원 총 이동거리 (km)
}
