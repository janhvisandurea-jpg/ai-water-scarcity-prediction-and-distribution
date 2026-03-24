package com.water.scarcity.controller;

import com.water.scarcity.model.WaterData;
import com.water.scarcity.service.WaterDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WaterDataController - REST API endpoints for Water Data management
 * 
 * Base URL: /api/water-data
 * 
 * Endpoints:
 * - POST   /api/water-data                 - Add water data
 * - GET    /api/water-data/{communityId}   - Get latest water data
 * - GET    /api/water-data/{communityId}/history - Get water data history
 * - GET    /api/water-data/{communityId}/balance - Get water balance info
 * - GET    /api/water-data/{communityId}/deficit - Check water deficit
 * 
 * @author Senior Java Developer
 * @version 1.0
 */
@RestController
@RequestMapping("/api/water-data")
@RequiredArgsConstructor
@Slf4j
public class WaterDataController {

    private final WaterDataService waterDataService;

    /**
     * Add new water data record
     * @param waterData Water data to save
     * @return Saved water data with ID
     */
    @PostMapping
    public ResponseEntity<?> addWaterData(@RequestBody WaterData waterData) {
        log.info("POST /api/water-data - Adding water data for community: {}", waterData.getCommunity().getId());
        
        try {
            WaterData saved = waterDataService.addWaterData(waterData);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            log.error("Error adding water data", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get latest water data for a community
     * @param communityId Community ID
     * @return Latest water data record
     */
    @GetMapping("/{communityId}/latest")
    public ResponseEntity<?> getLatestWaterData(@PathVariable Integer communityId) {
        log.info("GET /api/water-data/{}/latest - Fetching latest water data", communityId);
        
        return waterDataService.getLatestWaterData(communityId)
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "No water data found for community: " + communityId)));
    }

    /**
     * Get water data history for a community
     * @param communityId Community ID
     * @param days Number of past days (default 30)
     * @return List of water data records
     */
    @GetMapping("/{communityId}/history")
    public ResponseEntity<List<WaterData>> getWaterDataHistory(
            @PathVariable Integer communityId,
            @RequestParam(defaultValue = "30") int days) {
        log.info("GET /api/water-data/{}/history - Fetching water data for past {} days", communityId, days);
        
        List<WaterData> history = waterDataService.getWaterDataHistory(communityId, days);
        return ResponseEntity.ok(history);
    }

    /**
     * Get water balance information
     * @param communityId Community ID
     * @return Water balance metrics
     */
    @GetMapping("/{communityId}/balance")
    public ResponseEntity<Map<String, Object>> getWaterBalance(@PathVariable Integer communityId) {
        log.info("GET /api/water-data/{}/balance - Getting water balance", communityId);
        
        Map<String, Object> balance = new HashMap<>();
        
        waterDataService.getLatestWaterData(communityId).ifPresentOrElse(
            data -> {
                balance.put("communityId", communityId);
                balance.put("currentBalance", data.getWaterBalance());
                balance.put("totalSupply", data.getTotalWaterSupply());
                balance.put("totalUsage", data.getTotalWaterUsage());
                balance.put("surplus", data.getWaterBalance() > 0);
                balance.put("timestamp", data.getRecordedDate());
            },
            () -> {
                balance.put("error", "No water data found");
                balance.put("communityId", communityId);
            }
        );
        
        return ResponseEntity.ok(balance);
    }

    /**
     * Check water deficit status
     * @param communityId Community ID
     * @return Deficit information
     */
    @GetMapping("/{communityId}/deficit")
    public ResponseEntity<Map<String, Object>> checkWaterDeficit(@PathVariable Integer communityId) {
        log.info("GET /api/water-data/{}/deficit - Checking water deficit", communityId);
        
        Map<String, Object> deficit = new HashMap<>();
        deficit.put("communityId", communityId);
        deficit.put("hasDeficit", waterDataService.hasWaterDeficit(communityId));
        deficit.put("deficitPercentage", waterDataService.getDeficitPercentage(communityId));
        deficit.put("averageBalance", waterDataService.getAverageWaterBalance(communityId));
        
        return ResponseEntity.ok(deficit);
    }

    /**
     * Get communities with water deficit
     * @return List of communities with deficit
     */
    @GetMapping("/deficit-communities")
    public ResponseEntity<?> getCommunitiesWithDeficit() {
        log.info("GET /api/water-data/deficit-communities - Getting communities with water deficit");
        
        var communities = waterDataService.getCommunitiesWithDeficit();
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", communities.size());
        response.put("communities", communities);
        response.put("status", communities.isEmpty() ? "No communities with deficit" : "Communities with active water deficit");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get water quality trend
     * @param communityId Community ID
     * @param days Number of past days
     * @return Quality scores trend
     */
    @GetMapping("/{communityId}/quality-trend")
    public ResponseEntity<Map<String, Object>> getQualityTrend(
            @PathVariable Integer communityId,
            @RequestParam(defaultValue = "30") int days) {
        log.info("GET /api/water-data/{}/quality-trend - Getting water quality trend", communityId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("communityId", communityId);
        response.put("qualityScores", waterDataService.getWaterQualityTrend(communityId, days));
        response.put("days", days);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get total rainfall period
     * @param communityId Community ID
     * @param days Number of past days
     * @return Total rainfall information
     */
    @GetMapping("/{communityId}/rainfall")
    public ResponseEntity<Map<String, Object>> getRainfallInfo(
            @PathVariable Integer communityId,
            @RequestParam(defaultValue = "30") int days) {
        log.info("GET /api/water-data/{}/rainfall - Getting rainfall data", communityId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("communityId", communityId);
        response.put("totalRainfallMm", waterDataService.getTotalRainfallPeriod(communityId, days));
        response.put("period", days + " days");
        
        return ResponseEntity.ok(response);
    }
}
