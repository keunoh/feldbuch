# 📚 Feldbuch

::: {align="center"}
# 🤖 AI 기반 개발 지식 관리 플랫폼

**기록을 넘어, AI와 함께 성장하는 개발 노트**
:::

------------------------------------------------------------------------

## 📖 프로젝트 소개

Feldbuch는 개발자가 학습하며 얻은 지식, 트러블 슈팅, 코드, 환경설정 등을
기록하고,

AI가 이를 이해하여 **요약, 태깅, 추천, 코드 리뷰**까지 수행하는 개발
지식 관리 플랫폼입니다.

단순한 CRUD 프로젝트가 아니라 **실무 지향적인 백엔드 아키텍처와 AI
기능을 함께 학습하기 위해 시작한 프로젝트**입니다.

------------------------------------------------------------------------

# ✨ 핵심 기능

-   🔐 JWT 기반 로그인 / 회원가입
-   📒 개발 노트 CRUD
-   🔍 QueryDSL 기반 검색
-   📑 페이지네이션
-   📌 Pin 기능
-   📚 학습 상태 관리
-   🤖 OpenAI 기반 AI 요약
-   ⚡ 비동기 AI 처리

------------------------------------------------------------------------

# 🏗️ Architecture

``` mermaid
flowchart TD

Client --> Security

Security --> Controller

Controller --> CommandService
Controller --> QueryService

CommandService --> Reader
QueryService --> QueryDSL

Reader --> Repository
Repository --> MySQL

AiController --> AiFacade
AiFacade --> AiSummaryAsyncService
AiSummaryAsyncService --> OpenAiSummaryService
OpenAiSummaryService --> OpenAiClient
OpenAiClient --> OpenAI
```

------------------------------------------------------------------------

# 🧰 Tech Stack

  Category    Stack
  ----------- ------------------------
  Language    Java 21
  Framework   Spring Boot 3
  Security    Spring Security, JWT
  Database    MySQL, Spring Data JPA
  Query       QueryDSL
  Build       Gradle
  AI          OpenAI REST API
  Infra       Docker
  Test        JUnit5, MockMvc

------------------------------------------------------------------------

# 📂 프로젝트 구조

``` text
src/main/java
└── io.github.kaltz.feldbuch
    ├── ai
    ├── auth
    ├── common
    ├── config
    ├── note
    └── user
```

------------------------------------------------------------------------

# 🧩 적용한 설계 패턴

-   ✅ Reader Pattern
-   ✅ CQRS (Command / Query Separation)
-   ✅ Facade Pattern
-   ✅ Strategy Pattern
-   ✅ Mapper Pattern
-   ✅ Async Processing
-   ✅ Client Layer (OpenAI)

------------------------------------------------------------------------

# 🚀 개발 진행 과정

``` text
Spring Boot
      │
JWT 인증
      │
회원가입 / 로그인
      │
노트 CRUD
      │
QueryDSL 검색
      │
Reader Pattern
      │
CQRS
      │
Facade
      │
Async
      │
OpenAI REST API
      │
AI Platform
```

------------------------------------------------------------------------

# ✅ 구현 완료

-   [x] JWT 인증
-   [x] Spring Security
-   [x] 회원가입
-   [x] 로그인
-   [x] 노트 CRUD
-   [x] QueryDSL 검색
-   [x] PageableExecutionUtils
-   [x] Reader Pattern
-   [x] CQRS
-   [x] Facade
-   [x] Async
-   [x] OpenAI API 연동

------------------------------------------------------------------------

# 🛣️ Roadmap

## AI

-   [ ] Summary Prompt 고도화
-   [ ] AI 태그 생성
-   [ ] 제목 추천
-   [ ] 코드 리뷰
-   [ ] 학습 퀴즈
-   [ ] 학습 로드맵

## Backend

-   [ ] Redis Cache
-   [ ] Docker Compose
-   [ ] GitHub Actions
-   [ ] AWS 배포
-   [ ] Monitoring

------------------------------------------------------------------------

# 📚 문서

프로젝트의 상세 설계와 개발 과정은 `docs/`에서 관리합니다.

-   📘 DEVELOPMENT_BOOK.md
-   🏗️ ARCHITECTURE.md
-   📑 API.md
-   🔥 TROUBLE_SHOOTING.md

------------------------------------------------------------------------

# 💡 프로젝트 철학

> **"개발자의 학습 기록을 저장하고, AI가 그 기록을 이해하여 더 나은
> 학습을 돕는 지식 관리 플랫폼."**

Feldbuch는 기능 구현뿐 아니라 **리팩토링, 테스트, 아키텍처,
유지보수성**을 함께 고민하며 발전시키는 장기 프로젝트입니다.
