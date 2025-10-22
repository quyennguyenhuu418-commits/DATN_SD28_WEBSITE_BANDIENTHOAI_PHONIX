<template>
  <div class="test-auth-page">
    <h1>Test Authentication</h1>
    
    <div class="debug-info">
      <h2>Auth Store State:</h2>
      <p><strong>Token:</strong> {{ authStore.token || 'None' }}</p>
      <p><strong>User:</strong> {{ JSON.stringify(authStore.user, null, 2) }}</p>
      <p><strong>Is Authenticated:</strong> {{ authStore.isAuthenticated }}</p>
      <p><strong>Loading:</strong> {{ authStore.loading }}</p>
      <p><strong>Error:</strong> {{ authStore.error || 'None' }}</p>
    </div>

    <div class="actions">
      <button @click="testLogin" class="btn btn-primary">Test Login</button>
      <button @click="testLogout" class="btn btn-danger">Test Logout</button>
      <button @click="goToDashboard" class="btn btn-secondary">Go to Dashboard</button>
    </div>
  </div>
</template>

<script setup>
import { useAuthStore } from '@/stores/authStore'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

const testLogin = async () => {
  try {
    await authStore.login({
      username: 'admin@phonestore.com',
      password: '123456'
    })
    console.log('Login successful')
  } catch (error) {
    console.error('Login failed:', error)
  }
}

const testLogout = async () => {
  await authStore.logout()
  console.log('Logout successful')
}

const goToDashboard = () => {
  router.push('/dashboard')
}
</script>

<style scoped>
.test-auth-page {
  padding: 2rem;
  max-width: 800px;
  margin: 0 auto;
}

.debug-info {
  background: #f5f5f5;
  padding: 1rem;
  border-radius: 8px;
  margin: 1rem 0;
  font-family: monospace;
}

.actions {
  margin-top: 2rem;
}

.btn {
  padding: 0.5rem 1rem;
  margin: 0.5rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-primary {
  background: #007bff;
  color: white;
}

.btn-danger {
  background: #dc3545;
  color: white;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}
</style>
