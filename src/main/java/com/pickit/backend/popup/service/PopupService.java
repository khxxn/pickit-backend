package com.pickit.backend.popup.service;

import com.pickit.backend.category.repository.CategoryRepository;
import com.pickit.backend.popup.dto.request.NearbyPopupRequest;
import com.pickit.backend.popup.dto.request.PopupListFilter;
import com.pickit.backend.popup.dto.response.NearbyPopupItemResponse;
import com.pickit.backend.popup.dto.response.NearbyPopupResponse;
import com.pickit.backend.popup.dto.response.PopupDetailResponse;
import com.pickit.backend.popup.dto.response.PopupFilterOptionsResponse;
import com.pickit.backend.popup.dto.response.PopupListItemResponse;
import com.pickit.backend.popup.dto.response.PopupListResponse;
import com.pickit.backend.popup.entity.Popup;
import com.pickit.backend.popup.entity.PopupRegion;
import com.pickit.backend.popup.entity.PopupStatus;
import com.pickit.backend.popup.repository.PopupRepository;
import com.pickit.backend.popup.repository.PopupSpecifications;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

    /** likedPopupIds는 회원이 찜한 팝업 ID 목록이며, 찜한 팝업을 먼저 정렬하고 isLiked를 표시한다. */
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

    public NearbyPopupResponse getNearbyPopups(NearbyPopupRequest request) {
        LocalDate today = LocalDate.now(SEOUL);
        // MySQL의 구면 거리 함수는 경도 범위 (-180, 180]을 사용한다.
        BigDecimal longitude = request.lng().compareTo(BigDecimal.valueOf(-180)) == 0
                ? BigDecimal.valueOf(180) : request.lng();

        List<NearbyPopupItemResponse> items = popupRepository.findNearbyPopups(
                request.lat(), longitude, request.radiusMeters()
        ).stream().map(popup -> new NearbyPopupItemResponse(
                popup.getPopupId(),
                popup.getName(),
                popup.getThumbnailUrl(),
                popup.getDistrict(),
                popup.getAddress(),
                popup.getLatitude(),
                popup.getLongitude(),
                popup.getStartDate(),
                popup.getEndDate(),
                PopupStatus.fromDates(popup.getStartDate(), popup.getEndDate(), today),
                popup.getDistanceMeters()
        )).toList();

        return new NearbyPopupResponse(
                request.lat(), request.lng(), request.radiusMeters(), items, items.size()
        );
    }

    public PopupDetailResponse getPopupDetail(Long popupId) {
        Popup popup = popupRepository.findById(popupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "팝업을 찾을 수 없습니다."));

        return new PopupDetailResponse(
                popup.getPopupId(),
                popup.getName(),
                popup.getThumbnailUrl(),
                popup.getCategory().getCategoryId(),
                popup.getCategory().getName(),
                popup.getDistrict(),
                popup.getAddress(),
                popup.getStartDate(),
                popup.getEndDate(),
                PopupStatus.fromDates(popup.getStartDate(), popup.getEndDate(), LocalDate.now(SEOUL)),
                popup.getDescription()
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
