package com.secondwind.controller;

import com.secondwind.dto.CrewDTO;
import com.secondwind.dto.ActivityAreaDTO;
import com.secondwind.entity.Crew;
import com.secondwind.entity.CrewMember;
import com.secondwind.entity.CrewActivityArea;
import com.secondwind.repository.CrewRepository;
import com.secondwind.repository.CrewMemberRepository;
import com.secondwind.repository.CrewActivityAreaRepository;
import com.secondwind.repository.UserRepository;
import com.secondwind.repository.UserActivityAreaRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/crew")
public class CrewController {

    private final CrewRepository crewRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final CrewActivityAreaRepository crewActivityAreaRepository;
    private final UserRepository userRepository;
    private final UserActivityAreaRepository userActivityAreaRepository;

    public CrewController(CrewRepository crewRepository,
            CrewMemberRepository crewMemberRepository,
            CrewActivityAreaRepository crewActivityAreaRepository,
            UserRepository userRepository,
            UserActivityAreaRepository userActivityAreaRepository) {
        this.crewRepository = crewRepository;
        this.crewMemberRepository = crewMemberRepository;
        this.crewActivityAreaRepository = crewActivityAreaRepository;
        this.userRepository = userRepository;
        this.userActivityAreaRepository = userActivityAreaRepository;
    }

    @PostMapping
    @org.springframework.transaction.annotation.Transactional
    public CrewDTO createCrew(@RequestBody CrewDTO crewDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var userAuth = userRepository.findByEmail(email);

        if (userAuth == null) {
            throw new RuntimeException("User not found");
        }

        // 중복 크루 생성 허용 - 제한 제거

        Crew crew = new Crew();
        crew.setName(crewDTO.getName());
        crew.setDescription(crewDTO.getDescription());
        crew.setImageUrl(crewDTO.getImageUrl());
        crew.setCaptainId(userAuth.getId());
        crew.setJoinType(crewDTO.getJoinType() != null ? crewDTO.getJoinType() : "AUTO");

        Crew savedCrew = crewRepository.save(crew);

        // Save activity areas
        if (crewDTO.getActivityAreas() != null && !crewDTO.getActivityAreas().isEmpty()) {
            for (ActivityAreaDTO areaDTO : crewDTO.getActivityAreas()) {
                CrewActivityArea area = new CrewActivityArea();
                area.setCrewId(savedCrew.getId());
                area.setCountryCode(areaDTO.getCountryCode());
                area.setCountryName(areaDTO.getCountryName());
                area.setAdminLevel1(areaDTO.getAdminLevel1());
                area.setAdminLevel2(areaDTO.getAdminLevel2());
                area.setAdminLevel3(areaDTO.getAdminLevel3());
                area.setAdminLevelFull(areaDTO.getAdminLevelFull());
                area.setLatitude(areaDTO.getLatitude());
                area.setLongitude(areaDTO.getLongitude());
                crewActivityAreaRepository.save(area);
            }
        }

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
        response.setJoinType(savedCrew.getJoinType());
        response.setCreatedAt(savedCrew.getCreatedAt().toString());
        response.setMemberCount(1L);

        return response;
    }

    @GetMapping("/my-crews")
    public java.util.Map<String, Object> getMyCrews() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var userAuth = userRepository.findByEmail(email);

        if (userAuth == null) {
            throw new RuntimeException("User not found");
        }

        Long userId = userAuth.getId();

        // Get primary crew
        List<com.secondwind.entity.CrewMember> primaryMembers = crewMemberRepository.findByUserIdAndIsPrimary(userId,
                true);
        CrewDTO primaryCrew = null;
        if (!primaryMembers.isEmpty()) {
            var member = primaryMembers.get(0);
            if ("APPROVED".equals(member.getStatus())) {
                var crew = crewRepository.findById(member.getCrewId());
                if (crew.isPresent()) {
                    Crew c = crew.get();
                    primaryCrew = new CrewDTO();
                    primaryCrew.setId(c.getId());
                    primaryCrew.setName(c.getName());
                    primaryCrew.setDescription(c.getDescription());
                    primaryCrew.setImageUrl(c.getImageUrl());
                    primaryCrew.setCaptainId(c.getCaptainId());
                    primaryCrew.setJoinType(c.getJoinType());
                    primaryCrew.setCreatedAt(c.getCreatedAt().toString());
                    primaryCrew.setMemberCount(crewMemberRepository.countByCrewId(c.getId()));
                }
            }
        }

        // Get secondary crews
        List<CrewDTO> secondaryCrews = crewMemberRepository.findByUserIdAndIsPrimary(userId, false)
                .stream()
                .filter(member -> "APPROVED".equals(member.getStatus()))
                .map(member -> {
                    var crew = crewRepository.findById(member.getCrewId());
                    if (crew.isPresent()) {
                        Crew c = crew.get();
                        CrewDTO dto = new CrewDTO();
                        dto.setId(c.getId());
                        dto.setName(c.getName());
                        dto.setDescription(c.getDescription());
                        dto.setImageUrl(c.getImageUrl());
                        dto.setCaptainId(c.getCaptainId());
                        dto.setJoinType(c.getJoinType());
                        dto.setCreatedAt(c.getCreatedAt().toString());
                        dto.setMemberCount(crewMemberRepository.countByCrewId(c.getId()));
                        return dto;
                    }
                    return null;
                })
                .filter(dto -> dto != null)
                .collect(java.util.stream.Collectors.toList());

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("primaryCrew", primaryCrew);
        result.put("secondaryCrews", secondaryCrews);

        return result;
    }

    // Keep old endpoint for backward compatibility
    @GetMapping("/my")
    public CrewDTO getMyCrew() {
        var result = getMyCrews();
        return (CrewDTO) result.get("primaryCrew");
    }

    @GetMapping("/all")
    public List<CrewDTO> getAllCrews() {
        List<Crew> crews = crewRepository.findAll();

        return crews.stream().map(crew -> {
            CrewDTO dto = new CrewDTO();
            dto.setId(crew.getId());
            dto.setName(crew.getName());
            dto.setDescription(crew.getDescription());
            dto.setImageUrl(crew.getImageUrl());
            dto.setCaptainId(crew.getCaptainId());
            dto.setJoinType(crew.getJoinType());
            dto.setCreatedAt(crew.getCreatedAt().toString());
            dto.setMemberCount(crewMemberRepository.countByCrewId(crew.getId()));
            return dto;
        }).collect(java.util.stream.Collectors.toList());
    }

    @GetMapping("/nearby")
    public List<CrewDTO> getNearbyCrews(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(defaultValue = "3") double radiusKm) {

        // 사용자 활동 지역 정보가 없으면 전체 크루 반환
        if (latitude == null || longitude == null) {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            var userAuth = userRepository.findByEmail(email);

            if (userAuth != null) {
                var userArea = userActivityAreaRepository.findByUserId(userAuth.getId());
                if (userArea.isPresent()) {
                    latitude = userArea.get().getLatitude();
                    longitude = userArea.get().getLongitude();
                }
            }

            // 여전히 좌표가 없으면 전체 크루 반환
            if (latitude == null || longitude == null) {
                return getAllCrews();
            }
        }

        // 1. DB에서 radiusKm 이내 크루 조회
        List<Crew> nearbyCrews = crewRepository.findNearbyCrews(latitude, longitude, radiusKm);

        // 2. 근처에 크루가 없으면 전체 크루 반환
        if (nearbyCrews.isEmpty()) {
            return getAllCrews();
        }

        // 3. DTO로 변환하여 반환
        return nearbyCrews.stream().map(crew -> {
            CrewDTO dto = new CrewDTO();
            dto.setId(crew.getId());
            dto.setName(crew.getName());
            dto.setDescription(crew.getDescription());
            dto.setImageUrl(crew.getImageUrl());
            dto.setCaptainId(crew.getCaptainId());
            dto.setJoinType(crew.getJoinType());
            dto.setCreatedAt(crew.getCreatedAt().toString());
            dto.setMemberCount(crewMemberRepository.countByCrewId(crew.getId()));
            return dto;
        }).collect(java.util.stream.Collectors.toList());
    }
}
