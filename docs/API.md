# 📑 Feldbuch API 문서

> **Base URL:** `http://localhost:8080`
>
> **인증 방식:** Bearer Token (`Authorization: Bearer <accessToken>`)
>
> 🔒 표시된 엔드포인트는 JWT 인증이 필요합니다.

---

## 목차

- [Auth](#-auth)
- [User](#-user)
- [Note](#-note)
- [AI](#-ai)
- [공통 응답 형식](#-공통-응답-형식)
- [Enum 타입](#-enum-타입)

---

## 🔐 Auth

### 로그인

```
POST /api/auth/login
```

**Request Body**

```json
{
  "email": "test@example.com",
  "password": "password123"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| email | String | ✅ | 이메일 형식 |
| password | String | ✅ | 비밀번호 |

**Response `200 OK`**

```json
{
  "success": true,
  "data": {
    "userId": 1,
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer"
  }
}
```

---

## 👤 User

### 회원가입

```
POST /api/users/signup
```

**Request Body**

```json
{
  "email": "test@example.com",
  "password": "password123",
  "nickname": "testuser"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| email | String | ✅ | 이메일 형식 |
| password | String | ✅ | 8~20자 |
| nickname | String | ✅ | 2~20자 |

**Response `201 Created`**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "test@example.com",
    "nickname": "testuser"
  }
}
```

---

### 내 정보 조회 🔒

```
GET /api/users/me
```

**Headers**

```
Authorization: Bearer <accessToken>
```

**Response `200 OK`**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "test@example.com",
    "nickname": "testuser",
    "role": "USER"
  }
}
```

---

## 📒 Note

> 모든 Note API는 JWT 인증이 필요합니다.

**Headers (공통)**

```
Authorization: Bearer <accessToken>
```

---

### 노트 생성 🔒

```
POST /api/notes
```

**Request Body**

```json
{
  "title": "Spring Boot 학습 노트",
  "content": "Spring Boot 3.x 기반 백엔드 개발 내용 정리",
  "category": "STUDY"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| title | String | ✅ | 노트 제목 |
| content | String | ✅ | 노트 내용 |
| category | NoteCategory | ✅ | 카테고리 ([Enum 참고](#-enum-타입)) |

**Response `200 OK`**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "Spring Boot 학습 노트",
    "content": "Spring Boot 3.x 기반 백엔드 개발 내용 정리",
    "summary": null,
    "category": "STUDY",
    "pinned": false,
    "studyStatus": "TODO"
  }
}
```

---

### 노트 목록 검색 🔒

```
GET /api/notes
```

**Query Parameters**

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| keyword | String | ❌ | 검색 키워드 (제목/내용) |
| page | int | ❌ | 페이지 번호 (기본값: 0) |
| size | int | ❌ | 페이지 크기 (기본값: 20) |

**Example**

```
GET /api/notes?keyword=Spring&page=0&size=20
```

**Response `200 OK`**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "title": "Spring Boot 학습 노트",
        "summary": "AI 요약 내용",
        "category": "STUDY",
        "pinned": false,
        "studyStatus": "TODO"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

### 노트 단건 조회 🔒

```
GET /api/notes/{noteId}
```

**Path Variables**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| noteId | Long | 노트 ID |

**Response `200 OK`**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "Spring Boot 학습 노트",
    "content": "Spring Boot 3.x 기반 백엔드 개발 내용 정리",
    "summary": "AI 요약 내용",
    "category": "STUDY",
    "pinned": false,
    "studyStatus": "TODO"
  }
}
```

---

### 노트 수정 🔒

```
PATCH /api/notes/{noteId}
```

**Path Variables**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| noteId | Long | 노트 ID |

**Request Body**

```json
{
  "title": "Spring Boot 학습 노트",
  "content": "Spring Boot 3.x 기반 백엔드 개발 내용 정리",
  "category": "STUDY"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| title | String | ✅ | 노트 제목 |
| content | String | ✅ | 노트 내용 |
| category | NoteCategory | ✅ | 카테고리 ([Enum 참고](#-enum-타입)) |

**Response `200 OK`**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "Spring Boot 학습 노트",
    "content": "Spring Boot 3.x 기반 백엔드 개발 내용 정리",
    "summary": "AI 요약 내용",
    "category": "STUDY",
    "pinned": false,
    "studyStatus": "TODO"
  }
}
```

---

### 노트 삭제 🔒

```
DELETE /api/notes/{noteId}
```

**Path Variables**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| noteId | Long | 노트 ID |

**Response `200 OK`**

```json
{
  "success": true,
  "data": null
}
```

---

### 핀 상태 변경 🔒

```
PATCH /api/notes/{noteId}/pin
```

**Path Variables**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| noteId | Long | 노트 ID |

**Request Body**

```json
{
  "pinned": true
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| pinned | boolean | ✅ | 핀 여부 |

**Response `200 OK`**

```json
{
  "success": true,
  "data": null
}
```

---

### 학습 상태 변경 🔒

```
PATCH /api/notes/{noteId}/study-status
```

**Path Variables**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| noteId | Long | 노트 ID |

**Request Body**

```json
{
  "studyStatus": "IN_PROGRESS"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| studyStatus | StudyStatus | ✅ | 학습 상태 ([Enum 참고](#-enum-타입)) |

**Response `200 OK`**

```json
{
  "success": true,
  "data": null
}
```

---

## 🤖 AI

### 노트 AI 요약 요청 🔒

```
POST /api/ai/notes/{noteId}/summary
```

비동기로 처리됩니다. 요청 즉시 응답을 반환하며, AI 요약은 백그라운드에서 처리됩니다.

**Headers**

```
Authorization: Bearer <accessToken>
```

**Path Variables**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| noteId | Long | 요약할 노트 ID |

**Response `200 OK`**

```json
{
  "success": true,
  "data": null
}
```

> 요약 완료 후 `GET /api/notes/{noteId}` 로 조회하면 `summary` 필드에 결과가 반영됩니다.

---

## 📦 공통 응답 형식

모든 API는 `ApiResponse<T>` 형식으로 응답합니다.

```json
{
  "success": true,
  "data": { ... }
}
```

### 에러 응답

```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "에러 메시지"
  }
}
```

---

## 🏷️ Enum 타입

### NoteCategory

| 값 | 설명 |
|----|------|
| `STUDY` | 학습 내용 |
| `ERROR` | 트러블슈팅 |
| `ENVIRONMENT` | 환경 설정 |
| `AI` | AI 관련 |
| `MEMO` | 메모 |

### StudyStatus

| 값 | 설명 |
|----|------|
| `TODO` | 학습 예정 |
| `IN_PROGRESS` | 학습 중 |
| `DONE` | 학습 완료 |
