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
}
