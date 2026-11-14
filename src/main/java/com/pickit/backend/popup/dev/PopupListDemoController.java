package com.pickit.backend.popup.dev;

import com.pickit.backend.popup.dto.response.PopupListResponse;
import com.pickit.backend.popup.service.PopupService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
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
            @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(name = "likedPopupIds", required = false) Set<@Positive Long> likedPopupIds
    ) {
        return popupService.getPopups(page, size, likedPopupIds);
    }
}
