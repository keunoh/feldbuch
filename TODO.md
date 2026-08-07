# Feldbuch TODO

## 프로젝트 방향

Feldbuch는 단순한 AI 채팅 서비스가 아니라,
AI와 함께 공부한 과정과 결과를 기록하고 돌아볼 수 있는 개인 학습 공간을 목표로 한다.

사용자는 AI와 자유롭게 대화할 수 있으며,
대화에서 학습한 주제, 핵심 내용, 요약, 학습 날짜가 지속적으로 축적된다.

최종적으로는 다음 질문에 답할 수 있는 서비스를 만든다.

- 오늘 무엇을 공부했는가?
- 최근 어떤 주제를 반복해서 공부했는가?
- 특정 주제를 언제 처음 공부했는가?
- 공부한 내용을 짧게 다시 확인할 수 있는가?
- 일정 기간 동안 얼마나 꾸준히 공부했는가?

---

## 핵심 기능

### 1. AI 대화

- [x] 새로운 대화 생성
- [x] 대화 목록 조회
- [x] 대화 선택
- [x] 선택한 대화의 메시지 조회
- [x] 사용자 메시지 저장
- [x] AI 응답 요청 및 저장
- [x] SSE 기반 AI 응답 스트리밍
- [x] 스트리밍 토큰을 화면에 실시간 반영
- [x] 대화 제목 자동 생성
- [x] 대화 삭제
- [x] 대화 제목 수정
- [x] AI 응답 Markdown 렌더링
- [x] 코드 블록 문법 강조
- [x] 코드 블록 복사
- [ ] 대화 종료 상태 처리

### 2. 학습 기록

- [ ] 대화를 학습 기록으로 저장
- [ ] 학습한 날짜 기록
- [ ] 학습 시작 시간과 종료 시간 기록
- [ ] 학습 주제 기록
- [ ] 학습 내용에 태그 추가
- [ ] 학습 완료 여부 표시
- [ ] 오늘 공부한 내용 조회
- [ ] 날짜별 학습 기록 조회
- [ ] 주제별 학습 기록 조회

### 3. AI 요약

- [x] 대화 내용 자동 요약
- [x] 학습한 핵심 개념 추출
- [ ] 사용자가 질문한 내용 정리
- [ ] 이해하지 못한 내용 또는 추가 학습 항목 추출
- [ ] 대화 종료 시 학습 노트 생성
- [ ] 여러 대화의 주간 학습 내용 요약
- [ ] 여러 대화의 월간 학습 내용 요약

### 4. 학습 대시보드

- [ ] 오늘의 학습 기록 표시
- [ ] 최근 학습한 주제 표시
- [ ] 누적 학습 일수 표시
- [ ] 연속 학습 일수 표시
- [ ] 주제별 학습 횟수 표시
- [ ] 가장 많이 공부한 주제 표시
- [ ] 최근 생성된 학습 요약 표시

### 5. 학습 캘린더

- [ ] 날짜별 학습 여부 표시
- [ ] GitHub Contribution Graph 형태의 학습 기록 표시
- [ ] 학습량에 따라 날짜별 색상 농도 구분
- [ ] 날짜 선택 시 해당 날짜의 학습 기록 조회
- [ ] 주간 및 월간 학습량 확인
- [ ] 연속 학습 기록 표시

### 6. 학습 노트

- [ ] AI 대화와 별도로 개인 메모 작성
- [ ] 대화 내용을 학습 노트로 변환
- [ ] 학습 노트 직접 수정
- [ ] Markdown 형식 지원
- [ ] 코드 블록 저장 및 표시
- [ ] 태그별 노트 분류
- [ ] 노트 검색
- [ ] 관련 대화와 노트 연결

### 7. 복습

- [ ] 복습이 필요한 학습 기록 표시
- [ ] 이전에 공부한 내용을 무작위로 추천
- [ ] AI가 학습 내용으로 복습 질문 생성
- [ ] 사용자의 답변과 AI 설명 비교
- [ ] 이해도 또는 복습 상태 기록
- [ ] 일정 기간이 지난 학습 내용 다시 알림

### 8. 검색

- [ ] 대화 제목 검색
- [ ] 메시지 내용 검색
- [x] 폴더 내 학습 노트 검색
- [ ] 전체 학습 노트 검색
- [ ] 태그 검색
- [ ] 날짜 범위 검색
- [ ] 학습 주제 통합 검색

### 9. 대화 자동 학습 노트 증류

Feldbuch의 핵심 방향은 단순 채팅이 아니라, AI와 나눈 대화를 자동으로 학습 노트로 증류하는 구조다.

```text
AI와 대화
  ↓
대화 종료 또는 일정 시간 미사용
  ↓
Spring Batch가 요약 대상 대화 조회
  ↓
AI가 Knowledge 경로 분류 + 짧은 학습 요약 생성
  ↓
Knowledge 폴더 자동 생성
  ↓
학습 노트 저장
  ↓
사용자는 카테고리별 요약 노트 열람
```

현재 코드에서는 `category`/`study_note`라는 이름 대신 `knowledge`/`knowledge_notes` 도메인으로 저장 모델을 구성한다.

#### 9-1. 도메인 및 DB 모델

- [x] Knowledge 폴더 엔티티 생성
- [x] Knowledge 폴더 자기 참조 구조 추가
- [x] 사용자별 Knowledge 소유 관계 추가
- [x] Knowledge 루트 폴더 생성 메서드 추가
- [x] Knowledge 자식 폴더 생성 메서드 추가
- [x] Knowledge 이름 변경 메서드 추가
- [x] Knowledge 폴더 이동 메서드 추가
- [x] 다른 사용자의 Knowledge로 이동하지 못하도록 검증
- [x] KnowledgeRepository 추가
- [x] 사용자별 루트 Knowledge 조회 쿼리 추가
- [x] 사용자별 특정 parent 하위 Knowledge 조회 쿼리 추가
- [x] 같은 parent 안의 Knowledge 이름 중복 확인 쿼리 추가
- [x] Knowledge 조회 인덱스 추가: `idx_knowledge_user_parent`
- [x] Knowledge parent 조회 인덱스 추가: `idx_knowledge_parent`
- [x] KnowledgeNote 엔티티 생성
- [x] KnowledgeNote와 User 연결
- [x] KnowledgeNote와 Conversation 연결
- [x] KnowledgeNote와 Knowledge 연결
- [x] KnowledgeNote 제목, 설명, 요약 컬럼 추가
- [x] KnowledgeNote 키워드 ElementCollection 추가
- [x] `knowledge_note_keywords` 테이블 구조 추가
- [x] 키워드 공백 제거, 중복 제거, 최대 10개 제한
- [x] KnowledgeNoteRepository 추가
- [x] Knowledge별 학습 노트 조회 쿼리 추가
- [x] Conversation별 학습 노트 조회 쿼리 추가
- [x] 사용자별 최근 학습 노트 조회 쿼리 추가
- [x] Conversation 기준 학습 노트 생성 여부 확인 쿼리 추가
- [x] KnowledgeNote 조회 인덱스 추가: `idx_knowledge_note_user`
- [x] KnowledgeNote 조회 인덱스 추가: `idx_knowledge_note_knowledge`
- [x] KnowledgeNote 조회 인덱스 추가: `idx_knowledge_note_conversation`
- [x] Conversation에 Knowledge 추출 상태 필드 추가
- [x] Conversation에 Knowledge 추출 재시도 횟수 필드 추가
- [x] Conversation에 Knowledge 추출 실패 메시지 필드 추가
- [x] Conversation에 Knowledge 추출 실패 시각 필드 추가
- [ ] 요약 대상 조회를 위한 Conversation 인덱스 추가
- [ ] DB 마이그레이션 방식 정리

#### 9-2. 수동 학습 노트 생성

- [x] AI 요약 응답을 KnowledgeNote로 저장하는 Command 서비스 추가
- [x] Conversation, User, Knowledge를 연결해 KnowledgeNote 생성
- [x] Knowledge와 User 소유자 일치 검증
- [x] KnowledgeNote 제목/설명/요약 필수값 검증
- [x] KnowledgeNote 저장 Command 서비스 단위 테스트
- [ ] 대화 내용을 사용자가 직접 입력해 수동 학습 노트로 저장하는 서비스 추가
- [ ] KnowledgeNote 생성 요청 DTO 추가
- [x] KnowledgeNote 응답 DTO 추가
- [ ] KnowledgeNote 생성 API 추가
- [x] KnowledgeNote 단건 조회 API 추가
- [x] KnowledgeNote 목록 조회 API 추가
- [ ] 같은 Conversation에서 중복 생성 방지 정책 정리

#### 9-3. AI 구조화 요약

- [ ] AI에게 기존 Knowledge 경로 목록을 함께 전달
- [ ] 기존 Knowledge 재사용 우선 프롬프트 작성
- [ ] 새 Knowledge는 적절한 기존 경로가 없을 때만 생성하도록 지시
- [x] 한 번의 AI 요청으로 구조화된 JSON 생성
- [x] `knowledgePath` 파싱
- [x] `title`, `description`, `summary`, `keywords` 파싱
- [x] AI JSON 응답 파싱 실패 처리
- [x] Knowledge 경로 자동 조회 및 생성 서비스 추가
- [x] 존재하는 Knowledge 경로 재사용
- [x] 없는 Knowledge 경로 자동 생성
- [x] Knowledge 경로 공백/빈 항목 정규화
- [x] 저장되지 않은 사용자 검증
- [x] Knowledge 경로 Resolver 단위 테스트
- [x] AI 요약 결과를 KnowledgeNote로 저장

예상 AI 응답:

```json
{
  "knowledgePath": [
    "개발",
    "Spring"
  ],
  "title": "Spring 트랜잭션 적용 범위",
  "description": "Spring 트랜잭션 적용 위치와 우선순위를 정리한 학습 노트",
  "summary": "서비스 클래스와 메서드에 @Transactional을 적용했을 때의 우선순위와 동작 차이를 정리한 학습 노트입니다.",
  "keywords": [
    "Spring",
    "Transactional",
    "트랜잭션"
  ]
}
```

#### 9-4. Spring Batch 자동 증류

- [x] Knowledge 추출 Batch Job 추가: `knowledgeExtractionJob`
- [x] Knowledge 추출 Batch Step 추가: `knowledgeExtractionStep`
- [x] Tasklet 기반 Knowledge 추출 배치 구현
- [x] Knowledge 추출 대상 Conversation 조회 Reader 추가
- [x] QueryDSL 기반 Knowledge 추출 대상 조회 구현
- [x] 대상 기준: `ConversationStatus.COMPLETED`
- [x] 대상 기준: `knowledgeExtractStatus = NONE`
- [x] 재시도 기준: `knowledgeExtractStatus = FAILED`
- [x] 재시도 기준: `knowledgeExtractRetryCount < 3`
- [x] 재시도 기준: `knowledgeExtractFailedAt <= now - 1 minute`
- [x] 처리 시작 시 `knowledgeExtractStatus = PROCESSING` 처리
- [x] 성공 시 `knowledgeExtractStatus = COMPLETED` 처리
- [x] 실패 시 `knowledgeExtractStatus = FAILED` 처리
- [x] 실패 시 재시도 횟수 증가
- [x] 실패 시 실패 메시지와 실패 시각 저장
- [x] 한 대화 처리 실패 시 다음 대화를 계속 처리
- [x] 로컬 프로필 수동 실행 Runner 추가
- [x] `feldbuch.batch.knowledge-extraction.run=true` 설정 시 애플리케이션 시작 후 1회 실행
- [x] Job 실행 시 `executionTime` Job Parameter 추가
- [x] Knowledge 추출 배치 Job 설정 테스트
- [x] Knowledge 추출 Tasklet 테스트
- [x] Knowledge 추출 대상 조회 Repository 테스트
- [ ] 대상 기준: 메시지 2개 이상
- [ ] 대상 기준: 마지막 메시지 이후 30분 이상 경과
- [x] 30분마다 실행하는 스케줄 설정
- [ ] 매일 새벽 실행하는 스케줄 옵션 검토
- [ ] 외부 AI API 실패 재시도 정책 추가

#### 9-5. 학습 화면

- [ ] `/study` 라우트 추가
- [x] 왼쪽 Knowledge 폴더 트리 화면 추가
- [x] Knowledge 폴더 검색
- [x] 오른쪽 학습 노트 목록 화면 추가
- [x] 폴더 내 학습 노트 검색
- [x] 검색어 하이라이트
- [x] Knowledge breadcrumb 표시
- [x] 학습 노트 본문 화면 추가
- [x] 키워드 표시
- [x] 마지막 선택한 사이드바 모드, Knowledge 폴더, Knowledge 노트 복원
- [ ] 원본 Conversation으로 이동하는 링크 추가
- [x] Knowledge별 노트 필터링
- [ ] 최근 생성된 학습 노트 표시

#### 9-6. 관리 및 개선

- [ ] Knowledge 이름 수정 API
- [ ] Knowledge 이동 API
- [ ] KnowledgeNote 이동 API
- [ ] KnowledgeNote 제목/요약/키워드 수정 API
- [ ] 대화 재요약 기능
- [ ] 자동 분류 결과 수동 보정
- [ ] 유사 Knowledge 병합
- [ ] Knowledge 이름 정규화 정책

---

## 화면 구상

### 대화 화면

- 왼쪽에는 대화 목록을 표시한다.
- 오른쪽에는 선택한 대화의 메시지를 표시한다.
- 대화가 학습 기록으로 연결될 수 있도록 한다.
- 대화 종료 후 요약 또는 학습 노트를 생성할 수 있도록 한다.
- AI 응답은 SSE 스트리밍으로 실시간 표시한다.
- 대화 원문은 학습 노트와 분리해 보존한다.

### 학습 노트 화면

라우트는 `/study`로 둔다.

```text
┌──────────────┬───────────────────────────────┐
│ 개발         │ Spring 트랜잭션 적용 범위     │
│  ├ Spring    │                               │
│  ├ Vue       │ 핵심 요약                     │
│  └ Database  │ ...                           │
│              │                               │
│ 취업         │ 키워드: Spring, Transactional │
└──────────────┴───────────────────────────────┘
```

- 왼쪽에는 Knowledge 폴더 트리를 표시한다.
- 오른쪽에는 선택한 폴더의 학습 노트 목록과 본문을 표시한다.
- 학습 노트에는 제목, 요약, 키워드, 생성일, 원본 대화 링크를 표시한다.
- 자동 분류된 Knowledge를 사용자가 나중에 수정할 수 있게 한다.

### 학습 대시보드

- 오늘 공부한 내용
- 최근 공부한 주제
- 연속 학습 일수
- 누적 학습 기록
- 최근 생성된 요약
- 복습이 필요한 내용

### 학습 캘린더

GitHub Contribution Graph와 유사하게 날짜별 학습 활동을 표시한다.

학습량은 다음 기준 등을 이용해 계산할 수 있다.

- 생성한 대화 수
- 저장한 메시지 수
- 작성한 학습 노트 수
- 학습 시간
- 완료한 학습 주제 수

### 학습 기록 상세 화면

- 학습 날짜
- 학습 주제
- 관련 대화
- 학습 요약
- 핵심 개념
- 개인 메모
- 태그
- 추가로 공부할 내용
- 복습 상태

---

## 우선 개발 순서

### 1단계: 기본 채팅 완성

- [x] 로그인 및 JWT 인증
- [x] 대화 목록 조회
- [x] 대화 선택
- [x] 선택한 대화 메시지 조회
- [x] 메시지 전송
- [x] AI 응답 표시
- [x] SSE 스트리밍 AI 응답 표시
- [x] 대화 생성
- [x] 대화 삭제
- [x] 대화 제목 수정
- [x] 메시지 전송 중 로딩 표시
- [x] 메시지 목록 자동 스크롤
- [x] AI 응답 Markdown 표시
- [x] AI 응답 코드 문법 강조
- [x] 코드 블록 복사

### 2단계: 대화를 학습 기록으로 전환

- [x] Knowledge 폴더 도메인 생성
- [x] KnowledgeNote 학습 노트 도메인 생성
- [x] KnowledgeNote 키워드 저장 구조 생성
- [x] Knowledge/KnowledgeNote Repository 생성
- [x] Knowledge 경로 자동 조회/생성 서비스
- [x] AI 구조화 요약 서비스
- [x] AI 요약 결과 KnowledgeNote 저장 서비스
- [x] Conversation Knowledge 추출 상태 필드 추가
- [x] Conversation Knowledge 추출 재시도/실패 정보 필드 추가
- [x] Knowledge 추출 Batch Job/Step/Tasklet 추가
- [ ] 대화를 수동으로 학습 노트로 변환하는 서비스
- [x] AI 구조화 요약으로 학습 주제, 제목, 설명, 요약, 키워드 추출
- [x] 완료된 대화 기준으로 학습 요약 생성 대상 조회
- [ ] 대화 미사용 시간 기준으로 학습 요약 생성
- [ ] 핵심 개념과 추가 학습 항목 저장
- [ ] 학습 날짜 기록
- [ ] 태그 또는 키워드 기반 분류 기능 추가
- [x] Spring Batch 기반 학습 노트 생성 실행 흐름
- [ ] Spring Scheduler 기반 정기 실행

### 3단계: 학습 기록 탐색

- [ ] `/study` 화면 추가
- [ ] Knowledge 폴더 트리 표시
- [ ] Knowledge별 학습 노트 목록
- [ ] 학습 노트 상세 화면
- [ ] 날짜별 조회
- [ ] 주제 및 태그별 조회
- [ ] 통합 검색

### 4단계: 학습 대시보드

- [ ] 오늘의 학습 표시
- [ ] 최근 학습 주제 표시
- [ ] 누적 및 연속 학습 일수 계산
- [ ] GitHub 형태의 학습 캘린더 구현

### 5단계: 복습 시스템

- [ ] 복습 대상 선정
- [ ] AI 복습 질문 생성
- [ ] 복습 결과 저장
- [ ] 학습 이해도 및 진행 상태 표시

---

## 장기 아이디어

- [ ] AI가 사용자의 학습 관심사 분석
- [ ] 자주 질문하는 개념 자동 분류
- [ ] 서로 관련된 대화와 노트 연결
- [ ] 학습 주제 지식 지도 시각화
- [ ] 주간 및 월간 학습 보고서 생성
- [ ] Markdown 또는 PDF로 학습 기록 내보내기
- [ ] GitHub 학습 저장소와 연결
- [ ] 공부한 기술과 프로젝트 작업 기록 연결
- [ ] 사용자의 학습 흐름을 바탕으로 다음 공부 추천
