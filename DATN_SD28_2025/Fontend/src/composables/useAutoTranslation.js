import { useGlobalTranslation } from './useGlobalTranslation.js'

/**
 * Auto-translation composable
 * Sử dụng để tự động dịch text mà không cần định nghĩa key
 * 
 * Usage:
 * const { translate } = useAutoTranslation()
 * 
 * In template:
 * {{ translate('Trang chủ') }} // -> 'Home' (if language is English)
 * {{ translate('Sản phẩm') }} // -> 'Products' (if language is English)
 * {{ translate('Khách hàng') }} // -> 'Customers' (if language is English)
 */
export function useAutoTranslation() {
  const { autoTranslate, getCurrentLanguage } = useGlobalTranslation()
  
  /**
   * Auto-translate Vietnamese text to current language
   * @param {string} text - Vietnamese text to translate
   * @returns {string} - Translated text or original text if no translation found
   */
  const translate = (text) => {
    if (!text || typeof text !== 'string') return text
    return autoTranslate(text)
  }
  
  /**
   * Translate multiple texts at once
   * @param {string[]} texts - Array of Vietnamese texts
   * @returns {string[]} - Array of translated texts
   */
  const translateMultiple = (texts) => {
    if (!Array.isArray(texts)) return texts
    return texts.map(text => translate(text))
  }
  
  /**
   * Translate object values
   * @param {Object} obj - Object with Vietnamese values
   * @returns {Object} - Object with translated values
   */
  const translateObject = (obj) => {
    if (!obj || typeof obj !== 'object') return obj
    
    const translated = {}
    for (const [key, value] of Object.entries(obj)) {
      if (typeof value === 'string') {
        translated[key] = translate(value)
      } else {
        translated[key] = value
      }
    }
    return translated
  }
  
  /**
   * Get current language
   * @returns {string} - Current language code
   */
  const getCurrentLang = () => getCurrentLanguage()
  
  return {
    translate,
    translateMultiple,
    translateObject,
    getCurrentLang
  }
}

export default useAutoTranslation








