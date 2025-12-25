package com.secondwind.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RunningRecordDTO {
    private String sessionId;
    private Double distance;
    private Long duration;
    private Double speed;
    private Double pace;
    private String route; // JSON string from frontend
    private String wateringSegments; // JSON string from frontend
    private String splits; // JSON string from frontend
    private Boolean isComplete;
}
