<template>
  <AdminTable
    :data="cpus"
    :columns="columns"
    title="Danh Sách CPU"
    titleIcon="⚡"
    entityName="CPU"
    searchPlaceholder="Tìm kiếm theo tên CPU..."
    @openForm="openForm"
    @exportExcel="exportExcel"
    @toggleStatus="toggleCpuStatus"
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

async function toggleCpuStatus(cpu: Cpu) {
  try {
    const newStatus = (cpu.trangThai || 0) === 1 ? 0 : 1
    await api.put(`/api/cpu/${cpu.id}/status`, { trangThai: newStatus })
    
    // Update local data
    const index = cpus.value.findIndex(c => c.id === cpu.id)
    if (index !== -1) {
      cpus.value[index].trangThai = newStatus
    }
    
    const statusText = newStatus === 1 ? 'Hoạt động' : 'Ngừng hoạt động'
    const message = newStatus === 1 
      ? `Đã chuyển CPU "${cpu.tenCpu}" sang trạng thái <span style="color: #28a745; font-weight: bold;">${statusText}</span>`
      : `Đã chuyển CPU "${cpu.tenCpu}" sang trạng thái <span style="color: #dc3545; font-weight: bold;">${statusText}</span>`
    toastRef.value?.success('Thành công', message)
  } catch (error: any) {
    console.error('Lỗi khi cập nhật trạng thái:', error)
    toastRef.value?.error('Lỗi cập nhật', 'Không thể cập nhật trạng thái CPU')
  }
}

async function exportExcel() {
  try {
    const response = await api.get('/api/cpu/export', { responseType: 'blob' })
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', 'danh_sach_cpu.xlsx')
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
    toastRef.value?.success('Thành công', 'Xuất Excel thành công!')
  } catch (error: any) {
    console.error('Lỗi khi xuất Excel:', error)
    toastRef.value?.error('Lỗi xuất Excel', 'Có lỗi xảy ra khi xuất file Excel')
  }
}

onMounted(loadCpus)
</script>

<style scoped>
@import '@/styles/admin-layout.css';
</style>
