-- ==============================================================================
-- WATER SCARCITY PREDICTION OPTIMIZER - DATABASE SCHEMA
-- ==============================================================================
-- This schema contains tables for water data, population, predictions, and distribution
-- Created for: Water Scarcity Prediction & Distribution Optimizer
-- ==============================================================================

-- Create Database
CREATE DATABASE IF NOT EXISTS water_scarcity_db;
USE water_scarcity_db;

-- ==============================================================================
-- 1. COMMUNITIES TABLE - Stores community information
-- ==============================================================================
CREATE TABLE IF NOT EXISTS communities (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    region VARCHAR(100) NOT NULL,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    elevation INT,  -- Elevation in meters (affects rainfall)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- 2. WATER DATA TABLE - Stores water usage, rainfall, and supply information
-- ==============================================================================
CREATE TABLE IF NOT EXISTS water_data (
    id INT PRIMARY KEY AUTO_INCREMENT,
    community_id INT NOT NULL,
    
    -- Water Supply (in liters/day)
    rainfall_mm DECIMAL(10, 2),         -- Rainfall in millimeters
    groundwater_supply INT,             -- Groundwater supply in liters
    surface_water_supply INT,           -- Surface water supply in liters
    recycled_water_supply INT,          -- Recycled water supply in liters
    total_water_supply INT,             -- Total available water in liters
    
    -- Water Usage (in liters/day)
    residential_usage INT,              -- Residential consumption
    agricultural_usage INT,             -- Agricultural consumption
    industrial_usage INT,               -- Industrial consumption
    total_water_usage INT,              -- Total water usage
    
    -- Additional Metrics
    water_quality_score DECIMAL(3, 2),  -- 0 to 10 scale
    recorded_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create index for better query performance
CREATE INDEX idx_water_data_community_date ON water_data(community_id, recorded_date DESC);

-- ==============================================================================
-- 3. POPULATION DATA TABLE - Stores population information by time period
-- ==============================================================================
CREATE TABLE IF NOT EXISTS population_data (
    id INT PRIMARY KEY AUTO_INCREMENT,
    community_id INT NOT NULL,
    
    -- Population Metrics
    total_population INT NOT NULL,      -- Total population count
    urban_population INT,               -- Urban population
    rural_population INT,               -- Rural population
    average_household_size DECIMAL(3, 2),
    
    -- Population Growth
    growth_rate DECIMAL(5, 2),          -- Annual growth rate in percentage
    
    -- Water Consumption Metrics
    avg_per_capita_usage INT,           -- Average water usage per person (liters/day)
    poverty_rate DECIMAL(5, 2),         -- Percentage of population below poverty line
    
    -- Additional Info
    recorded_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create index for better query performance
CREATE INDEX idx_population_data_community_date ON population_data(community_id, recorded_date DESC);

-- ==============================================================================
-- 4. PREDICTION RESULTS TABLE - Stores ML model predictions
-- ==============================================================================
CREATE TABLE IF NOT EXISTS prediction_results (
    id INT PRIMARY KEY AUTO_INCREMENT,
    community_id INT NOT NULL,
    
    -- Scarcity Prediction
    scarcity_level VARCHAR(20) NOT NULL,  -- LOW, MODERATE, HIGH, CRITICAL
    scarcity_percentage DECIMAL(5, 2),    -- Predicted scarcity percentage (0-100)
    confidence DECIMAL(5, 2),             -- Model confidence (0-100)
    
    -- Prediction Details
    predicted_water_deficit INT,          -- Predicted water shortage in liters
    predicted_demand INT,                 -- Predicted water demand
    predicted_supply INT,                 -- Predicted water supply
    
    -- Timeline
    prediction_date DATE NOT NULL,        -- Date of prediction
    prediction_horizon INT,               -- Prediction period (days ahead)
    data_points_used INT,                 -- Number of data points used in prediction
    
    -- Model Performance
    model_accuracy DECIMAL(5, 2),         -- Model accuracy percentage
    model_version VARCHAR(20),            -- Version of ML model used
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create index for better query performance
CREATE INDEX idx_prediction_community_date ON prediction_results(community_id, prediction_date DESC);

-- ==============================================================================
-- 5. WATER DISTRIBUTION PLAN TABLE - Stores optimal distribution recommendations
-- ==============================================================================
CREATE TABLE IF NOT EXISTS distribution_plans (
    id INT PRIMARY KEY AUTO_INCREMENT,
    
    -- Distribution Details
    plan_name VARCHAR(100) NOT NULL,
    plan_description TEXT,
    
    -- Resource Information
    total_available_water INT NOT NULL,    -- Total water available for distribution
    distribution_date DATE NOT NULL,       -- Date of distribution plan
    implementation_date DATE,              -- When to start implementing the plan
    
    -- Plan Status
    status VARCHAR(20) DEFAULT 'DRAFT',    -- DRAFT, APPROVED, IN_PROGRESS, COMPLETED
    
    -- Optimization metrics
    efficiency_score DECIMAL(5, 2),        -- How efficient is the distribution (0-100)
    coverage_percentage DECIMAL(5, 2),     -- Percentage of population covered
    waste_percentage DECIMAL(5, 2),        -- Percentage of water wasted
    
    -- Timeline
    duration_days INT,                     -- How many days the plan covers
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- 6. DISTRIBUTION ALLOCATION TABLE - Stores water allocation per community in a plan
-- ==============================================================================
CREATE TABLE IF NOT EXISTS distribution_allocations (
    id INT PRIMARY KEY AUTO_INCREMENT,
    plan_id INT NOT NULL,
    community_id INT NOT NULL,
    
    -- Allocation Details
    allocated_water INT NOT NULL,         -- Water allocated to this community (liters)
    allocation_percentage DECIMAL(5, 2),  -- Percentage of total water
    
    -- Priority and Constraints
    priority_level INT,                   -- 1 (lowest) to 5 (highest)
    minimum_required INT,                 -- Minimum water needed
    maximum_capacity INT,                 -- Maximum water can be stored/used
    
    -- Justification
    allocation_reason TEXT,               -- Why this community got this amount
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (plan_id) REFERENCES distribution_plans(id) ON DELETE CASCADE,
    FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create index for better query performance
CREATE INDEX idx_allocation_plan_community ON distribution_allocations(plan_id, community_id);

-- ==============================================================================
-- 7. SCARCITY ALERTS TABLE - Stores alerts generated based on predictions
-- ==============================================================================
CREATE TABLE IF NOT EXISTS scarcity_alerts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    community_id INT NOT NULL,
    prediction_id INT,
    
    -- Alert Details
    alert_type VARCHAR(30) NOT NULL,      -- WARNING, CRITICAL, EMERGENCY
    alert_level INT,                      -- 1 (lowest) to 5 (highest)
    message TEXT NOT NULL,
    
    -- Alert Status
    is_acknowledged BOOLEAN DEFAULT FALSE,
    acknowledged_by VARCHAR(100),
    acknowledged_at TIMESTAMP NULL,
    
    -- Timeline
    alert_triggered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE CASCADE,
    FOREIGN KEY (prediction_id) REFERENCES prediction_results(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create index for alert queries
CREATE INDEX idx_alert_community_type ON scarcity_alerts(community_id, alert_type);

-- ==============================================================================
-- 8. SYSTEM LOGS TABLE - Stores application logs for auditing and debugging
-- ==============================================================================
CREATE TABLE IF NOT EXISTS system_logs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    
    -- Log Details
    log_level VARCHAR(20),                -- INFO, WARNING, ERROR, DEBUG
    logger_name VARCHAR(255),
    message TEXT,
    exception_trace TEXT,
    
    -- Context
    user_action VARCHAR(100),
    affected_entity VARCHAR(100),
    entity_id INT,
    
    -- Timestamp
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create index for log queries
CREATE INDEX idx_logs_timestamp ON system_logs(created_at DESC);
CREATE INDEX idx_logs_level ON system_logs(log_level);

-- ==============================================================================
-- 9. API USAGE STATISTICS TABLE - Tracks API usage for monitoring
-- ==============================================================================
CREATE TABLE IF NOT EXISTS api_statistics (
    id INT PRIMARY KEY AUTO_INCREMENT,
    endpoint VARCHAR(100) NOT NULL,
    method VARCHAR(10) NOT NULL,
    
    -- Usage Stats
    total_calls INT DEFAULT 0,
    total_errors INT DEFAULT 0,
    avg_response_time_ms INT,
    
    -- Timestamp
    last_called TIMESTAMP,
    date DATE NOT NULL,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_endpoint_date (endpoint, method, date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- INSERT SAMPLE DATA FOR DEMONSTRATION
-- ==============================================================================

-- Add sample communities
INSERT INTO communities (name, region, latitude, longitude, elevation) VALUES
('North Plains', 'Northern Region', 40.7128, -74.0060, 150),
('South Valley', 'Southern Region', 34.0522, -118.2437, 100),
('Eastern Hills', 'Eastern Region', 42.3601, -71.0589, 200),
('Western Desert', 'Western Region', 47.6062, -122.3321, 180);

-- ==============================================================================
-- CREATE VIEWS FOR COMMON QUERIES
-- ==============================================================================

-- View: Current Water Balance by Community
CREATE OR REPLACE VIEW vw_water_balance AS
SELECT 
    c.id,
    c.name,
    c.region,
    w.recorded_date,
    w.total_water_supply,
    w.total_water_usage,
    (w.total_water_supply - w.total_water_usage) AS water_balance,
    CASE 
        WHEN (w.total_water_supply - w.total_water_usage) < 0 THEN 'DEFICIT'
        WHEN (w.total_water_supply - w.total_water_usage) < 100000 THEN 'LOW'
        ELSE 'SURPLUS'
    END AS balance_status
FROM communities c
LEFT JOIN water_data w ON c.id = w.community_id
WHERE w.recorded_date = (
    SELECT MAX(recorded_date) FROM water_data WHERE community_id = c.id
);

-- View: Population and Water Usage Trends
CREATE OR REPLACE VIEW vw_population_water_trends AS
SELECT 
    c.name,
    p.recorded_date,
    p.total_population,
    w.total_water_usage,
    ROUND(w.total_water_usage / p.total_population, 2) AS per_capita_usage,
    p.avg_per_capita_usage AS expected_usage
FROM communities c
JOIN population_data p ON c.id = p.community_id
JOIN water_data w ON c.id = w.community_id AND p.recorded_date = w.recorded_date;

-- View: Recent Alerts by Community
CREATE OR REPLACE VIEW vw_recent_alerts AS
SELECT 
    c.name,
    a.alert_type,
    a.alert_level,
    a.message,
    a.is_acknowledged,
    a.alert_triggered_at
FROM communities c
JOIN scarcity_alerts a ON c.id = a.community_id
WHERE a.alert_triggered_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
ORDER BY a.alert_triggered_at DESC;

-- ==============================================================================
-- CREATE STORED PROCEDURES (Optional but useful)
-- ==============================================================================

-- Procedure: Calculate Scarcity Level
DELIMITER $$
CREATE PROCEDURE sp_calculate_scarcity_level(
    IN p_community_id INT,
    OUT p_scarcity_level VARCHAR(20),
    OUT p_scarcity_percentage DECIMAL(5, 2)
)
BEGIN
    DECLARE v_supply INT;
    DECLARE v_usage INT;
    
    -- Get latest water data
    SELECT total_water_supply, total_water_usage 
    INTO v_supply, v_usage
    FROM water_data 
    WHERE community_id = p_community_id
    ORDER BY recorded_date DESC 
    LIMIT 1;
    
    -- Calculate scarcity percentage
    SET p_scarcity_percentage = (v_usage - v_supply) / v_usage * 100;
    
    -- Determine scarcity level
    IF p_scarcity_percentage < 0 THEN
        SET p_scarcity_level = 'LOW';
    ELSEIF p_scarcity_percentage < 20 THEN
        SET p_scarcity_level = 'MODERATE';
    ELSEIF p_scarcity_percentage < 50 THEN
        SET p_scarcity_level = 'HIGH';
    ELSE
        SET p_scarcity_level = 'CRITICAL';
    END IF;
END$$
DELIMITER ;

-- ==============================================================================
-- GRANT PERMISSIONS (Adjust as needed)
-- ==============================================================================
-- For application database user:
-- GRANT ALL PRIVILEGES ON water_scarcity_db.* TO 'water_user'@'localhost' IDENTIFIED BY 'password';
-- FLUSH PRIVILEGES;

-- ==============================================================================
-- DATABASE SETUP VERIFICATION QUERIES
-- ==============================================================================
-- Run these to verify tables are created:
-- SELECT COUNT(*) as total_tables FROM information_schema.tables WHERE table_schema = 'water_scarcity_db';
-- SHOW TABLES;
-- DESC communities;
-- DESC water_data;
-- etc.
