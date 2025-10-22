<template>
  <div class="simple-test">
    <h1>Simple Test Page</h1>
    <p>Nếu bạn thấy trang này, Vue đã hoạt động!</p>
    <button @click="testClick">Test Button</button>
    <p>{{ message }}</p>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const message = ref('Hello World!')

function testClick() {
  message.value = 'Button clicked!'
  console.log('Test function called')
}
</script>

<style scoped>
.simple-test {
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



























