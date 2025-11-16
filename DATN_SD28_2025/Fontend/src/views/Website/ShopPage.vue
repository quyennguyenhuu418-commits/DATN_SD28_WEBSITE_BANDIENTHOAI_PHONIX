<template>
  <div class="shop-page">
    <!-- Header Layout -->
    <HeaderLayout />


    <!-- Main Content -->
    <main class="shop-main" style="margin-top: 100px">
      <div class="container">
        <div class="shop-layout">
          <!-- Sidebar Filters -->
          <aside class="shop-sidebar">
            <div class="filter-section">
              <h3 class="filter-title">
                <i class="bi bi-funnel-fill"></i>
                Bộ lọc
              </h3>

              <!-- Brand Filter -->
              <div class="filter-group">
                <h4 class="filter-group-title">Thương hiệu</h4>
                <div class="filter-options">
                  <label
                    v-for="brand in brands"
                    :key="brand.id"
                    class="filter-option"
                  >
                    <input
                      type="checkbox"
                      :value="brand.id"
                      v-model="selectedBrands"
                      @change="applyFilters"
                    />
                    <span class="option-label">{{ brand.ten }}</span>
                  </label>
                </div>
              </div>

              <!-- Category Filter -->
              <div class="filter-group">
                <h4 class="filter-group-title">Danh mục</h4>
                <div class="filter-options">
                  <label
                    v-for="category in categories"
                    :key="category.id"
                    class="filter-option"
                  >
                    <input
                      type="checkbox"
                      :value="category.id"
                      v-model="selectedCategories"
                      @change="applyFilters"
                    />
                    <span class="option-label">{{ category.tenDanhMuc }}</span>
                  </label>
                </div>
              </div>

              <!-- Price Filter -->
              <div class="filter-group">
                <h4 class="filter-group-title">Mức giá</h4>
                <div class="filter-options">
                  <label
                    v-for="priceRange in priceRanges"
                    :key="priceRange.id"
                    class="filter-option"
                  >
                    <input
                      type="radio"
                      name="priceRange"
                      :value="priceRange.id"
                      v-model="selectedPriceRange"
                      @change="applyFilters"
                    />
                    <span class="option-label">{{ priceRange.label }}</span>
                  </label>
                </div>
              </div>

              <!-- Features Filter -->
              <div class="filter-group">
                <h4 class="filter-group-title">Tính năng nổi bật</h4>
                <div class="filter-options">
                  <label class="filter-option">
                    <input type="checkbox" v-model="filters.gaming" @change="applyFilters" />
                    <span class="option-label">🎮 Chơi game</span>
                  </label>
                  <label class="filter-option">
                    <input type="checkbox" v-model="filters.battery" @change="applyFilters" />
                    <span class="option-label">🔋 Pin trâu</span>
                  </label>
                  <label class="filter-option">
                    <input type="checkbox" v-model="filters.camera" @change="applyFilters" />
                    <span class="option-label">📸 Camera đẹp</span>
                  </label>
                  <label class="filter-option">
                    <input type="checkbox" v-model="filters.fiveG" @change="applyFilters" />
                    <span class="option-label">🚀 5G</span>
                  </label>
                </div>
              </div>

              <!-- Reset Button -->
              <button class="reset-filters-btn" @click="resetFilters">
                <i class="bi bi-arrow-clockwise"></i>
                Đặt lại bộ lọc
              </button>
            </div>
          </aside>

          <!-- Products Area -->
          <div class="shop-content">
            <!-- Toolbar -->
            <div class="products-toolbar">
              <div class="toolbar-left">
                <p class="results-count">
                  Hiển thị <strong>{{ displayedProducts.length }}</strong> trong
                  <strong>{{ filteredProducts.length }}</strong> sản phẩm
                </p>
              </div>
              <div class="toolbar-right">
                <label class="sort-label">Sắp xếp:</label>
                <select v-model="sortBy" @change="applySorting" class="sort-select">
                  <option value="default">Mặc định</option>
                  <option value="price-asc">Giá: Thấp đến cao</option>
                  <option value="price-desc">Giá: Cao đến thấp</option>
                  <option value="sales">Bán chạy nhất</option>
                  <option value="rating">Đánh giá cao</option>
                  <option value="newest">Mới nhất</option>
                </select>
              </div>
            </div>

            <!-- Loading State -->
            <div v-if="loading" class="loading-state">
              <div class="spinner"></div>
              <p>Đang tải sản phẩm...</p>
            </div>

            <!-- Empty State -->
            <div v-else-if="displayedProducts.length === 0" class="empty-state">
              <i class="bi bi-inbox"></i>
              <h3>Không tìm thấy sản phẩm</h3>
              <p>Vui lòng thử điều chỉnh bộ lọc hoặc tìm kiếm khác</p>
              <button @click="resetFilters" class="btn-reset">Đặt lại bộ lọc</button>
            </div>

            <!-- Products Grid -->
            <div v-else class="products-grid">
              <div
                v-for="product in displayedProducts"
                :key="product.chiTietSanPhamId || product.id"
                class="product-card"
                @click="viewProductDetail(product)"
              >
                <!-- Product Image -->
                <div class="product-image-wrapper">
                  <img
                    :src="getProductImage(product.hinhAnh)"
                    :alt="product.tenSanPham"
                    class="product-image"
                    @error="handleImageError"
                  />

                  <!-- Badges -->
                  <div class="product-badges">
                    <span v-if="product.isHot" class="badge badge-hot">🔥 HOT</span>
                    <span v-if="product.isNew" class="badge badge-new">✨ MỚI</span>
                    <span v-if="product.discount > 0" class="badge badge-discount">
                      <i class="bi bi-fire"></i>
                      -{{ product.discount }}%
                    </span>
                  </div>

                  <!-- Wishlist Button -->
                  <button
                    class="wishlist-btn"
                    :class="{ active: product.isWishlisted }"
                    @click.stop="toggleWishlist(product)"
                  >
                    <i class="bi" :class="product.isWishlisted ? 'bi-heart-fill' : 'bi-heart'"></i>
                  </button>
                </div>

                <!-- Product Info -->
                <div class="product-info">
                  <h3 class="product-name">{{ product.tenSanPham }}</h3>

                  <!-- Product Specs -->
                  <div class="product-specs">
                    <span v-if="product.tenRam" class="spec-item">{{ product.tenRam }}</span>
                    <span v-if="product.tenRom" class="spec-item">{{ product.tenRom }}</span>
                    <span v-if="product.tenMauSac" class="spec-item">{{ product.tenMauSac }}</span>
                  </div>

                  <!-- Rating -->
                  <div class="product-rating">
                    <div class="stars">
                      <i
                        v-for="star in 5"
                        :key="star"
                        class="bi"
                        :class="star <= product.rating ? 'bi-star-fill' : 'bi-star'"
                      ></i>
                    </div>
                    <span class="rating-count">({{ product.reviewCount }})</span>
                  </div>

                  <!-- Price -->
                  <div class="product-price">
                    <span class="current-price">{{ formatPrice(product.giaSauGiam || product.gia) }}</span>
                    <span v-if="product.oldPrice" class="old-price">{{ formatPrice(product.oldPrice) }}</span>
                  </div>

                  <!-- Promotion Text -->
                  <div class="promotion-tag">
                    <i class="bi bi-credit-card"></i>
                    Trả góp 0%
                  </div>

                  <!-- Stock Info -->
                  <div class="stock-info">
                    <i class="bi bi-check-circle-fill"></i>
                    <span v-if="product.soLuongTon > 0">Còn {{ product.soLuongTon }} sản phẩm</span>
                    <span v-else class="out-of-stock">Hết hàng</span>
                  </div>

                  <!-- Actions -->
                  <div class="product-actions">
                    <button
                      class="btn-buy-now"
                      :disabled="product.soLuongTon === 0"
                      @click.stop="openConfirm(product, 'buy')"
                    >
                      <i class="bi bi-lightning-fill"></i>
                      Mua ngay
                    </button>
                    <button
                      class="btn-add-cart"
                      :disabled="product.soLuongTon === 0"
                      @click.stop="openConfirm(product, 'add')"
                    >
                      <i class="bi bi-cart-plus"></i>
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Pagination -->
            <div v-if="totalPages > 1" class="pagination">
              <button
                class="page-btn"
                :disabled="currentPage === 1"
                @click="goToPage(currentPage - 1)"
              >
                <i class="bi bi-chevron-left"></i>
              </button>

              <button
                v-for="page in visiblePages"
                :key="page"
                class="page-btn"
                :class="{ active: page === currentPage }"
                @click="goToPage(page)"
              >
                {{ page }}
              </button>

              <button
                class="page-btn"
                :disabled="currentPage === totalPages"
                @click="goToPage(currentPage + 1)"
              >
                <i class="bi bi-chevron-right"></i>
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- Footer Layout -->
    <FooterLayout />

    <!-- Toast Notification -->
    <transition name="toast">
      <div v-if="toast.show" class="toast-notification" :class="toast.type">
        <i class="bi" :class="toast.icon"></i>
        <span>{{ toast.message }}</span>
      </div>
    </transition>

    <!-- Confirm Modal -->
    <div v-if="confirmModal.show" class="confirm-overlay" @click.self="closeConfirm">
      <div class="confirm-card">
        <div class="confirm-header">
          <h3>Xác nhận {{ confirmModal.action === 'buy' ? 'mua ngay' : 'thêm vào giỏ' }}</h3>
          <button class="confirm-close" type="button" @click="closeConfirm">×</button>
        </div>
        <div class="confirm-content">
          <div class="confirm-row"><span class="label">Sản phẩm</span><span class="value">{{ confirmModal.product?.tenSanPham }}</span></div>
          <div class="confirm-row"><span class="label">Giá</span><span class="value">{{ formatPrice(confirmModal.product?.gia || 0) }}</span></div>
        </div>
        <div class="confirm-actions">
          <button type="button" class="btn-cancel" @click="closeConfirm" :disabled="confirmModal.loading">Hủy</button>
          <button type="button" class="btn-confirm" @click="confirmProceed" :disabled="confirmModal.loading">
            <i v-if="confirmModal.loading" class="bi bi-hourglass-split"></i>
            {{ confirmModal.action === 'buy' ? 'Xác nhận mua ngay' : 'Xác nhận thêm giỏ' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useCartStore } from '@/stores/cartStore'
import HeaderLayout from './HeaderLayout.vue'
import FooterLayout from './FooterLayout.vue'
import axios from 'axios'

const router = useRouter()
const route = useRoute()
const cartStore = useCartStore()

// API Base URL
const API_BASE_URL = 'http://localhost:8080'

// State
const loading = ref(false)
const products = ref([])
const brands = ref([])
const categories = ref([])
const showPromoBanner = ref(true)

// Filters
const selectedBrands = ref([])
const selectedCategories = ref([])
const selectedPriceRange = ref(null)
const filters = ref({
  gaming: false,
  battery: false,
  camera: false,
  fiveG: false
})

// Sorting & Pagination
const sortBy = ref('default')
const currentPage = ref(1)
const itemsPerPage = 12

// Price Ranges
const priceRanges = [
  { id: null, label: 'Tất cả' },
  { id: 1, label: 'Dưới 5 triệu', min: 0, max: 5000000 },
  { id: 2, label: '5 - 10 triệu', min: 5000000, max: 10000000 },
  { id: 3, label: '10 - 15 triệu', min: 10000000, max: 15000000 },
  { id: 4, label: '15 - 20 triệu', min: 15000000, max: 20000000 },
  { id: 5, label: 'Trên 20 triệu', min: 20000000, max: Infinity }
]

// Countdown
const countdown = ref({
  days: 0,
  hours: 0,
  minutes: 0,
  seconds: 0
})

// Toast
const toast = ref({
  show: false,
  type: 'success',
  message: '',
  icon: 'bi-check-circle-fill'
})

// Confirm modal
const confirmModal = ref({ show: false, loading: false, action: /** @type{'buy'|'add'|null} */(null), product: null })

const openConfirm = (product, action) => {
  confirmModal.value = { show: true, loading: false, action, product }
}

const closeConfirm = () => {
  if (confirmModal.value.loading) return
  confirmModal.value.show = false
}

const confirmProceed = async () => {
  if (!confirmModal.value.product || !confirmModal.value.action) return
  confirmModal.value.loading = true
  try {
    if (confirmModal.value.action === 'add') {
      addToCart(confirmModal.value.product)
    } else {
      await buyNow(confirmModal.value.product)
    }
    confirmModal.value.show = false
  } finally {
    confirmModal.value.loading = false
  }
}

// Computed
const filteredProducts = computed(() => {
  let filtered = [...products.value]

  // Search is handled by HeaderLayout

  // Filter by brands
  if (selectedBrands.value.length > 0) {
    filtered = filtered.filter(p => selectedBrands.value.includes(p.hangId))
  }

  // Filter by categories
  if (selectedCategories.value.length > 0) {
    filtered = filtered.filter(p => selectedCategories.value.includes(p.danhMucId))
  }

  // Filter by price range
  if (selectedPriceRange.value) {
    const range = priceRanges.find(r => r.id === selectedPriceRange.value)
    if (range) {
      filtered = filtered.filter(p => p.gia >= range.min && p.gia < range.max)
    }
  }

  // Filter by features (mock logic)
  if (filters.value.gaming) {
    filtered = filtered.filter(p => p.tenRam && parseInt(p.tenRam) >= 8)
  }
  if (filters.value.battery) {
    filtered = filtered.filter(p => p.isHot) // Mock: hot products have good battery
  }
  if (filters.value.camera) {
    filtered = filtered.filter(p => p.rating >= 4.5)
  }
  if (filters.value.fiveG) {
    filtered = filtered.filter(p => p.tenSanPham.toLowerCase().includes('5g'))
  }

  return filtered
})

const sortedProducts = computed(() => {
  const sorted = [...filteredProducts.value]

  switch (sortBy.value) {
    case 'price-asc':
      return sorted.sort((a, b) => a.gia - b.gia)
    case 'price-desc':
      return sorted.sort((a, b) => b.gia - a.gia)
    case 'sales':
      return sorted.sort((a, b) => (b.soLuongDaBan || 0) - (a.soLuongDaBan || 0))
    case 'rating':
      return sorted.sort((a, b) => b.rating - a.rating)
    case 'newest':
      return sorted.sort((a, b) => b.isNew - a.isNew)
    default:
      return sorted
  }
})

const totalPages = computed(() => {
  return Math.ceil(sortedProducts.value.length / itemsPerPage)
})

const displayedProducts = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage
  const end = start + itemsPerPage
  return sortedProducts.value.slice(start, end)
})

const visiblePages = computed(() => {
  const pages = []
  const maxVisible = 5
  let start = Math.max(1, currentPage.value - Math.floor(maxVisible / 2))
  let end = Math.min(totalPages.value, start + maxVisible - 1)

  if (end - start < maxVisible - 1) {
    start = Math.max(1, end - maxVisible + 1)
  }

  for (let i = start; i <= end; i++) {
    pages.push(i)
  }

  return pages
})

// Methods
const fetchProducts = async () => {
  loading.value = true
  try {
    const response = await axios.get(`${API_BASE_URL}/api/san-pham-pos`)

    if (response.data && Array.isArray(response.data)) {
      products.value = response.data.map(p => enhanceProduct(p))
    }
  } catch (error) {
    console.error('Error fetching products:', error)
    showToast('error', 'Không thể tải sản phẩm. Vui lòng thử lại!', 'bi-exclamation-circle-fill')
  } finally {
    loading.value = false
  }
}

const enhanceProduct = (product) => {
  // Calculate rating based on sales
  const salesCount = product.soLuongDaBan || 0
  let rating = 3.5
  if (salesCount > 100) rating = 5.0
  else if (salesCount > 50) rating = 4.8
  else if (salesCount > 20) rating = 4.5
  else if (salesCount > 10) rating = 4.0

  // Sử dụng dữ liệu giảm giá từ backend (không dùng mock)
  // Chỉ hiển thị giảm giá khi có đợt giảm giá thực từ backend
  let discount = 0
  let oldPrice = null
  
  // Kiểm tra nếu có giảm giá từ backend
  if (product.giamPhanTram && product.giamPhanTram > 0) {
    discount = Math.round(product.giamPhanTram)
    // Nếu có giá gốc và giá sau giảm, dùng giá gốc làm oldPrice
    if (product.giaGoc && product.giaSauGiam && product.giaGoc > product.giaSauGiam) {
      oldPrice = product.giaGoc
    }
  }

  // Determine if hot or new (không dùng discount mock)
  const isHot = salesCount > 30
  const isNew = salesCount < 5 // Sản phẩm mới nếu bán ít hơn 5

  return {
    ...product,
    rating: Math.round(rating * 10) / 10,
    reviewCount: Math.max(5, Math.floor(salesCount * 0.8)),
    oldPrice, // Chỉ có giá trị khi có giảm giá từ backend
    discount, // Chỉ có giá trị khi có giảm giá từ backend
    isHot,
    isNew,
    isWishlisted: cartStore.isInWishlist(product.id)
  }
}

const fetchBrands = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/api/hang`)
    if (response.data && Array.isArray(response.data)) {
      brands.value = response.data.filter(b => b.trangThai === 1)
    }
  } catch (error) {
    console.error('Error fetching brands:', error)
  }
}

const fetchCategories = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/api/danh-muc`)
    if (response.data && Array.isArray(response.data)) {
      categories.value = response.data.filter(c => c.trangThai === 1)
    }
  } catch (error) {
    console.error('Error fetching categories:', error)
  }
}



const applyFilters = () => {
  currentPage.value = 1
}

const applySorting = () => {
  currentPage.value = 1
}

const resetFilters = () => {
  selectedBrands.value = []
  selectedCategories.value = []
  selectedPriceRange.value = null
  filters.value = {
    gaming: false,
    battery: false,
    camera: false,
    fiveG: false
  }
  currentPage.value = 1
}

const goToPage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

const addToCart = (product) => {
  try {
    cartStore.addItem(product)
    showToast('success', `Đã thêm "${product.tenSanPham}" vào giỏ hàng!`, 'bi-cart-check-fill')
  } catch (error) {
    showToast('error', error.message, 'bi-exclamation-circle-fill')
  }
}

const buyNow = async (product) => {
  try {
    // Thêm sản phẩm vào giỏ hàng trước
    cartStore.addItem(product)
    
    // Delay ngắn để cart store cập nhật
    await new Promise(resolve => setTimeout(resolve, 100))
    
    // Chuyển đến trang đặt hàng với thông tin sản phẩm
    router.push({
      path: '/dat-hang',
      query: {
        productId: product.id,
        variantId: product.chiTietSanPhamId || product.id,
        quantity: 1,
        buyNow: 'true'
      }
    })
  } catch (error) {
    showToast('error', error.message, 'bi-exclamation-circle-fill')
  }
}

const viewProductDetail = (product) => {
  router.push({ name: 'product-detail', params: { id: product.id } })
}

const toggleWishlist = (product) => {
  const isAdded = cartStore.toggleWishlist(product.id)
  product.isWishlisted = isAdded
  const message = isAdded
    ? `Đã thêm "${product.tenSanPham}" vào yêu thích!`
    : `Đã xóa "${product.tenSanPham}" khỏi yêu thích!`
  showToast('info', message, 'bi-heart-fill')
}

const formatPrice = (price) => {
  if (!price) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

const getProductImage = (imagePath) => {
  if (!imagePath) return '/placeholder.png'
  if (imagePath.startsWith('http')) return imagePath
  return `${API_BASE_URL}${imagePath.startsWith('/') ? '' : '/'}${imagePath}`
}

const handleImageError = (event) => {
  event.target.src = 'https://via.placeholder.com/300x300?text=No+Image'
}

const showToast = (type, message, icon) => {
  toast.value = {
    show: true,
    type,
    message,
    icon
  }
  setTimeout(() => {
    toast.value.show = false
  }, 3000)
}

// Countdown Timer
let countdownInterval
const updateCountdown = () => {
  const endDate = new Date()
  endDate.setDate(endDate.getDate() + 3) // 3 days from now
  endDate.setHours(23, 59, 59, 999)

  const now = new Date()
  const diff = endDate - now

  if (diff > 0) {
    countdown.value = {
      days: Math.floor(diff / (1000 * 60 * 60 * 24)),
      hours: Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)),
      minutes: Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60)),
      seconds: Math.floor((diff % (1000 * 60)) / 1000)
    }
  }
}

// Lifecycle
// Đọc query parameters từ URL và áp dụng filter
const applyQueryFilters = () => {
  // Xử lý brand filter từ query
  if (route.query.brand) {
    const brandId = parseInt(route.query.brand)
    if (!isNaN(brandId) && !selectedBrands.value.includes(brandId)) {
      selectedBrands.value.push(brandId)
      console.log('✅ Applied brand filter from URL:', brandId)
    }
  }

  // Xử lý category filter từ query
  if (route.query.category) {
    const categoryId = parseInt(route.query.category)
    if (!isNaN(categoryId) && !selectedCategories.value.includes(categoryId)) {
      selectedCategories.value.push(categoryId)
      console.log('✅ Applied category filter from URL:', categoryId)
    }
  }
}

// Watch route changes để cập nhật filter khi URL thay đổi
watch(() => route.query, (newQuery) => {
  // Reset filters
  selectedBrands.value = []
  selectedCategories.value = []
  
  // Apply new filters from query
  if (newQuery.brand) {
    const brandId = parseInt(newQuery.brand)
    if (!isNaN(brandId)) {
      selectedBrands.value.push(brandId)
    }
  }
  
  if (newQuery.category) {
    const categoryId = parseInt(newQuery.category)
    if (!isNaN(categoryId)) {
      selectedCategories.value.push(categoryId)
    }
  }
  
  // Reset to first page when filters change
  currentPage.value = 1
}, { immediate: false })

onMounted(async () => {
  // Scroll to top khi vào trang
  window.scrollTo({ top: 0, behavior: 'smooth' })
  
  // Load data first
  await fetchProducts()
  await fetchBrands()
  await fetchCategories()
  
  // Sau khi load xong brands và categories, mới áp dụng filters từ query
  applyQueryFilters()
  
  updateCountdown()
  countdownInterval = setInterval(updateCountdown, 1000)
})

onUnmounted(() => {
  if (countdownInterval) {
    clearInterval(countdownInterval)
  }
})
</script>

<style scoped>
/* Phoenix Theme Colors */
:root {
  --phoenix-primary: #FF6B35;
  --phoenix-secondary: #F7931E;
  --phoenix-accent: #DC143C;
  --phoenix-gold: #FFD700;
  --phoenix-dark: #2C1810;
  --phoenix-light: #FFF5E1;
}

/* Reset & Base */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.shop-page {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  color: #333;
  background: #f5f5f5;
  min-height: 100vh;
}




/* Promo Banner */
.promo-banner {
  background: linear-gradient(90deg, #F7931E 0%, #FFD700 100%);
  padding: 1.5rem 2rem;
  position: relative;
  overflow: hidden;
}

.promo-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2rem;
  flex-wrap: wrap;
}

.promo-title {
  color: #2C1810;
  font-size: 1.5rem;
  font-weight: bold;
  text-shadow: 1px 1px 2px rgba(255, 255, 255, 0.5);
}

.countdown {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.countdown-label {
  color: #2C1810;
  font-weight: 600;
}

.countdown-timer {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.time-unit {
  display: flex;
  flex-direction: column;
  align-items: center;
  background: white;
  padding: 0.5rem 0.75rem;
  border-radius: 8px;
  min-width: 50px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.time-value {
  font-size: 1.5rem;
  font-weight: bold;
  color: #FF5500;
}

.time-label {
  font-size: 0.7rem;
  color: #666;
  text-transform: uppercase;
}

.time-separator {
  font-size: 1.5rem;
  font-weight: bold;
  color: #2C1810;
}

.close-banner {
  position: absolute;
  top: 0.5rem;
  right: 1rem;
  background: none;
  border: none;
  color: #2C1810;
  font-size: 1.25rem;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 50%;
  transition: background 0.2s;
}

.close-banner:hover {
  background: rgba(0, 0, 0, 0.1);
}

/* Main Content */
.shop-main {
  padding: 2rem 0;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 2rem;
}

.shop-layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 2rem;
}

/* Sidebar */
.shop-sidebar {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  height: fit-content;
  position: static;
}

.filter-title {
  font-size: 1.25rem;
  color: #FF5500;
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.filter-group {
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid #eee;
}

.filter-group:last-of-type {
  border-bottom: none;
}

.filter-group-title {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 1rem;
  color: #333;
}

.filter-options {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.filter-option {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  font-size: 0.9rem;
}

.filter-option input[type="checkbox"],
.filter-option input[type="radio"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: #FF5500;
}

.reset-filters-btn {
  width: 100%;
  padding: 0.75rem;
  background: #FF5500;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  transition: all 0.3s;
}

.reset-filters-btn:hover {
  background: #DC143C;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(220, 20, 60, 0.3);
}

/* Products Content */
.shop-content {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.products-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #f0f0f0;
}

.results-count {
  color: #666;
  font-size: 0.95rem;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.sort-label {
  font-weight: 600;
  color: #333;
}

.sort-select {
  padding: 0.5rem 2rem 0.5rem 1rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 0.9rem;
  cursor: pointer;
  background: white;
  outline: none;
  transition: border-color 0.2s;
}

.sort-select:focus {
  border-color: #FF5500;
}

/* Loading & Empty States */
.loading-state,
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  color: #666;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #FF5500;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-state i {
  font-size: 4rem;
  color: #ccc;
  margin-bottom: 1rem;
}

.empty-state h3 {
  margin-bottom: 0.5rem;
}

.btn-reset {
  margin-top: 1rem;
  padding: 0.75rem 1.5rem;
  background: #FF5500;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s;
}

.btn-reset:hover {
  background: #DC143C;
}

/* Products Grid */
.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 1.5rem;
}

.product-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  cursor: pointer;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(255, 107, 53, 0.15);
}

.product-image-wrapper {
  position: relative;
  padding-top: 100%;
  background: #f9f9f9;
  overflow: hidden;
}

.product-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.product-card:hover .product-image {
  transform: scale(1.05);
}

.product-badges {
  position: absolute;
  top: 0.75rem;
  left: 0.75rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.badge {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: bold;
  text-transform: uppercase;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.badge-hot {
  background: linear-gradient(135deg, #ff416c, #ff4b2b);
  color: white;
}

.badge-new {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.badge-discount {
  background: linear-gradient(135deg, #ff6b35, #f97316);
  color: white;
  display: inline-flex;
  align-items: center;
  gap: 0.375rem;
  padding: 0.5rem 0.875rem;
  border: 2px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.4);
}

.badge-discount i {
  font-size: 0.875rem;
  animation: fire-flicker 1.5s ease-in-out infinite;
}

@keyframes fire-flicker {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.9;
  }
}

.wishlist-btn {
  position: absolute;
  top: 0.75rem;
  right: 0.75rem;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
  color: #999;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.wishlist-btn:hover {
  color: #DC143C;
  transform: scale(1.1);
}

.wishlist-btn.active {
  color: #DC143C;
}

.product-info {
  padding: 1rem;
}

.product-name {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: #333;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 2.5rem;
}

.product-specs {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.spec-item {
  font-size: 0.75rem;
  padding: 0.25rem 0.5rem;
  background: #f0f0f0;
  border-radius: 4px;
  color: #666;
}

.product-rating {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.stars {
  display: flex;
  gap: 0.125rem;
}

.stars i {
  font-size: 0.875rem;
  color: #FFD700;
}

.stars .bi-star {
  color: #ddd;
}

.rating-count {
  font-size: 0.8rem;
  color: #999;
}

.product-price {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
}

.current-price {
  font-size: 1.25rem;
  font-weight: bold;
  color: #DC143C;
}

.old-price {
  font-size: 0.9rem;
  color: #999;
  text-decoration: line-through;
}

.promotion-tag {
  display: inline-flex;
  align-items: center;
  gap: 0.375rem;
  padding: 0.375rem 0.75rem;
  background: linear-gradient(135deg, #F7931E, #FFD700);
  color: #2C1810;
  font-size: 0.8rem;
  font-weight: 600;
  border-radius: 6px;
  margin-bottom: 0.75rem;
}

.stock-info {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  font-size: 0.85rem;
  color: #28a745;
  margin-bottom: 0.75rem;
}

.out-of-stock {
  color: #dc3545;
}

.product-actions {
  display: flex;
  gap: 0.5rem;
  margin-top: 1rem;
}

.btn-buy-now {
  flex: 1;
  padding: 0.75rem;
  background: linear-gradient(135deg, #FF5500, #FF6B35);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(255, 85, 0, 0.3);
}

.btn-buy-now:hover:not(:disabled) {
  background: linear-gradient(135deg, #E04A00, #FF5500);
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(255, 85, 0, 0.4);
}

.btn-buy-now:disabled {
  background: #ccc;
  cursor: not-allowed;
  box-shadow: none;
}

.btn-add-cart {
  width: 48px;
  height: 48px;
  background: #f8f9fa;
  color: #FF5500;
  border: 2px solid #FF5500;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.btn-add-cart:hover:not(:disabled) {
  background: #FF5500;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 85, 0, 0.3);
}

.btn-add-cart:disabled {
  background: #f8f9fa;
  color: #ccc;
  border-color: #ccc;
  cursor: not-allowed;
}

/* Pagination */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.5rem;
  margin-top: 2rem;
  padding-top: 2rem;
  border-top: 2px solid #f0f0f0;
}

.page-btn {
  min-width: 40px;
  height: 40px;
  border: 2px solid #e0e0e0;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  color: #333;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.page-btn:hover:not(:disabled) {
  border-color: #FF5500;
  color: #FF5500;
}

.page-btn.active {
  background: #FF5500;
  color: white;
  border-color: #FF5500;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}


/* Toast Notification */
.toast-notification {
  position: fixed;
  bottom: 2rem;
  right: 2rem;
  padding: 1rem 1.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-weight: 600;
  z-index: 9999;
  max-width: 400px;
}

.toast-notification.success {
  background: #28a745;
  color: white;
}

.toast-notification.error {
  background: #dc3545;
  color: white;
}

.toast-notification.info {
  background: #17a2b8;
  color: white;
}

.toast-notification i {
  font-size: 1.5rem;
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateX(100px);
}

/* Confirm Modal */
.confirm-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; z-index: 10000; padding: 16px; }
.confirm-card { width: 100%; max-width: 520px; background: #fff; border-radius: 14px; box-shadow: 0 20px 40px rgba(0,0,0,0.3); overflow: hidden; }
.confirm-header { display: flex; align-items: center; justify-content: space-between; padding: 16px 20px; border-bottom: 1px solid #eee; }
.confirm-header h3 { margin: 0; font-size: 18px; font-weight: 700; color: #333; }
.confirm-close { background: none; border: none; font-size: 22px; cursor: pointer; color: #666; }
.confirm-content { padding: 16px 20px; }
.confirm-row { display: flex; justify-content: space-between; gap: 12px; padding: 8px 0; color: #555; }
.confirm-row .label { font-weight: 600; }
.confirm-actions { display: flex; justify-content: flex-end; gap: 10px; padding: 12px 20px 20px; }
.btn-cancel { background: #6c757d; color: #fff; border: none; padding: 10px 14px; border-radius: 10px; cursor: pointer; }
.btn-confirm { background: #FF5500; color: #fff; border: none; padding: 10px 14px; border-radius: 10px; cursor: pointer; }

/* Responsive */
@media (max-width: 1400px) {
  .shop-layout {
    grid-template-columns: 250px 1fr;
  }

  .products-grid {
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  }
}

@media (max-width: 992px) {
  .shop-layout {
    grid-template-columns: 1fr;
  }

  .shop-sidebar {
    position: static;
  }

  .products-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  }

  .promo-title {
    font-size: 1.25rem;
  }
}

@media (max-width: 768px) {
  .header-container {
    flex-direction: column;
    gap: 1rem;
    padding: 1rem;
  }


  .promo-content {
    flex-direction: column;
    gap: 1rem;
  }

  .promo-title {
    font-size: 1rem;
    text-align: center;
  }

  .products-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 1rem;
  }

  .product-name {
    font-size: 0.9rem;
  }

  .current-price {
    font-size: 1rem;
  }


  .toast-notification {
    right: 1rem;
    left: 1rem;
    max-width: none;
  }
}

@media (max-width: 480px) {
  .container {
    padding: 0 1rem;
  }

  .products-toolbar {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }

  .toolbar-right {
    flex-direction: column;
    align-items: stretch;
  }

  .sort-select {
    width: 100%;
  }

  .countdown-timer {
    flex-wrap: wrap;
  }

  .time-unit {
    min-width: 45px;
    padding: 0.375rem 0.5rem;
  }

  .time-value {
    font-size: 1.25rem;
  }
}
</style>

