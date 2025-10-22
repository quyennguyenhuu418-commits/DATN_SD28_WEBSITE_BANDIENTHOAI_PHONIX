// Polyfill for global object in browser environment
if (typeof global === 'undefined') {
  if (typeof window !== 'undefined') {
    // Browser environment
    window.global = window
  } else if (typeof globalThis !== 'undefined') {
    // Modern browsers with globalThis
    global = globalThis
  } else {
    // Fallback
    global = this
  }
}

// Also ensure process is available for some libraries
if (typeof process === 'undefined') {
  if (typeof window !== 'undefined') {
    window.process = { env: {} }
  } else {
    global.process = { env: {} }
  }
}


