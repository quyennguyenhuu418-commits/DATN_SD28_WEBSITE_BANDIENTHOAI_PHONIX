<template>
  <div class="free-map-picker">
    <!-- Address Input with Search -->
    <div class="address-search">
      <div class="search-input-wrapper">
        <input
          v-model="searchQuery"
          @input="onSearchInput"
          @focus="showSuggestions = true"
          :placeholder="placeholder"
          class="search-input"
        />
        <button @click="openMapModal" class="map-icon-button" type="button" title="Chọn trên bản đồ">
          <i class="bi bi-geo-alt"></i>
        </button>
      </div>

      <!-- Search Suggestions -->
      <div v-if="showSuggestions && suggestions.length > 0" class="suggestions">
        <div
          v-for="(suggestion, index) in suggestions"
          :key="index"
          @click="selectSuggestion(suggestion)"
          class="suggestion-item"
        >
          <i class="bi bi-geo-alt"></i>
          <div class="suggestion-content">
            <div class="suggestion-main">{{ suggestion.display_name }}</div>
            <div class="suggestion-secondary">{{ suggestion.address?.city || suggestion.address?.state }}</div>
          </div>
        </div>
      </div>
      
      <!-- API Error Message -->
      <div v-if="apiError" class="api-error">
        <i class="bi bi-exclamation-triangle"></i>
        <span>{{ apiError }}</span>
      </div>
    </div>

    <!-- Map Modal -->
    <div v-if="showMapModal" class="map-modal-overlay" @click="closeMapModal">
      <div class="map-modal" @click.stop>
        <div class="map-header">
          <h3>Chọn địa chỉ trên bản đồ</h3>
          <button @click="closeMapModal" class="close-button" type="button">
            <i class="bi bi-x"></i>
          </button>
        </div>

        <div class="map-container">
          <div ref="mapContainer" class="map"></div>

          <!-- Loading indicator for current location -->
          <div v-if="isGettingLocation" class="location-loading">
            <i class="bi bi-hourglass-split"></i>
            <span>Đang lấy vị trí hiện tại...</span>
          </div>

          <div class="map-controls">
            <button @click="getCurrentLocation" class="location-button" type="button" :disabled="isGettingLocation">
              <i v-if="isGettingLocation" class="bi bi-hourglass-split"></i>
              <i v-else class="bi bi-crosshair"></i>
              {{ isGettingLocation ? 'Đang lấy vị trí...' : 'Vị trí hiện tại' }}
            </button>
          </div>
        </div>

        <!-- Action buttons at bottom -->
        <div class="map-actions">
          <button @click="closeMapModal" class="cancel-button" type="button">
            <i class="bi bi-x"></i>
            Hủy
          </button>
          <button @click="confirmLocation" class="confirm-button" type="button" :disabled="!selectedLocation">
            <i class="bi bi-check"></i>
            Xác nhận địa chỉ
          </button>
        </div>

        <div v-if="selectedLocation" class="selected-address">
          <i class="bi bi-geo-alt-fill"></i>
          <span>{{ selectedLocation.display_name }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'

// Props
const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: 'Nhập địa chỉ...'
  }
})

// Emits
const emit = defineEmits(['update:modelValue', 'address-selected', 'location-updated'])

// State
const searchQuery = ref(props.modelValue)
const showSuggestions = ref(false)
const suggestions = ref([])
const showMapModal = ref(false)
const selectedLocation = ref(null)
const mapContainer = ref(null)
const map = ref(null)
const marker = ref(null)
const searchTimeout = ref(null)
const isGettingLocation = ref(false)
const apiError = ref('')

// OpenStreetMap Nominatim API (Free)
const NOMINATIM_BASE_URL = 'https://nominatim.openstreetmap.org'
const BACKUP_API_URL = 'https://photon.komoot.io/api'

// Function to try multiple APIs
const tryMultipleAPIs = async (url, options) => {
  const apis = [
    { url: url, name: 'Nominatim' },
    { url: url.replace('nominatim.openstreetmap.org', 'nominatim.openstreetmap.org'), name: 'Nominatim Backup' }
  ]
  
  for (const api of apis) {
    try {
      const response = await fetch(api.url, options)
      if (response.ok) {
        return response
      }
    } catch (error) {
      console.warn(`API ${api.name} failed:`, error)
      continue
    }
  }
  
  throw new Error('All APIs failed')
}

// Search input handler
const onSearchInput = () => {
  if (searchTimeout.value) {
    clearTimeout(searchTimeout.value)
  }

  searchTimeout.value = setTimeout(() => {
    if (searchQuery.value.length > 2) {
      searchPlaces()
    } else {
      suggestions.value = []
      showSuggestions.value = false
    }
  }, 300)
}

// Search places using OpenStreetMap Nominatim API
const searchPlaces = async () => {
  try {
    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), 10000) // 10 second timeout
    
    const url = `${NOMINATIM_BASE_URL}/search?format=json&q=${encodeURIComponent(searchQuery.value)}&limit=5&countrycodes=vn&addressdetails=1`
    const options = {
      signal: controller.signal,
      headers: {
        'User-Agent': 'PhoniX-Store-App/1.0'
      }
    }
    
    const response = await tryMultipleAPIs(url, options)
    clearTimeout(timeoutId)
    
    const data = await response.json()

    suggestions.value = data.map(place => ({
      place_id: place.place_id,
      display_name: place.display_name,
      lat: parseFloat(place.lat),
      lon: parseFloat(place.lon),
      address: place.address,
      formatted_address: place.display_name
    }))
  } catch (error) {
    console.error('Error searching places:', error)
    suggestions.value = []
    
    // Show user-friendly error message
    if (error.name === 'AbortError') {
      console.warn('Search request timed out. Please try again.')
      apiError.value = 'Tìm kiếm địa chỉ bị timeout. Vui lòng thử lại.'
    } else if (error.message.includes('Failed to fetch')) {
      console.warn('Network error. Please check your internet connection.')
      apiError.value = 'Lỗi kết nối mạng. Vui lòng kiểm tra internet.'
    } else if (error.message.includes('All APIs failed')) {
      console.warn('All geocoding services are unavailable.')
      apiError.value = 'Dịch vụ tìm kiếm địa chỉ tạm thời không khả dụng.'
    }
    
    // Clear error after 5 seconds
    setTimeout(() => {
      apiError.value = ''
    }, 5000)
  }
}

// Select suggestion
const selectSuggestion = (suggestion) => {
  searchQuery.value = suggestion.display_name
  selectedLocation.value = suggestion
  showSuggestions.value = false
  emit('update:modelValue', suggestion.display_name)
  emit('address-selected', suggestion)
}

// Open map modal
const openMapModal = async () => {
  showMapModal.value = true
  await nextTick()
  initMap()
}

// Close map modal
const closeMapModal = () => {
  showMapModal.value = false
  selectedLocation.value = null
}

// Initialize map using Leaflet
const initMap = () => {
  if (!mapContainer.value) return

  // Load Leaflet CSS and JS dynamically
  loadLeafletResources().then(() => {
    // Try to get current location first, fallback to Ho Chi Minh City
    getCurrentLocationForMap()
  })
}

// Get current location for map initialization
const getCurrentLocationForMap = () => {
  if (navigator.geolocation) {
    isGettingLocation.value = true
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const lat = position.coords.latitude
        const lng = position.coords.longitude

        // Initialize map with current location
        initMapWithLocation(lat, lng)

        // Reverse geocode to get address
        reverseGeocode(lat, lng)
        isGettingLocation.value = false
      },
      (error) => {
        console.log('Could not get current location:', error)
        // Fallback to Ho Chi Minh City
        initMapWithLocation(10.8231, 106.6297)
        isGettingLocation.value = false
      }
    )
  } else {
    // Fallback to Ho Chi Minh City
    initMapWithLocation(10.8231, 106.6297)
  }
}

// Initialize map with specific location
const initMapWithLocation = (lat, lng) => {
  const location = [lat, lng]

  map.value = L.map(mapContainer.value).setView(location, 15)

  // Add OpenStreetMap tiles
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap contributors',
    maxZoom: 19
  }).addTo(map.value)

  // Add click listener to map
  map.value.on('click', (event) => {
    const lat = event.latlng.lat
    const lng = event.latlng.lng

    // Reverse geocode to get address
    reverseGeocode(lat, lng)
  })

  // If we have a search query, try to geocode it
  if (searchQuery.value) {
    geocodeAddress(searchQuery.value)
  }
}

// Load Leaflet resources
const loadLeafletResources = () => {
  return new Promise((resolve, reject) => {
    // Check if Leaflet is already loaded
    if (window.L) {
      resolve()
      return
    }

    // Load CSS
    const link = document.createElement('link')
    link.rel = 'stylesheet'
    link.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'
    document.head.appendChild(link)

    // Load JS
    const script = document.createElement('script')
    script.src = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'
    script.onload = resolve
    script.onerror = reject
    document.head.appendChild(script)
  })
}

// Geocode address
const geocodeAddress = async (address) => {
  try {
    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), 10000) // 10 second timeout
    
    const response = await fetch(
      `${NOMINATIM_BASE_URL}/search?format=json&q=${encodeURIComponent(address)}&limit=1&countrycodes=vn&addressdetails=1`,
      {
        signal: controller.signal,
        headers: {
          'User-Agent': 'PhoniX-Store-App/1.0'
        }
      }
    )
    
    clearTimeout(timeoutId)
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    const data = await response.json()

    if (data.length > 0) {
      const place = data[0]
      const lat = parseFloat(place.lat)
      const lng = parseFloat(place.lon)

      map.value.setView([lat, lng], 15)

      const locationData = {
        place_id: place.place_id,
        display_name: place.display_name,
        lat: lat,
        lon: lng,
        address: place.address,
        formatted_address: place.display_name
      }

      selectLocation(locationData)
    }
  } catch (error) {
    console.error('Error geocoding address:', error)
    
    // Show user-friendly error message
    if (error.name === 'AbortError') {
      console.warn('Address geocoding timed out. Please try again.')
    } else if (error.message.includes('Failed to fetch')) {
      console.warn('Network error during address geocoding. Please check your connection.')
    }
  }
}

// Reverse geocode coordinates to address
const reverseGeocode = async (lat, lng) => {
  try {
    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), 10000) // 10 second timeout
    
    const response = await fetch(
      `${NOMINATIM_BASE_URL}/reverse?format=json&lat=${lat}&lon=${lng}&addressdetails=1`,
      {
        signal: controller.signal,
        headers: {
          'User-Agent': 'PhoniX-Store-App/1.0'
        }
      }
    )
    
    clearTimeout(timeoutId)
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    const data = await response.json()

    if (data) {
      const locationData = {
        place_id: data.place_id,
        display_name: data.display_name,
        lat: lat,
        lon: lng,
        address: data.address,
        formatted_address: data.display_name
      }

      selectLocation(locationData)
    }
  } catch (error) {
    console.error('Error reverse geocoding:', error)
    
    // Fallback: Create a basic location data without address details
    const fallbackLocationData = {
      place_id: `fallback_${Date.now()}`,
      display_name: `Vị trí: ${lat.toFixed(6)}, ${lng.toFixed(6)}`,
      lat: lat,
      lon: lng,
      address: null,
      formatted_address: `Vị trí: ${lat.toFixed(6)}, ${lng.toFixed(6)}`
    }
    
    selectLocation(fallbackLocationData)
    
    // Show user-friendly error message
    if (error.name === 'AbortError') {
      console.warn('Reverse geocoding timed out. Using coordinates only.')
    } else if (error.message.includes('Failed to fetch')) {
      console.warn('Network error during reverse geocoding. Using coordinates only.')
    }
  }
}

// Select location on map
const selectLocation = (locationData) => {
  selectedLocation.value = locationData

  // Remove existing marker
  if (marker.value) {
    map.value.removeLayer(marker.value)
  }

  // Add new marker
  marker.value = L.marker([locationData.lat, locationData.lon])
    .addTo(map.value)
    .bindPopup(locationData.display_name)
    .openPopup()

  // Parse address and emit location update
  parseAddressAndEmit(locationData)
}

// Parse address to extract province and district
const parseAddressAndEmit = (locationData) => {
  const address = locationData.address
  if (!address) return

  console.log('Parsing address:', address) // Debug log

  // Extract province/city - try multiple fields
  let province = ''
  if (address.state) {
    province = address.state
  } else if (address.city) {
    province = address.city
  } else if (address.town) {
    province = address.town
  } else if (address.municipality) {
    province = address.municipality
  }

  // Extract district - try multiple fields
  let district = ''
  if (address.county) {
    district = address.county
  } else if (address.suburb) {
    district = address.suburb
  } else if (address.village) {
    district = address.village
  } else if (address.hamlet) {
    district = address.hamlet
  } else if (address.neighbourhood) {
    district = address.neighbourhood
  }

  // If no district found, try to extract from display_name
  if (!district && locationData.display_name) {
    const displayName = locationData.display_name.toLowerCase()

    // Common Vietnamese district patterns
    const districtPatterns = [
      /quận\s+(\d+)/i,
      /huyện\s+([^,]+)/i,
      /thành phố\s+([^,]+)/i,
      /thị xã\s+([^,]+)/i
    ]

    for (const pattern of districtPatterns) {
      const match = displayName.match(pattern)
      if (match) {
        district = match[1].trim()
        break
      }
    }
  }

  console.log('Parsed province:', province, 'district:', district) // Debug log

  // Emit location update with parsed data
  emit('location-updated', {
    address: locationData.display_name,
    province: province,
    district: district,
    coordinates: {
      lat: locationData.lat,
      lng: locationData.lon
    },
    fullAddress: locationData
  })
}

// Get current location
const getCurrentLocation = () => {
  if (!navigator.geolocation) {
    alert('Trình duyệt không hỗ trợ định vị')
    return
  }

  isGettingLocation.value = true
  navigator.geolocation.getCurrentPosition(
    (position) => {
      const lat = position.coords.latitude
      const lng = position.coords.longitude

      map.value.setView([lat, lng], 15)
      reverseGeocode(lat, lng)
      isGettingLocation.value = false
    },
    (error) => {
      console.error('Error getting location:', error)
      alert('Không thể lấy vị trí hiện tại')
      isGettingLocation.value = false
    }
  )
}

// Confirm location
const confirmLocation = () => {
  if (selectedLocation.value) {
    searchQuery.value = selectedLocation.value.display_name
    emit('update:modelValue', selectedLocation.value.display_name)
    emit('address-selected', selectedLocation.value)
    closeMapModal()
  }
}

// Watch for modelValue changes
watch(() => props.modelValue, (newValue) => {
  searchQuery.value = newValue
})

// Lifecycle
onMounted(() => {
  // No API key needed for OpenStreetMap
})

onUnmounted(() => {
  if (searchTimeout.value) {
    clearTimeout(searchTimeout.value)
  }
})
</script>

<style scoped>
.free-map-picker {
  position: relative;
  width: 100%;
}

.address-search {
  position: relative;
}

.search-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.search-input {
  width: 100%;
  padding: 12px 50px 12px 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.search-input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.25);
}

.map-icon-button {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  background: white;
  color: black;
  border: none;
  padding: 8px;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  width: 32px;
  height: 32px;
  z-index: 10;
}

.map-icon-button:hover {
  background: #218838;
  transform: translateY(-50%) scale(1.1);
}

.map-icon-button:active {
  transform: translateY(-50%) scale(0.95);
}

.suggestions {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #ddd;
  border-top: none;
  border-radius: 0 0 8px 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  max-height: 200px;
  overflow-y: auto;
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.suggestion-item:hover {
  background: #f8f9fa;
}

.suggestion-item i {
  color: #28a745;
  font-size: 16px;
}

.suggestion-content {
  flex: 1;
}

.suggestion-main {
  font-weight: 500;
  color: #333;
  margin-bottom: 2px;
}

.suggestion-secondary {
  font-size: 12px;
  color: #666;
}

/* API Error Styles */
.api-error {
  background: #fff3cd;
  border: 1px solid #ffeaa7;
  border-radius: 8px;
  padding: 12px 16px;
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #856404;
}

.api-error i {
  color: #856404;
  font-size: 16px;
}

/* Map Modal */
.map-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 20px;
}

.map-modal {
  background: white;
  border-radius: 12px;
  width: 100%;
  max-width: 800px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.map-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.map-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.close-button {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
  padding: 4px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.close-button:hover {
  background: #f8f9fa;
}

.map-container {
  position: relative;
  flex: 1;
  min-height: 400px;
}

.map {
  width: 100%;
  height: 400px;
}

.location-loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(255, 255, 255, 0.95);
  padding: 12px 20px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #333;
  z-index: 1000;
}

.location-loading i {
  animation: spin 1s linear infinite;
  color: #007bff;
}

.map-controls {
  position: absolute;
  top: 10px;
  right: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  z-index: 1000;
}

.location-button {
  background: white;
  border: 1px solid #ddd;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
  box-shadow: 0 3px 6px rgba(0, 0, 0, 0.15);
  min-width: 120px;
  justify-content: center;
  font-weight: 500;
}

.map-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 20px;
  background: #f8f9fa;
  border-top: 1px solid #eee;
}

.cancel-button,
.confirm-button {
  padding: 12px 20px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
  font-weight: 500;
  min-width: 120px;
  justify-content: center;
}

.cancel-button {
  background: #6c757d;
  color: white;
  border: 1px solid #6c757d;
}

.cancel-button:hover {
  background: #5a6268;
}

.confirm-button {
  background: #28a745;
  color: white;
  border: 1px solid #28a745;
}

.location-button:hover:not(:disabled) {
  background: #f8f9fa;
}

.location-button:disabled {
  background: #6c757d;
  cursor: not-allowed;
  opacity: 0.6;
}

.confirm-button:hover:not(:disabled) {
  background: #218838;
}

.confirm-button:disabled {
  background: #6c757d;
  border-color: #6c757d;
  cursor: not-allowed;
}

.selected-address {
  padding: 16px 20px;
  background: #d4edda;
  border-top: 1px solid #ddd;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #155724;
}

.selected-address i {
  font-size: 16px;
}

/* Responsive */
@media (max-width: 768px) {
  .map-modal {
    margin: 10px;
    max-height: calc(100vh - 20px);
  }

  .map {
    height: 300px;
  }

  .search-input {
    padding: 12px 45px 12px 16px;
  }

  .map-icon-button {
    width: 28px;
    height: 28px;
    padding: 6px;
    font-size: 14px;
  }
}
</style>
