<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/services/api'
import Toast from '@/components/Toast.vue'
import * as XLSX from 'xlsx'

const router = useRouter()

interface HoaDon {
  id: number
  maHoaDon: string
  tenKhachHang?: string
  soDienThoai?: string
  loaiHoaDon: string
  tongTien: number
  tongTienSauGiam?: number
  trangThai: number
  ngayTao: string
  phieuGiamGiaId?: number
  ghiChu?: string
}

// Reactive data
const hoaDons = ref<HoaDon[]>([])
const loading = ref(false)
const toastRef = ref(null)

const currentPage = ref(1)
const pageSize = ref(5)
const totalPages = ref(0)
const totalElements = ref(0)

// Search and filter
const searchKeyword = ref('')
const showFilters = ref(false)
const filterTrangThai = ref('')
const filterLoaiHoaDon = ref('')
const dateFrom = ref('')
const dateTo = ref('')
const sortBy = ref('ngayTao')
const sortDirection = ref('desc')
const quickSort = ref('ngayTao_desc')

// Status options
const statusOptions = [
  { value: 0, label: 'Chờ xác nhận', color: '#ffc107' },
  { value: 1, label: 'Đã thanh toán chờ xác nhận', color: '#6f42c1' },
  { value: 2, label: 'Chờ giao hàng', color: '#17a2b8' },
  { value: 3, label: 'Đang giao', color: '#ff6b35' },
  { value: 4, label: 'Hoàn thành', color: '#28a745' },
  { value: 5, label: 'Đã hủy', color: '#dc3545' }
]

// Quick sort options for common use cases
const quickSortOptions = [
  { value: 'ngayTao_desc', label: 'Mới nhất', sortBy: 'ngayTao', direction: 'desc' },
  { value: 'ngayTao_asc', label: 'Cũ nhất', sortBy: 'ngayTao', direction: 'asc' },
  { value: 'tongTien_desc', label: 'Tổng tiền cao nhất', sortBy: 'tongTien', direction: 'desc' },
  { value: 'tongTien_asc', label: 'Tổng tiền thấp nhất', sortBy: 'tongTien', direction: 'asc' }
]

// Computed
const filteredHoaDons = computed(() => {
  let filtered = hoaDons.value

  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(hd =>
      hd.maHoaDon.toLowerCase().includes(keyword) ||
      (hd.tenKhachHang && hd.tenKhachHang.toLowerCase().includes(keyword)) ||
      (hd.soDienThoai && hd.soDienThoai.includes(keyword))
    )
  }

  if (filterTrangThai.value !== '') {
    filtered = filtered.filter(hd => hd.trangThai === parseInt(filterTrangThai.value))
  }

  if (filterLoaiHoaDon.value !== '') {
    filtered = filtered.filter(hd => hd.loaiHoaDon === filterLoaiHoaDon.value)
  }

  if (dateFrom.value) {
    filtered = filtered.filter(hd => new Date(hd.ngayTao) >= new Date(dateFrom.value))
  }

  if (dateTo.value) {
    filtered = filtered.filter(hd => new Date(hd.ngayTao) <= new Date(dateTo.value))
  }

  return filtered
})

// Methods
function handleStatusChange() {
  console.log('🔄 Status changed to:', filterTrangThai.value)
  currentPage.value = 1

  if (filterTrangThai.value !== '') {
    const statusName = statusOptions.find(s => s.value === parseInt(filterTrangThai.value))?.label || 'Trạng thái'
    toastRef.value?.info('Lọc', `Đã lọc theo trạng thái: ${statusName}`)
  } else {
    toastRef.value?.info('Lọc', 'Đã hiển thị tất cả trạng thái')
  }

  loadHoaDons()
}

function handleLoaiHoaDonChange() {
  console.log('🔄 Loai hoa don changed to:', filterLoaiHoaDon.value)
  currentPage.value = 1

  if (filterLoaiHoaDon.value !== '') {
    let loaiName = ''
    switch(filterLoaiHoaDon.value) {
      case 'BAN_THUONG':
        loaiName = 'Bán tại quầy'
        break
      case 'BAN_ONLINE':
        loaiName = 'Bán online'
        break
      default:
        loaiName = filterLoaiHoaDon.value
    }
    toastRef.value?.info('Lọc', `Đã lọc theo loại: ${loaiName}`)
  } else {
    toastRef.value?.info('Lọc', 'Đã hiển thị tất cả loại đơn hàng')
  }

  loadHoaDons()
}

async function loadHoaDons() {
  loading.value = true
  try {
    const searchRequest = {
      keyword: searchKeyword.value,
      trangThai: filterTrangThai.value !== '' ? parseInt(filterTrangThai.value) : null,
      loaiHoaDon: filterLoaiHoaDon.value || null,
      tuNgay: dateFrom.value ? (dateFrom.value + 'T00:00:00') : null,
      denNgay: dateTo.value ? (dateTo.value + 'T23:59:59') : null,
      page: currentPage.value - 1,
      size: pageSize.value,
      sortBy: sortBy.value,
      sortDirection: sortDirection.value
    }

    console.log('🔍 Search Request:', searchRequest)

    const response = await api.get('/hoa-don/search', { params: searchRequest })
    
    if (response.data && response.data.content) {
      hoaDons.value = response.data.content
      totalPages.value = response.data.totalPages
      totalElements.value = response.data.totalElements
      console.log('✅ Loaded hoa dons:', hoaDons.value.length)
      console.log('📋 Sample hoa don data:', hoaDons.value[0])
      if (hoaDons.value[0]) {
        console.log('🔍 First hoa don trangThai:', hoaDons.value[0].trangThai, 'type:', typeof hoaDons.value[0].trangThai)
      }
    } else {
      hoaDons.value = []
      totalPages.value = 0
      totalElements.value = 0
    }
  } catch (error) {
    console.error('❌ Error loading hoa dons:', error)
    toastRef.value?.error('Lỗi', 'Không thể tải danh sách hóa đơn')
    hoaDons.value = []
  } finally {
    loading.value = false
  }
}

function handleQuickSortChange() {
  const selectedOption = quickSortOptions.find(option => option.value === quickSort.value)
  if (selectedOption) {
    sortBy.value = selectedOption.sortBy
    sortDirection.value = selectedOption.direction
    currentPage.value = 1
    loadHoaDons()
  }
}

function handlePageSizeChange() {
  currentPage.value = 1
  loadHoaDons()
}

function goToPage(page: number) {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    loadHoaDons()
  }
}

function getVisiblePages() {
  const pages = []
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, currentPage.value + 2)
  
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  
  return pages
}

function toggleFilters() {
  showFilters.value = !showFilters.value
}

function handleSearch() {
  currentPage.value = 1
  loadHoaDons()
}

function resetFilters() {
  searchKeyword.value = ''
  filterTrangThai.value = ''
  filterLoaiHoaDon.value = ''
  dateFrom.value = ''
  dateTo.value = ''
  quickSort.value = 'ngayTao_desc'
  sortBy.value = 'ngayTao'
  sortDirection.value = 'desc'
  currentPage.value = 1
  showFilters.value = false
  loadHoaDons()
  toastRef.value?.info('Lọc', 'Đã xóa tất cả bộ lọc')
}

async function exportToExcel() {
  try {
    loading.value = true
    
    const searchRequest = {
      keyword: searchKeyword.value,
      trangThai: filterTrangThai.value !== '' ? parseInt(filterTrangThai.value) : null,
      loaiHoaDon: filterLoaiHoaDon.value || null,
      tuNgay: dateFrom.value ? (dateFrom.value + 'T00:00:00') : null,
      denNgay: dateTo.value ? (dateTo.value + 'T23:59:59') : null,
      page: 0,
      size: 10000, // Lấy tất cả dữ liệu để export
      sortBy: sortBy.value,
      sortDirection: sortDirection.value
    }

    const response = await api.get('/hoa-don/search', { params: searchRequest })
    const data = response.data.content || []

    // Chuẩn bị dữ liệu cho Excel
    const excelData = data.map((hd: HoaDon, index: number) => ({
      'STT': index + 1,
      'Mã hóa đơn': hd.maHoaDon,
      'Tên khách hàng': hd.tenKhachHang || 'Khách lẻ',
      'Số điện thoại': hd.soDienThoai || 'Không có',
      'Loại đơn': (hd.loaiHoaDon === 'BAN_ONLINE' || hd.loaiHoaDon === 'DELIVERY') ? 'Bán online' : 'Bán tại quầy',
      'Tổng tiền': hd.tongTienSauGiam || hd.tongTien,
      'Trạng thái': getStatusName(hd.trangThai),
      'Ngày tạo': formatDate(hd.ngayTao)
    }))

    // Tạo workbook và worksheet
    const wb = XLSX.utils.book_new()
    const ws = XLSX.utils.json_to_sheet(excelData)

    // Thêm worksheet vào workbook
    XLSX.utils.book_append_sheet(wb, ws, 'Danh sách hóa đơn')

    // Xuất file
    const fileName = `danh_sach_hoa_don_${new Date().toISOString().split('T')[0]}.xlsx`
    XLSX.writeFile(wb, fileName)

    toastRef.value?.success('Thành công', 'Đã xuất file Excel thành công')
  } catch (error) {
    console.error('❌ Error exporting to Excel:', error)
    toastRef.value?.error('Lỗi', 'Không thể xuất file Excel')
  } finally {
    loading.value = false
  }
}

function viewDetailsAndTrack(hoaDon: HoaDon) {
  // Emit event để parent component xử lý
  emit('viewDetails', hoaDon)
}

// Utility functions
function getStatusName(trangThai: number | string | null | undefined): string {
  console.log('🔍 getStatusName called with trangThai:', trangThai, 'type:', typeof trangThai)
  
  if (trangThai === null || trangThai === undefined) {
    console.log('❌ trangThai is null/undefined')
    return 'Không xác định'
  }
  
  const numericTrangThai = typeof trangThai === 'string' ? parseInt(trangThai) : trangThai
  console.log('🔢 Converted to numeric:', numericTrangThai)
  
  const status = statusOptions.find(s => s.value === numericTrangThai)
  console.log('📋 Found status:', status)
  
  return status ? status.label : 'Không xác định'
}

function getStatusClass(trangThai: number | string | null | undefined): string {
  if (trangThai === null || trangThai === undefined) {
    return 'status-unknown'
  }
  const numericTrangThai = typeof trangThai === 'string' ? parseInt(trangThai) : trangThai
  return `status-${numericTrangThai}`
}

function getStatusColor(trangThai: number | string | null | undefined): string {
  if (trangThai === null || trangThai === undefined) {
    return '#6c757d'
  }
  const numericTrangThai = typeof trangThai === 'string' ? parseInt(trangThai) : trangThai
  const status = statusOptions.find(s => s.value === numericTrangThai)
  return status ? status.color : '#6c757d'
}

function getStatusIcon(trangThai: number | string | null | undefined): string {
  if (trangThai === null || trangThai === undefined) {
    return 'question-circle'
  }
  
  const numericTrangThai = typeof trangThai === 'string' ? parseInt(trangThai) : trangThai
  const icons = {
    0: 'clock',
    1: 'credit-card',
    2: 'box',
    3: 'truck',
    4: 'check-circle',
    5: 'times-circle'
  }
  return icons[numericTrangThai as keyof typeof icons] || 'question-circle'
}

function getStatusBadgeClass(trangThai: number | string | null | undefined): string {
  if (trangThai === null || trangThai === undefined) {
    return 'status-unknown'
  }
  
  const numericTrangThai = typeof trangThai === 'string' ? parseInt(trangThai) : trangThai
  const classes = {
    0: 'status-pending',
    1: 'status-paid-pending',
    2: 'status-waiting',
    3: 'status-shipping',
    4: 'status-completed',
    5: 'status-cancelled'
  }
  return classes[numericTrangThai as keyof typeof classes] || 'status-unknown'
}

function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}

function formatDate(dateString: string): string {
  return new Date(dateString).toLocaleDateString('vi-VN')
}

// Emit events
const emit = defineEmits<{
  viewDetails: [hoaDon: HoaDon]
}>()

// Lifecycle
onMounted(() => {
  loadHoaDons()
})
</script>

<template>
  <div class="hoa-don-list-page">
    <!-- Search and Filter Section -->
    <section class="search-filter-section">
      <!-- Search Bar -->
      <div class="search-bar">
        <div class="input-group">
          <font-awesome-icon icon="search" class="input-icon" />
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="Tìm kiếm theo mã hóa đơn, tên khách hàng, SĐT"
            class="search-input"
            @input="handleSearch"
          />
        </div>
        <button @click="toggleFilters" class="btn-filter">
          <font-awesome-icon icon="filter" />
          Bộ lọc
        </button>
        <button @click="exportToExcel" class="btn-excel">
          <font-awesome-icon icon="file-excel" />
          Xuất Excel
        </button>
      </div>

      <!-- Expandable Filter Options -->
      <div v-if="showFilters" class="filter-options">
        <div class="filter-grid">
          <!-- Row 1: 3 filters -->
          <div class="filter-group">
            <label>Trạng thái:</label>
            <select v-model="filterTrangThai" class="filter-select" @change="handleStatusChange">
              <option value="">Tất cả trạng thái</option>
              <option v-for="status in statusOptions" :key="status.value" :value="status.value">
                {{ status.label }}
              </option>
            </select>
          </div>
          <div class="filter-group">
            <label>Loại đơn:</label>
            <select v-model="filterLoaiHoaDon" class="filter-select" @change="handleLoaiHoaDonChange">
              <option value="">Tất cả loại</option>
              <option value="BAN_THUONG">Bán tại quầy</option>
              <option value="BAN_ONLINE">Bán online</option>
            </select>
          </div>
          <div class="filter-group">
            <label>Sắp xếp:</label>
            <select v-model="quickSort" class="filter-select" @change="handleQuickSortChange">
              <option v-for="option in quickSortOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </div>

          <!-- Row 2: 2 date inputs + 2 buttons -->
          <div class="filter-group">
            <label>Từ ngày:</label>
            <input v-model="dateFrom" type="date" class="filter-input" @change="loadHoaDons" />
          </div>
          <div class="filter-group">
            <label>Đến ngày:</label>
            <input v-model="dateTo" type="date" class="filter-input" @change="loadHoaDons" />
          </div>
          <div class="filter-group button-group">
            <button @click="resetFilters" class="btn-reset">
              <font-awesome-icon icon="times" />
              Xóa bộ lọc
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- Table Section -->
    <section class="table-section">
      <div class="table-container">
        <div class="table-header">
          <h3>Danh sách hóa đơn</h3>
        </div>
        <div class="table-wrapper">
          <table>
            <thead>
            <tr>
              <th>STT</th>
              <th>Mã HD</th>
              <th>Khách hàng</th>
              <th>SĐT</th>
              <th>Loại đơn</th>
              <th>Tổng tiền</th>
              <th>Trạng thái</th>
              <th>Ngày tạo</th>
              <th>Thao tác</th>
            </tr>
            </thead>
            <tbody>
            <tr v-if="loading">
              <td colspan="9" class="text-center">
                <div class="loading">
                  <font-awesome-icon icon="spinner" class="fa-spin" />
                  Đang tải...
                </div>
              </td>
            </tr>
            <tr v-else-if="hoaDons.length === 0">
              <td colspan="9" class="text-center">
                <div class="empty-state">
                  <font-awesome-icon icon="file-invoice" class="empty-icon" />
                  <p>Không có dữ liệu</p>
                </div>
              </td>
            </tr>
            <tr v-else v-for="(hd, index) in hoaDons" :key="hd.id">
              <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
              <td>
                <div class="invoice-code">
                  {{ hd.maHoaDon }}
                </div>
              </td>
              <td>
                <div class="customer-info">
                  <div class="customer-name">{{ hd.tenKhachHang || 'Khách lẻ' }}</div>
                </div>
              </td>
              <td>
                <div class="phone-info" v-if="hd.soDienThoai">
                  {{ hd.soDienThoai }}
                </div>
                <div class="no-phone" v-else>
                  <span class="text-muted">Không có</span>
                </div>
              </td>
              <td>
                  <span :class="['order-type', (hd.loaiHoaDon === 'BAN_ONLINE' || hd.loaiHoaDon === 'DELIVERY') ? 'online' : 'normal']">
                    {{ (hd.loaiHoaDon === 'BAN_ONLINE' || hd.loaiHoaDon === 'DELIVERY') ? 'Bán online' : 'Bán tại quầy' }}
          </span>
              </td>
              <td>
                <div class="amount">
                  {{ formatCurrency(hd.tongTienSauGiam || hd.tongTien) }}
                </div>
              </td>
              <td>
                  <span :class="['status-badge', getStatusBadgeClass(hd.trangThai)]">
                    {{ getStatusName(hd.trangThai) }}
                  </span>
              </td>
              <td>
                <div class="date-info">
                  {{ formatDate(hd.ngayTao) }}
                </div>
              </td>
              <td>
                <div class="action-buttons">
                  <button @click="viewDetailsAndTrack(hd)" class="btn-view" title="Xem chi tiết và theo dõi">
                    <font-awesome-icon icon="eye" />
                  </button>
                </div>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>
    </section>

    <!-- Pagination -->
    <section class="pagination-section">
      <div class="pagination-controls">
        <!-- Page Size Selector -->
        <div class="page-size-selector">
          <label for="pageSize">Hiển thị:</label>
          <select v-model="pageSize" @change="handlePageSizeChange" id="pageSize" class="page-size-select">
            <option value="5">5</option>
            <option value="10">10</option>
            <option value="20">20</option>
            <option value="50">50</option>
            <option value="100">100</option>
          </select>
          <span>hóa đơn/trang</span>
        </div>

        <!-- Pagination Info -->
        <div class="pagination-info">
          <span>Hiển thị {{ Math.min((currentPage - 1) * pageSize + 1, totalElements) }} - {{ Math.min(currentPage * pageSize, totalElements) }} trong tổng số {{ totalElements }} hóa đơn</span>
        </div>
      </div>

      <!-- Page Navigation -->
      <div class="pagination" v-if="totalPages > 0">
        <button @click="() => { console.log('Previous clicked'); goToPage(currentPage - 1); }" :disabled="currentPage <= 1" class="page-btn">
          <font-awesome-icon icon="chevron-left" />
          Trước
        </button>

        <!-- Page Numbers -->
        <div class="page-numbers">
          <!-- First page -->
          <button
            v-if="totalPages > 1 && currentPage > 3"
            @click="() => { console.log('First page clicked'); goToPage(1); }"
            class="page-number"
          >
            1
          </button>
          <span v-if="totalPages > 1 && currentPage > 4" class="page-ellipsis">...</span>

          <!-- Pages around current page -->
          <template v-for="page in getVisiblePages()" :key="page">
            <button
              @click="() => { console.log('Page number clicked:', page); goToPage(page); }"
              :class="['page-number', { 'active': page === currentPage }]"
            >
              {{ page }}
            </button>
          </template>

          <!-- Last page -->
          <span v-if="totalPages > 1 && currentPage < totalPages - 3" class="page-ellipsis">...</span>
          <button
            v-if="totalPages > 1 && currentPage < totalPages - 2"
            @click="() => { console.log('Last page clicked:', totalPages); goToPage(totalPages); }"
            class="page-number"
          >
            {{ totalPages }}
          </button>
        </div>

        <button @click="() => { console.log('Next clicked'); goToPage(currentPage + 1); }" :disabled="currentPage >= totalPages" class="page-btn">
          Sau
          <font-awesome-icon icon="chevron-right" />
        </button>
      </div>
    </section>

    <!-- Toast Component -->
    <Toast ref="toastRef" />
  </div>
</template>

<style scoped>
.hoa-don-list-page {
  padding: 20px;
  background-color: #f8f9fa;
  min-height: 100vh;
}

/* Search and Filter Section */
.search-filter-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.search-bar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
}

.input-group {
  position: relative;
  flex: 1;
}

.input-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #6c757d;
  z-index: 2;
}

.search-input {
  width: 100%;
  padding: 12px 12px 12px 40px;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
}

.btn-filter, .btn-excel {
  padding: 12px 16px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-filter {
  background: #6c757d;
  color: white;
}

.btn-filter:hover {
  background: #5a6268;
}

.btn-excel {
  background: #28a745;
  color: white;
}

.btn-excel:hover {
  background: #218838;
}

/* Filter Options */
.filter-options {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e9ecef;
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  align-items: end;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.filter-group label {
  font-size: 12px;
  font-weight: 600;
  color: #495057;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.filter-select, .filter-input {
  padding: 8px 12px;
  border: 1px solid #ced4da;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.filter-select:focus, .filter-input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.1);
}

.button-group {
  display: flex;
  justify-content: flex-end;
}

.btn-reset {
  padding: 8px 16px;
  background: #dc3545;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
}

.btn-reset:hover {
  background: #c82333;
}

/* Table Section */
.table-section {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.table-header {
  padding: 20px;
  border-bottom: 1px solid #e9ecef;
  background: #f8f9fa;
}

.table-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #495057;
}

.table-wrapper {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

th, td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #e9ecef;
}

th {
  background: #f8f9fa;
  font-weight: 600;
  color: #495057;
  text-transform: uppercase;
  font-size: 12px;
  letter-spacing: 0.5px;
}

tbody tr:hover {
  background: #f8f9fa;
}

.text-center {
  text-align: center;
}

.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 20px;
  color: #6c757d;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #6c757d;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

/* Table Content Styles */
.invoice-code {
  font-family: 'Courier New', monospace;
  font-weight: 600;
  color: #007bff;
}

.customer-info {
  display: flex;
  flex-direction: column;
}

.customer-name {
  font-weight: 500;
  color: #495057;
}

.phone-info {
  color: #6c757d;
}

.no-phone {
  color: #adb5bd;
}

.order-type {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.order-type.online {
  background: #e3f2fd;
  color: #1976d2;
}

.order-type.normal {
  background: #f3e5f5;
  color: #7b1fa2;
}

.amount {
  font-weight: 600;
  color: #28a745;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-pending {
  background: #fff3cd;
  color: #856404;
}

.status-paid-pending {
  background: #e2e3f1;
  color: #6f42c1;
}

.status-waiting {
  background: #d1ecf1;
  color: #0c5460;
}

.status-shipping {
  background: #ffeaa7;
  color: #d63031;
}

.status-completed {
  background: #d4edda;
  color: #155724;
}

.status-cancelled {
  background: #f8d7da;
  color: #721c24;
}

.status-unknown {
  background: #e2e3e5;
  color: #6c757d;
}

.date-info {
  color: #6c757d;
  font-size: 13px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.btn-view {
  padding: 6px 12px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 4px;
}

.btn-view:hover {
  background: #0056b3;
}

/* Pagination */
.pagination-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.pagination-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 16px;
}

.page-size-selector {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #495057;
}

.page-size-select {
  padding: 6px 12px;
  border: 1px solid #ced4da;
  border-radius: 4px;
  font-size: 14px;
}

.pagination-info {
  font-size: 14px;
  color: #6c757d;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #dee2e6;
  background: white;
  color: #495057;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

.page-btn:hover:not(:disabled) {
  background: #e9ecef;
  border-color: #adb5bd;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-numbers {
  display: flex;
  gap: 4px;
  align-items: center;
}

.page-number {
  padding: 8px 12px;
  border: 1px solid #dee2e6;
  background: white;
  color: #495057;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
  min-width: 40px;
  text-align: center;
}

.page-number:hover {
  background: #e9ecef;
  border-color: #adb5bd;
}

.page-number.active {
  background: #007bff;
  color: white;
  border-color: #007bff;
}

.page-ellipsis {
  padding: 8px 4px;
  color: #6c757d;
  font-size: 14px;
}

/* Responsive */
@media (max-width: 768px) {
  .hoa-don-list-page {
    padding: 10px;
  }
  
  .search-bar {
    flex-direction: column;
    gap: 8px;
  }
  
  .filter-grid {
    grid-template-columns: 1fr;
  }
  
  .pagination-controls {
    flex-direction: column;
    align-items: stretch;
  }
  
  .pagination {
    flex-wrap: wrap;
  }
  
  .table-wrapper {
    font-size: 12px;
  }
  
  th, td {
    padding: 8px 12px;
  }
}
</style>



