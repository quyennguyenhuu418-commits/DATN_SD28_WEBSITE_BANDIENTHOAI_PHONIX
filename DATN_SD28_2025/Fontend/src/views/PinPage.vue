<template>
  <div class="page">
    <PosHeader />
    
    <div class="content">
      <div class="header">
        <div>
          <button class="btn-primary" @click="openForm()">Thêm Pin</button>
          <button class="btn-secondary" @click="testCreateInactivePin" style="margin-left: 10px;">Test Tạo Pin Không Hoạt Động</button>
        </div>
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

    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Mã Pin</th>
            <th>Dung lượng Pin</th>
            <th>Công nghệ sạc</th>
            <th>Mô tả</th>
            <th>Trạng thái</th>
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="pin in pins" :key="pin.id">
            <td>{{ pin.id }}</td>
            <td>{{ pin.maPin }}</td>
            <td>{{ pin.dungLuongPin }}</td>
            <td>{{ pin.congNgheSac || '-' }}</td>
            <td>{{ pin.moTa || '-' }}</td>
            <td>
              <span :class="pin.trangThai === 1 ? 'status-active' : 'status-inactive'">
                {{ pin.trangThai === 1 ? 'Hoạt động' : 'Không hoạt động' }}
              </span>
            </td>
            <td>
              <button class="btn-edit" @click="openForm(pin)">Sửa</button>
              <button class="btn-delete" @click="deletePin(pin.id)">Xóa</button>
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
  { key: 'trangThai', label: 'Trạng thái hoạt động', type: 'checkbox' as const }
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

onMounted(loadPins)
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
