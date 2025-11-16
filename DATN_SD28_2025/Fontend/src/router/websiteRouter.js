import { createRouter, createWebHistory } from 'vue-router'
import { setupWebsiteAuthGuard } from './websiteAuthGuard'
import customerRoutes from './customerRoutes.js'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    if (to.hash) {
      return {
        el: to.hash,
        behavior: 'smooth',
        top: 100
      }
    }
    return {
      top: 0,
      behavior: 'smooth'
    }
  },
  routes: [
    // Homepage
    {
      path: '/',
      name: 'home',
      component: () => import('../views/Website/HomePage.vue'),
      meta: { public: true }
    },
    // Shop Page Route (full products listing with filters)
    {
      path: '/shop',
      name: 'shop',
      component: () => import('../views/Website/ShopPage.vue'),
      meta: { public: true }
    },
    // Product Detail Page Route
    {
      path: '/product/:id',
      name: 'product-detail',
      component: () => import('../views/Website/ProductDetailPage.vue'),
      meta: { public: true }
    },
    // Cart Page
    {
      path: '/cart',
      name: 'cart',
      component: () => import('../views/Website/CartPage.vue'),
      meta: { public: true }
    },
    // Checkout Page
    {
      path: '/dat-hang',
      name: 'checkout',
      component: () => import('../views/Website/DatHangPage.vue'),
      meta: { public: true }
    },
    // Direct checkout route
    {
      path: '/checkout',
      name: 'checkout-direct',
      component: () => import('../views/Website/DatHangPage.vue'),
      meta: { public: true }
    },
    // Legacy route redirect
    {
      path: '/dathang',
      redirect: '/dat-hang'
    },
    // Payment return pages
    {
      path: '/payment/vnpay-return',
      name: 'vnpay-return',
      component: () => import('../views/Website/PaymentResultPage.vue'),
      meta: { public: true }
    },
    {
      path: '/payment/zalopay-return',
      name: 'zalopay-return',
      component: () => import('../views/Website/ZaloPayResultPage.vue'),
      meta: { public: true }
    },
    // Order Detail Page (for customer website)
    {
      path: '/don-hang/:orderId',
      name: 'order-detail',
      component: () => import('../views/Website/DonHangChiTietPage.vue'),
      meta: { public: true }
    },
    // Customer routes
    ...customerRoutes,
    
    // Website public routes
    {
      path: '/customer-chat',
      name: 'customer-chat',
      component: () => import('../views/CustomerChatPage.vue'),
      meta: { public: true }
    },
    {
      path: '/faq',
      name: 'faq',
      component: () => import('../views/FAQPage.vue'),
      meta: { public: true }
    },
    // Account page (optional auth - can access if logged in)
    {
      path: '/account',
      name: 'account',
      component: () => import('../views/AccountInfoPage.vue'),
      meta: { public: true } // Public but shows different content if logged in
    },
  ],
})

// Setup website auth guard (không yêu cầu đăng nhập)
console.log('✅ Setting up WebsiteAuthGuard (no login required)')
setupWebsiteAuthGuard(router)

export default router

