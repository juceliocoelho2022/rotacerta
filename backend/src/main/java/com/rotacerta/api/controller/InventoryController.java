package com.rotacerta.api.controller;

import com.rotacerta.api.dto.InventoryDtos.InventoryItemResponse;
import com.rotacerta.api.dto.InventoryDtos.InventoryReservationRequest;
import com.rotacerta.api.dto.InventoryDtos.MovementResponse;
import com.rotacerta.api.dto.InventoryDtos.ProductCreateRequest;
import com.rotacerta.api.dto.InventoryDtos.ReservationResponse;
import com.rotacerta.api.dto.InventoryDtos.StockEntryRequest;
import com.rotacerta.api.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public List<InventoryItemResponse> findAll() {
        return inventoryService.findAll();
    }

    @GetMapping("/alerts")
    public List<InventoryItemResponse> findLowStock() {
        return inventoryService.findLowStock();
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryItemResponse createProduct(@Valid @RequestBody ProductCreateRequest request) {
        return inventoryService.createProduct(request);
    }

    @PostMapping("/{sku}/entries")
    public InventoryItemResponse addStock(@PathVariable String sku, @Valid @RequestBody StockEntryRequest request) {
        return inventoryService.addStock(sku, request);
    }

    @PostMapping("/orders/{orderId}/reserve")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse reserve(@PathVariable Long orderId,
                                       @Valid @RequestBody InventoryReservationRequest request) {
        return inventoryService.reserve(orderId, request);
    }

    @PostMapping("/orders/{orderId}/{sku}/release")
    public ReservationResponse release(@PathVariable Long orderId, @PathVariable String sku) {
        return inventoryService.release(orderId, sku);
    }

    @PostMapping("/orders/{orderId}/{sku}/pick")
    public ReservationResponse pick(@PathVariable Long orderId, @PathVariable String sku) {
        return inventoryService.pick(orderId, sku);
    }

    @GetMapping("/orders/{orderId}/reservations")
    public List<ReservationResponse> findReservations(@PathVariable Long orderId) {
        return inventoryService.findReservations(orderId);
    }

    @GetMapping("/movements")
    public List<MovementResponse> findMovements() {
        return inventoryService.findMovements();
    }
}
