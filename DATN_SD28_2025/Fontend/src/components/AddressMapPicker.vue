<template>
  <div class="address-map-picker">
    <!-- Address Input with Search -->
    <div class="address-search">
      <div class="search-input-wrapper">
        <input
          v-model="searchQuery"
          @input="onSearchInput"
          @focus="showSuggestions = true"
          placeholder="Tìm kiếm địa chỉ..."
          class="search-input"
        />
        <button @click="openMapModal" class="map-button" type="button" :disabled="!isGoogleMapsAvailable">
          <i class="bi bi-geo-alt"></i>
          Chọn trên bản đồ
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
            <div class="suggestion-main">{{ suggestion.main_text }}</div>
            <div class="suggestion-secondary">{{ suggestion.secondary_text }}</div>
          </div>
        </div>
      </div>
      
      <!-- API Key Error -->
      <div v-if="apiKeyError" class="api-error">
        <i class="bi bi-exclamation-triangle"></i>
        <div class="error-content">
          <div class="error-title">Chế độ nhập địa chỉ thủ công</div>
          <div class="error-message">
            Google Maps API chưa được cấu hình. Bạn có thể nhập địa chỉ trực tiếp vào ô trên.
          </div>
          <div class="error-help">
            <a href="https://console.cloud.google.com/" target="_blank" class="help-link">
              <i class="bi bi-external-link"></i>
              Lấy API Key để sử dụng bản đồ
            </a>
          </div>
        </div>
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
          <div class="map-controls">
            <button @click="getCurrentLocation" class="location-button" type="button">
              <i class="bi bi-crosshair"></i>
              Vị trí hiện tại
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
          <span>{{ selectedLocation.formatted_address }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { GOOGLE_MAPS_CONFIG } from '@/config/maps.js'

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
const emit = defineEmits(['update:modelValue', 'address-selected'])

// State
const searchQuery = ref(props.modelValue)
const showSuggestions = ref(false)
const suggestions = ref([])
const showMapModal = ref(false)
const selectedLocation = ref(null)
const mapContainer = ref(null)
const map = ref(null)
const marker = ref(null)
const geocoder = ref(null)
const placesService = ref(null)
const searchTimeout = ref(null)
const isGoogleMapsAvailable = ref(false)
const apiKeyError = ref('')

// Google Maps API key from config
const GOOGLE_MAPS_API_KEY = GOOGLE_MAPS_CONFIG.API_KEY

// Initialize Google Maps
const initGoogleMaps = () => {
  if (window.google && window.google.maps) {
    return Promise.resolve()
  }

  // Check if API key is valid
  if (!GOOGLE_MAPS_API_KEY || GOOGLE_MAPS_API_KEY === 'YOUR_GOOGLE_MAPS_API_KEY' || GOOGLE_MAPS_API_KEY === 'DEMO_KEY') {
    console.warn('Google Maps API Key not configured. Using fallback mode.')
    return Promise.reject(new Error('API Key not configured'))
  }

  return new Promise((resolve, reject) => {
    const script = document.createElement('script')
    script.src = `https://maps.googleapis.com/maps/api/js?key=${GOOGLE_MAPS_API_KEY}&libraries=places&language=vi&region=VN`
    script.onload = resolve
    script.onerror = (error) => {
      console.error('Failed to load Google Maps:', error)
      reject(error)
    }
    document.head.appendChild(script)
  })
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

// Search places using Google Places API
const searchPlaces = () => {
  if (!window.google || !window.google.maps) return

  const service = new window.google.maps.places.PlacesService(document.createElement('div'))
  const request = {
    query: searchQuery.value,
    fields: ['formatted_address', 'geometry', 'name', 'place_id'],
    location: new window.google.maps.LatLng(GOOGLE_MAPS_CONFIG.DEFAULT_LOCATION.lat, GOOGLE_MAPS_CONFIG.DEFAULT_LOCATION.lng),
    radius: GOOGLE_MAPS_CONFIG.SEARCH_OPTIONS.radius
  }

  service.textSearch(request, (results, status) => {
    if (status === window.google.maps.places.PlacesServiceStatus.OK && results) {
      suggestions.value = results.slice(0, 5).map(place => ({
        place_id: place.place_id,
        main_text: place.name || place.formatted_address,
        secondary_text: place.formatted_address,
        geometry: place.geometry,
        formatted_address: place.formatted_address
      }))
    } else {
      suggestions.value = []
    }
  })
}

// Select suggestion
const selectSuggestion = (suggestion) => {
  searchQuery.value = suggestion.formatted_address
  selectedLocation.value = suggestion
  showSuggestions.value = false
  emit('update:modelValue', suggestion.formatted_address)
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

// Initialize map
const initMap = () => {
  if (!mapContainer.value || !window.google) return

  // Use default location from config
  const defaultLocation = GOOGLE_MAPS_CONFIG.DEFAULT_LOCATION
  
  map.value = new window.google.maps.Map(mapContainer.value, {
    ...GOOGLE_MAPS_CONFIG.MAP_OPTIONS,
    center: defaultLocation
  })

  geocoder.value = new window.google.maps.Geocoder()

  // Add click listener to map
  map.value.addListener('click', (event) => {
    const lat = event.latLng.lat()
    const lng = event.latLng.lng()
    
    // Reverse geocode to get address
    geocoder.value.geocode({ location: { lat, lng } }, (results, status) => {
      if (status === 'OK' && results[0]) {
        const place = {
          place_id: results[0].place_id,
          main_text: results[0].formatted_address,
          secondary_text: '',
          geometry: { location: { lat, lng } },
          formatted_address: results[0].formatted_address
        }
        
        selectLocation(place)
      }
    })
  })

  // If we have a search query, try to geocode it
  if (searchQuery.value) {
    geocoder.value.geocode({ address: searchQuery.value }, (results, status) => {
      if (status === 'OK' && results[0]) {
        const location = results[0].geometry.location
        map.value.setCenter(location)
        map.value.setZoom(15)
        
        const place = {
          place_id: results[0].place_id,
          main_text: results[0].formatted_address,
          secondary_text: '',
          geometry: { location: { lat: location.lat(), lng: location.lng() } },
          formatted_address: results[0].formatted_address
        }
        
        selectLocation(place)
      }
    })
  }
}

// Select location on map
const selectLocation = (place) => {
  selectedLocation.value = place
  
  // Remove existing marker
  if (marker.value) {
    marker.value.setMap(null)
  }
  
  // Add new marker
  marker.value = new window.google.maps.Marker({
    position: place.geometry.location,
    map: map.value,
    title: place.formatted_address,
    draggable: true
  })
  
  // Add drag listener
  marker.value.addListener('dragend', (event) => {
    const lat = event.latLng.lat()
    const lng = event.latLng.lng()
    
    geocoder.value.geocode({ location: { lat, lng } }, (results, status) => {
      if (status === 'OK' && results[0]) {
        selectedLocation.value = {
          ...place,
          formatted_address: results[0].formatted_address,
          geometry: { location: { lat, lng } }
        }
      }
    })
  })
}

// Get current location
const getCurrentLocation = () => {
  if (!navigator.geolocation) {
    alert('Trình duyệt không hỗ trợ định vị')
    return
  }

  navigator.geolocation.getCurrentPosition(
    (position) => {
      const lat = position.coords.latitude
      const lng = position.coords.longitude
      
      map.value.setCenter({ lat, lng })
      map.value.setZoom(15)
      
      geocoder.value.geocode({ location: { lat, lng } }, (results, status) => {
        if (status === 'OK' && results[0]) {
          const place = {
            place_id: results[0].place_id,
            main_text: results[0].formatted_address,
            secondary_text: '',
            geometry: { location: { lat, lng } },
            formatted_address: results[0].formatted_address
          }
          
          selectLocation(place)
        }
      })
    },
    (error) => {
      console.error('Error getting location:', error)
      alert('Không thể lấy vị trí hiện tại')
    }
  )
}

// Confirm location
const confirmLocation = () => {
  if (selectedLocation.value) {
    searchQuery.value = selectedLocation.value.formatted_address
    emit('update:modelValue', selectedLocation.value.formatted_address)
    emit('address-selected', selectedLocation.value)
    closeMapModal()
  }
}

// Watch for modelValue changes
watch(() => props.modelValue, (newValue) => {
  searchQuery.value = newValue
})

// Lifecycle
onMounted(async () => {
  try {
    await initGoogleMaps()
    isGoogleMapsAvailable.value = true
  } catch (error) {
    console.error('Error loading Google Maps:', error)
    isGoogleMapsAvailable.value = false
    if (error.message === 'API Key not configured') {
      apiKeyError.value = 'Google Maps API Key chưa được cấu hình. Vui lòng xem hướng dẫn trong file API_KEY_SETUP.md'
    } else {
      apiKeyError.value = 'Không thể tải Google Maps. Vui lòng kiểm tra kết nối mạng.'
    }
  }
})

onUnmounted(() => {
  if (searchTimeout.value) {
    clearTimeout(searchTimeout.value)
  }
})
</script>

<style scoped>
.address-map-picker {
  position: relative;
  width: 100%;
}

.address-search {
  position: relative;
}

.search-input-wrapper {
  display: flex;
  gap: 8px;
  align-items: center;
}

.search-input {
  flex: 1;
  padding: 12px 16px;
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

.map-button {
  background: #007bff;
  color: white;
  border: none;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: background-color 0.2s;
  white-space: nowrap;
}

.map-button:hover {
  background: #0056b3;
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
  color: #007bff;
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

.location-button:hover {
  background: #f8f9fa;
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
  background: #e3f2fd;
  border-top: 1px solid #ddd;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #1976d2;
}

.selected-address i {
  font-size: 16px;
}

/* API Error Styles */
.api-error {
  background: #fff3cd;
  border: 1px solid #ffeaa7;
  border-radius: 8px;
  padding: 16px;
  margin-top: 12px;
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.api-error i {
  color: #856404;
  font-size: 20px;
  margin-top: 2px;
}

.error-content {
  flex: 1;
}

.error-title {
  font-weight: 600;
  color: #856404;
  margin-bottom: 4px;
}

.error-message {
  font-size: 14px;
  color: #856404;
  margin-bottom: 8px;
}

.error-help {
  margin-top: 8px;
}

.help-link {
  color: #007bff;
  text-decoration: none;
  font-size: 14px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: color 0.2s;
}

.help-link:hover {
  color: #0056b3;
  text-decoration: underline;
}

.map-button:disabled {
  background: #6c757d;
  cursor: not-allowed;
  opacity: 0.6;
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
  
  .search-input-wrapper {
    flex-direction: column;
  }
  
  .map-button {
    width: 100%;
    justify-content: center;
  }
}
</style>
