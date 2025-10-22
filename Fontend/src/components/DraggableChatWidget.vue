<template>
  <div
    v-if="isVisible"
    ref="chatWidget"
    class="fixed bottom-4 right-4 w-80 h-96 bg-white rounded-2xl shadow-2xl border border-gray-200 flex flex-col z-50 transition-all duration-300"
    :style="{ transform: `translate(${position.x}px, ${position.y}px)` }"
  >
    <!-- Header với drag handle -->
    <div
      ref="dragHandle"
      class="bg-gradient-to-r from-orange-500 to-orange-600 text-white p-4 rounded-t-2xl cursor-move flex justify-between items-center"
    >
      <div class="flex items-center space-x-3">
        <div class="w-8 h-8 bg-white bg-opacity-20 rounded-full flex items-center justify-center">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"></path>
          </svg>
        </div>
        <div>
          <h3 class="font-semibold text-lg">Chat hỗ trợ</h3>
          <p class="text-sm text-orange-100" v-if="assignedStaff">Đã kết nối với {{ assignedStaff }}</p>
          <p class="text-sm text-orange-100" v-else>Hỗ trợ</p>
        </div>
      </div>
      <div class="flex items-center space-x-2">
        <button
          @click="toggleMinimize"
          class="text-white hover:text-orange-200 transition-colors p-1 hover:bg-white hover:bg-opacity-20 rounded"
        >
          <svg v-if="!isMinimized" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 12H4"></path>
          </svg>
          <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 8V4m0 0h4M4 4l5 5m11-1V4m0 0h-4m4 0l-5 5M4 16v4m0 0h4m-4 0l5-5m11 5l-5-5m5 5v-4m0 4h-4"></path>
          </svg>
        </button>
        <button
          @click="closeChat"
          class="text-white hover:text-orange-200 transition-colors p-1 hover:bg-white hover:bg-opacity-20 rounded"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
          </svg>
        </button>
      </div>
    </div>

    <!-- Content -->
    <div v-if="!isMinimized" class="flex-1 flex flex-col">
      <!-- Pre-chat form -->
      <div v-if="!isCustomer && !customerChatData" class="p-4 space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Tên của bạn *</label>
          <input
            v-model="customerForm.customerName"
            @input="clearFieldError('customerName')"
            @blur="validateField('customerName')"
            type="text"
            placeholder="Nhập tên của bạn"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent"
            :class="{ 'border-red-500': errors.customerName }"
          />
          <p v-if="errors.customerName" class="text-red-500 text-xs mt-1">{{ errors.customerName }}</p>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Số điện thoại *</label>
          <input
            v-model="customerForm.phone"
            @input="clearFieldError('phone')"
            @blur="validateField('phone')"
            type="tel"
            placeholder="Nhập số điện thoại"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent"
            :class="{ 'border-red-500': errors.phone }"
          />
          <p v-if="errors.phone" class="text-red-500 text-xs mt-1">{{ errors.phone }}</p>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Email</label>
          <input
            v-model="customerForm.email"
            @input="clearFieldError('email')"
            @blur="validateField('email')"
            type="email"
            placeholder="Nhập email (không bắt buộc)"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent"
            :class="{ 'border-red-500': errors.email }"
          />
          <p v-if="errors.email" class="text-red-500 text-xs mt-1">{{ errors.email }}</p>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Tin nhắn</label>
          <textarea
            v-model="customerForm.message"
            placeholder="Mô tả vấn đề của bạn (không bắt buộc)"
            rows="3"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent resize-none"
          ></textarea>
        </div>

        <button
          @click="startChat"
          :disabled="!canStartChat || isJoining"
          class="w-full bg-gradient-to-r from-orange-500 to-orange-600 hover:from-orange-600 hover:to-orange-700 disabled:from-gray-400 disabled:to-gray-500 text-white py-3 px-4 rounded-lg transition-all duration-300 flex items-center justify-center space-x-2 font-semibold shadow-lg hover:shadow-xl disabled:shadow-none transform hover:scale-105 disabled:scale-100"
        >
          <svg v-if="isJoining" class="w-5 h-5 animate-spin" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
          </svg>
          <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"></path>
          </svg>
          <span>{{ isJoining ? 'ĐANG KẾT NỐI...' : 'BẮT ĐẦU TRÒ CHUYỆN' }}</span>
        </button>

        <!-- Auto reset message when there are errors -->
        <div v-if="Object.keys(errors).length > 0" class="text-center">
          <p class="text-sm text-gray-600">Vui lòng kiểm tra lại thông tin và thử lại</p>
        </div>
      </div>

      <!-- Chat messages -->
      <div v-else class="flex-1 overflow-y-auto p-4 space-y-4" ref="messagesContainer">
        <div
          v-for="message in messages"
          :key="message.id"
          :class="[
            'flex',
            message.sender === 'customer' ? 'justify-end' : 
            message.sender === 'staff' ? 'justify-start' : 'justify-center'
          ]"
        >
          <div
            :class="[
              'max-w-xs px-4 py-3 rounded-2xl shadow-lg',
              message.sender === 'customer'
                ? 'bg-gradient-to-br from-orange-500 to-orange-600 text-white'
                : message.sender === 'staff'
                ? 'bg-gradient-to-br from-gray-100 to-gray-200 text-gray-800'
                : 'bg-gradient-to-br from-green-100 to-green-200 text-green-800 text-center font-medium'
            ]"
          >
            <p class="text-sm leading-relaxed">{{ message.content }}</p>
            <p class="text-xs opacity-70 mt-2">
              {{ formatTime(message.timestamp) }}
              <span v-if="message.sender === 'staff' && message.staffName" class="ml-2">
                - {{ message.staffName }}
              </span>
              <span v-else-if="message.sender === 'customer'" class="ml-2">
                - Bạn
              </span>
            </p>
          </div>
        </div>
      </div>

      <!-- Input và disconnect button -->
      <div v-if="isCustomer || customerChatData" class="p-4 border-t border-orange-100 bg-gray-50">
        <div class="flex space-x-2">
          <input
            v-model="newMessage"
            @keyup.enter="sendMessage"
            type="text"
            placeholder="Nhập tin nhắn..."
            class="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-transparent"
          />
          <button
            @click="sendMessage"
            :disabled="!newMessage.trim()"
            class="bg-gradient-to-r from-orange-500 to-orange-600 hover:from-orange-600 hover:to-orange-700 disabled:from-gray-400 disabled:to-gray-500 text-white px-4 py-2 rounded-lg transition-all duration-300 shadow-md hover:shadow-lg disabled:shadow-none transform hover:scale-105 disabled:scale-100"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"></path>
            </svg>
          </button>
        </div>
        
        <div class="mt-3 flex justify-center">
          <button
            @click="handleDisconnect"
            class="bg-gradient-to-r from-red-500 to-red-600 hover:from-red-600 hover:to-red-700 text-white px-4 py-2 rounded-lg text-sm font-medium transition-all duration-300 shadow-md hover:shadow-lg transform hover:scale-105 flex items-center space-x-2"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path>
            </svg>
            <span>Ngắt kết nối</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Minimized state -->
    <div v-else class="p-4 text-center">
      <p class="text-sm text-gray-600">Chat đã được thu nhỏ</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useChat } from '@/composables/useChat'

const {
  isConnected,
  isCustomer,
  assignedStaff,
  messages,
  newMessage,
  customerForm,
  joinAsCustomer,
  sendCustomerMessage,
  resetCustomerState,
  customerChatData,
  customerDisconnect,
  scrollToBottom: scrollToBottomFromChat
} = useChat()

// Widget state
const isVisible = ref(true)
const isMinimized = ref(false)
const position = ref({ x: 0, y: 0 })
const isDragging = ref(false)
const dragStart = ref({ x: 0, y: 0 })

// Form state
const errors = ref({})
const isJoining = ref(false)

// Refs
const chatWidget = ref(null)
const dragHandle = ref(null)
const messagesContainer = ref(null)

// Computed
const canStartChat = computed(() => {
  return customerForm.value.customerName.trim() && 
         customerForm.value.phone.trim() && 
         !isJoining.value
})

// Methods
const clearFieldError = (field) => {
  if (errors.value[field]) {
    delete errors.value[field]
  }
}

const validateField = (field) => {
  const value = customerForm.value[field]
  
  switch (field) {
    case 'customerName':
      if (!value.trim()) {
        errors.value.customerName = 'Tên là bắt buộc'
      } else if (value.trim().length < 2) {
        errors.value.customerName = 'Tên phải có ít nhất 2 ký tự'
      } else {
        delete errors.value.customerName
      }
      break
    case 'phone':
      if (!value.trim()) {
        errors.value.phone = 'Số điện thoại là bắt buộc'
      } else if (!/^[0-9]{10,11}$/.test(value.trim())) {
        errors.value.phone = 'Số điện thoại không hợp lệ'
      } else {
        delete errors.value.phone
      }
      break
    case 'email':
      if (value.trim() && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value.trim())) {
        errors.value.email = 'Email không hợp lệ'
      } else {
        delete errors.value.email
      }
      break
  }
}

const validateForm = () => {
  validateField('customerName')
  validateField('phone')
  validateField('email')
  return Object.keys(errors.value).length === 0
}

const startChat = () => {
  console.log('startChat called - canStartChat:', canStartChat.value, 'isCustomer:', isCustomer.value, 'isJoining:', isJoining.value)
  
  if (isJoining.value) {
    console.log('Already joining, skipping')
    return
  }
  
  errors.value = {}
  
  if (!validateForm()) {
    console.log('Form validation failed - auto resetting form')
    resetForm()
    return
  }
  
  if (canStartChat.value && !isCustomer.value) {
    console.log('Starting chat for customer:', customerForm.value.customerName)
    
    isJoining.value = true
    
    joinAsCustomer()
    
    if (customerForm.value.message.trim()) {
      messages.value.push({
        id: Date.now(),
        sender: 'customer',
        content: customerForm.value.message,
        timestamp: new Date()
      })
    }
  } else {
    console.log('Cannot start chat - canStartChat:', canStartChat.value, 'isCustomer:', isCustomer.value)
  }
}

const sendMessage = () => {
  if (newMessage.value.trim() && (isCustomer.value || customerChatData.value)) {
    sendCustomerMessage(newMessage.value)
    newMessage.value = ''
    nextTick(() => {
      scrollToBottom()
    })
  }
}

const handleDisconnect = () => {
  if (confirm('Bạn có chắc chắn muốn ngắt kết nối? Tất cả tin nhắn sẽ bị xóa.')) {
    customerDisconnect()
    resetForm()
  }
}

const resetForm = () => {
  customerForm.value = {
    customerName: '',
    phone: '',
    email: '',
    message: ''
  }
  errors.value = {}
  isJoining.value = false
}

const closeChat = () => {
  isVisible.value = false
}

const toggleMinimize = () => {
  isMinimized.value = !isMinimized.value
}

const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleTimeString('vi-VN', { 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// Drag functionality
const startDrag = (e) => {
  isDragging.value = true
  dragStart.value = {
    x: e.clientX - position.value.x,
    y: e.clientY - position.value.y
  }
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
  e.preventDefault()
}

const onDrag = (e) => {
  if (!isDragging.value) return
  
  position.value = {
    x: e.clientX - dragStart.value.x,
    y: e.clientY - dragStart.value.y
  }
}

const stopDrag = () => {
  isDragging.value = false
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
}

// Lifecycle
onMounted(() => {
  if (dragHandle.value) {
    dragHandle.value.addEventListener('mousedown', startDrag)
  }
})

onUnmounted(() => {
  if (dragHandle.value) {
    dragHandle.value.removeEventListener('mousedown', startDrag)
  }
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
})
</script>
