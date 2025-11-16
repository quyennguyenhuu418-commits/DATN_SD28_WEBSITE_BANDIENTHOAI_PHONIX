<template>
  <div class="callback-page">
    <div class="callback-container">
      <div class="loading-spinner">
        <div class="spinner"></div>
        <h2>Đang xử lý đăng nhập Google...</h2>
        <p>Vui lòng chờ trong giây lát</p>
      </div>
    </div>
  </div>
</template>

<script>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCustomerAuthStore } from '@/stores/customerAuthStore'

export default {
  name: 'GoogleCallbackPage',
  setup() {
    const router = useRouter()
    const customerAuthStore = useCustomerAuthStore()

    onMounted(async () => {
      try {
        // Get parameters from URL
        const urlParams = new URLSearchParams(window.location.search)
        const token = urlParams.get('token')
        const error = urlParams.get('error')
        const code = urlParams.get('code')

        if (error) {
          console.error('Google OAuth error:', error)
          router.push(`/customer/login?error=${encodeURIComponent(error)}`)
          return
        }

        if (token) {
          // Token received from backend redirect
          try {
            // Decode token to get user info (you might want to verify token with backend)
            const response = await fetch('/api/customer/auth/me', {
              headers: {
                Authorization: `Bearer ${token}`,
              },
            })

            if (response.ok) {
              const userData = await response.json()

              // Store token and user data
              localStorage.setItem('customer_token', token)
              localStorage.setItem('customer_user', JSON.stringify(userData))

              // Redirect to home page
              router.push('/')
            } else {
              throw new Error('Invalid token')
            }
          } catch (err) {
            console.error('Token validation error:', err)
            router.push('/customer/login?error=invalid_token')
          }
        } else if (code) {
          // Authorization code received, process it with store
          console.log('Received code from Google:', code)
          const result = await customerAuthStore.googleLogin(code)

          if (result.success) {
            // Wait a bit for store to initialize
            await new Promise(resolve => setTimeout(resolve, 500))
            // Redirect to home page
            router.push('/').catch(() => {
              // If push fails, use replace
              window.location.href = '/'
            })
          } else {
            // Redirect to login with error
            const errorMsg = result.error || 'Đăng nhập Google thất bại'
            console.error('Google login failed:', errorMsg)
            router.push(`/customer/login?error=${encodeURIComponent(errorMsg)}`)
          }
        } else {
          console.error('No token or code received')
          router.push('/customer/login?error=no_token_or_code')
        }
      } catch (err) {
        console.error('Google callback error:', err)
        const errorMsg = err.message || 'Lỗi xử lý đăng nhập Google'
        router.push(`/customer/login?error=${encodeURIComponent(errorMsg)}`)
      }
    })

    return {}
  },
}
</script>

<style scoped>
.callback-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.callback-container {
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  padding: 40px;
  text-align: center;
  max-width: 400px;
  width: 100%;
}

.loading-spinner h2 {
  color: #333;
  margin: 20px 0 10px 0;
  font-size: 24px;
  font-weight: 600;
}

.loading-spinner p {
  color: #666;
  margin: 0;
  font-size: 16px;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #ff6b35;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
</style>
