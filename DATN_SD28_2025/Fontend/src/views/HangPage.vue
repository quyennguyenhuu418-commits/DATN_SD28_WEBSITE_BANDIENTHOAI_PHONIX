<template>
  <div class="page">
    <PosHeader />
    
    <div class="content">
      <div class="header">
        <button class="btn-primary" @click="openForm()">Thêm Hãng</button>
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
      :title="editingHang ? 'Sửa Hãng' : 'Thêm Hãng'"
      :fields="hangFields"
      :initial-data="editingHang ? {
        ten: editingHang.ten,
        xuatXu: editingHang.xuatXu || '',
        moTa: editingHang.moTa || '',
        trangThai: editingHang.trangThai
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
            <th>Tên Hãng</th>
            <th>Xuất xứ</th>
            <th>Mô tả</th>
            <th>Trạng thái</th>
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="hang in hangs" :key="hang.id">
            <td>{{ hang.id }}</td>
            <td>{{ hang.ten }}</td>
            <td>{{ hang.xuatXu || '-' }}</td>
            <td>{{ hang.moTa || '-' }}</td>
            <td>
              <span :class="hang.trangThai === 1 ? 'status-active' : 'status-inactive'">
                {{ hang.trangThai === 1 ? 'Hoạt động' : 'Không hoạt động' }}
              </span>
            </td>
            <td>
              <button class="btn-edit" @click="openForm(hang)">Sửa</button>
              <button class="btn-delete" @click="deleteHang(hang.id)">Xóa</button>
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

interface Hang {
  id: number
  ten: string
  xuatXu?: string
  moTa?: string
  ngayTao?: string
  ngayCapNhat?: string
  trangThai: number
}

const hangs = ref<Hang[]>([])
const loading = ref(false)
const showForm = ref(false)
const editingHang = ref<Hang | null>(null)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

// Confirm modal state
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const pendingAction = ref<(() => void) | null>(null)

const hangFields = [
  { key: 'ten', label: 'Tên Hãng', type: 'text' as const, required: true },
  { key: 'xuatXu', label: 'Xuất xứ', type: 'text' as const },
  { key: 'moTa', label: 'Mô tả', type: 'textarea' as const },
  { key: 'trangThai', label: 'Trạng thái hoạt động', type: 'checkbox' as const }
]

async function loadHangs() {
  loading.value = true
  try {
    const { data } = await api.get<Hang[]>('/api/hang')
    hangs.value = data
  } finally {
    loading.value = false
  }
}

function openForm(hang?: Hang) {
  editingHang.value = hang || null
  showForm.value = true
}

async function handleFormSubmit(data: any) {
  try {
    if (editingHang.value) {
      await api.put(`/api/hang/${editingHang.value.id}`, data)
      toastRef.value?.success('Thành công', 'Cập nhật hãng thành công!')
    } else {
      await api.post('/api/hang', data)
      toastRef.value?.success('Thành công', 'Thêm hãng thành công!')
    }
    showForm.value = false
    await loadHangs()
  } catch (error: any) {
    console.error('Lỗi khi lưu:', error)
    if (error.response?.data) {
      toastRef.value?.error('Lỗi lưu Hãng', error.response.data)
    } else {
      toastRef.value?.error('Lỗi lưu Hãng', 'Có lỗi xảy ra khi lưu hãng')
    }
  }
}

function deleteHang(id: number) {
  const hang = hangs.value.find(h => h.id === id)
  confirmTitle.value = 'Xác nhận xóa Hãng'
  confirmMessage.value = `Bạn có chắc chắn muốn xóa hãng "${hang?.ten || 'này'}"? Hành động này không thể hoàn tác.`
  pendingAction.value = () => performDelete(id)
  showConfirmModal.value = true
}

async function performDelete(id: number) {
  try {
    await api.delete(`/api/hang/${id}`)
    toastRef.value?.success('Thành công', 'Xóa hãng thành công!')
    await loadHangs()
  } catch (error: any) {
    console.error('Lỗi khi xóa:', error)
    // Hiển thị thông báo lỗi cho user
    if (error.response?.data) {
      // Backend trả về thông báo lỗi trực tiếp trong response.data
      toastRef.value?.error('Không thể xóa', error.response.data)
    } else {
      toastRef.value?.error('Lỗi xóa Hãng', 'Có lỗi xảy ra khi xóa hãng')
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

onMounted(loadHangs)
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
