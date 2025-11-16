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
  padding: 28px 36px;
  background: linear-gradient(135deg, #f97316 0%, #ea580c 50%, #dc2626 100%);
  color: white;
  position: relative;
  box-shadow: 0 4px 20px rgba(249, 115, 22, 0.2);
}

.modal-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.1) 0%, transparent 100%);
  pointer-events: none;
}

.modal-header h3 {
  margin: 0;
  color: white;
  font-size: 22px;
  font-weight: 800;
  letter-spacing: -0.3px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 10px;
}

.modal-header h3::before {
  content: '⚠';
  font-size: 24px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
  margin-right: 8px;
}

.close-btn {
  background: rgba(255, 255, 255, 0.15);
  border: 2px solid rgba(255, 255, 255, 0.3);
  font-size: 28px;
  color: white;
  cursor: pointer;
  padding: 0;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(10px);
  font-weight: 300;
  line-height: 1;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 0.5);
  transform: scale(1.1) rotate(90deg);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.close-btn:active {
  transform: scale(0.95) rotate(90deg);
}

.modal-body {
  padding: 36px;
  background: white;
  position: relative;
}

.modal-body::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, transparent, #f97316, transparent);
}

.modal-body p {
  margin: 0;
  color: #1e293b;
  line-height: 1.75;
  font-size: 16px;
  font-weight: 500;
  text-align: left;
  letter-spacing: -0.2px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 14px;
  padding: 24px 36px;
  background: linear-gradient(to bottom, #f8fafc, #ffffff);
  border-top: 1px solid #e2e8f0;
  position: relative;
  z-index: 10;
  flex-shrink: 0;
}

.modal-footer::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, #e2e8f0, transparent);
}

.btn {
  padding: 14px 28px;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 700;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  min-width: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  letter-spacing: 0.3px;
  text-transform: uppercase;
  font-size: 13px;
  position: relative;
  overflow: hidden;
}

.btn::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

.btn:hover::before {
  width: 300px;
  height: 300px;
}

.btn-cancel {
  background: linear-gradient(135deg, #f1f5f9, #e2e8f0);
  color: #475569;
  border: 2px solid #cbd5e1;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.btn-cancel:hover {
  background: linear-gradient(135deg, #e2e8f0, #cbd5e1);
  color: #334155;
  border-color: #94a3b8;
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.btn-cancel:active {
  transform: translateY(0);
}

.btn-confirm {
  background: linear-gradient(135deg, #f97316 0%, #ea580c 50%, #dc2626 100%);
  color: white;
  box-shadow: 0 6px 20px rgba(249, 115, 22, 0.35);
  position: relative;
  z-index: 11;
  min-width: 140px;
  border: none;
  font-weight: 800;
}

.btn-confirm:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 30px rgba(249, 115, 22, 0.5);
  background: linear-gradient(135deg, #ea580c 0%, #dc2626 50%, #b91c1c 100%);
}

.btn-confirm:active {
  transform: translateY(-1px);
  box-shadow: 0 4px 15px rgba(249, 115, 22, 0.4);
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
