package com.secondwind.repository;

import com.secondwind.entity.CrewActivityArea;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CrewActivityAreaRepository extends JpaRepository<CrewActivityArea, Long> {
    List<CrewActivityArea> findByCrewId(Long crewId);

    void deleteByCrewId(Long crewId);
}
