<template>
  <div class="page dark-mode-transition">
    <PosHeader />
    
    <div class="content">

    <Toast ref="toastRef" />

    <!-- Filter Section -->
    <div class="filter-section">
      <div class="filter-header">
        <div class="filter-title">
          <h3>Bộ lọc nâng cao</h3>
          <p>Tìm kiếm và lọc voucher theo tiêu chí</p>
        </div>
        <div class="filter-actions">
          <button class="btn-clear-filters" @click="clearAllFilters">
            Xóa bộ lọc
          </button>
        </div>
      </div>
      <div class="filter-row">
        <div class="filter-group">
          <label>Tìm kiếm:</label>
          <input 
            type="text" 
            v-model="searchText" 
            placeholder="Tìm theo tên hoặc mã voucher..."
            @input="applyFilters"
            class="search-input"
          />
        </div>
        <div class="filter-group">
          <label>Trạng thái:</label>
          <select v-model="statusFilter" @change="applyFilters" class="filter-select">
            <option value="">Tất cả trạng thái</option>
            <option value="1">Hoạt động</option>
            <option value="0">Không hoạt động</option>
          </select>
        </div>
        <div class="filter-group">
          <label>Loại voucher:</label>
          <select v-model="typeFilter" @change="applyFilters" class="filter-select">
            <option value="">Tất cả loại</option>
            <option value="PERCENT">Phần trăm (%)</option>
            <option value="FIXED">Số tiền cố định (VND)</option>
          </select>
        </div>
        <div class="filter-group">
          <label>Loại phiếu:</label>
          <select v-model="privacyFilter" @change="applyFilters" class="filter-select">
            <option value="">Tất cả loại phiếu</option>
            <option value="public">Công khai</option>
            <option value="private">Cá nhân</option>
          </select>
        </div>
        <div class="filter-group">
          <label>Từ ngày:</label>
          <input 
            type="date" 
            v-model="startDateFilter" 
            @change="applyFilters"
            class="filter-select"
          />
        </div>
        <div class="filter-group">
          <label>Đến ngày:</label>
          <input 
            type="date" 
            v-model="endDateFilter" 
            @change="applyFilters"
            class="filter-select"
          />
        </div>
      </div>
    </div>

    <!-- Add Voucher Button -->
    <div class="add-voucher-section">
      <button class="btn-add-voucher" @click="addVoucher()">
        <i class="icon-plus"></i>
        Thêm Voucher
      </button>
    </div>

    <!-- Main Content Area -->
    <div class="main-content">
      <!-- Table Header Section -->
      <div class="table-header">
        <div class="table-title">
          <h2>Danh sách Voucher</h2>
          <span class="item-count">{{ totalItems }} voucher</span>
        </div>
        <div class="table-actions">
          <div class="items-per-page">
            <label>Hiển thị:</label>
            <select v-model="itemsPerPage" @change="applyFilters" class="page-size-select">
              <option value="5">5 mục / trang</option>
              <option value="10">10 mục / trang</option>
              <option value="20">20 mục / trang</option>
              <option value="50">50 mục / trang</option>
            </select>
          </div>
        </div>
      </div>

      <div class="table-container">
        <table>
          <thead>
            <tr>
              <th class="checkbox-column">
                <input type="checkbox" class="select-all-checkbox" />
              </th>
              <th>STT</th>
              <th>Mã Phiếu</th>
              <th>Tên Phiếu</th>
              <th>Loại</th>
              <th>Giá Trị</th>
              <th>Số Lượng</th>
              <th>Ngày Bắt Đầu</th>
              <th>Ngày Kết Thúc</th>
              <th>Trạng Thái</th>
              <th>Thao Tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(voucher, index) in filteredVouchers" :key="voucher.id">
              <td class="checkbox-column">
                <input type="checkbox" class="row-checkbox" :value="voucher.id" />
              </td>
              <td>{{ startItem + index }}</td>
              <td>{{ voucher.maPhieuGiamGia }}</td>
              <td>{{ voucher.tenPhieuGiamGia }}</td>
              <td>
                <div class="voucher-type">
                  <span :class="getTypeClass(voucher)">
                    {{ getTypeLabel(voucher) }}
                  </span>
                </div>
              </td>
              <td>
                <div class="discount-value">
                  <span class="value">{{ formatDiscountValue(voucher) }}</span>
                  <span v-if="voucher.soTienGiamToiDa && isPercentVoucher(voucher)" class="max-discount">
                    Tối đa: {{ formatCurrency(voucher.soTienGiamToiDa) }}
                  </span>
                  <span v-if="voucher.hoaDonToiThieu" class="min-order">
                    Đơn tối thiểu: {{ formatCurrency(voucher.hoaDonToiThieu) }}
                  </span>
                </div>
              </td>
              <td>
                <div class="quantity-display">
                  <span class="quantity-number">{{ voucher.soLuongDung }}</span>
                  <span v-if="voucher.riengTu" class="quantity-type private">
                    Cá nhân
                  </span>
                  <span v-else class="quantity-type public">
                    Công khai
                  </span>
                </div>
              </td>
              <td>{{ formatDate(voucher.ngayBatDau) }}</td>
              <td>{{ formatDate(voucher.ngayKetThuc) }}</td>
              <td>
                <div class="status-badge-container">
                  <span class="status-badge" :class="getStatusBadgeClass(voucher)">
                    {{ getStatusText(voucher) }}
                  </span>
                  <div class="toggle-container">
                    <label class="toggle-switch" :class="{ 'disabled': isVoucherExpired(voucher) || voucher.soLuongDung <= 0 }">
                      <input 
                        type="checkbox"
                        :checked="voucher.trangThai === 1"
                        :disabled="isVoucherExpired(voucher) || voucher.soLuongDung <= 0"
                        @change="toggleStatus(voucher.id)"
                      />
                      <span class="toggle-slider"></span>
                    </label>
                  </div>
                </div>
              </td>
              <td>
                <div class="action-buttons">
                  <button class="btn-view" @click="viewVoucher(voucher)" title="Xem chi tiết">
                    <font-awesome-icon icon="eye" />
                  </button>
                  <button class="btn-edit" @click="editVoucher(voucher)" title="Chỉnh sửa">
                    <font-awesome-icon icon="edit" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="pagination-section" v-if="totalPages > 1">
        <div class="pagination-info">
          <span>Hiển thị {{ startItem }} - {{ endItem }} / {{ totalItems }} mục</span>
        </div>
        
        <div class="pagination-controls">
          <button 
            class="pagination-btn prev-btn" 
            :disabled="currentPage === 1"
            @click="previousPage"
          >
            <i class="icon-chevron-left"></i>
            Trước
          </button>
          
          <div class="pagination-numbers">
            <button
              v-for="page in getVisiblePages()"
              :key="page"
              class="pagination-number"
              :class="{ active: page === currentPage, ellipsis: page === '...' }"
              @click="page !== '...' && goToPage(page as number)"
              :disabled="page === '...'"
            >
              {{ page }}
            </button>
          </div>
          
          <button 
            class="pagination-btn next-btn" 
            :disabled="currentPage === totalPages"
            @click="nextPage"
          >
            Sau
            <i class="icon-chevron-right"></i>
          </button>
        </div>
      </div>
    </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/services/api'
import Toast from '@/components/Toast.vue'
import PosHeader from '@/components/PosHeader.vue'

const router = useRouter()

interface PhieuGiamGia {
  id: number
  maPhieuGiamGia: string
  tenPhieuGiamGia: string
  loaiPhieuGiamGia?: string
  giaTriGiamGia: number
  soTienGiamToiDa?: number
  hoaDonToiThieu?: number
  soLuongDung: number
  ngayBatDau: string
  ngayKetThuc: string
  trangThai: number
  riengTu?: boolean
  moTa?: string
  ngayTao?: string
  ngayCapNhat?: string
}

const vouchers = ref<PhieuGiamGia[]>([])
const loading = ref(false)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

// Filter states
const statusFilter = ref('')
const typeFilter = ref('')
const privacyFilter = ref('')
const startDateFilter = ref('')
const endDateFilter = ref('')
const searchText = ref('')

// Pagination states
const currentPage = ref(1)
const itemsPerPage = ref(5)
const totalItems = ref(0)

// Computed property for filtered vouchers (without pagination)
const allFilteredVouchers = computed(() => {
  let filtered = vouchers.value

  // Filter by status (active/inactive)
  if (statusFilter.value !== '') {
    filtered = filtered.filter(v => v.trangThai.toString() === statusFilter.value)
  }

  // Filter by voucher type (PERCENT/FIXED)
  if (typeFilter.value !== '') {
    if (typeFilter.value === 'PERCENT') {
      filtered = filtered.filter(v => isPercentVoucher(v))
    } else if (typeFilter.value === 'FIXED') {
      filtered = filtered.filter(v => !isPercentVoucher(v))
    }
  }

  // Filter by privacy type (public/private)
  if (privacyFilter.value !== '') {
    if (privacyFilter.value === 'public') {
      filtered = filtered.filter(v => !v.riengTu)
    } else if (privacyFilter.value === 'private') {
      filtered = filtered.filter(v => v.riengTu)
    }
  }

  // Filter by date range
  if (startDateFilter.value !== '') {
    const startDate = new Date(startDateFilter.value)
    filtered = filtered.filter(v => new Date(v.ngayBatDau) >= startDate)
  }

  if (endDateFilter.value !== '') {
    const endDate = new Date(endDateFilter.value)
    filtered = filtered.filter(v => new Date(v.ngayKetThuc) <= endDate)
  }

  // Search filter
  if (searchText.value.trim() !== '') {
    const search = searchText.value.toLowerCase()
    filtered = filtered.filter(v => 
      v.tenPhieuGiamGia.toLowerCase().includes(search) ||
      v.maPhieuGiamGia.toLowerCase().includes(search) ||
      (v.moTa && v.moTa.toLowerCase().includes(search))
    )
  }

  return filtered
})

// Computed property for paginated vouchers
const filteredVouchers = computed(() => {
  const filtered = allFilteredVouchers.value
  totalItems.value = filtered.length
  
  const startIndex = (currentPage.value - 1) * itemsPerPage.value
  const endIndex = startIndex + itemsPerPage.value
  
  return filtered.slice(startIndex, endIndex)
})

// Computed properties for pagination
const totalPages = computed(() => Math.ceil(totalItems.value / itemsPerPage.value))
const startItem = computed(() => (currentPage.value - 1) * itemsPerPage.value + 1)
const endItem = computed(() => Math.min(currentPage.value * itemsPerPage.value, totalItems.value))

async function loadVouchers() {
  loading.value = true
  try {
    const { data } = await api.get<PhieuGiamGia[]>('/api/phieu-giam-gia')
    console.log('Loaded vouchers:', data)
    
    // Tự động vô hiệu hóa voucher hết hạn
    const updatedVouchers = []
    for (const voucher of data) {
      console.log('Processing voucher:', voucher.id, voucher.tenPhieuGiamGia, typeof voucher.id)
      if (voucher.trangThai === 1 && isVoucherExpired(voucher)) {
        try {
          // Tự động vô hiệu hóa voucher hết hạn - gạt toggle về OFF
          await api.post(`/api/phieu-giam-gia/${voucher.id}/toggle-status`)
          voucher.trangThai = 0
          console.log(`Tự động vô hiệu hóa voucher hết hạn: ${voucher.tenPhieuGiamGia}`)
        } catch (error) {
          console.error('Lỗi khi tự động vô hiệu hóa voucher:', error)
          // Nếu API call thất bại, vẫn cập nhật local state
          voucher.trangThai = 0
        }
      }
      updatedVouchers.push(voucher)
    }
    
    vouchers.value = updatedVouchers
  } catch (error) {
    console.error('Lỗi khi tải danh sách voucher:', error)
    toastRef.value?.error('Lỗi', 'Không thể tải danh sách phiếu giảm giá')
  } finally {
    loading.value = false
  }
}

function addVoucher() {
  router.push('/voucher/form/new')
}

function editVoucher(voucher: PhieuGiamGia) {
  console.log('Editing voucher:', voucher)
  console.log('Voucher ID:', voucher.id)
  console.log('Navigate to:', `/voucher/form/${voucher.id}`)
  router.push(`/voucher/form/${voucher.id}`)
}

function viewVoucher(voucher: PhieuGiamGia) {
  console.log('Viewing voucher:', voucher)
  // TODO: Implement view voucher modal or page
  toastRef.value?.info('Thông tin', `Xem chi tiết voucher: ${voucher.tenPhieuGiamGia}`)
}


async function toggleStatus(id: number) {
  // Tìm voucher để kiểm tra thời gian hiệu lực
  const voucher = vouchers.value.find(v => v.id === id)
  if (!voucher) return

  // Kiểm tra nếu voucher đã hết hạn
  if (isVoucherExpired(voucher)) {
    toastRef.value?.error('Không thể kích hoạt', 'Voucher đã hết thời gian hiệu lực. Không thể kích hoạt lại.')
    return
  }

  // Kiểm tra nếu voucher đã hết số lượng
  if (voucher.soLuongDung <= 0) {
    toastRef.value?.error('Không thể kích hoạt', 'Voucher đã hết số lượng sử dụng. Không thể kích hoạt lại.')
    return
  }

  // Kiểm tra nếu voucher chưa bắt đầu và đang cố gắng kích hoạt
  if (voucher.trangThai === 0 && isVoucherNotStarted(voucher)) {
    toastRef.value?.warning('Chưa thể kích hoạt', 'Voucher chưa đến thời gian hiệu lực.')
    return
  }

  try {
    await api.post(`/api/phieu-giam-gia/${id}/toggle-status`)
    
    const statusText = voucher.trangThai === 1 ? 'vô hiệu hóa' : 'kích hoạt'
    toastRef.value?.success('Thành công', `${statusText} voucher thành công!`)
    await loadVouchers()
  } catch (error: any) {
    console.error('Lỗi khi cập nhật trạng thái:', error)
    toastRef.value?.error('Lỗi', 'Không thể cập nhật trạng thái')
  }
}

function applyFilters() {
  // Reset to first page when filters change
  currentPage.value = 1
}

function clearAllFilters() {
  searchText.value = ''
  statusFilter.value = ''
  typeFilter.value = ''
  privacyFilter.value = ''
  startDateFilter.value = ''
  endDateFilter.value = ''
  currentPage.value = 1
  toastRef.value?.info('Thông báo', 'Đã xóa tất cả bộ lọc')
}

// Pagination functions
function goToPage(page: number) {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

function previousPage() {
  if (currentPage.value > 1) {
    currentPage.value--
  }
}

function nextPage() {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
  }
}

function getVisiblePages(): (number | string)[] {
  const pages: (number | string)[] = []
  const total = totalPages.value
  const current = currentPage.value
  
  if (total <= 7) {
    // Show all pages if total <= 7
    for (let i = 1; i <= total; i++) {
      pages.push(i)
    }
  } else {
    // Always show first page
    pages.push(1)
    
    if (current <= 4) {
      // Show 1, 2, 3, 4, 5, ..., last
      for (let i = 2; i <= 5; i++) {
        pages.push(i)
      }
      pages.push('...')
      pages.push(total)
    } else if (current >= total - 3) {
      // Show 1, ..., last-4, last-3, last-2, last-1, last
      pages.push('...')
      for (let i = total - 4; i <= total; i++) {
        pages.push(i)
      }
    } else {
      // Show 1, ..., current-1, current, current+1, ..., last
      pages.push('...')
      for (let i = current - 1; i <= current + 1; i++) {
        pages.push(i)
      }
      pages.push('...')
      pages.push(total)
    }
  }
  
  return pages
}

// Utility functions
function formatDate(dateString: string): string {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleDateString('vi-VN')
}

function isVoucherExpired(voucher: PhieuGiamGia): boolean {
  const now = new Date()
  const endDate = new Date(voucher.ngayKetThuc)
  return now > endDate
}

function isVoucherNotStarted(voucher: PhieuGiamGia): boolean {
  const now = new Date()
  const startDate = new Date(voucher.ngayBatDau)
  return now < startDate
}

function isVoucherInValidPeriod(voucher: PhieuGiamGia): boolean {
  const now = new Date()
  const startDate = new Date(voucher.ngayBatDau)
  const endDate = new Date(voucher.ngayKetThuc)
  return now >= startDate && now <= endDate
}

function formatDiscountValue(voucher: PhieuGiamGia): string {
  // Phân biệt dựa trên giá trị và tên
  const isPercent = isPercentVoucher(voucher)
  
  if (isPercent) {
    return `${voucher.giaTriGiamGia}%`
  } else {
    return new Intl.NumberFormat('vi-VN', { 
      style: 'currency', 
      currency: 'VND' 
    }).format(voucher.giaTriGiamGia)
  }
}

function isPercentVoucher(voucher: PhieuGiamGia): boolean {
  // Logic phân biệt voucher phần trăm vs số tiền cố định
  // Ưu tiên dựa trên field loaiPhieuGiamGia từ form
  if (voucher.loaiPhieuGiamGia) {
    return voucher.loaiPhieuGiamGia === 'PERCENT'
  }
  
  // Fallback: dựa trên tên voucher nếu không có field loaiPhieuGiamGia
  const name = voucher.tenPhieuGiamGia.toLowerCase()
  
  // Kiểm tra dấu hiệu số tiền cố định TRƯỚC (ưu tiên cao hơn)
  const hasFixedMoneyInName = name.includes('k') || 
                             name.includes('đ') || 
                             name.includes('vnd') || 
                             name.includes('000') ||
                             name.includes('tiền') ||
                             name.includes('tien')
  
  // Kiểm tra dấu hiệu phần trăm
  const hasPercentInName = name.includes('%') || 
                          name.includes('phần trăm') || 
                          name.includes('phan tram') ||
                          name.includes('percent')
  
  // Nếu có dấu hiệu rõ ràng trong tên
  if (hasFixedMoneyInName) return false  // Ưu tiên kiểm tra tiền trước
  if (hasPercentInName) return true
  
  // Kiểm tra pattern đặc biệt cho "Giảm giá X%" 
  if (name.includes('giảm giá') || name.includes('giam gia')) {
    // Nếu có "giảm giá" và giá trị nhỏ <= 100 thì có thể là %
    return voucher.giaTriGiamGia <= 100
  }
  
  // Fallback cuối cùng: dựa trên giá trị
  // <= 100 có thể là %, > 100 có thể là tiền
  return voucher.giaTriGiamGia <= 100
}

function getTypeLabel(voucher: PhieuGiamGia): string {
  return isPercentVoucher(voucher) ? 'Phần trăm' : 'Số tiền cố định'
}

function getTypeClass(voucher: PhieuGiamGia): string {
  return isPercentVoucher(voucher) ? 'type-percent' : 'type-fixed'
}


function formatCurrency(amount: number): string {
  if (!amount) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', { 
    style: 'currency', 
    currency: 'VND' 
  }).format(amount)
}

function getStatusLabel(voucher: PhieuGiamGia): string {
  const now = new Date()
  const startDate = new Date(voucher.ngayBatDau)
  const endDate = new Date(voucher.ngayKetThuc)
  
  if (voucher.trangThai === 0) return 'Không hoạt động'
  if (voucher.soLuongDung <= 0) return 'Hết lượt'
  if (now < startDate) return 'Chưa bắt đầu'
  if (now > endDate) return 'Đã hết hạn'
  return 'Hoạt động'
}

function getStatusClass(voucher: PhieuGiamGia): string {
  const now = new Date()
  const startDate = new Date(voucher.ngayBatDau)
  const endDate = new Date(voucher.ngayKetThuc)
  
  if (voucher.trangThai === 0) return 'status-inactive'
  if (voucher.soLuongDung <= 0) return 'status-expired'
  if (now < startDate) return 'status-pending'
  if (now > endDate) return 'status-expired'
  return 'status-active'
}

function getStatusText(voucher: PhieuGiamGia): string {
  const now = new Date()
  const startDate = new Date(voucher.ngayBatDau)
  const endDate = new Date(voucher.ngayKetThuc)
  
  if (voucher.trangThai === 0) return 'Vô hiệu'
  if (voucher.soLuongDung <= 0) return 'Hết số lượng'
  if (now < startDate) return 'Chưa bắt đầu'
  if (now > endDate) return 'Hết hạn'
  return 'Hoạt động'
}

function getStatusBadgeClass(voucher: PhieuGiamGia): string {
  const now = new Date()
  const startDate = new Date(voucher.ngayBatDau)
  const endDate = new Date(voucher.ngayKetThuc)
  
  if (voucher.trangThai === 0) return 'badge-inactive'
  if (voucher.soLuongDung <= 0) return 'badge-out-of-stock'
  if (now < startDate) return 'badge-pending'
  if (now > endDate) return 'badge-expired'
  return 'badge-active'
}

onMounted(loadVouchers)
</script>

<style scoped>
/* Orange and Black POS Theme */
.page {
  background: #f8fafc;
  min-height: 100vh;
  padding: 0;
  padding-top: 80px;
  width: 100%;
  overflow-x: hidden;
  position: relative;
}

.content {
  padding: 24px;
  padding-top: 0;
  width: 100%;
  margin: 0;
}



.btn-primary {
  background: linear-gradient(135deg, #f97316, #ea580c);
  color: white;
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(249, 115, 22, 0.3);
}

.btn-primary:hover {
  background: linear-gradient(135deg, #ea580c, #dc2626);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.4);
}

/* Add Voucher Section */
.add-voucher-section {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 24px;
  margin-top: 0;
}

.btn-add-voucher {
  background: #f97316;
  color: white;
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(249, 115, 22, 0.3);
}

.btn-add-voucher:hover {
  background: #ea580c;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.4);
}

.icon-plus::before {
  content: "+";
  font-size: 16px;
  font-weight: bold;
}

.filter-section {
  background: white;
  padding: 28px;
  margin-bottom: 24px;
  margin-top: 20px;
  border-radius: 16px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
  border: 1px solid #e2e8f0;
  position: relative;
  overflow: hidden;
}

.filter-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #f97316, #ea580c, #dc2626);
  border-radius: 16px 16px 0 0;
}

/* Filter Header */
.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f1f5f9;
}

.filter-title h3 {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 4px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* Removed search icon */

.filter-title p {
  font-size: 14px;
  color: #64748b;
  margin: 0;
  font-weight: 400;
}

.filter-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.btn-clear-filters {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #f8fafc;
  color: #64748b;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.btn-clear-filters:hover {
  background: #f1f5f9;
  color: #374151;
  border-color: #cbd5e1;
  transform: translateY(-1px);
}

/* Removed clear icon */

.filter-row {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 20px;
  position: relative;
}

.filter-row::after {
  content: '';
  position: absolute;
  bottom: -12px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 3px;
  background: linear-gradient(90deg, #f97316, #ea580c);
  border-radius: 2px;
  opacity: 0.6;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  position: relative;
  transition: all 0.3s ease;
}

.filter-group:hover {
  transform: translateY(-2px);
}

.filter-group:hover label {
  color: #f97316;
}

.filter-group label {
  font-weight: 600;
  font-size: 14px;
  color: #374151;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.filter-group label::before {
  content: '';
  width: 3px;
  height: 16px;
  background: linear-gradient(135deg, #f97316, #ea580c);
  border-radius: 2px;
}

.search-input,
.filter-select {
  padding: 14px 18px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  background: #fafbfc;
  font-size: 14px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: #1f2937;
  font-weight: 500;
  position: relative;
}

.search-input:focus,
.filter-select:focus {
  outline: none;
  border-color: #f97316;
  background: white;
  box-shadow: 0 0 0 4px rgba(249, 115, 22, 0.1), 0 4px 12px rgba(0, 0, 0, 0.05);
  transform: translateY(-1px);
}

.search-input::placeholder {
  color: #9ca3af;
  font-weight: 400;
}

.filter-select {
  cursor: pointer;
  appearance: none;
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 20 20'%3e%3cpath stroke='%236b7280' stroke-linecap='round' stroke-linejoin='round' stroke-width='1.5' d='m6 8 4 4 4-4'/%3e%3c/svg%3e");
  background-position: right 12px center;
  background-repeat: no-repeat;
  background-size: 16px;
  padding-right: 40px;
}

.main-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  margin-bottom: 24px;
}

/* Table Header Section */
.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  margin-top: 0;
}

.table-title {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.table-title h2 {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.item-count {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

.table-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.items-per-page {
  display: flex;
  align-items: center;
  gap: 8px;
}

.items-per-page label {
  font-size: 14px;
  color: #374151;
  font-weight: 500;
}

.page-size-select {
  padding: 6px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: white;
  font-size: 14px;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s ease;
}

.page-size-select:focus {
  outline: none;
  border-color: #f97316;
  box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.1);
}

.table-container {
  overflow-x: auto;
  width: 100%;
}

table {
  width: 100%;
  min-width: 1400px;
  border-collapse: collapse;
  background: white;
  table-layout: fixed;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
}

th,
td {
  border-bottom: 1px solid #e2e8f0;
  border-right: 1px solid #e2e8f0;
  padding: 16px 20px;
  text-align: left;
  font-size: 14px;
  vertical-align: middle;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

th:last-child,
td:last-child {
  border-right: none;
}

th {
  background: #f8fafc;
  font-weight: 600;
  color: #374151;
  position: sticky;
  top: 0;
  z-index: 10;
  border-top: 1px solid #e2e8f0;
  border-bottom: 2px solid #d1d5db;
}

thead tr {
  border-bottom: 2px solid #d1d5db;
}

/* Column Widths - Balanced layout */
.checkbox-column {
  width: 50px;
}

th:nth-child(2) { 
  width: 80px; 
  text-align: center;
  padding: 8px 4px;
} /* STT */
th:nth-child(3) { width: 200px; } /* Mã Phiếu */
th:nth-child(4) { width: 200px; } /* Tên Phiếu */
th:nth-child(5) { width: 160px; } /* Loại */
th:nth-child(6) { width: 180px; } /* Giá Trị */
th:nth-child(7) { width: 140px; } /* Số Lượng */
th:nth-child(8) { width: 120px; } /* Ngày Bắt Đầu */
th:nth-child(9) { width: 120px; } /* Ngày Kết Thúc */
th:nth-child(10) { width: 140px; } /* Trạng Thái */
th:nth-child(11) { width: 100px; } /* Thao Tác */

/* Special border for action column */
th:nth-child(11),
td:nth-child(11) {
  border-left: 2px solid #d1d5db;
}

/* Special handling for long content columns */
th:nth-child(3),
td:nth-child(3) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px;
}

th:nth-child(4),
td:nth-child(4) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px;
}

th:nth-child(2),
td:nth-child(2) {
  white-space: nowrap;
  overflow: visible;
  text-align: center;
  padding: 8px 4px;
  font-weight: 600;
}

th:nth-child(5),
td:nth-child(5) {
  white-space: nowrap;
  overflow: visible;
  text-align: center;
  padding: 8px 12px;
}

th:nth-child(6),
td:nth-child(6) {
  white-space: normal;
  word-wrap: break-word;
  line-height: 1.4;
  max-width: 180px;
}

tbody tr {
  border-bottom: 1px solid #e2e8f0;
}

tbody tr:hover {
  background: #f8fafc;
}

tbody tr:nth-child(even) {
  background: #fafbfc;
}

tbody tr:nth-child(even):hover {
  background: #f1f5f9;
}

tbody tr:last-child {
  border-bottom: none;
}

/* Checkbox Styles */
.checkbox-column {
  width: 60px;
  text-align: center;
  padding: 16px 12px !important;
  border-right: 2px solid #d1d5db !important;
}

.select-all-checkbox,
.row-checkbox {
  width: 16px;
  height: 16px;
  cursor: pointer;
  accent-color: #f97316;
}

/* Status Badge Styles */
.status-badge-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  min-width: 80px;
  text-align: center;
}

.badge-active {
  background: #dcfce7;
  color: #166534;
}

.badge-inactive {
  background: #fef2f2;
  color: #dc2626;
}

.badge-expired {
  background: #fef2f2;
  color: #dc2626;
}

.badge-pending {
  background: #fef3c7;
  color: #d97706;
}

.badge-out-of-stock {
  background: #f3f4f6;
  color: #6b7280;
}

.toggle-container {
  display: flex;
  justify-content: center;
}


/* Toggle Switch Styles */
.toggle-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.toggle-switch {
  position: relative;
  display: inline-block;
  width: 50px;
  height: 24px;
}

.toggle-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #cbd5e1;
  transition: 0.3s;
  border-radius: 24px;
}

.toggle-slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: 0.3s;
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

input:checked + .toggle-slider {
  background-color: #f97316;
}

input:checked + .toggle-slider:before {
  transform: translateX(26px);
}

.toggle-switch.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.toggle-label {
  font-size: 12px;
  font-weight: 500;
  min-width: 80px;
}

.toggle-label.active {
  color: #059669;
}

.toggle-label.inactive {
  color: #6b7280;
}

.toggle-label.expired-text {
  color: #dc2626;
}

.toggle-label.not-started {
  color: #f59e0b;
}

.toggle-label.out-of-stock {
  color: #dc2626;
}

/* Action Buttons */
.action-buttons {
  display: flex;
  gap: 6px;
  align-items: center;
  justify-content: center;
}

.btn-view,
.btn-edit {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  font-size: 14px;
}

.btn-view {
  background: #e0f2fe;
  color: #0277bd;
}

.btn-view:hover {
  background: #b3e5fc;
  transform: translateY(-1px);
}

.btn-edit {
  background: #fff3e0;
  color: #f57c00;
}

.btn-edit:hover {
  background: #ffe0b2;
  transform: translateY(-1px);
}


/* Voucher Type Styles */
.voucher-type {
  display: flex;
  align-items: center;
}

.type-percent,
.type-fixed {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.type-percent {
  background: #dcfce7;
  color: #166534;
}

.type-fixed {
  background: #fef3c7;
  color: #92400e;
}

/* Discount Value Styles */
.discount-value {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.discount-value .value {
  font-weight: 600;
  color: #1e293b;
  font-size: 14px;
}

.discount-value .max-discount,
.discount-value .min-order {
  font-size: 11px;
  color: #64748b;
  line-height: 1.2;
}

/* Quantity Display Styles */
.quantity-display {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.quantity-number {
  font-weight: 600;
  color: #1e293b;
  font-size: 14px;
}

.quantity-type {
  font-size: 11px;
  font-weight: 500;
  padding: 2px 6px;
  border-radius: 4px;
  text-align: center;
  width: fit-content;
}

.quantity-type.private {
  background: #fef3c7;
  color: #92400e;
}

.quantity-type.public {
  background: #dcfce7;
  color: #166534;
}

/* Pagination Styles */
.pagination-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
  border-radius: 0 0 12px 12px;
}

.pagination-info {
  color: #64748b;
  font-size: 14px;
  font-weight: 500;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: white;
  color: #374151;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.pagination-btn:hover:not(:disabled) {
  background: #f97316;
  color: white;
  border-color: #f97316;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(249, 115, 22, 0.3);
}

.pagination-btn:disabled {
  background: #f3f4f6;
  color: #9ca3af;
  cursor: not-allowed;
  border-color: #e5e7eb;
}

.pagination-numbers {
  display: flex;
  gap: 4px;
}

.pagination-number {
  min-width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  color: #374151;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.pagination-number:hover:not(:disabled):not(.ellipsis) {
  background: #f97316;
  color: white;
  border-color: #f97316;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(249, 115, 22, 0.3);
}

.pagination-number.active {
  background: #f97316;
  color: white;
  border-color: #f97316;
  box-shadow: 0 2px 4px rgba(249, 115, 22, 0.3);
}

.pagination-number.ellipsis {
  background: transparent;
  border: none;
  cursor: default;
  color: #9ca3af;
}

.pagination-number:disabled {
  cursor: not-allowed;
}

.icon-chevron-left::before {
  content: "‹";
  font-size: 16px;
  font-weight: bold;
}

.icon-chevron-right::before {
  content: "›";
  font-size: 16px;
  font-weight: bold;
}

/* Responsive design */
@media (max-width: 768px) {
  .content {
    padding: 16px;
  }
  
  .filter-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .filter-actions {
    justify-content: flex-end;
  }
  
  .filter-row {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .header h1 {
    font-size: 1.5rem;
  }
  
  .table-container {
    font-size: 12px;
  }
  
  th, td {
    padding: 12px 8px;
  }
  
  .discount-value .value {
    font-size: 13px;
  }
  
  .type-percent,
  .type-fixed {
    padding: 3px 6px;
    font-size: 10px;
  }
  
  .action-buttons {
    gap: 4px;
  }
  
  .btn-edit {
    width: 32px;
    height: 32px;
    font-size: 14px;
  }
  
  .status-badge-container {
    gap: 4px;
  }
  
  .status-badge {
    font-size: 10px;
    padding: 2px 8px;
    min-width: 60px;
  }
  
  .checkbox-column {
    width: 40px;
    padding: 8px 4px !important;
  }
  
  .table-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .table-actions {
    justify-content: space-between;
  }
  
  .pagination-section {
    flex-direction: column;
    gap: 16px;
    align-items: center;
  }
  
  .pagination-controls {
    flex-wrap: wrap;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .content {
    padding: 12px;
  }
  
  .filter-section {
    padding: 16px;
  }
  
  .table-container {
    font-size: 11px;
  }
  
  th, td {
    padding: 8px 4px;
  }
  
  .pagination-container {
    padding: 16px;
  }
  
  .pagination-btn {
    padding: 6px 12px;
    font-size: 12px;
  }
  
  .pagination-number {
    min-width: 32px;
    height: 32px;
    font-size: 12px;
  }
  
  .toggle-container {
    flex-direction: column;
    gap: 4px;
    align-items: flex-start;
  }
  
  .toggle-label {
    font-size: 10px;
    min-width: auto;
  }
}
</style
