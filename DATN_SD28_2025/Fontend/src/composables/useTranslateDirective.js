import { useGlobalTranslation } from './useGlobalTranslation.js'

/**
 * Vue directive for auto-translation
 * Usage: v-translate="'Vietnamese text'"
 * 
 * This directive will automatically translate Vietnamese text to the current language
 */
export const vTranslate = {
  mounted(el, binding) {
    const { autoTranslate } = useGlobalTranslation()
    
    // Get the text to translate
    const text = binding.value || el.textContent
    
    if (text && typeof text === 'string') {
      // Translate the text
      const translatedText = autoTranslate(text)
      
      // Update the element's text content
      el.textContent = translatedText
      
      // Store original text for future updates
      el._originalText = text
    }
  },
  
  updated(el, binding) {
    const { autoTranslate } = useGlobalTranslation()
    
    // Get the text to translate
    const text = binding.value || el.textContent
    
    if (text && typeof text === 'string' && text !== el._originalText) {
      // Translate the text
      const translatedText = autoTranslate(text)
      
      // Update the element's text content
      el.textContent = translatedText
      
      // Store original text for future updates
      el._originalText = text
    }
  }
}

/**
 * Vue directive for auto-translation with reactive updates
 * Usage: v-translate-reactive="'Vietnamese text'"
 * 
 * This directive will automatically translate Vietnamese text and update when language changes
 */
export const vTranslateReactive = {
  mounted(el, binding) {
    const { autoTranslate, currentLanguage } = useGlobalTranslation()
    
    // Get the text to translate
    const text = binding.value || el.textContent
    
    if (text && typeof text === 'string') {
      // Store original text
      el._originalText = text
      
      // Translate the text
      const translatedText = autoTranslate(text)
      el.textContent = translatedText
      
      // Watch for language changes
      el._unwatch = currentLanguage.watch(() => {
        const newTranslatedText = autoTranslate(el._originalText)
        el.textContent = newTranslatedText
      })
    }
  },
  
  updated(el, binding) {
    const { autoTranslate } = useGlobalTranslation()
    
    // Get the text to translate
    const text = binding.value || el.textContent
    
    if (text && typeof text === 'string' && text !== el._originalText) {
      // Store original text
      el._originalText = text
      
      // Translate the text
      const translatedText = autoTranslate(text)
      el.textContent = translatedText
    }
  },
  
  unmounted(el) {
    // Clean up watcher
    if (el._unwatch) {
      el._unwatch()
    }
  }
}

export default {
  vTranslate,
  vTranslateReactive
}








