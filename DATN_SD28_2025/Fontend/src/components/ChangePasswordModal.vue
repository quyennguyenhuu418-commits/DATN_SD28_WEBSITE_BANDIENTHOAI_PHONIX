<template>
  <div v-if="isOpen" class="modal-overlay" @click="closeModal">
    <!-- Debug info -->
    <div style="position: fixed; top: 50px; left: 10px; background: green; color: white; padding: 10px; z-index: 10001;">
      Modal isOpen: {{ isOpen }}
    </div>
    <div class="modal-container" @click.stop>
      <div class="modal-header">
        <h3 class="modal-title">Đổi mật khẩu</h3>
        <button class="close-btn" @click="closeModal">
          <i class="fas fa-times"></i>
        </button>
      </div>
      
      <div class="modal-body">
        <form @submit.prevent="handleSubmit">
          <div class="form-group">
            <label for="currentPassword">Mật khẩu hiện tại</label>
            <input
              id="currentPassword"
              v-model="formData.currentPassword"
              type="password"
              class="form-input"
              placeholder="Nhập mật khẩu hiện tại"
              required
            />
          </div>
          
          <div class="form-group">
            <label for="newPassword">Mật khẩu mới</label>
            <input
              id="newPassword"
              v-model="formData.newPassword"
              type="password"
              class="form-input"
              placeholder="Nhập mật khẩu mới"
              required
              minlength="6"
            />
          </div>
          
          <div class="form-group">
            <label for="confirmPassword">Xác nhận mật khẩu mới</label>
            <input
              id="confirmPassword"
              v-model="formData.confirmPassword"
              type="password"
              class="form-input"
              placeholder="Nhập lại mật khẩu mới"
              required
              minlength="6"
            />
          </div>
          
          <div v-if="error" class="error-message">
            {{ error }}
          </div>
          
          <div v-if="success" class="success-message">
            {{ success }}
          </div>
        </form>
      </div>
      
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" @click="closeModal">
          Hủy
        </button>
        <button 
          type="submit" 
          class="btn btn-primary" 
          @click="handleSubmit"
          :disabled="loading"
        >
          <i v-if="loading" class="fas fa-spinner fa-spin"></i>
          {{ loading ? 'Đang xử lý...' : 'Đổi mật khẩu' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useAuthStore } from '@/stores/authStore'

export default {
  name: 'ChangePasswordModal',
  props: {
    isOpen: {
      type: Boolean,
      default: false
    }
  },
  emits: ['close'],
  setup(props, { emit }) {
    const authStore = useAuthStore()
    const loading = ref(false)
    const error = ref('')
    const success = ref('')
    
    const formData = reactive({
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
    
    const closeModal = () => {
      // Reset form
      formData.currentPassword = ''
      formData.newPassword = ''
      formData.confirmPassword = ''
      error.value = ''
      success.value = ''
      emit('close')
    }
    
    const handleSubmit = async () => {
      error.value = ''
      success.value = ''
      
      // Validate form
      if (formData.newPassword !== formData.confirmPassword) {
        error.value = 'Mật khẩu mới và xác nhận mật khẩu không khớp'
        return
      }
      
      if (formData.newPassword.length < 6) {
        error.value = 'Mật khẩu mới phải có ít nhất 6 ký tự'
        return
      }
      
      if (formData.currentPassword === formData.newPassword) {
        error.value = 'Mật khẩu mới phải khác mật khẩu hiện tại'
        return
      }
      
      loading.value = true
      
      try {
        await authStore.changePassword({
          currentPassword: formData.currentPassword,
          newPassword: formData.newPassword,
          confirmPassword: formData.confirmPassword
        })
        
        success.value = 'Đổi mật khẩu thành công!'
        
        // Close modal after 2 seconds
        setTimeout(() => {
          closeModal()
        }, 2000)
        
      } catch (err) {
        console.error('Change password error:', err)
        error.value = err.response?.data?.error || 'Có lỗi xảy ra khi đổi mật khẩu'
      } finally {
        loading.value = false
      }
    }
    
    return {
      formData,
      loading,
      error,
      success,
      closeModal,
      handleSubmit
    }
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
}

.modal-container {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.5rem;
  border-bottom: 1px solid #e5e7eb;
}

.modal-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.25rem;
  color: #6b7280;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.close-btn:hover {
  background: #f3f4f6;
  color: #374151;
}

.modal-body {
  padding: 1.5rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
  margin-bottom: 0.5rem;
}

.form-input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.875rem;
  transition: border-color 0.2s ease;
}

.form-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.error-message {
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #dc2626;
  padding: 0.75rem;
  border-radius: 6px;
  font-size: 0.875rem;
  margin-bottom: 1rem;
}

.success-message {
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  color: #16a34a;
  padding: 0.75rem;
  border-radius: 6px;
  font-size: 0.875rem;
  margin-bottom: 1rem;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  padding: 1.5rem;
  border-top: 1px solid #e5e7eb;
  background: #f9fafb;
}

.btn {
  padding: 0.75rem 1.5rem;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  background: #f3f4f6;
  color: #374151;
}

.btn-secondary:hover:not(:disabled) {
  background: #e5e7eb;
}

.btn-primary {
  background: #3b82f6;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #2563eb;
}

/* Responsive */
@media (max-width: 640px) {
  .modal-container {
    width: 95%;
    margin: 1rem;
  }
  
  .modal-header,
  .modal-body,
  .modal-footer {
    padding: 1rem;
  }
}
</style>