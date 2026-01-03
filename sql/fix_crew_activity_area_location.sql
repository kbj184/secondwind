-- crew_activity_area 테이블에서 location 컬럼 제거
-- (latitude, longitude가 이미 있으므로 불필요)

ALTER TABLE crew_activity_area DROP COLUMN IF EXISTS location;
