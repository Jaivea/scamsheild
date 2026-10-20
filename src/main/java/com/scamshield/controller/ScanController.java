package com.scamshield.controller;

import com.scamshield.model.RiskAssessment;
import com.scamshield.model.ScanRequest;
import com.scamshield.model.ScanResult;
import com.scamshield.service.RiskAnalysisService;
import com.scamshield.service.ScanHistoryService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ScanController {

    private final RiskAnalysisService riskService;
    private final ScanHistoryService historyService;

    public ScanController(
            RiskAnalysisService riskService,
            ScanHistoryService historyService) {
        this.riskService = riskService;
        this.historyService = historyService;
    }

    @PostMapping("/scan")
    public ScanResult scan(@RequestBody ScanRequest request) {
        if (request == null || request.getContent() == null
                || request.getContent().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please enter a message or URL.");
        }

        if (request.getContent().length() > 5000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Input must be 5000 characters or fewer.");
        }

        RiskAssessment assessment =
                riskService.analyze(request.getContent());

        return historyService.save(
                request.getContent().trim(), assessment);
    }

    @GetMapping("/history")
    public List<ScanResult> history() {
        return historyService.findAll();
    }

    @GetMapping("/analytics")
    public Map<String, Object> analytics() {
        return historyService.analytics();
    }
}
