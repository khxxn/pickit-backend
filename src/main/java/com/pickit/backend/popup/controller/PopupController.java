package com.pickit.backend.popup.controller;

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
import com.pickit.backend.verification.auth.VerificationUserResolver;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.security.Principal;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/popups")
public class PopupController {

    private final PopupService popupService;
    private final PopupLikeService popupLikeService;
    private final VerificationUserResolver userResolver;

    public PopupController(PopupService popupService, PopupLikeService popupLikeService,
            VerificationUserResolver userResolver) {
        this.popupService = popupService;
        this.popupLikeService = popupLikeService;
        this.userResolver = userResolver;
    }

    @GetMapping
    public PopupListResponse getPopups(
            Principal principal,
            @RequestParam(name = "q", required = false) @Size(max = 255) String q,
            @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(name = "statuses", required = false) Set<@NotNull PopupStatus> statuses,
            @RequestParam(name = "regions", required = false) Set<@NotNull PopupRegion> regions,
            @RequestParam(name = "categoryIds", required = false) Set<@NotNull @Positive Long> categoryIds
    ) {
        // 찜 여부와 찜 우선 정렬에는 서버가 확인한 회원 ID가 필요하다.
        Long userId = userResolver.requireUserId(principal);
        return popupService.getPopups(page, size, new PopupListFilter(q, statuses, regions, categoryIds),
                popupLikeService.findLikedPopupIds(userId));
    }

    @GetMapping("/nearby")
    public NearbyPopupResponse getNearbyPopups(
            Principal principal,
            @Valid @ModelAttribute NearbyPopupRequest request
    ) {
        requireLogin(principal);
        return popupService.getNearbyPopups(request);
    }

    @GetMapping("/{popupId}")
    public PopupDetailResponse getPopupDetail(
            Principal principal,
            @PathVariable("popupId") @Positive Long popupId
    ) {
        requireLogin(principal);
        return popupService.getPopupDetail(popupId);
    }

    @PutMapping("/{popupId}/like")
    public PopupLikeResponse likePopup(
            Principal principal,
            @PathVariable("popupId") @Positive Long popupId
    ) {
        return popupLikeService.like(userResolver.requireUserId(principal), popupId);
    }

    @DeleteMapping("/{popupId}/like")
    public PopupLikeResponse unlikePopup(
            Principal principal,
            @PathVariable("popupId") @Positive Long popupId
    ) {
        return popupLikeService.unlike(userResolver.requireUserId(principal), popupId);
    }

    @GetMapping("/filters")
    public PopupFilterOptionsResponse getFilterOptions(Principal principal) {
        requireLogin(principal);
        return popupService.getFilterOptions();
    }

    private void requireLogin(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
    }
}
