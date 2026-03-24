package com.water.scarcity.controller;

import com.water.scarcity.model.PredictionResult;
import com.water.scarcity.service.PredictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PredictionController - REST API endpoints for ML Predictions
 * 
 * Base URL: /api/predictions
 * 
 * Endpoints:
 * - POST   /api/predictions                    - Generate prediction for a community
 * - POST   /api/predictions/all                - Generate predictions for all communities
 * - GET    /api/predictions/{communityId}     - Get latest prediction
 * - GET    /api/predictions/{communityId}/history - Get prediction history
 * - GET    /api/predictions/critical          - Get critical predictions
 * - GET    /api/predictions/model-info        - Get ML model information
 * 
 * @author Senior Java Developer
 * @version 1.0
 */
@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
@Slf4j
public class PredictionController {

    private final PredictionService predictionService;

    /**
     * Generate prediction for a specific community
     * @param communityId Community ID
     * @param days Days to predict ahead (optional)
     * @return Generated prediction
     */
    @PostMapping("/{communityId}")
    public ResponseEntity<?> generatePrediction(
            @PathVariable Integer communityId,
            @RequestParam(required = false) Integer days) {
        log.info("POST /api/predictions/{} - Generating prediction", communityId);
        
        try {
            PredictionResult prediction;
            if (days != null) {
                prediction = predictionService.generatePredictionForCommunity(communityId, days);
            } else {
                prediction = predictionService.generatePredictionForCommunity(communityId);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(prediction);
        } catch (Exception e) {
            log.error("Error generating prediction", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Generate predictions for all active communities
     * @param days Days to predict ahead (optional)
     * @return List of predictions
     */
    @PostMapping("/all")
    public ResponseEntity<?> generatePredictionsForAll(
            @RequestParam(required = false) Integer days) {
        log.info("POST /api/predictions/all - Generating predictions for all communities");
        
        try {
            Integer predDays = days != null ? days : 30;
            List<PredictionResult> predictions = predictionService.generatePredictionsForAllCommunities(predDays);
            
            Map<String, Object> response = new HashMap<>();
            response.put("count", predictions.size());
            response.put("predictions", predictions);
            response.put("message", "Predictions generated successfully");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error generating batch predictions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get latest prediction for a community
     * @param communityId Community ID
     * @return Latest prediction
     */
    @GetMapping("/{communityId}")
    public ResponseEntity<?> getLatestPrediction(@PathVariable Integer communityId) {
        log.info("GET /api/predictions/{} - Fetching latest prediction", communityId);
        
        return predictionService.getLatestPrediction(communityId)
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "No prediction found for community: " + communityId)));
    }

    /**
     * Get prediction history for a community
     * @param communityId Community ID
     * @param days Number of past days (default 30)
     * @return List of predictions
     */
    @GetMapping("/{communityId}/history")
    public ResponseEntity<Map<String, Object>> getPredictionHistory(
            @PathVariable Integer communityId,
            @RequestParam(defaultValue = "30") int days) {
        log.info("GET /api/predictions/{}/history - Getting prediction history", communityId);
        
        List<PredictionResult> history = predictionService.getPredictionHistory(communityId, days);
        
        Map<String, Object> response = new HashMap<>();
        response.put("communityId", communityId);
        response.put("count", history.size());
        response.put("days", days);
        response.put("predictions", history);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get all critical predictions
     * @return List of critical predictions
     */
    @GetMapping("/critical")
    public ResponseEntity<Map<String, Object>> getCriticalPredictions() {
        log.info("GET /api/predictions/critical - Fetching critical predictions");
        
        List<PredictionResult> criticalPredictions = predictionService.getCriticalPredictions();
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", criticalPredictions.size());
        response.put("status", criticalPredictions.isEmpty() ? "No critical predictions" : "ALERT: Critical water scarcity");
        response.put("predictions", criticalPredictions);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get high confidence predictions
     * @param threshold Confidence threshold (0-100)
     * @return List of high-confidence predictions
     */
    @GetMapping("/high-confidence")
    public ResponseEntity<Map<String, Object>> getHighConfidencePredictions(
            @RequestParam(defaultValue = "75") Double threshold) {
        log.info("GET /api/predictions/high-confidence - Fetching predictions with confidence >= {}", threshold);
        
        List<PredictionResult> highConfidence = predictionService.getHighConfidencePredictions(threshold);
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", highConfidence.size());
        response.put("confidenceThreshold", threshold);
        response.put("predictions", highConfidence);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get ML model information
     * @return Model info
     */
    @GetMapping("/model-info")
    public ResponseEntity<Map<String, Object>> getModelInfo() {
        log.info("GET /api/predictions/model-info - Getting ML model information");
        
        Map<String, Object> info = new HashMap<>();
        info.put("modelName", "Water Scarcity Prediction Model");
        info.put("version", "1.0");
        info.put("algorithm", "Statistical Time-Series Forecasting with Linear Regression");
        info.put("description", predictionService.getModelInfo());
        info.put("averageAccuracy", predictionService.getAverageConfidence());
        
        return ResponseEntity.ok(info);
    }

    /**
     * Get prediction statistics
     * @return Statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/predictions/stats - Getting prediction statistics");
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("averageConfidence", predictionService.getAverageConfidence());
        stats.put("totalCriticalPredictions", predictionService.getCriticalPredictions().size());
        stats.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(stats);
    }
}
