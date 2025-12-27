package com.secondwind.controller;

import com.secondwind.dto.CrewMemberDTO;
import com.secondwind.entity.CrewMember;
import com.secondwind.repository.CrewMemberRepository;
import com.secondwind.repository.CrewRepository;
import com.secondwind.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/crew")
public class CrewMemberController {

    private final CrewMemberRepository crewMemberRepository;
    private final CrewRepository crewRepository;
    private final UserRepository userRepository;

    public CrewMemberController(CrewMemberRepository crewMemberRepository,
            CrewRepository crewRepository,
            UserRepository userRepository) {
        this.crewMemberRepository = crewMemberRepository;
        this.crewRepository = crewRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/{crewId}/members")
    public List<CrewMemberDTO> getCrewMembers(@PathVariable Long crewId) {
        List<CrewMember> members = crewMemberRepository.findByCrewId(crewId);

        return members.stream().map(member -> {
            var user = userRepository.findById(member.getUserId()).orElse(null);

            CrewMemberDTO dto = new CrewMemberDTO();
            dto.setId(member.getId());
            dto.setUserId(member.getUserId());
            dto.setRole(member.getRole());
            dto.setJoinedAt(member.getJoinedAt().toString());

            if (user != null) {
                dto.setNickname(user.getNickname());
                dto.setNicknameImage(user.getNicknameImage());
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @PostMapping("/{crewId}/join")
    @org.springframework.transaction.annotation.Transactional
    public CrewMemberDTO joinCrew(@PathVariable Long crewId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var userAuth = userRepository.findByEmail(email);

        if (userAuth == null) {
            throw new RuntimeException("User not found");
        }

        // Check if crew exists
        var crew = crewRepository.findById(crewId);
        if (crew.isEmpty()) {
            throw new RuntimeException("Crew not found");
        }

        // Check if already a member
        var existingMember = crewMemberRepository.findByCrewIdAndUserId(crewId, userAuth.getId());
        if (existingMember.isPresent()) {
            throw new RuntimeException("Already a member of this crew");
        }

        // Check if user is already in another crew
        var userCrew = crewMemberRepository.findByUserId(userAuth.getId());
        if (userCrew.isPresent()) {
            throw new RuntimeException("Already a member of another crew");
        }

        CrewMember member = new CrewMember();
        member.setCrewId(crewId);
        member.setUserId(userAuth.getId());
        member.setRole("member");

        CrewMember savedMember = crewMemberRepository.save(member);

        CrewMemberDTO response = new CrewMemberDTO();
        response.setId(savedMember.getId());
        response.setUserId(savedMember.getUserId());
        response.setRole(savedMember.getRole());
        response.setJoinedAt(savedMember.getJoinedAt().toString());
        response.setNickname(userAuth.getNickname());
        response.setNicknameImage(userAuth.getNicknameImage());

        return response;
    }

    @DeleteMapping("/{crewId}/leave")
    @org.springframework.transaction.annotation.Transactional
    public void leaveCrew(@PathVariable Long crewId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var userAuth = userRepository.findByEmail(email);

        if (userAuth == null) {
            throw new RuntimeException("User not found");
        }

        var member = crewMemberRepository.findByCrewIdAndUserId(crewId, userAuth.getId());
        if (member.isEmpty()) {
            throw new RuntimeException("Not a member of this crew");
        }

        // Captain cannot leave
        if ("captain".equals(member.get().getRole())) {
            throw new RuntimeException("Captain cannot leave the crew");
        }

        crewMemberRepository.delete(member.get());
    }
}
