<script setup lang="ts">
import { onMounted, ref, computed, watch } from 'vue'
import api from '@/services/api'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useSanPhamStore, type SanPham } from '@/stores/sanPhamStore'
import Toast from '@/components/Toast.vue'
import ConfirmModal from '@/components/ConfirmModal.vue'

interface DanhMuc {
  id: number
  tenDanhMuc: string
}

interface Hang {
  id: number
  ten: string
}

interface Ram { id: number; dungLuong: string }
interface Rom { id: number; dungLuong: string }
interface MauSac { id: number; tenMau: string }
interface HeDieuHanh { id: number; tenHeDieuHanh: string }
interface ManHinh { id: number; kichThuoc: string; doPhanGiai?: string }
interface Pin { id: number; dungLuongPin: string }

const sanPhamStore = useSanPhamStore()
const { items } = storeToRefs(sanPhamStore)
const sanPhams = computed(() => items.value)
const danhMucs = ref<DanhMuc[]>([])
const hangs = ref<Hang[]>([])
const rams = ref<Ram[]>([])
const roms = ref<Rom[]>([])
const mauSacs = ref<MauSac[]>([])
const heDieuHanhs = ref<HeDieuHanh[]>([])
const manHinhs = ref<ManHinh[]>([])
const pins = ref<Pin[]>([])
const loading = ref(false)

// Filters
const keyword = ref('')
const selectedHang = ref<number | null>(null)
const selectedHeDieuHanh = ref<number | null>(null)
const selectedManHinh = ref<number | null>(null)
const selectedPin = ref<number | null>(null)
const selectedTrangThai = ref<'all' | 'active' | 'inactive'>('all')

// Pagination
const currentPage = ref(1)
const itemsPerPage = ref(5)

// Checkbox selection
const selectedProducts = ref<Set<number>>(new Set())
const selectAll = ref(false)
const showConfirmModal = ref(false)
const isUpdatingStatus = ref(false)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

const filteredSanPhams = computed(() => {
  const k = keyword.value.trim().toLowerCase()
  return sanPhams.value.filter((sp) => {
    const matchK = !k || sp.tenSanPham.toLowerCase().includes(k) || sp.maSanPham?.toLowerCase().includes(k)
    const matchHang = !selectedHang.value || sp.tenHang === hangs.value.find(h => h.id === selectedHang.value)?.ten
    const matchHdh = !selectedHeDieuHanh.value || sp.tenHeDieuHanh === heDieuHanhs.value.find(h => h.id === selectedHeDieuHanh.value)?.tenHeDieuHanh
    const matchMh = !selectedManHinh.value || sp.tenManHinh === manHinhs.value.find(m => m.id === selectedManHinh.value)?.kichThuoc
    const matchPin = !selectedPin.value || sp.tenPin === pins.value.find(p => p.id === selectedPin.value)?.dungLuongPin
    const matchStatus = selectedTrangThai.value === 'all' ||
      (selectedTrangThai.value === 'active' ? (sp.tongImei || 0) > 0 : (sp.tongImei || 0) === 0)
    return matchK && matchHang && matchHdh && matchMh && matchPin && matchStatus
  })
})

const paginatedSanPhams = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return filteredSanPhams.value.slice(start, end)
})

const router = useRouter()

function openForm(sanPham?: SanPham) {
  if (sanPham) {
    router.push({ name: 'san-pham-form', params: { id: sanPham.id } })
  } else {
    router.push({ name: 'san-pham-form' })
  }
}

function viewProduct(sanPham: SanPham) {
  router.push({ name: 'san-pham-view', params: { id: sanPham.id } })
}

async function toggleProductStatus(sanPham: SanPham) {
  if (isUpdatingStatus.value) return
  
  console.log('Toggle status for product:', sanPham.tenSanPham, 'Current status:', sanPham.trangThai)
  
  // Kiểm tra xem có thể chuyển đổi không
  if (isToggleDisabled(sanPham)) {
    showToastMessage('Không thể chuyển sang hoạt động vì số lượng = 0', 'warning')
    return
  }
  
  isUpdatingStatus.value = true
  
  try {
    // Chuyển đổi trạng thái: 1 -> 0 hoặc 0 -> 1
    const newStatus = (sanPham.trangThai || 0) === 1 ? 0 : 1
    console.log('Changing status to:', newStatus)
    
    // Gọi API để cập nhật trạng thái sản phẩm
    const response = await api.put(`/api/san-pham/${sanPham.id}/status`, { 
      trangThai: newStatus 
    })
    console.log('API response:', response.data)
    
    // Cập nhật local state
    sanPham.trangThai = newStatus
    console.log('Updated local state:', sanPham.trangThai)
    
    // Hiển thị toast thông báo
    const statusText = newStatus === 1 ? 'Hoạt động' : 'Ngừng hoạt động'
    const message = newStatus === 1 
      ? `Đã chuyển sản phẩm "${sanPham.tenSanPham}" sang trạng thái <span style="color: #28a745; font-weight: bold;">${statusText}</span>`
      : `Đã chuyển sản phẩm "${sanPham.tenSanPham}" sang trạng thái <span style="color: #dc3545; font-weight: bold;">${statusText}</span>`
    showToastMessage(message, 'success')
    
  } catch (error: any) {
    console.error('Lỗi khi cập nhật trạng thái sản phẩm:', error)
    showToastMessage('Không thể cập nhật trạng thái sản phẩm: ' + (error.response?.data?.message || error.message), 'error')
  } finally {
    isUpdatingStatus.value = false
  }
}

async function loadSanPhams() {
  await sanPhamStore.fetchAll()
  console.log('Loaded products:', sanPhams.value.map(sp => ({
    id: sp.id,
    name: sp.tenSanPham,
    status: sp.trangThai,
    quantity: sp.tongImei
  })))
  // Tự động cập nhật trạng thái dựa trên số lượng
  await updateProductStatusBasedOnQuantity()
}

async function updateStatusForZeroQuantityProducts() {
  const productsToUpdate = sanPhams.value.filter(sp => (sp.tongImei || 0) === 0 && sp.trangThai === 1)
  
  for (const product of productsToUpdate) {
    try {
      await api.put(`/api/san-pham/${product.id}/status`, { 
        trangThai: 0 
      })
      product.trangThai = 0
    } catch (error) {
      console.error(`Lỗi khi cập nhật trạng thái sản phẩm ${product.tenSanPham}:`, error)
    }
  }
}

// Function để cập nhật trạng thái khi có thay đổi số lượng
async function updateProductStatusBasedOnQuantity() {
  // Không tự động cập nhật trạng thái khi load page
  // Để khách hàng có thể tự quyết định trạng thái sản phẩm
  // Chỉ tự động cập nhật khi thực sự có thay đổi số lượng (ví dụ: khi sửa sản phẩm)
  console.log('Không tự động cập nhật trạng thái khi load page - để khách hàng tự quyết định')
}

async function loadDanhMucs() {
  const { data } = await api.get<DanhMuc[]>('/api/danh-muc/active')
  danhMucs.value = data
}

async function loadHangs() {
  const { data } = await api.get<Hang[]>('/api/hang/active')
  hangs.value = data
}

async function loadVariantsData() {
  const [ramRes, romRes, mauRes] = await Promise.all([
    api.get<Ram[]>('/api/ram/active'),
    api.get<Rom[]>('/api/rom/active'),
    api.get<MauSac[]>('/api/mau-sac/active'),
  ])
  rams.value = ramRes.data
  roms.value = romRes.data
  mauSacs.value = mauRes.data
}

async function loadFilterLookups() {
  const [hdhRes, mhRes, hangRes, pinRes] = await Promise.all([
    api.get<HeDieuHanh[]>('/api/he-dieu-hanh/active'),
    api.get<ManHinh[]>('/api/man-hinh/active'),
    api.get<Hang[]>('/api/hang/active'),
    api.get<Pin[]>('/api/pin/active'),
  ])
  heDieuHanhs.value = hdhRes.data
  manHinhs.value = mhRes.data
  hangs.value = hangRes.data
  pins.value = pinRes.data
}

async function load() {
  await Promise.all([loadSanPhams(), loadDanhMucs(), loadHangs(), loadVariantsData(), loadFilterLookups()])
}

async function deleteSanPham(id: number) {
  if (confirm('Bạn có chắc muốn xóa sản phẩm này?')) {
    try {
      await api.delete(`/api/san-pham/${id}`)
      await loadSanPhams()
    } catch (error) {
      console.error('Lỗi khi xóa:', error)
    }
  }
}

// Helper functions for table display
function getPriceRange(sanPham: any) {
  const importPrice = getImportPriceText(sanPham)
  const salePrice = getSalePriceText(sanPham)
  
  if (importPrice === 'Chưa có giá' && salePrice === 'Chưa có giá') {
    return 'Chưa có giá'
  }
  
  if (importPrice === 'Chưa có giá') {
    return salePrice
  }
  
  if (salePrice === 'Chưa có giá') {
    return importPrice
  }
  
  return `${importPrice} - ${salePrice}`
}

function getImportPriceText(sanPham: any) {
  if (!sanPham.giaNhapMin && !sanPham.giaNhapMax) {
    return 'Chưa có giá'
  }

  const min = sanPham.giaNhapMin ? formatPrice(sanPham.giaNhapMin) : ''
  const max = sanPham.giaNhapMax ? formatPrice(sanPham.giaNhapMax) : ''

  // Nếu có nhiều phiên bản với giá khác nhau, hiển thị khoảng giá
  if (min && max && min !== max) {
    return `${min} - ${max}`
  } 
  // Nếu chỉ có 1 giá duy nhất
  else if (min) {
    return min
  } else if (max) {
    return max
  }

  return 'Chưa có giá'
}

function getSalePriceText(sanPham: any) {
  if (!sanPham.giaBanMin && !sanPham.giaBanMax) {
    return 'Chưa có giá'
  }

  const min = sanPham.giaBanMin ? formatPrice(sanPham.giaBanMin) : ''
  const max = sanPham.giaBanMax ? formatPrice(sanPham.giaBanMax) : ''

  // Nếu có nhiều phiên bản với giá khác nhau, hiển thị khoảng giá
  if (min && max && min !== max) {
    return `${min} - ${max}`
  } 
  // Nếu chỉ có 1 giá duy nhất
  else if (min) {
    return min
  } else if (max) {
    return max
  }

  return 'Chưa có giá'
}

function formatPrice(price: number) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

function getStatusClass(sanPham: any) {
  const status = sanPham.trangThai || 0
  if (status === 1) {
    return 'status-active'
  } else {
    return 'status-inactive'
  }
}

function getStatusText(sanPham: any) {
  const status = sanPham.trangThai || 0
  return status === 1 ? 'Hoạt động' : 'Ngừng hoạt động'
}

function isToggleDisabled(sanPham: any) {
  const quantity = sanPham.tongImei || 0
  const status = sanPham.trangThai || 0
  
  // Chỉ vô hiệu hóa khi:
  // - Sản phẩm đang ở trạng thái "Ngừng hoạt động" (status = 0)
  // - Và số lượng = 0
  // - Và người dùng muốn chuyển sang "Hoạt động" (toggle sẽ ON)
  return status === 0 && quantity === 0
}

function getToggleTooltip(sanPham: any) {
  const quantity = sanPham.tongImei || 0
  const status = sanPham.trangThai || 0
  
  if (status === 0 && quantity === 0) {
    return 'Không thể chuyển sang hoạt động vì số lượng = 0'
  }
  return ''
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

function getHangName(sanPham: any): string {
  // Sử dụng trực tiếp tên từ API thay vì tìm kiếm bằng ID
  return sanPham.tenHang || 'Chưa cập nhật'
}

function getHeDieuHanhName(sanPham: any): string {
  // Sử dụng trực tiếp tên từ API thay vì tìm kiếm bằng ID
  return sanPham.tenHeDieuHanh || 'Chưa cập nhật'
}

function getManHinhName(sanPham: any): string {
  // Sử dụng trực tiếp tên từ API thay vì tìm kiếm bằng ID
  return sanPham.tenManHinh || 'Chưa cập nhật'
}

function getPinName(sanPham: any): string {
  // Sử dụng trực tiếp tên từ API thay vì tìm kiếm bằng ID
  return sanPham.tenPin || 'Chưa cập nhật'
}

// Pagination computed properties
const totalPages = computed(() => Math.ceil(filteredSanPhams.value.length / itemsPerPage.value))

const startItem = computed(() => {
  return (currentPage.value - 1) * itemsPerPage.value + 1
})

const endItem = computed(() => {
  const end = currentPage.value * itemsPerPage.value
  return Math.min(end, filteredSanPhams.value.length)
})

const visiblePages = computed(() => {
  const pages = []
  const total = totalPages.value
  const current = currentPage.value

  if (total <= 5) {
    for (let i = 1; i <= total; i++) {
      pages.push(i)
    }
  } else {
    if (current <= 3) {
      for (let i = 1; i <= 5; i++) {
        pages.push(i)
      }
    } else if (current >= total - 2) {
      for (let i = total - 4; i <= total; i++) {
        pages.push(i)
      }
    } else {
      for (let i = current - 2; i <= current + 2; i++) {
        pages.push(i)
      }
    }
  }

  return pages
})

// Checkbox computed properties
const selectedCount = computed(() => selectedProducts.value.size)
const hasSelectedProducts = computed(() => selectedProducts.value.size > 0)
const allCurrentPageSelected = computed(() => {
  return paginatedSanPhams.value.length > 0 &&
         paginatedSanPhams.value.every(sp => selectedProducts.value.has(sp.id))
})

// Watch for select all checkbox
watch(selectAll, (newValue) => {
  if (newValue) {
    // Select all products on current page
    paginatedSanPhams.value.forEach(sp => {
      selectedProducts.value.add(sp.id)
    })
  } else {
    // Deselect all products on current page
    paginatedSanPhams.value.forEach(sp => {
      selectedProducts.value.delete(sp.id)
    })
  }
})

// Watch for page changes to update select all checkbox
watch([currentPage, paginatedSanPhams], () => {
  selectAll.value = allCurrentPageSelected.value
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

// Checkbox methods
function toggleProductSelection(productId: number) {
  if (selectedProducts.value.has(productId)) {
    selectedProducts.value.delete(productId)
  } else {
    selectedProducts.value.add(productId)
  }
}

function isProductSelected(productId: number): boolean {
  return selectedProducts.value.has(productId)
}

// Excel export methods
function handleExcelExport() {
  if (!hasSelectedProducts.value) {
    showToastMessage('Vui lòng chọn ít nhất một sản phẩm để tải Excel!', 'warning')
    return
  }

  showConfirmModal.value = true
}

function confirmExcelExport() {
  const selectedProductsList = sanPhams.value.filter(sp => selectedProducts.value.has(sp.id))
  exportToExcel(selectedProductsList)
  showConfirmModal.value = false
}

function exportToExcel(products: SanPham[]) {
  try {
    // Create Excel data
    const excelData = products.map((sp, index) => ({
      'STT': index + 1,
      'Mã sản phẩm': sp.maSanPham || '',
      'Tên sản phẩm': sp.tenSanPham,
      'Hãng': getHangName(sp),
      'Hệ điều hành': getHeDieuHanhName(sp),
      'Màn hình': getManHinhName(sp),
      'Pin': getPinName(sp),
      'Số lượng': sp.tongImei || 0,
      'Giá nhập': getImportPriceText(sp),
      'Giá bán': getSalePriceText(sp),
      'Trạng thái': getStatusText(sp)
    }))

    // Convert to CSV
    const headers = Object.keys(excelData[0])
    const csvContent = [
      headers.join(','),
      ...excelData.map(row =>
        headers.map(header => {
          const value = row[header as keyof typeof row]
          // Escape commas and quotes in CSV
          return typeof value === 'string' && (value.includes(',') || value.includes('"'))
            ? `"${value.replace(/"/g, '""')}"`
            : value
        }).join(',')
      )
    ].join('\n')

    // Create and download file
    const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    link.setAttribute('href', url)
    link.setAttribute('download', `danh_sach_san_pham_${new Date().toISOString().split('T')[0]}.csv`)
    link.style.visibility = 'hidden'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)

    showToastMessage(`Đã tải Excel thành công! (${products.length} sản phẩm)`, 'success')

    // Clear selection after export
    selectedProducts.value.clear()
    selectAll.value = false

  } catch (error) {
    console.error('Lỗi khi tải Excel:', error)
    showToastMessage('Có lỗi xảy ra khi tải Excel!', 'error')
  }
}

// Reset filters method
function resetAllFilters() {
  // Reset search and filters
  keyword.value = ''
  selectedHang.value = null
  selectedHeDieuHanh.value = null
  selectedManHinh.value = null
  selectedPin.value = null
  selectedTrangThai.value = 'all'

  // Reset pagination
  currentPage.value = 1

  // Reset checkbox selection
  selectedProducts.value.clear()
  selectAll.value = false

  showToastMessage('Đã đặt lại tất cả bộ lọc!', 'success')
}

// Toast methods
function showToastMessage(message: string, type: 'success' | 'error' | 'warning' = 'success') {
  if (toastRef.value) {
    const title = type === 'success' ? 'Thành công' : type === 'error' ? 'Lỗi' : 'Cảnh báo'
    toastRef.value[type](title, message, 3000)
  }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <!-- Main Header -->
    <div class="main-header">
      <div class="header-left">
        <img src="@/assets/file.png" alt="Danh sách" class="header-icon" />
        <h1>Danh Sách Sản Phẩm</h1>
      </div>
    </div>

    <!-- Filter Section -->
    <div class="filter-card">
      <div class="filter-header">
        <img src="/src/assets/loupe.png" alt="Tìm kiếm" class="filter-icon" />
        <h3>Bộ Lọc Tìm Kiếm</h3>
      </div>

      <div class="filter-content">
        <div class="filter-row">
          <div class="filter-group">
            <label>Tìm kiếm</label>
            <input v-model="keyword" placeholder="Tìm kiếm theo tên sản phẩm..." class="search-input" />
          </div>
          <div class="filter-group">
            <label>Hãng</label>
            <select v-model.number="selectedHang" class="filter-select">
              <option :value="null">Tất cả</option>
              <option v-for="h in hangs" :key="h.id" :value="h.id">{{ h.ten }}</option>
            </select>
          </div>
          <div class="filter-group">
            <label>Hệ Điều Hành</label>
            <select v-model.number="selectedHeDieuHanh" class="filter-select">
              <option :value="null">Tất cả</option>
              <option v-for="o in heDieuHanhs" :key="o.id" :value="o.id">{{ o.tenHeDieuHanh }}</option>
            </select>
          </div>
        </div>

        <div class="filter-row">
          <div class="filter-group">
            <label>Công nghệ màn hình</label>
            <select v-model.number="selectedManHinh" class="filter-select">
              <option :value="null">Tất cả</option>
              <option v-for="m in manHinhs" :key="m.id" :value="m.id">{{ m.kichThuoc }} {{ m.doPhanGiai?('- '+m.doPhanGiai):'' }}</option>
            </select>
          </div>
          <div class="filter-group">
            <label>Pin</label>
            <select v-model.number="selectedPin" class="filter-select">
              <option :value="null">Tất cả</option>
              <option v-for="p in pins" :key="p.id" :value="p.id">{{ p.dungLuongPin }}</option>
            </select>
          </div>
          <div class="filter-group">
            <label>Trạng Thái Tồn Kho</label>
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
          Tổng số sản phẩm: <span class="product-count">{{ filteredSanPhams.length }}</span>
        </div>
        <div class="filter-actions">
          <button class="btn-secondary" @click="handleExcelExport" :disabled="!hasSelectedProducts">
            Tải Excel {{ hasSelectedProducts ? `(${selectedCount})` : '' }}
          </button>
          <button class="btn-primary" @click="openForm()">Thêm chi tiết sản phẩm</button>
          <button class="btn-secondary" @click="resetAllFilters">Đặt lại bộ lọc</button>
        </div>
      </div>
    </div>

    <!-- Product List Section -->
    <div class="product-list-card">
      <div class="product-list-header">
        <div class="list-header-left">
          <span class="list-icon">📊</span>
          <h3>Danh Sách Sản Phẩm</h3>
        </div>
      </div>

      <div class="product-count-info">
        {{ filteredSanPhams.length }} sản phẩm
      </div>

      <div class="table-container">
        <table>
          <thead>
            <tr>
              <th>
                <input
                  type="checkbox"
                  v-model="selectAll"
                  :indeterminate="hasSelectedProducts && !allCurrentPageSelected"
                />
              </th>
              <th>STT</th>
              <th>TÊN SẢN PHẨM</th>
              <th>HÃNG</th>
              <th>HỆ ĐIỀU HÀNH</th>
              <th>MÀN HÌNH</th>
              <th>PIN</th>
              <th>SỐ LƯỢNG</th>
              <th>KHOẢNG GIÁ</th>
              <th>TRẠNG THÁI</th>
              <th>THAO TÁC</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(sp, idx) in paginatedSanPhams" :key="sp.id">
              <td>
                <input
                  type="checkbox"
                  :checked="isProductSelected(sp.id)"
                  @change="toggleProductSelection(sp.id)"
                />
              </td>
              <td>{{ startItem + idx }}</td>
              <td class="product-name">{{ sp.tenSanPham }}</td>
              <td :class="{ 'attribute-not-updated': getHangName(sp) === 'Chưa cập nhật' }">{{ getHangName(sp) }}</td>
              <td :class="{ 'attribute-not-updated': getHeDieuHanhName(sp) === 'Chưa cập nhật' }">{{ getHeDieuHanhName(sp) }}</td>
              <td :class="{ 'attribute-not-updated': getManHinhName(sp) === 'Chưa cập nhật' }">{{ getManHinhName(sp) }}</td>
              <td :class="{ 'attribute-not-updated': getPinName(sp) === 'Chưa cập nhật' }">{{ getPinName(sp) }}</td>
              <td>{{ sp.tongImei || 0 }}</td>
              <td class="price-cell">
                <div class="price-container">
                  <div v-if="getImportPriceText(sp) === 'Chưa có giá' && getSalePriceText(sp) === 'Chưa có giá'" class="no-price">
                    Chưa có giá
                  </div>
                  <div v-else-if="getImportPriceText(sp) !== 'Chưa có giá' && getSalePriceText(sp) === 'Chưa có giá'" class="import-price">
                    {{ getImportPriceText(sp) }}
                  </div>
                  <div v-else-if="getImportPriceText(sp) === 'Chưa có giá' && getSalePriceText(sp) !== 'Chưa có giá'" class="sale-price">
                    {{ getSalePriceText(sp) }}
                  </div>
                  <template v-else>
                    <div class="import-price">{{ getImportPriceText(sp) }}</div>
                    <div class="sale-price">{{ getSalePriceText(sp) }}</div>
                  </template>
                </div>
              </td>
              <td>
                <span :class="getStatusClass(sp)" class="status-text">
                  {{ getStatusText(sp) }}
                </span>
              </td>
              <td>
                <div class="action-buttons">
                  <button class="icon-btn" title="Xem" @click="viewProduct(sp)">
                    <img src="/src/assets/view.png" alt="Xem" class="action-icon" />
                  </button>
                  <button class="icon-btn" title="Sửa" @click="openForm(sp)">
                    <img src="/src/assets/edit.png" alt="Sửa" class="action-icon" />
                  </button>
                  <div class="status-toggle">
                    <label class="toggle-switch" :title="getToggleTooltip(sp)">
                      <input 
                        type="checkbox" 
                        :checked="(sp.trangThai || 0) === 1"
                        @change="toggleProductStatus(sp)"
                        :disabled="isUpdatingStatus || isToggleDisabled(sp)"
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
          Hiển thị {{ startItem }} - {{ endItem }} / {{ filteredSanPhams.length }} mục
        </div>

        <div class="pagination-right">
          <button class="pagination-btn" @click="goToFirstPage" :disabled="currentPage === 1"><<</button>
          <button class="pagination-btn" @click="goToPrevPage" :disabled="currentPage === 1"><</button>
          <button
            v-for="page in visiblePages"
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

    <!-- Toast Notification -->
    <Toast ref="toastRef" />

    <!-- Confirm Modal -->
    <ConfirmModal
      :show="showConfirmModal"
      title="Xác nhận tải Excel"
      :message="`Bạn có chắc muốn tải Excel cho ${selectedCount} sản phẩm đã chọn?`"
      confirmText="Tải Excel"
      cancelText="Hủy"
      @confirm="confirmExcelExport"
      @cancel="showConfirmModal = false"
    />
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
.main-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  background: white;
  padding: 20px 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  width: 32px;
  height: 32px;
  transition: all 0.3s ease;
}

.header-icon:hover {
  transform: scale(1.1);
}

.main-header h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary, #1f2937);
  background: linear-gradient(135deg, #1f2937, #374151);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* Modern Filter Card */
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

/* Product List Card */
.product-list-card {
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.product-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e0e0e0;
  background: #f8f9fa;
  border-radius: 8px 8px 0 0;
}

.list-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.list-icon {
  font-size: 16px;
}

.product-list-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.product-count-info {
  padding: 12px 20px;
  font-size: 14px;
  color: #6c757d;
  border-bottom: 1px solid #e0e0e0;
}

/* Table */
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

.product-name {
  color: #007bff;
  font-weight: 500;
}

.attribute-not-updated {
  color: #6c757d;
  font-style: italic;
  font-size: 13px;
}

.price-cell {
  text-align: center;
  white-space: nowrap;
  padding: 8px 12px;
}

.price-container {
  display: flex;
  flex-direction: column;
  gap: 2px;
  align-items: center;
}

.import-price {
  font-size: 15px;
  color: #6c757d;
  font-weight: 500;
  line-height: 1.4;
  text-align: center;
}

.sale-price {
  font-size: 16px;
  color: #28a745;
  font-weight: 600;
  line-height: 1.4;
  text-align: center;
}

.no-price {
  font-size: 14px;
  color: #6c757d;
  font-weight: 500;
  line-height: 1.4;
  text-align: center;
  font-style: italic;
}

.status-active {
  background: #28a745;
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-inactive {
  background: #dc3545;
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

/* Status Toggle Styles */
.status-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
}

.toggle-switch {
  position: relative;
  display: inline-block;
  width: 44px;
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
  transition: .4s;
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
  transition: .4s;
  border-radius: 50%;
}

input:checked + .toggle-slider {
  background-color: #28a745;
}

input:checked + .toggle-slider:before {
  transform: translateX(20px);
}

input:disabled + .toggle-slider {
  opacity: 0.6;
  cursor: not-allowed;
  background-color: #6c757d !important;
}

input:disabled + .toggle-slider:before {
  background-color: #f8f9fa;
  border: 1px solid #dee2e6;
}

.status-text-container {
  display: flex;
  justify-content: center;
  width: 100%;
}

.status-text {
  font-size: 11px;
  font-weight: 500;
  text-align: center;
  padding: 2px 6px;
  border-radius: 8px;
  white-space: nowrap;
}

/* Modern Buttons */
.btn-primary {
  background: linear-gradient(135deg, #ff6b35 0%, #fd7e14 100%);
  color: white;
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 6px -1px rgba(255, 107, 53, 0.3);
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 15px -3px rgba(255, 107, 53, 0.4);
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

.btn-secondary:disabled {
  background: #9ca3af;
  cursor: not-allowed;
  opacity: 0.6;
  transform: none;
  box-shadow: none;
}

.btn-success {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(16, 185, 129, 0.3);
}

.btn-success:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(16, 185, 129, 0.4);
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

/* Pagination */
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
</style>
