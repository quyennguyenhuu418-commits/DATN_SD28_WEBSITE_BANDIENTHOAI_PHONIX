<template>
  <header class="pos-header">
    <div class="header-left">
      <div class="brand-text">
        <h1 class="main-title">{{ pageTitle }}</h1>
        <p class="subtitle">{{ pageSubtitle }}</p>
      </div>
    </div>
    
    <div class="header-center">
      <!-- Empty space for proper alignment -->
    </div>
    
    <div class="header-right">
      <div class="time-info">
        <div class="current-time">{{ currentTime }}</div>
        <div class="current-date">{{ currentDate }}</div>
      </div>
      
      <!-- Chat Toggle Button (for support pages) -->
      <div v-if="showChatToggle" class="chat-toggle-container">
        <button class="chat-toggle-btn" @click="toggleChat" :title="isChatVisible ? 'Ẩn chat' : 'Hiện chat'">
          <svg v-if="isChatVisible" class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
          </svg>
          <svg v-else class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"></path>
          </svg>
        </button>
      </div>
      
      <!-- Notification Bell -->
      <div class="notification-container">
        <button class="notification-btn" @click="toggleNotifications" :class="{ 'has-unread': unreadCount > 0 }">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
          </svg>
          <span v-if="unreadCount > 0" class="notification-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
        </button>
        
        
        <!-- Notification Modal using Teleport -->
        <Teleport to="body">
          <div v-if="showNotifications" class="notification-modal-overlay" @click="closeModal">
            <div class="notification-modal" @click.stop>
              <div class="notification-modal-header">
                <div class="header-left">
                  <h2 class="notification-modal-title">Thông Báo Mới Nhận</h2>
                  <span v-if="unreadCount > 0" class="unread-count-badge">{{ unreadCount }} chưa đọc</span>
                </div>
                <div class="header-right">
                  <button v-if="unreadCount > 0" @click="markAllAsRead" class="mark-all-read-btn">
                    Đánh dấu tất cả đã đọc
                  </button>
                  <button @click="closeModal" class="close-btn">
                    <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </button>
                </div>
              </div>
              
              <div class="notification-modal-content">
                <!-- Dynamic notifications from database -->
                <div v-if="notifications.length === 0" class="no-notifications">
                  <div class="no-notifications-icon">
                    <svg class="w-16 h-16 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                    </svg>
                  </div>
                  <h3>Không có thông báo mới</h3>
                  <p>Bạn sẽ nhận được thông báo khi có hoạt động mới trong hệ thống</p>
                </div>

                <div v-for="notification in notifications" :key="notification.id" 
                     class="notification-card" 
                     :class="{ 
                       'urgent': notification.loaiThongBao === 'ORDER_NEW',
                       'warning': notification.loaiThongBao === 'PRODUCT_NEW',
                       'unread': notification.trangThai === 0,
                       'read': notification.trangThai === 1
                     }"
                     @click="markAsRead(notification.id)">
                  <div class="notification-card-header">
                    <div class="notification-card-icon" :class="getNotificationIconClass(notification.loaiThongBao)">
                      <div class="notification-badge-text">{{ getNotificationBadge(notification.loaiThongBao) }}</div>
                    </div>
                    <div class="notification-card-title">{{ notification.tieuDe }}</div>
                  </div>
                  <div class="notification-card-body">
                    <p>{{ notification.noiDung }}</p>
                    <div class="notification-meta">
                      <span class="notification-time">{{ formatTime(notification.ngayTao) }}</span>
                      <span v-if="notification.trangThai === 0" class="unread-indicator">Mới</span>
                    </div>
                  </div>
                </div>
              </div>
              
              <div class="notification-modal-footer">
                <button @click="showAllNotifications" class="view-all-btn">
                  Xem tất cả
                </button>
              </div>
            </div>
          </div>
        </Teleport>
      </div>
      
      <!-- Header Logout Dropdown Component -->
      <HeaderLogoutDropdown 
        :isCollapsed="false" 
        @open-change-password="showChangePasswordModal = true"
      />
    </div>
  </header>
  
  <!-- Change Password Modal -->
  <ChangePasswordModal 
    :isOpen="showChangePasswordModal" 
    @close="closeChangePasswordModal" 
  />
  
  <!-- Debug info -->
  <div v-if="showChangePasswordModal" style="position: fixed; top: 10px; left: 10px; background: red; color: white; padding: 10px; z-index: 99999;">
    Modal should be open: {{ showChangePasswordModal }}
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import HeaderLogoutDropdown from './HeaderLogoutDropdown.vue'
import ChangePasswordModal from './ChangePasswordModal.vue'
import api from '../services/api'

const route = useRoute()
const currentTime = ref('')
const currentDate = ref('')
let timeInterval = null

// Chat toggle state
const isChatVisible = ref(true)

// Notification state
const showNotifications = ref(false)
const notifications = ref([])
const unreadCount = ref(0)
let notificationInterval = null

// Change password modal state
const showChangePasswordModal = ref(false)

// Props
const props = defineProps({
  pageTitle: {
    type: String,
    default: ''
  },
  pageSubtitle: {
    type: String,
    default: ''
  },
  showChatToggle: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['toggle-chat'])

// Page titles mapping
const pageTitles = {
  '/trang-chu': 'Trang chủ',
  '/dashboard': 'Trang chủ', // Redirect support
  '/thong-ke': 'Thống kê',
  '/san-pham': 'Sản phẩm',
  '/khach-hang': 'Khách hàng',
  '/hoa-don': 'Hóa đơn',
  '/phieu-giam-gia': 'Phiếu giảm giá',
  '/phieu-giam-gia/form': 'Phiếu giảm giá',
  '/phieu-giam-gia/detail': 'Phiếu giảm giá',
  '/voucher': 'Phiếu giảm giá', // Redirect support
  '/voucher/form': 'Phiếu giảm giá', // Redirect support
  '/voucher/detail': 'Phiếu giảm giá', // Redirect support
  '/dot-giam-gia': 'Đợt giảm giá',
  '/nhan-vien': 'Quản lý nhân viên',
  '/staff-chat': 'Chat hỗ trợ',
  '/cai-dat': 'Cài đặt',
  '/pos': 'Bán hàng',
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
  '/trang-chu': 'Tổng quan hệ thống',
  '/dashboard': 'Tổng quan hệ thống', // Redirect support
  '/thong-ke': 'Thống kê và báo cáo',
  '/san-pham': 'Quản lý sản phẩm',
  '/khach-hang': 'Quản lý khách hàng',
  '/hoa-don': 'Quản lý hóa đơn',
  '/phieu-giam-gia': 'Quản lý phiếu giảm giá',
  '/phieu-giam-gia/form': 'Quản lý phiếu giảm giá',
  '/phieu-giam-gia/detail': 'Quản lý phiếu giảm giá',
  '/voucher': 'Quản lý phiếu giảm giá', // Redirect support
  '/voucher/form': 'Quản lý phiếu giảm giá', // Redirect support
  '/voucher/detail': 'Quản lý phiếu giảm giá', // Redirect support
  '/dot-giam-gia': 'Quản lý đợt giảm giá',
  '/nhan-vien': 'Nhân viên',
  '/nhan-vien/chi-tiet': 'Chi tiết nhân viên',
  '/nhan-vien/sua': 'Chỉnh sửa nhân viên',
  '/nhan-vien/them': 'Thêm nhân viên mới',
  '/staff-chat': 'Hỗ trợ',
  '/cai-dat': 'Cài đặt hệ thống',
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

// Notification functions
const loadNotifications = async () => {
  try {
    const response = await api.get('/api/notifications/recent')
    const previousCount = notifications.value.length
    notifications.value = response.data || []
    
    // Play sound if new notifications
    if (notifications.value.length > previousCount) {
      playNotificationSound()
    }
  } catch (error) {
    console.error('Error loading notifications:', error)
  }
}

const loadUnreadCount = async () => {
  try {
    const response = await api.get('/api/notifications/unread-count')
    unreadCount.value = response.data.count || 0
  } catch (error) {
    console.error('Error loading unread count:', error)
  }
}

const toggleNotifications = () => {
  showNotifications.value = !showNotifications.value
  if (showNotifications.value) {
    loadNotifications()
  }
}

const toggleChat = () => {
  isChatVisible.value = !isChatVisible.value
  emit('toggle-chat', isChatVisible.value)
}

// Đóng dropdown khi click bên ngoài
const handleClickOutside = (event) => {
  if (showNotifications.value && !event.target.closest('.notification-container')) {
    showNotifications.value = false
  }
}

const markAsRead = async (id) => {
  try {
    console.log('Marking notification as read:', id)
    console.log('Current unread count before:', unreadCount.value)
    
    // Tìm notification trong danh sách
    const notification = notifications.value.find(n => n.id === id)
    console.log('Found notification:', notification)
    
    // Nếu notification chưa được đánh dấu đã đọc
    if (notification && notification.trangThai === 0) {
      console.log('Notification is unread, marking as read...')
      
      // Cập nhật UI ngay lập tức
      notification.trangThai = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
      
      console.log('UI updated. New unread count:', unreadCount.value)
      
      // Gọi API để cập nhật database
      const response = await api.put(`/api/notifications/${id}/mark-read`)
      console.log('API response:', response)
      
      if (response.data.updatedCount > 0) {
        console.log(`Successfully marked notification ${id} as read. Unread count: ${unreadCount.value}`)
      } else {
        console.log('Notification was already read or not found')
        // Revert UI changes if notification was already read
        notification.trangThai = 0
        unreadCount.value += 1
      }
    } else {
      console.log('Notification already read or not found')
    }
  } catch (error) {
    console.error('Error marking notification as read:', error)
    console.error('Error details:', error.response?.data || error.message)
    
    // Nếu API call thất bại, revert lại UI
    const notification = notifications.value.find(n => n.id === id)
    if (notification) {
      notification.trangThai = 0
      unreadCount.value += 1
      console.log('Reverted UI changes due to API error')
    }
  }
}

const markAllAsRead = async () => {
  try {
    // Cập nhật UI ngay lập tức
    notifications.value.forEach(notification => {
      if (notification.trangThai === 0) {
        notification.trangThai = 1
      }
    })
    unreadCount.value = 0
    
    // Gọi API để cập nhật database
    const response = await api.put('/api/notifications/mark-all-read')
    console.log('Mark all read API response:', response)
    
    console.log(`Marked all notifications as read. Updated count: ${response.data.updatedCount}`)
  } catch (error) {
    console.error('Error marking all as read:', error)
    // Nếu API call thất bại, reload lại data
    loadNotifications()
    loadUnreadCount()
  }
}

const showAllNotifications = () => {
  // Navigate to notifications page or show all
  console.log('Show all notifications')
  showNotifications.value = false
}

const closeModal = () => {
  showNotifications.value = false
}

const closeChangePasswordModal = () => {
  showChangePasswordModal.value = false
}

const getNotificationIconClass = (loaiThongBao) => {
  switch (loaiThongBao) {
    case 'ORDER_NEW':
      return 'urgent-icon'
    case 'PRODUCT_NEW':
      return 'warning-icon'
    case 'CUSTOMER_NEW':
      return 'customer-icon'
    case 'CUSTOMER_CHAT':
      return 'chat-icon'
    case 'EMPLOYEE_NEW':
      return 'employee-icon'
    case 'SYSTEM':
      return 'system-icon'
    default:
      return 'default-icon'
  }
}

const getNotificationBadge = (loaiThongBao) => {
  switch (loaiThongBao) {
    case 'ORDER_NEW':
      return 'ĐƠN HÀNG'
    case 'PRODUCT_NEW':
      return 'SẢN PHẨM'
    case 'CUSTOMER_NEW':
      return 'KHÁCH HÀNG'
    case 'CUSTOMER_CHAT':
      return 'TƯ VẤN'
    case 'EMPLOYEE_NEW':
      return 'NHÂN VIÊN'
    case 'SYSTEM':
      return 'HỆ THỐNG'
    default:
      return 'THÔNG BÁO'
  }
}

const formatTime = (dateString) => {
  const date = new Date(dateString)
  const now = new Date()
  const diffInMinutes = Math.floor((now - date) / (1000 * 60))
  
  if (diffInMinutes < 1) return 'Vừa xong'
  if (diffInMinutes < 60) return `${diffInMinutes} phút trước`
  
  const diffInHours = Math.floor(diffInMinutes / 60)
  if (diffInHours < 24) return `${diffInHours} giờ trước`
  
  const diffInDays = Math.floor(diffInHours / 24)
  if (diffInDays < 7) return `${diffInDays} ngày trước`
  
  return date.toLocaleDateString('vi-VN')
}

const playNotificationSound = () => {
  try {
    // Create notification sound using Web Audio API
    const audioContext = new (window.AudioContext || window.webkitAudioContext)()
    
    // Create ting ting ting sound - 3 quick bell-like sounds
    const createTing = (frequency, startTime, duration = 0.2) => {
      const oscillator = audioContext.createOscillator()
      const gainNode = audioContext.createGain()
      
      // Create a bell-like sound with quick decay
      oscillator.type = 'sine'
      oscillator.frequency.setValueAtTime(frequency, startTime)
      
      // Quick attack, quick decay - like a bell
      gainNode.gain.setValueAtTime(0, startTime)
      gainNode.gain.linearRampToValueAtTime(0.3, startTime + 0.01)  // Quick attack
      gainNode.gain.exponentialRampToValueAtTime(0.01, startTime + duration) // Quick decay
      
      oscillator.connect(gainNode)
      gainNode.connect(audioContext.destination)
      
      oscillator.start(startTime)
      oscillator.stop(startTime + duration)
    }
    
    const baseTime = audioContext.currentTime
    
    // Ting 1 - High pitch
    createTing(1760.00, baseTime, 0.15) // A6
    
    // Ting 2 - Medium pitch (slightly lower)
    createTing(1567.98, baseTime + 0.2, 0.15) // G6
    
    // Ting 3 - Lower pitch
    createTing(1318.51, baseTime + 0.4, 0.15) // E6
    
    console.log('🔔 Ting ting ting notification sound played')
  } catch (error) {
    console.error('Error playing notification sound:', error)
  }
}

const updatePageInfo = () => {
  const path = route.path
  
  // Check for exact match first
  if (pageTitles[path]) {
    pageTitle.value = pageTitles[path]
    pageSubtitle.value = pageSubtitles[path]
    return
  }
  
  // Check for pattern matches
  if (path.startsWith('/voucher/form')) {
    pageTitle.value = 'Phiếu giảm giá'
    pageSubtitle.value = 'Quản lý phiếu giảm giá'
  } else if (path.startsWith('/voucher/detail')) {
    pageTitle.value = 'Phiếu giảm giá'
    pageSubtitle.value = 'Quản lý phiếu giảm giá'
  } else if (path.startsWith('/khach-hang/add')) {
    pageTitle.value = 'Khách hàng'
    pageSubtitle.value = 'Thêm khách hàng mới'
  } else if (path.startsWith('/khach-hang/edit')) {
    pageTitle.value = 'Khách hàng'
    pageSubtitle.value = 'Chỉnh sửa thông tin khách hàng'
  } else if (path.startsWith('/khach-hang/detail')) {
    pageTitle.value = 'Khách hàng'
    pageSubtitle.value = 'Chi tiết khách hàng'
  } else if (path.startsWith('/san-pham/add')) {
    pageTitle.value = 'Sản phẩm'
    pageSubtitle.value = 'Thêm sản phẩm mới'
  } else if (path.startsWith('/san-pham/edit')) {
    pageTitle.value = 'Sản phẩm'
    pageSubtitle.value = 'Chỉnh sửa thông tin sản phẩm'
  } else if (path.startsWith('/san-pham/view')) {
    pageTitle.value = 'Sản phẩm'
    pageSubtitle.value = 'Chi tiết sản phẩm'
  } else if (path.startsWith('/nhan-vien/add')) {
    pageTitle.value = 'Nhân viên'
    pageSubtitle.value = 'Thêm nhân viên mới'
  } else if (path.startsWith('/nhan-vien/edit')) {
    pageTitle.value = 'Nhân viên'
    pageSubtitle.value = 'Chỉnh sửa thông tin nhân viên'
  } else if (path.startsWith('/nhan-vien/detail')) {
    pageTitle.value = 'Nhân viên'
    pageSubtitle.value = 'Chi tiết nhân viên'
  } else if (path.startsWith('/hoa-don/detail')) {
    pageTitle.value = 'Hóa đơn'
    pageSubtitle.value = 'Chi tiết hóa đơn'
  } else if (path.startsWith('/thong-ke')) {
    pageTitle.value = 'Thống kê'
    pageSubtitle.value = 'Thống kê và báo cáo'
  } else if (path.startsWith('/bao-cao-ban-hang')) {
    pageTitle.value = 'Báo cáo bán hàng'
    pageSubtitle.value = 'Báo cáo bán hàng'
  } else if (path.startsWith('/bao-cao-ton-kho')) {
    pageTitle.value = 'Báo cáo tồn kho'
    pageSubtitle.value = 'Báo cáo tồn kho'
  } else if (path.startsWith('/phan-tich-khach-hang')) {
    pageTitle.value = 'Phân tích khách hàng'
    pageSubtitle.value = 'Phân tích khách hàng'
  } else if (path.startsWith('/pos')) {
    pageTitle.value = 'POS System'
    pageSubtitle.value = 'Hệ thống bán hàng'
  } else if (path.startsWith('/online')) {
    pageTitle.value = 'Đơn hàng online'
    pageSubtitle.value = 'Quản lý đơn hàng online'
  } else if (path.startsWith('/cai-dat')) {
    pageTitle.value = 'Cài đặt'
    pageSubtitle.value = 'Cài đặt hệ thống'
  } else if (path.startsWith('/dot-giam-gia')) {
    pageTitle.value = 'Đợt giảm giá'
    pageSubtitle.value = 'Quản lý đợt giảm giá'
  } else if (path.startsWith('/hang')) {
    pageTitle.value = 'Hãng sản xuất'
    pageSubtitle.value = 'Quản lý hãng sản xuất'
  } else if (path.startsWith('/man-hinh')) {
    pageTitle.value = 'Màn hình'
    pageSubtitle.value = 'Quản lý màn hình'
  } else if (path.startsWith('/camera-truoc')) {
    pageTitle.value = 'Camera trước'
    pageSubtitle.value = 'Quản lý camera trước'
  } else if (path.startsWith('/camera-sau')) {
    pageTitle.value = 'Camera sau'
    pageSubtitle.value = 'Quản lý camera sau'
  } else if (path.startsWith('/chip')) {
    pageTitle.value = 'Chip'
    pageSubtitle.value = 'Quản lý chip'
  } else if (path.startsWith('/gpu')) {
    pageTitle.value = 'GPU'
    pageSubtitle.value = 'Quản lý GPU'
  } else if (path.startsWith('/he-dieu-hanh')) {
    pageTitle.value = 'Hệ điều hành'
    pageSubtitle.value = 'Quản lý hệ điều hành'
  } else if (path.startsWith('/cpu')) {
    pageTitle.value = 'CPU'
    pageSubtitle.value = 'Quản lý CPU'
  } else if (path.startsWith('/pin')) {
    pageTitle.value = 'Pin'
    pageSubtitle.value = 'Quản lý pin'
  } else if (path.startsWith('/ram')) {
    pageTitle.value = 'RAM'
    pageSubtitle.value = 'Quản lý RAM'
  } else if (path.startsWith('/rom')) {
    pageTitle.value = 'ROM'
    pageSubtitle.value = 'Quản lý ROM'
  } else if (path.startsWith('/mau-sac')) {
    pageTitle.value = 'Màu sắc'
    pageSubtitle.value = 'Quản lý màu sắc'
  } else {
    pageTitle.value = 'Quản lý nhân viên'
    pageSubtitle.value = 'Nhân viên'
  }
}

onMounted(() => {
  updateTime()
  updatePageInfo()
  timeInterval = setInterval(updateTime, 1000)
  
  // Load notifications
  loadUnreadCount()
  loadNotifications()
  notificationInterval = setInterval(() => {
    const previousUnreadCount = unreadCount.value
    loadUnreadCount()
    loadNotifications()
    
    // Play sound if new unread notifications
    if (unreadCount.value > previousUnreadCount) {
      playNotificationSound()
    }
  }, 5000) // Check every 5 seconds
  
  // Add click outside listener
  document.addEventListener('click', handleClickOutside)
  
  // Event listeners for modals are now handled via component props/emits
})

onUnmounted(() => {
  if (timeInterval) {
    clearInterval(timeInterval)
  }
  if (notificationInterval) {
    clearInterval(notificationInterval)
  }
  
  // Remove click outside listener
  document.removeEventListener('click', handleClickOutside)
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
  grid-template-columns: auto 1fr auto;
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
  justify-content: flex-start;
}

.brand-text {
  display: flex;
  flex-direction: column;
  text-align: left;
  align-items: flex-start;
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
  text-align: left;
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
  text-align: left;
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

/* Notification Styles */
.notification-container {
  position: relative;
}

.notification-btn {
  position: relative;
  background: none;
  border: none;
  padding: 0.5rem;
  border-radius: 0.5rem;
  cursor: pointer;
  color: #64748b;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notification-btn:hover {
  background: #f1f5f9;
  color: #1e293b;
}

.notification-btn.has-unread {
  color: #3b82f6;
}

.notification-badge {
  position: absolute;
  top: -0.25rem;
  right: -0.25rem;
  background: #ef4444;
  color: white;
  border-radius: 50%;
  width: 1.25rem;
  height: 1.25rem;
  font-size: 0.625rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 1.25rem;
}

/* Notification Modal */
.notification-modal-overlay {
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  right: 0 !important;
  bottom: 0 !important;
  width: 100vw !important;
  height: 100vh !important;
  background: rgba(0, 0, 0, 0.5);
  z-index: 99999 !important;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  margin: 0 !important;
}

.notification-modal {
  background: white;
  border-radius: 1rem;
  width: 100%;
  max-width: 600px;
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  position: relative;
  z-index: 100000;
}

.notification-modal-header {
  padding: 1.5rem;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8fafc;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.unread-count-badge {
  background: #3b82f6;
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 1rem;
  font-size: 0.75rem;
  font-weight: 600;
}

.mark-all-read-btn {
  background: #10b981;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 0.375rem;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s ease;
}

.mark-all-read-btn:hover {
  background: #059669;
}

.notification-modal-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  padding: 0.5rem;
  border-radius: 0.5rem;
  cursor: pointer;
  color: #64748b;
  transition: all 0.2s ease;
}

.close-btn:hover {
  background: #f1f5f9;
  color: #1e293b;
}

.notification-modal-content {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
  max-height: 60vh;
}

.notification-card {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 0.75rem;
  margin-bottom: 1rem;
  padding: 1rem;
  transition: all 0.2s ease;
}

.notification-card:hover {
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.notification-card.urgent {
  border-left: 4px solid #ef4444;
  background: #fef2f2;
}

.notification-card.warning {
  border-left: 4px solid #f59e0b;
  background: #fffbeb;
}

.notification-card.unread {
  background: #eff6ff;
  border-left: 3px solid #3b82f6;
}

.notification-card.read {
  background: #f8fafc;
  opacity: 0.8;
}

.notification-card.read .notification-card-title {
  color: #64748b;
}

.notification-card.read .notification-card-body {
  color: #94a3b8;
}

.notification-card-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 0.75rem;
}

.notification-card-icon {
  width: 50px;
  height: 50px;
  border-radius: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 0.75rem;
  color: white;
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
}

.notification-card-icon.urgent-icon {
  background: linear-gradient(135deg, #ef4444, #dc2626);
}

.notification-card-icon.warning-icon {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.notification-card-icon.customer-icon {
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
}

.notification-card-icon.chat-icon {
  background: linear-gradient(135deg, #e74c3c, #c0392b);
}

.notification-card-icon.employee-icon {
  background: linear-gradient(135deg, #06b6d4, #0891b2);
}

.notification-card-icon.system-icon {
  background: linear-gradient(135deg, #6b7280, #4b5563);
}

.notification-card-icon.default-icon {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
}

.discount-badge, .urgent-badge, .order-badge, .warning-badge, .sale-badge, .notification-badge-text {
  text-align: center;
  line-height: 1.2;
  font-size: 0.625rem;
  font-weight: 700;
}

.notification-card-title {
  font-size: 1rem;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
  flex: 1;
}

.notification-card-body {
  color: #64748b;
  line-height: 1.5;
}

.notification-card-body p {
  margin: 0.25rem 0;
  font-size: 0.875rem;
}

.notification-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 0.5rem;
  padding-top: 0.5rem;
  border-top: 1px solid #f1f5f9;
}

.unread-indicator {
  background: #3b82f6;
  color: white;
  padding: 0.125rem 0.5rem;
  border-radius: 0.25rem;
  font-size: 0.625rem;
  font-weight: 600;
}

.no-notifications {
  text-align: center;
  padding: 3rem 1rem;
  color: #64748b;
}

.no-notifications-icon {
  margin-bottom: 1rem;
}

.no-notifications h3 {
  font-size: 1.125rem;
  font-weight: 600;
  color: #374151;
  margin: 0 0 0.5rem 0;
}

.no-notifications p {
  font-size: 0.875rem;
  margin: 0;
}

.notification-modal-footer {
  padding: 1.5rem;
  border-top: 1px solid #e2e8f0;
  background: #f8fafc;
  text-align: center;
}

.view-all-btn {
  background: #3b82f6;
  color: white;
  border: none;
  padding: 0.75rem 2rem;
  border-radius: 0.5rem;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s ease;
}

.view-all-btn:hover {
  background: #2563eb;
}

/* Responsive */
@media (max-width: 768px) {
  .notification-modal {
    max-width: 95vw;
    margin: 0.5rem;
  }
  
  .notification-modal-header {
    padding: 1rem;
  }
  
  .notification-modal-content {
    padding: 0.75rem;
  }
  
  .notification-card {
    padding: 0.75rem;
  }
  
  .notification-card-header {
    gap: 0.75rem;
  }
  
  .notification-card-icon {
    width: 40px;
    height: 40px;
    font-size: 0.625rem;
  }
}

/* Đảm bảo modal hiển thị trên tất cả */
.notification-modal-overlay {
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  right: 0 !important;
  bottom: 0 !important;
  width: 100vw !important;
  height: 100vh !important;
  z-index: 99999 !important;
}

/* Đảm bảo không có container nào che khuất */
.notification-modal-overlay * {
  z-index: 100000 !important;
}

/* Reset tất cả z-index conflicts */
body:has(.notification-modal-overlay) {
  overflow: hidden;
}

/* Đảm bảo modal không bị ảnh hưởng bởi parent containers */
.notification-container {
  position: static !important;
}

.notification-modal-overlay {
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  right: 0 !important;
  bottom: 0 !important;
  width: 100vw !important;
  height: 100vh !important;
  z-index: 99999 !important;
  margin: 0 !important;
  padding: 0 !important;
}

/* Chat Toggle Button Styles */
.chat-toggle-container {
  display: flex;
  align-items: center;
  margin-right: 1rem;
}

.chat-toggle-btn {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 0.5rem;
  padding: 0.5rem;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
}

.chat-toggle-btn:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
  color: #475569;
}

.chat-toggle-btn:active {
  transform: scale(0.95);
}
</style>
