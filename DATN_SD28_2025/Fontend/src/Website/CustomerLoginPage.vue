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
              <img src="/Logo2.png" alt="PhoniX Logo" class="logo-image" />
              <h1>PhoniX Store</h1>
            </div>
            <p class="welcome-text">Chào mừng đến với PhoniX</p>
          </div>

          <form @submit.prevent="handleJwtLogin" class="login-form">
            <div class="input-group">
              <div class="input-container">
                <i class="fas fa-envelope input-icon"></i>
                <input
                  id="taiKhoan"
                  type="text"
                  v-model="jwtForm.taiKhoan"
                  required
                  placeholder="Tài khoản / Email"
                  class="form-input"
                />
              </div>
            </div>

            <div class="input-group">
              <div class="input-container">
                <i class="fas fa-lock input-icon"></i>
                <input
                  id="matKhau"
                  type="password"
                  v-model="jwtForm.matKhau"
                  required
                  placeholder="Mật khẩu"
                  class="form-input"
                />
              </div>
            </div>

            <button type="submit" class="btn-login" :disabled="loading">
              <span v-if="loading">Đang đăng nhập...</span>
              <span v-else>ĐĂNG NHẬP</span>
            </button>
          </form>

          <!-- Divider -->
          <div class="divider">
            <span>Hoặc</span>
          </div>

          <!-- Google Login -->
          <button @click="handleGoogleLogin" class="btn-google" :disabled="loading">
            <svg class="google-icon" viewBox="0 0 24 24">
              <path
                fill="#4285F4"
                d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
              />
              <path
                fill="#34A853"
                d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
              />
              <path
                fill="#FBBC05"
                d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"
              />
              <path
                fill="#EA4335"
                d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
              />
            </svg>
            <span v-if="loading">Đang đăng nhập...</span>
            <span v-else>Đăng nhập với Google</span>
          </button>

          <div class="form-footer">
            <div class="footer-links">
              <router-link to="/customer/forgot-password" class="forgot-link">
                <i class="fas fa-key"></i>
                Quên tài khoản / mật khẩu?
              </router-link>
              <p class="register-text">
                Chưa có tài khoản? <router-link to="/customer/register">Đăng ký ngay</router-link>
              </p>
            </div>
          </div>

          <!-- Error Message -->
          <div v-if="error" class="error-message">
            {{ error }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCustomerAuthStore } from '@/stores/customerAuthStore'

const router = useRouter()
const customerAuthStore = useCustomerAuthStore()

// State
const loading = ref(false)
const error = ref('')

// JWT Form
const jwtForm = reactive({
  taiKhoan: '',
  matKhau: '',
})

onMounted(() => {
  // Add login-active class to body
  document.body.classList.add('login-active')
})

onUnmounted(() => {
  // Remove login-active class from body
  document.body.classList.remove('login-active')
})

// Methods
const handleJwtLogin = async () => {
  loading.value = true
  error.value = ''

  try {
    const result = await customerAuthStore.login(jwtForm)

    if (result.success) {
      // Redirect to homepage or intended page
      router.push('/')
    } else {
      error.value = result.error
    }
  } catch (err) {
    error.value = 'Có lỗi xảy ra khi đăng nhập'
  } finally {
    loading.value = false
  }
}

const handleGoogleLogin = async () => {
  loading.value = true
  error.value = ''

  try {
    // Get Google OAuth URL from backend
    const result = await customerAuthStore.getGoogleOAuthUrl()

    if (result.success) {
      console.log('Google OAuth URL:', result.data.googleUrl)
      // Redirect to Google OAuth URL
      window.location.href = result.data.googleUrl
    } else {
      error.value = result.error || 'Không thể lấy Google OAuth URL'
    }
  } catch (err) {
    error.value = 'Có lỗi xảy ra khi đăng nhập với Google'
    console.error('Google login error:', err)
  } finally {
    loading.value = false
  }
}

const loadGoogleAPI = () => {
  return new Promise((resolve, reject) => {
    if (window.gapi) {
      resolve()
      return
    }

    const script = document.createElement('script')
    script.src = 'https://apis.google.com/js/api.js'
    script.onload = () => {
      window.gapi.load('auth2', resolve)
      window.gapi.load('client', resolve)
    }
    script.onerror = reject
    document.head.appendChild(script)
  })
}

const initializeGoogleAuth = async () => {
  try {
    // Sử dụng biến môi trường hoặc fallback
    const clientId =
      import.meta.env.VITE_GOOGLE_CLIENT_ID || 'YOUR_ACTUAL_CLIENT_ID.apps.googleusercontent.com'
    const apiKey = import.meta.env.VITE_GOOGLE_API_KEY || 'AIzaSyD07RFQhglQJF61d19IF83TxYUIn-ZYsDY'

    await window.gapi.client.init({
      apiKey: apiKey,
      clientId: clientId,
      discoveryDocs: ['https://www.googleapis.com/discovery/v1/apis/oauth2/v2/rest'],
      scope: 'email profile openid',
    })

    return window.gapi.auth2.getAuthInstance()
  } catch (err) {
    console.error('Google Auth initialization error:', err)
    throw new Error('Không thể khởi tạo Google Auth. Vui lòng kiểm tra cấu hình Google OAuth.')
  }
}

const signInWithGoogle = (authInstance) => {
  return new Promise((resolve) => {
    authInstance
      .signIn()
      .then((googleUser) => {
        const token = googleUser.getAuthResponse().id_token
        resolve({ success: true, token })
      })
      .catch((err) => {
        resolve({ success: false, error: 'Đăng nhập Google thất bại' })
      })
  })
}
</script>

<style scoped>
/* Fullscreen login container */
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
  width: 400px;
  padding: 16px 16px 16px 20px;
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

.divider {
  text-align: center;
  margin: 30px 0;
  position: relative;
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: #e1e5e9;
}

.divider span {
  background: white;
  padding: 0 20px;
  color: #666;
  font-size: 14px;
}

.btn-google {
  width: 100%;
  padding: 14px;
  background: white;
  color: #333;
  border: 2px solid #e1e5e9;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.btn-google:hover:not(:disabled) {
  border-color: #4285f4;
  transform: translateY(-2px);
}

.btn-google:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.google-icon {
  width: 20px;
  height: 20px;
}

/* Form footer */
.form-footer {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.footer-links {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
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

.register-text {
  color: #6c757d;
  font-size: 14px;
  margin: 0;
  text-align: center;
}

.register-text a {
  color: #ff6b35;
  text-decoration: none;
  font-weight: 600;
}

.register-text a:hover {
  text-decoration: underline;
}

.error-message {
  background: #fee;
  color: #c33;
  padding: 12px;
  border-radius: 8px;
  text-align: center;
  font-size: 14px;
  margin-top: 15px;
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
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.fa-spinner {
  animation: spin 1s linear infinite;
}
</style>
