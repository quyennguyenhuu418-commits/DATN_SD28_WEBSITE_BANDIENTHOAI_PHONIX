<template>
  <div class="dark-mode-demo dark-mode-transition">
    <div class="demo-header">
      <h1>🌓 Dark Mode Demo</h1>
      <p>Thử nghiệm chuyển đổi giữa Light Mode và Dark Mode</p>
    </div>

    <!-- Demo Cards -->
    <div class="demo-grid">
      <!-- Card 1: Basic Card -->
      <div class="demo-card">
        <div class="card-header">
          <h3>📊 Thống kê</h3>
          <span class="badge success">Hoạt động</span>
        </div>
        <div class="card-content">
          <div class="stat-item">
            <span class="stat-label">Doanh thu hôm nay:</span>
            <span class="stat-value">25,480,000 ₫</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">Đơn hàng:</span>
            <span class="stat-value">147</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">Khách hàng mới:</span>
            <span class="stat-value">23</span>
          </div>
        </div>
      </div>

      <!-- Card 2: Form Card -->
      <div class="demo-card">
        <div class="card-header">
          <h3>📝 Form Demo</h3>
        </div>
        <div class="card-content">
          <div class="form-group">
            <label>Tên sản phẩm:</label>
            <input type="text" placeholder="Nhập tên sản phẩm..." class="form-input">
          </div>
          <div class="form-group">
            <label>Danh mục:</label>
            <select class="form-select">
              <option>Điện thoại</option>
              <option>Laptop</option>
              <option>Phụ kiện</option>
            </select>
          </div>
          <div class="form-group">
            <label>Mô tả:</label>
            <textarea placeholder="Nhập mô tả..." class="form-textarea"></textarea>
          </div>
          <div class="form-actions">
            <button class="btn btn-primary">Lưu</button>
            <button class="btn btn-secondary">Hủy</button>
          </div>
        </div>
      </div>

      <!-- Card 3: Table Card -->
      <div class="demo-card full-width">
        <div class="card-header">
          <h3>📋 Bảng dữ liệu</h3>
          <button class="btn btn-success">Thêm mới</button>
        </div>
        <div class="card-content">
          <div class="table-container">
            <table class="demo-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Tên sản phẩm</th>
                  <th>Giá</th>
                  <th>Trạng thái</th>
                  <th>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>1</td>
                  <td>iPhone 15 Pro Max</td>
                  <td>29,990,000 ₫</td>
                  <td><span class="badge success">Còn hàng</span></td>
                  <td>
                    <button class="btn btn-sm btn-warning">Sửa</button>
                    <button class="btn btn-sm btn-danger">Xóa</button>
                  </td>
                </tr>
                <tr>
                  <td>2</td>
                  <td>Samsung Galaxy S24</td>
                  <td>22,990,000 ₫</td>
                  <td><span class="badge warning">Sắp hết</span></td>
                  <td>
                    <button class="btn btn-sm btn-warning">Sửa</button>
                    <button class="btn btn-sm btn-danger">Xóa</button>
                  </td>
                </tr>
                <tr>
                  <td>3</td>
                  <td>MacBook Pro M3</td>
                  <td>52,990,000 ₫</td>
                  <td><span class="badge danger">Hết hàng</span></td>
                  <td>
                    <button class="btn btn-sm btn-warning">Sửa</button>
                    <button class="btn btn-sm btn-danger">Xóa</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- Card 4: Chart Placeholder -->
      <div class="demo-card">
        <div class="card-header">
          <h3>📈 Biểu đồ</h3>
        </div>
        <div class="card-content">
          <div class="chart-placeholder">
            <div class="chart-bars">
              <div class="bar" style="height: 60%"></div>
              <div class="bar" style="height: 80%"></div>
              <div class="bar" style="height: 45%"></div>
              <div class="bar" style="height: 90%"></div>
              <div class="bar" style="height: 70%"></div>
            </div>
            <p>Doanh thu 5 ngày qua</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Instructions -->
    <div class="instructions">
      <h3>🎯 Hướng dẫn sử dụng Dark Mode:</h3>
      <ul>
        <li>🌙 Click vào nút <strong>Dark Mode Toggle</strong> ở góc trên bên phải để chuyển đổi</li>
        <li>🔄 Hệ thống sẽ tự động lưu lựa chọn của bạn</li>
        <li>🖥️ Nếu chưa có setting, sẽ theo system preference</li>
        <li>✨ Tất cả components đều hỗ trợ dark mode</li>
      </ul>
    </div>
  </div>
</template>

<script setup>
// Demo page for dark mode
</script>

<style scoped>
.dark-mode-demo {
  padding: 2rem;
  min-height: 100vh;
  background: var(--bg-primary);
}

.demo-header {
  text-align: center;
  margin-bottom: 3rem;
}

.demo-header h1 {
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 0.5rem 0;
}

.demo-header p {
  color: var(--text-secondary);
  font-size: 1.1rem;
}

.demo-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 2rem;
  margin-bottom: 3rem;
}

.demo-card {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px var(--shadow-light);
  overflow: hidden;
  transition: all 0.3s ease;
}

.demo-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px var(--shadow-medium);
}

.demo-card.full-width {
  grid-column: 1 / -1;
}

.card-header {
  padding: 1.5rem;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--bg-tertiary);
}

.card-header h3 {
  margin: 0;
  color: var(--text-primary);
  font-size: 1.25rem;
  font-weight: 600;
}

.card-content {
  padding: 1.5rem;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 0;
  border-bottom: 1px solid var(--border-light);
}

.stat-item:last-child {
  border-bottom: none;
}

.stat-label {
  color: var(--text-secondary);
  font-size: 0.875rem;
}

.stat-value {
  color: var(--text-primary);
  font-weight: 600;
  font-size: 1rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: var(--text-primary);
  font-weight: 500;
}

.form-input, .form-select, .form-textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--input-border);
  border-radius: 8px;
  background: var(--input-bg);
  color: var(--text-primary);
  font-size: 0.875rem;
  transition: all 0.3s ease;
}

.form-input:focus, .form-select:focus, .form-textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-textarea {
  min-height: 80px;
  resize: vertical;
}

.form-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1.5rem;
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.875rem;
}

.btn:hover {
  transform: translateY(-1px);
}

.btn-primary {
  background: var(--button-primary);
  color: white;
}

.btn-secondary {
  background: var(--bg-tertiary);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
}

.btn-success {
  background: var(--button-success);
  color: white;
}

.btn-warning {
  background: var(--button-warning);
  color: white;
}

.btn-danger {
  background: var(--button-danger);
  color: white;
}

.btn-sm {
  padding: 0.5rem 1rem;
  font-size: 0.75rem;
}

.table-container {
  overflow-x: auto;
}

.demo-table {
  width: 100%;
  border-collapse: collapse;
}

.demo-table th {
  background: var(--bg-tertiary);
  padding: 1rem;
  text-align: left;
  font-weight: 600;
  color: var(--text-primary);
  border-bottom: 1px solid var(--border-color);
}

.demo-table td {
  padding: 1rem;
  border-bottom: 1px solid var(--border-light);
  color: var(--text-primary);
}

.demo-table tr:hover {
  background: var(--bg-tertiary);
}

.badge {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 500;
}

.badge.success {
  background: #d1fae5;
  color: #065f46;
}

.badge.warning {
  background: #fef3c7;
  color: #92400e;
}

.badge.danger {
  background: #fee2e2;
  color: #991b1b;
}

:root.dark .badge.success {
  background: #064e3b;
  color: #6ee7b7;
}

:root.dark .badge.warning {
  background: #78350f;
  color: #fbbf24;
}

:root.dark .badge.danger {
  background: #7f1d1d;
  color: #fca5a5;
}

.chart-placeholder {
  text-align: center;
  padding: 2rem;
}

.chart-bars {
  display: flex;
  justify-content: center;
  align-items: end;
  gap: 0.5rem;
  height: 120px;
  margin-bottom: 1rem;
}

.bar {
  width: 20px;
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  border-radius: 4px 4px 0 0;
  transition: all 0.3s ease;
}

.bar:hover {
  transform: scaleY(1.1);
}

.chart-placeholder p {
  color: var(--text-secondary);
  font-size: 0.875rem;
}

.instructions {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 4px 6px -1px var(--shadow-light);
}

.instructions h3 {
  color: var(--text-primary);
  margin: 0 0 1rem 0;
  font-size: 1.25rem;
}

.instructions ul {
  margin: 0;
  padding-left: 1.5rem;
}

.instructions li {
  color: var(--text-secondary);
  margin-bottom: 0.5rem;
  line-height: 1.6;
}

.instructions strong {
  color: var(--text-primary);
}

@media (max-width: 768px) {
  .demo-grid {
    grid-template-columns: 1fr;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .card-header {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }
}
</style>
















