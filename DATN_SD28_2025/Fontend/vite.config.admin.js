import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import fs from 'fs'
import path from 'path'

// Plugin to ensure admin HTML is served
const adminHtmlPlugin = () => {
  return {
    name: 'admin-html-plugin',
    configureServer(server) {
      return () => {
        server.middlewares.use((req, res, next) => {
          // Always serve index-admin.html for root and /index.html
          if (req.url === '/' || req.url === '/index.html') {
            const adminHtmlPath = path.resolve(process.cwd(), 'index-admin.html')
            if (fs.existsSync(adminHtmlPath)) {
              req.url = '/index-admin.html'
              console.log('✅ Admin HTML Plugin: Serving index-admin.html')
            }
          }
          next()
        })
      }
    }
  }
}

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
    adminHtmlPlugin(),
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
    target: 'es2015',
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true,
      },
    },
    rollupOptions: {
      input: {
        main: fileURLToPath(new URL('./index-admin.html', import.meta.url)),
      },
      output: {
        manualChunks: {
          vendor: ['vue', 'vue-router', 'pinia'],
          ui: ['@fortawesome/fontawesome-free'],
        },
      },
    },
    chunkSizeWarningLimit: 1000,
  },
  server: {
    port: 5173,
    open: false, // Don't auto-open
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
  optimizeDeps: {
    include: ['vue', 'vue-router', 'pinia', '@fortawesome/fontawesome-free'],
  },
})

