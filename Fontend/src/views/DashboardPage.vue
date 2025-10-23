<template>
  <div class="dashboard">
<<<<<<< HEAD
    <PosHeader />
    
    <!-- Main Content -->
    <main class="dashboard-main">
      <!-- Welcome Section -->
      <section class="welcome-section">
        <div class="welcome-content">
          <div class="welcome-text">
            <h1 class="welcome-title">Dashboard Tổng Quan</h1>
            <p class="welcome-subtitle">Chào mừng trở lại! Đây là tổng quan về hệ thống của bạn</p>
            <div class="welcome-actions">
              <button class="refresh-btn" @click="refreshData" :disabled="loading">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                </svg>
                {{ loading ? 'Đang tải...' : 'Làm mới' }}
              </button>
              <router-link to="/thong-ke" class="view-stats-btn">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                </svg>
                Xem thống kê chi tiết
              </router-link>
            </div>
          </div>
          <div class="welcome-chart">
            <canvas ref="revenueChart" class="chart-canvas"></canvas>
          </div>
        </div>
      </section>

      <!-- System Overview Cards -->
      <section class="overview-section">
        <div class="section-header">
          <h2 class="section-title">Tổng quan hệ thống</h2>
          <p class="section-subtitle">Cập nhật lần cuối: {{ lastUpdated }}</p>
        </div>
        <div class="overview-grid">
          <div class="overview-card revenue-card">
            <div class="card-icon">
              <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
              </svg>
            </div>
            <div class="card-content">
              <h3 class="card-title">Doanh thu hôm nay</h3>
              <p class="card-value">{{ formatCurrency(overview.todayRevenue) }}</p>
              <p class="card-subtitle">Trong ngày</p>
            </div>
          </div>

          <div class="overview-card orders-card">
            <div class="card-icon">
              <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
              </svg>
            </div>
            <div class="card-content">
              <h3 class="card-title">Đơn hàng hôm nay</h3>
              <p class="card-value">{{ overview.todayOrders }}</p>
              <p class="card-subtitle">Trong ngày</p>
            </div>
          </div>

          <div class="overview-card products-card">
            <div class="card-icon">
              <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
              </svg>
            </div>
            <div class="card-content">
              <h3 class="card-title">Sản phẩm bán hôm nay</h3>
              <p class="card-value">{{ overview.todayProductsSold }}</p>
              <p class="card-subtitle">Trong ngày</p>
            </div>
          </div>

          <div class="overview-card customers-card">
            <div class="card-icon">
              <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
              </svg>
            </div>
            <div class="card-content">
              <h3 class="card-title">Khách hàng mới hôm nay</h3>
              <p class="card-value">{{ overview.todayNewCustomers }}</p>
              <p class="card-subtitle">Trong ngày</p>
            </div>
          </div>

          <div class="overview-card staff-card">
            <div class="card-icon">
              <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z" />
              </svg>
            </div>
            <div class="card-content">
              <h3 class="card-title">Nhân viên</h3>
              <p class="card-value">{{ overview.totalStaff }}</p>
              <p class="card-subtitle">Đang hoạt động</p>
            </div>
          </div>

          <div class="overview-card categories-card">
            <div class="card-icon">
              <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
              </svg>
            </div>
            <div class="card-content">
              <h3 class="card-title">Danh mục</h3>
              <p class="card-value">{{ overview.totalCategories }}</p>
              <p class="card-subtitle">Thương hiệu: {{ overview.totalBrands }}</p>
=======
    <!-- Header -->
    <header class="dashboard-header">
      <div class="header-content">
        <div class="logo">
          <h1>PhoniX Admin</h1>
          <p>Hệ thống quản lý điện thoại thông minh</p>
        </div>
        <div class="header-actions">
          <button class="btn-notification">
            <i class="icon-bell" />
            <span class="badge">3</span>
          </button>
          <div class="user-profile">
            <img src="https://via.placeholder.com/40" alt="Admin" class="avatar" />
            <span>Administrator</span>
          </div>
        </div>
      </div>
    </header>

    <!-- Main Content -->
    <main class="dashboard-main">
      <!-- Stats Cards -->
      <section class="stats-section">
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon">
              <i class="icon-phone" />
            </div>
            <div class="stat-content">
              <h3>{{ stats.totalProducts }}</h3>
              <p>Tổng sản phẩm</p>
              <span class="stat-change positive">+12%</span>
            </div>
          </div>

          <div class="stat-card">
            <div class="stat-icon">
              <i class="icon-shopping-cart" />
            </div>
            <div class="stat-content">
              <h3>{{ stats.totalOrders }}</h3>
              <p>Đơn hàng hôm nay</p>
              <span class="stat-change positive">+8%</span>
            </div>
          </div>

          <div class="stat-card">
            <div class="stat-icon">
              <i class="icon-dollar" />
            </div>
            <div class="stat-content">
              <h3>{{ formatCurrency(stats.revenue) }}</h3>
              <p>Doanh thu hôm nay</p>
              <span class="stat-change positive">+15%</span>
            </div>
          </div>

          <div class="stat-card">
            <div class="stat-icon">
              <i class="icon-users" />
            </div>
            <div class="stat-content">
              <h3>{{ stats.totalCustomers }}</h3>
              <p>Khách hàng mới</p>
              <span class="stat-change positive">+5%</span>
>>>>>>> origin/Huan
            </div>
          </div>
        </div>
      </section>

      <!-- Quick Actions -->
<<<<<<< HEAD
      <section class="quick-actions-section">
        <div class="section-header">
          <h2 class="section-title">Thao tác nhanh</h2>
        </div>
        <div class="actions-grid">
          <router-link to="/san-pham" class="action-card">
            <div class="action-icon">
              <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
              </svg>
            </div>
            <div class="action-content">
              <h3 class="action-title">Quản lý sản phẩm</h3>
              <p class="action-description">Thêm, sửa, xóa sản phẩm</p>
            </div>
            <div class="action-badge">{{ overview.totalProducts }}</div>
=======
      <section class="quick-actions">
        <h2>Thao tác nhanh</h2>
        <div class="actions-grid">
          <router-link to="/san-pham" class="action-card">
            <div class="action-icon">
              <i class="icon-phone" />
            </div>
            <h3>Quản lý sản phẩm</h3>
            <p>Thêm, sửa, xóa sản phẩm</p>
>>>>>>> origin/Huan
          </router-link>

          <router-link to="/hoa-don" class="action-card">
            <div class="action-icon">
<<<<<<< HEAD
              <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
            </div>
            <div class="action-content">
              <h3 class="action-title">Quản lý đơn hàng</h3>
              <p class="action-description">Xem và xử lý đơn hàng</p>
            </div>
            <div class="action-badge">{{ overview.totalOrders }}</div>
          </router-link>

          <router-link to="/khach-hang" class="action-card">
            <div class="action-icon">
              <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
              </svg>
            </div>
            <div class="action-content">
              <h3 class="action-title">Quản lý khách hàng</h3>
              <p class="action-description">Thông tin khách hàng</p>
            </div>
            <div class="action-badge">{{ overview.totalCustomers }}</div>
          </router-link>

          <router-link to="/nhan-vien" class="action-card">
            <div class="action-icon">
              <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z" />
              </svg>
            </div>
            <div class="action-content">
              <h3 class="action-title">Quản lý nhân viên</h3>
              <p class="action-description">Thông tin nhân viên</p>
            </div>
            <div class="action-badge">{{ overview.totalStaff }}</div>
=======
              <i class="icon-receipt" />
            </div>
            <h3>Quản lý hóa đơn</h3>
            <p>Xem và quản lý đơn hàng</p>
          </router-link>

          <router-link to="/pos" class="action-card">
            <div class="action-icon">
              <i class="icon-cash-register" />
            </div>
            <h3>Bán hàng tại quầy</h3>
            <p>Hệ thống POS</p>
          </router-link>

          <router-link to="/online" class="action-card">
            <div class="action-icon">
              <i class="icon-globe" />
            </div>
            <h3>Bán hàng online</h3>
            <p>Quản lý đơn hàng online</p>
>>>>>>> origin/Huan
          </router-link>
        </div>
      </section>

<<<<<<< HEAD
      <!-- Recent Data -->
      <section class="recent-data-section">
        <div class="data-grid">
          <!-- Recent Orders -->
          <div class="data-card">
            <div class="card-header">
              <h3 class="card-title">Đơn hàng gần đây</h3>
              <router-link to="/hoa-don" class="view-all-link">Xem tất cả</router-link>
            </div>
            <div class="card-content">
              <div v-if="loading" class="loading-state">
                <div class="loading-spinner"></div>
                <p>Đang tải dữ liệu...</p>
              </div>
              <div v-else-if="recentOrders.length === 0" class="empty-state">
                <svg class="w-12 h-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
                <p>Chưa có đơn hàng nào</p>
              </div>
              <div v-else class="orders-list">
                <div v-for="order in recentOrders" :key="order.id" class="order-item">
                  <div class="order-info">
                    <h4 class="order-id">{{ order.maHoaDon }}</h4>
                    <p class="order-customer">{{ order.customerName }}</p>
                  </div>
                  <div class="order-details">
                    <span class="order-amount">{{ formatCurrency(order.total) }}</span>
                    <span class="order-status" :class="getStatusClass(order.status)">
                      {{ getStatusText(order.status) }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Top Products -->
          <div class="data-card">
            <div class="card-header">
              <h3 class="card-title">Sản phẩm bán chạy</h3>
              <router-link to="/thong-ke" class="view-all-link">Xem chi tiết</router-link>
            </div>
            <div class="card-content">
              <div v-if="loading" class="loading-state">
                <div class="loading-spinner"></div>
                <p>Đang tải dữ liệu...</p>
              </div>
              <div v-else-if="topProducts.length === 0" class="empty-state">
                <svg class="w-12 h-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
                </svg>
                <p>Chưa có dữ liệu sản phẩm</p>
              </div>
              <div v-else class="products-list">
                <div v-for="(product, index) in topProducts" :key="product.productId" class="product-item">
                  <div class="product-rank" :class="`rank-${index + 1}`">{{ index + 1 }}</div>
                  <div class="product-info">
                    <h4 class="product-name">{{ product.productName }}</h4>
                    <p class="product-sales">{{ product.totalSold }} sản phẩm</p>
                  </div>
                  <div class="product-revenue">{{ formatCurrency(product.totalRevenue) }}</div>
                </div>
              </div>
            </div>
          </div>
=======
      <!-- Recent Orders -->
      <section class="recent-orders">
        <div class="section-header">
          <h2>Đơn hàng gần đây</h2>
          <router-link to="/hoa-don" class="view-all"> Xem tất cả </router-link>
        </div>
        <div class="orders-table">
          <table>
            <thead>
              <tr>
                <th>Mã đơn</th>
                <th>Khách hàng</th>
                <th>Sản phẩm</th>
                <th>Tổng tiền</th>
                <th>Trạng thái</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="order in recentOrders" :key="order.id">
                <td>{{ order.maHoaDon }}</td>
                <td>{{ order.khachHang?.tenKhachHang || 'N/A' }}</td>
                <td>{{ order.chiTietHoaDons?.length || 0 }} sản phẩm</td>
                <td>{{ formatCurrency(order.tongTien) }}</td>
                <td>
                  <span :class="getStatusClass(order.trangThai)">{{
                    getStatusText(order.trangThai)
                  }}</span>
                </td>
                <td>
                  <button class="btn-action">Xem</button>
                </td>
              </tr>
            </tbody>
          </table>
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
>>>>>>> origin/Huan
        </div>
      </section>
    </main>
  </div>
</template>

<<<<<<< HEAD
<script>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { Chart, registerables } from 'chart.js'
import PosHeader from '../components/PosHeader.vue'
import api from '../services/api'

Chart.register(...registerables)

export default {
  name: 'DashboardPage',
  components: {
    PosHeader
  },
  setup() {
    // Reactive data
    const loading = ref(false)
    const overview = ref({
      totalStaff: 0,
      totalCategories: 0,
      totalBrands: 0,
      todayRevenue: 0,
      todayOrders: 0,
      todayProductsSold: 0,
      todayNewCustomers: 0
    })
    
    const recentOrders = ref([])
    const topProducts = ref([])
    const chartData = ref([])
    const lastUpdated = ref('')
    
    // Chart refs
    const revenueChart = ref(null)
    const chartInstance = ref(null)

    // Load dashboard data
    async function loadDashboardData() {
      loading.value = true
      try {
        console.log('🔄 Loading dashboard data...')
        
        // Load overview data - sử dụng API mới
        const overviewRes = await api.get('/api/dashboard/tong-quan')
        console.log('[Dashboard] /tong-quan response:', overviewRes.data)
        if (overviewRes.data && overviewRes.data.thanhCong) {
          const data = overviewRes.data.duLieu
          overview.value = {
            totalStaff: data.tongNhanVien || 0,
            totalCategories: data.tongDanhMuc || 0,
            totalBrands: data.tongHang || 0,
            totalProducts: data.tongSanPham || 0,
            totalCustomers: data.tongKhachHang || 0,
            todayRevenue: data.doanhThuHomNay || 0,
            todayOrders: data.donHangHomNay || 0,
            // Backend hiện không trả sanPhamBanRaHomNay cho dashboard cơ bản
            todayProductsSold: data.sanPhamBanRaHomNay || 0,
            todayNewCustomers: data.khachHangMoiHomNay || 0
          }
          lastUpdated.value = data.thoiGianCapNhat || ''
        }
        
        // Load recent orders - sử dụng API mới
        const ordersRes = await api.get('/api/dashboard/don-hang-gan-day')
        console.log('[Dashboard] /don-hang-gan-day response:', ordersRes.data)
        if (ordersRes.data && ordersRes.data.thanhCong) {
          recentOrders.value = ordersRes.data.duLieu.donHangGanDay || []
        }
        
        // Load top products - sử dụng API mới
        const productsRes = await api.get('/api/dashboard/san-pham-ban-chay-nhat')
        console.log('[Dashboard] /san-pham-ban-chay-nhat response:', productsRes.data)
        if (productsRes.data && productsRes.data.thanhCong) {
          topProducts.value = productsRes.data.duLieu.sanPhamBanChay || []
        }
        
        // Load chart data - sử dụng API mới
        const chartRes = await api.get('/api/dashboard/bieu-do-doanh-thu')
        console.log('[Dashboard] /bieu-do-doanh-thu response:', chartRes.data)
        if (chartRes.data && chartRes.data.thanhCong) {
          chartData.value = chartRes.data.duLieu.duLieuBieuDo || []
        }
        
        // Initialize chart
        await nextTick()
        await initializeChart()
        
        console.log('✅ Dashboard data loaded successfully')
        
      } catch (error) {
        console.error('❌ Error loading dashboard data:', error)
        setDefaultValues()
      } finally {
        loading.value = false
      }
    }

    // Set default values
    function setDefaultValues() {
      overview.value = {
        totalProducts: 0,
        totalOrders: 0,
        totalCustomers: 0,
        totalStaff: 0,
        totalCategories: 0,
        totalBrands: 0,
        totalRevenue: 0,
        recentRevenue: 0,
        recentOrders: 0,
        newCustomers: 0,
        productsSold: 0
      }
      recentOrders.value = []
      topProducts.value = []
      chartData.value = []
      lastUpdated.value = new Date().toLocaleString('vi-VN')
    }

    // Initialize chart
    async function initializeChart() {
      try {
        await nextTick()
        
        if (!revenueChart.value) {
          console.warn('Chart canvas not found')
          return
        }

        // Destroy existing chart
        if (chartInstance.value) {
          chartInstance.value.destroy()
          chartInstance.value = null
        }

        const ctx = revenueChart.value.getContext('2d')
        const labels = chartData.value.map(item => {
          if (item.date) {
            return new Date(item.date).toLocaleDateString('vi-VN')
          }
          return item.label || 'Không có dữ liệu'
        })
        const values = chartData.value.map(item => Number(item.value || item.revenue) || 0)

        chartInstance.value = new Chart(ctx, {
          type: 'line',
          data: {
            labels: labels,
            datasets: [{
              label: 'Doanh thu (VNĐ)',
              data: values,
              borderColor: '#ff8c42',
              backgroundColor: 'rgba(255, 140, 66, 0.1)',
              borderWidth: 3,
              fill: true,
              tension: 0.4,
              pointBackgroundColor: '#ff8c42',
              pointBorderColor: '#ffffff',
              pointBorderWidth: 2,
              pointRadius: 6,
              pointHoverRadius: 8
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: {
                display: false
              },
              tooltip: {
                backgroundColor: 'rgba(0, 0, 0, 0.8)',
                titleColor: '#ffffff',
                bodyColor: '#ffffff',
                borderColor: '#ff8c42',
                borderWidth: 1,
                callbacks: {
                  label: function(context) {
                    return 'Doanh thu: ' + formatCurrency(context.parsed.y)
                  }
                }
              }
            },
            scales: {
              x: {
                grid: {
                  display: false
                },
                ticks: {
                  color: '#666666',
                  font: {
                    size: 12
                  }
                }
              },
              y: {
                beginAtZero: true,
                grid: {
                  color: 'rgba(0, 0, 0, 0.1)'
                },
                ticks: {
                  color: '#666666',
                  font: {
                    size: 12
                  },
                  callback: function(value) {
                    return formatCurrency(value)
                  }
                }
              }
            },
            interaction: {
              intersect: false,
              mode: 'index'
            }
          }
        })
        
        console.log('✅ Chart created successfully')
      } catch (error) {
        console.error('❌ Error creating chart:', error)
      }
    }

    // Refresh data
    async function refreshData() {
      await loadDashboardData()
    }

    // Helper functions
    function formatCurrency(amount) {
      if (!amount) return '0 ₫'
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(amount)
    }

    function getStatusClass(status) {
      const statusClasses = {
        0: 'status-pending',
        1: 'status-paid',
        2: 'status-waiting',
        3: 'status-processing',
        4: 'status-completed',
        5: 'status-cancelled'
      }
      return statusClasses[status] || 'status-unknown'
    }

    function getStatusText(status) {
      const statusTexts = {
        0: 'Chờ xác nhận',
        1: 'Đã thanh toán',
        2: 'Chờ giao hàng',
        3: 'Đang giao hàng',
        4: 'Hoàn thành',
        5: 'Đã hủy'
      }
      return statusTexts[status] || 'Không xác định'
    }

    // Lifecycle
    onMounted(async () => {
      await loadDashboardData()
    })

    onUnmounted(() => {
      if (chartInstance.value) {
        chartInstance.value.destroy()
      }
    })

    return {
      loading,
      overview,
      recentOrders,
      topProducts,
      chartData,
      lastUpdated,
      revenueChart,
      refreshData,
      formatCurrency,
      getStatusClass,
      getStatusText
    }
=======
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '@/services/api'

const stats = ref({
  totalProducts: 0,
  totalOrders: 0,
  revenue: 0,
  totalCustomers: 0,
  totalBrands: 0,
  totalChips: 0,
  totalRams: 0,
  totalRoms: 0,
})

const recentOrders = ref([])

onMounted(async () => {
  await loadStats()
  await loadRecentOrders()
})

async function loadStats() {
  try {
    // Load products count
    const productsRes = await api.get('/api/san-pham')
    stats.value.totalProducts = productsRes.data.length

    // Load orders count
    const ordersRes = await api.get('/api/hoa-don')
    stats.value.totalOrders = ordersRes.data.length
    stats.value.revenue = ordersRes.data.reduce(
      (sum: number, order: any) => sum + (order.tongTien || 0),
      0
    )

    // Load customers count
    const customersRes = await api.get('/api/khach-hang')
    stats.value.totalCustomers = customersRes.data.length

    // Load brands count
    const brandsRes = await api.get('/api/hang/active')
    stats.value.totalBrands = brandsRes.data.length

    // Load chips count
    const chipsRes = await api.get('/api/chip/active')
    stats.value.totalChips = chipsRes.data.length

    // Load RAMs count
    const ramsRes = await api.get('/api/ram/active')
    stats.value.totalRams = ramsRes.data.length

    // Load ROMs count
    const romsRes = await api.get('/api/rom/active')
    stats.value.totalRoms = romsRes.data.length
  } catch (error) {
    console.error('Error loading stats:', error)
  }
}

async function loadRecentOrders() {
  try {
    const { data } = await api.get('/api/hoa-don')
    recentOrders.value = data.slice(0, 5) // Get latest 5 orders
  } catch (error) {
    console.error('Error loading recent orders:', error)
  }
}

function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
}

function getStatusClass(status: number): string {
  switch (status) {
    case 1:
      return 'status-pending'
    case 2:
      return 'status-processing'
    case 3:
      return 'status-completed'
    case 4:
      return 'status-cancelled'
    default:
      return 'status-pending'
  }
}

function getStatusText(status: number): string {
  switch (status) {
    case 1:
      return 'Chờ xử lý'
    case 2:
      return 'Đang xử lý'
    case 3:
      return 'Hoàn thành'
    case 4:
      return 'Đã hủy'
    default:
      return 'Chờ xử lý'
>>>>>>> origin/Huan
  }
}
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
<<<<<<< HEAD
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
}

.dashboard-main {
  margin-top: 80px;
  padding: 2rem;
  width: 100%;
  min-height: calc(100vh - 80px);
}

/* Welcome Section */
.welcome-section {
  background: linear-gradient(135deg, #ff8c42 0%, #ffb347 100%);
  border-radius: 20px;
  padding: 3rem;
  margin-bottom: 2rem;
  box-shadow: 0 20px 40px rgba(255, 140, 66, 0.3);
  position: relative;
  overflow: hidden;
}

.welcome-section::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  animation: float 6s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(180deg); }
}

.welcome-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 100%;
}

.welcome-text {
  flex: 1;
  color: white;
}

.welcome-title {
  font-size: 3rem;
  font-weight: 800;
  margin-bottom: 1rem;
  text-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.welcome-subtitle {
  font-size: 1.2rem;
  margin-bottom: 2rem;
  opacity: 0.9;
  line-height: 1.6;
}

.welcome-actions {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.refresh-btn, .view-stats-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border-radius: 12px;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.3s ease;
  border: none;
  cursor: pointer;
}

.refresh-btn {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.refresh-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.view-stats-btn {
  background: white;
  color: #ff8c42;
}

.view-stats-btn:hover {
  background: #f8f9fa;
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
}

.welcome-chart {
  flex: 1;
  height: 300px;
  margin-left: 2rem;
  min-width: 400px;
  max-width: 600px;
}

.chart-canvas {
  width: 100% !important;
  height: 100% !important;
}

/* Overview Section */
.overview-section {
  margin-bottom: 2rem;
}

.section-header {
  margin-bottom: 1.5rem;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 0.5rem 0;
}

.section-subtitle {
  font-size: 0.875rem;
  color: #6b7280;
  margin: 0;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
  width: 100%;
  max-width: 100%;
}

.overview-card {
  background: white;
  border-radius: 16px;
  padding: 1.5rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  gap: 1rem;
}

.overview-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.card-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
=======
  background: #f8f9fa;
}

.dashboard-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 1rem 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
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
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.stats-section {
  margin-bottom: 2rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
}

.stat-card {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea, #764ba2);
>>>>>>> origin/Huan
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
<<<<<<< HEAD
  flex-shrink: 0;
}

.revenue-card .card-icon {
  background: linear-gradient(135deg, #ff8c42, #ffb347);
}

.orders-card .card-icon {
  background: linear-gradient(135deg, #ff6b35, #f7931e);
}

.products-card .card-icon {
  background: linear-gradient(135deg, #ff8c42, #ffb347);
}

.customers-card .card-icon {
  background: linear-gradient(135deg, #ff8c42, #ffb347);
}

.staff-card .card-icon {
  background: linear-gradient(135deg, #ff8c42, #ffb347);
}

.categories-card .card-icon {
  background: linear-gradient(135deg, #f59e0b, #fbbf24);
}

.card-content {
  flex: 1;
}

.card-title {
  font-size: 0.875rem;
  font-weight: 600;
  color: #6b7280;
  margin-bottom: 0.5rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.card-value {
  font-size: clamp(1.2rem, 4vw, 2rem);
  font-weight: 800;
  color: #1f2937;
  margin-bottom: 0.25rem;
  line-height: 1.1;
  word-break: break-all;
  overflow-wrap: break-word;
}

.card-subtitle {
  font-size: 0.875rem;
  color: #9ca3af;
  margin: 0;
}

/* Quick Actions Section */
.quick-actions-section {
  margin-bottom: 2rem;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
  width: 100%;
=======
  font-size: 1.5rem;
}

.stat-content h3 {
  margin: 0;
  font-size: 2rem;
  font-weight: 700;
  color: #2c3e50;
}

.stat-content p {
  margin: 0.25rem 0;
  color: #7f8c8d;
  font-size: 0.9rem;
}

.stat-change {
  font-size: 0.8rem;
  font-weight: 600;
}

.stat-change.positive {
  color: #27ae60;
}

.quick-actions {
  margin-bottom: 2rem;
}

.quick-actions h2 {
  margin-bottom: 1rem;
  color: #2c3e50;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
>>>>>>> origin/Huan
}

.action-card {
  background: white;
<<<<<<< HEAD
  border-radius: 16px;
  padding: 1.5rem;
  text-decoration: none;
  color: inherit;
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 0, 0, 0.05);
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.action-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(135deg, #ff8c42, #ffb347);
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

.action-card:hover::before {
  transform: scaleX(1);
}

.action-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  text-decoration: none;
  color: inherit;
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #ff8c42, #ffb347);
=======
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  text-decoration: none;
  color: inherit;
  transition: all 0.2s;
  text-align: center;
}

.action-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.action-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea, #764ba2);
>>>>>>> origin/Huan
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
<<<<<<< HEAD
  flex-shrink: 0;
}

.action-content {
  flex: 1;
}

.action-title {
  font-size: 1rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 0.25rem;
}

.action-description {
  font-size: 0.875rem;
  color: #6b7280;
  margin: 0;
}

.action-badge {
  background: linear-gradient(135deg, #ff8c42, #ffb347);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 600;
  min-width: 2rem;
  text-align: center;
}

/* Recent Data Section */
.recent-data-section {
  margin-bottom: 2rem;
}

.data-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(450px, 1fr));
  gap: 2rem;
  width: 100%;
}

.data-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.data-card .card-header {
  padding: 1.5rem 1.5rem 0 1.5rem;
  border-bottom: 1px solid #f3f4f6;
  margin-bottom: 0;
  padding-bottom: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 1.125rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.view-all-link {
  color: #ff8c42;
  text-decoration: none;
  font-size: 0.875rem;
  font-weight: 600;
  transition: color 0.3s ease;
}

.view-all-link:hover {
  color: #ff6b35;
  text-decoration: none;
}

.card-content {
  padding: 1.5rem;
}

/* Loading and Empty States */
.loading-state, .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 1rem;
  text-align: center;
  color: #6b7280;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #f3f4f6;
  border-top: 3px solid #ff8c42;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Orders List */
.orders-list {
  space-y: 0.75rem;
}

.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 0;
  border-bottom: 1px solid #f3f4f6;
}

.order-item:last-child {
  border-bottom: none;
}

.order-info {
  flex: 1;
}

.order-id {
  font-size: 0.875rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 0.25rem;
}

.order-customer {
  font-size: 0.75rem;
  color: #6b7280;
  margin: 0;
}

.order-details {
  text-align: right;
}

.order-amount {
  display: block;
  font-size: 0.875rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 0.25rem;
}

.order-status {
  font-size: 0.75rem;
  padding: 0.25rem 0.5rem;
  border-radius: 6px;
  font-weight: 500;
}

.status-pending { background: #fef3c7; color: #92400e; }
.status-paid { background: #e0e7ff; color: #3730a3; }
.status-waiting { background: #fef3c7; color: #92400e; }
.status-processing { background: #dbeafe; color: #1e40af; }
.status-completed { background: #d1fae5; color: #065f46; }
.status-cancelled { background: #fee2e2; color: #dc2626; }
.status-unknown { background: #f3f4f6; color: #6b7280; }

/* Products List */
.products-list {
  space-y: 0.75rem;
}

.product-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.75rem 0;
  border-bottom: 1px solid #f3f4f6;
}

.product-item:last-child {
  border-bottom: none;
}

.product-rank {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 0.875rem;
  color: white;
  flex-shrink: 0;
}

.product-rank.rank-1 { background: linear-gradient(135deg, #ffd700, #ffed4e); }
.product-rank.rank-2 { background: linear-gradient(135deg, #ff8c42, #ffb347); }
.product-rank.rank-3 { background: linear-gradient(135deg, #ff6b35, #f7931e); }
.product-rank.rank-4,
.product-rank.rank-5 { background: linear-gradient(135deg, #6b7280, #9ca3af); }

.product-info {
  flex: 1;
}

.product-name {
  font-size: 0.875rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 0.25rem;
}

.product-sales {
  font-size: 0.75rem;
  color: #6b7280;
  margin: 0;
}

.product-revenue {
  font-size: 0.875rem;
  font-weight: 600;
  color: #16a34a;
}

/* Responsive Design */
@media (max-width: 768px) {
  .dashboard-main {
    padding: 1rem;
  }
  
  .welcome-section {
    padding: 2rem;
  }
  
  .welcome-content {
    flex-direction: column;
    text-align: center;
  }
  
  .welcome-chart {
    margin-left: 0;
    margin-top: 2rem;
    height: 250px;
    min-width: unset;
    max-width: unset;
  }
  
  .welcome-title {
    font-size: 2rem;
  }
  
  .overview-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .card-value {
    font-size: 1.5rem;
  }

  .card-subtitle {
    font-size: 0.75rem;
  }
  
  .data-grid {
    grid-template-columns: 1fr;
  }
  
  .actions-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .welcome-actions {
    flex-direction: column;
    width: 100%;
  }
  
  .refresh-btn, .view-stats-btn {
    width: 100%;
    justify-content: center;
  }

  .overview-grid {
    grid-template-columns: 1fr;
    gap: 0.75rem;
  }

  .card-value {
    font-size: 1.2rem;
  }

  .overview-card {
    padding: 1rem;
  }
=======
  font-size: 1.5rem;
  margin: 0 auto 1rem;
}

.action-card h3 {
  margin: 0 0 0.5rem;
  color: #2c3e50;
}

.action-card p {
  margin: 0;
  color: #7f8c8d;
  font-size: 0.9rem;
}

.recent-orders,
.product-categories {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  margin-bottom: 2rem;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.section-header h2 {
  margin: 0;
  color: #2c3e50;
}

.view-all {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
}

.view-all:hover {
  text-decoration: underline;
}

.orders-table {
  overflow-x: auto;
}

.orders-table table {
  width: 100%;
  border-collapse: collapse;
}

.orders-table th,
.orders-table td {
  padding: 0.75rem;
  text-align: left;
  border-bottom: 1px solid #ecf0f1;
}

.orders-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #2c3e50;
}

.status-pending {
  background: #fff3cd;
  color: #856404;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
}

.status-processing {
  background: #d1ecf1;
  color: #0c5460;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
}

.status-completed {
  background: #d4edda;
  color: #155724;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
}

.status-cancelled {
  background: #f8d7da;
  color: #721c24;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
}

.btn-action {
  background: #667eea;
  color: white;
  border: none;
  padding: 0.25rem 0.75rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.8rem;
}

.btn-action:hover {
  background: #5a6fd8;
}

.categories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.category-card {
  background: #f8f9fa;
  padding: 1rem;
  border-radius: 8px;
  text-decoration: none;
  color: inherit;
  transition: all 0.2s;
  text-align: center;
}

.category-card:hover {
  background: #e9ecef;
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
  content: '';
  background-image: url('@/assets/settings 1.png');
  background-size: 16px 16px;
  background-repeat: no-repeat;
  background-position: center;
  width: 16px;
  height: 16px;
  display: inline-block;
}
.icon-memory::before {
  content: '💾';
}
.icon-storage::before {
  content: '💿';
>>>>>>> origin/Huan
}
</style>
