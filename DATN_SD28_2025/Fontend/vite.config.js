import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  define: {
    global: 'globalThis',
  },
  build: {
    // Tối ưu build
    target: 'es2015',
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true,
      },
    },
    rollupOptions: {
      output: {
        manualChunks: {
          // Tách vendor chunks
          vendor: ['vue', 'vue-router', 'pinia'],
          // Tách UI components
          ui: ['@fortawesome/fontawesome-free'],
        },
      },
    },
    // Tối ưu chunk size
    chunkSizeWarningLimit: 1000,
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/ws': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: true,
      },
    },
  },
  // Tối ưu dev server
  optimizeDeps: {
    include: ['vue', 'vue-router', 'pinia', '@fortawesome/fontawesome-free'],
  },
})
