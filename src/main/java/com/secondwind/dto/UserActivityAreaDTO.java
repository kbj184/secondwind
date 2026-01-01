package com.secondwind.dto;

import lombok.Data;

@Data
public class UserActivityAreaDTO {
    private String mainCountryCode;
    private String mainCountryName;
    private String adminLevel1;
    private String adminLevel2;
    private String adminLevel3;
    private String adminLevelFull; // 전체 주소
    private Double latitude;
    private Double longitude;
}
