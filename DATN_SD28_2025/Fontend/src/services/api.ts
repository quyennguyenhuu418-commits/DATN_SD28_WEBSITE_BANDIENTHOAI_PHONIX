import axios, { AxiosRequestConfig, InternalAxiosRequestConfig } from 'axios'

const baseURL = 'http://localhost:8080'

type AuthType = 'staff' | 'customer'

const STORAGE_KEYS = {
  staffToken: 'token',
  staffUser: 'user',
  customerToken: 'customer_token',
  customerUser: 'customer_user',
}

const isValidJwtToken = (token: string | null): token is string => {
  if (!token || typeof token !== 'string') return false
  const parts = token.split('.')
  return parts.length === 3 && parts.every(part => part.length > 0)
}

const isAdminRoute = (): boolean => {
  if (typeof window === 'undefined') return false
  const path = window.location.pathname || ''
  return path.startsWith('/admin')
}

const isWebsiteRoute = (): boolean => {
  if (typeof window === 'undefined') return false
  const path = window.location.pathname || ''
  return (
    path === '/' ||
    path.startsWith('/shop') ||
    path.startsWith('/product/') ||
    path.startsWith('/cart') ||
    path.startsWith('/dat-hang') ||
    path.startsWith('/checkout') ||
    path.startsWith('/payment/') ||
    path.startsWith('/don-hang/') ||
    path.startsWith('/theo-doi-don-hang') ||
    path.startsWith('/account') ||
    path.startsWith('/faq') ||
    path.startsWith('/customer-chat')
  )
}

const pickToken = () => {
  const staffToken = localStorage.getItem(STORAGE_KEYS.staffToken)
  const customerToken = localStorage.getItem(STORAGE_KEYS.customerToken)

  if (isAdminRoute()) {
    if (isValidJwtToken(staffToken)) {
      return { token: staffToken, type: 'staff' as AuthType }
    }
    if (isValidJwtToken(customerToken)) {
      return { token: customerToken, type: 'customer' as AuthType }
    }
  } else {
    if (isValidJwtToken(customerToken)) {
      return { token: customerToken, type: 'customer' as AuthType }
    }
    if (isValidJwtToken(staffToken)) {
      return { token: staffToken, type: 'staff' as AuthType }
    }
  }

  return null
}

const clearAuth = (type: AuthType) => {
  if (type === 'staff') {
    localStorage.removeItem(STORAGE_KEYS.staffToken)
    localStorage.removeItem(STORAGE_KEYS.staffUser)
  } else {
    localStorage.removeItem(STORAGE_KEYS.customerToken)
    localStorage.removeItem(STORAGE_KEYS.customerUser)
  }
}

const storeAuth = (type: AuthType, token: string, userData: any) => {
  if (type === 'staff') {
    localStorage.setItem(STORAGE_KEYS.staffToken, token)
    if (userData) {
      localStorage.setItem(STORAGE_KEYS.staffUser, JSON.stringify(userData))
    }
  } else {
    localStorage.setItem(STORAGE_KEYS.customerToken, token)
    if (userData) {
      localStorage.setItem(STORAGE_KEYS.customerUser, JSON.stringify(userData))
    }
  }
}

const api = axios.create({
  baseURL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

api.interceptors.request.use(
  (config: InternalAxiosRequestConfig & { _authType?: AuthType }) => {
    const tokenInfo = pickToken()

    if (tokenInfo) {
      config.headers.Authorization = `Bearer ${tokenInfo.token}`
      config._authType = tokenInfo.type
    } else {
      delete config.headers.Authorization
    }

    return config
  },
  (error) => Promise.reject(error)
)

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest: AxiosRequestConfig & { _retry?: boolean; _authType?: AuthType } = error.config || {}

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true

      const tokenInfo = pickToken()
      if (!tokenInfo) {
        return Promise.reject(error)
      }

      const authType: AuthType = originalRequest._authType || tokenInfo.type
      const refreshEndpoint = authType === 'staff'
        ? '/api/auth/refresh'
        : '/api/customer/auth/refresh'

      try {
        const refreshResponse = await api.post(refreshEndpoint, {}, {
          headers: { Authorization: `Bearer ${tokenInfo.token}` },
        })

        const { token: newToken, user: userData } = refreshResponse.data || {}

        if (newToken && isValidJwtToken(newToken)) {
          storeAuth(authType, newToken, userData)
          api.defaults.headers.common['Authorization'] = `Bearer ${newToken}`
          originalRequest.headers = originalRequest.headers || {}
          originalRequest.headers['Authorization'] = `Bearer ${newToken}`
          originalRequest._authType = authType
          return api(originalRequest)
        }
      } catch (refreshError) {
        clearAuth(authType)

        if (typeof window !== 'undefined') {
          const currentPath = window.location.pathname
          if (authType === 'staff') {
            if (!currentPath.startsWith('/admin/login')) {
              window.location.href = '/admin/login'
            }
          } else if (!isWebsiteRoute()) {
            if (!currentPath.startsWith('/login')) {
              window.location.href = '/login'
            }
          }
        }

        return Promise.reject(refreshError)
      }
    }

    return Promise.reject(error)
  }
)

export default api

export const paymentApi = {
  getBanks() {
    return api.get('/api/payments/banks')
  },
  createVnPayPayment(data: { amount: number; orderInfo: string; bankCode?: string }) {
    return api.post('/api/payments/vnpay/create', data)
  },
  createVnPayPaymentWithOrder(data: { amount: number; orderInfo: string; bankCode?: string; order: any }) {
    return api.post('/api/payments/vnpay/create-with-order', data)
  },
  createZaloPayPaymentWithOrder(data: { amount: number; orderInfo: string; order: any }) {
    return api.post('/api/payments/zalopay/create-with-order', data)
  },
  handleZaloPayReturn(params: any) {
    return api.get('/api/payments/zalopay/return', { params })
  },
}
