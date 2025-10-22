<template>
  <div class="login-fullscreen">
    <!-- Main login container with two panels -->
    <div class="login-container">
      <!-- Left Panel - Illustration -->
      <div class="left-panel">
        <div class="illustration-container">
          <div class="main-circle">
            <div class="laptop-icon">
              <i class="fas fa-laptop"></i>
              <div class="user-profile">
                <i class="fas fa-user"></i>
              </div>
            </div>
          </div>
          <!-- Decorative shapes -->
          <div class="shape shape-1"></div>
          <div class="shape shape-2"></div>
          <div class="shape shape-3"></div>
          <div class="shape shape-4"></div>
          <div class="shape shape-5"></div>
        </div>
      </div>

      <!-- Right Panel - Login Form -->
      <div class="right-panel">
        <div class="login-form-container">
          <div class="form-header">
            <div class="logo-container">
              <img src="/logo.png" alt="PhoniX Logo" class="logo-image" />
              <h1>PhoniX Store</h1>
            </div>
            <p class="welcome-text">Chào mừng đến với PhoniX</p>
          </div>

          <form @submit.prevent="handleLogin" class="login-form">
            <div class="input-group">
              <div class="input-container">
                <i class="fas fa-envelope input-icon"></i>
                <input
                  id="username"
                  type="text"
                  v-model="loginForm.username"
                  required
                  placeholder="Email"
                  class="form-input"
                />
              </div>
            </div>

            <div class="input-group">
              <div class="input-container">
                <i class="fas fa-lock input-icon"></i>
                <input
                  id="password"
                  type="password"
                  v-model="loginForm.password"
                  required
                  placeholder="Password"
                  class="form-input"
                />
              </div>
            </div>

            <button type="submit" class="btn-login" :disabled="loading">
              <span v-if="loading">Đang đăng nhập...</span>
              <span v-else>ĐĂNG NHẬP</span>
            </button>
          </form>

          <div class="form-footer">
            <router-link to="/forgot-password" class="forgot-link">
              <i class="fas fa-key"></i>
              Quên tài khoản / mật khẩu?
            </router-link>
          </div>
        </div>
      </div>
    </div>

    <Toast ref="toastRef" />
    
    
    <!-- Shift Handover Modal -->
    <ShiftHandoverModal
      :isOpen="showShiftHandoverModal"
      @close="closeShiftHandoverModal"
      @confirm="handleShiftHandoverConfirmed"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import api from '@/services/api'
import Toast from '@/components/Toast.vue'
import ShiftHandoverModal from '@/components/ShiftHandoverModal.vue'
import { markLoginTime, forcePageReload } from '@/utils/dataRefresh'

const router = useRouter()
const authStore = useAuthStore()
const toastRef = ref<InstanceType<typeof Toast> | null>(null)
const loading = ref(false)
const showShiftHandoverModal = ref(false)

const loginForm = ref({
  username: '',
  password: '',
  rememberMe: false
})

onMounted(() => {
  // Add login-active class to body
  document.body.classList.add('login-active')
  
  // If already authenticated, redirect to dashboard
  if (authStore.isAuthenticated) {
    router.push('/dashboard')
  }
})

onUnmounted(() => {
  // Remove login-active class from body
  document.body.classList.remove('login-active')
})

async function handleLogin() {
  loading.value = true
  
  try {
    const result = await authStore.login({
      username: loginForm.value.username,
      password: loginForm.value.password
    })
    
    if (result.success) {
      toastRef.value?.success('Thành công', 'Đăng nhập thành công!')
      
      // Mark login time for refresh detection
      markLoginTime()
      
      // Check if there are pending shift handovers
      if (result.pendingHandovers && result.pendingHandovers.length > 0) {
        showShiftHandoverModal.value = true
      } else {
        // Auto reload to ensure all data is loaded properly
        forcePageReload(1000)
      }
    } else {
      toastRef.value?.error('Lỗi đăng nhập', result.error || 'Tài khoản hoặc mật khẩu không đúng!')
    }
  } catch (error) {
    toastRef.value?.error('Lỗi', 'Có lỗi xảy ra khi đăng nhập!')
  } finally {
    loading.value = false
  }
}


const closeShiftHandoverModal = () => {
  // Chỉ cho phép đóng khi không có giao ca chờ xác nhận
  // Logic này sẽ được xử lý trong ShiftHandoverModal component
  showShiftHandoverModal.value = false
  // Auto reload to ensure all data is loaded properly
  forcePageReload(500)
}

const handleShiftHandoverConfirmed = (confirmedGiaoCa) => {
  console.log('LoginPage: Shift handover confirmed:', confirmedGiaoCa)
  toastRef.value?.success('Thành công', 'Xác nhận giao ca thành công!')
  
  // Close modal after a short delay to show success message
  setTimeout(() => {
    showShiftHandoverModal.value = false
    
    // Force refresh the page to update all data
    forcePageReload(500)
  }, 1000)
}
</script>

<style scoped>
/* Fullscreen login container - Override everything */
.login-fullscreen {
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  width: 100vw !important;
  height: 100vh !important;
  z-index: 999999 !important;
  background: white !important;
  overflow: hidden !important;
  margin: 0 !important;
  padding: 0 !important;
}

/* Main container with two panels */
.login-container {
  display: flex;
  width: 100%;
  height: 100vh;
  background: white;
}

/* Left Panel - Illustration */
.left-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
  position: relative;
  overflow: hidden;
}

.illustration-container {
  position: relative;
  width: 400px;
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.main-circle {
  width: 200px;
  height: 200px;
  background: #e9ecef;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 2;
}

.laptop-icon {
  font-size: 60px;
  color: #6c757d;
  position: relative;
}

.user-profile {
  position: absolute;
  top: -10px;
  right: -10px;
  width: 30px;
  height: 30px;
  background: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #e9ecef;
}

.user-profile i {
  font-size: 16px;
  color: #6c757d;
}

/* Decorative shapes */
.shape {
  position: absolute;
  border: 2px solid;
  border-radius: 50%;
}

.shape-1 {
  width: 40px;
  height: 40px;
  top: 20%;
  left: 10%;
  border-color: #a8d8ea;
  background: rgba(168, 216, 234, 0.1);
}

.shape-2 {
  width: 30px;
  height: 30px;
  top: 30%;
  right: 15%;
  border-color: #a8e6cf;
  background: rgba(168, 230, 207, 0.1);
  border-radius: 0;
  transform: rotate(45deg);
}

.shape-3 {
  width: 25px;
  height: 25px;
  bottom: 25%;
  left: 20%;
  border-color: #d1d5db;
  background: rgba(209, 213, 219, 0.1);
  border-radius: 0;
}

.shape-4 {
  width: 35px;
  height: 35px;
  bottom: 20%;
  right: 25%;
  border-color: #a8e6cf;
  background: rgba(168, 230, 207, 0.1);
  border-radius: 0;
  transform: rotate(45deg);
}

.shape-5 {
  width: 20px;
  height: 20px;
  top: 60%;
  left: 5%;
  border-color: #a8d8ea;
  background: rgba(168, 216, 234, 0.1);
}

/* Right Panel - Login Form */
.right-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  padding: 60px;
  border-left: 1px solid #e9ecef;
}

.login-form-container {
  width: 100%;
  max-width: 400px;
}

.form-header {
  margin-bottom: 40px;
  text-align: center;
}

.logo-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
}

.logo-image {
  width: 160px;
  height: 160px;
  object-fit: contain;
  margin-bottom: 24px;
  filter: drop-shadow(0 8px 16px rgba(0, 0, 0, 0.2));
}

.form-header h1 {
  font-size: 36px;
  font-weight: 700;
  color: #212529;
  margin: 0;
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.welcome-text {
  font-size: 16px;
  color: #6c757d;
  margin: 8px 0 0 0;
  font-weight: 400;
}

/* Form */
.login-form {
  margin-bottom: 30px;
}

.input-group {
  margin-bottom: 20px;
}

.input-container {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 16px;
  color: #6c757d;
  font-size: 16px;
  z-index: 2;
}

.form-input {
  width: 100%;
  padding: 16px 16px 16px 50px;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  font-size: 16px;
  transition: all 0.3s ease;
  background: white;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
}

.form-input::placeholder {
  color: #6c757d;
}

/* Login button */
.btn-login {
  width: 100%;
  padding: 16px 24px;
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  box-shadow: 0 4px 15px rgba(255, 107, 53, 0.3);
}

.btn-login:hover:not(:disabled) {
  background: linear-gradient(135deg, #e55a2b, #e8841a);
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(255, 107, 53, 0.4);
}

.btn-login:active:not(:disabled) {
  transform: translateY(0);
}

.btn-login:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

/* Form footer */
.form-footer {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.forgot-link {
  color: #6c757d;
  font-size: 14px;
  text-decoration: none;
  transition: color 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
  justify-content: center;
}

.forgot-link:hover {
  color: #ff6b35;
  text-decoration: underline;
}

/* Responsive */
@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
  }
  
  .left-panel {
    flex: 0 0 40%;
  }
  
  .right-panel {
    flex: 1;
    padding: 40px 30px;
  }
  
  .illustration-container {
    width: 300px;
    height: 300px;
  }
  
  .main-circle {
    width: 150px;
    height: 150px;
  }
  
  .laptop-icon {
    font-size: 40px;
  }
  
  .logo-image {
    width: 140px;
    height: 140px;
  }
}

@media (max-width: 480px) {
  .right-panel {
    padding: 30px 20px;
  }
  
  .logo-image {
    width: 120px;
    height: 120px;
  }
  
  .form-header h1 {
    font-size: 30px;
  }
  
  .form-input {
    padding: 14px 14px 14px 45px;
  }
  
  .btn-login {
    padding: 14px 20px;
  }
}

/* Loading animation */
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.fa-spinner {
  animation: spin 1s linear infinite;
}

/* Global styles to hide sidebar and other elements when login is active */
.login-fullscreen :global(.sidebar-container),
.login-fullscreen :global(.sidebar-header),
.login-fullscreen :global(.navigation-menu),
.login-fullscreen :global(.main-content),
.login-fullscreen :global(.header),
.login-fullscreen :global(.pos-header) {
  display: none !important;
}

.login-fullscreen :global(body) {
  overflow: hidden !important;
}

/* Force show sidebar when not on login page */
:global(body:not(.login-active)) .sidebar-container,
:global(body:not(.login-active)) .sidebar-header,
:global(body:not(.login-active)) .navigation-menu,
:global(body:not(.login-active)) .main-content,
:global(body:not(.login-active)) .header,
:global(body:not(.login-active)) .pos-header {
  display: block !important;
}
</style>

