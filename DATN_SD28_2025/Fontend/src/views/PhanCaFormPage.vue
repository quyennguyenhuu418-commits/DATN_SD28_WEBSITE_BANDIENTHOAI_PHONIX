<template>
  <div class="phan-ca-form-page dark-mode-transition">
    <PosHeader />
    
    <div class="edit-container">
      <div class="edit-header">
        <div class="header-left">
          <button class="btn-back" @click="goBack">
            <font-awesome-icon :icon="['fas', 'arrow-left']" />
            Quay lại
          </button>
          <h1>{{ isEdit ? 'Chỉnh sửa Phân Ca' : 'Thêm Phân Ca Mới' }}</h1>
        </div>
      </div>

      <!-- Loading state -->
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>Đang tải thông tin phân ca...</p>
      </div>

      <!-- Error state -->
      <div v-else-if="error" class="error-state">
        <div class="error-icon">⚠️</div>
        <h3>Lỗi tải dữ liệu</h3>
        <p>{{ error }}</p>
        <button @click="loadPhanCa" class="btn-retry">Thử lại</button>
      </div>

      <!-- Main content -->
      <div v-else class="edit-content">
        <!-- Warning for completed shift -->
        <div v-if="isReadOnly" class="read-only-notice">
          <div class="notice-icon">
            <font-awesome-icon :icon="['fas', 'info-circle']" />
          </div>
          <div class="notice-content">
            <h3>Không thể chỉnh sửa</h3>
            <p>Phân ca này đã kết thúc, chỉ có thể xem thông tin. Đang chuyển đến trang chi tiết...</p>
          </div>
        </div>
        
        <form @submit.prevent="handleSubmit" class="edit-form" v-else>
          <div class="form-section">
            <h3>Thông tin cơ bản</h3>
            <div class="form-grid">
              <div class="form-group">
                <label class="form-label required">
                  <font-awesome-icon :icon="['fas', 'user']" />
                  <span>Nhân viên *</span>
                </label>
                <select
                  v-model="formData.nhanVienId"
                  class="form-select"
                  required
                  :disabled="loading"
                >
                  <option value="">Chọn nhân viên</option>
                  <option v-for="nv in nhanVienList" :key="nv.id" :value="nv.id">
                    {{ nv.hoTen }} ({{ nv.maNhanVien }})
                  </option>
                </select>
              </div>

              <div class="form-group">
                <label class="form-label required">
                  <font-awesome-icon :icon="['fas', 'clock']" />
                  <span>Ca làm việc *</span>
                </label>
                <select
                  v-model="formData.caId"
                  class="form-select"
                  required
                  :disabled="loading"
                >
                  <option value="">Chọn ca làm việc</option>
                  <option v-for="ca in caList" :key="ca.id" :value="ca.id">
                    {{ ca.tenCa }} ({{ formatTime(ca.gioBatDau) }} - {{ formatTime(ca.gioKetThuc) }})
                  </option>
                </select>
              </div>

              <div class="form-group">
                <label class="form-label required">
                  <font-awesome-icon :icon="['fas', 'calendar']" />
                  <span>Ngày làm việc *</span>
                </label>
                <input
                  type="date"
                  v-model="formData.ngayLamViec"
                  class="form-input"
                  required
                  :min="minDate"
                  :disabled="loading"
                />
              </div>

              <div class="form-group">
                <label class="form-label">
                  <font-awesome-icon :icon="['fas', 'comment']" />
                  <span>Ghi chú</span>
                </label>
                <textarea
                  v-model="formData.ghiChu"
                  class="form-textarea"
                  rows="3"
                  placeholder="Nhập ghi chú (nếu có)..."
                  :disabled="loading"
                ></textarea>
              </div>
            </div>
          </div>

          <div v-if="conflictWarning" class="warning-box">
            <font-awesome-icon :icon="['fas', 'exclamation-triangle']" />
            <span>{{ conflictWarning }}</span>
          </div>

          <div class="form-actions">
            <button type="button" class="btn-cancel" @click="goBack" :disabled="loading">
              <font-awesome-icon :icon="['fas', 'times']" />
              <span>Hủy bỏ</span>
            </button>
            <button type="submit" class="btn-save" :disabled="loading || !isFormValid">
              <font-awesome-icon :icon="['fas', 'save']" />
              <span>{{ loading ? 'Đang lưu...' : (isEdit ? 'Cập nhật' : 'Thêm mới') }}</span>
            </button>
          </div>
        </form>
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
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '@/services/api'
import Toast from '@/components/Toast.vue'
import PosHeader from '@/components/PosHeader.vue'
import ConfirmModal from '@/components/ConfirmModal.vue'

const router = useRouter()
const route = useRoute()
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

const loading = ref(false)
const error = ref('')
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const pendingAction = ref<(() => void) | null>(null)
const conflictWarning = ref('')

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

const nhanVienList = ref<NhanVien[]>([])
const caList = ref<Ca[]>([])

const phanCaId = computed(() => route.params.id)
const isEdit = computed(() => phanCaId.value && phanCaId.value !== 'new')

const formData = ref({
  nhanVienId: '',
  caId: '',
  ngayLamViec: '',
  ghiChu: ''
})

const minDate = computed(() => {
  const today = new Date()
  return today.toISOString().split('T')[0]
})

const isFormValid = computed(() => {
  return formData.value.nhanVienId !== '' &&
         formData.value.caId !== '' &&
         formData.value.ngayLamViec !== ''
})

function formatTime(time: string) {
  if (!time) return ''
  return time.substring(0, 5)
}

async function loadNhanVien() {
  try {
    const { data } = await api.get<NhanVien[]>('/api/nhan-vien')
    nhanVienList.value = data
  } catch (error) {
    console.error('Lỗi khi tải danh sách nhân viên:', error)
    toastRef.value?.error('Lỗi', 'Không thể tải danh sách nhân viên')
  }
}

async function loadCa() {
  try {
    const { data } = await api.get<Ca[]>('/api/ca')
    caList.value = data
  } catch (error) {
    console.error('Lỗi khi tải danh sách ca:', error)
    toastRef.value?.error('Lỗi', 'Không thể tải danh sách ca')
  }
}

const isReadOnly = ref(false)

async function loadPhanCa() {
  if (!isEdit.value) return
  
  loading.value = true
  error.value = ''
  try {
    const { data } = await api.get(`/api/phan-ca/${phanCaId.value}`)
    
    // Kiểm tra nếu ca đã kết thúc (trangThai === 2) thì không cho chỉnh sửa
    if (data.trangThai === 2) {
      isReadOnly.value = true
      toastRef.value?.warning('Thông báo', 'Phân ca đã kết thúc, không thể chỉnh sửa. Chuyển đến trang chi tiết...')
      setTimeout(() => {
        router.push(`/phan-ca/detail/${phanCaId.value}`)
      }, 2000)
      return
    }
    
    formData.value = {
      nhanVienId: data.nhanVienId || '',
      caId: data.caId || '',
      ngayLamViec: data.ngayLamViec || '',
      ghiChu: data.ghiChu || ''
    }
  } catch (err: any) {
    console.error('Lỗi khi tải thông tin phân ca:', err)
    error.value = err.response?.data?.message || 'Không thể tải thông tin phân ca'
    toastRef.value?.error('Lỗi', error.value)
  } finally {
    loading.value = false
  }
}

async function checkConflict() {
  if (!formData.value.nhanVienId || !formData.value.ngayLamViec || !formData.value.caId) {
    conflictWarning.value = ''
    return
  }

  try {
    const { data } = await api.get(`/api/phan-ca/nhan-vien/${formData.value.nhanVienId}/ngay-lam-viec?ngayLamViec=${formData.value.ngayLamViec}`)
    const existingPhanCa = data.find((pc: any) => 
      pc.caId === parseInt(formData.value.caId) && 
      (!isEdit.value || pc.id !== parseInt(phanCaId.value as string))
    )
    
    if (existingPhanCa) {
      conflictWarning.value = `Cảnh báo: Nhân viên này đã có phân ca cho ngày ${formData.value.ngayLamViec} và ca này.`
    } else {
      conflictWarning.value = ''
    }
  } catch (error) {
    // Silent fail
  }
}

function goBack() {
  router.push('/phan-ca')
}

function handleSubmit() {
  if (!isFormValid.value) {
    toastRef.value?.error('Lỗi', 'Vui lòng điền đầy đủ thông tin bắt buộc')
    return
  }

  const action = isEdit.value ? 'cập nhật' : 'thêm mới'
  confirmTitle.value = `Xác nhận ${action} phân ca`
  confirmMessage.value = `Bạn có chắc chắn muốn ${action} phân ca này?`
  
  pendingAction.value = () => {
    if (isEdit.value) {
      updatePhanCa()
    } else {
      createPhanCa()
    }
  }
  showConfirmModal.value = true
}

async function createPhanCa() {
  loading.value = true
  try {
    await api.post('/api/phan-ca', formData.value)
    toastRef.value?.success('Thành công', 'Đã thêm phân ca mới')
    goBack()
  } catch (err: any) {
    console.error('Lỗi khi tạo phân ca:', err)
    toastRef.value?.error('Lỗi', err.response?.data?.message || 'Không thể tạo phân ca')
  } finally {
    loading.value = false
  }
}

async function updatePhanCa() {
  loading.value = true
  try {
    await api.put(`/api/phan-ca/${phanCaId.value}`, formData.value)
    toastRef.value?.success('Thành công', 'Đã cập nhật phân ca')
    goBack()
  } catch (err: any) {
    console.error('Lỗi khi cập nhật phân ca:', err)
    toastRef.value?.error('Lỗi', err.response?.data?.message || 'Không thể cập nhật phân ca')
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

// Watch for changes to check conflicts
watch([() => formData.value.nhanVienId, () => formData.value.caId, () => formData.value.ngayLamViec], () => {
  if (isEdit.value) return // Don't check conflicts when editing
  checkConflict()
}, { debounce: 500 })

onMounted(async () => {
  await Promise.all([loadNhanVien(), loadCa()])
  if (isEdit.value) {
    await loadPhanCa()
  }
})
</script>

<style scoped>
.phan-ca-form-page {
  background: #f8fafc;
  min-height: 100vh;
  padding: 0;
  padding-top: 80px;
}

.edit-container {
  width: 100%;
  max-width: 100%;
  margin: 0 auto;
  padding: 24px;
}

.edit-header {
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

.edit-header h1 {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.edit-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  padding: 32px;
}

.edit-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-section h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
  padding-bottom: 12px;
  border-bottom: 2px solid #f1f5f9;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group:has(textarea) {
  grid-column: 1 / -1;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #374151;
  font-size: 14px;
}

.form-label.required::after {
  content: '*';
  color: #dc2626;
  margin-left: 4px;
}

.form-input,
.form-select,
.form-textarea {
  padding: 12px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.2s ease;
  background: #fafbfc;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: #f97316;
  background: white;
  box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.1);
}

.form-textarea {
  resize: vertical;
  min-height: 100px;
  font-family: inherit;
}

.warning-box {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #fef3c7;
  border: 1px solid #fbbf24;
  border-radius: 8px;
  color: #92400e;
  font-size: 14px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 24px;
  border-top: 1px solid #e5e7eb;
}

.btn-cancel,
.btn-save {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-cancel {
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
}

.btn-cancel:hover:not(:disabled) {
  background: #e5e7eb;
}

.btn-save {
  background: linear-gradient(135deg, #f97316, #ea580c);
  color: white;
  box-shadow: 0 2px 4px rgba(249, 115, 22, 0.3);
}

.btn-save:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.4);
}

.btn-cancel:disabled,
.btn-save:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.loading-state,
.error-state {
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
  margin-bottom: 16px;
}

.error-state h3 {
  font-size: 18px;
  color: #dc2626;
  margin: 0 0 8px 0;
}

.error-state p {
  color: #64748b;
  margin: 0 0 16px 0;
}

.read-only-notice {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 20px;
  margin-bottom: 24px;
  background: #fef3c7;
  border: 2px solid #fbbf24;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.notice-icon {
  font-size: 24px;
  color: #d97706;
  flex-shrink: 0;
  margin-top: 2px;
}

.notice-content h3 {
  margin: 0 0 8px 0;
  color: #92400e;
  font-size: 18px;
  font-weight: 600;
}

.notice-content p {
  margin: 0;
  color: #78350f;
  font-size: 14px;
  line-height: 1.5;
}

.btn-retry {
  padding: 10px 20px;
  background: #f97316;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
}

@media (max-width: 768px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
  
  .edit-container {
    padding: 16px;
  }
  
  .edit-content {
    padding: 20px;
  }
}
</style>

