<template>
  <div class="page">
    <PosHeader />
    
    <div class="content">
      <div class="header">
        <h1>Quản lý Nhân Viên</h1>
        <button class="btn-primary" @click="openForm()">Thêm Nhân Viên</button>
      </div>

    <Toast ref="toastRef" />

    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>STT</th>
            <th>Mã NV</th>
            <th>Họ Tên</th>
            <th>SĐT</th>
            <th>Email</th>
            <th>Chức Vụ</th>
            <th>Trạng Thái</th>
            <th>Thao Tác</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td colspan="8" class="text-center">Tính năng đang phát triển...</td>
          </tr>
        </tbody>
      </table>
    </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Toast from '@/components/Toast.vue'
import PosHeader from '@/components/PosHeader.vue'

const toastRef = ref<InstanceType<typeof Toast> | null>(null)

function openForm() {
  toastRef.value?.info('Thông báo', 'Tính năng quản lý nhân viên đang được phát triển')
}
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
}

.btn-primary {
  background: #007bff;
  color: white;
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.table-container {
  overflow-x: auto;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

table {
  width: 100%;
  border-collapse: collapse;
  background: white;
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

.text-center {
  text-align: center;
  color: #6c757d;
  font-style: italic;
}
</style>

