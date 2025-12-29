package com.secondwind.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class UserAuth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String authProvider;
    private String providerId;
    private String email;
    private String password;
    private String role;
    private String nickname;
    private String nicknameImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "runner_grade")
    private RunnerGrade runnerGrade = RunnerGrade.BEGINNER;
}
