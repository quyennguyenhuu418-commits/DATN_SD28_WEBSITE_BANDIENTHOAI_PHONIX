<template>
  <div class="customer-forgot-password-container">
    <div class="forgot-password-card">
      <!-- Step 1: Enter Email -->
      <div v-if="step === 1" class="step-content">
        <div class="step-header">
          <h1>Quên mật khẩu</h1>
          <p>Nhập email của bạn để nhận mã OTP</p>
        </div>

        <form @submit.prevent="handleSendOTP" class="forgot-password-form">
          <div class="form-group">
            <label for="email">Email:</label>
            <input
              id="email"
              type="email"
              v-model="emailForm.email"
              required
              placeholder="Nhập email đã đăng ký"
              :disabled="loading"
            />
          </div>

          <div v-if="error" class="error-message">
            {{ error }}
          </div>

          <div v-if="successMessage" class="success-message">
            {{ successMessage }}
          </div>

          <button type="submit" class="btn-submit" :disabled="loading">
            <span v-if="loading">Đang gửi...</span>
            <span v-else>Gửi mã OTP</span>
          </button>
        </form>
      </div>

      <!-- Step 2: Enter OTP -->
      <div v-if="step === 2" class="step-content">
        <div class="step-header">
          <h1>Xác thực OTP</h1>
          <p>Nhập mã OTP đã được gửi đến email: <strong>{{ emailForm.email }}</strong></p>
        </div>

        <form @submit.prevent="handleVerifyOTP" class="forgot-password-form">
          <div class="form-group">
            <label for="otp">Mã OTP:</label>
            <input
              id="otp"
              type="text"
              v-model="otpForm.otp"
              required
              placeholder="Nhập mã OTP 5 chữ số"
              maxlength="5"
              :disabled="loading"
            />
            <p class="form-hint">Mã OTP có hiệu lực trong 5 phút</p>
          </div>

          <div v-if="error" class="error-message">
            {{ error }}
          </div>

          <div class="form-actions">
            <button type="button" class="btn-secondary" @click="resendOTP" :disabled="loading || resending">
              <span v-if="resending">Đang gửi lại...</span>
              <span v-else>Gửi lại mã OTP</span>
            </button>
            <button type="submit" class="btn-submit" :disabled="loading">
              <span v-if="loading">Đang xác thực...</span>
              <span v-else>Xác thực</span>
            </button>
          </div>
        </form>
      </div>

      <!-- Step 3: Reset Password -->
      <div v-if="step === 3" class="step-content">
        <div class="step-header">
          <h1>Đặt lại mật khẩu</h1>
          <p>Nhập mật khẩu mới cho tài khoản: <strong>{{ emailForm.email }}</strong></p>
        </div>

        <form @submit.prevent="handleResetPassword" class="forgot-password-form">
          <div class="form-group">
            <label for="newPassword">Mật khẩu mới:</label>
            <input
              id="newPassword"
              type="password"
              v-model="passwordForm.newPassword"
              required
              placeholder="Nhập mật khẩu mới (tối thiểu 6 ký tự)"
              minlength="6"
              :disabled="loading"
            />
          </div>

          <div class="form-group">
            <label for="confirmPassword">Xác nhận mật khẩu:</label>
            <input
              id="confirmPassword"
              type="password"
              v-model="passwordForm.confirmPassword"
              required
              placeholder="Nhập lại mật khẩu mới"
              minlength="6"
              :disabled="loading"
            />
          </div>

          <div v-if="error" class="error-message">
            {{ error }}
          </div>

          <div v-if="successMessage" class="success-message">
            {{ successMessage }}
          </div>

          <button type="submit" class="btn-submit" :disabled="loading">
            <span v-if="loading">Đang đặt lại...</span>
            <span v-else>Đặt lại mật khẩu</span>
          </button>
        </form>
      </div>

      <div class="forgot-password-footer">
        <p>
          Nhớ mật khẩu?
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
import { useRouter } from 'vue-router'
import { useCustomerAuthStore } from '@/stores/customerAuthStore'

const router = useRouter()
const customerAuthStore = useCustomerAuthStore()

const step = ref(1) // 1: Enter email, 2: Enter OTP, 3: Reset password
const loading = ref(false)
const resending = ref(false)
const error = ref('')
const successMessage = ref('')
const resetToken = ref('')

const emailForm = ref({
  email: ''
})

const otpForm = ref({
  otp: ''
})

const passwordForm = ref({
  newPassword: '',
  confirmPassword: ''
})

const handleSendOTP = async () => {
  loading.value = true
  error.value = ''
  successMessage.value = ''

  try {
    const result = await customerAuthStore.forgotPassword({
      email: emailForm.value.email
    })

    if (result.success) {
      successMessage.value = result.message || 'Mã OTP đã được gửi đến email của bạn'
      step.value = 2
    } else {
      error.value = result.message || 'Có lỗi xảy ra khi gửi OTP'
    }
  } catch (err) {
    error.value = err.response?.data?.message || err.message || 'Có lỗi xảy ra khi gửi OTP'
  } finally {
    loading.value = false
  }
}

const handleVerifyOTP = async () => {
  loading.value = true
  error.value = ''

  try {
    const result = await customerAuthStore.verifyOTP({
      email: emailForm.value.email,
      otp: otpForm.value.otp
    })

    if (result.success) {
      resetToken.value = result.resetToken
      step.value = 3
      successMessage.value = ''
    } else {
      error.value = result.message || 'Mã OTP không đúng hoặc đã hết hạn'
    }
  } catch (err) {
    error.value = err.response?.data?.message || err.message || 'Có lỗi xảy ra khi xác thực OTP'
  } finally {
    loading.value = false
  }
}

const resendOTP = async () => {
  resending.value = true
  error.value = ''

  try {
    const result = await customerAuthStore.forgotPassword({
      email: emailForm.value.email
    })

    if (result.success) {
      successMessage.value = 'Mã OTP mới đã được gửi đến email của bạn'
      setTimeout(() => {
        successMessage.value = ''
      }, 3000)
    } else {
      error.value = result.message || 'Có lỗi xảy ra khi gửi lại OTP'
    }
  } catch (err) {
    error.value = err.response?.data?.message || err.message || 'Có lỗi xảy ra khi gửi lại OTP'
  } finally {
    resending.value = false
  }
}

const handleResetPassword = async () => {
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    error.value = 'Mật khẩu xác nhận không khớp'
    return
  }

  if (passwordForm.value.newPassword.length < 6) {
    error.value = 'Mật khẩu phải có ít nhất 6 ký tự'
    return
  }

  loading.value = true
  error.value = ''
  successMessage.value = ''

  try {
    const result = await customerAuthStore.resetPassword({
      resetToken: resetToken.value,
      newPassword: passwordForm.value.newPassword,
      confirmPassword: passwordForm.value.confirmPassword
    })

    if (result.success) {
      successMessage.value = result.message || 'Đặt lại mật khẩu thành công! Đang chuyển hướng...'
      setTimeout(() => {
        router.push('/customer/login')
      }, 2000)
    } else {
      error.value = result.message || 'Có lỗi xảy ra khi đặt lại mật khẩu'
    }
  } catch (err) {
    error.value = err.response?.data?.message || err.message || 'Có lỗi xảy ra khi đặt lại mật khẩu'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.customer-forgot-password-container {
  min-height: calc(100vh - 200px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  margin-top: 77px; /* Account for fixed header */
}

.forgot-password-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  padding: 3rem;
  max-width: 500px;
  width: 100%;
}

.step-header {
  text-align: center;
  margin-bottom: 2rem;
}

.step-header h1 {
  color: #FF5500;
  font-size: 2rem;
  margin-bottom: 0.5rem;
  font-weight: 700;
}

.step-header p {
  color: #666;
  font-size: 0.95rem;
  margin-bottom: 0.5rem;
}

.step-header strong {
  color: #FF5500;
  font-weight: 600;
}

.forgot-password-form {
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

.form-hint {
  margin-top: 0.5rem;
  font-size: 0.85rem;
  color: #999;
  margin-bottom: 0;
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

.btn-submit {
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
  margin-bottom: 1rem;
}

.btn-submit:hover:not(:disabled) {
  background: #DC143C;
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(255, 85, 0, 0.3);
}

.btn-submit:disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: none;
}

.form-actions {
  display: flex;
  gap: 1rem;
}

.btn-secondary {
  flex: 1;
  padding: 1rem;
  background: white;
  color: #FF5500;
  border: 2px solid #FF5500;
  border-radius: 10px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-secondary:hover:not(:disabled) {
  background: #fff5f0;
  transform: translateY(-2px);
}

.btn-secondary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.forgot-password-footer {
  text-align: center;
  padding-top: 1.5rem;
  border-top: 1px solid #e0e0e0;
}

.forgot-password-footer p {
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
  .customer-forgot-password-container {
    padding: 1rem;
    margin-top: 67px;
  }

  .forgot-password-card {
    padding: 2rem 1.5rem;
  }

  .step-header h1 {
    font-size: 1.5rem;
  }

  .form-actions {
    flex-direction: column;
  }
}
</style>

