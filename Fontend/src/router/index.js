import { createRouter, createWebHistory } from 'vue-router'
<<<<<<< HEAD
import { setupAuthGuard } from './authGuard'
=======
>>>>>>> origin/Huan

const DashboardPage = () => import('../views/DashboardPage.vue')
const PosPage = () => import('../views/PosPage.vue')
const OnlinePage = () => import('../views/OnlinePage.vue')
const SanPhamPage = () => import('../views/SanPhamPage.vue')
const SanPhamFormPage = () => import('../views/SanPhamFormPage.vue')
const SanPhamViewPage = () => import('../views/SanPhamViewPage.vue')
const HoaDonPage = () => import('../views/HoaDonPage.vue')
const VoucherPage = () => import('../views/VoucherPage.vue')
<<<<<<< HEAD
const VoucherFormPage = () => import('../views/VoucherFormPage.vue')
const VoucherDetailPage = () => import('../views/VoucherDetailPage.vue')
const DotGiamGiaPage = () => import('../views/DotGiamGiaPage.vue')
const DotGiamGiaFormPage = () => import('../views/DotGiamGiaFormPage.vue')
const DotGiamGiaDetailPage = () => import('../views/DotGiamGiaDetailPage.vue')
=======
>>>>>>> origin/Huan
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
<<<<<<< HEAD
const KhachHangPage = () => import('../views/KhachHangPage.vue')
const KhachHangDetailPage = () => import('../views/KhachHangDetailPage.vue')
const NhanVienPage = () => import('../views/NhanVienPage.vue')
const ChiTietNhanVienPage = () => import('../views/ChiTietNhanVienPage.vue')
const SuaNhanVienPage = () => import('../views/SuaNhanVienPage.vue')
const ThemNhanVienPage = () => import('../views/ThemNhanVienPage.vue')
const CaiDatPage = () => import('../views/CaiDatPage.vue')
const LoginPage = () => import('../views/LoginPage.vue')
const ForgotPasswordPage = () => import('../views/ForgotPasswordPage.vue')
const CustomerVoucherPage = () => import('../views/CustomerVoucherPage.vue')
const ThongKePage = () => import('../views/ThongKePage.vue')
const QuanLyGiaoCaPage = () => import('../views/QuanLyGiaoCaPage.vue')
const GiaoCaPage = () => import('../views/GiaoCaPage.vue')
const GiaoCaCreatePage = () => import('../views/GiaoCaCreatePage.vue')
const GiaoCaDetailPage = () => import('../views/GiaoCaDetailPage.vue')
const CaPage = () => import('../views/CaPage.vue')
const CaFormPage = () => import('../views/CaFormPage.vue')
const CaDetailPage = () => import('../views/CaDetailPage.vue')
const PhanCaPage = () => import('../views/PhanCaPage.vue')
const PhanCaFormPage = () => import('../views/PhanCaFormPage.vue')
const PhanCaDetailPage = () => import('../views/PhanCaDetailPage.vue')
const DarkModeDemo = () => import('../views/DarkModeDemo.vue')
const MessengerSettingsPage = () => import('../views/MessengerSettingsPage.vue')
const StaffChatDashboard = () => import('../components/StaffChatDashboard.vue')
const CustomerChatPage = () => import('../views/CustomerChatPage.vue')
const TestPage = () => import('../views/TestPage.vue')
const SimpleTestPage = () => import('../views/SimpleTestPage.vue')
const AccountInfoPage = () => import('../views/AccountInfoPage.vue')
const AuthDebugPage = () => import('../views/AuthDebugPage.vue')
const TestAuthPage = () => import('../views/TestAuthPage.vue')
const PaymentResultPage = () => import('../views/PaymentResultPage.vue')
const ZaloPayResultPage = () => import('../views/ZaloPayResultPage.vue')
=======
const DanhMucPage = () => import('../views/DanhMucPage.vue')
const ProductDetailPage = () => import('../views/ProductDetailPage.vue')
const AdminReviewPage = () => import('../views/AdminReviewPage.vue')
>>>>>>> origin/Huan


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/dashboard' },
<<<<<<< HEAD
    { path: '/dashboard', name: 'dashboard', component: DashboardPage, meta: { requiresAuth: true } },
    { path: '/trang-chu', name: 'trang-chu', component: DashboardPage, meta: { requiresAuth: true } },
    { path: '/pos', name: 'pos', component: PosPage, meta: { requiresAuth: true, role: 'STAFF' } },
    { path: '/online', name: 'online', component: OnlinePage, meta: { requiresAuth: true, role: 'STAFF' } },
    { path: '/san-pham', name: 'san-pham', component: SanPhamPage, meta: { requiresAuth: true, role: 'STAFF' } },
    { path: '/san-pham/form/:id?', name: 'san-pham-form', component: SanPhamFormPage, meta: { requiresAuth: true, role: 'STAFF' } },
    { path: '/san-pham/view/:id', name: 'san-pham-view', component: SanPhamViewPage, meta: { requiresAuth: true, role: 'STAFF' } },
    // merged into SanPhamPage
    { path: '/hoa-don', name: 'hoa-don', component: HoaDonPage, meta: { requiresAuth: true, role: 'STAFF' } },
    { path: '/voucher', name: 'voucher', component: VoucherPage, meta: { requiresAuth: true, role: 'STAFF' } },
    { path: '/phieu-giam-gia', name: 'phieu-giam-gia', component: VoucherPage, meta: { requiresAuth: true, role: 'STAFF' } },
    { path: '/voucher/form/:id', name: 'voucher-form', component: VoucherFormPage, meta: { requiresAuth: true, role: 'STAFF' } },
    { path: '/voucher/form/new', name: 'voucher-form-new', component: VoucherFormPage, meta: { requiresAuth: true, role: 'STAFF' } },
    { path: '/voucher/detail/:id', name: 'voucher-detail', component: VoucherDetailPage, meta: { requiresAuth: true, role: 'STAFF' } },
    { path: '/phieu-giam-gia/detail/:id', name: 'phieu-giam-gia-detail', component: VoucherDetailPage, meta: { requiresAuth: true, role: 'STAFF' } },
    { path: '/customer-vouchers', name: 'customer-vouchers', component: CustomerVoucherPage, meta: { requiresAuth: true, role: 'STAFF' } },
    { path: '/dot-giam-gia', name: 'dot-giam-gia', component: DotGiamGiaPage, meta: { requiresAuth: true, role: 'STAFF' } },
    { path: '/dot-giam-gia/form/:id', name: 'dot-giam-gia-form', component: DotGiamGiaFormPage, meta: { requiresAuth: true, role: 'STAFF' } },
    { path: '/dot-giam-gia/form/new', name: 'dot-giam-gia-form-new', component: DotGiamGiaFormPage, meta: { requiresAuth: true, role: 'STAFF' } },
    { path: '/dot-giam-gia/detail/:id', name: 'dot-giam-gia-detail', component: DotGiamGiaDetailPage, meta: { requiresAuth: true, role: 'STAFF' } },
    { path: '/hang', name: 'hang', component: HangPage, meta: { requiresAuth: true, role: 'STAFF' } },
    { path: '/chip', name: 'chip', component: ChipPage, meta: { requiresAuth: true, role: 'STAFF' } },
    { path: '/ram', name: 'ram', component: RamPage, meta: { requiresAuth: true, role: 'STAFF' } },
    { path: '/rom', name: 'rom', component: RomPage, meta: { requiresAuth: true, role: 'STAFF' } },
    { path: '/man-hinh', name: 'man-hinh', component: ManHinhPage, meta: { requiresAuth: true, role: 'STAFF' } },
    { path: '/mau-sac', name: 'mau-sac', component: MauSacPage, meta: { requiresAuth: true, role: 'STAFF' } },
    { path: '/pin', name: 'pin', component: PinPage, meta: { requiresAuth: true, role: 'STAFF' } },
    { path: '/he-dieu-hanh', name: 'he-dieu-hanh', component: HeDieuHanhPage, meta: { requiresAuth: true, role: 'STAFF' } },
    { path: '/camera-truoc', name: 'camera-truoc', component: CameraTruocPage, meta: { requiresAuth: true, role: 'STAFF' } },
    { path: '/camera-sau', name: 'camera-sau', component: CameraSauPage, meta: { requiresAuth: true, role: 'STAFF' } },
    { path: '/cpu', name: 'cpu', component: CpuPage, meta: { requiresAuth: true, role: 'STAFF' } },
    { path: '/gpu', name: 'gpu', component: GpuPage, meta: { requiresAuth: true, role: 'STAFF' } },
    { path: '/khach-hang', name: 'khach-hang', component: KhachHangPage, meta: { requiresAuth: true, role: 'STAFF' } },
    { path: '/khach-hang/detail/:id', name: 'khach-hang-detail', component: KhachHangDetailPage, meta: { requiresAuth: true, role: 'STAFF' } },
    { path: '/khach-hang-sidebar', name: 'khach-hang-sidebar', component: () => import('../views/KhachHangSidebarPage.vue'), meta: { requiresAuth: true, role: 'STAFF' } },
    { path: '/khach-hang/add', name: 'khach-hang-add', component: () => import('../views/KhachHangAddPage.vue'), meta: { requiresAuth: true, role: 'STAFF' } },
    { path: '/khach-hang/edit/:id', name: 'khach-hang-edit', component: () => import('../views/KhachHangEditPage.vue'), meta: { requiresAuth: true, role: 'STAFF' } },
    { path: '/nhan-vien', name: 'nhan-vien', component: NhanVienPage, meta: { requiresAuth: true, role: 'MANAGER' } },
    { path: '/nhan-vien/chi-tiet/:id', name: 'nhan-vien-detail', component: ChiTietNhanVienPage, meta: { requiresAuth: true, role: 'MANAGER' } },
    { path: '/nhan-vien/sua/:id', name: 'nhan-vien-edit', component: SuaNhanVienPage, meta: { requiresAuth: true, role: 'MANAGER' } },
    { path: '/nhan-vien/them', name: 'nhan-vien-add', component: ThemNhanVienPage, meta: { requiresAuth: true, role: 'MANAGER' } },
    { path: '/cai-dat', name: 'cai-dat', component: CaiDatPage, meta: { requiresAuth: true, role: 'ADMIN' } },
    { path: '/login', name: 'login', component: LoginPage, meta: { requiresAuth: false } },
    { path: '/forgot-password', name: 'forgot-password', component: ForgotPasswordPage, meta: { requiresAuth: false } },
    { path: '/thong-ke', name: 'thong-ke', component: ThongKePage, meta: { requiresAuth: true, role: 'MANAGER' } },
    { path: '/quan-ly-giao-ca', name: 'quan-ly-giao-ca', component: QuanLyGiaoCaPage, meta: { requiresAuth: true, role: 'MANAGER' } },
    { path: '/giao-ca', name: 'giao-ca', component: GiaoCaPage, meta: { requiresAuth: true, role: 'STAFF' } },
    { path: '/giao-ca/create', name: 'giao-ca-create', component: GiaoCaCreatePage, meta: { requiresAuth: true, role: 'STAFF' } },
    { path: '/giao-ca/detail/:id', name: 'giao-ca-detail', component: GiaoCaDetailPage, meta: { requiresAuth: true, role: 'STAFF' } },
    { path: '/ca', name: 'ca', component: CaPage, meta: { requiresAuth: true, role: 'MANAGER' } },
    { path: '/ca/form/:id', name: 'ca-form', component: CaFormPage, meta: { requiresAuth: true, role: 'MANAGER' } },
    { path: '/ca/detail/:id', name: 'ca-detail', component: CaDetailPage, meta: { requiresAuth: true, role: 'MANAGER' } },
    { path: '/phan-ca', name: 'phan-ca', component: PhanCaPage, meta: { requiresAuth: true, role: 'MANAGER' } },
    { path: '/phan-ca/form/:id', name: 'phan-ca-form', component: PhanCaFormPage, meta: { requiresAuth: true, role: 'MANAGER' } },
    { path: '/phan-ca/detail/:id', name: 'phan-ca-detail', component: PhanCaDetailPage, meta: { requiresAuth: true, role: 'MANAGER' } },
    { path: '/dark-mode-demo', name: 'dark-mode-demo', component: DarkModeDemo, meta: { requiresAuth: true } },
    { path: '/messenger-settings', name: 'messenger-settings', component: MessengerSettingsPage, meta: { requiresAuth: true, role: 'ADMIN' } },
    { path: '/test', name: 'test', component: TestPage, meta: { requiresAuth: false } },
    { path: '/simple-test', name: 'simple-test', component: SimpleTestPage, meta: { requiresAuth: false } },
    { path: '/staff-chat', name: 'staff-chat', component: StaffChatDashboard, meta: { requiresAuth: true, role: 'STAFF' } },
    { path: '/customer-chat', name: 'customer-chat', component: CustomerChatPage },
    { path: '/account', name: 'account', component: AccountInfoPage, meta: { requiresAuth: true } },
    { path: '/auth-debug', name: 'auth-debug', component: AuthDebugPage, meta: { requiresAuth: false } },
    { path: '/test-auth', name: 'test-auth', component: TestAuthPage, meta: { requiresAuth: false } },
    { path: '/payment/vnpay-return', name: 'payment-result', component: PaymentResultPage, meta: { requiresAuth: false } },
    { path: '/payment/zalopay-return', name: 'zalopay-result', component: ZaloPayResultPage, meta: { requiresAuth: false } },
    { path: '/logout', redirect: '/login' },
    { path: '/giam-gia', redirect: '/voucher' }, // Redirect old route to new
  ],
})

// Setup auth guard
setupAuthGuard(router)

export default router
=======
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
    { path: '/admin-reviews', name: 'admin-reviews', component: AdminReviewPage },
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
    { path: '/danh-muc', name: 'danh-muc', component: DanhMucPage },
    // Customer product detail page
    { path: '/product/:id', name: 'product-detail', component: ProductDetailPage },
   
  ],
})

export default router
>>>>>>> origin/Huan
