package com.secondwind.controller;

import com.secondwind.dto.UserDTO;
import com.secondwind.entity.UserAuth;
import com.secondwind.entity.CrewMember;
import com.secondwind.repository.UserRepository;
import com.secondwind.repository.CrewMemberRepository;
import com.secondwind.repository.CrewRepository;
import com.secondwind.repository.UserActivityAreaRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.secondwind.service.RunnerGradeService;
import com.secondwind.entity.RunnerGrade;

@RestController
public class MyController {

    private final UserRepository userRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final CrewRepository crewRepository;
    private final RunnerGradeService runnerGradeService;
    private final UserActivityAreaRepository activityAreaRepository;

    public MyController(UserRepository userRepository,
            CrewMemberRepository crewMemberRepository,
            CrewRepository crewRepository,
            RunnerGradeService runnerGradeService,
            UserActivityAreaRepository activityAreaRepository) {
        this.userRepository = userRepository;
        this.crewMemberRepository = crewMemberRepository;
        this.crewRepository = crewRepository;
        this.runnerGradeService = runnerGradeService;
        this.activityAreaRepository = activityAreaRepository;
    }

    @GetMapping("/my")
    public UserDTO my() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserAuth userAuth = userRepository.findByEmail(email);

        if (userAuth == null) {
            return null;
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setId(userAuth.getId());
        userDTO.setEmail(userAuth.getEmail());
        userDTO.setRole(userAuth.getRole());
        userDTO.setNickname(userAuth.getNickname());
        userDTO.setNicknameImage(userAuth.getNicknameImage());

        // Runner Grade - 실제 기록 기반 재동기화 (데이터 오염 복구)
        Long idObj = userAuth.getId();
        if (idObj != null) {
            long userId = idObj;
            RunnerGrade correctedGrade = runnerGradeService.refreshUserGrade(userId);
            userDTO.setRunnerGrade(correctedGrade != null ? correctedGrade.name() : "BEGINNER");
        } else {
            userDTO.setRunnerGrade("BEGINNER");
        }

        // Crew Info Logic (Primary Crew Only)
        // 대표 크루만 헤더와 사용자 정보에 표시
        var crewMembers = crewMemberRepository.findByUserIdAndIsPrimary(userAuth.getId(), true);
        if (!crewMembers.isEmpty()) {
            CrewMember member = crewMembers.get(0);
            if ("APPROVED".equals(member.getStatus())) {
                Long crewId = member.getCrewId();
                if (crewId != null) {
                    var crew = crewRepository.findById(crewId);
                    if (crew.isPresent()) {
                        userDTO.setCrewId(crew.get().getId());
                        userDTO.setCrewName(crew.get().getName());
                        userDTO.setCrewImage(crew.get().getImageUrl());
                    }
                }
            }
        }

        // Activity Area Status
        var activityArea = activityAreaRepository.findByUserId(userAuth.getId());
        userDTO.setActivityAreaRegistered(activityArea.isPresent());
        activityArea.ifPresent(area -> userDTO.setActivityAreaAddress(area.getAdminLevel2()));

        return userDTO;
    }
}
