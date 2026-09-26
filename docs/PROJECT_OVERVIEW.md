# 프로젝트 정리

> 외국어(일본어/영어) 학습 서비스. 예문 조회, 스토리형 AI 퀴즈, 음성 대화 세 가지 기능으로 구성.
> 이 문서는 새 채팅이나 AI 코딩 도구에 컨텍스트로 넘기기 위한 요약본입니다. 결정이 바뀌면 이 문서도 함께 수정하세요.

---

## 1. 서비스 개요

### 1-1. 예문 / 단어
- 예문·단어 모두 우리가 미리 준비해 둔 데이터를 **단순 조회**한다 (AI 없음).
- **예문(SENTENCE):** 한 문장이 한국어 + 일본어 + 영어 세트이고, 카테고리로 분류된다.
- **단어(WORD):** 문장이 아닌 단어 단위로, 마찬가지로 한국어 + 일본어 + 영어 세트이고 카테고리로 분류된다.
- 카테고리 상세 화면에서는 **예문 목록이 기본으로 노출**되고, "필수 단어 보러가기"를 누르면 같은 카테고리의 **단어 목록**으로 이동한다 (예문과 단어는 같은 카테고리를 공유하지만 별개의 목록·별개의 테이블).

### 1-2. 스토리형 퀴즈
- **역할 분담:** DB(스토리 뼈대) + AI(퀴즈 내용 생성)
- **스토리 흐름(선로):** DB의 `STORY` 테이블(`step_order`)로 통제
- **퀴즈 내용(알맹이):** DB에는 핵심 문장과 문제 유형만 지정하고, 백엔드가 OpenAI/Gemini API를 호출해 퀴즈를 생성한다.
- **퀴즈를 맞췄을 때**
    - 프론트엔드 상태(`currentStepIndex`) 중심으로 제어한다.
    - 스토리 초기 로딩 시 스텝 배열을 받아오고, 정답 판별과 다음 스텝 이동은 프론트가 즉시 처리한다 (서버 왕복 없음).
    - 스토리를 **최종 완주했을 때만** 백엔드 완료 API를 호출한다.

### 1-3. 음성 대화
- 백엔드 HTTP REST API 방식
- 프론트에서 마이크 녹음(`.webm`/`.wav`) → 백엔드로 `FormData` 전송
- 백엔드: **STT(Whisper) → LLM(OpenAI/Gemini) → TTS(ElevenLabs/OpenAI)** 3단계 파이프라인
- 응답: 음성 URL + 텍스트
- 대화 기록을 저장하지 않는다면 별도 테이블은 필요 없다.

### 1-4. 로그인
- 카카오 로그인 (OAuth2)
- **개발 우선순위 낮음.** 개발 중에는 DB에 테스트 유저 1명을 넣고 `user_id = 1`로 고정해서 쓰다가, 마지막에 로그인을 붙인다.

---

## 2. 기술 스택

| 항목 | 상태 |
|---|---|
| 백엔드 | Spring (설정 완료) |
| DB | **MySQL 8.4 LTS** (Docker 컨테이너로 실행, 10-6 참고) |
| 프론트 | 미정 |
| AI (퀴즈, 대화) | 미정 (OpenAI / Gemini 중 선택) |
| STT / TTS | Whisper / ElevenLabs 또는 OpenAI |

---

## 3. ERD (최종)

퀴즈 처리 2안(AI 생성 → 검증 → DB 저장) 기준이다. 전체는 예문 / 스토리·퀴즈 / 사용자 세 영역이다.

### 3-1. 예문·단어 영역

| 테이블 | 컬럼 | 역할 |
|---|---|---|
| **JAPANESE** | id, jpn, jpn_pro | 일본어 예문과 한글 발음 |
| **ENGLISH** | id, eng, eng_pro | 영어 예문과 한글 발음 |
| **CATEGORY** | id, name | 예문·단어·스토리의 분류 (예: 여행, 식당) |
| **SENTENCE** | id, category_id, jpn_id, eng_id, kor | 한국어 + 일본어 + 영어를 한 세트로 묶는 **문장** 핵심 테이블 |
| **WORD** | id, category_id, jpn_id, eng_id, kor | 한국어 + 일본어 + 영어를 한 세트로 묶는 **단어** 테이블. `jpn_id`/`eng_id`는 SENTENCE와 동일하게 `JAPANESE`/`ENGLISH`를 참조·재사용 (같은 단어가 단어장과 문장에 중복 저장되지 않도록) |

### 3-2. 스토리·퀴즈 영역

| 테이블 | 컬럼 | 역할 |
|---|---|---|
| **THEME** | id, category_id, title | 스토리 한 편 (예: "공항에서 길 묻기") |
| **STORY** | id, theme_id, sentence_id (nullable), step_order, narrative, speaker (ENUM), is_quiz | 스토리의 한 스텝. `step_order`로 순서 통제, `is_quiz = true`면 퀴즈 스텝 |
| **QUIZ** | id, story_id, quiz_text, quiz_type (ENUM) | AI가 만든 문제와 문제 유형 |
| **ANSWER** | id, quiz_id, answer, is_correct, explanation | 선택지 1개 = 1행. `explanation`은 **그 선택지의 해설** (AI JSON에서는 `explain` 키) |

> 이름 주의: THEME이 "스토리 한 편", STORY가 "스토리의 한 스텝"이다. 헷갈리면 `STORY` / `STORY_STEP`으로 바꾸는 것도 고려할 수 있다.

### 3-3. 사용자 영역

| 테이블 | 컬럼 | 역할 |
|---|---|---|
| **USERS** | id, email, kakao_id (unique), nickname | 카카오 로그인 사용자 |
| **BOOKMARK** | id, user_id, sentence_id | 사용자가 저장한 예문 |
| **WORD_BOOKMARK** | id, user_id, word_id | 사용자가 저장한 단어. `BOOKMARK`(예문)와는 화면·API가 분리돼 있어 별도 테이블로 확정 |
| **PROGRESS** | id, user_id, theme_id, complete | 사용자의 스토리 완주 기록 |

### 3-4. 관계 (1 : N)

| 관계 | 의미 |
|---|---|
| JAPANESE / ENGLISH → SENTENCE, WORD | 일본어·영어 표현 하나를 여러 문장·단어 행이 참조 (문장과 단어가 같은 표현을 공유 가능) |
| CATEGORY → SENTENCE, WORD, THEME | 카테고리 하나에 여러 예문·단어·스토리 |
| THEME → STORY | 스토리 한 편에 여러 스텝 |
| SENTENCE → STORY | 예문 하나가 여러 스텝에서 쓰일 수 있음 (스텝은 예문 없이도 가능) |
| STORY → QUIZ | 스텝 하나에 퀴즈 여러 개 (캐시된 퀴즈 풀) |
| QUIZ → ANSWER | 문제 하나에 선택지 여러 개 |
| USERS → BOOKMARK ← SENTENCE | 사용자와 예문의 다대다 연결 |
| USERS → WORD_BOOKMARK ← WORD | 사용자와 단어의 다대다 연결 |
| USERS → PROGRESS ← THEME | 사용자와 스토리의 다대다 연결 |

### 3-5. 설계 시 반영한 결정과 이유

| 결정 | 이유 |
|---|---|
| USER → **USERS** | `user`는 일부 DB(PostgreSQL 등)에서 예약어. MySQL에서는 문제없지만 DB를 바꿔도 안전하도록 유지 |
| `order` → **step_order** | `order`는 SQL 예약어 |
| `password` 삭제 | 카카오 로그인이라 비밀번호를 받지 않음 |
| `kakao_id` unique | 카카오 고유 번호로 가입 여부 확인, 중복 가입 방지 |
| `STORY.sentence_id` nullable | 예문 없이 서사·대사만 있는 스텝 허용 |
| `ANSWER.explanation` 유지 (컬럼명 `explain` → `explanation`) | **선택지마다 해설을 보여주기로 함.** `EXPLAIN`은 MySQL 예약어라 컬럼명으로 쓰면 SQL 오류 위험 |
| `QUIZ.quiz_type` 추가 | 문제 유형(빈칸 채우기 등)에 따라 프롬프트와 검증이 달라짐 |

### 3-6. 아직 적용 안 한 항목 (DB 생성 시 반영 권장)

| 항목 | 내용 | 중요도 |
|---|---|---|
| UNIQUE `(theme_id, step_order)` | 한 스토리 안에서 순서 번호 중복 방지 | 권장 |
| UNIQUE `(user_id, theme_id)` | 같은 스토리 완료 기록 중복 방지 | 권장 |
| UNIQUE `(user_id, sentence_id)` | 예문 북마크 중복 방지 | 권장 |
| UNIQUE `(user_id, word_id)` | 단어 북마크 중복 방지 (WORD_BOOKMARK) | 권장 |
| `USERS.email` nullable | 카카오에서 이메일 동의를 안 하면 못 받을 수 있음 | 권장 |
| `USERS.created_at` | 가입일 기록 | 선택 |
| `QUIZ.created_at`, `QUIZ.is_active` | 생성일 기록 / 이상한 퀴즈를 삭제하지 않고 끄기 | 선택 |
| `PROGRESS.complete` | 현재 BOOLEAN 유지. 완주 시에만 row를 만든다면 `completed_at`(완료 시각)으로 대체 가능 | 선택 |
| `STORY.speaker` ENUM 값 | 예: `NARRATOR`, `USER`, `NPC` 등 종류를 확정해야 함 | 필요 |

### 3-7. MySQL 기준 반영 사항

| 항목 | 내용 |
|---|---|
| 문자셋 `utf8mb4` | 한국어·일본어·이모지 저장용. `compose.yaml`에서 명시해 둔다 (10-6) |
| ENUM | `STORY.speaker`, `QUIZ.quiz_type`은 MySQL ENUM으로 가능. JPA에서는 `@Enumerated(EnumType.STRING)` 사용 (`ORDINAL`은 순서가 바뀌면 데이터가 깨진다) |
| BOOLEAN | MySQL에서는 `TINYINT(1)`로 저장됨 (`is_quiz`, `is_correct`, `complete`). JPA가 알아서 매핑 |
| 예약어 | `order`, `explain`은 이미 피했다. 새 컬럼명을 정할 때 MySQL 예약어인지 확인 |
| UNIQUE / FK | 3-6의 UNIQUE 제약과 외래키는 InnoDB(기본 엔진)에서 그대로 사용 가능. 데이터 입력은 부모 → 자식 순서 |

---

## 4. 퀴즈 처리 방식

### 4-1. 핵심 원칙 (공통)
- AI는 문장을 지어내지 않는다. **DB의 검증된 문장을 재료로 주고 문제만 가공**시킨다 (틀린 외국어 방지).
- AI 호출은 프론트가 아니라 **백엔드에서만** 한다 (API 키 보호).
- 프론트는 AI가 아니라 우리 백엔드 API만 호출한다.
- 같은 프롬프트라도 결과는 매번 조금씩 다르다. 문제가 달라지는 것은 괜찮고, **형식이 맞는지와 내용이 틀리지 않는지**를 통제하는 것이 핵심이다.

### 4-2. 1안: 실시간 생성 (저장 안 함)
- 퀴즈가 필요할 때마다 AI에게 요청하고 쓰고 버린다.
- ERD에서 QUIZ / ANSWER 테이블이 필요 없다.
- 장점: 구조가 단순하고 매번 새 문제가 나온다.
- 단점: 퀴즈 스텝마다 AI 응답을 기다려야 하고(수 초), 호출마다 비용이 들며, 이상한 문제를 걸러낼 방법이 코드 검증뿐이다.

### 4-3. 2안: 생성 + 저장 (캐시) ← **채택**
- AI가 만들고 검증을 통과한 퀴즈를 DB에 쌓아 재사용한다. 없으면 만들어서 저장하고, 있으면 꺼내 쓴다.
- 장점: 비용 절감, 응답 속도 향상, 사람이 검수 가능, AI 장애 시에도 저장된 퀴즈로 서비스 유지.
- 단점: 구현이 조금 더 많고, 스텝당 퀴즈를 3~5개 쌓지 않으면 같은 문제만 반복된다.

### 4-4. 비교

| | 1안 (실시간) | 2안 (캐시) |
|---|---|---|
| 구현 난이도 | 쉬움 | 보통 |
| 사용자 대기 시간 | 매번 수 초 | 처음만 수 초, 이후 빠름 |
| AI 비용 | 높음 | 낮음 |
| 문제 다양성 | 항상 새 문제 | 저장된 풀에서 랜덤 |
| 품질 관리 | 코드 검증만 | 코드 검증 + 사람 검수 |
| QUIZ/ANSWER 테이블 | 삭제 | 유지 |

### 4-5. 개발 접근: 2안으로 가되 1안 순서로 시작
1. 스토리 흐름을 **가짜 퀴즈 JSON(하드코딩)** 으로 먼저 완성한다 (`currentStepIndex` 로직 검증).
2. AI 호출 + JSON 검증 + fallback을 붙인다 (이 단계가 곧 1안).
3. 검증 통과한 퀴즈를 QUIZ/ANSWER에 저장하고 "있으면 꺼내 쓰기"를 붙인다 (2안 완성).

### 4-6. "대기 시간 0초"와의 충돌 해결
정답 판별은 프론트에서 즉시 처리되지만, **퀴즈 내용을 가져오는 시점**에는 AI 응답을 기다릴 수 있다. 해결 방법은 다음과 같다.
- 스토리 시작 시 퀴즈들을 병렬로 미리 생성해서 내려주기 (첫 로딩은 길어짐)
- 퀴즈 바로 앞 스텝에서 다음 퀴즈를 미리 요청하기 (prefetch)
- 2안의 캐시 사용 (저장된 퀴즈는 즉시 응답)

---

## 5. AI 응답 JSON 형식

선택지별 해설을 쓰기로 했으므로 형식은 다음과 같다.

```json
{
  "question": "駅は ___ ですか。",
  "quiz_type": "FILL_BLANK",
  "options": [
    { "text": "どこ", "is_correct": true,  "explain": "'어디'라는 뜻으로 장소를 물을 때 씁니다." },
    { "text": "いつ", "is_correct": false, "explain": "'언제'라는 뜻이라 시간을 물을 때 씁니다." },
    { "text": "だれ", "is_correct": false, "explain": "'누구'라는 뜻이라 사람을 물을 때 씁니다." },
    { "text": "なに", "is_correct": false, "explain": "'무엇'이라는 뜻이라 사물을 물을 때 씁니다." }
  ]
}
```

| JSON | 저장 위치 |
|---|---|
| question | QUIZ.quiz_text |
| quiz_type | QUIZ.quiz_type |
| options[i].text | ANSWER.answer |
| options[i].is_correct | ANSWER.is_correct |
| options[i].explain | ANSWER.explanation |

### 프롬프트에 넣을 재료 (예시)
```
핵심 문장: 駅はどこですか (한국어: 역이 어디예요?)   ← STORY.sentence_id → SENTENCE
스토리 상황: 여행 중 길을 묻는 장면                    ← THEME.title / STORY.narrative
퀴즈 유형: 빈칸 채우기                                 ← quiz_type
학습자 수준: 초급
```
system 프롬프트에는 "반드시 JSON 형식으로만 답하고, 정답은 제공된 핵심 문장에 근거해야 한다"를 명시한다.

---

## 6. JSON 검증 (4단계)

| 단계 | 확인 내용 | 방법 |
|---|---|---|
| 1. 파싱 | JSON으로 읽히는가 | 마크다운(```` ```json ````) 제거 후 파싱, 실패하면 재시도 |
| 2. 스키마 | 필드와 타입이 맞는가 | 스키마 라이브러리 (Spring이면 Jackson + Bean Validation) |
| 3. 논리 | 값들이 서로 앞뒤가 맞는가 | 아래 목록 참고 |
| 4. 내용 | 일본어/영어가 정말 맞는 문장인가 | 사람 검수 (필요하면 AI 2차 검증) |

**3단계 논리 검사 목록**
- 선택지가 4개인가
- 선택지에 빈 문자열이나 중복이 없는가
- **`is_correct`가 true인 선택지가 정확히 1개인가**
- (빈칸 유형) `question`에 `___`가 있는가
- (빈칸 유형) 정답을 빈칸에 넣으면 DB의 핵심 문장과 같아지는가 (가장 강력한 검사)
- 일본어 퀴즈라면 일본어 문자가 실제로 들어 있는가

**실패 처리:** 재시도(최대 3회) → 그래도 실패하면 미리 준비한 대체(fallback) 퀴즈를 사용한다. **검증을 통과한 퀴즈만 DB에 저장**한다. 어떤 검증에서 자주 걸리는지 로그를 남기면 프롬프트를 고칠 위치를 알 수 있다.

---

## 7. 데이터 흐름

### ① 예문 조회 (AI 없음)
```
프론트 → 백엔드: 카테고리 선택
백엔드: CATEGORY → SENTENCE → JAPANESE/ENGLISH 조인 조회
프론트 ← 한국어/일본어/영어/발음 목록
```

### ①-1 단어 조회 ("필수 단어 보러가기", AI 없음)
```
프론트 → 백엔드: 카테고리 선택 (단어 보기)
백엔드: CATEGORY → WORD → JAPANESE/ENGLISH 조인 조회
프론트 ← 한국어/일본어/영어/발음 단어 목록
```

### ② 스토리 시작
```
프론트 → 백엔드: 스토리(theme) 시작 요청
백엔드: THEME → STORY를 step_order 순으로 조회
        (sentence_id가 있으면 SENTENCE 내용도 함께)
프론트 ← 스텝 배열 (서사, 화자, 예문, is_quiz 여부)
프론트: currentStepIndex = 0으로 시작, 스텝을 순서대로 화면에 표시
```

### ③ 퀴즈 스텝 도달 (`is_quiz = true`)
```
프론트 → 백엔드: 이 스텝의 퀴즈 요청
백엔드: QUIZ에서 story_id로 조회
  ├ 있음 → 그중 랜덤 1개 + ANSWER 조인 ──────────┐
  └ 없음/부족                                      │
       → STORY의 핵심 문장 + quiz_type으로 프롬프트 구성
       → AI 호출 → JSON 검증 (실패 시 재시도, 그래도 실패면 대체 퀴즈)
       → 통과한 것만 QUIZ + ANSWER에 저장 ────────┤
프론트 ←──────────── 퀴즈 JSON ────────────────────┘
```

### ④ 정답 처리 (프론트 즉시 처리)
```
사용자가 선택지 클릭
프론트: is_correct로 정오답 판별, 그 선택지의 explain 표시 (서버 왕복 없음)
프론트: currentStepIndex + 1 → 다음 스텝
```

### ⑤ 완주
```
마지막 스텝 종료 → 프론트 → 백엔드: 완료 API 호출
백엔드: PROGRESS에 (user_id, theme_id, complete=true) 저장
```

### ⑥ 북마크
```
프론트 → 백엔드: 예문 북마크 추가/삭제 (sentence_id)
백엔드: BOOKMARK에 저장/삭제

프론트 → 백엔드: 단어 북마크 추가/삭제 (word_id)
백엔드: WORD_BOOKMARK에 저장/삭제
```

### ⑦ 음성 대화 (테이블 없음)
```
프론트: 녹음 (.webm/.wav) → FormData로 백엔드 전송
백엔드: STT(Whisper) → LLM → TTS → 음성 URL + 텍스트 응답
프론트 ← 재생 + 텍스트 표시
```

---

## 8. 정해야 할 것 (미정 사항)

1. **quiz_type을 누가 정하나?**
   문제를 생성하기 전에 유형을 알아야 프롬프트를 만들 수 있다.
    - 방법 A: 백엔드가 랜덤으로 유형을 골라 생성하고 결과를 `QUIZ.quiz_type`에 기록한다. (현재 ERD 그대로 가능, 문제가 다양해짐)
    - 방법 B: `STORY`에도 `quiz_type`을 두어 스텝마다 유형을 지정한다. (스토리 작성자가 통제하고 싶을 때)
    - 처음에는 A로 시작해도 충분하다.
2. **`quiz_keyword`(상황 키워드)를 둘 것인가?** 없어도 `THEME.title`이나 `STORY.narrative`를 프롬프트에 넣어 대체할 수 있다.
3. **퀴즈를 언제 요청할 것인가?** 스토리 시작 시 일괄 / 직전 스텝에서 prefetch / 캐시 (4-6 참고)
4. **`STORY.speaker` ENUM 값** 확정
5. **프론트 스택, AI 제공자(OpenAI/Gemini)** 확정 (DB는 MySQL 8.4 + Docker로 확정)
6. **TTS 음성 파일 저장 위치** (로컬, S3 등)
7. **대화 기록을 저장할 것인가?** 복습 기능이 필요하면 나중에 `CONVERSATION` 테이블 추가

---

## 9. 개발 순서 (마지막에 AI를 붙인다)

1. ERD 확정 + 예문 데이터 수십 개 입력 (데이터 준비가 생각보다 큰 작업)
2. 예문 조회 API와 화면 (AI 없음)
3. 스토리 흐름을 **하드코딩한 가짜 퀴즈 JSON**으로 완성 (`currentStepIndex` 로직 검증)
4. 가짜 퀴즈를 AI 생성으로 교체 (프롬프트 + JSON 검증 공부)
5. 음성은 STT만 / LLM만 / TTS만 각각 따로 테스트한 뒤 연결
6. 북마크와 진행도 추가
7. 마지막에 카카오 로그인(OAuth2)

---

## 10. 환경 설정 (.env / application.yaml)

이 프로젝트는 DB 비밀번호뿐 아니라 **OpenAI/Gemini API 키, ElevenLabs API 키, 카카오 OAuth 클라이언트 시크릿**까지 시크릿이 여러 개 쌓인다. 시크릿은 전부 **`.env` 파일 하나**에 모으고, Docker Compose와 Spring이 같은 `.env`를 읽도록 한다.

### 10-1. 파일 역할 분리

| 파일 | 커밋 여부 | 용도 |
|---|---|---|
| `application.yaml` | ✅ 커밋 | 구조만 정의, 실제 값은 `${...}` 환경변수 참조 |
| `.env` | ❌ (`.gitignore`) | 실제 시크릿 값 (DB 비밀번호, API 키 등). 유일한 시크릿 저장소 |
| `compose.yaml` | ✅ 커밋 | MySQL 컨테이너 정의 (시크릿 없이 `${DB_PASSWORD}`만 참조) |

### 10-2. `application.yaml` (커밋되는 파일)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3307/hamasushi?serverTimezone=Asia/Seoul&characterEncoding=UTF-8&allowPublicKeyRetrieval=true&useSSL=false
    username: root
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update   # 개발 초기용. schema.sql로 관리하기 시작하면 validate 또는 none으로 변경
    properties:
      hibernate:
        format_sql: true
    show-sql: true

# 카카오 로그인 (OAuth2) - 붙이는 시점에 주석 해제
# spring.security.oauth2.client.registration.kakao:
#   client-id: ${KAKAO_CLIENT_ID}
#   client-secret: ${KAKAO_CLIENT_SECRET}

# AI / STT / TTS - 콜론(:) 뒤가 기본값. 키를 아직 안 넣었어도 앱이 시작된다
ai:
  openai:
    api-key: ${OPENAI_API_KEY:}
  gemini:
    api-key: ${GEMINI_API_KEY:}
  elevenlabs:
    api-key: ${ELEVENLABS_API_KEY:}
```

- `DB_PASSWORD`는 기본값을 주지 않았다. 없으면 앱이 바로 실패해서 설정 누락을 금방 알 수 있다.
- 카카오 설정은 키가 없는 상태에서 켜져 있으면 시작 단계에서 에러가 날 수 있으므로, 로그인을 붙일 때 주석을 푼다.

### 10-3. `.env`

**`.env`** (프로젝트 루트, 커밋 안 됨)

```
DB_PASSWORD=실제비밀번호
# 아래는 사용하는 시점에 추가 (OPENAI / GEMINI는 쓰는 것만)
# OPENAI_API_KEY=sk-실제키
# GEMINI_API_KEY=실제키
# ELEVENLABS_API_KEY=실제키
# KAKAO_CLIENT_ID=실제카카오키
# KAKAO_CLIENT_SECRET=실제카카오시크릿
```

- `=` 양옆에 공백을 넣지 않는다. 값에 따옴표도 필요 없다.
  **필요한 키 정리**

| 키 | 필요한 시점 | 발급처 | 비고 |
|---|---|---|---|
| `DB_PASSWORD` | 지금 | 직접 정함 | `compose.yaml`(MySQL 생성)과 Spring(접속)이 같이 사용 |
| `OPENAI_API_KEY` 또는 `GEMINI_API_KEY` | AI 퀴즈 붙일 때 (개발 순서 4) | OpenAI Platform / Google AI Studio | LLM은 **하나만** 선택. 선택 안 한 쪽은 `.env`, `application.yaml`에서 삭제 |
| `ELEVENLABS_API_KEY` | 음성 대화에서 TTS로 ElevenLabs를 쓸 때 (개발 순서 5) | ElevenLabs | TTS를 OpenAI로 하면 불필요 |
| `KAKAO_CLIENT_ID` | 로그인 붙일 때 (개발 순서 7) | Kakao Developers → 내 애플리케이션 → **REST API 키** | Spring의 `client-id`에 해당 |
| `KAKAO_CLIENT_SECRET` | 위와 동일 | Kakao Developers → 카카오 로그인 → 보안 → **Client Secret** 발급 및 활성화 | Spring의 `client-secret`에 해당 |

- OpenAI로 정하면 Whisper(STT) + LLM + TTS를 **키 하나**로 처리할 수 있어 가장 단순하다. Gemini나 ElevenLabs를 섞으면 키가 그만큼 늘어난다.
- 카카오는 키 외에도 Kakao Developers에서 **Redirect URI 등록**이 필요하다 (Spring 기본값 기준 `http://localhost:8080/login/oauth2/code/kakao`, 포트가 다르면 맞춘다). 이메일 동의항목은 설정에 따라 못 받을 수 있으므로 `USERS.email`은 nullable로 둔다 (3-6 참고).
- 로그인 후 자체 JWT를 발급하는 방식을 쓰면 `JWT_SECRET` 같은 키가 하나 더 필요하다. 세션 방식이면 필요 없다.
- 새 시크릿이 생기면 `.env`에 값을 추가하고, `application.yaml`에는 `${키이름}`만 추가한다.

### 10-4. Spring에서 `.env` 읽기 (IntelliJ)

`.env`는 **Docker Compose만 자동으로 읽는다.** Spring은 자동으로 읽지 않으므로 실행할 때 환경변수로 넘겨줘야 한다. IntelliJ에서는 **EnvFile 플러그인**을 쓴다.

1. `Settings → Plugins`에서 **EnvFile** 설치 후 IntelliJ 재시작 (`.env`를 열었을 때 상단에 뜨는 안내 배너로도 설치 가능)
2. 상단 실행 설정 `HamasushiApplication` → `Edit Configurations`
3. **EnvFile** 탭에서 `Enable EnvFile` 체크 → `+` → `.env` 추가

- 이 설정은 IntelliJ에서 실행할 때만 적용된다. 터미널에서 `./gradlew bootRun`으로 실행한다면 `.env` 값을 환경변수로 먼저 올려야 한다 (bash 예: `set -a; source .env; set +a; ./gradlew bootRun`).
- `Could not resolve placeholder 'DB_PASSWORD'` 에러가 나면 EnvFile 설정이 빠진 것이다.
- 비밀번호는 `.env` 한 곳에만 있으므로, 바꿀 때는 `.env`만 고치면 된다.

### 10-5. `.gitignore`에 추가

```
### Secrets ###
.env
```

### 10-6. Docker로 MySQL 실행

로컬에 MySQL을 직접 설치하지 않고 Docker(MySQL 8.4 LTS)로 띄운다. MySQL 8.0은 2026년 4월에 지원이 종료되어 8.4를 쓴다. `application.yaml`의 JDBC URL과 **포트·DB 이름을 맞춘다.**

**`compose.yaml`** (프로젝트 루트, 커밋 가능, 시크릿 없음)

```yaml
services:
  mysql:
    image: mysql:8.4
    container_name: hamasushi-mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_PASSWORD}
      MYSQL_DATABASE: hamasushi
      TZ: Asia/Seoul
    command:
      - --character-set-server=utf8mb4
      - --collation-server=utf8mb4_unicode_ci
    ports:
      - "3307:3306"
    volumes:
      - mysql-data:/var/lib/mysql
      # schema.sql을 첫 실행 때 자동 적용하고 싶다면 주석 해제 (db/init 폴더에 .sql 파일 배치)
      # - ./db/init:/docker-entrypoint-initdb.d

volumes:
  mysql-data:
```

- `${DB_PASSWORD}`는 같은 폴더의 `.env`(10-3 참고)에서 자동으로 읽어온다. `compose.yaml` 자체에는 실제 비밀번호가 없으므로 커밋해도 된다.
- 포트를 3307로 매핑한 이유: 로컬에 MySQL을 따로 설치해 3306을 쓰고 있을 경우 충돌 방지.
- 컨테이너 이름과 DB 이름은 **영문**으로 쓴다. Docker 컨테이너 이름은 한글을 허용하지 않고, DB 이름이 한글이면 JDBC URL·덤프·쉘에서 인코딩 문제가 생기기 쉽다.
- `MYSQL_ROOT_PASSWORD`, `MYSQL_DATABASE`는 **볼륨이 처음 만들어질 때만** 적용된다. 비밀번호나 DB 이름을 바꿨는데 반영이 안 되면 `docker compose down -v`로 볼륨을 지우고 다시 올린다 (데이터도 같이 삭제됨).
- `docker-entrypoint-initdb.d`의 SQL도 마찬가지로 **첫 실행 때만** 한 번 실행된다.

**처음 실행 순서**

1. 프로젝트 루트에 `.env` 생성 (`DB_PASSWORD=...`)
2. `docker compose up -d`
3. `docker compose logs -f mysql`에서 `ready for connections`가 뜨면 준비 완료
4. IntelliJ에서 Spring 애플리케이션 실행 (EnvFile 설정이 되어 있는지 확인, 10-4)

**자주 쓰는 명령어**

```bash
docker compose up -d          # 컨테이너 시작 (백그라운드)
docker compose down           # 컨테이너 중지 (데이터는 유지됨)
docker compose down -v        # 컨테이너 중지 + 데이터까지 완전 삭제 (초기화하고 싶을 때)
docker compose logs -f mysql  # 로그 확인
docker exec -it hamasushi-mysql mysql -uroot -p   # 컨테이너 안 MySQL 쉘 접속 (비밀번호 입력)
```

**다른 기기(노트북 등)로 데이터 옮기기**

Windows PowerShell에서는 `>` 리다이렉트가 파일을 UTF-16으로 저장해 한글/일본어가 깨질 수 있으므로, 컨테이너 안에서 파일로 저장한 뒤 `docker cp`로 꺼낸다.

```bash
# desktop에서 덤프
docker exec hamasushi-mysql sh -c 'mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" --default-character-set=utf8mb4 --result-file=/tmp/dump.sql hamasushi'
docker cp hamasushi-mysql:/tmp/dump.sql ./dump.sql

# 노트북에서 (컨테이너 띄운 뒤) 복원
docker cp ./dump.sql hamasushi-mysql:/tmp/dump.sql
docker exec hamasushi-mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" --default-character-set=utf8mb4 hamasushi < /tmp/dump.sql'
```

`dump.sql`은 시크릿이 아니라 예문/스토리 데이터이므로, 필요하면 git이 아니어도 그냥 파일로 주고받아도 된다 (단, 실제 유저 개인정보가 들어가면 커밋 금지).

### 10-7. 원칙

- **AI API 키는 절대 프론트엔드 코드에 넣지 않는다.** 백엔드에서만 호출하고(4-1 원칙과 동일 이유), 프론트는 우리 백엔드 API만 호출한다.
- 새 시크릿(카카오 키, TTS 키 등)이 추가될 때마다 `.env`에만 값을 넣고, `application.yaml`에는 `${키이름}`만 추가한다.
- 만약 실수로 커밋된 적이 있다면 즉시 해당 키를 재발급(rotate)하고 git 히스토리에서 제거한다.

---

## 11. 진행 상황

> 세부 체크리스트와 결정 로그는 `PROGRESS.md`에서 관리한다. 이 문서는 "프로젝트가 어떻게 생겼는지"에 집중하고, "지금 어디까지 했는지"는 `PROGRESS.md` 한 곳에서만 갱신한다.