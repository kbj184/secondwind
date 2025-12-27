package com.secondwind.repository;

import com.secondwind.entity.Crew;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CrewRepository extends JpaRepository<Crew, Long> {
    Optional<Crew> findByCaptainId(Long captainId);
}
