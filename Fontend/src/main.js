import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './polyfills/global.js'
import './assets/main.css'

// Import FontAwesome
import { FontAwesomeIcon } from './plugins/fontawesome.js'

// Import WebSocket components
import CustomerChatWidget from './components/CustomerChatWidget.vue'

// Import auth store
import { useAuthStore } from './stores/authStore'

// Import Messenger Toggle utility
import { MessengerToggle } from './utils/messengerToggle.js'

const app = createApp(App)
const pinia = createPinia()

// Register global components
app.component('CustomerChatWidget', CustomerChatWidget)
app.component('font-awesome-icon', FontAwesomeIcon)

app.use(pinia)
app.use(router)

// Initialize auth store with error handling
const authStore = useAuthStore()

// Chỉ log trong development
if (import.meta.env.DEV) {
  console.log('Initializing auth store...')
}

authStore.initializeAuth().then(() => {
  if (import.meta.env.DEV) {
    console.log('Auth initialization completed')
  }
}).catch(error => {
  console.error('Auth initialization failed:', error)
})

// Initialize Messenger Toggle
MessengerToggle.init()

app.mount('#app')