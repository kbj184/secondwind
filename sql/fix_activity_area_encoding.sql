-- user_activity_area 테이블의 문자셋을 utf8mb4로 변경하여 한글 저장 문제를 해결합니다.
ALTER TABLE user_activity_area CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 개별 컬럼에 대해서도 명시적으로 변경 (필요한 경우)
ALTER TABLE user_activity_area MODIFY admin_level1 VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE user_activity_area MODIFY admin_level2 VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE user_activity_area MODIFY admin_level3 VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE user_activity_area MODIFY main_country_name VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
