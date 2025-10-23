<template>
  <div v-if="show" class="modal">
    <div class="modal-content">
<<<<<<< HEAD
      <h2>{{ title }}</h2>
      <form @submit.prevent="handleSubmit">
        <div v-for="field in fields" :key="field.key" class="form-group">
          <label>{{ field.label }}:</label>
          <input
            v-if="field.type === 'text' || field.type === 'number' || field.type === 'date'"
            :type="field.type"
            v-model="formData[field.key]"
            :required="field.required"
            :placeholder="field.placeholder"
          />
          <textarea
            v-else-if="field.type === 'textarea'"
            v-model="formData[field.key]"
            :rows="field.rows || 3"
            :placeholder="field.placeholder"
          />
          <select
            v-else-if="field.type === 'select'"
            v-model="formData[field.key]"
            :required="field.required"
          >
            <option value="">-- Chọn --</option>
            <option 
              v-for="option in field.options" 
              :key="option.value" 
              :value="option.value"
            >
              {{ option.label }}
            </option>
          </select>
          <div v-else-if="field.type === 'checkbox'" class="checkbox-group">
            <label>
              <input
                type="checkbox"
                v-model="formData[field.key]"
                :true-value="1"
                :false-value="0"
              />
              {{ field.label }}
            </label>
          </div>
        </div>
        <div class="form-actions">
          <button type="submit" class="btn-primary">Lưu</button>
          <button type="button" @click="handleCancel" class="btn-secondary">Hủy</button>
        </div>
      </form>
=======
      <div class="modal-header">
        <h3>{{ title }}</h3>
        <button class="modal-close" @click="handleCancel">×</button>
      </div>
      <div class="modal-body">
        <form @submit.prevent="handleSubmit">
          <div v-for="field in fields" :key="field.key" class="form-group">
            <label>{{ field.label }}:</label>
            <input
              v-if="field.type === 'text' || field.type === 'number'"
              :type="field.type"
              v-model="formData[field.key]"
              :required="field.required"
              :placeholder="field.placeholder"
            />
            <textarea
              v-else-if="field.type === 'textarea'"
              v-model="formData[field.key]"
              :rows="field.rows || 3"
              :placeholder="field.placeholder"
            />
            <div v-else-if="field.type === 'checkbox'" class="checkbox-group">
              <label>
                <input
                  type="checkbox"
                  v-model="formData[field.key]"
                  :true-value="1"
                  :false-value="0"
                />
                {{ field.label }}
              </label>
            </div>
            <div v-else-if="field.type === 'rating'" class="rating-group">
              <label>{{ field.label }}: {{ formData[field.key] || 0 }}/5 sao</label>
              <div class="rating-stars">
                <span 
                  v-for="i in 5" 
                  :key="i" 
                  class="star" 
                  :class="{ 'filled': i <= (formData[field.key] || 0) }"
                  @click="formData[field.key] = i"
                >
                  ★
                </span>
              </div>
            </div>
            <div v-else-if="field.type === 'radio'" class="radio-group">
              <label>{{ field.label }}:</label>
              <div class="radio-options">
                <label v-for="option in field.options" :key="option.value" class="radio-option">
                  <input
                    type="radio"
                    :name="field.key"
                    :value="option.value"
                    v-model="formData[field.key]"
                  />
                  <span class="radio-label">{{ option.label }}</span>
                </label>
              </div>
            </div>
          </div>
          
          <!-- Custom fields slot -->
          <slot name="custom-fields"></slot>
        </form>
      </div>
      <div class="modal-footer">
        <button type="button" @click="handleCancel" class="btn-secondary">Đóng</button>
        <button type="button" @click="handleSubmit" class="btn-primary">Xác nhận</button>
      </div>
>>>>>>> origin/Huan
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

interface FormField {
  key: string
  label: string
<<<<<<< HEAD
  type: 'text' | 'textarea' | 'checkbox' | 'number' | 'select' | 'date'
  required?: boolean
  placeholder?: string
  rows?: number
  options?: { value: string; label: string }[]
=======
  type: 'text' | 'textarea' | 'checkbox' | 'number' | 'rating' | 'radio'
  required?: boolean
  placeholder?: string
  rows?: number
  options?: { value: any; label: string }[]
>>>>>>> origin/Huan
}

interface Props {
  show: boolean
  title: string
  fields: FormField[]
  initialData?: Record<string, any>
}

interface Emits {
  (e: 'submit', data: Record<string, any>): void
  (e: 'cancel'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const formData = ref<Record<string, any>>({})

// Khởi tạo form data
const initializeFormData = () => {
  const data: Record<string, any> = {}
  props.fields.forEach(field => {
    if (field.type === 'checkbox') {
      data[field.key] = 1 // Mặc định là checked
    } else {
      data[field.key] = ''
    }
  })
  formData.value = data
}

<<<<<<< HEAD
// Cập nhật form data khi có initialData
watch(() => props.initialData, (newData) => {
  if (newData) {
    formData.value = { ...newData }
  } else {
    initializeFormData()
  }
}, { immediate: true })

// Reset form khi modal đóng
watch(() => props.show, (show) => {
  if (!show) {
    initializeFormData()
  }
})
=======
// Xử lý khi modal mở/đóng
watch(() => props.show, (show) => {
  if (show) {
    // Khi modal mở, cập nhật form data từ initialData
    if (props.initialData) {
      formData.value = { ...props.initialData }
    } else {
      initializeFormData()
    }
  }
}, { immediate: true })

// Cập nhật form data khi initialData thay đổi
watch(() => props.initialData, (newData) => {
  if (newData && props.show) {
    formData.value = { ...newData }
  }
}, { deep: true })
>>>>>>> origin/Huan

const handleSubmit = () => {
  emit('submit', formData.value)
}

const handleCancel = () => {
  emit('cancel')
}
</script>

<style scoped>
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
<<<<<<< HEAD
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
=======
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
>>>>>>> origin/Huan
  z-index: 1000;
}

.modal-content {
  background: white;
<<<<<<< HEAD
  padding: 20px;
  border-radius: 8px;
  width: 400px;
  max-width: 90vw;
  max-height: 70vh;
  overflow-y: auto;
}

=======
  border-radius: 8px;
  width: 500px;
  max-width: 90vw;
  max-height: 90vh;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-body {
  padding: 20px;
  overflow-y: auto;
  flex: 1;
  min-height: 0;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #eee;
  flex-shrink: 0;
}

/* Rating stars styles */
.rating-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.rating-stars {
  display: flex;
  gap: 4px;
  align-items: center;
}

.star {
  font-size: 24px;
  color: #e5e7eb;
  cursor: pointer;
  transition: color 0.2s ease;
  user-select: none;
}

.star.filled {
  color: #ffc107;
}

.star:hover {
  color: #ffc107;
}

.rating-text {
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
}

/* Radio buttons styles */
.radio-group {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.radio-options {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.radio-option {
  position: relative;
  cursor: pointer;
  padding: 16px 20px;
  border: none;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: white;
  min-width: 140px;
  text-align: center;
  overflow: hidden;
}

.radio-option::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #ff6b35, #ff8c42);
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: 0;
}

.radio-option:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255, 107, 53, 0.15);
}

.radio-option:hover::before {
  opacity: 0.05;
}

.radio-option input[type="radio"] {
  display: none;
}

.radio-option:has(input[type="radio"]:checked) {
  background: linear-gradient(135deg, #fef7f0, #fff5f0);
  box-shadow: 0 0 0 3px rgba(255, 107, 53, 0.1), 0 4px 12px rgba(255, 107, 53, 0.2);
  transform: translateY(-1px);
}

.radio-option:has(input[type="radio"]:checked)::before {
  opacity: 0.1;
}

.radio-label {
  position: relative;
  z-index: 1;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.radio-label::before {
  content: '';
  width: 18px;
  height: 18px;
  border: 2px solid #d1d5db;
  border-radius: 50%;
  background: white;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
}

.radio-option:has(input[type="radio"]:checked) .radio-label {
  color: #ff6b35;
  font-weight: 600;
}

.radio-option:has(input[type="radio"]:checked) .radio-label::before {
  border-color: #ff6b35;
  background: #ff6b35;
  transform: scale(1.1);
}


>>>>>>> origin/Huan
.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

.form-group input,
<<<<<<< HEAD
.form-group textarea,
.form-group select {
=======
.form-group textarea {
>>>>>>> origin/Huan
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
}

.checkbox-group label {
  display: flex;
  align-items: center;
  font-weight: normal;
  margin-bottom: 0;
}

.checkbox-group input[type="checkbox"] {
  width: auto;
  margin-right: 8px;
}

<<<<<<< HEAD
.form-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

=======
>>>>>>> origin/Huan
.btn-primary {
  background: #007bff;
  color: white;
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-secondary {
  background: #6c757d;
  color: white;
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
</style>
