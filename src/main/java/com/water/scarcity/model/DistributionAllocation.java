package com.water.scarcity.model;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * DistributionAllocation Entity - Stores water allocation per community in a plan
 * 
 * This entity represents how much water is allocated to each community
 * within a distribution plan.
 * 
 * @author Senior Java Developer
 * @version 1.0
 */
@Entity
@Table(name = "distribution_allocations", indexes = {
    @Index(name = "idx_allocation_plan_community", columnList = "plan_id, community_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistributionAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private DistributionPlan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    // ========== ALLOCATION DETAILS ==========
    @Column(name = "allocated_water", nullable = false)
    private Integer allocatedWater;  // in liters

    @Column(name = "allocation_percentage", precision = 5, scale = 2)
    private Double allocationPercentage;

    // ========== PRIORITY AND CONSTRAINTS ==========
    @Column(name = "priority_level")
    private Integer priorityLevel;  // 1 (lowest) to 5 (highest)

    @Column(name = "minimum_required")
    private Integer minimumRequired;  // in liters

    @Column(name = "maximum_capacity")
    private Integer maximumCapacity;  // in liters

    // ========== JUSTIFICATION ==========
    @Column(name = "allocation_reason", columnDefinition = "TEXT")
    private String allocationReason;

    // ========== TIMESTAMPS ==========
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Check if allocation meets minimum requirement
     * @return true if allocated water >= minimum required
     */
    public Boolean meetsMinimumRequirement() {
        if (minimumRequired == null) {
            return true;
        }
        return allocatedWater >= minimumRequired;
    }

    /**
     * Check if allocation exceeds maximum capacity
     * @return true if allocated water <= maximum capacity
     */
    public Boolean withinMaximumCapacity() {
        if (maximumCapacity == null) {
            return true;
        }
        return allocatedWater <= maximumCapacity;
    }

    /**
     * Get allocation status
     * @return Status message about the allocation
     */
    public String getAllocationStatus() {
        if (!meetsMinimumRequirement()) {
            return "BELOW_MINIMUM";
        }
        if (!withinMaximumCapacity()) {
            return "EXCEEDS_MAXIMUM";
        }
        if (priorityLevel != null && priorityLevel >= 4) {
            return "HIGH_PRIORITY";
        }
        return "OPTIMAL";
    }
}
