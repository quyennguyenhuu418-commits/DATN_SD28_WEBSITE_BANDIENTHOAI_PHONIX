<template>
  <div class="order-detail-page">
    <!-- Header -->
    <HeaderLayout/>

    <!-- Main Content -->
    <main class="order-detail-main">
      <div class="container">
        <!-- Order Header -->
        <div class="order-header">
          <div class="order-title">
            <h1>Chi tiết đơn hàng #{{ orderDetail.id }} - <span class="status-text">{{ getStatusText(orderDetail.status) }}</span></h1>
          </div>
          <div class="order-time">
            Đặt lúc: {{ formatDateTime(orderDetail.orderTime) }}
          </div>
        </div>

        <!-- Timeline -->
        <div class="timeline-section">
          <div class="timeline">
            <div 
              v-for="(step, index) in timelineSteps" 
              :key="index"
              :class="['timeline-step', { 
                'completed': step.completed, 
                'current': step.current,
                'pending': !step.completed && !step.current
              }]"
            >
              <div class="timeline-marker">
                <i :class="step.icon"></i>
              </div>
              <div class="timeline-content">
                <div class="timeline-title">{{ step.title }}</div>
                <div class="timeline-time" v-if="step.time">{{ step.time }}</div>
              </div>
              <div v-if="index < timelineSteps.length - 1" class="timeline-line"></div>
            </div>
          </div>
        </div>

        <!-- Information Sections -->
        <div class="info-sections">
          <!-- Delivery Information -->
          <div class="info-card">
            <div class="card-header">
              <i class="bi bi-geo-alt-fill card-icon"></i>
              <h3>THÔNG TIN NHẬN HÀNG</h3>
            </div>
            <div class="card-content">
              <div class="info-item">
                <span class="label">Người nhận:</span>
                <span class="value">{{ orderDetail.recipientName }} - {{ orderDetail.recipientPhone }}</span>
              </div>
              <div class="info-item">
                <span class="label">Nhận tại:</span>
                <span class="value">{{ orderDetail.deliveryAddress }}</span>
              </div>
              <div class="info-item">
                <span class="label">Nhận lúc:</span>
                <span class="value">{{ orderDetail.deliveryTime }}</span>
              </div>
            </div>
          </div>

          <!-- Payment Information -->
          <div class="info-card">
            <div class="card-header">
              <i class="bi bi-credit-card card-icon"></i>
              <h3>HÌNH THỨC THANH TOÁN</h3>
            </div>
            <div class="card-content">
              <div class="info-item">
                <span class="label">Phương thức:</span>
                <span class="value">{{ orderDetail.paymentMethod }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Product Information -->
        <div class="product-section">
          <div class="section-header">
            <i class="bi bi-bag card-icon"></i>
            <h3>THÔNG TIN SẢN PHẨM</h3>
          </div>
          <div class="product-list">
            <div v-for="product in orderDetail.products" :key="product.id" class="product-item">
              <div class="product-image">
                <img :src="product.image" :alt="product.name" />
              </div>
              <div class="product-details">
                <div class="product-name">{{ product.name }}</div>
                <div class="product-warranty" v-if="product.warranty">
                  Bảo hành: {{ product.warranty }}
                </div>
                <div class="product-quantity">Số lượng: {{ product.quantity }}</div>
              </div>
              <div class="product-price">{{ formatPrice(product.price) }}</div>
            </div>
          </div>
        </div>

        <!-- Order Summary -->
        <div class="order-summary">
          <div class="summary-item">
            <span class="label">Tạm tính:</span>
            <span class="value">{{ formatPrice(orderDetail.subtotal) }}</span>
          </div>
          <div class="summary-item">
            <span class="label">Tổng tiền:</span>
            <span class="value">{{ formatPrice(orderDetail.total) }}</span>
          </div>
          <div class="summary-item paid">
            <span class="label">Số tiền đã thanh toán:</span>
            <span class="value">{{ formatPrice(orderDetail.paidAmount) }}</span>
          </div>
        </div>

        <!-- Back Button -->
        <div class="back-section">
          <button class="btn-back" @click="goBack">
            VỀ TRANG DANH SÁCH ĐƠN HÀNG
          </button>
        </div>
      </div>
    </main>

    <!-- Footer -->
    <footer class="order-detail-footer">
      <FooterLayout/>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import HeaderLayout from './HeaderLayout.vue'
import FooterLayout from './FooterLayout.vue'

const router = useRouter()
const route = useRoute()

// Sample order detail data
const orderDetail = ref({
  id: '00850SO25090248857',
  status: 'completed',
  orderTime: '2025-09-23T10:23:00',
  recipientName: 'Anh Hùng',
  recipientPhone: '0971350995',
  deliveryAddress: 'Siêu thị 10-12 đường Võ Nguyên Giáp, Tổ 1, Phường Bắc Kạn, Tỉnh Thái Nguyên, Việt Nam',
  deliveryTime: 'Trước 10:24 - Thứ Ba (23/09)',
  paymentMethod: 'Thanh toán khi nhận hàng',
  products: [
    {
      id: 1,
      name: 'Tai nghe Bluetooth TWS Xiaomi Redmi Buds 6 Play Đen - Imei',
      image: '/placeholder-earbuds.jpg',
      warranty: 'Còn BH đến 22/09/2026',
      quantity: 1,
      price: 330000
    }
  ],
  subtotal: 330000,
  total: 330000,
  paidAmount: 330000
})

// Timeline steps
const timelineSteps = ref([
  {
    title: 'Đặt hàng',
    icon: 'bi bi-cart-check',
    completed: true,
    current: false,
    time: '10:23 - 23/09/2025'
  },
  {
    title: 'Xác nhận đơn hàng',
    icon: 'bi bi-check-circle',
    completed: true,
    current: false,
    time: '10:25 - 23/09/2025'
  },
  {
    title: 'Chuẩn bị hàng',
    icon: 'bi bi-box-seam',
    completed: true,
    current: false,
    time: '14:30 - 23/09/2025'
  },
  {
    title: 'Đang giao hàng',
    icon: 'bi bi-truck',
    completed: true,
    current: false,
    time: '08:00 - 24/09/2025'
  },
  {
    title: 'Đã giao hàng',
    icon: 'bi bi-check-circle-fill',
    completed: true,
    current: true,
    time: '15:30 - 24/09/2025'
  }
])

const getStatusText = (status) => {
  const statusMap = {
    'completed': 'Đã nhận hàng',
    'cancelled': 'Đã hủy',
    'return': 'Trả hàng/Hoàn tiền'
  }
  return statusMap[status] || 'Đã nhận hàng'
}

const formatPrice = (price) => {
  if (!price) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

const formatDateTime = (dateString) => {
  if (!dateString) return 'N/A'
  const date = new Date(dateString)
  return date.toLocaleString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit',
    weekday: 'long',
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  })
}

const goBack = () => {
  router.push('/theo-doi-don-hang')
}

onMounted(() => {
  // Load order detail based on route params if needed
  if (route.params.orderId) {
    // Load order detail from API
    console.log('Loading order detail for:', route.params.orderId)
  }
})
</script>

<style scoped>
.order-detail-page {
  min-height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
  padding-top: 77px; /* Compensate for fixed header */
}

.order-detail-main {
  flex: 1;
  padding: 3rem 0;
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 1rem;
  width: 100%;
}

/* Order Header */
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* Timeline Section */
.timeline-section {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.timeline {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  overflow-x: auto;
  padding-bottom: 1rem;
}

.timeline-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 120px;
  position: relative;
}

.timeline-marker {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  margin-bottom: 0.5rem;
  transition: all 0.3s;
}

.timeline-step.completed .timeline-marker {
  background: #28a745;
  color: white;
}

.timeline-step.current .timeline-marker {
  background: #FF5500;
  color: white;
  animation: pulse 2s infinite;
}

.timeline-step.pending .timeline-marker {
  background: #e9ecef;
  color: #6c757d;
}

.timeline-content {
  text-align: center;
  min-width: 100px;
}

.timeline-title {
  font-size: 0.85rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 0.25rem;
  line-height: 1.2;
}

.timeline-time {
  font-size: 0.75rem;
  color: #666;
  line-height: 1.2;
}

.timeline-line {
  position: absolute;
  top: 20px;
  left: 50%;
  width: 100%;
  height: 2px;
  background: #e9ecef;
  z-index: -1;
}

.timeline-step.completed + .timeline-step .timeline-line {
  background: #28a745;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(255, 85, 0, 0.7);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(255, 85, 0, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(255, 85, 0, 0);
  }
}

.order-title h1 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.status-text {
  color: #28a745;
  font-weight: 600;
}

.order-time {
  font-size: 0.9rem;
  color: #666;
  text-align: right;
}

/* Information Sections */
.info-sections {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.info-card {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 1.5rem;
  border: 1px solid #e9ecef;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.card-icon {
  color: #FF5500;
  font-size: 1.2rem;
}

.card-header h3 {
  font-size: 1rem;
  font-weight: 600;
  color: #333;
  margin: 0;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.card-content {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.info-item .label {
  font-weight: 600;
  color: #666;
  font-size: 0.9rem;
}

.info-item .value {
  color: #333;
  font-size: 0.95rem;
  line-height: 1.4;
}

/* Product Section */
.product-section {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 1.5rem;
  border: 1px solid #e9ecef;
  margin-bottom: 2rem;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.section-header h3 {
  font-size: 1rem;
  font-weight: 600;
  color: #333;
  margin: 0;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.product-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.product-item {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 1rem;
  background: white;
  border-radius: 6px;
  border: 1px solid #e9ecef;
}

.product-image {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  overflow: hidden;
  background: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.product-name {
  font-weight: 500;
  color: #333;
  line-height: 1.4;
}

.product-warranty {
  font-size: 0.9rem;
  color: #28a745;
  font-weight: 500;
}

.product-quantity {
  font-size: 0.9rem;
  color: #666;
}

.product-price {
  font-weight: 600;
  color: #333;
  font-size: 1.1rem;
  text-align: right;
  align-self: flex-start;
}

/* Order Summary */
.order-summary {
  background: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 2rem;
  max-width: 400px;
  margin-left: auto;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 0;
  border-bottom: 1px solid #f0f0f0;
}

.summary-item:last-child {
  border-bottom: none;
}

.summary-item .label {
  color: #666;
  font-size: 0.9rem;
}

.summary-item .value {
  font-weight: 600;
  color: #333;
}

.summary-item.paid .value {
  color: #FF5500;
  font-weight: 700;
}

/* Back Button */
.back-section {
  text-align: center;
  margin-top: 2rem;
}

.btn-back {
  background: transparent;
  color: #FF5500;
  border: 2px solid #FF5500;
  padding: 0.75rem 2rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 0.9rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.btn-back:hover {
  background: #FF5500;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 85, 0, 0.3);
}

.order-detail-footer {
  background: #000000;
  color: white;
  text-align: center;
  padding: 2rem;
  margin-top: auto;
}

/* Responsive */
@media (max-width: 768px) {
  .order-detail-main {
    padding: 2rem 0;
  }
  
  .container {
    padding: 0 1rem;
  }
  
  .order-header {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }
  
  .timeline {
    gap: 0.5rem;
  }
  
  .timeline-step {
    min-width: 100px;
  }
  
  .timeline-marker {
    width: 35px;
    height: 35px;
    font-size: 1rem;
  }
  
  .timeline-title {
    font-size: 0.8rem;
  }
  
  .timeline-time {
    font-size: 0.7rem;
  }
  
  .info-sections {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  
  .product-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .product-price {
    align-self: flex-end;
  }
  
  .order-summary {
    max-width: 100%;
    margin-left: 0;
  }
}

@media (max-width: 480px) {
  .order-header {
    padding: 1.5rem;
  }
  
  .order-title h1 {
    font-size: 1.2rem;
  }
  
  .info-card,
  .product-section {
    padding: 1rem;
  }
  
  .product-item {
    padding: 0.75rem;
  }
}
</style>
