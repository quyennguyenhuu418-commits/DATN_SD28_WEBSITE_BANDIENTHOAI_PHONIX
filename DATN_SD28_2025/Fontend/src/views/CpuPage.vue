<template>
  <div class="page">
    <PosHeader />
    
    <div class="content">
      <div class="header">
        <button class="btn-primary" @click="openForm()">Thêm CPU</button>
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

    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Mã CPU</th>
            <th>Tên CPU</th>
            <th>Mô tả</th>
            <th>Trạng thái</th>
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="cpu in cpus" :key="cpu.id">
            <td>{{ cpu.id }}</td>
            <td>{{ cpu.maCpu }}</td>
            <td>{{ cpu.tenCpu }}</td>
            <td>{{ cpu.moTa || '-' }}</td>
            <td>
              <span :class="cpu.trangThai === 1 ? 'status-active' : 'status-inactive'">
                {{ cpu.trangThai === 1 ? 'Hoạt động' : 'Không hoạt động' }}
              </span>
            </td>
            <td>
              <button class="btn-edit" @click="openForm(cpu)">Sửa</button>
              <button class="btn-delete" @click="deleteCpu(cpu.id)">Xóa</button>
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

// Confirm modal state
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const pendingAction = ref<(() => void) | null>(null)

const cpuFields = [
  { key: 'maCpu', label: 'Mã CPU', type: 'text' as const, required: true },
  { key: 'tenCpu', label: 'Tên CPU', type: 'text' as const, required: true },
  { key: 'moTa', label: 'Mô tả', type: 'textarea' as const },
  { key: 'trangThai', label: 'Trạng thái hoạt động', type: 'checkbox' as const }
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

onMounted(loadCpus)
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
