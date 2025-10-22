<template>
  <div class="facebook-messenger-widget">
    <div 
      v-if="showChat" 
      class="messenger-container"
      :class="{ 'minimized': isMinimized }"
    >
      <div class="messenger-header" @click="toggleChat">
        <div class="messenger-title">
          <i class="fab fa-facebook-messenger"></i>
          <span>Messenger</span>
        </div>
        <div class="messenger-controls">
          <button @click.stop="toggleMinimize" class="minimize-btn">
            {{ isMinimized ? '□' : '−' }}
          </button>
          <button @click.stop="closeChat" class="close-btn">×</button>
        </div>
      </div>
      
      <div v-if="!isMinimized" class="messenger-content">
        <iframe
          :src="messengerUrl"
          width="350"
          height="500"
          frameborder="0"
          scrolling="no"
          allowtransparency="true"
        ></iframe>
      </div>
    </div>
    
    <!-- Chat button -->
    <button 
      v-if="!showChat" 
      @click="openChat"
      class="messenger-toggle-btn"
      title="Mở Messenger"
    >
      <i class="fab fa-facebook-messenger"></i>
    </button>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  pageId: {
    type: String,
    default: '796591066879645'
  },
  greeting: {
    type: String,
    default: 'Xin chào! Chúng tôi có thể giúp gì cho bạn?'
  }
})

const showChat = ref(false)
const isMinimized = ref(false)

const messengerUrl = computed(() => {
  const baseUrl = 'https://www.facebook.com/v18.0/plugins/customerchat.php'
  const params = new URLSearchParams({
    'app_id': 'YOUR_APP_ID', // Bạn cần thay thế bằng App ID thực tế
    'page_id': props.pageId,
    'ref': 'page',
    'theme_color': '#0084ff',
    'logged_in_greeting': props.greeting,
    'logged_out_greeting': props.greeting
  })
  
  return `${baseUrl}?${params.toString()}`
})

const openChat = () => {
  showChat.value = true
  isMinimized.value = false
}

const closeChat = () => {
  showChat.value = false
  isMinimized.value = false
}

const toggleChat = () => {
  if (isMinimized.value) {
    isMinimized.value = false
  }
}

const toggleMinimize = () => {
  isMinimized.value = !isMinimized.value
}
</script>

<style scoped>
.facebook-messenger-widget {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 9999;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.messenger-toggle-btn {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: #0084ff;
  border: none;
  color: white;
  font-size: 24px;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 132, 255, 0.3);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.messenger-toggle-btn:hover {
  background: #0066cc;
  transform: scale(1.1);
}

.messenger-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  transition: all 0.3s ease;
  width: 350px;
}

.messenger-container.minimized {
  height: 60px;
}

.messenger-header {
  background: #0084ff;
  color: white;
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
}

.messenger-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.messenger-title i {
  font-size: 18px;
}

.messenger-controls {
  display: flex;
  gap: 8px;
}

.minimize-btn,
.close-btn {
  background: none;
  border: none;
  color: white;
  font-size: 16px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.2s ease;
}

.minimize-btn:hover,
.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.messenger-content {
  height: 500px;
}

.messenger-content iframe {
  width: 100%;
  height: 100%;
  border: none;
}

/* Mobile responsive */
@media (max-width: 768px) {
  .facebook-messenger-widget {
    bottom: 10px;
    right: 10px;
  }
  
  .messenger-container {
    width: calc(100vw - 20px);
    max-width: 350px;
  }
  
  .messenger-content {
    height: 400px;
  }
}
</style>







