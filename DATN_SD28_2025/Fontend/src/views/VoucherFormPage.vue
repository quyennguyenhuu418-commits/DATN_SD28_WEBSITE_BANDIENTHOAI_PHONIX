<template>
  <div class="form-page">
    <div class="form-header">
      <div class="header-left">
        <button class="btn-back" @click="goBack">
          <i class="icon-arrow-left"></i>
          Quay lại
        </button>
        <h1>{{ isEdit ? 'Sửa Phiếu Giảm Giá' : 'Thêm Phiếu Giảm Giá' }}</h1>
      </div>
    </div>

    <div class="form-container">
      <form @submit.prevent="handleSubmit" class="voucher-form">
        <div class="form-section">
          <h2>Thông Tin Cơ Bản</h2>
          <div class="form-row">
            <div class="form-group">
              <label for="maPhieuGiamGia">Mã Phiếu Giảm Giá:</label>
              <input
                id="maPhieuGiamGia"
                type="text"
                v-model="formData.maPhieuGiamGia"
                placeholder="Để trống để tự động tạo"
              />
            </div>
            <div class="form-group">
              <label for="tenPhieuGiamGia">Tên Phiếu Giảm Giá: <span class="required">*</span></label>
              <input
                id="tenPhieuGiamGia"
                type="text"
                v-model="formData.tenPhieuGiamGia"
                :class="{ 'error': errors.tenPhieuGiamGia }"
                required
                placeholder="Nhập tên phiếu giảm giá"
              />
              <span v-if="errors.tenPhieuGiamGia" class="error-message">{{ errors.tenPhieuGiamGia }}</span>
            </div>
          </div>

          <div class="form-group">
            <label for="moTa">Mô Tả:</label>
            <textarea
              id="moTa"
              v-model="formData.moTa"
              rows="3"
              placeholder="Mô tả chi tiết về phiếu giảm giá"
            ></textarea>
          </div>
        </div>

        <div class="form-section">
          <h2>Cài Đặt Giảm Giá</h2>
          <div class="form-row">
            <div class="form-group">
              <label for="loaiPhieuGiamGia">Loại Phiếu: <span class="required">*</span></label>
              <select id="loaiPhieuGiamGia" v-model="formData.loaiPhieuGiamGia" :class="{ 'error': errors.loaiPhieuGiamGia }" required>
                <option value="">-- Chọn loại --</option>
                <option value="PERCENT">Phần trăm (%)</option>
                <option value="FIXED">Số tiền cố định (VND)</option>
              </select>
              <span v-if="errors.loaiPhieuGiamGia" class="error-message">{{ errors.loaiPhieuGiamGia }}</span>
            </div>
            <div class="form-group">
              <label for="giaTriGiamGia">
                {{ formData.loaiPhieuGiamGia === 'PERCENT' ? 'Phần trăm giảm (%)' : 'Số tiền giảm (VND)' }}: 
                <span class="required">*</span>
              </label>
              <input
                id="giaTriGiamGia"
                type="number"
                v-model="formData.giaTriGiamGia"
                :class="{ 'error': errors.giaTriGiamGia }"
                required
                :placeholder="formData.loaiPhieuGiamGia === 'PERCENT' ? 'Ví dụ: 15' : 'Ví dụ: 50000'"
                :max="formData.loaiPhieuGiamGia === 'PERCENT' ? 100 : undefined"
                min="0"
                step="0.01"
              />
              <span v-if="errors.giaTriGiamGia" class="error-message">{{ errors.giaTriGiamGia }}</span>
            </div>
          </div>

          <div class="form-row" v-if="formData.loaiPhieuGiamGia === 'PERCENT'">
            <div class="form-group">
              <label for="soTienGiamToiDa">Số Tiền Giảm Tối Đa (VND):</label>
              <input
                id="soTienGiamToiDa"
                type="number"
                v-model="formData.soTienGiamToiDa"
                :class="{ 'error': errors.soTienGiamToiDa }"
                placeholder="Ví dụ: 100000"
                min="0"
              />
              <span v-if="errors.soTienGiamToiDa" class="error-message">{{ errors.soTienGiamToiDa }}</span>
            </div>
            <div class="form-group">
              <label for="hoaDonToiThieu">Hóa Đơn Tối Thiểu (VND):</label>
              <input
                id="hoaDonToiThieu"
                type="number"
                v-model="formData.hoaDonToiThieu"
                :class="{ 'error': errors.hoaDonToiThieu }"
                placeholder="Ví dụ: 200000"
                min="0"
              />
              <span v-if="errors.hoaDonToiThieu" class="error-message">{{ errors.hoaDonToiThieu }}</span>
            </div>
          </div>

          <div class="form-row" v-if="formData.loaiPhieuGiamGia === 'FIXED'">
            <div class="form-group">
              <label for="hoaDonToiThieuFixed">Hóa Đơn Tối Thiểu (VND):</label>
              <input
                id="hoaDonToiThieuFixed"
                type="number"
                v-model="formData.hoaDonToiThieu"
                :class="{ 'error': errors.hoaDonToiThieu }"
                placeholder="Ví dụ: 200000"
                min="0"
              />
              <span v-if="errors.hoaDonToiThieu" class="error-message">{{ errors.hoaDonToiThieu }}</span>
            </div>
            <div class="form-group">
              <label for="soLuongDung">Số Lượng Sử Dụng: <span class="required">*</span></label>
              <input
                id="soLuongDung"
                type="number"
                v-model="formData.soLuongDung"
                :class="{ 'error': errors.soLuongDung }"
                required
                placeholder="Ví dụ: 100"
                min="1"
              />
              <span v-if="errors.soLuongDung" class="error-message">{{ errors.soLuongDung }}</span>
            </div>
          </div>

          <div class="form-row" v-if="formData.loaiPhieuGiamGia === 'PERCENT'">
            <div class="form-group">
              <label for="soLuongDungPercent">Số Lượng Sử Dụng: <span class="required">*</span></label>
              <input
                id="soLuongDungPercent"
                type="number"
                v-model="formData.soLuongDung"
                :class="{ 'error': errors.soLuongDung }"
                required
                placeholder="Ví dụ: 100"
                min="1"
              />
              <span v-if="errors.soLuongDung" class="error-message">{{ errors.soLuongDung }}</span>
            </div>
          </div>
        </div>

        <div class="form-section">
          <h2>Thời Gian Áp Dụng</h2>
          <div class="form-row">
            <div class="form-group">
              <label for="ngayBatDau">Ngày Bắt Đầu: <span class="required">*</span></label>
              <input
                id="ngayBatDau"
                type="date"
                v-model="formData.ngayBatDau"
                :class="{ 'error': errors.ngayBatDau }"
                required
              />
              <span v-if="errors.ngayBatDau" class="error-message">{{ errors.ngayBatDau }}</span>
            </div>
            <div class="form-group">
              <label for="ngayKetThuc">Ngày Kết Thúc: <span class="required">*</span></label>
              <input
                id="ngayKetThuc"
                type="date"
                v-model="formData.ngayKetThuc"
                :class="{ 'error': errors.ngayKetThuc }"
                required
              />
              <span v-if="errors.ngayKetThuc" class="error-message">{{ errors.ngayKetThuc }}</span>
            </div>
          </div>
        </div>

        <div class="form-section">
          <h2>Cài Đặt Khác</h2>
          <div class="form-row">
            <div class="form-group">
              <label class="checkbox-label">
                <input type="checkbox" v-model="formData.riengTu" />
                <span class="checkmark"></span>
                Voucher riêng tư (chỉ dành cho khách hàng cụ thể)
              </label>
            </div>
            <div class="form-group">
              <label class="checkbox-label">
                <input type="checkbox" v-model="formData.trangThai" />
                <span class="checkmark"></span>
                Kích hoạt ngay sau khi tạo
              </label>
            </div>
          </div>

          <!-- Customer Selection for Private Vouchers -->
          <div v-if="formData.riengTu" class="customer-selection-section">
            <h3>Chọn Khách Hàng Cho Voucher Riêng Tư</h3>
            <div class="customer-selection-info">
              <p><strong>Lưu ý:</strong> Mỗi khách hàng được chọn sẽ có 1 phiếu riêng. Khi khách hàng sử dụng, phiếu của họ sẽ biến mất nhưng khách hàng khác vẫn còn phiếu.</p>
              <p><strong>Số phiếu sẽ tạo:</strong> {{ selectedCustomers.length }} phiếu (1 phiếu/khách hàng)</p>
              <p>Đã chọn: {{ selectedCustomers.length }} khách hàng</p>
            </div>
            
            <button type="button" @click="openCustomerModal" class="btn-select-customer">
              Chọn khách hàng
            </button>
            
            <div v-if="selectedCustomers.length > 0" class="selected-customers">
              <div v-for="customer in selectedCustomers" :key="customer.id" class="customer-item">
                <div class="customer-info">
                  <span class="customer-name">{{ customer.hoTen }}</span>
                  <span class="customer-phone">{{ customer.soDienThoai }}</span>
                </div>
                <button type="button" @click="removeCustomer(customer.id)" class="btn-remove-customer">
                  ×
                </button>
              </div>
            </div>
            
            <span v-if="errors.selectedCustomers" class="error-message">{{ errors.selectedCustomers }}</span>
          </div>
        </div>

        <div class="form-actions">
          <button type="button" @click="goBack" class="btn-cancel">Hủy</button>
          <button type="submit" class="btn-submit" :disabled="loading">
            <span v-if="loading">Đang lưu...</span>
            <span v-else>{{ isEdit ? 'Cập nhật' : 'Tạo mới' }}</span>
          </button>
        </div>
      </form>
    </div>

    <Toast ref="toastRef" />

    <!-- Customer Selection Modal -->
    <div v-if="showCustomerModal" class="modal-overlay" @click="closeCustomerModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>Chọn Khách Hàng</h3>
          <button type="button" @click="closeCustomerModal" class="btn-close-modal">×</button>
        </div>
        
        <div class="modal-body">
          <div class="search-section">
            <input
              type="text"
              v-model="searchText"
              @input="searchCustomers"
              placeholder="Tìm kiếm khách hàng theo tên hoặc số điện thoại..."
              class="search-input"
            />
          </div>
          
          <div v-if="searching" class="loading">
            Đang tìm kiếm...
          </div>
          
          <div v-else-if="customers.length > 0" class="customer-list">
            <div
              v-for="customer in customers"
              :key="customer.id"
              class="customer-option"
              @click="selectCustomer(customer)"
            >
              <div class="customer-info">
                <div class="customer-name">{{ customer.hoTen }}</div>
                <div class="customer-phone">{{ customer.soDienThoai }}</div>
              </div>
              <button type="button" class="btn-select">Chọn</button>
            </div>
          </div>
          
          <div v-else-if="searchText.length >= 2" class="no-results">
            Không tìm thấy khách hàng nào
          </div>
          
          <div v-else class="search-hint">
            Nhập ít nhất 2 ký tự để tìm kiếm
          </div>
        </div>
        
        <div class="modal-footer">
          <button type="button" @click="closeCustomerModal" class="btn-cancel">Đóng</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '@/services/api'
import Toast from '@/components/Toast.vue'

const router = useRouter()
const route = useRoute()
const toastRef = ref<InstanceType<typeof Toast> | null>(null)
const loading = ref(false)

const isEdit = ref(false)
const voucherId = ref<number | null>(null)

const formData = ref({
  maPhieuGiamGia: '',
  tenPhieuGiamGia: '',
  loaiPhieuGiamGia: '',
  giaTriGiamGia: 0,
  soTienGiamToiDa: 0,
  hoaDonToiThieu: 0,
  soLuongDung: 1,
  ngayBatDau: '',
  ngayKetThuc: '',
  riengTu: false,
  moTa: '',
  trangThai: true,
})

// Validation errors
const errors = ref({
  tenPhieuGiamGia: '',
  loaiPhieuGiamGia: '',
  giaTriGiamGia: '',
  soLuongDung: '',
  ngayBatDau: '',
  ngayKetThuc: '',
  soTienGiamToiDa: '',
  hoaDonToiThieu: '',
  selectedCustomers: ''
})

// Customer selection states
const selectedCustomers = ref([])
const customers = ref([])
const searchText = ref('')
const showCustomerModal = ref(false)
const searching = ref(false)


onMounted(async () => {
  const id = route.params.id
  console.log('=== VoucherFormPage onMounted ===')
  console.log('Route params:', route.params)
  console.log('ID from params:', id)
  console.log('ID type:', typeof id)
  
  if (id && id !== 'new') {
    isEdit.value = true
    const parsedId = parseInt(id as string)
    console.log('Parsed ID:', parsedId)
    console.log('Is valid number:', !isNaN(parsedId))
    
    if (!isNaN(parsedId)) {
      voucherId.value = parsedId
      await loadVoucher()
    } else {
      console.error('Invalid voucher ID:', id)
      toastRef.value?.error('Lỗi', 'ID voucher không hợp lệ')
      goBack()
    }
  } else {
    // Set default dates
    const today = new Date()
    const nextMonth = new Date(today)
    nextMonth.setMonth(today.getMonth() + 1)
    
    formData.value.ngayBatDau = today.toISOString().split('T')[0]
    formData.value.ngayKetThuc = nextMonth.toISOString().split('T')[0]
  }
})

async function loadVoucher() {
  if (!voucherId.value) return
  
  try {
    console.log('Loading voucher with ID:', voucherId.value)
    const { data } = await api.get(`/api/phieu-giam-gia/${voucherId.value}`)
    console.log('Loaded voucher data:', data)
    
    formData.value = {
      ...data,
      ngayBatDau: data.ngayBatDau?.split('T')[0] || '',
      ngayKetThuc: data.ngayKetThuc?.split('T')[0] || '',
      trangThai: data.trangThai === 1
    }
    
    // Load selected customers for private vouchers
    if (data.riengTu) {
      console.log('Loading customers for private voucher')
      await loadSelectedCustomers()
    }
  } catch (error) {
    console.error('Lỗi khi tải voucher:', error)
    toastRef.value?.error('Lỗi', 'Không thể tải thông tin voucher')
    goBack()
  }
}

async function loadSelectedCustomers() {
  if (!voucherId.value) return
  
  try {
    console.log('Loading selected customers for voucher:', voucherId.value)
    const { data } = await api.get(`/api/khach-hang-giam-gia/voucher/${voucherId.value}`)
    console.log('Customer relations:', data)
    
    // Map relations to selectedCustomers format
    selectedCustomers.value = data.map(relation => ({
      id: relation.khachHang?.id,
      hoTen: relation.khachHang?.hoTen,
      soDienThoai: relation.khachHang?.soDienThoai,
      email: relation.khachHang?.email,
      daSuDung: relation.daSuDung || false
    })).filter(customer => customer.id) // Filter out invalid customers
    
    console.log('Loaded selected customers:', selectedCustomers.value)
  } catch (error) {
    console.error('Lỗi khi tải danh sách khách hàng:', error)
    // Don't show error toast, just log it
  }
}


// Validation functions
function clearErrors() {
  Object.keys(errors.value).forEach(key => {
    errors.value[key as keyof typeof errors.value] = ''
  })
}

function validateForm(): boolean {
  clearErrors()
  let isValid = true

  // Validate tên phiếu giảm giá
  if (!formData.value.tenPhieuGiamGia.trim()) {
    errors.value.tenPhieuGiamGia = 'Tên phiếu giảm giá không được để trống'
    isValid = false
  } else if (formData.value.tenPhieuGiamGia.trim().length < 3) {
    errors.value.tenPhieuGiamGia = 'Tên phiếu giảm giá phải có ít nhất 3 ký tự'
    isValid = false
  } else if (formData.value.tenPhieuGiamGia.trim().length > 100) {
    errors.value.tenPhieuGiamGia = 'Tên phiếu giảm giá không được vượt quá 100 ký tự'
    isValid = false
  }

  // Validate loại phiếu giảm giá
  if (!formData.value.loaiPhieuGiamGia) {
    errors.value.loaiPhieuGiamGia = 'Vui lòng chọn loại phiếu giảm giá'
    isValid = false
  }

  // Validate giá trị giảm giá
  if (!formData.value.giaTriGiamGia || formData.value.giaTriGiamGia <= 0) {
    errors.value.giaTriGiamGia = 'Giá trị giảm giá phải lớn hơn 0'
    isValid = false
  } else if (formData.value.loaiPhieuGiamGia === 'PERCENT') {
    if (formData.value.giaTriGiamGia > 100) {
      errors.value.giaTriGiamGia = 'Phần trăm giảm không được vượt quá 100%'
      isValid = false
    }
  } else if (formData.value.loaiPhieuGiamGia === 'FIXED') {
    if (formData.value.giaTriGiamGia > 10000000) {
      errors.value.giaTriGiamGia = 'Số tiền giảm không được vượt quá 10,000,000 VND'
      isValid = false
    }
  }

  // Validate số lượng sử dụng
  if (!formData.value.soLuongDung || formData.value.soLuongDung < 1) {
    errors.value.soLuongDung = 'Số lượng sử dụng phải ít nhất là 1'
    isValid = false
  } else if (formData.value.soLuongDung > 10000) {
    errors.value.soLuongDung = 'Số lượng sử dụng không được vượt quá 10,000'
    isValid = false
  }

  // Validate ngày bắt đầu
  if (!formData.value.ngayBatDau) {
    errors.value.ngayBatDau = 'Vui lòng chọn ngày bắt đầu'
    isValid = false
  } else {
    const startDate = new Date(formData.value.ngayBatDau)
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    
    if (startDate < today && !isEdit.value) {
      errors.value.ngayBatDau = 'Ngày bắt đầu không được là ngày trong quá khứ'
      isValid = false
    }
  }

  // Validate ngày kết thúc
  if (!formData.value.ngayKetThuc) {
    errors.value.ngayKetThuc = 'Vui lòng chọn ngày kết thúc'
    isValid = false
  } else if (formData.value.ngayBatDau) {
    const startDate = new Date(formData.value.ngayBatDau)
    const endDate = new Date(formData.value.ngayKetThuc)
    
    if (endDate <= startDate) {
      errors.value.ngayKetThuc = 'Ngày kết thúc phải sau ngày bắt đầu'
      isValid = false
    }
    
    // Check if the voucher duration is reasonable (max 1 year)
    const diffTime = Math.abs(endDate.getTime() - startDate.getTime())
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
    if (diffDays > 365) {
      errors.value.ngayKetThuc = 'Thời gian hiệu lực không được vượt quá 1 năm'
      isValid = false
    }
  }

  // Validate số tiền giảm tối đa (cho phần trăm)
  if (formData.value.loaiPhieuGiamGia === 'PERCENT' && formData.value.soTienGiamToiDa) {
    if (formData.value.soTienGiamToiDa < 1000) {
      errors.value.soTienGiamToiDa = 'Số tiền giảm tối đa phải ít nhất 1,000 VND'
      isValid = false
    } else if (formData.value.soTienGiamToiDa > 50000000) {
      errors.value.soTienGiamToiDa = 'Số tiền giảm tối đa không được vượt quá 50,000,000 VND'
      isValid = false
    }
  }

  // Validate hóa đơn tối thiểu
  if (formData.value.hoaDonToiThieu) {
    if (formData.value.hoaDonToiThieu < 0) {
      errors.value.hoaDonToiThieu = 'Hóa đơn tối thiểu không được âm'
      isValid = false
    } else if (formData.value.hoaDonToiThieu > 100000000) {
      errors.value.hoaDonToiThieu = 'Hóa đơn tối thiểu không được vượt quá 100,000,000 VND'
      isValid = false
    }
    
    // For fixed amount vouchers, minimum order should be higher than discount
    if (formData.value.loaiPhieuGiamGia === 'FIXED' && formData.value.giaTriGiamGia) {
      if (formData.value.hoaDonToiThieu <= formData.value.giaTriGiamGia) {
        errors.value.hoaDonToiThieu = 'Hóa đơn tối thiểu phải lớn hơn số tiền giảm'
        isValid = false
      }
    }
  }

  // Validate khách hàng cho voucher riêng tư
  if (formData.value.riengTu) {
    if (selectedCustomers.value.length === 0) {
      errors.value.selectedCustomers = 'Voucher riêng tư phải chọn ít nhất 1 khách hàng'
      isValid = false
    }
    // Không giới hạn số lượng khách hàng vì voucher riêng tư chỉ có thể dùng 1 lần
  }

  return isValid
}

async function handleSubmit() {
  if (!validateForm()) {
    toastRef.value?.error('Lỗi', 'Vui lòng kiểm tra lại thông tin đã nhập')
    return
  }

  loading.value = true
  
  try {
    const submitData = {
      ...formData.value,
      trangThai: formData.value.trangThai ? 1 : 0,
      riengTu: formData.value.riengTu || false,
      selectedCustomers: formData.value.riengTu ? selectedCustomers.value.map(c => c.id) : []
    }

    console.log('Submitting voucher data:', submitData)

    if (isEdit.value && voucherId.value) {
      console.log('Updating voucher with ID:', voucherId.value)
      const response = await api.put(`/api/phieu-giam-gia/${voucherId.value}`, submitData)
      console.log('Update response:', response)
      toastRef.value?.success('Thành công', 'Cập nhật phiếu giảm giá thành công!')
    } else {
      console.log('Creating new voucher')
      const response = await api.post('/api/phieu-giam-gia', submitData)
      console.log('Create response:', response)
      toastRef.value?.success('Thành công', 'Thêm phiếu giảm giá thành công!')
    }
    
    setTimeout(() => {
      goBack()
    }, 1500)
  } catch (error: any) {
    console.error('Lỗi khi lưu:', error)
    if (error.response?.data) {
      toastRef.value?.error('Lỗi', error.response.data)
    } else {
      toastRef.value?.error('Lỗi', 'Có lỗi xảy ra khi lưu phiếu giảm giá')
    }
    loading.value = false
  }
}

function goBack() {
  router.push('/voucher')
}

// Customer selection functions
async function searchCustomers() {
  if (searchText.value.trim().length < 2) {
    customers.value = []
    return
  }
  
  searching.value = true
  try {
    const { data } = await api.get(`/api/khach-hang/search?query=${encodeURIComponent(searchText.value)}`)
    // Lọc bỏ khách hàng đã được chọn
    customers.value = data.filter(customer => 
      !selectedCustomers.value.some(selected => selected.id === customer.id)
    )
  } catch (error) {
    console.error('Lỗi khi tìm kiếm khách hàng:', error)
    customers.value = []
  } finally {
    searching.value = false
  }
}

function selectCustomer(customer) {
  // Voucher riêng tư có thể chọn nhiều khách hàng, nhưng chỉ 1 người được dùng
  selectedCustomers.value.push(customer)
  customers.value = customers.value.filter(c => c.id !== customer.id)
  searchText.value = ''
}

function removeCustomer(customerId) {
  selectedCustomers.value = selectedCustomers.value.filter(c => c.id !== customerId)
}

function openCustomerModal() {
  showCustomerModal.value = true
  searchText.value = ''
  customers.value = []
}

function closeCustomerModal() {
  showCustomerModal.value = false
  searchText.value = ''
  customers.value = []
}

</script>

<style scoped>
.form-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #e9ecef;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.btn-back {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #6c757d;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.btn-back:hover {
  background: #545b62;
}

.icon-arrow-left::before {
  content: "←";
}

.form-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.voucher-form {
  padding: 30px;
}

.form-section {
  margin-bottom: 40px;
  padding-bottom: 30px;
  border-bottom: 1px solid #e9ecef;
}

.form-section:last-of-type {
  border-bottom: none;
  margin-bottom: 0;
}

.form-section h2 {
  color: #495057;
  margin-bottom: 20px;
  font-size: 18px;
  font-weight: 600;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-weight: 600;
  color: #495057;
  font-size: 14px;
}

.required {
  color: #dc3545;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 12px;
  border: 2px solid #e9ecef;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
}

.form-group input.error,
.form-group select.error,
.form-group textarea.error {
  border-color: #dc3545;
  box-shadow: 0 0 0 3px rgba(220, 53, 69, 0.1);
}

.error-message {
  color: #dc3545;
  font-size: 12px;
  margin-top: 4px;
  display: block;
}

.checkbox-label {
  display: flex !important;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  font-weight: normal !important;
}

.checkbox-label input[type="checkbox"] {
  width: auto;
  margin: 0;
}

.checkmark {
  font-size: 14px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 40px;
  padding-top: 30px;
  border-top: 2px solid #e9ecef;
}

.btn-cancel {
  padding: 12px 24px;
  background: #6c757d;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: background-color 0.3s;
}

.btn-cancel:hover {
  background: #545b62;
}

.btn-submit {
  padding: 12px 24px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: background-color 0.3s;
}

.btn-submit:hover:not(:disabled) {
  background: #0056b3;
}

.btn-submit:disabled {
  background: #6c757d;
  cursor: not-allowed;
}

/* Customer Selection Styles */
.customer-selection-section {
  margin-top: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.customer-selection-section h3 {
  margin: 0 0 15px 0;
  color: #495057;
  font-size: 16px;
}

.customer-selection-info p {
  margin: 0 0 15px 0;
  color: #6c757d;
  font-size: 14px;
}

.btn-select-customer {
  padding: 10px 16px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  margin-bottom: 15px;
}

.btn-select-customer:hover {
  background: #0056b3;
}

.selected-customers {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.customer-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: white;
  border: 1px solid #dee2e6;
  border-radius: 6px;
}

.customer-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.customer-name {
  font-weight: 600;
  color: #495057;
}

.customer-phone {
  font-size: 12px;
  color: #6c757d;
}

.btn-remove-customer {
  background: #dc3545;
  color: white;
  border: none;
  border-radius: 4px;
  width: 24px;
  height: 24px;
  cursor: pointer;
  font-size: 16px;
}

.btn-remove-customer:hover {
  background: #c82333;
}

/* Modal Styles */
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
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  max-height: 70vh;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e9ecef;
}

.modal-header h3 {
  margin: 0;
  color: #495057;
}

.btn-close-modal {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #6c757d;
  width: 30px;
  height: 30px;
}

.modal-body {
  padding: 20px;
  flex: 1;
  overflow-y: auto;
}

.search-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ced4da;
  border-radius: 6px;
  font-size: 14px;
  margin-bottom: 20px;
}

.search-input:focus {
  outline: none;
  border-color: #007bff;
}

.loading, .no-results, .search-hint {
  text-align: center;
  padding: 20px;
  color: #6c757d;
}

.customer-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.customer-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  cursor: pointer;
}

.customer-option:hover {
  background: #f8f9fa;
}

.btn-select {
  background: #28a745;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 6px 12px;
  font-size: 12px;
  cursor: pointer;
}

.btn-select:hover {
  background: #218838;
}

.modal-footer {
  padding: 20px;
  border-top: 1px solid #e9ecef;
  display: flex;
  justify-content: flex-end;
}






@media (max-width: 768px) {
  .form-page {
    padding: 15px;
  }
  
  .form-row {
    grid-template-columns: 1fr;
    gap: 15px;
  }
  
  .header-left {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .voucher-form {
    padding: 20px;
  }
  
  .form-actions {
    flex-direction: column;
  }
}
</style>
