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
          <p>Tìm kiếm và lọc đợt giảm giá theo tiêu chí</p>
        </div>
        <div class="filter-actions">
          <button class="btn-show-inactive" @click="showInactivePromotions">
            Xem đợt giảm giá ngừng hoạt động
          </button>
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
            placeholder="Tìm theo tên hoặc mã đợt giảm giá..."
            @input="applyFilters"
            class="search-input"
          />
        </div>
        <div class="filter-group">
          <label>Trạng thái:</label>
          <select v-model="statusFilter" @change="applyFilters" class="filter-select">
            <option value="">Tất cả trạng thái</option>
            <option value="active">Đang hoạt động</option>
            <option value="pending">Chưa bắt đầu</option>
            <option value="inactive">Ngừng hoạt động (có thể kích hoạt lại)</option>
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

    <!-- Add Promotion Button -->
    <div class="add-promotion-section">
      <button @click="exportToExcel" class="btn-export-excel">
        <font-awesome-icon :icon="['fas', 'file-excel']" />
        Xuất Excel
      </button>
      <button class="btn-add-promotion" @click="addKhuyenMai()">
        <font-awesome-icon :icon="['fas', 'plus-circle']" />
        Thêm Đợt Giảm Giá
      </button>
    </div>


    <!-- Main Content Area -->
    <div class="main-content">
      <!-- Table Header Section -->
      <div class="table-header">
        <div class="table-title">
          <h2>Danh sách Đợt Giảm Giá</h2>
          <span class="item-count">{{ totalItems }} đợt giảm giá</span>
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
              <th>Mã Đợt</th>
              <th>Tên Đợt Giảm Giá</th>
            <th>Giá Trị Giảm</th>
            <th>SL Sản Phẩm</th>
            <th>Ngày Bắt Đầu</th>
            <th>Ngày Kết Thúc</th>
            <th>Trạng Thái</th>
            <th>Thao Tác</th>
          </tr>
        </thead>
        <tbody>
            <tr v-for="(item, index) in filteredItems" :key="item.id">
              <td class="checkbox-column">
                <input type="checkbox" class="row-checkbox" :value="item.id" />
              </td>
              <td>{{ startItem + index }}</td>
              <td>{{ item.maKhuyenMai }}</td>
              <td>
                <div class="promotion-name">
                  <span class="name">{{ item.tenKhuyenMai }}</span>
                  <span v-if="item.moTa" class="description">{{ item.moTa }}</span>
                </div>
              </td>
            <td>
                <div class="discount-value">
                  <span class="value">{{ formatDiscountValue(item) }}</span>
                  <span v-if="item.giamToiDa && item.giamToiDa > 0" class="max-discount">
                    Tối đa: {{ formatCurrency(item.giamToiDa) }}
                  </span>
                </div>
              </td>
              <td>
                <div class="product-count">
                  <span v-if="item.soLuongSanPham === 0" class="count-label">Tất cả</span>
                  <span v-else class="count-number">{{ item.soLuongSanPham !== undefined ? item.soLuongSanPham : '-' }}</span>
                </div>
              </td>
              <td>{{ formatDate(item.ngayBatDau) }}</td>
              <td>{{ formatDate(item.ngayKetThuc) }}</td>
              <td>
                <div class="status-badge-container">
                  <span class="status-badge" :class="getStatusBadgeClass(item)">
                    {{ getStatusText(item) }}
              </span>
                  <div class="toggle-container">
                    <label class="toggle-switch" :class="{ 'disabled': isExpired(item) }">
                      <input
                        type="checkbox"
                        :checked="item.trangThai === 1"
                        :disabled="isExpired(item)"
                        @change="toggleStatus(item.id)"
                      />
                      <span class="toggle-slider"></span>
                    </label>
                  </div>
                </div>
            </td>
            <td>
                <div class="action-buttons">
                  <button class="btn-view" @click="viewKhuyenMai(item)" title="Xem chi tiết">
                    <font-awesome-icon icon="eye" />
              </button>
                  <button class="btn-edit" @click="editKhuyenMai(item)" title="Chỉnh sửa">
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
import { onMounted, ref, computed, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/services/api'
import Toast from '@/components/Toast.vue'
import PosHeader from '@/components/PosHeader.vue'
import * as XLSX from 'xlsx'

const router = useRouter()

interface KhuyenMai {
  id: number
  maKhuyenMai: string
  tenKhuyenMai: string
  moTa?: string
  phanTramGiam: number
  giamToiDa: number
  ngayBatDau: string
  ngayKetThuc: string
  trangThai: number
  ngayTao?: string
  ngayCapNhat?: string
  nguoiTao?: string
  nguoiCapNhat?: string
  soLuongSanPham?: number
}

const items = ref<KhuyenMai[]>([])
const loading = ref(false)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

// Filter states
const statusFilter = ref('')
const startDateFilter = ref('')
const endDateFilter = ref('')
const searchText = ref('')

// Pagination states
const currentPage = ref(1)
const itemsPerPage = ref(5)
const totalItems = ref(0)

// Computed property for filtered items (without pagination)
const allFilteredItems = computed(() => {
  let filtered = items.value

  // Filter by status (active/pending/inactive)
  if (statusFilter.value !== '') {
    const now = new Date()
    filtered = filtered.filter(v => {
      const startDate = new Date(v.ngayBatDau)
      const endDate = new Date(v.ngayKetThuc)

      if (statusFilter.value === 'active') {
        // Đang hoạt động: trangThai = 1 và trong khoảng thời gian
        return v.trangThai === 1 && now >= startDate && now <= endDate
      } else if (statusFilter.value === 'pending') {
        // Chưa bắt đầu: trangThai = 1 và chưa đến ngày bắt đầu
        return v.trangThai === 1 && now < startDate
      } else if (statusFilter.value === 'inactive') {
        // Vô hiệu: trangThai = 0 hoặc đã hết hạn
        return v.trangThai === 0 || now > endDate
      }
      return true
    })
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
      v.tenKhuyenMai.toLowerCase().includes(search) ||
      v.maKhuyenMai.toLowerCase().includes(search) ||
      (v.moTa && v.moTa.toLowerCase().includes(search))
    )
  }

  return filtered
})

// Computed property for paginated items
const filteredItems = computed(() => {
  const filtered = allFilteredItems.value
  totalItems.value = filtered.length

  const startIndex = (currentPage.value - 1) * itemsPerPage.value
  const endIndex = startIndex + itemsPerPage.value

  return filtered.slice(startIndex, endIndex)
})

// Computed properties for pagination
const totalPages = computed(() => Math.ceil(totalItems.value / itemsPerPage.value))
const startItem = computed(() => (currentPage.value - 1) * itemsPerPage.value + 1)
const endItem = computed(() => Math.min(currentPage.value * itemsPerPage.value, totalItems.value))

async function loadKhuyenMais() {
  loading.value = true
  try {
    console.log('Loading promotions from /api/khuyen-mai...')
    const { data } = await api.get<KhuyenMai[]>('/api/khuyen-mai')
    console.log('Loaded promotions:', data)

    // Tự động vô hiệu hóa đợt giảm giá hết hạn
    const updatedItems = []
    for (const item of data) {
      if (item.trangThai === 1 && isExpired(item)) {
        try {
          await api.post(`/api/khuyen-mai/${item.id}/toggle-status`)
          item.trangThai = 0
          console.log(`Tự động vô hiệu hóa đợt giảm giá hết hạn: ${item.tenKhuyenMai}`)
        } catch (error) {
          console.error('Lỗi khi tự động vô hiệu hóa đợt giảm giá:', error)
          item.trangThai = 0
        }
      }
      
      // Load số lượng sản phẩm áp dụng
      try {
        const countResponse = await api.get(`/api/khuyen-mai/${item.id}/san-pham/count`)
        item.soLuongSanPham = countResponse.data.count || 0
      } catch (error) {
        console.error(`Lỗi khi tải số lượng sản phẩm cho đợt giảm giá ${item.id}:`, error)
        item.soLuongSanPham = 0
      }
      
      updatedItems.push(item)
    }

    // Sắp xếp theo ngày tạo mới nhất (mới nhất lên đầu)
    updatedItems.sort((a, b) => {
      const dateA = new Date(a.ngayTao || a.ngayCapNhat || 0)
      const dateB = new Date(b.ngayTao || b.ngayCapNhat || 0)
      return dateB.getTime() - dateA.getTime()
    })

    items.value = updatedItems
  } catch (error) {
    console.error('Lỗi khi tải danh sách đợt giảm giá:', error)
    console.error('Error details:', error.response)
    console.error('Error status:', error.response?.status)
    console.error('Error data:', error.response?.data)
    toastRef.value?.error('Lỗi', 'Không thể tải danh sách đợt giảm giá: ' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

function addKhuyenMai() {
  router.push('/dot-giam-gia/form/new')
}

function editKhuyenMai(item: KhuyenMai) {
  router.push(`/dot-giam-gia/form/${item.id}`)
}

function viewKhuyenMai(item: KhuyenMai) {
  router.push(`/dot-giam-gia/detail/${item.id}`)
}

async function toggleStatus(id: number) {
  const item = items.value.find(v => v.id === id)
  if (!item) return

  if (isExpired(item)) {
    toastRef.value?.error('Không thể kích hoạt', 'Đợt giảm giá đã hết thời gian hiệu lực. Không thể kích hoạt lại.')
    return
  }

  if (item.trangThai === 0 && isNotStarted(item)) {
    toastRef.value?.warning('Chưa thể kích hoạt', 'Đợt giảm giá chưa đến thời gian hiệu lực.')
    return
  }

  try {
    await api.post(`/api/khuyen-mai/${id}/toggle-status`)

    const statusText = item.trangThai === 1 ? 'vô hiệu hóa' : 'kích hoạt'
    toastRef.value?.success('Thành công', `${statusText} đợt giảm giá thành công!`)
    await loadKhuyenMais()
  } catch (error: any) {
    console.error('Lỗi khi cập nhật trạng thái:', error)
    toastRef.value?.error('Lỗi', 'Không thể cập nhật trạng thái')
  }
}

function applyFilters() {
  currentPage.value = 1
}

function clearAllFilters() {
  searchText.value = ''
  statusFilter.value = ''
  startDateFilter.value = ''
  endDateFilter.value = ''
  currentPage.value = 1
  toastRef.value?.info('Thông báo', 'Đã xóa tất cả bộ lọc')
}

function showInactivePromotions() {
  statusFilter.value = 'inactive'
  currentPage.value = 1
  toastRef.value?.info('Thông báo', 'Đang hiển thị đợt giảm giá ngừng hoạt động. Gạt toggle để kích hoạt lại!')
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
    for (let i = 1; i <= total; i++) {
      pages.push(i)
    }
  } else {
    pages.push(1)

    if (current <= 4) {
      for (let i = 2; i <= 5; i++) {
        pages.push(i)
      }
      pages.push('...')
      pages.push(total)
    } else if (current >= total - 3) {
      pages.push('...')
      for (let i = total - 4; i <= total; i++) {
        pages.push(i)
      }
    } else {
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

function formatCurrency(amount: number): string {
  if (!amount) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}

function isExpired(item: KhuyenMai): boolean {
  const now = new Date()
  const endDate = new Date(item.ngayKetThuc)
  return now > endDate
}

function isNotStarted(item: KhuyenMai): boolean {
  const now = new Date()
  const startDate = new Date(item.ngayBatDau)
  return now < startDate
}

function getStatusText(item: KhuyenMai): string {
  const now = new Date()
  const startDate = new Date(item.ngayBatDau)
  const endDate = new Date(item.ngayKetThuc)

  if (item.trangThai === 0) return 'Vô hiệu'
  if (now < startDate) return 'Chưa bắt đầu'
  if (now > endDate) return 'Vô hiệu'
  return 'Đang hoạt động'
}

function getStatusBadgeClass(item: KhuyenMai): string {
  const now = new Date()
  const startDate = new Date(item.ngayBatDau)
  const endDate = new Date(item.ngayKetThuc)

  if (item.trangThai === 0) return 'badge-inactive'
  if (now < startDate) return 'badge-pending'
  if (now > endDate) return 'badge-inactive'
  return 'badge-active'
}

function formatDiscountValue(item: KhuyenMai): string {
  return `${item.phanTramGiam}%`
}

function isPercentPromotion(item: KhuyenMai): boolean {
  return true // Luôn là phần trăm
}

// Export to Excel function
function exportToExcel() {
  try {
    // Get all filtered data (without pagination)
    const dataToExport = allFilteredItems.value
    
    if (dataToExport.length === 0) {
      toastRef.value?.warning('Cảnh báo', 'Không có dữ liệu đợt giảm giá để xuất Excel')
      return
    }
    
    // Prepare data for Excel
    const excelData = dataToExport.map((item, index) => ({
      'STT': index + 1,
      'Mã đợt giảm giá': item.maKhuyenMai,
      'Tên đợt giảm giá': item.tenKhuyenMai,
      'Mô tả': item.moTa || '',
      'Phần trăm giảm (%)': item.phanTramGiam,
      'Số lượng sản phẩm': item.soLuongSanPham !== undefined ? (item.soLuongSanPham === 0 ? 'Tất cả' : item.soLuongSanPham) : '-',
      'Ngày bắt đầu': formatDate(item.ngayBatDau),
      'Ngày kết thúc': formatDate(item.ngayKetThuc),
      'Trạng thái': getStatusText(item),
      'Ngày tạo': item.ngayTao ? formatDate(item.ngayTao) : ''
    }))
    
    // Create Excel file
    const ws = XLSX.utils.json_to_sheet(excelData)
    const wb = XLSX.utils.book_new()
    
    // Điều chỉnh độ rộng cột tự động
    const colWidths = []
    const headers = Object.keys(excelData[0])
    
    // Tính toán độ rộng cho mỗi cột
    headers.forEach((header, colIndex) => {
      let maxLength = header.length
      
      // Kiểm tra độ dài của header
      if (header.length > maxLength) {
        maxLength = header.length
      }
      
      // Kiểm tra độ dài của dữ liệu trong cột
      excelData.forEach(row => {
        const cellValue = String(row[header] || '')
        if (cellValue.length > maxLength) {
          maxLength = cellValue.length
        }
      })
      
      // Đặt độ rộng tối thiểu và tối đa
      const width = Math.min(Math.max(maxLength + 2, 10), 50)
      colWidths.push({ wch: width })
    })
    
    // Áp dụng độ rộng cột
    ws['!cols'] = colWidths
    
    XLSX.utils.book_append_sheet(wb, ws, 'Danh sách đợt giảm giá')
    
    // Generate filename with current date
    const now = new Date()
    const dateStr = now.toISOString().split('T')[0]
    const filename = `danh_sach_dot_giam_gia_${dateStr}.xlsx`
    
    // Download file
    XLSX.writeFile(wb, filename)
    
    toastRef.value?.success('Thành công', `Đã xuất ${dataToExport.length} đợt giảm giá ra file Excel`)
    
  } catch (error) {
    console.error('Error exporting to Excel:', error)
    toastRef.value?.error('Lỗi', 'Không thể xuất file Excel')
  }
}

onMounted(loadKhuyenMais)

// Reload data when returning from form page
onActivated(() => {
  loadKhuyenMais()
})
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

/* Filter Section */
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

.btn-show-inactive {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #fef3c7;
  color: #d97706;
  border: 1px solid #fbbf24;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s ease;
  margin-right: 8px;
}

.btn-show-inactive:hover {
  background: #fde68a;
  color: #b45309;
  border-color: #f59e0b;
  transform: translateY(-1px);
}

.filter-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  position: relative;
  width: 100%;
  max-width: 100%;
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
}

.search-input:focus,
.filter-select:focus {
  outline: none !important;
  border-color: #f97316 !important;
  background: white;
  box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.2) !important;
  transform: translateY(-1px);
}

.search-input::placeholder {
  color: #9ca3af;
  font-weight: 400;
}

.filter-select {
  cursor: pointer;
  appearance: none;
}

/* Force orange focus for all filter selects */
.filter-group select:focus,
.filter-select:focus,
select.filter-select:focus {
  border-color: #f97316 !important;
  box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.2) !important;
  outline: none !important;
}

.filter-select {
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 20 20'%3e%3cpath stroke='%236b7280' stroke-linecap='round' stroke-linejoin='round' stroke-width='1.5' d='m6 8 4 4 4-4'/%3e%3c/svg%3e");
  background-position: right 12px center;
  background-repeat: no-repeat;
  background-size: 16px;
  padding-right: 40px;
}


/* Add Promotion Section */
.add-promotion-section {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-bottom: 24px;
  margin-top: 0;
}

.btn-add-promotion {
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

.btn-add-promotion:hover {
  background: #ea580c;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.4);
}

.btn-export-excel {
  padding: 10px 16px;
  background: #28a745;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: background-color 0.3s;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-export-excel:hover {
  background: #218838;
}

.btn-export-excel font-awesome-icon {
  font-size: 16px;
  color: #28a745;
  margin-right: 8px;
}

.btn-add-promotion font-awesome-icon {
  font-size: 16px;
  color: #f97316;
  margin-right: 8px;
}

/* Main Content */
.main-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  margin-bottom: 24px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
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
  min-width: 1200px; /* Reduced minimum width for better responsiveness */
  border-collapse: collapse;
  background: white;
  table-layout: auto; /* Changed from fixed to auto for better text wrapping */
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
  white-space: normal; /* Allow text wrapping */
  word-wrap: break-word; /* Break long words */
  word-break: break-word; /* Break long words */
  overflow-wrap: break-word; /* Modern property for word breaking */
  line-height: 1.4; /* Better line spacing */
}

/* Center align all columns */
td:nth-child(2), /* STT */
td:nth-child(3), /* Mã Đợt */
td:nth-child(4), /* Tên Đợt Giảm Giá */
td:nth-child(5), /* Giá Trị Giảm */
td:nth-child(6), /* SL Sản Phẩm */
td:nth-child(7), /* Ngày Bắt Đầu */
td:nth-child(8), /* Ngày Kết Thúc */
td:nth-child(9), /* Trạng Thái */
td:nth-child(10) { /* Thao Tác */
  text-align: center;
}

/* Compact padding for STT and Mã Đợt */
td:nth-child(2),
td:nth-child(3) {
  padding: 8px 4px;
}

/* Special handling for discount value column */
td:nth-child(5) { /* Giá Trị Giảm */
  white-space: normal;
  word-wrap: break-word;
  line-height: 1.4;
  max-width: 150px;
}

/* Special handling for product count column */
td:nth-child(6) { /* SL Sản Phẩm */
  white-space: normal;
  word-wrap: break-word;
  line-height: 1.4;
}

/* Ensure all cells can wrap text properly */
td {
  min-width: 0; /* Allow cells to shrink below content width */
  max-width: none; /* Remove any max-width constraints */
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

/* Column Widths - Flexible widths for better text wrapping */
.checkbox-column {
  width: 50px;
  text-align: center;
  min-width: 50px;
}

th:nth-child(2) {
  width: 60px;
  text-align: center;
  min-width: 60px;
}
th:nth-child(3) { 
  width: 150px; 
  text-align: center; 
  min-width: 120px;
  max-width: 200px;
} /* Mã Đợt - Allow wrapping */
th:nth-child(4) { 
  width: 300px; 
  text-align: center; 
  min-width: 200px;
} /* Tên Đợt Giảm Giá */
th:nth-child(5) { 
  width: 150px; 
  text-align: center; 
  min-width: 120px;
} /* Giá Trị Giảm */
th:nth-child(6) { 
  width: 120px; 
  text-align: center; 
  min-width: 100px;
} /* SL Sản Phẩm */
th:nth-child(7) { 
  width: 130px; 
  text-align: center; 
  min-width: 100px;
} /* Ngày Bắt Đầu */
th:nth-child(8) { 
  width: 130px; 
  text-align: center; 
  min-width: 100px;
} /* Ngày Kết Thúc */
th:nth-child(9) { 
  width: 140px; 
  text-align: center; 
  min-width: 120px;
} /* Trạng Thái */
th:nth-child(10) { 
  width: 120px; 
  text-align: center; 
  min-width: 100px;
} /* Thao Tác */

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

/* Promotion Name Styles */
.promotion-name {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.promotion-name .name {
  font-weight: 600;
  color: #1e293b;
}

.promotion-name .description {
  font-size: 12px;
  color: #64748b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Voucher Type Styles - Exact copy from VoucherPage */
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
  background: #fef3c7;
  color: #f97316;
}

.type-fixed {
  background: #fef3c7;
  color: #f97316;
}

/* Discount Value Styles - Exact copy from VoucherPage */
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

.max-discount {
  font-weight: 600;
  color: #1e293b;
}

/* Product Count Styles */
.product-count {
  display: flex;
  flex-direction: column;
  gap: 2px;
  align-items: center;
}

.product-count .count-number {
  font-weight: 700;
  color: #1e293b;
  font-size: 16px;
}

.product-count .count-label {
  font-size: 11px;
  color: #f97316;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.3px;
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

/* Toggle Switch Styles */
.toggle-container {
  display: flex;
  justify-content: center;
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

  .filter-row {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .status-radio-group {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .status-radio-group .radio-option {
    padding: 6px 0;
  }

  .table-container {
    font-size: 12px;
  }

  th, td {
    padding: 12px 8px;
  }

  .pagination-section {
    flex-direction: column;
    gap: 16px;
    align-items: center;
  }
  }

/* Responsive table adjustments for better text wrapping */
@media (max-width: 1366px) {
  table {
    min-width: 1000px; /* Further reduced for better mobile experience */
  }
  
  th:nth-child(3) { /* Mã Đợt */
    min-width: 100px;
    max-width: 150px;
  }
  
  th:nth-child(4) { /* Tên Đợt Giảm Giá */
    min-width: 150px;
  }
}

@media (max-width: 1200px) {
  table {
    min-width: 100%;
  }
  
  /* Make table more flexible on smaller screens */
  th, td {
    padding: 12px 16px; /* Reduce padding */
    font-size: 13px; /* Slightly smaller font */
  }
  
  th:nth-child(3) { /* Mã Đợt */
    min-width: 80px;
    max-width: 120px;
  }
}
</style>
