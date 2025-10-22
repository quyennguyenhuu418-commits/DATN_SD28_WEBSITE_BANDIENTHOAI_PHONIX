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
          <p>Tìm kiếm và lọc giao ca theo tiêu chí</p>
        </div>
        <div class="filter-actions">
          <button class="btn-show-inactive" @click="showPendingGiaoCa">
            Xem giao ca chờ xác nhận
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
            placeholder="Tìm theo mã giao ca, nhân viên..."
            @input="applyFilters"
            class="search-input"
          />
        </div>
        <div class="filter-group">
          <label>Trạng thái:</label>
          <select v-model="statusFilter" @change="applyFilters" class="filter-select">
            <option value="">Tất cả trạng thái</option>
            <option value="0">Chờ xác nhận</option>
            <option value="1">Đã xác nhận</option>
            <option value="2">Đã hủy</option>
          </select>
        </div>
        <div class="filter-group">
          <label>Ca làm việc:</label>
          <select v-model="caFilter" @change="applyFilters" class="filter-select">
            <option value="">Tất cả ca</option>
            <option v-for="ca in caList" :key="ca.id" :value="ca.id">{{ ca.tenCa }}</option>
          </select>
        </div>
        <div class="filter-group">
          <label>Nhân viên:</label>
          <select v-model="nhanVienFilter" @change="applyFilters" class="filter-select">
            <option value="">Tất cả nhân viên</option>
            <option v-for="nv in nhanVienList" :key="nv.id" :value="nv.id">{{ nv.hoTen }}</option>
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

    <!-- Add Giao Ca Button -->
    <div class="add-voucher-section">
      <button @click="exportToExcel" class="btn-export-excel">
        <font-awesome-icon :icon="['fas', 'file-excel']" />
        Xuất Excel
      </button>
      <button class="btn-add-voucher" @click="addGiaoCa()">
        <font-awesome-icon :icon="['fas', 'plus-circle']" />
        Tạo Giao Ca Mới
      </button>
    </div>

    <!-- Main Content Area -->
    <div class="main-content">
      <!-- Table Header Section -->
      <div class="table-header">
        <div class="table-title">
          <h2>Danh sách Giao Ca</h2>
          <span class="item-count">{{ totalItems }} giao ca</span>
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
                <input 
                  type="checkbox" 
                  class="select-all-checkbox" 
                  :checked="isAllSelected"
                  @change="toggleSelectAll"
                />
              </th>
              <th>STT</th>
              <th>Mã Giao Ca</th>
              <th>Ca Làm Việc</th>
              <th>Nhân Viên Giao</th>
              <th>Nhân Viên Nhận</th>
              <th>Ngày Giao</th>
              <th>Doanh Thu</th>
              <th>Tiền Mặt</th>
              <th>Trạng Thái</th>
              <th>Thao Tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(giaoCa, index) in filteredGiaoCa" :key="giaoCa.id">
              <td class="checkbox-column">
                <input 
                  type="checkbox" 
                  class="row-checkbox" 
                  :value="giaoCa.id"
                  :checked="selectedGiaoCaIds.includes(giaoCa.id)"
                  @change="toggleSelectGiaoCa(giaoCa.id)"
                />
              </td>
              <td>{{ startItem + index }}</td>
              <td>{{ giaoCa.maGiaoCa }}</td>
              <td>
                <div class="ca-info">
                  <span class="ca-name">{{ giaoCa.caTen }}</span>
                  <span class="ca-date">{{ formatDate(giaoCa.ngayLamViec) }}</span>
                </div>
              </td>
              <td>{{ giaoCa.nhanVienGiaoTen }}</td>
              <td>{{ giaoCa.nhanVienNhanTen || 'Chờ xác nhận' }}</td>
              <td>{{ formatDateTime(giaoCa.ngayGiaoCa) }}</td>
              <td>
                <div class="revenue-info">
                  <span class="revenue-amount">{{ formatCurrency(giaoCa.tongDoanhThu) }}</span>
                  <span class="order-count">{{ giaoCa.soDonHang }} đơn</span>
                </div>
              </td>
              <td>
                <div class="money-info">
                  <span class="money-amount">{{ formatCurrency(giaoCa.soTienCuoiCa) }}</span>
                  <span v-if="giaoCa.chenhLech !== 0" 
                        :class="giaoCa.chenhLech > 0 ? 'chenh-lech-positive' : 'chenh-lech-negative'"
                        class="chenh-lech">
                    {{ giaoCa.chenhLech > 0 ? '+' : '' }}{{ formatCurrency(giaoCa.chenhLech) }}
                  </span>
                </div>
              </td>
              <td>
                <div class="status-badge-container">
                  <span class="status-badge" :class="getStatusBadgeClass(giaoCa)">
                    {{ getStatusText(giaoCa) }}
                  </span>
                </div>
              </td>
              <td>
                <div class="action-buttons">
                  <div class="button-row">
                    <button class="btn-view" @click="viewGiaoCa(giaoCa)" title="Xem chi tiết">
                      <font-awesome-icon icon="eye" />
                    </button>
                    <button v-if="giaoCa.trangThai === 0" class="btn-edit" @click="confirmGiaoCa(giaoCa)" title="Xác nhận">
                      <font-awesome-icon icon="check" />
                    </button>
                    <button v-if="giaoCa.trangThai === 0" class="btn-cancel" @click="cancelGiaoCa(giaoCa)" title="Hủy">
                      <font-awesome-icon icon="times" />
                    </button>
                  </div>
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
import * as XLSX from 'xlsx'

const router = useRouter()

interface GiaoCa {
  id: number
  maGiaoCa: string
  phanCaId: number
  nhanVienGiaoId: number
  nhanVienNhanId?: number
  ngayGiaoCa: string
  soTienDauCa: number
  soTienCuoiCa: number
  soTienThuThem: number
  soTienChiRa: number
  chenhLech: number
  tongDoanhThu: number
  soDonHang: number
  soDonHangThanhToanTienMat: number
  soDonHangThanhToanChuyenKhoan: number
  trangThai: number
  thoiGianXacNhan?: string
  ghiChu?: string
  baoCaoCongViec?: string
  suCoBatThuong?: string
  congViecTonDong?: string
  // Additional fields for display
  caTen?: string
  nhanVienGiaoTen?: string
  nhanVienNhanTen?: string
  ngayLamViec?: string
}

interface Ca {
  id: number
  tenCa: string
  gioBatDau: string
  gioKetThuc: string
}

interface NhanVien {
  id: number
  hoTen: string
  maNhanVien: string
}

const giaoCaList = ref<GiaoCa[]>([])
const caList = ref<Ca[]>([])
const nhanVienList = ref<NhanVien[]>([])
const selectedGiaoCa = ref<GiaoCa[]>([])
const selectedGiaoCaIds = ref<number[]>([])
const loading = ref(false)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

// Filter states
const statusFilter = ref('')
const caFilter = ref('')
const nhanVienFilter = ref('')
const startDateFilter = ref('')
const endDateFilter = ref('')
const searchText = ref('')

// Pagination states
const currentPage = ref(1)
const itemsPerPage = ref(5)
const totalItems = ref(0)

// Computed property for filtered giao ca (without pagination)
const allFilteredGiaoCa = computed(() => {
  let filtered = giaoCaList.value

  // Filter by status
  if (statusFilter.value !== '') {
    filtered = filtered.filter(g => g.trangThai.toString() === statusFilter.value)
  }

  // Filter by ca
  if (caFilter.value !== '') {
    filtered = filtered.filter(g => g.phanCaId.toString() === caFilter.value)
  }

  // Filter by nhan vien
  if (nhanVienFilter.value !== '') {
    filtered = filtered.filter(g => 
      g.nhanVienGiaoId.toString() === nhanVienFilter.value ||
      g.nhanVienNhanId?.toString() === nhanVienFilter.value
    )
  }

  // Filter by date range
  if (startDateFilter.value !== '') {
    const startDate = new Date(startDateFilter.value)
    filtered = filtered.filter(g => new Date(g.ngayGiaoCa) >= startDate)
  }

  if (endDateFilter.value !== '') {
    const endDate = new Date(endDateFilter.value)
    filtered = filtered.filter(g => new Date(g.ngayGiaoCa) <= endDate)
  }

  // Search filter
  if (searchText.value.trim() !== '') {
    const search = searchText.value.toLowerCase()
    filtered = filtered.filter(g => 
      g.maGiaoCa.toLowerCase().includes(search) ||
      g.nhanVienGiaoTen?.toLowerCase().includes(search) ||
      g.nhanVienNhanTen?.toLowerCase().includes(search) ||
      g.caTen?.toLowerCase().includes(search)
    )
  }

  return filtered
})

// Computed property for paginated giao ca
const filteredGiaoCa = computed(() => {
  const filtered = allFilteredGiaoCa.value
  totalItems.value = filtered.length
  
  const startIndex = (currentPage.value - 1) * itemsPerPage.value
  const endIndex = startIndex + itemsPerPage.value
  
  return filtered.slice(startIndex, endIndex)
})

// Computed property for select all checkbox
const isAllSelected = computed(() => {
  return filteredGiaoCa.value.length > 0 && 
         filteredGiaoCa.value.every(giaoCa => selectedGiaoCaIds.value.includes(giaoCa.id))
})

// Computed property for selected giao ca
const selectedGiaoCaComputed = computed(() => {
  return giaoCaList.value.filter(giaoCa => selectedGiaoCaIds.value.includes(giaoCa.id))
})

// Computed properties for pagination
const totalPages = computed(() => Math.ceil(totalItems.value / itemsPerPage.value))
const startItem = computed(() => (currentPage.value - 1) * itemsPerPage.value + 1)
const endItem = computed(() => Math.min(currentPage.value * itemsPerPage.value, totalItems.value))

async function loadGiaoCa() {
  loading.value = true
  try {
    console.log('Loading giao ca from /api/giao-ca...')
    const { data } = await api.get<GiaoCa[]>('/api/giao-ca')
    console.log('Loaded giao ca:', data)
    
    giaoCaList.value = data
  } catch (error) {
    console.error('Lỗi khi tải danh sách giao ca:', error)
    toastRef.value?.error('Lỗi', 'Không thể tải danh sách giao ca: ' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

async function loadCaList() {
  try {
    const { data } = await api.get<Ca[]>('/api/ca/active')
    caList.value = data
  } catch (error) {
    console.error('Lỗi khi tải danh sách ca:', error)
  }
}

async function loadNhanVienList() {
  try {
    const { data } = await api.get<NhanVien[]>('/api/nhan-vien')
    nhanVienList.value = data
  } catch (error) {
    console.error('Lỗi khi tải danh sách nhân viên:', error)
  }
}

function addGiaoCa() {
  router.push('/giao-ca/create')
}

function viewGiaoCa(giaoCa: GiaoCa) {
  router.push(`/giao-ca/detail/${giaoCa.id}`)
}

async function confirmGiaoCa(giaoCa: GiaoCa) {
  try {
    await api.put(`/api/giao-ca/${giaoCa.id}/confirm?nhanVienNhanId=${giaoCa.nhanVienGiaoId}`, {
      soTienCuoiCa: giaoCa.soTienCuoiCa,
      tongDoanhThu: giaoCa.tongDoanhThu,
      soDonHang: giaoCa.soDonHang,
      baoCaoCongViec: giaoCa.baoCaoCongViec
    })
    
    giaoCa.trangThai = 1
    toastRef.value?.success('Thành công', 'Đã xác nhận giao ca!')
  } catch (error: any) {
    console.error('Lỗi khi xác nhận giao ca:', error)
    toastRef.value?.error('Lỗi', 'Không thể xác nhận giao ca')
  }
}

async function cancelGiaoCa(giaoCa: GiaoCa) {
  if (confirm('Bạn có chắc chắn muốn hủy giao ca này?')) {
    try {
      await api.put(`/api/giao-ca/${giaoCa.id}/cancel?lyDo=Hủy bởi quản lý`)
      
      giaoCa.trangThai = 2
      toastRef.value?.success('Thành công', 'Đã hủy giao ca!')
    } catch (error: any) {
      console.error('Lỗi khi hủy giao ca:', error)
      toastRef.value?.error('Lỗi', 'Không thể hủy giao ca')
    }
  }
}

function applyFilters() {
  // Reset to first page when filters change
  currentPage.value = 1
}

function clearAllFilters() {
  searchText.value = ''
  statusFilter.value = ''
  caFilter.value = ''
  nhanVienFilter.value = ''
  startDateFilter.value = ''
  endDateFilter.value = ''
  currentPage.value = 1
  toastRef.value?.info('Thông báo', 'Đã xóa tất cả bộ lọc')
}

function showPendingGiaoCa() {
  statusFilter.value = '0'
  currentPage.value = 1
  toastRef.value?.info('Thông báo', 'Đang hiển thị giao ca chờ xác nhận')
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
  
  if (total <= 5) {
    for (let i = 1; i <= total; i++) {
      pages.push(i)
    }
  } else {
    if (current <= 3) {
      for (let i = 1; i <= 3; i++) {
        pages.push(i)
      }
      pages.push('...')
      pages.push(total)
    } else if (current >= total - 2) {
      pages.push(1)
      pages.push('...')
      for (let i = total - 2; i <= total; i++) {
        pages.push(i)
      }
    } else {
      pages.push(1)
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

function formatDateTime(dateTime: string): string {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('vi-VN')
}

function formatCurrency(amount: number): string {
  if (!amount) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', { 
    style: 'currency', 
    currency: 'VND' 
  }).format(amount)
}

function getStatusText(giaoCa: GiaoCa): string {
  switch (giaoCa.trangThai) {
    case 0: return 'Chờ xác nhận'
    case 1: return 'Đã xác nhận'
    case 2: return 'Đã hủy'
    default: return 'Không xác định'
  }
}

function getStatusBadgeClass(giaoCa: GiaoCa): string {
  switch (giaoCa.trangThai) {
    case 0: return 'badge-pending'
    case 1: return 'badge-active'
    case 2: return 'badge-cancelled'
    default: return 'badge-unknown'
  }
}

// Export to Excel function
function exportToExcel() {
  try {
    const giaoCaToExport = selectedGiaoCaComputed.value.length > 0 ? selectedGiaoCaComputed.value : giaoCaList.value
    
    if (giaoCaToExport.length === 0) {
      toastRef.value?.warning('Cảnh báo', 'Không có dữ liệu giao ca để xuất Excel')
      return
    }
    
    const excelData = giaoCaToExport.map((giaoCa, index) => ({
      'STT': index + 1,
      'Mã giao ca': giaoCa.maGiaoCa,
      'Ca làm việc': giaoCa.caTen,
      'Nhân viên giao': giaoCa.nhanVienGiaoTen,
      'Nhân viên nhận': giaoCa.nhanVienNhanTen || 'Chờ xác nhận',
      'Ngày giao': formatDateTime(giaoCa.ngayGiaoCa),
      'Doanh thu': formatCurrency(giaoCa.tongDoanhThu),
      'Số đơn hàng': giaoCa.soDonHang,
      'Tiền mặt cuối ca': formatCurrency(giaoCa.soTienCuoiCa),
      'Chênh lệch': formatCurrency(giaoCa.chenhLech),
      'Trạng thái': getStatusText(giaoCa)
    }))
    
    const ws = XLSX.utils.json_to_sheet(excelData)
    const wb = XLSX.utils.book_new()
    
    const colWidths = []
    const headers = Object.keys(excelData[0])
    
    headers.forEach((header, colIndex) => {
      let maxLength = header.length
      
      excelData.forEach(row => {
        const cellValue = String(row[header] || '')
        if (cellValue.length > maxLength) {
          maxLength = cellValue.length
        }
      })
      
      const width = Math.min(Math.max(maxLength + 2, 10), 50)
      colWidths.push({ wch: width })
    })
    
    ws['!cols'] = colWidths
    XLSX.utils.book_append_sheet(wb, ws, 'Danh sách giao ca')
    
    const now = new Date()
    const dateStr = now.toISOString().split('T')[0]
    const filename = `danh_sach_giao_ca_${dateStr}.xlsx`
    
    XLSX.writeFile(wb, filename)
    
    toastRef.value?.success('Thành công', `Đã xuất ${giaoCaToExport.length} giao ca ra file Excel`)
    
  } catch (error) {
    console.error('Error exporting to Excel:', error)
    toastRef.value?.error('Lỗi', 'Không thể xuất file Excel')
  }
}

// Checkbox functions
function toggleSelectAll() {
  if (isAllSelected.value) {
    const currentPageIds = filteredGiaoCa.value.map(g => g.id)
    selectedGiaoCaIds.value = selectedGiaoCaIds.value.filter(id => !currentPageIds.includes(id))
  } else {
    const currentPageIds = filteredGiaoCa.value.map(g => g.id)
    const newIds = currentPageIds.filter(id => !selectedGiaoCaIds.value.includes(id))
    selectedGiaoCaIds.value = [...selectedGiaoCaIds.value, ...newIds]
  }
}

function toggleSelectGiaoCa(giaoCaId: number) {
  const index = selectedGiaoCaIds.value.indexOf(giaoCaId)
  if (index > -1) {
    selectedGiaoCaIds.value.splice(index, 1)
  } else {
    selectedGiaoCaIds.value.push(giaoCaId)
  }
}

onMounted(() => {
  loadGiaoCa()
  loadCaList()
  loadNhanVienList()
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
  max-width: 100%;
  margin: 0;
  overflow-x: hidden;
  box-sizing: border-box;
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
  gap: 12px;
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
  justify-content: flex-end;
}

.items-per-page {
  display: flex;
  align-items: center;
  gap: 8px;
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

.btn-add-voucher font-awesome-icon {
  font-size: 16px;
  color: #f97316;
  margin-right: 8px;
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
  max-width: 100%;
}

table {
  width: 100%;
  min-width: 1000px;
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
  padding: 12px 16px;
  text-align: left;
  font-size: 14px;
  vertical-align: top;
  overflow: hidden;
  white-space: normal;
  word-wrap: break-word;
  word-break: break-word;
  hyphens: auto;
  box-sizing: border-box;
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

/* Column Widths - Responsive layout */
.checkbox-column {
  width: 40px;
  min-width: 40px;
  max-width: 40px;
  overflow: hidden;
}

th:nth-child(2) { 
  width: 50px; 
  min-width: 50px;
  max-width: 50px;
  text-align: center;
  padding: 8px 4px;
  overflow: hidden;
} /* STT */
th:nth-child(3) { 
  width: 120px; 
  min-width: 120px; 
  max-width: 120px;
  overflow: hidden;
} /* Mã Giao Ca */
th:nth-child(4) { 
  width: 150px; 
  min-width: 150px; 
  max-width: 150px;
  overflow: hidden;
} /* Ca Làm Việc */
th:nth-child(5) { 
  width: 120px; 
  min-width: 120px; 
  max-width: 120px;
  overflow: hidden;
  text-align: center;
} /* Nhân Viên Giao */
th:nth-child(6) { 
  width: 120px; 
  min-width: 120px; 
  max-width: 120px;
  overflow: hidden;
} /* Nhân Viên Nhận */
th:nth-child(7) { 
  width: 90px; 
  min-width: 90px; 
  max-width: 90px;
  overflow: hidden;
} /* Ngày Giao */
th:nth-child(8) { 
  width: 120px; 
  min-width: 120px; 
  max-width: 120px;
  white-space: normal;
  word-wrap: break-word;
} /* Doanh Thu */
th:nth-child(9) { 
  width: 120px; 
  min-width: 120px; 
  max-width: 120px;
  white-space: normal;
  word-wrap: break-word;
} /* Tiền Mặt */
th:nth-child(10) { 
  width: 100px; 
  min-width: 100px; 
  white-space: normal;
} /* Trạng Thái */
th:nth-child(11) { 
  width: 100px; 
  min-width: 100px; 
  max-width: 100px;
  white-space: normal;
  word-wrap: break-word;
  word-break: break-word;
  hyphens: auto;
  overflow-wrap: break-word;
  overflow: hidden;
} /* Thao Tác */

/* Special border for action column */
th:nth-child(11),
td:nth-child(11) {
  border-left: 2px solid #d1d5db;
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
  justify-content: center;
  align-items: center;
  width: 100%;
  white-space: normal;
  word-wrap: break-word;
  word-break: break-word;
  hyphens: auto;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  min-width: 60px;
  text-align: center;
  white-space: normal;
  word-wrap: break-word;
  word-break: break-word;
  hyphens: auto;
  line-height: 1.2;
}

.badge-pending {
  background: #fef3c7;
  color: #d97706;
}

.badge-active {
  background: #dcfce7;
  color: #166534;
}

.badge-cancelled {
  background: #fef2f2;
  color: #dc2626;
}

.badge-unknown {
  background: #f3f4f6;
  color: #6b7280;
}

/* Action Buttons */
.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
  justify-content: center;
  width: 100%;
  white-space: normal;
  word-wrap: break-word;
  word-break: break-word;
  hyphens: auto;
}

.button-row {
  display: flex;
  gap: 6px;
  align-items: center;
  justify-content: center;
}

.btn-view,
.btn-edit,
.btn-cancel {
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  font-size: 12px;
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
  background: #dcfce7;
  color: #059669;
}

.btn-edit:hover {
  background: #bbf7d0;
  transform: translateY(-1px);
}

.btn-cancel {
  background: #fef2f2;
  color: #dc2626;
}

.btn-cancel:hover {
  background: #fecaca;
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

/* Additional styles for Giao Ca specific elements */
.ca-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.ca-name {
  font-weight: 600;
  color: #1e293b;
  font-size: 14px;
}

.ca-date {
  font-size: 12px;
  color: #64748b;
}

.revenue-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.revenue-amount {
  font-weight: 600;
  color: #059669;
  font-size: 14px;
}

.order-count {
  font-size: 12px;
  color: #64748b;
}

.money-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.money-amount {
  font-weight: 600;
  color: #1e293b;
  font-size: 14px;
}

.chenh-lech {
  font-size: 12px;
  font-weight: 500;
}

.chenh-lech-positive {
  color: #059669;
}

.chenh-lech-negative {
  color: #dc2626;
}

/* Responsive design */
@media (max-width: 1200px) {
  .filter-row {
    grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
    gap: 16px;
  }
  
  table {
    min-width: 900px;
  }
  
  th, td {
    padding: 10px 12px;
    font-size: 13px;
  }
}

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
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
    gap: 12px;
  }
  
  table {
    min-width: 800px;
  }
  
  th, td {
    padding: 8px 10px;
    font-size: 12px;
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

@media (max-width: 600px) {
  .filter-row {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .filter-group {
    width: 100%;
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
  
  .pagination-btn {
    padding: 6px 12px;
    font-size: 12px;
  }
  
  .pagination-number {
    min-width: 32px;
    height: 32px;
    font-size: 12px;
  }
}
</style>
