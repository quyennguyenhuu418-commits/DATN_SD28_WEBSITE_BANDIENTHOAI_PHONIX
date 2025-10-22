const QuanTriApp = () => import('../views/QuanTriApp.vue')
const DashboardPage = () => import('../views/DashboardPage.vue')
const PosPage = () => import('../views/PosPage.vue')
const OnlinePage = () => import('../views/OnlinePage.vue')
const SanPhamPage = () => import('../views/SanPhamPage.vue')
const SanPhamFormPage = () => import('../views/SanPhamFormPage.vue')
const SanPhamViewPage = () => import('../views/SanPhamViewPage.vue')
const HoaDonPage = () => import('../views/HoaDonPage.vue')
const UpdateOrderPage = () => import('../views/UpdateOrderPage.vue')
const VoucherPage = () => import('../views/VoucherPage.vue')
const VoucherFormPage = () => import('../views/VoucherFormPage.vue')
const CustomerVoucherPage = () => import('../views/CustomerVoucherPage.vue')
const ThongKePage = () => import('../views/ThongKePage.vue')
const BaoCaoBanHangPage = () => import('../views/BaoCaoBanHangPage.vue')
const BaoCaoTonKhoPage = () => import('../views/BaoCaoTonKhoPage.vue')
const PhanTichKhachHangPage = () => import('../views/PhanTichKhachHangPage.vue')
const DarkModeDemo = () => import('../views/DarkModeDemo.vue')
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
const DotGiamGiaPage = () => import('../views/DotGiamGiaPage.vue')
const AdminReviewPage = () => import('../views/AdminReviewPage.vue')

const adminRoutes = [
  {
    path: '/admin',
    component: QuanTriApp,
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'admin-dashboard', component: DashboardPage },
      { path: 'pos', name: 'admin-pos', component: PosPage },
      { path: 'online', name: 'admin-online', component: OnlinePage },
      { path: 'san-pham', name: 'admin-san-pham', component: SanPhamPage },
      { path: 'san-pham/form/:id?', name: 'admin-san-pham-form', component: SanPhamFormPage },
      { path: 'san-pham/view/:id', name: 'admin-san-pham-view', component: SanPhamViewPage },
      { path: 'hoa-don', name: 'admin-hoa-don', component: HoaDonPage },
      { path: 'update-order', name: 'admin-update-order', component: UpdateOrderPage },
      { path: 'voucher', name: 'admin-voucher', component: VoucherPage },
      { path: 'voucher/form/:id', name: 'admin-voucher-form', component: VoucherFormPage },
      { path: 'customer-vouchers', name: 'admin-customer-vouchers', component: CustomerVoucherPage },
      { path: 'dot-giam-gia', name: 'admin-dot-giam-gia', component: DotGiamGiaPage },
      { path: 'hang', name: 'admin-hang', component: HangPage },
      { path: 'chip', name: 'admin-chip', component: ChipPage },
      { path: 'ram', name: 'admin-ram', component: RamPage },
      { path: 'rom', name: 'admin-rom', component: RomPage },
      { path: 'man-hinh', name: 'admin-man-hinh', component: ManHinhPage },
      { path: 'mau-sac', name: 'admin-mau-sac', component: MauSacPage },
      { path: 'pin', name: 'admin-pin', component: PinPage },
      { path: 'he-dieu-hanh', name: 'admin-he-dieu-hanh', component: HeDieuHanhPage },
      { path: 'camera-truoc', name: 'admin-camera-truoc', component: CameraTruocPage },
      { path: 'camera-sau', name: 'admin-camera-sau', component: CameraSauPage },
      { path: 'cpu', name: 'admin-cpu', component: CpuPage },
      { path: 'gpu', name: 'admin-gpu', component: GpuPage },
      { path: 'khach-hang', name: 'admin-khach-hang', component: KhachHangPage },
      { path: 'khach-hang-sidebar', name: 'admin-khach-hang-sidebar', component: () => import('../views/KhachHangSidebarPage.vue') },
      { path: 'khach-hang/add', name: 'admin-khach-hang-add', component: () => import('../views/KhachHangAddPage.vue') },
      { path: 'khach-hang/edit/:id', name: 'admin-khach-hang-edit', component: () => import('../views/KhachHangEditPage.vue') },
      { path: 'nhan-vien', name: 'admin-nhan-vien', component: NhanVienPage },
      { path: 'cai-dat', name: 'admin-cai-dat', component: CaiDatPage },
      { path: 'login', name: 'admin-login', component: LoginPage },
      { path: 'thong-ke', name: 'admin-thong-ke', component: ThongKePage },
      { path: 'bao-cao-ban-hang', name: 'admin-bao-cao-ban-hang', component: BaoCaoBanHangPage },
      { path: 'bao-cao-ton-kho', name: 'admin-bao-cao-ton-kho', component: BaoCaoTonKhoPage },
      { path: 'phan-tich-khach-hang', name: 'admin-phan-tich-khach-hang', component: PhanTichKhachHangPage },
      { path: 'dark-mode-demo', name: 'admin-dark-mode-demo', component: DarkModeDemo },
      { path: 'reviews', name: 'admin-reviews', component: AdminReviewPage },
      { path: 'logout', redirect: '/admin/login' },
      { path: 'giam-gia', redirect: '/admin/voucher' }
    ],
    meta: {
      title: 'PhoniX Admin - Quản trị hệ thống',
      description: 'Hệ thống quản trị bán hàng PhoniX Store'
    }
  }
]

export default adminRoutes
