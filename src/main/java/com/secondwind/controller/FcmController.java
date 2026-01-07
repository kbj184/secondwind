package com.secondwind.controller;

import com.secondwind.service.FcmService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.secondwind.repository.UserRepository;
import com.secondwind.entity.UserAuth;

import java.util.Map;

@RestController
@RequestMapping("/api/fcm")
public class FcmController {

    private final FcmService fcmService;
    private final UserRepository userRepository;

    public FcmController(FcmService fcmService, UserRepository userRepository) {
        this.fcmService = fcmService;
        this.userRepository = userRepository;
    }

    /**
     * Save FCM token for the authenticated user
     */
    @PostMapping("/token")
    public ResponseEntity<?> saveToken(@RequestBody TokenRequest request) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            UserAuth user = userRepository.findByEmail(email);

            if (user == null) {
                return ResponseEntity.status(401).body("User not authenticated");
            }

            fcmService.saveToken(user.getId(), request.getToken(), request.getDeviceType());
            return ResponseEntity.ok(Map.of("message", "Token saved successfully"));

        } catch (Exception e) {
            System.err.println("Error saving FCM token: " + e.getMessage());
            return ResponseEntity.status(500).body("Failed to save token");
        }
    }

    /**
     * Delete FCM token
     */
    @DeleteMapping("/token")
    public ResponseEntity<?> deleteToken(@RequestParam String token) {
        try {
            fcmService.removeToken(token);
            return ResponseEntity.ok(Map.of("message", "Token deleted successfully"));

        } catch (Exception e) {
            System.err.println("Error deleting FCM token: " + e.getMessage());
            return ResponseEntity.status(500).body("Failed to delete token");
        }
    }

    /**
     * Delete all tokens for the authenticated user
     */
    @DeleteMapping("/tokens")
    public ResponseEntity<?> deleteAllTokens() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            UserAuth user = userRepository.findByEmail(email);

            if (user == null) {
                return ResponseEntity.status(401).body("User not authenticated");
            }

            fcmService.removeUserTokens(user.getId());
            return ResponseEntity.ok(Map.of("message", "All tokens deleted successfully"));

        } catch (Exception e) {
            System.err.println("Error deleting FCM tokens: " + e.getMessage());
            return ResponseEntity.status(500).body("Failed to delete tokens");
        }
    }

    /**
     * Request DTO for saving token
     */
    public static class TokenRequest {
        private String token;
        private String deviceType;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getDeviceType() {
            return deviceType;
        }

        public void setDeviceType(String deviceType) {
            this.deviceType = deviceType;
        }
    }
}
