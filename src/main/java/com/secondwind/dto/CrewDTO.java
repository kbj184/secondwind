package com.secondwind.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrewDTO {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private Long captainId;
    private String createdAt;
    private long memberCount; // 멤버 수 추가
}
