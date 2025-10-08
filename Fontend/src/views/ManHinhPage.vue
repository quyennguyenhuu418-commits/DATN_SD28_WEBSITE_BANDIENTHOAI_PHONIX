<template>
  <AdminTable
    :data="manHinhs"
    :columns="columns"
    title="Danh Sách Màn Hình"
    titleIcon="📱"
    entityName="Màn hình"
    searchPlaceholder="Tìm kiếm theo kích thước màn hình..."
    @openForm="openForm"
    @exportExcel="exportExcel"
    @toggleStatus="toggleManHinhStatus"
  />

  <!-- Confirm Modal -->
  <ConfirmModal
    :show="showConfirmModal"
    :title="confirmTitle"
    :message="confirmMessage"
    @confirm="handleConfirm"
    @cancel="handleCancel"
  />

  <FormModal
    :show="showForm"
    :title="editingManHinh ? 'Sửa Màn hình' : 'Thêm Màn hình'"
    :fields="manHinhFields"
    :initial-data="editingManHinh ? {
      maManHinh: editingManHinh.maManHinh,
      kichThuoc: editingManHinh.kichThuoc,
      congNghe: editingManHinh.congNghe || '',
      doPhanGiai: editingManHinh.doPhanGiai,
      tanSoQuet: editingManHinh.tanSoQuet || '',
      kieuManHinh: editingManHinh.kieuManHinh || '',
      moTa: editingManHinh.moTa || '',
      trangThai: editingManHinh.trangThai
    } : undefined"
    @submit="handleFormSubmit"
    @cancel="showForm = false"
  />

  <Toast ref="toastRef" />
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import api from '@/services/api'
import ConfirmModal from '@/components/ConfirmModal.vue'
import FormModal from '@/components/FormModal.vue'
import Toast from '@/components/Toast.vue'
import AdminTable from '@/components/AdminTable.vue'

interface ManHinh {
  id: number
  maManHinh: string
  kichThuoc: string
  congNghe?: string
  doPhanGiai: string
  tanSoQuet?: string
  kieuManHinh?: string
  moTa?: string
  ngayTao?: string
  ngayCapNhat?: string
  trangThai: number
}

const manHinhs = ref<ManHinh[]>([])
const loading = ref(false)
const showForm = ref(false)
const editingManHinh = ref<ManHinh | null>(null)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

// Table columns configuration
const columns = [
  { key: 'maManHinh', label: 'Mã', class: 'code-col', type: 'code' as const },
  { key: 'kichThuoc', label: 'Kích thước', class: 'size-col' },
  { key: 'doPhanGiai', label: 'Độ phân giải', class: 'desc-col' },
  { key: 'congNghe', label: 'Công nghệ', class: 'desc-col' },
  { key: 'tanSoQuet', label: 'Tần số quét', class: 'frequency-col' },
  { key: 'kieuManHinh', label: 'Kiểu màn hình', class: 'type-col' },
  { key: 'trangThai', label: 'Trạng thái', class: 'status-col', type: 'status' as const }
]

// Confirm modal state
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const pendingAction = ref<(() => void) | null>(null)

const manHinhFields = [
  { key: 'maManHinh', label: 'Mã Màn hình', type: 'text' as const, required: true },
  { key: 'kichThuoc', label: 'Kích thước', type: 'text' as const, required: true },
  { key: 'congNghe', label: 'Công nghệ', type: 'text' as const },
  { key: 'doPhanGiai', label: 'Độ phân giải', type: 'text' as const, required: true },
  { key: 'tanSoQuet', label: 'Tần số quét', type: 'text' as const },
  { key: 'kieuManHinh', label: 'Kiểu màn hình', type: 'text' as const },
  { key: 'moTa', label: 'Mô tả', type: 'textarea' as const },
  { key: 'trangThai', label: 'Hoạt động', type: 'checkbox' as const }
]

async function loadManHinhs() {
  loading.value = true
  try {
    const { data } = await api.get<ManHinh[]>('/api/man-hinh')
    manHinhs.value = data
  } finally {
    loading.value = false
  }
}

function openForm(manHinh?: ManHinh) {
  editingManHinh.value = manHinh || null
  showForm.value = true
}

async function handleFormSubmit(data: any) {
  try {
    if (editingManHinh.value) {
      await api.put(`/api/man-hinh/${editingManHinh.value.id}`, data)
      toastRef.value?.success('Thành công', 'Cập nhật màn hình thành công!')
    } else {
      await api.post('/api/man-hinh', data)
      toastRef.value?.success('Thành công', 'Thêm màn hình thành công!')
    }
    showForm.value = false
    await loadManHinhs()
  } catch (error: any) {
    console.error('Lỗi khi lưu:', error)
    if (error.response?.data) {
      toastRef.value?.error('Lỗi lưu Màn Hình', error.response.data)
    } else {
      toastRef.value?.error('Lỗi lưu Màn Hình', 'Có lỗi xảy ra khi lưu màn hình')
    }
  }
}

function deleteManHinh(id: number) {
  const manHinh = manHinhs.value.find(m => m.id === id)
  confirmTitle.value = 'Xác nhận xóa Màn hình'
  confirmMessage.value = `Bạn có chắc chắn muốn xóa màn hình "${manHinh?.kichThuoc || 'này'}"? Hành động này không thể hoàn tác.`
  pendingAction.value = () => performDelete(id)
  showConfirmModal.value = true
}

async function performDelete(id: number) {
  try {
    await api.delete(`/api/man-hinh/${id}`)
    toastRef.value?.success('Thành công', 'Xóa màn hình thành công!')
    await loadManHinhs()
  } catch (error: any) {
    console.error('Lỗi khi xóa:', error)
    // Hiển thị thông báo lỗi cho user
    if (error.response?.data) {
      // Backend trả về thông báo lỗi trực tiếp trong response.data
      toastRef.value?.error('Không thể xóa', error.response.data)
    } else {
      toastRef.value?.error('Lỗi xóa Màn Hình', 'Có lỗi xảy ra khi xóa màn hình')
    }
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

async function toggleManHinhStatus(manHinh: ManHinh) {
  try {
    const newStatus = (manHinh.trangThai || 0) === 1 ? 0 : 1
    await api.put(`/api/man-hinh/${manHinh.id}/status`, { trangThai: newStatus })
    
    // Update local data
    const index = manHinhs.value.findIndex(m => m.id === manHinh.id)
    if (index !== -1) {
      manHinhs.value[index].trangThai = newStatus
    }
    
    const statusText = newStatus === 1 ? 'Hoạt động' : 'Ngừng hoạt động'
    const message = newStatus === 1 
      ? `Đã chuyển Màn hình "${manHinh.kichThuoc}" sang trạng thái <span style="color: #28a745; font-weight: bold;">${statusText}</span>`
      : `Đã chuyển Màn hình "${manHinh.kichThuoc}" sang trạng thái <span style="color: #dc3545; font-weight: bold;">${statusText}</span>`
    toastRef.value?.success('Thành công', message)
  } catch (error: any) {
    console.error('Lỗi khi cập nhật trạng thái:', error)
    toastRef.value?.error('Lỗi cập nhật', 'Không thể cập nhật trạng thái Màn hình')
  }
}

async function exportExcel() {
  try {
    const response = await api.get('/api/man-hinh/export', { responseType: 'blob' })
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', 'danh_sach_man_hinh.xlsx')
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
    toastRef.value?.success('Thành công', 'Xuất Excel thành công!')
  } catch (error: any) {
    console.error('Lỗi khi xuất Excel:', error)
    toastRef.value?.error('Lỗi xuất Excel', 'Có lỗi xảy ra khi xuất file Excel')
  }
}

onMounted(loadManHinhs)
</script>

<style scoped>
@import '@/styles/admin-layout.css';

/* Custom column widths for ManHinh page */
:deep(.data-table .code-col) {
  width: 100px;
  min-width: 100px;
}

:deep(.data-table .size-col) {
  width: 170px !important;
  min-width: 170px !important;
}

:deep(.data-table .stt-col) {
  width: 40px;
  min-width: 40px;
}

:deep(.data-table .desc-col) {
  width: 100px;
  min-width: 100px;
}

:deep(.data-table .frequency-col) {
  width: 80px;
  min-width: 80px;
}

:deep(.data-table .type-col) {
  width: 80px;
  min-width: 80px;
}

:deep(.data-table .status-col) {
  width: 100px;
  min-width: 100px;
}

:deep(.data-table .date-col) {
  width: 100px;
  min-width: 100px;
}

:deep(.data-table .action-col) {
  width: 180px;
  min-width: 180px;
  text-align: center;
}

/* Improve action buttons spacing */
:deep(.edit-btn, .delete-btn) {
  margin: 0 4px;
  padding: 6px 10px;
  font-size: 14px;
}
</style>
