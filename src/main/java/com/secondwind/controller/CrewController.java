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
import com.secondwind.repository.RunningSessionRepository;
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
    private final RunningSessionRepository runningSessionRepository;

    public CrewController(CrewRepository crewRepository,
            CrewMemberRepository crewMemberRepository,
            CrewActivityAreaRepository crewActivityAreaRepository,
            UserRepository userRepository,
            UserActivityAreaRepository userActivityAreaRepository,
            RunningSessionRepository runningSessionRepository) {
        this.crewRepository = crewRepository;
        this.crewMemberRepository = crewMemberRepository;
        this.crewActivityAreaRepository = crewActivityAreaRepository;
        this.userRepository = userRepository;
        this.userActivityAreaRepository = userActivityAreaRepository;
        this.runningSessionRepository = runningSessionRepository;
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
        captainMember.setStatus("APPROVED"); // Captain is always approved

        // Set as primary crew if this is the user's first crew
        long userCrewCount = crewMemberRepository.countByUserId(userAuth.getId());
        captainMember.setIsPrimary(userCrewCount == 0);

        // If setting as primary, unset existing primary crew
        if (captainMember.getIsPrimary()) {
            List<CrewMember> existingPrimaryList = crewMemberRepository.findByUserIdAndIsPrimary(userAuth.getId(),
                    true);
            for (CrewMember existingPrimary : existingPrimaryList) {
                existingPrimary.setIsPrimary(false);
                crewMemberRepository.save(existingPrimary);
            }
        }

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
    public List<CrewDTO> getAllCrews(
            @RequestParam(required = false) String adminLevel1,
            @RequestParam(required = false) String adminLevel2,
            @RequestParam(required = false) String adminLevel3) {

        List<Crew> crews = crewRepository.findAll();

        return crews.stream()
                .map(crew -> {
                    CrewDTO dto = new CrewDTO();
                    dto.setId(crew.getId());
                    dto.setName(crew.getName());
                    dto.setDescription(crew.getDescription());
                    dto.setImageUrl(crew.getImageUrl());
                    dto.setCaptainId(crew.getCaptainId());
                    dto.setJoinType(crew.getJoinType());
                    dto.setCreatedAt(crew.getCreatedAt().toString());
                    dto.setMemberCount(crewMemberRepository.countByCrewId(crew.getId()));

                    // 활동 지역 정보 추가 (첫 번째 활동 지역)
                    List<CrewActivityArea> activityAreas = crewActivityAreaRepository.findByCrewId(crew.getId());
                    if (!activityAreas.isEmpty()) {
                        CrewActivityArea area = activityAreas.get(0);
                        dto.setActivityAreaLevel1(area.getAdminLevel1());
                        dto.setActivityAreaLevel2(area.getAdminLevel2());
                        dto.setActivityAreaLevel3(area.getAdminLevel3());
                    }

                    // 크루원 총 이동거리 추가
                    Double totalDistance = runningSessionRepository.sumDistanceByCrewMembers(crew.getId());
                    dto.setTotalDistance(totalDistance != null ? totalDistance : 0.0);

                    return dto;
                })
                // 지역 필터링
                .filter(dto -> {
                    if (adminLevel1 != null && !adminLevel1.equals(dto.getActivityAreaLevel1())) {
                        return false;
                    }
                    if (adminLevel2 != null && !adminLevel2.equals(dto.getActivityAreaLevel2())) {
                        return false;
                    }
                    if (adminLevel3 != null && !adminLevel3.equals(dto.getActivityAreaLevel3())) {
                        return false;
                    }
                    return true;
                })
                .collect(java.util.stream.Collectors.toList());
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
                return getAllCrews(null, null, null);
            }
        }

        // 1. DB에서 radiusKm 이내 크루 조회
        List<Crew> nearbyCrews = crewRepository.findNearbyCrews(latitude, longitude, radiusKm);

        // 2. 근처에 크루가 없으면 전체 크루 반환
        if (nearbyCrews.isEmpty()) {
            return getAllCrews(null, null, null);
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

            // 활동 지역 정보 추가 (첫 번째 활동 지역)
            List<CrewActivityArea> activityAreas = crewActivityAreaRepository.findByCrewId(crew.getId());
            if (!activityAreas.isEmpty()) {
                CrewActivityArea area = activityAreas.get(0);
                dto.setActivityAreaLevel1(area.getAdminLevel1());
                dto.setActivityAreaLevel2(area.getAdminLevel2());
                dto.setActivityAreaLevel3(area.getAdminLevel3());
            }

            // 크루원 총 이동거리 추가
            Double totalDistance = runningSessionRepository.sumDistanceByCrewMembers(crew.getId());
            dto.setTotalDistance(totalDistance != null ? totalDistance : 0.0);

            return dto;
        }).collect(java.util.stream.Collectors.toList());
    }

    // 크루 통계 조회
    @GetMapping("/{crewId}/stats")
    public java.util.Map<String, Object> getCrewStats(@PathVariable Long crewId) {
        // 크루원 수 (승인된 멤버만)
        long memberCount = crewMemberRepository.countByCrewId(crewId);

        // 크루원 총 이동거리
        Double totalDistance = runningSessionRepository.sumDistanceByCrewMembers(crewId);

        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("memberCount", memberCount);
        stats.put("totalDistance", totalDistance != null ? totalDistance : 0.0);

        return stats;
    }
}
