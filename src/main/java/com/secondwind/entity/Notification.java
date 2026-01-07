package com.secondwind.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAuth user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    private String relatedUrl; // Store the direct URL or relevant ID

    @Column(name = "is_read")
    private boolean isRead = false;

    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;
}
