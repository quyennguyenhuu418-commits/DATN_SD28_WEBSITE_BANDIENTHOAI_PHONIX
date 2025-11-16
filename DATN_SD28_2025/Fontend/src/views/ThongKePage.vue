<template>
  <div class="thong-ke">
    <PosHeader />
    
    <!-- Main Content -->
    <main class="thong-ke-main">
      <!-- Header Section -->
      <section class="header-section">
        <div class="header-content">
          <div class="header-text">
            <h1 class="header-title">Thống Kê Chi Tiết</h1>
            <p class="header-subtitle">Phân tích sâu và báo cáo đầy đủ về hoạt động kinh doanh</p>
          </div>
          <div class="header-controls">
            <div class="time-range-selector">
              <label for="timeRange">Khoảng thời gian:</label>
              <select id="timeRange" v-model="selectedTimeRange" @change="loadData">
                <option value="7days">7 ngày qua</option>
                <option value="30days">30 ngày qua</option>
                <option value="3months">3 tháng qua</option>
                <option value="1year">1 năm qua</option>
                <option value="custom">Tùy chỉnh</option>
              </select>
            </div>
            <div v-if="selectedTimeRange === 'custom'" class="custom-date-range">
              <input type="date" v-model="startDate" @change="loadData">
              <span>đến</span>
              <input type="date" v-model="endDate" @change="loadData">
            </div>
            <button class="refresh-btn" @click="loadData" :disabled="loading">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
              </svg>
              {{ loading ? 'Đang tải...' : 'Làm mới' }}
            </button>
          </div>
        </div>
      </section>

      <!-- Overview Statistics -->
      <section class="overview-section">
        <div class="section-header">
          <h2 class="section-title">Tổng quan thống kê</h2>
          <p class="section-subtitle">Các chỉ số chính trong khoảng thời gian đã chọn</p>
        </div>
        <div v-if="loading" class="loading-state">
          <div class="loading-spinner"></div>
          <p>Đang tải dữ liệu thống kê...</p>
        </div>
        <div v-else class="overview-grid">
          <div class="stat-card revenue-card">
            <div class="stat-icon">
              <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
              </svg>
            </div>
            <div class="stat-content">
              <h3 class="stat-title">Tổng doanh thu</h3>
              <p class="stat-value">{{ formatCurrency(overview.tongDoanhThu) }}</p>
              <div class="stat-change" :class="overview.tyLeTangTruongDoanhThu >= 0 ? 'positive' : 'negative'">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
                </svg>
                {{ Math.abs(overview.tyLeTangTruongDoanhThu) }}%
              </div>
            </div>
          </div>

          <div class="stat-card orders-card">
            <div class="stat-icon">
              <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
              </svg>
            </div>
            <div class="stat-content">
              <h3 class="stat-title">Tổng đơn hàng</h3>
              <p class="stat-value">{{ overview.tongDonHang }}</p>
              <div class="stat-change positive">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
                </svg>
                {{ overview.tongDonHang }} đơn
              </div>
            </div>
          </div>

          <div class="stat-card products-card">
            <div class="stat-icon">
              <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
              </svg>
            </div>
            <div class="stat-content">
              <h3 class="stat-title">Sản phẩm bán ra</h3>
              <p class="stat-value">{{ overview.tongSanPhamBanRa }}</p>
              <div class="stat-change positive">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
                </svg>
                {{ overview.soSanPhamKhacNhau }} loại
              </div>
            </div>
          </div>

          <div class="stat-card customers-card">
            <div class="stat-icon">
              <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
              </svg>
            </div>
            <div class="stat-content">
              <h3 class="stat-title">Khách hàng mới</h3>
              <p class="stat-value">{{ overview.tongKhachHangMoi }}</p>
              <div class="stat-change positive">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
                </svg>
                {{ overview.soKhachHangKhacNhau }} khách hàng
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Charts Section -->
      <section class="charts-section">
        <div class="section-header">
          <h2 class="section-title">Biểu đồ phân tích</h2>
          <p class="section-subtitle">Xu hướng và phân tích theo thời gian</p>
        </div>
        <div class="charts-grid">
          <!-- Revenue Chart -->
          <div class="chart-card">
            <div class="chart-header">
              <h3 class="chart-title">Doanh thu theo ngày</h3>
              <div class="chart-controls">
                <button class="chart-btn" @click="exportChart('revenue')">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                  </svg>
                  Xuất
                </button>
              </div>
            </div>
            <div class="chart-content">
              <canvas ref="revenueChart" class="chart-canvas"></canvas>
            </div>
          </div>

          <!-- Products Chart -->
          <div class="chart-card">
            <div class="chart-header">
              <h3 class="chart-title">Sản phẩm bán theo ngày</h3>
              <div class="chart-controls">
                <button class="chart-btn" @click="exportChart('products')">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                  </svg>
                  Xuất
                </button>
              </div>
            </div>
            <div class="chart-content">
              <canvas ref="productsChart" class="chart-canvas"></canvas>
            </div>
          </div>

          <!-- Brand Revenue Chart -->
          <div class="chart-card">
            <div class="chart-header">
              <h3 class="chart-title">Doanh thu theo thương hiệu</h3>
              <div class="chart-controls">
                <button class="chart-btn" @click="exportChart('brand')">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                  </svg>
                  Xuất
                </button>
              </div>
            </div>
            <div class="chart-content">
              <canvas ref="brandChart" class="chart-canvas"></canvas>
            </div>
          </div>

          <!-- Category Revenue Chart -->
          <div class="chart-card">
            <div class="chart-header">
              <h3 class="chart-title">Doanh thu theo danh mục</h3>
              <div class="chart-controls">
                <button class="chart-btn" @click="exportChart('category')">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                  </svg>
                  Xuất
                </button>
              </div>
            </div>
            <div class="chart-content">
              <canvas ref="categoryChart" class="chart-canvas"></canvas>
            </div>
          </div>

          <!-- Top Customers Chart -->
          <div class="chart-card">
            <div class="chart-header">
              <h3 class="chart-title">Khách hàng chi tiêu nhiều nhất</h3>
              <div class="chart-controls">
                <button class="chart-btn" @click="exportChart('topCustomers')">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                  </svg>
                  Xuất
                </button>
              </div>
            </div>
            <div class="chart-content">
              <canvas ref="topCustomersChart" class="chart-canvas"></canvas>
            </div>
          </div>

          <!-- Top Products Chart -->
          <div class="chart-card">
            <div class="chart-header">
              <h3 class="chart-title">Sản phẩm bán chạy nhất</h3>
              <div class="chart-controls">
                <button class="chart-btn" @click="exportChart('topProducts')">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                  </svg>
                  Xuất
                </button>
              </div>
            </div>
            <div class="chart-content">
              <canvas ref="topProductsChart" class="chart-canvas"></canvas>
            </div>
          </div>
        </div>
      </section>

      <!-- Detailed Analysis -->
      <section class="analysis-section">
        <div class="section-header">
          <h2 class="section-title">Phân tích chi tiết</h2>
          <p class="section-subtitle">Dữ liệu và báo cáo chi tiết</p>
        </div>
        <div class="analysis-tabs">
          <button 
            v-for="tab in analysisTabs" 
            :key="tab.id"
            :class="['tab-btn', { active: activeTab === tab.id }]"
            @click="activeTab = tab.id"
          >
            {{ tab.name }}
          </button>
        </div>
        <div class="analysis-content">
          <!-- Top Products Tab -->
          <div v-if="activeTab === 'products'" class="tab-content">
            <div class="data-table">
              <div class="table-header">
                <h3>Sản phẩm bán chạy</h3>
                <div class="table-controls">
                  <span>Tổng: {{ topProducts.length }} sản phẩm</span>
                </div>
              </div>
              <div class="table-content">
                <div v-if="loading" class="loading-state">
                  <div class="loading-spinner"></div>
                  <p>Đang tải dữ liệu...</p>
                </div>
                <div v-else-if="topProducts.length === 0" class="empty-state">
                  <p>Chưa có dữ liệu sản phẩm</p>
                </div>
                <div v-else class="table products-table">
                  <div class="table-row header">
                    <div class="col-rank">#</div>
                    <div class="col-name">Tên sản phẩm</div>
                    <div class="col-sales">Số lượng bán</div>
                    <div class="col-revenue">Doanh thu</div>
                  </div>
                  <div v-for="(product, index) in topProducts" :key="product.productId" class="table-row">
                    <div class="col-rank">{{ index + 1 }}</div>
                    <div class="col-name">{{ product.productName }}</div>
                    <div class="col-sales">{{ product.totalSold }}</div>
                    <div class="col-revenue">{{ formatCurrency(product.totalRevenue) }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Staff Performance Tab -->
          <div v-if="activeTab === 'staff'" class="tab-content">
            <div class="data-table">
              <div class="table-header">
                <h3>Hiệu suất nhân viên</h3>
                <div class="table-controls">
                  <span>Tổng: {{ staffPerformance.length }} nhân viên</span>
                </div>
              </div>
              <div class="table-content">
                <div v-if="loading" class="loading-state">
                  <div class="loading-spinner"></div>
                  <p>Đang tải dữ liệu...</p>
                </div>
                <div v-else-if="staffPerformance.length === 0" class="empty-state">
                  <p>Chưa có dữ liệu nhân viên</p>
                </div>
                <div v-else class="table staff-table">
                  <div class="table-row header">
                    <div class="col-name">Tên nhân viên</div>
                    <div class="col-role">Chức vụ</div>
                    <div class="col-orders">Số đơn hàng</div>
                    <div class="col-revenue">Doanh thu</div>
                  </div>
                  <div v-for="staff in staffPerformance" :key="staff.id" class="table-row">
                    <div class="col-name">{{ staff.name }}</div>
                    <div class="col-role">{{ staff.role }}</div>
                    <div class="col-orders">{{ staff.orders }}</div>
                    <div class="col-revenue">{{ formatCurrency(staff.revenue) }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Customer Analysis Tab -->
          <div v-if="activeTab === 'customers'" class="tab-content">
            <div class="data-table">
              <div class="table-header">
                <h3>Khách hàng VIP</h3>
                <div class="table-controls">
                  <span>Tổng: {{ vipCustomers.length }} khách hàng</span>
                </div>
              </div>
              <div class="table-content">
                <div v-if="loading" class="loading-state">
                  <div class="loading-spinner"></div>
                  <p>Đang tải dữ liệu...</p>
                </div>
                <div v-else-if="vipCustomers.length === 0" class="empty-state">
                  <p>Chưa có dữ liệu khách hàng VIP</p>
                  </div>
                <div v-else class="table customers-table">
                  <div class="table-row header">
                    <div class="col-name">Tên khách hàng</div>
                    <div class="col-orders">Số đơn hàng</div>
                    <div class="col-total">Tổng chi tiêu</div>
                  </div>
                  <div v-for="customer in vipCustomers" :key="customer.customerId" class="table-row">
                    <div class="col-name">{{ customer.customerName }}</div>
                    <div class="col-orders">{{ customer.orderCount }}</div>
                    <div class="col-total">{{ formatCurrency(customer.totalSpent) }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Inventory Analysis Tab -->
          <div v-if="activeTab === 'inventory'" class="tab-content">
            <div class="data-table">
              <div class="table-header">
                <h3>Thống kê tồn kho</h3>
                <div class="table-controls">
                  <span>Tổng sản phẩm: {{ inventoryData.tongSanPham || 0 }}</span>
                </div>
              </div>
              <div class="table-content">
                <div v-if="loading" class="loading-state">
                  <div class="loading-spinner"></div>
                  <p>Đang tải dữ liệu...</p>
                </div>
                <div v-else class="inventory-overview">
                  <div class="inventory-cards">
                    <div class="inventory-card">
                      <div class="card-icon">
                        <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
                        </svg>
                      </div>
                      <div class="card-content">
                        <h4>Tổng sản phẩm</h4>
                        <p class="card-value">{{ inventoryData.tongSanPham || 0 }}</p>
                      </div>
                    </div>
                    <div class="inventory-card">
                      <div class="card-icon warning">
                        <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
                        </svg>
                      </div>
                      <div class="card-content">
                        <h4>Sắp hết hàng</h4>
                        <p class="card-value">{{ inventoryData.sanPhamSapHetHang || 0 }}</p>
                      </div>
                    </div>
                    <div class="inventory-card">
                      <div class="card-icon danger">
                        <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                        </svg>
                      </div>
                      <div class="card-content">
                        <h4>Hết hàng</h4>
                        <p class="card-value">{{ inventoryData.sanPhamHetHang || 0 }}</p>
                      </div>
                    </div>
                    <div class="inventory-card">
                      <div class="card-icon success">
                        <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
                        </svg>
                      </div>
                      <div class="card-content">
                        <h4>Giá trị tồn kho</h4>
                        <p class="card-value">{{ formatCurrency(inventoryData.giaTriTonKho || 0) }}</p>
                      </div>
                    </div>
                  </div>
                  
                  <div v-if="inventoryData.topSanPhamTonKho && inventoryData.topSanPhamTonKho.length > 0" class="inventory-table">
                    <h4>Top sản phẩm tồn kho nhiều nhất</h4>
                    <div class="table">
                      <div class="table-row header">
                        <div class="col-name">Tên sản phẩm</div>
                        <div class="col-quantity">Số lượng</div>
                        <div class="col-cost">Giá nhập</div>
                        <div class="col-sell">Giá bán</div>
                        <div class="col-value">Giá trị tồn kho</div>
                      </div>
                      <div v-for="(product, index) in inventoryData.topSanPhamTonKho" :key="index" class="table-row">
                        <div class="col-name">{{ product.productName }}</div>
                        <div class="col-quantity">{{ product.quantity }}</div>
                        <div class="col-cost">{{ formatCurrency(product.costPrice) }}</div>
                        <div class="col-sell">{{ formatCurrency(product.sellPrice) }}</div>
                        <div class="col-value">{{ formatCurrency(product.inventoryValue) }}</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Brand Analysis Tab -->
          <div v-if="activeTab === 'brands'" class="tab-content">
            <div class="data-table">
              <div class="table-header">
                <h3>Thống kê theo thương hiệu</h3>
                <div class="table-controls">
                  <span>Phân tích hiệu suất thương hiệu</span>
                </div>
              </div>
              <div class="table-content">
                <div v-if="loading" class="loading-state">
                  <div class="loading-spinner"></div>
                  <p>Đang tải dữ liệu...</p>
                </div>
                <div v-else-if="!brandData.doanhThuThuongHieu || brandData.doanhThuThuongHieu.length === 0" class="empty-state">
                  <p>Chưa có dữ liệu thương hiệu</p>
                </div>
                <div v-else>
                  <div v-if="brandData.thuongHieuBanChay && brandData.thuongHieuBanChay.length > 0" class="brand-table">
                    <h4>Top thương hiệu bán chạy</h4>
                    <div class="table">
                      <div class="table-row header">
                        <div class="col-name">Thương hiệu</div>
                        <div class="col-revenue">Doanh thu</div>
                        <div class="col-quantity">Số lượng</div>
                      </div>
                      <div v-for="(brand, index) in brandData.thuongHieuBanChay" :key="index" class="table-row">
                        <div class="col-name">{{ brand.brandName }}</div>
                        <div class="col-revenue">{{ formatCurrency(brand.revenue) }}</div>
                        <div class="col-quantity">{{ brand.quantity }}</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Category Analysis Tab -->
          <div v-if="activeTab === 'categories'" class="tab-content">
            <div class="data-table">
              <div class="table-header">
                <h3>Thống kê theo danh mục</h3>
                <div class="table-controls">
                  <span>Phân tích hiệu suất danh mục</span>
                </div>
              </div>
              <div class="table-content">
                <div v-if="loading" class="loading-state">
                  <div class="loading-spinner"></div>
                  <p>Đang tải dữ liệu...</p>
                </div>
                <div v-else-if="!categoryData.doanhThuDanhMuc || categoryData.doanhThuDanhMuc.length === 0" class="empty-state">
                  <p>Chưa có dữ liệu danh mục</p>
                </div>
                <div v-else>
                  <div v-if="categoryData.danhMucBanChay && categoryData.danhMucBanChay.length > 0" class="category-table">
                    <h4>Top danh mục bán chạy</h4>
                    <div class="table">
                      <div class="table-row header">
                        <div class="col-name">Danh mục</div>
                        <div class="col-revenue">Doanh thu</div>
                        <div class="col-quantity">Số lượng</div>
                      </div>
                      <div v-for="(category, index) in categoryData.danhMucBanChay" :key="index" class="table-row">
                        <div class="col-name">{{ category.categoryName }}</div>
                        <div class="col-revenue">{{ formatCurrency(category.revenue) }}</div>
                        <div class="col-quantity">{{ category.quantity }}</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Trends Analysis Tab -->
          <div v-if="activeTab === 'trends'" class="tab-content">
            <div class="data-table">
              <div class="table-header">
                <h3>Phân tích xu hướng mua hàng</h3>
                <div class="table-controls">
                  <span>Xu hướng và mùa vụ</span>
                </div>
              </div>
              <div class="table-content">
                <div v-if="loading" class="loading-state">
                  <div class="loading-spinner"></div>
                  <p>Đang tải dữ liệu...</p>
                </div>
                <div v-else class="trends-overview">
                  <div v-if="trendData.muaVu" class="trend-card">
                    <h4>Phân tích mùa vụ</h4>
                    <div class="trend-info">
                      <span class="trend-label">Tình trạng hiện tại:</span>
                      <span class="trend-value" :class="trendData.muaVu === 'Cao điểm' ? 'high' : trendData.muaVu === 'Thấp điểm' ? 'low' : 'normal'">
                        {{ trendData.muaVu }}
                      </span>
                    </div>
                  </div>
                  
                  <div v-if="trendData.xuHuongGio && trendData.xuHuongGio.length > 0" class="trend-table">
                    <h4>Xu hướng theo giờ trong ngày</h4>
                    <div class="table">
                      <div class="table-row header">
                        <div class="col-hour">Giờ</div>
                        <div class="col-revenue">Doanh thu</div>
                        <div class="col-orders">Số đơn</div>
                      </div>
                      <div v-for="(trend, index) in trendData.xuHuongGio" :key="index" class="table-row">
                        <div class="col-hour">{{ trend.hour }}:00</div>
                        <div class="col-revenue">{{ formatCurrency(trend.revenue) }}</div>
                        <div class="col-orders">{{ trend.orders }}</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Financial Analysis Tab -->
          <div v-if="activeTab === 'financial'" class="tab-content">
            <div class="data-table">
              <div class="table-header">
                <h3>Báo cáo tài chính</h3>
                <div class="table-controls">
                  <span>Phân tích lợi nhuận và chi phí</span>
                </div>
              </div>
              <div class="table-content">
                <div v-if="loading" class="loading-state">
                  <div class="loading-spinner"></div>
                  <p>Đang tải dữ liệu...</p>
                </div>
                <div v-else class="financial-overview">
                  <div class="financial-cards">
                    <div class="financial-card">
                      <div class="card-icon">
                        <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
                        </svg>
                      </div>
                      <div class="card-content">
                        <h4>Tổng doanh thu</h4>
                        <p class="card-value">{{ formatCurrency(financialData.tongDoanhThu || 0) }}</p>
                      </div>
                    </div>
                    <div class="financial-card">
                      <div class="card-icon warning">
                        <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 17h8m0 0V9m0 8l-8-8-4 4-6-6" />
                        </svg>
                      </div>
                      <div class="card-content">
                        <h4>Tổng chi phí</h4>
                        <p class="card-value">{{ formatCurrency(financialData.tongChiPhi || 0) }}</p>
                      </div>
                    </div>
                    <div class="financial-card">
                      <div class="card-icon success">
                        <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                      </div>
                      <div class="card-content">
                        <h4>Lợi nhuận</h4>
                        <p class="card-value" :class="(financialData.loiNhuan || 0) >= 0 ? 'positive' : 'negative'">
                          {{ formatCurrency(financialData.loiNhuan || 0) }}
                        </p>
                      </div>
                    </div>
                    <div class="financial-card">
                      <div class="card-icon info">
                        <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                        </svg>
                      </div>
                      <div class="card-content">
                        <h4>Tỷ lệ lợi nhuận</h4>
                        <p class="card-value">{{ (financialData.tyLeLoiNhuan || 0).toFixed(2) }}%</p>
                      </div>
                    </div>
                  </div>
                  
                  <div v-if="financialData.doanhThuTrungBinh" class="financial-summary">
                    <h4>Tóm tắt tài chính</h4>
                    <div class="summary-item">
                      <span class="summary-label">Doanh thu trung bình mỗi đơn hàng:</span>
                      <span class="summary-value">{{ formatCurrency(financialData.doanhThuTrungBinh) }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { Chart, registerables } from 'chart.js'
import * as XLSX from 'xlsx'
import PosHeader from '../components/PosHeader.vue'
import api from '../services/api'

Chart.register(...registerables)

export default {
  name: 'ThongKePage',
  components: {
    PosHeader
  },
  setup() {
    // Reactive data
    const loading = ref(false)
    const selectedTimeRange = ref('7days')
    const startDate = ref('')
    const endDate = ref('')
    const activeTab = ref('products')
    
    const overview = ref({
      tongDoanhThu: 0,
      tongDonHang: 0,
      tongSanPhamBanRa: 0,
      tongKhachHangMoi: 0,
      tyLeTangTruongDoanhThu: 0,
      soSanPhamKhacNhau: 0,
      soKhachHangKhacNhau: 0
    })
    
    const topProducts = ref([])
    const staffPerformance = ref([])
    const vipCustomers = ref([])
    
    const revenueData = ref([])
    const productsData = ref([])
    const noDataNotice = ref('')
    const didAutoExpand = ref(false)
    
    // New data for extended reports
    const inventoryData = ref({})
    const brandData = ref({})
    const categoryData = ref({})
    const trendData = ref({})
    const financialData = ref({})
    
    // Chart refs
    const revenueChart = ref(null)
    const productsChart = ref(null)
    const brandChart = ref(null)
    const categoryChart = ref(null)
    const topCustomersChart = ref(null)
    const topProductsChart = ref(null)
    const revenueChartInstance = ref(null)
    const productsChartInstance = ref(null)
    const brandChartInstance = ref(null)
    const categoryChartInstance = ref(null)
    const topCustomersChartInstance = ref(null)
    const topProductsChartInstance = ref(null)
    
    // Analysis tabs
    const analysisTabs = ref([
      { id: 'products', name: 'Sản phẩm' },
      { id: 'staff', name: 'Nhân viên' },
      { id: 'customers', name: 'Khách hàng' },
      { id: 'inventory', name: 'Tồn kho' },
      { id: 'brands', name: 'Thương hiệu' },
      { id: 'categories', name: 'Danh mục' },
      { id: 'trends', name: 'Xu hướng' },
      { id: 'financial', name: 'Tài chính' }
    ])

    // Get date range based on selection
    function getDateRange() {
      const today = new Date()
      let start, end
      
      switch (selectedTimeRange.value) {
        case '7days':
          start = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000)
          end = today
          break
        case '30days':
          start = new Date(today.getTime() - 30 * 24 * 60 * 60 * 1000)
          end = today
          break
        case '3months':
          start = new Date(today.getTime() - 90 * 24 * 60 * 60 * 1000)
          end = today
          break
        case '1year':
          start = new Date(today.getTime() - 365 * 24 * 60 * 60 * 1000)
          end = today
          break
        case 'custom':
          start = new Date(startDate.value)
          end = new Date(endDate.value)
          break
        default:
          start = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000)
          end = today
      }
      
      return {
        startDate: start.toISOString().split('T')[0],
        endDate: end.toISOString().split('T')[0]
      }
    }

    // Load statistics data
    async function loadData() {
      loading.value = true
      try {
        console.log('🔄 Loading statistics data...')
        
        // Calculate date range
        const { startDate: start, endDate: end } = getDateRange()
        
          // Load detailed statistics - sử dụng API mới
        const statsRes = await api.get('/api/thong-ke/chi-tiet', {
          params: {
            tuNgay: start,
            denNgay: end
          }
        })
        console.log('[ThongKe] /chi-tiet params:', { tuNgay: start, denNgay: end })
        console.log('[ThongKe] /chi-tiet response:', statsRes.data)
        if (statsRes.data && statsRes.data.thanhCong) {
          const data = statsRes.data.duLieu
          
          // Update overview
          overview.value = {
            tongDoanhThu: data.tongDoanhThu || 0,
            tongDonHang: data.tongDonHang || 0,
            tongSanPhamBanRa: data.tongSanPhamBanRa || 0,
            tongKhachHangMoi: data.tongKhachHangMoi || 0,
            tyLeTangTruongDoanhThu: data.tyLeTangTruongDoanhThu || 0,
            soSanPhamKhacNhau: data.soSanPhamKhacNhau || 0,
            soKhachHangKhacNhau: data.soKhachHangKhacNhau || 0
          }
          
          console.log('[ThongKe] Overview data:', overview.value)
          
          // Update detailed data
          revenueData.value = data.doanhThuTheoNgay || []
          productsData.value = data.sanPhamTheoNgay || []
          topProducts.value = data.sanPhamBanChayChiTiet || []
          staffPerformance.value = data.hieuSuatNhanVien || []
          
          // Load VIP customers separately
          await loadVipCustomers(start, end)
          
          console.log('[ThongKe] Revenue data:', revenueData.value)
          console.log('[ThongKe] Products data:', productsData.value)
          console.log('[ThongKe] Top products:', topProducts.value)
          
          // Load extended reports data
          await loadExtendedReports(start, end)
          
          // If everything is empty and we haven't auto-expanded yet, retry with 1 year
          const isAllEmpty =
            (revenueData.value?.length || 0) === 0 &&
            (productsData.value?.length || 0) === 0 &&
            (topProducts.value?.length || 0) === 0 &&
            (staffPerformance.value?.length || 0) === 0 &&
            (vipCustomers.value?.length || 0) === 0

          if (isAllEmpty && !didAutoExpand.value && selectedTimeRange.value !== '1year') {
            console.warn('[ThongKe] Empty data for range', selectedTimeRange.value, '→ auto expand to 1 year')
            didAutoExpand.value = true
            selectedTimeRange.value = '1year'
            await loadData()
            return
          }

          // Show small notice when still empty after fallback
          noDataNotice.value = isAllEmpty ? 'Không có dữ liệu trong khoảng thời gian đã chọn.' : ''

          // Update charts
        await nextTick()
        await initializeCharts()
        
        console.log('✅ Statistics data loaded successfully')
        }
        
      } catch (error) {
        console.error('❌ Error loading statistics data:', error)
        setDefaultValues()
        noDataNotice.value = 'Không thể tải dữ liệu. Vui lòng thử lại.'
      } finally {
        loading.value = false
      }
    }

    // Load VIP customers data
    async function loadVipCustomers(start, end) {
      try {
        console.log('[ThongKe] Loading VIP customers...')
        const vipRes = await api.get('/api/thong-ke/khach-hang-vip', {
          params: { 
            tuNgay: start, 
            denNgay: end,
            limit: 10
          }
        })
        console.log('[ThongKe] VIP customers response:', vipRes.data)
        
        if (vipRes.data && vipRes.data.thanhCong) {
          const data = vipRes.data.duLieu
          vipCustomers.value = data.khachHangVIP || []
          console.log('[ThongKe] VIP customers loaded:', vipCustomers.value)
        }
      } catch (error) {
        console.error('❌ Error loading VIP customers:', error)
        vipCustomers.value = []
      }
    }

    // Load extended reports data
    async function loadExtendedReports(start, end) {
      try {
        // Load inventory data
        const inventoryRes = await api.get('/api/thong-ke/ton-kho')
        if (inventoryRes.data && inventoryRes.data.thanhCong) {
          inventoryData.value = inventoryRes.data.duLieu
        }

        // Load brand data
        const brandRes = await api.get('/api/thong-ke/thuong-hieu', {
          params: { tuNgay: start, denNgay: end }
        })
        if (brandRes.data && brandRes.data.thanhCong) {
          brandData.value = brandRes.data.duLieu
        }

        // Load category data
        const categoryRes = await api.get('/api/thong-ke/danh-muc', {
          params: { tuNgay: start, denNgay: end }
        })
        if (categoryRes.data && categoryRes.data.thanhCong) {
          categoryData.value = categoryRes.data.duLieu
        }

        // Load trend data
        const trendRes = await api.get('/api/thong-ke/xu-huong', {
          params: { tuNgay: start, denNgay: end }
        })
        if (trendRes.data && trendRes.data.thanhCong) {
          trendData.value = trendRes.data.duLieu
        }

        // Load financial data
        const financialRes = await api.get('/api/thong-ke/tai-chinh', {
          params: { tuNgay: start, denNgay: end }
        })
        if (financialRes.data && financialRes.data.thanhCong) {
          financialData.value = financialRes.data.duLieu
        }

        console.log('✅ Extended reports data loaded successfully')
      } catch (error) {
        console.error('❌ Error loading extended reports:', error)
      }
    }

    // Set default values
    function setDefaultValues() {
      overview.value = {
        tongDoanhThu: 0,
        tongDonHang: 0,
        tongSanPhamBanRa: 0,
        tongKhachHangMoi: 0,
        tyLeTangTruongDoanhThu: 0,
        soSanPhamKhacNhau: 0,
        soKhachHangKhacNhau: 0
      }
      topProducts.value = []
      staffPerformance.value = []
      vipCustomers.value = []
      revenueData.value = []
      productsData.value = []
    }

    // Initialize charts
    async function initializeCharts() {
      await nextTick()
      await Promise.all([
        initializeRevenueChart(),
        initializeProductsChart(),
        initializeBrandChart(),
        initializeCategoryChart(),
        initializeTopCustomersChart(),
        initializeTopProductsChart()
      ])
    }

    // Initialize revenue chart
    async function initializeRevenueChart() {
      try {
      if (!revenueChart.value) return
      
        // Destroy existing chart
      if (revenueChartInstance.value) {
        revenueChartInstance.value.destroy()
          revenueChartInstance.value = null
      }
      
      const ctx = revenueChart.value.getContext('2d')
        const labels = revenueData.value.map(item => {
        if (item.date) {
          return new Date(item.date).toLocaleDateString('vi-VN')
        }
          return 'Không có dữ liệu'
      })
        const values = revenueData.value.map(item => Number(item.revenue) || 0)
        
        console.log('[ThongKe] Revenue chart labels:', labels)
        console.log('[ThongKe] Revenue chart values:', values)
      
      revenueChartInstance.value = new Chart(ctx, {
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
            }
          }
        })
      } catch (error) {
        console.error('❌ Error creating revenue chart:', error)
      }
    }

    // Initialize products chart
    async function initializeProductsChart() {
      try {
      if (!productsChart.value) return
      
        // Destroy existing chart
      if (productsChartInstance.value) {
        productsChartInstance.value.destroy()
          productsChartInstance.value = null
      }
      
      const ctx = productsChart.value.getContext('2d')
        const labels = productsData.value.map(item => {
        if (item.date) {
          return new Date(item.date).toLocaleDateString('vi-VN')
        }
          return 'Không có dữ liệu'
      })
        const values = productsData.value.map(item => Number(item.value) || 0)
        
        console.log('[ThongKe] Products chart labels:', labels)
        console.log('[ThongKe] Products chart values:', values)
      
      productsChartInstance.value = new Chart(ctx, {
        type: 'bar',
        data: {
          labels: labels,
          datasets: [{
            label: 'Sản phẩm bán ra',
            data: values,
              backgroundColor: 'rgba(255, 140, 66, 0.8)',
              borderColor: '#ff8c42',
              borderWidth: 2,
              borderRadius: 8,
              borderSkipped: false
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
                    return 'Sản phẩm: ' + context.parsed.y
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
                  }
                }
              }
            }
          }
        })
      } catch (error) {
        console.error('❌ Error creating products chart:', error)
      }
    }

    // Initialize top customers (bar)
    async function initializeTopCustomersChart() {
      try {
        if (!topCustomersChart.value) return
        if (topCustomersChartInstance.value) {
          topCustomersChartInstance.value.destroy()
          topCustomersChartInstance.value = null
        }
        const ctx = topCustomersChart.value.getContext('2d')
        const rows = Array.isArray(vipCustomers.value) ? vipCustomers.value : []
        const labels = rows.map(r => r.customerName || r.tenKhachHang || 'Không rõ')
        const values = rows.map(r => Number(r.totalSpent || r.tongChiTieu) || 0)
        if (labels.length === 0) return
        topCustomersChartInstance.value = new Chart(ctx, {
          type: 'bar',
          data: {
            labels,
            datasets: [{
              label: 'Tổng chi tiêu',
              data: values,
              backgroundColor: 'rgba(16, 185, 129, 0.6)',
              borderColor: '#10b981',
              borderWidth: 2,
              borderRadius: 8,
              borderSkipped: false
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: { display: false },
              tooltip: {
                backgroundColor: 'rgba(0, 0, 0, 0.8)',
                titleColor: '#ffffff',
                bodyColor: '#ffffff',
                callbacks: { label: (c) => 'Chi tiêu: ' + formatCurrency(c.parsed.y) }
              }
            },
            scales: {
              x: { grid: { display: false } },
              y: { beginAtZero: true, ticks: { callback: (v)=>formatCurrency(v) } }
            }
          }
        })
      } catch (error) {
        console.error('❌ Error creating top customers chart:', error)
      }
    }

    // Initialize top products (horizontal bar)
    async function initializeTopProductsChart() {
      try {
        if (!topProductsChart.value) return
        if (topProductsChartInstance.value) {
          topProductsChartInstance.value.destroy()
          topProductsChartInstance.value = null
        }
        const ctx = topProductsChart.value.getContext('2d')
        const rows = Array.isArray(topProducts.value) ? topProducts.value : []
        const labels = rows.map(r => r.productName || r.tenSanPham || 'Không rõ')
        const values = rows.map(r => Number(r.totalSold || r.soLuongBan) || 0)
        if (labels.length === 0) return
        topProductsChartInstance.value = new Chart(ctx, {
          type: 'bar',
          data: {
            labels,
            datasets: [{
              label: 'Số lượng bán',
              data: values,
              backgroundColor: 'rgba(239, 68, 68, 0.6)',
              borderColor: '#ef4444',
              borderWidth: 2,
              borderRadius: 8,
              borderSkipped: false
            }]
          },
          options: {
            indexAxis: 'y',
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: { display: false },
              tooltip: {
                backgroundColor: 'rgba(0, 0, 0, 0.8)',
                titleColor: '#ffffff',
                bodyColor: '#ffffff',
                callbacks: { label: (c) => 'Số lượng: ' + c.parsed.x }
              }
            },
            scales: {
              x: { beginAtZero: true },
              y: { grid: { display: false } }
            }
          }
        })
      } catch (error) {
        console.error('❌ Error creating top products chart:', error)
      }
    }

    // Initialize brand chart (bar)
    async function initializeBrandChart() {
      try {
        if (!brandChart.value) return
        if (brandChartInstance.value) {
          brandChartInstance.value.destroy()
          brandChartInstance.value = null
        }
        const ctx = brandChart.value.getContext('2d')
        let rows = Array.isArray(brandData.value?.doanhThuThuongHieu) ? brandData.value.doanhThuThuongHieu : []
        // Fallback to top brands if primary dataset is empty
        if ((!rows || rows.length === 0) && Array.isArray(brandData.value?.thuongHieuBanChay)) {
          rows = brandData.value.thuongHieuBanChay
        }
        const labels = (rows || []).map(r => r.brandName || r.tenThuongHieu || 'Không rõ')
        const values = (rows || []).map(r => Number(r.revenue || r.tongDoanhThu) || 0)
        console.log('[ThongKe] Brand chart rows:', rows)
        if (!labels.length) {
          console.warn('[ThongKe] Brand chart skipped: no data')
          return
        }
        brandChartInstance.value = new Chart(ctx, {
          type: 'bar',
          data: {
            labels,
            datasets: [{
              label: 'Doanh thu theo thương hiệu',
              data: values,
              backgroundColor: 'rgba(59, 130, 246, 0.6)',
              borderColor: '#3b82f6',
              borderWidth: 2,
              borderRadius: 8,
              borderSkipped: false
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: { display: false },
              tooltip: {
                backgroundColor: 'rgba(0, 0, 0, 0.8)',
                titleColor: '#ffffff',
                bodyColor: '#ffffff',
                callbacks: {
                  label: function(context) {
                    return 'Doanh thu: ' + formatCurrency(context.parsed.y)
                  }
                }
              }
            },
            scales: {
              x: { grid: { display: false } },
              y: {
                beginAtZero: true,
                grid: { color: 'rgba(0,0,0,0.1)' },
                ticks: { callback: (v) => formatCurrency(v) }
              }
            }
          }
        })
      } catch (error) {
        console.error('❌ Error creating brand chart:', error)
      }
    }

    // Initialize category chart (doughnut)
    async function initializeCategoryChart() {
      try {
        if (!categoryChart.value) return
        if (categoryChartInstance.value) {
          categoryChartInstance.value.destroy()
          categoryChartInstance.value = null
        }
        const ctx = categoryChart.value.getContext('2d')
        let rows = Array.isArray(categoryData.value?.doanhThuDanhMuc) ? categoryData.value.doanhThuDanhMuc : []
        // Fallback to top categories if primary dataset is empty
        if ((!rows || rows.length === 0) && Array.isArray(categoryData.value?.danhMucBanChay)) {
          rows = categoryData.value.danhMucBanChay
        }
        const labels = (rows || []).map(r => r.categoryName || r.tenDanhMuc || 'Không rõ')
        const values = (rows || []).map(r => Number(r.revenue || r.tongDoanhThu) || 0)
        console.log('[ThongKe] Category chart rows:', rows)
        if (!labels.length) {
          console.warn('[ThongKe] Category chart skipped: no data')
          return
        }
        const palette = ['#ff8c42', '#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#06b6d4', '#84cc16']
        categoryChartInstance.value = new Chart(ctx, {
          type: 'doughnut',
          data: {
            labels,
            datasets: [{
              label: 'Doanh thu theo danh mục',
              data: values,
              backgroundColor: labels.map((_, i) => palette[i % palette.length]),
              borderColor: '#ffffff',
              borderWidth: 2
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: { position: 'bottom' },
              tooltip: {
                backgroundColor: 'rgba(0, 0, 0, 0.8)',
                titleColor: '#ffffff',
                bodyColor: '#ffffff',
                callbacks: {
                  label: function(context) {
                    return context.label + ': ' + formatCurrency(context.parsed) 
                  }
                }
              }
            }
          }
        })
      } catch (error) {
        console.error('❌ Error creating category chart:', error)
      }
    }

    // Export chart
    function exportChart(type) {
      try {
        const { startDate: start, endDate: end } = getDateRange()
        let sheetName = ''
        let rows = []

        if (type === 'revenue') {
          sheetName = 'DoanhThuTheoNgay'
          rows = (revenueData.value || []).map(item => ({
            Ngay: item.date ? new Date(item.date).toLocaleDateString('vi-VN') : '',
            DoanhThu: Number(item.revenue || 0)
          }))
        } else if (type === 'products') {
          sheetName = 'SanPhamTheoNgay'
          rows = (productsData.value || []).map(item => ({
            Ngay: item.date ? new Date(item.date).toLocaleDateString('vi-VN') : '',
            SoLuong: Number(item.value || 0)
          }))
        } else if (type === 'brand') {
          sheetName = 'DoanhThuThuongHieu'
          let rowsSrc = Array.isArray(brandData.value?.doanhThuThuongHieu) ? brandData.value.doanhThuThuongHieu : []
          if ((!rowsSrc || rowsSrc.length === 0) && Array.isArray(brandData.value?.thuongHieuBanChay)) {
            rowsSrc = brandData.value.thuongHieuBanChay
          }
          rows = (rowsSrc || []).map(r => ({
            ThuongHieu: r.brandName || r.tenThuongHieu || '',
            DoanhThu: Number(r.revenue || r.tongDoanhThu || 0),
            SoLuong: r.quantity != null ? Number(r.quantity) : undefined
          }))
        } else if (type === 'category') {
          sheetName = 'DoanhThuDanhMuc'
          let rowsSrc = Array.isArray(categoryData.value?.doanhThuDanhMuc) ? categoryData.value.doanhThuDanhMuc : []
          if ((!rowsSrc || rowsSrc.length === 0) && Array.isArray(categoryData.value?.danhMucBanChay)) {
            rowsSrc = categoryData.value.danhMucBanChay
          }
          rows = (rowsSrc || []).map(r => ({
            DanhMuc: r.categoryName || r.tenDanhMuc || '',
            DoanhThu: Number(r.revenue || r.tongDoanhThu || 0),
            SoLuong: r.quantity != null ? Number(r.quantity) : undefined
          }))
        } else if (type === 'topCustomers') {
          sheetName = 'KhachHangChiTieuNhieu'
          rows = (vipCustomers.value || []).map(r => ({
            KhachHang: r.customerName || r.tenKhachHang || '',
            SoDon: Number(r.orderCount || 0),
            TongChiTieu: Number(r.totalSpent || r.tongChiTieu || 0)
          }))
        } else if (type === 'topProducts') {
          sheetName = 'SanPhamBanChay'
          rows = (topProducts.value || []).map(r => ({
            SanPham: r.productName || r.tenSanPham || '',
            SoLuongBan: Number(r.totalSold || r.soLuongBan || 0),
            DoanhThu: Number(r.totalRevenue || r.doanhThu || 0)
          }))
        }

        if (!rows || rows.length === 0) {
          console.warn('Không có dữ liệu để xuất:', type)
          return
        }

        const worksheet = XLSX.utils.json_to_sheet(rows)
        const workbook = XLSX.utils.book_new()
        XLSX.utils.book_append_sheet(workbook, worksheet, sheetName)
        const fileName = `${sheetName}_${start}_den_${end}.xlsx`
        XLSX.writeFile(workbook, fileName)
      } catch (err) {
        console.error('Xuất Excel thất bại:', err)
      }
    }

    // Helper functions
    function formatCurrency(amount) {
      if (!amount || amount === 0) return '0 ₫'
      // Convert to number if it's a string
      const numAmount = typeof amount === 'string' ? parseFloat(amount) : amount
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(numAmount)
    }

    // Lifecycle
    onMounted(async () => {
      await loadData()
    })

    onUnmounted(() => {
      if (revenueChartInstance.value) {
        revenueChartInstance.value.destroy()
      }
      if (productsChartInstance.value) {
        productsChartInstance.value.destroy()
      }
      if (brandChartInstance.value) {
        brandChartInstance.value.destroy()
      }
      if (categoryChartInstance.value) {
        categoryChartInstance.value.destroy()
      }
      if (topCustomersChartInstance.value) {
        topCustomersChartInstance.value.destroy()
      }
      if (topProductsChartInstance.value) {
        topProductsChartInstance.value.destroy()
      }
    })

    return {
      loading,
      selectedTimeRange,
      startDate,
      endDate,
      activeTab,
      overview,
      topProducts,
      staffPerformance,
      vipCustomers,
      analysisTabs,
      revenueChart,
      productsChart,
      brandChart,
      categoryChart,
      topCustomersChart,
      topProductsChart,
      loadData,
      formatCurrency,
      exportChart,
      noDataNotice,
      // New data for extended reports
      inventoryData,
      brandData,
      categoryData,
      trendData,
      financialData
    }
  }
}
</script>

<style scoped>
.thong-ke {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
}

.thong-ke-main {
  margin-top: 80px;
  padding: 2rem;
  width: 100%;
  min-height: calc(100vh - 80px);
}

/* Header Section */
.header-section {
  background: linear-gradient(135deg, #ff8c42 0%, #ffb347 100%);
  border-radius: 20px;
  padding: 3rem;
  margin-bottom: 2rem;
  box-shadow: 0 20px 40px rgba(255, 140, 66, 0.3);
  position: relative;
  overflow: hidden;
}

.header-section::before {
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

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 100%;
}

.header-text {
  flex: 1;
  color: white;
}

.header-title {
  font-size: 3rem;
  font-weight: 800;
  margin-bottom: 1rem;
  text-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.header-subtitle {
  font-size: 1.2rem;
  margin-bottom: 2rem;
  opacity: 0.9;
  line-height: 1.6;
}

.header-controls {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  align-items: flex-end;
}

.time-range-selector {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  align-items: flex-end;
}

.time-range-selector label {
  color: white;
  font-weight: 600;
  font-size: 0.875rem;
}

.time-range-selector select {
  padding: 0.75rem 1rem;
  border-radius: 8px;
  border: none;
  background: rgba(255, 255, 255, 0.9);
  color: #1f2937;
  font-weight: 600;
  min-width: 150px;
}

.custom-date-range {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: white;
  font-weight: 600;
}

.custom-date-range input {
  padding: 0.5rem;
  border-radius: 6px;
  border: none;
  background: rgba(255, 255, 255, 0.9);
  color: #1f2937;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border-radius: 12px;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  cursor: pointer;
  transition: all 0.3s ease;
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

.stat-card {
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

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.revenue-card .stat-icon {
  background: linear-gradient(135deg, #ff8c42, #ffb347);
}

.orders-card .stat-icon {
  background: linear-gradient(135deg, #ff6b35, #f7931e);
}

.products-card .stat-icon {
  background: linear-gradient(135deg, #ff8c42, #ffb347);
}

.customers-card .stat-icon {
  background: linear-gradient(135deg, #ff8c42, #ffb347);
}

.stat-content {
  flex: 1;
}

.stat-title {
  font-size: 0.875rem;
  font-weight: 600;
  color: #6b7280;
  margin-bottom: 0.5rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.stat-value {
  font-size: clamp(1.2rem, 4vw, 2rem);
  font-weight: 800;
  color: #1f2937;
  margin-bottom: 0.25rem;
  line-height: 1.1;
  word-break: break-all;
  overflow-wrap: break-word;
}

.stat-change {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.75rem;
  font-weight: 600;
  padding: 0.25rem 0.5rem;
  border-radius: 6px;
}

.stat-change.positive {
  background: #d1fae5;
  color: #065f46;
}

.stat-change.negative {
  background: #fee2e2;
  color: #dc2626;
}

/* Charts Section */
.charts-section {
  margin-bottom: 2rem;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
  gap: 2rem;
  width: 100%;
}

.chart-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.chart-header {
  padding: 1.5rem 1.5rem 0 1.5rem;
  border-bottom: 1px solid #f3f4f6;
  margin-bottom: 0;
  padding-bottom: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-title {
  font-size: 1.125rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.chart-controls {
  display: flex;
  gap: 0.5rem;
}

.chart-btn {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.5rem 0.75rem;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 600;
  background: #f3f4f6;
  color: #6b7280;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
}

.chart-btn:hover {
  background: #e5e7eb;
  color: #374151;
}

.chart-content {
  padding: 1.5rem;
  height: 400px;
}

.chart-canvas {
  width: 100% !important;
  height: 100% !important;
}

/* Analysis Section */
.analysis-section {
  margin-bottom: 2rem;
}

.analysis-tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 2rem;
  border-bottom: 1px solid #e5e7eb;
}

.tab-btn {
  padding: 0.75rem 1.5rem;
  border: none;
  background: transparent;
  color: #6b7280;
  font-weight: 600;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.3s ease;
}

.tab-btn:hover {
  color: #374151;
}

.tab-btn.active {
  color: #ff8c42;
  border-bottom-color: #ff8c42;
}

.analysis-content {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.tab-content {
  padding: 0;
}

.data-table {
  width: 100%;
}

.table-header {
  padding: 1.5rem;
  border-bottom: 1px solid #f3f4f6;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-header h3 {
  font-size: 1.125rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.table-controls {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.table-controls span {
  font-size: 0.875rem;
  color: #6b7280;
  font-weight: 600;
}

.table-content {
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

/* Table Styles */
.table {
  width: 100%;
  border-collapse: collapse;
}

.table-row {
  display: grid;
  grid-template-columns: 60px 1fr 120px 150px;
  gap: 1rem;
  padding: 1rem 0;
  border-bottom: 1px solid #f3f4f6;
  align-items: center;
}

.table-row.header {
  font-weight: 700;
  color: #374151;
  background: #f9fafb;
  border-radius: 8px;
  padding: 0.75rem 1rem;
  margin-bottom: 0.5rem;
}

.table-row:last-child {
  border-bottom: none;
}

.col-rank {
  text-align: center;
  font-weight: 700;
  color: #ff8c42;
}

.col-name {
  font-weight: 600;
  color: #1f2937;
}

.col-sales, .col-revenue, .col-orders, .col-total {
  text-align: right;
  font-weight: 600;
  color: #374151;
}

.col-role {
  color: #6b7280;
  font-size: 0.875rem;
}

/* Staff Table */
.staff-table .table-row {
  grid-template-columns: 1fr 120px 100px 150px;
}

/* Customers Table */
.customers-table .table-row {
  grid-template-columns: 1fr 100px 150px;
}

/* New Extended Reports Styles */

/* Inventory Cards */
.inventory-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.inventory-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: all 0.3s ease;
}

.inventory-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.inventory-card .card-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.inventory-card .card-icon.warning {
  background: linear-gradient(135deg, #f59e0b, #fbbf24);
}

.inventory-card .card-icon.danger {
  background: linear-gradient(135deg, #ef4444, #f87171);
}

.inventory-card .card-icon.success {
  background: linear-gradient(135deg, #10b981, #34d399);
}

.inventory-card .card-content h4 {
  font-size: 0.875rem;
  font-weight: 600;
  color: #6b7280;
  margin: 0 0 0.5rem 0;
}

.inventory-card .card-value {
  font-size: 1.5rem;
  font-weight: 800;
  color: #1f2937;
  margin: 0;
}

/* Inventory Table */
.inventory-table {
  margin-top: 2rem;
}

.inventory-table h4 {
  font-size: 1.125rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 1rem;
}

.inventory-table .table-row {
  grid-template-columns: 1fr 100px 120px 120px 150px;
}

/* Financial Cards */
.financial-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.financial-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: all 0.3s ease;
}

.financial-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.financial-card .card-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.financial-card .card-icon.warning {
  background: linear-gradient(135deg, #f59e0b, #fbbf24);
}

.financial-card .card-icon.success {
  background: linear-gradient(135deg, #10b981, #34d399);
}

.financial-card .card-icon.info {
  background: linear-gradient(135deg, #3b82f6, #60a5fa);
}

.financial-card .card-content h4 {
  font-size: 0.875rem;
  font-weight: 600;
  color: #6b7280;
  margin: 0 0 0.5rem 0;
}

.financial-card .card-value {
  font-size: 1.5rem;
  font-weight: 800;
  color: #1f2937;
  margin: 0;
}

.financial-card .card-value.positive {
  color: #10b981;
}

.financial-card .card-value.negative {
  color: #ef4444;
}

/* Financial Summary */
.financial-summary {
  background: #f9fafb;
  border-radius: 12px;
  padding: 1.5rem;
  margin-top: 1rem;
}

.financial-summary h4 {
  font-size: 1.125rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 1rem;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 0;
  border-bottom: 1px solid #e5e7eb;
}

.summary-item:last-child {
  border-bottom: none;
}

.summary-label {
  font-weight: 600;
  color: #6b7280;
}

.summary-value {
  font-weight: 700;
  color: #1f2937;
}

/* Trend Cards */
.trend-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  margin-bottom: 2rem;
}

.trend-card h4 {
  font-size: 1.125rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 1rem;
}

.trend-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.trend-label {
  font-weight: 600;
  color: #6b7280;
}

.trend-value {
  padding: 0.5rem 1rem;
  border-radius: 8px;
  font-weight: 700;
  font-size: 0.875rem;
}

.trend-value.high {
  background: #d1fae5;
  color: #065f46;
}

.trend-value.low {
  background: #fee2e2;
  color: #dc2626;
}

.trend-value.normal {
  background: #e5e7eb;
  color: #374151;
}

/* Brand and Category Tables */
.brand-table, .category-table {
  margin-top: 1rem;
}

.brand-table h4, .category-table h4 {
  font-size: 1.125rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 1rem;
}

.brand-table .table-row, .category-table .table-row {
  grid-template-columns: 1fr 150px 100px;
}

/* Trend Table */
.trend-table {
  margin-top: 1rem;
}

.trend-table h4 {
  font-size: 1.125rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 1rem;
}

.trend-table .table-row {
  grid-template-columns: 80px 150px 100px;
}

/* Additional column styles */
.col-quantity, .col-cost, .col-sell, .col-value, .col-hour {
  text-align: right;
  font-weight: 600;
  color: #374151;
}

.col-quantity, .col-cost, .col-sell, .col-value {
  font-size: 0.875rem;
}

.col-hour {
  font-weight: 700;
  color: #ff8c42;
}

/* Responsive Design */
@media (max-width: 768px) {
  .thong-ke-main {
    padding: 1rem;
  }
  
  .header-section {
    padding: 2rem;
  }
  
  .header-content {
    flex-direction: column;
    text-align: center;
  }
  
  .header-controls {
    align-items: center;
    margin-top: 2rem;
  }
  
  .header-title {
    font-size: 2rem;
  }
  
  .overview-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .stat-value {
    font-size: 1.5rem;
  }
  
  .charts-grid {
    grid-template-columns: 1fr;
  }
  
  .analysis-tabs {
    flex-wrap: wrap;
  }
  
  .table-row {
    grid-template-columns: 1fr;
    gap: 0.5rem;
    text-align: left;
  }
  
  .col-sales, .col-revenue, .col-orders, .col-total {
    text-align: left;
  }
}

@media (max-width: 480px) {
  .header-controls {
    width: 100%;
  }
  
  .time-range-selector {
    width: 100%;
    align-items: center;
  }
  
  .time-range-selector select {
    width: 100%;
  }
  
  .custom-date-range {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .refresh-btn {
    width: 100%;
    justify-content: center;
  }

  .overview-grid {
    grid-template-columns: 1fr;
    gap: 0.75rem;
  }

  .stat-value {
    font-size: 1.2rem;
  }

  .stat-card {
    padding: 1rem;
  }
}

</style>