package com.water.scarcity.model;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DistributionPlan Entity - Stores water distribution plan information
 * 
 * This entity represents a distribution plan that allocates water among
 * multiple communities with optimization metrics.
 * 
 * @author Senior Java Developer
 * @version 1.0
 */
@Entity
@Table(name = "distribution_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistributionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "plan_name", nullable = false, length = 100)
    private String planName;

    @Column(name = "plan_description", columnDefinition = "TEXT")
    private String planDescription;

    // ========== RESOURCE INFORMATION ==========
    @Column(name = "total_available_water", nullable = false)
    private Integer totalAvailableWater;  // in liters

    @Column(name = "distribution_date", nullable = false)
    private LocalDate distributionDate;

    @Column(name = "implementation_date")
    private LocalDate implementationDate;

    // ========== PLAN STATUS ==========
    @Column(name = "status", length = 20)
    private String status;  // DRAFT, APPROVED, IN_PROGRESS, COMPLETED

    // ========== OPTIMIZATION METRICS ==========
    @Column(name = "efficiency_score", precision = 5, scale = 2)
    private Double efficiencyScore;  // 0-100

    @Column(name = "coverage_percentage", precision = 5, scale = 2)
    private Double coveragePercentage;  // 0-100

    @Column(name = "waste_percentage", precision = 5, scale = 2)
    private Double wastePercentage;  // 0-100

    // ========== TIMELINE ==========
    @Column(name = "duration_days")
    private Integer durationDays;

    // ========== TIMESTAMPS ==========
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if plan is approved and ready to implement
     * @return true if plan status is APPROVED
     */
    public Boolean isApproved() {
        return "APPROVED".equals(status);
    }

    /**
     * Get plan quality score (combination of metrics)
     * @return Quality score from 0 to 100
     */
    public Double getQualityScore() {
        double efficiency = efficiencyScore != null ? efficiencyScore : 0;
        double coverage = coveragePercentage != null ? coveragePercentage : 0;
        double quality = 100 - (wastePercentage != null ? wastePercentage : 0);
        
        return (efficiency + coverage + quality) / 3;
    }
}
