-- UserAuth 테이블에 runner_grade 컬럼 추가
ALTER TABLE user_auth
ADD COLUMN runner_grade VARCHAR(50) DEFAULT 'BEGINNER';

-- 기존 사용자들의 등급을 BEGINNER로 초기화
UPDATE user_auth
SET runner_grade = 'BEGINNER'
WHERE runner_grade IS NULL;
