package com.secondwind.repository;

import com.secondwind.entity.CrewCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CrewCourseRepository extends JpaRepository<CrewCourse, Long> {

    List<CrewCourse> findByCrewIdOrderByCreatedAtDesc(Long crewId);

    Optional<CrewCourse> findByIdAndCrewId(Long id, Long crewId);

    void deleteByIdAndUserId(Long id, Long userId);
}
