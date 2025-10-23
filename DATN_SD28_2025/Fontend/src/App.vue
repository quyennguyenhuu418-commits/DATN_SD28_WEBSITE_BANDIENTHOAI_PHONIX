<template>
  <!-- Admin Layout with Sidebar -->
  <div v-if="isAdminApp" id="app">
    <router-view />
  </div>

  <!-- Public Website Layout (HomePage, ShopPage, etc.) -->
  <div v-else-if="isPublicWebsite" id="app">
    <router-view />
  </div>

  <!-- Legacy Layout with Sidebar -->
  <div v-else id="app" class="h-screen bg-gradient-to-br from-slate-50 to-blue-50">
    <div class="flex h-full">
      <!-- Modern Sidebar - Hide on login page -->
      <aside
        v-if="!isLoginPage"
        class="sidebar-container transition-all duration-300 ease-in-out"
        :class="{ 'sidebar-collapsed': sidebarCollapsed }"
      >
        <!-- Sidebar Header with Logo and Toggle -->
        <div class="sidebar-header">
          <div class="flex items-center justify-center w-full relative">
            <!-- Logo -->
            <img
              v-if="!sidebarCollapsed"
              src="/logo.png"
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
          <!-- Trang chủ -->
          <router-link to="/trang-chu" class="nav-item" :class="{ 'nav-item-collapsed': sidebarCollapsed }">
            <div class="nav-icon">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
              </svg>
            </div>
            <span v-if="!sidebarCollapsed" class="nav-text">Trang chủ</span>
          </router-link>

          <!-- Divider -->
          <div v-if="!sidebarCollapsed" class="nav-divider">
            <span class="nav-section-title">Quản lý bán hàng</span>
          </div>

          <!-- Bán hàng -->
          <router-link to="/pos" class="nav-item" :class="{ 'nav-item-collapsed': sidebarCollapsed }">
            <div class="nav-icon">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                      d="M9 7h6m0 10v-3m-3 3h.01M9 17h.01M9 14h.01M12 14h.01M15 11h.01M12 11h.01M9 11h.01M7 21h10a2 2 0 002-2V5a2 2 0 00-2-2H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
              </svg>
            </div>
            <span v-if="!sidebarCollapsed" class="nav-text">Bán hàng</span>
          </router-link>

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

          <!-- Đơn hàng -->
          <router-link to="/hoa-don/detail" class="nav-item" :class="{ 'nav-item-collapsed': sidebarCollapsed }">
            <div class="nav-icon">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />
              </svg>
            </div>
            <span v-if="!sidebarCollapsed" class="nav-text">Đơn hàng</span>
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
              <router-link to="/hang" class="nav-submenu-item">
                <span>Hãng</span>
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

          <!-- Divider -->
          <div v-if="!sidebarCollapsed" class="nav-divider">
            <span class="nav-section-title">Quản lý giảm giá</span>
          </div>

          <!-- Phiếu giảm giá -->
          <router-link to="/phieu-giam-gia" class="nav-item" :class="{ 'nav-item-collapsed': sidebarCollapsed }">
            <div class="nav-icon">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                      d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
            </svg>
            </div>
            <span v-if="!sidebarCollapsed" class="nav-text">Phiếu giảm giá</span>
          </router-link>

          <!-- Đợt Giảm Giá -->
          <router-link to="/dot-giam-gia" class="nav-item" :class="{ 'nav-item-collapsed': sidebarCollapsed }">
            <div class="nav-icon">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M9 12l2 2 4-4M7.835 4.697a3.42 3.42 0 001.946-.806 3.42 3.42 0 014.438 0 3.42 3.42 0 001.946.806 3.42 3.42 0 013.138 3.138 3.42 3.42 0 00.806 1.946 3.42 3.42 0 010 4.438 3.42 3.42 0 00-.806 1.946 3.42 3.42 0 01-3.138 3.138 3.42 3.42 0 00-1.946.806 3.42 3.42 0 01-4.438 0 3.42 3.42 0 00-1.946-.806 3.42 3.42 0 01-3.138-3.138 3.42 3.42 0 00-.806-1.946 3.42 3.42 0 010-4.438 3.42 3.42 0 00.806-1.946 3.42 3.42 0 013.138-3.138z" />
              </svg>
            </div>
            <span v-if="!sidebarCollapsed" class="nav-text">Đợt giảm giá</span>
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

          <!-- Messenger Settings -->
          <router-link to="/messenger-settings" class="nav-item" :class="{ 'nav-item-collapsed': sidebarCollapsed }">
            <div class="nav-icon">
              <i class="fab fa-facebook-messenger"></i>
            </div>
            <span v-if="!sidebarCollapsed" class="nav-text">Messenger</span>
          </router-link>

          <!-- Staff Chat Dashboard -->
          <router-link to="/staff-chat" class="nav-item" :class="{ 'nav-item-collapsed': sidebarCollapsed }">
            <div class="nav-icon">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                      d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
              </svg>
            </div>
            <span v-if="!sidebarCollapsed" class="nav-text">Chat Hỗ Trợ</span>
          </router-link>


          <!-- Divider -->
          <div v-if="!sidebarCollapsed" class="nav-divider">
            <span class="nav-section-title">Báo cáo & Phân tích</span>
          </div>

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


          <!-- Quản lý ca làm việc -->
          <router-link to="/ca" class="nav-item" :class="{ 'nav-item-collapsed': sidebarCollapsed }">
            <div class="nav-icon">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                      d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <span v-if="!sidebarCollapsed" class="nav-text">Ca làm việc</span>
          </router-link>

          <!-- Phân ca -->
          <router-link to="/phan-ca" class="nav-item" :class="{ 'nav-item-collapsed': sidebarCollapsed }">
            <div class="nav-icon">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                      d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            </div>
            <span v-if="!sidebarCollapsed" class="nav-text">Phân ca</span>
          </router-link>

          <!-- Giao ca -->
          <router-link to="/giao-ca" class="nav-item" :class="{ 'nav-item-collapsed': sidebarCollapsed }">
            <div class="nav-icon">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                      d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" />
              </svg>
            </div>
            <span v-if="!sidebarCollapsed" class="nav-text">Giao ca</span>
          </router-link>

          <!-- Divider -->
          <div v-if="!sidebarCollapsed" class="nav-divider">
            <span class="nav-section-title">HỖ TRỢ & QUẢN LÝ</span>
          </div>

          <!-- Hỗ trợ -->
          <router-link to="/staff-chat" class="nav-item" :class="{ 'nav-item-collapsed': sidebarCollapsed }">
            <div class="nav-icon">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                      d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
              </svg>
            </div>
            <span v-if="!sidebarCollapsed" class="nav-text">Hỗ trợ</span>
          </router-link>

          <!-- Divider -->
          <div v-if="!sidebarCollapsed" class="nav-divider">
            <span class="nav-section-title">Demo & Test</span>
          </div>

          <!-- Public Website -->
          <a href="/" target="_blank" class="nav-item" :class="{ 'nav-item-collapsed': sidebarCollapsed }">
            <div class="nav-icon">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
              </svg>
            </div>
            <span v-if="!sidebarCollapsed" class="nav-text">Website công khai</span>
          </a>

          <!-- Dark Mode Demo -->
          <router-link to="/dark-mode-demo" class="nav-item" :class="{ 'nav-item-collapsed': sidebarCollapsed }">
            <div class="nav-icon">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" />
              </svg>
            </div>
            <span v-if="!sidebarCollapsed" class="nav-text">Dark Mode Demo</span>
          </router-link>

        </nav>

        <!-- User Profile Section - Removed UserMenu -->
      </aside>

      <!-- Main Content -->
      <main 
        class="flex-1 overflow-y-auto h-full"
        :class="{ 'full-width': isLoginPage }"
      >
          <router-view />
      </main>
    </div>
    
    <!-- Simple Chat Widget -->
    <SimpleChatWidget />
    
    <!-- Ultra Chat Box (AI Chatbot) -->
    <UltraChatBox />
    
    <!-- Facebook Messenger Manager - TẠM THỜI ẨN -->
    <!-- <MessengerManager 
      :pageId="'796591066879645'"
      :showControls="false"
      :defaultEnabled="false"
    /> -->
    
    <!-- Shift Handover Modal -->
    <ShiftHandoverModal
      :isOpen="showShiftHandoverModal"
      @close="closeShiftHandoverModal"
      @confirm="handleShiftHandoverConfirmed"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { useDarkMode } from '@/composables/useDarkMode.js'
import api from '@/services/api'
import SimpleChatWidget from '@/components/SimpleChatWidget.vue'
import UltraChatBox from '@/components/UltraChatBox.vue'
import ShiftHandoverModal from '@/components/ShiftHandoverModal.vue'
import MessengerManager from '@/components/MessengerManager.vue'
// import UserMenu from '@/components/UserMenu.vue' // Removed

const route = useRoute()

const isAdminApp = computed(() => {
  return route.path.startsWith('/admin')
})

// Public website pages (no sidebar)
const isPublicWebsite = computed(() => {
  return route.path === '/' ||
    route.path === '/shop' ||
    route.path === '/cart' ||
    route.path === '/checkout' ||
    route.path === '/dat-hang' ||
    route.path.startsWith('/product')
})

const sidebarCollapsed = ref(false)
const showThuocTinh = ref(false)
const showShiftHandoverModal = ref(false)

// Auth store
const authStore = useAuthStore()
const router = useRouter()

// Dark mode
const { isDarkMode, toggleDarkMode } = useDarkMode()

// Computed properties
const currentRoute = computed(() => route.name)

function toggleSidebar() {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

function toggleThuocTinh() {
  showThuocTinh.value = !showThuocTinh.value
}

// Check for pending handovers on app load
onMounted(async () => {
  // Remove login-active class from body on app mount
  document.body.classList.remove('login-active')
  
  // Wait for auth store to initialize
  await new Promise(resolve => setTimeout(resolve, 200))
  
  // Only check if user is authenticated and not on login page
  if (authStore.isAuthenticated && authStore.user?.id && router.currentRoute.value.path !== '/login') {
    try {
      const response = await api.get(`/api/auth/pending-shift-handover?nhanVienId=${authStore.user.id}`)
      if (response.data && response.data.length > 0) {
        showShiftHandoverModal.value = true
      }
    } catch (error) {
      // Log error but don't show to user - this is a non-critical feature
      console.warn('Could not check pending handovers (backend may be offline):', error.message)
    }
  }
})

const closeShiftHandoverModal = () => {
  showShiftHandoverModal.value = false
}

const handleShiftHandoverConfirmed = (confirmedGiaoCa) => {
  console.log('App.vue: Shift handover confirmed:', confirmedGiaoCa)
  showShiftHandoverModal.value = false
  // Reload the page to refresh all data
  setTimeout(() => {
    window.location.reload()
  }, 500)
}

const route = useRoute()
const currentPathName = computed(() => route.name || route.path)

// Check if current page is login
const isLoginPage = computed(() => {
  return route.path === '/login' || route.name === 'LoginPage' || 
         route.path === '/forgot-password' || route.name === 'forgot-password'
})
const globalSearch = ref('')
function doGlobalSearch() {
  // hook tìm kiếm toàn cục, có thể emit event hoặc điều hướng sang trang có ô filter keyword
  console.log('Global search:', globalSearch.value)
}

// Watch for route changes to manage body class
watch(() => route.path, (newPath) => {
  if (newPath === '/login' || newPath === '/forgot-password') {
    document.body.classList.add('login-active')
  } else {
    document.body.classList.remove('login-active')
  }
}, { immediate: true })
</script>

<!-- Giữ nguyên tất cả CSS hiện tại -->
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

:root.dark {
  /* Dark Mode Colors */
  --bg-primary: #0f172a;
  --bg-secondary: #1e293b;
  --bg-sidebar: linear-gradient(135deg, #1e293b 0%, #334155 100%);
  --text-primary: #f1f5f9;
  --text-secondary: #cbd5e1;
  --text-sidebar: rgba(241, 245, 249, 0.8);
  --border-color: #334155;
  --shadow-light: rgba(0, 0, 0, 0.3);
  --topbar-bg: rgba(30, 41, 59, 0.9);
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
  overflow: hidden; /* Không scroll toàn bộ sidebar */
  position: relative;
  z-index: 1000;
}

.sidebar-collapsed {
  width: 80px;
  overflow: hidden; /* Không scroll toàn bộ sidebar khi collapsed */
}

/* Sidebar Header */
.sidebar-header {
  padding: 0.5rem 1rem;
  border-bottom: 1px solid #e2e8f0;
  background: #f8fafc;
  display: flex;
  align-items: center;
  min-height: 60px;
  flex-shrink: 0; /* Prevent header from shrinking */
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

/* User Profile Section - Removed */

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

/* Main Content */
main {
  background: var(--bg-primary);
  min-height: 100vh;
  transition: all 0.3s ease;
  overflow-x: hidden; /* Ẩn thanh trượt ngang */
  z-index: 1;
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

/* Dark Mode Toggle */
.dark-mode-toggle {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.dark-mode-toggle:hover {
  transform: translateY(-2px) rotate(180deg);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

:root.dark .dark-mode-toggle {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  box-shadow: 0 4px 15px rgba(245, 158, 11, 0.3);
}

:root.dark .dark-mode-toggle:hover {
  box-shadow: 0 8px 25px rgba(245, 158, 11, 0.4);
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

/* Custom scrollbar cho navigation menu */
.navigation-menu::-webkit-scrollbar {
  width: 6px;
  background: transparent;
}

.navigation-menu::-webkit-scrollbar-track {
  background: transparent;
  border-radius: 3px;
}

.navigation-menu::-webkit-scrollbar-thumb {
  background: rgba(156, 163, 175, 0.3);
  border-radius: 3px;
  transition: all 0.2s ease;
}

.navigation-menu::-webkit-scrollbar-thumb:hover {
  background: rgba(156, 163, 175, 0.6);
}

/* Dark mode scrollbar */
:root.dark .navigation-menu::-webkit-scrollbar-thumb {
  background: rgba(107, 114, 128, 0.4);
}

:root.dark .navigation-menu::-webkit-scrollbar-thumb:hover {
  background: rgba(107, 114, 128, 0.7);
}

/* Navigation Menu - scrollable */
.navigation-menu {
  flex: 1;
  padding: 1rem;
  overflow-y: auto; /* Chỉ scroll phần navigation menu */
  overflow-x: visible; /* Cho phép dropdown hiển thị */
  min-height: 0; /* Cho phép flex item shrink */
  max-height: calc(100vh - 80px); /* Trừ đi chiều cao header */
}

/* Responsive */
@media (max-width: 768px) {
  .sidebar-container {
    transform: translateX(-100%);
    position: fixed;
    z-index: 50;
    height: 100vh;
    overflow: hidden; /* Không scroll toàn bộ sidebar */
    -webkit-overflow-scrolling: touch; /* Smooth scrolling on iOS */
  }

  .sidebar-container.open {
    transform: translateX(0);
  }

  main {
    width: 100%;
  }
  
  /* Mobile scrollbar - thinner cho navigation menu */
  .navigation-menu::-webkit-scrollbar {
    width: 4px;
  }
  
  /* Điều chỉnh max-height cho mobile */
  .navigation-menu {
    max-height: calc(100vh - 70px); /* Trừ đi chiều cao header trên mobile */
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

/* Full width for login page */
.full-width {
  width: 100vw !important;
  margin-left: 0 !important;
}

/* Force show sidebar when not on login page */
body:not(.login-active) .sidebar-container {
  display: block !important;
}

body:not(.login-active) .sidebar-header {
  display: block !important;
}

body:not(.login-active) .navigation-menu {
  display: block !important;
}

body:not(.login-active) .main-content {
  display: block !important;
}

/* Force show all table and data elements */
body:not(.login-active) .data-table,
body:not(.login-active) .table,
body:not(.login-active) .table-container,
body:not(.login-active) .table-responsive,
body:not(.login-active) .card,
body:not(.login-active) .dashboard-content,
body:not(.login-active) .page-content {
  display: block !important;
  visibility: visible !important;
  opacity: 1 !important;
}

/* Force show table rows and cells */
body:not(.login-active) .table tbody,
body:not(.login-active) .table tbody tr,
body:not(.login-active) .table tbody td,
body:not(.login-active) .table tbody th {
  display: table !important;
  visibility: visible !important;
  opacity: 1 !important;
}

body:not(.login-active) .table tbody tr {
  display: table-row !important;
}

body:not(.login-active) .table tbody td,
body:not(.login-active) .table tbody th {
  display: table-cell !important;
}

body:not(.login-active) .header {
  display: block !important;
}

body:not(.login-active) .pos-header {
  display: block !important;
}

/* Force show all header elements */
body:not(.login-active) header {
  display: block !important;
  visibility: visible !important;
  opacity: 1 !important;
}

body:not(.login-active) .pos-header {
  display: grid !important;
  visibility: visible !important;
  opacity: 1 !important;
  position: fixed !important;
  top: 0 !important;
  left: 280px !important;
  right: 0 !important;
  width: calc(100vw - 280px) !important;
  z-index: 1000 !important;
  background: white !important;
  padding: 1rem 2rem !important;
  border-bottom: 1px solid #e2e8f0 !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1) !important;
  grid-template-columns: auto 1fr auto !important;
  align-items: center !important;
  gap: 2rem !important;
  min-height: 80px !important;
  box-sizing: border-box !important;
}

body:not(.login-active) .header-left,
body:not(.login-active) .header-center,
body:not(.login-active) .header-right {
  display: flex !important;
  visibility: visible !important;
  opacity: 1 !important;
  align-items: center !important;
}

/* Global override for any page */
:global(.pos-header) {
  display: grid !important;
  visibility: visible !important;
  opacity: 1 !important;
  position: fixed !important;
  top: 0 !important;
  left: 280px !important;
  right: 0 !important;
  width: calc(100vw - 280px) !important;
  z-index: 1000 !important;
  background: white !important;
  padding: 1rem 2rem !important;
  border-bottom: 1px solid #e2e8f0 !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1) !important;
  grid-template-columns: auto 1fr auto !important;
  align-items: center !important;
  gap: 2rem !important;
  min-height: 80px !important;
  box-sizing: border-box !important;
}

:global(body:not(.login-active) .pos-header) {
  display: grid !important;
  visibility: visible !important;
  opacity: 1 !important;
  position: fixed !important;
  top: 0 !important;
  left: 280px !important;
  right: 0 !important;
  width: calc(100vw - 280px) !important;
  z-index: 1000 !important;
  background: white !important;
  padding: 1rem 2rem !important;
  border-bottom: 1px solid #e2e8f0 !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1) !important;
  grid-template-columns: auto 1fr auto !important;
  align-items: center !important;
  gap: 2rem !important;
  min-height: 80px !important;
  box-sizing: border-box !important;
}

/* Khi sidebar collapsed */
:global(body:not(.login-active) .sidebar-collapsed + main .pos-header) {
  left: 80px !important;
  width: calc(100vw - 80px) !important;
}

:global(body:not(.login-active) .sidebar-collapsed .pos-header) {
  left: 80px !important;
  width: calc(100vw - 80px) !important;
}

/* TẠM THỜI ẨN FACEBOOK MESSENGER */
.facebook-messenger-floating,
.messenger-manager {
  display: none !important;
  visibility: hidden !important;
  opacity: 0 !important;
}
</style>
