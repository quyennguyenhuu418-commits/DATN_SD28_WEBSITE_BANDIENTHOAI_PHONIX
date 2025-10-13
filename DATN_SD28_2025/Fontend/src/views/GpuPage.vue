<template>
  <div class="page">
    <PosHeader />
    
    <div class="content">
      <div class="header">
        <button class="btn-primary" @click="openForm()">Thêm GPU</button>
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

    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Mã GPU</th>
            <th>Tên GPU</th>
            <th>Mô tả</th>
            <th>Trạng thái</th>
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="gpu in gpus" :key="gpu.id">
            <td>{{ gpu.id }}</td>
            <td>{{ gpu.maGpu }}</td>
            <td>{{ gpu.tenGpu }}</td>
            <td>{{ gpu.moTa || '-' }}</td>
            <td>
              <span :class="gpu.trangThai === 1 ? 'status-active' : 'status-inactive'">
                {{ gpu.trangThai === 1 ? 'Hoạt động' : 'Không hoạt động' }}
              </span>
            </td>
            <td>
              <button class="btn-edit" @click="openForm(gpu)">Sửa</button>
              <button class="btn-delete" @click="deleteGpu(gpu.id)">Xóa</button>
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

// Confirm modal state
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const pendingAction = ref<(() => void) | null>(null)

const gpuFields = [
  { key: 'maGpu', label: 'Mã GPU', type: 'text' as const, required: true },
  { key: 'tenGpu', label: 'Tên GPU', type: 'text' as const, required: true },
  { key: 'moTa', label: 'Mô tả', type: 'textarea' as const },
  { key: 'trangThai', label: 'Trạng thái hoạt động', type: 'checkbox' as const }
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

onMounted(loadGpus)
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
