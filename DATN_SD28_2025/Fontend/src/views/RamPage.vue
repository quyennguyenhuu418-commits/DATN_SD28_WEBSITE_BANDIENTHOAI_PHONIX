<template>
  <div class="page">
    <PosHeader />
    
    <div class="content">
      <div class="header">
        <button class="btn-primary" @click="openForm()">Thêm RAM</button>
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
      :title="editingRam ? 'Sửa RAM' : 'Thêm RAM'"
      :fields="ramFields"
      :initial-data="editingRam ? {
        maRam: editingRam.maRam,
        tenRam: editingRam.tenRam,
        moTa: editingRam.moTa || '',
        trangThai: editingRam.trangThai
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
            <th>Mã RAM</th>
            <th>Tên RAM</th>
            <th>Mô tả</th>
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="ram in rams" :key="ram.id">
            <td>{{ ram.id }}</td>
            <td>{{ ram.maRam }}</td>
            <td>{{ ram.tenRam }}</td>
            <td>{{ ram.moTa || '-' }}</td>
            <td>
              <button class="btn-edit" @click="openForm(ram)">Sửa</button>
              <button class="btn-delete" @click="deleteRam(ram.id)">Xóa</button>
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

interface Ram {
  id: number
  maRam: string
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

const ramFields = [
  { key: 'maRam', label: 'Mã RAM', type: 'text' as const, required: true },
  { key: 'tenRam', label: 'Tên RAM', type: 'text' as const, required: true },
  { key: 'moTa', label: 'Mô tả', type: 'textarea' as const },
  { key: 'trangThai', label: 'Trạng thái hoạt động', type: 'checkbox' as const }
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
    // Hiển thị thông báo lỗi cho user
    if (error.response?.data) {
      // Backend trả về thông báo lỗi trực tiếp trong response.data
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

onMounted(loadRams)
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
</style>
