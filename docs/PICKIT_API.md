# PICKIT MVP 백엔드 API와 역할 분담 v1

작성일 2026-11-12. 사용자 확인과 Figma 작업 화면을 기준으로 작성한 설계 초안이다. 아래 API와 DB는 구현 완료 상태가 아니다.

## 확인된 서비스 규칙

- 전체 교환은 로그인한 누구나 이용한다. 홈의 '지금 올라온 교환글'은 항상 전체 교환 기준이며 현장 인증으로 범위가 바뀌지 않는다.
- 현장 교환글은 같은 팝업의 유효한 위치 인증자만 목록·상세 열람, 작성, 신규 채팅 시작이 가능하다.
- 현장 글이 이탈 후 일반 글로 전환되면 전체 교환에 공개된다.
- 로그인은 이메일·비밀번호와 Google을 모두 지원한다. 인증 시작·콜백 이외의 업무 API는 인증이 필요하다.
- 교환글 작성에 카테고리를 추가한다.
- 교환 확정 시 시간 선택은 없다. 확정 후 버튼이 '교환 완료'로 바뀐다.
- 교환글은 일반(ACTIVE) → 교환 중(TRADING) → 교환 완료(COMPLETED) 순서다. 교환 확정 버튼을 누르면 글과 교환 기록이 TRADING이 되고 해당 상대방의 채팅방만 내 약속 탭에 나타난다. 완료 버튼을 누르면 두 상태가 COMPLETED가 되어 내 약속에서 빠지며, 글과 대화 기록은 남는다. 전체/현장이라는 공개 범위는 이 교환 상태와 별개다.
- 완료해도 글을 삭제하지 않는다. 별도 예약 기능과 '예약 중' 게시글 상태는 두지 않는다.
- New 태그는 일정 시간이 지나면 사라진다.
- 평점 모달은 기획 중이다. 메모에 적힌 '신청자는 사진만 전송'은 확정 요구사항이 아니다.

## 2인 역할 분담

두 사람이 하나의 Spring Boot 애플리케이션과 DB를 함께 개발한다.

| 담당 | 책임 |
|---|---|
| A 구교현 | 팝업 데이터 반영, 목록·상세·필터·주변 조회, 현장 인증과 이탈, 게시글 공개 범위와 전환, 교환글 CRUD, 찜, 카테고리·관심사, 홈 추천, 공통 이미지 업로드 |
| B 다른 백엔드 | 이메일·Google 인증, 토큰과 사용자 기본 정보, 채팅방·메시지·읽음·실시간 전달, 교환 확정과 완료, 나의 교환 중·완료 내역 |
| 공동 합의 | 공통 사용자 ID·오류 응답·인증 전달 방식·DB 마이그레이션 순번·시간 형식·CORS·배포 환경 |

A 23개 HTTP API, B 17개 HTTP/OAuth 경로와 WebSocket 연결 1개를 제안한다. 개수는 공평함의 절대 기준이 아니다. B는 두 로그인과 실시간 채팅 난이도가 있으므로 공통 이미지 업로드·관심사·홈 데이터 조합은 A가 맡는다. 초기 DB 설계와 팝업 데이터 반영도 A가 정리한다.

## A 담당 API

| ID | 방식 | 경로 | 기능 | 권한 | 주요 규칙 |
|---|---|---|---|---|---|
| A01 | GET | /api/categories | 상품·관심사 카테고리 목록 | 로그인 | 기획 목록을 공통 코드로 관리. 관심사 저장 시 같은 ID 사용. |
| A02 | GET | /api/popups/filters | 팝업 필터 선택지 | 로그인 | 지원 지역·카테고리·진행 상태만 반환. 서울 외 미지원 항목은 미노출. |
| A03 | GET | /api/popups | 팝업 검색·목록 | 로그인 | q, region, categoryId, status, page, size. status는 UPCOMING/ONGOING/ENDED. 서버 날짜로 계산. |
| A04 | GET | /api/popups/{placeId} | 팝업 상세 | 로그인 | 대표 이미지 1개, 사진 목록, 주소, 좌표, 기간, 지도·홈페이지 링크. |
| A05 | GET | /api/popups/nearby | 내 주변 팝업 선택 | 로그인 | lat, lng, radiusKm. 반경 필터·거리순 정렬·distanceMeters. 검색과 인증 허용 거리는 별개. |
| A06 | POST | /api/popup-visits | 선택한 팝업 현장 인증 | 로그인 | placeId, lat, lng, accuracyMeters, measuredAt. 서버가 거리와 위치 최신성 검사. |
| A07 | GET | /api/me/popup-visit | 내 현재 인증 상태 | 본인 | placeId, status, verifiedAt, expiresAt. 선택한 팝업과 인증한 팝업을 구분. |
| A08 | PATCH | /api/me/popup-visit/location | 화면 이용 중 위치 갱신·이탈 확인 | 본인 | lat, lng, accuracyMeters, measuredAt. GPS 오차를 고려해 이탈 판정. |
| A09 | DELETE | /api/me/popup-visit | 현장 이용 종료 | 본인 | 명시적인 떠나기 처리. 반복 요청은 같은 결과. 해당 방문에 연결된 글의 공개 전환 시각 계산. |
| A10 | GET | /api/me/popups | 내가 인증한 팝업 목록 | 본인 | 내 팝업 교환 화면용. activeOnly 파라미터로 현재 인증/이력 구분. |
| A11 | GET | /api/me/interests | 내 관심사 조회 | 본인 | 카테고리 ID와 이름. |
| A12 | PUT | /api/me/interests | 관심사 설정·변경 | 본인 | categoryIds. 무료 최대 3개를 기본안으로 두고 설정값으로 관리. |
| A13 | GET | /api/exchange-posts | 전체 또는 현장 교환글 목록 | 로그인/현장 인증 | board=GENERAL 또는 ONSITE. placeId, categoryId, q, sort=latest, cursor, size. GENERAL에는 공개 시각이 된 글만 포함. |
| A14 | GET | /api/exchange-posts/recommended | 홈 관심사 추천 교환글 | 로그인 | GENERAL 공개 범위 안에서 관심사 매칭. 부족하면 일반 최신 글로 채움. 초기 제안. |
| A15 | GET | /api/exchange-posts/{postId} | 교환글 상세 | 로그인/현장 인증 | 현재 비공개 현장 글은 같은 팝업 유효 인증 필요. 작성자는 자신의 글 관리 가능. |
| A16 | POST | /api/exchange-posts | 교환글 작성 | 로그인/현장 인증 | itemName, wantedItemName, categoryId, photoUrls, description, sourceBoard, placeId 등. 현장 글의 visitId는 서버가 결정. |
| A17 | PATCH | /api/exchange-posts/{postId} | 내 교환글 수정 | 작성자 | 판매 상대·교환 완료 상태는 이 API로 변경 불가. 완료 상태를 유지하며 내용 수정 범위는 MVP에서 단순화. |
| A18 | DELETE | /api/exchange-posts/{postId} | 내 교환글 삭제 | 작성자 | 명시적 삭제만 삭제 표시. 교환 완료 버튼은 이 API를 호출하지 않음. 대화·교환 기록은 보존. |
| A19 | PUT | /api/exchange-posts/{postId}/like | 교환글 찜 | 로그인/현장 인증 | 같은 글 중복 찜 방지. 본문 접근 권한 검사 후 저장. |
| A20 | DELETE | /api/exchange-posts/{postId}/like | 찜 취소 | 본인 | 중복 취소는 같은 결과. |
| A21 | GET | /api/me/liked-posts | 내 찜 목록 | 본인 | 현재 열람 권한이 없는 현장 글의 본문은 노출하지 않음. |
| A22 | GET | /api/me/exchange-posts | 내가 쓴 글 목록 | 본인 | 완료 글 포함. 작성자 관리 화면용. |
| A23 | POST | /api/uploads/images | 상품·프로필·채팅 사진 업로드 | 로그인 | multipart 업로드 후 URL 반환. 공통 파일 크기·형식·업로드 소유자 검증을 A 담당으로 통일. |

## B 담당 API

| ID | 방식 | 경로 | 기능 | 권한 | 주요 규칙 |
|---|---|---|---|---|---|
| B01 | POST | /api/auth/signup | 이메일 회원가입 | 비로그인 허용 | email, password, nickname. |
| B02 | POST | /api/auth/login | 이메일 로그인 | 비로그인 허용 | 비밀번호 검증 후 로그인 수단 발급. |
| B03 | GET | /oauth2/authorization/google | Google 로그인 시작 | 비로그인 허용 | Spring Security OAuth 로그인 시작 경로로 설계. |
| B04 | GET | /login/oauth2/code/google | Google 로그인 콜백 | 인증 콜백 | 서버가 authorization code 검증·계정 조회. 프론트가 임의 호출하는 일반 API가 아님. |
| B05 | POST | /api/auth/refresh | 로그인 갱신 | 갱신 자격 증명 | 갱신 토큰 검사·회전. 상세 전달 방식은 인증 담당이 프론트와 합의. |
| B06 | POST | /api/auth/logout | 로그아웃 | 본인 | 갱신 토큰 무효화. |
| B07 | GET | /api/me | 내 기본 정보 | 본인 | userId, nickname, profileImageUrl, 관심사 설정 여부. |
| B08 | PATCH | /api/me | 닉네임·프로필 수정 | 본인 | 프로필 사진은 A23 업로드 결과 사용. |
| B09 | POST | /api/chat-rooms | 교환글에서 채팅 시작 | 로그인/현장 인증 | postId. 글쓴이와 신청자 2명. 같은 글·같은 신청자는 기존 방 반환. 자기 글에 신청 불가. |
| B10 | GET | /api/chat-rooms | 내 채팅 목록 | 참여자 | 최근 메시지·미읽음 수. tradeStatus=TRADING 필터로 본인이 참여한 확정 교환의 상대방 채팅방만 내 약속 탭에 표시. 일반 글의 미확정 방과 완료된 방은 제외. |
| B11 | GET | /api/chat-rooms/{roomId} | 채팅방 헤더·교환 상태 | 참여자 | 상대방, 연결된 글 요약, 확정·완료 버튼 상태. |
| B12 | GET | /api/chat-rooms/{roomId}/messages | 대화 기록·재접속 복구 | 참여자 | beforeId, size로 과거 메시지 조회. 연결 복구 후 이후 메시지도 조회 가능한 커서 규약 정의. |
| B13 | POST | /api/chat-rooms/{roomId}/messages | 문자·사진 메시지 전송 | 참여자 | TEXT는 사용자 문자, IMAGE는 사용자 사진, SYSTEM은 서버 자동 안내. 요청은 TEXT/IMAGE만 허용하고 senderId는 서버가 로그인 정보로 지정. clientMessageId로 재전송 중복 방지. |
| B14 | PUT | /api/chat-rooms/{roomId}/read | 읽은 위치 갱신 | 참여자 | lastReadMessageId. 본인 방의 메시지인지 검사. |
| B15 | POST | /api/chat-rooms/{roomId}/exchange | 상대방과 교환 확정 | 글쓴이 기본안 | 시간 선택 없음. 글의 tradeStatus를 ACTIVE에서 TRADING으로 변경하고 TRADING 교환 기록 생성·시스템 메시지 전송. 확정한 방만 내 약속에 표시. 같은 글의 중복 확정 방지. |
| B16 | POST | /api/exchanges/{exchangeId}/complete | 교환 완료 | 글쓴이 기본안 | 교환과 글을 TRADING에서 COMPLETED로 변경. 내 약속에서 제외하고 글·채팅방·대화 기록은 삭제하지 않음. |
| B17 | GET | /api/me/exchanges | 나의 교환 중·완료 내역 | 본인 | status=TRADING 또는 COMPLETED. 내 약속·홈 나의 약속은 TRADING만 조회하고 완료 내역은 COMPLETED로 조회. |
| B18 | WebSocket | /ws | 채팅 실시간 수신 연결 | 인증된 본인 | 메시지 쓰기는 B13 REST 하나로 통일. 서버가 저장 후 본인용 /user/queue/chat-events에 전달. 연결·수신 모두 참여 권한 검사. |

## 프론트 화면에서의 호출

| 화면 | 호출 |
|---|---|
| 로그인·가입 | B01~B06 |
| 관심사 설정 | A01, A11, A12 |
| 홈 | A14 추천, A13 board=GENERAL 최신 글, A03 팝업, B17 나의 확정 교환 |
| 팝업 목록·필터 | A02, A03 |
| 팝업 상세 | A04 |
| 현장 팝업 선택 | A05 주변 팝업 → A06 선택한 팝업 인증 |
| 현장 교환보드 | A07 인증 조회 → A13 board=ONSITE와 placeId |
| 전체 교환보드 | A13 board=GENERAL |
| 글 작성 | A01 카테고리 → A23 이미지 → A16 글 저장 |
| 글 상세·찜 | A15, A19, A20 |
| 찜 탭·내 글 | A21, A22 |
| 채팅 시작 | B09 |
| 채팅 목록·대화 | B10~B14, B18 |
| 교환 확정·완료 | B15 → B16. 시간 입력 없음 |

화면마다 전용 API를 하나씩 만드는 방식은 피한다. 홈은 기존 조회 API들을 조합한다. B17의 '확정 교환'은 시안의 '나의 약속' 카드에 사용하며 예약 시간을 저장한다는 뜻이 아니다.

## 공통 인터페이스 규칙

- placeId는 기존 JSON의 문자열을 그대로 사용한다. 내부 사용자·게시글·채팅방 ID와 혼용하지 않는다.
- 게시글과 채팅의 authorId, senderId, requesterId는 로그인 정보로 결정한다. 요청 본문으로 타인을 지정할 수 없다.
- 같은 placeId를 프론트가 보냈다는 것만으로 현장 인증이 되지 않는다. A의 현장 권한 검증을 호출한다.
- 목록·상세·찜·추천·신규 채팅 모두 같은 현장 비공개 규칙을 적용한다. 홈 GENERAL 조회에 아직 공개되지 않은 글을 섞지 않는다.
- 작성자는 본인의 글을 현장 이탈 후에도 관리할 수 있도록 한다. 다른 사람의 글에 대한 현장 인증 우회는 허용하지 않는다.
- 날짜는 YYYY-MM-DD, 시각은 시간대가 있는 ISO 8601로 주고받는다. 날짜 기준은 Asia/Seoul로 통일한다.
- nearby는 distanceMeters를 계산해 거리순으로 반환한다. distanceMeters는 원본 카탈로그에 저장하는 고정 필드가 아니다.
- 팝업 목록은 page/size, 계속 갱신되는 교환글·메시지는 커서 페이지를 기본안으로 삼는다.
- 오류는 status, code, message, fieldErrors 구조를 공통으로 맞춘다. 401은 로그인 필요, 403은 접근 불가, 404는 대상 없음, 409는 중복 확정 같은 충돌에 사용한다.
- 완료 처리와 확정은 한 트랜잭션에서 검증한다. 같은 글을 두 채팅방에서 동시에 확정할 수 없도록 잠금 또는 조건부 갱신과 유일 제약을 사용한다.

## 게시글 노출과 교환 상태

두 축을 분리한다.

| 축 | 값 |
|---|---|
| 최초 작성 보드 sourceBoard | GENERAL / ONSITE |
| 현재 공개 여부 | publicAt이 현재 이전인지로 계산. ONSITE 작성 당시에는 null |
| 교환 진행 상태 tradeStatus | ACTIVE 일반 / TRADING 교환 중 / COMPLETED 교환 완료 |
| 교환 기록 exchanges.status | TRADING 교환 중 / COMPLETED 교환 완료 |
| 표시 태그 | 방문 상태와 시각을 기준으로 계산. 게시글 완료 여부와 혼합하지 않음 |

현장 글은 해당 글을 작성할 때의 visitId에 연결한다. 이탈이 확인되면 그 방문에 연결된 글의 publicAt을 이탈 확인 시각 + 60분으로 설정하는 안이다. 조회할 때 publicAt을 비교하면 브라우저를 닫아도 공개 시각이 지난 글을 전체 목록에서 조회할 수 있다.

사용자가 설명한 태그 흐름은 현장 Live → 이탈 30분 뒤 '방금 전' → 추가 30분 뒤 일반 전환이다. 이탈 직후 첫 30분의 표시와 일반 글 New 유지 시간은 아직 정확한 수치가 확정되지 않았다. 별도 시간 설정값으로 둔다.

브라우저 위치 수신 중단은 실제 현장 이탈과 다르다. 이탈 확인 leftAt과 인증 만료 expiresAt을 분리한다. 인증이 만료되면 신규 현장 접근을 막되, 실제 이탈 시각을 임의로 기록하지 않는다. 만료 글을 언제 일반 전환할지는 아직 기획 보완이 필요한 정책이다.

## 확정되지 않은 부분의 초기 설계안

필수 질문을 더 늘리지 않기 위해 변경 가능한 기본안으로 분리한다.

- 글 하나에 교환 상대 한 명, 글쓴이가 확정·완료 처리한다. 게시글 예약 상태는 만들지 않는다.
- 일반 교환글은 연결 팝업 없이 작성할 수 있도록 DB의 placeId를 nullable로 둔다. 현장 글은 placeId와 visitId가 필수다.
- 한 번에 하나의 현재 현장 인증을 사용한다.
- 기존 대화방은 인증 만료 후에도 참여자가 이용하는 방향을 기본안으로 삼되, 신규 현장 글 열람과 신규 방 생성은 재인증이 필요하다.
- New 유지 시간, 인증 반경·유효시간, 위치 이탈 오차 처리, 관심사 최대 선택 수는 설정값으로 관리한다. 아직 임의 숫자를 확정 규칙으로 넣지 않는다.
- 글 작성 화면의 '교환 시간' 설명 문구와 확정 모달의 시간 선택은 별개다. 확정 모달의 예약 시각은 제거하며, 글의 자유 설명 필드는 필요하면 그대로 둘 수 있다.
- 카테고리별 추천 결과가 부족하면 GENERAL 최신 글로 보완한다.

## 기존 JSON 활용

원본은 C:/Users/Administrator/Documents/Codex/2026-10-30/d/outputs/gacha-mvp-popups.json 이다.

재확인 결과 241개이며 placeId 중복 0개, 좌표 누락 0개다. 대표 이미지와 시작·종료일 값이 모두 존재한다. 종료 행사도 포함한다.

전체 서비스에서는 사용자와 교환글 때문에 DB가 필요하므로, 팝업도 이 JSON을 초기 데이터로 DB에 한 번 반영하는 구조를 추천한다. 데이터 수집을 다시 하지 않고, 원본 JSON을 수정하지 않는다.

- places의 각 행 → popups
- photos 배열 → popup_photos
- officialImageUrl → popups.official_image_url 한 개
- placeUrl, homepageUrl도 팝업 상세에 필요한 링크로 보관
- 기존의 비정형 카테고리는 기획 카테고리와 매핑할 수 있는 항목부터 정리하고, 나머지를 임의로 애니메이션 등으로 추정하지 않는다.
- 이미지 파일은 JSON 안에 들어 있지 않다. 기존 팝업 사진은 외부 URL이며, 사용자 업로드 사진과 구분한다.
- 조회용 API는 수집 증빙·외부 검토 사유 같은 원본 메타데이터를 응답하지 않는다.
- 같은 placeId를 재반영해도 중복 행을 만들지 않도록 한다. 서비스 실행마다 테이블을 지우거나 전체를 재생성하지 않는다.

## 후순위 후보

아래는 첫 구현에서 제외하는 제안이며 확정 구현 범위가 아니다.

| 기능 | 후보 API | 담당 |
|---|---|---|
| 거래 후 별점 | POST /api/exchanges/{id}/review, GET /api/users/{id}/rating | B |
| 앱 안 알림 | GET /api/me/notifications, PATCH /api/me/notifications/{id}/read | B |
| 이메일 비밀번호 재설정 | POST /api/auth/password-reset-requests, POST /api/auth/password-resets | B |
| 교환 확정 취소 | POST /api/exchanges/{id}/cancel | B |
| 프리미엄·부스트·결제 | 기획 범위 확정 후 설계 | 후순위 |
| 별도 댓글 | 말풍선 숫자의 의미가 확정되지 않아 API·테이블을 만들지 않음 | 미정 |

## 첫 구현 순서

1. 문서에서 역할·ID·응답 규약을 맞춘다. 초기 세팅 PR과 별도의 작업 이슈·브랜치를 사용한다.
2. 공통 DB 연결과 스키마를 준비하고 팝업 241개를 반영한다.
3. A는 팝업 목록·상세·주변 조회, B는 이메일·Google 로그인부터 병렬로 작업한다.
4. 사용자 인증 연동 후 A는 현장 인증·게시글·찜·업로드, B는 채팅을 진행한다.
5. A와 B의 서비스 계약을 연결해 교환 확정·완료, 홈·찜·내 약속 흐름을 통합 검증한다.
6. 시간 전환은 테스트 시계를 이용해 30분·60분 경계를 검증한다. 실제로 한 시간을 기다리는 테스트를 하지 않는다.

## 합치는 경계

- B의 인증 결과로 얻는 userId를 A가 공통으로 사용한다.
- A의 현장 권한 판정과 게시글 접근 판정을 B의 신규 채팅 생성에서도 재사용한다.
- B의 교환 완료 서비스는 A의 게시글 완료 전용 메서드를 같은 DB 트랜잭션에서 호출한다. 서로 다른 API 서버로 나누지 않는다.
- A의 이미지 업로드를 B의 채팅·프로필에서 함께 사용한다.
- A는 B의 로그인 구현을 복제하지 않고, B는 현장 권한 판정과 게시글 공개 정책을 복제하지 않는다.


