<template>
  <div class="page">
    <PosHeader />
    
    <div class="content">
      <div class="header">
        <button class="btn-primary" @click="openForm()">Thêm Hệ điều hành</button>
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
      :title="editingHeDieuHanh ? 'Sửa Hệ điều hành' : 'Thêm Hệ điều hành'"
      :fields="heDieuHanhFields"
      :initial-data="editingHeDieuHanh ? {
        maHeDieuHanh: editingHeDieuHanh.maHeDieuHanh,
        tenHeDieuHanh: editingHeDieuHanh.tenHeDieuHanh,
        moTa: editingHeDieuHanh.moTa || '',
        trangThai: editingHeDieuHanh.trangThai
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
            <th>Mã Hệ điều hành</th>
            <th>Tên Hệ điều hành</th>
            <th>Mô tả</th>
            <th>Trạng thái</th>
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="heDieuHanh in heDieuHanhs" :key="heDieuHanh.id">
            <td>{{ heDieuHanh.id }}</td>
            <td>{{ heDieuHanh.maHeDieuHanh }}</td>
            <td>{{ heDieuHanh.tenHeDieuHanh }}</td>
            <td>{{ heDieuHanh.moTa || '-' }}</td>
            <td>
              <span :class="heDieuHanh.trangThai === 1 ? 'status-active' : 'status-inactive'">
                {{ heDieuHanh.trangThai === 1 ? 'Hoạt động' : 'Không hoạt động' }}
              </span>
            </td>
            <td>
              <button class="btn-edit" @click="openForm(heDieuHanh)">Sửa</button>
              <button class="btn-delete" @click="deleteHeDieuHanh(heDieuHanh.id)">Xóa</button>
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

interface HeDieuHanh {
  id: number
  maHeDieuHanh: string
  tenHeDieuHanh: string
  moTa?: string
  trangThai: number
}

const heDieuHanhs = ref<HeDieuHanh[]>([])
const loading = ref(false)
const showForm = ref(false)
const editingHeDieuHanh = ref<HeDieuHanh | null>(null)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

// Confirm modal state
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const pendingAction = ref<(() => void) | null>(null)

const heDieuHanhFields = [
  { key: 'maHeDieuHanh', label: 'Mã Hệ điều hành', type: 'text' as const, required: true },
  { key: 'tenHeDieuHanh', label: 'Tên Hệ điều hành', type: 'text' as const, required: true },
  { key: 'moTa', label: 'Mô tả', type: 'textarea' as const },
  { key: 'trangThai', label: 'Trạng thái hoạt động', type: 'checkbox' as const }
]

async function loadHeDieuHanhs() {
  loading.value = true
  try {
    const { data } = await api.get<HeDieuHanh[]>('/api/he-dieu-hanh')
    heDieuHanhs.value = data
  } finally {
    loading.value = false
  }
}

function openForm(heDieuHanh?: HeDieuHanh) {
  editingHeDieuHanh.value = heDieuHanh || null
  showForm.value = true
}

async function handleFormSubmit(data: any) {
  try {
    if (editingHeDieuHanh.value) {
      await api.put(`/api/he-dieu-hanh/${editingHeDieuHanh.value.id}`, data)
      toastRef.value?.success('Thành công', 'Cập nhật hệ điều hành thành công!')
    } else {
      await api.post('/api/he-dieu-hanh', data)
      toastRef.value?.success('Thành công', 'Thêm hệ điều hành thành công!')
    }
    showForm.value = false
    await loadHeDieuHanhs()
  } catch (error: any) {
    console.error('Lỗi khi lưu:', error)
    if (error.response?.data) {
      toastRef.value?.error('Lỗi lưu Hệ Điều Hành', error.response.data)
    } else {
      toastRef.value?.error('Lỗi lưu Hệ Điều Hành', 'Có lỗi xảy ra khi lưu hệ điều hành')
    }
  }
}

function deleteHeDieuHanh(id: number) {
  const heDieuHanh = heDieuHanhs.value.find(h => h.id === id)
  confirmTitle.value = 'Xác nhận xóa Hệ điều hành'
  confirmMessage.value = `Bạn có chắc chắn muốn xóa hệ điều hành "${heDieuHanh?.tenHeDieuHanh || 'này'}"? Hành động này không thể hoàn tác.`
  pendingAction.value = () => performDelete(id)
  showConfirmModal.value = true
}

async function performDelete(id: number) {
  try {
    await api.delete(`/api/he-dieu-hanh/${id}`)
    toastRef.value?.success('Thành công', 'Xóa hệ điều hành thành công!')
    await loadHeDieuHanhs()
  } catch (error: any) {
    console.error('Lỗi khi xóa:', error)
    // Hiển thị thông báo lỗi cho user
    if (error.response?.data) {
      // Backend trả về thông báo lỗi trực tiếp trong response.data
      toastRef.value?.error('Không thể xóa', error.response.data)
    } else {
      toastRef.value?.error('Lỗi xóa Hệ Điều Hành', 'Có lỗi xảy ra khi xóa hệ điều hành')
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

onMounted(loadHeDieuHanhs)
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
