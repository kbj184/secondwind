-- FCM Push Notification System - Database Migration
-- Create user_fcm_tokens table for storing FCM device tokens

CREATE TABLE IF NOT EXISTS user_fcm_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) NOT NULL UNIQUE,
    device_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_user_id (user_id),
    INDEX idx_token (token),
    
    FOREIGN KEY (user_id) REFERENCES user_auth(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Comments
ALTER TABLE user_fcm_tokens COMMENT = 'Stores FCM device tokens for push notifications';
