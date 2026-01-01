package com.secondwind.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivityAreaDTO {
    private String countryCode;
    private String countryName;
    private String adminLevel1;
    private String adminLevel2;
    private String adminLevel3;
    private String adminLevelFull;
    private Double latitude;
    private Double longitude;
}
