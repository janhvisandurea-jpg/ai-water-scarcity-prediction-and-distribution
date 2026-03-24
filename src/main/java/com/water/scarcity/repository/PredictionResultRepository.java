package com.water.scarcity.repository;

import com.water.scarcity.model.PredictionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for PredictionResult entity
 * Handles database operations for ML prediction records
 * 
 * @author Senior Java Developer
 * @version 1.0
 */
@Repository
public interface PredictionResultRepository extends JpaRepository<PredictionResult, Integer> {

    /**
     * Find latest prediction for a community
     * @param communityId Community ID
     * @return Latest prediction
     */
    @Query(value = "SELECT * FROM prediction_results WHERE community_id = :communityId ORDER BY prediction_date DESC LIMIT 1",
           nativeQuery = true)
    Optional<PredictionResult> findLatestByCommunityId(@Param("communityId") Integer communityId);

    /**
     * Find predictions for a community on a specific date
     * @param communityId Community ID
     * @param predictionDate Date
     * @return List of predictions
     */
    List<PredictionResult> findByCommunityIdAndPredictionDate(Integer communityId, LocalDate predictionDate);

    /**
     * Find predictions for a community within a date range
     * @param communityId Community ID
     * @param startDate Start date
     * @param endDate End date
     * @return List of predictions
     */
    List<PredictionResult> findByCommunityIdAndPredictionDateBetweenOrderByPredictionDateDesc(
        Integer communityId, LocalDate startDate, LocalDate endDate);

    /**
     * Find predictions with specific scarcity level
     * @param communityId Community ID
     * @param scarcityLevel Scarcity level (LOW, MODERATE, HIGH, CRITICAL)
     * @return List of predictions
     */
    List<PredictionResult> findByCommunityIdAndScarcityLevel(Integer communityId, String scarcityLevel);

    /**
     * Find all critical predictions (CRITICAL scarcity level)
     * @return List of critical predictions
     */
    @Query(value = "SELECT * FROM prediction_results WHERE scarcity_level = 'CRITICAL' ORDER BY prediction_date DESC",
           nativeQuery = true)
    List<PredictionResult> findAllCriticalPredictions();

    /**
     * Find predictions with high confidence
     * @param confidenceThreshold Minimum confidence level
     * @return List of high-confidence predictions
     */
    @Query(value = "SELECT * FROM prediction_results WHERE confidence >= :threshold ORDER BY prediction_date DESC",
           nativeQuery = true)
    List<PredictionResult> findHighConfidencePredictions(@Param("threshold") Double confidenceThreshold);

    /**
     * Get average model accuracy
     * @return Average accuracy
     */
    @Query(value = "SELECT AVG(model_accuracy) FROM prediction_results",
           nativeQuery = true)
    Double getAverageModelAccuracy();

    /**
     * Find predictions by model version
     * @param modelVersion Model version string
     * @return List of predictions
     */
    List<PredictionResult> findByModelVersion(String modelVersion);

    /**
     * Count predictions by scarcity level for a community
     * @param communityId Community ID
     * @return Count of critical predictions
     */
    @Query(value = "SELECT COUNT(*) FROM prediction_results WHERE community_id = :communityId AND scarcity_level = 'CRITICAL'",
           nativeQuery = true)
    Integer countCriticalPredictionsByCommunity(@Param("communityId") Integer communityId);
}
