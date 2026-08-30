package com.rotacerta.api.controller;

import com.rotacerta.api.dto.OrderSummaryResponse;
import com.rotacerta.api.security.PortalService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customer-portal")
public class CustomerPortalController {

    private final PortalService portalService;

    public CustomerPortalController(PortalService portalService) {
        this.portalService = portalService;
    }

    @GetMapping("/orders")
    public List<OrderSummaryResponse> myOrders(Authentication authentication) {
        return portalService.customerOrders(authentication.getName());
    }
}
