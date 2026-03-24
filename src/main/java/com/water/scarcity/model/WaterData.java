package com.water.scarcity.model;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * WaterData Entity - Stores water usage, rainfall, and supply information
 * 
 * This entity captures daily water data including rainfall, groundwater supply,
 * surface water supply, recycled water, and various consumption metrics.
 * 
 * @author Senior Java Developer
 * @version 1.0
 */
@Entity
@Table(name = "water_data", indexes = {
    @Index(name = "idx_water_data_community_date", columnList = "community_id, recorded_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaterData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    // ========== WATER SUPPLY METRICS ==========
    @Column(name = "rainfall_mm", precision = 10, scale = 2)
    private Double rainfallMm;  // Rainfall in millimeters

    @Column(name = "groundwater_supply")
    private Integer groundwaterSupply;  // in liters

    @Column(name = "surface_water_supply")
    private Integer surfaceWaterSupply;  // in liters

    @Column(name = "recycled_water_supply")
    private Integer recycledWaterSupply;  // in liters

    @Column(name = "total_water_supply")
    private Integer totalWaterSupply;  // Total available water in liters

    // ========== WATER USAGE METRICS ==========
    @Column(name = "residential_usage")
    private Integer residentialUsage;  // in liters

    @Column(name = "agricultural_usage")
    private Integer agriculturalUsage;  // in liters

    @Column(name = "industrial_usage")
    private Integer industrialUsage;  // in liters

    @Column(name = "total_water_usage")
    private Integer totalWaterUsage;  // Total water used in liters

    // ========== QUALITY METRICS ==========
    @Column(name = "water_quality_score", precision = 3, scale = 2)
    private Double waterQualityScore;  // 0 to 10 scale

    // ========== TIMESTAMP FIELDS ==========
    @Column(name = "recorded_date", nullable = false)
    private LocalDate recordedDate;  // Date of data recording

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Calculate water balance (supply - usage)
     * @return Water balance in liters (positive = surplus, negative = deficit)
     */
    public Integer getWaterBalance() {
        if (totalWaterSupply == null || totalWaterUsage == null) {
            return 0;
        }
        return totalWaterSupply - totalWaterUsage;
    }

    /**
     * Calculate water deficit percentage
     * @return Percentage of deficit (0 = no deficit, >0 = deficit exists)
     */
    public Double getDeficitPercentage() {
        if (totalWaterUsage == null || totalWaterUsage == 0) {
            return 0.0;
        }
        int balance = getWaterBalance();
        if (balance < 0) {
            return (Math.abs(balance) * 100.0) / totalWaterUsage;
        }
        return 0.0;
    }
}
