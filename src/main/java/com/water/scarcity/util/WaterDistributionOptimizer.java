package com.water.scarcity.util;

import com.water.scarcity.model.Community;
import com.water.scarcity.model.DistributionAllocation;
import com.water.scarcity.model.DistributionPlan;
import com.water.scarcity.model.PredictionResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * WaterDistributionOptimizer - Optimization algorithm for water distribution
 * 
 * This utility implements a greedy optimization algorithm that allocates water
 * efficiently among communities based on:
 * 1. Population and water demand
 * 2. Current scarcity levels
 * 3. Priority levels
 * 4. Storage capacity constraints
 * 
 * Algorithm: Priority-based Greedy Distribution
 * - Communities with higher scarcity get higher allocation
 * - Respects minimum and maximum capacity constraints
 * - Ensures equitable distribution based on need
 * 
 * @author Senior Java Developer
 * @version 1.0
 */
public class WaterDistributionOptimizer {

    /**
     * Data class to hold community and its demand info
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommunityDemand {
        private Community community;
        private Integer demand;
        private Integer minimumRequired;
        private Integer maximumCapacity;
        private Integer scarcityLevel;  // 1-5, higher = more urgent
        private Double populationRatio; // Population percentage
    }

    /**
     * Optimize water distribution among communities
     * 
     * @param totalAvailableWater Total water available for distribution
     * @param communities List of communities
     * @param demands Map of community ID to demand in liters
     * @param predictions Map of community ID to latest predictions
     * @return List of distribution allocations
     */
    public static List<DistributionAllocation> optimizeDistribution(
            Integer totalAvailableWater,
            List<Community> communities,
            Map<Integer, Integer> demands,
            Map<Integer, PredictionResult> predictions) {

        List<DistributionAllocation> allocations = new ArrayList<>();

        // Build community demand objects with all information
        List<CommunityDemand> communityDemands = buildCommunityDemands(
            communities, demands, predictions
        );

        // Sort by scarcity level (descending) and population ratio
        sortByPriority(communityDemands);

        // Phase 1: Allocate minimum required water
        Integer remainingWater = totalAvailableWater;
        Map<Integer, Integer> allocatedWater = new HashMap<>();

        for (CommunityDemand cd : communityDemands) {
            Integer minRequired = cd.getMinimumRequired();
            if (remainingWater >= minRequired) {
                allocatedWater.put(cd.getCommunity().getId(), minRequired);
                remainingWater -= minRequired;
            } else {
                // Not enough water to meet minimum - allocate what's available
                int allocated = Math.min(remainingWater, minRequired);
                allocatedWater.put(cd.getCommunity().getId(), allocated);
                remainingWater -= allocated;
            }
        }

        // Phase 2: Distribute remaining water to high-need communities
        while (remainingWater > 0) {
            boolean allocated = false;

            for (CommunityDemand cd : communityDemands) {
                Integer communityId = cd.getCommunity().getId();
                Integer currentAllocation = allocatedWater.getOrDefault(communityId, 0);
                Integer maxCapacity = cd.getMaximumCapacity();

                if (currentAllocation < maxCapacity && remainingWater > 0) {
                    // Calculate additional allocation (proportional to scarcity)
                    Integer additionalAllocation = Math.min(
                        cd.getDemand() / 5,  // Allocate demand/5 increments
                        Math.min(remainingWater, maxCapacity - currentAllocation)
                    );

                    if (additionalAllocation > 0) {
                        allocatedWater.put(communityId, currentAllocation + additionalAllocation);
                        remainingWater -= additionalAllocation;
                        allocated = true;
                    }
                }
            }

            if (!allocated) break;  // No more allocations possible
        }

        // Create allocation objects
        for (CommunityDemand cd : communityDemands) {
            Integer communityId = cd.getCommunity().getId();
            Integer allocatedAmount = allocatedWater.getOrDefault(communityId, 0);

            Double allocationPercentage = (allocatedAmount.doubleValue() / totalAvailableWater) * 100;
            Integer priorityLevel = cd.getScarcityLevel();

            String reason = buildAllocationReason(cd, allocatedAmount, cd.getDemand());

            DistributionAllocation allocation = DistributionAllocation.builder()
                .community(cd.getCommunity())
                .allocatedWater(allocatedAmount)
                .allocationPercentage(allocationPercentage)
                .priorityLevel(priorityLevel)
                .minimumRequired(cd.getMinimumRequired())
                .maximumCapacity(cd.getMaximumCapacity())
                .allocationReason(reason)
                .build();

            allocations.add(allocation);
        }

        return allocations;
    }

    /**
     * Build community demand information
     */
    private static List<CommunityDemand> buildCommunityDemands(
            List<Community> communities,
            Map<Integer, Integer> demands,
            Map<Integer, PredictionResult> predictions) {

        List<CommunityDemand> list = new ArrayList<>();

        for (Community community : communities) {
            Integer demand = demands.getOrDefault(community.getId(), 100000);  // Default 100K liters
            PredictionResult prediction = predictions.get(community.getId());

            // Determine scarcity level (1-5)
            Integer scarcityLevel = 2;  // Default
            if (prediction != null) {
                String level = prediction.getScarcityLevel();
                scarcityLevel = switch (level) {
                    case "CRITICAL" -> 5;
                    case "HIGH" -> 4;
                    case "MODERATE" -> 3;
                    default -> 1;
                };
            }

            // Set capacity constraints (75% to 125% of demand)
            Integer minRequired = (int) (demand * 0.75);
            Integer maxCapacity = (int) (demand * 1.25);

            CommunityDemand cd = new CommunityDemand(
                community,
                demand,
                minRequired,
                maxCapacity,
                scarcityLevel,
                calculatePopulationRatio(community)
            );

            list.add(cd);
        }

        return list;
    }

    /**
     * Sort by priority (scarcity level and population)
     */
    private static void sortByPriority(List<CommunityDemand> list) {
        list.sort((c1, c2) -> {
            // Primary: Scarcity level (descending)
            int scarcityCompare = c2.getScarcityLevel().compareTo(c1.getScarcityLevel());
            if (scarcityCompare != 0) return scarcityCompare;

            // Secondary: Population ratio (descending)
            return c2.getPopulationRatio().compareTo(c1.getPopulationRatio());
        });
    }

    /**
     * Calculate population ratio (approximate)
     */
    private static Double calculatePopulationRatio(Community community) {
        // Placeholder - in real scenario, get from PopulationData
        return 0.5;
    }

    /**
     * Build allocation reason string
     */
    private static String buildAllocationReason(CommunityDemand cd, Integer allocated, Integer demand) {
        StringBuilder sb = new StringBuilder();

        if (cd.getScarcityLevel() >= 4) {
            sb.append("High scarcity level: urgent water needed. ");
        } else if (cd.getScarcityLevel() == 3) {
            sb.append("Moderate scarcity: water demand is significant. ");
        }

        double percentageOfDemand = (allocated.doubleValue() / demand) * 100;
        sb.append(String.format("Allocated %.0f%% of demand. ", percentageOfDemand));

        if (percentageOfDemand < 75) {
            sb.append("WARNING: Below minimum recommended allocation.");
        }

        return sb.toString();
    }

    /**
     * Calculate distribution plan metrics
     */
    public static Map<String, Double> calculateMetrics(
            List<DistributionAllocation> allocations,
            Integer totalWater) {

        Map<String, Double> metrics = new HashMap<>();

        if (allocations.isEmpty() || totalWater == 0) {
            metrics.put("efficiency", 0.0);
            metrics.put("coverage", 0.0);
            metrics.put("waste", 0.0);
            return metrics;
        }

        // Efficiency: How many allocations met minimum requirement
        long validAllocations = allocations.stream()
            .filter(DistributionAllocation::meetsMinimumRequirement)
            .count();
        double efficiency = (validAllocations / (double) allocations.size()) * 100;

        // Coverage: Percentage of total water allocated
        Integer totalAllocated = allocations.stream()
            .mapToInt(DistributionAllocation::getAllocatedWater)
            .sum();
        double coverage = (totalAllocated / (double) totalWater) * 100;

        // Waste: Unallocated water
        double waste = (1 - coverage / 100) * 100;

        metrics.put("efficiency", Math.min(100.0, efficiency));
        metrics.put("coverage", Math.min(100.0, coverage));
        metrics.put("waste", Math.max(0.0, waste));

        return metrics;
    }
}
