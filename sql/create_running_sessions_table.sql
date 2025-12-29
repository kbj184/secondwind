-- Running Sessions 테이블 생성
CREATE TABLE IF NOT EXISTS running_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_id VARCHAR(255) NOT NULL,
    distance DECIMAL(10, 3) DEFAULT 0,
    duration INT DEFAULT 0,
    speed DECIMAL(10, 2) DEFAULT 0,
    pace DECIMAL(10, 2) DEFAULT 0,
    current_elevation DECIMAL(10, 2) DEFAULT 0,
    total_ascent DECIMAL(10, 2) DEFAULT 0,
    total_descent DECIMAL(10, 2) DEFAULT 0,
    route LONGTEXT,
    watering_segments TEXT,
    splits TEXT,
    is_complete BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_session_id (session_id),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (user_id) REFERENCES user_auth(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
