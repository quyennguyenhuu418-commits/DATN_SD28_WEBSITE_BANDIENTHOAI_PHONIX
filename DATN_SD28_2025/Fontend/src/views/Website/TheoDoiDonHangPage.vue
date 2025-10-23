<template>
  <div class="tracking-page">
    <!-- Header -->
    <HeaderLayout/>

    <!-- Main Content -->
    <main class="tracking-main" style="margin-top: 80px;">
      <div class="container">
        <div class="page-header">
          <h1>Theo dõi đơn hàng</h1>
          <p>Kiểm tra trạng thái đơn hàng của bạn</p>
        </div>

        <!-- Search Form -->
        <div class="search-section">
          <div class="search-card">
            <h2>Tìm đơn hàng</h2>
            <form @submit.prevent="searchOrder" class="search-form">
              <div class="form-group">
                <label for="orderId">Mã đơn hàng</label>
                <input
                  type="text"
                  id="orderId"
                  v-model="searchForm.orderId"
                  placeholder="Nhập mã đơn hàng"
                  :class="{ 'error': errors.orderId }"
                >
                <span v-if="errors.orderId" class="error-message">{{ errors.orderId }}</span>
              </div>
              <div class="form-group">
                <label for="phone">Số điện thoại</label>
                <input
                  type="tel"
                  id="phone"
                  v-model="searchForm.phone"
                  placeholder="Nhập số điện thoại"
                  :class="{ 'error': errors.phone }"
                >
                <span v-if="errors.phone" class="error-message">{{ errors.phone }}</span>
              </div>
              <button type="submit" class="btn-search" :disabled="isSearching">
                <i v-if="isSearching" class="bi bi-hourglass-split"></i>
                <i v-else class="bi bi-search"></i>
                {{ isSearching ? 'Đang tìm...' : 'Tìm đơn hàng' }}
              </button>
            </form>
          </div>
        </div>

        <!-- Order Details -->
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

            <!-- Order Info -->
            <div class="order-info">
              <div class="info-grid">
                <div class="info-item">
                  <span class="info-label">Khách hàng:</span>
                  <span class="info-value">{{ orderDetails.tenKhachHang }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">Số điện thoại:</span>
                  <span class="info-value">{{ orderDetails.soDienThoai }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">Địa chỉ:</span>
                  <span class="info-value">{{ orderDetails.diaChi }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">Ngày đặt:</span>
                  <span class="info-value">{{ formatDate(orderDetails.ngayTao) }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">Phương thức giao hàng:</span>
                  <span class="info-value">{{ getDeliveryMethodText(orderDetails.phuongThucGiaoHang) }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">Phương thức thanh toán:</span>
                  <span class="info-value">{{ getPaymentMethodText(orderDetails.phuongThucThanhToan) }}</span>
                </div>
              </div>
            </div>

            <!-- Order Items -->
            <div class="order-items">
              <h3>Sản phẩm đã đặt</h3>
              <div class="items-list">
                <div v-for="item in orderDetails.chiTietDonHang" :key="item.id" class="item-card">
                  <div class="item-info">
                    <h4>{{ item.tenSanPham }}</h4>
                    <div class="item-specs">
                      <span v-if="item.tenRam">{{ item.tenRam }}</span>
                      <span v-if="item.tenRom">{{ item.tenRom }}</span>
                      <span v-if="item.tenMauSac">{{ item.tenMauSac }}</span>
                    </div>
                    <div class="item-quantity">Số lượng: {{ item.soLuong }}</div>
                  </div>
                  <div class="item-price">{{ formatPrice(item.thanhTien) }}</div>
                </div>
              </div>
            </div>

            <!-- Order Summary -->
            <div class="order-summary">
              <div class="summary-row">
                <span>Tạm tính:</span>
                <span>{{ formatPrice(orderDetails.tongTien - orderDetails.phiVanChuyen) }}</span>
              </div>
              <div class="summary-row">
                <span>Phí vận chuyển:</span>
                <span>{{ formatPrice(orderDetails.phiVanChuyen) }}</span>
              </div>
              <div class="summary-row total">
                <span>Tổng cộng:</span>
                <span>{{ formatPrice(orderDetails.tongTien) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- No Order Found -->
        <div v-if="showNoOrder" class="no-order">
          <div class="no-order-card">
            <i class="bi bi-exclamation-circle"></i>
            <h3>Không tìm thấy đơn hàng</h3>
            <p>Vui lòng kiểm tra lại mã đơn hàng và số điện thoại</p>
            
            <div class="no-order-info">
              <h4>Gợi ý:</h4>
              <ul>
                <li>Kiểm tra lại mã đơn hàng (có thể có lỗi chính tả)</li>
                <li>Đảm bảo số điện thoại chính xác</li>
                <li>Đơn hàng có thể chưa được xử lý (vui lòng thử lại sau)</li>
                <li>Hệ thống có thể đang bảo trì (thử lại sau ít phút)</li>
                <li>Liên hệ hotline <strong>1900 1234</strong> để được hỗ trợ</li>
              </ul>
            </div>
            
            <div class="no-order-actions">
              <button @click="resetSearch" class="btn-retry">
                <i class="bi bi-arrow-clockwise"></i>
                Thử lại
              </button>
              <button @click="contactSupport" class="btn-support">
                <i class="bi bi-telephone"></i>
                Liên hệ hỗ trợ
              </button>
            </div>
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
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import HeaderLayout from '@/views/Website/HeaderLayout.vue'
import FooterLayout from '@/views/Website/FooterLayout.vue'

const router = useRouter()
const route = useRoute()

const API_BASE_URL = 'http://localhost:8080'

// Form data
const searchForm = ref({
  orderId: '',
  phone: ''
})

const errors = ref({})
const isSearching = ref(false)
const orderDetails = ref(null)
const showNoOrder = ref(false)

// Methods
const searchOrder = async () => {
  if (!validateSearchForm()) {
    return
  }

  isSearching.value = true
  showNoOrder.value = false
  orderDetails.value = null

  try {
    const response = await fetch(`${API_BASE_URL}/api/hoa-don/ma-hoa-don/${searchForm.value.orderId}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      }
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const result = await response.json()
    
    if (result && result.maHoaDon) {
      // Transform the response to match our expected format
      orderDetails.value = {
        id: result.maHoaDon,
        tenKhachHang: result.tenKhachHang,
        soDienThoai: result.soDienThoai,
        email: result.email,
        diaChi: result.diaChi,
        tinhThanh: result.tinhThanh,
        quanHuyen: result.quanHuyen,
        ngayTao: result.ngayTao,
        phuongThucGiaoHang: result.phuongThucGiaoHang,
        phuongThucThanhToan: result.phuongThucThanhToan,
        tongTien: result.tongTien,
        phiVanChuyen: result.phiVanChuyen,
        trangThai: result.trangThai,
        chiTietDonHang: result.chiTietHoaDon || []
      }
    } else {
      showNoOrder.value = true
    }
  } catch (error) {
    console.error('Error searching order:', error)
    showNoOrder.value = true
  } finally {
    isSearching.value = false
  }
}

const validateSearchForm = () => {
  errors.value = {}

  if (!searchForm.value.orderId.trim()) {
    errors.value.orderId = 'Vui lòng nhập mã đơn hàng'
  }

  if (!searchForm.value.phone.trim()) {
    errors.value.phone = 'Vui lòng nhập số điện thoại'
  } else if (!/^[0-9]{10,11}$/.test(searchForm.value.phone.replace(/\s/g, ''))) {
    errors.value.phone = 'Số điện thoại không hợp lệ'
  }

  return Object.keys(errors.value).length === 0
}

const resetSearch = () => {
  searchForm.value = { orderId: '', phone: '' }
  errors.value = {}
  orderDetails.value = null
  showNoOrder.value = false
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
  if (!dateString) return 'N/A'
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getStatusClass = (status) => {
  const statusMap = {
    'CHO_XAC_NHAN': 'status-waiting',
    'DA_XAC_NHAN': 'status-confirmed',
    'DANG_GIAO': 'status-shipping',
    'DA_GIAO': 'status-delivered',
    'DA_HUY': 'status-cancelled'
  }
  return statusMap[status] || 'status-waiting'
}

const getStatusIcon = (status) => {
  const iconMap = {
    'CHO_XAC_NHAN': 'bi bi-clock',
    'DA_XAC_NHAN': 'bi bi-check-circle',
    'DANG_GIAO': 'bi bi-truck',
    'DA_GIAO': 'bi bi-check-circle-fill',
    'DA_HUY': 'bi bi-x-circle'
  }
  return iconMap[status] || 'bi bi-clock'
}

const getStatusText = (status) => {
  const textMap = {
    'CHO_XAC_NHAN': 'Chờ xác nhận',
    'DA_XAC_NHAN': 'Đã xác nhận',
    'DANG_GIAO': 'Đang giao',
    'DA_GIAO': 'Đã giao',
    'DA_HUY': 'Đã hủy'
  }
  return textMap[status] || 'Chờ xác nhận'
}

const getDeliveryMethodText = (method) => {
  const methodMap = {
    'standard': 'Giao hàng tiêu chuẩn',
    'express': 'Giao hàng nhanh',
    'ghn': 'Ship hỏa tốc'
  }
  return methodMap[method] || method
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

onMounted(() => {
  // Check if order ID and phone are provided in URL params
  if (route.query.orderId && route.query.phone) {
    searchForm.value.orderId = route.query.orderId
    searchForm.value.phone = route.query.phone
    searchOrder()
  }
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
}

.tracking-main {
  flex: 1;
  padding: 3rem 0;
}

.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 2rem;
}

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
  font-size: 1.5rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 1.5rem;
}

.search-form {
  display: grid;
  grid-template-columns: 1fr 1fr auto;
  gap: 1rem;
  align-items: end;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  font-weight: 600;
  color: #333;
  margin-bottom: 0.5rem;
}

.form-group input {
  padding: 0.75rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s;
}

.form-group input:focus {
  outline: none;
  border-color: var(--phoenix-primary);
  box-shadow: 0 0 0 3px rgba(255, 107, 53, 0.1);
}

.form-group input.error {
  border-color: var(--phoenix-accent);
}

.error-message {
  color: var(--phoenix-accent);
  font-size: 0.875rem;
  margin-top: 0.25rem;
}

.btn-search {
  background: var(--phoenix-primary);
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
}

.btn-search:hover:not(:disabled) {
  background: var(--phoenix-accent);
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

.order-card {
  background: white;
  border-radius: 15px;
  padding: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
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
  background: var(--phoenix-primary);
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

.order-info {
  margin-bottom: 2rem;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1rem;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-label {
  font-weight: 600;
  color: #666;
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
  font-size: 1.3rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 1rem;
}

.items-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.item-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background: #f8f9fa;
}

.item-info h4 {
  font-size: 1rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 0.5rem;
}

.item-specs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.25rem;
}

.item-specs span {
  background: #e9ecef;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
  color: #666;
}

.item-quantity {
  font-size: 0.9rem;
  color: #666;
}

.item-price {
  font-weight: 700;
  color: var(--phoenix-accent);
  font-size: 1.1rem;
}

.order-summary {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 1.5rem;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.75rem;
  color: #666;
}

.summary-row.total {
  font-size: 1.2rem;
  font-weight: 700;
  color: #333;
  border-top: 2px solid #e0e0e0;
  padding-top: 0.75rem;
  margin-top: 0.75rem;
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
  color: var(--phoenix-primary);
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
  background: var(--phoenix-primary);
  color: white;
}

.btn-retry:hover {
  background: var(--phoenix-accent);
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
@media (max-width: 768px) {
  .container {
    padding: 0 1rem;
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
}
</style>
