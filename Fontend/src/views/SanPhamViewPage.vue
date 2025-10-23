<<<<<<< HEAD
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
            <p>Tìm kiếm và lọc chi tiết sản phẩm theo tiêu chí</p>
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
              placeholder="Tìm theo RAM, ROM, màu sắc..."
              @input="applyFilters"
              class="search-input"
            />
          </div>
          <div class="filter-group">
            <label>RAM:</label>
            <select v-model="selectedRamId" @change="applyFilters" class="filter-select">
              <option :value="null">Tất cả RAM</option>
              <option v-for="ram in rams" :key="ram.id" :value="ram.id">{{ ram.tenRam }}</option>
            </select>
          </div>
          <div class="filter-group">
            <label>ROM:</label>
            <select v-model="selectedRomId" @change="applyFilters" class="filter-select">
              <option :value="null">Tất cả ROM</option>
              <option v-for="rom in roms" :key="rom.id" :value="rom.id">{{ rom.dungLuong }}</option>
            </select>
          </div>
          <div class="filter-group">
            <label>Màu sắc:</label>
            <select v-model="selectedMauId" @change="applyFilters" class="filter-select">
              <option :value="null">Tất cả Màu</option>
              <option v-for="m in mauSacs" :key="m.id" :value="m.id">{{ m.tenMau }}</option>
            </select>
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
            <label>Thuộc tính:</label>
            <select v-model="attributeFilter" @change="applyFilters" class="filter-select">
              <option value="">Tất cả</option>
              <option value="ram">RAM</option>
              <option value="rom">ROM</option>
              <option value="mau">Màu sắc</option>
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
          
        </div>
      </div>

      <!-- Main Content Area -->
      <div class="main-content">
        <!-- Table Header Section -->
        <div class="table-header">
          <div class="table-title">
            <h2>Chi tiết Sản phẩm</h2>
            <span class="item-count">{{ totalItems }} phiên bản</span>
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
              <th>RAM</th>
              <th>ROM</th>
              <th>Màu</th>
              <th>Giá bán</th>
              <th>Giá nhập</th>
              <th>Số lượng</th>
              <th>Trạng Thái</th>
              <th>Thao Tác</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="(ct, index) in paginatedChiTiets" :key="ct.id" class="variant-row" :class="{ selected: selectedVariant?.id === ct.id }" @click="selectedVariant = ct">
              <td class="checkbox-column">
                <input
                  type="checkbox"
                  class="row-checkbox"
                  :value="ct.id"
                  :checked="selectedChiTietIds.includes(ct.id)"
                  @change="toggleSelectChiTiet(ct.id)"
                />
              </td>
              <td>{{ startItem + index }}</td>
              <td>{{ getRamName(ct) }}</td>
              <td>{{ getRomName(ct) }}</td>
              <td class="address-cell">
                <div class="address-content" :title="getMauName(ct)">
                  {{ getMauName(ct) }}
                </div>
              </td>
              <td>{{ formatPrice(getGiaBan(ct)) }}</td>
              <td>{{ ct.giaNhap ? formatPrice(ct.giaNhap) : 'Chưa cập nhật' }}</td>
              <td>{{ getSoLuong(ct) }}</td>
              <td>
                <span class="status-badge" :class="(ct.trangThai ?? 0) === 1 ? 'badge-active' : 'badge-inactive'">
                  {{ (ct.trangThai ?? 0) === 1 ? 'Hoạt động' : 'Ngừng hoạt động' }}
                  </span>
              </td>
              <td>
                <div class="action-buttons">
                  <div class="button-row">
                    <button class="btn-view" @click="openVariantModal(ct)" title="Xem chi tiết">
                      <font-awesome-icon icon="eye" />
                    </button>
                  </div>
                  <div class="toggle-container">
                    <label class="toggle-switch" :title="(ct.trangThai ?? 0) === 1 ? 'Gạt để vô hiệu hóa' : 'Gạt để kích hoạt'">
                      <input type="checkbox" :checked="(ct.trangThai ?? 0) === 1" @change="toggleChiTietStatus(ct)" />
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

    <!-- Variant Detail Modal -->
    <div v-if="showVariantModal" class="modal-overlay" @click="closeVariantModal">
      <div class="variant-detail-modal" @click.stop>
        <div class="modal-header">
          <div class="modal-title">
            <div class="title-icon">
              <font-awesome-icon icon="info-circle" />
            </div>
            <h3>Chi tiết Biến thể Sản phẩm</h3>
          </div>
          <button class="close-btn" @click="closeVariantModal">
            <font-awesome-icon icon="times" />
          </button>
        </div>
        
        <div class="modal-body" v-if="selectedVariant">
          <div class="variant-info-grid">
            <!-- Basic Info Section -->
            <div class="info-section">
              <h4 class="section-title">Thông tin cơ bản</h4>
              <div class="info-grid">
                <div class="info-item">
                  <label>Mã biến thể:</label>
                  <span class="info-value">{{ selectedVariant.maCtsp || 'Chưa có' }}</span>
                </div>
                <div class="info-item">
                  <label>Số lượng:</label>
                  <span class="info-value">{{ getSoLuong(selectedVariant) }}</span>
                </div>
                <div class="info-item">
                  <label>Trạng thái:</label>
                  <span class="status-badge" :class="(selectedVariant.trangThai ?? 0) === 1 ? 'badge-active' : 'badge-inactive'">
                    {{ (selectedVariant.trangThai ?? 0) === 1 ? 'Hoạt động' : 'Ngừng hoạt động' }}
                  </span>
                </div>
              </div>
            </div>

            <!-- Specifications Section -->
            <div class="info-section">
              <h4 class="section-title">Thông số kỹ thuật</h4>
              <div class="info-grid">
                <div class="info-item">
                  <label>RAM:</label>
                  <span class="info-value">{{ getRamName(selectedVariant) || 'Chưa có' }}</span>
                </div>
                <div class="info-item">
                  <label>ROM:</label>
                  <span class="info-value">{{ getRomName(selectedVariant) || 'Chưa có' }}</span>
                </div>
                <div class="info-item">
                  <label>Màu sắc:</label>
                  <div class="color-info">
                    <span class="info-value">{{ getMauName(selectedVariant) || 'Chưa có' }}</span>
                    <div v-if="selectedVariant?.mauSac?.maHex" 
                         class="color-preview" 
                         :style="{ backgroundColor: selectedVariant.mauSac.maHex }">
                    </div>
                  </div>
                </div>
                <div class="info-item" v-if="product?.tenChip">
                  <label>Chip:</label>
                  <span class="info-value">{{ product.tenChip }}</span>
                </div>
                <div class="info-item" v-if="product?.tenCpu">
                  <label>CPU:</label>
                  <span class="info-value">{{ product.tenCpu }}</span>
                </div>
                <div class="info-item" v-if="product?.tenGpu">
                  <label>GPU:</label>
                  <span class="info-value">{{ product.tenGpu }}</span>
                </div>
                <div class="info-item" v-if="product?.tenHeDieuHanh">
                  <label>Hệ điều hành:</label>
                  <span class="info-value">{{ product.tenHeDieuHanh }}</span>
                </div>
                <div class="info-item" v-if="product?.tenManHinh">
                  <label>Màn hình:</label>
                  <span class="info-value">{{ product.tenManHinh }} inch</span>
                </div>
                <div class="info-item" v-if="product?.tenPin">
                  <label>Pin:</label>
                  <span class="info-value">{{ product.tenPin }} mAh</span>
                </div>
                <div class="info-item" v-if="product?.tenCameraTruoc">
                  <label>Camera trước:</label>
                  <span class="info-value">{{ product.tenCameraTruoc }}</span>
                </div>
                <div class="info-item" v-if="product?.tenCameraSau">
                  <label>Camera sau:</label>
                  <span class="info-value">{{ product.tenCameraSau }}</span>
                </div>
                <div class="info-item" v-if="product?.tenSim">
                  <label>Loại SIM:</label>
                  <span class="info-value">{{ product.tenSim }}</span>
                </div>
                <div class="info-item" v-if="product?.tenHang">
                  <label>Hãng sản xuất:</label>
                  <span class="info-value">{{ product.tenHang }}</span>
                </div>
                <div class="info-item" v-if="product?.tenDanhMuc">
                  <label>Danh mục:</label>
                  <span class="info-value">{{ product.tenDanhMuc }}</span>
                </div>
                
                <!-- IMEI Section moved here -->
                <div class="info-item full-width imei-section">
                  <label>Danh sách IMEI:</label>
                  <div class="imei-container">
                    <div v-if="loadingImeis" class="loading-imeis">
                      <div class="loading-spinner"></div>
                      <span>Đang tải IMEI...</span>
                    </div>
                    <div v-else-if="variantImeis.length === 0" class="no-imeis">
                      <div class="no-imeis-icon">
                        <font-awesome-icon icon="mobile-alt" />
                      </div>
                      <span>Chưa có IMEI nào</span>
                    </div>
                    <div v-else class="imei-list">
                      <div class="imei-item" v-for="(imei, index) in variantImeis" :key="imei.id">
                        <div class="imei-number">
                          <span class="imei-label">IMEI {{ index + 1 }}:</span>
                          <span class="imei-value">{{ imei.soImei }}</span>
                        </div>
                        <div class="imei-status">
                          <span class="status-badge" :class="imei.trangThai === 1 ? 'badge-active' : 'badge-inactive'">
                            {{ imei.trangThai === 1 ? 'Hoạt động' : 'Ngừng hoạt động' }}
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Pricing Section -->
            <div class="info-section">
              <h4 class="section-title">Thông tin giá</h4>
              <div class="info-grid">
                <div class="info-item">
                  <label>Giá nhập:</label>
                  <span class="info-value price-value">
                    {{ selectedVariant.giaNhap ? formatPrice(selectedVariant.giaNhap) : 'Chưa cập nhật' }}
                  </span>
                </div>
                <div class="info-item">
                  <label>Giá bán:</label>
                  <span class="info-value price-value">
                    {{ formatPrice(getGiaBan(selectedVariant)) }}
                  </span>
                </div>
                <div class="info-item" v-if="selectedVariant.giaNhap && getGiaBan(selectedVariant)">
                  <label>Lợi nhuận:</label>
                  <span class="info-value profit-value">
                    {{ formatPrice(getGiaBan(selectedVariant) - selectedVariant.giaNhap) }}
                  </span>
                </div>
              </div>
            </div>

            <!-- Additional Info Section -->
            <div class="info-section" v-if="selectedVariant.ghiChu">
              <h4 class="section-title">Ghi chú</h4>
              <div class="info-item full-width">
                <span class="info-value">{{ selectedVariant.ghiChu }}</span>
              </div>
            </div>


            <!-- Timestamps Section -->
            <div class="info-section">
              <h4 class="section-title">Thông tin thời gian</h4>
              <div class="info-grid">
                <div class="info-item" v-if="selectedVariant.ngayTao">
                  <label>Ngày tạo:</label>
                  <span class="info-value">{{ formatDateTime(selectedVariant.ngayTao) }}</span>
                </div>
                <div class="info-item" v-if="selectedVariant.ngayCapNhat">
                  <label>Ngày cập nhật:</label>
                  <span class="info-value">{{ formatDateTime(selectedVariant.ngayCapNhat) }}</span>
                </div>
                <div class="info-item" v-if="selectedVariant.nguoiTao">
                  <label>Người tạo:</label>
                  <span class="info-value">{{ selectedVariant.nguoiTao }}</span>
                </div>
                <div class="info-item" v-if="selectedVariant.nguoiCapNhat">
                  <label>Người cập nhật:</label>
                  <span class="info-value">{{ selectedVariant.nguoiCapNhat }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn-cancel" @click="closeVariantModal">
            <font-awesome-icon icon="times" />
            Đóng
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/services/api'
import Toast from '@/components/Toast.vue'
import PosHeader from '@/components/PosHeader.vue'
import ConfirmModal from '@/components/ConfirmModal.vue'
import FormModal from '@/components/FormModal.vue'
import { FontAwesomeIcon } from '@/plugins/fontawesome'
import * as XLSX from 'xlsx'

const router = useRouter()
const route = useRoute()
const productId = computed(() => route.params.id ? Number(route.params.id) : null)
const product = ref<any | null>(null)
const showVariantModal = ref(false)
const selectedVariant = ref<any | null>(null)
const variantImeis = ref<any[]>([])
const loadingImeis = ref(false)
const rams = ref<any[]>([])
const roms = ref<any[]>([])
const mauSacs = ref<any[]>([])

interface ChiTietSanPham {
  id: number
  idRam?: number | null
  idRom?: number | null
  idMauSac?: number | null
  ram?: { tenRam?: string }
  rom?: { dungLuong?: string }
  mauSac?: { tenMau?: string; maHex?: string }
  donGia?: number | null
  giaBan?: number | null
  gia?: number | null
  soLuong?: number | null
  trangThai?: number | null
}

const chiTiets = ref<ChiTietSanPham[]>([])
const selectedChiTiets = ref<ChiTietSanPham[]>([])
const selectedChiTietIds = ref<number[]>([])
const loading = ref(false)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

// Filter states
const statusFilter = ref('')
const attributeFilter = ref('')
const searchText = ref('')
const selectedRamId = ref<number | null>(null)
const selectedRomId = ref<number | null>(null)
const selectedMauId = ref<number | null>(null)

// Pagination states
const currentPage = ref(1)
const itemsPerPage = ref(5)
const totalItems = ref(0)

// Modal states
const showForm = ref(false)
const editingHang = ref(null)
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const pendingAction = ref<(() => void) | null>(null)

// Form data for enhanced modal
const formData = ref({})

// Computed property for filtered hangs (without pagination)
const allFilteredChiTiets = computed(() => {
  let filtered = chiTiets.value

  console.log('=== FILTERING CHI TIET SAN PHAM ===')
  console.log('Original chi tiet count:', chiTiets.value.length)
  console.log('Status filter value:', statusFilter.value)

  // Filter by status (active/inactive)
  if (statusFilter.value !== '') {
    console.log('Applying status filter:', statusFilter.value)
    filtered = filtered.filter(c => c.trangThai.toString() === statusFilter.value)
    console.log('After status filter:', filtered.length)
  } else {
    console.log('No status filter applied - showing all details')
  }

  // Attribute filter (ram/rom/mau)
  if (attributeFilter.value) {
    const attr = attributeFilter.value
    filtered = filtered.filter(ct => {
      if (attr === 'ram') return (getRamName(ct) || '').toLowerCase().includes((searchText.value || '').toLowerCase()) || searchText.value === ''
      if (attr === 'rom') return (getRomName(ct) || '').toLowerCase().includes((searchText.value || '').toLowerCase()) || searchText.value === ''
      if (attr === 'mau') return (getMauName(ct) || '').toLowerCase().includes((searchText.value || '').toLowerCase()) || searchText.value === ''
      return true
    })
  }

  // Explicit dropdown filters
  if (selectedRamId.value) {
    filtered = filtered.filter(ct => (ct.ramId === selectedRamId.value) || getRamName(ct) === (rams.value.find(r => r.id === selectedRamId.value)?.tenRam))
  }
  if (selectedRomId.value) {
    filtered = filtered.filter(ct => (ct.romId === selectedRomId.value) || getRomName(ct) === (roms.value.find(r => r.id === selectedRomId.value)?.dungLuong))
  }
  if (selectedMauId.value) {
    filtered = filtered.filter(ct => (ct.mauSacId === selectedMauId.value) || getMauName(ct) === (mauSacs.value.find(m => m.id === selectedMauId.value)?.tenMau))
  }

  // Search filter
  if (searchText.value) {
    const search = searchText.value.toLowerCase()
    filtered = filtered.filter(ct =>
      getRamName(ct).toLowerCase().includes(search) ||
      getRomName(ct).toLowerCase().includes(search) ||
      getMauName(ct).toLowerCase().includes(search)
    )
  }

  totalItems.value = filtered.length
  console.log('Final filtered details count:', filtered.length)
  console.log('=== END FILTERING CHI TIET ===')
  return filtered
})

// Computed property for paginated hangs
const paginatedChiTiets = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return allFilteredChiTiets.value.slice(start, end)
})

const totalPages = computed(() => {
  return Math.ceil(totalItems.value / itemsPerPage.value)
})
=======
<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/services/api'

interface SanPhamDetail {
  id: number
  maSanPham: string
  tenSanPham: string
  moTa?: string
  ngayTao?: string
  ngayCapNhat?: string
  trangThai: number
  tenDanhMuc?: string
  tenHang?: string
  tenManHinh?: string
  tenCameraTruoc?: string
  tenCameraSau?: string
  tenChip?: string
  tenGpu?: string
  tenSim?: string
  tenHeDieuHanh?: string
  tenCpu?: string
  tenPin?: string
  tongImei?: number
  variants?: VariantDetail[]
}

interface VariantDetail {
  id: number
  idRam: number
  idRom: number
  idMauSac: number
  soLuong: number
  donGia: number
  giaNhap?: number
  ghiChu?: string
  tenRam?: string
  tenRom?: string
  tenMauSac?: string
  imeis?: string[]
  imageUrls?: string[]
}

const route = useRoute()
const router = useRouter()
const productId = computed(() => route.params.id as string)
const product = ref<SanPhamDetail | null>(null)
const loading = ref(false)
const mauSacs = ref<any[]>([])
const hangs = ref<any[]>([])
const heDieuHanhs = ref<any[]>([])
const manHinhs = ref<any[]>([])
const cameraTruocs = ref<any[]>([])
const cameraSaus = ref<any[]>([])
const chips = ref<any[]>([])
const gpus = ref<any[]>([])
const cpus = ref<any[]>([])
const pins = ref<any[]>([])
const sims = ref<any[]>([])
const danhMucs = ref<any[]>([])
const rams = ref<any[]>([])
const roms = ref<any[]>([])

// Filter states
const keyword = ref('')
const selectedRam = ref<number | null>(null)
const selectedRom = ref<number | null>(null)
const selectedMauSac = ref<number | null>(null)
const selectedTrangThai = ref<'all' | 'active' | 'inactive'>('all')

// Pagination
const currentPage = ref(1)
const itemsPerPage = ref(5)

// Modal states
const showVariantModal = ref(false)
const selectedVariant = ref<VariantDetail | null>(null)

async function loadProductDetail() {
  if (!productId.value) return
  
  loading.value = true
  try {
    const { data } = await api.get<SanPhamDetail>(`/api/san-pham/${productId.value}/view`)
    product.value = data
  } catch (error) {
    console.error('Lỗi khi tải chi tiết sản phẩm:', error)
  } finally {
    loading.value = false
  }
}

async function loadMauSacs() {
  try {
    const { data } = await api.get('/api/mau-sac/active')
    mauSacs.value = data
  } catch (error) {
    console.error('Lỗi khi tải danh sách màu sắc:', error)
  }
}

async function loadAllAttributes() {
  try {
    const [hangRes, hdhRes, mhRes, ctRes, csRes, chipRes, gpuRes, cpuRes, pinRes, simRes, dmRes, ramRes, romRes] = await Promise.allSettled([
      api.get('/api/hang/active'),
      api.get('/api/he-dieu-hanh/active'),
      api.get('/api/man-hinh/active'),
      api.get('/api/camera-truoc/active'),
      api.get('/api/camera-sau/active'),
      api.get('/api/chip/active'),
      api.get('/api/gpu/active'),
      api.get('/api/cpu/active'),
      api.get('/api/pin/active'),
      api.get('/api/sim/active'),
      api.get('/api/danh-muc/active'),
      api.get('/api/ram/active'),
      api.get('/api/rom/active')
    ])
    
    // Handle successful responses
    if (hangRes.status === 'fulfilled') {
      hangs.value = hangRes.value.data
    } else {
      console.error('Hang request failed:', hangRes.reason)
    }
    
    if (hdhRes.status === 'fulfilled') {
      heDieuHanhs.value = hdhRes.value.data
    } else {
      console.error('HeDieuHanh request failed:', hdhRes.reason)
    }
    
    if (mhRes.status === 'fulfilled') {
      manHinhs.value = mhRes.value.data
    } else {
      console.error('ManHinh request failed:', mhRes.reason)
    }
    
    if (ctRes.status === 'fulfilled') {
      cameraTruocs.value = ctRes.value.data
    } else {
      console.error('CameraTruoc request failed:', ctRes.reason)
    }
    
    if (csRes.status === 'fulfilled') {
      cameraSaus.value = csRes.value.data
    } else {
      console.error('CameraSau request failed:', csRes.reason)
    }
    
    if (chipRes.status === 'fulfilled') {
      chips.value = chipRes.value.data
    } else {
      console.error('Chip request failed:', chipRes.reason)
    }
    
    if (gpuRes.status === 'fulfilled') {
      gpus.value = gpuRes.value.data
    } else {
      console.error('Gpu request failed:', gpuRes.reason)
    }
    
    if (cpuRes.status === 'fulfilled') {
      cpus.value = cpuRes.value.data
    } else {
      console.error('Cpu request failed:', cpuRes.reason)
    }
    
    if (pinRes.status === 'fulfilled') {
      pins.value = pinRes.value.data
    } else {
      console.error('Pin request failed:', pinRes.reason)
    }
    
    if (simRes.status === 'fulfilled') {
      sims.value = simRes.value.data
    } else {
      console.error('Sim request failed:', simRes.reason)
    }
    
    if (dmRes.status === 'fulfilled') {
      danhMucs.value = dmRes.value.data
    } else {
      console.error('DanhMuc request failed:', dmRes.reason)
    }
    
    if (ramRes.status === 'fulfilled') {
      rams.value = ramRes.value.data
    } else {
      // Fallback: try to load all RAMs if active endpoint fails
      try {
        const fallbackRes = await api.get('/api/ram')
        rams.value = fallbackRes.data
      } catch (fallbackError) {
        console.error('Fallback RAM request also failed:', fallbackError)
      }
    }
    
    if (romRes.status === 'fulfilled') {
      roms.value = romRes.value.data
    } else {
      // Fallback: try to load all ROMs if active endpoint fails
      try {
        const fallbackRes = await api.get('/api/rom')
        roms.value = fallbackRes.data
      } catch (fallbackError) {
        console.error('Fallback ROM request also failed:', fallbackError)
      }
    }
  } catch (error) {
    console.error('Lỗi khi tải danh sách thuộc tính:', error)
  }
}

function goBack() {
  router.push({ name: 'san-pham' })
}

function editProduct() {
  router.push({ name: 'san-pham-form', params: { id: productId.value } })
}

function formatPrice(price: number) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

function getStatusText(status: number) {
  return status === 1 ? 'Hoạt động' : 'Đã đóng'
}

function getStatusClass(status: number) {
  return status === 1 ? 'status-active' : 'status-inactive'
}

function getColorCode(idMauSac: number | null) {
  if (!idMauSac) return '#ccc'
  
  // Tìm màu sắc trong danh sách đã load từ database
  const mauSac = mauSacs.value.find(m => m.id === idMauSac)
  if (mauSac && mauSac.maMau) {
    // Sử dụng mã màu từ database
    return mauSac.maMau.startsWith('#') ? mauSac.maMau : `#${mauSac.maMau}`
  }
  
  // Fallback về màu mặc định nếu không tìm thấy
  return '#ccc'
}

function getStockStatus(quantity: number) {
  return quantity > 0 ? 'Còn hàng' : 'Hết hàng'
}

function getStockClass(quantity: number) {
  return quantity > 0 ? 'stock-in' : 'stock-out'
}

function createFullImageUrl(url: string): string {
  // Nếu URL đã có protocol, trả về nguyên
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  // Nếu URL bắt đầu với /, thêm domain
  if (url.startsWith('/')) {
    return `http://localhost:8080${url}`
  }
  // Nếu không, thêm domain và /
  return `http://localhost:8080/${url}`
}

// Helper functions to handle soft-deleted attributes
function isAttributeActive(attributeId: number | null, attributeList: any[]): boolean {
  if (!attributeId) return false
  return attributeList.some(attr => attr.id === attributeId)
}

function getAttributeName(attributeId: number | null, attributeList: any[], nameField: string = 'ten'): string {
  if (!attributeId) return 'Chưa cập nhật'
  
  const attribute = attributeList.find(attr => attr.id === attributeId)
  if (attribute) {
    return attribute[nameField] || 'Chưa cập nhật'
  }
  
  return 'Chưa cập nhật'
}

function getHangName(product: any): string {
  // Sử dụng trực tiếp tên từ API thay vì tìm kiếm bằng ID
  return product.tenHang || 'Chưa cập nhật'
}

function getDanhMucName(product: any): string {
  // Sử dụng trực tiếp tên từ API thay vì tìm kiếm bằng ID
  return product.tenDanhMuc || 'Chưa cập nhật'
}

function getHeDieuHanhName(product: any): string {
  // Sử dụng trực tiếp tên từ API thay vì tìm kiếm bằng ID
  return product.tenHeDieuHanh || 'Chưa cập nhật'
}

function getManHinhName(product: any): string {
  // Sử dụng trực tiếp tên từ API thay vì tìm kiếm bằng ID
  return product.tenManHinh || 'Chưa cập nhật'
}

function getCameraTruocName(product: any): string {
  // Sử dụng trực tiếp tên từ API thay vì tìm kiếm bằng ID
  return product.tenCameraTruoc || 'Chưa cập nhật'
}

function getCameraSauName(product: any): string {
  // Sử dụng trực tiếp tên từ API thay vì tìm kiếm bằng ID
  return product.tenCameraSau || 'Chưa cập nhật'
}

function getChipName(product: any): string {
  // Sử dụng trực tiếp tên từ API thay vì tìm kiếm bằng ID
  return product.tenChip || 'Chưa cập nhật'
}

function getGpuName(product: any): string {
  // Sử dụng trực tiếp tên từ API thay vì tìm kiếm bằng ID
  return product.tenGpu || 'Chưa cập nhật'
}

function getCpuName(product: any): string {
  // Sử dụng trực tiếp tên từ API thay vì tìm kiếm bằng ID
  return product.tenCpu || 'Chưa cập nhật'
}

function getPinName(product: any): string {
  // Sử dụng trực tiếp tên từ API thay vì tìm kiếm bằng ID
  return product.tenPin || 'Chưa cập nhật'
}

function getSimName(product: any): string {
  // Sử dụng trực tiếp tên từ API thay vì tìm kiếm bằng ID
  return product.tenSim || 'Chưa cập nhật'
}

// Filtered variants
const filteredVariants = computed(() => {
  if (!product.value?.variants) return []
  
  const k = keyword.value.trim().toLowerCase()
  return product.value.variants.filter((variant) => {
    const matchK = !k || 
      (variant.tenRam && variant.tenRam.toLowerCase().includes(k)) ||
      (variant.tenRom && variant.tenRom.toLowerCase().includes(k)) ||
      (variant.tenMauSac && variant.tenMauSac.toLowerCase().includes(k))
    
    const matchRam = !selectedRam.value || variant.idRam === selectedRam.value
    const matchRom = !selectedRom.value || variant.idRom === selectedRom.value
    const matchMauSac = !selectedMauSac.value || variant.idMauSac === selectedMauSac.value
    const matchStatus = selectedTrangThai.value === 'all' ||
      (selectedTrangThai.value === 'active' ? variant.soLuong > 0 : variant.soLuong === 0)
    
    return matchK && matchRam && matchRom && matchMauSac && matchStatus
  })
})

// Paginated variants
const paginatedVariants = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return filteredVariants.value.slice(start, end)
})

// Pagination computed properties
const totalPages = computed(() => Math.ceil(filteredVariants.value.length / itemsPerPage.value))
>>>>>>> origin/Huan

const startItem = computed(() => {
  return (currentPage.value - 1) * itemsPerPage.value + 1
})

const endItem = computed(() => {
<<<<<<< HEAD
  return Math.min(currentPage.value * itemsPerPage.value, totalItems.value)
})

// Computed property for selected hangs
const selectedChiTietsComputed = computed(() => {
  return chiTiets.value.filter(ct => selectedChiTietIds.value.includes(ct.id))
})

// Computed property for select all checkbox
const isAllSelected = computed(() => {
return paginatedChiTiets.value.length > 0 &&
    paginatedChiTiets.value.every(ct => selectedChiTietIds.value.includes(ct.id))
})

// Methods
async function loadProductDetail() {
  try {
    if (!productId.value) {
      console.warn('No product id provided in route')
      return
    }
    const productResp = await api.get(`/api/san-pham/${productId.value}/view`)
    product.value = productResp.data
    console.log('Loaded product detail:', product.value)
  } catch (e) {
    console.warn('Failed to load base product view:', e)
  }
}

async function loadChiTiets() {
  try {
    loading.value = true
    if (!productId.value) {
      console.warn('No product id provided in route')
      chiTiets.value = []
      return
    }

    const response = await api.get(`/api/san-pham/${productId.value}/chi-tiet`)

    if (!response.data) {
      console.warn('API response data is null or undefined')
      chiTiets.value = []
      return
    }

    const allChiTiets = Array.isArray(response.data) ? response.data : []

    chiTiets.value = allChiTiets

    console.log('=== LOADED CHI TIET SAN PHAM ===')
    console.log('API Response:', response)
    console.log('Total details loaded:', chiTiets.value.length)
    
    // Debug chi tiết từng item
    chiTiets.value.forEach((ct, index) => {
      console.log(`CTSP ${index}:`, {
        id: ct.id,
        ramId: ct.ramId,
        romId: ct.romId,
        mauSacId: ct.mauSacId,
        ram: ct.ram,
        rom: ct.rom,
        mauSac: ct.mauSac,
        tenRam: ct.tenRam,
        tenRom: ct.tenRom,
        tenMauSac: ct.tenMauSac,
        dungLuong: ct.dungLuong,
        tenMau: ct.tenMau,
        resolvedRam: getRamName(ct),
        resolvedRom: getRomName(ct),
        resolvedMau: getMauName(ct),
        soLuong: getSoLuong(ct),
        giaBan: getGiaBan(ct),
        trangThai: ct.trangThai ?? 0
      })
    })
    
    console.log('Available RAMs:', rams.value)
    console.log('Available ROMs:', roms.value)
    console.log('Available MauSacs:', mauSacs.value)
    console.log('Current statusFilter:', statusFilter.value)
    console.log('=== END LOADED CHI TIET ===')
  } catch (error) {
    console.error('Lỗi khi tải chi tiết sản phẩm:', error)
    console.error('Error details:', error.response?.data)
    toastRef.value?.error('Lỗi', 'Không thể tải chi tiết sản phẩm')
    chiTiets.value = []
  } finally {
    loading.value = false
  }
}

async function loadAttributesForLookup() {
  try {
    console.log('=== LOADING ATTRIBUTE LOOKUP LISTS ===')
    
    const [ramRes, romRes, mauRes] = await Promise.all([
      api.get('/api/ram').catch((e) => {
        console.warn('Failed to load RAMs:', e)
        return { data: [] }
      }),
      api.get('/api/rom').catch((e) => {
        console.warn('Failed to load ROMs:', e)
        return { data: [] }
      }),
      api.get('/api/mau-sac').catch((e) => {
        console.warn('Failed to load MauSacs:', e)
        return { data: [] }
      }),
    ])
    
    console.log('RAM API Response:', ramRes)
    console.log('ROM API Response:', romRes)
    console.log('MauSac API Response:', mauRes)
    
    rams.value = Array.isArray(ramRes?.data) ? ramRes.data : []
    roms.value = Array.isArray(romRes?.data) ? romRes.data : []
    mauSacs.value = Array.isArray(mauRes?.data) ? mauRes.data : []
    
    console.log('Loaded RAMs:', rams.value)
    console.log('Loaded ROMs:', roms.value)
    console.log('Loaded MauSacs:', mauSacs.value)
    console.log('=== END LOADING ATTRIBUTE LOOKUP ===')
  } catch (e) {
    console.error('Failed loading attribute lookup lists', e)
  }
}

function applyFilters() {
  currentPage.value = 1
}

function clearAllFilters() {
  searchText.value = ''
  statusFilter.value = ''
  attributeFilter.value = ''
  selectedRamId.value = null
  selectedRomId.value = null
  selectedMauId.value = null
  applyFilters()
}

function goToPage(page: number) {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

function previousPage() {
=======
  const end = currentPage.value * itemsPerPage.value
  return Math.min(end, filteredVariants.value.length)
})

// Pagination methods
function goToPage(page: number) {
  currentPage.value = page
}

function goToFirstPage() {
  currentPage.value = 1
}

function goToPrevPage() {
>>>>>>> origin/Huan
  if (currentPage.value > 1) {
    currentPage.value--
  }
}

<<<<<<< HEAD
function nextPage() {
=======
function goToNextPage() {
>>>>>>> origin/Huan
  if (currentPage.value < totalPages.value) {
    currentPage.value++
  }
}

<<<<<<< HEAD
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

function viewChiTiet(ct: any) {
  console.log('View chi tiet:', ct)
}

function editChiTiet(ct: any) {
  console.log('Edit chi tiet:', ct)
}


// Simple toggle hang status
async function toggleChiTietStatus(ct: any) {
  try {
    const newStatus = (ct.trangThai ?? 0) === 1 ? 0 : 1

    console.log('=== TOGGLE CHI TIET STATUS ===')
    console.log('CTSP ID:', ct.id)
    console.log('Current status:', ct.trangThai)
    console.log('New status:', newStatus)

    const cleanId = parseInt(String(ct.id).replace(/[^\d]/g, ''), 10)
    console.log('Clean ID for API call:', cleanId)

    const response = await api.put(`/api/san-pham/chi-tiet/${cleanId}/status`, {
      trangThai: newStatus
    })

    console.log('API response:', response.status, response.data)

    if (response.data) {
      ct.trangThai = newStatus
      toastRef.value?.success('Thành công', newStatus === 1 ? 'Đã kích hoạt phiên bản!' : 'Đã vô hiệu hóa phiên bản!')
    } else {
      throw new Error('API response data is null')
    }
  } catch (error) {
    console.error('Lỗi khi cập nhật trạng thái chi tiết:', error)
    console.error('Error details:', error.response?.data)
    toastRef.value?.error('Lỗi', 'Không thể cập nhật trạng thái chi tiết')
  }
}

// Checkbox functions
function toggleSelectAll() {
  if (isAllSelected.value) {
    const currentPageIds = paginatedChiTiets.value.map(c => c.id)
    selectedChiTietIds.value = selectedChiTietIds.value.filter(id => !currentPageIds.includes(id))
  } else {
    const currentPageIds = paginatedChiTiets.value.map(c => c.id)
    const newIds = currentPageIds.filter(id => !selectedChiTietIds.value.includes(id))
    selectedChiTietIds.value = [...selectedChiTietIds.value, ...newIds]
  }
}

function toggleSelectChiTiet(chiTietId: number) {
  const index = selectedChiTietIds.value.indexOf(chiTietId)
  if (index > -1) {
    selectedChiTietIds.value.splice(index, 1)
  } else {
    selectedChiTietIds.value.push(chiTietId)
  }
}

function exportToExcel() {
  try {
    // Get selected hangs or all hangs
    const hangsToExport = selectedChiTietsComputed.value.length > 0 ? selectedChiTietsComputed.value : chiTiets.value

    if (hangsToExport.length === 0) {
      toastRef.value?.warning('Cảnh báo', 'Không có dữ liệu chi tiết để xuất Excel')
      return
    }

    // Tạo dữ liệu cho Excel
    const excelData = hangsToExport.map((ct, index) => ({
      'STT': index + 1,
      'RAM': getRamName(ct),
      'ROM': getRomName(ct),
      'Màu': getMauName(ct),
      'Giá bán': formatPrice(getGiaBan(ct)),
      'Số lượng': getSoLuong(ct),
      'Trạng Thái': (ct.trangThai ?? 0) === 1 ? 'Hoạt động' : 'Ngừng hoạt động'
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
    XLSX.utils.book_append_sheet(wb, ws, 'Chi tiết Sản phẩm')

    // Xuất file Excel
    const fileName = `chi_tiet_san_pham_${new Date().toISOString().split('T')[0]}.xlsx`
    XLSX.writeFile(wb, fileName)

    toastRef.value?.success('Thành công', 'Đã export chi tiết sản phẩm thành công!')
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
  loadProductDetail()
  loadAttributesForLookup()
  loadChiTiets()
})

function getRamName(ct: any): string {
  // Try nested object first
  if (ct?.ram?.tenRam) {
    console.log('Found RAM from nested object:', ct.ram.tenRam)
    return ct.ram.tenRam
  }
  
  // Try direct property
  if (ct?.tenRam) {
    console.log('Found RAM from direct property:', ct.tenRam)
    return ct.tenRam
  }
  
  // Try lookup by ID (using ramId instead of idRam)
  if (ct?.ramId) {
    const found = rams.value.find(r => Number(r.id) === Number(ct.ramId))
    if (found?.tenRam) {
      console.log('Found RAM from lookup:', found.tenRam)
      return found.tenRam
    }
  }
  
  console.log('No RAM found for:', ct)
  return ''
}

function getRomName(ct: any): string {
  // Try nested object first
  if (ct?.rom?.dungLuong) {
    console.log('Found ROM from nested object:', ct.rom.dungLuong)
    return ct.rom.dungLuong
  }
  
  // Try direct property
  if (ct?.dungLuong) {
    console.log('Found ROM from direct property:', ct.dungLuong)
    return ct.dungLuong
  }
  
  if (ct?.tenRom) {
    console.log('Found ROM from tenRom property:', ct.tenRom)
    return ct.tenRom
  }
  
  // Try lookup by ID (using romId instead of idRom)
  if (ct?.romId) {
    const found = roms.value.find(r => Number(r.id) === Number(ct.romId))
    if (found?.dungLuong) {
      console.log('Found ROM from lookup:', found.dungLuong)
      return found.dungLuong
    }
  }
  
  console.log('No ROM found for:', ct)
  return ''
}

function getMauName(ct: any): string {
  // Try nested object first
  if (ct?.mauSac?.tenMau) {
    console.log('Found Mau from nested object:', ct.mauSac.tenMau)
    return ct.mauSac.tenMau
  }
  
  // Try direct property
  if (ct?.tenMau) {
    console.log('Found Mau from direct property:', ct.tenMau)
    return ct.tenMau
  }
  
  if (ct?.tenMauSac) {
    console.log('Found Mau from tenMauSac property:', ct.tenMauSac)
    return ct.tenMauSac
  }
  
  // Try lookup by ID (using mauSacId instead of idMauSac)
  if (ct?.mauSacId) {
    const found = mauSacs.value.find(m => Number(m.id) === Number(ct.mauSacId))
    if (found?.tenMau) {
      console.log('Found Mau from lookup:', found.tenMau)
      return found.tenMau
    }
  }
  
  console.log('No Mau found for:', ct)
  return ''
}

function getGiaBan(ct: any): number {
  return (ct?.giaBan ?? ct?.donGia ?? ct?.gia ?? 0) as number
}

function getSoLuong(ct: any): number {
  return (ct?.soLuong ?? 0) as number
}

function formatPrice(value: number | string): string {
  const num = typeof value === 'string' ? Number(value) : (value || 0)
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(num)
}
// Simple modal controls
async function openVariantModal(ct: any) {
  selectedVariant.value = ct
  showVariantModal.value = true
  
  // Try to get IMEI from product variants first
  if (product.value?.variants) {
    const variant = product.value.variants.find(v => v.id === ct.id)
    if (variant?.imeis) {
      variantImeis.value = variant.imeis.map((imei, index) => ({
        id: index + 1,
        soImei: imei,
        trangThai: 1 // Assume active since they're in the variant
      }))
      return
    }
  }
  
  // Fallback to API call
  await loadVariantImeis(ct.id)
=======
function goToLastPage() {
  currentPage.value = totalPages.value
}

// Modal methods
function openVariantModal(variant: VariantDetail) {
  selectedVariant.value = variant
  showVariantModal.value = true
>>>>>>> origin/Huan
}

function closeVariantModal() {
  showVariantModal.value = false
  selectedVariant.value = null
<<<<<<< HEAD
  variantImeis.value = []
}

async function loadVariantImeis(chiTietId: number) {
  try {
    loadingImeis.value = true
    const response = await api.get(`/api/imei/chi-tiet/${chiTietId}`)
    variantImeis.value = Array.isArray(response.data) ? response.data : []
    console.log('Loaded IMEIs for variant:', chiTietId, variantImeis.value)
  } catch (error) {
    console.error('Error loading IMEIs:', error)
    variantImeis.value = []
  } finally {
    loadingImeis.value = false
  }
}

function formatDateTime(dateTime: string | Date): string {
  if (!dateTime) return 'Chưa có'
  const date = new Date(dateTime)
  return date.toLocaleString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
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

/* Variant Detail Modal Styles */
.variant-detail-modal {
  background: white;
  border-radius: 20px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  width: 90%;
  max-width: 900px;
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  animation: modalSlideIn 0.3s ease-out;
}

.variant-info-grid {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.info-section {
  background: #f8fafc;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #e2e8f0;
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 16px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title::before {
  content: '';
  width: 4px;
  height: 20px;
  background: linear-gradient(135deg, #f97316, #ea580c);
  border-radius: 2px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
=======
}

// Reset filters
function resetAllFilters() {
  keyword.value = ''
  selectedRam.value = null
  selectedRom.value = null
  selectedMauSac.value = null
  selectedTrangThai.value = 'all'
  currentPage.value = 1
}

onMounted(async () => {
  await Promise.all([
    loadProductDetail(),
    loadMauSacs(),
    loadAllAttributes()
  ])
})
</script>

<template>
  <div class="page">
    <!-- Header -->
    <div class="header">
      <div class="header-left">
        <button class="btn-back" @click="goBack">← Quay lại</button>
        <h1>Chi Tiết Sản Phẩm</h1>
      </div>
      <div class="header-actions">
        <button class="btn-edit" @click="editProduct">✎ Chỉnh sửa</button>
      </div>
    </div>

    <div v-if="loading" class="loading">
      Đang tải...
    </div>

    <div v-else-if="product" class="content">
      <!-- Product Info Card -->
      <div class="info-card">
        <div class="card-header">
          <h2>Thông tin cơ bản</h2>
        </div>
        <div class="card-content">
          <div class="info-grid">
            <div class="info-item">
              <label>Mã sản phẩm:</label>
              <span>{{ product.maSanPham }}</span>
            </div>
            <div class="info-item">
              <label>Tên sản phẩm:</label>
              <span class="product-name">{{ product.tenSanPham }}</span>
            </div>
            <div class="info-item">
              <label>Hãng:</label>
              <span :class="{ 'attribute-not-updated': getHangName(product) === 'Chưa cập nhật' }">{{ getHangName(product) }}</span>
            </div>
            <div class="info-item">
              <label>Danh mục:</label>
              <span :class="{ 'attribute-not-updated': getDanhMucName(product) === 'Chưa cập nhật' }">{{ getDanhMucName(product) }}</span>
            </div>
            <div class="info-item">
              <label>Hệ điều hành:</label>
              <span :class="{ 'attribute-not-updated': getHeDieuHanhName(product) === 'Chưa cập nhật' }">{{ getHeDieuHanhName(product) }}</span>
            </div>
            <div class="info-item">
              <label>Màn hình:</label>
              <span :class="{ 'attribute-not-updated': getManHinhName(product) === 'Chưa cập nhật' }">{{ getManHinhName(product) }}</span>
            </div>
            <div class="info-item">
              <label>Camera trước:</label>
              <span :class="{ 'attribute-not-updated': getCameraTruocName(product) === 'Chưa cập nhật' }">{{ getCameraTruocName(product) }}</span>
            </div>
            <div class="info-item">
              <label>Camera sau:</label>
              <span :class="{ 'attribute-not-updated': getCameraSauName(product) === 'Chưa cập nhật' }">{{ getCameraSauName(product) }}</span>
            </div>
            <div class="info-item">
              <label>Chip:</label>
              <span :class="{ 'attribute-not-updated': getChipName(product) === 'Chưa cập nhật' }">{{ getChipName(product) }}</span>
            </div>
            <div class="info-item">
              <label>GPU:</label>
              <span :class="{ 'attribute-not-updated': getGpuName(product) === 'Chưa cập nhật' }">{{ getGpuName(product) }}</span>
            </div>
            <div class="info-item">
              <label>CPU:</label>
              <span :class="{ 'attribute-not-updated': getCpuName(product) === 'Chưa cập nhật' }">{{ getCpuName(product) }}</span>
            </div>
            <div class="info-item">
              <label>Pin:</label>
              <span :class="{ 'attribute-not-updated': getPinName(product) === 'Chưa cập nhật' }">{{ getPinName(product) }}</span>
            </div>
            <div class="info-item">
              <label>Sim:</label>
              <span :class="{ 'attribute-not-updated': getSimName(product) === 'Chưa cập nhật' }">{{ getSimName(product) }}</span>
            </div>
            <div class="info-item">
              <label>Tổng số lượng:</label>
              <span class="quantity">{{ product.tongImei || 0 }}</span>
            </div>
            <div class="info-item">
              <label>Trạng thái:</label>
              <span :class="getStatusClass(product.trangThai)">
                {{ getStatusText(product.trangThai) }}
              </span>
            </div>
          </div>
          
          <div class="description">
            <label>Mô tả sản phẩm:</label>
            <div class="description-content">
              <p v-if="product.moTa">{{ product.moTa }}</p>
              <p v-else class="no-description">Chưa có mô tả cho sản phẩm này</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Filter Section -->
      <div v-if="product.variants && product.variants.length > 0" class="filter-card">
        <div class="filter-header">
          <img src="/src/assets/loupe.png" alt="Tìm kiếm" class="filter-icon" />
          <h3>Bộ Lọc Biến Thể</h3>
        </div>

        <div class="filter-content">
          <div class="filter-row">
            <div class="filter-group">
              <label>Tìm kiếm</label>
              <input v-model="keyword" placeholder="Tìm kiếm theo RAM, ROM, màu sắc..." class="search-input" />
            </div>
            <div class="filter-group">
              <label>RAM</label>
              <select v-model.number="selectedRam" class="filter-select">
                <option :value="null">Tất cả</option>
                <option v-for="ram in rams" :key="ram.id" :value="ram.id">{{ ram.tenRam }}</option>
              </select>
            </div>
            <div class="filter-group">
              <label>ROM</label>
              <select v-model.number="selectedRom" class="filter-select">
                <option :value="null">Tất cả</option>
                <option v-for="rom in roms" :key="rom.id" :value="rom.id">{{ rom.dungLuong }}</option>
              </select>
            </div>
          </div>

          <div class="filter-row">
            <div class="filter-group">
              <label>Màu sắc</label>
              <select v-model.number="selectedMauSac" class="filter-select">
                <option :value="null">Tất cả</option>
                <option v-for="mau in mauSacs" :key="mau.id" :value="mau.id">{{ mau.tenMau }}</option>
              </select>
            </div>
            <div class="filter-group">
              <label>Trạng thái</label>
              <div class="radio-group">
                <label class="radio-label">
                  <input type="radio" value="all" v-model="selectedTrangThai" />
                  <span class="radio-custom"></span>
                  Tất cả
                </label>
                <label class="radio-label">
                  <input type="radio" value="active" v-model="selectedTrangThai" />
                  <span class="radio-custom"></span>
                  Còn hàng
                </label>
                <label class="radio-label">
                  <input type="radio" value="inactive" v-model="selectedTrangThai" />
                  <span class="radio-custom"></span>
                  Hết hàng
                </label>
              </div>
            </div>
          </div>
        </div>

        <div class="filter-footer">
          <div class="filter-info">
            Tổng số biến thể: <span class="product-count">{{ filteredVariants.length }}</span>
          </div>
          <div class="filter-actions">
            <button class="btn-secondary" @click="resetAllFilters">Đặt lại bộ lọc</button>
          </div>
        </div>
      </div>

      <!-- Variants Table -->
      <div v-if="product.variants && product.variants.length > 0" class="variants-card">
        <div class="card-header">
          <h2>Danh sách biến thể</h2>
        </div>
        <div class="card-content">
          <div class="table-container">
            <table>
              <thead>
                <tr>
                  <th>STT</th>
                  <th>RAM</th>
                  <th>ROM</th>
                  <th>MÀU SẮC</th>
                  <th>SỐ LƯỢNG</th>
                  <th>ĐƠN GIÁ</th>
                  <th>GIÁ NHẬP</th>
                  <th>TRẠNG THÁI</th>
                  <th>THAO TÁC</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(variant, index) in paginatedVariants" :key="variant.id">
                  <td>{{ startItem + index }}</td>
                  <td>{{ variant.tenRam || '-' }}</td>
                  <td>{{ variant.tenRom || '-' }}</td>
                  <td>
                    <div class="color-display">
                      <div class="color-indicator" :style="{ backgroundColor: getColorCode(variant.idMauSac) }"></div>
                      <span>{{ variant.tenMauSac || '-' }}</span>
                    </div>
                  </td>
                  <td class="quantity">{{ variant.soLuong }}</td>
                  <td class="price">{{ formatPrice(variant.donGia) }}</td>
                  <td class="price">{{ variant.giaNhap ? formatPrice(variant.giaNhap) : 'Chưa cập nhật' }}</td>
                  <td>
                    <span :class="getStockClass(variant.soLuong)">
                      {{ getStockStatus(variant.soLuong) }}
                    </span>
                  </td>
                  <td>
                    <button class="icon-btn" title="Xem chi tiết" @click="openVariantModal(variant)">
                      <img src="/src/assets/view.png" alt="Xem" class="action-icon" />
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Pagination -->
          <div class="pagination-container">
            <div class="pagination-left">
              <span>Hiển thị</span>
              <select v-model="itemsPerPage" class="items-per-page">
                <option value="5">5</option>
                <option value="10">10</option>
                <option value="20">20</option>
                <option value="50">50</option>
              </select>
              <span>mục / trang</span>
            </div>

            <div class="pagination-center">
              Hiển thị {{ startItem }} - {{ endItem }} / {{ filteredVariants.length }} mục
            </div>

            <div class="pagination-right">
              <button class="pagination-btn" @click="goToFirstPage" :disabled="currentPage === 1"><<</button>
              <button class="pagination-btn" @click="goToPrevPage" :disabled="currentPage === 1"><</button>
              <button
                v-for="page in Math.min(5, totalPages)"
                :key="page"
                class="pagination-btn"
                :class="{ active: page === currentPage }"
                @click="goToPage(page)"
              >
                {{ page }}
              </button>
              <button class="pagination-btn" @click="goToNextPage" :disabled="currentPage === totalPages">></button>
              <button class="pagination-btn" @click="goToLastPage" :disabled="currentPage === totalPages">>></button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="error">
      Không tìm thấy sản phẩm
    </div>

    <!-- Variant Detail Modal -->
    <div v-if="showVariantModal" class="modal-overlay" @click="closeVariantModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>Chi tiết biến thể</h3>
          <button class="modal-close" @click="closeVariantModal">×</button>
        </div>
        
        <div class="modal-body" v-if="selectedVariant">
          <div class="variant-detail-grid">
            <div class="detail-section">
              <h4>Thông tin cơ bản</h4>
              <div class="detail-item">
                <label>RAM:</label>
                <span>{{ selectedVariant.tenRam || '-' }}</span>
              </div>
              <div class="detail-item">
                <label>ROM:</label>
                <span>{{ selectedVariant.tenRom || '-' }}</span>
              </div>
              <div class="detail-item">
                <label>Màu sắc:</label>
                <div class="color-display">
                  <div class="color-indicator" :style="{ backgroundColor: getColorCode(selectedVariant.idMauSac) }"></div>
                  <span>{{ selectedVariant.tenMauSac || '-' }}</span>
                </div>
              </div>
              <div class="detail-item">
                <label>Số lượng:</label>
                <span class="quantity">{{ selectedVariant.soLuong }}</span>
              </div>
            </div>

            <div class="detail-section">
              <h4>Thông tin giá</h4>
              <div class="detail-item">
                <label>Đơn giá:</label>
                <span class="price">{{ formatPrice(selectedVariant.donGia) }}</span>
              </div>
              <div class="detail-item">
                <label>Giá nhập:</label>
                <span class="price">{{ selectedVariant.giaNhap ? formatPrice(selectedVariant.giaNhap) : 'Chưa cập nhật' }}</span>
              </div>
            </div>

            <div class="detail-section" v-if="selectedVariant.ghiChu">
              <h4>Ghi chú</h4>
              <div class="detail-item">
                <p>{{ selectedVariant.ghiChu }}</p>
              </div>
            </div>

            <div class="detail-section" v-if="selectedVariant.imageUrls && selectedVariant.imageUrls.length > 0">
              <h4>Hình ảnh ({{ selectedVariant.imageUrls.length }})</h4>
              <div class="images-grid">
                <div v-for="(imageUrl, imgIndex) in selectedVariant.imageUrls" :key="imgIndex" class="image-item">
                  <img :src="createFullImageUrl(imageUrl)" :alt="`${selectedVariant.tenMauSac} - ${imgIndex + 1}`" />
                </div>
              </div>
            </div>

            <div class="detail-section" v-if="selectedVariant.imeis && selectedVariant.imeis.length > 0">
              <h4>IMEI ({{ selectedVariant.imeis.length }})</h4>
              <div class="imeis-list">
                <span v-for="(imei, imeiIndex) in selectedVariant.imeis" :key="imeiIndex" class="imei-item">
                  {{ imei }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Modern Page Layout */
.page {
  padding: 20px;
  background: var(--bg-primary, #f8f9fa);
  min-height: 100vh;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* Modern Header */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  background: white;
  padding: 20px 24px;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.btn-back {
  background: linear-gradient(135deg, #6b7280 0%, #4b5563 100%);
  color: white;
  padding: 12px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.btn-back:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.header h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary, #1f2937);
  background: linear-gradient(135deg, #1f2937, #374151);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.btn-edit {
  background: #28a745;
  color: white;
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-edit:hover {
  background: #218838;
}

/* Loading & Error */
.loading, .error {
  text-align: center;
  padding: 40px;
  font-size: 16px;
  color: #6c757d;
}

/* Modern Cards */
.info-card, .variants-card {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  margin-bottom: 24px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  transition: all 0.3s ease;
}

.info-card:hover, .variants-card:hover {
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
  transform: translateY(-2px);
}

.card-header {
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.card-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  background: linear-gradient(135deg, #1e293b, #475569);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.card-content {
  padding: 24px;
  background: #fafbfc;
}

/* Info Grid */
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
>>>>>>> origin/Huan
}

.info-item {
  display: flex;
  flex-direction: column;
<<<<<<< HEAD
  gap: 6px;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-item label {
  font-weight: 600;
  color: #374151;
  font-size: 14px;
  margin-bottom: 4px;
}

.info-value {
  color: #1e293b;
  font-size: 14px;
  font-weight: 500;
  padding: 8px 12px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  min-height: 20px;
  display: flex;
  align-items: center;
}

.price-value {
  color: #059669;
  font-weight: 600;
  font-size: 15px;
}

.profit-value {
  color: #dc2626;
  font-weight: 600;
  font-size: 15px;
}

.color-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.color-preview {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 2px solid #e5e7eb;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* IMEI Section Styles */
.imei-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e2e8f0;
}

.imei-container {
  margin-top: 8px;
}

.loading-imeis {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px;
  justify-content: center;
  color: #64748b;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid #e5e7eb;
  border-top: 2px solid #f97316;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.no-imeis {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 20px;
  color: #64748b;
  text-align: center;
}

.no-imeis-icon {
  font-size: 24px;
  color: #cbd5e1;
}

.imei-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 200px;
  overflow-y: auto;
  padding: 8px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.imei-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.imei-item:hover {
  border-color: #f97316;
  box-shadow: 0 2px 4px rgba(249, 115, 22, 0.1);
}

.imei-number {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
}

.imei-label {
  font-size: 11px;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.imei-value {
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
  font-family: 'Courier New', monospace;
  letter-spacing: 0.5px;
}

.imei-status {
  display: flex;
  align-items: center;
}

.imei-list::-webkit-scrollbar {
  width: 4px;
}

.imei-list::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 2px;
}

.imei-list::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 2px;
}

.imei-list::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

/* Modal Footer Buttons */
.variant-detail-modal .modal-footer {
  padding: 24px 32px;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.variant-detail-modal .btn-cancel {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: #f3f4f6;
  color: #6b7280;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  cursor: pointer;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s ease;
}

.variant-detail-modal .btn-cancel:hover {
  background: #e5e7eb;
  color: #374151;
  transform: translateY(-1px);
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

  .variant-detail-modal {
    width: 95%;
    margin: 20px;
  }

  .info-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .info-section {
    padding: 16px;
  }
=======
  gap: 4px;
}

.info-item label {
  font-size: 14px;
  font-weight: 500;
  color: #666;
}

.info-item span {
  font-size: 14px;
  color: #333;
}

.product-name {
  color: #007bff;
  font-weight: 600;
}

.attribute-not-updated {
  color: #6c757d;
  font-style: italic;
  font-size: 13px;
}

.quantity {
  color: #28a745;
  font-weight: 600;
}

.price {
  color: #dc3545;
  font-weight: 600;
}

.status-active {
  color: #28a745;
  font-weight: 600;
}

.status-inactive {
  color: #dc3545;
  font-weight: 600;
}

.stock-in {
  background: #28a745;
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.stock-out {
  background: #dc3545;
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

/* Description */
.description {
  margin-top: 20px;
}

.description label {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  display: block;
  margin-bottom: 12px;
  border-bottom: 2px solid #007bff;
  padding-bottom: 4px;
}

.description-content {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  border-left: 4px solid #007bff;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.description-content p {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  margin: 0;
  text-align: justify;
}

.description-content .no-description {
  color: #6c757d;
  font-style: italic;
  text-align: center;
  padding: 20px;
}

/* Variants */
.variants-grid {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.variant-item {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
}

.variant-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f8f9fa;
  border-bottom: 1px solid #e0e0e0;
}

.variant-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.variant-info {
  padding: 16px;
}

.variant-specs {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.spec-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.spec-item label {
  font-size: 12px;
  font-weight: 500;
  color: #666;
}

.spec-item span {
  font-size: 14px;
  color: #333;
}

.color-display {
  display: flex;
  align-items: center;
  gap: 8px;
}

.color-indicator {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid #e0e0e0;
  box-shadow: 0 1px 3px rgba(0,0,0,0.2);
}

/* Images */
.variant-images {
  margin-bottom: 16px;
}

.variant-images label {
  font-size: 14px;
  font-weight: 500;
  color: #666;
  display: block;
  margin-bottom: 8px;
}

.images-grid {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.image-item {
  width: 80px;
  height: 80px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
}

.image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* Variant Note */
.variant-note {
  margin-bottom: 16px;
}

.variant-note label {
  font-size: 14px;
  font-weight: 500;
  color: #666;
  display: block;
  margin-bottom: 8px;
}

.variant-note p {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
  margin: 0;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 4px;
  border-left: 3px solid #007bff;
}

/* IMEIs */
.variant-imeis {
  margin-bottom: 16px;
}

.variant-imeis label {
  font-size: 14px;
  font-weight: 500;
  color: #666;
  display: block;
  margin-bottom: 8px;
}

.imeis-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.imei-item {
  background: #f8f9fa;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #333;
  border: 1px solid #e0e0e0;
}

/* Filter Card Styles */
.filter-card {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  margin-bottom: 24px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  transition: all 0.3s ease;
}

.filter-card:hover {
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
}

.filter-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.filter-icon {
  width: 20px;
  height: 20px;
  object-fit: contain;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}

.filter-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  background: linear-gradient(135deg, #1e293b, #475569);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.filter-content {
  padding: 24px;
  background: #fafbfc;
}

.filter-row {
  display: flex;
  gap: 24px;
  margin-bottom: 20px;
}

.filter-row:last-child {
  margin-bottom: 0;
}

.filter-group {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-group label {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 4px;
}

.search-input {
  padding: 12px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  background: white;
  transition: all 0.3s ease;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.search-input:focus {
  outline: none;
  border-color: #ff6b35;
  box-shadow: 0 0 0 3px rgba(255, 107, 53, 0.1);
  transform: translateY(-1px);
}

.filter-select {
  padding: 12px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  background: white;
  transition: all 0.3s ease;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.filter-select:focus {
  outline: none;
  border-color: #ff6b35;
  box-shadow: 0 0 0 3px rgba(255, 107, 53, 0.1);
  transform: translateY(-1px);
}

.radio-group {
  display: flex;
  gap: 16px;
  align-items: center;
}

.radio-label {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-size: 14px;
}

.radio-label input[type="radio"] {
  display: none;
}

.radio-custom {
  width: 16px;
  height: 16px;
  border: 2px solid #ddd;
  border-radius: 50%;
  position: relative;
  transition: all 0.2s;
}

.radio-label input[type="radio"]:checked + .radio-custom {
  border-color: #28a745;
}

.radio-label input[type="radio"]:checked + .radio-custom::after {
  content: '';
  position: absolute;
  top: 2px;
  left: 2px;
  width: 8px;
  height: 8px;
  background: #28a745;
  border-radius: 50%;
}

.filter-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-top: 1px solid #e0e0e0;
  background: #f8f9fa;
  border-radius: 0 0 8px 8px;
}

.filter-info {
  font-size: 14px;
  color: #333;
}

.product-count {
  color: #28a745;
  font-weight: 600;
}

.filter-actions {
  display: flex;
  gap: 8px;
}

.btn-secondary {
  background: linear-gradient(135deg, #6b7280 0%, #4b5563 100%);
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.btn-secondary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

/* Table Styles */
.table-container {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th,
td {
  border: 1px solid #e0e0e0;
  padding: 12px;
  text-align: center;
}

th {
  background: #f8f9fa;
  font-weight: 600;
  font-size: 14px;
  color: #333;
  text-align: center;
}

td {
  font-size: 14px;
  text-align: center;
}

.quantity {
  color: #28a745;
  font-weight: 600;
}

.price {
  color: #dc3545;
  font-weight: 600;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: center;
}

.icon-btn {
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 8px;
  border-radius: 6px;
  transition: all 0.2s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.icon-btn:hover {
  background: #f8f9fa;
  transform: translateY(-1px);
}

.action-icon {
  width: 18px;
  height: 18px;
  object-fit: contain;
}

/* Pagination Styles */
.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-top: 1px solid #e0e0e0;
  background: #f8f9fa;
  border-radius: 0 0 8px 8px;
}

.pagination-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.items-per-page {
  padding: 4px 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.pagination-center {
  font-size: 14px;
  color: #6c757d;
}

.pagination-right {
  display: flex;
  gap: 4px;
}

.pagination-btn {
  padding: 6px 12px;
  border: 1px solid #ddd;
  background: white;
  cursor: pointer;
  border-radius: 4px;
  font-size: 14px;
}

.pagination-btn:hover:not(:disabled) {
  background: #f8f9fa;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-btn.active {
  background: #007bff;
  color: white;
  border-color: #007bff;
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
  border-radius: 12px;
  max-width: 800px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.modal-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
}

.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #6b7280;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s;
}

.modal-close:hover {
  background: #f3f4f6;
  color: #374151;
}

.modal-body {
  padding: 24px;
}

.variant-detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
}

.detail-section {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  border-left: 4px solid #007bff;
}

.detail-section h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  border-bottom: 2px solid #007bff;
  padding-bottom: 8px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 12px;
}

.detail-item:last-child {
  margin-bottom: 0;
}

.detail-item label {
  font-size: 14px;
  font-weight: 500;
  color: #666;
}

.detail-item span {
  font-size: 14px;
  color: #333;
}

.detail-item p {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
  margin: 0;
  padding: 12px;
  background: white;
  border-radius: 4px;
  border-left: 3px solid #007bff;
>>>>>>> origin/Huan
}
</style>
