<template>
  <div class="user-menu" v-if="authStore.isAuthenticated">
    <!-- User Info Button -->
    <div class="user-info" @click="toggleDropdown" :class="{ active: isDropdownOpen }">
      <div class="user-avatar">
        <i class="fas fa-user-circle"></i>
      </div>
      <div class="user-details">
        <div class="user-name">{{ authStore.userName }}</div>
        <div class="user-role">{{ getRoleDisplayName(authStore.userRole) }}</div>
      </div>
      <div class="dropdown-arrow">
        <i class="fas fa-chevron-down" :class="{ rotated: isDropdownOpen }"></i>
      </div>
    </div>
    
    <!-- Dropdown Menu -->
    <div v-if="isDropdownOpen" class="dropdown-menu" @click.stop>
      <!-- User Profile Section -->
      <div class="dropdown-header">
        <div class="profile-info">
          <div class="profile-avatar">
            <i class="fas fa-user-circle"></i>
          </div>
          <div class="profile-details">
            <div class="profile-name">{{ authStore.userName }}</div>
            <div class="profile-email">{{ authStore.user?.email || 'N/A' }}</div>
            <div class="profile-role">{{ getRoleDisplayName(authStore.userRole) }}</div>
            <div class="profile-id" v-if="authStore.user?.id">
              ID: {{ authStore.user.id }}
            </div>
          </div>
        </div>
      </div>
      
      <div class="dropdown-divider"></div>
      
      <!-- Menu Items -->
      <div class="dropdown-items">
        <button @click="viewProfile" class="dropdown-item">
          <div class="item-icon">
            <i class="fas fa-user"></i>
          </div>
          <div class="item-content">
            <span class="item-title">Thông tin cá nhân</span>
            <span class="item-subtitle">Xem và chỉnh sửa profile</span>
          </div>
        </button>
        
        <button @click="changePassword" class="dropdown-item">
          <div class="item-icon">
            <i class="fas fa-key"></i>
          </div>
          <div class="item-content">
            <span class="item-title">Đổi mật khẩu</span>
            <span class="item-subtitle">Cập nhật bảo mật tài khoản</span>
          </div>
        </button>
        
        <button @click="viewSettings" class="dropdown-item">
          <div class="item-icon">
            <i class="fas fa-cog"></i>
          </div>
          <div class="item-content">
            <span class="item-title">Cài đặt</span>
            <span class="item-subtitle">Tùy chỉnh hệ thống</span>
          </div>
        </button>
        
        <div class="dropdown-divider"></div>
        
        <button @click="handleLogout" class="dropdown-item logout-item" :disabled="authStore.loading">
          <div class="item-icon">
            <i class="fas fa-sign-out-alt"></i>
          </div>
          <div class="item-content">
            <span class="item-title" v-if="authStore.loading">Đang đăng xuất...</span>
            <span class="item-title" v-else>Đăng xuất</span>
            <span class="item-subtitle">Thoát khỏi tài khoản</span>
          </div>
        </button>
      </div>
    </div>
  </div>
  
  <div v-else class="login-prompt">
    <router-link to="/login" class="login-link">
      <i class="fas fa-sign-in-alt"></i>
      Đăng nhập
    </router-link>
  </div>
  
  <!-- Change Password Modal -->
  <ChangePasswordModal 
    :isOpen="showChangePasswordModal" 
    @close="closeChangePasswordModal" 
  />
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useAuthStore } from '@/stores/authStore'
import { useRouter } from 'vue-router'
import ChangePasswordModal from './ChangePasswordModal.vue'

const authStore = useAuthStore()
const router = useRouter()
const isDropdownOpen = ref(false)
const showChangePasswordModal = ref(false)

const getRoleDisplayName = (role) => {
  const roleMap = {
    'ADMIN': 'Quản trị viên',
    'MANAGER': 'Quản lý',
    'STAFF': 'Nhân viên',
    'GUEST': 'Khách'
  }
  return roleMap[role] || role
}

const toggleDropdown = () => {
  isDropdownOpen.value = !isDropdownOpen.value
}

const closeDropdown = () => {
  isDropdownOpen.value = false
}

const viewProfile = () => {
  closeDropdown()
  router.push('/account')
}

const changePassword = () => {
  closeDropdown()
  showChangePasswordModal.value = true
}

const viewSettings = () => {
  closeDropdown()
  // TODO: Navigate to settings page
  console.log('View settings clicked')
}

const handleLogout = async () => {
  try {
    closeDropdown()
    await authStore.logout()
    router.push('/login')
  } catch (error) {
    console.error('Logout error:', error)
  }
}

const closeChangePasswordModal = () => {
  showChangePasswordModal.value = false
}

// Close dropdown when clicking outside
const handleClickOutside = (event) => {
  if (!event.target.closest('.user-menu')) {
    closeDropdown()
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.user-menu {
  position: relative;
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  background: white;
  border-radius: 6px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid #e5e7eb;
}

.user-info:hover {
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
  border-color: #d1d5db;
}

.user-info.active {
  border-color: #667eea;
  box-shadow: 0 2px 6px rgba(102, 126, 234, 0.2);
}

.user-avatar {
  font-size: 1.5rem;
  color: #667eea;
}

.user-details {
  display: flex;
  flex-direction: column;
  min-width: 100px;
}

.user-name {
  font-weight: 600;
  color: #333;
  font-size: 0.85rem;
  margin-bottom: 1px;
}

.user-role {
  font-size: 0.75rem;
  color: #666;
}

.dropdown-arrow {
  margin-left: 0.25rem;
  transition: transform 0.2s ease;
}

.dropdown-arrow i {
  font-size: 0.7rem;
  color: #666;
}

.dropdown-arrow i.rotated {
  transform: rotate(180deg);
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  border: 1px solid #e5e7eb;
  min-width: 280px;
  z-index: 1000;
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
  padding: 1rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.profile-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.profile-avatar {
  font-size: 2.5rem;
  opacity: 0.9;
}

.profile-details {
  flex: 1;
}

.profile-name {
  font-weight: 600;
  font-size: 1rem;
  margin-bottom: 4px;
}

.profile-email {
  font-size: 0.85rem;
  opacity: 0.9;
  margin-bottom: 2px;
}

.profile-role {
  font-size: 0.8rem;
  opacity: 0.8;
  background: rgba(255, 255, 255, 0.2);
  padding: 2px 8px;
  border-radius: 12px;
  display: inline-block;
  margin-bottom: 4px;
}

.profile-id {
  font-size: 0.75rem;
  opacity: 0.7;
  font-family: 'Courier New', monospace;
}

.dropdown-divider {
  height: 1px;
  background: #e5e7eb;
  margin: 0;
}

.dropdown-items {
  padding: 0.5rem 0;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  width: 100%;
  padding: 0.75rem 1rem;
  background: none;
  border: none;
  text-align: left;
  cursor: pointer;
  color: #374151;
  transition: background-color 0.2s ease;
}

.item-icon {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.item-icon i {
  font-size: 0.9rem;
  color: #6b7280;
}

.item-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
}

.item-title {
  font-size: 0.9rem;
  font-weight: 500;
  color: #374151;
}

.item-subtitle {
  font-size: 0.75rem;
  color: #9ca3af;
}

.dropdown-item:hover {
  background: #f3f4f6;
}

.dropdown-item:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}


.dropdown-item.logout-item {
  color: #dc2626;
}

.dropdown-item.logout-item:hover {
  background: #fef2f2;
}

.dropdown-item.logout-item .item-icon i {
  color: #dc2626;
}

.dropdown-item.logout-item .item-title {
  color: #dc2626;
}

.login-prompt {
  display: flex;
  align-items: center;
}

.login-link {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: #667eea;
  color: white;
  text-decoration: none;
  border-radius: 8px;
  font-size: 0.9rem;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.2);
}

.login-link:hover {
  background: #5a67d8;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  color: white;
}

@media (max-width: 768px) {
  .dropdown-menu {
    left: 0;
    right: 0;
    min-width: auto;
  }

  .user-details {
    min-width: 100px;
  }

  .profile-info {
    flex-direction: column;
    text-align: center;
  }

  .profile-details {
    text-align: center;
  }
}
</style>
