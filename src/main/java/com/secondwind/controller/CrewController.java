package com.secondwind.controller;

import com.secondwind.dto.CrewDTO;
import com.secondwind.dto.ActivityAreaDTO;
import com.secondwind.entity.Crew;
import com.secondwind.entity.CrewActivityArea;
import com.secondwind.entity.CrewMember;
import com.secondwind.entity.UserAuth;
import com.secondwind.repository.CrewActivityAreaRepository;
import com.secondwind.repository.CrewMemberRepository;
import com.secondwind.repository.CrewRepository;
import com.secondwind.repository.RunningSessionRepository;
import com.secondwind.repository.UserActivityAreaRepository;
import com.secondwind.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @GetMapping("/check-name")
    public java.util.Map<String, Object> checkCrewName(@RequestParam String name) {
        boolean exists = crewRepository.existsByName(name);

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("available", !exists);
        response.put("message", exists
                ? "이미 사용 중인 크루 이름입니다."
                : "사용 가능한 크루 이름입니다.");

        return response;
    }

    @PutMapping("/{crewId}")
    @org.springframework.transaction.annotation.Transactional
    public CrewDTO updateCrew(@PathVariable Long crewId, @RequestBody CrewDTO crewDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var userAuth = userRepository.findByEmail(email);

        if (userAuth == null) {
            throw new RuntimeException("User not found");
        }

        Crew crew = crewRepository.findById(crewId)
                .orElseThrow(() -> new RuntimeException("Crew not found"));

        // 권한 체크: 크루장만 수정 가능
        if (!crew.getCaptainId().equals(userAuth.getId())) {
            throw new RuntimeException("Only captain can update crew");
        }

        // 크루 이름 변경 시 중복 체크 및 제한 체크
        if (crewDTO.getName() != null && !crewDTO.getName().equals(crew.getName())) {
            // 1. 중복 체크
            List<Crew> existingCrews = crewRepository.findByName(crewDTO.getName());

            // 다른 크루가 같은 이름을 쓰고 있는지 확인
            boolean isDuplicate = existingCrews.stream()
                    .anyMatch(c -> !c.getId().equals(crewId));

            if (isDuplicate) {
                throw new RuntimeException("이미 사용 중인 크루 이름입니다.");
            }

            // 2. 변경 제한 체크
            boolean isWithin7Days = java.time.temporal.ChronoUnit.DAYS.between(
                    crew.getCreatedAt(), LocalDateTime.now()) <= 7;

            if (!isWithin7Days) {
                // 7일 이후: 마지막 변경으로부터 30일 체크
                if (crew.getLastNameChangeDate() != null) {
                    long daysSinceLastChange = java.time.temporal.ChronoUnit.DAYS.between(
                            crew.getLastNameChangeDate(), LocalDateTime.now());

                    if (daysSinceLastChange < 30) {
                        throw new RuntimeException(
                                "크루 이름은 30일에 1회만 변경 가능합니다. " +
                                        (30 - daysSinceLastChange) + "일 후 변경 가능합니다.");
                    }
                }
            }

            // 3. 이름 변경 실행
            String oldName = crew.getName();
            crew.setName(crewDTO.getName());
            crew.setNameChangeCount((crew.getNameChangeCount() != null ? crew.getNameChangeCount() : 0) + 1);
            crew.setLastNameChangeDate(LocalDateTime.now());

            // 4. 모든 멤버에게 알림 (TODO: FCM 서비스 추가 시 구현)
            // notifyNameChange(crewId, oldName, crewDTO.getName());
        }

        // 기타 필드 업데이트
        if (crewDTO.getDescription() != null)
            crew.setDescription(crewDTO.getDescription());
        if (crewDTO.getImageUrl() != null)
            crew.setImageUrl(crewDTO.getImageUrl());
        if (crewDTO.getJoinType() != null)
            crew.setJoinType(crewDTO.getJoinType());

        crewRepository.save(crew);

        // 활동 지역 업데이트 (기존 지역 삭제 후 새로 저장)
        if (crewDTO.getActivityAreas() != null) {
            crewActivityAreaRepository.deleteByCrewId(crewId);

            if (!crewDTO.getActivityAreas().isEmpty()) {
                for (ActivityAreaDTO areaDTO : crewDTO.getActivityAreas()) {
                    CrewActivityArea area = new CrewActivityArea();
                    area.setCrewId(crew.getId());
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
        }

        CrewDTO response = new CrewDTO();
        response.setId(crew.getId());
        response.setName(crew.getName());
        response.setDescription(crew.getDescription());
        response.setImageUrl(crew.getImageUrl());
        response.setCaptainId(crew.getCaptainId());
        response.setJoinType(crew.getJoinType());
        response.setCreatedAt(crew.getCreatedAt().toString());
        response.setMemberCount(crewMemberRepository.countByCrewId(crew.getId()));

        return response;
    }

    @PostMapping
    @org.springframework.transaction.annotation.Transactional
    public CrewDTO createCrew(@RequestBody CrewDTO crewDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var userAuth = userRepository.findByEmail(email);

        if (userAuth == null) {
            throw new RuntimeException("User not found");
        }

        // 크루 이름 중복 체크
        boolean exists = crewRepository.existsByName(crewDTO.getName());
        if (exists) {
            throw new RuntimeException("이미 사용 중인 크루 이름입니다.");
        }

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
                    primaryCrew = convertToDTO(crew.get());
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
                        return convertToDTO(crew.get());
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

                    // 활동 지역 정보 추가
                    List<CrewActivityArea> activityAreas = crewActivityAreaRepository.findByCrewId(crew.getId());

                    List<ActivityAreaDTO> areaDTOs = activityAreas.stream().map(area -> {
                        ActivityAreaDTO areaDTO = new ActivityAreaDTO();
                        areaDTO.setCountryCode(area.getCountryCode());
                        areaDTO.setCountryName(area.getCountryName());
                        areaDTO.setAdminLevel1(area.getAdminLevel1());
                        areaDTO.setAdminLevel2(area.getAdminLevel2());
                        areaDTO.setAdminLevel3(area.getAdminLevel3());
                        areaDTO.setAdminLevelFull(area.getAdminLevelFull());
                        areaDTO.setLatitude(area.getLatitude());
                        areaDTO.setLongitude(area.getLongitude());
                        return areaDTO;
                    }).collect(java.util.stream.Collectors.toList());
                    dto.setActivityAreas(areaDTOs);

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
        return nearbyCrews.stream().map(this::convertToDTO).collect(java.util.stream.Collectors.toList());
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

    // Helper method to convert Crew entity to DTO with activity area info
    private CrewDTO convertToDTO(Crew crew) {
        System.out.println("DEBUG: convertToDTO called for crew ID: " + crew.getId());

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
        System.out.println("DEBUG: Found " + activityAreas.size() + " activity areas for crew " + crew.getId());

        if (!activityAreas.isEmpty()) {
            CrewActivityArea area = activityAreas.get(0);
            System.out.println("DEBUG: Activity area - lat: " + area.getLatitude() + ", lng: " + area.getLongitude()
                    + ", address: " + area.getAdminLevelFull());

            dto.setActivityAreaLevel1(area.getAdminLevel1());
            dto.setActivityAreaLevel2(area.getAdminLevel2());
            dto.setActivityAreaLevel3(area.getAdminLevel3());
            dto.setActivityAreaLatitude(area.getLatitude());
            dto.setActivityAreaLongitude(area.getLongitude());
            dto.setActivityAreaAddress(area.getAdminLevelFull());
        } else {
            System.out.println("DEBUG: No activity areas found for crew " + crew.getId());
        }

        // 크루원 총 이동거리 추가
        Double totalDistance = runningSessionRepository.sumDistanceByCrewMembers(crew.getId());
        dto.setTotalDistance(totalDistance != null ? totalDistance : 0.0);

        return dto;
    }
}
