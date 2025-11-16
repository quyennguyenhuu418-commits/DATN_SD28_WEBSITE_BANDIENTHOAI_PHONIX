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
import { useAuthStore } from '@/stores/authStore'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// API Base URL
const API_BASE_URL = 'http://localhost:8080'

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

// Confirm Add IMEI Modal (xác nhận trước khi thêm IMEI vào sản phẩm)
const showConfirmAddImeiModal = ref(false)
const confirmAddImeiMessage = ref('')
const confirmAddImeiProduct = ref<any>(null)
const confirmAddImeiImei = ref('')
// Tracking steps - 4 bước cố định cho BAN_ONLINE
const trackingSteps = ref([
  { title: 'Chờ xác nhận', icon: 'clock', time: '', status: 0, isBranch: true },
  { title: 'Chờ giao hàng', icon: 'box', time: '', status: 1, isBranch: false },
  { title: 'Đang giao hàng', icon: 'truck', time: '', status: 2, isBranch: false },
  { title: 'Hoàn thành', icon: 'check-circle', time: '', status: 3, isBranch: false },
  { title: 'Đã hủy', icon: 'times-circle', time: '', status: 4, isBranch: false }
])

// Computed property để lấy steps phù hợp với loại đơn hàng
const getTrackingSteps = computed(() => {
  if (!trackingData.value || !trackingData.value.loaiHoaDon) {
    return trackingSteps.value
  }

  const isBanThuong = trackingData.value.loaiHoaDon === 'BAN_THUONG' || trackingData.value.loaiHoaDon === 'NORMAL'
  const isCancelled = trackingData.value.trangThai === 4

  // Nếu đơn hàng đã hủy - chỉ hiển thị step "Đã hủy"
  if (isCancelled) {
    let cancelTime = ''
    // Ưu tiên sử dụng ngayCapNhat (thời gian cập nhật) nếu có, fallback về ngayTao
    const timeSource = trackingData.value.ngayCapNhat || trackingData.value.ngayTao
    if (timeSource) {
      const date = new Date(timeSource)
      cancelTime = date.toLocaleTimeString('vi-VN') + ' ' + date.toLocaleDateString('vi-VN')
    }
    return [{ title: 'Đã hủy', icon: 'times-circle', time: cancelTime, status: 4, isBranch: true, isCancelled: true }]
  }

  // Nếu không phải đã hủy, hiển thị timeline bình thường
  if (isBanThuong) {
    // Bán tại quầy: chỉ có 1 bước "Hoàn thành"
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
      status: 3,
      isBranch: true,
      isCompleted: true // Đánh dấu là đã hoàn thành
    }

    console.log('🔍 BAN_THUONG: Final step object:', step)
    return [step]
  } else {
    // Bán online: 4 bước nhưng không bao gồm "Đã hủy" khi chưa hủy
    // Lọc bỏ step "Đã hủy" (status = 4) nếu chưa bị hủy
    return trackingSteps.value.filter(step => step.status !== 4)
  }
})

// Watcher để cập nhật thời gian cho các step đã hoàn thành khi tải dữ liệu
watch(trackingData, (newData) => {
  if (newData && newData.loaiHoaDon !== 'BAN_THUONG' && newData.loaiHoaDon !== 'NORMAL') {
    // Chỉ cập nhật cho BAN_ONLINE
    console.log('🔍 Updating step times from database:', newData.lichSuTrangThai)

    // Reset tất cả step times trước khi cập nhật từ database
    const autoActivateConfirm = (newData.trangThai === 0) && checkAllProductsHaveImei()
    trackingSteps.value.forEach(step => {
      // Reset step "Chờ xác nhận" (status 0) khi đơn hàng về trạng thái 0
      if (step.status === 0 && newData.trangThai === 0) {
        if (autoActivateConfirm) {
          // Tự động active nút Chờ xác nhận khi tất cả sản phẩm đã có IMEI
          const createdAt = newData.ngayDat || newData.ngayTao
          if (createdAt) {
            const d = new Date(createdAt)
            step.time = d.toLocaleTimeString('vi-VN') + ' ' + d.toLocaleDateString('vi-VN')
            console.log('✅ Auto-activate step "Chờ xác nhận" (IMEI đầy đủ) với thời gian tạo đơn:', step.time)
          }
        } else {
          step.time = ''
          console.log('🔄 Reset step "Chờ xác nhận" - chưa activated')
        }
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
            // Step 0: chỉ set khi tất cả sản phẩm đã có IMEI (giữ logic yêu cầu)
            if (status === 0) {
              if (checkAllProductsHaveImei()) {
                trackingSteps.value[stepIndex].time = timeString
                console.log('✅ Set time for step "Chờ xác nhận" từ DB (IMEI đầy đủ):', timeString)
              } else {
                console.log('⚠️ Skipping auto-set time for step "Chờ xác nhận" - waiting for manual confirmation')
              }
            } else {
              // Steps 1–3: luôn set theo DB để không mất thời gian khi reload
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
        // Nếu trạng thái = 0 và đã có đủ IMEI: tự động set theo thời gian tạo đơn
        if (checkAllProductsHaveImei()) {
          const createdAt = newData.ngayDat || newData.ngayTao
          if (createdAt) {
            const d = new Date(createdAt)
            const timeString = d.toLocaleTimeString('vi-VN') + ' ' + d.toLocaleDateString('vi-VN')
            trackingSteps.value[0].time = timeString
            console.log('✅ Fallback: Auto-activate step 0 with order created time (IMEI đầy đủ):', timeString)
          }
        } else {
          console.log('⚠️ Fallback: Skipping auto-set time for step 0 when order status is 0 - waiting for manual confirmation')
        }
      }
    }
  }
}, { immediate: true })

// Computed property để hiển thị sản phẩm - giữ nguyên mỗi sản phẩm là 1 dòng
const displayProductList = computed(() => {
  console.log('🔍 displayProductList - trackingData:', trackingData.value)
  console.log('🔍 displayProductList - danhSachSanPham:', trackingData.value?.danhSachSanPham)
  console.log('🔍 displayProductList - danhSachSanPham type:', Array.isArray(trackingData.value?.danhSachSanPham))

  if (!trackingData.value) {
    console.log('❌ No trackingData found')
    return []
  }

  if (!trackingData.value.danhSachSanPham) {
    console.log('❌ No danhSachSanPham found')
    return []
  }

  if (!Array.isArray(trackingData.value.danhSachSanPham)) {
    console.warn('⚠️ danhSachSanPham is not an array, converting...')
    trackingData.value.danhSachSanPham = [trackingData.value.danhSachSanPham]
  }

  if (trackingData.value.danhSachSanPham.length === 0) {
    console.log('⚠️ danhSachSanPham is empty array')
    return []
  }

  // Duy trì thứ tự như backend trả về; gán khóa ổn định theo chiTietHoaDonId/maCtsp
  const result = trackingData.value.danhSachSanPham.map((product, index) => {
    const stableKey = product.chiTietHoaDonId || product.idChiTietHoaDon || product.maCtsp || `CTSP-${product.id}-${index}`
    return {
      ...product,
      stt: index + 1,
      _stableKey: stableKey,
      // Hiển thị tất cả IMEI nếu có array
      imeis: product.imeis || (product.imei ? [product.imei] : [])
    }
  })
  
  console.log('✅ displayProductList result:', result)
  return result
})

// Computed property để expand sản phẩm cho IN HÓA ĐƠN - chỉ mở rộng khi có nhiều IMEI trong 1 sản phẩm
const expandedProductList = computed(() => {
  console.log('🔍 expandedProductList - trackingData:', trackingData.value)
  console.log('🔍 expandedProductList - danhSachSanPham:', trackingData.value?.danhSachSanPham)
  console.log('🔍 expandedProductList - danhSachSanPham type:', Array.isArray(trackingData.value?.danhSachSanPham))

  if (!trackingData.value) {
    console.log('❌ No trackingData found')
    return []
  }

  if (!trackingData.value.danhSachSanPham) {
    console.log('❌ No danhSachSanPham found')
    return []
  }

  if (!Array.isArray(trackingData.value.danhSachSanPham)) {
    console.warn('⚠️ danhSachSanPham is not an array, converting...')
    trackingData.value.danhSachSanPham = [trackingData.value.danhSachSanPham]
  }

  if (trackingData.value.danhSachSanPham.length === 0) {
    console.log('⚠️ danhSachSanPham is empty array')
    return []
  }

  const expandedProducts = []
  let stt = 1

  trackingData.value.danhSachSanPham.forEach((product, index) => {
    console.log(`🔍 Processing product ${index}:`, product)

    const imeis = product.imeis || (product.imei ? [product.imei] : [])
    const imeiCount = imeis.length
    console.log(`🔍 Product ${index} imeiCount:`, imeiCount)

    if (imeiCount > 1) {
      // Nếu có nhiều IMEI trong 1 sản phẩm, expand thành nhiều dòng
      imeis.forEach((imei, imeiIndex) => {
        expandedProducts.push({
          ...product,
          stt: stt++,
          soLuong: 1,
          imei: imei,
          imeiIndex: imeiIndex + 1,
          maCtsp: product.maCtsp
        })
      })
    } else if (imeiCount === 1) {
      // Nếu chỉ có 1 IMEI
      expandedProducts.push({
        ...product,
        stt: stt++,
        imei: imeis[0],
        imeiIndex: 1,
        maCtsp: product.maCtsp
      })
    } else {
      // Không có IMEI
      expandedProducts.push({
        ...product,
        stt: stt++,
        imei: null,
        imeiIndex: null,
        maCtsp: product.maCtsp
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
  { value: 2, label: 'Đang giao' },
  { value: 3, label: 'Hoàn thành' },
  { value: 4, label: 'Đã hủy' }
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
      // Đảm bảo dữ liệu được gán đúng và có đầy đủ các trường
      trackingData.value = {
        ...data,
        // Đảm bảo danhSachSanPham luôn là array
        danhSachSanPham: Array.isArray(data.danhSachSanPham) ? data.danhSachSanPham : (data.danhSachSanPham ? [data.danhSachSanPham] : []),
        // Đảm bảo lichSuTrangThai luôn là array
        lichSuTrangThai: Array.isArray(data.lichSuTrangThai) ? data.lichSuTrangThai : (data.lichSuTrangThai ? [data.lichSuTrangThai] : [])
      }
      
      console.log('🔄 Updated trackingData:', trackingData.value)
      console.log('🔄 danhSachSanPham:', trackingData.value?.danhSachSanPham)
      console.log('🔄 danhSachSanPham length:', trackingData.value?.danhSachSanPham?.length)
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
    console.error('❌ Error details:', error.response?.data || error.message)
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
    console.log('🔍 API Response type:', typeof data)
    console.log('🔍 API Response keys:', data ? Object.keys(data) : 'null')

    if (data) {
      // Đảm bảo dữ liệu được gán đúng và có đầy đủ các trường
      trackingData.value = {
        ...data,
        // Đảm bảo danhSachSanPham luôn là array
        danhSachSanPham: Array.isArray(data.danhSachSanPham) ? data.danhSachSanPham : (data.danhSachSanPham ? [data.danhSachSanPham] : []),
        // Đảm bảo lichSuTrangThai luôn là array
        lichSuTrangThai: Array.isArray(data.lichSuTrangThai) ? data.lichSuTrangThai : (data.lichSuTrangThai ? [data.lichSuTrangThai] : [])
      }
      
      trackingSearched.value = true
      
      console.log('✅ Set trackingData:', trackingData.value)
      console.log('✅ danhSachSanPham:', trackingData.value?.danhSachSanPham)
      console.log('✅ danhSachSanPham type:', Array.isArray(trackingData.value?.danhSachSanPham))
      console.log('✅ danhSachSanPham length:', trackingData.value?.danhSachSanPham?.length)
      console.log('✅ lichSuTrangThai:', trackingData.value?.lichSuTrangThai)
      console.log('✅ lichSuTrangThai type:', Array.isArray(trackingData.value?.lichSuTrangThai))
      console.log('✅ lichSuTrangThai length:', trackingData.value?.lichSuTrangThai?.length)

      // Debug chi tiết danhSachSanPham
      if (trackingData.value?.danhSachSanPham && trackingData.value.danhSachSanPham.length > 0) {
        console.log('📦 Products detail:')
        trackingData.value.danhSachSanPham.forEach((product, idx) => {
          console.log(`  Product ${idx}:`, {
            id: product.id,
            tenSanPham: product.tenSanPham,
            maCtsp: product.maCtsp,
            hinhAnh: product.hinhAnh,
            imeis: product.imeis,
            imei: product.imei
          })
        })
      } else {
        console.warn('⚠️ danhSachSanPham is empty or undefined')
      }

      // Debug chi tiết lichSuTrangThai
      if (trackingData.value?.lichSuTrangThai && trackingData.value.lichSuTrangThai.length > 0) {
        console.log('📜 History detail:')
        trackingData.value.lichSuTrangThai.forEach((log, idx) => {
          console.log(`  History ${idx}:`, {
            trangThai: log.trangThai,
            tenTrangThai: log.tenTrangThai,
            thoiGian: log.thoiGian,
            actor: log.actor || log.maNhanVien || log.tenNhanVien
          })
        })
      } else {
        console.warn('⚠️ lichSuTrangThai is empty or undefined')
      }

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
    console.error('❌ Error searching tracking:', error)
    console.error('❌ Error details:', error.response?.data || error.message)
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
  const isCancelled = trackingData.value.trangThai === 4

  // Nếu đơn hàng đã hủy
  if (isCancelled) {
    return 0  // Trả về index 0 cho step "Đã hủy" (step duy nhất trong mảng)
  }

  if (isBanThuong) {
    return 0  // Trả về index 0 cho step "Hoàn thành" (step duy nhất trong mảng)
  } else {
    // Bán online: 4 bước đầy đủ
    switch (status) {
      case 0: return 0  // Chờ xác nhận
      case 1: return 1  // Chờ giao hàng
      case 2: return 2  // Đang giao hàng
      case 3: return 3  // Hoàn thành
      case 4: return 0  // Đã hủy - không nên đến đây vì đã handle ở trên
      default: return 0
    }
  }
}

function isStepClickable(stepIndex: number, step: any, currentStepIndex: number): boolean {
  if (!trackingData.value) return false
  if (isUpdatingStatus.value) return false
  
  const loaiHoaDon = trackingData.value.loaiHoaDon
  const isBanThuong = loaiHoaDon === 'BAN_THUONG' || loaiHoaDon === 'NORMAL'
  
  if (isBanThuong) {
    return false // Bán tại quầy không cho click
  }
  
  // Không cho phép click khi đơn hàng đã hoàn thành
  if (trackingData.value.trangThai === 3) {
    return false
  }
  
  // Không cho click bước "Chờ xác nhận"
  if (step.status === 0) {
    return false
  }
  
  // Kiểm tra tất cả các bước trước đó đã có time chưa
  let allPreviousStepsHaveTime = true
  for (let i = 0; i < stepIndex; i++) {
    if (!trackingSteps.value[i].time) {
      allPreviousStepsHaveTime = false
      break
    }
  }
  
  // Chỉ cho phép click nếu:
  // 1. Tất cả các bước trước đó đã có time
  // 2. Là bước tiếp theo (stepIndex >= currentStepIndex)
  return allPreviousStepsHaveTime && stepIndex >= currentStepIndex
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
    3: 'status-completed',
    4: 'status-cancelled'
  }
  return statusClasses[trangThai] || 'status-pending'
}

function getStatusColor(trangThai: number): string {
  const statusColors = {
    0: '#f59e0b',  // Chờ xác nhận
    1: '#3b82f6',  // Chờ giao hàng
    2: '#8b5cf6',  // Đang giao
    3: '#10b981',  // Hoàn thành
    4: '#ef4444'   // Đã hủy
  }
  return statusColors[trangThai] || '#6b7280'
}

function getStatusIcon(trangThai: number): string {
  const statusIcons = {
    0: 'clock',           // Chờ xác nhận
    1: 'shipping-fast',   // Chờ giao hàng
    2: 'truck',           // Đang giao
    3: 'check-circle',    // Hoàn thành
    4: 'times-circle'     // Đã hủy
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
    'express': 'Nhanh',
    'ghn': 'Hỏa tốc',
    'fast': 'Nhanh',
    'urgent': 'Hỏa tốc',
    'pickup': 'Lấy tại cửa hàng',
    'delivery': 'Giao hàng tận nơi'
  }

  return shippingMethods[method.toLowerCase()] || method
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
    console.log('🔼 Product for saveImeiToDatabase:', product)
    console.log('🔼 Full product data:', JSON.stringify(product, null, 2))

    const API_BASE_URL = 'http://localhost:8080'
    
    // Xác định chính xác sản phẩm cần lưu IMEI
    let lineIndex: number | null = null
    let chiTietHoaDonId: number | null = null
    let originalProduct = product
    
    try {
      if (trackingData.value?.danhSachSanPham && product) {
        // Ưu tiên _index (được gắn khi mở modal) để phân biệt 2 dòng giống nhau
        if (typeof product._index === 'number' && product._index >= 0) {
          lineIndex = product._index
        } else {
          // Tìm bằng tham chiếu object trước
          let idx = trackingData.value.danhSachSanPham.findIndex((p: any) => p === product)
          // Nếu không có, tìm theo chiTietHoaDonId/idChiTietHoaDon
          if (idx === -1) {
            idx = trackingData.value.danhSachSanPham.findIndex((p: any) => (
              (p.chiTietHoaDonId && product.chiTietHoaDonId && p.chiTietHoaDonId === product.chiTietHoaDonId) ||
              (p.idChiTietHoaDon && product.idChiTietHoaDon && p.idChiTietHoaDon === product.idChiTietHoaDon)
            ))
          }
          lineIndex = idx !== -1 ? idx : null
        }

        if (lineIndex !== null && lineIndex !== -1) {
          originalProduct = trackingData.value.danhSachSanPham[lineIndex]

          // Chỉ lấy chiTietHoaDonId nếu có thuộc tính chính xác (không fallback về id)
          // vì id có thể là ctspId, không phải chiTietHoaDonId
          if (originalProduct.chiTietHoaDonId) {
            chiTietHoaDonId = originalProduct.chiTietHoaDonId
          } else if (originalProduct.idChiTietHoaDon) {
            chiTietHoaDonId = originalProduct.idChiTietHoaDon
          }
          
          console.log('✅ Found product at index:', lineIndex)
          console.log('✅ originalProduct:', originalProduct)
          console.log('✅ originalProduct.id:', originalProduct.id, '(this is ctspId, not chiTietHoaDonId)')
          console.log('✅ originalProduct.chiTietHoaDonId:', originalProduct.chiTietHoaDonId)
          console.log('✅ originalProduct.idChiTietHoaDon:', originalProduct.idChiTietHoaDon)
          console.log('✅ chiTietHoaDonId to send:', chiTietHoaDonId)
          console.log('✅ lineIndex:', lineIndex)
          console.log('✅ maCtsp:', originalProduct.maCtsp)
        } else {
          console.warn('⚠️ Product not found in danhSachSanPham, using provided product')
        }
      }
    } catch (error) {
      console.error('Error finding product:', error)
    }
    
    // Không gửi chiTietHoaDonId vì không chắc chắn giá trị đó có đúng không
    // Backend sẽ tự tìm bằng lineIndex, ctspId, hoặc maCtsp
    const payload: any = { 
      maHoaDon: trackingData.value.maHoaDon, 
      imei, 
      lineIndex
    }
    
    // Không gửi chiTietHoaDonId nữa - để backend tự tìm đúng HoaDonCt
    // if (chiTietHoaDonId && chiTietHoaDonId > 0) {
    //   payload.chiTietHoaDonId = chiTietHoaDonId
    // }
    
    if (originalProduct?.ctspId || originalProduct?.id) {
      payload.ctspId = originalProduct.ctspId ?? originalProduct.id
    }
    if (originalProduct?.maCtsp) {
      payload.maCtsp = originalProduct.maCtsp
    }
    
    console.log('🔼 save-imei payload:', payload)
    
    const response = await fetch(`${API_BASE_URL}/api/hoa-don/save-imei`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload)
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

// Lấy tên nhân viên đang đăng nhập (dùng khi gửi API cập nhật trạng thái)
function getCurrentStaffName(): string {
  if (authStore.user) {
    // Ưu tiên tên đầy đủ (hoTen), sau đó là username, cuối cùng là ID
    if (authStore.user.hoTen) {
      return authStore.user.hoTen
    }
    if (authStore.user.username) {
      return authStore.user.username
    }
    if (authStore.user.id) {
      return String(authStore.user.id)
    }
  }
  return 'Hệ thống'
}

// Lấy tên nhân viên đã xử lý hóa đơn (dùng để hiển thị - lấy từ lịch sử xử lý)
// Ưu tiên: lịch sử trạng thái (actor) > trackingData.tenNhanVien > Chưa xử lý
function getStaffName(): string {
  // Ưu tiên 1: Lấy từ lịch sử trạng thái - nhân viên cuối cùng đã xử lý đơn hàng
  if (trackingData.value?.lichSuTrangThai && Array.isArray(trackingData.value.lichSuTrangThai) && trackingData.value.lichSuTrangThai.length > 0) {
    // Lấy log mới nhất (cuối cùng trong mảng đã được sắp xếp theo thời gian tăng dần)
    const latestLog = trackingData.value.lichSuTrangThai[trackingData.value.lichSuTrangThai.length - 1]
    if (latestLog?.actor && latestLog.actor !== 'online_customer' && latestLog.actor !== 'ONLINE_CUSTOMER' && latestLog.actor !== 'Hệ thống') {
      return latestLog.actor
    }
    // Fallback: thử lấy từ maNhanVien hoặc tenNhanVien trong log
    if (latestLog?.maNhanVien && latestLog.maNhanVien !== 'online_customer' && latestLog.maNhanVien !== 'ONLINE_CUSTOMER') {
      return String(latestLog.maNhanVien)
    }
    if (latestLog?.tenNhanVien && latestLog.tenNhanVien !== 'online_customer' && latestLog.tenNhanVien !== 'ONLINE_CUSTOMER') {
      return latestLog.tenNhanVien
    }
  }
  
  // Ưu tiên 2: Lấy từ trackingData (người tạo đơn hàng)
  if (trackingData.value?.tenNhanVien && trackingData.value.tenNhanVien !== 'online_customer' && trackingData.value.tenNhanVien !== 'ONLINE_CUSTOMER') {
    return trackingData.value.tenNhanVien
  }
  if (trackingData.value?.maNhanVien && trackingData.value.maNhanVien !== 'online_customer' && trackingData.value.maNhanVien !== 'ONLINE_CUSTOMER') {
    return String(trackingData.value.maNhanVien)
  }
  
  // Nếu là đơn online và chưa có nhân viên xử lý
  if (trackingData.value?.loaiHoaDon === 'BAN_ONLINE' || trackingData.value?.loaiHoaDon === 'ONLINE') {
    return 'Chưa xử lý'
  }
  
  // Fallback cuối cùng
  return 'Không xác định'
}

// Lấy ID nhân viên đang đăng nhập để gửi về backend
function getStaffId(): number | string | null {
  if (authStore.user?.id) {
    return authStore.user.id
  }
  return null
}

// Kiểm tra tất cả sản phẩm đã có IMEI chưa
function checkAllProductsHaveImei(): boolean {
  if (!trackingData.value?.danhSachSanPham) {
    console.log('🔍 No products found')
    return false
  }

  console.log('🔍 Checking IMEI for all products:', trackingData.value.danhSachSanPham)

  for (let i = 0; i < trackingData.value.danhSachSanPham.length; i++) {
    const product = trackingData.value.danhSachSanPham[i]
    console.log(`🔍 Product ${i}:`, product)

    // Kiểm tra product.imeis (array)
    if (product.imeis && Array.isArray(product.imeis)) {
      if (product.imeis.length === 0) {
        console.log(`❌ Product ${i} has empty imeis array`)
        return false
      }
      // Kiểm tra từng IMEI trong array
      for (let j = 0; j < product.imeis.length; j++) {
        const imei = product.imeis[j]
        const imeiValue = typeof imei === 'object' ? imei.imei : imei
        if (!imeiValue || imeiValue.trim() === '') {
          console.log(`❌ Product ${i}, IMEI ${j} is empty`)
          return false
        }
      }
    }
    // Kiểm tra product.imei (single IMEI)
    else if (product.imei) {
      const imeiValue = typeof product.imei === 'object' ? product.imei.imei : product.imei
      if (!imeiValue || imeiValue.trim() === '') {
        console.log(`❌ Product ${i} has empty single imei`)
        return false
      }
    }
    // Không có IMEI nào
    else {
      console.log(`❌ Product ${i} has no IMEI at all`)
      return false
    }
  }

  console.log('✅ All products have IMEI')
  return true
}

async function confirmOrder() {
  try {
    console.log('🔍 Confirming order:', trackingData.value?.maHoaDon)

    // Kiểm tra tất cả sản phẩm đã có IMEI chưa
    if (!checkAllProductsHaveImei()) {
      toastRef.value?.warning('Cảnh báo', 'Bạn chưa nhập IMEI cho tất cả sản phẩm. Vui lòng kiểm tra lại!')
      return
    }

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

    // Không cần refresh tracking data vì đã cập nhật thời gian trực tiếp
    // await refreshTrackingData()

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

    // Không cần refresh tracking data vì updateOrderStatusDirectly đã cập nhật đầy đủ
    // await refreshTrackingData()

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

  // Không cho phép thao tác khi đơn hàng đã hoàn thành
  if (trackingData.value.trangThai === 3) {
    toastRef.value?.info('Thông báo', 'Đơn hàng đã hoàn thành, không thể thao tác')
    return
  }

  // Kiểm tra lại điều kiện click (để chắc chắn)
  if (!isStepClickable(stepIndex, step, currentStepIndex)) {
    console.log('❌ Step is not clickable:', step.title)
    toastRef.value?.warning('Cảnh báo', 'Vui lòng hoàn thành các bước trước đó trước')
    return
  }

  // Kiểm tra nếu trạng thái step giống với trạng thái hiện tại
  if (step.status === trackingData.value.trangThai && !step.time) {
    toastRef.value?.warning('Cảnh báo', 'Trạng thái hiện tại đã giống với trạng thái mới')
    return
  }

  console.log('✅ Opening confirm modal for step:', step.title)
  currentStep.value = step
  confirmMessage.value = `Bạn có chắc chắn muốn chuyển đơn hàng sang trạng thái "${step.title}"?`
  confirmCallback.value = async () => {
    await updateOrderStatusDirectly(step.status, step)
  }
  showConfirmModal.value = true
}

async function updateOrderStatusDirectly(newStatus: number, step: any) {
  if (!trackingData.value) return

  isUpdatingStatus.value = true

  // Lưu trạng thái trước đó để xử lý logic đặc biệt
  const previousStatus = trackingData.value.trangThai

  try {
    const { data } = await api.put(`/api/hoa-don/update-status/${trackingData.value.maHoaDon}`, {
      trangThai: newStatus,
      nguoiThucHien: getCurrentStaffName() // Luôn gửi nhân viên đang đăng nhập khi cập nhật
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

    // Đặc biệt: Khi chuyển trạng thái, cần cập nhật thời gian cho bước trước đó (đã hoàn thành)
    if (newStatus === 1 && previousStatus === 0) {
      // Chuyển từ "Chờ xác nhận" sang "Chờ giao hàng" - cập nhật thời gian cho step 0
      if (trackingSteps.value[0]) {
        trackingSteps.value[0].time = timeString
        console.log('✅ Updated time for completed step 0 (Chờ xác nhận):', timeString)
      }
    } else if (newStatus === 2 && previousStatus === 1) {
      // Chuyển từ "Chờ giao hàng" sang "Đang giao" - cập nhật thời gian cho step 1
      if (trackingSteps.value[1]) {
        trackingSteps.value[1].time = timeString
        console.log('✅ Updated time for completed step 1 (Chờ giao hàng):', timeString)
      }
    } else if (newStatus === 3 && previousStatus === 2) {
      // Chuyển từ "Đang giao" sang "Hoàn thành" - cập nhật thời gian cho step 2
      if (trackingSteps.value[2]) {
        trackingSteps.value[2].time = timeString
        console.log('✅ Updated time for completed step 2 (Đang giao):', timeString)
      }
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
    // Thêm log lịch sử ngay lập tức (không cần reload)
    addLocalHistoryLog(previousStatus, newStatus)

    console.log('✅ Cập nhật trạng thái thành công:', data)
    toastRef.value?.success('Thành công', `Đã cập nhật trạng thái đơn hàng sang "${step.title}"`)
    // Đồng bộ lại từ server để lấy bất kỳ xử lý hậu kỳ nào (nếu cần), nhưng không bắt buộc
    // await refreshTrackingData()

    // Không cần refresh tracking data vì đã cập nhật trực tiếp

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
  // Kiểm tra nếu đơn hàng đã hoàn thành hoặc đã hủy
  if (trackingData.value?.trangThai === 3 || trackingData.value?.trangThai === 4) {
    toastRef.value?.info('Thông báo', 'Không thể cập nhật đơn hàng ở trạng thái này')
    return
  }

  showUpdateStatusModal.value = true
  newStatus.value = trackingData.value?.trangThai || 0
}

function closeUpdateStatusModal() {
  showUpdateStatusModal.value = false
  newStatus.value = 0
}

async function updateOrderStatusFromModal() {
  if (!trackingData.value) return

  // Kiểm tra xem trạng thái có thay đổi không
  if (newStatus.value === trackingData.value.trangThai) {
    toastRef.value?.warning('Cảnh báo', 'Bạn chưa thay đổi trạng thái')
    return
  }

  // Kiểm tra nếu đơn hàng đã hoàn thành hoặc đã hủy
  if (trackingData.value?.trangThai === 3 || trackingData.value?.trangThai === 4) {
    toastRef.value?.info('Thông báo', 'Không thể cập nhật đơn hàng ở trạng thái này')
    return
  }

  // Hiển thị confirm modal trước khi cập nhật
  const statusName = getStatusName(newStatus.value)
  confirmMessage.value = `Bạn có chắc chắn muốn cập nhật trạng thái đơn hàng sang "${statusName}"?`
  confirmCallback.value = async () => {
    closeUpdateStatusModal()
    await performUpdateStatus(newStatus.value)
  }
  showConfirmModal.value = true
}

async function performUpdateStatus(status: number) {
  if (!trackingData.value) return

  isUpdatingStatus.value = true

  try {
    const { data } = await api.put(`/api/hoa-don/update-status/${trackingData.value.maHoaDon}`, {
      trangThai: status,
      nguoiThucHien: getCurrentStaffName() // Luôn gửi nhân viên đang đăng nhập khi cập nhật
    })

    trackingData.value.trangThai = status

    console.log('✅ Cập nhật trạng thái thành công:', data)
    toastRef.value?.success('Thành công', 'Đã cập nhật trạng thái đơn hàng')

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
      console.log(`🔍 Product ${index}:`, product)
      console.log(`🔍 Product ${index} id:`, product.id)
      console.log(`🔍 Product ${index} chiTietHoaDonId:`, product.chiTietHoaDonId)
      console.log(`🔍 Product ${index} imeis:`, product.imeis)
      
      // Khởi tạo newImei cho mỗi sản phẩm
      if (!product.newImei) {
        product.newImei = ''
      }
      
      // Lưu index vào product để dễ tìm sau này
      product._index = index
    })
    
    // Gán trực tiếp reference để đảm bảo cập nhật đúng sản phẩm
    // Dùng clone nông để đảm bảo reactivity và tránh tham chiếu lẫn, giữ đúng thứ tự
    selectedProductForImei.value = trackingData.value.danhSachSanPham.map((p, i) => ({ ...p, _index: i }))
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

// Add IMEI to product (open confirm modal first)
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

    // Hiển thị modal xác nhận trước khi thêm
    confirmAddImeiProduct.value = product
    confirmAddImeiImei.value = newImei
    confirmAddImeiMessage.value = `Bạn có chắc chắn muốn thêm IMEI "${newImei}" vào sản phẩm "${product.tenSanPham}"?`
    showConfirmAddImeiModal.value = true

  } catch (error) {
    console.error('Error adding IMEI to product (pre-validate):', error)
    toastRef.value?.error('Lỗi', 'Có lỗi xảy ra khi kiểm tra IMEI')
  }
}

// Thực thi thêm IMEI sau khi người dùng xác nhận
async function performAddImeiToProduct() {
  const product = confirmAddImeiProduct.value
  const newImei = confirmAddImeiImei.value

  if (!product || !newImei) {
    showConfirmAddImeiModal.value = false
    return
  }

  try {
    console.log('Performing add IMEI to product:', newImei)

    // Tìm sản phẩm trong danh sách gốc bằng INDEX để phân biệt các sản phẩm giống nhau
    let targetProduct: any = null
    let targetIndex: number = -1
    
    if (trackingData.value?.danhSachSanPham) {
      // Nếu product có _index đã lưu sẵn, dùng nó (chính xác nhất)
      if (product._index !== undefined && product._index >= 0) {
        targetIndex = product._index
        targetProduct = trackingData.value.danhSachSanPham[targetIndex]
        console.log('✅ Using saved _index:', targetIndex)
      } else {
        // Tìm index của sản phẩm trong danhSachSanPham bằng reference
        const index = trackingData.value.danhSachSanPham.findIndex((p: any, idx: number) => {
          // Ưu tiên tìm bằng object reference trước
          if (p === product) return true
          return false
        })
        
        if (index !== -1) {
          targetIndex = index
          targetProduct = trackingData.value.danhSachSanPham[index]
          console.log('✅ Found product by reference at index:', index)
        } else {
          console.warn('⚠️ Could not find product in danhSachSanPham, using provided product')
          targetProduct = product
          targetIndex = -1
        }
      }
      
      if (targetProduct) {
        console.log('✅ Target product before update:', JSON.stringify(targetProduct, null, 2))
      }
    } else {
      targetProduct = product
      targetIndex = -1
    }

    // Lưu vào backend để không bị mất khi reload
    await saveImeiToDatabase(newImei, targetProduct)

    // Cập nhật UI ngay lập tức - CHỈ cập nhật vào sản phẩm CHÍNH XÁC trong danhSachSanPham
    if (targetIndex !== -1 && trackingData.value?.danhSachSanPham[targetIndex]) {
      const exactProduct = trackingData.value.danhSachSanPham[targetIndex]
      if (!exactProduct.imeis) {
        exactProduct.imeis = []
      }
      // Tránh thêm trùng nếu đã tồn tại
      if (!exactProduct.imeis.some((x: any) => (typeof x === 'object' ? x.imei : x) === newImei)) {
        exactProduct.imeis.push({ imei: newImei, trangThai: 0 })
      }
      console.log('✅ Updated exact product at index', targetIndex)
      console.log('✅ Exact product after update:', JSON.stringify(exactProduct, null, 2))

      // Force reactivity cho modal (do cùng tham chiếu mảng)
      if (selectedProductForImei.value) {
        selectedProductForImei.value = [...trackingData.value.danhSachSanPham]
        console.log('🔄 Forced reactivity for selectedProductForImei')
      }
    }
    
    // KHÔNG cập nhật product trong modal nữa - để Vue reactivity tự động update từ danhSachSanPham
    
    const savedImei = newImei
    product.newImei = ''

    // Đóng confirm modal
    showConfirmAddImeiModal.value = false

    toastRef.value?.success('Thành công', `Đã thêm IMEI ${savedImei}`)

    // Không cần reload để tránh mất dữ liệu, chỉ refresh khi cần
    // await refreshTrackingData()

  } catch (error) {
    console.error('Error adding IMEI to product:', error)
    toastRef.value?.error('Lỗi', 'Có lỗi xảy ra khi thêm IMEI')
  } finally {
    confirmAddImeiProduct.value = null
    confirmAddImeiImei.value = ''
    confirmAddImeiMessage.value = ''
  }
}

// Confirm Add IMEI Modal functions
function cancelAddImei() {
  showConfirmAddImeiModal.value = false
  confirmAddImeiProduct.value = null
  confirmAddImeiImei.value = ''
  confirmAddImeiMessage.value = ''
}

function confirmAddImei() {
  performAddImeiToProduct()
}

// Utility functions
function getProductImage(imagePath: string | null | undefined): string {
  const placeholderSVG = 'data:image/svg+xml;charset=utf-8,%3Csvg xmlns="http://www.w3.org/2000/svg" width="300" height="300"%3E%3Crect width="300" height="300" fill="%23f0f0f0"/%3E%3Ctext x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" fill="%23999" font-family="Arial" font-size="16"%3ENo Image%3C/text%3E%3C/svg%3E'

  if (!imagePath) {
    console.log('⚠️ getProductImage: No imagePath provided')
    return placeholderSVG
  }
  
  // Nếu đã là URL đầy đủ (http/https), trả về trực tiếp
  if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
    console.log('✅ getProductImage: Full URL detected:', imagePath)
    return imagePath
  }
  
  // Xử lý URL tương đối
  // Đảm bảo có dấu / ở đầu
  const normalizedPath = imagePath.startsWith('/') ? imagePath : `/${imagePath}`
  const fullUrl = `${API_BASE_URL}${normalizedPath}`
  
  console.log('🖼️ getProductImage:', {
    original: imagePath,
    normalized: normalizedPath,
    fullUrl: fullUrl
  })
  
  return fullUrl
}

function handleImageError(event: Event) {
  const img = event.target as HTMLImageElement
  const originalSrc = img.getAttribute('data-original-src') || img.src
  console.error('❌ Image load error:', {
    url: originalSrc,
    status: 'File not found or server error',
    suggestion: 'Check if file exists in uploads directory'
  })
  
  // Thay thế bằng placeholder SVG
  const placeholderSVG = 'data:image/svg+xml;charset=utf-8,%3Csvg xmlns="http://www.w3.org/2000/svg" width="300" height="300"%3E%3Crect width="300" height="300" fill="%23f0f0f0"/%3E%3Ctext x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" fill="%23999" font-family="Arial" font-size="16"%3ENo Image%3C/text%3E%3C/svg%3E'
  img.src = placeholderSVG
  img.onerror = null // Prevent infinite loop
}

function handleImageLoad(event: Event) {
  const img = event.target as HTMLImageElement
  console.log('✅ Image loaded successfully:', img.src)
}

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

function formatDateTime(dateString: string): string {
  const date = new Date(dateString)
  return date.toLocaleTimeString('vi-VN') + ' ' + date.toLocaleDateString('vi-VN')
}

function getHistoryActionText(log: any): string {
  const status = log.trangThai
  
  switch (status) {
    case 0: return 'Chuyển đơn sang trạng thái: Chờ xác nhận'
    case 1: return 'Chuyển đơn sang trạng thái: Chờ giao hàng'
    case 2: return 'Chuyển đơn sang trạng thái: Đang giao hàng'
    case 3: return 'Hoàn thành đơn hàng'
    case 4: return 'Hủy đơn hàng'
    default: return log.action || 'Cập nhật thông tin hóa đơn: ' + trackingData.value?.maHoaDon
  }
}

function getStatusText(trangThai: number): string {
  return getStatusName(trangThai)
}

// Local helper: thêm log lịch sử ngay lập tức khi đổi trạng thái (không cần reload)
function addLocalHistoryLog(previousStatus: number, newStatus: number) {
  try {
    if (!trackingData.value) return
    if (!trackingData.value.lichSuTrangThai) {
      trackingData.value.lichSuTrangThai = []
    }
    const nowIso = new Date().toISOString()
    const tenTrangThai = getStatusName(newStatus)
    const moTa = `Trạng thái thay đổi từ ${getStatusName(previousStatus)} sang ${tenTrangThai}`
    const actor = getCurrentStaffName() // Sử dụng nhân viên đang đăng nhập cho log mới
    trackingData.value.lichSuTrangThai.push({
      trangThai: newStatus,
      tenTrangThai,
      thoiGian: nowIso,
      moTa,
      actor
    })
    // Sắp xếp tăng dần theo thời gian để khớp hiển thị hiện tại
    trackingData.value.lichSuTrangThai.sort((a: any, b: any) => new Date(a.thoiGian).getTime() - new Date(b.thoiGian).getTime())
  } catch (e) {
    console.warn('addLocalHistoryLog error', e)
  }
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
          tenSanPham: product.tenSanPham,
          fullProduct: product
        })
      })
    }
    
    // Debug: Log expandedProductList
    console.log('🔍 Print Invoice - expandedProductList:', expandedProductList.value)
    if (expandedProductList.value) {
      expandedProductList.value.forEach((product, index) => {
        console.log(`🔍 Expanded Product ${index}:`, {
          id: product.id,
          maCtsp: product.maCtsp,
          originalProduct: product.originalProduct,
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
            <p style="margin: 5px 0; font-size: 14px;"><strong>Nhân viên:</strong> ${getStaffName()}</p>
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
                    ${product.maCtsp || product.originalProduct?.maCtsp || 'N/A'}
                  </td>
                  <td style="border: 1px solid #ccc; padding: 8px; text-align: left; font-weight: 500;">
                    ${product.tenSanPham || 'N/A'}${product.tenRam ? ' ' + product.tenRam : ''}${product.tenRom ? ' ' + product.tenRom : ''}${product.tenMauSac ? ' ' + product.tenMauSac : ''}
                  </td>
                  <td style="border: 1px solid #ccc; padding: 8px; text-align: center; font-family: monospace; font-size: 12px;">
                    ${(() => {
                      if (product.imei) {
                        return typeof product.imei === 'object' ? product.imei.imei : product.imei
                      }
                      return 'N/A'
                    })()}
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
                    active: !(index < getCurrentStepIndex() || (step.time && index === getCurrentStepIndex()) || (trackingData?.loaiHoaDon === 'BAN_THUONG' || trackingData?.loaiHoaDon === 'NORMAL') || (step as any).isCancelled),
                    completed: index < getCurrentStepIndex() || (step.time && index === getCurrentStepIndex()) || (trackingData?.loaiHoaDon === 'BAN_THUONG' || trackingData?.loaiHoaDon === 'NORMAL'),
                clickable: isStepClickable(index, step, getCurrentStepIndex()),
                'pending-confirmation': step.status === 0 && trackingData?.trangThai === 0 && !step.time,
                'activated': step.status === 0 && step.time && trackingData?.trangThai === 0,
                'isCancelled': (step as any).isCancelled
                  }]"
              :title="`Step ${index}: ${step.title} (status: ${step.status}), Current: ${getCurrentStepIndex()}, Clickable: ${isStepClickable(index, step, getCurrentStepIndex())}`"
              @click="!isStepClickable(index, step, getCurrentStepIndex()) ? null : (handleStepClick(step, index))"
            >
              <div class="step-icon">
                <font-awesome-icon
                  :icon="(step.icon === 'times-circle') ? 'times-circle' : ((index < getCurrentStepIndex() || (step.time && index === getCurrentStepIndex()) || (trackingData?.loaiHoaDon === 'BAN_THUONG' || trackingData?.loaiHoaDon === 'NORMAL')) ? 'check' : step.icon)"
                  v-if="index < getCurrentStepIndex() || index === getCurrentStepIndex() || (trackingData?.loaiHoaDon === 'BAN_THUONG' || trackingData?.loaiHoaDon === 'NORMAL') || (step as any).isCancelled"
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

      <!-- Main Content - Three Row Layout -->
      <div v-if="trackingData" class="main-content-container">
        <!-- Row 1: Order Info + Customer Info -->
        <div class="row-container">
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
              <span>{{ getStaffName() }}</span>
          </div>
        </div>

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
          </div>

        <!-- Row 2: Invoice History + Order Summary -->
        <div class="row-container">
          <div class="info-card">
            <h3>
              <font-awesome-icon icon="clock" class="mr-2" />
              Lịch Sử Hóa Đơn
            </h3>
            <div v-if="trackingData && trackingData.lichSuTrangThai && Array.isArray(trackingData.lichSuTrangThai) && trackingData.lichSuTrangThai.length > 0" class="history-list">
              <div v-for="(log, index) in trackingData.lichSuTrangThai" :key="`history-${index}-${log.thoiGian || index}`" class="history-item">
                <div class="history-dot"></div>
                <div class="history-content">
                  <div class="history-action">{{ getHistoryActionText(log) }}</div>
                  <div class="history-actor">{{ log.maNhanVien || log.actor || log.tenNhanVien || 'Hệ thống' }}</div>
                </div>
                <div class="history-time">{{ formatDateTime(log.thoiGian) }}</div>
              </div>
            </div>
            <div v-else class="no-history">
              <font-awesome-icon icon="history" />
              <p>Chưa có lịch sử hoạt động</p>
              <p v-if="trackingData && !trackingData.lichSuTrangThai" class="text-sm text-gray-500 mt-2">
                (Dữ liệu lịch sử chưa được tải)
              </p>
            </div>
          </div>

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

        <!-- Row 3: Product List (Full Width) -->
        <div class="info-card product-list-container">
          <h3>Danh sách sản phẩm</h3>
          <div v-if="displayProductList && displayProductList.length > 0" class="product-list">
            <div v-for="(product, index) in displayProductList" :key="`${product._stableKey}`" class="product-item">
              <div class="product-image">
                <img 
                  :src="getProductImage(product.hinhAnh)" 
                  :data-original-src="product.hinhAnh"
                  :alt="product.tenSanPham"
                  @error="handleImageError($event)"
                  @load="handleImageLoad($event)"
                />
                </div>
              <div class="product-info">
                <div class="product-name">{{ product.tenSanPham }}</div>
                <div class="product-specs" v-if="product.tenRam || product.tenRom || product.tenMauSac">
                  <span v-if="product.tenRam" class="spec-tag">{{ product.tenRam }}</span>
                  <span v-if="product.tenRom" class="spec-tag">{{ product.tenRom }}</span>
                  <span v-if="product.tenMauSac" class="spec-tag">{{ product.tenMauSac }}</span>
              </div>
                <div class="product-price">{{ formatCurrency(product.gia) }}</div>
            </div>
              <div class="product-actions">
                <div v-if="product.imeis && product.imeis.length > 0" class="imei-info">
                  <div v-for="(imei, imeiIdx) in product.imeis" :key="imeiIdx" class="imei-item">
                    <span>IMEI: {{ typeof imei === 'object' ? imei.imei : imei }}</span>
            </div>
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
          <button v-if="trackingData?.trangThai !== 3 && trackingData?.trangThai !== 4" class="btn-action" @click="openUpdateStatusModal">
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
    <div v-if="showConfirmModal" class="fixed inset-0 bg-white/20 flex items-center justify-center z-[120]" @click="cancelAction">
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
    <div v-if="showImeiModal" class="fixed inset-0 bg-white/20 flex items-center justify-center z-[100]">
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
                  <img :src="getProductImage(product.hinhAnh)" :alt="product.tenSanPham" class="w-16 h-16 object-cover rounded-lg" />
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
                    :disabled="trackingData?.trangThai === 3 || trackingData?.trangThai === 4"
                    class="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 disabled:bg-gray-100 disabled:cursor-not-allowed"
                  />
                  <button
                    v-if="trackingData?.trangThai !== 3 && trackingData?.trangThai !== 4"
                    @click="startProductQRScanner(product)"
                    class="w-10 h-10 bg-purple-500 hover:bg-purple-600 rounded-full transition-colors flex items-center justify-center"
                    title="Quét QR Code"
                  >
                    <img src="/QR.png" alt="QR" class="w-6 h-6" />
                  </button>
                  <button
                    v-if="trackingData?.trangThai !== 3 && trackingData?.trangThai !== 4"
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
                        v-if="trackingData?.trangThai !== 3 && trackingData?.trangThai !== 4"
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
                <img :src="getProductImage(selectedProductForImei.hinhAnh)" :alt="selectedProductForImei.tenSanPham" class="w-16 h-16 object-cover rounded-lg" />
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
                  :disabled="trackingData?.trangThai === 3 || trackingData?.trangThai === 4"
                  class="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 disabled:bg-gray-100 disabled:cursor-not-allowed"
                />
                <button
                  v-if="trackingData?.trangThai !== 3 && trackingData?.trangThai !== 4"
                  @click="startProductQRScanner(selectedProductForImei)"
                  class="w-10 h-10 bg-purple-500 hover:bg-purple-600 rounded-full transition-colors flex items-center justify-center"
                  title="Quét QR Code"
                >
                  <img src="/QR.png" alt="QR" class="w-6 h-6" />
                </button>
                <button
                  v-if="trackingData?.trangThai !== 3 && trackingData?.trangThai !== 4"
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
                      v-if="trackingData?.trangThai !== 3 && trackingData?.trangThai !== 4"
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
    <div v-if="showUpdateStatusModal" class="fixed inset-0 bg-white/20 flex items-center justify-center z-[110]" @click="closeUpdateStatusModal">
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
    <div v-if="showProductQRScannerModal" class="fixed inset-0 bg-white/20 flex items-center justify-center z-[130]" @click="closeProductQRScanner">
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

    <!-- Confirm Add IMEI Modal -->
    <div v-if="showConfirmAddImeiModal" class="confirm-modal fixed inset-0 flex items-center justify-center z-[135]" @click="cancelAddImei">
      <div class="modal-panel" @click.stop>
        <!-- Modal Header -->
        <div class="modal-header">
          <h3 class="modal-title">
            <font-awesome-icon icon="question-circle" />
            Xác nhận thêm IMEI
          </h3>
          <button @click="cancelAddImei" class="btn-close">
            <font-awesome-icon icon="times" />
          </button>
        </div>

        <!-- Modal Body -->
        <div class="modal-body">
          <div class="body-stack">
            <!-- Icon -->
            <div class="icon-wrap">
              <div class="icon-circle">
                <font-awesome-icon icon="exclamation-triangle" />
              </div>
            </div>

            <!-- Message -->
            <div class="message">
              <p class="lead">Bạn có chắc chắn muốn thêm IMEI này?</p>
              <p class="sub">{{ confirmAddImeiMessage }}</p>
            </div>

            <!-- IMEI Info -->
            <div class="imei-box">
              <font-awesome-icon icon="mobile-alt" />
              <div class="imei-text">
                <div class="label">Mã IMEI</div>
                <div class="value">{{ confirmAddImeiImei }}</div>
              </div>
            </div>

            <!-- Product Info -->
            <div class="product-box">
              <div class="label">Sản phẩm</div>
              <div class="name">{{ confirmAddImeiProduct?.tenSanPham }}</div>
            </div>
          </div>
        </div>

        <!-- Modal Footer -->
        <div class="modal-footer">
          <button @click="cancelAddImei" class="btn secondary">Hủy</button>
          <button @click="confirmAddImei" class="btn primary">
            <font-awesome-icon icon="check" />
            <span>Xác nhận thêm</span>
          </button>
        </div>
      </div>
    </div>

    <!-- IMEI Confirmation Modal -->
    <div v-if="showImeiConfirmationModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-[140]" @click="closeImeiConfirmationModal">
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
  background: #f8f9fa;
  /* Đảm bảo không che sidebar */
  width: 100%;
  box-sizing: border-box;
}

.main-content {
  padding: 20px;
  padding-top: 100px;
  width: 100%;
  margin: 0;
  box-sizing: border-box;
  /* Đảm bảo content không che sidebar - sidebar sẽ được render bên ngoài component này */
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

.status-step.isCancelled .step-icon {
  background: #ef4444;
  color: white;
  box-shadow: 0 0 20px rgba(239, 68, 68, 0.5);
  animation: pulse-red 2s infinite;
}

@keyframes pulse-red {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 0 20px rgba(239, 68, 68, 0.5);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 0 0 30px rgba(239, 68, 68, 0.8);
  }
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

/* Main Content Container - Three Row Layout */
.main-content-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
  margin-bottom: 32px;
}

/* Row Container for 2 boxes side by side */
.row-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

/* Product List Container (Full Width) */
.product-list-container {
  width: 100%;
}

.info-card {
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #e2e8f0;
  min-height: 0;
}

/* Row container cards should have equal height */
.row-container .info-card {
  height: 100%;
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
  align-items: flex-start;
  padding: 10px 0;
  border-bottom: 1px solid #f1f5f9;
  min-height: 40px;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item label {
  font-weight: 500;
  color: #6b7280;
  font-size: 14px;
  min-width: 140px;
  flex-shrink: 0;
  line-height: 1.5;
  padding-top: 2px;
}

.info-item span {
  font-weight: 600;
  color: #1f2937;
  font-size: 14px;
  text-align: right;
  flex: 0 0 auto;
  line-height: 1.5;
  word-break: break-word;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  max-width: 100%;
}

.order-code {
  background: #fef3c7;
  color: #92400e;
  padding: 4px 8px;
  border-radius: 6px;
  font-family: monospace;
  display: inline-block;
  width: fit-content;
  white-space: nowrap;
  flex-shrink: 0;
}

.order-type {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  width: fit-content;
  white-space: nowrap;
  flex-shrink: 0;
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
  width: fit-content;
  white-space: nowrap;
  flex-shrink: 0;
}

.shipping-method {
  font-weight: 500;
  color: #059669;
  padding: 4px 8px;
  background: #ecfdf5;
  border-radius: 6px;
  display: inline-block;
  width: fit-content;
  white-space: nowrap;
  flex-shrink: 0;
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
  gap: 6px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.spec-tag {
  background: #f3f4f6;
  color: #374151;
  padding: 3px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  border: 1px solid #e5e7eb;
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
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 200px;
}

.imei-item {
  padding: 8px 12px;
  background-color: #fef3c7;
  border-left: 3px solid #f97316;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  font-family: monospace;
  color: #92400e;
}

.imei-item span {
  display: block;
  white-space: nowrap;
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
.status-pending,
.status-confirmed,
.status-waiting,
.status-shipping,
.status-completed,
.status-cancelled {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: 6px;
  width: fit-content;
  white-space: nowrap;
  flex-shrink: 0;
}

.status-pending {
  color: #f59e0b;
  background: #fef3c7;
}

.status-confirmed {
  color: #3b82f6;
  background: #dbeafe;
}

.status-waiting {
  color: #8b5cf6;
  background: #e9d5ff;
}

.status-shipping {
  color: #06b6d4;
  background: #cffafe;
}

.status-completed {
  color: #10b981;
  background: #d1fae5;
}

.status-cancelled {
  color: #ef4444;
  background: #fee2e2;
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
    gap: 16px;
  }
  
  .row-container {
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

  .product-specs {
    justify-content: center;
  }

  .spec-tag {
    font-size: 11px;
    padding: 2px 6px;
  }

  .btn-primary {
    padding: 10px 20px;
    font-size: 13px;
  }
}

/* Invoice History Styles */
.history-list {
  position: relative;
}

.history-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  position: relative;
}

.history-item:not(:last-child) {
  margin-bottom: 8px;
}

/* Green dot */
.history-dot {
  width: 12px;
  height: 12px;
  background: #10b981;
  border-radius: 50%;
  flex-shrink: 0;
  position: relative;
  z-index: 2;
}

/* Vertical line connecting dots */
.history-item:not(:last-child)::after {
  content: '';
  position: absolute;
  left: 5px;
  top: 28px;
  width: 2px;
  height: calc(100% + 8px);
  background: #10b981;
  z-index: 1;
}

.history-content {
  flex: 1;
  margin-left: 16px;
}

.history-action {
  font-weight: 500;
  color: #1f2937;
  font-size: 14px;
  margin-bottom: 2px;
}

.history-actor {
  font-size: 12px;
  color: #6b7280;
}

.history-time {
  font-size: 12px;
  color: #6b7280;
  text-align: right;
  white-space: nowrap;
  min-width: 150px;
}

.no-history {
  text-align: center;
  padding: 40px 20px;
  color: #6b7280;
}

.no-history svg {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.no-history p {
  font-size: 14px;
  margin: 0;
}

/* Confirm Add IMEI Modal Styles */
.confirm-modal {
  background: rgba(0, 0, 0, 0.45);
}

.modal-panel {
  width: 90%;
  max-width: 560px;
  max-height: 90vh;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.2);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #f3e8ff;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 22px;
  background: linear-gradient(90deg, #fff7ed, #fffbeb);
  border-bottom: 1px solid #fde68a;
}

.modal-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 700;
  color: #7c2d12;
}

.btn-close {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #fff7ed;
  color: #a16207;
  border: 1px solid #fde68a;
}

.modal-body {
  padding: 20px 22px;
  overflow-y: auto;
}

.body-stack { gap: 16px; display: flex; flex-direction: column; }
.icon-wrap { display: flex; justify-content: center; }
.icon-circle {
  width: 64px;
  height: 64px;
  border-radius: 9999px;
  background: #ffedd5;
  color: #ea580c;
  display: grid;
  place-items: center;
  font-size: 28px;
}

.message { text-align: center; }
.message .lead { font-size: 18px; font-weight: 600; color: #1f2937; margin-bottom: 4px; }
.message .sub { font-size: 14px; color: #6b7280; }

.imei-box {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fff7ed;
  border: 1px solid #fde68a;
  padding: 12px 14px;
  border-radius: 12px;
  color: #c2410c;
}
.imei-box .imei-text .label { font-size: 12px; color: #b45309; }
.imei-box .imei-text .value { font-family: monospace; font-weight: 700; font-size: 16px; color: #7c2d12; }

.product-box {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  padding: 12px 14px;
  border-radius: 12px;
}
.product-box .label { font-size: 12px; color: #6b7280; margin-bottom: 4px; }
.product-box .name { font-size: 15px; font-weight: 600; color: #1f2937; }

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 22px;
  background: #fafafa;
  border-top: 1px solid #e5e7eb;
}

.btn { height: 40px; padding: 0 16px; border-radius: 10px; font-weight: 600; display: inline-flex; align-items: center; gap: 8px; }
.btn.primary { background: linear-gradient(135deg, #fb923c, #f97316); color: #fff; border: 1px solid #fb923c; }
.btn.primary:hover { background: linear-gradient(135deg, #f97316, #ea580c); }
.btn.secondary { background: #e5e7eb; color: #374151; border: 1px solid #d1d5db; }
.btn.secondary:hover { background: #d1d5db; }
</style>

