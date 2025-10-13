<template>
  <div class="page">
    <PosHeader />
    
    <div class="content">
      <div class="header">
        <button class="btn-primary" @click="openForm()">Thêm Màu sắc</button>
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
      :title="editingMauSac ? 'Sửa Màu sắc' : 'Thêm Màu sắc'"
      :fields="mauSacFields"
      :initial-data="editingMauSac ? {
        maMau: editingMauSac.maMau,
        tenMau: editingMauSac.tenMau,
        moTa: editingMauSac.moTa || '',
        trangThai: editingMauSac.trangThai
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
            <th>Mã Màu</th>
            <th>Tên Màu</th>
            <th>Mô tả</th>
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="mauSac in mauSacs" :key="mauSac.id">
            <td>{{ mauSac.id }}</td>
            <td>{{ mauSac.maMau }}</td>
            <td>{{ mauSac.tenMau }}</td>
            <td>{{ mauSac.moTa || '-' }}</td>
            <td>
              <button class="btn-edit" @click="openForm(mauSac)">Sửa</button>
              <button class="btn-delete" @click="deleteMauSac(mauSac.id)">Xóa</button>
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

interface MauSac {
  id: number
  maMau: string
  tenMau: string
  moTa?: string
  ngayTao?: string
  ngayCapNhat?: string
  trangThai: number
}

const mauSacs = ref<MauSac[]>([])
const loading = ref(false)
const showForm = ref(false)
const editingMauSac = ref<MauSac | null>(null)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

// Confirm modal state
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const pendingAction = ref<(() => void) | null>(null)

const mauSacFields = [
  { key: 'maMau', label: 'Mã Màu', type: 'text' as const, required: true },
  { key: 'tenMau', label: 'Tên Màu', type: 'text' as const, required: true },
  { key: 'moTa', label: 'Mô tả', type: 'textarea' as const },
  { key: 'trangThai', label: 'Trạng thái hoạt động', type: 'checkbox' as const }
]

async function loadMauSacs() {
  loading.value = true
  try {
    const { data } = await api.get<MauSac[]>('/api/mau-sac')
    mauSacs.value = data
  } finally {
    loading.value = false
  }
}

function openForm(mauSac?: MauSac) {
  editingMauSac.value = mauSac || null
  showForm.value = true
}

async function handleFormSubmit(data: any) {
  try {
    if (editingMauSac.value) {
      await api.put(`/api/mau-sac/${editingMauSac.value.id}`, data)
      toastRef.value?.success('Thành công', 'Cập nhật màu sắc thành công!')
    } else {
      await api.post('/api/mau-sac', data)
      toastRef.value?.success('Thành công', 'Thêm màu sắc thành công!')
    }
    showForm.value = false
    await loadMauSacs()
  } catch (error: any) {
    console.error('Lỗi khi lưu:', error)
    if (error.response?.data) {
      toastRef.value?.error('Lỗi lưu Màu Sắc', error.response.data)
    } else {
      toastRef.value?.error('Lỗi lưu Màu Sắc', 'Có lỗi xảy ra khi lưu màu sắc')
    }
  }
}

function deleteMauSac(id: number) {
  const mauSac = mauSacs.value.find(m => m.id === id)
  confirmTitle.value = 'Xác nhận xóa Màu sắc'
  confirmMessage.value = `Bạn có chắc chắn muốn xóa màu sắc "${mauSac?.tenMau || 'này'}"? Hành động này không thể hoàn tác.`
  pendingAction.value = () => performDelete(id)
  showConfirmModal.value = true
}

async function performDelete(id: number) {
  try {
    await api.delete(`/api/mau-sac/${id}`)
    toastRef.value?.success('Thành công', 'Xóa màu sắc thành công!')
    await loadMauSacs()
  } catch (error: any) {
    console.error('Lỗi khi xóa:', error)
    // Hiển thị thông báo lỗi cho user
    if (error.response?.data) {
      // Backend trả về thông báo lỗi trực tiếp trong response.data
      toastRef.value?.error('Không thể xóa', error.response.data)
    } else {
      toastRef.value?.error('Lỗi xóa Màu Sắc', 'Có lỗi xảy ra khi xóa màu sắc')
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

onMounted(loadMauSacs)
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

.color-preview {
  width: 30px;
  height: 30px;
  border-radius: 4px;
  border: 1px solid #ddd;
  display: inline-block;
}
</style>
