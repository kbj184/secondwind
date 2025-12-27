package com.secondwind.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private Long id;
    private String role;
    private String username;
    private String providerId;
    private String name;
    private String email;
    private String nickname;
    private String nicknameImage;
    private String password;

    // Crew Info
    private Long crewId;
    private String crewName;
    private String crewImage;
}