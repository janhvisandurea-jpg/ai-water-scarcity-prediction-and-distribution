package com.water.scarcity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot Application class for Water Scarcity Prediction Optimizer
 * 
 * This application predicts water scarcity using machine learning and optimizes
 * water distribution among communities based on population, rainfall, and usage data.
 * 
 * @author Senior Java Developer
 * @version 1.0
 * @since March 2026
 */
@SpringBootApplication
@EnableScheduling  // Enable scheduling for periodic tasks
public class WaterApplication {

    /**
     * Main method to start the Spring Boot application
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(WaterApplication.class, args);
        System.out.println("===========================================");
        System.out.println("Water Scarcity Prediction Optimizer Started");
        System.out.println("===========================================");
        System.out.println("Available Endpoints:");
        System.out.println("- POST   /api/water-data        -> Add water usage data");
        System.out.println("- POST   /api/population-data   -> Add population data");
        System.out.println("- GET    /api/predictions       -> Get scarcity predictions");
        System.out.println("- GET    /api/distribution      -> Get distribution plan");
        System.out.println("- GET    /api/dashboard         -> Get dashboard metrics");
        System.out.println("- GET    /api/history/{communityId} -> Get history");
        System.out.println("- DELETE /api/data/{id}         -> Delete data record");
        System.out.println("===========================================");
    }
}
