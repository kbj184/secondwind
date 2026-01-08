package com.secondwind.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "crews")
public class Crew {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "captain_id", nullable = false)
    private Long captainId;

    @Column(name = "join_type", nullable = false)
    private String joinType = "AUTO"; // "AUTO" or "APPROVAL"

    @Column(name = "activity_area")
    private String activityArea;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "name_change_count")
    private Integer nameChangeCount = 0;

    @Column(name = "last_name_change_date")
    private LocalDateTime lastNameChangeDate;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
