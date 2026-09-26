v# 개발 진행 상황

> `PROJECT_OVERVIEW.md`가 "프로젝트가 어떻게 생겼는지"를 정리한 문서라면, 이 문서는 "지금 어디까지 했고 뭘 정했는지"를 정리한 문서다.
> 개발하거나 회의할 때마다 체크리스트와 결정 로그를 갱신한다.

---

## 1. 전체 체크리스트

### 1-1. 기획 / 설계
- [x] 기획 정리
- [x] ERD 1차 확정 (예문 SENTENCE / 단어 WORD 분리 포함)
- [x] 카테고리 후보 목록 정리 (회의에서 최종 확정 예정)
- [ ] 카테고리 최종 확정 (회의)

### 1-2. 환경 설정
- [x] Spring 프로젝트 기본 설정
- [ ] Docker로 MySQL 컨테이너 실행 확인 (`docker compose up -d`)
- [ ] `schema.sql` 작성 + UNIQUE 제약 반영 (3-6 항목들)

### 1-3. 도메인 / 엔티티 (패키지: 테이블 단위로 세분화, 아래 2장 참고)
- [ ] `domain/common` — `Japanese`, `English`, `LocalizedEntry`(@MappedSuperclass), `BookmarkBase`(@MappedSuperclass)
- [ ] `domain/category` — `Category`
- [ ] `domain/sentence` — `Sentence`
- [ ] `domain/word` — `Word`, `WordBookmark`
- [ ] `domain/story` — `Theme`, `Story`, `Quiz`, `Answer`
- [ ] `domain/user` — `Users`, `Bookmark`, `Progress`
- [ ] 각 도메인 Repository 작성

### 1-4. 예문 / 단어 (AI 없음)
- [ ] 예문 데이터 입력
- [ ] 단어 데이터 입력
- [ ] 예문 조회 API
- [ ] 단어 조회 API

### 1-5. 스토리형 퀴즈
- [ ] 스토리 흐름 하드코딩 가짜 퀴즈로 완성 (`currentStepIndex` 검증)
- [ ] AI 퀴즈 생성 연결 (프롬프트 + JSON 4단계 검증)
- [ ] 검증 통과 퀴즈 QUIZ/ANSWER 저장 (캐시, 2안)

### 1-6. 음성 대화
- [ ] TTS 단독 테스트
- [ ] STT 단독 테스트
- [ ] 브라우저 녹음 + 업로드
- [ ] STT → LLM → TTS 파이프라인 연결

### 1-7. 부가 기능 / 마무리
- [ ] 북마크 (예문 / 단어 각각)
- [ ] 진행도 (PROGRESS)
- [ ] 카카오 로그인 (마지막)

---

## 2. 결정 로그

날짜별로 뭘 정했는지, 왜 그렇게 정했는지 짧게 남긴다. `PROJECT_OVERVIEW.md`를 고칠 정도의 결정만 여기 남기고, 문서에도 반영한다.

| 날짜 | 결정 | 이유 / 비고 |
|---|---|---|
| 2026-09-26 | `SENTENCE`/`WORD` 테이블을 합치지 않고 분리 유지 | 둘 다 `JAPANESE`/`ENGLISH`를 공유 재사용해서 실질 중복은 이미 적음. `STORY`가 `SENTENCE`만 참조하는 등 하위 관계가 달라 합치면 오히려 복잡해짐 |
| 2026-09-26 | `BOOKMARK`(예문)와 `WORD_BOOKMARK`(단어)도 분리 유지 | 화면이 이미 분리돼 있고, nullable FK 두 개로 합치면 DB가 "둘 중 하나만" 규칙을 보장 못함 |
| 2026-09-26 | 엔티티 코드 중복은 `@MappedSuperclass`로 해결 | `LocalizedEntry`(category, japanese, english, kor 공통 필드), `BookmarkBase`(id, user, createdAt 공통 필드). 테이블은 그대로 분리되고 자바 코드만 공통화 |
| 2026-09-26 | 패키지 구조를 3대 영역(예문단어/스토리퀴즈/사용자)이 아니라 **테이블 단위**로 세분화 | `common / category / sentence / word / story / user`. 탐색 범위를 좁히기 위함. 의존 방향은 `sentence`/`word` → `common` 한 방향만 |
| 2026-09-26 | `domain` 패키지는 `com.hamasushi.hamasushi` **하위**에 생성 (형제 X) | `src/main/java` 바로 아래는 default package라 컴포넌트 스캔 대상에서 벗어남 |

---

## 3. 아직 안 정한 것 (`PROJECT_OVERVIEW.md` 8장과 연동)

- [ ] `quiz_type`을 누가 정하나 (A: 백엔드 랜덤 / B: `STORY`에 지정)
- [ ] `quiz_keyword`(상황 키워드) 컬럼을 따로 둘지
- [ ] 퀴즈를 언제 요청할지 (일괄 생성 / prefetch / 캐시만 사용)
- [ ] `STORY.speaker` ENUM 값 확정
- [ ] 프론트 스택, AI 제공자(OpenAI/Gemini) 확정
- [ ] TTS 음성 파일 저장 위치 (로컬 / S3 등)
- [ ] 대화 기록 저장 여부

> ~~단어 북마크를 기존 `BOOKMARK`와 통합할지~~ → **해결됨**: 분리 유지로 확정 (2026-09-26), `PROJECT_OVERVIEW.md`에도 반영 완료.