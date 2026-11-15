package com.pickit.backend.popup.dev;

import com.pickit.backend.popup.dto.request.NearbyPopupRequest;
import com.pickit.backend.popup.dto.request.PopupListFilter;
import com.pickit.backend.popup.dto.response.NearbyPopupResponse;
import com.pickit.backend.popup.dto.response.PopupDetailResponse;
import com.pickit.backend.popup.dto.response.PopupFilterOptionsResponse;
import com.pickit.backend.popup.dto.response.PopupLikeResponse;
import com.pickit.backend.popup.dto.response.PopupListResponse;
import com.pickit.backend.popup.entity.PopupRegion;
import com.pickit.backend.popup.entity.PopupStatus;
import com.pickit.backend.popup.service.PopupLikeService;
import com.pickit.backend.popup.service.PopupService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Profile("popup-list-demo")
@RestController
@RequestMapping("/api/dev/popups")
public class PopupListDemoController {

    private final PopupService popupService;
    private final PopupLikeService popupLikeService;

    public PopupListDemoController(PopupService popupService, PopupLikeService popupLikeService) {
        this.popupService = popupService;
        this.popupLikeService = popupLikeService;
    }

    /**
     * X-Dev-User-Id가 있으면 그 회원의 실제 찜 기록을 사용한다.
     * 없으면 기존 테스트처럼 likedPopupIds를 찜했다고 가정한다.
     */
    @GetMapping
    public PopupListResponse getPopups(
            @RequestHeader(name = "X-Dev-User-Id", required = false) @Positive Long userId,
            @RequestParam(name = "q", required = false) @Size(max = 255) String q,
            @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(name = "likedPopupIds", required = false) Set<@NotNull @Positive Long> likedPopupIds,
            @RequestParam(name = "statuses", required = false) Set<@NotNull PopupStatus> statuses,
            @RequestParam(name = "regions", required = false) Set<@NotNull PopupRegion> regions,
            @RequestParam(name = "categoryIds", required = false) Set<@NotNull @Positive Long> categoryIds
    ) {
        if (userId != null && likedPopupIds != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "X-Dev-User-Id와 likedPopupIds는 함께 사용할 수 없습니다.");
        }
        Set<Long> likes = userId != null ? popupLikeService.findLikedPopupIds(userId) : likedPopupIds;
        return popupService.getPopups(
                page, size, new PopupListFilter(q, statuses, regions, categoryIds), likes
        );
    }

    @GetMapping("/nearby")
    public NearbyPopupResponse getNearbyPopups(@Valid @ModelAttribute NearbyPopupRequest request) {
        return popupService.getNearbyPopups(request);
    }

    @GetMapping("/{popupId}")
    public PopupDetailResponse getPopupDetail(@PathVariable("popupId") @Positive Long popupId) {
        return popupService.getPopupDetail(popupId);
    }

    @PutMapping("/{popupId}/like")
    public PopupLikeResponse likePopup(
            @RequestHeader("X-Dev-User-Id") @Positive Long userId,
            @PathVariable("popupId") @Positive Long popupId
    ) {
        return popupLikeService.like(userId, popupId);
    }

    @DeleteMapping("/{popupId}/like")
    public PopupLikeResponse unlikePopup(
            @RequestHeader("X-Dev-User-Id") @Positive Long userId,
            @PathVariable("popupId") @Positive Long popupId
    ) {
        return popupLikeService.unlike(userId, popupId);
    }

    @GetMapping("/filters")
    public PopupFilterOptionsResponse getFilterOptions() {
        return popupService.getFilterOptions();
    }
}
