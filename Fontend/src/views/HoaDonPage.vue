<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '@/services/api'
import Toast from '@/components/Toast.vue'
import PosHeader from '@/components/PosHeader.vue'
import jsPDF from 'jspdf'
import html2canvas from 'html2canvas'
import QRCode from 'qrcode'
import QrScanner from 'qr-scanner'
import * as XLSX from 'xlsx'

const router = useRouter()
const route = useRoute()

interface HoaDon {
  id: number
  maHoaDon: string
  tenKhachHang?: string
  soDienThoai?: string
  loaiHoaDon: string
  tongTien: number
  tongTienSauGiam?: number
  trangThai: number
  ngayTao: string
  phieuGiamGiaId?: number
  ghiChu?: string
}

// Reactive data
const activeTab = ref('list')
const hoaDons = ref<HoaDon[]>([])
const loading = ref(false)
const toastRef = ref(null)

const showDetailsModal = ref(false)
const selectedHoaDon = ref<HoaDon | null>(null)
const currentPage = ref(1)
const pageSize = ref(5)
const totalPages = ref(0)
const totalElements = ref(0)
const updatingStatus = ref<number | null>(null)
const isUpdatingStatus = ref(false)

// Tracking
const trackingCode = ref('')
const trackingData = ref<any>(null)
const trackingSearched = ref(false)

// QR Scanner state
const isQRScannerActive = ref(false)
const qrScanner = ref(null)
const currentQRScanner = ref(null)
const showQRScannerModal = ref(false)

// Update Status Modal
const showUpdateStatusModal = ref(false)
const newStatus = ref<number>(0)
const trackingSteps = ref([
  { title: 'Chờ xác nhận', icon: 'clock', time: '', status: 0, isBranch: true },
  { title: 'Chờ giao hàng', icon: 'box', time: '', status: 2, isBranch: false },
  { title: 'Đang giao hàng', icon: 'truck', time: '', status: 3, isBranch: false },
  { title: 'Hoàn thành', icon: 'check-circle', time: '', status: 4, isBranch: false }
])

// Computed property để lấy steps phù hợp với loại đơn hàng
const getTrackingSteps = computed(() => {
  if (!trackingData.value || !trackingData.value.loaiHoaDon) {
    return trackingSteps.value
  }

  const isBanThuong = trackingData.value.loaiHoaDon === 'BAN_THUONG' || trackingData.value.loaiHoaDon === 'NORMAL'

  if (isBanThuong) {
    // Bán tại quầy: chỉ có 1 bước "Hoàn thành" và luôn ở trạng thái hoàn thành
    // Tự động set thời gian hoàn thành = ngày giờ tạo hóa đơn
    let completionTime = ''
    if (trackingData.value.ngayTao) {
      const ngayTao = new Date(trackingData.value.ngayTao)
      completionTime = ngayTao.toLocaleTimeString('vi-VN') + ' ' + ngayTao.toLocaleDateString('vi-VN')
    } else {
      // Fallback: sử dụng thời gian hiện tại nếu không có ngayTao
      const now = new Date()
      completionTime = now.toLocaleTimeString('vi-VN') + ' ' + now.toLocaleDateString('vi-VN')
      console.log('⚠️ BAN_THUONG: No ngayTao found, using current time:', completionTime)
    }

    console.log('🔍 BAN_THUONG: Auto setting completion time:', completionTime)
    console.log('🔍 BAN_THUONG: trackingData.ngayTao:', trackingData.value.ngayTao)

    const step = {
      title: 'Hoàn thành',
      icon: 'check-circle',
      time: completionTime,
      status: 4,
      isBranch: true,
      isCompleted: true // Đánh dấu là đã hoàn thành
    }

    console.log('🔍 BAN_THUONG: Final step object:', step)

    return [step]
  } else {
    // Bán online: giữ nguyên 4 bước
    return trackingSteps.value
  }
})

// Payment method modal
const showPaymentModal = ref(false)
const currentStep = ref<any>(null)

// Confirm modal
const showConfirmModal = ref(false)
const confirmMessage = ref('')
const confirmCallback = ref<() => Promise<void>>()

// IMEI modal
const showImeiModal = ref(false)
const selectedProductForImei = ref<any>(null)


// Search and filter
const searchKeyword = ref('')
const showFilters = ref(false)
const filterTrangThai = ref('')
const filterLoaiHoaDon = ref('')
const dateFrom = ref('')
const dateTo = ref('')
const sortBy = ref('ngayTao')
const sortDirection = ref('desc')
const quickSort = ref('ngayTao_desc')

// Form data

// Status options
const statusOptions = [
  { value: 0, label: 'Chờ xác nhận', color: '#ffc107' },
  { value: 1, label: 'Đã thanh toán chờ xác nhận', color: '#6f42c1' },
  { value: 2, label: 'Chờ giao hàng', color: '#17a2b8' },
  { value: 3, label: 'Đang giao', color: '#ff6b35' },
  { value: 4, label: 'Hoàn thành', color: '#28a745' },
  { value: 5, label: 'Đã hủy', color: '#dc3545' }
]

// Sort options
const sortOptions = [
  { value: 'ngayTao', label: 'Ngày tạo' },
  { value: 'tongTien', label: 'Tổng tiền' }
]

const sortDirectionOptions = [
  { value: 'desc', label: 'Giảm dần' },
  { value: 'asc', label: 'Tăng dần' }
]

// Quick sort options for common use cases
const quickSortOptions = [
  { value: 'ngayTao_desc', label: 'Mới nhất', sortBy: 'ngayTao', direction: 'desc' },
  { value: 'ngayTao_asc', label: 'Cũ nhất', sortBy: 'ngayTao', direction: 'asc' },
  { value: 'tongTien_desc', label: 'Tổng tiền cao nhất', sortBy: 'tongTien', direction: 'desc' },
  { value: 'tongTien_asc', label: 'Tổng tiền thấp nhất', sortBy: 'tongTien', direction: 'asc' }
]

// Computed
const filteredHoaDons = computed(() => {
  let filtered = hoaDons.value

  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(hd =>
      hd.maHoaDon.toLowerCase().includes(keyword) ||
      (hd.tenKhachHang && hd.tenKhachHang.toLowerCase().includes(keyword)) ||
      (hd.soDienThoai && hd.soDienThoai.includes(keyword))
    )
  }

  if (filterTrangThai.value !== '') {
    filtered = filtered.filter(hd => hd.trangThai === parseInt(filterTrangThai.value))
  }

  if (filterLoaiHoaDon.value !== '') {
    filtered = filtered.filter(hd => hd.loaiHoaDon === filterLoaiHoaDon.value)
  }

  if (dateFrom.value) {
    filtered = filtered.filter(hd => new Date(hd.ngayTao) >= new Date(dateFrom.value))
  }

  if (dateTo.value) {
    filtered = filtered.filter(hd => new Date(hd.ngayTao) <= new Date(dateTo.value))
  }

  return filtered
})

// Computed property để expand sản phẩm theo số lượng IMEI
const expandedProductList = computed(() => {
  if (!trackingData.value?.danhSachSanPham) return []

  const expandedProducts = []
  let stt = 1

  trackingData.value.danhSachSanPham.forEach(product => {
    const imeiCount = product.imeis ? product.imeis.length : 1

    if (imeiCount > 1) {
      // Nếu có nhiều IMEI, tạo từng dòng riêng cho mỗi IMEI
      product.imeis.forEach((imei, imeiIndex) => {
        expandedProducts.push({
          ...product,
          stt: stt++,
          soLuong: 1, // Mỗi dòng chỉ có 1 sản phẩm
          imei: imei, // IMEI cụ thể cho dòng này
          imeiIndex: imeiIndex + 1, // Thứ tự IMEI
          originalProduct: product // Tham chiếu đến sản phẩm gốc
        })
      })
    } else {
      // Nếu chỉ có 1 IMEI hoặc không có IMEI
      expandedProducts.push({
        ...product,
        stt: stt++,
        imei: product.imeis ? product.imeis[0] : null,
        imeiIndex: 1,
        originalProduct: product
      })
    }
  })

  return expandedProducts
})

// Methods
function handleStatusChange() {
  console.log('🔄 Status changed to:', filterTrangThai.value)
  console.log('🔄 Status type:', typeof filterTrangThai.value)
  console.log('🔄 Status parsed:', parseInt(filterTrangThai.value))
  currentPage.value = 1 // Reset to first page

  // Thông báo toast cho lọc trạng thái
  if (filterTrangThai.value !== '') {
    const statusName = statusOptions.find(s => s.value === parseInt(filterTrangThai.value))?.label || 'Trạng thái'
    toastRef.value?.info('Lọc', `Đã lọc theo trạng thái: ${statusName}`)
  } else {
    toastRef.value?.info('Lọc', 'Đã hiển thị tất cả trạng thái')
  }

  loadHoaDons()
}

function handleLoaiHoaDonChange() {
  console.log('🔄 Loai hoa don changed to:', filterLoaiHoaDon.value)
  currentPage.value = 1 // Reset to first page

  // Thông báo toast cho lọc loại đơn hàng
  if (filterLoaiHoaDon.value !== '') {
    let loaiName = ''
    switch(filterLoaiHoaDon.value) {
      case 'BAN_THUONG':
        loaiName = 'Bán tại quầy'
        break
      case 'BAN_ONLINE':
        loaiName = 'Bán online'
        break
      default:
        loaiName = filterLoaiHoaDon.value
    }
    toastRef.value?.info('Lọc', `Đã lọc theo loại: ${loaiName}`)
  } else {
    toastRef.value?.info('Lọc', 'Đã hiển thị tất cả loại đơn hàng')
  }

  loadHoaDons()
}


async function loadHoaDons() {
  loading.value = true
  try {
    const searchRequest = {
      keyword: searchKeyword.value,
      trangThai: filterTrangThai.value !== '' ? parseInt(filterTrangThai.value) : null,
      loaiHoaDon: filterLoaiHoaDon.value || null,
      tuNgay: dateFrom.value ? (dateFrom.value + 'T00:00:00') : null,
      denNgay: dateTo.value ? (dateTo.value + 'T23:59:59') : null,
      page: currentPage.value - 1,
      size: pageSize.value,
      sortBy: sortBy.value,
      sortDirection: sortDirection.value
    }

    console.log('🔍 Search Request:', searchRequest)
    console.log('🔍 Date From:', dateFrom.value, '-> tuNgay:', searchRequest.tuNgay)
    console.log('🔍 Date To:', dateTo.value, '-> denNgay:', searchRequest.denNgay)
    console.log('🔍 Filter Loai Hoa Don:', filterLoaiHoaDon.value)
    console.log('🔍 Filter Trang Thai:', filterTrangThai.value)
    console.log('🔍 Sort By:', sortBy.value, 'Sort Direction:', sortDirection.value)

    const { data } = await api.post('/api/hoa-don/search-advanced', searchRequest)
    console.log('📊 Response Data:', data)
    console.log('📊 Response Content:', data.content)
    console.log('📊 Response Total Elements:', data.totalElements)
    console.log('📊 Pagination Info:', {
      content: data.content?.length || 0,
      totalPages: data.totalPages,
      totalElements: data.totalElements,
      currentPage: currentPage.value,
      pageSize: pageSize.value
    })

    hoaDons.value = data.content || []
    totalPages.value = data.totalPages || 0
    totalElements.value = data.totalElements || 0

    console.log('📊 Updated pagination state:', {
      totalPages: totalPages.value,
      totalElements: totalElements.value,
      currentPage: currentPage.value
    })

    console.log('📋 Filtered Results:', hoaDons.value.length, 'items')
    console.log('📋 Sample Data:', hoaDons.value.slice(0, 3).map(hd => ({
      maHoaDon: hd.maHoaDon,
      trangThai: hd.trangThai,
      loaiHoaDon: hd.loaiHoaDon
    })))
  } catch (error) {
    console.error('Lỗi khi tải danh sách hóa đơn:', error)
    console.error('Error details:', {
      message: error.message,
      status: error.response?.status,
      statusText: error.response?.statusText,
      data: error.response?.data,
      config: error.config
    })
    hoaDons.value = []
    toastRef.value?.error('Lỗi', 'Không thể tải danh sách hóa đơn')
  } finally {
    loading.value = false
  }
}


async function deleteHoaDon(id: number) {
  // Sử dụng toast để xác nhận thay vì confirm
  toastRef.value?.warning('Xác nhận', 'Bạn có chắc muốn xóa hóa đơn này?')

  // Tạm thời tự động xóa sau 2 giây (có thể cải tiến thành modal xác nhận riêng)
  setTimeout(async () => {
    try {
      await api.delete(`/api/hoa-don/${id}`)
      await loadHoaDons()
      toastRef.value?.success('Thành công', 'Đã xóa hóa đơn thành công')
    } catch (error) {
      console.error('Lỗi khi xóa:', error)
      toastRef.value?.error('Lỗi', 'Không thể xóa hóa đơn')
    }
  }, 2000)
}

function viewDetails(hoaDon: HoaDon) {
  selectedHoaDon.value = hoaDon
  showDetailsModal.value = true
  toastRef.value?.info('Hiển thị', `Đang xem chi tiết hóa đơn: ${hoaDon.maHoaDon}`)
}

async function viewDetailsAndTrack(hoaDon: HoaDon) {
  // Chuyển sang tab theo dõi đơn hàng
  switchToTab('tracking')
  // Set mã đơn hàng để tìm kiếm
  trackingCode.value = hoaDon.maHoaDon

  toastRef.value?.info('Theo dõi', `Đang theo dõi đơn hàng: ${hoaDon.maHoaDon}`)

  // Tạo tracking data cơ bản từ dữ liệu hóa đơn hiện tại
  trackingData.value = {
    id: hoaDon.id,
    maHoaDon: hoaDon.maHoaDon,
    tenKhachHang: hoaDon.tenKhachHang || 'Khách lẻ',
    soDienThoai: hoaDon.soDienThoai || '0901234567',
    diaChi: 'Hà Nội',
    email: 'test@example.com',
    loaiHoaDon: hoaDon.loaiHoaDon,
    trangThai: hoaDon.trangThai,
    phieuGiamGia: hoaDon.phieuGiamGiaId ? `VC${hoaDon.phieuGiamGiaId}` : 'Không có',
    ngayDat: hoaDon.ngayTao,
    ghiChu: hoaDon.ghiChu || 'Không có',
    tongTienHang: hoaDon.tongTien,
    giamGia: (hoaDon.tongTien || 0) - (hoaDon.tongTienSauGiam || hoaDon.tongTien || 0),
    thanhTien: hoaDon.tongTienSauGiam || hoaDon.tongTien,
    danhSachSanPham: [] // Khởi tạo mảng rỗng
  }

  trackingSearched.value = true

  // Gọi API để lấy sản phẩm thực từ database
  await loadHoaDonProducts(hoaDon.id)
}

async function loadHoaDonProducts(hoaDonId: number) {
  try {
    console.log('🔍 Loading products for hoaDonId:', hoaDonId)
    const { data } = await api.get(`/api/hoa-don/${hoaDonId}/products`)
    console.log('✅ API Response:', data)

    if (data && Array.isArray(data)) {
      // Debug: Log dữ liệu từ API
      console.log('🔍 Raw API data for products:', data)
      data.forEach((item, index) => {
        console.log(`🔍 Product ${index}:`, {
          id: item.id,
          maCtsp: item.maCtsp,
          tenSanPham: item.tenSanPham,
          donGia: item.donGia
        })
      })

      // Chuyển đổi dữ liệu từ API thành format hiển thị
      const products = data.map((item: any, index: number) => ({
        id: item.id || index + 1,
        maCtsp: item.maCtsp || 'N/A',
        tenSanPham: item.tenSanPham || item.ten_ctsp || 'Sản phẩm không xác định',
        hinhAnh: item.hinhAnh || item.duong_dan || '/placeholder-product.png',
        gia: item.donGia || item.don_gia || 0,
        soLuong: item.soLuong || item.so_luong || 1,
        imeis: item.imeis || item.imei_da_bans || [],
        // Thêm các thông tin khác nếu có
        tenRam: item.tenRam || item.ten_ram,
        tenRom: item.tenRom || item.ten_rom,
        tenMauSac: item.tenMauSac || item.ten_mau_sac
      }))

      // Debug: Log dữ liệu sau khi mapping
      console.log('🔍 Mapped products:', products)

      // Cập nhật danh sách sản phẩm vào trackingData
      if (trackingData.value) {
        trackingData.value.danhSachSanPham = products
        console.log('✅ Updated trackingData with products:', products.length, 'products')
      }
    } else {
      console.log('⚠️ API returned no products or invalid format')
      // Tạo dữ liệu mẫu nếu API không trả về sản phẩm
      if (trackingData.value) {
        trackingData.value.danhSachSanPham = [
          {
            id: 1,
            maCtsp: 'SP001-8GB-256GB-TX',
            tenSanPham: 'iPhone 15 Pro Max',
            hinhAnh: '/placeholder-product.png',
            gia: 25000000,
            donGia: 25000000,
            soLuong: 1,
            imeis: ['123456789012345'],
            tenRam: '8GB',
            tenRom: '256GB',
            tenMauSac: 'Titan Xanh'
          },
          {
            id: 2,
            maCtsp: 'SP002-12GB-512GB-DX',
            tenSanPham: 'Samsung Galaxy S24 Ultra',
            hinhAnh: '/placeholder-product.png',
            gia: 20000000,
            donGia: 20000000,
            soLuong: 1,
            imeis: ['987654321098765'],
            tenRam: '12GB',
            tenRom: '512GB',
            tenMauSac: 'Titan Đen'
          }
        ]
        console.log('✅ Created fallback products for print preview')
      }
    }
  } catch (error) {
    console.error('❌ Error loading hoa don products:', error)
    toastRef.value?.error('Lỗi', 'Không thể tải danh sách sản phẩm của hóa đơn')
    // Tạo dữ liệu mẫu nếu có lỗi
    if (trackingData.value) {
      trackingData.value.danhSachSanPham = [
        {
          id: 1,
          maCtsp: 'SP001-8GB-256GB-TX',
          tenSanPham: 'iPhone 15 Pro Max',
          hinhAnh: '/placeholder-product.png',
          gia: 25000000,
          donGia: 25000000,
          soLuong: 1,
          imeis: ['123456789012345'],
          tenRam: '8GB',
          tenRom: '256GB',
          tenMauSac: 'Titan Xanh'
        },
        {
          id: 2,
          maCtsp: 'SP002-12GB-512GB-DX',
          tenSanPham: 'Samsung Galaxy S24 Ultra',
          hinhAnh: '/placeholder-product.png',
          gia: 20000000,
          donGia: 20000000,
          soLuong: 1,
          imeis: ['987654321098765'],
          tenRam: '12GB',
          tenRom: '512GB',
          tenMauSac: 'Titan Đen'
        }
      ]
      console.log('✅ Created fallback products due to error')
    }
  }
}

function updateOrder(hoaDon: HoaDon) {
  // Chuyển đến trang cập nhật đơn hàng với mã đơn hàng
  router.push({
    name: 'update-order',
    query: { code: hoaDon.maHoaDon }
  })
}

async function updateOrderStatus(hoaDonId: number, newStatus: string) {
  updatingStatus.value = hoaDonId

  try {
    const { data } = await api.put(`/api/hoa-don/${hoaDonId}/status`, {
      trangThai: parseInt(newStatus)
    })

    // Cập nhật trạng thái trong danh sách hiện tại
    const index = hoaDons.value.findIndex(hd => hd.id === hoaDonId)
    if (index !== -1) {
      hoaDons.value[index].trangThai = parseInt(newStatus)
    }

    // Cập nhật trạng thái trong trackingData nếu đang ở trang tracking
    if (trackingData.value && trackingData.value.id === hoaDonId) {
      trackingData.value.trangThai = parseInt(newStatus)
    }

    console.log('✅ Cập nhật trạng thái thành công:', data)
    toastRef.value?.success('Thành công', 'Cập nhật trạng thái thành công!')

  } catch (error) {
    console.error('❌ Lỗi khi cập nhật trạng thái:', error)
    toastRef.value?.error('Lỗi', 'Có lỗi xảy ra khi cập nhật trạng thái đơn hàng')
  } finally {
    updatingStatus.value = null
  }
}

async function handleStepClick(step: any, stepIndex: number) {
  const currentStepIndex = getCurrentStepIndex()
  const loaiHoaDon = trackingData.value?.loaiHoaDon
  const isBanThuong = loaiHoaDon === 'BAN_THUONG'

  // Debug log
  console.log('🔍 Debug handleStepClick:', {
    stepTitle: step.title,
    stepStatus: step.status,
    stepIndex,
    currentStepIndex,
    loaiHoaDon,
    isBanThuong,
    isUpdatingStatus: isUpdatingStatus.value,
    hasTrackingData: !!trackingData.value
  })

  if (isUpdatingStatus.value) {
    console.log('❌ Blocked: Already updating status')
    return
  }

  if (!trackingData.value) {
    toastRef.value?.error('Lỗi', 'Không có dữ liệu đơn hàng để cập nhật')
    return
  }

  // Kiểm tra nếu trạng thái step giống với trạng thái hiện tại
  // Loại trừ bước "Chờ xác nhận" (status = 0) vì cần chọn phương thức thanh toán
  if (step.status === trackingData.value.trangThai && step.status !== 0) {
    toastRef.value?.warning('Cảnh báo', 'Trạng thái hiện tại đã giống với trạng thái mới')
    return
  }

  // Nếu là bước "Chờ xác nhận" (status = 0), hiển thị modal chọn phương thức thanh toán
  if (step.status === 0 && currentStepIndex === 0) {
    if (isBanThuong) {
      // Bán tại quầy: không cần chọn phương thức thanh toán, tự động hoàn thành
      console.log('✅ BAN_THUONG: Auto completing order without payment method selection')

      // Tự động set thời gian hoàn thành = ngày giờ tạo hóa đơn
      if (trackingData.value.ngayTao) {
        const ngayTao = new Date(trackingData.value.ngayTao)
        const timeString = ngayTao.toLocaleTimeString('vi-VN') + ' ' + ngayTao.toLocaleDateString('vi-VN')
        step.time = timeString
        console.log('✅ Set completion time to invoice creation time:', timeString)
      }

      await updateOrderStatusDirectly(4, step) // Status 4 = Hoàn thành
      return
    } else {
      // Bán online: vẫn cần chọn phương thức thanh toán
      console.log('✅ Opening payment modal for step 0')
      currentStep.value = step
      showPaymentModal.value = true
      return
    }
  }

  // Bước "Hoàn thành" (status = 4) - xử lý khác nhau cho bán thường và bán online
  if (step.status === 4) {
    if (isBanThuong) {
      // Bán tại quầy: đơn hàng đã hoàn thành từ khi tạo, không cần click gì thêm
      console.log('ℹ️ BAN_THUONG order is already completed, no action needed')
      toastRef.value?.info('Thông báo', 'Đơn hàng bán thường đã hoàn thành từ khi tạo')
      return
    }

    // Bán online: chỉ có thể hoàn thành từ bước 2 (đang giao hàng)
    const canComplete = currentStepIndex === 2

    if (canComplete) {
      // Bán online: vẫn cần xác nhận
      console.log('✅ Opening confirm modal for completion (BAN_ONLINE)')
      currentStep.value = step
      confirmMessage.value = `Bạn có chắc chắn muốn hoàn thành đơn hàng?`
      confirmCallback.value = async () => {
        await updateOrderStatusDirectly(step.status, step)
      }
      showConfirmModal.value = true
      return
    }
  }

  // Các bước khác - logic click khác nhau cho bán thường và bán online
  let canClick = false

  if (isBanThuong) {
    // Bán tại quầy: chỉ có 1 bước (Hoàn thành) và đã hoàn thành rồi
    // Không cho phép click vào bất kỳ bước nào vì đã hoàn thành
    canClick = false
    console.log('ℹ️ BAN_THUONG order is already completed, no further steps to click')
  } else {
    // Bán online: 4 bước đầy đủ
    // Cho phép click nếu:
    // 1. Bước tiếp theo (stepIndex = currentStepIndex + 1)
    // 2. Hoặc bước hiện tại (stepIndex = currentStepIndex) và không phải bước 0
    // 3. Hoặc đang ở status 1 và click vào bước 1 (Chờ giao hàng)
    canClick = (stepIndex === currentStepIndex + 1) ||
      (stepIndex === currentStepIndex && step.status !== 0) ||
      (trackingData.value?.trangThai === 1 && stepIndex === 1 && step.status === 2)
  }

  console.log('🔍 Can click check:', {
    canClick,
    stepIndex,
    currentStepIndex,
    stepStatus: step.status,
    currentStatus: trackingData.value?.trangThai,
    loaiHoaDon,
    isBanThuong
  })

  if (canClick) {
    console.log('✅ Opening confirm modal for step:', step.title)
    currentStep.value = step
    confirmMessage.value = `Bạn có chắc chắn muốn chuyển đơn hàng sang trạng thái "${step.title}"?`
    confirmCallback.value = async () => {
      await updateOrderStatusDirectly(step.status, step)
    }
    showConfirmModal.value = true
    return
  }

  console.log('❌ No action taken for step:', step.title)
}

async function updateOrderStatusDirectly(newStatus: number, step: any) {
  if (!trackingData.value) return

  isUpdatingStatus.value = true

  try {
    const { data } = await api.put(`/api/hoa-don/update-status/${trackingData.value.maHoaDon}`, {
      trangThai: newStatus
    })

    // Cập nhật trạng thái trong danh sách hiện tại
    const index = hoaDons.value.findIndex(hd => hd.id === trackingData.value.id)
    if (index !== -1) {
      hoaDons.value[index].trangThai = newStatus
    }

    // Cập nhật trạng thái trong trackingData
    trackingData.value.trangThai = newStatus

    // Cập nhật thời gian cho bước vừa hoàn thành (chỉ nếu chưa có thời gian)
    if (!step.time) {
      const now = new Date()
      const timeString = now.toLocaleTimeString('vi-VN') + ' ' + now.toLocaleDateString('vi-VN')
      step.time = timeString
      console.log('✅ Set completion time to current time:', timeString)
    } else {
      console.log('✅ Keeping existing completion time:', step.time)
    }

    console.log('✅ Cập nhật trạng thái thành công:', data)
    toastRef.value?.success('Thành công', `Đã cập nhật trạng thái đơn hàng sang "${step.title}"`)

  } catch (error) {
    console.error('❌ Lỗi khi cập nhật trạng thái:', error)
    toastRef.value?.error('Lỗi', 'Có lỗi xảy ra khi cập nhật trạng thái đơn hàng')
  } finally {
    isUpdatingStatus.value = false
  }
}

async function selectPaymentMethod(method: 'cod' | 'prepaid') {
  if (!currentStep.value || !trackingData.value) return

  const loaiHoaDon = trackingData.value.loaiHoaDon
  const isBanThuong = loaiHoaDon === 'BAN_THUONG'

  let newStatus: number
  if (isBanThuong) {
    // Bán tại quầy: bất kể phương thức thanh toán nào, đều chuyển thẳng sang "Hoàn thành" (status = 4)
    newStatus = 4
    console.log('✅ BAN_THUONG: Auto completing order after payment method selection')

    // Tự động set thời gian hoàn thành = ngày giờ tạo hóa đơn
    if (trackingData.value.ngayTao) {
      const ngayTao = new Date(trackingData.value.ngayTao)
      const timeString = ngayTao.toLocaleTimeString('vi-VN') + ' ' + ngayTao.toLocaleDateString('vi-VN')
      currentStep.value.time = timeString
      console.log('✅ Set completion time to invoice creation time:', timeString)
    }
  } else {
    // Bán online: logic cũ
    if (method === 'cod') {
      // COD - chuyển trực tiếp sang "Chờ giao hàng" (status = 2)
      newStatus = 2
    } else {
      // Đã thanh toán - chuyển sang "Đã thanh toán chờ xác nhận" (status = 1)
      newStatus = 1
    }
  }

  showPaymentModal.value = false
  await updateOrderStatusDirectly(newStatus, currentStep.value)
  currentStep.value = null
}

function closePaymentModal() {
  showPaymentModal.value = false
  currentStep.value = null
}

async function confirmAction() {
  if (confirmCallback.value) {
    await confirmCallback.value()
  }
  showConfirmModal.value = false
  currentStep.value = null
  confirmCallback.value = undefined
}

function cancelAction() {
  showConfirmModal.value = false
  currentStep.value = null
  confirmCallback.value = undefined
}

// IMEI Modal functions
function showImeiDetails(product: any) {
  selectedProductForImei.value = product
  showImeiModal.value = true
}

function closeImeiModal() {
  showImeiModal.value = false
  selectedProductForImei.value = null
}

function showImeiDetailsForAllProducts() {
  if (trackingData.value && trackingData.value.danhSachSanPham && trackingData.value.danhSachSanPham.length > 0) {
    selectedProductForImei.value = trackingData.value.danhSachSanPham
    showImeiModal.value = true
  } else {
    console.log('No products available to show IMEI details')
  }
}

function getStatusName(trangThai: number | string | null | undefined): string {
  if (trangThai === null || trangThai === undefined) {
    return 'Không xác định'
  }
  
  const numericTrangThai = typeof trangThai === 'string' ? parseInt(trangThai) : trangThai
  const status = statusOptions.find(s => s.value === numericTrangThai)
  return status ? status.label : 'Không xác định'
}

function getStatusClass(trangThai: number | string | null | undefined): string {
  if (trangThai === null || trangThai === undefined) {
    return 'status-unknown'
  }
  
  const numericTrangThai = typeof trangThai === 'string' ? parseInt(trangThai) : trangThai
  const status = statusOptions.find(s => s.value === numericTrangThai)
  return status ? `status-${numericTrangThai}` : 'status-unknown'
}

function getStatusColor(trangThai: number | string | null | undefined): string {
  if (trangThai === null || trangThai === undefined) {
    return '#6c757d'
  }
  
  const numericTrangThai = typeof trangThai === 'string' ? parseInt(trangThai) : trangThai
  const status = statusOptions.find(s => s.value === numericTrangThai)
  return status ? status.color : '#6c757d'
}

function getStatusIcon(trangThai: number | string | null | undefined): string {
  if (trangThai === null || trangThai === undefined) {
    return 'question-circle'
  }
  
  const numericTrangThai = typeof trangThai === 'string' ? parseInt(trangThai) : trangThai
  const iconMap: Record<number, string> = {
    0: 'clock',           // Chờ xác nhận
    1: 'credit-card',     // Đã thanh toán chờ xác nhận
    2: 'box',             // Chờ giao hàng
    3: 'truck',           // Đang giao
    4: 'check-circle',    // Hoàn thành
    5: 'times-circle'     // Đã hủy
  }
  return iconMap[numericTrangThai] || 'question-circle'
}

function getStatusBadgeClass(trangThai: number | string | null | undefined): string {
  if (trangThai === null || trangThai === undefined) {
    return 'badge-unknown'
  }
  
  const numericTrangThai = typeof trangThai === 'string' ? parseInt(trangThai) : trangThai
  const badgeMap: Record<number, string> = {
    0: 'badge-pending',     // Chờ xác nhận
    1: 'badge-paid',        // Đã thanh toán chờ xác nhận
    2: 'badge-shipping',    // Chờ giao hàng
    3: 'badge-delivering',  // Đang giao
    4: 'badge-completed',   // Hoàn thành
    5: 'badge-cancelled'    // Đã hủy
  }
  return badgeMap[numericTrangThai] || 'badge-unknown'
}

function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}

function formatDate(dateString: string): string {
  return new Date(dateString).toLocaleDateString('vi-VN')
}

function formatDateTime(dateString: string): string {
  return new Date(dateString).toLocaleString('vi-VN')
}

function toggleFilters() {
  showFilters.value = !showFilters.value
  toastRef.value?.info('Bộ lọc', showFilters.value ? 'Đã mở bộ lọc' : 'Đã đóng bộ lọc')
}

// Debounce search function
let searchTimeout: NodeJS.Timeout | null = null

function handleSearch() {
  if (searchTimeout) {
    clearTimeout(searchTimeout)
  }

  searchTimeout = setTimeout(() => {
    currentPage.value = 1
    loadHoaDons()

    // Thông báo toast cho tìm kiếm
    if (searchKeyword.value.trim()) {
      toastRef.value?.info('Tìm kiếm', `Đang tìm kiếm: "${searchKeyword.value}"`)
    } else {
      toastRef.value?.info('Tìm kiếm', 'Đã xóa bộ lọc tìm kiếm')
    }
  }, 500) // 500ms delay
}

function resetFilters() {
  searchKeyword.value = ''
  filterTrangThai.value = ''
  filterLoaiHoaDon.value = ''
  dateFrom.value = ''
  dateTo.value = ''
  sortBy.value = 'ngayTao'
  sortDirection.value = 'desc'
  quickSort.value = 'ngayTao_desc'
  currentPage.value = 1
  loadHoaDons()
  toastRef.value?.info('Thông báo', 'Đã reset bộ lọc')
}

function exportToExcel() {
  try {
    // Tạo workbook và worksheet
    const wb = XLSX.utils.book_new()

    // Chuẩn bị dữ liệu cho Excel
    const excelData = []

    // Thêm header
    const headers = [
      'STT',
      'Mã HD',
      'Khách hàng',
      'SĐT',
      'Loại đơn',
      'Tổng tiền',
      'Trạng thái',
      'Ngày tạo'
    ]
    excelData.push(headers)

    // Thêm dữ liệu từ bảng
    hoaDons.value.forEach((hd, index) => {
      const row = [
        index + 1,
        hd.maHoaDon,
        hd.tenKhachHang || 'Không có',
        hd.soDienThoai || 'Không có',
        (hd.loaiHoaDon === 'BAN_ONLINE' || hd.loaiHoaDon === 'DELIVERY') ? 'Bán online' : 'Bán tại quầy',
        formatCurrency(hd.tongTienSauGiam || hd.tongTien),
        getStatusName(hd.trangThai),
        formatDate(hd.ngayTao)
      ]
      excelData.push(row)
    })

    // Tạo worksheet từ dữ liệu
    const ws = XLSX.utils.aoa_to_sheet(excelData)

    // Điều chỉnh độ rộng cột
    ws['!cols'] = [
      { width: 5 },   // STT
      { width: 15 },  // Mã HD
      { width: 20 },  // Khách hàng
      { width: 12 },  // SĐT
      { width: 12 },  // Loại đơn
      { width: 15 },  // Tổng tiền
      { width: 15 },  // Trạng thái
      { width: 12 }   // Ngày tạo
    ]

    // Thêm worksheet vào workbook
    XLSX.utils.book_append_sheet(wb, ws, 'Danh sách hóa đơn')

    // Tạo tên file với timestamp
    const now = new Date()
    const timestamp = now.toISOString().slice(0, 19).replace(/:/g, '-')
    const fileName = `Danh_sach_hoa_don_${timestamp}.xlsx`

    // Xuất file
    XLSX.writeFile(wb, fileName)

    toastRef.value?.success('Xuất Excel', `Đã xuất ${hoaDons.value.length} hóa đơn thành công!`)

  } catch (error) {
    console.error('Lỗi khi xuất Excel:', error)
    toastRef.value?.error('Lỗi', 'Có lỗi xảy ra khi xuất file Excel')
  }
}

function handleSortChange() {
  currentPage.value = 1 // Reset to first page when changing sort
  loadHoaDons()

  const sortLabel = sortOptions.find(opt => opt.value === sortBy.value)?.label || 'Ngày tạo'
  const directionLabel = sortDirection.value === 'desc' ? 'Giảm dần' : 'Tăng dần'

  toastRef.value?.info('Sắp xếp', `Đã sắp xếp theo ${sortLabel} - ${directionLabel}`)
}

function handleQuickSortChange() {
  const selectedOption = quickSortOptions.find(opt => opt.value === quickSort.value)
  console.log('🔄 Quick Sort Change:', {
    quickSortValue: quickSort.value,
    selectedOption: selectedOption,
    sortBy: selectedOption?.sortBy,
    sortDirection: selectedOption?.direction
  })

  if (selectedOption) {
    sortBy.value = selectedOption.sortBy
    sortDirection.value = selectedOption.direction
    currentPage.value = 1

    console.log('🔄 Updated sort values:', {
      sortBy: sortBy.value,
      sortDirection: sortDirection.value
    })

    loadHoaDons()

    toastRef.value?.info('Sắp xếp', `Đã sắp xếp: ${selectedOption.label}`)
  }
}

function goToPage(page: number) {
  console.log('🔍 goToPage called:', {
    requestedPage: page,
    currentPage: currentPage.value,
    totalPages: totalPages.value,
    canNavigate: page >= 1 && page <= totalPages.value
  })

  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    console.log('✅ Navigating to page:', page)
    toastRef.value?.info('Phân trang', `Đang chuyển đến trang ${page}`)
    loadHoaDons()
  } else {
    console.log('❌ Cannot navigate to page:', page, 'Total pages:', totalPages.value)
    toastRef.value?.error('Lỗi', `Không thể chuyển đến trang ${page}`)
  }
}

function handlePageSizeChange() {
  currentPage.value = 1 // Reset to first page when changing page size
  toastRef.value?.info('Hiển thị', `Đã thay đổi số lượng hiển thị: ${pageSize.value} đơn hàng/trang`)
  loadHoaDons()
}

function switchToTab(tabName: string) {
  activeTab.value = tabName
  const tabLabel = tabName === 'list' ? 'Danh sách hóa đơn' : 'Theo dõi đơn hàng'
  toastRef.value?.info('Chuyển tab', `Đã chuyển sang: ${tabLabel}`)
}


// Function to get visible page numbers for pagination
function getVisiblePages() {
  const pages = []
  const current = currentPage.value
  const total = totalPages.value

  console.log('🔍 getVisiblePages called:', {
    current,
    total,
    currentPage: currentPage.value,
    totalPages: totalPages.value
  })

  if (total === 0) {
    console.log('❌ No pages available (total = 0)')
    return []
  }

  if (total <= 7) {
    // Show all pages if total is 7 or less
    for (let i = 1; i <= total; i++) {
      pages.push(i)
    }
  } else {
    // Show pages around current page
    const start = Math.max(1, current - 2)
    const end = Math.min(total, current + 2)

    for (let i = start; i <= end; i++) {
      pages.push(i)
    }
  }

  console.log('✅ getVisiblePages returning:', pages, 'Length:', pages.length)
  return pages
}

// QR Scanner functions
function startQRScanner() {
  showQRScannerModal.value = true
  isQRScannerActive.value = true

  // Khởi động camera sau khi modal hiển thị
  setTimeout(() => {
    initCamera()
  }, 100)
}

function initCamera() {
  const video = document.getElementById('qr-scanner-video') as HTMLVideoElement
  if (!video) return

  navigator.mediaDevices.getUserMedia({
    video: {
      facingMode: 'environment' // Camera sau
    }
  })
    .then(stream => {
      video.srcObject = stream
      video.play()

      // Lưu reference để có thể dừng sau
      qrScanner.value = { video, stream }

      // Bắt đầu quét QR
      startQRDetection(video)
    })
    .catch(error => {
      console.error('Lỗi khi khởi động camera:', error)
      toastRef.value?.error('Lỗi', 'Không thể khởi động camera. Vui lòng kiểm tra quyền truy cập camera.')
      closeQRScanner()
    })
}

function closeQRScanner() {
  console.log('🔄 Đang đóng QR Scanner...')

  try {
    // Dừng QR Scanner instance nếu có
    if (currentQRScanner.value) {
      console.log('🛑 Dừng QR Scanner instance...')
      try {
        currentQRScanner.value.stop()
        currentQRScanner.value.destroy()
        currentQRScanner.value = null
        console.log('✅ QR Scanner instance đã được dừng')
      } catch (error) {
        console.warn('⚠️ Lỗi khi dừng QR Scanner instance:', error)
      }
    }

    // Dừng camera stream nếu có
    if (qrScanner.value) {
      if (qrScanner.value.stream) {
        console.log('🛑 Dừng camera stream...')
        qrScanner.value.stream.getTracks().forEach(track => {
          track.stop()
          console.log('✅ Đã dừng track:', track.kind)
        })
      }

      // Dừng video element
      if (qrScanner.value.video) {
        qrScanner.value.video.pause()
        qrScanner.value.video.srcObject = null
        console.log('✅ Video element đã được dừng')
      }

      qrScanner.value = null
    }

    // Đóng modal và reset trạng thái
    isQRScannerActive.value = false
    showQRScannerModal.value = false

    // Thông báo
    toastRef.value?.info('Thông báo', 'Đã đóng camera và QR Scanner')

    console.log('✅ QR Scanner đã được đóng hoàn toàn')

  } catch (error) {
    console.error('❌ Lỗi khi đóng QR Scanner:', error)
    // Vẫn đóng modal dù có lỗi
    isQRScannerActive.value = false
    showQRScannerModal.value = false
    toastRef.value?.error('Lỗi', 'Có lỗi khi đóng camera')
  }
}

function startQRDetection(video) {
  if (!video) return

  // Sử dụng QrScanner để quét QR code thực tế
  const qrScannerInstance = new QrScanner(
    video,
    result => {
      console.log('🔍 QR Code detected:', result.data)
      handleQRCodeDetected(result.data)
    },
    {
      onDecodeError: error => {
        // Không cần log lỗi liên tục, chỉ log khi cần debug
        // console.log('QR decode error:', error)
      },
      highlightScanRegion: true,
      highlightCodeOutline: true,
    }
  )

  // Lưu reference của QR scanner instance
  currentQRScanner.value = qrScannerInstance

  // Bắt đầu quét
  qrScannerInstance.start().catch(error => {
    console.error('Lỗi khi bắt đầu quét QR:', error)
    toastRef.value?.error('Lỗi', 'Không thể bắt đầu quét QR code')
  })
}

function handleQRCodeDetected(qrCode) {
  console.log('🔍 QR Code detected:', qrCode)

  // Trích xuất mã hóa đơn từ QR code
  // QR code có thể chứa: "HOADON:HD001" hoặc chỉ "HD001"
  let maHoaDon = qrCode

  if (qrCode.includes(':')) {
    const parts = qrCode.split(':')
    if (parts.length >= 2) {
      maHoaDon = parts[1] // Lấy phần sau dấu :
    }
  }

  // Đóng modal trước
  closeQRScanner()

  // Cập nhật tracking code và tự động tìm kiếm
  trackingCode.value = maHoaDon
  searchTracking()

  // Hiển thị thông báo thành công
  toastRef.value?.success('Thành công', `Đã quét thành công mã hóa đơn: ${maHoaDon}`)
}

// Reset tracking page function
function resetTrackingPage() {
  // Reset tracking data
  trackingCode.value = ''
  trackingData.value = null
  trackingSearched.value = false

  // Reset to tracking tab (not list tab)
  switchToTab('tracking')

  toastRef.value?.info('Thông báo', 'Đã reset trang theo dõi')

  console.log('🔄 Tracking page reset to initial state')
}

// Tracking functions
async function searchTracking() {
  if (!trackingCode.value.trim()) {
    toastRef.value?.warning('Cảnh báo', 'Vui lòng nhập mã hóa đơn để tìm kiếm')
    return
  }

  trackingSearched.value = true
  toastRef.value?.info('Tìm kiếm', `Đang tìm kiếm hóa đơn: ${trackingCode.value}`)

  try {
    // Thử endpoint tracking trước
    const { data } = await api.get(`/api/hoa-don/tracking/${trackingCode.value}`)
    if (data) {
      trackingData.value = data
      console.log('✅ Tracking API returned data:', data)
      toastRef.value?.success('Thành công', `Đã tìm thấy hóa đơn: ${trackingCode.value}`)

      // Nếu tracking data không có sản phẩm, thử lấy từ products API
      if (!data.danhSachSanPham || data.danhSachSanPham.length === 0) {
        console.log('🔍 No products in tracking data, trying products API...')
        // Tìm hóa đơn trong danh sách hiện tại để lấy ID
        let hoaDon = hoaDons.value.find(hd => hd.maHoaDon === trackingCode.value)

        // Nếu không tìm thấy trong danh sách hiện tại, tìm trong toàn bộ database
        if (!hoaDon) {
          console.log('🔍 Hoa don not found in current list, searching in database...')
          try {
            const { data: hoaDonData } = await api.get(`/api/hoa-don/ma-hoa-don/${trackingCode.value}`)
            if (hoaDonData) {
              hoaDon = hoaDonData
              console.log('✅ Found hoa don in database:', hoaDon)
            }
          } catch (error) {
            console.error('❌ Error searching hoa don in database:', error)
          }
        }

        if (hoaDon) {
          await loadHoaDonProducts(hoaDon.id)
        }
      }
    } else {
      console.log('❌ No hoa don found with code:', trackingCode.value)
      trackingData.value = null
      toastRef.value?.error('Không tìm thấy', `Không tìm thấy hóa đơn với mã: ${trackingCode.value}`)
    }
  } catch (trackingError) {
    console.log('Tracking endpoint failed:', trackingError)

    // Fallback: tìm hóa đơn trong danh sách hiện tại
    const hoaDon = hoaDons.value.find(hd => hd.maHoaDon === trackingCode.value)
    if (hoaDon) {
      console.log('🔍 Found hoa don in current list, loading products...')
      toastRef.value?.success('Thành công', `Đã tìm thấy hóa đơn trong danh sách: ${trackingCode.value}`)
      // Tạo tracking data cơ bản và load sản phẩm
      trackingData.value = {
        id: hoaDon.id,
        maHoaDon: hoaDon.maHoaDon,
        tenKhachHang: hoaDon.tenKhachHang || 'Khách lẻ',
        soDienThoai: hoaDon.soDienThoai || '0901234567',
        diaChi: 'Hà Nội',
        email: 'test@example.com',
        loaiHoaDon: hoaDon.loaiHoaDon,
        trangThai: hoaDon.trangThai,
        phieuGiamGia: hoaDon.phieuGiamGiaId ? `VC${hoaDon.phieuGiamGiaId}` : 'Không có',
        ngayDat: hoaDon.ngayTao,
        ghiChu: hoaDon.ghiChu || 'Không có',
        tongTienHang: hoaDon.tongTien,
        giamGia: (hoaDon.tongTien || 0) - (hoaDon.tongTienSauGiam || hoaDon.tongTien || 0),
        thanhTien: hoaDon.tongTienSauGiam || hoaDon.tongTien,
        danhSachSanPham: []
      }
      await loadHoaDonProducts(hoaDon.id)
    } else {
      console.log('❌ No hoa don found with code:', trackingCode.value)
      trackingData.value = null
      toastRef.value?.error('Không tìm thấy', `Không thể tìm thấy hóa đơn với mã: ${trackingCode.value}`)
    }
  }
}

function getCurrentStepIndex(): number {
  if (!trackingData.value) {
    console.log('🔍 getCurrentStepIndex: No tracking data, returning 0')
    return 0
  }

  const status = trackingData.value.trangThai
  const loaiHoaDon = trackingData.value.loaiHoaDon
  console.log('🔍 getCurrentStepIndex: status =', status, 'loaiHoaDon =', loaiHoaDon)

  const isBanThuong = loaiHoaDon === 'BAN_THUONG' || loaiHoaDon === 'NORMAL'

  if (isBanThuong) {
    // Bán tại quầy: chỉ có 1 bước (Hoàn thành)
    // Đơn bán thường luôn ở trạng thái hoàn thành (status = 4)
    // Nhưng trong UI, chúng ta hiển thị như đã hoàn thành rồi
    return 0 // Luôn hiển thị bước duy nhất (Hoàn thành)
  } else {
    // Bán online: 4 bước đầy đủ
    switch (status) {
      case 0: return 0  // Chờ xác nhận
      case 1: return 1  // Đã thanh toán chờ xác nhận -> chuyển thành Chờ giao hàng
      case 2: return 1  // Chờ giao hàng
      case 3: return 2  // Đang giao hàng
      case 4: return 3  // Hoàn thành
      case 5: return 0  // Đã hủy - reset về bước đầu
      default: return 0
    }
  }
}

// Function tính tổng tiền
function getTotalAmount(): number {
  if (!trackingData.value?.danhSachSanPham) return 0

  return trackingData.value.danhSachSanPham.reduce((total: number, product: any) => {
    const unitPrice = product.donGia || product.gia || 0
    const quantity = product.soLuong || 1
    return total + (quantity * unitPrice)
  }, 0)
}

// Function in hóa đơn (tạo nội dung hóa đơn trong window print)
async function printInvoice() {
  if (!trackingData.value) {
    toastRef.value?.error('Lỗi', 'Không có dữ liệu hóa đơn để in')
    return
  }

  try {
    // Debug: Kiểm tra dữ liệu
    console.log('🔍 Print Invoice - trackingData:', trackingData.value)
    console.log('🔍 Print Invoice - loaiHoaDon:', trackingData.value.loaiHoaDon, 'Type:', typeof trackingData.value.loaiHoaDon)
    console.log('🔍 Print Invoice - loaiHoaDon === "BAN_THUONG":', trackingData.value.loaiHoaDon === 'BAN_THUONG')
    console.log('🔍 Print Invoice - loaiHoaDon === "NORMAL":', trackingData.value.loaiHoaDon === 'NORMAL')
    console.log('🔍 Print Invoice - tenNhanVien:', trackingData.value.tenNhanVien)
    console.log('🔍 Print Invoice - ngayDat:', trackingData.value.ngayDat)
    console.log('🔍 Print Invoice - ngayTao:', trackingData.value.ngayTao)

    // Tạo QR Code
    const qrCodeDataURL = await QRCode.toDataURL(trackingData.value.maHoaDon, {
      width: 100,
      margin: 2,
      color: {
        dark: '#000000',
        light: '#FFFFFF'
      }
    })

    // Debug: Log dữ liệu sản phẩm trước khi in
    console.log('🔍 Print Invoice - danhSachSanPham:', trackingData.value.danhSachSanPham)
    if (trackingData.value.danhSachSanPham) {
      trackingData.value.danhSachSanPham.forEach((product, index) => {
        console.log(`🔍 Product ${index} for printing:`, {
          id: product.id,
          maCtsp: product.maCtsp,
          tenSanPham: product.tenSanPham
        })
      })
    }

    // Tạo nội dung hóa đơn HTML
    const invoiceHTML = `
      <div class="invoice-print" style="
        font-family: 'Segoe UI', sans-serif;
        max-width: 210mm;
        margin: 0 auto;
        padding: 20mm;
        color: #333;
        background: white;
        line-height: 1.4;
      ">
        <!-- Header -->
        <div style="display: flex; justify-content: space-between; border-bottom: 2px solid #ff6b35; padding-bottom: 10px; margin-bottom: 15px;">
          <div>
            <img src="/logo.png" alt="PhoniX Logo" style="width: 80px; height: auto; margin-bottom: 10px;" />
            <h2 style="margin: 0 0 8px 0; font-size: 16px; font-weight: bold;">PhoniX Store</h2>
            <p style="margin: 4px 0; font-size: 12px;">Địa chỉ: 123 Trần Duy Hưng, Cầu Giấy, Hà Nội</p>
            <p style="margin: 4px 0; font-size: 12px;">SĐT: 0909 123 456 | Email: phunixstore@gmail.com</p>
          </div>
          <div style="text-align: right;">
            <h1 style="font-size: 24px; font-weight: bold; margin: 0 0 10px 0; color: #ff6b35;">HÓA ĐƠN BÁN HÀNG</h1>
            <p style="margin: 5px 0; font-size: 14px;"><strong>Mã đơn hàng:</strong> ${trackingData.value.maHoaDon}</p>
            <p style="margin: 5px 0; font-size: 14px;"><strong>Ngày đặt:</strong> ${trackingData.value.ngayDat || trackingData.value.ngayTao ? (() => {
      try {
        const dateValue = trackingData.value.ngayDat || trackingData.value.ngayTao;
        const date = new Date(dateValue);
        return isNaN(date.getTime()) ? 'N/A' : date.toLocaleDateString('vi-VN');
      } catch (e) {
        return 'N/A';
      }
    })() : 'N/A'}</p>
            <div style="margin-top: 10px; text-align: center;">
              <img src="${qrCodeDataURL}" alt="QR Code" style="width: 80px; height: 80px;" />
              <p style="font-size: 10px; margin: 5px 0 0 0;">Mã hóa đơn</p>
            </div>
          </div>
        </div>

        <!-- Customer + Summary -->
        <div style="display: flex; justify-content: space-between; margin-bottom: 15px;">
          <div style="width: 48%; border: 1px solid #ccc; padding: 10px; border-radius: 8px;">
            <h3 style="font-size: 16px; font-weight: bold; margin: 0 0 10px 0;">Thông tin khách hàng</h3>
            <p style="margin: 5px 0; font-size: 14px;"><strong>Tên khách hàng:</strong> ${trackingData.value.tenKhachHang || 'Khách lẻ'}</p>
            <p style="margin: 5px 0; font-size: 14px;"><strong>Số điện thoại:</strong> ${trackingData.value.soDienThoai || 'Không có'}</p>
            <p style="margin: 5px 0; font-size: 14px;"><strong>Địa chỉ:</strong> ${trackingData.value.diaChi || 'Không có'}</p>
            <p style="margin: 5px 0; font-size: 14px;"><strong>Email:</strong> ${trackingData.value.email || "Không có"}</p>
          </div>
          <div style="width: 48%; border: 1px solid #ccc; padding: 10px; border-radius: 8px;">
            <h3 style="font-size: 16px; font-weight: bold; margin: 0 0 10px 0;">Thông tin đơn hàng</h3>
            <p style="margin: 5px 0; font-size: 14px;"><strong>Loại đơn:</strong> ${(() => {
      const loai = trackingData.value.loaiHoaDon
      if (loai === 'BAN_THUONG' || loai === 'NORMAL') {
        return 'Bán tại quầy'
      } else if (loai === 'BAN_ONLINE' || loai === 'DELIVERY') {
        return 'Bán online'
      } else {
        return loai || 'Không xác định'
      }
    })()}</p>
            <p style="margin: 5px 0; font-size: 14px;"><strong>Trạng thái:</strong> ${getStatusText(trackingData.value.trangThai)}</p>
            <p style="margin: 5px 0; font-size: 14px;"><strong>Phiếu giảm giá:</strong> ${trackingData.value.phieuGiamGia || "Không có"}</p>
            <p style="margin: 5px 0; font-size: 14px;"><strong>Nhân viên:</strong> ${trackingData.value.tenNhanVien || 'Không xác định'}</p>
          </div>
        </div>

        <!-- Product list -->
        <table style="width: 100%; border-collapse: collapse; margin-top: 10px;">
          <thead>
            <tr>
              <th style="border: 1px solid #ccc; padding: 8px; text-align: center; background: #ff6b35; color: white; font-weight: bold;">STT</th>
              <th style="border: 1px solid #ccc; padding: 8px; text-align: center; background: #ff6b35; color: white; font-weight: bold;">Mã CTSP</th>
              <th style="border: 1px solid #ccc; padding: 8px; text-align: left; background: #ff6b35; color: white; font-weight: bold;">Tên sản phẩm</th>
              <th style="border: 1px solid #ccc; padding: 8px; text-align: center; background: #ff6b35; color: white; font-weight: bold;">IMEI</th>
              <th style="border: 1px solid #ccc; padding: 8px; text-align: right; background: #ff6b35; color: white; font-weight: bold;">Đơn giá (₫)</th>
              <th style="border: 1px solid #ccc; padding: 8px; text-align: right; background: #ff6b35; color: white; font-weight: bold;">Thành tiền (₫)</th>
            </tr>
          </thead>
          <tbody>
            ${(() => {
      // Sử dụng expandedProductList để hiển thị đúng dữ liệu
      const products = expandedProductList.value || []
      if (products.length === 0) {
        return '<tr><td colspan="6" style="border: 1px solid #ccc; padding: 8px; text-align: center;">Không có sản phẩm</td></tr>'
      }
      return products.map((product, index) => `
                <tr>
                  <td style="border: 1px solid #ccc; padding: 8px; text-align: center;">${product.stt || (index + 1)}</td>
                  <td style="border: 1px solid #ccc; padding: 8px; text-align: center; font-family: monospace; font-size: 12px; font-weight: 600;">
                    ${product.maCtsp || 'N/A'}
                  </td>
                  <td style="border: 1px solid #ccc; padding: 8px; text-align: left; font-weight: 500;">
                    ${product.tenSanPham || 'N/A'}${product.tenMauSac ? ' ' + product.tenMauSac : ''}${product.tenRom ? ' ' + product.tenRom : ''}
                  </td>
                  <td style="border: 1px solid #ccc; padding: 8px; text-align: center; font-family: monospace; font-size: 12px;">
                    ${product.imei || 'N/A'}
                  </td>
                  <td style="border: 1px solid #ccc; padding: 8px; text-align: right;">${formatCurrency(product.donGia || product.gia || 0)}</td>
                  <td style="border: 1px solid #ccc; padding: 8px; text-align: right; font-weight: 600;">${formatCurrency(product.donGia || product.gia || 0)}</td>
                </tr>
              `).join('')
    })()}
          </tbody>
        </table>


        <!-- Total -->
        <div style="text-align: right; margin-top: 15px; padding: 15px; background: #f8f9fa; border-radius: 8px;">
          <p style="margin: 5px 0; font-size: 16px;"><strong>Tổng tiền hàng:</strong> ${formatCurrency(trackingData.value.tongTienHang || 0)}</p>
          <p style="margin: 5px 0; font-size: 16px;"><strong>Giảm giá:</strong> -${formatCurrency(trackingData.value.giamGia || 0)}</p>
          <p style="margin: 5px 0; font-size: 18px; color: green; font-weight: bold;"><strong>Thành tiền:</strong> ${formatCurrency(trackingData.value.thanhTien || 0)}</p>
        </div>

        <!-- Notes -->
        ${trackingData.value.ghiChu ? `
          <div style="margin-top: 15px; padding: 10px; background: #f8f9fa; border-radius: 8px;">
            <h3 style="font-size: 16px; font-weight: bold; margin: 0 0 10px 0;">Ghi chú</h3>
            <p style="margin: 0; font-size: 14px;">${trackingData.value.ghiChu}</p>
          </div>
        ` : ''}

      </div>
    `

    // Tạo window mới để in
    const printWindow = window.open('', '_blank')
    printWindow.document.write(`
      <html>
        <head>
          <title>Hóa đơn ${trackingData.value.maHoaDon}</title>
          <style>
            @media print {
              body { margin: 0; padding: 0; }
              .invoice-print { margin: 0; padding: 20mm; }
            }
          </style>
        </head>
        <body>
          ${invoiceHTML}
        </body>
      </html>
    `)
    printWindow.document.close()

    // Đợi nội dung load xong rồi in
    printWindow.onload = function() {
      printWindow.print()
      printWindow.close()
    }

    toastRef.value?.success('Thành công', 'Đã mở cửa sổ in hóa đơn')

  } catch (error) {
    console.error('Lỗi khi in hóa đơn:', error)
    toastRef.value?.error('Lỗi', 'Có lỗi xảy ra khi in hóa đơn')
  }
}




// Helper function để lấy text trạng thái
function getStatusText(status: number): string {
  switch (status) {
    case 0: return 'Chờ xác nhận'
    case 1: return 'Đã thanh toán'
    case 2: return 'Chờ giao hàng'
    case 3: return 'Đang giao hàng'
    case 4: return 'Hoàn thành'
    case 5: return 'Đã hủy'
    default: return 'Không xác định'
  }
}

// Function để hiển thị modal cập nhật trạng thái
function openUpdateStatusModal() {
  if (!trackingData.value) {
    toastRef.value?.error('Lỗi', 'Không có dữ liệu hóa đơn để cập nhật')
    return
  }
  newStatus.value = trackingData.value.trangThai
  showUpdateStatusModal.value = true
}

// Function để đóng modal cập nhật trạng thái
function closeUpdateStatusModal() {
  showUpdateStatusModal.value = false
  newStatus.value = 0
}

// Function để cập nhật trạng thái hóa đơn từ modal
async function updateOrderStatusFromModal() {
  if (!trackingData.value) {
    toastRef.value?.error('Lỗi', 'Không có dữ liệu hóa đơn để cập nhật')
    return
  }

  if (newStatus.value === trackingData.value.trangThai) {
    toastRef.value?.warning('Cảnh báo', 'Trạng thái hiện tại đã giống với trạng thái mới')
    return
  }

  isUpdatingStatus.value = true

  try {
    const response = await api.put(`/api/hoa-don/${trackingData.value.id}/status`, {
      trangThai: newStatus.value
    })

    if (response.data) {
      // Cập nhật trạng thái trong trackingData
      trackingData.value.trangThai = newStatus.value

      // Cập nhật thời gian cho step tương ứng
      const currentStep = trackingSteps.value.find(step => step.status === newStatus.value)
      if (currentStep) {
        currentStep.time = new Date().toLocaleString('vi-VN')
      }

      toastRef.value?.success('Thành công', 'Cập nhật trạng thái thành công!')
      closeUpdateStatusModal()
    }
  } catch (error) {
    console.error('Lỗi khi cập nhật trạng thái:', error)
    toastRef.value?.error('Lỗi', 'Có lỗi xảy ra khi cập nhật trạng thái')
  } finally {
    isUpdatingStatus.value = false
  }
}


onMounted(() => {
  loadHoaDons()

  // Kiểm tra query parameter 'track' để tự động tìm kiếm hóa đơn
  const trackCode = route.query.track as string
  if (trackCode) {
    console.log('🎯 Auto tracking from URL parameter:', trackCode)
    trackingCode.value = trackCode
    switchToTab('tracking')

    // Tự động tìm kiếm sau 1 giây để đảm bảo component đã load xong
    setTimeout(() => {
      searchTracking()
    }, 1000)
  }
})

// Cleanup khi component bị unmount
onBeforeUnmount(() => {
  console.log('🧹 Cleaning up QR Scanner before component unmount...')
  closeQRScanner()
})

// Debug watcher để kiểm tra trackingData changes
watch(trackingData, (newData) => {
  if (newData) {
    console.log('🔍 trackingData changed:', newData)
    console.log('🔍 loaiHoaDon:', newData.loaiHoaDon)
    console.log('🔍 ngayTao:', newData.ngayTao)
    console.log('🔍 trangThai:', newData.trangThai)

    if (newData.loaiHoaDon === 'BAN_THUONG' || newData.loaiHoaDon === 'NORMAL') {
      console.log('🔍 BAN_THUONG order detected, checking getTrackingSteps...')
      console.log('🔍 getTrackingSteps result:', getTrackingSteps.value)
    }
  }
}, { deep: true })
</script>

<template>
  <div class="hoa-don-page">
    <PosHeader />


    <main class="main-content">

      <!-- Tab Navigation -->
      <section class="tab-section">
        <div class="tab-navigation">
          <button
            :class="['tab-button', { active: activeTab === 'list' }]"
            @click="switchToTab('list')"
          >
            <font-awesome-icon icon="list" />
            Danh sách hóa đơn
          </button>
          <button
            :class="['tab-button', { active: activeTab === 'tracking' }]"
            @click="switchToTab('tracking')"
          >
            <font-awesome-icon icon="truck" />
            Theo dõi đơn hàng
          </button>
        </div>
      </section>

      <!-- Tab Content -->
      <div class="tab-content">
        <!-- Tab 1: Danh sách hóa đơn -->
        <div v-if="activeTab === 'list'" class="tab-panel">
          <!-- Search and Filter Section -->
          <section class="search-filter-section">
            <!-- Search Bar -->
            <div class="search-bar">
              <div class="input-group">
                <font-awesome-icon icon="search" class="input-icon" />
                <input
                  v-model="searchKeyword"
                  type="text"
                  placeholder="Tìm kiếm theo mã hóa đơn, tên khách hàng, SĐT"
                  class="search-input"
                  @input="handleSearch"
                />
              </div>
              <button @click="toggleFilters" class="btn-filter">
                <font-awesome-icon icon="filter" />
                Bộ lọc
              </button>
              <button @click="exportToExcel" class="btn-excel">
                <font-awesome-icon icon="file-excel" />
                Xuất Excel
              </button>
            </div>

            <!-- Expandable Filter Options -->
            <div v-if="showFilters" class="filter-options">
              <div class="filter-grid">
                <!-- Row 1: 3 filters -->
                <div class="filter-group">
                  <label>Trạng thái:</label>
                  <select v-model="filterTrangThai" class="filter-select" @change="handleStatusChange">
                    <option value="">Tất cả trạng thái</option>
                    <option v-for="status in statusOptions" :key="status.value" :value="status.value">
                      {{ status.label }}
                    </option>
                  </select>
                </div>
                <div class="filter-group">
                  <label>Loại đơn:</label>
                  <select v-model="filterLoaiHoaDon" class="filter-select" @change="handleLoaiHoaDonChange">
                    <option value="">Tất cả loại</option>
                    <option value="BAN_THUONG">Bán tại quầy</option>
                    <option value="BAN_ONLINE">Bán online</option>
                  </select>
                </div>
                <div class="filter-group">
                  <label>Sắp xếp:</label>
                  <select v-model="quickSort" class="filter-select" @change="handleQuickSortChange">
                    <option v-for="option in quickSortOptions" :key="option.value" :value="option.value">
                      {{ option.label }}
                    </option>
                  </select>
                </div>

                <!-- Row 2: 2 date inputs + 2 buttons -->
                <div class="filter-group">
                  <label>Từ ngày:</label>
                  <input v-model="dateFrom" type="date" class="filter-input" @change="loadHoaDons" />
                </div>
                <div class="filter-group">
                  <label>Đến ngày:</label>
                  <input v-model="dateTo" type="date" class="filter-input" @change="loadHoaDons" />
                </div>
                <div class="filter-group button-group">
                  <button @click="resetFilters" class="btn-reset">
                    <font-awesome-icon icon="times" />
                    Xóa bộ lọc
                  </button>
                </div>
              </div>
            </div>
          </section>

          <!-- Table Section -->
          <section class="table-section">
            <div class="table-container">
              <div class="table-header">
                <h3>Danh sách hóa đơn</h3>
              </div>
              <div class="table-wrapper">
                <table>
                  <thead>
                  <tr>
                    <th>STT</th>
                    <th>Mã HD</th>
                    <th>Khách hàng</th>
                    <th>SĐT</th>
                    <th>Loại đơn</th>
                    <th>Tổng tiền</th>
                    <th>Trạng thái</th>
                    <th>Ngày tạo</th>
                    <th>Thao tác</th>
                  </tr>
                  </thead>
                  <tbody>
                  <tr v-if="loading">
                    <td colspan="9" class="text-center">
                      <div class="loading">
                        <font-awesome-icon icon="spinner" class="fa-spin" />
                        Đang tải...
                      </div>
                    </td>
                  </tr>
                  <tr v-else-if="hoaDons.length === 0">
                    <td colspan="9" class="text-center">
                      <div class="empty-state">
                        <font-awesome-icon icon="file-invoice" class="empty-icon" />
                        <p>Không có dữ liệu</p>
                      </div>
                    </td>
                  </tr>
                  <tr v-else v-for="(hd, index) in hoaDons" :key="hd.id">
                    <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
                    <td>
                      <div class="invoice-code">
                        {{ hd.maHoaDon }}
                      </div>
                    </td>
                    <td>
                      <div class="customer-info">
                        <div class="customer-name">{{ hd.tenKhachHang || 'Khách lẻ' }}</div>
                      </div>
                    </td>
                    <td>
                      <div class="phone-info" v-if="hd.soDienThoai">
                        {{ hd.soDienThoai }}
                      </div>
                      <div class="no-phone" v-else>
                        <span class="text-muted">Không có</span>
                      </div>
                    </td>
                    <td>
                        <span :class="['order-type', (hd.loaiHoaDon === 'BAN_ONLINE' || hd.loaiHoaDon === 'DELIVERY') ? 'online' : 'normal']">
                          {{ (hd.loaiHoaDon === 'BAN_ONLINE' || hd.loaiHoaDon === 'DELIVERY') ? 'Bán online' : 'Bán tại quầy' }}
              </span>
                    </td>
                    <td>
                      <div class="amount">
                        {{ formatCurrency(hd.tongTienSauGiam || hd.tongTien) }}
                      </div>
                    </td>
                    <td>
                        <span :class="['status-badge', getStatusBadgeClass(hd.trangThai)]">
                          {{ getStatusName(hd.trangThai) }}
                        </span>
                    </td>
                    <td>
                      <div class="date-info">
                        {{ formatDate(hd.ngayTao) }}
                      </div>
                    </td>
                    <td>
                      <div class="action-buttons">
                        <button @click="viewDetailsAndTrack(hd)" class="btn-view" title="Xem chi tiết và theo dõi">
                          <font-awesome-icon icon="eye" />
                        </button>
                      </div>
                    </td>
                  </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </section>

          <!-- Pagination -->
          <section class="pagination-section">
            <div class="pagination-controls">
              <!-- Page Size Selector -->
              <div class="page-size-selector">
                <label for="pageSize">Hiển thị:</label>
                <select v-model="pageSize" @change="handlePageSizeChange" id="pageSize" class="page-size-select">
                  <option value="5">5</option>
                  <option value="10">10</option>
                  <option value="20">20</option>
                  <option value="50">50</option>
                  <option value="100">100</option>
                </select>
                <span>hóa đơn/trang</span>
              </div>

              <!-- Pagination Info -->
              <div class="pagination-info">
                <span>Hiển thị {{ Math.min((currentPage - 1) * pageSize + 1, totalElements) }} - {{ Math.min(currentPage * pageSize, totalElements) }} trong tổng số {{ totalElements }} hóa đơn</span>
              </div>
            </div>

            <!-- Page Navigation -->
            <div class="pagination" v-if="totalPages > 0">

              <button @click="() => { console.log('Previous clicked'); goToPage(currentPage - 1); }" :disabled="currentPage <= 1" class="page-btn">
                <font-awesome-icon icon="chevron-left" />
                Trước
              </button>

              <!-- Page Numbers -->
              <div class="page-numbers">
                <!-- First page -->
                <button
                  v-if="totalPages > 1 && currentPage > 3"
                  @click="() => { console.log('First page clicked'); goToPage(1); }"
                  class="page-number"
                >
                  1
                </button>
                <span v-if="totalPages > 1 && currentPage > 4" class="page-ellipsis">...</span>

                <!-- Pages around current page -->
                <template v-for="page in getVisiblePages()" :key="page">
                  <button
                    @click="() => { console.log('Page number clicked:', page); goToPage(page); }"
                    :class="['page-number', { 'active': page === currentPage }]"
                  >
                    {{ page }}
                  </button>
                </template>

                <!-- Last page -->
                <span v-if="totalPages > 1 && currentPage < totalPages - 3" class="page-ellipsis">...</span>
                <button
                  v-if="totalPages > 1 && currentPage < totalPages - 2"
                  @click="() => { console.log('Last page clicked:', totalPages); goToPage(totalPages); }"
                  class="page-number"
                >
                  {{ totalPages }}
                </button>
              </div>

              <button @click="() => { console.log('Next clicked'); goToPage(currentPage + 1); }" :disabled="currentPage >= totalPages" class="page-btn">
                Sau
                <font-awesome-icon icon="chevron-right" />
              </button>
            </div>
          </section>
        </div>

        <!-- Tab 2: Theo dõi đơn hàng -->
        <div v-if="activeTab === 'tracking'" class="tab-panel">
          <div class="tracking-container">
            <!-- Search Order -->
            <div class="tracking-search">
              <div class="search-box">
                <div class="input-group">
                  <font-awesome-icon icon="search" class="input-icon" />
                  <input
                    v-model="trackingCode"
                    type="text"
                    placeholder="Nhập mã đơn hàng để theo dõi..."
                    class="tracking-input"
                    @keyup.enter="searchTracking"
                  />
                </div>
                <div class="tracking-buttons">
                  <button @click="searchTracking" class="btn-track-search" title="Theo dõi">
                    <font-awesome-icon icon="search" />
                  </button>
                  <button @click="startQRScanner" class="btn-qr-scanner" title="Quét QR">
                    <font-awesome-icon icon="qrcode" class="qr-icon" />
                    <span class="qr-text">QR Code</span>
                  </button>
                  <button @click="resetTrackingPage" class="btn-refresh" title="Làm mới">
                    <font-awesome-icon icon="refresh" />
                  </button>
                </div>
              </div>
            </div>

            <!-- Tracking Result -->
            <div v-if="trackingData" class="tracking-result">
              <!-- Timeline Container -->
              <div class="timeline-container">
                <div class="timeline-header">
                  <font-awesome-icon icon="clock" />
                  <span>Trạng Thái Hóa Đơn</span>
                </div>

                <!-- Progress Bar -->
                <div class="progress-container">
                  <div class="progress-bar">
                    <div
                      v-for="(step, index) in getTrackingSteps"
                      :key="index"
                      :class="['progress-step', {
                        active: index === getCurrentStepIndex(),
                        completed: index < getCurrentStepIndex() || (step.status === 4 && trackingData?.trangThai === 4) || (trackingData?.loaiHoaDon === 'BAN_THUONG' || trackingData?.loaiHoaDon === 'NORMAL'),
                        clickable: (index === getCurrentStepIndex() || (index > getCurrentStepIndex() && step.status !== 0)) && !isUpdatingStatus && !(trackingData?.loaiHoaDon === 'BAN_THUONG' || trackingData?.loaiHoaDon === 'NORMAL')
                      }]"
                      @click="(trackingData?.loaiHoaDon === 'BAN_THUONG' || trackingData?.loaiHoaDon === 'NORMAL') ? null : handleStepClick(step, index)"
                    >
                      <div class="step-icon">
                        <font-awesome-icon
                          :icon="(index < getCurrentStepIndex() || (step.status === 4 && trackingData?.trangThai === 4) || (trackingData?.loaiHoaDon === 'BAN_THUONG' || trackingData?.loaiHoaDon === 'NORMAL')) ? 'check' : step.icon"
                          v-if="index < getCurrentStepIndex() || index === getCurrentStepIndex() || (step.status === 4 && trackingData?.trangThai === 4) || (trackingData?.loaiHoaDon === 'BAN_THUONG' || trackingData?.loaiHoaDon === 'NORMAL')"
                        />
                        <font-awesome-icon :icon="step.icon" v-else />
                      </div>
                      <div class="step-content">
                        <div class="step-title">{{ step.title }}</div>
                        <div class="step-time">{{ step.time || 'Không có thời gian' }}</div>
                        <div v-if="(index === getCurrentStepIndex() || (index > getCurrentStepIndex() && step.status !== 0)) && !isUpdatingStatus && !(trackingData?.loaiHoaDon === 'BAN_THUONG' || trackingData?.loaiHoaDon === 'NORMAL')" class="step-hint">
                          {{ index === getCurrentStepIndex() ? '' : (step.status === 4 ? 'Click để hoàn thành' : 'Click để cập nhật') }}
                        </div>
                        <div v-if="isUpdatingStatus && (index === getCurrentStepIndex() || (index > getCurrentStepIndex() && step.status !== 0))" class="step-loading">
                          <font-awesome-icon icon="spinner" spin />
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>


              <!-- Order Information -->
              <div class="order-info-grid">
                <!-- Order Details -->
                <div class="info-card">
                  <h3>Thông Tin Đơn Hàng</h3>
                  <div class="info-item">
                    <label>Mã đơn hàng:</label>
                    <span class="order-code">{{ trackingData.maHoaDon }}</span>
                  </div>
                  <div class="info-item">
                    <label>Loại đơn:</label>
                    <span :class="['order-type', (trackingData.loaiHoaDon === 'BAN_ONLINE' || trackingData.loaiHoaDon === 'DELIVERY') ? 'online' : 'normal']">
                      <font-awesome-icon :icon="(trackingData.loaiHoaDon === 'BAN_ONLINE' || trackingData.loaiHoaDon === 'DELIVERY') ? 'laptop' : 'store'" />
                      {{ (trackingData.loaiHoaDon === 'BAN_ONLINE' || trackingData.loaiHoaDon === 'DELIVERY') ? 'Bán online' : 'Bán tại quầy' }}
                    </span>
                  </div>
                  <div class="info-item">
                    <label>Trạng thái:</label>
                    <span :class="getStatusClass(trackingData.trangThai)" :style="{ color: getStatusColor(trackingData.trangThai) }">
                      <font-awesome-icon :icon="getStatusIcon(trackingData.trangThai)" />
                      {{ getStatusName(trackingData.trangThai) }}
                    </span>
                  </div>
                  <div class="info-item">
                    <label>Phiếu giảm giá:</label>
                    <span class="voucher-code">{{ trackingData.phieuGiamGia || 'Không có' }}</span>
                  </div>
                  <div class="info-item">
                    <label>Ngày Đặt:</label>
                    <span class="order-date">{{ formatDateTime(trackingData.ngayDat) }}</span>
                  </div>
                  <div class="info-item">
                    <label>Nhân viên:</label>
                    <span class="employee-name">{{ trackingData.tenNhanVien || 'Không xác định' }}</span>
                  </div>
                </div>

                <!-- Customer Information -->
                <div class="info-card">
                  <h3>Thông Tin Khách Hàng</h3>
                  <div class="info-item">
                    <label>Tên khách hàng:</label>
                    <span>{{ trackingData.tenKhachHang }}</span>
                  </div>
                  <div class="info-item">
                    <label>Số điện thoại:</label>
                    <span>{{ trackingData.soDienThoai }}</span>
                  </div>
                  <div class="info-item">
                    <label>Địa chỉ:</label>
                    <span>{{ trackingData.diaChi }}</span>
                  </div>
                  <div class="info-item">
                    <label>Email:</label>
                    <span>{{ trackingData.email || 'Không có' }}</span>
                  </div>
                  <div class="info-item">
                    <label>Ghi Chú:</label>
                    <span>{{ trackingData.ghiChu || 'Không có' }}</span>
                  </div>
                </div>

                <!-- Product List -->
                <div class="info-card">
                  <h3>Danh sách sản phẩm</h3>
                  <div v-if="expandedProductList && expandedProductList.length > 0" class="product-list">
                    <div v-for="(product, index) in expandedProductList" :key="`${product.id || index}-${product.imeiIndex}`" class="product-item">
                      <div class="product-image">
                        <img :src="product.hinhAnh || '/placeholder-product.png'" :alt="product.tenSanPham" />
                      </div>
                      <div class="product-info">
                        <div class="product-name">{{ product.tenSanPham }}</div>
                        <div class="product-specs" v-if="product.tenRam || product.tenRom || product.tenMauSac">
                          <span v-if="product.tenRam">{{ product.tenRam }}</span>
                          <span v-if="product.tenRom">{{ product.tenRom }}</span>
                          <span v-if="product.tenMauSac">{{ product.tenMauSac }}</span>
                        </div>
                        <div class="product-price">{{ formatCurrency(product.gia) }}</div>
                      </div>
                      <div class="product-actions">
                        <span class="quantity-badge">{{ product.soLuong }}</span>
                        <div v-if="product.imei" class="imei-info">
                          <small>IMEI: {{ product.imei }}</small>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div v-else class="no-products">
                    <font-awesome-icon icon="box-open" />
                    <p>Không có sản phẩm trong đơn hàng này</p>
                  </div>
                </div>

                <!-- Order Summary -->
                <div class="info-card">
                  <h3>Tổng kết đơn hàng</h3>
                  <div class="summary-item">
                    <label>Tổng tiền hàng:</label>
                    <span class="amount">{{ formatCurrency(trackingData.tongTienHang) }}</span>
                    <div class="amount-note">Giá trị sản phẩm</div>
                  </div>
                  <div class="summary-item">
                    <label>Giảm giá:</label>
                    <span class="discount">(-{{ formatCurrency(trackingData.giamGia) }})</span>
                    <div class="discount-note">Khuyến mãi áp dụng</div>
                  </div>
                  <div class="summary-item total">
                    <label>Thành tiền:</label>
                    <span class="total-amount">{{ formatCurrency(trackingData.thanhTien) }}</span>
                    <div class="total-note">Số tiền phải thanh toán</div>
                  </div>
                </div>
              </div>

              <!-- Action Buttons -->
              <div class="tracking-actions">
                <div class="action-group">
                  <button class="btn-primary" @click="showImeiDetailsForAllProducts()">
                    <font-awesome-icon icon="barcode" />
                    Xem IMEI
                  </button>
                </div>
                <div class="action-group">
                  <button class="btn-primary" @click="openUpdateStatusModal">
                    <font-awesome-icon icon="edit" />
                    Cập nhật
                  </button>
                  <button class="btn-primary" @click="printInvoice">
                    <font-awesome-icon icon="print" />
                    In hoá đơn
                  </button>
                </div>
              </div>
            </div>

            <!-- No Result -->
            <div v-else-if="trackingSearched && !trackingData" class="no-result">
              <div class="no-result-icon">
                <font-awesome-icon icon="exclamation-triangle" />
              </div>
              <h3>Không tìm thấy đơn hàng</h3>
              <p>Mã đơn hàng "{{ trackingCode }}" không tồn tại hoặc đã bị xóa.</p>
            </div>
          </div>
        </div>
      </div>
    </main>


    <!-- Details Modal -->
    <div v-if="showDetailsModal" class="modal">
      <div class="modal-content large">
        <div class="modal-header">
          <h2>Chi tiết hóa đơn</h2>
          <button @click="showDetailsModal = false" class="btn-close">×</button>
        </div>
        <div v-if="selectedHoaDon" class="details-content">
          <div class="details-grid">
            <div class="detail-section">
              <h3>Thông tin hóa đơn</h3>
              <div class="detail-item">
                <label>Mã hóa đơn:</label>
                <span>{{ selectedHoaDon.maHoaDon }}</span>
              </div>
              <div class="detail-item">
                <label>Loại đơn:</label>
                <span :class="['order-type', (selectedHoaDon.loaiHoaDon === 'BAN_ONLINE' || selectedHoaDon.loaiHoaDon === 'DELIVERY') ? 'online' : 'normal']">
                  {{ (selectedHoaDon.loaiHoaDon === 'BAN_ONLINE' || selectedHoaDon.loaiHoaDon === 'DELIVERY') ? 'Bán online' : 'Bán tại quầy' }}
                </span>
              </div>
              <div class="detail-item">
                <label>Trạng thái:</label>
                <span :class="getStatusClass(selectedHoaDon.trangThai)" :style="{ color: getStatusColor(selectedHoaDon.trangThai) }">
                  {{ getStatusName(selectedHoaDon.trangThai) }}
                </span>
              </div>
              <div class="detail-item">
                <label>Ngày tạo:</label>
                <span>{{ formatDateTime(selectedHoaDon.ngayTao) }}</span>
              </div>
            </div>
            <div class="detail-section">
              <h3>Thông tin khách hàng</h3>
              <div class="detail-item">
                <label>Tên khách hàng:</label>
                <span>{{ selectedHoaDon.tenKhachHang || 'Khách lẻ' }}</span>
              </div>
              <div class="detail-item">
                <label>Số điện thoại:</label>
                <span>{{ selectedHoaDon.soDienThoai || 'Không có' }}</span>
              </div>
            </div>
            <div class="detail-section">
              <h3>Thông tin thanh toán</h3>
              <div class="detail-item">
                <label>Tổng tiền:</label>
                <span class="amount">{{ formatCurrency(selectedHoaDon.tongTien) }}</span>
              </div>
              <div class="detail-item" v-if="selectedHoaDon.tongTienSauGiam">
                <label>Tiền sau giảm:</label>
                <span class="amount">{{ formatCurrency(selectedHoaDon.tongTienSauGiam) }}</span>
              </div>
              <div class="detail-item" v-if="selectedHoaDon.ghiChu">
                <label>Ghi chú:</label>
                <span>{{ selectedHoaDon.ghiChu }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Payment Method Modal -->
    <div v-if="showPaymentModal" class="modal-overlay" @click="closePaymentModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>Chọn phương thức thanh toán</h3>
          <button class="close-btn" @click="closePaymentModal">
            <font-awesome-icon icon="times" />
          </button>
        </div>

        <div class="modal-body">
          <p class="modal-description">
            Đơn hàng đã được xác nhận. Vui lòng chọn phương thức thanh toán:
          </p>

          <div class="payment-options">
            <div class="payment-option" @click="selectPaymentMethod('cod')">
              <div class="payment-icon cod">
                <font-awesome-icon icon="truck" />
              </div>
              <div class="payment-info">
                <h4>COD (Thanh toán khi nhận hàng)</h4>
                <p>Khách hàng thanh toán khi nhận được sản phẩm</p>
                <div class="next-status">{{ trackingData?.loaiHoaDon === 'BAN_THUONG' ? '→ Hoàn thành đơn hàng' : '→ Chuyển sang "Chờ giao hàng"' }}</div>
              </div>
            </div>

            <div class="payment-option" @click="selectPaymentMethod('prepaid')">
              <div class="payment-icon prepaid">
                <font-awesome-icon icon="credit-card" />
              </div>
              <div class="payment-info">
                <h4>Đã thanh toán trước</h4>
                <p>Khách hàng đã thanh toán qua chuyển khoản/thẻ</p>
                <div class="next-status">{{ trackingData?.loaiHoaDon === 'BAN_THUONG' ? '→ Hoàn thành đơn hàng' : '→ Chuyển sang "Đã thanh toán chờ xác nhận"' }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- QR Scanner Modal -->
    <div v-if="showQRScannerModal" class="fixed inset-0 bg-white/20 flex items-center justify-center z-50" @click="closeQRScanner">
      <div class="bg-gradient-to-br from-purple-50 to-pink-50 border-2 border-purple-200 rounded-xl w-4/5 max-w-2xl max-h-[80vh] flex flex-col shadow-2xl" @click.stop>
        <!-- Modal Header -->
        <div class="p-6 border-b border-purple-200 bg-gradient-to-r from-purple-100 to-pink-100">
          <div class="flex items-center justify-between">
            <h3 class="text-xl font-bold text-gray-800">Quét QR Code</h3>
            <button @click="closeQRScanner" class="text-gray-500 hover:text-gray-700 transition-colors">
              <font-awesome-icon icon="times" class="text-xl" />
            </button>
          </div>
        </div>

        <!-- Modal Body -->
        <div class="flex-1 overflow-y-auto p-6">
          <div class="text-center space-y-6">
            <!-- Camera Container -->
            <div class="relative bg-gray-900 rounded-lg overflow-hidden">
              <video
                id="qr-scanner-video"
                class="w-full h-64 object-cover"
                autoplay
                muted
                playsinline
              ></video>

              <!-- QR Scanner Overlay -->
              <div class="absolute inset-0 flex items-center justify-center pointer-events-none">
                <div class="relative w-48 h-48 border-2 border-orange-400 rounded-lg">
                  <!-- Scan Line Animation -->
                  <div class="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-transparent via-orange-400 to-transparent animate-pulse"></div>

                  <!-- Corner indicators -->
                  <div class="absolute top-0 left-0 w-6 h-6 border-t-2 border-l-2 border-orange-400"></div>
                  <div class="absolute top-0 right-0 w-6 h-6 border-t-2 border-r-2 border-orange-400"></div>
                  <div class="absolute bottom-0 left-0 w-6 h-6 border-b-2 border-l-2 border-orange-400"></div>
                  <div class="absolute bottom-0 right-0 w-6 h-6 border-b-2 border-r-2 border-orange-400"></div>
                </div>
              </div>
            </div>

            <!-- Instructions -->
            <div class="bg-gray-100 rounded-lg p-4">
              <p class="text-gray-700 font-medium">Đưa camera vào QR code để quét</p>
              <p class="text-sm text-gray-500 mt-1">QR code sẽ được tự động nhận diện</p>
            </div>
          </div>
        </div>

        <!-- Modal Footer -->
        <div class="p-6 border-t border-purple-200 bg-gradient-to-r from-purple-50 to-pink-50">
          <div class="flex justify-end space-x-3">
            <button
              @click="closeQRScanner"
              class="px-6 py-2 bg-gray-500 text-white font-medium rounded-lg hover:bg-gray-600 transition-all"
            >
              Đóng
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Confirm Modal -->
    <div v-if="showConfirmModal" class="fixed inset-0 bg-white/20 flex items-center justify-center z-50" @click="cancelAction">
      <div class="bg-gradient-to-br from-purple-50 to-pink-50 border-2 border-purple-200 rounded-xl w-4/5 max-w-md max-h-[80vh] flex flex-col shadow-2xl" @click.stop>
        <!-- Modal Header -->
        <div class="p-6 border-b border-purple-200 bg-gradient-to-r from-purple-100 to-pink-100">
          <div class="flex items-center justify-between">
            <h3 class="text-xl font-semibold text-purple-800">Xác nhận thay đổi</h3>
            <button
              @click="cancelAction"
              class="w-8 h-8 flex items-center justify-center bg-purple-200 hover:bg-purple-300 rounded-full text-purple-700 transition-colors"
            >
              ×
            </button>
          </div>
        </div>

        <!-- Modal Body -->
        <div class="flex-1 overflow-y-auto p-6">
          <div class="text-center space-y-6">
            <div class="w-16 h-16 mx-auto bg-yellow-100 rounded-full flex items-center justify-center">
              <font-awesome-icon icon="exclamation-triangle" class="text-2xl text-yellow-600" />
            </div>
            <p class="text-gray-800 text-lg font-medium">{{ confirmMessage }}</p>
          </div>
        </div>

        <!-- Modal Footer -->
        <div class="p-6 border-t border-purple-200 bg-gradient-to-r from-purple-50 to-pink-50 flex justify-end gap-3">
          <button
            @click="cancelAction"
            class="px-6 py-2 bg-white border border-gray-300 rounded-lg text-gray-700 font-medium hover:bg-gray-50 transition-colors"
          >
            Hủy bỏ
          </button>
          <button
            @click="confirmAction"
            class="px-6 py-2 bg-gradient-to-r from-purple-500 to-pink-500 text-white font-medium rounded-lg hover:from-purple-600 hover:to-pink-600 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
            :disabled="isUpdatingStatus"
          >
            <font-awesome-icon v-if="isUpdatingStatus" icon="spinner" spin />
            Xác nhận
          </button>
        </div>
      </div>
    </div>

    <!-- IMEI Modal - Same layout as IMEI selection modal -->
    <div v-if="showImeiModal" class="fixed inset-0 bg-white/20 flex items-center justify-center z-50">
      <div class="bg-gradient-to-br from-purple-50 to-pink-50 border-2 border-purple-200 rounded-xl w-4/5 max-w-2xl max-h-[80vh] flex flex-col shadow-2xl">
        <!-- Modal Header -->
        <div class="p-6 border-b border-purple-200 bg-gradient-to-r from-purple-100 to-pink-100">
          <div class="flex items-center justify-between">
            <h3 class="text-xl font-semibold text-purple-800">Thông tin sản phẩm và IMEI</h3>
            <button
              @click="closeImeiModal"
              class="w-8 h-8 flex items-center justify-center bg-purple-200 hover:bg-purple-300 rounded-full text-purple-700 transition-colors"
            >
              ×
            </button>
          </div>
        </div>

        <!-- Modal Body -->
        <div class="flex-1 overflow-y-auto p-6">
          <div v-if="Array.isArray(selectedProductForImei)" class="space-y-6">
            <div v-for="(product, index) in selectedProductForImei" :key="product.id || index" class="space-y-4">
              <!-- Product Info -->
              <div class="bg-white rounded-lg p-4 border border-gray-200">
                <div class="flex items-center space-x-4 mb-3">
                  <img :src="product.hinhAnh || '/placeholder-product.png'" :alt="product.tenSanPham" class="w-16 h-16 object-cover rounded-lg" />
                  <div class="flex-1">
                    <h4 class="font-bold text-lg text-gray-800">{{ product.tenSanPham }}</h4>
                    <div class="flex flex-wrap gap-2 mt-1">
                      <span v-if="product.tenRam" class="px-2 py-1 bg-gray-100 text-gray-600 text-sm rounded">{{ product.tenRam }}</span>
                      <span v-if="product.tenRom" class="px-2 py-1 bg-gray-100 text-gray-600 text-sm rounded">{{ product.tenRom }}</span>
                      <span v-if="product.tenMauSac" class="px-2 py-1 bg-gray-100 text-gray-600 text-sm rounded">{{ product.tenMauSac }}</span>
                    </div>
                    <div class="text-orange-600 font-bold text-lg mt-1">{{ formatCurrency(product.gia) }}</div>
                  </div>
                </div>
              </div>

              <!-- IMEI Section -->
              <div class="space-y-3">
                <h5 class="font-semibold text-gray-700">Mã IMEI:</h5>
                <div v-if="product.imeis && product.imeis.length > 0" class="space-y-2">
                  <div v-for="(imei, imeiIndex) in product.imeis" :key="imeiIndex" class="bg-gradient-to-r from-orange-50 to-yellow-50 border-2 border-orange-300 rounded-lg p-4 flex items-center justify-between">
                    <div class="flex-1">
                      <div class="font-bold text-gray-800 text-lg font-mono">{{ imei }}</div>
                      <div class="text-orange-600 text-sm font-medium">Khả dụng</div>
                    </div>
                    <div class="w-7 h-7 bg-orange-500 rounded-full flex items-center justify-center text-white font-bold">
                      ✓
                    </div>
                  </div>
                </div>
                <div v-else class="text-center py-8 text-gray-500 italic">
                  Không có IMEI
                </div>
              </div>
            </div>
          </div>

          <div v-else-if="selectedProductForImei" class="space-y-4">
            <!-- Product Info -->
            <div class="bg-white rounded-lg p-4 border border-gray-200">
              <div class="flex items-center space-x-4 mb-3">
                <img :src="selectedProductForImei.hinhAnh || '/placeholder-product.png'" :alt="selectedProductForImei.tenSanPham" class="w-16 h-16 object-cover rounded-lg" />
                <div class="flex-1">
                  <h4 class="font-bold text-lg text-gray-800">{{ selectedProductForImei.tenSanPham }}</h4>
                  <div class="flex flex-wrap gap-2 mt-1">
                    <span v-if="selectedProductForImei.tenRam" class="px-2 py-1 bg-gray-100 text-gray-600 text-sm rounded">{{ selectedProductForImei.tenRam }}</span>
                    <span v-if="selectedProductForImei.tenRom" class="px-2 py-1 bg-gray-100 text-gray-600 text-sm rounded">{{ selectedProductForImei.tenRom }}</span>
                    <span v-if="selectedProductForImei.tenMauSac" class="px-2 py-1 bg-gray-100 text-gray-600 text-sm rounded">{{ selectedProductForImei.tenMauSac }}</span>
                  </div>
                  <div class="text-orange-600 font-bold text-lg mt-1">{{ formatCurrency(selectedProductForImei.gia) }}</div>
                </div>
              </div>
            </div>

            <!-- IMEI Section -->
            <div class="space-y-3">
              <h5 class="font-semibold text-gray-700">Mã IMEI:</h5>
              <div v-if="selectedProductForImei.imeis && selectedProductForImei.imeis.length > 0" class="space-y-2">
                <div v-for="(imei, imeiIndex) in selectedProductForImei.imeis" :key="imeiIndex" class="bg-gradient-to-r from-orange-50 to-yellow-50 border-2 border-orange-300 rounded-lg p-4 flex items-center justify-between">
                  <div class="flex-1">
                    <div class="font-bold text-gray-800 text-lg font-mono">{{ imei }}</div>
                    <div class="text-orange-600 text-sm font-medium">Khả dụng</div>
                  </div>
                  <div class="w-7 h-7 bg-orange-500 rounded-full flex items-center justify-center text-white font-bold">
                    ✓
                  </div>
                </div>
              </div>
              <div v-else class="text-center py-8 text-gray-500 italic">
                Không có IMEI
              </div>
            </div>
          </div>
        </div>

        <!-- Modal Footer -->
        <div class="p-6 border-t border-purple-200 bg-gradient-to-r from-purple-50 to-pink-50 flex justify-end">
          <button
            @click="closeImeiModal"
            class="px-6 py-2 bg-white border border-gray-300 rounded-lg text-gray-700 font-medium hover:bg-gray-50 transition-colors"
          >
            Đóng
          </button>
        </div>
      </div>
    </div>

    <!-- Update Status Modal -->
    <div v-if="showUpdateStatusModal" class="fixed inset-0 bg-white/20 flex items-center justify-center z-50" @click="closeUpdateStatusModal">
      <div class="bg-gradient-to-br from-purple-50 to-pink-50 border-2 border-purple-200 rounded-xl w-4/5 max-w-2xl max-h-[80vh] flex flex-col shadow-2xl" @click.stop>
        <!-- Modal Header -->
        <div class="p-6 border-b border-purple-200 bg-gradient-to-r from-purple-100 to-pink-100">
          <div class="flex items-center justify-between">
            <h3 class="text-xl font-semibold text-purple-800">Cập nhật trạng thái đơn hàng</h3>
            <button
              @click="closeUpdateStatusModal"
              class="w-8 h-8 flex items-center justify-center bg-purple-200 hover:bg-purple-300 rounded-full text-purple-700 transition-colors"
            >
              ×
            </button>
          </div>
        </div>

        <!-- Modal Body -->
        <div class="flex-1 overflow-y-auto p-6">
          <div class="space-y-6">
            <div class="bg-white rounded-lg p-4 border border-purple-200">
              <label class="block text-sm font-medium text-purple-700 mb-2">Mã đơn hàng:</label>
              <p class="text-gray-800 font-semibold">{{ trackingData?.maHoaDon }}</p>
            </div>

            <div class="bg-white rounded-lg p-4 border border-purple-200">
              <label class="block text-sm font-medium text-purple-700 mb-2">Trạng thái hiện tại:</label>
              <p class="text-gray-800 font-semibold">{{ getStatusText(trackingData?.trangThai) }}</p>
            </div>

            <div class="bg-white rounded-lg p-4 border border-purple-200">
              <label class="block text-sm font-medium text-purple-700 mb-2">Trạng thái mới:</label>
              <select v-model="newStatus" class="w-full px-3 py-2 border border-purple-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500 text-gray-800">
                <option v-for="status in statusOptions" :key="status.value" :value="status.value">
                  {{ status.label }}
                </option>
              </select>
            </div>
          </div>
        </div>

        <!-- Modal Footer -->
        <div class="p-6 border-t border-purple-200 bg-gradient-to-r from-purple-50 to-pink-50 flex justify-end gap-3">
          <button
            @click="closeUpdateStatusModal"
            class="px-6 py-2 bg-white border border-gray-300 rounded-lg text-gray-700 font-medium hover:bg-gray-50 transition-colors"
          >
            Hủy
          </button>
          <button
            @click="updateOrderStatusFromModal"
            class="px-6 py-2 bg-gradient-to-r from-purple-500 to-pink-500 text-white font-medium rounded-lg hover:from-purple-600 hover:to-pink-600 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
            :disabled="isUpdatingStatus"
          >
            <font-awesome-icon v-if="isUpdatingStatus" icon="spinner" spin />
            {{ isUpdatingStatus ? 'Đang cập nhật...' : 'Cập nhật' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Toast Component -->
    <Toast ref="toastRef" />
  </div>
</template>

<style scoped>
.hoa-don-page {
  min-height: 100vh;
  background: var(--bg-primary, #f8f9fa);
}

.main-content {
  padding: 20px;
  padding-top: 100px;
  width: 100%;
  margin: 0;
}


/* Tab Navigation */
.tab-section {
  margin-top: 20px;
  margin-bottom: 24px;
}

.tab-navigation {
  display: flex;
  background: white;
  border-radius: 12px;
  padding: 4px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  gap: 4px;
}

.tab-button {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px 24px;
  border: none;
  background: transparent;
  color: #718096;
  cursor: pointer;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.tab-button:hover {
  background: #f7fafc;
  color: #4a5568;
}

.tab-button.active {
  background: #ff6b35; /* Orange */
  color: white;
  border-bottom: 3px solid #ff6b35;
}

.tab-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  overflow: hidden;
}

.tab-panel {
  padding: 24px;
}

/* Search and Filter Section */
.search-filter-section {
  background: white;
  padding: 28px;
  margin-bottom: 24px;
  border-radius: 16px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
  border: 1px solid #e2e8f0;
  position: relative;
  overflow: hidden;
}

.search-filter-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #f97316, #ea580c, #dc2626);
  border-radius: 16px 16px 0 0;
}

/* Search filter container styles moved to section */

.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.input-group {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #718096;
  z-index: 10;
  pointer-events: none;
  font-size: 16px;
  width: 16px;
  height: 16px;
}

.search-input {
  width: 100%;
  height: 48px;
  padding: 14px 18px 14px 45px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  background: #fafbfc;
  font-size: 14px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: #1f2937;
  font-weight: 500;
  position: relative;
  box-sizing: border-box;
}

.search-input:focus {
  outline: none;
  border-color: #f97316;
  background: white;
  box-shadow: 0 0 0 4px rgba(249, 115, 22, 0.1), 0 4px 12px rgba(0, 0, 0, 0.05);
  transform: translateY(-1px);
}

.search-input::placeholder {
  color: #9ca3af;
  font-weight: 400;
}

.btn-filter {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 48px;
  padding: 0 16px;
  background: #f8fafc;
  color: #64748b;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s ease;
  box-sizing: border-box;
  white-space: nowrap;
}

.btn-filter:hover {
  background: #f1f5f9;
  color: #374151;
  border-color: #cbd5e1;
  transform: translateY(-1px);
}

.btn-excel {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 48px;
  padding: 0 16px;
  background: #22c55e;
  color: white;
  border: 1px solid #22c55e;
  border-radius: 12px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
  box-sizing: border-box;
  white-space: nowrap;
}

.btn-excel:hover {
  background: #16a34a;
  color: white;
  border-color: #16a34a;
  transform: translateY(-1px);
}

.filter-options {
  border-top: 1px solid #f1f5f9;
  padding: 20px 0 0 0;
  background: transparent;
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  position: relative;
  margin-bottom: 20px;
}

/* First row: 3 columns with equal width */
.filter-grid .filter-group:nth-child(-n+3) {
  grid-column: auto;
}

/* Second row: 2 date inputs on left */
.filter-grid .filter-group:nth-child(4) {
  grid-column: 1;
}

.filter-grid .filter-group:nth-child(5) {
  grid-column: 2;
}

/* Second row: button group on same row as date inputs */
.filter-grid .filter-group:nth-child(6) {
  grid-column: 3;
  grid-row: 2;
}

/* Button group styling */
.button-group {
  display: flex !important;
  flex-direction: row !important;
  gap: 10px;
  align-items: end;
  justify-content: center; /* Căn giữa button */
  width: 100%;
  margin-top: 20px; /* Thêm margin-top để đẩy button group xuống */
}

.button-group button {
  width: auto !important;
  padding: 8px 16px;
  white-space: nowrap;
  flex-shrink: 0;
  display: inline-flex !important;
  float: none !important;
  margin-bottom: 5px !important;
}

/* Button styling within filter-group */
.filter-group button {
  margin-top: 0;
  height: 40px;
  align-self: end;
}

/* Specific styling for buttons in column 3 - restore original size */
.filter-grid .filter-group:nth-child(6) button,
.filter-grid .filter-group:nth-child(7) button {
  width: auto;
  padding: 8px 16px;
}

/* Make buttons inline in the same row */
.filter-grid .filter-group:nth-child(6),
.filter-grid .filter-group:nth-child(7) {
  display: inline-block;
  width: auto;
}

.filter-group label {
  display: block;
  margin-bottom: 6px;
  font-weight: 500;
  color: #374151;
  font-size: 14px;
}

/* Hide label for button groups */
.filter-group:nth-child(6) label,
.filter-group:nth-child(7) label {
  display: none;
}

.filter-grid::after {
  content: '';
  position: absolute;
  bottom: -12px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 3px;
  background: linear-gradient(90deg, #f97316, #ea580c);
  border-radius: 2px;
  opacity: 0.6;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  position: relative;
  transition: all 0.3s ease;
}

.filter-group:hover {
  transform: translateY(-2px);
}

.filter-group:hover label {
  color: #f97316;
}

.filter-group label {
  font-weight: 600;
  font-size: 14px;
  color: #374151;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.filter-group label::before {
  content: '';
  width: 3px;
  height: 16px;
  background: linear-gradient(135deg, #f97316, #ea580c);
  border-radius: 2px;
}

.filter-select,
.filter-input {
  width: 100%;
  height: 48px;
  padding: 14px 18px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  background: #fafbfc;
  font-size: 14px;
  font-family: inherit;
  line-height: 1.5;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: #1f2937;
  font-weight: 500;
  position: relative;
  box-sizing: border-box;
  vertical-align: middle;
}

.filter-select:focus,
.filter-input:focus {
  outline: none;
  border-color: #f97316;
  background: white;
  box-shadow: 0 0 0 4px rgba(249, 115, 22, 0.1), 0 4px 12px rgba(0, 0, 0, 0.05);
  transform: translateY(-1px);
}

.filter-select {
  cursor: pointer;
  appearance: none;
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 20 20'%3e%3cpath stroke='%236b7280' stroke-linecap='round' stroke-linejoin='round' stroke-width='1.5' d='m6 8 4 4 4-4'/%3e%3c/svg%3e");
  background-position: right 12px center;
  background-repeat: no-repeat;
  background-size: 16px;
  padding-right: 40px;
}

/* Đặc biệt cho input type="date" để đảm bảo kiểu bo góc giống nhau */
input[type="date"].filter-input {
  -webkit-appearance: none;
  -moz-appearance: none;
  appearance: none;
  /* Giữ nguyên tất cả thuộc tính từ .filter-input */
  width: 100%;
  height: 48px;
  padding: 14px 18px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  background: #fafbfc;
  font-size: 14px;
  font-family: inherit;
  line-height: 1.5;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: #1f2937;
  font-weight: 500;
  position: relative;
  box-sizing: border-box;
  vertical-align: middle;
}

input[type="date"].filter-input:focus {
  outline: none;
  border-color: #f97316;
  background: white;
  box-shadow: 0 0 0 4px rgba(249, 115, 22, 0.1), 0 4px 12px rgba(0, 0, 0, 0.05);
  transform: translateY(-1px);
}

input[type="date"].filter-input::-webkit-calendar-picker-indicator {
  background: transparent;
  bottom: 0;
  color: transparent;
  cursor: pointer;
  height: auto;
  left: 0;
  position: absolute;
  right: 0;
  top: 0;
  width: auto;
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 20 20'%3e%3cpath stroke='%236b7280' stroke-linecap='round' stroke-linejoin='round' stroke-width='1.5' d='M6 2a1 1 0 0 0-1 1v1H4a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V6a2 2 0 0 0-2-2h-1V3a1 1 0 1 0-2 0v1H7V3a1 1 0 0 0-1-1zM4 7h12v9a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7z'/%3e%3c/svg%3e");
  background-position: right 12px center;
  background-repeat: no-repeat;
  background-size: 16px;
  opacity: 1;
  padding-right: 40px;
}

/* Đảm bảo tất cả input có cùng text alignment và placeholder */
.filter-select,
.filter-input,
input[type="date"].filter-input {
  text-align: left;
}

.filter-select::placeholder,
.filter-input::placeholder,
input[type="date"].filter-input::placeholder {
  color: #9ca3af;
  font-weight: 400;
  opacity: 1;
}

.filter-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.btn-search,
.btn-reset {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.btn-search {
  background: #f97316;
  color: white;
}

.btn-search:hover {
  background: #ea580c;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(249, 115, 22, 0.3);
}

.btn-reset {
  background: #f8fafc;
  color: #64748b;
  border: 1px solid #e2e8f0;
}

.btn-reset:hover {
  background: #f1f5f9;
  color: #374151;
  border-color: #cbd5e1;
  transform: translateY(-1px);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon {
  font-size: 20px;
}

.main-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary, #333);
}

.btn-primary {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #ff6b35 !important;
  color: white !important;
  padding: 12px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn-primary:hover {
  background: #e55a2b !important;
  transform: translateY(-1px);
}


/* Filter Card */
.filter-card {
  background: #f8f9fa;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.filter-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 20px;
  border-bottom: 1px solid #e0e0e0;
  background: white;
  border-radius: 8px 8px 0 0;
}

.filter-icon {
  font-size: 16px;
}

.filter-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.filter-content {
  padding: 20px;
}

.filter-row {
  display: flex;
  gap: 20px;
  margin-bottom: 16px;
}

.filter-row:last-child {
  margin-bottom: 0;
}

.filter-group {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.filter-group label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.search-input, .filter-select {
  padding: 8px 45px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  background: white;
}

.filter-actions {
  display: flex;
  gap: 8px;
  align-items: end;
}

.btn-search, .btn-reset {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn-search {
  background: #ff6b35;
  color: white;
}

.btn-search:hover {
  background: #e55a2b;
}

.btn-reset {
  background: #6c757d;
  color: white;
}

.btn-reset:hover {
  background: #545b62;
}

/* Table */
.table-container {
  overflow-x: auto;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  background: white;
}

.table-header {
  padding: 16px 20px 5px 20px;
}

.table-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

table {
  width: 100%;
  border-collapse: collapse;
  min-width: 1200px;
}


/* Điều chỉnh độ rộng cột STT (cột 1) */
table th:nth-child(1),
table td:nth-child(1) {
  width: 60px;
  min-width: 60px;
  max-width: 60px;
  text-align: center;
}

/* Điều chỉnh độ rộng cột Mã HD (cột 2) */
table th:nth-child(2),
table td:nth-child(2) {
  width: 140px;
  min-width: 140px;
  max-width: 140px;
  text-align: center;
}

/* Điều chỉnh độ rộng cột Khách hàng (cột 3) - mở rộng hơn */
table th:nth-child(3),
table td:nth-child(3) {
  width: 180px;
  min-width: 180px;
}

/* Điều chỉnh độ rộng cột SĐT (cột 4) */
table th:nth-child(4),
table td:nth-child(4) {
  width: 120px;
  min-width: 120px;
  max-width: 120px;
}

/* Điều chỉnh độ rộng cột Loại đơn (cột 5) */
table th:nth-child(5),
table td:nth-child(5) {
  width: 100px;
  min-width: 100px;
  max-width: 100px;
  text-align: center;
}

/* Điều chỉnh độ rộng cột Tổng tiền (cột 6) */
table th:nth-child(6),
table td:nth-child(6) {
  width: 130px;
  min-width: 130px;
  text-align: right;
}

/* Điều chỉnh độ rộng cột Trạng thái (cột 7) */
table th:nth-child(7),
table td:nth-child(7) {
  width: 120px;
  min-width: 120px;
  text-align: center;
}

/* Điều chỉnh độ rộng cột Ngày tạo (cột 8) */
table th:nth-child(8),
table td:nth-child(8) {
  width: 110px;
  min-width: 110px;
  text-align: center;
}

/* Điều chỉnh độ rộng cột Thao tác (cột 9) */
table th:nth-child(9),
table td:nth-child(9) {
  width: 80px;
  min-width: 80px;
  text-align: center;
}

th, td {
  border: 1px solid #e0e0e0;
  padding: 12px;
  text-align: left;
}

th {
  background: #f8f9fa;
  font-weight: 600;
  color: #333;
}

.text-center {
  text-align: center;
  color: #6c757d;
  font-style: italic;
}

.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: #007bff;
}

/* Customer Info */
.customer-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  background: none !important;
  border: none !important;
  padding: 0 !important;
  border-radius: 0 !important;
  box-shadow: none !important;
}

.customer-name {
  font-weight: 500;
  color: #333;
  background: none !important;
  border: none !important;
  padding: 0 !important;
  border-radius: 0 !important;
  box-shadow: none !important;
}

.phone-info {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #374151;
  font-weight: 500;
}

.phone-info svg {
  color: #6b7280;
  font-size: 14px;
}

.no-phone {
  color: #9ca3af;
  font-style: italic;
}

.text-muted {
  color: #9ca3af;
}

/* Order Type */
.order-type {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.order-type.normal {
  background: #fef3c7;
  color: #92400e;
  border: 1px solid #f59e0b;
}

.order-type.online {
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

/* Status Badge */
.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.badge-pending {
  background: #fff3cd;
  color: #856404;
  border: 1px solid #ffeaa7;
}

.badge-paid {
  background: #d1ecf1;
  color: #0c5460;
  border: 1px solid #bee5eb;
}

.badge-shipping {
  background: #cce5ff;
  color: #004085;
  border: 1px solid #99d3ff;
}

.badge-delivering {
  background: #ffe6cc;
  color: #cc6600;
  border: 1px solid #ffcc99;
}

.badge-completed {
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.badge-cancelled {
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.badge-unknown {
  background: #e2e3e5;
  color: #383d41;
  border: 1px solid #d6d8db;
}

/* Status Classes */
.status-0 { color: #ffc107; font-weight: 600; }
.status-1 { color: #17a2b8; font-weight: 600; }
.status-2 { color: #007bff; font-weight: 600; }
.status-3 { color: #28a745; font-weight: 600; }
.status-4 { color: #6f42c1; font-weight: 600; }
.status-5 { color: #dc3545; font-weight: 600; }
.status-unknown { color: #6c757d; font-weight: 600; }

/* Action Buttons */
.action-buttons {
  display: flex;
  gap: 4px;
}

.btn-view {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  font-size: 14px;
  background: #e0f2fe;
  color: #0277bd;
}

.btn-edit, .btn-update, .btn-status, .btn-delete {
  padding: 4px 8px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
}

.btn-edit, .btn-update {
  background: #28a745;
  color: white;
}

.btn-status {
  background: #ffc107;
  color: #333;
}

.btn-delete {
  background: #dc3545;
  color: white;
}

.btn-view:hover {
  background: #b3e5fc;
  transform: translateY(-1px);
}

.btn-edit:hover, .btn-update:hover, .btn-status:hover, .btn-delete:hover {
  opacity: 0.8;
  transform: translateY(-1px);
}

/* Pagination */
.pagination-section {
  margin-top: 20px;
  padding: 16px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.pagination-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 16px;
}

.page-size-selector {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-size-selector label {
  font-weight: 500;
  color: #333;
}

.page-size-select {
  padding: 6px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
  font-size: 14px;
  cursor: pointer;
}

.page-size-select:focus {
  outline: none;
  border-color: #ff6b35;
  box-shadow: 0 0 0 2px rgba(255, 107, 53, 0.1);
}

.pagination-info {
  color: #666;
  font-size: 14px;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
}

.page-numbers {
  display: flex;
  align-items: center;
  gap: 4px;
}

.page-number {
  min-width: 40px;
  height: 40px;
  padding: 0 8px;
  border: 1px solid #e0e0e0;
  background: white;
  color: #666;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.page-number:hover {
  background: #f5f5f5;
  border-color: #ff6b35;
  color: #ff6b35;
}

.page-number.active {
  background: #ff6b35;
  border-color: #ff6b35;
  color: white;
}

.page-number.active:hover {
  background: #e55a2b;
  border-color: #e55a2b;
}

.page-ellipsis {
  padding: 0 8px;
  color: #999;
  font-weight: 500;
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: white;
  color: #333;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-btn:hover:not(:disabled) {
  background: #f8f9fa;
  border-color: #007bff;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-weight: 500;
  color: #333;
}

/* Modal */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 500px;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
}

.modal-content.large {
  width: 800px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.modal-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.btn-close {
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
  border-radius: 50%;
  transition: all 0.3s ease;
}

.btn-close:hover {
  background: #f0f0f0;
  color: #333;
}

/* Form */
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  padding: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  background: white;
}

.form-group textarea {
  resize: vertical;
  min-height: 80px;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding: 20px;
  border-top: 1px solid #e0e0e0;
}

.btn-secondary {
  background: #6c757d;
  color: white;
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn-secondary:hover {
  background: #545b62;
}

/* Details Modal */
.details-content {
  padding: 20px;
}

.details-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 20px;
}

.detail-section {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 6px;
  border: 1px solid #e0e0e0;
}

.detail-section h3 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  border-bottom: 2px solid #e0e0e0;
  padding-bottom: 8px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #e8e8e8;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-item label {
  font-weight: 500;
  color: #666;
  min-width: 120px;
}

.detail-item span {
  color: #333;
  font-weight: 500;
}

.amount {
  color: #28a745;
  font-weight: 600;
}

/* Status Modal */
.status-content {
  padding: 20px;
}

.current-status {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
  margin-bottom: 20px;
}

.current-status label {
  font-weight: 500;
  color: #333;
}

.status-options h4 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.status-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.status-btn {
  padding: 12px;
  border: 2px solid #e0e0e0;
  background: white;
  color: #333;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  text-align: center;
}

.status-btn:hover {
  background: #f8f9fa;
  transform: translateY(-1px);
}

.status-btn.active {
  background: #e3f2fd;
  border-color: #007bff;
  color: #007bff;
}

/* Tracking Styles */
.tracking-container {
  max-width: 1200px;
  margin: 0 auto;
}

.tracking-search {
  margin-bottom: 30px;
}

.search-box {
  display: flex;
  gap: 12px;
  max-width: 500px;
  margin: 0 auto;
}

.search-box .input-group {
  position: relative;
  flex: 1;
}

.tracking-input {
  width: 100%;
  padding: 12px 16px 12px 40px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 16px;
  transition: border-color 0.3s ease;
}

.tracking-input:focus {
  outline: none;
  border-color: #667eea;
}

.tracking-buttons {
  display: flex;
  gap: 8px;
}

.btn-track-search, .btn-qr-scanner {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px 16px;
  background: linear-gradient(135deg, #ff6b35 0%, #fd7e14 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s ease;
  min-width: 48px;
  height: 48px;
}

.btn-track-search:hover, .btn-qr-scanner:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.4);
}

.btn-qr-scanner {
  background: linear-gradient(135deg, #ff6b35 0%, #fd7e14 100%);
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 16px;
  min-width: auto;
  width: auto;
}

.btn-qr-scanner:hover {
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.4);
}

.qr-icon {
  width: 18px;
  height: 18px;
  color: white;
  font-size: 18px;
}

.qr-text {
  font-size: 12px;
  font-weight: 600;
  color: white;
  white-space: nowrap;
}

.btn-refresh {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px 16px;
  min-width: 48px;
  height: 48px;
  border-radius: 8px;
}

.btn-refresh:hover {
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.4);
}

/* Timeline Container */
.timeline-container {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin: 20px 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
}

.timeline-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  font-size: 18px;
  font-weight: 600;
  color: #2d3748;
}

.timeline-header svg {
  color: #ff6b35;
}

/* Progress Bar */
.progress-container {
  margin: 0;
}

.progress-bar {
  display: flex;
  justify-content: space-between;
  position: relative;
  padding: 20px 0;
}

.progress-bar::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 2px;
  background: #e2e8f0;
  z-index: 1;
}

/* Đường kết nối cho tất cả các bước - màu xám mặc định */
.progress-step:not(:last-child)::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  right: -50%;
  height: 2px;
  background: #e2e8f0;
  z-index: 1;
  transform: translateY(-50%);
}

/* Đường xanh cho các bước đã completed */
.progress-step.completed:not(:last-child)::after {
  background: #28a745;
}

.progress-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  z-index: 2;
  flex: 1;
}

.step-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e2e8f0;
  color: #718096;
  font-size: 18px;
  margin-bottom: 8px;
  transition: all 0.3s ease;
}

.progress-step.active .step-icon {
  background: #ff6b35;
  color: white;
}

.progress-step.completed .step-icon {
  background: #48bb78;
  color: white;
}

.step-content {
  text-align: center;
}

.step-title {
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 4px;
}

.step-time {
  font-size: 12px;
  color: #718096;
}

/* Order Info Grid */
.order-info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 30px;
}

.info-card {
  background: #f7fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 20px;
}

.info-card h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
  border-bottom: 2px solid #e2e8f0;
  padding-bottom: 8px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #e2e8f0;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item label {
  font-weight: 500;
  color: #718096;
  min-width: 120px;
}

.info-item span {
  color: #2d3748;
  font-weight: 500;
}

.order-code {
  font-weight: 700;
  color: #ff6b35;
}

.voucher-code {
  font-weight: 700;
  color: #48bb78;
}

.order-date {
  color: #718096;
}

.employee-name {
  color: #2d3748;
  font-weight: 600;
}

/* Product List */
.product-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.product-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: white;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
}

.product-image {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  overflow: hidden;
  background: #e2e8f0;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  flex: 1;
}

.product-name {
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 4px;
}

.product-specs {
  font-size: 12px;
  color: #718096;
  margin-bottom: 4px;
}

.product-specs span {
  margin-right: 8px;
  padding: 2px 6px;
  background: #f7fafc;
  border-radius: 4px;
}

.product-price {
  color: #ff6b35;
  font-weight: 600;
}

.product-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.quantity-badge {
  background: #ff6b35;
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.no-products {
  text-align: center;
  color: #718096;
  font-style: italic;
  padding: 20px;
}

/* Order Summary */
.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #e2e8f0;
}

.summary-item:last-child {
  border-bottom: none;
}

.summary-item.total {
  border-top: 2px solid #e2e8f0;
  margin-top: 8px;
  padding-top: 16px;
}

.summary-item label {
  font-weight: 500;
  color: #718096;
}

.amount, .discount, .total-amount {
  font-weight: 600;
  text-align: right;
}

.amount {
  color: #2d3748;
}

.discount {
  color: #e53e3e;
}

.total-amount {
  color: #48bb78;
  font-size: 18px;
}

.amount-note, .discount-note, .total-note {
  font-size: 12px;
  color: #718096;
  text-align: right;
  margin-top: 2px;
}

/* Tracking Actions */
.tracking-actions {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  margin-top: 30px;
}

.action-group {
  display: flex;
  gap: 12px;
}

.action-group .btn-primary {
  background: #ff6b35 !important;
  color: white !important;
}

.action-group .btn-primary:hover {
  background: #e55a2b !important;
}

/* Clickable Progress Steps */
.progress-step.clickable {
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.progress-step.clickable:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.2);
}

.progress-step.clickable:hover .step-icon {
  background: #ff6b35;
  color: white;
  transform: scale(1.1);
}

.progress-step.clickable:hover .step-title {
  color: #ff6b35;
  font-weight: 600;
}

/* Non-clickable completed steps (BAN_THUONG orders) */
.progress-step:not(.clickable) {
  cursor: default;
  opacity: 0.8;
}

.progress-step:not(.clickable) .step-icon {
  background: #28a745 !important;
  color: white !important;
}

.progress-step:not(.clickable) .step-title {
  color: #28a745 !important;
  font-weight: 600;
}

.step-hint {
  font-size: 11px;
  color: #ff6b35;
  font-weight: 500;
  margin-top: 4px;
  opacity: 0.8;
}

.step-loading {
  font-size: 11px;
  color: #ff6b35;
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 4px;
}


/* No Result */
.no-result {
  text-align: center;
  padding: 60px 20px;
  color: #718096;
}

.no-result-icon {
  font-size: 48px;
  margin-bottom: 16px;
  color: #e53e3e;
}

.no-result h3 {
  margin: 0 0 8px 0;
  color: #2d3748;
}

.no-result p {
  margin: 0;
  font-size: 14px;
}

/* Responsive */
@media (max-width: 768px) {
  .filter-row {
    flex-direction: column;
    gap: 12px;
  }

  .filter-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .filter-grid .filter-group:nth-child(4),
  .filter-grid .filter-group:nth-child(5),
  .filter-grid .filter-group:nth-child(6),
  .filter-grid .filter-group:nth-child(7) {
    grid-column: 1;
    grid-row: auto;
  }

  .details-grid {
    grid-template-columns: 1fr;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .status-grid {
    grid-template-columns: 1fr;
  }

  .order-info-grid {
    grid-template-columns: 1fr;
  }

  .tracking-actions {
    flex-direction: column;
  }

  .action-group {
    justify-content: center;
  }

  .modal-content {
    width: 95%;
    margin: 10px;
  }

  .modal-content.large {
    width: 95%;
  }
}

/* Payment Method Modal Styles */
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
  padding: 20px;
}

.modal-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
  max-width: 500px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 24px 0 24px;
  border-bottom: 1px solid #e5e7eb;
  margin-bottom: 24px;
}

.modal-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.close-btn {
  background: none;
  border: none;
  font-size: 18px;
  color: #6b7280;
  cursor: pointer;
  padding: 8px;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.close-btn:hover {
  background: #f3f4f6;
  color: #374151;
}

.modal-body {
  padding: 0 24px 24px 24px;
}

.modal-description {
  margin: 0 0 24px 0;
  color: #6b7280;
  line-height: 1.5;
}

.payment-options {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.payment-option {
  display: flex;
  align-items: center;
  padding: 20px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: #fafafa;
}

.payment-option:hover {
  border-color: #3b82f6;
  background: #f0f9ff;
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(59, 130, 246, 0.15);
}

.payment-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
  margin-right: 16px;
  flex-shrink: 0;
}

.payment-icon.cod {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.payment-icon.prepaid {
  background: linear-gradient(135deg, #10b981, #059669);
}

.payment-info {
  flex: 1;
}

.payment-info h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.payment-info p {
  margin: 0 0 8px 0;
  color: #6b7280;
  font-size: 14px;
  line-height: 1.4;
}

.next-status {
  font-size: 12px;
  color: #3b82f6;
  font-weight: 500;
  background: #eff6ff;
  padding: 4px 8px;
  border-radius: 6px;
  display: inline-block;
}

@media (max-width: 640px) {
  .modal-overlay {
    padding: 10px;
  }

  .modal-header {
    padding: 20px 20px 0 20px;
  }

  .modal-body {
    padding: 0 20px 20px 20px;
  }

  .payment-option {
    padding: 16px;
  }

  .payment-icon {
    width: 40px;
    height: 40px;
    font-size: 18px;
    margin-right: 12px;
  }

  .payment-info h4 {
    font-size: 15px;
  }

  .payment-info p {
    font-size: 13px;
  }
}

/* Confirm Modal Styles */
.confirm-modal {
  max-width: 400px;
}

.confirm-icon {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.confirm-icon .fa-exclamation-triangle {
  font-size: 48px;
  color: #f59e0b;
}

.confirm-message {
  text-align: center;
  margin-bottom: 24px;
  color: #374151;
  line-height: 1.5;
  font-size: 16px;
}

.confirm-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.btn {
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: 500;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 100px;
  justify-content: center;
}

.btn-cancel {
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
}

.btn-cancel:hover {
  background: #e5e7eb;
}

.btn-confirm {
  background: #dc2626;
  color: white;
}

.btn-confirm:hover:not(:disabled) {
  background: #b91c1c;
}

.btn-confirm:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

@media (max-width: 640px) {
  .confirm-actions {
    flex-direction: column;
  }

  .btn {
    width: 100%;
  }
}

/* IMEI Modal uses Tailwind CSS classes - no custom CSS needed */

/* Print Template Styles */
.print-template {
  display: none;
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: white;
  z-index: 9999;
  overflow: auto;
}


/* Ẩn tất cả nội dung khác khi in */
body {
  background: white !important;
  margin: 0 !important;
  padding: 0 !important;
}

/* Ẩn tất cả nội dung ngoại trừ print template */
body * {
  visibility: hidden !important;
}

/* Chỉ hiển thị print template */
.print-template,
.print-template * {
  visibility: visible !important;
}

.print-template {
  position: absolute !important;
  left: 0 !important;
  top: 0 !important;
  width: 100% !important;
  height: 100% !important;
  background: white !important;
  margin: 0 !important;
  padding: 0 !important;
}

/* Invoice Container */
.invoice-container {
  width: 210mm;
  min-height: 297mm;
  background: #fff;
  padding: 20mm;
  margin: auto;
  color: #333;
  font-family: "Segoe UI", sans-serif;
  border: 1px solid #ccc;
}

.logo {
  width: 80px;
  height: auto;
  margin-bottom: 10px;
}

.invoice-header {
  display: flex;
  justify-content: space-between;
  border-bottom: 2px solid #007bff;
  padding-bottom: 10px;
  margin-bottom: 15px;
}

.invoice-title {
  text-align: right;
}

.invoice-title h1 {
  font-size: 24px;
  font-weight: bold;
  margin: 0 0 10px 0;
  color: #007bff;
}

.invoice-title p {
  margin: 5px 0;
  font-size: 14px;
  color: #333;
}

.qr-section {
  margin-top: 10px;
  text-align: center;
}

.qr-section p {
  font-size: 10px;
  margin: 5px 0 0 0;
  color: #666;
}

.info-section {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
}

.customer-info, .summary-info {
  width: 48%;
  border: 1px solid #ccc;
  padding: 10px;
  border-radius: 8px;
}

.customer-info h3, .summary-info h3 {
  font-size: 16px;
  font-weight: bold;
  margin: 0 0 10px 0;
  color: #333;
}

.customer-info p, .summary-info p {
  margin: 5px 0;
  font-size: 14px;
  color: #333;
}

.product-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 10px;
}

.product-table th, .product-table td {
  border: 1px solid #ccc;
  padding: 8px;
  text-align: center;
}

.product-table th {
  background: #007bff;
  color: white;
  font-weight: bold;
}

.product-table td {
  background: white;
  color: #333;
}

.imei-section {
  margin-top: 15px;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 8px;
}

.imei-section h3 {
  font-size: 16px;
  font-weight: bold;
  margin: 0 0 10px 0;
  color: #333;
}

.imei-item {
  margin: 5px 0;
}

.imei-item p {
  margin: 3px 0;
  font-size: 14px;
  color: #333;
}

.total-section {
  text-align: right;
  margin-top: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.total-section p {
  margin: 5px 0;
  font-size: 16px;
  color: #333;
}

.total-section .final {
  font-size: 18px;
  color: green;
  font-weight: bold;
}

.notes-section {
  margin-top: 15px;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 8px;
}

.notes-section h3 {
  font-size: 16px;
  font-weight: bold;
  margin: 0 0 10px 0;
  color: #333;
}

.notes-section p {
  margin: 0;
  font-size: 14px;
  color: #333;
}

.footer {
  display: flex;
  justify-content: space-between;
  margin-top: 30px;
  text-align: center;
}

.sign {
  width: 45%;
}

.sign p {
  margin: 5px 0;
  font-size: 14px;
  color: #333;
}

/* Đảm bảo tất cả element trong hóa đơn có nền trắng */
.invoice-print * {
  background: white !important;
  box-shadow: none !important;
  border: none !important;
}

/* Chỉ giữ border cho bảng */
.invoice-print .products-table,
.invoice-print .products-table th,
.invoice-print .products-table td {
  border: 1px solid #333 !important;
}

/* Giữ màu nền cho header và total */
.invoice-print .products-table th {
  background-color: #f97316 !important;
  color: white !important;
}

.invoice-print .total-row {
  background-color: #f97316 !important;
  color: white !important;
}

.invoice-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 3px solid #f97316;
}

.store-info h1 {
  font-size: 24px;
  font-weight: bold;
  color: #f97316;
  margin: 0 0 10px 0;
}

.store-info p {
  margin: 5px 0;
  font-size: 12px;
  color: #666;
}

.qr-code-container {
  text-align: center;
}

.qr-code-container p {
  font-size: 10px;
  margin: 5px 0 0 0;
  font-weight: bold;
}

.invoice-info h2 {
  font-size: 20px;
  font-weight: bold;
  text-align: center;
  margin: 20px 0;
  color: #f97316;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin: 15px 0;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px dotted #ccc;
}

.info-item .label {
  font-weight: bold;
  color: #333;
}

.info-item .value {
  color: #000;
}

.customer-info,
.products-section,
.total-section,
.notes-section {
  margin: 20px 0;
}

.customer-info h3,
.products-section h3 {
  font-size: 16px;
  font-weight: bold;
  color: #f97316;
  margin-bottom: 15px;
  border-bottom: 2px solid #f97316;
  padding-bottom: 5px;
}

.products-table {
  width: 100%;
  border-collapse: collapse;
  margin: 15px 0;
}

.products-table th,
.products-table td {
  border: 1px solid #333;
  padding: 8px;
  text-align: left;
  font-size: 11px;
}

.products-table th {
  background-color: #f97316;
  color: white;
  font-weight: bold;
  text-align: center;
}

.products-table tr:nth-child(even) {
  background-color: #f9f9f9;
}

.imei-info {
  margin: 10px 0;
  padding: 8px;
  background-color: #f0f0f0;
  font-size: 10px;
  border-left: 3px solid #f97316;
}

.imei-info p {
  margin: 0;
}

.total-section {
  margin-top: 30px;
}

.total-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background-color: #f97316;
  color: white;
  font-weight: bold;
  font-size: 16px;
}

.total-label {
  font-size: 18px;
}

.total-amount {
  font-size: 20px;
}

.notes-section {
  margin: 20px 0;
  padding: 10px;
  background-color: #f9f9f9;
  border-left: 3px solid #f97316;
}

.notes-section p {
  margin: 0;
  font-size: 12px;
}

.invoice-footer {
  text-align: center;
  margin-top: 40px;
  padding-top: 20px;
  border-top: 2px solid #f97316;
  color: #666;
}

.invoice-footer p {
  margin: 5px 0;
  font-size: 12px;
}

/* Page break */
.page-break {
  page-break-before: always;
}

/* Update Status Modal Styles */
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
  border-radius: 12px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
}

.modal-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.modal-close {
  background: none;
  border: none;
  font-size: 20px;
  color: #6b7280;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s;
}

.modal-close:hover {
  background: #f3f4f6;
  color: #374151;
}

.modal-body {
  padding: 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 8px;
}

.form-value {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
  padding: 8px 12px;
  background: #f9fafb;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}

.form-select {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  background: white;
  transition: border-color 0.2s;
}

.form-select:focus {
  outline: none;
  border-color: #f97316;
  box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.1);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid #e5e7eb;
  background: #f9fafb;
  border-radius: 0 0 12px 12px;
}

.btn-cancel {
  padding: 10px 20px;
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  color: #374151;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel:hover {
  background: #f9fafb;
  border-color: #9ca3af;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
