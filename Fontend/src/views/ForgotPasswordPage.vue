<template>
  <div class="login-fullscreen">
    <!-- Main login container with two panels -->
    <div class="login-container">
      <!-- Left Panel - Illustration -->
      <div class="left-panel">
        <div class="illustration-container">
          <div class="main-circle">
            <div class="laptop-icon">
              <i class="fas fa-key"></i>
              <div class="user-profile">
                <i class="fas fa-shield-alt"></i>
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

      <!-- Right Panel - Reset Password Form -->
      <div class="right-panel">
        <div class="login-form-container">
          <div class="form-header">
            <div class="logo-container">
              <img src="/logo.png" alt="PhoniX Logo" class="logo-image" />
              <h1>PhoniX Store</h1>
            </div>
            <p class="welcome-text">Khôi phục mật khẩu tài khoản</p>
          </div>

          <!-- Step 1: Enter Email -->
          <div v-if="step === 1" class="step-content">
            <div class="step-indicator">
              <div class="step active">1</div>
              <div class="step-line"></div>
              <div class="step">2</div>
              <div class="step-line"></div>
              <div class="step">3</div>
            </div>
            
            <form @submit.prevent="sendOtp" class="login-form">
              <div class="input-group">
                <div class="input-container">
                  <i class="fas fa-envelope input-icon"></i>
                  <input
                    id="email"
                    type="email"
                    v-model="form.email"
                    required
                    placeholder="Email đăng ký"
                    class="form-input"
                  />
                </div>
              </div>

              <div v-if="error" class="error-message">
                <i class="fas fa-exclamation-circle"></i>
                {{ error }}
              </div>
              
              <div v-if="success" class="success-message">
                <i class="fas fa-check-circle"></i>
                {{ success }}
              </div>

              <button type="submit" class="btn-login" :disabled="loading || !form.email">
                <i v-if="loading" class="fas fa-spinner fa-spin"></i>
                <i v-else class="fas fa-paper-plane"></i>
                <span v-if="loading">Đang gửi...</span>
                <span v-else>Gửi mã OTP</span>
              </button>
            </form>
          </div>

          <!-- Step 2: Enter OTP -->
          <div v-if="step === 2" class="step-content">
            <div class="step-indicator">
              <div class="step completed">1</div>
              <div class="step-line completed"></div>
              <div class="step active">2</div>
              <div class="step-line"></div>
              <div class="step">3</div>
            </div>
            
            <div class="otp-info">
              <i class="fas fa-shield-alt"></i>
              <p>Chúng tôi đã gửi mã OTP 6 chữ số đến email:</p>
              <strong>{{ form.email }}</strong>
            </div>
            
            <form @submit.prevent="verifyOtp" class="login-form">
              <div class="input-group">
                <div class="input-container">
                  <i class="fas fa-key input-icon"></i>
                  <input
                    id="otp"
                    type="text"
                    v-model="form.otp"
                    required
                    placeholder="Nhập mã OTP 6 chữ số"
                    class="form-input otp-input"
                    maxlength="6"
                  />
                </div>
              </div>

              <div v-if="error" class="error-message">
                <i class="fas fa-exclamation-circle"></i>
                {{ error }}
              </div>
              
              <div v-if="success" class="success-message">
                <i class="fas fa-check-circle"></i>
                {{ success }}
              </div>

              <button type="submit" class="btn-login" :disabled="loading || !form.otp || form.otp.length !== 6">
                <i v-if="loading" class="fas fa-spinner fa-spin"></i>
                <i v-else class="fas fa-check"></i>
                <span v-if="loading">Đang xác thực...</span>
                <span v-else>Xác thực OTP</span>
              </button>
              
              <div class="resend-section">
                <p>Không nhận được mã?</p>
                <button type="button" @click="resendOtp" class="resend-btn" :disabled="resendCooldown > 0">
                  <span v-if="resendCooldown > 0">Gửi lại sau {{ resendCooldown }}s</span>
                  <span v-else>Gửi lại mã OTP</span>
                </button>
              </div>
            </form>
          </div>

          <!-- Step 3: Reset Password -->
          <div v-if="step === 3" class="step-content">
            <div class="step-indicator">
              <div class="step completed">1</div>
              <div class="step-line completed"></div>
              <div class="step completed">2</div>
              <div class="step-line completed"></div>
              <div class="step active">3</div>
            </div>
            
            <div class="success-info">
              <i class="fas fa-check-circle"></i>
              <p>OTP đã được xác thực thành công!</p>
              <p>Bây giờ bạn có thể đặt lại mật khẩu mới.</p>
            </div>
            
            <form @submit.prevent="resetPassword" class="login-form">
              <div class="input-group">
                <div class="input-container">
                  <i class="fas fa-lock input-icon"></i>
                  <input
                    id="newPassword"
                    type="password"
                    v-model="form.newPassword"
                    required
                    placeholder="Mật khẩu mới"
                    class="form-input"
                  />
                </div>
              </div>

              <div class="input-group">
                <div class="input-container">
                  <i class="fas fa-lock input-icon"></i>
                  <input
                    id="confirmPassword"
                    type="password"
                    v-model="form.confirmPassword"
                    required
                    placeholder="Xác nhận mật khẩu mới"
                    class="form-input"
                  />
                </div>
              </div>

              <div v-if="error" class="error-message">
                <i class="fas fa-exclamation-circle"></i>
                {{ error }}
              </div>
              
              <div v-if="success" class="success-message">
                <i class="fas fa-check-circle"></i>
                {{ success }}
              </div>

              <div class="form-actions">
                <button type="button" @click="goBack" class="btn-back">
                  <i class="fas fa-arrow-left"></i>
                  Quay lại
                </button>
                <button type="submit" class="btn-login" :disabled="loading || !isFormValid">
                  <i v-if="loading" class="fas fa-spinner fa-spin"></i>
                  <i v-else class="fas fa-save"></i>
                  <span v-if="loading">Đang cập nhật...</span>
                  <span v-else>Đặt lại mật khẩu</span>
                </button>
              </div>
            </form>
          </div>

          <!-- Back to Login -->
          <div class="login-footer">
            <p>Nhớ mật khẩu rồi?</p>
            <router-link to="/login" class="login-link">
              <i class="fas fa-arrow-left"></i>
              Quay lại đăng nhập
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/services/api'

const router = useRouter()
const step = ref(1)
const loading = ref(false)
const error = ref('')
const success = ref('')
const resendCooldown = ref(0)
let cooldownTimer: NodeJS.Timeout | null = null

onMounted(() => {
  // Add login-active class to body
  document.body.classList.add('login-active')
})

onUnmounted(() => {
  // Remove login-active class from body
  document.body.classList.remove('login-active')
})

const form = ref({
  email: '',
  otp: '',
  newPassword: '',
  confirmPassword: ''
})

const isFormValid = computed(() => {
  if (step.value === 3) {
    return form.value.newPassword && 
           form.value.confirmPassword && 
           form.value.newPassword === form.value.confirmPassword &&
           form.value.newPassword.length >= 6
  }
  return false
})

const sendOtp = async () => {
  loading.value = true
  error.value = ''
  success.value = ''

  try {
    const response = await api.post('/api/auth/forgot-password', {
      email: form.value.email
    })
    
    success.value = 'Mã OTP đã được gửi đến email của bạn!'
    step.value = 2
    startResendCooldown()
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Có lỗi xảy ra khi gửi mã OTP'
  } finally {
    loading.value = false
  }
}

const verifyOtp = async () => {
  loading.value = true
  error.value = ''
  success.value = ''

  try {
    const response = await api.post('/api/auth/verify-otp', {
      email: form.value.email,
      otp: form.value.otp
    })
    
    success.value = 'OTP đã được xác thực thành công!'
    setTimeout(() => {
      step.value = 3
      success.value = ''
    }, 1500)
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Mã OTP không đúng hoặc đã hết hạn'
  } finally {
    loading.value = false
  }
}

const resetPassword = async () => {
  loading.value = true
  error.value = ''
  success.value = ''

  try {
    const response = await api.post('/api/auth/reset-password', {
      email: form.value.email,
      otp: form.value.otp,
      newPassword: form.value.newPassword,
      confirmPassword: form.value.confirmPassword
    })
    
    success.value = 'Mật khẩu đã được đặt lại thành công!'
    setTimeout(() => {
      router.push('/login')
    }, 2000)
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Có lỗi xảy ra khi đặt lại mật khẩu'
  } finally {
    loading.value = false
  }
}

const resendOtp = async () => {
  if (resendCooldown.value > 0) return
  
  loading.value = true
  error.value = ''
  success.value = ''

  try {
    const response = await api.post('/api/auth/resend-otp', {
      email: form.value.email
    })
    
    success.value = 'Mã OTP mới đã được gửi!'
    startResendCooldown()
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Có lỗi xảy ra khi gửi lại mã OTP'
  } finally {
    loading.value = false
  }
}

const startResendCooldown = () => {
  resendCooldown.value = 60
  cooldownTimer = setInterval(() => {
    resendCooldown.value--
    if (resendCooldown.value <= 0) {
      clearInterval(cooldownTimer)
      cooldownTimer = null
    }
  }, 1000)
}

const goBack = () => {
  if (step.value === 2) {
    step.value = 1
    form.value.otp = ''
  } else if (step.value === 3) {
    step.value = 2
    form.value.newPassword = ''
    form.value.confirmPassword = ''
  }
  error.value = ''
  success.value = ''
}

onUnmounted(() => {
  if (cooldownTimer) {
    clearInterval(cooldownTimer)
  }
})
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

/* Additional styles for forgot password page */
.step-content {
  animation: slideIn 0.5s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* Step indicator */
.step-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32px;
}

.step {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
  background: #e2e8f0;
  color: #64748b;
  transition: all 0.3s ease;
}

.step.active {
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: white;
}

.step.completed {
  background: #10b981;
  color: white;
}

.step-line {
  width: 50px;
  height: 2px;
  background: #e2e8f0;
  margin: 0 8px;
  transition: all 0.3s ease;
}

.step-line.completed {
  background: #10b981;
}

/* OTP Info */
.otp-info {
  text-align: center;
  background: rgba(255, 107, 53, 0.1);
  border: 1px solid rgba(255, 107, 53, 0.2);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
}

.otp-info i {
  font-size: 24px;
  color: #ff6b35;
  margin-bottom: 12px;
}

.otp-info p {
  margin: 4px 0;
  color: #4a5568;
  font-size: 14px;
}

.otp-info strong {
  color: #2d3748;
  font-weight: 600;
}

/* Success Info */
.success-info {
  text-align: center;
  background: rgba(16, 185, 129, 0.1);
  border: 1px solid rgba(16, 185, 129, 0.2);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
}

.success-info i {
  font-size: 24px;
  color: #10b981;
  margin-bottom: 12px;
}

.success-info p {
  margin: 4px 0;
  color: #4a5568;
  font-size: 14px;
}

/* OTP Input */
.otp-input {
  text-align: center;
  font-size: 20px;
  letter-spacing: 6px;
  font-weight: 600;
}

/* Resend Section */
.resend-section {
  text-align: center;
  margin-top: 20px;
}

.resend-section p {
  color: #6c757d;
  font-size: 14px;
  margin-bottom: 8px;
}

.resend-btn {
  background: none;
  border: none;
  color: #ff6b35;
  font-size: 14px;
  cursor: pointer;
  text-decoration: underline;
  transition: color 0.3s ease;
}

.resend-btn:hover:not(:disabled) {
  color: #e55a2b;
}

.resend-btn:disabled {
  color: #a0aec0;
  cursor: not-allowed;
  text-decoration: none;
}

/* Form actions */
.form-actions {
  display: flex;
  gap: 16px;
  margin-top: 24px;
}

.btn-back {
  background: none;
  border: 2px solid #e2e8f0;
  color: #6c757d;
  padding: 12px 20px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  justify-content: center;
}

.btn-back:hover {
  border-color: #ff6b35;
  color: #ff6b35;
}

.form-actions .btn-login {
  flex: 2;
}

/* Messages */
.error-message {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.2);
  border-radius: 8px;
  color: #dc2626;
  font-size: 14px;
  margin-bottom: 16px;
}

.success-message {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: rgba(16, 185, 129, 0.1);
  border: 1px solid rgba(16, 185, 129, 0.2);
  border-radius: 8px;
  color: #059669;
  font-size: 14px;
  margin-bottom: 16px;
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
  
  .form-actions {
    flex-direction: column;
  }
  
  .btn-back {
    flex: none;
  }
  
  .form-actions .btn-login {
    flex: none;
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
</style>
