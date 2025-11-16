<template>
  <div class="messenger-settings-page">
    <div class="page-header">
      <h1 class="page-title">
        <i class="fab fa-facebook-messenger"></i>
        Cài đặt Facebook Messenger
      </h1>
      <p class="page-description">
        Quản lý tích hợp Facebook Messenger cho hệ thống CSKH
      </p>
    </div>

    <div class="settings-container">
      <!-- Messenger Status -->
      <div class="setting-card">
        <div class="setting-header">
          <h3>Trạng thái Messenger</h3>
          <div class="status-indicator" :class="{ 'active': isMessengerEnabled }">
            {{ isMessengerEnabled ? 'Đang bật' : 'Đã tắt' }}
          </div>
        </div>
        <p class="setting-description">
          Bật/tắt widget Facebook Messenger trên trang web
        </p>
        <button 
          @click="toggleMessenger"
          class="toggle-btn"
          :class="{ 'active': isMessengerEnabled }"
        >
          <i class="fas fa-power-off"></i>
          {{ isMessengerEnabled ? 'Tắt Messenger' : 'Bật Messenger' }}
        </button>
      </div>

      <!-- Page ID Settings -->
      <div class="setting-card">
        <div class="setting-header">
          <h3>Cấu hình Page ID</h3>
        </div>
        <div class="setting-content">
          <label class="input-label">Facebook Page ID</label>
          <input 
            v-model="pageId"
            type="text"
            class="input-field"
            placeholder="Nhập Page ID của bạn"
            @input="updatePageId"
          >
          <p class="input-help">
            Page ID hiện tại: <strong>{{ pageId }}</strong><br>
            Link Messenger: <a :href="`https://m.me/${pageId}`" target="_blank" class="messenger-link">
              m.me/{{ pageId }}
            </a>
          </p>
        </div>
      </div>

      <!-- Position Settings -->
      <div class="setting-card">
        <div class="setting-header">
          <h3>Vị trí hiển thị</h3>
        </div>
        <div class="setting-content">
          <div class="position-options">
            <label class="position-option" :class="{ 'selected': position === 'bottom-right' }">
              <input 
                type="radio" 
                value="bottom-right" 
                v-model="position"
                @change="updatePosition"
              >
              <div class="position-preview">
                <i class="fas fa-circle"></i>
                <span>Dưới phải</span>
              </div>
            </label>
            
            <label class="position-option" :class="{ 'selected': position === 'bottom-left' }">
              <input 
                type="radio" 
                value="bottom-left" 
                v-model="position"
                @change="updatePosition"
              >
              <div class="position-preview">
                <i class="fas fa-circle"></i>
                <span>Dưới trái</span>
              </div>
            </label>
            
            <label class="position-option" :class="{ 'selected': position === 'top-right' }">
              <input 
                type="radio" 
                value="top-right" 
                v-model="position"
                @change="updatePosition"
              >
              <div class="position-preview">
                <i class="fas fa-circle"></i>
                <span>Trên phải</span>
              </div>
            </label>
            
            <label class="position-option" :class="{ 'selected': position === 'top-left' }">
              <input 
                type="radio" 
                value="top-left" 
                v-model="position"
                @change="updatePosition"
              >
              <div class="position-preview">
                <i class="fas fa-circle"></i>
                <span>Trên trái</span>
              </div>
            </label>
          </div>
        </div>
      </div>

      <!-- Preview -->
      <div class="setting-card">
        <div class="setting-header">
          <h3>Xem trước</h3>
        </div>
        <div class="setting-content">
          <div class="preview-container">
            <div class="preview-mockup">
              <div class="mockup-header">Trang web của bạn</div>
              <div class="mockup-content">
                <div class="mockup-widget" :class="`position-${position}`">
                  <i class="fab fa-facebook-messenger"></i>
                </div>
              </div>
            </div>
            <p class="preview-note">
              Widget Messenger sẽ xuất hiện ở vị trí đã chọn
            </p>
          </div>
        </div>
      </div>

      <!-- Actions -->
      <div class="setting-card">
        <div class="setting-header">
          <h3>Hành động</h3>
        </div>
        <div class="setting-actions">
          <button @click="testMessenger" class="action-btn test-btn">
            <i class="fas fa-external-link-alt"></i>
            Test Messenger
          </button>
          <button @click="resetSettings" class="action-btn reset-btn">
            <i class="fas fa-undo"></i>
            Reset cài đặt
          </button>
          <button @click="saveSettings" class="action-btn save-btn">
            <i class="fas fa-save"></i>
            Lưu cài đặt
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const pageId = ref('796591066879645')
const isMessengerEnabled = ref(true)
const position = ref('bottom-right')

const toggleMessenger = () => {
  isMessengerEnabled.value = !isMessengerEnabled.value
  saveSettings()
}

const updatePageId = () => {
  saveSettings()
}

const updatePosition = () => {
  saveSettings()
}

const testMessenger = () => {
  window.open(`https://m.me/${pageId.value}`, '_blank', 'noopener,noreferrer')
}

const resetSettings = () => {
  pageId.value = '796591066879645'
  isMessengerEnabled.value = true
  position.value = 'bottom-right'
  saveSettings()
}

const saveSettings = () => {
  const settings = {
    pageId: pageId.value,
    enabled: isMessengerEnabled.value,
    position: position.value
  }
  localStorage.setItem('messenger-settings', JSON.stringify(settings))
  
  // Emit event to update MessengerManager
  window.dispatchEvent(new CustomEvent('messenger-settings-updated', { 
    detail: settings 
  }))
}

const loadSettings = () => {
  const saved = localStorage.getItem('messenger-settings')
  if (saved) {
    try {
      const settings = JSON.parse(saved)
      pageId.value = settings.pageId || '796591066879645'
      isMessengerEnabled.value = settings.enabled ?? true
      position.value = settings.position || 'bottom-right'
    } catch (error) {
      console.warn('Failed to load messenger settings:', error)
    }
  }
}

onMounted(() => {
  loadSettings()
})
</script>

<style scoped>
.messenger-settings-page {
  padding: 2rem;
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 2rem;
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.page-title i {
  color: #0084ff;
}

.page-description {
  color: #6b7280;
  font-size: 1.1rem;
}

.settings-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.setting-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
}

.setting-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.setting-header h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.status-indicator {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 500;
  background: #f3f4f6;
  color: #6b7280;
}

.status-indicator.active {
  background: #dcfce7;
  color: #166534;
}

.setting-description {
  color: #6b7280;
  margin-bottom: 1rem;
}

.toggle-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  background: #f3f4f6;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-weight: 500;
}

.toggle-btn:hover {
  background: #e5e7eb;
}

.toggle-btn.active {
  background: #0084ff;
  color: white;
  border-color: #0084ff;
}

.input-label {
  display: block;
  font-weight: 500;
  color: #374151;
  margin-bottom: 0.5rem;
}

.input-field {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.2s ease;
}

.input-field:focus {
  outline: none;
  border-color: #0084ff;
  box-shadow: 0 0 0 3px rgba(0, 132, 255, 0.1);
}

.input-help {
  margin-top: 0.5rem;
  font-size: 0.875rem;
  color: #6b7280;
}

.messenger-link {
  color: #0084ff;
  text-decoration: none;
}

.messenger-link:hover {
  text-decoration: underline;
}

.position-options {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
}

.position-option {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.position-option:hover {
  border-color: #d1d5db;
}

.position-option.selected {
  border-color: #0084ff;
  background: #f0f9ff;
}

.position-option input[type="radio"] {
  margin: 0;
}

.position-preview {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 500;
}

.position-preview i {
  color: #0084ff;
}

.preview-container {
  text-align: center;
}

.preview-mockup {
  position: relative;
  width: 300px;
  height: 200px;
  background: #f9fafb;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  margin: 0 auto 1rem;
  overflow: hidden;
}

.mockup-header {
  background: #e5e7eb;
  padding: 0.5rem;
  font-size: 0.875rem;
  font-weight: 500;
  color: #6b7280;
}

.mockup-content {
  position: relative;
  height: calc(100% - 2rem);
  background: white;
}

.mockup-widget {
  position: absolute;
  width: 40px;
  height: 40px;
  background: #0084ff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
}

.mockup-widget.position-bottom-right {
  bottom: 10px;
  right: 10px;
}

.mockup-widget.position-bottom-left {
  bottom: 10px;
  left: 10px;
}

.mockup-widget.position-top-right {
  top: 10px;
  right: 10px;
}

.mockup-widget.position-top-left {
  top: 10px;
  left: 10px;
}

.preview-note {
  color: #6b7280;
  font-size: 0.875rem;
}

.setting-actions {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s ease;
}

.test-btn {
  background: #f0f9ff;
  color: #0369a1;
  border: 1px solid #bae6fd;
}

.test-btn:hover {
  background: #e0f2fe;
}

.reset-btn {
  background: #fef3c7;
  color: #92400e;
  border: 1px solid #fde68a;
}

.reset-btn:hover {
  background: #fde68a;
}

.save-btn {
  background: #dcfce7;
  color: #166534;
  border: 1px solid #bbf7d0;
}

.save-btn:hover {
  background: #bbf7d0;
}

/* Mobile responsive */
@media (max-width: 768px) {
  .messenger-settings-page {
    padding: 1rem;
  }
  
  .position-options {
    grid-template-columns: 1fr;
  }
  
  .setting-actions {
    flex-direction: column;
  }
  
  .action-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>







