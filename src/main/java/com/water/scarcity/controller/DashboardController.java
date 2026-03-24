package com.water.scarcity.controller;

import com.water.scarcity.service.CommunityService;
import com.water.scarcity.service.PredictionService;
import com.water.scarcity.service.WaterDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * DashboardController - Main dashboard and system health endpoints
 * 
 * Base URL: /api/dashboard or /api
 * 
 * Endpoints:
 * - GET /api/health         - System health check
 * - GET /api/dashboard      - Main dashboard metrics
 * - GET /api/overview       - System overview
 * 
 * @author Senior Java Developer
 * @version 1.0
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final CommunityService communityService;
    private final WaterDataService waterDataService;
    private final PredictionService predictionService;

    /**
     * System health check endpoint
     * @return Health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealthStatus() {
        log.info("GET /health - Health check");
        
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "Water Scarcity Prediction Optimizer");
        health.put("version", "1.0.0");
        
        return ResponseEntity.ok(health);
    }

    /**
     * Main dashboard metrics
     * @return Dashboard data
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        log.info("GET /dashboard - Getting main dashboard metrics");
        
        Map<String, Object> dashboard = new HashMap<>();
        
        // System metrics
        dashboard.put("timestamp", LocalDateTime.now());
        dashboard.put("systemStatus", "OPERATIONAL");
        
        // Community metrics
        Long totalCommunities = communityService.getTotalActiveCommunityCount();
        dashboard.put("totalCommunities", totalCommunities);
        
        // Water metrics
        var communitiesWithDeficit = waterDataService.getCommunitiesWithDeficit();
        dashboard.put("communitiesWithDeficit", communitiesWithDeficit.size());
        dashboard.put("percentageInDeficit", totalCommunities > 0 ? 
            (communitiesWithDeficit.size() * 100.0 / totalCommunities) : 0);
        
        // Prediction metrics
        var criticalPredictions = predictionService.getCriticalPredictions();
        dashboard.put("criticalPredictions", criticalPredictions.size());
        dashboard.put("averagePredictionConfidence", predictionService.getAverageConfidence());
        
        // Summary
        Map<String, Object> summary = new HashMap<>();
        summary.put("overallStatus", criticalPredictions.size() > totalCommunities / 2 ? "CRITICAL" : "NORMAL");
        summary.put("actionRequired", criticalPredictions.size() > 0);
        summary.put("message", generateDashboardMessage(
            communitiesWithDeficit.size(), 
            criticalPredictions.size(), 
            totalCommunities
        ));
        
        dashboard.put("summary", summary);
        
        return ResponseEntity.ok(dashboard);
    }

    /**
     * System overview
     * @return System overview information
     */
    @GetMapping("/api/overview")
    public ResponseEntity<Map<String, Object>> getOverview() {
        log.info("GET /api/overview - Getting system overview");
        
        Map<String, Object> overview = new HashMap<>();
        
        overview.put("applicationName", "AI-Based Community Water Scarcity Prediction & Distribution Optimizer");
        overview.put("version", "1.0.0");
        overview.put("environment", "Production");
        overview.put("timestamp", LocalDateTime.now());
        
        // API endpoints
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("communities", "GET /api/communities");
        endpoints.put("water-data", "POST /api/water-data");
        endpoints.put("predictions", "POST /api/predictions/{communityId}");
        endpoints.put("dashboard", "GET /api/dashboard");
        endpoints.put("health", "GET /health");
        
        overview.put("availableEndpoints", endpoints);
        
        // System info
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("javaVersion", System.getProperty("java.version"));
        systemInfo.put("processorCount", Runtime.getRuntime().availableProcessors());
        systemInfo.put("totalMemory", Runtime.getRuntime().totalMemory() / (1024 * 1024) + " MB");
        
        overview.put("systemInfo", systemInfo);
        
        return ResponseEntity.ok(overview);
    }

    /**
     * Generate dashboard message based on metrics
     */
    private String generateDashboardMessage(int communitiesWithDeficit, int criticalPredictions, Long totalCommunities) {
        if (criticalPredictions > 0) {
            return String.format("ALERT: %d communities have critical water scarcity predictions", criticalPredictions);
        } else if (communitiesWithDeficit > 0) {
            return String.format("WARNING: %d communities currently have water deficit", communitiesWithDeficit);
        } else {
            return "All systems normal. Water supply is adequate across communities.";
        }
    }
}
