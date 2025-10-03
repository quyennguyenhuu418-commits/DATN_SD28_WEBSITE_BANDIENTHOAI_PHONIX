<template>
  <AdminTable
    :data="cpus"
    :columns="columns"
    title="Danh Sách CPU"
    title-icon="⚡"
    entity-name="CPU"
    search-placeholder="Tìm kiếm theo tên CPU..."
    @open-form="openForm"
    @delete-item="deleteCpu"
    @export-excel="exportExcel"
    @toggle-status="toggleCpuStatus"
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
    :title="editingCpu ? 'Sửa CPU' : 'Thêm CPU'"
    :fields="cpuFields"
    :initial-data="editingCpu ? {
      maCpu: editingCpu.maCpu,
      tenCpu: editingCpu.tenCpu,
      moTa: editingCpu.moTa || '',
      trangThai: editingCpu.trangThai
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

interface Cpu {
  id: number
  maCpu: string
  tenCpu: string
  moTa?: string
  trangThai: number
}

const cpus = ref<Cpu[]>([])
const loading = ref(false)
const showForm = ref(false)
const editingCpu = ref<Cpu | null>(null)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

// Table columns configuration
const columns = [
  { key: 'maCpu', label: 'Mã', class: 'code-col', type: 'code' as const },
  { key: 'tenCpu', label: 'Tên CPU', class: 'name-col' },
  { key: 'moTa', label: 'Mô tả', class: 'desc-col' },
  { key: 'trangThai', label: 'Trạng thái', class: 'status-col', type: 'status' as const }
]

// Confirm modal state
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const pendingAction = ref<(() => void) | null>(null)

const cpuFields = [
  { key: 'maCpu', label: 'Mã CPU', type: 'text' as const, required: true },
  { key: 'tenCpu', label: 'Tên CPU', type: 'text' as const, required: true },
  { key: 'moTa', label: 'Mô tả', type: 'textarea' as const },
  { key: 'trangThai', label: 'Hoạt động', type: 'checkbox' as const }
]

async function loadCpus() {
  loading.value = true
  try {
    const { data } = await api.get<Cpu[]>('/api/cpu')
    cpus.value = data
  } finally {
    loading.value = false
  }
}

function openForm(cpu?: Cpu) {
  editingCpu.value = cpu || null
  showForm.value = true
}

async function handleFormSubmit(data: any) {
  try {
    if (editingCpu.value) {
      await api.put(`/api/cpu/${editingCpu.value.id}`, data)
      toastRef.value?.success('Thành công', 'Cập nhật CPU thành công!')
    } else {
      await api.post('/api/cpu', data)
      toastRef.value?.success('Thành công', 'Thêm CPU thành công!')
    }
    showForm.value = false
    await loadCpus()
  } catch (error: any) {
    console.error('Lỗi khi lưu:', error)
    if (error.response?.data) {
      toastRef.value?.error('Lỗi lưu CPU', error.response.data)
    } else {
      toastRef.value?.error('Lỗi lưu CPU', 'Có lỗi xảy ra khi lưu CPU')
    }
  }
}

function deleteCpu(id: number) {
  const cpu = cpus.value.find(c => c.id === id)
  confirmTitle.value = 'Xác nhận xóa CPU'
  confirmMessage.value = `Bạn có chắc chắn muốn xóa CPU "${cpu?.tenCpu || 'này'}"? Hành động này không thể hoàn tác.`
  pendingAction.value = () => performDelete(id)
  showConfirmModal.value = true
}

async function performDelete(id: number) {
  try {
    await api.delete(`/api/cpu/${id}`)
    toastRef.value?.success('Thành công', 'Xóa CPU thành công!')
    await loadCpus()
  } catch (error: any) {
    console.error('Lỗi khi xóa:', error)
    // Hiển thị thông báo lỗi cho user
    if (error.response?.data) {
      // Backend trả về thông báo lỗi trực tiếp trong response.data
      toastRef.value?.error('Không thể xóa', error.response.data)
    } else {
      toastRef.value?.error('Lỗi xóa CPU', 'Có lỗi xảy ra khi xóa CPU')
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

function exportExcel() {
  // TODO: Implement Excel export
  console.log('Export Excel for CPU')
}

onMounted(loadCpus)
</script>

<style scoped>
@import '@/styles/admin-layout.css';
</style>
