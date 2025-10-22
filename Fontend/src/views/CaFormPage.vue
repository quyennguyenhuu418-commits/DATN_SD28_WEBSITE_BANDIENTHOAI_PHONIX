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
              <h1>{{ isEdit ? 'Chỉnh sửa Ca Làm Việc' : 'Thêm Ca Làm Việc Mới' }}</h1>
              <p>{{ isEdit ? 'Cập nhật thông tin ca làm việc' : 'Tạo ca làm việc mới cho hệ thống' }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Form Section -->
      <div class="form-container">
        <form @submit.prevent="handleSubmit" class="ca-form">
          <div class="form-grid">
            <!-- Tên Ca -->
            <div class="form-group">
              <label for="tenCa" class="form-label required">
                <font-awesome-icon :icon="['fas', 'clock']" />
                Tên Ca Làm Việc
              </label>
              <input
                id="tenCa"
                v-model="formData.tenCa"
                type="text"
                class="form-input"
                placeholder="Nhập tên ca làm việc (VD: Ca sáng, Ca chiều, Ca tối)"
                required
                :disabled="loading"
              />
              <div v-if="errors.tenCa" class="error-message">
                {{ errors.tenCa }}
              </div>
            </div>

            <!-- Giờ Bắt Đầu -->
            <div class="form-group">
              <label for="gioBatDau" class="form-label required">
                <font-awesome-icon :icon="['fas', 'play']" />
                Giờ Bắt Đầu
              </label>
              <input
                id="gioBatDau"
                v-model="formData.gioBatDau"
                type="time"
                class="form-input"
                required
                :disabled="loading"
              />
              <div v-if="errors.gioBatDau" class="error-message">
                {{ errors.gioBatDau }}
              </div>
            </div>

            <!-- Giờ Kết Thúc -->
            <div class="form-group">
              <label for="gioKetThuc" class="form-label required">
                <font-awesome-icon :icon="['fas', 'stop']" />
                Giờ Kết Thúc
              </label>
              <input
                id="gioKetThuc"
                v-model="formData.gioKetThuc"
                type="time"
                class="form-input"
                required
                :disabled="loading"
              />
              <div v-if="errors.gioKetThuc" class="error-message">
                {{ errors.gioKetThuc }}
              </div>
            </div>

            <!-- Mô Tả -->
            <div class="form-group full-width">
              <label for="moTa" class="form-label">
                <font-awesome-icon :icon="['fas', 'file-text']" />
                Mô Tả
              </label>
              <textarea
                id="moTa"
                v-model="formData.moTa"
                class="form-textarea"
                placeholder="Nhập mô tả chi tiết về ca làm việc..."
                rows="4"
                :disabled="loading"
              ></textarea>
              <div v-if="errors.moTa" class="error-message">
                {{ errors.moTa }}
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
        ? 'Bạn có chắc chắn muốn tạo ca làm việc mới với thông tin này?' 
        : 'Bạn có chắc chắn muốn cập nhật thông tin ca làm việc này?'"
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

interface CaFormData {
  tenCa: string
  gioBatDau: string
  gioKetThuc: string
  moTa: string
  trangThai: number
}

const formData = ref<CaFormData>({
  tenCa: '',
  gioBatDau: '',
  gioKetThuc: '',
  moTa: '',
  trangThai: 1
})

const errors = ref<Partial<CaFormData>>({})
const loading = ref(false)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

// Confirm modal state
const showConfirmModal = ref(false)
const confirmAction = ref<'add' | 'edit' | null>(null)

const isEdit = computed(() => route.params.id && route.params.id !== 'new')
const caId = computed(() => route.params.id as string)

const isFormValid = computed(() => {
  return formData.value.tenCa.trim() !== '' &&
         formData.value.gioBatDau !== '' &&
         formData.value.gioKetThuc !== '' &&
         formData.value.gioBatDau < formData.value.gioKetThuc
})

async function loadCa() {
  if (!isEdit.value) return
  
  loading.value = true
  try {
    const { data } = await api.get(`/api/ca/${caId.value}`)
    formData.value = {
      tenCa: data.tenCa || '',
      gioBatDau: data.gioBatDau || '',
      gioKetThuc: data.gioKetThuc || '',
      moTa: data.moTa || '',
      trangThai: data.trangThai || 1
    }
  } catch (error) {
    console.error('Lỗi khi tải thông tin ca:', error)
    toastRef.value?.error('Lỗi', 'Không thể tải thông tin ca')
    goBack()
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!isFormValid.value) {
    toastRef.value?.warning('Cảnh báo', 'Vui lòng điền đầy đủ thông tin bắt buộc')
    return
  }

  // Validate time
  if (formData.value.gioBatDau >= formData.value.gioKetThuc) {
    errors.value.gioKetThuc = 'Giờ kết thúc phải sau giờ bắt đầu'
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
    if (isEdit.value) {
      await api.put(`/api/ca/${caId.value}`, formData.value)
      toastRef.value?.success('Thành công', 'Cập nhật ca làm việc thành công!')
    } else {
      await api.post('/api/ca', formData.value)
      toastRef.value?.success('Thành công', 'Tạo ca làm việc mới thành công!')
    }
    
    setTimeout(() => {
      goBack()
    }, 1500)
  } catch (error: any) {
    console.error('Lỗi khi lưu ca:', error)
    
    if (error.response?.data?.errors) {
      errors.value = error.response.data.errors
    } else {
      toastRef.value?.error('Lỗi', error.response?.data?.message || 'Không thể lưu ca làm việc')
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

function goBack() {
  router.push('/ca')
}

onMounted(() => {
  loadCa()
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

.ca-form {
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
.form-textarea:focus {
  outline: none;
  border-color: #f97316;
  background: white;
  box-shadow: 0 0 0 4px rgba(249, 115, 22, 0.1);
  transform: translateY(-1px);
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
  
  .ca-form {
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
  
  .ca-form {
    padding: 16px;
  }
  
  .header-title h1 {
    font-size: 20px;
  }
}
</style>



