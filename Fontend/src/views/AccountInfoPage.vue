<template>
  <div class="account-info-page">
    <PosHeader />
    
    <div class="main-content">
      <div class="container">
        <div class="page-header">
          <h1>Thông tin tài khoản</h1>
          <p>Quản lý thông tin cá nhân và bảo mật tài khoản</p>
        </div>
        
        <div class="account-content">
          <!-- User Profile Card -->
          <div class="profile-card">
            <div class="profile-header">
              <div class="profile-avatar">
                <i class="fas fa-user-circle"></i>
              </div>
              <div class="profile-info">
                <h2>{{ authStore.userName }}</h2>
                <p class="user-role">{{ getRoleDisplayName(authStore.userRole) }}</p>
                <p class="user-email">{{ authStore.user?.email || 'N/A' }}</p>
              </div>
            </div>
          </div>
          
          <!-- Account Actions -->
          <div class="actions-grid">
            <div class="action-card" @click="openChangePasswordModal">
              <div class="action-icon">
                <i class="fas fa-key"></i>
              </div>
              <div class="action-content">
                <h3>Đổi mật khẩu</h3>
                <p>Cập nhật mật khẩu để bảo mật tài khoản</p>
              </div>
              <div class="action-arrow">
                <i class="fas fa-chevron-right"></i>
              </div>
            </div>
            
            <div class="action-card" @click="viewProfile">
              <div class="action-icon">
                <i class="fas fa-user"></i>
              </div>
              <div class="action-content">
                <h3>Thông tin cá nhân</h3>
                <p>Xem và chỉnh sửa thông tin cá nhân</p>
              </div>
              <div class="action-arrow">
                <i class="fas fa-chevron-right"></i>
              </div>
            </div>
            
            <div class="action-card" @click="viewSettings">
              <div class="action-icon">
                <i class="fas fa-cog"></i>
              </div>
              <div class="action-content">
                <h3>Cài đặt</h3>
                <p>Tùy chỉnh cài đặt tài khoản</p>
              </div>
              <div class="action-arrow">
                <i class="fas fa-chevron-right"></i>
              </div>
            </div>
            
            <div class="action-card logout-card" @click="handleLogout">
              <div class="action-icon">
                <i class="fas fa-sign-out-alt"></i>
              </div>
              <div class="action-content">
                <h3>Đăng xuất</h3>
                <p>Đăng xuất khỏi tài khoản</p>
              </div>
              <div class="action-arrow">
                <i class="fas fa-chevron-right"></i>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Change Password Modal -->
    <ChangePasswordModal 
      :isOpen="showChangePasswordModal" 
      @close="closeChangePasswordModal" 
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useAuthStore } from '@/stores/authStore'
import { useRouter } from 'vue-router'
import PosHeader from '@/components/PosHeader.vue'
import ChangePasswordModal from '@/components/ChangePasswordModal.vue'

const authStore = useAuthStore()
const router = useRouter()
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

const openChangePasswordModal = () => {
  showChangePasswordModal.value = true
}

const closeChangePasswordModal = () => {
  showChangePasswordModal.value = false
}

const viewProfile = () => {
  // TODO: Navigate to profile page
  console.log('View profile clicked')
}

const viewSettings = () => {
  // TODO: Navigate to settings page
  console.log('View settings clicked')
}

const handleLogout = async () => {
  try {
    await authStore.logout()
    router.push('/login')
  } catch (error) {
    console.error('Logout error:', error)
  }
}
</script>

<style scoped>
.account-info-page {
  min-height: 100vh;
  background: #f8fafc;
}

.main-content {
  padding-top: 80px;
  min-height: 100vh;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.page-header {
  text-align: center;
  margin-bottom: 3rem;
}

.page-header h1 {
  font-size: 2.5rem;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 0.5rem;
}

.page-header p {
  font-size: 1.125rem;
  color: #64748b;
}

.account-content {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.profile-card {
  background: white;
  border-radius: 16px;
  padding: 2rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.profile-avatar {
  font-size: 4rem;
  color: #667eea;
}

.profile-info h2 {
  font-size: 1.875rem;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 0.5rem;
}

.user-role {
  font-size: 1rem;
  color: #667eea;
  font-weight: 600;
  margin-bottom: 0.25rem;
}

.user-email {
  font-size: 0.875rem;
  color: #64748b;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
}

.action-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 4px -1px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.action-card:hover {
  box-shadow: 0 4px 12px -1px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
  border-color: #d1d5db;
}

.action-card.logout-card {
  border-color: #fecaca;
  background: #fef2f2;
}

.action-card.logout-card:hover {
  border-color: #fca5a5;
  background: #fee2e2;
}

.action-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.25rem;
  flex-shrink: 0;
}

.logout-card .action-icon {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
}

.action-content {
  flex: 1;
}

.action-content h3 {
  font-size: 1.125rem;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 0.25rem;
}

.action-content p {
  font-size: 0.875rem;
  color: #64748b;
  margin: 0;
}

.action-arrow {
  color: #9ca3af;
  font-size: 0.875rem;
}

@media (max-width: 768px) {
  .container {
    padding: 1rem;
  }
  
  .page-header h1 {
    font-size: 2rem;
  }
  
  .profile-header {
    flex-direction: column;
    text-align: center;
  }
  
  .actions-grid {
    grid-template-columns: 1fr;
  }
  
  .action-card {
    padding: 1rem;
  }
}
</style>
