package com.rotacerta.api.controller;

import com.rotacerta.api.dto.OrderSummaryResponse;
import com.rotacerta.api.dto.StatusUpdateRequest;
import com.rotacerta.api.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping("/orders")
    public List<OrderSummaryResponse> orders() {
        return service.findAll();
    }

    @GetMapping("/orders/{id}")
    public OrderSummaryResponse order(@PathVariable Long id) {
        return service.findById(id);
    }

    @PatchMapping("/deliveries/{id}/status")
    public OrderSummaryResponse updateStatus(@PathVariable Long id,
                                             @Valid @RequestBody StatusUpdateRequest request) {
        return service.updateStatus(id, request);
    }

    @PostMapping("/deliveries/{id}/confirm")
    public OrderSummaryResponse confirm(@PathVariable Long id) {
        return service.confirmDelivery(id);
    }

    @PostMapping("/deliveries/{id}/failure")
    public OrderSummaryResponse failure(@PathVariable Long id) {
        return service.failDelivery(id);
    }
}
