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
            <p>Tìm kiếm và lọc nhân viên theo tiêu chí</p>
          </div>
          <div class="filter-actions">
            <button class="btn-clear-filters" @click="clearAllFilters">Xóa bộ lọc</button>
          </div>
        </div>
        <div class="filter-row">
          <div class="filter-group">
            <label>Tìm kiếm:</label>
            <input
              type="text"
              v-model="searchQuery"
              placeholder="Tìm theo tên hoặc mã nhân viên..."
              @input="searchNhanVien"
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
            <label>Chức vụ:</label>
            <select v-model="selectedChucVu" @change="filterByChucVu" class="filter-select">
              <option value="">Tất cả chức vụ</option>
              <option value="Quản lý">Quản lý</option>
              <option value="Nhân viên">Nhân viên</option>
            </select>
          </div>
          <div class="filter-group">
            <label>Giới tính:</label>
            <select v-model="genderFilter" @change="applyFilters" class="filter-select">
              <option value="">Tất cả giới tính</option>
              <option value="Nam">Nam</option>
              <option value="Nữ">Nữ</option>
            </select>
          </div>
          <div class="filter-group">
            <button class="btn-add-employee" @click="addEmployee()">
              <i class="icon-plus"></i>
              Thêm Nhân Viên
            </button>
          </div>
        </div>
      </div>

      <!-- Main Content Area -->
      <div class="main-content">
        <!-- Table Header Section -->
        <div class="table-header">
          <div class="table-title">
            <h2>Danh sách Nhân Viên</h2>
            <span class="item-count">{{ totalItems }} nhân viên</span>
          </div>
          <div class="table-actions">
            <div class="action-buttons">
              <button class="btn-import-excel" @click="importExcel">
                <span class="excel-logo">X</span>
                Import Excel
              </button>
              <button class="btn-export-excel" @click="exportExcel">
                <span class="excel-logo">X</span>
                Export Excel
              </button>
              <div v-if="selectedEmployees.length > 0" class="selected-info">
                <span class="selected-count">{{ selectedEmployees.length }} đã chọn</span>
                <button
                  class="btn-bulk-action"
                  @click="clearSelection"
                  v-if="selectedEmployees.length > 0"
                >
                  ❌ Bỏ chọn
                </button>
              </div>
            </div>
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
              <th>
                <input
                  type="checkbox"
                  v-model="selectAll"
                  @change="toggleSelectAll"
                  class="select-all-checkbox"
                />
              </th>
              <th>STT</th>
              <th>Mã NV</th>
              <th>Họ Tên</th>
              <th>SĐT</th>
              <th>Email</th>
              <th>Chức Vụ</th>
              <th>Trạng Thái</th>
              <th>Thao Tác</th>
            </tr>
            </thead>
            <tbody>
            <tr v-if="loading">
              <td colspan="9" class="text-center">Đang tải...</td>
            </tr>
            <tr v-else-if="filteredNhanViens.length === 0">
              <td colspan="9" class="text-center">Không có dữ liệu</td>
            </tr>
            <tr v-else v-for="(nhanVien, index) in paginatedNhanViens" :key="nhanVien.id">
              <td>
                <input
                  type="checkbox"
                  v-model="selectedEmployees"
                  :value="nhanVien.id"
                  class="employee-checkbox"
                />
              </td>
              <td>{{ (currentPage - 1) * itemsPerPage + index + 1 }}</td>
              <td>{{ nhanVien.maNhanVien }}</td>
              <td>{{ nhanVien.hoTen }}</td>
              <td>{{ nhanVien.soDienThoai }}</td>
              <td>{{ nhanVien.email }}</td>
              <td>{{ nhanVien.chucVu }}</td>
              <td>
                <span class="status-badge" :class="getStatusBadgeClass(nhanVien)">
                  {{ getStatusText(nhanVien) }}
                </span>
              </td>
              <td>
                <div class="action-buttons">
                  <div class="button-row">
                    <button class="btn-view" @click="viewEmployee(nhanVien)" title="Xem chi tiết">
                      <font-awesome-icon icon="eye" />
                    </button>
                    <button class="btn-edit" @click="editEmployee(nhanVien)" title="Chỉnh sửa">
                      <font-awesome-icon icon="edit" />
                    </button>
                  </div>
                  <div class="toggle-container">
                    <label class="toggle-switch" :title="nhanVien.trangThai === 1 ? 'Gạt để vô hiệu hóa' : 'Gạt để kích hoạt'">
                      <input
                        type="checkbox"
                        :checked="nhanVien.trangThai === 1"
                        @change="toggleStatus(nhanVien)"
                      />
                      <span class="toggle-slider"></span>
                    </label>
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
              <i class="icon-chevron-right"></i>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Hidden file input for Excel import -->
    <input
      type="file"
      ref="excelFileInput"
      @change="handleExcelImport"
      accept=".xlsx,.xls"
      style="display: none"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import Toast from '@/components/Toast.vue'
import * as XLSX from 'xlsx'
import PosHeader from '@/components/PosHeader.vue'
import api from '@/services/api'

const router = useRouter()

interface NhanVien {
  id?: number
  maNhanVien: string
  hoTen: string
  soDienThoai: string
  ngaySinh: string
  taiKhoan: string
  matKhau: string
  gioiTinh: string
  diaChi: string
  email: string
  chucVu: string
  trangThai: number
}

const toastRef = ref<InstanceType<typeof Toast> | null>(null)
const excelFileInput = ref<HTMLInputElement | null>(null)
const nhanViens = ref<NhanVien[]>([])
const loading = ref(false)
const searchQuery = ref('')
const selectedChucVu = ref('')
const statusFilter = ref('')
const genderFilter = ref('')
const currentPage = ref(1)
const itemsPerPage = ref(10)
const totalItems = ref(0)

// Checkbox selection
const selectedEmployees = ref<number[]>([])
const selectAll = ref(false)

const filteredNhanViens = computed(() => {
  let filtered = nhanViens.value

  // Search filter
  if (searchQuery.value.trim() !== '') {
    const search = searchQuery.value.toLowerCase()
    filtered = filtered.filter(
      (nv) =>
        nv.hoTen.toLowerCase().includes(search) ||
        nv.maNhanVien.toLowerCase().includes(search) ||
        nv.email.toLowerCase().includes(search),
    )
  }

  // Status filter
  if (statusFilter.value !== '') {
    filtered = filtered.filter((nv) => nv.trangThai.toString() === statusFilter.value)
  }

  // Position filter
  if (selectedChucVu.value) {
    filtered = filtered.filter((nv) => nv.chucVu === selectedChucVu.value)
  }

  // Gender filter
  if (genderFilter.value) {
    filtered = filtered.filter((nv) => nv.gioiTinh === genderFilter.value)
  }

  totalItems.value = filtered.length
  return filtered
})

// Paginated data
const paginatedNhanViens = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return filteredNhanViens.value.slice(start, end)
})

const totalPages = computed(() => Math.ceil(totalItems.value / itemsPerPage.value))
const startItem = computed(() => (currentPage.value - 1) * itemsPerPage.value + 1)
const endItem = computed(() => Math.min(currentPage.value * itemsPerPage.value, totalItems.value))

// Watch for changes in selected employees to update select all checkbox
watch(
  selectedEmployees,
  (newSelection) => {
    const currentPageIds = paginatedNhanViens.value.map((nv) => nv.id)
    selectAll.value =
      currentPageIds.length > 0 && currentPageIds.every((id) => newSelection.includes(id))
  },
  { deep: true },
)

// Checkbox functions
function toggleSelectAll() {
  if (selectAll.value) {
    selectedEmployees.value = paginatedNhanViens.value.map((nv) => nv.id)
  } else {
    selectedEmployees.value = []
  }
}

// Clear selection function
function clearSelection() {
  if (selectedEmployees.value.length === 0) return

  // Clear all selected employees
  selectedEmployees.value = []
  selectAll.value = false

  toastRef.value?.info('Thông báo', 'Đã bỏ tích tất cả nhân viên đã chọn')
}

// Import Excel function
function importExcel() {
  excelFileInput.value?.click()
}

// Handle Excel file import
async function handleExcelImport(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]

  if (!file) return

  try {
    const data = await file.arrayBuffer()
    const workbook = XLSX.read(data)
    const sheetName = workbook.SheetNames[0]
    const worksheet = workbook.Sheets[sheetName]
    const jsonData = XLSX.utils.sheet_to_json(worksheet, { header: 1 })

    // Skip header row and process data
    const employees = jsonData
      .slice(1)
      .map((row: any, index: number) => ({
        maNhanVien: row[0] || `NV${Date.now()}${index}`,
        hoTen: row[1] || '',
        email: row[2] || '',
        chucVu: row[3] || 'Nhân viên',
        trangThai: row[4] === 'Hoạt động' || row[4] === 1 ? 1 : 0,
      }))
      .filter((emp) => emp.hoTen) // Only include rows with names

    if (employees.length === 0) {
      toastRef.value?.warning(
        'Cảnh báo',
        'Không tìm thấy dữ liệu nhân viên hợp lệ trong file Excel',
      )
      return
    }

    // Send each employee to API individually
    let successCount = 0
    let errorCount = 0

    for (const employee of employees) {
      try {
        await api.post('/api/nhan-vien', employee)
        successCount++
      } catch (error) {
        console.error('Error creating employee:', error)
        errorCount++
      }
    }

    if (successCount > 0) {
      toastRef.value?.success('Thành công', `Đã import ${successCount} nhân viên từ file Excel`)
    }

    if (errorCount > 0) {
      toastRef.value?.warning('Cảnh báo', `${errorCount} nhân viên không thể import do lỗi`)
    }

    // Reload data
    await loadNhanViens()

    // Clear file input
    target.value = ''
  } catch (error: any) {
    console.error('Import error:', error)
    toastRef.value?.error(
      'Lỗi',
      'Có lỗi khi import file Excel: ' + (error.response?.data?.message || error.message),
    )
    target.value = ''
  }
}

// Export Excel function
async function exportExcel() {
  try {
    // Get all employees using existing API
    const response = await api.get('/api/nhan-vien')
    const employees = response.data

    // Prepare data for Excel
    const excelData = [['Mã NV', 'Họ tên', 'Email', 'Chức vụ', 'Trạng thái', 'Ngày tạo']]

    employees.forEach((emp: NhanVien) => {
      excelData.push([
        emp.maNhanVien,
        emp.hoTen,
        emp.email,
        emp.chucVu,
        emp.trangThai === 1 ? 'Hoạt động' : 'Không hoạt động',
        new Date().toLocaleDateString('vi-VN'),
      ])
    })

    // Create workbook
    const ws = XLSX.utils.aoa_to_sheet(excelData)
    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, 'Danh sách nhân viên')

    // Download file
    const fileName = `danh_sach_nhan_vien_${new Date().toISOString().split('T')[0]}.xlsx`
    XLSX.writeFile(wb, fileName)

    toastRef.value?.success('Thành công', 'Đã xuất file Excel thành công')
  } catch (error: any) {
    console.error('Export error:', error)
    toastRef.value?.error(
      'Lỗi',
      'Có lỗi khi xuất file Excel: ' + (error.response?.data?.message || error.message),
    )
  }
}


onMounted(() => {
  loadNhanViens()
})

async function loadNhanViens() {
  loading.value = true
  try {
    const { data } = await api.get<NhanVien[]>('/api/nhan-vien')
    nhanViens.value = data
  } catch (error) {
    console.error('Error loading nhan vien:', error)
    toastRef.value?.error('Lỗi', 'Không thể tải danh sách nhân viên')
  } finally {
    loading.value = false
  }
}

function searchNhanVien() {
  // Reset to first page when searching
  currentPage.value = 1
}

function filterByChucVu() {
  // Reset to first page when filtering
  currentPage.value = 1
}

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

function addEmployee() {
  router.push('/nhan-vien/them')
}

function viewEmployee(nhanVien: NhanVien) {
  router.push(`/nhan-vien/chi-tiet/${nhanVien.id}`)
}

function editEmployee(nhanVien: NhanVien) {
  router.push(`/nhan-vien/sua/${nhanVien.id}`)
}

function applyFilters() {
  // Reset to first page when filters change
  currentPage.value = 1
}

function clearAllFilters() {
  searchQuery.value = ''
  statusFilter.value = ''
  selectedChucVu.value = ''
  genderFilter.value = ''
  currentPage.value = 1
  toastRef.value?.info('Thông báo', 'Đã xóa tất cả bộ lọc')
}

function getStatusText(nhanVien: NhanVien): string {
  return nhanVien.trangThai === 1 ? 'Hoạt động' : 'Nghỉ việc'
}

function getStatusBadgeClass(nhanVien: NhanVien): string {
  return nhanVien.trangThai === 1 ? 'badge-active' : 'badge-inactive'
}

// Toggle status
async function toggleStatus(nhanVien: NhanVien) {
  const newStatus = nhanVien.trangThai === 1 ? 0 : 1
  const statusText = newStatus === 1 ? 'kích hoạt' : 'vô hiệu hóa'

  if (confirm(`Bạn có chắc chắn muốn ${statusText} nhân viên ${nhanVien.hoTen}?`)) {
    try {
      const updateData = {
        ...nhanVien,
        trangThai: newStatus,
      }

      await api.put(`/api/nhan-vien/${nhanVien.id}`, updateData)

      // Update local data
      nhanVien.trangThai = newStatus

      toastRef.value?.success(
        'Thành công',
        `${statusText.charAt(0).toUpperCase() + statusText.slice(1)} nhân viên thành công!`,
      )
    } catch (error) {
      console.error('Error toggling status:', error)
      toastRef.value?.error('Lỗi', 'Không thể thay đổi trạng thái nhân viên')
    }
  }
}
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

/* Add Employee Button in Filter */
.btn-add-employee {
  background: #f97316;
  color: white;
  padding: 12px 24px;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 13px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(249, 115, 22, 0.3);
  width: 100%;
  justify-content: center;
  height: 48px;
  box-sizing: border-box;
}

.btn-add-employee:hover {
  background: #ea580c;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.4);
}

.icon-plus::before {
  content: '+';
  font-size: 16px;
  font-weight: bold;
}

.filter-section {
  background: white;
  padding: 28px;
  margin-bottom: 24px;
  margin-top: 20px;
  border-radius: 16px;
  box-shadow:
    0 10px 25px -5px rgba(0, 0, 0, 0.1),
    0 4px 6px -2px rgba(0, 0, 0, 0.05);
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

.filter-row {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
  position: relative;
  align-items: end;
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
  align-items: stretch;
}

.filter-group:hover {
  transform: translateY(-2px);
}

.filter-group:hover label {
  color: #f97316;
}

.filter-group label {
  font-weight: 600;
  font-size: 13px;
  color: #374151;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 6px;
  min-height: 20px;
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
  font-size: 13px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: #374151;
  font-weight: 500;
  position: relative;
  height: 48px;
  box-sizing: border-box;
  display: flex;
  align-items: center;
}

.search-input:focus,
.filter-select:focus {
  outline: none;
  border-color: #f97316;
  background: white;
  box-shadow:
    0 0 0 4px rgba(249, 115, 22, 0.1),
    0 4px 12px rgba(0, 0, 0, 0.05);
  transform: translateY(-1px);
}

.search-input::placeholder {
  color: #9ca3af;
  font-weight: 400;
  font-size: 13px;
}

.search-input {
  line-height: 1;
}

.filter-select {
  cursor: pointer;
  appearance: none;
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 20 20'%3e%3cpath stroke='%236b7280' stroke-linecap='round' stroke-linejoin='round' stroke-width='1.5' d='m6 8 4 4 4-4'/%3e%3c/svg%3e");
  background-position: right 12px center;
  background-repeat: no-repeat;
  background-size: 16px;
  padding-right: 40px;
  line-height: 1;
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

.table-actions .action-buttons {
  display: flex;
  flex-direction: row;
  gap: 12px;
  align-items: center;
}

.btn-import-excel,
.btn-export-excel {
  background: linear-gradient(135deg, #f97316, #ea580c);
  color: white;
  border: none;
  border-radius: 8px;
  padding: 10px 16px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 2px 4px rgba(249, 115, 22, 0.3);
}

/* Excel logo styling */
.excel-logo {
  background: linear-gradient(135deg, #217346, #1e5f3a);
  color: white;
  width: 20px;
  height: 20px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 12px;
  margin-right: 4px;
  box-shadow: 0 1px 3px rgba(33, 115, 70, 0.3);
}

.btn-import-excel:hover {
  background: linear-gradient(135deg, #ea580c, #dc2626);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.4);
}

.btn-export-excel {
  background: linear-gradient(135deg, #159850, #35c371);
  box-shadow: 0 2px 4px rgba(33, 115, 70, 0.3);
}

.btn-export-excel:hover {
  background: linear-gradient(135deg, #1e5f3a, #1a4d2e);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(33, 115, 70, 0.4);
}

/* Excel logo hover effect */
.btn-import-excel:hover .excel-logo,
.btn-export-excel:hover .excel-logo {
  background: linear-gradient(135deg, #1a4d2e, #0f3d1f);
  transform: scale(1.05);
}

/* Test email button */
.btn-test-email {
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
  color: white;
  border: none;
  border-radius: 8px;
  padding: 10px 16px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 2px 4px rgba(139, 92, 246, 0.3);
}

.btn-test-email:hover {
  background: linear-gradient(135deg, #7c3aed, #6d28d9);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.4);
}

/* Checkbox styles */
.select-all-checkbox,
.employee-checkbox {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: #f97316;
  margin: 0;
  display: block;
  margin-left: auto;
  margin-right: auto;
  position: relative;
  top: 0;
  left: 0;
}

/* Ensure checkbox column has consistent width and alignment */
th:first-child,
td:first-child {
  width: 50px;
  text-align: center;
  vertical-align: middle;
  padding: 12px 4px;
  position: relative;
}

/* Column width adjustments */
th:nth-child(2), /* STT column */
td:nth-child(2) {
  width: 40px;
  text-align: center;
  padding: 8px 1px;
}

th:nth-child(3), /* Mã NV column */
td:nth-child(3) {
  width: 30px;
  text-align: center;
  padding: 8px 1px;
}

th:nth-child(4), /* Họ Tên column */
td:nth-child(4) {
  width: 200px;
  text-align: left;
  padding: 8px 12px;
  min-width: 180px;
}

/* Other columns width */
th:nth-child(5), /* SĐT column */
td:nth-child(5) {
  width: 120px;
  text-align: center;
  padding: 8px 6px;
}

th:nth-child(6), /* Email column */
td:nth-child(6) {
  width: 180px;
  text-align: left;
  padding: 8px 8px;
  min-width: 160px;
}

th:nth-child(7), /* Chức Vụ column */
td:nth-child(7) {
  width: 100px;
  text-align: center;
  padding: 8px 6px;
}

th:nth-child(8), /* Trạng Thái column */
td:nth-child(8) {
  width: 120px;
  text-align: center;
  padding: 8px 6px;
}

th:nth-child(9), /* Thao Tác column */
td:nth-child(9) {
  width: 100px;
  text-align: center;
  padding: 8px 6px;
}

/* Center checkboxes in their cells */
th:first-child {
  text-align: center;
  vertical-align: middle;
}

td:first-child {
  text-align: center;
  vertical-align: middle;
}

/* Ensure all checkboxes are perfectly aligned */
th:first-child input,
td:first-child input {
  position: relative;
  top: 0;
  left: 0;
  transform: none;
}

.selected-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  background: #f0f9ff;
  border: 1px solid #0ea5e9;
  border-radius: 8px;
  margin-left: 12px;
}

.selected-count {
  color: #0369a1;
  font-weight: 500;
  font-size: 14px;
}

.btn-bulk-action {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  color: white;
  border: none;
  border-radius: 6px;
  padding: 6px 12px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 4px;
}

.btn-bulk-action:hover {
  background: linear-gradient(135deg, #dc2626, #b91c1c);
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(239, 68, 68, 0.3);
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
  min-width: 1200px;
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

/* Column Widths */
th:nth-child(1) {
  width: 40px;
  text-align: center;
  padding: 8px 4px;
} /* STT */
th:nth-child(2) {
  width: 60px;
} /* Mã NV */
th:nth-child(3) {
  width: 100px;
} /* Họ Tên */
th:nth-child(4) {
  width: 140px;
} /* SĐT */
th:nth-child(5) {
  width: 150px;
} /* Email */
th:nth-child(6) {
  width: 150px;
} /* Chức Vụ */
th:nth-child(7) {
  width: 140px;
} /* Trạng Thái */
th:nth-child(8) {
  width: 100px;
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

tbody tr:last-child {
  border-bottom: none;
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

.toggle-container {
  display: flex;
  justify-content: center;
  align-items: center;
}

.action-buttons .toggle-container {
  margin-left: 8px;
  display: flex;
  align-items: center;
}

.action-buttons .toggle-switch {
  position: relative;
  display: inline-block;
  width: 40px;
  height: 20px;
}

.action-buttons .toggle-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.action-buttons .toggle-slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  transition: .4s;
  border-radius: 20px;
}

.action-buttons .toggle-slider:before {
  position: absolute;
  content: "";
  height: 16px;
  width: 16px;
  left: 2px;
  bottom: 2px;
  background-color: white;
  transition: .4s;
  border-radius: 50%;
}

.action-buttons .toggle-switch input:checked + .toggle-slider {
  background-color: #ff9800;
}

.action-buttons .toggle-switch input:checked + .toggle-slider:before {
  transform: translateX(20px);
}

/* Toggle Switch Styles */
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
  content: '';
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

/* Action Buttons */
table .action-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
  justify-content: center;
  min-width: 100px;
  padding: 8px;
}

.button-row {
  display: flex;
  gap: 6px;
  align-items: center;
}

.btn-view {
  width: 36px;
  height: 36px;
  border: 2px solid #3b82f6;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  font-size: 18px;
  background: #eff6ff;
  color: #3b82f6;
  box-shadow: 0 2px 4px rgba(59, 130, 246, 0.2);
}

.btn-view:hover {
  background: #dbeafe;
  border-color: #2563eb;
  color: #2563eb;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(59, 130, 246, 0.3);
}

.btn-view {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e3f2fd;
  color: #1976d2;
  font-size: 14px;
  transition: all 0.2s ease;
}

.btn-view:hover {
  background: #bbdefb;
  transform: translateY(-1px);
}

.btn-edit {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff3e0;
  color: #f57c00;
  font-size: 14px;
  transition: all 0.2s ease;
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
  justify-content: center;
  width: 40px;
  height: 40px;
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
  content: '‹';
  font-size: 16px;
  font-weight: bold;
}

.icon-chevron-right::before {
  content: '›';
  font-size: 16px;
  font-weight: bold;
}

.text-center {
  text-align: center;
  color: #6c757d;
  font-style: italic;
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
    align-items: stretch;
  }

  .table-container {
    font-size: 12px;
  }

  .search-input,
  .filter-select {
    padding: 10px 14px;
    font-size: 12px;
    line-height: 1;
  }

  th,
  td {
    padding: 12px 8px;
  }

  .action-buttons {
    gap: 4px;
  }

  .btn-view,
  .btn-edit {
    width: 32px;
    height: 32px;
    font-size: 16px;
    border-radius: 6px;
  }

  .status-badge-container {
    gap: 4px;
  }

  .status-badge {
    font-size: 10px;
    padding: 2px 8px;
    min-width: 60px;
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

  th,
  td {
    padding: 8px 4px;
  }

  .pagination-btn {
    width: 32px;
    height: 32px;
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
}
</style>
