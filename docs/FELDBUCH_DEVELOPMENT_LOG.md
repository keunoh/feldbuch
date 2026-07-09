# Feldbuch 개발 기록

> AI 기반 개발 학습 노트 서비스

## 프로젝트를 시작한 계기

개발을 공부하면서 ChatGPT와 나눈 대화, 삽질 기록, 환경 설정, 문제 해결
과정을 노트처럼 정리하고 검색할 수 있는 서비스를 만들고 싶었다.

목표는 단순 메모 앱이 아니라 **AI가 개발 노트를 이해하고
요약·태깅·추천까지 수행하는 서비스**를 만드는 것이다.

------------------------------------------------------------------------

# 전체 아키텍처

``` text
Client
   │
Spring Security (JWT)
   │
Controller
   ├──────────────┐
CommandService   QueryService
   │              │
 Reader        QueryDSL
   │              │
 Repository      Repository
        │
      MySQL

AI
AiController
      │
 AiFacade
      │
AiSummaryAsyncService
      │
 SummaryService
      │
OpenAiSummaryService
      │
 OpenAiClient
      │
 OpenAI API
```

------------------------------------------------------------------------

# 지금까지 구현한 기능

## 인증

-   JWT 로그인
-   Spring Security
-   CustomUserDetails
-   AuthenticationProvider
-   JWT Filter

## 노트

-   생성
-   조회
-   수정
-   삭제
-   검색(QueryDSL)
-   Pagination
-   Pin
-   StudyStatus

## 리팩토링

-   Reader Pattern
-   Mapper Pattern
-   CQRS(Command/Query 분리)
-   Facade
-   Async
-   Client Layer
-   DTO 분리

## AI

-   AI 도메인 분리
-   MockSummaryService
-   OpenAI REST API 연동
-   RestClient 적용

------------------------------------------------------------------------

# 사용 기술

  기술                 용도
  -------------------- -------------------
  🌱 Spring Boot       백엔드 프레임워크
  🔒 Spring Security   인증/인가
  🎫 JWT               토큰 인증
  🐬 MySQL             DB
  📦 Docker            컨테이너
  🔍 QueryDSL          동적 검색
  🤖 OpenAI API        AI 요약
  ⚡ Async             비동기 작업

------------------------------------------------------------------------

# 지금까지의 개발 흐름

``` text
회원가입
    ↓
로그인
    ↓
JWT 인증
    ↓
노트 CRUD
    ↓
QueryDSL 검색
    ↓
CQRS
    ↓
Reader Pattern
    ↓
Facade
    ↓
AI 도메인 분리
    ↓
OpenAI 연동
```

------------------------------------------------------------------------

# 앞으로의 로드맵

## AI

-   SummaryPromptTemplate
-   OpenAI Summary
-   Tag 생성
-   제목 추천
-   코드 리뷰
-   학습 로드맵 추천

## 인프라

-   Docker Compose
-   GitHub Actions
-   AWS 배포
-   Redis Cache

## 고도화

-   이벤트 기반 처리
-   RAG
-   Vector Search
-   Knowledge Graph

------------------------------------------------------------------------

# 프로젝트를 통해 배우고 싶은 것

-   실무 아키텍처
-   유지보수하기 쉬운 코드
-   AI 서비스 설계
-   대규모 프로젝트 구조
-   테스트 문화
-   클라우드 배포

------------------------------------------------------------------------

# 커밋 히스토리 철학

기능 단위로 작은 커밋을 유지한다.

예시

``` text
기능: JWT 로그인 구현
리팩토링: Reader 패턴 적용
기능: QueryDSL 검색 추가
기능: OpenAI API 클라이언트 구현
기능: AI 요약 서비스 구현
```

------------------------------------------------------------------------

# 기술 스택 다이어그램

``` text
🌱 Spring Boot
      │
🔒 Security
      │
🎫 JWT
      │
📒 Note
      │
🔍 QueryDSL
      │
🤖 AI
      │
🌐 OpenAI
```

------------------------------------------------------------------------

# 참고할 기술 로고

Markdown에서 아래 이미지를 확인할 수 있다.

-   Spring Boot:
    https://raw.githubusercontent.com/devicons/devicon/master/icons/spring/spring-original.svg
-   Docker:
    https://raw.githubusercontent.com/devicons/devicon/master/icons/docker/docker-original.svg
-   MySQL:
    https://raw.githubusercontent.com/devicons/devicon/master/icons/mysql/mysql-original.svg
-   Java:
    https://raw.githubusercontent.com/devicons/devicon/master/icons/java/java-original.svg
-   GitHub Actions:
    https://raw.githubusercontent.com/devicons/devicon/master/icons/github/github-original.svg

> 이후 README에서는 위 로고를 `<img>` 태그로 활용하면 시각적으로 더욱
> 풍부하게 꾸밀 수 있다.
