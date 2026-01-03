-- ============================================
-- 크루 멤버십 시스템 재설계: 대표 크루 + 보조 크루
-- ============================================

-- 1. 기존 유니크 제약 조건 제거 (한 사용자가 여러 크루 가입 가능하도록)
ALTER TABLE crew_members DROP INDEX IF EXISTS uk_user_crew;

-- 2. 새로운 유니크 제약 조건 추가 (같은 크루에 중복 가입 방지)
ALTER TABLE crew_members 
ADD UNIQUE INDEX uk_user_crew_unique (user_id, crew_id);

-- 3. is_primary 컬럼 추가 (대표 크루 여부)
ALTER TABLE crew_members 
ADD COLUMN is_primary BOOLEAN NOT NULL DEFAULT FALSE 
COMMENT 'Primary crew flag';

-- 4. 인덱스 추가 (대표 크루 조회 최적화)
CREATE INDEX idx_user_primary ON crew_members(user_id, is_primary);

-- 5. 기존 데이터 마이그레이션: 크루장을 대표 크루로 설정
UPDATE crew_members cm
JOIN crews c ON cm.crew_id = c.id
SET cm.is_primary = TRUE
WHERE cm.role = 'captain';

-- 6. 크루장이 아닌 멤버 중 가장 먼저 가입한 크루를 대표 크루로 설정
UPDATE crew_members cm1
SET cm1.is_primary = TRUE
WHERE cm1.id IN (
    SELECT * FROM (
        SELECT MIN(cm2.id) 
        FROM crew_members cm2 
        WHERE cm2.user_id = cm1.user_id 
        AND cm2.role != 'captain'
        AND NOT EXISTS (
            SELECT 1 FROM crew_members cm3 
            WHERE cm3.user_id = cm2.user_id 
            AND cm3.is_primary = TRUE
        )
        GROUP BY cm2.user_id
    ) AS temp
);

-- 7. 검증: 각 사용자가 정확히 1개의 대표 크루를 가지는지 확인
SELECT 
    user_id, 
    COUNT(*) as primary_crew_count
FROM crew_members 
WHERE is_primary = TRUE
GROUP BY user_id
HAVING COUNT(*) != 1;

-- 위 쿼리 결과가 비어있으면 마이그레이션 성공
-- 결과가 있으면 해당 사용자들의 대표 크루 설정을 수동으로 조정 필요
