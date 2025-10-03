<template>
  <AdminTable
    :data="rams"
    :columns="ramColumns"
    title="Danh Sách RAM"
    titleIcon="🧠"
    entityName="RAM"
    searchPlaceholder="Tìm kiếm theo tên RAM..."
    @openForm="openForm"
    @deleteItem="deleteRam"
    @exportExcel="exportExcel"
    @toggleStatus="toggleRAMStatus"
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
    :title="editingRam ? 'Sửa RAM' : 'Thêm RAM'"
    :fields="ramFields"
    :initial-data="editingRam ? {
      maRam: editingRam.maRam || '',
      tenRam: editingRam.tenRam,
      moTa: editingRam.moTa || '',
      trangThai: editingRam.trangThai
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

interface Ram {
  id: number
  maRam?: string
  tenRam: string
  moTa?: string
  ngayTao?: string
  ngayCapNhat?: string
  trangThai: number
}

const rams = ref<Ram[]>([])
const loading = ref(false)
const showForm = ref(false)
const editingRam = ref<Ram | null>(null)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

// Confirm modal state
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const pendingAction = ref<(() => void) | null>(null)

const ramColumns = [
  { key: 'maRam', label: 'Mã', class: 'code-col', type: 'code' as const },
  { key: 'tenRam', label: 'Tên RAM', class: 'name-col' },
  { key: 'moTa', label: 'Mô tả', class: 'desc-col' },
  { key: 'trangThai', label: 'Trạng thái', class: 'status-col', type: 'status' as const }
]

const ramFields = [
  { key: 'maRam', label: 'Mã RAM', type: 'text' as const, required: true },
  { key: 'tenRam', label: 'Tên RAM', type: 'text' as const, required: true },
  { key: 'moTa', label: 'Mô tả', type: 'textarea' as const },
  { key: 'trangThai', label: 'Hoạt động', type: 'checkbox' as const }
]

async function loadRams() {
  loading.value = true
  try {
    const { data } = await api.get<Ram[]>('/api/ram')
    rams.value = data
  } finally {
    loading.value = false
  }
}

function openForm(ram?: Ram) {
  editingRam.value = ram || null
  showForm.value = true
}

async function handleFormSubmit(data: any) {
  try {
    if (editingRam.value) {
      await api.put(`/api/ram/${editingRam.value.id}`, data)
      toastRef.value?.success('Thành công', 'Cập nhật RAM thành công!')
    } else {
      await api.post('/api/ram', data)
      toastRef.value?.success('Thành công', 'Thêm RAM thành công!')
    }
    showForm.value = false
    await loadRams()
  } catch (error: any) {
    console.error('Lỗi khi lưu:', error)
    if (error.response?.data) {
      toastRef.value?.error('Lỗi lưu RAM', error.response.data)
    } else {
      toastRef.value?.error('Lỗi lưu RAM', 'Có lỗi xảy ra khi lưu RAM')
    }
  }
}

function deleteRam(id: number) {
  const ram = rams.value.find(r => r.id === id)
  confirmTitle.value = 'Xác nhận xóa RAM'
  confirmMessage.value = `Bạn có chắc chắn muốn xóa RAM "${ram?.tenRam || 'này'}"? Hành động này không thể hoàn tác.`
  pendingAction.value = () => performDelete(id)
  showConfirmModal.value = true
}

async function performDelete(id: number) {
  try {
    await api.delete(`/api/ram/${id}`)
    toastRef.value?.success('Thành công', 'Xóa RAM thành công!')
    await loadRams()
  } catch (error: any) {
    console.error('Lỗi khi xóa:', error)
    if (error.response?.data) {
      toastRef.value?.error('Không thể xóa', error.response.data)
    } else {
      toastRef.value?.error('Lỗi xóa RAM', 'Có lỗi xảy ra khi xóa RAM')
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
    const response = await api.get('/api/ram/export', { responseType: 'blob' })
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', 'danh_sach_ram.xlsx')
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
  loadRams()
})
</script>

<style scoped>
/* Custom column widths for RAM page */
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