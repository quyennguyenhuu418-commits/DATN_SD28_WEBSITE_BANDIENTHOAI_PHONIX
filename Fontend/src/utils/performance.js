// Performance monitoring utilities
export class PerformanceMonitor {
  constructor() {
    this.metrics = new Map()
    this.observers = []
  }

  // Measure function execution time
  measure(name, fn) {
    const start = performance.now()
    const result = fn()
    const end = performance.now()
    
    this.metrics.set(name, {
      duration: end - start,
      timestamp: Date.now()
    })
    
    if (import.meta.env.DEV) {
      console.log(`⏱️ ${name}: ${(end - start).toFixed(2)}ms`)
    }
    
    return result
  }

  // Measure async function execution time
  async measureAsync(name, fn) {
    const start = performance.now()
    const result = await fn()
    const end = performance.now()
    
    this.metrics.set(name, {
      duration: end - start,
      timestamp: Date.now()
    })
    
    if (import.meta.env.DEV) {
      console.log(`⏱️ ${name}: ${(end - start).toFixed(2)}ms`)
    }
    
    return result
  }

  // Monitor component render time
  monitorComponent(componentName) {
    return {
      mounted() {
        this._renderStart = performance.now()
      },
      updated() {
        const renderTime = performance.now() - this._renderStart
        if (import.meta.env.DEV) {
          console.log(`🔄 ${componentName} render: ${renderTime.toFixed(2)}ms`)
        }
      }
    }
  }

  // Get performance metrics
  getMetrics() {
    return Object.fromEntries(this.metrics)
  }

  // Clear metrics
  clearMetrics() {
    this.metrics.clear()
  }

  // Monitor memory usage
  getMemoryUsage() {
    if (performance.memory) {
      return {
        used: Math.round(performance.memory.usedJSHeapSize / 1024 / 1024),
        total: Math.round(performance.memory.totalJSHeapSize / 1024 / 1024),
        limit: Math.round(performance.memory.jsHeapSizeLimit / 1024 / 1024)
      }
    }
    return null
  }

  // Monitor network performance
  monitorNetwork() {
    if ('connection' in navigator) {
      const connection = navigator.connection
      return {
        effectiveType: connection.effectiveType,
        downlink: connection.downlink,
        rtt: connection.rtt,
        saveData: connection.saveData
      }
    }
    return null
  }
}

// Global performance monitor instance
export const perfMonitor = new PerformanceMonitor()

// Vue directive for performance monitoring
export const vPerf = {
  mounted(el, binding) {
    const componentName = binding.value || 'Unknown'
    el._perfStart = performance.now()
    
    if (import.meta.env.DEV) {
      console.log(`🚀 ${componentName} mounted`)
    }
  },
  updated(el, binding) {
    const componentName = binding.value || 'Unknown'
    const renderTime = performance.now() - el._perfStart
    
    if (import.meta.env.DEV) {
      console.log(`🔄 ${componentName} updated: ${renderTime.toFixed(2)}ms`)
    }
  }
}

// Utility functions
export const debounce = (func, wait) => {
  let timeout
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout)
      func(...args)
    }
    clearTimeout(timeout)
    timeout = setTimeout(later, wait)
  }
}

export const throttle = (func, limit) => {
  let inThrottle
  return function(...args) {
    if (!inThrottle) {
      func.apply(this, args)
      inThrottle = true
      setTimeout(() => inThrottle = false, limit)
    }
  }
}

// Image lazy loading utility
export const lazyLoadImage = (img, src) => {
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        img.src = src
        observer.unobserve(img)
      }
    })
  })
  
  observer.observe(img)
  return observer
}
