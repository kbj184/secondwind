package com.secondwind.controller;

import com.secondwind.dto.CrewCourseDTO;
import com.secondwind.entity.CrewCourse;
import com.secondwind.entity.CrewMember;
import com.secondwind.entity.UserAuth;
import com.secondwind.repository.CrewCourseRepository;
import com.secondwind.repository.CrewMemberRepository;
import com.secondwind.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/crew/{crewId}/courses")
@RequiredArgsConstructor
public class CrewCourseController {

    private final CrewCourseRepository crewCourseRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<CrewCourseDTO>> getCourses(@PathVariable Long crewId) {
        List<CrewCourse> courses = crewCourseRepository.findByCrewIdOrderByCreatedAtDesc(crewId);

        List<CrewCourseDTO> courseDTOs = courses.stream().map(course -> {
            CrewCourseDTO dto = new CrewCourseDTO();
            dto.setId(course.getId());
            dto.setCrewId(course.getCrewId());
            dto.setUserId(course.getUserId());
            dto.setName(course.getName());
            dto.setDescription(course.getDescription());
            dto.setDistance(course.getDistance());
            dto.setRouteData(course.getRouteData());
            dto.setMapThumbnailUrl(course.getMapThumbnailUrl());
            dto.setCreatedAt(course.getCreatedAt());

            // Get creator info
            UserAuth creator = userRepository.findById(course.getUserId()).orElse(null);
            if (creator != null) {
                dto.setCreatorNickname(creator.getNickname());
                dto.setCreatorProfileImage(null); // UserAuth doesn't have profileImage
            }

            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(courseDTOs);
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<CrewCourseDTO> getCourse(
            @PathVariable Long crewId,
            @PathVariable Long courseId) {

        CrewCourse course = crewCourseRepository.findByIdAndCrewId(courseId, crewId)
                .orElse(null);

        if (course == null) {
            return ResponseEntity.notFound().build();
        }

        CrewCourseDTO dto = new CrewCourseDTO();
        dto.setId(course.getId());
        dto.setCrewId(course.getCrewId());
        dto.setUserId(course.getUserId());
        dto.setName(course.getName());
        dto.setDescription(course.getDescription());
        dto.setDistance(course.getDistance());
        dto.setRouteData(course.getRouteData());
        dto.setMapThumbnailUrl(course.getMapThumbnailUrl());
        dto.setCreatedAt(course.getCreatedAt());

        // Get creator info
        UserAuth creator = userRepository.findById(course.getUserId()).orElse(null);
        if (creator != null) {
            dto.setCreatorNickname(creator.getNickname());
            dto.setCreatorProfileImage(null);
        }

        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<?> createCourse(
            @PathVariable Long crewId,
            @RequestBody CrewCourseDTO courseDTO,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401).body("인증이 필요합니다.");
        }

        String email = authentication.getName();
        UserAuth user = userRepository.findByEmail(email);

        if (user == null) {
            return ResponseEntity.status(401).body("사용자를 찾을 수 없습니다.");
        }

        // Check if user is a member of the crew
        CrewMember member = crewMemberRepository.findByCrewIdAndUserId(crewId, user.getId()).orElse(null);
        if (member == null || !"APPROVED".equals(member.getStatus())) {
            return ResponseEntity.status(403).body("크루 멤버만 코스를 등록할 수 있습니다.");
        }

        CrewCourse course = new CrewCourse();
        course.setCrewId(crewId);
        course.setUserId(user.getId());
        course.setName(courseDTO.getName());
        course.setDescription(courseDTO.getDescription());
        course.setDistance(courseDTO.getDistance());
        course.setRouteData(courseDTO.getRouteData());
        course.setMapThumbnailUrl(courseDTO.getMapThumbnailUrl());

        CrewCourse saved = crewCourseRepository.save(course);

        CrewCourseDTO responseDTO = new CrewCourseDTO();
        responseDTO.setId(saved.getId());
        responseDTO.setCrewId(saved.getCrewId());
        responseDTO.setUserId(saved.getUserId());
        responseDTO.setName(saved.getName());
        responseDTO.setDescription(saved.getDescription());
        responseDTO.setDistance(saved.getDistance());
        responseDTO.setRouteData(saved.getRouteData());
        responseDTO.setMapThumbnailUrl(saved.getMapThumbnailUrl());
        responseDTO.setCreatedAt(saved.getCreatedAt());
        responseDTO.setCreatorNickname(user.getNickname());
        responseDTO.setCreatorProfileImage(null);

        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{courseId}")
    @Transactional
    public ResponseEntity<?> deleteCourse(
            @PathVariable Long crewId,
            @PathVariable Long courseId,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401).body("인증이 필요합니다.");
        }

        String email = authentication.getName();
        UserAuth user = userRepository.findByEmail(email);

        if (user == null) {
            return ResponseEntity.status(401).body("사용자를 찾을 수 없습니다.");
        }

        CrewCourse course = crewCourseRepository.findByIdAndCrewId(courseId, crewId)
                .orElse(null);

        if (course == null) {
            return ResponseEntity.notFound().build();
        }

        // Only creator can delete
        if (!course.getUserId().equals(user.getId())) {
            return ResponseEntity.status(403).body("코스 작성자만 삭제할 수 있습니다.");
        }

        crewCourseRepository.deleteById(courseId);

        return ResponseEntity.ok().build();
    }
}
