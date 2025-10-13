import { ref, computed } from 'vue'

// Global language state
const currentLanguage = ref('vi')

// Translation data
const translations = {
  vi: {
    // Navigation
    dashboard: 'Trang chủ',
    products: 'Sản phẩm',
    orders: 'Đơn hàng',
    customers: 'Khách hàng',
    vouchers: 'Voucher',
    settings: 'Cài đặt',
    pos: 'Bán hàng',
    
    // Dashboard
    totalProducts: 'Tổng sản phẩm',
    ordersToday: 'Đơn hàng hôm nay',
    revenueToday: 'Doanh thu hôm nay',
    newCustomers: 'Khách hàng mới',
    recentOrders: 'Đơn hàng gần đây',
    viewAll: 'Xem tất cả',
    orderId: 'Mã đơn',
    customer: 'Khách hàng',
    products: 'Sản phẩm',
    totalAmount: 'Tổng tiền',
    status: 'Trạng thái',
    actions: 'Thao tác',
    view: 'Xem',
    edit: 'Sửa',
    
    // Quick Actions
    quickActions: 'Thao tác nhanh',
    manageProducts: 'Quản lý sản phẩm',
    manageProductsDesc: 'Thêm, sửa, xóa sản phẩm',
    manageOrders: 'Quản lý hóa đơn',
    manageOrdersDesc: 'Xem và quản lý đơn hàng',
    posSystem: 'Bán hàng tại quầy',
    posSystemDesc: 'Hệ thống POS',
    onlineSales: 'Bán hàng online',
    onlineSalesDesc: 'Quản lý đơn hàng online',
    
    // Status
    pending: 'Chờ xử lý',
    processing: 'Đang xử lý',
    completed: 'Đã hoàn thành',
    cancelled: 'Đã hủy',
    
    // Common
    save: 'Lưu',
    cancel: 'Hủy',
    delete: 'Xóa',
    edit: 'Sửa',
    add: 'Thêm',
    search: 'Tìm kiếm',
    loading: 'Đang tải...',
    success: 'Thành công',
    error: 'Lỗi',
    warning: 'Cảnh báo',
    info: 'Thông tin'
  },
  en: {
    // Navigation
    dashboard: 'Dashboard',
    products: 'Products',
    orders: 'Orders',
    customers: 'Customers',
    vouchers: 'Vouchers',
    settings: 'Settings',
    pos: 'POS',
    
    // Dashboard
    totalProducts: 'Total Products',
    ordersToday: 'Orders Today',
    revenueToday: 'Revenue Today',
    newCustomers: 'New Customers',
    recentOrders: 'Recent Orders',
    viewAll: 'View All',
    orderId: 'Order ID',
    customer: 'Customer',
    products: 'Products',
    totalAmount: 'Total Amount',
    status: 'Status',
    actions: 'Actions',
    view: 'View',
    edit: 'Edit',
    
    // Quick Actions
    quickActions: 'Quick Actions',
    manageProducts: 'Manage Products',
    manageProductsDesc: 'Add, edit, delete products',
    manageOrders: 'Manage Orders',
    manageOrdersDesc: 'View and manage orders',
    posSystem: 'Point of Sale',
    posSystemDesc: 'POS System',
    onlineSales: 'Online Sales',
    onlineSalesDesc: 'Manage online orders',
    
    // Status
    pending: 'Pending',
    processing: 'Processing',
    completed: 'Completed',
    cancelled: 'Cancelled',
    
    // Common
    save: 'Save',
    cancel: 'Cancel',
    delete: 'Delete',
    edit: 'Edit',
    add: 'Add',
    search: 'Search',
    loading: 'Loading...',
    success: 'Success',
    error: 'Error',
    warning: 'Warning',
    info: 'Info'
  }
}

export function useTranslation() {
  // Translation function
  const t = (key) => {
    const keys = key.split('.')
    let value = translations[currentLanguage.value]
    for (const k of keys) {
      value = value?.[k]
    }
    return value || key
  }

  // Change language
  const setLanguage = (lang) => {
    currentLanguage.value = lang
    localStorage.setItem('app-language', lang)
    // Emit event to update all components
    window.dispatchEvent(new CustomEvent('language-changed', { detail: { language: lang } }))
  }

  // Get current language
  const getCurrentLanguage = computed(() => currentLanguage.value)

  // Initialize language from localStorage
  const initLanguage = () => {
    const savedLanguage = localStorage.getItem('app-language')
    if (savedLanguage && translations[savedLanguage]) {
      currentLanguage.value = savedLanguage
    }
  }

  return {
    t,
    setLanguage,
    getCurrentLanguage,
    initLanguage
  }
}



