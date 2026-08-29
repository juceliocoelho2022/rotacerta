package com.rotacerta.api.controller;

import com.rotacerta.api.dto.TrackingResponse;
import com.rotacerta.api.service.OrderService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tracking")
public class TrackingController {
    private final OrderService service;

    public TrackingController(OrderService service) {
        this.service = service;
    }

    @GetMapping("/{trackingCode}")
    public TrackingResponse track(@PathVariable String trackingCode) {
        return service.track(trackingCode);
    }
}
