package com.water.scarcity.ml;

import com.water.scarcity.model.WaterData;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

/**
 * WaterScarcityPredictor - Machine Learning Model for Water Scarcity Prediction
 * 
 * This is a simplified but functional ML model using statistical analysis and
 * time-series forecasting. It analyzes historical water data to predict future
 * scarcity levels.
 * 
 * Model Approach:
 * 1. Analyze historical water balance data
 * 2. Calculate trend using linear regression
 * 3. Forecast future supply and demand
 * 4. Predict scarcity levels and deficits
 * 
 * @author Senior Java Developer
 * @version 1.0
 */
@Component
@NoArgsConstructor
@Slf4j
public class WaterScarcityPredictor {

    // Model configuration constants
    private static final Double CONFIDENCE_THRESHOLD = 0.75;
    private static final Integer MIN_DATA_POINTS = 5;
    private static final Double CRITICAL_THRESHOLD = 0.50;  // 50% deficit
    private static final Double HIGH_THRESHOLD = 0.25;      // 25% deficit
    private static final Double MODERATE_THRESHOLD = 0.10;  // 10% deficit

    /**
     * Predict water scarcity for a community based on historical data
     * 
     * @param historicalWaterData List of historical water data (minimum 5 records needed)
     * @param predictionDaysAhead Number of days to forecast ahead (default: 30)
     * @return ScarcityPrediction object with prediction results
     */
    public ScarcityPrediction predictScarcity(List<WaterData> historicalWaterData, Integer predictionDaysAhead) {
        log.info("Starting scarcity prediction with {} data points", historicalWaterData.size());
        
        ScarcityPrediction prediction = new ScarcityPrediction();
        
        // Validate input data
        if (historicalWaterData == null || historicalWaterData.size() < MIN_DATA_POINTS) {
            log.warn("Insufficient data points for prediction. Minimum required: {}", MIN_DATA_POINTS);
            prediction.setScarcityLevel("UNKNOWN");
            prediction.setConfidence(0.0);
            prediction.setReasoning("Insufficient historical data for accurate prediction");
            return prediction;
        }

        try {
            // Extract supply and demand values
            List<Integer> supplies = extractValues(historicalWaterData, "supply");
            List<Integer> demands = extractValues(historicalWaterData, "demand");

            // Calculate current metrics
            Integer currentSupply = supplies.get(supplies.size() - 1);
            Integer currentDemand = demands.get(demands.size() - 1);
            Integer currentBalance = currentSupply - currentDemand;

            // Calculate linear regression trends
            double supplyTrend = calculateLinearRegressionSlope(supplies);
            double demandTrend = calculateLinearRegressionSlope(demands);

            log.debug("Supply trend: {}, Demand trend: {}", supplyTrend, demandTrend);

            // Forecast future supply and demand
            Integer predictedSupply = (int) (currentSupply + (supplyTrend * predictionDaysAhead / 7));
            Integer predictedDemand = (int) (currentDemand + (demandTrend * predictionDaysAhead / 7));
            Integer predictedBalance = predictedSupply - predictedDemand;

            // Calculate scarcity percentage and confidence
            Double scarcityPercentage = calculateScarcityPercentage(predictedDemand, predictedBalance);
            Double confidence = calculateModelConfidence(historicalWaterData, supplies, demands);

            // Determine scarcity level
            String scarcityLevel = determineScarcityLevel(scarcityPercentage);

            // Build reasoning explanation
            String reasoning = buildReasoning(currentBalance, predictedBalance, supplyTrend, demandTrend, scarcityPercentage);

            // Set prediction results
            prediction.setScarcityLevel(scarcityLevel);
            prediction.setScarcityPercentage(scarcityPercentage);
            prediction.setConfidence(confidence);
            prediction.setPredictedDeficit(Math.max(0, -predictedBalance));
            prediction.setPredictedDemand(predictedDemand);
            prediction.setPredictedSupply(predictedSupply);
            prediction.setReasoning(reasoning);

            log.info("Prediction complete: Level={}, Confidence={}", scarcityLevel, confidence);

        } catch (Exception e) {
            log.error("Error during prediction calculation", e);
            prediction.setScarcityLevel("ERROR");
            prediction.setConfidence(0.0);
            prediction.setReasoning("Error occurred during prediction: " + e.getMessage());
        }

        return prediction;
    }

    /**
     * Extract supply or demand values from water data
     * 
     * @param historicalWaterData List of water data
     * @param type Type to extract ("supply" or "demand")
     * @return List of extracted values
     */
    private List<Integer> extractValues(List<WaterData> historicalWaterData, String type) {
        return historicalWaterData.stream()
            .map(data -> "supply".equals(type) ? data.getTotalWaterSupply() : data.getTotalWaterUsage())
            .collect(Collectors.toList());
    }

    /**
     * Calculate linear regression slope (trend)
     * Formula: slope = Σ((x - mean_x)(y - mean_y)) / Σ((x - mean_x)²)
     * 
     * @param values List of values
     * @return Slope value (trend)
     */
    private double calculateLinearRegressionSlope(List<Integer> values) {
        int n = values.size();
        if (n < 2) return 0.0;

        // Calculate means
        double meanX = (n - 1) / 2.0;  // Mean of indices
        double meanY = values.stream().mapToDouble(Integer::doubleValue).average().orElse(0);

        // Calculate slope components
        double numerator = 0;
        double denominator = 0;

        for (int i = 0; i < n; i++) {
            double x = i;
            double y = values.get(i);
            numerator += (x - meanX) * (y - meanY);
            denominator += (x - meanX) * (x - meanX);
        }

        if (denominator == 0) return 0.0;
        return numerator / denominator;
    }

    /**
     * Calculate scarcity percentage
     * Formula: deficit / demand * 100
     * 
     * @param demand Predicted water demand
     * @param balance Predicted water balance
     * @return Scarcity percentage
     */
    private Double calculateScarcityPercentage(Integer demand, Integer balance) {
        if (demand == null || demand == 0) {
            return 0.0;
        }

        if (balance >= 0) {
            return 0.0;  // No scarcity
        }

        Double deficit = (double) Math.abs(balance);
        return (deficit / demand) * 100;
    }

    /**
     * Calculate model confidence based on data quality and trend consistency
     * 
     * @param historicalWaterData Historical data
     * @param supplies Supply values
     * @param demands Demand values
     * @return Confidence percentage (0-100)
     */
    private Double calculateModelConfidence(List<WaterData> historicalWaterData, 
                                           List<Integer> supplies, 
                                           List<Integer> demands) {
        double confidence = 50.0;  // Base confidence

        // Bonus for more data points
        confidence += Math.min(25.0, (historicalWaterData.size() - MIN_DATA_POINTS) * 2.5);

        // Bonus for data consistency (low variance = consistent patterns)
        double supplyVariance = calculateVariance(supplies);
        double demandVariance = calculateVariance(demands);
        double avgVariance = (supplyVariance + demandVariance) / 2;

        if (avgVariance < 1000) {
            confidence += 15.0;  // High consistency
        } else if (avgVariance < 5000) {
            confidence += 5.0;   // Moderate consistency
        }

        // Bonus for recent data quality
        if (historicalWaterData.stream().filter(d -> d.getWaterQualityScore() != null && d.getWaterQualityScore() > 7).count() > historicalWaterData.size() / 2) {
            confidence += 10.0;
        }

        return Math.min(100.0, confidence);
    }

    /**
     * Calculate variance of a list of values
     * 
     * @param values List of values
     * @return Variance
     */
    private double calculateVariance(List<Integer> values) {
        if (values.isEmpty()) return 0;

        double mean = values.stream().mapToDouble(Integer::doubleValue).average().orElse(0);
        double sumSquareDifferences = values.stream()
            .mapToDouble(v -> Math.pow(v - mean, 2))
            .sum();

        return sumSquareDifferences / values.size();
    }

    /**
     * Determine scarcity level based on scarcity percentage
     * 
     * @param scarcityPercentage Scarcity percentage
     * @return Scarcity level string
     */
    private String determineScarcityLevel(Double scarcityPercentage) {
        if (scarcityPercentage >= CRITICAL_THRESHOLD * 100) {
            return "CRITICAL";
        } else if (scarcityPercentage >= HIGH_THRESHOLD * 100) {
            return "HIGH";
        } else if (scarcityPercentage >= MODERATE_THRESHOLD * 100) {
            return "MODERATE";
        } else {
            return "LOW";
        }
    }

    /**
     * Build reasoning explanation for the prediction
     * 
     * @param currentBalance Current water balance
     * @param predictedBalance Predicted water balance
     * @param supplyTrend Supply trend
     * @param demandTrend Demand trend
     * @param scarcityPercentage Scarcity percentage
     * @return Reasoning string
     */
    private String buildReasoning(Integer currentBalance, Integer predictedBalance, 
                                 double supplyTrend, double demandTrend, 
                                 Double scarcityPercentage) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Current water balance: ").append(currentBalance).append(" liters. ");
        
        if (supplyTrend < 0) {
            sb.append("Water supply is DECLINING at rate of ").append(String.format("%.0f", -supplyTrend)).append(" L/week. ");
        } else if (supplyTrend > 0) {
            sb.append("Water supply is IMPROVING at rate of ").append(String.format("%.0f", supplyTrend)).append(" L/week. ");
        }
        
        if (demandTrend > 0) {
            sb.append("Water demand is INCREASING at rate of ").append(String.format("%.0f", demandTrend)).append(" L/week. ");
        }
        
        if (predictedBalance < 0) {
            sb.append("PREDICTED DEFICIT: ").append(Math.abs(predictedBalance)).append(" liters. ");
            sb.append("Scarcity level: ").append(String.format("%.1f", scarcityPercentage)).append("%. ");
        } else {
            sb.append("Sufficient water supply expected.");
        }
        
        return sb.toString();
    }

    /**
     * Model performance information
     * @return Model version and accuracy info
     */
    public String getModelInfo() {
        return "Water Scarcity Prediction Model v1.0 - " +
               "Statistical Time-Series Forecasting using Linear Regression. " +
               "Minimum data points required: " + MIN_DATA_POINTS;
    }
}
