# 부크루장 권한 설정 가이드

> **작성일**: 2026-01-08  
> **목적**: 부크루장(vice_captain) 권한 체계 정리 및 구현 가이드

---

## 📋 목차

1. [권한 체계 개요](#권한-체계-개요)
2. [CrewPermissionService 사용법](#crewpermissionservice-사용법)
3. [부크루장 권한이 필요한 기능](#부크루장-권한이-필요한-기능)
4. [구현 예시](#구현-예시)
5. [프론트엔드 연동](#프론트엔드-연동)

---

## 🎯 권한 체계 개요

### 역할 정의

| 역할 | 코드 | 설명 | 권한 |
|------|------|------|------|
| **크루장** | `captain` | 크루 생성자 | 모든 권한 |
| **부크루장** | `vice_captain` | 크루장이 지정 | 관리자 권한 (일부 제한) |
| **일반 멤버** | `member` | 일반 크루원 | 기본 권한 |

### 권한 레벨

```
Level 1: 크루장 (Captain)
  - 크루 삭제
  - 크루 정보 수정
  - 부크루장 임명
  - 멤버 강퇴
  - 멤버 승인/거절
  - 게시글 삭제 (모든 게시글)
  - 댓글 삭제 (모든 댓글)
  - 코스 삭제 (모든 코스)

Level 2: 부크루장 (Vice Captain) ⭐
  - 멤버 승인/거절
  - 게시글 삭제 (모든 게시글)
  - 댓글 삭제 (모든 댓글)
  - 코스 삭제 (모든 코스)
  - 공지사항 작성

Level 3: 일반 멤버 (Member)
  - 자신의 게시글/댓글 작성/수정/삭제
  - 코스 작성
  - 크루 활동 참여
```

---

## 🛠️ CrewPermissionService 사용법

### 기본 사용법

```java
@RestController
@RequestMapping("/crew")
public class YourController {

    private final CrewPermissionService permissionService;

    public YourController(CrewPermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping("/{crewId}/some-action")
    public void someAction(@PathVariable Long crewId) {
        Long userId = getCurrentUserId(); // 현재 사용자 ID 가져오기

        // 방법 1: 권한 체크 (예외 발생)
        permissionService.requireManager(crewId, userId);

        // 방법 2: 권한 확인 (boolean 반환)
        if (permissionService.isManager(crewId, userId)) {
            // 권한 있음
        } else {
            throw new RuntimeException("권한이 없습니다");
        }

        // 실제 로직...
    }
}
```

### 제공되는 메서드

#### 권한 확인 (boolean 반환)

```java
// 크루장인지 확인
boolean isCaptain = permissionService.isCaptain(crewId, userId);

// 부크루장인지 확인
boolean isViceCaptain = permissionService.isViceCaptain(crewId, userId);

// 크루장 또는 부크루장인지 확인 (관리자)
boolean isManager = permissionService.isManager(crewId, userId);

// 승인된 멤버인지 확인
boolean isMember = permissionService.isMember(crewId, userId);
```

#### 권한 체크 (예외 발생)

```java
// 크루장 권한 필요 (아니면 예외)
permissionService.requireCaptain(crewId, userId);

// 부크루장 권한 필요 (아니면 예외)
permissionService.requireViceCaptain(crewId, userId);

// 관리자 권한 필요 (크루장 또는 부크루장, 아니면 예외)
permissionService.requireManager(crewId, userId);

// 멤버 권한 필요 (승인된 멤버, 아니면 예외)
permissionService.requireMember(crewId, userId);
```

#### 정보 조회

```java
// 역할 가져오기 ("captain", "vice_captain", "member", null)
String role = permissionService.getMemberRole(crewId, userId);

// 멤버 정보 가져오기
Optional<CrewMember> member = permissionService.getMember(crewId, userId);

// 크루 정보 가져오기
Optional<Crew> crew = permissionService.getCrew(crewId);
```

---

## 📝 부크루장 권한이 필요한 기능

### 1. 멤버 관리

#### ✅ 멤버 승인 (approveMember)
**파일**: `CrewMemberController.java`  
**현재**: 크루장만 가능  
**변경**: 부크루장도 가능하게

```java
@PostMapping("/{crewId}/members/{userId}/approve")
public CrewMemberDTO approveMember(@PathVariable Long crewId, @PathVariable Long userId) {
    Long currentUserId = getCurrentUserId();
    
    // 변경 전: 크루장만
    // if (!crew.getCaptainId().equals(currentUserId)) {
    //     throw new RuntimeException("Only captain can approve members");
    // }
    
    // 변경 후: 크루장 또는 부크루장
    permissionService.requireManager(crewId, currentUserId);
    
    // 나머지 로직...
}
```

#### ✅ 멤버 거절 (rejectMember)
**파일**: `CrewMemberController.java`  
**현재**: 크루장만 가능  
**변경**: 부크루장도 가능하게

```java
@PostMapping("/{crewId}/members/{userId}/reject")
public void rejectMember(@PathVariable Long crewId, @PathVariable Long userId) {
    Long currentUserId = getCurrentUserId();
    
    // 변경 후: 크루장 또는 부크루장
    permissionService.requireManager(crewId, currentUserId);
    
    // 나머지 로직...
}
```

#### ❌ 멤버 강퇴 (kickMember)
**파일**: `CrewMemberController.java`  
**현재**: 크루장만 가능  
**변경**: **크루장만 유지** (부크루장 권한 없음)

```java
@DeleteMapping("/{crewId}/members/{memberId}/kick")
public void kickMember(@PathVariable Long crewId, @PathVariable Long memberId) {
    Long currentUserId = getCurrentUserId();
    
    // 크루장만 가능 (유지)
    permissionService.requireCaptain(crewId, currentUserId);
    
    // 나머지 로직...
}
```

#### ❌ 역할 변경 (updateMemberRole)
**파일**: `CrewMemberController.java`  
**현재**: 크루장만 가능  
**변경**: **크루장만 유지** (부크루장 임명은 크루장만)

### 2. 게시판 관리

#### ✅ 게시글 삭제 (모든 게시글)
**파일**: `BoardController.java`  
**현재**: 작성자 또는 크루장  
**변경**: 작성자, 크루장, 부크루장

```java
@DeleteMapping("/{crewId}/posts/{postId}")
public void deletePost(@PathVariable Long crewId, @PathVariable Long postId) {
    Long currentUserId = getCurrentUserId();
    Post post = getPost(postId);
    
    // 변경 후: 작성자, 크루장, 부크루장
    boolean isAuthor = post.getUserId().equals(currentUserId);
    boolean isManager = permissionService.isManager(crewId, currentUserId);
    
    if (!isAuthor && !isManager) {
        throw new RuntimeException("권한이 없습니다");
    }
    
    // 삭제 로직...
}
```

#### ✅ 댓글 삭제 (모든 댓글)
**파일**: `BoardController.java`  
**현재**: 작성자 또는 크루장  
**변경**: 작성자, 크루장, 부크루장

```java
@DeleteMapping("/{crewId}/posts/{postId}/comments/{commentId}")
public void deleteComment(@PathVariable Long crewId, 
                         @PathVariable Long postId, 
                         @PathVariable Long commentId) {
    Long currentUserId = getCurrentUserId();
    Comment comment = getComment(commentId);
    
    // 변경 후: 작성자, 크루장, 부크루장
    boolean isAuthor = comment.getUserId().equals(currentUserId);
    boolean isManager = permissionService.isManager(crewId, currentUserId);
    
    if (!isAuthor && !isManager) {
        throw new RuntimeException("권한이 없습니다");
    }
    
    // 삭제 로직...
}
```

#### ✅ 공지사항 작성
**파일**: `BoardController.java`  
**현재**: 구현 필요  
**변경**: 크루장, 부크루장만 가능

```java
@PostMapping("/{crewId}/posts")
public PostDTO createPost(@PathVariable Long crewId, @RequestBody PostDTO postDTO) {
    Long currentUserId = getCurrentUserId();
    
    // 공지사항인 경우 관리자 권한 필요
    if (postDTO.isNotice()) {
        permissionService.requireManager(crewId, currentUserId);
    } else {
        // 일반 게시글은 멤버면 가능
        permissionService.requireMember(crewId, currentUserId);
    }
    
    // 작성 로직...
}
```

### 3. 코스 관리

#### ✅ 코스 삭제 (모든 코스)
**파일**: `CrewCourseController.java`  
**현재**: 작성자 또는 크루장  
**변경**: 작성자, 크루장, 부크루장

```java
@DeleteMapping("/{crewId}/courses/{courseId}")
public void deleteCourse(@PathVariable Long crewId, @PathVariable Long courseId) {
    Long currentUserId = getCurrentUserId();
    CrewCourse course = getCourse(courseId);
    
    // 변경 후: 작성자, 크루장, 부크루장
    boolean isAuthor = course.getUserId().equals(currentUserId);
    boolean isManager = permissionService.isManager(crewId, currentUserId);
    
    if (!isAuthor && !isManager) {
        throw new RuntimeException("권한이 없습니다");
    }
    
    // 삭제 로직...
}
```

---

## 💻 구현 예시

### 예시 1: BoardController 수정

```java
package com.secondwind.controller;

import com.secondwind.service.CrewPermissionService;
// ... 기타 imports

@RestController
@RequestMapping("/crew")
public class BoardController {

    private final CrewPermissionService permissionService;
    // ... 기타 dependencies

    public BoardController(CrewPermissionService permissionService, ...) {
        this.permissionService = permissionService;
        // ... 기타 초기화
    }

    @DeleteMapping("/{crewId}/posts/{postId}")
    @Transactional
    public void deletePost(@PathVariable Long crewId, @PathVariable Long postId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var userAuth = userRepository.findByEmail(email);
        
        if (userAuth == null) {
            throw new RuntimeException("User not found");
        }
        
        Long currentUserId = userAuth.getId();
        
        // 게시글 조회
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found"));
        
        // 권한 체크: 작성자, 크루장, 부크루장
        boolean isAuthor = post.getUserId().equals(currentUserId);
        boolean isManager = permissionService.isManager(crewId, currentUserId);
        
        if (!isAuthor && !isManager) {
            throw new RuntimeException("게시글을 삭제할 권한이 없습니다");
        }
        
        // 삭제
        postRepository.delete(post);
    }

    @PostMapping("/{crewId}/posts")
    @Transactional
    public PostDTO createPost(@PathVariable Long crewId, @RequestBody PostDTO postDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var userAuth = userRepository.findByEmail(email);
        
        if (userAuth == null) {
            throw new RuntimeException("User not found");
        }
        
        Long currentUserId = userAuth.getId();
        
        // 공지사항인 경우 관리자 권한 필요
        if (Boolean.TRUE.equals(postDTO.getIsNotice())) {
            permissionService.requireManager(crewId, currentUserId);
        } else {
            // 일반 게시글은 승인된 멤버면 가능
            permissionService.requireMember(crewId, currentUserId);
        }
        
        // 게시글 생성 로직...
        Post post = new Post();
        post.setCrewId(crewId);
        post.setUserId(currentUserId);
        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());
        post.setIsNotice(postDTO.getIsNotice());
        // ... 기타 설정
        
        Post savedPost = postRepository.save(post);
        
        // DTO 변환 및 반환
        return convertToDTO(savedPost);
    }
}
```

### 예시 2: CrewMemberController 수정

```java
@PostMapping("/{crewId}/members/{userId}/approve")
@Transactional
public CrewMemberDTO approveMember(@PathVariable Long crewId, @PathVariable Long userId) {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    var userAuth = userRepository.findByEmail(email);
    
    if (userAuth == null) {
        throw new RuntimeException("User not found");
    }
    
    Long currentUserId = userAuth.getId();
    
    // 변경: 크루장 또는 부크루장만 가능
    permissionService.requireManager(crewId, currentUserId);
    
    // 멤버 승인 로직 (기존과 동일)
    CrewMember member = crewMemberRepository.findByCrewIdAndUserId(crewId, userId)
        .orElseThrow(() -> new RuntimeException("Member not found"));
    
    member.setStatus("APPROVED");
    CrewMember updatedMember = crewMemberRepository.save(member);
    
    // ... 나머지 로직 (자동 팔로우, 알림 등)
    
    return convertToDTO(updatedMember);
}
```

### 예시 3: CrewCourseController 수정

```java
@DeleteMapping("/{crewId}/courses/{courseId}")
@Transactional
public void deleteCourse(@PathVariable Long crewId, @PathVariable Long courseId) {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    var userAuth = userRepository.findByEmail(email);
    
    if (userAuth == null) {
        throw new RuntimeException("User not found");
    }
    
    Long currentUserId = userAuth.getId();
    
    // 코스 조회
    CrewCourse course = crewCourseRepository.findById(courseId)
        .orElseThrow(() -> new RuntimeException("Course not found"));
    
    // 권한 체크: 작성자, 크루장, 부크루장
    boolean isAuthor = course.getUserId().equals(currentUserId);
    boolean isManager = permissionService.isManager(crewId, currentUserId);
    
    if (!isAuthor && !isManager) {
        throw new RuntimeException("코스를 삭제할 권한이 없습니다");
    }
    
    // 삭제
    crewCourseRepository.delete(course);
}
```

---

## 🎨 프론트엔드 연동

### 1. 사용자 권한 정보 가져오기

백엔드에 API 추가:

```java
@GetMapping("/{crewId}/my-role")
public Map<String, Object> getMyRole(@PathVariable Long crewId) {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    var userAuth = userRepository.findByEmail(email);
    
    if (userAuth == null) {
        throw new RuntimeException("User not found");
    }
    
    Long userId = userAuth.getId();
    String role = permissionService.getMemberRole(crewId, userId);
    
    boolean isCaptain = permissionService.isCaptain(crewId, userId);
    boolean isViceCaptain = permissionService.isViceCaptain(crewId, userId);
    boolean isManager = permissionService.isManager(crewId, userId);
    
    return Map.of(
        "role", role != null ? role : "none",
        "isCaptain", isCaptain,
        "isViceCaptain", isViceCaptain,
        "isManager", isManager
    );
}
```

### 2. 프론트엔드에서 사용

```javascript
// CrewDetailPage.jsx
const [userRole, setUserRole] = useState(null);

useEffect(() => {
  const fetchUserRole = async () => {
    try {
      const response = await axios.get(`/crew/${crewId}/my-role`);
      setUserRole(response.data);
    } catch (error) {
      console.error('Failed to fetch user role:', error);
    }
  };
  
  fetchUserRole();
}, [crewId]);

// 권한에 따라 UI 표시
{userRole?.isManager && (
  <button onClick={handleApproveMember}>멤버 승인</button>
)}

{userRole?.isCaptain && (
  <button onClick={handleKickMember}>멤버 강퇴</button>
)}

{(userRole?.isManager || isAuthor) && (
  <button onClick={handleDeletePost}>게시글 삭제</button>
)}
```

### 3. 역할 표시

```javascript
// CrewMemberList.jsx
const getRoleBadge = (role) => {
  switch (role) {
    case 'captain':
      return <span className="badge badge-primary">크루장</span>;
    case 'vice_captain':
      return <span className="badge badge-secondary">부크루장</span>;
    case 'member':
      return <span className="badge badge-light">멤버</span>;
    default:
      return null;
  }
};

// 멤버 목록 렌더링
{members.map(member => (
  <div key={member.id} className="member-item">
    <img src={member.nicknameImage} alt={member.nickname} />
    <span>{member.nickname}</span>
    {getRoleBadge(member.role)}
  </div>
))}
```

---

## 📋 구현 체크리스트

### 백엔드

```
✅ CrewPermissionService.java 생성
□ CrewMemberController.java 수정
  □ approveMember - 부크루장 권한 추가
  □ rejectMember - 부크루장 권한 추가
  □ kickMember - 크루장만 유지
  □ updateMemberRole - 크루장만 유지
  
□ BoardController.java 수정
  □ deletePost - 부크루장 권한 추가
  □ deleteComment - 부크루장 권한 추가
  □ createPost - 공지사항은 관리자만
  
□ CrewCourseController.java 수정
  □ deleteCourse - 부크루장 권한 추가
  
□ API 추가
  □ GET /{crewId}/my-role - 사용자 권한 조회
```

### 프론트엔드

```
□ 사용자 권한 조회 API 연동
□ 권한에 따른 UI 표시/숨김
  □ 멤버 승인/거절 버튼
  □ 게시글 삭제 버튼
  □ 댓글 삭제 버튼
  □ 코스 삭제 버튼
  □ 공지사항 작성 버튼
  
□ 역할 배지 표시
  □ 크루장 배지
  □ 부크루장 배지
  □ 일반 멤버 표시
```

---

## 🎯 요약

### 부크루장이 할 수 있는 일

✅ **멤버 관리**
- 가입 신청 승인/거절

✅ **게시판 관리**
- 모든 게시글 삭제
- 모든 댓글 삭제
- 공지사항 작성

✅ **코스 관리**
- 모든 코스 삭제

### 부크루장이 할 수 없는 일

❌ **크루 관리**
- 크루 삭제
- 크루 정보 수정

❌ **멤버 관리**
- 멤버 강퇴
- 부크루장 임명
- 역할 변경

### 구현 위치

- **서비스**: `CrewPermissionService.java` (새로 생성) ✅
- **컨트롤러**: 기존 Controller들 수정 필요
  - `CrewMemberController.java`
  - `BoardController.java`
  - `CrewCourseController.java`

---

**다음 단계**: 각 Controller를 수정하여 `CrewPermissionService`를 사용하도록 업데이트하세요!
