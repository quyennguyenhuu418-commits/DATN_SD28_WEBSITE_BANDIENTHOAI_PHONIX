import { createRouter, createWebHistory } from 'vue-router'

const DashboardPage = () => import('../views/DashboardPage.vue')
const PosPage = () => import('../views/PosPage.vue')
const OnlinePage = () => import('../views/OnlinePage.vue')
const SanPhamPage = () => import('../views/SanPhamPage.vue')
const SanPhamFormPage = () => import('../views/SanPhamFormPage.vue')
const SanPhamViewPage = () => import('../views/SanPhamViewPage.vue')
const HoaDonPage = () => import('../views/HoaDonPage.vue')
const VoucherPage = () => import('../views/VoucherPage.vue')
const VoucherFormPage = () => import('../views/VoucherFormPage.vue')
const DotGiamGiaPage = () => import('../views/DotGiamGiaPage.vue')
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
const KhachHangPage = () => import('../views/KhachHangPage.vue')
const NhanVienPage = () => import('../views/NhanVienPage.vue')
const CaiDatPage = () => import('../views/CaiDatPage.vue')
const LoginPage = () => import('../views/LoginPage.vue')
const CustomerVoucherPage = () => import('../views/CustomerVoucherPage.vue')
const ThongKePage = () => import('../views/ThongKePage.vue')
const BaoCaoBanHangPage = () => import('../views/BaoCaoBanHangPage.vue')
const BaoCaoTonKhoPage = () => import('../views/BaoCaoTonKhoPage.vue')
const PhanTichKhachHangPage = () => import('../views/PhanTichKhachHangPage.vue')
const DarkModeDemo = () => import('../views/DarkModeDemo.vue')


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
    { path: '/voucher/form/:id', name: 'voucher-form', component: VoucherFormPage },
    { path: '/customer-vouchers', name: 'customer-vouchers', component: CustomerVoucherPage },
    { path: '/dot-giam-gia', name: 'dot-giam-gia', component: DotGiamGiaPage },
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
    { path: '/khach-hang', name: 'khach-hang', component: KhachHangPage },
    { path: '/khach-hang-sidebar', name: 'khach-hang-sidebar', component: () => import('../views/KhachHangSidebarPage.vue') },
    { path: '/khach-hang/add', name: 'khach-hang-add', component: () => import('../views/KhachHangAddPage.vue') },
    { path: '/khach-hang/edit/:id', name: 'khach-hang-edit', component: () => import('../views/KhachHangEditPage.vue') },
    { path: '/nhan-vien', name: 'nhan-vien', component: NhanVienPage },
    { path: '/cai-dat', name: 'cai-dat', component: CaiDatPage },
    { path: '/login', name: 'login', component: LoginPage },
    { path: '/thong-ke', name: 'thong-ke', component: ThongKePage },
    { path: '/bao-cao-ban-hang', name: 'bao-cao-ban-hang', component: BaoCaoBanHangPage },
    { path: '/bao-cao-ton-kho', name: 'bao-cao-ton-kho', component: BaoCaoTonKhoPage },
    { path: '/phan-tich-khach-hang', name: 'phan-tich-khach-hang', component: PhanTichKhachHangPage },
    { path: '/dark-mode-demo', name: 'dark-mode-demo', component: DarkModeDemo },
    { path: '/logout', redirect: '/login' },
    { path: '/giam-gia', redirect: '/voucher' }, // Redirect old route to new
  ],
})

export default router
