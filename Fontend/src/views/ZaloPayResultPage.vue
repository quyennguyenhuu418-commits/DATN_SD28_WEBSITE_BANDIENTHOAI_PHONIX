<template>
  <div class="min-h-screen bg-gray-50 flex items-center justify-center p-4">
    <div class="max-w-md w-full bg-white rounded-lg shadow-lg p-6">
      <!-- Header -->
      <div class="text-center mb-6">
        <div class="w-16 h-16 mx-auto mb-4 bg-blue-100 rounded-full flex items-center justify-center">
          <svg v-if="isSuccess" class="w-8 h-8 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
          </svg>
          <svg v-else class="w-8 h-8 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
          </svg>
        </div>
        <h1 class="text-2xl font-bold text-gray-900 mb-2">
          {{ isSuccess ? 'Thanh toán thành công!' : 'Thanh toán thất bại' }}
        </h1>
        <p class="text-gray-600">
          {{ isSuccess ? 'Cảm ơn bạn đã sử dụng ZaloPay' : 'Vui lòng thử lại sau' }}
        </p>
      </div>

      <!-- Transaction Details -->
      <div v-if="transactionDetails" class="bg-gray-50 rounded-lg p-4 mb-6">
        <h3 class="font-semibold text-gray-900 mb-3">Chi tiết giao dịch</h3>
        <div class="space-y-2 text-sm">
          <div class="flex justify-between">
            <span class="text-gray-600">Mã giao dịch:</span>
            <span class="font-medium">{{ transactionDetails.appTransId }}</span>
          </div>
          <div v-if="transactionDetails.amount" class="flex justify-between">
            <span class="text-gray-600">Số tiền:</span>
            <span class="font-medium text-green-600">{{ formatCurrency(transactionDetails.amount) }}</span>
          </div>
          <div v-if="transactionDetails.description" class="flex justify-between">
            <span class="text-gray-600">Mô tả:</span>
            <span class="font-medium">{{ transactionDetails.description }}</span>
          </div>
          <div v-if="transactionDetails.orderCode" class="flex justify-between">
            <span class="text-gray-600">Mã đơn hàng:</span>
            <span class="font-medium">{{ transactionDetails.orderCode }}</span>
          </div>
        </div>
      </div>

      <!-- Error Message -->
      <div v-if="!isSuccess && errorMessage" class="bg-red-50 border border-red-200 rounded-lg p-4 mb-6">
        <div class="flex">
          <div class="flex-shrink-0">
            <svg class="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
            </svg>
          </div>
          <div class="ml-3">
            <h3 class="text-sm font-medium text-red-800">Lỗi thanh toán</h3>
            <div class="mt-2 text-sm text-red-700">
              {{ errorMessage }}
            </div>
          </div>
        </div>
      </div>

      <!-- Actions -->
      <div class="space-y-3">
        <button
          @click="goToPos"
          class="w-full bg-blue-600 text-white py-3 px-4 rounded-lg font-medium hover:bg-blue-700 transition-colors"
        >
          {{ isSuccess ? 'Tạo đơn hàng mới' : 'Thử lại thanh toán' }}
        </button>
        
        <button
          @click="goToOrders"
          v-if="isSuccess"
          class="w-full bg-gray-100 text-gray-700 py-3 px-4 rounded-lg font-medium hover:bg-gray-200 transition-colors"
        >
          Xem đơn hàng
        </button>
        
        <button
          @click="goHome"
          class="w-full bg-gray-100 text-gray-700 py-3 px-4 rounded-lg font-medium hover:bg-gray-200 transition-colors"
        >
          Về trang chủ
        </button>
      </div>

      <!-- Footer -->
      <div class="mt-6 text-center">
        <p class="text-xs text-gray-500">
          Được hỗ trợ bởi ZaloPay
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { paymentApi } from '@/services/api'

const router = useRouter()
const route = useRoute()

const isSuccess = ref(false)
const errorMessage = ref('')
const transactionDetails = ref(null)

const formatCurrency = (amount) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}

const goToPos = () => {
  router.push('/pos')
}

const goToOrders = () => {
  router.push('/orders')
}

const goHome = () => {
  router.push('/')
}

const handleZaloPayReturn = async () => {
  try {
    const params = route.query
    console.log('ZaloPay return params:', params)
    
    const response = await paymentApi.handleZaloPayReturn(params)
    const result = response.data
    
    console.log('ZaloPay return result:', result)
    
    if (result.success) {
      isSuccess.value = true
      transactionDetails.value = {
        appTransId: result.appTransId,
        amount: result.amount,
        description: result.description,
        orderCode: result.orderCode
      }
    } else {
      isSuccess.value = false
      errorMessage.value = result.message || 'Thanh toán thất bại'
    }
  } catch (error) {
    console.error('Error handling ZaloPay return:', error)
    isSuccess.value = false
    errorMessage.value = 'Có lỗi xảy ra khi xử lý thanh toán'
  }
}

onMounted(() => {
  handleZaloPayReturn()
})
</script>
