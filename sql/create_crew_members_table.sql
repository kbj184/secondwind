-- Crew Members 테이블 생성
CREATE TABLE IF NOT EXISTS crew_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    crew_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'member',
    joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    CONSTRAINT fk_crew_member_crew FOREIGN KEY (crew_id) REFERENCES crews(id) ON DELETE CASCADE,
    CONSTRAINT fk_crew_member_user FOREIGN KEY (user_id) REFERENCES user_auth(id) ON DELETE CASCADE,
    
    -- Unique constraint: 한 사용자는 하나의 크루에만 가입 가능
    CONSTRAINT uk_user_crew UNIQUE (user_id),
    
    -- Index
    INDEX idx_crew_id (crew_id),
    INDEX idx_user_id (user_id),
    INDEX idx_joined_at (joined_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 테이블 설명
-- id: 멤버십 고유 ID (자동 증가)
-- crew_id: 크루 ID (crews 테이블 참조)
-- user_id: 사용자 ID (user_auth 테이블 참조)
-- role: 역할 ("captain" 또는 "member")
-- joined_at: 가입 일시 (자동 설정)
