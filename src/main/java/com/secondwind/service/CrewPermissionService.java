package com.secondwind.service;

import com.secondwind.entity.Crew;
import com.secondwind.entity.CrewMember;
import com.secondwind.repository.CrewMemberRepository;
import com.secondwind.repository.CrewRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 크루 권한 관리 서비스
 * 크루장, 부크루장, 일반 멤버의 권한을 체크하는 중앙화된 서비스
 */
@Service
public class CrewPermissionService {

    private final CrewRepository crewRepository;
    private final CrewMemberRepository crewMemberRepository;

    public CrewPermissionService(CrewRepository crewRepository, CrewMemberRepository crewMemberRepository) {
        this.crewRepository = crewRepository;
        this.crewMemberRepository = crewMemberRepository;
    }

    /**
     * 크루장인지 확인
     */
    public boolean isCaptain(Long crewId, Long userId) {
        Optional<Crew> crew = crewRepository.findById(crewId);
        return crew.isPresent() && crew.get().getCaptainId().equals(userId);
    }

    /**
     * 부크루장인지 확인
     */
    public boolean isViceCaptain(Long crewId, Long userId) {
        Optional<CrewMember> member = crewMemberRepository.findByCrewIdAndUserId(crewId, userId);
        return member.isPresent() && "vice_captain".equals(member.get().getRole());
    }

    /**
     * 크루장 또는 부크루장인지 확인 (관리자 권한)
     */
    public boolean isManager(Long crewId, Long userId) {
        return isCaptain(crewId, userId) || isViceCaptain(crewId, userId);
    }

    /**
     * 크루 멤버인지 확인 (승인된 멤버)
     */
    public boolean isMember(Long crewId, Long userId) {
        Optional<CrewMember> member = crewMemberRepository.findByCrewIdAndUserId(crewId, userId);
        return member.isPresent() && "APPROVED".equals(member.get().getStatus());
    }

    /**
     * 크루장 권한 체크 (예외 발생)
     */
    public void requireCaptain(Long crewId, Long userId) {
        if (!isCaptain(crewId, userId)) {
            throw new RuntimeException("Only captain can perform this action");
        }
    }

    /**
     * 부크루장 권한 체크 (예외 발생)
     */
    public void requireViceCaptain(Long crewId, Long userId) {
        if (!isViceCaptain(crewId, userId)) {
            throw new RuntimeException("Only vice captain can perform this action");
        }
    }

    /**
     * 관리자 권한 체크 (크루장 또는 부크루장, 예외 발생)
     */
    public void requireManager(Long crewId, Long userId) {
        if (!isManager(crewId, userId)) {
            throw new RuntimeException("Only captain or vice captain can perform this action");
        }
    }

    /**
     * 멤버 권한 체크 (예외 발생)
     */
    public void requireMember(Long crewId, Long userId) {
        if (!isMember(crewId, userId)) {
            throw new RuntimeException("Only approved members can perform this action");
        }
    }

    /**
     * 크루 멤버 역할 가져오기
     */
    public String getMemberRole(Long crewId, Long userId) {
        if (isCaptain(crewId, userId)) {
            return "captain";
        }

        Optional<CrewMember> member = crewMemberRepository.findByCrewIdAndUserId(crewId, userId);
        return member.map(CrewMember::getRole).orElse(null);
    }

    /**
     * 크루 멤버 정보 가져오기
     */
    public Optional<CrewMember> getMember(Long crewId, Long userId) {
        return crewMemberRepository.findByCrewIdAndUserId(crewId, userId);
    }

    /**
     * 크루 정보 가져오기
     */
    public Optional<Crew> getCrew(Long crewId) {
        return crewRepository.findById(crewId);
    }
}
