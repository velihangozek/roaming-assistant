// Turkcell Roaming Assistant Application
// API Configuration
const API_BASE_URL = 'http://localhost:8080/api';

// Data storage (will be loaded from API)
let countries = [];
let roamingRates = [];
let roamingPacks = [];
let users = [];
let usageProfiles = [];
let exchangeRates = {};

// API Helper Functions
async function apiCall(endpoint, options = {}) {
    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        return await response.json();
    } catch (error) {
        console.error(`API call failed for ${endpoint}:`, error);
        throw error;
    }
}

// Load data from API
async function loadDataFromAPI() {
    try {
        showLoading('Veriler yükleniyor...');
        
        // Load all data in parallel
        const [
            countriesData,
            ratesData,
            packsData,
            usersData,
            profilesData
        ] = await Promise.all([
            apiCall('/countries'),
            apiCall('/roaming-rates'),
            apiCall('/roaming-packs'),
            apiCall('/users'),
            apiCall('/usage-profiles')
        ]);
        
        countries = countriesData;
        roamingRates = ratesData;
        roamingPacks = packsData;
        users = usersData;
        usageProfiles = profilesData;
        
        // Populate dropdowns
        populateCountryDropdown();
        populateUserDropdown();
        
        hideLoading();
        console.log('Data loaded successfully from API');
        
    } catch (error) {
        hideLoading();
        console.error('Failed to load data from API:', error);
        alert('Veriler yüklenirken bir hata oluştu. Lütfen backend sunucusunun çalıştığından emin olun.');
    }
}

// Show/Hide loading indicator
function showLoading(message = 'Yükleniyor...') {
    const loadingDiv = document.createElement('div');
    loadingDiv.id = 'loading-indicator';
    loadingDiv.className = 'loading-overlay';
    loadingDiv.innerHTML = `
        <div class="loading-content">
            <div class="spinner-border text-warning" role="status">
                <span class="visually-hidden">Loading...</span>
            </div>
            <p class="mt-2">${message}</p>
        </div>
    `;
    document.body.appendChild(loadingDiv);
}

function hideLoading() {
    const loadingDiv = document.getElementById('loading-indicator');
    if (loadingDiv) {
        loadingDiv.remove();
    }
}

// Exchange Rates (fallback if API fails)
let exchangeRates = {
    'USD': 34.5,
    'EUR': 37.2,
    'GBP': 43.1,
    'CHF': 38.9,
    'NOK': 3.2,
    'SEK': 3.4,
    'DKK': 5.0,
    'CAD': 25.3
};

// Fetch live exchange rates
async function fetchExchangeRates() {
    try {
        const response = await fetch('https://api.exchangerate-api.com/v4/latest/TRY');
        if (response.ok) {
            const data = await response.json();
            // Convert to TRY base rates
            const tryRates = {};
            for (const [currency, rate] of Object.entries(data.rates)) {
                tryRates[currency] = 1 / rate; // Convert to how many TRY per foreign currency
            }
            exchangeRates = { ...exchangeRates, ...tryRates };
            console.log('Exchange rates updated:', exchangeRates);
        }
    } catch (error) {
        console.error('Failed to fetch exchange rates:', error);
        console.log('Using fallback exchange rates');
    }
}

// Global Variables
let selectedUser = null;
let trips = [];
let currentUsageProfile = { avg_daily_mb: 600, avg_daily_min: 10, avg_daily_sms: 2 };
let selectedRecommendation = null;

// Application Initialization
document.addEventListener('DOMContentLoaded', async function() {
    try {
        // Initialize theme first
        initializeTheme();
        
        // Load data from API
        await loadDataFromAPI();
        
        // Initialize app with loaded data
        initializeApp();
        
        // Fetch exchange rates
        fetchExchangeRates();
        
    } catch (error) {
        console.error('Uygulama başlatılırken hata:', error);
        alert('Uygulama başlatılırken bir hata oluştu. Sayfa yenilenecek.');
        setTimeout(() => window.location.reload(), 2000);
    }
});

function initializeApp() {
    setDefaultDates();
    setupEventListeners();
}

function populateUserDropdown() {
    const userSelect = document.getElementById('userSelect');
    userSelect.innerHTML = '<option value="">Kullanıcı Seçin</option>';
    
    users.forEach(user => {
        const option = document.createElement('option');
        option.value = user.user_id;
        option.textContent = `${user.name} (${user.home_plan})`;
        userSelect.appendChild(option);
    });
}

function populateCountryDropdown() {
    const countrySelect = document.getElementById('countrySelect');
    countrySelect.innerHTML = '<option value="">Ülke Seçin</option>';
    
    countries.filter(c => c.country_code !== 'TR').forEach(country => {
        const option = document.createElement('option');
        option.value = country.country_code;
        option.textContent = country.country_name;
        countrySelect.appendChild(option);
    });
}

function setDefaultDates() {
    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    const weekLater = new Date(today);
    weekLater.setDate(weekLater.getDate() + 7);

    // Initialize Flatpickr for start date
    flatpickr("#startDate", {
        dateFormat: "Y-m-d",
        minDate: "today",
        locale: "tr",
        defaultDate: tomorrow,
        onChange: function(selectedDates, dateStr, instance) {
            // Update end date minimum when start date changes
            if (selectedDates.length > 0) {
                const nextDay = new Date(selectedDates[0]);
                nextDay.setDate(nextDay.getDate() + 1);
                endDatePicker.set('minDate', nextDay);
                
                // If end date is before new minimum, update it
                if (endDatePicker.selectedDates.length > 0 && endDatePicker.selectedDates[0] <= selectedDates[0]) {
                    endDatePicker.setDate(nextDay);
                }
            }
            resetCalculationState();
        }
    });

    // Initialize Flatpickr for end date
    const endDatePicker = flatpickr("#endDate", {
        dateFormat: "Y-m-d",
        minDate: tomorrow,
        locale: "tr",
        defaultDate: weekLater,
        onChange: function(selectedDates, dateStr, instance) {
            resetCalculationState();
        }
    });

    // Store the instance for later use
    window.endDatePicker = endDatePicker;
}

function setupEventListeners() {
    // User selection
    document.getElementById('userSelect').addEventListener('change', onUserChange);
    
    // Usage profile changes
    document.getElementById('dailyData').addEventListener('input', onUsageProfileChange);
    document.getElementById('dailyMin').addEventListener('input', onUsageProfileChange);
    document.getElementById('dailySMS').addEventListener('input', onUsageProfileChange);
}

// Theme Management
function initializeTheme() {
    const savedTheme = localStorage.getItem('theme') || 'light';
    setTheme(savedTheme);
    updateThemeIcon(savedTheme);
}

function toggleTheme() {
    const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
    const newTheme = currentTheme === 'light' ? 'dark' : 'light';
    setTheme(newTheme);
    updateThemeIcon(newTheme);
    localStorage.setItem('theme', newTheme);
}

function setTheme(theme) {
    document.documentElement.setAttribute('data-theme', theme);
}

function updateThemeIcon(theme) {
    const icon = document.getElementById('theme-icon');
    icon.className = theme === 'light' ? 'fas fa-moon' : 'fas fa-sun';
}

// User Selection Handler
function onUserChange() {
    const userSelect = document.getElementById('userSelect');
    const userId = parseInt(userSelect.value);
    
    if (userId) {
        selectedUser = users.find(u => u.user_id === userId);
        if (selectedUser) {
            // Find and set user's usage profile
            const userProfile = usageProfiles.find(p => p.user_id === userId);
            if (userProfile) {
                currentUsageProfile = userProfile;
                document.getElementById('dailyData').value = userProfile.avg_daily_mb;
                document.getElementById('dailyMin').value = userProfile.avg_daily_min;
                document.getElementById('dailySMS').value = userProfile.avg_daily_sms;
            }
        }
    } else {
        selectedUser = null;
    }
    
    resetCalculationState();
}

// Usage Profile Change Handler
function onUsageProfileChange() {
    updateUsageProfile();
    resetCalculationState();
}

function updateUsageProfile() {
    currentUsageProfile = {
        avg_daily_mb: parseInt(document.getElementById('dailyData').value) || 600,
        avg_daily_min: parseInt(document.getElementById('dailyMin').value) || 10,
        avg_daily_sms: parseInt(document.getElementById('dailySMS').value) || 2
    };
}

// Trip Management
function addTrip() {
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;
    const countryCode = document.getElementById('countrySelect').value;
    
    if (!startDate || !endDate || !countryCode) {
        alert('Lütfen tüm alanları doldurun!');
        return;
    }
    
    if (new Date(startDate) >= new Date(endDate)) {
        alert('Bitiş tarihi başlangıç tarihinden sonra olmalıdır!');
        return;
    }
    
    const country = countries.find(c => c.country_code === countryCode);
    if (!country) {
        alert('Seçilen ülke bulunamadı!');
        return;
    }
    
    const trip = {
        start_date: startDate,
        end_date: endDate,
        country_code: countryCode,
        country_name: country.country_name,
        region: country.region
    };
    
    trips.push(trip);
    updateTripSummary();
    resetCalculationState();
    
    // Clear form
    document.getElementById('countrySelect').value = '';
}

function removeTrip(index) {
    trips.splice(index, 1);
    updateTripSummary();
    resetCalculationState();
}

function updateTripSummary() {
    const container = document.getElementById('tripSummary');
    
    if (trips.length === 0) {
        container.style.display = 'none';
        return;
    }
    
    container.style.display = 'block';
    
    let totalDays = 0;
    let totalData = 0;
    let totalVoice = 0;
    let totalSMS = 0;
    let uniqueCountries = new Set();
    
    let tripsHtml = '';
    trips.forEach((trip, index) => {
        const days = Math.ceil((new Date(trip.end_date) - new Date(trip.start_date)) / (1000 * 60 * 60 * 24)) + 1;
        totalDays += days;
        totalData += currentUsageProfile.avg_daily_mb * days;
        totalVoice += currentUsageProfile.avg_daily_min * days;
        totalSMS += currentUsageProfile.avg_daily_sms * days;
        uniqueCountries.add(trip.country_name);
        
        tripsHtml += `
            <div class="trip-item">
                <div class="trip-info">
                    <strong>${trip.country_name}</strong><br>
                    <small>${trip.start_date} - ${trip.end_date} (${days} gün)</small>
                </div>
                <button class="btn btn-sm btn-outline-danger" onclick="removeTrip(${index})">
                    <i class="fas fa-times"></i>
                </button>
            </div>
        `;
    });
    
    document.getElementById('tripList').innerHTML = tripsHtml;
    document.getElementById('totalDays').textContent = totalDays;
    document.getElementById('totalData').textContent = (totalData / 1024).toFixed(1);
    document.getElementById('totalVoice').textContent = totalVoice;
    document.getElementById('totalSMS').textContent = totalSMS;
    document.getElementById('countryCount').textContent = uniqueCountries.size;
}

// Main Calculation Function
async function calculateRecommendations() {
    if (!selectedUser) {
        alert('Lütfen önce bir kullanıcı seçin!');
        return;
    }
    
    if (trips.length === 0) {
        alert('Lütfen en az bir seyahat planı ekleyin!');
        return;
    }
    
    // Reset previous state
    resetCalculationState();
    
    updateUsageProfile();
    showLoading('Öneriler hesaplanıyor...');
    
    try {
        // Prepare request data
        const requestData = {
            userId: selectedUser.user_id,
            trips: trips.map(trip => ({
                countryCode: trip.country_code,
                startDate: trip.start_date,
                endDate: trip.end_date
            })),
            profile: {
                avgDailyMb: currentUsageProfile.avg_daily_mb,
                avgDailyMin: currentUsageProfile.avg_daily_min,
                avgDailySms: currentUsageProfile.avg_daily_sms
            }
        };
        
        // Call backend API for recommendations
        const recommendations = await apiCall('/recommendations', {
            method: 'POST',
            body: JSON.stringify(requestData)
        });
        
        displayRecommendations(recommendations);
        hideLoading();
        
    } catch (error) {
        console.error('API çağrısı hatası:', error);
        hideLoading();
        resetCalculationState();
        
        // Fallback to local calculation if API fails
        alert('Backend bağlantısı başarısız. Lütfen backend sunucusunun çalıştığından emin olun.\n\nHata: ' + error.message);
        
        // Optional: Try local calculation as fallback
        try {
            const recommendations = generateRecommendations();
            displayRecommendations(recommendations);
        } catch (localError) {
            alert('Hesaplama sırasında bir hata oluştu: ' + localError.message);
        }
    }
}

function resetCalculationState() {
    // Clear previous recommendations
    const recommendationsContainer = document.getElementById('recommendations');
    if (recommendationsContainer) {
        recommendationsContainer.style.display = 'none';
        recommendationsContainer.innerHTML = '';
    }
    
    // Hide Ana Sayfa button
    const homeButton = document.getElementById('homeButton');
    if (homeButton) {
        homeButton.style.display = 'none';
    }
    
    // Close and remove any existing modals
    const existingModals = document.querySelectorAll('.modal.show');
    existingModals.forEach(modal => {
        const modalInstance = bootstrap.Modal.getInstance(modal);
        if (modalInstance) {
            modalInstance.hide();
        }
    });
    
    // Remove modal elements after a short delay
    setTimeout(() => {
        const modalElements = document.querySelectorAll('#checkoutModal, #successModal, #simulationModal');
        modalElements.forEach(modal => modal.remove());
    }, 300);
    
    // Reset global variables
    selectedRecommendation = null;
    window.currentRecommendations = null;
    
    // Reset summary values
    document.getElementById('totalDays').textContent = '0';
    document.getElementById('totalData').textContent = '0';
    document.getElementById('totalVoice').textContent = '0';
    document.getElementById('totalSMS').textContent = '0';
    document.getElementById('countryCount').textContent = '0';
}

// Go back to home state
function goToHome() {
    // Reset all form data
    document.getElementById('userSelect').value = '';
    document.getElementById('countrySelect').value = '';
    
    // Clear trips
    trips = [];
    updateTripSummary();
    
    // Reset user selection
    selectedUser = null;
    
    // Reset usage profile to defaults
    currentUsageProfile = { avg_daily_mb: 600, avg_daily_min: 10, avg_daily_sms: 2 };
    document.getElementById('dailyData').value = 600;
    document.getElementById('dailyMin').value = 10;
    document.getElementById('dailySMS').value = 2;
    
    // Reset calculation state
    resetCalculationState();
    
    // Scroll to top
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

// Rest of the calculation functions would go here...
// For brevity, I'm including just the essential API integration parts
// The existing generateRecommendations, calculatePayAsYouGo, etc. functions
// would remain the same for fallback purposes

// Checkout Functions
async function confirmCheckout() {
    if (!selectedRecommendation) {
        alert('Seçilen paket bulunamadı.');
        return;
    }
    
    // Close checkout modal
    const checkoutModal = bootstrap.Modal.getInstance(document.getElementById('checkoutModal'));
    if (checkoutModal) {
        checkoutModal.hide();
    }
    
    showLoading('Sipariş işleniyor...');
    
    try {
        // Prepare order data
        const orderData = {
            userId: selectedUser.user_id,
            packageId: selectedRecommendation.pack_id,
            packageName: selectedRecommendation.name,
            totalCost: selectedRecommendation.total_cost,
            currency: selectedRecommendation.currency,
            trips: trips
        };
        
        // Call backend API for checkout
        const orderResult = await apiCall('/checkout', {
            method: 'POST',
            body: JSON.stringify(orderData)
        });
        
        hideLoading();
        
        // Create success modal dynamically
        createSuccessModal();
        
        document.getElementById('orderDetails').innerHTML = `
            Sipariş No: <strong>${orderResult.orderId}</strong><br>
            Paket: <strong>${selectedRecommendation.name || 'Seçilen Paket'}</strong><br>
            Tutar: <strong>₺${(selectedRecommendation.total_cost_tl || 0).toFixed(2)}</strong>
        `;
        
        const successModal = new bootstrap.Modal(document.getElementById('successModal'));
        successModal.show();
        
    } catch (error) {
        hideLoading();
        console.error('Checkout hatası:', error);
        alert('Sipariş işlenirken bir hata oluştu. Lütfen tekrar deneyin.\n\nHata: ' + error.message);
    }
}

// Sample data function for testing
function populateWithSampleData() {
    document.getElementById('userSelect').value = '1001';
    onUserChange();
    
    // Set sample dates
    const startPicker = flatpickr("#startDate");
    const endPicker = flatpickr("#endDate");
    
    const today = new Date();
    const startDate = new Date(today);
    startDate.setDate(today.getDate() + 7);
    const endDate = new Date(startDate);
    endDate.setDate(startDate.getDate() + 5);
    
    startPicker.setDate(startDate);
    endPicker.setDate(endDate);
    
    // Add sample trips
    trips = [
        {
            start_date: startDate.toISOString().split('T')[0],
            end_date: endDate.toISOString().split('T')[0],
            country_code: 'DE',
            country_name: 'Almanya',
            region: 'Europe'
        }
    ];
    
    updateTripSummary();
}

// Fallback calculation functions (for when API is not available)
function generateRecommendations() {
    try {
        const payAsYouGo = calculatePayAsYouGo();
        const packageOptions = calculatePackageOptions();
        
        const allOptions = [payAsYouGo, ...packageOptions];
        
        // Sort by total cost in TL
        allOptions.sort((a, b) => (a.total_cost_tl || 0) - (b.total_cost_tl || 0));
        
        // Return top 3 options
        const top3 = allOptions.slice(0, 3);
        
        // Store globally for modal access
        window.currentRecommendations = top3;
        
        return top3;
    } catch (error) {
        console.error('Error in generateRecommendations:', error);
        throw error;
    }
}

function calculatePayAsYouGo() {
    let totalCost = 0;
    let totalDataCost = 0;
    let totalVoiceCost = 0;
    let totalSMSCost = 0;
    let currency = 'USD';
    const details = [];
    
    trips.forEach(trip => {
        const days = Math.ceil((new Date(trip.end_date) - new Date(trip.start_date)) / (1000 * 60 * 60 * 24)) + 1;
        const rate = roamingRates.find(r => r.country_code === trip.country_code);
        
        if (rate) {
            const dailyData = currentUsageProfile.avg_daily_mb || 0;
            const dailyVoice = currentUsageProfile.avg_daily_min || 0;
            const dailySMS = currentUsageProfile.avg_daily_sms || 0;
            
            const dataCost = (dailyData * days) * (rate.data_per_mb || 0);
            const voiceCost = (dailyVoice * days) * (rate.voice_per_min || 0);
            const smsCost = (dailySMS * days) * (rate.sms_per_msg || 0);
            const tripTotal = dataCost + voiceCost + smsCost;
            
            totalDataCost += dataCost;
            totalVoiceCost += voiceCost;
            totalSMSCost += smsCost;
            totalCost += tripTotal;
            currency = rate.currency;
            
            details.push({
                country: trip.country_name,
                data_cost: dataCost,
                voice_cost: voiceCost,
                sms_cost: smsCost,
                total: tripTotal,
                currency: rate.currency
            });
        }
    });
    
    const exchangeRate = exchangeRates[currency] || 30;
    
    return {
        type: 'payg',
        name: 'Kullandıkça Öde Sistemi',
        description: 'Kullandığınız kadar ödeyin',
        total_cost: totalCost,
        total_cost_tl: totalCost * exchangeRate,
        currency: currency,
        data_cost: totalDataCost,
        voice_cost: totalVoiceCost,
        sms_cost: totalSMSCost,
        details: details,
        coverage: 'Tüm ülkeler',
        validity_days: null,
        pack_count: null,
        warnings: []
    };
}

// Display Functions
function displayRecommendations(recommendations) {
    const container = document.getElementById('recommendations');
    container.style.display = 'block';
    
    // Show Ana Sayfa button
    const homeButton = document.getElementById('homeButton');
    if (homeButton) {
        homeButton.style.display = 'inline-block';
    }
    
    // Store recommendations globally
    window.currentRecommendations = recommendations;
    
    let html = `
        <div class="recommendations-section">
            <h3 class="section-title">
                <i class="fas fa-star text-warning me-2"></i>
                Size Özel Önerilerimiz
            </h3>
            <div class="row">
    `;
    
    recommendations.forEach((rec, index) => {
        const savings = calculateSavings(rec, recommendations);
        const badge = index === 0 ? '<span class="badge bg-success position-absolute top-0 start-50 translate-middle">EN İYİ SEÇİM</span>' : '';
        
        html += `
            <div class="col-lg-4 col-md-6 mb-4">
                <div class="card recommendation-card h-100 position-relative">
                    ${badge}
                    <div class="card-header">
                        <h5 class="card-title mb-1">${rec.name}</h5>
                        <div class="price-display">
                            <span class="price-amount">₺${(rec.total_cost_tl || 0).toFixed(2)}</span>
                            <span class="price-original">${rec.total_cost?.toFixed(2) || '0.00'} ${rec.currency}</span>
                        </div>
                    </div>
                    <div class="card-body">
                        <div class="card-content">
                            <p class="card-text">${rec.description}</p>
                            
                            ${generateWarningBadges(rec)}
                            ${generatePackageDetails(rec)}
                            
                            <div class="coverage-info">
                                <i class="fas fa-globe text-primary me-1"></i>
                                <small>${rec.coverage}</small>
                            </div>
                            
                            ${savings ? `<div class="savings-info"><i class="fas fa-piggy-bank text-success me-1"></i><small class="text-success">${savings}</small></div>` : ''}
                        </div>
                        
                        <div class="recommendation-actions">
                            <button class="btn btn-outline-primary btn-sm" onclick="showSimulation(${index})">
                                <i class="fas fa-chart-line me-1"></i>Detaylı Analiz
                            </button>
                            <button class="btn btn-warning btn-sm" onclick="showCheckoutModal(${index})">
                                <i class="fas fa-shopping-cart me-1"></i>Bu Paketi Seç
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;
    });
    
    html += `
            </div>
        </div>
    `;
    
    container.innerHTML = html;
}

function generateWarningBadges(recommendation) {
    if (!recommendation.warnings || recommendation.warnings.length === 0) {
        return '';
    }
    
    let html = '<div class="warning-badges mb-2">';
    recommendation.warnings.forEach(warning => {
        html += `<span class="badge bg-warning text-dark me-1"><i class="fas fa-exclamation-triangle me-1"></i>${warning}</span>`;
    });
    html += '</div>';
    
    return html;
}

function generatePackageDetails(recommendation) {
    if (recommendation.type === 'payg') {
        const theme = document.documentElement.getAttribute('data-theme') || 'light';
        const textClass = theme === 'dark' ? 'text-light' : 'text-dark';
        return `<div class="package-details ${textClass}"><small>Kullandığınız kadar ödeyin - aşım riski yok</small></div>`;
    }
    
    if (recommendation.pack_details) {
        const details = recommendation.pack_details;
        let html = '<div class="package-details">';
        
        if (recommendation.pack_count > 1) {
            html += `<div class="package-count-alert">
                <i class="fas fa-info-circle text-info me-1"></i>
                <small>${recommendation.pack_count} paket gerekli</small>
            </div>`;
        }
        
        html += '<div class="feature-list">';
        if (details.data_gb) {
            const totalData = (details.data_gb * recommendation.pack_count).toFixed(1);
            html += `<div class="feature-item"><i class="fas fa-wifi text-primary me-1"></i><small>${details.data_gb}GB Data (${totalData}GB toplam)</small></div>`;
        }
        if (details.voice_min) {
            const totalVoice = details.voice_min * recommendation.pack_count;
            html += `<div class="feature-item"><i class="fas fa-phone text-success me-1"></i><small>${details.voice_min} dk Arama (${totalVoice} dk toplam)</small></div>`;
        }
        if (details.sms) {
            const totalSms = details.sms * recommendation.pack_count;
            html += `<div class="feature-item"><i class="fas fa-sms text-info me-1"></i><small>${details.sms} SMS (${totalSms} SMS toplam)</small></div>`;
        }
        html += '</div></div>';
        
        return html;
    }
    
    return '';
}

function calculateSavings(recommendation, allRecommendations) {
    const paygOption = allRecommendations.find(r => r.type === 'payg');
    if (!paygOption || recommendation.type === 'payg') {
        return null;
    }
    
    const savings = (paygOption.total_cost_tl || 0) - (recommendation.total_cost_tl || 0);
    if (savings > 0) {
        return `Kullandıkça öde sistemine göre ₺${savings.toFixed(2)} tasarruf`;
    }
    
    return null;
}

// Modal Functions
function showCheckoutModal(index) {
    if (!window.currentRecommendations || !window.currentRecommendations[index]) {
        alert('Seçilen öneri bulunamadı. Lütfen tekrar hesaplama yapın.');
        return;
    }
    
    selectedRecommendation = window.currentRecommendations[index];
    
    // Create modal dynamically
    createCheckoutModal();
    
    // Populate modal content
    document.getElementById('checkoutPackageName').textContent = selectedRecommendation.name;
    document.getElementById('checkoutPrice').textContent = `₺${(selectedRecommendation.total_cost_tl || 0).toFixed(2)}`;
    document.getElementById('checkoutOriginalPrice').textContent = `${selectedRecommendation.total_cost?.toFixed(2) || '0.00'} ${selectedRecommendation.currency}`;
    
    const modal = new bootstrap.Modal(document.getElementById('checkoutModal'));
    modal.show();
}

function createCheckoutModal() {
    // Remove existing modal if any
    const existingModal = document.getElementById('checkoutModal');
    if (existingModal) {
        existingModal.remove();
    }
    
    const modalHtml = `
        <div class="modal fade" id="checkoutModal" tabindex="-1" aria-labelledby="checkoutModalLabel" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="checkoutModalLabel">
                            <i class="fas fa-shopping-cart me-2"></i>Paket Onayı
                        </h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <div class="text-center mb-4">
                            <h6 id="checkoutPackageName">-</h6>
                            <div class="price-display">
                                <span class="h4 text-warning" id="checkoutPrice">₺0.00</span>
                                <br>
                                <small class="text-muted" id="checkoutOriginalPrice">0.00 USD</small>
                            </div>
                        </div>
                        <div class="alert alert-info">
                            <i class="fas fa-info-circle me-2"></i>
                            Bu bir demo uygulamasıdır. Gerçek bir satın alma işlemi yapılmayacaktır.
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">İptal</button>
                        <button type="button" class="btn btn-warning" onclick="confirmCheckout()">
                            <i class="fas fa-check me-1"></i>Onayla
                        </button>
                    </div>
                </div>
            </div>
        </div>
    `;
    
    document.body.insertAdjacentHTML('beforeend', modalHtml);
}

function createSuccessModal() {
    // Remove existing modal if any
    const existingModal = document.getElementById('successModal');
    if (existingModal) {
        existingModal.remove();
    }
    
    const modalHtml = `
        <div class="modal fade" id="successModal" tabindex="-1" aria-labelledby="successModalLabel" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header bg-success text-white">
                        <h5 class="modal-title" id="successModalLabel">
                            <i class="fas fa-check-circle me-2"></i>Sipariş Başarılı!
                        </h5>
                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body text-center">
                        <div class="mb-3">
                            <i class="fas fa-check-circle text-success" style="font-size: 3rem;"></i>
                        </div>
                        <h6>Siparişiniz başarıyla oluşturuldu!</h6>
                        <div id="orderDetails" class="mt-3 p-3 bg-light rounded">
                            Sipariş detayları yükleniyor...
                        </div>
                        <div class="alert alert-info mt-3">
                            <i class="fas fa-info-circle me-2"></i>
                            Bu demo bir uygulamadır. Gerçek bir işlem yapılmamıştır.
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-success" data-bs-dismiss="modal">Tamam</button>
                    </div>
                </div>
            </div>
        </div>
    `;
    
    document.body.insertAdjacentHTML('beforeend', modalHtml);
}
