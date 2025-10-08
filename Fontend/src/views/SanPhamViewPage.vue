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

const startItem = computed(() => {
  return (currentPage.value - 1) * itemsPerPage.value + 1
})

const endItem = computed(() => {
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
  if (currentPage.value > 1) {
    currentPage.value--
  }
}

function goToNextPage() {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
  }
}

function goToLastPage() {
  currentPage.value = totalPages.value
}

// Modal methods
function openVariantModal(variant: VariantDetail) {
  selectedVariant.value = variant
  showVariantModal.value = true
}

function closeVariantModal() {
  showVariantModal.value = false
  selectedVariant.value = null
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
}

.info-item {
  display: flex;
  flex-direction: column;
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
}
</style>
