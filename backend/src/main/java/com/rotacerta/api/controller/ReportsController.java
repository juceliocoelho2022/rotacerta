package com.rotacerta.api.controller;

import com.rotacerta.api.dto.OperationsReportResponse;
import com.rotacerta.api.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportsController {
    private final ReportService service;

    public ReportsController(ReportService service) {
        this.service = service;
    }

    @GetMapping("/operations")
    public OperationsReportResponse operations() {
        return service.operations();
    }
}
