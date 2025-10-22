<template>
  <div class="facebook-messenger-widget">
    <!-- Messenger Button -->
    <button 
      v-if="!isOpen"
      @click="openMessenger"
      class="messenger-button"
      title="Mở Messenger"
    >
      <i class="fab fa-facebook-messenger"></i>
    </button>
    
    <!-- Messenger Chat Window -->
    <div v-if="isOpen" class="messenger-window">
      <div class="messenger-header">
        <div class="messenger-title">
          <i class="fab fa-facebook-messenger"></i>
          <span>Messenger</span>
        </div>
        <button @click="closeMessenger" class="close-button">×</button>
      </div>
      
      <div class="messenger-content">
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
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  pageId: {
    type: String,
    default: '796591066879645'
  }
})

const isOpen = ref(false)

const messengerUrl = computed(() => {
  // Sử dụng Facebook Messenger m.me link
  return `https://www.messenger.com/t/${props.pageId}`
})

const openMessenger = () => {
  isOpen.value = true
}

const closeMessenger = () => {
  isOpen.value = false
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

.messenger-button {
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

.messenger-button:hover {
  background: #0066cc;
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(0, 132, 255, 0.4);
}

.messenger-window {
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  width: 350px;
  height: 500px;
  display: flex;
  flex-direction: column;
}

.messenger-header {
  background: #0084ff;
  color: white;
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.close-button {
  background: none;
  border: none;
  color: white;
  font-size: 20px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.2s ease;
}

.close-button:hover {
  background: rgba(255, 255, 255, 0.2);
}

.messenger-content {
  flex: 1;
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
  
  .messenger-window {
    width: calc(100vw - 20px);
    max-width: 350px;
    height: 400px;
  }
  
  .messenger-content {
    height: 400px;
  }
}
</style>







