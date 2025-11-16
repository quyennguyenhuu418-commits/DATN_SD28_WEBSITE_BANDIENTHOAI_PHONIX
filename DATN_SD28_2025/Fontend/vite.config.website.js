import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import fs from 'fs'
import path from 'path'

// Plugin to serve index-website.html as index.html for dev server
const websiteHtmlPlugin = () => {
  return {
    name: 'website-html-plugin',
    configureServer(server) {
      return () => {
        server.middlewares.use((req, res, next) => {
          // Redirect root and /index.html to index-website.html
          if (req.url === '/' || req.url === '/index.html') {
            const websiteHtmlPath = path.resolve(process.cwd(), 'index-website.html')
            if (fs.existsSync(websiteHtmlPath)) {
              req.url = '/index-website.html'
              console.log('✅ Website HTML Plugin: Redirecting to index-website.html')
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
    websiteHtmlPlugin(),
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
        main: fileURLToPath(new URL('./index-website.html', import.meta.url)),
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
    port: 5174,
    open: false, // Don't auto-open, user will navigate manually
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

