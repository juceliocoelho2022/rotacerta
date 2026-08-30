package com.rotacerta.api.controller;

import com.rotacerta.api.dto.SettingsDtos;
import com.rotacerta.api.service.SettingsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {
    private final SettingsService service;

    public SettingsController(SettingsService service) {
        this.service = service;
    }

    @GetMapping
    public List<SettingsDtos.Response> findAll() {
        return service.findAll();
    }

    @PutMapping("/{key}")
    public SettingsDtos.Response update(@PathVariable String key,
                                        @Valid @RequestBody SettingsDtos.UpdateRequest request) {
        return service.update(key, request);
    }
}
