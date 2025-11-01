<template>
  <div class="voucher-page">
    <!-- Phoenix-style Header -->
    <div class="page-header">
      <div class="header-container">
        <!-- Breadcrumb Navigation -->
        <div class="breadcrumb">
          <router-link to="/" class="breadcrumb-item">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
              <polyline points="9,22 9,12 15,12 15,22"></polyline>
            </svg>
            Trang chủ
          </router-link>
          <span class="breadcrumb-separator">/</span>
          <span class="breadcrumb-current">Ví Voucher Phoenix</span>
        </div>

        <!-- Header Content -->
        <div class="header-content">
          <div class="header-left">
            <h1 class="page-title">Ví Voucher Phoenix</h1>
            <p class="page-subtitle">Quản lý voucher và mã giảm giá Phoenix của bạn</p>
          </div>
          
          <div class="header-right">
            <div class="user-stats">
              <div class="stat-item">
                <div class="stat-value">{{ totalVouchers }}</div>
                <div class="stat-label">Voucher</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ availableVouchers }}</div>
                <div class="stat-label">Có thể dùng</div>
              </div>
              <div v-if="myVouchers.length > 0" class="stat-item limit-warning">
                <div class="stat-value">⚠️</div>
                <div class="stat-label">Đã có voucher</div>
              </div>
            </div>
            
            <div class="header-actions">
              <button @click="refreshVouchers" class="refresh-btn" :disabled="loading">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
                  <polyline points="23 4 23 10 17 10"></polyline>
                  <polyline points="1 20 1 14 7 14"></polyline>
                  <path d="m3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path>
                </svg>
                Làm mới
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Phoenix-style Search & Filter Section -->
    <div class="search-filter-section">
      <div class="search-container">
        <div class="search-box">
          <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <circle cx="11" cy="11" r="8"></circle>
            <path d="m21 21-4.35-4.35"></path>
          </svg>
          <input 
            type="text" 
            v-model="searchTerm" 
            placeholder="Tìm kiếm voucher Phoenix, mã giảm giá..."
            @input="handleSearch"
          />
          <button v-if="searchTerm" @click="clearSearch" class="clear-btn">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
      </div>
      
      <!-- Phoenix-style Filter Tabs -->
      <div class="filter-tabs">
        <button 
          v-for="tab in tabs" 
          :key="tab.key"
          @click="activeTab = tab.key"
          :class="['tab-btn', { active: activeTab === tab.key }]"
        >
          <span class="tab-icon">{{ tab.icon }}</span>
          <span class="tab-text">{{ tab.label }}</span>
          <span v-if="tab.count > 0" class="tab-count">{{ tab.count }}</span>
        </button>
      </div>

      <!-- Additional Filters -->
      <div class="additional-filters">
        <div class="filter-group">
          <label class="filter-label">Loại voucher:</label>
          <select v-model="selectedCategory" @change="filterByCategory" class="filter-select">
            <option value="">Tất cả</option>
            <option value="percent">Phần trăm</option>
            <option value="amount">Số tiền</option>
          </select>
        </div>
        
        <div class="filter-group">
          <label class="filter-label">Trạng thái:</label>
          <select v-model="selectedStatus" @change="filterByStatus" class="filter-select">
            <option value="">Tất cả</option>
            <option value="available">Có thể sử dụng</option>
            <option value="used">Đã sử dụng</option>
            <option value="expired">Hết hạn</option>
          </select>
        </div>
      </div>
    </div>


    <!-- Phoenix-style Voucher Grid -->
    <div class="voucher-grid">
      <div v-for="voucher in filteredVouchers" :key="voucher.id" 
           class="voucher-card" 
           :class="getVoucherCardClass(voucher)">
        
        <!-- Coupon-style Voucher Card -->
        <div class="coupon-card">
          <!-- Left Section (Orange Background) -->
          <div class="coupon-left">
            <!-- New Badge -->
            <div class="new-badge">Mới!</div>
            
            <!-- Limited Quantity Badge -->
            <div v-if="voucher.soLuongDung && voucher.soLuongDung > 0" class="limited-badge">
              Số lượng có hạn
            </div>
            
            <!-- Brand/Category Section -->
            <div class="brand-section">
              <div class="brand-icon">
                <div v-if="voucher.loaiPhieuGiamGia?.includes('Shop')" class="shop-icon">🛍️</div>
                <div v-else-if="voucher.loaiPhieuGiamGia?.includes('Vận chuyển')" class="shipping-icon">🚚</div>
                <div v-else-if="voucher.loaiPhieuGiamGia?.includes('Khỏe')" class="health-icon">💄</div>
                <div v-else class="default-icon">🔥</div>
              </div>
              <div class="brand-text">
                <div class="category-name">{{ getCategoryName(voucher) }}</div>
                <div class="brand-name">PHOENIX</div>
              </div>
            </div>
          </div>
          
          <!-- Perforated Line -->
          <div class="perforated-line">
            <div class="perforation"></div>
            <div class="perforation"></div>
            <div class="perforation"></div>
            <div class="perforation"></div>
            <div class="perforation"></div>
          </div>
          
          <!-- Right Section (White Background) -->
          <div class="coupon-right">
            <!-- Voucher Name and Type -->
            <div class="voucher-header">
              <div class="voucher-name">
                {{ voucher.tenPhieuGiamGia }}
              </div>
              <div class="voucher-type">
                {{ voucher.loaiPhieuGiamGia }}
              </div>
            </div>
            
            <!-- Discount Info -->
            <div class="discount-info">
              <div class="discount-main">
                <span class="discount-text">{{ voucher.discountText }}</span>
                <span class="discount-max" v-if="voucher.soTienGiamToiDa">
                  Giảm tối đa {{ formatPrice(voucher.soTienGiamToiDa) }}
                </span>
              </div>
              
              <!-- Minimum Order -->
              <div v-if="voucher.hoaDonToiThieu" class="min-order">
                Đơn Tối Thiểu {{ formatPrice(voucher.hoaDonToiThieu) }}
              </div>
              
              <!-- Validity Info -->
              <div class="validity-info">
                <svg class="clock-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                  <circle cx="12" cy="12" r="10"></circle>
                  <polyline points="12,6 12,12 16,14"></polyline>
                </svg>
                <span>Hiệu lực sau: {{ voucher.timeRemaining || '2 giờ' }} Điều Kiện</span>
              </div>
            </div>
            
            <!-- Category Tag -->
            <div class="category-tag">
              {{ getCategoryTag(voucher) }}
            </div>
            
            <!-- Progress Info for Limited Vouchers -->
            <div v-if="voucher.soLuongDung && voucher.soLuongDung > 0" class="progress-section">
              <div class="progress-info">
                <span class="progress-label">Lượt sử dụng còn lại</span>
                <span class="progress-count">{{ voucher.remainingUses || voucher.soLuongDung }}</span>
              </div>
            </div>
            
            <!-- Action Button -->
            <button 
              @click="handleVoucherAction(voucher)" 
              :disabled="!canInteractWithVoucher(voucher)"
              :class="['use-later-btn', { disabled: !canInteractWithVoucher(voucher) }]"
            >
              {{ getActionText(voucher) }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-if="filteredVouchers.length === 0 && !loading" class="empty-state">
      <div class="empty-icon">🎫</div>
      <h3 class="empty-title">Không có voucher nào</h3>
      <p class="empty-description">
        {{ searchTerm ? 'Không tìm thấy voucher phù hợp với từ khóa của bạn' : 'Hiện tại chưa có voucher khả dụng' }}
      </p>
      <button v-if="searchTerm" @click="clearSearch" class="clear-search-btn">
        Xóa bộ lọc tìm kiếm
      </button>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>Đang tải voucher...</p>
    </div>

    <!-- Toast Notifications -->
    <div v-if="toast.show" class="toast" :class="toast.type">
      <div class="toast-content">
        <div class="toast-icon">
          <svg v-if="toast.type === 'success'" viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
            <polyline points="22,4 12,14.01 9,11.01"></polyline>
          </svg>
          <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="15" y1="9" x2="9" y2="15"></line>
            <line x1="9" y1="9" x2="15" y2="15"></line>
          </svg>
        </div>
        <span>{{ toast.message }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// Reactive data
const vouchers = ref([])
const myVouchers = ref([])
const searchTerm = ref('')
const activeTab = ref('available')
const loading = ref(false)
const toast = ref({ show: false, message: '', type: 'success' })
const selectedCategory = ref('')
const selectedStatus = ref('')

// Tabs configuration (Phoenix style)
const tabs = ref([
  { key: 'all', label: 'Tất cả', icon: '🔥', count: 0 },
  { key: 'available', label: 'Có thể dùng', icon: '✅', count: 0 },
  { key: 'my-vouchers', label: 'Voucher Phoenix của tôi', icon: '💳', count: 0 },
  { key: 'expired', label: 'Hết hạn', icon: '⏰', count: 0 }
])

// Computed properties for stats
const totalVouchers = computed(() => {
  return [...vouchers.value, ...myVouchers.value].length
})

const availableVouchers = computed(() => {
  // Available = vouchers that user can claim (not already claimed)
  const availableIds = myVouchers.value.map(v => v.id)
  return vouchers.value.filter(v => !availableIds.includes(v.id) && !v.isExpired && !v.isUsed).length
})

// Computed properties
const filteredVouchers = computed(() => {
  let list = []
  
  switch (activeTab.value) {
    case 'all':
      // Combine vouchers and myVouchers, remove duplicates by id
      const allVouchers = [...vouchers.value, ...myVouchers.value]
      const uniqueVouchers = allVouchers.filter((voucher, index, self) => 
        index === self.findIndex(v => v.id === voucher.id)
      )
      
      // Mark claimed status for all vouchers
      list = uniqueVouchers.map(voucher => {
        const isClaimed = myVouchers.value.some(myV => myV.id === voucher.id)
        return {
          ...voucher,
          isClaimed: isClaimed,
          isAvailable: !voucher.isExpired && !voucher.isUsed
        }
      })
      break
    case 'available':
      // Show all available vouchers, mark claimed ones
      // First, remove duplicates from vouchers array
      const availableUniqueVouchers = vouchers.value.filter((voucher, index, self) => 
        index === self.findIndex(v => v.id === voucher.id)
      )
      
      list = availableUniqueVouchers.map(voucher => {
        const isClaimed = myVouchers.value.some(myV => myV.id === voucher.id)
        return {
          ...voucher,
          isClaimed: isClaimed,
          isAvailable: !voucher.isExpired && !voucher.isUsed
        }
      })
      break
    case 'my-vouchers':
      list = myVouchers.value
      break
    case 'expired':
      const expiredVouchers = [...vouchers.value, ...myVouchers.value].filter(v => v.isExpired)
      const uniqueExpired = expiredVouchers.filter((voucher, index, self) => 
        index === self.findIndex(v => v.id === voucher.id)
      )
      list = uniqueExpired
      break
  }
  
  // Apply search filter
  if (searchTerm.value) {
    list = list.filter(voucher => 
      voucher.tenPhieuGiamGia.toLowerCase().includes(searchTerm.value.toLowerCase()) ||
      voucher.maPhieuGiamGia.toLowerCase().includes(searchTerm.value.toLowerCase()) ||
      (voucher.shopName && voucher.shopName.toLowerCase().includes(searchTerm.value.toLowerCase()))
    )
  }
  
  // Apply category filter
  if (selectedCategory.value) {
    list = list.filter(voucher => {
      switch (selectedCategory.value) {
        case 'percent':
          return voucher.loaiPhieuGiamGia?.includes('Phần trăm')
        case 'amount':
          return voucher.loaiPhieuGiamGia?.includes('Số tiền')
        default:
          return true
      }
    })
  }
  
  // Apply status filter
  if (selectedStatus.value) {
    list = list.filter(voucher => {
      switch (selectedStatus.value) {
        case 'available':
          return !voucher.isExpired && !voucher.isUsed
        case 'used':
          return voucher.isUsed
        case 'expired':
          return voucher.isExpired
        default:
          return true
      }
    })
  }
  
  return list
})

// Methods
const loadVouchers = async () => {
  loading.value = true
  try {
    const [vouchersRes, myVouchersRes] = await Promise.all([
      fetch('/api/customer/vouchers/available?customerId=1'),
      fetch('/api/customer/vouchers/my-vouchers?customerId=1')
    ])
    
    if (vouchersRes.ok) {
      const data = await vouchersRes.json()
      vouchers.value = data
    } else {
      vouchers.value = []
    }
    
    if (myVouchersRes.ok) {
      const data = await myVouchersRes.json()
      myVouchers.value = data
    } else {
      myVouchers.value = []
    }
    
    updateTabCounts()
    
  } catch (error) {
    console.error('Error loading vouchers:', error)
    showToast('Có lỗi xảy ra khi tải voucher', 'error')
  } finally {
    loading.value = false
  }
}


const searchVouchers = async () => {
  if (!searchTerm.value.trim()) return
  
  loading.value = true
  try {
    const response = await fetch(`/api/customer/vouchers/search?keyword=${encodeURIComponent(searchTerm.value)}&customerId=1`)
    if (response.ok) {
      const results = await response.json()
      vouchers.value = results
    }
  } catch (error) {
    console.error('Error searching vouchers:', error)
  } finally {
    loading.value = false
  }
}

const claimVoucher = async (voucher) => {
  if (!voucher.isAvailable) return
  
  // Kiểm tra nếu đã có voucher rồi
  if (myVouchers.value.length > 0) {
    showToast('Mỗi tài khoản chỉ được nhận 1 voucher!', 'error')
    return
  }
  
  try {
    const response = await fetch('/api/customer/vouchers/claim', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: `customerId=1&voucherId=${voucher.id}`
    })
    
    if (response.ok) {
      showToast('Nhận voucher thành công!', 'success')
      await loadVouchers()
    } else {
      const errorData = await response.json()
      if (errorData.message && errorData.message.includes('đã có voucher')) {
        showToast('Mỗi tài khoản chỉ được nhận 1 voucher!', 'error')
      } else {
        showToast('Không thể nhận voucher này', 'error')
      }
    }
  } catch (error) {
    console.error('Error claiming voucher:', error)
    showToast('Có lỗi xảy ra', 'error')
  }
}

const useVoucher = (voucher) => {
  // Navigate to checkout with voucher
  router.push(`/checkout?voucher=${voucher.id}`)
}

const handleVoucherAction = (voucher) => {
  if (activeTab.value === 'my-vouchers') {
    useVoucher(voucher)
  } else {
    claimVoucher(voucher)
  }
}

const handleSearch = () => {
  if (searchTerm.value.trim()) {
    searchVouchers()
  } else {
    loadVouchers()
  }
}

const clearSearch = () => {
  searchTerm.value = ''
  loadVouchers()
}

const refreshVouchers = async () => {
  await loadVouchers()
  showToast('Đã làm mới danh sách voucher', 'success')
}

const copyVoucherCode = async (voucher) => {
  try {
    const code = voucher.maPhieuGiamGia || 'PHOENIX2024'
    await navigator.clipboard.writeText(code)
    showToast(`Đã copy mã voucher Phoenix: ${code}`, 'success')
  } catch (error) {
    console.error('Error copying voucher code:', error)
    showToast('Không thể copy mã voucher Phoenix', 'error')
  }
}


const updateTabCounts = () => {
  // All tab: total unique vouchers
  const allVouchers = [...vouchers.value, ...myVouchers.value]
  const uniqueAll = allVouchers.filter((voucher, index, self) => 
    index === self.findIndex(v => v.id === voucher.id)
  )
  tabs.value[0].count = uniqueAll.length
  
  // Available tab: vouchers user can claim (not already claimed)
  const availableIds = myVouchers.value.map(v => v.id)
  const availableCount = vouchers.value.filter(v => !availableIds.includes(v.id) && !v.isExpired && !v.isUsed).length
  tabs.value[1].count = availableCount
  
  // My vouchers tab: vouchers user has claimed
  tabs.value[2].count = myVouchers.value.length
  
  // Expired tab: expired vouchers
  const expiredVouchers = [...vouchers.value, ...myVouchers.value].filter(v => v.isExpired)
  const uniqueExpired = expiredVouchers.filter((voucher, index, self) => 
    index === self.findIndex(v => v.id === voucher.id)
  )
  tabs.value[3].count = uniqueExpired.length
}

// Helper methods
const getVoucherCardClass = (voucher) => {
  const classes = []
  if (voucher.isExpired) classes.push('expired')
  if (voucher.riengTu) classes.push('private')
  if (voucher.isClaimed) classes.push('claimed')
  return classes
}


const getActionText = (voucher) => {
  if (activeTab.value === 'my-vouchers') {
    if (voucher.isExpired) return 'Hết hạn'
    if (voucher.isUsed) return 'Đã sử dụng'
    return 'Sử dụng ngay'
  } else {
    if (voucher.isExpired) return 'Hết hạn'
    if (voucher.isClaimed) return 'Đã nhận'
    return 'Nhận ngay'
  }
}

const canInteractWithVoucher = (voucher) => {
  if (activeTab.value === 'my-vouchers') {
    return !voucher.isExpired && !voucher.isUsed
  } else {
    // Chỉ có thể nhận nếu chưa claim và còn available
    return !voucher.isClaimed && voucher.isAvailable
  }
}


const getCategoryName = (voucher) => {
  if (voucher.loaiPhieuGiamGia?.includes('Shop')) return 'SHOP THỊNH HÀNH'
  if (voucher.loaiPhieuGiamGia?.includes('Vận chuyển')) return 'VẬN CHUYỂN'
  if (voucher.loaiPhieuGiamGia?.includes('Khỏe')) return 'KHỎE & ĐẸP'
  return 'PHOENIX'
}

const getCategoryTag = (voucher) => {
  if (voucher.loaiPhieuGiamGia?.includes('Shop')) return 'Shop Thịnh Hành'
  if (voucher.loaiPhieuGiamGia?.includes('Vận chuyển')) return 'Vận Chuyển'
  if (voucher.loaiPhieuGiamGia?.includes('Khỏe')) return 'Khỏe & Đẹp'
  return 'Phoenix'
}

const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

const showToast = (message, type = 'success') => {
  toast.value = { show: true, message, type }
  setTimeout(() => {
    toast.value.show = false
  }, 3000)
}

// Watch for tab changes
watch(activeTab, () => {
  // Update counts when switching tabs
  updateTabCounts()
})

// Lifecycle
onMounted(() => {
  loadVouchers()
})
</script>

<style scoped>
/* Phoenix-inspired Voucher Page Styles */
.voucher-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 0;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* Phoenix-style Header */
.page-header {
  background: linear-gradient(135deg, #ff6b35 0%, #f7931e 50%, #ff8c00 100%);
  padding: 0;
  color: white;
  position: relative;
  overflow: hidden;
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 1rem 1.5rem;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
  font-size: 0.9rem;
}

.breadcrumb-item {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  transition: color 0.3s ease;
}

.breadcrumb-item:hover {
  color: white;
}

.breadcrumb-item svg {
  width: 16px;
  height: 16px;
}

.breadcrumb-separator {
  color: rgba(255, 255, 255, 0.6);
}

.breadcrumb-current {
  color: white;
  font-weight: 600;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 1;
}

.header-left {
  flex: 1;
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  margin: 0 0 0.25rem 0;
  color: white;
}

.page-subtitle {
  font-size: 0.9rem;
  opacity: 0.9;
  margin: 0;
  font-weight: 400;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.user-stats {
  display: flex;
  gap: 1rem;
}

.stat-item {
  text-align: center;
  background: rgba(255, 255, 255, 0.15);
  padding: 0.75rem 1rem;
  border-radius: 8px;
  backdrop-filter: blur(10px);
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: white;
  margin: 0;
}

.stat-label {
  font-size: 0.8rem;
  opacity: 0.9;
  margin: 0;
}

.limit-warning {
  background: rgba(255, 193, 7, 0.2) !important;
  border: 1px solid rgba(255, 193, 7, 0.3);
}

.limit-warning .stat-value {
  color: #f59e0b;
}

.limit-warning .stat-label {
  color: #f59e0b;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 0.75rem;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: rgba(255, 255, 255, 0.15);
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.refresh-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-1px);
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.refresh-btn svg {
  width: 16px;
  height: 16px;
}


/* Phoenix-style Search & Filter Section */
.search-filter-section {
  background: white;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.additional-filters {
  display: flex;
  gap: 1.5rem;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #f0f0f0;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.filter-label {
  font-size: 0.9rem;
  font-weight: 600;
  color: #333;
}

.filter-select {
  padding: 0.5rem 0.75rem;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  background: white;
  font-size: 0.9rem;
  color: #333;
  cursor: pointer;
  transition: border-color 0.3s ease;
}

.filter-select:focus {
  outline: none;
  border-color: #ff6b35;
  box-shadow: 0 0 0 2px rgba(255, 107, 53, 0.1);
}

.search-container {
  max-width: 1200px;
  margin: 0 auto 1.5rem auto;
}

.search-box {
  position: relative;
  max-width: 500px;
  margin: 0 auto;
}

.search-box input {
  width: 100%;
  padding: 1rem 3rem 1rem 3rem;
  border: 2px solid #e1e5e9;
  border-radius: 25px;
  font-size: 1rem;
  background: #f8f9fa;
  transition: all 0.3s ease;
  outline: none;
}

.search-box input:focus {
  border-color: #ff6b35;
  background: white;
  box-shadow: 0 0 0 3px rgba(255, 107, 53, 0.1);
}

.search-icon {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  width: 20px;
  height: 20px;
  color: #6c757d;
}

.clear-btn {
  position: absolute;
  right: 1rem;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #6c757d;
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.clear-btn:hover {
  background: #e9ecef;
  color: #495057;
}

/* Filter Tabs */
.filter-tabs {
  display: flex;
  gap: 0.5rem;
  max-width: 1200px;
  margin: 0 auto;
  overflow-x: auto;
  padding-bottom: 0.5rem;
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border: 2px solid #e1e5e9;
  border-radius: 25px;
  background: white;
  color: #6c757d;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
  position: relative;
}

.tab-btn:hover {
  border-color: #ff6b35;
  color: #ff6b35;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.2);
}

.tab-btn.active {
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  border-color: #ff6b35;
  color: white;
  box-shadow: 0 4px 15px rgba(255, 107, 53, 0.3);
}

.tab-icon {
  font-size: 1.2rem;
}

.tab-count {
  background: rgba(255, 255, 255, 0.2);
  padding: 0.25rem 0.5rem;
  border-radius: 10px;
  font-size: 0.8rem;
  font-weight: 700;
}


/* Phoenix-style Voucher Grid */
.voucher-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 1rem;
  padding: 1.5rem;
  max-width: 1200px;
  margin: 0 auto;
}

.voucher-card {
  background: transparent;
  border-radius: 0;
  padding: 0;
  box-shadow: none;
  transition: all 0.3s ease;
  position: relative;
  overflow: visible;
  border: none;
}

/* Coupon-style Card */
.coupon-card {
  display: flex;
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: relative;
  min-height: 120px;
}

.coupon-left {
  background: linear-gradient(135deg, #ff6b35 0%, #f7931e 100%);
  width: 40%;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  position: relative;
  color: white;
}

.coupon-right {
  background: white;
  width: 60%;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  position: relative;
}

/* New Badge */
.new-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: #dc2626;
  color: white;
  padding: 0.25rem 0.5rem;
  font-size: 0.7rem;
  font-weight: 700;
  border-radius: 4px;
  transform: rotate(15deg);
  z-index: 2;
}

/* Limited Badge */
.limited-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  background: #fbbf24;
  color: #92400e;
  padding: 0.25rem 0.5rem;
  font-size: 0.7rem;
  font-weight: 600;
  border-radius: 4px;
  z-index: 2;
}

/* Brand Section */
.brand-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 0.5rem;
}

.brand-icon {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.brand-text {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
}

.category-name {
  font-size: 0.8rem;
  font-weight: 700;
  color: #1e40af;
  text-align: center;
  line-height: 1.1;
}

.brand-name {
  font-size: 1rem;
  font-weight: 800;
  color: white;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

/* Perforated Line */
.perforated-line {
  position: absolute;
  left: 40%;
  top: 0;
  bottom: 0;
  width: 2px;
  display: flex;
  flex-direction: column;
  justify-content: space-around;
  z-index: 1;
}

.perforation {
  width: 8px;
  height: 8px;
  background: white;
  border-radius: 50%;
  margin: 0.5rem 0;
  box-shadow: 0 0 0 2px #ff6b35;
}

/* Right Section Styles */
.voucher-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.voucher-name {
  font-size: 1.1rem;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.3;
  flex: 1;
}

.voucher-type {
  font-size: 0.8rem;
  color: #6b7280;
  background: #f3f4f6;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-weight: 600;
  white-space: nowrap;
  margin-left: 0.5rem;
}

.discount-info {
  margin-bottom: 1rem;
}

.discount-main {
  margin-bottom: 0.5rem;
}

.discount-text {
  font-size: 1.2rem;
  font-weight: 700;
  color: #1f2937;
  display: block;
  margin-bottom: 0.25rem;
}

.discount-max {
  font-size: 0.9rem;
  color: #6b7280;
  font-weight: 500;
}

.min-order {
  font-size: 0.9rem;
  color: #374151;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.validity-info {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.8rem;
  color: #6b7280;
}

.clock-icon {
  width: 14px;
  height: 14px;
}

.category-tag {
  background: #f3f4f6;
  border: 1px solid #d1d5db;
  color: #374151;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 600;
  display: inline-block;
  margin-bottom: 1rem;
}

.use-later-btn {
  background: white;
  border: 1px solid #ff6b35;
  color: #ff6b35;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  font-weight: 600;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
  align-self: flex-end;
  margin-top: auto;
}

.use-later-btn:hover:not(:disabled) {
  background: #ff6b35;
  color: white;
}

.use-later-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Disabled state for "Đã có voucher" */
.use-later-btn.disabled {
  background: #f3f4f6;
  border-color: #d1d5db;
  color: #6b7280;
  cursor: not-allowed;
}

.use-later-btn.disabled:hover {
  background: #f3f4f6;
  color: #6b7280;
}

.coupon-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.voucher-card.expired {
  opacity: 0.6;
  background: #f8f9fa;
}

.voucher-card.private {
  border-left: 3px solid #ff6b35;
}

.voucher-card.claimed {
  border-left: 3px solid #00a650;
}


/* Progress Section */
.progress-section {
  margin: 0.75rem 0;
  padding: 0.5rem 0;
  border-top: 1px solid #e1e5e9;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
  font-size: 0.85rem;
  color: #6c757d;
}

.progress-label {
  font-weight: 600;
}

.progress-count {
  font-weight: 700;
  color: #ff6b6b;
}


/* Empty State */
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  max-width: 500px;
  margin: 0 auto;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.empty-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 0.5rem 0;
}

.empty-description {
  color: #6c757d;
  margin: 0 0 2rem 0;
  line-height: 1.5;
}

.clear-search-btn {
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 25px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.clear-search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(255, 107, 53, 0.3);
}

/* Loading State */
.loading-state {
  text-align: center;
  padding: 4rem 2rem;
  color: #6c757d;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e1e5e9;
  border-top: 4px solid #ff6b35;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem auto;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Toast Notifications */
.toast {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 1000;
  max-width: 400px;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

.toast-content {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 1.5rem;
  border-radius: 15px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  font-weight: 600;
}

.toast.success .toast-content {
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.toast.error .toast-content {
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.toast-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

/* Responsive Design */
@media (max-width: 768px) {
  .header-container {
    padding: 1rem;
  }
  
  .page-title {
    font-size: 1.5rem;
  }
  
  .header-content {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }
  
  .header-right {
    flex-direction: column;
    gap: 1rem;
  }
  
  .user-stats {
    justify-content: center;
  }
  
  .header-actions {
    justify-content: center;
  }
  
  .search-filter-section {
    padding: 1rem;
  }
  
  .additional-filters {
    flex-direction: column;
    gap: 1rem;
  }
  
  .filter-tabs {
    gap: 0.25rem;
  }
  
  .tab-btn {
    padding: 0.5rem 1rem;
    font-size: 0.9rem;
  }
  
  .voucher-grid {
    grid-template-columns: 1fr;
    padding: 1rem;
  }
  
  .coupon-card {
    flex-direction: column;
    min-height: auto;
  }
  
  .coupon-left {
    width: 100%;
    padding: 1rem;
  }
  
  .coupon-right {
    width: 100%;
    padding: 1rem;
  }
  
  .perforated-line {
    left: 0;
    top: auto;
    bottom: 0;
    width: 100%;
    height: 2px;
    flex-direction: row;
    justify-content: space-around;
  }
  
  .perforation {
    width: 8px;
    height: 8px;
    margin: 0 0.5rem;
  }
  
  
  .toast {
    right: 10px;
    left: 10px;
    max-width: none;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 1.5rem;
  }
  
  .page-subtitle {
    font-size: 1rem;
  }
  
  .voucher-card {
    padding: 1rem;
  }
}
</style>
