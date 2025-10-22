<template>
  <div v-if="show" class="modal">
    <div class="modal-content">
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
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

interface FormField {
  key: string
  label: string
  type: 'text' | 'textarea' | 'checkbox' | 'number' | 'select' | 'date'
  required?: boolean
  placeholder?: string
  rows?: number
  options?: { value: string; label: string }[]
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
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 20px;
  border-radius: 8px;
  width: 400px;
  max-width: 90vw;
  max-height: 70vh;
  overflow-y: auto;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

.form-group input,
.form-group textarea,
.form-group select {
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

.form-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

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
