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
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import Toast from '@/components/Toast.vue'

const router = useRouter()
const toastRef = ref<InstanceType<typeof Toast> | null>(null)
const loading = ref(false)

const loginForm = ref({
  username: '',
  password: '',
  rememberMe: false
})

async function handleLogin() {
  loading.value = true
  
  try {
    // Simulate login API call
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // Demo login - accept admin/admin123
    if (loginForm.value.username === 'admin' && loginForm.value.password === 'admin123') {
      // Save login state
      localStorage.setItem('isLoggedIn', 'true')
      localStorage.setItem('user', JSON.stringify({
        username: loginForm.value.username,
        role: 'admin'
      }))
      
      toastRef.value?.success('Thành công', 'Đăng nhập thành công!')
      
      // Redirect to dashboard
      setTimeout(() => {
        router.push('/dashboard')
      }, 1000)
    } else {
      toastRef.value?.error('Lỗi đăng nhập', 'Tài khoản hoặc mật khẩu không đúng!')
    }
  } catch (error) {
    toastRef.value?.error('Lỗi', 'Có lỗi xảy ra khi đăng nhập!')
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

.form-group input[type="text"],
.form-group input[type="password"] {
  width: 100%;
  padding: 12px;
  border: 2px solid #ddd;
  border-radius: 6px;
  font-size: 16px;
  transition: border-color 0.3s;
  box-sizing: border-box;
}

.form-group input[type="text"]:focus,
.form-group input[type="password"]:focus {
  outline: none;
  border-color: #007bff;
}

.checkbox-label {
  display: flex !important;
  align-items: center;
  font-weight: normal !important;
  cursor: pointer;
}

.checkbox-label input[type="checkbox"] {
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

