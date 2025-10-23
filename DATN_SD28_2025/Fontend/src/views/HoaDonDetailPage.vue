<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref, computed, watch, nextTick } from 'vue'
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

// Reactive data
const loading = ref(false)
const trackingCode = ref('')
const trackingData = ref<any>(null)
const trackingSearched = ref(false)
const toastRef = ref(null)

// QR Scanner state
const isQRScannerActive = ref(false)
const qrScanner = ref(null)
const currentQRScanner = ref(null)
const showQRScannerModal = ref(false)

// Update Status Modal
const showUpdateStatusModal = ref(false)
const newStatus = ref<number>(0)
const isUpdatingStatus = ref(false)

// IMEI Confirmation Modal
const showImeiConfirmationModal = ref(false)
const imeiInput = ref('')
const isScanningQR = ref(false)
// Tracking steps - 4 bước cố định cho BAN_ONLINE
const trackingSteps = ref([
  { title: 'Chờ xác nhận', icon: 'clock', time: '', status: 0, isBranch: true },
  { title: 'Chờ giao hàng', icon: 'box', time: '', status: 1, isBranch: false },
  { title: 'Đang giao hàng', icon: 'truck', time: '', status: 2, isBranch: false },
  { title: 'Hoàn thành', icon: 'check-circle', time: '', status: 3, isBranch: false }
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

// Watcher để cập nhật thời gian cho các step đã hoàn thành khi tải dữ liệu
watch(trackingData, (newData) => {
  if (newData && newData.loaiHoaDon !== 'BAN_THUONG' && newData.loaiHoaDon !== 'NORMAL') {
    // Chỉ cập nhật cho BAN_ONLINE
    console.log('🔍 Updating step times from database:', newData.lichSuTrangThai)
    
    // Reset tất cả step times trước khi cập nhật từ database
    trackingSteps.value.forEach(step => {
      // Reset step "Chờ xác nhận" (status 0) khi đơn hàng về trạng thái 0
      if (step.status === 0 && newData.trangThai === 0) {
        step.time = ''
        console.log('🔄 Reset step "Chờ xác nhận" - chưa activated')
      }
      // Giữ nguyên step "Chờ giao hàng" nếu đã có thời gian
      else if (!(step.status === 1 && step.time)) {
        step.time = ''
      }
    })
    
    // Cập nhật thời gian từ lichSuTrangThai (database)
    if (newData.lichSuTrangThai && newData.lichSuTrangThai.length > 0) {
      newData.lichSuTrangThai.forEach(trangThaiItem => {
        const status = trangThaiItem.trangThai
        const thoiGian = trangThaiItem.thoiGian
        
        if (thoiGian) {
          const thoiGianDate = new Date(thoiGian)
          const timeString = thoiGianDate.toLocaleTimeString('vi-VN') + ' ' + thoiGianDate.toLocaleDateString('vi-VN')
          
          // Map status to step index
          let stepIndex = -1
          switch (status) {
            case 0: stepIndex = 0; break  // Chờ xác nhận
            case 1: stepIndex = 1; break  // Chờ giao hàng
            case 2: stepIndex = 2; break  // Đang giao hàng
            case 3: stepIndex = 3; break  // Hoàn thành
          }
          
          if (stepIndex !== -1 && stepIndex < trackingSteps.value.length) {
            // Đặc biệt cho step "Chờ xác nhận" (status = 0): không tự động set thời gian từ database
            // Chỉ set thời gian khi user click nút "Xác nhận"
            if (status === 0) {
              console.log('⚠️ Skipping auto-set time for step "Chờ xác nhận" - waiting for manual confirmation')
            }
            // Đặc biệt cho step "Chờ giao hàng" (status = 1): chỉ set thời gian nếu đã có thời gian trước đó
            // (tức là đã được click trước đó)
            else if (status === 1 && !trackingSteps.value[stepIndex].time) {
              console.log('⚠️ Skipping auto-set time for step "Chờ giao hàng" - waiting for manual click')
            } else if (status === 1 && trackingSteps.value[stepIndex].time) {
              // Nếu step "Chờ giao hàng" đã có thời gian, giữ nguyên thời gian đã set
              console.log('✅ Keeping existing time for step "Chờ giao hàng":', trackingSteps.value[stepIndex].time)
            } else {
              trackingSteps.value[stepIndex].time = timeString
              console.log('✅ Set time from database for step', stepIndex, '(', trackingSteps.value[stepIndex].title, '):', timeString)
            }
          }
        }
      })
    } else {
      // Fallback: nếu không có lichSuTrangThai, chỉ set thời gian cho step đầu tiên nếu đơn hàng không ở trạng thái 0
      if (newData.ngayTao && newData.trangThai !== 0) {
        const ngayTao = new Date(newData.ngayTao)
        const timeString = ngayTao.toLocaleTimeString('vi-VN') + ' ' + ngayTao.toLocaleDateString('vi-VN')
        trackingSteps.value[0].time = timeString
        console.log('✅ Fallback: Set time from ngayTao for step 0:', timeString)
      } else if (newData.trangThai === 0) {
        console.log('⚠️ Fallback: Skipping auto-set time for step 0 when order status is 0 - waiting for manual confirmation')
      }
    }
  }
}, { immediate: true })

// Computed property để expand sản phẩm theo số lượng IMEI
const expandedProductList = computed(() => {
  console.log('🔍 expandedProductList - trackingData:', trackingData.value)
  console.log('🔍 expandedProductList - danhSachSanPham:', trackingData.value?.danhSachSanPham)

  if (!trackingData.value?.danhSachSanPham) {
    console.log('❌ No danhSachSanPham found')
    return []
  }

  const expandedProducts = []
  let stt = 1

  trackingData.value.danhSachSanPham.forEach((product, index) => {
    console.log(`🔍 Processing product ${index}:`, product)

    // Kiểm tra cấu trúc dữ liệu sản phẩm
    const imeiCount = product.imeis ? product.imeis.length : (product.imei ? 1 : 0)
    console.log(`🔍 Product ${index} imeiCount:`, imeiCount)

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
        imei: product.imeis ? product.imeis[0] : product.imei || null,
        imeiIndex: 1,
        originalProduct: product
      })
    }
  })

  console.log('✅ expandedProductList result:', expandedProducts)
  return expandedProducts
})

// Status options
const statusOptions = ref([
  { value: 0, label: 'Chờ xác nhận' },
  { value: 1, label: 'Chờ giao hàng' },
  { value: 2, label: 'Đang giao hàng' },
  { value: 3, label: 'Hoàn thành' }
])

// Confirm modal
const showConfirmModal = ref(false)
const confirmMessage = ref('')
const confirmCallback = ref<() => Promise<void>>()
const currentStep = ref<any>(null)

// IMEI modal
const showImeiModal = ref(false)
const selectedProductForImei = ref<any>(null)

// Product QR Scanner state
const showProductQRScannerModal = ref(false)
const currentProductForQR = ref<any>(null)
const productQRScanner = ref(null)

// Payment method modal - REMOVED: No longer needed, data comes from backend

// Lifecycle
onMounted(async () => {
  // Lấy mã đơn hàng từ query params
  const orderCode = route.query.code as string
  if (orderCode) {
    trackingCode.value = orderCode
    await searchTracking()
  }
})

// Methods
async function refreshTrackingData() {
  if (!trackingData.value?.maHoaDon) {
    console.log('❌ No tracking data to refresh')
    return
  }

  try {
    console.log('🔄 Refreshing tracking data for:', trackingData.value.maHoaDon)
    const { data } = await api.get(`/api/hoa-don/tracking/${trackingData.value.maHoaDon}`)
    console.log('🔄 Refreshed API Response:', data)

    if (data) {
      trackingData.value = data
      console.log('🔄 Updated trackingData:', trackingData.value)
      console.log('🔄 lichSuTrangThai:', trackingData.value?.lichSuTrangThai)
      console.log('🔄 lichSuTrangThai length:', trackingData.value?.lichSuTrangThai?.length)

      // Debug: Kiểm tra các trường mới
      console.log('🔍 Debug - New fields from backend:')
      console.log('📧 Email:', data.email)
      console.log('💳 Phương thức thanh toán:', data.phuongThucThanhToan)
      console.log('🚚 Phí vận chuyển:', data.phiVanChuyen)
      console.log('🏙️ Tỉnh thành:', data.tinhThanh)
      console.log('🏘️ Quận huyện:', data.quanHuyen)
      console.log('📦 Phương thức giao hàng:', data.phuongThucGiaoHang)
    } else {
      console.log('❌ No data returned from refresh API')
    }
  } catch (error) {
    console.error('❌ Error refreshing tracking data:', error)
  }
}

async function searchTracking() {
  if (!trackingCode.value.trim()) {
    toastRef.value?.error('Lỗi', 'Vui lòng nhập mã đơn hàng')
    return
  }

  try {
    loading.value = true
    console.log('🔍 Searching tracking for code:', trackingCode.value)
    const { data } = await api.get(`/api/hoa-don/tracking/${trackingCode.value}`)
    console.log('🔍 API Response:', data)

    if (data) {
      trackingData.value = data
      trackingSearched.value = true
      console.log('🔍 Set trackingData:', trackingData.value)
      console.log('🔍 danhSachSanPham:', trackingData.value?.danhSachSanPham)
      console.log('🔍 lichSuTrangThai:', trackingData.value?.lichSuTrangThai)
      console.log('🔍 lichSuTrangThai length:', trackingData.value?.lichSuTrangThai?.length)

      // Debug: Kiểm tra các trường mới
      console.log('🔍 Debug - New fields from backend:')
      console.log('📧 Email:', data.email)
      console.log('💳 Phương thức thanh toán:', data.phuongThucThanhToan)
      console.log('🚚 Phí vận chuyển:', data.phiVanChuyen)
      console.log('🏙️ Tỉnh thành:', data.tinhThanh)
      console.log('🏘️ Quận huyện:', data.quanHuyen)
      console.log('📦 Phương thức giao hàng:', data.phuongThucGiaoHang)

      toastRef.value?.success('Thành công', 'Tìm thấy đơn hàng')
    } else {
      console.log('❌ No data returned from API')
      toastRef.value?.error('Lỗi', 'Không tìm thấy đơn hàng')
    }
  } catch (error) {
    console.error('Error searching tracking:', error)
    toastRef.value?.error('Lỗi', 'Có lỗi xảy ra khi tìm kiếm đơn hàng')
  } finally {
    loading.value = false
  }
}

function resetTrackingPage() {
  trackingCode.value = ''
  trackingData.value = null
  trackingSearched.value = false
}

// QR Scanner methods
async function startQRScanner() {
  try {
    showQRScannerModal.value = true
    await nextTick()

    const video = document.getElementById('qr-scanner-video') as HTMLVideoElement
    if (video) {
      currentQRScanner.value = new QrScanner(
        video,
        result => {
          console.log('QR Code detected:', result)
          trackingCode.value = result.data
          closeQRScanner()
          searchTracking()
        },
        {
          onDecodeError: error => {
            // Ignore decode errors
          }
        }
      )
      await currentQRScanner.value.start()
    }
  } catch (error) {
    console.error('Error starting QR scanner:', error)
    toastRef.value?.error('Lỗi', 'Không thể khởi động camera')
  }
}

function closeQRScanner() {
  if (currentQRScanner.value) {
    currentQRScanner.value.stop()
    currentQRScanner.value = null
  }
  showQRScannerModal.value = false
}

// Status methods
function getCurrentStepIndex(): number {
  if (!trackingData.value) {
    console.log('🔍 getCurrentStepIndex: No tracking data, returning 0')
    return 0
  }
  
  const status = trackingData.value.trangThai
  const loaiHoaDon = trackingData.value.loaiHoaDon
  
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
      case 1: return 1  // Chờ giao hàng
      case 2: return 2  // Đang giao hàng
      case 3: return 3  // Hoàn thành
      case 5: return 0  // Đã hủy - reset về bước đầu
      default: return 0
    }
  }
}

function getStatusName(trangThai: number): string {
  const status = statusOptions.value.find(s => s.value === trangThai)
  return status ? status.label : 'Không xác định'
}

function getStatusClass(trangThai: number): string {
  const statusClasses = {
    0: 'status-pending',
    1: 'status-waiting',
    2: 'status-shipping',
    3: 'status-completed'
  }
  return statusClasses[trangThai] || 'status-pending'
}

function getStatusColor(trangThai: number): string {
  const statusColors = {
    0: '#f59e0b',
    1: '#3b82f6',
    2: '#8b5cf6',
    3: '#06b6d4',
    4: '#10b981'
  }
  return statusColors[trangThai] || '#6b7280'
}

function getStatusIcon(trangThai: number): string {
  const statusIcons = {
    0: 'clock',
    1: 'check',
    2: 'box',
    3: 'truck',
    4: 'check-circle'
  }
  return statusIcons[trangThai] || 'question'
}

function getPaymentMethodName(method: string): string {
  if (!method) return 'Chưa chọn'

  const paymentMethods: { [key: string]: string } = {
    'cod': 'Thanh toán khi nhận hàng (COD)',
    'momo': 'Ví MoMo',
    'bank': 'Chuyển khoản ngân hàng',
    'prepaid': 'Thanh toán trước',
    'cash': 'Tiền mặt',
    'card': 'Thẻ tín dụng/ghi nợ'
  }

  return paymentMethods[method] || method
}

function getShippingMethodName(method: string): string {
  if (!method) return 'Chưa chọn'
  
  const shippingMethods: { [key: string]: string } = {
    'standard': 'Tiêu chuẩn',
    'express': 'Giao nhanh', 
    'ghn': 'Hỏa tốc',
    'fast': 'Giao nhanh',
    'urgent': 'Hỏa tốc'
  }
  
  return shippingMethods[method] || method
}

// IMEI Confirmation Functions
function openImeiConfirmationModal() {
  showImeiConfirmationModal.value = true
  imeiInput.value = ''
  isScanningQR.value = false
}

function closeImeiConfirmationModal() {
  showImeiConfirmationModal.value = false
  imeiInput.value = ''
  isScanningQR.value = false
  stopImeiQRScanner()
}

function startImeiQRScanner() {
  isScanningQR.value = true
  // TODO: Implement QR scanner logic
  console.log('Starting IMEI QR scanner...')
}

function stopImeiQRScanner() {
  isScanningQR.value = false
  // TODO: Stop QR scanner
  console.log('Stopping IMEI QR scanner...')
}

async function saveImeiToDatabase(imei: string, product?: any) {
  try {
    console.log('Saving IMEI to database:', imei)
    
    const API_BASE_URL = 'http://localhost:8080'
    // Xác định index của dòng sản phẩm trong đơn (nếu tìm được)
    let lineIndex: number | null = null
    try {
      if (trackingData.value?.danhSachSanPham && product) {
        lineIndex = trackingData.value.danhSachSanPham.indexOf(product)
      }
    } catch {}
    const response = await fetch(`${API_BASE_URL}/api/hoa-don/save-imei`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify((() => {
        const payload: any = { maHoaDon: trackingData.value.maHoaDon, imei, lineIndex }
        // Tránh gửi chiTietHoaDonId khi không chắc chắn để backend fallback chọn dòng phù hợp
        // if (product?.chiTietHoaDonId || product?.idChiTietHoaDon) payload.chiTietHoaDonId = product.chiTietHoaDonId ?? product.idChiTietHoaDon
        if (product?.ctspId || product?.id) payload.ctspId = product.ctspId ?? product.id
        if (product?.maCtsp) payload.maCtsp = product.maCtsp
        console.log('🔼 save-imei payload:', payload)
        return payload
      })())
    })
    
    // Đọc response body (kể cả khi lỗi) để hiển thị thông điệp chi tiết từ backend
    const rawText = await response.text()
    if (!response.ok) {
      console.error('save-imei error body:', rawText)
      throw new Error(`HTTP error! status: ${response.status} body: ${rawText}`)
    }
    
    const result = rawText ? JSON.parse(rawText) : {}
    console.log('IMEI saved successfully:', result)
    
    // Sau khi lưu IMEI thành công, cần đánh dấu IMEI đã bán (giống như trong PosPage.vue)
    await markImeiAsSold(imei)
    
    return result
    
  } catch (error) {
    console.error('Error saving IMEI:', error)
    throw error
  }
}

// Hàm đánh dấu IMEI đã bán (giống như trong PosPage.vue)
async function markImeiAsSold(imei: string) {
  try {
    console.log('Marking IMEI as sold:', imei)
    
    // Gọi API để đánh dấu IMEI đã bán (trangThai = 0)
    const API_BASE_URL = 'http://localhost:8080'
    const response = await fetch(`${API_BASE_URL}/api/imei/mark-sold`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        imei: imei,
        trangThai: 0 // Đánh dấu đã bán
      })
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    const result = await response.json()
    console.log('✅ IMEI marked as sold successfully:', result)
    
  } catch (error) {
    console.error('Error marking IMEI as sold:', error)
    // Không throw error để không làm gián đoạn quá trình xác nhận
    console.warn('⚠️ Failed to mark IMEI as sold, but continuing with confirmation process')
  }
}

async function confirmOrder() {
  try {
    console.log('🔍 Confirming order:', trackingData.value?.maHoaDon)
    
    // Chỉ kích hoạt bước "Chờ xác nhận" (status = 0) mà không chuyển trạng thái
    const currentTime = new Date()
    const timeString = currentTime.toLocaleTimeString('vi-VN') + ' ' + currentTime.toLocaleDateString('vi-VN')
    
    // Cập nhật thời gian cho step "Chờ xác nhận"
    trackingSteps.value[0].time = timeString
    
    // Print invoice
    try {
      await printInvoice()
      console.log('✅ Invoice printed successfully')
    } catch (printError) {
      console.warn('⚠️ Failed to print invoice:', printError)
    }
    
    // Refresh tracking data
    await refreshTrackingData()
    
    // Show success message
    toastRef.value?.success('Thành công', 'Đơn hàng đã được xác nhận và kích hoạt')
    
    console.log('✅ Order confirmation completed:')
    console.log('📋 Order ID:', trackingData.value?.maHoaDon)
    console.log('📄 Invoice printed automatically')
    console.log('🔄 Step "Chờ xác nhận" activated with time:', timeString)
    console.log('📊 Tracking data refreshed')
    
  } catch (error) {
    console.error('Error confirming order:', error)
    toastRef.value?.error('Lỗi', 'Có lỗi xảy ra khi xác nhận đơn hàng')
  }
}

async function confirmImei() {
  if (!imeiInput.value.trim()) {
    toastRef.value?.error('Lỗi', 'Vui lòng nhập IMEI hoặc quét QR code')
    return
  }
  
  try {
    console.log('Confirming IMEI:', imeiInput.value)
    
    // Call API to save IMEI to database
    await saveImeiToDatabase(imeiInput.value)
    
    // Update status to confirmed (status = 1) - Chờ giao hàng
    await updateOrderStatusDirectly(1, { title: 'Chờ giao hàng', status: 1 })
    
    // Close modal
    closeImeiConfirmationModal()
    
    // Print invoice - giống như khâu bán hàng trong PosPage
    await printInvoice()
    
    // Hiển thị thông báo thành công - giống như khâu bán hàng trong PosPage
    toastRef.value?.success('Thành công', 'Xác nhận IMEI thành công! Đơn hàng đã được xử lý giống như khâu bán hàng.')
    
    // Refresh tracking data để cập nhật trạng thái mới - giống như khâu bán hàng trong PosPage
    await refreshTrackingData()
    
    // Log thông tin xử lý giống như khâu bán hàng
    console.log('✅ IMEI confirmation completed - processed like POS sale:')
    console.log('📋 Order ID:', trackingData.value?.maHoaDon)
    console.log('📱 IMEI confirmed:', imeiInput.value)
    console.log('📄 Invoice printed automatically')
    console.log('🔄 Status updated to confirmed')
    console.log('📊 Tracking data refreshed')
    
  } catch (error) {
    console.error('Error confirming IMEI:', error)
    toastRef.value?.error('Lỗi', 'Có lỗi xảy ra khi xác nhận IMEI')
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
  // Loại trừ bước "Chờ giao hàng" (status = 2) nếu chưa có thời gian (chưa được click)
  if (step.status === trackingData.value.trangThai && step.status !== 0 && !(step.status === 2 && !step.time)) {
    toastRef.value?.warning('Cảnh báo', 'Trạng thái hiện tại đã giống với trạng thái mới')
    return
  }

  // Nếu là bước "Chờ xác nhận" (status = 0), chỉ kích hoạt mà không chuyển trạng thái
  if (step.status === 0 && currentStepIndex === 0) {
    console.log('✅ Activating step "Chờ xác nhận" without status change')
    const currentTime = new Date()
    const timeString = currentTime.toLocaleTimeString('vi-VN') + ' ' + currentTime.toLocaleDateString('vi-VN')
    trackingSteps.value[0].time = timeString
    step.time = timeString
    toastRef.value?.success('Thành công', 'Bước "Chờ xác nhận" đã được kích hoạt')
    return
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

  // Xử lý đặc biệt cho step "Chờ giao hàng" (status = 1) 
  if (step.status === 1 && (trackingData.value.trangThai === 0 || trackingData.value.trangThai === 1) && !step.time) {
    console.log('✅ Special case: Clicking "Chờ giao hàng" to complete it')
    currentStep.value = step
    confirmMessage.value = `Bạn có chắc chắn muốn hoàn thành bước "${step.title}"?`
    confirmCallback.value = async () => {
      await updateOrderStatusDirectly(step.status, step)
    }
    showConfirmModal.value = true
    return
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
  // 3. Hoặc đang ở status 0 và click vào bước 1 (Chờ giao hàng) khi bước 0 đã được kích hoạt
  // 4. Hoặc đang ở status 1 và click vào bước 1 (Chờ giao hàng)
  canClick = (stepIndex === currentStepIndex + 1) ||
    (stepIndex === currentStepIndex && step.status !== 0) ||
    (trackingData.value?.trangThai === 0 && stepIndex === 1 && step.status === 1 && !!trackingSteps.value[0].time) ||
    (trackingData.value?.trangThai === 1 && stepIndex === 1 && step.status === 1)
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

    // Cập nhật trạng thái trong trackingData
    trackingData.value.trangThai = newStatus

    // Cập nhật thời gian ngay lập tức cho step vừa hoàn thành
    const now = new Date()
    const timeString = now.toLocaleTimeString('vi-VN') + ' ' + now.toLocaleDateString('vi-VN')
    
    // Cập nhật thời gian cho step trong trackingSteps dựa trên newStatus
    let stepIndex = -1
    switch (newStatus) {
      case 0: stepIndex = 0; break  // Chờ xác nhận
      case 1: stepIndex = 1; break  // Chờ giao hàng
      case 2: stepIndex = 2; break  // Đang giao hàng
      case 3: stepIndex = 3; break  // Hoàn thành
    }
    
    if (stepIndex !== -1 && stepIndex < trackingSteps.value.length) {
      trackingSteps.value[stepIndex].time = timeString
      console.log('✅ Updated time immediately for step:', trackingSteps.value[stepIndex].title, 'Time:', timeString)
      console.log('🔍 Step object after update:', trackingSteps.value[stepIndex])
    }
    
    // Cũng cập nhật step.time để đảm bảo consistency
    step.time = timeString
    
    // Đặc biệt cho step "Chờ xác nhận" (status = 0) và "Chờ giao hàng" (status = 1): completed ngay lập tức khi click
    if (newStatus === 0) {
      console.log('✅ Step "Chờ xác nhận" completed immediately after click')
    } else if (newStatus === 1) {
      console.log('✅ Step "Chờ giao hàng" completed immediately after click')
    }
    
    console.log('✅ Status updated and time set immediately')

    console.log('✅ Cập nhật trạng thái thành công:', data)
    toastRef.value?.success('Thành công', `Đã cập nhật trạng thái đơn hàng sang "${step.title}"`)

    // Tải lại dữ liệu tracking để cập nhật thời gian từ database
    console.log('🔄 Refreshing tracking data to get updated times...')
    await refreshTrackingData()

  } catch (error) {
    console.error('❌ Lỗi khi cập nhật trạng thái:', error)
    toastRef.value?.error('Lỗi', 'Có lỗi xảy ra khi cập nhật trạng thái đơn hàng')
  } finally {
    isUpdatingStatus.value = false
  }
}

// Payment Modal methods - REMOVED: No longer needed, data comes from backend

// Modal methods
function openUpdateStatusModal() {
  showUpdateStatusModal.value = true
  newStatus.value = trackingData.value?.trangThai || 0
}

function closeUpdateStatusModal() {
  showUpdateStatusModal.value = false
  newStatus.value = 0
}

async function updateOrderStatusFromModal() {
  if (!trackingData.value) return

  isUpdatingStatus.value = true

  try {
    const { data } = await api.put(`/api/hoa-don/update-status/${trackingData.value.maHoaDon}`, {
      trangThai: newStatus.value
    })

    trackingData.value.trangThai = newStatus.value

    console.log('✅ Cập nhật trạng thái thành công:', data)
    toastRef.value?.success('Thành công', 'Đã cập nhật trạng thái đơn hàng')

    closeUpdateStatusModal()
  } catch (error) {
    console.error('❌ Lỗi khi cập nhật trạng thái:', error)
    toastRef.value?.error('Lỗi', 'Có lỗi xảy ra khi cập nhật trạng thái đơn hàng')
  } finally {
    isUpdatingStatus.value = false
  }
}

// Confirm modal methods
function confirmAction() {
  if (confirmCallback.value) {
    confirmCallback.value()
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

// IMEI Modal methods
function showImeiDetails(product: any) {
  selectedProductForImei.value = product
  showImeiModal.value = true
}

function closeImeiModal() {
  showImeiModal.value = false
  selectedProductForImei.value = null
}

// Hủy IMEI (chuyển trạng thái từ 0 về 1 và xóa khỏi danh sách)
async function cancelImei(imei: string) {
  console.log('🚀 CANCEL IMEI FUNCTION CALLED:', imei)
  
  try {
    console.log('Canceling IMEI:', imei)
    
    const API_BASE_URL = 'http://localhost:8080'
    const requestBody = {
      imei: imei,
      trangThai: 1 // Chuyển về trạng thái khả dụng (1 = khả dụng, 0 = đã bán)
    }
    
    console.log('🚀 Sending API request to:', `${API_BASE_URL}/api/imei/mark-sold`)
    console.log('🚀 Request body:', requestBody)
    
    const response = await fetch(`${API_BASE_URL}/api/imei/mark-sold`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(requestBody)
    })
    
    console.log('🚀 Response status:', response.status)
    console.log('🚀 Response ok:', response.ok)

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const result = await response.json()
    console.log('IMEI canceled successfully:', result)

    // Xóa IMEI khỏi danh sách sản phẩm ngay lập tức
    if (trackingData.value && trackingData.value.danhSachSanPham) {
      console.log('🔍 Before removing IMEI:', imei)
      console.log('🔍 Current products:', trackingData.value.danhSachSanPham)
      
      trackingData.value.danhSachSanPham.forEach((product, index) => {
        console.log(`🔍 Processing product ${index}:`, product)
        
        // Xử lý product.imeis (array)
        if (product.imeis && Array.isArray(product.imeis)) {
          console.log(`🔍 Product ${index} imeis before:`, product.imeis)
          product.imeis = product.imeis.filter(item => {
            const imeiValue = typeof item === 'object' ? item.imei : item
            console.log(`🔍 Checking imei: ${imeiValue} vs ${imei}`)
            return imeiValue !== imei
          })
          console.log(`🔍 Product ${index} imeis after:`, product.imeis)
        }
        
        // Xử lý product.imei (single IMEI)
        if (product.imei) {
          const imeiValue = typeof product.imei === 'object' ? product.imei.imei : product.imei
          console.log(`🔍 Product ${index} single imei: ${imeiValue} vs ${imei}`)
          if (imeiValue === imei) {
            console.log(`🔍 Removing single imei from product ${index}`)
            product.imei = null // Xóa IMEI khỏi sản phẩm
          }
        }
      })
      
      console.log('🔍 After removing IMEI:', trackingData.value.danhSachSanPham)
    }

    // Xóa IMEI khỏi selectedProductForImei nếu có
    if (selectedProductForImei.value && Array.isArray(selectedProductForImei.value)) {
      console.log('🔍 Processing selectedProductForImei:', selectedProductForImei.value)
      
      selectedProductForImei.value.forEach((product, index) => {
        console.log(`🔍 Processing selectedProduct ${index}:`, product)
        
        // Xử lý product.imeis (array)
        if (product.imeis && Array.isArray(product.imeis)) {
          console.log(`🔍 SelectedProduct ${index} imeis before:`, product.imeis)
          product.imeis = product.imeis.filter(item => {
            const imeiValue = typeof item === 'object' ? item.imei : item
            console.log(`🔍 Checking selectedProduct imei: ${imeiValue} vs ${imei}`)
            return imeiValue !== imei
          })
          console.log(`🔍 SelectedProduct ${index} imeis after:`, product.imeis)
        }
        
        // Xử lý product.imei (single IMEI)
        if (product.imei) {
          const imeiValue = typeof product.imei === 'object' ? product.imei.imei : product.imei
          console.log(`🔍 SelectedProduct ${index} single imei: ${imeiValue} vs ${imei}`)
          if (imeiValue === imei) {
            console.log(`🔍 Removing single imei from selectedProduct ${index}`)
            product.imei = null // Xóa IMEI khỏi sản phẩm
          }
        }
      })
      
      console.log('🔍 After processing selectedProductForImei:', selectedProductForImei.value)
    }

    // Hiển thị thông báo thành công
    toastRef.value?.success('Thành công', 'IMEI đã được hủy và xóa khỏi danh sách')

    // Refresh tracking data để cập nhật dữ liệu từ backend
    await refreshTrackingData()

  } catch (error) {
    console.error('Error canceling IMEI:', error)
    toastRef.value?.error('Lỗi', 'Có lỗi xảy ra khi hủy IMEI')
  }
}

function showImeiDetailsForAllProducts() {
  if (trackingData.value && trackingData.value.danhSachSanPham && trackingData.value.danhSachSanPham.length > 0) {
    console.log('🔍 Products for IMEI modal:', trackingData.value.danhSachSanPham)
    trackingData.value.danhSachSanPham.forEach((product, index) => {
      console.log(`🔍 Product ${index} imeis:`, product.imeis)
      // Khởi tạo newImei cho mỗi sản phẩm
      if (!product.newImei) {
        product.newImei = ''
      }
    })
    selectedProductForImei.value = trackingData.value.danhSachSanPham
    showImeiModal.value = true
  } else {
    console.log('No products available to show IMEI details')
  }
}

// Product QR Scanner methods
async function startProductQRScanner(product: any) {
  try {
    currentProductForQR.value = product
    showProductQRScannerModal.value = true
    await nextTick()

    const video = document.getElementById('product-qr-scanner-video') as HTMLVideoElement
    if (video) {
      productQRScanner.value = new QrScanner(
        video,
        result => {
          console.log('Product QR Code detected:', result)
          product.newImei = result.data
          closeProductQRScanner()
        },
        {
          onDecodeError: error => {
            // Ignore decode errors
          }
        }
      )
      await productQRScanner.value.start()
    }
  } catch (error) {
    console.error('Error starting product QR scanner:', error)
    toastRef.value?.error('Lỗi', 'Không thể khởi động camera')
  }
}

function closeProductQRScanner() {
  if (productQRScanner.value) {
    productQRScanner.value.stop()
    productQRScanner.value = null
  }
  showProductQRScannerModal.value = false
  currentProductForQR.value = null
}

// Add IMEI to product
async function addImeiToProduct(product: any) {
  if (!product.newImei?.trim()) {
    toastRef.value?.error('Lỗi', 'Vui lòng nhập IMEI')
    return
  }

  try {
    console.log('Adding IMEI to product:', product.newImei)

    const newImei = String(product.newImei).trim()

    // Chặn IMEI trùng toàn đơn
    if (isImeiDuplicatedInOrder(newImei)) {
      toastRef.value?.warning('Cảnh báo', 'IMEI này đã tồn tại trong đơn hàng')
      return
    }

    // Kiểm tra IMEI đã tồn tại trong UI hiện tại
    if (!product.imeis) {
      product.imeis = []
    }
    const existingImei = product.imeis.find((imei: any) => {
      const imeiValue = typeof imei === 'object' ? imei.imei : imei
      return String(imeiValue).trim() === newImei
    })
    if (existingImei) {
      toastRef.value?.warning('Cảnh báo', 'IMEI này đã tồn tại trong sản phẩm')
      return
    }

    // Validate IMEI thuộc sản phẩm (nếu backend hỗ trợ)
    const belongs = await validateImeiBelongsToProduct(newImei, product)
    if (!belongs) {
      toastRef.value?.error('Lỗi', 'IMEI không khớp với sản phẩm này')
      return
    }

    // Lưu vào backend để không bị mất khi reload
    await saveImeiToDatabase(newImei, product)

    // Cập nhật UI ngay lập tức
    product.imeis.push({ imei: newImei, trangThai: 0 })
    const savedImei = newImei
    product.newImei = ''

    toastRef.value?.success('Thành công', `Đã thêm IMEI ${savedImei}`)

    // Reload dữ liệu từ backend để đồng bộ đầy đủ
    await refreshTrackingData()

  } catch (error) {
    console.error('Error adding IMEI to product:', error)
    toastRef.value?.error('Lỗi', 'Có lỗi xảy ra khi thêm IMEI')
  }
}

// Utility functions
function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}

function formatDate(dateString: string): string {
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function getStatusText(trangThai: number): string {
  return getStatusName(trangThai)
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
      } else if (loai === 'BAN_ONLINE' || loai === 'DELIVERY' || loai === 'ONLINE') {
        return 'Đơn online'
      } else {
        return loai || 'Không xác định'
      }
    })()}</p>
            <p style="margin: 5px 0; font-size: 14px;"><strong>Phương thức thanh toán:</strong> ${(() => {
      const method = trackingData.value.phuongThucThanhToan
      if (!method) return 'Chưa chọn'

      const paymentMethods = {
        'cod': 'Thanh toán khi nhận hàng (COD)',
        'momo': 'Ví MoMo',
        'bank': 'Chuyển khoản ngân hàng',
        'prepaid': 'Thanh toán trước',
        'cash': 'Tiền mặt',
        'card': 'Thẻ tín dụng/ghi nợ'
      }

      return paymentMethods[method] || method
    })()}</p>
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
          ${trackingData.value.phiVanChuyen && trackingData.value.phiVanChuyen > 0 ?
        `<p style="margin: 5px 0; font-size: 16px;"><strong>Phí vận chuyển:</strong> ${formatCurrency(trackingData.value.phiVanChuyen)}</p>` : ''}
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
    if (!printWindow) {
      toastRef.value?.error('Lỗi', 'Không thể mở cửa sổ in. Vui lòng cho phép popup.')
      return
    }
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

// Cleanup
onBeforeUnmount(() => {
  closeQRScanner()
  closeProductQRScanner()
})

// Utility: kiểm tra IMEI đã tồn tại trong toàn bộ đơn hàng
function isImeiDuplicatedInOrder(imeiToCheck: string): boolean {
  if (!trackingData.value?.danhSachSanPham) return false
  const normalized = String(imeiToCheck).trim()
  for (const p of trackingData.value.danhSachSanPham) {
    const list = p?.imeis || []
    for (const item of list) {
      const value = typeof item === 'object' ? item.imei : item
      if (String(value).trim() === normalized) return true
    }
    if (p?.imei && String((typeof p.imei === 'object' ? p.imei.imei : p.imei)).trim() === normalized) return true
  }
  return false
}

// Validate IMEI thuộc đúng sản phẩm (best-effort)
async function validateImeiBelongsToProduct(imei: string, product: any): Promise<boolean> {
  try {
    const API_BASE_URL = 'http://localhost:8080'
    const res = await fetch(`${API_BASE_URL}/api/imei/validate?imei=${encodeURIComponent(imei)}`)
    if (!res.ok) {
      console.warn('IMEI validate API not available, skipping strict validation')
      return true
    }
    const data = await res.json()
    if (data?.valid === false) return false

    const productMaCtsp = product?.maCtsp
    const productCtspId = product?.ctspId || product?.id
    if (data?.maCtsp && productMaCtsp && String(data.maCtsp) !== String(productMaCtsp)) return false
    if (data?.ctspId && productCtspId && String(data.ctspId) !== String(productCtspId)) return false
    return true
  } catch (e) {
    console.warn('IMEI validation error, skipping strict validation', e)
    return true
  }
}
</script>

<template>
  <div class="hoa-don-detail-page">
    <PosHeader />

    <div class="main-content">
      <!-- Search Bar - Top Header -->
      <div class="search-header">
        <div class="search-container">
          <div class="search-input-group">
            <font-awesome-icon icon="search" class="search-icon" />
              <input
                v-model="trackingCode"
                type="text"
              :placeholder="trackingCode || 'Nhập mã đơn hàng...'"
              class="search-input"
                @keyup.enter="searchTracking"
              />
            </div>
          <div class="search-actions">
            <button @click="searchTracking" class="btn-search" title="Tìm kiếm">
                <font-awesome-icon icon="search" />
              </button>
            <button @click="startQRScanner" class="btn-qr" title="Quét QR Code">
                <img src="/QR.png" alt="QR" class="qr-icon" />
              <span>QR Code</span>
              </button>
              <button @click="resetTrackingPage" class="btn-refresh" title="Làm mới">
                <font-awesome-icon icon="refresh" />
              </button>
            </div>
          </div>
        </div>

      <!-- Order Status Section -->
      <div v-if="trackingData" class="order-status-section">
        <div class="status-header">
              <font-awesome-icon icon="clock" />
              <span>Trạng Thái Hóa Đơn - {{ trackingData.maHoaDon }}</span>
            </div>
        <div class="status-timeline">
          <div class="status-steps">
                <div
                  v-for="(step, index) in getTrackingSteps"
                  :key="index"
              :class="['status-step', {
                    active: !(index < getCurrentStepIndex() || (step.time && index === getCurrentStepIndex()) || (trackingData?.loaiHoaDon === 'BAN_THUONG' || trackingData?.loaiHoaDon === 'NORMAL')),
                    completed: index < getCurrentStepIndex() || (step.time && index === getCurrentStepIndex()) || (trackingData?.loaiHoaDon === 'BAN_THUONG' || trackingData?.loaiHoaDon === 'NORMAL'),
                clickable: ((index === getCurrentStepIndex() + 1) || (index === getCurrentStepIndex() && step.status !== 0) || (trackingData?.trangThai === 0 && index === 1 && step.status === 1 && trackingSteps[0].time) || (trackingData?.trangThai === 1 && index === 1 && step.status === 1)) && !isUpdatingStatus && !(trackingData?.loaiHoaDon === 'BAN_THUONG' || trackingData?.loaiHoaDon === 'NORMAL'),
                'pending-confirmation': step.status === 0 && trackingData?.trangThai === 0 && !step.time,
                'activated': step.status === 0 && step.time && trackingData?.trangThai === 0
                  }]"
              :title="`Step ${index}: ${step.title} (status: ${step.status}), Current: ${getCurrentStepIndex()}, Clickable: ${((index === getCurrentStepIndex() + 1) || (index === getCurrentStepIndex())) && !isUpdatingStatus && !(trackingData?.loaiHoaDon === 'BAN_THUONG' || trackingData?.loaiHoaDon === 'NORMAL')}`"
                  @click="(trackingData?.loaiHoaDon === 'BAN_THUONG' || trackingData?.loaiHoaDon === 'NORMAL') ? null : handleStepClick(step, index)"
                >
                  <div class="step-icon">
                    <font-awesome-icon
                      :icon="(index < getCurrentStepIndex() || (step.time && index === getCurrentStepIndex()) || (trackingData?.loaiHoaDon === 'BAN_THUONG' || trackingData?.loaiHoaDon === 'NORMAL')) ? 'check' : step.icon"
                      v-if="index < getCurrentStepIndex() || index === getCurrentStepIndex() || (trackingData?.loaiHoaDon === 'BAN_THUONG' || trackingData?.loaiHoaDon === 'NORMAL')"
                    />
                    <font-awesome-icon :icon="step.icon" v-else />
                  </div>
                  <div class="step-content">
                    <div class="step-title">{{ step.title }}</div>
                    <div class="step-time">{{ step.time || 'Không có thời gian' }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

      <!-- Main Content - Two Column Layout -->
      <div v-if="trackingData" class="main-content-container">
            <!-- Left Column -->
            <div class="left-column">
          <!-- Order Information and Product List Combined -->
              <div class="info-card">
                <h3>Thông Tin Đơn Hàng</h3>
                <div class="info-item">
                  <label>Mã đơn hàng:</label>
                  <span class="order-code">{{ trackingData.maHoaDon }}</span>
                </div>
                <div class="info-item">
                  <label>Loại đơn:</label>
              <span :class="['order-type', (trackingData.loaiHoaDon === 'BAN_ONLINE' || trackingData.loaiHoaDon === 'DELIVERY' || trackingData.loaiHoaDon === 'ONLINE') ? 'online' : 'normal']">
                    <font-awesome-icon :icon="(trackingData.loaiHoaDon === 'BAN_ONLINE' || trackingData.loaiHoaDon === 'DELIVERY' || trackingData.loaiHoaDon === 'ONLINE') ? 'laptop' : 'store'" />
                    {{ (trackingData.loaiHoaDon === 'BAN_ONLINE' || trackingData.loaiHoaDon === 'DELIVERY' || trackingData.loaiHoaDon === 'ONLINE') ? 'Đơn online' : 'Bán tại quầy' }}
                  </span>
            </div>
            <div class="info-item">
              <label>Phương thức thanh toán:</label>
              <span class="payment-method">
                    {{ getPaymentMethodName(trackingData.phuongThucThanhToan) }}
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
                  <span>{{ trackingData.phieuGiamGia || 'Không có' }}</span>
                </div>
                <div class="info-item">
                  <label>Ngày đặt:</label>
                  <span>{{ formatDate(trackingData.ngayDat) }}</span>
                </div>
                <div class="info-item">
                  <label>Nhân viên:</label>
                  <span>{{ trackingData.tenNhanVien || 'Không xác định' }}</span>
              </div>

            <hr class="card-divider" />

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
                      <div v-if="product.imei" class="imei-info">
                        <span>IMEI: {{ typeof product.imei === 'object' ? product.imei.imei : product.imei }}</span>
                      </div>
                    </div>
                  </div>
                </div>
                <div v-else class="no-products">
                  <font-awesome-icon icon="box-open" />
                  <p>Không có sản phẩm trong đơn hàng này</p>
                </div>
              </div>
            </div>

            <!-- Right Column -->
            <div class="right-column">
              <!-- Customer Information -->
              <div class="info-card">
                <h3>Thông Tin Khách Hàng</h3>
                <div class="info-item">
                  <label>Tên khách hàng:</label>
                  <span>{{ trackingData.tenKhachHang || 'Khách lẻ' }}</span>
                </div>
                <div class="info-item">
                  <label>Số điện thoại:</label>
                  <span>{{ trackingData.soDienThoai || 'Không có' }}</span>
                </div>
                <div class="info-item">
                  <label>Email:</label>
                  <span>{{ trackingData.email || 'Không có' }}</span>
                </div>
                <div class="info-item">
                  <label>Địa chỉ:</label>
                  <span>{{ trackingData.diaChi || 'Không có' }}</span>
                </div>
            <div class="info-item" v-if="trackingData.phuongThucGiaoHang">
              <label>Phương thức giao hàng:</label>
              <span class="shipping-method">{{ getShippingMethodName(trackingData.phuongThucGiaoHang) }}</span>
            </div>
                <div class="info-item" v-if="trackingData.ghiChu">
                  <label>Ghi chú:</label>
                  <span>{{ trackingData.ghiChu }}</span>
                </div>
              </div>

              <!-- Order Summary -->
              <div class="info-card">
                <h3>Tổng kết đơn hàng</h3>
                <div class="summary-item">
                  <label>Tổng tiền hàng:</label>
                  <div class="amount-section">
                    <span class="amount">{{ formatCurrency(trackingData.tongTienHang) }}</span>
                    <div class="amount-note">Giá trị sản phẩm</div>
                  </div>
                </div>
                <div class="summary-item">
                  <label>Giảm giá:</label>
                  <div class="amount-section">
                    <span class="amount discount">{{ formatCurrency(trackingData.giamGia || 0) }}</span>
                    <div class="amount-note">Khuyến mãi áp dụng</div>
                  </div>
                </div>
            <div class="summary-item" v-if="trackingData.phiVanChuyen && trackingData.phiVanChuyen > 0">
              <label>Phí vận chuyển:</label>
              <div class="amount-section">
                <span class="amount">{{ formatCurrency(trackingData.phiVanChuyen) }}</span>
                <div class="amount-note">Chi phí giao hàng</div>
              </div>
            </div>
                <div class="summary-item total">
                  <label>Thành tiền:</label>
                  <div class="amount-section">
                    <span class="amount">{{ formatCurrency(trackingData.thanhTien) }}</span>
                    <div class="amount-note">Số tiền phải thanh toán</div>
              </div>
            </div>
          </div>
                  </div>
                </div>
                
      <!-- Action Buttons - Bottom Row -->
      <div v-if="trackingData" class="action-buttons">
        <div class="action-buttons-left">
          <button class="btn-action" @click="showImeiDetailsForAllProducts()">
            <font-awesome-icon icon="barcode" />
            IMEI
          </button>
          <button 
            v-if="trackingData?.trangThai === 0" 
            class="btn-action btn-confirm" 
            @click="confirmOrder"
            :disabled="isUpdatingStatus"
          >
            <font-awesome-icon icon="check" />
            Xác nhận
          </button>
        </div>
        <div class="action-buttons-right">
                  <button class="btn-action" @click="openUpdateStatusModal">
                    <font-awesome-icon icon="edit" />
                    Cập nhật
                  </button>
                  <button class="btn-action" @click="printInvoice">
                    <font-awesome-icon icon="print" />
                    In hoá đơn
                  </button>
            </div>
        </div>

        <!-- No Data State -->
        <div v-else-if="trackingSearched" class="no-data">
          <font-awesome-icon icon="search" />
          <h3>Không tìm thấy đơn hàng</h3>
          <p>Mã đơn hàng "{{ trackingCode }}" không tồn tại hoặc đã bị xóa</p>
          <button @click="resetTrackingPage" class="btn-retry">
            <font-awesome-icon icon="refresh" />
            Thử lại
          </button>
        </div>

        <!-- Initial State -->
        <div v-else class="initial-state">
          <font-awesome-icon icon="search" />
          <h3>Tra cứu đơn hàng</h3>
          <p>Nhập mã đơn hàng để xem chi tiết và theo dõi trạng thái</p>
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
              class="px-6 py-2 bg-gray-500 text-white font-medium rounded-full hover:bg-gray-600 transition-all"
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

    <!-- Payment Method Modal - REMOVED: No longer needed, data comes from backend -->

    <!-- IMEI Modal -->
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
                <div class="flex items-center gap-3">
                  <h5 class="font-semibold text-gray-700">Mã IMEI:</h5>
                  <input
                    v-model="product.newImei"
                    type="text"
                    placeholder="Nhập IMEI..."
                    class="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500"
                  />
                  <button
                    @click="startProductQRScanner(product)"
                    class="w-10 h-10 bg-purple-500 hover:bg-purple-600 rounded-full transition-colors flex items-center justify-center"
                    title="Quét QR Code"
                  >
                    <img src="/QR.png" alt="QR" class="w-6 h-6" />
                  </button>
                  <button
                    @click="addImeiToProduct(product)"
                    :disabled="!product.newImei?.trim()"
                    :class="[
                      'w-10 h-10 rounded-full transition-colors flex items-center justify-center',
                      product.newImei?.trim() 
                        ? 'bg-orange-500 hover:bg-orange-600 text-white' 
                        : 'bg-gray-300 text-gray-500 cursor-not-allowed'
                    ]"
                    title="Thêm IMEI"
                  >
                    <font-awesome-icon icon="plus" class="text-lg" />
                  </button>
                </div>

                <!-- Existing IMEI List -->
                <div v-if="product.imeis && product.imeis.length > 0" class="space-y-2">
                  <div v-for="(imei, imeiIndex) in product.imeis" :key="imeiIndex" 
                       v-show="typeof imei === 'object' ? imei.trangThai === 0 : true"
                       class="bg-gradient-to-r from-orange-50 to-yellow-50 border-2 border-orange-300 rounded-lg p-4 flex items-center justify-between">
                    <div class="flex-1">
                      <div class="font-bold text-gray-800 text-lg font-mono">{{ typeof imei === 'object' ? imei.imei : imei }}</div>
                    </div>
                    <div class="flex items-center gap-2">
                      <div class="w-7 h-7 bg-orange-500 rounded-full flex items-center justify-center text-white font-bold">
                        ✓
                      </div>
                      <button 
                        @click="cancelImei(typeof imei === 'object' ? imei.imei : imei)"
                        class="w-7 h-7 bg-red-500 hover:bg-red-600 rounded-full flex items-center justify-center text-white transition-colors"
                        title="Hủy IMEI"
                      >
                        <font-awesome-icon icon="trash" class="text-sm" />
                      </button>
                    </div>
                  </div>
                </div>
                <div v-else class="text-center py-4 text-gray-500 italic">
                  Chưa có IMEI nào
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
              <div class="flex items-center gap-3">
                <h5 class="font-semibold text-gray-700">Mã IMEI:</h5>
                <input
                  v-model="selectedProductForImei.newImei"
                  type="text"
                  placeholder="Nhập IMEI..."
                  class="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500"
                />
                <button
                  @click="startProductQRScanner(selectedProductForImei)"
                  class="w-10 h-10 bg-purple-500 hover:bg-purple-600 rounded-full transition-colors flex items-center justify-center"
                  title="Quét QR Code"
                >
                  <img src="/QR.png" alt="QR" class="w-6 h-6" />
                </button>
                <button
                  @click="addImeiToProduct(selectedProductForImei)"
                  :disabled="!selectedProductForImei.newImei?.trim()"
                  :class="[
                    'w-10 h-10 rounded-full transition-colors flex items-center justify-center',
                    selectedProductForImei.newImei?.trim() 
                      ? 'bg-orange-500 hover:bg-orange-600 text-white' 
                      : 'bg-gray-300 text-gray-500 cursor-not-allowed'
                  ]"
                  title="Thêm IMEI"
                >
                  <font-awesome-icon icon="plus" class="text-lg" />
                </button>
              </div>

              <!-- Existing IMEI List -->
              <div v-if="selectedProductForImei.imeis && selectedProductForImei.imeis.length > 0" class="space-y-2">
                <div v-for="(imei, imeiIndex) in selectedProductForImei.imeis" :key="imeiIndex" 
                     v-show="typeof imei === 'object' ? imei.trangThai === 0 : true"
                     class="bg-gradient-to-r from-orange-50 to-yellow-50 border-2 border-orange-300 rounded-lg p-4 flex items-center justify-between">
                  <div class="flex-1">
                    <div class="font-bold text-gray-800 text-lg font-mono">{{ typeof imei === 'object' ? imei.imei : imei }}</div>
                  </div>
                  <div class="flex items-center gap-2">
                    <div class="w-7 h-7 bg-orange-500 rounded-full flex items-center justify-center text-white font-bold">
                      ✓
                    </div>
                    <button 
                      @click="cancelImei(typeof imei === 'object' ? imei.imei : imei)"
                      class="w-7 h-7 bg-red-500 hover:bg-red-600 rounded-full flex items-center justify-center text-white transition-colors"
                      title="Hủy IMEI"
                    >
                      <font-awesome-icon icon="trash" class="text-sm" />
                    </button>
                  </div>
                </div>
              </div>
              <div v-else class="text-center py-4 text-gray-500 italic">
                Chưa có IMEI nào
              </div>
            </div>
          </div>
        </div>

        <!-- Modal Footer -->
        <div class="p-6 border-t border-purple-200 bg-gradient-to-r from-purple-50 to-pink-50 flex justify-end">
          <button
            @click="closeImeiModal"
            class="px-6 py-2 bg-white border border-gray-300 rounded-full text-gray-700 font-medium hover:bg-gray-50 transition-colors"
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
            class="px-6 py-2 bg-white border border-gray-300 rounded-full text-gray-700 font-medium hover:bg-gray-50 transition-colors"
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

    <!-- Product QR Scanner Modal -->
    <div v-if="showProductQRScannerModal" class="fixed inset-0 bg-white/20 flex items-center justify-center z-50" @click="closeProductQRScanner">
      <div class="bg-gradient-to-br from-blue-50 to-indigo-50 border-2 border-blue-200 rounded-xl w-4/5 max-w-2xl max-h-[80vh] flex flex-col shadow-2xl" @click.stop>
        <!-- Modal Header -->
        <div class="p-6 border-b border-blue-200 bg-gradient-to-r from-blue-100 to-indigo-100">
          <div class="flex items-center justify-between">
            <h3 class="text-xl font-bold text-gray-800">Quét QR Code cho sản phẩm</h3>
            <button @click="closeProductQRScanner" class="text-gray-500 hover:text-gray-700 transition-colors">
              <font-awesome-icon icon="times" class="text-xl" />
            </button>
          </div>
          <p class="text-gray-600 mt-2">{{ currentProductForQR?.tenSanPham || 'Sản phẩm' }}</p>
        </div>

        <!-- Modal Body -->
        <div class="flex-1 overflow-y-auto p-6">
          <div class="text-center space-y-6">
            <!-- Camera Container -->
            <div class="relative bg-gray-900 rounded-lg overflow-hidden">
              <video
                id="product-qr-scanner-video"
                class="w-full h-64 object-cover"
                autoplay
                muted
                playsinline
              ></video>

              <!-- QR Scanner Overlay -->
              <div class="absolute inset-0 flex items-center justify-center pointer-events-none">
                <div class="relative w-48 h-48 border-2 border-blue-400 rounded-lg">
                  <!-- Scan Line Animation -->
                  <div class="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-transparent via-blue-400 to-transparent animate-pulse"></div>

                  <!-- Corner indicators -->
                  <div class="absolute top-0 left-0 w-6 h-6 border-t-2 border-l-2 border-blue-400"></div>
                  <div class="absolute top-0 right-0 w-6 h-6 border-t-2 border-r-2 border-blue-400"></div>
                  <div class="absolute bottom-0 left-0 w-6 h-6 border-b-2 border-l-2 border-blue-400"></div>
                  <div class="absolute bottom-0 right-0 w-6 h-6 border-b-2 border-r-2 border-blue-400"></div>
                </div>
              </div>
            </div>

            <!-- Instructions -->
            <div class="bg-blue-100 rounded-lg p-4">
              <p class="text-blue-800 font-medium">Đưa camera vào QR code IMEI để quét</p>
              <p class="text-sm text-blue-600 mt-1">IMEI sẽ được tự động điền vào ô nhập</p>
            </div>
          </div>
        </div>

        <!-- Modal Footer -->
        <div class="p-6 border-t border-blue-200 bg-gradient-to-r from-blue-50 to-indigo-50">
          <div class="flex justify-end space-x-3">
            <button
              @click="closeProductQRScanner"
              class="px-6 py-2 bg-gray-500 text-white font-medium rounded-full hover:bg-gray-600 transition-all"
            >
              Đóng
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- IMEI Confirmation Modal -->
    <div v-if="showImeiConfirmationModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click="closeImeiConfirmationModal">
      <div class="bg-white rounded-xl w-4/5 max-w-2xl max-h-[90vh] flex flex-col shadow-2xl" @click.stop>
        <!-- Modal Header -->
        <div class="p-6 border-b border-gray-200 bg-gradient-to-r from-orange-50 to-red-50">
          <div class="flex items-center justify-between">
            <h3 class="text-xl font-bold text-gray-800 flex items-center gap-3">
              <font-awesome-icon icon="check" class="text-orange-500" />
              Xác nhận IMEI
            </h3>
            <button @click="closeImeiConfirmationModal" class="text-gray-400 hover:text-gray-600 transition-colors">
              <font-awesome-icon icon="times" class="text-xl" />
            </button>
          </div>
          <p class="text-gray-600 mt-2">Nhập IMEI hoặc quét QR code để xác nhận đơn hàng</p>
        </div>

        <!-- Modal Body -->
        <div class="p-6 flex-1 overflow-y-auto">
          <!-- IMEI Input -->
          <div class="mb-6">
            <label class="block text-sm font-medium text-gray-700 mb-2">Nhập IMEI thủ công</label>
            <div class="relative">
              <input
                v-model="imeiInput"
                type="text"
                placeholder="Nhập IMEI của sản phẩm..."
                class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 transition-all"
              />
              <font-awesome-icon icon="barcode" class="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
            </div>
          </div>

          <!-- QR Scanner Toggle -->
          <div class="mb-6">
            <div class="flex items-center justify-between mb-3">
              <label class="text-sm font-medium text-gray-700">Quét QR code</label>
              <button
                @click="isScanningQR ? stopImeiQRScanner() : startImeiQRScanner()"
                :class="[
                  'px-4 py-2 rounded-lg font-medium transition-all',
                  isScanningQR 
                    ? 'bg-red-500 text-white hover:bg-red-600' 
                    : 'bg-orange-500 text-white hover:bg-orange-600'
                ]"
              >
                <font-awesome-icon :icon="isScanningQR ? 'stop' : 'camera'" class="mr-2" />
                {{ isScanningQR ? 'Dừng quét' : 'Bắt đầu quét' }}
              </button>
            </div>

            <!-- QR Scanner Area -->
            <div v-if="isScanningQR" class="bg-gray-900 rounded-lg overflow-hidden">
              <div class="aspect-video bg-gray-800 flex items-center justify-center">
                <div class="text-center text-white">
                  <font-awesome-icon icon="camera" class="text-4xl mb-2" />
                  <p class="text-sm">Đưa camera vào QR code để quét</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Instructions -->
          <div class="bg-blue-50 border border-blue-200 rounded-lg p-4">
            <h4 class="font-medium text-blue-800 mb-2">Hướng dẫn:</h4>
            <ul class="text-sm text-blue-700 space-y-1">
              <li>• Nhập IMEI thủ công vào ô trên</li>
              <li>• Hoặc sử dụng camera để quét QR code</li>
              <li>• Sau khi xác nhận, đơn hàng sẽ chuyển sang trạng thái "Đã xác nhận"</li>
              <li>• Hóa đơn sẽ được in tự động</li>
            </ul>
          </div>
        </div>

        <!-- Modal Footer -->
        <div class="p-6 border-t border-gray-200 bg-gray-50">
          <div class="flex justify-end space-x-3">
            <button
              @click="closeImeiConfirmationModal"
              class="px-6 py-2 bg-gray-500 text-white font-medium rounded-lg hover:bg-gray-600 transition-all"
            >
              Hủy
            </button>
            <button
              @click="confirmImei"
              :disabled="!imeiInput.trim()"
              :class="[
                'px-6 py-2 font-medium rounded-full transition-all',
                imeiInput.trim() 
                  ? 'bg-orange-500 text-white hover:bg-orange-600' 
                  : 'bg-gray-300 text-gray-500 cursor-not-allowed'
              ]"
            >
              <font-awesome-icon icon="check" class="mr-2" />
              Xác nhận IMEI
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Toast Component -->
    <Toast ref="toastRef" />
  </div>
</template>

<style scoped>
.hoa-don-detail-page {
  min-height: 100vh;
  background: var(--bg-primary, #f8f9fa);
}

.main-content {
  padding: 20px;
  padding-top: 100px;
  width: 100%;
  margin: 0;
}

/* Search Header */
.search-header {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
  border: 1px solid #e2e8f0;
}

.search-container {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
}

.search-input-group {
  flex: 1;
  position: relative;
}

.search-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #6b7280;
  z-index: 10;
}

.search-input {
  width: 100%;
  height: 48px;
  padding: 14px 18px 14px 45px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  background: white;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: #f97316;
  box-shadow: 0 0 0 4px rgba(249, 115, 22, 0.1);
}

.search-actions {
  display: flex;
  gap: 12px;
}

.btn-search,
.btn-qr,
.btn-refresh {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 48px;
  padding: 0 20px;
  border: none;
  border-radius: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-search {
  background: #f97316;
  color: white;
}

.btn-search:hover {
  background: #ea580c;
  transform: translateY(-2px);
}

.btn-qr {
  background: #8b5cf6;
  color: white;
}

.btn-qr:hover {
  background: #7c3aed;
  transform: translateY(-2px);
}

.btn-refresh {
  background: #10b981;
  color: white;
}

.btn-refresh:hover {
  background: #059669;
  transform: translateY(-2px);
}

/* Order Status Section */
.order-status-section {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
  border: 1px solid #e2e8f0;
  padding: 24px;
}

.status-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.status-timeline {
  background: white;
  border-radius: 16px;
  padding: 24px;
  border: 1px solid #e2e8f0;
}

.status-steps {
  display: flex;
  justify-content: space-between;
  position: relative;
  padding: 20px 0;
}

.status-steps::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 2px;
  background: #e2e8f0;
  z-index: 1;
}

.status-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  z-index: 2;
  flex: 1;
}

.status-step.completed:not(:last-child)::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  right: -50%;
  height: 2px;
  background: #10b981;
  z-index: 1;
  transform: translateY(-50%);
}

.status-step .step-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f97316; /* Màu cam mặc định cho tất cả step chưa completed */
  color: white;
  font-size: 18px;
  margin-bottom: 8px;
  transition: all 0.3s ease;
}

.status-step.active .step-icon {
  background: #f97316;
  color: white;
}

.status-step.completed .step-icon {
  background: #10b981;
  color: white;
}

.status-step.clickable {
  cursor: pointer;
}

.status-step.clickable:hover .step-icon {
  transform: scale(1.1);
  box-shadow: 0 8px 25px rgba(249, 115, 22, 0.3);
}

.status-step.pending-confirmation .step-icon {
  background: #f97316;
  color: white;
  animation: pulse-orange 2s infinite;
  box-shadow: 0 0 20px rgba(249, 115, 22, 0.5);
}

.status-step.activated .step-icon {
  background: #10b981;
  color: white;
  box-shadow: 0 0 20px rgba(16, 185, 129, 0.5);
  animation: pulse-green 2s infinite;
}

@keyframes pulse-green {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 0 20px rgba(16, 185, 129, 0.5);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 0 0 30px rgba(16, 185, 129, 0.8);
  }
}

@keyframes pulse-orange {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 0 20px rgba(249, 115, 22, 0.5);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 0 0 30px rgba(249, 115, 22, 0.8);
  }
}

.status-step .step-content {
  text-align: center;
}

.status-step .step-title {
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 4px;
}

.status-step .step-time {
  font-size: 12px;
  color: #718096;
}

.qr-icon {
  width: 20px;
  height: 20px;
}

/* Main Content Container - Two Column Layout */
.main-content-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 32px;
}

.left-column,
.right-column {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.info-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #e2e8f0;
  min-height: 0;
}

.info-card h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #e5e7eb;
}

.card-divider {
  border: none;
  height: 1px;
  background: #e5e7eb;
  margin: 24px 0;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f1f5f9;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item label {
  font-weight: 500;
  color: #6b7280;
  font-size: 14px;
}

.info-item span {
  font-weight: 600;
  color: #1f2937;
  font-size: 14px;
}

.order-code {
  background: #fef3c7;
  color: #92400e;
  padding: 4px 8px;
  border-radius: 6px;
  font-family: monospace;
}

.order-type {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}

.order-type.online {
  background: #dbeafe;
  color: #1e40af;
}

.order-type.normal {
  background: #dcfce7;
  color: #166534;
}

.payment-method {
  font-weight: 500;
  color: #374151;
  padding: 4px 8px;
  background: #f3f4f6;
  border-radius: 6px;
  display: inline-block;
}

.shipping-method {
  font-weight: 500;
  color: #059669;
  padding: 4px 8px;
  background: #ecfdf5;
  border-radius: 6px;
  display: inline-block;
}

/* Product List */
.product-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.product-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: white;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  transition: all 0.3s ease;
}

.product-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.product-image {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
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
  color: #1f2937;
  margin-bottom: 4px;
  font-size: 16px;
}

.product-specs {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.product-specs span {
  background: #f3f4f6;
  color: #6b7280;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.product-price {
  font-weight: 700;
  color: #f97316;
  font-size: 16px;
}

.product-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}


.imei-info {
  margin: 10px 0;
  padding: 12px;
  background-color: #f0f0f0;
  font-size: 18px;
  font-weight: 700;
  border-left: 4px solid #f97316;
  border-radius: 6px;
}

.imei-info span {
  margin: 0;
}

.product-actions-bottom {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
  text-align: center;
}

.no-products {
  text-align: center;
  padding: 40px;
  color: #6b7280;
}

.no-products svg {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.no-products p {
  font-size: 16px;
  margin: 0;
}

/* Order Summary */
.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 12px 0;
  border-bottom: 1px solid #f1f5f9;
}

.summary-item:last-child {
  border-bottom: none;
}

.summary-item.total {
  border-top: 2px solid #e5e7eb;
  margin-top: 8px;
  padding-top: 16px;
  font-weight: 700;
}

.summary-item label {
  font-weight: 500;
  color: #6b7280;
  font-size: 14px;
}

.amount-section {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.summary-item .amount {
  font-weight: 600;
  color: #1f2937;
  font-size: 16px;
}

.summary-item.total .amount {
  font-size: 18px;
  color: #f97316;
}

.summary-item .amount.discount {
  color: #ef4444;
}

.summary-item .amount.discount:before {
  content: "(-";
}

.summary-item .amount.discount:after {
  content: ")";
}

.amount-note {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 2px;
}

/* Action Buttons - Bottom Row */
.action-buttons {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  gap: 16px;
}

.action-buttons-left {
  display: flex;
  gap: 16px;
}

.action-buttons-right {
  display: flex;
  gap: 16px;
}

.btn-action {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px 20px;
  background: #f97316;
  color: white;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
  min-width: 180px;
}

.btn-action:hover {
  background: #ea580c;
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(249, 115, 22, 0.3);
}

.btn-confirm {
  background: linear-gradient(135deg, #f97316, #ea580c);
  color: white;
  border: 2px solid #f97316;
}

.btn-confirm:hover {
  background: linear-gradient(135deg, #ea580c, #dc2626);
  border-color: #ea580c;
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(249, 115, 22, 0.4);
}

.btn-action svg {
  font-size: 16px;
}

/* Product Actions Bottom */
.product-actions-bottom {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
  display: flex;
  justify-content: flex-start;
}

.product-actions-bottom .btn-primary {
  background: #f97316;
  color: white;
  border: none;
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.product-actions-bottom .btn-primary:hover {
  background: #ea580c;
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(249, 115, 22, 0.3);
}


/* No Data States */
.no-data,
.initial-state {
  text-align: center;
  padding: 60px 24px;
  color: #6b7280;
}

.no-data svg,
.initial-state svg {
  font-size: 64px;
  margin-bottom: 24px;
  opacity: 0.5;
}

.no-data h3,
.initial-state h3 {
  font-size: 24px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 12px;
}

.no-data p,
.initial-state p {
  font-size: 16px;
  margin-bottom: 24px;
  max-width: 400px;
  margin-left: auto;
  margin-right: auto;
}

.btn-retry {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: #f97316;
  color: white;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
}

.btn-retry:hover {
  background: #ea580c;
  transform: translateY(-2px);
}

/* Status Classes */
.status-pending {
  color: #f59e0b;
}

.status-confirmed {
  color: #3b82f6;
}

.status-waiting {
  color: #8b5cf6;
}

.status-shipping {
  color: #06b6d4;
}

.status-completed {
  color: #10b981;
}

/* Responsive Design */
@media (max-width: 768px) {
  .main-content {
    padding: 16px;
    padding-top: 80px;
  }

  .search-container {
    flex-direction: column;
    gap: 12px;
  }

  .search-actions {
    width: 100%;
    justify-content: center;
  }

  .main-content-container {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .status-steps {
    flex-direction: row;
    gap: 8px;
    overflow-x: auto;
  }

  .status-step {
    min-width: 100px;
  }

  .action-buttons {
    flex-direction: column;
    gap: 12px;
    align-items: center;
    justify-content: center;
  }

  .action-buttons-left,
  .action-buttons-right {
    width: 100%;
    justify-content: center;
  }

  .btn-action {
    width: 100%;
    max-width: 280px;
    min-width: auto;
  }
}

@media (max-width: 480px) {
  .product-item {
    flex-direction: column;
    text-align: center;
  }

  .product-actions {
    flex-direction: row;
    justify-content: center;
  }

  .btn-primary {
    padding: 10px 20px;
    font-size: 13px;
  }
}
</style>
