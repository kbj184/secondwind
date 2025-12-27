-- Crews 테이블 생성
CREATE TABLE IF NOT EXISTS crews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    image_url TEXT,
    captain_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Key (user_auth 테이블과 연결)
    CONSTRAINT fk_crew_captain FOREIGN KEY (captain_id) REFERENCES user_auth(id) ON DELETE CASCADE,
    
    -- Index
    INDEX idx_captain_id (captain_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 테이블 설명
-- id: 크루 고유 ID (자동 증가)
-- name: 크루 이름 (필수)
-- description: 크루 설명 (선택)
-- image_url: 크루 이미지 URL (Cloudinary URL 또는 JSON 형식의 기본 이미지 정보)
-- captain_id: 크루장 ID (user_auth 테이블의 id 참조)
-- created_at: 크루 생성 일시 (자동 설정)
