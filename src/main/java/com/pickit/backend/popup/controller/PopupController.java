package com.pickit.backend.popup.controller;

import com.pickit.backend.popup.dto.request.PopupListFilter;
import com.pickit.backend.popup.dto.response.PopupFilterOptionsResponse;
import com.pickit.backend.popup.dto.response.PopupListResponse;
import com.pickit.backend.popup.entity.PopupRegion;
import com.pickit.backend.popup.entity.PopupStatus;
import com.pickit.backend.popup.service.PopupService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.security.Principal;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/popups")
public class PopupController {

    private final PopupService popupService;

    public PopupController(PopupService popupService) {
        this.popupService = popupService;
    }

    @GetMapping
    public PopupListResponse getPopups(
            Principal principal,
            @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(name = "statuses", required = false) Set<@NotNull PopupStatus> statuses,
            @RequestParam(name = "regions", required = false) Set<@NotNull PopupRegion> regions,
            @RequestParam(name = "categoryIds", required = false) Set<@NotNull @Positive Long> categoryIds
    ) {
        requireLogin(principal);
        return popupService.getPopups(page, size, new PopupListFilter(statuses, regions, categoryIds));
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
