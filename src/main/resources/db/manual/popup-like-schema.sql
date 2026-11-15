-- 앱 시작 시 자동 실행하지 않는 수동 DDL입니다.
-- users 테이블 준비 및 앱 실행(JPA의 popup_likes 생성) 후 실행합니다.
-- popup_likes는 최종 ERD에 없는 테이블입니다. post_likes와 같은 구조로 팝업 찜을 저장합니다.
-- 회원 엔티티 없이 user_id를 users에 DB 외래키로 연결합니다.
-- 기존 데이터에 없는 user_id가 있으면 FK 추가가 실패하므로 그 데이터를 먼저 확인하세요.
USE pickit;

SET @pickit_popup_like_user_fk_exists = (
    SELECT COUNT(*)
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'popup_likes'
      AND COLUMN_NAME = 'user_id'
      AND REFERENCED_TABLE_NAME = 'users'
      AND REFERENCED_COLUMN_NAME = 'user_id'
);

SET @pickit_popup_like_user_fk_sql = IF(
    @pickit_popup_like_user_fk_exists = 0,
    'ALTER TABLE popup_likes ADD CONSTRAINT fk_popup_likes_user FOREIGN KEY (user_id) REFERENCES users (user_id)',
    'SELECT ''users FK already exists'' AS message'
);

PREPARE pickit_popup_like_user_fk FROM @pickit_popup_like_user_fk_sql;
EXECUTE pickit_popup_like_user_fk;
DEALLOCATE PREPARE pickit_popup_like_user_fk;
