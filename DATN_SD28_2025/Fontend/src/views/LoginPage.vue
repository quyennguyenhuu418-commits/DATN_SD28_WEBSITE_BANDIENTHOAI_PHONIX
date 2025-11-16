<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h1>PhoniX Admin</h1>
        <p>Đăng nhập vào hệ thống quản lý</p>
      </div>

      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label for="username">Tài khoản:</label>
          <input
            id="username"
            type="text"
            v-model="loginForm.username"
            required
            placeholder="Nhập tài khoản"
          />
        </div>

        <div class="form-group">
          <label for="password">Mật khẩu:</label>
          <input
            id="password"
            type="password"
            v-model="loginForm.password"
            required
            placeholder="Nhập mật khẩu"
          />
        </div>

        <div class="form-group">
          <label class="checkbox-label">
            <input type="checkbox" v-model="loginForm.rememberMe" />
            Ghi nhớ đăng nhập
          </label>
          <router-link to="/admin/forgot-password" class="forgot-password-link">
            Quên mật khẩu?
          </router-link>
        </div>

        <button type="submit" class="btn-login" :disabled="loading">
          <span v-if="loading">Đang đăng nhập...</span>
          <span v-else>Đăng Nhập</span>
        </button>
      </form>

      <div class="login-footer">
        <p>Demo: admin / admin123</p>
      </div>
    </div>

    <Toast ref="toastRef" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import api from '@/services/api'
import Toast from '@/components/Toast.vue'

const router = useRouter()
const authStore = useAuthStore()
const toastRef = ref<InstanceType<typeof Toast> | null>(null)
const loading = ref(false)

const loginForm = ref({
  username: '',
  password: '',
  rememberMe: false,
})

// Check if user is already authenticated and redirect to dashboard
onMounted(() => {
  if (authStore.isAuthenticated && authStore.user) {
    console.log('User already authenticated, redirecting to dashboard')
    router.push('/admin/dashboard')
  }
})

async function handleLogin() {
  if (!loginForm.value.username || !loginForm.value.password) {
    toastRef.value?.error('Lỗi', 'Vui lòng nhập đầy đủ tài khoản và mật khẩu!')
    return
  }

  loading.value = true

  try {
    // Gọi API đăng nhập
    const result = await authStore.login({
      username: loginForm.value.username.trim(),
      password: loginForm.value.password
    })

    if (result.success) {
      toastRef.value?.success('Thành công', 'Đăng nhập thành công!')

      // Redirect to dashboard immediately (don't wait for pending handover check)
      router.push('/admin/dashboard').catch((err) => {
        // Ignore navigation errors (e.g., already on dashboard)
        console.log('Navigation error (ignored):', err)
      })
    } else {
      const errorMsg = result.error || 'Tài khoản hoặc mật khẩu không đúng!'
      toastRef.value?.error('Lỗi đăng nhập', errorMsg)
    }
  } catch (error: any) {
    console.error('Login error:', error)
    const errorMessage = error.response?.data?.message || 
                        error.response?.data?.error || 
                        error.message || 
                        'Có lỗi xảy ra khi đăng nhập!'
    toastRef.value?.error('Lỗi', errorMessage)
  } finally {
    loading.value = false
  }
}


</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-card {
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  width: 100%;
  max-width: 400px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  color: #333;
  margin-bottom: 10px;
  font-size: 28px;
}

.login-header p {
  color: #666;
  margin: 0;
}

.login-form {
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
  color: #333;
}

.form-group input[type='text'],
.form-group input[type='password'] {
  width: 100%;
  padding: 12px;
  border: 2px solid #ddd;
  border-radius: 6px;
  font-size: 16px;
  transition: border-color 0.3s;
  box-sizing: border-box;
}

.form-group input[type='text']:focus,
.form-group input[type='password']:focus {
  outline: none;
  border-color: #007bff;
}

.checkbox-label {
  display: flex !important;
  align-items: center;
  font-weight: normal !important;
  cursor: pointer;
}

.form-group {
  position: relative;
}

.forgot-password-link {
  margin-left: auto;
  color: #007bff;
  font-size: 14px;
  text-decoration: none;
  transition: color 0.3s;
}

.forgot-password-link:hover {
  color: #0056b3;
  text-decoration: underline;
}

.checkbox-label input[type='checkbox'] {
  margin-right: 8px;
  width: auto;
}

.btn-login {
  width: 100%;
  padding: 12px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: background-color 0.3s;
}

.btn-login:hover:not(:disabled) {
  background: #0056b3;
}

.btn-login:disabled {
  background: #6c757d;
  cursor: not-allowed;
}

.login-footer {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.login-footer p {
  color: #666;
  font-size: 14px;
  margin: 0;
}

@media (max-width: 480px) {
  .login-card {
    padding: 30px 20px;
  }

  .login-header h1 {
    font-size: 24px;
  }
}
</style>
