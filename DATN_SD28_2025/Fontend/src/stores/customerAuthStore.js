import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/services/api'

export const useCustomerAuthStore = defineStore('customerAuth', () => {
  // State
  const token = ref(localStorage.getItem('customer_token') || null)
  const user = ref(JSON.parse(localStorage.getItem('customer_user') || 'null'))
  const loading = ref(false)
  const error = ref(null)

  // Getters
  const isAuthenticated = computed(() => {
    const hasToken = !!token.value
    const hasUser = !!user.value && user.value !== null
    const isAuth = hasToken && hasUser
    return isAuth
  })

  const isCustomer = computed(
    () => user.value?.role === 'CUSTOMER' || user.value?.role === 'Khách hàng',
  )
  const userName = computed(
    () => user.value?.hoTen || user.value?.tenKhachHang || user.value?.username || 'Khách hàng',
  )
  const userEmail = computed(() => user.value?.email || '')
  const userId = computed(() => user.value?.id || user.value?.khachHangId || null)
  const customer = computed(() => user.value)

  // Actions
  const login = async (credentials) => {
    loading.value = true
    error.value = null

    try {
      const response = await api.post('/api/customer/auth/login', credentials)

      if (response.data.success) {
        const { token: newToken, user: userData } = response.data

        // Store token and user data
        token.value = newToken
        user.value = userData

        // Persist to localStorage with customer prefix
        localStorage.setItem('customer_token', newToken)
        localStorage.setItem('customer_user', JSON.stringify(userData))

        // Set default authorization header
        api.defaults.headers.common['Authorization'] = `Bearer ${newToken}`

        return { success: true, data: response.data }
      } else {
        error.value = response.data.message || 'Đăng nhập thất bại'
        return { success: false, error: error.value }
      }
    } catch (err) {
      error.value = err.response?.data?.message || 'Đăng nhập thất bại'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const googleLogin = async (code) => {
    loading.value = true
    error.value = null

    try {
      if (!code || code.trim() === '') {
        throw new Error('Authorization code không hợp lệ')
      }

      const response = await api.post('/api/customer/auth/google-login', {
        code: code,
      })

      if (!response.data) {
        throw new Error('Không nhận được phản hồi từ server')
      }

      if (response.data.success === true) {
        const { token: newToken, user: userData } = response.data

        if (!newToken || !userData) {
          throw new Error('Thông tin đăng nhập không đầy đủ')
        }

        // Store token and user data
        token.value = newToken
        user.value = userData

        // Persist to localStorage with customer prefix
        localStorage.setItem('customer_token', newToken)
        localStorage.setItem('customer_user', JSON.stringify(userData))

        // Set default authorization header
        api.defaults.headers.common['Authorization'] = `Bearer ${newToken}`

        return { success: true, data: response.data }
      } else {
        const errorMsg = response.data.message || response.data.error || 'Đăng nhập Google thất bại'
        error.value = errorMsg
        return { success: false, error: errorMsg }
      }
    } catch (err) {
      console.error('Google login error:', err)
      const errorMsg = err.response?.data?.message || 
                      err.response?.data?.error || 
                      err.message || 
                      'Đăng nhập Google thất bại'
      error.value = errorMsg
      return { success: false, error: errorMsg }
    } finally {
      loading.value = false
    }
  }

  const getGoogleOAuthUrl = async () => {
    try {
      const response = await api.get('/api/customer/auth/google-url')
      return { success: true, data: response.data }
    } catch (err) {
      return {
        success: false,
        error: err.response?.data?.message || 'Không thể lấy Google OAuth URL',
      }
    }
  }

  const register = async (userData) => {
    loading.value = true
    error.value = null

    try {
      const response = await api.post('/api/customer/auth/register', userData)
      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.message || 'Đăng ký thất bại'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  const logout = async () => {
    loading.value = true

    try {
      // Call logout endpoint (optional)
      await api.post('/api/customer/auth/logout')
    } catch (err) {
      console.warn('Customer logout API call failed:', err)
    } finally {
      // Clear local state regardless of API call result
      token.value = null
      user.value = null
      error.value = null

      // Clear localStorage
      localStorage.removeItem('customer_token')
      localStorage.removeItem('customer_user')

      // Remove authorization header
      delete api.defaults.headers.common['Authorization']

      loading.value = false
    }
  }

  const refreshToken = async () => {
    if (!token.value) return { success: false, error: 'No token to refresh' }

    try {
      const response = await api.post(
        '/api/customer/auth/refresh',
        {},
        {
          headers: { Authorization: `Bearer ${token.value}` },
        },
      )

      const { token: newToken, user: userData } = response.data

      // Update stored data
      token.value = newToken
      user.value = userData

      // Update localStorage
      localStorage.setItem('customer_token', newToken)
      localStorage.setItem('customer_user', JSON.stringify(userData))

      // Update authorization header
      api.defaults.headers.common['Authorization'] = `Bearer ${newToken}`

      return { success: true, data: response.data }
    } catch (err) {
      // If refresh fails, logout user
      await logout()
      return { success: false, error: 'Token refresh failed' }
    }
  }

  const forgotPassword = async (emailData) => {
    loading.value = true
    error.value = null

    try {
      const response = await api.post('/api/customer/auth/forgot-password', emailData)
      return response.data
    } catch (err) {
      console.error('Customer forgot password error:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const verifyOTP = async (otpData) => {
    loading.value = true
    error.value = null

    try {
      const response = await api.post('/api/customer/auth/verify-otp', otpData)
      return response.data
    } catch (err) {
      console.error('Customer verify OTP error:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const resetPassword = async (resetData) => {
    loading.value = true
    error.value = null

    try {
      const response = await api.post('/api/customer/auth/reset-password', resetData)
      return response.data
    } catch (err) {
      console.error('Customer reset password error:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const getCurrentUser = async () => {
    if (!token.value) return { success: false, error: 'No token' }

    try {
      const response = await api.get('/api/customer/auth/me')
      user.value = response.data
      localStorage.setItem('customer_user', JSON.stringify(response.data))
      return { success: true, data: response.data }
    } catch (err) {
      console.warn('getCurrentUser API failed:', err)

      // If getting user info fails, don't logout immediately
      // Just return failure and let the caller decide
      if (err.response?.status === 401 || err.response?.status === 400) {
        return { success: false, error: 'Authentication failed' }
      }

      return { success: false, error: 'Failed to get user info' }
    }
  }

  const initializeAuth = async () => {
    const storedToken = localStorage.getItem('customer_token')
    const storedUser = localStorage.getItem('customer_user')

    console.log('Initializing customer auth...')
    console.log('Stored token:', storedToken ? 'exists' : 'missing')
    console.log('Stored user:', storedUser ? 'exists' : 'missing')

    if (storedToken) {
      // Always set token and auth header first
      token.value = storedToken
      api.defaults.headers.common['Authorization'] = `Bearer ${storedToken}`

      if (storedUser) {
        try {
          const parsedUser = JSON.parse(storedUser)
          if (parsedUser && (parsedUser.id || parsedUser.khachHangId)) {
            user.value = parsedUser
            // Attempt to refresh user info in background
            try {
              const result = await getCurrentUser()
              if (result.success && result.data) {
                user.value = result.data
                localStorage.setItem('customer_user', JSON.stringify(result.data))
              }
            } catch (error) {
              console.warn('Customer auth verification failed, keeping stored data:', error)
            }
          } else {
            // If stored user invalid, fetch from API using token
            const result = await getCurrentUser()
            if (result.success && result.data) {
              user.value = result.data
              localStorage.setItem('customer_user', JSON.stringify(result.data))
            }
          }
        } catch (error) {
          console.error('Error parsing stored user data:', error)
          // Try to fetch user using token
          const result = await getCurrentUser()
          if (result.success && result.data) {
            user.value = result.data
            localStorage.setItem('customer_user', JSON.stringify(result.data))
          } else {
            await logout()
          }
        }
      } else {
        // No stored user, fetch it using the token
        const result = await getCurrentUser()
        if (result.success && result.data) {
          user.value = result.data
          localStorage.setItem('customer_user', JSON.stringify(result.data))
        } else {
          // If cannot fetch user, keep token but do not force logout; leave unauthenticated state
          console.warn('No stored user and failed to fetch user info')
        }
      }
    } else {
      // Ensure clean state when no stored token
      token.value = null
      user.value = null
      delete api.defaults.headers.common['Authorization']
    }
  }

  const updateProfile = async (profileData) => {
    loading.value = true
    error.value = null

    try {
      const response = await api.put('/api/customer/auth/update-profile', profileData)
      user.value = response.data
      localStorage.setItem('customer_user', JSON.stringify(response.data))
      return { success: true, data: response.data }
    } catch (err) {
      console.error('Update customer profile error:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const getCustomerInfo = async () => {
    if (!token.value) return { success: false, error: 'No token' }

    try {
      const response = await api.get('/api/customer/auth/me')
      user.value = response.data
      localStorage.setItem('customer_user', JSON.stringify(response.data))
      return { success: true, data: response.data }
    } catch (err) {
      console.error('Get customer info error:', err)
      return { success: false, error: 'Failed to get customer info' }
    }
  }

  const handleGoogleCallback = async (code) => {
    loading.value = true
    error.value = null

    try {
      // Sử dụng POST /google-login thay vì GET /google/callback
      const response = await api.post('/api/customer/auth/google-login', { code })

      if (response.data.success) {
        // Store token and user data
        token.value = response.data.token
        user.value = response.data.user

        // Save to localStorage
        localStorage.setItem('customer_token', token.value)
        localStorage.setItem('customer_user', JSON.stringify(user.value))

        // Set authorization header
        api.defaults.headers.common['Authorization'] = `Bearer ${token.value}`

        return { success: true, data: response.data }
      } else {
        throw new Error(response.data.message || 'Google login failed')
      }
    } catch (err) {
      error.value = err.response?.data?.message || 'Đăng nhập Google thất bại'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  return {
    // State
    token,
    user,
    loading,
    error,

    // Getters
    isAuthenticated,
    isCustomer,
    userName,
    userEmail,
    userId,
    customer,

    // Actions
    login,
    googleLogin,
    getGoogleOAuthUrl,
    register,
    logout,
    refreshToken,
    forgotPassword,
    verifyOTP,
    resetPassword,
    getCurrentUser,
    initializeAuth,
    updateProfile,
    getCustomerInfo,
    handleGoogleCallback,
  }
})
