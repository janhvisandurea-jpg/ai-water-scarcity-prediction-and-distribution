package com.water.scarcity.repository;

import com.water.scarcity.model.WaterData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for WaterData entity
 * Handles database operations for water data records
 * 
 * @author Senior Java Developer
 * @version 1.0
 */
@Repository
public interface WaterDataRepository extends JpaRepository<WaterData, Integer> {

    /**
     * Find latest water data for a community
     * @param communityId Community ID
     * @return Latest water data record
     */
    @Query(value = "SELECT * FROM water_data WHERE community_id = :communityId ORDER BY recorded_date DESC LIMIT 1",
           nativeQuery = true)
    Optional<WaterData> findLatestByCommunityId(@Param("communityId") Integer communityId);

    /**
     * Find water data for a community on a specific date
     * @param communityId Community ID
     * @param recordedDate Date of recording
     * @return Water data if exists
     */
    Optional<WaterData> findByCommunityIdAndRecordedDate(Integer communityId, LocalDate recordedDate);

    /**
     * Find water data for a community within a date range
     * @param communityId Community ID
     * @param startDate Start date
     * @param endDate End date
     * @return List of water data records
     */
    List<WaterData> findByCommunityIdAndRecordedDateBetweenOrderByRecordedDateDesc(
        Integer communityId, LocalDate startDate, LocalDate endDate);

    /**
     * Find all water data for a community (all-time)
     * @param communityId Community ID
     * @return List of all water data records
     */
    List<WaterData> findByCommunityIdOrderByRecordedDateDesc(Integer communityId);

    /**
     * Find water data for a specific date across all communities
     * @param recordedDate Date
     * @return List of water data records for that date
     */
    List<WaterData> findByRecordedDate(LocalDate recordedDate);

    /**
     * Get average water balance for a community
     * @param communityId Community ID
     * @return Average balance value
     */
    @Query(value = "SELECT AVG(total_water_supply - total_water_usage) FROM water_data WHERE community_id = :communityId",
           nativeQuery = true)
    Double getAverageWaterBalanceByCommunity(@Param("communityId") Integer communityId);

    /**
     * Get total rainfall for a community in a date range
     * @param communityId Community ID
     * @param startDate Start date
     * @param endDate End date
     * @return Total rainfall in mm
     */
    @Query(value = "SELECT SUM(rainfall_mm) FROM water_data WHERE community_id = :communityId " +
                   "AND recorded_date BETWEEN :startDate AND :endDate",
           nativeQuery = true)
    Double getTotalRainfallByCommunity(@Param("communityId") Integer communityId, 
                                       @Param("startDate") LocalDate startDate, 
                                       @Param("endDate") LocalDate endDate);
}
