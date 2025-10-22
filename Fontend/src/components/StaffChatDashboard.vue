<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <!-- Header -->
    <PosHeader 
      :show-chat-toggle="true"
      @toggle-chat="handleToggleChat"
    />

    <!-- Main Content -->
    <div class="flex-1 flex overflow-hidden">
      <!-- Sidebar - Waiting Customers & Active Chats -->
      <div class="w-80 bg-white shadow-lg border-r border-gray-200 pt-24">
        <!-- Waiting Customers -->
        <div class="h-80 border-b border-gray-200">
          <div class="p-4 bg-gradient-to-r from-orange-50 to-orange-100">
            <h2 class="text-lg font-semibold text-gray-800 flex items-center space-x-2">
              <svg class="w-5 h-5 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
              </svg>
              <span>Khách hàng chờ</span>
              <span class="bg-orange-500 text-white text-xs px-2 py-1 rounded-full">{{ waitingCustomers.length }}</span>
            </h2>
          </div>
          <div class="p-4 h-64 overflow-y-auto">
            <div v-if="waitingCustomers.length === 0" class="text-center text-gray-500 py-8">
              <svg class="w-12 h-12 mx-auto mb-4 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path>
              </svg>
              <p class="text-sm">Không có khách hàng chờ</p>
            </div>
            <div v-else class="space-y-3">
              <div
                v-for="customer in waitingCustomers"
                :key="customer.sessionId"
                class="border border-orange-200 rounded-xl p-3 hover:bg-orange-50 transition-all duration-200 hover:shadow-md"
              >
                <div class="flex justify-between items-start">
                  <div class="flex-1">
                    <h3 class="font-medium text-gray-800">{{ customer.customerName }}</h3>
                    <p class="text-sm text-gray-600">{{ customer.phone }}</p>
                    <p class="text-xs text-gray-500">{{ formatTime(customer.joinedAt) }}</p>
                  </div>
                  <button
                    @click="takeCustomer(customer.sessionId)"
                    class="bg-gradient-to-r from-orange-500 to-orange-600 hover:from-orange-600 hover:to-orange-700 text-white px-3 py-1 rounded-lg text-sm font-medium transition-all duration-200 shadow-md hover:shadow-lg transform hover:scale-105"
                  >
                    Nhận
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Active Chats -->
        <div class="h-full">
          <div class="p-4 bg-gradient-to-r from-green-50 to-green-100">
            <h2 class="text-lg font-semibold text-gray-800 flex items-center space-x-2">
              <svg class="w-5 h-5 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"></path>
              </svg>
              <span>Cuộc trò chuyện</span>
              <span class="bg-green-500 text-white text-xs px-2 py-1 rounded-full">{{ activeChats.length }}</span>
            </h2>
          </div>
          <div class="p-4 space-y-3 overflow-y-auto" style="height: calc(100vh - 400px);">
            <div
              v-for="chat in activeChats"
              :key="chat.sessionId"
              @click="selectChat(chat)"
              :class="[
                'border rounded-xl p-3 cursor-pointer transition-all duration-200 hover:shadow-md',
                selectedChat && selectedChat.sessionId === chat.sessionId
                  ? 'border-orange-500 bg-orange-50 shadow-md'
                  : 'border-gray-200 hover:border-orange-300 hover:bg-orange-50'
              ]"
            >
              <div class="flex justify-between items-start">
                <div class="flex-1">
                  <h3 class="font-medium text-gray-800">{{ chat.customerName }}</h3>
                  <p class="text-sm text-gray-600">{{ chat.phone }}</p>
                  <p class="text-xs text-gray-500">Bắt đầu: {{ formatTime(chat.joinedAt) }}</p>
                </div>
                <div class="flex items-center space-x-1">
                  <div class="w-2 h-2 bg-green-500 rounded-full"></div>
                  <span class="text-xs text-green-600 font-medium">Đang chat</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Main Chat Area -->
      <div class="flex-1 flex flex-col mt-24">
        <!-- Chat Header -->
        <div v-if="selectedChat" class="bg-white border-b border-gray-200 p-4">
          <div class="flex items-center justify-between">
            <div class="flex items-center space-x-4">
              <div class="w-10 h-10 bg-gradient-to-r from-orange-500 to-orange-600 rounded-full flex items-center justify-center">
                <span class="text-white font-semibold text-lg">{{ selectedChat.customerName.charAt(0) }}</span>
              </div>
              <div>
                <h2 class="text-lg font-semibold text-gray-800">Chat với {{ selectedChat.customerName }}</h2>
                <div class="flex items-center space-x-4 text-sm text-gray-600">
                  <div class="flex items-center space-x-1">
                    <svg class="w-4 h-4 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                    </svg>
                    <span class="font-medium text-green-600">Đã kết nối</span>
                  </div>
                  <div class="flex items-center space-x-1">
                    <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"></path>
                    </svg>
                    <span>{{ selectedChat.phone }}</span>
                  </div>
                  <div v-if="selectedChat.email" class="flex items-center space-x-1">
                    <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 4.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path>
                    </svg>
                    <span>{{ selectedChat.email }}</span>
                  </div>
                </div>
              </div>
            </div>
            <button
              @click="manualDisconnectCustomer"
              class="bg-gradient-to-r from-red-500 to-red-600 hover:from-red-600 hover:to-red-700 text-white px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200 shadow-md hover:shadow-lg transform hover:scale-105 flex items-center space-x-2"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path>
              </svg>
              <span>Ngắt kết nối</span>
            </button>
          </div>
        </div>

        <!-- Chat Messages -->
        <div 
          v-if="selectedChat" 
          ref="staffMessagesContainer"
          class="flex-1 overflow-y-auto p-4 bg-gray-50"
        >
          <div v-if="messages.length === 0" class="flex items-center justify-center h-full text-gray-500">
            <div class="text-center">
              <svg class="w-16 h-16 mx-auto mb-4 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"></path>
              </svg>
              <p class="text-lg font-medium">Chưa có tin nhắn nào</p>
              <p class="text-sm">Bắt đầu cuộc trò chuyện với khách hàng</p>
            </div>
          </div>
          <div v-else class="space-y-4">
            <div
              v-for="message in messages"
              :key="message.id"
              :class="[
                'flex',
                message.sender === 'staff' ? 'justify-end' : 'justify-start'
              ]"
            >
              <div
                :class="[
                  'max-w-xs px-4 py-3 rounded-2xl shadow-lg',
                  message.sender === 'staff'
                    ? 'bg-gradient-to-br from-orange-500 to-orange-600 text-white'
                    : message.sender === 'customer'
                    ? 'bg-gradient-to-br from-gray-100 to-gray-200 text-gray-800'
                    : 'bg-gradient-to-br from-green-100 to-green-200 text-green-800 text-center font-medium'
                ]"
              >
                <p class="text-sm leading-relaxed">{{ message.content }}</p>
                <p class="text-xs opacity-70 mt-2">
                  {{ formatTime(message.timestamp) }}
                  <span v-if="message.sender === 'customer' && message.customerName" class="ml-2">
                    - {{ message.customerName }}
                  </span>
                  <span v-else-if="message.sender === 'staff'" class="ml-2">
                    - Bạn
                  </span>
                </p>
              </div>
            </div>
          </div>
        </div>

        <!-- No Chat Selected -->
        <div v-else class="flex-1 flex items-center justify-center bg-gray-50">
          <div class="text-center text-gray-500">
            <svg class="w-20 h-20 mx-auto mb-4 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"></path>
            </svg>
            <h3 class="text-xl font-medium mb-2">Chọn cuộc trò chuyện</h3>
            <p class="text-sm">Chọn một cuộc trò chuyện từ danh sách bên trái để bắt đầu</p>
          </div>
        </div>

        <!-- Message Input -->
        <div v-if="selectedChat" class="bg-white border-t border-gray-200 p-4">
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
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useChat } from '@/composables/useChat'
import PosHeader from './PosHeader.vue'

const {
  isConnected,
  isStaff,
  staffId,
  staffName,
  activeChats,
  waitingCustomers,
  selectedChat,
  messages,
  newMessage,
  takeCustomer,
  selectChat,
  sendStaffMessage
} = useChat()

const staffMessagesContainer = ref(null)
const isChatVisible = ref(true)

const sendMessage = () => {
  if (newMessage.value.trim() && selectedChat.value) {
    sendStaffMessage(newMessage.value)
    newMessage.value = ''
    // Auto scroll to bottom after sending
    nextTick(() => {
      scrollToBottom()
    })
  }
}

// Manual disconnect customer for testing
const manualDisconnectCustomer = () => {
  if (selectedChat.value) {
    console.log('Manual disconnect customer:', selectedChat.value.sessionId)
    
    // Remove from active chats
    activeChats.value = activeChats.value.filter(c => c.sessionId !== selectedChat.value.sessionId)
    
    // Clear selected chat
    selectedChat.value = null
    messages.value = []
    
    console.log('Customer manually disconnected from dashboard')
  }
}

// Handle chat toggle from header
const handleToggleChat = (visible) => {
  isChatVisible.value = visible
  // You can add logic here to show/hide chat area
}

// Auto scroll function
const scrollToBottom = () => {
  if (staffMessagesContainer.value) {
    staffMessagesContainer.value.scrollTop = staffMessagesContainer.value.scrollHeight
  }
}

// Format time helper
const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleTimeString('vi-VN', { 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

// Auto scroll when messages change
onMounted(() => {
  console.log('🏗️ StaffChatDashboard mounted')
  console.log('📊 Initial state:', {
    isConnected: isConnected.value,
    isStaff: isStaff.value,
    waitingCount: waitingCustomers.value.length,
    activeCount: activeChats.value.length,
    selectedChat: selectedChat.value?.customerName || 'none'
  })
  
  nextTick(() => {
    scrollToBottom()
  })
})
</script>