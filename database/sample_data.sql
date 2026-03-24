-- ============================================================================
-- WATER SCARCITY PREDICTION OPTIMIZER - SAMPLE DATA FOR TESTING
-- ============================================================================
-- This script populates the database with sample data for demonstration
-- and testing purposes.
-- ============================================================================

USE water_scarcity_db;

-- ============================================================================
-- 1. INSERT SAMPLE COMMUNITIES
-- ============================================================================

INSERT INTO communities (name, region, latitude, longitude, elevation, is_active, created_at, updated_at) VALUES
('North Plains City', 'Northern Region', 40.7128, -74.0060, 150, TRUE, NOW(), NOW()),
('South Valley Town', 'Southern Region', 34.0522, -118.2437, 100, TRUE, NOW(), NOW()),
('Eastern Hills District', 'Eastern Region', 42.3601, -71.0589, 200, TRUE, NOW(), NOW()),
('Western Desert Area', 'Western Region', 47.6062, -122.3321, 180, TRUE, NOW(), NOW()),
('Central Lake Region', 'Central Region', 41.8781, -87.6298, 120, TRUE, NOW(), NOW());

-- ============================================================================
-- 2. INSERT SAMPLE WATER DATA (Last 30 days)
-- ============================================================================

-- North Plains City - Data for 30 days
INSERT INTO water_data (community_id, rainfall_mm, groundwater_supply, surface_water_supply, recycled_water_supply, total_water_supply, residential_usage, agricultural_usage, industrial_usage, total_water_usage, water_quality_score, recorded_date, created_at) VALUES

-- Day 1-5: Normal conditions
(1, 5.2, 150000, 200000, 30000, 380000, 80000, 150000, 60000, 290000, 8.5, DATE_SUB(NOW(), INTERVAL 30 DAY), NOW()),
(1, 4.8, 148000, 198000, 32000, 378000, 81000, 152000, 61000, 294000, 8.3, DATE_SUB(NOW(), INTERVAL 29 DAY), NOW()),
(1, 6.1, 152000, 202000, 31000, 385000, 79000, 148000, 59000, 286000, 8.7, DATE_SUB(NOW(), INTERVAL 28 DAY), NOW()),
(1, 3.5, 146000, 196000, 28000, 370000, 82000, 155000, 63000, 300000, 8.2, DATE_SUB(NOW(), INTERVAL 27 DAY), NOW()),
(1, 7.2, 155000, 205000, 35000, 395000, 78000, 145000, 57000, 280000, 8.6, DATE_SUB(NOW(), INTERVAL 26 DAY), NOW()),

-- Day 6-10: Some decline
(1, 2.1, 140000, 185000, 25000, 350000, 85000, 160000, 65000, 310000, 7.8, DATE_SUB(NOW(), INTERVAL 25 DAY), NOW()),
(1, 1.5, 138000, 180000, 22000, 340000, 86000, 165000, 67000, 318000, 7.5, DATE_SUB(NOW(), INTERVAL 24 DAY), NOW()),
(1, 0.8, 135000, 175000, 20000, 330000, 88000, 170000, 69000, 327000, 7.2, DATE_SUB(NOW(), INTERVAL 23 DAY), NOW()),
(1, 0.3, 132000, 170000, 18000, 320000, 90000, 175000, 71000, 336000, 6.9, DATE_SUB(NOW(), INTERVAL 22 DAY), NOW()),
(1, 0.0, 130000, 165000, 15000, 310000, 92000, 180000, 73000, 345000, 6.5, DATE_SUB(NOW(), INTERVAL 21 DAY), NOW()),

-- Day 11-20: Further decline (DEFICIT STARTING)
(1, 0.0, 128000, 160000, 12000, 300000, 95000, 185000, 75000, 355000, 6.2, DATE_SUB(NOW(), INTERVAL 20 DAY), NOW()),
(1, 0.0, 125000, 155000, 10000, 290000, 98000, 190000, 77000, 365000, 6.0, DATE_SUB(NOW(), INTERVAL 19 DAY), NOW()),
(1, 0.0, 122000, 150000, 8000, 280000, 100000, 195000, 79000, 374000, 5.8, DATE_SUB(NOW(), INTERVAL 18 DAY), NOW()),
(1, 0.0, 120000, 145000, 5000, 270000, 102000, 200000, 81000, 383000, 5.6, DATE_SUB(NOW(), INTERVAL 17 DAY), NOW()),
(1, 0.0, 118000, 140000, 3000, 261000, 104000, 205000, 83000, 392000, 5.4, DATE_SUB(NOW(), INTERVAL 16 DAY), NOW()),

(1, 0.0, 115000, 135000, 2000, 252000, 106000, 210000, 85000, 401000, 5.2, DATE_SUB(NOW(), INTERVAL 15 DAY), NOW()),
(1, 0.0, 112000, 130000, 1000, 243000, 108000, 215000, 87000, 410000, 5.0, DATE_SUB(NOW(), INTERVAL 14 DAY), NOW()),
(1, 0.0, 110000, 125000, 0, 235000, 110000, 220000, 89000, 419000, 4.8, DATE_SUB(NOW(), INTERVAL 13 DAY), NOW()),
(1, 0.0, 108000, 120000, 0, 228000, 112000, 225000, 91000, 428000, 4.6, DATE_SUB(NOW(), INTERVAL 12 DAY), NOW()),
(1, 0.0, 105000, 115000, 0, 220000, 114000, 230000, 93000, 437000, 4.4, DATE_SUB(NOW(), INTERVAL 11 DAY), NOW()),

-- Day 21-30: Critical deficit
(1, 0.0, 100000, 110000, 0, 210000, 116000, 235000, 95000, 446000, 4.2, DATE_SUB(NOW(), INTERVAL 10 DAY), NOW()),
(1, 0.0, 98000, 105000, 0, 203000, 118000, 240000, 97000, 455000, 4.0, DATE_SUB(NOW(), INTERVAL 9 DAY), NOW()),
(1, 0.0, 95000, 100000, 0, 195000, 120000, 245000, 99000, 464000, 3.8, DATE_SUB(NOW(), INTERVAL 8 DAY), NOW()),
(1, 0.0, 92000, 95000, 0, 187000, 122000, 250000, 101000, 473000, 3.6, DATE_SUB(NOW(), INTERVAL 7 DAY), NOW()),
(1, 0.0, 90000, 90000, 0, 180000, 124000, 255000, 103000, 482000, 3.4, DATE_SUB(NOW(), INTERVAL 6 DAY), NOW()),

(1, 0.0, 88000, 85000, 0, 173000, 126000, 260000, 105000, 491000, 3.2, DATE_SUB(NOW(), INTERVAL 5 DAY), NOW()),
(1, 0.0, 85000, 80000, 0, 165000, 128000, 265000, 107000, 500000, 3.0, DATE_SUB(NOW(), INTERVAL 4 DAY), NOW()),
(1, 0.0, 83000, 75000, 0, 158000, 130000, 270000, 109000, 509000, 2.8, DATE_SUB(NOW(), INTERVAL 3 DAY), NOW()),
(1, 0.2, 85000, 78000, 2000, 165000, 128000, 268000, 107000, 503000, 2.9, DATE_SUB(NOW(), INTERVAL 2 DAY), NOW()),
(1, 0.1, 84000, 77000, 1000, 162000, 129000, 269000, 108000, 506000, 2.85, NOW(), NOW());

-- ============================================================================
-- 3. INSERT SAMPLE POPULATION DATA
-- ============================================================================

INSERT INTO population_data (community_id, total_population, urban_population, rural_population, average_household_size, growth_rate, avg_per_capita_usage, poverty_rate, recorded_date, created_at) VALUES

-- North Plains City
(1, 500000, 400000, 100000, 3.5, 2.1, 260, 18.5, DATE_SUB(NOW(), INTERVAL 30 DAY), NOW()),
(1, 505050, 404040, 101010, 3.5, 2.1, 265, 18.3, NOW(), NOW()),

-- South Valley Town
(2, 350000, 280000, 70000, 3.8, 1.8, 240, 22.1, NOW(), NOW()),

-- Eastern Hills District
(3, 400000, 300000, 100000, 3.6, 2.5, 255, 20.5, NOW(), NOW()),

-- Western Desert Area
(4, 280000, 200000, 80000, 3.9, 1.5, 275, 25.3, NOW(), NOW()),

-- Central Lake Region
(5, 450000, 350000, 100000, 3.4, 2.3, 250, 19.8, NOW(), NOW());

-- ============================================================================
-- 4. VERIFY DATA INSERTION
-- ============================================================================

SELECT 'Communities Count:' as `Status`, COUNT(*) as `Value` FROM communities;
SELECT 'Water Data Records:' as `Status`, COUNT(*) as `Value` FROM water_data;
SELECT 'Population Data Records:' as `Status`, COUNT(*) as `Value` FROM population_data;

-- Display sample data
SELECT c.name, c.region, COUNT(wd.id) as water_records 
FROM communities c 
LEFT JOIN water_data wd ON c.id = wd.community_id 
GROUP BY c.id, c.name, c.region;

SELECT * FROM vw_water_balance LIMIT 5;
