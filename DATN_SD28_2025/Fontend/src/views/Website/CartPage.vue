<template>
  <div class="cart-page">
    <!-- Header -->
    <HeaderLayout/>


    <!-- Cart Content -->
    <main class="cart-main">

      <div class="container">
        <div class="page-header">
          <h1 class="page-title">Giỏ hàng của bạn</h1>
          <p class="page-subtitle" v-if="cartStore.items.length > 0">{{ cartStore.items.length }} sản phẩm trong giỏ hàng</p>
        </div>
        <div v-if="cartStore.items.length === 0" class="empty-cart">
          <i class="bi bi-cart-x"></i>
          <h2>Giỏ hàng trống</h2>
          <p>Bạn chưa có sản phẩm nào trong giỏ hàng</p>
          <router-link to="/shop" class="btn-shop">
            <i class="bi bi-shop"></i>
            <p> Tiếp tục mua sắm</p>
          </router-link>
        </div>

        <div v-else class="cart-layout">
          <!-- Cart Items -->
          <div class="cart-items">
            <div class="cart-header">
              <h2 class="section-title">Sản phẩm ({{ cartStore.items.length }})</h2>
              <div class="select-all-container">
                <label class="select-all-checkbox">
                  <input
                    type="checkbox"
                    :checked="isAllSelected"
                    @change="toggleSelectAll"
                  >
                  <span class="checkmark"></span>
                  <span class="select-all-text">Chọn tất cả</span>
                </label>
              </div>
            </div>

            <div v-for="item in cartStore.items" :key="item.chiTietSanPhamId" class="cart-item">
              <div class="item-checkbox">
                <label class="item-checkbox-label">
                  <input
                    type="checkbox"
                    :checked="selectedItems.has(item.chiTietSanPhamId)"
                    @change="toggleItemSelection(item.chiTietSanPhamId)"
                  >
                  <span class="checkmark"></span>
                </label>
              </div>
              <div class="item-image">
                <img :src="getProductImage(item.hinhAnh)" :alt="item.tenSanPham">
              </div>

              <div class="item-info">
                <h3 class="item-name">{{ item.tenSanPham }}</h3>
                <div class="item-specs">
                  <span v-if="item.tenRam">{{ item.tenRam }}</span>
                  <span v-if="item.tenRom">{{ item.tenRom }}</span>
                  <span v-if="item.tenMauSac">{{ item.tenMauSac }}</span>
                </div>
                <p class="item-price">{{ formatPrice(item.gia) }}</p>
              </div>

              <div class="item-quantity">
                <button @click="decreaseQuantity(item)" class="qty-btn">
                  <i class="bi bi-dash"></i>
                </button>
                <span class="qty-value">{{ item.quantity }}</span>
                <button @click="increaseQuantity(item)" class="qty-btn">
                  <i class="bi bi-plus"></i>
                </button>
              </div>

              <div class="item-total">
                <p class="total-price">{{ formatPrice(item.gia * item.quantity) }}</p>
                <button @click="removeItem(item)" class="btn-remove">
                  <i class="bi bi-trash"></i>
                  Xóa
                </button>
              </div>
            </div>
          </div>

          <!-- Cart Summary -->
          <div class="cart-summary">
            <h3 class="summary-title">Tóm tắt đơn hàng</h3>

            <div class="summary-row">
              <span>Sản phẩm đã chọn:</span>
              <span class="summary-value">{{ getSelectedItemsCount() }}/{{ cartStore.items.length }}</span>
            </div>

            <div class="summary-row">
              <span>Tạm tính:</span>
              <span class="summary-value">{{ formatPrice(getSelectedItemsTotal()) }}</span>
            </div>



            <div class="summary-divider"></div>

            <div class="summary-row total">
              <span>Tổng cộng:</span>
              <span class="summary-total">{{ formatPrice(getSelectedItemsTotal()) }}</span>
            </div>

            <button class="btn-checkout" @click="proceedToCheckout" :disabled="getSelectedItemsCount() === 0">
              <i class="bi bi-credit-card"></i>
              {{ getSelectedItemsCount() > 0 ? `Thanh toán (${getSelectedItemsCount()})` : 'Chọn sản phẩm' }}
            </button>

            <router-link to="/shop" class="btn-continue">
              <i class="bi bi-arrow-left"></i>
              Tiếp tục mua sắm
            </router-link>

          </div>
        </div>
      </div>
      <section class="related-products">
        <div class="related-container">
          <h3 class="related-title">Có thể bạn quan tâm</h3>
          <div class="related-wrapper">
            <button class="scroll-btn scroll-left" @click="scrollLeft" :disabled="scrollPosition === 0">
              <i class="bi bi-chevron-left"></i>
            </button>
            <div class="related-scroll-container" ref="scrollContainer">
              <div class="related-grid">
                <div
                  v-for="(product, index) in recommendedProducts"
                  :key="index"
                  class="related-card"
                >
                  <img
                    :src="getProductImage(product.hinhAnh || product.anh)"
                    :alt="product.tenSanPham || product.ten"
                    class="related-image"
                    @error="handleImageError"
                  >
                  <h4 class="related-name">{{ product.tenSanPham || product.ten }}</h4>
                  <p class="related-price">{{ formatPrice(product.gia) }}</p>
                  <button class="related-btn" @click="viewProduct(product)">
                    Xem chi tiết
                  </button>
                </div>
              </div>
            </div>
            <button class="scroll-btn scroll-right" @click="scrollRight" :disabled="isScrollAtEnd">
              <i class="bi bi-chevron-right"></i>
            </button>
          </div>
        </div>
      </section>

    </main>

    <!-- Footer -->
    <footer class="cart-footer">
      <FooterLayout/>
    </footer>

    <!-- Toast -->
    <transition name="toast">
      <div v-if="toast.show" class="toast-notification" :class="toast.type">
        <i class="bi" :class="toast.icon"></i>
        <span>{{ toast.message }}</span>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { useCartStore } from '@/stores/cartStore.js'
import HeaderLayout from '@/views/Website/HeaderLayout.vue'
import FooterLayout from '@/views/Website/FooterLayout.vue'

const router = useRouter()
const cartStore = useCartStore()

const API_BASE_URL = 'http://localhost:8080'

// State quản lý checkbox
const selectedItems = ref(new Set())
const isAllSelected = ref(false)

// State quản lý scroll
const scrollPosition = ref(0)
const isScrollAtEnd = ref(false)
const scrollContainer = ref(null)

const toast = ref({
  show: false,
  type: 'success',
  message: '',
  icon: 'bi-check-circle-fill'
})

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

const increaseQuantity = (item) => {
  try {
    cartStore.increaseQuantity(item.chiTietSanPhamId)
  } catch (error) {
    showToast('error', error.message, 'bi-exclamation-circle-fill')
  }
}

const decreaseQuantity = (item) => {
  cartStore.decreaseQuantity(item.chiTietSanPhamId)
}

const removeItem = (item) => {
  cartStore.removeItem(item.chiTietSanPhamId)
  showToast('success', 'Đã xóa sản phẩm khỏi giỏ hàng', 'bi-check-circle-fill')
}

// Logic xử lý chọn sản phẩm
const toggleItemSelection = (itemId) => {
  if (selectedItems.value.has(itemId)) {
    selectedItems.value.delete(itemId)
  } else {
    selectedItems.value.add(itemId)
  }
  updateSelectAllState()
}

const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selectedItems.value.clear()
  } else {
    cartStore.items.forEach(item => {
      selectedItems.value.add(item.chiTietSanPhamId)
    })
  }
  isAllSelected.value = !isAllSelected.value
}

const updateSelectAllState = () => {
  isAllSelected.value = selectedItems.value.size === cartStore.items.length && cartStore.items.length > 0
}

// Tính tổng giá trị của các sản phẩm đã chọn
const getSelectedItemsTotal = () => {
  return cartStore.items
    .filter(item => selectedItems.value.has(item.chiTietSanPhamId))
    .reduce((total, item) => total + (item.gia * item.quantity), 0)
}

const getSelectedItemsCount = () => {
  return selectedItems.value.size
}

const proceedToCheckout = () => {
  if (selectedItems.value.size === 0) {
    showToast('warning', 'Vui lòng chọn ít nhất một sản phẩm để thanh toán!', 'bi-exclamation-triangle-fill')
    return
  }

  // Lọc chỉ những sản phẩm đã chọn
  const selectedProducts = cartStore.items.filter(item =>
    selectedItems.value.has(item.chiTietSanPhamId)
  )

  // Lưu sản phẩm đã chọn vào sessionStorage để trang đặt hàng có thể truy cập
  sessionStorage.setItem('selectedItems', JSON.stringify(selectedProducts))

  // Chuyển đến trang đặt hàng
  router.push('/checkout')
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

// 🔽 THÊM MỚI: Phần "Có thể bạn quan tâm"
const recommendedProducts = ref([])

const getRecommendedProducts = async () => {
  try {
    const res = await axios.get(`${API_BASE_URL}/api/san-pham-pos`)
    console.log('Recommended products data:', res.data)
    recommendedProducts.value = res.data
  } catch (error) {
    console.error('Lỗi khi tải sản phẩm gợi ý:', error)
  }
}

const viewProduct = (product) => {
  router.push(`/product/${product.id}`)
}

// Logic xử lý scroll
const scrollLeft = () => {
  if (scrollContainer.value) {
    const cardWidth = 300 // 280px + 20px gap
    const newPosition = Math.max(0, scrollPosition.value - cardWidth)
    scrollContainer.value.scrollTo({
      left: newPosition,
      behavior: 'smooth'
    })
    scrollPosition.value = newPosition
    updateScrollState()
  }
}

const scrollRight = () => {
  if (scrollContainer.value) {
    const cardWidth = 300 // 280px + 20px gap
    const maxScroll = scrollContainer.value.scrollWidth - scrollContainer.value.clientWidth
    const newPosition = Math.min(maxScroll, scrollPosition.value + cardWidth)
    scrollContainer.value.scrollTo({
      left: newPosition,
      behavior: 'smooth'
    })
    scrollPosition.value = newPosition
    updateScrollState()
  }
}

const updateScrollState = () => {
  if (scrollContainer.value) {
    const maxScroll = scrollContainer.value.scrollWidth - scrollContainer.value.clientWidth
    isScrollAtEnd.value = scrollPosition.value >= maxScroll
  }
}

const handleScroll = () => {
  if (scrollContainer.value) {
    scrollPosition.value = scrollContainer.value.scrollLeft
    updateScrollState()
  }
}

onMounted(() => {
  getRecommendedProducts()
  // Thêm event listener cho scroll
  if (scrollContainer.value) {
    scrollContainer.value.addEventListener('scroll', handleScroll)
  }
})
</script>


<style scoped>
:root {
  --phoenix-primary: #FF6B35;
  --phoenix-secondary: #F7931E;
  --phoenix-accent: #DC143C;
  --phoenix-gold: #FFD700;
  --phoenix-dark: #2C1810;
}

.cart-page {
  min-height: 100vh;
  background: linear-gradient(to bottom, #f8f9fa 0%, #ffffff 100%);
  display: flex;
  flex-direction: column;
}

/* Header */
.cart-header {
  background: white;
  padding: 1.5rem 0;
}

.header-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 2rem;
  display: flex;
  align-items: center;
  gap: 2rem;
}

.logo-img {
  height: 50px;
  width: auto;
  filter: drop-shadow(2px 2px 4px rgba(0, 0, 0, 0.2));
}

.page-title {
  color: white;
  font-size: 1.8rem;
  font-weight: 700;
}

/* Main Content */
.cart-main {
  flex: 1;
  padding: 2rem 0 3rem 0;
  margin-top: 100px; /* Đảm bảo không bị chồm vào header */
}

.container {
  max-width: 1400px !important;
  margin: 0 auto !important;
  padding: 0 2rem !important;
}

/* Page Header */
.page-header {
  margin-bottom: 2.5rem;
  padding-top: 1.5rem; /* Thêm padding-top để tạo khoảng cách */
  padding-bottom: 1.5rem;
  border-bottom: 2px solid #e9ecef;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 0.5rem 0;
  background: linear-gradient(135deg, #FF5500 0%, #DC143C 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-subtitle {
  font-size: 1rem;
  color: #6c757d;
  margin: 0;
  font-weight: 500;
}

/* Empty Cart */
.empty-cart {
  text-align: center;
  padding: 5rem 2rem;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 20px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
  border: 1px solid #e9ecef;
  max-width: 600px;
  margin: 0 auto;
}

.empty-cart i {
  font-size: 6rem;
  color: #dee2e6;
  margin-bottom: 1.5rem;
  display: inline-block;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
}

.empty-cart h2 {
  font-size: 2rem;
  color: #2c3e50;
  margin-bottom: 0.75rem;
  font-weight: 700;
}

.empty-cart p {
  color: #6c757d;
  margin-bottom: 2.5rem;
  font-size: 1.1rem;
}

.btn-shop {
  display: inline-flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1.125rem 2.5rem;
  background: linear-gradient(135deg, #FF5500 0%, #DC143C 100%);
  color: white;
  text-decoration: none;
  border-radius: 50px;
  font-weight: 600;
  font-size: 1.1rem;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(255, 85, 0, 0.3);
  border: none;
  cursor: pointer;
}

.btn-shop:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(255, 85, 0, 0.4);
  background: linear-gradient(135deg, #DC143C 0%, #FF5500 100%);
}

/* Cart Layout */
.cart-layout {
  display: grid;
  grid-template-columns: 1fr 420px;
  gap: 2.5rem;
  align-items: start;
}

.section-title {
  font-size: 1.75rem;
  font-weight: 700;
  margin-bottom: 1.5rem;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.section-title::before {
  content: '';
  width: 4px;
  height: 2rem;
  background: linear-gradient(135deg, #FF5500 0%, #DC143C 100%);
  border-radius: 2px;
}

/* Cart Items */
.cart-items {
  background: white;
  padding: 2.5rem;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #e9ecef;
}

/* Cart Header */
.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  background: transparent;

}

.select-all-container {
  display: flex;
  align-items: center;
}

.select-all-checkbox {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  font-weight: 600;
  color: #333;
}

.select-all-checkbox input[type="checkbox"] {
  display: none;
}

.select-all-checkbox .checkmark {
  width: 20px;
  height: 20px;
  border: 2px solid #ddd;
  border-radius: 4px;
  position: relative;
  transition: all 0.3s;
}

.select-all-checkbox input[type="checkbox"]:checked + .checkmark {
  background: linear-gradient(135deg, #FF5500 0%, #DC143C 100%);
  border-color: #FF5500;
}

.select-all-checkbox input[type="checkbox"]:checked + .checkmark::after {
  content: '';
  position: absolute;
  left: 6px;
  top: 2px;
  width: 6px;
  height: 10px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.select-all-text {
  font-size: 1rem;
  user-select: none;
}

.cart-item {
  display: grid;
  grid-template-columns: 30px 120px 1fr auto auto;
  gap: 1.5rem;
  padding: 1.75rem;
  border: 1px solid #e9ecef;
  border-radius: 16px;
  margin-bottom: 1.25rem;
  transition: all 0.3s ease;
  align-items: center;
  background: #ffffff;
  position: relative;
  overflow: hidden;
}

.cart-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(135deg, #FF5500 0%, #DC143C 100%);
  transform: scaleY(0);
  transition: transform 0.3s ease;
}

.cart-item:hover::before {
  transform: scaleY(1);
}

/* Item Checkbox */
.item-checkbox {
  display: flex;
  align-items: center;
  justify-content: center;
}

.item-checkbox-label {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.item-checkbox-label input[type="checkbox"] {
  display: none;
}

.item-checkbox-label .checkmark {
  width: 18px;
  height: 18px;
  border: 2px solid #ddd;
  border-radius: 4px;
  position: relative;
  transition: all 0.3s;
}

.item-checkbox-label input[type="checkbox"]:checked + .checkmark {
  background: linear-gradient(135deg, #FF5500 0%, #DC143C 100%);
  border-color: #FF5500;
}

.item-checkbox-label input[type="checkbox"]:checked + .checkmark::after {
  content: '';
  position: absolute;
  left: 5px;
  top: 2px;
  width: 5px;
  height: 8px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.cart-item:hover {
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
  border-color: #FF5500;
}

.item-image {
  position: relative;
  overflow: hidden;
  border-radius: 12px;
  background: #f8f9fa;
}

.item-image img {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 12px;
  transition: transform 0.3s ease;
}

.cart-item:hover .item-image img {
  transform: scale(1.05);
}

.item-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.item-name {
  font-size: 1.15rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.75rem;
  line-height: 1.4;
}

.item-specs {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.item-specs span {
  font-size: 0.875rem;
  padding: 0.375rem 0.75rem;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 8px;
  color: #495057;
  font-weight: 500;
  border: 1px solid #dee2e6;
}

.item-price {
  font-size: 1.25rem;
  font-weight: 700;
  color: #DC143C;
  background: linear-gradient(135deg, #DC143C 0%, #FF5500 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.item-quantity {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.qty-btn {
  width: 40px;
  height: 40px;
  border: 2px solid #dee2e6;
  background: white;
  border-radius: 10px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  font-size: 1.1rem;
  color: #495057;
  font-weight: 600;
}

.qty-btn:hover {
  background: linear-gradient(135deg, #FF5500 0%, #DC143C 100%);
  color: white;
  border-color: #FF5500;
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(255, 85, 0, 0.3);
}

.qty-value {
  min-width: 40px;
  text-align: center;
  font-weight: 600;
  font-size: 1.1rem;
}

.item-total {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: space-between;
}

.total-price {
  font-size: 1.5rem;
  font-weight: 700;
  color: #DC143C;
  background: linear-gradient(135deg, #DC143C 0%, #FF5500 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 0.75rem;
}

.btn-remove {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.625rem 1.25rem;
  background: transparent;
  color: #dc3545;
  border: 2px solid #dc3545;
  border-radius: 10px;
  cursor: pointer;
  font-size: 0.95rem;
  font-weight: 600;
  transition: all 0.3s ease;
}

.btn-remove:hover {
  background: linear-gradient(135deg, #dc3545 0%, #c82333 100%);
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(220, 53, 69, 0.3);
}

/* Cart Summary */
.cart-summary {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  padding: 2.5rem;
  border-radius: 20px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  height: fit-content;
  position: sticky;
  top: 2rem;
  border: 1px solid #e9ecef;
}

.summary-title {
  font-size: 1.5rem;
  font-weight: 700;
  margin-bottom: 2rem;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #e9ecef;
}

.summary-title::before {
  content: '';
  width: 4px;
  height: 1.5rem;
  background: linear-gradient(135deg, #FF5500 0%, #DC143C 100%);
  border-radius: 2px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.25rem;
  color: #6c757d;
  font-size: 1.05rem;
  padding: 0.75rem 0;
}

.summary-value {
  font-weight: 600;
  color: #2c3e50;
  font-size: 1.1rem;
}

.summary-value.free {
  color: #28a745;
  font-weight: 700;
}

.summary-divider {
  height: 2px;
  background: linear-gradient(90deg, transparent 0%, #e9ecef 50%, transparent 100%);
  margin: 2rem 0;
  border: none;
}

.summary-row.total {
  font-size: 1.3rem;
  font-weight: 700;
  color: #2c3e50;
  padding: 1.25rem 0;
  margin-top: 0.5rem;
  border-top: 2px solid #e9ecef;
  border-bottom: 2px solid #e9ecef;
}

.summary-total {
  font-size: 1.75rem;
  background: linear-gradient(135deg, #DC143C 0%, #FF5500 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-weight: 800;
}

.btn-checkout {
  width: 100%;
  padding: 1.25rem;
  background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
  color: white;
  border: none;
  border-radius: 15px;
  font-weight: 700;
  font-size: 1.15rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  margin-top: 2rem;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(40, 167, 69, 0.3);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.btn-checkout:hover:not(:disabled) {
  background: linear-gradient(135deg, #20c997 0%, #28a745 100%);
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(40, 167, 69, 0.4);
}

.btn-checkout:disabled {
  background: #dee2e6;
  cursor: not-allowed;
  opacity: 0.6;
  box-shadow: none;
  transform: none;
}

.btn-continue {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  padding: 1rem;
  margin-top: 1rem;
  background: linear-gradient(135deg, #FF5500 0%, #DC143C 100%);
  color: #FFF;
  border: none;
  border-radius: 15px;
  font-weight: 600;
  font-size: 1.05rem;
  text-decoration: none;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(255, 85, 0, 0.3);
}

.btn-continue:hover {
  background: linear-gradient(135deg, #DC143C 0%, #FF5500 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 85, 0, 0.4);
  color: white;
}

.promo-section {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid #e0e0e0;
}

.promo-text {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  color: #28a745;
  font-weight: 600;
}

/* Related Products Section */
.related-products {
  background: #f8f9fa !important;
  padding: 3rem 0 !important;
  margin-top: 3rem !important;
}

.related-container {
  max-width: 1200px !important;
  margin: 0 auto !important;
  padding: 0 2rem !important;
}

.related-title {
  text-align: center;
  font-size: 2rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 2rem;
}

.related-wrapper {
  width: 100%;
  max-width: 100%;
  position: relative;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.scroll-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: white;
  border: 2px solid #FF5500;
  color: #FF5500;
  font-size: 1.5rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  z-index: 10;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.scroll-btn:hover:not(:disabled) {
  background: #FF5500;
  color: white;
  transform: translateY(-50%) scale(1.1);
}

.scroll-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.scroll-left {
  left: -25px;
}

.scroll-right {
  right: -25px;
}

.related-scroll-container {
  overflow-x: auto;
  overflow-y: hidden;
  padding: 1rem 0;
  scroll-behavior: smooth;
  flex: 1;
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
}

.related-scroll-container::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Opera */
}

.related-grid {
  display: flex;
  gap: 1.5rem;
  min-width: max-content;
}

.related-card {
  background: white;
  border-radius: 15px;
  padding: 1.5rem;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  text-align: center;
  flex: 0 0 280px;
  width: 280px;
  min-width: 280px;
  max-width: 280px;
  height: 400px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.related-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.related-image {
  width: 100%;
  height: 180px;
  object-fit: cover;
  border-radius: 10px;
  background: #f9f9f9;
  transition: transform 0.3s ease;
  flex-shrink: 0;
}

.related-card:hover .related-image {
  transform: scale(1.05);
}

.related-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin: 0.5rem 0;
  flex-shrink: 0;
}

.related-price {
  font-size: 1.2rem;
  font-weight: 700;
  color: #DC143C;
  margin: 0.5rem 0;
  flex-shrink: 0;
}

.related-btn {
  width: 100%;
  padding: 0.75rem 1rem;
  background: #FF5500;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  flex-shrink: 0;
  margin-top: auto;
}

.related-btn:hover {
  background: #DC143C;
  transform: translateY(-2px);
}

/* Footer */
.cart-footer {
  background: #000000;
  color: white;
  text-align: center;
  padding: 2rem;
  margin-top: auto;
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

.toast-notification.warning {
  background: #ffc107;
  color: #333;
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

/* Responsive */
@media (max-width: 992px) {
  .cart-layout {
    grid-template-columns: 1fr !important;
  }

  .cart-summary {
    position: static !important;
  }

  .container {
    padding: 0 1rem !important;
  }

  .related-container {
    padding: 0 1rem !important;
  }

  .cart-item {
    grid-template-columns: 30px 80px 1fr;
    gap: 1rem;
  }

  .item-quantity {
    grid-column: 1 / -1;
    justify-content: center;
  }

  .item-total {
    grid-column: 1 / -1;
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
  }
}

@media (max-width: 480px) {
  .header-container {
    flex-direction: column;
    text-align: center;
  }

  .logo-img {
    height: 40px;
  }

  .page-title {
    font-size: 1.3rem;
  }

  .cart-item {
    grid-template-columns: 25px 60px 1fr;
  }

  .item-image img {
    width: 60px;
    height: 60px;
  }

  .cart-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .select-all-container {
    align-self: flex-end;
  }

  .related-card {
    flex: 0 0 250px;
    width: 250px;
    min-width: 250px;
    max-width: 250px;
    height: 380px;
    padding: 1rem;
  }

  .related-title {
    font-size: 1.5rem;
  }

  .related-image {
    height: 160px;
  }

  .scroll-btn {
    width: 40px;
    height: 40px;
    font-size: 1.2rem;
  }

  .scroll-left {
    left: -20px;
  }

  .scroll-right {
    right: -20px;
  }

  .related-grid {
    gap: 1rem;
  }
}

@media (max-width: 480px) {
  .container {
    padding: 0 0.5rem !important;
  }

  .related-container {
    padding: 0 0.5rem !important;
  }

  .related-card {
    flex: 0 0 220px;
    width: 220px;
    min-width: 220px;
    max-width: 220px;
    height: 360px;
    padding: 0.75rem;
  }

  .related-image {
    height: 120px;
  }

  .related-title {
    font-size: 1.2rem;
  }

  .related-name {
    font-size: 1rem;
  }

  .related-price {
    font-size: 1.1rem;
  }

  .scroll-btn {
    width: 35px;
    height: 35px;
    font-size: 1rem;
  }

  .scroll-left {
    left: -15px;
  }

  .scroll-right {
    right: -15px;
  }
}
</style>

