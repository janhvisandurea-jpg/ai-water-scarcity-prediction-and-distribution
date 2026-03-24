package com.water.scarcity.ml;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * ScarcityPrediction - Data class for ML prediction results
 * 
 * Encapsulates water scarcity prediction output from the ML model.
 * 
 * @author Senior Java Developer
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScarcityPrediction {
    private String scarcityLevel;           // LOW, MODERATE, HIGH, CRITICAL
    private Double scarcityPercentage;      // 0-100
    private Double confidence;              // 0-100 confidence score
    private Integer predictedDeficit;       // Predicted water deficit in liters
    private Integer predictedDemand;        // Predicted water demand
    private Integer predictedSupply;        // Predicted water supply
    private String reasoning;               // Explanation of prediction
}
