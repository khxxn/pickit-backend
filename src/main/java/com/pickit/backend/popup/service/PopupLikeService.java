package com.pickit.backend.popup.service;

import com.pickit.backend.popup.dto.response.PopupLikeResponse;
import com.pickit.backend.popup.entity.Popup;
import com.pickit.backend.popup.entity.PopupLike;
import com.pickit.backend.popup.repository.PopupLikeRepository;
import com.pickit.backend.popup.repository.PopupRepository;
import com.pickit.backend.verification.repository.VerificationUserStore;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class PopupLikeService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final PopupLikeRepository popupLikeRepository;
    private final PopupRepository popupRepository;
    private final VerificationUserStore userStore;

    public PopupLikeService(PopupLikeRepository popupLikeRepository, PopupRepository popupRepository,
            VerificationUserStore userStore) {
        this.popupLikeRepository = popupLikeRepository;
        this.popupRepository = popupRepository;
        this.userStore = userStore;
    }

    /** 같은 팝업을 다시 찜해도 찜 기록은 하나만 유지한다. */
    @Transactional
    public PopupLikeResponse like(Long userId, Long popupId) {
        // 회원 행을 잠가 같은 회원의 동시 요청을 순서대로 처리한다.
        userStore.requireUser(userId, true);
        Popup popup = findPopup(popupId);
        if (!popupLikeRepository.existsByUserIdAndPopup_PopupId(userId, popupId)) {
            popupLikeRepository.saveAndFlush(new PopupLike(userId, popup,
                    LocalDateTime.now(SEOUL).truncatedTo(ChronoUnit.SECONDS)));
        }
        return new PopupLikeResponse(popupId, true, popupLikeRepository.countByPopup_PopupId(popupId));
    }

    /** 찜하지 않은 팝업의 취소 요청도 같은 결과를 반환한다. */
    @Transactional
    public PopupLikeResponse unlike(Long userId, Long popupId) {
        userStore.requireUser(userId, true);
        findPopup(popupId);
        popupLikeRepository.deleteByUserIdAndPopupId(userId, popupId);
        return new PopupLikeResponse(popupId, false, popupLikeRepository.countByPopup_PopupId(popupId));
    }

    public Set<Long> findLikedPopupIds(Long userId) {
        return Set.copyOf(popupLikeRepository.findPopupIdsByUserId(userId));
    }

    private Popup findPopup(Long popupId) {
        return popupRepository.findById(popupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "팝업을 찾을 수 없습니다."));
    }
}
