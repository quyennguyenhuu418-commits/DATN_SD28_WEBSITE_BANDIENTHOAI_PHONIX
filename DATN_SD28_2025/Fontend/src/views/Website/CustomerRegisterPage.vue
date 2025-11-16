<template>
  <div class="customer-register-container">
    <div class="register-card">
      <div class="register-header">
        <h1>Đăng ký tài khoản</h1>
        <p>Tạo tài khoản mới để trải nghiệm tốt hơn</p>
      </div>

      <form @submit.prevent="handleRegister" class="register-form">
        <div class="form-row">
          <div class="form-group">
            <label for="hoTen">Họ và tên <span class="required">*</span>:</label>
            <input
              id="hoTen"
              type="text"
              v-model="registerForm.hoTen"
              required
              placeholder="Nhập họ và tên"
              :disabled="loading"
            />
          </div>

          <div class="form-group">
            <label for="soDienThoai">Số điện thoại <span class="required">*</span>:</label>
            <input
              id="soDienThoai"
              type="tel"
              v-model="registerForm.soDienThoai"
              required
              placeholder="Nhập số điện thoại"
              :disabled="loading"
            />
          </div>
        </div>

        <div class="form-group">
          <label for="email">Email <span class="required">*</span>:</label>
          <input
            id="email"
            type="email"
            v-model="registerForm.email"
            required
            placeholder="Nhập email (dùng để đăng nhập)"
            :disabled="loading"
          />
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="matKhau">Mật khẩu <span class="required">*</span>:</label>
            <input
              id="matKhau"
              type="password"
              v-model="registerForm.matKhau"
              required
              placeholder="Nhập mật khẩu"
              :disabled="loading"
              minlength="6"
            />
          </div>

          <div class="form-group">
            <label for="confirmMatKhau">Xác nhận mật khẩu <span class="required">*</span>:</label>
            <input
              id="confirmMatKhau"
              type="password"
              v-model="registerForm.confirmMatKhau"
              required
              placeholder="Nhập lại mật khẩu"
              :disabled="loading"
              minlength="6"
            />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="ngaySinh">Ngày sinh:</label>
            <input
              id="ngaySinh"
              type="date"
              v-model="registerForm.ngaySinh"
              :disabled="loading"
            />
          </div>

          <div class="form-group">
            <label for="gioiTinh">Giới tính:</label>
            <select
              id="gioiTinh"
              v-model="registerForm.gioiTinh"
              :disabled="loading"
            >
              <option value="">Chọn giới tính</option>
              <option value="Nam">Nam</option>
              <option value="Nữ">Nữ</option>
              <option value="Khác">Khác</option>
            </select>
          </div>
        </div>

        <div v-if="error" class="error-message">
          {{ error }}
        </div>

        <div v-if="successMessage" class="success-message">
          {{ successMessage }}
        </div>

        <button type="submit" class="btn-register" :disabled="loading">
          <span v-if="loading">Đang đăng ký...</span>
          <span v-else>Đăng Ký</span>
        </button>
      </form>

      <!-- Divider -->
      <div class="divider">
        <span>Hoặc</span>
      </div>

      <!-- Google Register Button -->
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
        <span v-if="loading">Đang đăng ký...</span>
        <span v-else>Đăng ký với Google</span>
      </button>

      <div class="register-footer">
        <p>
          Đã có tài khoản?
          <router-link to="/customer/login" class="login-link">
            Đăng nhập ngay
          </router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useCustomerAuthStore } from '@/stores/customerAuthStore'

const router = useRouter()
const route = useRoute()
const customerAuthStore = useCustomerAuthStore()

const loading = ref(false)
const error = ref('')
const successMessage = ref('')

const registerForm = ref({
  hoTen: '',
  soDienThoai: '',
  email: '',
  taiKhoan: '',
  matKhau: '',
  confirmMatKhau: '',
  ngaySinh: '',
  gioiTinh: ''
})

const handleRegister = async () => {
  loading.value = true
  error.value = ''
  successMessage.value = ''

  // Validation
  if (registerForm.value.matKhau !== registerForm.value.confirmMatKhau) {
    error.value = 'Mật khẩu xác nhận không khớp'
    loading.value = false
    return
  }

  if (registerForm.value.matKhau.length < 6) {
    error.value = 'Mật khẩu phải có ít nhất 6 ký tự'
    loading.value = false
    return
  }

  try {
    const result = await customerAuthStore.register({
      hoTen: registerForm.value.hoTen,
      soDienThoai: registerForm.value.soDienThoai,
      email: registerForm.value.email,
      taiKhoan: registerForm.value.email, // Dùng email làm tài khoản
      matKhau: registerForm.value.matKhau,
      ngaySinh: registerForm.value.ngaySinh || null,
      gioiTinh: registerForm.value.gioiTinh || null
    })

    if (result.success) {
      successMessage.value = 'Đăng ký thành công! Đang chuyển hướng...'
      // Redirect to previous page or home after 1 second
      setTimeout(() => {
        const redirectTo = route.query.redirect || '/'
        router.push(redirectTo)
      }, 1000)
    } else {
      error.value = result.error || 'Đăng ký thất bại'
      loading.value = false
    }
  } catch (err) {
    error.value = err.response?.data?.message || err.message || 'Có lỗi xảy ra khi đăng ký'
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
    error.value = err.message || 'Có lỗi xảy ra khi đăng ký Google'
    loading.value = false
  }
}
</script>

<style scoped>
.customer-register-container {
  min-height: calc(100vh - 200px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  margin-top: 77px; /* Account for fixed header */
}

.register-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  padding: 3rem;
  max-width: 600px;
  width: 100%;
}

.register-header {
  text-align: center;
  margin-bottom: 2rem;
}

.register-header h1 {
  color: #FF5500;
  font-size: 2rem;
  margin-bottom: 0.5rem;
  font-weight: 700;
}

.register-header p {
  color: #666;
  font-size: 0.95rem;
}

.register-form {
  margin-bottom: 1.5rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  margin-bottom: 1rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #333;
  font-weight: 500;
  font-size: 0.9rem;
}

.required {
  color: #DC143C;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 0.875rem 1rem;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  font-size: 1rem;
  transition: all 0.3s;
  box-sizing: border-box;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #FF5500;
  box-shadow: 0 0 0 3px rgba(255, 85, 0, 0.1);
}

.form-group input:disabled,
.form-group select:disabled {
  background: #f5f5f5;
  cursor: not-allowed;
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

.success-message {
  background: #efe;
  color: #3c3;
  padding: 0.75rem 1rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  font-size: 0.9rem;
  border: 1px solid #cfc;
}

.btn-register {
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

.btn-register:hover:not(:disabled) {
  background: #DC143C;
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(255, 85, 0, 0.3);
}

.btn-register:disabled {
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

.register-footer {
  text-align: center;
  padding-top: 1.5rem;
  border-top: 1px solid #e0e0e0;
}

.register-footer p {
  color: #666;
  font-size: 0.9rem;
  margin: 0;
}

.login-link {
  color: #FF5500;
  text-decoration: none;
  font-weight: 600;
  margin-left: 0.25rem;
  transition: color 0.3s;
}

.login-link:hover {
  color: #DC143C;
  text-decoration: underline;
}

@media (max-width: 768px) {
  .customer-register-container {
    padding: 1rem;
    margin-top: 67px;
  }

  .register-card {
    padding: 2rem 1.5rem;
  }

  .register-header h1 {
    font-size: 1.5rem;
  }

  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>

