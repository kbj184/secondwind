# 게시판 테이블 스키마 수정 SQL

## 문제
`posts`와 `comments` 테이블의 `content` 컬럼이 `LONGTEXT`가 아닌 다른 타입으로 생성되어 게시글 작성 시 SQL 오류 발생

## 해결 방법
아래 SQL을 DB 툴에서 실행하여 테이블 스키마를 수정하세요.

### 1. posts 테이블이 없는 경우 (새로 생성)
```sql
CREATE TABLE IF NOT EXISTS posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category VARCHAR(50) NOT NULL,
    crew_id BIGINT,
    author_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content LONGTEXT,
    is_pinned BOOLEAN NOT NULL DEFAULT FALSE,
    view_count INT NOT NULL DEFAULT 0,
    like_count INT NOT NULL DEFAULT 0,
    comment_count INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    INDEX idx_category (category),
    INDEX idx_crew_id (crew_id),
    INDEX idx_author_id (author_id)
);
```

### 2. posts 테이블이 이미 있는 경우 (컬럼 수정)
```sql
-- content 컬럼 타입 변경
ALTER TABLE posts MODIFY COLUMN content LONGTEXT;
```

### 3. comments 테이블이 없는 경우 (새로 생성)
```sql
CREATE TABLE IF NOT EXISTS comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    content LONGTEXT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    INDEX idx_post_id (post_id),
    INDEX idx_author_id (author_id)
);
```

### 4. comments 테이블이 이미 있는 경우 (컬럼 수정)
```sql
-- content 컬럼 타입 변경
ALTER TABLE comments MODIFY COLUMN content LONGTEXT NOT NULL;
```

## 확인
```sql
-- 테이블 구조 확인
DESCRIBE posts;
DESCRIBE comments;
```

`content` 컬럼의 Type이 `longtext`로 표시되면 정상입니다.
