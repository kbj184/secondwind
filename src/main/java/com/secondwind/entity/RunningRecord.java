package com.secondwind.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "running_record")
public class RunningRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserAuth user;

    private Double distance;
    private Long duration;
    private Double averageSpeed;
    private Double averagePace;

    @Column(columnDefinition = "LONGTEXT")
    private String route; // JSON string

    @Column(columnDefinition = "TEXT")
    private String wateringSegments; // JSON string

    @Column(columnDefinition = "TEXT")
    private String splits; // JSON string

    private Boolean isComplete;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
