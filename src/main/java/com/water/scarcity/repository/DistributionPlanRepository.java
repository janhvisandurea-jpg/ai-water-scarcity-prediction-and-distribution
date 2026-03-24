package com.water.scarcity.repository;

import com.water.scarcity.model.DistributionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for DistributionPlan entity
 * Handles database operations for distribution plans
 * 
 * @author Senior Java Developer
 * @version 1.0
 */
@Repository
public interface DistributionPlanRepository extends JpaRepository<DistributionPlan, Integer> {

    /**
     * Find distribution plans by status
     * @param status Plan status (DRAFT, APPROVED, IN_PROGRESS, COMPLETED)
     * @return List of distribution plans
     */
    List<DistributionPlan> findByStatus(String status);

    /**
     * Find latest distribution plan regardless of status
     * @return Latest plan
     */
    @Query(value = "SELECT * FROM distribution_plans ORDER BY distribution_date DESC LIMIT 1",
           nativeQuery = true)
    Optional<DistributionPlan> findLatestPlan();

    /**
     * Find approved distribution plans
     * @return List of approved plans
     */
    List<DistributionPlan> findByStatusOrderByDistributionDateDesc(String status);

    /**
     * Find active distribution plans (IN_PROGRESS or APPROVED)
     * @return List of active plans
     */
    @Query(value = "SELECT * FROM distribution_plans WHERE status IN ('APPROVED', 'IN_PROGRESS') ORDER BY distribution_date DESC",
           nativeQuery = true)
    List<DistributionPlan> findActivePlans();

    /**
     * Find plans created within a date range
     * @param startDate Start date
     * @param endDate End date
     * @return List of plans
     */
    List<DistributionPlan> findByDistributionDateBetweenOrderByDistributionDateDesc(LocalDate startDate, LocalDate endDate);

    /**
     * Find highest efficiency plans
     * @param limit Number of plans
     * @return List of plans with highest efficiency
     */
    @Query(value = "SELECT * FROM distribution_plans ORDER BY efficiency_score DESC LIMIT :limit",
           nativeQuery = true)
    List<DistributionPlan> findTopByEfficiency(@Param("limit") int limit);

    /**
     * Get average efficiency score
     * @return Average efficiency
     */
    @Query(value = "SELECT AVG(efficiency_score) FROM distribution_plans WHERE efficiency_score IS NOT NULL",
           nativeQuery = true)
    Double getAverageEfficiency();

    /**
     * Count plans by status
     * @param status Status
     * @return Count of plans
     */
    Integer countByStatus(String status);
}
