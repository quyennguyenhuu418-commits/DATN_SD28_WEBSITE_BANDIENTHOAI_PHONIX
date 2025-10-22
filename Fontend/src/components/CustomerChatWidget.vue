<template>
  <div class="chat-widget" v-if="showWidget">
    <!-- Chat Button (when minimized or not started) -->
    <div v-if="!isChatOpen || isMinimized" class="chat-button" @click="openChat">
      <font-awesome-icon :icon="['fas', 'comment-dots']" />
      <span>Chat với nhân viên</span>
      <div class="notification-dot" v-if="hasUnreadMessages"></div>
    </div>

    <!-- Chat Window -->
    <div v-else class="chat-window">
      <!-- Header -->
      <div class="chat-header">
        <div class="header-info">
          <div class="avatar">
            <font-awesome-icon :icon="['fas', 'headset']" />
          </div>
          <div class="header-text">
            <h3>Chat với nhân viên tư vấn</h3>
            <p v-if="!isConnected">Đang kết nối...</p>
            <p v-else-if="assignedStaff">Đang chat với {{ assignedStaff }}</p>
            <p v-else>Đang tìm nhân viên hỗ trợ...</p>
          </div>
        </div>
        <div class="header-actions">
          <button class="action-btn" @click="minimizeChat" title="Thu nhỏ">
            <font-awesome-icon :icon="['fas', 'window-minimize']" />
          </button>
          <button class="action-btn" @click="closeChat" title="Tạm ẩn chat">
            <font-awesome-icon :icon="['fas', 'times']" />
          </button>
        </div>
      </div>

      <!-- Form Section (when not connected) -->
      <div v-if="!isConnected" class="chat-form">
        <!-- User Info Display (when logged in) -->
        <div v-if="isLoggedIn" class="user-info-display">
          <div class="user-info-header">
            <div class="user-avatar">
              <i class="fas fa-user-circle"></i>
            </div>
            <div class="user-details">
              <h4>{{ userInfo.hoTen || userInfo.username || 'Người dùng' }}</h4>
              <p>{{ userInfo.email || 'Chưa có email' }}</p>
              <p class="user-role">{{ getRoleDisplayName(userInfo.role) }}</p>
            </div>
          </div>
          <button @click="startChat" class="start-chat-btn" :disabled="!canStartChat">
            <i class="fas fa-comments"></i>
            Bắt đầu chat
          </button>
        </div>

        <!-- Guest Form (when not logged in) -->
        <div v-else class="guest-form">
          <h4>Thông tin liên hệ</h4>
          <form @submit.prevent="startChat">
            <div class="form-group">
              <label>Họ và tên *</label>
              <input 
                v-model="formData.customerName" 
                type="text" 
                required 
                placeholder="Nhập họ và tên"
                class="form-input"
              />
            </div>
            <div class="form-group">
              <label>Email</label>
              <input 
                v-model="formData.email" 
                type="email" 
                placeholder="Nhập email"
                class="form-input"
              />
            </div>
            <div class="form-group">
              <label>Số điện thoại *</label>
              <input 
                v-model="formData.phoneNumber" 
                type="tel" 
                required 
                placeholder="Nhập số điện thoại"
                class="form-input"
              />
            </div>
            <div class="form-group">
              <label>Giới tính</label>
              <select v-model="formData.gender" class="form-input">
                <option value="male">Nam</option>
                <option value="female">Nữ</option>
                <option value="other">Khác</option>
              </select>
            </div>
            <div class="form-group">
              <label>Tin nhắn</label>
              <textarea 
                v-model="formData.message" 
                placeholder="Mô tả vấn đề cần hỗ trợ..."
                class="form-textarea"
                rows="3"
              ></textarea>
            </div>
            <button type="submit" class="start-chat-btn" :disabled="!canStartChat">
              <i class="fas fa-comments"></i>
              Bắt đầu chat
            </button>
          </form>
        </div>
      </div>

      <!-- Chat Section (when connected) -->
      <div v-else class="chat-section">
        <!-- Messages -->
        <div class="messages-container">
          <div v-for="message in messages" :key="message.id" class="message" :class="message.sender">
            <div class="message-content">
              <p>{{ message.content }}</p>
              <span class="message-time">{{ formatTime(message.timestamp) }}</span>
            </div>
          </div>
        </div>

        <!-- Input -->
        <div class="input-container">
          <input 
            v-model="newMessage" 
            @keyup.enter="sendMessage"
            placeholder="Nhập tin nhắn..."
            class="message-input"
            :disabled="!assignedStaff"
          />
          <button @click="sendMessage" class="send-btn" :disabled="!assignedStaff">
            <font-awesome-icon :icon="['fas', 'paper-plane']" />
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/authStore'

export default {
  name: 'CustomerChatWidget',
  setup() {
    const authStore = useAuthStore()
    
    // Reactive data
    const showWidget = ref(true)
    const isChatOpen = ref(false)
    const isMinimized = ref(false)
    const isConnected = ref(false)
    const assignedStaff = ref('')
    const hasUnreadMessages = ref(false)
    const messages = ref([])
    const newMessage = ref('')
    
    const formData = ref({
      customerName: '',
      email: '',
      phoneNumber: '',
      gender: 'male',
      message: ''
    })

    // Computed
    const isLoggedIn = computed(() => !!authStore.user)
    const userInfo = computed(() => authStore.user || {})
    
    const canStartChat = computed(() => {
      if (isLoggedIn.value) return true
      return formData.value.customerName.trim() && formData.value.phoneNumber.trim()
    })

    // Methods
    const openChat = () => {
      isChatOpen.value = true
      isMinimized.value = false
    }

    const minimizeChat = () => {
      isMinimized.value = true
    }

    const closeChat = () => {
      isChatOpen.value = false
      isMinimized.value = false
      if (isConnected.value) {
        disconnectChat()
      }
    }

    const startChat = () => {
      if (!canStartChat.value) return
      
      console.log('🚀 Bắt đầu chat với thông tin:', formData.value)
      
      // Simulate connection
      isConnected.value = true
      addMessage('system', 'Đã kết nối thành công! Vui lòng chờ nhân viên hỗ trợ...')
      
      // Simulate staff assignment after 2 seconds
      setTimeout(() => {
        assignedStaff.value = 'Nguyễn Thị Thu'
        addMessage('system', `Đã kết nối với nhân viên ${assignedStaff.value}`)
      }, 2000)
    }

    const addMessage = (sender, content) => {
      messages.value.push({
        id: Date.now(),
        sender,
        content,
        timestamp: new Date()
      })
    }

    const sendMessage = () => {
      if (newMessage.value.trim() && assignedStaff.value) {
        addMessage('customer', newMessage.value)
        newMessage.value = ''
      }
    }

    const disconnectChat = () => {
      isConnected.value = false
      assignedStaff.value = ''
      messages.value = []
    }

    const formatTime = (timestamp) => {
      const date = new Date(timestamp)
      return date.toLocaleTimeString('vi-VN', { 
        hour: '2-digit', 
        minute: '2-digit' 
      })
    }

    const getRoleDisplayName = (role) => {
      const roleMap = {
        'ADMIN': 'Quản trị viên',
        'MANAGER': 'Quản lý',
        'STAFF': 'Nhân viên',
        'CUSTOMER': 'Khách hàng'
      }
      return roleMap[role] || 'Người dùng'
    }

    // Lifecycle
    onMounted(() => {
      // Auto-fill form if logged in
      if (isLoggedIn.value) {
        formData.value.customerName = userInfo.value.hoTen || ''
        formData.value.email = userInfo.value.email || ''
        formData.value.phoneNumber = userInfo.value.soDienThoai || ''
        formData.value.gender = userInfo.value.gioiTinh || 'male'
      }
    })

    return {
      showWidget,
      isChatOpen,
      isMinimized,
      isConnected,
      assignedStaff,
      hasUnreadMessages,
      messages,
      newMessage,
      formData,
      isLoggedIn,
      userInfo,
      canStartChat,
      openChat,
      minimizeChat,
      closeChat,
      startChat,
      sendMessage,
      formatTime,
      getRoleDisplayName
    }
  }
}
</script>

<style scoped>
.chat-widget {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 1000;
}

.chat-button {
  background: #3b82f6;
  color: white;
  padding: 1rem 1.5rem;
  border-radius: 50px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
  transition: all 0.3s ease;
  font-weight: 500;
}

.chat-button:hover {
  background: #2563eb;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4);
}

.notification-dot {
  position: absolute;
  top: -5px;
  right: -5px;
  width: 12px;
  height: 12px;
  background: #ef4444;
  border-radius: 50%;
  border: 2px solid white;
}

.chat-window {
  width: 400px;
  height: 600px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  background: #3b82f6;
  color: white;
  padding: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.avatar {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
}

.header-text h3 {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
}

.header-text p {
  margin: 0;
  font-size: 0.875rem;
  opacity: 0.9;
}

.header-actions {
  display: flex;
  gap: 0.5rem;
}

.action-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  padding: 0.5rem;
  border-radius: 6px;
  cursor: pointer;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s ease;
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.chat-form {
  flex: 1;
  padding: 1.5rem;
  overflow-y: auto;
}

.user-info-display {
  text-align: center;
}

.user-info-header {
  margin-bottom: 1.5rem;
}

.user-avatar {
  font-size: 3rem;
  color: #3b82f6;
  margin-bottom: 1rem;
}

.user-details h4 {
  margin: 0 0 0.5rem 0;
  color: #1f2937;
  font-size: 1.25rem;
}

.user-details p {
  margin: 0 0 0.25rem 0;
  color: #6b7280;
  font-size: 0.875rem;
}

.user-role {
  background: #f3f4f6;
  color: #374151;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 500;
}

.guest-form h4 {
  margin: 0 0 1rem 0;
  color: #1f2937;
  font-size: 1.125rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #374151;
  font-size: 0.875rem;
  font-weight: 500;
}

.form-input,
.form-textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 0.875rem;
  transition: border-color 0.2s ease;
  box-sizing: border-box;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: #3b82f6;
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.start-chat-btn {
  width: 100%;
  background: #3b82f6;
  color: white;
  border: none;
  padding: 0.875rem;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.start-chat-btn:hover:not(:disabled) {
  background: #2563eb;
}

.start-chat-btn:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.chat-section {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.messages-container {
  flex: 1;
  padding: 1rem;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.message {
  display: flex;
  margin-bottom: 0.75rem;
}

.message.customer {
  justify-content: flex-end;
}

.message.staff {
  justify-content: flex-start;
}

.message.system {
  justify-content: center;
}

.message-content {
  max-width: 80%;
  padding: 0.75rem 1rem;
  border-radius: 12px;
  position: relative;
}

.message.customer .message-content {
  background: #3b82f6;
  color: white;
  border-radius: 12px 12px 4px 12px;
}

.message.staff .message-content {
  background: #f3f4f6;
  color: #1f2937;
  border-radius: 12px 12px 12px 4px;
}

.message.system .message-content {
  background: #fef3c7;
  color: #92400e;
  border-radius: 12px;
  text-align: center;
  font-size: 0.875rem;
}

.message-content p {
  margin: 0;
  line-height: 1.4;
}

.message-time {
  font-size: 0.75rem;
  opacity: 0.7;
  display: block;
  margin-top: 0.25rem;
}

.input-container {
  display: flex;
  padding: 1rem;
  border-top: 1px solid #e5e7eb;
  gap: 0.75rem;
}

.message-input {
  flex: 1;
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  outline: none;
  font-size: 0.875rem;
}

.message-input:focus {
  border-color: #3b82f6;
}

.message-input:disabled {
  background: #f9fafb;
  color: #9ca3af;
}

.send-btn {
  background: #3b82f6;
  color: white;
  border: none;
  padding: 0.75rem;
  border-radius: 8px;
  cursor: pointer;
  width: 45px;
  height: 45px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s ease;
}

.send-btn:hover:not(:disabled) {
  background: #2563eb;
}

.send-btn:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

@media (max-width: 480px) {
  .chat-window {
    width: calc(100vw - 40px);
    height: calc(100vh - 40px);
    bottom: 20px;
    right: 20px;
  }
}
</style>