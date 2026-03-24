package com.water.scarcity.controller;

import com.water.scarcity.model.Community;
import com.water.scarcity.service.CommunityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CommunityController - REST API endpoints for Community management
 * 
 * Base URL: /api/communities
 * 
 * Endpoints:
 * - GET    /api/communities              - Get all active communities
 * - GET    /api/communities/{id}         - Get community by ID
 * - GET    /api/communities/name/{name}  - Get community by name
 * - GET    /api/communities/region/{region} - Get communities in region
 * - POST   /api/communities              - Create new community
 * - PUT    /api/communities/{id}         - Update community
 * - DELETE /api/communities/{id}         - Deactivate community
 * 
 * @author Senior Java Developer
 * @version 1.0
 */
@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
@Slf4j
public class CommunityController {

    private final CommunityService communityService;

    /**
     * Get all active communities
     * @return List of active communities
     */
    @GetMapping
    public ResponseEntity<List<Community>> getAllCommunities() {
        log.info("GET /api/communities - Fetching all active communities");
        List<Community> communities = communityService.getAllActiveCommunities();
        return ResponseEntity.ok(communities);
    }

    /**
     * Get community by ID
     * @param id Community ID
     * @return Community details
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCommunityById(@PathVariable Integer id) {
        log.info("GET /api/communities/{} - Fetching community", id);
        return communityService.getCommunityById(id)
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Community not found with ID: " + id)));
    }

    /**
     * Get community by name
     * @param name Community name
     * @return Community details
     */
    @GetMapping("/search/{name}")
    public ResponseEntity<?> getCommunityByName(@PathVariable String name) {
        log.info("GET /api/communities/search/{} - Searching community by name", name);
        return communityService.getCommunityByName(name)
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Community not found with name: " + name)));
    }

    /**
     * Get communities in a region
     * @param region Region name
     * @return List of communities in region
     */
    @GetMapping("/region/{region}")
    public ResponseEntity<List<Community>> getCommununitiesByRegion(@PathVariable String region) {
        log.info("GET /api/communities/region/{} - Fetching communities", region);
        List<Community> communities = communityService.getCommununitiesByRegion(region);
        return ResponseEntity.ok(communities);
    }

    /**
     * Create new community
     * @param community Community object to create
     * @return Created community with ID
     */
    @PostMapping
    public ResponseEntity<Community> createCommunity(@RequestBody Community community) {
        log.info("POST /api/communities - Creating community: {}", community.getName());
        Community created = communityService.createCommunity(community);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update community
     * @param id Community ID
     * @param community Updated community object
     * @return Updated community
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCommunity(@PathVariable Integer id, @RequestBody Community community) {
        log.info("PUT /api/communities/{} - Updating community", id);
        try {
            Community updated = communityService.updateCommunity(id, community);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Deactivate community
     * @param id Community ID
     * @return Success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deactivateCommunity(@PathVariable Integer id) {
        log.info("DELETE /api/communities/{} - Deactivating community", id);
        try {
            communityService.deactivateCommunity(id);
            return ResponseEntity.ok(Map.of("message", "Community deactivated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get statistics about communities
     * @return Community statistics
     */
    @GetMapping("/stats/overview")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/communities/stats/overview - Getting community statistics");
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalActiveCommunities", communityService.getTotalActiveCommunityCount());
        stats.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(stats);
    }
}
