# 러너 등급 시스템 (Runner Grade System)

## 📊 등급 체계

### 자동 승급 등급 (조건 충족 시 자동 승급)

| 등급 | 영문명 | 조건 |
|------|--------|------|
| 🥉 초보자 | Beginner | 5km 미만 (시간 제한 없음) |
| 🏃 5K 러너 | 5K Runner | 10km 미만 및 1시간 이내 |
| 🏃‍♂️ 10K 러너 | 10K Runner | 21km 미만 및 1시간 30분 이내 |
| 🎽 하프 마라토너 | Half Marathoner | 42km 미만 및 2시간 30분 이내 |
| 🏅 풀 마라토너 | Full Marathoner | 42km 이상 및 5시간 30분 이내 |
| ⚡ Sub-3 마라토너 | Sub-3 Marathoner | 42km 이상 및 3시간 이내 |
| 👑 엘리트 마라토너 | Elite Marathoner | 42km 이상 및 2시간 30분 이내 |

### 특별 등급 (관리자 수동 승급)

| 등급 | 영문명 | 조건 |
|------|--------|------|
| 🌟 전설의 러너 | Legend Marathoner | 관리자 승급 (총 거리 40,000km 이상 또는 풀코스 40회 이상) |

## 🔄 작동 방식

### 자동 승급
1. 사용자가 러닝 세션을 완료하면 자동으로 등급 체크
2. 거리와 시간 조건을 모두 충족하면 즉시 승급
3. 현재 등급보다 높은 등급만 적용됨 (강등 없음)
4. 승급 시 프론트엔드에 알림 표시

### 수동 승급 (관리자 전용)
- Legend Marathoner 등급은 관리자만 부여 가능
- 특별한 업적을 달성한 사용자에게 수여

## 🛠️ 구현 내역

### 백엔드
- `RunnerGrade.java` - 등급 enum 및 계산 로직
- `RunnerGradeService.java` - 등급 관리 서비스
- `UserAuth.java` - 사용자 등급 필드 추가
- `RunningController.java` - 세션 완료 시 자동 승급 체크

### 데이터베이스
```sql
-- UserAuth 테이블에 runner_grade 컬럼 추가
ALTER TABLE user_auth
ADD COLUMN runner_grade VARCHAR(50) DEFAULT 'BEGINNER';
```

### API 응답 형식
```json
{
  "session": { ... },
  "gradeUpgraded": true,
  "newGrade": "5K Runner",
  "gradeLevel": 1,
  "gradeDescription": "10km 미만 및 1시간 이내"
}
```

## 📝 사용 예시

### 등급 확인
```java
RunnerGrade grade = runnerGradeService.getUserGrade(userId);
```

### 수동 승급 (관리자)
```java
runnerGradeService.setUserGrade(userId, RunnerGrade.LEGEND_MARATHONER);
```

## 🎯 향후 계획
- [ ] 프론트엔드 등급 배지 UI
- [ ] 승급 축하 애니메이션
- [ ] 등급별 통계 대시보드
- [ ] 등급별 리더보드
