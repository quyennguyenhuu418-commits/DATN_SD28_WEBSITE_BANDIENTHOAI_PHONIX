<template>
  <div class="customer-login-container">
    <div class="login-card">
      <div class="login-header">
        <h1>Đăng nhập</h1>
        <p>Chào mừng bạn quay trở lại PhoniX Store</p>
      </div>

      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label for="email">Email:</label>
          <input
            id="email"
            type="email"
            v-model="loginForm.email"
            required
            placeholder="Nhập email"
            :disabled="loading"
          />
        </div>

        <div class="form-group">
          <label for="matKhau">Mật khẩu:</label>
          <input
            id="matKhau"
            type="password"
            v-model="loginForm.matKhau"
            required
            placeholder="Nhập mật khẩu"
            :disabled="loading"
          />
        </div>

        <div class="form-options">
          <label class="checkbox-label">
            <input type="checkbox" v-model="loginForm.rememberMe" />
            Ghi nhớ đăng nhập
          </label>
          <router-link to="/customer/forgot-password" class="forgot-password-link">
            Quên mật khẩu?
          </router-link>
        </div>

        <div v-if="error" class="error-message">
          {{ error }}
        </div>

        <button type="submit" class="btn-login" :disabled="loading">
          <span v-if="loading">Đang đăng nhập...</span>
          <span v-else>Đăng Nhập</span>
        </button>
      </form>

      <!-- Divider -->
      <div class="divider">
        <span>Hoặc</span>
      </div>

      <!-- Google Login Button -->
      <button @click="handleGoogleLogin" class="btn-google" :disabled="loading">
        <svg class="google-icon" viewBox="0 0 24 24" width="20" height="20">
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
            d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.16H2.18C1.43 8.66 1 10.29 1 12s.43 3.34 1.18 4.84l2.85-2.84.81-.91z"
          />
          <path
            fill="#EA4335"
            d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.16l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
          />
        </svg>
        <span v-if="loading">Đang đăng nhập...</span>
        <span v-else>Đăng nhập với Google</span>
      </button>

      <div class="login-footer">
        <p>
          Chưa có tài khoản?
          <router-link to="/customer/register" class="register-link">
            Đăng ký ngay
          </router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useCustomerAuthStore } from '@/stores/customerAuthStore'

const router = useRouter()
const route = useRoute()
const customerAuthStore = useCustomerAuthStore()

const loading = ref(false)
const error = ref('')

const loginForm = ref({
  email: '',
  matKhau: '',
  rememberMe: false
})

// Check if redirected from Google OAuth
onMounted(async () => {
  const code = route.query.code
  if (code) {
    await handleGoogleCallback(code)
  }
})

const handleLogin = async () => {
  loading.value = true
  error.value = ''

  try {
    const result = await customerAuthStore.login({
      taiKhoan: loginForm.value.email, // Dùng email làm tài khoản
      matKhau: loginForm.value.matKhau
    })

    if (result.success) {
      // Redirect to previous page or home
      const redirectTo = route.query.redirect || '/'
      router.push(redirectTo)
    } else {
      error.value = result.error || 'Đăng nhập thất bại'
    }
  } catch (err) {
    error.value = err.message || 'Có lỗi xảy ra khi đăng nhập'
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
    
    if (result.success && result.data?.googleUrl) {
      // Redirect to Google OAuth
      window.location.href = result.data.googleUrl
    } else {
      error.value = 'Không thể lấy Google OAuth URL'
      loading.value = false
    }
  } catch (err) {
    error.value = err.message || 'Có lỗi xảy ra khi đăng nhập Google'
    loading.value = false
  }
}

const handleGoogleCallback = async (code) => {
  loading.value = true
  error.value = ''

  try {
    const result = await customerAuthStore.googleLogin(code)

    if (result.success) {
      // Remove code from URL
      router.replace({ query: {} })
      // Redirect to previous page or home
      const redirectTo = route.query.redirect || '/'
      router.push(redirectTo)
    } else {
      error.value = result.error || 'Đăng nhập Google thất bại'
      loading.value = false
    }
  } catch (err) {
    error.value = err.message || 'Có lỗi xảy ra khi đăng nhập Google'
    loading.value = false
  }
}
</script>

<style scoped>
.customer-login-container {
  min-height: calc(100vh - 200px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  margin-top: 77px; /* Account for fixed header */
}

.login-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  padding: 3rem;
  max-width: 450px;
  width: 100%;
}

.login-header {
  text-align: center;
  margin-bottom: 2rem;
}

.login-header h1 {
  color: #FF5500;
  font-size: 2rem;
  margin-bottom: 0.5rem;
  font-weight: 700;
}

.login-header p {
  color: #666;
  font-size: 0.95rem;
}

.login-form {
  margin-bottom: 1.5rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #333;
  font-weight: 500;
  font-size: 0.9rem;
}

.form-group input {
  width: 100%;
  padding: 0.875rem 1rem;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  font-size: 1rem;
  transition: all 0.3s;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #FF5500;
  box-shadow: 0 0 0 3px rgba(255, 85, 0, 0.1);
}

.form-group input:disabled {
  background: #f5f5f5;
  cursor: not-allowed;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  font-size: 0.9rem;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  color: #666;
}

.checkbox-label input[type="checkbox"] {
  width: auto;
  cursor: pointer;
}

.forgot-password-link {
  color: #FF5500;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s;
}

.forgot-password-link:hover {
  color: #DC143C;
  text-decoration: underline;
}

.error-message {
  background: #fee;
  color: #c33;
  padding: 0.75rem 1rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  font-size: 0.9rem;
  border: 1px solid #fcc;
}

.btn-login {
  width: 100%;
  padding: 1rem;
  background: #FF5500;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 1.5rem;
}

.btn-login:hover:not(:disabled) {
  background: #DC143C;
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(255, 85, 0, 0.3);
}

.btn-login:disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: none;
}

.divider {
  text-align: center;
  margin: 1.5rem 0;
  position: relative;
}

.divider::before,
.divider::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 45%;
  height: 1px;
  background: #e0e0e0;
}

.divider::before {
  left: 0;
}

.divider::after {
  right: 0;
}

.divider span {
  background: white;
  padding: 0 1rem;
  color: #999;
  font-size: 0.9rem;
}

.btn-google {
  width: 100%;
  padding: 1rem;
  background: white;
  color: #333;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
}

.btn-google:hover:not(:disabled) {
  border-color: #4285F4;
  box-shadow: 0 5px 15px rgba(66, 133, 244, 0.2);
  transform: translateY(-2px);
}

.btn-google:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.google-icon {
  flex-shrink: 0;
}

.login-footer {
  text-align: center;
  padding-top: 1.5rem;
  border-top: 1px solid #e0e0e0;
}

.login-footer p {
  color: #666;
  font-size: 0.9rem;
  margin: 0;
}

.register-link {
  color: #FF5500;
  text-decoration: none;
  font-weight: 600;
  margin-left: 0.25rem;
  transition: color 0.3s;
}

.register-link:hover {
  color: #DC143C;
  text-decoration: underline;
}

@media (max-width: 768px) {
  .customer-login-container {
    padding: 1rem;
    margin-top: 67px;
  }

  .login-card {
    padding: 2rem 1.5rem;
  }

  .login-header h1 {
    font-size: 1.5rem;
  }
}
</style>

