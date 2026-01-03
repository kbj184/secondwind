package com.secondwind.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrewMemberDTO {
    private Long id;
    private Long userId;
    private String nickname;
    private String nicknameImage;
    private String role;
    private String status; // "PENDING", "APPROVED", "REJECTED"
    private Boolean isPrimary; // Primary crew flag
    private String joinedAt;
}
