package com.water.scarcity.service;

import com.water.scarcity.model.PopulationData;
import com.water.scarcity.repository.PopulationDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * PopulationDataService - Business logic for Population Data management
 * 
 * Handles demographic data, growth rates, and population-based water analysis.
 * 
 * @author Senior Java Developer
 * @version 1.0
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PopulationDataService {

    private final PopulationDataRepository populationDataRepository;

    /**
     * Add population data
     * @param populationData Population data to save
     * @return Saved population data
     */
    public PopulationData addPopulationData(PopulationData populationData) {
        log.info("Adding population data for community: {}, date: {}", 
                 populationData.getCommunity().getId(), populationData.getRecordedDate());
        return populationDataRepository.save(populationData);
    }

    /**
     * Get latest population for a community
     * @param communityId Community ID
     * @return Latest population data
     */
    public Optional<PopulationData> getLatestPopulationData(Integer communityId) {
        log.debug("Fetching latest population data for community: {}", communityId);
        return populationDataRepository.findLatestByCommunityId(communityId);
    }

    /**
     * Get estimated water demand for a community
     * @param communityId Community ID
     * @return Estimated daily water demand in liters
     */
    public Integer getEstimatedWaterDemand(Integer communityId) {
        log.debug("Calculating estimated water demand for community: {}", communityId);
        Optional<PopulationData> latestData = getLatestPopulationData(communityId);
        
        if (latestData.isEmpty()) {
            return 0;
        }
        
        return latestData.get().calculateWaterDemand();
    }

    /**
     * Get urbanization rate
     * @param communityId Community ID
     * @return Urbanization percentage
     */
    public Double getUrbanizationRate(Integer communityId) {
        log.debug("Getting urbanization rate for community: {}", communityId);
        Optional<PopulationData> latestData = getLatestPopulationData(communityId);
        
        if (latestData.isEmpty()) {
            return 0.0;
        }
        
        return latestData.get().getUrbanizationPercentage();
    }

    /**
     * Get average per capita water usage
     * @param communityId Community ID
     * @return Average usage in liters per person per day
     */
    public Integer getAveragePerCapitaUsage(Integer communityId) {
        log.debug("Getting average per capita usage for community: {}", communityId);
        Integer usage = populationDataRepository.getAveragePerCapitaUsage(communityId);
        return usage != null ? usage : 0;
    }

    /**
     * Get population growth trend
     * @param communityId Community ID
     * @param months Number of past months to analyze
     * @return List of population values in chronological order
     */
    public List<Integer> getPopulationTrend(Integer communityId, int months) {
        log.debug("Getting population trend for community: {}, months: {}", communityId, months);
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months);
        
        return populationDataRepository.findByCommunityIdAndRecordedDateBetweenOrderByRecordedDateAsc(
            communityId, startDate, endDate).stream()
            .map(PopulationData::getTotalPopulation)
            .toList();
    }

    /**
     * Get high poverty communities
     * @param povertyThreshold Poverty rate threshold (0-100)
     * @return List of population data for high-poverty communities
     */
    public List<PopulationData> getHighPovertyCommuntities(Double povertyThreshold) {
        log.debug("Fetching high poverty communities with threshold: {}", povertyThreshold);
        return populationDataRepository.findHighPovertyCommunitiesLatest(povertyThreshold);
    }

    /**
     * Get population data for a specific date
     * @param communityId Community ID
     * @param date Date
     * @return Population data if found
     */
    public Optional<PopulationData> getPopulationDataByDate(Integer communityId, LocalDate date) {
        log.debug("Fetching population data for community: {}, date: {}", communityId, date);
        return populationDataRepository.findByCommunityIdAndRecordedDate(communityId, date);
    }

    /**
     * Get average growth rate across all communities
     * @return Average annual growth rate
     */
    public Double getAverageGrowthRate() {
        log.debug("Calculating average growth rate");
        Double rate = populationDataRepository.getAverageGrowthRate();
        return rate != null ? rate : 0.0;
    }

    /**
     * Project future population
     * @param communityId Community ID
     * @param yearsAhead Number of years to project
     * @return Projected population
     */
    public Integer projectPopulation(Integer communityId, int yearsAhead) {
        log.debug("Projecting population for community: {}, years: {}", communityId, yearsAhead);
        
        Optional<PopulationData> latestData = getLatestPopulationData(communityId);
        if (latestData.isEmpty()) {
            return 0;
        }
        
        PopulationData data = latestData.get();
        if (data.getGrowthRate() == null) {
            return data.getTotalPopulation();
        }
        
        // Simple compound growth formula: P_future = P_current * (1 + growth_rate)^years
        Double growthRate = data.getGrowthRate() / 100.0;
        Integer future = (int) (data.getTotalPopulation() * Math.pow(1 + growthRate, yearsAhead));
        
        return future;
    }
}
