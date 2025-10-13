<template>
  <div class="bao-cao-ban-hang-page">
    <PosHeader />
    
    <div class="content">
      <div class="page-header">
        <h1 class="page-title">Báo cáo bán hàng</h1>
        <p class="page-subtitle">Theo dõi và phân tích hiệu suất bán hàng</p>
      </div>

    <!-- Filters -->
    <div class="filters-section">
      <div class="filter-group">
        <label class="filter-label">Thời gian:</label>
        <select class="filter-select">
          <option>Hôm nay</option>
          <option>7 ngày qua</option>
          <option>30 ngày qua</option>
          <option>Tùy chọn</option>
        </select>
      </div>
      <div class="filter-group">
        <label class="filter-label">Nhân viên:</label>
        <select class="filter-select">
          <option>Tất cả</option>
          <option>Nguyễn Văn A</option>
          <option>Trần Thị B</option>
        </select>
      </div>
      <button class="filter-btn">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
        </svg>
        Lọc
      </button>
    </div>

    <!-- Summary Cards -->
    <div class="summary-grid">
      <div class="summary-card">
        <h3>Tổng doanh thu</h3>
        <p class="summary-value">125,480,000 ₫</p>
        <span class="summary-trend positive">+15.2%</span>
      </div>
      <div class="summary-card">
        <h3>Số đơn hàng</h3>
        <p class="summary-value">1,247</p>
        <span class="summary-trend positive">+8.5%</span>
      </div>
      <div class="summary-card">
        <h3>Giá trị trung bình</h3>
        <p class="summary-value">100,580 ₫</p>
        <span class="summary-trend negative">-2.1%</span>
      </div>
      <div class="summary-card">
        <h3>Sản phẩm bán ra</h3>
        <p class="summary-value">3,892</p>
        <span class="summary-trend positive">+12.8%</span>
      </div>
    </div>

    <!-- Report Table -->
    <div class="report-table-section">
      <div class="table-header">
        <h3>Chi tiết bán hàng</h3>
        <button class="export-btn">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-4-4m4 4l4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          Xuất Excel
        </button>
      </div>
      
      <div class="table-container">
        <table class="report-table">
          <thead>
            <tr>
              <th>Ngày</th>
              <th>Mã đơn hàng</th>
              <th>Khách hàng</th>
              <th>Nhân viên</th>
              <th>Sản phẩm</th>
              <th>Số lượng</th>
              <th>Thành tiền</th>
              <th>Trạng thái</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>02/10/2025</td>
              <td>HD001</td>
              <td>Nguyễn Văn A</td>
              <td>Trần Thị B</td>
              <td>iPhone 15 Pro Max</td>
              <td>2</td>
              <td>59,980,000 ₫</td>
              <td><span class="status completed">Hoàn thành</span></td>
            </tr>
            <tr>
              <td>02/10/2025</td>
              <td>HD002</td>
              <td>Lê Thị C</td>
              <td>Phạm Văn D</td>
              <td>Samsung Galaxy S24</td>
              <td>1</td>
              <td>22,990,000 ₫</td>
              <td><span class="status completed">Hoàn thành</span></td>
            </tr>
            <tr>
              <td>01/10/2025</td>
              <td>HD003</td>
              <td>Hoàng Văn E</td>
              <td>Trần Thị B</td>
              <td>MacBook Pro M3</td>
              <td>1</td>
              <td>52,990,000 ₫</td>
              <td><span class="status pending">Đang xử lý</span></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    </div>
  </div>
</template>

<script setup>
import PosHeader from '@/components/PosHeader.vue'
// Placeholder for sales report page
</script>

<style scoped>
.bao-cao-ban-hang-page {
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

.filters-section {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  display: flex;
  gap: 1rem;
  align-items: end;
  margin-bottom: 2rem;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.filter-label {
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
}

.filter-select {
  padding: 0.5rem 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 0.875rem;
  min-width: 150px;
}

.filter-btn {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
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

.filter-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.summary-card {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.summary-card h3 {
  font-size: 0.875rem;
  color: #6b7280;
  margin: 0 0 0.5rem 0;
  font-weight: 500;
}

.summary-value {
  font-size: 1.75rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 0.5rem 0;
}

.summary-trend {
  font-size: 0.75rem;
  font-weight: 500;
}

.summary-trend.positive {
  color: #10b981;
}

.summary-trend.negative {
  color: #ef4444;
}

.report-table-section {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.table-header {
  padding: 1.5rem;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-header h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
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
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.4);
}

.table-container {
  overflow-x: auto;
}

.report-table {
  width: 100%;
  border-collapse: collapse;
}

.report-table th {
  background: #f9fafb;
  padding: 1rem;
  text-align: left;
  font-weight: 600;
  color: #374151;
  border-bottom: 1px solid #e5e7eb;
}

.report-table td {
  padding: 1rem;
  border-bottom: 1px solid #e5e7eb;
  color: #1f2937;
}

.status {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 500;
}

.status.completed {
  background: #d1fae5;
  color: #065f46;
}

.status.pending {
  background: #fef3c7;
  color: #92400e;
}
</style>


