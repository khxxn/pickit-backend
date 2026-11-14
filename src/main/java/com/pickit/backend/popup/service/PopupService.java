package com.pickit.backend.popup.service;

import com.pickit.backend.popup.dto.response.PopupListItemResponse;
import com.pickit.backend.popup.dto.response.PopupListResponse;
import com.pickit.backend.popup.entity.Popup;
import com.pickit.backend.popup.entity.PopupStatus;
import com.pickit.backend.popup.repository.PopupRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PopupService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final PopupRepository popupRepository;

    public PopupService(PopupRepository popupRepository) {
        this.popupRepository = popupRepository;
    }

    public PopupListResponse getPopups(int page, int size) {
        // 사용자별 팝업 찜 저장 기능이 연결되면 인증된 사용자의 찜 ID 목록을 전달한다.
        return getPopups(page, size, Set.of());
    }

    public PopupListResponse getPopups(int page, int size, Set<Long> likedPopupIds) {
        LocalDate today = LocalDate.now(SEOUL);
        Set<Long> likes = likedPopupIds == null ? Set.of() : Set.copyOf(likedPopupIds);
        PageRequest pageable = PageRequest.of(page, size);

        Page<Popup> popups = likes.isEmpty()
                ? popupRepository.findPopupList(today, pageable)
                : popupRepository.findPopupListWithLikes(today, likes, pageable);

        Page<PopupListItemResponse> items = popups.map(popup -> new PopupListItemResponse(
                popup.getPopupId(),
                popup.getName(),
                popup.getThumbnailUrl(),
                popup.getDistrict(),
                popup.getStartDate(),
                popup.getEndDate(),
                PopupStatus.fromDates(popup.getStartDate(), popup.getEndDate(), today),
                likes.contains(popup.getPopupId())
        ));

        return new PopupListResponse(
                items.getContent(),
                items.getNumber(),
                items.getSize(),
                items.getTotalElements(),
                items.getTotalPages(),
                items.hasNext()
        );
    }
}
