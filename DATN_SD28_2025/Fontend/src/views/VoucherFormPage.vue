<template>
  <div class="form-page">
    <PosHeader />
    <div class="form-container">
      <div class="form-header">
        <div class="header-left">
          <button class="btn-back" @click="goBack">
            <i class="icon-arrow-left"></i>
            Quay lại
          </button>
          <h1>{{ isEdit ? 'Sửa Phiếu Giảm Giá' : 'Thêm Phiếu Giảm Giá' }}</h1>
        </div>
      </div>
      <form @submit.prevent="handleSubmit" class="voucher-form">
        <div class="form-section">
          <h2>Thông Tin Cơ Bản</h2>
          <div class="form-row">
            <div class="form-group">
              <label for="maPhieuGiamGia">Mã Phiếu Giảm Giá:</label>
              <input
                id="maPhieuGiamGia"
                type="text"
                v-model="formData.maPhieuGiamGia"
                placeholder="Để trống để tự động tạo"
              />
            </div>
            <div class="form-group">
              <label for="tenPhieuGiamGia">Tên Phiếu Giảm Giá: <span class="required">*</span></label>
              <input
                id="tenPhieuGiamGia"
                type="text"
                v-model="formData.tenPhieuGiamGia"
                :class="{ 'error': errors.tenPhieuGiamGia }"
                required
                placeholder="Nhập tên phiếu giảm giá"
              />
              <span v-if="errors.tenPhieuGiamGia" class="error-message">{{ errors.tenPhieuGiamGia }}</span>
            </div>
          </div>

          <div class="form-group">
            <label for="moTa">Mô Tả:</label>
            <textarea
              id="moTa"
              v-model="formData.moTa"
              rows="3"
              placeholder="Mô tả chi tiết về phiếu giảm giá"
            ></textarea>
          </div>
        </div>

        <div class="form-section">
          <h2>Cài Đặt Giảm Giá</h2>
          <div class="form-row">
            <div class="form-group">
              <label for="loaiPhieuGiamGia">Loại Phiếu: <span class="required">*</span></label>
              <select id="loaiPhieuGiamGia" v-model="formData.loaiPhieuGiamGia" :class="{ 'error': errors.loaiPhieuGiamGia }" required>
                <option value="">-- Chọn loại --</option>
                <option value="PERCENT">Phần trăm (%)</option>
                <option value="FIXED">Số tiền cố định (VND)</option>
              </select>
              <span v-if="errors.loaiPhieuGiamGia" class="error-message">{{ errors.loaiPhieuGiamGia }}</span>
            </div>
            <div class="form-group">
              <label for="giaTriGiamGia">
                {{ formData.loaiPhieuGiamGia === 'PERCENT' ? 'Phần trăm giảm (%)' : 'Số tiền giảm (VND)' }}: 
                <span class="required">*</span>
                <span v-if="formData.loaiPhieuGiamGia === 'PERCENT'" class="help-text">
                  (Nhập số phần trăm, ví dụ: 10 = 10%)
                </span>
              </label>
              <input
                id="giaTriGiamGia"
                type="number"
                v-model="formData.giaTriGiamGia"
                :class="{ 'error': errors.giaTriGiamGia }"
                required
                :placeholder="formData.loaiPhieuGiamGia === 'PERCENT' ? 'Ví dụ: 15' : 'Ví dụ: 50000'"
                :max="formData.loaiPhieuGiamGia === 'PERCENT' ? 100 : undefined"
                min="0"
                step="0.01"
              />
              <span v-if="errors.giaTriGiamGia" class="error-message">{{ errors.giaTriGiamGia }}</span>
              <div v-if="formData.giaTriGiamGia && formData.loaiPhieuGiamGia === 'PERCENT'" class="current-value-display">
                Giá trị hiện tại: {{ formData.giaTriGiamGia }}% ({{ formData.giaTriGiamGia }} phần trăm)
              </div>
              <div v-if="formData.giaTriGiamGia && formData.loaiPhieuGiamGia === 'FIXED'" class="current-value-display">
                Giá trị hiện tại: {{ formatCurrency(formData.giaTriGiamGia) }}
              </div>
            </div>
          </div>

          <div class="form-row" v-if="formData.loaiPhieuGiamGia === 'PERCENT'">
            <div class="form-group">
              <label for="soTienGiamToiDa">Số Tiền Giảm Tối Đa (VND):</label>
              <input
                id="soTienGiamToiDa"
                type="number"
                v-model="formData.soTienGiamToiDa"
                :class="{ 'error': errors.soTienGiamToiDa }"
                placeholder="Ví dụ: 100000"
                min="0"
              />
              <span v-if="errors.soTienGiamToiDa" class="error-message">{{ errors.soTienGiamToiDa }}</span>
            </div>
            <div class="form-group">
              <label for="hoaDonToiThieu">Hóa Đơn Tối Thiểu (VND):</label>
              <input
                id="hoaDonToiThieu"
                type="number"
                v-model="formData.hoaDonToiThieu"
                :class="{ 'error': errors.hoaDonToiThieu }"
                placeholder="Ví dụ: 200000"
                min="0"
              />
              <span v-if="errors.hoaDonToiThieu" class="error-message">{{ errors.hoaDonToiThieu }}</span>
            </div>
          </div>

          <div class="form-row" v-if="formData.loaiPhieuGiamGia === 'FIXED'">
            <div class="form-group">
              <label for="hoaDonToiThieuFixed">Hóa Đơn Tối Thiểu (VND):</label>
              <input
                id="hoaDonToiThieuFixed"
                type="number"
                v-model="formData.hoaDonToiThieu"
                :class="{ 'error': errors.hoaDonToiThieu }"
                placeholder="Ví dụ: 200000"
                min="0"
              />
              <span v-if="errors.hoaDonToiThieu" class="error-message">{{ errors.hoaDonToiThieu }}</span>
            </div>
            <div class="form-group">
              <label for="soLuongDung">Số Lượng Sử Dụng: <span class="required">*</span></label>
              <input
                id="soLuongDung"
                type="number"
                v-model="formData.soLuongDung"
                :class="{ 'error': errors.soLuongDung }"
                required
                placeholder="Ví dụ: 100"
                min="1"
              />
              <span v-if="errors.soLuongDung" class="error-message">{{ errors.soLuongDung }}</span>
            </div>
          </div>

          <div class="form-row" v-if="formData.loaiPhieuGiamGia === 'PERCENT'">
            <div class="form-group">
              <label for="soLuongDungPercent">Số Lượng Sử Dụng: <span class="required">*</span></label>
              <input
                id="soLuongDungPercent"
                type="number"
                v-model="formData.soLuongDung"
                :class="{ 'error': errors.soLuongDung }"
                required
                placeholder="Ví dụ: 100"
                min="1"
              />
              <span v-if="errors.soLuongDung" class="error-message">{{ errors.soLuongDung }}</span>
            </div>
          </div>
        </div>

        <div class="form-section">
          <h2>Thời Gian Áp Dụng</h2>
          <div class="form-row">
            <div class="form-group">
              <label for="ngayBatDau">Ngày Bắt Đầu: <span class="required">*</span></label>
              <input
                id="ngayBatDau"
                type="date"
                v-model="formData.ngayBatDau"
                :class="{ 'error': errors.ngayBatDau }"
                required
              />
              <span v-if="errors.ngayBatDau" class="error-message">{{ errors.ngayBatDau }}</span>
            </div>
            <div class="form-group">
              <label for="ngayKetThuc">Ngày Kết Thúc: <span class="required">*</span></label>
              <input
                id="ngayKetThuc"
                type="date"
                v-model="formData.ngayKetThuc"
                :class="{ 'error': errors.ngayKetThuc }"
                required
              />
              <span v-if="errors.ngayKetThuc" class="error-message">{{ errors.ngayKetThuc }}</span>
            </div>
          </div>
        </div>

        <div class="form-section">
          <h2>Cài Đặt Khác</h2>
          <div class="form-row">
            <div class="form-group">
              <label class="checkbox-label">
                <input type="checkbox" v-model="formData.riengTu" @change="handlePrivateVoucherChange" />
                <span class="checkmark"></span>
                Voucher riêng tư (chỉ dành cho khách hàng cụ thể)
              </label>
            </div>
            <div class="form-group">
              <label class="checkbox-label">
                <input type="checkbox" v-model="formData.trangThai" />
                <span class="checkmark"></span>
                Kích hoạt ngay sau khi tạo
              </label>
            </div>
          </div>

          <!-- Customer Selection for Private Vouchers -->
          <div v-if="formData.riengTu" class="customer-selection-section">
            <h3>Chọn Khách Hàng Cho Voucher Riêng Tư</h3>
            <div class="customer-selection-info">
              <p><strong>Lưu ý:</strong> Mỗi khách hàng được chọn sẽ có 1 phiếu riêng. Khi khách hàng sử dụng, phiếu của họ sẽ biến mất nhưng khách hàng khác vẫn còn phiếu.</p>
              <p><strong>Số phiếu sẽ tạo:</strong> {{ selectedCustomers.length }} phiếu (1 phiếu/khách hàng)</p>
              <p>Đã chọn: {{ selectedCustomers.length }} khách hàng</p>
            </div>


            <!-- Customer Table -->
            <div class="customer-table-section">
              <div class="table-header">
                <div class="table-actions">
                  <!-- Refresh Button -->
                  <button type="button" class="btn-refresh" @click="refreshCustomers" :disabled="loading">
                    <i class="icon-refresh"></i>
                    Làm mới
                  </button>
                  
                  <!-- Horizontal Filters -->
                  <div class="horizontal-filters">
                    <div class="filter-group">
                      <input 
                        type="text" 
                        v-model="searchFilters.name" 
                        placeholder="Tìm theo tên..."
                        class="filter-input"
                        @input="applyFilters"
                      />
                    </div>
                    <div class="filter-group">
                      <input 
                        type="text" 
                        v-model="searchFilters.phone" 
                        placeholder="Tìm theo SĐT..."
                        class="filter-input"
                        @input="applyFilters"
                      />
                    </div>
                    <div class="filter-group">
                      <select v-model="searchFilters.status" @change="applyFilters" class="filter-select">
                        <option value="">Tất cả trạng thái</option>
                        <option value="VIP">VIP</option>
                        <option value="Tích cực">Tích cực</option>
                        <option value="Thường xuyên">Thường xuyên</option>
                        <option value="Mới">Mới</option>
                      </select>
                    </div>
                    <button type="button" class="btn-clear-filters" @click="clearFilters">
                      <i class="icon-clear"></i>
                      Xóa lọc
                    </button>
                  </div>
                  
                  <div class="pagination-size-selector">
                    <label for="itemsPerPage">Hiển thị:</label>
                    <select id="itemsPerPage" v-model="itemsPerPage" @change="resetToFirstPage" class="size-select">
                      <option value="5">5</option>
                      <option value="10">10</option>
                      <option value="15">15</option>
                      <option value="20">20</option>
                    </select>
                  </div>
                </div>
              </div>
              
              <div class="customer-table-container">
                <table class="customer-table">
                  <thead>
                    <tr>
                      <th>
                        <input 
                          type="checkbox" 
                          @change="toggleSelectAllCustomers"
                          :checked="isAllCustomersSelected"
                        />
                      </th>
                      <th>Tên</th>
                      <th>SĐT</th>
                      <th>Email</th>
                      <th>Ngày sinh</th>
                      <th>Tổng chi tiêu</th>
                      <th>Số đơn hàng</th>
                      <th>Đơn hàng gần nhất</th>
                      <th>Trạng thái</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="customer in paginatedCustomers" :key="customer.id" class="customer-row">
                      <td>
                        <input 
                          type="checkbox" 
                          :value="customer.id"
                          v-model="selectedCustomerIds"
                          @change="updateSelectedCustomers"
                        />
                      </td>
                      <td>{{ customer.hoTen }}</td>
                      <td>{{ customer.soDienThoai }}</td>
                      <td>{{ customer.email || 'N/A' }}</td>
                      <td>{{ formatDate(customer.ngaySinh) }}</td>
                      <td>{{ formatCurrency(customer.tongChiTieu || 0) }}</td>
                      <td>{{ customer.soDonHang || 0 }}</td>
                      <td>{{ formatDate(customer.donHangGanNhat) }}</td>
                      <td>
                        <span class="status-badge" :class="getCustomerStatusClass(customer)">
                          {{ getCustomerStatusText(customer) }}
                        </span>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
              
              <!-- Pagination -->
              <div class="pagination-container" v-if="totalPages > 1">
                <div class="pagination-info">
                  Hiển thị {{ (currentPage - 1) * itemsPerPage + 1 }} - {{ Math.min(currentPage * itemsPerPage, customers.length) }} 
                  trong {{ customers.length }} khách hàng
                </div>
                <div class="pagination-controls">
                  <button 
                    class="pagination-btn" 
                    :disabled="currentPage === 1"
                    @click="goToPage(currentPage - 1)"
                  >
                    ‹
                  </button>
                  
                  <div class="pagination-numbers">
                    <button 
                      v-for="page in visiblePages" 
                      :key="page"
                      class="pagination-btn"
                      :class="{ 'active': page === currentPage }"
                      @click="goToPage(page)"
                    >
                      {{ page }}
                    </button>
                  </div>
                  
                  <button 
                    class="pagination-btn" 
                    :disabled="currentPage === totalPages"
                    @click="goToPage(currentPage + 1)"
                  >
                    ›
                  </button>
                </div>
              </div>
            </div>
            
            <button type="button" @click="openCustomerModal" class="btn-select-customer">
              Chọn khách hàng từ bảng trên
            </button>
            
            <div v-if="selectedCustomers.length > 0" class="selected-customers">
              <div v-for="customer in selectedCustomers" :key="customer.id" class="customer-item">
                <div class="customer-info">
                  <span class="customer-name">{{ customer.hoTen }}</span>
                  <span class="customer-phone">{{ customer.soDienThoai }}</span>
                </div>
                <button type="button" @click="removeCustomer(customer.id)" class="btn-remove-customer">
                  ×
                </button>
              </div>
            </div>
            
            <span v-if="errors.selectedCustomers" class="error-message">{{ errors.selectedCustomers }}</span>
          </div>
        </div>

        <div class="form-actions">
          <button type="button" @click="goBack" class="btn-cancel">Hủy</button>
          <button type="submit" class="btn-submit" :disabled="loading">
            <span v-if="loading">Đang lưu...</span>
            <span v-else>{{ isEdit ? 'Cập nhật' : 'Tạo mới' }}</span>
          </button>
        </div>
      </form>
    </div>

    <Toast ref="toastRef" />

    <!-- Customer Selection Modal -->
    <div v-if="showCustomerModal" class="modal-overlay" @click="closeCustomerModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>Chọn Khách Hàng</h3>
          <button type="button" @click="closeCustomerModal" class="btn-close-modal">×</button>
        </div>
        
        <div class="modal-body">
          <div class="search-section">
            <input
              type="text"
              v-model="searchText"
              @input="searchCustomers"
              placeholder="Tìm kiếm khách hàng theo tên hoặc số điện thoại..."
              class="search-input"
            />
          </div>
          
          <div v-if="searching" class="loading">
            Đang tìm kiếm...
          </div>
          
          <div v-else-if="customers.length > 0" class="customer-list">
            <div
              v-for="customer in customers"
              :key="customer.id"
              class="customer-option"
              @click="selectCustomer(customer)"
            >
              <div class="customer-info">
                <div class="customer-name">{{ customer.hoTen }}</div>
                <div class="customer-phone">{{ customer.soDienThoai }}</div>
              </div>
              <button type="button" class="btn-select">Chọn</button>
            </div>
          </div>
          
          <div v-else-if="searchText.length >= 2" class="no-results">
            Không tìm thấy khách hàng nào
          </div>
          
          <div v-else class="search-hint">
            Nhập ít nhất 2 ký tự để tìm kiếm
          </div>
        </div>
        
        <div class="modal-footer">
          <button type="button" @click="closeCustomerModal" class="btn-cancel">Đóng</button>
        </div>
      </div>
    </div>

    <!-- Confirm Modal -->
    <ConfirmModal
      :show="showConfirmModal"
      :title="confirmAction === 'add' ? 'Xác nhận tạo mới' : 'Xác nhận cập nhật'"
      :message="confirmAction === 'add' 
        ? 'Bạn có chắc chắn muốn tạo phiếu giảm giá mới với thông tin này?' 
        : 'Bạn có chắc chắn muốn cập nhật thông tin phiếu giảm giá này?'"
      @confirm="confirmSubmit"
      @cancel="cancelSubmit"
    />

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '@/services/api'
import Toast from '@/components/Toast.vue'
import PosHeader from '@/components/PosHeader.vue'
import ConfirmModal from '@/components/ConfirmModal.vue'

const router = useRouter()
const route = useRoute()
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

const isEdit = ref(false)
const voucherId = ref<number | null>(null)

// Confirm modal state
const showConfirmModal = ref(false)
const confirmAction = ref<'add' | 'edit' | null>(null)

const formData = ref({
  maPhieuGiamGia: '',
  tenPhieuGiamGia: '',
  loaiPhieuGiamGia: '',
  giaTriGiamGia: 0,
  soTienGiamToiDa: 0,
  hoaDonToiThieu: 0,
  soLuongDung: 1,
  ngayBatDau: '',
  ngayKetThuc: '',
  riengTu: false,
  moTa: '',
  trangThai: true,
})

// Validation errors
const errors = ref({
  tenPhieuGiamGia: '',
  loaiPhieuGiamGia: '',
  giaTriGiamGia: '',
  soLuongDung: '',
  ngayBatDau: '',
  ngayKetThuc: '',
  soTienGiamToiDa: '',
  hoaDonToiThieu: '',
  selectedCustomers: ''
})

// Customer selection states
const selectedCustomers = ref([])
const customers = ref([])
const filteredCustomers = ref([])
const searchText = ref('')
const loading = ref(false)

// Search filters
const searchFilters = ref({
  name: '',
  phone: '',
  status: ''
})

const selectedCustomerIds = ref([])
const showCustomerModal = ref(false)
const searching = ref(false)

// Pagination variables
const currentPage = ref(1)
const itemsPerPage = ref(10)


// Computed properties
const isAllCustomersSelected = computed(() => {
  return filteredCustomers.value.length > 0 && 
         filteredCustomers.value.every(customer => selectedCustomerIds.value.includes(customer.id))
})

// Pagination computed properties
const totalPages = computed(() => {
  return Math.ceil(filteredCustomers.value.length / itemsPerPage.value)
})

const paginatedCustomers = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return filteredCustomers.value.slice(start, end)
})

const visiblePages = computed(() => {
  const total = totalPages.value
  const current = currentPage.value
  const delta = 2
  const range = []
  const rangeWithDots = []

  for (let i = Math.max(2, current - delta); i <= Math.min(total - 1, current + delta); i++) {
    range.push(i)
  }

  if (current - delta > 2) {
    rangeWithDots.push(1, '...')
  } else {
    rangeWithDots.push(1)
  }

  rangeWithDots.push(...range)

  if (current + delta < total - 1) {
    rangeWithDots.push('...', total)
  } else if (total > 1) {
    rangeWithDots.push(total)
  }

  return rangeWithDots
})

onMounted(async () => {
  console.log('=== VoucherFormPage onMounted ===')
  const id = route.params.id
  console.log('Route params:', route.params)
  console.log('ID from params:', id)
  console.log('ID type:', typeof id)
  console.log('Form data initial:', formData.value)
  
  if (id && id !== 'new') {
    isEdit.value = true
    const parsedId = parseInt(id as string)
    console.log('Parsed ID:', parsedId)
    console.log('Is valid number:', !isNaN(parsedId))
    
    if (!isNaN(parsedId)) {
      voucherId.value = parsedId
      await loadVoucher()
    } else {
      console.error('Invalid voucher ID:', id)
      toastRef.value?.error('Lỗi', 'ID voucher không hợp lệ')
      goBack()
    }
  } else {
    // Set default dates
    const today = new Date()
    const nextMonth = new Date(today)
    nextMonth.setMonth(today.getMonth() + 1)
    
    formData.value.ngayBatDau = today.toISOString().split('T')[0]
    formData.value.ngayKetThuc = nextMonth.toISOString().split('T')[0]
  }
  
  // Customer stats will be loaded automatically when private voucher checkbox is checked
})

async function loadVoucher() {
  if (!voucherId.value) return
  
  try {
    console.log('Loading voucher with ID:', voucherId.value)
    const { data } = await api.get(`/api/phieu-giam-gia/${voucherId.value}`)
    console.log('Loaded voucher data:', data)
    
    formData.value = {
      ...data,
      ngayBatDau: data.ngayBatDau?.split('T')[0] || '',
      ngayKetThuc: data.ngayKetThuc?.split('T')[0] || '',
      trangThai: data.trangThai === 1
    }
    
    // Load selected customers for private vouchers
    if (data.riengTu) {
      console.log('Loading customers for private voucher')
      await loadSelectedCustomers()
    }
  } catch (error) {
    console.error('Lỗi khi tải voucher:', error)
    toastRef.value?.error('Lỗi', 'Không thể tải thông tin voucher')
    goBack()
  }
}

async function loadSelectedCustomers() {
  if (!voucherId.value) {
    console.log('No voucher ID, skipping customer load')
    return
  }
  
  try {
    console.log('=== Loading selected customers for voucher:', voucherId.value, '===')
    
    // Try the simple endpoint first
    let response
    try {
      response = await api.get(`/api/khach-hang-giam-gia/simple/voucher/${voucherId.value}`)
      console.log('Simple endpoint response status:', response.status)
    } catch (simpleError) {
      console.log('Simple endpoint failed, trying original endpoint...')
      response = await api.get(`/api/khach-hang-giam-gia/voucher/${voucherId.value}`)
      console.log('Original endpoint response status:', response.status)
    }
    
    console.log('Customer relations raw data:', response.data)
    
    if (!response.data || !Array.isArray(response.data)) {
      console.warn('Invalid response data format:', response.data)
      selectedCustomers.value = []
      return
    }
    
    // Map relations to selectedCustomers format
    const mappedCustomers = response.data.map(relation => {
      console.log('Processing relation:', relation)
      
      if (!relation.khachHang) {
        console.warn('Relation has no customer data:', relation)
        return null
      }
      
      return {
        id: relation.khachHang.id,
        hoTen: relation.khachHang.hoTen || '',
        soDienThoai: relation.khachHang.soDienThoai || '',
        email: relation.khachHang.email || '',
        daSuDung: relation.daSuDung || false
      }
    }).filter(customer => customer && customer.id) // Filter out invalid customers
    
    selectedCustomers.value = mappedCustomers
    console.log('Successfully loaded selected customers:', selectedCustomers.value.length, 'customers')
    console.log('Selected customers details:', selectedCustomers.value)
    
    // Update selectedCustomerIds for the UI
    selectedCustomerIds.value = selectedCustomers.value.map(c => c.id)
    console.log('Updated selectedCustomerIds:', selectedCustomerIds.value)
    
  } catch (error) {
    console.error('Error loading selected customers:', error)
    console.error('Error response:', error.response?.data)
    console.error('Error status:', error.response?.status)
    
    selectedCustomers.value = []
    selectedCustomerIds.value = []
    
    // Show error toast for debugging
    toastRef.value?.error('Lỗi', `Không thể tải danh sách khách hàng: ${error.response?.data?.message || error.message}`)
  }
}


// Validation functions
function clearErrors() {
  Object.keys(errors.value).forEach(key => {
    errors.value[key as keyof typeof errors.value] = ''
  })
}

function validateForm(): boolean {
  clearErrors()
  let isValid = true

  // Validate tên phiếu giảm giá
  if (!formData.value.tenPhieuGiamGia.trim()) {
    errors.value.tenPhieuGiamGia = 'Tên phiếu giảm giá không được để trống'
    isValid = false
  } else if (formData.value.tenPhieuGiamGia.trim().length < 3) {
    errors.value.tenPhieuGiamGia = 'Tên phiếu giảm giá phải có ít nhất 3 ký tự'
    isValid = false
  } else if (formData.value.tenPhieuGiamGia.trim().length > 100) {
    errors.value.tenPhieuGiamGia = 'Tên phiếu giảm giá không được vượt quá 100 ký tự'
    isValid = false
  }

  // Validate loại phiếu giảm giá
  if (!formData.value.loaiPhieuGiamGia) {
    errors.value.loaiPhieuGiamGia = 'Vui lòng chọn loại phiếu giảm giá'
    isValid = false
  }

  // Validate giá trị giảm giá
  if (!formData.value.giaTriGiamGia || formData.value.giaTriGiamGia <= 0) {
    errors.value.giaTriGiamGia = 'Giá trị giảm giá phải lớn hơn 0'
    isValid = false
  } else if (formData.value.loaiPhieuGiamGia === 'PERCENT') {
    if (formData.value.giaTriGiamGia > 100) {
      errors.value.giaTriGiamGia = 'Phần trăm giảm không được vượt quá 100%'
      isValid = false
    }
  } else if (formData.value.loaiPhieuGiamGia === 'FIXED') {
    if (formData.value.giaTriGiamGia > 10000000) {
      errors.value.giaTriGiamGia = 'Số tiền giảm không được vượt quá 10,000,000 VND'
      isValid = false
    }
  }

  // Validate số lượng sử dụng
  if (!formData.value.soLuongDung || formData.value.soLuongDung < 1) {
    errors.value.soLuongDung = 'Số lượng sử dụng phải ít nhất là 1'
    isValid = false
  } else if (formData.value.soLuongDung > 10000) {
    errors.value.soLuongDung = 'Số lượng sử dụng không được vượt quá 10,000'
    isValid = false
  }

  // Validate ngày bắt đầu
  if (!formData.value.ngayBatDau) {
    errors.value.ngayBatDau = 'Vui lòng chọn ngày bắt đầu'
    isValid = false
  } else {
    const startDate = new Date(formData.value.ngayBatDau)
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    
    if (startDate < today && !isEdit.value) {
      errors.value.ngayBatDau = 'Ngày bắt đầu không được là ngày trong quá khứ'
      isValid = false
    }
  }

  // Validate ngày kết thúc
  if (!formData.value.ngayKetThuc) {
    errors.value.ngayKetThuc = 'Vui lòng chọn ngày kết thúc'
    isValid = false
  } else if (formData.value.ngayBatDau) {
    const startDate = new Date(formData.value.ngayBatDau)
    const endDate = new Date(formData.value.ngayKetThuc)
    
    if (endDate <= startDate) {
      errors.value.ngayKetThuc = 'Ngày kết thúc phải sau ngày bắt đầu'
      isValid = false
    }
    
    // Check if the voucher duration is reasonable (max 1 year)
    const diffTime = Math.abs(endDate.getTime() - startDate.getTime())
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
    if (diffDays > 365) {
      errors.value.ngayKetThuc = 'Thời gian hiệu lực không được vượt quá 1 năm'
      isValid = false
    }
  }

  // Validate số tiền giảm tối đa (cho phần trăm)
  if (formData.value.loaiPhieuGiamGia === 'PERCENT' && formData.value.soTienGiamToiDa) {
    if (formData.value.soTienGiamToiDa < 1000) {
      errors.value.soTienGiamToiDa = 'Số tiền giảm tối đa phải ít nhất 1,000 VND'
      isValid = false
    } else if (formData.value.soTienGiamToiDa > 50000000) {
      errors.value.soTienGiamToiDa = 'Số tiền giảm tối đa không được vượt quá 50,000,000 VND'
      isValid = false
    }
  }

  // Validate hóa đơn tối thiểu
  if (formData.value.hoaDonToiThieu) {
    if (formData.value.hoaDonToiThieu < 0) {
      errors.value.hoaDonToiThieu = 'Hóa đơn tối thiểu không được âm'
      isValid = false
    } else if (formData.value.hoaDonToiThieu > 100000000) {
      errors.value.hoaDonToiThieu = 'Hóa đơn tối thiểu không được vượt quá 100,000,000 VND'
      isValid = false
    }
    
    // For fixed amount vouchers, minimum order should be higher than discount
    if (formData.value.loaiPhieuGiamGia === 'FIXED' && formData.value.giaTriGiamGia) {
      if (formData.value.hoaDonToiThieu <= formData.value.giaTriGiamGia) {
        errors.value.hoaDonToiThieu = 'Hóa đơn tối thiểu phải lớn hơn số tiền giảm'
        isValid = false
      }
    }
  }

  // Validate khách hàng cho voucher riêng tư
  if (formData.value.riengTu) {
    if (selectedCustomers.value.length === 0) {
      errors.value.selectedCustomers = 'Voucher riêng tư phải chọn ít nhất 1 khách hàng'
      isValid = false
    }
    // Không giới hạn số lượng khách hàng vì voucher riêng tư chỉ có thể dùng 1 lần
  }

  return isValid
}

async function handleSubmit() {
  if (!validateForm()) {
    toastRef.value?.error('Lỗi', 'Vui lòng kiểm tra lại thông tin đã nhập')
    return
  }

  // Show confirm modal
  confirmAction.value = isEdit.value ? 'edit' : 'add'
  showConfirmModal.value = true
}

async function confirmSubmit() {
  loading.value = true
  
  try {
    const submitData = {
      ...formData.value,
      trangThai: formData.value.trangThai ? 1 : 0,
      riengTu: formData.value.riengTu || false,
      selectedCustomers: formData.value.riengTu ? selectedCustomers.value.map(c => c.id) : []
    }

    console.log('Submitting voucher data:', submitData)

    if (isEdit.value && voucherId.value) {
      console.log('Updating voucher with ID:', voucherId.value)
      const response = await api.put(`/api/phieu-giam-gia/${voucherId.value}`, submitData)
      console.log('Update response:', response)
      toastRef.value?.success('Thành công', 'Cập nhật phiếu giảm giá thành công!')
    } else {
      console.log('Creating new voucher')
      const response = await api.post('/api/phieu-giam-gia', submitData)
      console.log('Create response:', response)
      toastRef.value?.success('Thành công', 'Thêm phiếu giảm giá thành công!')
    }
    
    setTimeout(() => {
      goBack()
    }, 1500)
  } catch (error: any) {
    console.error('Lỗi khi lưu:', error)
    if (error.response?.data) {
      toastRef.value?.error('Lỗi', error.response.data)
    } else {
      toastRef.value?.error('Lỗi', 'Có lỗi xảy ra khi lưu phiếu giảm giá')
    }
  } finally {
    loading.value = false
    showConfirmModal.value = false
    confirmAction.value = null
  }
}

function cancelSubmit() {
  showConfirmModal.value = false
  confirmAction.value = null
}

function goBack() {
  router.push('/voucher')
}

// Customer selection functions
async function searchCustomers() {
  if (searchText.value.trim().length < 2) {
    customers.value = []
    return
  }
  
  searching.value = true
  try {
    const { data } = await api.get(`/api/khach-hang/search?query=${encodeURIComponent(searchText.value)}`)
    // Lọc bỏ khách hàng đã được chọn
    customers.value = data.filter(customer => 
      !selectedCustomers.value.some(selected => selected.id === customer.id)
    )
  } catch (error) {
    console.error('Lỗi khi tìm kiếm khách hàng:', error)
    customers.value = []
  } finally {
    searching.value = false
  }
}

function selectCustomer(customer) {
  // Voucher riêng tư có thể chọn nhiều khách hàng, nhưng chỉ 1 người được dùng
  selectedCustomers.value.push(customer)
  customers.value = customers.value.filter(c => c.id !== customer.id)
  searchText.value = ''
}

function removeCustomer(customerId) {
  selectedCustomers.value = selectedCustomers.value.filter(c => c.id !== customerId)
}

function openCustomerModal() {
  showCustomerModal.value = true
  searchText.value = ''
  customers.value = []
}

function closeCustomerModal() {
  showCustomerModal.value = false
  searchText.value = ''
  customers.value = []
}

// Customer statistics and filtering methods
async function loadCustomerStats() {
  try {
    console.log('Loading customer statistics from database...')
    
    // Load customers from database
    const customersResponse = await api.get('/api/khach-hang')
    console.log('Database customers response:', customersResponse.data)
    
    if (customersResponse.data && customersResponse.data.length > 0) {
      // Load invoices for each customer to calculate real statistics
      const customersWithStats = await Promise.all(
        customersResponse.data.map(async (customer) => {
          try {
            // Get invoices for this customer
            const invoicesResponse = await api.get(`/api/hoa-don/khach-hang/${customer.id}`)
            const invoices = invoicesResponse.data || []
            
            // Calculate real statistics from invoices
            const totalSpent = invoices.reduce((sum, invoice) => sum + (invoice.tongTien || 0), 0)
            const orderCount = invoices.length
            const latestOrder = invoices.length > 0 
              ? invoices.sort((a, b) => new Date(b.ngayTao) - new Date(a.ngayTao))[0]
              : null
            
            // Determine customer status based on order frequency
            let status = 'Mới'
            if (orderCount >= 10) {
              status = 'VIP'
            } else if (orderCount >= 5) {
              status = 'Tích cực'
            } else if (orderCount >= 2) {
              status = 'Thường xuyên'
            }
            
            return {
              ...customer,
              tongChiTieu: totalSpent,
              soDonHang: orderCount,
              donHangGanNhat: latestOrder ? latestOrder.ngayTao : null,
              trangThai: status
            }
          } catch (error) {
            console.error(`Error loading invoices for customer ${customer.id}:`, error)
            return {
              ...customer,
              tongChiTieu: 0,
              soDonHang: 0,
              donHangGanNhat: null,
              trangThai: 'Mới'
            }
          }
        })
      )
      
      // Calculate overall statistics
      const totalCustomers = customersWithStats.length
      
      // Sort customers by total spending (descending) and then by total orders (descending)
      const sortedCustomers = customersWithStats.sort((a, b) => {
        // First sort by total spending (descending)
        if (b.tongChiTieu !== a.tongChiTieu) {
          return b.tongChiTieu - a.tongChiTieu
        }
        // If total spending is equal, sort by total orders (descending)
        return b.soDonHang - a.soDonHang
      })
      
      customers.value = sortedCustomers
      filteredCustomers.value = sortedCustomers
      
      console.log('Customers with real invoice data:', sortedCustomers)
      
      toastRef.value?.success('Thành công', `Đã tải ${totalCustomers} khách hàng với dữ liệu hóa đơn thật`)
    } else {
      // No customers found in database
      customers.value = []
      filteredCustomers.value = []
      console.log('No customers found in database')
      toastRef.value?.warning('Thông báo', 'Không có khách hàng nào trong database')
    }
  } catch (error) {
    console.error('Error loading customer statistics from database:', error)
    console.error('Error details:', {
      status: error.response?.status,
      statusText: error.response?.statusText,
      data: error.response?.data,
      message: error.message
    })
    
    // Set default values to prevent errors
    customers.value = []
    filteredCustomers.value = []
    
    // Show detailed error message
    const errorMessage = error.response?.data?.message || error.message || 'Không thể kết nối đến server'
    toastRef.value?.error('Lỗi Database', `Không thể tải dữ liệu từ database: ${errorMessage}`)
  }
}


// Pagination functions
function goToPage(page) {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

function resetToFirstPage() {
  currentPage.value = 1
}

// Refresh customers data
async function refreshCustomers() {
  if (!formData.value.riengTu) return
  
  loading.value = true
  try {
    await loadCustomerStats()
    applyFilters()
    toastRef.value?.success('Thành công', 'Danh sách khách hàng đã được làm mới')
  } catch (error) {
    console.error('Error refreshing customers:', error)
    toastRef.value?.error('Lỗi', 'Không thể làm mới danh sách khách hàng')
  } finally {
    loading.value = false
  }
}

// Apply filters to customers
function applyFilters() {
  const filters = searchFilters.value
  
  if (!filters.name && !filters.phone && !filters.status) {
    filteredCustomers.value = customers.value
  } else {
    filteredCustomers.value = customers.value.filter(customer => {
      // Name filter
      if (filters.name && !customer.hoTen.toLowerCase().includes(filters.name.toLowerCase())) {
        return false
      }
      
      // Phone filter
      if (filters.phone && !customer.soDienThoai.includes(filters.phone)) {
        return false
      }
      
      // Status filter
      if (filters.status && customer.trangThai !== filters.status) {
        return false
      }
      
      return true
    })
  }
  
  // Reset to first page when filtering
  currentPage.value = 1
}

// Clear all filters
function clearFilters() {
  searchFilters.value = {
    name: '',
    phone: '',
    status: ''
  }
  applyFilters()
}


function toggleSelectAllCustomers() {
  if (isAllCustomersSelected.value) {
    selectedCustomerIds.value = []
  } else {
    selectedCustomerIds.value = filteredCustomers.value.map(customer => customer.id)
  }
  updateSelectedCustomers()
}

function updateSelectedCustomers() {
  selectedCustomers.value = customers.value.filter(customer => 
    selectedCustomerIds.value.includes(customer.id)
  )
}

// Handle private voucher checkbox change
async function handlePrivateVoucherChange() {
  if (formData.value.riengTu) {
    // Load customer data when private voucher is selected
    try {
      console.log('Private voucher selected, loading customer data...')
      await loadCustomerStats()
    } catch (error) {
      console.error('Error loading customer data for private voucher:', error)
      toastRef.value?.error('Lỗi', 'Không thể tải dữ liệu khách hàng')
    }
  } else {
    // Clear customer data when private voucher is deselected
    customers.value = []
    filteredCustomers.value = []
    selectedCustomerIds.value = []
    selectedCustomers.value = []
    searchFilters.value = {
      name: '',
      phone: '',
      status: ''
    }
  }
}

function getCustomerStatusClass(customer) {
  if (!customer) return 'status-new'
  const status = customer.trangThai || 'Mới'
  switch (status) {
    case 'VIP': return 'status-vip'
    case 'Tích cực': return 'status-active'
    case 'Thường xuyên': return 'status-regular'
    default: return 'status-new'
  }
}

function getCustomerStatusText(customer) {
  if (!customer) return 'Mới'
  return customer.trangThai || 'Mới'
}

function formatCurrency(amount) {
  if (!amount) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', { 
    style: 'currency', 
    currency: 'VND' 
  }).format(amount)
}

function formatDate(dateString) {
  if (!dateString) return 'Chưa có'
  return new Date(dateString).toLocaleDateString('vi-VN')
}


</script>

<style scoped>
.form-page {
  background: #f8fafc;
  min-height: 100vh;
  padding: 0;
  padding-top: 80px;
  width: 100%;
  overflow-x: hidden;
  position: relative;
}

.form-container {
  padding: 24px;
  padding-top: 0;
  width: 100%;
  margin: 0;
}

.form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  width: 100%;
  border-bottom: 2px solid #e9ecef;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.btn-back {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #6c757d;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.btn-back:hover {
  background: #545b62;
}

.icon-arrow-left::before {
  content: "←";
}

.form-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.voucher-form {
  background: white;
  padding: 32px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  width: 100%;
}

.form-section {
  margin-bottom: 40px;
  padding-bottom: 30px;
  border-bottom: 1px solid #e9ecef;
}

.form-section:last-of-type {
  border-bottom: none;
  margin-bottom: 0;
}

.form-section h2 {
  color: #495057;
  margin-bottom: 20px;
  font-size: 18px;
  font-weight: 600;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-weight: 600;
  color: #495057;
  font-size: 14px;
}

.required {
  color: #dc3545;
}

.help-text {
  color: #6b7280;
  font-size: 12px;
  font-weight: normal;
  display: block;
  margin-top: 2px;
}

.current-value-display {
  background: #f0f9ff;
  border: 1px solid #0ea5e9;
  border-radius: 4px;
  padding: 8px 12px;
  margin-top: 8px;
  font-size: 13px;
  color: #0369a1;
  font-weight: 500;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 12px;
  border: 2px solid #e9ecef;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
}

.form-group input.error,
.form-group select.error,
.form-group textarea.error {
  border-color: #dc3545;
  box-shadow: 0 0 0 3px rgba(220, 53, 69, 0.1);
}

.error-message {
  color: #dc3545;
  font-size: 12px;
  margin-top: 4px;
  display: block;
}

.checkbox-label {
  display: flex !important;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  font-weight: normal !important;
}

.checkbox-label input[type="checkbox"] {
  width: auto;
  margin: 0;
}

.checkmark {
  font-size: 14px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 40px;
  padding-top: 30px;
  border-top: 2px solid #e9ecef;
}

.btn-cancel {
  padding: 12px 24px;
  background: #6c757d;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: background-color 0.3s;
}

.btn-cancel:hover {
  background: #545b62;
}

.btn-submit {
  padding: 12px 24px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: background-color 0.3s;
}

.btn-submit:hover:not(:disabled) {
  background: #0056b3;
}

.btn-submit:disabled {
  background: #6c757d;
  cursor: not-allowed;
}

/* Customer Selection Styles */
.customer-selection-section {
  margin-top: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.customer-selection-section h3 {
  margin: 0 0 15px 0;
  color: #495057;
  font-size: 16px;
}

.customer-selection-info p {
  margin: 0 0 15px 0;
  color: #6c757d;
  font-size: 14px;
}

.btn-select-customer {
  padding: 10px 16px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  margin-bottom: 15px;
}

.btn-select-customer:hover {
  background: #0056b3;
}

.selected-customers {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.customer-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: white;
  border: 1px solid #dee2e6;
  border-radius: 6px;
}

.customer-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.customer-name {
  font-weight: 600;
  color: #495057;
}

.customer-phone {
  font-size: 12px;
  color: #6c757d;
}

.btn-remove-customer {
  background: #dc3545;
  color: white;
  border: none;
  border-radius: 4px;
  width: 24px;
  height: 24px;
  cursor: pointer;
  font-size: 16px;
}

.btn-remove-customer:hover {
  background: #c82333;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  max-height: 70vh;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e9ecef;
}

.modal-header h3 {
  margin: 0;
  color: #495057;
}

.btn-close-modal {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #6c757d;
  width: 30px;
  height: 30px;
}

.modal-body {
  padding: 20px;
  flex: 1;
  overflow-y: auto;
}

.search-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ced4da;
  border-radius: 6px;
  font-size: 14px;
  margin-bottom: 20px;
}

.search-input:focus {
  outline: none;
  border-color: #007bff;
}

.loading, .no-results, .search-hint {
  text-align: center;
  padding: 20px;
  color: #6c757d;
}

.customer-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.customer-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  cursor: pointer;
}

.customer-option:hover {
  background: #f8f9fa;
}

.btn-select {
  background: #28a745;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 6px 12px;
  font-size: 12px;
  cursor: pointer;
}

.btn-select:hover {
  background: #218838;
}

.modal-footer {
  padding: 20px;
  border-top: 1px solid #e9ecef;
  display: flex;
  justify-content: flex-end;
}






@media (max-width: 768px) {
  .form-page {
    padding-top: 60px;
  }
  
  .form-container {
    padding: 16px;
  }
  
  .form-row {
    grid-template-columns: 1fr;
    gap: 15px;
  }
  
  .header-left {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .voucher-form {
    padding: 20px;
  }
  
  .form-actions {
    flex-direction: column;
  }
}

/* Customer Statistics Styles */
.customer-stats-section {
  margin: 20px 0;
  padding: 20px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.customer-stats-section h4 {
  margin: 0 0 16px 0;
  color: #1e293b;
  font-size: 16px;
  font-weight: 600;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.stat-card {
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
  text-align: center;
  transition: all 0.2s ease;
}

.stat-card:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.stat-content {
  width: 100%;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 8px;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-card-blue {
  border-left: 4px solid #3b82f6;
}

.stat-card-blue .stat-number {
  color: #3b82f6;
}

.stat-card-green {
  border-left: 4px solid #10b981;
}

.stat-card-green .stat-number {
  color: #10b981;
}

.stat-card-orange {
  border-left: 4px solid #f59e0b;
}

.stat-card-orange .stat-number {
  color: #f59e0b;
}

.stat-card-purple {
  border-left: 4px solid #8b5cf6;
}

.stat-card-purple .stat-number {
  color: #8b5cf6;
}

/* Customer Table Styles */
.customer-table-section {
  margin: 20px 0;
  background: white;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  overflow: hidden;
}

.table-header {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  min-height: 80px;
}


.table-actions {
  display: flex;
  align-items: center;
  gap: 24px;
  justify-content: space-between;
  width: 100%;
  height: 100%;
}

.search-container {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
}

.search-input {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  width: 300px;
  height: 40px;
  box-sizing: border-box;
}

/* Enhanced Search Filters */
.search-filters {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
}

.search-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.search-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.search-group label {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  margin: 0;
}

.search-group .search-input {
  width: 100%;
  height: 36px;
  font-size: 13px;
  padding: 6px 10px;
}

.search-group .search-select {
  width: 100%;
  height: 36px;
  font-size: 13px;
  padding: 6px 10px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: white;
  color: #374151;
  cursor: pointer;
}

.search-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e2e8f0;
}

.btn-search, .btn-clear, .btn-reload {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  height: 36px;
}

.btn-search {
  background: #3b82f6;
  color: white;
}

.btn-search:hover {
  background: #2563eb;
}

.btn-clear {
  background: #6b7280;
  color: white;
}

.btn-clear:hover {
  background: #4b5563;
}

.btn-reload {
  background: #10b981;
  color: white;
}

.btn-reload:hover {
  background: #059669;
}

.btn-reload:disabled {
  background: #d1d5db;
  cursor: not-allowed;
}

.pagination-size-selector {
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
  flex-shrink: 0;
}

.pagination-size-selector label {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
  height: 40px;
  display: flex;
  align-items: center;
  line-height: 1;
}

.size-select {
  padding: 8px 10px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: white;
  color: #374151;
  font-size: 14px;
  cursor: pointer;
  min-width: 60px;
  height: 40px;
  box-sizing: border-box;
}

.size-select:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}



/* Pagination Styles */
.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
}

.pagination-info {
  font-size: 14px;
  color: #64748b;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination-numbers {
  display: flex;
  align-items: center;
  gap: 4px;
}

.pagination-btn {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  background: white;
  color: #374151;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
  min-width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pagination-btn:hover:not(:disabled) {
  background: #f3f4f6;
  border-color: #9ca3af;
}

.pagination-btn.active {
  background: #3b82f6;
  color: white;
  border-color: #3b82f6;
}

.pagination-btn:disabled {
  background: #f9fafb;
  color: #9ca3af;
  cursor: not-allowed;
  border-color: #e5e7eb;
}

.customer-table-container {
  max-height: 400px;
  overflow-y: auto;
}

.customer-table {
  width: 100%;
  border-collapse: collapse;
}

.customer-table th,
.customer-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #e2e8f0;
}

.customer-table th {
  background: #f8fafc;
  font-weight: 600;
  color: #374151;
  font-size: 14px;
}

.customer-table td {
  font-size: 14px;
  color: #1f2937;
}

.customer-row:hover {
  background: #f8fafc;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  text-transform: uppercase;
}

.status-vip {
  background: #fef3c7;
  color: #92400e;
}

.status-active {
  background: #dcfce7;
  color: #166534;
}

.status-regular {
  background: #e0f2fe;
  color: #0277bd;
}

.status-new {
  background: #f3f4f6;
  color: #6b7280;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .table-header {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .table-actions {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .search-container {
    order: 1;
  }
  
  .pagination-size-selector {
    order: 2;
    justify-content: center;
  }
  
  .search-input {
    width: 100%;
  }
  
  .customer-table-container {
    overflow-x: auto;
  }
  
  .customer-table {
    min-width: 600px;
  }
  
  .pagination-container {
    flex-direction: column;
    gap: 12px;
    padding: 12px 16px;
  }
  
  .pagination-controls {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .pagination-btn {
    min-width: 36px;
    height: 36px;
    font-size: 13px;
  }
}

/* Refresh Button Styles */
.btn-refresh {
  background: #3b82f6;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
  margin-right: 12px;
}

.btn-refresh:hover:not(:disabled) {
  background: #2563eb;
  transform: translateY(-1px);
}

.btn-refresh:disabled {
  background: #9ca3af;
  cursor: not-allowed;
  transform: none;
}

/* Horizontal Filters Styles */
.horizontal-filters {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  margin-right: 12px;
}

.filter-group {
  display: flex;
  align-items: center;
}

.filter-input,
.filter-select {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  min-width: 150px;
  transition: border-color 0.2s;
}

.filter-input:focus,
.filter-select:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.btn-clear-filters {
  background: #6b7280;
  color: white;
  border: none;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.2s;
}

.btn-clear-filters:hover {
  background: #4b5563;
  transform: translateY(-1px);
}

/* Responsive adjustments for filters */
@media (max-width: 1024px) {
  .horizontal-filters {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .filter-input,
  .filter-select {
    min-width: 120px;
  }
}

@media (max-width: 768px) {
  .table-actions {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .horizontal-filters {
    margin-right: 0;
    justify-content: center;
  }
  
  .btn-refresh {
    margin-right: 0;
    align-self: center;
  }
  
  .pagination-size-selector {
    align-self: center;
  }
}
</style>
