# FELDBUCH DEVELOPMENT DOCUMENTATION

> AI 기반 개발 학습 노트 서비스 Feldbuch의 현재 구현, 개발 흐름, 아키텍처를 정리한 문서입니다.

## Project Overview

Feldbuch는 개발자가 학습하며 얻은 지식, 트러블슈팅, 코드, 환경 설정을 기록하고 검색할 수 있는 개발 지식 관리 플랫폼입니다.

현재 구현은 대화 중심 학습 흐름을 기준으로 구성되어 있습니다. 사용자는 Vue SPA에서 AI와 개발 학습 대화를 나누고, 완료된 대화는 Batch를 통해 Knowledge 폴더와 KnowledgeNote로 추출됩니다. 추출 결과는 매 실행마다 생성되는 증분 노트와 Conversation 단위로 누적 병합되는 통합 노트로 나뉩니다.

## Current Product Surface

메인 사용자 화면은 `frontend/src/views/ConversationView.vue`입니다.

- `WorkspaceSidebar`가 왼쪽 고정 영역에서 `대화`와 `지식` 탭을 전환합니다.
- `대화` 모드에서는 대화 목록, 채팅 메시지, 입력창, 선택 대화의 학습 정보 패널을 렌더링합니다.
- `지식` 모드에서는 Knowledge 폴더 트리, 선택된 폴더의 KnowledgeNote 목록, 선택한 노트의 상세 요약과 키워드를 함께 렌더링합니다.
- Knowledge 폴더는 서버가 고정 대분류를 먼저 결정하고, AI가 기존 하위 폴더 재사용 또는 새 폴더 생성을 선택하는 방식으로 정리됩니다.
- Knowledge 폴더와 노트 목록은 검색을 지원하고, 검색어는 `SearchHighlight`로 강조합니다.
- 선택한 사이드바 모드, 대화, Knowledge 폴더, Knowledge 경로, Knowledge 노트는 `localStorage`에 저장해 새로고침 후 복원합니다.
- 로그아웃은 대화/지식 모드 양쪽 헤더에서 동일하게 제공됩니다.
- 대화 메시지 전송은 사용자 메시지와 빈 Assistant 메시지를 낙관적으로 추가한 뒤 SSE 토큰을 누적 표시하고, 완료 후 상세를 재조회합니다.
- 메시지가 저장될 때 Conversation은 ACTIVE 상태와 `lastMessageAt`을 갱신하며, 완료된 대화에 새 메시지가 추가되면 다음 증분 Knowledge 추출을 위해 추출 상태를 다시 `NONE`으로 준비합니다.

## Screens

![Feldbuch Main Chat Screen](./images/screenshots/feldbuch-main-chat-screen.png)

![Feldbuch Knowledge Notes Screen](./images/screenshots/feldbuch-knowledge-notes-screen.png)

## Tech Stack

| Category | Stack |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 3.5 |
| Security | Spring Security, JWT |
| Auth Config | Google OAuth2 client properties |
| Database | MySQL, H2 Test DB |
| ORM / Query | Spring Data JPA, QueryDSL |
| AI | OpenAI REST API, OpenAI SSE Streaming |
| Batch / Infra | Spring Batch, Redis, Docker Compose |
| Frontend | Vue 3, Vite, Vue Router, Axios, Fetch SSE, marked, highlight.js, DOMPurify |
| Legacy View | Thymeleaf, static CSS/JS |
| Test | JUnit 5, MockMvc, Spring Security Test, Spring Batch Test |

## Development Flow

```mermaid
flowchart TD
    subgraph Phase1["초기 기반"]
        A[프로젝트 생성] --> B[JWT 인증] --> C[회원가입/로그인]
    end

    subgraph Phase2["노트와 백엔드 구조"]
        D[노트 CRUD] --> E[QueryDSL 검색] --> F[CQRS/Reader/Mapper]
    end

    subgraph Phase3["AI 처리 기반"]
        G[OpenAI 연동] --> H[Async 처리] --> I[AI Job 상태 관리]
    end

    subgraph Phase4["대화 도메인"]
        J[Conversation] --> K[Conversation Message] --> L[대화 컨텍스트 채팅]
    end

    subgraph Phase5["Vue 전환"]
        M[Vue 3/Vite] --> N[Axios/Router Guard] --> O[SSE 스트리밍 채팅]
    end

    subgraph Phase6["Knowledge 구조"]
        P[Knowledge 대분류] --> Q[AI 폴더 재사용] --> R[증분/통합 KnowledgeNote]
    end

    C --> D
    F --> G
    I --> J
    L --> M
    O --> P
```

## Implemented Features

- Spring Security, JWT 로그인, JWT Claims 기반 `userId`, `email`, `role` 저장
- CustomUserDetails, JWT Filter, JWT AuthenticationEntryPoint 401 처리
- Vite 개발 서버 `http://localhost:5173` CORS 허용
- 회원가입, 로그인, 클라이언트 로그아웃
- 노트 생성, 조회, 수정, 삭제
- QueryDSL 검색, Pagination, Pin, StudyStatus
- Reader Pattern, Mapper Pattern, CQRS, Facade
- RestClient 기반 OpenAI 일반 요청
- WebClient 기반 OpenAI Chat Completion SSE 스트리밍
- AI Job Entity, Reader, Service, Controller
- AI Job 상태 조회 API
- Conversation Entity, Controller, Command/Query Service
- Conversation 제목 수정/삭제 API
- ConversationMessage Entity, Controller, Command/Query Service
- Conversation별 메시지 순서 저장
- Conversation별 마지막 메시지 활동 시각 저장
- Conversation 자동 완료 Scheduler와 Completion Service
- 비활성 ACTIVE Conversation 조회 QueryDSL 쿼리
- 대화 내역을 OpenAI Chat Completion 메시지 컨텍스트로 변환
- Conversation Chat API와 Conversation Chat Stream API
- `StreamResponse` 기반 `TOKEN`, `COMPLETE`, `ERROR` 스트리밍 이벤트 계약
- 첫 사용자 메시지 기반 Conversation 제목 자동 생성
- Conversation Knowledge 추출 상태 필드와 실패 재시도 메타데이터
- Conversation Knowledge 추출 체크포인트 `lastExtractedMessageId`
- Knowledge Entity, KnowledgeNote Entity, KnowledgeNote Keyword ElementCollection
- KnowledgeRootCategory 기반 고정 대분류 체계
- KnowledgeNoteType 기반 `INCREMENTAL`, `CONSOLIDATED` 노트 분리
- KnowledgeRepository, KnowledgeNoteRepository
- KnowledgePathResolver
- KnowledgeRootCategoryResolver
- KnowledgeFolderSelectionService, OpenAiKnowledgeFolderSelectionService
- KnowledgeFolderCandidate, AiKnowledgeFolderSelectionResponse
- AiKnowledgeSummaryResponse
- AiKnowledgeMergeResponse
- KnowledgeSummaryPrompt
- KnowledgeFolderSelectionPrompt
- KnowledgeMergePrompt
- AiKnowledgeSummaryService, OpenAiKnowledgeSummaryService
- AiKnowledgeMergeService, OpenAiKnowledgeMergeService
- KnowledgeExtractionService, KnowledgeExtractionStatusService
- ConversationAiContextBuilder
- KnowledgeConversationReader
- KnowledgeExtractionBatchConfig, KnowledgeExtractionTasklet, LocalKnowledgeExtractionJobRunner
- KnowledgeExtractionScheduler
- 사용자별 Knowledge 루트/자식 조회, 동일 폴더명 중복 확인 쿼리
- QueryDSL 기반 Knowledge 추출 대상 Conversation 조회와 대상 존재 여부 확인 쿼리
- KnowledgeNote의 Knowledge별/Conversation별/사용자별/타입별 조회 쿼리
- Knowledge Tree 조회 API, KnowledgeNote 목록/상세 조회 API, Conversation별 통합 노트 조회 API
- Thymeleaf 기반 로그인/대화 비교 화면
- Vue 3 + Vite SPA: `LoginView`, `ConversationView`
- Vue 컴포넌트: `WorkspaceSidebar`, `SidebarHeader`, `SidebarTabs`, `SidebarSectionLabel`, `ConversationSidebar`, `MessageList`, `ChatInput`, `StudyInfoPanel`, `KnowledgeSidebar`, `KnowledgeTreeNode`, `KnowledgeWorkspace`, `KnowledgeNoteList`, `KnowledgeNoteDetail`, `SearchHighlight`
- 새 대화 생성, 대화 제목 인라인 수정, 대화 삭제 UI
- 대화 생성/수정/삭제/메시지 전송 중복 요청 방지 상태
- 메시지 전송 중 AI 응답 작성 로딩 표시
- 메시지 목록 자동 스크롤
- AI 응답 Markdown 렌더링, highlight.js 코드 문법 강조, DOMPurify sanitize
- 코드 블록 언어 표시와 클립보드 COPY 버튼
- Knowledge 폴더 검색, 노트 검색, 검색 결과 없음 상태 표시
- Knowledge 검색어 하이라이트
- Knowledge breadcrumb 표시
- Knowledge 노트 목록 선택 상태 표시
- Knowledge 노트 상세 화면에서 제목, 설명, 요약, 키워드 표시
- Postman 컬렉션의 Bearer 인증, Path Variable 환경 변수화, Conversation Chat Stream 요청 정리
- 사이드바 모드, 선택 대화, 선택 Knowledge 폴더, 선택 Knowledge 경로, 선택 Knowledge 노트 localStorage 저장/복원
- Request ID Filter, SLF4J MDC 기반 requestId 저장/해제, `X-Request-Id` 응답 헤더
- Axios 공통 API client와 Request/Response Interceptor
- Vue Router Guard 기반 인증 라우트 보호
- `localStorage` 기반 `accessToken`, `userId` 저장/삭제 유틸리티
- RedisTemplate 구성
- Spring Batch Summary Job 구성
- 주요 도메인/서비스/컨트롤러 통합 테스트와 Knowledge 추출 Batch 테스트

## Runtime Configuration

- 기본 활성 프로필: `local`
- 공통 설정 파일: `src/main/resources/application.yml`
- 로컬 설정 파일: `src/main/resources/application-local.yml`
- 운영 설정 파일: `src/main/resources/application-prod.yml`
- OpenAI Base URL: `https://api.openai.com/v1`
- OpenAI 모델 설정 키: `openai.model`
- 현재 기본 모델: `gpt-4.1-nano`
- Google OAuth2 client-id 설정 키: `GOOGLE_CLIENT_ID`
- Google OAuth2 client-secret 설정 키: `GOOGLE_CLIENT_SECRET`
- Google OAuth2 scope: `openid`, `profile`, `email`
- 현재 Spring Security와 Vue 로그인 흐름은 JWT 폼 로그인 중심이며, OAuth2 인증 플로우는 아직 연결하지 않았습니다.
- 로컬 Docker 인프라: MySQL, Redis
- Spring Batch 기본 자동 실행: `spring.batch.job.enabled=false`
- Knowledge 추출 배치 로컬 실행 플래그: `feldbuch.batch.knowledge-extraction.run=true`
- Knowledge 추출 스케줄러 간격 설정 키: `batch.knowledge-extraction.fixed-delay`
- Knowledge 추출 스케줄러 기본 간격: `60000` ms
- Knowledge 추출 배치 Job 이름: `knowledgeExtractionJob`
- Knowledge 추출 배치 Step 이름: `knowledgeExtractionStep`
- Conversation 자동 완료 스케줄러 간격 설정 키: `conversation.auto-completion.fixed-delay`
- Conversation 자동 완료 스케줄러 기본 간격: `60000` ms
- Conversation 자동 완료 비활성 시간 설정 키: `conversation.auto-completion.inactivity-timeout`
- Conversation 자동 완료 기본 비활성 시간: `30m`

## Architecture

### System Overview

![Feldbuch Project Architecture](./images/diagrams/feldbuch-architecture.svg)

Spring Boot는 인증, REST API, AI 호출, Batch 처리를 담당합니다. Vue SPA는 독립 프론트엔드로 개발하며, Thymeleaf 화면은 전환 과정의 비교용 구현으로 남겨둡니다.

```mermaid
flowchart TD
    Browser --> VueSPA
    Browser --> ThymeleafView

    VueSPA --> ApiClient
    ThymeleafView --> StaticJS
    StaticJS --> Api
    ApiClient --> Api

    Api --> RequestIdFilter
    RequestIdFilter --> Security
    Security --> Controller

    Controller --> CommandService
    Controller --> QueryService
    CommandService --> Reader
    QueryService --> QueryDSL
    Reader --> Repository
    QueryDSL --> Repository

    Repository --> MySQL
    Repository --> Redis
```

### AI and Conversation

```mermaid
flowchart TD
    AiController --> AiFacade
    AiFacade --> AiJobService
    AiFacade --> AiSummaryAsyncService
    AiSummaryAsyncService --> SummaryService
    SummaryService --> OpenAiClient
    OpenAiClient --> OpenAI

    ChatController --> ConversationChatService
    ConversationChatService --> ConversationMessageCommandService
    ConversationChatService --> ChatContextBuilder
    ConversationChatService --> ChatService
    ChatContextBuilder --> ConversationMessageReader
    ChatService --> OpenAiWebClient
    OpenAiWebClient --> OpenAI

    ConversationController --> ConversationCommandService
    ConversationController --> ConversationQueryService
    ConversationMessageController --> ConversationMessageCommandService
    ConversationMessageController --> ConversationMessageQueryService
    ConversationCompletionScheduler --> ConversationCompletionService
    ConversationCompletionService --> ConversationRepository
    ConversationMessageCommandService --> Conversation
    Conversation --> LastMessageAt
```

### Knowledge and Batch

![Feldbuch AI Job Flow](./images/diagrams/feldbuch-ai-job-flow.svg)

```mermaid
flowchart TD
    KnowledgeController --> KnowledgeQueryService
    KnowledgeQueryService --> KnowledgeRepository
    KnowledgeQueryService --> KnowledgeNoteRepository

    OpenAiKnowledgeSummaryService --> AiClient
    OpenAiKnowledgeSummaryService --> KnowledgeSummaryPrompt
    AiClient --> OpenAI

    KnowledgeNoteCommandService --> KnowledgePathResolver
    KnowledgePathResolver --> KnowledgeRepository
    KnowledgeNoteCommandService --> KnowledgeNoteRepository

    KnowledgeExtractionScheduler --> KnowledgeConversationReader
    KnowledgeExtractionScheduler --> KnowledgeExtractionJob
    LocalKnowledgeExtractionJobRunner --> KnowledgeExtractionJob
    KnowledgeExtractionJob --> KnowledgeExtractionStep
    KnowledgeExtractionStep --> KnowledgeExtractionTasklet
    KnowledgeExtractionTasklet --> KnowledgeConversationReader
    KnowledgeExtractionTasklet --> KnowledgeExtractionService
    KnowledgeExtractionTasklet --> KnowledgeExtractionStatusService
    KnowledgeExtractionService --> ConversationAiContextBuilder
    ConversationAiContextBuilder --> ConversationMessageReader
    KnowledgeExtractionService --> OpenAiKnowledgeSummaryService
    KnowledgeExtractionService --> OpenAiKnowledgeMergeService
    KnowledgeExtractionService --> KnowledgeNoteCommandService
    KnowledgeNoteCommandService --> KnowledgePathResolver
    KnowledgePathResolver --> KnowledgeRootCategoryResolver
    KnowledgePathResolver --> OpenAiKnowledgeFolderSelectionService
```

## Client Architecture

![Feldbuch Client Architecture](./images/diagrams/feldbuch-client-architecture.svg)

```mermaid
flowchart TD
    Browser --> VueApp
    VueApp --> Router
    Router --> LoginView
    Router --> ConversationView
    Router --> RouterGuard
    RouterGuard --> AuthUtil

    LoginView --> AuthApi
    ConversationView --> WorkspaceSidebar
    WorkspaceSidebar --> ConversationSidebar
    WorkspaceSidebar --> KnowledgeSidebar
    KnowledgeSidebar --> KnowledgeTreeNode

    ConversationView --> ConversationApi
    ConversationView --> MessageList
    ConversationView --> ChatInput
    ConversationView --> StudyInfoPanel
    ConversationView --> KnowledgeWorkspace
    KnowledgeWorkspace --> KnowledgeNoteList
    KnowledgeWorkspace --> KnowledgeNoteDetail
    KnowledgeNoteList --> KnowledgeApi
    KnowledgeNoteList --> SearchHighlight
    KnowledgeSidebar --> SearchHighlight

    AuthApi --> ApiClient
    ConversationApi --> ApiClient
    KnowledgeApi --> ApiClient
    ApiClient --> RequestInterceptor
    ApiClient --> ResponseInterceptor
    RequestInterceptor --> LocalStorage
    ResponseInterceptor --> AuthUtil
    ResponseInterceptor --> Router
    ApiClient --> SpringBootApi
```

클라이언트 책임 분리:

- `App.vue`: `RouterView`를 렌더링하는 Vue 앱 루트
- `router/index.js`: `/login`, `/conversations` 라우트와 인증 Guard 관리
- `LoginView.vue`: 로그인 폼, 로그인 API 호출, 토큰 저장, 대화 화면 이동
- `ConversationView.vue`: 대화/지식 워크스페이스 컨테이너, 선택 상태와 요청 중 상태 관리, localStorage 기반 선택 상태 복원
- `WorkspaceSidebar.vue`: 대화/지식 탭 전환과 하위 사이드바 이벤트 중계
- `SidebarHeader.vue`: Feldbuch 로고와 새 대화 생성 버튼
- `SidebarTabs.vue`: 대화/지식 모드 전환 버튼
- `SidebarSectionLabel.vue`: 사이드바 섹션 라벨 공통 컴포넌트
- `ConversationSidebar.vue`: 대화 목록, 선택 상태, 새 대화 생성, 인라인 제목 수정, 삭제 버튼
- `KnowledgeSidebar.vue`: Knowledge 폴더 트리 조회, 폴더 검색, 새로고침, 선택 이벤트 전달
- `KnowledgeTreeNode.vue`: 재귀 폴더 노드 렌더링, 펼침/접힘, 폴더 선택, 검색어 강조
- `KnowledgeWorkspace.vue`: Knowledge breadcrumb, 노트 목록 패널, 노트 상세 패널 조합
- `KnowledgeNoteList.vue`: 선택 Knowledge 폴더의 노트 목록 조회, 노트 검색, 검색어 강조, 선택 상태 표시
- `KnowledgeNoteDetail.vue`: 선택한 Knowledge 노트의 제목, 설명, 요약, 키워드 표시
- `SearchHighlight.vue`: 폴더/노트 검색어 부분 강조
- `MessageList.vue`: `USER`, `ASSISTANT` 메시지 렌더링, Markdown, 코드 강조, COPY 버튼, 로딩 메시지
- `ChatInput.vue`: 사용자 입력을 `send` 이벤트로 상위 컴포넌트에 전달하고 전송 중 입력 비활성화
- `StudyInfoPanel.vue`: 선택 대화의 주제, 상태, 메시지 수, 생성일, 수정일, 활동 신호 표시
- `apiClient.js`: Axios instance, baseURL, Request/Response Interceptor 관리
- `authApi.js`, `conversationApi.js`, `knowledgeApi.js`: 도메인별 API 호출 함수
- `utils/markdownRenderer.js`: marked, highlight.js, DOMPurify 조합 Markdown 렌더링
- `utils/auth.js`: `accessToken`, `userId` 저장/조회/삭제와 로그인 여부 판단

대화 화면 데이터 흐름:

```text
ConversationView mounted
  ↓
getConversations()
  ↓
대화 목록 저장
  ↓
첫 번째 대화 자동 선택
  ↓
getConversation(conversationId)
  ↓
conversation / messages 상태 갱신
  ↓
ConversationSidebar / MessageList / StudyInfoPanel 렌더링
```

메시지 스트리밍 전송 흐름:

```text
ChatInput send event
  ↓
ConversationView.sendMessage(content)
  ↓
sendingMessage = true
  ↓
사용자 메시지와 빈 ASSISTANT 메시지 낙관적 추가
  ↓
conversationApi.streamMessage(conversationId, message)
  ↓
POST /api/conversations/{conversationId}/chat/stream
  ↓
SSE TOKEN 이벤트마다 ASSISTANT 메시지 content 누적
  ↓
COMPLETE 이벤트 수신
  ↓
getConversation(conversationId) 재조회
  ↓
메시지 목록과 제목 최신화
  ↓
대화 목록 최상단 이동과 자동 스크롤
```

Knowledge 화면 데이터 흐름:

```text
WorkspaceSidebar 지식 탭 선택
  ↓
localStorage feldbuch.sidebarMode 갱신
  ↓
KnowledgeSidebar mounted
  ↓
GET /api/knowledge/tree
  ↓
폴더 검색어에 따라 KnowledgeTreeNode 재귀 렌더링
  ↓
폴더 선택
  ↓
ConversationView.selectedKnowledgeId / selectedKnowledgePath 갱신
  ↓
localStorage feldbuch.selectedKnowledgeId / feldbuch.selectedKnowledgePath 저장
  ↓
KnowledgeWorkspace
  ↓
GET /api/knowledge/{knowledgeId}/notes
  ↓
노트 목록 렌더링과 노트 검색
  ↓
노트 선택
  ↓
localStorage feldbuch.selectedKnowledgeNoteId 저장
  ↓
GET /api/knowledge/notes/{noteId}
  ↓
KnowledgeNoteDetail 제목/설명/요약/키워드 렌더링
```

## API Communication

공통 규칙:

- 요청 본문은 JSON을 사용합니다.
- 일반 응답은 `ApiResponse<T>` 형식으로 통일하고 실제 데이터는 `data` 필드에 담습니다.
- 로그인 성공 시 `accessToken`, `userId`를 `localStorage`에 저장합니다.
- Axios Request Interceptor가 `Authorization: Bearer <accessToken>` 헤더를 자동으로 추가합니다.
- Axios Response Interceptor가 `401 Unauthorized`를 감지하면 `logout()`으로 클라이언트 토큰을 제거하고 `/login`으로 이동합니다.
- Fetch 기반 SSE 스트리밍 함수는 `Authorization` 헤더를 직접 추가합니다.
- 백엔드는 `RequestIdFilter`로 모든 요청에 UUID 기반 `requestId`를 부여하고, MDC와 `X-Request-Id` 응답 헤더에 기록합니다.

주요 API:

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/auth/login` | 로그인 |
| `GET` | `/api/conversations` | 대화 목록 조회 |
| `POST` | `/api/conversations` | 새 대화 생성 |
| `GET` | `/api/conversations/{conversationId}` | 대화 상세와 메시지 조회 |
| `PATCH` | `/api/conversations/{conversationId}` | 대화 제목 수정 |
| `DELETE` | `/api/conversations/{conversationId}` | 대화 삭제 |
| `POST` | `/api/conversations/{conversationId}/chat` | 일반 대화형 AI 요청 |
| `POST` | `/api/conversations/{conversationId}/chat/stream` | SSE 대화형 AI 요청 |
| `GET` | `/api/knowledge/tree` | Knowledge 폴더 트리 조회 |
| `GET` | `/api/knowledge/{knowledgeId}/notes` | Knowledge 폴더별 노트 목록 조회 |
| `GET` | `/api/knowledge/notes/{noteId}` | Knowledge 노트 상세 조회 |
| `GET` | `/api/knowledge/conversations/{conversationId}/consolidated-note` | Conversation별 통합 Knowledge 노트 조회 |
| `GET` | `/api/ai/jobs/{jobId}` | AI Job 상태 조회 |

SSE 이벤트 계약:

```text
StreamResponse
  type: TOKEN | COMPLETE | ERROR
  content: TOKEN일 때 새 토큰 조각, ERROR일 때 오류 메시지
```

## Database Model

![Feldbuch Entity Relationship Diagram](./images/diagrams/feldbuch-erd.svg)

현재 영속 모델은 `users`, `user_identities`, `notes`, `ai_job`, `conversations`, `conversation_messages`, `knowledge`, `knowledge_notes`, `knowledge_note_keywords`를 중심으로 구성합니다.

- `users`: 노트, 대화, Knowledge의 소유자
- `user_identities`: Google OIDC 등 외부 인증 Provider 계정 식별자
- `notes`: 개발 노트와 학습 상태
- `ai_job`: 비동기 AI 요약 작업 상태
- `conversations`: AI 학습 대화 세션, 마지막 메시지 활동 시각, Knowledge 추출 상태와 체크포인트
- `conversation_messages`: 대화별 USER/ASSISTANT 메시지
- `knowledge`: 사용자별 지식 폴더 트리. 최상위 폴더는 서버가 결정한 `KnowledgeRootCategory` 이름을 사용
- `knowledge_notes`: 대화에서 AI가 추출한 학습 노트. `INCREMENTAL`은 추출 범위별 노트, `CONSOLIDATED`는 Conversation 누적 통합 노트
- `knowledge_note_keywords`: KnowledgeNote 키워드 ElementCollection

## Knowledge Extraction Batch

Knowledge 추출 배치는 완료된 대화를 AI 학습 노트로 증류하기 위한 Spring Batch 작업입니다. Conversation은 메시지가 저장될 때 `lastMessageAt`을 갱신하고 ACTIVE 상태가 되며, 자동 완료 스케줄러가 일정 시간 비활성인 ACTIVE 대화를 COMPLETED로 전환합니다. COMPLETED 대화만 Knowledge 추출 대상이 됩니다.

- Job 이름: `knowledgeExtractionJob`
- Step 이름: `knowledgeExtractionStep`
- 실행 방식: Tasklet 기반 단일 Step
- 실행 시점: `KnowledgeExtractionScheduler`가 `batch.knowledge-extraction.fixed-delay` 기준으로 대상 존재 여부를 확인한 뒤 Job 실행
- 기본 스케줄 간격: 60초
- 로컬 수동 실행: `local` 프로필에서 `feldbuch.batch.knowledge-extraction.run=true` 설정 시 애플리케이션 시작 직후 1회 실행
- Scheduler Job Parameter: `requestedAt=System.currentTimeMillis()`로 매 실행을 고유 Job 인스턴스로 구분
- Local Runner Job Parameter: `executionTime=System.currentTimeMillis()`로 매 실행을 고유 Job 인스턴스로 구분
- 반복 방식: 한 번 실행할 때 조회된 대상 Conversation 목록을 순회 처리
- 대상 조건: `status = COMPLETED`이고 `knowledgeExtractStatus = NONE` 또는 재시도 가능한 `FAILED`
- 재시도 조건: `knowledgeExtractStatus = FAILED`, `knowledgeExtractRetryCount < 3`, `knowledgeExtractFailedAt <= now - 1 minute`
- 제외 조건: ACTIVE 대화, 이미 `COMPLETED`로 추출된 대화, 실패 횟수 3회 이상, 실패 후 1분 대기 시간이 지나지 않은 대화
- 처리 순서: `updatedAt ASC`
- 대상 존재 확인: `existsKnowledgeExtractionTarget()`으로 스케줄러가 불필요한 Job 실행을 건너뜀
- 성공 처리: `PROCESSING -> COMPLETED`, `lastExtractedMessageId` 갱신, 오류 메시지와 실패 시각 초기화
- 실패 처리: `FAILED`로 변경, 재시도 횟수 증가, 실패 메시지와 실패 시각 저장
- 증분 추출: 완료된 대화에 새 메시지가 추가되면 상태를 `NONE`으로 되돌리고 `lastExtractedMessageId` 이후 메시지만 AI 컨텍스트로 구성
- 노트 생성: 새 메시지 범위는 항상 `INCREMENTAL` KnowledgeNote로 저장
- 통합 노트: 같은 Conversation의 `CONSOLIDATED` KnowledgeNote가 없으면 최초 생성하고, 있으면 기존 통합 노트와 신규 증분 노트를 AI로 병합해 갱신
- 폴더 결정: AI 요약/병합 응답의 하위 경로를 `KnowledgeRootCategoryResolver`가 고정 대분류 아래에 배치하고, 기존 하위 폴더 후보가 있으면 `OpenAiKnowledgeFolderSelectionService`가 재사용 여부를 판단

## Knowledge Classification and Merge

Knowledge 분류와 병합은 추출 품질과 폴더 중복 방지를 위한 서버 내부 정책입니다.

- 최상위 분류: `KnowledgeRootCategory` enum으로 고정
- 지원 대분류: `COMPUTER_SCIENCE`, `PROGRAMMING_LANGUAGE`, `WEB_DEVELOPMENT`, `DATABASE`, `NETWORK`, `OPERATING_SYSTEM`, `CLOUD`, `DEVOPS`, `ARTIFICIAL_INTELLIGENCE`, `SECURITY`, `COMPUTER_USAGE`, `COMMUNICATION`
- AI 응답 경로: 하위 경로만 받으며 최대 2단계까지 허용
- 기본 대분류: 키워드 매칭 실패 시 `COMPUTER_SCIENCE`
- 폴더 재사용: 동일 이름이 없더라도 기존 자식 폴더 후보를 AI에 제공해 `EXISTING` 또는 `CREATE`를 선택
- 병합 응답: `AiKnowledgeMergeResponse`가 `knowledgePath`, `title`, `description`, `summary`, `keywords`를 반환
- 병합 결과의 경로가 바뀌면 통합 KnowledgeNote를 새 Knowledge 폴더로 이동

## Conversation Auto Completion

비활성 대화 자동 완료는 사용자가 명시적으로 대화를 닫지 않아도 Knowledge 추출 대상이 자연스럽게 만들어지도록 하는 스케줄링 흐름입니다.

- Scheduler: `ConversationCompletionScheduler`
- Service: `ConversationCompletionService`
- Repository query: `findInactiveActiveConversations(cutoff)`
- 기본 실행 간격: `conversation.auto-completion.fixed-delay=60000`
- 기본 비활성 시간: `conversation.auto-completion.inactivity-timeout=30m`
- 대상 조건: `status = ACTIVE`, `lastMessageAt IS NOT NULL`, `lastMessageAt <= cutoff`
- 제외 조건: 최근 메시지가 있는 ACTIVE 대화, 이미 COMPLETED인 대화, 메시지가 아직 없는 대화
- 정렬 기준: `lastMessageAt ASC`
- 유효성: 비활성 시간은 null, 0, 음수일 수 없음

## Project Structure

```text
src/main/java/io.github.kaltz.feldbuch
├── ai
│   ├── client          # OpenAI REST/SSE 클라이언트
│   ├── dto             # OpenAI/AI 응답 DTO
│   ├── facade          # AI 요청 진입 Facade
│   ├── job             # 비동기 AI Job 상태 관리
│   ├── mapper          # 요청/응답/메시지 변환
│   ├── model           # 채팅/제목 생성 모델
│   ├── prompt          # Summary/Knowledge/Title 프롬프트
│   └── service         # 요약, 채팅, Knowledge 요약 서비스
├── auth
├── batch
├── common
├── config
├── conversation
├── knowledge
│   ├── folder          # AI 기반 기존 Knowledge 폴더 선택
│   ├── context         # Conversation 메시지 기반 추출 컨텍스트
│   └── service         # Knowledge 경로 결정, 추출, 노트 저장
├── note
├── redis
└── user

src/main/resources
├── templates           # Thymeleaf 비교용 화면
└── static              # Thymeleaf 화면용 CSS/JS

frontend/src
├── api
├── assets
├── components
│   ├── chat
│   ├── common
│   ├── knowledge
│   └── sidebar
├── constants
├── router
├── utils
└── views
```

## Design Points

- CQRS 기반 Command/Query 책임 분리
- Reader Pattern으로 조회 전용 도메인 접근 책임 분리
- Mapper Pattern으로 DTO/도메인/OpenAI 메시지 변환 분리
- Facade와 Async Service를 통한 AI Job 요청 흐름 분리
- OpenAI 일반 요청과 SSE 스트리밍 요청 계층 분리
- Conversation 메시지 영속화 후 AI 컨텍스트 구성
- 메시지 저장 시 `lastMessageAt` 갱신과 완료 대화 재활성화
- 비활성 ACTIVE 대화 자동 완료 후 Knowledge 추출 대상으로 연결
- `lastExtractedMessageId` 기반 증분 Knowledge 추출
- 낙관적 사용자 메시지와 스트리밍 Assistant 메시지 렌더링
- Markdown 렌더링과 sanitize 책임을 `markdownRenderer.js`로 분리
- Knowledge 폴더 트리 자기 참조 모델링
- KnowledgePathResolver로 AI 응답 경로 기반 폴더 자동 조회/생성
- 고정 대분류와 AI 폴더 선택으로 Knowledge 폴더 중복 생성 완화
- 추출 범위별 `INCREMENTAL` 노트와 Conversation 단위 `CONSOLIDATED` 노트를 분리
- 기존 통합 노트와 신규 증분 노트를 AI로 병합해 누적 요약 유지
- Knowledge 워크스페이스를 목록 패널과 상세 패널로 분리
- 폴더/노트 검색어 하이라이트를 공통 컴포넌트로 분리
- 사용자의 마지막 작업 위치를 localStorage로 복원
- Batch 대상 조회와 재시도 조건을 QueryDSL로 관리
- Scheduler에서 대상 존재 여부를 먼저 확인해 불필요한 Batch 실행 방지
- Knowledge 추출 상태 변경은 별도 트랜잭션으로 반영
- Request ID 기반 요청 추적
- Thymeleaf 화면은 비교용으로 유지하고 Vue SPA를 주 사용자 화면으로 전환

## Roadmap

- OAuth2 로그인 플로우 연결
- Knowledge 노트 원본 Conversation 이동 링크
- Knowledge 폴더/노트 생성/수정/삭제 UI 확장
- Vue 화면 상태 관리 구조 정리
- Vue 삭제 확인 UX 개선
- Knowledge 추출 대상 조회 인덱스 추가
- Conversation 자동 완료/Knowledge 추출 스케줄러 운영 설정 외부화 고도화
- Postman Knowledge 요청 파일 보강
- AI 태그 생성
- 코드 리뷰
- 학습 퀴즈 생성
- 학습 로드맵 추천
- Docker Compose 운영 구성 정리
- 테스트 커버리지 확장
- Monitoring
