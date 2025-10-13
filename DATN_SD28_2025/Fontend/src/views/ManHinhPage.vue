<template>
  <div class="page">
    <PosHeader />
    
    <div class="content">
      <div class="header">
        <button class="btn-primary" @click="openForm()">Thêm Màn hình</button>
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
      :title="editingManHinh ? 'Sửa Màn hình' : 'Thêm Màn hình'"
      :fields="manHinhFields"
      :initial-data="editingManHinh ? {
        maManHinh: editingManHinh.maManHinh,
        kichThuoc: editingManHinh.kichThuoc,
        congNghe: editingManHinh.congNghe || '',
        doPhanGiai: editingManHinh.doPhanGiai,
        tanSoQuet: editingManHinh.tanSoQuet || '',
        kieuManHinh: editingManHinh.kieuManHinh || '',
        moTa: editingManHinh.moTa || '',
        trangThai: editingManHinh.trangThai
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
            <th>Mã Màn hình</th>
            <th>Kích thước</th>
            <th>Độ phân giải</th>
            <th>Công nghệ</th>
            <th>Tần số quét</th>
            <th>Kiểu màn hình</th>
            <th>Mô tả</th>
            <th>Trạng thái</th>
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="manHinh in manHinhs" :key="manHinh.id">
            <td>{{ manHinh.id }}</td>
            <td>{{ manHinh.maManHinh }}</td>
            <td>{{ manHinh.kichThuoc }}</td>
            <td>{{ manHinh.doPhanGiai }}</td>
            <td>{{ manHinh.congNghe || '-' }}</td>
            <td>{{ manHinh.tanSoQuet || '-' }}</td>
            <td>{{ manHinh.kieuManHinh || '-' }}</td>
            <td>{{ manHinh.moTa || '-' }}</td>
            <td>
              <span :class="manHinh.trangThai === 1 ? 'status-active' : 'status-inactive'">
                {{ manHinh.trangThai === 1 ? 'Hoạt động' : 'Không hoạt động' }}
              </span>
            </td>
            <td>
              <button class="btn-edit" @click="openForm(manHinh)">Sửa</button>
              <button class="btn-delete" @click="deleteManHinh(manHinh.id)">Xóa</button>
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

interface ManHinh {
  id: number
  maManHinh: string
  kichThuoc: string
  congNghe?: string
  doPhanGiai: string
  tanSoQuet?: string
  kieuManHinh?: string
  moTa?: string
  ngayTao?: string
  ngayCapNhat?: string
  trangThai: number
}

const manHinhs = ref<ManHinh[]>([])
const loading = ref(false)
const showForm = ref(false)
const editingManHinh = ref<ManHinh | null>(null)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

// Confirm modal state
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const pendingAction = ref<(() => void) | null>(null)

const manHinhFields = [
  { key: 'maManHinh', label: 'Mã Màn hình', type: 'text' as const, required: true },
  { key: 'kichThuoc', label: 'Kích thước', type: 'text' as const, required: true },
  { key: 'congNghe', label: 'Công nghệ', type: 'text' as const },
  { key: 'doPhanGiai', label: 'Độ phân giải', type: 'text' as const, required: true },
  { key: 'tanSoQuet', label: 'Tần số quét', type: 'text' as const },
  { key: 'kieuManHinh', label: 'Kiểu màn hình', type: 'text' as const },
  { key: 'moTa', label: 'Mô tả', type: 'textarea' as const },
  { key: 'trangThai', label: 'Trạng thái hoạt động', type: 'checkbox' as const }
]

async function loadManHinhs() {
  loading.value = true
  try {
    const { data } = await api.get<ManHinh[]>('/api/man-hinh')
    manHinhs.value = data
  } finally {
    loading.value = false
  }
}

function openForm(manHinh?: ManHinh) {
  editingManHinh.value = manHinh || null
  showForm.value = true
}

async function handleFormSubmit(data: any) {
  try {
    if (editingManHinh.value) {
      await api.put(`/api/man-hinh/${editingManHinh.value.id}`, data)
      toastRef.value?.success('Thành công', 'Cập nhật màn hình thành công!')
    } else {
      await api.post('/api/man-hinh', data)
      toastRef.value?.success('Thành công', 'Thêm màn hình thành công!')
    }
    showForm.value = false
    await loadManHinhs()
  } catch (error: any) {
    console.error('Lỗi khi lưu:', error)
    if (error.response?.data) {
      toastRef.value?.error('Lỗi lưu Màn Hình', error.response.data)
    } else {
      toastRef.value?.error('Lỗi lưu Màn Hình', 'Có lỗi xảy ra khi lưu màn hình')
    }
  }
}

function deleteManHinh(id: number) {
  const manHinh = manHinhs.value.find(m => m.id === id)
  confirmTitle.value = 'Xác nhận xóa Màn hình'
  confirmMessage.value = `Bạn có chắc chắn muốn xóa màn hình "${manHinh?.kichThuoc || 'này'}"? Hành động này không thể hoàn tác.`
  pendingAction.value = () => performDelete(id)
  showConfirmModal.value = true
}

async function performDelete(id: number) {
  try {
    await api.delete(`/api/man-hinh/${id}`)
    toastRef.value?.success('Thành công', 'Xóa màn hình thành công!')
    await loadManHinhs()
  } catch (error: any) {
    console.error('Lỗi khi xóa:', error)
    // Hiển thị thông báo lỗi cho user
    if (error.response?.data) {
      // Backend trả về thông báo lỗi trực tiếp trong response.data
      toastRef.value?.error('Không thể xóa', error.response.data)
    } else {
      toastRef.value?.error('Lỗi xóa Màn Hình', 'Có lỗi xảy ra khi xóa màn hình')
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

onMounted(loadManHinhs)
</script>

<style scoped>
.page {
  padding: 20px;
  padding-top: 100px; /* Bù đắp cho PosHeader cố định */
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
