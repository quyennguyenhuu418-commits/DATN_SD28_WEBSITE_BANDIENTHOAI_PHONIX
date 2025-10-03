import { createRouter, createWebHistory } from 'vue-router'

const DashboardPage = () => import('../views/DashboardPage.vue')
const PosPage = () => import('../views/PosPage.vue')
const OnlinePage = () => import('../views/OnlinePage.vue')
const SanPhamPage = () => import('../views/SanPhamPage.vue')
const SanPhamFormPage = () => import('../views/SanPhamFormPage.vue')
const SanPhamViewPage = () => import('../views/SanPhamViewPage.vue')
const HoaDonPage = () => import('../views/HoaDonPage.vue')
const VoucherPage = () => import('../views/VoucherPage.vue')
const HangPage = () => import('../views/HangPage.vue')
const ChipPage = () => import('../views/ChipPage.vue')
const RamPage = () => import('../views/RamPage.vue')
const RomPage = () => import('../views/RomPage.vue')
const ManHinhPage = () => import('../views/ManHinhPage.vue')
const MauSacPage = () => import('../views/MauSacPage.vue')
const PinPage = () => import('../views/PinPage.vue')
const HeDieuHanhPage = () => import('../views/HeDieuHanhPage.vue')
const CameraTruocPage = () => import('../views/CameraTruocPage.vue')
const CameraSauPage = () => import('../views/CameraSauPage.vue')
const CpuPage = () => import('../views/CpuPage.vue')
const GpuPage = () => import('../views/GpuPage.vue')


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/dashboard' },
    { path: '/dashboard', name: 'dashboard', component: DashboardPage },
    { path: '/pos', name: 'pos', component: PosPage },
    { path: '/online', name: 'online', component: OnlinePage },
    { path: '/san-pham', name: 'san-pham', component: SanPhamPage },
    { path: '/san-pham/form/:id?', name: 'san-pham-form', component: SanPhamFormPage },
    { path: '/san-pham/view/:id', name: 'san-pham-view', component: SanPhamViewPage },
    // merged into SanPhamPage
    { path: '/hoa-don', name: 'hoa-don', component: HoaDonPage },
    { path: '/voucher', name: 'voucher', component: VoucherPage },
    { path: '/hang', name: 'hang', component: HangPage },
    { path: '/chip', name: 'chip', component: ChipPage },
    { path: '/ram', name: 'ram', component: RamPage },
    { path: '/rom', name: 'rom', component: RomPage },
    { path: '/man-hinh', name: 'man-hinh', component: ManHinhPage },
    { path: '/mau-sac', name: 'mau-sac', component: MauSacPage },
    { path: '/pin', name: 'pin', component: PinPage },
    { path: '/he-dieu-hanh', name: 'he-dieu-hanh', component: HeDieuHanhPage },
    { path: '/camera-truoc', name: 'camera-truoc', component: CameraTruocPage },
    { path: '/camera-sau', name: 'camera-sau', component: CameraSauPage },
    { path: '/cpu', name: 'cpu', component: CpuPage },
    { path: '/gpu', name: 'gpu', component: GpuPage },
   
  ],
})

export default router
