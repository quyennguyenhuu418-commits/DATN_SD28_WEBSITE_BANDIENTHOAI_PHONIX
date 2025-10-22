import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useAuthStore } from '@/stores/authStore'

/**
 * Simple Chat Composable
 * 
 * Features:
 * - Staff auto-join when login
 * - Customer chat support
 * - Real-time messaging
 */
export function useChat() {
  const authStore = useAuthStore()
  
  // WebSocket connection
  const socket = ref(null)
  const isConnected = ref(false)
  const error = ref(null)
  
  // Staff state
  const isStaff = ref(false)
  const staffId = ref('')
  const staffName = ref('')
  const waitingCustomers = ref([])
  const activeChats = ref([])
  const selectedChat = ref(null)
  
  // Customer state
  const isCustomer = ref(false)
  const sessionId = ref('')
  const customerName = ref('')
  const assignedStaff = ref('')
  const messages = ref([])
  const newMessage = ref('')
  
  // Customer form
  const customerForm = ref({
    customerName: '',
    phone: '',
    email: '',
    message: ''
  })
  
  // Customer chat data for persistence
  const customerChatData = ref(null)
  
  // Staff session data for persistence
  const staffSessionData = ref({
    staffId: '',
    staffName: '',
    waitingCustomers: [],
    activeChats: [],
    selectedChat: null,
    messages: []
  })

  /**
   * Save staff session data to localStorage
   */
  const saveStaffSessionData = () => {
    try {
      console.log('💾 Attempting to save staff session data...')
      console.log('📊 Current state:', {
        isStaff: isStaff.value,
        staffId: staffId.value,
        staffName: staffName.value,
        waitingCount: waitingCustomers.value.length,
        activeCount: activeChats.value.length,
        selectedChat: selectedChat.value?.customerName || 'none',
        messageCount: messages.value.length
      })
      
      // Validate data before saving
      if (!isStaff.value) {
        console.log('⚠️ Not in staff mode, skipping save')
        return
      }
      
      const data = {
        staffId: staffId.value || '',
        staffName: staffName.value || '',
        waitingCustomers: Array.isArray(waitingCustomers.value) ? waitingCustomers.value : [],
        activeChats: Array.isArray(activeChats.value) ? activeChats.value : [],
        selectedChat: selectedChat.value || null,
        messages: Array.isArray(messages.value) ? messages.value : [],
        timestamp: Date.now()
      }
      
      // Double check localStorage is available
      if (typeof localStorage === 'undefined') {
        console.error('❌ localStorage is not available')
        return
      }
      
      localStorage.setItem('staffChatSession', JSON.stringify(data))
      console.log('✅ Staff session data saved successfully:', {
        staffId: data.staffId,
        staffName: data.staffName,
        waitingCount: data.waitingCustomers.length,
        activeCount: data.activeChats.length,
        selectedChat: data.selectedChat?.customerName || 'none',
        messageCount: data.messages.length,
        timestamp: new Date(data.timestamp).toLocaleString()
      })
    } catch (error) {
      console.error('❌ Error saving staff session data:', error)
      // Try to save minimal data as fallback
      try {
        const minimalData = {
          staffId: staffId.value || '',
          staffName: staffName.value || '',
          waitingCustomers: [],
          activeChats: [],
          selectedChat: null,
          messages: [],
          timestamp: Date.now()
        }
        localStorage.setItem('staffChatSession', JSON.stringify(minimalData))
        console.log('🔄 Saved minimal data as fallback')
      } catch (fallbackError) {
        console.error('❌ Fallback save also failed:', fallbackError)
      }
    }
  }

  /**
   * Load staff session data from localStorage
   */
  const loadStaffSessionData = () => {
    try {
      console.log('🔄 Attempting to load staff session data...')
      
      // Check if localStorage is available
      if (typeof localStorage === 'undefined') {
        console.error('❌ localStorage is not available')
        return false
      }
      
      const saved = localStorage.getItem('staffChatSession')
      console.log('📦 Saved data from localStorage:', saved ? 'exists' : 'null')
      
      if (saved) {
        const data = JSON.parse(saved)
        console.log('📊 Parsed data:', data)
        
        // Validate data structure
        if (!data || typeof data !== 'object') {
          console.error('❌ Invalid data structure')
          return false
        }
        
        // Check if data is not too old (within 1 hour)
        const timeDiff = Date.now() - (data.timestamp || 0)
        console.log('⏰ Time difference:', timeDiff, 'ms (max: 3600000ms)')
        
        if (timeDiff < 3600000) {
          console.log('✅ Data is fresh, loading...')
          
          // Safely assign values with validation
          staffId.value = data.staffId || ''
          staffName.value = data.staffName || ''
          waitingCustomers.value = Array.isArray(data.waitingCustomers) ? data.waitingCustomers : []
          activeChats.value = Array.isArray(data.activeChats) ? data.activeChats : []
          selectedChat.value = data.selectedChat || null
          messages.value = Array.isArray(data.messages) ? data.messages : []
          
          console.log('📂 Staff session data loaded successfully:', {
            staffId: staffId.value,
            staffName: staffName.value,
            waitingCount: waitingCustomers.value.length,
            activeCount: activeChats.value.length,
            selectedChat: selectedChat.value?.customerName || 'none',
            messageCount: messages.value.length
          })
          return true
        } else {
          console.log('⏰ Staff session data expired, clearing...')
          localStorage.removeItem('staffChatSession')
        }
      } else {
        console.log('❌ No saved data found in localStorage')
      }
    } catch (error) {
      console.error('❌ Error loading staff session data:', error)
      // Clear corrupted data
      try {
        localStorage.removeItem('staffChatSession')
        console.log('🧹 Cleared corrupted data')
      } catch (clearError) {
        console.error('❌ Error clearing corrupted data:', clearError)
      }
    }
    return false
  }

  /**
   * Clear staff session data
   */
  const clearStaffSessionData = () => {
    try {
      localStorage.removeItem('staffChatSession')
      console.log('🗑️ Staff session data cleared')
    } catch (error) {
      console.error('❌ Error clearing staff session data:', error)
    }
  }

  /**
   * Initialize WebSocket connection
   */
  const initWebSocket = () => {
    try {
      // Load SockJS and Stomp
      import('sockjs-client').then(sockjsModule => {
        import('stompjs').then(stompModule => {
          const SockJS = sockjsModule.default || sockjsModule
          const Stomp = stompModule.default || stompModule
          
          const socketUrl = 'http://localhost:8080/ws'
          const client = new SockJS(socketUrl)
          const stompClient = Stomp.over(client)
          
          // Disable debug logs
          stompClient.debug = null
          
          stompClient.connect({}, (frame) => {
            console.log('WebSocket connected:', frame)
            isConnected.value = true
            error.value = null
            
            // Auto-join as staff if logged in
            if (authStore.isAuthenticated && authStore.user) {
              joinAsStaff()
            }
            
            // Subscribe to staff notifications
            console.log('Subscribing to staff notifications:', '/topic/staff/notifications')
            stompClient.subscribe('/topic/staff/notifications', (message) => {
              console.log('🔔 Received message on staff notifications:', message.body)
              const data = JSON.parse(message.body)
              handleStaffNotification(data)
            })
            
            // Subscribe to staff status
            stompClient.subscribe('/topic/staff/status', (message) => {
              const data = JSON.parse(message.body)
              handleStaffStatus(data)
            })
            
            // Subscribe to customer joined confirmation
            stompClient.subscribe('/topic/customer/joined', (message) => {
              const data = JSON.parse(message.body)
              handleCustomerJoined(data)
            })
            
            // Customer messages subscription will be handled in joinAsCustomer
            console.log('WebSocket initialized, ready for subscriptions')
            
            // Subscribe to staff messages if staff
            if (authStore.isAuthenticated && authStore.user) {
              console.log('Subscribing to staff messages:', `/topic/staff/${authStore.user.id}`)
              stompClient.subscribe(`/topic/staff/${authStore.user.id}`, (message) => {
                console.log('📨 Received message on staff subscription:', message.body)
                const data = JSON.parse(message.body)
                handleStaffMessage(data)
              })
            }
            
          }, (error) => {
            console.error('WebSocket connection error:', error)
            isConnected.value = false
            error.value = error
          })
          
          socket.value = stompClient
        })
      })
    } catch (err) {
      console.error('Failed to initialize WebSocket:', err)
      error.value = err
    }
  }

  /**
   * Join as staff
   */
  const joinAsStaff = () => {
    console.log('🚀 joinAsStaff called - isStaff:', isStaff.value, 'isConnected:', isConnected.value)
    
    if (!socket.value || !isConnected.value) {
      console.log('❌ Cannot join - socket or connection not ready')
      return
    }
    
    // Try to load existing session data first
    console.log('🔄 Checking for existing session data...')
    const hasExistingData = loadStaffSessionData()
    
    if (!hasExistingData) {
      console.log('🆕 No existing data, creating new session...')
      // No existing data, create new session
      staffId.value = authStore.user.id.toString()
      staffName.value = authStore.user.hoTen || 'Nhân viên'
    } else {
      console.log('✅ Existing data loaded, continuing with saved session...')
    }
    
    isStaff.value = true
    
    console.log('Sending staff join request:', { staffId: staffId.value, staffName: staffName.value })
    socket.value.send('/app/staff/join', {}, JSON.stringify({
      staffId: staffId.value,
      staffName: staffName.value
    }))
    
    // Save session data
    saveStaffSessionData()
    
    console.log('Joined as staff:', staffName.value)
  }

  /**
   * Leave as staff
   */
  const leaveAsStaff = () => {
    if (!socket.value || !isStaff.value) return
    
    socket.value.send('/app/staff/leave', {}, JSON.stringify({
      staffId: staffId.value
    }))
    
    isStaff.value = false
    staffId.value = ''
    staffName.value = ''
    waitingCustomers.value = []
    activeChats.value = []
    selectedChat.value = null
    
    // Clear session data when leaving
    clearStaffSessionData()
    
    console.log('Left as staff')
  }

  /**
   * Join as customer
   */
  const joinAsCustomer = () => {
    console.log('joinAsCustomer called - isCustomer:', isCustomer.value, 'isConnected:', isConnected.value)
    
    if (!socket.value || !isConnected.value) {
      console.log('Cannot join - socket or connection not ready')
      return
    }
    
    if (isCustomer.value) {
      console.log('Already joined as customer, skipping')
      return
    }
    
    isCustomer.value = true
    
    // Check if we have existing customer chat data
    if (customerChatData.value) {
      console.log('Loading existing customer chat data')
      sessionId.value = customerChatData.value.sessionId
      customerName.value = customerChatData.value.customerName
      assignedStaff.value = customerChatData.value.assignedStaff
      messages.value = [...customerChatData.value.messages]
      
      // Still need to subscribe to WebSocket for new messages
      console.log('Subscribing to existing customer messages:', `/topic/customer/${sessionId.value}`)
      
      // Unsubscribe from any existing subscription first
      if (socket.value.subscriptions) {
        Object.keys(socket.value.subscriptions).forEach(key => {
          if (key.includes('/topic/customer/')) {
            console.log('Unsubscribing from:', key)
            socket.value.subscriptions[key].unsubscribe()
          }
        })
      }
      
      // Add a small delay to ensure WebSocket is ready
      setTimeout(() => {
        if (socket.value && sessionId.value) {
          const subscription = socket.value.subscribe(`/topic/customer/${sessionId.value}`, (message) => {
            console.log('Received message on existing customer subscription:', message.body)
            const data = JSON.parse(message.body)
            handleCustomerMessage(data)
          })
          
          console.log('Existing customer subscription created:', subscription)
          console.log('Subscription topic:', `/topic/customer/${sessionId.value}`)
        }
      }, 100)
    } else {
      // Generate session ID for new customer
      sessionId.value = 'customer_' + Date.now() + '_' + Math.random().toString(36).substr(2, 8)
      customerName.value = customerForm.value.customerName
      
      // Initialize customer chat data for persistence
      customerChatData.value = {
        sessionId: sessionId.value,
        customerName: customerForm.value.customerName,
        phone: customerForm.value.phone,
        email: customerForm.value.email,
        messages: [],
        assignedStaff: null
      }
      
      console.log('Sending customer join request:', {
        customerName: customerForm.value.customerName,
        phone: customerForm.value.phone,
        email: customerForm.value.email,
        sessionId: sessionId.value
      })
      
      socket.value.send('/app/customer/join', {}, JSON.stringify({
        customerName: customerForm.value.customerName,
        phone: customerForm.value.phone,
        email: customerForm.value.email
      }))
    }
    
    // Subscribe to customer messages after sessionId is set
    nextTick(() => {
      if (sessionId.value && socket.value) {
        console.log('Subscribing to customer messages:', `/topic/customer/${sessionId.value}`)
        
        // Unsubscribe from any existing subscription first
        if (socket.value.subscriptions) {
          Object.keys(socket.value.subscriptions).forEach(key => {
            if (key.includes('/topic/customer/')) {
              console.log('Unsubscribing from:', key)
              socket.value.subscriptions[key].unsubscribe()
            }
          })
        }
        
        // Add a small delay to ensure WebSocket is ready
        setTimeout(() => {
          if (socket.value && sessionId.value) {
            const subscription = socket.value.subscribe(`/topic/customer/${sessionId.value}`, (message) => {
              console.log('Received message on customer subscription:', message.body)
              const data = JSON.parse(message.body)
              handleCustomerMessage(data)
            })
            
            console.log('Customer subscription created:', subscription)
            console.log('Subscription topic:', `/topic/customer/${sessionId.value}`)
          }
        }, 100)
      } else {
        console.log('Cannot subscribe - sessionId:', sessionId.value, 'socket:', !!socket.value)
      }
    })
    
    console.log('Joined as customer:', customerForm.value.customerName, 'Session:', sessionId.value)
  }

  /**
   * Take customer (staff action)
   */
  const takeCustomer = (customerSessionId) => {
    if (!socket.value || !isStaff.value) return
    
    console.log('🎯 takeCustomer called:', customerSessionId)
    console.log('📊 Before take - waiting:', waitingCustomers.value.length, 'active:', activeChats.value.length)
    
    // Check if already in active chats
    const existingActive = activeChats.value.find(c => c.sessionId === customerSessionId)
    if (existingActive) {
      console.log('Customer already in active chats, selecting:', customerSessionId)
      selectedChat.value = existingActive
      return
    }
    
    socket.value.send('/app/staff/take-customer', {}, JSON.stringify({
      staffId: staffId.value,
      sessionId: customerSessionId
    }))
    
    // Add to active chats if not already exists
    const customer = waitingCustomers.value.find(c => c.sessionId === customerSessionId)
    if (customer) {
      console.log('Adding customer to active chats from takeCustomer:', customerSessionId)
      activeChats.value.push(customer)
      waitingCustomers.value = waitingCustomers.value.filter(c => c.sessionId !== customerSessionId)
      selectedChat.value = customer
      
      // Save session data after taking customer
      saveStaffSessionData()
      
      console.log('📊 After take - waiting:', waitingCustomers.value.length, 'active:', activeChats.value.length)
      console.log('✅ Customer taken successfully:', customerSessionId)
    }
  }

  /**
   * Select chat (staff action)
   */
  const selectChat = (chat) => {
    selectedChat.value = chat
    
    // Load messages for this chat if they exist
    if (chat.messages) {
      messages.value = chat.messages
    } else {
      messages.value = []
      // Initialize empty messages array for this chat
      chat.messages = []
    }
    
    // Save session data after selecting chat
    saveStaffSessionData()
  }

  /**
   * Send message as staff
   */
  const sendStaffMessage = (message) => {
    if (!socket.value || !isStaff.value || !selectedChat.value) return

    // Check if message already exists to prevent duplicate
    const messageExists = messages.value.some(msg => 
      msg.content === message && 
      msg.sender === 'staff' && 
      Math.abs(new Date() - new Date(msg.timestamp)) < 1000
    )
    
    if (messageExists) {
      console.log('Message already exists, skipping duplicate:', message)
      return
    }

    // Add to local messages immediately (only once)
    const messageId = Date.now()
    const newMessage = {
      id: messageId,
      sender: 'staff',
      content: message,
      timestamp: new Date()
    }
    
    messages.value.push(newMessage)
    
    // Also add to chat's messages array for persistence
    if (selectedChat.value.messages) {
      selectedChat.value.messages.push(newMessage)
    }

    console.log('Sending staff message:', message, 'to session:', selectedChat.value.sessionId)
    socket.value.send('/app/staff/send-message', {}, JSON.stringify({
      staffId: staffId.value,
      sessionId: selectedChat.value.sessionId,
      message: message
    }))
    
    // Save session data after sending message
    saveStaffSessionData()
  }

  /**
   * Send message as customer
   */
  const sendCustomerMessage = (message) => {
    if (!socket.value || !isCustomer.value) return
    
    // Check if message already exists to prevent duplicate
    const messageExists = messages.value.some(msg => 
      msg.content === message && 
      msg.sender === 'customer' && 
      Math.abs(new Date() - new Date(msg.timestamp)) < 1000
    )
    
    if (messageExists) {
      console.log('Customer message already exists, skipping duplicate:', message)
      return
    }
    
    // Add to local messages immediately (only once)
    const messageId = Date.now()
    const newMessage = {
      id: messageId,
      sender: 'customer',
      content: message,
      timestamp: new Date()
    }
    
    messages.value.push(newMessage)
    
    // Also add to customer chat data for persistence
    if (customerChatData.value) {
      customerChatData.value.messages.push(newMessage)
    }

    console.log('Sending customer message:', message, 'to session:', sessionId.value)
    socket.value.send('/app/customer/send-message', {}, JSON.stringify({
      sessionId: sessionId.value,
      message: message
    }))
  }

  /**
   * Handle staff notifications
   */
  const handleStaffNotification = (data) => {
    console.log('🔔 Staff notification received:', data)
    switch (data.type) {
      case 'new_customer':
        // Check if customer already exists in waiting list
        const existingWaiting = waitingCustomers.value.find(c => c.sessionId === data.sessionId)
        if (!existingWaiting) {
          console.log('Adding new customer to waiting list:', data.sessionId)
          waitingCustomers.value.push({
            sessionId: data.sessionId,
            customerName: data.customerName,
            phone: data.phone,
            email: data.email,
            joinedAt: new Date(),
            messages: [] // Initialize messages array
          })
        } else {
          console.log('Customer already in waiting list, skipping:', data.sessionId)
        }
        
        // Save session data after new customer
        saveStaffSessionData()
        break
             case 'customer_assigned':
               // Check if already exists in active chats
               const existingChat = activeChats.value.find(c => c.sessionId === data.sessionId)
               if (!existingChat) {
                 console.log('Adding customer to active chats:', data.sessionId)
                 // Add to active chats
                 activeChats.value.push({
                   sessionId: data.sessionId,
                   customerName: data.customerName,
                   phone: data.phone,
                   email: data.email,
                   joinedAt: new Date(),
                   messages: [] // Initialize messages array
                 })
               } else {
                 console.log('Customer already in active chats, skipping:', data.sessionId)
               }
               // Remove from waiting
               waitingCustomers.value = waitingCustomers.value.filter(c => c.sessionId !== data.sessionId)
               
               // Save session data after customer assigned
               saveStaffSessionData()
               break
             case 'customer_disconnect':
               console.log('🚫 Customer disconnected notification:', data.customerName, 'Session:', data.sessionId)
               console.log('📊 Before removal - Active chats:', activeChats.value.length, 'Waiting:', waitingCustomers.value.length)
               
               // Remove from active chats
               const beforeActive = activeChats.value.length
               activeChats.value = activeChats.value.filter(c => c.sessionId !== data.sessionId)
               const afterActive = activeChats.value.length
               console.log('📈 Active chats: before', beforeActive, 'after', afterActive)
               
               // Remove from waiting customers
               const beforeWaiting = waitingCustomers.value.length
               waitingCustomers.value = waitingCustomers.value.filter(c => c.sessionId !== data.sessionId)
               const afterWaiting = waitingCustomers.value.length
               console.log('📈 Waiting customers: before', beforeWaiting, 'after', afterWaiting)
               
               // Clear selected chat if it's the disconnected customer
               if (selectedChat.value && selectedChat.value.sessionId === data.sessionId) {
                 console.log('🧹 Clearing selected chat for disconnected customer')
                 selectedChat.value = null
                 messages.value = []
               }
               
               console.log('✅ Customer', data.customerName, 'removed from dashboard')
               
               // Save session data after customer disconnect
               saveStaffSessionData()
               break
      default:
        console.log('Unknown staff notification:', data.type)
    }
  }

  /**
   * Handle staff status updates
   */
  const handleStaffStatus = (data) => {
    console.log('Staff status update:', data)
  }

  /**
   * Handle customer joined confirmation
   */
  const handleCustomerJoined = (data) => {
    console.log('Customer joined data:', data)
    
    // Only process if this is for a customer, not staff
    if (!isCustomer.value) {
      console.log('Not a customer, skipping customer joined message')
      return
    }
    
    if (data.success) {
      sessionId.value = data.sessionId
      const systemMessage = {
        id: Date.now(),
        sender: 'system',
        content: data.message,
        timestamp: new Date()
      }
      messages.value.push(systemMessage)
      
      // Also add to customer chat data for persistence
      if (customerChatData.value) {
        customerChatData.value.messages.push(systemMessage)
      }
    } else if (data.type === 'staff_connected') {
      // Staff connected to customer - don't show message here, handled by staff_assigned
      assignedStaff.value = data.staffName
      
      // Update customer chat data for persistence
      if (customerChatData.value) {
        customerChatData.value.assignedStaff = data.staffName
      }
    } else if (data.type === 'staff_message') {
      // Staff message received - check for duplicate
      const messageExists = messages.value.some(msg => 
        msg.content === data.message && 
        msg.sender === 'staff' && 
        Math.abs(new Date(msg.timestamp) - new Date(data.timestamp)) < 1000
      )
      
      console.log('Customer joined - Message exists check:', messageExists, 'for message:', data.message)
      
      if (!messageExists) {
        const newMessage = {
          id: Date.now(),
          sender: 'staff',
          content: data.message,
          staffName: data.staffName,
          timestamp: new Date(data.timestamp)
        }
        messages.value.push(newMessage)
        
        // Also add to customer chat data for persistence
        if (customerChatData.value) {
          customerChatData.value.messages.push(newMessage)
        }
        
        console.log('Customer joined - Added new staff message:', data.message)
      } else {
        console.log('Customer joined - Skipped duplicate staff message:', data.message)
      }
    }
  }

  /**
   * Handle customer messages
   */
  const handleCustomerMessage = (data) => {
    console.log('Customer message received:', data)
    console.log('Current isCustomer:', isCustomer.value, 'sessionId:', sessionId.value)
    
    switch (data.type) {
      case 'customer_joined':
        console.log('Customer joined message received')
        if (data.success) {
          sessionId.value = data.sessionId
          const systemMessage = {
            id: Date.now(),
            sender: 'system',
            content: data.message,
            timestamp: new Date()
          }
          messages.value.push(systemMessage)
          
          // Also add to customer chat data for persistence
          if (customerChatData.value) {
            customerChatData.value.messages.push(systemMessage)
          }
          
          // Auto scroll after adding message
          nextTick(() => {
            scrollToBottom.value()
          })
        }
        break
      case 'staff_assigned':
        assignedStaff.value = data.staffName
        
        // Check if staff connection message already exists to avoid duplicate
        const staffMessageExists = messages.value.some(msg => 
          msg.sender === 'system' && 
          msg.content.includes('Đã kết nối với nhân viên') &&
          msg.content.includes(data.staffName)
        )
        
        if (!staffMessageExists) {
          // Add system message to show staff connection
          const staffConnectedMessage = {
            id: Date.now(),
            sender: 'system',
            content: `Đã kết nối với nhân viên ${data.staffName}`,
            timestamp: new Date()
          }
          messages.value.push(staffConnectedMessage)
          
          // Update customer chat data for persistence
          if (customerChatData.value) {
            customerChatData.value.messages.push(staffConnectedMessage)
          }
          
          // Auto scroll after adding message
          nextTick(() => {
            scrollToBottom.value()
          })
        }
        
        // Update customer chat data for persistence
        if (customerChatData.value) {
          customerChatData.value.assignedStaff = data.staffName
        }
        break
      case 'staff_message':
        console.log('Processing staff message:', data.message, 'from:', data.staffName)
        
        // Check if message already exists to avoid duplicate
        const messageExists = messages.value.some(msg => 
          msg.content === data.message && 
          msg.sender === 'staff' && 
          Math.abs(new Date(msg.timestamp) - new Date(data.timestamp)) < 1000
        )
        
        console.log('Message exists check:', messageExists, 'for message:', data.message)
        
        if (!messageExists) {
          const newMessage = {
            id: Date.now(),
            sender: 'staff',
            content: data.message,
            staffName: data.staffName,
            timestamp: new Date(data.timestamp)
          }
          messages.value.push(newMessage)
          
          // Also add to customer chat data for persistence
          if (customerChatData.value) {
            customerChatData.value.messages.push(newMessage)
          }
          
          console.log('Added new staff message:', data.message)
          
          // Auto scroll after adding message
          nextTick(() => {
            scrollToBottom.value()
          })
        } else {
          console.log('Skipped duplicate staff message:', data.message)
        }
        break
      default:
        console.log('Unknown customer message:', data.type)
    }
  }

  /**
   * Remove customer from waiting list
   */
  const removeFromWaiting = (sessionId) => {
    waitingCustomers.value = waitingCustomers.value.filter(c => c.sessionId !== sessionId)
  }

  /**
   * Remove customer from active chats
   */
  const removeFromActive = (sessionId) => {
    console.log('🗑️ removeFromActive called for sessionId:', sessionId)
    console.log('📊 Before removal - Active chats:', activeChats.value.length)
    
    // Find the chat to clear its messages
    const chatToRemove = activeChats.value.find(chat => chat.sessionId === sessionId)
    if (chatToRemove && chatToRemove.messages) {
      console.log('🧹 Clearing messages for chat:', chatToRemove.customerName)
      chatToRemove.messages = []
    }
    
    const beforeCount = activeChats.value.length
    activeChats.value = activeChats.value.filter(c => c.sessionId !== sessionId)
    const afterCount = activeChats.value.length
    
    console.log('📈 Active chats: before', beforeCount, 'after', afterCount)
    
    if (selectedChat.value && selectedChat.value.sessionId === sessionId) {
      console.log('🧹 Clearing selected chat for disconnected customer')
      selectedChat.value = null
      messages.value = []
    }
    
    console.log('✅ Customer removed from active chats:', sessionId)
  }

  /**
   * Handle staff messages
   */
  const handleStaffMessage = (data) => {
    console.log('Staff message received:', data)
    switch (data.type) {
      case 'customer_assigned':
        // This is handled by handleStaffNotification, skip here to avoid duplicate
        console.log('Customer assigned handled by notification, skipping staff message')
        break
             case 'customer_message':
               // Add message to selected chat if it matches
               if (selectedChat.value && selectedChat.value.sessionId === data.sessionId) {
                 // Check if message already exists to avoid duplicate
                 const messageExists = messages.value.some(msg => 
                   msg.content === data.message && 
                   msg.sender === 'customer' && 
                   Math.abs(new Date(msg.timestamp) - new Date(data.timestamp)) < 1000
                 )
                 
                 console.log('Staff received customer message:', data.message, 'exists:', messageExists)
                 
                 if (!messageExists) {
                   const newMessage = {
                     id: Date.now(),
                     sender: 'customer',
                     content: data.message,
                     customerName: data.customerName,
                     timestamp: new Date(data.timestamp)
                   }
                   
                   messages.value.push(newMessage)
                   
                   // Also add to chat's messages array for persistence
                   if (selectedChat.value.messages) {
                     selectedChat.value.messages.push(newMessage)
                   }
                   
                   // Save session data after receiving message
                   saveStaffSessionData()
                   
                   console.log('Added customer message to staff chat:', data.message)
                 } else {
                   console.log('Skipped duplicate customer message:', data.message)
                 }
               }
               break
      case 'customer_disconnect':
        console.log('🚫 Staff message - Customer disconnected:', data.customerName, 'Session:', data.sessionId)
        
        // Clear messages when customer disconnects
        if (selectedChat.value && selectedChat.value.sessionId === data.sessionId) {
          console.log('🧹 Clearing messages for disconnected customer')
          messages.value = []
          if (selectedChat.value.messages) {
            selectedChat.value.messages = []
          }
          // Clear selected chat
          selectedChat.value = null
        }
        
        // Remove from active chats
        console.log('📊 Removing from active chats...')
        removeFromActive(data.sessionId)
        
        // Show notification to staff
               console.log('✅ Customer', data.customerName, 'has disconnected')
               
               // Save session data after customer disconnect
               saveStaffSessionData()
               break
      default:
        console.log('Unknown staff message:', data.type)
    }
  }

  /**
   * Disconnect WebSocket
   */
  const disconnect = () => {
    if (socket.value) {
      if (isStaff.value) {
        leaveAsStaff()
      }
      socket.value.disconnect()
      socket.value = null
    }
    isConnected.value = false
    isStaff.value = false
    isCustomer.value = false
  }

  /**
   * Customer disconnect (clear data but keep WebSocket)
   */
  const customerDisconnect = () => {
    console.log('=== CUSTOMER DISCONNECT START ===')
    console.log('Current state - isCustomer:', isCustomer.value, 'sessionId:', sessionId.value, 'socket:', !!socket.value)
    console.log('Socket subscriptions:', socket.value?.subscriptions ? Object.keys(socket.value.subscriptions) : 'none')
    
    // Send disconnect message to server BEFORE clearing state
    if (socket.value && sessionId.value) {
      console.log('Sending customer disconnect message for session:', sessionId.value)
      try {
        // Send disconnect message
        socket.value.send('/app/customer/disconnect', {}, JSON.stringify({
          sessionId: sessionId.value
        }))
        console.log('✅ Disconnect message sent successfully')
        
        // Also send to customer joined topic as backup
        socket.value.send('/app/customer/joined', {}, JSON.stringify({
          type: 'customer_disconnect',
          sessionId: sessionId.value
        }))
        console.log('✅ Backup disconnect message sent to customer/joined')
        
        // Wait a bit for message to be sent before clearing state
        setTimeout(() => {
          clearCustomerState()
        }, 200)
      } catch (error) {
        console.error('❌ Error sending disconnect message:', error)
        clearCustomerState()
      }
    } else if (socket.value) {
      // Try to send disconnect even without sessionId (fallback)
      console.log('Sending disconnect without sessionId as fallback')
      try {
        socket.value.send('/app/customer/disconnect', {}, JSON.stringify({
          sessionId: 'unknown'
        }))
        console.log('✅ Fallback disconnect message sent')
        setTimeout(() => {
          clearCustomerState()
        }, 200)
      } catch (error) {
        console.error('❌ Error sending fallback disconnect message:', error)
        clearCustomerState()
      }
    } else {
      console.log('❌ Cannot send disconnect - socket:', !!socket.value, 'sessionId:', sessionId.value)
      clearCustomerState()
    }
  }

  /**
   * Clear customer state
   */
  const clearCustomerState = () => {
    console.log('Clearing customer state...')
    
    // Clear customer state
    isCustomer.value = false
    sessionId.value = ''
    customerName.value = ''
    assignedStaff.value = null
    messages.value = []
    customerChatData.value = null
    
    // Reset form
    customerForm.value = {
      customerName: '',
      phone: '',
      email: '',
      message: ''
    }
    
    console.log('✅ Customer state cleared')
    console.log('=== CUSTOMER DISCONNECT END ===')
  }

  /**
   * Scroll to bottom of messages
   */
  const scrollToBottom = ref(() => {
    console.log('Scroll to bottom called - no function set')
  })


  // Lifecycle
  onMounted(() => {
    // Add global error handler
    window.addEventListener('error', (event) => {
      console.error('🚨 Global JavaScript error:', event.error)
      // Try to save data even if there's an error
      if (isStaff.value) {
        try {
          saveStaffSessionData()
        } catch (saveError) {
          console.error('❌ Error saving data after global error:', saveError)
        }
      }
    })
    
    // Only init WebSocket if we have a token
    if (authStore.token) {
      initWebSocket()
    } else {
      console.log('No auth token, skipping WebSocket initialization')
    }
    
    // Auto-save session data every 30 seconds
    setInterval(() => {
      try {
        if (isStaff.value) {
          saveStaffSessionData()
        }
      } catch (error) {
        console.error('❌ Error in auto-save interval:', error)
      }
    }, 30000)
    
    // Force save every 5 seconds as backup
    setInterval(() => {
      try {
        if (isStaff.value && (waitingCustomers.value.length > 0 || activeChats.value.length > 0)) {
          console.log('🔄 Force saving data as backup...')
          saveStaffSessionData()
        }
      } catch (error) {
        console.error('❌ Error in force save interval:', error)
      }
    }, 5000)
    
    // Save data before page unload
    const handleBeforeUnload = () => {
      try {
        if (isStaff.value) {
          console.log('💾 Saving staff data before page unload...')
          saveStaffSessionData()
        }
      } catch (error) {
        console.error('❌ Error in beforeunload save:', error)
      }
    }
    
    window.addEventListener('beforeunload', handleBeforeUnload)
    
    // Cleanup on unmount
    onUnmounted(() => {
      window.removeEventListener('beforeunload', handleBeforeUnload)
    })
    
    // Watch for auth changes
    watch(() => authStore.token, (newToken) => {
      if (newToken && !isConnected.value) {
        console.log('Auth token available, initializing WebSocket')
        initWebSocket()
      } else if (!newToken && isConnected.value) {
        console.log('Auth token removed, disconnecting WebSocket')
        disconnect()
      }
    })
    
    // Watch for data changes and auto-save
    watch([waitingCustomers, activeChats, selectedChat, messages], () => {
      try {
        if (isStaff.value) {
          console.log('🔄 Data changed, auto-saving...')
          saveStaffSessionData()
        }
      } catch (error) {
        console.error('❌ Error in watch auto-save:', error)
      }
    }, { deep: true })
  })

  onUnmounted(() => {
    console.log('useChat unmounted, saving data and disconnecting...')
    
    try {
      // Save data before unmounting
      if (isStaff.value) {
        console.log('💾 Saving staff data before unmount...')
        saveStaffSessionData()
      }
    } catch (error) {
      console.error('❌ Error saving data before unmount:', error)
    }
    
    try {
      disconnect()
    } catch (error) {
      console.error('❌ Error during disconnect:', error)
    }
  })

  return {
    // Connection state
    isConnected,
    error,
    
    // Staff state
    isStaff,
    staffId,
    staffName,
    waitingCustomers,
    activeChats,
    selectedChat,
    
    // Customer state
    isCustomer,
    sessionId,
    customerName,
    assignedStaff,
    messages,
    newMessage,
    customerForm,
    customerChatData,
    
    // Methods
    joinAsStaff,
    leaveAsStaff,
    joinAsCustomer,
    takeCustomer,
    selectChat,
    sendStaffMessage,
    sendCustomerMessage,
    disconnect,
    customerDisconnect,
    scrollToBottom,
    
    // Reset functions
    resetCustomerState: () => {
      isCustomer.value = false
      sessionId.value = null
      customerName.value = ''
      assignedStaff.value = null
      messages.value = []
      customerForm.value = {
        customerName: '',
        phone: '',
        email: '',
        message: ''
      }
      // Clear customer chat data when disconnecting
      customerChatData.value = null
    }
  }
}
