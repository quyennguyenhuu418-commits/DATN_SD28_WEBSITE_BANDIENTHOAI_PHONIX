<template>
  <div class="page">
    <!-- Filter Section -->
    <div class="filter-card">
      <div class="filter-header">
        <img src="/src/assets/loupe.png" alt="Tìm kiếm" class="filter-icon" />
        <h3>Bộ Lọc Tìm Kiếm</h3>
      </div>

      <div class="filter-content">
        <div class="filter-row">
          <div class="filter-group">
            <label>Tìm kiếm</label>
            <input 
              type="text" 
              class="search-input" 
              :placeholder="searchPlaceholder"
              v-model="searchQuery"
              @input="handleSearch"
            />
          </div>
          <div class="filter-group">
            <label>Trạng thái</label>
            <div class="radio-group">
              <label class="radio-label">
                <input 
                  type="radio" 
                  value="all" 
                  v-model="statusFilter"
                  @change="handleStatusFilter"
                />
                <span class="radio-custom"></span>
                Tất cả
              </label>
              <label class="radio-label">
                <input 
                  type="radio" 
                  value="active" 
                  v-model="statusFilter"
                  @change="handleStatusFilter"
                />
                <span class="radio-custom"></span>
                Hoạt động
              </label>
              <label class="radio-label">
                <input 
                  type="radio" 
                  value="inactive" 
                  v-model="statusFilter"
                  @change="handleStatusFilter"
                />
                <span class="radio-custom"></span>
                Ngừng hoạt động
              </label>
            </div>
          </div>
        </div>
      </div>

      <div class="filter-footer">
        <div class="filter-info">
          Tổng số {{ entityName }}: <span class="product-count">{{ filteredData.length }}</span>
        </div>
        <div class="filter-actions">
          <button class="btn-secondary" @click="handleExcelExport" :disabled="!hasSelectedItems">
            Tải Excel {{ hasSelectedItems ? `(${selectedCount})` : '' }}
          </button>
          <button class="btn-primary" @click="openForm()">Thêm {{ entityName }}</button>
          <button class="btn-secondary" @click="resetFilter">Đặt lại bộ lọc</button>
        </div>
      </div>
    </div>

    <!-- Main Content Section -->
    <div class="main-content">
      <div class="content-header">
        <div class="content-title">
          <div class="title-icon">{{ titleIcon }}</div>
          <h3>{{ title }}</h3>
        </div>
        <div class="content-count">{{ filteredData.length }} {{ entityName }}</div>
      </div>

      <div class="table-container">
        <table class="data-table">
          <thead>
            <tr>
              <th class="checkbox-col">
                <input
                  type="checkbox"
                  v-model="selectAll"
                  :indeterminate="hasSelectedItems && !allCurrentPageSelected"
                />
              </th>
              <th class="stt-col">STT</th>
              <template v-for="column in columns" :key="column.key">
                <th v-if="column.type !== 'status'" :class="column.class">
                  {{ column.label }}
                </th>
              </template>
              <th class="status-toggle-col">Trạng Thái</th>
              <th class="action-col">Thao Tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in paginatedData" :key="item.id" class="table-row">
              <td class="checkbox-col">
                <input
                  type="checkbox"
                  :checked="isItemSelected(item.id)"
                  @change="toggleItemSelection(item.id)"
                />
              </td>
              <td class="stt-col">{{ (currentPage - 1) * itemsPerPage + index + 1 }}</td>
              <template v-for="column in columns" :key="column.key">
                <td v-if="column.type !== 'status'" :class="column.class">
                  <slot :name="`cell-${column.key}`" :item="item" :value="getColumnValue(item, column.key)">
                    <span>{{ formatColumnValue(item, column) }}</span>
                  </slot>
                </td>
              </template>
              <td class="status-toggle-col">
                <div class="status-toggle">
                  <label class="toggle-switch" :title="getToggleTooltip(item)">
                    <input 
                      type="checkbox" 
                      :checked="(item.trangThai || 0) === 1"
                      @change="toggleItemStatus(item)"
                      :disabled="isUpdatingStatus"
                    />
                    <span class="toggle-slider"></span>
                  </label>
                  <div class="status-text-container">
                    <span :class="getStatusClass(item)" class="status-text">
                      {{ getStatusText(item) }}
                    </span>
                  </div>
                </div>
              </td>
              <td class="action-col">
                <button class="edit-btn" @click="openForm(item)">
                  <img src="/src/assets/edit.png" alt="Sửa" class="action-icon" />
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="pagination-container">
        <div class="pagination-left">
          <span>Hiển thị</span>
          <select v-model="itemsPerPage" @change="handleItemsPerPageChange" class="items-per-page">
            <option value="5">5</option>
            <option value="10">10</option>
            <option value="20">20</option>
            <option value="50">50</option>
          </select>
          <span>mục / trang</span>
        </div>

        <div class="pagination-center">
          Hiển thị {{ (currentPage - 1) * itemsPerPage + 1 }} - {{ Math.min(currentPage * itemsPerPage, filteredData.length) }} / {{ filteredData.length }} mục
        </div>

        <div class="pagination-right">
          <button class="pagination-btn" @click="goToPage(1)" :disabled="currentPage === 1"><<</button>
          <button class="pagination-btn" @click="goToPage(currentPage - 1)" :disabled="currentPage === 1"><</button>
          <button
            v-for="page in visiblePages"
            :key="page"
            class="pagination-btn"
            :class="{ active: page === currentPage }"
            @click="goToPage(page)"
          >
            {{ page }}
          </button>
          <button class="pagination-btn" @click="goToPage(currentPage + 1)" :disabled="currentPage === totalPages">></button>
          <button class="pagination-btn" @click="goToPage(totalPages)" :disabled="currentPage === totalPages">>></button>
        </div>
      </div>
    </div>
  </div>

  <!-- Toast Component -->
  <Toast ref="toastRef" />

  <!-- Confirm Modal for Excel Export -->
  <ConfirmModal
    v-if="showConfirmModal"
    :show="showConfirmModal"
    title="Xác nhận tải Excel"
    :message="`Bạn có chắc muốn tải Excel cho ${selectedCount} ${entityName} đã chọn?`"
    confirmText="Tải Excel"
    cancelText="Hủy"
    @confirm="confirmExcelExport"
    @cancel="showConfirmModal = false"
  />
</template>

<script setup lang="ts">
import { computed, watch, ref } from 'vue'
import { useAdminTable } from '@/composables/useAdminTable'
import Toast from '@/components/Toast.vue'
import ConfirmModal from '@/components/ConfirmModal.vue'

interface Column {
  key: string
  label: string
  class?: string
  type?: 'text' | 'date' | 'status' | 'code'
}

interface Props {
  data: any[]
  columns: Column[]
  title: string
  titleIcon: string
  entityName: string
  searchPlaceholder: string
}

const props = defineProps<Props>()

const emit = defineEmits<{
  openForm: [item?: any]
  exportExcel: []
  toggleStatus: [item: any]
}>()

// Toast and modal refs
const toastRef = ref()
const showConfirmModal = ref(false)

// Checkbox selection
const selectedItems = ref<Set<number>>(new Set())
const selectAll = ref(false)

// Status toggle state
const isUpdatingStatus = ref(false)

// Status filter state
const statusFilter = ref<string>('all')

// Use common admin table functionality
const {
  searchQuery,
  currentPage,
  itemsPerPage,
  totalPages,
  visiblePages,
  filteredData: baseFilteredData,
  paginatedData: basePaginatedData,
  handleSearch: baseHandleSearch,
  resetFilter: baseResetFilter,
  goToPage,
  handleItemsPerPageChange,
  setData,
  formatDate
} = useAdminTable(5)

// Update data when props change
watch(() => props.data, (newData) => {
  setData(newData)
}, { immediate: true })

// Computed properties for status filtering
const filteredData = computed(() => {
  let data = baseFilteredData.value
  
  // Apply status filter
  if (statusFilter.value !== 'all') {
    data = data.filter(item => {
      const status = item.trangThai
      if (statusFilter.value === 'active' && status === 1) return true
      if (statusFilter.value === 'inactive' && status === 0) return true
      return false
    })
  }
  
  return data
})

const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return filteredData.value.slice(start, end)
})

// Checkbox computed properties
const selectedCount = computed(() => selectedItems.value.size)
const hasSelectedItems = computed(() => selectedItems.value.size > 0)
const allCurrentPageSelected = computed(() => {
  return paginatedData.value.length > 0 &&
         paginatedData.value.every(item => selectedItems.value.has(item.id))
})

// Methods
const getColumnValue = (item: any, key: string) => {
  return item[key]
}

// Toast methods
const showToastMessage = (message: string, type: 'success' | 'error' | 'warning' | 'info' = 'success') => {
  if (toastRef.value) {
    const title = type === 'success' ? 'Thành công' : type === 'error' ? 'Lỗi' : type === 'warning' ? 'Cảnh báo' : 'Thông báo'
    toastRef.value[type](title, message, 3000)
  }
}

// Reset filter method
const resetFilter = () => {
  searchQuery.value = ''
  statusFilter.value = 'all'
  currentPage.value = 1
  
  // Reset checkbox selection
  selectedItems.value.clear()
  selectAll.value = false
  
  showToastMessage('Đã đặt lại bộ lọc thành công!', 'success')
}

// Checkbox methods
const toggleItemSelection = (itemId: number) => {
  if (selectedItems.value.has(itemId)) {
    selectedItems.value.delete(itemId)
  } else {
    selectedItems.value.add(itemId)
  }
}

const isItemSelected = (itemId: number): boolean => {
  return selectedItems.value.has(itemId)
}

// Watch for select all checkbox
watch(selectAll, (newValue) => {
  if (newValue) {
    // Select all items on current page
    paginatedData.value.forEach(item => {
      selectedItems.value.add(item.id)
    })
  } else {
    // Deselect all items on current page
    paginatedData.value.forEach(item => {
      selectedItems.value.delete(item.id)
    })
  }
})

// Watch for individual item selection to update select all checkbox
watch(selectedItems, () => {
  selectAll.value = allCurrentPageSelected.value
}, { deep: true })

// Watch for page changes to update select all checkbox
watch([currentPage, paginatedData], () => {
  // Update select all checkbox based on current page selection
  selectAll.value = allCurrentPageSelected.value
}, { immediate: true })

// Watch for data changes to reset selection
watch(() => props.data, () => {
  // Clear selection when data changes
  selectedItems.value.clear()
  selectAll.value = false
}, { deep: true })

// Excel export methods
const handleExcelExport = () => {
  if (!hasSelectedItems.value) {
    showToastMessage('Vui lòng chọn ít nhất một mục để tải Excel!', 'warning')
    return
  }
  showConfirmModal.value = true
}

const confirmExcelExport = () => {
  const selectedItemsList = filteredData.value.filter(item => selectedItems.value.has(item.id))
  exportToExcel(selectedItemsList)
  showConfirmModal.value = false
}

const exportToExcel = (data: any[]) => {
  try {
    // Import xlsx dynamically
    import('xlsx').then((XLSX) => {
      // Create Excel data
      const excelData = data.map((item, index) => {
        const row: any = { 'STT': index + 1 }
        
        // Add all columns dynamically
        props.columns.forEach(column => {
          if (column.key !== 'actions') {
            const value = getColumnValue(item, column.key)
            if (column.type === 'date') {
              row[column.label] = formatDate(value)
            } else if (column.type === 'status') {
              row[column.label] = value === 1 ? 'Hoạt động' : 'Ngừng hoạt động'
            } else {
              row[column.label] = value || ''
            }
          }
        })
        
        return row
      })

      // Create workbook and worksheet
      const ws = XLSX.utils.json_to_sheet(excelData)
      const wb = XLSX.utils.book_new()
      XLSX.utils.book_append_sheet(wb, ws, 'Danh sách')

      // Auto-size columns
      const colWidths = props.columns.map(col => ({ wch: 15 }))
      ws['!cols'] = colWidths

      // Generate filename with current date
      const now = new Date()
      const dateStr = now.toISOString().split('T')[0]
      const filename = `Danh_sach_${props.entityName}_${dateStr}.xlsx`

      // Save file
      XLSX.writeFile(wb, filename)
      
      showToastMessage(`Đã tải Excel thành công! (${data.length} ${props.entityName})`, 'success')
      
      // Clear selection after export
      selectedItems.value.clear()
      selectAll.value = false
    })
  } catch (error) {
    console.error('Lỗi khi tải Excel:', error)
    showToastMessage('Có lỗi xảy ra khi tải Excel!', 'error')
  }
}

const formatColumnValue = (item: any, column: Column) => {
  const value = item[column.key]
  
  if (column.type === 'date') {
    return formatDate(value)
  }
  
  if (column.type === 'code') {
    return value || `${column.key.toUpperCase()}${String(item.id).padStart(5, '0')}`
  }
  
  if (column.type === 'status') {
    const statusClass = value === 1 ? 'status-active' : 'status-inactive'
    const statusText = value === 1 ? 'Hoạt động' : 'Không hoạt động'
    return `<span class="${statusClass}">${statusText}</span>`
  }
  
  return value || '-'
}

const handleSearch = () => {
  baseHandleSearch()
}

const handleStatusFilter = () => {
  // Reset to first page when filter changes
  currentPage.value = 1
}


const openForm = (item?: any) => {
  emit('openForm', item)
}

const exportExcel = () => {
  emit('exportExcel')
}

// Status toggle methods
const toggleItemStatus = (item: any) => {
  if (isUpdatingStatus.value) return
  
  isUpdatingStatus.value = true
  
  try {
    // Emit event to parent component to handle status toggle
    emit('toggleStatus', item)
    
    // Show toast message
    const newStatus = (item.trangThai || 0) === 1 ? 0 : 1
    const statusText = newStatus === 1 ? 'Hoạt động' : 'Ngừng hoạt động'
    const message = newStatus === 1 
      ? `Đã chuyển ${props.entityName} "${getItemName(item)}" sang trạng thái <span style="color: #28a745; font-weight: bold;">${statusText}</span>`
      : `Đã chuyển ${props.entityName} "${getItemName(item)}" sang trạng thái <span style="color: #dc3545; font-weight: bold;">${statusText}</span>`
    showToastMessage(message, 'success')
    
  } catch (error: any) {
    console.error('Lỗi khi cập nhật trạng thái:', error)
    showToastMessage('Không thể cập nhật trạng thái: ' + (error.response?.data?.message || error.message), 'error')
  } finally {
    isUpdatingStatus.value = false
  }
}

const getItemName = (item: any) => {
  // Try to get name from common fields
  return item.ten || item.tenMau || item.tenHeDieuHanh || item.tenCpu || item.tenGpu || 
         item.dungLuongPin || item.thongSo || item.kichThuoc || item.tenHang || 
         item.tenChip || item.tenRam || item.dungLuong || item.ten || 'mục này'
}

const getStatusClass = (item: any) => {
  const status = item.trangThai || 0
  if (status === 1) {
    return 'status-active'
  } else {
    return 'status-inactive'
  }
}

const getStatusText = (item: any) => {
  const status = item.trangThai || 0
  return status === 1 ? 'Hoạt động' : 'Ngừng hoạt động'
}

const getToggleTooltip = (item: any) => {
  const quantity = item.tongImei || item.soLuong || 0
  const status = item.trangThai || 0
  
  if (status === 0 && quantity === 0) {
    return 'Không thể chuyển sang hoạt động vì số lượng = 0'
  }
  return ''
}
</script>

<style scoped>
@import '@/styles/admin-layout.css';

/* Filter Card - Copy from SanPhamPage */
.filter-card {
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.filter-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 20px;
  border-bottom: 1px solid #e0e0e0;
  background: #f8f9fa;
  border-radius: 8px 8px 0 0;
}

.filter-icon {
  width: 16px;
  height: 16px;
  object-fit: contain;
  opacity: 0.6;
}

.filter-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.filter-content {
  padding: 20px;
}

.filter-row {
  display: flex;
  gap: 20px;
  margin-bottom: 16px;
}

.filter-row:last-child {
  margin-bottom: 0;
}

.filter-group {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.filter-group label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.search-input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}

.search-input:focus {
  border-color: #10b981;
}

.radio-group {
  display: flex;
  gap: 16px;
  align-items: center;
}

.radio-label {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-size: 14px;
}

.radio-label input[type="radio"] {
  display: none;
}

.radio-custom {
  width: 16px;
  height: 16px;
  border: 2px solid #ddd;
  border-radius: 50%;
  position: relative;
  transition: all 0.2s;
}

.radio-label input[type="radio"]:checked + .radio-custom {
  border-color: #28a745;
}

.radio-label input[type="radio"]:checked + .radio-custom::after {
  content: '';
  position: absolute;
  top: 2px;
  left: 2px;
  width: 8px;
  height: 8px;
  background: #28a745;
  border-radius: 50%;
}

.filter-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-top: 1px solid #e0e0e0;
  background: #f8f9fa;
  border-radius: 0 0 8px 8px;
}

.filter-info {
  font-size: 14px;
  color: #666;
}

.product-count {
  color: #10b981;
  font-weight: 600;
}

.filter-actions {
  display: flex;
  gap: 8px;
}

/* Table header and data alignment */
:deep(.data-table th),
:deep(.data-table td) {
  text-align: center !important;
}

/* Date column width */
:deep(.date-col) {
  width: 120px !important;
  min-width: 120px !important;
  text-align: center;
}

/* Status column width */
:deep(.status-col) {
  width: 140px !important;
  min-width: 140px !important;
  text-align: center;
}

/* Status toggle column */
.status-toggle-col {
  text-align: center;
  white-space: nowrap;
  width: 180px;
  min-width: 180px;
}

/* Status Toggle Styles */
.status-toggle {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.toggle-switch {
  position: relative;
  display: inline-block;
  width: 44px;
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
  background-color: #ccc;
  transition: .4s;
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
  transition: .4s;
  border-radius: 50%;
}

input:checked + .toggle-slider {
  background-color: #28a745;
}

input:checked + .toggle-slider:before {
  transform: translateX(20px);
}

input:disabled + .toggle-slider {
  opacity: 0.6;
  cursor: not-allowed;
  background-color: #6c757d !important;
}

input:disabled + .toggle-slider:before {
  background-color: #f8f9fa;
  border: 1px solid #dee2e6;
}

.status-text-container {
  display: flex;
  justify-content: center;
  width: 100%;
}

.status-text {
  font-size: 11px;
  font-weight: 500;
  text-align: center;
  padding: 2px 6px;
  border-radius: 8px;
  white-space: nowrap;
}

.status-active {
  background: #28a745;
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-inactive {
  background: #dc3545;
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

/* Action buttons with icons */
.action-col {
  text-align: center;
  white-space: nowrap;
  width: 120px;
  min-width: 120px;
}

.edit-btn {
  background: none;
  border: none;
  padding: 8px;
  margin: 0 4px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.edit-btn:hover {
  background-color: #e3f2fd;
  transform: translateY(-1px);
}

.action-icon {
  width: 18px;
  height: 18px;
  object-fit: contain;
}

.edit-btn .action-icon {
  filter: brightness(0) saturate(100%) invert(27%) sepia(51%) saturate(2878%) hue-rotate(346deg) brightness(104%) contrast(97%);
}

/* Button disabled state */
.btn-secondary:disabled {
  background: #9ca3af;
  cursor: not-allowed;
  opacity: 0.6;
}

.btn-secondary:disabled:hover {
  background: #9ca3af;
  transform: none;
}

/* Checkbox styles */
.checkbox-col {
  width: 50px;
  text-align: center;
}

.checkbox {
  width: 16px;
  height: 16px;
  cursor: pointer;
}
</style>
