package com.secondwind.repository;

import com.secondwind.entity.CrewMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CrewMemberRepository extends JpaRepository<CrewMember, Long> {
    List<CrewMember> findByCrewId(Long crewId);

    Optional<CrewMember> findByCrewIdAndUserId(Long crewId, Long userId);

    Optional<CrewMember> findByUserId(Long userId);

    long countByCrewId(Long crewId);

    // Primary crew management
    List<CrewMember> findAllByUserId(Long userId);

    List<CrewMember> findByUserIdAndIsPrimary(Long userId, Boolean isPrimary);

    long countByUserId(Long userId);

    List<CrewMember> findByUserIdAndStatus(Long userId, String status);

    // Board permission check
    boolean existsByCrewIdAndUserIdAndStatus(Long crewId, Long userId, String status);
}
