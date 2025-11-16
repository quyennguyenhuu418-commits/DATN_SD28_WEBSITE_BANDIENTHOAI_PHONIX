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
            <p>Tìm kiếm và lọc hãng theo tiêu chí</p>
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
              placeholder="Tìm theo tên hãng, mã hãng..."
              @input="applyFilters"
              class="search-input"
            />
          </div>
          <div class="filter-group">
            <label>Trạng thái:</label>
            <select v-model="statusFilter" @change="applyFilters" class="filter-select">
              <option value="">Tất cả trạng thái</option>
              <option value="1">Hoạt động</option>
              <option value="0">Ngừng hoạt động</option>
            </select>
          </div>
        <div class="filter-group">
          <label>Loại hãng:</label>
          <select v-model="typeFilter" @change="applyFilters" class="filter-select">
            <option value="">Tất cả loại</option>
            <option value="cong_nghe">Công nghệ</option>
            <option value="dien_tu">Điện tử</option>
            <option value="xe_co">Xe cộ</option>
            <option value="thoi_trang">Thời trang</option>
            <option value="khac">Khác</option>
          </select>
        </div>
        </div>
      </div>

      <!-- Add Color Button -->
      <div class="add-customer-section">
        <div class="add-buttons">
          <button class="btn-export-excel" @click="exportToExcel()">
            <FontAwesomeIcon :icon="['fas', 'file-excel']" />
            Xuất Excel
          </button>
          <button class="btn-add-customer" @click="addHang()">
            <FontAwesomeIcon :icon="['fas', 'plus-circle']" />
            Thêm Hãng
          </button>
        </div>
      </div>

      <!-- Main Content Area -->
      <div class="main-content">
        <!-- Table Header Section -->
        <div class="table-header">
          <div class="table-title">
            <h2>Danh sách Hãng</h2>
            <span class="item-count">{{ totalItems }} hãng</span>
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
              <th>Tên Hãng</th>
              <th>Xuất xứ</th>
              <th>Mô tả</th>
              <th>Trạng Thái</th>
              <th>Thao Tác</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="(hang, index) in paginatedHangs" :key="hang.id">
              <td class="checkbox-column">
                <input
                  type="checkbox"
                  class="row-checkbox"
                  :value="hang.id"
                  :checked="selectedHangIds.includes(hang.id)"
                  @change="toggleSelectHang(hang.id)"
                />
              </td>
              <td>{{ startItem + index }}</td>
              <td>
                <div class="hang-name">
                  {{ hang.ten }}
                </div>
              </td>
              <td class="address-cell">
                <div class="address-content" :title="hang.xuatXu || '-'">
                  {{ hang.xuatXu || '-' }}
                </div>
              </td>
              <td class="address-cell">
                <div class="address-content" :title="hang.moTa || '-'">
                  {{ hang.moTa || '-' }}
                </div>
              </td>
              <td>
                <span class="status-badge" :class="hang.trangThai === 1 ? 'badge-active' : 'badge-inactive'">
                  {{ hang.trangThai === 1 ? 'Hoạt động' : 'Ngừng hoạt động' }}
                  </span>
              </td>
              <td>
                <div class="action-buttons">
                  <div class="button-row">
                    <button class="btn-view" @click="viewHang(hang)" title="Xem chi tiết">
                      <font-awesome-icon icon="eye" />
                    </button>
                    <button class="btn-edit" @click="editHang(hang)" title="Chỉnh sửa">
                      <font-awesome-icon icon="edit" />
                    </button>
                  </div>
                  <div class="toggle-container">
                    <label class="toggle-switch" :title="hang.trangThai === 1 ? 'Gạt để vô hiệu hóa' : 'Gạt để kích hoạt'">
                      <input
                        type="checkbox"
                        :checked="hang.trangThai === 1"
                        @change="toggleHangStatus(hang)"
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

    <!-- Confirm Modal -->
    <ConfirmModal
      :show="showConfirmModal"
      :title="confirmTitle"
      :message="confirmMessage"
      @confirm="handleConfirm"
      @cancel="handleCancel"
    />

    <!-- Enhanced Color Form Modal -->
    <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
      <div class="enhanced-modal">
        <div class="modal-header">
          <div class="modal-title">
            <h3>{{ editingHang ? 'Sửa Hãng' : 'Thêm Hãng Mới' }}</h3>
          </div>
          <button @click="showForm = false" class="close-btn">
            <FontAwesomeIcon :icon="['fas', 'times']" />
          </button>
        </div>

        <div class="modal-body">
          <div class="form-fields">
            <div class="field-group">
              <label class="field-label">Tên Hãng *</label>
              <input
                v-model="formData.ten"
                type="text"
                placeholder="VD: Apple"
                class="form-input"
                required
              />
            </div>

            <div class="field-group">
              <label class="field-label">Xuất xứ</label>
              <input
                v-model="formData.xuatXu"
                type="text"
                placeholder="VD: Mỹ, Hàn Quốc, Trung Quốc..."
                class="form-input"
              />
            </div>

            <div class="field-group">
              <label class="field-label">Mô tả</label>
              <textarea
                v-model="formData.moTa"
                placeholder="Mô tả về hãng..."
                class="form-textarea"
                rows="3"
              ></textarea>
            </div>

            <div class="field-group">
              <label class="checkbox-label">
                <input
                  v-model="formData.trangThai"
                  type="checkbox"
                  :true-value="1"
                  :false-value="0"
                  class="form-checkbox"
                />
                <span class="checkbox-text">Hoạt động</span>
              </label>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button @click="showForm = false" class="btn-cancel">
            <FontAwesomeIcon :icon="['fas', 'times']" />
            Hủy
          </button>
          <button @click="handleFormSubmit" class="btn-save">
            <FontAwesomeIcon :icon="['fas', 'save']" />
            {{ editingHang ? 'Cập nhật' : 'Thêm mới' }}
          </button>
        </div>
      </div>
    </div>


  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/services/api'
import Toast from '@/components/Toast.vue'
import PosHeader from '@/components/PosHeader.vue'
import ConfirmModal from '@/components/ConfirmModal.vue'
import FormModal from '@/components/FormModal.vue'
import { FontAwesomeIcon } from '@/plugins/fontawesome'
import * as XLSX from 'xlsx'

const router = useRouter()

interface Hang {
  id: number
  ten: string
  xuatXu?: string
  moTa?: string
  trangThai: number
  ngayTao?: string
  ngayCapNhat?: string
}

const hangs = ref<Hang[]>([])
const selectedHangs = ref<Hang[]>([])
const selectedHangIds = ref<number[]>([])
const loading = ref(false)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

// Filter states
const statusFilter = ref('')
const typeFilter = ref('')
const searchText = ref('')

// Pagination states
const currentPage = ref(1)
const itemsPerPage = ref(5)
const totalItems = ref(0)

// Modal states
const showForm = ref(false)
const editingHang = ref<Hang | null>(null)
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const pendingAction = ref<(() => void) | null>(null)

// Form data for enhanced modal
const formData = ref({
  ten: '',
  xuatXu: '',
  moTa: '',
  trangThai: 1
})

const hangFields = [
  { key: 'ten', label: 'Tên Hãng', type: 'text' as const, required: true },
  { key: 'xuatXu', label: 'Xuất xứ', type: 'text' as const },
  { key: 'moTa', label: 'Mô tả', type: 'textarea' as const },
  { key: 'trangThai', label: 'Hoạt động', type: 'checkbox' as const }
]

// Computed property for filtered hangs (without pagination)
const allFilteredHangs = computed(() => {
  let filtered = hangs.value

  console.log('=== FILTERING HANGS ===')
  console.log('Original hangs count:', hangs.value.length)
  console.log('Status filter value:', statusFilter.value)

  // Filter by status (active/inactive)
  if (statusFilter.value !== '') {
    console.log('Applying status filter:', statusFilter.value)
    filtered = filtered.filter(c => c.trangThai.toString() === statusFilter.value)
    console.log('After status filter:', filtered.length)
  } else {
    console.log('No status filter applied - showing all hangs')
  }

  // Filter by type (based on hang name)
  if (typeFilter.value) {
    filtered = filtered.filter(hang => {
      const name = hang.ten.toLowerCase()
      const type = typeFilter.value

      console.log('Filtering by type:', type, 'for hang:', name)

      if (type === 'cong_nghe') {
        // Công nghệ: Apple, Samsung, Xiaomi, Vivo, OPPO
        return name.includes('apple') || name.includes('samsung') || name.includes('xiaomi') || 
               name.includes('vivo') || name.includes('oppo')
      } else if (type === 'dien_tu') {
        // Điện tử: Sony, LG, Panasonic
        return name.includes('sony') || name.includes('lg') || name.includes('panasonic')
      } else if (type === 'xe_co') {
        // Xe cộ: Toyota, Honda, Ford, BMW
        return name.includes('toyota') || name.includes('honda') || name.includes('ford') || 
               name.includes('bmw')
      } else if (type === 'thoi_trang') {
        // Thời trang: Nike, Adidas, Zara
        return name.includes('nike') || name.includes('adidas') || name.includes('zara')
      } else if (type === 'khac') {
        // Khác: các hãng không thuộc các loại trên
        return !name.includes('apple') && !name.includes('samsung') && !name.includes('xiaomi') &&
               !name.includes('vivo') && !name.includes('oppo') &&
               !name.includes('sony') && !name.includes('lg') && !name.includes('panasonic') &&
               !name.includes('toyota') && !name.includes('honda') && !name.includes('ford') &&
               !name.includes('bmw') &&
               !name.includes('nike') && !name.includes('adidas') && !name.includes('zara')
      }
      return false
    })
  }

  // Search filter
  if (searchText.value) {
    const search = searchText.value.toLowerCase()
    filtered = filtered.filter(hang =>
      hang.ten?.toLowerCase().includes(search) ||
      hang.xuatXu?.toLowerCase().includes(search) ||
      hang.moTa?.toLowerCase().includes(search)
    )
  }

  totalItems.value = filtered.length
  console.log('Final filtered hangs count:', filtered.length)
  console.log('=== END FILTERING ===')
  return filtered
})

// Computed property for paginated hangs
const paginatedHangs = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return allFilteredHangs.value.slice(start, end)
})

const totalPages = computed(() => {
  return Math.ceil(totalItems.value / itemsPerPage.value)
})

const startItem = computed(() => {
  return (currentPage.value - 1) * itemsPerPage.value + 1
})

const endItem = computed(() => {
  return Math.min(currentPage.value * itemsPerPage.value, totalItems.value)
})

// Computed property for selected hangs
const selectedHangsComputed = computed(() => {
  return hangs.value.filter(hang => selectedHangIds.value.includes(hang.id))
})

// Computed property for select all checkbox
const isAllSelected = computed(() => {
  return paginatedHangs.value.length > 0 &&
    paginatedHangs.value.every(hang => selectedHangIds.value.includes(hang.id))
})

// Methods
async function loadHangs() {
  try {
    loading.value = true
    const response = await api.get('/api/hang')
    
    if (!response.data) {
      console.warn('API response data is null or undefined')
      hangs.value = []
      return
    }

    const allHangs = response.data

    // Sort hangs by creation date (newest first)
    hangs.value = allHangs.sort((a: any, b: any) => {
      const dateA = new Date(a.ngayTao || a.ngayCapNhat || 0)
      const dateB = new Date(b.ngayTao || b.ngayCapNhat || 0)
      return dateB.getTime() - dateA.getTime() // Newest first
    })

    // Debug: Log all hangs and their status
    console.log('=== LOADED HANGS ===')
    console.log('API Response:', response)
    console.log('Total hangs loaded:', hangs.value.length)
    hangs.value.forEach((hang, index) => {
      console.log(`Hang ${index}:`, {
        id: hang.id,
        idType: typeof hang.id,
        ten: hang.ten,
        trangThai: hang.trangThai,
        ngayTao: hang.ngayTao,
        ngayCapNhat: hang.ngayCapNhat
      })
    })
    console.log('Current statusFilter:', statusFilter.value)
    console.log('=== END LOADED HANGS ===')
  } catch (error) {
    console.error('Lỗi khi tải danh sách hãng:', error)
    console.error('Error details:', error.response?.data)
    toastRef.value?.error('Lỗi', 'Không thể tải danh sách hãng')
    hangs.value = []
  } finally {
    loading.value = false
  }
}

function applyFilters() {
  currentPage.value = 1
}

function clearAllFilters() {
  searchText.value = ''
  statusFilter.value = ''
  typeFilter.value = ''
  applyFilters()
}

function showInactiveHangs() {
  statusFilter.value = '0'
  currentPage.value = 1
  toastRef.value?.info('Thông báo', 'Đang hiển thị hãng ngừng hoạt động. Gạt toggle để kích hoạt lại!')
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

function getVisiblePages() {
  const pages = []
  const total = totalPages.value
  const current = currentPage.value

  // Always show pagination with ellipsis for better UX
  if (total <= 5) {
    // If total pages <= 5, show all pages
    for (let i = 1; i <= total; i++) {
      pages.push(i)
    }
  } else {
    if (current <= 3) {
      // Show: 1 2 3 ... last
      for (let i = 1; i <= 3; i++) {
        pages.push(i)
      }
      pages.push('...')
      pages.push(total)
    } else if (current >= total - 2) {
      // Show: 1 ... (last-2) (last-1) last
      pages.push(1)
      pages.push('...')
      for (let i = total - 2; i <= total; i++) {
        pages.push(i)
      }
    } else {
      // Show: 1 ... (current-1) current (current+1) ... last
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

function addHang() {
  editingHang.value = null
  resetFormData()
  showForm.value = true
}

function viewHang(hang: Hang) {
  // TODO: Implement view hang detail
  console.log('View hang:', hang)
}

function editHang(hang: Hang) {
  editingHang.value = hang
  formData.value = {
    ten: hang.ten,
    xuatXu: hang.xuatXu || '',
    moTa: hang.moTa || '',
    trangThai: hang.trangThai
  }
  showForm.value = true
}

function resetFormData() {
  formData.value = {
    ten: '',
    xuatXu: '',
    moTa: '',
    trangThai: 1
  }
}


// Simple toggle hang status
async function toggleHangStatus(hang: Hang) {
  try {
    const newStatus = hang.trangThai === 1 ? 0 : 1

    console.log('=== TOGGLE HANG STATUS ===')
    console.log('Hang ID:', hang.id)
    console.log('Hang name:', hang.ten)
    console.log('Current status:', hang.trangThai)
    console.log('New status:', newStatus)

    // Use the correct endpoint with status
    const cleanId = parseInt(String(hang.id).replace(/[^\d]/g, ''), 10)
    console.log('Clean ID for API call:', cleanId)

    const response = await api.put(`/api/hang/${cleanId}/status`, {
      trangThai: newStatus
    })

    console.log('API response:', response.status, response.data)

    if (response.data) {
      // Update local state
      hang.trangThai = newStatus

      if (newStatus === 0) {
        toastRef.value?.success('Thành công', 'Đã vô hiệu hóa hãng!')
      } else {
        toastRef.value?.success('Thành công', 'Đã kích hoạt hãng!')
      }
    } else {
      throw new Error('API response data is null')
    }
  } catch (error) {
    console.error('Lỗi khi cập nhật trạng thái:', error)
    console.error('Error details:', error.response?.data)
    toastRef.value?.error('Lỗi', 'Không thể cập nhật trạng thái hãng')
  }
}

// Checkbox functions
function toggleSelectAll() {
  if (isAllSelected.value) {
    // Unselect all hangs on current page
    const currentPageIds = paginatedHangs.value.map(c => c.id)
    selectedHangIds.value = selectedHangIds.value.filter(id => !currentPageIds.includes(id))
  } else {
    // Select all hangs on current page
    const currentPageIds = paginatedHangs.value.map(c => c.id)
    const newIds = currentPageIds.filter(id => !selectedHangIds.value.includes(id))
    selectedHangIds.value = [...selectedHangIds.value, ...newIds]
  }
}

function toggleSelectHang(hangId: number) {
  const index = selectedHangIds.value.indexOf(hangId)
  if (index > -1) {
    selectedHangIds.value.splice(index, 1)
  } else {
    selectedHangIds.value.push(hangId)
  }
}

function exportToExcel() {
  try {
    // Get selected hangs or all hangs
    const hangsToExport = selectedHangsComputed.value.length > 0 ? selectedHangsComputed.value : hangs.value

    if (hangsToExport.length === 0) {
      toastRef.value?.warning('Cảnh báo', 'Không có dữ liệu hãng để xuất Excel')
      return
    }

    // Tạo dữ liệu cho Excel
    const excelData = hangsToExport.map((hang, index) => ({
      'STT': index + 1,
      'Tên Hãng': hang.ten,
      'Xuất xứ': hang.xuatXu || '',
      'Mô tả': hang.moTa || '',
      'Trạng Thái': hang.trangThai === 1 ? 'Hoạt động' : 'Ngừng hoạt động',
      'Ngày Tạo': hang.ngayTao ? new Date(hang.ngayTao).toLocaleDateString('vi-VN') : '',
      'Ngày Cập Nhật': hang.ngayCapNhat ? new Date(hang.ngayCapNhat).toLocaleDateString('vi-VN') : ''
    }))

    // Tạo workbook và worksheet
    const wb = XLSX.utils.book_new()
    const ws = XLSX.utils.json_to_sheet(excelData)

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

    // Thêm worksheet vào workbook
    XLSX.utils.book_append_sheet(wb, ws, 'Danh sách Hãng')

    // Xuất file Excel
    const fileName = `danh_sach_hang_${new Date().toISOString().split('T')[0]}.xlsx`
    XLSX.writeFile(wb, fileName)

    toastRef.value?.success('Thành công', 'Đã export danh sách hãng thành công!')
  } catch (error) {
    console.error('Lỗi khi export Excel:', error)
    toastRef.value?.error('Lỗi', 'Có lỗi xảy ra khi export Excel')
  }
}


function handleFormSubmit() {
  // Validate form data
  if (!formData.value.ten) {
    toastRef.value?.error('Lỗi', 'Vui lòng điền đầy đủ thông tin bắt buộc')
    return
  }

  if (editingHang.value) {
    updateHang(editingHang.value.id, formData.value)
  } else {
    createHang(formData.value)
  }
}

async function createHang(formData: any) {
  try {
    console.log('Creating hang with data:', formData)
    const response = await api.post('/api/hang', formData)
    console.log('API response:', response.data)
    
    if (response.data) {
      await loadHangs()
      showForm.value = false
      resetFormData()
      toastRef.value?.success('Thành công', 'Đã thêm hãng mới')
    } else {
      throw new Error('API response data is null')
    }
  } catch (error) {
    console.error('Error creating hang:', error)
    console.error('Error response:', error.response?.data)
    toastRef.value?.error('Lỗi tạo hãng', 'Không thể tạo hãng mới')
  }
}

async function updateHang(id: number, formData: any) {
  try {
    const response = await api.put(`/api/hang/${id}`, formData)
    console.log('Update response:', response.data)
    
    if (response.data) {
      await loadHangs()
      showForm.value = false
      toastRef.value?.success('Thành công', 'Đã cập nhật hãng')
    } else {
      throw new Error('API response data is null')
    }
  } catch (error) {
    console.error('Error updating hang:', error)
    console.error('Error response:', error.response?.data)
    toastRef.value?.error('Lỗi cập nhật', 'Không thể cập nhật hãng')
  }
}

function handleConfirm() {
  if (pendingAction.value) {
    pendingAction.value()
    pendingAction.value = null
  }
  showConfirmModal.value = false
}

function handleCancel() {
  pendingAction.value = null
  showConfirmModal.value = false
}

// Lifecycle
onMounted(() => {
  loadHangs()
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

/* Add Color Section */
.add-customer-section {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 24px;
  margin-top: 0;
}

.add-buttons {
  display: flex;
  gap: 12px;
  align-items: center;
}

.btn-export-excel {
  background: #28a745;
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
  box-shadow: 0 2px 4px rgba(40, 167, 69, 0.3);
  margin-right: 12px;
}

.btn-export-excel:hover {
  background: #218838;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(40, 167, 69, 0.4);
}


.btn-add-customer {
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

.btn-add-customer:hover {
  background: #ea580c;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.4);
}

/* FontAwesome icon styles */
.btn-export-excel FontAwesomeIcon {
  color: white;
  margin-right: 4px;
}

.btn-add-customer FontAwesomeIcon {
  color: white;
  margin-right: 4px;
}

.btn-scan-quick {
  background: #10b981;
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
  box-shadow: 0 2px 4px rgba(16, 185, 129, 0.3);
}

.btn-scan-quick:hover {
  background: #059669;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.4);
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
  flex-wrap: wrap;
  gap: 16px;
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
  flex-wrap: wrap;
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
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
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

.btn-export-excel FontAwesomeIcon {
  font-size: 16px;
  color: #28a745;
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
}

table {
  width: 100%;
  min-width: 1200px;
  border-collapse: collapse;
  background: white;
  table-layout: auto;
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

/* Cột mã màu vừa đủ */
th:nth-child(3),
td:nth-child(3) {
  width: 100px !important;
  max-width: 100px !important;
  min-width: 100px !important;
  padding: 12px 8px !important;
  font-size: 13px !important;
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
th:nth-child(3) { width: 200px; } /* Mã Màu */
th:nth-child(4) { width: 200px; } /* Tên Màu */
th:nth-child(5) { width: 160px; } /* Mã Hex */
th:nth-child(6) { width: 200px; } /* Mô tả */
th:nth-child(7) { width: 140px; } /* Trạng Thái */
th:nth-child(8) { width: 100px; } /* Thao Tác */

/* Address cell styling */
.address-cell {
  max-width: 200px;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

.address-content {
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: help;
}

.address-content:hover {
  white-space: normal;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

/* Special border for action column */
th:nth-child(8),
td:nth-child(8) {
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

th:nth-child(8),
td:nth-child(8) {
  white-space: nowrap;
  overflow: visible;
  text-align: center;
  padding: 8px 12px;
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

.toggle-container {
  display: flex;
  justify-content: center;
  align-items: center;
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

/* Action Buttons */
.action-buttons {
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
  justify-content: center;
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

/* Hang Name Styles */
.hang-name {
  font-weight: 600;
  color: #1e293b;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px;
}

/* Color Info Styles */
.customer-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.customer-avatar {
  width: 40px;
  height: 40px;
  background: #e2e8f0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b7280;
  font-size: 16px;
  flex-shrink: 0;
}

.hang-avatar {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  color: white;
  border: 2px solid #3b82f6;
}

.customer-details {
  min-width: 0;
  flex: 1;
}

.customer-name {
  font-weight: 600;
  color: #1e293b;
  font-size: 14px;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.customer-email {
  font-size: 12px;
  color: #6b7280;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.hex-code {
  font-family: 'Courier New', monospace;
  color: #666;
  font-size: 14px;
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
@media (max-width: 1400px) {
  .filter-row {
    grid-template-columns: repeat(5, 1fr);
  }

  .filter-actions {
    flex-wrap: wrap;
  }
}

@media (max-width: 1200px) {
  .filter-row {
    grid-template-columns: repeat(4, 1fr);
  }

  .filter-header {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-actions {
    justify-content: flex-end;
  }
}

@media (max-width: 1000px) {
  .filter-row {
    grid-template-columns: repeat(3, 1fr);
  }

  .filter-section {
    padding: 20px;
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
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .table-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .table-container {
    font-size: 12px;
  }

  th, td {
    padding: 12px 8px;
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

  .pagination-section {
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

/* Enhanced Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.enhanced-modal {
  background: white;
  border-radius: 20px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  width: 90%;
  max-width: 800px;
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  animation: modalSlideIn 0.3s ease-out;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-50px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-header {
  background: linear-gradient(135deg, #f97316, #ea580c);
  color: white;
  padding: 24px 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
}

.modal-header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #f97316, #ea580c, #dc2626);
}

.modal-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  font-size: 24px;
  background: rgba(255, 255, 255, 0.2);
  padding: 8px;
  border-radius: 12px;
  backdrop-filter: blur(10px);
}

.modal-title h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}

.close-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  backdrop-filter: blur(10px);
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

.modal-body {
  padding: 32px;
  flex: 1;
  overflow-y: auto;
}

.form-grid {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.input-with-button {
  display: flex;
  gap: 8px;
  align-items: center;
}

.btn-generate {
  background: #f97316;
  color: white;
  border: none;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  min-width: 40px;
  height: 40px;
}

.btn-generate:hover {
  background: #ea580c;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.3);
}

.color-square-btn {
  width: 40px;
  height: 40px;
  border: 2px solid #ddd;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.color-square-btn:hover {
  border-color: #f97316;
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.form-fields {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.field-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field-group.full-width {
  grid-column: 1 / -1;
}

.field-label {
  font-weight: 600;
  color: #374151;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.form-input,
.form-textarea {
  padding: 12px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  font-size: 14px;
  transition: all 0.3s ease;
  background: #fafbfc;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: #f97316;
  background: white;
  box-shadow: 0 0 0 4px rgba(249, 115, 22, 0.1);
}

.hex-input-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.hex-input {
  flex: 1;
  font-family: 'Courier New', monospace;
}

.hex-preview {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  border: 2px solid #ddd;
  flex-shrink: 0;
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 12px 16px;
  background: #f8fafc;
  border-radius: 12px;
  border: 2px solid #e5e7eb;
  transition: all 0.3s ease;
}

.checkbox-label:hover {
  background: #fef3c7;
  border-color: #f97316;
}

.form-checkbox {
  width: 18px;
  height: 18px;
  accent-color: #f97316;
  cursor: pointer;
}

.checkbox-text {
  font-weight: 500;
  color: #374151;
}

.modal-footer {
  padding: 24px 32px;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn-cancel,
.btn-save {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s ease;
}

.btn-cancel {
  background: #f3f4f6;
  color: #6b7280;
  border: 2px solid #e5e7eb;
}

.btn-cancel:hover {
  background: #e5e7eb;
  color: #374151;
  transform: translateY(-1px);
}


.btn-save {
  background: linear-gradient(135deg, #f97316, #ea580c);
  color: white;
  box-shadow: 0 4px 15px rgba(249, 115, 22, 0.3);
}

.btn-save:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(249, 115, 22, 0.4);
}

/* Responsive Modal */
@media (max-width: 768px) {
  .enhanced-modal {
    width: 95%;
    margin: 20px;
  }

  .modal-header {
    padding: 20px 24px;
  }

  .modal-body {
    padding: 24px;
  }

  .form-grid {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .color-picker-section {
    padding: 20px;
  }

  .modal-footer {
    padding: 20px 24px;
    flex-direction: column;
  }

  .btn-cancel,
  .btn-save {
    width: 100%;
    justify-content: center;
  }
}
</style>
