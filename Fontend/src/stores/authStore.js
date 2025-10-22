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
      const response = await api.post('/api/auth/login', credentials)
      const { token: newToken, user: userData } = response.data
      
      // Store token and user data
      token.value = newToken
      user.value = userData
      
      // Persist to localStorage
      localStorage.setItem('token', newToken)
      localStorage.setItem('user', JSON.stringify(userData))
      
      // Set default authorization header
      api.defaults.headers.common['Authorization'] = `Bearer ${newToken}`
      
      // Check for pending shift handovers after successful login
      console.log('AuthStore: User logged in, checking pending handovers for ID:', userData.id)
      const pendingHandovers = await checkPendingShiftHandovers(userData.id)
      console.log('AuthStore: Final pendingHandovers result:', pendingHandovers)
      
      return { success: true, data: response.data, pendingHandovers }
    } catch (err) {
      error.value = err.response?.data?.message || 'Đăng nhập thất bại'
      return { success: false, error: error.value }
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
      
      // If getting user info fails, logout
      if (err.response?.status === 401 || err.response?.status === 400) {
        console.log('Authentication failed, logging out')
        await logout()
        return { success: false, error: 'Authentication failed' }
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
        if (!result.success) {
          console.log('Token validation failed, logging out')
          await logout()
        } else {
          console.log('User authenticated successfully:', user.value)
        }
      } catch (error) {
        console.error('Auth initialization error:', error)
        await logout()
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
