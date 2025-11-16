<template>
  <div class="page dark-mode-transition">
    <PosHeader />
    
    <div class="content">
      <Toast ref="toastRef" />

      <!-- Filter Section -->
      <div class="filter-section">
        <div class="filter-header">
          <div class="filter-title">
            <h3>Bộ lọc nâng cao</h3>
            <p>Tìm kiếm và lọc phiếu bảo hành theo tiêu chí</p>
          </div>
          <div class="filter-actions">
            <button class="btn-clear-filters" @click="clearAllFilters">
              Xóa bộ lọc
            </button>
          </div>
        </div>
        <div class="filter-row">
          <div class="filter-group">
            <label>Tìm kiếm:</label>
            <input 
              type="text" 
              v-model="searchText" 
              placeholder="Tìm theo mã phiếu, tên KH, SĐT, IMEI..."
              @input="applyFilters"
              class="search-input"
            />
          </div>
          <div class="filter-group">
            <label>Trạng thái:</label>
            <select v-model="statusFilter" @change="applyFilters" class="filter-select">
              <option value="">Tất cả trạng thái</option>
              <option value="0">Mới tiếp nhận</option>
              <option value="1">Đủ điều kiện</option>
              <option value="2">Không đủ điều kiện</option>
              <option value="3">Đang sửa chữa nội bộ</option>
              <option value="4">Đã gửi TTBH hãng</option>
              <option value="5">Đã nhận từ TTBH</option>
              <option value="6">Đang kiểm tra QC</option>
              <option value="7">Đã sửa xong</option>
              <option value="8">Đã trả khách</option>
              <option value="9">Hoàn tất</option>
            </select>
          </div>
          <div class="filter-group">
            <label>Ngày nhận từ:</label>
            <input 
              type="date" 
              v-model="ngayNhanTu" 
              @change="applyFilters"
              class="filter-select"
            />
          </div>
          <div class="filter-group">
            <label>Ngày nhận đến:</label>
            <input 
              type="date" 
              v-model="ngayNhanDen" 
              @change="applyFilters"
              class="filter-select"
            />
          </div>
        </div>
      </div>

      <!-- Add Button -->
      <div class="add-customer-section">
        <div class="add-buttons">
          <button class="btn-add-customer" @click="tiepNhanMoi">
            <FontAwesomeIcon :icon="['fas', 'plus-circle']" />
            Tiếp nhận bảo hành mới
          </button>
        </div>
      </div>

      <!-- Main Content Area -->
      <div class="main-content">
        <!-- Table Header Section -->
        <div class="table-header">
          <div class="table-title">
            <h2>Danh sách Phiếu Bảo Hành</h2>
            <span class="item-count">{{ totalItems }} phiếu bảo hành</span>
          </div>
          <div class="table-actions">
            <div class="items-per-page">
              <label>Hiển thị:</label>
              <select v-model="itemsPerPage" @change="applyFilters" class="page-size-select">
                <option value="5">5 mục / trang</option>
                <option value="10">10 mục / trang</option>
                <option value="20">20 mục / trang</option>
                <option value="50">50 mục / trang</option>
              </select>
            </div>
          </div>
        </div>

        <div class="table-container">
          <table>
            <thead>
              <tr>
                <th>STT</th>
                <th>Mã phiếu</th>
                <th>Tên khách hàng</th>
                <th>Số điện thoại</th>
                <th>Tên sản phẩm</th>
                <th>IMEI/Serial</th>
                <th>Ngày nhận</th>
                <th>Ngày hẹn trả</th>
                <th>Trạng thái</th>
                <th>Hướng xử lý</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                <td colspan="11" class="text-center">
                  <div class="loading-spinner">Đang tải dữ liệu...</div>
                </td>
              </tr>
              <tr v-else-if="paginatedPhieuBaoHanh.length === 0">
                <td colspan="11" class="text-center">
                  <div class="empty-state">Không có dữ liệu</div>
                </td>
              </tr>
              <tr v-else v-for="(phieu, index) in paginatedPhieuBaoHanh" :key="phieu.id">
                <td>{{ startItem + index }}</td>
                <td>
                  <span class="ma-phieu">{{ phieu.maPhieu }}</span>
                </td>
                <td>{{ phieu.tenKhachHang }}</td>
                <td>{{ phieu.soDienThoai }}</td>
                <td>{{ phieu.tenSanPham }}</td>
                <td>{{ phieu.imeiSerial || '-' }}</td>
                <td>{{ formatDate(phieu.ngayNhan) }}</td>
                <td>{{ formatDate(phieu.ngayHenTraDuKien) }}</td>
                <td>
                  <span :class="['status-badge', getStatusClass(phieu.trangThai)]">
                    {{ getStatusText(phieu.trangThai) }}
                  </span>
                </td>
                <td>
                  <span v-if="phieu.huongXuLy" class="huong-xu-ly-badge">
                    {{ phieu.huongXuLy === 'SUA_TAI_CUA_HANG' ? 'Sửa tại CH' : 'Gửi TTBH' }}
                  </span>
                  <span v-else>-</span>
                </td>
                <td>
                  <div class="action-buttons">
                    <button class="btn-view" @click="viewDetail(phieu.id)" title="Xem chi tiết">
                      <FontAwesomeIcon :icon="['fas', 'eye']" />
                    </button>
                    <button 
                      v-if="canProcess(phieu.trangThai)" 
                      class="btn-process" 
                      @click="processPhieu(phieu.id)"
                      title="Xử lý"
                    >
                      <FontAwesomeIcon :icon="['fas', 'cog']" />
                    </button>
                    <button 
                      v-if="phieu.trangThai === 7" 
                      class="btn-return" 
                      @click="traMay(phieu.id)"
                      title="Trả máy"
                    >
                      <FontAwesomeIcon :icon="['fas', 'hand-holding']" />
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination -->
        <div class="pagination" v-if="totalPages > 1">
          <button 
            class="pagination-btn" 
            :disabled="currentPage === 1" 
            @click="currentPage = 1"
          >
            <FontAwesomeIcon :icon="['fas', 'angles-left']" />
          </button>
          <button 
            class="pagination-btn" 
            :disabled="currentPage === 1" 
            @click="currentPage--"
          >
            <FontAwesomeIcon :icon="['fas', 'chevron-left']" />
          </button>
          
          <span class="pagination-info">
            Trang {{ currentPage }} / {{ totalPages }}
          </span>
          
          <button 
            class="pagination-btn" 
            :disabled="currentPage === totalPages" 
            @click="currentPage++"
          >
            <FontAwesomeIcon :icon="['fas', 'chevron-right']" />
          </button>
          <button 
            class="pagination-btn" 
            :disabled="currentPage === totalPages" 
            @click="currentPage = totalPages"
          >
            <FontAwesomeIcon :icon="['fas', 'angles-right']" />
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { FontAwesomeIcon } from '@/plugins/fontawesome'
import PosHeader from '@/components/PosHeader.vue'
import Toast from '@/components/Toast.vue'
import api from '@/services/api'

const router = useRouter()
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

interface PhieuBaoHanh {
  id: number
  maPhieu: string
  tenKhachHang: string
  soDienThoai: string
  tenSanPham: string
  imeiSerial?: string
  ngayNhan?: string
  ngayHenTraDuKien?: string
  ngayTraThucTe?: string
  trangThai: number
  trangThaiText?: string
  huongXuLy?: string
  duDieuKienBaoHanh?: boolean
  chiPhiSuaChua?: number
  khachDaThanhToan?: number
}

const phieuBaoHanhList = ref<PhieuBaoHanh[]>([])
const loading = ref(false)

// Filter states
const searchText = ref('')
const statusFilter = ref('')
const ngayNhanTu = ref('')
const ngayNhanDen = ref('')

// Pagination states
const currentPage = ref(1)
const itemsPerPage = ref(10)
const totalItems = ref(0)

// Computed properties
const totalPages = computed(() => Math.ceil(totalItems.value / itemsPerPage.value))
const startItem = computed(() => (currentPage.value - 1) * itemsPerPage.value + 1)

const filteredPhieuBaoHanh = computed(() => {
  let filtered = [...phieuBaoHanhList.value]

  // Search filter
  if (searchText.value.trim() !== '') {
    const search = searchText.value.toLowerCase()
    filtered = filtered.filter(phieu =>
      phieu.maPhieu?.toLowerCase().includes(search) ||
      phieu.tenKhachHang?.toLowerCase().includes(search) ||
      phieu.soDienThoai?.toLowerCase().includes(search) ||
      phieu.tenSanPham?.toLowerCase().includes(search) ||
      phieu.imeiSerial?.toLowerCase().includes(search)
    )
  }

  // Status filter
  if (statusFilter.value !== '') {
    filtered = filtered.filter(phieu => phieu.trangThai.toString() === statusFilter.value)
  }

  // Date filters
  if (ngayNhanTu.value) {
    filtered = filtered.filter(phieu => {
      if (!phieu.ngayNhan) return false
      return phieu.ngayNhan >= ngayNhanTu.value
    })
  }
  if (ngayNhanDen.value) {
    filtered = filtered.filter(phieu => {
      if (!phieu.ngayNhan) return false
      return phieu.ngayNhan <= ngayNhanDen.value
    })
  }

  totalItems.value = filtered.length
  return filtered
})

const paginatedPhieuBaoHanh = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return filteredPhieuBaoHanh.value.slice(start, end)
})

// Methods
async function loadPhieuBaoHanh() {
  loading.value = true
  try {
    const response = await api.get('/api/bao-hanh')
    phieuBaoHanhList.value = response.data
    toastRef.value?.success('Thành công', 'Tải danh sách phiếu bảo hành thành công')
  } catch (error: any) {
    console.error('Error loading phiếu bảo hành:', error)
    toastRef.value?.error('Lỗi', 'Không thể tải danh sách phiếu bảo hành: ' + (error.message || 'Unknown error'))
  } finally {
    loading.value = false
  }
}

function applyFilters() {
  currentPage.value = 1 // Reset to first page when filters change
}

function clearAllFilters() {
  searchText.value = ''
  statusFilter.value = ''
  ngayNhanTu.value = ''
  ngayNhanDen.value = ''
  currentPage.value = 1
}

function formatDate(dateString?: string): string {
  if (!dateString) return '-'
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('vi-VN')
  } catch {
    return dateString
  }
}

function getStatusText(trangThai: number): string {
  const statusMap: Record<number, string> = {
    0: 'Mới tiếp nhận',
    1: 'Đủ điều kiện',
    2: 'Không đủ điều kiện',
    3: 'Đang sửa chữa nội bộ',
    4: 'Đã gửi TTBH hãng',
    5: 'Đã nhận từ TTBH',
    6: 'Đang kiểm tra QC',
    7: 'Đã sửa xong',
    8: 'Đã trả khách',
    9: 'Hoàn tất'
  }
  return statusMap[trangThai] || 'Không xác định'
}

function getStatusClass(trangThai: number): string {
  const classMap: Record<number, string> = {
    0: 'status-new',
    1: 'status-eligible',
    2: 'status-not-eligible',
    3: 'status-repairing',
    4: 'status-sent',
    5: 'status-received',
    6: 'status-qc',
    7: 'status-done',
    8: 'status-returned',
    9: 'status-completed'
  }
  return classMap[trangThai] || 'status-unknown'
}

function canProcess(trangThai: number): boolean {
  // Cho phép xử lý khi chưa hoàn tất (trạng thái 0-7)
  return trangThai >= 0 && trangThai < 8
}

function viewDetail(id: number) {
  router.push(`/bao-hanh/chi-tiet/${id}`)
}

function processPhieu(id: number) {
  router.push(`/bao-hanh/chi-tiet/${id}`)
}

function traMay(id: number) {
  router.push(`/bao-hanh/tra-may/${id}`)
}

function tiepNhanMoi() {
  router.push('/bao-hanh/tiep-nhan')
}

onMounted(() => {
  loadPhieuBaoHanh()
})
</script>

<style scoped>
.page {
  padding: 20px;
  background-color: var(--bg-color, #f5f5f5);
  min-height: 100vh;
}

.filter-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-title h3 {
  margin: 0 0 5px 0;
  color: #333;
}

.filter-title p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.filter-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
}

.filter-group {
  display: flex;
  flex-direction: column;
}

.filter-group label {
  margin-bottom: 5px;
  font-weight: 500;
  color: #555;
}

.search-input,
.filter-select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.add-customer-section {
  margin-bottom: 20px;
}

.add-buttons {
  display: flex;
  gap: 10px;
}

.btn-add-customer,
.btn-clear-filters {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s;
}

.btn-add-customer {
  background-color: #4CAF50;
  color: white;
}

.btn-add-customer:hover {
  background-color: #45a049;
}

.btn-clear-filters {
  background-color: #f44336;
  color: white;
}

.btn-clear-filters:hover {
  background-color: #da190b;
}

.main-content {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.table-title h2 {
  margin: 0 0 5px 0;
  color: #333;
}

.item-count {
  color: #666;
  font-size: 14px;
}

.table-container {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

thead {
  background-color: #f8f9fa;
}

th {
  padding: 12px;
  text-align: left;
  font-weight: 600;
  color: #333;
  border-bottom: 2px solid #dee2e6;
}

td {
  padding: 12px;
  border-bottom: 1px solid #dee2e6;
}

.ma-phieu {
  font-weight: 600;
  color: #2196F3;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  display: inline-block;
}

.status-new { background-color: #E3F2FD; color: #1976D2; }
.status-eligible { background-color: #E8F5E9; color: #388E3C; }
.status-not-eligible { background-color: #FFEBEE; color: #C62828; }
.status-repairing { background-color: #FFF3E0; color: #F57C00; }
.status-sent { background-color: #E1BEE7; color: #7B1FA2; }
.status-received { background-color: #BBDEFB; color: #1565C0; }
.status-qc { background-color: #F3E5F5; color: #6A1B9A; }
.status-done { background-color: #C8E6C9; color: #2E7D32; }
.status-returned { background-color: #B2DFDB; color: #00695C; }
.status-completed { background-color: #A5D6A7; color: #1B5E20; }

.huong-xu-ly-badge {
  padding: 4px 8px;
  background-color: #E3F2FD;
  color: #1976D2;
  border-radius: 4px;
  font-size: 12px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.btn-view,
.btn-process,
.btn-return {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  color: white;
  transition: all 0.3s;
}

.btn-view {
  background-color: #2196F3;
}

.btn-view:hover {
  background-color: #1976D2;
}

.btn-process {
  background-color: #FF9800;
}

.btn-process:hover {
  background-color: #F57C00;
}

.btn-return {
  background-color: #4CAF50;
}

.btn-return:hover {
  background-color: #45a049;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  margin-top: 20px;
}

.pagination-btn {
  padding: 8px 12px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.pagination-btn:hover:not(:disabled) {
  background-color: #f0f0f0;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-info {
  padding: 0 15px;
  color: #666;
}

.text-center {
  text-align: center;
}

.loading-spinner,
.empty-state {
  padding: 40px;
  color: #999;
}
</style>

