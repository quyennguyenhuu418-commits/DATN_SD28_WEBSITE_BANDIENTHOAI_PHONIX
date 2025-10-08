<template>
  <AdminTable
    :data="gpus"
    :columns="columns"
    title="Danh Sách GPU"
    titleIcon="🎮"
    entityName="GPU"
    searchPlaceholder="Tìm kiếm theo tên GPU..."
    @openForm="openForm"
    @exportExcel="exportExcel"
    @toggleStatus="toggleGpuStatus"
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
    :title="editingGpu ? 'Sửa GPU' : 'Thêm GPU'"
    :fields="gpuFields"
    :initial-data="editingGpu ? {
      maGpu: editingGpu.maGpu,
      tenGpu: editingGpu.tenGpu,
      moTa: editingGpu.moTa || '',
      trangThai: editingGpu.trangThai
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

interface Gpu {
  id: number
  maGpu: string
  tenGpu: string
  moTa?: string
  trangThai: number
}

const gpus = ref<Gpu[]>([])
const loading = ref(false)
const showForm = ref(false)
const editingGpu = ref<Gpu | null>(null)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

// Table columns configuration
const columns = [
  { key: 'maGpu', label: 'Mã', class: 'code-col', type: 'code' as const },
  { key: 'tenGpu', label: 'Tên GPU', class: 'name-col' },
  { key: 'moTa', label: 'Mô tả', class: 'desc-col' },
  { key: 'trangThai', label: 'Trạng thái', class: 'status-col', type: 'status' as const }
]

// Confirm modal state
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const pendingAction = ref<(() => void) | null>(null)

const gpuFields = [
  { key: 'maGpu', label: 'Mã GPU', type: 'text' as const, required: true },
  { key: 'tenGpu', label: 'Tên GPU', type: 'text' as const, required: true },
  { key: 'moTa', label: 'Mô tả', type: 'textarea' as const },
  { key: 'trangThai', label: 'Hoạt động', type: 'checkbox' as const }
]

async function loadGpus() {
  loading.value = true
  try {
    const { data } = await api.get<Gpu[]>('/api/gpu')
    gpus.value = data
  } finally {
    loading.value = false
  }
}

function openForm(gpu?: Gpu) {
  editingGpu.value = gpu || null
  showForm.value = true
}

async function handleFormSubmit(data: any) {
  try {
    if (editingGpu.value) {
      await api.put(`/api/gpu/${editingGpu.value.id}`, data)
      toastRef.value?.success('Thành công', 'Cập nhật GPU thành công!')
    } else {
      await api.post('/api/gpu', data)
      toastRef.value?.success('Thành công', 'Thêm GPU thành công!')
    }
    showForm.value = false
    await loadGpus()
  } catch (error: any) {
    console.error('Lỗi khi lưu:', error)
    if (error.response?.data) {
      toastRef.value?.error('Lỗi lưu GPU', error.response.data)
    } else {
      toastRef.value?.error('Lỗi lưu GPU', 'Có lỗi xảy ra khi lưu GPU')
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

async function toggleGpuStatus(gpu: Gpu) {
  try {
    const newStatus = (gpu.trangThai || 0) === 1 ? 0 : 1
    await api.put(`/api/gpu/${gpu.id}/status`, { trangThai: newStatus })
    
    // Update local data
    const index = gpus.value.findIndex(g => g.id === gpu.id)
    if (index !== -1) {
      gpus.value[index].trangThai = newStatus
    }
    
    const statusText = newStatus === 1 ? 'Hoạt động' : 'Ngừng hoạt động'
    const message = newStatus === 1 
      ? `Đã chuyển GPU "${gpu.tenGpu}" sang trạng thái <span style="color: #28a745; font-weight: bold;">${statusText}</span>`
      : `Đã chuyển GPU "${gpu.tenGpu}" sang trạng thái <span style="color: #dc3545; font-weight: bold;">${statusText}</span>`
    toastRef.value?.success('Thành công', message)
  } catch (error: any) {
    console.error('Lỗi khi cập nhật trạng thái:', error)
    toastRef.value?.error('Lỗi cập nhật', 'Không thể cập nhật trạng thái GPU')
  }
}

async function exportExcel() {
  try {
    const response = await api.get('/api/gpu/export', { responseType: 'blob' })
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', 'danh_sach_gpu.xlsx')
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

onMounted(loadGpus)
</script>

<style scoped>
@import '@/styles/admin-layout.css';
</style>
