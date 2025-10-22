<template>
  <div class="page" :class="{ 'dark': isDark }">
    <div class="header">
      <h1>{{ tSettings('systemSettings.title') }}</h1>
    </div>

    <div class="settings-container">
      <div class="settings-section">
        <h2>{{ tSettings('systemSettings.general.title') }}</h2>
        <div class="setting-item">
          <label>{{ tSettings('systemSettings.general.storeName') }}:</label>
          <input type="text" v-model="settings.storeName" />
        </div>
        <div class="setting-item">
          <label>{{ tSettings('systemSettings.general.address') }}:</label>
          <input type="text" v-model="settings.address" />
        </div>
        <div class="setting-item">
          <label>{{ tSettings('systemSettings.general.phone') }}:</label>
          <input type="text" v-model="settings.phone" />
        </div>
        <div class="setting-item">
          <label>{{ tSettings('systemSettings.general.email') }}:</label>
          <input type="email" v-model="settings.email" />
        </div>
      </div>

      <div class="settings-section">
        <h2>{{ tSettings('systemSettings.interface.title') }}</h2>
        <div class="setting-item">
          <label>{{ tSettings('systemSettings.interface.darkMode') }}:</label>
          <div class="toggle-container">
            <input 
              type="checkbox" 
              :id="'darkModeToggle'"
              v-model="settings.darkMode" 
              @change="toggleDarkMode"
              class="toggle-input"
            />
            <label :for="'darkModeToggle'" class="toggle-label">
              <span class="toggle-slider"></span>
            </label>
          </div>
        </div>
        <div class="setting-item">
          <label>{{ tSettings('systemSettings.interface.language') }}:</label>
          <select v-model="settings.language" @change="changeLanguage">
            <option value="vi">Tiếng Việt</option>
            <option value="en">English</option>
          </select>
        </div>
      </div>

      <div class="settings-section">
        <h2>{{ tSettings('systemSettings.notifications.title') }}</h2>
        <div class="setting-item">
          <label>{{ tSettings('systemSettings.notifications.email') }}:</label>
          <div class="toggle-container">
            <input 
              type="checkbox" 
              :id="'emailNotificationsToggle'"
              v-model="settings.emailNotifications" 
              class="toggle-input"
            />
            <label :for="'emailNotificationsToggle'" class="toggle-label">
              <span class="toggle-slider"></span>
            </label>
          </div>
        </div>
        <div class="setting-item">
          <label>{{ tSettings('systemSettings.notifications.newOrders') }}:</label>
          <div class="toggle-container">
            <input 
              type="checkbox" 
              :id="'orderNotificationsToggle'"
              v-model="settings.orderNotifications" 
              class="toggle-input"
            />
            <label :for="'orderNotificationsToggle'" class="toggle-label">
              <span class="toggle-slider"></span>
            </label>
          </div>
        </div>
      </div>

      <div class="settings-actions">
        <button class="btn-primary" @click="saveSettings">{{ tSettings('systemSettings.actions.save') }}</button>
        <button class="btn-secondary" @click="resetSettings">{{ tSettings('systemSettings.actions.reset') }}</button>
      </div>
    </div>

    <Toast ref="toastRef" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import Toast from '@/components/Toast.vue'
import { useDarkMode } from '@/composables/useDarkMode.js'
import { useTranslation } from '@/composables/useTranslation.js'

const toastRef = ref<InstanceType<typeof Toast> | null>(null)
const { isDark, toggleDarkMode: toggleDarkModeComposable } = useDarkMode()
const { t, setLanguage, getCurrentLanguage, initLanguage } = useTranslation()

// Translation system for settings specific content
const settingsTranslations = {
  vi: {
    systemSettings: {
      title: 'Cài Đặt Hệ Thống',
      general: {
        title: 'Cài Đặt Chung',
        storeName: 'Tên Cửa Hàng',
        address: 'Địa Chỉ',
        phone: 'Số Điện Thoại',
        email: 'Email'
      },
      interface: {
        title: 'Cài Đặt Giao Diện',
        darkMode: 'Chế Độ Tối',
        language: 'Ngôn Ngữ'
      },
      notifications: {
        title: 'Cài Đặt Thông Báo',
        email: 'Thông Báo Email',
        newOrders: 'Thông Báo Đơn Hàng Mới'
      },
      actions: {
        save: 'Lưu Cài Đặt',
        reset: 'Khôi Phục Mặc Định'
      }
    }
  },
  en: {
    systemSettings: {
      title: 'System Settings',
      general: {
        title: 'General Settings',
        storeName: 'Store Name',
        address: 'Address',
        phone: 'Phone Number',
        email: 'Email'
      },
      interface: {
        title: 'Interface Settings',
        darkMode: 'Dark Mode',
        language: 'Language'
      },
      notifications: {
        title: 'Notification Settings',
        email: 'Email Notifications',
        newOrders: 'New Order Notifications'
      },
      actions: {
        save: 'Save Settings',
        reset: 'Restore Default'
      }
    }
  }
}

const settings = ref({
  storeName: 'PhoniX Store',
  address: '123 Đường ABC, Quận XYZ, TP.HCM',
  phone: '0123456789',
  email: 'admin@phonix.com',
  darkMode: false,
  language: 'vi',
  emailNotifications: true,
  orderNotifications: true
})

// Translation function for settings
const tSettings = (key: string) => {
  const keys = key.split('.')
  let value = settingsTranslations[getCurrentLanguage.value as keyof typeof settingsTranslations]
  for (const k of keys) {
    value = value?.[k as keyof typeof value]
  }
  return value || key
}

// Dark mode toggle
const toggleDarkMode = () => {
  settings.value.darkMode = !settings.value.darkMode
  // Update the global dark mode state
  isDark.value = settings.value.darkMode
  toggleDarkModeComposable()
}

// Language change
const changeLanguage = () => {
  setLanguage(settings.value.language)
  console.log('Language changed to:', settings.value.language)
}

function saveSettings() {
  try {
    // Save settings to localStorage
    localStorage.setItem('app-settings', JSON.stringify(settings.value))
    localStorage.setItem('darkMode', JSON.stringify(settings.value.darkMode))
    localStorage.setItem('app-language', settings.value.language)
    
    // Sync with global dark mode state
    isDark.value = settings.value.darkMode
    
    // Apply dark mode immediately
    if (settings.value.darkMode) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
    
    // Show success message based on language
    const message = settings.value.language === 'en' 
      ? 'Settings saved successfully!' 
      : 'Đã lưu cài đặt thành công!'
    
    toastRef.value?.success('Thành công', message)
  } catch (error) {
    console.error('Error saving settings:', error)
    const errorMessage = settings.value.language === 'en'
      ? 'Failed to save settings!'
      : 'Lỗi khi lưu cài đặt!'
    toastRef.value?.error('Lỗi', errorMessage)
  }
}

function resetSettings() {
  try {
    settings.value = {
      storeName: 'PhoniX Store',
      address: '123 Đường ABC, Quận XYZ, TP.HCM',
      phone: '0123456789',
      email: 'admin@phonix.com',
      darkMode: false,
      language: 'vi',
      emailNotifications: true,
      orderNotifications: true
    }
    
    // Reset dark mode
    isDark.value = false
    document.documentElement.classList.remove('dark')
    localStorage.setItem('darkMode', 'false')
    localStorage.setItem('app-language', 'vi')
    localStorage.setItem('app-settings', JSON.stringify(settings.value))
    
    // Show reset message based on language
    const message = settings.value.language === 'en'
      ? 'Settings restored to default!'
      : 'Đã khôi phục cài đặt mặc định'
    
    toastRef.value?.info('Thông báo', message)
  } catch (error) {
    console.error('Error resetting settings:', error)
    const errorMessage = settings.value.language === 'en'
      ? 'Failed to reset settings!'
      : 'Lỗi khi khôi phục cài đặt!'
    toastRef.value?.error('Lỗi', errorMessage)
  }
}

onMounted(() => {
  // Initialize global translation system
  initLanguage()
  
  // Load settings from localStorage
  const savedSettings = localStorage.getItem('app-settings')
  const savedLanguage = localStorage.getItem('app-language')
  const savedDarkMode = localStorage.getItem('darkMode')
  
  if (savedSettings) {
    settings.value = { ...settings.value, ...JSON.parse(savedSettings) }
  }
  
  if (savedLanguage) {
    settings.value.language = savedLanguage
    setLanguage(savedLanguage)
  }
  
  if (savedDarkMode) {
    settings.value.darkMode = JSON.parse(savedDarkMode)
    // Sync with global dark mode state
    isDark.value = settings.value.darkMode
  }
  
  // Initialize dark mode
  if (settings.value.darkMode) {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
})
</script>

<style scoped>
.page {
  padding: 20px;
  background: var(--bg-primary);
  min-height: 100vh;
  transition: background-color 0.3s ease;
}

.page.dark {
  background: var(--bg-primary);
}

.header {
  margin-bottom: 30px;
}

.header h1 {
  color: var(--text-primary);
  font-size: 2rem;
  font-weight: 600;
}

.settings-container {
  max-width: 800px;
}

.settings-section {
  background: var(--card-bg);
  padding: 24px;
  margin-bottom: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 6px var(--shadow-light);
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
}

.settings-section h2 {
  margin-bottom: 24px;
  color: var(--text-primary);
  border-bottom: 2px solid #007bff;
  padding-bottom: 12px;
  font-size: 1.25rem;
  font-weight: 600;
}

.setting-item {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  gap: 16px;
}

.setting-item label {
  flex: 0 0 200px;
  font-weight: 500;
  color: var(--text-primary);
  font-size: 0.95rem;
}

.setting-item input,
.setting-item select {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid var(--input-border);
  border-radius: 8px;
  max-width: 300px;
  background: var(--input-bg);
  color: var(--text-primary);
  font-size: 0.95rem;
  transition: all 0.3s ease;
}

.setting-item input:focus,
.setting-item select:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
}

/* Toggle Switch Styles */
.toggle-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.toggle-input {
  display: none;
}

.toggle-label {
  position: relative;
  display: inline-block;
  width: 64px;
  height: 36px;
  cursor: pointer;
  border-radius: 18px;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.1);
}

.toggle-slider {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #e5e7eb, #d1d5db);
  border-radius: 18px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid transparent;
}

.toggle-slider:before {
  position: absolute;
  content: "";
  height: 28px;
  width: 28px;
  left: 2px;
  top: 2px;
  background: linear-gradient(135deg, #ffffff, #f9fafb);
  border-radius: 50%;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2), 0 1px 2px rgba(0, 0, 0, 0.1);
}

.toggle-input:checked + .toggle-label .toggle-slider {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  border-color: #1d4ed8;
  box-shadow: inset 0 2px 4px rgba(59, 130, 246, 0.3);
}

.toggle-input:checked + .toggle-label .toggle-slider:before {
  transform: translateX(28px);
  background: linear-gradient(135deg, #ffffff, #f8fafc);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15), 0 2px 4px rgba(0, 0, 0, 0.1);
}

.toggle-input:focus + .toggle-label .toggle-slider {
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

/* Dark mode toggle styles */
.page.dark .toggle-slider {
  background: linear-gradient(135deg, #4b5563, #374151);
}

.page.dark .toggle-input:checked + .toggle-label .toggle-slider {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
}

.page.dark .toggle-slider:before {
  background: linear-gradient(135deg, #f3f4f6, #e5e7eb);
}

.settings-actions {
  display: flex;
  gap: 16px;
  margin-top: 32px;
}

.btn-primary {
  background: linear-gradient(135deg, #007bff, #0056b3);
  color: white;
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  font-size: 0.95rem;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 123, 255, 0.2);
}

.btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 123, 255, 0.3);
}

.btn-secondary {
  background: linear-gradient(135deg, #6c757d, #545b62);
  color: white;
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  font-size: 0.95rem;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(108, 117, 125, 0.2);
}

.btn-secondary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(108, 117, 125, 0.3);
}

/* Dark mode specific styles */
.page.dark .settings-section {
  background: var(--card-bg);
  border-color: var(--border-color);
}

.page.dark .setting-item input,
.page.dark .setting-item select {
  background: var(--input-bg);
  border-color: var(--input-border);
  color: var(--text-primary);
}

@media (max-width: 768px) {
  .page {
    padding: 16px;
  }
  
  .setting-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .setting-item label {
    flex: none;
    margin-bottom: 0;
  }
  
  .setting-item input,
  .setting-item select {
    max-width: 100%;
    width: 100%;
  }
  
  .settings-actions {
    flex-direction: column;
  }
  
  .btn-primary,
  .btn-secondary {
    width: 100%;
  }
}
</style>

