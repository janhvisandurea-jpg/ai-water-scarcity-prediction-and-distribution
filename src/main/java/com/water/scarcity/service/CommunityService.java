package com.water.scarcity.service;

import com.water.scarcity.model.Community;
import com.water.scarcity.repository.CommunityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * CommunityService - Business logic for Community management
 * 
 * Handles all operations related to communities including CRUD operations,
 * queries, and community-related data processing.
 * 
 * @author Senior Java Developer
 * @version 1.0
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CommunityService {

    private final CommunityRepository communityRepository;

    /**
     * Get all active communities
     * @return List of active communities
     */
    public List<Community> getAllActiveCommunities() {
        log.debug("Fetching all active communities");
        return communityRepository.findByIsActiveTrue();
    }

    /**
     * Get community by ID
     * @param id Community ID
     * @return Optional containing community if found
     */
    public Optional<Community> getCommunityById(Integer id) {
        log.debug("Fetching community with ID: {}", id);
        return communityRepository.findById(id);
    }

    /**
     * Get community by name
     * @param name Community name
     * @return Optional containing community if found
     */
    public Optional<Community> getCommunityByName(String name) {
        log.debug("Fetching community with name: {}", name);
        return communityRepository.findByName(name);
    }

    /**
     * Get all communities in a region
     * @param region Region name
     * @return List of communities in that region
     */
    public List<Community> getCommununitiesByRegion(String region) {
        log.debug("Fetching communities in region: {}", region);
        return communityRepository.findByRegion(region);
    }

    /**
     * Create a new community
     * @param community Community object to create
     * @return Created community with ID
     */
    public Community createCommunity(Community community) {
        log.info("Creating new community: {}", community.getName());
        Community saved = communityRepository.save(community);
        log.info("Community created with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Update existing community
     * @param id Community ID
     * @param community Updated community object
     * @return Updated community
     */
    public Community updateCommunity(Integer id, Community community) {
        log.info("Updating community with ID: {}", id);
        Community existing = communityRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Community not found with ID: " + id));
        
        existing.setName(community.getName());
        existing.setRegion(community.getRegion());
        existing.setLatitude(community.getLatitude());
        existing.setLongitude(community.getLongitude());
        existing.setElevation(community.getElevation());
        existing.setIsActive(community.getIsActive());
        
        Community updated = communityRepository.save(existing);
        log.info("Community updated successfully");
        return updated;
    }

    /**
     * Delete community (soft delete - mark as inactive)
     * @param id Community ID
     */
    public void deactivateCommunity(Integer id) {
        log.info("Deactivating community with ID: {}", id);
        Community community = communityRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Community not found with ID: " + id));
        
        community.setIsActive(false);
        communityRepository.save(community);
        log.info("Community deactivated");
    }

    /**
     * Get community count in a region
     * @param region Region name
     * @return Count of active communities
     */
    public Integer getCommunityCountInRegion(String region) {
        log.debug("Getting community count in region: {}", region);
        return communityRepository.countByRegionAndIsActiveTrue(region);
    }

    /**
     * Get communities with highest elevation
     * @param limit Number of communities to return
     * @return List of communities
     */
    public List<Community> getHighestElevationCommunities(int limit) {
        log.debug("Fetching {} highest elevation communities", limit);
        return communityRepository.findTopByElevation(limit);
    }

    /**
     * Health check - count total communities
     * @return Total count of active communities
     */
    public Long getTotalActiveCommunityCount() {
        log.debug("Counting total active communities");
        return communityRepository.findByIsActiveTrue().stream().count();
    }
}
