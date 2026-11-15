package com.pickit.backend.verification.controller;

import com.pickit.backend.popup.dto.response.NearbyPopupResponse;
import com.pickit.backend.verification.auth.VerificationUserResolver;
import com.pickit.backend.verification.dto.request.LocationRequest;
import com.pickit.backend.verification.dto.response.VerificationResponse;
import com.pickit.backend.verification.service.PopupVerificationService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PopupVerificationController {

    private final PopupVerificationService verificationService;
    private final VerificationUserResolver userResolver;

    public PopupVerificationController(PopupVerificationService verificationService,
            VerificationUserResolver userResolver) {
        this.verificationService = verificationService;
        this.userResolver = userResolver;
    }

    @PostMapping("/api/popup-verifications/preview")
    public NearbyPopupResponse preview(Principal principal, @Valid @RequestBody LocationRequest request) {
        return verificationService.preview(userResolver.requireUserId(principal), request);
    }

    @PostMapping("/api/popup-verifications")
    public VerificationResponse verify(Principal principal, @Valid @RequestBody LocationRequest request) {
        return verificationService.verify(userResolver.requireUserId(principal), request);
    }

    @PatchMapping("/api/me/location")
    public VerificationResponse updateLocation(Principal principal, @Valid @RequestBody LocationRequest request) {
        return verificationService.updateLocation(userResolver.requireUserId(principal), request);
    }

    @GetMapping("/api/me/popup-verifications")
    public VerificationResponse getCurrent(Principal principal,
            @RequestParam(name = "includeInactive", defaultValue = "false") boolean includeInactive,
            HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        return verificationService.getCurrent(userResolver.requireUserId(principal), includeInactive);
    }
}
