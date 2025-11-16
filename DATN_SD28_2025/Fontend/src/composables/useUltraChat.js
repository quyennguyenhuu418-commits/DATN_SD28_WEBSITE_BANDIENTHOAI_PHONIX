import { ref, computed, reactive, onMounted, onUnmounted, nextTick, watch } from 'vue'

// ===== STATE MANAGEMENT =====
const isOpen = ref(false)
const isMinimized = ref(false)
const isFullscreen = ref(false)
const isTyping = ref(false)
const isBotTyping = ref(false)
const isConnected = ref(false)
const connectionStatus = ref('disconnected')
const messages = ref([])
const unreadCount = ref(0)
const inputMessage = ref('')
const suggestions = ref([])
const showSuggestions = ref(false)
const showAttachMenu = ref(false)
const showEmojiPicker = ref(false)
const showSettings = ref(false)
// Keep last search results for quick-compare action
const lastSearchProducts = ref([])

// ===== USER CONTEXT =====
const userContext = reactive({
  name: '',
  email: '',
  phone: '',
  preferences: {},
  history: [],
  sessionId: null
})

// ===== AI CONFIGURATION =====
const ai = reactive({
  model: 'gpt-3.5-turbo',
  temperature: 0.7,
  maxTokens: 500,
  systemPrompt: 'Bạn là một chatbot hỗ trợ khách hàng cho cửa hàng điện thoại PhoniXDB. Hãy trả lời một cách thân thiện, hữu ích và chính xác.',
  isEnabled: true,
  responseDelay: 1000
})

// ===== REALTIME FEATURES =====
const realtime = reactive({
  agentStatus: 'online',
  responseTime: null,
  lastActivity: null,
  typingSpeed: 50
})

// ===== MEDIA HANDLING =====
const media = reactive({
  supportedTypes: ['image/jpeg', 'image/png', 'image/gif', 'application/pdf'],
  maxFileSize: 10 * 1024 * 1024, // 10MB
  uploadProgress: 0
})

// ===== UI SETTINGS =====
const ui = reactive({
  isDarkMode: false,
  compactMode: false,
  soundEnabled: true,
  vibrationEnabled: true,
  animationsEnabled: true,
  fontSize: 'medium'
})

// ===== ANALYTICS =====
const analytics = reactive({
  messagesSent: 0,
  messagesReceived: 0,
  sessionDuration: 0,
  featuresUsed: new Set(),
  errors: []
})

// ===== FEATURES =====
const features = reactive({
  quickReplies: true,
  productSearch: true,
  orderTracking: true,
  comparison: true,
  fileUpload: true,
  voiceMessage: false,
  videoCall: false
})

// ===== COMPUTED PROPERTIES =====
const messageCount = computed(() => messages.value.length)
const hasUnreadMessages = computed(() => unreadCount.value > 0)
const isTypingActive = computed(() => isTyping.value || isBotTyping.value)

// ===== CORE FUNCTIONS =====

// Chat Management
const openChat = () => {
  isOpen.value = true
  isMinimized.value = false
  unreadCount.value = 0
  trackEvent('chat_opened')
  
  // Initialize AI conversation if first time
  if (messages.value.length === 0) {
    addWelcomeMessage()
  }
}

const closeChat = () => {
  isOpen.value = false
  isMinimized.value = false
  isFullscreen.value = false
  trackEvent('chat_closed')
}

const minimizeChat = () => {
  isMinimized.value = true
  trackEvent('chat_minimized')
}

const maximizeChat = () => {
  isMinimized.value = false
  trackEvent('chat_maximized')
}

const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
  trackEvent('chat_fullscreen_toggled', { fullscreen: isFullscreen.value })
}

// Message Management
const addMessage = (message) => {
  const newMessage = {
    id: Date.now() + Math.random(),
    sender: 'user',
    type: 'text',
    content: message,
    timestamp: new Date(),
    ...message
  }
  
  messages.value.push(newMessage)
  analytics.messagesSent++
  trackEvent('message_sent', { type: newMessage.type })
  
  nextTick(() => {
    scrollToBottom()
  })
}

const addBotMessage = (message) => {
  const newMessage = {
    id: Date.now() + Math.random(),
    sender: 'bot',
    type: 'text',
    content: '',
    timestamp: new Date(),
    ...message
  }

  // Normalize comparison message shape for renderer compatibility
  if (newMessage.type === 'comparison') {
    try {
      const content = newMessage.content || {}
      if (!newMessage.products && Array.isArray(content.products)) {
        newMessage.products = content.products
      }
      if (!newMessage.verdict && content.verdict) {
        newMessage.verdict = content.verdict
      }
      if (!newMessage.quickReviews && content.quickReviews) {
        newMessage.quickReviews = content.quickReviews
      }
    } catch (_) {}
  }

  messages.value.push(newMessage)
  analytics.messagesReceived++
  trackEvent('message_received', { type: newMessage.type })
  
  nextTick(() => {
    scrollToBottom()
  })
}

const addWelcomeMessage = () => {
  addBotMessage({
    type: 'text',
    content: '👋 Xin chào! Tôi là PhoniX BOT, trợ lý AI của cửa hàng điện thoại PhoniXDB. Tôi có thể giúp bạn:\n\n🔍 Tìm kiếm sản phẩm\n📊 So sánh điện thoại\n📦 Tra cứu đơn hàng\n💬 Hỗ trợ tư vấn\n\nBạn cần hỗ trợ gì hôm nay?'
  })
  
  // Add quick replies as separate message
  addBotMessage({
    type: 'quick-replies',
    content: '',
    quickReplies: [
      { id: 1, text: 'Tìm điện thoại iPhone' },
      { id: 2, text: 'Tìm điện thoại Samsung' },
      { id: 3, text: 'So sánh điện thoại' },
      { id: 4, text: 'Tra cứu đơn hàng' }
    ]
  })
}

// Send Message with AI Integration
const sendMessage = async () => {
  if (!inputMessage.value.trim()) return
  
  const message = inputMessage.value.trim()
  inputMessage.value = ''
  suggestions.value = []
  showSuggestions.value = false
  
  // Add user message
  addMessage({
    type: 'text',
    content: message
  })
  
  // Show typing indicator
  isBotTyping.value = true
  
  try {
    // Analyze intent and handle accordingly
    const intent = analyzeIntent(message)
    
    switch (intent) {
      case 'product_search':
        await handleProductSearch(message)
        break
      case 'order_tracking':
        await handleOrderTracking(message)
        break
      case 'statistics':
        await handleStatisticsRequest(message)
        break
      case 'customer_info':
        await handleCustomerInfo(message)
        break
      case 'comparison':
        await handleProductComparison(message)
        break
      default:
        await handleGeneralChat(message)
        break
    }
    
  } catch (error) {
    console.error('Error processing message:', error)
    analytics.errors.push({
      type: 'processing_error',
      message: error.message,
      timestamp: new Date()
    })
    
    addBotMessage({
      type: 'text',
      content: 'Xin lỗi, tôi gặp sự cố kỹ thuật. Vui lòng thử lại sau hoặc liên hệ nhân viên hỗ trợ trực tiếp.'
    })
  } finally {
    isBotTyping.value = false
  }
}

// Enhanced Intent Analysis with advanced search patterns
const analyzeIntent = (message) => {
  const lowerMessage = message.toLowerCase()
  
  // Advanced search patterns
  const searchPatterns = [
    'tìm', 'search', 'mua', 'điện thoại', 'smartphone', 'phone',
    'iphone', 'samsung', 'xiaomi', 'oppo', 'vivo', 'huawei',
    'dưới', 'trên', 'từ', 'đến', 'triệu', 'tr', 'm',
    'gb ram', 'gb rom', 'android', 'ios', 'one ui', 'miui',
    'mah', 'mAh', 'inch', 'mp camera', 'pin trâu', 'màn hình'
  ]
  
  if (searchPatterns.some(pattern => lowerMessage.includes(pattern))) {
    return 'product_search'
  }
  
  if (lowerMessage.includes('đơn hàng') || lowerMessage.includes('order') ||
      lowerMessage.includes('tra cứu') || lowerMessage.includes('mã đơn')) {
    return 'order_tracking'
  }
  
  if (lowerMessage.includes('thống kê') || lowerMessage.includes('statistics') ||
      lowerMessage.includes('doanh thu') || lowerMessage.includes('báo cáo')) {
    return 'statistics'
  }
  
  if (lowerMessage.includes('khách hàng') || lowerMessage.includes('customer') ||
      lowerMessage.includes('thông tin')) {
    return 'customer_info'
  }
  
  if (lowerMessage.includes('so sánh') || lowerMessage.includes('compare')) {
    return 'comparison'
  }
  
  return 'general'
}

// Handle Product Search
const handleProductSearch = async (message) => {
  try {
    const response = await fetch(`http://localhost:8080/api/chatbot/search-products?query=${encodeURIComponent(message)}`)
    const data = await response.json()
    
    if (data.success && data.products.length > 0) {
      lastSearchProducts.value = data.products
      addBotMessage({
        type: 'product_cards',
        content: {
          title: `Tìm thấy ${data.count} sản phẩm phù hợp:`,
          products: data.products.slice(0, 6).map(product => ({
            id: product.id,
            name: product.tenSanPham || product.name,
            price: product.giaBan || product.price || 0,
            imageUrl: product.hinhAnh || '/images/placeholder-phone.jpg',
            rating: 4.5,
            stock: product.soLuong || 0,
            specs: [
              product.tenHang || 'Unknown Brand',
              product.tenRam || 'Unknown RAM',
              product.tenRom || 'Unknown Storage',
              product.tenHeDieuHanh || 'Unknown OS',
              product.tenChip || 'Unknown Chip'
            ].filter(spec => spec && spec !== 'Unknown Brand' && spec !== 'Unknown RAM' && 
                     spec !== 'Unknown Storage' && spec !== 'Unknown OS' && spec !== 'Unknown Chip'),
            // Enhanced details
            brand: product.tenHang,
            ram: product.tenRam,
            rom: product.tenRom,
            os: product.tenHeDieuHanh,
            chip: product.tenChip,
            cpu: product.tenCpu,
            gpu: product.tenGpu,
            battery: product.tenPin,
            rearCamera: product.tenCameraSau,
            frontCamera: product.tenCameraTruoc,
            sim: product.tenSim,
            screen: product.tenManHinh,
            relevanceScore: product.relevanceScore || 0
          }))
        }
      })

      // Offer quick compare of top 2 results
      if (data.products.length >= 2) {
        addBotMessage({
          type: 'quick-replies',
          content: '',
          quickReplies: [
            { id: 'compare_top2', text: 'So sánh 2 sản phẩm đầu', action: 'compare_top_results' }
          ]
        })
      }
    } else {
      addBotMessage({
        type: 'text',
        content: 'Không tìm thấy sản phẩm phù hợp với yêu cầu của bạn. Vui lòng thử từ khóa khác.'
      })
    }
  } catch (error) {
    console.error('Error searching products:', error)
    addBotMessage({
      type: 'text',
      content: 'Xin lỗi, tôi không thể tìm kiếm sản phẩm lúc này. Vui lòng thử lại sau.'
    })
  }
}

// Handle Order Tracking (auto-parse from text if possible)
const handleOrderTracking = async (message) => {
  try {
    const codeMatch = String(message).match(/(ma\s*don|mã\s*đơn|code|order)\s*[:#-]?\s*([A-Za-z0-9_-]{4,})/i)
    const phoneMatch = String(message).match(/(0\d{9,10}|\+?84\d{9,10})/)
    const code = codeMatch && codeMatch[2]
    const phone = phoneMatch && phoneMatch[1]
    if (code && phone) {
      const res = await fetch(`http://localhost:8080/api/chatbot/check-order?orderCode=${encodeURIComponent(code)}&phone=${encodeURIComponent(phone)}`)
      const data = await res.json()
      if (data.success && data.found) {
        const order = data.order
        addBotMessage({
          type: 'text',
          content: `• Mã đơn hàng: ${order.maHoaDon}\n• Khách hàng: ${order.tenKhachHang}\n• SĐT: ${order.soDienThoai}\n• Trạng thái: ${order.tenTrangThai || order.trangThai}\n• Tổng tiền: ${formatPrice(order.tongTien)}`
        })
        return
      }
    }
  } catch (_) {}

  // fallback to form
  addBotMessage({
    type: 'form',
    content: {
      title: 'Tra cứu đơn hàng',
      fields: [
        { name: 'orderCode', label: 'Mã đơn hàng', type: 'text', placeholder: 'Nhập mã đơn hàng', required: true },
        { name: 'phone', label: 'Số điện thoại', type: 'tel', placeholder: 'Nhập số điện thoại', required: true }
      ],
      submitText: 'Tra cứu'
    }
  })
}

// Handle Statistics Request
const handleStatisticsRequest = async (message) => {
  try {
    const response = await fetch('http://localhost:8080/api/chatbot/quick-stats')
    const data = await response.json()
    
    if (data.success) {
      const stats = data.stats
      addBotMessage({
        type: 'text',
        content: `📊 **Thống kê cửa hàng:**\n\n` +
                `💰 Tổng doanh thu: ${formatPrice(stats.tongDoanhThu || 0)}\n` +
                `📦 Tổng đơn hàng: ${stats.tongDonHang || 0}\n` +
                `👥 Tổng khách hàng: ${stats.tongKhachHang || 0}\n` +
                `📱 Tổng sản phẩm: ${stats.tongSanPham || 0}`
      })
    } else {
      addBotMessage({
        type: 'text',
        content: 'Xin lỗi, tôi không thể lấy thống kê lúc này.'
      })
    }
  } catch (error) {
    console.error('Error getting statistics:', error)
    addBotMessage({
      type: 'text',
      content: 'Xin lỗi, tôi không thể lấy thống kê lúc này.'
    })
  }
}

// Handle Customer Info
const handleCustomerInfo = async (message) => {
  addBotMessage({
    type: 'form',
    content: {
      title: 'Tra cứu thông tin khách hàng',
      fields: [
        {
          name: 'phone',
          label: 'Số điện thoại',
          type: 'tel',
          placeholder: 'Nhập số điện thoại',
          required: true
        }
      ],
      submitText: 'Tra cứu'
    }
  })
}

// Comparison helpers
const extractProductNames = (text) => {
  if (!text) return []
  let s = text
  s = s.replace(/so\s*sánh|compare|vs\.?|với|voi|and|&/gi, ',')
  s = s.replace(/[|]/g, ',')
  const raw = s.split(',').map(t => t.trim()).filter(Boolean)
  const uniq = []
  for (const t of raw) {
    if (t.length < 2) continue
    if (!uniq.includes(t)) uniq.push(t)
    if (uniq.length >= 3) break
  }
  return uniq
}

const resolveFirstProductId = async (query) => {
  try {
    const res = await fetch(`http://localhost:8080/api/chatbot/search-products?query=${encodeURIComponent(query)}`)
    const data = await res.json()
    if (data && data.success && Array.isArray(data.products) && data.products.length > 0) {
      return data.products[0].id
    }
  } catch (_) {}
  return null
}

const buildLocalVerdict = (products = []) => {
  try {
    const parseNum = (s) => {
      if (s == null) return 0
      const m = String(s).match(/\d+/)
      return m ? parseInt(m[0], 10) : 0
    }
    const hzFrom = (scr) => {
      if (!scr) return 0
      const m = String(scr).toLowerCase().match(/(\d+)\s*hz/)
      return m ? parseInt(m[1], 10) : 0
    }
    const scoreOf = (p) => {
      const ram = parseNum(p.tenRam)
      const pin = parseNum(p.tenPin)
      const hz = hzFrom(p.tenManHinh)
      const price = Number(p.price || p.giaBan || 0)
      return ram + (pin/1000) + (hz/60) - (price/10000000)
    }
    let best = null
    let bestScore = -Infinity
    for (const p of products) {
      const sc = scoreOf(p)
      if (sc > bestScore) { bestScore = sc; best = p }
    }
    if (best) {
      return `Gợi ý: ${best.name || best.tenSanPham} có cấu hình/giá tốt trong nhóm.`
    }
  } catch (_) {}
  return ''
}

// Handle Product Comparison
const handleProductComparison = async (message) => {
  // Try to parse product names directly from user message
  const names = extractProductNames(message)
  if (names.length >= 2) {
    const ids = []
    for (const n of names) {
      const id = await resolveFirstProductId(n)
      if (id) ids.push(id)
      if (ids.length >= 3) break
    }
    if (ids.length >= 2) {
      try {
        const res = await fetch(`http://localhost:8080/api/chatbot/compare-products?productIds=${ids.join(',')}`)
        const payload = await res.json()
        if (payload && payload.success) {
          addBotMessage({
            type: 'comparison',
            title: 'Bảng so sánh sản phẩm',
            products: payload.products || [],
            verdict: payload.verdict || buildLocalVerdict(payload.products || []),
            quickReviews: payload.quickReviews || null
          })
          return
        }
      } catch (e) {
        // fall back to form
      }
    }
  }
  // Fall back to form when we cannot auto-resolve
  addBotMessage({
    type: 'form',
    content: {
      title: 'So sánh sản phẩm',
      fields: [
        { name: 'product1', label: 'Sản phẩm 1', type: 'text', placeholder: 'Tên sản phẩm hoặc ID', required: true },
        { name: 'product2', label: 'Sản phẩm 2', type: 'text', placeholder: 'Tên sản phẩm hoặc ID', required: true },
        { name: 'product3', label: 'Sản phẩm 3 (tùy chọn)', type: 'text', placeholder: 'Tên sản phẩm hoặc ID', required: false }
      ],
      submitText: 'So sánh'
    }
  })
}

// Handle General Chat
const handleGeneralChat = async (message) => {
  try {
    const response = await fetch('http://localhost:8080/api/chatbot/chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        message: message
      })
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    const data = await response.json()
    
    // Simulate typing delay
    await new Promise(resolve => setTimeout(resolve, ai.responseDelay))
    
    // Add bot response
    addBotMessage({
      type: 'text',
      content: data.message || data.response
    })
    
  } catch (error) {
    console.error('Error calling AI:', error)
    addBotMessage({
      type: 'text',
      content: 'Xin lỗi, tôi gặp sự cố kỹ thuật. Vui lòng thử lại sau.'
    })
  }
}

// Quick Reply Handler
const sendQuickReply = async (reply) => {
  try {
    // Handle special actions without sending text
    if (reply && reply.action === 'compare_top_results') {
      if (Array.isArray(lastSearchProducts.value) && lastSearchProducts.value.length >= 2) {
        const firstTwo = lastSearchProducts.value.slice(0, 2)
        const ids = firstTwo.map(p => p.id).filter(Boolean)
        if (ids.length >= 2) {
          isBotTyping.value = true
          const res = await fetch(`http://localhost:8080/api/chatbot/compare-products?productIds=${ids.join(',')}`)
          const payload = await res.json()
          addBotMessage({
            type: 'comparison',
            title: 'Bảng so sánh sản phẩm',
            products: (payload && payload.products) || [],
            verdict: payload && payload.verdict,
            quickReviews: payload && payload.quickReviews
          })
          isBotTyping.value = false
          trackEvent('quick_reply_used', { reply: reply.text, action: reply.action })
          return
        }
      }
    }
  } catch (e) {
    console.error('quick reply action error', e)
  }
  inputMessage.value = reply.text
  sendMessage()
  trackEvent('quick_reply_used', { reply: reply.text })
}

// Product Query Handler
const isProductRelated = (message) => {
  const productKeywords = [
    'iphone', 'samsung', 'xiaomi', 'oppo', 'vivo', 'huawei',
    'điện thoại', 'smartphone', 'phone', 'máy', 'so sánh',
    'giá', 'thông số', 'camera', 'pin', 'ram', 'rom'
  ]
  
  return productKeywords.some(keyword => 
    message.toLowerCase().includes(keyword)
  )
}

const handleProductQuery = async (message) => {
  try {
    // Search for products
    const searchResponse = await fetch(`http://localhost:8080/api/san-pham?search=${encodeURIComponent(message)}`)
    if (searchResponse.ok) {
      const products = await searchResponse.json()
      if (products && products.length > 0) {
        // Show product recommendations
        addBotMessage({
          type: 'product_cards',
          content: {
            title: 'Sản phẩm phù hợp với yêu cầu của bạn:',
            products: products.slice(0, 3).map(product => ({
              id: product.id,
              name: product.tenSanPham || product.name,
              price: product.giaBan || product.price || 0,
              imageUrl: product.hinhAnh || '/images/placeholder-phone.jpg',
              rating: 4.5,
              stock: product.soLuong || 0,
              specs: [
                product.tenHang || 'Unknown Brand',
                product.tenRam || 'Unknown RAM',
                product.tenRom || 'Unknown Storage'
              ].filter(Boolean)
            }))
          }
        })
      }
    }
  } catch (error) {
    console.error('Error searching products:', error)
  }
}

// Utility Functions
const scrollToBottom = () => {
  const container = document.querySelector('.ultra-chat-messages')
  if (container) {
    container.scrollTop = container.scrollHeight
  }
}

const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleTimeString('vi-VN', { 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

const formatPrice = (price) => {
  if (!price) return 'Liên hệ'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

// Analytics
const trackEvent = (eventName, data = {}) => {
  analytics.featuresUsed.add(eventName)
  
  // Send to analytics service (if implemented)
  if (typeof gtag !== 'undefined') {
    gtag('event', eventName, data)
  }
  
  console.log('Event tracked:', eventName, data)
}

// AI Response Generation (Legacy - now handled by backend)
const generateAIResponse = async (message) => {
  // This is now handled by the backend OpenAI service
  return null
}

const processWithAI = async (message) => {
  // This is now handled by the backend OpenAI service
  return null
}


const getProductRecommendations = async (query) => {
  try {
    const response = await fetch(`http://localhost:8080/api/san-pham?search=${encodeURIComponent(query)}`)
    if (response.ok) {
      return await response.json()
    }
  } catch (error) {
    console.error('Error getting product recommendations:', error)
  }
  return []
}

// Main composable function
const useUltraChat = () => {
  // Lifecycle hooks - must be called inside composable function
  onMounted(() => {
    // Initialize session
    userContext.sessionId = Date.now().toString()
    
    // Load saved settings
    const savedSettings = localStorage.getItem('ultra-chat-settings')
    if (savedSettings) {
      try {
        const settings = JSON.parse(savedSettings)
        Object.assign(ui, settings)
      } catch (error) {
        console.error('Error loading settings:', error)
      }
    }
    
    // Auto-save settings
    watch(ui, (newSettings) => {
      localStorage.setItem('ultra-chat-settings', JSON.stringify(newSettings))
    }, { deep: true })
  })

  onUnmounted(() => {
    // Cleanup
    trackEvent('session_ended', {
      duration: analytics.sessionDuration,
      messages: analytics.messagesSent + analytics.messagesReceived
    })
  })

  return {
    // State
    isOpen,
    isMinimized,
    isFullscreen,
    isTyping,
    isBotTyping,
    isConnected,
    connectionStatus,
    messages,
    unreadCount,
    inputMessage,
    suggestions,
    showSuggestions,
    showAttachMenu,
    showEmojiPicker,
    showSettings,
    
    // Context
    userContext,
    ai,
    realtime,
    media,
    ui,
    analytics,
    features,
    
    // Computed
    messageCount,
    hasUnreadMessages,
    isTypingActive,
    
    // Functions
    openChat,
    closeChat,
    minimizeChat,
    maximizeChat,
    toggleFullscreen,
    sendMessage,
    sendQuickReply,
    addMessage,
    addBotMessage,
    scrollToBottom,
    trackEvent,
    formatTime,
    formatPrice,
    processWithAI,
    analyzeIntent,
    generateAIResponse,
    getProductRecommendations
  }
}

// Export the composable function
export { useUltraChat }
