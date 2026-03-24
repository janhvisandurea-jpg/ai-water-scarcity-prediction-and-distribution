package com.water.scarcity.model;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "population_data", indexes = {
    @Index(name = "idx_population_data_community_date", columnList = "community_id, recorded_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PopulationData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    @Column(name = "total_population", nullable = false)
    private Integer totalPopulation;

    @Column(name = "urban_population")
    private Integer urbanPopulation;

    @Column(name = "rural_population")
    private Integer ruralPopulation;

    @Column(name = "average_household_size", precision = 3, scale = 2)
    private Double averageHouseholdSize;

    @Column(name = "growth_rate", precision = 5, scale = 2)
    private Double growthRate;

    @Column(name = "avg_per_capita_usage")
    private Integer avgPerCapitaUsage;

    @Column(name = "poverty_rate", precision = 5, scale = 2)
    private Double povertyRate;

    @Column(name = "recorded_date", nullable = false)
    private LocalDate recordedDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Integer calculateWaterDemand() {
        if (totalPopulation == null || avgPerCapitaUsage == null) {
            return 0;
        }
        return totalPopulation * avgPerCapitaUsage;
    }

    public Double getUrbanizationPercentage() {
        if (totalPopulation == null || totalPopulation == 0) {
            return 0.0;
        }
        if (urbanPopulation == null) {
            return 0.0;
        }
        return (urbanPopulation * 100.0) / totalPopulation;
    }

    public Boolean validatePopulationData() {
        if (urbanPopulation == null || ruralPopulation == null) {
            return true;
        }
        return (urbanPopulation + ruralPopulation) == totalPopulation;
    }
}