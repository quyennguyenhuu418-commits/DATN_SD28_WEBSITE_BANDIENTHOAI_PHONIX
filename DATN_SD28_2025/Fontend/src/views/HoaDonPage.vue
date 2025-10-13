<script setup lang="ts">
import { onMounted, ref } from 'vue'
import api from '@/services/api'
import PosHeader from '@/components/PosHeader.vue'

interface HoaDon {
  id: number
  maHoaDon: string
  khachHang?: { hoTen: string; soDienThoai: string }
  nhanVien?: { hoTen: string }
  tongTien: number
  trangThai: string
  ngayTao: string
  phuongThucThanhToan?: string
}

const hoaDons = ref<HoaDon[]>([])
const loading = ref(false)
const showForm = ref(false)
const editingHoaDon = ref<HoaDon | null>(null)

const formData = ref({
  maHoaDon: '',
  idKhachHang: null as number | null,
  idNhanVien: null as number | null,
  tongTien: 0,
  trangThai: 'Chưa thanh toán',
  phuongThucThanhToan: '',
})

async function loadHoaDons() {
  loading.value = true
  try {
    const { data } = await api.get<HoaDon[]>('/api/hoa-don')
    hoaDons.value = data
  } finally {
    loading.value = false
  }
}

function openForm(hoaDon?: HoaDon) {
  if (hoaDon) {
    editingHoaDon.value = hoaDon
    formData.value = {
      maHoaDon: hoaDon.maHoaDon,
      idKhachHang: hoaDon.khachHang?.id || null,
      idNhanVien: hoaDon.nhanVien?.id || null,
      tongTien: hoaDon.tongTien,
      trangThai: hoaDon.trangThai,
      phuongThucThanhToan: hoaDon.phuongThucThanhToan || '',
    }
  } else {
    editingHoaDon.value = null
    formData.value = {
      maHoaDon: '',
      idKhachHang: null,
      idNhanVien: null,
      tongTien: 0,
      trangThai: 'Chưa thanh toán',
      phuongThucThanhToan: '',
    }
  }
  showForm.value = true
}

async function save() {
  try {
    if (editingHoaDon.value) {
      await api.put(`/api/hoa-don/${editingHoaDon.value.id}`, formData.value)
    } else {
      await api.post('/api/hoa-don', formData.value)
    }
    showForm.value = false
    await loadHoaDons()
  } catch (error) {
    console.error('Lỗi khi lưu:', error)
  }
}

async function updateTrangThai(id: number, trangThai: string) {
  try {
    await api.put(`/api/hoa-don/${id}/trang-thai?trangThai=${trangThai}`)
    await loadHoaDons()
  } catch (error) {
    console.error('Lỗi khi cập nhật trạng thái:', error)
  }
}

async function deleteHoaDon(id: number) {
  if (confirm('Bạn có chắc muốn xóa hóa đơn này?')) {
    try {
      await api.delete(`/api/hoa-don/${id}`)
      await loadHoaDons()
    } catch (error) {
      console.error('Lỗi khi xóa:', error)
    }
  }
}

function getStatusClass(trangThai: string) {
  const statusMap: Record<string, string> = {
    'Chưa thanh toán': 'status-chua-thanh-toan',
    'Đã thanh toán': 'status-da-thanh-toan',
    'Đang giao hàng': 'status-dang-giao-hang',
    'Hoàn thành': 'status-hoan-thanh',
    Hủy: 'status-huy',
  }
  return statusMap[trangThai] || ''
}

onMounted(loadHoaDons)
</script>

<template>
  <div class="page">
    <PosHeader />
    
    <div class="content">
      <div class="header">
        <h1>Quản lý Hóa đơn</h1>
        <button class="btn-primary" @click="openForm()">Thêm hóa đơn</button>
      </div>

    <div v-if="showForm" class="modal">
      <div class="modal-content">
        <h2>{{ editingHoaDon ? 'Sửa hóa đơn' : 'Thêm hóa đơn' }}</h2>
        <form @submit.prevent="save">
          <div class="form-group">
            <label>Mã hóa đơn:</label>
            <input v-model="formData.maHoaDon" required />
          </div>
          <div class="form-group">
            <label>Tổng tiền:</label>
            <input v-model.number="formData.tongTien" type="number" required />
          </div>
          <div class="form-group">
            <label>Trạng thái:</label>
            <select v-model="formData.trangThai">
              <option value="Chưa thanh toán">Chưa thanh toán</option>
              <option value="Đã thanh toán">Đã thanh toán</option>
              <option value="Đang giao hàng">Đang giao hàng</option>
              <option value="Hoàn thành">Hoàn thành</option>
              <option value="Hủy">Hủy</option>
            </select>
          </div>
          <div class="form-group">
            <label>Phương thức thanh toán:</label>
            <input v-model="formData.phuongThucThanhToan" />
          </div>
          <div class="form-actions">
            <button type="submit" class="btn-primary">Lưu</button>
            <button type="button" @click="showForm = false" class="btn-secondary">Hủy</button>
          </div>
        </form>
      </div>
    </div>

    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Mã HD</th>
            <th>Khách hàng</th>
            <th>Tổng tiền</th>
            <th>Trạng thái</th>
            <th>Ngày tạo</th>
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="hd in hoaDons" :key="hd.id">
            <td>{{ hd.id }}</td>
            <td>{{ hd.maHoaDon }}</td>
            <td>{{ hd.khachHang?.hoTen || '-' }}</td>
            <td>{{ hd.tongTien.toLocaleString() }} VNĐ</td>
            <td>
              <span :class="getStatusClass(hd.trangThai)">
                {{ hd.trangThai }}
              </span>
            </td>
            <td>{{ new Date(hd.ngayTao).toLocaleDateString() }}</td>
            <td>
              <button class="btn-edit" @click="openForm(hd)">Sửa</button>
              <button class="btn-success" @click="updateTrangThai(hd.id, 'Đã thanh toán')">
                Thanh toán
              </button>
              <button class="btn-delete" @click="deleteHoaDon(hd.id)">Xóa</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    </div>
  </div>
</template>

<style scoped>
.page {
  padding: 20px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
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
.btn-success {
  background: #17a2b8;
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

.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}
.modal-content {
  background: white;
  padding: 20px;
  border-radius: 8px;
  width: 500px;
  max-height: 80vh;
  overflow-y: auto;
}

.form-group {
  margin-bottom: 15px;
}
.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}
.form-group input,
.form-group select {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}
.form-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
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

.status-chua-thanh-toan {
  color: #ffc107;
  font-weight: bold;
}
.status-da-thanh-toan {
  color: #28a745;
  font-weight: bold;
}
.status-dang-giao-hang {
  color: #17a2b8;
  font-weight: bold;
}
.status-hoan-thanh {
  color: #28a745;
  font-weight: bold;
}
.status-huy {
  color: #dc3545;
  font-weight: bold;
}
</style>
