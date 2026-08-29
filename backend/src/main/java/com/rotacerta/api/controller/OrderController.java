package com.rotacerta.api.controller;

import com.rotacerta.api.dto.OrderCreateRequest;
import com.rotacerta.api.dto.OrderDetailResponse;
import com.rotacerta.api.dto.OrderSummaryResponse;
import com.rotacerta.api.dto.StatusUpdateRequest;
import com.rotacerta.api.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDetailResponse createOrder(@Valid @RequestBody OrderCreateRequest request) {
        return service.create(request);
    }

    @GetMapping("/orders/{id}")
    public OrderSummaryResponse order(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/orders/{id}/detail")
    public OrderDetailResponse orderDetail(@PathVariable Long id) {
        return service.findDetailById(id);
    }

    @PatchMapping("/deliveries/{id}/status")
    public OrderSummaryResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request
    ) {
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
