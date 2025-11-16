import { vTranslate, vTranslateReactive } from './useTranslateDirective.js'

/**
 * Vue plugin for auto-translation
 * This plugin automatically registers translation directives
 * 
 * Usage in main.js:
 * import { createApp } from 'vue'
 * import App from './App.vue'
 * import { translatePlugin } from './composables/translatePlugin.js'
 * 
 * const app = createApp(App)
 * app.use(translatePlugin)
 * app.mount('#app')
 */
export const translatePlugin = {
  install(app) {
    // Register directives globally
    app.directive('translate', vTranslate)
    app.directive('translate-reactive', vTranslateReactive)
    
    // Add global properties for easy access
    app.config.globalProperties.$translate = (text) => {
      const { autoTranslate } = useGlobalTranslation()
      return autoTranslate(text)
    }
  }
}

export default translatePlugin








