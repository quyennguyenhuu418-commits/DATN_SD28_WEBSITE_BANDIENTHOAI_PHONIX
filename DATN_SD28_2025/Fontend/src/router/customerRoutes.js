// Redirected to HomePage
const HomePage = () => import('../views/Website/HomePage.vue')
const TheoDoiDonHangPage = () => import('../views/Website/TheoDoiDonHangPage.vue')
const CustomerLoginPage = () => import('../views/Website/CustomerLoginPage.vue')
const CustomerRegisterPage = () => import('../views/Website/CustomerRegisterPage.vue')
const CustomerAccountPage = () => import('../views/Website/CustomerAccountPage.vue')
const CustomerForgotPasswordPage = () => import('../views/Website/CustomerForgotPasswordPage.vue')

const customerRoutes = [
  {
    path: '/customer',
    redirect: '/' // Redirect to new homepage
  },
  {
    path: '/customer/login',
    name: 'customer-login',
    component: CustomerLoginPage,
    meta: { public: true }
  },
  {
    path: '/customer/register',
    name: 'customer-register',
    component: CustomerRegisterPage,
    meta: { public: true }
  },
  {
    path: '/customer/forgot-password',
    name: 'customer-forgot-password',
    component: CustomerForgotPasswordPage,
    meta: { public: true }
  },
  {
    path: '/google-callback',
    name: 'google-callback',
    component: CustomerLoginPage, // Same component handles callback
    meta: { public: true }
  },
  {
    path: '/login',
    redirect: '/customer/login' // Redirect old login route to customer login
  },
  {
    path: '/theo-doi-don-hang',
    name: 'theo-doi-don-hang',
    component: TheoDoiDonHangPage,
    meta: { public: true }
  },
  {
    path: '/account',
    name: 'customer-account',
    component: CustomerAccountPage,
    meta: { public: true, requiresAuth: true }
  }
]

export default customerRoutes
