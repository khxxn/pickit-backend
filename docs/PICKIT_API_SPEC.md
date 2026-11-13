# PICKIT API 명세서

2026-11-13 수정 기준 설계 초안. HTTP/OAuth 경로 40개와 WebSocket 연결 1개이며, 구현 완료 목록이 아니다.

교환글은 일반(ACTIVE) → 교환 중(TRADING) → 교환 완료(COMPLETED) 순서다. 교환 확정 버튼을 누르면 글과 교환 기록이 TRADING이 되고 해당 상대방의 채팅방만 내 약속 탭에 나타난다. 완료 버튼을 누르면 두 상태가 COMPLETED가 되어 내 약속에서 빠지며, 글과 대화 기록은 남는다. 전체/현장이라는 공개 범위는 이 교환 상태와 별개다.

| 번호 | 메서드 | 경로 | 기능 | 권한 | 주요 입력·동작 |
|---|---|---|---|---|---|
| 01 | POST | /api/auth/signup | 이메일 회원가입 | 비로그인 허용 | email, password, nickname. |
| 02 | POST | /api/auth/login | 이메일 로그인 | 비로그인 허용 | 비밀번호 검증 후 로그인 수단 발급. |
| 03 | GET | /oauth2/authorization/google | Google 로그인 시작 | 비로그인 허용 | Spring Security OAuth 로그인 시작 경로로 설계. |
| 04 | GET | /login/oauth2/code/google | Google 로그인 콜백 | 인증 콜백 | 서버가 authorization code 검증·계정 조회. 프론트가 임의 호출하는 일반 API가 아님. |
| 05 | POST | /api/auth/refresh | 로그인 갱신 | 갱신 자격 증명 | 갱신 토큰 검사·회전. 상세 전달 방식은 프론트와 합의. |
| 06 | POST | /api/auth/logout | 로그아웃 | 본인 | 갱신 토큰 무효화. |
| 07 | GET | /api/me | 내 기본 정보 | 본인 | userId, nickname, profileImageUrl, 관심사 설정 여부. |
| 08 | PATCH | /api/me | 닉네임·프로필 수정 | 본인 | 프로필 사진은 POST /api/uploads/images의 업로드 결과 사용. |
| 09 | GET | /api/categories | 상품·관심사 카테고리 목록 | 로그인 | 기획 목록을 공통 코드로 관리. 관심사 저장 시 같은 ID 사용. |
| 10 | GET | /api/me/interests | 내 관심사 조회 | 본인 | 카테고리 ID와 이름. |
| 11 | PUT | /api/me/interests | 관심사 설정·변경 | 본인 | categoryIds. 무료 최대 3개를 기본안으로 두고 설정값으로 관리. |
| 12 | GET | /api/popups/filters | 팝업 필터 선택지 | 로그인 | 지원 지역·카테고리·진행 상태만 반환. 서울 외 미지원 항목은 미노출. |
| 13 | GET | /api/popups | 팝업 검색·목록 | 로그인 | q, region, categoryId, status, page, size. status는 UPCOMING/ONGOING/ENDED. 서버 날짜로 계산. |
| 14 | GET | /api/popups/{placeId} | 팝업 상세 | 로그인 | 대표 이미지 1개, 사진 목록, 주소, 좌표, 기간, 지도·홈페이지 링크. |
| 15 | GET | /api/popups/nearby | 내 주변 팝업 선택 | 로그인 | lat, lng, radiusKm. 반경 필터·거리순 정렬·distanceMeters. 검색과 인증 허용 거리는 별개. |
| 16 | POST | /api/popup-visits | 선택한 팝업 현장 인증 | 로그인 | placeId, lat, lng, accuracyMeters, measuredAt. 서버가 거리와 위치 최신성 검사. |
| 17 | GET | /api/me/popup-visit | 내 현재 인증 상태 | 본인 | placeId, status, verifiedAt, expiresAt. 선택한 팝업과 인증한 팝업을 구분. |
| 18 | PATCH | /api/me/popup-visit/location | 화면 이용 중 위치 갱신·이탈 확인 | 본인 | lat, lng, accuracyMeters, measuredAt. GPS 오차를 고려해 이탈 판정. |
| 19 | DELETE | /api/me/popup-visit | 현장 이용 종료 | 본인 | 명시적인 떠나기 처리. 반복 요청은 같은 결과. 해당 방문에 연결된 글의 공개 전환 시각 계산. |
| 20 | GET | /api/me/popups | 내가 인증한 팝업 목록 | 본인 | 내 팝업 교환 화면용. activeOnly 파라미터로 현재 인증/이력 구분. |
| 21 | GET | /api/exchange-posts | 전체 또는 현장 교환글 목록 | 로그인/현장 인증 | board=GENERAL 또는 ONSITE. placeId, categoryId, q, sort=latest, cursor, size. GENERAL에는 공개 시각이 된 글만 포함. |
| 22 | GET | /api/exchange-posts/recommended | 홈 관심사 추천 교환글 | 로그인 | GENERAL 공개 범위 안에서 관심사 매칭. 부족하면 일반 최신 글로 채움. 초기 제안. |
| 23 | GET | /api/exchange-posts/{postId} | 교환글 상세 | 로그인/현장 인증 | 현재 비공개 현장 글은 같은 팝업 유효 인증 필요. 작성자는 자신의 글 관리 가능. |
| 24 | POST | /api/exchange-posts | 교환글 작성 | 로그인/현장 인증 | itemName, wantedItemName, categoryId, photoUrls, description, sourceBoard, placeId 등. 현장 글의 visitId는 서버가 결정. |
| 25 | PATCH | /api/exchange-posts/{postId} | 내 교환글 수정 | 작성자 | 교환 상대·교환 완료 상태는 이 API로 변경 불가. 완료 상태를 유지하며 내용 수정 범위는 MVP에서 단순화. |
| 26 | DELETE | /api/exchange-posts/{postId} | 내 교환글 삭제 | 작성자 | 명시적 삭제만 삭제 표시. 교환 완료 버튼은 이 API를 호출하지 않음. 대화·교환 기록은 보존. |
| 27 | PUT | /api/exchange-posts/{postId}/like | 교환글 찜 | 로그인/현장 인증 | 같은 글 중복 찜 방지. 본문 접근 권한 검사 후 저장. |
| 28 | DELETE | /api/exchange-posts/{postId}/like | 찜 취소 | 본인 | 중복 취소는 같은 결과. |
| 29 | GET | /api/me/liked-posts | 내 찜 목록 | 본인 | 현재 열람 권한이 없는 현장 글의 본문은 노출하지 않음. |
| 30 | GET | /api/me/exchange-posts | 내가 쓴 글 목록 | 본인 | 완료 글 포함. 작성자 관리 화면용. |
| 31 | POST | /api/uploads/images | 상품·프로필·채팅 사진 업로드 | 로그인 | multipart 업로드 후 URL 반환. 파일 크기·형식·업로드 소유자를 공통으로 검증. |
| 32 | POST | /api/chat-rooms | 교환글에서 채팅 시작 | 로그인/현장 인증 | postId. 글쓴이와 신청자 2명. 같은 글·같은 신청자는 기존 방 반환. 자기 글에 신청 불가. |
| 33 | GET | /api/chat-rooms | 내 채팅 목록 | 참여자 | 최근 메시지·미읽음 수. tradeStatus=TRADING 필터로 본인이 참여한 확정 교환의 상대방 채팅방만 내 약속 탭에 표시. 일반 글의 미확정 방과 완료된 방은 제외. |
| 34 | GET | /api/chat-rooms/{roomId} | 채팅방 헤더·교환 상태 | 참여자 | 상대방, 연결된 글 요약, 확정·완료 버튼 상태. |
| 35 | GET | /api/chat-rooms/{roomId}/messages | 대화 기록·재접속 복구 | 참여자 | beforeId, size로 과거 메시지 조회. 연결 복구 후 이후 메시지도 조회 가능한 커서 규약 정의. |
| 36 | POST | /api/chat-rooms/{roomId}/messages | 문자·사진 메시지 전송 | 참여자 | TEXT는 사용자 문자, IMAGE는 사용자 사진, SYSTEM은 서버 자동 안내. 요청은 TEXT/IMAGE만 허용하고 senderId는 서버가 로그인 정보로 지정. clientMessageId로 재전송 중복 방지. |
| 37 | PUT | /api/chat-rooms/{roomId}/read | 읽은 위치 갱신 | 참여자 | lastReadMessageId. 본인 방의 메시지인지 검사. |
| 38 | POST | /api/chat-rooms/{roomId}/exchange | 상대방과 교환 확정 | 글쓴이 기본안 | 시간 선택 없음. 글의 tradeStatus를 ACTIVE에서 TRADING으로 변경하고 TRADING 교환 기록 생성·시스템 메시지 전송. 확정한 방만 내 약속에 표시. 같은 글의 중복 확정 방지. |
| 39 | POST | /api/exchanges/{exchangeId}/complete | 교환 완료 | 글쓴이 기본안 | 교환과 글을 TRADING에서 COMPLETED로 변경. 내 약속에서 제외하고 글·채팅방·대화 기록은 삭제하지 않음. |
| 40 | GET | /api/me/exchanges | 나의 교환 중·완료 내역 | 본인 | status=TRADING 또는 COMPLETED. 내 약속·홈 나의 약속은 TRADING만 조회하고 완료 내역은 COMPLETED로 조회. |
| 41 | WebSocket | /ws | 채팅 실시간 수신 연결 | 인증된 본인 | 메시지 쓰기는 POST /api/chat-rooms/{roomId}/messages REST 하나로 통일. 서버가 저장 후 본인용 /user/queue/chat-events에 전달. 연결·수신 모두 참여 권한 검사. |


