package com.pickit.backend.verification.auth;

import java.security.Principal;

/** 로그인 담당자의 인증 객체 또는 어댑터가 제공하는 서버 검증 사용자 ID 계약. */
public interface VerificationPrincipal extends Principal {

    Long getUserId();
}
