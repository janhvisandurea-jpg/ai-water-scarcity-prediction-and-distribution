# Water Scarcity Prediction Optimizer - Testing & Setup Guide

## Quick Start Guide

### Prerequisites
- Java 11 or higher
- MySQL 8.0+
- Maven 3.6+
- Git (optional)

---

## 1. DATABASE SETUP

### Step 1a: Create Database and Schema

```bash
# Connect to MySQL
mysql -u root -p

# In MySQL:
source /path/to/database/schema.sql
```

Or run the SQL commands manually in MySQL Workbench.

### Step 1b: Insert Sample Data

```bash
mysql -u root -p water_scarcity_db < /path/to/database/sample_data.sql
```

**Database Configuration:**
- Database Name: `water_scarcity_db`
- Default User: `root`
- Default Password: `admin123` (update in application.properties!)

---

## 2. BUILD THE PROJECT

```bash
# Navigate to project directory
cd /path/to/WaterScarcityOptimizer

# Clean and build
mvn clean install

# Run tests (if available)
mvn test
```

---

## 3. RUN THE APPLICATION

```bash
# Start Spring Boot application
mvn spring-boot:run

# OR build JAR and run
mvn package
java -jar target/water-scarcity-optimizer-1.0.0.jar
```

**Expected Output:**
```
===========================================
Water Scarcity Prediction Optimizer Started
===========================================
[Available endpoints listed]
```

Application runs on: `http://localhost:8080`

---

## 4. TESTING ENDPOINTS

### 4a. Health Check

```bash
curl http://localhost:8080/health
```

**Expected Response:**
```json
{
  "status": "UP",
  "timestamp": "2026-03-20T10:30:00",
  "service": "Water Scarcity Prediction Optimizer",
  "version": "1.0.0"
}
```

### 4b. Dashboard

```bash
curl http://localhost:8080/api/dashboard
```

### 4c. Get All Communities

```bash
curl http://localhost:8080/api/communities
```

**Expected Response:**
```json
[
  {
    "id": 1,
    "name": "North Plains City",
    "region": "Northern Region",
    "latitude": 40.7128,
    "longitude": -74.0060,
    "elevation": 150,
    "isActive": true
  },
  ...
]
```

### 4d. Get Latest Water Data for Community

```bash
curl http://localhost:8080/api/water-data/1/latest
```

### 4e. Generate Prediction for a Community

```bash
curl -X POST http://localhost:8080/api/predictions/1
```

**Expected Response:**
```json
{
  "id": 1,
  "community": {
    "id": 1,
    "name": "North Plains City"
  },
  "scarcityLevel": "CRITICAL",
  "scarcityPercentage": 65.4,
  "confidence": 82.3,
  "predictedWaterDeficit": 252000,
  "predictedDemand": 506000,
  "predictedSupply": 254000,
  "predictionDate": "2026-03-20",
  "predictionHorizon": 30,
  "dataPointsUsed": 30,
  "modelAccuracy": 78.5,
  "modelVersion": "1.0"
}
```

### 4f. Get Critical Predictions

```bash
curl http://localhost:8080/api/predictions/critical
```

### 4g. Check Water Deficit

```bash
curl http://localhost:8080/api/water-data/1/deficit
```

---

## 5. WEB INTERFACE TESTING

Navigate to `http://localhost:8080` or `http://localhost:8080/dashboard`

### 5a. Dashboard Section
- View system health status
- See community count and water deficit statistics
- View critical prediction count
- Check model confidence

### 5b. Predictions Section
- Select a community
- Generate prediction
- View prediction results and analysis

### 5c. Communities Section
- View list of all communities
- Add new communities using the form

### 5d. Data Management Section
- Add new water data records
- View water statistics

---

## 6. SAMPLE TEST SCENARIOS

### Scenario 1: Community with Water Deficit
1. Go to Predictions section
2. Select "North Plains City"
3. Click "Generate Prediction"
4. Expected: CRITICAL scarcity level (based on sample data showing deficit growing over 30 days)

### Scenario 2: Add New Community
1. Go to Communities section
2. Fill in:
   - Name: "Test Community"
   - Region: "Test Region"
   - Latitude: 35.5
   - Longitude: -120.5
3. Click "Add Community"
4. It should appear in communities list

### Scenario 3: Record Water Data
1. Go to Data Management
2. Select a community
3. Fill in water metrics:
   - Rainfall: 10mm
   - Groundwater: 100000L
   - Surface Water: 150000L
   - Residential Usage: 50000L
   - Agricultural Usage: 100000L
   - Industrial Usage: 40000L
4. Click "Save Water Data"
5. Refresh dashboard - metrics should update

---

## 7. TROUBLESHOOTING

### Issue: "Connection refused" when connecting to database

**Solution:**
- Ensure MySQL is running: `mysql -u root -p` (Windows) or `sudo systemctl start mysql` (Linux)
- Update credentials in `application.properties`:
  ```properties
  spring.datasource.url=jdbc:mysql://localhost:3306/water_scarcity_db
  spring.datasource.username=root
  spring.datasource.password=your_password
  ```

### Issue: Port 8080 already in use

**Solution:**
```bash
# Change port in application.properties
server.port=8081

# Or kill existing process on port 8080
# Windows: netstat -ano | findstr :8080
# Linux: lsof -i :8080
```

### Issue: Prediction returns "ERROR"

**Solution:**
- Ensure community has at least 5 water data records (for ML model)
- Check logs: `tail -f logs/application.log`
- Verify database has population data for the community

### Issue: Frontend doesn't load

**Solution:**
- Clear browser cache (Ctrl+Shift+Del)
- Check browser console for JavaScript errors (F12)
- Ensure backend is running and accessible
- Check CORS settings if frontend is on different port

---

## 8. PERFORMANCE TESTING

### Test Batch Prediction Generation

```bash
# Generate predictions for all communities
curl -X POST http://localhost:8080/api/predictions/all
```

**Expected:** Should complete in < 5 seconds for 5 communities

### Load Test Sample

```bash
# Simple load test with ab (Apache Bench)
ab -n 100 -c 10 http://localhost:8080/api/communities
```

---

## 9. DATABASE QUERIES FOR DEBUGGING

```sql
-- Check communities
SELECT * FROM communities;

-- View latest water data
SELECT c.name, wd.* FROM water_data wd
JOIN communities c ON wd.community_id = c.id
ORDER BY wd.recorded_date DESC LIMIT 5;

-- Check predictions
SELECT c.name, pr.scarcity_level, pr.confidence 
FROM prediction_results pr
JOIN communities c ON pr.community_id = c.id
ORDER BY pr.prediction_date DESC LIMIT 10;

-- View water balance
SELECT * FROM vw_water_balance;

-- Check population data
SELECT * FROM population_data ORDER BY recorded_date DESC;
```

---

## 10. NEXT STEPS FOR IMPROVEMENT

1. **Real Data Integration:**
   - Connect to IoT sensors for real-time data
   - Integrate weather APIs for rainfall data

2. **Advanced ML:**
   - Implement Deep Learning (LSTM, GRU) models
   - Add seasonal adjustments

3. **Features:**
   - Alerts and notifications
   - Export reports (PDF, CSV)
   - Multi-user authentication
   - Admin dashboard

4. **Performance:**
   - Add caching (Redis)
   - Implement pagination
   - Database query optimization

5. **Scalability:**
   - Containerize with Docker
   - Deploy to Kubernetes
   - Multi-region support

---

## 11. USEFUL COMMANDS

```bash
# Build
mvn clean package

# Run
mvn spring-boot:run

# Check dependencies
mvn dependency:tree

# Generate reports
mvn site

# Run specific test
mvn test -Dtest=CommunityServiceTest

# Skip tests
mvn package -DskipTests

# View help
mvn help:describe -Dplugin=spring-boot
```

---

## 12. CONFIGURATION FILES TO UPDATE

### application.properties
```properties
# Database credentials
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.url=jdbc:mysql://your_host:3306/water_scarcity_db

# ML Model settings
app.ml.model.accuracy-threshold=0.75
app.ml.model.prediction-days=30

# Logging
logging.level.root=INFO
logging.level.com.water.scarcity=DEBUG
```

---

## 13. API DOCUMENTATION

Full API documentation available at:
- REST Endpoints: `GET /api/overview`
- Swagger/OpenAPI: Can be enabled by adding `springfox-swagger2` dependency

---

**Last Updated:** March 2026
**Version:** 1.0.0
**Support:** Refer to code comments and README.md
