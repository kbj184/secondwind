package com.secondwind.controller;

import com.secondwind.dto.CrewMemberDTO;
import com.secondwind.entity.Crew;
import com.secondwind.entity.CrewMember;
import com.secondwind.repository.CrewMemberRepository;
import com.secondwind.repository.CrewRepository;
import com.secondwind.repository.UserRepository;
import com.secondwind.service.FcmService;
import com.secondwind.service.FollowService;
import com.secondwind.entity.NotificationType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/crew")
public class CrewMemberController {

    private final CrewMemberRepository crewMemberRepository;
    private final CrewRepository crewRepository;
    private final UserRepository userRepository;
    private final FcmService fcmService;
    private final FollowService followService;

    public CrewMemberController(CrewMemberRepository crewMemberRepository,
            CrewRepository crewRepository,
            UserRepository userRepository,
            FcmService fcmService,
            FollowService followService) {
        this.crewMemberRepository = crewMemberRepository;
        this.crewRepository = crewRepository;
        this.userRepository = userRepository;
        this.fcmService = fcmService;
        this.followService = followService;
    }

    @GetMapping("/{crewId}/members")
    public List<CrewMemberDTO> getCrewMembers(@PathVariable Long crewId) {
        List<CrewMember> members = crewMemberRepository.findByCrewId(crewId);

        return members.stream().map(member -> {
            Long userIdObj = member.getUserId();
            var user = userIdObj != null ? userRepository.findById((long) userIdObj).orElse(null) : null;

            CrewMemberDTO dto = new CrewMemberDTO();
            dto.setId(member.getId());
            dto.setUserId(member.getUserId());
            dto.setRole(member.getRole());
            dto.setStatus(member.getStatus());
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
        if (crewId == null) {
            throw new RuntimeException("Invalid Crew ID");
        }
        var crew = crewRepository.findById(crewId);
        if (crew.isEmpty()) {
            throw new RuntimeException("Crew not found");
        }

        // Check if already a member of THIS crew
        Long userId = userAuth.getId();
        if (userId == null)
            throw new RuntimeException("User ID not found");

        var existingMember = crewMemberRepository.findByCrewIdAndUserId((long) crewId, userId);
        if (existingMember.isPresent()) {
            throw new RuntimeException("Already a member of this crew");
        }

        // 중복 크루 가입 허용 - 다른 크루 체크 제거

        // Get crew to check join type
        Crew crewEntity = crew.get();
        String joinType = crewEntity.getJoinType();

        CrewMember member = new CrewMember();
        member.setCrewId(crewId);
        member.setUserId(userId);
        member.setRole("member");

        // Set status based on join type
        if ("AUTO".equals(joinType)) {
            member.setStatus("APPROVED");
        } else {
            member.setStatus("PENDING");
        }

        // Set primary crew logic
        // First crew becomes primary automatically
        long userCrewCount = crewMemberRepository.countByUserId(userId);
        boolean isFirstCrew = userCrewCount == 0;
        member.setIsPrimary(isFirstCrew);

        // If setting as primary, unset existing primary crew
        if (member.getIsPrimary()) {
            List<CrewMember> existingPrimaryList = crewMemberRepository.findByUserIdAndIsPrimary(userId, true);
            for (CrewMember existingPrimary : existingPrimaryList) {
                existingPrimary.setIsPrimary(false);
                crewMemberRepository.save(existingPrimary);
            }
        }

        CrewMember savedMember = crewMemberRepository.save(member);

        CrewMemberDTO response = new CrewMemberDTO();
        response.setId(savedMember.getId());
        response.setUserId(savedMember.getUserId());
        response.setRole(savedMember.getRole());
        response.setStatus(savedMember.getStatus());
        response.setIsPrimary(savedMember.getIsPrimary());
        response.setJoinedAt(savedMember.getJoinedAt().toString());
        response.setNickname(userAuth.getNickname());
        response.setNicknameImage(userAuth.getNicknameImage());

        // Send notification to Captain if status is PENDING
        if ("PENDING".equals(savedMember.getStatus())) {
            try {
                fcmService.sendToUser(
                        crewEntity.getCaptainId(),
                        "크루 가입 요청",
                        userAuth.getNickname() + "님이 " + crewEntity.getName() + " 크루 가입을 신청했습니다.",
                        NotificationType.CREW_JOIN_REQUEST,
                        Map.of("crewId", crewId.toString()));
            } catch (Exception e) {
                System.err.println("Failed to send join request notification: " + e.getMessage());
                // Don't fail the request just because notification failed
            }
        }

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

        Long currentUserId = userAuth.getId();
        if (currentUserId == null)
            throw new RuntimeException("User ID not found");

        CrewMember memberEntity = crewMemberRepository.findByCrewIdAndUserId((long) crewId, currentUserId)
                .orElseThrow(() -> new RuntimeException("Not a member of this crew"));

        // Captain cannot leave
        if ("captain".equals(memberEntity.getRole())) {
            throw new RuntimeException("Captain cannot leave the crew");
        }

        crewMemberRepository.delete(memberEntity);
    }

    @PostMapping("/{crewId}/members/{userId}/approve")
    @org.springframework.transaction.annotation.Transactional
    public CrewMemberDTO approveMember(@PathVariable Long crewId, @PathVariable Long userId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var userAuth = userRepository.findByEmail(email);

        if (userAuth == null) {
            throw new RuntimeException("User not found");
        }

        // Check if user is the captain
        var crew = crewRepository.findById(crewId)
                .orElseThrow(() -> new RuntimeException("Crew not found"));

        if (!crew.getCaptainId().equals(userAuth.getId())) {
            throw new RuntimeException("Only captain can approve members");
        }

        // Find and approve the member
        CrewMember member = crewMemberRepository.findByCrewIdAndUserId(crewId, userId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        if (!member.getCrewId().equals(crewId)) {
            throw new RuntimeException("Member does not belong to this crew");
        }

        member.setStatus("APPROVED");
        CrewMember updatedMember = crewMemberRepository.save(member);

        // AUTO-FOLLOW: Create mutual follows between new member and all existing
        // approved members
        List<CrewMember> approvedMembers = crewMemberRepository.findByCrewId(crewId).stream()
                .filter(m -> "APPROVED".equals(m.getStatus()) && !m.getUserId().equals(userId))
                .collect(Collectors.toList());

        for (CrewMember existingMember : approvedMembers) {
            try {
                // New member follows existing member (silent)
                followService.followUser(userId, existingMember.getUserId(), true);
                // Existing member follows new member (silent)
                followService.followUser(existingMember.getUserId(), userId, true);
            } catch (Exception e) {
                System.err.println("Failed to create auto-follow: " + e.getMessage());
                // Continue even if auto-follow fails
            }
        }

        // Get user info for response
        var memberUser = userRepository.findById(updatedMember.getUserId()).orElse(null);

        CrewMemberDTO response = new CrewMemberDTO();
        response.setId(updatedMember.getId());
        response.setUserId(updatedMember.getUserId());
        response.setRole(updatedMember.getRole());
        response.setStatus(updatedMember.getStatus());
        response.setJoinedAt(updatedMember.getJoinedAt().toString());
        if (memberUser != null) {
            response.setNickname(memberUser.getNickname());
            response.setNicknameImage(memberUser.getNicknameImage());
        }

        // Send FCM notification
        try {
            Long targetUserId = updatedMember.getUserId();
            fcmService.sendToUser(
                    targetUserId,
                    "크루 가입 승인",
                    crew.getName() + " 크루에 가입되었습니다!",
                    NotificationType.CREW_JOIN_APPROVED,
                    Map.of("crewId", crewId.toString()));
        } catch (Exception e) {
            System.err.println("Failed to send approval notification: " + e.getMessage());
            e.printStackTrace();
        }

        return response;
    }

    @PostMapping("/{crewId}/members/{userId}/reject")
    @org.springframework.transaction.annotation.Transactional
    public void rejectMember(@PathVariable Long crewId, @PathVariable Long userId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var userAuth = userRepository.findByEmail(email);

        if (userAuth == null) {
            throw new RuntimeException("User not found");
        }

        // Check if user is the captain
        var crew = crewRepository.findById(crewId)
                .orElseThrow(() -> new RuntimeException("Crew not found"));

        if (!crew.getCaptainId().equals(userAuth.getId())) {
            throw new RuntimeException("Only captain can reject members");
        }

        // Find and delete the member (or set status to REJECTED)
        CrewMember member = crewMemberRepository.findByCrewIdAndUserId(crewId, userId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        if (!member.getCrewId().equals(crewId)) {
            throw new RuntimeException("Member does not belong to this crew");
        }

        // Option 1: Delete the member
        crewMemberRepository.delete(member);

        // Send FCM notification
        try {
            fcmService.sendToUser(
                    userId,
                    "크루 가입 거절",
                    crew.getName() + " 크루 가입이 거절되었습니다.",
                    NotificationType.CREW_JOIN_REJECTED,
                    Map.of());
        } catch (Exception e) {
            System.err.println("Failed to send rejection notification: " + e.getMessage());
        }

        // Option 2: Set status to REJECTED (uncomment if you prefer this)
        // member.setStatus("REJECTED");
        // crewMemberRepository.save(member);
    }

    @PutMapping("/{crewId}/set-primary")
    @org.springframework.transaction.annotation.Transactional
    public CrewMemberDTO setPrimaryCrew(@PathVariable Long crewId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var userAuth = userRepository.findByEmail(email);

        if (userAuth == null) {
            throw new RuntimeException("User not found");
        }

        Long userId = userAuth.getId();
        if (userId == null) {
            throw new RuntimeException("User ID not found");
        }

        // Check if user is a member of this crew
        CrewMember member = crewMemberRepository.findByCrewIdAndUserId(crewId, userId)
                .orElseThrow(() -> new RuntimeException("Not a member of this crew"));

        // Only approved members can set primary crew
        if (!"APPROVED".equals(member.getStatus())) {
            throw new RuntimeException("Only approved members can set primary crew");
        }

        // Unset existing primary crew
        List<CrewMember> existingPrimaryList = crewMemberRepository.findByUserIdAndIsPrimary(userId, true);
        for (CrewMember existingPrimary : existingPrimaryList) {
            existingPrimary.setIsPrimary(false);
            crewMemberRepository.save(existingPrimary);
        }

        // Set new primary crew
        member.setIsPrimary(true);
        CrewMember updatedMember = crewMemberRepository.save(member);

        // Get user info for response
        var memberUser = userRepository.findById(updatedMember.getUserId()).orElse(null);

        CrewMemberDTO response = new CrewMemberDTO();
        response.setId(updatedMember.getId());
        response.setUserId(updatedMember.getUserId());
        response.setRole(updatedMember.getRole());
        response.setStatus(updatedMember.getStatus());
        response.setIsPrimary(updatedMember.getIsPrimary());
        response.setJoinedAt(updatedMember.getJoinedAt().toString());
        if (memberUser != null) {
            response.setNickname(memberUser.getNickname());
            response.setNicknameImage(memberUser.getNicknameImage());
        }

        return response;
    }

    @PutMapping("/{crewId}/members/{memberId}/role")
    @org.springframework.transaction.annotation.Transactional
    public CrewMemberDTO updateMemberRole(@PathVariable Long crewId, @PathVariable Long memberId,
            @RequestBody Map<String, String> request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var userAuth = userRepository.findByEmail(email);

        if (userAuth == null) {
            throw new RuntimeException("User not found");
        }

        // Check if requester is the captain
        var crew = crewRepository.findById(crewId)
                .orElseThrow(() -> new RuntimeException("Crew not found"));

        if (!crew.getCaptainId().equals(userAuth.getId())) {
            throw new RuntimeException("Only captain can update member roles");
        }

        // Find the member
        CrewMember member = crewMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        if (!member.getCrewId().equals(crewId)) {
            throw new RuntimeException("Member does not belong to this crew");
        }

        String newRole = request.get("role");
        if (newRole == null || (!newRole.equals("member") && !newRole.equals("vice_captain"))) {
            throw new RuntimeException("Invalid role");
        }

        // Captain role cannot be changed this way (needs ownership transfer)
        if ("captain".equals(member.getRole())) {
            throw new RuntimeException("Cannot change captain's role directly");
        }

        member.setRole(newRole);
        CrewMember updatedMember = crewMemberRepository.save(member);

        // Prepare response
        var memberUser = userRepository.findById(updatedMember.getUserId()).orElse(null);
        CrewMemberDTO response = new CrewMemberDTO();
        response.setId(updatedMember.getId());
        response.setUserId(updatedMember.getUserId());
        response.setRole(updatedMember.getRole());
        response.setStatus(updatedMember.getStatus());
        response.setJoinedAt(updatedMember.getJoinedAt().toString());
        if (memberUser != null) {
            response.setNickname(memberUser.getNickname());
            response.setNicknameImage(memberUser.getNicknameImage());
        }

        // Notification
        try {
            String roleName = "vice_captain".equals(newRole) ? "부크루장" : "일반 멤버";
            fcmService.sendToUser(
                    updatedMember.getUserId(),
                    "크루 등급 변경",
                    crew.getName() + " 크루에서 " + roleName + "(으)로 변경되었습니다.",
                    NotificationType.CREW_JOIN_APPROVED, // Reusing appropriate type or add new one
                    Map.of("crewId", crewId.toString()));
        } catch (Exception e) {
            System.err.println("Failed to send role update notification: " + e.getMessage());
        }

        return response;
    }

    @DeleteMapping("/{crewId}/members/{memberId}/kick")
    @org.springframework.transaction.annotation.Transactional
    public void kickMember(@PathVariable Long crewId, @PathVariable Long memberId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var userAuth = userRepository.findByEmail(email);

        if (userAuth == null) {
            throw new RuntimeException("User not found");
        }

        // Check if requester is the captain
        var crew = crewRepository.findById(crewId)
                .orElseThrow(() -> new RuntimeException("Crew not found"));

        if (!crew.getCaptainId().equals(userAuth.getId())) {
            throw new RuntimeException("Only captain can kick members");
        }

        // Find the member
        CrewMember member = crewMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        if (!member.getCrewId().equals(crewId)) {
            throw new RuntimeException("Member does not belong to this crew");
        }

        // Cannot kick self (captain)
        if (member.getUserId().equals(userAuth.getId())) {
            throw new RuntimeException("Cannot kick yourself");
        }

        crewMemberRepository.delete(member);

        // Notification
        try {
            fcmService.sendToUser(
                    member.getUserId(),
                    "크루 강퇴 알림",
                    crew.getName() + " 크루에서 강퇴되었습니다.",
                    NotificationType.CREW_JOIN_REJECTED, // Using rejected type implies removal
                    Map.of());
        } catch (Exception e) {
            System.err.println("Failed to send kick notification: " + e.getMessage());
        }
    }
}
