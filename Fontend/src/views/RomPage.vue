<template>
  <AdminTable
    :data="roms"
    :columns="romColumns"
    title="Danh Sách ROM"
    titleIcon="💾"
    entityName="ROM"
    searchPlaceholder="Tìm kiếm theo tên ROM..."
    @openForm="openForm"
    @deleteItem="deleteRom"
    @exportExcel="exportExcel"
    @toggleStatus="toggleROMStatus"
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
    :title="editingRom ? 'Sửa ROM' : 'Thêm ROM'"
    :fields="romFields"
    :initial-data="editingRom ? {
      maRom: editingRom.maRom || '',
      dungLuong: editingRom.dungLuong,
      moTa: editingRom.moTa || '',
      trangThai: editingRom.trangThai
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

interface Rom {
  id: number
  maRom?: string
  dungLuong: string
  moTa?: string
  ngayTao?: string
  ngayCapNhat?: string
  trangThai: number
}

const roms = ref<Rom[]>([])
const loading = ref(false)
const showForm = ref(false)
const editingRom = ref<Rom | null>(null)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

// Confirm modal state
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const pendingAction = ref<(() => void) | null>(null)

const romColumns = [
  { key: 'maRom', label: 'Mã', class: 'code-col', type: 'code' as const },
  { key: 'dungLuong', label: 'Dung lượng', class: 'name-col' },
  { key: 'moTa', label: 'Mô tả', class: 'desc-col' },
  { key: 'trangThai', label: 'Trạng thái', class: 'status-col', type: 'status' as const }
]

const romFields = [
  { key: 'maRom', label: 'Mã ROM', type: 'text' as const, required: true },
  { key: 'dungLuong', label: 'Dung lượng', type: 'text' as const, required: true },
  { key: 'moTa', label: 'Mô tả', type: 'textarea' as const },
  { key: 'trangThai', label: 'Hoạt động', type: 'checkbox' as const }
]

async function loadRoms() {
  loading.value = true
  try {
    const { data } = await api.get<Rom[]>('/api/rom')
    roms.value = data
  } finally {
    loading.value = false
  }
}

function openForm(rom?: Rom) {
  editingRom.value = rom || null
  showForm.value = true
}

async function handleFormSubmit(data: any) {
  try {
    if (editingRom.value) {
      await api.put(`/api/rom/${editingRom.value.id}`, data)
      toastRef.value?.success('Thành công', 'Cập nhật ROM thành công!')
    } else {
      await api.post('/api/rom', data)
      toastRef.value?.success('Thành công', 'Thêm ROM thành công!')
    }
    showForm.value = false
    await loadRoms()
  } catch (error: any) {
    console.error('Lỗi khi lưu:', error)
    if (error.response?.data) {
      toastRef.value?.error('Lỗi lưu ROM', error.response.data)
    } else {
      toastRef.value?.error('Lỗi lưu ROM', 'Có lỗi xảy ra khi lưu ROM')
    }
  }
}

function deleteRom(id: number) {
  const rom = roms.value.find(r => r.id === id)
  confirmTitle.value = 'Xác nhận xóa ROM'
  confirmMessage.value = `Bạn có chắc chắn muốn xóa ROM "${rom?.dungLuong || 'này'}"? Hành động này không thể hoàn tác.`
  pendingAction.value = () => performDelete(id)
  showConfirmModal.value = true
}

async function performDelete(id: number) {
  try {
    await api.delete(`/api/rom/${id}`)
    toastRef.value?.success('Thành công', 'Xóa ROM thành công!')
    await loadRoms()
  } catch (error: any) {
    console.error('Lỗi khi xóa:', error)
    if (error.response?.data) {
      toastRef.value?.error('Không thể xóa', error.response.data)
    } else {
      toastRef.value?.error('Lỗi xóa ROM', 'Có lỗi xảy ra khi xóa ROM')
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

async function exportExcel() {
  try {
    const response = await api.get('/api/rom/export', { responseType: 'blob' })
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', 'danh_sach_rom.xlsx')
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
  loadRoms()
})
</script>

<style scoped>
/* Custom column widths for ROM page */
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