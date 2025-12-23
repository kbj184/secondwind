package com.secondwind.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private Long id;
    private String role;
    private String providerId;
    private String name;
    private String email;
    private String password;
}