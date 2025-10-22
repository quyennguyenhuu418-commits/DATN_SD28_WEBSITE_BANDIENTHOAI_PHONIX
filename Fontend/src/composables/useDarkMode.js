import { ref, watch, onMounted } from 'vue'

const isDark = ref(false)

export function useDarkMode() {
  // Khởi tạo dark mode từ localStorage hoặc system preference
  const initDarkMode = () => {
    const stored = localStorage.getItem('darkMode')
    if (stored !== null) {
      isDark.value = JSON.parse(stored)
    } else {
      // Sử dụng system preference nếu chưa có setting
      isDark.value = window.matchMedia('(prefers-color-scheme: dark)').matches
    }

    // Apply dark mode class
    updateDarkModeClass()
  }

  // Cập nhật class cho document
  const updateDarkModeClass = () => {
    if (isDark.value) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }

  // Toggle dark mode
  const toggleDarkMode = () => {
    isDark.value = !isDark.value
  }

  // Watch để lưu vào localStorage và update class
  watch(isDark, (newValue) => {
    localStorage.setItem('darkMode', JSON.stringify(newValue))
    updateDarkModeClass()
  })

  // Listen for system theme changes
  const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
  const handleSystemThemeChange = (e) => {
    // Chỉ update nếu user chưa set manual preference
    const stored = localStorage.getItem('darkMode')
    if (stored === null) {
      isDark.value = e.matches
    }
  }

  onMounted(() => {
    initDarkMode()
    mediaQuery.addEventListener('change', handleSystemThemeChange)
  })

  return {
    isDark,
    toggleDarkMode,
    initDarkMode
  }
}







