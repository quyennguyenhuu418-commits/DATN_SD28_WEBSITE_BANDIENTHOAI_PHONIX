<template>
  <header class="pos-header">
    <div class="header-left">
      <div class="brand-text">
        <h1 class="main-title">{{ pageTitle }}</h1>
        <p class="subtitle">{{ pageSubtitle }}</p>
      </div>
    </div>
    
    <div class="header-right">
      <div class="time-info">
        <div class="current-time">{{ currentTime }}</div>
        <div class="current-date">{{ currentDate }}</div>
      </div>
      
      <div class="user-info">
        <div class="user-avatar">A</div>
        <div class="user-details">
          <div class="user-name">Admin</div>
          <div class="user-role">Quản lý</div>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const currentTime = ref('')
const currentDate = ref('')
let timeInterval = null

// Page titles mapping
const pageTitles = {
  '/dashboard': 'Dashboard',
  '/thong-ke': 'Thống kê',
  '/san-pham': 'Sản phẩm',
  '/khach-hang': 'Khách hàng',
  '/hoa-don/detail': 'Đơn hàng',
  '/hoa-don': 'Hóa đơn',
  '/voucher': 'Voucher',
  '/customer-vouchers': 'Voucher khách hàng',
  '/giam-gia': 'Giảm giá',
  '/dot-giam-gia': 'Đợt giảm giá',
  '/nhan-vien': 'Nhân viên',
  '/cai-dat': 'Cài đặt',
  '/bao-cao-ban-hang': 'Báo cáo bán hàng',
  '/bao-cao-ton-kho': 'Báo cáo tồn kho',
  '/phan-tich-khach-hang': 'Phân tích khách hàng',
  '/pos': 'POS System',
  '/online': 'Đơn hàng online',
  '/khach-hang/add': 'Thêm khách hàng',
  '/khach-hang/edit': 'Sửa khách hàng',
  '/khach-hang-sidebar': 'Khách hàng',
  '/san-pham/add': 'Thêm sản phẩm',
  '/san-pham/edit': 'Sửa sản phẩm',
  '/san-pham/view': 'Xem chi tiết sản phẩm',
  '/voucher/add': 'Thêm voucher',
  '/voucher/edit': 'Sửa voucher',
  '/danh-muc': 'Danh mục',
  '/hang': 'Hãng',
  '/man-hinh': 'Màn hình',
  '/camera-truoc': 'Camera Trước',
  '/camera-sau': 'Camera Sau',
  '/chip': 'Chip',
  '/gpu': 'GPU',
  '/he-dieu-hanh': 'Hệ điều hành',
  '/cpu': 'CPU',
  '/pin': 'Pin',
  '/ram': 'RAM',
  '/rom': 'ROM',
  '/mau-sac': 'Màu sắc'
}

// Page subtitles mapping
const pageSubtitles = {
  '/dashboard': 'Tổng quan hệ thống',
  '/thong-ke': 'Thống kê và báo cáo',
  '/san-pham': 'Quản lý sản phẩm',
  '/khach-hang': 'Quản lý khách hàng',
  '/hoa-don/detail': 'Tra cứu đơn hàng',
  '/hoa-don': 'Quản lý hóa đơn',
  '/voucher': 'Quản lý voucher',
  '/customer-vouchers': 'Voucher của khách hàng',
  '/giam-gia': 'Quản lý giảm giá',
  '/dot-giam-gia': 'Quản lý đợt giảm giá',
  '/nhan-vien': 'Quản lý nhân viên',
  '/cai-dat': 'Cài đặt hệ thống',
  '/bao-cao-ban-hang': 'Báo cáo bán hàng',
  '/bao-cao-ton-kho': 'Báo cáo tồn kho',
  '/phan-tich-khach-hang': 'Phân tích khách hàng',
  '/pos': 'Hệ thống bán hàng',
  '/online': 'Quản lý đơn hàng online',
  '/khach-hang/add': 'Thêm khách hàng mới',
  '/khach-hang/edit': 'Chỉnh sửa thông tin khách hàng',
  '/khach-hang-sidebar': 'Quản lý khách hàng',
  '/san-pham/add': 'Thêm sản phẩm mới',
  '/san-pham/edit': 'Chỉnh sửa thông tin sản phẩm',
  '/san-pham/view': 'Chi tiết sản phẩm',
  '/voucher/add': 'Thêm voucher mới',
  '/voucher/edit': 'Chỉnh sửa voucher',
  '/danh-muc': 'Quản lý danh mục',
  '/hang': 'Quản lý hãng sản xuất',
  '/man-hinh': 'Quản lý màn hình',
  '/camera-truoc': 'Quản lý camera trước',
  '/camera-sau': 'Quản lý camera sau',
  '/chip': 'Quản lý chip',
  '/gpu': 'Quản lý GPU',
  '/he-dieu-hanh': 'Quản lý hệ điều hành',
  '/cpu': 'Quản lý CPU',
  '/pin': 'Quản lý pin',
  '/ram': 'Quản lý RAM',
  '/rom': 'Quản lý ROM',
  '/mau-sac': 'Quản lý màu sắc'
}

const pageTitle = ref('')
const pageSubtitle = ref('')

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('vi-VN', { 
    hour12: false,
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
  currentDate.value = now.toLocaleDateString('vi-VN', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const updatePageInfo = () => {
  const path = route.path
  pageTitle.value = pageTitles[path] || 'Hệ thống quản lý'
  pageSubtitle.value = pageSubtitles[path] || 'Quản lý hệ thống'
}

onMounted(() => {
  updateTime()
  updatePageInfo()
  timeInterval = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  if (timeInterval) {
    clearInterval(timeInterval)
  }
})

watch(() => route.path, () => {
  updatePageInfo()
})
</script>

<style scoped>
.pos-header {
  background: white;
  padding: 1rem 2rem;
  border-bottom: 1px solid #e2e8f0;
  display: grid;
  grid-template-columns: 1fr auto;
  align-items: center;
  gap: 2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: fixed;
  top: 0;
  left: 280px;
  right: 0;
  z-index: 1000;
  width: calc(100vw - 280px);
  min-height: 80px;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  box-sizing: border-box;
  transform: translateX(0);
  margin: 0;
  max-width: calc(100vw - 280px);
}

/* Khi sidebar co lại */
.sidebar-collapsed + main .pos-header {
  left: 80px;
  width: calc(100vw - 80px);
  max-width: calc(100vw - 80px);
  transform: translateX(0);
}

/* Auto-adjust khi sidebar thay đổi */
.pos-header {
  will-change: left, width;
}

/* Container-based responsive */
.container-fluid .pos-header {
  left: 280px;
  width: calc(100% - 280px);
  max-width: calc(100% - 280px);
}

.container-fluid.sidebar-collapsed .pos-header {
  left: 80px;
  width: calc(100% - 80px);
  max-width: calc(100% - 80px);
}

/* Responsive khi sidebar co/giãn */
@media (max-width: 1200px) {
  .pos-header {
    left: 80px;
    width: calc(100vw - 80px);
    max-width: calc(100vw - 80px);
    padding: 0.75rem 1.5rem;
  }
}

@media (max-width: 768px) {
  .pos-header {
    left: 0;
    width: 100vw;
    max-width: 100vw;
    padding: 0.5rem 1rem;
  }
  
  .main-title {
    font-size: 1.25rem !important;
    max-width: 150px;
  }
  
  .subtitle {
    font-size: 0.75rem !important;
    max-width: 150px;
  }
  
  .current-time {
    font-size: 0.75rem !important;
  }
  
  .current-date {
    font-size: 0.625rem !important;
  }
}

/* Responsive cho màn hình rất nhỏ */
@media (max-width: 480px) {
  .pos-header {
    left: 0;
    width: 100vw;
    max-width: 100vw;
    padding: 0.5rem;
  }
  
  .header-right {
    gap: 1rem;
  }
  
  .current-date {
    display: none;
  }
  
  .user-details {
    display: none;
  }
  
  .main-title {
    font-size: 1rem !important;
    max-width: 100px;
  }
  
  .subtitle {
    font-size: 0.625rem !important;
    max-width: 100px;
  }
}

/* Responsive cho màn hình cực nhỏ */
@media (max-width: 320px) {
  .pos-header {
    padding: 0.25rem;
  }
  
  .main-title {
    font-size: 0.875rem !important;
    max-width: 80px;
  }
  
  .subtitle {
    display: none;
  }
  
  .current-time {
    font-size: 0.625rem !important;
  }
}

/* Đảm bảo header full width bên phải */
.pos-header {
  margin-right: 0;
  padding-right: 2rem;
}

/* Auto-adjust cho tất cả kích thước màn hình */
@media (max-width: 1400px) {
  .pos-header {
    left: 280px;
    width: calc(100vw - 280px);
    max-width: calc(100vw - 280px);
  }
}

@media (max-width: 1024px) {
  .pos-header {
    left: 80px;
    width: calc(100vw - 80px);
    max-width: calc(100vw - 80px);
  }
}

@media (max-width: 640px) {
  .pos-header {
    left: 0;
    width: 100vw;
    max-width: 100vw;
  }
}

/* Fluid responsive - tự động điều chỉnh */
.pos-header {
  min-width: 320px;
  overflow: hidden;
}

/* Viewport-based responsive */
@media (max-width: 1600px) {
  .pos-header {
    left: 280px;
    width: calc(100vw - 280px);
    max-width: calc(100vw - 280px);
  }
}

@media (max-width: 1200px) {
  .pos-header {
    left: 80px;
    width: calc(100vw - 80px);
    max-width: calc(100vw - 80px);
  }
}

@media (max-width: 768px) {
  .pos-header {
    left: 0;
    width: 100vw;
    max-width: 100vw;
  }
}

/* Auto-adjust cho mọi kích thước */
.pos-header {
  position: fixed;
  top: 0;
  z-index: 1000;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/* Dynamic width calculation */
.pos-header {
  width: calc(100vw - var(--sidebar-width, 280px));
  left: var(--sidebar-width, 280px);
  max-width: calc(100vw - var(--sidebar-width, 280px));
}

/* CSS Variables for dynamic adjustment */
:root {
  --sidebar-width: 280px;
}

.sidebar-collapsed {
  --sidebar-width: 80px;
}

@media (max-width: 768px) {
  :root {
    --sidebar-width: 0px;
  }
}

@media (max-width: 480px) {
  .pos-header {
    padding: 0.5rem;
  }
  
  .header-right {
    gap: 1rem;
  }
  
  .current-date {
    display: none;
  }
  
  .user-details {
    display: none;
  }
}

.header-left {
  display: flex;
  align-items: center;
}

.brand-text {
  display: flex;
  flex-direction: column;
}

.main-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
  line-height: 1.2;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px;
}

.subtitle {
  font-size: 0.875rem;
  color: #64748b;
  margin: 0;
  line-height: 1.2;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 2rem;
}

.time-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.125rem;
  min-width: 0;
  flex-shrink: 1;
}

.current-time {
  font-size: 0.875rem;
  font-weight: 600;
  color: #1e293b;
  font-family: 'Courier New', monospace;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.current-date {
  font-size: 0.75rem;
  color: #64748b;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  min-width: 0;
  flex-shrink: 0;
}

.user-avatar {
  width: 40px;
  height: 40px;
  background: #3b82f6;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 1rem;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 0.875rem;
  font-weight: 600;
  color: #1e293b;
  line-height: 1.2;
}

.user-role {
  font-size: 0.75rem;
  color: #64748b;
  line-height: 1.2;
}
</style>
