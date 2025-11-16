<template>
  <div v-if="isEnabled" class="facebook-messenger-floating">
    <!-- Messenger Button -->
    <button 
      @click="toggleMessenger"
      class="messenger-floating-btn"
      :class="{ 'active': isOpen }"
      :title="isOpen ? 'Đóng Messenger' : 'Mở Messenger'"
    >
      <i class="fab fa-facebook-messenger"></i>
      <span v-if="isOpen" class="close-icon">×</span>
    </button>
    
    <!-- Messenger Chat Window -->
    <div v-if="isOpen" class="messenger-floating-window">
      <div class="messenger-floating-header">
        <div class="messenger-floating-title">
          <i class="fab fa-facebook-messenger"></i>
          <span>Messenger</span>
        </div>
        <div class="messenger-floating-actions">
          <button @click="openInNewTab" class="action-btn" title="Mở trong tab mới">
            <i class="fas fa-external-link-alt"></i>
          </button>
          <button @click="closeMessenger" class="action-btn" title="Đóng">
            <i class="fas fa-times"></i>
          </button>
        </div>
      </div>
      
      <div class="messenger-floating-content">
        <iframe
          :src="messengerUrl"
          width="100%"
          height="100%"
          frameborder="0"
          scrolling="no"
          allowtransparency="true"
        ></iframe>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const props = defineProps({
  pageId: {
    type: String,
    default: '796591066879645'
  },
  enabled: {
    type: Boolean,
    default: true
  },
  position: {
    type: String,
    default: 'bottom-right', // bottom-right, bottom-left, top-right, top-left
    validator: (value) => ['bottom-right', 'bottom-left', 'top-right', 'top-left'].includes(value)
  }
})

const isOpen = ref(false)
const isEnabled = ref(props.enabled)

const messengerUrl = computed(() => {
  return `https://m.me/${props.pageId}`
})

const toggleMessenger = () => {
  isOpen.value = !isOpen.value
}

const closeMessenger = () => {
  isOpen.value = false
}

const openInNewTab = () => {
  window.open(messengerUrl.value, '_blank', 'noopener,noreferrer')
}

// Toggle enabled state
const toggleEnabled = () => {
  isEnabled.value = !isEnabled.value
  if (!isEnabled.value) {
    isOpen.value = false
  }
}

// Expose methods for parent component
defineExpose({
  toggleEnabled,
  openMessenger: () => { isOpen.value = true },
  closeMessenger,
  isEnabled: () => isEnabled.value
})

onMounted(() => {
  // Load from localStorage if available
  const savedState = localStorage.getItem('facebook-messenger-enabled')
  if (savedState !== null) {
    isEnabled.value = JSON.parse(savedState)
  }
  
  // Save to localStorage when changed
  const unwatch = () => {
    localStorage.setItem('facebook-messenger-enabled', JSON.stringify(isEnabled.value))
  }
  
  // Watch for changes
  const stopWatcher = () => {
    // This will be called when component unmounts
  }
  
  // Save state on change
  const originalValue = isEnabled.value
  const checkForChanges = () => {
    if (isEnabled.value !== originalValue) {
      localStorage.setItem('facebook-messenger-enabled', JSON.stringify(isEnabled.value))
    }
  }
  
  // Check every second (simple approach)
  const interval = setInterval(checkForChanges, 1000)
  
  // Cleanup
  onUnmounted(() => {
    clearInterval(interval)
  })
})
</script>

<style scoped>
.facebook-messenger-floating {
  position: fixed;
  z-index: 10000; /* Higher than CSKH */
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* Position classes */
.facebook-messenger-floating {
  bottom: 20px;
  right: 20px;
}

.facebook-messenger-floating[data-position="bottom-left"] {
  bottom: 20px;
  right: auto;
  left: 20px;
}

.facebook-messenger-floating[data-position="top-right"] {
  top: 20px;
  bottom: auto;
  right: 20px;
}

.facebook-messenger-floating[data-position="top-left"] {
  top: 20px;
  bottom: auto;
  right: auto;
  left: 20px;
}

.messenger-floating-btn {
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
  position: relative;
}

.messenger-floating-btn:hover {
  background: #0066cc;
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(0, 132, 255, 0.4);
}

.messenger-floating-btn.active {
  background: #dc2626;
  transform: scale(1.1);
}

.messenger-floating-btn.active:hover {
  background: #b91c1c;
}

.close-icon {
  position: absolute;
  font-size: 16px;
  font-weight: bold;
}

.messenger-floating-window {
  position: absolute;
  bottom: 80px;
  right: 0;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  width: 350px;
  height: 500px;
  display: flex;
  flex-direction: column;
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.messenger-floating-header {
  background: #0084ff;
  color: white;
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}

.messenger-floating-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.messenger-floating-title i {
  font-size: 18px;
}

.messenger-floating-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  background: none;
  border: none;
  color: white;
  font-size: 14px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.2s ease;
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.messenger-floating-content {
  flex: 1;
  height: 100%;
}

.messenger-floating-content iframe {
  width: 100%;
  height: 100%;
  border: none;
}

/* Mobile responsive */
@media (max-width: 768px) {
  .facebook-messenger-floating {
    bottom: 10px;
    right: 10px;
  }
  
  .facebook-messenger-floating[data-position="bottom-left"] {
    bottom: 10px;
    left: 10px;
  }
  
  .facebook-messenger-floating[data-position="top-right"] {
    top: 10px;
    right: 10px;
  }
  
  .facebook-messenger-floating[data-position="top-left"] {
    top: 10px;
    left: 10px;
  }
  
  .messenger-floating-window {
    width: calc(100vw - 20px);
    max-width: 350px;
    height: 400px;
    bottom: 80px;
    right: 10px;
    left: 10px;
  }
  
  .messenger-floating-btn {
    width: 50px;
    height: 50px;
    font-size: 20px;
  }
}

/* Ensure it doesn't interfere with CSKH */
.facebook-messenger-floating {
  z-index: 10000 !important;
}

/* Hide when CSKH is active (if needed) */
.cskh-active .facebook-messenger-floating {
  display: none;
}
</style>







