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
            <p>Tìm kiếm và lọc phân ca theo tiêu chí</p>
          </div>
          <div class="filter-actions">
            <button class="btn-show-inactive" @click="showInactiveCa">
              Xem phân ca đã kết thúc
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
              placeholder="Tìm theo tên nhân viên, ca làm việc..."
              @input="applyFilters"
              class="search-input"
            />
          </div>
          <div class="filter-group">
            <label>Trạng thái:</label>
            <select v-model="statusFilter" @change="applyFilters" class="filter-select">
              <option value="">Tất cả trạng thái</option>
              <option value="0">Chưa bắt đầu</option>
              <option value="1">Đang làm</option>
              <option value="2">Đã kết thúc</option>
              <option value="3">Vắng mặt</option>
            </select>
          </div>
          <div class="filter-group">
            <label>Ngày làm việc:</label>
            <input
              type="date"
              v-model="dateFilter"
              @change="applyFilters"
              class="filter-select"
            />
          </div>
          <div class="filter-group">
            <label>Nhân viên:</label>
            <input
              type="text"
              v-model="nhanVienFilter"
              placeholder="Tìm theo tên nhân viên..."
              @input="applyFilters"
              class="search-input"
            />
          </div>
        </div>
      </div>

      <!-- Add Ca Button -->
      <div class="add-voucher-section">
        <button @click="exportToExcel" class="btn-export-excel">
          <font-awesome-icon :icon="['fas', 'file-excel']" />
          Xuất Excel
        </button>
        <button class="btn-add-voucher" @click="addPhanCa()">
          <font-awesome-icon :icon="['fas', 'plus-circle']" />
          Thêm Phân Ca
        </button>
      </div>

      <!-- Main Content Area -->
      <div class="main-content">
        <!-- Table Header Section -->
        <div class="table-header">
          <div class="table-title">
            <h2>Danh sách Phân Ca</h2>
            <span class="item-count">{{ totalItems }} phân ca</span>
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
              <th>Nhân viên</th>
              <th>Ca làm việc</th>
              <th>Thời gian</th>
              <th>Ngày làm việc</th>
              <th>Trạng thái</th>
              <th>Thao tác</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="(phanCa, index) in filteredPhanCa" :key="phanCa.id">
              <td class="checkbox-column">
                <input
                  type="checkbox"
                  class="row-checkbox"
                  :value="phanCa.id"
                  :checked="selectedPhanCaIds.includes(phanCa.id)"
                  @change="toggleSelectPhanCa(phanCa.id)"
                />
              </td>
              <td>{{ startItem + index }}</td>
              <td>{{ phanCa.nhanVien?.hoTen || 'N/A' }}</td>
              <td>{{ phanCa.ca?.tenCa || 'N/A' }}</td>
              <td>
                <span v-if="phanCa.ca">
                  {{ formatTime(phanCa.ca.gioBatDau) }} - {{ formatTime(phanCa.ca.gioKetThuc) }}
                </span>
                <span v-else>N/A</span>
              </td>
              <td>{{ formatDate(phanCa.ngayLamViec) }}</td>
              <td>
                <div class="status-badge-container">
                  <span class="status-badge" :class="getStatusBadgeClass(phanCa)">
                    {{ getStatusText(phanCa) }}
                  </span>
                </div>
              </td>
              <td>
                <div class="action-buttons">
                  <div class="button-row">
                    <button class="btn-view" @click="viewPhanCa(phanCa)" title="Xem chi tiết">
                      <font-awesome-icon icon="eye" />
                    </button>
                    <button 
                      v-if="phanCa.trangThai !== 2" 
                      class="btn-edit" 
                      @click="editPhanCa(phanCa)" 
                      title="Chỉnh sửa"
                    >
                      <font-awesome-icon icon="edit" />
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

interface PhanCa {
  id: number
  nhanVienId: number
  caId: number
  ngayLamViec: string
  trangThai: number | null
  gioBatDauThucTe?: string | null
  gioKetThucThucTe?: string | null
  ghiChu?: string | null
  nhanVien?: {
    id: number
    hoTen: string
    maNhanVien?: string
  }
  ca?: {
    id: number
    tenCa: string
    gioBatDau: string
    gioKetThuc: string
    moTa?: string
  }
}

const phanCaList = ref<PhanCa[]>([])
const selectedPhanCa = ref<PhanCa[]>([])
const selectedPhanCaIds = ref<number[]>([])
const loading = ref(false)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

// Filter states
const statusFilter = ref('')
const dateFilter = ref('')
const nhanVienFilter = ref('')
const caFilter = ref('')
const searchText = ref('')

// Pagination states
const currentPage = ref(1)
const itemsPerPage = ref(5)
const totalItems = ref(0)

// Computed property for filtered phan ca (without pagination)
const allFilteredPhanCa = computed(() => {
  let filtered = phanCaList.value

  // Filter by status
  if (statusFilter.value !== '') {
    filtered = filtered.filter(pc => pc.trangThai !== null && pc.trangThai.toString() === statusFilter.value)
  }

  // Filter by date
  if (dateFilter.value !== '') {
    filtered = filtered.filter(pc => pc.ngayLamViec === dateFilter.value)
  }

  // Filter by nhan vien
  if (nhanVienFilter.value !== '') {
    filtered = filtered.filter(pc => 
      pc.nhanVien?.hoTen?.toLowerCase().includes(nhanVienFilter.value.toLowerCase()) ||
      pc.nhanVienId.toString() === nhanVienFilter.value
    )
  }

  // Filter by ca
  if (caFilter.value !== '') {
    filtered = filtered.filter(pc => 
      pc.ca?.tenCa?.toLowerCase().includes(caFilter.value.toLowerCase()) ||
      pc.caId.toString() === caFilter.value
    )
  }

  // Search filter
  if (searchText.value.trim() !== '') {
    const search = searchText.value.toLowerCase()
    filtered = filtered.filter(pc =>
      (pc.nhanVien?.hoTen && pc.nhanVien.hoTen.toLowerCase().includes(search)) ||
      (pc.ca?.tenCa && pc.ca.tenCa.toLowerCase().includes(search)) ||
      (pc.ghiChu && pc.ghiChu.toLowerCase().includes(search))
    )
  }

  return filtered
})

// Computed property for paginated phan ca
const filteredPhanCa = computed(() => {
  const filtered = allFilteredPhanCa.value
  totalItems.value = filtered.length

  const startIndex = (currentPage.value - 1) * itemsPerPage.value
  const endIndex = startIndex + itemsPerPage.value

  return filtered.slice(startIndex, endIndex)
})

// Computed property for select all checkbox
const isAllSelected = computed(() => {
  return filteredPhanCa.value.length > 0 &&
    filteredPhanCa.value.every(pc => selectedPhanCaIds.value.includes(pc.id))
})

// Computed property for selected phan ca
const selectedPhanCaComputed = computed(() => {
  return phanCaList.value.filter(pc => selectedPhanCaIds.value.includes(pc.id))
})

// Computed properties for pagination
const totalPages = computed(() => Math.ceil(totalItems.value / itemsPerPage.value))
const startItem = computed(() => (currentPage.value - 1) * itemsPerPage.value + 1)
const endItem = computed(() => Math.min(currentPage.value * itemsPerPage.value, totalItems.value))

async function loadPhanCa() {
  loading.value = true
  try {
    console.log('Loading phan ca from /api/phan-ca...')
    const { data } = await api.get<PhanCa[]>('/api/phan-ca')
    console.log('Loaded phan ca:', data)

    phanCaList.value = data
  } catch (error) {
    console.error('Lỗi khi tải danh sách phân ca:', error)
    toastRef.value?.error('Lỗi', 'Không thể tải danh sách phân ca: ' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

function addPhanCa() {
  router.push('/phan-ca/form/new')
}

function editPhanCa(phanCa: PhanCa) {
  router.push(`/phan-ca/form/${phanCa.id}`)
}

function viewPhanCa(phanCa: PhanCa) {
  router.push(`/phan-ca/detail/${phanCa.id}`)
}

async function toggleStatus(id: number) {
  // Không cho phép toggle status từ list page, chỉ xem
  toastRef.value?.info('Thông báo', 'Vui lòng sử dụng chức năng bắt đầu/kết thúc ca từ trang Giao Ca')
}

function applyFilters() {
  // Reset to first page when filters change
  currentPage.value = 1
}

function clearAllFilters() {
  searchText.value = ''
  statusFilter.value = ''
  dateFilter.value = ''
  nhanVienFilter.value = ''
  caFilter.value = ''
  currentPage.value = 1
  toastRef.value?.info('Thông báo', 'Đã xóa tất cả bộ lọc')
}

function showInactiveCa() {
  statusFilter.value = '2' // Đã kết thúc
  currentPage.value = 1
  toastRef.value?.info('Thông báo', 'Đang hiển thị phân ca đã kết thúc')
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
function formatTime(timeString: string): string {
  if (!timeString) return '-'
  return timeString.substring(0, 5) // HH:MM
}

function getStatusText(phanCa: PhanCa): string {
  if (phanCa.trangThai === null) return 'Chưa bắt đầu'
  switch (phanCa.trangThai) {
    case 0: return 'Chưa bắt đầu'
    case 1: return 'Đang làm'
    case 2: return 'Đã kết thúc'
    case 3: return 'Vắng mặt'
    default: return 'Không xác định'
  }
}

function getStatusBadgeClass(phanCa: PhanCa): string {
  if (phanCa.trangThai === null) return 'badge-pending'
  switch (phanCa.trangThai) {
    case 0: return 'badge-pending'
    case 1: return 'badge-active'
    case 2: return 'badge-completed'
    case 3: return 'badge-absent'
    default: return 'badge-unknown'
  }
}

// Export to Excel function
function exportToExcel() {
  try {
    const phanCaToExport = selectedPhanCaComputed.value.length > 0 ? selectedPhanCaComputed.value : phanCaList.value

    if (phanCaToExport.length === 0) {
      toastRef.value?.warning('Cảnh báo', 'Không có dữ liệu phân ca để xuất Excel')
      return
    }

    const excelData = phanCaToExport.map((pc, index) => ({
      'STT': index + 1,
      'Nhân viên': pc.nhanVien?.hoTen || 'N/A',
      'Ca làm việc': pc.ca?.tenCa || 'N/A',
      'Giờ làm việc': pc.ca ? `${formatTime(pc.ca.gioBatDau)} - ${formatTime(pc.ca.gioKetThuc)}` : 'N/A',
      'Ngày làm việc': formatDate(pc.ngayLamViec),
      'Trạng thái': getStatusText(pc),
      'Giờ bắt đầu thực tế': pc.gioBatDauThucTe ? formatDateTime(pc.gioBatDauThucTe) : 'Chưa bắt đầu',
      'Giờ kết thúc thực tế': pc.gioKetThucThucTe ? formatDateTime(pc.gioKetThucThucTe) : 'Chưa kết thúc',
      'Ghi chú': pc.ghiChu || ''
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
    XLSX.utils.book_append_sheet(wb, ws, 'Danh sách phân ca')

    const now = new Date()
    const dateStr = now.toISOString().split('T')[0]
    const filename = `danh_sach_phan_ca_${dateStr}.xlsx`

    XLSX.writeFile(wb, filename)

    toastRef.value?.success('Thành công', `Đã xuất ${phanCaToExport.length} phân ca ra file Excel`)

  } catch (error) {
    console.error('Error exporting to Excel:', error)
    toastRef.value?.error('Lỗi', 'Không thể xuất file Excel')
  }
}

// Checkbox functions
function toggleSelectAll() {
  if (isAllSelected.value) {
    const currentPageIds = filteredPhanCa.value.map(pc => pc.id)
    selectedPhanCaIds.value = selectedPhanCaIds.value.filter(id => !currentPageIds.includes(id))
  } else {
    const currentPageIds = filteredPhanCa.value.map(pc => pc.id)
    const newIds = currentPageIds.filter(id => !selectedPhanCaIds.value.includes(id))
    selectedPhanCaIds.value = [...selectedPhanCaIds.value, ...newIds]
  }
}

function toggleSelectPhanCa(phanCaId: number) {
  const index = selectedPhanCaIds.value.indexOf(phanCaId)
  if (index > -1) {
    selectedPhanCaIds.value.splice(index, 1)
  } else {
    selectedPhanCaIds.value.push(phanCaId)
  }
}

function formatDate(dateString: string): string {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString('vi-VN')
}

function formatDateTime(dateTimeString: string | null | undefined): string {
  if (!dateTimeString) return 'N/A'
  return new Date(dateTimeString).toLocaleString('vi-VN')
}

onMounted(loadPhanCa)
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
  min-width: 800px;
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
  width: 150px;
  min-width: 150px;
  max-width: 150px;
  overflow: hidden;
} /* Nhân viên */
th:nth-child(4) {
  width: 120px;
  min-width: 120px;
  max-width: 120px;
  overflow: hidden;
} /* Ca làm việc */
th:nth-child(5) {
  width: 120px;
  min-width: 120px;
  max-width: 120px;
  overflow: hidden;
  text-align: center;
} /* Thời gian */
th:nth-child(6) {
  width: 120px;
  min-width: 120px;
  max-width: 120px;
  white-space: normal;
  word-wrap: break-word;
  text-align: center;
} /* Ngày làm việc */
th:nth-child(7) {
  width: 120px;
  min-width: 120px;
  white-space: normal;
  text-align: center;
} /* Trạng thái */
th:nth-child(8) {
  width: 100px;
  min-width: 100px;
  max-width: 100px;
  white-space: normal;
  word-wrap: break-word;
  word-break: break-word;
  hyphens: auto;
  overflow-wrap: break-word;
  overflow: hidden;
} /* Thao tác */

/* Special border for action column */
th:nth-child(8),
td:nth-child(8) {
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

.badge-active {
  background: #dcfce7;
  color: #166534;
}

.badge-inactive {
  background: #fef2f2;
  color: #dc2626;
}

.badge-pending {
  background: #fef3c7;
  color: #92400e;
}

.badge-completed {
  background: #dcfce7;
  color: #166534;
}

.badge-absent {
  background: #fee2e2;
  color: #991b1b;
}

.badge-unknown {
  background: #f3f4f6;
  color: #374151;
}

/* Toggle Switch Styles */
.toggle-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  width: 100%;
  white-space: normal;
  word-wrap: break-word;
  word-break: break-word;
  hyphens: auto;
}

.toggle-switch {
  position: relative;
  display: inline-block;
  width: 40px;
  height: 20px;
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
  border-radius: 20px;
}

.toggle-slider:before {
  position: absolute;
  content: "";
  height: 14px;
  width: 14px;
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
  transform: translateX(20px);
}

.toggle-switch.disabled {
  opacity: 0.5;
  cursor: not-allowed;
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
.btn-edit {
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
@media (max-width: 1200px) {
  .filter-row {
    grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
    gap: 16px;
  }

  table {
    min-width: 700px;
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
    min-width: 600px;
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



































