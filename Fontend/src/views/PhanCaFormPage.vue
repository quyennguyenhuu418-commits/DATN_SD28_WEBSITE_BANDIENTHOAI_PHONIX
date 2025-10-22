<template>
  <div class="page dark-mode-transition">
    <PosHeader />
    
    <div class="content">
      <Toast ref="toastRef" />

      <!-- Header Section -->
      <div class="form-header">
        <div class="header-content">
          <div class="header-left">
            <button @click="goBack" class="btn-back">
              <font-awesome-icon :icon="['fas', 'arrow-left']" />
              Quay lại
            </button>
            <div class="header-title">
              <h1>{{ isEdit ? 'Chỉnh sửa Phân Ca' : 'Thêm Phân Ca Mới' }}</h1>
              <p>{{ isEdit ? 'Cập nhật thông tin phân ca' : 'Tạo phân ca mới cho nhân viên' }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Form Section -->
      <div class="form-container">
        <form @submit.prevent="handleSubmit" class="phan-ca-form">
          <div class="form-grid">
            <!-- Nhân Viên -->
            <div class="form-group">
              <label for="nhanVienId" class="form-label required">
                <font-awesome-icon :icon="['fas', 'user']" />
                Nhân Viên
              </label>
              <select
                id="nhanVienId"
                v-model="formData.nhanVienId"
                class="form-select"
                required
                :disabled="loading"
                @change="validateNhanVien"
              >
                <option value="">Chọn nhân viên</option>
                <option v-for="nv in nhanVienList" :key="nv.id" :value="nv.id">
                  {{ nv.hoTen }}
                </option>
              </select>
              <div v-if="errors.nhanVienId" class="error-message">
                {{ errors.nhanVienId }}
              </div>
            </div>

            <!-- Ca Làm Việc -->
            <div class="form-group">
              <label for="caId" class="form-label required">
                <font-awesome-icon :icon="['fas', 'clock']" />
                Ca Làm Việc
              </label>
              <select
                id="caId"
                v-model="formData.caId"
                class="form-select"
                required
                :disabled="loading"
                @change="validateCa"
              >
                <option value="">Chọn ca làm việc</option>
                <option v-for="ca in caList" :key="ca.id" :value="ca.id">
                  {{ ca.tenCa }} ({{ formatTime(ca.gioBatDau) }} - {{ formatTime(ca.gioKetThuc) }})
                </option>
              </select>
              <div v-if="errors.caId" class="error-message">
                {{ errors.caId }}
              </div>
            </div>

            <!-- Ngày Làm Việc -->
            <div class="form-group">
              <label for="ngayLamViec" class="form-label required">
                <font-awesome-icon :icon="['fas', 'calendar']" />
                Ngày Làm Việc
              </label>
              <input
                id="ngayLamViec"
                v-model="formData.ngayLamViec"
                type="date"
                class="form-input"
                required
                :disabled="loading"
                :min="minDate"
                @change="validateDate"
              />
              <div v-if="errors.ngayLamViec" class="error-message">
                {{ errors.ngayLamViec }}
              </div>
            </div>

            <!-- Ghi Chú -->
            <div class="form-group full-width">
              <label for="ghiChu" class="form-label">
                <font-awesome-icon :icon="['fas', 'file-text']" />
                Ghi Chú
              </label>
              <textarea
                id="ghiChu"
                v-model="formData.ghiChu"
                class="form-textarea"
                placeholder="Nhập ghi chú về phân ca này..."
                rows="4"
                :disabled="loading"
              ></textarea>
              <div v-if="errors.ghiChu" class="error-message">
                {{ errors.ghiChu }}
              </div>
            </div>

            <!-- Trạng Thái -->
            <div class="form-group">
              <label class="form-label required">
                <font-awesome-icon :icon="['fas', 'toggle-on']" />
                Trạng Thái
              </label>
              <div class="status-toggle">
                <label class="toggle-switch">
                  <input
                    type="checkbox"
                    v-model="formData.trangThai"
                    :true-value="1"
                    :false-value="0"
                    :disabled="loading"
                  />
                  <span class="toggle-slider"></span>
                </label>
                <span class="status-text">
                  {{ formData.trangThai === 1 ? 'Hoạt động' : 'Ngừng hoạt động' }}
                </span>
              </div>
            </div>

            <!-- Conflict Warning -->
            <div v-if="conflictWarning" class="conflict-warning">
              <font-awesome-icon :icon="['fas', 'exclamation-triangle']" />
              <div class="warning-content">
                <h4>Cảnh báo xung đột lịch</h4>
                <p>{{ conflictWarning }}</p>
              </div>
            </div>
          </div>

          <!-- Action Buttons -->
          <div class="form-actions">
            <button
              type="button"
              @click="goBack"
              class="btn-cancel"
              :disabled="loading"
            >
              <font-awesome-icon :icon="['fas', 'times']" />
              Hủy
            </button>
            <button
              type="submit"
              class="btn-submit"
              :disabled="loading || !isFormValid"
            >
              <font-awesome-icon v-if="loading" :icon="['fas', 'spinner']" spin />
              <font-awesome-icon v-else :icon="['fas', 'save']" />
              {{ loading ? 'Đang xử lý...' : (isEdit ? 'Cập nhật' : 'Tạo mới') }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Confirm Modal -->
    <ConfirmModal
      :show="showConfirmModal"
      :title="confirmAction === 'add' ? 'Xác nhận tạo mới' : 'Xác nhận cập nhật'"
      :message="confirmAction === 'add' 
        ? 'Bạn có chắc chắn muốn tạo phân ca mới với thông tin này?' 
        : 'Bạn có chắc chắn muốn cập nhật thông tin phân ca này?'"
      @confirm="confirmSubmit"
      @cancel="cancelSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '@/services/api'
import Toast from '@/components/Toast.vue'
import PosHeader from '@/components/PosHeader.vue'
import ConfirmModal from '@/components/ConfirmModal.vue'

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
}

interface PhanCaFormData {
  nhanVienId: number | string
  caId: number | string
  ngayLamViec: string
  ghiChu: string
  trangThai: number
}

const formData = ref<PhanCaFormData>({
  nhanVienId: '',
  caId: '',
  ngayLamViec: '',
  ghiChu: '',
  trangThai: 1
})

const nhanVienList = ref<NhanVien[]>([])
const caList = ref<Ca[]>([])
const errors = ref<Partial<PhanCaFormData>>({})
const conflictWarning = ref('')
const loading = ref(false)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

// Confirm modal state
const showConfirmModal = ref(false)
const confirmAction = ref<'add' | 'edit' | null>(null)

const isEdit = computed(() => route.params.id && route.params.id !== 'new')
const phanCaId = computed(() => route.params.id as string)

const minDate = computed(() => {
  const today = new Date()
  return today.toISOString().split('T')[0]
})

const isFormValid = computed(() => {
  return formData.value.nhanVienId !== '' &&
         formData.value.caId !== '' &&
         formData.value.ngayLamViec !== '' &&
         !conflictWarning.value
})

async function loadNhanVien() {
  try {
    const { data } = await api.get('/api/nhan-vien')
    nhanVienList.value = data
  } catch (error) {
    console.error('Lỗi khi tải danh sách nhân viên:', error)
    toastRef.value?.error('Lỗi', 'Không thể tải danh sách nhân viên')
  }
}

async function loadCa() {
  try {
    const { data } = await api.get('/api/ca')
    caList.value = data
  } catch (error) {
    console.error('Lỗi khi tải danh sách ca:', error)
    toastRef.value?.error('Lỗi', 'Không thể tải danh sách ca')
  }
}

async function loadPhanCa() {
  if (!isEdit.value) return
  
  loading.value = true
  try {
    const { data } = await api.get(`/api/phan-ca/${phanCaId.value}`)
    formData.value = {
      nhanVienId: data.nhanVienId || '',
      caId: data.caId || '',
      ngayLamViec: data.ngayLamViec || '',
      ghiChu: data.ghiChu || '',
      trangThai: data.trangThai || 1
    }
  } catch (error) {
    console.error('Lỗi khi tải thông tin phân ca:', error)
    toastRef.value?.error('Lỗi', 'Không thể tải thông tin phân ca')
    goBack()
  } finally {
    loading.value = false
  }
}

async function validateNhanVien() {
  if (!formData.value.nhanVienId || !formData.value.ngayLamViec) return
  
  try {
    const { data } = await api.get(`/api/phan-ca/check-conflict`, {
      params: {
        nhanVienId: formData.value.nhanVienId,
        ngayLamViec: formData.value.ngayLamViec,
        excludeId: isEdit.value ? phanCaId.value : undefined
      }
    })
    
    if (data.hasConflict) {
      conflictWarning.value = `Nhân viên đã có phân ca khác vào ngày ${formatDate(formData.value.ngayLamViec)}`
    } else {
      conflictWarning.value = ''
    }
  } catch (error) {
    console.error('Lỗi khi kiểm tra xung đột:', error)
  }
}

async function validateCa() {
  if (!formData.value.caId || !formData.value.ngayLamViec) return
  
  try {
    const { data } = await api.get(`/api/phan-ca/check-conflict`, {
      params: {
        caId: formData.value.caId,
        ngayLamViec: formData.value.ngayLamViec,
        excludeId: isEdit.value ? phanCaId.value : undefined
      }
    })
    
    if (data.hasConflict) {
      const caName = caList.value.find(c => c.id == formData.value.caId)?.tenCa || 'ca này'
      conflictWarning.value = `Ca ${caName} đã được phân cho nhân viên khác vào ngày ${formatDate(formData.value.ngayLamViec)}`
    } else {
      conflictWarning.value = ''
    }
  } catch (error) {
    console.error('Lỗi khi kiểm tra xung đột:', error)
  }
}

function validateDate() {
  if (!formData.value.ngayLamViec) return
  
  const selectedDate = new Date(formData.value.ngayLamViec)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  
  if (selectedDate < today) {
    errors.value.ngayLamViec = 'Ngày làm việc không thể là ngày trong quá khứ'
  } else {
    errors.value.ngayLamViec = ''
    // Re-validate conflicts when date changes
    validateNhanVien()
    validateCa()
  }
}

async function handleSubmit() {
  if (!isFormValid.value) {
    toastRef.value?.warning('Cảnh báo', 'Vui lòng điền đầy đủ thông tin bắt buộc và giải quyết xung đột lịch')
    return
  }

  // Show confirm modal
  confirmAction.value = isEdit.value ? 'edit' : 'add'
  showConfirmModal.value = true
}

async function confirmSubmit() {
  loading.value = true
  errors.value = {}

  try {
    const submitData = {
      ...formData.value,
      nhanVienId: Number(formData.value.nhanVienId),
      caId: Number(formData.value.caId)
    }

    if (isEdit.value) {
      await api.put(`/api/phan-ca/${phanCaId.value}`, submitData)
      toastRef.value?.success('Thành công', 'Cập nhật phân ca thành công!')
    } else {
      await api.post('/api/phan-ca', submitData)
      toastRef.value?.success('Thành công', 'Tạo phân ca mới thành công!')
    }
    
    setTimeout(() => {
      goBack()
    }, 1500)
  } catch (error: any) {
    console.error('Lỗi khi lưu phân ca:', error)
    
    if (error.response?.data?.errors) {
      errors.value = error.response.data.errors
    } else {
      toastRef.value?.error('Lỗi', error.response?.data?.message || 'Không thể lưu phân ca')
    }
  } finally {
    loading.value = false
    showConfirmModal.value = false
    confirmAction.value = null
  }
}

function cancelSubmit() {
  showConfirmModal.value = false
  confirmAction.value = null
}

function formatTime(timeString: string): string {
  if (!timeString) return ''
  return timeString.substring(0, 5) // HH:MM
}

function formatDate(dateString: string): string {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN')
}

function goBack() {
  router.push('/phan-ca')
}

onMounted(async () => {
  await Promise.all([
    loadNhanVien(),
    loadCa(),
    loadPhanCa()
  ])
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

/* Form Header */
.form-header {
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

/* Form Container */
.form-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
  overflow: hidden;
}

.phan-ca-form {
  padding: 32px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 14px;
  color: #374151;
  margin-bottom: 6px;
}

.form-label.required::after {
  content: '*';
  color: #ef4444;
  margin-left: 4px;
}

.form-input,
.form-select,
.form-textarea {
  padding: 12px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  background: #fafbfc;
  font-size: 14px;
  transition: all 0.3s ease;
  color: #1f2937;
  font-weight: 500;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: #f97316;
  background: white;
  box-shadow: 0 0 0 4px rgba(249, 115, 22, 0.1);
  transform: translateY(-1px);
}

.form-select {
  cursor: pointer;
  appearance: none;
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 20 20'%3e%3cpath stroke='%236b7280' stroke-linecap='round' stroke-linejoin='round' stroke-width='1.5' d='m6 8 4 4 4-4'/%3e%3c/svg%3e");
  background-position: right 12px center;
  background-repeat: no-repeat;
  background-size: 16px;
  padding-right: 40px;
}

.form-textarea {
  resize: vertical;
  min-height: 100px;
  font-family: inherit;
}

/* Status Toggle */
.status-toggle {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #f8fafc;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.status-toggle:hover {
  border-color: #f97316;
  background: white;
}

.toggle-switch {
  position: relative;
  display: inline-block;
  width: 50px;
  height: 24px;
}

.toggle-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #cbd5e1;
  transition: 0.3s;
  border-radius: 24px;
}

.toggle-slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: 0.3s;
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

input:checked + .toggle-slider {
  background-color: #f97316;
}

input:checked + .toggle-slider:before {
  transform: translateX(26px);
}

.status-text {
  font-weight: 600;
  font-size: 14px;
  color: #374151;
}

/* Conflict Warning */
.conflict-warning {
  grid-column: 1 / -1;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  background: #fef3c7;
  border: 1px solid #fbbf24;
  border-radius: 8px;
  color: #d97706;
}

.conflict-warning font-awesome-icon {
  font-size: 20px;
  margin-top: 2px;
  flex-shrink: 0;
}

.warning-content h4 {
  font-size: 14px;
  font-weight: 700;
  margin: 0 0 4px 0;
}

.warning-content p {
  font-size: 13px;
  margin: 0;
  line-height: 1.4;
}

/* Error Message */
.error-message {
  color: #ef4444;
  font-size: 12px;
  font-weight: 500;
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.error-message::before {
  content: '⚠';
  font-size: 10px;
}

/* Form Actions */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 24px;
  border-top: 1px solid #e2e8f0;
}

.btn-cancel {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: #f8fafc;
  color: #64748b;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.2s ease;
}

.btn-cancel:hover:not(:disabled) {
  background: #f1f5f9;
  color: #374151;
  border-color: #cbd5e1;
  transform: translateY(-1px);
}

.btn-cancel:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-submit {
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

.btn-submit:hover:not(:disabled) {
  background: linear-gradient(135deg, #ea580c, #dc2626);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.4);
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* Responsive Design */
@media (max-width: 768px) {
  .content {
    padding: 16px;
  }
  
  .form-header {
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
  
  .phan-ca-form {
    padding: 20px;
  }
  
  .form-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .btn-cancel,
  .btn-submit {
    width: 100%;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .content {
    padding: 12px;
  }
  
  .form-header {
    padding: 12px;
  }
  
  .phan-ca-form {
    padding: 16px;
  }
  
  .header-title h1 {
    font-size: 20px;
  }
}
</style>



