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
}
