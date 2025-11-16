import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/services/api'

export const useAuthStore = defineStore('auth', () => {
  // State
  const token = ref(localStorage.getItem('token') || null)
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const loading = ref(false)
  const error = ref(null)

  // Getters
  const isAuthenticated = computed(() => {
    const hasToken = !!token.value
    console.log('AuthStore - isAuthenticated check:', { hasToken, token: token.value, user: user.value })
    return hasToken
  })
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const isManager = computed(() => user.value?.role === 'MANAGER')
  const isStaff = computed(() => user.value?.role === 'STAFF')
  const userRole = computed(() => user.value?.role || 'GUEST')
  const userName = computed(() => user.value?.hoTen || user.value?.username || 'Guest')

  // Actions
  const login = async (credentials) => {
    loading.value = true
    error.value = null
    
    try {
      console.log('AuthStore: Attempting login with credentials:', { username: credentials.username, hasPassword: !!credentials.password })
      
      // Validate credentials before sending
      if (!credentials.username || !credentials.password) {
        const errorMsg = !credentials.username ? 'Tài khoản không được để trống' : 'Mật khẩu không được để trống'
        error.value = errorMsg
        return { success: false, error: errorMsg }
      }
      
      const response = await api.post('/api/auth/login', credentials)
      
      if (response.data && response.data.success) {
        const { token: newToken, user: userData } = response.data
        
        // Store token and user data
        token.value = newToken
        user.value = userData
        
        // Persist to localStorage
        localStorage.setItem('token', newToken)
        localStorage.setItem('user', JSON.stringify(userData))
        
        // Set default authorization header
        api.defaults.headers.common['Authorization'] = `Bearer ${newToken}`
        
        // Check for pending shift handovers after successful login (async, don't block)
        console.log('AuthStore: User logged in, checking pending handovers for ID:', userData.id)
        // Run handover check in background, don't wait for it
        checkPendingShiftHandovers(userData.id).then((pendingHandovers) => {
          console.log('AuthStore: Final pendingHandovers result:', pendingHandovers)
        }).catch((handoverError) => {
          // Ignore handover check errors, login is still successful
          console.warn('AuthStore: Handover check failed (non-blocking):', handoverError)
        })
        
        // Return success immediately without waiting for handover check
        return { success: true, data: response.data, pendingHandovers: null }
      } else {
        const errorMsg = response.data?.message || 'Đăng nhập thất bại'
        error.value = errorMsg
        return { success: false, error: errorMsg }
      }
    } catch (err) {
      console.error('AuthStore: Login error:', err)
      const errorMsg = err.response?.data?.message || 
                      err.response?.data?.error || 
                      err.message || 
                      'Đăng nhập thất bại'
      error.value = errorMsg
      return { success: false, error: errorMsg }
    } finally {
      loading.value = false
    }
  }

  const logout = async () => {
    loading.value = true
    
    try {
      // Call logout endpoint (optional)
      await api.post('/api/auth/logout')
    } catch (err) {
      console.warn('Logout API call failed:', err)
    } finally {
      // Clear local state regardless of API call result
      token.value = null
      user.value = null
      error.value = null
      
      // Clear localStorage
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      
      // Remove authorization header
      delete api.defaults.headers.common['Authorization']
      
      loading.value = false
    }
  }

  const refreshToken = async () => {
    if (!token.value) return { success: false, error: 'No token to refresh' }
    
    try {
      const response = await api.post('/api/auth/refresh', {}, {
        headers: { Authorization: `Bearer ${token.value}` }
      })
      
      const { token: newToken, user: userData } = response.data
      
      // Update stored data
      token.value = newToken
      user.value = userData
      
      // Update localStorage
      localStorage.setItem('token', newToken)
      localStorage.setItem('user', JSON.stringify(userData))
      
      // Update authorization header
      api.defaults.headers.common['Authorization'] = `Bearer ${newToken}`
      
      return { success: true, data: response.data }
    } catch (err) {
      // If refresh fails, logout user
      await logout()
      return { success: false, error: 'Token refresh failed' }
    }
  }

  const changePassword = async (passwordData) => {
    loading.value = true
    error.value = null

    try {
      const response = await api.post('/api/auth/change-password', passwordData)
      return response.data
    } catch (err) {
      console.error('Change password error:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const forgotPassword = async (emailData) => {
    loading.value = true
    error.value = null

    try {
      const response = await api.post('/api/auth/forgot-password', emailData)
      return response.data
    } catch (err) {
      console.error('Forgot password error:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const resetPassword = async (resetData) => {
    loading.value = true
    error.value = null

    try {
      const response = await api.post('/api/auth/reset-password', resetData)
      return response.data
    } catch (err) {
      console.error('Reset password error:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

    const checkPendingShiftHandovers = async (nhanVienId) => {
      try {
        console.log('AuthStore: Checking pending shift handovers for nhanVienId:', nhanVienId)
        const response = await api.get(`/api/auth/pending-shift-handover?nhanVienId=${nhanVienId}`)
        console.log('AuthStore: Received response:', response.data)
        console.log('AuthStore: Response status:', response.status)
        console.log('AuthStore: Response headers:', response.headers)
        console.log('AuthStore: Response data type:', typeof response.data)
        console.log('AuthStore: Response data length:', Array.isArray(response.data) ? response.data.length : 'Not an array')
        return response.data
      } catch (err) {
        console.error('AuthStore: Error checking pending shift handovers:', err)
        console.error('AuthStore: Error response:', err.response)
        return []
      }
    }

  const getCurrentUser = async () => {
    if (!token.value) return { success: false, error: 'No token' }
    
    try {
      const response = await api.get('/api/auth/me')
      user.value = response.data
      localStorage.setItem('user', JSON.stringify(response.data))
      return { success: true, data: response.data }
    } catch (err) {
      console.log('Auth error:', err.response?.status, err.response?.data)
      
      // Only logout on 401 (unauthorized) - not on 404 (user not found)
      // 404 might mean token is valid but user was deleted, which is different from invalid token
      if (err.response?.status === 401) {
        console.log('Token invalid (401), logging out')
        await logout()
        return { success: false, error: 'Authentication failed' }
      }
      
      // For 404, don't logout immediately - token might still be valid
      // Just return failure and let the caller decide
      if (err.response?.status === 404) {
        console.warn('User not found (404), but token might still be valid')
        return { success: false, error: 'User not found' }
      }
      
      return { success: false, error: 'Failed to get user info' }
    }
  }

  const initializeAuth = async () => {
    const storedToken = localStorage.getItem('token')
    const storedUser = localStorage.getItem('user')
    
    if (storedToken && storedUser) {
      token.value = storedToken
      user.value = JSON.parse(storedUser)
      api.defaults.headers.common['Authorization'] = `Bearer ${storedToken}`
      
      // Verify token is still valid by getting current user
      try {
        const result = await getCurrentUser()
        if (result.success) {
          console.log('User authenticated successfully:', user.value)
        } else {
          // Only logout if it's a 401 (unauthorized), not 404 (user not found)
          // For 404, keep the stored user data and token - might be a temporary backend issue
          if (result.error === 'Authentication failed') {
            console.log('Token validation failed (401), logging out')
            await logout()
          } else {
            console.warn('Could not refresh user data, but keeping stored auth:', result.error)
            // Keep the stored user data and token - user can still use the app
          }
        }
      } catch (error) {
        console.error('Auth initialization error:', error)
        // Only logout on 401, not on other errors
        if (error.response?.status === 401) {
          await logout()
        } else {
          console.warn('Auth initialization error (non-401), keeping stored auth')
        }
      }
    } else {
      console.log('No stored token or user data found')
    }
  }

  const hasPermission = (requiredRole) => {
    if (!user.value) return false
    
    // Map Vietnamese roles to English roles
    const roleMapping = {
      'Khách': 'GUEST',
      'Nhân viên': 'STAFF', 
      'Quản lý': 'MANAGER',
      'Quản trị viên': 'ADMIN',
      'GUEST': 'GUEST',
      'STAFF': 'STAFF',
      'MANAGER': 'MANAGER', 
      'ADMIN': 'ADMIN'
    }
    
    const roleHierarchy = {
      'GUEST': 0,
      'STAFF': 1,
      'MANAGER': 2,
      'ADMIN': 3
    }
    
    const mappedUserRole = roleMapping[user.value.role] || 'GUEST'
    const userLevel = roleHierarchy[mappedUserRole] || 0
    const requiredLevel = roleHierarchy[requiredRole] || 0
    
    console.log('Role check:', {
      originalRole: user.value.role,
      mappedRole: mappedUserRole,
      userLevel,
      requiredRole,
      requiredLevel,
      hasPermission: userLevel >= requiredLevel
    })
    
    return userLevel >= requiredLevel
  }

  const canAccess = (route) => {
    if (!route.meta?.requiresAuth) return true
    if (!isAuthenticated.value) return false
    
    const requiredRole = route.meta?.role
    if (!requiredRole) return true
    
    return hasPermission(requiredRole)
  }

  return {
    // State
    token,
    user,
    loading,
    error,
    
    // Getters
    isAuthenticated,
    isAdmin,
    isManager,
    isStaff,
    userRole,
    userName,
    
    // Actions
    login,
    logout,
    refreshToken,
    changePassword,
    forgotPassword,
    resetPassword,
    checkPendingShiftHandovers,
    getCurrentUser,
    initializeAuth,
    hasPermission,
    canAccess
  }
})
