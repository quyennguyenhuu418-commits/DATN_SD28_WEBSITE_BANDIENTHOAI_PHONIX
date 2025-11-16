<template>
  <footer class="main-footer">
    <div class="footer-container">
      <div class="footer-grid">
        <div class="footer-col">
          <h3 class="footer-logo">
            <img src="/niX%20(1).png" alt="PhoniX" style="height: 50px; width: auto;">
          </h3>
          <p class="footer-desc">
            Hệ thống cửa hàng PhoniX chuyên bán lẻ điện thoại - Giá tốt, giao miễn phí.
          </p>
          <div class="contact-info">
            <p><i class="bi bi-geo-alt-fill"></i> Số 49, ngách 230/55 , Đồng Me, Mễ Trì Thượng </p>
            <p><i class="bi bi-telephone-fill"></i> <strong>0919965342</strong></p>
            <p><i class="bi bi-envelope-fill"></i> info@phonix.vn</p>
          </div>
        </div>

        <div class="footer-col">
          <h4 class="footer-title">Chính sách</h4>
          <ul class="footer-links">
            <li><a href="#">Chính sách mua hàng</a></li>
            <li><a href="#">Chính sách đổi trả</a></li>
            <li><a href="#">Chính sách vận chuyển</a></li>
            <li><a href="#">Chính sách bảo mật</a></li>
            <li><a href="#">Cam kết cửa hàng</a></li>
          </ul>
        </div>

        <div class="footer-col">
          <h4 class="footer-title">Hướng dẫn</h4>
          <ul class="footer-links">
            <li><a href="#">Hướng dẫn mua hàng</a></li>
            <li><a href="#">Hướng dẫn đổi trả</a></li>
            <li><a href="#">Hướng dẫn chuyển khoản</a></li>
            <li><a href="#">Hướng dẫn trả góp</a></li>
            <li><a href="#">Tra cứu đơn hàng</a></li>
          </ul>
        </div>

        <div class="footer-col">
          <h4 class="footer-title">Kết nối với chúng tôi</h4>
          <div class="social-links">
            <a href="#" class="social-link facebook"><i class="bi bi-facebook"></i></a>
            <a href="#" class="social-link instagram"><i class="bi bi-instagram"></i></a>
            <a href="#" class="social-link youtube"><i class="bi bi-youtube"></i></a>
            <a href="#" class="social-link tiktok"><i class="bi bi-tiktok"></i></a>
          </div>

          <h4 class="footer-title" style="margin-top: 2rem;">Phương thức thanh toán</h4>
          <div class="payment-methods">
            <div class="payment-badge">ZaloPay</div>
            <div class="payment-badge">VNPAY</div>
            <div class="payment-badge">ATM</div>
            <div class="payment-badge">Visa</div>
            <div class="payment-badge">MasterCard</div>
          </div>
        </div>
      </div>

      <div class="footer-bottom">
        <p>&copy; 2025 PhoniX. All rights reserved. | Designed with ❤️ by PhoniX Team</p>
      </div>
    </div>
  </footer>

  <!-- Floating Buttons -->
  <div class="floating-buttons">
    <button class="floating-btn scroll-top" @click="scrollToTop" v-if="showScrollTop">
      <i class="bi bi-arrow-up"></i>
    </button>
    <!-- Removed Zalo and Messenger floating buttons per request -->
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

// State
const showScrollTop = ref(false)

// Methods
const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// Scroll listener
const handleScroll = () => {
  showScrollTop.value = window.scrollY > 500
}

// Lifecycle
onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
/* Phoenix Theme Colors */
:root {
  --phoenix-primary: #FF6B35;
  --phoenix-secondary: #F7931E;
  --phoenix-accent: #DC143C;
  --phoenix-gold: #FFD700;
  --phoenix-dark: #2C1810;
  --phoenix-light: #FFF5E1;
}

/* Footer */
.main-footer {
  background: #000000;
  color: white;
  padding: 3rem 0 1rem;
}

.footer-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 2rem;
}

.footer-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2.5rem;
  margin-bottom: 2rem;
}

.footer-logo {
  margin-bottom: 1rem;
}

.footer-desc {
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 1.5rem;
}

.contact-info p {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
  color: rgba(255, 255, 255, 0.8);
}

.contact-info i {
  color: #FFD700;
  font-size: 1.1rem;
}

.footer-title {
  color: #FFD700;
  font-size: 1.125rem;
  margin-bottom: 1.5rem;
  font-weight: 700;
}

.footer-links {
  list-style: none;
}

.footer-links li {
  margin-bottom: 0.75rem;
}

.footer-links a {
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  transition: color 0.2s;
}

.footer-links a:hover {
  color: #F7931E;
}

.social-links {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
}

.social-link {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.3rem;
  text-decoration: none;
  transition: all 0.3s;
}

.social-link:hover {
  background: #FF5500;
  transform: translateY(-3px);
}

.payment-methods {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

/* Payment Badge (thay vì placeholder images) */
.payment-badge {
  padding: 0.5rem 1rem;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  font-size: 0.85rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  transition: all 0.3s;
}

.payment-badge:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: #FFD700;
  color: #FFD700;
}

.footer-bottom {
  text-align: center;
  padding-top: 2rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.6);
}

/* Floating Buttons */
.floating-buttons {
  position: fixed;
  right: 2rem;
  bottom: 2rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  z-index: 999;
}

.floating-btn {
  width: 55px;
  height: 55px;
  border-radius: 50%;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  transition: all 0.3s;
  text-decoration: none;
}

.floating-btn:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.3);
}

.floating-btn.scroll-top {
  background: #DC143C;
}

.floating-btn.zalo {
  background: #0068ff;
}

.floating-btn.messenger {
  background: #0084ff;
}

/* Responsive */
@media (max-width: 768px) {
  .footer-grid {
    grid-template-columns: 1fr;
  }

  .floating-buttons {
    right: 1rem;
    bottom: 1rem;
  }

  .floating-btn {
    width: 50px;
    height: 50px;
    font-size: 1.3rem;
  }
}
</style>

