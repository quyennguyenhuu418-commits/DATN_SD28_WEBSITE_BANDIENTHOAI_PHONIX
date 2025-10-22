<template>
  <div class="khach-hang-edit">
    <PosHeader />
    
    <!-- Confirm Dialog -->
    <div v-if="showConfirmDialog" class="confirm-overlay">
      <div class="confirm-dialog">
        <div class="confirm-header">
          <h3>Xác nhận cập nhật khách hàng</h3>
        </div>
        <div class="confirm-body">
          <p>Bạn có chắc chắn muốn cập nhật thông tin khách hàng <strong>{{ formData.hoTen }}</strong> không?</p>
          <div class="customer-preview">
            <p><strong>Mã:</strong> {{ customer?.maKhachHang || 'N/A' }}</p>
            <p><strong>Họ tên:</strong> {{ formData.hoTen }}</p>
            <p><strong>SĐT:</strong> {{ formData.soDienThoai }}</p>
            <p><strong>Email:</strong> {{ formData.email || 'Chưa có' }}</p>
          </div>
        </div>
        <div class="confirm-actions">
          <button class="btn-cancel" @click="cancelUpdate">Hủy</button>
          <button class="btn-confirm" @click="confirmUpdate">Xác nhận cập nhật</button>
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
          <h1>Chỉnh sửa Khách hàng</h1>
        </div>
      </div>

      <!-- Loading state -->
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>Đang tải thông tin khách hàng...</p>
    </div>

      <!-- Error state -->
      <div v-else-if="error" class="error-state">
        <div class="error-icon">⚠️</div>
        <h3>Lỗi tải dữ liệu</h3>
        <p>{{ error }}</p>
        <button @click="loadCustomer" class="btn-retry">Thử lại</button>
    </div>

      <!-- Main content -->
      <div v-else class="edit-content">
        <form @submit.prevent="handleSubmit" class="edit-form">
        <div class="form-section">
            <h3>Thông tin cơ bản</h3>
          <div class="form-grid">
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
                <font-awesome-icon icon="envelope" />
                <span>Email</span>
              </label>
              <input 
                type="email" 
                v-model="formData.email" 
                class="form-input"
                  placeholder="Nhập email"
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

            <div class="form-group">
              <label class="form-label">
                  <font-awesome-icon icon="map-marker-alt" />
                  <span>Địa chỉ cũ (sẽ được thay thế)</span>
              </label>
                <textarea 
                  v-model="formData.diaChi" 
                  class="form-textarea"
                  placeholder="Địa chỉ cũ - sẽ được thay thế bởi hệ thống địa chỉ mới"
                  rows="2"
                  readonly
                ></textarea>
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
          <h3>Thông tin tài khoản</h3>
          <div class="form-grid">
            <div class="form-group">
              <label class="form-label">
                <font-awesome-icon icon="user" />
                <span>Tài khoản</span>
              </label>
              <input 
                type="text" 
                v-model="formData.taiKhoan" 
                class="form-input"
                placeholder="Nhập tài khoản"
              />
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
                      <!-- Debug: {{ tinhThanhPhoList.length }} tỉnh/thành phố -->
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
                      <!-- Debug: {{ getPhuongXaByTinh(address.idTinhThanhPho).length }} phường/xã -->
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
              <span>{{ loading ? 'Đang lưu...' : 'Lưu thay đổi' }}</span>
          </button>
        </div>
      </form>
      </div>
    </div>

    <Toast ref="toastRef" />
    
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '@/services/api'
import Toast from '@/components/Toast.vue'
import PosHeader from '@/components/PosHeader.vue'
// FontAwesome icons are now globally available

const router = useRouter()
const route = useRoute()
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

const customer = ref<any>(null)
const loading = ref(true) // Start with loading = true
const error = ref('')

// Form data
const formData = ref({
  hoTen: '',
  soDienThoai: '',
  email: '',
  gioiTinh: '',
  ngaySinh: '',
  diaChi: '',
  taiKhoan: '',
  trangThai: true
})

// Confirm dialog state
const showConfirmDialog = ref(false)

// Address management
const addresses = ref<any[]>([])


// Administrative divisions
const tinhThanhPhoList = ref<any[]>([])
const quanHuyenList = ref<any[]>([])
const phuongXaList = ref<any[]>([])
const loadingProvinces = ref(false)
const loadingDistricts = ref(false)
const loadingWards = ref(false)

// Cache for wards by province to preserve data when adding new addresses
const wardsCache = ref<Record<string, any[]>>({})

// Individual ward lists for each address to prevent overwriting
const addressWardsLists = ref<Record<number, any[]>>({})

// Function to restore wards for a specific address
async function restoreWardsForAddress(index: number) {
  const address = addresses.value[index]
  console.log(`=== RESTORE WARDS FOR ADDRESS ${index} ===`)
  console.log('Address data:', {
    idTinhThanhPho: address.idTinhThanhPho,
    phuongXa: address.phuongXa,
    tinhThanhPho: address.tinhThanhPho
  })
  
  if (address.idTinhThanhPho && address.phuongXa) {
    console.log(`Restoring wards for address ${index}, province: ${address.idTinhThanhPho}`)
    
    // Check if we already have wards for this province in cache
    if (wardsCache.value[address.idTinhThanhPho]) {
      console.log('Using cached wards for province:', address.idTinhThanhPho)
      console.log('Cached wards count:', wardsCache.value[address.idTinhThanhPho].length)
      
      // Store wards for this specific address
      addressWardsLists.value[index] = [...wardsCache.value[address.idTinhThanhPho]]
      
      // Pre-fill the ward for this address
      const ward = addressWardsLists.value[index].find(w => w.name === address.phuongXa)
      console.log('Looking for ward:', address.phuongXa)
      console.log('Available wards:', addressWardsLists.value[index].map(w => w.name))
      console.log('Found ward:', ward)
      
      if (ward) {
        address.idPhuongXa = ward.code
        console.log(`✅ Pre-filling ward for address ${index}:`, ward.name, 'code:', ward.code)
      } else {
        console.log(`❌ Ward not found for address ${index}:`, address.phuongXa)
        // Try partial matching
        const partialMatch = addressWardsLists.value[index].find(w => 
          w.name.includes(address.phuongXa) || address.phuongXa.includes(w.name)
        )
        if (partialMatch) {
          address.idPhuongXa = partialMatch.code
          console.log(`✅ Found ward by partial match for address ${index}:`, partialMatch.name, 'code:', partialMatch.code)
        }
      }
    } else {
      console.log('No cached wards, loading from API...')
      // Load wards if not in cache
      await loadWards(address.idTinhThanhPho, index)
    }
  } else {
    console.log(`❌ Skipping address ${index} - missing province or ward data`)
  }
  
  console.log(`=== END RESTORE WARDS FOR ADDRESS ${index} ===`)
}

// Function to prefill all existing addresses
async function prefillAllAddresses() {
  console.log('=== PREFILL ALL ADDRESSES DEBUG ===')
  console.log('Total addresses:', addresses.value.length)
  
  for (let i = 0; i < addresses.value.length; i++) {
    const address = addresses.value[i]
    console.log(`Address ${i}:`, {
      idTinhThanhPho: address.idTinhThanhPho,
      phuongXa: address.phuongXa,
      tinhThanhPho: address.tinhThanhPho
    })
    
    // If we have province name but no code, find the code
    if (address.tinhThanhPho && !address.idTinhThanhPho) {
      const province = tinhThanhPhoList.value.find(p => p.name === address.tinhThanhPho)
      if (province) {
        address.idTinhThanhPho = province.code
        console.log(`Found province code for address ${i}:`, province.code)
      } else {
        console.log(`❌ Province not found for address ${i}:`, address.tinhThanhPho)
        // Try partial matching
        const partialMatch = tinhThanhPhoList.value.find(p => 
          p.name.includes(address.tinhThanhPho) || address.tinhThanhPho.includes(p.name)
        )
        if (partialMatch) {
          address.idTinhThanhPho = partialMatch.code
          console.log(`Found province by partial match for address ${i}:`, partialMatch.code)
        }
      }
    }
    
    if (address.idTinhThanhPho && address.phuongXa) {
      console.log(`Pre-filling address ${i}:`, address.phuongXa, address.tinhThanhPho)
      await restoreWardsForAddress(i)
    } else {
      console.log(`Skipping address ${i} - missing province or ward data`)
    }
  }
  
  console.log('=== END PREFILL DEBUG ===')
}

// Address management functions
async function addNewAddress() {
  console.log('Adding new address, current count:', addresses.value.length)
  
  // Store current wards list before adding new address
  const currentWards = [...phuongXaList.value]
  console.log('Storing current wards:', currentWards.length, 'items')
  
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
    macDinh: false,
    trangThai: 1
  })
  console.log('New address count:', addresses.value.length)
  
  // Restore wards list to preserve existing dropdowns
  phuongXaList.value = currentWards
  console.log('Restored wards list:', phuongXaList.value.length, 'items')
  
  // Prefill all existing addresses to preserve their dropdowns
  await prefillAllAddresses()
}

async function removeAddress(index: number) {
  if (addresses.value.length > 1) {
    const addressToRemove = addresses.value[index]
    
    // Delete from database if the address is saved
    if (addressToRemove.id) {
      try {
        await api.delete(`/api/user-dia-chi/${addressToRemove.id}`)
        console.log('Deleted address from database:', addressToRemove.id)
      } catch (error) {
        console.error('Error deleting address from database:', error)
        toastRef.value?.error('Lỗi', 'Không thể xóa địa chỉ khỏi database')
        return
      }
    }
    
    // If removing a default address, set another address as default
    if (addressToRemove.macDinh) {
      // Find another address to set as default
      const remainingAddresses = addresses.value.filter((_, i) => i !== index)
      if (remainingAddresses.length > 0) {
        // Set the first remaining address as default
        remainingAddresses[0].macDinh = true
        
        // Update in database if the address is saved
        if (remainingAddresses[0].id) {
          try {
            await api.put(`/api/user-dia-chi/${remainingAddresses[0].id}/mac-dinh`)
            console.log('Set new default address after removal')
          } catch (error) {
            console.error('Error setting new default address:', error)
          }
        }
      }
    }
    
    // Remove the address from the list
    addresses.value.splice(index, 1)
    
    // If only one address remains, make it default
    if (addresses.value.length === 1) {
      addresses.value[0].macDinh = true
      
      // Update in database if the address is saved
      if (addresses.value[0].id) {
        try {
          await api.put(`/api/user-dia-chi/${addresses.value[0].id}/mac-dinh`)
          console.log('Set remaining address as default')
        } catch (error) {
          console.error('Error setting remaining address as default:', error)
        }
      }
    }
    
    toastRef.value?.success('Thành công', 'Đã xóa địa chỉ thành công')
  }
}

function isAddressValid(address: any): boolean {
  return !!(address.diaChiChiTiet?.trim() && 
           address.tinhThanhPho && 
           address.phuongXa)
}

async function saveAddress(index: number) {
  const address = addresses.value[index]
  console.log('Saving address at index:', index)
  console.log('Address data before save:', address)
  
  if (!isAddressValid(address)) {
    console.log('Address validation failed')
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
      console.log('Updating existing DiaChi:', diaChiId)
      // Update existing DiaChi
      await api.put(`/api/dia-chi/${diaChiId}`, diaChiData)
    } else {
      console.log('Creating new DiaChi')
      // Create new DiaChi
      const diaChiResponse = await api.post('/api/dia-chi', diaChiData)
      diaChiId = diaChiResponse.data.id
      address.idDiaChi = diaChiId
      console.log('Created DiaChi with ID:', diaChiId)
    }
    
    // Mark as saved
    address.saved = true
    
    // If this is the first address or no default address exists, set it as default
    const hasDefaultAddress = addresses.value.some(addr => addr.macDinh && addr.id)
    if (!hasDefaultAddress) {
      console.log('No default address found, setting this as default')
      // Set all addresses to not default first
      addresses.value.forEach((addr, i) => {
        addr.macDinh = false
      })
      
      // Set this address as default
      address.macDinh = true
      
      // Create UserDiaChi record for this address
      try {
        const userDiaChiResponse = await api.post('/api/user-dia-chi', {
          idDiaChi: diaChiId,
          idUser: customer.value?.id,
          loaiDiaChi: address.loaiDiaChi || 'Nhà riêng',
          macDinh: true
        })
        address.id = userDiaChiResponse.data.id
        console.log('Created UserDiaChi with ID:', address.id)
        
        // Update displayed address
        updateDisplayedAddress()
        
        toastRef.value?.success('Thành công', 'Đã lưu địa chỉ và đặt làm mặc định!')
      } catch (userDiaChiError: any) {
        console.error('Error creating UserDiaChi:', userDiaChiError)
        toastRef.value?.success('Thành công', 'Đã lưu địa chỉ thành công!')
      }
    } else {
      toastRef.value?.success('Thành công', 'Đã lưu địa chỉ thành công!')
    }
  } catch (err: any) {
    console.error('Lỗi khi lưu địa chỉ:', err)
    toastRef.value?.error('Lỗi', 'Có lỗi xảy ra khi lưu địa chỉ')
  }
}

async function setDefaultAddress(index: number) {
  try {
    const address = addresses.value[index]
    console.log('Setting default address for index:', index)
    console.log('Address data:', address)
    console.log('Address valid:', isAddressValid(address))
    console.log('Address saved:', address.saved)
    console.log('Address idDiaChi:', address.idDiaChi)
    
    // Check if address is valid first
    if (!isAddressValid(address)) {
      console.log('Address is not valid')
      toastRef.value?.error('Lỗi', 'Vui lòng nhập đầy đủ thông tin địa chỉ')
      return
    }
    
    // If address is not saved yet, save it first
    if (!address.saved || !address.idDiaChi) {
      console.log('Address not saved yet, saving first...')
      await saveAddress(index)
      // After saving, the address should have idDiaChi
      if (!address.idDiaChi) {
        console.log('Failed to save address')
        toastRef.value?.error('Lỗi', 'Không thể lưu địa chỉ')
        return
      }
      console.log('Address saved successfully, idDiaChi:', address.idDiaChi)
    }
    
    // First, set all addresses to not default in UI
    addresses.value.forEach((addr, i) => {
      addr.macDinh = false
    })
    
    // Set the selected address as default in UI
    addresses.value[index].macDinh = true
    
    // If address doesn't have UserDiaChi ID, create it first
    if (!address.id) {
      console.log('Creating UserDiaChi for new address:', address)
      // Create UserDiaChi
      const userDiaChiResponse = await api.post('/api/user-dia-chi', {
        idDiaChi: address.idDiaChi,
        idUser: customer.value?.id,
        loaiDiaChi: address.loaiDiaChi || 'Nhà riêng',
        macDinh: true
      })
      address.id = userDiaChiResponse.data.id
      address.saved = true
      console.log('Created UserDiaChi with ID:', address.id)
    } else {
      // Call API to set as default
      console.log('Setting existing address as default:', address)
      const response = await api.put(`/api/user-dia-chi/${address.id}/mac-dinh`)
      console.log('API response:', response.data)
    }
    
    // Update displayed address after API call is complete
    updateDisplayedAddress()
    
    // Get the full address for the event
    const fullAddress = [
      address.diaChiChiTiet || '',
      address.phuongXa || '',
      address.tinhThanhPho || ''
    ].filter(part => part.trim()).join(', ')
    
    toastRef.value?.success('Thành công', 'Đã đặt địa chỉ làm mặc định')
    
    // Update the customer in the parent page's list directly (real-time update)
    window.dispatchEvent(new CustomEvent('customerAddressUpdated', {
      detail: {
        customerId: customer.value?.id,
        newAddress: fullAddress
      }
    }))
  } catch (error) {
    console.error('Error setting default address:', error)
    toastRef.value?.error('Lỗi', 'Không thể đặt địa chỉ làm mặc định')
    
    // Revert UI changes on error
    addresses.value.forEach((addr, i) => {
      addr.macDinh = false
    })
  }
}


// Administrative division functions
async function loadProvinces() {
  try {
    loadingProvinces.value = true
    console.log('Loading provinces...')
    const response = await api.get('/api/vietnam-administrative/provinces')
    console.log('Provinces response:', response.data)
    tinhThanhPhoList.value = response.data || []
    console.log('Provinces list set:', tinhThanhPhoList.value.length, 'items')
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
    console.log('Using fallback provinces:', tinhThanhPhoList.value.length, 'items')
    toastRef.value?.warning('Cảnh báo', 'Sử dụng dữ liệu dự phòng cho tỉnh/thành phố')
  } finally {
    loadingProvinces.value = false
  }
}

async function loadDistricts(provinceCode: string) {
  try {
    loadingDistricts.value = true
    const response = await api.get(`/api/vietnam-administrative/districts/${provinceCode}`)
    quanHuyenList.value = response.data || []
  } catch (err: any) {
    console.error('Lỗi khi tải danh sách quận/huyện:', err)
    toastRef.value?.error('Lỗi', 'Không thể tải danh sách quận/huyện')
  } finally {
    loadingDistricts.value = false
  }
}

async function loadWards(provinceCode: string, addressIndex?: number) {
  try {
    loadingWards.value = true
    console.log('Loading wards for province:', provinceCode, 'for address:', addressIndex)
    
    // Check cache first
    if (wardsCache.value[provinceCode]) {
      console.log('Using cached wards for province:', provinceCode)
      const wards = wardsCache.value[provinceCode]
      
      // Store wards for the specific address
      if (addressIndex !== undefined) {
        addressWardsLists.value[addressIndex] = [...wards]
        console.log(`Stored ${wards.length} wards for address ${addressIndex}`)
      } else {
        phuongXaList.value = wards
      }
    } else {
      const response = await api.get(`/api/vietnam-administrative/wards/${provinceCode}`)
      console.log('Wards response:', response.data)
      const wards = response.data || []
      
      // Cache the wards for this province
      wardsCache.value[provinceCode] = wards
      console.log('Cached wards for province:', provinceCode)
      
      // Store wards for the specific address
      if (addressIndex !== undefined) {
        addressWardsLists.value[addressIndex] = [...wards]
        console.log(`Stored ${wards.length} wards for address ${addressIndex}`)
      } else {
        phuongXaList.value = wards
      }
    }
    
    console.log('Wards list set:', addressIndex !== undefined ? addressWardsLists.value[addressIndex]?.length : phuongXaList.value.length, 'items')
    
    // Debug: Log all wards to see their structure
    if (phuongXaList.value.length > 0) {
      console.log('First ward structure:', phuongXaList.value[0])
      console.log('All wards:', phuongXaList.value.map(w => ({ code: w.code, name: w.name, parentCode: w.parentCode })))
    }
    
    // If this is for pre-filling, select the ward after loading
    if (addressIndex !== undefined && addresses.value[addressIndex]) {
      const address = addresses.value[addressIndex]
      if (address.phuongXa) {
        const ward = phuongXaList.value.find(w => w.name === address.phuongXa)
        if (ward) {
          address.idPhuongXa = ward.code
          console.log(`Pre-filling ward for address ${addressIndex}:`, ward.name)
        }
      }
    }
  } catch (err: any) {
    console.error('Lỗi khi tải danh sách phường/xã:', err)
    // Fallback data
    phuongXaList.value = [
      { code: '00001', name: 'Phường Phúc Xá', type: 'Phường', parentCode: provinceCode },
      { code: '00002', name: 'Phường Trúc Bạch', type: 'Phường', parentCode: provinceCode },
      { code: '00003', name: 'Phường Vĩnh Phú', type: 'Phường', parentCode: provinceCode },
      { code: '00004', name: 'Phường Cống Vị', type: 'Phường', parentCode: provinceCode },
      { code: '00005', name: 'Phường Liễu Giai', type: 'Phường', parentCode: provinceCode }
    ]
    console.log('Using fallback data:', phuongXaList.value.length, 'wards')
    toastRef.value?.warning('Cảnh báo', 'Sử dụng dữ liệu dự phòng cho phường/xã')
  } finally {
    loadingWards.value = false
  }
}

function getPhuongXaByTinh(provinceCode: string) {
  console.log('Getting wards for province code:', provinceCode)
  console.log('Available wards:', phuongXaList.value.length)
  const filtered = phuongXaList.value.filter(phuong => phuong.parentCode === provinceCode)
  console.log('Filtered wards:', filtered.length)
  return filtered
}

// Get wards for a specific address
function getWardsForAddress(addressIndex: number) {
  const address = addresses.value[addressIndex]
  if (!address) return []
  
  // Use individual ward list for this address if available
  if (addressWardsLists.value[addressIndex]) {
    console.log(`Using individual wards for address ${addressIndex}:`, addressWardsLists.value[addressIndex].length)
    return addressWardsLists.value[addressIndex]
  }
  
  // Fallback to global list
  console.log(`Using global wards for address ${addressIndex}:`, phuongXaList.value.length)
  return phuongXaList.value
}

async function onTinhThanhPhoChange(index: number) {
  const address = addresses.value[index]
  if (address.idTinhThanhPho) {
    // Reset ward when province changes
    address.idPhuongXa = ''
    address.phuongXa = ''
    
    // Load wards directly for selected province (no districts anymore)
    const selectedProvince = tinhThanhPhoList.value.find(p => p.code === address.idTinhThanhPho)
    if (selectedProvince) {
      address.tinhThanhPho = selectedProvince.name
      console.log('Loading wards for province:', selectedProvince.name, 'code:', address.idTinhThanhPho)
      await loadWards(address.idTinhThanhPho, index)
      console.log('Wards loaded:', phuongXaList.value.length)
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
      console.log(`Selected ward for address ${index}:`, selectedWard.name)
    }
  }
}

async function loadCustomerAddresses(customerId: string) {
  try {
    console.log('Loading customer addresses for ID:', customerId)
    const response = await api.get(`/api/user-dia-chi/khach-hang/${customerId}`)
    const existingAddresses = response.data || []
    console.log('API response:', response.status, existingAddresses)
    
    if (existingAddresses.length > 0) {
      console.log('Loading existing addresses:', existingAddresses)
      addresses.value = existingAddresses.map((addr: any) => {
        console.log('Processing address:', addr)
        console.log('DiaChi data:', addr.diaChi)
        
        // Use the actual DiaChi data directly instead of parsing
        const diaChi = addr.diaChi || {}
        
        const processedAddress = {
          id: addr.id,
          idUser: addr.idUser,
          idDiaChi: addr.idDiaChi,
          loaiDiaChi: addr.loaiDiaChi || '',
          diaChiChiTiet: diaChi.diaChiChiTiet || '',
          phuongXa: diaChi.phuongXa || '',
          tinhThanhPho: diaChi.tinhThanhPho || '',
          idTinhThanhPho: '', // Will be set during prefill
          idPhuongXa: '', // Will be set during prefill
          maBuuDien: diaChi.maBuuDien || '',
          ghiChu: diaChi.ghiChu || '',
          macDinh: addr.macDinh || false,
          trangThai: addr.trangThai || 1,
          saved: true // Existing addresses are already saved
        }
        
        console.log('Processed address:', processedAddress)
        return processedAddress
      })
      
      // Load provinces first, then pre-fill dropdowns for each address
      await loadProvinces()
      
      // If only one address exists, make it default
      if (addresses.value.length === 1 && !addresses.value[0].macDinh) {
        addresses.value[0].macDinh = true
        console.log('Auto-set single address as default')
        
        // Update in database if the address is saved
        if (addresses.value[0].id) {
          try {
            await api.put(`/api/user-dia-chi/${addresses.value[0].id}/mac-dinh`)
            console.log('Updated database: set single address as default')
          } catch (error) {
            console.error('Error updating single address as default:', error)
          }
        }
      }
      
      // Pre-fill all addresses to restore their dropdowns
      await prefillAllAddresses()
      
      // Set the correct selected values for each address
      for (let i = 0; i < addresses.value.length; i++) {
        const address = addresses.value[i]
        console.log(`=== SETTING VALUES FOR ADDRESS ${i} ===`)
        console.log('Address data:', {
          tinhThanhPho: address.tinhThanhPho,
          phuongXa: address.phuongXa,
          idTinhThanhPho: address.idTinhThanhPho,
          idPhuongXa: address.idPhuongXa
        })
        
        if (address.tinhThanhPho && address.phuongXa) {
          console.log(`Setting selected values for address ${i}:`, address.tinhThanhPho, address.phuongXa)
          
          // Find and set province code
          const province = tinhThanhPhoList.value.find(p => p.name === address.tinhThanhPho)
          if (province) {
            address.idTinhThanhPho = province.code
            console.log(`✅ Set province code for address ${i}:`, province.code)
          } else {
            console.log(`❌ Province not found for address ${i}:`, address.tinhThanhPho)
          }
          
          // Find and set ward code
          const wards = getWardsForAddress(i)
          console.log(`Available wards for address ${i}:`, wards.length)
          const ward = wards.find(w => w.name === address.phuongXa)
          if (ward) {
            address.idPhuongXa = ward.code
            console.log(`✅ Set ward code for address ${i}:`, ward.code)
          } else {
            console.log(`❌ Ward not found for address ${i}:`, address.phuongXa)
          }
        }
        
        console.log(`Final values for address ${i}:`, {
          idTinhThanhPho: address.idTinhThanhPho,
          idPhuongXa: address.idPhuongXa
        })
        console.log(`=== END ADDRESS ${i} ===`)
      }
    } else {
      // If no existing addresses, create a default one
      createDefaultAddress()
    }
  } catch (err: any) {
    console.error('Lỗi khi tải địa chỉ khách hàng:', err)
    // If error, create a default address
    createDefaultAddress()
  }
}

function createDefaultAddress() {
  addresses.value = [{
    id: null,
    idUser: null,
    idDiaChi: null,
    loaiDiaChi: 'Nhà riêng',
    diaChiChiTiet: formData.value.diaChi || '',
    phuongXa: '',
    tinhThanhPho: '',
    idTinhThanhPho: '',
    idPhuongXa: '',
    maBuuDien: '',
    ghiChu: '',
    macDinh: true,
    trangThai: 1,
    saved: false // New addresses are not saved yet
  }]
}

async function loadCustomer() {
  console.log('loadCustomer called')
  const customerId = Array.isArray(route.params.id) ? route.params.id[0] : route.params.id
  console.log('Customer ID:', customerId, 'Type:', typeof customerId)
  
  if (!customerId) {
    error.value = 'Không tìm thấy ID khách hàng'
    loading.value = false
    return
  }

  try {
    loading.value = true
    error.value = ''
    console.log('Loading customer data...')
    
    const response = await api.get(`/api/khach-hang/${customerId}`)
    customer.value = response.data
    
    console.log('=== LOAD CUSTOMER DEBUG ===')
    console.log('Customer data from API:', customer.value)
    console.log('Customer diaChi from API:', customer.value.diaChi)
    console.log('Customer ID:', customer.value.id)
    console.log('Customer name:', customer.value.hoTen)
    
    // Populate form data
    formData.value = {
      hoTen: customer.value.hoTen || '',
      soDienThoai: customer.value.soDienThoai || '',
      email: customer.value.email || '',
      gioiTinh: customer.value.gioiTinh || '',
      ngaySinh: formatDateForInput(customer.value.ngaySinh) || '',
      diaChi: customer.value.diaChi || '',
      taiKhoan: customer.value.taiKhoan || '',
      trangThai: customer.value.trangThai !== false
    }

    // Load existing addresses
    await loadCustomerAddresses(customerId as string)
  } catch (err: any) {
    console.error('Lỗi khi tải thông tin khách hàng:', err)
    error.value = err.response?.data?.message || 'Có lỗi xảy ra khi tải thông tin khách hàng'
  } finally {
    loading.value = false
  }
}

// Parse combined address back to individual fields
function parseCombinedAddress(combinedAddress: string) {
  if (!combinedAddress) {
    return {
      diaChiChiTiet: '',
      phuongXa: '',
      tinhThanhPho: ''
    }
  }
  
  // Split by comma and trim
  const parts = combinedAddress.split(',').map(part => part.trim())
  
  if (parts.length >= 3) {
    return {
      diaChiChiTiet: parts[0], // First part: house number, street name
      phuongXa: parts[1],      // Second part: ward/commune
      tinhThanhPho: parts[2]   // Third part: province/city
    }
  } else if (parts.length === 2) {
    return {
      diaChiChiTiet: parts[0],
      phuongXa: parts[1],
      tinhThanhPho: ''
    }
  } else {
    return {
      diaChiChiTiet: combinedAddress,
      phuongXa: '',
      tinhThanhPho: ''
    }
  }
}

// Pre-fill dropdowns for an address
async function prefillAddressDropdowns(addressIndex: number) {
  const address = addresses.value[addressIndex]
  if (!address) return
  
  console.log(`Pre-filling address ${addressIndex}:`, address)
  console.log('Available provinces:', tinhThanhPhoList.value.length)
  
  try {
    // Find and select province
    if (address.tinhThanhPho) {
      console.log(`Looking for province: "${address.tinhThanhPho}"`)
      const province = tinhThanhPhoList.value.find(p => p.name === address.tinhThanhPho)
      console.log('Found province:', province)
      
      if (province) {
        address.idTinhThanhPho = province.code
        console.log(`Pre-filling province for address ${addressIndex}:`, province.name, 'code:', province.code)
        
        // Load wards for this province and pre-fill ward
        await loadWards(province.code, addressIndex)
      } else {
        console.warn(`Province not found: "${address.tinhThanhPho}"`)
        // Try to find by partial match
        const partialMatch = tinhThanhPhoList.value.find(p => 
          p.name.includes(address.tinhThanhPho) || address.tinhThanhPho.includes(p.name)
        )
        if (partialMatch) {
          console.log('Found partial match:', partialMatch.name)
          address.idTinhThanhPho = partialMatch.code
          await loadWards(partialMatch.code, addressIndex)
        }
      }
    }
  } catch (error) {
    console.error(`Error pre-filling dropdowns for address ${addressIndex}:`, error)
  }
}

async function handleSubmit() {
  const customerId = route.params.id
  
  // Validate required fields
  if (!formData.value.hoTen?.trim()) {
    toastRef.value?.error('Lỗi', 'Vui lòng nhập họ và tên')
    return
  }

  if (!formData.value.soDienThoai?.trim()) {
    toastRef.value?.error('Lỗi', 'Vui lòng nhập số điện thoại')
    return
  }
  
  // Show confirm dialog
  showConfirmDialog.value = true
}

async function confirmUpdate() {
  showConfirmDialog.value = false
  await performUpdate()
}

function cancelUpdate() {
  showConfirmDialog.value = false
}

async function performUpdate() {
  try {
    loading.value = true
    
    const customerId = route.params.id
    
    // Prepare data for API
    const customerData = {
      ...formData.value,
      trangThai: formData.value.trangThai ? 1 : 0
    }

    // Debug: Log the data being sent
    console.log('Sending customer data:', customerData)
    
    // Update customer basic info
    const response = await api.put(`/api/khach-hang/${customerId}`, customerData)
    
    // Update addresses
    await updateCustomerAddresses(customerId as string)
    
    toastRef.value?.success('Thành công', 'Đã cập nhật thông tin khách hàng và địa chỉ thành công!')
    router.push(`/khach-hang/detail/${customerId}`)
  } catch (err: any) {
    console.error('Lỗi khi cập nhật khách hàng:', err)
    
    // Handle validation errors
    if (err.response?.status === 400 && err.response?.data) {
      const errors = err.response.data
      let errorMessage = 'Có lỗi validation:\n'
      
      if (typeof errors === 'object' && !Array.isArray(errors)) {
        Object.keys(errors).forEach(field => {
          if (field === 'error') {
            errorMessage = errors[field]
    } else {
            errorMessage += `• ${field}: ${errors[field]}\n`
          }
        })
      } else {
        errorMessage = Array.isArray(errors) ? errors.join('\n') : String(errors)
      }
      
      toastRef.value?.error('Lỗi Validation', errorMessage)
    } else {
      toastRef.value?.error('Lỗi', err.response?.data?.message || 'Có lỗi xảy ra khi cập nhật khách hàng')
    }
  } finally {
    loading.value = false
  }
}

async function updateCustomerAddresses(customerId: string) {
  try {
    // Process each address
    for (const address of addresses.value) {
      // Skip empty addresses
      if (!address.diaChiChiTiet && !address.phuongXa && !address.quanHuyen && !address.tinhThanhPho) {
        continue
      }

      // Combine address components into full address
      const fullAddress = [
        address.diaChiChiTiet || '',
        address.phuongXa || '',
        address.tinhThanhPho || ''
      ].filter(part => part.trim()).join(', ')

      // Set default values for empty fields
      const diaChiData = {
        diaChiChiTiet: fullAddress, // Combined full address
        phuongXa: address.phuongXa || '',
        quanHuyen: '', // No longer used
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
      } else {
        // Create new DiaChi
        const diaChiResponse = await api.post('/api/dia-chi', diaChiData)
        diaChiId = diaChiResponse.data.id
      }

      // Create or update UserDiaChi
      const userDiaChiData = {
        idUser: parseInt(customerId),
        idDiaChi: diaChiId,
        loaiDiaChi: address.loaiDiaChi || 'Nhà riêng',
        macDinh: address.macDinh || false,
        trangThai: 1
      }

      if (address.id) {
        // Update existing UserDiaChi
        await api.put(`/api/user-dia-chi/${address.id}`, userDiaChiData)
      } else {
        // Create new UserDiaChi
        await api.post('/api/user-dia-chi', userDiaChiData)
      }
    }
  } catch (err: any) {
    console.error('Lỗi khi cập nhật địa chỉ:', err)
    throw err
  }
}

function goBack() {
  router.go(-1)
}

// Helper function to update displayed address without reloading
function updateDisplayedAddress() {
  const defaultAddress = addresses.value.find(addr => addr.macDinh)
  if (defaultAddress) {
    // Build address string from the default address
    const addressParts = [
      defaultAddress.diaChiChiTiet,
      defaultAddress.phuongXa,
      defaultAddress.tinhThanhPho
    ].filter(part => part && part.trim()).join(', ')
    
    // Update both customer and formData
    if (customer.value) {
      customer.value.diaChi = addressParts
    }
    formData.value.diaChi = addressParts
    
    console.log('Updated displayed address:', addressParts)
  }
}

function formatDateForInput(dateString: string) {
  if (!dateString) return ''
  
  try {
    const date = new Date(dateString)
    return date.toISOString().split('T')[0]
  } catch {
    return dateString
  }
}

onMounted(async () => {
  console.log('onMounted called')
  console.log('Route params:', route.params)
  
  await loadCustomer()
  await loadProvinces()
  
  // Ensure we always have at least one address
  if (addresses.value.length === 0) {
    createDefaultAddress()
  }
  
  console.log('onMounted completed')
})
</script>

<style scoped>
.khach-hang-edit {
  background: #f8fafc;
  min-height: 100vh;
  padding: 0;
  padding-top: 80px;
  width: 100%;
  overflow-x: hidden;
  position: relative;
}

.edit-container {
  padding: 24px;
  padding-top: 0;
  width: 100%;
  margin: 0;
}

.edit-header {
  margin-bottom: 32px;
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  width: 100%;
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

/* Input with scan button */
.input-with-scan {
  position: relative;
  display: flex;
  align-items: center;
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

.btn-cancel:active {
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(239, 68, 68, 0.2);
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

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e2e8f0;
  border-top: 4px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-container p {
  color: #64748b;
  font-size: 14px;
}

.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.error-icon {
  font-size: 48px;
  color: #ef4444;
  margin-bottom: 16px;
}

.error-container h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
}

.error-container p {
  margin: 0 0 24px 0;
  color: #64748b;
  font-size: 14px;
}

.btn-retry {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.2s;
}

.btn-retry:hover {
  background: #2563eb;
  transform: translateY(-1px);
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


/* Loading and Error States */
.loading-state {
  display: flex;
    flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #4f46e5;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-state {
  display: flex;
    flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  margin: 20px;
}

.error-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.error-state h3 {
  color: #dc2626;
  margin-bottom: 8px;
}

.error-state p {
  color: #7f1d1d;
  margin-bottom: 20px;
}

.btn-retry {
  background: #dc2626;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.2s;
}

.btn-retry:hover {
  background: #b91c1c;
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