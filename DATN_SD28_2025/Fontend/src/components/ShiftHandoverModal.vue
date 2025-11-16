<template>
  <div v-if="isOpen" class="modal-overlay" @click="handleOverlayClick">
    <div class="modal-container" @click.stop>
      <div class="modal-header">
        <h3 class="modal-title">Xác nhận giao ca</h3>
        <button v-if="canClose" class="close-btn" @click="closeModal">
          <i class="fas fa-times"></i>
        </button>
        <div v-else class="close-disabled">
          <i class="fas fa-lock"></i>
        </div>
      </div>
      
      <div class="modal-body">
        <div v-if="loading" class="loading-container">
          <div class="spinner"></div>
          <p>Đang tải thông tin giao ca...</p>
        </div>
        
        <div v-else-if="error" class="error-container">
          <div class="error-icon">
            <i class="fas fa-exclamation-triangle"></i>
          </div>
          <p class="error-message">{{ error }}</p>
          <button class="retry-btn" @click="loadPendingHandovers">Thử lại</button>
        </div>
        
        <div v-else-if="confirmedHandovers.length > 0 && pendingHandovers.length === 0" class="success-container">
          <div class="success-icon">
            <i class="fas fa-check-circle"></i>
          </div>
          <h4>Xác nhận giao ca thành công!</h4>
          <p>Bạn đã xác nhận {{ confirmedHandovers.length }} giao ca thành công.</p>
          <div class="success-details">
            <div v-for="handover in confirmedHandovers" :key="handover.id" class="confirmed-item">
              <i class="fas fa-check"></i>
              <span>{{ handover.maGiaoCa }} - {{ handover.nhanVienGiao?.hoTen }}</span>
            </div>
          </div>
          <p class="redirect-message">Đang chuyển hướng đến trang chủ...</p>
        </div>
        
        <div v-else-if="pendingHandovers.length === 0 && !loading" class="no-handovers">
          <div class="no-handovers-icon">
            <i class="fas fa-check-circle"></i>
          </div>
          <h4>Không có giao ca nào đang chờ xác nhận</h4>
          <p>Bạn có thể tiếp tục làm việc bình thường.</p>
        </div>
        
        <div v-else-if="loading" class="loading-state">
          <div class="loading-spinner">
            <i class="fas fa-spinner fa-spin"></i>
          </div>
          <p>Đang tải thông tin giao ca...</p>
        </div>
        
        <div v-else class="handover-list">
          <div v-for="handover in pendingHandovers" :key="handover.id" class="handover-item">
            <div class="handover-header">
              <div class="handover-info">
                <h4 class="handover-title">{{ handover.maGiaoCa }}</h4>
                <p class="handover-subtitle">Giao ca từ {{ handover.nhanVienGiao?.hoTen || 'Nhân viên' }}</p>
              </div>
              <div class="handover-status">
                <span class="status-badge pending">Chờ xác nhận</span>
              </div>
            </div>
            
            <div class="handover-details">
              <div class="detail-row">
                <span class="detail-label">Ngày giao ca:</span>
                <span class="detail-value">{{ formatDateTime(handover.ngayGiaoCa) }}</span>
              </div>
              
              <div class="detail-row">
                <span class="detail-label">Số tiền đầu ca:</span>
                <span class="detail-value amount">{{ formatCurrency(handover.soTienDauCa) }}</span>
              </div>
              
              <div class="detail-row">
                <span class="detail-label">Số tiền cuối ca:</span>
                <span class="detail-value amount">{{ formatCurrency(handover.soTienCuoiCa) }}</span>
              </div>
              
              <div v-if="handover.ghiChu" class="detail-row">
                <span class="detail-label">Ghi chú:</span>
                <span class="detail-value">{{ handover.ghiChu }}</span>
              </div>
              
              <div v-if="handover.baoCaoCongViec" class="detail-row">
                <span class="detail-label">Báo cáo công việc:</span>
                <span class="detail-value">{{ handover.baoCaoCongViec }}</span>
              </div>
              
              <div v-if="handover.suCoBatThuong" class="detail-row warning">
                <span class="detail-label">Sự cố bất thường:</span>
                <span class="detail-value">{{ handover.suCoBatThuong }}</span>
              </div>
              
              <div v-if="handover.congViecTonDong" class="detail-row warning">
                <span class="detail-label">Công việc tồn đọng:</span>
                <span class="detail-value">{{ handover.congViecTonDong }}</span>
              </div>
            </div>
            
            <div class="handover-actions">
              <button 
                class="confirm-btn" 
                @click="confirmHandover(handover)"
                :disabled="confirming"
                :class="{ 'confirming': confirming }"
              >
                <i v-if="confirming" class="fas fa-spinner fa-spin"></i>
                <i v-else class="fas fa-check"></i>
                <span v-if="confirming">Đang xác nhận...</span>
                <span v-else>Xác nhận giao ca</span>
              </button>
              
              <!-- Success indicator -->
              <div v-if="confirmedHandovers.some(c => c.id === handover.id)" class="success-indicator">
                <i class="fas fa-check-circle"></i>
                <span>Đã xác nhận</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div v-if="canClose" class="modal-footer">
        <button class="cancel-btn" @click="closeModal">
          <i class="fas fa-times"></i>
          Đóng
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed, watch } from 'vue'
import { useAuthStore } from '@/stores/authStore'
import api from '@/services/api'

export default {
  name: 'ShiftHandoverModal',
  props: {
    isOpen: {
      type: Boolean,
      default: false
    }
  },
  emits: ['close', 'confirm'],
  setup(props, { emit }) {
    const authStore = useAuthStore()
    const loading = ref(false)
    const error = ref(null)
    const confirming = ref(false)
    const pendingHandovers = ref([])
    const confirmedHandovers = ref([])

    // Computed property để kiểm tra có thể đóng modal hay không
    const canClose = computed(() => {
      // Chỉ có thể đóng khi không có giao ca chờ xác nhận
      return pendingHandovers.value.length === 0
    })

    const loadPendingHandovers = async () => {
      if (!authStore || !authStore.user?.id) {
        console.log('ShiftHandoverModal: authStore or user not available yet, retrying in 500ms...')
        // Retry after 500ms if authStore is not ready
        setTimeout(() => {
          if (authStore && authStore.user?.id) {
            loadPendingHandovers()
          }
        }, 500)
        return
      }
      
      console.log('ShiftHandoverModal: Loading pending handovers for user ID:', authStore.user.id)
      loading.value = true
      error.value = null
      
      try {
        const response = await api.get(`/api/auth/pending-shift-handover?nhanVienId=${authStore.user.id}`)
        console.log('ShiftHandoverModal: Received response:', response.data)
        console.log('ShiftHandoverModal: Response status:', response.status)
        console.log('ShiftHandoverModal: Response data type:', typeof response.data)
        console.log('ShiftHandoverModal: Response data length:', Array.isArray(response.data) ? response.data.length : 'Not an array')
        pendingHandovers.value = response.data || []
        console.log('ShiftHandoverModal: Set pendingHandovers to:', pendingHandovers.value)
      } catch (err) {
        console.error('ShiftHandoverModal: Error loading pending handovers:', err)
        console.error('ShiftHandoverModal: Error response:', err.response)
        error.value = 'Không thể tải thông tin giao ca. Vui lòng thử lại.'
      } finally {
        loading.value = false
      }
    }

    const confirmHandover = async (handover) => {
      confirming.value = true
      
      try {
        console.log('ShiftHandoverModal: Confirming handover:', handover.id)
        const response = await api.post('/api/auth/confirm-shift-handover', {
          giaoCaId: handover.id,
          nhanVienNhanId: authStore.user.id
        })
        
        console.log('ShiftHandoverModal: Confirm response:', response.data)
        
        // Show success message immediately
        console.log('ShiftHandoverModal: Handover confirmed successfully')
        
        // Add to confirmed list
        confirmedHandovers.value.push({
          ...handover,
          status: 'confirmed',
          confirmedAt: new Date().toISOString()
        })
        
        // Remove confirmed handover from pending list
        pendingHandovers.value = pendingHandovers.value.filter(h => h.id !== handover.id)
        
        // Emit confirm event to parent
        emit('confirm', response.data.giaoCa)
        
        // If no more handovers, close modal after a short delay
        if (pendingHandovers.value.length === 0) {
          setTimeout(() => {
            closeModal()
          }, 1500) // Give user time to see the success
        }
      } catch (err) {
        console.error('ShiftHandoverModal: Error confirming handover:', err)
        error.value = err.response?.data?.message || 'Không thể xác nhận giao ca. Vui lòng thử lại.'
      } finally {
        confirming.value = false
      }
    }

    const closeModal = () => {
      // Chỉ cho phép đóng khi không có giao ca chờ xác nhận
      if (canClose.value) {
        emit('close')
      }
    }

    const handleOverlayClick = () => {
      // Không cho phép đóng khi click overlay nếu có giao ca chờ xác nhận
      if (canClose.value) {
        closeModal()
      }
    }

    const formatDateTime = (dateTime) => {
      if (!dateTime) return 'N/A'
      try {
        let dateStr = dateTime
        // Backend gửi LocalDateTime dạng "2025-10-31T09:52:26" (không có timezone)
        // Coi nó là Vietnam time (UTC+7), thêm timezone vào string để parse đúng
        if (typeof dateTime === 'string') {
          // Nếu string không có timezone indicator (Z, +, - ở cuối), thêm +07:00 (Vietnam timezone)
          if (!dateTime.includes('Z') && !dateTime.includes('+') && !dateTime.match(/-\d{2}:\d{2}$/)) {
            // Loại bỏ milliseconds nếu có (format: "2025-10-31T09:52:26.123")
            dateStr = dateTime.split('.')[0]
            // Thêm timezone Vietnam (UTC+7)
            dateStr = dateStr + '+07:00'
          }
        }
        
        const date = new Date(dateStr)
        
        // Kiểm tra parse thành công
        if (isNaN(date.getTime())) {
          console.warn('Failed to parse datetime:', dateTime)
          return 'N/A'
        }
        
        // Format với locale Vietnam
        const options = {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit',
          hour12: false,
          timeZone: 'Asia/Ho_Chi_Minh'
        }
        return date.toLocaleString('vi-VN', options)
      } catch (error) {
        console.error('Error formatting datetime:', error, dateTime)
        return 'N/A'
      }
    }

    const formatCurrency = (amount) => {
      if (!amount) return '0 ₫'
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(amount)
    }

    onMounted(() => {
      if (props.isOpen) {
        // Thêm delay để đảm bảo authStore đã được khởi tạo
        setTimeout(() => {
          if (authStore && authStore.user?.id) {
            loadPendingHandovers()
          } else {
            console.log('ShiftHandoverModal: authStore not ready on mount, retrying...')
            // Retry every 200ms until authStore is ready
            const retryInterval = setInterval(() => {
              if (authStore && authStore.user?.id) {
                console.log('ShiftHandoverModal: authStore ready, loading handovers')
                loadPendingHandovers()
                clearInterval(retryInterval)
              }
            }, 200)
            
            // Stop retrying after 5 seconds
            setTimeout(() => {
              clearInterval(retryInterval)
            }, 5000)
          }
        }, 100)
      }
    })

    // Watch for authStore changes
    watch(() => authStore?.user?.id, (newUserId) => {
      if (newUserId && props.isOpen) {
        console.log('ShiftHandoverModal: authStore user ID changed to:', newUserId)
        loadPendingHandovers()
      }
    }, { immediate: true })

    // Watch for isOpen prop changes
    watch(() => props.isOpen, (newVal) => {
      if (newVal) {
        console.log('ShiftHandoverModal: Modal opened, checking authStore...')
        if (authStore && authStore.user?.id) {
          console.log('ShiftHandoverModal: authStore ready, loading handovers')
          loadPendingHandovers()
        } else {
          console.log('ShiftHandoverModal: authStore not ready, will retry...')
          // Retry every 200ms until authStore is ready
          const retryInterval = setInterval(() => {
            if (authStore && authStore.user?.id) {
              console.log('ShiftHandoverModal: authStore ready, loading handovers')
              loadPendingHandovers()
              clearInterval(retryInterval)
            }
          }, 200)
          
          // Stop retrying after 5 seconds
          setTimeout(() => {
            clearInterval(retryInterval)
          }, 5000)
        }
      }
    })

    return {
      loading,
      error,
      confirming,
      pendingHandovers,
      confirmedHandovers,
      loadPendingHandovers,
      confirmHandover,
      closeModal,
      formatDateTime,
      formatCurrency
    }
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
}

.modal-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  max-width: 800px;
  width: 90%;
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-header {
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #f8fafc;
}

.modal-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  color: #6b7280;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s;
}

.close-btn:hover {
  background: #e5e7eb;
  color: #374151;
}

.close-disabled {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  color: #ccc;
  font-size: 16px;
  cursor: not-allowed;
  background: #f5f5f5;
  border-radius: 4px;
}

.modal-body {
  padding: 24px;
  flex: 1;
  overflow-y: auto;
}

.loading-container {
  text-align: center;
  padding: 40px 20px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e5e7eb;
  border-top: 4px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-container {
  text-align: center;
  padding: 40px 20px;
}

.error-icon {
  font-size: 48px;
  color: #ef4444;
  margin-bottom: 16px;
}

.error-message {
  color: #ef4444;
  margin-bottom: 20px;
}

.retry-btn {
  background: #3b82f6;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: background 0.2s;
}

.retry-btn:hover {
  background: #2563eb;
}

.no-handovers {
  text-align: center;
  padding: 40px 20px;
}

.no-handovers-icon {
  font-size: 48px;
  color: #10b981;
  margin-bottom: 16px;
}

.no-handovers h4 {
  margin: 0 0 8px 0;
  color: #1f2937;
}

.no-handovers p {
  margin: 0;
  color: #6b7280;
}

.success-container {
  text-align: center;
  padding: 40px 20px;
}

.success-icon {
  font-size: 48px;
  color: #10b981;
  margin-bottom: 16px;
}

.success-container h4 {
  margin: 0 0 8px 0;
  color: #1f2937;
}

.success-container p {
  margin: 0;
  color: #6b7280;
}

.success-details {
  margin: 20px 0;
  padding: 16px;
  background: #f0fdf4;
  border-radius: 8px;
  border: 1px solid #bbf7d0;
}

.confirmed-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  color: #166534;
  font-weight: 500;
}

.confirmed-item i {
  color: #10b981;
}

.redirect-message {
  margin-top: 16px;
  font-style: italic;
  color: #059669;
}

.loading-state {
  text-align: center;
  padding: 40px 20px;
}

.loading-spinner {
  font-size: 48px;
  color: #007bff;
  margin-bottom: 20px;
}

.loading-state p {
  color: #666;
  margin-bottom: 0;
}

.waiting-auth {
  text-align: center;
  padding: 40px 20px;
}

.waiting-icon {
  font-size: 48px;
  color: #ffc107;
  margin-bottom: 20px;
}

.waiting-auth h4 {
  color: #333;
  margin-bottom: 10px;
}

.waiting-auth p {
  color: #666;
  margin-bottom: 0;
}

.handover-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.handover-item {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px;
  background: #f9fafb;
}

.handover-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.handover-title {
  margin: 0 0 4px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.handover-subtitle {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.pending {
  background: #fef3c7;
  color: #d97706;
}

.handover-details {
  margin-bottom: 20px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 8px 0;
  border-bottom: 1px solid #f3f4f6;
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-row.warning {
  background: #fef2f2;
  padding: 12px;
  border-radius: 6px;
  margin: 8px 0;
}

.detail-label {
  font-weight: 500;
  color: #374151;
  min-width: 140px;
}

.detail-value {
  color: #1f2937;
  text-align: right;
  flex: 1;
}

.detail-value.amount {
  font-weight: 600;
}


.handover-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
}

.success-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #10b981;
  font-weight: 500;
  font-size: 14px;
}

.success-indicator i {
  font-size: 16px;
}

.confirm-btn {
  background: #059669;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
}

.confirm-btn:hover:not(:disabled) {
  background: #047857;
}

.confirm-btn:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.confirm-btn.confirming {
  background: #f59e0b;
  cursor: not-allowed;
}

.confirm-btn.confirming:hover {
  background: #f59e0b;
}

.modal-footer {
  padding: 20px 24px;
  border-top: 1px solid #e5e7eb;
  display: flex;
  justify-content: flex-end;
  background: #f8fafc;
}

.cancel-btn {
  background: #6b7280;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: background 0.2s;
}

.cancel-btn:hover {
  background: #4b5563;
}

/* Responsive */
@media (max-width: 768px) {
  .modal-container {
    width: 95%;
    margin: 20px;
  }
  
  .handover-header {
    flex-direction: column;
    gap: 12px;
  }
  
  .detail-row {
    flex-direction: column;
    gap: 4px;
  }
  
  .detail-value {
    text-align: left;
  }
}
</style>
