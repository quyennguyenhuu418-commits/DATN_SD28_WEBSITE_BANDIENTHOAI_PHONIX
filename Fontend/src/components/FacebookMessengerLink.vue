<template>
  <div class="facebook-messenger-link">
    <a 
      :href="messengerUrl" 
      target="_blank" 
      rel="noopener noreferrer"
      class="messenger-link"
      title="Chat với chúng tôi trên Messenger"
    >
      <i class="fab fa-facebook-messenger"></i>
      <span v-if="showText">Messenger</span>
    </a>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  pageId: {
    type: String,
    default: '796591066879645'
  },
  showText: {
    type: Boolean,
    default: false
  }
})

const messengerUrl = computed(() => {
  return `https://m.me/${props.pageId}`
})
</script>

<style scoped>
.facebook-messenger-link {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 9999;
}

.messenger-link {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #0084ff;
  color: white;
  text-decoration: none;
  border-radius: 50px;
  font-weight: 600;
  font-size: 14px;
  box-shadow: 0 4px 12px rgba(0, 132, 255, 0.3);
  transition: all 0.3s ease;
  min-width: 60px;
  justify-content: center;
}

.messenger-link:hover {
  background: #0066cc;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 132, 255, 0.4);
  color: white;
  text-decoration: none;
}

.messenger-link i {
  font-size: 20px;
}

/* Mobile responsive */
@media (max-width: 768px) {
  .facebook-messenger-link {
    bottom: 10px;
    right: 10px;
  }
  
  .messenger-link {
    padding: 10px 14px;
    font-size: 13px;
  }
  
  .messenger-link i {
    font-size: 18px;
  }
}
</style>







