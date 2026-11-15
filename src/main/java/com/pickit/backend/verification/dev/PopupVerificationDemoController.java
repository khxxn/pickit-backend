package com.pickit.backend.verification.dev;

import com.pickit.backend.popup.dto.response.NearbyPopupResponse;
import com.pickit.backend.verification.dto.request.LocationRequest;
import com.pickit.backend.verification.dto.response.VerificationResponse;
import com.pickit.backend.verification.service.PopupVerificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Profile("popup-list-demo")
@RestController
public class PopupVerificationDemoController {

    private final PopupVerificationService verificationService;

    public PopupVerificationDemoController(PopupVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping("/api/dev/popup-verifications/preview")
    public NearbyPopupResponse preview(
            @RequestHeader("X-Dev-User-Id") @Positive Long userId,
            @Valid @RequestBody LocationRequest request) {
        return verificationService.preview(userId, request);
    }

    @PostMapping("/api/dev/popup-verifications")
    public VerificationResponse verify(
            @RequestHeader("X-Dev-User-Id") @Positive Long userId,
            @Valid @RequestBody LocationRequest request) {
        return verificationService.verify(userId, request);
    }

    @PatchMapping("/api/dev/me/location")
    public VerificationResponse updateLocation(
            @RequestHeader("X-Dev-User-Id") @Positive Long userId,
            @Valid @RequestBody LocationRequest request) {
        return verificationService.updateLocation(userId, request);
    }

    @GetMapping("/api/dev/me/popup-verifications")
    public VerificationResponse getCurrent(
            @RequestHeader("X-Dev-User-Id") @Positive Long userId,
            @RequestParam(name = "includeInactive", defaultValue = "false") boolean includeInactive,
            HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        return verificationService.getCurrent(userId, includeInactive);
    }
}
