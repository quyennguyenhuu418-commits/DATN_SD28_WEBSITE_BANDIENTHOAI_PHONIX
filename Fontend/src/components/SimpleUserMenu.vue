<template>
  <div class="simple-user-menu" v-if="authStore.isAuthenticated">
    <div class="user-info" @click="viewProfile">
      <div class="user-avatar">
        <i class="fas fa-user-circle"></i>
      </div>
      <div class="user-details">
        <div class="user-name">{{ authStore.userName }}</div>
        <div class="user-role">{{ getRoleDisplayName(authStore.userRole) }}</div>
      </div>
    </div>
  </div>
  
  <div v-else class="login-prompt">
    <router-link to="/login" class="login-link">
      <i class="fas fa-sign-in-alt"></i>
      Đăng nhập
    </router-link>
  </div>
</template>

<script setup>
import { useAuthStore } from '@/stores/authStore'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

const getRoleDisplayName = (role) => {
  const roleMap = {
    'ADMIN': 'Quản trị viên',
    'MANAGER': 'Quản lý',
    'STAFF': 'Nhân viên',
    'GUEST': 'Khách'
  }
  return roleMap[role] || role
}

const viewProfile = () => {
  router.push('/account')
}
</script>

<style scoped>
.simple-user-menu {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.25rem;
  cursor: pointer;
  transition: all 0.2s ease;
  border-radius: 4px;
}

.user-info:hover {
  background: rgba(0, 0, 0, 0.05);
}

.user-avatar {
  font-size: 1.5rem;
  color: #667eea;
}

.user-details {
  display: flex;
  flex-direction: column;
  min-width: 100px;
}

.user-name {
  font-weight: 600;
  color: #333;
  font-size: 0.85rem;
  margin-bottom: 1px;
}

.user-role {
  font-size: 0.75rem;
  color: #666;
}

.login-prompt {
  display: flex;
  align-items: center;
}

.login-link {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: #667eea;
  color: white;
  text-decoration: none;
  border-radius: 6px;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.login-link:hover {
  background: #5a67d8;
  transform: translateY(-1px);
  color: white;
}
</style>
