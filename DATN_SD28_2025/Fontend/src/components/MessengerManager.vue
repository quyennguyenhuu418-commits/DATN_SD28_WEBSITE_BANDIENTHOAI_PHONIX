<template>
  <div class="messenger-manager">
    <!-- Control Panel (only visible in development or for admin) -->
    <div v-if="showControls" class="messenger-controls">
      <button 
        @click="toggleMessenger"
        class="control-btn"
        :class="{ 'active': isMessengerEnabled }"
      >
        <i class="fab fa-facebook-messenger"></i>
        <span>{{ isMessengerEnabled ? 'Tắt' : 'Bật' }} Messenger</span>
      </button>
      
      <div class="control-options">
        <label class="control-label">
          <input 
            type="radio" 
            value="bottom-right" 
            v-model="position"
            @change="updatePosition"
          >
          Dưới phải
        </label>
        <label class="control-label">
          <input 
            type="radio" 
            value="bottom-left" 
            v-model="position"
            @change="updatePosition"
          >
          Dưới trái
        </label>
        <label class="control-label">
          <input 
            type="radio" 
            value="top-right" 
            v-model="position"
            @change="updatePosition"
          >
          Trên phải
        </label>
        <label class="control-label">
          <input 
            type="radio" 
            value="top-left" 
            v-model="position"
            @change="updatePosition"
          >
          Trên trái
        </label>
      </div>
    </div>
    
    <!-- Facebook Messenger Widget -->
    <FacebookMessengerFloating
      ref="messengerRef"
      :pageId="pageId"
      :enabled="isMessengerEnabled"
      :position="position"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import FacebookMessengerFloating from './FacebookMessengerFloating.vue'

const props = defineProps({
  pageId: {
    type: String,
    default: '796591066879645'
  },
  showControls: {
    type: Boolean,
    default: false // Only show in development or for admin
  },
  defaultEnabled: {
    type: Boolean,
    default: true
  }
})

const messengerRef = ref(null)
const isMessengerEnabled = ref(props.defaultEnabled)
const position = ref('bottom-right')

const toggleMessenger = () => {
  isMessengerEnabled.value = !isMessengerEnabled.value
  saveSettings()
}

const updatePosition = () => {
  saveSettings()
}

const saveSettings = () => {
  const settings = {
    enabled: isMessengerEnabled.value,
    position: position.value
  }
  localStorage.setItem('messenger-settings', JSON.stringify(settings))
}

const loadSettings = () => {
  const saved = localStorage.getItem('messenger-settings')
  if (saved) {
    try {
      const settings = JSON.parse(saved)
      isMessengerEnabled.value = settings.enabled ?? props.defaultEnabled
      position.value = settings.position ?? 'bottom-right'
    } catch (error) {
      console.warn('Failed to load messenger settings:', error)
    }
  }
}

// Watch for changes and save
watch([isMessengerEnabled, position], () => {
  saveSettings()
})

onMounted(() => {
  loadSettings()
  
  // Listen for settings updates from MessengerSettingsPage
  window.addEventListener('messenger-settings-updated', (event) => {
    const settings = event.detail
    isMessengerEnabled.value = settings.enabled
    position.value = settings.position
    if (messengerRef.value) {
      // Update the messenger component if it exists
      messengerRef.value.pageId = settings.pageId
    }
  })
})

// Expose methods for parent component
defineExpose({
  toggleMessenger,
  enableMessenger: () => { isMessengerEnabled.value = true },
  disableMessenger: () => { isMessengerEnabled.value = false },
  isEnabled: () => isMessengerEnabled.value
})
</script>

<style scoped>
.messenger-manager {
  position: relative;
}

.messenger-controls {
  position: fixed;
  top: 20px;
  left: 20px;
  background: white;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  z-index: 10001;
  min-width: 200px;
}

.control-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #f3f4f6;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  width: 100%;
  margin-bottom: 12px;
}

.control-btn:hover {
  background: #e5e7eb;
}

.control-btn.active {
  background: #0084ff;
  color: white;
  border-color: #0084ff;
}

.control-btn i {
  font-size: 16px;
}

.control-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.control-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  cursor: pointer;
}

.control-label input[type="radio"] {
  margin: 0;
}

/* Hide controls on mobile */
@media (max-width: 768px) {
  .messenger-controls {
    display: none;
  }
}
</style>
