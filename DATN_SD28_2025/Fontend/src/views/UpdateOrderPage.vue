<template>
  <div class="update-order-page">
    <div class="main-content">
      <!-- Header -->
      <div class="page-header">
        <h1>Cập nhật đơn hàng</h1>
        <button @click="goBack" class="btn-back">
          <font-awesome-icon icon="arrow-left" />
          Quay lại
        </button>
      </div>

      <!-- Search Order -->
      <div class="search-section">
        <div class="search-box">
          <font-awesome-icon icon="search" class="search-icon" />
          <input
            v-model="searchCode"
            type="text"
            placeholder="Nhập mã đơn hàng để tìm kiếm..."
            class="search-input"
            @keyup.enter="searchOrder"
          />
          <button @click="searchOrder" class="btn-search">Tìm kiếm</button>
        </div>
      </div>

      <!-- Order Details -->
      <div v-if="orderDetails" class="order-details">
        <!-- Order Information -->
        <div class="info-section">
          <h3>Thông tin đơn hàng</h3>
          <div class="info-grid">
            <div class="info-item">
              <label>Mã đơn hàng:</label>
              <span>{{ orderDetails.maHoaDon }}</span>
            </div>
            <div class="info-item">
              <label>Loại đơn:</label>
              <select v-model="orderDetails.loaiHoaDon" class="form-select">
                <option value="NORMAL">Bán tại quầy</option>
                <option value="DELIVERY">Bán online</option>
              </select>
            </div>
            <div class="info-item">
              <label>Trạng thái:</label>
              <select v-model="orderDetails.trangThai" class="form-select">
                <option value="0">Chờ xác nhận</option>
                <option value="1">Đã thanh toán chờ xác nhận</option>
                <option value="2">Chờ giao hàng</option>
                <option value="3">Đang giao</option>
                <option value="4">Hoàn thành</option>
                <option value="5">Đã hủy</option>
              </select>
            </div>
            <div class="info-item">
              <label>Ngày tạo:</label>
              <span>{{ formatDate(orderDetails.ngayTao) }}</span>
            </div>
          </div>
        </div>

        <!-- Customer Information -->
        <div class="info-section">
          <h3>Thông tin khách hàng</h3>
          <div class="info-grid">
            <div class="info-item">
              <label>Tên khách hàng:</label>
              <input v-model="orderDetails.tenKhachHang" type="text" class="form-input" />
            </div>
            <div class="info-item">
              <label>Số điện thoại:</label>
              <input v-model="orderDetails.soDienThoai" type="text" class="form-input" />
            </div>
            <div class="info-item">
              <label>Địa chỉ:</label>
              <input v-model="orderDetails.diaChi" type="text" class="form-input" />
            </div>
            <div class="info-item">
              <label>Ghi chú:</label>
              <textarea v-model="orderDetails.ghiChu" class="form-textarea"></textarea>
            </div>
          </div>
        </div>

        <!-- Products List -->
        <div class="info-section">
          <h3>Danh sách sản phẩm</h3>
          <div v-if="products.length > 0" class="products-table">
            <table>
              <thead>
                <tr>
                  <th>STT</th>
                  <th>Tên sản phẩm</th>
                  <th>Số lượng</th>
                  <th>Đơn giá</th>
                  <th>Thành tiền</th>
                  <th>IMEI</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(product, index) in products" :key="product.id">
                  <td>{{ index + 1 }}</td>
                  <td>{{ product.tenSanPham }}</td>
                  <td>
                    <input 
                      v-model.number="product.soLuong" 
                      type="number" 
                      min="1" 
                      class="quantity-input"
                      @change="updateProductTotal(product)"
                    />
                  </td>
                  <td>{{ formatCurrency(product.donGia) }}</td>
                  <td>{{ formatCurrency(product.thanhTien) }}</td>
                  <td>{{ product.imei }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div v-else class="no-products">
            <font-awesome-icon icon="box-open" />
            <p>Không có sản phẩm</p>
          </div>
        </div>

        <!-- Order Summary -->
        <div class="info-section">
          <h3>Tổng kết đơn hàng</h3>
          <div class="summary-grid">
            <div class="summary-item">
              <label>Tổng tiền hàng:</label>
              <span>{{ formatCurrency(calculateTotal()) }}</span>
            </div>
            <div class="summary-item">
              <label>Giảm giá:</label>
              <input 
                v-model.number="discount" 
                type="number" 
                class="form-input"
                @change="updateOrderTotal"
              />
            </div>
            <div class="summary-item total">
              <label>Thành tiền:</label>
              <span>{{ formatCurrency(calculateFinalTotal()) }}</span>
            </div>
          </div>
        </div>

        <!-- Action Buttons -->
        <div class="action-buttons">
          <button @click="saveOrder" class="btn-save" :disabled="loading">
            <font-awesome-icon icon="save" />
            {{ loading ? 'Đang lưu...' : 'Lưu thay đổi' }}
          </button>
          <button @click="cancelOrder" class="btn-cancel">
            <font-awesome-icon icon="times" />
            Hủy đơn hàng
          </button>
        </div>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="loading">
        <font-awesome-icon icon="spinner" spin />
        <p>Đang tải...</p>
      </div>

      <!-- Error Message -->
      <div v-if="error" class="error-message">
        <font-awesome-icon icon="exclamation-triangle" />
        <p>{{ error }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../services/api'

const router = useRouter()

// Reactive data
const searchCode = ref('')
const orderDetails = ref(null)
const products = ref([])
const discount = ref(0)
const loading = ref(false)
const error = ref('')

// Methods
const searchOrder = async () => {
  if (!searchCode.value.trim()) return
  
  loading.value = true
  error.value = ''
  
  try {
    // Tìm hóa đơn theo mã
    const { data: hoaDonData } = await api.get(`/api/hoa-don/ma-hoa-don/${searchCode.value}`)
    orderDetails.value = hoaDonData
    
    // Lấy danh sách sản phẩm
    const { data: productsData } = await api.get(`/api/hoa-don/${hoaDonData.id}/products`)
    products.value = productsData
    
    // Tính toán giảm giá
    discount.value = hoaDonData.tongTien - hoaDonData.tongTienSauGiam
    
  } catch (err) {
    error.value = 'Không tìm thấy đơn hàng hoặc có lỗi xảy ra'
    console.error('Error searching order:', err)
  } finally {
    loading.value = false
  }
}

const updateProductTotal = (product) => {
  product.thanhTien = product.soLuong * product.donGia
  updateOrderTotal()
}

const calculateTotal = () => {
  return products.value.reduce((total, product) => total + product.thanhTien, 0)
}

const calculateFinalTotal = () => {
  return calculateTotal() - discount.value
}

const updateOrderTotal = () => {
  if (orderDetails.value) {
    orderDetails.value.tongTien = calculateTotal()
    orderDetails.value.tongTienSauGiam = calculateFinalTotal()
  }
}

const saveOrder = async () => {
  if (!orderDetails.value) return
  
  loading.value = true
  error.value = ''
  
  try {
    // Cập nhật thông tin hóa đơn
    const { data } = await api.put(`/api/hoa-don/${orderDetails.value.id}`, orderDetails.value)
    orderDetails.value = data
    
    alert('Cập nhật đơn hàng thành công!')
    
  } catch (err) {
    error.value = 'Lỗi khi cập nhật đơn hàng'
    console.error('Error updating order:', err)
  } finally {
    loading.value = false
  }
}

const cancelOrder = async () => {
  if (!orderDetails.value) return
  
  if (confirm('Bạn có chắc chắn muốn hủy đơn hàng này?')) {
    loading.value = true
    
    try {
      orderDetails.value.trangThai = 5 // Đã hủy
      const { data } = await api.put(`/api/hoa-don/${orderDetails.value.id}`, orderDetails.value)
      orderDetails.value = data
      
      alert('Đơn hàng đã được hủy!')
      
    } catch (err) {
      error.value = 'Lỗi khi hủy đơn hàng'
      console.error('Error canceling order:', err)
    } finally {
      loading.value = false
    }
  }
}

const goBack = () => {
  router.go(-1)
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('vi-VN')
}

const formatCurrency = (amount) => {
  if (!amount) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}

// Load order from URL params if available
onMounted(() => {
  const urlParams = new URLSearchParams(window.location.search)
  const orderCode = urlParams.get('code')
  if (orderCode) {
    searchCode.value = orderCode
    searchOrder()
  }
})
</script>

<style scoped>
.update-order-page {
  min-height: 100vh;
  background: var(--bg-primary, #f8f9fa);
  padding: 20px;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.page-header h1 {
  margin: 0;
  color: #333;
  font-size: 24px;
}

.btn-back {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: #6c757d;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.3s;
}

.btn-back:hover {
  background: #545b62;
}

.search-section {
  margin-bottom: 30px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.search-box {
  display: flex;
  align-items: center;
  gap: 12px;
  max-width: 600px;
}

.search-icon {
  color: #666;
  font-size: 16px;
}

.search-input {
  flex: 1;
  padding: 12px 16px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 16px;
}

.search-input:focus {
  outline: none;
  border-color: #ff6b35;
}

.btn-search {
  padding: 12px 24px;
  background: #ff6b35;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: background 0.3s;
}

.btn-search:hover {
  background: #e55a2b;
}

.order-details {
  display: grid;
  gap: 30px;
}

.info-section {
  background: white;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.info-section h3 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 18px;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 10px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item label {
  font-weight: 500;
  color: #555;
}

.info-item span {
  color: #333;
  font-size: 16px;
}

.form-input, .form-select, .form-textarea {
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.form-input:focus, .form-select:focus, .form-textarea:focus {
  outline: none;
  border-color: #ff6b35;
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.products-table {
  overflow-x: auto;
}

.products-table table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 16px;
}

.products-table th,
.products-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.products-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #333;
}

.quantity-input {
  width: 80px;
  padding: 6px 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  text-align: center;
}

.no-products {
  text-align: center;
  padding: 40px;
  color: #666;
}

.no-products svg {
  font-size: 48px;
  margin-bottom: 16px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-top: 16px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
}

.summary-item.total {
  background: #e3f2fd;
  font-weight: 600;
  font-size: 16px;
}

.action-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-top: 30px;
}

.btn-save, .btn-cancel {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s;
}

.btn-save {
  background: #28a745;
  color: white;
}

.btn-save:hover:not(:disabled) {
  background: #218838;
}

.btn-save:disabled {
  background: #6c757d;
  cursor: not-allowed;
}

.btn-cancel {
  background: #dc3545;
  color: white;
}

.btn-cancel:hover {
  background: #c82333;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #666;
}

.loading svg {
  font-size: 24px;
  margin-bottom: 16px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.error-message {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
  border-radius: 6px;
  margin: 20px 0;
}
</style>
