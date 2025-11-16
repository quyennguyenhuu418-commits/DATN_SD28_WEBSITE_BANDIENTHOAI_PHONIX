import axios from 'axios'

// GHN Configuration
const GHN_TOKEN = '78b1bc64-ad9d-11f0-9a0f-be0ce4355cc0'
const BASE_URL = 'https://online-gateway.ghn.vn/shiip/public-api'
const FROM_DISTRICT_ID = 1442 // Gò Vấp

class ShippingService {
  constructor() {
    this.provinces = []
    this.districts = []
    this.cache = new Map() // Cache for calculated fees
  }

  // Load provinces on initialization
  async loadProvinces() {
    if (this.provinces.length > 0) return this.provinces

    try {
      const response = await axios.get(`${BASE_URL}/master-data/province`, {
        headers: {
          Token: GHN_TOKEN,
          'Content-Type': 'application/json'
        }
      })
      this.provinces = response.data.data
      return this.provinces
    } catch (error) {
      console.error('Error loading provinces:', error)
      return []
    }
  }

  // Load districts by province
  async loadDistricts(provinceId) {
    if (!provinceId) return []

    try {
      const response = await axios.post(
        `${BASE_URL}/master-data/district`,
        { province_id: Number(provinceId) },
        {
          headers: {
            Token: GHN_TOKEN,
            'Content-Type': 'application/json'
          }
        }
      )
      this.districts = response.data.data
      return this.districts
    } catch (error) {
      console.error('Error loading districts:', error)
      return []
    }
  }

  // Calculate shipping fee based on province, district and weight
  async calculateShippingFee(provinceId, districtId, weight = 1000) {
    const cacheKey = `${provinceId}-${districtId}-${weight}`
    
    // Check cache first
    if (this.cache.has(cacheKey)) {
      return this.cache.get(cacheKey)
    }

    try {
      if (!provinceId || !districtId) {
        return this.getDefaultShippingRates()
      }

      // Calculate express fee first
      const expressFee = this.calculateExpressFee(provinceId, districtId, weight)
      
      // Calculate GHN fee
      const ghnFee = await this.calculateGHNFee(districtId, weight)
      
      // Calculate different shipping options
      const shippingRates = {
        standard: {
          name: 'Giao hàng tiêu chuẩn',
          description: 'Giao hàng trong 5-7 ngày',
          price: 0, // Free for standard
          estimatedDays: '5-7 ngày'
        },
        express: {
          name: 'Giao hàng nhanh',
          description: 'Giao hàng trong 2-3 ngày làm việc',
          price: expressFee,
          estimatedDays: '2-3 ngày'
        },
        ghn: {
          name: 'Ship hỏa tốc',
          description: 'Giao hàng hỏa tốc',
          price: Math.max(ghnFee, expressFee + 10000), // Ensure GHN is more expensive than express
          estimatedDays: '1-2 ngày',
          provider: 'GHN'
        }
      }

      // Cache the result
      this.cache.set(cacheKey, shippingRates)
      return shippingRates

    } catch (error) {
      console.error('Error calculating shipping fee:', error)
      return this.getDefaultShippingRates()
    }
  }

  // Parse address to extract province and district
  async parseAddress(address) {
    if (!address) return { provinceId: null, districtId: null }

    // Load provinces if not already loaded
    await this.loadProvinces()

    // Simple address parsing - you might want to improve this
    const addressLower = address.toLowerCase()
    
    // Find province
    let provinceId = null
    for (const province of this.provinces) {
      if (addressLower.includes(province.ProvinceName.toLowerCase())) {
        provinceId = province.ProvinceID
        break
      }
    }

    if (!provinceId) {
      // Default to Ho Chi Minh City if not found
      const hcmc = this.provinces.find(p => p.ProvinceName.includes('Hồ Chí Minh'))
      provinceId = hcmc ? hcmc.ProvinceID : null
    }

    if (!provinceId) return { provinceId: null, districtId: null }

    // Load districts for the province
    await this.loadDistricts(provinceId)

    // Find district
    let districtId = null
    for (const district of this.districts) {
      if (addressLower.includes(district.DistrictName.toLowerCase())) {
        districtId = district.DistrictID
        break
      }
    }

    return { provinceId, districtId }
  }

  // Calculate GHN fee
  async calculateGHNFee(districtId, weight) {
    try {
      const response = await axios.post(
        `${BASE_URL}/v2/shipping-order/fee`,
        {
          service_id: 53320,
          insurance_value: 100000,
          coupon: null,
          from_district_id: FROM_DISTRICT_ID,
          to_district_id: Number(districtId),
          weight: Number(weight),
          length: 20,
          width: 10,
          height: 10
        },
        {
          headers: {
            Token: GHN_TOKEN,
            'Content-Type': 'application/json'
          }
        }
      )

      return response.data.data.total
      
    } catch (error) {
      console.error('Error calculating GHN fee:', error)
      // Return a fee that's more expensive than express default
      return 45000 // Increased default fee
    }
  }

  // Calculate express fee based on distance/region
  calculateExpressFee(provinceId, districtId, weight) {
    // Simple pricing logic - you can customize this
    const baseWeight = Math.max(weight, 1000) // Minimum 1kg
    
    // Base fee by weight
    let fee = 0
    if (baseWeight <= 1000) {
      fee = 25000 // Increased base price
    } else if (baseWeight <= 2000) {
      fee = 35000
    } else if (baseWeight <= 3000) {
      fee = 45000
    } else {
      fee = 55000 + Math.ceil((baseWeight - 3000) / 1000) * 10000
    }

    // Add distance factor (simplified)
    // You can implement more sophisticated distance calculation
    const distanceMultiplier = this.getDistanceMultiplier(provinceId, districtId)
    fee = Math.round(fee * distanceMultiplier)

    return fee
  }

  // Get distance multiplier based on province/district
  getDistanceMultiplier(provinceId, districtId) {
    // Simplified distance calculation
    // In a real app, you'd use actual distance calculation
    const hcmcProvinceId = 202 // Ho Chi Minh City
    
    if (provinceId === hcmcProvinceId) {
      return 1.0 // Same city
    } else if (this.isNearbyProvince(provinceId)) {
      return 1.2 // Nearby provinces
    } else {
      return 1.5 // Far provinces
    }
  }

  // Check if province is nearby (simplified)
  isNearbyProvince(provinceId) {
    const nearbyProvinces = [201, 203, 204, 205] // Example nearby province IDs
    return nearbyProvinces.includes(provinceId)
  }

  // Get default shipping rates when calculation fails
  getDefaultShippingRates() {
    return {
      standard: {
        name: 'Giao hàng tiêu chuẩn',
        description: 'Giao hàng trong 5-7 ngày',
        price: 0,
        estimatedDays: '5-7 ngày'
      },
      express: {
        name: 'Giao hàng nhanh',
        description: 'Giao hàng trong 2-3 ngày làm việc',
        price: 30000,
        estimatedDays: '2-3 ngày'
      },
      ghn: {
        name: 'Ship hỏa tốc',
        description: 'Giao hàng hỏa tốc',
        price: 45000, // Increased to be more expensive than express
        estimatedDays: '1-2 ngày',
        provider: 'GHN'
      }
    }
  }

  // Clear cache
  clearCache() {
    this.cache.clear()
  }
}

// Export singleton instance
export default new ShippingService()






