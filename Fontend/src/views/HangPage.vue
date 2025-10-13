<template>
  <AdminTable
    :data="hangs"
    :columns="hangColumns"
    title="Danh Sách Hãng"
    titleIcon="📋"
    entityName="hãng"
    searchPlaceholder="Tìm kiếm theo tên hãng..."
    @openForm="openForm"
    @deleteItem="deleteHang"
    @exportExcel="exportExcel"
    @toggleStatus="toggleHangStatus"
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
    :title="editingHang ? 'Sửa Hãng' : 'Thêm Hãng'"
    :fields="hangFields"
    :initial-data="editingHang ? {
      ten: editingHang.ten,
      xuatXu: editingHang.xuatXu || '',
      moTa: editingHang.moTa || '',
      trangThai: editingHang.trangThai
    } : undefined"
    @submit="handleFormSubmit"
    @cancel="showForm = false"
  />

  <Toast ref="toastRef" />
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import api from '@/services/api'
import AdminTable from '@/components/AdminTable.vue'
import ConfirmModal from '@/components/ConfirmModal.vue'
import FormModal from '@/components/FormModal.vue'
import Toast from '@/components/Toast.vue'
import '@/styles/admin-layout.css'

interface Hang {
  id: number
  ten: string
  xuatXu?: string
  moTa?: string
  ngayTao?: string
  ngayCapNhat?: string
  trangThai: number
}

const hangs = ref<Hang[]>([])
const loading = ref(false)
const showForm = ref(false)
const editingHang = ref<Hang | null>(null)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

// Confirm modal state
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const pendingAction = ref<(() => void) | null>(null)

const hangColumns = [
  { key: 'ten', label: 'Tên Hãng', class: 'name-col' },
  { key: 'xuatXu', label: 'Xuất xứ', class: 'origin-col' },
  { key: 'moTa', label: 'Mô tả', class: 'desc-col' },
  { key: 'trangThai', label: 'Trạng thái', class: 'status-col', type: 'status' as const }
]

const hangFields = [
  { key: 'ten', label: 'Tên Hãng', type: 'text' as const, required: true },
  { key: 'xuatXu', label: 'Xuất xứ', type: 'text' as const },
  { key: 'moTa', label: 'Mô tả', type: 'textarea' as const },
  { key: 'trangThai', label: 'Hoạt động', type: 'checkbox' as const }
]

async function loadHangs() {
  loading.value = true
  try {
    const { data } = await api.get<Hang[]>('/api/hang')
    hangs.value = data
  } finally {
    loading.value = false
  }
}

function openForm(hang?: Hang) {
  editingHang.value = hang || null
  showForm.value = true
}

async function handleFormSubmit(data: any) {
  try {
    if (editingHang.value) {
      await api.put(`/api/hang/${editingHang.value.id}`, data)
      toastRef.value?.success('Thành công', 'Cập nhật hãng thành công!')
    } else {
      await api.post('/api/hang', data)
      toastRef.value?.success('Thành công', 'Thêm hãng thành công!')
    }
    showForm.value = false
    await loadHangs()
  } catch (error: any) {
    console.error('Lỗi khi lưu:', error)
    if (error.response?.data) {
      toastRef.value?.error('Lỗi lưu Hãng', error.response.data)
    } else {
      toastRef.value?.error('Lỗi lưu Hãng', 'Có lỗi xảy ra khi lưu hãng')
    }
  }
}

function deleteHang(id: number) {
  const hang = hangs.value.find(h => h.id === id)
  confirmTitle.value = 'Xác nhận xóa Hãng'
  confirmMessage.value = `Bạn có chắc chắn muốn xóa hãng "${hang?.ten || 'này'}"? Hành động này không thể hoàn tác.`
  pendingAction.value = () => performDelete(id)
  showConfirmModal.value = true
}

async function performDelete(id: number) {
  try {
    await api.delete(`/api/hang/${id}`)
    toastRef.value?.success('Thành công', 'Xóa hãng thành công!')
    await loadHangs()
  } catch (error: any) {
    console.error('Lỗi khi xóa:', error)
    if (error.response?.data) {
      toastRef.value?.error('Không thể xóa', error.response.data)
    } else {
      toastRef.value?.error('Lỗi xóa Hãng', 'Có lỗi xảy ra khi xóa hãng')
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

async function toggleHangStatus(hang: Hang) {
  try {
    const newStatus = hang.trangThai === 1 ? 0 : 1
    await api.put(`/api/hang/${hang.id}/status`, { trangThai: newStatus })
    
    // Update local data
    const index = hangs.value.findIndex(h => h.id === hang.id)
    if (index !== -1) {
      hangs.value[index].trangThai = newStatus
    }
  } catch (error: any) {
    console.error('Lỗi khi cập nhật trạng thái:', error)
    if (error.response?.data) {
      toastRef.value?.error('Lỗi cập nhật trạng thái', error.response.data)
    } else {
      toastRef.value?.error('Lỗi cập nhật trạng thái', 'Có lỗi xảy ra khi cập nhật trạng thái')
    }
  }
}

async function exportExcel() {
  try {
    const response = await api.get('/api/hang/export', { responseType: 'blob' })
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', 'danh_sach_hang.xlsx')
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

onMounted(() => {
  loadHangs()
})
</script>

<style scoped>
/* Custom column widths for Hang page */
:deep(.data-table .code-col) {
  width: 120px;
  min-width: 120px;
}

:deep(.data-table .name-col) {
  min-width: 150px;
}

:deep(.data-table .origin-col) {
  min-width: 120px;
}

:deep(.data-table .desc-col) {
  min-width: 200px;
  max-width: 300px;
  word-wrap: break-word;
}

:deep(.data-table .status-col) {
  width: 120px;
  min-width: 120px;
  text-align: center;
}

:deep(.data-table .date-col) {
  width: 120px;
  min-width: 120px;
  text-align: center;
  font-size: 13px;
}

:deep(.data-table .action-col) {
  width: 100px;
  min-width: 100px;
  text-align: center;
}
</style>