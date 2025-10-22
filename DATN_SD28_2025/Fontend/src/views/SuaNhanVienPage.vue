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
          <h1>Sửa nhân viên</h1>
        </div>
      </div>

      <Toast ref="toastRef" />

      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>Đang tải thông tin nhân viên...</p>
      </div>

      <div v-else-if="!nhanVien" class="error-container">
        <p>Không tìm thấy thông tin nhân viên</p>
        <button class="btn-primary" @click="goBack">Quay lại</button>
      </div>

      <div v-else class="form-container">
        <!-- Combined Section: Image Upload -->
        <div class="combined-section">
          <!-- Ảnh Nhân Viên -->
          <div class="image-upload-area">
            <div class="image-preview" v-if="selectedImage">
              <img
                :src="selectedImage"
                alt="Ảnh nhân viên"
                class="preview-image"
                @error="handleImageError"
                @load="handleImageLoad"
              />
              <button type="button" class="btn-remove-image" @click="removeImage">×</button>
            </div>
            <div class="image-upload-placeholder" v-else>
              <i class="fas fa-user-circle"></i>
              <p>Chưa có ảnh</p>
            </div>

            <div class="image-upload-controls">
              <input
                type="file"
                id="employeeImage"
                ref="imageInput"
                @change="handleImageUpload"
                accept="image/*"
                class="image-input"
              />
              <label for="employeeImage" class="btn-upload-image">
                <i class="fas fa-upload"></i>
                Tải ảnh lên
              </label>
              <div class="image-upload-info">
                <small>Định dạng: JPG, PNG, GIF (Tối đa 5MB)</small>
              </div>
            </div>

            <div v-if="errors.anhDaiDien" class="error-message">
              {{ errors.anhDaiDien }}
            </div>
          </div>
        </div>

        <form @submit.prevent="updateNhanVien" class="staff-form" enctype="multipart/form-data">
          <div class="form-grid">
            <!-- Mã Nhân Viên (Read-only) -->
            <div class="form-group">
              <label for="maNhanVien" class="form-label"> Mã Nhân Viên </label>
              <input
                id="maNhanVien"
                v-model="formData.maNhanVien"
                type="text"
                class="form-input"
                readonly
                disabled
              />
              <div class="form-help">Mã nhân viên không thể thay đổi</div>
            </div>

            <!-- Họ Tên -->
            <div class="form-group">
              <label for="hoTen" class="form-label"> Họ Tên <span class="required">*</span> </label>
              <input
                id="hoTen"
                v-model="formData.hoTen"
                type="text"
                class="form-input"
                placeholder="Nhập họ và tên đầy đủ"
                maxlength="255"
                @blur="validateHoTen"
                @input="validateHoTen"
                required
              />
              <div v-if="errors.hoTen" class="error-message">
                {{ errors.hoTen }}
              </div>
            </div>

            <!-- Số Điện Thoại -->
            <div class="form-group">
              <label for="soDienThoai" class="form-label">
                Số Điện Thoại <span class="required">*</span>
              </label>
              <input
                id="soDienThoai"
                v-model="formData.soDienThoai"
                type="text"
                class="form-input"
                placeholder="Nhập số điện thoại (10-11 chữ số)"
                pattern="^[0-9]{10,11}$"
                @blur="validateSoDienThoai"
                @input="validateSoDienThoai"
                required
              />
              <div v-if="errors.soDienThoai" class="error-message">
                {{ errors.soDienThoai }}
              </div>
            </div>

            <!-- Ngày Sinh -->
            <div class="form-group">
              <label for="ngaySinh" class="form-label">
                Ngày Sinh <span class="required">*</span>
              </label>
              <input
                id="ngaySinh"
                v-model="formData.ngaySinh"
                type="date"
                class="form-input"
                @blur="validateNgaySinh"
                @change="validateNgaySinh"
                required
              />
              <div v-if="errors.ngaySinh" class="error-message">
                {{ errors.ngaySinh }}
              </div>
              <div v-if="formData.ngaySinh && !errors.ngaySinh" class="age-display">
                Tuổi hiện tại: {{ calculateAge(formData.ngaySinh) }} tuổi
              </div>
            </div>

            <!-- Giới Tính -->
            <div class="form-group">
              <label for="gioiTinh" class="form-label">
                Giới Tính <span class="required">*</span>
              </label>
              <select 
                id="gioiTinh" 
                v-model="formData.gioiTinh" 
                class="form-select" 
                @change="validateGioiTinh"
                required
              >
                <option value="">Chọn giới tính</option>
                <option value="Nam">Nam</option>
                <option value="Nữ">Nữ</option>
              </select>
              <div v-if="errors.gioiTinh" class="error-message">
                {{ errors.gioiTinh }}
              </div>
            </div>

            <!-- Chức Vụ -->
            <div class="form-group">
              <label for="chucVu" class="form-label">
                Chức Vụ <span class="required">*</span>
              </label>
              <select 
                id="chucVu" 
                v-model="formData.chucVu" 
                class="form-select" 
                @change="validateChucVu"
                required
              >
                <option value="">Chọn chức vụ</option>
                <option value="Quản lý">Quản lý</option>
                <option value="Nhân viên">Nhân viên</option>
              </select>
              <div v-if="errors.chucVu" class="error-message">
                {{ errors.chucVu }}
              </div>
            </div>

            <!-- Email -->
            <div class="form-group">
              <label for="email" class="form-label"> Email <span class="required">*</span> </label>
              <input
                id="email"
                v-model="formData.email"
                type="email"
                class="form-input"
                placeholder="Nhập email hợp lệ"
                maxlength="255"
                @blur="validateEmail"
                @input="validateEmail"
                required
              />
              <div v-if="errors.email" class="error-message">
                {{ errors.email }}
              </div>
            </div>
          </div>

          <!-- Địa chỉ chi tiết -->
          <div class="form-group full-width">
            <label class="form-label">
              <font-awesome-icon icon="map-marker-alt" />
              <span>Địa chỉ chi tiết</span>
            </label>
            <textarea
              v-model="formData.diaChiChiTiet"
              class="form-textarea"
              rows="2"
              placeholder="Số nhà, tên đường, tòa nhà..."
              maxlength="500"
            ></textarea>
            <div v-if="errors.diaChiChiTiet" class="error-message">
              {{ errors.diaChiChiTiet }}
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">
              <font-awesome-icon icon="map-marker-alt" />
              <span>Tỉnh/Thành phố *</span>
            </label>
            <select 
              v-model="formData.tinhThanhPho" 
              class="form-select"
              @change="onProvinceChange"
              :disabled="loadingProvinces"
              required
            >
              <option value="">Chọn tỉnh/thành phố</option>
              <option 
                v-for="tinh in tinhThanhPhoList" 
                :key="tinh.code" 
                :value="tinh.code"
              >
                {{ tinh.name }}
              </option>
            </select>
            <div v-if="errors.tinhThanhPho" class="error-message">
              {{ errors.tinhThanhPho }}
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">
              <font-awesome-icon icon="map-marker-alt" />
              <span>Phường/Xã *</span>
            </label>
            <select 
              v-model="formData.phuongXa" 
              class="form-select"
              :disabled="!formData.tinhThanhPho || loadingWards"
              required
            >
              <option value="">Chọn phường/xã</option>
              <option 
                v-for="phuong in phuongXaList" 
                :key="phuong.code" 
                :value="phuong.code"
              >
                {{ phuong.name }}
              </option>
            </select>
            <div v-if="errors.phuongXa" class="error-message">
              {{ errors.phuongXa }}
            </div>
          </div>

          <!-- Form Actions -->
          <div class="form-actions">
            <button type="button" class="btn-secondary" @click="goBack">Hủy</button>
            <button type="submit" class="btn-primary" :disabled="saving">
              <span v-if="saving" class="loading-spinner"></span>
              {{ saving ? 'Đang lưu...' : 'Cập Nhật Nhân Viên' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute, onBeforeRouteLeave } from 'vue-router'
import Toast from '@/components/Toast.vue'
import PosHeader from '@/components/PosHeader.vue'
import api from '@/services/api'
import { maNhanVienService } from '@/services/maNhanVienService'

const router = useRouter()
const route = useRoute()
const toastRef = ref()
const loading = ref(true)
const saving = ref(false)
const nhanVien = ref(null)
const hasUnsavedChanges = ref(false)

// Administrative division data
const tinhThanhPhoList = ref([])
const phuongXaList = ref([])
const loadingProvinces = ref(false)
const loadingWards = ref(false)
const wardsCache = ref({})

// Image upload variables
const selectedImage = ref<string | null>(null)
const selectedFile = ref<File | null>(null)
const imageInput = ref<HTMLInputElement>()

// Form data
const formData = reactive({
  id: null,
  maNhanVien: '',
  hoTen: '',
  soDienThoai: '',
  ngaySinh: '',
  gioiTinh: '',
  diaChiChiTiet: '',
  tinhThanhPho: '',
  phuongXa: '',
  email: '',
  chucVu: '',
  anhDaiDien: null as File | string | null,
})

// Error handling
const errors = reactive({
  hoTen: '',
  soDienThoai: '',
  ngaySinh: '',
  gioiTinh: '',
  diaChiChiTiet: '',
  tinhThanhPho: '',
  phuongXa: '',
  email: '',
  chucVu: '',
  anhDaiDien: '',
})

// Clear errors
function clearErrors() {
  Object.keys(errors).forEach((key) => {
    errors[key] = ''
  })
}

// Validation functions
function validateForm() {
  clearErrors()
  let isValid = true

  // Validate Họ tên
  if (!formData.hoTen || formData.hoTen.trim().length < 2) {
    errors.hoTen = 'Họ tên phải có ít nhất 2 ký tự'
    isValid = false
  } else if (formData.hoTen.trim().length > 255) {
    errors.hoTen = 'Họ tên không được quá 255 ký tự'
    isValid = false
  }

  // Validate Số điện thoại
  if (!formData.soDienThoai) {
    errors.soDienThoai = 'Số điện thoại không được để trống'
    isValid = false
  } else if (!/^[0-9]{10,11}$/.test(formData.soDienThoai)) {
    errors.soDienThoai = 'Số điện thoại phải có 10-11 chữ số'
    isValid = false
  }

  // Validate Ngày sinh và tuổi
  if (!formData.ngaySinh) {
    errors.ngaySinh = 'Ngày sinh không được để trống'
    isValid = false
  } else {
    const birthDate = new Date(formData.ngaySinh)
    const today = new Date()
    const age = today.getFullYear() - birthDate.getFullYear()
    const monthDiff = today.getMonth() - birthDate.getMonth()
    
    // Adjust age if birthday hasn't occurred this year
    const actualAge = monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate()) 
      ? age - 1 
      : age

    if (actualAge < 18) {
      errors.ngaySinh = 'Nhân viên phải đủ 18 tuổi trở lên'
      isValid = false
    } else if (actualAge > 65) {
      errors.ngaySinh = 'Tuổi nhân viên không được vượt quá 65 tuổi'
      isValid = false
    } else if (birthDate > today) {
      errors.ngaySinh = 'Ngày sinh không được là ngày trong tương lai'
      isValid = false
    }
  }

  // Validate Giới tính
  if (!formData.gioiTinh) {
    errors.gioiTinh = 'Vui lòng chọn giới tính'
    isValid = false
  } else if (!['Nam', 'Nữ'].includes(formData.gioiTinh)) {
    errors.gioiTinh = 'Giới tính không hợp lệ'
    isValid = false
  }

  // Validate Chức vụ
  if (!formData.chucVu) {
    errors.chucVu = 'Vui lòng chọn chức vụ'
    isValid = false
  } else if (!['Quản lý', 'Nhân viên'].includes(formData.chucVu)) {
    errors.chucVu = 'Chức vụ không hợp lệ'
    isValid = false
  }

  // Validate Email
  if (!formData.email) {
    errors.email = 'Email không được để trống'
    isValid = false
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
    errors.email = 'Email không đúng định dạng'
    isValid = false
  } else if (formData.email.length > 255) {
    errors.email = 'Email không được quá 255 ký tự'
    isValid = false
  }

  // Validate Địa chỉ chi tiết
  if (formData.diaChiChiTiet && formData.diaChiChiTiet.length > 500) {
    errors.diaChiChiTiet = 'Địa chỉ chi tiết không được quá 500 ký tự'
    isValid = false
  }

  // Validate Tỉnh/Thành phố
  if (!formData.tinhThanhPho) {
    errors.tinhThanhPho = 'Vui lòng chọn tỉnh/thành phố'
    isValid = false
  }

  // Validate Phường/Xã
  if (!formData.phuongXa) {
    errors.phuongXa = 'Vui lòng chọn phường/xã'
    isValid = false
  }

  // Validate Ảnh đại diện (nếu có)
  if (formData.anhDaiDien && typeof formData.anhDaiDien === 'string') {
    // Check if it's a valid URL or path
    const isValidUrl = formData.anhDaiDien.startsWith('/uploads/') || 
                      formData.anhDaiDien.startsWith('http') ||
                      formData.anhDaiDien.startsWith('data:')
    if (!isValidUrl) {
      errors.anhDaiDien = 'Đường dẫn ảnh không hợp lệ'
      isValid = false
    }
  }

  return isValid
}

// Real-time validation functions
function validateHoTen() {
  if (!formData.hoTen || formData.hoTen.trim().length < 2) {
    errors.hoTen = 'Họ tên phải có ít nhất 2 ký tự'
    return false
  } else if (formData.hoTen.trim().length > 255) {
    errors.hoTen = 'Họ tên không được quá 255 ký tự'
    return false
  } else {
    errors.hoTen = ''
    return true
  }
}

function validateSoDienThoai() {
  if (!formData.soDienThoai) {
    errors.soDienThoai = 'Số điện thoại không được để trống'
    return false
  } else if (!/^[0-9]{10,11}$/.test(formData.soDienThoai)) {
    errors.soDienThoai = 'Số điện thoại phải có 10-11 chữ số'
    return false
  } else {
    errors.soDienThoai = ''
    return true
  }
}

function validateNgaySinh() {
  if (!formData.ngaySinh) {
    errors.ngaySinh = 'Ngày sinh không được để trống'
    return false
  }
  
  const birthDate = new Date(formData.ngaySinh)
  const today = new Date()
  const age = today.getFullYear() - birthDate.getFullYear()
  const monthDiff = today.getMonth() - birthDate.getMonth()
  
  // Adjust age if birthday hasn't occurred this year
  const actualAge = monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate()) 
    ? age - 1 
    : age

  if (actualAge < 18) {
    errors.ngaySinh = 'Nhân viên phải đủ 18 tuổi trở lên'
    return false
  } else if (actualAge > 65) {
    errors.ngaySinh = 'Tuổi nhân viên không được vượt quá 65 tuổi'
    return false
  } else if (birthDate > today) {
    errors.ngaySinh = 'Ngày sinh không được là ngày trong tương lai'
    return false
  } else {
    errors.ngaySinh = ''
    return true
  }
}

function validateEmail() {
  if (!formData.email) {
    errors.email = 'Email không được để trống'
    return false
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
    errors.email = 'Email không đúng định dạng'
    return false
  } else if (formData.email.length > 255) {
    errors.email = 'Email không được quá 255 ký tự'
    return false
  } else {
    errors.email = ''
    return true
  }
}

// Calculate age from birth date
function calculateAge(birthDate: string) {
  if (!birthDate) return 0
  
  const birth = new Date(birthDate)
  const today = new Date()
  const age = today.getFullYear() - birth.getFullYear()
  const monthDiff = today.getMonth() - birth.getMonth()
  
  // Adjust age if birthday hasn't occurred this year
  return monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate()) 
    ? age - 1 
    : age
}

function validateGioiTinh() {
  if (!formData.gioiTinh) {
    errors.gioiTinh = 'Vui lòng chọn giới tính'
    return false
  } else if (!['Nam', 'Nữ'].includes(formData.gioiTinh)) {
    errors.gioiTinh = 'Giới tính không hợp lệ'
    return false
  } else {
    errors.gioiTinh = ''
    return true
  }
}

function validateChucVu() {
  if (!formData.chucVu) {
    errors.chucVu = 'Vui lòng chọn chức vụ'
    return false
  } else if (!['Quản lý', 'Nhân viên'].includes(formData.chucVu)) {
    errors.chucVu = 'Chức vụ không hợp lệ'
    return false
  } else {
    errors.chucVu = ''
    return true
  }
}

// Go back to staff list
function goBack() {
  router.push('/nhan-vien')
}

// Administrative division functions
async function loadProvinces() {
  try {
    loadingProvinces.value = true
    const response = await api.get('/api/vietnam-administrative/provinces')
    tinhThanhPhoList.value = response.data || []
  } catch (err: any) {
    console.error('Lỗi khi tải danh sách tỉnh/thành phố:', err)
    // Fallback data
    tinhThanhPhoList.value = [
      { code: '01', name: 'Hà Nội', type: 'Thành phố Trung ương' },
      { code: '79', name: 'TP. Hồ Chí Minh', type: 'Thành phố Trung ương' },
      { code: '31', name: 'Hải Phòng', type: 'Thành phố Trung ương' },
      { code: '48', name: 'Đà Nẵng', type: 'Thành phố Trung ương' },
      { code: '92', name: 'Cần Thơ', type: 'Thành phố Trung ương' }
    ]
    toastRef.value?.warning('Cảnh báo', 'Sử dụng dữ liệu dự phòng cho tỉnh/thành phố')
  } finally {
    loadingProvinces.value = false
  }
}

async function loadWards(provinceCode: string) {
  try {
    loadingWards.value = true
    
    // Check cache first
    if (wardsCache.value[provinceCode]) {
      phuongXaList.value = wardsCache.value[provinceCode]
      return
    }
    
    const response = await api.get(`/api/vietnam-administrative/wards/${provinceCode}`)
    const wards = response.data || []
    
    // Cache the result
    wardsCache.value[provinceCode] = wards
    phuongXaList.value = wards
  } catch (err: any) {
    console.error('Lỗi khi tải danh sách phường/xã:', err)
    // Fallback data
    const fallbackWards = [
      { code: '00001', name: 'Phường Phúc Xá', type: 'Phường', parentCode: provinceCode },
      { code: '00002', name: 'Phường Trúc Bạch', type: 'Phường', parentCode: provinceCode },
      { code: '00003', name: 'Phường Vĩnh Phú', type: 'Phường', parentCode: provinceCode },
      { code: '00004', name: 'Phường Cống Vị', type: 'Phường', parentCode: provinceCode },
      { code: '00005', name: 'Phường Liễu Giai', type: 'Phường', parentCode: provinceCode }
    ]
    wardsCache.value[provinceCode] = [...fallbackWards]
    phuongXaList.value = [...fallbackWards]
    toastRef.value?.warning('Cảnh báo', 'Sử dụng dữ liệu dự phòng cho phường/xã')
  } finally {
    loadingWards.value = false
  }
}

async function onProvinceChange() {
  // Reset phường/xã when province changes
  formData.phuongXa = ''
  phuongXaList.value = []
  
  if (formData.tinhThanhPho) {
    await loadWards(formData.tinhThanhPho)
  }
}

// Parse existing address into separate fields
function parseAddress(address: string) {
  if (!address) return
  
  // Try to find province and ward in the address
  const addressParts = address.split(',').map(part => part.trim())
  
  // Find province
  for (const province of tinhThanhPhoList.value) {
    if (address.includes(province.name)) {
      formData.tinhThanhPho = province.code
      break
    }
  }
  
  // Find ward (only if province is found)
  if (formData.tinhThanhPho && phuongXaList.value.length > 0) {
    for (const ward of phuongXaList.value) {
      if (address.includes(ward.name)) {
        formData.phuongXa = ward.code
        break
      }
    }
  }
  
  // The remaining parts are detailed address
  const detailedParts = addressParts.filter(part => {
    const isProvince = tinhThanhPhoList.value.some(p => p.name === part)
    const isWard = phuongXaList.value.some(w => w.name === part)
    return !isProvince && !isWard
  })
  
  formData.diaChiChiTiet = detailedParts.join(', ')
}

// Image upload functions
async function handleImageUpload(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]

  if (!file) return

  // Validate file type
  if (!file.type.startsWith('image/')) {
    errors.anhDaiDien = 'Vui lòng chọn file ảnh hợp lệ'
    toastRef.value?.error('Lỗi', 'Vui lòng chọn file ảnh hợp lệ')
    return
  }

  // Validate file size (5MB)
  if (file.size > 5 * 1024 * 1024) {
    errors.anhDaiDien = 'Kích thước file không được vượt quá 5MB'
    toastRef.value?.error('Lỗi', 'Kích thước file không được vượt quá 5MB')
    return
  }

  // Clear previous errors
  errors.anhDaiDien = ''

  try {
    // Upload image immediately like product images
    const formDataUpload = new FormData()
    formDataUpload.append('file', file)

    const response = await api.post('/api/upload/avatar', formDataUpload, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })

    if (response.data.url) {
      // Store the uploaded image URL instead of file
      selectedImage.value = getImageUrl(response.data.url) // Use getImageUrl to create full URL
      selectedFile.value = file
      formData.anhDaiDien = response.data.url // Store URL instead of file

      console.log('Image uploaded successfully:', response.data.url)
      toastRef.value?.success('Thành công', 'Upload ảnh thành công!')
    } else {
      throw new Error('No URL returned from server')
    }
  } catch (error) {
    console.error('Error uploading image:', error)

    if (error.response?.status === 413) {
      toastRef.value?.error('Lỗi', 'File quá lớn! Vui lòng chọn file nhỏ hơn 5MB.')
    } else {
      toastRef.value?.error(
        'Lỗi',
        'Lỗi upload ảnh: ' + (error.response?.data?.error || error.message),
      )
    }
  }

  // Clear input để có thể chọn lại file cùng tên
  target.value = ''
}

async function removeImage() {
  // If there's an uploaded image, try to delete it from server
  if (
    formData.anhDaiDien &&
    typeof formData.anhDaiDien === 'string' &&
    formData.anhDaiDien.startsWith('/uploads/avatar/')
  ) {
    try {
      const filename = formData.anhDaiDien.split('/').pop()
      if (filename) {
        await api.delete(`/api/upload/avatar/${filename}`)
        console.log('Image deleted from server:', filename)
      }
    } catch (error) {
      console.error('Error deleting image from server:', error)
      // Don't show error to user as the image is already removed from UI
    }
  }

  // Clear all image data
  selectedImage.value = null
  selectedFile.value = null
  formData.anhDaiDien = null
  errors.anhDaiDien = ''

  // Reset file input
  if (imageInput.value) {
    imageInput.value.value = ''
  }
}

// Handle image error
function handleImageError(event: Event) {
  console.error('Image load error:', event)
  console.error('Failed to load image:', selectedImage.value)

  // Show error message
  toastRef.value?.error('Lỗi', 'Không thể tải ảnh. Vui lòng chọn ảnh khác.')

  // Remove the failed image
  removeImage()
}

// Handle image load success
function handleImageLoad(event: Event) {
  console.log('Image loaded successfully:', event)
}

// Get image URL - use static resource serving (like product images)
function getImageUrl(imagePath: string) {
  if (!imagePath) return ''

  // If it's already a full URL, return as is
  if (imagePath.startsWith('http')) {
    return imagePath
  }

  const baseUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'

  // If path already starts with /uploads/, use it directly
  if (imagePath.startsWith('/uploads/')) {
    return `${baseUrl}${imagePath}`
  }

  // If path doesn't start with /uploads/, add it
  return `${baseUrl}/uploads/${imagePath}`
}

// Load staff data
async function loadNhanVien() {
  const id = route.params.id
  if (!id) {
    toastRef.value?.error('Lỗi', 'Không tìm thấy ID nhân viên')
    goBack()
    return
  }

  try {
    const response = await api.get(`/api/nhan-vien/${id}`)
    nhanVien.value = response.data

    // Populate form data
    Object.keys(formData).forEach((key) => {
      if (response.data.hasOwnProperty(key)) {
        formData[key] = response.data[key]
      }
    })

    // Convert date format for input
    if (formData.ngaySinh) {
      formData.ngaySinh = formData.ngaySinh.split('T')[0]
    }

    // Load current image if exists
    if (response.data.anhDaiDien) {
      selectedImage.value = getImageUrl(response.data.anhDaiDien)
    }

    // Parse existing address and load wards if province is selected
    if (formData.diaChi) {
      parseAddress(formData.diaChi)
    }
    
    if (formData.tinhThanhPho) {
      await loadWards(formData.tinhThanhPho)
    }
  } catch (error) {
    console.error('Error loading nhan vien:', error)
    toastRef.value?.error('Lỗi', 'Không thể tải thông tin nhân viên')
    goBack()
  } finally {
    loading.value = false
  }
}

// Update staff
async function updateNhanVien() {
  // Validate form first
  if (!validateForm()) {
    toastRef.value?.error('Lỗi', 'Vui lòng kiểm tra lại thông tin đã nhập')
    return
  }

  // Confirm before updating
  const confirmed = confirm('Bạn có chắc chắn muốn cập nhật thông tin nhân viên này?')
  if (!confirmed) return

  saving.value = true

  try {
    // Image is already uploaded, just use the URL
    let imagePath = null
    if (formData.anhDaiDien) {
      if (typeof formData.anhDaiDien === 'string') {
        // It's already a URL from previous upload
        imagePath = formData.anhDaiDien
        console.log('Using existing image URL:', imagePath)
      } else {
        // Fallback: if it's still a file, upload it (shouldn't happen with new flow)
        console.warn('Unexpected file type, uploading...')
        try {
          const formDataUpload = new FormData()
          formDataUpload.append('file', formData.anhDaiDien)

          const uploadResponse = await api.post('/api/upload/avatar', formDataUpload, {
            headers: {
              'Content-Type': 'multipart/form-data',
            },
          })

          imagePath = uploadResponse.data.filename
          console.log('Fallback image upload successful:', imagePath)
        } catch (uploadError) {
          console.error('Error uploading image:', uploadError)
          toastRef.value?.error('Lỗi', 'Không thể upload ảnh đại diện')
          return
        }
      }
    }

    // Prepare JSON data
    const { anhDaiDien, diaChiChiTiet, tinhThanhPho, phuongXa, ...dataWithoutImage } = formData
    
    // Combine address fields into single diaChi field for backend
    let fullAddress = ''
    if (diaChiChiTiet) {
      fullAddress += diaChiChiTiet.trim()
    }
    if (tinhThanhPho) {
      const provinceName = tinhThanhPhoList.value.find(p => p.code === tinhThanhPho)?.name || tinhThanhPho
      if (fullAddress) fullAddress += ', '
      fullAddress += provinceName
    }
    if (phuongXa) {
      const wardName = phuongXaList.value.find(w => w.code === phuongXa)?.name || phuongXa
      if (fullAddress) fullAddress += ', '
      fullAddress += wardName
    }

    // Auto-generate taiKhoan if not exists
    const taiKhoan = formData.email || `nv${Date.now()}`

    // Keep existing password or set default
    const matKhau = formData.matKhau || '123456'

    const requestData = {
      ...dataWithoutImage,
      diaChi: fullAddress, // Use combined address
      anhDaiDien: imagePath,
      taiKhoan: taiKhoan,
      matKhau: matKhau,
      trangThai: formData.trangThai || 1, // Keep existing status or default to active
    }

    console.log('Sending update data:', requestData)
    console.log('Update data keys:', Object.keys(requestData))
    console.log('Update data JSON:', JSON.stringify(requestData, null, 2))

    const response = await api.put(`/api/nhan-vien/${formData.id}`, requestData, {
      headers: {
        'Content-Type': 'application/json',
      },
    })

    console.log('Update response:', response.data)
    toastRef.value?.success('Thành công', 'Cập nhật nhân viên thành công!')

    // Reset unsaved changes flag
    hasUnsavedChanges.value = false

    // Go back after 1 second
    setTimeout(() => {
      goBack()
    }, 1000)
  } catch (error) {
    console.error('Error updating nhan vien:', error)
    console.error('Error response:', error.response?.data)

    if (error.response?.status === 400) {
      const errorData = error.response.data

      // Handle validation errors
      if (errorData.errors) {
        Object.keys(errorData.errors).forEach((field) => {
          if (errors.hasOwnProperty(field)) {
            errors[field] = errorData.errors[field]
          }
        })
        toastRef.value?.error('Lỗi', 'Vui lòng kiểm tra lại thông tin')
      } else {
        toastRef.value?.error('Lỗi', errorData?.error || 'Không thể cập nhật nhân viên')
      }
    } else {
      toastRef.value?.error('Lỗi', 'Không thể cập nhật nhân viên')
    }
  } finally {
    saving.value = false
  }
}

// Warning when leaving page with unsaved changes
onBeforeRouteLeave((to, from, next) => {
  if (hasUnsavedChanges.value) {
    const confirmed = confirm('Dữ liệu chưa được cập nhật. Bạn có chắc chắn rời khỏi trang này?')
    if (confirmed) {
      next()
    } else {
      next(false)
    }
  } else {
    next()
  }
})

// Warning when closing browser/tab
onUnmounted(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload)
})

function handleBeforeUnload(event: BeforeUnloadEvent) {
  if (hasUnsavedChanges.value) {
    event.preventDefault()
    event.returnValue = 'Dữ liệu chưa được cập nhật. Bạn có chắc chắn rời khỏi trang này?'
    return 'Dữ liệu chưa được cập nhật. Bạn có chắc chắn rời khỏi trang này?'
  }
}

// Watch for form changes
watch(
  formData,
  () => {
    hasUnsavedChanges.value = true
  },
  { deep: true },
)

// Load data on mount
onMounted(async () => {
  // Load provinces first
  await loadProvinces()
  // Then load nhan vien data
  await loadNhanVien()
  window.addEventListener('beforeunload', handleBeforeUnload)
})
</script>

<style scoped>
.page {
  padding: 20px;
  padding-top: 80px;
}

.content {
  margin-top: 30px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
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
  padding: 8px 16px;
  background: #6c757d;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
  font-size: 14px;
}

.btn-back:hover {
  background: #5a6268;
}

.back-icon {
  font-size: 16px;
  font-weight: bold;
}

h1 {
  margin: 0;
  color: #333;
  font-size: 28px;
  font-weight: 600;
}

.loading-container,
.error-container {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #007bff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.form-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 30px;
  width: 100%;
  margin: 0 auto;
}

.staff-form {
  width: 100%;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-label {
  font-weight: 600;
  margin-bottom: 8px;
  color: #333;
  font-size: 14px;
}

.required {
  color: #dc3545;
}

.form-input,
.form-select,
.form-textarea {
  padding: 12px 16px;
  border: 2px solid #e1e5e9;
  border-radius: 6px;
  font-size: 14px;
  transition:
    border-color 0.3s,
    box-shadow 0.3s;
  background: white;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
}

.form-input:disabled {
  background-color: #f8f9fa;
  color: #6c757d;
  cursor: not-allowed;
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.error-message {
  color: #dc3545;
  font-size: 12px;
  margin-top: 4px;
  font-weight: 500;
}

.age-display {
  color: #27ae60;
  font-size: 12px;
  margin-top: 4px;
  font-weight: 500;
}

.form-help {
  color: #6c757d;
  font-size: 12px;
  margin-top: 4px;
  font-style: italic;
}

.form-actions {
  display: flex;
  gap: 15px;
  justify-content: flex-end;
  padding-top: 20px;
  border-top: 1px solid #e1e5e9;
}

.btn-primary,
.btn-secondary {
  padding: 12px 24px;
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
  background: #007bff;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #0056b3;
}

.btn-primary:disabled {
  background: #6c757d;
  cursor: not-allowed;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #5a6268;
}

/* Responsive */
@media (max-width: 1200px) {
  .form-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 900px) {
  .form-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .header-left {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .form-actions {
    flex-direction: column;
  }

  .btn-primary,
  .btn-secondary {
    width: 100%;
    justify-content: center;
  }
}

/* Combined Section Layout */
.combined-section {
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 12px;
  border: 2px dashed #dee2e6;
}

/* Image Upload Styles */
.image-upload-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  gap: 12px;
}

.image-preview {
  position: relative;
  width: 140px;
  height: 140px;
  border-radius: 12px;
  overflow: hidden;
  border: 3px solid #f97316;
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.2);
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.btn-remove-image {
  position: absolute;
  top: -4px;
  right: -4px;
  width: 24px;
  height: 24px;
  background: transparent;
  color: #333;
  border: none;
  border-radius: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 18px;
  font-weight: bold;
  line-height: 1;
  transition: all 0.3s ease;
  box-shadow: none;
}

.btn-remove-image:hover {
  background: transparent;
  color: #000;
  transform: scale(1.2);
  box-shadow: none;
}

.image-upload-placeholder {
  width: 140px;
  height: 140px;
  border: 2px dashed #6c757d;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: white;
  color: #6c757d;
  transition: all 0.3s ease;
}

.image-upload-placeholder:hover {
  border-color: #f97316;
  color: #f97316;
}

.image-upload-placeholder i {
  font-size: 32px;
  margin-bottom: 8px;
}

.image-upload-placeholder p {
  margin: 0;
  font-size: 12px;
  font-weight: 500;
}

.image-upload-controls {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
  text-align: center;
}

.image-input {
  display: none;
}

.btn-upload-image {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #f97316;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
  justify-content: center;
}

.btn-upload-image:hover {
  background: #ea580c;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.3);
}

.btn-upload-image i {
  font-size: 16px;
}

.image-upload-info {
  color: #6c757d;
  font-size: 12px;
  text-align: center;
}

.image-upload-info small {
  font-style: italic;
}

/* Responsive Image Upload */
/* Large screens */
@media (min-width: 1400px) {
  .form-container {
    padding: 40px;
  }
}

/* Medium screens */
@media (max-width: 1200px) {
  .form-container {
    padding: 25px;
  }
}

/* Small screens */
@media (max-width: 768px) {
  .form-container {
    padding: 20px;
  }
  
  .image-upload-area {
    min-height: 180px;
  }

  .image-preview,
  .image-upload-placeholder {
    width: 100px;
    height: 100px;
  }
}

/* Extra small screens */
@media (max-width: 480px) {
  .form-container {
    padding: 15px;
  }
}
</style>





