package com.pickit.backend.verification.auth;

import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class VerificationUserResolver {

    public Long requireUserId(Principal principal) {
        if (principal instanceof VerificationPrincipal user
                && user.getUserId() != null && user.getUserId() > 0) {
            return user.getUserId();
        }
        // Principal.getName()이 회원 ID인지 이메일인지 임의로 가정하지 않는다.
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 사용자 정보 연동이 필요합니다.");
    }
}
