package com.water.scarcity.model;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * PredictionResult Entity - Stores ML model predictions for water scarcity
 * 
 * This entity captures predictions from the machine learning model, including
 * scarcity levels, confidence scores, and prediction details.
 * 
 * @author Senior Java Developer
 * @version 1.0
 */
@Entity
@Table(name = "prediction_results", indexes = {
    @Index(name = "idx_prediction_community_date", columnList = "community_id, prediction_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    // ========== SCARCITY PREDICTION ==========
    @Column(name = "scarcity_level", nullable = false, length = 20)
    private String scarcityLevel;  // LOW, MODERATE, HIGH, CRITICAL

    @Column(name = "scarcity_percentage", precision = 5, scale = 2)
    private Double scarcityPercentage;  // 0-100

    @Column(name = "confidence", precision = 5, scale = 2)
    private Double confidence;  // Model confidence 0-100

    // ========== PREDICTION DETAILS ==========
    @Column(name = "predicted_water_deficit")
    private Integer predictedWaterDeficit;  // in liters

    @Column(name = "predicted_demand")
    private Integer predictedDemand;  // in liters

    @Column(name = "predicted_supply")
    private Integer predictedSupply;  // in liters

    // ========== TIMELINE ==========
    @Column(name = "prediction_date", nullable = false)
    private LocalDate predictionDate;  // Date of prediction

    @Column(name = "prediction_horizon")
    private Integer predictionHorizon;  // days ahead

    @Column(name = "data_points_used")
    private Integer dataPointsUsed;  // Number of data points used

    // ========== MODEL PERFORMANCE ==========
    @Column(name = "model_accuracy", precision = 5, scale = 2)
    private Double modelAccuracy;  // Model accuracy percentage

    @Column(name = "model_version", length = 20)
    private String modelVersion;  // Version of ML model

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Determine if this prediction is reliable
     * @return true if confidence and accuracy are above threshold
     */
    public Boolean isReliable(Double confidenceThreshold, Double accuracyThreshold) {
        if (confidence == null || modelAccuracy == null) {
            return false;
        }
        return confidence >= confidenceThreshold && modelAccuracy >= accuracyThreshold;
    }

    /**
     * Get severity level based on scarcity percentage
     * @return Severity from 1 (lowest) to 5 (highest)
     */
    public Integer getSeverityLevel() {
        if (scarcityPercentage == null) {
            return 1;
        }
        if (scarcityPercentage < 10) return 1;
        if (scarcityPercentage < 25) return 2;
        if (scarcityPercentage < 50) return 3;
        if (scarcityPercentage < 75) return 4;
        return 5;
    }
}
