<template>
  <div>
    <!-- Main Header Bar (giống POS) -->
    <header class="pos-main-header">
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

  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

// Reactive data
const currentTime = ref('')
const currentDate = ref('')

// Page info mapping
const pageInfo = {
  '/dashboard': { title: 'Dashboard', subtitle: 'Tổng quan hệ thống' },
  '/khach-hang': { title: 'Khách hàng', subtitle: 'Quản lý khách hàng' },
  '/san-pham': { title: 'Sản phẩm', subtitle: 'Quản lý sản phẩm' },
  '/hoa-don': { title: 'Hóa đơn', subtitle: 'Quản lý hóa đơn' },
  '/nhan-vien': { title: 'Nhân viên', subtitle: 'Quản lý nhân viên' },
  '/voucher': { title: 'Phiếu giảm giá', subtitle: 'Quản lý phiếu giảm giá' },
  '/voucher/form': { title: 'Phiếu giảm giá', subtitle: 'Quản lý phiếu giảm giá' },
  '/voucher/detail': { title: 'Phiếu giảm giá', subtitle: 'Quản lý phiếu giảm giá' },
  '/thong-ke': { title: 'Thống kê', subtitle: 'Báo cáo thống kê' },
  '/pos': { title: 'POS System', subtitle: 'Hệ thống bán hàng' }
}

// Computed properties
const pageTitle = computed(() => {
  const path = route.path
  
  // Check for exact match first
  if (pageInfo[path]) {
    return pageInfo[path].title
  }
  
  // Check for pattern matches
  if (path.startsWith('/voucher/form')) {
    return 'Phiếu giảm giá'
  } else if (path.startsWith('/voucher/detail')) {
    return 'Phiếu giảm giá'
  } else if (path.startsWith('/khach-hang/add')) {
    return 'Khách hàng'
  } else if (path.startsWith('/khach-hang/edit')) {
    return 'Khách hàng'
  } else if (path.startsWith('/khach-hang/detail')) {
    return 'Khách hàng'
  } else if (path.startsWith('/san-pham/add')) {
    return 'Sản phẩm'
  } else if (path.startsWith('/san-pham/edit')) {
    return 'Sản phẩm'
  } else if (path.startsWith('/san-pham/view')) {
    return 'Sản phẩm'
  } else if (path.startsWith('/nhan-vien/add')) {
    return 'Nhân viên'
  } else if (path.startsWith('/nhan-vien/edit')) {
    return 'Nhân viên'
  } else if (path.startsWith('/nhan-vien/detail')) {
    return 'Nhân viên'
  } else if (path.startsWith('/hoa-don/detail')) {
    return 'Hóa đơn'
  } else if (path.startsWith('/thong-ke')) {
    return 'Thống kê'
  } else if (path.startsWith('/pos')) {
    return 'POS System'
  } else if (path.startsWith('/online')) {
    return 'Đơn hàng online'
  } else if (path.startsWith('/customer-vouchers')) {
    return 'Phiếu giảm giá khách hàng'
  } else if (path.startsWith('/cai-dat')) {
    return 'Cài đặt'
  } else if (path.startsWith('/dot-giam-gia')) {
    return 'Đợt giảm giá'
  } else if (path.startsWith('/hang')) {
    return 'Hãng sản xuất'
  } else if (path.startsWith('/man-hinh')) {
    return 'Màn hình'
  } else if (path.startsWith('/camera-truoc')) {
    return 'Camera trước'
  } else if (path.startsWith('/camera-sau')) {
    return 'Camera sau'
  } else if (path.startsWith('/chip')) {
    return 'Chip'
  } else if (path.startsWith('/gpu')) {
    return 'GPU'
  } else if (path.startsWith('/he-dieu-hanh')) {
    return 'Hệ điều hành'
  } else if (path.startsWith('/cpu')) {
    return 'CPU'
  } else if (path.startsWith('/pin')) {
    return 'Pin'
  } else if (path.startsWith('/ram')) {
    return 'RAM'
  } else if (path.startsWith('/rom')) {
    return 'ROM'
  } else if (path.startsWith('/mau-sac')) {
    return 'Màu sắc'
  } else {
    return 'Trang chủ'
  }
})

const pageSubtitle = computed(() => {
  const path = route.path
  
  // Check for exact match first
  if (pageInfo[path]) {
    return pageInfo[path].subtitle
  }
  
  // Check for pattern matches
  if (path.startsWith('/voucher/form')) {
    return 'Quản lý phiếu giảm giá'
  } else if (path.startsWith('/voucher/detail')) {
    return 'Quản lý phiếu giảm giá'
  } else if (path.startsWith('/khach-hang/add')) {
    return 'Thêm khách hàng mới'
  } else if (path.startsWith('/khach-hang/edit')) {
    return 'Chỉnh sửa thông tin khách hàng'
  } else if (path.startsWith('/khach-hang/detail')) {
    return 'Chi tiết khách hàng'
  } else if (path.startsWith('/san-pham/add')) {
    return 'Thêm sản phẩm mới'
  } else if (path.startsWith('/san-pham/edit')) {
    return 'Chỉnh sửa thông tin sản phẩm'
  } else if (path.startsWith('/san-pham/view')) {
    return 'Chi tiết sản phẩm'
  } else if (path.startsWith('/nhan-vien/add')) {
    return 'Thêm nhân viên mới'
  } else if (path.startsWith('/nhan-vien/edit')) {
    return 'Chỉnh sửa thông tin nhân viên'
  } else if (path.startsWith('/nhan-vien/detail')) {
    return 'Chi tiết nhân viên'
  } else if (path.startsWith('/hoa-don/detail')) {
    return 'Chi tiết hóa đơn'
  } else if (path.startsWith('/thong-ke')) {
    return 'Thống kê và báo cáo'
  } else if (path.startsWith('/pos')) {
    return 'Hệ thống bán hàng'
  } else if (path.startsWith('/online')) {
    return 'Quản lý đơn hàng online'
  } else if (path.startsWith('/customer-vouchers')) {
    return 'Phiếu giảm giá của khách hàng'
  } else if (path.startsWith('/cai-dat')) {
    return 'Cài đặt hệ thống'
  } else if (path.startsWith('/dot-giam-gia')) {
    return 'Quản lý đợt giảm giá'
  } else if (path.startsWith('/hang')) {
    return 'Quản lý hãng sản xuất'
  } else if (path.startsWith('/man-hinh')) {
    return 'Quản lý màn hình'
  } else if (path.startsWith('/camera-truoc')) {
    return 'Quản lý camera trước'
  } else if (path.startsWith('/camera-sau')) {
    return 'Quản lý camera sau'
  } else if (path.startsWith('/chip')) {
    return 'Quản lý chip'
  } else if (path.startsWith('/gpu')) {
    return 'Quản lý GPU'
  } else if (path.startsWith('/he-dieu-hanh')) {
    return 'Quản lý hệ điều hành'
  } else if (path.startsWith('/cpu')) {
    return 'Quản lý CPU'
  } else if (path.startsWith('/pin')) {
    return 'Quản lý pin'
  } else if (path.startsWith('/ram')) {
    return 'Quản lý RAM'
  } else if (path.startsWith('/rom')) {
    return 'Quản lý ROM'
  } else if (path.startsWith('/mau-sac')) {
    return 'Quản lý màu sắc'
  } else {
    return 'Hệ thống quản lý'
  }
})


// Methods
const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('vi-VN', { 
    hour12: false,
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
  
  const options = { 
    weekday: 'long', 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric' 
  }
  currentDate.value = now.toLocaleDateString('vi-VN', options)
}


// Lifecycle
onMounted(() => {
  updateTime()
  const interval = setInterval(updateTime, 1000)
  
  onUnmounted(() => {
    clearInterval(interval)
  })
})
</script>

<style scoped>
/* Main Header Bar - giống hệt POS */
.pos-main-header {
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
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-sizing: border-box;
  transform: translateX(0);
  margin: 0;
  max-width: calc(100vw - 280px);
}

/* Khi sidebar co lại */
.sidebar-collapsed + main .pos-main-header {
  left: 80px;
  width: calc(100vw - 80px);
  max-width: calc(100vw - 80px);
  transform: translateX(0);
}

/* Auto-adjust khi sidebar thay đổi */
.pos-main-header {
  will-change: left, width;
}

/* Container-based responsive */
.container-fluid .pos-main-header {
  left: 280px;
  width: calc(100% - 280px);
  max-width: calc(100% - 280px);
}

.container-fluid.sidebar-collapsed .pos-main-header {
  left: 80px;
  width: calc(100% - 80px);
  max-width: calc(100% - 80px);
}

/* Responsive khi sidebar co/giãn */
@media (max-width: 1200px) {
  .pos-main-header {
    left: 80px;
    width: calc(100vw - 80px);
    max-width: calc(100vw - 80px);
    padding: 0.75rem 1.5rem;
  }
}

@media (max-width: 768px) {
  .pos-main-header {
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
  .pos-main-header {
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
  .pos-main-header {
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

/* Viewport-based responsive */
@media (max-width: 1600px) {
  .pos-main-header {
    left: 280px;
    width: calc(100vw - 280px);
    max-width: calc(100vw - 280px);
  }
}

@media (max-width: 1200px) {
  .pos-main-header {
    left: 80px;
    width: calc(100vw - 80px);
    max-width: calc(100vw - 80px);
  }
}

@media (max-width: 768px) {
  .pos-main-header {
    left: 0;
    width: 100vw;
    max-width: 100vw;
  }
}

/* Fluid responsive - tự động điều chỉnh */
.pos-main-header {
  min-width: 320px;
  overflow: hidden;
}

/* Dynamic width calculation */
.pos-main-header {
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
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 1.5rem;
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
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.current-date {
  font-size: 0.75rem;
  color: #64748b;
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


