<template>
  <div class="min-h-screen bg-gradient-to-br from-green-50 to-blue-50 flex items-center justify-center p-4">
    <div class="bg-white rounded-2xl shadow-2xl max-w-md w-full p-8 text-center">
      <!-- Success Icon -->
      <div v-if="paymentResult.success" class="mb-6">
        <div class="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
          <svg class="w-10 h-10 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
          </svg>
        </div>
        <h1 class="text-2xl font-bold text-green-600 mb-2">Thanh toán thành công!</h1>
        <p class="text-gray-600">Đơn hàng của bạn đã được xử lý thành công</p>
      </div>

      <!-- Error Icon -->
      <div v-else class="mb-6">
        <div class="w-20 h-20 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
          <svg class="w-10 h-10 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
          </svg>
        </div>
        <h1 class="text-2xl font-bold text-red-600 mb-2">Thanh toán thất bại</h1>
        <p class="text-gray-600">{{ paymentResult.message || 'Có lỗi xảy ra trong quá trình thanh toán' }}</p>
      </div>

      <!-- Payment Details -->
      <div v-if="paymentResult.success" class="bg-gray-50 rounded-lg p-4 mb-6 text-left">
        <h3 class="font-semibold text-gray-800 mb-3">Thông tin giao dịch</h3>
        <div class="space-y-2 text-sm">
          <div class="flex justify-between">
            <span class="text-gray-600">Mã giao dịch:</span>
            <span class="font-medium">{{ paymentResult.transactionNo }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-600">Số tiền:</span>
            <span class="font-medium">{{ formatCurrency(paymentResult.amount) }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-600">Mã đơn hàng:</span>
            <span class="font-medium">{{ paymentResult.txnRef }}</span>
          </div>
        </div>
      </div>

      <!-- Action Buttons -->
      <div class="space-y-3">
        <button
          @click="goToOrders"
          class="w-full bg-blue-600 hover:bg-blue-700 text-white font-medium py-3 px-4 rounded-lg transition-colors"
        >
          Xem đơn hàng
        </button>
        <button
          @click="goToHome"
          class="w-full bg-gray-200 hover:bg-gray-300 text-gray-700 font-medium py-3 px-4 rounded-lg transition-colors"
        >
          Về trang chủ
        </button>
        <button
          @click="goToPOS"
          class="w-full bg-orange-500 hover:bg-orange-600 text-white font-medium py-3 px-4 rounded-lg transition-colors"
        >
          Quay lại POS
        </button>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="mt-4">
        <div class="flex items-center justify-center space-x-2 text-gray-600">
          <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600"></div>
          <span>Đang xử lý...</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../services/api'

export default {
  name: 'PaymentResultPage',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const loading = ref(true)
    const paymentResult = ref({
      success: false,
      message: '',
      transactionNo: '',
      amount: 0,
      txnRef: ''
    })

    const formatCurrency = (amount) => {
      if (!amount) return '0 ₫'
      const numAmount = parseInt(amount) / 100 // VNPay returns amount in cents
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(numAmount)
    }

    const processPaymentResult = async () => {
      try {
        // Get all query parameters
        const params = new URLSearchParams(window.location.search)
        const paramObj = {}
        for (const [key, value] of params) {
          paramObj[key] = value
        }

        // Call backend to verify payment
        const response = await api.get('/api/payments/vnpay/return', { params: paramObj })
        paymentResult.value = response.data

        if (paymentResult.value.success) {
          // TODO: Save order to database here
          console.log('Payment successful, saving order...')
          // You can call an API to save the order with the transaction details
        }

      } catch (error) {
        console.error('Error processing payment result:', error)
        paymentResult.value = {
          success: false,
          message: 'Lỗi xử lý kết quả thanh toán'
        }
      } finally {
        loading.value = false
      }
    }

    const goToOrders = () => {
      router.push('/hoa-don')
    }

    const goToHome = () => {
      router.push('/')
    }

    const goToPOS = () => {
      router.push('/pos')
    }

    onMounted(() => {
      processPaymentResult()
    })

    return {
      loading,
      paymentResult,
      formatCurrency,
      goToOrders,
      goToHome,
      goToPOS
    }
  }
}
</script>
