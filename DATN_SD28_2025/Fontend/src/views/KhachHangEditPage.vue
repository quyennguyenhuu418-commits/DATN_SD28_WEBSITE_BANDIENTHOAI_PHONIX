<template>
  <div class="customer-form-page">
    <!-- Header -->
    <div class="page-header">
      <div class="header-left">
        <button class="btn-back" @click="goBack">
          <font-awesome-icon icon="arrow-left" />
          <span>Quay lại</span>
        </button>
        <div class="page-title">
          <font-awesome-icon icon="user-edit" class="title-icon" />
          <h1>Sửa Khách Hàng</h1>
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner">
        <font-awesome-icon icon="spinner" class="fa-spin" />
        <span>Đang tải thông tin khách hàng...</span>
      </div>
    </div>

    <!-- Form -->
    <div v-else class="form-container">
      <form @submit.prevent="handleSubmit" class="customer-form">
        <div class="form-section">
          <h3 class="section-title">
            <font-awesome-icon icon="user" />
            <span>Thông tin cơ bản</span>
          </h3>
          
          <div class="form-grid">
            <div class="form-group">
              <label class="form-label">
                <font-awesome-icon icon="id-card" />
                <span>Mã khách hàng</span>
              </label>
              <input 
                type="text" 
                v-model="formData.maKhachHang" 
                class="form-input"
                disabled
              />
              <span class="form-note">Mã khách hàng không thể thay đổi</span>
            </div>

            <div class="form-group">
              <label class="form-label required">
                <font-awesome-icon icon="user" />
                <span>Họ và tên *</span>
              </label>
              <input 
                type="text" 
                v-model="formData.hoTen" 
                class="form-input"
                :class="{ 'error': errors.hoTen }"
                placeholder="Nhập họ và tên khách hàng"
                required
              />
              <span v-if="errors.hoTen" class="error-message">{{ errors.hoTen }}</span>
            </div>

            <div class="form-group">
              <label class="form-label required">
                <font-awesome-icon icon="phone" />
                <span>Số điện thoại *</span>
              </label>
              <input 
                type="tel" 
                v-model="formData.soDienThoai" 
                class="form-input"
                :class="{ 'error': errors.soDienThoai }"
                placeholder="Nhập số điện thoại"
                required
              />
              <span v-if="errors.soDienThoai" class="error-message">{{ errors.soDienThoai }}</span>
            </div>

            <div class="form-group">
              <label class="form-label">
                <font-awesome-icon icon="envelope" />
                <span>Email</span>
              </label>
              <input 
                type="email" 
                v-model="formData.email" 
                class="form-input"
                :class="{ 'error': errors.email }"
                placeholder="Nhập email (tùy chọn)"
              />
              <span v-if="errors.email" class="error-message">{{ errors.email }}</span>
            </div>

            <div class="form-group">
              <label class="form-label">
                <font-awesome-icon icon="venus-mars" />
                <span>Giới tính</span>
              </label>
              <select v-model="formData.gioiTinh" class="form-select">
                <option value="">-- Chọn giới tính --</option>
                <option value="Nam">Nam</option>
                <option value="Nữ">Nữ</option>
              </select>
            </div>

            <div class="form-group">
              <label class="form-label">
                <font-awesome-icon icon="calendar" />
                <span>Ngày sinh</span>
              </label>
              <input 
                type="date" 
                v-model="formData.ngaySinh" 
                class="form-input"
                :max="maxDate"
              />
            </div>

            <div class="form-group">
              <label class="form-label">
                <font-awesome-icon icon="id-card" />
                <span>Tài khoản</span>
              </label>
              <input 
                type="text" 
                v-model="formData.taiKhoan" 
                class="form-input"
                placeholder="Nhập tài khoản (tùy chọn)"
              />
            </div>
          </div>
        </div>

        <div class="form-section">
          <h3 class="section-title">
            <font-awesome-icon icon="cog" />
            <span>Cài đặt</span>
          </h3>
          
          <div class="form-group checkbox-group">
            <label class="checkbox-label">
              <input 
                type="checkbox" 
                v-model="formData.trangThai"
                class="checkbox-input"
              />
              <span class="checkbox-text">
                <font-awesome-icon icon="toggle-on" />
                Kích hoạt tài khoản khách hàng
              </span>
            </label>
          </div>
        </div>

        <div class="form-section">
          <h3 class="section-title">
            <font-awesome-icon icon="info-circle" />
            <span>Thông tin hệ thống</span>
          </h3>
          
          <div class="form-grid">
            <div class="form-group">
              <label class="form-label">
                <font-awesome-icon icon="calendar-plus" />
                <span>Ngày tạo</span>
              </label>
              <input 
                type="text" 
                :value="formatDate(formData.ngayTao)" 
                class="form-input"
                disabled
              />
            </div>

            <div class="form-group">
              <label class="form-label">
                <font-awesome-icon icon="calendar-edit" />
                <span>Ngày cập nhật</span>
              </label>
              <input 
                type="text" 
                :value="formatDate(formData.ngayCapNhat)" 
                class="form-input"
                disabled
              />
            </div>
          </div>
        </div>

        <div class="form-actions">
          <button type="button" class="btn-secondary" @click="goBack">
            <font-awesome-icon icon="times" />
            <span>Hủy</span>
          </button>
          <button type="submit" class="btn-primary" :disabled="saving">
            <font-awesome-icon icon="save" />
            <span v-if="saving">Đang lưu...</span>
            <span v-else>Lưu thay đổi</span>
          </button>
        </div>
      </form>
    </div>

    <Toast ref="toastRef" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '@/services/api'
import Toast from '@/components/Toast.vue'

const router = useRouter()
const route = useRoute()
const toastRef = ref<InstanceType<typeof Toast> | null>(null)
const loading = ref(true)
const saving = ref(false)

// Form data
const formData = ref({
  id: 0,
  maKhachHang: '',
  hoTen: '',
  soDienThoai: '',
  email: '',
  gioiTinh: '',
  ngaySinh: '',
  taiKhoan: '',
  trangThai: true,
  ngayTao: '',
  ngayCapNhat: ''
})

// Validation errors
const errors = ref<Record<string, string>>({})

// Max date for birth date (18 years ago)
const maxDate = computed(() => {
  const date = new Date()
  date.setFullYear(date.getFullYear() - 18)
  return date.toISOString().split('T')[0]
})

// Load customer data
async function loadCustomer() {
  try {
    const customerId = route.params.id
    const { data } = await api.get(`/api/khach-hang/${customerId}`)
    
    formData.value = {
      ...data,
      trangThai: data.trangThai === 1
    }
  } catch (error: any) {
    console.error('Lỗi khi tải thông tin khách hàng:', error)
    toastRef.value?.error('Lỗi', 'Không thể tải thông tin khách hàng')
    router.push('/khach-hang')
  } finally {
    loading.value = false
  }
}

// Validation functions
function validateForm() {
  errors.value = {}
  let isValid = true

  // Validate hoTen
  if (!formData.value.hoTen.trim()) {
    errors.value.hoTen = 'Họ và tên không được để trống'
    isValid = false
  } else if (formData.value.hoTen.trim().length < 2) {
    errors.value.hoTen = 'Họ và tên phải có ít nhất 2 ký tự'
    isValid = false
  }

  // Validate soDienThoai
  if (!formData.value.soDienThoai.trim()) {
    errors.value.soDienThoai = 'Số điện thoại không được để trống'
    isValid = false
  } else if (!/^[0-9]{10,11}$/.test(formData.value.soDienThoai.replace(/\s/g, ''))) {
    errors.value.soDienThoai = 'Số điện thoại phải có 10-11 chữ số'
    isValid = false
  }

  // Validate email
  if (formData.value.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.value.email)) {
    errors.value.email = 'Email không đúng định dạng'
    isValid = false
  }

  return isValid
}

// Form submission
async function handleSubmit() {
  if (!validateForm()) {
    toastRef.value?.error('Lỗi', 'Vui lòng kiểm tra lại thông tin nhập')
    return
  }

  saving.value = true
  try {
    const data = {
      ...formData.value,
      trangThai: formData.value.trangThai ? 1 : 0
    }

    await api.put(`/api/khach-hang/${formData.value.id}`, data)
    toastRef.value?.success('Thành công', 'Cập nhật khách hàng thành công!')
    router.push('/khach-hang')
  } catch (error: any) {
    console.error('Lỗi khi cập nhật khách hàng:', error)
    if (error.response?.data) {
      toastRef.value?.error('Lỗi', error.response.data)
    } else {
      toastRef.value?.error('Lỗi', 'Có lỗi xảy ra khi cập nhật khách hàng')
    }
  } finally {
    saving.value = false
  }
}

function formatDate(dateString: string): string {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function goBack() {
  router.push('/khach-hang')
}

onMounted(loadCustomer)
</script>

<style scoped>
.customer-form-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 0;
}

.page-header {
  background: white;
  padding: 1.5rem 2rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.btn-back {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: #e2e8f0;
  color: #475569;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.btn-back:hover {
  background: #cbd5e1;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.title-icon {
  font-size: 1.5rem;
  color: #667eea;
}

.page-title h1 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  color: #667eea;
  font-size: 1.1rem;
  font-weight: 500;
}

.loading-spinner .fa-spin {
  font-size: 2rem;
}

.form-container {
  margin: 0 2rem 2rem 2rem;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.customer-form {
  padding: 2rem;
}

.form-section {
  margin-bottom: 2rem;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 1.25rem;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 1.5rem;
  padding-bottom: 0.75rem;
  border-bottom: 2px solid #e2e8f0;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;
  color: #374151;
  font-size: 0.875rem;
}

.form-label.required::after {
  content: '*';
  color: #dc2626;
  margin-left: 0.25rem;
}

.form-input, .form-select {
  padding: 0.75rem 1rem;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 0.9rem;
  transition: all 0.3s ease;
  background: white;
}

.form-input:focus, .form-select:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-input.error, .form-select.error {
  border-color: #dc2626;
}

.form-input:disabled {
  background: #f8fafc;
  color: #64748b;
  cursor: not-allowed;
}

.form-note {
  font-size: 0.75rem;
  color: #64748b;
  font-style: italic;
}

.error-message {
  color: #dc2626;
  font-size: 0.875rem;
  font-weight: 500;
}

.checkbox-group {
  margin-bottom: 0;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  cursor: pointer;
  font-size: 0.9rem;
  color: #374151;
}

.checkbox-input {
  width: 18px;
  height: 18px;
  accent-color: #667eea;
}

.checkbox-text {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  padding-top: 2rem;
  border-top: 1px solid #e2e8f0;
}

.btn-primary, .btn-secondary {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  background: #e2e8f0;
  color: #475569;
}

.btn-secondary:hover {
  background: #cbd5e1;
}

/* Responsive Design */
@media (max-width: 768px) {
  .page-header {
    padding: 1rem;
  }
  
  .header-left {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }
  
  .form-container {
    margin: 0 1rem 1rem 1rem;
  }
  
  .customer-form {
    padding: 1rem;
  }
  
  .form-grid {
    grid-template-columns: 1fr;
  }
  
  .form-actions {
    flex-direction: column;
  }
}
</style>



