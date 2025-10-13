<template>
  <div class="phan-tich-khach-hang-page">
    <div class="page-header">
      <h1 class="page-title">Phân tích khách hàng</h1>
      <p class="page-subtitle">Hiểu rõ hành vi và xu hướng mua sắm của khách hàng</p>
    </div>

    <!-- Customer Metrics -->
    <div class="metrics-grid">
      <div class="metric-card">
        <div class="metric-icon customers">
          <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                  d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
          </svg>
        </div>
        <div class="metric-content">
          <h3>Tổng khách hàng</h3>
          <p class="metric-value">2,847</p>
          <span class="metric-trend positive">+12.5%</span>
        </div>
      </div>

      <div class="metric-card">
        <div class="metric-icon new-customers">
          <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                  d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
          </svg>
        </div>
        <div class="metric-content">
          <h3>Khách hàng mới</h3>
          <p class="metric-value">156</p>
          <span class="metric-trend positive">+8.2%</span>
        </div>
      </div>

      <div class="metric-card">
        <div class="metric-icon retention">
          <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                  d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
          </svg>
        </div>
        <div class="metric-content">
          <h3>Tỷ lệ quay lại</h3>
          <p class="metric-value">68.5%</p>
          <span class="metric-trend positive">+3.1%</span>
        </div>
      </div>

      <div class="metric-card">
        <div class="metric-icon lifetime-value">
          <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                  d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
          </svg>
        </div>
        <div class="metric-content">
          <h3>Giá trị TB/KH</h3>
          <p class="metric-value">2.8M ₫</p>
          <span class="metric-trend negative">-1.2%</span>
        </div>
      </div>
    </div>

    <!-- Customer Segments -->
    <div class="segments-section">
      <div class="section-header">
        <h3>Phân khúc khách hàng</h3>
        <select class="period-select">
          <option>30 ngày qua</option>
          <option>90 ngày qua</option>
          <option>1 năm qua</option>
        </select>
      </div>
      
      <div class="segments-grid">
        <div class="segment-card vip">
          <div class="segment-header">
            <h4>Khách hàng VIP</h4>
            <span class="segment-count">124 KH</span>
          </div>
          <div class="segment-stats">
            <div class="stat-item">
              <span class="stat-label">Doanh thu:</span>
              <span class="stat-value">45.2%</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">Đơn hàng TB:</span>
              <span class="stat-value">8.5M ₫</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">Tần suất mua:</span>
              <span class="stat-value">2.3 lần/tháng</span>
            </div>
          </div>
        </div>

        <div class="segment-card loyal">
          <div class="segment-header">
            <h4>Khách hàng thân thiết</h4>
            <span class="segment-count">456 KH</span>
          </div>
          <div class="segment-stats">
            <div class="stat-item">
              <span class="stat-label">Doanh thu:</span>
              <span class="stat-value">32.8%</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">Đơn hàng TB:</span>
              <span class="stat-value">3.2M ₫</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">Tần suất mua:</span>
              <span class="stat-value">1.5 lần/tháng</span>
            </div>
          </div>
        </div>

        <div class="segment-card regular">
          <div class="segment-header">
            <h4>Khách hàng thường xuyên</h4>
            <span class="segment-count">892 KH</span>
          </div>
          <div class="segment-stats">
            <div class="stat-item">
              <span class="stat-label">Doanh thu:</span>
              <span class="stat-value">18.5%</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">Đơn hàng TB:</span>
              <span class="stat-value">1.8M ₫</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">Tần suất mua:</span>
              <span class="stat-value">0.8 lần/tháng</span>
            </div>
          </div>
        </div>

        <div class="segment-card new">
          <div class="segment-header">
            <h4>Khách hàng mới</h4>
            <span class="segment-count">1,375 KH</span>
          </div>
          <div class="segment-stats">
            <div class="stat-item">
              <span class="stat-label">Doanh thu:</span>
              <span class="stat-value">3.5%</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">Đơn hàng TB:</span>
              <span class="stat-value">950K ₫</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">Tần suất mua:</span>
              <span class="stat-value">0.2 lần/tháng</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Top Customers -->
    <div class="top-customers-section">
      <div class="section-header">
        <h3>Top khách hàng</h3>
        <div class="header-actions">
          <button class="export-btn">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-4-4m4 4l4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            Xuất danh sách
          </button>
        </div>
      </div>
      
      <div class="customers-table">
        <table>
          <thead>
            <tr>
              <th>Hạng</th>
              <th>Khách hàng</th>
              <th>Loại</th>
              <th>Tổng chi tiêu</th>
              <th>Số đơn hàng</th>
              <th>Lần mua cuối</th>
              <th>Trạng thái</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td class="rank">1</td>
              <td>
                <div class="customer-info">
                  <div class="customer-avatar">NV</div>
                  <div>
                    <div class="customer-name">Nguyễn Văn A</div>
                    <div class="customer-phone">0901234567</div>
                  </div>
                </div>
              </td>
              <td><span class="customer-type vip">VIP</span></td>
              <td class="amount">45,680,000 ₫</td>
              <td>28</td>
              <td>2 ngày trước</td>
              <td><span class="status active">Hoạt động</span></td>
            </tr>
            <tr>
              <td class="rank">2</td>
              <td>
                <div class="customer-info">
                  <div class="customer-avatar">LT</div>
                  <div>
                    <div class="customer-name">Lê Thị B</div>
                    <div class="customer-phone">0912345678</div>
                  </div>
                </div>
              </td>
              <td><span class="customer-type vip">VIP</span></td>
              <td class="amount">38,920,000 ₫</td>
              <td>22</td>
              <td>1 tuần trước</td>
              <td><span class="status active">Hoạt động</span></td>
            </tr>
            <tr>
              <td class="rank">3</td>
              <td>
                <div class="customer-info">
                  <div class="customer-avatar">TV</div>
                  <div>
                    <div class="customer-name">Trần Văn C</div>
                    <div class="customer-phone">0923456789</div>
                  </div>
                </div>
              </td>
              <td><span class="customer-type loyal">Thân thiết</span></td>
              <td class="amount">32,150,000 ₫</td>
              <td>19</td>
              <td>3 ngày trước</td>
              <td><span class="status active">Hoạt động</span></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
// Placeholder for customer analytics page
</script>

<style scoped>
.phan-tich-khach-hang-page {
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

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.metric-card {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 1rem;
}

.metric-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.metric-icon.customers {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  color: white;
}

.metric-icon.new-customers {
  background: linear-gradient(135deg, #10b981, #059669);
  color: white;
}

.metric-icon.retention {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  color: white;
}

.metric-icon.lifetime-value {
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
  color: white;
}

.metric-content h3 {
  font-size: 0.875rem;
  color: #6b7280;
  margin: 0 0 0.25rem 0;
  font-weight: 500;
}

.metric-value {
  font-size: 1.75rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 0.25rem 0;
}

.metric-trend {
  font-size: 0.75rem;
  font-weight: 500;
}

.metric-trend.positive {
  color: #10b981;
}

.metric-trend.negative {
  color: #ef4444;
}

.segments-section, .top-customers-section {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  padding: 1.5rem;
  margin-bottom: 2rem;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.section-header h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.period-select {
  padding: 0.5rem 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 0.875rem;
}

.segments-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1rem;
}

.segment-card {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 1rem;
  border-left: 4px solid;
}

.segment-card.vip {
  border-left-color: #dc2626;
  background: #fef2f2;
}

.segment-card.loyal {
  border-left-color: #f59e0b;
  background: #fffbeb;
}

.segment-card.regular {
  border-left-color: #3b82f6;
  background: #eff6ff;
}

.segment-card.new {
  border-left-color: #10b981;
  background: #ecfdf5;
}

.segment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.segment-header h4 {
  font-size: 1rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.segment-count {
  font-size: 0.875rem;
  color: #6b7280;
  font-weight: 500;
}

.segment-stats {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-label {
  font-size: 0.75rem;
  color: #6b7280;
}

.stat-value {
  font-size: 0.75rem;
  font-weight: 600;
  color: #1f2937;
}

.header-actions {
  display: flex;
  gap: 0.75rem;
}

.export-btn {
  background: linear-gradient(135deg, #10b981, #059669);
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.2s ease;
}

.export-btn:hover {
  transform: translateY(-1px);
}

.customers-table {
  overflow-x: auto;
}

.customers-table table {
  width: 100%;
  border-collapse: collapse;
}

.customers-table th {
  background: #f9fafb;
  padding: 1rem;
  text-align: left;
  font-weight: 600;
  color: #374151;
  border-bottom: 1px solid #e5e7eb;
}

.customers-table td {
  padding: 1rem;
  border-bottom: 1px solid #e5e7eb;
  color: #1f2937;
}

.rank {
  font-weight: 700;
  color: #f59e0b;
}

.customer-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.customer-avatar {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.875rem;
}

.customer-name {
  font-weight: 600;
  color: #1f2937;
}

.customer-phone {
  font-size: 0.75rem;
  color: #6b7280;
}

.customer-type {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 500;
}

.customer-type.vip {
  background: #fee2e2;
  color: #991b1b;
}

.customer-type.loyal {
  background: #fef3c7;
  color: #92400e;
}

.amount {
  font-weight: 600;
  color: #10b981;
}

.status {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 500;
}

.status.active {
  background: #d1fae5;
  color: #065f46;
}
</style>




