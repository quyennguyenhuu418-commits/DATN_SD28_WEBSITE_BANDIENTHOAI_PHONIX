<template>
  <div class="customer-page">
    <!-- POS Header -->
    <PosStyleHeader />

    <!-- Filter Section -->
    <div class="filter-section">
      <div class="filter-header">
        <div class="filter-title">
          <h3>Bộ lọc nâng cao</h3>
          <p>Tìm kiếm và lọc khách hàng theo tiêu chí</p>
      </div>
        <div class="filter-actions">
          <button class="btn-clear-filters" @click="clearAllFilters">
            Xóa bộ lọc
          </button>
        </div>
      </div>
      <div class="filter-row">
        <div class="filter-group">
          <label>Tìm kiếm:</label>
              <input 
                type="text" 
            v-model="searchQuery" 
            placeholder="Tìm theo tên, mã, SĐT, email..."
            @input="applyFilters"
                class="search-input"
              />
            </div>
        <div class="filter-group">
          <label>Trạng thái:</label>
          <select v-model="statusFilter" @change="applyFilters" class="filter-select">
            <option value="">Tất cả trạng thái</option>
            <option value="1">Kích hoạt</option>
            <option value="0">Chưa kích hoạt</option>
          </select>
          </div>
        <div class="filter-group">
          <label>Giới tính:</label>
          <select v-model="genderFilter" @change="applyFilters" class="filter-select">
            <option value="">Tất cả giới tính</option>
          <option value="Nam">Nam</option>
          <option value="Nữ">Nữ</option>
        </select>
      </div>
        <div class="filter-group">
          <label>Loại khách hàng:</label>
          <select v-model="customerTypeFilter" @change="applyFilters" class="filter-select">
            <option value="">Tất cả loại</option>
              <option value="vip">VIP</option>
              <option value="regular">Thường</option>
              <option value="new">Mới</option>
            </select>
          </div>
        <div class="filter-group">
          <label>Từ ngày:</label>
              <input 
                type="date" 
                v-model="dateFrom" 
            @change="applyFilters"
            class="filter-input"
              />
        </div>
        <div class="filter-group">
          <label>Đến ngày:</label>
        <input 
                type="date" 
                v-model="dateTo" 
            @change="applyFilters"
            class="filter-input"
              />
            </div>
          </div>
        </div>

    <!-- Add Customer Section -->
    <div class="add-customer-section">
      <button class="btn-add-customer" @click="goToAddCustomer">
        <span class="icon-plus"></span>
        Thêm Khách Hàng
          </button>
        </div>

    <!-- Table Header Section -->
    <div class="table-header">
      <div class="table-title">
        <h2>Danh sách Khách Hàng</h2>
        <span class="item-count">{{ totalItems }} khách hàng</span>
      </div>
      <div class="table-actions">
        <div class="items-per-page">
          <label>Hiển thị:</label>
          <select v-model="itemsPerPage" @change="applyFilters" class="page-size-select">
            <option value="5">5 mục / trang</option>
            <option value="10">10 mục / trang</option>
            <option value="20">20 mục / trang</option>
            <option value="50">50 mục / trang</option>
          </select>
        </div>
      </div>
    </div>

    <!-- Customer List -->
    <div class="customer-list-card">
    <div class="table-container">
        <table class="customer-table">
        <thead>
          <tr>
            <th class="checkbox-column">
              <input 
                type="checkbox" 
                v-model="selectAll" 
                @change="toggleSelectAll"
                class="select-all-checkbox"
              />
            </th>
            <th>STT</th>
            <th>MÃ KHÁCH HÀNG</th>
            <th>TÊN KHÁCH HÀNG</th>
            <th>SỐ ĐIỆN THOẠI</th>
            <th>GIỚI TÍNH</th>
            <th>ĐỊA CHỈ</th>
            <th>TRẠNG THÁI</th>
            <th class="action-column">THAO TÁC</th>
          </tr>
        </thead>
        <tbody>
            <tr v-for="(customer, index) in paginatedCustomers" :key="customer.id">
              <td class="checkbox-column">
                <input 
                  type="checkbox" 
                  v-model="selectedCustomers" 
                  :value="customer.id"
                  class="row-checkbox"
                />
              </td>
              <td class="stt-cell">{{ String((currentPage - 1) * itemsPerPage + index + 1).padStart(2, '0') }}</td>
              <td class="code-cell">{{ customer.maKhachHang }}</td>
              <td class="name-cell">
                <div class="customer-info">
                  <div class="customer-avatar">
                    <font-awesome-icon icon="user" />
                  </div>
                  <div class="customer-details">
                    <div class="customer-name">{{ customer.hoTen }}</div>
                    <div class="customer-email">{{ customer.email || '-' }}</div>
                  </div>
                </div>
              </td>
              <td class="phone-cell">{{ customer.soDienThoai }}</td>
              <td class="gender-cell">
                <div class="gender-display" :class="customer.gioiTinh?.toLowerCase()">
                  <font-awesome-icon 
                    :icon="customer.gioiTinh === 'Nam' ? 'mars' : customer.gioiTinh === 'Nữ' ? 'venus' : 'question'" 
                    class="gender-icon"
                  />
                  <span class="gender-text">{{ customer.gioiTinh || '-' }}</span>
                </div>
            </td>
              <td class="address-cell">{{ customer.diaChi || '-' }}</td>
              <td class="status-cell">
                <div class="status-badge-container">
                  <span class="status-badge" :class="{ 'active': customer.trangThai === 1 }">
                    {{ customer.trangThai === 1 ? 'Hoạt động' : 'Ngừng hoạt động' }}
                  </span>
                  <div class="toggle-container">
                    <label class="toggle-switch">
                      <input 
                        type="checkbox"
                        :checked="customer.trangThai === 1"
                        @change="toggleCustomerStatus(customer)"
                      />
                      <span class="toggle-slider"></span>
                    </label>
                  </div>
                </div>
              </td>
              <td class="action-column">
                <div class="action-buttons">
                  <button class="btn-view" @click="viewCustomer(customer)" title="Xem chi tiết">
                    <font-awesome-icon icon="eye" />
                  </button>
                  <button class="btn-edit" @click="editCustomer(customer)" title="Chỉnh sửa">
                    <font-awesome-icon icon="edit" />
              </button>
                </div>
            </td>
          </tr>
        </tbody>
      </table>
        
        <!-- Pagination -->
        <div class="pagination-section">
          <div class="pagination-info">
            <span>Hiển thị {{ startItem }} - {{ endItem }} / {{ totalItems }} mục</span>
          </div>
          
          <div class="pagination-controls">
            <button 
              class="pagination-btn prev-btn" 
              :disabled="currentPage === 1"
              @click="previousPage"
            >
              <i class="icon-chevron-left"></i>
              Trước
            </button>
            
            <div class="pagination-numbers">
              <button 
                v-for="page in getVisiblePages()"
                :key="page"
                class="pagination-number"
                :class="{ active: page === currentPage, ellipsis: page === '...' }"
                @click="page !== '...' && goToPage(page as number)"
                :disabled="page === '...'"
              >
                {{ page }}
              </button>
            </div>
            
            <button 
              class="pagination-btn next-btn" 
              :disabled="currentPage === totalPages"
              @click="nextPage"
            >
              Sau
              <i class="icon-chevron-right"></i>
            </button>
          </div>
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
import PosStyleHeader from '../components/PosStyleHeader.vue'

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
const searchError = ref('')
const selectedCustomers = ref([])
const selectAll = ref(false)
const currentPage = ref(1)
const itemsPerPage = 5

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

// Pagination
const totalPages = computed(() => {
  return Math.ceil(filteredCustomers.value.length / itemsPerPage)
})

const paginatedCustomers = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage
  const end = start + itemsPerPage
  return filteredCustomers.value.slice(start, end)
})

const totalItems = computed(() => {
  return filteredCustomers.value.length
})

const startItem = computed(() => {
  return (currentPage.value - 1) * itemsPerPage.value + 1
})

const endItem = computed(() => {
  return Math.min(currentPage.value * itemsPerPage.value, totalItems.value)
})

const getVisiblePages = () => {
  const pages = []
  const maxVisible = 5
  let start = Math.max(1, currentPage.value - Math.floor(maxVisible / 2))
  let end = Math.min(totalPages.value, start + maxVisible - 1)
  
  if (end - start + 1 < maxVisible) {
    start = Math.max(1, end - maxVisible + 1)
  }
  
  // Add first page and ellipsis if needed
  if (start > 1) {
    pages.push(1)
    if (start > 2) {
      pages.push('...')
    }
  }
  
  // Add visible pages
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  
  // Add last page and ellipsis if needed
  if (end < totalPages.value) {
    if (end < totalPages.value - 1) {
      pages.push('...')
    }
    pages.push(totalPages.value)
  }
  
  return pages
}

// Methods
const loadCustomers = async () => {
  try {
    const response = await api.get('/api/khach-hang')
    customers.value = response.data.map(customer => ({
      ...customer,
      // Ensure address is properly formatted from database
      diaChi: customer.diaChi || customer.dia_chi || null
    }))
  } catch (error) {
    console.error('Error loading customers:', error)
    toastRef.value?.error('Lỗi', 'Không thể tải danh sách khách hàng')
  }
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

const editCustomer = (customer) => {
  router.push(`/khach-hang/edit/${customer.id}`)
}

const viewCustomer = (customer) => {
  router.push(`/khach-hang/${customer.id}`)
}

const toggleCustomerStatus = async (customer) => {
  try {
    const newStatus = customer.trangThai === 1 ? 0 : 1
    await api.put(`/api/khach-hang/${customer.id}`, {
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

const goToAddCustomer = () => {
  router.push('/khach-hang/add')
}

const clearFilters = () => {
  searchQuery.value = ''
  statusFilter.value = ''
  genderFilter.value = ''
  customerTypeFilter.value = ''
  dateFrom.value = ''
  dateTo.value = ''
  searchError.value = ''
  currentPage.value = 1
}

const clearAllFilters = () => {
  clearFilters()
}

const applyFilters = () => {
  currentPage.value = 1
}

const toggleSelectAll = () => {
  if (selectAll.value) {
    selectedCustomers.value = paginatedCustomers.value.map(customer => customer.id)
  } else {
    selectedCustomers.value = []
  }
}

const changeItemsPerPage = () => {
  currentPage.value = 1
}

const goToPage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

const previousPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
  }
}

const validateSearch = () => {
  if (searchQuery.value && searchQuery.value.length < 2) {
    searchError.value = 'Từ khóa tìm kiếm phải có ít nhất 2 ký tự'
  } else {
    searchError.value = ''
  }
}

const validateRequiredFields = (customer) => {
  const errors = []
  
  if (!customer.hoTen || customer.hoTen.trim() === '') {
    errors.push('Họ tên là bắt buộc')
  }
  
  if (!customer.soDienThoai || customer.soDienThoai.trim() === '') {
    errors.push('Số điện thoại là bắt buộc')
  } else if (!/^[0-9]{10,11}$/.test(customer.soDienThoai)) {
    errors.push('Số điện thoại phải có 10-11 chữ số')
  }
  
  if (customer.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(customer.email)) {
    errors.push('Email không đúng định dạng')
  }
  
  return errors
}

onMounted(() => {
  loadCustomers()
})
</script>

<style scoped>
.customer-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 1rem;
  padding-top: 80px;
}


/* Filter Section */
.filter-section {
  background: white;
  padding: 28px;
  margin-bottom: 24px;
  margin-top: 20px;
  border-radius: 16px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
  border: 1px solid #e2e8f0;
  position: relative;
  overflow: hidden;
}

.filter-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #ff6b35, #f7931e, #ff6b35);
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e2e8f0;
}

.filter-title h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1a202c;
}

.filter-title p {
  margin: 4px 0 0 0;
  font-size: 14px;
  color: #64748b;
}

.filter-actions {
  display: flex;
  gap: 12px;
}

.btn-clear-filters {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  color: #64748b;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-clear-filters:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
  color: #475569;
}

.filter-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  align-items: end;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-group label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  margin: 0;
}

.search-input,
.filter-select,
.filter-input {
  padding: 12px 16px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.2s ease;
  background: white;
}

.search-input:focus,
.filter-select:focus,
.filter-input:focus {
  outline: none;
  border-color: #ff6b35;
  box-shadow: 0 0 0 3px rgba(255, 107, 53, 0.1);
}

/* Add Customer Section */
.add-customer-section {
  margin-bottom: 24px;
  display: flex;
  justify-content: flex-start;
}

.btn-add-customer {
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.3);
}

.btn-add-customer:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255, 107, 53, 0.4);
}

.icon-plus {
  width: 20px;
  height: 20px;
  position: relative;
}

.icon-plus::before,
.icon-plus::after {
  content: '';
  position: absolute;
  background: white;
  border-radius: 2px;
}

.icon-plus::before {
  width: 2px;
  height: 16px;
  left: 9px;
  top: 2px;
}

.icon-plus::after {
  width: 16px;
  height: 2px;
  left: 2px;
  top: 9px;
}

/* Table Header */
.table-header {
  background: white;
  padding: 20px 24px;
  margin-bottom: 16px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-title h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1a202c;
}

.table-title p {
  margin: 4px 0 0 0;
  font-size: 14px;
  color: #64748b;
}

.table-controls {
  display: flex;
  align-items: center;
  gap: 16px;
}

.items-per-page {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #64748b;
}

.items-select {
  padding: 6px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  background: white;
}

/* Table Styling */
.customer-table {
  width: 100%;
  min-width: 1300px;
  border-collapse: separate;
  border-spacing: 0;
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #e2e8f0;
  table-layout: fixed;
}

.customer-table th {
  background: linear-gradient(135deg, #f8fafc, #f1f5f9);
  padding: 16px 20px;
  text-align: left;
  font-weight: 700;
  font-size: 14px;
  color: #1f2937;
  border-bottom: 2px solid #e2e8f0;
  border-right: 1px solid #e2e8f0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  text-transform: uppercase;
}

.customer-table th:last-child {
  border-right: none;
}

.customer-table td {
  padding: 16px 20px;
  border-bottom: 1px solid #e2e8f0;
  border-right: 1px solid #e2e8f0;
  vertical-align: middle;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.customer-table td:last-child {
  border-right: none;
}

.customer-table tbody tr:hover {
  background: #f8fafc;
}

/* Checkbox Column */
.checkbox-column {
  width: 60px;
  text-align: left;
  padding: 8px 12px !important;
}

/* STT Column */
.customer-table th:nth-child(2) {
  width: 50px;
  text-align: center;
  padding: 8px 4px;
  font-weight: 700;
  font-size: 14px;
  color: #1f2937;
}

.customer-table td:nth-child(2) {
  text-align: center;
  padding: 8px 4px;
  font-weight: 600;
  font-size: 14px;
}

/* Customer Code Column */
.customer-table th:nth-child(3) {
  font-size: 14px;
  font-weight: 700;
  padding: 8px 12px;
  color: #1f2937;
}

.customer-table td:nth-child(3) {
  font-size: 14px;
  font-weight: 500;
  padding: 8px 12px;
  color: #374151;
}

/* Customer Name Column */
.customer-table th:nth-child(4) {
  font-size: 14px;
  font-weight: 700;
  padding: 8px 12px;
  color: #1f2937;
}

.customer-table td:nth-child(4) {
  font-size: 14px;
  font-weight: 500;
  padding: 8px 12px;
}

/* Phone Column */
.customer-table th:nth-child(5) {
  font-size: 14px;
  font-weight: 700;
  padding: 8px 12px;
  color: #1f2937;
}

.customer-table td:nth-child(5) {
  font-size: 14px;
  font-weight: 500;
  padding: 8px 12px;
  color: #374151;
}

/* Gender Column */
.customer-table th:nth-child(6) {
  width: 100px;
  text-align: left;
  padding: 8px 12px;
  font-size: 14px;
  font-weight: 700;
  color: #1f2937;
}

.customer-table td:nth-child(6) {
  text-align: left;
  padding: 8px 12px;
  font-size: 13px;
  font-weight: 400;
  color: #6b7280;
}

/* Address Column */
.customer-table th:nth-child(7) {
  font-size: 14px;
  font-weight: 700;
  padding: 8px 12px;
  color: #1f2937;
}

.customer-table td:nth-child(7) {
  font-size: 13px;
  font-weight: 400;
  padding: 8px 12px;
  color: #6b7280;
}


.checkbox-column input[type="checkbox"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
}

/* Action Column */
.action-column {
  width: 120px;
  text-align: center;
  padding: 8px 12px !important;
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.btn-view,
.btn-edit {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.btn-view {
  background: #e0f2fe;
  color: #0277bd;
}

.btn-view:hover {
  background: #b3e5fc;
  transform: translateY(-1px);
}

.btn-edit {
  background: #fff3e0;
  color: #f57c00;
}

.btn-edit:hover {
  background: #ffe0b2;
  transform: translateY(-1px);
}


/* Status Styling */
.status-badge-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.toggle-switch {
  position: relative;
  display: inline-block;
  width: 50px;
  height: 24px;
}

.toggle-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #cbd5e1;
  transition: 0.3s;
  border-radius: 24px;
}

.toggle-slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: 0.3s;
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

input:checked + .toggle-slider {
  background-color: #f97316;
}

input:checked + .toggle-slider:before {
  transform: translateX(26px);
}

/* Status Column */
.customer-table th:nth-child(8) {
  width: 120px;
  text-align: left;
  padding: 8px 12px;
  font-size: 14px;
  font-weight: 700;
  color: #1f2937;
}

.customer-table td:nth-child(8) {
  text-align: left;
  padding: 8px 12px;
  font-size: 14px;
  font-weight: 500;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  background: #fef2f2;
  color: #dc2626;
  border: 1px solid #fecaca;
}

.status-badge.active {
  background: #f0fdf4;
  color: #16a34a;
  border-color: #bbf7d0;
}

/* Pagination Styling */
.pagination-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
  border-radius: 0 0 12px 12px;
}

.pagination-info {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

.pagination-center {
  display: flex;
  justify-content: center;
  align-items: center;
}

.pagination-icons {
  display: flex;
  gap: 8px;
}

.icon-filter,
.icon-target {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #6b7280;
  cursor: pointer;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: white;
  color: #374151;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 14px;
  font-weight: 500;
}

.pagination-btn:hover:not(:disabled) {
  background: #f97316;
  color: white;
  border-color: #f97316;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(249, 115, 22, 0.3);
}

.pagination-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  color: #9ca3af;
}

.pagination-numbers {
  display: flex;
  gap: 4px;
}

.pagination-number {
  min-width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  color: #374151;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 14px;
  font-weight: 500;
}

.pagination-number:hover:not(:disabled):not(.ellipsis) {
  background: #f97316;
  color: white;
  border-color: #f97316;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(249, 115, 22, 0.3);
}

.pagination-number.active {
  background: #f97316;
  color: white;
  border-color: #f97316;
  box-shadow: 0 2px 4px rgba(249, 115, 22, 0.3);
}

.pagination-number.ellipsis {
  background: transparent;
  border: none;
  cursor: default;
  color: #9ca3af;
}

.pagination-number:disabled {
  cursor: not-allowed;
}

.icon-chevron-left::before {
  content: "‹";
  font-size: 16px;
  font-weight: bold;
}

.icon-chevron-right::before {
  content: "›";
  font-size: 16px;
  font-weight: bold;
}


.filter-header {
  background: #f8fafc;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid #e2e8f0;
}

.filter-header h3 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: #1e293b;
}

.filter-content {
  padding: 1.5rem;
}

.search-row {
  margin-bottom: 1.5rem;
}

.search-group {
  width: 100%;
}

.search-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #374151;
}


.error-message {
  color: #dc2626;
  font-size: 0.8rem;
  margin-top: 0.25rem;
}

.filter-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.status-group,
.gender-group,
.type-group,
.date-group {
  min-width: 0;
}

.status-group label,
.gender-group label,
.type-group label,
.date-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #374151;
  font-size: 0.9rem;
}

.gender-select,
.type-select {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.9rem;
  background: white;
  cursor: pointer;
  transition: border-color 0.3s ease;
}

.gender-select:focus,
.type-select:focus {
  outline: none;
  border-color: #3b82f6;
}

.date-range {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.date-input {
  flex: 1;
  padding: 0.5rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.9rem;
  transition: border-color 0.3s ease;
}

.date-input:focus {
  outline: none;
  border-color: #3b82f6;
}

.date-separator {
  color: #6b7280;
  font-weight: 500;
}

.radio-buttons {
  display: flex;
  gap: 1rem;
}

.radio-option {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
}

.radio-option input[type="radio"] {
  margin: 0;
}

.results-info {
  margin-bottom: 1rem;
  font-size: 0.9rem;
  color: #6b7280;
}

.action-buttons {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.btn-gray {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: #f3f4f6;
  color: #374151;
  border: none;
  border-radius: 6px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-gray:hover {
  background: #e5e7eb;
}

.btn-blue {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-blue:hover {
  background: #2563eb;
}

/* Customer List Card */
.customer-list-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.list-header {
  background: #f8fafc;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid #e2e8f0;
}

.list-header h3 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: #1e293b;
}

/* Table */
.table-container {
  overflow-x: auto;
}

.customer-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
}

.customer-table th {
  background: #f8fafc;
  padding: 1rem;
  text-align: left;
  font-weight: 600;
  color: #374151;
  border-bottom: 1px solid #e2e8f0;
  border-right: 1px solid #e2e8f0;
  white-space: nowrap;
}

.customer-table th:last-child {
  border-right: none;
}

.customer-table td {
  padding: 1rem;
  border-bottom: 1px solid #f1f5f9;
  border-right: 1px solid #f1f5f9;
  vertical-align: middle;
}

.customer-table td:last-child {
  border-right: none;
}

.customer-table tr:hover {
  background: #f8fafc;
}

/* Table Cells */
.stt-cell {
  text-align: center;
  font-weight: 500;
  color: #6b7280;
}

.code-cell {
  font-family: 'Courier New', monospace;
  font-weight: 500;
  color: #1e293b;
}

.name-cell {
  min-width: 200px;
}

.customer-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.customer-avatar {
  width: 40px;
  height: 40px;
  background: #e2e8f0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b7280;
  font-size: 1rem;
  flex-shrink: 0;
}

.customer-details {
  min-width: 0;
  flex: 1;
}

.customer-name {
  font-weight: 600;
  color: #1e293b;
  font-size: 0.95rem;
  margin-bottom: 0.25rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.customer-email {
  font-size: 0.8rem;
  color: #6b7280;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.phone-cell {
  font-family: 'Courier New', monospace;
  color: #1e293b;
}

.gender-cell {
  text-align: center;
}

.gender-display {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.gender-icon {
  font-size: 1rem;
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

.gender-text {
  font-size: 0.9rem;
  font-weight: 500;
}

.gender-display.nam .gender-text {
  color: #2563eb;
}

.gender-display.nữ .gender-text {
  color: #dc2626;
}

.gender-display:not(.nam):not(.nữ) .gender-text {
  color: #6b7280;
}

.address-cell {
  max-width: 200px;
  white-space: normal;
  word-wrap: break-word;
  color: #374151;
}

.address-cell:empty::after {
  content: '-';
  color: #9ca3af;
  font-style: italic;
}

.status-cell {
  text-align: center;
}

.status-btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 100px;
}

.status-btn.active {
  background: #dbeafe;
  color: #1e40af;
  border: 1px solid #93c5fd;
}

.status-btn.inactive {
  background: #f3f4f6;
  color: #6b7280;
  border: 1px solid #d1d5db;
}

.actions-cell {
  text-align: center;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
  justify-content: center;
}

.action-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.8rem;
}

.edit-btn {
  background: #dbeafe;
  color: #1e40af;
}

.edit-btn:hover {
  background: #bfdbfe;
}

.delete-btn {
  background: #fee2e2;
  color: #dc2626;
}

.delete-btn:hover {
  background: #fecaca;
}

/* Pagination */
.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
}

</style>
