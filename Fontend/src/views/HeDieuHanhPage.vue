<template>
  <AdminTable
    :data="heDieuHanhs"
    :columns="columns"
    title="Danh Sách Hệ Điều Hành"
    title-icon="💻"
    entity-name="hệ điều hành"
    search-placeholder="Tìm kiếm theo tên hệ điều hành..."
    @open-form="openForm"
    @delete-item="deleteHeDieuHanh"
    @export-excel="exportExcel"
    @toggle-status="toggleHeDieuHanhStatus"
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
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import api from '@/services/api'
import ConfirmModal from '@/components/ConfirmModal.vue'
import FormModal from '@/components/FormModal.vue'
import Toast from '@/components/Toast.vue'
import AdminTable from '@/components/AdminTable.vue'

interface HeDieuHanh {
  id: number
  maHeDieuHanh: string
  tenHeDieuHanh: string
  moTa?: string
  ngayTao?: string
  ngayCapNhat?: string
  trangThai: number
}

const heDieuHanhs = ref<HeDieuHanh[]>([])
const loading = ref(false)
const showForm = ref(false)
const editingHeDieuHanh = ref<HeDieuHanh | null>(null)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

// Table columns configuration
const columns = [
  { key: 'maHeDieuHanh', label: 'Mã', class: 'code-col', type: 'code' as const },
  { key: 'tenHeDieuHanh', label: 'Tên Hệ điều hành', class: 'name-col' },
  { key: 'moTa', label: 'Mô tả', class: 'desc-col' },
  { key: 'trangThai', label: 'Trạng thái', class: 'status-col', type: 'status' as const }
]

// Confirm modal state
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const pendingAction = ref<(() => void) | null>(null)

const heDieuHanhFields = [
  { key: 'maHeDieuHanh', label: 'Mã Hệ điều hành', type: 'text' as const, required: true },
  { key: 'tenHeDieuHanh', label: 'Tên Hệ điều hành', type: 'text' as const, required: true },
  { key: 'moTa', label: 'Mô tả', type: 'textarea' as const },
  { key: 'trangThai', label: 'Hoạt động', type: 'checkbox' as const }
]

async function loadHeDieuHanhs() {
  loading.value = true
  try {
    const response = await api.get('/api/he-dieu-hanh')
    heDieuHanhs.value = response.data
  } catch (error) {
    console.error('Lỗi khi tải danh sách hệ điều hành:', error)
    showToastMessage('error', 'Lỗi', 'Không thể tải danh sách hệ điều hành')
  } finally {
    loading.value = false
  }
}

function openForm(heDieuHanh?: HeDieuHanh) {
  if (heDieuHanh) {
    editingHeDieuHanh.value = { ...heDieuHanh }
  } else {
    editingHeDieuHanh.value = {
      id: 0,
      maHeDieuHanh: '',
      tenHeDieuHanh: '',
      moTa: '',
      trangThai: 1
    }
  }
  showForm.value = true
}

async function handleFormSubmit(formData: any) {
  try {
    if (editingHeDieuHanh.value?.id) {
      // Update existing
      await api.put(`/api/he-dieu-hanh/${editingHeDieuHanh.value.id}`, formData)
      showToastMessage('success', 'Thành công', 'Cập nhật hệ điều hành thành công')
    } else {
      // Create new
      await api.post('/api/he-dieu-hanh', formData)
      showToastMessage('success', 'Thành công', 'Thêm hệ điều hành thành công')
    }
    showForm.value = false
    await loadHeDieuHanhs()
  } catch (error) {
    console.error('Lỗi khi lưu hệ điều hành:', error)
    showToastMessage('error', 'Lỗi', 'Không thể lưu hệ điều hành')
  }
}

function deleteHeDieuHanh(id: number) {
  const heDieuHanh = heDieuHanhs.value.find(h => h.id === id)
  if (heDieuHanh) {
    confirmTitle.value = 'Xác nhận xóa'
    confirmMessage.value = `Bạn có chắc chắn muốn xóa hệ điều hành "${heDieuHanh.tenHeDieuHanh}"?`
    pendingAction.value = () => performDelete(id)
    showConfirmModal.value = true
  }
}

async function performDelete(id: number) {
  try {
    await api.delete(`/api/he-dieu-hanh/${id}`)
    showToastMessage('success', 'Thành công', 'Xóa hệ điều hành thành công')
    await loadHeDieuHanhs()
  } catch (error) {
    console.error('Lỗi khi xóa hệ điều hành:', error)
    showToastMessage('error', 'Lỗi', 'Không thể xóa hệ điều hành')
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

async function toggleHeDieuHanhStatus(item: any) {
  try {
    const newStatus = item.trangThai === 1 ? 0 : 1
    await api.put(`/api/he-dieu-hanh/${item.id}/status`, { trangThai: newStatus })
    
    // Update local data
    const index = heDieuHanhs.value.findIndex(i => i.id === item.id)
    if (index !== -1) {
      heDieuHanhs.value[index].trangThai = newStatus
    }
  } catch (error: any) {
    console.error('Lỗi khi cập nhật trạng thái:', error)
    if (error.response?.data) {
      showToastMessage('error', 'Lỗi cập nhật trạng thái', error.response.data)
    } else {
      showToastMessage('error', 'Lỗi cập nhật trạng thái', 'Có lỗi xảy ra khi cập nhật trạng thái')
    }
  }
}

function exportExcel() {
  // TODO: Implement Excel export
  console.log('Export Excel for HeDieuHanh')
}

function showToastMessage(type: 'success' | 'error' | 'warning' | 'info', title: string, message: string) {
  if (toastRef.value) {
    toastRef.value[type](title, message, 3000)
  }
}

onMounted(loadHeDieuHanhs)
</script>

<style scoped>
@import '@/styles/admin-layout.css';
</style>