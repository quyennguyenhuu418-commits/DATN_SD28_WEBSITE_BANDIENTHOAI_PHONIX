<template>
  <div class="form-page">
    <PosHeader />
    <div class="form-container">
      <div class="form-header">
        <div class="header-left">
          <button class="btn-back" @click="goBack">
            <FontAwesomeIcon :icon="['fas', 'arrow-left']" />
            Quay lại
          </button>
          <h1>{{ isEdit ? 'Sửa Đợt Giảm Giá' : 'Thêm Đợt Giảm Giá' }}</h1>
        </div>
      </div>
      <form @submit.prevent="handleSubmit" class="promotion-form">
        <div class="form-section">
          <h2>Thông Tin Cơ Bản</h2>
          <div class="form-row">
            <div class="form-group">
              <label for="maKhuyenMai">Mã Đợt Giảm Giá:</label>
              <input
                id="maKhuyenMai"
                type="text"
                v-model="formData.maKhuyenMai"
                placeholder="Để trống để tự động tạo"
              />
            </div>
            <div class="form-group">
              <label for="tenKhuyenMai">Tên Đợt Giảm Giá: <span class="required">*</span></label>
              <input
                id="tenKhuyenMai"
                type="text"
                v-model="formData.tenKhuyenMai"
                :class="{ 'error': errors.tenKhuyenMai }"
                required
                placeholder="Nhập tên đợt giảm giá"
              />
              <span v-if="errors.tenKhuyenMai" class="error-message">{{ errors.tenKhuyenMai }}</span>
            </div>
          </div>

          <div class="form-group">
            <label for="moTa">Mô Tả:</label>
            <textarea
              id="moTa"
              v-model="formData.moTa"
              rows="3"
              placeholder="Mô tả chi tiết về đợt giảm giá"
            ></textarea>
          </div>
        </div>

        <div class="form-section">
          <h2>Cài Đặt Giảm Giá</h2>
          
          <!-- Slider cho Giảm Theo Phần Trăm -->
          <div class="form-group">
              <label for="phanTramGiam">Giá trị giảm (%): <span class="required">*</span></label>
            <div class="input-with-unit">
                <input
                  id="phanTramGiam"
                type="number"
                  v-model="formData.phanTramGiam"
                  :class="{ 'error': errors.phanTramGiam }"
                  required
                  min="0"
                  max="50"
                  step="0.1"
                placeholder="Nhập phần trăm giảm giá"
                />
              <span class="input-unit">%</span>
              </div>
              <span v-if="errors.phanTramGiam" class="error-message">{{ errors.phanTramGiam }}</span>
          </div>

        </div>

        <div class="form-section">
          <h2>Sản Phẩm Áp Dụng</h2>
          <div class="product-selection-section">
            <div class="product-selection-header">
              <p class="help-text">Chọn sản phẩm để áp dụng đợt giảm giá này. Nếu không chọn, giảm giá sẽ áp dụng cho tất cả sản phẩm.</p>
            </div>
            
            <!-- Thanh lọc sản phẩm -->
            <div class="product-filters">
              <div class="filter-group">
                <label>Tìm kiếm:</label>
                <input
                  v-model="productSearchKeyword"
                  type="text"
                  placeholder="Tìm theo tên sản phẩm..."
                  class="filter-input"
                />
              </div>
              <div class="filter-group">
                <label>Danh mục:</label>
                <select v-model="productCategoryFilter" class="filter-select">
                  <option value="">Tất cả danh mục</option>
                  <option v-for="category in categories" :key="category.id" :value="category.id">
                    {{ category.tenDanhMuc }}
                  </option>
                </select>
              </div>
              <div class="filter-group">
                <label>Hãng:</label>
                <select v-model="productBrandFilter" class="filter-select">
                  <option value="">Tất cả hãng</option>
                  <option v-for="brand in brands" :key="brand.id" :value="brand.id">
                    {{ brand.ten }}
                  </option>
                </select>
              </div>
            </div>

            <!-- Bảng sản phẩm -->
            <div class="products-table-container">
              <table class="products-table">
                <thead>
                  <tr>
                    <th>
                      <input
                        type="checkbox"
                        :checked="allProductsSelected"
                        @change="toggleAllProducts"
                        class="select-all-checkbox"
                      />
                    </th>
                    <th>Hình ảnh</th>
                    <th>Tên sản phẩm</th>
                    <th>Danh mục</th>
                    <th>Hãng</th>
                    <th>Giá bán</th>
                    <th>Trạng thái</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="product in filteredProducts" :key="product.chiTietSanPhamId" class="product-row">
                    <td>
                      <input
                        type="checkbox"
                        :checked="isProductSelected(product.chiTietSanPhamId)"
                        @change="toggleProductSelection(product)"
                        class="product-checkbox"
                      />
                    </td>
                    <td class="product-image-cell">
                      <img 
                        v-if="product.hinhAnh" 
                        :src="getImageUrl(product.hinhAnh)" 
                        :alt="product.tenSanPham" 
                        class="product-image" 
                      />
                    <div v-else class="product-image-placeholder">
                      <FontAwesomeIcon :icon="['fas', 'image']" />
                    </div>
                    </td>
                    <td class="product-name">{{ product.tenSanPham }}</td>
                    <td class="product-category">{{ getCategoryNameFor(product) }}</td>
                    <td class="product-brand">{{ product.tenHang }}</td>
                    <td class="product-price">{{ formatCurrency(product.gia) }}</td>
                    <td class="product-status">
                      <span :class="`status-badge ${product.trangThai === 1 ? 'active' : 'inactive'}`">
                        {{ product.trangThai === 1 ? 'Hoạt động' : 'Tạm dừng' }}
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
              
              <div v-if="filteredProducts.length === 0" class="no-products">
                <FontAwesomeIcon :icon="['fas', 'search']" />
                <p>Không tìm thấy sản phẩm nào phù hợp</p>
                    </div>
                  </div>

            <!-- Thống kê sản phẩm đã chọn -->
            <div v-if="selectedProducts.length > 0" class="selected-products-summary">
              <h3>Đã chọn {{ selectedProducts.length }} sản phẩm</h3>
              <button type="button" class="btn-clear-selection" @click="clearAllSelections">
                    <FontAwesomeIcon :icon="['fas', 'times']" />
                Xóa tất cả
                  </button>
            </div>
          </div>
        </div>

        <div class="form-section">
          <h2>Thời Gian Áp Dụng</h2>
          <div class="form-row">
            <div class="form-group">
              <label for="ngayBatDau">Ngày Bắt Đầu: <span class="required">*</span></label>
              <input
                id="ngayBatDau"
                type="date"
                v-model="formData.ngayBatDau"
                :class="{ 'error': errors.ngayBatDau }"
                required
              />
              <span v-if="errors.ngayBatDau" class="error-message">{{ errors.ngayBatDau }}</span>
            </div>
            <div class="form-group">
              <label for="ngayKetThuc">Ngày Kết Thúc: <span class="required">*</span></label>
              <input
                id="ngayKetThuc"
                type="date"
                v-model="formData.ngayKetThuc"
                :class="{ 'error': errors.ngayKetThuc }"
                required
              />
              <span v-if="errors.ngayKetThuc" class="error-message">{{ errors.ngayKetThuc }}</span>
            </div>
          </div>
        </div>

        <div class="form-section">
          <h2>Cài Đặt Khác</h2>
          <div class="form-row">
            <div class="form-group">
              <label class="checkbox-label">
                <input type="checkbox" v-model="formData.trangThai" />
                <span class="checkmark"></span>
                Kích hoạt ngay sau khi tạo
              </label>
            </div>
          </div>
        </div>

        <div class="form-actions">
          <button type="button" @click="goBack" class="btn-cancel">Hủy</button>
          <button type="submit" class="btn-submit" :disabled="loading">
            <span v-if="loading">Đang lưu...</span>
            <span v-else>{{ isEdit ? 'Cập nhật' : 'Tạo mới' }}</span>
          </button>
        </div>
      </form>
    </div>

    <Toast ref="toastRef" />
    
    <!-- Product Selection Modal -->
    <div v-if="showProductModal" class="modal-overlay" @click.self="showProductModal = false">
      <div class="modal-container">
        <div class="modal-header">
          <h2>Chọn Sản Phẩm</h2>
          <button type="button" class="btn-close-modal" @click="showProductModal = false">
            <FontAwesomeIcon :icon="['fas', 'times']" />
          </button>
        </div>
        
        <div class="modal-body">
          <div class="search-section">
            <input 
              type="text" 
              v-model="productSearchText" 
              placeholder="Tìm kiếm sản phẩm..."
              class="search-input"
              @input="searchProducts"
            />
          </div>
          
          <div v-if="loadingProducts" class="loading-products">
            <div class="spinner"></div>
            <p>Đang tải sản phẩm...</p>
          </div>
          
          <div v-else class="products-list">
            <div v-for="product in filteredProductList" :key="product.id" class="product-item">
              <label class="product-checkbox-label">
                <input 
                  type="checkbox" 
                  :value="product.chiTietSanPhamId"
                  :checked="isProductSelected(product.chiTietSanPhamId)"
                  @change="toggleProduct(product)"
                />
                <div class="product-item-info">
                  <img v-if="product.hinhAnh" :src="getImageUrl(product.hinhAnh)" :alt="product.tenSanPham" class="product-thumb" />
                  <div v-else class="product-thumb-placeholder">
                    <FontAwesomeIcon :icon="['fas', 'image']" />
                  </div>
                  <div class="product-item-details">
                    <h4>{{ product.tenSanPham }}</h4>
                    <p class="product-meta">Mã: {{ product.maCtsp }} | {{ formatCurrency(product.gia) }}</p>
                  </div>
                </div>
              </label>
            </div>
            
            <div v-if="filteredProductList.length === 0" class="no-products">
              <FontAwesomeIcon :icon="['fas', 'box']" />
              <p>Không tìm thấy sản phẩm</p>
            </div>
          </div>
        </div>
        
        <div class="modal-footer">
          <button type="button" class="btn-cancel" @click="showProductModal = false">Đóng</button>
          <button type="button" class="btn-submit" @click="confirmProductSelection">
            Xác nhận ({{ selectedProducts.length }})
          </button>
        </div>
      </div>
    </div>

    <!-- Confirm Modal -->
    <ConfirmModal
      :show="showConfirmModal"
      :title="confirmAction === 'add' ? 'Xác nhận tạo mới' : 'Xác nhận cập nhật'"
      :message="confirmAction === 'add' 
        ? 'Bạn có chắc chắn muốn tạo đợt giảm giá mới với thông tin này?' 
        : 'Bạn có chắc chắn muốn cập nhật thông tin đợt giảm giá này?'"
      @confirm="confirmSubmit"
      @cancel="cancelSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, watch, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '@/services/api'
import Toast from '@/components/Toast.vue'
import PosHeader from '@/components/PosHeader.vue'
import ConfirmModal from '@/components/ConfirmModal.vue'
import { FontAwesomeIcon } from '@/plugins/fontawesome'

const router = useRouter()
const route = useRoute()
const toastRef = ref<InstanceType<typeof Toast> | null>(null)
const loading = ref(false)

const isEdit = ref(false)
const promotionId = ref<number | null>(null)

// Confirm modal state
const showConfirmModal = ref(false)
const confirmAction = ref<'add' | 'edit' | null>(null)

const formData = ref({
  maKhuyenMai: '',
  tenKhuyenMai: '',
  moTa: '',
  phanTramGiam: 0,
  giamToiDa: 0,
  ngayBatDau: '',
  ngayKetThuc: '',
  trangThai: true,
})

// Product selection
const showProductModal = ref(false)
const productList = ref<any[]>([])
const selectedProducts = ref<any[]>([])
const loadingProducts = ref(false)
const productSearchText = ref('')

// Product filtering
const productSearchKeyword = ref('')
const productCategoryFilter = ref('')
const productBrandFilter = ref('')
const categories = ref<any[]>([])
const brands = ref<any[]>([])

// Validation errors
const errors = ref({
  tenKhuyenMai: '',
  phanTramGiam: '',
  ngayBatDau: '',
  ngayKetThuc: ''
})

onMounted(async () => {
  // Load data first
  await Promise.all([
    loadProducts(),
    loadCategories(),
    loadBrands()
  ])
  
  const id = route.params.id
  console.log('=== DotGiamGiaFormPage onMounted ===')
  console.log('Route params:', route.params)
  console.log('ID from params:', id)
  
  if (id && id !== 'new') {
    isEdit.value = true
    const parsedId = parseInt(id as string)
    console.log('Parsed ID:', parsedId)
    
    if (!isNaN(parsedId)) {
      promotionId.value = parsedId
      await loadPromotion()
    } else {
      console.error('Invalid promotion ID:', id)
      toastRef.value?.error('Lỗi', 'ID đợt giảm giá không hợp lệ')
      goBack()
    }
  } else {
    console.log('Creating new promotion - clearing selected products')
    // Clear selected products for new promotion
    selectedProducts.value = []
    
    // Set default dates
    const today = new Date()
    const nextMonth = new Date(today)
    nextMonth.setMonth(today.getMonth() + 1)
    
    formData.value.ngayBatDau = today.toISOString().split('T')[0]
    formData.value.ngayKetThuc = nextMonth.toISOString().split('T')[0]
  }
})

async function loadPromotion() {
  if (!promotionId.value) return
  
  try {
    console.log('Loading promotion with ID:', promotionId.value)
    const { data } = await api.get(`/api/khuyen-mai/${promotionId.value}`)
    console.log('Loaded promotion data:', data)
    
    formData.value = {
      ...data,
      phanTramGiam: data.phanTramGiam || 0,
      giamToiDa: 0,
      ngayBatDau: data.ngayBatDau?.split('T')[0] || '',
      ngayKetThuc: data.ngayKetThuc?.split('T')[0] || '',
      trangThai: data.trangThai === 1
    }
    
    // Load danh sách sản phẩm đã áp dụng
    await loadAppliedProducts()
  } catch (error) {
    console.error('Lỗi khi tải đợt giảm giá:', error)
    toastRef.value?.error('Lỗi', 'Không thể tải thông tin đợt giảm giá')
    goBack()
  }
}

// Load danh sách sản phẩm đã áp dụng cho đợt giảm giá
async function loadAppliedProducts() {
  if (!promotionId.value) return
  
  try {
    console.log('🔍 Loading applied products for promotion:', promotionId.value)
    const { data } = await api.get(`/api/khuyen-mai/${promotionId.value}/san-pham`)
    console.log('📋 API response:', data)
    
    // Map sang danh sách sản phẩm đầy đủ
    const appliedProductIds = data.map((item: any) => item.idChiTietSanPham)
    console.log('🆔 Applied ChiTietSanPham IDs from API:', appliedProductIds)
    console.log('📦 Available products:', productList.value.length, 'items')
    
    selectedProducts.value = productList.value.filter(p => appliedProductIds.includes(p.chiTietSanPhamId))
    console.log('✅ Loaded selected products:', selectedProducts.value.length, 'items')
    console.log('📊 Selected products details:', selectedProducts.value)
  } catch (error) {
    console.error('❌ Lỗi khi tải danh sách sản phẩm áp dụng:', error)
    // Không hiển thị lỗi vì đây không phải thao tác quan trọng
  }
}

// Lưu danh sách sản phẩm áp dụng
async function saveAppliedProducts(khuyenMaiId: number) {
  try {
    console.log('🔍 Selected products before save:', selectedProducts.value)
    console.log('📊 Number of selected products:', selectedProducts.value.length)
    
    const idChiTietSanPhams = selectedProducts.value.map(p => p.chiTietSanPhamId)
    console.log('🆔 ChiTietSanPham IDs to save:', idChiTietSanPhams)
    
    const requestData = {
      idChiTietSanPhams: idChiTietSanPhams,
      nguoiTao: 'Admin' // TODO: Lấy từ session/auth
    }
    console.log('📤 Request data:', requestData)
    
    await api.post(`/api/khuyen-mai/${khuyenMaiId}/san-pham/batch`, requestData)
    
    console.log('✅ Đã lưu danh sách sản phẩm áp dụng')
  } catch (error) {
    console.error('❌ Lỗi khi lưu danh sách sản phẩm:', error)
    toastRef.value?.warning('Cảnh báo', 'Lưu đợt giảm giá thành công nhưng không thể lưu danh sách sản phẩm')
  }
}

// Validation functions
function clearErrors() {
  Object.keys(errors.value).forEach(key => {
    errors.value[key as keyof typeof errors.value] = ''
  })
}

function validateForm(): boolean {
  clearErrors()
  let isValid = true

  // Validate tên đợt giảm giá
  if (!formData.value.tenKhuyenMai.trim()) {
    errors.value.tenKhuyenMai = 'Tên đợt giảm giá không được để trống'
    isValid = false
  } else if (formData.value.tenKhuyenMai.trim().length < 3) {
    errors.value.tenKhuyenMai = 'Tên đợt giảm giá phải có ít nhất 3 ký tự'
    isValid = false
  } else if (formData.value.tenKhuyenMai.trim().length > 100) {
    errors.value.tenKhuyenMai = 'Tên đợt giảm giá không được vượt quá 100 ký tự'
    isValid = false
  }

  // Validate phần trăm giảm
  if (formData.value.phanTramGiam < 0) {
    errors.value.phanTramGiam = 'Phần trăm giảm không được âm'
    isValid = false
  } else if (formData.value.phanTramGiam > 50) {
    errors.value.phanTramGiam = 'Phần trăm giảm không được vượt quá 50%'
    isValid = false
  }

  // Validate ngày bắt đầu
  if (!formData.value.ngayBatDau) {
    errors.value.ngayBatDau = 'Vui lòng chọn ngày bắt đầu'
    isValid = false
  } else {
    const startDate = new Date(formData.value.ngayBatDau)
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    
    if (startDate < today && !isEdit.value) {
      errors.value.ngayBatDau = 'Ngày bắt đầu không được là ngày trong quá khứ'
      isValid = false
    }
  }

  // Validate ngày kết thúc
  if (!formData.value.ngayKetThuc) {
    errors.value.ngayKetThuc = 'Vui lòng chọn ngày kết thúc'
    isValid = false
  } else if (formData.value.ngayBatDau) {
    const startDate = new Date(formData.value.ngayBatDau)
    const endDate = new Date(formData.value.ngayKetThuc)
    
    if (endDate <= startDate) {
      errors.value.ngayKetThuc = 'Ngày kết thúc phải sau ngày bắt đầu'
      isValid = false
    }
    
    // Check if the promotion duration is reasonable (max 1 year)
    const diffTime = Math.abs(endDate.getTime() - startDate.getTime())
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
    if (diffDays > 365) {
      errors.value.ngayKetThuc = 'Thời gian hiệu lực không được vượt quá 1 năm'
      isValid = false
    }
  }

  return isValid
}

async function handleSubmit() {
  if (!validateForm()) {
    toastRef.value?.error('Lỗi', 'Vui lòng kiểm tra lại thông tin đã nhập')
    return
  }

  // Show confirm modal
  confirmAction.value = isEdit.value ? 'edit' : 'add'
  showConfirmModal.value = true
}

async function confirmSubmit() {
  loading.value = true
  
  try {
    // Convert date to LocalDateTime format (YYYY-MM-DDTHH:mm:ss)
    const ngayBatDauFormatted = formData.value.ngayBatDau ? `${formData.value.ngayBatDau}T00:00:00` : null
    const ngayKetThucFormatted = formData.value.ngayKetThuc ? `${formData.value.ngayKetThuc}T23:59:59` : null
    
    // Chuẩn bị dữ liệu theo loại giảm giá
    let submitData: any = {
      maKhuyenMai: formData.value.maKhuyenMai || null,
      tenKhuyenMai: formData.value.tenKhuyenMai.trim(),
      moTa: formData.value.moTa?.trim() || null,
      ngayBatDau: ngayBatDauFormatted,
      ngayKetThuc: ngayKetThucFormatted,
      trangThai: formData.value.trangThai ? 1 : 0
    }

    // Chỉ sử dụng phần trăm giảm
    submitData.phanTramGiam = parseFloat(formData.value.phanTramGiam.toString())
    submitData.giamToiDa = 0

    console.log('Submitting promotion data:', submitData)

    let savedPromotionId = promotionId.value

    if (isEdit.value && promotionId.value) {
      console.log('Updating promotion with ID:', promotionId.value)
      const response = await api.put(`/api/khuyen-mai/${promotionId.value}`, submitData)
      console.log('Update response:', response)
      toastRef.value?.success('Thành công', 'Cập nhật đợt giảm giá thành công!')
    } else {
      console.log('Creating new promotion')
      const response = await api.post('/api/khuyen-mai', submitData)
      console.log('Create response:', response)
      savedPromotionId = response.data.id
      toastRef.value?.success('Thành công', 'Thêm đợt giảm giá thành công!')
    }
    
    // Lưu danh sách sản phẩm áp dụng nếu có chọn
    if (selectedProducts.value.length > 0 && savedPromotionId) {
      await saveAppliedProducts(savedPromotionId)
    }
    
    setTimeout(() => {
      goBack()
    }, 1500)
  } catch (error: any) {
    console.error('Lỗi khi lưu:', error)
    console.error('Error response:', error.response)
    if (error.response?.data?.message) {
      toastRef.value?.error('Lỗi', error.response.data.message)
    } else if (error.response?.data) {
      toastRef.value?.error('Lỗi', JSON.stringify(error.response.data))
    } else {
      toastRef.value?.error('Lỗi', 'Có lỗi xảy ra khi lưu đợt giảm giá')
    }
  } finally {
    loading.value = false
    showConfirmModal.value = false
    confirmAction.value = null
  }
}

function cancelSubmit() {
  showConfirmModal.value = false
  confirmAction.value = null
}

function formatCurrency(amount: number | null | undefined): string {
  if (!amount || isNaN(amount)) {
    return '0 ₫'
  }
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  }).format(amount)
}


function updateSliderProgress(slider: HTMLInputElement) {
  const value = parseFloat(slider.value)
  const max = parseFloat(slider.max)
  const percentage = (value / max) * 100
  slider.style.setProperty('--value', `${percentage}%`)
}

// Watch để cập nhật slider progress khi giá trị thay đổi
watch([() => formData.value.phanTramGiam, () => formData.value.giamToiDa], () => {
  nextTick(() => {
    const sliders = document.querySelectorAll('.orange-slider') as NodeListOf<HTMLInputElement>
    sliders.forEach(slider => {
      updateSliderProgress(slider)
    })
  })
})

onMounted(() => {
  // Cập nhật slider progress khi component mount
  nextTick(() => {
    const sliders = document.querySelectorAll('.orange-slider') as NodeListOf<HTMLInputElement>
    sliders.forEach(slider => {
      updateSliderProgress(slider)
      slider.addEventListener('input', () => updateSliderProgress(slider))
    })
  })
})

function goBack() {
  router.push('/dot-giam-gia')
}

// Product functions
async function loadProducts() {
  loadingProducts.value = true
  try {
    const { data } = await api.get('/api/san-pham-pos')
    // Normalize product fields so template always has tenDanhMuc/tenHang/gia/danhMucId
    productList.value = (data || []).map((p: any) => ({
      ...p,
      // display name fallbacks (include snake_case)
      tenDanhMuc: p?.tenDanhMuc ?? p?.danhMuc?.tenDanhMuc ?? p?.danh_muc?.ten_danh_muc ?? p?.categoryName ?? '-',
      tenHang: p?.tenHang ?? p?.hang?.ten ?? p?.brandName ?? '-',
      // ids for filtering/logic (include snake_case)
      danhMucId: p?.danhMucId ?? p?.idDanhMuc ?? p?.danhMuc?.id ?? p?.danh_muc?.id ?? p?.id_danh_muc ?? p?.categoryId ?? null,
      hangId: p?.hangId ?? p?.idHang ?? p?.hang?.id ?? p?.brandId ?? null,
      // price for display
      gia: p?.gia ?? p?.giaBan ?? p?.price ?? 0,
    }))
    
    if (productList.value.length > 0) {
      // one-time debug of keys to help verify mapping
      // eslint-disable-next-line no-console
      console.log('🧭 First product keys:', Object.keys(productList.value[0] || {}))
      // eslint-disable-next-line no-console
      console.log('🧭 First product raw:', productList.value[0])
    }
    
    console.log('🔍 Loaded products:', productList.value.length, 'items')
    console.log('📊 Products with duplicate idSanPham:')
    const idSanPhamCounts = {}
    productList.value.forEach((p: any) => {
      if (idSanPhamCounts[p.idSanPham]) {
        idSanPhamCounts[p.idSanPham]++
      } else {
        idSanPhamCounts[p.idSanPham] = 1
      }
    })
    
    Object.entries(idSanPhamCounts).forEach(([idSanPham, count]) => {
      if (count > 1) {
        console.log(`⚠️ idSanPham ${idSanPham} appears ${count} times (different chiTietSanPhamId)`)  
        const duplicates = productList.value.filter((p: any) => p.idSanPham == idSanPham)
        duplicates.forEach((p: any) => {
          console.log(`  - chiTietSanPhamId: ${p.chiTietSanPhamId}, tenSanPham: ${p.tenSanPham}, gia: ${p.gia}, danhMuc: ${p.tenDanhMuc}, danhMucId: ${p.danhMucId}`)
        })
      }
    })
  } catch (error) {
    console.error('Lỗi khi tải danh sách sản phẩm:', error)
    toastRef.value?.error('Lỗi', 'Không thể tải danh sách sản phẩm')
  } finally {
    loadingProducts.value = false
  }
}

async function loadCategories() {
  try {
    const { data } = await api.get('/api/danh-muc')
    categories.value = data
  } catch (error) {
    console.error('Error loading categories:', error)
  }
}

async function loadBrands() {
  try {
    const { data } = await api.get('/api/hang')
    brands.value = data
  } catch (error) {
    console.error('Error loading brands:', error)
  }
}

// Resolve category name for a product using multiple fallbacks and loaded category list
function getCategoryNameFor(product: any): string {
  const directName = product?.tenDanhMuc ?? product?.danhMuc?.tenDanhMuc ?? product?.danh_muc?.ten_danh_muc ?? product?.categoryName
  if (directName && directName !== '-') return directName
  const categoryId = product?.danhMucId ?? product?.idDanhMuc ?? product?.danhMuc?.id ?? product?.danh_muc?.id ?? product?.id_danh_muc ?? product?.categoryId
  if (!categoryId) return '-'
  const cat = categories.value?.find((c: any) => c.id === categoryId)
  return cat?.tenDanhMuc || '-'
}

const filteredProductList = computed(() => {
  if (!productSearchText.value.trim()) {
    return productList.value
  }
  const search = productSearchText.value.toLowerCase()
  return productList.value.filter(p => 
    p.tenSanPham?.toLowerCase().includes(search) ||
    p.maCtsp?.toLowerCase().includes(search)
  )
})

// Filtered products for the table
const filteredProducts = computed(() => {
  let filtered = productList.value

  // Search filter
  if (productSearchKeyword.value.trim()) {
    const search = productSearchKeyword.value.toLowerCase()
    filtered = filtered.filter(p =>
      p.tenSanPham?.toLowerCase().includes(search) ||
      p.maCtsp?.toLowerCase().includes(search)
    )
  }

  // Category filter (support multiple source fields)
  if (productCategoryFilter.value) {
    const categoryId = parseInt(productCategoryFilter.value)
    filtered = filtered.filter(p => (p.danhMucId ?? p.idDanhMuc ?? p?.danhMuc?.id) === categoryId)
  }

  // Brand filter
  if (productBrandFilter.value) {
    const brandId = parseInt(productBrandFilter.value)
    filtered = filtered.filter(p => (p.hangId ?? p.idHang ?? p?.hang?.id) === brandId)
  }

  return filtered
})

// Check if all products are selected
const allProductsSelected = computed(() => {
  return filteredProducts.value.length > 0 && 
         filteredProducts.value.every(p => isProductSelected(p.chiTietSanPhamId))
})

function searchProducts() {
  // Search is handled by computed property
}

function isProductSelected(productId: number): boolean {
  const isSelected = selectedProducts.value.some(p => p.chiTietSanPhamId === productId)
  console.log('🔍 isProductSelected check - chiTietSanPhamId:', productId, 'isSelected:', isSelected)
  console.log('🔍 Current selectedProducts chiTietSanPhamIds:', selectedProducts.value.map(p => p.chiTietSanPhamId))
  return isSelected
}

// New methods for table-based product selection
function toggleProductSelection(product: any) {
  console.log('🔄 Toggle product selection:', product)
  console.log('📋 Current selected products:', selectedProducts.value.length, 'items')
  console.log('🔍 Product details - idSanPham:', product.idSanPham, 'chiTietSanPhamId:', product.chiTietSanPhamId, 'tenSanPham:', product.tenSanPham)
  
  // Kiểm tra duplicate chiTietSanPhamId trong selectedProducts
  const duplicateCount = selectedProducts.value.filter(p => p.chiTietSanPhamId === product.chiTietSanPhamId).length
  console.log('🔍 Duplicate count for chiTietSanPhamId', product.chiTietSanPhamId, ':', duplicateCount)
  
  const index = selectedProducts.value.findIndex(p => p.chiTietSanPhamId === product.chiTietSanPhamId)
  console.log('🔍 Found index:', index)
  
  if (index > -1) {
    selectedProducts.value.splice(index, 1)
    console.log('➖ Removed product. New count:', selectedProducts.value.length)
  } else {
    selectedProducts.value.push(product)
    console.log('➕ Added product. New count:', selectedProducts.value.length)
  }
  
  console.log('📊 Updated selected products:', selectedProducts.value)
  console.log('📊 Selected products by chiTietSanPhamId:', selectedProducts.value.map(p => ({ idSanPham: p.idSanPham, chiTietSanPhamId: p.chiTietSanPhamId, tenSanPham: p.tenSanPham })))
}

function toggleAllProducts() {
  console.log('🔄 Toggle all products')
  console.log('📊 All products selected:', allProductsSelected.value)
  console.log('📋 Filtered products count:', filteredProducts.value.length)
  console.log('📋 Current selected count:', selectedProducts.value.length)
  
  if (allProductsSelected.value) {
    // Unselect all filtered products
    const filteredIds = filteredProducts.value.map(p => p.chiTietSanPhamId)
    console.log('🆔 Filtered IDs to remove:', filteredIds)
    
    selectedProducts.value = selectedProducts.value.filter(p => !filteredIds.includes(p.chiTietSanPhamId))
    console.log('➖ Unselected all. New count:', selectedProducts.value.length)
  } else {
    // Select all filtered products
    const newSelections = filteredProducts.value.filter(p => !isProductSelected(p.chiTietSanPhamId))
    console.log('➕ New selections to add:', newSelections.length, 'items')
    
    selectedProducts.value.push(...newSelections)
    console.log('✅ Selected all. New count:', selectedProducts.value.length)
  }
  
  console.log('📊 Final selected products:', selectedProducts.value)
}

function clearAllSelections() {
  selectedProducts.value = []
}

function toggleProduct(product: any) {
  const index = selectedProducts.value.findIndex(p => p.chiTietSanPhamId === product.chiTietSanPhamId)
  if (index > -1) {
    selectedProducts.value.splice(index, 1)
  } else {
    selectedProducts.value.push(product)
  }
}

function removeProduct(productId: number) {
  const index = selectedProducts.value.findIndex(p => p.chiTietSanPhamId === productId)
  if (index > -1) {
    selectedProducts.value.splice(index, 1)
  }
}

function confirmProductSelection() {
  showProductModal.value = false
  toastRef.value?.success('Thành công', `Đã chọn ${selectedProducts.value.length} sản phẩm`)
}

function getImageUrl(imagePath: string): string {
  if (!imagePath) return ''
  // Nếu đã là URL đầy đủ, return luôn
  if (imagePath.startsWith('http')) return imagePath
  // Nếu là đường dẫn tương đối, thêm base URL
  const baseUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
  return `${baseUrl}/uploads/${imagePath}`
}
</script>

<style scoped>
.form-page {
  background: #f8fafc;
  min-height: 100vh;
  padding: 0;
  padding-top: 80px;
  width: 100%;
  overflow-x: hidden;
  position: relative;
}

.form-container {
  padding: 24px;
  padding-top: 0;
  width: 100%;
  margin: 0;
}

.form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  width: 100%;
  border-bottom: 2px solid #e9ecef;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-left h1 {
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
  letter-spacing: -0.5px;
}

.btn-back {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #6c757d;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.btn-back:hover {
  background: #545b62;
}

/* FontAwesome icon styling */
.btn-back svg,
.btn-select-products svg,
.btn-remove-product svg,
.btn-close-modal svg {
  margin: 0;
}

.product-image-placeholder svg,
.product-thumb-placeholder svg,
.no-products-selected svg,
.no-products svg {
  color: #9ca3af;
  font-size: 24px;
}

.no-products-selected svg,
.no-products svg {
  font-size: 32px;
  margin-bottom: 12px;
  display: block;
}

.promotion-form {
  background: white;
  padding: 32px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  width: 100%;
}

.form-section {
  margin-bottom: 40px;
  padding-bottom: 30px;
  border-bottom: 1px solid #e9ecef;
}

.form-section:last-of-type {
  border-bottom: none;
  margin-bottom: 0;
}

.form-section h2 {
  color: #495057;
  margin-bottom: 20px;
  font-size: 18px;
  font-weight: 600;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-weight: 600;
  color: #495057;
  font-size: 14px;
}

.required {
  color: #dc3545;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 12px;
  border: 2px solid #e9ecef;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.3s ease;
  background: white;
}

/* Global input styles to ensure orange focus */
input,
select,
textarea {
  border: 2px solid #e9ecef;
  border-radius: 6px;
  transition: all 0.3s ease;
}

input:focus,
select:focus,
textarea:focus {
  border-color: #f97316 !important;
  box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.2) !important;
  outline: none !important;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
}

/* Orange border and background when input has value */
.form-group input:not(:placeholder-shown),
.form-group input[type="date"]:not([value=""]),
.form-group input[type="number"]:not([value=""]),
.form-group select:not([value=""]),
.form-group textarea:not(:placeholder-shown) {
  border-color: #f97316;
  background: #fef3c7;
}

.form-group input.error,
.form-group select.error,
.form-group textarea.error {
  border-color: #dc3545;
  box-shadow: 0 0 0 3px rgba(220, 53, 69, 0.1);
}

.error-message {
  color: #dc3545;
  font-size: 12px;
  margin-top: 4px;
  display: block;
}

.checkbox-label {
  display: flex !important;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  font-weight: normal !important;
}

.checkbox-label input[type="checkbox"] {
  width: auto;
  margin: 0;
}

.checkmark {
  font-size: 14px;
}

/* Form Groups */
.discount-type-group {
  margin-bottom: 32px;
}

/* Input with unit */
.input-with-unit {
  position: relative;
  display: flex;
  align-items: center;
}

.input-with-unit input {
  padding-right: 40px;
}

.input-unit {
  position: absolute;
  right: 12px;
  color: #6b7280;
  font-weight: 500;
  pointer-events: none;
}

.slider-section {
  margin-top: 20px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

/* Radio Button Styles */
.discount-type-selector {
  display: flex;
  gap: 16px;
  margin-top: 12px;
}

.radio-option {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 16px 20px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  transition: all 0.3s ease;
  background: white;
  flex: 1;
  position: relative;
  overflow: hidden;
}

.radio-option:hover {
  border-color: #f97316;
  background: #fef3c7;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.15);
}

.radio-option.selected {
  border-color: #f97316;
  background: linear-gradient(135deg, #fef3c7, #fde68a);
  box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.1);
  transform: translateY(-2px);
}

.radio-option input[type="radio"] {
  margin: 0;
  width: 20px;
  height: 20px;
  accent-color: #f97316;
  cursor: pointer;
}

.radio-option input[type="radio"]:checked + .radio-label {
  color: #f97316;
  font-weight: 700;
}

.radio-label {
  font-size: 15px;
  font-weight: 600;
  color: #374151;
  transition: all 0.3s ease;
  cursor: pointer;
}

/* Help Text */
.help-text {
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px;
  display: block;
}

/* Slider Styles */
.slider-group {
  margin-bottom: 20px;
}

.slider-group:last-child {
  margin-bottom: 0;
}

.slider-container {
  position: relative;
  margin-top: 8px;
}

.slider-value {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  text-align: center;
  margin-bottom: 12px;
  padding: 8px 16px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  display: inline-block;
  min-width: 100px;
}

.slider {
  -webkit-appearance: none;
  appearance: none;
  width: 100%;
  height: 8px;
  border-radius: 4px;
  background: #f97316;
  outline: none;
  transition: all 0.3s ease;
  cursor: pointer;
  box-shadow: inset 0 1px 3px rgba(249, 115, 22, 0.3);
}

.orange-slider {
  background: #f97316 !important;
  box-shadow: inset 0 1px 3px rgba(249, 115, 22, 0.3);
}

.slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, #f97316, #ea580c);
  cursor: pointer;
  box-shadow: 0 3px 6px rgba(249, 115, 22, 0.4), 0 0 0 2px white;
  transition: all 0.3s ease;
  border: 3px solid white;
}

.slider::-webkit-slider-thumb:hover {
  background: linear-gradient(135deg, #ea580c, #dc2626);
  transform: scale(1.15);
  box-shadow: 0 5px 12px rgba(249, 115, 22, 0.5), 0 0 0 2px white;
}

.slider::-moz-range-thumb {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, #f97316, #ea580c);
  cursor: pointer;
  border: 3px solid white;
  box-shadow: 0 3px 6px rgba(249, 115, 22, 0.4), 0 0 0 2px white;
  transition: all 0.3s ease;
}

.slider::-moz-range-thumb:hover {
  background: linear-gradient(135deg, #ea580c, #dc2626);
  transform: scale(1.15);
  box-shadow: 0 5px 12px rgba(249, 115, 22, 0.5), 0 0 0 2px white;
}

.slider::-webkit-slider-track {
  background: #f97316 !important;
  height: 8px;
  border-radius: 4px;
  box-shadow: inset 0 1px 3px rgba(249, 115, 22, 0.3);
}

.slider::-moz-range-track {
  background: #f97316 !important;
  height: 8px;
  border-radius: 4px;
  border: none;
  box-shadow: inset 0 1px 3px rgba(249, 115, 22, 0.3);
}

.slider-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 12px;
  color: #64748b;
  font-weight: 500;
}

.slider.error {
  border: 2px solid #ef4444;
  background: #fef2f2;
}

.slider.error::-webkit-slider-thumb {
  background: #ef4444;
}

.slider.error::-moz-range-thumb {
  background: #ef4444;
}

/* Force orange background for all slider states */
input[type="range"].slider {
  background: #f97316 !important;
}

input[type="range"].slider::-webkit-slider-track {
  background: #f97316 !important;
}

input[type="range"].slider::-moz-range-track {
  background: #f97316 !important;
}

/* Override any default browser styles */
input[type="range"] {
  background: #f97316 !important;
}

input[type="range"]::-webkit-slider-track {
  background: #f97316 !important;
}

input[type="range"]::-moz-range-track {
  background: #f97316 !important;
}

/* Force orange focus for all input types */
input:focus,
select:focus,
textarea:focus {
  border-color: #f97316 !important;
  box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.2) !important;
  outline: none !important;
}

/* Override browser default focus styles */
input[type="text"]:focus,
input[type="email"]:focus,
input[type="password"]:focus,
input[type="number"]:focus,
input[type="date"]:focus,
input[type="datetime-local"]:focus,
input[type="time"]:focus,
input[type="search"]:focus,
input[type="tel"]:focus,
input[type="url"]:focus,
select:focus,
textarea:focus {
  border-color: #f97316 !important;
  box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.2) !important;
  outline: none !important;
  border-width: 2px !important;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 40px;
  padding-top: 30px;
  border-top: 2px solid #e9ecef;
}

.btn-cancel {
  padding: 12px 24px;
  background: #6c757d;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: background-color 0.3s;
}

.btn-cancel:hover {
  background: #545b62;
}

.btn-submit {
  padding: 12px 24px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: background-color 0.3s;
}

.btn-submit:hover:not(:disabled) {
  background: #0056b3;
}

.btn-submit:disabled {
  background: #6c757d;
  cursor: not-allowed;
}

/* Product Selection Section */
.product-selection-section {
  margin-top: 20px;
}

.product-selection-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 20px;
}

.product-selection-header .help-text {
  flex: 1;
  margin: 0;
  color: #64748b;
}

.btn-select-products {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: #f97316;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.btn-select-products:hover {
  background: #ea580c;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.3);
}


/* Selected Products List */
.selected-products-list h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 16px;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.product-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.product-card:hover {
  border-color: #f97316;
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.1);
}

.product-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.product-image {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
}

.product-image-placeholder {
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f1f5f9;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
}

.product-details h4 {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 4px 0;
}

.product-code {
  font-size: 12px;
  color: #64748b;
  margin: 0 0 2px 0;
}

.product-price {
  font-size: 13px;
  font-weight: 600;
  color: #f97316;
  margin: 0;
}

.btn-remove-product {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fee2e2;
  color: #dc2626;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-remove-product:hover {
  background: #fecaca;
  transform: scale(1.1);
}

.no-products-selected {
  text-align: center;
  padding: 40px 20px;
  background: #f8fafc;
  border: 2px dashed #cbd5e1;
  border-radius: 8px;
  color: #64748b;
}

.no-products-selected i {
  display: block;
  margin-bottom: 12px;
}

.no-products-selected p {
  margin: 0;
  font-size: 14px;
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
  z-index: 9999;
  padding: 20px;
}

.modal-container {
  background: white;
  border-radius: 12px;
  width: 100%;
  max-width: 800px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
  position: relative;
}

.modal-header h2 {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.btn-close-modal {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f1f5f9;
  color: #64748b;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: absolute;
  top: 16px;
  right: 16px;
}

.btn-close-modal:hover {
  background: #e2e8f0;
  color: #1e293b;
}

.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.search-section {
  margin-bottom: 20px;
}

.search-section .search-input {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.search-section .search-input:focus {
  outline: none;
  border-color: #f97316;
  box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.1);
}

.loading-products {
  text-align: center;
  padding: 40px 20px;
  color: #64748b;
}

.spinner {
  width: 40px;
  height: 40px;
  margin: 0 auto 16px;
  border: 4px solid #f1f5f9;
  border-top: 4px solid #f97316;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.products-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.product-item {
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.product-item:hover {
  border-color: #f97316;
  background: #fef3c7;
}

.product-checkbox-label {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  cursor: pointer;
}

.product-checkbox-label input[type="checkbox"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: #f97316;
}

.product-item-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.product-thumb {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
}

.product-thumb-placeholder {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f1f5f9;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
}

.product-item-details h4 {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 4px 0;
}

.product-meta {
  font-size: 13px;
  color: #64748b;
  margin: 0;
}

.no-products {
  text-align: center;
  padding: 40px 20px;
  color: #64748b;
}

.no-products i {
  display: block;
  margin-bottom: 12px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid #e2e8f0;
}

@media (max-width: 768px) {
  .form-page {
    padding-top: 60px;
  }
  
  .form-container {
    padding: 16px;
  }
  
  .form-row {
    grid-template-columns: 1fr;
    gap: 15px;
  }
  
  .header-left {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .promotion-form {
    padding: 20px;
  }
  
  .form-actions {
    flex-direction: column;
  }

  .product-selection-header {
    flex-direction: column;
    align-items: stretch;
  }

  .products-grid {
    grid-template-columns: 1fr;
  }

  .modal-container {
    max-height: 95vh;
  }
}

/* Product filters */
.product-filters {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.product-filters .filter-group {
  flex: 1;
  min-width: 200px;
}

.product-filters label {
  display: block;
  margin-bottom: 4px;
  font-weight: 500;
  color: #374151;
  font-size: 14px;
}

.filter-input,
.filter-select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.2s;
}

.filter-input:focus,
.filter-select:focus {
  outline: none;
  border-color: #f97316;
  box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.1);
}

/* Products table */
.products-table-container {
  margin-top: 20px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
  background: white;
}

.products-table {
  width: 100%;
  border-collapse: collapse;
}

.products-table th {
  background: #f8fafc;
  padding: 12px 16px;
  text-align: left;
  font-weight: 600;
  color: #374151;
  border-bottom: 1px solid #e2e8f0;
  font-size: 14px;
}

.products-table td {
  padding: 12px 16px;
  border-bottom: 1px solid #f1f5f9;
  vertical-align: middle;
}

.product-row:hover {
  background: #f8fafc;
}

.product-image-cell {
  width: 60px;
}

.product-image {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 6px;
}

.product-image-placeholder {
  width: 40px;
  height: 40px;
  background: #f1f5f9;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9ca3af;
}

.product-name {
  font-weight: 500;
  color: #1f2937;
}

.product-category,
.product-brand {
  color: #6b7280;
  font-size: 14px;
}

.product-price {
  font-weight: 600;
  color: #059669;
}

.product-status {
  text-align: center;
}

.status-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.active {
  background: #dcfce7;
  color: #166534;
}

.status-badge.inactive {
  background: #fef2f2;
  color: #dc2626;
}

.select-all-checkbox,
.product-checkbox {
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.no-products {
  text-align: center;
  padding: 40px;
  color: #6b7280;
}

.no-products svg {
  font-size: 48px;
  margin-bottom: 16px;
  color: #d1d5db;
}

.selected-products-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding: 16px;
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  border-radius: 8px;
}

.selected-products-summary h3 {
  margin: 0;
  color: #0369a1;
  font-size: 16px;
}

.btn-clear-selection {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #ef4444;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-clear-selection:hover {
  background: #dc2626;
  transform: translateY(-1px);
}
</style>
