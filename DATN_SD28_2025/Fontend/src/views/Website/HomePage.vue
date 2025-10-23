<template>
  <div class="homepage">
    <!-- Header Layout -->
    <HeaderLayout />

    <!-- Banner Carousel -->
    <section class="banner-carousel">
      <div id="bannerCarousel" class="carousel slide carousel-fade" data-bs-ride="carousel" data-bs-interval="8000">
        <!-- Carousel Indicators -->
        <div class="carousel-indicators">
          <button
            v-for="(slide, index) in bannerSlides"
            :key="index"
            type="button"
            data-bs-target="#bannerCarousel"
            :data-bs-slide-to="index"
            :class="{ active: index === 0 }"
            :aria-label="'Slide ' + (index + 1)"
          ></button>
        </div>

        <!-- Carousel Inner -->
        <div class="carousel-inner">
          <!-- Slide được render từ array -->
          <div
            v-for="(slide, index) in bannerSlides"
            :key="slide.id"
            class="carousel-item"
            :class="{ active: index === 0 }"
          >
            <!-- YouTube video slide -->
            <iframe
              v-if="slide.type === 'youtube'"
              class="carousel-media carousel-iframe"
              :src="slide.media"
              frameborder="0"
              allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
              allowfullscreen
            ></iframe>

            <!-- Local video slide -->
            <video
              v-else-if="slide.type === 'video'"
              autoplay
              muted
              loop
              playsinline
              class="carousel-media"
            >
              <source :src="slide.media" type="video/mp4">
            </video>

            <!-- Image slide -->
            <img
              v-else
              :src="slide.media"
              class="carousel-media"
              :alt="slide.title"
            >

            <!-- Overlay cho images -->
            <div v-if="slide.type === 'image'" class="carousel-overlay"></div>

            <!-- Caption -->
            <div class="carousel-caption">
              <h1 class="carousel-title">{{ slide.title }}</h1>
              <p class="carousel-subtitle">{{ slide.subtitle }}</p>
              <div class="carousel-buttons">
                <button class="btn btn-primary btn-lg" @click="scrollToProducts">
                  {{ slide.buttonPrimary || 'Mua ngay' }}
                </button>
                <button class="btn btn-outline-light btn-lg" @click="scrollToProducts">
                  {{ slide.buttonSecondary || 'Tìm hiểu thêm' }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Carousel Controls -->
        <button class="carousel-control-prev" type="button" data-bs-target="#bannerCarousel" data-bs-slide="prev">
          <span class="carousel-control-prev-icon" aria-hidden="true"></span>
          <span class="visually-hidden">Previous</span>
        </button>
        <button class="carousel-control-next" type="button" data-bs-target="#bannerCarousel" data-bs-slide="next">
          <span class="carousel-control-next-icon" aria-hidden="true"></span>
          <span class="visually-hidden">Next</span>
        </button>
      </div>
    </section>

    <!-- Features Section -->
    <section class="features-section">
      <div class="container">
        <div class="features-grid">
          <div class="feature-card feature-card-1">
            <div class="feature-icon">
              <i class="bi bi-truck"></i>
            </div>
            <div class="feature-info">
              <h3 class="feature-title">Vận chuyển miễn phí</h3>
              <p class="feature-desc">Hóa đơn trên 5 triệu</p>
            </div>
          </div>

          <div class="feature-card feature-card-2">
            <div class="feature-icon">
              <i class="bi bi-gift"></i>
            </div>
            <div class="feature-info">
              <h3 class="feature-title">Quà tặng hấp dẫn</h3>
              <p class="feature-desc">Hóa đơn trên 10 triệu</p>
            </div>
          </div>

          <div class="feature-card feature-card-3">
            <div class="feature-icon">
              <i class="bi bi-award"></i>
            </div>
            <div class="feature-info">
              <h3 class="feature-title">Chứng nhận chất lượng</h3>
              <p class="feature-desc">Sản phẩm chính hãng</p>
            </div>
          </div>

          <div class="feature-card feature-card-4">
            <div class="feature-icon">
              <i class="bi bi-headset"></i>
            </div>
            <div class="feature-info">
              <h3 class="feature-title">Hotline: 0919965342</h3>
              <p class="feature-desc">Hỗ trợ 24/7</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Promotional Banners -->
    <section class="promo-banners-section">
      <div class="container">
        <div class="promo-banners-grid">
          <div class="promo-banner-card">
            <img src="/samsung-galaxy-s23-plus-1020x570-1.png" alt="Samsung Galaxy" class="promo-img">
          </div>
          <div class="promo-banner-card">
            <img src="/oppo.png" alt="OPPO" class="promo-img">
          </div>
          <div class="promo-banner-card">
            <img src="/iphone2.png" alt="iPhone" class="promo-img">
          </div>
        </div>
      </div>
    </section>

    <!-- Hot Sale Banner -->
    <section v-if="showPromoBanner" class="hot-sale-banner">
      <div class="hot-sale-content">
        <h2 class="hot-sale-title">🔥 NGÀY HỘI PHONIX - SĂN DEAL NGAY!</h2>
        <div class="countdown">
          <span class="countdown-label">Kết thúc sau:</span>
          <div class="countdown-timer">
            <div class="time-unit">
              <span class="time-value">{{ countdown.days }}</span>
              <span class="time-label">Ngày</span>
            </div>
            <span class="time-separator">:</span>
            <div class="time-unit">
              <span class="time-value">{{ countdown.hours }}</span>
              <span class="time-label">Giờ</span>
            </div>
            <span class="time-separator">:</span>
            <div class="time-unit">
              <span class="time-value">{{ countdown.minutes }}</span>
              <span class="time-label">Phút</span>
            </div>
            <span class="time-separator">:</span>
            <div class="time-unit">
              <span class="time-value">{{ countdown.seconds }}</span>
              <span class="time-label">Giây</span>
            </div>
          </div>
        </div>
        <button class="close-banner" @click="showPromoBanner = false">
          <i class="bi bi-x-lg"></i>
        </button>
      </div>
    </section>

    <!-- Featured Brands -->
    <section id="brands" class="brands-section">
      <div class="container">
        <h2 class="section-title">HÃNG</h2>

        <div v-if="brandsLoading" class="loading-state">
          <div class="spinner"></div>
          <p>Đang tải hãng...</p>
        </div>

        <div v-else-if="brands.length > 0" class="brands-grid">
          <div
            v-for="brand in brands"
            :key="brand.id"
            class="brand-card"
            @click="filterByBrand(brand.id)"
          >
            <div class="brand-image" :data-text="brand.displayText">
              <img :src="brand.image" :alt="brand.tenHang" @error="handleBrandImageError">
            </div>
            <h3 class="brand-name">{{ brand.displayText }}</h3>
          </div>
        </div>

        <div v-else class="empty-state">
          <p>Không có hãng nào</p>
        </div>
      </div>
    </section>

    <!-- Products Section -->
    <section id="products" class="products-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">SẢN PHẨM BÁN CHẠY</h2>
          <button class="refresh-btn" @click="refreshCategories" :disabled="categoriesLoading">
            <i class="bi bi-arrow-clockwise" :class="{ 'spinning': categoriesLoading }"></i>
            {{ categoriesLoading ? 'Đang tải...' : 'Làm mới' }}
          </button>
        </div>

        <!-- Category Tabs -->
        <div class="category-tabs-container">
          <button class="scroll-left-btn" @click="scrollTabsLeft" v-show="showScrollLeft">
            <i class="bi bi-arrow-left"></i>
          </button>
          <div class="category-tabs">
            <button
              @click="selectedCategoryFilter = null"
              :class="['tab-btn', { active: selectedCategoryFilter === null }]"
            >
              Tất cả
            </button>
            <button
              v-for="category in categories"
              :key="category.id"
              @click="selectedCategoryFilter = category.id"
              :class="['tab-btn', { active: selectedCategoryFilter === category.id }]"
            >
              {{ category.name }}
            </button>
            <button class="view-all-btn" @click="scrollTabsRight">
              <i class="bi bi-arrow-right"></i>
            </button>
          </div>
        </div>

        <!-- Products Grid -->
        <div v-if="loading" class="loading-state">
          <div class="spinner"></div>
          <p>Đang tải sản phẩm...</p>
        </div>

        <div v-else-if="displayedHomeProducts.length > 0" class="products-grid">
          <div
            v-for="product in displayedHomeProducts"
            :key="product.id"
            class="product-card"
            @click="viewProductDetail(product)"
          >
            <div class="product-image-wrapper">
              <img
                :src="getProductImage(product.hinhAnh)"
                :alt="product.tenSanPham"
                class="product-image"
                @error="handleImageError"
              />

              <div class="product-badges">
                <span v-if="product.discount > 0" class="badge badge-discount">
                  Giảm {{ product.discount }}%
                </span>
                <span class="badge badge-installment">
                  Trả góp 0%
                </span>
              </div>

            </div>

            <div class="product-info">
              <h3 class="product-name">
                {{ product.tenSanPham }}
                <span v-if="product.tenRam || product.tenRom" class="product-variant">
                  {{ [product.tenRam, product.tenRom].filter(Boolean).join(' ') }}
                </span>
                | Chính hãng VN/A
              </h3>

              <div class="product-price">
                <span class="current-price">{{ formatPrice(product.gia) }}</span>
                <span v-if="product.oldPrice" class="old-price">{{ formatPrice(product.oldPrice) }}</span>
              </div>

              <div class="product-footer">
                <button class="wishlist-footer-btn" @click.stop="toggleWishlist(product)">
                  <i class="bi bi-heart" :class="{ 'bi-heart-fill': product.isWishlisted }"></i>
                  <span>Yêu thích</span>
                </button>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="empty-state">
          <i class="bi bi-inbox"></i>
          <p>Không có sản phẩm nào</p>
        </div>

        <!-- View All Button -->
        <div class="view-all-container">
          <router-link to="/shop" class="btn-view-all" @click="scrollToTop">
            Xem tất cả sản phẩm
            <i class="bi bi-arrow-right"></i>
          </router-link>
        </div>
      </div>
    </section>

    <!-- News Section -->
    <section id="news" class="news-section">
      <div class="container">
        <h2 class="section-title">TIN TỨC CÔNG NGHỆ</h2>
        <div class="news-grid">
          <div v-for="article in newsArticles" :key="article.id" class="news-card">
            <div class="news-image">
              <img :src="article.image" :alt="article.title">
            </div>
            <div class="news-content">
              <h3 class="news-title">{{ article.title }}</h3>
              <p class="news-desc">{{ article.description }}</p>
              <div class="news-footer">
                <span class="news-date">
                  <i class="bi bi-clock"></i>
                  {{ article.date }}
                </span>
                <a href="#" class="news-link">Đọc thêm →</a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Newsletter Section -->
    <section class="newsletter-section">
      <div class="container">
        <div class="newsletter-content">
          <h2 class="newsletter-title">Đăng ký nhận tin từ PhoniX</h2>
          <p class="newsletter-desc">Nhận thông tin sản phẩm mới nhất và các chương trình khuyến mãi hấp dẫn</p>
          <div class="newsletter-form">
            <input
              v-model="newsletterEmail"
              type="email"
              placeholder="Nhập địa chỉ email của bạn"
              class="newsletter-input"
            >
            <button @click="subscribeNewsletter" class="btn-subscribe">
              <i class="bi bi-send-fill"></i>
              Đăng ký
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- Footer Layout -->
    <FooterLayout />

    <!-- Toast Notification -->
    <transition name="toast">
      <div v-if="toast.show" class="toast-notification" :class="toast.type">
        <i class="bi" :class="toast.icon"></i>
        <span>{{ toast.message }}</span>
      </div>
    </transition>

    <!-- Login Modal (Simple version) -->
    <div v-if="showLogin" class="modal-overlay" @click="showLogin = false">
      <div class="modal-box" @click.stop>
        <div class="modal-header">
          <h3>Đăng nhập</h3>
          <button class="close-modal" @click="showLogin = false">
            <i class="bi bi-x-lg"></i>
          </button>
        </div>
        <div class="modal-body">
          <p>Tính năng đăng nhập sẽ được cập nhật sớm!</p>
          <button class="btn btn-primary" @click="showLogin = false">Đóng</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cartStore'
import HeaderLayout from './HeaderLayout.vue'
import FooterLayout from './FooterLayout.vue'
import axios from 'axios'

const router = useRouter()
const cartStore = useCartStore()

// API Base URL
const API_BASE_URL = 'http://localhost:8080'

// State
const loading = ref(false)
const products = ref([])
const categories = ref([])
const categoriesLoading = ref(false)
const brands = ref([])
const brandsLoading = ref(false)
const showPromoBanner = ref(true)
const newsletterEmail = ref('')
const selectedCategoryFilter = ref(null)
const showScrollLeft = ref(false)
const showLogin = ref(false)

// Banner Slides - BẠN CÓ THỂ THÊM VIDEO/IMAGE MỚI VÀO ĐÂY!
const bannerSlides = ref([
  // Slide 1: Video local video01.mp4 (BẠN ĐÃ CÓ VIDEO NÀY!)
  {
    id: 1,
    type: 'video',
    media: '/videos/video02.mp4', // ← Video của bạn trong public/videos/
    title: 'Chào Mừng Đến PhoniX',
    subtitle: 'Hệ thống bán lẻ điện thoại #1 Việt Nam',
    buttonPrimary: 'Mua ngay',
    buttonSecondary: 'Khám phá'
  },
  // Slide 2: Video local video02.mp4 (BẠN ĐÃ CÓ VIDEO NÀY!)
  {
    id: 2,
    type: 'video',
    media: '/videos/video01.mp4', // ← Video của bạn trong public/
    title: 'Iphone 16 Series',
    subtitle: 'Titanium - So Strong - So Light - So Pro',
    buttonPrimary: 'Xem ngay',
    buttonSecondary: 'Chi tiết'
  }
])

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

// News articles
const newsArticles = ref([
  {
    id: 1,
    title: "iPhone 15 Pro Max: Đánh giá chi tiết sau 3 tháng sử dụng",
    description: "Sau 3 tháng trải nghiệm iPhone 15 Pro Max, đây là những điểm đáng chú ý về hiệu năng, camera và thời lượng pin...",
    date: '15/01/2025',
    image: '/iphone2.png' // ← Dùng image local thay placeholder
  },
  {
    id: 2,
    title: "Samsung Galaxy S24 Ultra - Siêu phẩm flagship 2025",
    description: "Galaxy S24 Ultra đã chính thức ra mắt với những nâng cấp đáng kể về camera AI, hiệu năng Snapdragon 8 Gen 3...",
    date: '12/01/2025',
    image: '/samsung-galaxy-s23-plus-1020x570-1.png' // ← Dùng image local
  },
  {
    id: 3,
    title: "Top 5 smartphone pin trâu nhất năm 2025",
    description: "Tổng hợp những chiếc điện thoại có thời lượng pin tốt nhất hiện nay, phù hợp cho người dùng di động nhiều...",
    date: '10/01/2025',
    image: '/iphone1.png' // ← Dùng image local
  }
])

// Computed
const displayedHomeProducts = computed(() => {
  let filtered = [...products.value]

  if (selectedCategoryFilter.value !== null) {
    filtered = filtered.filter(p => p.danhMucId === selectedCategoryFilter.value)
  }

  // Sort by sales and limit to 8
  return filtered
    .sort((a, b) => (b.soLuongDaBan || 0) - (a.soLuongDaBan || 0))
    .slice(0, 8)
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
    showToast('error', 'Không thể tải sản phẩm', 'bi-exclamation-circle-fill')
  } finally {
    loading.value = false
  }
}

const enhanceProduct = (product) => {
  const salesCount = product.soLuongDaBan || 0
  let rating = 3.5
  if (salesCount > 100) rating = 5.0
  else if (salesCount > 50) rating = 4.8
  else if (salesCount > 20) rating = 4.5
  else if (salesCount > 10) rating = 4.0

  const discountMultiplier = 1.2 + Math.random() * 0.1
  const oldPrice = product.gia * discountMultiplier
  const discount = Math.round(((oldPrice - product.gia) / oldPrice) * 100)

  const isHot = salesCount > 30 || discount > 20
  const isNew = Math.random() > 0.7

  return {
    ...product,
    rating: Math.round(rating * 10) / 10,
    reviewCount: Math.max(5, Math.floor(salesCount * 0.8)),
    oldPrice,
    discount,
    isHot,
    isNew,
    isWishlisted: cartStore.isInWishlist(product.id)
  }
}

const fetchCategories = async () => {
  categoriesLoading.value = true
  try {
    const response = await axios.get(`${API_BASE_URL}/api/danh-muc`, {
      params: {
        _t: Date.now() // Prevent caching
      }
    })
    console.log('Categories API response:', response.data)
    if (response.data && Array.isArray(response.data)) {
      categories.value = response.data
        .filter(c => c.trangThai === 1)
        .map(c => ({
          id: c.id,
          name: c.tenDanhMuc,
          image: createPlaceholderImage(c.tenDanhMuc, 150, 150, '#FF6B35')
        }))
      console.log('Processed categories:', categories.value)
    }
  } catch (error) {
    console.error('Error fetching categories:', error)
  } finally {
    categoriesLoading.value = false
  }
}

const fetchBrands = async () => {
  brandsLoading.value = true
  try {
    const response = await axios.get(`${API_BASE_URL}/api/hang`)
    if (response.data && Array.isArray(response.data)) {
      brands.value = response.data
        .filter(b => b.trangThai === 1)
        .map(b => ({
          id: b.id,
          tenHang: b.tenHang,
          image: createPlaceholderImage(b.tenHang, 200, 200, '#FF6B35'),
          displayText: b.tenHang // Thêm text để hiển thị
        }))
    }
  } catch (error) {
    console.error('Error fetching brands:', error)
  } finally {
    brandsLoading.value = false
  }
}

const createPlaceholderImage = (text, width = 200, height = 200, bgColor = '#cccccc') => {
  const svgContent = `
    <svg width="${width}" height="${height}" xmlns="http://www.w3.org/2000/svg">
      <rect width="${width}" height="${height}" fill="${bgColor}"/>
      <text x="${width/2}" y="${height/2}" text-anchor="middle" dy=".3em" fill="#ffffff" font-family="Arial" font-size="14" font-weight="bold">${text}</text>
    </svg>
  `
  return `data:image/svg+xml;charset=utf-8,${encodeURIComponent(svgContent)}`
}


const filterByCategory = (categoryId) => {
  router.push({ path: '/shop', query: { category: categoryId } })
}

const filterByBrand = (brandId) => {
  router.push({ path: '/shop', query: { brand: brandId } })
}

const handleBrandImageError = (event) => {
  event.target.src = createPlaceholderImage('Brand', 200, 200, '#FF6B35')
}

const scrollToProducts = () => {
  const element = document.getElementById('products')
  if (element) {
    element.scrollIntoView({ behavior: 'smooth' })
  }
}

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const scrollTabsLeft = () => {
  const tabsContainer = document.querySelector('.category-tabs-container')
  if (tabsContainer) {
    tabsContainer.scrollBy({ left: -200, behavior: 'smooth' })
  }
}

const scrollTabsRight = () => {
  const tabsContainer = document.querySelector('.category-tabs-container')
  if (tabsContainer) {
    tabsContainer.scrollBy({ left: 200, behavior: 'smooth' })
  }
}

const checkScrollPosition = () => {
  const tabsContainer = document.querySelector('.category-tabs-container')
  if (tabsContainer) {
    showScrollLeft.value = tabsContainer.scrollLeft > 0
  }
}

const refreshCategories = async () => {
  await fetchCategories()
  // Re-check scroll position after categories are loaded
  setTimeout(checkScrollPosition, 100)
}

const addToCart = (product) => {
  try {
    cartStore.addItem(product)
    showToast('success', `Đã thêm "${product.tenSanPham}" vào giỏ hàng!`, 'bi-cart-check-fill')
  } catch (error) {
    showToast('error', error.message, 'bi-exclamation-circle-fill')
  }
}

const toggleWishlist = (product) => {
  const isAdded = cartStore.toggleWishlist(product.id)
  product.isWishlisted = isAdded
  const message = isAdded
    ? `Đã thêm "${product.tenSanPham}" vào yêu thích!`
    : `Đã xóa "${product.tenSanPham}" khỏi yêu thích!`
  showToast('info', message, 'bi-heart-fill')
}

const viewProductDetail = (product) => {
  router.push({ name: 'product-detail', params: { id: product.id } })
}

const subscribeNewsletter = () => {
  if (newsletterEmail.value.trim()) {
    showToast('success', 'Đăng ký nhận tin thành công!', 'bi-check-circle-fill')
    newsletterEmail.value = ''
  } else {
    showToast('error', 'Vui lòng nhập email hợp lệ', 'bi-exclamation-circle-fill')
  }
}

const formatPrice = (price) => {
  if (!price) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

const getProductImage = (imagePath) => {
  // Tạo placeholder SVG local (không cần internet)
  const placeholderSVG = 'data:image/svg+xml;charset=utf-8,%3Csvg xmlns="http://www.w3.org/2000/svg" width="300" height="300"%3E%3Crect width="300" height="300" fill="%23f0f0f0"/%3E%3Ctext x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" fill="%23999" font-family="Arial" font-size="16"%3ENo Image%3C/text%3E%3C/svg%3E'

  if (!imagePath) return placeholderSVG
  if (imagePath.startsWith('http')) return imagePath
  return `${API_BASE_URL}${imagePath.startsWith('/') ? '' : '/'}${imagePath}`
}

const handleImageError = (event) => {
  // Tạo placeholder SVG local thay vì dùng via.placeholder.com
  event.target.src = 'data:image/svg+xml;charset=utf-8,%3Csvg xmlns="http://www.w3.org/2000/svg" width="300" height="300"%3E%3Crect width="300" height="300" fill="%23f0f0f0"/%3E%3Ctext x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" fill="%23999" font-family="Arial" font-size="16"%3ENo Image%3C/text%3E%3C/svg%3E'
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
  endDate.setDate(endDate.getDate() + 3)
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
onMounted(() => {
  fetchProducts()
  refreshCategories()
  fetchBrands()
  updateCountdown()
  countdownInterval = setInterval(updateCountdown, 1000)

  // Add scroll listener for category tabs
  setTimeout(() => {
    const tabsContainer = document.querySelector('.category-tabs-container')
    if (tabsContainer) {
      tabsContainer.addEventListener('scroll', checkScrollPosition)
    }
  }, 100)
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

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.homepage {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  color: #333;
  background: #f5f5f5;
  padding-top: 77px; /* Compensate for fixed header */
}


/* Banner Carousel */
.banner-carousel {
  height: 90vh;
  position: relative;
  overflow: hidden;
}

.banner-carousel .carousel {
  height: 100%;
}

.banner-carousel .carousel-inner {
  height: 100%;
}

.banner-carousel .carousel-item {
  height: 100%;
  position: relative;
}

.carousel-media {
  width: 100%;
  height: 95%;
  object-fit: cover;
  filter: brightness(0.7);
}

/* YouTube iframe */
.carousel-iframe {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border: none;
  pointer-events: none; /* Disable click on iframe */
}

/* Overlay for images (darker background) */
.carousel-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.3);
  z-index: 1;
}

.carousel-caption {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  color: #fff;
  z-index: 2;
  max-width: 800px;
  padding: 0 20px;
}

.carousel-title {
  font-size: 3.5rem;
  font-weight: 700;
  margin-bottom: 1rem;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
}

.carousel-subtitle {
  font-size: 1.5rem;
  margin-bottom: 2rem;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
}

.carousel-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.carousel-buttons .btn {
  padding: 12px 30px;
  font-size: 1rem;
  font-weight: 600;
  border-radius: 30px;
  cursor: pointer;
  transition: all 0.3s;
}

.carousel-buttons .btn-primary {
  background: var(--phoenix-accent);
  border: none;
  color: white;
}

.carousel-buttons .btn-primary:hover {
  background: var(--phoenix-primary);
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(220, 20, 60, 0.4);
}

.carousel-buttons .btn-outline-light {
  background: transparent;
  border: 2px solid white;
  color: white;
}

.carousel-buttons .btn-outline-light:hover {
  background: white;
  color: var(--phoenix-accent);
}

/* Carousel Controls (Prev/Next Buttons) */
.banner-carousel .carousel-control-prev,
.banner-carousel .carousel-control-next {
  width: 60px;
  height: 60px;
  background: rgba(255, 85, 0, 0.3);
  border-radius: 50%;
  top: 50%;
  transform: translateY(-50%);
  transition: all 0.3s;
  backdrop-filter: blur(10px);
  opacity: 0.8;
  border: 2px solid rgba(255, 255, 255, 0.5);
}

.banner-carousel .carousel-control-prev:hover,
.banner-carousel .carousel-control-next:hover {
  background: rgba(255, 85, 0, 0.6);
  transform: translateY(-50%) scale(1.15);
  opacity: 1;
  border-color: white;
}

.banner-carousel .carousel-control-prev {
  left: 30px;
}

.banner-carousel .carousel-control-next {
  right: 30px;
}

.banner-carousel .carousel-control-prev-icon,
.banner-carousel .carousel-control-next-icon {
  width: 30px;
  height: 30px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.5));
}

/* Carousel Indicators (Dots) */
.banner-carousel .carousel-indicators {
  bottom: 30px;
  z-index: 3;
  margin: 0;
}

.banner-carousel .carousel-indicators button {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  border: 2px solid white;
  margin: 0 8px;
  transition: all 0.3s;
  opacity: 0.7;
  padding: 0;
}

.banner-carousel .carousel-indicators .active {
  background: #FF5500;
  transform: scale(1.4);
  opacity: 1;
  box-shadow: 0 0 10px rgba(255, 85, 0, 0.8);
}

.banner-carousel .carousel-indicators button:hover {
  background: rgba(255, 255, 255, 0.9);
  opacity: 1;
  transform: scale(1.2);
}

/* Fade Transition Effect */
.carousel-fade .carousel-item {
  opacity: 0;
  transition: opacity 0.8s ease-in-out;
}

.carousel-fade .carousel-item.active {
  opacity: 1;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-slide-up {
  animation: slideUp 1s ease-out;
}

/* Features Section */
.features-section {
  padding: 3rem 0;
  background: white;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

.container-wide {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 2rem;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1.5rem;
  max-width: 1200px;
  margin: 0 auto;
}

.feature-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
  border: none;
}

/* Card 1 - Light Blue */
.feature-card-1 {
  background: #e3f2fd;
}

/* Card 2 - Light Pink */
.feature-card-2 {
  background: #fce4ec;
}

/* Card 3 - Light Yellow */
.feature-card-3 {
  background: #fff8e1;
}

/* Card 4 - Light Green */
.feature-card-4 {
  background: #e8f5e8;
}

.feature-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
}

.feature-icon {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #333;
  font-size: 1.5rem;
  flex-shrink: 0;
  border: none;
}

.feature-info {
  flex: 1;
}

.feature-title {
  font-size: 1rem;
  font-weight: 700;
  color: #333;
  margin: 0 0 0.25rem 0;
  line-height: 1.2;
}

.feature-desc {
  font-size: 0.85rem;
  font-weight: 400;
  color: #666;
  margin: 0;
  line-height: 1.2;
}

/* Promo Banners Section */
.promo-banners-section {
  padding: 2rem 0;
  background: #f8f9fa;
}

.promo-banners-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
}

.promo-banner-card {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
  cursor: pointer;
}

.promo-banner-card:hover {
  transform: translateY(-5px);
}

.promo-img {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

/* Hot Sale Banner */
.hot-sale-banner {
  background: linear-gradient(135deg, var(--phoenix-accent), var(--phoenix-primary));
  padding: 2rem 0;
  position: relative;
}

.hot-sale-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2rem;
  flex-wrap: wrap;
  position: relative;
}

.hot-sale-title {
  color: white;
  font-size: 1.8rem;
  font-weight: bold;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.countdown {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.countdown-label {
  color: white;
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
  padding: 0.75rem 1rem;
  border-radius: 8px;
  min-width: 60px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.time-value {
  font-size: 1.8rem;
  font-weight: bold;
  color: var(--phoenix-accent);
}

.time-label {
  font-size: 0.75rem;
  color: #666;
  text-transform: uppercase;
}

.time-separator {
  font-size: 1.8rem;
  font-weight: bold;
  color: white;
}

.close-banner {
  position: absolute;
  top: 0.5rem;
  right: 1rem;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  font-size: 1.25rem;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 50%;
  width: 35px;
  height: 35px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.close-banner:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* Brands Section */
.brands-section {
  padding: 4rem 0;
  background: white;
}

.section-title {
  text-align: center;
  font-size: 2rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 2.5rem;
  position: relative;
  padding-bottom: 1rem;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 4px;
  background: linear-gradient(90deg, var(--phoenix-primary), var(--phoenix-secondary));
  border-radius: 2px;
}

.brands-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
  max-width: 1200px;
  margin: 0 auto;
}

.brand-card {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  cursor: pointer;
  border: 1px solid #e0e0e0;
}

.brand-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 20px rgba(255, 107, 53, 0.15);
  border-color: #FF5500;
}

.brand-image {
  width: 120px;
  height: 120px;
  margin: 0 auto 1rem;
  border-radius: 12px;
  overflow: hidden;
  background: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #e0e0e0;
  position: relative;
}

.brand-image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.brand-image::after {
  content: attr(data-text);
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  font-size: 0.75rem;
  font-weight: 600;
  padding: 0.25rem;
  text-align: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.brand-card:hover .brand-image::after {
  opacity: 1;
}

.brand-name {
  font-size: 1rem;
  font-weight: 600;
  color: #333;
  margin: 0;
}

/* Products Section */
.products-section {
  padding: 4rem 0;
  background: #f8f9fa;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  color: #666;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s;
}

.refresh-btn:hover:not(:disabled) {
  background: #f8f9fa;
  border-color: #FF5500;
  color: #FF5500;
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.category-tabs-container {
  margin-bottom: 2.5rem;
  overflow-x: auto;
  overflow-y: hidden;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
  -ms-overflow-style: none;
  position: relative;
}

.category-tabs-container::-webkit-scrollbar {
  display: none;
}

.category-tabs {
  display: flex;
  gap: 0.75rem;
  padding: 0.5rem 50px 0.5rem 50px;
  min-width: max-content;
}

.tab-btn {
  padding: 0.75rem 1.25rem;
  border: none;
  background: #f0f0f0;
  border-radius: 8px;
  font-weight: 600;
  color: #333;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

.tab-btn:hover {
  background: #e8e8e8;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.tab-btn.active {
  background: #FF5500;
  color: white;
  box-shadow: 0 4px 8px rgba(255, 85, 0, 0.3);
}

.view-all-btn {
  padding: 0.75rem;
  border: none;
  background: white;
  border-radius: 50%;
  font-weight: 600;
  color: #333;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #e0e0e0;
  width: 40px;
  height: 40px;
}

.view-all-btn:hover {
  background: #f8f9fa;
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  border-color: #FF5500;
  color: #FF5500;
}

.view-all-btn i {
  font-size: 0.9rem;
  transition: transform 0.3s;
}

.view-all-btn:hover i {
  transform: translateX(2px);
}

.scroll-left-btn {
  position: absolute;
  left: 5px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 10;
  width: 40px;
  height: 40px;
  border: none;
  background: white;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #333;
  font-size: 1rem;
  transition: all 0.3s;
  border: 1px solid #e0e0e0;
}

.scroll-left-btn:hover {
  background: #f8f9fa;
  transform: translateY(-50%) scale(1.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.product-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(255, 182, 193, 0.3);
  border: 1px solid rgba(255, 182, 193, 0.2);
  transition: all 0.3s;
  cursor: pointer;
  position: relative;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(255, 182, 193, 0.4);
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
  padding: 0.375rem 0.75rem;
  border-radius: 15px;
  font-size: 0.75rem;
  font-weight: bold;
  text-transform: uppercase;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  min-width: 60px;
  text-align: center;
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
  background: #dc3545;
  color: white;
}

.badge-installment {
  background: #e3f2fd;
  color: #1976d2;
  border: 1px solid #bbdefb;
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

.wishlist-btn:hover,
.wishlist-btn.active {
  color: var(--phoenix-accent);
  transform: scale(1.1);
}

.product-info {
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.product-name {
  font-size: 0.95rem;
  font-weight: 500;
  color: #333;
  line-height: 1.4;
  margin: 0;
}

.product-variant {
  font-weight: 600;
  color: #666;
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
  color: var(--phoenix-gold);
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
}

.current-price {
  font-size: 1.4rem;
  font-weight: bold;
  color: #333;
}

.old-price {
  font-size: 1rem;
  color: #999;
  text-decoration: line-through;
}

.product-footer {
  display: flex;
  justify-content: flex-end;
}

.wishlist-footer-btn {
  background: none;
  border: none;
  color: #1976d2;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: color 0.2s;
}

.wishlist-footer-btn:hover {
  color: #0d47a1;
}

.wishlist-footer-btn i {
  font-size: 1rem;
}

.promotion-tag {
  display: inline-flex;
  align-items: center;
  gap: 0.375rem;
  padding: 0.375rem 0.75rem;
  background: linear-gradient(135deg, var(--phoenix-secondary), var(--phoenix-gold));
  color: var(--phoenix-dark);
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

.btn-add-cart {
  width: 100%;
  padding: 0.75rem;
  background: var(--phoenix-primary);
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

.btn-add-cart:hover:not(:disabled) {
  background: var(--phoenix-accent);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(220, 20, 60, 0.3);
}

.btn-add-cart:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.view-all-container {
  text-align: center;
  margin-top: 2rem;
}

.btn-view-all {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 1rem 2.5rem;
  background: white;
  color: var(--phoenix-primary);
  border: 2px solid var(--phoenix-primary);
  border-radius: 30px;
  font-weight: 700;
  text-decoration: none;
  transition: all 0.3s;
}

.btn-view-all:hover {
  background: var(--phoenix-primary);
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(255, 107, 53, 0.3);
}

/* News Section */
.news-section {
  padding: 4rem 0;
  background: white;
}

.news-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 2rem;
}

.news-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  cursor: pointer;
}

.news-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
}

.news-image {
  height: 200px;
  overflow: hidden;
}

.news-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.news-card:hover .news-image img {
  transform: scale(1.1);
}

.news-content {
  padding: 1.5rem;
}

.news-title {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 0.75rem;
  color: #333;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-desc {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 1rem;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
}

.news-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.news-date {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  font-size: 0.85rem;
  color: #999;
}

.news-link {
  font-size: 0.9rem;
  color: var(--phoenix-primary);
  text-decoration: none;
  font-weight: 600;
  transition: color 0.2s;
}

.news-link:hover {
  color: var(--phoenix-accent);
}

/* Newsletter Section */
.newsletter-section {
  padding: 4rem 0;
  background: linear-gradient(135deg, var(--phoenix-primary), var(--phoenix-accent));
  color: white;
  text-align: center;
}

.newsletter-content {
  max-width: 600px;
  margin: 0 auto;
  padding: 0 2rem;
}

.newsletter-title {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 1rem;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
}

.newsletter-desc {
  font-size: 1.1rem;
  margin-bottom: 2rem;
  opacity: 0.95;
}

.newsletter-form {
  display: flex;
  gap: 1rem;
  max-width: 500px;
  margin: 0 auto;
}

.newsletter-input {
  flex: 1;
  padding: 1rem 1.5rem;
  border: none;
  border-radius: 30px;
  font-size: 1rem;
  outline: none;
}

.btn-subscribe {
  padding: 1rem 2rem;
  background: white;
  color: var(--phoenix-accent);
  border: none;
  border-radius: 30px;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s;
  white-space: nowrap;
}

.btn-subscribe:hover {
  background: var(--phoenix-gold);
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}


/* Toast */
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

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.modal-box {
  background: white;
  padding: 2rem;
  border-radius: 15px;
  max-width: 400px;
  width: 90%;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.modal-header h3 {
  margin: 0;
  color: #333;
}

.close-modal {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #999;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-modal:hover {
  color: var(--phoenix-accent);
}

.modal-body {
  text-align: center;
}

.modal-body .btn {
  margin-top: 1rem;
  padding: 0.75rem 2rem;
  background: var(--phoenix-primary);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
}

/* Loading & Empty States */
.loading-state,
.empty-state {
  text-align: center;
  padding: 3rem 2rem;
  color: #666;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid var(--phoenix-primary);
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

/* Responsive */
@media (max-width: 1200px) {
  .products-grid {
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  }
}

@media (max-width: 992px) {
  .header-container {
    flex-wrap: wrap;
    gap: 1rem;
  }

  .search-section {
    order: 3;
    width: 100%;
  }

  .carousel-title {
    font-size: 2.5rem;
  }

  .carousel-subtitle {
    font-size: 1.2rem;
  }

  .features-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 1rem;
  }

  .products-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  }
}

@media (max-width: 768px) {
  .homepage {
    padding-top: 67px; /* Adjust for smaller header on mobile */
  }

  .logo-section .logo-img {
    height: 50px;
  }

  .header-container {
    height: 67px;
  }

  .header-actions {
    width: 100%;
    justify-content: space-around;
  }

  .action-text .label {
    display: none;
  }

  .menu-list {
    flex-wrap: wrap;
    justify-content: center;
    gap: 0.5rem;
    padding: 0.5rem 1rem;
  }

  .banner-carousel {
    height: 60vh;
  }

  .carousel-title {
    font-size: 2rem;
  }

  .carousel-subtitle {
    font-size: 1rem;
  }

  .carousel-buttons {
    flex-direction: column;
  }

  .banner-carousel .carousel-control-prev,
  .banner-carousel .carousel-control-next {
    width: 50px;
    height: 50px;
  }

  .banner-carousel .carousel-control-prev {
    left: 15px;
  }

  .banner-carousel .carousel-control-next {
    right: 15px;
  }

  .banner-carousel .carousel-indicators {
    bottom: 20px;
  }

  .banner-carousel .carousel-indicators button {
    width: 11px;
    height: 11px;
    margin: 0 5px;
  }

  .features-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .feature-card {
    padding: 1rem;
  }

  .feature-icon {
    width: 40px;
    height: 40px;
    font-size: 1.2rem;
  }

  .feature-title {
    font-size: 0.9rem;
  }

  .feature-desc {
    font-size: 0.75rem;
  }

  .category-tabs-container {
    margin-bottom: 2rem;
  }

  .tab-btn {
    padding: 0.6rem 1rem;
    font-size: 0.9rem;
  }

  .view-all-btn {
    padding: 0.6rem;
    font-size: 0.9rem;
    width: 35px;
    height: 35px;
  }

  .scroll-left-btn {
    width: 35px;
    height: 35px;
    font-size: 0.9rem;
  }

  .section-header {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }

  .refresh-btn {
    font-size: 0.8rem;
    padding: 0.4rem 0.8rem;
  }

  .hot-sale-title {
    font-size: 1.3rem;
  }

  .countdown {
    flex-direction: column;
  }

  .products-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 1rem;
  }

  .newsletter-form {
    flex-direction: column;
  }

  .newsletter-input {
    border-radius: 25px;
  }

  .btn-subscribe {
    border-radius: 25px;
    justify-content: center;
  }

  .footer-grid {
    grid-template-columns: 1fr;
  }

  .floating-buttons {
    right: 1rem;
    bottom: 1rem;
  }

  .floating-btn {
    width: 50px;
    height: 50px;
    font-size: 1.3rem;
  }
}

@media (max-width: 480px) {
  .homepage {
    padding-top: 62px; /* Adjust for smaller header on mobile */
  }

  .logo-section .logo-img {
    height: 45px;
  }

  .header-container {
    height: 62px;
  }

  .section-title {
    font-size: 1.5rem;
  }

  .products-grid {
    grid-template-columns: 1fr;
  }

  .time-unit {
    min-width: 50px;
    padding: 0.5rem 0.75rem;
  }

  .time-value {
    font-size: 1.5rem;
  }
}
</style>

