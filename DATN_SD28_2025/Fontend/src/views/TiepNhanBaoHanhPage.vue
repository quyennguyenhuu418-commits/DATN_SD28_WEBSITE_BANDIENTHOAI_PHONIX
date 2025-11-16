<template>
  <div class="form-page">
    <PosHeader />
    <Toast ref="toastRef" />
    
    <div class="form-container">
      <div class="form-header">
        <div class="header-left">
          <button class="btn-back" @click="goBack">
            <FontAwesomeIcon :icon="['fas', 'arrow-left']" />
            Quay lại
          </button>
          <h1>Tiếp nhận Bảo hành Mới</h1>
        </div>
      </div>

      <form @submit.prevent="handleSubmit" class="bao-hanh-form">
        <!-- Thông tin khách hàng -->
        <div class="form-section">
          <h2>Thông tin Khách hàng</h2>
          <div class="form-grid">
            <div class="form-group">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'user']" />
                <span>Tên khách hàng *</span>
              </label>
              <input 
                type="text" 
                v-model="formData.tenKhachHang" 
                class="form-input"
                placeholder="Nhập tên khách hàng"
                required
              />
              <small class="form-help">Tên khách hàng hoặc thông tin liên hệ</small>
            </div>
            
            <div class="form-group">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'phone']" />
                <span>Số điện thoại *</span>
              </label>
              <input 
                type="tel" 
                v-model="formData.soDienThoai" 
                class="form-input"
                placeholder="Nhập số điện thoại"
                required
              />
            </div>

            <div class="form-group">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'id-card']" />
                <span>Mã khách hàng (nếu có)</span>
              </label>
              <input 
                type="number" 
                v-model.number="formData.khachHangId" 
                class="form-input"
                placeholder="ID khách hàng trong hệ thống"
              />
              <button type="button" class="btn-search-customer" @click="searchCustomer">
                <FontAwesomeIcon :icon="['fas', 'search']" />
                Tìm khách hàng
              </button>
            </div>
          </div>
        </div>

        <!-- Thông tin sản phẩm -->
        <div class="form-section">
          <h2>Thông tin Sản phẩm</h2>
          <div class="form-grid">
            <div class="form-group">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'mobile-alt']" />
                <span>Tên sản phẩm/Model *</span>
              </label>
              <input 
                type="text" 
                v-model="formData.tenSanPham" 
                class="form-input"
                placeholder="Nhập tên sản phẩm"
                required
              />
            </div>

            <div class="form-group">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'barcode']" />
                <span>Số IMEI/Serial *</span>
              </label>
              <input 
                type="text" 
                v-model="formData.imeiSerial" 
                class="form-input"
                placeholder="Nhập IMEI hoặc Serial"
                required
              />
              <small class="form-help">Kiểm tra số IMEI/Serial có trùng khớp với hóa đơn</small>
            </div>

            <div class="form-group">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'receipt']" />
                <span>Số hóa đơn (nếu có)</span>
              </label>
              <input 
                type="number" 
                v-model.number="formData.hoaDonId" 
                class="form-input"
                placeholder="ID hóa đơn mua hàng"
              />
              <button type="button" class="btn-search-invoice" @click="searchInvoice">
                <FontAwesomeIcon :icon="['fas', 'search']" />
                Tìm hóa đơn
              </button>
            </div>

            <div class="form-group">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'box']" />
                <span>Mã sản phẩm (nếu có)</span>
              </label>
              <input 
                type="number" 
                v-model.number="formData.sanPhamId" 
                class="form-input"
                placeholder="ID sản phẩm"
              />
            </div>

            <div class="form-group">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'layer-group']" />
                <span>Chi tiết sản phẩm (nếu có)</span>
              </label>
              <input 
                type="number" 
                v-model.number="formData.chiTietSanPhamId" 
                class="form-input"
                placeholder="ID chi tiết sản phẩm"
              />
            </div>
          </div>
        </div>

        <!-- Tình trạng tiếp nhận -->
        <div class="form-section">
          <h2>Tình trạng Tiếp nhận</h2>
          <div class="form-grid">
            <div class="form-group full-width">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'comment-alt']" />
                <span>Mô tả lỗi (theo khách hàng) *</span>
              </label>
              <textarea 
                v-model="formData.moTaLoiKhachHang" 
                class="form-textarea"
                rows="4"
                placeholder="Nhập mô tả lỗi mà khách hàng trình bày"
                required
              ></textarea>
            </div>

            <div class="form-group full-width">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'clipboard-check']" />
                <span>Mô tả lỗi (nhân viên ghi nhận)</span>
              </label>
              <textarea 
                v-model="formData.moTaLoiNhanVien" 
                class="form-textarea"
                rows="4"
                placeholder="Nhập mô tả lỗi sau khi kiểm tra nhanh"
              ></textarea>
            </div>

            <div class="form-group full-width">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'exclamation-triangle']" />
                <span>Tình trạng vật lý (trầy xước, cấn móp, v.v.)</span>
              </label>
              <textarea 
                v-model="formData.tinhTrangVatLy" 
                class="form-textarea"
                rows="3"
                placeholder="Mô tả tình trạng vật lý của sản phẩm, nên chụp ảnh lại"
              ></textarea>
              <small class="form-help">Nên chụp ảnh lại các vết trầy xước, cấn móp nếu có</small>
            </div>

            <div class="form-group full-width">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'luggage-cart']" />
                <span>Phụ kiện đi kèm</span>
              </label>
              <input 
                type="text" 
                v-model="formData.phuKienDiKem" 
                class="form-input"
                placeholder="Ví dụ: Sạc, cáp, hộp, tai nghe..."
              />
              <small class="form-help">Liệt kê các phụ kiện khách hàng gửi kèm</small>
            </div>
          </div>
        </div>

        <!-- Lịch hẹn -->
        <div class="form-section">
          <h2>Lịch hẹn</h2>
          <div class="form-grid">
            <div class="form-group">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'calendar-alt']" />
                <span>Ngày nhận</span>
              </label>
              <input 
                type="date" 
                v-model="formData.ngayNhan" 
                class="form-input"
              />
              <small class="form-help">Mặc định là ngày hôm nay</small>
            </div>

            <div class="form-group">
              <label class="form-label">
                <FontAwesomeIcon :icon="['fas', 'calendar-check']" />
                <span>Ngày hẹn trả (dự kiến) *</span>
              </label>
              <input 
                type="date" 
                v-model="formData.ngayHenTraDuKien" 
                class="form-input"
                required
              />
              <small class="form-help">Ngày dự kiến trả máy cho khách hàng</small>
            </div>
          </div>
        </div>

        <!-- Ghi chú -->
        <div class="form-section">
          <h2>Ghi chú</h2>
          <div class="form-group full-width">
            <textarea 
              v-model="formData.ghiChu" 
              class="form-textarea"
              rows="3"
              placeholder="Ghi chú thêm (nếu có)"
            ></textarea>
          </div>
        </div>

        <!-- Form Actions -->
        <div class="form-actions">
          <button type="button" class="btn-cancel" @click="goBack">
            Hủy
          </button>
          <button type="submit" class="btn-submit" :disabled="isSubmitting">
            <FontAwesomeIcon :icon="['fas', 'save']" v-if="!isSubmitting" />
            <span v-if="isSubmitting">Đang lưu...</span>
            <span v-else>Tiếp nhận Bảo hành</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { FontAwesomeIcon } from '@/plugins/fontawesome'
import PosHeader from '@/components/PosHeader.vue'
import Toast from '@/components/Toast.vue'
import api from '@/services/api'

const router = useRouter()
const toastRef = ref<InstanceType<typeof Toast> | null>(null)
const isSubmitting = ref(false)

interface FormData {
  khachHangId?: number
  tenKhachHang: string
  soDienThoai: string
  sanPhamId?: number
  chiTietSanPhamId?: number
  hoaDonId?: number
  tenSanPham: string
  imeiSerial: string
  moTaLoiKhachHang: string
  moTaLoiNhanVien: string
  tinhTrangVatLy: string
  phuKienDiKem: string
  ngayNhan: string
  ngayHenTraDuKien: string
  ghiChu: string
}

const formData = ref<FormData>({
  tenKhachHang: '',
  soDienThoai: '',
  tenSanPham: '',
  imeiSerial: '',
  moTaLoiKhachHang: '',
  moTaLoiNhanVien: '',
  tinhTrangVatLy: '',
  phuKienDiKem: '',
  ngayNhan: new Date().toISOString().split('T')[0],
  ngayHenTraDuKien: '',
  ghiChu: ''
})

function goBack() {
  router.push('/bao-hanh')
}

async function searchCustomer() {
  if (!formData.value.soDienThoai && !formData.value.khachHangId) {
    toastRef.value?.warning('Cảnh báo', 'Vui lòng nhập số điện thoại hoặc mã khách hàng để tìm kiếm')
    return
  }
  
  try {
    const searchQuery = formData.value.soDienThoai || formData.value.khachHangId?.toString()
    const response = await api.get(`/api/khach-hang/search?query=${searchQuery}`)
    const customers = response.data || []
    
    if (customers.length > 0) {
      const customer = customers[0]
      formData.value.khachHangId = customer.id
      formData.value.tenKhachHang = customer.hoTen
      formData.value.soDienThoai = customer.soDienThoai
      toastRef.value?.success('Thành công', 'Đã tìm thấy khách hàng')
    } else {
      toastRef.value?.warning('Không tìm thấy', 'Không tìm thấy khách hàng trong hệ thống')
    }
  } catch (error: any) {
    toastRef.value?.error('Lỗi', 'Không thể tìm kiếm khách hàng: ' + (error.message || 'Unknown error'))
  }
}

async function searchInvoice() {
  if (!formData.value.hoaDonId && !formData.value.imeiSerial) {
    toastRef.value?.warning('Cảnh báo', 'Vui lòng nhập số hóa đơn hoặc IMEI để tìm kiếm')
    return
  }
  
  try {
    const searchQuery = formData.value.hoaDonId?.toString() || formData.value.imeiSerial
    const response = await api.get(`/api/hoa-don/search?query=${searchQuery}`)
    const hoaDons = response.data || []
    
    if (hoaDons.length > 0) {
      const hoaDon = hoaDons[0]
      formData.value.hoaDonId = hoaDon.id
      toastRef.value?.success('Thành công', 'Đã tìm thấy hóa đơn')
    } else {
      toastRef.value?.warning('Không tìm thấy', 'Không tìm thấy hóa đơn trong hệ thống')
    }
  } catch (error: any) {
    toastRef.value?.error('Lỗi', 'Không thể tìm kiếm hóa đơn: ' + (error.message || 'Unknown error'))
  }
}

async function handleSubmit() {
  if (!validateForm()) {
    return
  }

  isSubmitting.value = true
  try {
    const payload = {
      ...formData.value,
      ngayNhan: formData.value.ngayNhan || new Date().toISOString().split('T')[0]
    }

    const response = await api.post('/api/bao-hanh/tiep-nhan', payload)
    
    toastRef.value?.success('Thành công', 'Đã tiếp nhận yêu cầu bảo hành thành công')
    
    // Redirect to detail page
    setTimeout(() => {
      router.push(`/bao-hanh/chi-tiet/${response.data.id}`)
    }, 1500)
  } catch (error: any) {
    console.error('Error submitting form:', error)
    toastRef.value?.error('Lỗi', 'Không thể tiếp nhận bảo hành: ' + (error.response?.data?.message || error.message || 'Unknown error'))
  } finally {
    isSubmitting.value = false
  }
}

function validateForm(): boolean {
  if (!formData.value.tenKhachHang?.trim()) {
    toastRef.value?.error('Lỗi', 'Vui lòng nhập tên khách hàng')
    return false
  }
  
  if (!formData.value.soDienThoai?.trim()) {
    toastRef.value?.error('Lỗi', 'Vui lòng nhập số điện thoại')
    return false
  }
  
  if (!formData.value.tenSanPham?.trim()) {
    toastRef.value?.error('Lỗi', 'Vui lòng nhập tên sản phẩm')
    return false
  }
  
  if (!formData.value.imeiSerial?.trim()) {
    toastRef.value?.error('Lỗi', 'Vui lòng nhập số IMEI/Serial')
    return false
  }
  
  if (!formData.value.moTaLoiKhachHang?.trim()) {
    toastRef.value?.error('Lỗi', 'Vui lòng nhập mô tả lỗi theo khách hàng')
    return false
  }
  
  if (!formData.value.ngayHenTraDuKien) {
    toastRef.value?.error('Lỗi', 'Vui lòng chọn ngày hẹn trả dự kiến')
    return false
  }
  
  return true
}

onMounted(() => {
  // Set default date for ngayNhan
  if (!formData.value.ngayNhan) {
    formData.value.ngayNhan = new Date().toISOString().split('T')[0]
  }
})
</script>

<style scoped>
.form-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20px;
}

.form-container {
  max-width: 1200px;
  margin: 0 auto;
  background: white;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #e0e0e0;
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
  font-size: 14px;
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

.form-section {
  margin-bottom: 30px;
  padding: 20px;
  background-color: #fafafa;
  border-radius: 6px;
  border: 1px solid #e0e0e0;
}

.form-section h2 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 18px;
  border-bottom: 2px solid #2196F3;
  padding-bottom: 10px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-weight: 500;
  color: #555;
}

.form-input,
.form-textarea,
.form-select {
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.form-input:focus,
.form-textarea:focus,
.form-select:focus {
  outline: none;
  border-color: #2196F3;
}

.form-textarea {
  resize: vertical;
  font-family: inherit;
}

.form-help {
  margin-top: 5px;
  font-size: 12px;
  color: #666;
}

.btn-search-customer,
.btn-search-invoice {
  margin-top: 8px;
  padding: 8px 16px;
  background-color: #2196F3;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: background-color 0.3s;
}

.btn-search-customer:hover,
.btn-search-invoice:hover {
  background-color: #1976D2;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 30px;
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
</style>

