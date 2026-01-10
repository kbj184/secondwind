package com.secondwind.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "running_sessions", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_session_id", columnList = "session_id"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
public class RunningSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "distance")
    private Double distance;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "speed")
    private Double speed;

    @Column(name = "pace")
    private Double pace;

    @Column(name = "current_elevation")
    private Double currentElevation;

    @Column(name = "total_ascent")
    private Double totalAscent;

    @Column(name = "total_descent")
    private Double totalDescent;

    @Column(name = "route", columnDefinition = "LONGTEXT")
    private String route;

    @Column(name = "watering_segments", columnDefinition = "TEXT")
    private String wateringSegments;

    @Column(name = "splits", columnDefinition = "TEXT")
    private String splits;

    @Column(name = "is_complete")
    private Boolean isComplete;

    @Column(name = "thumbnail", columnDefinition = "TEXT")
    private String thumbnail;

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "course_completed", nullable = false)
    private Boolean courseCompleted = false;

    @Column(name = "is_bookmarked", nullable = false)
    private Boolean isBookmarked = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
