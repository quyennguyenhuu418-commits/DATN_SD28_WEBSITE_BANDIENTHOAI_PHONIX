<template>
  <div class="giao-ca-create-page dark-mode-transition">
    <PosHeader />
    
    <div class="edit-container">
      <div class="edit-header">
        <div class="header-left">
          <button class="btn-back" @click="goBack">
            <font-awesome-icon :icon="['fas', 'arrow-left']" />
            Quay lại
          </button>
          <h1>{{ isEdit ? 'Sửa Giao Ca' : 'Thêm Giao Ca Mới' }}</h1>
        </div>
      </div>

      <!-- Loading state -->
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>Đang tải thông tin...</p>
      </div>

      <!-- Main content -->
      <div v-else class="edit-content">
        <form @submit.prevent="handleSubmit" class="edit-form">
          <div class="form-section">
            <h3>Thông tin giao ca</h3>
            <div class="info-notice" v-if="route.query.phanCaId">
              <font-awesome-icon :icon="['fas', 'info-circle']" />
              <div>
                <strong>Bạn đang giao ca đã kết thúc</strong>
                <p>
                  Ca này đã được kết thúc. Bạn có thể chọn nhân viên nhận ca (tùy chọn). 
                  Nếu để trống, nhân viên có ca tiếp theo sẽ tự xác nhận giao ca khi đăng nhập. 
                  Sau khi tạo giao ca, nhân viên nhận ca sẽ xác nhận để hoàn tất quá trình giao ca và bắt đầu ca của họ.
                </p>
              </div>
            </div>
            <div class="form-grid">
              <div class="form-group">
                <label class="form-label required">
                  <font-awesome-icon :icon="['fas', 'calendar-alt']" />
                  <span>Phân ca *</span>
                </label>
                <!-- Dropdown chỉ hiển thị khi chưa chọn phân ca và không phải edit mode -->
                <select
                  v-if="!formData.phanCaId && !isEdit"
                  v-model="formData.phanCaId"
                  class="form-select"
                  required
                  :disabled="loading"
                  @change="onPhanCaChange"
                >
                  <option value="">Chọn phân ca</option>
                  <option v-for="pc in phanCaList" :key="pc.id" :value="pc.id">
                    {{ pc.nhanVien?.hoTen || 'N/A' }} - {{ pc.ca?.tenCa || 'N/A' }} ({{ formatDate(pc.ngayLamViec) }})
                  </option>
                </select>
                <!-- Input readonly hiển thị thông tin phân ca đã chọn -->
                <input
                  v-else
                  type="text"
                  :value="selectedPhanCaDisplay"
                  class="form-input"
                  readonly
                  disabled
                  @click="!isEdit && (formData.phanCaId = '')"
                />
                <small v-if="formData.phanCaId && !isEdit" class="form-help">
                  Nhấp vào để chọn lại phân ca khác
                </small>
                <small v-if="isEdit" class="form-help">
                  Không thể thay đổi phân ca khi đang chỉnh sửa
                </small>
              </div>

              <div class="form-group">
                <label class="form-label required">
                  <font-awesome-icon :icon="['fas', 'user']" />
                  <span>Nhân viên giao *</span>
                </label>
                <select
                  v-model="formData.nhanVienGiaoId"
                  class="form-select"
                  required
                  :disabled="loading || formData.phanCaId || isEdit"
                >
                  <option value="">Chọn nhân viên giao</option>
                  <option v-for="nv in nhanVienList" :key="nv.id" :value="nv.id">
                    {{ nv.hoTen }} ({{ nv.maNhanVien }})
                  </option>
                </select>
                <small v-if="formData.phanCaId || isEdit" class="form-help">
                  {{ isEdit ? 'Không thể thay đổi nhân viên giao khi đang chỉnh sửa' : 'Đã tự động chọn theo phân ca' }}
                </small>
              </div>

              <div class="form-group">
                <label class="form-label">
                  <font-awesome-icon :icon="['fas', 'user-plus']" />
                  <span>Nhân viên nhận ca</span>
                </label>
                <input
                  type="text"
                  :value="nextShiftEmployeeInfo"
                  class="form-input"
                  readonly
                  disabled
                  placeholder="Đang tìm nhân viên có ca tiếp theo..."
                  v-if="loadingNextShift || !formData.phanCaId"
                />
                <select
                  v-else
                  v-model="formData.nhanVienNhanId"
                  class="form-select"
                  :disabled="loading || nextShiftEmployeeId === null"
                >
                  <option v-if="nextShiftEmployeeId === null" value="">
                    Không tìm thấy ca tiếp theo
                  </option>
                  <option v-else :value="nextShiftEmployeeId">
                    {{ nextShiftEmployeeInfo }}
                  </option>
                </select>
                <small class="form-help" v-if="nextShiftEmployeeId">
                  Nhân viên có ca tiếp theo sau ca này
                </small>
                <small class="form-help" v-else-if="formData.phanCaId && !loadingNextShift">
                  Không tìm thấy ca tiếp theo cho phân ca này
                </small>
              </div>

              <div class="form-group">
                <label class="form-label">
                  <font-awesome-icon :icon="['fas', 'money-bill-wave']" />
                  <span>Số tiền đầu ca</span>
                </label>
                <input
                  type="number"
                  v-model="formData.soTienDauCa"
                  class="form-input"
                  step="1000"
                  min="0"
                  placeholder="Nhập số tiền đầu ca..."
                  :disabled="loading"
                  :readonly="isEdit"
                />
                <small class="form-help">
                  {{ isEdit ? 'Không thể thay đổi số tiền đầu ca khi đang chỉnh sửa' : 'Để trống sẽ tự động lấy từ giao ca trước đó' }}
                </small>
              </div>

              <div class="form-group">
                <label class="form-label required">
                  <font-awesome-icon :icon="['fas', 'money-bill-wave']" />
                  <span>Số tiền cuối ca *</span>
                </label>
                <input
                  type="number"
                  v-model="formData.soTienCuoiCa"
                  class="form-input"
                  step="1000"
                  min="0"
                  placeholder="Đếm và nhập số tiền cuối ca..."
                  required
                  :disabled="loading"
                />
                <small class="form-help">Vui lòng đếm tiền mặt thực tế và nhập vào</small>
              </div>

              <div class="form-group">
                <label class="form-label">
                  <font-awesome-icon :icon="['fas', 'plus-circle']" />
                  <span>Số tiền thu thêm</span>
                </label>
                <input
                  type="number"
                  v-model="formData.soTienThuThem"
                  class="form-input"
                  step="1000"
                  min="0"
                  placeholder="Tiền thu thêm (nếu có)..."
                  :disabled="loading"
                />
                <small class="form-help">Tiền thu thêm ngoài bán hàng (nếu có)</small>
              </div>

              <div class="form-group">
                <label class="form-label">
                  <font-awesome-icon :icon="['fas', 'minus-circle']" />
                  <span>Số tiền chi ra</span>
                </label>
                <input
                  type="number"
                  v-model="formData.soTienChiRa"
                  class="form-input"
                  step="1000"
                  min="0"
                  placeholder="Tiền chi ra (nếu có)..."
                  :disabled="loading"
                />
                <small class="form-help">Tiền chi ra trong ca (nếu có)</small>
              </div>
            </div>
          </div>

          <div class="form-section">
            <h3>Báo cáo công việc</h3>
            <div class="form-grid">
              <div class="form-group full-width">
                <label class="form-label">
                  <font-awesome-icon :icon="['fas', 'clipboard-list']" />
                  <span>Báo cáo công việc</span>
                </label>
                <textarea
                  v-model="formData.baoCaoCongViec"
                  class="form-textarea"
                  rows="4"
                  placeholder="Mô tả công việc đã làm trong ca..."
                  :disabled="loading"
                ></textarea>
              </div>

              <div class="form-group full-width">
                <label class="form-label">
                  <font-awesome-icon :icon="['fas', 'exclamation-triangle']" />
                  <span>Sự cố bất thường</span>
                </label>
                <textarea
                  v-model="formData.suCoBatThuong"
                  class="form-textarea"
                  rows="3"
                  placeholder="Ghi nhận các sự cố bất thường (nếu có)..."
                  :disabled="loading"
                ></textarea>
              </div>

              <div class="form-group full-width">
                <label class="form-label">
                  <font-awesome-icon :icon="['fas', 'tasks']" />
                  <span>Công việc tồn đọng</span>
                </label>
                <textarea
                  v-model="formData.congViecTonDong"
                  class="form-textarea"
                  rows="3"
                  placeholder="Liệt kê các công việc còn tồn đọng..."
                  :disabled="loading"
                ></textarea>
              </div>

              <div class="form-group full-width">
                <label class="form-label">
                  <font-awesome-icon :icon="['fas', 'comment']" />
                  <span>Ghi chú</span>
                </label>
                <textarea
                  v-model="formData.ghiChu"
                  class="form-textarea"
                  rows="3"
                  placeholder="Nhập ghi chú (nếu có)..."
                  :disabled="loading"
                ></textarea>
              </div>
            </div>
          </div>

          <div class="form-actions">
            <button type="button" class="btn-cancel" @click="goBack" :disabled="loading">
              <font-awesome-icon :icon="['fas', 'times']" />
              <span>Hủy bỏ</span>
            </button>
            <button type="submit" class="btn-save" :disabled="loading || !isFormValid">
              <font-awesome-icon :icon="['fas', 'save']" />
              <span>{{ loading ? 'Đang lưu...' : (isEdit ? 'Cập nhật' : 'Thêm mới') }}</span>
            </button>
          </div>
        </form>
      </div>
    </div>

    <Toast ref="toastRef" />
    <ConfirmModal
      :show="showConfirmModal"
      :title="confirmTitle"
      :message="confirmMessage"
      @confirm="handleConfirm"
      @cancel="handleCancel"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '@/services/api'
import Toast from '@/components/Toast.vue'
import PosHeader from '@/components/PosHeader.vue'
import ConfirmModal from '@/components/ConfirmModal.vue'
import { useAuthStore } from '@/stores/authStore'

const router = useRouter()
const route = useRoute()
const toastRef = ref<InstanceType<typeof Toast> | null>(null)
const authStore = useAuthStore()

const loading = ref(false)
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const pendingAction = ref<(() => void) | null>(null)

// Check if editing
const giaoCaId = computed(() => route.params.id as string | undefined)
const isEdit = computed(() => !!giaoCaId.value && giaoCaId.value !== 'new')

interface NhanVien {
  id: number
  hoTen: string
  maNhanVien: string
}

interface PhanCa {
  id: number
  nhanVienId: number
  caId: number
  ngayLamViec: string
  nhanVien?: {
    id: number
    hoTen: string
  }
  ca?: {
    id: number
    tenCa: string
    gioBatDau: string
    gioKetThuc: string
  }
}

const nhanVienList = ref<NhanVien[]>([])
const phanCaList = ref<PhanCa[]>([])
const loadingNextShift = ref(false)
const nextShiftEmployeeId = ref<number | null>(null)
const nextShiftEmployeeInfo = ref('')

const formData = ref({
  phanCaId: '',
  nhanVienGiaoId: '',
  nhanVienNhanId: '',
  soTienDauCa: '',
  soTienCuoiCa: '',
  soTienThuThem: '',
  soTienChiRa: '',
  baoCaoCongViec: '',
  suCoBatThuong: '',
  congViecTonDong: '',
  ghiChu: ''
})

const isFormValid = computed(() => {
  return formData.value.phanCaId !== '' && 
         formData.value.nhanVienGiaoId !== '' &&
         formData.value.soTienCuoiCa !== '' &&
         parseFloat(formData.value.soTienCuoiCa) >= 0
})

function formatDate(date: string) {
  if (!date) return 'N/A'
  const d = new Date(date)
  return d.toLocaleDateString('vi-VN')
}

// Computed property để hiển thị phân ca đã chọn
const selectedPhanCaDisplay = computed(() => {
  if (!formData.value.phanCaId) return ''
  
  const selectedPhanCa = phanCaList.value.find(pc => pc.id === parseInt(formData.value.phanCaId))
  if (!selectedPhanCa) return ''
  
  const employeeName = selectedPhanCa.nhanVien?.hoTen || 'N/A'
  const gioBatDau = formatTime(selectedPhanCa.ca?.gioBatDau)
  const gioKetThuc = formatTime(selectedPhanCa.ca?.gioKetThuc)
  
  // Format: "Tên nhân viên (Giờ bắt đầu - Giờ kết thúc)"
  // Ví dụ: "Trần Văn Admin (07:00 - 12:00)"
  return `${employeeName} (${gioBatDau} - ${gioKetThuc})`
})

async function loadNhanVien() {
  try {
    const { data } = await api.get<NhanVien[]>('/api/nhan-vien')
    nhanVienList.value = data
  } catch (error) {
    console.error('Lỗi khi tải danh sách nhân viên:', error)
    toastRef.value?.error('Lỗi', 'Không thể tải danh sách nhân viên')
  }
}

async function loadPhanCa() {
  try {
    const { data } = await api.get<PhanCa[]>('/api/phan-ca')
    // OPTION 1: Chỉ lấy các phân ca ĐÃ KẾT THÚC (status = 2) - chỉ phân ca đã kết thúc mới được giao ca
    phanCaList.value = data.filter(pc => pc.trangThai === 2)
    
    // Nếu có query params từ GiaoCaPage, tự động chọn phân ca
    const phanCaIdParam = route.query.phanCaId as string
    if (phanCaIdParam) {
      const phanCaId = parseInt(phanCaIdParam)
      const phanCa = phanCaList.value.find(pc => pc.id === phanCaId)
      if (phanCa) {
        formData.value.phanCaId = phanCaId.toString()
        onPhanCaChange()
      }
    }
  } catch (error) {
    console.error('Lỗi khi tải danh sách phân ca:', error)
    toastRef.value?.error('Lỗi', 'Không thể tải danh sách phân ca')
  }
}

async function onPhanCaChange() {
  const selectedPhanCa = phanCaList.value.find(pc => pc.id === parseInt(formData.value.phanCaId))
  if (selectedPhanCa) {
    formData.value.nhanVienGiaoId = selectedPhanCa.nhanVienId.toString()
    
    // Tự động tìm phân ca tiếp theo và prefill nhân viên nhận ca
    await loadNextShiftForHandover(selectedPhanCa.id)
    
    // Tự động lấy số tiền đầu ca từ giao ca trước
    await loadAutoTienDauCa(selectedPhanCa.id)
  } else {
    formData.value.nhanVienNhanId = ''
    formData.value.soTienDauCa = ''
  }
}

async function loadAutoTienDauCa(phanCaId: number) {
  try {
    console.log('[GiaoCaCreatePage] Đang tìm số tiền cuối ca từ giao ca trước cho phân ca:', phanCaId)
    
    // Tìm phân ca để lấy thông tin ca và ngày
    const selectedPhanCa = phanCaList.value.find(pc => pc.id === phanCaId)
    if (!selectedPhanCa || !selectedPhanCa.ca) {
      return
    }
    
    // Gọi API để lấy giao ca gần nhất đã xác nhận
    const { data: giaoCaList } = await api.get(`/api/giao-ca/recent?limit=5`)
    
    // Tìm giao ca có cùng caId và ngày, hoặc cùng nhân viên giao
    const lastGiaoCa = giaoCaList.find((gc: any) => {
      return gc.phanCa?.caId === selectedPhanCa.caId && 
             gc.trangThai === 1 && // Đã xác nhận
             gc.soTienCuoiCa && 
             gc.soTienCuoiCa > 0
    })
    
    if (lastGiaoCa && lastGiaoCa.soTienCuoiCa) {
      formData.value.soTienDauCa = lastGiaoCa.soTienCuoiCa.toString()
      console.log('[GiaoCaCreatePage] ✅ Tự động lấy số tiền đầu ca:', formData.value.soTienDauCa)
      toastRef.value?.info('Thông tin', `Đã tự động lấy số tiền đầu ca: ${formatCurrency(lastGiaoCa.soTienCuoiCa)}`)
    } else {
      console.log('[GiaoCaCreatePage] ⚠️ Không tìm thấy giao ca trước để lấy số tiền đầu ca')
      // Để trống, backend sẽ tự động lấy hoặc để mặc định
    }
  } catch (error: any) {
    console.log('[GiaoCaCreatePage] Không thể tự động lấy số tiền đầu ca:', error)
    // Không báo lỗi, để backend tự xử lý
  }
}

function formatCurrency(amount: number) {
  if (!amount) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount)
}

function formatTime(time: string | null | undefined): string {
  if (!time) return 'N/A'
  try {
    // Format từ "HH:mm:ss" hoặc "HH:mm" thành "HH:mm"
    const timeStr = time.toString()
    const parts = timeStr.split(':')
    if (parts.length >= 2) {
      return `${parts[0]}:${parts[1]}`
    }
    return timeStr
  } catch {
    return time?.toString() || 'N/A'
  }
}

async function loadNextShiftForHandover(phanCaId: number) {
  loadingNextShift.value = true
  nextShiftEmployeeId.value = null
  nextShiftEmployeeInfo.value = ''
  formData.value.nhanVienNhanId = ''
  
  try {
    console.log('[GiaoCaCreatePage] Đang tìm phân ca tiếp theo sau phân ca:', phanCaId)
    const { data: nextPhanCa } = await api.get(`/api/phan-ca/next-shift?phanCaId=${phanCaId}`)
    
    if (nextPhanCa && nextPhanCa.nhanVienId) {
      // Kiểm tra lại: đảm bảo ca này là ca sắp tới (ngày >= hôm nay và chưa bắt đầu)
      const today = new Date().toISOString().split('T')[0]
      const shiftDate = nextPhanCa.ngayLamViec
      const shiftStatus = nextPhanCa.trangThai
      
      console.log('[GiaoCaCreatePage] Thông tin phân ca tiếp theo:', {
        id: nextPhanCa.id,
        nhanVienId: nextPhanCa.nhanVienId,
        nhanVienTen: nextPhanCa.nhanVien?.hoTen,
        ngayLamViec: shiftDate,
        trangThai: shiftStatus,
        caTen: nextPhanCa.ca?.tenCa,
        gioBatDau: nextPhanCa.ca?.gioBatDau,
        gioKetThuc: nextPhanCa.ca?.gioKetThuc,
        today: today
      })
      
      // Validate: chỉ chấp nhận ca có ngày >= hôm nay và trangThai = 0
      if (shiftDate && shiftDate < today) {
        console.warn('[GiaoCaCreatePage] ⚠️ Ca tiếp theo có ngày làm việc (' + shiftDate + ') < hôm nay (' + today + ') - Bỏ qua')
        nextShiftEmployeeId.value = null
        nextShiftEmployeeInfo.value = ''
        formData.value.nhanVienNhanId = ''
        return
      }
      
      if (shiftStatus !== null && shiftStatus !== 0) {
        console.warn('[GiaoCaCreatePage] ⚠️ Ca tiếp theo có trangThai = ' + shiftStatus + ' (không phải 0 - chưa bắt đầu) - Bỏ qua')
        nextShiftEmployeeId.value = null
        nextShiftEmployeeInfo.value = ''
        formData.value.nhanVienNhanId = ''
        return
      }
      
      nextShiftEmployeeId.value = nextPhanCa.nhanVienId
      formData.value.nhanVienNhanId = nextPhanCa.nhanVienId.toString()
      
      const employeeName = nextPhanCa.nhanVien?.hoTen || 'N/A'
      const gioBatDau = formatTime(nextPhanCa.ca?.gioBatDau)
      const gioKetThuc = formatTime(nextPhanCa.ca?.gioKetThuc)
      
      // Hiển thị: "Tên nhân viên (Giờ bắt đầu - Giờ kết thúc)"
      // Ví dụ: "Nguyễn Thị Thu (07:00 - 12:00)"
      nextShiftEmployeeInfo.value = `${employeeName} (${gioBatDau} - ${gioKetThuc})`
      
      console.log('[GiaoCaCreatePage] ✅ Tìm thấy nhân viên nhận ca hợp lệ:', {
        id: nextPhanCa.nhanVienId,
        hoTen: employeeName,
        ngayLamViec: shiftDate,
        trangThai: shiftStatus,
        gioBatDau,
        gioKetThuc
      })
    } else {
      nextShiftEmployeeId.value = null
      formData.value.nhanVienNhanId = ''
      nextShiftEmployeeInfo.value = 'Không tìm thấy ca tiếp theo'
      console.log('[GiaoCaCreatePage] ⚠️ Không tìm thấy phân ca tiếp theo')
    }
  } catch (error: any) {
    nextShiftEmployeeId.value = null
    formData.value.nhanVienNhanId = ''
    nextShiftEmployeeInfo.value = 'Không tìm thấy ca tiếp theo'
    console.log('[GiaoCaCreatePage] Không tìm thấy phân ca tiếp theo:', error)
  } finally {
    loadingNextShift.value = false
  }
}

function onNhanVienNhanChange() {
  // Logic để validate hoặc hiển thị thông tin về ca của nhân viên nhận (nếu cần)
  if (formData.value.nhanVienNhanId) {
    console.log('[GiaoCaCreatePage] Đã chọn nhân viên nhận ca:', formData.value.nhanVienNhanId)
  }
}

function goBack() {
  router.push('/giao-ca')
}

function handleSubmit() {
  if (!isFormValid.value) {
    toastRef.value?.error('Lỗi', 'Vui lòng điền đầy đủ thông tin bắt buộc')
    return
  }

  if (isEdit.value) {
    confirmTitle.value = 'Xác nhận cập nhật giao ca'
    confirmMessage.value = 'Bạn có chắc chắn muốn cập nhật giao ca này?'
    pendingAction.value = () => updateGiaoCa()
  } else {
    confirmTitle.value = 'Xác nhận thêm giao ca'
    confirmMessage.value = 'Bạn có chắc chắn muốn thêm giao ca mới?'
    pendingAction.value = () => createGiaoCa()
  }
  showConfirmModal.value = true
}

async function loadGiaoCa() {
  if (!isEdit.value || !giaoCaId.value) return
  
  loading.value = true
  try {
    const { data } = await api.get(`/api/giao-ca/${giaoCaId.value}`)
    
    // Kiểm tra trạng thái - chỉ cho phép sửa nếu chưa xác nhận (trangThai = 0)
    if (data.trangThai !== 0) {
      toastRef.value?.error('Lỗi', 'Chỉ có thể sửa giao ca chưa được xác nhận')
      goBack()
      return
    }
    
    // Populate form data
    formData.value.phanCaId = data.phanCaId?.toString() || ''
    formData.value.nhanVienGiaoId = data.nhanVienGiaoId?.toString() || ''
    formData.value.nhanVienNhanId = data.nhanVienNhanId?.toString() || ''
    formData.value.soTienDauCa = data.soTienDauCa?.toString() || ''
    formData.value.soTienCuoiCa = data.soTienCuoiCa?.toString() || ''
    formData.value.soTienThuThem = data.soTienThuThem?.toString() || ''
    formData.value.soTienChiRa = data.soTienChiRa?.toString() || ''
    formData.value.baoCaoCongViec = data.baoCaoCongViec || ''
    formData.value.suCoBatThuong = data.suCoBatThuong || ''
    formData.value.congViecTonDong = data.congViecTonDong || ''
    formData.value.ghiChu = data.ghiChu || ''
    
    // Load phân ca info nếu có
    if (data.phanCaId) {
      await loadPhanCa()
      // Load next shift info
      await loadNextShiftForHandover(data.phanCaId)
    }
  } catch (err: any) {
    console.error('Lỗi khi tải thông tin giao ca:', err)
    toastRef.value?.error('Lỗi', 'Không thể tải thông tin giao ca')
    goBack()
  } finally {
    loading.value = false
  }
}

async function createGiaoCa() {
  loading.value = true
  try {
    // Tính chênh lệch
    const dauCa = parseFloat(formData.value.soTienDauCa) || 0
    const cuoiCa = parseFloat(formData.value.soTienCuoiCa) || 0
    const thuThem = parseFloat(formData.value.soTienThuThem) || 0
    const chiRa = parseFloat(formData.value.soTienChiRa) || 0
    const chenhLech = cuoiCa - dauCa - thuThem + chiRa

    // Dùng API POST /api/giao-ca với DTO đầy đủ thay vì /api/giao-ca/create
    // Tự động generate maGiaoCa và ngayGiaoCa
    const now = new Date()
    const maGiaoCa = `GC${now.getTime()}`
    const ngayGiaoCa = now.toISOString()

    const payload: any = {
      maGiaoCa: maGiaoCa,
      phanCaId: parseInt(formData.value.phanCaId),
      nhanVienGiaoId: parseInt(formData.value.nhanVienGiaoId),
      nhanVienNhanId: formData.value.nhanVienNhanId ? parseInt(formData.value.nhanVienNhanId) : null,
      ngayGiaoCa: ngayGiaoCa,
      soTienDauCa: formData.value.soTienDauCa ? parseFloat(formData.value.soTienDauCa) : 0,
      soTienCuoiCa: parseFloat(formData.value.soTienCuoiCa),
      soTienThuThem: formData.value.soTienThuThem ? parseFloat(formData.value.soTienThuThem) : 0,
      soTienChiRa: formData.value.soTienChiRa ? parseFloat(formData.value.soTienChiRa) : 0,
      chenhLech: chenhLech,
      ghiChu: formData.value.ghiChu || null,
      baoCaoCongViec: formData.value.baoCaoCongViec || null,
      suCoBatThuong: formData.value.suCoBatThuong || null,
      congViecTonDong: formData.value.congViecTonDong || null,
      trangThai: 0 // Chờ xác nhận
    }

    console.log('[GiaoCaCreatePage] Gửi dữ liệu tạo giao ca:', payload)

    const response = await api.post('/api/giao-ca', payload)
    console.log('[GiaoCaCreatePage] Tạo giao ca thành công:', response.data)
    toastRef.value?.success('Thành công', 'Đã thêm giao ca mới')
    goBack()
  } catch (err: any) {
    console.error('[GiaoCaCreatePage] Lỗi khi tạo giao ca:', err)
    const errorMessage = err.response?.data?.message || 
                         err.response?.data?.error || 
                         err.message || 
                         'Không thể tạo giao ca'
    toastRef.value?.error('Lỗi', errorMessage)
  } finally {
    loading.value = false
    showConfirmModal.value = false
    pendingAction.value = null
  }
}

async function updateGiaoCa() {
  if (!giaoCaId.value) return
  
  loading.value = true
  try {
    // Tính chênh lệch
    const dauCa = parseFloat(formData.value.soTienDauCa) || 0
    const cuoiCa = parseFloat(formData.value.soTienCuoiCa) || 0
    const thuThem = parseFloat(formData.value.soTienThuThem) || 0
    const chiRa = parseFloat(formData.value.soTienChiRa) || 0
    const chenhLech = cuoiCa - dauCa - thuThem + chiRa

    const payload: any = {
      nhanVienNhanId: formData.value.nhanVienNhanId ? parseInt(formData.value.nhanVienNhanId) : null,
      soTienCuoiCa: parseFloat(formData.value.soTienCuoiCa),
      soTienThuThem: formData.value.soTienThuThem ? parseFloat(formData.value.soTienThuThem) : 0,
      soTienChiRa: formData.value.soTienChiRa ? parseFloat(formData.value.soTienChiRa) : 0,
      baoCaoCongViec: formData.value.baoCaoCongViec || null,
      suCoBatThuong: formData.value.suCoBatThuong || null,
      congViecTonDong: formData.value.congViecTonDong || null,
      ghiChu: formData.value.ghiChu || null
    }

    console.log('[GiaoCaCreatePage] Gửi dữ liệu cập nhật giao ca:', payload)

    const response = await api.put(`/api/giao-ca/${giaoCaId.value}`, payload)
    console.log('[GiaoCaCreatePage] Cập nhật giao ca thành công:', response.data)
    toastRef.value?.success('Thành công', 'Đã cập nhật giao ca')
    goBack()
  } catch (err: any) {
    console.error('[GiaoCaCreatePage] Lỗi khi cập nhật giao ca:', err)
    const errorMessage = err.response?.data?.message || 
                         err.response?.data?.error || 
                         err.message || 
                         'Không thể cập nhật giao ca'
    toastRef.value?.error('Lỗi', errorMessage)
  } finally {
    loading.value = false
    showConfirmModal.value = false
    pendingAction.value = null
  }
}

function handleConfirm() {
  if (pendingAction.value) {
    pendingAction.value()
    pendingAction.value = null
  }
  showConfirmModal.value = false
}

function handleCancel() {
  pendingAction.value = null
  showConfirmModal.value = false
}

onMounted(async () => {
  await Promise.all([loadNhanVien(), loadPhanCa()])
  
  // Nếu đang edit, load thông tin giao ca
  if (isEdit.value) {
    await loadGiaoCa()
  } else {
    // Tự động điền thông tin từ query params (khi truy cập từ GiaoCaPage)
    const phanCaIdParam = route.query.phanCaId as string
    const nhanVienGiaoIdParam = route.query.nhanVienGiaoId as string
    
    if (phanCaIdParam) {
      formData.value.phanCaId = phanCaIdParam
      onPhanCaChange()
    }
    
    if (nhanVienGiaoIdParam) {
      formData.value.nhanVienGiaoId = nhanVienGiaoIdParam
    }
  }
})
</script>

<style scoped>
.giao-ca-create-page {
  background: #f8fafc;
  min-height: 100vh;
  padding: 0;
  padding-top: 80px;
}

.edit-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.edit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
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
  padding: 10px 20px;
  background: white;
  color: #374151;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.btn-back:hover {
  background: #f3f4f6;
  border-color: #9ca3af;
}

.edit-header h1 {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.edit-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  padding: 32px;
}

.edit-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-section h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
  padding-bottom: 12px;
  border-bottom: 2px solid #f1f5f9;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group:has(textarea) {
  grid-column: 1 / -1;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #374151;
  font-size: 14px;
}

.form-label.required::after {
  content: '*';
  color: #dc2626;
  margin-left: 4px;
}

.form-input,
.form-select,
.form-textarea {
  padding: 12px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.2s ease;
  background: #fafbfc;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: #f97316;
  background: white;
  box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.1);
}

.form-textarea {
  resize: vertical;
  min-height: 100px;
  font-family: inherit;
}

.form-help {
  font-size: 12px;
  color: #64748b;
  margin-top: -4px;
}

.info-notice {
  background: #e0f2fe;
  border: 1px solid #0ea5e9;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 20px;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  color: #0c4a6e;
}

.info-notice svg {
  font-size: 20px;
  color: #0ea5e9;
  margin-top: 2px;
  flex-shrink: 0;
}

.info-notice strong {
  display: block;
  font-size: 15px;
  margin-bottom: 5px;
}

.info-notice p {
  margin: 0;
  font-size: 14px;
  line-height: 1.5;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 24px;
  border-top: 1px solid #e5e7eb;
}

.btn-cancel,
.btn-save {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-cancel {
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
}

.btn-cancel:hover:not(:disabled) {
  background: #e5e7eb;
}

.btn-save {
  background: linear-gradient(135deg, #f97316, #ea580c);
  color: white;
  box-shadow: 0 2px 4px rgba(249, 115, 22, 0.3);
}

.btn-save:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.4);
}

.btn-cancel:disabled,
.btn-save:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f4f6;
  border-top-color: #f97316;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
  
  .edit-container {
    padding: 16px;
  }
  
  .edit-content {
    padding: 20px;
  }
}
</style>

