<template>
  <div class="dashboard">
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
            </div>
          </div>
        </div>
      </section>

      <!-- Quick Actions -->
      <section class="quick-actions">
        <h2>Thao tác nhanh</h2>
        <div class="actions-grid">
          <router-link to="/san-pham" class="action-card">
            <div class="action-icon">
              <i class="icon-phone" />
            </div>
            <h3>Quản lý sản phẩm</h3>
            <p>Thêm, sửa, xóa sản phẩm</p>
          </router-link>

          <router-link to="/hoa-don" class="action-card">
            <div class="action-icon">
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
          </router-link>
        </div>
      </section>

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
        </div>
      </section>
    </main>
  </div>
</template>

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
  }
}
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
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
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
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
}

.action-card {
  background: white;
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
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
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
}
</style>
