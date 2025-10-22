<template>
  <div class="test-page">
    <h1>Test Page</h1>
    <p>Nếu bạn thấy trang này, Vue đã hoạt động!</p>
    <button @click="testFunction">Test Button</button>
    <p>{{ message }}</p>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const message = ref('Hello World!')

function testFunction() {
  message.value = 'Button clicked!'
  console.log('Test function called')
}
</script>

<style scoped>
.test-page {
  padding: 20px;
  text-align: center;
}

button {
  padding: 10px 20px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}
</style>



























