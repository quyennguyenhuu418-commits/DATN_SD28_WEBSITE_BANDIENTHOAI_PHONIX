<template>
  <div class="customer-account-page">
    <HeaderLayout />
    
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
                <i class="bi bi-person-circle"></i>
              </div>
              <div class="profile-info">
                <h2>{{ customerAuthStore.userName || 'Khách hàng' }}</h2>
                <p class="user-role">Khách hàng</p>
                <p class="user-email">{{ customerAuthStore.userEmail || customerAuthStore.user?.email || 'N/A' }}</p>
                <p class="user-phone" v-if="customerAuthStore.user?.soDienThoai">
                  {{ customerAuthStore.user.soDienThoai }}
                </p>
              </div>
            </div>
          </div>
          
          <!-- Account Actions -->
          <div class="actions-grid">
            <div class="action-card" @click="openChangePasswordModal">
              <div class="action-icon">
                <i class="bi bi-key"></i>
              </div>
              <div class="action-content">
                <h3>Đổi mật khẩu</h3>
                <p>Cập nhật mật khẩu để bảo mật tài khoản</p>
              </div>
              <div class="action-arrow">
                <i class="bi bi-chevron-right"></i>
              </div>
            </div>
            
            <div class="action-card" @click="viewProfile">
              <div class="action-icon">
                <i class="bi bi-person"></i>
              </div>
              <div class="action-content">
                <h3>Thông tin cá nhân</h3>
                <p>Xem và chỉnh sửa thông tin cá nhân</p>
              </div>
              <div class="action-arrow">
                <i class="bi bi-chevron-right"></i>
              </div>
            </div>
            
            <div class="action-card" @click="viewOrders">
              <div class="action-icon">
                <i class="bi bi-bag"></i>
              </div>
              <div class="action-content">
                <h3>Đơn hàng của tôi</h3>
                <p>Xem lịch sử đơn hàng và theo dõi đơn hàng</p>
              </div>
              <div class="action-arrow">
                <i class="bi bi-chevron-right"></i>
              </div>
            </div>
            
            <div class="action-card logout-card" @click="handleLogout">
              <div class="action-icon">
                <i class="bi bi-box-arrow-right"></i>
              </div>
              <div class="action-content">
                <h3>Đăng xuất</h3>
                <p>Đăng xuất khỏi tài khoản</p>
              </div>
              <div class="action-arrow">
                <i class="bi bi-chevron-right"></i>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Change Password Modal -->
    <div v-if="showChangePasswordModal" class="modal-overlay" @click="closeChangePasswordModal">
      <div class="modal-box" @click.stop>
        <div class="modal-header">
          <h3>Đổi mật khẩu</h3>
          <button class="close-modal" @click="closeChangePasswordModal">
            <i class="bi bi-x-lg"></i>
          </button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleChangePassword">
            <div class="form-group">
              <label>Mật khẩu hiện tại:</label>
              <input
                type="password"
                v-model="passwordForm.currentPassword"
                required
                placeholder="Nhập mật khẩu hiện tại"
              />
            </div>
            <div class="form-group">
              <label>Mật khẩu mới:</label>
              <input
                type="password"
                v-model="passwordForm.newPassword"
                required
                placeholder="Nhập mật khẩu mới"
                minlength="6"
              />
            </div>
            <div class="form-group">
              <label>Xác nhận mật khẩu mới:</label>
              <input
                type="password"
                v-model="passwordForm.confirmPassword"
                required
                placeholder="Nhập lại mật khẩu mới"
                minlength="6"
              />
            </div>
            <div v-if="passwordError" class="error-message">
              {{ passwordError }}
            </div>
            <div v-if="passwordSuccess" class="success-message">
              {{ passwordSuccess }}
            </div>
            <div class="modal-actions">
              <button type="button" class="btn-cancel" @click="closeChangePasswordModal">
                Hủy
              </button>
              <button type="submit" class="btn-submit" :disabled="changingPassword">
                {{ changingPassword ? 'Đang xử lý...' : 'Đổi mật khẩu' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useCustomerAuthStore } from '@/stores/customerAuthStore'
import { useRouter } from 'vue-router'
import HeaderLayout from './HeaderLayout.vue'
import api from '@/services/api'

const customerAuthStore = useCustomerAuthStore()
const router = useRouter()

const showChangePasswordModal = ref(false)
const changingPassword = ref(false)
const passwordError = ref('')
const passwordSuccess = ref('')

const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

onMounted(async () => {
  // Load customer info if authenticated
  if (customerAuthStore.isAuthenticated) {
    await customerAuthStore.getCurrentUser()
  } else {
    // Redirect to login if not authenticated
    router.push('/customer/login')
  }
})

const openChangePasswordModal = () => {
  if (!customerAuthStore.user?.matKhau) {
    alert('Tài khoản này được đăng nhập bằng Google, không thể đổi mật khẩu')
    return
  }
  showChangePasswordModal.value = true
  passwordForm.value = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  passwordError.value = ''
  passwordSuccess.value = ''
}

const closeChangePasswordModal = () => {
  showChangePasswordModal.value = false
  passwordForm.value = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  passwordError.value = ''
  passwordSuccess.value = ''
}

const handleChangePassword = async () => {
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    passwordError.value = 'Mật khẩu xác nhận không khớp'
    return
  }

  if (passwordForm.value.newPassword.length < 6) {
    passwordError.value = 'Mật khẩu phải có ít nhất 6 ký tự'
    return
  }

  changingPassword.value = true
  passwordError.value = ''
  passwordSuccess.value = ''

  try {
    const response = await api.put('/api/customer/auth/update-profile', {
      currentPassword: passwordForm.value.currentPassword,
      newPassword: passwordForm.value.newPassword,
      confirmPassword: passwordForm.value.confirmPassword
    })

    if (response.data.success) {
      passwordSuccess.value = 'Đổi mật khẩu thành công!'
      setTimeout(() => {
        closeChangePasswordModal()
      }, 1500)
    } else {
      passwordError.value = response.data.message || 'Đổi mật khẩu thất bại'
    }
  } catch (err) {
    passwordError.value = err.response?.data?.message || 'Có lỗi xảy ra khi đổi mật khẩu'
  } finally {
    changingPassword.value = false
  }
}

const viewProfile = () => {
  // TODO: Navigate to profile edit page
  alert('Tính năng đang được phát triển')
}

const viewOrders = () => {
  router.push('/theo-doi-don-hang')
}

const handleLogout = async () => {
  if (confirm('Bạn có chắc chắn muốn đăng xuất?')) {
    try {
      await customerAuthStore.logout()
      router.push('/')
    } catch (error) {
      console.error('Logout error:', error)
    }
  }
}
</script>

<style scoped>
.customer-account-page {
  min-height: 100vh;
  background: #f8fafc;
}

.main-content {
  padding-top: 77px;
  min-height: calc(100vh - 77px);
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
  color: #FF5500;
}

.profile-info h2 {
  font-size: 1.875rem;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 0.5rem;
}

.user-role {
  font-size: 1rem;
  color: #FF5500;
  font-weight: 600;
  margin-bottom: 0.25rem;
}

.user-email,
.user-phone {
  font-size: 0.875rem;
  color: #64748b;
  margin-bottom: 0.25rem;
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
  background: linear-gradient(135deg, #FF5500 0%, #DC143C 100%);
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

/* Modal Styles */
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
  border-radius: 16px;
  padding: 2rem;
  max-width: 500px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.modal-header h3 {
  margin: 0;
  color: #1e293b;
  font-size: 1.5rem;
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
  color: #DC143C;
}

.modal-body .form-group {
  margin-bottom: 1.5rem;
}

.modal-body .form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #333;
  font-weight: 500;
}

.modal-body .form-group input {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 1rem;
  box-sizing: border-box;
}

.modal-body .form-group input:focus {
  outline: none;
  border-color: #FF5500;
}

.error-message {
  background: #fee;
  color: #c33;
  padding: 0.75rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  font-size: 0.9rem;
}

.success-message {
  background: #efe;
  color: #3c3;
  padding: 0.75rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  font-size: 0.9rem;
}

.modal-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
}

.btn-cancel,
.btn-submit {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-cancel {
  background: #e5e7eb;
  color: #374151;
}

.btn-cancel:hover {
  background: #d1d5db;
}

.btn-submit {
  background: #FF5500;
  color: white;
}

.btn-submit:hover:not(:disabled) {
  background: #DC143C;
}

.btn-submit:disabled {
  background: #ccc;
  cursor: not-allowed;
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

