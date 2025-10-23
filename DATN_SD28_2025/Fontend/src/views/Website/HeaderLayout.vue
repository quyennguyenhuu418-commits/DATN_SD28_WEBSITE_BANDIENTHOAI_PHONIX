<template>
  <header class="main-header" :class="{ 'header-hidden': !isHeaderVisible }">
    <div class="header-container">
      <!-- Logo -->
      <div class="logo-section">
        <router-link to="/">
          <img src="/Logo2.png" alt="PhoniX" class="logo-img" />
        </router-link>
      </div>

      <!-- Search Bar -->
      <div class="search-section">
        <input
          v-model="searchQuery"
          @input="handleSearch"
          @keyup.enter="performSearch"
          type="text"
          placeholder="Tìm kiếm sản phẩm..."
          class="search-input"
        />
        <i class="bi bi-search search-icon" @click="performSearch"></i>
      </div>

      <!-- Header Actions -->
      <div class="header-actions">
        <!-- Location -->
        <div class="action-item location">
          <i class="bi bi-geo-alt-fill"></i>
          <div class="action-text">
            <span class="label">Khu vực</span>
            <strong>Hà Nội</strong>
          </div>
        </div>

        <!-- Cart -->
        <router-link to="/cart" class="action-item cart">
          <div class="cart-icon-wrapper">
            <i class="bi bi-cart3"></i>
            <span v-if="cartStore.itemCount > 0" class="cart-badge">{{ cartStore.itemCount }}</span>
          </div>
          <span class="action-text">Giỏ hàng</span>
        </router-link>

        <!-- User -->
        <div class="action-item user" @click="showLogin = true">
          <i class="bi bi-person-circle"></i>
          <span class="action-text">Đăng nhập</span>
        </div>
      </div>
    </div>

    <!-- Navigation Menu -->
    <nav class="nav-menu">
      <ul class="menu-list">
        <li><router-link to="/" class="menu-link" :class="{ active: $route.path === '/' }">Trang chủ</router-link></li>
        <li><router-link to="/shop" class="menu-link" :class="{ active: $route.path === '/shop' }">Sản phẩm</router-link></li>
        <li><a href="#brands" class="menu-link">Hãng</a></li>
        <li><a href="#news" class="menu-link">Tin tức</a></li>
        <li><a href="#contact" class="menu-link">Liên hệ</a></li>
      </ul>
    </nav>
  </header>

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
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cartStore'

const router = useRouter()
const cartStore = useCartStore()

// State
const searchQuery = ref('')
const showLogin = ref(false)
const isHeaderVisible = ref(true)
const lastScrollY = ref(0)

// Methods
const handleSearch = () => {
  // Debounce search if needed
}

const performSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({ path: '/shop', query: { q: searchQuery.value } })
  }
}

// Scroll listener
const handleScroll = () => {
  const currentScrollY = window.scrollY

  // Header hide/show logic
  if (currentScrollY > lastScrollY.value && currentScrollY > 100) {
    // Scrolling down and past 100px - hide header
    isHeaderVisible.value = false
  } else {
    // Scrolling up or at top - show header
    isHeaderVisible.value = true
  }

  lastScrollY.value = currentScrollY
}

// Lifecycle
onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
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

/* Header */
.main-header {
  background: #FF5500;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  transition: transform 0.3s ease-in-out;
}

.main-header.header-hidden {
  transform: translateY(-100%);
}

.header-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 1rem 2rem;
  display: flex;
  align-items: center;
  gap: 2rem;
  height: 77px;
}

.logo-section .logo-img {
  height: 60px;
  width: auto;
  object-fit: contain;
  filter: drop-shadow(2px 2px 4px rgba(0, 0, 0, 0.2));
}

.search-section {
  flex: 1;
  position: relative;
}

.search-input {
  width: 100%;
  padding: 0.875rem 3rem 0.875rem 1.25rem;
  border: none;
  border-radius: 50px;
  font-size: 0.95rem;
  outline: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.search-icon {
  position: absolute;
  right: 1.25rem;
  top: 50%;
  transform: translateY(-50%);
  color: var(--phoenix-primary);
  font-size: 1.25rem;
  cursor: pointer;
}

.header-actions {
  display: flex;
  gap: 1.5rem;
  align-items: center;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: white;
  text-decoration: none;
  cursor: pointer;
  transition: transform 0.2s;
}

.action-item:hover {
  transform: translateY(-2px);
}

.action-item i {
  font-size: 1.5rem;
}

.action-text {
  font-size: 0.9rem;
}

.action-text .label {
  display: block;
  font-size: 0.75rem;
  opacity: 0.9;
}

.cart-icon-wrapper {
  position: relative;
}

.cart-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background: red;             /* Màu nền đỏ */
  color: white;                /* Màu chữ trắng */
  font-size: 0.75rem;
  font-weight: bold;
  width: 20px;                 /* Chiều rộng cố định */
  height: 20px;                /* Chiều cao cố định */
  border-radius: 50%;          /* Bo tròn hoàn toàn */
  display: flex;               /* Dễ căn giữa */
  align-items: center;
  justify-content: center;
  box-shadow: 0 0 0 2px white; /* Viền trắng mỏng cho đẹp (tùy chọn) */
}


/* Navigation Menu */
.nav-menu {
  background: rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
}

.menu-list {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0.75rem 2rem;
  display: flex;
  gap: 2rem;
  list-style: none;
  justify-content: center;
}

.menu-link {
  color: white;
  text-decoration: none;
  font-weight: 500;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  transition: all 0.3s;
}

.menu-link:hover,
.menu-link.active {
  background: rgba(255, 255, 255, 0.2);
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

/* Responsive */
@media (max-width: 992px) {
  .header-container {
    flex-wrap: wrap;
    gap: 1rem;
  }

  .search-section {
    order: 3;
    width: 100%;
  }
}

@media (max-width: 768px) {
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
}

@media (max-width: 480px) {
  .logo-section .logo-img {
    height: 45px;
  }

  .header-container {
    height: 62px;
  }
}
</style>

