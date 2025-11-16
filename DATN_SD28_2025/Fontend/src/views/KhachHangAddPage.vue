<template>
  <div class="khach-hang-edit">
    <PosHeader />
    
    <!-- Confirm Dialog -->
    <div v-if="showConfirmDialog" class="confirm-overlay">
      <div class="confirm-dialog">
        <div class="confirm-header">
          <h3>Xác nhận thêm khách hàng</h3>
        </div>
        <div class="confirm-body">
          <p>Bạn có chắc chắn muốn thêm khách hàng <strong>{{ formData.hoTen }}</strong> không?</p>
          <div class="customer-preview">
            <p><strong>Mã:</strong> {{ formData.maKhachHang || 'Tự động tạo' }}</p>
            <p><strong>Họ tên:</strong> {{ formData.hoTen }}</p>
            <p><strong>SĐT:</strong> {{ formData.soDienThoai }}</p>
            <p><strong>Email:</strong> {{ formData.email || 'Chưa có' }}</p>
          </div>
        </div>
        <div class="confirm-actions">
          <button class="btn-cancel" @click="cancelAdd">Hủy</button>
          <button class="btn-confirm" @click="confirmAdd">Xác nhận thêm</button>
        </div>
      </div>
    </div>
    
    <div class="edit-container">
      <div class="edit-header">
      <div class="header-left">
        <button class="btn-back" @click="goBack">
          <font-awesome-icon icon="arrow-left" />
            Quay lại
        </button>
          <h1>Thêm Khách hàng</h1>
      </div>
    </div>

      <!-- Main content -->
      <div class="edit-content">
        <form @submit.prevent="handleSubmit" class="edit-form">
        <div class="form-section">
            <h3>Thông tin cơ bản</h3>
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
                  placeholder="Mã khách hàng (để trống sẽ tự động tạo)"
                />
                <small class="form-help">Để trống để hệ thống tự động tạo mã</small>
              </div>

              <div class="form-group">
                <label class="form-label">
                <font-awesome-icon icon="user" />
                <span>Họ và tên *</span>
              </label>
              <input 
                type="text" 
                v-model="formData.hoTen" 
                class="form-input"
                  placeholder="Nhập họ và tên"
                required
              />
            </div>

            <div class="form-group">
                <label class="form-label">
                <font-awesome-icon icon="phone" />
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
                  <font-awesome-icon icon="user" />
                <span>Giới tính</span>
              </label>
              <select v-model="formData.gioiTinh" class="form-select">
                  <option value="">Chọn giới tính</option>
                <option value="Nam">Nam</option>
                <option value="Nữ">Nữ</option>
                  <option value="Khác">Khác</option>
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
                />
              </div>
            </div>
          </div>

          <div class="form-section">
            <h3>Trạng thái</h3>
            <div class="form-group">
              <label class="checkbox-label">
                <input 
                  type="checkbox" 
                  v-model="formData.trangThai"
                  class="checkbox-input"
                />
                <span class="checkbox-text">Khách hàng đang hoạt động</span>
              </label>
            </div>
            </div>

          <div class="form-section">
            <h3>Thông tin đăng nhập</h3>
            <div class="form-grid">
              <div class="form-group">
                <label class="form-label">
                  <font-awesome-icon icon="envelope" />
                  <span>Email đăng nhập *</span>
                </label>
                <input 
                  type="email" 
                  v-model="formData.email" 
                  class="form-input"
                  placeholder="Nhập email để đăng nhập"
                  required
                />
                <small class="form-help">Email này sẽ được dùng để đăng nhập hệ thống</small>
              </div>
            </div>
          </div>

        <div class="form-section">
          <h3 class="section-title">
              <font-awesome-icon icon="map-marker-alt" />
              <span>Quản lý địa chỉ</span>
              <button type="button" class="btn-add-address" @click="addNewAddress">
                <font-awesome-icon icon="plus" />
                <span>Thêm địa chỉ</span>
              </button>
          </h3>
          
            <div v-if="addresses.length === 0" class="no-addresses">
              <p>Chưa có địa chỉ nào. Nhấn "Thêm địa chỉ" để thêm địa chỉ mới.</p>
            </div>
            
            <div v-for="(address, index) in addresses" :key="index" class="address-item">
              <div class="address-header">
                <h4>Địa chỉ {{ index + 1 }}</h4>
                <div class="address-actions">
                  <button 
                    type="button" 
                    class="btn-set-default"
                    :class="{ active: address.macDinh }"
                    @click="setDefaultAddress(index)"
                    :disabled="address.macDinh"
                  >
                    <font-awesome-icon icon="star" />
                    <span>{{ address.macDinh ? 'Mặc định' : 'Đặt mặc định' }}</span>
                  </button>
                  <button 
                    type="button" 
                    class="btn-save-address"
                    :class="{ 'saved': address.saved }"
                    @click="saveAddress(index)"
                    :disabled="!isAddressValid(address) || address.saved"
                  >
                    <font-awesome-icon :icon="address.saved ? 'check' : 'floppy-disk'" />
                    <span>{{ address.saved ? 'Đã lưu' : 'Lưu địa chỉ' }}</span>
                  </button>
                  <button 
                    type="button" 
                    class="btn-remove-address"
                    @click="removeAddress(index)"
                    :disabled="addresses.length === 1"
                    title="Xóa địa chỉ"
                  >
                    <font-awesome-icon icon="trash" />
                    <span>Xóa</span>
                  </button>
                </div>
              </div>
              
              <div class="address-form">
                <div class="form-grid">
                  <div class="form-group">
                    <label class="form-label">
                      <font-awesome-icon icon="tag" />
                      <span>Loại địa chỉ</span>
                    </label>
                    <select v-model="address.loaiDiaChi" class="form-select">
                      <option value="">Chọn loại địa chỉ</option>
                      <option value="Nhà riêng">Nhà riêng</option>
                      <option value="Công ty">Công ty</option>
                      <option value="Khác">Khác</option>
                    </select>
                  </div>
                  
                  <div class="form-group">
                    <label class="form-label">
                      <font-awesome-icon icon="map-marker-alt" />
                      <span>Số nhà, tên đường *</span>
                    </label>
                    <input 
                      type="text" 
                      v-model="address.diaChiChiTiet" 
                      class="form-input"
                      placeholder="Số nhà, tên đường..."
                    />
                    <small class="form-help">Ví dụ: 123 Đường Lê Lợi, Khu phố 1</small>
                  </div>

                  <div class="form-group">
                    <label class="form-label">
                      <font-awesome-icon icon="flag" />
                      <span>Tỉnh/Thành phố</span>
                    </label>
                    <select 
                      v-model="address.idTinhThanhPho" 
                      class="form-select"
                      @change="onTinhThanhPhoChange(index)"
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
                  </div>
                  
                  <div class="form-group">
                    <label class="form-label">
                      <font-awesome-icon icon="building" />
                      <span>Phường/Xã</span>
                    </label>
                    <select 
                      v-model="address.idPhuongXa" 
                      class="form-select"
                      @change="onPhuongXaChange(index)"
                      :disabled="!address.idTinhThanhPho"
                    >
                      <option value="">Chọn phường/xã</option>
                      <option 
                        v-for="phuong in getWardsForAddress(index)" 
                        :key="phuong.code" 
                        :value="phuong.code"
                      >
                        {{ phuong.name }}
                      </option>
                    </select>
                  </div>
                  
                  <div class="form-group">
                    <label class="form-label">
                      <font-awesome-icon icon="mail-bulk" />
                      <span>Mã bưu điện</span>
                    </label>
              <input 
                      type="text" 
                      v-model="address.maBuuDien" 
                      class="form-input"
                      placeholder="Mã bưu điện (tùy chọn)"
                    />
                  </div>
                </div>
                
                <div class="form-group">
                  <label class="form-label">
                    <font-awesome-icon icon="comment" />
                    <span>Ghi chú</span>
            </label>
                  <textarea 
                    v-model="address.ghiChu" 
                    class="form-textarea"
                    placeholder="Ghi chú về địa chỉ này"
                    rows="2"
                  ></textarea>
                </div>
              </div>
          </div>
        </div>

        <div class="form-actions">
            <button type="button" class="btn-cancel" @click="goBack">
              <font-awesome-icon icon="xmark" />
              <span>Hủy bỏ</span>
          </button>
            <button type="submit" class="btn-save" :disabled="loading">
              <font-awesome-icon icon="floppy-disk" />
              <span>{{ loading ? 'Đang thêm...' : 'Thêm khách hàng' }}</span>
          </button>
        </div>
      </form>
      </div>
    </div>

    <Toast ref="toastRef" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '@/services/api'
import Toast from '@/components/Toast.vue'
import PosHeader from '@/components/PosHeader.vue'

const router = useRouter()
const route = useRoute()
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

const loading = ref(false)

// Form data
const formData = ref({
  maKhachHang: '',
  hoTen: '',
  soDienThoai: '',
  email: '',
  gioiTinh: '',
  ngaySinh: '',
  taiKhoan: '',
  trangThai: true
})

// Confirm dialog state
const showConfirmDialog = ref(false)

// Address management
const addresses = ref<any[]>([])

// Administrative divisions
const tinhThanhPhoList = ref<any[]>([])
const phuongXaList = ref<any[]>([])
const loadingProvinces = ref(false)
const loadingWards = ref(false)

// Cache for wards by province to preserve data when adding new addresses
const wardsCache = ref<Record<string, any[]>>({})

// Individual ward lists for each address to prevent overwriting
const addressWardsLists = ref<Record<number, any[]>>({})

// Address management functions
async function addNewAddress() {
  addresses.value.push({
    id: null,
    idUser: null,
    idDiaChi: null,
    loaiDiaChi: '',
    diaChiChiTiet: '',
    phuongXa: '',
    tinhThanhPho: '',
    idTinhThanhPho: '',
    idPhuongXa: '',
    maBuuDien: '',
    ghiChu: '',
    macDinh: addresses.value.length === 0,
    trangThai: 1,
    saved: false
  })
}

async function removeAddress(index: number) {
  if (addresses.value.length > 1) {
    addresses.value.splice(index, 1)
  }
}

function isAddressValid(address: any): boolean {
  return !!(address.diaChiChiTiet?.trim() && 
           address.tinhThanhPho && 
           address.phuongXa)
}

async function saveAddress(index: number) {
  const address = addresses.value[index]
  
  if (!isAddressValid(address)) {
    toastRef.value?.error('Lỗi', 'Vui lòng nhập đầy đủ thông tin địa chỉ')
    return
  }

  try {
    // Prepare DiaChi data (don't combine into full address, keep components separate)
    const diaChiData = {
      diaChiChiTiet: address.diaChiChiTiet || '',
      phuongXa: address.phuongXa || '',
      quanHuyen: '',
      tinhThanhPho: address.tinhThanhPho || '',
      maBuuDien: address.maBuuDien || '',
      ghiChu: address.ghiChu || '',
      trangThai: 1
    }

    let diaChiId = address.idDiaChi

    // Create or update DiaChi
    if (diaChiId) {
      // Update existing DiaChi
      await api.put(`/api/dia-chi/${diaChiId}`, diaChiData)
      console.log('DiaChi updated successfully:', diaChiId)
    } else {
      // Create new DiaChi
      const diaChiResponse = await api.post('/api/dia-chi', diaChiData)
      diaChiId = diaChiResponse.data.id
      address.idDiaChi = diaChiId
      console.log('DiaChi created successfully:', diaChiId)
    }
    
    // Mark as saved (this prepares the address for UserDiaChi creation later)
    address.saved = true
    
    toastRef.value?.success('Thành công', 'Đã lưu địa chỉ thành công!')
  } catch (err: any) {
    console.error('Lỗi khi lưu địa chỉ:', err)
    console.error('Error details:', err.response?.data)
    toastRef.value?.error('Lỗi', 'Có lỗi xảy ra khi lưu địa chỉ')
  }
}

async function setDefaultAddress(index: number) {
  // Set all addresses to not default first
  addresses.value.forEach((addr, i) => {
    addr.macDinh = false
  })
  
  // Set the selected address as default
  addresses.value[index].macDinh = true
  
  toastRef.value?.success('Thành công', 'Đã đặt địa chỉ làm mặc định')
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

async function loadWards(provinceCode: string, addressIndex?: number) {
  try {
    loadingWards.value = true
    
    // Check cache first
    if (wardsCache.value[provinceCode]) {
      const wards = wardsCache.value[provinceCode]
      
      // Store wards for the specific address
      if (addressIndex !== undefined) {
        addressWardsLists.value[addressIndex] = [...wards]
      } else {
        phuongXaList.value = wards
      }
    } else {
      const response = await api.get(`/api/vietnam-administrative/wards/${provinceCode}`)
      const wards = response.data || []
      
      // Cache the wards for this province
      wardsCache.value[provinceCode] = wards
      
      // Store wards for the specific address
      if (addressIndex !== undefined) {
        addressWardsLists.value[addressIndex] = [...wards]
      } else {
        phuongXaList.value = wards
      }
    }
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
    if (addressIndex !== undefined) {
      addressWardsLists.value[addressIndex] = [...fallbackWards]
    } else {
      phuongXaList.value = [...fallbackWards]
    }
    toastRef.value?.warning('Cảnh báo', 'Sử dụng dữ liệu dự phòng cho phường/xã')
  } finally {
    loadingWards.value = false
  }
}

// Get wards for a specific address
function getWardsForAddress(addressIndex: number) {
  const address = addresses.value[addressIndex]
  if (!address) return []
  
  // Use individual ward list for this address if available
  if (addressWardsLists.value[addressIndex]) {
    return addressWardsLists.value[addressIndex]
  }
  
  // Fallback to global list
  return phuongXaList.value
}

async function onTinhThanhPhoChange(index: number) {
  const address = addresses.value[index]
  if (address.idTinhThanhPho) {
    // Reset ward when province changes
    address.idPhuongXa = ''
    address.phuongXa = ''
    
    // Load wards directly for selected province
    const selectedProvince = tinhThanhPhoList.value.find(p => p.code === address.idTinhThanhPho)
    if (selectedProvince) {
      address.tinhThanhPho = selectedProvince.name
      await loadWards(address.idTinhThanhPho, index)
    }
  }
}

function onPhuongXaChange(index: number) {
  const address = addresses.value[index]
  if (address.idPhuongXa) {
    // Use individual ward list for this address
    const wards = getWardsForAddress(index)
    const selectedWard = wards.find(w => w.code === address.idPhuongXa)
    if (selectedWard) {
      address.phuongXa = selectedWard.name
    }
  }
}

async function handleSubmit() {
  // Validate required fields
  if (!formData.value.hoTen?.trim()) {
    toastRef.value?.error('Lỗi', 'Vui lòng nhập họ và tên')
    return
  }

  if (!formData.value.soDienThoai?.trim()) {
    toastRef.value?.error('Lỗi', 'Vui lòng nhập số điện thoại')
    return
  }

  if (!formData.value.email?.trim()) {
    toastRef.value?.error('Lỗi', 'Vui lòng nhập email đăng nhập')
    return
  }
  
  // Show confirm dialog
  showConfirmDialog.value = true
}

async function confirmAdd() {
  showConfirmDialog.value = false
  await performAdd()
}

function cancelAdd() {
  showConfirmDialog.value = false
}

async function performAdd() {
  try {
    loading.value = true

    // Prepare customer data (without addresses)
    const customerData = {
      hoTen: formData.value.hoTen,
      soDienThoai: formData.value.soDienThoai,
      email: formData.value.email?.trim(),
      gioiTinh: formData.value.gioiTinh?.trim() || null,
      ngaySinh: formData.value.ngaySinh || null,
      trangThai: formData.value.trangThai ? 1 : 0,
      // Only include maKhachHang if it's not empty
      ...(formData.value.maKhachHang.trim() && { maKhachHang: formData.value.maKhachHang.trim() })
    }

    // Debug: Log data being sent
    console.log('Data being sent to server:', customerData)
    
    // Create customer first
    const response = await api.post('/api/khach-hang', customerData)
    const newCustomerId = response.data.id
    
    // Create addresses for the new customer
    await createCustomerAddresses(newCustomerId)
    
    // Show success message with confirmation
    toastRef.value?.success('Thành công', `Đã tạo khách hàng "${formData.value.hoTen}" thành công!`)
    
    // Redirect to customer list
    router.push('/khach-hang')
  } catch (err: any) {
    console.error('Lỗi khi thêm khách hàng:', err)
    console.error('Response data:', JSON.stringify(err.response?.data, null, 2))
    console.error('Response status:', err.response?.status)
    console.error('Full error object:', err)
    
    // Handle validation errors
    if (err.response?.status === 400 && err.response?.data) {
      const errors = err.response.data
      
      if (typeof errors === 'object' && !Array.isArray(errors)) {
        // Show each validation error separately
        Object.keys(errors).forEach(field => {
          if (field === 'error') {
            toastRef.value?.error('Lỗi', errors[field])
          } else {
            // Map field names to Vietnamese
            const fieldNames: { [key: string]: string } = {
              'hoTen': 'Họ và tên',
              'soDienThoai': 'Số điện thoại',
              'email': 'Email',
              'ngaySinh': 'Ngày sinh',
              'gioiTinh': 'Giới tính',
              'taiKhoan': 'Tài khoản',
              'maKhachHang': 'Mã khách hàng'
            }
            
            const fieldName = fieldNames[field] || field
            toastRef.value?.error(`Lỗi ${fieldName}`, errors[field])
          }
        })
      } else {
        const errorMessage = Array.isArray(errors) ? errors.join('\n') : String(errors)
        toastRef.value?.error('Lỗi Validation', errorMessage)
      }
    } else {
      toastRef.value?.error('Lỗi', err.response?.data?.message || 'Có lỗi xảy ra khi thêm khách hàng')
    }
  } finally {
    loading.value = false
  }
}

async function createCustomerAddresses(customerId: number) {
  try {
    console.log('Creating UserDiaChi records for customer ID:', customerId)
    console.log('Addresses to process:', addresses.value)
    
    for (const address of addresses.value) {
      console.log('Processing address:', address)
      
      // Skip empty or unsaved addresses
      if (!address.saved || !address.idDiaChi) {
        console.log('Skipping address - not saved or no DiaChi ID:', address)
        continue
      }

      // Create UserDiaChi record using existing DiaChi ID
      const userDiaChiData = {
        idUser: customerId,
        idDiaChi: address.idDiaChi,
        loaiDiaChi: address.loaiDiaChi || 'Nhà riêng',
        macDinh: address.macDinh || false,
        trangThai: 1
      }
      
      console.log('Creating UserDiaChi with data:', userDiaChiData)
      try {
        const response = await api.post('/api/user-dia-chi', userDiaChiData)
        console.log('UserDiaChi created successfully:', response.data)
      } catch (userDiaChiError: any) {
        console.error('Error creating UserDiaChi:', userDiaChiError)
        console.error('UserDiaChi error response:', userDiaChiError.response?.data)
        throw new Error(`Failed to create UserDiaChi: ${userDiaChiError.response?.data?.message || userDiaChiError.message}`)
      }
    }
  } catch (err: any) {
    console.error('Lỗi khi tạo địa chỉ:', err)
    throw new Error('Không thể tạo địa chỉ cho khách hàng')
  }
}

function goBack() {
  router.push('/khach-hang')
}

onMounted(async () => {
  // Load provinces
  await loadProvinces()
  
  // Load data from query params if available (from CCCD scan)
  if (route.query.hoTen) {
    formData.value.hoTen = route.query.hoTen as string
  }
  if (route.query.soDienThoai) {
    formData.value.soDienThoai = route.query.soDienThoai as string
  }
  if (route.query.email) {
    formData.value.email = route.query.email as string
  }
  if (route.query.ngaySinh) {
    formData.value.ngaySinh = route.query.ngaySinh as string
  }
  if (route.query.gioiTinh) {
    formData.value.gioiTinh = route.query.gioiTinh as string
  }
})
</script>

<style scoped>
.khach-hang-edit {
  background: #f8fafc;
  min-height: 100vh;
  padding: 0;
  padding-top: 80px;
  width: 100%;
  overflow-x: visible;
  position: relative;
}

.edit-container {
  padding: 24px;
  padding-top: 0;
  width: 100%;
  max-width: none;
  margin: 0;
  overflow-x: visible;
}

.edit-header {
  margin-bottom: 32px;
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  width: 100%;
  min-width: 0;
  max-width: none;
  overflow: visible;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  min-width: 0;
  flex: 1;
  width: 100%;
  max-width: none;
}

.btn-back {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #f1f5f9;
  color: #475569;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-back:hover {
  background: #e2e8f0;
  color: #334155;
}

.edit-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  white-space: nowrap;
  overflow: visible;
  text-overflow: unset;
  flex-shrink: 0;
  min-width: max-content;
  width: auto;
}

.edit-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.edit-form {
  padding: 32px;
}

.form-section {
  margin-bottom: 32px;
}

.form-section h3 {
  margin: 0 0 20px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  padding-bottom: 12px;
  border-bottom: 2px solid #f1f5f9;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 24px;
  width: 100%;
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
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.form-label i {
  color: #6b7280;
}

.form-input,
.form-select,
.form-textarea {
  padding: 12px 16px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.2s;
  background: white;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-help {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #6b7280;
  font-style: italic;
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 12px 16px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  transition: all 0.2s;
}

.checkbox-label:hover {
  background: #f1f5f9;
}

.checkbox-input {
  width: 18px;
  height: 18px;
  accent-color: #3b82f6;
}

.checkbox-text {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.form-actions {
  display: flex;
  gap: 16px;
  justify-content: flex-end;
  padding-top: 24px;
  border-top: 1px solid #e2e8f0;
}

.btn-cancel,
.btn-save {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.2s;
}

.btn-cancel {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: white;
  border: none;
  box-shadow: 0 2px 4px rgba(239, 68, 68, 0.2);
}

.btn-cancel:hover {
  background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.4);
}

.btn-save {
  background: #3b82f6;
  color: white;
}

.btn-save:hover:not(:disabled) {
  background: #2563eb;
  transform: translateY(-1px);
}

.btn-save:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Section title with button */
.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.section-title h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  color: #374151;
  font-size: 18px;
  font-weight: 600;
}

/* Address management */
.btn-add-address {
  padding: 8px 16px;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
}

.btn-add-address:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.4);
}

.no-addresses {
  text-align: center;
  padding: 40px 20px;
  color: #6b7280;
  background: #f9fafb;
  border-radius: 8px;
  border: 2px dashed #d1d5db;
}

.address-item {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.address-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e5e7eb;
}

.address-header h4 {
  margin: 0;
  color: #374151;
  font-size: 16px;
  font-weight: 600;
}

.address-actions {
  display: flex;
  gap: 8px;
}

.btn-set-default {
  padding: 6px 12px;
  background: #f3f4f6;
  color: #6b7280;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 4px;
}

.btn-set-default:hover:not(:disabled) {
  background: #e5e7eb;
  color: #374151;
}

.btn-set-default.active {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  color: white;
  border-color: #f59e0b;
}

.btn-set-default:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-remove-address {
  padding: 8px 16px;
  background: #fef2f2;
  color: #dc2626;
  border: 1px solid #fecaca;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  min-width: 60px;
}

.btn-remove-address:hover:not(:disabled) {
  background: #fee2e2;
  border-color: #fca5a5;
}

.btn-remove-address:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-save-address {
  padding: 6px 12px;
  background: #f0fdf4;
  color: #16a34a;
  border: 1px solid #bbf7d0;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
}

.btn-save-address:hover:not(:disabled) {
  background: #dcfce7;
  border-color: #86efac;
}

.btn-save-address:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-save-address.saved {
  background: #f3f4f6;
  color: #6b7280;
  border-color: #d1d5db;
}

.btn-save-address.saved:hover {
  background: #f3f4f6;
  border-color: #d1d5db;
}

.address-form .form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

/* Responsive */
@media (max-width: 768px) {
  .khach-hang-edit {
    padding-top: 60px;
  }
  
  .edit-container {
    padding: 16px;
  }
  
  .edit-form {
    padding: 20px;
  }
  
  .form-grid {
    grid-template-columns: 1fr;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .btn-cancel,
  .btn-save {
    width: 100%;
    justify-content: center;
  }
  
  .header-left {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .edit-header h1 {
    font-size: 20px;
    white-space: normal;
    word-break: break-word;
  }
}

/* Confirm Dialog Styles */
.confirm-overlay {
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

.confirm-dialog {
  background: white;
  border-radius: 12px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  max-width: 500px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.confirm-header {
  padding: 24px 24px 16px;
  border-bottom: 1px solid #e5e7eb;
}

.confirm-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.confirm-body {
  padding: 20px 24px;
}

.confirm-body p {
  margin: 0 0 16px;
  color: #374151;
  line-height: 1.5;
}

.customer-preview {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  margin-top: 16px;
}

.customer-preview p {
  margin: 0 0 8px;
  font-size: 14px;
  color: #4b5563;
}

.customer-preview p:last-child {
  margin-bottom: 0;
}

.confirm-actions {
  padding: 16px 24px 24px;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.btn-cancel {
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
  padding: 10px 20px;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel:hover {
  background: #e5e7eb;
  border-color: #9ca3af;
}

.btn-confirm {
  background: #dc2626;
  color: white;
  border: 1px solid #dc2626;
  padding: 10px 20px;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-confirm:hover {
  background: #b91c1c;
  border-color: #b91c1c;
}

@media (max-width: 480px) {
  .khach-hang-edit {
    padding-top: 50px;
  }
  
  .edit-container {
    padding: 12px;
  }
  
  .edit-form {
    padding: 16px;
  }
  
  .edit-header {
    padding: 16px;
  }
  
  .confirm-dialog {
    width: 95%;
    margin: 20px;
  }
  
  .confirm-actions {
    flex-direction: column;
  }
  
  .btn-cancel,
  .btn-confirm {
    width: 100%;
  }
}
</style>