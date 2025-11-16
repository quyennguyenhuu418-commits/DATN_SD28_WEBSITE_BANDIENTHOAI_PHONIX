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
              <h1>Chi Tiết Ca Làm Việc</h1>
              <p>Thông tin chi tiết về ca làm việc</p>
            </div>
          </div>
          <div class="header-actions">
            <button @click="editCa" class="btn-edit" :disabled="loading">
              <font-awesome-icon :icon="['fas', 'edit']" />
              Chỉnh sửa
            </button>
            <button @click="toggleStatus" class="btn-toggle" :disabled="loading">
              <font-awesome-icon :icon="['fas', 'power-off']" />
              {{ caData?.trangThai === 1 ? 'Tắt' : 'Bật' }}
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
      <div v-else-if="caData" class="detail-content">
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
                <label>Tên Ca Làm Việc</label>
                <div class="info-value">{{ caData.tenCa }}</div>
              </div>
              <div class="info-item">
                <label>Giờ Bắt Đầu</label>
                <div class="info-value time-value">{{ formatTime(caData.gioBatDau) }}</div>
              </div>
              <div class="info-item">
                <label>Giờ Kết Thúc</label>
                <div class="info-value time-value">{{ formatTime(caData.gioKetThuc) }}</div>
              </div>
              <div class="info-item">
                <label>Thời Gian Làm Việc</label>
                <div class="info-value duration-value">{{ calculateDuration() }}</div>
              </div>
              <div class="info-item">
                <label>Trạng Thái</label>
                <div class="info-value">
                  <span class="status-badge" :class="getStatusBadgeClass()">
                    {{ getStatusText() }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Description Card -->
        <div v-if="caData.moTa" class="info-card">
          <div class="card-header">
            <h2>
              <font-awesome-icon :icon="['fas', 'file-text']" />
              Mô Tả
            </h2>
          </div>
          <div class="card-content">
            <div class="description-text">{{ caData.moTa }}</div>
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
                  <font-awesome-icon :icon="['fas', 'users']" />
                </div>
                <div class="stat-content">
                  <div class="stat-value">{{ statistics.totalPhanCa }}</div>
                  <div class="stat-label">Phân Ca</div>
                </div>
              </div>
              <div class="stat-item">
                <div class="stat-icon">
                  <font-awesome-icon :icon="['fas', 'calendar-check']" />
                </div>
                <div class="stat-content">
                  <div class="stat-value">{{ statistics.activePhanCa }}</div>
                  <div class="stat-label">Đang Hoạt Động</div>
                </div>
              </div>
              <div class="stat-item">
                <div class="stat-icon">
                  <font-awesome-icon :icon="['fas', 'exchange-alt']" />
                </div>
                <div class="stat-content">
                  <div class="stat-value">{{ statistics.totalGiaoCa }}</div>
                  <div class="stat-label">Giao Ca</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Recent Phan Ca -->
        <div class="info-card">
          <div class="card-header">
            <h2>
              <font-awesome-icon :icon="['fas', 'calendar']" />
              Phân Ca Gần Đây
            </h2>
            <button @click="viewAllPhanCa" class="btn-view-all">
              Xem tất cả
              <font-awesome-icon :icon="['fas', 'arrow-right']" />
            </button>
          </div>
          <div class="card-content">
            <div v-if="recentPhanCa.length === 0" class="empty-state">
              <font-awesome-icon :icon="['fas', 'calendar-times']" />
              <p>Chưa có phân ca nào</p>
            </div>
            <div v-else class="phan-ca-list">
              <div v-for="phanCa in recentPhanCa" :key="phanCa.id" class="phan-ca-item">
                <div class="phan-ca-info">
                  <div class="phan-ca-nhan-vien">{{ phanCa.nhanVien?.hoTen || 'N/A' }}</div>
                  <div class="phan-ca-date">{{ formatDate(phanCa.ngayLamViec) }}</div>
                </div>
                <div class="phan-ca-status">
                  <span class="status-badge" :class="phanCa.trangThai === 1 ? 'badge-active' : 'badge-inactive'">
                    {{ phanCa.trangThai === 1 ? 'Hoạt động' : 'Ngừng' }}
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
        <h3>Không tìm thấy ca làm việc</h3>
        <p>Ca làm việc này có thể đã bị xóa hoặc không tồn tại.</p>
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

interface Ca {
  id: number
  tenCa: string
  gioBatDau: string
  gioKetThuc: string
  moTa?: string
  trangThai: number
}

interface PhanCa {
  id: number
  nhanVienId: number
  nhanVien?: {
    id: number
    hoTen: string
  }
  ngayLamViec: string
  trangThai: number
}

interface Statistics {
  totalPhanCa: number
  activePhanCa: number
  totalGiaoCa: number
}

const caData = ref<Ca | null>(null)
const recentPhanCa = ref<PhanCa[]>([])
const statistics = ref<Statistics>({
  totalPhanCa: 0,
  activePhanCa: 0,
  totalGiaoCa: 0
})
const loading = ref(false)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

const caId = computed(() => route.params.id as string)

async function loadCaDetail() {
  loading.value = true
  try {
    const { data } = await api.get(`/api/ca/${caId.value}`)
    caData.value = data
    await loadStatistics()
    await loadRecentPhanCa()
  } catch (error) {
    console.error('Lỗi khi tải chi tiết ca:', error)
    toastRef.value?.error('Lỗi', 'Không thể tải thông tin ca')
  } finally {
    loading.value = false
  }
}

async function loadStatistics() {
  try {
    // Load statistics for this ca
    const [phanCaRes, giaoCaRes] = await Promise.all([
      api.get(`/api/phan-ca/by-ca/${caId.value}`),
      api.get(`/api/giao-ca/by-ca/${caId.value}`)
    ])
    
    const phanCaList = phanCaRes.data || []
    const giaoCaList = giaoCaRes.data || []
    
    statistics.value = {
      totalPhanCa: phanCaList.length,
      activePhanCa: phanCaList.filter((p: PhanCa) => p.trangThai === 1).length,
      totalGiaoCa: giaoCaList.length
    }
  } catch (error) {
    console.error('Lỗi khi tải thống kê:', error)
  }
}

async function loadRecentPhanCa() {
  try {
    const { data } = await api.get(`/api/phan-ca/by-ca/${caId.value}?limit=5`)
    recentPhanCa.value = data || []
  } catch (error) {
    console.error('Lỗi khi tải phân ca gần đây:', error)
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

function calculateDuration(): string {
  if (!caData.value?.gioBatDau || !caData.value?.gioKetThuc) return '-'
  
  const start = new Date(`2000-01-01T${caData.value.gioBatDau}`)
  const end = new Date(`2000-01-01T${caData.value.gioKetThuc}`)
  
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
  return caData.value?.trangThai === 1 ? 'Hoạt động' : 'Ngừng hoạt động'
}

function getStatusBadgeClass(): string {
  return caData.value?.trangThai === 1 ? 'badge-active' : 'badge-inactive'
}

async function toggleStatus() {
  if (!caData.value) return
  
  try {
    await api.post(`/api/ca/${caId.value}/toggle-status`)
    caData.value.trangThai = caData.value.trangThai === 1 ? 0 : 1
    
    const message = caData.value.trangThai === 1 ? 'Đã kích hoạt ca!' : 'Đã tắt ca!'
    toastRef.value?.success('Thành công', message)
  } catch (error) {
    console.error('Lỗi khi cập nhật trạng thái:', error)
    toastRef.value?.error('Lỗi', 'Không thể cập nhật trạng thái')
  }
}

function editCa() {
  router.push(`/ca/form/${caId.value}`)
}

function viewAllPhanCa() {
  router.push(`/phan-ca?ca=${caId.value}`)
}

function goBack() {
  router.push('/ca')
}

onMounted(() => {
  loadCaDetail()
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

.time-value {
  font-family: 'Courier New', monospace;
  background: #f1f5f9;
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

/* Phan Ca List */
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

.phan-ca-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.phan-ca-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  transition: all 0.2s ease;
}

.phan-ca-item:hover {
  background: #f1f5f9;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.phan-ca-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.phan-ca-nhan-vien {
  font-weight: 600;
  color: #1e293b;
  font-size: 14px;
}

.phan-ca-date {
  color: #64748b;
  font-size: 12px;
}

.phan-ca-status {
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
  
  .stats-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .card-content {
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
}
</style>







































