<template>
  <div class="header-logout-dropdown">
    <div class="user-profile" @click="toggleDropdown">
      <div class="user-avatar">
        <i class="fas fa-user"></i>
      </div>
      <div class="user-info" v-if="!isCollapsed">
        <div class="user-name">{{ userInfo.hoTen }}</div>
        <div class="user-role">{{ userInfo.vaiTro }}</div>
      </div>
      <div class="dropdown-arrow">
        <i class="fas fa-chevron-down" :class="{ 'rotate-180': isOpen }"></i>
      </div>
    </div>

    <!-- Dropdown Menu -->
    <div v-if="isOpen" class="dropdown-menu" @click.stop>
      <div class="dropdown-header">
        <div class="header-avatar">
          <i class="fas fa-user"></i>
        </div>
        <div class="header-info">
          <div class="header-name">{{ userInfo.hoTen }}</div>
          <div class="header-email">{{ userInfo.email }}</div>
        </div>
      </div>

      <div class="dropdown-divider"></div>

      <div class="dropdown-item" @click="goToAccount">
        <i class="fas fa-user-edit"></i>
        <span>Thông tin tài khoản</span>
      </div>

      <div class="dropdown-item" @click="changePassword">
        <i class="fas fa-key"></i>
        <span>Đổi mật khẩu</span>
      </div>

      <div class="dropdown-divider"></div>

      <div class="dropdown-item logout" @click="logout">
        <i class="fas fa-sign-out-alt"></i>
        <span>Đăng xuất</span>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

export default {
  name: 'HeaderLogoutDropdown',
  props: {
    isCollapsed: {
      type: Boolean,
      default: false
    }
  },
  emits: ['open-change-password'],
  setup(props, { emit }) {
    const router = useRouter()
    const authStore = useAuthStore()
    const isOpen = ref(false)

    const userInfo = computed(() => {
      // Map role from English to Vietnamese
      const getRoleDisplayName = (role) => {
        const roleMap = {
          'ADMIN': 'Quản trị viên',
          'MANAGER': 'Quản lý',
          'STAFF': 'Nhân viên',
          'GUEST': 'Khách',
          'Quản trị viên': 'Quản trị viên',
          'Quản lý': 'Quản lý',
          'Nhân viên': 'Nhân viên',
          'Khách': 'Khách'
        }
        return roleMap[role] || 'Nhân viên'
      }

      return {
        hoTen: authStore.user?.hoTen || 'Người dùng',
        email: authStore.user?.email || 'user@example.com',
        vaiTro: getRoleDisplayName(authStore.user?.role || authStore.user?.vaiTro)
      }
    })

    const toggleDropdown = () => {
      isOpen.value = !isOpen.value
    }

    const goToAccount = () => {
      isOpen.value = false
      router.push('/account')
    }

    const changePassword = () => {
      console.log('Change password clicked!')
      isOpen.value = false
      // Emit event to parent to open change password modal
      emit('open-change-password')
      console.log('Change password event emitted!')
    }

    const logout = () => {
      isOpen.value = false
      authStore.logout()
      router.push('/login')
    }

    // Close dropdown when clicking outside
    const handleClickOutside = (event) => {
      if (!event.target.closest('.header-logout-dropdown')) {
        isOpen.value = false
      }
    }

    // Add event listener when component mounts
    onMounted(() => {
      document.addEventListener('click', handleClickOutside)
    })

    onUnmounted(() => {
      document.removeEventListener('click', handleClickOutside)
    })

    return {
      isOpen,
      userInfo,
      toggleDropdown,
      goToAccount,
      changePassword,
      logout
    }
  }
}
</script>

<style scoped>
.header-logout-dropdown {
  position: relative;
  display: flex;
  align-items: center;
  z-index: 1000;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  min-width: 200px;
}

.user-profile:hover {
  background: #f1f5f9;
  border-color: #3b82f6;
}

.user-avatar {
  width: 32px;
  height: 32px;
  background: #3b82f6;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 0.875rem;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-weight: 600;
  color: #1e293b;
  font-size: 0.875rem;
  line-height: 1.25rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-role {
  color: #64748b;
  font-size: 0.75rem;
  line-height: 1rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dropdown-arrow {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.dropdown-arrow:hover {
  background: #e2e8f0;
}

.dropdown-arrow i {
  font-size: 0.7rem;
  color: #64748b;
  transition: transform 0.2s ease;
}

.rotate-180 {
  transform: rotate(180deg);
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  border: 1px solid #e5e7eb;
  min-width: 280px;
  max-width: 350px;
  z-index: 99999;
  overflow: hidden;
  animation: slideDown 0.2s ease-out;
  margin-top: 8px;
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

.dropdown-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}

.header-avatar {
  width: 40px;
  height: 40px;
  background: #3b82f6;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1rem;
}

.header-info {
  flex: 1;
  min-width: 0;
}

.header-name {
  font-weight: 600;
  color: #1e293b;
  font-size: 0.875rem;
  line-height: 1.25rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.header-email {
  color: #64748b;
  font-size: 0.75rem;
  line-height: 1rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dropdown-divider {
  height: 1px;
  background: #e2e8f0;
  margin: 0.5rem 0;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #374151;
  font-size: 0.875rem;
}

.dropdown-item:hover {
  background: #f8fafc;
  color: #1e293b;
}

.dropdown-item i {
  width: 16px;
  text-align: center;
  color: #64748b;
}

.dropdown-item.logout {
  color: #dc2626;
}

.dropdown-item.logout:hover {
  background: #fef2f2;
  color: #dc2626;
}

.dropdown-item.logout i {
  color: #dc2626;
}

/* Responsive */
@media (max-width: 768px) {
  .user-profile {
    min-width: 150px;
    padding: 0.5rem;
  }
  
  .dropdown-menu {
    right: 0;
    left: auto;
    min-width: 250px;
    max-width: calc(100vw - 40px);
  }
}

/* Ensure dropdown is always visible */
.dropdown-menu {
  position: absolute !important;
  z-index: 99999 !important;
  max-height: calc(100vh - 200px);
  overflow-y: auto;
}

/* Force dropdown to appear above everything */
.dropdown-menu * {
  z-index: 100000 !important;
}

/* Ensure dropdown is visible and not hidden */
.dropdown-menu {
  display: block !important;
  visibility: visible !important;
  opacity: 1 !important;
  pointer-events: auto !important;
}

/* Ensure dropdown is not clipped by any parent container */
.header-logout-dropdown {
  overflow: visible !important;
}

/* Ensure parent containers allow dropdown to show */
:global(.pos-header) {
  overflow: visible !important;
}

:global(.header-right) {
  overflow: visible !important;
}
</style>
