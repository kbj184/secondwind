package com.secondwind.controller;

import com.secondwind.dto.CrewDTO;
import com.secondwind.entity.Crew;
import com.secondwind.entity.CrewMember;
import com.secondwind.repository.CrewRepository;
import com.secondwind.repository.CrewMemberRepository;
import com.secondwind.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/crew")
public class CrewController {

    private final CrewRepository crewRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final UserRepository userRepository;

    public CrewController(CrewRepository crewRepository,
            CrewMemberRepository crewMemberRepository,
            UserRepository userRepository) {
        this.crewRepository = crewRepository;
        this.crewMemberRepository = crewMemberRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public CrewDTO createCrew(@RequestBody CrewDTO crewDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var userAuth = userRepository.findByEmail(email);

        if (userAuth == null) {
            throw new RuntimeException("User not found");
        }

        // Check if user already has a crew
        var existingCrew = crewRepository.findByCaptainId(userAuth.getId());
        if (existingCrew.isPresent()) {
            throw new RuntimeException("User already has a crew");
        }

        Crew crew = new Crew();
        crew.setName(crewDTO.getName());
        crew.setDescription(crewDTO.getDescription());
        crew.setImageUrl(crewDTO.getImageUrl());
        crew.setCaptainId(userAuth.getId());

        Crew savedCrew = crewRepository.save(crew);

        // Add captain as a member
        CrewMember captainMember = new CrewMember();
        captainMember.setCrewId(savedCrew.getId());
        captainMember.setUserId(userAuth.getId());
        captainMember.setRole("captain");
        crewMemberRepository.save(captainMember);

        CrewDTO response = new CrewDTO();
        response.setId(savedCrew.getId());
        response.setName(savedCrew.getName());
        response.setDescription(savedCrew.getDescription());
        response.setImageUrl(savedCrew.getImageUrl());
        response.setCaptainId(savedCrew.getCaptainId());
        response.setCreatedAt(savedCrew.getCreatedAt().toString());

        return response;
    }

    @GetMapping("/my")
    public CrewDTO getMyCrew() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var userAuth = userRepository.findByEmail(email);

        if (userAuth == null) {
            throw new RuntimeException("User not found");
        }

        var crew = crewRepository.findByCaptainId(userAuth.getId());

        if (crew.isEmpty()) {
            return null;
        }

        Crew foundCrew = crew.get();
        CrewDTO response = new CrewDTO();
        response.setId(foundCrew.getId());
        response.setName(foundCrew.getName());
        response.setDescription(foundCrew.getDescription());
        response.setImageUrl(foundCrew.getImageUrl());
        response.setCaptainId(foundCrew.getCaptainId());
        response.setCreatedAt(foundCrew.getCreatedAt().toString());

        return response;
    }

    @GetMapping("/all")
    public java.util.List<CrewDTO> getAllCrews() {
        java.util.List<Crew> crews = crewRepository.findAll();

        return crews.stream().map(crew -> {
            CrewDTO dto = new CrewDTO();
            dto.setId(crew.getId());
            dto.setName(crew.getName());
            dto.setDescription(crew.getDescription());
            dto.setImageUrl(crew.getImageUrl());
            dto.setCaptainId(crew.getCaptainId());
            dto.setCreatedAt(crew.getCreatedAt().toString());
            return dto;
        }).collect(java.util.stream.Collectors.toList());
    }
}
