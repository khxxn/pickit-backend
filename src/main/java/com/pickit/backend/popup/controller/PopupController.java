package com.pickit.backend.popup.controller;

import com.pickit.backend.popup.dto.response.PopupListResponse;
import com.pickit.backend.popup.service.PopupService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.security.Principal;
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
            @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return popupService.getPopups(page, size);
    }
}
