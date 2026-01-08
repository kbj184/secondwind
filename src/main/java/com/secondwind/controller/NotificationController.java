package com.secondwind.controller;

import com.secondwind.entity.Notification;
import com.secondwind.entity.UserAuth;
import com.secondwind.repository.UserRepository;
import com.secondwind.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getNotifications(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserAuth user = userRepository.findByEmail(email);
        if (user == null)
            return ResponseEntity.status(401).build();

        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationService.getMyNotifications(user.getId(), pageable);

        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserAuth user = userRepository.findByEmail(email);
        if (user == null)
            return ResponseEntity.status(401).build();

        long count = notificationService.getUnreadCount(user.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserAuth user = userRepository.findByEmail(email);
        if (user == null)
            return ResponseEntity.status(401).build();

        try {
            notificationService.markAsRead(id, user.getId());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserAuth user = userRepository.findByEmail(email);
        if (user == null)
            return ResponseEntity.status(401).build();

        try {
            notificationService.deleteNotification(id, user.getId());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
