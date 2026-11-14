# PICKIT Backend

PICKIT 팝업 서비스 백엔드 저장소입니다.

- Java 21
- Spring Boot
- MySQL 8.0

## 로컬 실행

MySQL에 `pickit` 데이터베이스와 해당 DB의 사용 권한이 있는 `pickit_app` 계정을 준비합니다.

IntelliJ 실행 설정에 DB 접속 정보를 환경변수로 저장합니다.

1. **Run → Edit Configurations…**를 엽니다.
2. 실행할 **PickitBackendApplication** 설정을 선택합니다.
3. **Environment variables** 편집 창을 엽니다. 항목이 보이지 않으면 **Modify options → Environment variables**를 켭니다.
4. 이름을 `DB_PASSWORD`로 추가하고, 값에는 본인이 설정한 `pickit_app`의 실제 비밀번호를 입력합니다.
5. **Store as project file**은 체크하지 않고 **Apply → OK**로 로컬 설정에 저장합니다.


기본 주소와 계정을 사용한다면 `DB_PASSWORD`만 추가하면 됩니다. 비밀번호는 소스 파일에 적지 않습니다.

현재 로컬 개발 설정은 `ddl-auto: update`이며, JPA 엔티티를 작성하면 앱 시작 시 해당 테이블을 생성하거나 갱신합니다.
