<script setup lang="ts">
import { onMounted, onUnmounted, ref, computed, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/services/api'
import Toast from '@/components/Toast.vue'

interface ProductDetail {
  id: number
  maSanPham: string
  tenSanPham: string
  moTa?: string
  thietKe?: string
  kichThuoc?: string
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
  // Thông tin đánh giá
  averageRating?: number
  reviewCount?: number
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
  // Thông tin bảo hành
  baoHanh?: string
  giaBaoHanh?: number
}

interface Review {
  reviewId: number
  idSanPham: number
  idNguoiDung: number
  rating: number
  comment: string
  ngayTao: string
  tenNguoiDung?: string
}

const route = useRoute()
const router = useRouter()
const productId = computed(() => route.params.id as string)

// Product data
const product = ref<ProductDetail | null>(null)
const loading = ref(false)
const reviews = ref<Review[]>([])
const relatedProducts = ref<ProductDetail[]>([])

// Image gallery
const selectedImageIndex = ref(0)
const showImageModal = ref(false)

// Product selection
const selectedVariant = ref<VariantDetail | null>(null)
const selectedColor = ref<number | null>(null)
const selectedStorage = ref<number | null>(null)
const selectedWarranty = ref<string>('12')
const quantity = ref(1)

// Related products carousel
const relatedProductIndex = ref(0)
const windowWidth = ref(window.innerWidth)

// Specifications modal
const showSpecsModal = ref(false)

// Review form state
const showReviewForm = ref(false)
const reviewForm = ref({
  idNguoiDung: null, // Will be null for guests
  tenNguoiDung: '', // Guest name
  rating: 5,
  comment: ''
})
const isSubmittingReview = ref(false)
const isGuest = ref(true) // Assume guest by default

// Toast ref
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

// Rating statistics
const ratingStats = ref({
  averageRating: 0,
  reviewCount: 0
})

// Highlighted specifications
const highlightedSpecs = computed(() => {
  if (!product.value) return []

  const specs = []

  // Screen size
  if (product.value.tenManHinh) {
    specs.push({
      label: 'Kích thước màn hình',
      value: product.value.tenManHinh,
      description: 'Rất lớn'
    })
  }

  // Camera
  if (product.value.tenCameraSau) {
    specs.push({
      label: 'Camera',
      value: product.value.tenCameraSau,
      description: 'Cao cấp'
    })
  }

  // RAM
  if (currentVariant.value?.tenRam) {
    specs.push({
      label: 'RAM',
      value: currentVariant.value.tenRam,
      description: 'Rất lớn'
    })
  }

  // Storage
  if (currentVariant.value?.tenRom) {
    specs.push({
      label: 'Bộ nhớ',
      value: currentVariant.value.tenRom,
      description: 'Lưu trữ lớn'
    })
  }

  // Operating System
  if (product.value.tenHeDieuHanh) {
    specs.push({
      label: 'Hệ điều hành',
      value: product.value.tenHeDieuHanh,
      description: 'Mới nhất'
    })
  }

  // Battery
  if (product.value.tenPin) {
    specs.push({
      label: 'Pin',
      value: product.value.tenPin,
      description: 'Dung lượng lớn'
    })
  }

  return specs.slice(0, 4) // Show top 4 specs
})

// Available options
const availableColors = computed(() => {
  if (!product.value?.variants) return []
  const colors = new Map()
  product.value.variants.forEach(variant => {
    if (variant.idMauSac && variant.tenMauSac) {
      colors.set(variant.idMauSac, {
        id: variant.idMauSac,
        name: variant.tenMauSac,
        code: getColorCode(variant.idMauSac)
      })
    }
  })
  return Array.from(colors.values())
})

const availableStorage = computed(() => {
  if (!product.value?.variants) return []
  const storage = new Map()
  product.value.variants.forEach(variant => {
    if (variant.idRom && variant.tenRom) {
      storage.set(variant.idRom, {
        id: variant.idRom,
        name: variant.tenRom
      })
    }
  })
  return Array.from(storage.values())
})

// Current variant based on selection
const currentVariant = computed(() => {
  if (!product.value?.variants) return null

  return product.value.variants.find(variant =>
    variant.idMauSac === selectedColor.value &&
    variant.idRom === selectedStorage.value
  ) || product.value.variants[0]
})

// Product images - lấy tất cả ảnh từ tất cả các phiên bản
const productImages = computed(() => {
  if (!product.value?.variants) {
    return []
  }

  // Lấy tất cả ảnh từ tất cả các variants
  const allImages = []
  product.value.variants.forEach(variant => {
    if (variant.imageUrls && variant.imageUrls.length > 0) {
      allImages.push(...variant.imageUrls)
    }
  })

  // Loại bỏ ảnh trùng lặp
  const uniqueImages = Array.from(new Set(allImages))

  // Nếu có ảnh thực, trả về ảnh thực
  if (uniqueImages.length > 0) {
    return uniqueImages
  }

  // Fallback: Hiển thị hình ảnh mẫu khi không có hình ảnh thực
  const fallbackImages = [
    'https://via.placeholder.com/400x400/cccccc/666666?text=No+Image',
    'https://via.placeholder.com/400x400/cccccc/666666?text=Product+Image'
  ]
  return fallbackImages
})

// Ảnh của màu sắc đã chọn
const selectedColorImages = computed(() => {
  if (!product.value?.variants || !selectedColor.value) {
    return []
  }

  // Tìm variant có màu sắc đã chọn
  const colorVariant = product.value.variants.find(variant =>
    variant.idMauSac === selectedColor.value
  )

  return colorVariant?.imageUrls || []
})

// Stock status
const isInStock = computed(() => {
  return currentVariant.value ? currentVariant.value.soLuong > 0 : false
})

const stockQuantity = computed(() => {
  return currentVariant.value ? currentVariant.value.soLuong : 0
})

// Price formatting
function formatPrice(price: number) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

function getColorCode(idMauSac: number | null) {
  if (!idMauSac) return '#ccc'

  // Color mapping based on the image
  const colorMap: { [key: number]: string } = {
    1: '#FF6B35', // Cam Vũ Trụ (Cosmic Orange)
    2: '#1E3A8A', // Xanh Đậm (Dark Blue)
    3: '#C0C0C0', // Bạc (Silver)
    4: '#000000', // Đen
    5: '#FFFFFF', // Trắng
    6: '#FF0000', // Đỏ
    7: '#00FF00', // Xanh lá
    8: '#FFFF00', // Vàng
    9: '#800080', // Tím
    10: '#FFC0CB' // Hồng
  }

  return colorMap[idMauSac] || '#ccc'
}

// Load product data
async function loadProductDetail() {
  if (!productId.value) return

  loading.value = true
  try {
    const { data } = await api.get<ProductDetail>(`/api/san-pham/${productId.value}/view`)
    console.log('Product data from API:', data)
    product.value = data

    // Set default selections
    if (data.variants && data.variants.length > 0) {
      console.log('Variants found:', data.variants)
      const firstVariant = data.variants[0]
      selectedColor.value = firstVariant.idMauSac
      selectedStorage.value = firstVariant.idRom
      selectedVariant.value = firstVariant
    } else {
      console.log('No variants found in product data')
    }

    // Load related products
    await loadRelatedProducts()

    // Load reviews and rating stats
    await loadReviews()
    await loadRatingStats()

  } catch (error) {
    console.error('Lỗi khi tải chi tiết sản phẩm:', error)
    Toast.error('Không thể tải thông tin sản phẩm')
  } finally {
    loading.value = false
  }
}

async function loadRelatedProducts() {
  try {
    const { data } = await api.get<ProductDetail[]>('/api/san-pham/related')
    // Lọc bỏ sản phẩm hiện tại khỏi danh sách liên quan
    const filteredProducts = data.filter(p => p.id !== parseInt(productId.value))
    // Load số lượng phù hợp với kích thước màn hình
    const maxProducts = getMaxRelatedProducts()
    relatedProducts.value = filteredProducts.slice(0, maxProducts)
  } catch (error) {
    console.error('Lỗi khi tải sản phẩm liên quan:', error)
  }
}

async function loadReviews() {
  try {
    const { data } = await api.get<Review[]>(`/api/reviews/san-pham/${productId.value}`)
    reviews.value = data
  } catch (error) {
    console.error('Lỗi khi tải đánh giá:', error)
  }
}

async function loadRatingStats() {
  try {
    const { data } = await api.get(`/api/reviews/san-pham/${productId.value}/rating`)
    ratingStats.value = {
      averageRating: data.averageRating || 0,
      reviewCount: data.reviewCount || 0
    }
    
    // Update product with rating data
    if (product.value) {
      product.value.averageRating = data.averageRating || 0
      product.value.reviewCount = data.reviewCount || 0
    }
  } catch (error) {
    console.error('Lỗi khi tải thống kê đánh giá:', error)
  }
}

// Image gallery functions
function selectImage(index: number) {
  selectedImageIndex.value = index
}

function openImageModal() {
  showImageModal.value = true
}

function closeImageModal() {
  showImageModal.value = false
}

function nextImage() {
  if (productImages.value.length > 0) {
    selectedImageIndex.value = (selectedImageIndex.value + 1) % productImages.value.length
  }
}

function prevImage() {
  if (productImages.value.length > 0) {
    selectedImageIndex.value = selectedImageIndex.value === 0
      ? productImages.value.length - 1
      : selectedImageIndex.value - 1
  }
}

// Selection functions
function selectColor(colorId: number) {
  selectedColor.value = colorId
  quantity.value = 1 // Reset quantity when changing variant

  // Tự động chuyển đến ảnh của màu sắc đã chọn
  scrollToColorImages()
}

// Function để chuyển đến ảnh của màu sắc đã chọn
function scrollToColorImages() {
  if (selectedColorImages.value.length > 0) {
    // Tìm index của ảnh đầu tiên của màu sắc đã chọn
    const firstColorImage = selectedColorImages.value[0]
    const imageIndex = productImages.value.findIndex(img => img === firstColorImage)

    if (imageIndex !== -1) {
      selectedImageIndex.value = imageIndex
    }
  } else {
    // Nếu không có ảnh của màu sắc đã chọn, reset về ảnh đầu tiên
    selectedImageIndex.value = 0
  }
}

// Function để kiểm tra ảnh có thuộc về màu sắc đã chọn không
function isColorImage(imageUrl: string) {
  return selectedColorImages.value.includes(imageUrl)
}

// Related products carousel functions
function getVisibleProductsCount() {
  // Desktop: 4 sản phẩm, Tablet: 3 sản phẩm, Mobile: 2 sản phẩm
  if (windowWidth.value >= 1200) return 4
  if (windowWidth.value >= 768) return 3
  return 2
}

// Số lượng sản phẩm tối đa để load
function getMaxRelatedProducts() {
  // Load đủ sản phẩm cho carousel (3-4 trang)
  if (windowWidth.value >= 1200) return 12 // 3 trang x 4 sản phẩm
  if (windowWidth.value >= 768) return 9   // 3 trang x 3 sản phẩm
  return 8 // 4 trang x 2 sản phẩm
}

function getSlidePercentage() {
  // Tính phần trăm slide dựa trên số sản phẩm hiển thị
  const visibleCount = getVisibleProductsCount()
  return 100 / visibleCount
}

function nextRelatedProducts() {
  const visibleCount = getVisibleProductsCount()
  const maxIndex = Math.max(0, relatedProducts.value.length - visibleCount)
  relatedProductIndex.value = Math.min(relatedProductIndex.value + 1, maxIndex)
}

function prevRelatedProducts() {
  relatedProductIndex.value = Math.max(0, relatedProductIndex.value - 1)
}

// Specifications modal functions
function openSpecsModal() {
  showSpecsModal.value = true
}

function closeSpecsModal() {
  showSpecsModal.value = false
}

function selectStorage(storageId: number) {
  selectedStorage.value = storageId
  quantity.value = 1 // Reset quantity when changing variant
}

function selectWarranty(warranty: string) {
  selectedWarranty.value = warranty
}

function getStoragePrice(storageId: number): number {
  if (!product.value?.variants) return 0
  const variant = product.value.variants.find(v => v.idRom === storageId)
  return variant?.donGia || 0
}

function getColorPrice(colorId: number): number {
  if (!product.value?.variants) return 0
  const variant = product.value.variants.find(v => v.idMauSac === colorId)
  return variant?.donGia || 0
}

function getColorImage(colorId: number): string | null {
  if (!product.value?.variants) return null
  const variant = product.value.variants.find(v => v.idMauSac === colorId)
  return variant?.imageUrls?.[0] || null
}

function getLoyalCustomerPrice(): number {
  if (!currentVariant.value) return 0
  return Math.round(currentVariant.value.donGia * 0.996) // 0.4% discount
}

function getEduPrice(): number {
  if (!currentVariant.value) return 0
  return Math.round(currentVariant.value.donGia * 0.997) // 0.3% discount
}

// Quantity functions
function increaseQuantity() {
  if (currentVariant.value && quantity.value < currentVariant.value.soLuong) {
    quantity.value++
  }
}

function decreaseQuantity() {
  if (quantity.value > 1) {
    quantity.value--
  }
}

// Add to cart function
function addToCart() {
  if (!currentVariant.value) {
    toastRef.value?.error('Lỗi', 'Vui lòng chọn biến thể sản phẩm')
    return
  }

  if (!isInStock.value) {
    toastRef.value?.error('Lỗi', 'Sản phẩm hiện tại đã hết hàng')
    return
  }

  // Here you would implement the add to cart logic
  console.log('Adding to cart:', {
    variant: currentVariant.value,
    quantity: quantity.value
  })

  toastRef.value?.success('Thành công', `Đã thêm ${quantity.value} sản phẩm vào giỏ hàng!`)
}

// Review functions
function toggleReviewForm() {
  showReviewForm.value = !showReviewForm.value
  if (!showReviewForm.value) {
    resetReviewForm()
  }
}

function resetReviewForm() {
  reviewForm.value = {
    idNguoiDung: null,
    tenNguoiDung: '',
    rating: 5,
    comment: ''
  }
}

async function submitReview() {
  if (!reviewForm.value.comment.trim()) {
    toastRef.value?.error('Lỗi', 'Vui lòng điền đầy đủ thông tin đánh giá')
    return
  }

  // Validate guest information
  if (isGuest.value && !reviewForm.value.tenNguoiDung.trim()) {
    toastRef.value?.error('Lỗi', 'Vui lòng nhập tên của bạn')
    return
  }

  isSubmittingReview.value = true

  try {
    const reviewData = {
      idSanPham: parseInt(productId.value),
      idNguoiDung: isGuest.value ? null : reviewForm.value.idNguoiDung,
      tenNguoiDung: isGuest.value ? reviewForm.value.tenNguoiDung : null,
      rating: reviewForm.value.rating,
      comment: reviewForm.value.comment
    }

    // Submit review to API
    const { data } = await api.post('/api/reviews', reviewData)

    // Chỉ thêm vào danh sách nếu đánh giá đã được duyệt
    if (data.trangThai === 1) { // 1: DA_DUYET
      reviews.value.unshift(data)
      toastRef.value?.success('Thành công', 'Đánh giá của bạn đã được duyệt và hiển thị!')
    } else {
      // Hiển thị thông báo đánh giá đang chờ duyệt
      toastRef.value?.info('Thông báo', 'Đánh giá của bạn đã được gửi và đang chờ duyệt. Cảm ơn bạn!')
    }

    // Reload rating stats
    await loadRatingStats()

    // Reset form và ẩn form
    resetReviewForm()
    showReviewForm.value = false
  } catch (error) {
    console.error('Error submitting review:', error)
    toastRef.value?.error('Lỗi', 'Có lỗi xảy ra khi gửi đánh giá. Vui lòng thử lại.')
  } finally {
    isSubmittingReview.value = false
  }
}

// Navigation functions
function goBack() {
  router.back()
}

async function viewProduct(productId: number) {
  // Set loading state
  loading.value = true

  try {
    // Force navigation by using replace and adding timestamp
    await router.replace({
      name: 'product-detail',
      params: { id: productId },
      query: { t: Date.now() } // Add timestamp to force reload
    })

    // Wait for next tick to ensure route change is processed
    await nextTick()

    // Force reload data
    await loadProductDetail()
  } catch (error) {
    console.error('Navigation error:', error)
    loading.value = false
  }
}

// Get the best image for related product
function getRelatedProductImage(product: ProductDetail): string | null {
  if (!product.variants || product.variants.length === 0) {
    return null
  }

  // Strategy 1: Ưu tiên màu sắc phổ biến (đen, trắng, xanh dương)
  const popularColors = [1, 2, 4, 5] // ID của màu sắc phổ biến
  for (const colorId of popularColors) {
    const variantWithColor = product.variants.find(v => v.idMauSac === colorId)
    if (variantWithColor?.imageUrls && variantWithColor.imageUrls.length > 0) {
      return variantWithColor.imageUrls[0]
    }
  }

  // Strategy 2: Tìm variant có hình ảnh đầu tiên (bất kỳ màu nào)
  for (const variant of product.variants) {
    if (variant.imageUrls && variant.imageUrls.length > 0) {
      return variant.imageUrls[0]
    }
  }

  // Strategy 3: Fallback - lấy hình ảnh từ variant đầu tiên
  const firstVariant = product.variants[0]
  if (firstVariant?.imageUrls && firstVariant.imageUrls.length > 0) {
    return firstVariant.imageUrls[0]
  }

  return null
}

// Create full image URL
function createFullImageUrl(url: string): string {
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  if (url.startsWith('/')) {
    return `http://localhost:8080${url}`
  }
  return `http://localhost:8080/${url}`
}

// Watch for route changes to reload data
watch(() => route.params.id, (newId, oldId) => {
  if (newId !== oldId) {
    loadProductDetail()
  }
}, { immediate: true })

onMounted(() => {
  loadProductDetail()

  // Add resize listener for responsive carousel
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

function handleResize() {
  windowWidth.value = window.innerWidth
  // Reset carousel position if needed
  const maxIndex = Math.max(0, relatedProducts.value.length - getVisibleProductsCount())
  if (relatedProductIndex.value > maxIndex) {
    relatedProductIndex.value = maxIndex
  }
  // Reload related products with new count
  loadRelatedProducts()
}
</script>

<template>
  <div class="product-detail-page" :key="productId">
    <!-- Loading State -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>Đang tải sản phẩm...</p>
    </div>

    <!-- Product Content -->
    <div v-else-if="product" class="product-content">
      <!-- Customer Header -->
      <header class="customer-header">
        <div class="header-content">
          <button @click="goBack" class="back-button">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
            </svg>
            Quay lại
          </button>
          <div class="header-title">
            <h1>{{ product.tenSanPham }}</h1>
            <p class="header-subtitle">{{ product.tenHang || 'Điện thoại' }}</p>
          </div>
        </div>
      </header>

      <!-- Product Main Section -->
      <div class="product-main">
        <!-- Product Images -->
        <div class="product-images">
          <!-- Main Image -->
          <div class="main-image-container">
            <img
              v-if="productImages.length > 0"
              :src="createFullImageUrl(productImages[selectedImageIndex])"
              :alt="product.tenSanPham"
              class="main-image"
              @click="openImageModal"
            />
            <div v-else class="no-image">
              <div class="no-image-icon">📱</div>
              <p>Chưa có hình ảnh</p>
            </div>

            <!-- Image Navigation -->
            <button
              v-if="productImages.length > 1"
              @click="prevImage"
              class="image-nav prev"
              aria-label="Previous image"
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 20 20" fill="none">
                <path d="M12.2676 15.793C11.9677 16.0787 11.493 16.0672 11.2073 15.7672L6.20597 10.5168C5.93004 10.2271 5.93004 9.77187 6.20597 9.4822L11.2073 4.23173C11.493 3.93181 11.9677 3.92028 12.2676 4.20597C12.5676 4.49166 12.5791 4.96639 12.2934 5.26631L7.78483 9.99949L12.2934 14.7327C12.5791 15.0326 12.5676 15.5073 12.2676 15.793Z" fill="white"/>
              </svg>
            </button>
            <button
              v-if="productImages.length > 1"
              @click="nextImage"
              class="image-nav next"
              aria-label="Next image"
            >
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <g id="Chevron">
                  <path id="Shape" d="M8.29289 4.29289C7.90237 4.68342 7.90237 5.31658 8.29289 5.70711L14.5858 12L8.29289 18.2929C7.90237 18.6834 7.90237 19.3166 8.29289 19.7071C8.68342 20.0976 9.31658 20.0976 9.70711 19.7071L16.7071 12.7071C17.0976 12.3166 17.0976 11.6834 16.7071 11.2929L9.70711 4.29289C9.31658 3.90237 8.68342 3.90237 8.29289 4.29289Z" fill="white"/>
                </g>
              </svg>
            </button>
          </div>

          <!-- Thumbnail Images -->
          <div v-if="productImages.length > 1" class="thumbnail-images">
            <div class="gallery-label">
              <i class="bi bi-images"></i>
              <span>Thư viện</span>
            </div>
            <div class="thumbnails-container">
              <button
                v-for="(image, index) in productImages"
                :key="index"
                @click="selectImage(index)"
                :class="[
                  'thumbnail',
                  {
                    active: index === selectedImageIndex,
                    'color-match': isColorImage(image)
                  }
                ]"
              >
                <img :src="createFullImageUrl(image)" :alt="`${product.tenSanPham} ${index + 1}`" />
                <!-- Color indicator -->
                <div v-if="isColorImage(image)" class="color-indicator"></div>
              </button>
            </div>
          </div>

          <!-- Warranty Badge -->
          <div class="warranty-badge">
            <span class="badge-text">BẢO HÀNH 1 ĐỔI 1 12 THÁNG</span>
          </div>
        </div>

        <!-- Product Info -->
        <div class="product-info">
          <!-- Product Title -->
          <h1 class="product-title">{{ product.tenSanPham }}</h1>

          <!-- Rating Section -->
          <div class="rating-section">
            <div class="stars">
              <span v-for="i in 5" :key="i" class="star" :class="{ filled: i <= Math.round(product.averageRating || 0) }">★</span>
            </div>
            <span class="review-count">({{ product.reviewCount || 0 }} đánh giá)</span>
            <a href="#reviews" class="specs-link">Thông số kỹ thuật</a>
          </div>

          <!-- Price Location -->
          <div class="price-location">
            <span class="location-label">Giá tại:</span>
            <span class="location-text">Miền Bắc</span>
          </div>

          <!-- Price Section -->
          <div class="price-section">
            <div class="current-price">
              {{ currentVariant ? formatPrice(currentVariant.donGia) : 'Liên hệ' }}
            </div>
            <div v-if="currentVariant?.giaNhap && currentVariant.giaNhap < currentVariant.donGia" class="original-price">
              {{ formatPrice(currentVariant.giaNhap) }}
            </div>
          </div>

          <!-- Installment Section -->
          <div class="installment-section">
            <button class="installment-btn">
              <span class="installment-text">Trả góp 0%</span>
            </button>
            <span class="installment-desc">Trả góp chỉ từ 0₫</span>
          </div>

          <!-- Storage Selection (Phiên bản khác) -->
          <div v-if="availableStorage.length > 0" class="option-section">
            <h3 class="option-title">Phiên bản khác:</h3>
            <div class="storage-options">
              <button
                v-for="storage in availableStorage"
                :key="storage.id"
                @click="selectStorage(storage.id)"
                :class="['storage-option', { active: selectedStorage === storage.id }]"
              >
                <div class="storage-name">{{ storage.name }}</div>
                <div class="storage-price">{{ formatPrice(getStoragePrice(storage.id)) }}</div>
              </button>
            </div>
          </div>

          <!-- Color Selection -->
          <div v-if="availableColors.length > 0" class="option-section">
            <h3 class="option-title">Màu sắc:</h3>
            <div class="color-options">
              <button
                v-for="color in availableColors"
                :key="color.id"
                @click="selectColor(color.id)"
                :class="['color-option', { active: selectedColor === color.id }]"
                :style="{ backgroundColor: color.code }"
                :title="color.name"
              >
                <div class="color-thumbnail">
                  <img v-if="getColorImage(color.id)" :src="getColorImage(color.id)" :alt="color.name" />
                </div>
                <div class="color-info">
                  <div class="color-name">{{ color.name }}</div>
                  <div class="color-price">{{ formatPrice(getColorPrice(color.id)) }}</div>
                </div>
              </button>
            </div>
          </div>

          <!-- Quantity Selection -->
          <div v-if="isInStock" class="quantity-section">
            <h3 class="option-title">Số lượng:</h3>
            <div class="quantity-controls">
              <button @click="decreaseQuantity" class="quantity-btn">-</button>
              <input
                v-model.number="quantity"
                type="number"
                min="1"
                :max="stockQuantity"
                class="quantity-input"
              />
              <button @click="increaseQuantity" class="quantity-btn">+</button>
            </div>
          </div>

          <!-- Warranty Selection -->
          <div class="warranty-section">
            <h3 class="option-title">Bảo Hành:</h3>
            <div class="warranty-options">
              <button
                @click="selectWarranty('12')"
                :class="['warranty-option', { active: selectedWarranty === '12' }]"
              >
                <div class="warranty-info">
                  <div class="warranty-name">1 đổi 1 12 tháng</div>
                  <div class="warranty-price">Miễn phí</div>
                </div>
              </button>
              <button
                @click="selectWarranty('24')"
                :class="['warranty-option', { active: selectedWarranty === '24' }]"
              >
                <div class="warranty-info">
                  <div class="warranty-name">1 đổi 1 24 tháng</div>
                  <div class="warranty-price">+ 1.200.000 ₫</div>
                </div>
              </button>
            </div>
          </div>

          <!-- Add to Cart Button -->
          <div class="action-section">
            <button
              @click="addToCart"
              :disabled="!isInStock"
              class="add-to-cart-btn"
              :class="{ disabled: !isInStock }"
            >
              {{ isInStock ? 'SẴN HÀNG GIAO NGAY' : 'Hết hàng' }}
            </button>
          </div>

          <!-- Special Pricing -->
          <div class="special-pricing">
            <div class="pricing-item">
              <span class="pricing-label">Khách hàng thân thiết:</span>
              <span class="pricing-value">{{ formatPrice(getLoyalCustomerPrice()) }}</span>
            </div>
            <div class="pricing-item">
              <span class="pricing-label">Ưu đãi Edu:</span>
              <span class="pricing-value">{{ formatPrice(getEduPrice()) }}</span>
            </div>
          </div>

          <!-- Highlighted Specifications -->
          <div v-if="highlightedSpecs.length > 0" class="highlighted-specs">
            <h3 class="specs-title">Thông số kỹ thuật</h3>
            <div class="specs-grid">
              <div
                v-for="spec in highlightedSpecs"
                :key="spec.label"
                class="spec-item"
              >
                <div class="spec-value">{{ spec.value }}</div>
                <div class="spec-label">{{ spec.label }}</div>
                <div class="spec-description">{{ spec.description }}</div>
              </div>
            </div>
            <button @click="openSpecsModal" class="view-details-btn">
              Xem chi tiết
            </button>
          </div>

        </div>
      </div>

      <!-- Product Description -->
      <div v-if="product.moTa" class="product-description">
        <h2>Mô tả sản phẩm</h2>
        <div class="description-content">
          <p>{{ product.moTa }}</p>
        </div>
      </div>

      <!-- Reviews Section -->
      <div class="reviews-section">
        <div class="reviews-header">
          <div class="rating-summary">
            <h2>Đánh giá sản phẩm</h2>
            <div class="rating-stats">
              <div class="average-rating">
                <span class="rating-number">{{ ratingStats.averageRating.toFixed(1) }}</span>
                <div class="stars">
                  <span v-for="i in 5" :key="i" class="star" :class="{ filled: i <= Math.round(ratingStats.averageRating) }">★</span>
                </div>
                <span class="review-count">({{ ratingStats.reviewCount }} đánh giá)</span>
              </div>
            </div>
          </div>
          <button @click="toggleReviewForm" class="add-review-btn">
            {{ showReviewForm ? 'Hủy đánh giá' : 'Viết đánh giá' }}
          </button>
        </div>

        <!-- Review Form -->
        <div v-if="showReviewForm" class="review-form">
          <h3>Viết đánh giá của bạn</h3>
          <form @submit.prevent="submitReview">
            <!-- Guest Information Fields -->
            <div v-if="isGuest" class="guest-info">
              <div class="form-group">
                <label for="guestName">Tên của bạn *</label>
                <input
                  id="guestName"
                  v-model="reviewForm.tenNguoiDung"
                  type="text"
                  placeholder="Nhập tên của bạn"
                  required
                  class="form-input"
                />
              </div>

            </div>

            <div class="form-group">
              <label>Đánh giá của bạn *</label>
              <div class="rating-input">
                <span v-for="i in 5" :key="i"
                      @click="reviewForm.rating = i"
                      class="rating-star"
                      :class="{ active: i <= reviewForm.rating }">
                  ★
                </span>
                <span class="rating-text">{{ reviewForm.rating }}/5 sao</span>
              </div>
            </div>

            <div class="form-group">
              <label for="reviewContent">Nội dung đánh giá *</label>
              <textarea
                id="reviewContent"
                v-model="reviewForm.comment"
                placeholder="Chia sẻ trải nghiệm của bạn về sản phẩm này..."
                required
                rows="4"
                class="form-textarea"
              ></textarea>
            </div>

            <div class="form-actions">
              <button type="button" @click="toggleReviewForm" class="btn-cancel">
                Hủy
              </button>
              <button type="submit" :disabled="isSubmittingReview" class="btn-submit">
                {{ isSubmittingReview ? 'Đang gửi...' : 'Gửi đánh giá' }}
              </button>
            </div>
          </form>
        </div>

        <!-- Reviews List -->
        <div v-if="reviews.length > 0" class="reviews-list">
          <div v-for="review in reviews" :key="review.reviewId" class="review-item">
            <div class="review-header">
              <span class="reviewer-name">{{ review.tenNguoiDung || `Người dùng ${review.idNguoiDung}` }}</span>
              <div class="review-rating">
                <span v-for="i in 5" :key="i" class="star" :class="{ filled: i <= review.rating }">★</span>
              </div>
              <span class="review-date">{{ new Date(review.ngayTao).toLocaleDateString('vi-VN') }}</span>
            </div>
            <p class="review-content">{{ review.comment }}</p>
          </div>
        </div>
        <div v-else class="no-reviews">
          <p>Chưa có đánh giá nào cho sản phẩm này.</p>
        </div>
      </div>

      <!-- Related Products -->
      <div v-if="relatedProducts.length > 0" class="related-products">
        <h2>Sản phẩm liên quan</h2>
        <div class="related-carousel-container">
          <!-- Navigation Arrows -->
          <button
            v-if="relatedProducts.length > getVisibleProductsCount()"
            @click="prevRelatedProducts"
            class="related-nav prev"
            aria-label="Previous products"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 20 20" fill="none">
              <path d="M12.2676 15.793C11.9677 16.0787 11.493 16.0672 11.2073 15.7672L6.20597 10.5168C5.93004 10.2271 5.93004 9.77187 6.20597 9.4822L11.2073 4.23173C11.493 3.93181 11.9677 3.92028 12.2676 4.20597C12.5676 4.49166 12.5791 4.96639 12.2934 5.26631L7.78483 9.99949L12.2934 14.7327C12.5791 15.0326 12.5676 15.5073 12.2676 15.793Z" fill="white"/>
            </svg>
          </button>

          <button
            v-if="relatedProducts.length > getVisibleProductsCount()"
            @click="nextRelatedProducts"
            class="related-nav next"
            aria-label="Next products"
          >
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <g id="Chevron">
                <path id="Shape" d="M8.29289 4.29289C7.90237 4.68342 7.90237 5.31658 8.29289 5.70711L14.5858 12L8.29289 18.2929C7.90237 18.6834 7.90237 19.3166 8.29289 19.7071C8.68342 20.0976 9.31658 20.0976 9.70711 19.7071L16.7071 12.7071C17.0976 12.3166 17.0976 11.6834 16.7071 11.2929L9.70711 4.29289C9.31658 3.90237 8.68342 3.90237 8.29289 4.29289Z" fill="white"/>
              </g>
            </svg>
          </button>

          <div class="related-carousel" :style="{ transform: `translateX(-${relatedProductIndex * getSlidePercentage()}%)` }">
            <div
              v-for="relatedProduct in relatedProducts"
              :key="relatedProduct.id"
              class="related-item"
              @click="viewProduct(relatedProduct.id)"
            >
              <div class="related-image">
                <img
                  v-if="getRelatedProductImage(relatedProduct)"
                  :src="createFullImageUrl(getRelatedProductImage(relatedProduct))"
                  :alt="relatedProduct.tenSanPham"
                />
                <div v-else class="no-image">📱</div>
              </div>
              <h3 class="related-title">{{ relatedProduct.tenSanPham }}</h3>
              <div class="related-price">
                {{ relatedProduct.variants?.[0] ? formatPrice(relatedProduct.variants[0].donGia) : 'Liên hệ' }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Image Modal -->
    <div v-if="showImageModal" class="image-modal" @click="closeImageModal">
      <div class="modal-content" @click.stop>
        <button class="modal-close" @click="closeImageModal">×</button>
        <img
          v-if="productImages.length > 0"
          :src="createFullImageUrl(productImages[selectedImageIndex])"
          :alt="product?.tenSanPham"
          class="modal-image"
        />
        <div v-if="productImages.length > 1" class="modal-navigation">
          <button @click="prevImage" class="modal-nav prev">‹</button>
          <button @click="nextImage" class="modal-nav next">›</button>
        </div>
      </div>
    </div>

    <!-- Specifications Modal -->
    <div v-if="showSpecsModal" class="specs-modal" @click="closeSpecsModal">
      <div class="modal-content" @click.stop>
        <button class="modal-close" @click="closeSpecsModal">×</button>
        <h2 class="modal-title">Thông số kỹ thuật chi tiết</h2>
        <div class="specs-detail-grid">
          <div v-if="product.tenManHinh" class="spec-detail-item">
            <span class="spec-detail-label">Màn hình:</span>
            <span class="spec-detail-value">{{ product.tenManHinh }}</span>
          </div>
          <div v-if="product.tenCameraTruoc" class="spec-detail-item">
            <span class="spec-detail-label">Camera trước:</span>
            <span class="spec-detail-value">{{ product.tenCameraTruoc }}</span>
          </div>
          <div v-if="product.tenCameraSau" class="spec-detail-item">
            <span class="spec-detail-label">Camera sau:</span>
            <span class="spec-detail-value">{{ product.tenCameraSau }}</span>
          </div>
          <div v-if="currentVariant?.tenRam" class="spec-detail-item">
            <span class="spec-detail-label">RAM:</span>
            <span class="spec-detail-value">{{ currentVariant.tenRam }}</span>
          </div>
          <div v-if="currentVariant?.tenRom" class="spec-detail-item">
            <span class="spec-detail-label">Bộ nhớ trong:</span>
            <span class="spec-detail-value">{{ currentVariant.tenRom }}</span>
          </div>
          <div v-if="product.tenChip" class="spec-detail-item">
            <span class="spec-detail-label">Chip xử lý:</span>
            <span class="spec-detail-value">{{ product.tenChip }}</span>
          </div>
          <div v-if="product.tenGpu" class="spec-detail-item">
            <span class="spec-detail-label">GPU:</span>
            <span class="spec-detail-value">{{ product.tenGpu }}</span>
          </div>
          <div v-if="product.tenCpu" class="spec-detail-item">
            <span class="spec-detail-label">CPU:</span>
            <span class="spec-detail-value">{{ product.tenCpu }}</span>
          </div>
          <div v-if="product.tenHeDieuHanh" class="spec-detail-item">
            <span class="spec-detail-label">Hệ điều hành:</span>
            <span class="spec-detail-value">{{ product.tenHeDieuHanh }}</span>
          </div>
          <div v-if="product.tenPin" class="spec-detail-item">
            <span class="spec-detail-label">Pin:</span>
            <span class="spec-detail-value">{{ product.tenPin }}</span>
          </div>
          <div v-if="product.tenSim" class="spec-detail-item">
            <span class="spec-detail-label">SIM:</span>
            <span class="spec-detail-value">{{ product.tenSim }}</span>
          </div>
          <div v-if="product.tenHang" class="spec-detail-item">
            <span class="spec-detail-label">Thương hiệu:</span>
            <span class="spec-detail-value">{{ product.tenHang }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Toast Component -->
    <Toast ref="toastRef" />
  </div>
</template>

<style scoped>
/* Main Layout */
.product-detail-page {
  min-height: 100vh;
  background: #f8f9fa;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* Loading State */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 50vh;
  gap: 20px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #ff6b35;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Customer Header */
.customer-header {
  background: white;
  border-bottom: 1px solid #e2e8f0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.back-button {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f8f9fa;
  border: 1px solid #e2e8f0;
  color: #374151;
  padding: 10px 16px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.back-button:hover {
  background: #e5e7eb;
  color: #1f2937;
  transform: translateX(-2px);
}

.header-title h1 {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
  line-height: 1.2;
}

.header-subtitle {
  font-size: 16px;
  color: #6b7280;
  margin: 4px 0 0 0;
  font-weight: 500;
}

/* Product Main Section */
.product-main {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
  padding: 40px;
  background: white;
  max-width: 1200px;
  margin: 0 auto;
}

/* Product Images */
.product-images {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.main-image-container {
  position: relative;
  aspect-ratio: 1;
  border-radius: 12px;
  overflow: hidden;
  background: #f8f9fa;
}

.main-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.main-image:hover {
  transform: scale(1.05);
}

.no-image {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #6b7280;
}

.no-image-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.image-nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(0, 0, 0, 0.6);
  color: white;
  border: none;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  z-index: 10;
  backdrop-filter: blur(4px);
}

.image-nav:hover {
  background: rgba(0, 0, 0, 0.8);
  transform: translateY(-50%) scale(1.1);
}

.image-nav:active {
  transform: translateY(-50%) scale(0.95);
}

.image-nav.prev {
  left: 10px;
}

.image-nav.next {
  right: 10px;
}

.image-nav svg {
  width: 24px;
  height: 24px;
  transition: transform 0.2s ease;
}

.image-nav:hover svg {
  transform: scale(1.1);
}

.thumbnail-images {
  display: flex;
  flex-direction: column;
  gap: 15px;
  padding: 10px 0;
}

.gallery-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.gallery-label i {
  font-size: 16px;
  color: #6b7280;
}

.thumbnails-container {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding: 5px 0;
}

.thumbnail {
  flex-shrink: 0;
  width: 80px;
  height: 80px;
  border: 2px solid transparent;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
}

.thumbnail:hover {
  border-color: #ff6b35;
}

.thumbnail.active {
  border-color: #ff6b35;
}

.thumbnail.color-match {
  border-color: #10b981;
  position: relative;
}

.color-indicator {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 12px;
  height: 12px;
  background: #10b981;
  border-radius: 50%;
  border: 2px solid white;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

.thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* Warranty Badge */
.warranty-badge {
  position: absolute;
  bottom: 20px;
  left: 20px;
  background: #dc2626;
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  box-shadow: 0 2px 8px rgba(220, 38, 38, 0.3);
}

.badge-text {
  white-space: nowrap;
}

/* Product Info */
.product-info {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.product-title {
  font-size: 32px;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
  line-height: 1.2;
}

/* Rating Section */
.rating-section {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 10px;
}

.stars {
  display: flex;
  gap: 2px;
}

.stars .star {
  font-size: 18px;
  color: #ddd;
}

.stars .star.filled {
  color: #fbbf24;
}

.review-count {
  color: #6b7280;
  font-size: 14px;
}

.specs-link {
  color: #3b82f6;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
}

.specs-link:hover {
  text-decoration: underline;
}

/* Price Location */
.price-location {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  font-size: 14px;
}

.location-label {
  color: #6b7280;
}

.location-text {
  color: #374151;
  font-weight: 500;
}

/* Installment Section */
.installment-section {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
}

.installment-btn {
  background: #fbbf24;
  color: #1f2937;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.3s ease;
}

.installment-btn:hover {
  background: #f59e0b;
}

.installment-desc {
  color: #6b7280;
  font-size: 14px;
}

/* Price Section */
.price-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.current-price {
  font-size: 28px;
  font-weight: 700;
  color: #dc2626;
}

.original-price {
  font-size: 20px;
  color: #6b7280;
  text-decoration: line-through;
}

/* Stock Status */
.stock-status {
  font-size: 16px;
  font-weight: 500;
}

.in-stock {
  color: #059669;
}

.out-of-stock {
  color: #dc2626;
}

/* Option Sections */
.option-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.option-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.color-options {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.color-option {
  position: relative;
  width: 80px;
  height: 80px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: white;
  padding: 8px;
}

.color-option:hover {
  border-color: #ff6b35;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.2);
}

.color-option.active {
  border-color: #ff6b35;
  background: #fff7ed;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.3);
}

.color-thumbnail {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 4px;
}

.color-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.color-info {
  text-align: center;
}

.color-name {
  font-size: 12px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 2px;
}

.color-price {
  font-size: 11px;
  color: #6b7280;
  font-weight: 500;
}

.storage-options {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.storage-option {
  padding: 16px 20px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  background: white;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
  min-width: 120px;
  text-align: center;
}

.storage-option:hover {
  border-color: #ff6b35;
  background: #fff7ed;
}

.storage-option.active {
  border-color: #ff6b35;
  background: #ff6b35;
  color: white;
}

.storage-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
}

.storage-price {
  font-size: 14px;
  font-weight: 500;
  opacity: 0.8;
}

/* Quantity Section */
.quantity-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.quantity-controls {
  display: flex;
  align-items: center;
  gap: 0;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
  width: fit-content;
}

.quantity-btn {
  width: 40px;
  height: 40px;
  border: none;
  background: #f8f9fa;
  cursor: pointer;
  font-size: 18px;
  font-weight: 600;
  transition: background 0.3s ease;
}

.quantity-btn:hover {
  background: #e5e7eb;
}

.quantity-input {
  width: 60px;
  height: 40px;
  border: none;
  text-align: center;
  font-size: 16px;
  font-weight: 600;
}

.quantity-input:focus {
  outline: none;
}

/* Action Section */
.action-section {
  margin-top: 20px;
}

.add-to-cart-btn {
  width: 100%;
  padding: 16px 24px;
  background: linear-gradient(135deg, #ff6b35 0%, #fd7e14 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 6px -1px rgba(255, 107, 53, 0.3);
}

.add-to-cart-btn:hover:not(.disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 15px -3px rgba(255, 107, 53, 0.4);
}

.add-to-cart-btn.disabled {
  background: #9ca3af;
  cursor: not-allowed;
  box-shadow: none;
}

/* Warranty Section */
.warranty-section {
  margin-bottom: 20px;
}

.warranty-options {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.warranty-option {
  padding: 16px 20px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  background: white;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 150px;
  text-align: center;
}

.warranty-option:hover {
  border-color: #ff6b35;
  background: #fff7ed;
}

.warranty-option.active {
  border-color: #ff6b35;
  background: #ff6b35;
  color: white;
}

.warranty-name {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
}

.warranty-price {
  font-size: 12px;
  font-weight: 500;
  opacity: 0.8;
}

/* Special Pricing */
.special-pricing {
  margin-top: 20px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.pricing-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.pricing-item:last-child {
  margin-bottom: 0;
}

.pricing-label {
  color: #6b7280;
  font-size: 14px;
}

.pricing-value {
  color: #dc2626;
  font-weight: 600;
  font-size: 14px;
}

/* Highlighted Specifications */
.highlighted-specs {
  margin-top: 30px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.specs-title {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 16px 0;
}

.specs-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.spec-item {
  text-align: center;
  padding: 16px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  transition: transform 0.2s ease;
}

.spec-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.spec-value {
  font-size: 18px;
  font-weight: 700;
  color: #3b82f6;
  margin-bottom: 4px;
}

.spec-label {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 4px;
}

.spec-description {
  font-size: 12px;
  color: #6b7280;
}

.view-details-btn {
  width: 100%;
  padding: 12px 24px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.view-details-btn:hover {
  background: #2563eb;
  transform: translateY(-1px);
}

/* Product Features */
.product-features {
  margin-top: 20px;
}

.product-features h3 {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 16px 0;
}

.features-grid {
  display: grid;
  gap: 12px;
}

.feature-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.feature-label {
  font-weight: 500;
  color: #6b7280;
}

.feature-value {
  font-weight: 600;
  color: #1f2937;
}

/* Product Description */
.product-description {
  padding: 30px;
  background: white;
  max-width: 1200px;
  margin: 0 auto;
}

.product-description h2 {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 20px 0;
}

.description-content {
  font-size: 16px;
  line-height: 1.6;
  color: #374151;
}

/* Reviews Section */
.reviews-section {
  padding: 30px;
  background: white;
  max-width: 1200px;
  margin: 0 auto;
}

.reviews-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.rating-summary {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.rating-stats {
  display: flex;
  align-items: center;
  gap: 15px;
}

.average-rating {
  display: flex;
  align-items: center;
  gap: 10px;
}

.rating-number {
  font-size: 24px;
  font-weight: bold;
  color: #ff6b35;
}

.stars {
  display: flex;
  gap: 2px;
}

.stars .star {
  font-size: 18px;
  color: #ddd;
}

.stars .star.filled {
  color: #ffc107;
}

.review-count {
  color: #666;
  font-size: 14px;
}

.reviews-section h2 {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.add-review-btn {
  background: #ff6b35;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.3s ease;
}

.add-review-btn:hover {
  background: #e55a2b;
}

/* Review Form */
.review-form {
  background: #f8f9fa;
  padding: 24px;
  border-radius: 8px;
  margin-bottom: 30px;
  border: 1px solid #e5e7eb;
}

.guest-info {
  background: #fff;
  padding: 16px;
  border-radius: 6px;
  border: 1px solid #d1d5db;
  margin-bottom: 20px;
}

.review-form h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 20px 0;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  font-weight: 500;
  color: #374151;
  margin-bottom: 8px;
}

.form-input, .form-textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.3s ease;
}

.form-input:focus, .form-textarea:focus {
  outline: none;
  border-color: #ff6b35;
  box-shadow: 0 0 0 3px rgba(255, 107, 53, 0.1);
}

.form-textarea {
  resize: vertical;
  min-height: 100px;
}

.rating-input {
  display: flex;
  align-items: center;
  gap: 8px;
}

.rating-star {
  font-size: 24px;
  color: #d1d5db;
  cursor: pointer;
  transition: color 0.2s ease;
}

.rating-star.active {
  color: #fbbf24;
}

.rating-star:hover {
  color: #fbbf24;
}

.rating-text {
  font-size: 14px;
  color: #6b7280;
  margin-left: 8px;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.btn-cancel, .btn-submit {
  padding: 10px 20px;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-cancel {
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
}

.btn-cancel:hover {
  background: #e5e7eb;
}

.btn-submit {
  background: #ff6b35;
  color: white;
  border: none;
}

.btn-submit:hover:not(:disabled) {
  background: #e55a2b;
}

.btn-submit:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.review-item {
  padding: 20px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #f8f9fa;
}

.review-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}

.reviewer-name {
  font-weight: 600;
  color: #1f2937;
}

.review-rating {
  display: flex;
  gap: 2px;
}

.star {
  color: #d1d5db;
  font-size: 16px;
}

.star.filled {
  color: #fbbf24;
}

.review-date {
  color: #6b7280;
  font-size: 14px;
}

.review-content {
  color: #374151;
  line-height: 1.6;
  margin: 0;
}

.no-reviews {
  text-align: center;
  color: #6b7280;
  padding: 40px;
}

/* Related Products */
.related-products {
  padding: 30px;
  background: white;
  max-width: 1200px;
  margin: 0 auto;
}

.related-products h2 {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 20px 0;
}

.related-carousel-container {
  position: relative;
  overflow: hidden;
  margin: 0 -20px;
  padding: 0 20px;
}

.related-carousel {
  display: flex;
  transition: transform 0.3s ease;
  gap: 20px;
}

.related-nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(0, 0, 0, 0.6);
  color: white;
  border: none;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  z-index: 10;
  backdrop-filter: blur(4px);
}

.related-nav:hover {
  background: rgba(0, 0, 0, 0.8);
  transform: translateY(-50%) scale(1.1);
}

.related-nav.prev {
  left: 0;
}

.related-nav.next {
  right: 0;
}

.related-nav svg {
  width: 24px;
  height: 24px;
  transition: transform 0.2s ease;
}

.related-nav:hover svg {
  transform: scale(1.1);
}

.related-item {
  cursor: pointer;
  transition: transform 0.3s ease;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  flex: 0 0 calc(25% - 15px); /* 4 sản phẩm trên desktop */
  min-width: 200px;
}

.related-item:hover {
  transform: translateY(-4px);
}

.related-image {
  aspect-ratio: 1;
  background: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.related-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.related-image .no-image {
  font-size: 32px;
  color: #6b7280;
}

.related-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 12px 16px 8px;
  line-height: 1.4;
}

.related-price {
  font-size: 18px;
  font-weight: 700;
  color: #dc2626;
  margin: 0 16px 16px;
}

/* Image Modal */
.image-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  position: relative;
  max-width: 90vw;
  max-height: 90vh;
}

.modal-close {
  position: absolute;
  top: -40px;
  right: 0;
  background: none;
  border: none;
  color: white;
  font-size: 32px;
  cursor: pointer;
  z-index: 1001;
}

.modal-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.modal-navigation {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 100%;
  display: flex;
  justify-content: space-between;
  pointer-events: none;
}

.modal-nav {
  background: rgba(0, 0, 0, 0.5);
  color: white;
  border: none;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 24px;
  pointer-events: auto;
  transition: background 0.3s ease;
}

.modal-nav:hover {
  background: rgba(0, 0, 0, 0.7);
}

.modal-nav.prev {
  margin-left: 20px;
}

.modal-nav.next {
  margin-right: 20px;
}

/* Responsive Design */
@media (max-width: 768px) {
  .product-main {
    grid-template-columns: 1fr;
    gap: 20px;
    padding: 20px;
    margin: 10px;
  }

  .product-title {
    font-size: 24px;
  }

  .current-price {
    font-size: 24px;
  }

  .related-grid {
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  }
}

@media (max-width: 480px) {
  .breadcrumb {
    padding: 10px;
    font-size: 12px;
  }

  .product-main {
    padding: 15px;
    margin: 5px;
  }

  .product-title {
    font-size: 20px;
  }

  .current-price {
    font-size: 20px;
  }

  .color-options {
    justify-content: center;
  }

  .image-nav {
    width: 40px;
    height: 40px;
  }

  .image-nav svg {
    width: 20px;
    height: 20px;
  }

  .storage-options {
    justify-content: center;
  }

  .related-item {
    flex: 0 0 calc(50% - 10px); /* 2 sản phẩm trên mobile */
    min-width: 150px;
  }

  .related-nav {
    width: 40px;
    height: 40px;
  }

  .related-nav svg {
    width: 20px;
    height: 20px;
  }
}

/* Tablet breakpoint */
@media (max-width: 1024px) and (min-width: 769px) {
  .related-item {
    flex: 0 0 calc(33.333% - 15px); /* 3 sản phẩm trên tablet */
    min-width: 180px;
  }
}

/* Specifications Modal */
.specs-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.specs-modal .modal-content {
  background: white;
  border-radius: 16px;
  padding: 24px;
  max-width: 600px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
  position: relative;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
}

.specs-modal .modal-close {
  position: absolute;
  top: 16px;
  right: 16px;
  background: #f3f4f6;
  border: none;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 18px;
  color: #6b7280;
  transition: all 0.2s ease;
}

.specs-modal .modal-close:hover {
  background: #e5e7eb;
  color: #374151;
}

.modal-title {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 24px 0;
  padding-right: 40px;
}

.specs-detail-grid {
  display: grid;
  gap: 16px;
}

.spec-detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f8fafc;
  border-radius: 8px;
  border-left: 4px solid #3b82f6;
}

.spec-detail-label {
  font-weight: 600;
  color: #374151;
}

.spec-detail-value {
  font-weight: 500;
  color: #1f2937;
  text-align: right;
}

/* Responsive for modal */
@media (max-width: 768px) {
  .specs-modal .modal-content {
    width: 95%;
    padding: 20px;
  }

  .modal-title {
    font-size: 20px;
  }

  .spec-detail-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }

  .spec-detail-value {
    text-align: left;
  }
}
</style>
