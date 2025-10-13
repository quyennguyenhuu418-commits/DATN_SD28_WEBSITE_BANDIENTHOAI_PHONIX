<template>
  <div class="page">
    <div class="header">
      <h1>Quản lý Đợt Giảm Giá</h1>
      <button class="btn-primary" @click="openForm()">Thêm Đợt Giảm Giá</button>
    </div>

    <!-- Confirm Modal -->
    <ConfirmModal
      :show="showConfirmModal"
      :title="confirmTitle"
      :message="confirmMessage"
      @confirm="handleConfirm"
      @cancel="handleCancel"
    />

    <!-- Form Modal -->
    <FormModal
      :show="showForm"
      :title="editingKhuyenMai ? 'Sửa Đợt Giảm Giá' : 'Thêm Đợt Giảm Giá'"
      :fields="khuyenMaiFields"
      :initial-data="editingKhuyenMai ? {
        maKhuyenMai: editingKhuyenMai.maKhuyenMai,
        tenKhuyenMai: editingKhuyenMai.tenKhuyenMai,
        moTa: editingKhuyenMai.moTa || '',
        mucDoUuTien: editingKhuyenMai.mucDoUuTien,
        phanTramGiam: editingKhuyenMai.phanTramGiam,
        giamToiDa: editingKhuyenMai.giamToiDa,
        ngayBatDau: editingKhuyenMai.ngayBatDau?.split('T')[0],
        ngayKetThuc: editingKhuyenMai.ngayKetThuc?.split('T')[0],
        trangThai: editingKhuyenMai.trangThai
      } : undefined"
      @submit="handleFormSubmit"
      @cancel="showForm = false"
    />

    <Toast ref="toastRef" />

    <!-- Filter Section -->
    <div class="filter-section">
      <div class="filter-group">
        <label>Trạng thái:</label>
        <select v-model="statusFilter" @change="applyFilters">
          <option value="">Tất cả</option>
          <option value="1">Hoạt động</option>
          <option value="0">Không hoạt động</option>
        </select>
      </div>
      <div class="filter-group">
        <label>Tìm kiếm:</label>
        <input 
          type="text" 
          v-model="searchText" 
          placeholder="Tìm theo tên hoặc mã..."
          @input="applyFilters"
        />
      </div>
    </div>

    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>STT</th>
            <th>Mã Khuyến Mãi</th>
            <th>Tên Khuyến Mãi</th>
            <th>Mức Độ Ưu Tiên</th>
            <th>Phần Trăm Giảm</th>
            <th>Giảm Tối Đa</th>
            <th>Ngày Bắt Đầu</th>
            <th>Ngày Kết Thúc</th>
            <th>Trạng Thái</th>
            <th>Thao Tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(khuyenMai, index) in filteredKhuyenMais" :key="khuyenMai.id">
            <td>{{ index + 1 }}</td>
            <td>{{ khuyenMai.maKhuyenMai }}</td>
            <td>{{ khuyenMai.tenKhuyenMai }}</td>
            <td>
              <span :class="getPriorityClass(khuyenMai.mucDoUuTien)">
                {{ getPriorityLabel(khuyenMai.mucDoUuTien) }}
              </span>
            </td>
            <td>{{ khuyenMai.phanTramGiam }}%</td>
            <td>{{ formatCurrency(khuyenMai.giamToiDa) }}</td>
            <td>{{ formatDateTime(khuyenMai.ngayBatDau) }}</td>
            <td>{{ formatDateTime(khuyenMai.ngayKetThuc) }}</td>
            <td>
              <span :class="getStatusClass(khuyenMai)">
                {{ getStatusLabel(khuyenMai) }}
              </span>
            </td>
            <td>
              <button class="btn-edit" @click="openForm(khuyenMai)">Sửa</button>
              <button 
                :class="khuyenMai.trangThai === 1 ? 'btn-disable' : 'btn-enable'"
                @click="toggleStatus(khuyenMai.id)"
              >
                {{ khuyenMai.trangThai === 1 ? 'Vô hiệu' : 'Kích hoạt' }}
              </button>
              <button class="btn-delete" @click="deleteKhuyenMai(khuyenMai.id)">Xóa</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import api from '@/services/api'
import ConfirmModal from '@/components/ConfirmModal.vue'
import FormModal from '@/components/FormModal.vue'
import Toast from '@/components/Toast.vue'

interface KhuyenMai {
  id: number
  maKhuyenMai: string
  tenKhuyenMai: string
  moTa?: string
  mucDoUuTien: number
  phanTramGiam: number
  giamToiDa: number
  ngayBatDau: string
  ngayKetThuc: string
  trangThai: number
  ngayTao?: string
  ngayCapNhat?: string
  nguoiTao?: string
  nguoiCapNhat?: string
}

const khuyenMais = ref<KhuyenMai[]>([])
const loading = ref(false)
const showForm = ref(false)
const editingKhuyenMai = ref<KhuyenMai | null>(null)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

// Confirm modal state
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const pendingAction = ref<(() => void) | null>(null)

// Filter states
const statusFilter = ref('')
const searchText = ref('')

const khuyenMaiFields = [
  { key: 'maKhuyenMai', label: 'Mã Khuyến Mãi', type: 'text' as const, placeholder: 'Để trống để tự động tạo' },
  { key: 'tenKhuyenMai', label: 'Tên Khuyến Mãi', type: 'text' as const, required: true },
  { key: 'moTa', label: 'Mô Tả', type: 'textarea' as const },
  { key: 'mucDoUuTien', label: 'Mức Độ Ưu Tiên', type: 'select' as const, required: true, options: [
    { value: '1', label: 'Thấp' },
    { value: '2', label: 'Trung bình' },
    { value: '3', label: 'Cao' },
    { value: '4', label: 'Rất cao' },
    { value: '5', label: 'Khẩn cấp' }
  ]},
  { key: 'phanTramGiam', label: 'Phần Trăm Giảm (%)', type: 'number' as const, required: true },
  { key: 'giamToiDa', label: 'Giảm Tối Đa (VND)', type: 'number' as const, required: true },
  { key: 'ngayBatDau', label: 'Ngày Bắt Đầu', type: 'date' as const, required: true },
  { key: 'ngayKetThuc', label: 'Ngày Kết Thúc', type: 'date' as const, required: true },
  { key: 'trangThai', label: 'Trạng thái hoạt động', type: 'checkbox' as const }
]

// Computed property for filtered khuyenMais
const filteredKhuyenMais = computed(() => {
  let filtered = khuyenMais.value

  if (statusFilter.value !== '') {
    filtered = filtered.filter(k => k.trangThai.toString() === statusFilter.value)
  }

  if (searchText.value.trim() !== '') {
    const search = searchText.value.toLowerCase()
    filtered = filtered.filter(k => 
      k.tenKhuyenMai.toLowerCase().includes(search) ||
      k.maKhuyenMai.toLowerCase().includes(search)
    )
  }

  return filtered
})

async function loadKhuyenMais() {
  loading.value = true
  try {
    const { data } = await api.get<KhuyenMai[]>('/api/khuyen-mai')
    khuyenMais.value = data
  } catch (error) {
    console.error('Lỗi khi tải danh sách khuyến mãi:', error)
    toastRef.value?.error('Lỗi', 'Không thể tải danh sách đợt giảm giá')
  } finally {
    loading.value = false
  }
}

function openForm(khuyenMai?: KhuyenMai) {
  editingKhuyenMai.value = khuyenMai || null
  showForm.value = true
}

async function handleFormSubmit(data: any) {
  try {
    // Convert checkbox values
    data.trangThai = data.trangThai ? 1 : 0
    data.mucDoUuTien = parseInt(data.mucDoUuTien)
    data.phanTramGiam = parseFloat(data.phanTramGiam)
    data.giamToiDa = parseFloat(data.giamToiDa)

    if (editingKhuyenMai.value) {
      await api.put(`/api/khuyen-mai/${editingKhuyenMai.value.id}`, data)
      toastRef.value?.success('Thành công', 'Cập nhật đợt giảm giá thành công!')
    } else {
      await api.post('/api/khuyen-mai', data)
      toastRef.value?.success('Thành công', 'Thêm đợt giảm giá thành công!')
    }
    showForm.value = false
    await loadKhuyenMais()
  } catch (error: any) {
    console.error('Lỗi khi lưu:', error)
    if (error.response?.data) {
      toastRef.value?.error('Lỗi lưu Đợt Giảm Giá', error.response.data)
    } else {
      toastRef.value?.error('Lỗi lưu Đợt Giảm Giá', 'Có lỗi xảy ra khi lưu đợt giảm giá')
    }
  }
}

function deleteKhuyenMai(id: number) {
  const khuyenMai = khuyenMais.value.find(k => k.id === id)
  confirmTitle.value = 'Xác nhận xóa Đợt Giảm Giá'
  confirmMessage.value = `Bạn có chắc chắn muốn xóa đợt giảm giá "${khuyenMai?.tenKhuyenMai || 'này'}"? Hành động này không thể hoàn tác.`
  pendingAction.value = () => performDelete(id)
  showConfirmModal.value = true
}

async function performDelete(id: number) {
  try {
    await api.delete(`/api/khuyen-mai/${id}`)
    toastRef.value?.success('Thành công', 'Xóa đợt giảm giá thành công!')
    await loadKhuyenMais()
  } catch (error: any) {
    console.error('Lỗi khi xóa:', error)
    if (error.response?.data) {
      toastRef.value?.error('Không thể xóa', error.response.data)
    } else {
      toastRef.value?.error('Lỗi xóa Đợt Giảm Giá', 'Có lỗi xảy ra khi xóa đợt giảm giá')
    }
  }
}

async function toggleStatus(id: number) {
  try {
    await api.post(`/api/khuyen-mai/${id}/toggle-status`)
    toastRef.value?.success('Thành công', 'Cập nhật trạng thái thành công!')
    await loadKhuyenMais()
  } catch (error: any) {
    console.error('Lỗi khi cập nhật trạng thái:', error)
    toastRef.value?.error('Lỗi', 'Không thể cập nhật trạng thái')
  }
}

function handleConfirm() {
  if (pendingAction.value) {
    pendingAction.value()
  }
  showConfirmModal.value = false
  pendingAction.value = null
}

function handleCancel() {
  showConfirmModal.value = false
  pendingAction.value = null
}

function applyFilters() {
  // Filters are applied automatically through computed property
}

// Utility functions
function formatDateTime(dateString: string): string {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleDateString('vi-VN')
}

function formatCurrency(amount: number): string {
  if (!amount) return '-'
  return new Intl.NumberFormat('vi-VN', { 
    style: 'currency', 
    currency: 'VND' 
  }).format(amount)
}

function getPriorityLabel(priority: number): string {
  const labels = {
    1: 'Thấp',
    2: 'Trung bình', 
    3: 'Cao',
    4: 'Rất cao',
    5: 'Khẩn cấp'
  }
  return labels[priority as keyof typeof labels] || 'Không xác định'
}

function getPriorityClass(priority: number): string {
  const classes = {
    1: 'priority-low',
    2: 'priority-medium',
    3: 'priority-high', 
    4: 'priority-very-high',
    5: 'priority-urgent'
  }
  return classes[priority as keyof typeof classes] || 'priority-unknown'
}

function getStatusLabel(khuyenMai: KhuyenMai): string {
  const now = new Date()
  const startDate = new Date(khuyenMai.ngayBatDau)
  const endDate = new Date(khuyenMai.ngayKetThuc)
  
  if (khuyenMai.trangThai === 0) return 'Không hoạt động'
  if (now < startDate) return 'Chưa bắt đầu'
  if (now > endDate) return 'Đã kết thúc'
  return 'Đang diễn ra'
}

function getStatusClass(khuyenMai: KhuyenMai): string {
  const now = new Date()
  const startDate = new Date(khuyenMai.ngayBatDau)
  const endDate = new Date(khuyenMai.ngayKetThuc)
  
  if (khuyenMai.trangThai === 0) return 'status-inactive'
  if (now < startDate) return 'status-pending'
  if (now > endDate) return 'status-expired'
  return 'status-active'
}

onMounted(loadKhuyenMais)
</script>

<style scoped>
.page {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.btn-primary {
  background: #007bff;
  color: white;
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-primary:hover {
  background: #0056b3;
}

.filter-section {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.filter-group label {
  font-weight: bold;
  font-size: 14px;
}

.filter-group select,
.filter-group input {
  padding: 6px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  min-width: 150px;
}

.table-container {
  overflow-x: auto;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

table {
  width: 100%;
  border-collapse: collapse;
  background: white;
}

th,
td {
  border: 1px solid #ddd;
  padding: 12px;
  text-align: left;
}

th {
  background: #f8f9fa;
  font-weight: bold;
  position: sticky;
  top: 0;
}

tbody tr:hover {
  background: #f5f5f5;
}

/* Button styles */
.btn-edit {
  background: #28a745;
  color: white;
  padding: 4px 8px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 4px;
  font-size: 12px;
}

.btn-edit:hover {
  background: #218838;
}

.btn-delete {
  background: #dc3545;
  color: white;
  padding: 4px 8px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 4px;
  font-size: 12px;
}

.btn-delete:hover {
  background: #c82333;
}

.btn-disable {
  background: #ffc107;
  color: #212529;
  padding: 4px 8px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 4px;
  font-size: 12px;
}

.btn-disable:hover {
  background: #e0a800;
}

.btn-enable {
  background: #17a2b8;
  color: white;
  padding: 4px 8px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 4px;
  font-size: 12px;
}

.btn-enable:hover {
  background: #138496;
}

/* Status styles */
.status-active {
  color: #28a745;
  font-weight: bold;
  padding: 2px 8px;
  border-radius: 12px;
  background: #d4edda;
  font-size: 12px;
}

.status-inactive {
  color: #dc3545;
  font-weight: bold;
  padding: 2px 8px;
  border-radius: 12px;
  background: #f8d7da;
  font-size: 12px;
}

.status-expired {
  color: #6c757d;
  font-weight: bold;
  padding: 2px 8px;
  border-radius: 12px;
  background: #e2e3e5;
  font-size: 12px;
}

.status-pending {
  color: #ffc107;
  font-weight: bold;
  padding: 2px 8px;
  border-radius: 12px;
  background: #fff3cd;
  font-size: 12px;
}

/* Priority styles */
.priority-low {
  color: #6c757d;
  font-weight: bold;
  padding: 2px 8px;
  border-radius: 12px;
  background: #e2e3e5;
  font-size: 12px;
}

.priority-medium {
  color: #007bff;
  font-weight: bold;
  padding: 2px 8px;
  border-radius: 12px;
  background: #cce7ff;
  font-size: 12px;
}

.priority-high {
  color: #ffc107;
  font-weight: bold;
  padding: 2px 8px;
  border-radius: 12px;
  background: #fff3cd;
  font-size: 12px;
}

.priority-very-high {
  color: #fd7e14;
  font-weight: bold;
  padding: 2px 8px;
  border-radius: 12px;
  background: #ffeaa7;
  font-size: 12px;
}

.priority-urgent {
  color: #dc3545;
  font-weight: bold;
  padding: 2px 8px;
  border-radius: 12px;
  background: #f8d7da;
  font-size: 12px;
}

/* Responsive design */
@media (max-width: 768px) {
  .filter-section {
    flex-direction: column;
  }
  
  .filter-group {
    width: 100%;
  }
  
  .filter-group select,
  .filter-group input {
    min-width: 100%;
  }
  
  .header {
    flex-direction: column;
    gap: 10px;
    align-items: stretch;
  }
  
  table {
    font-size: 14px;
  }
  
  th, td {
    padding: 8px;
  }
}
</style>

