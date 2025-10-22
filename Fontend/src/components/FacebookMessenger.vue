<template>
  <div id="fb-root"></div>
  <!-- Messenger Chat Plugin -->
  <div id="fb-customer-chat" class="fb-customerchat"></div>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue'

const props = defineProps({
  pageId: {
    type: String,
    default: '796591066879645'
  },
  themeColor: {
    type: String,
    default: '#0084ff'
  },
  loggedInGreeting: {
    type: String,
    default: 'Xin chào! Chúng tôi có thể giúp gì cho bạn?'
  },
  loggedOutGreeting: {
    type: String,
    default: 'Xin chào! Chúng tôi có thể giúp gì cho bạn?'
  }
})

let fbLoaded = false

const loadFacebookSDK = () => {
  return new Promise((resolve) => {
    if (fbLoaded) {
      resolve()
      return
    }

    // Load Facebook SDK
    const script = document.createElement('script')
    script.src = 'https://connect.facebook.net/vi_VN/sdk/xfbml.customerchat.js'
    script.async = true
    script.defer = true
    script.crossOrigin = 'anonymous'
    
    script.onload = () => {
      fbLoaded = true
      resolve()
    }
    
    document.head.appendChild(script)
  })
}

const initMessenger = () => {
  if (window.FB) {
    window.FB.init({
      xfbml: true,
      version: 'v18.0'
    })
    
    // Initialize customer chat
    window.FB.CustomerChat.showDialog()
  }
}

onMounted(async () => {
  try {
    await loadFacebookSDK()
    
    // Wait a bit for SDK to fully load
    setTimeout(() => {
      initMessenger()
    }, 1000)
  } catch (error) {
    console.error('Error loading Facebook Messenger:', error)
  }
})

onUnmounted(() => {
  // Cleanup if needed
  if (window.FB && window.FB.CustomerChat) {
    window.FB.CustomerChat.hideDialog()
  }
})
</script>

<style scoped>
.fb-customerchat {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 9999;
}

/* Custom styling for the chat widget */
:deep(.fb-customerchat) {
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* Mobile responsive */
@media (max-width: 768px) {
  .fb-customerchat {
    bottom: 10px;
    right: 10px;
  }
}
</style>







