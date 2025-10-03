<template>
  <AdminTable
    :data="cameras"
    :columns="columns"
    title="Danh Sách Camera Sau"
    title-icon="📸"
    entity-name="camera sau"
    search-placeholder="Tìm kiếm theo thông số camera..."
    @open-form="openForm"
    @delete-item="deleteCamera"
    @export-excel="exportExcel"
    @toggle-status="toggleCameraSauStatus"
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
    :title="editingCamera ? 'Sửa Camera Sau' : 'Thêm Camera Sau'"
    :fields="cameraFields"
    :initial-data="editingCamera ? {
      maCamera: editingCamera.maCamera,
      thongSo: editingCamera.thongSo,
      moTa: editingCamera.moTa || '',
      trangThai: editingCamera.trangThai
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

interface Camera {
  id: number
  maCamera: string
  thongSo: string
  moTa?: string
  trangThai: number
}

const cameras = ref<Camera[]>([])
const loading = ref(false)
const showForm = ref(false)
const editingCamera = ref<Camera | null>(null)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

// Table columns configuration
const columns = [
  { key: 'maCamera', label: 'Mã', class: 'code-col', type: 'code' as const },
  { key: 'thongSo', label: 'Thông số', class: 'name-col' },
  { key: 'moTa', label: 'Mô tả', class: 'desc-col' },
  { key: 'trangThai', label: 'Trạng thái', class: 'status-col', type: 'status' as const }
]

// Confirm modal state
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const pendingAction = ref<(() => void) | null>(null)

const cameraFields = [
  { key: 'maCamera', label: 'Mã Camera', type: 'text' as const, required: true },
  { key: 'thongSo', label: 'Thông số', type: 'text' as const, required: true },
  { key: 'moTa', label: 'Mô tả', type: 'textarea' as const },
  { key: 'trangThai', label: 'Hoạt động', type: 'checkbox' as const }
]

async function loadCameras() {
  loading.value = true
  try {
    const { data } = await api.get<Camera[]>('/api/camera-sau')
    cameras.value = data
  } finally {
    loading.value = false
  }
}

function openForm(camera?: Camera) {
  editingCamera.value = camera || null
  showForm.value = true
}

async function handleFormSubmit(data: any) {
  try {
    if (editingCamera.value) {
      await api.put(`/api/camera-sau/${editingCamera.value.id}`, data)
      toastRef.value?.success('Thành công', 'Cập nhật camera sau thành công!')
    } else {
      await api.post('/api/camera-sau', data)
      toastRef.value?.success('Thành công', 'Thêm camera sau thành công!')
    }
    showForm.value = false
    await loadCameras()
  } catch (error: any) {
    console.error('Lỗi khi lưu:', error)
    if (error.response?.data) {
      toastRef.value?.error('Lỗi lưu Camera Sau', error.response.data)
    } else {
      toastRef.value?.error('Lỗi lưu Camera Sau', 'Có lỗi xảy ra khi lưu camera sau')
    }
  }
}

function deleteCamera(id: number) {
  const camera = cameras.value.find(c => c.id === id)
  confirmTitle.value = 'Xác nhận xóa Camera Sau'
  confirmMessage.value = `Bạn có chắc chắn muốn xóa camera "${camera?.maCamera || 'này'}"? Hành động này không thể hoàn tác.`
  pendingAction.value = () => performDelete(id)
  showConfirmModal.value = true
}

async function performDelete(id: number) {
  try {
    await api.delete(`/api/camera-sau/${id}`)
    toastRef.value?.success('Thành công', 'Xóa camera sau thành công!')
    await loadCameras()
  } catch (error: any) {
    console.error('Lỗi khi xóa:', error)
    // Hiển thị thông báo lỗi cho user
    if (error.response?.data) {
      // Backend trả về thông báo lỗi trực tiếp trong response.data
      toastRef.value?.error('Không thể xóa', error.response.data)
    } else {
      toastRef.value?.error('Lỗi xóa Camera Sau', 'Có lỗi xảy ra khi xóa camera sau')
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
  console.log('Export Excel for CameraSau')
}

onMounted(loadCameras)
</script>

<style scoped>
@import '@/styles/admin-layout.css';
</style>
