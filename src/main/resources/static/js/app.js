/**
 * Water Scarcity Optimizer - Frontend Application
 * Main JavaScript file for handling API calls and UI interactions
 * =================================================================
 */

// API Configuration
const API_BASE_URL = '/api';

let communityList = [];
let dashboardCharts = {};

// =====================================================================
// INITIALIZATION
// =====================================================================

document.addEventListener('DOMContentLoaded', function() {
    console.log("Application initialized");
    
    // Load initial data
    loadCommunities();
    refreshDashboard();
    
    // Set last update time
    updateLastUpdateTime();
    
    // Auto-refresh dashboard every 5 minutes
    setInterval(refreshDashboard, 5 * 60 * 1000);
});

// =====================================================================
// NAVIGATION
// =====================================================================

function showSection(sectionName) {
    // Hide all sections
    document.querySelectorAll('.section').forEach(section => {
        section.classList.remove('active');
    });
    
    // Show selected section
    const section = document.getElementById(sectionName);
    if (section) {
        section.classList.add('active');
        
        // Load section-specific data
        if (sectionName === 'communities') {
            loadCommunities();
        } else if (sectionName === 'predictions') {
            loadPredictions();
        } else if (sectionName === 'data') {
            loadDataStats();
        }
    }
}

// =====================================================================
// DASHBOARD FUNCTIONS
// =====================================================================

async function refreshDashboard() {
    console.log("Refreshing dashboard...");
    
    try {
        // Fetch dashboard data
        const response = await fetch(`${API_BASE_URL}/dashboard`);
        const data = await response.json();
        
        if (response.ok) {
            updateDashboardUI(data);
            updateCharts(data);
        } else {
            console.error("Failed to fetch dashboard data:", data);
        }
    } catch (error) {
        console.error("Error fetching dashboard:", error);
        alert("Error loading dashboard data. Please check your connection.");
    }
}

function updateDashboardUI(data) {
    const summary = data.summary || {};
    
    // Update metric cards
    document.getElementById('communities-count').textContent = data.totalCommunities || 0;
    document.getElementById('deficit-count').textContent = data.communitiesWithDeficit || 0;
    document.getElementById('deficit-percentage').textContent = 
        `${data.percentageInDeficit?.toFixed(1) || 0}% of communities`;
    document.getElementById('critical-count').textContent = data.criticalPredictions || 0;
    document.getElementById('model-confidence').textContent = 
        `${data.averagePredictionConfidence?.toFixed(1) || 0}%`;
    
    // Update health status
    const healthStatus = document.getElementById('health-status');
    const statusText = summary.overallStatus === 'CRITICAL' ? 'ALERT' : 'OPERATIONAL';
    healthStatus.innerHTML = `<strong>${statusText}</strong><br>${summary.message}`;
    healthStatus.className = summary.overallStatus === 'CRITICAL' ? 'status-critical' : 'status-operational';
    
    document.getElementById('health-time').textContent = 
        `Last updated: ${new Date(data.timestamp).toLocaleTimeString()}`;
}

function updateCharts(data) {
    // Chart 1: Water Status Distribution
    const statusCtx = document.getElementById('statusChart');
    if (statusCtx) {
        if (dashboardCharts.statusChart) {
            dashboardCharts.statusChart.destroy();
        }
        
        const totalCommunities = data.totalCommunities || 1;
        const withDeficit = data.communitiesWithDeficit || 0;
        const healthy = totalCommunities - withDeficit;
        
        dashboardCharts.statusChart = new Chart(statusCtx, {
            type: 'doughnut',
            data: {
                labels: ['Healthy', 'Water Deficit'],
                datasets: [{
                    data: [healthy, withDeficit],
                    backgroundColor: ['#28a745', '#ff9900'],
                    borderColor: '#fff',
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: {
                        position: 'bottom'
                    }
                }
            }
        });
    }
    
    // Chart 2: Critical Predictions
    const scarcityCtx = document.getElementById('scarcityChart');
    if (scarcityCtx) {
        if (dashboardCharts.scarcityChart) {
            dashboardCharts.scarcityChart.destroy();
        }
        
        const criticalCount = data.criticalPredictions || 0;
        const safeCount = (data.totalCommunities || 1) - criticalCount;
        
        dashboardCharts.scarcityChart = new Chart(scarcityCtx, {
            type: 'bar',
            data: {
                labels: ['Safe Level', 'Critical'],
                datasets: [{
                    label: 'Communities',
                    data: [safeCount, criticalCount],
                    backgroundColor: ['#0066cc', '#cc0000'],
                    borderColor: ['#0052a3', '#990000'],
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            stepSize: 1
                        }
                    }
                }
            }
        });
    }
}

// =====================================================================
// COMMUNITIES MANAGEMENT
// =====================================================================

async function loadCommunities() {
    try {
        const response = await fetch(`${API_BASE_URL}/communities`);
        const communities = await response.json();
        
        communityList = communities;
        
        // Update communities select dropdowns
        updateCommunitySelects(communities);
        
        // Update communities table
        updateCommunitiesTable(communities);
        
    } catch (error) {
        console.error("Error loading communities:", error);
    }
}

function updateCommunitySelects(communities) {
    const selects = document.querySelectorAll('#community-select, #data-community');
    
    selects.forEach(select => {
        const currentValue = select.value;
        select.innerHTML = '<option value="">-- Choose a community --</option>';
        
        communities.forEach(community => {
            const option = document.createElement('option');
            option.value = community.id;
            option.textContent = community.name;
            select.appendChild(option);
        });
        
        if (currentValue) {
            select.value = currentValue;
        }
    });
}

function updateCommunitiesTable(communities) {
    const tbody = document.getElementById('communities-tbody');
    tbody.innerHTML = '';
    
    communities.forEach(community => {
        const row = `
            <tr>
                <td>${community.name}</td>
                <td>${community.region}</td>
                <td>${community.latitude?.toFixed(4)}, ${community.longitude?.toFixed(4)}</td>
                <td><span class="badge ${community.isActive ? 'active' : 'inactive'}">${community.isActive ? 'Active' : 'Inactive'}</span></td>
                <td>
                    <button class="btn btn-secondary" onclick="viewCommunityDetails(${community.id})">View</button>
                </td>
            </tr>
        `;
        tbody.innerHTML += row;
    });
}

async function addCommunity(event) {
    event.preventDefault();
    
    const community = {
        name: document.getElementById('comm-name').value,
        region: document.getElementById('comm-region').value,
        latitude: parseFloat(document.getElementById('comm-latitude').value) || null,
        longitude: parseFloat(document.getElementById('comm-longitude').value) || null,
        elevation: parseInt(document.getElementById('comm-elevation').value) || null,
        isActive: true
    };
    
    try {
        const response = await fetch(`${API_BASE_URL}/communities`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(community)
        });
        
        if (response.ok) {
            alert('Community added successfully!');
            document.getElementById('community-form').reset();
            loadCommunities();
        } else {
            const error = await response.json();
            alert('Error: ' + (error.message || 'Failed to add community'));
        }
    } catch (error) {
        console.error('Error adding community:', error);
        alert('Error adding community. Please try again.');
    }
}

function viewCommunityDetails(communityId) {
    const community = communityList.find(c => c.id === communityId);
    if (community) {
        alert(`Community: ${community.name}\nRegion: ${community.region}\nCoordinates: ${community.latitude}, ${community.longitude}`);
    }
}

// =====================================================================
// PREDICTIONS
// =====================================================================

async function loadPredictions() {
    try {
        const response = await fetch(`${API_BASE_URL}/predictions/critical`);
        const data = await response.json();
        
        displayCriticalAlerts(data.predictions || []);
    } catch (error) {
        console.error("Error loading predictions:", error);
    }
}

function updatePredictionDisplay() {
    // This will be called when user selects a community
}

async function generatePrediction() {
    const communityId = document.getElementById('community-select').value;
    
    if (!communityId) {
        alert('Please select a community');
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/predictions/${communityId}`, {
            method: 'POST'
        });
        
        const prediction = await response.json();
        
        if (response.ok) {
            displayPredictionResults(prediction);
        } else {
            alert('Error: ' + (prediction.error || 'Failed to generate prediction'));
        }
    } catch (error) {
        console.error('Error generating prediction:', error);
        alert('Error generating prediction. Please try again.');
    }
}

function displayPredictionResults(prediction) {
    const card = document.getElementById('prediction-card');
    
    document.getElementById('pred-level').textContent = prediction.scarcityLevel || '-';
    document.getElementById('pred-percentage').textContent = 
        `${prediction.scarcityPercentage?.toFixed(1) || 0}%`;
    document.getElementById('pred-confidence').textContent = 
        `${prediction.confidence?.toFixed(1) || 0}%`;
    document.getElementById('pred-deficit').textContent = 
        `${prediction.predictedWaterDeficit?.toLocaleString() || 0} liters`;
    document.getElementById('pred-reasoning').textContent = prediction.reasoning || '-';
    
    card.style.display = 'block';
}

function displayCriticalAlerts(predictions) {
    const container = document.getElementById('alerts-container');
    
    if (predictions.length === 0) {
        container.innerHTML = '<div class="alert alert-success">No critical alerts</div>';
        return;
    }
    
    container.innerHTML = '';
    predictions.forEach(pred => {
        const alertHtml = `
            <div class="alert alert-danger">
                <strong>${pred.community?.name}</strong> - Scarcity: ${pred.scarcityLevel}<br>
                Confidence: ${pred.confidence.toFixed(1)}% | Deficit: ${(pred.predictedWaterDeficit || 0).toLocaleString()} L
            </div>
        `;
        container.innerHTML += alertHtml;
    });
}

async function generateAllPredictions() {
    if (!confirm('Generate predictions for all communities? This may take a moment.')) {
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/predictions/all`, {
            method: 'POST'
        });
        
        const data = await response.json();
        
        if (response.ok) {
            alert(`Predictions generated for ${data.count} communities`);
            refreshDashboard();
        } else {
            alert('Error: ' + (data.error || 'Failed to generate predictions'));
        }
    } catch (error) {
        console.error('Error generating batch predictions:', error);
        alert('Error generating predictions. Please try again.');
    }
}

// =====================================================================
// WATER DATA MANAGEMENT
// =====================================================================

async function loadDataStats() {
    // Load data statistics when data section is opened
    // Can be extended to fetch actual statistics from backend
}

async function addWaterData(event) {
    event.preventDefault();
    
    const communityId = document.getElementById('data-community').value;
    if (!communityId) {
        alert('Please select a community');
        return;
    }
    
    const community = communityList.find(c => c.id === parseInt(communityId));
    
    const waterData = {
        community: { id: parseInt(communityId) },
        rainfallMm: parseFloat(document.getElementById('data-rainfall').value) || 0,
        groundwaterSupply: parseInt(document.getElementById('data-groundwater').value) || 0,
        surfaceWaterSupply: parseInt(document.getElementById('data-surface').value) || 0,
        recycledWaterSupply: parseInt(document.getElementById('data-recycled').value) || 0,
        residentialUsage: parseInt(document.getElementById('data-residential').value) || 0,
        agriculturalUsage: parseInt(document.getElementById('data-agricultural').value) || 0,
        industrialUsage: parseInt(document.getElementById('data-industrial').value) || 0,
        waterQualityScore: parseFloat(document.getElementById('data-quality').value) || 0,
        recordedDate: new Date().toISOString().split('T')[0]
    };
    
    // Calculate totals
    waterData.totalWaterSupply = waterData.groundwaterSupply + 
                                 waterData.surfaceWaterSupply + 
                                 waterData.recycledWaterSupply;
    waterData.totalWaterUsage = waterData.residentialUsage + 
                                waterData.agriculturalUsage + 
                                waterData.industrialUsage;
    
    try {
        const response = await fetch(`${API_BASE_URL}/water-data`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(waterData)
        });
        
        if (response.ok) {
            alert('Water data saved successfully!');
            document.getElementById('water-data-form').reset();
            refreshDashboard();
        } else {
            const error = await response.json();
            alert('Error: ' + (error.error || 'Failed to save water data'));
        }
    } catch (error) {
        console.error('Error saving water data:', error);
        alert('Error saving water data. Please try again.');
    }
}

// =====================================================================
// UTILITY FUNCTIONS
// =====================================================================

function updateLastUpdateTime() {
    document.getElementById('last-update').textContent = new Date().toLocaleTimeString();
}

// Format large numbers with commas
function formatNumber(num) {
    return num?.toLocaleString() || '0';
}

// Status badge styling helper
function getStatusBadge(status) {
    const colors = {
        'LOW': 'success',
        'MODERATE': 'warning',
        'HIGH': 'warning',
        'CRITICAL': 'danger'
    };
    return colors[status] || 'info';
}

// ==================================================================
// ERROR HANDLING
// ==================================================================

window.onerror = function(msg, url, lineNo, columnNo, error) {
    console.error('Error:', msg, 'at', url, ':', lineNo, ':', columnNo);
    return false;
};

// Log messages to console
console.log("Water Scarcity Optimizer - Frontend Loaded");
console.log("API Base URL:", API_BASE_URL);
