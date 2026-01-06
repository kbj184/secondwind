package com.secondwind.repository;

import com.secondwind.entity.CrewCourseComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrewCourseCommentRepository extends JpaRepository<CrewCourseComment, Long> {
    List<CrewCourseComment> findByCourseIdOrderByCreatedAtDesc(Long courseId);

    long countByCourseId(Long courseId);
}
