<template>
  <div class="customer-voucher-page">
    <div class="page-header">
      <h1>Voucher Của Tôi</h1>
      <div class="customer-info">
        <select v-model="selectedCustomerId" @change="loadCustomerVouchers" class="customer-select">
          <option value="">-- Chọn khách hàng để xem voucher --</option>
          <option v-for="customer in customers" :key="customer.id" :value="customer.id">
            {{ customer.hoTen }} ({{ customer.soDienThoai }})
          </option>
        </select>
      </div>
    </div>

    <div v-if="selectedCustomerId" class="tabs-container">
      <div class="tabs">
        <button 
          class="tab-button" 
          :class="{ active: activeTab === 'available' }"
          @click="switchTab('available')"
        >
          Voucher khả dụng ({{ vouchers.length }})
        </button>
        <button 
          class="tab-button" 
          :class="{ active: activeTab === 'used' }"
          @click="switchTab('used')"
        >
          Đã sử dụng ({{ usedVouchers.length }})
        </button>
      </div>
    </div>

    <div v-if="loading" class="loading">
      Đang tải voucher...
    </div>

    <div v-else-if="activeTab === 'available' && vouchers.length === 0 && selectedCustomerId" class="no-vouchers">
      <div class="empty-state">
        <h3>Không có voucher nào</h3>
        <p>Khách hàng này chưa có voucher nào có thể sử dụng.</p>
      </div>
    </div>

    <div v-else-if="activeTab === 'used' && usedVouchers.length === 0 && selectedCustomerId" class="no-vouchers">
      <div class="empty-state">
        <h3>Chưa sử dụng voucher nào</h3>
        <p>Khách hàng này chưa sử dụng voucher nào.</p>
      </div>
    </div>

    <!-- Available Vouchers -->
    <div v-if="activeTab === 'available' && vouchers.length > 0" class="vouchers-grid">
      <div v-for="voucher in vouchers" :key="voucher.id" class="voucher-card">
        <div class="voucher-header">
          <div class="voucher-type" :class="getVoucherTypeClass(voucher)">
            {{ getVoucherTypeLabel(voucher) }}
          </div>
          <div class="voucher-status" :class="getStatusClass(voucher)">
            {{ getStatusLabel(voucher) }}
          </div>
        </div>
        
        <div class="voucher-content">
          <h3 class="voucher-title">{{ voucher.tenPhieuGiamGia }}</h3>
          <div class="voucher-code">Mã: {{ voucher.maPhieuGiamGia }}</div>
          
          <div class="voucher-value">
            <span class="value">{{ formatDiscountValue(voucher) }}</span>
            <div class="conditions" v-if="voucher.hoaDonToiThieu || voucher.soTienGiamToiDa">
              <div v-if="voucher.hoaDonToiThieu" class="condition">
                Đơn tối thiểu: {{ formatCurrency(voucher.hoaDonToiThieu) }}
              </div>
              <div v-if="voucher.soTienGiamToiDa && isPercentVoucher(voucher)" class="condition">
                Giảm tối đa: {{ formatCurrency(voucher.soTienGiamToiDa) }}
              </div>
            </div>
          </div>
          
          <div class="voucher-validity">
            <div class="date-range">
              <span>{{ formatDate(voucher.ngayBatDau) }} - {{ formatDate(voucher.ngayKetThuc) }}</span>
            </div>
            <div class="usage-count" v-if="voucher.riengTu">
              <span class="private-voucher-badge">Phiếu riêng</span>
              <span class="usage-text">Còn lại: 1 lượt</span>
            </div>
            <div class="usage-count" v-else>
              Còn lại: {{ voucher.soLuongDung }} lượt
            </div>
          </div>
          
          <div v-if="voucher.moTa" class="voucher-description">
            {{ voucher.moTa }}
          </div>
        </div>
        
        <div class="voucher-actions">
          <button 
            class="btn-use-voucher" 
            :disabled="!canUseVoucher(voucher)"
            @click="useVoucher(voucher)"
          >
            {{ getUseButtonText(voucher) }}
          </button>
          <button 
            class="btn-validate" 
            @click="validateVoucher(voucher)"
            :disabled="!selectedCustomerId"
          >
            Kiểm tra quyền sử dụng
          </button>
          <button 
            class="btn-mark-used" 
            @click="markAsUsed(voucher)"
            :disabled="!selectedCustomerId || !canUseVoucher(voucher)"
          >
            Đánh dấu đã dùng
          </button>
        </div>
      </div>
    </div>

    <!-- Used Vouchers -->
    <div v-if="activeTab === 'used' && usedVouchers.length > 0" class="vouchers-grid">
      <div v-for="voucher in usedVouchers" :key="voucher.id" class="voucher-card used-voucher">
        <div class="voucher-header">
          <div class="voucher-type" :class="getVoucherTypeClass(voucher)">
            {{ getVoucherTypeLabel(voucher) }}
          </div>
          <div class="voucher-status status-used">
            Đã sử dụng
          </div>
        </div>
        
        <div class="voucher-content">
          <h3 class="voucher-title">{{ voucher.tenPhieuGiamGia }}</h3>
          <div class="voucher-code">Mã: {{ voucher.maPhieuGiamGia }}</div>
          
          <div class="voucher-value">
            <span class="value">{{ formatDiscountValue(voucher) }}</span>
            <div class="conditions" v-if="voucher.hoaDonToiThieu || voucher.soTienGiamToiDa">
              <div v-if="voucher.hoaDonToiThieu" class="condition">
                Đơn tối thiểu: {{ formatCurrency(voucher.hoaDonToiThieu) }}
              </div>
              <div v-if="voucher.soTienGiamToiDa && isPercentVoucher(voucher)" class="condition">
                Giảm tối đa: {{ formatCurrency(voucher.soTienGiamToiDa) }}
              </div>
            </div>
          </div>
          
          <div class="voucher-validity">
            <div class="date-range">
              <span>{{ formatDate(voucher.ngayBatDau) }} - {{ formatDate(voucher.ngayKetThuc) }}</span>
            </div>
            <div class="usage-info">
              <span class="used-badge">✓ Đã sử dụng</span>
            </div>
          </div>
          
          <div v-if="voucher.moTa" class="voucher-description">
            {{ voucher.moTa }}
          </div>
        </div>
        
        <div class="voucher-actions">
          <div class="used-info">
            Voucher này đã được sử dụng và không thể dùng lại
          </div>
        </div>
      </div>
    </div>

    <Toast ref="toastRef" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '@/services/api'
import Toast from '@/components/Toast.vue'

interface Customer {
  id: number
  hoTen: string
  soDienThoai: string
  email: string
}

interface Voucher {
  id: number
  maPhieuGiamGia: string
  tenPhieuGiamGia: string
  loaiPhieuGiamGia: string
  giaTriGiamGia: number
  soTienGiamToiDa: number
  hoaDonToiThieu: number
  soLuongDung: number
  ngayBatDau: string
  ngayKetThuc: string
  trangThai: number
  riengTu: boolean
  moTa: string
}

const customers = ref<Customer[]>([])
const vouchers = ref<Voucher[]>([])
const usedVouchers = ref<Voucher[]>([])
const selectedCustomerId = ref<number | string>('')
const activeTab = ref<'available' | 'used'>('available')
const loading = ref(false)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

onMounted(async () => {
  await loadCustomers()
})

async function loadCustomers() {
  try {
    const { data } = await api.get<Customer[]>('/api/khach-hang/active')
    customers.value = data
    console.log('Loaded customers:', data.length)
  } catch (error: any) {
    console.error('Lỗi khi tải danh sách khách hàng:', error)
    toastRef.value?.error('Lỗi', `Không thể tải danh sách khách hàng: ${error.response?.status} - ${error.response?.data || error.message}`)
  }
}

async function loadCustomerVouchers() {
  if (!selectedCustomerId.value) {
    vouchers.value = []
    usedVouchers.value = []
    return
  }

  loading.value = true
  console.log('Loading vouchers for customer:', selectedCustomerId.value)
  
  try {
    // First, let's check if we have any vouchers at all
    try {
      const { data: debugData } = await api.get(`/api/phieu-giam-gia/debug/all`)
      console.log('Total vouchers in system:', debugData.totalVouchers)
      console.log('All vouchers:', debugData.vouchers)
    } catch (debugError) {
      console.log('Debug endpoint not available:', debugError)
    }

    // Load available vouchers
    try {
      console.log('Fetching available vouchers...')
      const { data: availableData } = await api.get<Voucher[]>(`/api/phieu-giam-gia/customer/${selectedCustomerId.value}`)
      console.log('Available vouchers raw data:', availableData)
      vouchers.value = Array.isArray(availableData) ? availableData : []
      console.log('Available vouchers loaded:', vouchers.value.length)
    } catch (error: any) {
      console.error('Lỗi khi tải voucher khả dụng:', error)
      console.error('Error details:', error.response?.data, error.response?.status)
      vouchers.value = []
      if (error.response?.status !== 404) {
        toastRef.value?.error('Lỗi', `Không thể tải voucher khả dụng: ${error.response?.data || error.message}`)
      }
    }

    // Load used vouchers
    try {
      console.log('Fetching used vouchers...')
      const { data: usedData } = await api.get<Voucher[]>(`/api/phieu-giam-gia/customer/${selectedCustomerId.value}/used`)
      console.log('Used vouchers raw data:', usedData)
      usedVouchers.value = Array.isArray(usedData) ? usedData : []
      console.log('Used vouchers loaded:', usedVouchers.value.length)
    } catch (error: any) {
      console.error('Lỗi khi tải voucher đã sử dụng:', error)
      console.error('Error details:', error.response?.data, error.response?.status)
      usedVouchers.value = []
      if (error.response?.status !== 404) {
        toastRef.value?.error('Lỗi', `Không thể tải voucher đã sử dụng: ${error.response?.data || error.message}`)
      }
    }
  } catch (error) {
    console.error('Lỗi chung khi tải voucher:', error)
  } finally {
    loading.value = false
  }
}

function switchTab(tab: 'available' | 'used') {
  activeTab.value = tab
}

async function validateVoucher(voucher: Voucher) {
  if (!selectedCustomerId.value) return

  try {
    const response = await api.post('/api/phieu-giam-gia/validate-usage', {
      customerId: selectedCustomerId.value,
      voucherCode: voucher.maPhieuGiamGia
    })
    
    toastRef.value?.success('Hợp lệ', 'Khách hàng có thể sử dụng voucher này')
  } catch (error: any) {
    const message = error.response?.data || 'Khách hàng không có quyền sử dụng voucher này hoặc đã sử dụng rồi'
    toastRef.value?.error('Không hợp lệ', message)
  }
}

async function markAsUsed(voucher: Voucher) {
  if (!selectedCustomerId.value) return

  // Giả lập số tiền giảm (trong thực tế sẽ tính từ đơn hàng)
  const discountAmount = calculateDiscountAmount(voucher, 500000) // Giả sử đơn hàng 500k

  try {
    await api.post('/api/phieu-giam-gia/mark-used', {
      customerId: selectedCustomerId.value,
      voucherCode: voucher.maPhieuGiamGia,
      orderAmount: discountAmount
    })
    
    toastRef.value?.success('Thành công', `Đã đánh dấu voucher đã sử dụng. Giảm: ${formatCurrency(discountAmount)}`)
    
    // Reload vouchers để cập nhật trạng thái
    await loadCustomerVouchers()
  } catch (error: any) {
    const message = error.response?.data || 'Không thể đánh dấu voucher đã sử dụng'
    toastRef.value?.error('Lỗi', message)
  }
}

function calculateDiscountAmount(voucher: Voucher, orderAmount: number): number {
  if (isPercentVoucher(voucher)) {
    let discount = (orderAmount * voucher.giaTriGiamGia) / 100
    if (voucher.soTienGiamToiDa && discount > voucher.soTienGiamToiDa) {
      discount = voucher.soTienGiamToiDa
    }
    return discount
  } else {
    return voucher.giaTriGiamGia
  }
}

function useVoucher(voucher: Voucher) {
  // Demo function - in real app this would integrate with checkout
  toastRef.value?.info('Demo', `Sử dụng voucher: ${voucher.maPhieuGiamGia}`)
}

function canUseVoucher(voucher: Voucher): boolean {
  const now = new Date()
  const startDate = new Date(voucher.ngayBatDau)
  const endDate = new Date(voucher.ngayKetThuc)
  
  return voucher.trangThai === 1 && 
         voucher.soLuongDung > 0 && 
         now >= startDate && 
         now <= endDate
}

function isPercentVoucher(voucher: Voucher): boolean {
  if (voucher.loaiPhieuGiamGia) {
    return voucher.loaiPhieuGiamGia === 'PERCENT'
  }
  return voucher.giaTriGiamGia <= 100
}

function getVoucherTypeClass(voucher: Voucher): string {
  return isPercentVoucher(voucher) ? 'type-percent' : 'type-fixed'
}

function getVoucherTypeLabel(voucher: Voucher): string {
  return isPercentVoucher(voucher) ? 'Giảm %' : 'Giảm tiền'
}

function getStatusClass(voucher: Voucher): string {
  if (!canUseVoucher(voucher)) return 'status-expired'
  return voucher.riengTu ? 'status-private' : 'status-public'
}

function getStatusLabel(voucher: Voucher): string {
  if (!canUseVoucher(voucher)) return 'Hết hạn'
  return voucher.riengTu ? 'Riêng tư' : 'Công khai'
}

function getUseButtonText(voucher: Voucher): string {
  if (!canUseVoucher(voucher)) {
    return 'Không thể sử dụng'
  }
  return 'Sử dụng ngay'
}

function formatDiscountValue(voucher: Voucher): string {
  if (isPercentVoucher(voucher)) {
    return `${voucher.giaTriGiamGia}%`
  } else {
    return formatCurrency(voucher.giaTriGiamGia)
  }
}

function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}

function formatDate(dateString: string): string {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleDateString('vi-VN')
}
</script>

<style scoped>
.customer-voucher-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #e9ecef;
}

.page-header h1 {
  color: #495057;
  margin: 0;
}

.customer-select {
  padding: 10px 15px;
  border: 2px solid #e9ecef;
  border-radius: 6px;
  font-size: 14px;
  min-width: 300px;
  background: white;
}

.customer-select:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
}

.tabs-container {
  margin-bottom: 30px;
}

.tabs {
  display: flex;
  border-bottom: 2px solid #e9ecef;
}

.tab-button {
  padding: 12px 24px;
  background: none;
  border: none;
  border-bottom: 3px solid transparent;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
  color: #6c757d;
  transition: all 0.3s;
}

.tab-button:hover {
  color: #007bff;
  background: #f8f9fa;
}

.tab-button.active {
  color: #007bff;
  border-bottom-color: #007bff;
  background: #f8f9fa;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #6c757d;
  font-size: 16px;
}

.no-vouchers {
  text-align: center;
  padding: 60px 20px;
}

.empty-state h3 {
  color: #6c757d;
  margin-bottom: 10px;
}

.empty-state p {
  color: #adb5bd;
  margin: 0;
}

.vouchers-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.voucher-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: transform 0.3s, box-shadow 0.3s;
}

.voucher-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 15px rgba(0, 0, 0, 0.15);
}

.voucher-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
}

.voucher-type {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
}

.type-percent {
  background: #e3f2fd;
  color: #1976d2;
}

.type-fixed {
  background: #f3e5f5;
  color: #7b1fa2;
}

.voucher-status {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.status-private {
  background: #fff3e0;
  color: #f57c00;
}

.status-public {
  background: #e8f5e8;
  color: #2e7d32;
}

.status-expired {
  background: #ffebee;
  color: #c62828;
}

.status-used {
  background: #e8f5e8;
  color: #2e7d32;
}

.used-voucher {
  opacity: 0.8;
  border: 2px solid #e8f5e8;
}

.used-voucher .voucher-title {
  color: #6c757d;
}

.used-voucher .voucher-value .value {
  color: #6c757d;
}

.used-badge {
  background: #e8f5e8;
  color: #2e7d32;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.used-info {
  text-align: center;
  color: #6c757d;
  font-style: italic;
  padding: 15px;
}

.voucher-content {
  padding: 20px;
}

.voucher-title {
  font-size: 18px;
  font-weight: 600;
  color: #495057;
  margin: 0 0 10px 0;
}

.voucher-code {
  font-size: 14px;
  color: #6c757d;
  font-family: 'Courier New', monospace;
  background: #f8f9fa;
  padding: 4px 8px;
  border-radius: 4px;
  display: inline-block;
  margin-bottom: 15px;
}

.voucher-value {
  margin-bottom: 15px;
}

.voucher-value .value {
  font-size: 24px;
  font-weight: 700;
  color: #007bff;
}

.conditions {
  margin-top: 8px;
}

.condition {
  font-size: 12px;
  color: #6c757d;
  margin-bottom: 4px;
}

.voucher-validity {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  font-size: 14px;
  color: #6c757d;
}

.voucher-description {
  font-size: 14px;
  color: #6c757d;
  font-style: italic;
  margin-bottom: 15px;
}

.voucher-actions {
  display: flex;
  gap: 10px;
  padding: 15px 20px;
  background: #f8f9fa;
  border-top: 1px solid #e9ecef;
}

.btn-use-voucher,
.btn-validate,
.btn-mark-used {
  flex: 1;
  padding: 10px 16px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-use-voucher {
  background: #007bff;
  color: white;
}

.btn-use-voucher:hover:not(:disabled) {
  background: #0056b3;
}

.btn-use-voucher:disabled {
  background: #6c757d;
  cursor: not-allowed;
}

.btn-validate {
  background: #28a745;
  color: white;
}

.btn-validate:hover:not(:disabled) {
  background: #218838;
}

.btn-validate:disabled {
  background: #6c757d;
  cursor: not-allowed;
}

.btn-mark-used {
  background: #ffc107;
  color: #212529;
}

.btn-mark-used:hover:not(:disabled) {
  background: #e0a800;
}

.btn-mark-used:disabled {
  background: #6c757d;
  cursor: not-allowed;
}

.private-voucher-badge {
  background: #fd7e14;
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  margin-right: 8px;
}

.usage-text {
  font-size: 14px;
  color: #28a745;
  font-weight: 500;
}

@media (max-width: 768px) {
  .customer-voucher-page {
    padding: 15px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
  
  .customer-select {
    min-width: auto;
  }
  
  .vouchers-grid {
    grid-template-columns: 1fr;
  }
  
  .voucher-actions {
    flex-direction: column;
  }
}
</style>
