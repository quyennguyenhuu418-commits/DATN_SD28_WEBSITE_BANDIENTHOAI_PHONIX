// API Configuration
const API_BASE_URL = 'http://localhost:8080/api'

// Utility functions
const showMessage = (message, type = 'info') => {
  const messageDiv = document.createElement('div')
  messageDiv.className = `message ${type}`
  messageDiv.textContent = message
  messageDiv.style.cssText = `
		position: fixed;
		top: 20px;
		right: 20px;
		padding: 15px 20px;
		border-radius: 5px;
		color: white;
		z-index: 10000;
		background: ${type === 'error' ? '#e74c3c' : type === 'success' ? '#27ae60' : '#3498db'};
	`
  document.body.appendChild(messageDiv)
  setTimeout(() => messageDiv.remove(), 5000)
}

const validateEmail = (email) => {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return re.test(email)
}

const validatePassword = (password) => {
  return password.length >= 6
}

// Validation functions
const validatePhone = (phone) => {
  const phoneRegex = /^[0-9]{10,11}$/
  return phoneRegex.test(phone.replace(/\s/g, ''))
}

// Check if email exists
const checkEmailExists = async (email) => {
  try {
    const response = await fetch(
      `${API_BASE_URL}/api/customer/auth/check-email?email=${encodeURIComponent(email)}`,
    )
    const data = await response.json()
    return data.exists
  } catch (error) {
    console.error('Error checking email:', error)
    return false
  }
}

// Check if phone exists
const checkPhoneExists = async (phone) => {
  try {
    const response = await fetch(
      `${API_BASE_URL}/api/customer/auth/check-phone?soDienThoai=${encodeURIComponent(phone)}`,
    )
    const data = await response.json()
    return data.exists
  } catch (error) {
    console.error('Error checking phone:', error)
    return false
  }
}

// Authentication functions
const loginUser = async (taiKhoan, matKhau) => {
  try {
    const response = await fetch(`${API_BASE_URL}/api/customer/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ taiKhoan, matKhau }),
    })

    const data = await response.json()

    if (response.ok && data.success) {
      localStorage.setItem('customer_token', data.token)
      localStorage.setItem('customer_user', JSON.stringify(data.user))
      showMessage('Đăng nhập thành công!', 'success')

      // Customer redirect to home
      window.location.href = window.location.origin + '/'
    } else {
      showMessage(data.message || 'Đăng nhập thất bại!', 'error')
    }
  } catch (error) {
    showMessage('Lỗi kết nối! Vui lòng thử lại.', 'error')
    console.error('Login error:', error)
  }
}

const registerUser = async (hoTen, email, soDienThoai, matKhau) => {
  try {
    const response = await fetch(`${API_BASE_URL}/api/customer/auth/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        hoTen,
        email,
        matKhau,
        taiKhoan: email, // Sử dụng email làm tài khoản
        soDienThoai: soDienThoai, // Số điện thoại từ form
        gioiTinh: 'Nam', // Mặc định là Nam
        ngaySinh: null, // null được chấp nhận
      }),
    })

    const data = await response.json()

    if (response.ok && data.success) {
      showMessage('Đăng ký thành công! Vui lòng đăng nhập.', 'success')
      // Switch to login form
      document.getElementById('container').classList.remove('right-panel-active')
    } else {
      showMessage(data.message || 'Đăng ký thất bại!', 'error')
    }
  } catch (error) {
    showMessage('Lỗi kết nối! Vui lòng thử lại.', 'error')
    console.error('Register error:', error)
  }
}

// Google OAuth
const initGoogleAuth = () => {
  if (typeof google !== 'undefined' && google.accounts) {
    google.accounts.id.initialize({
      client_id: '221883949331-14j4ji8h5b6mlhe7hf8d455hg2hk6vq2.apps.googleusercontent.com', // Your Google Client ID
      callback: handleGoogleResponse,
    })
  }
}

const handleGoogleResponse = async (response) => {
  try {
    const res = await fetch(`${API_BASE_URL}/api/customer/auth/google-login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ googleToken: response.credential }),
    })

    const data = await res.json()

    if (res.ok && data.success) {
      localStorage.setItem('customer_token', data.token)
      localStorage.setItem('customer_user', JSON.stringify(data.user))
      showMessage('Đăng nhập Google thành công!', 'success')

      // Customer redirect to home
      window.location.href = window.location.origin + '/'
    } else {
      showMessage('Đăng nhập Google thất bại!', 'error')
    }
  } catch (error) {
    showMessage('Lỗi đăng nhập Google!', 'error')
    console.error('Google auth error:', error)
  }
}

// Facebook OAuth removed - using Google only

// Forgot password functions
const sendOTP = async (email) => {
  try {
    const response = await fetch(`${API_BASE_URL}/customer/auth/forgot-password`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ email }),
    })

    const data = await response.json()

    if (response.ok && data.success) {
      showMessage(data.message || 'Mã OTP đã được gửi đến email của bạn!', 'success')
      // Store email and resetToken for verification
      sessionStorage.setItem('resetEmail', email)
      if (data.resetToken) {
        sessionStorage.setItem('resetToken', data.resetToken)
      }
      window.location.href = 'VeryfiMail.html'
    } else {
      showMessage(data.message || 'Gửi OTP thất bại!', 'error')
    }
  } catch (error) {
    showMessage('Lỗi gửi OTP! Vui lòng thử lại.', 'error')
    console.error('Send OTP error:', error)
  }
}

const verifyOTP = async (otp) => {
  const email = sessionStorage.getItem('resetEmail')
  if (!email) {
    showMessage('Email không tồn tại! Vui lòng thử lại.', 'error')
    return false
  }

  try {
    const response = await fetch(`${API_BASE_URL}/customer/auth/verify-otp`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ email, otp }),
    })

    const data = await response.json()

    if (response.ok && data.success) {
      showMessage(data.message || 'Xác minh OTP thành công!', 'success')
      // Store resetToken from response for password reset
      if (data.resetToken) {
        sessionStorage.setItem('resetToken', data.resetToken)
      }
      window.location.href = 'CreatePassword.html'
      return true
    } else {
      showMessage(data.message || 'Mã OTP không đúng!', 'error')
      return false
    }
  } catch (error) {
    showMessage('Lỗi xác minh OTP!', 'error')
    console.error('Verify OTP error:', error)
    return false
  }
}

const resetPassword = async (newPassword, confirmPassword) => {
  if (newPassword !== confirmPassword) {
    showMessage('Mật khẩu xác nhận không khớp!', 'error')
    return
  }

  if (!validatePassword(newPassword)) {
    showMessage('Mật khẩu phải có ít nhất 6 ký tự!', 'error')
    return
  }

  const resetToken = sessionStorage.getItem('resetToken')
  if (!resetToken) {
    showMessage('Token không tồn tại! Vui lòng thực hiện lại quy trình quên mật khẩu.', 'error')
    window.location.href = 'forgotpassword.html'
    return
  }

  try {
    const response = await fetch(`${API_BASE_URL}/customer/auth/reset-password`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ 
        resetToken: resetToken,
        newPassword: newPassword,
        confirmPassword: confirmPassword
      }),
    })

    const data = await response.json()

    if (response.ok && data.success) {
      showMessage(data.message || 'Đặt lại mật khẩu thành công!', 'success')
      // Clear session storage
      sessionStorage.removeItem('resetEmail')
      sessionStorage.removeItem('resetToken')
      setTimeout(() => {
        window.location.href = 'login.html'
      }, 1500)
    } else {
      showMessage(data.message || 'Đặt lại mật khẩu thất bại!', 'error')
    }
  } catch (error) {
    showMessage('Lỗi đặt lại mật khẩu!', 'error')
    console.error('Reset password error:', error)
  }
}

// Event listeners
document.addEventListener('DOMContentLoaded', () => {
  const signUpButton = document.getElementById('signUp')
  const signInButton = document.getElementById('signIn')
  const container = document.getElementById('container')

  if (!container) {
    console.warn('Container element with id "container" not found.')
    return
  }

  // Panel switching
  if (signUpButton) {
    signUpButton.addEventListener('click', () => {
      container.classList.add('right-panel-active')
    })
  }

  if (signInButton) {
    signInButton.addEventListener('click', () => {
      container.classList.remove('right-panel-active')
    })
  }

  // Login form
  const loginForm = document.querySelector('.sign-in-container form')
  if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
      e.preventDefault()
      const taiKhoan = loginForm.querySelector('input[type="email"]').value
      const matKhau = loginForm.querySelector('input[type="password"]').value

      if (!validateEmail(taiKhoan)) {
        showMessage('Tài khoản không hợp lệ!', 'error')
        return
      }

      if (!validatePassword(matKhau)) {
        showMessage('Mật khẩu phải có ít nhất 6 ký tự!', 'error')
        return
      }

      await loginUser(taiKhoan, matKhau)
    })
  }

  // Register form
  const registerForm = document.querySelector('.sign-up-container form')
  if (registerForm) {
    registerForm.addEventListener('submit', async (e) => {
      e.preventDefault()
      const name = registerForm.querySelector('input[type="text"]').value
      const email = registerForm.querySelector('input[type="email"]').value
      const phone = registerForm.querySelector('input[type="tel"]').value
      const password = registerForm.querySelector('input[type="password"]').value

      if (!name.trim()) {
        showMessage('Vui lòng nhập họ tên!', 'error')
        return
      }

      if (!validateEmail(email)) {
        showMessage('Email không hợp lệ!', 'error')
        return
      }

      if (!phone.trim()) {
        showMessage('Vui lòng nhập số điện thoại!', 'error')
        return
      }

      if (!validatePhone(phone)) {
        showMessage('Số điện thoại không hợp lệ! (10-11 chữ số)', 'error')
        return
      }

      if (!validatePassword(password)) {
        showMessage('Mật khẩu phải có ít nhất 6 ký tự!', 'error')
        return
      }

      // Check for duplicates
      const [emailExists, phoneExists] = await Promise.all([
        checkEmailExists(email),
        checkPhoneExists(phone),
      ])

      if (emailExists) {
        showMessage('Email đã được sử dụng!', 'error')
        return
      }

      if (phoneExists) {
        showMessage('Số điện thoại đã được sử dụng!', 'error')
        return
      }

      await registerUser(name, email, phone, password)
    })
  }

  // Social login buttons
  const googleButtons = document.querySelectorAll('.social[title="Google"]')
  googleButtons.forEach((button) => {
    button.addEventListener('click', async (e) => {
      e.preventDefault()
      try {
        // Get Google OAuth URL from backend
        const response = await fetch(`${API_BASE_URL}/api/customer/auth/google-url`)
        const data = await response.json()

        if (data.success) {
          // Redirect to Google OAuth
          window.location.href = data.googleUrl
        } else {
          showMessage('Lỗi lấy URL Google OAuth!', 'error')
        }
      } catch (error) {
        showMessage('Lỗi kết nối!', 'error')
        console.error('Google OAuth URL error:', error)
      }
    })
  })

  // Initialize OAuth
  initGoogleAuth()

  // Real-time validation for register form
  const emailInput = registerForm?.querySelector('input[type="email"]')
  const phoneInput = registerForm?.querySelector('input[type="tel"]')

  if (emailInput) {
    let emailTimeout
    emailInput.addEventListener('input', () => {
      clearTimeout(emailTimeout)
      emailTimeout = setTimeout(async () => {
        const email = emailInput.value.trim()
        if (email && validateEmail(email)) {
          const exists = await checkEmailExists(email)
          if (exists) {
            showMessage('Email đã được sử dụng!', 'error')
          }
        }
      }, 1000) // Debounce 1 second
    })
  }

  if (phoneInput) {
    let phoneTimeout
    phoneInput.addEventListener('input', () => {
      clearTimeout(phoneTimeout)
      phoneTimeout = setTimeout(async () => {
        const phone = phoneInput.value.trim()
        if (phone && validatePhone(phone)) {
          const exists = await checkPhoneExists(phone)
          if (exists) {
            showMessage('Số điện thoại đã được sử dụng!', 'error')
          }
        }
      }, 1000) // Debounce 1 second
    })
  }
})
