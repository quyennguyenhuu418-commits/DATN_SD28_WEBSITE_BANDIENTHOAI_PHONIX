<template>
  <div v-if="show" class="modal">
    <div class="modal-content">
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
          </div>
        </form>
      </div>
      <div class="modal-footer">
        <button type="button" @click="handleCancel" class="btn-secondary">Đóng</button>
        <button type="button" @click="handleSubmit" class="btn-primary">Xác nhận</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

interface FormField {
  key: string
  label: string
  type: 'text' | 'textarea' | 'checkbox' | 'number'
  required?: boolean
  placeholder?: string
  rows?: number
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
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
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

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

.form-group input,
.form-group textarea {
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
