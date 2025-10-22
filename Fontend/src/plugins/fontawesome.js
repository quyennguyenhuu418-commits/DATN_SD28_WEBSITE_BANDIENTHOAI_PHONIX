// FontAwesome Global Configuration
import { library } from '@fortawesome/fontawesome-svg-core'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'

// Import all icon sets
import { fas } from '@fortawesome/free-solid-svg-icons'
import { far } from '@fortawesome/free-regular-svg-icons'
import { fab } from '@fortawesome/free-brands-svg-icons'

// Add all icon sets to library
library.add(fas, far, fab)

// Export the component for global registration
export { FontAwesomeIcon }

// Optional: Export commonly used icons for type checking
export const commonIcons = {
  // Navigation
  arrowLeft: 'arrow-left',
  arrowRight: 'arrow-right',
  chevronLeft: 'chevron-left',
  chevronRight: 'chevron-right',
  home: 'home',
  
  // Actions
  plus: 'plus',
  edit: 'edit',
  trash: 'trash',
  save: 'floppy-disk',
  check: 'check',
  xmark: 'xmark',
  star: 'star',
  
  // User & Profile
  user: 'user',
  users: 'users',
  userPlus: 'user-plus',
  userEdit: 'user-edit',
  
  // Communication
  phone: 'phone',
  envelope: 'envelope',
  comment: 'comment',
  
  // Location & Address
  mapMarkerAlt: 'map-marker-alt',
  flag: 'flag',
  building: 'building',
  
  // Business
  box: 'box',
  shoppingCart: 'shopping-cart',
  dollarSign: 'dollar-sign',
  chartLine: 'chart-line',
  fileInvoice: 'file-invoice',
  cashRegister: 'cash-register',
  
  // System
  cog: 'cog',
  eye: 'eye',
  lock: 'lock',
  globe: 'globe',
  clipboardList: 'clipboard-list',
  tag: 'tag',
  mailBulk: 'mail-bulk',
  calendar: 'calendar'
}

// Usage example:
// <font-awesome-icon :icon="['fas', 'user']" />
// <font-awesome-icon :icon="['far', 'star']" />
// <font-awesome-icon :icon="['fab', 'github']" />





