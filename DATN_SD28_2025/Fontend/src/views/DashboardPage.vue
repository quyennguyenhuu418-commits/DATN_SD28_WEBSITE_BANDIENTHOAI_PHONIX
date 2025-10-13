<template>
  <div class="dashboard">
    <!-- POS Header -->
    <PosHeader />

    <!-- Main Content -->
    <main class="dashboard-main">

      <!-- Stats Cards -->
      <section class="stats-section">
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon">
              <font-awesome-icon icon="box" class="fa-2x" />
            </div>
            <div class="stat-content">
              <h3>{{ stats.totalProducts }}</h3>
              <p>{{ t('totalProducts') }}</p>
              <span class="stat-change" :class="stats.productsChangePercent >= 0 ? 'positive' : 'negative'">
                {{ stats.productsChangePercent >= 0 ? '+' : '' }}{{ stats.productsChangePercent.toFixed(0) }}%
              </span>
            </div>
          </div>

          <div class="stat-card">
            <div class="stat-icon">
              <font-awesome-icon icon="shopping-cart" class="fa-2x" />
            </div>
            <div class="stat-content">
              <h3>{{ stats.totalOrders }}</h3>
              <p>{{ t('ordersToday') }}</p>
              <span class="stat-change" :class="stats.ordersChangePercent >= 0 ? 'positive' : 'negative'">
                {{ stats.ordersChangePercent >= 0 ? '+' : '' }}{{ stats.ordersChangePercent.toFixed(0) }}%
              </span>
            </div>
          </div>

          <div class="stat-card">
            <div class="stat-icon">
              <font-awesome-icon icon="dollar-sign" class="fa-2x" />
            </div>
            <div class="stat-content">
              <h3>{{ formatNumberVietnamese(stats.revenue) }} ₫</h3>
              <p>{{ t('revenueToday') }}</p>
              <span class="stat-change" :class="stats.revenueChangePercent >= 0 ? 'positive' : 'negative'">
                {{ stats.revenueChangePercent >= 0 ? '+' : '' }}{{ stats.revenueChangePercent.toFixed(0) }}%
              </span>
            </div>
          </div>

          <div class="stat-card">
            <div class="stat-icon">
              <font-awesome-icon icon="users" class="fa-2x" />
            </div>
            <div class="stat-content">
              <h3>{{ stats.totalCustomers }}</h3>
              <p>{{ t('newCustomers') }}</p>
              <span class="stat-change" :class="stats.customersChangePercent >= 0 ? 'positive' : 'negative'">
                {{ stats.customersChangePercent >= 0 ? '+' : '' }}{{ stats.customersChangePercent.toFixed(0) }}%
              </span>
            </div>
          </div>
        </div>
      </section>

      <!-- Quick Actions -->
      <section class="quick-actions">
        <h2>{{ t('quickActions') }}</h2>
        <div class="actions-grid">
          <router-link to="/san-pham" class="action-card">
            <div class="action-icon">
              <font-awesome-icon icon="box" class="fa-2x" />
            </div>
            <h3>{{ t('manageProducts') }}</h3>
            <p>{{ t('manageProductsDesc') }}</p>
          </router-link>

          <router-link to="/hoa-don" class="action-card">
            <div class="action-icon">
              <font-awesome-icon icon="file-invoice" class="fa-2x" />
            </div>
            <h3>{{ t('manageOrders') }}</h3>
            <p>{{ t('manageOrdersDesc') }}</p>
          </router-link>

          <router-link to="/pos" class="action-card">
            <div class="action-icon">
              <font-awesome-icon icon="cash-register" class="fa-2x" />
            </div>
            <h3>{{ t('posSystem') }}</h3>
            <p>{{ t('posSystemDesc') }}</p>
          </router-link>

          <router-link to="/online" class="action-card">
            <div class="action-icon">
              <font-awesome-icon icon="globe" class="fa-2x" />
            </div>
            <h3>{{ t('onlineSales') }}</h3>
            <p>{{ t('onlineSalesDesc') }}</p>
          </router-link>
        </div>
      </section>

      <!-- Recent Orders -->
      <section class="recent-orders">
        <div class="section-header">
          <h2>{{ t('recentOrders') }}</h2>
          <div style="display: flex; gap: 10px; align-items: center;">
            <button @click="loadRecentOrders" style="padding: 5px 10px; background: #3b82f6; color: white; border: none; border-radius: 5px; cursor: pointer;">
              Reload
            </button>
            <router-link to="/hoa-don" class="view-all"> {{ t('viewAll') }} </router-link>
          </div>
        </div>
        <div class="orders-table">
          <div class="table-wrapper">
            <table class="modern-table">
              <thead>
                <tr>
                  <th class="table-header">
                    <div class="header-content">
                      <font-awesome-icon icon="clipboard-list" class="header-icon" />
                      <span>{{ t('orderId') }}</span>
                    </div>
                  </th>
                  <th class="table-header">
                    <div class="header-content">
                      <font-awesome-icon icon="user" class="header-icon" />
                      <span>{{ t('customer') }}</span>
                    </div>
                  </th>
                  <th class="table-header">
                    <div class="header-content">
                      <font-awesome-icon icon="box" class="header-icon" />
                      <span>{{ t('products') }}</span>
                    </div>
                  </th>
                  <th class="table-header">
                    <div class="header-content">
                      <font-awesome-icon icon="dollar-sign" class="header-icon" />
                      <span>{{ t('totalAmount') }}</span>
                    </div>
                  </th>
                  <th class="table-header">
                    <div class="header-content">
                      <font-awesome-icon icon="chart-line" class="header-icon" />
                      <span>{{ t('status') }}</span>
                    </div>
                  </th>
                  <th class="table-header">
                    <div class="header-content">
                      <font-awesome-icon icon="cog" class="header-icon" />
                      <span>{{ t('actions') }}</span>
                    </div>
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="order in recentOrders" :key="order.id" class="table-row">
                  <td class="table-cell">
                    <div class="cell-content">
                      <span class="order-code">{{ order.maHoaDon }}</span>
                    </div>
                  </td>
                  <td class="table-cell">
                    <div class="cell-content customer-info">
                      <div class="customer-avatar">
                        {{ (order.tenKhachHang || 'N/A').charAt(0).toUpperCase() }}
                      </div>
                      <div class="customer-details">
                        <span class="customer-name">{{ order.tenKhachHang || 'Khách lẻ' }}</span>
                        <span class="customer-id">#{{ order.soDienThoai || 'N/A' }}</span>
                      </div>
                    </div>
                  </td>
                  <td class="table-cell">
                    <div class="cell-content product-info">
                      <span class="product-count">{{ order.soLuongSanPham || 0 }}</span>
                      <span class="product-label">sản phẩm</span>
                    </div>
                  </td>
                  <td class="table-cell">
                    <div class="cell-content">
                      <span class="amount">{{ formatNumberVietnamese(order.tongTien) }} ₫</span>
                    </div>
                  </td>
                  <td class="table-cell">
                    <div class="cell-content">
                      <span :class="getStatusClass(order.trangThai)" class="status-badge">
                        <span class="status-dot"></span>
                        {{ order.trangThaiText || getStatusText(order.trangThai) }}
                      </span>
                    </div>
                  </td>
                  <td class="table-cell">
                    <div class="cell-content">
                      <div class="action-buttons">
                        <button class="btn-view" :title="t('view')">
                          <font-awesome-icon icon="eye" class="btn-icon" />
                          <span>{{ t('view') }}</span>
                        </button>
                        <button class="btn-edit" :title="t('edit')">
                          <font-awesome-icon icon="edit" class="btn-icon" />
                        </button>
                      </div>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          
          <!-- Debug Info -->
          <div v-if="recentOrders.length === 0" class="empty-state">
            <div class="empty-icon">📋</div>
            <h3>Chưa có đơn hàng nào</h3>
            <p>Các đơn hàng gần đây sẽ hiển thị tại đây</p>
            <div style="margin-top: 20px; padding: 10px; background: #f0f0f0; border-radius: 5px;">
              <p><strong>Debug Info:</strong></p>
              <p>recentOrders.length: {{ recentOrders.length }}</p>
              <p>recentOrders: {{ JSON.stringify(recentOrders) }}</p>
            </div>
          </div>
        </div>
      </section>

      <!-- Product Categories -->
      <section class="product-categories">
        <div class="section-header">
          <h2>Quản lý danh mục</h2>
          <router-link to="/san-pham" class="view-all"> Quản lý sản phẩm </router-link>
        </div>
        <div class="categories-grid">
          <router-link to="/hang" class="category-card">
            <div class="category-icon">
              <i class="icon-brand" />
            </div>
            <h3>Hãng sản xuất</h3>
            <p>{{ stats.totalBrands }} hãng</p>
          </router-link>

          <router-link to="/chip" class="category-card">
            <div class="category-icon">
              <i class="icon-cpu" />
            </div>
            <h3>Chip xử lý</h3>
            <p>{{ stats.totalChips }} loại</p>
          </router-link>

          <router-link to="/ram" class="category-card">
            <div class="category-icon">
              <i class="icon-memory" />
            </div>
            <h3>RAM</h3>
            <p>{{ stats.totalRams }} loại</p>
          </router-link>

          <router-link to="/rom" class="category-card">
            <div class="category-icon">
              <i class="icon-storage" />
            </div>
            <h3>ROM</h3>
            <p>{{ stats.totalRoms }} loại</p>
          </router-link>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '@/services/api'
import { useTranslation } from '@/composables/useTranslation.js'
import PosHeader from '@/components/PosHeader.vue'

const { t, initLanguage } = useTranslation()

// Header data

const stats = ref({
  totalProducts: 0,
  totalOrders: 0,
  revenue: 0,
  totalCustomers: 0,
  ordersChangePercent: 0,
  revenueChangePercent: 0,
  customersChangePercent: 0,
  productsChangePercent: 0,
})

const recentOrders = ref([])


onMounted(async () => {
  initLanguage()
  await loadStats()
  await loadRecentOrders()
  
  // Test Vietnamese number formatting
  console.log('Testing Vietnamese number formatting:')
  console.log('666666666 ->', formatNumberVietnamese(666666666))
  console.log('15000000 ->', formatNumberVietnamese(15000000))
  console.log('500000 ->', formatNumberVietnamese(500000))
  
  // Debug: Force some data for testing
  console.log('Recent orders after load:', recentOrders.value)
  if (recentOrders.value.length === 0) {
    console.log('No orders loaded, adding test data...')
    recentOrders.value = [
      {
        id: 1,
        maHoaDon: 'HD001',
        tenKhachHang: 'Nguyễn Văn A',
        soDienThoai: '0123456789',
        soLuongSanPham: 2,
        tongTien: 15000000,
        trangThai: 3,
        trangThaiText: 'Đã hoàn thành',
        ngayTao: new Date().toISOString()
      }
    ]
  }
})

async function loadStats() {
  try {
    // Load dashboard statistics from API
    const response = await api.get('/api/thong-ke/dashboard')
    const data = response.data
    
    stats.value.totalProducts = data.totalProducts || 0
    stats.value.totalOrders = data.ordersToday || 0
    stats.value.revenue = data.revenueToday || 0
    stats.value.totalCustomers = data.newCustomersToday || 0
    
    // Store percentage changes for display
    stats.value.ordersChangePercent = data.ordersChangePercent || 0
    stats.value.revenueChangePercent = data.revenueChangePercent || 0
    stats.value.customersChangePercent = data.customersChangePercent || 0
    stats.value.productsChangePercent = data.productsChangePercent || 0
  } catch (error) {
    console.error('Error loading stats:', error)
    // Fallback to default values
    stats.value = {
      totalProducts: 0,
      totalOrders: 0,
      revenue: 0,
      totalCustomers: 0,
      ordersChangePercent: 0,
      revenueChangePercent: 0,
      customersChangePercent: 0,
      productsChangePercent: 0
    }
  }
}

async function loadRecentOrders() {
  try {
    console.log('Loading recent orders...')
    const response = await api.get('/api/thong-ke/don-hang-gan-day?page=0&size=5')
    console.log('Recent orders response:', response.data)
    console.log('Response status:', response.status)
    
    // Backend returns { orders: [...], totalElements: ..., totalPages: ..., currentPage: ..., size: ... }
    if (response.data && response.data.orders) {
      recentOrders.value = response.data.orders
      console.log('Recent orders loaded:', recentOrders.value.length, 'orders')
      console.log('Orders data:', recentOrders.value)
    } else {
      recentOrders.value = []
      console.log('No orders found in response')
      console.log('Response structure:', Object.keys(response.data || {}))
    }
  } catch (error) {
    console.error('Error loading recent orders:', error)
    console.error('Error details:', error.response?.data)
    
    // Temporary mock data for testing display
    recentOrders.value = [
      {
        id: 1,
        maHoaDon: 'HD001',
        tenKhachHang: 'Nguyễn Văn A',
        soDienThoai: '0123456789',
        soLuongSanPham: 2,
        tongTien: 15000000,
        trangThai: 3,
        trangThaiText: 'Đã hoàn thành',
        ngayTao: new Date().toISOString()
      },
      {
        id: 2,
        maHoaDon: 'HD002',
        tenKhachHang: 'Trần Thị B',
        soDienThoai: '0987654321',
        soLuongSanPham: 1,
        tongTien: 8500000,
        trangThai: 2,
        trangThaiText: 'Đang xử lý',
        ngayTao: new Date().toISOString()
      },
      {
        id: 3,
        maHoaDon: 'HD003',
        tenKhachHang: 'Lê Văn C',
        soDienThoai: '0369258147',
        soLuongSanPham: 3,
        tongTien: 22000000,
        trangThai: 1,
        trangThaiText: 'Chờ xử lý',
        ngayTao: new Date().toISOString()
      }
    ]
    console.log('Using mock data for testing:', recentOrders.value)
    toastRef.value?.warning('Cảnh báo', 'Đang sử dụng dữ liệu mẫu - Backend chưa sẵn sàng')
  }
}

function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}

function formatCurrencyVietnamese(amount: number): string {
  if (amount >= 1000000) {
    const millions = amount / 1000000
    if (millions === Math.floor(millions)) {
      return `${millions} triệu`
    } else {
      return `${millions.toFixed(1)} triệu`
    }
  } else if (amount >= 1000) {
    const thousands = amount / 1000
    if (thousands === Math.floor(thousands)) {
      return `${thousands} nghìn`
    } else {
      return `${thousands.toFixed(1)} nghìn`
    }
  } else {
    return formatCurrency(amount)
  }
}

// Format number with Vietnamese thousands separator (dots)
function formatNumberVietnamese(number: number | string): string {
  const num = typeof number === 'string' ? parseFloat(number) : number
  if (isNaN(num)) return '0'
  
  // Use Vietnamese locale which uses dots as thousands separators
  return num.toLocaleString('vi-VN', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
    useGrouping: true
  })
}

// Parse Vietnamese formatted number back to number
function parseVietnameseNumber(formattedNumber: string): number {
  // Remove all dots and spaces, then parse
  const cleanNumber = formattedNumber.replace(/\./g, '').replace(/\s/g, '')
  return parseFloat(cleanNumber) || 0
}


function getStatusClass(status: number): string {
  switch (status) {
    case 0:
      return 'status-cancelled'
    case 1:
      return 'status-pending'
    case 2:
      return 'status-processing'
    case 3:
      return 'status-completed'
    case 4:
      return 'status-completed'
    default:
      return 'status-pending'
  }
}

function getStatusText(status: number): string {
  switch (status) {
    case 0:
      return 'Đã hủy'
    case 1:
      return 'Chờ xử lý'
    case 2:
      return 'Đang xử lý'
    case 3:
      return 'Đã hoàn thành'
    case 4:
      return 'Đã giao hàng'
    default:
      return 'Chờ xử lý'
  }
}
</script>

<style scoped>
/* Prevent horizontal scrolling globally */
* {
  box-sizing: border-box;
}

/* Reset margins and padding for dashboard */
.dashboard,
.dashboard * {
  margin: 0;
  padding: 0;
}

.dashboard {
  min-height: 100vh;
  background: #f8fafc;
  overflow-x: hidden;
  overflow-y: auto;
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
  scroll-behavior: smooth;
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}



.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 100%;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

.logo h1 {
  margin: 0;
  font-size: 1.8rem;
  font-weight: 700;
}

.logo p {
  margin: 0;
  opacity: 0.9;
  font-size: 0.9rem;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.btn-notification {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  padding: 0.5rem;
  border-radius: 50%;
  cursor: pointer;
  position: relative;
}

.badge {
  position: absolute;
  top: -5px;
  right: -5px;
  background: #ff4757;
  color: white;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.7rem;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.dashboard-main {
  max-width: 100%;
  margin: 0 auto;
  padding: 1.5rem;
  width: 100%;
  box-sizing: border-box;
  overflow-x: hidden;
  flex: 1;
  overflow-y: auto;
  min-height: 0;
  position: static;
  z-index: 1;
  background: #f8fafc;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}


.stats-section {
  margin-bottom: 0;
  position: static;
  z-index: 1;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
  width: 100%;
  box-sizing: border-box;
  padding: 0 1rem;
}

.stat-card {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 1rem;
  transition: all 0.3s ease;
  border: 1px solid #e2e8f0;
  width: 100%;
  box-sizing: border-box;
  position: relative;
  overflow: hidden;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  border-color: #3b82f6;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: #3b82f6;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.25rem;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
  margin-bottom: 0.5rem;
}

.stat-icon:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
}

.stat-icon .fa-2x {
  font-size: 1.5em;
}

.stat-content h3 {
  margin: 0;
  font-size: 2rem;
  font-weight: 700;
  color: #1e293b;
  line-height: 1.2;
}

.stat-content p {
  margin: 0.5rem 0;
  color: #64748b;
  font-size: 0.875rem;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-change {
  font-size: 0.75rem;
  font-weight: 600;
  padding: 0.25rem 0.5rem;
  border-radius: 6px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-top: 0.5rem;
}

.stat-change.positive {
  color: #059669;
  background: #d1fae5;
}

.stat-change.negative {
  color: #dc2626;
  background: #fee2e2;
}

.quick-actions {
  margin-bottom: 2rem;
}

.quick-actions h2 {
  margin-bottom: 1rem;
  color: var(--text-primary);
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
  width: 100%;
  box-sizing: border-box;
}

.action-card {
  background: var(--card-bg);
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 2px 10px var(--shadow-light);
  text-decoration: none;
  color: inherit;
  transition: all 0.3s ease;
  text-align: center;
  border: 1px solid var(--border-color);
  width: 100%;
  box-sizing: border-box;
}

.action-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px var(--shadow-medium);
}

.action-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.5rem;
  margin: 0 auto 1rem;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.action-icon:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.action-icon .fa-2x {
  font-size: 1.5em;
}

.action-card h3 {
  margin: 0 0 0.5rem;
  color: var(--text-primary);
}

.action-card p {
  margin: 0;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.recent-orders,
.product-categories {
  background: var(--card-bg);
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 10px var(--shadow-light);
  margin-bottom: 2rem;
  position: static;
  z-index: 1;
  border: 1px solid var(--border-color);
  width: 100%;
  box-sizing: border-box;
  overflow-x: hidden;
  max-width: 100%;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.section-header h2 {
  margin: 0;
  color: var(--text-primary);
}

.view-all {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
}

.view-all:hover {
  text-decoration: underline;
}

/* Modern Table Styles */
.orders-table {
  width: 100%;
  box-sizing: border-box;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 6px -1px var(--shadow-light);
}

.table-wrapper {
  overflow-x: auto;
  border-radius: 12px;
}

.modern-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  min-width: 800px;
  background: var(--card-bg);
}

/* Table Headers */
.table-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 0;
  border: none;
  position: relative;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 1rem 1.5rem;
  font-weight: 600;
  font-size: 0.875rem;
}

.header-icon {
  font-size: 1rem;
  opacity: 0.9;
  margin-right: 8px;
  color: white;
}

/* Table Rows */
.table-row {
  transition: all 0.3s ease;
  border-bottom: 1px solid var(--border-light);
}

.table-row:hover {
  background: var(--bg-tertiary);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px var(--shadow-light);
}

.table-row:last-child {
  border-bottom: none;
}

/* Table Cells */
.table-cell {
  padding: 0;
  border: none;
  vertical-align: middle;
}

.cell-content {
  padding: 1.25rem 1.5rem;
  display: flex;
  align-items: center;
}

/* Order Code */
.order-code {
  font-family: 'Monaco', 'Menlo', monospace;
  font-weight: 600;
  color: var(--text-primary);
  background: var(--bg-tertiary);
  padding: 0.25rem 0.5rem;
  border-radius: 6px;
  font-size: 0.875rem;
}

/* Customer Info */
.customer-info {
  gap: 0.75rem;
}

.customer-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.875rem;
}

.customer-details {
  display: flex;
  flex-direction: column;
  gap: 0.125rem;
}

.customer-name {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 0.875rem;
}

.customer-id {
  font-size: 0.75rem;
  color: var(--text-secondary);
  opacity: 0.8;
}

/* Product Info */
.product-info {
  gap: 0.25rem;
  flex-direction: column;
  align-items: flex-start;
}

.product-count {
  font-weight: 700;
  font-size: 1.125rem;
  color: var(--text-primary);
}

.product-label {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

/* Amount */
.amount {
  font-weight: 700;
  font-size: 1rem;
  color: #10b981;
  font-family: 'Monaco', 'Menlo', monospace;
}

/* Status Badges */
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.025em;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  animation: pulse-dot 2s infinite;
}

.status-pending {
  background: linear-gradient(135deg, #fef3c7, #fde68a);
  color: #92400e;
  border: 1px solid #f59e0b;
}

.status-pending .status-dot {
  background: #f59e0b;
}

.status-processing {
  background: linear-gradient(135deg, #dbeafe, #bfdbfe);
  color: #1e40af;
  border: 1px solid #3b82f6;
}

.status-processing .status-dot {
  background: #3b82f6;
}

.status-completed {
  background: linear-gradient(135deg, #d1fae5, #a7f3d0);
  color: #065f46;
  border: 1px solid #10b981;
}

.status-completed .status-dot {
  background: #10b981;
}

.status-cancelled {
  background: linear-gradient(135deg, #fee2e2, #fecaca);
  color: #991b1b;
  border: 1px solid #ef4444;
}

.status-cancelled .status-dot {
  background: #ef4444;
}

/* Action Buttons */
.action-buttons {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.btn-view {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  padding: 0.5rem 0.75rem;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.75rem;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn-view:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-edit {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-tertiary);
  color: var(--text-secondary);
  border: 1px solid var(--border-color);
  padding: 0.5rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  width: 36px;
  height: 36px;
}

.btn-edit:hover {
  background: var(--border-color);
  color: var(--text-primary);
  transform: translateY(-1px);
}

.btn-icon {
  font-size: 0.875rem;
  margin-right: 4px;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 3rem 2rem;
  background: var(--card-bg);
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
  opacity: 0.5;
}

.empty-state h3 {
  margin: 0 0 0.5rem 0;
  color: var(--text-primary);
  font-size: 1.125rem;
}

.empty-state p {
  margin: 0;
  color: var(--text-secondary);
  font-size: 0.875rem;
}

/* Animations */
@keyframes pulse-dot {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.categories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1rem;
  width: 100%;
  box-sizing: border-box;
}

.category-card {
  background: var(--bg-tertiary);
  padding: 1rem;
  border-radius: 8px;
  text-decoration: none;
  color: inherit;
  transition: all 0.3s ease;
  text-align: center;
  border: 1px solid var(--border-color);
  width: 100%;
  box-sizing: border-box;
}

.category-card:hover {
  background: var(--border-color);
  transform: translateY(-1px);
}

.category-icon {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.2rem;
  margin: 0 auto 0.5rem;
}

.category-card h3 {
  margin: 0 0 0.25rem;
  color: #2c3e50;
  font-size: 1rem;
}

.category-card p {
  margin: 0;
  color: #7f8c8d;
  font-size: 0.8rem;
}

/* Icon styles */
.icon-bell::before {
  content: '🔔';
}
.icon-phone::before {
  content: '📱';
}
.icon-shopping-cart::before {
  content: '🛒';
}
.icon-dollar::before {
  content: '💰';
}
.icon-users::before {
  content: '👥';
}
.icon-receipt::before {
  content: '🧾';
}
.icon-cash-register::before {
  content: '🏪';
}
.icon-globe::before {
  content: '🌐';
}
.icon-brand::before {
  content: '🏷️';
}
.icon-cpu::before {
  content: '⚙️';
}
.icon-memory::before {
  content: '💾';
}
.icon-storage::before {
  content: '💿';
}
</style>
