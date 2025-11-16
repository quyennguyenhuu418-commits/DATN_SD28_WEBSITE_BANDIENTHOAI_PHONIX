import { useAuthStore } from '@/stores/authStore'

// Kiểm tra route public dựa trên meta
const isPublicRoute = (to) => {
  // Nếu bất kỳ record nào trong matched có meta.public === true
  if (to.matched.some(record => record.meta?.public === true)) {
    return true
  }
  // Luôn cho phép trang login mà không cần đăng nhập
  if (to.path === '/login' || to.path === '/forgot-password' || to.path === '/admin/login' || to.path === '/admin/forgot-password') {
    return true
  }
  return false
}

// Kiểm tra xem route có phải là admin route không
const isAdminRoute = (path) => {
  return path.startsWith('/admin') && path !== '/admin/login'
}

// Kiểm tra xem route có phải là website route không (không phải admin, không phải legacy routes)
const isWebsiteRoute = (path) => {
  // Website routes: home, shop, product, cart, checkout, payment, order tracking
  // Loại trừ admin routes và login routes
  if (path.startsWith('/admin') || path === '/login' || path === '/forgot-password' || path === '/admin/forgot-password') {
    return false
  }
  
  // Tất cả các routes khác (không phải admin) đều là website routes
  // Website routes: home, shop, product, cart, checkout, payment, order tracking, account, etc.
  const websitePaths = ['/', '/shop', '/product', '/cart', '/dat-hang', '/checkout', '/payment', '/don-hang', '/customer-chat', '/theo-doi-don-hang', '/faq', '/account', '/dathang']
  return websitePaths.some(prefix => path === prefix || path.startsWith(prefix + '/')) || path === '/'
}

export const authGuard = (to, from, next) => {
  const authStore = useAuthStore()
  
  console.log('AuthGuard - Route:', to.path, 'Requires auth:', to.meta?.requiresAuth, 'Role:', to.meta?.role)
  console.log('AuthGuard - Is authenticated:', authStore.isAuthenticated, 'User:', authStore.user)
  
  // 1. Public routes - luôn cho phép truy cập (login pages, public website routes)
  // Kiểm tra meta.public TRƯỚC TIÊN để đảm bảo website routes luôn được cho phép
  if (isPublicRoute(to)) {
    console.log('AuthGuard - Public route (meta.public=true), allowing access')
    next()
    return
  }
  
  // 2. Website routes - KHÔNG BẮT BUỘC đăng nhập (optional authentication)
  // Kiểm tra website routes TRƯỚC admin routes để đảm bảo website luôn public
  if (isWebsiteRoute(to.path)) {
    console.log('AuthGuard - Website route detected, allowing access (optional authentication)')
    // Cho phép truy cập dù đã đăng nhập hay chưa
    next()
    return
  }
  
  // 3. Admin routes - BẮT BUỘC đăng nhập
  if (isAdminRoute(to.path)) {
    console.log('AuthGuard - Admin route detected, checking authentication')
    if (!authStore.isAuthenticated) {
      console.log('AuthGuard - Not authenticated for admin route, redirecting to admin login')
      next({
        path: '/admin/login',
        query: { redirect: to.fullPath }
      })
      return
    }
    
    // Check role-based access for admin routes
    const requiredRole = to.meta?.role || to.matched.find(record => record.meta?.role)?.meta?.role
    
    if (requiredRole && !authStore.canAccess(to)) {
      console.log('AuthGuard - Insufficient role for admin route')
      const userRole = authStore.userRole
      let redirectPath = '/admin/login'
      
      if (userRole === 'ADMIN' || userRole === 'MANAGER') {
        redirectPath = '/admin/dashboard'
      } else if (userRole === 'STAFF') {
        redirectPath = '/admin/dashboard' // STAFF vẫn có thể vào admin dashboard
      }
      
      next({
        path: redirectPath,
        query: { error: 'unauthorized' }
      })
      return
    }
    
    console.log('AuthGuard - Admin route access granted')
    next()
    return
  }
  
  // 4. Check explicit requiresAuth meta (cho legacy routes hoặc routes không rõ ràng)
  const requiresAuth = to.matched.some(record => record.meta?.requiresAuth) || to.meta?.requiresAuth
  
  if (requiresAuth) {
    if (!authStore.isAuthenticated) {
      console.log('AuthGuard - Route requires auth but not authenticated, redirecting to login')
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
      return
    }
    
    // Check role-based access
    const requiredRole = to.meta?.role || to.matched.find(record => record.meta?.role)?.meta?.role
    
    if (requiredRole && !authStore.canAccess(to)) {
      console.log('AuthGuard - Insufficient role')
      const userRole = authStore.userRole
      let redirectPath = '/login'
      
      if (userRole === 'ADMIN' || userRole === 'MANAGER') {
        redirectPath = '/admin/dashboard'
      } else if (userRole === 'STAFF') {
        redirectPath = '/pos'
      }
      
      next({
        path: redirectPath,
        query: { error: 'unauthorized' }
      })
      return
    }
  }
  
  // 5. Default: cho phép truy cập (public access)
  // Đảm bảo mặc định là public để website routes không bị chặn
  console.log('AuthGuard - Access granted (default public)')
  next()
}

export const setupAuthGuard = (router) => {
  router.beforeEach(authGuard)
}
