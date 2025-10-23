import './assets/main.css'
import './assets/css/dark-mode.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { useTranslation } from './composables/useTranslation.js'

// Import FontAwesome
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js' // ← THÊM BOOTSTRAP JS
import 'bootstrap-icons/font/bootstrap-icons.css'

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

const app = createApp(App)
const pinia = createPinia()

// Register FontAwesome component
app.component('font-awesome-icon', FontAwesomeIcon)

// Initialize global translation system
const { initLanguage } = useTranslation()
initLanguage()

app.use(router)
app.use(pinia)

app.mount('#app')
