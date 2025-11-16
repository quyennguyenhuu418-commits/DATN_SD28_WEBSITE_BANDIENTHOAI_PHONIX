<template>
  <div class="giao-ca-detail-page dark-mode-transition">
    <PosHeader />
    
    <div class="detail-container">
      <div class="detail-header">
        <div class="header-left">
          <button class="btn-back" @click="goBack">
            <font-awesome-icon :icon="['fas', 'arrow-left']" />
            Quay lại
          </button>
          <h1>Chi tiết Giao Ca</h1>
        </div>
        <div class="header-actions" v-if="giaoCa && !loading">
          <button 
            v-if="giaoCa.trangThai === 0" 
            class="btn-edit" 
            @click="editGiaoCa"
          >
            <font-awesome-icon :icon="['fas', 'edit']" />
            Sửa giao ca
          </button>
        </div>
      </div>

      <div class="detail-content" v-if="giaoCa && !loading">
        <div class="detail-card">
          <div class="card-header">
            <h2>Thông tin cơ bản</h2>
          </div>
          <div class="card-body">
            <div class="info-grid">
              <div class="info-item">
                <label>ID:</label>
                <span class="info-value">{{ giaoCa.id }}</span>
              </div>
              <div class="info-item">
                <label>Mã giao ca:</label>
                <span class="info-value">{{ giaoCa.maGiaoCa }}</span>
              </div>
              <div class="info-item">
                <label>Nhân viên giao:</label>
                <span class="info-value">{{ giaoCa.nhanVienGiaoTen || giaoCa.nhanVienGiao?.hoTen || 'N/A' }}</span>
              </div>
              <div class="info-item">
                <label>Nhân viên nhận:</label>
                <span class="info-value" :class="{'text-muted': !giaoCa.nhanVienNhanTen && !giaoCa.nhanVienNhan?.hoTen}">
                  {{ giaoCa.nhanVienNhanTen || giaoCa.nhanVienNhan?.hoTen || 'Chưa có người nhận' }}
                </span>
                <small v-if="!giaoCa.nhanVienNhanTen && !giaoCa.nhanVienNhan?.hoTen" class="info-hint">
                  Nhân viên có ca tiếp theo sẽ tự xác nhận khi đăng nhập
                </small>
              </div>
              <div class="info-item">
                <label>Ngày giao ca:</label>
                <span class="info-value">{{ formatDateTime(giaoCa.ngayGiaoCa) }}</span>
              </div>
              <div class="info-item">
                <label>Trạng thái:</label>
                <span class="info-value">
                  <span class="status-badge" :class="getStatusClass(giaoCa.trangThai)">
                    {{ getStatusText(giaoCa.trangThai) }}
                  </span>
                </span>
              </div>
            </div>
          </div>
        </div>

        <div class="detail-card">
          <div class="card-header">
            <h2>Thông tin tài chính</h2>
          </div>
          <div class="card-body">
            <div class="info-grid">
              <div class="info-item">
                <label>Số tiền đầu ca:</label>
                <span class="info-value">{{ formatCurrency(giaoCa.soTienDauCa) }}</span>
              </div>
              <div class="info-item">
                <label>Số tiền cuối ca:</label>
                <span class="info-value">{{ formatCurrency(giaoCa.soTienCuoiCa) }}</span>
              </div>
              <div class="info-item">
                <label>Số tiền thu thêm:</label>
                <span class="info-value">{{ formatCurrency(giaoCa.soTienThuThem) }}</span>
              </div>
              <div class="info-item">
                <label>Số tiền chi ra:</label>
                <span class="info-value">{{ formatCurrency(giaoCa.soTienChiRa) }}</span>
              </div>
              <div class="info-item">
                <label>Chênh lệch:</label>
                <span class="info-value" :class="getChenhLechClass(giaoCa.chenhLech)">
                  {{ formatCurrency(giaoCa.chenhLech) }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <div class="detail-card">
          <div class="card-header">
            <h2>Ghi chú và báo cáo</h2>
          </div>
          <div class="card-body">
            <div class="info-grid">
              <div class="info-item" v-if="giaoCa.ghiChu">
                <label>Ghi chú:</label>
                <span class="info-value">{{ giaoCa.ghiChu }}</span>
              </div>
              <div class="info-item" v-if="giaoCa.baoCaoCongViec">
                <label>Báo cáo công việc:</label>
                <span class="info-value">{{ giaoCa.baoCaoCongViec }}</span>
              </div>
              <div class="info-item" v-if="giaoCa.suCoBatThuong">
                <label>Sự cố bất thường:</label>
                <span class="info-value">{{ giaoCa.suCoBatThuong }}</span>
              </div>
              <div class="info-item" v-if="giaoCa.congViecTonDong">
                <label>Công việc tồn đọng:</label>
                <span class="info-value">{{ giaoCa.congViecTonDong }}</span>
              </div>
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
                <label>Ngày tạo:</label>
                <span class="info-value">{{ formatDateTime(giaoCa.ngayTao) }}</span>
              </div>
              <div class="info-item">
                <label>Ngày cập nhật:</label>
                <span class="info-value">{{ formatDateTime(giaoCa.ngayCapNhat) }}</span>
              </div>
              <div class="info-item">
                <label>Thời gian xác nhận:</label>
                <span class="info-value">{{ formatDateTime(giaoCa.thoiGianXacNhan) || 'Chưa xác nhận' }}</span>
              </div>
              <div class="info-item">
                <label>Người tạo:</label>
                <span class="info-value">{{ giaoCa.nguoiTao || 'Hệ thống' }}</span>
              </div>
              <div class="info-item">
                <label>Người cập nhật:</label>
                <span class="info-value">{{ giaoCa.nguoiCapNhat || 'Hệ thống' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="loading-container" v-else-if="loading">
        <div class="loading-spinner"></div>
        <p>Đang tải thông tin giao ca...</p>
      </div>

      <div class="error-container" v-else-if="error">
        <div class="error-icon">
          <font-awesome-icon :icon="['fas', 'exclamation-triangle']" />
        </div>
        <h3>Không thể tải thông tin giao ca</h3>
        <p>{{ error }}</p>
        <button class="btn-retry" @click="loadGiaoCa">
          <font-awesome-icon :icon="['fas', 'redo']" />
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

const giaoCa = ref<any>(null)
const loading = ref(false)
const error = ref('')

function formatDate(date: string) {
  if (!date) return 'N/A'
  const d = new Date(date)
  return d.toLocaleDateString('vi-VN')
}

function formatDateTime(dateTime: string) {
  if (!dateTime) return null
  const d = new Date(dateTime)
  return d.toLocaleString('vi-VN')
}

function formatCurrency(amount?: number) {
  if (amount === null || amount === undefined) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount)
}

function getStatusText(trangThai: number | null): string {
  if (trangThai === null) return 'Chờ xác nhận'
  switch (trangThai) {
    case 0: return 'Chờ xác nhận'
    case 1: return 'Đã xác nhận'
    case 2: return 'Đã hủy'
    default: return 'Không xác định'
  }
}

function getStatusClass(trangThai: number | null): string {
  if (trangThai === null) return 'badge-pending'
  switch (trangThai) {
    case 0: return 'badge-pending'
    case 1: return 'badge-active'
    case 2: return 'badge-cancelled'
    default: return 'badge-unknown'
  }
}

function getChenhLechClass(chenhLech?: number): string {
  if (chenhLech === null || chenhLech === undefined) return ''
  return chenhLech >= 0 ? 'text-success' : 'text-danger'
}

async function loadGiaoCa() {
  const giaoCaId = route.params.id
  if (!giaoCaId) {
    error.value = 'Không tìm thấy ID giao ca'
    return
  }

  try {
    loading.value = true
    error.value = ''
    
    const response = await api.get(`/api/giao-ca/${giaoCaId}`)
    giaoCa.value = response.data
  } catch (err: any) {
    if (err.response?.status === 404) {
      error.value = 'Không tìm thấy giao ca với ID này'
    } else if (err.response?.status === 500) {
      error.value = 'Lỗi server, vui lòng thử lại sau'
    } else {
      error.value = err.response?.data?.message || 'Có lỗi xảy ra khi tải thông tin giao ca'
    }
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push('/giao-ca')
}

function editGiaoCa() {
  if (giaoCa.value && giaoCa.value.id) {
    router.push(`/giao-ca/edit/${giaoCa.value.id}`)
  }
}


onMounted(() => {
  loadGiaoCa()
})
</script>

<style scoped>
.giao-ca-detail-page {
  background: #f8fafc;
  min-height: 100vh;
  padding: 0;
  padding-top: 80px;
}

.detail-container {
  width: 100%;
  max-width: 100%;
  margin: 0 auto;
  padding: 24px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.btn-edit {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn-edit:hover {
  background: #2563eb;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.btn-edit:disabled {
  background: #94a3b8;
  cursor: not-allowed;
  transform: none;
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
  padding: 10px 20px;
  background: white;
  color: #374151;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.btn-back:hover {
  background: #f3f4f6;
  border-color: #9ca3af;
}

.detail-header h1 {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}


.detail-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
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
  border-bottom: 1px solid #e5e7eb;
}

.card-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.card-body {
  padding: 24px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item label {
  font-weight: 600;
  color: #64748b;
  font-size: 14px;
}

.info-value {
  color: #1e293b;
  font-size: 15px;
  font-weight: 500;
}

.text-success {
  color: #166534;
}

.text-danger {
  color: #dc2626;
}

.text-muted {
  color: #9ca3af;
  font-style: italic;
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
}

.badge-pending {
  background: #fef3c7;
  color: #92400e;
}

.badge-active {
  background: #dcfce7;
  color: #166534;
}

.badge-cancelled {
  background: #fee2e2;
  color: #991b1b;
}

.badge-unknown {
  background: #f3f4f6;
  color: #374151;
}

.loading-container,
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f4f6;
  border-top-color: #f97316;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-icon {
  font-size: 48px;
  color: #dc2626;
  margin-bottom: 16px;
}

.error-container h3 {
  font-size: 18px;
  color: #dc2626;
  margin: 0 0 8px 0;
}

.error-container p {
  color: #64748b;
  margin: 0 0 16px 0;
}

.btn-retry {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: #f97316;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
}

.btn-retry:hover {
  background: #ea580c;
}

@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .detail-container {
    padding: 16px;
  }
}
</style>

