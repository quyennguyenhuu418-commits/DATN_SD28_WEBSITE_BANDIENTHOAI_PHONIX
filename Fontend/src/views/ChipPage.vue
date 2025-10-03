<template>
  <AdminTable
    :data="chips"
    :columns="chipColumns"
    title="Danh Sách Chip"
    titleIcon="🔧"
    entityName="chip"
    searchPlaceholder="Tìm kiếm theo tên chip..."
    @openForm="openForm"
    @deleteItem="deleteChip"
    @exportExcel="exportExcel"
    @toggleStatus="toggleChipStatus"
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
    :title="editingChip ? 'Sửa Chip' : 'Thêm Chip'"
    :fields="chipFields"
    :initial-data="editingChip ? {
      maChip: editingChip.maChip || '',
      tenChip: editingChip.tenChip,
      moTa: editingChip.moTa || '',
      trangThai: editingChip.trangThai
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

interface Chip {
  id: number
  maChip?: string
  tenChip: string
  moTa?: string
  ngayTao?: string
  ngayCapNhat?: string
  trangThai: number
}

const chips = ref<Chip[]>([])
const loading = ref(false)
const showForm = ref(false)
const editingChip = ref<Chip | null>(null)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

// Confirm modal state
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const pendingAction = ref<(() => void) | null>(null)

const chipColumns = [
  { key: 'maChip', label: 'Mã', class: 'code-col', type: 'code' as const },
  { key: 'tenChip', label: 'Tên Chip', class: 'name-col' },
  { key: 'moTa', label: 'Mô tả', class: 'desc-col' },
  { key: 'trangThai', label: 'Trạng thái', class: 'status-col', type: 'status' as const }
]

const chipFields = [
  { key: 'maChip', label: 'Mã Chip', type: 'text' as const, required: true },
  { key: 'tenChip', label: 'Tên Chip', type: 'text' as const, required: true },
  { key: 'moTa', label: 'Mô tả', type: 'textarea' as const },
  { key: 'trangThai', label: 'Hoạt động', type: 'checkbox' as const }
]

async function loadChips() {
  loading.value = true
  try {
    const { data } = await api.get<Chip[]>('/api/chip')
    chips.value = data
  } finally {
    loading.value = false
  }
}

function openForm(chip?: Chip) {
  editingChip.value = chip || null
  showForm.value = true
}

async function handleFormSubmit(data: any) {
  try {
    if (editingChip.value) {
      await api.put(`/api/chip/${editingChip.value.id}`, data)
      toastRef.value?.success('Thành công', 'Cập nhật chip thành công!')
    } else {
      await api.post('/api/chip', data)
      toastRef.value?.success('Thành công', 'Thêm chip thành công!')
    }
    showForm.value = false
    await loadChips()
  } catch (error: any) {
    console.error('Lỗi khi lưu:', error)
    if (error.response?.data) {
      toastRef.value?.error('Lỗi lưu Chip', error.response.data)
    } else {
      toastRef.value?.error('Lỗi lưu Chip', 'Có lỗi xảy ra khi lưu chip')
    }
  }
}

function deleteChip(id: number) {
  const chip = chips.value.find(c => c.id === id)
  confirmTitle.value = 'Xác nhận xóa Chip'
  confirmMessage.value = `Bạn có chắc chắn muốn xóa chip "${chip?.tenChip || 'này'}"? Hành động này không thể hoàn tác.`
  pendingAction.value = () => performDelete(id)
  showConfirmModal.value = true
}

async function performDelete(id: number) {
  try {
    await api.delete(`/api/chip/${id}`)
    toastRef.value?.success('Thành công', 'Xóa chip thành công!')
    await loadChips()
  } catch (error: any) {
    console.error('Lỗi khi xóa:', error)
    if (error.response?.data) {
      toastRef.value?.error('Không thể xóa', error.response.data)
    } else {
      toastRef.value?.error('Lỗi xóa Chip', 'Có lỗi xảy ra khi xóa chip')
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

async function toggleChipStatus(chip: Chip) {
  try {
    const newStatus = chip.trangThai === 1 ? 0 : 1
    await api.put(`/api/chip/${chip.id}/status`, { trangThai: newStatus })
    
    // Update local data
    const index = chips.value.findIndex(c => c.id === chip.id)
    if (index !== -1) {
      chips.value[index].trangThai = newStatus
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
    const response = await api.get('/api/chip/export', { responseType: 'blob' })
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', 'danh_sach_chip.xlsx')
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
  loadChips()
})
</script>

<style scoped>
/* Custom column widths for Chip page */
:deep(.data-table .code-col) {
  width: 120px;
  min-width: 120px;
}

:deep(.data-table .name-col) {
  min-width: 150px;
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