<template>
  <AdminTable
    :data="gpus"
    :columns="columns"
    title="Danh Sách GPU"
    title-icon="🎮"
    entity-name="GPU"
    search-placeholder="Tìm kiếm theo tên GPU..."
    @open-form="openForm"
    @delete-item="deleteGpu"
    @export-excel="exportExcel"
    @toggle-status="toggleGpuStatus"
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

function deleteGpu(id: number) {
  const gpu = gpus.value.find(g => g.id === id)
  confirmTitle.value = 'Xác nhận xóa GPU'
  confirmMessage.value = `Bạn có chắc chắn muốn xóa GPU "${gpu?.tenGpu || 'này'}"? Hành động này không thể hoàn tác.`
  pendingAction.value = () => performDelete(id)
  showConfirmModal.value = true
}

async function performDelete(id: number) {
  try {
    await api.delete(`/api/gpu/${id}`)
    toastRef.value?.success('Thành công', 'Xóa GPU thành công!')
    await loadGpus()
  } catch (error: any) {
    console.error('Lỗi khi xóa:', error)
    // Hiển thị thông báo lỗi cho user
    if (error.response?.data) {
      // Backend trả về thông báo lỗi trực tiếp trong response.data
      toastRef.value?.error('Không thể xóa', error.response.data)
    } else {
      toastRef.value?.error('Lỗi xóa GPU', 'Có lỗi xảy ra khi xóa GPU')
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

function exportExcel() {
  // TODO: Implement Excel export
  console.log('Export Excel for GPU')
}

onMounted(loadGpus)
</script>

<style scoped>
@import '@/styles/admin-layout.css';
</style>
