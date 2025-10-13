<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/services/api'
import * as XLSX from 'xlsx'
import ConfirmModal from '@/components/ConfirmModal.vue'
import Toast from '@/components/Toast.vue'

interface DanhMuc { id: number; tenDanhMuc: string }
interface Hang { id: number; ten: string }
interface Ram { id: number; tenRam: string }
interface Rom { id: number; dungLuong: string }
interface MauSac { id: number; tenMau: string; maMau?: string }
interface HeDieuHanh { id: number; tenHeDieuHanh: string }
interface ManHinh { id: number; kichThuoc: string; doPhanGiai?: string }
interface CameraTruoc { id: number; thongSo: string }
interface CameraSau { id: number; thongSo: string }
interface Pin { id: number; dungLuongPin: string }
interface Chip { id: number; tenChip: string }
interface Cpu { id: number; tenCpu: string }
interface Gpu { id: number; tenGpu: string }
interface Sim { id: number; loaiSim: string }

const route = useRoute()
const router = useRouter()
const id = computed(() => route.params.id ? Number(route.params.id) : null)

const danhMucs = ref<DanhMuc[]>([])
const hangs = ref<Hang[]>([])
const rams = ref<Ram[]>([])
const roms = ref<Rom[]>([])
const mauSacs = ref<MauSac[]>([])
const heDieuHanhs = ref<HeDieuHanh[]>([])
const manHinhs = ref<ManHinh[]>([])
const cameraTruocs = ref<CameraTruoc[]>([])
const cameraSaus = ref<CameraSau[]>([])
const pins = ref<Pin[]>([])
const chips = ref<Chip[]>([])
const cpus = ref<Cpu[]>([])
const gpus = ref<Gpu[]>([])
const sims = ref<Sim[]>([])

// Image upload state
const variantImages = ref<{[key: number]: File[]}>({})
const colorImages = ref<{[key: number]: string[]}>({}) // Images grouped by color ID

// Modal state
const showModal = ref(false)
const modalType = ref('')
const modalTitle = ref('')
const modalInputs = ref<{[key: string]: string}>({})

// Confirm modal state
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const pendingImeiAction = ref<(() => void) | null>(null)
const pendingAction = ref<(() => void) | null>(null)

// Toast ref
const toastRef = ref()

// IMEI Modal state
const showImeiModal = ref(false)
const currentVariantIndex = ref<number | null>(null)
const imeiInput = ref('')
const duplicateImeis = ref<string[]>([])
const modalLoading = ref(false)
const selectedFileName = ref('')
const excelFileInput = ref<HTMLInputElement | null>(null)

const form = ref({
  maSanPham: '',
  tenSanPham: '',
  idDanhMuc: null as number | null,
  idHang: null as number | null,
  moTa: '' as string,
  idHeDieuHanh: null as number | null,
  idManHinh: null as number | null,
  idCameraTruoc: null as number | null,
  idCameraSau: null as number | null,
  idSim: null as number | null,
  idPin: null as number | null,
  idChip: null as number | null,
  idGpu: null as number | null,
  idCpu: null as number | null,
})

type Variant = { 
  id?: number; // ID của chi tiết sản phẩm (khi edit)
  idRam: number | null; 
  idRom: number | null; 
  idMauSac: number | null; 
  soLuong: number; 
  donGia: number;
  giaNhap: number;
  ghiChu: string;
  // Thông tin form được gắn với phiên bản
  tenSanPham: string;
  idHang: number | null;
  idDanhMuc: number | null;
  moTa: string;
  idHeDieuHanh: number | null;
  idManHinh: number | null;
  idCameraTruoc: number | null;
  idCameraSau: number | null;
  idSim: number | null;
  idPin: number | null;
  idChip: number | null;
  imeis: string[];
  images: File[]; // Để hiển thị preview
  imageUrls: string[]; // Để lưu vào database 
  originalIndex?: number; // Index gốc trong array (khi edit)
}
const variantForm = ref({
  selectedRams: [] as number[],
  selectedRoms: [] as number[],
  selectedMauSacs: [] as number[]
})
const variants = ref<Variant[]>([])

// Multi-select state
const showRamDropdown = ref(false)
const showRomDropdown = ref(false)
const showMauSacDropdown = ref(false)

// Auto search for existing products
const searchResults = ref<any[]>([])
const showSearchResults = ref(false)
const searchTimeout = ref<NodeJS.Timeout | null>(null)


const isCreate = computed(() => id.value == null)
const canSaveCreate = computed(() => {
  // Chỉ cần có variants là có thể lưu, không cần kiểm tra form cơ bản
  const canSave = variants.value.length > 0
  return canSave
})
const canSaveUpdate = computed(() => !!form.value.tenSanPham && !!form.value.idHang && !!form.value.idDanhMuc)

function slugify(s: string) {
  return s.toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)/g, '')
}

function addVariant() {
  // Kiểm tra form cơ bản trước khi thêm phiên bản
  if (!form.value.tenSanPham) {
    toastRef.value?.warning('Thiếu thông tin', 'Vui lòng nhập tên sản phẩm trước khi thêm phiên bản')
    return
  }
  if (!form.value.idHang) {
    toastRef.value?.warning('Thiếu thông tin', 'Vui lòng chọn hãng trước khi thêm phiên bản')
    return
  }
  if (!form.value.idDanhMuc) {
    toastRef.value?.warning('Thiếu thông tin', 'Vui lòng chọn danh mục trước khi thêm phiên bản')
    return
  }
  
  if (variantForm.value.selectedRams.length === 0 || variantForm.value.selectedRoms.length === 0 || variantForm.value.selectedMauSacs.length === 0) {
    toastRef.value?.warning('Thiếu thông tin', 'Vui lòng chọn ít nhất một RAM, ROM và Màu sắc')
    return
  }
  
  // Generate all combinations và gắn thông tin form với mỗi phiên bản
  for (const ramId of variantForm.value.selectedRams) {
    for (const romId of variantForm.value.selectedRoms) {
      for (const mauSacId of variantForm.value.selectedMauSacs) {
        variants.value.push({
          idRam: ramId,
          idRom: romId,
          idMauSac: mauSacId,
          soLuong: 0,  // Số lượng sẽ được tính từ IMEI
          donGia: 0,   // Mặc định đơn giá = 0
          giaNhap: 0,  // Mặc định giá nhập = 0
          ghiChu: '',  // Mặc định ghi chú = rỗng
          // Gắn thông tin form với phiên bản
          tenSanPham: form.value.tenSanPham,
          idHang: form.value.idHang,
          idDanhMuc: form.value.idDanhMuc,
          moTa: form.value.moTa,
          idHeDieuHanh: form.value.idHeDieuHanh,
          idManHinh: form.value.idManHinh,
          idCameraTruoc: form.value.idCameraTruoc,
          idCameraSau: form.value.idCameraSau,
          idSim: form.value.idSim,
          idPin: form.value.idPin,
          idChip: form.value.idChip,
          imeis: [], 
          images: [],
          imageUrls: [] 
        })
      }
    }
  }
  
  // Không reset form để giữ lại các lựa chọn đã chọn
  // variantForm.value = {
  //   selectedRams: [],
  //   selectedRoms: [],
  //   selectedMauSacs: []
  // }
}
function removeVariant(i: number) { variants.value.splice(i, 1) }

function clearVariantSelections() {
  variantForm.value = {
    selectedRams: [],
    selectedRoms: [],
    selectedMauSacs: []
  }
  toastRef.value?.success('Thành công', 'Đã xóa tất cả lựa chọn RAM, ROM và màu sắc')
}

// Multi-select helper functions
function toggleRam(ramId: number) {
  const index = variantForm.value.selectedRams.indexOf(ramId)
  if (index > -1) {
    variantForm.value.selectedRams.splice(index, 1)
  } else {
    variantForm.value.selectedRams.push(ramId)
  }
}

function toggleRom(romId: number) {
  const index = variantForm.value.selectedRoms.indexOf(romId)
  if (index > -1) {
    variantForm.value.selectedRoms.splice(index, 1)
  } else {
    variantForm.value.selectedRoms.push(romId)
  }
}

function toggleMauSac(mauSacId: number) {
  const index = variantForm.value.selectedMauSacs.indexOf(mauSacId)
  if (index > -1) {
    variantForm.value.selectedMauSacs.splice(index, 1)
  } else {
    variantForm.value.selectedMauSacs.push(mauSacId)
  }
}

function isRamSelected(ramId: number) {
  return variantForm.value.selectedRams.includes(ramId)
}

function isRomSelected(romId: number) {
  return variantForm.value.selectedRoms.includes(romId)
}

function isMauSacSelected(mauSacId: number) {
  return variantForm.value.selectedMauSacs.includes(mauSacId)
}

function getSelectedRamsText() {
  if (variantForm.value.selectedRams.length === 0) return 'Chọn RAM'
  return variantForm.value.selectedRams.map(id => getAttributeName(id, rams.value, 'tenRam')).join(', ')
}

function getSelectedRomsText() {
  if (variantForm.value.selectedRoms.length === 0) return 'Chọn ROM'
  return variantForm.value.selectedRoms.map(id => getAttributeName(id, roms.value, 'dungLuong')).join(', ')
}

function getSelectedMauSacsText() {
  if (variantForm.value.selectedMauSacs.length === 0) return 'Chọn Màu sắc'
  return variantForm.value.selectedMauSacs.map(id => getAttributeName(id, mauSacs.value, 'tenMau')).join(', ')
}

function openAddModal(type: string) {
  modalType.value = type
  modalInputs.value = {}
  
  // Khởi tạo các trường cần thiết cho từng loại
  const fieldConfigs: Record<string, string[]> = {
    'danh-muc': ['maDanhMuc', 'tenDanhMuc'],
    'he-dieu-hanh': ['maHeDieuHanh', 'tenHeDieuHanh', 'moTa'],
    'man-hinh': ['maManHinh', 'kichThuoc', 'congNghe', 'doPhanGiai', 'tanSoQuet', 'kieuManHinh', 'moTa'],
    'hang': ['ten', 'xuatXu', 'moTa'],
    'camera-truoc': ['maCamera', 'thongSo', 'moTa'],
    'camera-sau': ['maCamera', 'thongSo', 'moTa'],
    'pin': ['maPin', 'dungLuongPin', 'congNgheSac', 'moTa'],
    'chip': ['maChip', 'tenChip', 'moTa'],
    'cpu': ['maCpu', 'tenCpu', 'moTa'],
    'gpu': ['maGpu', 'tenGpu', 'moTa'],
    'ram': ['maRam', 'tenRam', 'moTa'],
    'rom': ['maRom', 'dungLuong', 'moTa'],
    'mau-sac': ['maMau', 'tenMau', 'moTa'],
    'sim': ['loaiSim', 'moTa']
  }
  
  // Khởi tạo các trường với giá trị rỗng
  const fields = fieldConfigs[type] || ['ten']
  fields.forEach(field => {
    modalInputs.value[field] = ''
  })
  
  const titles: Record<string, string> = {
    'danh-muc': 'Thêm Mới Danh Mục',
    'he-dieu-hanh': 'Thêm Mới Hệ Điều Hành',
    'man-hinh': 'Thêm Mới Màn Hình', 
    'hang': 'Thêm Mới Hãng',
    'camera-truoc': 'Thêm Mới Camera Trước',
    'camera-sau': 'Thêm Mới Camera Sau',
    'pin': 'Thêm Mới Pin',
    'chip': 'Thêm Mới Chip',
    'cpu': 'Thêm Mới CPU',
    'gpu': 'Thêm Mới GPU',
    'ram': 'Thêm Mới RAM',
    'rom': 'Thêm Mới ROM',
    'mau-sac': 'Thêm Mới Màu Sắc',
    'sim': 'Thêm Mới Sim',
  }
  
  modalTitle.value = titles[type] || 'Thêm Mới'
  showModal.value = true
}

async function saveModal() {
  // Kiểm tra các trường bắt buộc
  const requiredFields = getRequiredFields(modalType.value)
  for (const field of requiredFields) {
    if (!modalInputs.value[field]?.trim()) {
      toastRef.value?.warning('Thiếu thông tin', `Vui lòng nhập ${getFieldLabel(field)}`)
      return
    }
  }
  
  modalLoading.value = true
  try {
    let payload: any = {}
    let endpoint = ''
    
    switch (modalType.value) {
      case 'danh-muc':
        endpoint = '/api/danh-muc'
        payload = { 
          maDanhMuc: modalInputs.value.maDanhMuc?.trim(),
          tenDanhMuc: modalInputs.value.tenDanhMuc?.trim(),
          trangThai: 1
        }
        break
      case 'he-dieu-hanh':
        endpoint = '/api/he-dieu-hanh'
        payload = { 
          maHeDieuHanh: modalInputs.value.maHeDieuHanh?.trim(),
          tenHeDieuHanh: modalInputs.value.tenHeDieuHanh?.trim(),
          moTa: modalInputs.value.moTa?.trim(),
          trangThai: 1
        }
        break
      case 'man-hinh':
        endpoint = '/api/man-hinh'
        payload = { 
          maManHinh: modalInputs.value.maManHinh?.trim(),
          kichThuoc: modalInputs.value.kichThuoc?.trim(),
          congNghe: modalInputs.value.congNghe?.trim(),
          doPhanGiai: modalInputs.value.doPhanGiai?.trim(),
          tanSoQuet: modalInputs.value.tanSoQuet?.trim(),
          kieuManHinh: modalInputs.value.kieuManHinh?.trim(),
          moTa: modalInputs.value.moTa?.trim(),
          trangThai: 1
        }
        break
      case 'hang':
        endpoint = '/api/hang'
        payload = { 
          ten: modalInputs.value.ten?.trim(),
          xuatXu: modalInputs.value.xuatXu?.trim(),
          moTa: modalInputs.value.moTa?.trim(),
          trangThai: 1
        }
        break
      case 'camera-truoc':
        endpoint = '/api/camera-truoc'
        payload = { 
          maCamera: modalInputs.value.maCamera?.trim(),
          thongSo: modalInputs.value.thongSo?.trim(),
          moTa: modalInputs.value.moTa?.trim(),
          trangThai: 1
        }
        break
      case 'camera-sau':
        endpoint = '/api/camera-sau'
        payload = { 
          maCamera: modalInputs.value.maCamera?.trim(),
          thongSo: modalInputs.value.thongSo?.trim(),
          moTa: modalInputs.value.moTa?.trim(),
          trangThai: 1
        }
        break
      case 'pin':
        endpoint = '/api/pin'
        payload = { 
          maPin: modalInputs.value.maPin?.trim(),
          dungLuongPin: modalInputs.value.dungLuongPin?.trim(),
          congNgheSac: modalInputs.value.congNgheSac?.trim(),
          moTa: modalInputs.value.moTa?.trim(),
          trangThai: 1
        }
        break
      case 'chip':
        endpoint = '/api/chip'
        payload = { 
          maChip: modalInputs.value.maChip?.trim(),
          tenChip: modalInputs.value.tenChip?.trim(),
          moTa: modalInputs.value.moTa?.trim(),
          trangThai: 1
        }
        break
      case 'cpu':
        endpoint = '/api/cpu'
        payload = { 
          maCpu: modalInputs.value.maCpu?.trim(),
          tenCpu: modalInputs.value.tenCpu?.trim(),
          moTa: modalInputs.value.moTa?.trim(),
          trangThai: 1
        }
        break
      case 'gpu':
        endpoint = '/api/gpu'
        payload = { 
          maGpu: modalInputs.value.maGpu?.trim(),
          tenGpu: modalInputs.value.tenGpu?.trim(),
          moTa: modalInputs.value.moTa?.trim(),
          trangThai: 1
        }
        break
      case 'ram':
        endpoint = '/api/ram'
        payload = { 
          maRam: modalInputs.value.maRam?.trim(),
          tenRam: modalInputs.value.tenRam?.trim(),
          moTa: modalInputs.value.moTa?.trim(),
          trangThai: 1
        }
        break
      case 'rom':
        endpoint = '/api/rom'
        payload = { 
          maRom: modalInputs.value.maRom?.trim(),
          dungLuong: modalInputs.value.dungLuong?.trim(),
          moTa: modalInputs.value.moTa?.trim(),
          trangThai: 1
        }
        break
      case 'mau-sac':
        endpoint = '/api/mau-sac'
        payload = { 
          maMau: modalInputs.value.maMau?.trim(),
          tenMau: modalInputs.value.tenMau?.trim(),
          moTa: modalInputs.value.moTa?.trim(),
          trangThai: 1
        }
        break
      case 'sim':
        endpoint = '/api/sim'
        payload = { 
          loaiSim: modalInputs.value.loaiSim?.trim(),
          moTa: modalInputs.value.moTa?.trim(),
          trangThai: 1
        }
        break
    }
    
    const response = await api.post(endpoint, payload)
    
    // Reload data to update dropdowns
    await loadLookups()
    
    showModal.value = false
    modalInputs.value = {}
    
    // Show success message
    toastRef.value?.success('Thành công', 'Thêm mới thành công!')
    
  } catch (error: any) {
    console.error('Lỗi khi thêm:', error)
    
    // Show user-friendly error message
    let errorMessage = 'Có lỗi xảy ra khi thêm mới'
    if (error.response?.status === 500) {
      errorMessage = 'Lỗi server. Vui lòng thử lại sau.'
    } else if (error.response?.status === 400) {
      errorMessage = 'Dữ liệu không hợp lệ. Vui lòng kiểm tra lại.'
    } else if (error.code === 'ERR_CONNECTION_REFUSED') {
      errorMessage = 'Không thể kết nối đến server. Vui lòng kiểm tra kết nối.'
    }
    
    toastRef.value?.error('Lỗi', errorMessage)
  } finally {
    modalLoading.value = false
  }
}

function closeModal() {
  showModal.value = false
  modalInputs.value = {}
}

// Confirm modal handlers
function handleConfirm() {
  if (pendingAction.value) {
    pendingAction.value()
    pendingAction.value = null
  }
  if (pendingImeiAction.value) {
    pendingImeiAction.value()
    pendingImeiAction.value = null
  }
  showConfirmModal.value = false
}

function handleCancel() {
  showConfirmModal.value = false
  pendingAction.value = null
  pendingImeiAction.value = null
}

function getRequiredFields(type: string): string[] {
  const requiredFields: Record<string, string[]> = {
    'danh-muc': ['maDanhMuc', 'tenDanhMuc'],
    'he-dieu-hanh': ['maHeDieuHanh', 'tenHeDieuHanh'],
    'man-hinh': ['maManHinh', 'kichThuoc', 'congNghe'],
    'hang': ['ten'],
    'camera-truoc': ['maCamera', 'thongSo'],
    'camera-sau': ['maCamera', 'thongSo'],
    'pin': ['maPin', 'dungLuongPin'],
    'chip': ['maChip', 'tenChip'],
    'cpu': ['maCpu', 'tenCpu'],
    'gpu': ['maGpu', 'tenGpu'],
    'ram': ['maRam', 'tenRam'],
    'rom': ['maRom', 'dungLuong'],
    'mau-sac': ['maMau', 'tenMau'],
    'sim': ['loaiSim'],
  }
  return requiredFields[type] || ['ten']
}

function getFieldLabel(field: string): string {
  const labels: Record<string, string> = {
    'maDanhMuc': 'Mã danh mục',
    'tenDanhMuc': 'Tên danh mục',
    'maHeDieuHanh': 'Mã hệ điều hành',
    'tenHeDieuHanh': 'Tên hệ điều hành',
    'moTa': 'Mô tả',
    'maManHinh': 'Mã màn hình',
    'kichThuoc': 'Kích thước',
    'congNghe': 'Công nghệ',
    'doPhanGiai': 'Độ phân giải',
    'tanSoQuet': 'Tần số quét',
    'kieuManHinh': 'Kiểu màn hình',
    'ten': 'Tên',
    'xuatXu': 'Xuất xứ',
    'maCamera': 'Mã camera',
    'thongSo': 'Thông số',
    'maPin': 'Mã pin',
    'dungLuongPin': 'Dung lượng pin',
    'congNgheSac': 'Công nghệ sạc',
    'maChip': 'Mã chip',
    'tenChip': 'Tên chip',
    'maCpu': 'Mã CPU',
    'tenCpu': 'Tên CPU',
    'maGpu': 'Mã GPU',
    'tenGpu': 'Tên GPU',
    'maRam': 'Mã RAM',
    'tenRam': 'Tên RAM',
    'maRom': 'Mã ROM',
    'dungLuong': 'Dung lượng',
    'maMau': 'Mã màu',
    'tenMau': 'Tên màu',
    'loaiSim': 'Loại sim',
  }
  return labels[field] || field
}

function isModalValid(): boolean {
  const requiredFields = getRequiredFields(modalType.value)
  return requiredFields.every(field => modalInputs.value[field]?.trim())
}

function getModalLabel() {
  const labels: Record<string, string> = {
    'danh-muc': 'Tên danh mục',
    'he-dieu-hanh': 'Tên hệ điều hành',
    'man-hinh': 'Kích thước màn hình', 
    'hang': 'Tên hãng',
    'camera-truoc': 'Thông số camera trước',
    'camera-sau': 'Thông số camera sau',
    'pin': 'Dung lượng pin',
    'chip': 'Tên chip',
    'ram': 'Tên RAM',
    'rom': 'Dung lượng ROM',
    'mau-sac': 'Tên màu sắc'
  }
  
  return labels[modalType.value] || 'Tên'
}

// Group variants by RAM/ROM combination
const groupedVariants = computed(() => {
  const groups: Record<string, any> = {}
  
  variants.value.forEach((variant, index) => {
    const ramName = getAttributeName(variant.idRam, rams.value, 'tenRam')
    const romName = getAttributeName(variant.idRom, roms.value, 'dungLuong')
    const key = `${variant.idRam}-${variant.idRom}`
    
    if (!groups[key]) {
      groups[key] = {
        key,
        ramName,
        romName,
        variants: [],
        searchValue: '',
        priceInput: ''
      }
    }
    
    groups[key].variants.push({ ...variant, originalIndex: index })
  })
  
  return Object.values(groups)
})

// Group variants by color for image management
const colorImageGroups = computed(() => {
  const groups: {[key: number]: any} = {}
  
  variants.value.forEach((variant) => {
    if (variant.idMauSac) {
      const colorName = getAttributeName(variant.idMauSac, mauSacs.value, 'tenMau')
      
      if (!groups[variant.idMauSac]) {
        groups[variant.idMauSac] = {
          colorId: variant.idMauSac,
          colorName,
          variants: [],
          images: colorImages.value[variant.idMauSac] || []
        }
      }
      
      groups[variant.idMauSac].variants.push(variant)
    }
  })
  
  return Object.values(groups)
})

function getColorCode(idMauSac: number | null) {
  if (!idMauSac) return '#ccc'
  
  // Tìm màu sắc trong danh sách đã load từ database
  const mauSac = mauSacs.value.find(m => m.id === idMauSac)
  if (mauSac && mauSac.maMau) {
    // Sử dụng mã màu từ database
    return mauSac.maMau.startsWith('#') ? mauSac.maMau : `#${mauSac.maMau}`
  }
  
  // Fallback về màu mặc định nếu không tìm thấy
  return '#ccc'
}

// Function to check if attribute is active
function isAttributeActive(attributeId: number | null, attributeList: any[]): boolean {
  if (!attributeId) return false
  return attributeList.some(attr => attr.id === attributeId)
}

// Function to get attribute name or "Chưa cập nhật"
function getAttributeName(attributeId: number | null, attributeList: any[], nameField: string = 'ten'): string {
  if (!attributeId) return 'Chưa cập nhật'
  
  const attribute = attributeList.find(attr => attr.id === attributeId)
  if (attribute) {
    return attribute[nameField] || 'Chưa cập nhật'
  }
  
  return 'Chưa cập nhật'
}

async function performDeleteVariant(variantIndex: number, variant: Variant, originalIndex: number) {
  // Chỉ xóa khỏi frontend array (UI) - KHÔNG xóa database ngay lập tức
  variants.value.splice(originalIndex, 1)
  
  // Hiển thị toast thông báo
  const ramName = getAttributeName(variant.idRam, rams.value, 'tenRam')
  const romName = getAttributeName(variant.idRom, roms.value, 'dungLuong')
  const mauName = getAttributeName(variant.idMauSac, mauSacs.value, 'tenMau')
  
  toastRef.value?.success(
    'Đã xóa khỏi danh sách',
    `Phiên bản ${ramName}/${romName} - ${mauName} sẽ được xóa dữ liệu khi bạn lưu cập nhật.`
  )
}

async function removeVariantFromGroup(groupKey: string, variantIndex: number) {
  const group = groupedVariants.value.find(g => g.key === groupKey)
  if (group) {
    const variant = group.variants[variantIndex]
    const originalIndex = variant.originalIndex
    
    // Xác nhận trước khi xóa
    const deleteMessage = variant.id ? 
      'Bạn có chắc chắn muốn xóa phiên bản này? Hành động này không thể hoàn tác.' :
      'Bạn có chắc chắn muốn xóa phiên bản này?'
    
    confirmTitle.value = 'Xác nhận xóa phiên bản'
    confirmMessage.value = deleteMessage
    pendingAction.value = () => performDeleteVariant(variantIndex, variant, originalIndex)
    showConfirmModal.value = true
  }
}

function refreshForm() {
  // Reset form data
  form.value = {
    maSanPham: '',
    tenSanPham: '',
    idDanhMuc: null,
    idHang: null,
    moTa: '',
    idHeDieuHanh: null,
    idManHinh: null,
    idCameraTruoc: null,
    idCameraSau: null,
    idSim: null,
    idPin: null,
    idChip: null,
    idGpu: null,
    idCpu: null,
  }
  
  // Reset variants and images
  variants.value = []
  variantImages.value = {}
  
  // Reset variant form selections
  variantForm.value = {
    selectedRams: [],
    selectedRoms: [],
    selectedMauSacs: []
  }
  
  // Clear all localStorage data
  clearAllProductLocalStorage()
  
  // Reset route to create mode
  if (router.currentRoute.value.name === 'san-pham-edit') {
    router.replace({ name: 'san-pham-create' })
  }
}

// Helper function to clear all product-related localStorage data
function clearAllProductLocalStorage() {
  // Remove specific keys
  localStorage.removeItem('sanpham-draft')
  localStorage.removeItem('product-draft')
  localStorage.removeItem('variant-draft')
  
  // Remove all keys that contain product-related terms
  const keysToRemove = []
  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i)
    if (key && (
      key.includes('sanpham') || 
      key.includes('product') || 
      key.includes('variant') ||
      key.includes('form') ||
      key.includes('draft')
    )) {
      keysToRemove.push(key)
    }
  }
  keysToRemove.forEach(key => localStorage.removeItem(key))
  
  console.log('Cleared localStorage keys:', keysToRemove)
}

// Image upload functions
async function handleImageUpload(variantIndex: number, event: Event) {
  const input = event.target as HTMLInputElement
  if (input.files && input.files.length > 0) {
    const file = input.files[0] // Chỉ lấy file đầu tiên
    
    // Validate file size (max 10MB per file)
    const maxSize = 10 * 1024 * 1024 // 10MB in bytes
    
    if (file.size > maxSize) {
      toastRef.value?.error('Lỗi', `File "${file.name}" vượt quá giới hạn 10MB`)
      return
    }
    
    // Initialize arrays if not exist
    if (!variantImages.value[variantIndex]) {
      variantImages.value[variantIndex] = []
    }
    if (!variants.value[variantIndex].imageUrls) {
      variants.value[variantIndex].imageUrls = []
    }
    
    // Clear existing images (chỉ cho phép 1 ảnh)
    variantImages.value[variantIndex] = []
    variants.value[variantIndex].imageUrls = []
    
    try {
      const formData = new FormData()
      formData.append('file', file)
      
      const response = await api.post('/api/upload/image', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
      
      if (response.data.url) {
        // Add to variantImages for display
        variantImages.value[variantIndex].push(file)
        // Add URL to variant for saving to database
        variants.value[variantIndex].imageUrls.push(response.data.url)
      }
    } catch (error) {
      console.error('Error uploading image:', error)
      if (error.response?.status === 413) {
        toastRef.value?.error('Lỗi', 'File quá lớn! Vui lòng chọn file nhỏ hơn 10MB.')
      } else {
        toastRef.value?.error('Lỗi', 'Lỗi upload ảnh: ' + (error.response?.data?.error || error.message))
      }
    }
    
    // Update variant with images
    if (variants.value[variantIndex]) {
      variants.value[variantIndex].images = variantImages.value[variantIndex]
    }
    
    // Clear input để có thể chọn lại file cùng tên
    input.value = ''
  }
}

function removeImage(variantIndex: number, imageIndex: number) {
  if (variantImages.value[variantIndex]) {
    variantImages.value[variantIndex].splice(imageIndex, 1)
    if (variants.value[variantIndex]) {
      variants.value[variantIndex].images = variantImages.value[variantIndex]
      // Also remove from imageUrls if exists
      if (variants.value[variantIndex].imageUrls) {
        variants.value[variantIndex].imageUrls.splice(imageIndex, 1)
      }
    }
  }
}

function removeSavedImage(variantIndex: number, imageIndex: number) {
  if (variants.value[variantIndex] && variants.value[variantIndex].imageUrls) {
    variants.value[variantIndex].imageUrls.splice(imageIndex, 1)
  }
}

async function updateVariantImeis(variant: Variant, variantIndex: number) {
  try {
    const chiTietId = variant.id
    
    if (!chiTietId) {
      console.warn('Phiên bản chưa có ID chi tiết sản phẩm, bỏ qua cập nhật IMEI:', variantIndex)
      return
    }
    
    // Thêm IMEI mới (sử dụng bulk upload)
    if (variant.imeis && variant.imeis.length > 0) {
      const imeiText = variant.imeis.join('\n')
      await api.post('/api/imei/bulk-upload', imeiText, {
        params: { chiTietId: chiTietId },
        headers: { 'Content-Type': 'text/plain' }
      })
    }
  } catch (error) {
    console.error('Lỗi khi cập nhật IMEI cho variant:', error)
  }
}

async function updateVariantImages(variant: Variant, variantIndex: number) {
  try {
    // Lấy ID của chi tiết sản phẩm từ variant
    const chiTietId = variant.id
    
    if (!chiTietId) {
      console.warn('Variant chưa có ID chi tiết sản phẩm, bỏ qua cập nhật hình ảnh:', variantIndex)
      return
    }
    
    // Xóa hình ảnh cũ
    await api.delete(`/api/san-pham/chi-tiet/${chiTietId}/images`)
    
    // Thêm hình ảnh mới
    for (const imageUrl of variant.imageUrls) {
      await api.post(`/api/san-pham/chi-tiet/${chiTietId}/images`, {
        urlAnh: imageUrl
      })
    }
  } catch (error) {
    console.error('Lỗi khi cập nhật hình ảnh cho variant:', error)
  }
}

function getVariantColorName(variant: Variant): string {
  return getAttributeName(variant.idMauSac, mauSacs.value, 'tenMau')
}

function getVariantFullName(variant: Variant): string {
  const ramName = getAttributeName(variant.idRam, rams.value, 'tenRam')
  const romName = getAttributeName(variant.idRom, roms.value, 'dungLuong')
  const mauName = getAttributeName(variant.idMauSac, mauSacs.value, 'tenMau')
  return `${ramName}/${romName} - ${mauName}`
}

function getVariantRamRomName(variant: Variant): string {
  const ramName = getAttributeName(variant.idRam, rams.value, 'tenRam')
  const romName = getAttributeName(variant.idRom, roms.value, 'dungLuong')
  return `${ramName}/${romName}`
}

function createImageUrl(file: File): string {
  return URL.createObjectURL(file)
}

function createFullImageUrl(url: string): string {
  // Nếu URL đã có protocol (Cloudinary hoặc local), trả về nguyên
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  // Nếu URL bắt đầu với /, thêm domain (local uploads)
  if (url.startsWith('/')) {
    return `http://localhost:8080${url}`
  }
  // Nếu không, thêm domain và / (local uploads)
  return `http://localhost:8080/${url}`
}

// Auto search functions
async function searchExistingProducts(query: string) {
  if (!query || query.length < 2) {
    searchResults.value = []
    showSearchResults.value = false
    return
  }

  try {
    const { data } = await api.get(`/api/san-pham?search=${encodeURIComponent(query)}`)
    
    // Lọc và sắp xếp kết quả theo độ tương đồng
    const filteredResults = data
      .filter((product: any) => 
        product.tenSanPham.toLowerCase().includes(query.toLowerCase())
      )
      .sort((a: any, b: any) => {
        // Sắp xếp theo độ tương đồng: bắt đầu với query trước
        const aStartsWith = a.tenSanPham.toLowerCase().startsWith(query.toLowerCase())
        const bStartsWith = b.tenSanPham.toLowerCase().startsWith(query.toLowerCase())
        
        if (aStartsWith && !bStartsWith) return -1
        if (!aStartsWith && bStartsWith) return 1
        
        // Nếu cùng loại, sắp xếp theo tên
        return a.tenSanPham.localeCompare(b.tenSanPham)
      })
      .slice(0, 8) // Tăng lên 8 kết quả để có nhiều lựa chọn hơn
    
    searchResults.value = filteredResults
    showSearchResults.value = filteredResults.length > 0
  } catch (error) {
    console.error('Lỗi khi tìm kiếm sản phẩm:', error)
    searchResults.value = []
    showSearchResults.value = false
  }
}

function onProductNameInput() {
  // Clear previous timeout
  if (searchTimeout.value) {
    clearTimeout(searchTimeout.value)
  }

  // Set new timeout for search
  searchTimeout.value = setTimeout(() => {
    if (form.value.tenSanPham && form.value.tenSanPham.length >= 2) {
      searchExistingProducts(form.value.tenSanPham)
    } else {
      searchResults.value = []
      showSearchResults.value = false
    }
  }, 300) // Giảm delay xuống 300ms để phản hồi nhanh hơn
}

function selectExistingProduct(product: any) {
  form.value.tenSanPham = product.tenSanPham
  showSearchResults.value = false
  searchResults.value = []
}

function hideSearchResults() {
  // Delay hiding to allow click on results
  setTimeout(() => {
    showSearchResults.value = false
  }, 200)
}

function highlightText(text: string, query: string): string {
  if (!query || !text) return text
  
  const regex = new RegExp(`(${query.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi')
  return text.replace(regex, '<mark>$1</mark>')
}

function triggerFileInput(index: number) {
  const input = document.getElementById(`imageUpload-${index}`) as HTMLInputElement
  if (input) {
    input.click()
  }
}

function clearDraftAndRefresh() {
  confirmTitle.value = 'Xác nhận làm mới form'
  confirmMessage.value = 'Bạn có chắc chắn muốn làm mới form? Các phiên bản đã chọn sẽ bị mất!'
  pendingAction.value = () => {
    // Use the centralized refresh function
    refreshForm()
    
    // Reset dropdown states
    showRamDropdown.value = false
    showRomDropdown.value = false
    showMauSacDropdown.value = false
    
    toastRef.value?.success('Thành công', 'Đã làm mới form thành công!')
  }
  showConfirmModal.value = true
}

async function save() {
  try {
    // Confirm before saving
    const action = isCreate.value ? 'tạo mới' : 'cập nhật'
    confirmTitle.value = `Xác nhận ${action} sản phẩm`
    confirmMessage.value = `Bạn có chắc chắn muốn ${action} sản phẩm này không?\n\nThông tin sẽ được lưu vào cơ sở dữ liệu.`
    pendingAction.value = () => performSave()
    showConfirmModal.value = true
  } catch (error) {
    console.error('Lỗi khi lưu sản phẩm:', error)
    toastRef.value?.error('Lỗi', 'Có lỗi xảy ra khi lưu sản phẩm: ' + (error.response?.data?.message || error.message))
  }
}

async function performSave() {
  try {
    
    // Validate required fields for create
    if (isCreate.value) {
      if (variants.value.length === 0) {
        toastRef.value?.warning('Thiếu thông tin', 'Vui lòng thêm ít nhất một phiên bản sản phẩm')
        return
      }
      
      // Lấy thông tin từ phiên bản đầu tiên để tạo sản phẩm chính
      const firstVariant = variants.value[0]
      
      // Tạo payload cho sản phẩm chính
      const payload = {
        maSanPham: form.value.maSanPham || slugify(firstVariant.tenSanPham),
        tenSanPham: firstVariant.tenSanPham,
        moTa: firstVariant.moTa || '',
        idDanhMuc: firstVariant.idDanhMuc,
        idHang: firstVariant.idHang,
        // Thêm tất cả các trường khác
        idManHinh: form.value.idManHinh,
        idCameraTruoc: form.value.idCameraTruoc,
        idCameraSau: form.value.idCameraSau,
        idChip: form.value.idChip,
        idGpu: form.value.idGpu,
        idSim: form.value.idSim,
        idHeDieuHanh: form.value.idHeDieuHanh,
        idCpu: form.value.idCpu,
        idPin: form.value.idPin,
        variants: variants.value.map(v => ({
          idRam: v.idRam,
          idRom: v.idRom,
          idMauSac: v.idMauSac,
          soLuong: v.imeis && v.imeis.length > 0 ? v.imeis.length : 0,
          donGia: Number(v.donGia) || 0,
          giaNhap: Number(v.giaNhap) || 0,
          ghiChu: v.ghiChu || '',
          imeis: v.imeis || [],
          imageUrls: v.imageUrls || [] 
        }))
      }
      
      // Validate payload trước khi gửi
      if (!payload.tenSanPham) {
        toastRef.value?.warning('Thiếu thông tin', 'Tên sản phẩm không được để trống')
        return
      }
      if (!payload.idDanhMuc) {
        toastRef.value?.warning('Thiếu thông tin', 'Danh mục không được để trống')
        return
      }
      if (!payload.idHang) {
        toastRef.value?.warning('Thiếu thông tin', 'Hãng không được để trống')
        return
      }
      if (payload.variants.length === 0) {
        toastRef.value?.warning('Thiếu thông tin', 'Phải có ít nhất một phiên bản')
        return
      }
      
      // Kiểm tra từng variant
      for (let i = 0; i < payload.variants.length; i++) {
        const variant = payload.variants[i]
        if (!variant.idRam || !variant.idRom || !variant.idMauSac) {
          toastRef.value?.warning('Thiếu thông tin', `Phiên bản ${i + 1} thiếu thông tin RAM, ROM hoặc màu sắc`)
          return
        }
      }
      
      
      const response = await api.post('/api/san-pham/full', payload)
      
      // Cập nhật ID của các variant từ response
      if (response.data && response.data.variants) {
        response.data.variants.forEach((createdVariant: any, index: number) => {
          if (variants.value[index]) {
            variants.value[index].id = createdVariant.id
          }
        })
      }
    } else if (id.value != null) {
      // Sử dụng endpoint mới để cập nhật sản phẩm với variants
      const payload = {
        tenSanPham: form.value.tenSanPham,
        moTa: form.value.moTa,
        idDanhMuc: form.value.idDanhMuc,
        idHang: form.value.idHang,
        idManHinh: form.value.idManHinh,
        idCameraTruoc: form.value.idCameraTruoc,
        idCameraSau: form.value.idCameraSau,
        idChip: form.value.idChip,
        idGpu: form.value.idGpu,
        idSim: form.value.idSim,
        idHeDieuHanh: form.value.idHeDieuHanh,
        idCpu: form.value.idCpu,
        idPin: form.value.idPin,
        variants: variants.value.map(v => ({
          idRam: v.idRam,
          idRom: v.idRom,
          idMauSac: v.idMauSac,
          soLuong: v.imeis && v.imeis.length > 0 ? v.imeis.length : 0,
          donGia: Number(v.donGia) || 0,
          giaNhap: Number(v.giaNhap) || 0,
          ghiChu: v.ghiChu || '',
          imeis: v.imeis || [],
          imageUrls: v.imageUrls || [] 
        }))
      }
      
      console.log('DEBUG - Frontend payload:', payload)
      
      const response = await api.put(`/api/san-pham/${id.value}/full`, payload)
    }
    
    // Clear all localStorage data after successful save
    clearAllProductLocalStorage()
    
    toastRef.value?.success('Thành công', 'Lưu sản phẩm thành công!')
    
    // Sau khi lưu thành công, redirect về trang danh sách sản phẩm
    router.push({ name: 'san-pham' })
  } catch (error) {
    console.error('Error saving product:', error)
    console.error('Error details:', error.response?.data)
    toastRef.value?.error('Lỗi', 'Có lỗi xảy ra khi lưu sản phẩm: ' + (error.response?.data?.message || error.message))
  }
}

async function loadLookups() {
  try {
    const [dmRes, hRes, ramRes, romRes, mauRes, hdhRes, mhRes, ctRes, csRes, pinRes, chipRes, cpuRes, gpuRes, simRes] = await Promise.allSettled([
      api.get<DanhMuc[]>('/api/danh-muc/active'),
      api.get<Hang[]>('/api/hang/active'),
      api.get<Ram[]>('/api/ram/active'),
      api.get<Rom[]>('/api/rom/active'),
      api.get<MauSac[]>('/api/mau-sac/active'),
      api.get<HeDieuHanh[]>('/api/he-dieu-hanh/active'),
      api.get<ManHinh[]>('/api/man-hinh/active'),
      api.get<CameraTruoc[]>('/api/camera-truoc/active'),
      api.get<CameraSau[]>('/api/camera-sau/active'),
      api.get<Pin[]>('/api/pin/active'),
      api.get<Chip[]>('/api/chip/active'),
      api.get<Cpu[]>('/api/cpu/active'),
      api.get<Gpu[]>('/api/gpu/active'),
      api.get<Sim[]>('/api/sim/active'),
    ])
    
    // Handle successful responses
    if (dmRes.status === 'fulfilled') {
      danhMucs.value = dmRes.value.data
    }
    if (hRes.status === 'fulfilled') {
      hangs.value = hRes.value.data
      console.log('Hangs loaded:', hangs.value)
    } else {
      console.error('Hang request failed:', hRes.reason)
    }
    if (ramRes.status === 'fulfilled') rams.value = ramRes.value.data
    if (romRes.status === 'fulfilled') roms.value = romRes.value.data
    if (mauRes.status === 'fulfilled') mauSacs.value = mauRes.value.data
    if (hdhRes.status === 'fulfilled') heDieuHanhs.value = hdhRes.value.data
    if (mhRes.status === 'fulfilled') manHinhs.value = mhRes.value.data
    if (ctRes.status === 'fulfilled') {
      cameraTruocs.value = ctRes.value.data
    }
    if (csRes.status === 'fulfilled') {
      cameraSaus.value = csRes.value.data
    }
    if (pinRes.status === 'fulfilled') {
      pins.value = pinRes.value.data
      console.log('Pins loaded:', pins.value)
      console.log('Pins count:', pins.value.length)
    } else {
      console.error('Pin request failed:', pinRes.reason)
      console.error('Pin request error details:', pinRes.reason?.response?.data)
    }
    if (chipRes.status === 'fulfilled') chips.value = chipRes.value.data
    if (cpuRes.status === 'fulfilled') cpus.value = cpuRes.value.data
    if (gpuRes.status === 'fulfilled') gpus.value = gpuRes.value.data
    if (simRes.status === 'fulfilled') sims.value = simRes.value.data
    
    // Log any failed requests
    const failed = [dmRes, hRes, ramRes, romRes, mauRes, hdhRes, mhRes, ctRes, csRes, pinRes, chipRes, cpuRes, gpuRes, simRes]
      .filter(result => result.status === 'rejected')
    if (failed.length > 0) {
      console.warn('Một số API không khả dụng:', failed.map(f => f.reason?.message))
      alert('Không thể tải một số dữ liệu cần thiết. Vui lòng kiểm tra kết nối và thử lại.')
    }
    
  } catch (error) {
    console.error('Lỗi khi tải dữ liệu:', error)
  }
}

// view len de sua
async function loadProductData() {
  try {
    if (!id.value) return
    
    const response = await api.get(`/api/san-pham/${id.value}/edit`)
    const productData = response.data
    
    // Load product basic info
    form.value = {
      maSanPham: productData.maSanPham || '',
      tenSanPham: productData.tenSanPham || '',
      idDanhMuc: productData.idDanhMuc || null,
      idHang: productData.idHang || null,
      moTa: productData.moTa || '',
      idHeDieuHanh: productData.idHeDieuHanh || null,
      idManHinh: productData.idManHinh || null,
      idCameraTruoc: productData.idCameraTruoc || null,
      idCameraSau: productData.idCameraSau || null,
      idPin: productData.idPin || null,
      idChip: productData.idChip || null,
      idSim: productData.idSim || null,
      idGpu: productData.idGpu || null,
      idCpu: productData.idCpu || null
    }
    
    // Load variants
    if (productData.variants && productData.variants.length > 0) {
      variants.value = productData.variants.map((variant: any, index: number) => ({
        id: variant.id, // ID của chi tiết sản phẩm
        idRam: variant.idRam || null,
        idRom: variant.idRom || null,
        idMauSac: variant.idMauSac || null,
        soLuong: variant.soLuong || 0,
        donGia: variant.donGia !== null && variant.donGia !== undefined ? variant.donGia : 0,
        giaNhap: variant.giaNhap !== null && variant.giaNhap !== undefined ? variant.giaNhap : 0,
        ghiChu: variant.ghiChu || '',
        tenSanPham: productData.tenSanPham || '',
        idHang: productData.idHang || null,
        idDanhMuc: productData.idDanhMuc || null,
        moTa: productData.moTa || '',
        idHeDieuHanh: productData.idHeDieuHanh || null,
        idManHinh: productData.idManHinh || null,
        idCameraTruoc: productData.idCameraTruoc || null,
        idCameraSau: productData.idCameraSau || null,
        idPin: productData.idPin || null,
        idChip: productData.idChip || null,
        idSim: productData.idSim || null,
        imeis: variant.imeis || [],
        images: [],
        imageUrls: variant.imageUrls || [], 
        originalIndex: index
      }))
      
      // QUAN TRỌNG: Load ảnh theo màu sắc từ variants
      variants.value.forEach(variant => {
        if (variant.idMauSac && variant.imageUrls && variant.imageUrls.length > 0) {
          // Initialize color images if not exists
          if (!colorImages.value[variant.idMauSac]) {
            colorImages.value[variant.idMauSac] = []
          }
          // Add images to color group (avoid duplicates)
          variant.imageUrls.forEach(imageUrl => {
            if (!colorImages.value[variant.idMauSac].includes(imageUrl)) {
              colorImages.value[variant.idMauSac].push(imageUrl)
            }
          })
        }
      })
      
      // Load selected values for the first variant into variantForm
      if (variants.value.length > 0) {
        const firstVariant = variants.value[0]
        variantForm.value.selectedRams = firstVariant.idRam ? [firstVariant.idRam] : []
        variantForm.value.selectedRoms = firstVariant.idRom ? [firstVariant.idRom] : []
        variantForm.value.selectedMauSacs = firstVariant.idMauSac ? [firstVariant.idMauSac] : []
      }
    }
    
  } catch (error) {
    console.error('Error loading product data:', error)
    alert('Có lỗi khi tải dữ liệu sản phẩm. Vui lòng thử lại.')
  }
}

onMounted(loadLookups)

// Close dropdowns when clicking outside
function handleClickOutside(event: Event) {
  const target = event.target as HTMLElement
  if (!target.closest('.multi-select') && !target.closest('.multi-select-dropdown')) {
    showRamDropdown.value = false
    showRomDropdown.value = false
    showMauSacDropdown.value = false
  }
}

// Close other dropdowns when opening one
function openRamDropdown() {
  showRomDropdown.value = false
  showMauSacDropdown.value = false
  showRamDropdown.value = !showRamDropdown.value
}

function openRomDropdown() {
  showRamDropdown.value = false
  showMauSacDropdown.value = false
  showRomDropdown.value = !showRomDropdown.value
}

function openMauSacDropdown() {
  showRamDropdown.value = false
  showRomDropdown.value = false
  showMauSacDropdown.value = !showMauSacDropdown.value
}

// Price handling functions

function applyPriceToAll(groupKey: string) {
  const group = groupedVariants.value.find(g => g.key === groupKey)
  if (group && group.priceInput) {
    const price = parseFloat(group.priceInput)
    if (!isNaN(price)) {
      // Apply price to all variants in this group
      group.variants.forEach(variant => {
        variants.value[variant.originalIndex].donGia = price
      })
      toastRef.value?.success(
        'Áp dụng giá thành công',
        `Đã áp dụng giá ${price.toLocaleString()} VNĐ cho tất cả biến thể trong phiên bản ${group.ramName}/${group.romName}`
      )
      group.priceInput = ''
    } else {
      toastRef.value?.error(
        'Lỗi nhập liệu',
        'Vui lòng nhập giá trị hợp lệ'
      )
    }
  } else {
    toastRef.value?.warning(
      'Thiếu thông tin',
      'Vui lòng nhập giá trị'
    )
  }
}

function deleteVariantGroup(groupKey: string) {
  const group = groupedVariants.value.find(g => g.key === groupKey)
  if (group) {
    // Remove all variants in this group from the variants array
    const indicesToRemove = group.variants.map(v => v.originalIndex).sort((a, b) => b - a)
    indicesToRemove.forEach(index => {
      variants.value.splice(index, 1)
    })
    
    toastRef.value?.success(
      'Xóa nhóm thành công',
      `Đã xóa ${group.variants.length} phiên bản trong nhóm ${group.ramName}/${group.romName}`
    )
  }
}

// Color image management functions
async function handleColorImageUpload(colorId: number, event: Event) {
  const input = event.target as HTMLInputElement
  if (input.files && input.files.length > 0) {
    const file = input.files[0]
    
    // Validate file size (max 10MB per file)
    const maxSize = 10 * 1024 * 1024 // 10MB in bytes
    
    if (file.size > maxSize) {
      toastRef.value?.error('Lỗi', `File "${file.name}" vượt quá giới hạn 10MB`)
      return
    }
    
    try {
      const formData = new FormData()
      formData.append('file', file)
      
      const response = await api.post('/api/upload/image', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
      
      if (response.data.url) {
        // Initialize color images array if not exists
        if (!colorImages.value[colorId]) {
          colorImages.value[colorId] = []
        }
        
        // Add URL to color images
        colorImages.value[colorId].push(response.data.url)
        
        // QUAN TRỌNG: Gán ảnh vào tất cả variants có cùng màu sắc
        variants.value.forEach((variant, index) => {
          if (variant.idMauSac === colorId) {
            // Initialize imageUrls if not exists
            if (!variant.imageUrls) {
              variant.imageUrls = []
            }
            // Add the new image URL to this variant
            variant.imageUrls.push(response.data.url)
          }
        })
        
        toastRef.value?.success('Upload thành công', `Đã thêm ảnh cho màu sắc`)
      }
    } catch (error) {
      console.error('Error uploading image:', error)
      if (error.response?.status === 413) {
        toastRef.value?.error('Lỗi', 'File quá lớn! Vui lòng chọn file nhỏ hơn 10MB.')
      } else {
        toastRef.value?.error('Lỗi', 'Lỗi upload ảnh: ' + (error.response?.data?.error || error.message))
      }
    }
    
    // Clear input để có thể chọn lại file cùng tên
    input.value = ''
  }
}

function removeColorImage(colorId: number, imageIndex: number) {
  if (colorImages.value[colorId] && colorImages.value[colorId].length > imageIndex) {
    // Get the image URL to remove
    const imageUrlToRemove = colorImages.value[colorId][imageIndex]
    
    // Remove from color images
    colorImages.value[colorId].splice(imageIndex, 1)
    
    // QUAN TRỌNG: Cũng xóa ảnh khỏi tất cả variants có cùng màu sắc
    variants.value.forEach(variant => {
      if (variant.idMauSac === colorId && variant.imageUrls) {
        const variantImageIndex = variant.imageUrls.indexOf(imageUrlToRemove)
        if (variantImageIndex !== -1) {
          variant.imageUrls.splice(variantImageIndex, 1)
        }
      }
    })
    
    toastRef.value?.success('Xóa ảnh thành công', 'Đã xóa ảnh khỏi màu sắc')
  }
}

function triggerColorFileInput(colorId: number) {
  const fileInput = document.getElementById(`colorImageUpload-${colorId}`) as HTMLInputElement
  if (fileInput) {
    fileInput.click()
  }
}

// Excel handling functions
function triggerExcelUpload(variantIndex: number) {
  const fileInput = document.getElementById(`excelFile-${variantIndex}`) as HTMLInputElement
  if (fileInput) {
    fileInput.click()
  } else {
    console.error('File input not found for variant:', variantIndex)
    alert('Không thể mở dialog chọn file. Vui lòng thử lại.')
  }
}

async function handleExcelUpload(variantIndex: number, event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (file) {
    const reader = new FileReader()
    reader.onload = (e) => {
      try {
        const data = e.target?.result
        const workbook = XLSX.read(data, { type: 'binary' })
        const sheetName = workbook.SheetNames[0]
        const worksheet = workbook.Sheets[sheetName]
        const jsonData = XLSX.utils.sheet_to_json(worksheet, { header: 1 })
        
        // Extract IMEI from Excel data
        let imeiList: string[] = []
        
        // Look for IMEI in different possible columns
        jsonData.forEach((row: any) => {
          if (Array.isArray(row)) {
            row.forEach((cell: any) => {
              if (cell !== null && cell !== undefined && cell !== '') {
                let imei = cell.toString().trim()
                
                // Remove any non-numeric characters except digits
                imei = imei.replace(/[^\d]/g, '')
                
                // Check if it's a valid IMEI (15 digits)
                if (/^\d{15}$/.test(imei)) {
                  imeiList.push(imei)
                }
              }
            })
          }
        })
        
        if (imeiList.length > 0) {
          // Check for duplicates in database
          checkDuplicateImeis(imeiList).then(dbDuplicates => {
            if (dbDuplicates.length > 0) {
              const message = `Có ${dbDuplicates.length} IMEI đã tồn tại trong database:\n${dbDuplicates.slice(0, 5).join('\n')}${dbDuplicates.length > 5 ? '\n...' : ''}\n\nBạn có muốn tiếp tục nhập các IMEI còn lại không?`
              
              // Show confirm modal instead of browser alert
              showConfirmModal.value = true
              confirmTitle.value = 'Xác nhận IMEI trùng lặp từ Excel'
              confirmMessage.value = message
              pendingImeiAction.value = () => {
                // Remove database duplicates from the list
                imeiList = imeiList.filter(imei => !dbDuplicates.includes(imei))
                continueWithExcelImport(imeiList, dbDuplicates, variantIndex, target)
              }
              return
            }
            
            // Continue with normal import if no duplicates
            continueWithExcelImport(imeiList, [], variantIndex, target)
          }).catch(error => {
            console.error('Error checking duplicates:', error)
            toastRef.value?.error('Lỗi', 'Có lỗi khi kiểm tra IMEI trùng lặp. Vui lòng thử lại.')
            target.value = ''
          })
        } else {
          toastRef.value?.warning('Cảnh báo', 'Không tìm thấy IMEI hợp lệ trong file Excel. Vui lòng kiểm tra định dạng file.')
          target.value = ''
        }
      } catch (error) {
        console.error('Error reading Excel file:', error)
        toastRef.value?.error('Lỗi', 'Có lỗi khi đọc file Excel. Vui lòng kiểm tra định dạng file.')
        target.value = ''
      }
    }
    reader.readAsBinaryString(file)
  }
}

async function importFromExcel(variantIndex: number) {
  currentVariantIndex.value = variantIndex
  imeiInput.value = ''
  showImeiModal.value = true
  
  // Kiểm tra trùng lặp DB cho IMEI hiện có
  const variant = variants.value[variantIndex]
  if (variant && variant.imeis && variant.imeis.length > 0) {
    try {
      const dbDuplicates = await checkDuplicateImeis(variant.imeis)
      duplicateImeis.value = dbDuplicates
    } catch (error) {
      console.error('Lỗi kiểm tra trùng lặp DB:', error)
      duplicateImeis.value = []
    }
  } else {
    duplicateImeis.value = []
  }
}

// Function to download Excel template
function downloadExcelTemplate() {
  const templateData = [
    ['IMEI'],
    ['123456789012345'],
    ['123456789012346'],
    ['123456789012347']
  ]
  
  const ws = XLSX.utils.aoa_to_sheet(templateData)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, 'IMEI Template')
  
  XLSX.writeFile(wb, 'imei_template.xlsx')
}

// IMEI Modal functions
function closeImeiModal() {
  showImeiModal.value = false
  currentVariantIndex.value = null
  imeiInput.value = ''
  selectedFileName.value = ''
  if (excelFileInput.value) {
    excelFileInput.value.value = ''
  }
}

async function saveImei() {
  if (!imeiInput.value.trim()) {
    toastRef.value?.warning('Thiếu thông tin', 'Vui lòng nhập IMEI')
    return
  }
  
  if (currentVariantIndex.value !== null) {
    // Parse IMEI from textarea (one per line)
    let imeiList = imeiInput.value
      .split('\n')
      .map(line => line.trim())
      .filter(line => line.length > 0)
    
    // Validate IMEI format (15 digits)
    const validImeis = imeiList.filter(imei => /^\d{15}$/.test(imei))
    const invalidImeis = imeiList.filter(imei => !/^\d{15}$/.test(imei))
    
    if (validImeis.length === 0) {
      toastRef.value?.error('Lỗi', 'Không có IMEI hợp lệ nào để lưu. IMEI phải có đúng 15 chữ số')
      return
    }
    
    // Kiểm tra trùng lặp trong danh sách đang nhập
    const duplicateImeis = validImeis.filter((imei, index) => validImeis.indexOf(imei) !== index)
    const uniqueValidImeis = Array.from(new Set(validImeis))
    
    if (duplicateImeis.length > 0) {
      toastRef.value?.warning('Cảnh báo', `Có ${duplicateImeis.length} IMEI trùng lặp trong danh sách đã được loại bỏ`)
    }
    
    if (invalidImeis.length > 0) {
      toastRef.value?.warning('Cảnh báo', `Có ${invalidImeis.length} IMEI không hợp lệ đã được bỏ qua`)
    }
    
    // Chỉ lưu IMEI hợp lệ và không trùng lặp
    imeiList = uniqueValidImeis
    
    // Check for duplicates in database
    const dbDuplicates = await checkDuplicateImeis(imeiList)
    
    if (dbDuplicates.length > 0) {
      const message = `Có ${dbDuplicates.length} IMEI đã tồn tại trong database:\n${dbDuplicates.slice(0, 5).join('\n')}${dbDuplicates.length > 5 ? '\n...' : ''}\n\nBạn có muốn tiếp tục nhập các IMEI còn lại không?`
      
      // Show confirm modal instead of browser alert
      showConfirmModal.value = true
      confirmTitle.value = 'Xác nhận IMEI trùng lặp'
      confirmMessage.value = message
      pendingImeiAction.value = () => {
        // Remove database duplicates from the list
        imeiList = imeiList.filter(imei => !dbDuplicates.includes(imei))
        continueWithImeiSave(imeiList, dbDuplicates)
      }
      return
    }
    
    // Continue with normal save if no duplicates
    continueWithImeiSave(imeiList, [])
  }
}

// Helper function to continue with Excel import after confirmation
function continueWithExcelImport(imeiList: string[], dbDuplicates: string[], variantIndex: number, target: HTMLInputElement) {
  if (imeiList.length > 0) {
    // Set current variant index and open IMEI modal for validation
    currentVariantIndex.value = variantIndex
    
    // Filter out IMEIs that already exist in this variant
    const existingImeis = variants.value[variantIndex].imeis || []
    const newImeis = imeiList.filter(imei => !existingImeis.includes(imei))
    const duplicateInVariant = imeiList.length - newImeis.length
    
    if (newImeis.length === 0) {
      toastRef.value?.warning('Cảnh báo', 'Tất cả IMEI trong file đã tồn tại trong phiên bản này')
      target.value = ''
      return
    }
    
    // Add IMEIs to input area for validation
    const currentInput = imeiInput.value.trim()
    const newInput = newImeis.join('\n')
    imeiInput.value = currentInput ? `${currentInput}\n${newInput}` : newInput
    
    // Open IMEI modal for user to review and save
    showImeiModal.value = true
    
    // Clear validation states for new IMEIs
    newImeis.forEach(imei => {
      imeiValidationStates.value.delete(imei)
    })
    
    // Start validation for new IMEIs
    validateAllInputImeis()
    
    let message = `Đã nhập ${newImeis.length} IMEI từ file Excel vào modal xem xét`
    if (duplicateInVariant > 0) {
      message += ` (${duplicateInVariant} IMEI đã tồn tại trong phiên bản này đã bỏ qua)`
    }
    if (dbDuplicates.length > 0) {
      message += ` (${dbDuplicates.length} IMEI đã tồn tại trong database đã được bỏ qua)`
    }
    
    toastRef.value?.success('Thành công', message)
  } else {
    toastRef.value?.warning('Thông báo', 'Tất cả IMEI trong file đã tồn tại trong database.')
  }
  
  // Clear the file input
  target.value = ''
}

// Helper function to continue with IMEI save after confirmation
function continueWithImeiSave(imeiList: string[], dbDuplicates: string[]) {
  if (imeiList.length > 0) {
    // Lưu IMEI vào phiên bản tương ứng
    const variant = variants.value[currentVariantIndex.value!]
    if (variant) {
      const existingImeis = variant.imeis || []
      const newImeis = [...existingImeis, ...imeiList]
      
      // Remove duplicates within the new list
      const uniqueImeis = Array.from(new Set(newImeis))
      const duplicateCount = newImeis.length - uniqueImeis.length
      
      variant.imeis = uniqueImeis
      variant.soLuong = uniqueImeis.length
      
      // Clear input after successful save
      imeiInput.value = ''
      
      // Show success message
      let message = `Đã thêm ${imeiList.length} IMEI hợp lệ`
      if (duplicateCount > 0) {
        message += ` (${duplicateCount} IMEI trùng lặp đã được loại bỏ)`
      }
      if (dbDuplicates.length > 0) {
        message += `\n${dbDuplicates.length} IMEI đã tồn tại trong database đã được bỏ qua`
      }
      
      toastRef.value?.success('Thành công', message)
    }
  } else {
    toastRef.value?.warning('Thông báo', 'Tất cả IMEI đã tồn tại trong database.')
  }
}

function clearImeiInput() {
  imeiInput.value = ''
  if (currentVariantIndex.value !== null) {
    const variant = variants.value[currentVariantIndex.value]
    if (variant) {
      variant.imeis = []
      variant.soLuong = 0
    }
  }
  toastRef.value?.success('Thành công', 'Đã xóa tất cả IMEI')
}


function removeImeiFromList(index: number) {
  if (currentVariantIndex.value !== null) {
    const variant = variants.value[currentVariantIndex.value]
    if (variant && variant.imeis) {
      const removedImei = variant.imeis[index]
      variant.imeis.splice(index, 1)
      variant.soLuong = variant.imeis.length
      toastRef.value?.success('Thành công', `Đã xóa IMEI: ${removedImei}`)
    }
  }
}

function removeImeiFromInput(index: number) {
  const inputImeis = getInputImeis()
  if (index >= 0 && index < inputImeis.length) {
    const removedImei = inputImeis[index]
    const newImeiList = inputImeis.filter((_, i) => i !== index)
    imeiInput.value = newImeiList.join('\n')
    
    // Remove from selected set if it was selected
    selectedImeis.value.delete(removedImei)
    
    toastRef.value?.success('Thành công', `Đã xóa IMEI: ${removedImei}`)
  }
}

function deleteSelectedImeis() {
  if (selectedImeis.value.size === 0) {
    toastRef.value?.warning('Cảnh báo', 'Chưa chọn IMEI nào để xóa')
    return
  }
  
  const inputImeis = getInputImeis()
  const remainingImeis = inputImeis.filter(imei => !selectedImeis.value.has(imei))
  
  imeiInput.value = remainingImeis.join('\n')
  const deletedCount = selectedImeis.value.size
  selectedImeis.value.clear()
  
  toastRef.value?.success('Thành công', `Đã xóa ${deletedCount} IMEI đã chọn`)
}

// Hàm kiểm tra DB cho tất cả IMEI đang nhập
async function validateAllInputImeis() {
  const inputImeis = getInputImeis()
  
  console.log('Validating IMEIs:', inputImeis)
  
  // Set initial states for all IMEIs
  inputImeis.forEach(imei => {
    if (!imeiValidationStates.value.has(imei)) {
      if (!isValidImei(imei)) {
        imeiValidationStates.value.set(imei, 'invalid')
      } else if (isDuplicateImei(imei)) {
        imeiValidationStates.value.set(imei, 'duplicate')
      } else {
        imeiValidationStates.value.set(imei, 'checking')
      }
    }
  })
  
  // Check database for valid IMEIs
  const validImeis = inputImeis.filter(imei => isValidImei(imei) && !isDuplicateImei(imei))
  
  for (const imei of validImeis) {
    if (imeiValidationStates.value.get(imei) === 'checking') {
      console.log(`Checking IMEI: ${imei}`)
      
      try {
        const isDuplicate = await checkImeiInDb(imei)
        console.log(`IMEI ${imei} result:`, isDuplicate ? 'DUPLICATE' : 'UNIQUE')
        imeiValidationStates.value.set(imei, isDuplicate ? 'duplicate' : 'valid')
      } catch (error) {
        console.error('Lỗi kiểm tra IMEI:', error)
        imeiValidationStates.value.set(imei, 'valid') // Mặc định là valid nếu lỗi
      }
    }
  }
}

function hasValidImeiToSave(): boolean {
  if (!imeiInput.value.trim()) return false
  
  const inputImeis = getInputImeis()
  const validImeis = inputImeis.filter(imei => isValidImei(imei) && !isDuplicateImei(imei))
  
  // Kiểm tra xem có IMEI nào đã được validate và không trùng lặp DB
  return validImeis.some(imei => {
    const state = imeiValidationStates.value.get(imei)
    return state === 'valid'
  })
}

// Helper functions for IMEI modal
function getCurrentVariantName() {
  if (currentVariantIndex.value === null) return ''
  const variant = variants.value[currentVariantIndex.value]
  if (!variant) return ''
  
  const ramName = getAttributeName(variant.idRam, rams.value, 'tenRam')
  const romName = getAttributeName(variant.idRom, roms.value, 'dungLuong')
  const mauName = getAttributeName(variant.idMauSac, mauSacs.value, 'tenMau')
  
  return `${ramName}/${romName} - ${mauName}`
}

function getImeiCount() {
  if (currentVariantIndex.value === null) return 0
  const variant = variants.value[currentVariantIndex.value]
  return variant ? (variant.imeis?.length || 0) : 0
}

function getCurrentImeis() {
  if (currentVariantIndex.value === null) return []
  const variant = variants.value[currentVariantIndex.value]
  return variant ? (variant.imeis || []) : []
}

function getInputImeis() {
  if (!imeiInput.value.trim()) return []
  return imeiInput.value
    .split('\n')
    .map(line => line.trim())
    .filter(line => line.length > 0)
}

function getTotalImeiCount() {
  return getCurrentImeis().length + getInputImeis().length
}

function isValidImei(imei: string): boolean {
  return /^\d{15}$/.test(imei)
}

function isDuplicateImei(imei: string): boolean {
  if (currentVariantIndex.value === null) return false
  const variant = variants.value[currentVariantIndex.value]
  if (!variant || !variant.imeis) return false
  
  // Kiểm tra trùng lặp với IMEI đã lưu
  const savedImeis = variant.imeis || []
  if (savedImeis.includes(imei)) return true
  
  // Kiểm tra trùng lặp với IMEI đang nhập khác
  const inputImeis = getInputImeis()
  const duplicateCount = inputImeis.filter(inputImei => inputImei === imei).length
  return duplicateCount > 1
}

function isDuplicateInDb(imei: string): boolean {
  // Kiểm tra trong danh sách IMEI đã được kiểm tra trùng lặp DB
  return duplicateImeis.value.includes(imei)
}

// Cache để lưu kết quả kiểm tra DB cho từng IMEI
const dbCheckCache = ref<Map<string, boolean>>(new Map())

async function checkImeiInDb(imei: string): Promise<boolean> {
  // Kiểm tra cache trước
  if (dbCheckCache.value.has(imei)) {
    return dbCheckCache.value.get(imei)!
  }
  
  try {
    const response = await fetch('/api/imei/check-duplicates', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify([imei]) // API expect List<String>, not object
    })
    
    if (response.ok) {
      const duplicates = await response.json() // API returns List<String> directly
      const isDuplicate = duplicates && duplicates.length > 0
      dbCheckCache.value.set(imei, isDuplicate)
      console.log(`IMEI ${imei} check result:`, isDuplicate ? 'DUPLICATE' : 'UNIQUE')
      return isDuplicate
    } else {
      console.error('API response not ok:', response.status, response.statusText)
    }
  } catch (error) {
    console.error('Lỗi kiểm tra IMEI trong DB:', error)
  }
  
  return false
}

// Reactive state cho validation
const imeiValidationStates = ref<Map<string, string>>(new Map())

// State for selected IMEIs
const selectedImeis = ref<Set<string>>(new Set())

// Computed property for checkbox binding
const selectedImeisArray = computed({
  get: () => Array.from(selectedImeis.value),
  set: (value: string[]) => {
    selectedImeis.value = new Set(value)
  }
})

function getImeiValidationText(imei: string): string {
  if (!isValidImei(imei)) {
    return 'Không hợp lệ'
  } else if (isDuplicateImei(imei)) {
    return 'Trùng lặp'
  } else {
    const state = imeiValidationStates.value.get(imei)
    if (state === 'checking') {
      return 'Đang kiểm tra...'
    } else if (state === 'duplicate') {
      return 'Trùng lặp DB'
    } else if (state === 'valid') {
      return 'Hợp lệ'
    } else {
      return 'Chưa kiểm tra'
    }
  }
}

function getImeiValidationClass(imei: string): string {
  if (!isValidImei(imei)) {
    return 'imei-item-invalid'
  } else if (isDuplicateImei(imei)) {
    return 'imei-item-duplicate'
  } else {
    const state = imeiValidationStates.value.get(imei)
    if (state === 'checking') {
      return 'imei-item-checking'
    } else if (state === 'duplicate') {
      return 'imei-item-db-duplicate'
    } else if (state === 'valid') {
      return 'imei-item-valid'
    } else {
      return 'imei-item-pending'
    }
  }
}

function getImeiStatusClass(imei: string): string {
  if (!isValidImei(imei)) {
    return 'imei-status-invalid'
  } else if (isDuplicateImei(imei)) {
    return 'imei-status-duplicate'
  } else {
    const state = imeiValidationStates.value.get(imei)
    if (state === 'checking') {
      return 'imei-status-checking'
    } else if (state === 'duplicate') {
      return 'imei-status-db-duplicate'
    } else if (state === 'valid') {
      return 'imei-status-valid'
    } else {
      return 'imei-status-pending'
    }
  }
}

function triggerExcelFileInput() {
  if (excelFileInput.value) {
    excelFileInput.value.click()
  }
}

function handleExcelFileUpload(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (file) {
    selectedFileName.value = file.name
    // Handle Excel file upload logic here
    handleExcelUpload(currentVariantIndex.value!, event)
  }
}

function downloadImeiTemplate() {
  downloadExcelTemplate()
}

// Function to update quantity for all variants based on IMEI count
function updateAllVariantQuantities() {
  variants.value.forEach(variant => {
    if (variant.imeis && variant.imeis.length > 0) {
      variant.soLuong = variant.imeis.length
    } else {
      variant.soLuong = 0
    }
  })
}

// Function to check for duplicate IMEIs in database
async function checkDuplicateImeis(imeiList: string[]): Promise<string[]> {
  try {
    const response = await api.post<string[]>('/api/imei/check-duplicates', imeiList)
    return response.data
  } catch (error) {
    console.error('Error checking duplicate IMEIs:', error)
    return []
  }
}


// Draft data management - chỉ lưu phiên bản
function saveDraftData() {
  // Chỉ lưu draft khi ở chế độ tạo mới, không lưu khi sửa
  if (isCreate.value) {
    const draftData = {
      variants: variants.value,
      variantForm: variantForm.value,
      timestamp: Date.now()
    }
    localStorage.setItem('sanpham-draft', JSON.stringify(draftData))
  }
}

function loadDraftData() {
  // Chỉ load draft khi ở chế độ tạo mới, không load khi sửa
  if (!isCreate.value) {
    return
  }
  
  try {
    const saved = localStorage.getItem('sanpham-draft')
    if (saved) {
      const draftData = JSON.parse(saved)
      
      // Check if draft is not too old (24 hours)
      const isOld = Date.now() - draftData.timestamp > 24 * 60 * 60 * 1000
      if (isOld) {
        localStorage.removeItem('sanpham-draft')
        return
      }
      
      // Chỉ restore variants và variantForm, không restore form data
      if (draftData.variants && Array.isArray(draftData.variants)) {
        variants.value = draftData.variants
      }
      
      if (draftData.variantForm) {
        variantForm.value = { ...variantForm.value, ...draftData.variantForm }
      }
      
      // Update quantities based on IMEI count
      updateAllVariantQuantities()
    }
  } catch (error) {
    console.error('Error loading draft data:', error)
    localStorage.removeItem('sanpham-draft')
  }
}

function clearDraftData() {
  confirmTitle.value = 'Xác nhận làm mới'
  confirmMessage.value = 'Bạn có chắc chắn muốn xóa? Các phiên bản đã chọn sẽ bị mất!'
  pendingAction.value = () => {
    // Clear all product-related data from localStorage
    clearAllProductLocalStorage()
    
    // Chỉ reset variants, không reset form data
    variants.value = []
    variantForm.value = {
      selectedRams: [],
      selectedRoms: [],
      selectedMauSacs: []
    }
    toastRef.value?.success('Thành công', 'Đã xóa bản nháp thành công!')
  }
  showConfirmModal.value = true
}

// Watch for changes and auto-save draft - chỉ lưu khi có variants

watch([variants, variantForm], () => {
  console.log('DEBUG - Variants array changed:', JSON.stringify(variants.value, null, 2))
  // Chỉ lưu khi có variants
  if (variants.value.length > 0) {
    saveDraftData()
  }
}, { deep: true })

// Watch for route changes to reset form when switching between create/edit
watch(() => route.params.id, (newId, oldId) => {
  // If switching from edit to create (or vice versa), reset form
  if (newId !== oldId) {
    if (!newId && oldId) {
      // Switching from edit to create - reset form completely
      refreshForm()
    } else if (newId && !oldId) {
      // Switching from create to edit - load product data
      loadProductData()
    }
  }
})

// Watch for route name changes (more reliable for navigation)
watch(() => route.name, (newName, oldName) => {
  if (newName === 'san-pham-create' && oldName === 'san-pham-edit') {
    // Navigating from edit to create - force reset form
    console.log('Navigating from edit to create - resetting form')
    refreshForm()
  }
})

// Watch for route path changes (most reliable)
watch(() => route.path, (newPath, oldPath) => {
  if (newPath === '/san-pham/create' && oldPath?.includes('/san-pham/edit/')) {
    // Navigating from edit to create - force reset form
    console.log('Path changed from edit to create - resetting form')
    refreshForm()
  }
})

// Watch for changes in imeiInput to validate IMEIs
watch(imeiInput, async () => {
  // Clear validation states when input changes
  imeiValidationStates.value.clear()
  
  // Debounce validation to avoid too many API calls
  setTimeout(async () => {
    await validateAllInputImeis()
  }, 500)
}, { flush: 'post' })

onMounted(async () => {
  await loadLookups()
  
  // Load product data if editing
  if (id.value) {
    await loadProductData()
  } else {
    // Chỉ load draft data khi ở chế độ tạo mới
    loadDraftData()
  }
  
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <div class="page">
  <div class="page-header">
    <h1 class="page-title">{{ isCreate ? 'THÊM SẢN PHẨM' : 'SỬA SẢN PHẨM' }}</h1>
    <button class="btn-refresh" @click="clearDraftAndRefresh">
      <img src="@/assets/002-notification-1.png" alt="Refresh" class="btn-icon" />
      Làm mới
    </button>
  </div>

  <!-- Confirm Modal -->
  <ConfirmModal
    :show="showConfirmModal"
    :title="confirmTitle"
    :message="confirmMessage"
    @confirm="handleConfirm"
    @cancel="handleCancel"
  />

  <!-- Toast -->
  <Toast ref="toastRef" />

    <div class="form-card">
      <div class="form-columns">
        <div class="form-column">
          <div class="form-group">
            <label>Tên sản phẩm</label>
            <div class="search-container">
              <input 
                v-model="form.tenSanPham" 
                @input="onProductNameInput"
                @blur="hideSearchResults"
                placeholder="Nhập tên sản phẩm..."
              />
              <div v-if="showSearchResults && searchResults.length > 0" class="search-dropdown">
                <div class="search-header">Sản phẩm tương tự:</div>
                <div 
                  v-for="product in searchResults" 
                  :key="product.id" 
                  class="search-item"
                  @mousedown="selectExistingProduct(product)"
                >
                  <div class="product-name" v-html="highlightText(product.tenSanPham, form.tenSanPham)"></div>
                  <div class="product-info">
                    <span class="brand">{{ product.tenHang || 'Chưa có hãng' }}</span>
                    <span class="status" :class="product.tongImei > 0 ? 'in-stock' : 'out-of-stock'">
                      {{ product.tongImei > 0 ? 'Còn hàng' : 'Hết hàng' }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="form-group">
            <label>Hệ điều hành</label>
            <div class="select-with-add">
              <select v-model.number="form.idHeDieuHanh">
                <option :value="null">Chọn hệ điều hành</option>
                <option v-for="hdh in heDieuHanhs" :key="hdh.id" :value="hdh.id">{{ hdh.tenHeDieuHanh }}</option>
              </select>
              <button type="button" class="btn-add" @click="openAddModal('he-dieu-hanh')">
                <img src="@/assets/edit.png" alt="Add" class="btn-icon" />
              </button>
            </div>
          </div>
          <div class="form-group">
            <label>Camera trước</label>
            <div class="select-with-add">
              <select v-model.number="form.idCameraTruoc">
                <option :value="null">Chọn camera trước</option>
                <option v-for="ct in cameraTruocs" :key="ct.id" :value="ct.id">{{ ct.thongSo }}</option>
              </select>
              <button type="button" class="btn-add" @click="openAddModal('camera-truoc')">
                <img src="@/assets/edit.png" alt="Add" class="btn-icon" />
              </button>
            </div>
          </div>
          <div class="form-group">
            <label>Pin</label>
            <div class="select-with-add">
              <select v-model.number="form.idPin">
                <option :value="null">Chọn pin</option>
                <option v-for="p in pins" :key="p.id" :value="p.id">{{ p.dungLuongPin }}</option>
              </select>
              <button type="button" class="btn-add" @click="openAddModal('pin')">
                <img src="@/assets/edit.png" alt="Add" class="btn-icon" />
              </button>
            </div>
          </div>
          
        </div>
        
        <div class="form-column">
          <div class="form-group">
            <label>Danh mục</label>
            <div class="select-with-add">
              <select v-model.number="form.idDanhMuc">
                <option :value="null">Chọn danh mục</option>
                <option v-for="dm in danhMucs" :key="dm.id" :value="dm.id">{{ dm.tenDanhMuc }}</option>
              </select>
              <button type="button" class="btn-add" @click="openAddModal('danh-muc')">
                <img src="@/assets/edit.png" alt="Add" class="btn-icon" />
              </button>
            </div>
          </div>
          <div class="form-group">
            <label>Màn hình</label>
            <div class="select-with-add">
              <select v-model.number="form.idManHinh">
                <option :value="null">Chọn màn hình</option>
                <option v-for="mh in manHinhs" :key="mh.id" :value="mh.id">{{ mh.kichThuoc }} {{ mh.doPhanGiai ? ('- ' + mh.doPhanGiai) : '' }}</option>
              </select>
              <button type="button" class="btn-add" @click="openAddModal('man-hinh')">
                <img src="@/assets/edit.png" alt="Add" class="btn-icon" />
              </button>
            </div>
          </div>
          <div class="form-group">
            <label>Camera sau</label>
            <div class="select-with-add">
              <select v-model.number="form.idCameraSau">
                <option :value="null">Chọn camera sau</option>
                <option v-for="cs in cameraSaus" :key="cs.id" :value="cs.id">{{ cs.thongSo }}</option>
              </select>
              <button type="button" class="btn-add" @click="openAddModal('camera-sau')">
                <img src="@/assets/edit.png" alt="Add" class="btn-icon" />
              </button>
            </div>
          </div>
          <div class="form-group">
            <label>Chip</label>
            <div class="select-with-add">
              <select v-model.number="form.idChip">
                <option :value="null">Chọn chip</option>
                <option v-for="c in chips" :key="c.id" :value="c.id">{{ c.tenChip }}</option>
              </select>
              <button type="button" class="btn-add" @click="openAddModal('chip')">
                <img src="@/assets/edit.png" alt="Add" class="btn-icon" />
              </button>
            </div>
          </div>
          
        </div>
        
        <div class="form-column">
          <div class="form-group">
            <label>Hãng</label>
            <div class="select-with-add">
              <select v-model.number="form.idHang">
                <option :value="null">Chọn hãng</option>
                <option v-for="h in hangs" :key="h.id" :value="h.id">{{ h.ten }}</option>
              </select>
              <button type="button" class="btn-add" @click="openAddModal('hang')">
                <img src="@/assets/edit.png" alt="Add" class="btn-icon" />
              </button>
            </div>
          </div>
          <div class="form-group">
            <label>SIM</label>
            <div class="select-with-add">
              <select v-model.number="form.idSim">
                <option :value="null">Chọn loại SIM</option>
                <option v-for="sim in sims" :key="sim.id" :value="sim.id">
                  {{ sim.loaiSim }}
                </option>
              </select>
              <button type="button" class="btn-add" @click="openAddModal('sim')">
                <img src="@/assets/edit.png" alt="Add" class="btn-icon" />
              </button>
            </div>
          </div>
          <div class="form-group">
            <label>CPU</label>
            <div class="select-with-add">
              <select v-model.number="form.idCpu">
                <option :value="null">Chọn CPU</option>
                <option v-for="cpu in cpus" :key="cpu.id" :value="cpu.id">{{ cpu.tenCpu }}</option>
              </select>
              <button type="button" class="btn-add" @click="openAddModal('cpu')">
                <img src="@/assets/edit.png" alt="Add" class="btn-icon" />
              </button>
            </div>
          </div>
          <div class="form-group">
            <label>GPU</label>
            <div class="select-with-add">
              <select v-model.number="form.idGpu">
                <option :value="null">Chọn GPU</option>
                <option v-for="gpu in gpus" :key="gpu.id" :value="gpu.id">{{ gpu.tenGpu }}</option>
              </select>
              <button type="button" class="btn-add" @click="openAddModal('gpu')">
                <img src="@/assets/edit.png" alt="Add" class="btn-icon" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Mô tả sản phẩm - một hàng riêng biệt -->
    <div class="form-card">
      <div class="form-group">
        <label>Mô tả sản phẩm</label>
        <textarea 
          v-model="form.moTa" 
          placeholder="Nhập mô tả chi tiết về sản phẩm..."
          rows="4"
          class="description-textarea"
        ></textarea>
      </div>
    </div>

    <h2 class="section-title">THÊM PHIÊN BẢN</h2>
    <div class="form-card">
      <div class="form-row">
        <div class="form-group">
          <label>RAM</label>
          <div class="select-with-add">
            <div class="multi-select" @click="openRamDropdown()">
              <span class="multi-select-text">{{ getSelectedRamsText() }}</span>
              <span class="multi-select-arrow">▼</span>
            </div>
            <button type="button" class="btn-add" @click="openAddModal('ram')">
              <img src="@/assets/edit.png" alt="Add" class="btn-icon" />
            </button>
            <div v-if="showRamDropdown" class="multi-select-dropdown">
              <div v-for="r in rams" :key="r.id" class="multi-select-option" @click="toggleRam(r.id)">
                <input type="checkbox" :checked="isRamSelected(r.id)" @change="toggleRam(r.id)" />
                <span>{{ r.tenRam }}</span>
              </div>
            </div>
          </div>
        </div>
        <div class="form-group">
          <label>ROM</label>
          <div class="select-with-add">
            <div class="multi-select" @click="openRomDropdown()">
              <span class="multi-select-text">{{ getSelectedRomsText() }}</span>
              <span class="multi-select-arrow">▼</span>
            </div>
            <button type="button" class="btn-add" @click="openAddModal('rom')">
              <img src="@/assets/edit.png" alt="Add" class="btn-icon" />
            </button>
            <div v-if="showRomDropdown" class="multi-select-dropdown">
              <div v-for="r in roms" :key="r.id" class="multi-select-option" @click="toggleRom(r.id)">
                <input type="checkbox" :checked="isRomSelected(r.id)" @change="toggleRom(r.id)" />
                <span>{{ r.dungLuong }}</span>
              </div>
            </div>
          </div>
        </div>
        <div class="form-group">
          <label>Màu sắc</label>
          <div class="select-with-add">
            <div class="multi-select" @click="openMauSacDropdown()">
              <span class="multi-select-text">{{ getSelectedMauSacsText() }}</span>
              <span class="multi-select-arrow">▼</span>
            </div>
            <button type="button" class="btn-add" @click="openAddModal('mau-sac')">
              <img src="@/assets/edit.png" alt="Add" class="btn-icon" />
            </button>
            <div v-if="showMauSacDropdown" class="multi-select-dropdown">
              <div v-for="m in mauSacs" :key="m.id" class="multi-select-option" @click="toggleMauSac(m.id)">
                <input type="checkbox" :checked="isMauSacSelected(m.id)" @change="toggleMauSac(m.id)" />
                <span>{{ m.tenMau }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="form-row">
        <div class="form-group" style="align-self:end">
          <button class="btn-secondary" @click="addVariant">Thêm phiên bản</button>
          <button class="btn-clear" @click="clearVariantSelections">Xóa lựa chọn</button>
        </div>
      </div>
      
      <div v-if="variants.length" class="variants-display">
        <div v-for="group in groupedVariants" :key="group.key" class="variant-group">
        <div class="variant-group-header">
          <h3>PHIÊN BẢN {{ group.ramName }}/{{ group.romName }}</h3>
          <div class="price-input-section">
            <input 
              v-model="group.priceInput" 
              placeholder="Nhập giá trị" 
              class="price-input"
              type="number"
            />
            <button class="btn-apply-price" @click="applyPriceToAll(group.key)">Áp dụng</button>
            <button class="btn-delete-group" @click="deleteVariantGroup(group.key)">
              <img src="@/assets/delete.png" alt="Delete" class="btn-icon" />
              Xóa nhóm
            </button>
          </div>
        </div>
          <div class="variants-table">
            <table>
              <thead>
                <tr>
                  <th>STT</th>
                  <th>TÊN SẢN PHẨM</th>
                  <th>MÀU SẮC</th>
                  <th>SỐ LƯỢNG</th>
                  <th>ĐƠN GIÁ</th>
                  <th>GIÁ NHẬP</th>
                  <th>GHI CHÚ</th>
                  <th>THAO TÁC</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(v, i) in group.variants" :key="i" class="variant-row">
                  <td>{{ i + 1 }}</td>
                  <td>{{ form.tenSanPham || `Sản phẩm ${group.ramName}/${group.romName}` }}</td>
                  <td>
                    <div class="color-indicator" :style="{ backgroundColor: getColorCode(v.idMauSac) }"></div>
                    {{ getAttributeName(v.idMauSac, mauSacs, 'tenMau') }}
                  </td>
                  <td>
                    <div class="quantity-display">
                      <span class="imei-count">
                        {{ (v.imeis && v.imeis.length > 0) ? v.imeis.length : 0 }} IMEI
                      </span>
                    </div>
                  </td>
                  <td>
                    <input 
                      v-model="variants[v.originalIndex].donGia" 
                      type="number" 
                      min="0" 
                      step="1000" 
                      class="price-input"
                      placeholder="Nhập đơn giá"
                    />
                  </td>
                  <td>
                    <input 
                      v-model="variants[v.originalIndex].giaNhap" 
                      type="number" 
                      min="0" 
                      step="1000" 
                      class="price-input"
                      placeholder="Nhập giá nhập"
                    />
                  </td>
                  <td>
                    <input 
                      v-model="variants[v.originalIndex].ghiChu" 
                      type="text" 
                      class="note-input"
                      placeholder="Nhập ghi chú"
                    />
                  </td>
                  <td>
                    <div class="variant-actions">
                      <button class="btn-delete" @click="removeVariantFromGroup(group.key, i)">
                        <img src="@/assets/delete.png" alt="Delete" class="btn-icon" />
                      </button>
                      <input 
                        type="file" 
                        :id="`excelFile-${v.originalIndex}`"
                        @change="handleExcelUpload(v.originalIndex, $event)"
                        accept=".xlsx,.xls"
                        class="excel-file-input"
                        style="display: none"
                      />
                      <button class="btn-import-excel" @click="importFromExcel(v.originalIndex)">Nhập</button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <!-- Image Upload Section by Color -->
    <div v-if="variants.length" class="image-upload-section">
      <h2 class="section-title">📷 THÊM HÌNH ẢNH THEO MÀU SẮC</h2>
      <div class="color-image-grid">
        <div v-for="colorGroup in colorImageGroups" :key="colorGroup.colorId" class="color-image-card">
          <div class="card-header">
            <h3 class="color-title">
              <span class="color-indicator" :style="{ backgroundColor: getColorCode(colorGroup.colorId) }"></span>
              {{ colorGroup.colorName }}
            </h3>
            <span class="variant-count">{{ colorGroup.variants.length }} phiên bản</span>
          </div>
          <div class="card-content">
            <!-- Preview existing images -->
            <div v-if="colorGroup.images && colorGroup.images.length > 0" class="image-preview-grid">
              <div v-for="(image, imageIndex) in colorGroup.images" :key="imageIndex" class="image-preview-item">
                <img :src="createFullImageUrl(image)" :alt="colorGroup.colorName" />
                <button class="remove-image-btn" @click="removeColorImage(colorGroup.colorId, imageIndex)">×</button>
              </div>
            </div>
            
            <!-- Empty state placeholder -->
            <div v-else class="empty-image-placeholder">
              Chưa có ảnh cho màu {{ colorGroup.colorName }}
            </div>
            
            <!-- Upload button -->
            <div class="upload-section">
              <input 
                type="file" 
                :id="`colorImageUpload-${colorGroup.colorId}`"
                @change="handleColorImageUpload(colorGroup.colorId, $event)"
                accept="image/*"
                style="display: none"
              />
              <button 
                class="upload-btn" 
                @click="triggerColorFileInput(colorGroup.colorId)"
              >
                📷 Thêm ảnh cho {{ colorGroup.colorName }}
              </button>
              <p class="upload-status">Sẵn sàng tải lên</p>
              <p class="upload-info">Nhiều ảnh/màu sắc • Tối đa 10MB/ảnh</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="form-actions" style="margin-top:12px">
      <button class="btn-cancel" @click="router.push({name: 'san-pham'})">Hủy</button>
      <button class="btn-primary" :disabled="isCreate ? !canSaveCreate : !canSaveUpdate" @click="save">
        {{ isCreate ? 'Xác nhận' : 'Lưu cập nhật' }}
      </button>
    </div>
  </div>

  <!-- Modal thêm mới -->
  <div v-if="showModal" class="modal">
    <div class="modal-content">
      <div class="modal-header">
        <h3>{{ modalTitle }}</h3>
        <button class="modal-close" @click="closeModal">×</button>
      </div>
      <div class="modal-body">
        <div v-for="(value, field) in modalInputs" :key="field" class="form-group">
          <label>{{ getFieldLabel(field as string) }}</label>
          <input 
            v-model="modalInputs[field]" 
            :placeholder="`Nhập ${getFieldLabel(field as string).toLowerCase()}`"
            @keyup.enter="saveModal"
            :ref="(field as string) === 'ten' || (field as string) === 'tenDanhMuc' || (field as string) === 'tenHeDieuHanh' || (field as string) === 'tenChip' || (field as string) === 'tenCpu' || (field as string) === 'tenGpu' || (field as string) === 'tenRam' || (field as string) === 'tenMau' ? 'modalInputRef' : null"
          />
        </div>
      </div>
      <div class="modal-footer">
        <button class="btn-secondary" @click="closeModal">Đóng</button>
        <button 
          class="btn-primary" 
          @click="saveModal" 
          :disabled="!isModalValid() || modalLoading"
        >
          {{ modalLoading ? 'Đang thêm...' : 'Xác nhận' }}
        </button>
      </div>
    </div>
  </div>

  <!-- Modal nhập IMEI -->
  <div v-if="showImeiModal" class="modal">
    <div class="modal-content imei-modal">
      <div class="modal-header">
        <h3>Nhập IMEI cho biến thể {{ getCurrentVariantName() }}</h3>
        <button class="modal-close" @click="closeImeiModal">×</button>
      </div>
      <div class="modal-body">
        <!-- Nhập IMEI Section -->
        <div class="imei-input-section">
          <h4>Nhập IMEI</h4>
          <textarea 
            v-model="imeiInput" 
            placeholder="Nhập IMEI, mỗi IMEI trên một dòng, đúng 15 chữ số..."
            rows="4"
            class="imei-textarea"
          ></textarea>
        </div>

        <!-- Danh sách IMEI Section -->
        <div class="imei-list-section">
          <div class="imei-list-header">
            <h4>Danh sách IMEI: {{ getTotalImeiCount() }} IMEI</h4>
            <div v-if="selectedImeis.size > 0" class="imei-bulk-actions">
              <button class="btn-delete-selected" @click="deleteSelectedImeis">
                <img src="@/assets/delete.png" alt="Delete" class="btn-icon" />
                Xóa đã chọn ({{ selectedImeis.size }})
              </button>
            </div>
          </div>
          <div v-if="getTotalImeiCount() === 0" class="no-imei-message">
            Chưa có IMEI nào được nhập
          </div>
          <div v-else class="imei-list">
            <!-- Hiển thị IMEI đang nhập (chưa lưu) -->
            <div v-for="(imei, index) in getInputImeis()" :key="`input-${index}`" class="imei-item" :class="getImeiValidationClass(imei)">
              <div class="imei-checkbox">
                <input 
                  type="checkbox" 
                  :id="`imei-checkbox-${index}`"
                  :value="imei"
                  v-model="selectedImeisArray"
                  class="imei-checkbox-input"
                />
                <label :for="`imei-checkbox-${index}`" class="imei-checkbox-label"></label>
              </div>
              <span class="imei-text">{{ imei }}</span>
              <div class="imei-actions">
                <span class="imei-status" :class="getImeiStatusClass(imei)">
                  {{ getImeiValidationText(imei) }}
                </span>
                <button class="btn-remove-imei-input" @click="removeImeiFromInput(index)" title="Xóa IMEI">
                  ×
                </button>
              </div>
            </div>
            <!-- Hiển thị IMEI đã lưu -->
            <div v-for="(imei, index) in getCurrentImeis()" :key="`saved-${index}`" class="imei-item imei-item-saved">
              <span class="imei-text">{{ imei }}</span>
              <button class="btn-remove-imei" @click="removeImeiFromList(index)" title="Xóa IMEI">
                ×
              </button>
            </div>
          </div>
        </div>

        <!-- Import from Excel Section -->
        <div class="excel-import-section">
          <div class="excel-header">
            <h4>Nhập từ file Excel</h4>
            <h4 class="excel-header">
              Tải mẫu Excel
            </h4>
          </div>
          <div class="file-input-container">
            <input 
              type="file" 
              ref="excelFileInput"
              @change="handleExcelFileUpload"
              accept=".xlsx,.xls"
              class="excel-file-input"
            />
            <button class="btn-choose-file" @click="triggerExcelFileInput">
              Choose File
            </button>
            <span class="file-name">{{ selectedFileName || 'No file chosen' }}</span>
            <button class="btn-download-imei-template" @click="downloadImeiTemplate">
              Tải mẫu IMEI
            </button>
          </div>
        </div>
      </div>
      <div class="modal-footer">
        <button class="btn-secondary" @click="clearImeiInput">Xóa tất cả</button>
        <button class="btn-secondary" @click="closeImeiModal">Đóng</button>
            <button 
              class="btn-primary" 
              @click="saveImei" 
              :disabled="!hasValidImeiToSave()"
            >
              Lưu
            </button>
      </div>
    </div>
  </div>

  <!-- Confirm Modal for IMEI duplicates -->
  <ConfirmModal
    :show="showConfirmModal"
    :title="confirmTitle"
    :message="confirmMessage"
    @confirm="handleConfirm"
    @cancel="handleCancel"
  />
</template>

<style scoped>
.page { padding: 20px; }
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.page-title { margin: 0; }
.breadcrumb { color: #6c757d; margin-bottom: 10px; }
.form-card { background: #fff; border: 1px solid #eee; border-radius: 8px; padding: 16px; margin-bottom: 12px; }
.section-title { margin: 12px 0; text-transform: uppercase; letter-spacing: .5px; }
.form-row { 
  display: flex; 
  gap: 12px; 
  margin: 8px 0; 
  align-items: flex-start;
  position: relative;
  overflow: visible;
  flex-wrap: wrap;
}

.form-columns {
  display: flex;
  gap: 20px;
  margin: 8px 0;
  align-items: flex-start;
  position: relative;
  overflow: visible;
}

.form-column {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 200px;
  max-width: calc(33.333% - 14px);
  position: relative;
  overflow: visible;
}

.form-group { 
  flex: 1; 
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 200px;
  max-width: 100%;
  position: relative;
  overflow: visible;
}
.form-group input, .form-group select, .form-group textarea { 
  width: 100%; 
  padding: 8px 12px; 
  border: 1px solid #ddd; 
  border-radius: 4px; 
  font-size: 14px;
}
.form-group input:focus, .form-group select:focus, .form-group textarea:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 2px rgba(0,123,255,0.25);
}

.description-textarea {
  resize: vertical;
  min-height: 80px;
  font-family: inherit;
  line-height: 1.5;
}

/* Search container styles */
.search-container {
  position: relative;
  width: 100%;
}

.search-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #ddd;
  border-top: none;
  border-radius: 0 0 4px 4px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  z-index: 1000;
  max-height: 300px;
  overflow-y: auto;
  margin-top: -1px; /* Để border liền mạch với input */
}

.search-header {
  padding: 8px 12px;
  background: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
  font-size: 12px;
  font-weight: 600;
  color: #6c757d;
}

.search-item {
  padding: 10px 12px;
  cursor: pointer;
  border-bottom: 1px solid #f1f3f4;
  transition: background-color 0.2s;
}

.search-item:hover {
  background: #f8f9fa;
}

.search-item:last-child {
  border-bottom: none;
}

.search-item .product-name {
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.search-item .product-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}

.search-item .brand {
  color: #6c757d;
}

.search-item .status {
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 500;
}

.search-item .status.in-stock {
  background: #d4edda;
  color: #155724;
}

.search-item .status.out-of-stock {
  background: #f8d7da;
  color: #721c24;
}

.search-item mark {
  background: #fff3cd;
  color: #856404;
  padding: 1px 2px;
  border-radius: 2px;
  font-weight: 600;
}
.btn-primary { 
  background: linear-gradient(135deg, #ff6b35 0%, #fd7e14 100%);
  color: white;
  border: none;
  border-radius: 8px;
  padding: 12px 24px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 6px -1px rgba(255, 107, 53, 0.3);
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 15px -3px rgba(255, 107, 53, 0.4);
}

.btn-primary:disabled {
  background: #6c757d;
  color: #fff;
  cursor: not-allowed;
  opacity: 0.6;
  transform: none;
}

.btn-primary:disabled:hover {
  background: #6c757d;
  transform: none;
}

/* Button Icon Styles */
.btn-icon {
  width: 16px;
  height: 16px;
  margin-right: 4px;
  vertical-align: middle;
}
.btn-secondary { 
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  border: none;
  border-radius: 8px;
  padding: 12px 24px;
  cursor: pointer;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(16, 185, 129, 0.3);
}
.btn-secondary:hover:not(:disabled) {
  background: linear-gradient(135deg, #059669 0%, #047857 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(16, 185, 129, 0.4);
}

.btn-clear { 
  background: linear-gradient(135deg, #2c2c2c, #1a1a1a);
  color: #fff; 
  border: none; 
  border-radius: 8px; 
  padding: 12px 20px; 
  cursor: pointer; 
  margin-left: 12px;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}
.btn-clear:hover {
  background: linear-gradient(135deg, #1a1a1a, #0d0d0d);
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.4);
}

.btn-cancel { 
  background: linear-gradient(135deg, #2c2c2c, #1a1a1a);
  color: #fff; 
  border: none; 
  border-radius: 8px; 
  padding: 12px 20px; 
  cursor: pointer; 
  margin-right: 12px;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}
.btn-cancel:hover {
  background: linear-gradient(135deg, #1a1a1a, #0d0d0d);
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.4);
}
.btn-delete { 
  background:#e74c3c; 
  color:#fff; 
  border:none; 
  border-radius:6px; 
  padding:4px 8px; 
  cursor:pointer; 
  transition: all 0.2s ease;
}

.btn-delete:hover {
  background:#c0392b;
  transform: scale(1.05);
}
.btn-add { 
  background: linear-gradient(135deg, #fb923c, #f97316);
  color:#fff; 
  border:none; 
  border-radius:4px; 
  padding:4px 8px; 
  cursor:pointer; 
  margin-left:4px; 
  transition: all 0.2s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.btn-add:hover {
  background: linear-gradient(135deg, #ea580c, #dc2626);
  transform: scale(1.05);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}
.select-with-add { 
  display: flex; 
  align-items: center; 
  width: 100%;
  gap: 8px;
  min-width: 0;
  flex: 1;
  position: relative;
  overflow: visible;
}

.select-with-add .multi-select {
  flex: 1;
  min-width: 200px;
  max-width: 100%;
}
.variants-display { margin-top: 20px; }
.variant-group { margin-bottom: 20px; border: 1px solid #eee; border-radius: 8px; overflow: hidden; }

/* Color Image Grid */
.color-image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.color-image-card {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  overflow: hidden;
  background: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.color-image-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.color-image-card .card-header {
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  padding: 16px;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.color-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.variant-count {
  background: #3b82f6;
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.image-preview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 8px;
  padding: 16px;
}
.variant-group-header { 
  display: flex; 
  justify-content: space-between; 
  align-items: center; 
  padding: 12px 16px; 
  background: #f8f9fa; 
  border-bottom: 1px solid #eee; 
}
.variant-group-header h3 { margin: 0; font-size: 16px; font-weight: 600; }
.search-input { 
  padding: 6px 12px; 
  border: 1px solid #ddd; 
  border-radius: 4px; 
  width: 200px; 
  font-size: 14px; 
}
.variants-table { overflow-x: auto; }
.variants-table table { width: 100%; border-collapse: collapse; }
.variants-table th, .variants-table td { 
  padding: 12px 8px; 
  text-align: left; 
  white-space: nowrap;
}
.variants-table th { background: #f8f9fa; font-weight: 600; }

/* Column widths */
.variants-table th:nth-child(1), .variants-table td:nth-child(1) { width: 40px; text-align: center;} /* STT */
.variants-table th:nth-child(2), .variants-table td:nth-child(2) { width: 50px; min-width: 80px; text-align: center;} /* TÊN SẢN PHẨM */
.variants-table th:nth-child(3), .variants-table td:nth-child(3) { 
  width: 50px; 
  min-width:100px; 
} /* MÀU SẮC */
.variants-table th:nth-child(4), .variants-table td:nth-child(4) { width: 80px; min-width: 70px; text-align: center;} /* SỐ LƯỢNG */
.variants-table th:nth-child(5), .variants-table td:nth-child(5) { width: 80px; min-width: 70px; text-align: center;} /* ĐƠN GIÁ */
.variants-table th:nth-child(6), .variants-table td:nth-child(6) { width: 80px; min-width: 70px; text-align: center;} /* GIÁ NHẬP */
.variants-table th:nth-child(7), .variants-table td:nth-child(7) { width: 100px; min-width: 80px; text-align: center;} /* GHI CHÚ */
.variants-table th:nth-child(8), .variants-table td:nth-child(8) { width: 80px; min-width: 70px; text-align: center;} /* THAO TÁC */
.variant-row td { vertical-align: middle; }
.variants-table .color-indicator { 
  display: inline-block; 
  width: 20px; 
  height: 20px; 
  border-radius: 50%; 
  margin-right: 8px;
  border: 2px solid #e0e0e0;
  box-shadow: 0 1px 3px rgba(0,0,0,0.2); 
  vertical-align: middle;
}

/* Color cell styling */
.variants-table td:nth-child(3) {
  align-items: center;
  gap: 8px;
}
.quantity-input, .price-input { 
  width: 80px; 
  padding: 4px 8px; 
  border: 1px solid #ddd; 
  border-radius: 4px; 
  font-size: 14px; 
}
.variant-actions { 
  display: flex; 
  gap: 4px; 
  align-items: center; 
  flex-wrap: wrap;
}
.file-input { 
  display: none; 
}
.btn-upload, .btn-import { 
  background: #dc3545; 
  color: white; 
  border: none; 
  border-radius: 4px; 
  padding: 4px 8px; 
  font-size: 12px; 
  cursor: pointer; 
}
.btn-refresh { 
  background: linear-gradient(135deg, #6b7280 0%, #4b5563 100%);
  color: white; 
  border: none; 
  border-radius: 8px; 
  padding: 12px 20px; 
  cursor: pointer; 
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.btn-refresh:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

/* Multi-select styles */
.multi-select {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
  cursor: pointer;
  height: 40px;
  width: 100%;
  min-width: 200px;
  max-width: 100%;
  box-sizing: border-box;
  font-size: 14px;
  flex: 1;
  overflow: visible;
}

.multi-select:hover {
  border-color: #007bff;
}

.multi-select-text {
  flex: 1;
  color: #333;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  min-width: 0;
}

.multi-select-arrow {
  color: #666;
  font-size: 12px;
  transition: transform 0.2s;
  margin-left: 8px;
}

.multi-select-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #ddd;
  border-top: none;
  border-radius: 0 0 6px 6px;
  box-shadow: 0 6px 20px rgba(0,0,0,0.15);
  z-index: 1000;
  max-height: 250px;
  overflow-y: auto;
  margin-top: -1px;
  min-width: 200px;
  width: 100%;
  animation: slideDown 0.2s ease-out;
}

/* Custom scrollbar for dropdown */
.multi-select-dropdown::-webkit-scrollbar {
  width: 6px;
}

.multi-select-dropdown::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.multi-select-dropdown::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.multi-select-dropdown::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.multi-select-option {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  border-bottom: 1px solid #f0f0f0;
  position: relative;
}

.multi-select-option:last-child {
  border-bottom: none;
  border-radius: 0 0 6px 6px;
}

.multi-select-option:hover {
  background-color: #f8f9fa;
  transform: translateX(2px);
}

.multi-select-option:active {
  background-color: #e9ecef;
  transform: translateX(1px);
}

.multi-select-option input[type="checkbox"] {
  margin-right: 12px;
  cursor: pointer;
  width: 18px;
  height: 18px;
  accent-color: #dc3545;
  border-radius: 3px;
  transition: all 0.2s ease;
}

.multi-select-option input[type="checkbox"]:hover {
  transform: scale(1.1);
}

.multi-select-option input[type="checkbox"]:checked {
  background-color: #dc3545;
  border-color: #dc3545;
  box-shadow: 0 0 0 2px rgba(220, 53, 69, 0.2);
}

.multi-select-option span {
  flex: 1;
  color: #333;
  font-size: 14px;
  font-weight: 400;
  line-height: 1.4;
  user-select: none;
}


/* Dropdown animation */
@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Focus states */
.multi-select:focus-within {
  border-color: #007bff;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
}

.multi-select:focus-within .multi-select-arrow {
  transform: rotate(180deg);
}

/* Selected state indicator */
.multi-select-option.selected {
  background-color: #e3f2fd;
  border-left: 3px solid #2196f3;
}

.multi-select-option.selected:hover {
  background-color: #bbdefb;
}

/* Price input section */
.price-input-section {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f8f9fa;
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid #e9ecef;
}

.price-input {
  flex: 1;
  padding: 6px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  min-width: 200px;
}

.price-input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.1);
}


.btn-apply-price {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  border: none;
  border-radius: 6px;
  padding: 8px 16px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(16, 185, 129, 0.3);
}

.btn-apply-price:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(16, 185, 129, 0.4);
}

.btn-delete-group {
  background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
  color: white;
  border: none;
  border-radius: 6px;
  padding: 8px 16px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(231, 76, 60, 0.3);
  display: flex;
  align-items: center;
  gap: 4px;
}

.btn-delete-group:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(231, 76, 60, 0.4);
}


.btn-import-excel {
  background: #6f42c1;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 4px 6px;
  cursor: pointer;
  font-size: 10px;
  margin-left: 4px;
  transition: all 0.2s;
}

.btn-import-excel:hover {
  background: #5a32a3;
  transform: scale(1.05);
}

.btn-danger {
  background: #dc3545;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 8px 16px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
}

.btn-danger:hover {
  background: #c82333;
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(220, 53, 69, 0.3);
}

.excel-file-input {
  display: none;
}

.btn-download-template {
  background: #28a745;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  margin-top: 10px;
  transition: all 0.2s;
}

.btn-download-template:hover {
  background: #218838;
  transform: scale(1.05);
}

/* IMEI Modal styles */
.imei-modal {
  width: 500px;
  min-height: 500px;
  max-width: 90vw;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}

.imei-modal .modal-footer {
  flex-shrink: 0;
  margin-top: auto;
}

.imei-modal .modal-body {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow: hidden;
}

.imei-input-section {
  flex: 1;
}

.imei-input-section h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.imei-textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.4;
  resize: none;
  height: 100px;
}

.imei-textarea:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.1);
}

.imei-list-section {
  flex: 1;
}

.imei-list-section h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.imei-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.imei-bulk-actions {
  display: flex;
  gap: 10px;
}

.btn-delete-selected {
  background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
  color: white;
  border: none;
  border-radius: 6px;
  padding: 8px 16px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(231, 76, 60, 0.3);
  display: flex;
  align-items: center;
  gap: 6px;
}

.btn-delete-selected:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(231, 76, 60, 0.4);
}

.imei-checkbox {
  display: flex;
  align-items: center;
  margin-right: 8px;
}

.imei-checkbox-input {
  margin: 0;
  cursor: pointer;
}

.imei-checkbox-label {
  margin-left: 4px;
  cursor: pointer;
  font-size: 12px;
  color: #666;
}

.no-imei-message {
  padding: 15px;
  text-align: center;
  color: #6c757d;
  font-style: italic;
  background: #f8f9fa;
  border-radius: 4px;
  border: 1px dashed #dee2e6;
  font-size: 13px;
}

.imei-list {
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  background: #f8f9fa;
}

.imei-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  border-bottom: 1px solid #e9ecef;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  color: #495057;
  background: white;
  margin: 2px;
  border-radius: 3px;
  transition: all 0.2s;
}

.imei-item:hover {
  background: #f8f9fa;
  transform: translateX(2px);
}

.imei-item:last-child {
  border-bottom: none;
}

.imei-item-valid {
  background: #d4edda;
  border-left: 3px solid #28a745;
}

.imei-item-invalid {
  background: #f8d7da;
  border-left: 3px solid #dc3545;
}

.imei-item-duplicate {
  background: #fff3cd;
  border-left: 3px solid #ffc107;
}

.imei-item-db-duplicate {
  background: #f8d7da;
  border-left: 3px solid #dc3545;
}

.imei-item-saved {
  background: #d1ecf1;
  border-left: 3px solid #17a2b8;
}

.imei-item-checking {
  background: #e2e3e5;
  border-left: 3px solid #6c757d;
}

.imei-item-pending {
  background: #f8f9fa;
  border-left: 3px solid #6c757d;
}

.imei-text {
  flex: 1;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  color: #495057;
}

.imei-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.imei-status {
  font-size: 10px;
  font-style: italic;
  padding: 2px 6px;
  border-radius: 10px;
  margin-left: 8px;
  font-weight: 500;
}

.imei-status-valid {
  color: #155724;
  background: #c3e6cb;
}

.imei-status-invalid {
  color: #721c24;
  background: #f5c6cb;
}

.imei-status-duplicate {
  color: #856404;
  background: #ffeaa7;
}

.imei-status-db-duplicate {
  color: #721c24;
  background: #f5c6cb;
}

.imei-status-checking {
  color: #495057;
  background: #e9ecef;
}

.imei-status-pending {
  color: #6c757d;
  background: #f8f9fa;
}

.btn-remove-imei {
  background: #dc3545;
  color: white;
  border: none;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  font-size: 14px;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  margin-left: 8px;
  flex-shrink: 0;
}

.btn-remove-imei:hover {
  background: #c82333;
  transform: scale(1.1);
}

.btn-remove-imei:active {
  transform: scale(0.95);
}

.btn-remove-imei-input {
  background: #6c757d;
  color: white;
  border: none;
  border-radius: 50%;
  width: 18px;
  height: 18px;
  font-size: 12px;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  flex-shrink: 0;
}

.btn-remove-imei-input:hover {
  background: #5a6268;
  transform: scale(1.1);
}

.btn-remove-imei-input:active {
  transform: scale(0.95);
}

.excel-import-section {
  flex: 1;
}

.excel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.excel-header h4 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.file-input-container {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.excel-file-input {
  display: none;
}

.btn-choose-file {
  background: #28a745;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-choose-file:hover {
  background: #218838;
}

.file-name {
  color: #6c757d;
  font-size: 12px;
  min-width: 100px;
  border: 1px solid #ddd;
  padding: 6px 12px;
  border-radius: 4px;
  padding-right: 160px;
}

.btn-download-template,
.btn-download-imei-template {
  background: #17a2b8;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-download-template:hover,
.btn-download-imei-template:hover {
  background: #138496;
}


/* Input styling for quantity and price */
.quantity-input, .price-input, .note-input {
  width: 100%;
  padding: 6px 8px;
  border: 2px solid #007bff;
  border-radius: 4px;
  font-size: 14px;
  text-align: center;
  background-color: #f8f9ff;
  transition: all 0.2s;
}

.quantity-display {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 32px;
}

.imei-count {
  background: #28a745;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  text-align: center;
}

.quantity-input:focus, .price-input:focus, .note-input:focus {
  outline: none;
  border-color: #0056b3;
  background-color: white;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
}

.quantity-input::placeholder, .price-input::placeholder, .note-input::placeholder {
  color: #6c757d;
  font-style: italic;
}

/* Modal styles */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal-content {
  background: white;
  border-radius: 8px;
  width: 500px;
  max-width: 90vw;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
}
.modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}
.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.modal-body {
  padding: 20px;
}
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #eee;
  flex-shrink: 0;
}

/* Image Upload Styles */
.image-upload-section {
  margin-top: 24px;
}

.image-upload-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  margin-top: 16px;
  justify-content: flex-start;
}

.image-upload-card {
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 24px;
  background: white;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  min-width: 320px;
  max-width: 400px;
  flex: 1;
}

.image-upload-card:hover {
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
  transform: translateY(-2px);
}

.card-header {
  margin-bottom: 20px;
  text-align: center;
}

.card-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
  padding: 12px 0;
  border-bottom: 2px solid #f3f4f6;
  letter-spacing: 0.5px;
}

.variant-title-with-color {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
}

.variant-info {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
}

.variant-color {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #555;
}

.variant-color .color-indicator {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid #ddd;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.card-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.image-preview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
  width: 100%;
}

.image-preview-single {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
  width: 100%;
}

.image-preview-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 12px;
  overflow: hidden;
  border: 2px solid #e5e7eb;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  max-width: 200px;
  width: 100%;
}

.image-preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-image-btn {
  position: absolute;
  top: 6px;
  right: 6px;
  background: rgba(239, 68, 68, 0.9);
  color: white;
  border: none;
  border-radius: 50%;
  width: 28px;
  height: 28px;
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.remove-image-btn:hover {
  background: rgba(239, 68, 68, 1);
  transform: scale(1.1);
}

.upload-section {
  text-align: center;
  padding: 24px;
  border: 2px dashed #d1d5db;
  border-radius: 12px;
  background: #f9fafb;
  transition: all 0.3s ease;
  width: 100%;
}

.upload-section:hover {
  border-color: #10b981;
  background: #f0fdf4;
}

.upload-btn {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  border: none;
  padding: 14px 28px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 12px;
  box-shadow: 0 4px 6px -1px rgba(16, 185, 129, 0.3);
  letter-spacing: 0.5px;
}

.upload-btn:hover {
  background: linear-gradient(135deg, #059669 0%, #047857 100%);
  transform: translateY(-1px);
  box-shadow: 0 6px 8px -1px rgba(16, 185, 129, 0.4);
}

.upload-btn:active {
  transform: translateY(0);
}

.upload-status {
  margin: 0;
  color: #10b981;
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 0.3px;
}

.upload-info {
  margin: 4px 0 0 0;
  color: #6b7280;
  font-size: 12px;
  font-weight: 400;
}

/* Placeholder for empty state */
.empty-image-placeholder {
  width: 100%;
  height: 200px;
  background: linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b7280;
  font-size: 16px;
  font-weight: 500;
  border: 2px dashed #d1d5db;
  margin-bottom: 16px;
}

.empty-image-placeholder:hover {
  border-color: #10b981;
  color: #10b981;
}

/* Responsive design for 3-column layout */
@media (max-width: 1200px) {
  .form-column {
    max-width: calc(50% - 10px);
  }
  
  .form-columns {
    flex-wrap: wrap;
  }
}

@media (max-width: 768px) {
  .form-column {
    max-width: 100%;
  }
  
  .form-columns {
    flex-direction: column;
  }
  
  .form-row {
    flex-direction: column;
  }
}
</style>


