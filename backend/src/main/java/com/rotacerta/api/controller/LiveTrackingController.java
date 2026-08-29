package com.rotacerta.api.controller;

import com.rotacerta.api.dto.AlternateRecipientRequest;
import com.rotacerta.api.dto.LiveLinkResponse;
import com.rotacerta.api.dto.LiveTrackingResponse;
import com.rotacerta.api.service.LiveTrackingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class LiveTrackingController {

    private final LiveTrackingService liveTrackingService;

    public LiveTrackingController(LiveTrackingService liveTrackingService) {
        this.liveTrackingService = liveTrackingService;
    }

    @PostMapping("/deliveries/{id}/live-link")
    public LiveLinkResponse createLiveLink(@PathVariable Long id) {
        return liveTrackingService.createOrGetLink(id);
    }

    @GetMapping("/public/live/{token}")
    public LiveTrackingResponse publicTracking(@PathVariable String token) {
        return liveTrackingService.getPublicTracking(token);
    }

    @PostMapping("/public/live/{token}/recipient")
    public LiveTrackingResponse authorizeRecipient(
            @PathVariable String token,
            @Valid @RequestBody AlternateRecipientRequest request
    ) {
        return liveTrackingService.authorizeAlternateRecipient(token, request);
    }
}
