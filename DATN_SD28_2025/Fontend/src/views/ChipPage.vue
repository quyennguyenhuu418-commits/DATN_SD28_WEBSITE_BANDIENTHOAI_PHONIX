<template>
  <div class="page">
    <PosHeader />
    
    <div class="content">
      <div class="header">
        <button class="btn-primary" @click="openForm()">Thêm Chip</button>
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
      :title="editingChip ? 'Sửa Chip' : 'Thêm Chip'"
      :fields="chipFields"
      :initial-data="editingChip ? {
        maChip: editingChip.maChip,
        tenChip: editingChip.tenChip,
        moTa: editingChip.moTa || '',
        trangThai: editingChip.trangThai
      } : undefined"
      @submit="handleFormSubmit"
      @cancel="showForm = false"
    />

    <Toast ref="toastRef" />

    <div class="table-container">
      <div v-if="loading" class="p-4 text-gray-500">Đang tải...</div>
      <div v-else-if="!chips.length" class="p-4 text-gray-500">Chưa có dữ liệu Chip.</div>
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Mã Chip</th>
            <th>Tên Chip</th>
            <th>Mô tả</th>
            <th>Trạng thái</th>
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="chip in chips" :key="chip.id">
            <td>{{ chip.id }}</td>
            <td>{{ chip.maChip }}</td>
            <td>{{ chip.tenChip }}</td>
            <td>{{ chip.moTa || '-' }}</td>
            <td>
              <span :class="chip.trangThai === 1 ? 'status-active' : 'status-inactive'">
                {{ chip.trangThai === 1 ? 'Hoạt động' : 'Không hoạt động' }}
              </span>
            </td>
            <td>
              <button class="btn-edit" @click="openForm(chip)">Sửa</button>
              <button class="btn-delete" @click="deleteChip(chip.id)">Xóa</button>
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

interface Chip {
  id: number
  maChip: string
  tenChip: string
  moTa?: string
  ngayTao?: string
  ngayCapNhat?: string
  trangThai: number
}

const chips = ref<Chip[]>([])
const loading = ref(false)
const showForm = ref(false)
const editingChip = ref<Chip | null>(null)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

// Confirm modal state
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const pendingAction = ref<(() => void) | null>(null)

const chipFields = [
  { key: 'maChip', label: 'Mã Chip', type: 'text' as const, required: true },
  { key: 'tenChip', label: 'Tên Chip', type: 'text' as const, required: true },
  { key: 'moTa', label: 'Mô tả', type: 'textarea' as const },
  { key: 'trangThai', label: 'Trạng thái hoạt động', type: 'checkbox' as const }
]

async function loadChips() {
  loading.value = true
  try {
    const { data } = await api.get<Chip[]>('/api/chip')
    chips.value = data
  } finally {
    loading.value = false
  }
}

function openForm(chip?: Chip) {
  editingChip.value = chip || null
  showForm.value = true
}

async function handleFormSubmit(data: any) {
  try {
    if (editingChip.value) {
      await api.put(`/api/chip/${editingChip.value.id}`, data)
      toastRef.value?.success('Thành công', 'Cập nhật chip thành công!')
    } else {
      await api.post('/api/chip', data)
      toastRef.value?.success('Thành công', 'Thêm chip thành công!')
    }
    showForm.value = false
    await loadChips()
  } catch (error: any) {
    console.error('Lỗi khi lưu:', error)
    if (error.response?.data) {
      toastRef.value?.error('Lỗi lưu Chip', error.response.data)
    } else {
      toastRef.value?.error('Lỗi lưu Chip', 'Có lỗi xảy ra khi lưu chip')
    }
  }
}

function deleteChip(id: number) {
  const chip = chips.value.find(c => c.id === id)
  confirmTitle.value = 'Xác nhận xóa Chip'
  confirmMessage.value = `Bạn có chắc chắn muốn xóa chip "${chip?.tenChip || 'này'}"? Hành động này không thể hoàn tác.`
  pendingAction.value = () => performDelete(id)
  showConfirmModal.value = true
}

async function performDelete(id: number) {
  try {
    await api.delete(`/api/chip/${id}`)
    toastRef.value?.success('Thành công', 'Xóa chip thành công!')
    await loadChips()
  } catch (error: any) {
    console.error('Lỗi khi xóa:', error)
    // Hiển thị thông báo lỗi cho user
    if (error.response?.data) {
      // Backend trả về thông báo lỗi trực tiếp trong response.data
      toastRef.value?.error('Không thể xóa', error.response.data)
    } else {
      toastRef.value?.error('Lỗi xóa Chip', 'Có lỗi xảy ra khi xóa chip')
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

onMounted(loadChips)
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
