package com.pickit.backend.popup.dev;

import com.pickit.backend.popup.dto.request.PopupListFilter;
import com.pickit.backend.popup.dto.response.PopupDetailResponse;
import com.pickit.backend.popup.dto.response.PopupFilterOptionsResponse;
import com.pickit.backend.popup.dto.response.PopupListResponse;
import com.pickit.backend.popup.entity.PopupRegion;
import com.pickit.backend.popup.entity.PopupStatus;
import com.pickit.backend.popup.service.PopupService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Profile("popup-list-demo")
@RestController
@RequestMapping("/api/dev/popups")
public class PopupListDemoController {

    private final PopupService popupService;

    public PopupListDemoController(PopupService popupService) {
        this.popupService = popupService;
    }

    @GetMapping
    public PopupListResponse getPopups(
            @RequestParam(name = "q", required = false) @Size(max = 255) String q,
            @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(name = "likedPopupIds", required = false) Set<@NotNull @Positive Long> likedPopupIds,
            @RequestParam(name = "statuses", required = false) Set<@NotNull PopupStatus> statuses,
            @RequestParam(name = "regions", required = false) Set<@NotNull PopupRegion> regions,
            @RequestParam(name = "categoryIds", required = false) Set<@NotNull @Positive Long> categoryIds
    ) {
        return popupService.getPopups(
                page, size, new PopupListFilter(q, statuses, regions, categoryIds), likedPopupIds
        );
    }

    @GetMapping("/{popupId}")
    public PopupDetailResponse getPopupDetail(@PathVariable("popupId") @Positive Long popupId) {
        return popupService.getPopupDetail(popupId);
    }

    @GetMapping("/filters")
    public PopupFilterOptionsResponse getFilterOptions() {
        return popupService.getFilterOptions();
    }
}
