<template>
  <div id="app" class="min-h-screen bg-gray-50">
    <div class="flex">
      <!-- Sidebar -->
      <aside
        class="w-64 bg-white text-gray-600 shadow-xl transition-all duration-300"
        :class="{ 'w-16': sidebarCollapsed }"
      >
        <!-- Header -->
        <div class="p-6">
          <div class="flex items-center justify-between">
            <div v-if="!sidebarCollapsed" class="flex items-center space-x-3">
              <div class="logo-container">
                <img 
                  src="@/assets/ChatGPT Image 16_53_01 28 thg 9, 2025 1.png" 
                  alt="PhoniX Logo" 
                  class="logo-image"
                />
              </div>
            </div>
            <button
              class="p-2 rounded-lg hover:bg-gray-100 transition-colors duration-200"
              @click="toggleSidebar"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M4 6h16M4 12h16M4 18h16"
                />
              </svg>
            </button>
          </div>
        </div>

        <!-- Navigation -->
        <nav class="flex-1 p-4 space-y-1">
          <!-- Thống Kê -->
          <router-link
            to="/dashboard"
            class="flex items-center space-x-3 px-4 py-3 rounded-lg transition-all duration-200 hover:bg-gray-50 group relative"
            :class="{ 'justify-center': sidebarCollapsed }"
          >
            <svg
              class="w-5 h-5 text-gray-400"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"
              />
            </svg>
            <span v-if="!sidebarCollapsed" class="text-sm font-medium text-gray-400">Thống Kê</span>
          </router-link>

          <!-- Sản phẩm -->
          <router-link
            to="/san-pham"
            class="flex items-center space-x-3 px-4 py-3 rounded-lg transition-all duration-200 hover:bg-gray-50 group relative"
            :class="{ 'justify-center': sidebarCollapsed }"
          >
            <svg
              class="w-5 h-5 text-blue-500"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"
              />
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1"
              />
            </svg>
            <span v-if="!sidebarCollapsed" class="text-sm font-medium text-blue-500">Sản phẩm</span>
            <div class="absolute left-0 top-0 bottom-0 w-1 bg-blue-500 rounded-r"></div>
          </router-link>

          <!-- Khách Hàng -->
          <router-link
            to="/khach-hang"
            class="flex items-center space-x-3 px-4 py-3 rounded-lg transition-all duration-200 hover:bg-gray-50 group relative"
            :class="{ 'justify-center': sidebarCollapsed }"
          >
            <svg
              class="w-5 h-5 text-gray-400"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
              />
            </svg>
            <span v-if="!sidebarCollapsed" class="text-sm font-medium text-gray-400">Khách Hàng</span>
          </router-link>

          <!-- Thuộc Tính with Submenu -->
          <div class="relative">
            <button
              @click="toggleThuocTinh"
              class="flex items-center space-x-3 px-4 py-3 rounded-lg transition-all duration-200 hover:bg-gray-50 group relative w-full"
              :class="{ 'justify-center': sidebarCollapsed }"
            >
              <svg
                class="w-5 h-5 text-gray-400"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"
                />
              </svg>
              <span v-if="!sidebarCollapsed" class="text-sm font-medium text-gray-400">Thuộc Tính</span>
              <svg 
                v-if="!sidebarCollapsed"
                class="w-4 h-4 text-gray-400 ml-auto transition-transform"
                :class="{ 'rotate-180': showThuocTinh }"
                fill="none" 
                stroke="currentColor" 
                viewBox="0 0 24 24"
              >
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/>
              </svg>
            </button>
            
            <!-- Submenu -->
            <div 
              v-if="showThuocTinh && !sidebarCollapsed"
              class="ml-4 mt-1 space-y-1 bg-gray-50 rounded-lg p-2"
            >
              <router-link
                to="/hang"
                class="flex items-center space-x-2 px-3 py-2 rounded-md text-sm text-gray-600 hover:bg-white hover:text-gray-900 transition-colors"
              >
                <span>Hãng</span>
              </router-link>
              <router-link
                to="/he-dieu-hanh"
                class="flex items-center space-x-2 px-3 py-2 rounded-md text-sm text-gray-600 hover:bg-white hover:text-gray-900 transition-colors"
              >
                <span>Hệ điều hành</span>
              </router-link>
              <router-link
                to="/mau-sac"
                class="flex items-center space-x-2 px-3 py-2 rounded-md text-sm text-gray-600 hover:bg-white hover:text-gray-900 transition-colors"
              >
                <span>Màu sắc</span>
              </router-link>
              <router-link
                to="/man-hinh"
                class="flex items-center space-x-2 px-3 py-2 rounded-md text-sm text-gray-600 hover:bg-white hover:text-gray-900 transition-colors"
              >
                <span>Màn hình</span>
              </router-link>
              <router-link
                to="/chip"
                class="flex items-center space-x-2 px-3 py-2 rounded-md text-sm text-gray-600 hover:bg-white hover:text-gray-900 transition-colors"
              >
                <span>Chip</span>
              </router-link>
              <router-link
                to="/ram"
                class="flex items-center space-x-2 px-3 py-2 rounded-md text-sm text-gray-600 hover:bg-white hover:text-gray-900 transition-colors"
              >
                <span>RAM</span>
              </router-link>
              <router-link
                to="/rom"
                class="flex items-center space-x-2 px-3 py-2 rounded-md text-sm text-gray-600 hover:bg-white hover:text-gray-900 transition-colors"
              >
                <span>ROM</span>
              </router-link>
              <router-link
                to="/cpu"
                class="flex items-center space-x-2 px-3 py-2 rounded-md text-sm text-gray-600 hover:bg-white hover:text-gray-900 transition-colors"
              >
                <span>CPU</span>
              </router-link>
              <router-link
                to="/gpu"
                class="flex items-center space-x-2 px-3 py-2 rounded-md text-sm text-gray-600 hover:bg-white hover:text-gray-900 transition-colors"
              >
                <span>GPU</span>
              </router-link>
              <router-link
                to="/pin"
                class="flex items-center space-x-2 px-3 py-2 rounded-md text-sm text-gray-600 hover:bg-white hover:text-gray-900 transition-colors"
              >
                <span>Pin</span>
              </router-link>
              <router-link
                to="/camera-truoc"
                class="flex items-center space-x-2 px-3 py-2 rounded-md text-sm text-gray-600 hover:bg-white hover:text-gray-900 transition-colors"
              >
                <span>Camera trước</span>
              </router-link>
              <router-link
                to="/camera-sau"
                class="flex items-center space-x-2 px-3 py-2 rounded-md text-sm text-gray-600 hover:bg-white hover:text-gray-900 transition-colors"
              >
                <span>Camera sau</span>
              </router-link>
            </div>
          </div>

          <!-- Giảm Giá -->
          <router-link
            to="/giam-gia"
            class="flex items-center space-x-3 px-4 py-3 rounded-lg transition-all duration-200 hover:bg-gray-50 group relative"
            :class="{ 'justify-center': sidebarCollapsed }"
          >
            <svg
              class="w-5 h-5 text-gray-400"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z"
              />
            </svg>
            <span v-if="!sidebarCollapsed" class="text-sm font-medium text-gray-400">Giảm Giá</span>
          </router-link>

          <!-- Đợt Giảm Giá -->
          <router-link
            to="/dot-giam-gia"
            class="flex items-center space-x-3 px-4 py-3 rounded-lg transition-all duration-200 hover:bg-gray-50 group relative"
            :class="{ 'justify-center': sidebarCollapsed }"
          >
            <svg
              class="w-5 h-5 text-gray-400"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M12 8v13m0-13V6a2 2 0 112 2h-2zm0 0V5.5A2.5 2.5 0 109.5 8H12zm-7 4h14M5 12a2 2 0 110-4h14a2 2 0 110 4M5 12v7a2 2 0 002 2h10a2 2 0 002-2v-7"
              />
            </svg>
            <span v-if="!sidebarCollapsed" class="text-sm font-medium text-gray-400">Đợt Giảm Giá</span>
          </router-link>

          <!-- Hoá Đơn -->
          <router-link
            to="/hoa-don"
            class="flex items-center space-x-3 px-4 py-3 rounded-lg transition-all duration-200 hover:bg-gray-50 group relative"
            :class="{ 'justify-center': sidebarCollapsed }"
          >
            <svg
              class="w-5 h-5 text-gray-400"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"
              />
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
              />
            </svg>
            <span v-if="!sidebarCollapsed" class="text-sm font-medium text-gray-400">Hoá Đơn</span>
          </router-link>

          <!-- Nhân Viên -->
          <router-link
            to="/nhan-vien"
            class="flex items-center space-x-3 px-4 py-3 rounded-lg transition-all duration-200 hover:bg-gray-50 group relative"
            :class="{ 'justify-center': sidebarCollapsed }"
          >
            <svg
              class="w-5 h-5 text-gray-400"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"
              />
            </svg>
            <span v-if="!sidebarCollapsed" class="text-sm font-medium text-gray-400">Nhân Viên</span>
          </router-link>

          <!-- Cài Đặt -->
          <router-link
            to="/cai-dat"
            class="flex items-center space-x-3 px-4 py-3 rounded-lg transition-all duration-200 hover:bg-gray-50 group relative"
            :class="{ 'justify-center': sidebarCollapsed }"
          >
            <svg
              class="w-5 h-5 text-gray-400"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"
              />
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
              />
            </svg>
            <span v-if="!sidebarCollapsed" class="text-sm font-medium text-gray-400">Cài Đặt</span>
          </router-link>

          <!-- Thoát -->
          <div class="mt-8 pt-4 border-t border-gray-200">
            <router-link
              to="/logout"
              class="flex items-center space-x-3 px-4 py-3 rounded-lg transition-all duration-200 hover:bg-gray-50 group relative"
              :class="{ 'justify-center': sidebarCollapsed }"
            >
              <svg
                class="w-5 h-5 text-gray-400"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"
                />
              </svg>
              <span v-if="!sidebarCollapsed" class="text-sm font-medium text-gray-400">Thoát</span>
            </router-link>
          </div>
        </nav>
      </aside>

      <!-- Main Content -->
      <main class="flex-1 overflow-y-auto">
        <!-- Topbar -->
        <div class="sticky top-0 z-30 bg-white border-b border-gray-200">
          <div class="px-6 py-4 flex items-center justify-between">
            <div class="flex items-center">
              <h1 class="text-2xl font-bold text-indigo-800">Sản Phẩm</h1>
            </div>
            <div class="flex items-center gap-4">
              <!-- Settings Icon -->
              <div class="w-10 h-10 bg-gray-100 rounded-full flex items-center justify-center cursor-pointer hover:bg-gray-200 transition-colors">
                <img 
                  src="@/assets/settings 1.png" 
                  alt="Settings" 
                  class="w-5 h-5"
                />
              </div>
              
              <!-- Notifications Icon -->
              <div class="w-10 h-10 bg-gray-100 rounded-full flex items-center justify-center cursor-pointer hover:bg-gray-200 transition-colors relative">
                <img 
                  src="@/assets/002-notification-1.png" 
                  alt="Notifications" 
                  class="w-5 h-5"
                />
                <!-- Notification dot -->
                <div class="absolute -top-1 -right-1 w-3 h-3 bg-red-500 rounded-full"></div>
              </div>
              
              <!-- Profile Picture -->
              <div class="w-10 h-10 rounded-full overflow-hidden cursor-pointer hover:opacity-80 transition-opacity">
                <img 
                  src="@/assets/avatar.png" 
                  alt="Profile" 
                  class="w-full h-full object-cover"
                />
              </div>
            </div>
          </div>
        </div>
        <div class="p-4">
          <router-view />
        </div>
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
const globalSearch = ref('')
function doGlobalSearch() {
  // hook tìm kiếm toàn cục, có thể emit event hoặc điều hướng sang trang có ô filter keyword
  console.log('Global search:', globalSearch.value)
}
</script>

<style scoped>
/* Logo styling */
.logo-container {
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-image {
  height: 60px;
  width: auto;
  object-fit: contain;
}

/* Active route styling */
.router-link-active {
  @apply bg-blue-50 border-r-2 border-blue-500;
}

.router-link-active .text-gray-400 {
  @apply text-blue-500 font-semibold;
}

.router-link-active svg {
  @apply text-blue-500;
}

/* Smooth transitions */
* {
  transition: all 0.2s ease-in-out;
}

/* Custom scrollbar for sidebar */
aside::-webkit-scrollbar {
  width: 4px;
}

aside::-webkit-scrollbar-track {
  background: transparent;
}

aside::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 2px;
}

aside::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.3);
}
</style>
          