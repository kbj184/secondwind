package com.secondwind.service;

import com.google.firebase.messaging.*;
import com.secondwind.entity.NotificationType;
import com.secondwind.entity.UserAuth;
import com.secondwind.entity.UserFcmToken;
import com.secondwind.repository.UserFcmTokenRepository;
import com.secondwind.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FcmService {

    private final UserFcmTokenRepository tokenRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public FcmService(UserFcmTokenRepository tokenRepository, NotificationService notificationService,
            UserRepository userRepository) {
        this.tokenRepository = tokenRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    /**
     * Send notification to a single user
     */
    public void sendToUser(Long userId, String title, String body, NotificationType type, Map<String, String> data) {
        // 1. Save notification to DB (regardless of token existence)
        try {
            UserAuth user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                String relatedUrl = type.getRouteTemplate();
                if (data != null) {
                    for (Map.Entry<String, String> entry : data.entrySet()) {
                        relatedUrl = relatedUrl.replace("{" + entry.getKey() + "}", entry.getValue());
                    }
                }
                notificationService.createNotification(user, type, title, body, relatedUrl);
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to save notification to DB: " + e.getMessage());
            e.printStackTrace();
        }

        // 2. Send Push Notification. If no tokens, just return.
        List<UserFcmToken> tokens = tokenRepository.findByUserId(userId);

        if (tokens.isEmpty()) {
            System.out.println("⚠️ No FCM tokens found for user: " + userId);
            return;
        }

        for (UserFcmToken tokenEntity : tokens) {
            sendNotification(tokenEntity.getToken(), title, body, type, data);
        }
    }

    /**
     * Send notification to multiple users
     */
    public void sendToUsers(List<Long> userIds, String title, String body, NotificationType type,
            Map<String, String> data) {
        for (Long userId : userIds) {
            sendToUser(userId, title, body, type, data);
        }
    }

    /**
     * Send notification to all users
     * Note: This currently only sends PUSH to devices in DB.
     * It does not create DB notifications for all users to avoid massive inserts.
     */
    public void sendToAll(String title, String body, NotificationType type, Map<String, String> data) {
        List<UserFcmToken> allTokens = tokenRepository.findAll();

        if (allTokens.isEmpty()) {
            System.out.println("⚠️ No FCM tokens found in database");
            return;
        }

        for (UserFcmToken tokenEntity : allTokens) {
            sendNotification(tokenEntity.getToken(), title, body, type, data);
        }
    }

    /**
     * Core method to send FCM notification
     */
    private void sendNotification(String token, String title, String body, NotificationType type,
            Map<String, String> data) {
        try {
            // Prepare data payload
            Map<String, String> dataPayload = new HashMap<>();
            if (data != null) {
                dataPayload.putAll(data);
            }
            dataPayload.put("type", type.name());
            dataPayload.put("route", type.getRouteTemplate());

            // Build notification (Firebase class)
            com.google.firebase.messaging.Notification firebaseNotification = com.google.firebase.messaging.Notification
                    .builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            // Build message
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(firebaseNotification)
                    .putAllData(dataPayload)
                    .setWebpushConfig(WebpushConfig.builder()
                            .setNotification(WebpushNotification.builder()
                                    .setTitle(title)
                                    .setBody(body)
                                    .setIcon("/logo.png")
                                    .build())
                            .build())
                    .build();

            // Send message
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("✅ Successfully sent message: " + response);

        } catch (FirebaseMessagingException e) {
            System.err.println("❌ Failed to send FCM message: " + e.getMessage());

            // If token is invalid, remove it from database
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED ||
                    e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT) {
                removeInvalidToken(token);
            }
        } catch (Exception e) {
            System.err.println("❌ Unexpected error sending FCM message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Save FCM token for a user
     */
    @Transactional
    public void saveToken(Long userId, String token, String deviceType) {
        // Check if token already exists
        if (tokenRepository.existsByToken(token)) {
            System.out.println("ℹ️ Token already exists, skipping save");
            return;
        }

        UserFcmToken fcmToken = new UserFcmToken();
        fcmToken.setUserId(userId);
        fcmToken.setToken(token);
        fcmToken.setDeviceType(deviceType);

        tokenRepository.save(fcmToken);
        System.out.println("✅ Saved FCM token for user: " + userId);
    }

    /**
     * Remove FCM token
     */
    @Transactional
    public void removeToken(String token) {
        tokenRepository.deleteByToken(token);
        System.out.println("🗑️ Removed FCM token");
    }

    /**
     * Remove all tokens for a user
     */
    @Transactional
    public void removeUserTokens(Long userId) {
        tokenRepository.deleteByUserId(userId);
        System.out.println("🗑️ Removed all FCM tokens for user: " + userId);
    }

    /**
     * Remove invalid token from database
     */
    @Transactional
    private void removeInvalidToken(String token) {
        try {
            tokenRepository.deleteByToken(token);
            System.out.println("🗑️ Removed invalid FCM token");
        } catch (Exception e) {
            System.err.println("❌ Failed to remove invalid token: " + e.getMessage());
        }
    }
}
