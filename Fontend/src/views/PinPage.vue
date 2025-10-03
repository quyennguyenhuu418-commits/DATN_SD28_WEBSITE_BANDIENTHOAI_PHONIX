<template>
  <AdminTable
    :data="pins"
    :columns="columns"
    title="Danh Sách Pin"
    title-icon="🔋"
    entity-name="pin"
    search-placeholder="Tìm kiếm theo dung lượng pin..."
    @open-form="openForm"
    @delete-item="deletePin"
    @export-excel="exportExcel"
    @toggle-status="togglePinStatus"
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
    :title="editingPin ? 'Sửa Pin' : 'Thêm Pin'"
    :fields="pinFields"
    :initial-data="editingPin ? {
      maPin: editingPin.maPin,
      dungLuongPin: editingPin.dungLuongPin,
      congNgheSac: editingPin.congNgheSac || '',
      moTa: editingPin.moTa || '',
      trangThai: editingPin.trangThai
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

interface Pin {
  id: number
  maPin: string
  dungLuongPin: string
  congNgheSac?: string
  moTa?: string
  trangThai: number
}

const pins = ref<Pin[]>([])
const loading = ref(false)
const showForm = ref(false)
const editingPin = ref<Pin | null>(null)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

// Table columns configuration
const columns = [
  { key: 'maPin', label: 'Mã', class: 'code-col', type: 'code' as const },
  { key: 'dungLuongPin', label: 'Dung lượng', class: 'name-col' },
  { key: 'congNgheSac', label: 'Công nghệ sạc', class: 'desc-col' },
  { key: 'moTa', label: 'Mô tả', class: 'desc-col' },
  { key: 'trangThai', label: 'Trạng thái', class: 'status-col', type: 'status' as const }
]

// Confirm modal state
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const pendingAction = ref<(() => void) | null>(null)

const pinFields = [
  { key: 'maPin', label: 'Mã Pin', type: 'text' as const, required: true },
  { key: 'dungLuongPin', label: 'Dung lượng Pin', type: 'text' as const, required: true },
  { key: 'congNgheSac', label: 'Công nghệ sạc', type: 'text' as const },
  { key: 'moTa', label: 'Mô tả', type: 'textarea' as const },
  { key: 'trangThai', label: 'Hoạt động', type: 'checkbox' as const }
]

async function loadPins() {
  loading.value = true
  try {
    const { data } = await api.get<Pin[]>('/api/pin')
    pins.value = data
  } finally {
    loading.value = false
  }
}

function openForm(pin?: Pin) {
  editingPin.value = pin || null
  showForm.value = true
}

async function handleFormSubmit(data: any) {
  try {
    if (editingPin.value) {
      await api.put(`/api/pin/${editingPin.value.id}`, data)
      toastRef.value?.success('Thành công', 'Cập nhật pin thành công!')
    } else {
      await api.post('/api/pin', data)
      toastRef.value?.success('Thành công', 'Thêm pin thành công!')
    }
    showForm.value = false
    await loadPins()
  } catch (error: any) {
    console.error('Lỗi khi lưu:', error)
    if (error.response?.data) {
      toastRef.value?.error('Lỗi lưu Pin', error.response.data)
    } else {
      toastRef.value?.error('Lỗi lưu Pin', 'Có lỗi xảy ra khi lưu pin')
    }
  }
}

function deletePin(id: number) {
  const pin = pins.value.find(p => p.id === id)
  confirmTitle.value = 'Xác nhận xóa Pin'
  confirmMessage.value = `Bạn có chắc chắn muốn xóa pin "${pin?.maPin || 'này'}"? Hành động này không thể hoàn tác.`
  pendingAction.value = () => performDelete(id)
  showConfirmModal.value = true
}

async function performDelete(id: number) {
  try {
    await api.delete(`/api/pin/${id}`)
    toastRef.value?.success('Thành công', 'Xóa pin thành công!')
    await loadPins()
  } catch (error: any) {
    console.error('Lỗi khi xóa:', error)
    // Hiển thị thông báo lỗi cho user
    if (error.response?.data) {
      // Backend trả về thông báo lỗi trực tiếp trong response.data
      toastRef.value?.error('Không thể xóa', error.response.data)
    } else {
      toastRef.value?.error('Lỗi xóa Pin', 'Có lỗi xảy ra khi xóa pin')
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

async function testCreateInactivePin() {
  try {
    const testData = {
      maPin: 'TEST_INACTIVE_' + Date.now(),
      dungLuongPin: '5000mAh',
      congNgheSac: 'Fast Charge',
      moTa: 'Pin test không hoạt động',
      trangThai: 0 // Trạng thái không hoạt động
    }
    await api.post('/api/pin', testData)
    await loadPins()
    console.log('Đã tạo pin test không hoạt động')
  } catch (error) {
    console.error('Lỗi khi tạo pin test:', error)
  }
}

function exportExcel() {
  // TODO: Implement Excel export
  console.log('Export Excel for Pin')
}

onMounted(loadPins)
</script>

<style scoped>
@import '@/styles/admin-layout.css';
</style>
