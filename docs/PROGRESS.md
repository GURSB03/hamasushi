# 개발 진행 상황

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

### 1-3. 도메인 / 엔티티 (패키지 구조는 3장 참고)
- [ ] `domain/common` — `Japanese`, `English`
- [ ] `domain` — `Category`, `Sentence`, `Word`, `WordBookmark`, `Theme`, `Story`, `Quiz`, `Answer`, `Users`, `Bookmark`, `Progress` (필드 각자 직접 작성, `@MappedSuperclass` 미사용)
- [ ] `repository` — 도메인별 Repository 인터페이스
- [ ] `service` — 도메인별 Service
- [ ] `controller` — 도메인별 Controller

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
| 2026-09-26 | 엔티티 공통 필드는 `@MappedSuperclass`로 추상화하지 않고, 각 엔티티에 직접 작성 | ~~배운 4계층 구조와 혼동 방지~~ → **번복됨 (아래 항목 참고)** |
| 2026-09-26 | 패키지 구조는 **계층(레이어) 기준**으로 확정: `domain / repository / service / controller` | 배운 방식과 통일. `domain` 안에서 `Japanese`/`English`만 `common` 서브패키지로 분리 (Sentence·Word가 공유 참조하므로) |
| 2026-09-26 | `domain` 패키지는 `com.hamasushi.hamasushi` **하위**에 생성 (형제 X) | `src/main/java` 바로 아래는 default package라 컴포넌트 스캔 대상에서 벗어남 |
| 2026-09-26 | **(번복)** `@MappedSuperclass` 다시 도입: `LocalizedExpression`(Sentence/Word 공통), `BookmarkBase`(Bookmark/WordBookmark 공통) | 실제로 엔티티를 다 짜보니 `Sentence`↔`Word`, `Bookmark`↔`WordBookmark` 필드가 그대로 중복되는 게 체감됨. 인터페이스는 필드(상태) 상속이 안 돼서 이 문제엔 부적합 — JPA 필드 상속이 가능한 `@MappedSuperclass`가 유일한 해결책. 이제 JPA 기본기를 익힌 뒤라 혼동 우려보다 중복 제거 이득이 더 큼 |
| 2026-09-26 | **(재번복, 최종)** `@MappedSuperclass` 도입 취소 → 각 엔티티에 필드 직접 작성으로 최종 확정 | 도입 검토 후 보류. 필요하면 나중에 다시 꺼내되, 지금은 각 엔티티(`Sentence`, `Word`, `Bookmark`, `WordBookmark`)에 필드를 그대로 각자 작성 |
| 2026-09-26 | Lombok 도입 확정 (`@Getter`, `@Setter`, `@NoArgsConstructor`) | 직접 타이핑 대신 사용. 단 엔티티엔 `@Data` 미사용 — 연관관계 필드 때문에 `@EqualsAndHashCode`/`@ToString`이 무한 루프 날 수 있어서 필요한 것만 골라 사용 |

---

## 3. 패키지 구조 (확정)

레이어(계층) 기준. `com.hamasushi.hamasushi` 바로 아래에 생성 (형제 X — default package는 컴포넌트 스캔 대상에서 벗어남).

```
com.hamasushi.hamasushi
 ├─ domain/
 │   ├─ common/
 │   │   ├─ Japanese.java
 │   │   └─ English.java
 │   ├─ Category.java
 │   ├─ Sentence.java
 │   ├─ Word.java
 │   ├─ WordBookmark.java
 │   ├─ Theme.java
 │   ├─ Story.java
 │   ├─ Quiz.java
 │   ├─ Answer.java
 │   ├─ Users.java
 │   ├─ Bookmark.java
 │   └─ Progress.java
 │
 ├─ repository/
 │   ├─ CategoryRepository.java
 │   ├─ SentenceRepository.java
 │   ├─ WordRepository.java
 │   ├─ WordBookmarkRepository.java
 │   ├─ ThemeRepository.java
 │   ├─ StoryRepository.java
 │   ├─ QuizRepository.java
 │   ├─ AnswerRepository.java
 │   ├─ UsersRepository.java
 │   ├─ BookmarkRepository.java
 │   └─ ProgressRepository.java
 │
 ├─ service/
 │   ├─ SentenceService.java
 │   ├─ WordService.java
 │   ├─ StoryService.java
 │   ├─ QuizService.java
 │   ├─ UserService.java
 │   └─ BookmarkService.java
 │
 └─ controller/
     ├─ SentenceController.java
     ├─ WordController.java
     ├─ StoryController.java
     ├─ QuizController.java
     └─ AuthController.java
```

- `domain/common`에는 `Sentence`·`Word`가 공유 참조하는 `Japanese`/`English`만 둔다. 그 외 엔티티(`Sentence`, `Word`, `Bookmark`, `WordBookmark` 포함)는 각자 필요한 필드를 직접 선언한다 (`@MappedSuperclass` 미사용, 최종 확정).

---

## 4. 아직 안 정한 것 (`PROJECT_OVERVIEW.md` 8장과 연동)

- [ ] `quiz_type`을 누가 정하나 (A: 백엔드 랜덤 / B: `STORY`에 지정)
- [ ] `quiz_keyword`(상황 키워드) 컬럼을 따로 둘지
- [ ] 퀴즈를 언제 요청할지 (일괄 생성 / prefetch / 캐시만 사용)
- [ ] `STORY.speaker` ENUM 값 확정
- [ ] 프론트 스택, AI 제공자(OpenAI/Gemini) 확정
- [ ] TTS 음성 파일 저장 위치 (로컬 / S3 등)
- [ ] 대화 기록 저장 여부

> ~~단어 북마크를 기존 `BOOKMARK`와 통합할지~~ → **해결됨**: 분리 유지로 확정 (2026-09-26), `PROJECT_OVERVIEW.md`에도 반영 완료.
