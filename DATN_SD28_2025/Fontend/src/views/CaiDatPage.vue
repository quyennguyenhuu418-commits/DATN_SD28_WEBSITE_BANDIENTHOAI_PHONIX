<template>
  <div class="settings-page">
    <PosHeader 
      :page-title="translate('systemSettings.title')"
      :page-subtitle="translate('systemSettings.subtitle')"
      :show-chat-toggle="false"
    />

    <!-- Main Content -->
    <div class="settings-container">
      <!-- Quick Actions -->
      <div class="quick-actions">
        <button class="action-btn primary" @click="saveAllSettings">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"></path>
            <polyline points="17,21 17,13 7,13 7,21"></polyline>
            <polyline points="7,3 7,8 15,8"></polyline>
          </svg>
          {{ translate('systemSettings.actions.save') }}
        </button>
        <button class="action-btn secondary" @click="resetToDefault">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="23,4 23,10 17,10"></polyline>
            <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"></path>
          </svg>
          {{ translate('systemSettings.actions.reset') }}
        </button>
        <button class="action-btn outline" @click="exportSettings">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
            <polyline points="7,10 12,15 17,10"></polyline>
            <line x1="12" y1="15" x2="12" y2="3"></line>
          </svg>
          {{ translate('systemSettings.actions.export') }}
        </button>
        </div>

      <!-- Settings Grid -->
      <div class="settings-grid">
        <!-- General Settings -->
        <div class="settings-card">
          <div class="card-header">
            <div class="card-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="3"></circle>
                <path d="M12 1v6m0 6v6m11-7h-6m-6 0H1"></path>
              </svg>
        </div>
            <div class="card-title">
              <h3>{{ translate('systemSettings.general.title') }}</h3>
              <p>{{ translate('systemSettings.general.subtitle') }}</p>
        </div>
          </div>
          <div class="card-content">
            <div class="form-group">
              <label>{{ translate('systemSettings.general.storeName') }}</label>
              <input 
                type="text" 
                v-model="settings.general.storeName" 
                placeholder="Nhập tên cửa hàng"
                class="form-input"
              >
            </div>
            <div class="form-group">
              <label>{{ translate('systemSettings.general.address') }}</label>
              <textarea 
                v-model="settings.general.address" 
                placeholder="Nhập địa chỉ cửa hàng"
                rows="3"
                class="form-textarea"
              ></textarea>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>{{ translate('systemSettings.general.phone') }}</label>
                <input 
                  type="tel" 
                  v-model="settings.general.phone" 
                  placeholder="0123456789"
                  class="form-input"
                >
              </div>
              <div class="form-group">
                <label>{{ translate('systemSettings.general.email') }}</label>
                <input 
                  type="email" 
                  v-model="settings.general.email" 
                  placeholder="admin@store.com"
                  class="form-input"
                >
              </div>
            </div>
            <div class="form-group">
              <label>{{ translate('systemSettings.general.timezone') }}</label>
              <select v-model="settings.general.timezone" class="form-select">
                <option value="Asia/Ho_Chi_Minh">Asia/Ho_Chi_Minh (UTC+7)</option>
                <option value="UTC">UTC (UTC+0)</option>
                <option value="America/New_York">America/New_York (UTC-5)</option>
                <option value="Europe/London">Europe/London (UTC+0)</option>
              </select>
            </div>
        </div>
      </div>

        <!-- Appearance Settings -->
        <div class="settings-card">
          <div class="card-header">
            <div class="card-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"></path>
              </svg>
            </div>
            <div class="card-title">
              <h3>{{ translate('systemSettings.appearance.title') }}</h3>
              <p>{{ translate('systemSettings.appearance.subtitle') }}</p>
            </div>
          </div>
          <div class="card-content">
            <div class="form-group">
              <label>{{ translate('systemSettings.appearance.language') }}</label>
              <select v-model="settings.appearance.language" @change="changeLanguage" class="form-select">
                <option value="vi">🇻🇳 Tiếng Việt</option>
                <option value="en">🇺🇸 English</option>
                <option value="ja">🇯🇵 日本語</option>
                <option value="ko">🇰🇷 한국어</option>
                <option value="zh">🇨🇳 中文</option>
              </select>
            </div>
            <div class="form-group">
              <label class="toggle-label">
                <span>{{ translate('systemSettings.appearance.darkMode') }}</span>
                <div class="toggle-switch">
            <input 
              type="checkbox" 
                    v-model="settings.appearance.darkMode"
              @change="toggleDarkMode"
                  >
              <span class="toggle-slider"></span>
                </div>
            </label>
          </div>
            <div class="form-group">
              <label>{{ translate('systemSettings.appearance.theme') }}</label>
              <div class="theme-options">
                <div 
                  v-for="theme in themes" 
                  :key="theme.value"
                  class="theme-option"
                  :class="{ active: settings.appearance.theme === theme.value }"
                  @click="settings.appearance.theme = theme.value"
                >
                  <div class="theme-preview" :style="{ backgroundColor: theme.color }"></div>
                  <span>{{ theme.name }}</span>
        </div>
              </div>
            </div>
            <div class="form-group">
              <label>{{ translate('systemSettings.appearance.fontSize') }}</label>
              <div class="font-size-options">
                <button 
                  v-for="size in fontSizes" 
                  :key="size.value"
                  class="font-size-btn"
                  :class="{ active: settings.appearance.fontSize === size.value }"
                  @click="settings.appearance.fontSize = size.value"
                >
                  {{ size.label }}
                </button>
              </div>
            </div>
        </div>
      </div>

        <!-- Notification Settings -->
        <div class="settings-card">
          <div class="card-header">
            <div class="card-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path>
                <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
              </svg>
            </div>
          <div class="card-title">
            <h3>{{ translate('systemSettings.notifications.title') }}</h3>
            <p>{{ translate('systemSettings.notifications.subtitle') }}</p>
          </div>
          </div>
          <div class="card-content">
            <div class="form-group">
              <label class="toggle-label">
                <span>{{ translate('systemSettings.notifications.email') }}</span>
                <div class="toggle-switch">
                  <input type="checkbox" v-model="settings.notifications.email">
              <span class="toggle-slider"></span>
                </div>
            </label>
          </div>
            <div class="form-group">
              <label class="toggle-label">
                <span>{{ translate('systemSettings.notifications.newOrders') }}</span>
                <div class="toggle-switch">
                  <input type="checkbox" v-model="settings.notifications.newOrders">
                  <span class="toggle-slider"></span>
        </div>
              </label>
            </div>
            <div class="form-group">
              <label class="toggle-label">
                <span>{{ translate('systemSettings.notifications.sound') }}</span>
                <div class="toggle-switch">
                  <input type="checkbox" v-model="settings.notifications.sound">
              <span class="toggle-slider"></span>
                </div>
            </label>
          </div>
            <div class="form-group">
              <label>{{ translate('systemSettings.notifications.frequency') }}</label>
              <select v-model="settings.notifications.frequency" class="form-select">
                <option value="immediate">{{ translate('systemSettings.notifications.immediate') }}</option>
                <option value="hourly">{{ translate('systemSettings.notifications.hourly') }}</option>
                <option value="daily">{{ translate('systemSettings.notifications.daily') }}</option>
                <option value="weekly">{{ translate('systemSettings.notifications.weekly') }}</option>
          </select>
            </div>
        </div>
      </div>

        <!-- Security Settings -->
        <div class="settings-card">
          <div class="card-header">
            <div class="card-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                <circle cx="12" cy="16" r="1"></circle>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
              </svg>
            </div>
          <div class="card-title">
            <h3>{{ translate('systemSettings.security.title') }}</h3>
            <p>{{ translate('systemSettings.security.subtitle') }}</p>
          </div>
          </div>
          <div class="card-content">
            <div class="form-group">
              <label class="toggle-label">
                <span>{{ translate('systemSettings.security.twoFactor') }}</span>
                <div class="toggle-switch">
                  <input type="checkbox" v-model="settings.security.twoFactor">
              <span class="toggle-slider"></span>
                </div>
            </label>
            </div>
            <div class="form-group">
              <label>{{ translate('systemSettings.security.sessionTimeout') }}</label>
              <select v-model="settings.security.sessionTimeout" class="form-select">
                <option value="15">{{ translate('systemSettings.security.timeout15min') }}</option>
                <option value="30">{{ translate('systemSettings.security.timeout30min') }}</option>
                <option value="60">{{ translate('systemSettings.security.timeout1hour') }}</option>
                <option value="0">{{ translate('systemSettings.security.timeoutNever') }}</option>
              </select>
            </div>
            <div class="form-group">
              <label>{{ translate('systemSettings.security.passwordPolicy') }}</label>
              <div class="password-policy">
                <div class="policy-item" :class="{ valid: passwordPolicy.minLength }">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="20,6 9,17 4,12"></polyline>
                  </svg>
                  {{ translate('systemSettings.security.minLength') }}
                </div>
                <div class="policy-item" :class="{ valid: passwordPolicy.hasUppercase }">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="20,6 9,17 4,12"></polyline>
                  </svg>
                  {{ translate('systemSettings.security.hasUppercase') }}
                </div>
                <div class="policy-item" :class="{ valid: passwordPolicy.hasNumbers }">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="20,6 9,17 4,12"></polyline>
                  </svg>
                  {{ translate('systemSettings.security.hasNumbers') }}
                </div>
              </div>
            </div>
      </div>
    </div>

        <!-- System Settings -->
        <div class="settings-card">
          <div class="card-header">
            <div class="card-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect>
                <line x1="8" y1="21" x2="16" y2="21"></line>
                <line x1="12" y1="17" x2="12" y2="21"></line>
              </svg>
            </div>
          <div class="card-title">
            <h3>{{ translate('systemSettings.system.title') }}</h3>
            <p>{{ translate('systemSettings.system.subtitle') }}</p>
          </div>
          </div>
          <div class="card-content">
            <div class="form-group">
              <label class="toggle-label">
                <span>Sao Lưu Tự Động</span>
                <div class="toggle-switch">
                  <input type="checkbox" v-model="settings.system.autoBackup">
                  <span class="toggle-slider"></span>
                </div>
              </label>
            </div>
            <div class="form-group">
              <label>Tần Suất Sao Lưu</label>
              <select v-model="settings.system.backupFrequency" class="form-select" :disabled="!settings.system.autoBackup">
                <option value="daily">Hàng ngày</option>
                <option value="weekly">Hàng tuần</option>
                <option value="monthly">Hàng tháng</option>
              </select>
            </div>
            <div class="form-group">
              <label>Mức Độ Ghi Log</label>
              <select v-model="settings.system.logLevel" class="form-select">
                <option value="error">Lỗi</option>
                <option value="warn">Cảnh báo</option>
                <option value="info">Thông tin</option>
                <option value="debug">Debug</option>
              </select>
            </div>
            <div class="form-group">
              <label class="toggle-label">
                <span>Chế Độ Bảo Trì</span>
                <div class="toggle-switch">
                  <input type="checkbox" v-model="settings.system.maintenanceMode">
              <span class="toggle-slider"></span>
                </div>
            </label>
            </div>
          </div>
          </div>
        </div>
      </div>

    <!-- Toast Notification -->
    <div v-if="toast.show" class="toast" :class="toast.type">
      <div class="toast-content">
        <svg v-if="toast.type === 'success'" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="20,6 9,17 4,12"></polyline>
        </svg>
        <svg v-else-if="toast.type === 'error'" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10"></circle>
          <line x1="15" y1="9" x2="9" y2="15"></line>
          <line x1="9" y1="9" x2="15" y2="15"></line>
        </svg>
        <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10"></circle>
          <line x1="12" y1="16" x2="12" y2="12"></line>
          <line x1="12" y1="8" x2="12.01" y2="8"></line>
        </svg>
        <span>{{ toast.message }}</span>
      </div>
      <button @click="hideToast" class="toast-close">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="18" y1="6" x2="6" y2="18"></line>
          <line x1="6" y1="6" x2="18" y2="18"></line>
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/authStore'
import PosHeader from '@/components/PosHeader.vue'
import { useGlobalTranslation } from '@/composables/useGlobalTranslation.js'

// Auth store
const authStore = useAuthStore()

// Translation
const { t, setLanguage, getCurrentLanguage, initLanguage } = useGlobalTranslation()

// Translation function for settings
const translate = (key) => {
  return t(key)
}

// User info
const userInfo = computed(() => ({
  name: authStore.user?.hoTen || authStore.user?.username || 'Admin User',
  role: authStore.user?.role || 'Quản lý'
}))

// Settings data
const settings = reactive({
      general: {
  storeName: 'PhoniX Store',
  address: '123 Đường ABC, Quận XYZ, TP.HCM',
  phone: '0123456789',
  email: 'admin@phonix.com',
    timezone: 'Asia/Ho_Chi_Minh'
  },
  appearance: {
  language: 'vi',
  darkMode: false,
    theme: 'blue',
    fontSize: 'medium'
  },
  notifications: {
    email: true,
    newOrders: true,
    sound: true,
    frequency: 'immediate'
  },
  security: {
    twoFactor: false,
    sessionTimeout: '30'
  },
  system: {
    autoBackup: true,
    backupFrequency: 'daily',
    logLevel: 'info',
    maintenanceMode: false
  }
})

// Theme options
const themes = ref([
  { value: 'blue', name: 'Xanh dương', color: '#3b82f6' },
  { value: 'green', name: 'Xanh lá', color: '#10b981' },
  { value: 'purple', name: 'Tím', color: '#8b5cf6' },
  { value: 'orange', name: 'Cam', color: '#f59e0b' },
  { value: 'red', name: 'Đỏ', color: '#ef4444' }
])

// Font size options
const fontSizes = ref([
  { value: 'small', label: 'Nhỏ' },
  { value: 'medium', label: 'Vừa' },
  { value: 'large', label: 'Lớn' },
  { value: 'xlarge', label: 'Rất lớn' }
])

// Password policy
const passwordPolicy = computed(() => ({
  minLength: true,
  hasUppercase: true,
  hasNumbers: true
}))

// Toast notification
const toast = reactive({
  show: false,
  type: 'info',
  message: ''
})

// Methods
const showToast = (type, message) => {
  toast.type = type
  toast.message = message
  toast.show = true
  setTimeout(() => {
    toast.show = false
  }, 3000)
}

const hideToast = () => {
  toast.show = false
}

const changeLanguage = () => {
  setLanguage(settings.appearance.language)
  localStorage.setItem('app-language', settings.appearance.language)
  showToast('success', 'Đã thay đổi ngôn ngữ thành công!')
}

const toggleDarkMode = () => {
  // Dark mode disabled - always remove dark class to prevent automatic dark mode
  document.documentElement.classList.remove('dark')
  localStorage.setItem('darkMode', false)
  settings.appearance.darkMode = false
}

const saveAllSettings = () => {
  try {
    localStorage.setItem('app-settings', JSON.stringify(settings))
    showToast('success', 'Đã lưu cài đặt thành công!')
  } catch (error) {
    showToast('error', 'Lỗi khi lưu cài đặt!')
  }
}

const resetToDefault = () => {
  Object.assign(settings.general, {
      storeName: 'PhoniX Store',
      address: '123 Đường ABC, Quận XYZ, TP.HCM',
      phone: '0123456789',
      email: 'admin@phonix.com',
    timezone: 'Asia/Ho_Chi_Minh'
  })
  
  Object.assign(settings.appearance, {
      language: 'vi',
      darkMode: false,
    theme: 'blue',
    fontSize: 'medium'
  })
  
  Object.assign(settings.notifications, {
    email: true,
    newOrders: true,
    sound: true,
    frequency: 'immediate'
  })
  
  Object.assign(settings.security, {
    twoFactor: false,
    sessionTimeout: '30'
  })
  
  Object.assign(settings.system, {
    autoBackup: true,
    backupFrequency: 'daily',
    logLevel: 'info',
    maintenanceMode: false
  })
  
  showToast('info', 'Đã khôi phục cài đặt mặc định!')
}

const exportSettings = () => {
  try {
    const settingsData = {
      ...settings,
      exportDate: new Date().toISOString(),
      version: '1.0.0'
    }
    
    const dataStr = JSON.stringify(settingsData, null, 2)
    const dataBlob = new Blob([dataStr], { type: 'application/json' })
    const url = URL.createObjectURL(dataBlob)
    
    const link = document.createElement('a')
    link.href = url
    link.download = `phonix-settings-${new Date().toISOString().split('T')[0]}.json`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    
    showToast('success', 'Đã xuất cài đặt thành công!')
  } catch (error) {
    showToast('error', 'Không thể xuất cài đặt!')
  }
}

// Load settings on mount
onMounted(() => {
  // Initialize translation system
  initLanguage()
  
  const savedSettings = localStorage.getItem('app-settings')
  if (savedSettings) {
    try {
      const parsed = JSON.parse(savedSettings)
      Object.assign(settings, parsed)
    } catch (error) {
      console.error('Error loading settings:', error)
    }
  }
  
  // Load language from localStorage and sync with settings
  const savedLanguage = localStorage.getItem('app-language')
  if (savedLanguage) {
    settings.appearance.language = savedLanguage
    setLanguage(savedLanguage)
  } else {
    // If no saved language, set default and sync
    settings.appearance.language = getCurrentLanguage()
  }
  
  // Dark mode disabled - always remove dark class to prevent automatic dark mode
  document.documentElement.classList.remove('dark')
  settings.appearance.darkMode = false
})
</script>

<style scoped>
.settings-page {
  min-height: 100vh;
  background: #f8fafc;
  padding: 0;
}

/* Main Container */
.settings-container {
  max-width: 100%;
  width: 100%;
  margin: 0 auto;
  padding: 32px 40px;
}

/* Quick Actions */
.quick-actions {
  display: flex;
  gap: 16px;
  margin-bottom: 32px;
  justify-content: center;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: 500;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
}

.action-btn.primary {
  background: #3b82f6;
  color: white;
}

.action-btn.primary:hover {
  background: #2563eb;
  transform: translateY(-1px);
}

.action-btn.secondary {
  background: #6b7280;
  color: white;
}

.action-btn.secondary:hover {
  background: #4b5563;
  transform: translateY(-1px);
}

.action-btn.outline {
  background: transparent;
  color: #3b82f6;
  border: 1px solid #3b82f6;
}

.action-btn.outline:hover {
  background: #3b82f6;
  color: white;
}

/* Settings Grid */
.settings-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 24px;
}

.settings-card {
  background: white;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: all 0.2s ease;
}

.settings-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.card-header {
  background: #f8fafc;
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  gap: 16px;
}

.card-icon {
  width: 48px;
  height: 48px;
  background: #3b82f6;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.card-title h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 4px 0;
}

.card-title p {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.card-content {
  padding: 24px;
}

/* Form Elements */
.form-group {
  margin-bottom: 20px;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

label {
  display: block;
  font-weight: 500;
  color: #374151;
  font-size: 14px;
  margin-bottom: 8px;
}

.form-input,
.form-select,
.form-textarea {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.2s ease;
  background: white;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

/* Toggle Switch */
.toggle-label {
  display: flex !important;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
}

.toggle-switch {
  position: relative;
  width: 48px;
  height: 24px;
}

.toggle-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  transition: 0.3s;
  border-radius: 24px;
}

.toggle-slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: 0.3s;
  border-radius: 50%;
}

input:checked + .toggle-slider {
  background-color: #3b82f6;
}

input:checked + .toggle-slider:before {
  transform: translateX(24px);
}

/* Theme Options */
.theme-options {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(80px, 1fr));
  gap: 12px;
}

.theme-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 12px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.theme-option:hover {
  border-color: #3b82f6;
}

.theme-option.active {
  border-color: #3b82f6;
  background: rgba(59, 130, 246, 0.1);
}

.theme-preview {
  width: 32px;
  height: 20px;
  border-radius: 4px;
  border: 1px solid #d1d5db;
}

.theme-option span {
  font-size: 12px;
  color: #374151;
  font-weight: 500;
}

/* Font Size Options */
.font-size-options {
  display: flex;
  gap: 8px;
}

.font-size-btn {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: white;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 12px;
}

.font-size-btn:hover {
  border-color: #3b82f6;
}

.font-size-btn.active {
  background: #3b82f6;
  color: white;
  border-color: #3b82f6;
}

/* Password Policy */
.password-policy {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.policy-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #6b7280;
}

.policy-item.valid {
  color: #10b981;
}

/* Toast */
.toast {
  position: fixed;
  top: 24px;
  right: 24px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border: 1px solid #e2e8f0;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  z-index: 1000;
  animation: slideIn 0.3s ease;
}

.toast.success {
  border-left: 4px solid #10b981;
}

.toast.error {
  border-left: 4px solid #ef4444;
}

.toast.info {
  border-left: 4px solid #3b82f6;
}

.toast-content {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.toast-close {
  background: none;
  border: none;
  cursor: pointer;
  color: #6b7280;
  padding: 4px;
  border-radius: 4px;
}

.toast-close:hover {
  background: #f1f5f9;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

/* Dark Mode */
.dark .settings-page {
  background: #0f172a;
}

.dark .settings-card {
  background: #1e293b;
  border-color: #334155;
}

.dark .card-header {
  background: #334155;
  border-color: #475569;
}

.dark .card-title h3 {
  color: #f1f5f9;
}

.dark .card-title p {
  color: #94a3b8;
}

.dark .form-input,
.dark .form-select,
.dark .form-textarea {
  background: #334155;
  border-color: #475569;
  color: #f1f5f9;
}

.dark label {
  color: #e2e8f0;
}

.dark .theme-option {
  border-color: #475569;
}

.dark .theme-option span {
  color: #e2e8f0;
}

.dark .font-size-btn {
  background: #334155;
  border-color: #475569;
  color: #e2e8f0;
}

.dark .toast {
  background: #1e293b;
  border-color: #334155;
}

/* Responsive */
@media (max-width: 768px) {
  .settings-container {
    padding: 16px;
  }
  
  .settings-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .quick-actions {
    flex-direction: column;
  }
  
  .action-btn {
    width: 100%;
    justify-content: center;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .theme-options {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .page-header {
    padding: 16px;
  }
}

@media (max-width: 480px) {
  .theme-options {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .card-content {
    padding: 16px;
  }
  
  .card-header {
    padding: 16px;
  }
}
</style>
