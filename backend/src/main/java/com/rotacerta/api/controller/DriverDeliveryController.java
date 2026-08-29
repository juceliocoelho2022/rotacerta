package com.rotacerta.api.controller;

import com.rotacerta.api.dto.DriverDeliveryResponse;
import com.rotacerta.api.service.DriverDeliveryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/driver")
public class DriverDeliveryController {

    private final DriverDeliveryService driverDeliveryService;

    public DriverDeliveryController(DriverDeliveryService driverDeliveryService) {
        this.driverDeliveryService = driverDeliveryService;
    }

    @GetMapping("/deliveries/{orderCode}")
    public DriverDeliveryResponse delivery(@PathVariable String orderCode) {
        return driverDeliveryService.findByOrderCode(orderCode);
    }
}
