package com.secondwind.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "crew_courses")
public class CrewCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "crew_id", nullable = false)
    private Long crewId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 200)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Double distance; // km

    @Column(name = "route_data", columnDefinition = "TEXT")
    private String routeData; // JSON polyline data

    @Column(name = "map_thumbnail_url", length = 500)
    private String mapThumbnailUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
