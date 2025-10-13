<template>
  <div class="thong-ke-page">
    <PosHeader />
    
    <div class="content">
      <div class="page-header">
        <h1 class="page-title">Thống kê tổng quan</h1>
        <p class="page-subtitle">Xem tổng quan về hiệu suất kinh doanh</p>
      </div>

    <div class="stats-grid">
      <!-- Doanh thu hôm nay -->
      <div class="stat-card revenue">
        <div class="stat-icon">
          <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                  d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
          </svg>
        </div>
        <div class="stat-content">
          <h3 class="stat-title">Doanh thu hôm nay</h3>
          <p class="stat-value">25,480,000 ₫</p>
          <p class="stat-change positive">+12.5% so với hôm qua</p>
        </div>
      </div>

      <!-- Đơn hàng -->
      <div class="stat-card orders">
        <div class="stat-icon">
          <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                  d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
          </svg>
        </div>
        <div class="stat-content">
          <h3 class="stat-title">Đơn hàng hôm nay</h3>
          <p class="stat-value">147</p>
          <p class="stat-change positive">+8.2% so với hôm qua</p>
        </div>
      </div>

      <!-- Khách hàng mới -->
      <div class="stat-card customers">
        <div class="stat-icon">
          <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                  d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
          </svg>
        </div>
        <div class="stat-content">
          <h3 class="stat-title">Khách hàng mới</h3>
          <p class="stat-value">23</p>
          <p class="stat-change positive">+15.8% so với hôm qua</p>
        </div>
      </div>

      <!-- Sản phẩm bán chạy -->
      <div class="stat-card products">
        <div class="stat-icon">
          <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                  d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
          </svg>
        </div>
        <div class="stat-content">
          <h3 class="stat-title">Sản phẩm đã bán</h3>
          <p class="stat-value">892</p>
          <p class="stat-change negative">-2.1% so với hôm qua</p>
        </div>
      </div>
    </div>

    <!-- Chart placeholder -->
    <div class="chart-section">
      <div class="chart-card">
        <h3 class="chart-title">Biểu đồ doanh thu 7 ngày qua</h3>
        <div class="chart-placeholder">
          <p>Biểu đồ sẽ được hiển thị ở đây</p>
        </div>
      </div>
    </div>
    </div>
  </div>
</template>

<script setup>
import PosHeader from '@/components/PosHeader.vue'
// Placeholder for statistics page
</script>

<style scoped>
.thong-ke-page {
  padding: 2rem;
}

.page-header {
  margin-bottom: 2rem;
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 0.5rem 0;
}

.page-subtitle {
  color: #6b7280;
  margin: 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: transform 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-card.revenue .stat-icon {
  background: linear-gradient(135deg, #10b981, #059669);
  color: white;
}

.stat-card.orders .stat-icon {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  color: white;
}

.stat-card.customers .stat-icon {
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
  color: white;
}

.stat-card.products .stat-icon {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  color: white;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-content {
  flex: 1;
}

.stat-title {
  font-size: 0.875rem;
  color: #6b7280;
  margin: 0 0 0.25rem 0;
  font-weight: 500;
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 0.25rem 0;
}

.stat-change {
  font-size: 0.75rem;
  margin: 0;
  font-weight: 500;
}

.stat-change.positive {
  color: #10b981;
}

.stat-change.negative {
  color: #ef4444;
}

.chart-section {
  margin-top: 2rem;
}

.chart-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.chart-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 1rem 0;
}

.chart-placeholder {
  height: 300px;
  background: #f9fafb;
  border: 2px dashed #d1d5db;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b7280;
}
</style>


