package com.rotacerta.api.controller;

import com.rotacerta.api.dto.PortalDtos.DriverPortalResponse;
import com.rotacerta.api.security.PortalService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/driver-portal")
public class DriverPortalController {

    private final PortalService portalService;

    public DriverPortalController(PortalService portalService) {
        this.portalService = portalService;
    }

    @GetMapping("/me")
    public DriverPortalResponse me(Authentication authentication) {
        return portalService.driverProfile(authentication.getName());
    }
}
