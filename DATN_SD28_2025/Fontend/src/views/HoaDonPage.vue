<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '@/services/api'
import Toast from '@/components/Toast.vue'
import PosHeader from '@/components/PosHeader.vue'
import * as XLSX from 'xlsx'

const router = useRouter()
const route = useRoute()

interface HoaDon {
  id: number
  maHoaDon: string
  tenKhachHang?: string
  soDienThoai?: string
  email?: string
  loaiHoaDon: string
  tongTien: number
  tongTienSauGiam?: number
  trangThai: number
  ngayTao: string
  phieuGiamGiaId?: number
  ghiChu?: string
  phuongThucThanhToan?: string
  phiVanChuyen?: number
}

// Reactive data
const activeTab = ref('list')
const hoaDons = ref<HoaDon[]>([])
const loading = ref(false)
const toastRef = ref(null)

const showDetailsModal = ref(false)
const selectedHoaDon = ref<HoaDon | null>(null)
const currentPage = ref(1)
const pageSize = ref(10)
const totalPages = ref(0)
const totalElements = ref(0)
const updatingStatus = ref<number | null>(null)
const isUpdatingStatus = ref(false)

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
      case 'ONLINE':
        loaiName = 'Đơn online'
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
  // Điều hướng đến trang chi tiết đơn hàng
  router.push({
    path: '/hoa-don/detail',
    query: { code: hoaDon.maHoaDon }
  })
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

    console.log('✅ Cập nhật trạng thái thành công:', data)
    toastRef.value?.success('Thành công', 'Cập nhật trạng thái thành công!')

  } catch (error) {
    console.error('❌ Lỗi khi cập nhật trạng thái:', error)
    toastRef.value?.error('Lỗi', 'Có lỗi xảy ra khi cập nhật trạng thái đơn hàng')
  } finally {
    updatingStatus.value = null
  }
}

function getStatusName(trangThai: number): string {
  const status = statusOptions.find(s => s.value === trangThai)
  return status ? status.label : 'Không xác định'
}

function getStatusClass(trangThai: number): string {
  const status = statusOptions.find(s => s.value === trangThai)
  return status ? `status-${trangThai}` : 'status-unknown'
}

function getStatusColor(trangThai: number): string {
  const status = statusOptions.find(s => s.value === trangThai)
  return status ? status.color : '#6c757d'
}

function getStatusIcon(trangThai: number): string {
  const iconMap: Record<number, string> = {
    0: 'clock',           // Chờ xác nhận
    1: 'shipping-fast',   // Đang giao hàng
    2: 'truck',           // Đã giao hàng
    3: 'check-circle',    // Đã thanh toán
    4: 'sync',            // Hoàn hàng
    5: 'times-circle'     // Hủy
  }
  return iconMap[trangThai] || 'question-circle'
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

function getStatusBadgeClass(trangThai: number): string {
  const badgeMap: Record<number, string> = {
    0: 'badge-pending',     // Chờ xác nhận
    1: 'badge-paid',        // Đã thanh toán chờ xác nhận
    2: 'badge-shipping',    // Chờ giao hàng
    3: 'badge-delivering',  // Đang giao
    4: 'badge-completed',   // Hoàn thành
    5: 'badge-cancelled'    // Đã hủy
  }
  return badgeMap[trangThai] || 'badge-unknown'
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
        (hd.loaiHoaDon === 'BAN_ONLINE' || hd.loaiHoaDon === 'DELIVERY' || hd.loaiHoaDon === 'ONLINE') ? 'Đơn online' : 'Bán tại quầy',
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

onMounted(() => {
  loadHoaDons()
})

// Cleanup khi component bị unmount
onBeforeUnmount(() => {
  // Cleanup nếu cần
})
</script>

<template>
  <div class="hoa-don-page">
    <PosHeader />


    <main class="main-content">
      

      <!-- Tab Content -->
      <div class="tab-content">
        <!-- Danh sách hóa đơn -->
        <div class="tab-panel">
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
                      <option value="BAN_ONLINE">Đơn online</option>
                      <option value="ONLINE">Đơn online</option>
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
                        <span :class="['order-type', (hd.loaiHoaDon === 'BAN_ONLINE' || hd.loaiHoaDon === 'DELIVERY' || hd.loaiHoaDon === 'ONLINE') ? 'online' : 'normal']">
                          {{ (hd.loaiHoaDon === 'BAN_ONLINE' || hd.loaiHoaDon === 'DELIVERY' || hd.loaiHoaDon === 'ONLINE') ? 'Đơn online' : 'Bán tại quầy' }}
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
                  <option value="10" selected>10</option>
                  <option value="20">20</option>
                  <option value="50">50</option>
                  <option value="100">100</option>
                </select>
                <span>hóa đơn/trang</span>
              </div>

              <!-- Pagination Info -->
              <div class="pagination-info">
                <span>Hiển thị {{ Math.min((currentPage - 1) * pageSize + 1, totalElements) }} - {{ Math.min(currentPage * pageSize, totalElements) }} trong tổng số {{ totalElements}} hóa đơn</span>
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
                <span :class="['order-type', (selectedHoaDon.loaiHoaDon === 'BAN_ONLINE' || selectedHoaDon.loaiHoaDon === 'DELIVERY' || selectedHoaDon.loaiHoaDon === 'ONLINE') ? 'online' : 'normal']">
                  {{ (selectedHoaDon.loaiHoaDon === 'BAN_ONLINE' || selectedHoaDon.loaiHoaDon === 'DELIVERY' || selectedHoaDon.loaiHoaDon === 'ONLINE') ? 'Đơn online' : 'Bán tại quầy' }}
                </span>
              </div>
              <div class="detail-item">
                <label>Phương thức thanh toán:</label>
                <span class="payment-method">
                  {{ getPaymentMethodName(selectedHoaDon.phuongThucThanhToan) }}
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
              <div class="detail-item">
                <label>Email:</label>
                <span>{{ selectedHoaDon.email || 'Không có' }}</span>
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
              <div class="detail-item" v-if="selectedHoaDon.phiVanChuyen && selectedHoaDon.phiVanChuyen > 0">
                <label>Phí vận chuyển:</label>
                <span class="amount">{{ formatCurrency(selectedHoaDon.phiVanChuyen) }}</span>
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
  justify-content: center;
  width: 100%;
  margin-top: 20px;
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

/* Đặc biệt cho input type="date" */
input[type="date"].filter-input {
  -webkit-appearance: none;
  -moz-appearance: none;
  appearance: none;
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

/* Điều chỉnh độ rộng cột */
table th:nth-child(1),
table td:nth-child(1) {
  width: 60px;
  min-width: 60px;
  max-width: 60px;
  text-align: center;
}

table th:nth-child(2),
table td:nth-child(2) {
  width: 140px;
  min-width: 140px;
  max-width: 140px;
  text-align: center;
}

table th:nth-child(3),
table td:nth-child(3) {
  width: 180px;
  min-width: 180px;
}

table th:nth-child(4),
table td:nth-child(4) {
  width: 120px;
  min-width: 120px;
  max-width: 120px;
}

table th:nth-child(5),
table td:nth-child(5) {
  width: 100px;
  min-width: 100px;
  max-width: 100px;
  text-align: center;
}

table th:nth-child(6),
table td:nth-child(6) {
  width: 130px;
  min-width: 130px;
  text-align: right;
}

table th:nth-child(7),
table td:nth-child(7) {
  width: 120px;
  min-width: 120px;
  text-align: center;
}

table th:nth-child(8),
table td:nth-child(8) {
  width: 110px;
  min-width: 110px;
  text-align: center;
}

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

.payment-method {
  font-weight: 500;
  color: #374151;
  padding: 4px 8px;
  background: #f3f4f6;
  border-radius: 6px;
  display: inline-block;
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

.btn-view:hover {
  background: #b3e5fc;
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

/* Responsive */
@media (max-width: 768px) {
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

  .modal-content {
    width: 95%;
    margin: 10px;
  }

  .modal-content.large {
    width: 95%;
  }
}
</style>
