package com.secondwind.repository;

import com.secondwind.entity.CrewCourseLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CrewCourseLikeRepository extends JpaRepository<CrewCourseLike, Long> {
    Optional<CrewCourseLike> findByCourseIdAndUserId(Long courseId, Long userId);

    long countByCourseId(Long courseId);

    boolean existsByCourseIdAndUserId(Long courseId, Long userId);
}
