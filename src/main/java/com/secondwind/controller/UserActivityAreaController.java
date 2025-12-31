package com.secondwind.controller;

import com.secondwind.dto.UserActivityAreaDTO;
import com.secondwind.entity.UserActivityArea;
import com.secondwind.entity.UserAuth;
import com.secondwind.repository.UserActivityAreaRepository;
import com.secondwind.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/user/activity-area")
public class UserActivityAreaController {

    private final UserActivityAreaRepository activityAreaRepository;
    private final UserRepository userRepository;

    public UserActivityAreaController(UserActivityAreaRepository activityAreaRepository,
            UserRepository userRepository) {
        this.activityAreaRepository = activityAreaRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<?> saveActivityArea(@RequestBody UserActivityAreaDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserAuth user = userRepository.findByEmail(email);

        if (user == null) {
            return ResponseEntity.status(401).body("User not found");
        }

        Optional<UserActivityArea> existing = activityAreaRepository.findByUserId(user.getId());
        UserActivityArea activityArea = existing.orElse(new UserActivityArea());

        activityArea.setUserId(user.getId());
        activityArea.setMainCountryCode(dto.getMainCountryCode());
        activityArea.setMainCountryName(dto.getMainCountryName());
        activityArea.setAdminLevel1(dto.getAdminLevel1());
        activityArea.setAdminLevel2(dto.getAdminLevel2());
        activityArea.setAdminLevel3(dto.getAdminLevel3());
        activityArea.setLatitude(dto.getLatitude());
        activityArea.setLongitude(dto.getLongitude());

        activityAreaRepository.save(activityArea);

        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<?> getActivityArea() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserAuth user = userRepository.findByEmail(email);

        if (user == null) {
            return ResponseEntity.status(401).body("User not found");
        }

        return activityAreaRepository.findByUserId(user.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
