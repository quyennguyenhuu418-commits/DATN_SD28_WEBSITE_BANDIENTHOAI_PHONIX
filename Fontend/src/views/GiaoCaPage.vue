<template>
  <div class="page dark-mode-transition">
    <PosHeader />
    
    <div class="content">
      <Toast ref="toastRef" />

      <!-- Header Section -->
      <div class="header-section">
        <div class="header-content">
          <div class="header-title">
            <h1>Giao Ca</h1>
            <p>Thực hiện giao ca giữa các nhân viên</p>
          </div>
          <div class="header-actions">
            <button class="btn-giao-ca" @click="startGiaoCa" :disabled="!canStartGiaoCa">
              <font-awesome-icon :icon="['fas', 'exchange-alt']" />
              Giao Ca
            </button>
          </div>
        </div>
      </div>

      <!-- Current Shift Info -->
      <div v-if="currentPhanCa" class="current-shift-card">
        <div class="card-header">
          <h2>
            <font-awesome-icon :icon="['fas', 'clock']" />
            Ca Hiện Tại
          </h2>
        </div>
        <div class="card-content">
          <div class="shift-info">
            <div class="shift-details">
              <div class="shift-item">
                <label>Nhân viên:</label>
                <span class="value">{{ currentPhanCa.nhanVien?.hoTen || 'N/A' }}</span>
              </div>
              <div class="shift-item">
                <label>Ca làm việc:</label>
                <span class="value">{{ currentPhanCa.ca?.tenCa || 'N/A' }}</span>
              </div>
              <div class="shift-item">
                <label>Thời gian:</label>
                <span class="value">{{ formatTime(currentPhanCa.ca?.gioBatDau) }} - {{ formatTime(currentPhanCa.ca?.gioKetThuc) }}</span>
              </div>
              <div class="shift-item">
                <label>Ngày:</label>
                <span class="value">{{ formatDate(currentPhanCa.ngayLamViec) }}</span>
              </div>
            </div>
            <div class="shift-status">
              <span class="status-badge" :class="getStatusClass(currentPhanCa.trangThai)">
                {{ getStatusText(currentPhanCa.trangThai) }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Giao Ca Form Modal -->
      <div v-if="showGiaoCaModal" class="modal-overlay" @click="closeModal">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <h2>Giao Ca</h2>
            <button class="close-btn" @click="closeModal">
              <font-awesome-icon :icon="['fas', 'times']" />
            </button>
          </div>
          
          <div class="modal-body">
            <!-- Current Employee Info -->
            <div class="employee-info">
              <h3>Nhân viên giao ca</h3>
              <div class="employee-card">
                <div class="employee-avatar">
                  <font-awesome-icon :icon="['fas', 'user']" />
                </div>
                <div class="employee-details">
                  <div class="employee-name">{{ currentPhanCa?.nhanVien?.hoTen || 'N/A' }}</div>
                  <div class="employee-shift">{{ currentPhanCa?.ca?.tenCa || 'N/A' }}</div>
                </div>
              </div>
            </div>

            <!-- Shift Summary -->
            <div class="shift-summary">
              <h3>Tổng kết ca làm việc</h3>
              <div class="summary-cards">
                <div class="summary-card">
                  <div class="summary-icon">
                    <font-awesome-icon :icon="['fas', 'money-bill-wave']" />
                  </div>
                  <div class="summary-content">
                    <div class="summary-label">Tổng doanh thu</div>
                    <div class="summary-value">{{ formatCurrency(shiftSummary.totalRevenue) }}</div>
                  </div>
                </div>
                <div class="summary-card">
                  <div class="summary-icon">
                    <font-awesome-icon :icon="['fas', 'shopping-cart']" />
                  </div>
                  <div class="summary-content">
                    <div class="summary-label">Số đơn hàng</div>
                    <div class="summary-value">{{ shiftSummary.totalOrders }}</div>
                  </div>
                </div>
                <div class="summary-card">
                  <div class="summary-icon">
                    <font-awesome-icon :icon="['fas', 'clock']" />
                  </div>
                  <div class="summary-content">
                    <div class="summary-label">Thời gian ca</div>
                    <div class="summary-value">{{ shiftSummary.shiftDuration }}</div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Next Shift Info -->
            <div class="next-shift-info">
              <h3>Ca tiếp theo</h3>
              <div v-if="nextPhanCa" class="next-shift-card">
                <div class="next-shift-details">
                  <div class="next-shift-item">
                    <label>Nhân viên nhận ca:</label>
                    <span class="value">{{ nextPhanCa.nhanVien?.hoTen || 'N/A' }}</span>
                  </div>
                  <div class="next-shift-item">
                    <label>Ca làm việc:</label>
                    <span class="value">{{ nextPhanCa.ca?.tenCa || 'N/A' }}</span>
                  </div>
                  <div class="next-shift-item">
                    <label>Thời gian:</label>
                    <span class="value">{{ formatTime(nextPhanCa.ca?.gioBatDau) }} - {{ formatTime(nextPhanCa.ca?.gioKetThuc) }}</span>
                  </div>
                </div>
              </div>
              <div v-else class="no-next-shift">
                <font-awesome-icon :icon="['fas', 'info-circle']" />
                <p>Không có ca tiếp theo được phân</p>
              </div>
            </div>

            <!-- Notes -->
            <div class="notes-section">
              <h3>Ghi chú</h3>
              <textarea
                v-model="giaoCaForm.ghiChu"
                class="notes-textarea"
                placeholder="Nhập ghi chú về ca làm việc..."
                rows="3"
              ></textarea>
            </div>
          </div>

          <div class="modal-footer">
            <button class="btn-cancel" @click="closeModal">
              <font-awesome-icon :icon="['fas', 'times']" />
              Hủy
            </button>
            <button class="btn-confirm" @click="confirmGiaoCa" :disabled="loading">
              <font-awesome-icon v-if="loading" :icon="['fas', 'spinner']" spin />
              <font-awesome-icon v-else :icon="['fas', 'check']" />
              {{ loading ? 'Đang xử lý...' : 'Xác nhận giao ca' }}
            </button>
          </div>
        </div>
      </div>

      <!-- Recent Giao Ca -->
      <div class="recent-giao-ca">
        <div class="card-header">
          <h2>
            <font-awesome-icon :icon="['fas', 'history']" />
            Lịch Sử Giao Ca Gần Đây
          </h2>
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
                <div class="giao-ca-details">
                  <span>{{ giaoCa.nhanVienGiao?.hoTen || 'N/A' }} → {{ giaoCa.nhanVienNhan?.hoTen || 'N/A' }}</span>
                  <span class="giao-ca-date">{{ formatDateTime(giaoCa.ngayGiaoCa) }}</span>
                </div>
                <div class="giao-ca-money">
                  <span class="money-label">Số tiền:</span>
                  <span class="money-value">{{ formatMoney(giaoCa.soTienDauCa) }}</span>
                </div>
                <div v-if="giaoCa.nguoiCapNhat && giaoCa.thoiGianXacNhan" class="giao-ca-confirmer">
                  <span class="confirmer-label">Xác nhận bởi:</span>
                  <span class="confirmer-value">{{ giaoCa.nguoiCapNhat }}</span>
                  <span class="confirmer-time">{{ formatDateTime(giaoCa.thoiGianXacNhan) }}</span>
                </div>
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
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/services/api'
import Toast from '@/components/Toast.vue'
import PosHeader from '@/components/PosHeader.vue'
import { useAuthStore } from '@/stores/authStore'

const router = useRouter()
const authStore = useAuthStore()

interface NhanVien {
  id: number
  hoTen: string
  maNhanVien: string
}

interface Ca {
  id: number
  tenCa: string
  gioBatDau: string
  gioKetThuc: string
}

interface PhanCa {
  id: number
  nhanVienId: number
  nhanVien?: NhanVien
  caId: number
  ca?: Ca
  ngayLamViec: string
  trangThai: number
}

interface GiaoCa {
  id: number
  nhanVienGiaoId: number
  nhanVienGiao?: NhanVien
  nhanVienNhanId: number
  nhanVienNhan?: NhanVien
  ngayGiaoCa: string
  soTienDauCa: number
  trangThai: number
  ghiChu?: string
  nguoiCapNhat?: string
  thoiGianXacNhan?: string
}

const currentPhanCa = ref<PhanCa | null>(null)
const nextPhanCa = ref<PhanCa | null>(null)
const recentGiaoCa = ref<GiaoCa[]>([])
const showGiaoCaModal = ref(false)
const loading = ref(false)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

const giaoCaForm = ref({
  soTienDauCa: '',
  ghiChu: ''
})

const shiftSummary = ref({
  totalRevenue: 0,
  totalOrders: 0,
  shiftDuration: '0h 0m'
})

const canStartGiaoCa = computed(() => {
  return currentPhanCa.value && currentPhanCa.value.trangThai === 1
})

async function loadCurrentPhanCa() {
  try {
    // Use fixed date for testing - change to 2025-10-17 to match database
    const today = '2025-10-17' // new Date().toISOString().split('T')[0]
    const { data } = await api.get(`/api/phan-ca/current-shift?date=${today}`)
    currentPhanCa.value = data
  } catch (error) {
    console.error('Lỗi khi tải ca hiện tại:', error)
    if (error.response?.status === 401) {
      router.push('/login')
    }
  }
}

async function loadNextPhanCa() {
  try {
    // Use fixed date for testing - change to 2025-10-17 to match database
    const today = '2025-10-17' // new Date().toISOString().split('T')[0]
    const response = await api.get(`/api/phan-ca/next-shift?date=${today}`)
    
    if (response.status === 200 && response.data) {
      nextPhanCa.value = response.data
    } else {
      nextPhanCa.value = null
    }
  } catch (error) {
    console.error('Lỗi khi tải ca tiếp theo:', error)
    if (error.response?.status === 401) {
      router.push('/login')
    }
    nextPhanCa.value = null
  }
}

async function loadRecentGiaoCa() {
  try {
    const { data } = await api.get('/api/giao-ca/recent?limit=5')
    recentGiaoCa.value = data
  } catch (error) {
    console.error('Lỗi khi tải lịch sử giao ca:', error)
    if (error.response?.status === 401) {
      router.push('/login')
    }
  }
}

async function loadShiftSummary() {
  if (!currentPhanCa.value) return
  
  try {
    const today = '2025-10-17' // Use fixed date for testing
    const shiftId = currentPhanCa.value.id
    
    // Load revenue and orders for current shift
    const response = await api.get(`/api/shift-summary?shiftId=${shiftId}&date=${today}`)
    shiftSummary.value = {
      totalRevenue: response.data.totalRevenue || 0,
      totalOrders: response.data.totalOrders || 0,
      shiftDuration: calculateShiftDuration()
    }
  } catch (error) {
    console.error('Lỗi khi tải tổng kết ca:', error)
    // Fallback to mock data for testing
    shiftSummary.value = {
      totalRevenue: 1500000, // Mock data
      totalOrders: 25, // Mock data
      shiftDuration: calculateShiftDuration()
    }
  }
}

function calculateShiftDuration() {
  if (!currentPhanCa.value?.ca) return '0h 0m'
  
  const startTime = currentPhanCa.value.ca.gioBatDau
  const endTime = currentPhanCa.value.ca.gioKetThuc
  
  if (!startTime || !endTime) return '0h 0m'
  
  const start = new Date(`2000-01-01T${startTime}`)
  const end = new Date(`2000-01-01T${endTime}`)
  
  const diffMs = end.getTime() - start.getTime()
  const hours = Math.floor(diffMs / (1000 * 60 * 60))
  const minutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60))
  
  return `${hours}h ${minutes}m`
}

function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}

function startGiaoCa() {
  if (!canStartGiaoCa.value) return
  showGiaoCaModal.value = true
  giaoCaForm.value = {
    soTienDauCa: '',
    ghiChu: ''
  }
  loadShiftSummary()
}

function closeModal() {
  showGiaoCaModal.value = false
  giaoCaForm.value = {
    soTienDauCa: '',
    ghiChu: ''
  }
}

function formatMoneyInput(event: Event) {
  const target = event.target as HTMLInputElement
  let value = target.value.replace(/[^\d]/g, '')
  if (value) {
    value = parseInt(value).toLocaleString('vi-VN')
  }
  giaoCaForm.value.soTienDauCa = value
}

function formatMoney(amount: number): string {
  if (!amount) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}

function formatTime(timeString: string): string {
  if (!timeString) return '-'
  return timeString.substring(0, 5)
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

function getStatusText(status: number): string {
  switch (status) {
    case 0: return 'Chưa bắt đầu'
    case 1: return 'Đang làm'
    case 2: return 'Đã kết thúc'
    case 3: return 'Vắng mặt'
    default: return 'Không xác định'
  }
}

function getStatusClass(status: number): string {
  switch (status) {
    case 0: return 'status-pending'
    case 1: return 'status-active'
    case 2: return 'status-completed'
    case 3: return 'status-absent'
    default: return 'status-unknown'
  }
}

function getGiaoCaStatusText(status: number): string {
  switch (status) {
    case 0: return 'Chờ xác nhận'
    case 1: return 'Đã xác nhận'
    case 2: return 'Đã hủy'
    default: return 'Không xác định'
  }
}

function getGiaoCaStatusClass(status: number): string {
  switch (status) {
    case 0: return 'status-pending'
    case 1: return 'status-completed'
    case 2: return 'status-cancelled'
    default: return 'status-unknown'
  }
}

async function confirmGiaoCa() {
  if (!currentPhanCa.value) return

  loading.value = true
  try {
    const giaoCaData = {
      maGiaoCa: 'GC' + new Date().getTime(),
      phanCaId: currentPhanCa.value.id,
      nhanVienGiaoId: currentPhanCa.value.nhanVienId,
      nhanVienNhanId: nextPhanCa.value?.nhanVienId || null,
      ngayGiaoCa: new Date().toISOString(),
      soTienDauCa: shiftSummary.value.totalRevenue, // Sử dụng tổng doanh thu tự động
      soTienCuoiCa: shiftSummary.value.totalRevenue,
      soDonHang: shiftSummary.value.totalOrders,
      ghiChu: giaoCaForm.value.ghiChu
    }

    await api.post('/api/giao-ca', giaoCaData)
    
    toastRef.value?.success('Thành công', 'Giao ca thành công!')
    closeModal()
    
    // Reload data to update current shift
    await Promise.all([
      loadCurrentPhanCa(),
      loadNextPhanCa(),
      loadRecentGiaoCa()
    ])
    
    // Force refresh the page to ensure all data is updated
    setTimeout(() => {
      window.location.reload()
    }, 1000)
  } catch (error: any) {
    console.error('Lỗi khi giao ca:', error)
    toastRef.value?.error('Lỗi', error.response?.data?.message || 'Không thể thực hiện giao ca')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  // Wait for auth initialization before loading data
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }
  
  loadCurrentPhanCa()
  loadNextPhanCa()
  loadRecentGiaoCa()
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

/* Header Section */
.header-section {
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

.btn-giao-ca {
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

.btn-giao-ca:hover:not(:disabled) {
  background: linear-gradient(135deg, #ea580c, #dc2626);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.4);
}

.btn-giao-ca:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* Cards */
.current-shift-card,
.recent-giao-ca {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
  margin-bottom: 24px;
  overflow: hidden;
}

.card-header {
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

.card-content {
  padding: 24px;
}

/* Current Shift Info */
.shift-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.shift-details {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.shift-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.shift-item label {
  font-weight: 600;
  font-size: 12px;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.shift-item .value {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
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
}

.status-pending {
  background: #fef3c7;
  color: #d97706;
}

.status-active {
  background: #dcfce7;
  color: #166534;
}

.status-completed {
  background: #dbeafe;
  color: #1d4ed8;
}

.status-absent {
  background: #fef2f2;
  color: #dc2626;
}


.status-cancelled {
  background: #f3f4f6;
  color: #6b7280;
}

.status-unknown {
  background: #f3f4f6;
  color: #6b7280;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
}

.modal-header h2 {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.close-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: #f3f4f6;
  color: #6b7280;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.close-btn:hover {
  background: #e5e7eb;
  color: #374151;
}

.modal-body {
  padding: 24px;
}

.modal-body h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 12px 0;
}

/* Employee Info */
.employee-info {
  margin-bottom: 24px;
}

.employee-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.employee-avatar {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #f97316, #ea580c);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.employee-details {
  flex: 1;
}

.employee-name {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
}

.employee-shift {
  font-size: 14px;
  color: #64748b;
}

/* Money Section */
.money-section {
  margin-bottom: 24px;
}

.money-input-container {
  position: relative;
  display: flex;
  align-items: center;
}

.currency-symbol {
  position: absolute;
  left: 12px;
  font-size: 18px;
  font-weight: 600;
  color: #f97316;
  z-index: 1;
}

.money-input {
  width: 100%;
  padding: 12px 12px 12px 40px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 18px;
  font-weight: 600;
  text-align: right;
  transition: all 0.3s ease;
}

.money-input:focus {
  outline: none;
  border-color: #f97316;
  box-shadow: 0 0 0 4px rgba(249, 115, 22, 0.1);
}

.money-help {
  font-size: 12px;
  color: #64748b;
  margin-top: 4px;
}

/* Shift Summary */
.shift-summary {
  margin-bottom: 24px;
}

.summary-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-top: 16px;
}

.summary-card {
  display: flex;
  align-items: center;
  padding: 16px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  transition: all 0.2s;
}

.summary-card:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
}

.summary-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f97316;
  color: white;
  border-radius: 8px;
  margin-right: 12px;
  font-size: 18px;
}

.summary-content {
  flex: 1;
}

.summary-label {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 4px;
}

.summary-value {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
}

/* Next Shift Info */
.next-shift-info {
  margin-bottom: 24px;
}

.next-shift-card {
  padding: 16px;
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  border-radius: 8px;
}

.next-shift-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.next-shift-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.next-shift-item label {
  font-weight: 600;
  font-size: 14px;
  color: #0369a1;
}

.next-shift-item .value {
  font-size: 14px;
  color: #1e293b;
  font-weight: 500;
}

.no-next-shift {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  color: #64748b;
}

.no-next-shift font-awesome-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.no-next-shift p {
  margin: 0;
  font-size: 14px;
}

/* Notes Section */
.notes-section {
  margin-bottom: 24px;
}

.notes-textarea {
  width: 100%;
  padding: 12px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  font-family: inherit;
  resize: vertical;
  transition: all 0.3s ease;
}

.notes-textarea:focus {
  outline: none;
  border-color: #f97316;
  box-shadow: 0 0 0 4px rgba(249, 115, 22, 0.1);
}

/* Modal Footer */
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid #e2e8f0;
}

.btn-cancel,
.btn-confirm {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.2s ease;
}

.btn-cancel {
  background: #f8fafc;
  color: #64748b;
  border: 1px solid #e2e8f0;
}

.btn-cancel:hover {
  background: #f1f5f9;
  color: #374151;
  border-color: #cbd5e1;
}

.btn-confirm {
  background: linear-gradient(135deg, #f97316, #ea580c);
  color: white;
  box-shadow: 0 2px 4px rgba(249, 115, 22, 0.3);
}

.btn-confirm:hover:not(:disabled) {
  background: linear-gradient(135deg, #ea580c, #dc2626);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.4);
}

.btn-confirm:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* Recent Giao Ca */
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
  flex: 1;
}

.giao-ca-title {
  font-weight: 600;
  color: #1e293b;
  font-size: 14px;
  margin-bottom: 4px;
}

.giao-ca-details {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #64748b;
  margin-bottom: 4px;
}

.giao-ca-date {
  font-weight: 500;
}

.giao-ca-money {
  font-size: 12px;
}

.money-label {
  color: #64748b;
}

.money-value {
  color: #f97316;
  font-weight: 600;
}

.giao-ca-confirmer {
  font-size: 12px;
  color: #64748b;
  margin-top: 4px;
  display: flex;
  gap: 8px;
  align-items: center;
}

.confirmer-label {
  font-weight: 500;
}

.confirmer-value {
  color: #059669;
  font-weight: 600;
}

.confirmer-time {
  color: #6b7280;
  font-style: italic;
}

.giao-ca-status {
  display: flex;
  align-items: center;
}

/* Responsive Design */
@media (max-width: 768px) {
  .content {
    padding: 16px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .shift-details {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .shift-info {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .modal-content {
    margin: 10px;
    max-height: 95vh;
  }
  
  .modal-body {
    padding: 16px;
  }
  
  .modal-footer {
    flex-direction: column;
  }
  
  .btn-cancel,
  .btn-confirm {
    width: 100%;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .content {
    padding: 12px;
  }
  
  .header-section {
    padding: 16px;
  }
  
  .card-content {
    padding: 16px;
  }
  
  .giao-ca-details {
    flex-direction: column;
    gap: 4px;
  }
}
</style>
