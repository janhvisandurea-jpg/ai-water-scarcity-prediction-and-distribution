package com.water.scarcity.model;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Community Entity - Represents a community/region in the system
 * 
 * This entity stores information about communities that need water distribution.
 * Each community is associated with water usage, population, and predictions.
 * 
 * @author Senior Java Developer
 * @version 1.0
 */
@Entity
@Table(name = "communities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "waterDataList")
@ToString(exclude = "waterDataList")
public class Community {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 100)
    private String region;

    @Column(name = "latitude", precision = 10, scale = 8)
    private Double latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private Double longitude;

    @Column(name = "elevation")
    private Integer elevation;  // in meters

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Pre-persist method to set creation timestamp
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Pre-update method to update modification timestamp
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
