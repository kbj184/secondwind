-- user_activity_area 테이블에 admin_level_full 컬럼 추가
-- 전체 주소를 저장하기 위한 필드

ALTER TABLE user_activity_area
ADD COLUMN admin_level_full VARCHAR(500) NULL COMMENT '주 활동 지역 전체 주소';

-- 기존 데이터가 있는 경우, adminLevel1, adminLevel2, adminLevel3을 조합하여 임시로 채울 수 있습니다
-- UPDATE user_activity_area
-- SET admin_level_full = CONCAT_WS(', ', main_country_name, admin_level_1, admin_level_2, admin_level_3)
-- WHERE admin_level_full IS NULL;
