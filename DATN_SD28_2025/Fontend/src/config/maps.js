// Google Maps Configuration
export const GOOGLE_MAPS_CONFIG = {
  // Replace with your actual Google Maps API key
  // Get your API key from: https://console.cloud.google.com/
  // For testing, you can use a demo key (limited functionality)
  API_KEY: import.meta.env.VITE_GOOGLE_MAPS_API_KEY || 'DEMO_KEY',
  
  // Default location (Ho Chi Minh City)
  DEFAULT_LOCATION: {
    lat: 10.8231,
    lng: 106.6297
  },
  
  // Map options
  MAP_OPTIONS: {
    zoom: 15,
    mapTypeControl: true,
    streetViewControl: true,
    fullscreenControl: true,
    zoomControl: true,
    scaleControl: true
  },
  
  // Search options
  SEARCH_OPTIONS: {
    radius: 50000, // 50km radius
    language: 'vi',
    region: 'VN'
  }
}

// Instructions for getting Google Maps API key:
// 1. Go to Google Cloud Console (https://console.cloud.google.com/)
// 2. Create a new project or select existing one
// 3. Enable the following APIs:
//    - Maps JavaScript API
//    - Places API
//    - Geocoding API
// 4. Create credentials (API Key)
// 5. Restrict the API key to your domain for security
// 6. Replace 'YOUR_GOOGLE_MAPS_API_KEY' with your actual API key
