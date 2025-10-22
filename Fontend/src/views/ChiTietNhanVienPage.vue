<template>
  <div class="page">
    <PosHeader />

    <div class="content">
      <div class="header">
        <div class="header-left">
          <button class="btn-back" @click="goBack">
            <span class="back-icon">←</span>
            Quay lại
          </button>
          <h1>Chi Tiết Nhân Viên</h1>
        </div>
      </div>

      <Toast ref="toastRef" />

      <div class="form-container" v-if="nhanVien">
        <!-- Thông tin cơ bản -->
        <div class="info-section">
          <div class="section-header">
            <h3>Thông tin cơ bản</h3>
          </div>

          <div class="info-grid">
            <!-- Ảnh nhân viên -->
            <div class="image-section">
              <div class="employee-image">
                <img
                  v-if="nhanVien.anhDaiDien && !imageError"
                  :src="getImageUrl(nhanVien.anhDaiDien)"
                  alt="Ảnh nhân viên"
                  class="profile-image"
                  @error="handleImageError"
                  @load="handleImageLoad"
                  loading="lazy"
                />
                <div v-if="!nhanVien.anhDaiDien || imageError" class="no-image">
                  <i class="fas fa-user-circle"></i>
                  <p>{{ imageError ? 'Lỗi tải ảnh' : 'Chưa có ảnh' }}</p>
                </div>
              </div>
            </div>

            <!-- Thông tin chi tiết -->
            <div class="details-section">
              <div class="detail-row">
                <label>Mã nhân viên:</label>
                <span class="detail-value">{{ nhanVien.maNhanVien }}</span>
              </div>
              <div class="detail-row">
                <label>Họ và tên:</label>
                <span class="detail-value">{{ nhanVien.hoTen }}</span>
              </div>
              <div class="detail-row">
                <label>Số căn cước:</label>
                <span class="detail-value">{{ nhanVien.cccd || 'Chưa cập nhật' }}</span>
              </div>
              <div class="detail-row">
                <label>Số điện thoại:</label>
                <span class="detail-value">{{ nhanVien.soDienThoai }}</span>
              </div>
              <div class="detail-row">
                <label>Email:</label>
                <span class="detail-value">{{ nhanVien.email }}</span>
              </div>
              <div class="detail-row">
                <label>Ngày sinh:</label>
                <span class="detail-value">{{ formatDate(nhanVien.ngaySinh) }}</span>
              </div>
              <div class="detail-row">
                <label>Giới tính:</label>
                <span class="detail-value">{{ nhanVien.gioiTinh }}</span>
              </div>
              <div class="detail-row">
                <label>Chức vụ:</label>
                <span class="detail-value">{{ nhanVien.chucVu }}</span>
              </div>
              <div class="detail-row">
                <label>Quyền hạn:</label>
                <span class="role-badge" :class="getRoleClass(nhanVien.chucVu)">
                  {{ getRoleDisplayName(nhanVien.chucVu) }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- Thông tin tài khoản -->
        <div class="info-section">
          <div class="section-header">
            <h3>Thông tin tài khoản</h3>
          </div>

          <div class="account-info">
            <div class="detail-row">
              <label>Tài khoản:</label>
              <span class="detail-value">{{ nhanVien.taiKhoan }}</span>
            </div>
            <div class="detail-row">
              <label>Mật khẩu:</label>
              <span class="detail-value">••••••••</span>
            </div>
          </div>
        </div>

        <!-- Địa chỉ -->
        <div class="info-section" v-if="nhanVien.diaChi">
          <div class="section-header">
            <h3>Địa chỉ</h3>
          </div>

          <div class="address-info">
            <p class="address-text">{{ nhanVien.diaChi }}</p>
          </div>
        </div>

        <!-- Thông tin hệ thống -->
        <div class="info-section">
          <div class="section-header">
            <h3>Thông tin hệ thống</h3>
          </div>

          <div class="system-info">
            <div class="detail-row">
              <label>Ngày tạo:</label>
              <span class="detail-value">{{ formatDateTime(nhanVien.ngayTao) }}</span>
            </div>
            <div class="detail-row" v-if="nhanVien.ngayCapNhat">
              <label>Ngày cập nhật:</label>
              <span class="detail-value">{{ formatDateTime(nhanVien.ngayCapNhat) }}</span>
            </div>
          </div>
        </div>

        <!-- Actions -->
        <div class="actions-section">
          <button class="btn-secondary" @click="goBack">Đóng</button>
          <button class="btn-primary" @click="editEmployee">Chỉnh sửa</button>
        </div>
      </div>

      <!-- Loading state -->
      <div v-else-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>Đang tải thông tin nhân viên...</p>
      </div>

      <!-- Error state -->
      <div v-else class="error-container">
        <div class="error-icon">⚠️</div>
        <h3>Không tìm thấy nhân viên</h3>
        <p>Nhân viên bạn đang tìm kiếm không tồn tại hoặc đã bị xóa.</p>
        <button class="btn-primary" @click="goBack">Quay lại danh sách</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import Toast from '@/components/Toast.vue'
import PosHeader from '@/components/PosHeader.vue'
import api from '@/services/api'

const router = useRouter()
const route = useRoute()
const toastRef = ref()
const loading = ref(true)
const nhanVien = ref(null)
const imageError = ref(false)

// Get employee ID from route params
const employeeId = route.params.id

// Go back to employee list
function goBack() {
  router.push('/nhan-vien')
}

// Edit employee
function editEmployee() {
  router.push(`/nhan-vien/sua/${employeeId}`)
}

// Handle image error
function handleImageError(event: Event) {
  console.error('Image load error:', event)
  console.error('Failed to load image:', nhanVien.value?.anhDaiDien)
  console.error('Generated URL:', getImageUrl(nhanVien.value?.anhDaiDien || ''))
  console.error('Image source type:', typeof nhanVien.value?.anhDaiDien)

  // Set error state immediately
  imageError.value = true

  // Don't retry if it's a CORS error - it will fail again
  console.log('Image failed to load, showing fallback')
}

// Handle image load success
function handleImageLoad(event: Event) {
  console.log('Image loaded successfully:', event)
  imageError.value = false
}

// Check if image exists on server - disabled due to CORS issues
async function checkImageExists(imageUrl: string): Promise<boolean> {
  // Skip image existence check due to CORS policy
  // Let the image element handle the error naturally
  console.log('Skipping image existence check due to CORS policy')
  return true
}

// Get image URL - use static resource serving (like product images)
function getImageUrl(imagePath: string) {
  if (!imagePath) {
    console.log('No image path provided')
    return ''
  }

  if (imagePath.startsWith('http')) {
    console.log('Using external URL:', imagePath)
    return imagePath
  }

  const baseUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'

  // If path already starts with /uploads/, use it directly
  if (imagePath.startsWith('/uploads/')) {
    const imageUrl = `${baseUrl}${imagePath}`
    console.log('Using direct uploads path:', imageUrl)
    return imageUrl
  }

  // If path doesn't start with /uploads/, add it
  const imageUrl = `${baseUrl}/uploads/${imagePath}`
  console.log('Generated uploads URL:', imageUrl)
  return imageUrl
}

// Format date
function formatDate(dateString: string) {
  if (!dateString) return 'Chưa cập nhật'
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN')
}

// Format date time
function formatDateTime(dateString: string) {
  if (!dateString) return 'Chưa cập nhật'
  const date = new Date(dateString)
  return date.toLocaleString('vi-VN')
}

// Get role display name from chuc_vu
function getRoleDisplayName(chucVu: string) {
  if (!chucVu) return 'Chưa xác định'

  const chucVuLower = chucVu.toLowerCase().trim()

  // Admin positions
  if (
    chucVuLower.includes('quản trị') ||
    chucVuLower.includes('admin') ||
    chucVuLower.includes('administrator')
  ) {
    return 'Quản trị viên'
  }

  // Manager positions
  if (
    chucVuLower.includes('quản lý') ||
    chucVuLower.includes('manager') ||
    chucVuLower.includes('giám đốc') ||
    chucVuLower.includes('director') ||
    chucVuLower.includes('trưởng phòng')
  ) {
    return 'Quản lý'
  }

  // Staff positions
  return 'Nhân viên'
}

// Get role CSS class from chuc_vu
function getRoleClass(chucVu: string) {
  if (!chucVu) return 'default'

  const chucVuLower = chucVu.toLowerCase().trim()

  // Admin positions
  if (
    chucVuLower.includes('quản trị') ||
    chucVuLower.includes('admin') ||
    chucVuLower.includes('administrator')
  ) {
    return 'admin'
  }

  // Manager positions
  if (
    chucVuLower.includes('quản lý') ||
    chucVuLower.includes('manager') ||
    chucVuLower.includes('giám đốc') ||
    chucVuLower.includes('director') ||
    chucVuLower.includes('trưởng phòng')
  ) {
    return 'manager'
  }

  // Staff positions
  return 'staff'
}

// Load employee details
async function loadEmployeeDetails() {
  try {
    loading.value = true
    imageError.value = false // Reset image error state
    const response = await api.get(`/api/nhan-vien/${employeeId}`)
    nhanVien.value = response.data
    console.log('Employee data:', response.data) // Debug log

    // Skip image existence check due to CORS issues
    // Let the image element handle loading errors naturally
    if (nhanVien.value?.anhDaiDien) {
      const imageUrl = getImageUrl(nhanVien.value.anhDaiDien)
      console.log('Employee has avatar, URL will be:', imageUrl)
    }
  } catch (error) {
    console.error('Error loading employee details:', error)
    toastRef.value?.error('Lỗi', 'Không thể tải thông tin nhân viên')
    nhanVien.value = null
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadEmployeeDetails()
})
</script>

<style scoped>
.page {
  padding: 20px;
  padding-top: 80px;
}

.content {
  margin-top: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.btn-back {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: #6c757d;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
  font-size: 13px;
}

.btn-back:hover {
  background: #5a6268;
}

.back-icon {
  font-size: 14px;
  font-weight: bold;
}

h1 {
  margin: 0;
  color: #333;
  font-size: 24px;
  font-weight: 600;
}

.form-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 30px;
  width: 100%;
  margin: 0 auto;
}

.info-section {
  margin-bottom: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
  border: 1px solid #e9ecef;
}

.section-header {
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #f97316;
}

.section-header h3 {
  margin: 0;
  color: #333;
  font-size: 18px;
  font-weight: 600;
}

.info-grid {
  display: grid;
  grid-template-columns: minmax(200px, 300px) 1fr;
  gap: 40px;
  align-items: start;
}

.image-section {
  display: flex;
  justify-content: center;
}

.employee-image {
  width: 100%;
  max-width: 280px;
  aspect-ratio: 1;
  border-radius: 12px;
  overflow: hidden;
  border: 3px solid #f97316;
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.2);
  position: relative;
}

.profile-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-image {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #e9ecef;
  color: #6c757d;
  position: absolute;
  top: 0;
  left: 0;
}

.no-image i {
  font-size: 48px;
  margin-bottom: 8px;
}

.no-image p {
  margin: 0;
  font-size: 14px;
  font-weight: 500;
}

.details-section {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.detail-row {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 10px 0;
  border-bottom: 1px solid #e9ecef;
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-row label {
  min-width: 120px;
  font-weight: 600;
  color: #495057;
  font-size: 14px;
}

.detail-value {
  color: #333;
  font-size: 14px;
  font-weight: 500;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-badge.active {
  background: #d1fae5;
  color: #065f46;
}

.status-badge.inactive {
  background: #fee2e2;
  color: #991b1b;
}

.role-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.role-badge.admin {
  background: #fef3c7;
  color: #92400e;
}

.role-badge.manager {
  background: #dbeafe;
  color: #1e40af;
}

.role-badge.staff {
  background: #d1fae5;
  color: #065f46;
}

.role-badge.default {
  background: #f3f4f6;
  color: #374151;
}

.account-info,
.address-info,
.system-info {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.address-text {
  margin: 0;
  color: #333;
  font-size: 14px;
  line-height: 1.6;
  padding: 15px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.actions-section {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding-top: 20px;
  border-top: 1px solid #e9ecef;
  margin-top: 20px;
}

.btn-primary,
.btn-secondary {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-primary {
  background: #f97316 !important;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #ea580c !important;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #5a6268;
}

.loading-container,
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  text-align: center;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 40px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #f97316;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.error-icon {
  font-size: 48px;
  margin-bottom: 20px;
}

.error-container h3 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 20px;
  font-weight: 600;
}

.error-container p {
  margin: 0 0 20px 0;
  color: #6c757d;
  font-size: 14px;
}

/* Responsive */
@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .employee-image {
    max-width: 200px;
  }

  .detail-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }

  .detail-row label {
    min-width: auto;
  }

  .actions-section {
    flex-direction: column;
  }
}

/* Large screens */
@media (min-width: 1400px) {
  .form-container {
    padding: 40px;
  }
  
  .info-grid {
    gap: 50px;
  }
  
  .employee-image {
    max-width: 320px;
  }
}

/* Medium screens */
@media (max-width: 1200px) {
  .form-container {
    padding: 25px;
  }
  
  .info-grid {
    gap: 30px;
  }
  
  .employee-image {
    max-width: 250px;
  }
}

/* Extra small screens */
@media (max-width: 480px) {
  .form-container {
    padding: 15px;
  }
  
  .employee-image {
    max-width: 150px;
  }
}
</style>





