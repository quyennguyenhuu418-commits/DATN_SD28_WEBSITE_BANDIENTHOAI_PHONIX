<template>
  <div class="tra-bao-hanh-page">
    <PosHeader />
    <Toast ref="toastRef" />
    
    <div class="form-container">
      <div class="form-header">
        <div class="header-left">
          <button class="btn-back" @click="goBack">
            <FontAwesomeIcon :icon="['fas', 'arrow-left']" />
            Quay lại
          </button>
          <h1>Trả Máy Bảo hành</h1>
        </div>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>Đang tải thông tin phiếu bảo hành...</p>
      </div>

      <!-- Error -->
      <div v-else-if="error" class="error-container">
        <div class="error-icon">
          <FontAwesomeIcon :icon="['fas', 'exclamation-triangle']" />
        </div>
        <h3>Không thể tải thông tin phiếu bảo hành</h3>
        <p>{{ error }}</p>
        <button class="btn-retry" @click="loadPhieuBaoHanh">
          <FontAwesomeIcon :icon="['fas', 'redo']" />
          Thử lại
        </button>
      </div>

      <!-- Form -->
      <div v-else-if="phieuBaoHanh" class="form-content">
        <!-- Thông tin phiếu bảo hành -->
        <div class="detail-card">
          <div class="card-header">
            <h2>Thông tin Phiếu Bảo hành</h2>
          </div>
          <div class="card-body">
            <div class="info-grid">
              <div class="info-item">
                <label>Mã phiếu:</label>
                <span class="info-value">{{ phieuBaoHanh.maPhieu }}</span>
              </div>
              <div class="info-item">
                <label>Tên khách hàng:</label>
                <span class="info-value">{{ phieuBaoHanh.tenKhachHang }}</span>
              </div>
              <div class="info-item">
                <label>Số điện thoại:</label>
                <span class="info-value">{{ phieuBaoHanh.soDienThoai }}</span>
              </div>
              <div class="info-item">
                <label>Tên sản phẩm:</label>
                <span class="info-value">{{ phieuBaoHanh.tenSanPham }}</span>
              </div>
              <div class="info-item">
                <label>IMEI/Serial:</label>
                <span class="info-value">{{ phieuBaoHanh.imeiSerial || '-' }}</span>
              </div>
              <div class="info-item">
                <label>Ngày nhận:</label>
                <span class="info-value">{{ formatDate(phieuBaoHanh.ngayNhan) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Thông tin trả máy -->
        <div class="detail-card">
          <div class="card-header">
            <h2>Thông tin Trả Máy</h2>
          </div>
          <div class="card-body">
            <form @submit.prevent="handleSubmit" class="tra-may-form">
              <div class="form-group">
                <label class="form-label">
                  <FontAwesomeIcon :icon="['fas', 'calendar-check']" />
                  <span>Ngày trả thực tế *</span>
                </label>
                <input 
                  type="date" 
                  v-model="formData.ngayTraThucTe" 
                  class="form-input"
                  required
                />
                <small class="form-help">Ngày trả máy thực tế cho khách hàng (mặc định là hôm nay)</small>
              </div>

              <div class="form-group">
                <label class="form-label">
                  <FontAwesomeIcon :icon="['fas', 'clipboard-check']" />
                  <span>Kết quả sửa chữa</span>
                </label>
                <div class="info-value-text">{{ phieuBaoHanh.noiDungSuaChua || '-' }}</div>
              </div>

              <div class="form-group">
                <label class="form-label">
                  <FontAwesomeIcon :icon="['fas', 'comments']" />
                  <span>Ghi chú kỹ thuật viên</span>
                </label>
                <div class="info-value-text">{{ phieuBaoHanh.ghiChuKyThuatVien || '-' }}</div>
              </div>

              <div class="form-group" v-if="phieuBaoHanh.chiPhiSuaChua">
                <label class="form-label">
                  <FontAwesomeIcon :icon="['fas', 'money-bill']" />
                  <span>Chi phí sửa chữa</span>
                </label>
                <div class="info-value-text">{{ formatCurrency(phieuBaoHanh.chiPhiSuaChua) }}</div>
              </div>

              <div class="form-group" v-if="phieuBaoHanh.khachDaThanhToan">
                <label class="form-label">
                  <FontAwesomeIcon :icon="['fas', 'check-circle']" />
                  <span>Khách đã thanh toán</span>
                </label>
                <div class="info-value-text">{{ formatCurrency(phieuBaoHanh.khachDaThanhToan) }}</div>
              </div>

              <div class="form-group">
                <label class="form-label">
                  <FontAwesomeIcon :icon="['fas', 'sticky-note']" />
                  <span>Ghi chú trả máy</span>
                </label>
                <textarea 
                  v-model="formData.ghiChu" 
                  class="form-textarea"
                  rows="4"
                  placeholder="Ghi chú thêm khi trả máy (nếu có)"
                ></textarea>
              </div>

              <div class="form-group checkbox-group">
                <label class="checkbox-label">
                  <input 
                    type="checkbox" 
                    v-model="formData.khachDaKiemTra" 
                    class="checkbox-input"
                  />
                  <span>Khách hàng đã kiểm tra máy và xác nhận hoạt động bình thường</span>
                </label>
              </div>

              <div class="form-actions">
                <button type="button" class="btn-cancel" @click="goBack">
                  Hủy
                </button>
                <button type="submit" class="btn-submit" :disabled="isSubmitting">
                  <FontAwesomeIcon :icon="['fas', 'check']" v-if="!isSubmitting" />
                  <span v-if="isSubmitting">Đang xử lý...</span>
                  <span v-else>Xác nhận Trả Máy</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { FontAwesomeIcon } from '@/plugins/fontawesome'
import PosHeader from '@/components/PosHeader.vue'
import Toast from '@/components/Toast.vue'
import api from '@/services/api'

const router = useRouter()
const route = useRoute()
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

const phieuBaoHanh = ref<any>(null)
const loading = ref(false)
const error = ref('')
const isSubmitting = ref(false)

const formData = ref({
  ngayTraThucTe: new Date().toISOString().split('T')[0],
  ghiChu: '',
  khachDaKiemTra: false
})

function goBack() {
  router.push(`/bao-hanh/chi-tiet/${route.params.id}`)
}

function formatDate(dateString?: string): string {
  if (!dateString) return '-'
  try {
    return new Date(dateString).toLocaleDateString('vi-VN')
  } catch {
    return dateString
  }
}

function formatCurrency(amount?: number): string {
  if (amount === null || amount === undefined) return '0 VNĐ'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount)
}

async function loadPhieuBaoHanh() {
  loading.value = true
  error.value = ''
  try {
    const id = route.params.id
    const response = await api.get(`/api/bao-hanh/${id}`)
    phieuBaoHanh.value = response.data
    
    // Set default date if not already set
    if (!formData.value.ngayTraThucTe) {
      formData.value.ngayTraThucTe = new Date().toISOString().split('T')[0]
    }
  } catch (err: any) {
    error.value = err.response?.data?.message || err.message || 'Không thể tải thông tin phiếu bảo hành'
    toastRef.value?.error('Lỗi', error.value)
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!formData.value.khachDaKiemTra) {
    toastRef.value?.warning('Cảnh báo', 'Vui lòng xác nhận khách hàng đã kiểm tra máy')
    return
  }

  if (!confirm('Bạn có chắc chắn muốn trả máy cho khách hàng?')) {
    return
  }

  isSubmitting.value = true
  try {
    await api.post(`/api/bao-hanh/${phieuBaoHanh.value.id}/tra-may`, {
      ghiChu: formData.value.ghiChu || 'Đã trả máy cho khách hàng'
    })
    
    toastRef.value?.success('Thành công', 'Đã cập nhật trạng thái trả máy thành công')
    
    setTimeout(() => {
      router.push(`/bao-hanh/chi-tiet/${phieuBaoHanh.value.id}`)
    }, 1500)
  } catch (err: any) {
    toastRef.value?.error('Lỗi', 'Không thể cập nhật: ' + (err.response?.data?.message || err.message))
  } finally {
    isSubmitting.value = false
  }
}

onMounted(() => {
  loadPhieuBaoHanh()
})
</script>

<style scoped>
.tra-bao-hanh-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20px;
}

.form-container {
  max-width: 900px;
  margin: 0 auto;
}

.form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.btn-back {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background-color: #6c757d;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.btn-back:hover {
  background-color: #5a6268;
}

.form-header h1 {
  margin: 0;
  color: #333;
  font-size: 24px;
}

.form-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  overflow: hidden;
}

.card-header {
  padding: 15px 20px;
  background-color: #f8f9fa;
  border-bottom: 2px solid #e0e0e0;
}

.card-header h2 {
  margin: 0;
  color: #333;
  font-size: 18px;
}

.card-body {
  padding: 20px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 15px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.info-item label {
  font-weight: 500;
  color: #666;
  font-size: 14px;
}

.info-value {
  color: #333;
  font-size: 15px;
}

.info-value-text {
  color: #333;
  font-size: 14px;
  line-height: 1.6;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.tra-may-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  color: #555;
}

.form-input,
.form-textarea {
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: #2196F3;
}

.form-textarea {
  resize: vertical;
  font-family: inherit;
}

.form-help {
  font-size: 12px;
  color: #666;
}

.checkbox-group {
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  font-weight: 500;
  color: #555;
}

.checkbox-input {
  width: 18px;
  height: 18px;
  cursor: pointer;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 2px solid #e0e0e0;
}

.btn-cancel,
.btn-submit {
  padding: 12px 30px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-cancel {
  background-color: #6c757d;
  color: white;
}

.btn-cancel:hover {
  background-color: #5a6268;
}

.btn-submit {
  background-color: #4CAF50;
  color: white;
}

.btn-submit:hover:not(:disabled) {
  background-color: #45a049;
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.loading-container,
.error-container {
  text-align: center;
  padding: 40px;
  background: white;
  border-radius: 8px;
}

.loading-spinner {
  border: 4px solid #f3f3f3;
  border-top: 4px solid #2196F3;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-icon {
  font-size: 48px;
  color: #f44336;
  margin-bottom: 20px;
}

.btn-retry {
  margin-top: 20px;
  padding: 10px 20px;
  background-color: #2196F3;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
  margin-right: auto;
}
</style>

