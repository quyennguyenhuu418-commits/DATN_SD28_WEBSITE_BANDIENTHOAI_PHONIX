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
          <p>Tìm kiếm và lọc chip theo tiêu chí</p>
        </div>
        <div class="filter-actions">
          <button class="btn-clear-filters" @click="clearAllFilters">
            <FontAwesomeIcon :icon="['fas', 'refresh']" />
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
            placeholder="Tìm theo tên chip, mã chip..."
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
          <label>Hãng sản xuất:</label>
          <select v-model="brandFilter" @change="applyFilters" class="filter-select">
            <option value="">Tất cả hãng</option>
            <option value="S">Snapdragon</option>
            <option value="M">MediaTek</option>
            <option value="E">Exynos</option>
            <option value="A">Apple</option>
            <option value="K">Kirin</option>
            <option value="O">Khác</option>
          </select>
        </div>
      </div>
    </div>

    <!-- Add Chip Button -->
    <div class="add-customer-section">
      <div class="add-buttons">
        <button class="btn-export-excel" @click="exportToExcel()">
          <FontAwesomeIcon :icon="['fas', 'file-excel']" />
          Xuất Excel
        </button>
        <button class="btn-add-customer" @click="addChip()">
          <FontAwesomeIcon :icon="['fas', 'microchip']" />
          Thêm Chip
        </button>
      </div>
    </div>

    <!-- Main Content Area -->
    <div class="main-content">
      <!-- Table Header Section -->
      <div class="table-header">
        <div class="table-title">
          <h2>Danh sách Chip</h2>
          <span class="item-count">{{ totalItems }} chip</span>
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
                  @change="toggleSelectAll"
                  :checked="selectedItems.length === paginatedChips.length && paginatedChips.length > 0"
                />
              </th>
              <th>STT</th>
              <th>Mã Chip</th>
              <th>Tên Chip</th>
              <th>Mô Tả</th>
              <th>Trạng Thái</th>
              <th>Thao Tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(chip, index) in paginatedChips" :key="chip.id" class="table-row">
              <td class="checkbox-column">
                <input 
                  type="checkbox" 
                  class="row-checkbox" 
                  :value="chip.id"
                  v-model="selectedItems"
                />
              </td>
              <td>{{ (currentPage - 1) * itemsPerPage + index + 1 }}</td>
              <td>
                <span class="code-text">{{ chip.maChip }}</span>
              </td>
              <td>
                <div class="customer-info">
                  <div class="customer-avatar chip-avatar">
                    <FontAwesomeIcon :icon="['fas', 'microchip']" />
                  </div>
                  <div class="customer-details">
                    <div class="customer-name">{{ chip.tenChip }}</div>
                  </div>
                </div>
              </td>
              <td>
                <div class="description-cell">
                  {{ chip.moTa || 'Chưa có mô tả' }}
                </div>
              </td>
              <td>
                <div class="status-cell">
                  <span 
                    class="status-badge" 
                    :class="chip.trangThai === 1 ? 'status-active' : 'status-inactive'"
                  >
                    {{ chip.trangThai === 1 ? 'HOẠT ĐỘNG' : 'Đã kết thúc' }}
                  </span>
                </div>
              </td>
              <td>
                <div class="action-buttons">
                  <div class="button-row">
                    <button @click="viewChip(chip)" class="btn-view" title="Xem chi tiết">
                      <FontAwesomeIcon :icon="['fas', 'eye']" />
                    </button>
                    <button @click="editChip(chip)" class="btn-edit" title="Chỉnh sửa">
                      <FontAwesomeIcon :icon="['fas', 'edit']" />
                    </button>
                  </div>
                  <label class="toggle-switch">
                    <input 
                      type="checkbox" 
                      :checked="chip.trangThai === 1"
                      @change="toggleChipStatus(chip)"
                    />
                    <span class="toggle-slider"></span>
                  </label>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="pagination">
        <div class="pagination-info">
          <span>Hiển thị {{ (currentPage - 1) * itemsPerPage + 1 }} - {{ Math.min(currentPage * itemsPerPage, totalItems) }} trong {{ totalItems }} kết quả</span>
        </div>
        <div class="pagination-controls">
          <button 
            @click="goToPage(currentPage - 1)" 
            :disabled="currentPage === 1"
            class="pagination-btn"
          >
            <FontAwesomeIcon :icon="['fas', 'chevron-left']" />
          </button>
          
          <template v-for="page in getVisiblePages()" :key="page">
            <button 
              v-if="page !== '...'"
              @click="goToPage(page)"
              :class="['pagination-btn', { active: page === currentPage }]"
            >
              {{ page }}
            </button>
            <span v-else class="pagination-ellipsis">...</span>
          </template>
          
          <button 
            @click="goToPage(currentPage + 1)" 
            :disabled="currentPage === totalPages"
            class="pagination-btn"
          >
            <FontAwesomeIcon :icon="['fas', 'chevron-right']" />
          </button>
        </div>
      </div>
    </div>

    <!-- Enhanced Modal -->
    <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
      <div class="enhanced-modal modalSlideIn">
        <div class="modal-header">
          <div class="title-icon">⚙️</div>
          <h3>{{ editingChip ? 'Sửa Chip' : 'Thêm Chip Mới' }}</h3>
          <button @click="showForm = false" class="close-btn">
            <FontAwesomeIcon :icon="['fas', 'times']" />
          </button>
        </div>

        <div class="modal-body">
          <div class="form-grid">
            <div class="form-fields">
              <div class="form-group">
                <label class="form-label">Mã Chip <span class="required">*</span></label>
                <div class="input-group">
                  <input 
                    v-model="formData.maChip" 
                    type="text" 
                    placeholder="VD: CHIP001"
                    class="form-input"
                    required
                  />
                  <button @click="generateMaChipAuto" class="btn-auto-generate" title="Tự động tạo mã">
                    <FontAwesomeIcon :icon="['fas', 'magic']" />
                  </button>
                </div>
              </div>

              <div class="form-group">
                <label class="form-label">Tên Chip <span class="required">*</span></label>
                <input 
                  v-model="formData.tenChip" 
                  type="text" 
                  placeholder="Nhập tên chip"
                  class="form-input"
                  required
                />
              </div>


              <div class="form-group">
                <label class="form-label">Mô Tả</label>
                <textarea 
                  v-model="formData.moTa" 
                  placeholder="Mô tả về chip..."
                  class="form-textarea"
                  rows="3"
                ></textarea>
              </div>

              <div class="form-group">
                <div class="checkbox-group">
                  <input 
                    type="checkbox" 
                    id="trangThai" 
                    v-model="formData.trangThai"
                    :true-value="1"
                    :false-value="0"
                    class="checkbox-input"
                  />
                  <label for="trangThai" class="checkbox-label">
                    <span class="checkbox-custom"></span>
                    Chip đang hoạt động
                  </label>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button @click="showForm = false" class="btn-cancel">
            <FontAwesomeIcon :icon="['fas', 'times']" />
            Hủy
          </button>
          <button @click="testCreateChip" class="btn-test" v-if="!editingChip">
            <FontAwesomeIcon :icon="['fas', 'flask']" />
            Test
          </button>
          <button @click="handleFormSubmit" class="btn-save">
            <FontAwesomeIcon :icon="['fas', 'save']" />
            {{ editingChip ? 'Cập nhật' : 'Thêm mới' }}
          </button>
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
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'

const router = useRouter()
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

// Data
interface Chip {
  id: number
  maChip: string
  tenChip: string
  moTa?: string
  trangThai: number
  ngayTao?: string
  ngayCapNhat?: string
}

const chips = ref<Chip[]>([])
const selectedItems = ref<number[]>([])
const showForm = ref(false)
const editingChip = ref<Chip | null>(null)
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const confirmAction = ref<(() => void) | null>(null)

// Form data
const formData = ref({
  maChip: '',
  tenChip: '',
  moTa: '',
  trangThai: 1
})

// Filter states
const statusFilter = ref('')
const brandFilter = ref('')
const searchText = ref('')

// Pagination states
const currentPage = ref(1)
const itemsPerPage = ref(5)
const totalItems = ref(0)

// Computed properties
const totalPages = computed(() => Math.ceil(totalItems.value / itemsPerPage.value))

const filteredChips = computed(() => {
  let filtered = [...chips.value]

  // Status filter
  if (statusFilter.value !== '') {
    filtered = filtered.filter(chip => chip.trangThai.toString() === statusFilter.value)
  }

  // Brand filter - phân loại theo chữ đầu của tên chip
  if (brandFilter.value) {
    filtered = filtered.filter(chip => {
      const firstLetter = chip.tenChip?.charAt(0).toUpperCase()
      return firstLetter === brandFilter.value
    })
  }

  // Search filter
  if (searchText.value) {
    const search = searchText.value.toLowerCase()
    filtered = filtered.filter(chip => 
      chip.tenChip?.toLowerCase().includes(search) ||
      chip.maChip?.toLowerCase().includes(search)
    )
  }

  return filtered
})

const paginatedChips = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return filteredChips.value.slice(start, end)
})

// Methods
async function loadChips() {
  try {
    const response = await api.get('/api/chip')
    chips.value = response.data
    totalItems.value = filteredChips.value.length
  } catch (error) {
    console.error('Error loading chips:', error)
    toastRef.value?.error('Lỗi tải dữ liệu', 'Không thể tải danh sách chip')
  }
}

function addChip() {
  resetFormData()
  editingChip.value = null
  showForm.value = true
}

function editChip(chip: Chip) {
  editingChip.value = chip
  formData.value = {
    maChip: chip.maChip,
    tenChip: chip.tenChip,
    moTa: chip.moTa || '',
    trangThai: chip.trangThai
  }
  showForm.value = true
}

function viewChip(chip: Chip) {
  console.log('View chip:', chip)
  toastRef.value?.info('Thông báo', `Xem chi tiết chip: ${chip.tenChip}`)
}

function resetFormData() {
  formData.value = {
    maChip: '',
    tenChip: '',
    moTa: '',
    trangThai: 1
  }
}

// Test function to create a chip with sample data
function testCreateChip() {
  formData.value = {
    maChip: 'CHIP001',
    tenChip: 'Snapdragon 8 Gen 2',
    moTa: 'Chip xử lý cao cấp từ Qualcomm',
    trangThai: 1
  }
  
  console.log('Test data:', formData.value)
  handleFormSubmit()
}

// Function to generate maChip automatically
function generateMaChipAuto() {
  if (formData.value.tenChip) {
    const words = formData.value.tenChip.toLowerCase().split(' ')
    const prefix = words.map(word => word.charAt(0)).join('').toUpperCase()
    const timestamp = Date.now().toString().slice(-3)
    formData.value.maChip = `CHIP${prefix}${timestamp}`
  } else {
    // Generate random code if no name
    const randomCode = Math.random().toString(36).substring(2, 5).toUpperCase()
    const timestamp = Date.now().toString().slice(-3)
    formData.value.maChip = `CHIP${randomCode}${timestamp}`
  }
}

function handleFormSubmit() {
  // Validate form data
  if (!formData.value.maChip || !formData.value.tenChip) {
    toastRef.value?.error('Lỗi', 'Vui lòng điền đầy đủ thông tin bắt buộc')
    return
  }
  
  if (editingChip.value) {
    updateChip(editingChip.value.id, formData.value)
  } else {
    createChip(formData.value)
  }
}

async function createChip(formData: any) {
  try {
    console.log('Creating chip with data:', formData)
    const response = await api.post('/api/chip', formData)
    console.log('API response:', response.data)
    await loadChips()
    showForm.value = false
    resetFormData()
    toastRef.value?.success('Thành công', 'Đã thêm chip mới')
  } catch (error) {
    console.error('Error creating chip:', error)
    console.error('Error response:', error.response?.data)
    toastRef.value?.error('Lỗi tạo chip', 'Không thể tạo chip mới')
  }
}

async function updateChip(id: number, formData: any) {
  try {
    await api.put(`/api/chip/${id}`, formData)
    await loadChips()
    showForm.value = false
    resetFormData()
    toastRef.value?.success('Thành công', 'Đã cập nhật chip')
  } catch (error) {
    console.error('Error updating chip:', error)
    toastRef.value?.error('Lỗi cập nhật', 'Không thể cập nhật chip')
  }
}

async function deleteChip(chip: Chip) {
  confirmTitle.value = 'Xác nhận xóa'
  confirmMessage.value = `Bạn có chắc chắn muốn xóa chip "${chip.tenChip}"?`
  confirmAction.value = async () => {
    try {
      await api.delete(`/api/chip/${chip.id}`)
      await loadChips()
      toastRef.value?.success('Thành công', 'Đã xóa chip')
    } catch (error) {
      console.error('Error deleting chip:', error)
      toastRef.value?.error('Lỗi xóa', 'Không thể xóa chip')
    }
  }
  showConfirmModal.value = true
}

async function toggleChipStatus(chip: Chip) {
  try {
    const newStatus = chip.trangThai === 1 ? 0 : 1
    await api.put(`/api/chip/${chip.id}/status`, { trangThai: newStatus })
    await loadChips()
    toastRef.value?.success('Thành công', `Đã ${newStatus === 1 ? 'kích hoạt' : 'ngừng hoạt động'} chip`)
  } catch (error) {
    console.error('Error toggling chip status:', error)
    toastRef.value?.error('Lỗi', 'Không thể thay đổi trạng thái chip')
  }
}

function exportToExcel() {
  console.log('Exporting to Excel...')
  toastRef.value?.info('Thông báo', 'Tính năng xuất Excel đang được phát triển')
}


function toggleSelectAll() {
  if (selectedItems.value.length === paginatedChips.value.length) {
    selectedItems.value = []
  } else {
    selectedItems.value = paginatedChips.value.map(chip => chip.id)
  }
}

function applyFilters() {
  currentPage.value = 1
  totalItems.value = filteredChips.value.length
}

function clearAllFilters() {
  searchText.value = ''
  statusFilter.value = ''
  brandFilter.value = ''
  applyFilters()
}

function goToPage(page: number) {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

function getVisiblePages() {
  const pages = []
  const total = totalPages.value
  const current = currentPage.value
  
  if (total <= 7) {
    for (let i = 1; i <= total; i++) {
      pages.push(i)
    }
    } else {
    pages.push(1)
    
    if (current > 4) {
      pages.push('...')
    }
    
    const start = Math.max(2, current - 1)
    const end = Math.min(total - 1, current + 1)
    
    for (let i = start; i <= end; i++) {
      pages.push(i)
    }
    
    if (current < total - 3) {
      pages.push('...')
    }
    
    pages.push(total)
  }
  
  return pages
}

function handleConfirm() {
  if (confirmAction.value) {
    confirmAction.value()
  }
  showConfirmModal.value = false
  confirmAction.value = null
}

function handleCancel() {
  showConfirmModal.value = false
  confirmAction.value = null
}

// Lifecycle
onMounted(() => {
  loadChips()
})

// Watch for filter changes
watch([searchText, statusFilter, brandFilter], () => {
  applyFilters()
})
</script>

<style scoped>
/* Modern Chip Management Theme */
.page {
  background: #f8fafc;
  min-height: 100vh;
  padding: 0;
  padding-top: 80px;
  width: 100%;
  overflow-x: hidden;
  position: relative;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.dark-mode-transition {
  transition: background-color 0.3s ease, color 0.3s ease;
}

.content {
  padding: 24px;
  padding-top: 0px;
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

/* Add Chip Section */
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
  color: #6b7280;
}

.page-size-select {
  padding: 6px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  background: white;
}

/* Table */
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
th:nth-child(3) { width: 200px; } /* Mã Chip */
th:nth-child(4) { width: 200px; } /* Tên Chip */
th:nth-child(5) { width: 160px; } /* Loại */
th:nth-child(6) { width: 200px; } /* Mô tả */
th:nth-child(7) { width: 140px; } /* Trạng Thái */
th:nth-child(8) { width: 100px; } /* Thao Tác */

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

.chip-avatar {
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
  color: white;
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

.code-text {
  font-family: 'Courier New', monospace;
  color: #666;
  font-size: 14px;
  background: #f3f4f6;
  padding: 4px 8px;
  border-radius: 4px;
}


.description-cell {
  max-width: 200px;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

.description-content {
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: help;
  color: #6b7280;
  font-size: 14px;
}

.description-content:hover {
  white-space: normal;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

.status-cell {
  display: flex;
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

.status-active {
  background: #dcfce7;
  color: #166534;
}

.status-inactive {
  background: #fef2f2;
  color: #dc2626;
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
  background-color: #ccc;
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
}

.toggle-switch input:checked + .toggle-slider {
  background-color: #f97316;
}

.toggle-switch input:checked + .toggle-slider:before {
  transform: translateX(26px);
}

.toggle-switch:hover .toggle-slider {
  box-shadow: 0 0 8px rgba(249, 115, 22, 0.3);
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
  justify-content: center;
}

.button-row {
  display: flex;
  gap: 6px;
  align-items: center;
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


/* Pagination */
.pagination {
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

.pagination-btn.active {
  background: #f97316;
  color: white;
  border-color: #f97316;
  box-shadow: 0 2px 4px rgba(249, 115, 22, 0.3);
}

.pagination-ellipsis {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  color: #6b7280;
  font-size: 14px;
}

/* Enhanced Modal */
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

.title-icon {
  font-size: 24px;
  background: rgba(255, 255, 255, 0.2);
  padding: 8px;
  border-radius: 12px;
  backdrop-filter: blur(10px);
}

.modal-header h3 {
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

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-weight: 600;
  color: #374151;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.required {
  color: #dc2626;
}

.input-group {
  display: flex;
  gap: 8px;
  align-items: center;
}

.form-input,
.form-textarea {
  flex: 1;
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

.btn-auto-generate {
  background: #f97316;
  color: white;
  border: none;
  border-radius: 12px;
  padding: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 40px;
  height: 40px;
}

.btn-auto-generate:hover {
  background: #ea580c;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.3);
}

.checkbox-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.checkbox-input {
  display: none;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #374151;
  padding: 12px 16px;
  background: #f8fafc;
  border-radius: 12px;
  border: 2px solid #e5e7eb;
  transition: all 0.3s ease;
}

.checkbox-label:hover {
  background: #fff7ed;
  border-color: #f97316;
}

.checkbox-custom {
  width: 20px;
  height: 20px;
  border: 2px solid #d1d5db;
  border-radius: 4px;
  position: relative;
  transition: all 0.2s ease;
}

.checkbox-input:checked + .checkbox-label .checkbox-custom {
  background: #f97316;
  border-color: #f97316;
}

.checkbox-input:checked + .checkbox-label .checkbox-custom::after {
  content: '✓';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: white;
  font-size: 12px;
  font-weight: bold;
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
.btn-test,
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

.btn-test {
  background: #f97316;
  color: white;
  box-shadow: 0 4px 15px rgba(249, 115, 22, 0.3);
}

.btn-test:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(249, 115, 22, 0.4);
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
  
  .pagination {
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
  
  .pagination {
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

@media (max-width: 768px) {
  .content {
    padding: 16px;
  }
  
  .filter-section {
    padding: 20px;
  }
  
  .add-buttons {
    flex-direction: column;
  }
  
  .btn-export-excel,
  .btn-add-customer {
    width: 100%;
    justify-content: center;
  }
  
  .table-container {
    overflow-x: auto;
  }
  
  table {
    min-width: 600px;
  }
  
  th, td {
    padding: 12px 8px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
}
</style>