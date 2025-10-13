<template>
  <div class="customer-sidebar-page">
    <!-- Sidebar -->
    <div class="sidebar">
      <div class="sidebar-header">
        <h2>Khách hàng</h2>
        <button class="add-customer-btn" @click="goToAddCustomer">
          <font-awesome-icon icon="plus" />
          Thêm khách hàng
        </button>
      </div>
      
      <!-- Search and Filter -->
      <div class="sidebar-content">
        <div class="search-section">
          <div class="search-box">
            <font-awesome-icon icon="search" class="search-icon" />
            <input 
              v-model="searchQuery" 
              type="text" 
              placeholder="Tìm kiếm khách hàng..."
              class="search-input"
              @input="handleSearch"
            />
          </div>
        </div>
        
        <div class="filter-section">
          <h4>Bộ lọc</h4>
          
          <div class="filter-group">
            <label>Trạng thái</label>
            <div class="filter-options">
              <label class="filter-option">
                <input type="radio" v-model="statusFilter" value="" name="status" />
                <span>Tất cả</span>
              </label>
              <label class="filter-option">
                <input type="radio" v-model="statusFilter" value="1" name="status" />
                <span>Kích hoạt</span>
              </label>
              <label class="filter-option">
                <input type="radio" v-model="statusFilter" value="0" name="status" />
                <span>Chưa kích hoạt</span>
              </label>
            </div>
          </div>
          
          <div class="filter-group">
            <label>Giới tính</label>
            <select v-model="genderFilter" class="filter-select">
              <option value="">Tất cả</option>
              <option value="Nam">Nam</option>
              <option value="Nữ">Nữ</option>
            </select>
          </div>
          
          <div class="filter-group">
            <label>Loại khách hàng</label>
            <select v-model="customerTypeFilter" class="filter-select">
              <option value="">Tất cả</option>
              <option value="vip">VIP</option>
              <option value="regular">Thường</option>
              <option value="new">Mới</option>
            </select>
          </div>
          
          <div class="filter-group">
            <label>Khoảng thời gian</label>
            <div class="date-range">
              <input 
                type="date" 
                v-model="dateFrom" 
                class="date-input"
                placeholder="Từ ngày"
              />
              <input 
                type="date" 
                v-model="dateTo" 
                class="date-input"
                placeholder="Đến ngày"
              />
            </div>
          </div>
        </div>
        
        <div class="action-section">
          <button class="action-btn export-btn" @click="exportCustomers">
            <font-awesome-icon icon="file-excel" />
            Xuất Excel
          </button>
          <button class="action-btn import-btn" @click="importCustomers">
            <font-awesome-icon icon="upload" />
            Nhập Excel
          </button>
          <button class="action-btn refresh-btn" @click="loadCustomers">
            <font-awesome-icon icon="sync-alt" />
            Làm mới
          </button>
        </div>
      </div>
    </div>
    
    <!-- Main Content -->
    <div class="main-content">
      <div class="content-header">
        <div class="header-left">
          <h1>Danh sách khách hàng</h1>
          <span class="customer-count">{{ filteredCustomers.length }} khách hàng</span>
        </div>
        <div class="header-right">
          <div class="view-options">
            <button 
              class="view-btn" 
              :class="{ active: viewMode === 'grid' }"
              @click="viewMode = 'grid'"
            >
              <font-awesome-icon icon="th" />
            </button>
            <button 
              class="view-btn" 
              :class="{ active: viewMode === 'list' }"
              @click="viewMode = 'list'"
            >
              <font-awesome-icon icon="list" />
            </button>
          </div>
        </div>
      </div>
      
      <div class="content-body">
        <!-- Grid View -->
        <div v-if="viewMode === 'grid'" class="customer-grid">
          <div 
            v-for="customer in filteredCustomers" 
            :key="customer.id" 
            class="customer-card"
            @click="selectCustomer(customer)"
          >
            <div class="card-header">
              <div class="customer-avatar">
                <font-awesome-icon icon="user" />
              </div>
              <div class="status-badge" :class="customer.trangThai === 1 ? 'active' : 'inactive'">
                {{ customer.trangThai === 1 ? 'Kích hoạt' : 'Chưa kích hoạt' }}
              </div>
            </div>
            
            <div class="card-body">
              <h3 class="customer-name">{{ customer.hoTen }}</h3>
              <p class="customer-code">{{ customer.maKhachHang }}</p>
              <p class="customer-phone">{{ customer.soDienThoai }}</p>
              
              <div class="customer-gender">
                <font-awesome-icon 
                  :icon="customer.gioiTinh === 'Nam' ? 'mars' : customer.gioiTinh === 'Nữ' ? 'venus' : 'question'" 
                  class="gender-icon"
                  :class="customer.gioiTinh?.toLowerCase()"
                />
                <span>{{ customer.gioiTinh || '-' }}</span>
              </div>
              
              <p class="customer-address" v-if="customer.diaChi">{{ customer.diaChi }}</p>
            </div>
            
            <div class="card-actions">
              <button class="action-btn edit-btn" @click.stop="editCustomer(customer)">
                <font-awesome-icon icon="edit" />
              </button>
              <button class="action-btn view-btn" @click.stop="viewCustomer(customer)">
                <font-awesome-icon icon="eye" />
              </button>
              <button class="action-btn delete-btn" @click.stop="deleteCustomer(customer)">
                <font-awesome-icon icon="trash" />
              </button>
            </div>
          </div>
        </div>
        
        <!-- List View -->
        <div v-else class="customer-list">
          <table class="customer-table">
            <thead>
              <tr>
                <th>STT</th>
                <th>Mã KH</th>
                <th>Tên khách hàng</th>
                <th>SĐT</th>
                <th>Giới tính</th>
                <th>Địa chỉ</th>
                <th>Trạng thái</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(customer, index) in filteredCustomers" :key="customer.id">
                <td>{{ String(index + 1).padStart(2, '0') }}</td>
                <td>{{ customer.maKhachHang }}</td>
                <td>
                  <div class="customer-info">
                    <div class="customer-avatar-small">
                      <font-awesome-icon icon="user" />
                    </div>
                    <div>
                      <div class="customer-name">{{ customer.hoTen }}</div>
                      <div class="customer-username" v-if="customer.taiKhoan">{{ customer.taiKhoan }}</div>
                    </div>
                  </div>
                </td>
                <td>{{ customer.soDienThoai }}</td>
                <td>
                  <div class="gender-display" :class="customer.gioiTinh?.toLowerCase()">
                    <font-awesome-icon 
                      :icon="customer.gioiTinh === 'Nam' ? 'mars' : customer.gioiTinh === 'Nữ' ? 'venus' : 'question'" 
                      class="gender-icon"
                    />
                    <span>{{ customer.gioiTinh || '-' }}</span>
                  </div>
                </td>
                <td>{{ customer.diaChi || '-' }}</td>
                <td>
                  <button 
                    class="status-btn" 
                    :class="customer.trangThai === 1 ? 'active' : 'inactive'"
                    @click="toggleStatus(customer.id)"
                  >
                    {{ customer.trangThai === 1 ? 'Kích hoạt' : 'Chưa kích hoạt' }}
                  </button>
                </td>
                <td>
                  <div class="action-buttons">
                    <button class="action-btn edit-btn" @click="editCustomer(customer)">
                      <font-awesome-icon icon="edit" />
                    </button>
                    <button class="action-btn view-btn" @click="viewCustomer(customer)">
                      <font-awesome-icon icon="eye" />
                    </button>
                    <button class="action-btn delete-btn" @click="deleteCustomer(customer)">
                      <font-awesome-icon icon="trash" />
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
    
    <Toast ref="toastRef" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../services/api'
import Toast from '../components/Toast.vue'

const router = useRouter()
const toastRef = ref(null)

// Data
const customers = ref([])
const searchQuery = ref('')
const statusFilter = ref('')
const genderFilter = ref('')
const customerTypeFilter = ref('')
const dateFrom = ref('')
const dateTo = ref('')
const viewMode = ref('grid')

// Computed
const filteredCustomers = computed(() => {
  let result = customers.value

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(customer => 
      customer.hoTen?.toLowerCase().includes(query) ||
      customer.maKhachHang?.toLowerCase().includes(query) ||
      customer.soDienThoai?.includes(query) ||
      customer.email?.toLowerCase().includes(query)
    )
  }

  if (statusFilter.value !== '') {
    result = result.filter(customer => customer.trangThai === parseInt(statusFilter.value))
  }

  if (genderFilter.value !== '') {
    result = result.filter(customer => customer.gioiTinh === genderFilter.value)
  }

  return result
})

// Methods
const loadCustomers = async () => {
  try {
    const response = await api.get('/api/khach-hang')
    customers.value = response.data.map(customer => ({
      ...customer,
      diaChi: customer.diaChi || customer.dia_chi || null
    }))
  } catch (error) {
    console.error('Error loading customers:', error)
    toastRef.value?.error('Lỗi', 'Không thể tải danh sách khách hàng')
  }
}

const handleSearch = () => {
  // Search is handled by computed property
}

const toggleStatus = async (customerId) => {
  try {
    const customer = customers.value.find(c => c.id === customerId)
    if (!customer) return

    const newStatus = customer.trangThai === 1 ? 0 : 1
    await api.put(`/api/khach-hang/${customerId}`, {
      ...customer,
      trangThai: newStatus
    })

    customer.trangThai = newStatus
    toastRef.value?.success('Thành công', 'Đã cập nhật trạng thái khách hàng')
  } catch (error) {
    console.error('Error updating status:', error)
    toastRef.value?.error('Lỗi', 'Không thể cập nhật trạng thái')
  }
}

const selectCustomer = (customer) => {
  console.log('Selected customer:', customer)
}

const editCustomer = (customer) => {
  router.push(`/khach-hang/edit/${customer.id}`)
}

const viewCustomer = (customer) => {
  console.log('View customer:', customer)
}

const deleteCustomer = (customer) => {
  console.log('Delete customer:', customer)
}

const goToAddCustomer = () => {
  router.push('/khach-hang/add')
}

const exportCustomers = () => {
  console.log('Export customers')
}

const importCustomers = () => {
  console.log('Import customers')
}

onMounted(() => {
  loadCustomers()
})
</script>

<style scoped>
.customer-sidebar-page {
  display: flex;
  height: 100vh;
  background: #f8fafc;
}

/* Sidebar */
.sidebar {
  width: 350px;
  background: white;
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.sidebar-header {
  padding: 1.5rem;
  border-bottom: 1px solid #e2e8f0;
  background: #f8fafc;
}

.sidebar-header h2 {
  margin: 0 0 1rem 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #1e293b;
}

.add-customer-btn {
  width: 100%;
  padding: 0.75rem;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  transition: all 0.3s ease;
}

.add-customer-btn:hover {
  background: #2563eb;
}

.sidebar-content {
  padding: 1.5rem;
  flex: 1;
}

.search-section {
  margin-bottom: 1.5rem;
}

.search-box {
  position: relative;
}

.search-icon {
  position: absolute;
  left: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  color: #6b7280;
}

.search-input {
  width: 100%;
  padding: 0.75rem 0.75rem 0.75rem 2.5rem;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 0.9rem;
  transition: border-color 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.filter-section h4 {
  margin: 0 0 1rem 0;
  font-size: 1rem;
  font-weight: 600;
  color: #374151;
}

.filter-group {
  margin-bottom: 1.5rem;
}

.filter-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #374151;
  font-size: 0.9rem;
}

.filter-options {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.filter-option {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  font-size: 0.9rem;
}

.filter-option input[type="radio"] {
  margin: 0;
}

.filter-select {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.9rem;
  background: white;
}

.date-range {
  display: flex;
  gap: 0.5rem;
}

.date-input {
  flex: 1;
  padding: 0.5rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.9rem;
}

.action-section {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.action-btn {
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: white;
  color: #374151;
  font-size: 0.9rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  transition: all 0.3s ease;
}

.action-btn:hover {
  background: #f3f4f6;
}

.export-btn {
  background: #10b981;
  color: white;
  border-color: #10b981;
}

.export-btn:hover {
  background: #059669;
}

.import-btn {
  background: #f59e0b;
  color: white;
  border-color: #f59e0b;
}

.import-btn:hover {
  background: #d97706;
}

.refresh-btn {
  background: #3b82f6;
  color: white;
  border-color: #3b82f6;
}

.refresh-btn:hover {
  background: #2563eb;
}

/* Main Content */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.content-header {
  background: white;
  padding: 1.5rem 2rem;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left h1 {
  margin: 0 0 0.25rem 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: #1e293b;
}

.customer-count {
  color: #6b7280;
  font-size: 0.9rem;
}

.view-options {
  display: flex;
  gap: 0.5rem;
}

.view-btn {
  width: 40px;
  height: 40px;
  border: 1px solid #d1d5db;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.view-btn.active {
  background: #3b82f6;
  color: white;
  border-color: #3b82f6;
}

.content-body {
  flex: 1;
  padding: 1.5rem 2rem;
  overflow-y: auto;
}

/* Grid View */
.customer-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
}

.customer-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #e2e8f0;
}

.customer-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.customer-avatar {
  width: 48px;
  height: 48px;
  background: #e2e8f0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b7280;
  font-size: 1.25rem;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
}

.status-badge.active {
  background: #dbeafe;
  color: #1e40af;
}

.status-badge.inactive {
  background: #f3f4f6;
  color: #6b7280;
}

.card-body {
  margin-bottom: 1rem;
}

.customer-name {
  margin: 0 0 0.5rem 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: #1e293b;
}

.customer-code {
  margin: 0 0 0.5rem 0;
  font-family: 'Courier New', monospace;
  color: #6b7280;
  font-size: 0.9rem;
}

.customer-phone {
  margin: 0 0 0.5rem 0;
  color: #374151;
  font-size: 0.9rem;
}

.customer-gender {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.gender-icon {
  font-size: 1rem;
}

.gender-icon.nam {
  color: #2563eb;
}

.gender-icon.nữ {
  color: #dc2626;
}

.gender-icon:not(.nam):not(.nữ) {
  color: #6b7280;
}

.customer-address {
  margin: 0;
  color: #6b7280;
  font-size: 0.8rem;
  line-height: 1.4;
}

.card-actions {
  display: flex;
  gap: 0.5rem;
  justify-content: flex-end;
}

.card-actions .action-btn {
  width: 32px;
  height: 32px;
  padding: 0;
  border-radius: 6px;
}

.edit-btn {
  background: #dbeafe;
  color: #1e40af;
  border-color: #dbeafe;
}

.view-btn {
  background: #d1fae5;
  color: #059669;
  border-color: #d1fae5;
}

.delete-btn {
  background: #fee2e2;
  color: #dc2626;
  border-color: #fee2e2;
}

/* List View */
.customer-list {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.customer-table {
  width: 100%;
  border-collapse: collapse;
}

.customer-table th {
  background: #f8fafc;
  padding: 1rem;
  text-align: left;
  font-weight: 600;
  color: #374151;
  border-bottom: 1px solid #e2e8f0;
  font-size: 0.9rem;
}

.customer-table td {
  padding: 1rem;
  border-bottom: 1px solid #f1f5f9;
  vertical-align: middle;
  font-size: 0.9rem;
}

.customer-table tr:hover {
  background: #f8fafc;
}

.customer-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.customer-avatar-small {
  width: 32px;
  height: 32px;
  background: #e2e8f0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b7280;
  font-size: 0.9rem;
}

.customer-name {
  font-weight: 500;
  color: #1e293b;
}

.customer-username {
  font-size: 0.8rem;
  color: #6b7280;
}

.gender-display {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.gender-display.nam .gender-icon {
  color: #2563eb;
}

.gender-display.nữ .gender-icon {
  color: #dc2626;
}

.gender-display:not(.nam):not(.nữ) .gender-icon {
  color: #6b7280;
}

.status-btn {
  padding: 0.25rem 0.75rem;
  border: none;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.status-btn.active {
  background: #dbeafe;
  color: #1e40af;
}

.status-btn.inactive {
  background: #f3f4f6;
  color: #6b7280;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
}

.action-buttons .action-btn {
  width: 28px;
  height: 28px;
  padding: 0;
  border-radius: 4px;
  font-size: 0.8rem;
}
</style>



