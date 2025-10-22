<template>
  <div class="page dark-mode-transition">
    <PosHeader />
    
    <div class="content">
      <Toast ref="toastRef" />

      <!-- Header Section -->
      <div class="header-section">
        <div class="header-content">
          <div class="header-title">
            <h1>Chi Tiết Giao Ca</h1>
            <p>Thông tin chi tiết về giao ca #{{ giaoCa?.maGiaoCa }}</p>
          </div>
          <div class="header-actions">
            <button class="btn-back" @click="goBack">
              <font-awesome-icon :icon="['fas', 'arrow-left']" />
              Quay lại
            </button>
            <button v-if="giaoCa?.trangThai === 0" class="btn-confirm" @click="confirmGiaoCa">
              <font-awesome-icon :icon="['fas', 'check']" />
              Xác nhận
            </button>
            <button v-if="giaoCa?.trangThai === 0" class="btn-cancel" @click="cancelGiaoCa">
              <font-awesome-icon :icon="['fas', 'times']" />
              Hủy
            </button>
          </div>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="loading-section">
        <div class="loading-spinner">
          <font-awesome-icon :icon="['fas', 'spinner']" class="fa-spin" />
          <p>Đang tải thông tin giao ca...</p>
        </div>
      </div>

      <!-- Content Section -->
      <div v-else-if="giaoCa" class="content-section">
        <!-- Basic Info Cards -->
        <div class="info-cards">
          <div class="info-card">
            <div class="card-header">
              <h3>Thông tin cơ bản</h3>
            </div>
            <div class="card-content">
              <div class="info-row">
                <span class="label">Mã giao ca:</span>
                <span class="value">{{ giaoCa.maGiaoCa }}</span>
              </div>
              <div class="info-row">
                <span class="label">Ca làm việc:</span>
                <span class="value">{{ giaoCa.caTen }}</span>
              </div>
              <div class="info-row">
                <span class="label">Ngày làm việc:</span>
                <span class="value">{{ formatDate(giaoCa.ngayLamViec) }}</span>
              </div>
              <div class="info-row">
                <span class="label">Nhân viên giao ca:</span>
                <span class="value">{{ giaoCa.nhanVienGiaoTen }}</span>
              </div>
              <div class="info-row">
                <span class="label">Nhân viên nhận ca:</span>
                <span class="value">{{ giaoCa.nhanVienNhanTen || 'Chưa xác định' }}</span>
              </div>
              <div class="info-row">
                <span class="label">Ngày giao ca:</span>
                <span class="value">{{ formatDateTime(giaoCa.ngayGiaoCa) }}</span>
              </div>
              <div class="info-row">
                <span class="label">Trạng thái:</span>
                <span class="status-badge" :class="getStatusBadgeClass(giaoCa)">
                  {{ getStatusText(giaoCa) }}
                </span>
              </div>
            </div>
          </div>

          <div class="info-card">
            <div class="card-header">
              <h3>Thông tin tài chính</h3>
            </div>
            <div class="card-content">
              <div class="info-row">
                <span class="label">Số tiền đầu ca:</span>
                <span class="value money">{{ formatCurrency(giaoCa.soTienDauCa) }}</span>
              </div>
              <div class="info-row">
                <span class="label">Số tiền cuối ca:</span>
                <span class="value money">{{ formatCurrency(giaoCa.soTienCuoiCa) }}</span>
              </div>
              <div class="info-row">
                <span class="label">Số tiền thu thêm:</span>
                <span class="value money positive">{{ formatCurrency(giaoCa.soTienThuThem) }}</span>
              </div>
              <div class="info-row">
                <span class="label">Số tiền chi ra:</span>
                <span class="value money negative">{{ formatCurrency(giaoCa.soTienChiRa) }}</span>
              </div>
              <div class="info-row">
                <span class="label">Chênh lệch:</span>
                <span class="value money" :class="giaoCa.chenhLech >= 0 ? 'positive' : 'negative'">
                  {{ giaoCa.chenhLech >= 0 ? '+' : '' }}{{ formatCurrency(giaoCa.chenhLech) }}
                </span>
              </div>
            </div>
          </div>

          <div class="info-card">
            <div class="card-header">
              <h3>Thông tin kinh doanh</h3>
            </div>
            <div class="card-content">
              <div class="info-row">
                <span class="label">Tổng doanh thu:</span>
                <span class="value money">{{ formatCurrency(giaoCa.tongDoanhThu) }}</span>
              </div>
              <div class="info-row">
                <span class="label">Số đơn hàng:</span>
                <span class="value">{{ giaoCa.soDonHang }} đơn</span>
              </div>
              <div class="info-row">
                <span class="label">Đơn thanh toán tiền mặt:</span>
                <span class="value">{{ giaoCa.soDonHangThanhToanTienMat }} đơn</span>
              </div>
              <div class="info-row">
                <span class="label">Đơn thanh toán chuyển khoản:</span>
                <span class="value">{{ giaoCa.soDonHangThanhToanChuyenKhoan }} đơn</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Report Section -->
        <div class="report-section">
          <div class="info-card">
            <div class="card-header">
              <h3>Báo cáo công việc</h3>
            </div>
            <div class="card-content">
              <div class="report-content">
                <p v-if="giaoCa.baoCaoCongViec">{{ giaoCa.baoCaoCongViec }}</p>
                <p v-else class="no-content">Chưa có báo cáo công việc</p>
              </div>
            </div>
          </div>

          <div class="info-card">
            <div class="card-header">
              <h3>Sự cố bất thường</h3>
            </div>
            <div class="card-content">
              <div class="report-content">
                <p v-if="giaoCa.suCoBatThuong">{{ giaoCa.suCoBatThuong }}</p>
                <p v-else class="no-content">Không có sự cố bất thường</p>
              </div>
            </div>
          </div>

          <div class="info-card">
            <div class="card-header">
              <h3>Công việc tồn đọng</h3>
            </div>
            <div class="card-content">
              <div class="report-content">
                <p v-if="giaoCa.congViecTonDong">{{ giaoCa.congViecTonDong }}</p>
                <p v-else class="no-content">Không có công việc tồn đọng</p>
              </div>
            </div>
          </div>

          <div class="info-card">
            <div class="card-header">
              <h3>Ghi chú</h3>
            </div>
            <div class="card-content">
              <div class="report-content">
                <p v-if="giaoCa.ghiChu">{{ giaoCa.ghiChu }}</p>
                <p v-else class="no-content">Không có ghi chú</p>
              </div>
            </div>
          </div>
        </div>

        <!-- History Section -->
        <div v-if="lichSuList.length > 0" class="history-section">
          <div class="info-card">
            <div class="card-header">
              <h3>Lịch sử thay đổi</h3>
            </div>
            <div class="card-content">
              <div class="history-list">
                <div v-for="lichSu in lichSuList" :key="lichSu.id" class="history-item">
                  <div class="history-time">{{ formatDateTime(lichSu.thoiGian) }}</div>
                  <div class="history-action">{{ lichSu.hanhDong }}</div>
                  <div class="history-details">{{ lichSu.chiTiet }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Error State -->
      <div v-else class="error-section">
        <div class="error-content">
          <font-awesome-icon :icon="['fas', 'exclamation-triangle']" />
          <h3>Không tìm thấy giao ca</h3>
          <p>Giao ca bạn đang tìm kiếm không tồn tại hoặc đã bị xóa.</p>
          <button class="btn-back" @click="goBack">
            <font-awesome-icon :icon="['fas', 'arrow-left']" />
            Quay lại
          </button>
        </div>
      </div>
    </div>
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
const loading = ref(false)

interface GiaoCa {
  id: number
  maGiaoCa: string
  phanCaId: number
  nhanVienGiaoId: number
  nhanVienNhanId?: number
  ngayGiaoCa: string
  soTienDauCa: number
  soTienCuoiCa: number
  soTienThuThem: number
  soTienChiRa: number
  chenhLech: number
  tongDoanhThu: number
  soDonHang: number
  soDonHangThanhToanTienMat: number
  soDonHangThanhToanChuyenKhoan: number
  trangThai: number
  thoiGianXacNhan?: string
  ghiChu?: string
  baoCaoCongViec?: string
  suCoBatThuong?: string
  congViecTonDong?: string
  // Additional fields for display
  caTen?: string
  nhanVienGiaoTen?: string
  nhanVienNhanTen?: string
  ngayLamViec?: string
}

interface LichSuGiaoCa {
  id: number
  thoiGian: string
  hanhDong: string
  chiTiet: string
  nguoiThucHien: string
}

const giaoCa = ref<GiaoCa | null>(null)
const lichSuList = ref<LichSuGiaoCa[]>([])

async function loadGiaoCa() {
  loading.value = true
  try {
    const giaoCaId = route.params.id
    const { data } = await api.get<GiaoCa>(`/api/giao-ca/${giaoCaId}`)
    giaoCa.value = data
    
    // Load lich su
    await loadLichSu(giaoCaId as string)
  } catch (error) {
    console.error('Lỗi khi tải thông tin giao ca:', error)
    toastRef.value?.error('Lỗi', 'Không thể tải thông tin giao ca')
  } finally {
    loading.value = false
  }
}

async function loadLichSu(giaoCaId: string) {
  try {
    const { data } = await api.get<LichSuGiaoCa[]>(`/api/lich-su-giao-ca/giao-ca/${giaoCaId}`)
    lichSuList.value = data
  } catch (error) {
    console.error('Lỗi khi tải lịch sử giao ca:', error)
  }
}

async function confirmGiaoCa() {
  if (!giaoCa.value) return
  
  if (confirm('Bạn có chắc chắn muốn xác nhận giao ca này?')) {
    try {
      await api.put(`/api/giao-ca/${giaoCa.value.id}/confirm?nhanVienNhanId=${giaoCa.value.nhanVienGiaoId}`, {
        soTienCuoiCa: giaoCa.value.soTienCuoiCa,
        tongDoanhThu: giaoCa.value.tongDoanhThu,
        soDonHang: giaoCa.value.soDonHang,
        baoCaoCongViec: giaoCa.value.baoCaoCongViec
      })
      
      giaoCa.value.trangThai = 1
      toastRef.value?.success('Thành công', 'Đã xác nhận giao ca!')
      
      // Reload lich su
      await loadLichSu(giaoCa.value.id.toString())
    } catch (error: any) {
      console.error('Lỗi khi xác nhận giao ca:', error)
      toastRef.value?.error('Lỗi', 'Không thể xác nhận giao ca')
    }
  }
}

async function cancelGiaoCa() {
  if (!giaoCa.value) return
  
  const lyDo = prompt('Nhập lý do hủy giao ca:')
  if (lyDo === null) return
  
  try {
    await api.put(`/api/giao-ca/${giaoCa.value.id}/cancel?lyDo=${encodeURIComponent(lyDo)}`)
    
    giaoCa.value.trangThai = 2
    toastRef.value?.success('Thành công', 'Đã hủy giao ca!')
    
    // Reload lich su
    await loadLichSu(giaoCa.value.id.toString())
  } catch (error: any) {
    console.error('Lỗi khi hủy giao ca:', error)
    toastRef.value?.error('Lỗi', 'Không thể hủy giao ca')
  }
}

function goBack() {
  router.push('/quan-ly-giao-ca')
}

function formatDate(dateString: string): string {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleDateString('vi-VN')
}

function formatDateTime(dateTime: string): string {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('vi-VN')
}

function formatCurrency(amount: number): string {
  if (!amount) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', { 
    style: 'currency', 
    currency: 'VND' 
  }).format(amount)
}

function getStatusText(giaoCa: GiaoCa): string {
  switch (giaoCa.trangThai) {
    case 0: return 'Chờ xác nhận'
    case 1: return 'Đã xác nhận'
    case 2: return 'Đã hủy'
    default: return 'Không xác định'
  }
}

function getStatusBadgeClass(giaoCa: GiaoCa): string {
  switch (giaoCa.trangThai) {
    case 0: return 'badge-pending'
    case 1: return 'badge-active'
    case 2: return 'badge-cancelled'
    default: return 'badge-unknown'
  }
}

onMounted(() => {
  loadGiaoCa()
})
</script>

<style scoped>
/* Import base styles - copy from VoucherPage.vue */

/* Page specific styles */
.page {
  background: #f8fafc;
  min-height: 100vh;
  padding: 0;
  padding-top: 80px;
  width: 100%;
  overflow-x: hidden;
  position: relative;
}

.content {
  padding: 24px;
  padding-top: 0;
  width: 100%;
  max-width: 100%;
  margin: 0;
  overflow-x: hidden;
  box-sizing: border-box;
}

/* Header Section */
.header-section {
  background: white;
  padding: 24px;
  margin-bottom: 24px;
  border-radius: 16px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
  border: 1px solid #e2e8f0;
  position: relative;
  overflow: hidden;
}

.header-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #f97316, #ea580c, #dc2626);
  border-radius: 16px 16px 0 0;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title h1 {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 8px 0;
}

.header-title p {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.btn-back,
.btn-confirm,
.btn-cancel {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
  border: none;
}

.btn-back {
  background: #f8fafc;
  color: #64748b;
  border: 1px solid #e2e8f0;
}

.btn-back:hover {
  background: #f1f5f9;
  color: #374151;
  border-color: #cbd5e1;
  transform: translateY(-1px);
}

.btn-confirm {
  background: #059669;
  color: white;
}

.btn-confirm:hover {
  background: #047857;
  transform: translateY(-1px);
}

.btn-cancel {
  background: #dc2626;
  color: white;
}

.btn-cancel:hover {
  background: #b91c1c;
  transform: translateY(-1px);
}

/* Loading Section */
.loading-section {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.loading-spinner {
  text-align: center;
  color: #64748b;
}

.loading-spinner svg {
  font-size: 48px;
  margin-bottom: 16px;
}

/* Content Section */
.content-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.info-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 24px;
}

.info-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
  overflow: hidden;
}

.card-header {
  background: #f8fafc;
  padding: 16px 20px;
  border-bottom: 1px solid #e2e8f0;
}

.card-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.card-content {
  padding: 20px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f1f5f9;
}

.info-row:last-child {
  border-bottom: none;
}

.info-row .label {
  font-weight: 500;
  color: #64748b;
  font-size: 14px;
}

.info-row .value {
  font-weight: 600;
  color: #1e293b;
  font-size: 14px;
}

.info-row .value.money {
  font-family: 'Courier New', monospace;
}

.info-row .value.positive {
  color: #059669;
}

.info-row .value.negative {
  color: #dc2626;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.badge-pending {
  background: #fef3c7;
  color: #d97706;
}

.badge-active {
  background: #dcfce7;
  color: #166534;
}

.badge-cancelled {
  background: #fef2f2;
  color: #dc2626;
}

.badge-unknown {
  background: #f3f4f6;
  color: #6b7280;
}

/* Report Section */
.report-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
}

.report-content {
  min-height: 100px;
}

.report-content p {
  margin: 0;
  line-height: 1.6;
  color: #374151;
}

.report-content .no-content {
  color: #9ca3af;
  font-style: italic;
}

/* History Section */
.history-section {
  margin-top: 24px;
}

.history-list {
  max-height: 400px;
  overflow-y: auto;
}

.history-item {
  padding: 12px 0;
  border-bottom: 1px solid #f1f5f9;
}

.history-item:last-child {
  border-bottom: none;
}

.history-time {
  font-size: 12px;
  color: #9ca3af;
  margin-bottom: 4px;
}

.history-action {
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
}

.history-details {
  font-size: 14px;
  color: #64748b;
  line-height: 1.4;
}

/* Error Section */
.error-section {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.error-content {
  text-align: center;
  color: #64748b;
}

.error-content svg {
  font-size: 64px;
  margin-bottom: 16px;
  color: #f59e0b;
}

.error-content h3 {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 8px 0;
}

.error-content p {
  margin: 0 0 24px 0;
}

/* Responsive design */
@media (max-width: 768px) {
  .content {
    padding: 16px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .header-actions {
    flex-direction: column;
    gap: 8px;
  }
  
  .btn-back,
  .btn-confirm,
  .btn-cancel {
    width: 100%;
    justify-content: center;
  }
  
  .info-cards {
    grid-template-columns: 1fr;
  }
  
  .report-section {
    grid-template-columns: 1fr;
  }
  
  .info-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}
</style>
