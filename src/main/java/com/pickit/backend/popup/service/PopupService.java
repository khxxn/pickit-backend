package com.pickit.backend.popup.service;

import com.pickit.backend.category.repository.CategoryRepository;
import com.pickit.backend.popup.dto.request.PopupListFilter;
import com.pickit.backend.popup.dto.response.PopupFilterOptionsResponse;
import com.pickit.backend.popup.dto.response.PopupListItemResponse;
import com.pickit.backend.popup.dto.response.PopupListResponse;
import com.pickit.backend.popup.entity.Popup;
import com.pickit.backend.popup.entity.PopupRegion;
import com.pickit.backend.popup.entity.PopupStatus;
import com.pickit.backend.popup.repository.PopupRepository;
import com.pickit.backend.popup.repository.PopupSpecifications;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PopupService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final PopupRepository popupRepository;
    private final CategoryRepository categoryRepository;

    public PopupService(PopupRepository popupRepository, CategoryRepository categoryRepository) {
        this.popupRepository = popupRepository;
        this.categoryRepository = categoryRepository;
    }

    public PopupListResponse getPopups(int page, int size, PopupListFilter filter) {
        // 사용자별 팝업 찜 저장 기능이 연결되면 인증된 사용자의 찜 ID 목록을 전달한다.
        return getPopups(page, size, filter, Set.of());
    }

    public PopupListResponse getPopups(
            int page, int size, PopupListFilter filter, Set<Long> likedPopupIds
    ) {
        LocalDate today = LocalDate.now(SEOUL);
        Set<Long> likes = likedPopupIds == null ? Set.of() : Set.copyOf(likedPopupIds);
        PageRequest pageable = PageRequest.of(page, size);

        Page<Popup> popups = popupRepository.findAll(
                PopupSpecifications.filteredAndOrdered(filter, today, likes), pageable
        );

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

    public PopupFilterOptionsResponse getFilterOptions() {
        return new PopupFilterOptionsResponse(
                Arrays.stream(PopupStatus.values())
                        .map(status -> new PopupFilterOptionsResponse.StatusOption(status, status.getLabel()))
                        .toList(),
                Arrays.stream(PopupRegion.values())
                        .map(region -> new PopupFilterOptionsResponse.RegionOption(region, region.getLabel()))
                        .toList(),
                categoryRepository.findAll(Sort.by("name", "categoryId")).stream()
                        .map(category -> new PopupFilterOptionsResponse.CategoryOption(
                                category.getCategoryId(), category.getName(), categoryLabel(category.getName())
                        ))
                        .toList()
        );
    }

    private String categoryLabel(String name) {
        return switch (name) {
            case "K-POP", "K_POP" -> "K_POP";
            case "키즈" -> "키즈/반려동물";
            default -> name;
        };
    }
}
