package com.water.scarcity.repository;

import com.water.scarcity.model.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Community entity
 * Handles database operations for Community data
 * 
 * @author Senior Java Developer
 * @version 1.0
 */
@Repository
public interface CommunityRepository extends JpaRepository<Community, Integer> {

    /**
     * Find community by name
     * @param name Community name
     * @return Optional containing community if found
     */
    Optional<Community> findByName(String name);

    /**
     * Find all active communities
     * @return List of active communities
     */
    List<Community> findByIsActiveTrue();

    /**
     * Find communities by region
     * @param region Region name
     * @return List of communities in that region
     */
    List<Community> findByRegion(String region);

    /**
     * Find communities by region and active status
     * @param region Region name
     * @param isActive Active status
     * @return List of matching communities
     */
    List<Community> findByRegionAndIsActive(String region, Boolean isActive);

    /**
     * Custom query to find communities with highest elevation
     * @param limit Number of communities to return
     * @return List of communities sorted by elevation (descending)
     */
    @Query(value = "SELECT * FROM communities WHERE is_active = true ORDER BY elevation DESC LIMIT :limit", 
           nativeQuery = true)
    List<Community> findTopByElevation(@Param("limit") int limit);

    /**
     * Count active communities in a region
     * @param region Region name
     * @return Count of active communities
     */
    Integer countByRegionAndIsActiveTrue(String region);
}
