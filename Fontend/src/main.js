<<<<<<< HEAD
=======
import './assets/main.css'

>>>>>>> origin/Huan
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
<<<<<<< HEAD
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
=======
>>>>>>> origin/Huan

const app = createApp(App)
const pinia = createPinia()

<<<<<<< HEAD
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
=======
app.use(router)
app.use(pinia)

app.mount('#app')
>>>>>>> origin/Huan
