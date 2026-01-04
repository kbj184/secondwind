package com.secondwind.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrewCourseDTO {
    private Long id;
    private Long crewId;
    private Long userId;
    private String name;
    private String description;
    private Double distance;
    private String routeData;
    private String mapThumbnailUrl;
    private LocalDateTime createdAt;

    // Creator info
    private String creatorNickname;
    private String creatorProfileImage;
}
