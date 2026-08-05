# Feldbuch API

> Base URL: `http://localhost:8080`
>
> 인증 방식: `Authorization: Bearer <accessToken>`

Postman 환경은 `postman/environments/Feldbuch Local.yaml`을 기준으로 합니다.

| 변수 | 기본값 | 설명 |
| --- | --- | --- |
| `baseUrl` | `http://localhost:8080` | 로컬 API 서버 |
| `accessToken` | secret | 로그인 후 발급받은 JWT |
| `noteId` | `1` | Note 요청용 Path Variable |
| `conversationId` | `1` | Conversation 요청용 Path Variable |
| `jobId` | `1` | AI Job 요청용 Path Variable |

일반 응답은 `ApiResponse<T>` 형식입니다. SSE 스트리밍 응답은 `ApiResponse<T>`로 감싸지 않습니다.

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
    "tokenType": "Bearer"
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

## Note

모든 Note API는 JWT 인증이 필요합니다.

### 노트 생성

```http
POST /api/notes
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{
  "title": "Spring Boot 학습 노트",
  "content": "Spring Boot 3.x 기반 백엔드 개발 내용 정리",
  "category": "STUDY"
}
```

응답:

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

### 노트 검색

```http
GET /api/notes?keyword=Spring&page=0&size=20
Authorization: Bearer <accessToken>
```

Query Parameters:

| 이름 | 필수 | 설명 |
| --- | --- | --- |
| `keyword` | 아니오 | 제목/내용 검색어 |
| `page` | 아니오 | 페이지 번호, 기본 `0` |
| `size` | 아니오 | 페이지 크기, 기본 `20` |

### 노트 단건 조회

```http
GET /api/notes/{noteId}
Authorization: Bearer <accessToken>
```

### 노트 수정

```http
PATCH /api/notes/{noteId}
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{
  "title": "Spring Boot 학습 노트",
  "content": "Spring Boot 3.x 기반 백엔드 개발 내용 정리",
  "category": "STUDY"
}
```

### 노트 삭제

```http
DELETE /api/notes/{noteId}
Authorization: Bearer <accessToken>
```

응답:

```json
{
  "success": true,
  "data": null
}
```

### 핀 상태 변경

```http
PATCH /api/notes/{noteId}/pin
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{
  "pinned": true
}
```

### 학습 상태 변경

```http
PATCH /api/notes/{noteId}/study-status
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{
  "studyStatus": "IN_PROGRESS"
}
```

## AI

### 노트 AI 요약 요청

```http
POST /api/ai/notes/{noteId}/summary
Authorization: Bearer <accessToken>
```

비동기 AI 요약 Job을 만들고 `jobId`를 반환합니다.

```json
{
  "success": true,
  "data": 1
}
```

### AI Job 상태 조회

```http
GET /api/ai/jobs/{jobId}
Authorization: Bearer <accessToken>
```

응답:

```json
{
  "success": true,
  "data": {
    "jobId": 1,
    "noteId": 10,
    "type": "SUMMARY",
    "status": "COMPLETED",
    "requestedAt": "2026-08-05T10:00:00",
    "startedAt": "2026-08-05T10:00:01",
    "completedAt": "2026-08-05T10:00:05",
    "errorMessage": null
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

응답:

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
      "createdAt": "2026-08-05T10:00:00"
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
    "createdAt": "2026-08-05T10:00:00",
    "updatedAt": "2026-08-05T10:05:00",
    "messages": [
      {
        "id": 1,
        "role": "USER",
        "content": "Spring Boot에 대해 설명해주세요.",
        "createdAt": "2026-08-05T10:01:00"
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

### 대화 삭제

```http
DELETE /api/conversations/{conversationId}
Authorization: Bearer <accessToken>
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

### 대화 메시지 목록 조회

```http
GET /api/conversations/{conversationId}/messages
Authorization: Bearer <accessToken>
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

일반 채팅 요청은 사용자 메시지와 Assistant 응답을 저장한 뒤 응답 본문을 반환합니다.

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

모든 Knowledge API는 JWT 인증이 필요합니다. Postman에는 아직 Knowledge 요청 파일이 완성되지 않았지만, 서버에는 아래 조회 API가 구현되어 있습니다.

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
      "name": "WEB_DEVELOPMENT",
      "children": [
        {
          "id": 2,
          "name": "Spring Boot",
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
      "id": 10,
      "title": "Spring Boot 자동 설정",
      "summary": "자동 설정의 조건과 적용 순서를 정리합니다.",
      "createdAt": "2026-08-05T10:30:00"
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
    "id": 10,
    "title": "Spring Boot 자동 설정",
    "description": "자동 설정이 적용되는 흐름을 요약한 노트",
    "summary": "AutoConfiguration은 조건부 Bean 등록을 통해 기본 구성을 제공합니다.",
    "keywords": ["Spring Boot", "AutoConfiguration"]
  }
}
```

### Conversation 통합 Knowledge 노트 조회

```http
GET /api/knowledge/conversations/{conversationId}/consolidated-note
Authorization: Bearer <accessToken>
```

같은 Conversation에서 누적 병합된 `CONSOLIDATED` KnowledgeNote를 조회합니다.

## Postman Collection

현재 Postman 컬렉션에 포함된 요청:

| 그룹 | 요청 |
| --- | --- |
| Auth | Login |
| User | Signup, Get My Info |
| Note | Create, Search, Get, Update, Delete, Update Pin, Update Study Status |
| AI | Summarize Note, Summarize Job Status |
| Conversation | Create, Get, Get All, Update, Delete, Create Message, Get Messages, Create Chat, Chat Stream |
| Knowledge | `Get Merged Knowledge` 파일은 존재하지만 URL이 비어 있어 아직 실행 가능한 요청이 아닙니다. |

## Enum

| Enum | 값 |
| --- | --- |
| `NoteCategory` | `STUDY`, `ERROR`, `ENVIRONMENT`, `AI`, `MEMO` |
| `StudyStatus` | `TODO`, `IN_PROGRESS`, `DONE` |
| `AiSummaryStatus` | `NONE`, `PENDING`, `COMPLETED`, `FAILED` |
| `AiJobType` | `SUMMARY`, `TAG`, `QUIZ`, `REVIEW`, `ROADMAP` |
| `AiJobStatus` | `REQUESTED`, `PROCESSING`, `COMPLETED`, `FAILED` |
| `ConversationStatus` | `ACTIVE`, `COMPLETED` |
| `ConversationRole` | `USER`, `ASSISTANT` |
| `StreamType` | `TOKEN`, `COMPLETE`, `ERROR` |
| `KnowledgeExtractStatus` | `NONE`, `PROCESSING`, `COMPLETED`, `FAILED` |
| `KnowledgeNoteType` | `INCREMENTAL`, `CONSOLIDATED` |
| `KnowledgeRootCategory` | `COMPUTER_SCIENCE`, `PROGRAMMING_LANGUAGE`, `WEB_DEVELOPMENT`, `DATABASE`, `NETWORK`, `OPERATING_SYSTEM`, `CLOUD`, `DEVOPS`, `ARTIFICIAL_INTELLIGENCE`, `SECURITY`, `COMPUTER_USAGE`, `COMMUNICATION` |
