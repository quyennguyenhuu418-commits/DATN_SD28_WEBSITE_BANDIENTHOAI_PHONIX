<template>
  <div>
    <!-- Facebook Customer Chat Plugin -->
    <div 
      id="fb-customer-chat" 
      class="fb-customerchat"
      :data-page_id="pageId"
      :data-theme_color="themeColor"
      :data-logged_in_greeting="loggedInGreeting"
      :data-logged_out_greeting="loggedOutGreeting"
    ></div>
  </div>
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

let fbSDKLoaded = false

const loadFacebookSDK = () => {
  return new Promise((resolve, reject) => {
    if (fbSDKLoaded) {
      resolve()
      return
    }

    // Check if SDK is already loaded
    if (window.FB) {
      fbSDKLoaded = true
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
      fbSDKLoaded = true
      resolve()
    }
    
    script.onerror = () => {
      reject(new Error('Failed to load Facebook SDK'))
    }
    
    document.head.appendChild(script)
  })
}

const initializeCustomerChat = () => {
  if (window.FB) {
    window.FB.init({
      xfbml: true,
      version: 'v18.0'
    })
    
    // Parse customer chat elements
    window.FB.XFBML.parse()
  }
}

onMounted(async () => {
  try {
    await loadFacebookSDK()
    
    // Wait for DOM to be ready
    setTimeout(() => {
      initializeCustomerChat()
    }, 500)
  } catch (error) {
    console.error('Error loading Facebook Customer Chat:', error)
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
/* Facebook Customer Chat Plugin styling */
:deep(.fb-customerchat) {
  position: fixed !important;
  bottom: 20px !important;
  right: 20px !important;
  z-index: 9999 !important;
}

/* Mobile responsive */
@media (max-width: 768px) {
  :deep(.fb-customerchat) {
    bottom: 10px !important;
    right: 10px !important;
  }
}

/* Custom styling for the chat button */
:deep(.fb-customerchat .fb_dialog_content) {
  border-radius: 12px !important;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15) !important;
}

:deep(.fb-customerchat .fb_dialog_content .fb_dialog_content_cover) {
  border-radius: 12px !important;
}
</style>







