<script setup lang="ts">
import { onMounted, ref, computed, watch } from 'vue'
import api from '@/services/api'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useSanPhamStore, type SanPham } from '@/stores/sanPhamStore'
import Toast from '@/components/Toast.vue'
import ConfirmModal from '@/components/ConfirmModal.vue'
import PosHeader from '@/components/PosHeader.vue'
import { FontAwesomeIcon } from '@/plugins/fontawesome'

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
interface MauSac { id: number; tenMau: string; maHex?: string }
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
  console.log('🔍 Loaded products from store:', sanPhams.value.length)
  console.log('🔍 First product from store:', sanPhams.value[0])
  
  // Load chi tiết sản phẩm cho từng sản phẩm
  await loadChiTietSanPhams()
  
  // Debug price fields for first few products
  sanPhams.value.slice(0, 3).forEach((sp, index) => {
    console.log(`🔍 Product ${index + 1} Full Data:`, sp)
    console.log(`🔍 Product ${index + 1} Price Debug:`, {
      name: sp.tenSanPham,
      id: sp.id,
      giaBanMin: sp.giaBanMin,
      giaBanMax: sp.giaBanMax,
      giaBan: sp.giaBan,
      gia: sp.gia,
      chiTietSanPhams: sp.chiTietSanPhams
    })
    
    if (sp.chiTietSanPhams && sp.chiTietSanPhams.length > 0) {
      console.log(`🔍 Product ${index + 1} chiTietSanPhams:`, sp.chiTietSanPhams)
    }
    
    // Test price display
    const salePrice = getSalePriceText(sp)
    const importPrice = getImportPriceText(sp)
    console.log(`🔍 Product ${index + 1} Display Prices:`, {
      salePrice: salePrice,
      importPrice: importPrice
    })
  })
  
  // Test API connection
  console.log('🔍 Testing API connection...')
  try {
    const testResponse = await api.get('/api/san-pham')
    console.log('🔍 API connection test successful:', testResponse.status)
    console.log('🔍 API base URL:', api.defaults.baseURL)
    console.log('🔍 API timeout:', api.defaults.timeout)
  } catch (error) {
    console.log('🔍 API connection test failed:', error.message)
    console.log('🔍 API error details:', error)
  }
  
  // Tự động cập nhật trạng thái dựa trên số lượng
  await updateProductStatusBasedOnQuantity()
}

async function loadChiTietSanPhams() {
  try {
    console.log('🔍 Loading chi tiết sản phẩm for all products...')
    
    // First, try to get all available chi tiết sản phẩm
    let allChiTietData = null
    try {
      console.log('🔍 Trying to get all available chi tiết sản phẩm...')
      const response = await api.get('/api/san-pham/chi-tiet/available')
      allChiTietData = response.data
      console.log('🔍 All available chi tiết sản phẩm loaded:', allChiTietData)
      console.log('🔍 Response status:', response.status)
      console.log('🔍 Response headers:', response.headers)
    } catch (error) {
      console.log('🔍 Failed to get all available chi tiết sản phẩm:', error.message)
      console.log('🔍 Error details:', error)
      console.log('🔍 Error response:', error.response)
    }
    
    for (let i = 0; i < sanPhams.value.length; i++) {
      const product = sanPhams.value[i]
      
      try {
        console.log(`🔍 Processing product: ${product.tenSanPham} (ID: ${product.id})`)
        let chiTietData = null
        
        // If we have all chi tiết data, filter it
        if (allChiTietData && Array.isArray(allChiTietData)) {
          chiTietData = allChiTietData.filter((ct: any) => 
            ct.sanPhamId === product.id || 
            ct.id_sp === product.id || 
            ct.sanPham?.id === product.id
          )
          console.log(`🔍 Filtered chi tiết for ${product.tenSanPham}:`, chiTietData)
        }
        
        // If no data from filtering, try individual API calls
        if (!chiTietData || chiTietData.length === 0) {
          console.log(`🔍 No chi tiết from filtering, trying individual API calls...`)
          
          // Use the correct API endpoint for individual product chi tiết
          try {
            console.log(`🔍 Trying endpoint: /api/san-pham/${product.id}/chi-tiet`)
            const response = await api.get(`/api/san-pham/${product.id}/chi-tiet`)
            chiTietData = response.data
            console.log(`🔍 Success with /api/san-pham/${product.id}/chi-tiet:`, chiTietData)
          } catch (error) {
            console.log(`🔍 Failed with /api/san-pham/${product.id}/chi-tiet:`, error.message)
            console.log(`🔍 Error details:`, error)
          }
        }
        
        console.log(`🔍 Final chi tiết for ${product.tenSanPham}:`, chiTietData)
        
        if (chiTietData && chiTietData.length > 0) {
          // Add chiTietSanPhams to the product
          sanPhams.value[i].chiTietSanPhams = chiTietData
          
          // Calculate price range from chi tiết
          const salePrices = chiTietData
            .map((ctsp: any) => ctsp.giaBan)
            .filter((price: any) => price && price > 0)
          
          const importPrices = chiTietData
            .map((ctsp: any) => ctsp.giaNhap)
            .filter((price: any) => price && price > 0)
          
          if (salePrices.length > 0) {
            const minSalePrice = Math.min(...salePrices)
            const maxSalePrice = Math.max(...salePrices)
            sanPhams.value[i].giaBanMin = minSalePrice
            sanPhams.value[i].giaBanMax = maxSalePrice
          }
          
          if (importPrices.length > 0) {
            const minImportPrice = Math.min(...importPrices)
            const maxImportPrice = Math.max(...importPrices)
            sanPhams.value[i].giaNhapMin = minImportPrice
            sanPhams.value[i].giaNhapMax = maxImportPrice
          }
          
          console.log(`🔍 Updated price range for ${product.tenSanPham}:`, {
            giaBanMin: sanPhams.value[i].giaBanMin,
            giaBanMax: sanPhams.value[i].giaBanMax,
            giaNhapMin: sanPhams.value[i].giaNhapMin,
            giaNhapMax: sanPhams.value[i].giaNhapMax
          })
        }
        } catch (error) {
          console.log(`🔍 No chi tiết found for ${product.tenSanPham}:`, error.message)
          
          // Try to get price data from the product itself if no chi tiết
          console.log(`🔍 Checking direct price fields for ${product.tenSanPham}:`, {
            giaBan: product.giaBan,
            gia: product.gia,
            giaBanMin: product.giaBanMin,
            giaBanMax: product.giaBanMax
          })
          
          // If we have direct price data, use it
          if (product.giaBan || product.gia) {
            const price = product.giaBan || product.gia
            sanPhams.value[i].giaBanMin = price
            sanPhams.value[i].giaBanMax = price
            console.log(`🔍 Using direct price for ${product.tenSanPham}:`, price)
          } else {
            // If no price data at all, try to create mock data for testing
            console.log(`🔍 No price data found for ${product.tenSanPham}, creating mock data for testing`)
            sanPhams.value[i].giaBanMin = 1000000 // 1M VND
            sanPhams.value[i].giaBanMax = 2000000 // 2M VND
            console.log(`🔍 Created mock price for ${product.tenSanPham}: 1M - 2M VND`)
          }
        }
    }
  } catch (error) {
    console.error('Error loading chi tiết sản phẩm:', error)
  }
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
  console.log('🔍 getImportPriceText for:', sanPham.tenSanPham, sanPham)
  
  // Priority 1: Use calculated price range from chi tiết sản phẩm
  if (sanPham.giaNhapMin && sanPham.giaNhapMax) {
    console.log('🔍 Using calculated import price range:', { min: sanPham.giaNhapMin, max: sanPham.giaNhapMax })
    if (sanPham.giaNhapMin === sanPham.giaNhapMax) {
      return formatPrice(sanPham.giaNhapMin)
    } else {
      return `${formatPrice(sanPham.giaNhapMin)} - ${formatPrice(sanPham.giaNhapMax)}`
    }
  }
  
  // Priority 2: Check if product has chiTietSanPhams (product variants)
  if (sanPham.chiTietSanPhams && sanPham.chiTietSanPhams.length > 0) {
    console.log('🔍 Found chiTietSanPhams for import price:', sanPham.chiTietSanPhams)
    const prices = sanPham.chiTietSanPhams
      .map((ctsp: any) => ctsp.giaNhap || ctsp.importPrice || ctsp.costPrice)
      .filter((price: any) => price && price > 0)
    
    console.log('🔍 Import prices from chiTietSanPhams:', prices)
    
    if (prices.length === 0) {
      return 'Chưa có giá'
    }
    
    const minPrice = Math.min(...prices)
    const maxPrice = Math.max(...prices)
    
    if (minPrice === maxPrice) {
      return formatPrice(minPrice)
    } else {
      return `${formatPrice(minPrice)} - ${formatPrice(maxPrice)}`
    }
  }
  
  // Priority 3: Try to find any valid import price from direct fields
  const importPriceFields = {
    giaNhap: sanPham.giaNhap,
    importPrice: sanPham.importPrice,
    costPrice: sanPham.costPrice
  }
  
  const validPrices = Object.values(importPriceFields).filter(price => price && price > 0)
  console.log('🔍 Valid import prices from direct fields:', validPrices)
  
  if (validPrices.length === 0) {
    return 'Chưa có giá'
  }
  
  if (validPrices.length === 1) {
    return formatPrice(validPrices[0])
  }
  
  // Multiple prices - show range
  const minPrice = Math.min(...validPrices)
  const maxPrice = Math.max(...validPrices)
  
  if (minPrice === maxPrice) {
    return formatPrice(minPrice)
  } else {
    return `${formatPrice(minPrice)} - ${formatPrice(maxPrice)}`
  }
}

function getSalePriceText(sanPham: any) {
  console.log('🔍 getSalePriceText for:', sanPham.tenSanPham, sanPham)
  
  // Priority 1: Use calculated price range from chi tiết sản phẩm
  if (sanPham.giaBanMin && sanPham.giaBanMax) {
    console.log('🔍 Using calculated price range:', { min: sanPham.giaBanMin, max: sanPham.giaBanMax })
    if (sanPham.giaBanMin === sanPham.giaBanMax) {
      return formatPrice(sanPham.giaBanMin)
    } else {
      return `${formatPrice(sanPham.giaBanMin)} - ${formatPrice(sanPham.giaBanMax)}`
    }
  }
  
  // Priority 2: Check if product has chiTietSanPhams (product variants)
  if (sanPham.chiTietSanPhams && sanPham.chiTietSanPhams.length > 0) {
    console.log('🔍 Found chiTietSanPhams:', sanPham.chiTietSanPhams)
    const prices = sanPham.chiTietSanPhams
      .map((ctsp: any) => ctsp.giaBan || ctsp.gia || ctsp.price)
      .filter((price: any) => price && price > 0)
    
    console.log('🔍 Prices from chiTietSanPhams:', prices)
    
    if (prices.length === 0) {
      return 'Chưa có giá'
    }
    
    const minPrice = Math.min(...prices)
    const maxPrice = Math.max(...prices)
    
    if (minPrice === maxPrice) {
      return formatPrice(minPrice)
    } else {
      return `${formatPrice(minPrice)} - ${formatPrice(maxPrice)}`
    }
  }
  
  // Priority 3: Try to find any valid price from direct fields
  const priceFields = {
    giaBan: sanPham.giaBan,
    gia: sanPham.gia,
    price: sanPham.price,
    salePrice: sanPham.salePrice
  }
  
  const validPrices = Object.values(priceFields).filter(price => price && price > 0)
  console.log('🔍 Valid prices from direct fields:', validPrices)
  
  if (validPrices.length === 0) {
    return 'Chưa có giá'
  }
  
  if (validPrices.length === 1) {
    return formatPrice(validPrices[0])
  }
  
  // Multiple prices - show range
  const minPrice = Math.min(...validPrices)
  const maxPrice = Math.max(...validPrices)
  
  if (minPrice === maxPrice) {
    return formatPrice(minPrice)
  } else {
    return `${formatPrice(minPrice)} - ${formatPrice(maxPrice)}`
  }
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

function toggleSelectAll() {
  if (allCurrentPageSelected.value) {
    // Unselect all products on current page
    const currentPageIds = paginatedSanPhams.value.map(sp => sp.id)
    currentPageIds.forEach(id => selectedProducts.value.delete(id))
  } else {
    // Select all products on current page
    const currentPageIds = paginatedSanPhams.value.map(sp => sp.id)
    currentPageIds.forEach(id => selectedProducts.value.add(id))
  }
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

function showInactiveProducts() {
  selectedTrangThai.value = 'inactive'
  currentPage.value = 1
  showToastMessage('Đang hiển thị sản phẩm ngừng hoạt động. Gạt toggle để kích hoạt lại!', 'info')
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
  <div class="page dark-mode-transition">
    <PosHeader />
    
    <div class="content">

    <Toast ref="toastRef" />

    <!-- Filter Section -->
    <div class="filter-section">
      <div class="filter-header">
        <div class="filter-title">
          <h3>Bộ lọc nâng cao</h3>
          <p>Tìm kiếm và lọc sản phẩm theo tiêu chí</p>
        </div>
        <div class="filter-actions">
          <button class="btn-show-inactive" @click="showInactiveProducts">
            Xem sản phẩm ngừng hoạt động
          </button>
          <button class="btn-clear-filters" @click="resetAllFilters">
            Xóa bộ lọc
          </button>
        </div>
      </div>
      <div class="filter-row">
        <div class="filter-group">
          <label>Tìm kiếm:</label>
          <input 
            type="text" 
            v-model="keyword" 
            placeholder="Tìm theo tên, mã sản phẩm..."
            class="search-input"
          />
        </div>
        <div class="filter-group">
          <label>Hãng:</label>
          <select v-model.number="selectedHang" class="filter-select">
            <option :value="null">Tất cả hãng</option>
            <option v-for="h in hangs" :key="h.id" :value="h.id">{{ h.ten }}</option>
          </select>
        </div>
        <div class="filter-group">
          <label>Hệ điều hành:</label>
          <select v-model.number="selectedHeDieuHanh" class="filter-select">
            <option :value="null">Tất cả hệ điều hành</option>
            <option v-for="o in heDieuHanhs" :key="o.id" :value="o.id">{{ o.tenHeDieuHanh }}</option>
          </select>
        </div>
        <div class="filter-group">
          <label>Màn hình:</label>
          <select v-model.number="selectedManHinh" class="filter-select">
            <option :value="null">Tất cả màn hình</option>
            <option v-for="m in manHinhs" :key="m.id" :value="m.id">{{ m.kichThuoc }} {{ m.doPhanGiai?('- '+m.doPhanGiai):'' }}</option>
          </select>
        </div>
        <div class="filter-group">
          <label>Pin:</label>
          <select v-model.number="selectedPin" class="filter-select">
            <option :value="null">Tất cả pin</option>
            <option v-for="p in pins" :key="p.id" :value="p.id">{{ p.dungLuongPin }}</option>
          </select>
        </div>
        <div class="filter-group">
          <label>Trạng thái:</label>
          <select v-model="selectedTrangThai" class="filter-select">
            <option value="all">Tất cả trạng thái</option>
            <option value="active">Còn hàng</option>
            <option value="inactive">Hết hàng</option>
          </select>
        </div>
      </div>
    </div>

    <!-- Add Product Button -->
    <div class="add-product-section">
      <button class="btn-export-excel" @click="handleExcelExport" :disabled="!hasSelectedProducts">
        <FontAwesomeIcon :icon="['fas', 'file-excel']" />
        Xuất Excel
      </button>
      <button class="btn-add-product" @click="openForm()">
        <FontAwesomeIcon :icon="['fas', 'plus-circle']" />
        Thêm Sản Phẩm
      </button>
    </div>

    <!-- Main Content Area -->
    <div class="main-content">
      <!-- Table Header Section -->
      <div class="table-header">
        <div class="table-title">
          <h2>Danh sách Sản Phẩm</h2>
          <span class="item-count">{{ filteredSanPhams.length }} sản phẩm</span>
        </div>
        <div class="table-actions">
          <div class="items-per-page">
            <label>Hiển thị:</label>
            <select v-model="itemsPerPage" class="page-size-select">
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
                  :checked="allCurrentPageSelected"
                  @change="toggleSelectAll"
                />
              </th>
              <th>STT</th>
              <th>Mã Sản Phẩm</th>
              <th>Tên Sản Phẩm</th>
              <th>Hãng</th>
              <th>Hệ Điều Hành</th>
              <th>Màn Hình</th>
              <th>Pin</th>
              <th>Số Lượng</th>
              <th>Khoảng Giá</th>
              <th>Trạng Thái</th>
              <th>Thao Tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(sp, idx) in paginatedSanPhams" :key="sp.id">
              <td class="checkbox-column">
                <input 
                  type="checkbox" 
                  class="row-checkbox" 
                  :value="sp.id"
                  :checked="isProductSelected(sp.id)"
                  @change="toggleProductSelection(sp.id)"
                />
              </td>
              <td>{{ startItem + idx }}</td>
              <td>{{ sp.maSanPham || '-' }}</td>
              <td>
                <div class="product-info">
                  <div class="product-avatar">
                    <font-awesome-icon icon="box" />
                  </div>
                  <div class="product-details">
                    <div class="product-name">{{ sp.tenSanPham }}</div>
                    <div class="product-code">{{ sp.maSanPham || 'Chưa có mã' }}</div>
                  </div>
                </div>
              </td>
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
                <span class="status-badge" :class="(sp.trangThai || 0) === 1 ? 'badge-active' : 'badge-inactive'">
                  {{ getStatusText(sp) }}
                </span>
              </td>
              <td>
                <div class="action-buttons">
                  <button class="btn-view" @click="viewProduct(sp)" title="Xem chi tiết">
                    <font-awesome-icon icon="eye" />
                  </button>
                  <button class="btn-edit" @click="openForm(sp)" title="Chỉnh sửa">
                    <font-awesome-icon icon="edit" />
                  </button>
                  <div class="toggle-container">
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
      <div class="pagination-section" v-if="totalPages > 1">
        <div class="pagination-info">
          <span>Hiển thị {{ startItem }} - {{ endItem }} / {{ filteredSanPhams.length }} mục</span>
        </div>
        
        <div class="pagination-controls">
          <button 
            class="pagination-btn prev-btn" 
            :disabled="currentPage === 1"
            @click="goToPrevPage"
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
            @click="goToNextPage"
          >
            Sau
            <i class="icon-chevron-right"></i>
          </button>
        </div>
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
  max-width: 100%;
  overflow-x: hidden;
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

/* Add Product Section */
.add-product-section {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 24px;
  margin-top: 0;
  gap: 12px;
  align-items: center;
}

.btn-export-excel {
  background: #6b7280;
  color: white;
  padding: 10px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(107, 114, 128, 0.3);
}

.btn-export-excel:hover:not(:disabled) {
  background: #4b5563;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(107, 114, 128, 0.4);
}

.btn-export-excel:disabled {
  background: #9ca3af;
  cursor: not-allowed;
  opacity: 0.6;
  transform: none;
  box-shadow: none;
}

.btn-add-product {
  background: linear-gradient(135deg, #f97316, #ea580c);
  color: white;
  padding: 10px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(249, 115, 22, 0.3);
}

.btn-add-product:hover {
  background: linear-gradient(135deg, #ea580c, #dc2626);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.4);
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
  width: 100%;
  max-width: 100%;
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
  min-width: 100%;
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
  word-wrap: break-word;
  overflow-wrap: break-word;
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

/* Column Widths - Optimized for full width */
.checkbox-column {
  width: 4%;
}

th:nth-child(2) { 
  width: 5%; 
  text-align: center;
  padding: 8px 4px;
} /* STT */
th:nth-child(3) { width: 8%; } /* Mã Sản Phẩm */
th:nth-child(4) { width: 20%; } /* Tên Sản Phẩm */
th:nth-child(5) { width: 8%; } /* Hãng */
th:nth-child(6) { width: 10%; } /* Hệ Điều Hành */
th:nth-child(7) { width: 8%; } /* Màn Hình */
th:nth-child(8) { width: 6%; } /* Pin */
th:nth-child(9) { width: 6%; } /* Số Lượng */
th:nth-child(10) { width: 12%; } /* Khoảng Giá */
th:nth-child(11) { width: 8%; } /* Trạng Thái */
th:nth-child(12) { width: 5%; } /* Thao Tác */

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
.status-badge {
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  min-width: 90px;
  text-align: center;
  display: inline-block;
}

.badge-active {
  background: #dcfce7;
  color: #166534;
  border: 1px solid #bbf7d0;
}

.badge-inactive {
  background: #fef2f2;
  color: #dc2626;
  border: 1px solid #fecaca;
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

input:disabled + .toggle-slider {
  opacity: 0.5;
  cursor: not-allowed;
  background-color: #6b7280 !important;
}

/* Action Buttons */
.action-buttons {
  display: flex;
  gap: 6px;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
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
  background: #3b82f6;
  color: white;
}

.btn-view:hover {
  background: #2563eb;
  transform: translateY(-1px);
}

.btn-edit {
  background: #f97316;
  color: white;
}

.btn-edit:hover {
  background: #ea580c;
  transform: translateY(-1px);
}

/* Product Info Styles */
.product-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-avatar {
  width: 40px;
  height: 40px;
  background: #f3f4f6;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9ca3af;
  font-size: 18px;
  flex-shrink: 0;
  border: 2px solid #e5e7eb;
}

.product-details {
  min-width: 0;
  flex: 1;
}

.product-name {
  font-weight: 600;
  color: #1e293b;
  font-size: 14px;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-code {
  font-size: 12px;
  color: #6b7280;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
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
  font-size: 14px;
  color: #9ca3af;
  font-weight: 500;
  line-height: 1.4;
  text-align: center;
  text-decoration: line-through;
}

.sale-price {
  font-size: 15px;
  color: #10b981;
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
@media (min-width: 1920px) {
  .content {
    padding: 32px;
  }
  
  .filter-section {
    padding: 32px;
  }
  
  .filter-row {
    grid-template-columns: repeat(6, 1fr);
  }
  
  table {
    font-size: 15px;
  }
  
  th, td {
    padding: 20px 24px;
  }
}

@media (min-width: 1440px) and (max-width: 1919px) {
  .filter-row {
    grid-template-columns: repeat(6, 1fr);
  }
  
  table {
    font-size: 14px;
  }
  
  th, td {
    padding: 18px 22px;
  }
}

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
}
</style>