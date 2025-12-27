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

        // Crew Info Logic
        var crewMember = crewMemberRepository.findByUserId(userAuth.getId());
        if (crewMember.isPresent()) {
            var crew = crewRepository.findById(crewMember.get().getCrewId());
            if (crew.isPresent()) {
                userDTO.setCrewId(crew.get().getId());
                userDTO.setCrewName(crew.get().getName());
                userDTO.setCrewImage(crew.get().getImageUrl());
            }
        }

        return userDTO;
    }
}
