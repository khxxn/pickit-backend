-- 앱 시작 시 자동 실행하지 않는 수동 DDL입니다.
-- users 테이블 준비 및 앱 실행(JPA의 popup_verifications 생성/갱신) 후 실행합니다.
-- 원본 ERD의 회원 관계를 회원 엔티티 없이 DB 외래키로 보완합니다.
-- 기존 데이터에 없는 user_id가 있으면 FK 추가가 실패하므로 그 데이터를 먼저 확인하세요.
USE pickit;

-- 원본 export에는 AUTO_INCREMENT가 없습니다. 기존 팝업 엔티티와 같은 IDENTITY 방식으로 맞춥니다.
-- 기존 ID 값은 유지합니다. 측정 시각은 같은 초의 요청 순서를 구분할 수 있게 저장합니다.
ALTER TABLE popup_verifications
    MODIFY verification_id BIGINT NOT NULL AUTO_INCREMENT,
    MODIFY last_location_at DATETIME(6) NULL;

SET @pickit_verification_user_fk_exists = (
    SELECT COUNT(*)
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'popup_verifications'
      AND COLUMN_NAME = 'user_id'
      AND REFERENCED_TABLE_NAME = 'users'
      AND REFERENCED_COLUMN_NAME = 'user_id'
);

SET @pickit_verification_user_fk_sql = IF(
    @pickit_verification_user_fk_exists = 0,
    'ALTER TABLE popup_verifications ADD CONSTRAINT fk_popup_verifications_user FOREIGN KEY (user_id) REFERENCES users (user_id)',
    'SELECT ''users FK already exists'' AS message'
);

PREPARE pickit_verification_user_fk FROM @pickit_verification_user_fk_sql;
EXECUTE pickit_verification_user_fk;
DEALLOCATE PREPARE pickit_verification_user_fk;
