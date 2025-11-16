// ===== CSS & Style =====
import './assets/main.css'
import './assets/css/dark-mode.css'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'
import 'bootstrap-icons/font/bootstrap-icons.css'

// ===== Vue Core =====
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router/adminRouter'

// ===== Polyfills & Utils =====
import './polyfills/global.js'

// ===== Composables =====
import { useTranslation } from './composables/useTranslation.js'

// ===== Stores =====
import { useAuthStore } from './stores/authStore'

// ===== FontAwesome Setup =====
import { library } from '@fortawesome/fontawesome-svg-core'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import {
  faBox,
  faShoppingCart,
  faDollarSign,
  faUsers,
  faCog,
  faFileInvoice,
  faCashRegister,
  faGlobe,
  faEye,
  faEdit,
  faClipboardList,
  faUser,
  faStar,
  faChartLine,
  faList,
  faTruck,
  faSearch,
  faFilter,
  faRefresh,
  faSpinner,
  faPhone,
  faLaptop,
  faStore,
  faCalendar,
  faChevronLeft,
  faChevronRight,
  faBoxOpen,
  faBarcode,
  faCheck,
  faTimes,
  faSync,
  faPrint,
  faExclamationTriangle,
  faPlus,
  faFileExcel,
  faUpload,
  faSyncAlt,
  faTh,
  faTrash,
  faArrowLeft,
  faUserCog,
  faIdCard,
  faEnvelope,
  faVenusMars,
  faToggleOn,
  faInfoCircle,
  faSave,
  faShippingFast,
  faCheckCircle,
  faTimesCircle,
  faClock,
  faCheckDouble,
  faCamera,
  faStop
} from '@fortawesome/free-solid-svg-icons'

// Add icons to library
library.add(
  faBox,
  faShoppingCart,
  faDollarSign,
  faUsers,
  faCog,
  faFileInvoice,
  faCashRegister,
  faGlobe,
  faEye,
  faEdit,
  faClipboardList,
  faUser,
  faStar,
  faChartLine,
  faList,
  faTruck,
  faSearch,
  faFilter,
  faRefresh,
  faSpinner,
  faPhone,
  faLaptop,
  faStore,
  faCalendar,
  faChevronLeft,
  faChevronRight,
  faBoxOpen,
  faBarcode,
  faCheck,
  faTimes,
  faSync,
  faPrint,
  faExclamationTriangle,
  faPlus,
  faFileExcel,
  faUpload,
  faSyncAlt,
  faTh,
  faTrash,
  faArrowLeft,
  faUserCog,
  faIdCard,
  faEnvelope,
  faVenusMars,
  faToggleOn,
  faInfoCircle,
  faSave,
  faShippingFast,
  faCheckCircle,
  faTimesCircle,
  faClock,
  faCheckDouble,
  faCamera,
  faStop
)

// ===== APP INIT =====
const app = createApp(App)
const pinia = createPinia()

// Register global components
app.component('font-awesome-icon', FontAwesomeIcon)

// Initialize translation
const { initLanguage } = useTranslation()
initLanguage()

app.use(pinia)
app.use(router)

// ===== AUTH INIT =====
const authStore = useAuthStore()

if (import.meta.env.DEV) {
  console.log('Initializing admin auth store...')
}

authStore.initializeAuth()
  .then(() => {
    if (import.meta.env.DEV) {
      console.log('Admin auth initialization completed')
    }
  })
  .catch(error => {
    console.error('Admin auth initialization failed:', error)
  })

// ===== DISABLE DARK MODE =====
// Remove dark class to prevent automatic dark mode in incognito
document.documentElement.classList.remove('dark')
localStorage.setItem('darkMode', false)

// ===== MOUNT =====
app.mount('#app')

