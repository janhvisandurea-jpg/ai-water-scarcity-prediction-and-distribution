package com.water.scarcity.service;

import com.water.scarcity.ml.ScarcityPrediction;
import com.water.scarcity.ml.WaterScarcityPredictor;
import com.water.scarcity.model.Community;
import com.water.scarcity.model.PredictionResult;
import com.water.scarcity.model.WaterData;
import com.water.scarcity.repository.CommunityRepository;
import com.water.scarcity.repository.PredictionResultRepository;
import com.water.scarcity.repository.WaterDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * PredictionService - Integrates ML model with database operations
 * 
 * This service handles:
 * 1. Running ML predictions on water data
 * 2. Storing predictions in database
 * 3. Retrieving prediction history
 * 4. Generating predictions for multiple communities
 * 
 * @author Senior Java Developer
 * @version 1.0
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PredictionService {

    private final WaterScarcityPredictor mlModel;
    private final WaterDataRepository waterDataRepository;
    private final CommunityRepository communityRepository;
    private final PredictionResultRepository predictionResultRepository;

    @Value("${app.ml.model.accuracy-threshold:0.75}")
    private Double accuracyThreshold;

    @Value("${app.ml.model.prediction-days:30}")
    private Integer defaultPredictionDays;

    /**
     * Generate prediction for a single community
     * 
     * @param communityId Community ID
     * @param predictionDays Days to predict ahead
     * @return Saved PredictionResult entity
     */
    public PredictionResult generatePredictionForCommunity(Integer communityId, Integer predictionDays) {
        log.info("Generating prediction for community: {}, days: {}", communityId, predictionDays);

        try {
            // Fetch community
            Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("Community not found with ID: " + communityId));

            // Fetch historical water data (last 60 days)
            List<WaterData> historicalData = waterDataRepository
                .findByCommunityIdAndRecordedDateBetweenOrderByRecordedDateDesc(
                    communityId,
                    LocalDate.now().minusDays(60),
                    LocalDate.now()
                );

            // Ensure data is sorted chronologically (oldest first)
            historicalData.sort((d1, d2) -> d1.getRecordedDate().compareTo(d2.getRecordedDate()));

            // Run ML prediction
            ScarcityPrediction mlPrediction = mlModel.predictScarcity(historicalData, predictionDays);

            log.debug("ML Prediction result: {}", mlPrediction);

            // Create PredictionResult entity
            PredictionResult result = PredictionResult.builder()
                .community(community)
                .scarcityLevel(mlPrediction.getScarcityLevel())
                .scarcityPercentage(mlPrediction.getScarcityPercentage())
                .confidence(mlPrediction.getConfidence())
                .predictedWaterDeficit(mlPrediction.getPredictedDeficit())
                .predictedDemand(mlPrediction.getPredictedDemand())
                .predictedSupply(mlPrediction.getPredictedSupply())
                .predictionDate(LocalDate.now())
                .predictionHorizon(predictionDays)
                .dataPointsUsed(historicalData.size())
                .modelAccuracy(calculateModelAccuracy(historicalData))
                .modelVersion("1.0")
                .build();

            // Save to database
            PredictionResult saved = predictionResultRepository.save(result);
            log.info("Prediction saved with ID: {}", saved.getId());

            return saved;

        } catch (Exception e) {
            log.error("Error generating prediction for community: {}", communityId, e);
            throw new RuntimeException("Failed to generate prediction: " + e.getMessage());
        }
    }

    /**
     * Generate predictions for all active communities
     * 
     * @param predictionDays Days to predict ahead
     * @return List of saved predictions
     */
    public List<PredictionResult> generatePredictionsForAllCommunities(Integer predictionDays) {
        log.info("Generating predictions for all active communities");

        List<Community> activeCommunities = communityRepository.findByIsActiveTrue();
        log.debug("Found {} active communities", activeCommunities.size());

        return activeCommunities.stream()
            .map(community -> {
                try {
                    return generatePredictionForCommunity(community.getId(), predictionDays);
                } catch (Exception e) {
                    log.error("Failed to generate prediction for community: {}", community.getName(), e);
                    return null;
                }
            })
            .filter(p -> p != null)
            .toList();
    }

    /**
     * Get latest prediction for a community
     * 
     * @param communityId Community ID
     * @return Latest prediction if exists
     */
    public Optional<PredictionResult> getLatestPrediction(Integer communityId) {
        log.debug("Fetching latest prediction for community: {}", communityId);
        return predictionResultRepository.findLatestByCommunityId(communityId);
    }

    /**
     * Get prediction history for a community
     * 
     * @param communityId Community ID
     * @param days Number of past days
     * @return List of predictions
     */
    public List<PredictionResult> getPredictionHistory(Integer communityId, int days) {
        log.debug("Fetching prediction history for community: {}, days: {}", communityId, days);

        LocalDate startDate = LocalDate.now().minusDays(days);
        return predictionResultRepository.findByCommunityIdAndPredictionDateBetweenOrderByPredictionDateDesc(
            communityId, startDate, LocalDate.now());
    }

    /**
     * Get all critical predictions across communities
     * 
     * @return List of critical predictions
     */
    public List<PredictionResult> getCriticalPredictions() {
        log.debug("Fetching all critical predictions");
        return predictionResultRepository.findAllCriticalPredictions();
    }

    /**
     * Get high confidence predictions
     * 
     * @param confidenceThreshold Minimum confidence level
     * @return List of predictions
     */
    public List<PredictionResult> getHighConfidencePredictions(Double confidenceThreshold) {
        log.debug("Fetching predictions with confidence >= {}", confidenceThreshold);
        return predictionResultRepository.findHighConfidencePredictions(confidenceThreshold);
    }

    /**
     * Calculate model accuracy based on historical data quality
     * This is a simplified accuracy metric based on data availability
     * 
     * @param historicalData Historical water data
     * @return Accuracy percentage
     */
    private Double calculateModelAccuracy(List<WaterData> historicalData) {
        if (historicalData.isEmpty()) {
            return 0.0;
        }

        // Base accuracy
        double accuracy = 60.0;

        // Bonus for data points
        accuracy += Math.min(20.0, historicalData.size() * 0.5);

        // Bonus for data quality (having quality scores)
        long dataWithQuality = historicalData.stream()
            .filter(d -> d.getWaterQualityScore() != null && d.getWaterQualityScore() > 0)
            .count();
        
        accuracy += (dataWithQuality / (double) historicalData.size()) * 15;

        return Math.min(100.0, accuracy);
    }

    /**
     * Get model information
     * 
     * @return Model version and description
     */
    public String getModelInfo() {
        return mlModel.getModelInfo();
    }

    /**
     * Get average prediction confidence across all predictions
     * 
     * @return Average confidence
     */
    public Double getAverageConfidence() {
        log.debug("Calculating average prediction confidence");
        Double accuracy = predictionResultRepository.getAverageModelAccuracy();
        return accuracy != null ? accuracy : 0.0;
    }

    /**
     * Count critical predictions by community
     * 
     * @param communityId Community ID
     * @return Count of critical predictions
     */
    public Integer getCriticalPredictionCount(Integer communityId) {
        log.debug("Getting critical prediction count for community: {}", communityId);
        return predictionResultRepository.countCriticalPredictionsByCommunity(communityId);
    }

    /**
     * Generate prediction using custom prediction days
     * 
     * @param communityId Community ID
     * @return Prediction result (uses default days if not provided)
     */
    public PredictionResult generatePredictionForCommunity(Integer communityId) {
        return generatePredictionForCommunity(communityId, defaultPredictionDays);
    }
}
