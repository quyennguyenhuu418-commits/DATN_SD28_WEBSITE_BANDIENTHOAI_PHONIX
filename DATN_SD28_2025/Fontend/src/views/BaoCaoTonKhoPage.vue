<template>
  <div class="bao-cao-ton-kho-page">
    <PosHeader />
    
    <div class="content">
      <div class="page-header">
        <h1 class="page-title">Báo cáo tồn kho</h1>
        <p class="page-subtitle">Theo dõi và quản lý tình trạng tồn kho sản phẩm</p>
      </div>

    <!-- Alert Cards -->
    <div class="alert-grid">
      <div class="alert-card warning">
        <div class="alert-icon">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.082 16.5c-.77.833.192 2.5 1.732 2.5z" />
          </svg>
        </div>
        <div class="alert-content">
          <h3>Sắp hết hàng</h3>
          <p>15 sản phẩm</p>
        </div>
      </div>

      <div class="alert-card danger">
        <div class="alert-icon">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <div class="alert-content">
          <h3>Hết hàng</h3>
          <p>8 sản phẩm</p>
        </div>
      </div>

      <div class="alert-card success">
        <div class="alert-icon">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <div class="alert-content">
          <h3>Đủ hàng</h3>
          <p>247 sản phẩm</p>
        </div>
      </div>

      <div class="alert-card info">
        <div class="alert-icon">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <div class="alert-content">
          <h3>Tồn kho cao</h3>
          <p>32 sản phẩm</p>
        </div>
      </div>
    </div>

    <!-- Filters -->
    <div class="filters-section">
      <div class="filter-group">
        <label class="filter-label">Danh mục:</label>
        <select class="filter-select">
          <option>Tất cả</option>
          <option>Điện thoại</option>
          <option>Laptop</option>
          <option>Phụ kiện</option>
        </select>
      </div>
      <div class="filter-group">
        <label class="filter-label">Trạng thái:</label>
        <select class="filter-select">
          <option>Tất cả</option>
          <option>Đủ hàng</option>
          <option>Sắp hết</option>
          <option>Hết hàng</option>
        </select>
      </div>
      <div class="filter-group">
        <label class="filter-label">Tìm kiếm:</label>
        <input type="text" class="filter-input" placeholder="Tên sản phẩm...">
      </div>
      <button class="filter-btn">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
        </svg>
        Lọc
      </button>
    </div>

    <!-- Inventory Table -->
    <div class="inventory-table-section">
      <div class="table-header">
        <h3>Chi tiết tồn kho</h3>
        <div class="table-actions">
          <button class="action-btn">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12" />
            </svg>
            Nhập hàng
          </button>
          <button class="export-btn">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-4-4m4 4l4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            Xuất Excel
          </button>
        </div>
      </div>
      
      <div class="table-container">
        <table class="inventory-table">
          <thead>
            <tr>
              <th>Mã SP</th>
              <th>Tên sản phẩm</th>
              <th>Danh mục</th>
              <th>Tồn kho</th>
              <th>Tối thiểu</th>
              <th>Tối đa</th>
              <th>Giá nhập</th>
              <th>Giá bán</th>
              <th>Trạng thái</th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>SP001</td>
              <td>iPhone 15 Pro Max 256GB</td>
              <td>Điện thoại</td>
              <td class="stock-quantity">25</td>
              <td>10</td>
              <td>100</td>
              <td>27,000,000 ₫</td>
              <td>29,990,000 ₫</td>
              <td><span class="status in-stock">Đủ hàng</span></td>
              <td>
                <button class="btn-update">Cập nhật</button>
              </td>
            </tr>
            <tr>
              <td>SP002</td>
              <td>Samsung Galaxy S24 Ultra</td>
              <td>Điện thoại</td>
              <td class="stock-quantity warning">8</td>
              <td>10</td>
              <td>80</td>
              <td>25,500,000 ₫</td>
              <td>27,990,000 ₫</td>
              <td><span class="status low-stock">Sắp hết</span></td>
              <td>
                <button class="btn-update">Cập nhật</button>
              </td>
            </tr>
            <tr>
              <td>SP003</td>
              <td>MacBook Pro M3 14inch</td>
              <td>Laptop</td>
              <td class="stock-quantity danger">0</td>
              <td>5</td>
              <td>50</td>
              <td>48,000,000 ₫</td>
              <td>52,990,000 ₫</td>
              <td><span class="status out-of-stock">Hết hàng</span></td>
              <td>
                <button class="btn-update">Cập nhật</button>
              </td>
            </tr>
            <tr>
              <td>SP004</td>
              <td>AirPods Pro 2nd Gen</td>
              <td>Phụ kiện</td>
              <td class="stock-quantity">150</td>
              <td>20</td>
              <td>200</td>
              <td>5,200,000 ₫</td>
              <td>6,490,000 ₫</td>
              <td><span class="status high-stock">Tồn kho cao</span></td>
              <td>
                <button class="btn-update">Cập nhật</button>
              </td>
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
// Placeholder for inventory report page
</script>

<style scoped>
.bao-cao-ton-kho-page {
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

.alert-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.alert-card {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 1rem;
  border-left: 4px solid;
}

.alert-card.warning {
  border-left-color: #f59e0b;
}

.alert-card.danger {
  border-left-color: #ef4444;
}

.alert-card.success {
  border-left-color: #10b981;
}

.alert-card.info {
  border-left-color: #3b82f6;
}

.alert-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.alert-card.warning .alert-icon {
  background: #fef3c7;
  color: #d97706;
}

.alert-card.danger .alert-icon {
  background: #fee2e2;
  color: #dc2626;
}

.alert-card.success .alert-icon {
  background: #d1fae5;
  color: #059669;
}

.alert-card.info .alert-icon {
  background: #dbeafe;
  color: #2563eb;
}

.alert-content h3 {
  font-size: 0.875rem;
  color: #6b7280;
  margin: 0 0 0.25rem 0;
  font-weight: 500;
}

.alert-content p {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1f2937;
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
  flex-wrap: wrap;
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

.filter-select, .filter-input {
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

.inventory-table-section {
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

.table-actions {
  display: flex;
  gap: 0.75rem;
}

.action-btn {
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
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

.action-btn:hover, .export-btn:hover {
  transform: translateY(-1px);
}

.table-container {
  overflow-x: auto;
}

.inventory-table {
  width: 100%;
  border-collapse: collapse;
}

.inventory-table th {
  background: #f9fafb;
  padding: 1rem;
  text-align: left;
  font-weight: 600;
  color: #374151;
  border-bottom: 1px solid #e5e7eb;
}

.inventory-table td {
  padding: 1rem;
  border-bottom: 1px solid #e5e7eb;
  color: #1f2937;
}

.stock-quantity {
  font-weight: 600;
}

.stock-quantity.warning {
  color: #d97706;
}

.stock-quantity.danger {
  color: #dc2626;
}

.status {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 500;
}

.status.in-stock {
  background: #d1fae5;
  color: #065f46;
}

.status.low-stock {
  background: #fef3c7;
  color: #92400e;
}

.status.out-of-stock {
  background: #fee2e2;
  color: #991b1b;
}

.status.high-stock {
  background: #dbeafe;
  color: #1e40af;
}

.btn-update {
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
  padding: 0.25rem 0.75rem;
  border-radius: 6px;
  font-size: 0.75rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-update:hover {
  background: #e5e7eb;
}
</style>


