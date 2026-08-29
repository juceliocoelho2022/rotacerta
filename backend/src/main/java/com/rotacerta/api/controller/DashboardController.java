package com.rotacerta.api.controller;

import com.rotacerta.api.dto.DashboardResponse;
import com.rotacerta.api.service.OrderService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final OrderService service;

    public DashboardController(OrderService service) {
        this.service = service;
    }

    @GetMapping
    public DashboardResponse dashboard() {
        return service.dashboard();
    }
}
