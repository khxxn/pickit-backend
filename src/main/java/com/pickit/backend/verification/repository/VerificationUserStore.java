package com.pickit.backend.verification.repository;

import java.util.List;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

@Repository
public class VerificationUserStore {

    private final JdbcTemplate jdbcTemplate;

    public VerificationUserStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void requireUser(Long userId, boolean lock) {
        List<Long> users;
        try {
            users = jdbcTemplate.queryForList(
                    "SELECT user_id FROM users WHERE user_id = ?" + (lock ? " FOR UPDATE" : ""),
                    Long.class, userId
            );
        } catch (DataAccessException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "회원 DB 연결을 확인해주세요. 개발 테스트는 회원 준비 SQL을 먼저 실행해주세요.", exception);
        }
        if (users.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다.");
        }
    }
}
