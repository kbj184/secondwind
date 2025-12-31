package com.secondwind.dto;

import lombok.Data;

@Data
public class RunningSessionDTO {
    private Long id;
    private Long userId;
    private String sessionId;
    private Double distance;
    private Integer duration;
    private Double speed;
    private Double pace;
    private Double currentElevation;
    private Double totalAscent;
    private Double totalDescent;
    private String route;
    private String wateringSegments;
    private String splits;
    private Boolean isComplete;
    private Long timestamp; // 타임스탬프 추가
    private String thumbnail; // 썸네일 URL 추가
}
