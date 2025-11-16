<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Header -->
    <div class="bg-gradient-to-r from-orange-500 to-orange-600 text-white shadow-lg">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center h-16">
          <div class="flex items-center space-x-4">
            <div class="flex-shrink-0">
              <img class="h-8 w-8" src="/logo.png" alt="Logo" />
            </div>
            <div>
              <h1 class="text-xl font-bold">Chat hỗ trợ</h1>
              <p class="text-sm text-orange-100">Hỗ trợ</p>
            </div>
          </div>
          <div class="flex items-center space-x-4">
            <div class="flex items-center space-x-2">
              <div class="w-2 h-2 bg-green-400 rounded-full animate-pulse"></div>
              <span class="text-sm font-medium">Trực tuyến</span>
            </div>
            <button
              @click="toggleChatWidget"
              class="bg-white bg-opacity-20 hover:bg-opacity-30 rounded-lg px-4 py-2 transition-all duration-200 flex items-center space-x-2"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"></path>
              </svg>
              <span>{{ isChatWidgetVisible ? 'Ẩn chat' : 'Hiện chat' }}</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <!-- Left Content -->
        <div class="space-y-8">
          <div class="bg-white rounded-2xl shadow-xl p-8">
            <h2 class="text-3xl font-bold text-gray-800 mb-4">Chào mừng đến với hỗ trợ khách hàng</h2>
            <p class="text-gray-600 text-lg leading-relaxed">
              Chúng tôi luôn sẵn sàng hỗ trợ bạn 24/7. Hãy liên hệ với chúng tôi qua chat trực tuyến 
              để được tư vấn và giải đáp mọi thắc mắc.
            </p>
          </div>

          <div class="bg-white rounded-2xl shadow-xl p-8">
            <h3 class="text-2xl font-bold text-gray-800 mb-6">Các cách liên hệ</h3>
            <div class="space-y-4">
              <div class="flex items-center space-x-4 p-4 bg-orange-50 rounded-xl">
                <div class="w-12 h-12 bg-orange-500 rounded-full flex items-center justify-center">
                  <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"></path>
                  </svg>
                </div>
                <div>
                  <h4 class="font-semibold text-gray-800">Chat trực tuyến</h4>
                  <p class="text-gray-600">Hỗ trợ nhanh chóng và tiện lợi</p>
                </div>
              </div>

              <div class="flex items-center space-x-4 p-4 bg-green-50 rounded-xl">
                <div class="w-12 h-12 bg-green-500 rounded-full flex items-center justify-center">
                  <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"></path>
                  </svg>
                </div>
                <div>
                  <h4 class="font-semibold text-gray-800">Hotline: 1900-xxxx</h4>
                  <p class="text-gray-600">Hỗ trợ qua điện thoại</p>
                </div>
              </div>

              <div class="flex items-center space-x-4 p-4 bg-blue-50 rounded-xl">
                <div class="w-12 h-12 bg-blue-500 rounded-full flex items-center justify-center">
                  <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 4.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path>
                  </svg>
                </div>
                <div>
                  <h4 class="font-semibold text-gray-800">Email: support@example.com</h4>
                  <p class="text-gray-600">Hỗ trợ qua email</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Right Content -->
        <div class="space-y-8">
          <div class="bg-white rounded-2xl shadow-xl p-8">
            <h3 class="text-2xl font-bold text-gray-800 mb-6">Thông tin liên hệ</h3>
            <div class="space-y-4">
              <div class="flex items-center space-x-3">
                <svg class="w-5 h-5 text-orange-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"></path>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path>
                </svg>
                <span class="text-gray-700">123 Đường ABC, Quận XYZ, TP.HCM</span>
              </div>
              <div class="flex items-center space-x-3">
                <svg class="w-5 h-5 text-orange-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                </svg>
                <span class="text-gray-700">Thứ 2 - Chủ nhật: 8:00 - 22:00</span>
              </div>
              <div class="flex items-center space-x-3">
                <svg class="w-5 h-5 text-orange-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"></path>
                </svg>
                <span class="text-gray-700">Hotline: 1900-xxxx</span>
              </div>
            </div>
          </div>

          <div class="bg-gradient-to-r from-orange-500 to-orange-600 rounded-2xl shadow-xl p-8 text-white">
            <h3 class="text-2xl font-bold mb-4">Bắt đầu chat ngay</h3>
            <p class="text-orange-100 mb-6">
              Click vào nút "Hiện chat" ở trên để bắt đầu cuộc trò chuyện với nhân viên hỗ trợ của chúng tôi.
            </p>
            <button
              @click="toggleChatWidget"
              class="bg-white text-orange-600 px-6 py-3 rounded-lg font-semibold hover:bg-orange-50 transition-all duration-200 shadow-lg hover:shadow-xl transform hover:scale-105"
            >
              Bắt đầu chat
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Draggable Chat Widget -->
    <DraggableChatWidget v-if="isChatWidgetVisible" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import DraggableChatWidget from '@/components/DraggableChatWidget.vue'

const isChatWidgetVisible = ref(false)

const toggleChatWidget = () => {
  isChatWidgetVisible.value = !isChatWidgetVisible.value
}
</script>
