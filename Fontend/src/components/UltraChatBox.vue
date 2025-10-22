<template>
  <div id="ultra-chatbox" class="ultra-chatbox-container">
    <!-- Floating Button -->
    <div 
      v-if="!isOpen" 
      class="ultra-chat-toggle"
      :class="{ 'has-unread': unreadCount > 0 }"
      @click="openChat"
    >
      <div class="toggle-icon">
        <template v-if="!isBotTyping">
          <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor" aria-hidden="true">
            <path d="M12 2a1 1 0 011 1v1h3a1 1 0 010 2h-1v2h3a2 2 0 012 2v7a3 3 0 01-3 3h-1a3 3 0 01-3-3H9a3 3 0 01-3 3H5a3 3 0 01-3-3V10a2 2 0 012-2h3V6H6a1 1 0 010-2h3V3a1 1 0 011-1h2zm-6 8v5a1 1 0 001 1h10a1 1 0 001-1v-5H6zm3 2a1 1 0 110 2 1 1 0 010-2zm6 0a1 1 0 110 2 1 1 0 010-2z"/>
          </svg>
        </template>
        <template v-else>
          <div class="typing-animation">
            <div class="typing-dot"></div>
            <div class="typing-dot"></div>
            <div class="typing-dot"></div>
          </div>
        </template>
      </div>
      
      <!-- Unread Badge -->
      <div v-if="unreadCount > 0" class="unread-badge">
        {{ unreadCount > 99 ? '99+' : unreadCount }}
      </div>
      
      <!-- Connection Status -->
      <div class="connection-status" :class="connectionStatus"></div>
    </div>

    <!-- Chat Window -->
            <div 
              v-if="isOpen" 
              class="ultra-chat-window"
              :class="{
                'minimized': isMinimized,
                'fullscreen': isFullscreen,
                'dark-mode': ui.isDarkMode,
                'compact-mode': ui.compactMode
              }"
              @click.self="isMinimized ? maximizeChat() : null"
            >
      <!-- Header -->
      <div class="ultra-chat-header">
        <div class="header-left">
          <div class="avatar-container">
            <!-- Robot logo -->
            <div class="avatar robot-logo">
              <svg viewBox="0 0 24 24" width="40" height="40" fill="currentColor">
                <path d="M12 2a1 1 0 011 1v1h3a1 1 0 010 2h-1v2h3a2 2 0 012 2v7a3 3 0 01-3 3h-1a3 3 0 01-3-3H9a3 3 0 01-3 3H5a3 3 0 01-3-3V10a2 2 0 012-2h3V6H6a1 1 0 010-2h3V3a1 1 0 011-1h2zm-6 8v5a1 1 0 001 1h10a1 1 0 001-1v-5H6zm3 2a1 1 0 110 2 1 1 0 010-2zm6 0a1 1 0 110 2 1 1 0 010-2z"/>
              </svg>
            </div>
            <div class="status-indicator" :class="realtime.agentStatus"></div>
          </div>
          <div class="header-info">
            <h3 class="chat-title">PhoniX BOT</h3>
            <p class="status-text">
              <span class="status-line">
                <span class="status-dot" :class="realtime.agentStatus"></span>
                {{ getStatusText() }}
              </span>
              <span v-if="realtime.responseTime" class="response-time">{{ realtime.responseTime }}</span>
            </p>
          </div>
        </div>
        
        <div class="header-actions">
          <!-- Settings -->
          <button class="header-btn" @click="showSettings = !showSettings" title="Cài đặt">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" stroke="currentColor" stroke-width="2"/>
            </svg>
          </button>
          
          <!-- Minimize -->
          <button class="header-btn" @click="minimizeChat" title="Thu nhỏ">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M4 10h12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </button>
          
          <!-- Fullscreen -->
          <button class="header-btn" @click="toggleFullscreen" title="Toàn màn hình">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M3 7V3h4M17 7V3h-4M3 13v4h4M17 13v4h-4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
          
          <!-- Close -->
          <button class="header-btn close-btn" @click="closeChat" title="Đóng">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M15 5L5 15M5 5l10 10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
      </div>

      <!-- Settings Panel -->
      <div v-if="showSettings" class="settings-panel">
        <div class="settings-section">
          <h4>Giao diện</h4>
          <div class="setting-item">
            <label>Chế độ tối</label>
            <button 
              class="toggle-switch" 
              :class="{ active: ui.isDarkMode }"
              @click="ui.isDarkMode = !ui.isDarkMode"
            >
              <div class="toggle-slider"></div>
            </button>
          </div>
          <div class="setting-item">
            <label>Chế độ compact</label>
            <button 
              class="toggle-switch" 
              :class="{ active: ui.compactMode }"
              @click="ui.compactMode = !ui.compactMode"
            >
              <div class="toggle-slider"></div>
            </button>
          </div>
        </div>
        
        <div class="settings-section">
          <h4>Thông báo</h4>
          <div class="setting-item">
            <label>Âm thanh</label>
            <button 
              class="toggle-switch" 
              :class="{ active: ui.soundEnabled }"
              @click="ui.soundEnabled = !ui.soundEnabled"
            >
              <div class="toggle-slider"></div>
            </button>
          </div>
          <div class="setting-item">
            <label>Rung</label>
            <button 
              class="toggle-switch" 
              :class="{ active: ui.vibrationEnabled }"
              @click="ui.vibrationEnabled = !ui.vibrationEnabled"
            >
              <div class="toggle-slider"></div>
            </button>
          </div>
        </div>
      </div>

      <!-- Messages Area -->
      <div class="ultra-chat-messages" ref="messagesContainer">
        <div 
          v-for="message in messages" 
          :key="message.id" 
          class="message-container"
          :class="[message.sender, message.type]"
        >
          <!-- User Message -->
          <div v-if="message.sender === 'user'" class="user-message">
            <div class="message-content">
              <div class="message-bubble user-bubble">
                <div v-if="message.type === 'text'" class="text-content">
                  {{ message.content }}
                </div>
                <div v-else-if="message.type === 'quick_reply'" class="quick-reply-content">
                  {{ message.content }}
                </div>
                <div v-else-if="message.type === 'file'" class="file-content">
                  <div class="file-info">
                    <div class="file-icon">📎</div>
                    <div class="file-details">
                      <div class="file-name">{{ message.content.name }}</div>
                      <div class="file-size">{{ formatFileSize(message.content.size) }}</div>
                    </div>
                  </div>
                </div>
              </div>
              <div class="message-time">{{ formatTime(message.timestamp) }}</div>
            </div>
            <div class="user-avatar">
              <img :src="userAvatar" alt="User" />
            </div>
          </div>

          <!-- Bot Message -->
          <div v-else class="bot-message">
                    <div class="bot-avatar">
                      <div class="avatar robot-logo">
                        <svg viewBox="0 0 24 24" width="32" height="32" fill="currentColor">
                          <path d="M12 2a1 1 0 011 1v1h3a1 1 0 010 2h-1v2h3a2 2 0 012 2v7a3 3 0 01-3 3h-1a3 3 0 01-3-3H9a3 3 0 01-3 3H5a3 3 0 01-3-3V10a2 2 0 012-2h3V6H6a1 1 0 010-2h3V3a1 1 0 011-1h2zm-6 8v5a1 1 0 001 1h10a1 1 0 001-1v-5H6zm3 2a1 1 0 110 2 1 1 0 010-2zm6 0a1 1 0 110 2 1 1 0 010-2z"/>
                        </svg>
                      </div>
                      <div class="ai-indicator">AI</div>
                    </div>
            <div class="message-content">
              <div class="message-bubble bot-bubble">
                <!-- Text Message -->
                <div v-if="message.type === 'text'" class="text-content">
                  {{ message.content }}
                </div>
                
        <!-- Quick Replies -->
        <div v-else-if="message.type === 'quick-replies'" class="quick-replies">
          <button
            v-for="reply in message.quickReplies"
            :key="reply.id"
            class="quick-reply-btn"
            @click="sendQuickReply(reply)"
          >
            {{ reply.text }}
          </button>
        </div>
        
        <!-- Form Messages -->
        <div v-else-if="message.type === 'form'" class="form-message">
          <div class="form-header">
            <h4>{{ message.content.title }}</h4>
          </div>
          <form @submit.prevent="submitForm" class="chat-form">
            <div v-for="field in message.content.fields" :key="field.name" class="form-field">
              <label :for="field.name">{{ field.label }}</label>
              <input
                :id="field.name"
                v-model="formData[field.name]"
                :type="field.type"
                :placeholder="field.placeholder"
                :required="field.required"
                class="form-input"
              />
            </div>
            <button type="submit" class="form-submit-btn">
              {{ message.content.submitText }}
            </button>
          </form>
        </div>
        
        <!-- Product Cards -->
        <div v-else-if="message.type === 'product_cards'" class="product-cards">
          <div class="product-cards-header">
            <h4>{{ message.content.title }}</h4>
          </div>
          <div class="product-grid">
            <div
              v-for="product in message.content.products"
              :key="product.id"
              class="product-card"
              @click="viewProduct(product)"
            >
              <div class="product-image">
                <img :src="product.imageUrl" :alt="product.name" />
              </div>
              <div class="product-info">
                <h5 class="product-name">{{ product.name }}</h5>
                <div class="product-price">{{ formatPrice(product.price) }}</div>
                <div class="product-specs">
                  <span v-for="spec in product.specs" :key="spec" class="spec-tag">
                    {{ spec }}
                  </span>
                </div>
                <div class="product-stock">
                  Còn lại: {{ product.stock }} sản phẩm
                </div>
                <div class="product-actions" style="margin-top:8px; display:flex; gap:8px;">
                  <button class="btn-primary" @click.stop="addToCart(product)">Thêm vào giỏ</button>
                </div>
              </div>
            </div>
          </div>
        </div>

                <!-- Legacy product_list (from other controllers) -->
                        <div v-else-if="message.type === 'product_list'" class="product-cards">
                  <h4 class="product-cards-title">Gợi ý sản phẩm</h4>
                  <div class="product-cards-grid">
                    <div v-for="product in message.content" :key="product.id" class="product-card">
                      <div class="product-image">
                        <img :src="product.imageUrl || '/images/placeholder-phone.jpg'" :alt="product.name" />
                        <div v-if="product.discount" class="discount-badge">{{ product.discount }}</div>
                      </div>
                      <div class="product-info">
                        <h5 class="product-name">{{ product.name }}</h5>
                        <div class="product-price">{{ formatPrice(product.price || 0) }}</div>
                        <div class="product-rating">
                          <span class="stars">⭐ {{ product.rating || 4.5 }}</span>
                          <span class="stock">{{ (product.stock && product.stock > 0) ? ('Còn ' + product.stock + ' máy') : 'Liên hệ' }}</span>
                        </div>
                        <div class="product-specs" v-if="product.keySpecs">
                          <span v-for="spec in product.keySpecs.slice(0, 3)" :key="spec" class="spec-tag">{{ spec }}</span>
                        </div>
                        <div class="product-actions">
                          <button class="btn-primary" @click="addToCart(product)">Thêm vào giỏ</button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                
                <!-- Form -->
                <div v-else-if="message.type === 'form'" class="form-container">
                  <h4 class="form-title">{{ message.content.title }}</h4>
                  <form @submit.prevent="submitForm">
                    <div 
                      v-for="field in message.content.fields" 
                      :key="field.name"
                      class="form-field"
                    >
                      <label :for="field.name">{{ field.label }}</label>
                      <input 
                        :id="field.name"
                        v-model="formData[field.name]"
                        :type="field.type"
                        :placeholder="field.placeholder"
                        :required="field.required"
                        class="form-input"
                      />
                    </div>
                    <button type="submit" class="form-submit">
                      {{ message.content.submitText || 'Gửi' }}
                    </button>
                  </form>
                </div>
                
                        <!-- Comparison Table (new layout) -->
                        <div v-else-if="message.type === 'comparison'" class="comparison-table">
                          <div class="comparison-title-row">
                            <h4 class="comparison-title">Bảng So Sánh {{ (message.products||[])[0]?.name }} và {{ (message.products||[])[1]?.name }}</h4>
                          </div>
                          <div class="comparison-grid new-compare">
                            <div class="comparison-header">
                              <div class="comparison-cell">Tiêu chí</div>
                              <div v-for="product in (message.products || [])" :key="product.id" class="comparison-cell product-header">
                                <img :src="product.imageUrl" :alt="product.name" />
                                <div class="product-name">{{ product.name }}</div>
                                <div class="product-price">{{ formatPrice(product.price) }}</div>
                              </div>
                            </div>
                            <div v-for="row in enhancedCompareRows(message.products || [])" :key="row.key" class="comparison-row">
                              <div class="comparison-cell spec-name">{{ row.label }}</div>
                              <div 
                                v-for="(val, idx) in row.values"
                                :key="idx"
                                class="comparison-cell spec-value"
                              >
                                {{ val || '—' }}
                              </div>
                            </div>
                            <div v-if="message.verdict" class="comparison-row">
                              <div class="comparison-cell spec-name">Kết luận</div>
                              <div class="comparison-cell" :colspan="2">{{ message.verdict }}</div>
                            </div>
                            <div v-if="message.quickReviews" class="comparison-row">
                              <div class="comparison-cell spec-name">Đánh giá nhanh</div>
                              <div class="comparison-cell" :colspan="2">
                                <div v-for="(txt, pname) in message.quickReviews" :key="pname" class="quick-review">• {{ txt }}</div>
                              </div>
                            </div>
                          </div>
                        </div>
              </div>
              <div class="message-time">{{ formatTime(message.timestamp) }}</div>
            </div>
          </div>
        </div>

        <!-- Typing Indicator -->
                <div v-if="isBotTyping" class="typing-indicator">
                  <div class="bot-avatar">
                    <div class="avatar robot-logo">
                      <svg viewBox="0 0 24 24" width="32" height="32" fill="currentColor">
                        <path d="M12 2a1 1 0 011 1v1h3a1 1 0 010 2h-1v2h3a2 2 0 012 2v7a3 3 0 01-3 3h-1a3 3 0 01-3-3H9a3 3 0 01-3 3H5a3 3 0 01-3-3V10a2 2 0 012-2h3V6H6a1 1 0 010-2h3V3a1 1 0 011-1h2zm-6 8v5a1 1 0 001 1h10a1 1 0 001-1v-5H6zm3 2a1 1 0 110 2 1 1 0 010-2zm6 0a1 1 0 110 2 1 1 0 010-2z"/>
                      </svg>
                    </div>
                    <div class="ai-indicator">AI</div>
                  </div>
          <div class="typing-bubble">
            <div class="typing-dots">
              <div class="typing-dot"></div>
              <div class="typing-dot"></div>
              <div class="typing-dot"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- Input Area -->
      <div class="ultra-chat-input-area">
        <!-- Attachment Menu -->
        <div v-if="showAttachMenu" class="attach-menu">
          <button class="attach-option" @click="attachImage">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M4 16L8 12L12 16L16 12L20 16V4H4V16Z" fill="currentColor"/>
            </svg>
            <span>Hình ảnh</span>
          </button>
          <button class="attach-option" @click="attachFile">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M14 2H6C4.9 2 4 2.9 4 4V16C4 17.1 4.9 18 6 18H14C15.1 18 16 17.1 16 16V4C16 2.9 15.1 2 14 2ZM14 16H6V4H14V16Z" fill="currentColor"/>
            </svg>
            <span>Tệp tin</span>
          </button>
          <button class="attach-option" @click="attachLocation">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M10 2C6.69 2 4 4.69 4 8c0 5.25 6 10 6 10s6-4.75 6-10c0-3.31-2.69-6-6-6zm0 8.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z" fill="currentColor"/>
            </svg>
            <span>Vị trí</span>
          </button>
        </div>

        <!-- Input Container -->
        <div class="input-container">
          <button 
            class="attach-btn" 
            @click="showAttachMenu = !showAttachMenu"
            :class="{ active: showAttachMenu }"
          >
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M15.5 7H12V14C12 15.1 11.1 16 10 16S8 15.1 8 14V7H4.5C3.7 7 3 6.3 3 5.5S3.7 4 4.5 4H15.5C16.3 4 17 4.7 17 5.5S16.3 7 15.5 7Z" fill="currentColor"/>
            </svg>
          </button>
          
          <div class="input-wrapper">
            <input 
              v-model="inputMessage"
              @keydown.enter.prevent="() => sendMessage()"
              @input="handleInput"
              @focus="handleInputFocus"
              @blur="handleInputBlur"
              placeholder="Nhập tin nhắn..."
              class="ultra-chat-input"
              ref="messageInput"
            />
            
            <!-- Suggestions -->
            <div v-if="suggestions.length > 0 && showSuggestions" class="suggestions">
              <div 
                v-for="suggestion in suggestions" 
                :key="suggestion"
                class="suggestion-item"
                @click="selectSuggestion(suggestion)"
              >
                {{ suggestion }}
              </div>
            </div>
          </div>
          
          <button 
            class="emoji-btn"
            @click="showEmojiPicker = !showEmojiPicker"
          >
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M10 18C14.4183 18 18 14.4183 18 10C18 5.58172 14.4183 2 10 2C5.58172 2 2 5.58172 2 10C2 14.4183 5.58172 18 10 18Z" stroke="currentColor" stroke-width="2"/>
              <path d="M8 14s1.5 2 4 2 4-2 4-2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <circle cx="7" cy="8" r="1" fill="currentColor"/>
              <circle cx="13" cy="8" r="1" fill="currentColor"/>
            </svg>
          </button>
          
          <button 
            class="send-btn" 
            @click="() => sendMessage()"
            :disabled="!inputMessage.trim()"
            :class="{ active: inputMessage.trim() }"
          >
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M2.01 21L23 12L2.01 3L2 10L17 12L2 14L2.01 21Z" fill="currentColor"/>
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useWindowSize } from '@vueuse/core'
import { useUltraChat } from '../composables/useUltraChat.js'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import userAvatar from '@/assets/avatar.png'

// Use ultra chat composable
const {
  isOpen,
  isMinimized,
  isFullscreen,
  isTyping,
  isBotTyping,
  isConnected,
  connectionStatus,
  messages,
  unreadCount,
  userContext,
  ai,
  realtime,
  media,
  ui,
  analytics,
  features,
  inputMessage,
  suggestions,
  showSuggestions,
  showAttachMenu,
  showEmojiPicker,
  showSettings,
  openChat,
  closeChat,
  minimizeChat,
  maximizeChat,
  toggleFullscreen,
  sendMessage,
  sendQuickReply,
  addMessage,
  addBotMessage,
  scrollToBottom,
  trackEvent,
  formatTime,
  formatPrice,
  processWithAI,
  analyzeIntent,
  generateAIResponse,
  getProductRecommendations
} = useUltraChat()

// register fontawesome component locally
const fontAwesomeIcon = FontAwesomeIcon

// Window size for responsive design
const { width } = useWindowSize()
const isMobile = computed(() => width.value < 768)

// Form data for forms
const formData = ref({})

// Refs
const messagesContainer = ref(null)
const messageInput = ref(null)
const compareFullscreen = ref(null)

// Methods
const getStatusText = () => {
  switch (realtime.agentStatus) {
    case 'online': return 'Đang online'
    case 'busy': return 'Đang bận'
    case 'away': return 'Tạm vắng'
    case 'offline': return 'Offline'
    default: return 'Đang online'
  }
}

const handleInput = () => {
  if (inputMessage.value.length > 2) {
    suggestions.value = generateSuggestions(inputMessage.value)
    showSuggestions.value = true
  } else {
    suggestions.value = []
    showSuggestions.value = false
  }
}

const handleInputFocus = () => {
  showSuggestions.value = suggestions.value.length > 0
}

const handleInputBlur = () => {
  setTimeout(() => {
    showSuggestions.value = false
  }, 200)
}

const generateSuggestions = (text) => {
  const commonPhrases = [
    'iPhone 15 Pro Max',
    'Samsung Galaxy S24',
    'Xiaomi 14',
    'Oppo Find X7',
    'Vivo X100',
    'dưới 10 triệu',
    'dưới 15 triệu',
    'chơi game',
    'chụp ảnh',
    'pin trâu',
    'so sánh điện thoại',
    'tra cứu đơn hàng',
    'hỗ trợ bảo hành'
  ]
  
  return commonPhrases.filter(phrase => 
    phrase.toLowerCase().includes(text.toLowerCase())
  ).slice(0, 5)
}

const selectSuggestion = (suggestion) => {
  inputMessage.value = suggestion
  suggestions.value = []
  showSuggestions.value = false
  sendMessage()
}

const attachImage = () => {
  showAttachMenu.value = false
  trackEvent('attach_image')
  // Implement image attachment
}

const attachFile = () => {
  showAttachMenu.value = false
  trackEvent('attach_file')
  // Implement file attachment
}

const attachLocation = () => {
  showAttachMenu.value = false
  trackEvent('attach_location')
  // Implement location sharing
}

const addToCart = (product) => {
  trackEvent('add_to_cart_from_chat', { product_id: product.id })
  addBotMessage({
    type: 'text',
    content: `Đã thêm ${product.name} vào giỏ hàng! Bạn có muốn tiếp tục mua sắm không?`
  })
}

async function resolveProductId(p) {
  const id = p && p.id
  if (typeof id === 'number') return id
  if (!id && p && p.name) {
    try {
      const res = await fetch(`http://localhost:8080/api/san-pham?search=${encodeURIComponent(p.name)}`)
      const arr = res.ok ? await res.json() : []
      if (Array.isArray(arr) && arr.length > 0) return arr[0].id
    } catch (e) {}
  }
  if (typeof id === 'string') {
    // try resolve by string id as search keyword
    try {
      const res = await fetch(`http://localhost:8080/api/san-pham?search=${encodeURIComponent(id)}`)
      const arr = res.ok ? await res.json() : []
      if (Array.isArray(arr) && arr.length > 0) return arr[0].id
    } catch (e) {}
  }
  return null
}

const compareProduct = async (product) => {
  trackEvent('compare_product_from_chat', { product_id: product.id })
  // Build id list: current product + up to 2 already compared
  const comparedIds = []
  messages.value.forEach(m => {
    if (m.type === 'comparison' && Array.isArray(m.content)) {
      m.content.slice(0, 2).forEach(p => comparedIds.push(p.id))
    }
  })
  // Resolve numeric ids
  const resolvedCurrent = await resolveProductId(product)
  const numericCompared = []
  for (const cid of comparedIds) {
    const r = await resolveProductId({ id: cid })
    if (typeof r === 'number') numericCompared.push(r)
  }
  const ids = Array.from(new Set([resolvedCurrent, ...numericCompared].filter(n => typeof n === 'number'))).slice(0, 3)
  if (ids.length === 0) return
  fetch(`http://localhost:8080/api/chatbot/compare-products?productIds=${ids.join(',')}`)
    .then(r => r.ok ? r.json() : null)
    .then(payload => {
      if (payload && payload.success) {
        addBotMessage({
          type: 'comparison',
          title: 'Bảng so sánh sản phẩm',
          products: payload.products || [],
          verdict: payload.verdict,
          quickReviews: payload.quickReviews
        })
      } else {
        addBotMessage({ type: 'text', content: 'Chưa có đủ dữ liệu để so sánh.' })
      }
    })
    .catch(() => addBotMessage({ type: 'text', content: 'Không thể lấy dữ liệu so sánh lúc này.' }))
}

const viewProduct = (product) => {
  trackEvent('view_product_from_chat', { product_id: product.id })
  // Navigate to product page
}

const submitForm = async () => {
  trackEvent('form_submitted', formData.value)
  
  // Handle order tracking form
  const { orderCode, phone } = formData.value || {}
  if (orderCode && phone) {
    try {
      const res = await fetch(`http://localhost:8080/api/chatbot/check-order?orderCode=${encodeURIComponent(orderCode)}&phone=${encodeURIComponent(phone)}`)
      const data = await res.json()
      if (data.success && data.found) {
        const order = data.order
        addBotMessage({
          type: 'text',
          content: `✅ **Thông tin đơn hàng:**\n\n` +
                  `📋 Mã đơn hàng: ${order.maHoaDon}\n` +
                  `👤 Khách hàng: ${order.tenKhachHang}\n` +
                  `📱 Số điện thoại: ${order.soDienThoai}\n` +
                  `📦 Trạng thái: ${order.tenTrangThai || order.trangThai}\n` +
                  `💰 Tổng tiền: ${formatPrice(order.tongTien)}`
        })
      } else {
        addBotMessage({ 
          type: 'text', 
          content: '❌ Không tìm thấy đơn hàng với thông tin đã cung cấp. Vui lòng kiểm tra lại mã đơn hàng và số điện thoại.' 
        })
      }
    } catch (e) {
      addBotMessage({ type: 'text', content: '❌ Không thể tra cứu đơn hàng lúc này. Vui lòng thử lại sau.' })
    }
    formData.value = {}
    return
  }
  
  // Handle customer info form
  const { phone: customerPhone } = formData.value || {}
  if (customerPhone) {
    try {
      const res = await fetch(`http://localhost:8080/api/chatbot/customer-info?phone=${encodeURIComponent(customerPhone)}`)
      const data = await res.json()
      if (data.success && data.found) {
        const customer = data.customer
        addBotMessage({
          type: 'text',
          content: `👤 **Thông tin khách hàng:**\n\n` +
                  `📋 Mã khách hàng: ${customer.maKhachHang}\n` +
                  `👤 Họ tên: ${customer.hoTen}\n` +
                  `📱 Số điện thoại: ${customer.soDienThoai}\n` +
                  `📧 Email: ${customer.email || 'Chưa cập nhật'}\n` +
                  `📅 Ngày tạo: ${new Date(customer.ngayTao).toLocaleDateString('vi-VN')}`
        })
      } else {
        addBotMessage({ 
          type: 'text', 
          content: '❌ Không tìm thấy thông tin khách hàng với số điện thoại này.' 
        })
      }
    } catch (e) {
      addBotMessage({ type: 'text', content: '❌ Không thể tra cứu thông tin khách hàng lúc này.' })
    }
    formData.value = {}
    return
  }
  
  // Handle product comparison form
  const { product1, product2, product3 } = formData.value || {}
  if (product1 && product2) {
    try {
      const products = [product1, product2, product3].filter(Boolean)
      const resolved = []
      
      for (const productName of products) {
        try {
          const res = await fetch(`http://localhost:8080/api/chatbot/search-products?query=${encodeURIComponent(productName)}`)
          const data = await res.json()
          if (data.success && data.products.length > 0) {
            resolved.push(data.products[0].id)
          }
        } catch (e) {}
      }
      
      if (resolved.length >= 2) {
        const cmpRes = await fetch(`http://localhost:8080/api/chatbot/compare-products?productIds=${resolved.join(',')}`)
        const payload = await cmpRes.json()
        if (payload.success) {
          addBotMessage({ 
            type: 'comparison', 
            content: {
              title: 'Bảng so sánh sản phẩm',
              products: payload.products
            }
          })
          try {
            const last = messages && messages[messages.length - 1]
            if (last && last.type === 'comparison') {
              last.products = payload.products || []
              last.verdict = payload.verdict
              last.quickReviews = payload.quickReviews
            }
          } catch (_) {}
        } else {
          addBotMessage({ type: 'text', content: '❌ Không thể so sánh sản phẩm lúc này.' })
        }
      } else {
        addBotMessage({ type: 'text', content: '❌ Vui lòng nhập ít nhất 2 sản phẩm hợp lệ để so sánh.' })
      }
    } catch (e) {
      addBotMessage({ type: 'text', content: '❌ Không thể so sánh sản phẩm lúc này.' })
    }
    formData.value = {}
    return
  }

  // Default form
  addBotMessage({ type: 'text', content: '✅ Cảm ơn bạn đã gửi thông tin! Chúng tôi sẽ xử lý và phản hồi sớm nhất có thể.' })
  formData.value = {}
}

const getComparisonSpecs = (products) => {
  if (!products || products.length === 0) return []
  
  const allSpecs = new Set()
  products.forEach(product => {
    Object.keys(product).forEach(key => {
      if (key !== 'id' && key !== 'name' && key !== 'imageUrl' && key !== 'price' && key !== 'verdict' && key !== 'score' && key !== 'winnerId') {
        allSpecs.add(key)
      }
    })
  })
  
  return Array.from(allSpecs)
}

const labelForSpec = (key) => {
  const map = {
    tenHang: 'Hãng sản xuất',
    tenHeDieuHanh: 'Hệ điều hành',
    tenManHinh: 'Màn hình',
    tenRam: 'RAM',
    tenChip: 'Chip xử lý',
    tenCpu: 'CPU',
    tenGpu: 'GPU',
    tenPin: 'Dung lượng pin',
    tenCameraSau: 'Camera chính',
    tenCameraTruoc: 'Camera trước',
    tenSim: 'SIM',
    giaThamKhao: 'Giá tham khảo (VND)',
    variantSummaries: 'Phiên bản'
  }
  return map[key] || key
}

// Build comparison rows for 2–3 products
const fixedCompareRows = (products) => {
  const norms = (prod) => ({
    tenHang: prod.tenHang,
    tenHeDieuHanh: prod.tenHeDieuHanh,
    tenManHinh: prod.tenManHinh,
    tanSoQuet: extractHz(prod.tenManHinh),
    tenChip: prod.tenChip,
    tenCpu: prod.tenCpu,
    tenGpu: prod.tenGpu,
    tenRam: prod.tenRam,
    tenRom: prod.tenRom,
    tenPin: prod.tenPin,
    tenCameraSau: prod.tenCameraSau,
    tenCameraTruoc: prod.tenCameraTruoc,
    tenSim: prod.tenSim,
    giaThamKhao: prod.giaThamKhao || formatPrice(prod.price || 0)
  })
  const ps = (products || []).map(p => norms(p || {}))
  const val = (key) => ps.map(x => x[key])
  return [
    { key: 'tenHang', label: 'Hãng sản xuất', values: val('tenHang') },
    { key: 'tenHeDieuHanh', label: 'Hệ điều hành', values: val('tenHeDieuHanh') },
    { key: 'tenManHinh', label: 'Màn hình', values: val('tenManHinh') },
    { key: 'tanSoQuet', label: 'Tần số quét', values: val('tanSoQuet') },
    { key: 'tenChip', label: 'Chip xử lý', values: val('tenChip') },
    { key: 'tenRam', label: 'RAM', values: val('tenRam') },
    { key: 'tenPin', label: 'Dung lượng pin', values: val('tenPin') },
    { key: 'tenCameraSau', label: 'Camera chính', values: val('tenCameraSau') },
    { key: 'tenCameraTruoc', label: 'Camera trước', values: val('tenCameraTruoc') },
    { key: 'tenSim', label: 'SIM', values: val('tenSim') },
    { key: 'giaThamKhao', label: 'Giá tham khảo (VND)', values: val('giaThamKhao') },
  ]
}

function extractHz(screen) {
  if (!screen) return ''
  const m = screen.toLowerCase().match(/(\d+\s*hz)/)
  return m ? m[1].toUpperCase() : ''
}

// Enhanced rows with detailed specs and highlighting
const enhancedCompareRows = (products) => {
  const base = fixedCompareRows(products) || []
  const keys = [
    { key: 'tenCpu', label: 'CPU' },
    { key: 'tenGpu', label: 'GPU' },
    { key: 'tenRom', label: 'ROM' },
    { key: 'tenHeDieuHanh', label: 'Hệ điều hành' },
    { key: 'tenChip', label: 'Chip' },
    { key: 'tenPin', label: 'Pin' },
    { key: 'tenManHinh', label: 'Màn hình' },
    { key: 'tenCameraSau', label: 'Camera sau' },
    { key: 'tenCameraTruoc', label: 'Camera trước' },
    { key: 'tenSim', label: 'Sim' }
  ]
  
  const valOf = (k) => (products || []).map(p => {
    const val = (p || {})[k]
    if (!val) return '—'
    
    // Add highlighting for important specs
    if (k === 'tenRam' && val.includes('8') && parseInt(val) >= 8) {
      return `⭐ ${val}`
    }
    if (k === 'tenRom' && val.includes('128') && parseInt(val) >= 128) {
      return `⭐ ${val}`
    }
    if (k === 'tenHeDieuHanh' && val.toLowerCase().includes('ios')) {
      return `🍎 ${val}`
    }
    if (k === 'tenPin' && val.includes('4000') && parseInt(val) >= 4000) {
      return `🔋 ${val}`
    }
    if (k === 'tenManHinh' && val.includes('120')) {
      return `📱 ${val}`
    }
    
    return val
  })
  
  // Avoid duplicates if rows already exist
  const existing = new Set(base.map(r => r.key))
  keys.forEach(k => {
    if (!existing.has(k.key)) {
      const values = valOf(k.key)
      if (values.some(v => v && v !== '—')) {
        base.push({ key: k.key, label: k.label, values })
      }
    }
  })
  return base
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// Lifecycle
onMounted(() => {
  // Auto-scroll to bottom when new messages arrive
  watch(messages, () => {
    nextTick(() => {
      scrollToBottom()
    })
  }, { deep: true })

  // Fullscreen/zoom removed per request

  // ESC to close comparison modal
  const onKey = (e) => {
    if (e.key === 'Escape' && compareFullscreen.value) {
      compareFullscreen.value = null
    }
  }
  window.addEventListener('keydown', onKey)
  onUnmounted(() => window.removeEventListener('keydown', onKey))
})

onUnmounted(() => {
  // Cleanup
})
</script>

<style scoped>
/* Ultra Chatbox Styles - Modern & Beautiful */
.ultra-chatbox-container {
  position: fixed;
  bottom: 20px;
  right: 400px; /* Đặt bên trái CustomerChatWidget */
  z-index: 9999;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* Floating Toggle Button */
.ultra-chat-toggle {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #ff8a00 0%, #ff5e62 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.4);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.ultra-chat-toggle:hover {
  transform: scale(1.1);
  box-shadow: 0 12px 40px rgba(102, 126, 234, 0.6);
}

.ultra-chat-toggle.has-unread {
  animation: pulse 2s infinite;
}

.toggle-icon {
  color: white;
  position: relative;
}

.typing-animation {
  display: flex;
  gap: 3px;
}

.typing-dot {
  width: 4px;
  height: 4px;
  background: white;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-dot:nth-child(1) { animation-delay: -0.32s; }
.typing-dot:nth-child(2) { animation-delay: -0.16s; }

.unread-badge {
  position: absolute;
  top: -5px;
  right: -5px;
  background: #ef4444;
  color: white;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  border: 2px solid white;
}

.connection-status {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid white;
}

.connection-status.connected {
  background: #10b981;
}

.connection-status.connecting {
  background: #f59e0b;
  animation: pulse 1s infinite;
}

.connection-status.disconnected {
  background: #ef4444;
}

/* Chat Window */
.ultra-chat-window {
  width: 400px;
  height: 600px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(20px);
}

.ultra-chat-window.minimized {
  height: 60px;
  width: 60px;
  border-radius: 50%;
  padding: 0;
  overflow: hidden;
}

.ultra-chat-window.minimized .ultra-chat-header,
.ultra-chat-window.minimized .settings-panel,
.ultra-chat-window.minimized .ultra-chat-messages,
.ultra-chat-window.minimized .ultra-chat-input-area {
  display: none;
}

.ultra-chat-window.fullscreen {
  width: 100vw;
  height: 100vh;
  border-radius: 0;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  right: 0;
}

.ultra-chat-window.dark-mode {
  background: #0f1115;
  color: #e5e7eb;
}

.ultra-chat-window.compact-mode {
  width: 350px;
  height: 500px;
}

/* Header */
.ultra-chat-header {
  background: linear-gradient(135deg, #ff8a00 0%, #ff5e62 100%);
  color: white;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar-container {
  position: relative;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.status-indicator {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid white;
}

.status-indicator.online {
  background: #10b981;
}

.status-indicator.busy {
  background: #f59e0b;
}

.status-indicator.away {
  background: #ef4444;
}

.header-info {
  flex: 1;
}

.chat-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 4px 0;
}

.status-text {
  font-size: 12px;
  margin: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
  opacity: 0.9;
}

.status-line {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.status-dot.online {
  background: #10b981;
}

.status-dot.busy {
  background: #f59e0b;
}

.status-dot.away {
  background: #ef4444;
}

.response-time {
  opacity: 0.7;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.header-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.header-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.05);
}

.header-btn.close-btn:hover {
  background: rgba(239, 68, 68, 0.8);
}

/* Settings Panel */
.settings-panel {
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  padding: 16px 20px;
  animation: slideDown 0.3s ease;
}

.settings-section {
  margin-bottom: 16px;
}

.settings-section:last-child {
  margin-bottom: 0;
}

.settings-section h4 {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin: 0 0 12px 0;
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.setting-item label {
  font-size: 14px;
  color: #6b7280;
}

.toggle-switch {
  width: 44px;
  height: 24px;
  background: #d1d5db;
  border-radius: 12px;
  position: relative;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  display: inline-flex;
  align-items: center;
  justify-content: flex-start;
  padding: 0;
  line-height: 0;
  vertical-align: middle;
}

.toggle-switch.active {
  background: #10b981;
}

.toggle-slider {
  width: 20px;
  height: 20px;
  background: white;
  border-radius: 50%;
  position: absolute;
  top: 50%;
  left: 2px;
  transform: translateY(-50%);
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.toggle-switch.active .toggle-slider {
  transform: translate(20px, -50%);
}

/* Messages Area */
.ultra-chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: #f8fafc;
}

.ultra-chat-messages::-webkit-scrollbar {
  width: 6px;
}

.ultra-chat-messages::-webkit-scrollbar-track {
  background: transparent;
}

.ultra-chat-messages::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.ultra-chat-messages::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

/* Message Styles */
.message-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.user-message {
  display: flex;
  justify-content: flex-end;
  align-items: flex-end;
  gap: 8px;
}

.bot-message {
  display: flex;
  justify-content: flex-start;
  align-items: flex-start;
  gap: 8px;
}

.user-avatar,
.bot-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  flex-shrink: 0;
  position: relative;
}

.user-avatar img,
.bot-avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.ai-indicator {
  position: absolute;
  bottom: -2px;
  right: -2px;
  background: #ff8a00;
  color: white;
  font-size: 8px;
  font-weight: 600;
  padding: 2px 4px;
  border-radius: 4px;
  border: 1px solid white;
}

.message-content {
  max-width: 70%;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 18px;
  position: relative;
  word-wrap: break-word;
}

.user-bubble {
  background: linear-gradient(135deg, #ff8a00 0%, #ff5e62 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

.bot-bubble {
  background: white;
  color: #374151;
  border: 1px solid #e5e7eb;
  border-bottom-left-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.message-time {
  font-size: 11px;
  color: #9ca3af;
  text-align: right;
  margin-top: 4px;
}

.bot-message .message-time {
  text-align: left;
}

/* Quick Replies */
.quick-replies {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.quick-reply-btn {
  background: #f3f4f6;
  border: 1px solid #e5e7eb;
  border-radius: 20px;
  padding: 8px 16px;
  font-size: 14px;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.quick-reply-btn:hover {
  background: #e5e7eb;
  border-color: #d1d5db;
  transform: translateY(-1px);
}

/* Form Messages */
.form-message {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 16px;
  margin: 8px 0;
  border: 1px solid #e9ecef;
}

.form-header h4 {
  margin: 0 0 16px 0;
  color: #333;
  font-size: 16px;
  font-weight: 600;
}

.chat-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.form-field label {
  font-size: 14px;
  font-weight: 500;
  color: #555;
}

.form-input {
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.3s ease;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-submit-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 8px;
}

.form-submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

/* Enhanced Product Cards */
.product-cards-header h4 {
  margin: 0 0 16px 0;
  color: #333;
  font-size: 16px;
  font-weight: 600;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
  max-height: 300px;
  overflow-y: auto;
}

.product-card {
  background: white;
  border-radius: 12px;
  padding: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #e9ecef;
}

.product-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
}

.product-image {
  width: 100%;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 8px;
  background: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.product-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin: 0;
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-price {
  font-size: 16px;
  font-weight: 700;
  color: #e74c3c;
}

.product-specs {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin: 4px 0;
}

.spec-tag {
  background: #f1f3f4;
  color: #666;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
}

.product-stock {
  font-size: 12px;
  color: #666;
  font-style: italic;
}

/* Product Cards */
.product-cards {
  margin-top: 8px;
}

.product-cards-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
}

.product-cards-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}

.product-card {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.product-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.product-image {
  position: relative;
  margin-bottom: 12px;
  border-radius: 8px;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
}

.discount-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: #ef4444;
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.product-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.3s ease;
}

.product-card:hover .product-overlay {
  opacity: 1;
}

.overlay-btn {
  background: white;
  border: none;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.overlay-btn:hover {
  transform: scale(1.1);
}

.product-info {
  text-align: left;
}

.product-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
  line-height: 1.4;
}

.product-price {
  font-size: 18px;
  font-weight: 700;
  color: #059669;
  margin-bottom: 8px;
}

.product-rating {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-size: 14px;
}

.stars {
  color: #f59e0b;
  font-weight: 500;
}

.stock {
  color: #6b7280;
}

.product-specs {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 16px;
}

.spec-tag {
  background: #f3f4f6;
  color: #374151;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}

.product-actions {
  display: flex;
  gap: 8px;
}

.btn-primary {
  flex: 1;
  background: #ff8a00;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 10px 16px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-primary:hover {
  background: #ff6a00;
  transform: translateY(-1px);
}

.btn-secondary {
  flex: 1;
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 10px 16px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-secondary:hover {
  background: #e5e7eb;
  transform: translateY(-1px);
}

/* Form */
.form-container {
  margin-top: 8px;
}

.form-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
}

.form-field {
  margin-bottom: 16px;
}

.form-field label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 6px;
}

.form-input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.2s ease;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-submit {
  width: 100%;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 12px 16px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.form-submit:hover {
  background: #5a67d8;
  transform: translateY(-1px);
}

/* Comparison Table */
.comparison-table {
  margin-top: 8px;
}

.comparison-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
}

.comparison-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.compare-fullscreen-btn {
  border: none;
  background: #e5e7eb;
  color: #374151;
  border-radius: 8px;
  padding: 6px 10px;
  cursor: pointer;
}
.compare-fullscreen-btn:hover {
  background: #d1d5db;
}

.compare-modal {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
}

.compare-modal-content {
  background: #fff;
  width: 80vw;
  max-width: 1200px;
  max-height: 85vh;
  overflow: auto;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}

.compare-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.compare-modal-actions button {
  border: none;
  margin-left: 8px;
  border-radius: 8px;
  padding: 6px 10px;
  cursor: pointer;
}

.compare-restore { background: #e5e7eb; color: #374151; }
.compare-close { background: #ef4444; color: #fff; }

.compare-close {
  border: none;
  background: #ef4444;
  color: #fff;
  border-radius: 8px;
  padding: 6px 10px;
  cursor: pointer;
}

.comparison-grid {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.comparison-header {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  background: #f8fafc;
}

.comparison-row {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  border-top: 1px solid #e5e7eb;
}

.comparison-cell {
  padding: 12px 16px;
  font-size: 14px;
  border-right: 1px solid #e5e7eb;
}

.comparison-cell:last-child {
  border-right: none;
}

.product-header {
  text-align: center;
  background: #f8fafc;
}

.product-header img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 8px;
}

.product-header .product-name {
  font-weight: 600;
  margin-bottom: 4px;
}

.product-header .product-price {
  font-size: 14px;
  color: #059669;
}

.spec-name {
  font-weight: 500;
  background: #f8fafc;
}

.spec-value {
  text-align: center;
}

/* Typing Indicator */
.typing-indicator {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-top: 8px;
}

.typing-bubble {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 18px;
  border-bottom-left-radius: 4px;
  padding: 12px 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.typing-dots {
  display: flex;
  gap: 4px;
}

.typing-dots .typing-dot {
  width: 8px;
  height: 8px;
  background: #9ca3af;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-dots .typing-dot:nth-child(1) { animation-delay: -0.32s; }
.typing-dots .typing-dot:nth-child(2) { animation-delay: -0.16s; }

/* Input Area */
.ultra-chat-input-area {
  background: white;
  border-top: 1px solid #e5e7eb;
  padding: 16px 20px;
}

.attach-menu {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
  padding: 12px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
}

.attach-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 12px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  min-width: 80px;
}

.attach-option:hover {
  background: #f3f4f6;
  border-color: #d1d5db;
  transform: translateY(-1px);
}

.attach-option span {
  font-size: 12px;
  color: #6b7280;
  font-weight: 500;
}

.input-container {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 24px;
  padding: 8px 12px;
  transition: all 0.2s ease;
}

.input-container:focus-within {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.attach-btn,
.emoji-btn,
.send-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  color: #6b7280;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.attach-btn:hover,
.emoji-btn:hover {
  background: #e5e7eb;
  color: #374151;
}

.attach-btn.active {
  background: #667eea;
  color: white;
}

.send-btn {
  background: #e5e7eb;
  color: #9ca3af;
}

.send-btn.active {
  background: #667eea;
  color: white;
}

.send-btn:hover.active {
  background: #5a67d8;
  transform: scale(1.05);
}

.input-wrapper {
  flex: 1;
  position: relative;
}

.ultra-chat-input {
  width: 100%;
  border: none;
  background: transparent;
  padding: 8px 12px;
  font-size: 14px;
  color: #374151;
  outline: none;
  resize: none;
  max-height: 120px;
  min-height: 20px;
}

.ultra-chat-input::placeholder {
  color: #9ca3af;
}

/* Suggestions */
.suggestions {
  position: absolute;
  bottom: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  margin-bottom: 8px;
  max-height: 200px;
  overflow-y: auto;
  z-index: 10;
}

.suggestion-item {
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  border-bottom: 1px solid #f3f4f6;
}

.suggestion-item:last-child {
  border-bottom: none;
}

.suggestion-item:hover {
  background: #f8fafc;
}

/* Animations */
@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-10px);
  }
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Responsive Design */
@media (max-width: 1200px) {
  .ultra-chatbox-container {
    right: 20px;
    bottom: 80px; /* Đặt phía trên CustomerChatWidget */
  }
}

@media (max-width: 768px) {
  .ultra-chatbox-container {
    bottom: 10px;
    right: 10px;
    left: 10px;
  }
  
  .ultra-chat-window {
    width: 100%;
    height: 80vh;
    border-radius: 16px;
  }
  
  .ultra-chat-window.fullscreen {
    height: 100vh;
    border-radius: 0;
  }
  
  .product-cards-grid {
    grid-template-columns: 1fr;
  }
  
  .comparison-header,
  .comparison-row {
    grid-template-columns: 1fr 1fr;
  }
  
  .comparison-cell:last-child {
    display: none;
  }
}

/* Dark Mode */
.ultra-chat-window.dark-mode .ultra-chat-messages {
  background: #1a1a1a;
}

.ultra-chat-window.dark-mode .bot-bubble {
  background: #1a1d23;
  color: #e5e7eb;
  border-color: #2b2f36;
}

.ultra-chat-window.dark-mode .product-card {
  background: #1a1d23;
  border-color: #2b2f36;
  color: #e5e7eb;
}

.ultra-chat-window.dark-mode .product-name {
  color: #e5e5e5;
}

.ultra-chat-window.dark-mode .ultra-chat-input-area {
  background: #0f1115;
  border-color: #2b2f36;
}

.ultra-chat-window.dark-mode .input-container {
  background: #1a1d23;
  border-color: #2b2f36;
}

.ultra-chat-window.dark-mode .ultra-chat-input {
  color: #e5e5e5;
}

.ultra-chat-window.dark-mode .ultra-chat-input::placeholder {
  color: #9ca3af;
}

.ultra-chat-window.dark-mode .suggestions {
  background: #1a1d23;
  border-color: #2b2f36;
}

.ultra-chat-window.dark-mode .suggestion-item {
  color: #e5e7eb;
  border-color: #2b2f36;
}

.ultra-chat-window.dark-mode .suggestion-item:hover {
  background: #232831;
}
</style>
