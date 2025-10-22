<template>
  <div class="auth-debug-page">
    <div class="container">
      <h1>Authentication Debug</h1>
      
      <div class="debug-section">
        <h2>Frontend Auth State</h2>
        <div class="debug-info">
          <p><strong>Is Authenticated:</strong> {{ authStore.isAuthenticated }}</p>
          <p><strong>Token:</strong> {{ authStore.token ? 'Present' : 'Missing' }}</p>
          <p><strong>User:</strong> {{ JSON.stringify(authStore.user, null, 2) }}</p>
          <p><strong>Loading:</strong> {{ authStore.loading }}</p>
          <p><strong>Error:</strong> {{ authStore.error }}</p>
        </div>
      </div>

      <div class="debug-section">
        <h2>Backend Auth Status</h2>
        <button @click="checkBackendAuth" :disabled="loading" class="btn">
          {{ loading ? 'Checking...' : 'Check Backend Auth' }}
        </button>
        <div v-if="backendResponse" class="debug-info">
          <pre>{{ JSON.stringify(backendResponse, null, 2) }}</pre>
        </div>
      </div>

      <div class="debug-section">
        <h2>Protected Endpoint Test</h2>
        <button @click="testProtectedEndpoint" :disabled="loading" class="btn">
          {{ loading ? 'Testing...' : 'Test Protected Endpoint' }}
        </button>
        <div v-if="protectedResponse" class="debug-info">
          <pre>{{ JSON.stringify(protectedResponse, null, 2) }}</pre>
        </div>
      </div>

      <div class="debug-section">
        <h2>Database Users</h2>
        <button @click="getAllUsers" :disabled="loading" class="btn">
          {{ loading ? 'Loading...' : 'Get All Users' }}
        </button>
        <button @click="getUserByUsername" :disabled="loading" class="btn">
          {{ loading ? 'Loading...' : 'Get User: admin@phonestore.com' }}
        </button>
        <div v-if="usersResponse" class="debug-info">
          <pre>{{ JSON.stringify(usersResponse, null, 2) }}</pre>
        </div>
      </div>

      <div class="debug-section">
        <h2>Actions</h2>
        <button @click="loginTest" class="btn btn-primary">Test Login (admin@phonestore.com/123456)</button>
        <button @click="loginTest2" class="btn btn-primary">Test Login (admin/123456)</button>
        <button @click="logout" class="btn btn-danger">Logout</button>
        <button @click="refreshToken" class="btn btn-secondary">Refresh Token</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useAuthStore } from '@/stores/authStore'
import api from '@/services/api'

const authStore = useAuthStore()
const loading = ref(false)
const backendResponse = ref(null)
const protectedResponse = ref(null)
const usersResponse = ref(null)

const checkBackendAuth = async () => {
  loading.value = true
  try {
    const response = await api.get('/api/test/auth/status')
    backendResponse.value = response.data
  } catch (error) {
    backendResponse.value = {
      error: error.message,
      status: error.response?.status,
      data: error.response?.data
    }
  } finally {
    loading.value = false
  }
}

const testProtectedEndpoint = async () => {
  loading.value = true
  try {
    const response = await api.get('/api/test/auth/protected')
    protectedResponse.value = response.data
  } catch (error) {
    protectedResponse.value = {
      error: error.message,
      status: error.response?.status,
      data: error.response?.data
    }
  } finally {
    loading.value = false
  }
}

const loginTest = async () => {
  try {
    const result = await authStore.login({
      username: 'admin@phonestore.com',
      password: '123456'
    })
    console.log('Login result:', result)
  } catch (error) {
    console.error('Login error:', error)
  }
}

const loginTest2 = async () => {
  try {
    const result = await authStore.login({
      username: 'admin',
      password: '123456'
    })
    console.log('Login result:', result)
  } catch (error) {
    console.error('Login error:', error)
  }
}

const logout = async () => {
  await authStore.logout()
}

const refreshToken = async () => {
  try {
    const result = await authStore.refreshToken()
    console.log('Refresh result:', result)
  } catch (error) {
    console.error('Refresh error:', error)
  }
}

const getAllUsers = async () => {
  loading.value = true
  try {
    const response = await api.get('/api/test/auth/users')
    usersResponse.value = response.data
  } catch (error) {
    usersResponse.value = {
      error: error.message,
      status: error.response?.status,
      data: error.response?.data
    }
  } finally {
    loading.value = false
  }
}

const getUserByUsername = async () => {
  loading.value = true
  try {
    const response = await api.get('/api/test/auth/user/admin@phonestore.com')
    usersResponse.value = response.data
  } catch (error) {
    usersResponse.value = {
      error: error.message,
      status: error.response?.status,
      data: error.response?.data
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-debug-page {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.container {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.debug-section {
  margin-bottom: 2rem;
  padding: 1rem;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
}

.debug-section h2 {
  margin-top: 0;
  color: #374151;
}

.debug-info {
  background: #f9fafb;
  padding: 1rem;
  border-radius: 4px;
  margin-top: 1rem;
  font-family: 'Courier New', monospace;
  font-size: 0.875rem;
  overflow-x: auto;
}

.btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 0.5rem;
  margin-bottom: 0.5rem;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background: #3b82f6;
  color: white;
}

.btn-danger {
  background: #ef4444;
  color: white;
}

.btn-secondary {
  background: #6b7280;
  color: white;
}

pre {
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>
