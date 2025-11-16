// Utility để bật/tắt Facebook Messenger
export const MessengerToggle = {
  // Ẩn Messenger
  hide() {
    localStorage.setItem('messenger-hidden', 'true')
    this.applyToggle()
  },
  
  // Hiện Messenger
  show() {
    localStorage.removeItem('messenger-hidden')
    this.applyToggle()
  },
  
  // Kiểm tra trạng thái
  isHidden() {
    return localStorage.getItem('messenger-hidden') === 'true'
  },
  
  // Áp dụng toggle
  applyToggle() {
    const isHidden = this.isHidden()
    const messengerElements = document.querySelectorAll('.facebook-messenger-floating, .messenger-manager')
    
    messengerElements.forEach(element => {
      if (isHidden) {
        element.style.display = 'none'
        element.style.visibility = 'hidden'
        element.style.opacity = '0'
      } else {
        element.style.display = ''
        element.style.visibility = ''
        element.style.opacity = ''
      }
    })
  },
  
  // Toggle trạng thái
  toggle() {
    if (this.isHidden()) {
      this.show()
    } else {
      this.hide()
    }
  },
  
  // Khởi tạo
  init() {
    this.applyToggle()
    
    // Lắng nghe sự kiện từ console
    window.messengerToggle = this
  }
}

// Auto init khi load
if (typeof window !== 'undefined') {
  window.addEventListener('DOMContentLoaded', () => {
    MessengerToggle.init()
  })
}

// Console commands
if (typeof window !== 'undefined') {
  window.hideMessenger = () => MessengerToggle.hide()
  window.showMessenger = () => MessengerToggle.show()
  window.toggleMessenger = () => MessengerToggle.toggle()
}







