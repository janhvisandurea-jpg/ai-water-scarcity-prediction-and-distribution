package com.water.scarcity.repository;

import com.water.scarcity.model.PopulationData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for PopulationData entity
 * Handles database operations for population data records
 * 
 * @author Senior Java Developer
 * @version 1.0
 */
@Repository
public interface PopulationDataRepository extends JpaRepository<PopulationData, Integer> {

    /**
     * Find latest population data for a community
     * @param communityId Community ID
     * @return Latest population data
     */
    @Query(value = "SELECT * FROM population_data WHERE community_id = :communityId ORDER BY recorded_date DESC LIMIT 1",
           nativeQuery = true)
    Optional<PopulationData> findLatestByCommunityId(@Param("communityId") Integer communityId);

    /**
     * Find population data for a community on a specific date
     * @param communityId Community ID
     * @param recordedDate Date
     * @return Population data if exists
     */
    Optional<PopulationData> findByCommunityIdAndRecordedDate(Integer communityId, LocalDate recordedDate);

    /**
     * Find population data for a community within a date range
     * @param communityId Community ID
     * @param startDate Start date
     * @param endDate End date
     * @return List of population data
     */
    List<PopulationData> findByCommunityIdAndRecordedDateBetweenOrderByRecordedDateAsc(
        Integer communityId, LocalDate startDate, LocalDate endDate);

    /**
     * Find all population data for a community
     * @param communityId Community ID
     * @return List of all population data records
     */
    List<PopulationData> findByCommunityIdOrderByRecordedDateDesc(Integer communityId);

    /**
     * Find population data for all communities on a specific date
     * @param recordedDate Date
     * @return List of population data
     */
    List<PopulationData> findByRecordedDate(LocalDate recordedDate);

    /**
     * Get average population growth rate
     * @return Average growth rate across all records
     */
    @Query(value = "SELECT AVG(growth_rate) FROM population_data WHERE growth_rate IS NOT NULL",
           nativeQuery = true)
    Double getAverageGrowthRate();

    /**
     * Get average per-capita water usage
     * @param communityId Community ID
     * @return Average usage
     */
    @Query(value = "SELECT AVG(avg_per_capita_usage) FROM population_data WHERE community_id = :communityId",
           nativeQuery = true)
    Integer getAveragePerCapitaUsage(@Param("communityId") Integer communityId);

    /**
     * Find high-poverty communities (poverty rate > threshold)
     * @param threshold Poverty rate threshold
     * @return List of population data records
     */
    @Query(value = "SELECT * FROM population_data WHERE poverty_rate > :threshold AND recorded_date = " +
                   "(SELECT MAX(recorded_date) FROM population_data WHERE poverty_rate > :threshold)",
           nativeQuery = true)
    List<PopulationData> findHighPovertyCommunitiesLatest(@Param("threshold") Double threshold);
}
