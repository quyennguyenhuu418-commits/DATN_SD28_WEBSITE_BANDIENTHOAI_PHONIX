<template>
  <Teleport to="body">
    <div v-if="show" class="modal-overlay" @click="handleOverlayClick">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ title }}</h3>
          <button class="close-btn" @click="cancel">&times;</button>
        </div>
        <div class="modal-body">
          <p>{{ message }}</p>
        </div>
        <div class="modal-footer">
          <button class="btn btn-cancel" @click="cancel">Hủy</button>
          <button class="btn btn-confirm" @click="confirm">Xác nhận</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

interface Props {
  show: boolean
  title?: string
  message?: string
}

interface Emits {
  (e: 'confirm'): void
  (e: 'cancel'): void
}

const props = withDefaults(defineProps<Props>(), {
  title: 'Xác nhận',
  message: 'Bạn có chắc chắn muốn thực hiện hành động này?'
})

const emit = defineEmits<Emits>()

function confirm() {
  emit('confirm')
}

function cancel() {
  emit('cancel')
}

function handleOverlayClick() {
  cancel()
}
</script>

<style scoped>
.modal-overlay {
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  right: 0 !important;
  bottom: 0 !important;
  width: 100vw !important;
  height: 100vh !important;
  background: rgba(0, 0, 0, 0.6) !important;
  display: flex !important;
  justify-content: center !important;
  align-items: center !important;
  z-index: 999999 !important;
  backdrop-filter: blur(4px) !important;
  margin: 0 !important;
  padding: 0 !important;
}

.modal-content {
  background: white !important;
  border-radius: 20px !important;
  width: 400px !important;
  max-width: 90vw !important;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25) !important;
  animation: modalSlideIn 0.3s ease-out !important;
  overflow: hidden !important;
  position: relative !important;
  z-index: 1000000 !important;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-50px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 32px;
  background: linear-gradient(135deg, #f97316, #ea580c);
  color: white;
  position: relative;
}

.modal-header h3 {
  margin: 0;
  color: white;
  font-size: 20px;
  font-weight: 700;
}

.close-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  font-size: 24px;
  color: white;
  cursor: pointer;
  padding: 0;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
  backdrop-filter: blur(10px);
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

.modal-body {
  padding: 32px;
}

.modal-body p {
  margin: 0;
  color: #374151;
  line-height: 1.6;
  font-size: 16px;
  font-weight: 500;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 24px 32px;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
  position: relative;
  z-index: 10;
  flex-shrink: 0;
}

.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
  min-width: 100px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-cancel {
  background: #f3f4f6;
  color: #6b7280;
  border: 2px solid #e5e7eb;
}

.btn-cancel:hover {
  background: #e5e7eb;
  color: #374151;
  transform: translateY(-1px);
}

.btn-confirm {
  background: linear-gradient(135deg, #f97316, #ea580c);
  color: white;
  box-shadow: 0 4px 15px rgba(249, 115, 22, 0.3);
  position: relative;
  z-index: 11;
  min-width: 120px;
}

.btn-confirm:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(249, 115, 22, 0.4);
}

.btn-confirm:active {
  transform: translateY(0);
}

/* Đảm bảo tất cả element con có z-index cao */
.modal-overlay * {
  z-index: 1000001 !important;
}

/* Reset tất cả z-index conflicts */
body:has(.modal-overlay) {
  overflow: hidden !important;
}

/* Đảm bảo modal không bị ảnh hưởng bởi parent containers */
.modal-overlay {
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  right: 0 !important;
  bottom: 0 !important;
  width: 100vw !important;
  height: 100vh !important;
  z-index: 999999 !important;
  margin: 0 !important;
  padding: 0 !important;
}

/* Global CSS để đảm bảo ConfirmModal luôn hiển thị trên cùng */
:global(.modal-overlay) {
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  right: 0 !important;
  bottom: 0 !important;
  width: 100vw !important;
  height: 100vh !important;
  z-index: 999999 !important;
  margin: 0 !important;
  padding: 0 !important;
}

:global(.modal-overlay *) {
  z-index: 1000000 !important;
}
</style>
