<template>
  <div class="detail-page">
    <div class="detail-header">
      <div class="header-left">
        <button class="btn-back" @click="goBack">
          <FontAwesomeIcon :icon="['fas', 'arrow-left']" />
          Quay lại
        </button>
        <h1>Chi tiết Đợt Giảm Giá</h1>
      </div>
    </div>

    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p>Đang tải thông tin...</p>
    </div>

    <div v-else-if="error" class="error-container">
      <FontAwesomeIcon :icon="['fas', 'exclamation-triangle']" />
      <h3>Không thể tải thông tin</h3>
      <p>{{ error }}</p>
      <button class="btn-retry" @click="loadPromotion">
        Thử lại
      </button>
    </div>

    <div v-else-if="promotion" class="detail-container">
      <!-- Thông tin cơ bản -->
      <div class="info-section">
        <div class="section-header">
          <h2>Thông tin cơ bản</h2>
          <div class="status-badge" :class="getStatusBadgeClass(promotion)">
            {{ getStatusText(promotion) }}
          </div>
        </div>

        <div class="info-grid">
          <div class="info-item">
            <label>Mã đợt giảm giá:</label>
            <span class="info-value">{{ promotion.maKhuyenMai || 'Không có' }}</span>
          </div>

          <div class="info-item">
            <label>Tên đợt giảm giá:</label>
            <span class="info-value">{{ promotion.tenKhuyenMai }}</span>
          </div>

          <div class="info-item full-width">
            <label>Mô tả:</label>
            <span class="info-value">{{ promotion.moTa || 'Không có mô tả' }}</span>
          </div>

          <div class="info-item">
            <label>Phần trăm giảm:</label>
            <span class="info-value discount-value">{{ promotion.phanTramGiam }}%</span>
          </div>
        </div>
      </div>

      <!-- Thời gian áp dụng -->
      <div class="info-section">
        <div class="section-header">
          <h2>Thời gian áp dụng</h2>
        </div>

        <div class="info-grid">
          <div class="info-item">
            <label>Ngày bắt đầu:</label>
            <span class="info-value">{{ formatDate(promotion.ngayBatDau) }}</span>
          </div>

          <div class="info-item">
            <label>Ngày kết thúc:</label>
            <span class="info-value">{{ formatDate(promotion.ngayKetThuc) }}</span>
          </div>

          <div class="info-item">
            <label>Thời gian hiệu lực:</label>
            <span class="info-value">{{ getDuration(promotion) }}</span>
          </div>
        </div>
      </div>

      <!-- Thông tin hệ thống -->
      <div class="info-section">
        <div class="section-header">
          <h2>Thông tin hệ thống</h2>
        </div>

        <div class="info-grid">
          <div class="info-item">
            <label>Ngày tạo:</label>
            <span class="info-value">{{ promotion.ngayTao ? formatDateTime(promotion.ngayTao) : 'Không có' }}</span>
          </div>

          <div class="info-item">
            <label>Ngày cập nhật:</label>
            <span class="info-value">{{ promotion.ngayCapNhat ? formatDateTime(promotion.ngayCapNhat) : 'Không có' }}</span>
          </div>

          <div class="info-item">
            <label>Người tạo:</label>
            <span class="info-value">{{ promotion.nguoiTao || 'Không có' }}</span>
          </div>

          <div class="info-item">
            <label>Người cập nhật:</label>
            <span class="info-value">{{ promotion.nguoiCapNhat || 'Không có' }}</span>
          </div>
        </div>
      </div>

      <!-- Thống kê -->
      <div class="info-section">
        <div class="section-header">
          <h2>Thống kê</h2>
        </div>

        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon">
              <FontAwesomeIcon :icon="['fas', 'calendar-days']" />
            </div>
            <div class="stat-content">
              <h3>{{ getDaysRemaining(promotion) }}</h3>
              <p>Ngày còn lại</p>
            </div>
          </div>

          <div class="stat-card">
            <div class="stat-icon">
              <FontAwesomeIcon :icon="['fas', 'percent']" />
            </div>
            <div class="stat-content">
              <h3>{{ promotion.phanTramGiam }}%</h3>
              <p>Mức giảm giá</p>
            </div>
          </div>

          <div class="stat-card">
            <div class="stat-icon">
              <FontAwesomeIcon :icon="['fas', 'bolt']" />
            </div>
            <div class="stat-content">
              <h3>{{ getStatusText(promotion) }}</h3>
              <p>Trạng thái</p>
            </div>
          </div>

          <div class="stat-card">
            <div class="stat-icon">
              <FontAwesomeIcon :icon="['fas', 'box']" />
            </div>
            <div class="stat-content">
              <h3>{{ appliedProducts.length }}</h3>
              <p>Sản phẩm áp dụng</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Danh sách sản phẩm áp dụng -->
      <div class="info-section">
        <div class="section-header">
          <h2>Sản Phẩm Áp Dụng</h2>
        </div>

        <div v-if="loadingProducts" class="loading-container">
          <div class="spinner"></div>
          <p>Đang tải danh sách sản phẩm...</p>
        </div>

        <div v-else-if="appliedProducts.length === 0" class="no-products">
          <FontAwesomeIcon :icon="['fas', 'box']" />
          <p>Chưa có sản phẩm nào được áp dụng cho đợt giảm giá này.</p>
          <p class="hint">Áp dụng cho tất cả sản phẩm trong hệ thống.</p>
        </div>

        <div v-else class="products-table-container">
          <table class="products-table">
            <thead>
            <tr>
              <th>#</th>
              <th>Mã SP</th>
              <th>Tên Sản Phẩm</th>
              <th>Hãng</th>
              <th>Danh Mục</th>
              <th>Giá</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="(product, index) in appliedProducts" :key="product.chiTietSanPhamId">
              <td>{{ index + 1 }}</td>
              <td><span class="product-code">{{ product.maCtsp }}</span></td>
              <td class="product-name">{{ product.tenSanPham }}</td>
              <td>{{ product.tenHang || 'N/A' }}</td>
              <td>{{ product.tenDanhMuc || 'N/A' }}</td>
              <td>{{ formatCurrency(product.gia) }}</td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Actions -->
      <div class="actions-section">
        <button class="btn-edit" @click="editPromotion">
          <FontAwesomeIcon :icon="['fas', 'edit']" />
          Chỉnh sửa
        </button>
      </div>
    </div>

    <Toast ref="toastRef" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '@/services/api'
import Toast from '@/components/Toast.vue'
import { FontAwesomeIcon } from '@/plugins/fontawesome'

const router = useRouter()
const route = useRoute()
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

const loading = ref(false)
const error = ref('')
const promotion = ref<any>(null)
const appliedProducts = ref<any[]>([])
const loadingProducts = ref(false)

const promotionId = ref<number | null>(null)

onMounted(async () => {
  const id = route.params.id
  if (id && !isNaN(Number(id))) {
    promotionId.value = Number(id)
    await loadPromotion()
  } else {
    error.value = 'ID đợt giảm giá không hợp lệ'
  }
})

async function loadPromotion() {
  if (!promotionId.value) return

  loading.value = true
  error.value = ''

  try {
    const { data } = await api.get(`/api/khuyen-mai/${promotionId.value}`)
    promotion.value = data

    // Load danh sách sản phẩm áp dụng
    await loadAppliedProducts()
  } catch (err: any) {
    console.error('Lỗi khi tải chi tiết đợt giảm giá:', err)
    error.value = 'Không thể tải thông tin đợt giảm giá'
    toastRef.value?.error('Lỗi', 'Không thể tải thông tin đợt giảm giá')
  } finally {
    loading.value = false
  }
}

async function loadAppliedProducts() {
  if (!promotionId.value) return

  loadingProducts.value = true
  try {
    console.log('🔍 Loading applied products for promotion:', promotionId.value)
    
    // Load danh sách sản phẩm áp dụng từ API khuyến mãi
    const { data: appliedProductIds } = await api.get(`/api/khuyen-mai/${promotionId.value}/san-pham`)
    console.log('📋 Applied product IDs from API:', appliedProductIds)

    // Load danh sách sản phẩm đầy đủ từ API sản phẩm
    const { data: allProducts } = await api.get('/api/san-pham-pos')
    console.log('📦 All products from API:', allProducts.length, 'items')

    // Map sang danh sách sản phẩm đầy đủ
    const ids = appliedProductIds.map((item: any) => item.idSanPham)
    console.log('🆔 Mapped IDs:', ids)
    
    appliedProducts.value = allProducts.filter((p: any) => ids.includes(p.chiTietSanPhamId))
    console.log('✅ Final applied products:', appliedProducts.value.length, 'items')
    console.log('📋 Applied products details:', appliedProducts.value)
  } catch (err: any) {
    console.error('❌ Lỗi khi tải danh sách sản phẩm:', err)
    // Không hiển thị lỗi vì đây không phải thông tin quan trọng
  } finally {
    loadingProducts.value = false
  }
}


function editPromotion() {
  router.push(`/dot-giam-gia/form/${promotionId.value}`)
}

function goBack() {
  router.push('/dot-giam-gia')
}

function formatDate(dateString: string): string {
  if (!dateString) return 'Không có'
  return new Date(dateString).toLocaleDateString('vi-VN')
}

function formatDateTime(dateString: string): string {
  if (!dateString) return 'Không có'
  return new Date(dateString).toLocaleString('vi-VN')
}

function getStatusText(item: any): string {
  const now = new Date()
  const startDate = new Date(item.ngayBatDau)
  const endDate = new Date(item.ngayKetThuc)

  if (item.trangThai === 0) return 'Vô hiệu'
  if (now < startDate) return 'Chưa bắt đầu'
  if (now > endDate) return 'Vô hiệu'
  return 'Đang hoạt động'
}

function getStatusBadgeClass(item: any): string {
  const now = new Date()
  const startDate = new Date(item.ngayBatDau)
  const endDate = new Date(item.ngayKetThuc)

  if (item.trangThai === 0) return 'badge-inactive'
  if (now < startDate) return 'badge-pending'
  if (now > endDate) return 'badge-inactive'
  return 'badge-active'
}

function getDuration(item: any): string {
  const startDate = new Date(item.ngayBatDau)
  const endDate = new Date(item.ngayKetThuc)
  const diffTime = Math.abs(endDate.getTime() - startDate.getTime())
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))

  if (diffDays === 1) return '1 ngày'
  return `${diffDays} ngày`
}

function getDaysRemaining(item: any): string {
  const now = new Date()
  const endDate = new Date(item.ngayKetThuc)

  if (now > endDate) return '0'

  const diffTime = endDate.getTime() - now.getTime()
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))

  return diffDays.toString()
}

function formatCurrency(amount: number | null | undefined): string {
  if (!amount || isNaN(amount)) {
    return '0 ₫'
  }
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  }).format(amount)
}
</script>

<style scoped>
.detail-page {
  padding: 20px;
  width: 100%;
  min-height: 100vh;
  background: #f8fafc;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #e9ecef;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-left h1 {
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
  letter-spacing: -0.5px;
}

.btn-back {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #6c757d;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.btn-back:hover {
  background: #545b62;
}

/* FontAwesome icon styling */
.btn-back svg,
.btn-edit svg {
  margin-right: 8px;
}

.stat-icon svg,
.no-products svg,
.error-container svg {
  color: inherit;
  font-size: inherit;
}

.error-container svg {
  font-size: 48px;
  display: block;
  margin-bottom: 20px;
}

.no-products svg {
  font-size: 48px;
  display: block;
  margin-bottom: 16px;
}

.loading-container,
.error-container {
  text-align: center;
  padding: 60px 20px;
}

.spinner {
  width: 40px;
  height: 40px;
  margin: 0 auto 20px;
  border: 4px solid #f1f5f9;
  border-top: 4px solid #f97316;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-container h3 {
  color: #dc3545;
  margin-bottom: 10px;
}

.btn-retry {
  margin-top: 20px;
  padding: 10px 20px;
  background: #f97316;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
}

.detail-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  width: 100%;
  max-width: 100%;
}

.info-section {
  padding: 30px;
  border-bottom: 1px solid #e9ecef;
}

.info-section:last-of-type {
  border-bottom: none;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}

.section-header h2 {
  color: #495057;
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.status-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.badge-active {
  background: #d4edda;
  color: #155724;
}

.badge-pending {
  background: #fff3cd;
  color: #856404;
}

.badge-inactive {
  background: #f8d7da;
  color: #721c24;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  width: 100%;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-item label {
  font-weight: 600;
  color: #6c757d;
  font-size: 14px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-value {
  color: #495057;
  font-size: 16px;
  font-weight: 500;
}

.info-value.discount-value {
  color: #f97316;
  font-size: 20px;
  font-weight: 700;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  width: 100%;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.stat-icon {
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f97316;
  color: white;
  border-radius: 8px;
  font-size: 20px;
}

.stat-content h3 {
  margin: 0 0 5px 0;
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
}

.stat-content p {
  margin: 0;
  color: #6c757d;
  font-size: 14px;
}

.actions-section {
  padding: 30px;
  display: flex;
  gap: 15px;
  justify-content: flex-end;
  border-top: 1px solid #e9ecef;
}

.btn-edit {
  padding: 12px 24px;
  background: #f97316;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(249, 115, 22, 0.3);
}

.btn-edit:hover {
  background: #ea580c;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.4);
}



/* No Products */
.no-products {
  text-align: center;
  padding: 60px 20px;
  background: #f8fafc;
  border: 2px dashed #cbd5e1;
  border-radius: 8px;
  color: #64748b;
}

.no-products p {
  margin: 8px 0;
  font-size: 14px;
}

.no-products .hint {
  font-size: 13px;
  color: #94a3b8;
  font-style: italic;
}

/* Products Table */
.products-table-container {
  overflow-x: auto;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.products-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
}

.products-table thead {
  background: #f8f9fa;
}

.products-table th {
  padding: 12px 16px;
  text-align: left;
  font-weight: 600;
  font-size: 13px;
  color: #495057;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 2px solid #e9ecef;
}

.products-table tbody tr {
  border-bottom: 1px solid #f1f5f9;
  transition: background-color 0.2s;
}

.products-table tbody tr:hover {
  background: #fef3c7;
}

.products-table td {
  padding: 12px 16px;
  font-size: 14px;
  color: #495057;
}

.product-code {
  display: inline-block;
  padding: 4px 8px;
  background: #e9ecef;
  color: #495057;
  border-radius: 4px;
  font-family: monospace;
  font-size: 12px;
  font-weight: 600;
}

.product-name {
  font-weight: 500;
  color: #2c3e50;
}

/* Large screens */
@media (min-width: 1920px) {
  .detail-page {
    padding: 40px;
  }

  .info-grid {
    grid-template-columns: repeat(4, 1fr);
  }

  .stats-grid {
    grid-template-columns: repeat(4, 1fr);
  }

  .info-section {
    padding: 40px;
  }
}

@media (min-width: 1440px) {
  .detail-page {
    padding: 30px;
  }

  .info-grid {
    grid-template-columns: repeat(3, 1fr);
  }

  .stats-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (min-width: 1200px) {
  .info-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .stats-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 768px) {
  .detail-page {
    padding: 15px;
  }

  .header-left {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .actions-section {
    flex-direction: column;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>
