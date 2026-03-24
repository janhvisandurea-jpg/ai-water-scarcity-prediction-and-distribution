package com.water.scarcity.repository;

import com.water.scarcity.model.DistributionAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for DistributionAllocation entity
 * Handles database operations for community-level water allocations
 * 
 * @author Senior Java Developer
 * @version 1.0
 */
@Repository
public interface DistributionAllocationRepository extends JpaRepository<DistributionAllocation, Integer> {

    /**
     * Find all allocations for a specific distribution plan
     * @param planId Distribution plan ID
     * @return List of allocations
     */
    List<DistributionAllocation> findByPlanId(Integer planId);

    /**
     * Find allocation for a community in a specific plan
     * @param planId Plan ID
     * @param communityId Community ID
     * @return Allocation if exists
     */
    Optional<DistributionAllocation> findByPlanIdAndCommunityId(Integer planId, Integer communityId);

    /**
     * Find allocations for a community across all plans
     * @param communityId Community ID
     * @return List of allocations
     */
    List<DistributionAllocation> findByCommunityId(Integer communityId);

    /**
     * Find high-priority allocations in a plan
     * @param planId Plan ID
     * @param priorityThreshold Minimum priority level
     * @return List of high-priority allocations
     */
    @Query(value = "SELECT * FROM distribution_allocations WHERE plan_id = :planId AND priority_level >= :threshold",
           nativeQuery = true)
    List<DistributionAllocation> findHighPriorityAllocations(@Param("planId") Integer planId, 
                                                             @Param("threshold") Integer priorityThreshold);

    /**
     * Find allocations that don't meet minimum requirement
     * @param planId Plan ID
     * @return List of problematic allocations
     */
    @Query(value = "SELECT * FROM distribution_allocations WHERE plan_id = :planId " +
                   "AND allocated_water < minimum_required",
           nativeQuery = true)
    List<DistributionAllocation> findBelowMinimumAllocations(@Param("planId") Integer planId);

    /**
     * Find allocations that exceed maximum capacity
     * @param planId Plan ID
     * @return List of problematic allocations
     */
    @Query(value = "SELECT * FROM distribution_allocations WHERE plan_id = :planId " +
                   "AND allocated_water > maximum_capacity",
           nativeQuery = true)
    List<DistributionAllocation> findExceedsMaximumAllocations(@Param("planId") Integer planId);

    /**
     * Get total allocated water for a plan
     * @param planId Plan ID
     * @return Total allocation
     */
    @Query(value = "SELECT SUM(allocated_water) FROM distribution_allocations WHERE plan_id = :planId",
           nativeQuery = true)
    Integer getTotalAllocatedWaterForPlan(@Param("planId") Integer planId);

    /**
     * Get average allocation per community in a plan
     * @param planId Plan ID
     * @return Average allocation
     */
    @Query(value = "SELECT AVG(allocated_water) FROM distribution_allocations WHERE plan_id = :planId",
           nativeQuery = true)
    Integer getAverageAllocationForPlan(@Param("planId") Integer planId);

    /**
     * Count allocations in a plan that meet all constraints
     * @param planId Plan ID
     * @return Count of valid allocations
     */
    @Query(value = "SELECT COUNT(*) FROM distribution_allocations WHERE plan_id = :planId " +
                   "AND allocated_water >= minimum_required AND allocated_water <= maximum_capacity",
           nativeQuery = true)
    Integer countValidAllocationsInPlan(@Param("planId") Integer planId);
}
