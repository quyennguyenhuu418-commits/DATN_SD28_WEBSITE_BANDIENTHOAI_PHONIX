// Redirected to HomePage
const HomePage = () => import('../views/Website/HomePage.vue')
const TheoDoiDonHangPage = () => import('../views/Website/TheoDoiDonHangPage.vue')

const customerRoutes = [
  {
    path: '/customer',
    redirect: '/' // Redirect to new homepage
  },
  {
    path: '/theo-doi-don-hang',
    name: 'theo-doi-don-hang',
    component: TheoDoiDonHangPage
  }
]

export default customerRoutes
