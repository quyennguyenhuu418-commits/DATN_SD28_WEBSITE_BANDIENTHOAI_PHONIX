<template>
  <div class="dathang-page">
    <!-- Header -->
    <HeaderLayout/>

    <!-- Main Content -->
    <main class="dathang-main" style="margin-top: 80px;">
      <div class="container">
        
        <!-- Order Success Message -->
        <div v-if="orderSuccess.show" class="order-success-overlay">
          <div class="order-success-card">
            <div class="success-icon">
              <i class="bi bi-check-circle-fill"></i>
            </div>
            <h1 class="success-title">Đặt hàng thành công!</h1>
            <p class="success-message">
              Cảm ơn bạn đã đặt hàng tại PhoniX. Đơn hàng của bạn đang được xử lý.
            </p>
            <div class="order-id">
              Mã đơn hàng: <strong>{{ orderSuccess.orderId }}</strong>
            </div>
            <div class="success-actions">
              <button @click="orderSuccess.show = false" class="btn-continue">
                <i class="bi bi-arrow-left"></i>
                Tiếp tục mua sắm
              </button>
              <button @click="trackOrder" class="btn-track">
                <i class="bi bi-search"></i>
                Theo dõi đơn hàng
              </button>
            </div>
          </div>
        </div>
        <div class="page-header">
          <h1>Đặt hàng</h1>
          <p>Hoàn tất thông tin để đặt hàng</p>
          
          <!-- User Login Status -->
          <div v-if="isLoggedIn" class="user-status">
            <div class="user-info">
              <i class="bi bi-person-check-fill"></i>
              <span>Xin chào, <strong>{{ userInfo?.fullName || userInfo?.tenKhachHang || 'Khách hàng' }}</strong></span>
            </div>
            <div class="login-benefits">
              <i class="bi bi-check-circle"></i>
              <span>Thông tin đã được tự động điền từ tài khoản</span>
            </div>
          </div>
          
          <div v-else class="guest-status">
            <div class="guest-info">
              <i class="bi bi-person"></i>
              <span>Bạn đang đặt hàng với tư cách khách</span>
            </div>
            <div class="login-prompt">
              <i class="bi bi-info-circle"></i>
              <span>Đăng nhập để tự động điền thông tin và lưu địa chỉ</span>
            </div>
          </div>
        </div>

        <div class="dathang-layout">
          <!-- Order Form -->
          <div class="order-form">
            <div class="form-section">
              <h2 class="section-title">
                <i class="bi bi-person"></i>
                Thông tin khách hàng
              </h2>
              
              <!-- Address Selection for Logged-in Users -->
              <div v-if="isLoggedIn && userAddresses.length > 0" class="address-selection">
                <h3 class="subsection-title">
                  <i class="bi bi-geo-alt"></i>
                  Chọn địa chỉ giao hàng
                </h3>
                
                <div class="address-options">
                  <label 
                    v-for="address in userAddresses" 
                    :key="address.id"
                    class="address-option"
                    :class="{ 'selected': selectedAddressId === address.id }"
                  >
                    <input
                      type="radio"
                      :value="address.id"
                      v-model="selectedAddressId"
                      @change="selectUserAddress(address)"
                      name="userAddress"
                    >
                    <div class="address-content">
                      <div class="address-header">
                        <span class="address-name">{{ address.fullName }}</span>
                        <span v-if="address.isDefault" class="default-badge">Mặc định</span>
                      </div>
                      <div class="address-details">
                        <div class="address-phone">
                          <i class="bi bi-telephone"></i>
                          {{ address.phone }}
                        </div>
                        <div class="address-location">
                          <i class="bi bi-geo-alt"></i>
                          {{ address.address }}, {{ address.districtName }}, {{ address.provinceName }}
                        </div>
                      </div>
                    </div>
                  </label>
                </div>
                
                <div class="address-actions">
                  <button type="button" class="btn-add-address">
                    <i class="bi bi-plus-circle"></i>
                    Thêm địa chỉ mới
                  </button>
                </div>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label for="fullName">Họ và tên *</label>
                  <input
                    type="text"
                    id="fullName"
                    v-model="orderForm.fullName"
                    placeholder="Nhập họ và tên"
                    :class="{ 'error': errors.fullName }"
                  >
                  <span v-if="errors.fullName" class="error-message">{{ errors.fullName }}</span>
                </div>

                <div class="form-group">
                  <label for="phone">Số điện thoại *</label>
                  <input
                    type="tel"
                    id="phone"
                    v-model="orderForm.phone"
                    placeholder="Nhập số điện thoại"
                    :class="{ 'error': errors.phone }"
                  >
                  <span v-if="errors.phone" class="error-message">{{ errors.phone }}</span>
                </div>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label for="email">Email</label>
                  <input
                    type="email"
                    id="email"
                    v-model="orderForm.email"
                    placeholder="Nhập email (không bắt buộc)"
                  >
                </div>

                <div class="form-group">
                  <label for="province">Tỉnh/Thành phố *</label>
                  <select
                    id="province"
                    v-model="orderForm.province"
                    @change="onProvinceChange"
                    :class="{ 'error': errors.province }"
                    :disabled="isLoadingProvinces"
                  >
                    <option value="">Chọn tỉnh/thành phố</option>
                    <option
                      v-for="province in provinces"
                      :key="province.ProvinceID"
                      :value="province.ProvinceID"
                    >
                      {{ province.ProvinceName }}
                    </option>
                  </select>
                  <span v-if="errors.province" class="error-message">{{ errors.province }}</span>
                </div>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label for="district">Quận/Huyện *</label>
                  <select
                    id="district"
                    v-model="orderForm.district"
                    @change="onDistrictChange"
                    :class="{ 'error': errors.district }"
                    :disabled="!orderForm.province || isLoadingDistricts"
                  >
                    <option value="">Chọn quận/huyện</option>
                    <option
                      v-for="district in districts"
                      :key="district.DistrictID"
                      :value="district.DistrictID"
                    >
                      {{ district.DistrictName }}
                    </option>
                  </select>
                  <span v-if="errors.district" class="error-message">{{ errors.district }}</span>
                </div>

                <div class="form-group">
                  <label for="address">Địa chỉ cụ thể *</label>
                  <FreeMapPicker
                    v-model="orderForm.address"
                    placeholder="Số nhà, tên đường, phường/xã hoặc chọn trên bản đồ"
                    @address-selected="onAddressSelected"
                    @location-updated="onLocationUpdated"
                  />
                  <span v-if="errors.address" class="error-message">{{ errors.address }}</span>
                </div>
              </div>
            </div>

            <div class="form-section">
              <h2 class="section-title">
                <i class="bi bi-truck"></i>
                Phương thức giao hàng
              </h2>

              <div class="delivery-options">
                <label class="delivery-option" v-for="option in deliveryOptions" :key="option.id">
                  <input
                    type="radio"
                    :value="option.id"
                    v-model="orderForm.deliveryMethod"
                    name="delivery"
                  >
                  <div class="option-content">
                    <div class="option-header">
                      <span class="option-name">{{ option.name }}</span>
                      <span class="option-price">{{ option.price === 0 ? 'Miễn phí' : formatPrice(option.price) }}</span>
                    </div>
                    <p class="option-description">{{ option.description }}</p>
                  </div>
                </label>
              </div>

              <!-- Shipping calculation status -->
              <div v-if="isCalculatingShipping" class="shipping-status">
                <i class="bi bi-hourglass-split"></i>
                <span>Đang tính phí vận chuyển...</span>
              </div>

              <div v-if="shippingError" class="shipping-error">
                <i class="bi bi-exclamation-triangle"></i>
                <span>{{ shippingError }}</span>
              </div>
            </div>

            <div class="form-section">
              <h2 class="section-title">
                <i class="bi bi-credit-card"></i>
                Phương thức thanh toán
              </h2>

              <div class="payment-options">
                <label 
                  class="payment-option" 
                  v-for="option in paymentOptions" 
                  :key="option.id"
                  :class="{ 
                    'popular': option.popular, 
                    'unavailable': !option.available,
                    'selected': orderForm.paymentMethod === option.id
                  }"
                >
                  <input
                    type="radio"
                    :value="option.id"
                    v-model="orderForm.paymentMethod"
                    name="payment"
                    :disabled="!option.available"
                  >
                  <div class="option-content">
                    <div class="option-header">
                      <div class="option-main">
                    <i :class="option.icon"></i>
                    <span class="option-name">{{ option.name }}</span>
                        <span v-if="option.popular" class="popular-badge">Phổ biến</span>
                      </div>
                      <div class="option-fee">
                        <span v-if="option.fee > 0" class="fee-amount">{{ formatPrice(option.fee) }}</span>
                        <span v-else class="free-text">Miễn phí</span>
                      </div>
                    </div>
                    
                    <div class="option-details">
                      <p class="option-description">{{ option.description }}</p>
                      
                      <div class="option-meta">
                        <div class="meta-item">
                          <i class="bi bi-clock"></i>
                          <span>{{ option.processingTime }}</span>
                        </div>
                        <div class="meta-item">
                          <i class="bi bi-shield-check"></i>
                          <span>{{ option.security }}</span>
                        </div>
                      </div>
                    </div>
                    
                    <!-- Bank Transfer Details -->
                    <div v-if="option.id === 'bank-transfer' && orderForm.paymentMethod === 'bank-transfer'" class="bank-details">
                      <h4>Thông tin chuyển khoản:</h4>
                      <div class="bank-info">
                        <div class="bank-row">
                          <span class="bank-label">Ngân hàng:</span>
                          <span class="bank-value">{{ option.bankInfo.bankName }}</span>
                        </div>
                        <div class="bank-row">
                          <span class="bank-label">Số tài khoản:</span>
                          <span class="bank-value">{{ option.bankInfo.accountNumber }}</span>
                          <button class="copy-btn" @click="copyToClipboard(option.bankInfo.accountNumber)">
                            <i class="bi bi-copy"></i>
                          </button>
                        </div>
                        <div class="bank-row">
                          <span class="bank-label">Chủ tài khoản:</span>
                          <span class="bank-value">{{ option.bankInfo.accountHolder }}</span>
                        </div>
                        <div class="bank-row">
                          <span class="bank-label">Chi nhánh:</span>
                          <span class="bank-value">{{ option.bankInfo.branch }}</span>
                        </div>
                      </div>
                      <div class="bank-note">
                        <i class="bi bi-info-circle"></i>
                        <span>Vui lòng ghi nội dung: "Đặt hàng PhoniX - [SĐT]" khi chuyển khoản</span>
                      </div>
                    </div>
                    
                    <!-- MoMo QR Code -->
                    <div v-if="option.id === 'momo' && orderForm.paymentMethod === 'momo'" class="momo-details">
                      <h4>Quét mã QR để thanh toán:</h4>
                      <div class="qr-container">
                        <img :src="option.qrCode" alt="MoMo QR Code" class="qr-code">
                        <p class="qr-note">Quét mã QR bằng ứng dụng MoMo để thanh toán</p>
                      </div>
                    </div>
                  </div>
                </label>
              </div>
              
              <!-- Payment Security Notice -->
              <div class="payment-security">
                <i class="bi bi-shield-check"></i>
                <div class="security-content">
                  <h4>Bảo mật thanh toán</h4>
                  <p>Thông tin thanh toán của bạn được mã hóa và bảo mật tuyệt đối. Chúng tôi không lưu trữ thông tin thẻ tín dụng.</p>
                </div>
              </div>
            </div>

            <div class="form-section">
              <h2 class="section-title">
                <i class="bi bi-chat-text"></i>
                Ghi chú đơn hàng
              </h2>

              <div class="form-group full-width">
                <textarea
                  v-model="orderForm.note"
                  placeholder="Ghi chú thêm cho đơn hàng (không bắt buộc)"
                  rows="3"
                ></textarea>
              </div>
            </div>
          </div>

          <!-- Order Summary -->
          <div class="order-summary">
            <h3 class="summary-title">Tóm tắt đơn hàng</h3>

            <div class="order-items">
              <div v-for="item in selectedItems" :key="item.chiTietSanPhamId" class="order-item">
                <div class="item-image">
                  <img :src="getProductImage(item.hinhAnh)" :alt="item.tenSanPham">
                </div>
                <div class="item-info">
                  <h4 class="item-name">{{ item.tenSanPham }}</h4>
                  <div class="item-specs">
                    <span v-if="item.tenRam">{{ item.tenRam }}</span>
                    <span v-if="item.tenRom">{{ item.tenRom }}</span>
                    <span v-if="item.tenMauSac">{{ item.tenMauSac }}</span>
                  </div>
                  <div class="item-quantity">Số lượng: {{ item.quantity }}</div>
                </div>
                <div class="item-price">{{ formatPrice(item.gia * item.quantity) }}</div>
              </div>
            </div>

            <div class="summary-divider"></div>

            <div class="summary-row">
              <span>Tạm tính:</span>
              <span class="summary-value">{{ formatPrice(selectedItems.reduce((s,i)=> s + (i.gia * i.quantity), 0)) }}</span>
            </div>

            <div class="summary-row">
              <span>Phí vận chuyển:</span>
              <span class="summary-value">{{ getDeliveryPrice() }}</span>
            </div>

            <!-- Shipping method info -->
            <div v-if="orderForm.deliveryMethod && deliveryOptions.find(o => o.id === orderForm.deliveryMethod)" class="summary-row shipping-info">
              <span class="shipping-details">
                <i class="bi bi-truck"></i>
                {{ deliveryOptions.find(o => o.id === orderForm.deliveryMethod)?.estimatedDays }}
                <span v-if="deliveryOptions.find(o => o.id === orderForm.deliveryMethod)?.provider">
                  - {{ deliveryOptions.find(o => o.id === orderForm.deliveryMethod)?.provider }}
                </span>
              </span>
            </div>

            <div class="summary-divider"></div>

            <div class="summary-row total">
              <span>Tổng cộng:</span>
              <span class="summary-total">{{ formatPrice(getTotalPrice()) }}</span>
            </div>

            <button class="btn-place-order" @click="placeOrder" :disabled="!isFormValid">
              <i class="bi bi-check-circle"></i>
              Đặt hàng ngay
            </button>

            <div class="security-info">
              <i class="bi bi-shield-check"></i>
              <span>Thông tin của bạn được bảo mật và mã hóa</span>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- Footer -->
    <footer class="dathang-footer">
      <FooterLayout/>
    </footer>

    <!-- Toast -->
    <transition name="toast">
      <div v-if="toast.show" class="toast-notification" :class="toast.type">
        <i class="bi" :class="toast.icon"></i>
        <span>{{ toast.message }}</span>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cartStore'
import HeaderLayout from '@/views/Website/HeaderLayout.vue'
import FooterLayout from '@/views/Website/FooterLayout.vue'
import FreeMapPicker from '@/components/FreeMapPicker.vue'
import shippingService from '@/services/shippingService.js'

const router = useRouter()
const cartStore = useCartStore()
// Local list of selected items for checkout (do not mutate full cart)
const selectedItems = ref([])

const API_BASE_URL = 'http://localhost:8080'

// Form data
const orderForm = ref({
  fullName: '',
  phone: '',
  email: '',
  province: '',
  district: '',
  address: '',
  deliveryMethod: 'standard',
  paymentMethod: 'cod',
  note: ''
})

// User authentication state
const isLoggedIn = ref(false)
const userInfo = ref(null)
const userAddresses = ref([])
const selectedAddressId = ref(null)

// Form validation errors
const errors = ref({})

// Address selection state
const provinces = ref([])
const districts = ref([])
const isLoadingProvinces = ref(false)
const isLoadingDistricts = ref(false)

// Shipping calculation state
const shippingRates = ref({})
const isCalculatingShipping = ref(false)
const shippingError = ref('')

// Delivery options - will be updated dynamically based on address
const deliveryOptions = ref([
  {
    id: 'standard',
    name: 'Giao hàng tiêu chuẩn',
    description: 'Giao hàng trong 5-7 ngày',
    price: 0,
    estimatedDays: '5-7 ngày'
  },
  {
    id: 'express',
    name: 'Giao hàng nhanh',
    description: 'Giao hàng trong 2-3 ngày làm việc',
    price: 0, // Will be calculated based on address
    estimatedDays: '2-3 ngày'
  },
  {
    id: 'ghn',
    name: 'Ship hỏa tốc',
    description: 'Giao hàng hỏa tốc',
    price: 0, // Will be calculated based on address
    estimatedDays: '1-2 ngày',
    provider: 'GHN'
  }
])

// Payment options
const paymentOptions = ref([
  {
    id: 'cod',
    name: 'Thanh toán khi nhận hàng (COD)',
    description: 'Thanh toán bằng tiền mặt khi nhận hàng',
    icon: 'bi bi-cash',
    available: true,
    fee: 0,
    processingTime: 'Ngay khi nhận hàng',
    security: 'An toàn tuyệt đối',
    popular: true
  },
  {
    id: 'bank-transfer',
    name: 'Chuyển khoản ngân hàng',
    description: 'Chuyển khoản trước khi giao hàng',
    icon: 'bi bi-bank',
    available: true,
    fee: 0,
    processingTime: '1-2 ngày làm việc',
    security: 'Bảo mật cao',
    popular: false,
    bankInfo: {
      bankName: 'Vietcombank',
      accountNumber: '1234567890',
      accountHolder: 'CÔNG TY TNHH PHONIX',
      branch: 'Chi nhánh Hà Nội'
    }
  },
  {
    id: 'momo',
    name: 'Ví MoMo',
    description: 'Thanh toán qua ví điện tử MoMo',
    icon: 'bi bi-phone',
    available: true,
    fee: 0,
    processingTime: 'Ngay lập tức',
    security: 'Bảo mật cao',
    popular: true,
    qrCode: '/QR.png'
  },
  {
    id: 'vnpay',
    name: 'VNPay',
    description: 'Thanh toán qua cổng VNPay',
    icon: 'bi bi-credit-card',
    available: true,
    fee: 0,
    processingTime: 'Ngay lập tức',
    security: 'Bảo mật cao',
    popular: false
  }
])

// Toast notification
const toast = ref({
  show: false,
  type: 'success',
  message: '',
  icon: 'bi-check-circle-fill'
})

// Order success state
const orderSuccess = ref({
  show: false,
  orderId: ''
})

// Computed properties
const isFormValid = computed(() => {
  return orderForm.value.fullName &&
    orderForm.value.phone &&
    orderForm.value.email &&
    orderForm.value.province &&
    orderForm.value.district &&
    orderForm.value.address &&
    orderForm.value.deliveryMethod &&
    orderForm.value.paymentMethod
})

// Methods
const checkUserLogin = () => {
  // Check localStorage for user token/info
  const userToken = localStorage.getItem('user_token')
  const userData = localStorage.getItem('user_data')
  
  if (userToken && userData) {
    try {
      isLoggedIn.value = true
      userInfo.value = JSON.parse(userData)
      
      // Auto-fill form with user data
      if (userInfo.value) {
        orderForm.value.fullName = userInfo.value.fullName || userInfo.value.tenKhachHang || ''
        orderForm.value.phone = userInfo.value.phone || userInfo.value.soDienThoai || ''
        orderForm.value.email = userInfo.value.email || userInfo.value.emailKhachHang || ''
      }
      
      // Load user addresses
      loadUserAddresses()
      
    } catch (error) {
      console.error('Error parsing user data:', error)
      isLoggedIn.value = false
      userInfo.value = null
    }
  } else {
    isLoggedIn.value = false
    userInfo.value = null
  }
}

// Load user addresses from localStorage or API
const loadUserAddresses = () => {
  const savedAddresses = localStorage.getItem('user_addresses')
  
  if (savedAddresses) {
    try {
      userAddresses.value = JSON.parse(savedAddresses)
      
      // Set default address if available
      const defaultAddress = userAddresses.value.find(addr => addr.isDefault)
      if (defaultAddress) {
        selectUserAddress(defaultAddress)
      }
    } catch (error) {
      console.error('Error parsing user addresses:', error)
      userAddresses.value = []
    }
  } else {
    // Mock data for demonstration - replace with actual API call
    userAddresses.value = [
      {
        id: 1,
        fullName: userInfo.value?.fullName || 'Nguyễn Văn A',
        phone: userInfo.value?.phone || '0123456789',
        province: 202,
        district: 1442,
        address: '123 Đường ABC, Phường XYZ',
        isDefault: true,
        provinceName: 'Hồ Chí Minh',
        districtName: 'Quận 1'
      },
      {
        id: 2,
        fullName: userInfo.value?.fullName || 'Nguyễn Văn A',
        phone: userInfo.value?.phone || '0123456789',
        province: 201,
        district: 1444,
        address: '456 Đường DEF, Phường UVW',
        isDefault: false,
        provinceName: 'Hà Nội',
        districtName: 'Quận Ba Đình'
      }
    ]
  }
}

// Select user address
const selectUserAddress = (address) => {
  selectedAddressId.value = address.id
  
  // Update form with selected address
  orderForm.value.fullName = address.fullName
  orderForm.value.phone = address.phone
  orderForm.value.province = address.province
  orderForm.value.district = address.district
  orderForm.value.address = address.address
  
  // Recalculate shipping
  calculateShippingRates()
}

const formatPrice = (price) => {
  if (!price) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

const getProductImage = (imagePath) => {
  const placeholderSVG = 'data:image/svg+xml;charset=utf-8,%3Csvg xmlns="http://www.w3.org/2000/svg" width="300" height="300"%3E%3Crect width="300" height="300" fill="%23f0f0f0"/%3E%3Ctext x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" fill="%23999" font-family="Arial" font-size="16"%3ENo Image%3C/text%3E%3C/svg%3E'

  if (!imagePath) return placeholderSVG
  if (imagePath.startsWith('http')) return imagePath
  return `${API_BASE_URL}${imagePath.startsWith('/') ? '' : '/'}${imagePath}`
}

const getDeliveryPrice = () => {
  const selectedDelivery = deliveryOptions.value.find(option => option.id === orderForm.value.deliveryMethod)

  if (!selectedDelivery) return 'Miễn phí'

  if (isCalculatingShipping.value) {
    return 'Đang tính...'
  }

  if (shippingError.value) {
    return 'Lỗi tính phí'
  }

  return selectedDelivery.price === 0 ? 'Miễn phí' : formatPrice(selectedDelivery.price)
}

const getTotalPrice = () => {
  const selectedDelivery = deliveryOptions.value.find(option => option.id === orderForm.value.deliveryMethod)
  const deliveryPrice = selectedDelivery ? selectedDelivery.price : 0
  const itemsTotal = selectedItems.value.reduce((sum, item) => sum + (item.gia * item.quantity), 0)
  return itemsTotal + deliveryPrice
}

// Calculate total weight for shipping
const calculateTotalWeight = () => {
  // Estimate weight based on product count (assuming average 200g per item)
  const baseWeight = selectedItems.value.reduce((sum, item) => sum + item.quantity, 0) * 200
  return Math.max(baseWeight, 1000) // Minimum 1kg
}

// Load provinces
const loadProvinces = async () => {
  if (provinces.value.length > 0) return

  isLoadingProvinces.value = true
  try {
    const data = await shippingService.loadProvinces()
    provinces.value = data
  } catch (error) {
    console.error('Error loading provinces:', error)
  } finally {
    isLoadingProvinces.value = false
  }
}

// Handle province change
const onProvinceChange = async () => {
  orderForm.value.district = '' // Reset district selection
  districts.value = [] // Clear districts

  if (orderForm.value.province) {
    isLoadingDistricts.value = true
    try {
      const data = await shippingService.loadDistricts(orderForm.value.province)
      districts.value = data
    } catch (error) {
      console.error('Error loading districts:', error)
    } finally {
      isLoadingDistricts.value = false
    }
  }

  // Recalculate shipping when province changes
  if (orderForm.value.address) {
    calculateShippingRates()
  }
}

// Handle district change
const onDistrictChange = () => {
  // Recalculate shipping when district changes
  if (orderForm.value.address) {
    calculateShippingRates()
  }
}

// Handle address selected from map
const onAddressSelected = (addressData) => {
  // Update the address field
  orderForm.value.address = addressData.formatted_address

  // Try to extract province and district from the address
  if (addressData.geometry && addressData.geometry.location) {
    // Use reverse geocoding to get more detailed address info
    reverseGeocodeAddress(addressData.geometry.location)
  }

  // Recalculate shipping
  calculateShippingRates()
}

// Handle location updated from map (with province/district info)
const onLocationUpdated = async (locationData) => {
  // Update the address field
  orderForm.value.address = locationData.address

  let updatedFields = []

  // Try to find and set province
  if (locationData.province) {
    const province = provinces.value.find(p =>
      p.ProvinceName.toLowerCase().includes(locationData.province.toLowerCase()) ||
      locationData.province.toLowerCase().includes(p.ProvinceName.toLowerCase())
    )

    if (province) {
      orderForm.value.province = province.ProvinceID
      updatedFields.push(`Tỉnh/Thành phố: ${province.ProvinceName}`)

      // Load districts for the selected province
      await onProvinceChange()

      // Try to find and set district
      if (locationData.district && districts.value.length > 0) {
        console.log('Looking for district:', locationData.district) // Debug log
        console.log('Available districts:', districts.value.map(d => d.DistrictName)) // Debug log

        // Try multiple matching strategies
        let district = null

        // Strategy 1: Exact match
        district = districts.value.find(d =>
          d.DistrictName.toLowerCase() === locationData.district.toLowerCase()
        )

        // Strategy 2: Contains match
        if (!district) {
          district = districts.value.find(d =>
            d.DistrictName.toLowerCase().includes(locationData.district.toLowerCase()) ||
            locationData.district.toLowerCase().includes(d.DistrictName.toLowerCase())
          )
        }

        // Strategy 3: Remove common prefixes and try again
        if (!district) {
          const cleanDistrict = locationData.district
            .replace(/^quận\s*/i, '')
            .replace(/^huyện\s*/i, '')
            .replace(/^thành phố\s*/i, '')
            .replace(/^thị xã\s*/i, '')
            .replace(/^phường\s*/i, '')
            .replace(/^xã\s*/i, '')
            .trim()

          district = districts.value.find(d =>
            d.DistrictName.toLowerCase().includes(cleanDistrict.toLowerCase()) ||
            cleanDistrict.toLowerCase().includes(d.DistrictName.toLowerCase())
          )
        }

        // Strategy 4: Partial matching for complex names
        if (!district) {
          const searchTerm = locationData.district.toLowerCase()
          district = districts.value.find(d => {
            const districtName = d.DistrictName.toLowerCase()

            // Check if any significant part of the search term matches
            const searchWords = searchTerm.split(/\s+/).filter(word => word.length > 2)
            const districtWords = districtName.split(/\s+/).filter(word => word.length > 2)

            // Check if any search word is contained in district name
            return searchWords.some(searchWord =>
              districtWords.some(districtWord =>
                districtWord.includes(searchWord) || searchWord.includes(districtWord)
              )
            )
          })
        }

        // Strategy 5: Special cases for common mismatches
        if (!district) {
          const searchTerm = locationData.district.toLowerCase()

          // Special case: "Từ Liêm" should match "Nam Từ Liêm" or "Bắc Từ Liêm"
          if (searchTerm.includes('từ liêm')) {
            // Prefer "Nam Từ Liêm" over "Bắc Từ Liêm" for "Phường Từ Liêm"
            district = districts.value.find(d =>
              d.DistrictName.toLowerCase().includes('nam từ liêm')
            ) || districts.value.find(d =>
              d.DistrictName.toLowerCase().includes('từ liêm')
            )
          }

          // Special case: "Cầu Giấy" variations
          if (searchTerm.includes('cầu giấy') || searchTerm.includes('cau giay')) {
            district = districts.value.find(d =>
              d.DistrictName.toLowerCase().includes('cầu giấy')
            )
          }

          // Special case: "Hoàn Kiếm" variations
          if (searchTerm.includes('hoàn kiếm') || searchTerm.includes('hoan kiem')) {
            district = districts.value.find(d =>
              d.DistrictName.toLowerCase().includes('hoàn kiếm')
            )
          }
        }

        if (district) {
          orderForm.value.district = district.DistrictID
          updatedFields.push(`Quận/Huyện: ${district.DistrictName}`)
          console.log('Found district:', district.DistrictName) // Debug log
        } else {
          console.log('No district found for:', locationData.district) // Debug log
        }
      }
    }
  }

  // Show success message if fields were updated
  if (updatedFields.length > 0) {
    showToast('success', `Đã tự động cập nhật: ${updatedFields.join(', ')}`, 'bi-geo-alt-fill')
  } else if (locationData.province || locationData.district) {
    // Show debug info if parsing failed
    const debugInfo = []
    if (locationData.province) debugInfo.push(`Tỉnh: ${locationData.province}`)
    if (locationData.district) debugInfo.push(`Quận: ${locationData.district}`)
    showToast('info', `Không tìm thấy phù hợp: ${debugInfo.join(', ')}. Vui lòng chọn thủ công.`, 'bi-info-circle')
  }

  // Recalculate shipping
  calculateShippingRates()
}

// Reverse geocode to get detailed address info
const reverseGeocodeAddress = async (location) => {
  try {
    // This would use Google Geocoding API to get detailed address components
    // For now, we'll just trigger shipping calculation
    calculateShippingRates()
  } catch (error) {
    console.error('Error reverse geocoding:', error)
  }
}

// Calculate shipping rates based on address
const calculateShippingRates = async () => {
  if (!orderForm.value.address || !orderForm.value.province || !orderForm.value.district) {
    return
  }

  isCalculatingShipping.value = true
  shippingError.value = ''

  try {
    const weight = calculateTotalWeight()

    const rates = await shippingService.calculateShippingFee(
      orderForm.value.province,
      orderForm.value.district,
      weight
    )
    shippingRates.value = rates

    // Update delivery options with calculated prices
    deliveryOptions.value.forEach(option => {
      if (rates[option.id]) {
        option.price = rates[option.id].price
        option.description = rates[option.id].description
        option.estimatedDays = rates[option.id].estimatedDays
      }
    })

  } catch (error) {
    console.error('Error calculating shipping rates:', error)
    shippingError.value = 'Không thể tính phí vận chuyển. Vui lòng thử lại.'
  } finally {
    isCalculatingShipping.value = false
  }
}

// Watch for address changes to recalculate shipping
watch(
  () => [orderForm.value.address, orderForm.value.province, orderForm.value.district],
  () => {
    if (orderForm.value.address && orderForm.value.province && orderForm.value.district) {
      calculateShippingRates()
    }
  },
  { deep: true }
)

const validateForm = () => {
  errors.value = {}

  if (!orderForm.value.fullName.trim()) {
    errors.value.fullName = 'Vui lòng nhập họ và tên'
  }

  if (!orderForm.value.phone.trim()) {
    errors.value.phone = 'Vui lòng nhập số điện thoại'
  } else if (!/^[0-9]{10,11}$/.test(orderForm.value.phone.replace(/\s/g, ''))) {
    errors.value.phone = 'Số điện thoại không hợp lệ'
  }

  if (!orderForm.value.province) {
    errors.value.province = 'Vui lòng chọn tỉnh/thành phố'
  }

  if (!orderForm.value.district) {
    errors.value.district = 'Vui lòng chọn quận/huyện'
  }

  if (!orderForm.value.address.trim()) {
    errors.value.address = 'Vui lòng nhập địa chỉ cụ thể'
  }

  return Object.keys(errors.value).length === 0
}

const placeOrder = async () => {
  if (!validateForm()) {
    showToast('error', 'Vui lòng kiểm tra lại thông tin', 'bi-exclamation-circle-fill')
    return
  }

  if (selectedItems.value.length === 0) {
    showToast('error', 'Giỏ hàng trống', 'bi-exclamation-circle-fill')
    return
  }

  try {
    // Prepare order data
    const selectedDelivery = deliveryOptions.value.find(option => option.id === orderForm.value.deliveryMethod)
    const orderData = {
      // Customer information
      tenKhachHang: orderForm.value.fullName,
      soDienThoai: orderForm.value.phone,
      email: orderForm.value.email,
      diaChi: orderForm.value.address,
      tinhThanh: provinces.value.find(p => p.ProvinceID === orderForm.value.province)?.ProvinceName || '',
      quanHuyen: districts.value.find(d => d.DistrictID === orderForm.value.district)?.DistrictName || '',
      
      // Order details
      phuongThucGiaoHang: orderForm.value.deliveryMethod,
      phuongThucThanhToan: orderForm.value.paymentMethod,
      ghiChu: orderForm.value.note,
      
      // Pricing
      tongTien: getTotalPrice(),
      phiVanChuyen: selectedDelivery?.price || 0,
      
      // Items
      chiTietDonHang: selectedItems.value.map(item => ({
        chiTietSanPhamId: item.chiTietSanPhamId,
        soLuong: item.quantity,
        gia: item.gia,
        thanhTien: item.gia * item.quantity
      })),
      
      // Status
      trangThai: 'CHO_XAC_NHAN',
      
      // Timestamps
      ngayTao: new Date().toISOString(),
      ngayCapNhat: new Date().toISOString()
    }

    console.log('Sending order data:', orderData)

    // Send order to backend API (dedicated online order API)
    const response = await fetch(`${API_BASE_URL}/api/hoa-don/online-order`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        // Customer information
        tenKhachHang: orderData.tenKhachHang,
        soDienThoai: orderData.soDienThoai,
        email: orderData.email,
        diaChi: orderData.diaChi,
        tinhThanh: orderData.tinhThanh,
        quanHuyen: orderData.quanHuyen,
        
        // Order details
        phuongThucGiaoHang: orderData.phuongThucGiaoHang,
        phuongThucThanhToan: orderData.phuongThucThanhToan,
        ghiChu: orderData.ghiChu || '',
        loaiHoaDon: 'ONLINE', // Đảm bảo loại đơn hàng là ONLINE
        
        // Pricing
        tongTien: orderData.tongTien,
        phiVanChuyen: orderData.phiVanChuyen,
        
        // Items
        chiTietDonHang: orderData.chiTietDonHang,
        
        // Status
        trangThai: 'CHO_XAC_NHAN',
        
        // Timestamps
        ngayTao: new Date().toISOString(),
        ngayCapNhat: new Date().toISOString()
      })
    })

    if (!response.ok) {
      const errorText = await response.text()
      console.error('❌ Server error response:', errorText)
      throw new Error(`HTTP error! status: ${response.status} - ${errorText}`)
    }

    const result = await response.json()
    const orderId = result.maHoaDon || result.id || result.maDonHang
    console.log('✅ Order created successfully:', result)

    // Show success message and clear form
    showOrderSuccess(orderId)
    
    // Remove only purchased items from cart
    try {
      console.log('🛒 Debug - cartStore.items before:', cartStore.items)
      console.log('🛒 Debug - purchasedItems:', window.purchasedItems)
      
      // Remove only the items that were purchased
      if (window.purchasedItems && window.purchasedItems.length > 0) {
        window.purchasedItems.forEach(item => {
          console.log(`🛒 Debug - Removing purchased item:`, item)
          cartStore.removeItem(item.chiTietSanPhamId)
          console.log(`✅ Removed purchased item: ${item.tenSanPham}`)
        })
      } else {
        console.log('⚠️ No purchased items found, clearing entire cart')
        cartStore.clearCart()
      }
      
      console.log('🛒 Debug - cartStore.items after:', cartStore.items)
      console.log('✅ Purchased items removed from cart')
      } catch (e) {
        console.error('Error removing purchased items from cart:', e)
      }

  } catch (error) {
    console.error('Error placing order:', error)
    showToast('error', 'Có lỗi xảy ra khi đặt hàng. Vui lòng thử lại.', 'bi-exclamation-circle-fill')
  }
}

const showToast = (type, message, icon) => {
  toast.value = {
    show: true,
    type,
    message,
    icon
  }
  setTimeout(() => {
    toast.value.show = false
  }, 3000)
}

const showOrderSuccess = (orderId) => {
  orderSuccess.value = {
    show: true,
    orderId: orderId
  }
  
  // Reset form
  orderForm.value = {
    fullName: '',
    phone: '',
    email: '',
    province: '',
    district: '',
    address: '',
    deliveryMethod: 'standard',
    paymentMethod: 'cod',
    note: ''
  }
  
  // Reset selected items
  selectedItems.value = []
}

// Copy to clipboard function
const copyToClipboard = async (text) => {
  try {
    await navigator.clipboard.writeText(text)
    showToast('success', 'Đã sao chép vào clipboard!', 'bi-check-circle-fill')
  } catch (error) {
    console.error('Error copying to clipboard:', error)
    showToast('error', 'Không thể sao chép', 'bi-exclamation-circle-fill')
  }
}

// Track order function
const trackOrder = () => {
  router.push({
    path: '/theo-doi-don-hang',
    query: { 
      orderId: orderSuccess.value.orderId,
      phone: orderForm.value.phone 
    }
  })
}

onMounted(async () => {
  // Check user login status first
  checkUserLogin()
  
  // Load selected items from sessionStorage into local list only
  const selected = sessionStorage.getItem('selectedItems')
  if (selected) {
    try {
      selectedItems.value = JSON.parse(selected) || []
      // Store a copy for later use in order processing
      window.purchasedItems = [...selectedItems.value]
      sessionStorage.removeItem('selectedItems')
    } catch (error) {
      console.error('Error loading selected items:', error)
    }
  } else if (cartStore.items.length === 0) {
    router.push('/cart')
  }

  // Load provinces for address selection
  await loadProvinces()
})
</script>

<style scoped>
:root {
  --phoenix-primary: #FF6B35;
  --phoenix-secondary: #F7931E;
  --phoenix-accent: #DC143C;
  --phoenix-gold: #FFD700;
  --phoenix-dark: #2C1810;
}

.dathang-page {
  min-height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
}

.dathang-main {
  flex: 1;
  padding: 3rem 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

/* Page Header */
.page-header {
  text-align: center;
  margin-bottom: 3rem;
}

.page-header h1 {
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--phoenix-primary);
  margin-bottom: 0.5rem;
}

.page-header p {
  font-size: 1.1rem;
  color: #666;
}

/* User Status Styles */
.user-status,
.guest-status {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  margin-top: 1.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.user-status {
  border-left: 4px solid #28a745;
}

.guest-status {
  border-left: 4px solid #ffc107;
}

.user-info,
.guest-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
  font-size: 1.1rem;
  font-weight: 600;
}

.user-info i {
  color: #28a745;
  font-size: 1.3rem;
}

.guest-info i {
  color: #ffc107;
  font-size: 1.3rem;
}

.login-benefits,
.login-prompt {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.95rem;
  color: #666;
}

.login-benefits i {
  color: #28a745;
}

.login-prompt i {
  color: #17a2b8;
}

/* Address Selection Styles */
.address-selection {
  margin-bottom: 2rem;
  padding: 1.5rem;
  background: #f8f9fa;
  border-radius: 12px;
  border: 1px solid #e9ecef;
}

.subsection-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.subsection-title i {
  color: var(--phoenix-primary);
}

.address-options {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-bottom: 1rem;
}

.address-option {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 1rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background: white;
}

.address-option:hover {
  border-color: var(--phoenix-primary);
}

.address-option.selected {
  border-color: var(--phoenix-primary);
  background: rgba(255, 107, 53, 0.05);
}

.address-option input[type="radio"] {
  margin: 0;
  margin-top: 0.25rem;
}

.address-content {
  flex: 1;
}

.address-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.address-name {
  font-weight: 600;
  font-size: 1rem;
  color: #333;
}

.default-badge {
  background: var(--phoenix-primary);
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 500;
}

.address-details {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.address-phone,
.address-location {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  color: #666;
}

.address-phone i,
.address-location i {
  color: var(--phoenix-primary);
  font-size: 0.9rem;
}

.address-actions {
  display: flex;
  justify-content: center;
}

.btn-add-address {
  background: transparent;
  color: var(--phoenix-primary);
  border: 2px dashed var(--phoenix-primary);
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s;
}

.btn-add-address:hover {
  background: var(--phoenix-primary);
  color: white;
}

/* Order Success Overlay */
.order-success-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 2rem;
}

.order-success-card {
  background: white;
  border-radius: 20px;
  padding: 3rem;
  max-width: 500px;
  width: 100%;
  text-align: center;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-50px) scale(0.9);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.success-icon {
  margin-bottom: 1.5rem;
}

.success-icon i {
  font-size: 4rem;
  color: #28a745;
  animation: bounce 1s ease-in-out;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-20px);
  }
  60% {
    transform: translateY(-10px);
  }
}

.success-title {
  font-size: 2rem;
  font-weight: 700;
  color: var(--phoenix-primary);
  margin-bottom: 1rem;
}

.success-message {
  font-size: 1.1rem;
  color: #666;
  margin-bottom: 1.5rem;
  line-height: 1.6;
}

.order-id {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 1rem;
  margin-bottom: 2rem;
  font-size: 1.1rem;
  color: #333;
}

.order-id strong {
  color: var(--phoenix-primary);
  font-family: monospace;
}

.success-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

.btn-continue,
.btn-track {
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s;
  border: none;
  min-width: 150px;
  justify-content: center;
}

.btn-continue {
  background: #6c757d;
  color: white;
}

.btn-continue:hover {
  background: #5a6268;
  transform: translateY(-2px);
}

.btn-track {
  background: var(--phoenix-primary);
  color: white;
}

.btn-track:hover {
  background: var(--phoenix-accent);
  transform: translateY(-2px);
}

/* Layout */
.dathang-layout {
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 2rem;
}

/* Form Styles */
.order-form {
  background: white;
  border-radius: 15px;
  padding: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.form-section {
  margin-bottom: 2.5rem;
}

.form-section:last-child {
  margin-bottom: 0;
}

.section-title {
  font-size: 1.3rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  margin-bottom: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-group label {
  font-weight: 600;
  color: #333;
  margin-bottom: 0.5rem;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 0.75rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.3s;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: var(--phoenix-primary);
  box-shadow: 0 0 0 3px rgba(255, 107, 53, 0.1);
}

.form-group input.error,
.form-group select.error,
.form-group textarea.error {
  border-color: var(--phoenix-accent);
}

.error-message {
  color: var(--phoenix-accent);
  font-size: 0.875rem;
  margin-top: 0.25rem;
}

.address-help {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 6px;
  padding: 8px 12px;
  background: #e3f2fd;
  border: 1px solid #bbdefb;
  border-radius: 6px;
  font-size: 12px;
  color: #1976d2;
}

.address-help i {
  font-size: 14px;
  color: #1976d2;
}

/* Delivery Options */
.delivery-options {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.delivery-option {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 1rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.delivery-option:hover {
  border-color: var(--phoenix-primary);
}

.delivery-option input[type="radio"] {
  margin: 0;
}

.delivery-option input[type="radio"]:checked + .option-content {
  color: var(--phoenix-primary);
}

.delivery-option:has(input:checked) {
  border-color: var(--phoenix-primary);
  background: rgba(255, 107, 53, 0.05);
}

.option-content {
  flex: 1;
}

.option-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.25rem;
}

.option-name {
  font-weight: 600;
  font-size: 1rem;
}

.option-price {
  font-weight: 700;
  color: var(--phoenix-accent);
}

.option-description {
  font-size: 0.875rem;
  color: #666;
  margin: 0;
}

/* Payment Options */
.payment-options {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.payment-option {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 1.5rem;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  background: white;
  position: relative;
}

.payment-option:hover {
  border-color: var(--phoenix-primary);
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.1);
}

.payment-option.selected {
  border-color: var(--phoenix-primary);
  background: rgba(255, 107, 53, 0.05);
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.15);
}

.payment-option.popular {
  border-color: #28a745;
}

.payment-option.popular::before {
  content: "Phổ biến";
  position: absolute;
  top: -8px;
  right: 20px;
  background: #28a745;
  color: white;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
}

.payment-option.unavailable {
  opacity: 0.6;
  cursor: not-allowed;
  background: #f8f9fa;
}

.payment-option input[type="radio"] {
  margin: 0;
  margin-top: 0.25rem;
}

.option-content {
  flex: 1;
}

.option-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 0.75rem;
}

.option-main {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex: 1;
}

.option-main i {
  font-size: 1.5rem;
  color: var(--phoenix-primary);
}

.option-name {
  font-weight: 600;
  font-size: 1rem;
  color: #333;
}

.popular-badge {
  background: #28a745;
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 500;
}

.option-fee {
  text-align: right;
}

.fee-amount {
  font-weight: 700;
  color: var(--phoenix-accent);
  font-size: 1rem;
}

.free-text {
  font-weight: 600;
  color: #28a745;
  font-size: 0.9rem;
}

.option-details {
  margin-bottom: 1rem;
}

.option-description {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 0.75rem;
}

.option-meta {
  display: flex;
  gap: 1.5rem;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.85rem;
  color: #666;
}

.meta-item i {
  color: var(--phoenix-primary);
  font-size: 0.9rem;
}

/* Bank Transfer Details */
.bank-details {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 1rem;
  margin-top: 1rem;
  border: 1px solid #e9ecef;
}

.bank-details h4 {
  font-size: 1rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 0.75rem;
}

.bank-info {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.bank-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.bank-label {
  font-weight: 600;
  color: #666;
  min-width: 120px;
  font-size: 0.9rem;
}

.bank-value {
  font-weight: 600;
  color: #333;
  font-family: monospace;
  font-size: 0.9rem;
  flex: 1;
}

.copy-btn {
  background: var(--phoenix-primary);
  color: white;
  border: none;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.8rem;
  transition: background 0.2s;
}

.copy-btn:hover {
  background: var(--phoenix-accent);
}

.bank-note {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: #e3f2fd;
  padding: 0.75rem;
  border-radius: 6px;
  font-size: 0.85rem;
  color: #1976d2;
}

.bank-note i {
  color: #1976d2;
}

/* MoMo QR Code */
.momo-details {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 1rem;
  margin-top: 1rem;
  border: 1px solid #e9ecef;
  text-align: center;
}

.momo-details h4 {
  font-size: 1rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 0.75rem;
}

.qr-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
}

.qr-code {
  width: 150px;
  height: 150px;
  border-radius: 8px;
  border: 1px solid #ddd;
}

.qr-note {
  font-size: 0.85rem;
  color: #666;
  margin: 0;
}

/* Payment Security */
.payment-security {
  background: #e8f5e8;
  border: 1px solid #c3e6c3;
  border-radius: 8px;
  padding: 1rem;
  margin-top: 1rem;
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
}

.payment-security i {
  color: #28a745;
  font-size: 1.5rem;
  margin-top: 0.25rem;
}

.security-content h4 {
  font-size: 1rem;
  font-weight: 600;
  color: #155724;
  margin-bottom: 0.5rem;
}

.security-content p {
  font-size: 0.9rem;
  color: #155724;
  margin: 0;
  line-height: 1.4;
}

/* Order Summary */
.order-summary {
  background: white;
  border-radius: 15px;
  padding: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  height: fit-content;
  position: sticky;
  top: 2rem;
}

.summary-title {
  font-size: 1.3rem;
  font-weight: 700;
  margin-bottom: 1.5rem;
  color: #333;
}

.order-items {
  margin-bottom: 1.5rem;
}

.order-item {
  display: flex;
  gap: 1rem;
  padding: 1rem 0;
  border-bottom: 1px solid #f0f0f0;
}

.order-item:last-child {
  border-bottom: none;
}

.item-image img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
}

.item-info {
  flex: 1;
}

.item-name {
  font-size: 0.9rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 0.25rem;
}

.item-specs {
  display: flex;
  gap: 0.25rem;
  margin-bottom: 0.25rem;
}

.item-specs span {
  font-size: 0.75rem;
  padding: 0.125rem 0.375rem;
  background: #f0f0f0;
  border-radius: 4px;
  color: #666;
}

.item-quantity {
  font-size: 0.8rem;
  color: #666;
}

.item-price {
  font-weight: 700;
  color: var(--phoenix-accent);
  font-size: 0.9rem;
}

.summary-divider {
  height: 1px;
  background: #e0e0e0;
  margin: 1.5rem 0;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.75rem;
  color: #666;
}

.summary-value {
  font-weight: 600;
  color: #333;
}

.summary-row.total {
  font-size: 1.2rem;
  font-weight: 700;
  color: #333;
}

.summary-total {
  font-size: 1.5rem;
  color: var(--phoenix-accent);
}

.btn-place-order {
  width: 100%;
  padding: 1rem;
  background: var(--phoenix-primary);
  color: white;
  border: none;
  border-radius: 12px;
  font-weight: 700;
  font-size: 1.1rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  margin-top: 1.5rem;
  transition: all 0.3s;
}

.btn-place-order:hover:not(:disabled) {
  background: var(--phoenix-accent);
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(220, 20, 60, 0.3);
}

.btn-place-order:disabled {
  background: #ccc;
  cursor: not-allowed;
  opacity: 0.6;
}

.security-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 1rem;
  padding: 0.75rem;
  background: #f8f9fa;
  border-radius: 8px;
  font-size: 0.875rem;
  color: #28a745;
}

.security-info i {
  font-size: 1.2rem;
}

/* Footer */
.dathang-footer {
  background: #000000;
  color: white;
  text-align: center;
  padding: 2rem;
  margin-top: auto;
}

/* Toast */
.toast-notification {
  position: fixed;
  bottom: 2rem;
  right: 2rem;
  padding: 1rem 1.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-weight: 600;
  z-index: 9999;
  max-width: 400px;
}

.toast-notification.success {
  background: #28a745;
  color: white;
}

.toast-notification.error {
  background: #dc3545;
  color: white;
}

.toast-notification i {
  font-size: 1.5rem;
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateX(100px);
}

/* Responsive */
@media (max-width: 992px) {
  .dathang-layout {
    grid-template-columns: 1fr;
  }

  .order-summary {
    position: static;
  }

  .form-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .container {
    padding: 0 1rem;
  }

  .page-header h1 {
    font-size: 2rem;
  }

  .order-form,
  .order-summary {
    padding: 1.5rem;
  }

  .delivery-option,
  .payment-option {
    padding: 0.75rem;
  }
}

/* Shipping Status Styles */
.shipping-status {
  background: #fff3cd;
  border: 1px solid #ffeaa7;
  color: #856404;
  padding: 12px;
  border-radius: 6px;
  margin: 12px 0;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.shipping-status i {
  animation: spin 1s linear infinite;
}

.shipping-error {
  background: #f8d7da;
  border: 1px solid #f5c6cb;
  color: #721c24;
  padding: 12px;
  border-radius: 6px;
  margin: 12px 0;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.shipping-info {
  background: #e3f2fd;
  border-left: 3px solid #2196f3;
  padding: 8px 12px;
  margin: 4px 0;
  border-radius: 4px;
}

.shipping-details {
  font-size: 14px;
  color: #1976d2;
  display: flex;
  align-items: center;
  gap: 6px;
}

.shipping-details i {
  font-size: 16px;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* Address form loading states */
.form-select:disabled {
  background-color: #f8f9fa;
  cursor: not-allowed;
  opacity: 0.6;
}

.form-select:disabled::after {
  content: "Đang tải...";
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 12px;
  color: #6c757d;
}
</style>
