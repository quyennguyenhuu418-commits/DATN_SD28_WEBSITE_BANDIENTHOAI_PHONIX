<template>
  <div class="tracking-page">
    <!-- Header -->
    <HeaderLayout/>

    <!-- Main Content -->
    <main class="tracking-main">
      <div class="container">
        <!-- Page Header -->
        <div class="page-header" style="margin-top: 20px">
          <h1>{{ isLoggedIn ? 'Đơn hàng đã mua' : 'Tra cứu đơn hàng' }}</h1>
        </div>

        <!-- Customer Info Section - Only show for logged in customers -->
        <div v-if="isLoggedIn && customerInfo" class="customer-info-section">
          <div class="customer-info-card">
            <div class="customer-info-header">
              <i class="bi bi-person-circle"></i>
              <h2>Thông tin khách hàng</h2>
            </div>
            <div class="customer-info-content">
              <div class="customer-info-grid">
                <div class="customer-info-item">
                  <span class="info-label">
                    <i class="bi bi-person"></i>
                    Họ và tên:
                  </span>
                  <span class="info-value">{{ customerInfo.hoTen || customerInfo.tenKhachHang || 'Chưa cập nhật' }}</span>
                </div>
                <div class="customer-info-item">
                  <span class="info-label">
                    <i class="bi bi-envelope"></i>
                    Email:
                  </span>
                  <span class="info-value">{{ customerInfo.email || 'Chưa cập nhật' }}</span>
                </div>
                <div class="customer-info-item">
                  <span class="info-label">
                    <i class="bi bi-telephone"></i>
                    Số điện thoại:
                  </span>
                  <span class="info-value">{{ customerInfo.soDienThoai || 'Chưa cập nhật' }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Status Tabs - Only show for logged in users -->
        <div v-if="isLoggedIn" class="status-tabs">
          <button
            v-for="(status, index) in statusTabs"
            :key="index"
            :class="['tab-btn', { active: selectedStatus === status.value }]"
            @click="selectedStatus = status.value"
          >
            {{ status.label }}
          </button>
        </div>

        <!-- Search Bar for Non-Logged In Users -->
        <div v-if="!isLoggedIn" class="search-section">
          <div class="search-card">
            <h2>Tìm kiếm đơn hàng theo mã hóa đơn</h2>
            <div class="search-form">
              <label for="orderId" class="form-label">Mã hóa đơn</label>
              <input
                id="orderId"
                type="text"
                v-model="searchForm.orderId"
                placeholder="Nhập mã hóa đơn"
                class="search-input"
                :class="{ error: errors.orderId }"
                @keyup.enter="searchOrder"
              />
              <button
                class="btn-search"
                @click="searchOrder"
                :disabled="isSearching"
              >
                <i class="bi bi-search"></i>
                {{ isSearching ? 'Đang tìm...' : 'Tìm kiếm' }}
              </button>
              <span v-if="errors.orderId" class="error-message">{{ errors.orderId }}</span>
            </div>
          </div>
        </div>

        <!-- Order Not Found Message for Non-Logged In Users -->
        <div v-if="!isLoggedIn && showNoOrder && !isSearching" class="no-order">
          <div class="no-order-card">
            <i class="bi bi-exclamation-triangle-fill"></i>
            <h3>Đơn hàng không tồn tại</h3>
            <p>Mã hóa đơn bạn nhập không tồn tại trong hệ thống. Vui lòng kiểm tra lại.</p>
            <div class="no-order-actions">
              <button class="btn-retry" @click="resetSearch">Thử lại</button>
              <button class="btn-support" @click="contactSupport">Liên hệ hỗ trợ</button>
            </div>
          </div>
        </div>

        <!-- Order Details - Show for both logged in and non-logged in users -->
        <div v-if="orderDetails" class="order-details">
          <div class="order-card">
            <div class="order-header">
              <h2>Chi tiết đơn hàng</h2>
              <div class="order-id">#{{ orderDetails.id }}</div>
            </div>

            <!-- Order Status -->
            <div class="status-section">
              <div class="status-badge" :class="getStatusClass(orderDetails.trangThai)">
                <i :class="getStatusIcon(orderDetails.trangThai)"></i>
                {{ getStatusText(orderDetails.trangThai) }}
              </div>
            </div>

            <!-- Order Status Timeline -->
            <div class="order-status-section">
              <div class="status-header">
                <i class="bi bi-clock-history"></i>
                <span>Trạng Thái Hóa Đơn - {{ orderDetails.maHoaDon }}</span>
              </div>
              <div class="status-timeline">
                <div class="status-steps">
                  <div
                    v-for="(step, index) in getTrackingSteps"
                    :key="index"
                    :class="['status-step', {
                      active: !(index < getCurrentStepIndex || (step.time && index === getCurrentStepIndex)),
                      completed: index < getCurrentStepIndex || (step.time && index === getCurrentStepIndex),
                      isCancelled: step.isCancelled
                    }]"
                  >
                    <div class="step-icon">
                      <i
                        :class="getStepIcon(step, index)"
                        v-if="index < getCurrentStepIndex || (step.time && index === getCurrentStepIndex) || step.isCancelled"
                      ></i>
                      <i :class="getStepIconOriginal(step.icon)" v-else></i>
                    </div>
                    <div class="step-content">
                      <div class="step-title">{{ step.title }}</div>
                      <div class="step-time">{{ step.time || 'Không có thời gian' }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Order Info -->
            <div class="order-info">
              <div class="info-grid">
                <div class="info-item">
                  <span class="info-label">Khách hàng:</span>
                  <span class="info-value">{{ orderDetails.tenKhachHang || 'Không có' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">Số điện thoại:</span>
                  <span class="info-value">{{ orderDetails.soDienThoai || 'Không có' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">Email:</span>
                  <span class="info-value">{{ orderDetails.email ? orderDetails.email : 'Không có' }}</span>
                </div>

                <div class="info-item">
                  <span class="info-label">Ngày đặt:</span>
                  <span class="info-value">{{ formatDate(orderDetails.ngayDat || orderDetails.ngayTao) }}</span>
                </div>
                <div class="info-item" v-if="orderDetails.maVoucher || orderDetails.phieuGiamGiaId">
                  <span class="info-label">Phiếu giảm giá:</span>
                  <span class="info-value">{{ orderDetails.maVoucher || `VC_${orderDetails.phieuGiamGiaId}` }}</span>
                </div>
                <div class="info-item" v-if="orderDetails.ghiChu">
                  <span class="info-label">Ghi chú:</span>
                  <span class="info-value">{{ orderDetails.ghiChu }}</span>
                </div>
              </div>
              <div class="info-item">
                <span class="info-label">Địa chỉ:</span>
                <span class="info-value">{{ formatAddress(orderDetails) || 'Không có' }}</span>
              </div>
            </div>

            <!-- Order Items -->
            <div class="order-items">
              <h3>Sản phẩm đã đặt</h3>
              <div class="items-list">
                <div v-for="(item, index) in orderDetails.chiTietDonHang" :key="item.id || item.chiTietHoaDonId || index" class="item-card">
                  <div class="item-image">
                    <img
                      :src="item.hinhAnh || '/placeholder-product.jpg'"
                      :alt="item.tenSanPham"
                      @error="($event) => $event.target.src = '/placeholder-product.jpg'"
                    />
                  </div>
                  <div class="item-info">
                    <h4>{{ item.tenSanPham }}</h4>
                    <div class="item-specs">
                      <span v-if="item.tenRam" class="spec-badge">{{ item.tenRam }}</span>
                      <span v-if="item.tenRom" class="spec-badge">{{ item.tenRom }}</span>
                      <span v-if="item.tenMauSac" class="spec-badge">{{ item.tenMauSac }}</span>
                    </div>
                  </div>
                  <div class="item-price">{{ formatPrice(item.thanhTien || (item.donGia * item.soLuong) || item.gia || 0) }}</div>
                </div>
                <div v-if="!orderDetails.chiTietDonHang || orderDetails.chiTietDonHang.length === 0" class="no-products">
                  Không có sản phẩm
                </div>
              </div>
            </div>

            <!-- Order Summary -->
            <div class="order-summary">
              <div class="summary-row">
                <span>Tạm tính:</span>
                <span>{{ formatPrice(orderDetails.tongTienHang || orderDetails.tongTien || 0) }}</span>
              </div>
              <div class="summary-row" v-if="orderDetails.giamGia && orderDetails.giamGia > 0">
                <span>Giảm giá:</span>
                <span class="discount-text">-{{ formatPrice(orderDetails.giamGia) }}</span>
              </div>
              <div class="summary-row" v-if="orderDetails.phiVanChuyen && orderDetails.phiVanChuyen > 0">
                <span>Phí vận chuyển:</span>
                <span>{{ formatPrice(orderDetails.phiVanChuyen) }}</span>
              </div>
              <div class="summary-row total">
                <span>Tổng cộng:</span>
                <span>{{ formatPrice(orderDetails.thanhTien || orderDetails.tongTienSauGiam || orderDetails.tongTien || 0) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Orders List - Only show for logged in users -->
        <div v-if="isLoggedIn && filteredOrders.length > 0" class="orders-list">
          <div v-for="order in filteredOrders" :key="order.id" class="order-card">
            <div class="order-header">
              <div class="order-info">
                <div class="order-id-section">
                  <span class="order-label">Đơn hàng:</span>
                  <span class="order-id">#{{ order.id }}</span>
                </div>
                <div class="delivery-info" v-if="order.deliveryTime">
                  <span class="bullet">•</span>
                  <span class="delivery-text">{{ order.deliveryTime }}</span>
                </div>
              </div>
              <div class="order-status">
                <span :class="['status-text', getStatusClass(order.status)]">
                  {{ getStatusText(order.status) }}
                </span>
              </div>
            </div>

            <div class="order-content">
              <div class="product-section">
                <div class="product-image" v-if="order.items && order.items.length > 0">
                  <img :src="order.items[0]?.hinhAnh || order.items[0]?.image || '/placeholder-product.jpg'" :alt="order.items[0]?.tenSanPham || order.items[0]?.name" />
                </div>
                <div class="product-details">
                  <div class="product-name">
                    {{ order.items && order.items.length > 0
                    ? (order.items[0]?.tenSanPham || order.items[0]?.name)
                    : 'Không có sản phẩm' }}
                  </div>
                  <div class="product-description" v-if="order.items && order.items.length > 0 && (order.items[0]?.description || order.items[0]?.moTa)">
                    {{ order.items[0]?.description || order.items[0]?.moTa }}
                  </div>
                  <div v-if="order.items && order.items.length > 1" class="product-count">
                    và {{ order.items.length - 1 }} sản phẩm khác
                  </div>
                </div>
              </div>

              <div class="order-summary">
                <div class="total-section">
                  <span class="total-label">Tổng tiền:</span>
                  <span :class="['total-amount', getTotalAmountClass(order.status)]">
                    {{ formatPrice(order.total) }}
                  </span>
                </div>
                <div class="payment-status" v-if="order.paymentStatus">
                  <span :class="['payment-text', getPaymentStatusClass(order.paymentStatus)]">
                    {{ order.paymentStatus }}
                  </span>
                </div>
                <div class="order-actions">
                  <button class="btn-view-detail" @click="viewOrderDetail(order)">
                    Xem chi tiết
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- No Orders State - Only show for logged in users -->
        <div v-if="isLoggedIn && !isLoadingOrders && filteredOrders.length === 0" class="no-orders-state">
          <div class="no-orders-content">
            <div class="no-orders-illustration">
              <div class="illustration-circle">
                <div class="clipboard-icon">
                  <div class="clipboard-body"></div>
                  <div class="clipboard-clip"></div>
                  <div class="checkmarks">
                    <div class="checkmark"></div>
                    <div class="checkmark"></div>
                    <div class="checkmark"></div>
                  </div>
                  <div class="pencil"></div>
                  <div class="floating-dots">
                    <div class="dot dot-1"></div>
                    <div class="dot dot-2"></div>
                    <div class="dot dot-3"></div>
                  </div>
                </div>
              </div>
            </div>
            <h3 class="no-orders-title">Chưa có đơn hàng</h3>
          </div>
        </div>

        <!-- Loading State -->
        <div v-if="isLoggedIn && isLoadingOrders" class="loading-state">
          <div class="loading-content">
            <div class="spinner"></div>
            <p>Đang tải danh sách đơn hàng...</p>
          </div>
        </div>
      </div>
    </main>

    <!-- Footer -->
    <footer class="tracking-footer">
      <FooterLayout/>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import HeaderLayout from './HeaderLayout.vue'
import FooterLayout from './FooterLayout.vue'
import api from '@/services/api'
import { useCustomerAuthStore } from '@/stores/customerAuthStore'

const router = useRouter()
const route = useRoute()
const customerAuthStore = useCustomerAuthStore()

const API_BASE_URL = 'http://localhost:8080'

// Authentication state
const isLoggedIn = ref(false)
const userInfo = ref(null)
const customerInfo = ref(null)

// Form data for search by order code (for non-logged in users)
const searchForm = ref({
  orderId: ''
})

const errors = ref({})
const isSearching = ref(false)
const isLoadingOrders = ref(false)
const orderDetails = ref(null)
const showNoOrder = ref(false)
const refreshInterval = ref(null)

// New variables for the redesigned interface
const selectedStatus = ref('all')
const searchQuery = ref('')

const statusTabs = ref([
  { label: 'Tất cả', value: 'all' },
  { label: 'Hoàn thành', value: 'completed' },
  { label: 'Đã hủy', value: 'cancelled' },
  { label: 'Trả hàng/Hoàn tiền', value: 'return' }
])

// Orders data - loaded from API
const orders = ref([])

// Check user login status
const checkUserLogin = async () => {
  // Check customer authentication first
  if (customerAuthStore.isAuthenticated && customerAuthStore.user) {
    isLoggedIn.value = true
    userInfo.value = customerAuthStore.user
    customerInfo.value = customerAuthStore.user
    
    // Try to get fresh customer info from API
    try {
      const result = await customerAuthStore.getCurrentUser()
      if (result.success && result.data) {
        customerInfo.value = result.data
        userInfo.value = result.data
      }
    } catch (error) {
      console.warn('Failed to fetch fresh customer info, using stored data:', error)
    }
    return
  }

  // Fallback to old user_token/user_data (for admin/staff)
  const userToken = localStorage.getItem('user_token')
  const userData = localStorage.getItem('user_data')

  if (userToken && userData) {
    try {
      isLoggedIn.value = true
      userInfo.value = JSON.parse(userData)
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

// Load orders for logged in user
const loadUserOrders = async () => {
  if (!isLoggedIn.value || !userInfo.value) {
    return
  }

  // Get khachHangId from userInfo (prioritize customer store)
  const khachHangId = customerInfo.value?.id || 
                      userInfo.value.id || 
                      userInfo.value.khachHangId || 
                      userInfo.value.userId

  if (!khachHangId) {
    console.error('Không tìm thấy ID khách hàng', userInfo.value)
    orders.value = []
    return
  }

  isLoadingOrders.value = true

  try {
    const response = await api.get(`/api/hoa-don/khach-hang/${khachHangId}`)
    const data = response.data || response

    if (data && Array.isArray(data)) {
      // Transform API response to match UI format
      orders.value = data.map(order => {
        // Get chiTietHoaDon or chiTietDonHang from order
        const chiTiet = order.chiTietHoaDon || order.chiTietDonHang || []

        return {
          id: order.maHoaDon || order.id,
          date: order.ngayTao,
          status: mapTrangThaiToStatus(order.trangThai),
          total: order.tongTien || order.thanhTien || 0,
          items: chiTiet,
          originalOrder: order
        }
      })
    } else {
      orders.value = []
    }
  } catch (error) {
    console.error('Error loading user orders:', error)
    orders.value = []
  } finally {
    isLoadingOrders.value = false
  }
}

// Map backend trangThai to frontend status
const mapTrangThaiToStatus = (trangThai) => {
  // Backend status: 0=Chờ xác nhận, 1=Chờ giao hàng, 2=Đang giao, 3=Hoàn thành, 4=Đã hủy
  const statusMap = {
    0: 'pending',
    1: 'pending',
    2: 'shipping',
    3: 'completed',
    4: 'cancelled'
  }
  return statusMap[trangThai] || 'pending'
}

// Methods for non-logged in users: search order by code
const searchOrder = async () => {
  if (!searchForm.value.orderId.trim()) {
    errors.value.orderId = 'Vui lòng nhập mã đơn hàng'
    return
  }

  isSearching.value = true
  showNoOrder.value = false
  orderDetails.value = null

  try {
    // Use tracking endpoint exactly like HoaDonDetailPage.vue
    const { data: result } = await api.get(`/api/hoa-don/tracking/${searchForm.value.orderId}`)

    console.log('🔍 API Response:', result)
    console.log('🔍 Full lichSuTrangThai from API:', result.lichSuTrangThai)
    console.log('🔍 lichSuTrangThai length:', result.lichSuTrangThai?.length)
    if (result.lichSuTrangThai && result.lichSuTrangThai.length > 0) {
      result.lichSuTrangThai.forEach((item, idx) => {
        console.log(`🔍 API History ${idx}:`, JSON.stringify(item, null, 2))
      })
    }

    if (result && result.maHoaDon) {
      // Get products from danhSachSanPham (tracking endpoint structure)
      const products = result.danhSachSanPham || []

      // Get product image URL helper
      const getProductImage = (imagePath) => {
        if (!imagePath) return '/placeholder-product.jpg'
        if (imagePath.startsWith('http')) return imagePath
        return `${API_BASE_URL}${imagePath.startsWith('/') ? '' : '/'}${imagePath}`
      }

      // Ensure trangThai is a number
      const trangThai = typeof result.trangThai === 'number' ? result.trangThai : parseInt(result.trangThai) || 0

      console.log('📊 Status from API:', result.trangThai, 'Type:', typeof result.trangThai, 'Parsed:', trangThai)

      orderDetails.value = {
        id: result.maHoaDon,
        maHoaDon: result.maHoaDon,
        tenKhachHang: result.tenKhachHang,
        soDienThoai: result.soDienThoai,
        email: result.email || '',
        diaChi: result.diaChi || '',
        tinhThanh: result.tinhThanh || '',
        quanHuyen: result.quanHuyen || '',
        ngayTao: result.ngayTao,
        ngayDat: result.ngayDat || result.ngayTao,
        ngayCapNhat: result.ngayCapNhat,
        loaiHoaDon: result.loaiHoaDon || 'BAN_ONLINE', // Important for timeline
        // Use correct price fields from tracking API
        tongTienHang: result.tongTienHang || 0,
        tongTien: result.tongTien || result.tongTienHang || 0,
        tongTienSauGiam: result.tongTienSauGiam || 0,
        thanhTien: result.thanhTien || result.tongTienSauGiam || 0,
        giamGia: result.giamGia || 0,
        phiVanChuyen: result.phiVanChuyen || 0,
        trangThai: trangThai, // Ensure it's a number
        maVoucher: result.maVoucher,
        phieuGiamGia: result.phieuGiamGia,
        phieuGiamGiaId: result.phieuGiamGiaId,
        ghiChu: result.ghiChu,
        lichSuTrangThai: result.lichSuTrangThai || [],
        chiTietDonHang: products.map(item => ({
          ...item,
          tenSanPham: item.tenSanPham || '',
          soLuong: item.soLuong || 1,
          donGia: item.donGia || item.gia || 0,
          thanhTien: item.thanhTien || (item.donGia || item.gia || 0) * (item.soLuong || 1),
          tenRam: item.tenRam || '',
          tenRom: item.tenRom || '',
          tenMauSac: item.tenMauSac || '',
          hinhAnh: getProductImage(item.hinhAnh)
        }))
      }

      console.log('✅ Order details set:', orderDetails.value)
      console.log('📊 Final status in orderDetails:', orderDetails.value.trangThai, 'Type:', typeof orderDetails.value.trangThai)
      console.log('📅 ngayTao:', orderDetails.value.ngayTao)
      console.log('📅 ngayCapNhat:', orderDetails.value.ngayCapNhat)
      console.log('📅 lichSuTrangThai:', orderDetails.value.lichSuTrangThai)
      if (orderDetails.value.lichSuTrangThai && orderDetails.value.lichSuTrangThai.length > 0) {
        orderDetails.value.lichSuTrangThai.forEach((item, idx) => {
          console.log(`📅 History ${idx}:`, {
            trangThai: item.trangThai,
            thoiGian: item.thoiGian,
            tenTrangThai: item.tenTrangThai,
            moTa: item.moTa
          })
        })
      }
      showNoOrder.value = false

      // Start auto-refresh to update status when admin changes it
      startAutoRefresh()
    } else {
      showNoOrder.value = true
      orderDetails.value = null
    }
  } catch (error) {
    console.error('Error searching order:', error)
    showNoOrder.value = true
    orderDetails.value = null
  } finally {
    isSearching.value = false
  }
}

const resetSearch = () => {
  searchForm.value = { orderId: '' }
  errors.value = {}
  orderDetails.value = null
  showNoOrder.value = false
  stopAutoRefresh() // Stop auto-refresh when clearing search
}

const contactSupport = () => {
  // You can implement actual contact support functionality here
  alert('Liên hệ hỗ trợ:\n\n📞 Hotline: 1900 1234\n📧 Email: support@phonix.com\n⏰ Giờ làm việc: 8:00 - 22:00 (Hàng ngày)')
}

const formatPrice = (price) => {
  if (!price) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

const formatDate = (dateString) => {
  if (!dateString) return 'Không có'
  try {
    const date = new Date(dateString)
    if (isNaN(date.getTime())) return 'Không có'
    return date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) + ' ' +
      date.toLocaleDateString('vi-VN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      })
  } catch (e) {
    return 'Không có'
  }
}

const formatDateTime = (dateString) => {
  if (!dateString) return 'N/A'
  const date = new Date(dateString)
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const year = date.getFullYear()
  return `${hours}:${minutes} ${day}/${month}/${year}`
}

const formatAddress = (order) => {
  const parts = []
  // Try multiple possible address fields
  const diaChi = order.diaChi || order.diaChiGiaoHang || ''
  const quanHuyen = order.quanHuyen || order.quanHuyenGiaoHang || ''
  const tinhThanh = order.tinhThanh || order.tinhThanhGiaoHang || ''

  if (diaChi) parts.push(diaChi)
  if (quanHuyen) parts.push(quanHuyen)
  if (tinhThanh) parts.push(tinhThanh)

  return parts.length > 0 ? parts.join(', ') : 'Không có'
}

const getLoaiHoaDonText = (loaiHoaDon) => {
  const typeMap = {
    'BAN_ONLINE': 'Đơn online',
    'ONLINE': 'Đơn online',
    'BAN_THUONG': 'Đơn tại quầy',
    'NORMAL': 'Đơn tại quầy'
  }
  return typeMap[loaiHoaDon] || loaiHoaDon || 'Không xác định'
}

const getStatusBadgeClass = (status) => {
  if (typeof status === 'number') {
    status = mapTrangThaiToStatus(status)
  }
  const classMap = {
    'pending': 'badge-warning',
    'shipping': 'badge-info',
    'completed': 'badge-success',
    'cancelled': 'badge-danger',
    'return': 'badge-secondary'
  }
  return classMap[status] || 'badge-warning'
}

// Get order status steps for progress bar
const getOrderStatusSteps = (order) => {
  if (!order) return []

  const isCancelled = order.trangThai === 4
  const isBanThuong = order.loaiHoaDon === 'BAN_THUONG' || order.loaiHoaDon === 'NORMAL'

  if (isCancelled) {
    const cancelTime = order.ngayCapNhat || order.ngayTao
    return [{
      title: 'Đã hủy',
      icon: 'bi-x-circle-fill',
      time: cancelTime ? formatDateTime(cancelTime) : '',
      isActive: true,
      isCompleted: false,
      status: 4
    }]
  }

  if (isBanThuong) {
    const completionTime = order.ngayTao ? formatDateTime(order.ngayTao) : ''
    return [{
      title: 'Hoàn thành',
      icon: 'bi-check-circle-fill',
      time: completionTime,
      isActive: true,
      isCompleted: true,
      status: 3
    }]
  }

  // BAN_ONLINE: 4 steps
  const steps = [
    { title: 'Chờ xác nhận', icon: 'bi-check-circle-fill', status: 0 },
    { title: 'Chờ giao hàng', icon: 'bi-check-circle-fill', status: 1 },
    { title: 'Đang giao hàng', icon: 'bi-truck', status: 2 },
    { title: 'Hoàn thành', icon: 'bi-check-circle-fill', status: 3 }
  ]

  const currentStatus = order.trangThai || 0

  // Get history from lichSuTrangThai if available
  const history = order.lichSuTrangThai || []

  return steps.map((step, index) => {
    let time = ''
    const historyItem = history.find(h => h.trangThai === step.status)
    if (historyItem && historyItem.thoiGian) {
      time = formatDateTime(historyItem.thoiGian)
    } else if (step.status === 0 && order.ngayTao && currentStatus > 0) {
      // Fallback: use ngayTao for step 0 if order has progressed
      time = formatDateTime(order.ngayTao)
    }

    const isCompleted = step.status < currentStatus
    const isActive = step.status === currentStatus

    return {
      ...step,
      time,
      isActive,
      isCompleted
    }
  })
}

// Get order history
const getOrderHistory = (order) => {
  if (!order) return []

  const history = []

  // Add history from lichSuTrangThai if available
  if (order.lichSuTrangThai && Array.isArray(order.lichSuTrangThai)) {
    order.lichSuTrangThai.forEach(item => {
      const statusText = getStatusText(item.trangThai)
      history.push({
        action: `Chuyển đơn sang trạng thái: ${statusText}`,
        time: item.thoiGian ? formatDateTime(item.thoiGian) : '',
        statusText: item.trangThai === order.trangThai ? 'Đã xử lý' : 'Chưa xử lý',
        statusClass: item.trangThai === order.trangThai ? 'status-processed' : 'status-pending',
        nguoiThucHien: item.nguoiThucHien || order.maNhanVien || order.nguoiTao || 'online_customer'
      })
    })
  } else {
    // Fallback: create history from order data
    if (order.ngayTao) {
      const statusText = getStatusText(order.trangThai)
      history.push({
        action: `Chuyển đơn sang trạng thái: ${statusText}`,
        time: formatDateTime(order.ngayTao),
        statusText: 'Đã xử lý',
        statusClass: 'status-processed',
        nguoiThucHien: order.maNhanVien || order.nguoiTao || 'online_customer'
      })
    }
  }

  return history.sort((a, b) => {
    // Sort by time descending (newest first)
    if (!a.time || !b.time) return 0
    return new Date(b.time) - new Date(a.time)
  })
}



const getDeliveryMethodText = (method) => {
  if (!method) return 'Chưa chọn'

  const methodMap = {
    'standard': 'Tiêu chuẩn',
    'express': 'Nhanh',
    'ghn': 'Hỏa tốc',
    'fast': 'Nhanh',
    'urgent': 'Hỏa tốc',
    'pickup': 'Lấy tại cửa hàng',
    'delivery': 'Giao hàng tận nơi'
  }
  return methodMap[method.toLowerCase()] || method
}

const getPaymentMethodText = (method) => {
  const methodMap = {
    'cod': 'Thanh toán khi nhận hàng',
    'bank-transfer': 'Chuyển khoản ngân hàng',
    'momo': 'Ví MoMo',
    'vnpay': 'VNPay'
  }
  return methodMap[method] || method
}

const getStatusClass = (status) => {
  // Handle both string status (from UI) and number status (from backend)
  if (typeof status === 'number') {
    status = mapTrangThaiToStatus(status)
  }

  const statusMap = {
    'pending': 'status-pending',
    'shipping': 'status-shipping',
    'completed': 'status-completed',
    'cancelled': 'status-cancelled',
    'return': 'status-return'
  }
  return statusMap[status] || 'status-pending'
}

const getStatusText = (status) => {
  // Handle both string status (from UI) and number status (from backend)
  if (typeof status === 'number') {
    const statusTextMap = {
      0: 'Chờ xác nhận',
      1: 'Chờ giao hàng',
      2: 'Đang giao hàng',
      3: 'Đã nhận hàng',
      4: 'Đã hủy'
    }
    return statusTextMap[status] || 'Chờ xác nhận'
  }

  const textMap = {
    'pending': 'Chờ xác nhận',
    'shipping': 'Đang giao hàng',
    'completed': 'Đã nhận hàng',
    'cancelled': 'Đã hủy',
    'return': 'Trả hàng/Hoàn tiền'
  }
  return textMap[status] || 'Chờ xác nhận'
}

const getStatusIcon = (status) => {
  // Handle both string status (from UI) and number status (from backend)
  if (typeof status === 'number') {
    status = mapTrangThaiToStatus(status)
  }

  const iconMap = {
    'pending': 'bi-clock-history',
    'shipping': 'bi-truck',
    'completed': 'bi-check-circle-fill',
    'cancelled': 'bi-x-circle-fill',
    'return': 'bi-arrow-return-left'
  }
  return iconMap[status] || 'bi-clock-history'
}

const getTotalAmountClass = (status) => {
  return status === 'cancelled' ? 'total-cancelled' : 'total-normal'
}

const getPaymentStatusClass = (paymentStatus) => {
  return paymentStatus.includes('thất bại') ? 'payment-failed' : 'payment-success'
}

// Timeline steps - giống HoaDonDetailPage.vue
const trackingSteps = ref([
  { title: 'Chờ xác nhận', icon: 'clock', time: '', status: 0 },
  { title: 'Chờ giao hàng', icon: 'box', time: '', status: 1 },
  { title: 'Đang giao hàng', icon: 'truck', time: '', status: 2 },
  { title: 'Hoàn thành', icon: 'check-circle', time: '', status: 3 },
  { title: 'Đã hủy', icon: 'times-circle', time: '', status: 4 }
])

// Computed property để lấy steps phù hợp với loại đơn hàng và cập nhật thời gian từ lichSuTrangThai
const getTrackingSteps = computed(() => {
  if (!orderDetails.value) {
    return trackingSteps.value.filter(step => step.status !== 4)
  }

  const isBanThuong = orderDetails.value.loaiHoaDon === 'BAN_THUONG' || orderDetails.value.loaiHoaDon === 'NORMAL'
  const isCancelled = orderDetails.value.trangThai === 4

  // Nếu đơn hàng đã hủy - chỉ hiển thị step "Đã hủy"
  if (isCancelled) {
    let cancelTime = ''
    const timeSource = orderDetails.value.ngayCapNhat || orderDetails.value.ngayTao
    if (timeSource) {
      const date = new Date(timeSource)
      cancelTime = date.toLocaleTimeString('vi-VN') + ' ' + date.toLocaleDateString('vi-VN')
    }
    return [{ title: 'Đã hủy', icon: 'times-circle', time: cancelTime, status: 4, isCancelled: true }]
  }

  // Nếu không phải đã hủy, hiển thị timeline bình thường
  if (isBanThuong) {
    // Bán tại quầy: chỉ có 1 bước "Hoàn thành"
    let completionTime = ''
    if (orderDetails.value.ngayTao) {
      const ngayTao = new Date(orderDetails.value.ngayTao)
      completionTime = ngayTao.toLocaleTimeString('vi-VN') + ' ' + ngayTao.toLocaleDateString('vi-VN')
    }
    return [{ title: 'Hoàn thành', icon: 'check-circle', time: completionTime, status: 3, isCompleted: true }]
  } else {
    // Bán online: 4 bước (không bao gồm "Đã hủy" khi chưa hủy)
    // Tạo bản sao của steps để không mutate trackingSteps.value
    const steps = trackingSteps.value.filter(step => step.status !== 4).map(step => ({ ...step }))

    // Cập nhật thời gian từ lichSuTrangThai - luôn cập nhật tất cả
    console.log('🔍 getTrackingSteps - lichSuTrangThai:', orderDetails.value.lichSuTrangThai)

    if (orderDetails.value.lichSuTrangThai && orderDetails.value.lichSuTrangThai.length > 0) {
      // Sắp xếp theo thời gian để đảm bảo thứ tự đúng
      const sortedHistory = [...orderDetails.value.lichSuTrangThai].sort((a, b) => {
        const timeA = new Date(a.thoiGian || a.thoiGianCapNhat || 0).getTime()
        const timeB = new Date(b.thoiGian || b.thoiGianCapNhat || 0).getTime()
        return timeA - timeB
      })

      console.log('🔍 Sorted history:', sortedHistory)

      // Group by status và lấy entry mới nhất cho mỗi status
      const statusMap = new Map()

      sortedHistory.forEach((trangThaiItem) => {
        const status = trangThaiItem.trangThai
        // Kiểm tra nhiều field có thể có thời gian
        const thoiGian = trangThaiItem.thoiGian || trangThaiItem.thoiGianCapNhat || trangThaiItem.ngayTao || trangThaiItem.time

        if (thoiGian) {
          try {
            const thoiGianDate = new Date(thoiGian)
            if (!isNaN(thoiGianDate.getTime())) {
              // Lưu entry mới nhất cho mỗi status
              if (!statusMap.has(status) || new Date(thoiGian) > new Date(statusMap.get(status).thoiGian)) {
                statusMap.set(status, { ...trangThaiItem, thoiGian })
              }
            }
          } catch (e) {
            console.error(`❌ Error parsing date for status ${status}:`, e, thoiGian)
          }
        }
      })

      console.log('🔍 Status map:', Array.from(statusMap.entries()))

      // Cập nhật thời gian cho các step
      statusMap.forEach((trangThaiItem, status) => {
        const thoiGian = trangThaiItem.thoiGian || trangThaiItem.thoiGianCapNhat || trangThaiItem.ngayTao || trangThaiItem.time

        if (thoiGian) {
          try {
            const thoiGianDate = new Date(thoiGian)
            if (!isNaN(thoiGianDate.getTime())) {
              const timeString = thoiGianDate.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) + ' ' +
                thoiGianDate.toLocaleDateString('vi-VN')

              const step = steps.find(s => s.status === status)

              if (step) {
                step.time = timeString
                console.log(`✅ Set time for step ${status} (${step.title}):`, timeString)
              } else {
                console.warn(`⚠️ No step found for status ${status}`)
              }
            }
          } catch (e) {
            console.error(`❌ Error formatting date for status ${status}:`, e)
          }
        }
      })

      console.log('🔍 Final steps with times:', steps.map(s => ({ title: s.title, status: s.status, time: s.time })))
    } else {
      console.warn('⚠️ No lichSuTrangThai found')
    }

    // Fallback: Nếu thiếu thời gian cho các step, suy ra từ status hiện tại
    // Nếu status > 0, các step trước đó đã hoàn thành (có thể chưa có trong lichSuTrangThai)
    const currentStatus = orderDetails.value.trangThai
    const currentIndex = getCurrentStepIndex.value

    // Nếu có ngayCapNhat, dùng nó cho step hiện tại
    if (orderDetails.value.ngayCapNhat && currentIndex >= 0 && currentIndex < steps.length) {
      const currentStep = steps[currentIndex]
      if (!currentStep.time) {
        try {
          const ngayCapNhat = new Date(orderDetails.value.ngayCapNhat)
          if (!isNaN(ngayCapNhat.getTime())) {
            currentStep.time = ngayCapNhat.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) + ' ' +
              ngayCapNhat.toLocaleDateString('vi-VN')
            console.log(`⏰ Fallback: Set time for current step ${currentStep.title} from ngayCapNhat:`, currentStep.time)
          }
        } catch (e) {
          console.error('❌ Error formatting ngayCapNhat:', e)
        }
      }
    }

    // Nếu có ngayTao, dùng nó cho step đầu tiên (nếu chưa có time)
    if (orderDetails.value.ngayTao && steps[0] && !steps[0].time) {
      try {
        const ngayTao = new Date(orderDetails.value.ngayTao)
        if (!isNaN(ngayTao.getTime())) {
          steps[0].time = ngayTao.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) + ' ' +
            ngayTao.toLocaleDateString('vi-VN')
          console.log(`⏰ Fallback: Set time for step 0 (${steps[0].title}) from ngayTao:`, steps[0].time)
        }
      } catch (e) {
        console.error('❌ Error formatting ngayTao:', e)
      }
    }

    // Nếu status > 0 và các step trước đó chưa có time, dùng ngayTao làm thời gian ước tính
    // (vì các step đã completed nhưng có thể chưa có entry trong lichSuTrangThai)
    if (currentStatus > 0 && orderDetails.value.ngayTao) {
      try {
        const ngayTao = new Date(orderDetails.value.ngayTao)
        if (!isNaN(ngayTao.getTime())) {
          for (let i = 0; i < currentIndex && i < steps.length; i++) {
            if (!steps[i].time) {
              // Ước tính: mỗi step cách nhau 1 giờ (hoặc có thể dùng ngayTao)
              // Hoặc đơn giản dùng ngayTao cho tất cả step đã completed nhưng chưa có time
              steps[i].time = ngayTao.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) + ' ' +
                ngayTao.toLocaleDateString('vi-VN')
              console.log(`⏰ Fallback: Set estimated time for completed step ${steps[i].title} from ngayTao:`, steps[i].time)
            }
          }
        }
      } catch (e) {
        console.error('❌ Error in fallback time estimation:', e)
      }
    }

    console.log('🔍 Final steps after fallback:', steps.map(s => ({ title: s.title, status: s.status, time: s.time })))

    return steps
  }
})

// Get current step index based on status
const getCurrentStepIndex = computed(() => {
  if (!orderDetails.value) return 0

  const status = orderDetails.value.trangThai
  const loaiHoaDon = orderDetails.value.loaiHoaDon
  const isBanThuong = loaiHoaDon === 'BAN_THUONG' || loaiHoaDon === 'NORMAL'
  const isCancelled = status === 4

  if (isCancelled) return 0
  if (isBanThuong) return 0

  switch (status) {
    case 0: return 0  // Chờ xác nhận
    case 1: return 1  // Chờ giao hàng
    case 2: return 2  // Đang giao hàng
    case 3: return 3  // Hoàn thành
    default: return 0
  }
})

// Get step icon - hiển thị check nếu completed hoặc active với time
const getStepIcon = (step, index) => {
  if (!orderDetails.value) return 'bi-check-circle-fill'

  const currentIndex = getCurrentStepIndex.value
  const isCompleted = index < currentIndex || (step.time && index === currentIndex)
  const isCancelled = step.isCancelled

  if (isCancelled) {
    return 'bi-x-circle-fill'
  }

  if (isCompleted) {
    return 'bi-check-circle-fill'
  }

  return 'bi-check-circle-fill' // Fallback
}

// Get original icon for active step without time
const getStepIconOriginal = (iconName) => {
  const iconMap = {
    'clock': 'bi-clock-history',
    'box': 'bi-box',
    'truck': 'bi-truck',
    'check-circle': 'bi-check-circle-fill',
    'times-circle': 'bi-x-circle-fill'
  }
  return iconMap[iconName] || 'bi-clock-history'
}

// Auto-refresh order status
const startAutoRefresh = () => {
  // Clear existing interval
  if (refreshInterval.value) {
    clearInterval(refreshInterval.value)
  }

  // Only refresh if we have an order displayed
  if (!orderDetails.value || !orderDetails.value.maHoaDon) {
    return
  }

  // Refresh every 5 seconds to get latest status
  refreshInterval.value = setInterval(async () => {
    // Check if orderDetails still exists and has maHoaDon
    if (!orderDetails.value || !orderDetails.value.maHoaDon) {
      stopAutoRefresh()
      return
    }

    try {
      const { data: result } = await api.get(`/api/hoa-don/tracking/${orderDetails.value.maHoaDon}`)

      if (result && result.maHoaDon && orderDetails.value) {
        const trangThai = typeof result.trangThai === 'number' ? result.trangThai : parseInt(result.trangThai) || 0

        // Only update if status changed and orderDetails still exists
        if (orderDetails.value && orderDetails.value.trangThai !== trangThai) {
          console.log('🔄 Status changed from', orderDetails.value.trangThai, 'to', trangThai)
          orderDetails.value.trangThai = trangThai
          // Also update other fields that might have changed
          orderDetails.value.ngayCapNhat = result.ngayCapNhat
          orderDetails.value.lichSuTrangThai = result.lichSuTrangThai || []
          orderDetails.value.loaiHoaDon = result.loaiHoaDon || orderDetails.value.loaiHoaDon || 'BAN_ONLINE'
        } else if (orderDetails.value && result.lichSuTrangThai) {
          // Update lichSuTrangThai even if status hasn't changed (in case new log entry was added)
          const oldHistoryLength = orderDetails.value.lichSuTrangThai?.length || 0
          const newHistoryLength = result.lichSuTrangThai?.length || 0
          if (newHistoryLength > oldHistoryLength) {
            console.log('🔄 New status log entry added')
            orderDetails.value.lichSuTrangThai = result.lichSuTrangThai
            orderDetails.value.loaiHoaDon = result.loaiHoaDon || orderDetails.value.loaiHoaDon || 'BAN_ONLINE'
          }
        }
      }
    } catch (error) {
      console.error('Error refreshing order status:', error)
      // Don't stop on error, just log it
    }
  }, 5000) // Refresh every 5 seconds
}

const stopAutoRefresh = () => {
  if (refreshInterval.value) {
    clearInterval(refreshInterval.value)
    refreshInterval.value = null
  }
}

const viewOrderDetail = async (order) => {
  // Use tracking API to get full order details
  const orderId = order.maHoaDon || order.id
  if (!orderId) return

  try {
    const { data: result } = await api.get(`/api/hoa-don/tracking/${orderId}`)

    if (result && result.maHoaDon) {
      const products = result.danhSachSanPham || []

      // Get product image URL helper
      const getProductImage = (imagePath) => {
        if (!imagePath) return '/placeholder-product.jpg'
        if (imagePath.startsWith('http')) return imagePath
        return `${API_BASE_URL}${imagePath.startsWith('/') ? '' : '/'}${imagePath}`
      }

      // Ensure trangThai is a number
      const trangThai = typeof result.trangThai === 'number' ? result.trangThai : parseInt(result.trangThai) || 0

      orderDetails.value = {
        id: result.maHoaDon,
        maHoaDon: result.maHoaDon,
        tenKhachHang: result.tenKhachHang,
        soDienThoai: result.soDienThoai,
        email: result.email || '',
        diaChi: result.diaChi || '',
        tinhThanh: result.tinhThanh || '',
        quanHuyen: result.quanHuyen || '',
        ngayTao: result.ngayTao,
        ngayDat: result.ngayDat || result.ngayTao,
        ngayCapNhat: result.ngayCapNhat,
        loaiHoaDon: result.loaiHoaDon || 'BAN_ONLINE', // Important for timeline
        tongTienHang: result.tongTienHang || 0,
        tongTien: result.tongTien || result.tongTienHang || 0,
        tongTienSauGiam: result.tongTienSauGiam || 0,
        thanhTien: result.thanhTien || result.tongTienSauGiam || 0,
        giamGia: result.giamGia || 0,
        phiVanChuyen: result.phiVanChuyen || 0,
        trangThai: trangThai, // Ensure it's a number
        maVoucher: result.maVoucher,
        phieuGiamGia: result.phieuGiamGia,
        phieuGiamGiaId: result.phieuGiamGiaId,
        ghiChu: result.ghiChu,
        lichSuTrangThai: result.lichSuTrangThai || [],
        chiTietDonHang: products.map(item => ({
          ...item,
          tenSanPham: item.tenSanPham || '',
          soLuong: item.soLuong || 1,
          donGia: item.donGia || item.gia || 0,
          thanhTien: item.thanhTien || (item.donGia || item.gia || 0) * (item.soLuong || 1),
          tenRam: item.tenRam || '',
          tenRom: item.tenRom || '',
          tenMauSac: item.tenMauSac || '',
          hinhAnh: getProductImage(item.hinhAnh)
        }))
      }

      // Start auto-refresh
      startAutoRefresh()
    }
  } catch (error) {
    console.error('Error loading order details:', error)
    // Fallback: use order data if available
    if (order.originalOrder) {
      const orig = order.originalOrder
      orderDetails.value = {
        id: orig.maHoaDon || order.id,
        maHoaDon: orig.maHoaDon || order.id,
        tenKhachHang: orig.tenKhachHang,
        soDienThoai: orig.soDienThoai,
        email: orig.email || '',
        diaChi: orig.diaChi || '',
        tinhThanh: orig.tinhThanh || '',
        quanHuyen: orig.quanHuyen || '',
        ngayTao: orig.ngayTao,
        ngayDat: orig.ngayDat || orig.ngayTao,
        ngayCapNhat: orig.ngayCapNhat,
        tongTienHang: orig.tongTienHang || 0,
        tongTien: orig.tongTien || 0,
        tongTienSauGiam: orig.tongTienSauGiam || 0,
        thanhTien: orig.thanhTien || orig.tongTienSauGiam || 0,
        giamGia: orig.giamGia || 0,
        phiVanChuyen: orig.phiVanChuyen || 0,
        trangThai: orig.trangThai,
        maVoucher: orig.maVoucher,
        phieuGiamGia: orig.phieuGiamGia,
        phieuGiamGiaId: orig.phieuGiamGiaId,
        ghiChu: orig.ghiChu,
        lichSuTrangThai: orig.lichSuTrangThai || [],
        chiTietDonHang: (orig.danhSachSanPham || orig.chiTietHoaDon || orig.chiTietDonHang || []).map(item => ({
          ...item,
          tenSanPham: item.tenSanPham || '',
          soLuong: item.soLuong || 1,
          donGia: item.donGia || item.gia || 0,
          thanhTien: item.thanhTien || (item.donGia || item.gia || 0) * (item.soLuong || 1),
          tenRam: item.tenRam || '',
          tenRom: item.tenRom || '',
          tenMauSac: item.tenMauSac || '',
          hinhAnh: item.hinhAnh || '/placeholder-product.jpg'
        }))
      }
    }
  }
}

// Filter orders by selected status
const filteredOrders = computed(() => {
  if (selectedStatus.value === 'all') {
    return orders.value
  }

  // Map status tab values to actual status values
  const statusMapping = {
    'completed': ['completed'],
    'cancelled': ['cancelled'],
    'return': ['return']
  }

  const targetStatuses = statusMapping[selectedStatus.value] || []
  return orders.value.filter(order => targetStatuses.includes(order.status))
})

onMounted(async () => {
  // Check user login status
  await checkUserLogin()

  if (isLoggedIn.value) {
    // Load user orders if logged in
    await loadUserOrders()
  } else {
    // Check if order ID is provided in URL params for non-logged in users
    if (route.query.orderId) {
      searchForm.value.orderId = route.query.orderId
      searchOrder()
    }
  }
})

onBeforeUnmount(() => {
  // Clean up auto-refresh interval
  stopAutoRefresh()
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

.tracking-page {
  min-height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
  padding-top: 77px; /* Compensate for fixed header */
}

.tracking-main {
  flex: 1;
  padding: 3rem 0;
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 1rem;
  width: 100%;
}

.page-header {
  text-align: center;
  margin-bottom: 2rem;
}

.page-header h1 {
  font-size: 2.5rem;
  font-weight: 700;
  color: #FF5500;
  margin-bottom: 0.5rem;
}

.page-header p {
  font-size: 1.1rem;
  color: #666;
}

.search-section {
  margin-bottom: 3rem;
}

.search-card {
  background: white;
  border-radius: 15px;
  padding: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.search-card h2 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #4a4a4a;
  margin-bottom: 1.5rem;
}

.search-form {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.form-label {
  font-weight: 600;
  color: #4a4a4a;
  font-size: 0.95rem;
  white-space: nowrap;
  min-width: 100px;
}

.search-input {
  flex: 1;
  min-width: 200px;
  padding: 0.75rem 1rem;
  border: 1px solid #d0d0d0;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s;
  background-color: #e8f4f8;
  color: #333;
}

.search-input:focus {
  outline: none;
  border-color: #FF5500;
  box-shadow: 0 0 0 3px rgba(255, 107, 53, 0.1);
}

.search-input.error {
  border-color: #DC143C;
}

.error-message {
  color: #DC143C;
  font-size: 0.875rem;
  margin-top: 0.25rem;
  width: 100%;
  order: 3;
}

.btn-search {
  background: #FF5500;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s;
  white-space: nowrap;
  height: fit-content;
  font-size: 1rem;
}

.btn-search:hover:not(:disabled) {
  background: #DC143C;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(255, 107, 53, 0.3);
}

.btn-search:disabled {
  background: #6c757d;
  cursor: not-allowed;
  opacity: 0.6;
}

.order-details {
  margin-bottom: 2rem;
}

/* Order Status Progress Bar */
.order-status-progress {
  background: white;
  border-radius: 15px;
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.progress-container {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  position: relative;
  gap: 1rem;
}

.progress-step {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  z-index: 1;
}

.step-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e9ecef;
  color: #999;
  font-size: 1.5rem;
  margin-bottom: 0.75rem;
  transition: all 0.3s;
}

.progress-step.completed .step-icon,
.progress-step.active .step-icon {
  background: #28a745;
  color: white;
}

.progress-step.active:not(.completed) .step-icon {
  background: #FF5500;
  color: white;
}

.progress-step.cancelled .step-icon {
  background: #dc3545;
  color: white;
}

.step-content {
  text-align: center;
}

.step-title {
  font-weight: 600;
  color: #333;
  margin-bottom: 0.25rem;
  font-size: 0.9rem;
}

.step-time {
  font-size: 0.75rem;
  color: #666;
}

.progress-step.completed .step-time,
.progress-step.active .step-time {
  color: #28a745;
  font-weight: 500;
}

.step-connector {
  position: absolute;
  top: 25px;
  left: calc(50% + 25px);
  right: calc(-50% + 25px);
  height: 2px;
  background: #e9ecef;
  z-index: 0;
}

.progress-step.completed ~ .progress-step .step-connector {
  background: #28a745;
}

/* Order Details Content - Two Column Layout */
.order-details-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
  margin-bottom: 2rem;
}

.order-details-left,
.order-details-right {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

/* Detail Card */
.detail-card {
  background: white;
  border-radius: 15px;
  padding: 1.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.card-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 1.5rem;
  padding-bottom: 0.75rem;
  border-bottom: 2px solid #f0f0f0;
}

.detail-grid {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 0.75rem 0;
  border-bottom: 1px solid #f0f0f0;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-label {
  font-weight: 500;
  color: #666;
  min-width: 150px;
}

.detail-value {
  font-weight: 600;
  color: #333;
  text-align: right;
  flex: 1;
}

.detail-value.highlight {
  background: #FFD700;
  padding: 0.25rem 0.75rem;
  border-radius: 6px;
  font-family: monospace;
  display: inline-block;
}

/* Badges */
.badge {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.375rem 0.75rem;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 500;
}

.badge-primary {
  background: #007bff;
  color: white;
}

.badge-success {
  background: #28a745;
  color: white;
}

.badge-warning {
  background: #ffc107;
  color: #333;
}

.badge-info {
  background: #17a2b8;
  color: white;
}

.badge-danger {
  background: #dc3545;
  color: white;
}

.badge-secondary {
  background: #6c757d;
  color: white;
}

/* History List */
.history-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 3px solid #007bff;
}

.history-content {
  flex: 1;
}

.history-action {
  font-weight: 500;
  color: #333;
  margin-bottom: 0.25rem;
}

.history-time {
  font-size: 0.875rem;
  color: #666;
}

.history-status {
  padding: 0.25rem 0.75rem;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 500;
}

.history-status.status-processed {
  background: #d4edda;
  color: #155724;
}

.history-status.status-pending {
  background: #fff3cd;
  color: #856404;
}

.history-empty {
  text-align: center;
  padding: 2rem;
  color: #999;
  font-style: italic;
}

/* Summary List */
.summary-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.summary-item {
  display: flex;
  flex-direction: column;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
}

.summary-item.total-item {
  background: #fff;
  border: 2px solid #FF5500;
  border-radius: 8px;
}

.summary-label {
  font-weight: 600;
  color: #333;
  margin-bottom: 0.25rem;
}

.summary-note {
  font-size: 0.875rem;
  color: #666;
  margin-bottom: 0.5rem;
}

.summary-value {
  font-size: 1.25rem;
  font-weight: 700;
  color: #333;
}

.summary-value.discount {
  color: #dc3545;
}

.summary-item.total-item .summary-value {
  color: #FF5500;
  font-size: 1.5rem;
}

/* Product List Card */
.product-list-card {
  margin-top: 2rem;
}

.products-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.product-item {
  display: flex;
  align-items: flex-start;
  gap: 1.5rem;
  padding: 1.5rem;
  background: #f8f9fa;
  border-radius: 12px;
  border: 1px solid #e9ecef;
}

.product-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  flex: 1;
}

.product-name {
  font-size: 1.125rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 0.5rem;
}

.product-specs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  flex-wrap: wrap;
}

.spec-badge {
  background: #e9ecef;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.875rem;
  color: #666;
}

.product-price {
  font-size: 1.125rem;
  font-weight: 700;
  color: #FF5500;
}

.product-imei {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  align-items: flex-end;
}

.imei-label {
  font-size: 0.875rem;
  font-weight: 500;
  color: #666;
}

.imei-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  align-items: flex-end;
}

.imei-badge {
  background: #FF5500;
  color: white;
  padding: 0.375rem 0.75rem;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 600;
  font-family: monospace;
}

.order-card {
  background: white;
  border-radius: 16px;
  padding: 2.5rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  margin-bottom: 2rem;
  border: 1px solid #e5e7eb;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #f0f0f0;
}

.order-header h2 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.order-id {
  background: #FF5500;
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-weight: 600;
  font-family: monospace;
}

.status-section {
  margin-bottom: 2rem;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border-radius: 25px;
  font-weight: 600;
  font-size: 1rem;
}

.status-waiting {
  background: #fff3cd;
  color: #856404;
  border: 1px solid #ffeaa7;
}

.status-confirmed {
  background: #d1ecf1;
  color: #0c5460;
  border: 1px solid #bee5eb;
}

.status-shipping {
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.status-delivered {
  background: #d1ecf1;
  color: #0c5460;
  border: 1px solid #bee5eb;
}

.status-cancelled {
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

/* Order Status Timeline - giống HoaDonDetailPage.vue */
.order-status-section {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 2rem;
  border: 1px solid #e2e8f0;
  padding: 24px;
}

.status-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.status-timeline {
  background: white;
  border-radius: 16px;
  padding: 24px;
  border: 1px solid #e2e8f0;
}

.status-steps {
  display: flex;
  justify-content: space-between;
  position: relative;
  padding: 20px 0;
}

.status-steps::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 2px;
  background: #e2e8f0;
  z-index: 1;
}

.status-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  z-index: 2;
  flex: 1;
}

.status-step.completed:not(:last-child)::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  right: -50%;
  height: 2px;
  background: #10b981;
  z-index: 1;
  transform: translateY(-50%);
}

.status-step .step-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f97316;
  color: white;
  font-size: 18px;
  margin-bottom: 8px;
  transition: all 0.3s ease;
}

.status-step.active .step-icon {
  background: #f97316;
  color: white;
}

.status-step.completed .step-icon {
  background: #10b981;
  color: white;
}

.status-step.isCancelled .step-icon {
  background: #ef4444;
  color: white;
  box-shadow: 0 0 20px rgba(239, 68, 68, 0.5);
}

.status-step .step-content {
  text-align: center;
}

.status-step .step-title {
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 4px;
  font-size: 14px;
}

.status-step .step-time {
  font-size: 12px;
  color: #718096;
}

.order-info {
  margin-bottom: 2rem;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1rem;
}

.info-item {
  display: block;
  align-items: center;
  padding: 0.75rem 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-label {
  font-weight: 600;
  color: #666;
  padding-right: 10px;
}

.info-value {
  font-weight: 600;
  color: #333;
  text-align: right;
}

.order-items {
  margin-bottom: 2rem;
}

.order-items h3 {
  font-size: 1.4rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 1.5rem;
  padding-bottom: 0.75rem;
  border-bottom: 2px solid #e5e7eb;
}

.items-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.item-card {
  display: flex;
  align-items: flex-start;
  gap: 1.25rem;
  padding: 1.25rem;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: white;
  position: relative;
  transition: all 0.3s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.item-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.item-image {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  overflow: hidden;
  background: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding-right: 140px; /* Space for price on the right */
}

.item-info h4 {
  font-size: 1.1rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
  line-height: 1.4;
}

.item-specs {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  margin-top: 0.25rem;
}

.spec-badge {
  background: #f3f4f6;
  padding: 0.35rem 0.75rem;
  border-radius: 6px;
  font-size: 0.85rem;
  color: #374151;
  font-weight: 500;
  border: 1px solid #e5e7eb;
}

.item-quantity {
  font-size: 0.9rem;
  color: #666;
}

.item-imei {
  margin-top: 0.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.imei-label {
  font-weight: 600;
  color: #666;
  font-size: 0.9rem;
}

.imei-tag {
  background: #FF5500;
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.85rem;
  font-weight: 600;
  font-family: monospace;
}

.item-price {
  font-weight: 700;
  color: #FF5500;
  font-size: 1.1rem;
  position: absolute;
  right: 1rem;
  top: 1rem;
}

.no-products {
  text-align: center;
  padding: 2rem;
  color: #999;
  font-style: italic;
}

.discount-text {
  color: #dc3545;
  font-weight: 600;
}

.order-summary {
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 12px;
  padding: 1.75rem;
  border: 1px solid #e5e7eb;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  color: #4b5563;
  font-size: 1rem;
}

.summary-row.total {
  font-size: 1.3rem;
  font-weight: 700;
  color: #1f2937;
  border-top: 2px solid #e5e7eb;
  padding-top: 1rem;
  margin-top: 1rem;
  margin-bottom: 0;
}

.summary-row.total span:last-child {
  color: #FF5500;
}

.no-order {
  margin-bottom: 2rem;
}

.no-order-card {
  background: white;
  border-radius: 15px;
  padding: 3rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  text-align: center;
}

.no-order-card i {
  font-size: 4rem;
  color: #ffc107;
  margin-bottom: 1rem;
}

.no-order-card h3 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 1rem;
}

.no-order-card p {
  color: #666;
  margin-bottom: 1.5rem;
}

.no-order-info {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 1.5rem;
  margin-bottom: 2rem;
  text-align: left;
}

.no-order-info h4 {
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 1rem;
}

.no-order-info ul {
  margin: 0;
  padding-left: 1.5rem;
}

.no-order-info li {
  color: #666;
  margin-bottom: 0.5rem;
  line-height: 1.5;
}

.no-order-info strong {
  color: #FF5500;
}

.no-order-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

.btn-retry,
.btn-support {
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s;
  border: none;
  min-width: 150px;
  justify-content: center;
}

.btn-retry {
  background: #FF5500;
  color: white;
}

.btn-retry:hover {
  background: #DC143C;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(255, 107, 53, 0.3);
}

.btn-support {
  background: #17a2b8;
  color: white;
}

.btn-support:hover {
  background: #138496;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(23, 162, 184, 0.3);
}

.tracking-footer {
  background: #000000;
  color: white;
  text-align: center;
  padding: 2rem;
  margin-top: auto;
}

/* Responsive */
@media (min-width: 1400px) {
  .tracking-main {
    max-width: 1400px;
  }

  .container {
    max-width: 1400px;
  }
}

@media (max-width: 1399px) {
  .tracking-main {
    max-width: 100%;
    padding: 3rem 2rem;
  }

  .container {
    max-width: 100%;
    padding: 0 2rem;
  }
}

@media (max-width: 768px) {
  .tracking-page {
    padding-top: 67px; /* Adjust for smaller header on mobile */
  }

  .container {
    padding: 0 1rem;
    max-width: 100%;
  }

  .search-form {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .order-header {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .item-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .item-price {
    align-self: flex-end;
  }

  /* Order Details Responsive */
  .order-details-content {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }

  .progress-container {
    flex-wrap: wrap;
    gap: 0.5rem;
  }

  .progress-step {
    flex: 0 0 calc(50% - 0.5rem);
    min-width: 120px;
  }

  .step-connector {
    display: none;
  }

  .detail-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }

  .detail-value {
    text-align: left;
  }

  .product-item {
    flex-direction: column;
    gap: 1rem;
  }

  .product-imei {
    align-items: flex-start;
    width: 100%;
  }

  .imei-list {
    align-items: flex-start;
  }
}

/* New Design Styles */
/* Status Tabs */
.status-tabs {
  display: flex;
  background: white;
  border-radius: 8px;
  padding: 0.5rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-bottom: 1px solid #e0e0e0;
}

.tab-btn {
  flex: 1;
  padding: 0.75rem 1rem;
  border: none;
  background: transparent;
  color: #666;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  border-radius: 6px;
}

.tab-btn:hover {
  color: #FF5500;
  background: rgba(255, 85, 0, 0.05);
}

.tab-btn.active {
  color: #FF5500;
  font-weight: 600;
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: -0.5rem;
  left: 50%;
  transform: translateX(-50%);
  width: 30px;
  height: 3px;
  background: #FF5500;
  border-radius: 2px;
}

/* Search Bar */
.search-bar {
  position: relative;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.search-icon {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  color: #999;
  font-size: 1.1rem;
}


/* Orders List */
.orders-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-top: 2rem;
}

.order-card {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  border: 1px solid #e9ecef;
  transition: all 0.3s;
}

.order-card:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.order-id-section {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.order-label {
  font-weight: 500;
  color: #333;
}

.order-id {
  font-weight: 600;
  color: #333;
  font-family: monospace;
}

.delivery-info {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.9rem;
  color: #666;
}

.bullet {
  color: #999;
}

.delivery-text {
  color: #666;
}

.status-text {
  font-weight: 600;
  font-size: 0.9rem;
}

.status-text.status-completed {
  color: #28a745;
}

.status-text.status-cancelled {
  color: #dc3545;
}

.status-text.status-return {
  color: #6c757d;
}

.status-pending {
  background: #fff3cd;
  color: #856404;
  border: 1px solid #ffeaa7;
}

.status-shipping {
  background: #d1ecf1;
  color: #0c5460;
  border: 1px solid #bee5eb;
}

.status-completed {
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.status-cancelled {
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.status-return {
  background: #e2e3e5;
  color: #383d41;
  border: 1px solid #d6d8db;
}

.order-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
}

.product-section {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  flex: 1;
}

.product-image {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  overflow: hidden;
  background: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-details {
  flex: 1;
}

.product-name {
  font-weight: 500;
  color: #333;
  margin-bottom: 0.25rem;
  line-height: 1.4;
}

.product-description {
  font-size: 0.9rem;
  color: #666;
  line-height: 1.3;
}

.order-summary {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.5rem;
  min-width: 200px;
}

.total-section {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.total-label {
  font-weight: 500;
  color: #333;
}

.total-amount {
  font-weight: 600;
  font-size: 1rem;
}

.total-amount.total-normal {
  color: #333;
}

.total-amount.total-cancelled {
  color: #dc3545;
}

.payment-status {
  margin-top: 0.25rem;
}

.payment-text {
  font-size: 0.9rem;
  font-weight: 500;
}

.payment-text.payment-failed {
  color: #dc3545;
}

.payment-text.payment-success {
  color: #28a745;
}

.order-actions {
  margin-top: 0.5rem;
}

.btn-view-detail {
  background: transparent;
  color: #FF5500;
  border: 1px solid #FF5500;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 0.9rem;
}

.btn-view-detail:hover {
  background: #FF5500;
  color: white;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(255, 85, 0, 0.3);
}

/* No Orders State */
.no-orders-state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.no-orders-content {
  text-align: center;
}

.no-orders-illustration {
  margin-bottom: 2rem;
}

.illustration-circle {
  width: 120px;
  height: 120px;
  background: #f5f5f5;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  position: relative;
}

.clipboard-icon {
  width: 60px;
  height: 60px;
  position: relative;
}

.clipboard-body {
  width: 40px;
  height: 50px;
  background: #f5d76e;
  border-radius: 4px;
  position: absolute;
  top: 5px;
  left: 10px;
}

.clipboard-clip {
  width: 20px;
  height: 15px;
  background: #d4a574;
  border-radius: 2px;
  position: absolute;
  top: 0;
  left: 15px;
}

.checkmarks {
  position: absolute;
  top: 15px;
  left: 15px;
}

.checkmark {
  width: 8px;
  height: 4px;
  background: #4caf50;
  border-radius: 2px;
  margin-bottom: 3px;
  transform: rotate(-45deg);
}

.checkmark::after {
  content: '';
  position: absolute;
  width: 4px;
  height: 8px;
  background: #4caf50;
  border-radius: 2px;
  top: -2px;
  right: -2px;
  transform: rotate(90deg);
}

.pencil {
  width: 20px;
  height: 3px;
  background: #2196f3;
  border-radius: 2px;
  position: absolute;
  bottom: 10px;
  right: 5px;
  transform: rotate(45deg);
}

.pencil::after {
  content: '';
  position: absolute;
  width: 3px;
  height: 3px;
  background: #ffeb3b;
  border-radius: 50%;
  top: -1px;
  right: -1px;
}

.floating-dots {
  position: absolute;
  top: -10px;
  left: -10px;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  position: absolute;
}

.dot-1 {
  background: #ff9800;
  top: 0;
  left: 0;
}

.dot-2 {
  background: #2196f3;
  top: 8px;
  left: 12px;
}

.dot-3 {
  background: #4caf50;
  top: 15px;
  left: 5px;
}

.no-orders-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #666;
  margin: 0;
}

/* Loading State */
.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.loading-content {
  text-align: center;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #FF5500;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-content p {
  color: #666;
  font-size: 1rem;
}

/* Product Count */
.product-count {
  font-size: 0.85rem;
  color: #999;
  margin-top: 0.25rem;
}

/* Customer Info Section */
.customer-info-section {
  margin-bottom: 2rem;
}

.customer-info-card {
  background: white;
  border-radius: 16px;
  padding: 2rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #e5e7eb;
}

.customer-info-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #f0f0f0;
}

.customer-info-header i {
  font-size: 2rem;
  color: #FF5500;
}

.customer-info-header h2 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.customer-info-content {
  margin-top: 1rem;
}

.customer-info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
}

.customer-info-item {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 10px;
  border-left: 4px solid #FF5500;
}

.customer-info-item .info-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;
  color: #666;
  font-size: 0.9rem;
}

.customer-info-item .info-label i {
  color: #FF5500;
  font-size: 1.1rem;
}

.customer-info-item .info-value {
  font-weight: 600;
  color: #333;
  font-size: 1.1rem;
  padding-left: 1.75rem;
}

/* Responsive for new design */
@media (max-width: 768px) {
  .status-tabs {
    flex-wrap: wrap;
    gap: 0.5rem;
  }

  .tab-btn {
    flex: none;
    min-width: 120px;
  }

  .search-input {
    font-size: 0.9rem;
  }

  .illustration-circle {
    width: 100px;
    height: 100px;
  }

  .clipboard-icon {
    width: 50px;
    height: 50px;
  }

  .order-content {
    flex-direction: column;
    gap: 1rem;
  }

  .order-summary {
    align-items: flex-start;
    min-width: auto;
  }

  .product-section {
    gap: 0.75rem;
  }

  .product-image {
    width: 50px;
    height: 50px;
  }

  .customer-info-card {
    padding: 1.5rem;
  }

  .customer-info-header h2 {
    font-size: 1.25rem;
  }

  .customer-info-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .customer-info-item {
    padding: 0.875rem;
  }
}
</style>
