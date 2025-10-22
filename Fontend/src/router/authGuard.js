import { useAuthStore } from '@/stores/authStore'

export const authGuard = (to, from, next) => {
  const authStore = useAuthStore()
  
  console.log('AuthGuard - Route:', to.path, 'Requires auth:', to.meta?.requiresAuth, 'Role:', to.meta?.role)
  console.log('AuthGuard - Is authenticated:', authStore.isAuthenticated, 'User:', authStore.user)
  console.log('AuthGuard - Token:', authStore.token)
  console.log('AuthGuard - Can access:', authStore.canAccess(to))
  
  // Check if route requires authentication
  if (to.meta?.requiresAuth) {
    if (!authStore.isAuthenticated) {
      console.log('AuthGuard - Not authenticated, redirecting to login')
      // Redirect to login if not authenticated
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
      return
    }
    
    // Check role-based access
    if (to.meta?.role && !authStore.canAccess(to)) {
      console.log('AuthGuard - Insufficient role, redirecting to unauthorized')
      // Redirect to unauthorized page or dashboard
      next({
        path: '/dashboard',
        query: { error: 'unauthorized' }
      })
      return
    }
  }
  
  // If already authenticated and trying to access login page, redirect to dashboard
  if (to.path === '/login' && authStore.isAuthenticated) {
    console.log('AuthGuard - Already authenticated, redirecting to dashboard')
    next('/dashboard')
    return
  }
  
  console.log('AuthGuard - Access granted')
  next()
}

export const setupAuthGuard = (router) => {
  router.beforeEach(authGuard)
}
