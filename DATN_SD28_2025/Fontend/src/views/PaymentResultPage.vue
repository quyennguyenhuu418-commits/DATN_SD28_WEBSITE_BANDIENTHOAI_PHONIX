<template>
  <div class="payment-result-container">
    <div class="payment-result-card">
      <div v-if="loading" class="loading-section">
        <div class="loading-spinner"></div>
        <h2>Đang xử lý thanh toán...</h2>
        <p>Vui lòng chờ trong giây lát</p>
      </div>

      <div v-else-if="paymentResult.success" class="success-section">
        <div class="success-icon">✅</div>
        <h2>Thanh toán thành công!</h2>
        <p class="success-message">Đơn hàng của bạn đã được xử lý thành công</p>
        
        <div v-if="paymentResult.orderId" class="order-info">
          <p><strong>Mã đơn hàng:</strong> {{ paymentResult.orderCode }}</p>
          <p><strong>Số tiền:</strong> {{ formatCurrency(paymentResult.amount) }}</p>
          <p><strong>Mã giao dịch:</strong> {{ paymentResult.transactionNo }}</p>
        </div>

        <div class="action-buttons">
          <button @click="goToOrderTracking" class="btn-primary">
            Theo dõi đơn hàng
          </button>
          <button @click="goToHome" class="btn-secondary">
            Về trang chủ
          </button>
        </div>
      </div>

      <div v-else class="error-section">
        <div class="error-icon">❌</div>
        <h2>Thanh toán thất bại</h2>
        <p class="error-message">{{ paymentResult.message || 'Có lỗi xảy ra trong quá trình thanh toán' }}</p>
        
        <div class="action-buttons">
          <button @click="goToPOS" class="btn-primary">
            Thử lại
          </button>
          <button @click="goToHome" class="btn-secondary">
            Về trang chủ
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../services/api'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const paymentResult = ref({
  success: false,
  message: '',
  orderId: null,
  orderCode: '',
  amount: 0,
  transactionNo: ''
})

onMounted(async () => {
  try {
    // Extract parameters from URL
    const paramObj = {}
    for (const [key, value] of Object.entries(route.query)) {
      paramObj[key] = value
    }

    console.log('Payment result params:', paramObj)

    // Call backend to verify payment
    const response = await api.get('/api/payments/vnpay/return', { params: paramObj })
    paymentResult.value = response.data

    console.log('Payment result:', paymentResult.value)
    console.log('Order ID:', paymentResult.value.orderId)
    console.log('Order Code:', paymentResult.value.orderCode)

    if (paymentResult.value.success) {
      // Show success notification and redirect to order tracking
      if (paymentResult.value.orderCode) {
        console.log('Redirecting to order tracking with Code:', paymentResult.value.orderCode)
        // Redirect to order tracking page with orderCode (maHoaDon)
        setTimeout(() => {
          window.location.href = `/hoa-don?track=${paymentResult.value.orderCode}`
        }, 2000)
        return
      } else if (paymentResult.value.orderId) {
        console.log('Redirecting to order tracking with ID:', paymentResult.value.orderId)
        // Fallback: use orderId if orderCode not available
        setTimeout(() => {
          window.location.href = `/hoa-don?track=${paymentResult.value.orderId}`
        }, 2000)
        return
      }
      
      // Fallback: redirect to orders list
      setTimeout(() => {
        router.push('/hoa-don')
      }, 2000)
    }

  } catch (error) {
    console.error('Error processing payment result:', error)
    paymentResult.value = {
      success: false,
      message: 'Lỗi xử lý thanh toán: ' + (error.response?.data?.message || error.message)
    }
  } finally {
    loading.value = false
  }
})

const formatCurrency = (amount) => {
  if (!amount) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}

const goToOrderTracking = () => {
  if (paymentResult.value.orderCode) {
    window.location.href = `/hoa-don?track=${paymentResult.value.orderCode}`
  } else if (paymentResult.value.orderId) {
    window.location.href = `/hoa-don?track=${paymentResult.value.orderId}`
  } else {
    router.push('/hoa-don')
  }
}

const goToPOS = () => {
  router.push('/pos')
}

const goToHome = () => {
  router.push('/')
}
</script>

<style scoped>
.payment-result-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.payment-result-card {
  background: white;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  text-align: center;
  max-width: 500px;
  width: 100%;
}

.loading-section {
  padding: 40px 20px;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.success-section .success-icon,
.error-section .error-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.success-section h2 {
  color: #10b981;
  margin-bottom: 10px;
  font-size: 28px;
}

.error-section h2 {
  color: #ef4444;
  margin-bottom: 10px;
  font-size: 28px;
}

.success-message,
.error-message {
  color: #6b7280;
  margin-bottom: 30px;
  font-size: 16px;
}

.order-info {
  background: #f9fafb;
  border-radius: 8px;
  padding: 20px;
  margin: 20px 0;
  text-align: left;
}

.order-info p {
  margin: 8px 0;
  color: #374151;
}

.action-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}

.btn-primary,
.btn-secondary {
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
  font-size: 14px;
}

.btn-primary {
  background: #667eea;
  color: white;
}

.btn-primary:hover {
  background: #5a67d8;
  transform: translateY(-1px);
}

.btn-secondary {
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
}

.btn-secondary:hover {
  background: #e5e7eb;
  transform: translateY(-1px);
}

@media (max-width: 480px) {
  .payment-result-card {
    padding: 20px;
    margin: 10px;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .btn-primary,
  .btn-secondary {
    width: 100%;
  }
}
</style>
