package com.water.scarcity.service;

import com.water.scarcity.model.Community;
import com.water.scarcity.model.WaterData;
import com.water.scarcity.repository.CommunityRepository;
import com.water.scarcity.repository.WaterDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * WaterDataService - Business logic for Water Data management
 * 
 * Handles all operations related to water usage, supply, and quality data.
 * Includes calculations for water balance, deficit analysis, and trends.
 * 
 * @author Senior Java Developer
 * @version 1.0
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class WaterDataService {

    private final WaterDataRepository waterDataRepository;
    private final CommunityRepository communityRepository;

    /**
     * Add new water data record
     * @param waterData Water data to save
     * @return Saved water data with ID
     */
    public WaterData addWaterData(WaterData waterData) {
        log.info("Adding water data for community: {}, date: {}", 
                 waterData.getCommunity().getId(), waterData.getRecordedDate());
        
        // Validate that community exists
        Community community = waterData.getCommunity();
        if (community.getId() != null) {
            communityRepository.findById(community.getId())
                .orElseThrow(() -> new RuntimeException("Community not found"));
        }
        
        WaterData saved = waterDataRepository.save(waterData);
        log.info("Water data saved with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Get latest water data for a community
     * @param communityId Community ID
     * @return Latest water data record
     */
    public Optional<WaterData> getLatestWaterData(Integer communityId) {
        log.debug("Fetching latest water data for community: {}", communityId);
        return waterDataRepository.findLatestByCommunityId(communityId);
    }

    /**
     * Get water data history for a community
     * @param communityId Community ID
     * @param days Number of past days to retrieve
     * @return List of water data records
     */
    public List<WaterData> getWaterDataHistory(Integer communityId, int days) {
        log.debug("Fetching water data history for community: {}, days: {}", communityId, days);
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);
        return waterDataRepository.findByCommunityIdAndRecordedDateBetweenOrderByRecordedDateDesc(
            communityId, startDate, endDate);
    }

    /**
     * Get water data for specific date
     * @param communityId Community ID
     * @param date Recording date
     * @return Water data if found
     */
    public Optional<WaterData> getWaterDataByDate(Integer communityId, LocalDate date) {
        log.debug("Fetching water data for community: {}, date: {}", communityId, date);
        return waterDataRepository.findByCommunityIdAndRecordedDate(communityId, date);
    }

    /**
     * Calculate average water balance for a community
     * @param communityId Community ID
     * @return Average balance value in liters
     */
    public Double getAverageWaterBalance(Integer communityId) {
        log.debug("Calculating average water balance for community: {}", communityId);
        Double balance = waterDataRepository.getAverageWaterBalanceByCommunity(communityId);
        return balance != null ? balance : 0.0;
    }

    /**
     * Get total rainfall in a period
     * @param communityId Community ID
     * @param days Number of past days
     * @return Total rainfall in mm
     */
    public Double getTotalRainfallPeriod(Integer communityId, int days) {
        log.debug("Calculating total rainfall for community: {}, days: {}", communityId, days);
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);
        Double rainfall = waterDataRepository.getTotalRainfallByCommunity(communityId, startDate, endDate);
        return rainfall != null ? rainfall : 0.0;
    }

    /**
     * Check if community has water deficit (usage > supply)
     * @param communityId Community ID
     * @return true if deficit exists
     */
    public Boolean hasWaterDeficit(Integer communityId) {
        log.debug("Checking water deficit for community: {}", communityId);
        Optional<WaterData> latestData = getLatestWaterData(communityId);
        
        if (latestData.isEmpty()) {
            return false;
        }
        
        WaterData data = latestData.get();
        return data.getWaterBalance() < 0;
    }

    /**
     * Get deficit percentage for community
     * @param communityId Community ID
     * @return Deficit percentage
     */
    public Double getDeficitPercentage(Integer communityId) {
        log.debug("Calculating deficit percentage for community: {}", communityId);
        Optional<WaterData> latestData = getLatestWaterData(communityId);
        
        if (latestData.isEmpty()) {
            return 0.0;
        }
        
        return latestData.get().getDeficitPercentage();
    }

    /**
     * Get all communities with water deficit
     * @return List of communities with deficit
     */
    public List<Community> getCommunitiesWithDeficit() {
        log.debug("Fetching all communities with water deficit");
        List<Community> activeCommunities = communityRepository.findByIsActiveTrue();
        
        return activeCommunities.stream()
            .filter(community -> hasWaterDeficit(community.getId()))
            .toList();
    }

    /**
     * Get water data for all communities on a specific date
     * @param date Date to query
     * @return List of water data records
     */
    public List<WaterData> getWaterDataByDate(LocalDate date) {
        log.debug("Fetching water data for all communities on date: {}", date);
        return waterDataRepository.findByRecordedDate(date);
    }

    /**
     * Calculate water quality score trend
     * @param communityId Community ID
     * @param days Number of past days
     * @return List of quality scores (empty list if insufficient data)
     */
    public List<Double> getWaterQualityTrend(Integer communityId, int days) {
        log.debug("Calculating water quality trend for community: {}, days: {}", communityId, days);
        return getWaterDataHistory(communityId, days).stream()
            .filter(data -> data.getWaterQualityScore() != null)
            .map(WaterData::getWaterQualityScore)
            .toList();
    }
}
