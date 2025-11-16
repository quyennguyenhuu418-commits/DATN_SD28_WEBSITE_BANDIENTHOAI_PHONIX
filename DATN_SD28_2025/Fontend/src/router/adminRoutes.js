const DashboardPage = () => import('../views/DashboardPage.vue')
const PosPage = () => import('../views/PosPage.vue')
const OnlinePage = () => import('../views/OnlinePage.vue')
const SanPhamPage = () => import('../views/SanPhamPage.vue')
const SanPhamFormPage = () => import('../views/SanPhamFormPage.vue')
const SanPhamViewPage = () => import('../views/SanPhamViewPage.vue')
const HoaDonPage = () => import('../views/HoaDonPage.vue')
const HoaDonDetailPage = () => import('../views/HoaDonDetailPage.vue')
const UpdateOrderPage = () => import('../views/UpdateOrderPage.vue')
const VoucherPage = () => import('../views/VoucherPage.vue')
const VoucherFormPage = () => import('../views/VoucherFormPage.vue')
const CustomerVoucherPage = () => import('../views/CustomerVoucherPage.vue')
const ThongKePage = () => import('../views/ThongKePage.vue')
const BaoCaoBanHangPage = () => import('../views/BaoCaoBanHangPage.vue')
const BaoCaoTonKhoPage = () => import('../views/BaoCaoTonKhoPage.vue')
// const PhanTichKhachHangPage = () => import('../views/PhanTichKhachHangPage.vue')
// const DarkModeDemo = () => import('../views/DarkModeDemo.vue')
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
const ForgotPasswordPage = () => import('../views/ForgotPasswordPage.vue')
const DotGiamGiaPage = () => import('../views/DotGiamGiaPage.vue')
const AdminReviewPage = () => import('../views/AdminReviewPage.vue')

const adminRoutes = [
  { path: '/admin', redirect: '/admin/dashboard' },
  { path: '/admin/dashboard', name: 'admin-dashboard', component: DashboardPage, meta: { requiresAuth: true } },
  { path: '/admin/pos', name: 'admin-pos', component: PosPage, meta: { requiresAuth: true, role: 'STAFF' } },
  { path: '/admin/online', name: 'admin-online', component: OnlinePage, meta: { requiresAuth: true, role: 'STAFF' } },
  { path: '/admin/san-pham', name: 'admin-san-pham', component: SanPhamPage, meta: { requiresAuth: true, role: 'STAFF' } },
  { path: '/admin/san-pham/form/:id?', name: 'admin-san-pham-form', component: SanPhamFormPage, meta: { requiresAuth: true, role: 'STAFF' } },
  { path: '/admin/san-pham/view/:id', name: 'admin-san-pham-view', component: SanPhamViewPage, meta: { requiresAuth: true, role: 'STAFF' } },
  { path: '/admin/hoa-don', name: 'admin-hoa-don', component: HoaDonPage, meta: { requiresAuth: true, role: 'STAFF' } },
  { path: '/admin/hoa-don/detail', name: 'admin-hoa-don-detail', component: HoaDonDetailPage, meta: { requiresAuth: true, role: 'STAFF' } },
  { path: '/admin/update-order', name: 'admin-update-order', component: UpdateOrderPage, meta: { requiresAuth: true, role: 'STAFF' } },
  { path: '/admin/voucher', name: 'admin-voucher', component: VoucherPage, meta: { requiresAuth: true, role: 'STAFF' } },
  { path: '/admin/voucher/form/:id', name: 'admin-voucher-form', component: VoucherFormPage, meta: { requiresAuth: true, role: 'STAFF' } },
  { path: '/admin/customer-vouchers', name: 'admin-customer-vouchers', component: CustomerVoucherPage, meta: { requiresAuth: true, role: 'STAFF' } },
  { path: '/admin/dot-giam-gia', name: 'admin-dot-giam-gia', component: DotGiamGiaPage, meta: { requiresAuth: true, role: 'STAFF' } },
  { path: '/admin/hang', name: 'admin-hang', component: HangPage, meta: { requiresAuth: true, role: 'STAFF' } },
  { path: '/admin/chip', name: 'admin-chip', component: ChipPage, meta: { requiresAuth: true, role: 'STAFF' } },
  { path: '/admin/ram', name: 'admin-ram', component: RamPage, meta: { requiresAuth: true, role: 'STAFF' } },
  { path: '/admin/rom', name: 'admin-rom', component: RomPage, meta: { requiresAuth: true, role: 'STAFF' } },
  { path: '/admin/man-hinh', name: 'admin-man-hinh', component: ManHinhPage, meta: { requiresAuth: true, role: 'STAFF' } },
  { path: '/admin/mau-sac', name: 'admin-mau-sac', component: MauSacPage, meta: { requiresAuth: true, role: 'STAFF' } },
  { path: '/admin/pin', name: 'admin-pin', component: PinPage, meta: { requiresAuth: true, role: 'STAFF' } },
  { path: '/admin/he-dieu-hanh', name: 'admin-he-dieu-hanh', component: HeDieuHanhPage, meta: { requiresAuth: true, role: 'STAFF' } },
  { path: '/admin/camera-truoc', name: 'admin-camera-truoc', component: CameraTruocPage, meta: { requiresAuth: true, role: 'STAFF' } },
  { path: '/admin/camera-sau', name: 'admin-camera-sau', component: CameraSauPage, meta: { requiresAuth: true, role: 'STAFF' } },
  { path: '/admin/cpu', name: 'admin-cpu', component: CpuPage, meta: { requiresAuth: true, role: 'STAFF' } },
  { path: '/admin/gpu', name: 'admin-gpu', component: GpuPage, meta: { requiresAuth: true, role: 'STAFF' } },
  { path: '/admin/khach-hang', name: 'admin-khach-hang', component: KhachHangPage, meta: { requiresAuth: true, role: 'STAFF' } },
  { path: '/admin/khach-hang-sidebar', name: 'admin-khach-hang-sidebar', component: () => import('../views/KhachHangSidebarPage.vue'), meta: { requiresAuth: true, role: 'STAFF' } },
  { path: '/admin/khach-hang/add', name: 'admin-khach-hang-add', component: () => import('../views/KhachHangAddPage.vue'), meta: { requiresAuth: true, role: 'STAFF' } },
  { path: '/admin/khach-hang/edit/:id', name: 'admin-khach-hang-edit', component: () => import('../views/KhachHangEditPage.vue'), meta: { requiresAuth: true, role: 'STAFF' } },
  { path: '/admin/nhan-vien', name: 'admin-nhan-vien', component: NhanVienPage, meta: { requiresAuth: true, role: 'MANAGER' } },
  { path: '/admin/cai-dat', name: 'admin-cai-dat', component: CaiDatPage, meta: { requiresAuth: true, role: 'MANAGER' } },
  { path: '/admin/login', name: 'admin-login', component: LoginPage, meta: { requiresAuth: false } }, // Login page không yêu cầu auth
  { path: '/admin/forgot-password', name: 'admin-forgot-password', component: ForgotPasswordPage, meta: { requiresAuth: false } }, // Forgot password page không yêu cầu auth
  { path: '/admin/thong-ke', name: 'admin-thong-ke', component: ThongKePage, meta: { requiresAuth: true, role: 'MANAGER' } },
  { path: '/admin/bao-cao-ban-hang', name: 'admin-bao-cao-ban-hang', component: BaoCaoBanHangPage, meta: { requiresAuth: true, role: 'MANAGER' } },
  { path: '/admin/bao-cao-ton-kho', name: 'admin-bao-cao-ton-kho', component: BaoCaoTonKhoPage, meta: { requiresAuth: true, role: 'MANAGER' } },
  // { path: '/admin/phan-tich-khach-hang', name: 'admin-phan-tich-khach-hang', component: PhanTichKhachHangPage },
  // { path: '/admin/dark-mode-demo', name: 'admin-dark-mode-demo', component: DarkModeDemo },
  { path: '/admin/reviews', name: 'admin-reviews', component: AdminReviewPage, meta: { requiresAuth: true, role: 'STAFF' } },
  { path: '/admin/logout', redirect: '/admin/login' },
  { path: '/admin/giam-gia', redirect: '/admin/voucher' }
]

export default adminRoutes
