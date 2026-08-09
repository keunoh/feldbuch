# Feldbuch API

> Base URL: `http://localhost:8080`
>
> 인증 방식: `Authorization: Bearer <accessToken>`

현재 서버에 남아 있는 API 표면은 Auth, User, Conversation, Knowledge입니다. Postman 컬렉션도 같은 그룹으로 정리합니다. 일반 응답은 `ApiResponse<T>` 형식이며, SSE 스트리밍 응답만 `ApiResponse<T>`로 감싸지 않습니다.

```json
{
  "success": true,
  "data": {}
}
```

## Auth

### 로그인

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "email": "test@example.com",
  "password": "password123"
}
```

응답:

```json
{
  "success": true,
  "data": {
    "userId": 1,
    "accessToken": "jwt-token",
    "refreshToken": "refresh-token",
    "tokenType": "Bearer"
  }
}
```

### Access Token 재발급

```http
POST /api/auth/refresh
Content-Type: application/json
```

인증 헤더 없이 로그인 시 받은 Refresh Token으로 새 Access Token을 발급합니다.

```json
{
  "refreshToken": "refresh-token"
}
```

응답:

```json
{
  "success": true,
  "data": {
    "accessToken": "new-jwt-token",
    "tokenType": "Bearer"
  }
}
```

### 현재 인증 사용자 조회

```http
GET /api/auth/me
Authorization: Bearer <accessToken>
```

JWT 토큰 기준으로 현재 로그인 사용자와 인증 Provider를 조회합니다.

응답:

```json
{
  "success": true,
  "data": {
    "userId": 1,
    "email": "user@example.com",
    "nickname": "홍길동",
    "role": "USER",
    "provider": "LOCAL"
  }
}
```

## User

### 회원가입

```http
POST /api/users/signup
Content-Type: application/json
```

```json
{
  "email": "test@example.com",
  "password": "password123",
  "nickname": "testuser"
}
```

요청 제약:

| 필드 | 타입 | 필수 | 제약 |
| --- | --- | --- | --- |
| `email` | string | Y | 이메일 형식 |
| `password` | string | Y | 8-20자 |
| `nickname` | string | Y | 2-20자 |

응답:

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

### 내 정보 조회

```http
GET /api/users/me
Authorization: Bearer <accessToken>
```

사용자 프로필 정보를 조회합니다. 인증 Provider까지 필요한 화면은 `GET /api/auth/me`를 사용합니다.

응답:

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

## Conversation

모든 Conversation API는 JWT 인증이 필요합니다.

### 대화 생성

```http
POST /api/conversations
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{
  "title": "새 대화"
}
```

요청 제약:

| 필드 | 타입 | 필수 | 제약 |
| --- | --- | --- | --- |
| `title` | string | Y | 100자 이하 |

응답 `data`는 생성된 Conversation ID입니다.

```json
{
  "success": true,
  "data": 1
}
```

### 대화 목록 조회

```http
GET /api/conversations
Authorization: Bearer <accessToken>
```

응답:

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "새 대화",
      "status": "ACTIVE",
      "createdAt": "2026-08-06T10:00:00"
    }
  ]
}
```

### 대화 상세 조회

```http
GET /api/conversations/{conversationId}
Authorization: Bearer <accessToken>
```

응답:

```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "새 대화",
    "status": "ACTIVE",
    "createdAt": "2026-08-06T10:00:00",
    "updatedAt": "2026-08-06T10:05:00",
    "messages": [
      {
        "id": 1,
        "role": "USER",
        "content": "Spring Boot에 대해 설명해주세요.",
        "createdAt": "2026-08-06T10:01:00"
      }
    ],
    "messageCount": 1
  }
}
```

### 대화 제목 수정

```http
PATCH /api/conversations/{conversationId}
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{
  "title": "수정된 대화 제목"
}
```

요청 제약:

| 필드 | 타입 | 필수 | 제약 |
| --- | --- | --- | --- |
| `title` | string | Y | 100자 이하 |

응답:

```json
{
  "success": true,
  "data": null
}
```

### 대화 삭제

```http
DELETE /api/conversations/{conversationId}
Authorization: Bearer <accessToken>
```

해당 대화와 대화에 속한 메시지를 삭제합니다.

응답:

```json
{
  "success": true,
  "data": null
}
```

### 대화 메시지 생성

```http
POST /api/conversations/{conversationId}/messages
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{
  "content": "안녕하세요, 질문이 있습니다."
}
```

응답 `data`는 생성된 메시지 ID입니다.

```json
{
  "success": true,
  "data": 1
}
```

### 대화 메시지 목록 조회

```http
GET /api/conversations/{conversationId}/messages
Authorization: Bearer <accessToken>
```

응답:

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "role": "USER",
      "content": "안녕하세요, 질문이 있습니다.",
      "createdAt": "2026-08-06T10:01:00"
    }
  ]
}
```

### 대화형 AI 요청

```http
POST /api/conversations/{conversationId}/chat
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{
  "message": "Spring Boot에 대해 설명해주세요."
}
```

응답:

```json
{
  "success": true,
  "data": {
    "content": "Spring Boot는 Spring 기반 애플리케이션을 빠르게 만들기 위한 프레임워크입니다."
  }
}
```

### 대화형 AI SSE 스트리밍

```http
POST /api/conversations/{conversationId}/chat/stream
Authorization: Bearer <accessToken>
Content-Type: application/json
Accept: text/event-stream
```

```json
{
  "message": "Spring Boot에 대해 설명해주세요."
}
```

SSE 이벤트:

```json
{"type":"TOKEN","content":"Spring"}
{"type":"COMPLETE","content":null}
{"type":"ERROR","content":"오류 메시지"}
```

## Knowledge

모든 Knowledge API는 JWT 인증이 필요합니다. KnowledgeNote는 Batch가 완료된 Conversation에서 자동 생성하며, 수동 생성/수정 API는 아직 없습니다.

### Knowledge 폴더 트리 조회

```http
GET /api/knowledge/tree
Authorization: Bearer <accessToken>
```

응답:

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "프로그래밍",
      "children": [
        {
          "id": 2,
          "name": "Spring Batch",
          "children": []
        }
      ]
    }
  ]
}
```

### Knowledge 폴더별 노트 목록 조회

```http
GET /api/knowledge/{knowledgeId}/notes
Authorization: Bearer <accessToken>
```

응답:

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "Spring Boot 개요",
      "summary": "핵심 요약 내용",
      "createdAt": "2026-01-01T00:00:00"
    }
  ]
}
```

### Knowledge 노트 상세 조회

```http
GET /api/knowledge/notes/{noteId}
Authorization: Bearer <accessToken>
```

응답:

```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "Spring Boot 개요",
    "description": "Spring Boot는 ...",
    "summary": "핵심 요약 내용",
    "keywords": ["Spring", "Boot", "Java"]
  }
}
```

### Conversation 통합 Knowledge 노트 조회

```http
GET /api/knowledge/conversations/{conversationId}/consolidated-note
Authorization: Bearer <accessToken>
```

같은 Conversation에서 누적 병합된 `CONSOLIDATED` KnowledgeNote를 조회합니다.

응답:

```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "대화 요약 노트",
    "description": "이 대화에서 다룬 내용 ...",
    "summary": "핵심 요약",
    "keywords": ["Spring", "JPA"]
  }
}
```

## Enum

| Enum | 값 |
| --- | --- |
| `UserRole` | `USER`, `ADMIN` |
| `ConversationStatus` | `ACTIVE`, `COMPLETED` |
| `ConversationRole` | `USER`, `ASSISTANT` |
| `StreamType` | `TOKEN`, `COMPLETE`, `ERROR` |
| `KnowledgeExtractStatus` | `NONE`, `PROCESSING`, `COMPLETED`, `FAILED` |
| `KnowledgeNoteType` | `INCREMENTAL`, `CONSOLIDATED` |
| `KnowledgeRootCategory` | `COMPUTER_SCIENCE`, `PROGRAMMING_LANGUAGE`, `WEB_DEVELOPMENT`, `DATABASE`, `NETWORK`, `OPERATING_SYSTEM`, `CLOUD`, `DEVOPS`, `ARTIFICIAL_INTELLIGENCE`, `SECURITY`, `COMPUTER_USAGE`, `COMMUNICATION` |
| `KnowledgeCategory` | `SPRING`, `SPRING_BOOT`, `SPRING_BATCH`, `SPRING_SECURITY`, `SPRING_WEBFLUX`, `VUE`, `REACT`, `JAVASCRIPT`, `TYPESCRIPT`, `JAVA`, `KOTLIN`, `PYTHON`, `JPA`, `QUERYDSL`, `MYSQL`, `ORACLE`, `MSSQL`, `REDIS`, `SQL`, `DOCKER`, `KUBERNETES`, `CI_CD`, `GITHUB_ACTIONS`, `AWS`, `NETWORK_GENERAL`, `OPERATING_SYSTEM_GENERAL`, `SECURITY_GENERAL`, `ARTIFICIAL_INTELLIGENCE_GENERAL`, `COMPUTER_USAGE_GENERAL`, `COMPUTER_SCIENCE_GENERAL`, `COMMUNICATION_GENERAL` |

## 삭제 로그

- Note CRUD/Search/Pin/StudyStatus API 제거
- 노트 AI 요약 요청 `POST /api/ai/notes/{noteId}/summary` 제거
- AI Job 상태 조회 `GET /api/ai/jobs/{jobId}` 제거
- `NoteCategory`, `StudyStatus`, `AiSummaryStatus`, `AiJobType`, `AiJobStatus` enum 문서 제거
- Postman의 Note/AI 요청 파일은 현재 코드 기준 실행 가능한 API가 아니므로 문서의 주요 API 표면에서 제외
