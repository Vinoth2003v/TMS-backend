package com.taskmanager.controller;

import com.taskmanager.dto.AnalyticsResponse;
import com.taskmanager.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // Constructor
    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping
    public ResponseEntity<AnalyticsResponse> getGlobalAnalytics() {
        return ResponseEntity.ok(analyticsService.getGlobalAnalytics());
    }

    @GetMapping("/manager/{email}")
    public ResponseEntity<AnalyticsResponse> getManagerAnalytics(
            @PathVariable String email) {

        return ResponseEntity.ok(
            analyticsService.getManagerAnalytics(email)
        );
    }
}