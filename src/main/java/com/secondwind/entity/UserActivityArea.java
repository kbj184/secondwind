package com.secondwind.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class UserActivityArea {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    private String mainCountryCode;
    private String mainCountryName;
    private String adminLevel1;
    private String adminLevel2;
    private String adminLevel3;

    @Column(length = 500)
    private String adminLevelFull; // 전체 주소

    private Double latitude;
    private Double longitude;
}
