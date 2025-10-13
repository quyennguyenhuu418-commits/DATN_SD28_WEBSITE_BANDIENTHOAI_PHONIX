<template>
  <div class="page">
    <PosHeader />
    
    <div class="content">
      <div class="header">
        <button class="btn-primary" @click="openForm()">Thêm Camera Sau</button>
      </div>

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

    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Mã Camera</th>
            <th>Thông số</th>
            <th>Mô tả</th>
            <th>Trạng thái</th>
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="camera in cameras" :key="camera.id">
            <td>{{ camera.id }}</td>
            <td>{{ camera.maCamera }}</td>
            <td>{{ camera.thongSo }}</td>
            <td>{{ camera.moTa || '-' }}</td>
            <td>
              <span :class="camera.trangThai === 1 ? 'status-active' : 'status-inactive'">
                {{ camera.trangThai === 1 ? 'Hoạt động' : 'Không hoạt động' }}
              </span>
            </td>
            <td>
              <button class="btn-edit" @click="openForm(camera)">Sửa</button>
              <button class="btn-delete" @click="deleteCamera(camera.id)">Xóa</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import api from '@/services/api'
import ConfirmModal from '@/components/ConfirmModal.vue'
import FormModal from '@/components/FormModal.vue'
import Toast from '@/components/Toast.vue'
import PosHeader from '@/components/PosHeader.vue'

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

// Confirm modal state
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const pendingAction = ref<(() => void) | null>(null)

const cameraFields = [
  { key: 'maCamera', label: 'Mã Camera', type: 'text' as const, required: true },
  { key: 'thongSo', label: 'Thông số', type: 'text' as const, required: true },
  { key: 'moTa', label: 'Mô tả', type: 'textarea' as const },
  { key: 'trangThai', label: 'Trạng thái hoạt động', type: 'checkbox' as const }
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

onMounted(loadCameras)
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
  position: sticky;
  top: 0;
  background: white;
  z-index: 10;
  padding: 10px 0;
  border-bottom: 1px solid #e2e8f0;
}
.btn-primary {
  background: #007bff;
  color: white;
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.btn-secondary {
  background: #6c757d;
  color: white;
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.btn-edit {
  background: #28a745;
  color: white;
  padding: 4px 8px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 4px;
}
.btn-delete {
  background: #dc3545;
  color: white;
  padding: 4px 8px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}


.table-container {
  overflow-x: auto;
}
table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 20px;
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
}

.status-active {
  color: #28a745;
  font-weight: bold;
}

.status-inactive {
  color: #dc3545;
  font-weight: bold;
}
</style>
