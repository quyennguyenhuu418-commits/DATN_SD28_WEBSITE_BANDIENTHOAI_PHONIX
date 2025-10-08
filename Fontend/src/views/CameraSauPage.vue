<template>
  <AdminTable
    :data="cameras"
    :columns="columns"
    title="Danh Sách Camera Sau"
    titleIcon="📸"
    entityName="Camera sau"
    searchPlaceholder="Tìm kiếm theo thông số camera..."
    @openForm="openForm"
    @exportExcel="exportExcel"
    @toggleStatus="toggleCameraSauStatus"
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

async function toggleCameraSauStatus(camera: Camera) {
  try {
    const newStatus = (camera.trangThai || 0) === 1 ? 0 : 1
    await api.put(`/api/camera-sau/${camera.id}/status`, { trangThai: newStatus })
    
    // Update local data
    const index = cameras.value.findIndex(c => c.id === camera.id)
    if (index !== -1) {
      cameras.value[index].trangThai = newStatus
    }
    
    const statusText = newStatus === 1 ? 'Hoạt động' : 'Ngừng hoạt động'
    const message = newStatus === 1 
      ? `Đã chuyển Camera sau "${camera.thongSo}" sang trạng thái <span style="color: #28a745; font-weight: bold;">${statusText}</span>`
      : `Đã chuyển Camera sau "${camera.thongSo}" sang trạng thái <span style="color: #dc3545; font-weight: bold;">${statusText}</span>`
    toastRef.value?.success('Thành công', message)
  } catch (error: any) {
    console.error('Lỗi khi cập nhật trạng thái:', error)
    toastRef.value?.error('Lỗi cập nhật', 'Không thể cập nhật trạng thái Camera sau')
  }
}

async function exportExcel() {
  try {
    const response = await api.get('/api/camera-sau/export', { responseType: 'blob' })
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', 'danh_sach_camera_sau.xlsx')
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

onMounted(loadCameras)
</script>

<style scoped>
@import '@/styles/admin-layout.css';
</style>
