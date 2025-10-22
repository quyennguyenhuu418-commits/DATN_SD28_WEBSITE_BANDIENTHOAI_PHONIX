<template>
  <div class="page dark-mode-transition">
    <PosHeader />
    
    <div class="content">
      <Toast ref="toastRef" />

      <!-- Header Section -->
      <div class="detail-header">
        <div class="header-content">
          <div class="header-left">
            <button @click="goBack" class="btn-back">
              <font-awesome-icon :icon="['fas', 'arrow-left']" />
              Quay lại
            </button>
            <div class="header-title">
              <h1>Chi Tiết Phân Ca</h1>
              <p>Thông tin chi tiết về phân ca</p>
            </div>
          </div>
          <div class="header-actions">
            <button @click="editPhanCa" class="btn-edit" :disabled="loading">
              <font-awesome-icon :icon="['fas', 'edit']" />
              Chỉnh sửa
            </button>
            <button @click="toggleStatus" class="btn-toggle" :disabled="loading">
              <font-awesome-icon :icon="['fas', 'power-off']" />
              {{ phanCaData?.trangThai === 1 ? 'Tắt' : 'Bật' }}
            </button>
          </div>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner">
          <font-awesome-icon :icon="['fas', 'spinner']" spin />
        </div>
        <p>Đang tải thông tin...</p>
      </div>

      <!-- Content -->
      <div v-else-if="phanCaData" class="detail-content">
        <!-- Basic Information Card -->
        <div class="info-card">
          <div class="card-header">
            <h2>
              <font-awesome-icon :icon="['fas', 'info-circle']" />
              Thông Tin Cơ Bản
            </h2>
          </div>
          <div class="card-content">
            <div class="info-grid">
              <div class="info-item">
                <label>Nhân Viên</label>
                <div class="info-value">{{ phanCaData.nhanVien?.hoTen || 'N/A' }}</div>
              </div>
              <div class="info-item">
                <label>Ca Làm Việc</label>
                <div class="info-value">{{ phanCaData.ca?.tenCa || 'N/A' }}</div>
              </div>
              <div class="info-item">
                <label>Ngày Làm Việc</label>
                <div class="info-value date-value">{{ formatDate(phanCaData.ngayLamViec) }}</div>
              </div>
              <div class="info-item">
                <label>Thời Gian Ca</label>
                <div class="info-value time-range">
                  {{ formatTime(phanCaData.ca?.gioBatDau) }} - {{ formatTime(phanCaData.ca?.gioKetThuc) }}
                </div>
              </div>
              <div class="info-item">
                <label>Trạng Thái</label>
                <div class="info-value">
                  <span class="status-badge" :class="getStatusBadgeClass()">
                    {{ getStatusText() }}
                  </span>
                </div>
              </div>
              <div class="info-item">
                <label>Ngày Tạo</label>
                <div class="info-value">{{ formatDateTime(phanCaData.createdAt) }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Description Card -->
        <div v-if="phanCaData.ghiChu" class="info-card">
          <div class="card-header">
            <h2>
              <font-awesome-icon :icon="['fas', 'file-text']" />
              Ghi Chú
            </h2>
          </div>
          <div class="card-content">
            <div class="description-text">{{ phanCaData.ghiChu }}</div>
          </div>
        </div>

        <!-- Ca Information Card -->
        <div v-if="phanCaData.ca" class="info-card">
          <div class="card-header">
            <h2>
              <font-awesome-icon :icon="['fas', 'clock']" />
              Thông Tin Ca Làm Việc
            </h2>
            <button @click="viewCaDetail" class="btn-view-ca">
              Xem chi tiết ca
              <font-awesome-icon :icon="['fas', 'arrow-right']" />
            </button>
          </div>
          <div class="card-content">
            <div class="ca-info-grid">
              <div class="ca-info-item">
                <div class="ca-info-label">Tên Ca</div>
                <div class="ca-info-value">{{ phanCaData.ca.tenCa }}</div>
              </div>
              <div class="ca-info-item">
                <div class="ca-info-label">Giờ Bắt Đầu</div>
                <div class="ca-info-value time-value">{{ formatTime(phanCaData.ca.gioBatDau) }}</div>
              </div>
              <div class="ca-info-item">
                <div class="ca-info-label">Giờ Kết Thúc</div>
                <div class="ca-info-value time-value">{{ formatTime(phanCaData.ca.gioKetThuc) }}</div>
              </div>
              <div class="ca-info-item">
                <div class="ca-info-label">Thời Gian Làm Việc</div>
                <div class="ca-info-value duration-value">{{ calculateCaDuration() }}</div>
              </div>
              <div v-if="phanCaData.ca.moTa" class="ca-info-item full-width">
                <div class="ca-info-label">Mô Tả Ca</div>
                <div class="ca-info-value">{{ phanCaData.ca.moTa }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Nhan Vien Information Card -->
        <div v-if="phanCaData.nhanVien" class="info-card">
          <div class="card-header">
            <h2>
              <font-awesome-icon :icon="['fas', 'user']" />
              Thông Tin Nhân Viên
            </h2>
            <button @click="viewNhanVienDetail" class="btn-view-nhan-vien">
              Xem chi tiết NV
              <font-awesome-icon :icon="['fas', 'arrow-right']" />
            </button>
          </div>
          <div class="card-content">
            <div class="nhan-vien-info">
              <div class="nhan-vien-avatar">
                <font-awesome-icon :icon="['fas', 'user']" />
              </div>
              <div class="nhan-vien-details">
                <div class="nhan-vien-name">{{ phanCaData.nhanVien.hoTen }}</div>
                <div class="nhan-vien-role">Nhân viên bán hàng</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Statistics Card -->
        <div class="info-card">
          <div class="card-header">
            <h2>
              <font-awesome-icon :icon="['fas', 'chart-bar']" />
              Thống Kê
            </h2>
          </div>
          <div class="card-content">
            <div class="stats-grid">
              <div class="stat-item">
                <div class="stat-icon">
                  <font-awesome-icon :icon="['fas', 'exchange-alt']" />
                </div>
                <div class="stat-content">
                  <div class="stat-value">{{ statistics.totalGiaoCa }}</div>
                  <div class="stat-label">Giao Ca</div>
                </div>
              </div>
              <div class="stat-item">
                <div class="stat-icon">
                  <font-awesome-icon :icon="['fas', 'check-circle']" />
                </div>
                <div class="stat-content">
                  <div class="stat-value">{{ statistics.completedGiaoCa }}</div>
                  <div class="stat-label">Hoàn Thành</div>
                </div>
              </div>
              <div class="stat-item">
                <div class="stat-icon">
                  <font-awesome-icon :icon="['fas', 'clock']" />
                </div>
                <div class="stat-content">
                  <div class="stat-value">{{ statistics.pendingGiaoCa }}</div>
                  <div class="stat-label">Chờ Xử Lý</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Recent Giao Ca -->
        <div class="info-card">
          <div class="card-header">
            <h2>
              <font-awesome-icon :icon="['fas', 'exchange-alt']" />
              Giao Ca Gần Đây
            </h2>
            <button @click="viewAllGiaoCa" class="btn-view-all">
              Xem tất cả
              <font-awesome-icon :icon="['fas', 'arrow-right']" />
            </button>
          </div>
          <div class="card-content">
            <div v-if="recentGiaoCa.length === 0" class="empty-state">
              <font-awesome-icon :icon="['fas', 'exchange-alt']" />
              <p>Chưa có giao ca nào</p>
            </div>
            <div v-else class="giao-ca-list">
              <div v-for="giaoCa in recentGiaoCa" :key="giaoCa.id" class="giao-ca-item">
                <div class="giao-ca-info">
                  <div class="giao-ca-title">Giao ca #{{ giaoCa.id }}</div>
                  <div class="giao-ca-date">{{ formatDateTime(giaoCa.ngayGiaoCa) }}</div>
                </div>
                <div class="giao-ca-status">
                  <span class="status-badge" :class="getGiaoCaStatusClass(giaoCa.trangThai)">
                    {{ getGiaoCaStatusText(giaoCa.trangThai) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Error State -->
      <div v-else class="error-container">
        <div class="error-icon">
          <font-awesome-icon :icon="['fas', 'exclamation-triangle']" />
        </div>
        <h3>Không tìm thấy phân ca</h3>
        <p>Phân ca này có thể đã bị xóa hoặc không tồn tại.</p>
        <button @click="goBack" class="btn-primary">
          <font-awesome-icon :icon="['fas', 'arrow-left']" />
          Quay lại
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '@/services/api'
import Toast from '@/components/Toast.vue'
import PosHeader from '@/components/PosHeader.vue'

const router = useRouter()
const route = useRoute()

interface NhanVien {
  id: number
  hoTen: string
}

interface Ca {
  id: number
  tenCa: string
  gioBatDau: string
  gioKetThuc: string
  moTa?: string
}

interface PhanCa {
  id: number
  nhanVienId: number
  nhanVien?: NhanVien
  caId: number
  ca?: Ca
  ngayLamViec: string
  ghiChu?: string
  trangThai: number
  createdAt: string
}

interface GiaoCa {
  id: number
  ngayGiaoCa: string
  trangThai: number
}

interface Statistics {
  totalGiaoCa: number
  completedGiaoCa: number
  pendingGiaoCa: number
}

const phanCaData = ref<PhanCa | null>(null)
const recentGiaoCa = ref<GiaoCa[]>([])
const statistics = ref<Statistics>({
  totalGiaoCa: 0,
  completedGiaoCa: 0,
  pendingGiaoCa: 0
})
const loading = ref(false)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

const phanCaId = computed(() => route.params.id as string)

async function loadPhanCaDetail() {
  loading.value = true
  try {
    const { data } = await api.get(`/api/phan-ca/${phanCaId.value}`)
    phanCaData.value = data
    await loadStatistics()
    await loadRecentGiaoCa()
  } catch (error) {
    console.error('Lỗi khi tải chi tiết phân ca:', error)
    toastRef.value?.error('Lỗi', 'Không thể tải thông tin phân ca')
  } finally {
    loading.value = false
  }
}

async function loadStatistics() {
  try {
    // Load statistics for this phan ca
    const { data } = await api.get(`/api/giao-ca/by-phan-ca/${phanCaId.value}`)
    const giaoCaList = data || []
    
    statistics.value = {
      totalGiaoCa: giaoCaList.length,
      completedGiaoCa: giaoCaList.filter((g: GiaoCa) => g.trangThai === 2).length,
      pendingGiaoCa: giaoCaList.filter((g: GiaoCa) => g.trangThai === 1).length
    }
  } catch (error) {
    console.error('Lỗi khi tải thống kê:', error)
  }
}

async function loadRecentGiaoCa() {
  try {
    const { data } = await api.get(`/api/giao-ca/by-phan-ca/${phanCaId.value}?limit=5`)
    recentGiaoCa.value = data || []
  } catch (error) {
    console.error('Lỗi khi tải giao ca gần đây:', error)
  }
}

function formatTime(timeString: string): string {
  if (!timeString) return '-'
  return timeString.substring(0, 5) // HH:MM
}

function formatDate(dateString: string): string {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN')
}

function formatDateTime(dateString: string): string {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('vi-VN')
}

function calculateCaDuration(): string {
  if (!phanCaData.value?.ca?.gioBatDau || !phanCaData.value?.ca?.gioKetThuc) return '-'
  
  const start = new Date(`2000-01-01T${phanCaData.value.ca.gioBatDau}`)
  const end = new Date(`2000-01-01T${phanCaData.value.ca.gioKetThuc}`)
  
  // Handle case where end time is next day
  if (end <= start) {
    end.setDate(end.getDate() + 1)
  }
  
  const diffMs = end.getTime() - start.getTime()
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
  const diffMinutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60))
  
  return `${diffHours}h ${diffMinutes}m`
}

function getStatusText(): string {
  return phanCaData.value?.trangThai === 1 ? 'Hoạt động' : 'Ngừng hoạt động'
}

function getStatusBadgeClass(): string {
  return phanCaData.value?.trangThai === 1 ? 'badge-active' : 'badge-inactive'
}

function getGiaoCaStatusText(status: number): string {
  switch (status) {
    case 1: return 'Chờ xử lý'
    case 2: return 'Hoàn thành'
    case 3: return 'Hủy bỏ'
    default: return 'Không xác định'
  }
}

function getGiaoCaStatusClass(status: number): string {
  switch (status) {
    case 1: return 'badge-pending'
    case 2: return 'badge-completed'
    case 3: return 'badge-cancelled'
    default: return 'badge-unknown'
  }
}

async function toggleStatus() {
  if (!phanCaData.value) return
  
  try {
    await api.post(`/api/phan-ca/${phanCaId.value}/toggle-status`)
    phanCaData.value.trangThai = phanCaData.value.trangThai === 1 ? 0 : 1
    
    const message = phanCaData.value.trangThai === 1 ? 'Đã kích hoạt phân ca!' : 'Đã tắt phân ca!'
    toastRef.value?.success('Thành công', message)
  } catch (error) {
    console.error('Lỗi khi cập nhật trạng thái:', error)
    toastRef.value?.error('Lỗi', 'Không thể cập nhật trạng thái')
  }
}

function editPhanCa() {
  router.push(`/phan-ca/form/${phanCaId.value}`)
}

function viewCaDetail() {
  if (phanCaData.value?.caId) {
    router.push(`/ca/detail/${phanCaData.value.caId}`)
  }
}

function viewNhanVienDetail() {
  if (phanCaData.value?.nhanVienId) {
    router.push(`/nhan-vien/detail/${phanCaData.value.nhanVienId}`)
  }
}

function viewAllGiaoCa() {
  router.push(`/quan-ly-giao-ca?phanCa=${phanCaId.value}`)
}

function goBack() {
  router.push('/phan-ca')
}

onMounted(() => {
  loadPhanCaDetail()
})
</script>

<style scoped>
/* Orange and Black POS Theme */
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

/* Detail Header */
.detail-header {
  background: white;
  padding: 24px;
  margin-bottom: 24px;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  padding: 10px 16px;
  background: #f8fafc;
  color: #64748b;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.btn-back:hover {
  background: #f1f5f9;
  color: #374151;
  border-color: #cbd5e1;
  transform: translateY(-1px);
}

.header-title h1 {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 4px 0;
}

.header-title p {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.btn-edit,
.btn-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.2s ease;
}

.btn-edit {
  background: #fef3c7;
  color: #d97706;
  border: 1px solid #fbbf24;
}

.btn-edit:hover:not(:disabled) {
  background: #fde68a;
  color: #b45309;
  border-color: #f59e0b;
  transform: translateY(-1px);
}

.btn-toggle {
  background: #dcfce7;
  color: #166534;
  border: 1px solid #bbf7d0;
}

.btn-toggle:hover:not(:disabled) {
  background: #bbf7d0;
  color: #14532d;
  border-color: #86efac;
  transform: translateY(-1px);
}

.btn-edit:disabled,
.btn-toggle:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

/* Loading State */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.loading-spinner {
  font-size: 32px;
  color: #f97316;
  margin-bottom: 16px;
}

.loading-container p {
  color: #64748b;
  font-size: 16px;
  margin: 0;
}

/* Detail Content */
.detail-content {
  display: flex;
  flex-direction: column;
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}

.card-header h2 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.btn-view-ca,
.btn-view-nhan-vien,
.btn-view-all {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: #f97316;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 600;
  transition: all 0.2s ease;
}

.btn-view-ca:hover,
.btn-view-nhan-vien:hover,
.btn-view-all:hover {
  background: #ea580c;
  transform: translateY(-1px);
}

.card-content {
  padding: 24px;
}

/* Info Grid */
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 24px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item label {
  font-weight: 600;
  font-size: 14px;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-value {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.date-value {
  font-family: 'Courier New', monospace;
  background: #f1f5f9;
  padding: 8px 12px;
  border-radius: 6px;
  display: inline-block;
  width: fit-content;
}

.time-range {
  font-family: 'Courier New', monospace;
  background: #fef3c7;
  color: #d97706;
  padding: 8px 12px;
  border-radius: 6px;
  display: inline-block;
  width: fit-content;
}

.duration-value {
  color: #f97316;
  font-size: 18px;
}

/* Status Badge */
.status-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  display: inline-block;
  width: fit-content;
}

.badge-active {
  background: #dcfce7;
  color: #166534;
}

.badge-inactive {
  background: #fef2f2;
  color: #dc2626;
}

.badge-pending {
  background: #fef3c7;
  color: #d97706;
}

.badge-completed {
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

/* Description */
.description-text {
  font-size: 16px;
  line-height: 1.6;
  color: #374151;
  background: #f8fafc;
  padding: 16px;
  border-radius: 8px;
  border-left: 4px solid #f97316;
}

/* Ca Info Grid */
.ca-info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.ca-info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ca-info-item.full-width {
  grid-column: 1 / -1;
}

.ca-info-label {
  font-weight: 600;
  font-size: 12px;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.ca-info-value {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

.time-value {
  font-family: 'Courier New', monospace;
  background: #f1f5f9;
  padding: 6px 10px;
  border-radius: 4px;
  display: inline-block;
  width: fit-content;
}

/* Nhan Vien Info */
.nhan-vien-info {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.nhan-vien-avatar {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #f97316, #ea580c);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.nhan-vien-details {
  flex: 1;
}

.nhan-vien-name {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 4px;
}

.nhan-vien-role {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

/* Statistics */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  transition: all 0.2s ease;
}

.stat-item:hover {
  background: #f1f5f9;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #f97316, #ea580c);
  color: white;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

/* Giao Ca List */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #9ca3af;
}

.empty-state font-awesome-icon {
  font-size: 32px;
  margin-bottom: 12px;
}

.empty-state p {
  margin: 0;
  font-size: 16px;
}

.giao-ca-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.giao-ca-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  transition: all 0.2s ease;
}

.giao-ca-item:hover {
  background: #f1f5f9;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.giao-ca-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.giao-ca-title {
  font-weight: 600;
  color: #1e293b;
  font-size: 14px;
}

.giao-ca-date {
  color: #64748b;
  font-size: 12px;
}

.giao-ca-status {
  display: flex;
  align-items: center;
}

/* Error State */
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.error-icon {
  font-size: 48px;
  color: #ef4444;
  margin-bottom: 16px;
}

.error-container h3 {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 8px 0;
}

.error-container p {
  color: #64748b;
  font-size: 16px;
  margin: 0 0 24px 0;
}

.btn-primary {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #f97316, #ea580c);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(249, 115, 22, 0.3);
}

.btn-primary:hover {
  background: linear-gradient(135deg, #ea580c, #dc2626);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.4);
}

/* Responsive Design */
@media (max-width: 768px) {
  .content {
    padding: 16px;
  }
  
  .detail-header {
    padding: 16px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .header-left {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: stretch;
  }
  
  .btn-edit,
  .btn-toggle {
    flex: 1;
    justify-content: center;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .ca-info-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .card-content {
    padding: 16px;
  }
  
  .nhan-vien-info {
    padding: 16px;
  }
}

@media (max-width: 480px) {
  .content {
    padding: 12px;
  }
  
  .detail-header {
    padding: 12px;
  }
  
  .header-title h1 {
    font-size: 20px;
  }
  
  .stat-item {
    padding: 16px;
  }
  
  .stat-icon {
    width: 40px;
    height: 40px;
    font-size: 16px;
  }
  
  .stat-value {
    font-size: 20px;
  }
  
  .nhan-vien-avatar {
    width: 50px;
    height: 50px;
    font-size: 20px;
  }
}
</style>

























