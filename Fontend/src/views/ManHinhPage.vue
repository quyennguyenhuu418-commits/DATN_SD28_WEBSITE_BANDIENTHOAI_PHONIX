<template>
<<<<<<< HEAD
  <div class="page dark-mode-transition">
    <PosHeader />

    <div class="content">

      <Toast ref="toastRef" />

      <!-- Filter Section -->
      <div class="filter-section">
        <div class="filter-header">
          <div class="filter-title">
            <h3>Bộ lọc nâng cao</h3>
            <p>Tìm kiếm và lọc màn hình theo tiêu chí</p>
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
              placeholder="Tìm theo tên màn hình, mã màn hình..."
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
            <label>Loại màn hình:</label>
            <select v-model="typeFilter" @change="applyFilters" class="filter-select">
              <option value="">Tất cả loại</option>
              <option value="lcd">LCD</option>
              <option value="oled">OLED</option>
              <option value="amoled">AMOLED</option>
              <option value="ips">IPS</option>
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
          <button class="btn-add-customer" @click="addManHinh()">
            <FontAwesomeIcon :icon="['fas', 'plus-circle']" />
            Thêm Màn Hình
          </button>
        </div>
      </div>

      <!-- Main Content Area -->
      <div class="main-content">
        <!-- Table Header Section -->
        <div class="table-header">
          <div class="table-title">
            <h2>Danh sách Màn Hình</h2>
            <span class="item-count">{{ totalItems }} màn hình</span>
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
              <th>Mã MH</th>
              <th>Kích thước</th>
              <th>Công nghệ</th>
              <th>Độ phân giải</th>
              <th>Tần số quét</th>
              <th>Kiểu màn hình</th>
              <th>Mô tả</th>
              <th>Trạng Thái</th>
              <th>Thao Tác</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="(manHinh, index) in paginatedManHinhs" :key="manHinh.id">
              <td class="checkbox-column">
                <input
                  type="checkbox"
                  class="row-checkbox"
                  :value="manHinh.id"
                  :checked="selectedManHinhIds.includes(manHinh.id)"
                  @change="toggleSelectManHinh(manHinh.id)"
                />
              </td>
              <td>{{ startItem + index }}</td>
              <td>{{ manHinh.maManHinh }}</td>
              <td>
                <div class="customer-info">
                  <div class="customer-avatar screen-avatar">
                    <FontAwesomeIcon :icon="['fas', 'tv']" />
                  </div>
                  <div class="customer-details">
                    <div class="customer-name">{{ manHinh.kichThuoc }}</div>
                    <div class="customer-email">{{ manHinh.congNghe || 'Chưa có công nghệ' }}</div>
                  </div>
                </div>
              </td>
              <td class="address-cell">
                <div class="address-content" :title="manHinh.congNghe || '-'">
                  {{ manHinh.congNghe || '-' }}
                </div>
              </td>
              <td class="address-cell">
                <div class="address-content" :title="manHinh.doPhanGiai || '-'">
                  {{ manHinh.doPhanGiai || '-' }}
                </div>
              </td>
              <td class="address-cell">
                <div class="address-content" :title="manHinh.tanSoQuet || '-'">
                  {{ manHinh.tanSoQuet || '-' }}
                </div>
              </td>
              <td class="address-cell">
                <div class="address-content" :title="manHinh.kieuManHinh || '-'">
                  {{ manHinh.kieuManHinh || '-' }}
                </div>
              </td>
              <td class="address-cell">
                <div class="address-content" :title="manHinh.moTa || '-'">
                  {{ manHinh.moTa || '-' }}
                </div>
              </td>
              <td>
                <span class="status-badge" :class="manHinh.trangThai === 1 ? 'badge-active' : 'badge-inactive'">
                  {{ manHinh.trangThai === 1 ? 'Hoạt động' : 'Ngừng hoạt động' }}
                </span>
              </td>
              <td>
                <div class="action-buttons">
                  <div class="button-row">
                    <button class="btn-view" @click="viewManHinh(manHinh)" title="Xem chi tiết">
                      <font-awesome-icon icon="eye" />
                    </button>
                    <button class="btn-edit" @click="editManHinh(manHinh)" title="Chỉnh sửa">
                      <font-awesome-icon icon="edit" />
                    </button>
                  </div>
                  <div class="toggle-container">
                    <label class="toggle-switch" :title="manHinh.trangThai === 1 ? 'Gạt để vô hiệu hóa' : 'Gạt để kích hoạt'">
                      <input
                        type="checkbox"
                        :checked="manHinh.trangThai === 1"
                        @change="toggleManHinhStatus(manHinh)"
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
            <h3>{{ editingManHinh ? 'Sửa Màn Hình' : 'Thêm Màn Hình Mới' }}</h3>
          </div>
          <button @click="showForm = false" class="close-btn">
            <FontAwesomeIcon :icon="['fas', 'times']" />
          </button>
        </div>

        <div class="modal-body">
          <div class="form-fields">
            <div class="field-group">
              <label class="field-label">Mã Màn Hình *</label>
              <div class="input-with-button">
                <input
                  v-model="formData.maManHinh"
                  type="text"
                  placeholder="VD: MH001"
                  class="form-input"
                  required
                />
                <button @click="generateMaManHinhAuto" class="btn-generate" title="Tự động tạo mã">
                  <FontAwesomeIcon :icon="['fas', 'magic']" />
                </button>
              </div>
            </div>

            <div class="field-group">
              <label class="field-label">Kích thước *</label>
              <input
                v-model="formData.kichThuoc"
                type="text"
                placeholder="VD: 6.1 inch"
                class="form-input"
                required
              />
            </div>

            <div class="field-group">
              <label class="field-label">Công nghệ</label>
              <input
                v-model="formData.congNghe"
                type="text"
                placeholder="VD: OLED"
                class="form-input"
              />
            </div>

            <div class="field-group">
              <label class="field-label">Độ phân giải</label>
              <input
                v-model="formData.doPhanGiai"
                type="text"
                placeholder="VD: 1080x2400"
                class="form-input"
              />
            </div>

            <div class="field-group">
              <label class="field-label">Tần số quét</label>
              <input
                v-model="formData.tanSoQuet"
                type="text"
                placeholder="VD: 120Hz"
                class="form-input"
              />
            </div>

            <div class="field-group">
              <label class="field-label">Kiểu màn hình</label>
              <input
                v-model="formData.kieuManHinh"
                type="text"
                placeholder="VD: Dynamic AMOLED"
                class="form-input"
              />
            </div>

            <div class="field-group">
              <label class="field-label">Mô tả</label>
              <textarea
                v-model="formData.moTa"
                placeholder="Mô tả về màn hình..."
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
          <button @click="testCreateCpu" class="btn-test" v-if="!editingCpu">
            <FontAwesomeIcon :icon="['fas', 'flask']" />
            Test
          </button>
          <button @click="handleFormSubmit" class="btn-save">
            <FontAwesomeIcon :icon="['fas', 'save']" />
            {{ editingCpu ? 'Cập nhật' : 'Thêm mới' }}
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
=======
  <AdminTable
    :data="manHinhs"
    :columns="columns"
    title="Danh Sách Màn Hình"
    titleIcon="📱"
    entityName="Màn hình"
    searchPlaceholder="Tìm kiếm theo kích thước màn hình..."
    @openForm="openForm"
    @exportExcel="exportExcel"
    @toggleStatus="toggleManHinhStatus"
  />

  <!-- Confirm Modal -->
  <ConfirmModal
    :show="showConfirmModal"
    :title="confirmTitle"
    :message="confirmMessage"
    @confirm="handleConfirm"
    @cancel="handleCancel"
  />

  <FormModal
    :show="showForm"
    :title="editingManHinh ? 'Sửa Màn hình' : 'Thêm Màn hình'"
    :fields="manHinhFields"
    :initial-data="editingManHinh ? {
      maManHinh: editingManHinh.maManHinh,
      kichThuoc: editingManHinh.kichThuoc,
      congNghe: editingManHinh.congNghe || '',
      doPhanGiai: editingManHinh.doPhanGiai,
      tanSoQuet: editingManHinh.tanSoQuet || '',
      kieuManHinh: editingManHinh.kieuManHinh || '',
      moTa: editingManHinh.moTa || '',
      trangThai: editingManHinh.trangThai
    } : undefined"
    @submit="handleFormSubmit"
    @cancel="showForm = false"
  />

  <Toast ref="toastRef" />
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import api from '@/services/api'
import ConfirmModal from '@/components/ConfirmModal.vue'
import FormModal from '@/components/FormModal.vue'
import Toast from '@/components/Toast.vue'
import AdminTable from '@/components/AdminTable.vue'
>>>>>>> origin/Huan

interface ManHinh {
  id: number
  maManHinh: string
  kichThuoc: string
  congNghe?: string
<<<<<<< HEAD
  doPhanGiai?: string
  tanSoQuet?: string
  kieuManHinh?: string
  moTa?: string
  trangThai: number
  ngayTao?: string
  ngayCapNhat?: string
}

const manHinhs = ref<ManHinh[]>([])
const selectedManHinhs = ref<ManHinh[]>([])
const selectedManHinhIds = ref<number[]>([])
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
const editingManHinh = ref<ManHinh | null>(null)
=======
  doPhanGiai: string
  tanSoQuet?: string
  kieuManHinh?: string
  moTa?: string
  ngayTao?: string
  ngayCapNhat?: string
  trangThai: number
}

const manHinhs = ref<ManHinh[]>([])
const loading = ref(false)
const showForm = ref(false)
const editingManHinh = ref<ManHinh | null>(null)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

// Table columns configuration
const columns = [
  { key: 'maManHinh', label: 'Mã', class: 'code-col', type: 'code' as const },
  { key: 'kichThuoc', label: 'Kích thước', class: 'size-col' },
  { key: 'doPhanGiai', label: 'Độ phân giải', class: 'desc-col' },
  { key: 'congNghe', label: 'Công nghệ', class: 'desc-col' },
  { key: 'tanSoQuet', label: 'Tần số quét', class: 'frequency-col' },
  { key: 'kieuManHinh', label: 'Kiểu màn hình', class: 'type-col' },
  { key: 'trangThai', label: 'Trạng thái', class: 'status-col', type: 'status' as const }
]

// Confirm modal state
>>>>>>> origin/Huan
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const pendingAction = ref<(() => void) | null>(null)

<<<<<<< HEAD
// Form data for enhanced modal
const formData = ref({
  maManHinh: '',
  kichThuoc: '',
  congNghe: '',
  doPhanGiai: '',
  tanSoQuet: '',
  kieuManHinh: '',
  moTa: '',
  trangThai: 1
})

const manHinhFields = [
  { key: 'maManHinh', label: 'Mã Màn Hình', type: 'text' as const, required: true },
  { key: 'kichThuoc', label: 'Kích thước', type: 'text' as const, required: true },
  { key: 'congNghe', label: 'Công nghệ', type: 'text' as const },
  { key: 'doPhanGiai', label: 'Độ phân giải', type: 'text' as const },
=======
const manHinhFields = [
  { key: 'maManHinh', label: 'Mã Màn hình', type: 'text' as const, required: true },
  { key: 'kichThuoc', label: 'Kích thước', type: 'text' as const, required: true },
  { key: 'congNghe', label: 'Công nghệ', type: 'text' as const },
  { key: 'doPhanGiai', label: 'Độ phân giải', type: 'text' as const, required: true },
>>>>>>> origin/Huan
  { key: 'tanSoQuet', label: 'Tần số quét', type: 'text' as const },
  { key: 'kieuManHinh', label: 'Kiểu màn hình', type: 'text' as const },
  { key: 'moTa', label: 'Mô tả', type: 'textarea' as const },
  { key: 'trangThai', label: 'Hoạt động', type: 'checkbox' as const }
]

<<<<<<< HEAD
// Computed property for filtered manHinhs (without pagination)
const allFilteredManHinhs = computed(() => {
  let filtered = manHinhs.value

  console.log('=== FILTERING MANHINHS ===')
  console.log('Original manHinhs count:', manHinhs.value.length)
  console.log('Status filter value:', statusFilter.value)

  // Filter by status (active/inactive)
  if (statusFilter.value !== '') {
    console.log('Applying status filter:', statusFilter.value)
    filtered = filtered.filter(m => m.trangThai.toString() === statusFilter.value)
    console.log('After status filter:', filtered.length)
  } else {
    console.log('No status filter applied - showing all manHinhs')
  }

  // Filter by type (based on screen technology)
  if (typeFilter.value) {
    filtered = filtered.filter(manHinh => {
      const congNghe = manHinh.congNghe?.toLowerCase() || ''
      const kieuManHinh = manHinh.kieuManHinh?.toLowerCase() || ''
      const type = typeFilter.value

      console.log('Filtering by type:', type, 'for manHinh:', congNghe, kieuManHinh)

      if (type === 'lcd') {
        return congNghe.includes('lcd') || kieuManHinh.includes('lcd')
      } else if (type === 'oled') {
        return congNghe.includes('oled') || kieuManHinh.includes('oled')
      } else if (type === 'amoled') {
        return congNghe.includes('amoled') || kieuManHinh.includes('amoled')
      } else if (type === 'ips') {
        return congNghe.includes('ips') || kieuManHinh.includes('ips')
      } else if (type === 'khac') {
        return !congNghe.includes('lcd') && !congNghe.includes('oled') && 
               !congNghe.includes('amoled') && !congNghe.includes('ips') &&
               !kieuManHinh.includes('lcd') && !kieuManHinh.includes('oled') && 
               !kieuManHinh.includes('amoled') && !kieuManHinh.includes('ips')
      }
      return false
    })
  }

  // Search filter
  if (searchText.value) {
    const search = searchText.value.toLowerCase()
    filtered = filtered.filter(manHinh =>
      manHinh.kichThuoc?.toLowerCase().includes(search) ||
      manHinh.maManHinh?.toLowerCase().includes(search) ||
      manHinh.congNghe?.toLowerCase().includes(search) ||
      manHinh.doPhanGiai?.toLowerCase().includes(search) ||
      manHinh.tanSoQuet?.toLowerCase().includes(search) ||
      manHinh.kieuManHinh?.toLowerCase().includes(search) ||
      manHinh.moTa?.toLowerCase().includes(search)
    )
  }

  totalItems.value = filtered.length
  console.log('Final filtered manHinhs count:', filtered.length)
  console.log('=== END FILTERING ===')
  return filtered
})

// Computed property for paginated manHinhs
const paginatedManHinhs = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return allFilteredManHinhs.value.slice(start, end)
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

// Computed property for selected manHinhs
const selectedManHinhsComputed = computed(() => {
  return manHinhs.value.filter(manHinh => selectedManHinhIds.value.includes(manHinh.id))
})

// Computed property for select all checkbox
const isAllSelected = computed(() => {
  return paginatedManHinhs.value.length > 0 &&
    paginatedManHinhs.value.every(manHinh => selectedManHinhIds.value.includes(manHinh.id))
})

// Methods
async function loadManHinhs() {
  try {
    loading.value = true
    const response = await api.get('/api/man-hinh')
    const allManHinhs = response.data || []

    // Sort manHinhs by creation date (newest first)
    manHinhs.value = allManHinhs.sort((a: any, b: any) => {
      const dateA = new Date(a.ngayTao || a.ngayCapNhat || 0)
      const dateB = new Date(b.ngayTao || b.ngayCapNhat || 0)
      return dateB.getTime() - dateA.getTime() // Newest first
    })

    // Debug: Log all manHinhs and their status
    console.log('=== LOADED MANHINHS ===')
    console.log('Total manHinhs loaded:', manHinhs.value.length)
    manHinhs.value.forEach((manHinh, index) => {
      console.log(`ManHinh ${index}:`, {
        id: manHinh.id,
        maManHinh: manHinh.maManHinh,
        kichThuoc: manHinh.kichThuoc,
        congNghe: manHinh.congNghe,
        doPhanGiai: manHinh.doPhanGiai,
        tanSoQuet: manHinh.tanSoQuet,
        kieuManHinh: manHinh.kieuManHinh,
        moTa: manHinh.moTa,
        trangThai: manHinh.trangThai,
        ngayTao: manHinh.ngayTao,
        ngayCapNhat: manHinh.ngayCapNhat
      })
    })
    console.log('Current statusFilter:', statusFilter.value)
    console.log('=== END LOADED MANHINHS ===')
  } catch (error) {
    console.error('Lỗi khi tải danh sách màn hình:', error)
    toastRef.value?.error('Lỗi', 'Không thể tải danh sách màn hình')
=======
async function loadManHinhs() {
  loading.value = true
  try {
    const { data } = await api.get<ManHinh[]>('/api/man-hinh')
    manHinhs.value = data
>>>>>>> origin/Huan
  } finally {
    loading.value = false
  }
}

<<<<<<< HEAD
function applyFilters() {
  currentPage.value = 1
}

function clearAllFilters() {
  searchText.value = ''
  statusFilter.value = ''
  typeFilter.value = ''
  applyFilters()
}

function showInactiveManHinhs() {
  statusFilter.value = '0'
  currentPage.value = 1
  toastRef.value?.info('Thông báo', 'Đang hiển thị màn hình ngừng hoạt động. Gạt toggle để kích hoạt lại!')
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

function addManHinh() {
  editingManHinh.value = null
  resetFormData()
  showForm.value = true
}

function viewManHinh(manHinh: ManHinh) {
  // TODO: Implement view manHinh detail
  console.log('View manHinh:', manHinh)
}

function editManHinh(manHinh: ManHinh) {
  editingManHinh.value = manHinh
  formData.value = {
    maManHinh: manHinh.maManHinh,
    kichThuoc: manHinh.kichThuoc,
    congNghe: manHinh.congNghe || '',
    doPhanGiai: manHinh.doPhanGiai || '',
    tanSoQuet: manHinh.tanSoQuet || '',
    kieuManHinh: manHinh.kieuManHinh || '',
    moTa: manHinh.moTa || '',
    trangThai: manHinh.trangThai
  }
  showForm.value = true
}

function resetFormData() {
  formData.value = {
    maManHinh: '',
    kichThuoc: '',
    congNghe: '',
    doPhanGiai: '',
    tanSoQuet: '',
    kieuManHinh: '',
    moTa: '',
    trangThai: 1
  }
}

// Test function to create a manHinh with sample data
function testCreateManHinh() {
  formData.value = {
    maManHinh: 'MHTEST001',
    kichThuoc: '6.1 inch',
    congNghe: 'OLED',
    doPhanGiai: '1080x2400',
    tanSoQuet: '120Hz',
    kieuManHinh: 'Dynamic AMOLED',
    moTa: 'Màn hình test để kiểm tra API',
    trangThai: 1
  }

  console.log('Test data:', formData.value)
  handleFormSubmit()
}

// Simple toggle manHinh status
function toggleManHinhStatus(manHinh: ManHinh) {
  const newStatus = manHinh.trangThai === 1 ? 0 : 1
  const action = newStatus === 1 ? 'kích hoạt' : 'vô hiệu hóa'
  
  confirmTitle.value = `Xác nhận ${action} màn hình`
  confirmMessage.value = `Bạn có chắc chắn muốn ${action} màn hình "${manHinh.kichThuoc}"?`
  
  pendingAction.value = () => executeToggleManHinhStatus(manHinh, newStatus)
  showConfirmModal.value = true
}

async function executeToggleManHinhStatus(manHinh: ManHinh, newStatus: number) {
  try {
    console.log('=== TOGGLE MANHINH STATUS ===')
    console.log('ManHinh ID:', manHinh.id)
    console.log('ManHinh kichThuoc:', manHinh.kichThuoc)
    console.log('Current status:', manHinh.trangThai)
    console.log('New status:', newStatus)

    // Use the new simple endpoint - ensure clean ID
    const cleanId = parseInt(String(manHinh.id).replace(/[^\d]/g, ''), 10)
    console.log('Clean ID for API call:', cleanId)

    const response = await api.put(`/api/man-hinh/${cleanId}/status`, {
      trangThai: newStatus
    })

    console.log('API response:', response.status, response.data)

    // Update local state
    manHinh.trangThai = newStatus

    if (newStatus === 0) {
      toastRef.value?.success('Thành công', 'Đã vô hiệu hóa màn hình!')
    } else {
      toastRef.value?.success('Thành công', 'Đã kích hoạt màn hình!')
    }
  } catch (error) {
    console.error('Lỗi khi cập nhật trạng thái:', error)
    console.error('Error details:', error.response?.data)
    toastRef.value?.error('Lỗi', 'Không thể cập nhật trạng thái màn hình')
  }
}

// Checkbox functions
function toggleSelectAll() {
  if (isAllSelected.value) {
    // Unselect all manHinhs on current page
    const currentPageIds = paginatedManHinhs.value.map(m => m.id)
    selectedManHinhIds.value = selectedManHinhIds.value.filter(id => !currentPageIds.includes(id))
  } else {
    // Select all manHinhs on current page
    const currentPageIds = paginatedManHinhs.value.map(m => m.id)
    const newIds = currentPageIds.filter(id => !selectedManHinhIds.value.includes(id))
    selectedManHinhIds.value = [...selectedManHinhIds.value, ...newIds]
  }
}

function toggleSelectManHinh(manHinhId: number) {
  const index = selectedManHinhIds.value.indexOf(manHinhId)
  if (index > -1) {
    selectedManHinhIds.value.splice(index, 1)
  } else {
    selectedManHinhIds.value.push(manHinhId)
  }
}

function exportToExcel() {
  try {
    // Get selected manHinhs or all manHinhs
    const manHinhsToExport = selectedManHinhsComputed.value.length > 0 ? selectedManHinhsComputed.value : manHinhs.value

    if (manHinhsToExport.length === 0) {
      toastRef.value?.warning('Cảnh báo', 'Không có dữ liệu màn hình để xuất Excel')
      return
    }

    // Tạo dữ liệu cho Excel
    const excelData = manHinhsToExport.map((manHinh, index) => ({
      'STT': index + 1,
      'Mã MH': manHinh.maManHinh,
      'Kích thước': manHinh.kichThuoc,
      'Công nghệ': manHinh.congNghe || '',
      'Độ phân giải': manHinh.doPhanGiai || '',
      'Tần số quét': manHinh.tanSoQuet || '',
      'Kiểu màn hình': manHinh.kieuManHinh || '',
      'Mô tả': manHinh.moTa || '',
      'Trạng Thái': manHinh.trangThai === 1 ? 'Hoạt động' : 'Ngừng hoạt động',
      'Ngày Tạo': manHinh.ngayTao ? new Date(manHinh.ngayTao).toLocaleDateString('vi-VN') : '',
      'Ngày Cập Nhật': manHinh.ngayCapNhat ? new Date(manHinh.ngayCapNhat).toLocaleDateString('vi-VN') : ''
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
    XLSX.utils.book_append_sheet(wb, ws, 'Danh sách Màn Hình')

    // Xuất file Excel
    const fileName = `danh_sach_man_hinh_${new Date().toISOString().split('T')[0]}.xlsx`
    XLSX.writeFile(wb, fileName)

    toastRef.value?.success('Thành công', 'Đã export danh sách màn hình thành công!')
  } catch (error) {
    console.error('Lỗi khi export Excel:', error)
    toastRef.value?.error('Lỗi', 'Có lỗi xảy ra khi export Excel')
  }
}

// Function to generate maManHinh automatically
function generateMaManHinhAuto() {
  if (formData.value.kichThuoc) {
    const words = formData.value.kichThuoc.toLowerCase().split(' ')
    const prefix = words.map(word => word.charAt(0)).join('').toUpperCase()
    const timestamp = Date.now().toString().slice(-3)
    formData.value.maManHinh = `MH${prefix}${timestamp}`
  } else {
    // Generate random code if no size
    const randomCode = Math.random().toString(36).substring(2, 5).toUpperCase()
    const timestamp = Date.now().toString().slice(-3)
    formData.value.maManHinh = `MH${randomCode}${timestamp}`
  }
}

function handleFormSubmit() {
  // Validate form data
  if (!formData.value.maManHinh || !formData.value.kichThuoc) {
    toastRef.value?.error('Lỗi', 'Vui lòng điền đầy đủ thông tin bắt buộc')
    return
  }

  const isEdit = !!editingManHinh.value
  const action = isEdit ? 'cập nhật' : 'thêm mới'
  
  confirmTitle.value = `Xác nhận ${action} màn hình`
  confirmMessage.value = `Bạn có chắc chắn muốn ${action} màn hình "${formData.value.kichThuoc}"?`
  
  pendingAction.value = () => {
    if (isEdit) {
      updateManHinh(editingManHinh.value!.id, formData.value)
    } else {
      createManHinh(formData.value)
    }
  }
  showConfirmModal.value = true
}

async function createManHinh(formData: any) {
  try {
    console.log('Creating manHinh with data:', formData)
    const response = await api.post('/api/man-hinh', formData)
    console.log('API response:', response.data)
    await loadManHinhs()
    showForm.value = false
    resetFormData()
    toastRef.value?.success('Thành công', 'Đã thêm màn hình mới')
  } catch (error) {
    console.error('Error creating manHinh:', error)
    console.error('Error response:', error.response?.data)
    toastRef.value?.error('Lỗi tạo màn hình', 'Không thể tạo màn hình mới')
  }
}

async function updateManHinh(id: number, formData: any) {
  try {
    await api.put(`/api/man-hinh/${id}`, formData)
    await loadManHinhs()
    showForm.value = false
    toastRef.value?.success('Thành công', 'Đã cập nhật màn hình')
  } catch (error) {
    console.error('Error updating manHinh:', error)
    toastRef.value?.error('Lỗi cập nhật', 'Không thể cập nhật màn hình')
=======
function openForm(manHinh?: ManHinh) {
  editingManHinh.value = manHinh || null
  showForm.value = true
}

async function handleFormSubmit(data: any) {
  try {
    if (editingManHinh.value) {
      await api.put(`/api/man-hinh/${editingManHinh.value.id}`, data)
      toastRef.value?.success('Thành công', 'Cập nhật màn hình thành công!')
    } else {
      await api.post('/api/man-hinh', data)
      toastRef.value?.success('Thành công', 'Thêm màn hình thành công!')
    }
    showForm.value = false
    await loadManHinhs()
  } catch (error: any) {
    console.error('Lỗi khi lưu:', error)
    if (error.response?.data) {
      toastRef.value?.error('Lỗi lưu Màn Hình', error.response.data)
    } else {
      toastRef.value?.error('Lỗi lưu Màn Hình', 'Có lỗi xảy ra khi lưu màn hình')
    }
  }
}

function deleteManHinh(id: number) {
  const manHinh = manHinhs.value.find(m => m.id === id)
  confirmTitle.value = 'Xác nhận xóa Màn hình'
  confirmMessage.value = `Bạn có chắc chắn muốn xóa màn hình "${manHinh?.kichThuoc || 'này'}"? Hành động này không thể hoàn tác.`
  pendingAction.value = () => performDelete(id)
  showConfirmModal.value = true
}

async function performDelete(id: number) {
  try {
    await api.delete(`/api/man-hinh/${id}`)
    toastRef.value?.success('Thành công', 'Xóa màn hình thành công!')
    await loadManHinhs()
  } catch (error: any) {
    console.error('Lỗi khi xóa:', error)
    // Hiển thị thông báo lỗi cho user
    if (error.response?.data) {
      // Backend trả về thông báo lỗi trực tiếp trong response.data
      toastRef.value?.error('Không thể xóa', error.response.data)
    } else {
      toastRef.value?.error('Lỗi xóa Màn Hình', 'Có lỗi xảy ra khi xóa màn hình')
    }
>>>>>>> origin/Huan
  }
}

function handleConfirm() {
  if (pendingAction.value) {
    pendingAction.value()
<<<<<<< HEAD
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
  loadManHinhs()
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

.screen-avatar {
  background: linear-gradient(135deg, #f97316, #ea580c);
  color: white;
  border: 2px solid #f97316;
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
  z-index: 9999;
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
  position: relative;
  z-index: 10;
  flex-shrink: 0;
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
  background: #10b981;
  color: white;
  box-shadow: 0 4px 15px rgba(16, 185, 129, 0.3);
}

.btn-test:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(16, 185, 129, 0.4);
}

.btn-save {
  background: linear-gradient(135deg, #f97316, #ea580c);
  color: white;
  box-shadow: 0 4px 15px rgba(249, 115, 22, 0.3);
  position: relative;
  z-index: 11;
  min-width: 120px;
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
=======
  }
  showConfirmModal.value = false
  pendingAction.value = null
}

function handleCancel() {
  showConfirmModal.value = false
  pendingAction.value = null
}

async function toggleManHinhStatus(manHinh: ManHinh) {
  try {
    const newStatus = (manHinh.trangThai || 0) === 1 ? 0 : 1
    await api.put(`/api/man-hinh/${manHinh.id}/status`, { trangThai: newStatus })
    
    // Update local data
    const index = manHinhs.value.findIndex(m => m.id === manHinh.id)
    if (index !== -1) {
      manHinhs.value[index].trangThai = newStatus
    }
    
    const statusText = newStatus === 1 ? 'Hoạt động' : 'Ngừng hoạt động'
    const message = newStatus === 1 
      ? `Đã chuyển Màn hình "${manHinh.kichThuoc}" sang trạng thái <span style="color: #28a745; font-weight: bold;">${statusText}</span>`
      : `Đã chuyển Màn hình "${manHinh.kichThuoc}" sang trạng thái <span style="color: #dc3545; font-weight: bold;">${statusText}</span>`
    toastRef.value?.success('Thành công', message)
  } catch (error: any) {
    console.error('Lỗi khi cập nhật trạng thái:', error)
    toastRef.value?.error('Lỗi cập nhật', 'Không thể cập nhật trạng thái Màn hình')
  }
}

async function exportExcel() {
  try {
    const response = await api.get('/api/man-hinh/export', { responseType: 'blob' })
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', 'danh_sach_man_hinh.xlsx')
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
    toastRef.value?.success('Thành công', 'Xuất Excel thành công!')
  } catch (error: any) {
    console.error('Lỗi khi xuất Excel:', error)
    toastRef.value?.error('Lỗi xuất Excel', 'Có lỗi xảy ra khi xuất file Excel')
  }
}

onMounted(loadManHinhs)
</script>

<style scoped>
@import '@/styles/admin-layout.css';

/* Custom column widths for ManHinh page */
:deep(.data-table .code-col) {
  width: 100px;
  min-width: 100px;
}

:deep(.data-table .size-col) {
  width: 170px !important;
  min-width: 170px !important;
}

:deep(.data-table .stt-col) {
  width: 40px;
  min-width: 40px;
}

:deep(.data-table .desc-col) {
  width: 100px;
  min-width: 100px;
}

:deep(.data-table .frequency-col) {
  width: 80px;
  min-width: 80px;
}

:deep(.data-table .type-col) {
  width: 80px;
  min-width: 80px;
}

:deep(.data-table .status-col) {
  width: 100px;
  min-width: 100px;
}

:deep(.data-table .date-col) {
  width: 100px;
  min-width: 100px;
}

:deep(.data-table .action-col) {
  width: 180px;
  min-width: 180px;
  text-align: center;
}

/* Improve action buttons spacing */
:deep(.edit-btn, .delete-btn) {
  margin: 0 4px;
  padding: 6px 10px;
  font-size: 14px;
>>>>>>> origin/Huan
}
</style>
