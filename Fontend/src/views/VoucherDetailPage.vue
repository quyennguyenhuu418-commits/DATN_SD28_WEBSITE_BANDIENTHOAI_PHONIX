<template>
  <div class="voucher-detail">
    <PosHeader />
    
    <div class="detail-container">
      <div class="detail-header">
        <div class="header-left">
          <button class="btn-back" @click="goBack">
            <font-awesome-icon icon="arrow-left" />
            Quay lại
          </button>
          <h1>Chi tiết Voucher</h1>
        </div>
        <div class="header-actions">
          <button class="btn-edit" @click="editVoucher">
            <font-awesome-icon icon="edit" />
            Chỉnh sửa
          </button>
          <button class="btn-delete" @click="deleteVoucher" v-if="voucher">
            <font-awesome-icon icon="trash" />
            Xóa
          </button>
        </div>
      </div>

      <div class="detail-content" v-if="voucher">
        <div class="detail-card">
          <div class="card-header">
            <h2>Thông tin cơ bản</h2>
          </div>
          <div class="card-body">
            <div class="info-grid">
              <div class="info-item">
                <label>Mã voucher:</label>
                <span class="info-value">{{ voucher.maPhieuGiamGia }}</span>
              </div>
              <div class="info-item">
                <label>Tên voucher:</label>
                <span class="info-value">{{ voucher.tenPhieuGiamGia }}</span>
              </div>
              <div class="info-item">
                <label>Loại phiếu giảm giá:</label>
                <span class="info-value">{{ voucher.loaiPhieuGiamGia || 'Không xác định' }}</span>
              </div>
              <div class="info-item">
                <label>Giá trị giảm giá:</label>
                <span class="info-value">{{ formatCurrency(voucher.giaTriGiamGia) }}</span>
              </div>
              <div class="info-item">
                <label>Số tiền giảm tối đa:</label>
                <span class="info-value">{{ formatCurrency(voucher.soTienGiamToiDa) }}</span>
              </div>
              <div class="info-item">
                <label>Hóa đơn tối thiểu:</label>
                <span class="info-value">{{ formatCurrency(voucher.hoaDonToiThieu) }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="detail-card">
          <div class="card-header">
            <h2>Thông tin thời gian</h2>
          </div>
          <div class="card-body">
            <div class="info-grid">
              <div class="info-item">
                <label>Ngày bắt đầu:</label>
                <span class="info-value">{{ formatDate(voucher.ngayBatDau) }}</span>
              </div>
              <div class="info-item">
                <label>Ngày kết thúc:</label>
                <span class="info-value">{{ formatDate(voucher.ngayKetThuc) }}</span>
              </div>
              <div class="info-item">
                <label>Riêng tư:</label>
                <span class="info-value">{{ voucher.riengTu ? 'Có' : 'Không' }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="detail-card">
          <div class="card-header">
            <h2>Thông tin sử dụng</h2>
          </div>
          <div class="card-body">
            <div class="info-grid">
              <div class="info-item">
                <label>Số lượng dùng:</label>
                <span class="info-value">{{ voucher.soLuongDung || 'Không giới hạn' }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="detail-card">
          <div class="card-header">
            <h2>Mô tả và điều kiện</h2>
          </div>
          <div class="card-body">
            <div class="info-item full-width">
              <label>Mô tả:</label>
              <span class="info-value">{{ voucher.moTa || 'Không có mô tả' }}</span>
            </div>
            <div class="info-item full-width">
              <label>Điều kiện sử dụng:</label>
              <span class="info-value">{{ voucher.dieuKienSuDung || 'Không có điều kiện đặc biệt' }}</span>
            </div>
          </div>
        </div>

        <div class="detail-card">
          <div class="card-header">
            <h2>Thông tin hệ thống</h2>
          </div>
          <div class="card-body">
            <div class="info-grid">
              <div class="info-item">
                <label>Trạng thái:</label>
                <span class="status-badge" :class="getTrangThaiClass(voucher.trangThai)">
                  {{ getTrangThaiText(voucher.trangThai) }}
                </span>
              </div>
              <div class="info-item">
                <label>Ngày tạo:</label>
                <span class="info-value">{{ formatDateTime(voucher.ngayTao) }}</span>
              </div>
              <div class="info-item">
                <label>Ngày cập nhật:</label>
                <span class="info-value">{{ formatDateTime(voucher.ngayCapNhat) }}</span>
              </div>
              <div class="info-item">
                <label>Người tạo:</label>
                <span class="info-value">{{ voucher.nguoiTao || 'Hệ thống' }}</span>
              </div>
              <div class="info-item">
                <label>Người cập nhật:</label>
                <span class="info-value">{{ voucher.nguoiCapNhat || 'Hệ thống' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="loading-container" v-else-if="loading">
        <div class="loading-spinner"></div>
        <p>Đang tải thông tin voucher...</p>
      </div>

      <div class="error-container" v-else-if="error">
        <div class="error-icon">
          <font-awesome-icon icon="exclamation-triangle" />
        </div>
        <h3>Không thể tải thông tin voucher</h3>
        <p>{{ error }}</p>
        <button class="btn-retry" @click="loadVoucher">
          <font-awesome-icon icon="redo" />
          Thử lại
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
import PosHeader from '@/components/PosHeader.vue'

const router = useRouter()
const route = useRoute()
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

const voucher = ref<any>(null)
const loading = ref(false)
const error = ref('')

async function loadVoucher() {
  const voucherId = route.params.id
  if (!voucherId) {
    error.value = 'Không tìm thấy ID voucher'
    return
  }

  try {
    loading.value = true
    error.value = ''
    
    const response = await api.get(`/api/phieu-giam-gia/${voucherId}`)
    voucher.value = response.data
  } catch (err: any) {
    console.error('Lỗi khi tải thông tin voucher:', err)
    error.value = err.response?.data?.message || 'Có lỗi xảy ra khi tải thông tin voucher'
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.go(-1)
}

function editVoucher() {
  if (voucher.value) {
    router.push(`/voucher/form/${voucher.value.id}`)
  }
}

async function deleteVoucher() {
  if (!voucher.value) return

  if (!confirm('Bạn có chắc chắn muốn xóa voucher này?')) {
    return
  }

  try {
    await api.delete(`/api/phieu-giam-gia/${voucher.value.id}`)
    toastRef.value?.success('Thành công', 'Đã xóa voucher thành công!')
    router.push('/voucher')
  } catch (err: any) {
    console.error('Lỗi khi xóa voucher:', err)
    toastRef.value?.error('Lỗi', err.response?.data?.message || 'Có lỗi xảy ra khi xóa voucher')
  }
}

function formatDate(dateString: any) {
  if (!dateString) return ''
  
  try {
    // Handle LocalDate format (YYYY-MM-DD) or Date object
    const date = new Date(dateString)
    return date.toLocaleDateString('vi-VN')
  } catch {
    return String(dateString)
  }
}

function formatDateTime(dateString: any) {
  if (!dateString) return ''
  
  try {
    // Handle LocalDateTime format or Date object
    const date = new Date(dateString)
    return date.toLocaleString('vi-VN')
  } catch {
    return String(dateString)
  }
}

function formatCurrency(amount: any) {
  if (!amount) return '0 VNĐ'
  
  // Convert to number if it's a string or object
  const numAmount = typeof amount === 'number' ? amount : parseFloat(amount)
  
  if (isNaN(numAmount)) return '0 VNĐ'
  
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(numAmount)
}


function getTrangThaiText(trangThai: number) {
  switch (trangThai) {
    case 1: return 'Hoạt động'
    case 0: return 'Không hoạt động'
    case 2: return 'Hết hạn'
    case 3: return 'Hết số lượng'
    default: return 'Không xác định'
  }
}

function getTrangThaiClass(trangThai: number) {
  switch (trangThai) {
    case 1: return 'active'
    case 0: return 'inactive'
    case 2: return 'expired'
    case 3: return 'out-of-stock'
    default: return 'unknown'
  }
}

onMounted(() => {
  loadVoucher()
})
</script>

<style scoped>
.voucher-detail {
  background: #f8fafc;
  min-height: 100vh;
  padding: 0;
  padding-top: 80px;
  width: 100%;
  overflow-x: hidden;
  position: relative;
}

.detail-container {
  padding: 24px;
  padding-top: 0;
  width: 100%;
  margin: 0;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  width: 100%;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.btn-back {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #f1f5f9;
  color: #475569;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-back:hover {
  background: #e2e8f0;
  color: #334155;
}

.detail-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.btn-edit,
.btn-delete {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.2s;
}

.btn-edit {
  background: #3b82f6;
  color: white;
}

.btn-edit:hover {
  background: #2563eb;
  transform: translateY(-1px);
}

.btn-delete {
  background: #ef4444;
  color: white;
}

.btn-delete:hover {
  background: #dc2626;
  transform: translateY(-1px);
}

.detail-content {
  display: grid;
  gap: 24px;
  width: 100%;
}

.detail-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.card-header {
  padding: 20px 24px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
}

.card-body {
  padding: 24px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 24px;
  width: 100%;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-item label {
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-value {
  font-size: 14px;
  font-weight: 500;
  color: #1e293b;
  padding: 8px 12px;
  background: #f8fafc;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-badge.active {
  background: #dcfce7;
  color: #166534;
}

.status-badge.inactive {
  background: #fef2f2;
  color: #dc2626;
}

.status-badge.expired {
  background: #fef3c7;
  color: #d97706;
}

.status-badge.out-of-stock {
  background: #f3f4f6;
  color: #6b7280;
}

.status-badge.unknown {
  background: #f3f4f6;
  color: #6b7280;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e2e8f0;
  border-top: 4px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-container p {
  color: #64748b;
  font-size: 14px;
}

.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.error-icon {
  font-size: 48px;
  color: #ef4444;
  margin-bottom: 16px;
}

.error-container h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
}

.error-container p {
  margin: 0 0 24px 0;
  color: #64748b;
  font-size: 14px;
}

.btn-retry {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.2s;
}

.btn-retry:hover {
  background: #2563eb;
  transform: translateY(-1px);
}

/* Responsive */
@media (max-width: 768px) {
  .voucher-detail {
    padding-top: 60px;
  }
  
  .detail-container {
    padding: 16px;
  }
  
  .detail-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: center;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .btn-edit,
  .btn-delete {
    flex: 1;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .voucher-detail {
    padding-top: 50px;
  }
  
  .detail-container {
    padding: 12px;
  }
  
  .detail-header {
    padding: 16px;
  }
  
  .card-body {
    padding: 16px;
  }
}
</style>
