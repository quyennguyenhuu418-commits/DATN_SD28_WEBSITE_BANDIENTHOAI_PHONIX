<template>
  <div class="fixed bottom-4 right-4 z-50">
    <!-- Chat Button -->
    <div v-if="!isOpen" class="relative group">
      <button
        @click="openChat"
        class="bg-gradient-to-br from-orange-500 to-orange-600 hover:from-orange-600 hover:to-orange-700 text-white rounded-full p-4 shadow-xl transition-all duration-300 hover:scale-110 hover:shadow-2xl"
      >
        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"></path>
        </svg>
      </button>
      
      <!-- Online indicator -->
      <div class="absolute -bottom-1 -right-1 w-4 h-4 bg-green-500 rounded-full border-2 border-white flex items-center justify-center">
        <div class="w-2 h-2 bg-white rounded-full animate-pulse"></div>
      </div>
      
      <!-- Notification badge -->
      <div v-if="hasUnreadMessages" class="absolute -top-2 -right-2 bg-red-500 text-white text-xs rounded-full h-6 w-6 flex items-center justify-center animate-pulse font-bold">
        {{ unreadCount }}
      </div>
    </div>

    <!-- Chat Window -->
    <div v-if="isOpen" class="bg-white rounded-2xl shadow-2xl w-96 h-[500px] flex flex-col overflow-hidden border border-orange-100">
      <!-- Header -->
      <div class="bg-gradient-to-r from-orange-500 to-orange-600 text-white p-4 flex justify-between items-center">
        <div class="flex items-center space-x-3">
          <div class="w-10 h-10 bg-white bg-opacity-20 rounded-full flex items-center justify-center">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
            </svg>
          </div>
        <div>
          <h3 class="font-semibold text-lg">Chat hỗ trợ</h3>
          <p class="text-sm text-orange-100" v-if="assignedStaff">Đã kết nối với {{ assignedStaff }}</p>
          <p class="text-sm text-orange-100" v-else>Hỗ trợ</p>
        </div>
        </div>
        <button @click="closeChat" class="text-white hover:text-orange-200 transition-colors p-2 hover:bg-white hover:bg-opacity-20 rounded-full">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
          </svg>
        </button>
      </div>

             <!-- Customer Info Form -->
             <div v-if="!isCustomer && !customerChatData" class="flex-1 overflow-y-auto p-4">
        <div class="space-y-4">
          <div class="text-center mb-6">
            <div class="w-20 h-20 bg-gradient-to-br from-orange-100 to-orange-200 rounded-full flex items-center justify-center mx-auto mb-4 shadow-lg">
              <svg class="w-10 h-10 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
              </svg>
            </div>
            <h3 class="text-xl font-bold text-gray-800 mb-2">Thông tin cơ bản</h3>
            <p class="text-sm text-gray-600">Vui lòng điền thông tin để bắt đầu chat</p>
          </div>
          
          <div class="space-y-3">
                   <div>
                     <label class="block text-sm font-medium text-gray-700 mb-1">Tên của bạn *</label>
                     <input
                       v-model="customerForm.customerName"
                       @input="clearFieldError('customerName')"
                       @blur="validateName(customerForm.customerName) && (errors.customerName = validateName(customerForm.customerName))"
                       type="text"
                       placeholder="Nhập tên của bạn"
                       :class="[
                         'w-full px-4 py-3 border rounded-xl focus:outline-none focus:ring-2 transition-all duration-200',
                         errors.customerName ? 'border-red-500 focus:ring-red-500 bg-red-50' : 'border-gray-300 focus:ring-orange-500 hover:border-orange-300'
                       ]"
                       required
                     />
                     <p v-if="errors.customerName" class="text-red-500 text-xs mt-1">{{ errors.customerName }}</p>
                   </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Số điện thoại *</label>
              <input
                v-model="customerForm.phone"
                @input="clearFieldError('phone')"
                @blur="validatePhone(customerForm.phone) && (errors.phone = validatePhone(customerForm.phone))"
                type="tel"
                placeholder="Nhập số điện thoại"
                :class="[
                  'w-full px-4 py-3 border rounded-xl focus:outline-none focus:ring-2 transition-all duration-200',
                  errors.phone ? 'border-red-500 focus:ring-red-500 bg-red-50' : 'border-gray-300 focus:ring-orange-500 hover:border-orange-300'
                ]"
                required
              />
              <p v-if="errors.phone" class="text-red-500 text-xs mt-1">{{ errors.phone }}</p>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
              <input
                v-model="customerForm.email"
                @input="clearFieldError('email')"
                @blur="validateEmail(customerForm.email) && (errors.email = validateEmail(customerForm.email))"
                type="email"
                placeholder="Nhập email của bạn"
                :class="[
                  'w-full px-4 py-3 border rounded-xl focus:outline-none focus:ring-2 transition-all duration-200',
                  errors.email ? 'border-red-500 focus:ring-red-500 bg-red-50' : 'border-gray-300 focus:ring-orange-500 hover:border-orange-300'
                ]"
              />
              <p v-if="errors.email" class="text-red-500 text-xs mt-1">{{ errors.email }}</p>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Tin nhắn</label>
              <textarea
                v-model="customerForm.message"
                placeholder="Nhập tin nhắn của bạn..."
                rows="3"
                class="w-full px-4 py-3 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-orange-500 hover:border-orange-300 transition-all duration-200 resize-none"
              ></textarea>
            </div>
          </div>
        </div>
      </div>

      <!-- Messages -->
      <div v-else-if="isCustomer || customerChatData" class="flex-1 overflow-y-auto p-4 space-y-3">
        <div v-if="!isConnected" class="text-center text-gray-500">
          <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-red-600 mx-auto mb-2"></div>
          <p>Đang kết nối...</p>
        </div>
        
        <div v-else-if="messages.length === 0" class="text-center text-gray-500 py-8">
          <div class="w-16 h-16 bg-gradient-to-br from-orange-100 to-orange-200 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg class="w-8 h-8 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"></path>
            </svg>
          </div>
          <p class="text-lg font-semibold text-gray-700 mb-2">Chào mừng bạn đến với PhoniX!</p>
          <p v-if="assignedStaff" class="text-sm text-green-600 font-medium">
            ✅ Đã kết nối với nhân viên {{ assignedStaff }}
          </p>
          <p v-else class="text-sm text-orange-600 font-medium">
            ⏳ Đang chờ nhân viên hỗ trợ...
          </p>
        </div>
        
        <div v-else class="space-y-4" ref="messagesContainer">
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
      </div>

             <!-- Start Chat Button -->
             <div v-if="!isCustomer && !customerChatData" class="p-4 border-t border-orange-100">
               <button
                 @click="startChat"
                 :disabled="!canStartChat || isJoining"
                 class="w-full bg-gradient-to-r from-orange-500 to-orange-600 hover:from-orange-600 hover:to-orange-700 disabled:from-gray-400 disabled:to-gray-500 text-white py-4 px-6 rounded-xl transition-all duration-300 flex items-center justify-center space-x-3 font-semibold shadow-lg hover:shadow-xl disabled:shadow-none transform hover:scale-105 disabled:scale-100"
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
               <div v-if="Object.keys(errors).length > 0" class="mt-3 text-center">
                 <p class="text-sm text-gray-600">Vui lòng kiểm tra lại thông tin và thử lại</p>
               </div>
             </div>

      <!-- Input -->
      <div v-else-if="(isCustomer || customerChatData) && isConnected" class="p-4 border-t border-orange-100 bg-gray-50">
        <div class="flex space-x-3">
          <input
            v-model="newMessage"
            @keyup.enter="sendMessage"
            type="text"
            placeholder="Nhập tin nhắn..."
            class="flex-1 px-4 py-3 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-orange-500 hover:border-orange-300 transition-all duration-200"
          />
          <button
            @click="sendMessage"
            :disabled="!newMessage.trim()"
            class="bg-gradient-to-r from-orange-500 to-orange-600 hover:from-orange-600 hover:to-orange-700 disabled:from-gray-400 disabled:to-gray-500 text-white px-6 py-3 rounded-xl transition-all duration-300 shadow-lg hover:shadow-xl disabled:shadow-none transform hover:scale-105 disabled:scale-100"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"></path>
            </svg>
          </button>
        </div>
        
        <!-- Disconnect Button -->
        <div class="mt-3 flex justify-center">
          <button
            @click="handleDisconnect"
            class="bg-gradient-to-r from-red-500 to-red-600 hover:from-red-600 hover:to-red-700 text-white px-4 py-2 rounded-lg text-sm transition-all duration-300 shadow-md hover:shadow-lg transform hover:scale-105 flex items-center space-x-2"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path>
            </svg>
            <span>Ngắt kết nối</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
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

const isOpen = ref(false)
const hasUnreadMessages = ref(false)
const unreadCount = ref(0)
const customerName = ref('')
const errors = ref({})
const messagesContainer = ref(null)
const isJoining = ref(false) // Added loading state

// Computed
const canStartChat = computed(() => {
  return customerForm.value.customerName.trim() && 
         customerForm.value.phone.trim() && 
         Object.keys(errors.value).length === 0
})

// Validation functions
const validateName = (name) => {
  if (!name.trim()) {
    return 'Tên không được để trống'
  }
  if (name.trim().length < 2) {
    return 'Tên phải có ít nhất 2 ký tự'
  }
  return null
}

const validatePhone = (phone) => {
  if (!phone.trim()) {
    return 'Số điện thoại không được để trống'
  }
  const phoneRegex = /^[0-9]{10,11}$/
  if (!phoneRegex.test(phone.trim())) {
    return 'Số điện thoại phải có 10-11 chữ số'
  }
  return null
}

const validateEmail = (email) => {
  if (!email.trim()) return null // Email is optional
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(email.trim())) {
    return 'Email không đúng định dạng'
  }
  return null
}

// Clear specific field error when user starts typing
const clearFieldError = (field) => {
  if (errors.value[field]) {
    delete errors.value[field]
  }
}

const validateForm = () => {
  errors.value = {}
  
  const nameError = validateName(customerForm.value.customerName)
  if (nameError) errors.value.customerName = nameError
  
  const phoneError = validatePhone(customerForm.value.phone)
  if (phoneError) errors.value.phone = phoneError
  
  const emailError = validateEmail(customerForm.value.email)
  if (emailError) errors.value.email = emailError
  
  return Object.keys(errors.value).length === 0
}

const openChat = () => {
  isOpen.value = true
  hasUnreadMessages.value = false
  unreadCount.value = 0
}

const closeChat = () => {
  isOpen.value = false
  // Don't reset customer state when closing - keep data for persistence
  isJoining.value = false
}

const startChat = () => {
  console.log('startChat called - canStartChat:', canStartChat.value, 'isCustomer:', isCustomer.value, 'isJoining:', isJoining.value)
  
  // Prevent multiple clicks
  if (isJoining.value) {
    console.log('Already joining, skipping')
    return
  }
  
  // Clear previous errors
  errors.value = {}
  
  // Validate form first
  if (!validateForm()) {
    console.log('Form validation failed - auto resetting form')
    // Auto reset form immediately after validation fails
    resetForm()
    return
  }
  
  if (canStartChat.value && !isCustomer.value) {
    console.log('Starting chat for customer:', customerForm.value.customerName)
    
    // Set loading state
    isJoining.value = true
    
    // Set customer name for display
    customerName.value = customerForm.value.customerName
    
    // Join as customer
    joinAsCustomer()
    
    // Add initial message if provided
    if (customerForm.value.message.trim()) {
      messages.value.push({
        id: Date.now(),
        sender: 'customer',
        content: customerForm.value.message,
        timestamp: new Date()
      })
    }
    
    // Reset loading state when customer joined successfully
    // This will be handled by the customer joined event
  } else {
    console.log('Cannot start chat - canStartChat:', canStartChat.value, 'isCustomer:', isCustomer.value)
  }
}

const sendMessage = () => {
  if (newMessage.value.trim()) {
    sendCustomerMessage(newMessage.value)
    newMessage.value = ''
    // Auto scroll to bottom after sending
    nextTick(() => {
      scrollToBottom()
    })
  }
}

const handleDisconnect = () => {
  if (confirm('Bạn có chắc chắn muốn ngắt kết nối? Tất cả tin nhắn sẽ bị xóa.')) {
    customerDisconnect()
    isOpen.value = false
  }
}

// Reset form and errors
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

// Auto scroll function
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// Override scrollToBottom in useChat
onMounted(() => {
  scrollToBottomFromChat.value = scrollToBottom
})

// Watch for new messages to auto scroll
watch(messages, () => {
  nextTick(() => {
    scrollToBottom()
  })
}, { deep: true })

// Watch for customer joined to reset loading state
watch(isCustomer, (newValue) => {
  if (newValue) {
    isJoining.value = false
  }
})

const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleTimeString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

// Watch for new messages to show notification
const lastMessageCount = ref(0)
onMounted(() => {
  setInterval(() => {
    if (messages.value.length > lastMessageCount.value) {
      lastMessageCount.value = messages.value.length
      if (!isOpen.value) {
        hasUnreadMessages.value = true
        unreadCount.value++
      }
    }
  }, 1000)
})
</script>

<style scoped>
/* Custom scrollbar */
.overflow-y-auto::-webkit-scrollbar {
  width: 4px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 2px;
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
