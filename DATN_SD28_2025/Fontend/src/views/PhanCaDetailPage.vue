<template>
  <div class="phan-ca-detail-page dark-mode-transition">
    <PosHeader />
    
    <div class="detail-container">
      <div class="detail-header">
        <div class="header-left">
          <button class="btn-back" @click="goBack">
            <font-awesome-icon :icon="['fas', 'arrow-left']" />
            Quay lại
          </button>
          <h1>Chi tiết Phân Ca</h1>
        </div>
        <div class="header-actions">
          <button 
            v-if="phanCa && phanCa.trangThai !== 2" 
            class="btn-edit" 
            @click="editPhanCa"
          >
            <font-awesome-icon :icon="['fas', 'edit']" />
            Chỉnh sửa
          </button>
          <button 
            v-if="phanCa && phanCa.trangThai !== 2" 
            class="btn-delete" 
            @click="deletePhanCa"
          >
            <font-awesome-icon :icon="['fas', 'trash']" />
            Xóa
          </button>
        </div>
      </div>

      <div class="detail-content" v-if="phanCa && !loading">
        <div class="detail-card">
          <div class="card-header">
            <h2>Thông tin cơ bản</h2>
          </div>
          <div class="card-body">
            <div class="info-grid">
              <div class="info-item">
                <label>ID:</label>
                <span class="info-value">{{ phanCa.id }}</span>
              </div>
              <div class="info-item">
                <label>Nhân viên:</label>
                <span class="info-value">{{ phanCa.nhanVien?.hoTen || 'N/A' }} ({{ phanCa.nhanVien?.maNhanVien || 'N/A' }})</span>
              </div>
              <div class="info-item">
                <label>Ca làm việc:</label>
                <span class="info-value">{{ phanCa.ca?.tenCa || 'N/A' }} ({{ formatTime(phanCa.ca?.gioBatDau) }} - {{ formatTime(phanCa.ca?.gioKetThuc) }})</span>
              </div>
              <div class="info-item">
                <label>Ngày làm việc:</label>
                <span class="info-value">{{ formatDate(phanCa.ngayLamViec) }}</span>
              </div>
              <div class="info-item">
                <label>Trạng thái:</label>
                <span class="info-value">
                  <span class="status-badge" :class="getStatusClass(phanCa.trangThai)">
                    {{ getStatusText(phanCa.trangThai) }}
                  </span>
                </span>
              </div>
              <div class="info-item">
                <label>Giờ bắt đầu thực tế:</label>
                <span class="info-value">{{ formatDateTime(phanCa.gioBatDauThucTe) || 'Chưa bắt đầu' }}</span>
              </div>
              <div class="info-item">
                <label>Giờ kết thúc thực tế:</label>
                <span class="info-value">{{ formatDateTime(phanCa.gioKetThucThucTe) || 'Chưa kết thúc' }}</span>
              </div>
              <div class="info-item">
                <label>Ghi chú:</label>
                <span class="info-value">{{ phanCa.ghiChu || 'Không có' }}</span>
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
                <span class="info-value">{{ formatDateTime(phanCa.ngayTao) }}</span>
              </div>
              <div class="info-item">
                <label>Ngày cập nhật:</label>
                <span class="info-value">{{ formatDateTime(phanCa.ngayCapNhat) }}</span>
              </div>
              <div class="info-item">
                <label>Người tạo:</label>
                <span class="info-value">{{ phanCa.nguoiTao || 'Hệ thống' }}</span>
              </div>
              <div class="info-item">
                <label>Người cập nhật:</label>
                <span class="info-value">{{ phanCa.nguoiCapNhat || 'Hệ thống' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="loading-container" v-else-if="loading">
        <div class="loading-spinner"></div>
        <p>Đang tải thông tin phân ca...</p>
      </div>

      <div class="error-container" v-else-if="error">
        <div class="error-icon">
          <font-awesome-icon :icon="['fas', 'exclamation-triangle']" />
        </div>
        <h3>Không thể tải thông tin phân ca</h3>
        <p>{{ error }}</p>
        <button class="btn-retry" @click="loadPhanCa">
          <font-awesome-icon :icon="['fas', 'redo']" />
          Thử lại
        </button>
      </div>
    </div>

    <Toast ref="toastRef" />
    <ConfirmModal
      :show="showConfirmModal"
      :title="confirmTitle"
      :message="confirmMessage"
      @confirm="handleConfirm"
      @cancel="handleCancel"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '@/services/api'
import Toast from '@/components/Toast.vue'
import PosHeader from '@/components/PosHeader.vue'
import ConfirmModal from '@/components/ConfirmModal.vue'

const router = useRouter()
const route = useRoute()
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

const phanCa = ref<any>(null)
const loading = ref(false)
const error = ref('')
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const pendingAction = ref<(() => void) | null>(null)

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

function formatTime(time: string) {
  if (!time) return ''
  return time.substring(0, 5)
}

function getStatusText(trangThai: number | null): string {
  if (trangThai === null) return 'Chưa bắt đầu'
  switch (trangThai) {
    case 0: return 'Chưa bắt đầu'
    case 1: return 'Đang làm'
    case 2: return 'Đã kết thúc'
    case 3: return 'Vắng mặt'
    default: return 'Không xác định'
  }
}

function getStatusClass(trangThai: number | null): string {
  if (trangThai === null) return 'badge-pending'
  switch (trangThai) {
    case 0: return 'badge-pending'
    case 1: return 'badge-active'
    case 2: return 'badge-completed'
    case 3: return 'badge-absent'
    default: return 'badge-unknown'
  }
}

async function loadPhanCa() {
  const phanCaId = route.params.id
  if (!phanCaId) {
    error.value = 'Không tìm thấy ID phân ca'
    return
  }

  try {
    loading.value = true
    error.value = ''
    
    const response = await api.get(`/api/phan-ca/${phanCaId}`)
    phanCa.value = response.data
  } catch (err: any) {
    if (err.response?.status === 404) {
      error.value = 'Không tìm thấy phân ca với ID này'
    } else if (err.response?.status === 500) {
      error.value = 'Lỗi server, vui lòng thử lại sau'
    } else {
      error.value = err.response?.data?.message || 'Có lỗi xảy ra khi tải thông tin phân ca'
    }
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push('/phan-ca')
}

function editPhanCa() {
  if (phanCa.value?.id) {
    router.push(`/phan-ca/form/${phanCa.value.id}`)
  }
}

function deletePhanCa() {
  confirmTitle.value = 'Xác nhận xóa phân ca'
  confirmMessage.value = `Bạn có chắc chắn muốn xóa phân ca này? Hành động này không thể hoàn tác.`
  
  pendingAction.value = () => executeDelete()
  showConfirmModal.value = true
}

async function executeDelete() {
  if (!phanCa.value?.id) return

  try {
    loading.value = true
    await api.delete(`/api/phan-ca/${phanCa.value.id}`)
    toastRef.value?.success('Thành công', 'Đã xóa phân ca')
    goBack()
  } catch (err: any) {
    console.error('Lỗi khi xóa phân ca:', err)
    toastRef.value?.error('Lỗi', err.response?.data?.message || 'Không thể xóa phân ca')
  } finally {
    loading.value = false
  }
}

function handleConfirm() {
  if (pendingAction.value) {
    pendingAction.value()
    pendingAction.value = null
  }
  showConfirmModal.value = false
}

function handleCancel() {
  pendingAction.value = null
  showConfirmModal.value = false
}

onMounted(() => {
  loadPhanCa()
})
</script>

<style scoped>
.phan-ca-detail-page {
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
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-edit {
  background: linear-gradient(135deg, #f97316, #ea580c);
  color: white;
  box-shadow: 0 2px 4px rgba(249, 115, 22, 0.3);
}

.btn-edit:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.4);
}

.btn-delete {
  background: #dc2626;
  color: white;
  box-shadow: 0 2px 4px rgba(220, 38, 38, 0.3);
}

.btn-delete:hover {
  background: #b91c1c;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.4);
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

.badge-completed {
  background: #e0e7ff;
  color: #3730a3;
}

.badge-absent {
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
  
  .header-actions {
    flex-direction: column;
    width: 100%;
  }
  
  .btn-edit,
  .btn-delete {
    width: 100%;
  }
}
</style>

