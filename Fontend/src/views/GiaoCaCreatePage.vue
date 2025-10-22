<template>
  <div class="page dark-mode-transition">
    <PosHeader />
    
    <div class="content">
      <Toast ref="toastRef" />

      <!-- Header Section -->
      <div class="header-section">
        <div class="header-content">
          <div class="header-title">
            <h1>Tạo Giao Ca Mới</h1>
            <p>Thiết lập giao ca cho nhân viên</p>
          </div>
          <div class="header-actions">
            <button class="btn-back" @click="goBack">
              <font-awesome-icon :icon="['fas', 'arrow-left']" />
              Quay lại
            </button>
          </div>
        </div>
      </div>

      <!-- Form Section -->
      <div class="form-section">
        <form @submit.prevent="submitForm" class="giao-ca-form">
          <div class="form-grid">
            <!-- Ca làm việc -->
            <div class="form-group">
              <label class="form-label required">Ca làm việc</label>
              <select v-model="formData.phanCaId" @change="onPhanCaChange" class="form-select" required>
                <option value="">Chọn ca làm việc</option>
                <option v-for="phanCa in phanCaList" :key="phanCa.id" :value="phanCa.id">
                  {{ phanCa.caTen }} - {{ formatDate(phanCa.ngayLamViec) }}
                </option>
              </select>
              <div v-if="errors.phanCaId" class="error-message">{{ errors.phanCaId }}</div>
            </div>

            <!-- Nhân viên giao ca -->
            <div class="form-group">
              <label class="form-label required">Nhân viên giao ca</label>
              <select v-model="formData.nhanVienGiaoId" class="form-select" required>
                <option value="">Chọn nhân viên</option>
                <option v-for="nv in nhanVienList" :key="nv.id" :value="nv.id">
                  {{ nv.hoTen }} ({{ nv.maNhanVien }})
                </option>
              </select>
              <div v-if="errors.nhanVienGiaoId" class="error-message">{{ errors.nhanVienGiaoId }}</div>
            </div>

            <!-- Số tiền đầu ca -->
            <div class="form-group">
              <label class="form-label required">Số tiền đầu ca (VND)</label>
              <input 
                type="number" 
                v-model.number="formData.soTienDauCa" 
                class="form-input"
                placeholder="Nhập số tiền đầu ca"
                required
                min="0"
                step="1000"
              />
              <div v-if="errors.soTienDauCa" class="error-message">{{ errors.soTienDauCa }}</div>
            </div>

            <!-- Số tiền cuối ca -->
            <div class="form-group">
              <label class="form-label">Số tiền cuối ca (VND)</label>
              <input 
                type="number" 
                v-model.number="formData.soTienCuoiCa" 
                class="form-input"
                placeholder="Nhập số tiền cuối ca"
                min="0"
                step="1000"
              />
              <div v-if="errors.soTienCuoiCa" class="error-message">{{ errors.soTienCuoiCa }}</div>
            </div>

            <!-- Số tiền thu thêm -->
            <div class="form-group">
              <label class="form-label">Số tiền thu thêm (VND)</label>
              <input 
                type="number" 
                v-model.number="formData.soTienThuThem" 
                class="form-input"
                placeholder="Nhập số tiền thu thêm"
                min="0"
                step="1000"
              />
              <div v-if="errors.soTienThuThem" class="error-message">{{ errors.soTienThuThem }}</div>
            </div>

            <!-- Số tiền chi ra -->
            <div class="form-group">
              <label class="form-label">Số tiền chi ra (VND)</label>
              <input 
                type="number" 
                v-model.number="formData.soTienChiRa" 
                class="form-input"
                placeholder="Nhập số tiền chi ra"
                min="0"
                step="1000"
              />
              <div v-if="errors.soTienChiRa" class="error-message">{{ errors.soTienChiRa }}</div>
            </div>

            <!-- Tổng doanh thu -->
            <div class="form-group">
              <label class="form-label">Tổng doanh thu (VND)</label>
              <input 
                type="number" 
                v-model.number="formData.tongDoanhThu" 
                class="form-input"
                placeholder="Nhập tổng doanh thu"
                min="0"
                step="1000"
              />
              <div v-if="errors.tongDoanhThu" class="error-message">{{ errors.tongDoanhThu }}</div>
            </div>

            <!-- Số đơn hàng -->
            <div class="form-group">
              <label class="form-label">Số đơn hàng</label>
              <input 
                type="number" 
                v-model.number="formData.soDonHang" 
                class="form-input"
                placeholder="Nhập số đơn hàng"
                min="0"
              />
              <div v-if="errors.soDonHang" class="error-message">{{ errors.soDonHang }}</div>
            </div>

            <!-- Số đơn thanh toán tiền mặt -->
            <div class="form-group">
              <label class="form-label">Số đơn thanh toán tiền mặt</label>
              <input 
                type="number" 
                v-model.number="formData.soDonHangThanhToanTienMat" 
                class="form-input"
                placeholder="Nhập số đơn thanh toán tiền mặt"
                min="0"
              />
              <div v-if="errors.soDonHangThanhToanTienMat" class="error-message">{{ errors.soDonHangThanhToanTienMat }}</div>
            </div>

            <!-- Số đơn thanh toán chuyển khoản -->
            <div class="form-group">
              <label class="form-label">Số đơn thanh toán chuyển khoản</label>
              <input 
                type="number" 
                v-model.number="formData.soDonHangThanhToanChuyenKhoan" 
                class="form-input"
                placeholder="Nhập số đơn thanh toán chuyển khoản"
                min="0"
              />
              <div v-if="errors.soDonHangThanhToanChuyenKhoan" class="error-message">{{ errors.soDonHangThanhToanChuyenKhoan }}</div>
            </div>
          </div>

          <!-- Textarea fields -->
          <div class="form-textarea-group">
            <div class="form-group full-width">
              <label class="form-label">Báo cáo công việc</label>
              <textarea 
                v-model="formData.baoCaoCongViec" 
                class="form-textarea"
                placeholder="Mô tả công việc đã thực hiện trong ca..."
                rows="4"
              ></textarea>
              <div v-if="errors.baoCaoCongViec" class="error-message">{{ errors.baoCaoCongViec }}</div>
            </div>

            <div class="form-group full-width">
              <label class="form-label">Sự cố bất thường</label>
              <textarea 
                v-model="formData.suCoBatThuong" 
                class="form-textarea"
                placeholder="Mô tả các sự cố bất thường (nếu có)..."
                rows="3"
              ></textarea>
              <div v-if="errors.suCoBatThuong" class="error-message">{{ errors.suCoBatThuong }}</div>
            </div>

            <div class="form-group full-width">
              <label class="form-label">Công việc tồn đọng</label>
              <textarea 
                v-model="formData.congViecTonDong" 
                class="form-textarea"
                placeholder="Mô tả công việc tồn đọng cần xử lý..."
                rows="3"
              ></textarea>
              <div v-if="errors.congViecTonDong" class="error-message">{{ errors.congViecTonDong }}</div>
            </div>

            <div class="form-group full-width">
              <label class="form-label">Ghi chú</label>
              <textarea 
                v-model="formData.ghiChu" 
                class="form-textarea"
                placeholder="Ghi chú thêm (nếu có)..."
                rows="2"
              ></textarea>
              <div v-if="errors.ghiChu" class="error-message">{{ errors.ghiChu }}</div>
            </div>
          </div>

          <!-- Form Actions -->
          <div class="form-actions">
            <button type="button" class="btn-cancel" @click="goBack">
              <font-awesome-icon :icon="['fas', 'times']" />
              Hủy
            </button>
            <button type="submit" class="btn-submit" :disabled="loading">
              <font-awesome-icon v-if="loading" :icon="['fas', 'spinner']" class="fa-spin" />
              <font-awesome-icon v-else :icon="['fas', 'save']" />
              {{ loading ? 'Đang tạo...' : 'Tạo Giao Ca' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/services/api'
import Toast from '@/components/Toast.vue'
import PosHeader from '@/components/PosHeader.vue'

const router = useRouter()
const toastRef = ref<InstanceType<typeof Toast> | null>(null)
const loading = ref(false)

interface PhanCa {
  id: number
  caTen: string
  ngayLamViec: string
  nhanVienId: number
  nhanVienTen: string
}

interface NhanVien {
  id: number
  hoTen: string
  maNhanVien: string
}

const phanCaList = ref<PhanCa[]>([])
const nhanVienList = ref<NhanVien[]>([])

const formData = ref({
  phanCaId: '',
  nhanVienGiaoId: '',
  soTienDauCa: 0,
  soTienCuoiCa: 0,
  soTienThuThem: 0,
  soTienChiRa: 0,
  tongDoanhThu: 0,
  soDonHang: 0,
  soDonHangThanhToanTienMat: 0,
  soDonHangThanhToanChuyenKhoan: 0,
  baoCaoCongViec: '',
  suCoBatThuong: '',
  congViecTonDong: '',
  ghiChu: ''
})

const errors = ref<Record<string, string>>({})

async function loadPhanCaList() {
  try {
    const { data } = await api.get<PhanCa[]>('/api/phan-ca/active')
    phanCaList.value = data
  } catch (error) {
    console.error('Lỗi khi tải danh sách phân ca:', error)
    toastRef.value?.error('Lỗi', 'Không thể tải danh sách phân ca')
  }
}

async function loadNhanVienList() {
  try {
    const { data } = await api.get<NhanVien[]>('/api/nhan-vien')
    nhanVienList.value = data
  } catch (error) {
    console.error('Lỗi khi tải danh sách nhân viên:', error)
    toastRef.value?.error('Lỗi', 'Không thể tải danh sách nhân viên')
  }
}

function onPhanCaChange() {
  // Reset nhan vien giao ca when phan ca changes
  formData.value.nhanVienGiaoId = ''
  
  // Filter nhan vien based on selected phan ca
  const selectedPhanCa = phanCaList.value.find(pc => pc.id.toString() === formData.value.phanCaId)
  if (selectedPhanCa) {
    // Set the assigned employee as default
    formData.value.nhanVienGiaoId = selectedPhanCa.nhanVienId.toString()
  }
}

function validateForm(): boolean {
  errors.value = {}
  
  if (!formData.value.phanCaId) {
    errors.value.phanCaId = 'Vui lòng chọn ca làm việc'
  }
  
  if (!formData.value.nhanVienGiaoId) {
    errors.value.nhanVienGiaoId = 'Vui lòng chọn nhân viên giao ca'
  }
  
  if (formData.value.soTienDauCa < 0) {
    errors.value.soTienDauCa = 'Số tiền đầu ca không được âm'
  }
  
  if (formData.value.soTienCuoiCa < 0) {
    errors.value.soTienCuoiCa = 'Số tiền cuối ca không được âm'
  }
  
  if (formData.value.tongDoanhThu < 0) {
    errors.value.tongDoanhThu = 'Tổng doanh thu không được âm'
  }
  
  if (formData.value.soDonHang < 0) {
    errors.value.soDonHang = 'Số đơn hàng không được âm'
  }
  
  return Object.keys(errors.value).length === 0
}

async function submitForm() {
  if (!validateForm()) {
    toastRef.value?.error('Lỗi', 'Vui lòng kiểm tra lại thông tin')
    return
  }
  
  loading.value = true
  
  try {
    const payload = {
      phanCaId: parseInt(formData.value.phanCaId),
      nhanVienGiaoId: parseInt(formData.value.nhanVienGiaoId),
      soTienDauCa: formData.value.soTienDauCa,
      soTienCuoiCa: formData.value.soTienCuoiCa,
      soTienThuThem: formData.value.soTienThuThem,
      soTienChiRa: formData.value.soTienChiRa,
      tongDoanhThu: formData.value.tongDoanhThu,
      soDonHang: formData.value.soDonHang,
      soDonHangThanhToanTienMat: formData.value.soDonHangThanhToanTienMat,
      soDonHangThanhToanChuyenKhoan: formData.value.soDonHangThanhToanChuyenKhoan,
      baoCaoCongViec: formData.value.baoCaoCongViec,
      suCoBatThuong: formData.value.suCoBatThuong,
      congViecTonDong: formData.value.congViecTonDong,
      ghiChu: formData.value.ghiChu
    }
    
    await api.post('/api/giao-ca', payload)
    
    toastRef.value?.success('Thành công', 'Đã tạo giao ca thành công!')
    router.push('/quan-ly-giao-ca')
    
  } catch (error: any) {
    console.error('Lỗi khi tạo giao ca:', error)
    toastRef.value?.error('Lỗi', 'Không thể tạo giao ca: ' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push('/quan-ly-giao-ca')
}

function formatDate(dateString: string): string {
  if (!dateString) return ''
  return new Date(dateString).toLocaleDateString('vi-VN')
}

onMounted(() => {
  loadPhanCaList()
  loadNhanVienList()
})
</script>

<style scoped>
/* Import base styles - copy from VoucherPage.vue */

/* Page specific styles */
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
  border-radius: 16px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
  border: 1px solid #e2e8f0;
  position: relative;
  overflow: hidden;
}

.header-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #f97316, #ea580c, #dc2626);
  border-radius: 16px 16px 0 0;
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
  margin: 0 0 8px 0;
}

.header-title p {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.btn-back {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
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

/* Form Section */
.form-section {
  background: white;
  padding: 32px;
  border-radius: 16px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
  border: 1px solid #e2e8f0;
}

.giao-ca-form {
  max-width: 100%;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.form-textarea-group {
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
  font-weight: 600;
  font-size: 14px;
  color: #374151;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.form-label.required::after {
  content: '*';
  color: #dc2626;
  margin-left: 4px;
}

.form-label::before {
  content: '';
  width: 3px;
  height: 16px;
  background: linear-gradient(135deg, #f97316, #ea580c);
  border-radius: 2px;
}

.form-input,
.form-select,
.form-textarea {
  padding: 14px 18px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  background: #fafbfc;
  font-size: 14px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: #1f2937;
  font-weight: 500;
  font-family: inherit;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: #f97316;
  background: white;
  box-shadow: 0 0 0 4px rgba(249, 115, 22, 0.1), 0 4px 12px rgba(0, 0, 0, 0.05);
  transform: translateY(-1px);
}

.form-textarea {
  resize: vertical;
  min-height: 100px;
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

.error-message {
  color: #dc2626;
  font-size: 12px;
  font-weight: 500;
  margin-top: 4px;
}

/* Form Actions */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
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
  font-weight: 500;
  transition: all 0.2s ease;
}

.btn-cancel:hover {
  background: #f1f5f9;
  color: #374151;
  border-color: #cbd5e1;
  transform: translateY(-1px);
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
}

/* Responsive design */
@media (max-width: 768px) {
  .content {
    padding: 16px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .form-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .form-actions {
    flex-direction: column;
    gap: 12px;
  }
  
  .btn-cancel,
  .btn-submit {
    width: 100%;
    justify-content: center;
  }
}
</style>
