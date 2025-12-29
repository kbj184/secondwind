package com.secondwind.controller;

import com.secondwind.dto.UserDTO;
import com.secondwind.entity.UserAuth;
import com.secondwind.repository.UserRepository;
import com.secondwind.repository.CrewMemberRepository;
import com.secondwind.repository.CrewRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyController {

    private final UserRepository userRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final CrewRepository crewRepository;

    public MyController(UserRepository userRepository,
            CrewMemberRepository crewMemberRepository,
            CrewRepository crewRepository) {
        this.userRepository = userRepository;
        this.crewMemberRepository = crewMemberRepository;
        this.crewRepository = crewRepository;
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

        // Runner Grade
        if (userAuth.getRunnerGrade() != null) {
            userDTO.setRunnerGrade(userAuth.getRunnerGrade().name());
        } else {
            userDTO.setRunnerGrade("BEGINNER");
        }

        // Crew Info Logic
        var crewMember = crewMemberRepository.findByUserId(userAuth.getId());
        System.out.println("DEBUG: Finding crew for user ID " + userAuth.getId());

        if (crewMember.isPresent()) {
            System.out.println("DEBUG: Found crew member record. Crew ID: " + crewMember.get().getCrewId());
            var crew = crewRepository.findById(crewMember.get().getCrewId());
            if (crew.isPresent()) {
                System.out.println("DEBUG: Found crew details. Name: " + crew.get().getName());
                userDTO.setCrewId(crew.get().getId());
                userDTO.setCrewName(crew.get().getName());
                userDTO.setCrewImage(crew.get().getImageUrl());
            } else {
                System.out.println("DEBUG: Crew found but details missing for ID " + crewMember.get().getCrewId());
            }
        } else {
            System.out.println("DEBUG: No crew member record found for user.");
        }

        return userDTO;
    }
}
