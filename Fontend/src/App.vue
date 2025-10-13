<template>
  <div id="app" class="h-screen bg-gradient-to-br from-slate-50 to-blue-50">
    <!-- Customer Layout (no sidebar) -->
    <div v-if="isCustomerRoute" class="customer-layout">
      <router-view />
    </div>
    
    <!-- Admin Layout (with sidebar) -->
    <div v-else class="flex h-full">
      <!-- Modern Sidebar -->
      <aside
        class="sidebar-container transition-all duration-300 ease-in-out"
        :class="{ 'sidebar-collapsed': sidebarCollapsed }"
      >
        <!-- Sidebar Header with Logo and Toggle -->
        <div class="sidebar-header">
          <div class="flex items-center justify-center w-full relative">
            <!-- Logo -->
            <img 
              v-if="!sidebarCollapsed"
              src="@/assets/ChatGPT Image 16_53_01 28 thg 9, 2025 1.png" 
              alt="Logo" 
              class="logo-image"
            />
            
            <!-- Toggle Button -->
            <button
              class="toggle-btn absolute right-0"
              @click="toggleSidebar"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  :d="sidebarCollapsed ? 'M13 5l7 7-7 7M5 5l7 7-7 7' : 'M11 19l-7-7 7-7M19 19l-7-7 7-7'"
                />
              </svg>
            </button>
          </div>
        </div>

        <!-- Navigation Menu -->
        <nav class="navigation-menu">
          <!-- Dashboard -->
          <router-link to="/dashboard" class="nav-item" :class="{ 'nav-item-collapsed': sidebarCollapsed }">
            <div class="nav-icon">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                      d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
              </svg>
            </div>
            <span v-if="!sidebarCollapsed" class="nav-text">Dashboard</span>
          </router-link>

          <!-- Thống kê -->
          <router-link to="/thong-ke" class="nav-item" :class="{ 'nav-item-collapsed': sidebarCollapsed }">
            <div class="nav-icon">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                      d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
            </svg>
            </div>
            <span v-if="!sidebarCollapsed" class="nav-text">Thống kê</span>
          </router-link>

          <!-- Divider -->
          <div v-if="!sidebarCollapsed" class="nav-divider">
            <span class="nav-section-title">Quản lý bán hàng</span>
          </div>

          <!-- Sản phẩm -->
          <router-link to="/san-pham" class="nav-item" :class="{ 'nav-item-collapsed': sidebarCollapsed }">
            <div class="nav-icon">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                      d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
              </svg>
            </div>
            <span v-if="!sidebarCollapsed" class="nav-text">Sản phẩm</span>
          </router-link>

          <!-- Khách hàng -->
          <router-link to="/khach-hang" class="nav-item" :class="{ 'nav-item-collapsed': sidebarCollapsed }">
            <div class="nav-icon">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                      d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z" />
              </svg>
            </div>
            <span v-if="!sidebarCollapsed" class="nav-text">Khách hàng</span>
          </router-link>

          <!-- Hóa đơn -->
          <router-link to="/hoa-don" class="nav-item" :class="{ 'nav-item-collapsed': sidebarCollapsed }">
            <div class="nav-icon">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                      d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
            </div>
            <span v-if="!sidebarCollapsed" class="nav-text">Hóa đơn</span>
          </router-link>

          <!-- Voucher -->
          <router-link to="/voucher" class="nav-item" :class="{ 'nav-item-collapsed': sidebarCollapsed }">
            <div class="nav-icon">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                      d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
            </svg>
            </div>
            <span v-if="!sidebarCollapsed" class="nav-text">Voucher</span>
          </router-link>

          <!-- Voucher Khách hàng -->
          <router-link to="/customer-vouchers" class="nav-item" :class="{ 'nav-item-collapsed': sidebarCollapsed }">
            <div class="nav-icon">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                      d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
            </svg>
            </div>
            <span v-if="!sidebarCollapsed" class="nav-text">Voucher KH</span>
          </router-link>

          <!-- Thuộc Tính with Submenu -->
          <div class="nav-dropdown">
            <button
              @click="toggleThuocTinh"
              class="nav-item nav-dropdown-toggle"
              :class="{ 'nav-item-collapsed': sidebarCollapsed }"
            >
              <div class="nav-icon">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                        d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
              </svg>
              </div>
              <span v-if="!sidebarCollapsed" class="nav-text">Thuộc Tính</span>
              <div v-if="!sidebarCollapsed" class="nav-arrow">
                <svg class="w-4 h-4 transition-transform" :class="{ 'rotate-180': showThuocTinh }" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/>
              </svg>
              </div>
            </button>
            
            <!-- Submenu -->
            <div v-if="showThuocTinh && !sidebarCollapsed" class="nav-submenu">
              <router-link to="/danh-muc" class="nav-submenu-item">
                <span>Danh mục</span>
              </router-link>
              <router-link to="/hang" class="nav-submenu-item">
                <span>Hãng</span>
              </router-link>
              <router-link to="/admin-reviews" class="nav-submenu-item">
                <span>Quản lý đánh giá</span>
              </router-link>
              <router-link to="/he-dieu-hanh" class="nav-submenu-item">
                <span>Hệ điều hành</span>
              </router-link>
              <router-link to="/mau-sac" class="nav-submenu-item">
                <span>Màu sắc</span>
              </router-link>
              <router-link to="/man-hinh" class="nav-submenu-item">
                <span>Màn hình</span>
              </router-link>
              <router-link to="/chip" class="nav-submenu-item">
                <span>Chip</span>
              </router-link>
              <router-link to="/ram" class="nav-submenu-item">
                <span>RAM</span>
              </router-link>
              <router-link to="/rom" class="nav-submenu-item">
                <span>ROM</span>
              </router-link>
              <router-link to="/cpu" class="nav-submenu-item">
                <span>CPU</span>
              </router-link>
              <router-link to="/gpu" class="nav-submenu-item">
                <span>GPU</span>
              </router-link>
              <router-link to="/pin" class="nav-submenu-item">
                <span>Pin</span>
              </router-link>
              <router-link to="/camera-truoc" class="nav-submenu-item">
                <span>Camera trước</span>
              </router-link>
              <router-link to="/camera-sau" class="nav-submenu-item">
                <span>Camera sau</span>
              </router-link>
            </div>
          </div>

          <!-- Giảm Giá -->
          <router-link to="/giam-gia" class="nav-item" :class="{ 'nav-item-collapsed': sidebarCollapsed }">
            <div class="nav-icon">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                      d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
            </svg>
            </div>
            <span v-if="!sidebarCollapsed" class="nav-text">Giảm Giá</span>
          </router-link>

          <!-- Đợt Giảm Giá -->
          <router-link to="/dot-giam-gia" class="nav-item" :class="{ 'nav-item-collapsed': sidebarCollapsed }">
            <div class="nav-icon">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                      d="M9 12l2 2 4-4M7.835 4.697a3.42 3.42 0 001.946-.806 3.42 3.42 0 014.438 0 3.42 3.42 0 001.946.806 3.42 3.42 0 013.138 3.138 3.42 3.42 0 00.806 1.946 3.42 3.42 0 010 4.438 3.42 3.42 0 00-.806 1.946 3.42 3.42 0 01-3.138 3.138 3.42 3.42 0 00-1.946.806 3.42 3.42 0 01-4.438 0 3.42 3.42 0 00-1.946-.806 3.42 3.42 0 01-3.138-3.138 3.42 3.42 0 00-.806-1.946 3.42 3.42 0 010-4.438 3.42 3.42 0 00.806-1.946 3.42 3.42 0 013.138-3.138z" />
              </svg>
            </div>
            <span v-if="!sidebarCollapsed" class="nav-text">Đợt Giảm Giá</span>
          </router-link>

          <!-- Divider -->
          <div v-if="!sidebarCollapsed" class="nav-divider">
            <span class="nav-section-title">Quản lý hệ thống</span>
          </div>

          <!-- Nhân viên -->
          <router-link to="/nhan-vien" class="nav-item" :class="{ 'nav-item-collapsed': sidebarCollapsed }">
            <div class="nav-icon">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                      d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
            </svg>
            </div>
            <span v-if="!sidebarCollapsed" class="nav-text">Nhân viên</span>
          </router-link>

          <!-- Cài đặt -->
          <router-link to="/cai-dat" class="nav-item" :class="{ 'nav-item-collapsed': sidebarCollapsed }">
            <div class="nav-icon">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                      d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
              </svg>
            </div>
            <span v-if="!sidebarCollapsed" class="nav-text">Cài đặt</span>
          </router-link>

          <!-- Divider -->
          <div v-if="!sidebarCollapsed" class="nav-divider">
            <span class="nav-section-title">Báo cáo & Phân tích</span>
          </div>

          <!-- Báo cáo bán hàng -->
          <router-link to="/bao-cao-ban-hang" class="nav-item" :class="{ 'nav-item-collapsed': sidebarCollapsed }">
            <div class="nav-icon">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                      d="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
            </div>
            <span v-if="!sidebarCollapsed" class="nav-text">Báo cáo bán hàng</span>
          </router-link>

          <!-- Báo cáo tồn kho -->
          <router-link to="/bao-cao-ton-kho" class="nav-item" :class="{ 'nav-item-collapsed': sidebarCollapsed }">
            <div class="nav-icon">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                      d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
            </svg>
            </div>
            <span v-if="!sidebarCollapsed" class="nav-text">Báo cáo tồn kho</span>
          </router-link>

          <!-- Phân tích khách hàng -->
          <router-link to="/phan-tich-khach-hang" class="nav-item" :class="{ 'nav-item-collapsed': sidebarCollapsed }">
            <div class="nav-icon">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                      d="M16 8v8m-4-5v5m-4-2v2m-2 4h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            </div>
            <span v-if="!sidebarCollapsed" class="nav-text">Phân tích KH</span>
            </router-link>


        </nav>

        <!-- User Profile Section -->
        <div class="user-profile-section">
          <div class="user-profile" :class="{ 'user-profile-collapsed': sidebarCollapsed }">
            <div class="user-avatar">
              <img src="@/assets/avatar.png" alt="User Avatar" class="w-8 h-8 rounded-full">
            </div>
            <div v-if="!sidebarCollapsed" class="user-info">
              <div class="user-name">Admin User</div>
              <div class="user-role">Quản trị viên</div>
              </div>
            <div v-if="!sidebarCollapsed" class="user-actions">
              <button class="user-action-btn">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                        d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                </svg>
              </button>
            </div>
          </div>
        </div>
      </aside>

      <!-- Main Content -->
      <main class="flex-1 overflow-y-auto h-full">
          <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
const sidebarCollapsed = ref(false)
const showThuocTinh = ref(false)

function toggleSidebar() {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

function toggleThuocTinh() {
  showThuocTinh.value = !showThuocTinh.value
}

const route = useRoute()
const currentPathName = computed(() => route.name || route.path)

// Check if current route is customer route (no admin sidebar)
const isCustomerRoute = computed(() => {
  const customerRoutes = ['product-detail']
  return customerRoutes.includes(route.name as string)
})

const globalSearch = ref('')
function doGlobalSearch() {
  // hook tìm kiếm toàn cục, có thể emit event hoặc điều hướng sang trang có ô filter keyword
  console.log('Global search:', globalSearch.value)
}
</script>

<style scoped>
/* CSS Variables for Light/Dark Mode */
:root {
  /* Light Mode Colors */
  --bg-primary: #f5f7fa;
  --bg-secondary: #ffffff;
  --bg-sidebar: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  --text-primary: #1f2937;
  --text-secondary: #6b7280;
  --text-sidebar: rgba(255, 255, 255, 0.8);
  --border-color: #e5e7eb;
  --shadow-light: rgba(0, 0, 0, 0.1);
  --topbar-bg: rgba(255, 255, 255, 0.9);
}


/* Modern App Styles */
html, body {
  height: 100%;
  margin: 0;
  padding: 0;
  overflow: hidden;
}

#app {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background: var(--bg-primary);
  color: var(--text-primary);
  transition: all 0.3s ease;
  height: 100vh;
  display: flex;
  flex-direction: column;
}

/* Sidebar Styles */
.sidebar-container {
  width: 280px;
  background: white;
  border-right: 1px solid #e2e8f0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  height: 100vh;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
}

.sidebar-collapsed {
  width: 80px;
}

/* Sidebar Header */
.sidebar-header {
  padding: 0.5rem 1rem;
  border-bottom: 1px solid #e2e8f0;
  background: #f8fafc;
  display: flex;
  align-items: center;
  min-height: 60px;
}

/* Logo styling */
.logo-image {
  height: 60px;
  width: 60px;
  object-fit: contain;
  border-radius: 0;
  box-shadow: none;
  background: transparent;
  flex-shrink: 0;
  /* Remove white background and make logo bigger */
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.1));
  /* Use mix-blend-mode to remove white background */
  mix-blend-mode: multiply;
  /* Ensure logo is visible on light background */
  background-color: transparent;
  /* Scale logo bigger while keeping container size */
  transform: scale(3.0);
  transform-origin: center;
  /* Move logo slightly to the left */
  margin-left: -2cm;
}

/* Toggle Button */
.toggle-btn {
  background: linear-gradient(135deg, #fb923c, #f97316);
  border: 1px solid #ea580c;
  color: white;
  padding: 0.5rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.toggle-btn:hover {
  background: linear-gradient(135deg, #ea580c, #dc2626);
  color: white;
  transform: scale(1.05);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

/* Navigation Menu */
.navigation-menu {
  flex: 1;
  padding: 1rem;
  overflow-y: auto;
}

/* Navigation Items */
.nav-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  margin-bottom: 0.5rem;
  border-radius: 12px;
  color: #64748b;
  text-decoration: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.nav-item:hover {
  background: #f1f5f9;
  color: #1e293b;
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.nav-item-collapsed {
  justify-content: center;
  padding: 0.75rem;
}

.nav-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
}

.nav-text {
  font-size: 0.875rem;
  font-weight: 500;
  color: inherit;
}

.nav-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 0;
  height: 70%;
  background: linear-gradient(135deg, #ff6b6b, #feca57);
  border-radius: 0 8px 8px 0;
  transition: width 0.3s ease;
}

.nav-item:hover::before {
  width: 4px;
}

/* Navigation Divider */
.nav-divider {
  margin: 1.5rem 0 1rem 0;
  padding: 0 1rem;
}

.nav-section-title {
  font-size: 0.75rem;
  font-weight: 600;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

/* Navigation Dropdown */
.nav-dropdown {
  position: relative;
}

.nav-dropdown-toggle {
  width: 100%;
  border: none;
  background: none;
  cursor: pointer;
  justify-content: space-between;
}

.nav-arrow {
  display: flex;
  align-items: center;
  margin-left: auto;
}

/* Navigation Submenu */
.nav-submenu {
  margin: 0.5rem 0 0 2rem;
  padding: 0.5rem 0;
  background: #f8fafc;
  border-radius: 8px;
  border-left: 2px solid #e2e8f0;
  animation: slideDown 0.3s ease-out;
}

.nav-submenu-item {
  display: flex;
  align-items: center;
  padding: 0.5rem 1rem;
  margin: 0.125rem 0;
  border-radius: 6px;
  color: #64748b;
  text-decoration: none;
  font-size: 0.8rem;
  transition: all 0.3s ease;
}

.nav-submenu-item:hover {
  background: #e2e8f0;
  color: #1e293b;
  transform: translateX(4px);
}

.nav-submenu-item.router-link-active {
  background: #dbeafe;
  color: #1e40af;
  font-weight: 500;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Active route styling */
.router-link-active {
  background: #dbeafe !important;
  color: #1e40af !important;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.2);
  border: 1px solid #93c5fd;
}

.router-link-active::before {
  width: 4px !important;
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
}

.router-link-active .nav-text {
  color: #1e40af !important;
  font-weight: 600;
}

.router-link-active .nav-icon svg {
  color: #1e40af !important;
}

/* User Profile Section */
.user-profile-section {
  padding: 1rem;
  border-top: 1px solid #e2e8f0;
  margin-top: auto;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem;
  border-radius: 12px;
  background: #f8fafc;
  transition: all 0.3s ease;
}

.user-profile:hover {
  background: #f1f5f9;
}

.user-profile-collapsed {
  justify-content: center;
}

.user-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-avatar img {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 2px solid #e2e8f0;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 0.875rem;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
  line-height: 1.2;
}

.user-role {
  font-size: 0.75rem;
  color: #64748b;
  margin: 0;
  line-height: 1;
}

.user-actions {
  display: flex;
  align-items: center;
}

.user-action-btn {
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.7);
  padding: 0.25rem;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.user-action-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

/* Customer Layout */
.customer-layout {
  width: 100%;
  height: 100vh;
  background: #f8f9fa;
  overflow-y: auto;
}

/* Main Content */
main {
  background: var(--bg-primary);
  min-height: 100vh;
  transition: all 0.3s ease;
  overflow-x: hidden; /* Ẩn thanh trượt ngang */
}

/* Ẩn thanh trượt dọc cho main content */
main::-webkit-scrollbar {
  width: 0px;
  background: transparent;
}

main::-webkit-scrollbar-thumb {
  background: transparent;
}

/* Topbar */
.sticky.top-0.z-30.bg-white.border-b.border-gray-200 {
  background: var(--topbar-bg) !important;
  backdrop-filter: blur(20px);
  border-bottom: 1px solid var(--border-color) !important;
  box-shadow: 0 4px 20px var(--shadow-light);
  transition: all 0.3s ease;
}

/* Topbar Text */
.sticky.top-0.z-30.bg-white.border-b.border-gray-200 h1 {
  color: var(--text-primary) !important;
}


/* Topbar Icons */
.topbar-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.topbar-icon:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

/* Profile Picture */
.profile-picture {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  border: 3px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.profile-picture:hover {
  transform: scale(1.1);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

/* Notification dot */
.notification-dot {
  position: absolute;
  top: -4px;
  right: -4px;
  width: 12px;
  height: 12px;
  background: linear-gradient(135deg, #ff6b6b, #ee5a52);
  border-radius: 50%;
  animation: pulse 2s infinite;
  box-shadow: 0 0 10px rgba(255, 107, 107, 0.5);
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

/* Ẩn scrollbar cho sidebar */
.sidebar-container::-webkit-scrollbar {
  width: 0px;
  background: transparent;
}

.sidebar-container::-webkit-scrollbar-track {
  background: transparent;
}

.sidebar-container::-webkit-scrollbar-thumb {
  background: transparent;
}

.sidebar-container::-webkit-scrollbar-thumb:hover {
  background: transparent;
}

/* Navigation Menu - ẩn overflow */
.navigation-menu {
  flex: 1;
  padding: 1rem;
  overflow-y: auto;
  overflow-x: hidden; /* Ẩn thanh trượt ngang */
}

.navigation-menu::-webkit-scrollbar {
  width: 0px;
  background: transparent;
}

/* Responsive */
@media (max-width: 768px) {
  .sidebar-container {
    transform: translateX(-100%);
    position: fixed;
    z-index: 50;
    height: 100vh;
  }
  
  .sidebar-container.open {
    transform: translateX(0);
  }
  
  main {
    width: 100%;
  }
}

/* Animations */
@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.nav-item {
  animation: slideIn 0.3s ease-out;
}
</style>
          