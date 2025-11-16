<template>
  <div class="khach-hang-detail">
    <PosHeader />
    
    <div class="detail-container">
      <div class="detail-header">
        <div class="header-left">
          <button class="btn-back" @click="goBack">
            <font-awesome-icon :icon="['fas', 'arrow-left']" />
            Quay lại
          </button>
          <h1>Chi tiết Khách hàng</h1>
        </div>
        <div class="header-actions">
          <button class="btn-edit" @click="editCustomer">
            <font-awesome-icon :icon="['fas', 'edit']" />
            Chỉnh sửa
          </button>
          <button class="btn-delete" @click="deleteCustomer" v-if="customer">
            <font-awesome-icon :icon="['fas', 'trash']" />
            Xóa
          </button>
        </div>
      </div>

      <div class="detail-content" v-if="customer">
        <div class="detail-card">
          <div class="card-header">
            <h2>Thông tin cơ bản</h2>
          </div>
          <div class="card-body">
            <div class="info-grid">
              <div class="info-item">
                <label>Mã khách hàng:</label>
                <span class="info-value">{{ customer.maKhachHang }}</span>
              </div>
              <div class="info-item">
                <label>Họ và tên:</label>
                <span class="info-value">{{ customer.hoTen }}</span>
              </div>
              <div class="info-item">
                <label>Số điện thoại:</label>
                <span class="info-value">{{ customer.soDienThoai }}</span>
              </div>
              <div class="info-item">
                <label>Email:</label>
                <span class="info-value">{{ customer.email || 'Chưa cập nhật' }}</span>
              </div>
              <div class="info-item">
                <label>Giới tính:</label>
                <span class="info-value">{{ customer.gioiTinh || 'Chưa cập nhật' }}</span>
              </div>
              <div class="info-item">
                <label>Ngày sinh:</label>
                <span class="info-value">{{ formatDate(customer.ngaySinh) || 'Chưa cập nhật' }}</span>
              </div>
            </div>
          </div>
        </div>


        <!-- Địa chỉ chi tiết -->
        <div class="detail-card">
          <div class="card-header">
            <h2>Địa chỉ</h2>
            <div class="header-actions">
              <button class="btn-refresh" @click="loadAddresses(customer.id)" :disabled="loadingAddresses">
                <font-awesome-icon :icon="['fas', 'sync-alt']" :class="{ 'spinning': loadingAddresses }" />
                Làm mới
              </button>
            </div>
          </div>
          <div class="card-body">
            
            <div v-if="loadingAddresses" class="loading-addresses">
              <div class="loading-spinner"></div>
              <p>Đang tải địa chỉ...</p>
            </div>
            <div v-else-if="addresses && addresses.length > 0" class="addresses-list">
              <div v-for="(address, index) in sortedAddresses" :key="address.id || index" class="address-item">
                <div class="address-header">
                  <h4>{{ address.loaiDiaChi || 'Địa chỉ' }} {{ index + 1 }}</h4>
                  <span v-if="address.macDinh" class="default-badge">Mặc định</span>
                </div>
                <div class="address-content">
                  <p><strong>Địa chỉ:</strong> {{ address.diaChiChiTiet || address.diaChi || 'Chưa có' }}</p>
                  <p><strong>Phường/Xã:</strong> {{ address.phuongXa || 'Chưa có' }}</p>
                  <p><strong>Tỉnh/Thành phố:</strong> {{ address.tinhThanhPho || 'Chưa có' }}</p>
                  <p v-if="address.maBuuDien"><strong>Mã bưu điện:</strong> {{ address.maBuuDien }}</p>
                  <p v-if="address.ghiChu"><strong>Ghi chú:</strong> {{ address.ghiChu }}</p>
                </div>
              </div>
            </div>
            <div v-else class="no-addresses">
              <div class="no-addresses-icon">
                <font-awesome-icon :icon="['fas', 'map-marker-alt']" />
              </div>
              <h3>Chưa có địa chỉ nào</h3>
              <p>Khách hàng chưa có địa chỉ được lưu trong hệ thống</p>
            </div>
            
          </div>
        </div>

        <!-- Lịch sử mua hàng -->
        <div class="detail-card">
          <div class="card-header">
            <h2>Lịch sử mua hàng</h2>
            <div class="header-actions">
              <button class="btn-refresh" @click="loadOrders" :disabled="loadingOrders">
                <font-awesome-icon :icon="['fas', 'sync-alt']" :class="{ 'spinning': loadingOrders }" />
                Làm mới
              </button>
            </div>
          </div>
          <div class="card-body">
            <div v-if="loadingOrders" class="loading-orders">
              <div class="loading-spinner"></div>
              <p>Đang tải lịch sử mua hàng...</p>
            </div>
            <div v-else-if="orders && orders.length > 0" class="orders-list">
              <div class="orders-summary">
                <div class="summary-item">
                  <span class="summary-label">Tổng đơn hàng:</span>
                  <span class="summary-value">{{ orders.length }}</span>
                </div>
                <div class="summary-item">
                  <span class="summary-label">Tổng chi tiêu:</span>
                  <span class="summary-value">{{ formatCurrency(totalSpent) }}</span>
                </div>
                <div class="summary-item">
                  <span class="summary-label">Đơn hàng gần nhất:</span>
                  <span class="summary-value">{{ formatDate(lastOrderDate) }}</span>
                </div>
                <div class="summary-item">
                  <span class="summary-label">Khách hàng ID:</span>
                  <span class="summary-value">{{ customer.id }}</span>
                </div>
              </div>
              
              <div class="orders-table-container">
                <table class="orders-table">
                  <thead>
                    <tr>
                      <th>Mã hóa đơn</th>
                      <th>Ngày mua</th>
                      <th>Tổng tiền</th>
                      <th>Trạng thái</th>
                      <th>Thao tác</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="order in orders" :key="order.id" class="order-row">
                      <td>{{ order.maHoaDon }}</td>
                      <td>{{ formatDate(order.ngayTao) }}</td>
                      <td>{{ formatCurrency(order.tongTien) }}</td>
                      <td>
                        <span class="status-badge" :class="getOrderStatusClass(order.trangThai)">
                          {{ getOrderStatusText(order.trangThai) }}
                        </span>
                      </td>
                      <td>
                        <button class="btn-view-detail" @click="viewOrderDetail(order.id)">
                          <font-awesome-icon :icon="['fas', 'eye']" />
                          Xem chi tiết
                        </button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
            <div v-else class="no-orders">
              <div class="no-orders-icon">
                <font-awesome-icon :icon="['fas', 'shopping-cart']" />
              </div>
              <h3>Chưa có đơn hàng nào</h3>
              <p>Khách hàng chưa thực hiện giao dịch nào</p>
            </div>
          </div>
        </div>

        <div class="detail-card">
          <div class="card-header">
            <h2>Thông tin hệ thống</h2>
          </div>
          <div class="card-body">
            <div class="info-grid">
              <div class="info-item">
                <label>Trạng thái:</label>
                <span class="status-badge" :class="customer.trangThai ? 'active' : 'inactive'">
                  {{ customer.trangThai ? 'Hoạt động' : 'Không hoạt động' }}
                </span>
              </div>
              <div class="info-item">
                <label>Ngày tạo:</label>
                <span class="info-value">{{ formatDateTime(customer.ngayTao) }}</span>
              </div>
              <div class="info-item">
                <label>Ngày cập nhật:</label>
                <span class="info-value">{{ formatDateTime(customer.ngayCapNhat) }}</span>
              </div>
              <div class="info-item">
                <label>Người tạo:</label>
                <span class="info-value">{{ customer.nguoiTao || 'Hệ thống' }}</span>
              </div>
              <div class="info-item">
                <label>Người cập nhật:</label>
                <span class="info-value">{{ customer.nguoiCapNhat || 'Hệ thống' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="loading-container" v-else-if="loading">
        <div class="loading-spinner"></div>
        <p>Đang tải thông tin khách hàng...</p>
      </div>

      <div class="error-container" v-else-if="error">
        <div class="error-icon">
          <font-awesome-icon :icon="['fas', 'exclamation-triangle']" />
        </div>
        <h3>Không thể tải thông tin khách hàng</h3>
        <p>{{ error }}</p>
        <button class="btn-retry" @click="loadCustomer">
          <font-awesome-icon :icon="['fas', 'redo']" />
          Thử lại
        </button>
      </div>
    </div>

    <Toast ref="toastRef" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '@/services/api'
import Toast from '@/components/Toast.vue'
import PosHeader from '@/components/PosHeader.vue'

const router = useRouter()
const route = useRoute()
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

const customer = ref<any>(null)
const loading = ref(false)
const error = ref('')

// Địa chỉ và đơn hàng
const addresses = ref<any[]>([])
const orders = ref<any[]>([])
const loadingOrders = ref(false)
const loadingAddresses = ref(false)


interface KhachHang {
  id: number
  maKhachHang: string
  hoTen: string
  soDienThoai: string
  email?: string
  gioiTinh?: string
  ngaySinh?: string
  diaChi?: string
  trangThai: boolean
  ngayTao: string
  ngayCapNhat: string
  nguoiTao?: string
  nguoiCapNhat?: string
}

// Computed properties
const totalSpent = computed(() => {
  if (!orders.value || !Array.isArray(orders.value)) return 0
  return orders.value.reduce((total, order) => total + (order.tongTien || 0), 0)
})

const lastOrderDate = computed(() => {
  if (!orders.value || !Array.isArray(orders.value) || orders.value.length === 0) return null
  const sortedOrders = [...orders.value].sort((a, b) => new Date(b.ngayTao).getTime() - new Date(a.ngayTao).getTime())
  return sortedOrders[0].ngayTao
})

const sortedAddresses = computed(() => {
  if (!addresses.value || addresses.value.length === 0) return []
  
  // Sort addresses: default first, then by creation date
  return [...addresses.value].sort((a, b) => {
    // Default addresses first
    if (a.macDinh && !b.macDinh) return -1
    if (!a.macDinh && b.macDinh) return 1
    
    // Then sort by creation date (newest first)
    const dateA = new Date(a.ngayTao || 0).getTime()
    const dateB = new Date(b.ngayTao || 0).getTime()
    return dateB - dateA
  })
})

async function loadCustomer() {
  const customerId = route.params.id
  if (!customerId) {
    error.value = 'Không tìm thấy ID khách hàng'
    return
  }

  try {
    loading.value = true
    error.value = ''
    
    const response = await api.get(`/api/khach-hang/${customerId}`)
    customer.value = response.data
    
    // Load addresses and orders (don't wait for them to fail the main load)
    Promise.all([
      loadAddresses(customerId as string),
      loadOrders()
    ]).catch(err => {
      // Silent fail for sub-loads
    })
  } catch (err: any) {
    if (err.response?.status === 404) {
      error.value = 'Không tìm thấy khách hàng với ID này'
    } else if (err.response?.status === 500) {
      error.value = 'Lỗi server, vui lòng thử lại sau'
    } else {
      error.value = err.response?.data?.message || 'Có lỗi xảy ra khi tải thông tin khách hàng'
    }
  } finally {
    loading.value = false
  }
}

// Load addresses
async function loadAddresses(customerId: string) {
  try {
    loadingAddresses.value = true
    
    // Try multiple possible endpoints for addresses
    let response
    let endpointUsed = ''
    
    const endpoints = [
      `/api/user-dia-chi/khach-hang/${customerId}`,  // Correct endpoint for customer addresses
      `/api/khach-hang/${customerId}/addresses`,
      `/api/user-dia-chi?userId=${customerId}`,
      `/api/user-dia-chi?idUser=${customerId}`,
      `/api/khach-hang/${customerId}/dia-chi`,
      `/api/dia-chi?userId=${customerId}`,
      `/api/dia-chi?idUser=${customerId}`,
      `/api/user-dia-chi?khachHangId=${customerId}`,
      `/api/user-dia-chi?customerId=${customerId}`
    ]
    
    for (const endpoint of endpoints) {
      try {
        response = await api.get(endpoint)
        endpointUsed = endpoint
        break
      } catch (err) {
        continue
      }
    }
    
    if (!response) {
      addresses.value = []
      return
    }
    
    const addressesData = response.data || []
    
    // Process addresses data - handle different response structures
    let rawAddresses = []
    if (Array.isArray(addressesData)) {
      rawAddresses = addressesData
    } else if (addressesData.data && Array.isArray(addressesData.data)) {
      rawAddresses = addressesData.data
    } else if (addressesData.content && Array.isArray(addressesData.content)) {
      rawAddresses = addressesData.content
    } else {
      rawAddresses = []
    }
    
    // Process UserDiaChi data structure
    if (endpointUsed.includes('/api/user-dia-chi/khach-hang/')) {
      addresses.value = rawAddresses.map((userDiaChi: any) => {
        // Extract address data from UserDiaChi structure
        const diaChi = userDiaChi.diaChi || {}
        return {
          id: userDiaChi.id,
          diaChiChiTiet: diaChi.diaChiChiTiet || '',
          phuongXa: diaChi.phuongXa || '',
          quanHuyen: diaChi.quanHuyen || '',
          tinhThanhPho: diaChi.tinhThanhPho || '',
          maBuuDien: diaChi.maBuuDien || '',
          ghiChu: diaChi.ghiChu || '',
          loaiDiaChi: userDiaChi.loaiDiaChi || '',
          macDinh: userDiaChi.macDinh || false,
          ngayTao: userDiaChi.ngayTao || '',
          ngayCapNhat: userDiaChi.ngayCapNhat || '',
          trangThai: userDiaChi.trangThai || 1
        }
      })
    } else {
      // For other endpoints, use the old filtering logic
      addresses.value = rawAddresses.filter(address => {
        const isCustomerAddress = address.userId === parseInt(customerId) || 
                                 address.idUser === parseInt(customerId) ||
                                 address.khachHangId === parseInt(customerId) ||
                                 address.customerId === parseInt(customerId) ||
                                 address.userId == customerId ||  // Loose comparison
                                 address.idUser == customerId ||
                                 address.khachHangId == customerId ||
                                 address.customerId == customerId
        
        return isCustomerAddress
      })
      
      // If no addresses after filter, try showing all addresses as fallback
      if (addresses.value.length === 0 && rawAddresses.length > 0) {
        addresses.value = rawAddresses
      }
    }
    
        // If still no addresses, try to load from dia_chi table directly
        if (addresses.value.length === 0) {
          try {
            const diaChiResponse = await api.get(`/api/dia-chi`)
            
            if (diaChiResponse.data && Array.isArray(diaChiResponse.data)) {
              // Filter by customer ID even from dia_chi table
              const filteredAddresses = diaChiResponse.data.filter(address => {
                // Check if this address belongs to the current customer
                // We need to find the relationship between address and customer
                // This might require checking a junction table or foreign key
                return address.khachHangId === parseInt(customerId) || 
                       address.customerId === parseInt(customerId) ||
                       address.userId === parseInt(customerId) ||
                       address.idUser === parseInt(customerId)
              })
              
              if (filteredAddresses.length > 0) {
                addresses.value = filteredAddresses
              } else {
                addresses.value = []
              }
            }
          } catch (err) {
            // Silent fail
          }
        }
    
  } catch (err: any) {
    addresses.value = []
  } finally {
    loadingAddresses.value = false
  }
}

// Load orders
async function loadOrders() {
  if (!customer.value?.id) {
    console.log('No customer ID, skipping orders load')
    orders.value = []
    return
  }
  
  try {
    loadingOrders.value = true
    console.log('Loading orders for customer ID:', customer.value.id)
    
    // Try different possible endpoints for orders
    let response
    try {
      response = await api.get(`/api/hoa-don?khachHangId=${customer.value.id}`)
      console.log('Orders loaded with khachHangId parameter:', response.data)
    } catch (err) {
      console.log('Failed with khachHangId, trying customerId parameter')
      // Fallback to different parameter name
      response = await api.get(`/api/hoa-don?customerId=${customer.value.id}`)
      console.log('Orders loaded with customerId parameter:', response.data)
    }
    
    const ordersData = response.data || []
    console.log('Total orders found:', ordersData.length)
    
    // Filter to ensure we only get orders for this specific customer
    orders.value = ordersData.filter(order => {
      const isCustomerOrder = order.khachHangId === customer.value.id || 
                             order.customerId === customer.value.id ||
                             order.idKhachHang === customer.value.id
      if (!isCustomerOrder) {
        console.warn('Found order not belonging to customer:', order)
      }
      return isCustomerOrder
    })
    
    console.log('Filtered orders count:', orders.value.length)
  } catch (err: any) {
    console.error('Lỗi khi tải đơn hàng:', err)
    // Set empty array instead of leaving undefined
    orders.value = []
  } finally {
    loadingOrders.value = false
  }
}

// View order detail
function viewOrderDetail(orderId: number) {
  router.push(`/hoa-don/detail/${orderId}`)
}

// Order status helpers
function getOrderStatusClass(status: number | string | null | undefined) {
  if (status === null || status === undefined) {
    return 'unknown'
  }
  
  const numericStatus = typeof status === 'string' ? parseInt(status) : status
  
  switch (numericStatus) {
    case 0: return 'pending'
    case 1: return 'paid-pending'
    case 2: return 'waiting-delivery'
    case 3: return 'delivering'
    case 4: return 'completed'
    case 5: return 'cancelled'
    default: return 'unknown'
  }
}

function getOrderStatusText(status: number | string | null | undefined) {
  if (status === null || status === undefined) {
    return 'Không xác định'
  }
  
  const numericStatus = typeof status === 'string' ? parseInt(status) : status
  
  switch (numericStatus) {
    case 0: return 'Chờ xác nhận'
    case 1: return 'Đã thanh toán chờ xác nhận'
    case 2: return 'Chờ giao hàng'
    case 3: return 'Đang giao'
    case 4: return 'Hoàn thành'
    case 5: return 'Đã hủy'
    default: return 'Không xác định'
  }
}

// Utility functions
function formatCurrency(amount: number | string | null | undefined) {
  if (!amount && amount !== 0) return '0 ₫'
  const numAmount = typeof amount === 'string' ? parseFloat(amount) : amount
  if (isNaN(numAmount)) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(numAmount)
}

function formatDate(dateString: string | null | undefined) {
  if (!dateString) return 'Chưa cập nhật'
  try {
    const date = new Date(dateString)
    if (isNaN(date.getTime())) return 'Chưa cập nhật'
    return date.toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    })
  } catch (err) {
    return 'Chưa cập nhật'
  }
}

function formatDateTime(dateString: string | null | undefined) {
  if (!dateString) return 'Chưa cập nhật'
  try {
    const date = new Date(dateString)
    if (isNaN(date.getTime())) return 'Chưa cập nhật'
    return date.toLocaleString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (err) {
    return 'Chưa cập nhật'
  }
}

function goBack() {
  router.go(-1)
}

function editCustomer() {
  if (customer.value) {
    router.push(`/khach-hang/edit/${customer.value.id}`)
  }
}

async function deleteCustomer() {
  if (!customer.value) return

  if (!confirm('Bạn có chắc chắn muốn xóa khách hàng này?')) {
    return
  }

  try {
    await api.delete(`/api/khach-hang/${customer.value.id}`)
    toastRef.value?.success('Thành công', 'Đã xóa khách hàng thành công!')
    router.push('/khach-hang')
  } catch (err: any) {
    console.error('Lỗi khi xóa khách hàng:', err)
    toastRef.value?.error('Lỗi', err.response?.data?.message || 'Có lỗi xảy ra khi xóa khách hàng')
  }
}


onMounted(() => {
  loadCustomer()
})
</script>

<style scoped>
.khach-hang-detail {
  background: #f8fafc;
  min-height: 100vh;
  padding: 0;
  padding-top: 80px;
  width: 100%;
  overflow-x: hidden;
  position: relative;
}

.detail-container {
  padding: 24px;
  padding-top: 0;
  width: 100%;
  margin: 0;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  width: 100%;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.btn-back {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #f1f5f9;
  color: #475569;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-back:hover {
  background: #e2e8f0;
  color: #334155;
}

.detail-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.btn-edit,
.btn-delete {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.2s;
}

.btn-edit {
  background: #3b82f6;
  color: white;
}

.btn-edit:hover {
  background: #2563eb;
  transform: translateY(-1px);
}

.btn-delete {
  background: #ef4444;
  color: white;
}

.btn-delete:hover {
  background: #dc2626;
  transform: translateY(-1px);
}

.detail-content {
  display: grid;
  gap: 24px;
  width: 100%;
}

.detail-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.card-header {
  padding: 20px 24px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
}

.card-body {
  padding: 24px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 24px;
  width: 100%;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-item label {
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-value {
  font-size: 14px;
  font-weight: 500;
  color: #1e293b;
  padding: 8px 12px;
  background: #f8fafc;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-badge.active {
  background: #dcfce7;
  color: #166534;
}

.status-badge.inactive {
  background: #fef2f2;
  color: #dc2626;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e2e8f0;
  border-top: 4px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-container p {
  color: #64748b;
  font-size: 14px;
}

.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.error-icon {
  font-size: 48px;
  color: #ef4444;
  margin-bottom: 16px;
}

.error-container h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
}

.error-container p {
  margin: 0 0 24px 0;
  color: #64748b;
  font-size: 14px;
}

.btn-retry {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.2s;
}

.btn-retry:hover {
  background: #2563eb;
  transform: translateY(-1px);
}

/* Responsive */
@media (max-width: 768px) {
  .khach-hang-detail {
    padding-top: 60px;
  }
  
  .detail-container {
    padding: 16px;
  }
  
  .detail-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: center;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .btn-edit,
  .btn-delete {
    flex: 1;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .khach-hang-detail {
    padding-top: 50px;
  }
  
  .detail-container {
    padding: 12px;
  }
  
  .detail-header {
    padding: 16px;
  }
  
  .card-body {
    padding: 16px;
  }
}

/* Address styles */
.addresses-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.address-item {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.2s ease;
}

.address-item:hover {
  border-color: #3b82f6;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.1);
}

.address-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e2e8f0;
}

.address-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.default-badge {
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.address-content p {
  margin: 8px 0;
  font-size: 14px;
  color: #374151;
  line-height: 1.5;
}

.no-addresses {
  text-align: center;
  padding: 40px 20px;
  color: #6b7280;
  background: #f9fafb;
  border-radius: 8px;
  border: 2px dashed #d1d5db;
}

/* Orders styles */
.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.btn-refresh {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #f1f5f9;
  color: #475569;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.btn-refresh:hover:not(:disabled) {
  background: #e2e8f0;
  color: #334155;
}

.btn-refresh:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.loading-orders,
.loading-addresses {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 20px;
  color: #6b7280;
}

.orders-summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.summary-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.summary-label {
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.summary-value {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
}

.orders-table-container {
  overflow-x: auto;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.orders-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
}

.orders-table th {
  background: #f8fafc;
  padding: 16px;
  text-align: left;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  border-bottom: 1px solid #e2e8f0;
}

.orders-table td {
  padding: 16px;
  border-bottom: 1px solid #f1f5f9;
  font-size: 14px;
  color: #1e293b;
}

.order-row:hover {
  background: #f8fafc;
}

.btn-view-detail {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.btn-view-detail:hover {
  background: #2563eb;
  transform: translateY(-1px);
}

.status-badge.pending {
  background: #fef3c7;
  color: #92400e;
}

.status-badge.paid-pending {
  background: #e0e7ff;
  color: #3730a3;
}

.status-badge.waiting-delivery {
  background: #dbeafe;
  color: #1e40af;
}

.status-badge.delivering {
  background: #fed7aa;
  color: #ea580c;
}

.status-badge.completed {
  background: #dcfce7;
  color: #166534;
}

.status-badge.cancelled {
  background: #fef2f2;
  color: #dc2626;
}

.status-badge.unknown {
  background: #f3f4f6;
  color: #6b7280;
}

.no-orders {
  text-align: center;
  padding: 60px 20px;
  color: #6b7280;
}

.no-orders-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.no-orders h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #374151;
}

.no-orders p {
  margin: 0;
  font-size: 14px;
  color: #6b7280;
}

/* Responsive */
@media (max-width: 768px) {
  .orders-summary {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .orders-table-container {
    font-size: 12px;
  }
  
  .orders-table th,
  .orders-table td {
    padding: 12px 8px;
  }
  
  .address-item {
    padding: 16px;
  }
  
  .address-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
