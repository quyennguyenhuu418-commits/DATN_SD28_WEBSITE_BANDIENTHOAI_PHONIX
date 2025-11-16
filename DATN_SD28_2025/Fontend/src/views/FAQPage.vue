<template>
  <div class="faq-page">
    <PosHeader 
      pageTitle="FAQ"
      pageSubtitle="Câu hỏi thường gặp"
    />
    
    <main class="faq-main">
      <div class="faq-container">
        <div class="faq-header">
          <h1 class="faq-title">FAQ</h1>
          <p class="faq-subtitle">Câu hỏi thường gặp - Tìm câu trả lời cho các câu hỏi phổ biến về hệ thống PhoniX POS</p>
        </div>

        <div class="faq-list">
          <div 
            v-for="(faq, index) in faqs" 
            :key="index" 
            class="faq-item"
            @click="toggleFaq(index)"
          >
            <div class="faq-question">
              <span>{{ faq.question }}</span>
              <svg 
                class="faq-icon" 
                :class="{ 'rotated': activeIndex === index }"
                fill="none" 
                stroke="currentColor" 
                viewBox="0 0 24 24"
              >
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
              </svg>
            </div>
            <div 
              class="faq-answer" 
              :class="{ 'expanded': activeIndex === index }"
            >
              <p>{{ faq.answer }}</p>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import PosHeader from '../components/PosHeader.vue'

const activeIndex = ref(null)

const faqs = ref([
  {
    question: 'Hệ thống PhoniX POS là gì?',
    answer: 'PhoniX POS là hệ thống quản lý bán hàng điểm bán hàng (Point of Sale) được thiết kế chuyên biệt cho cửa hàng điện thoại. Hệ thống giúp quản lý sản phẩm, khách hàng, đơn hàng, nhân viên và các hoạt động kinh doanh một cách hiệu quả.'
  },
  {
    question: 'Làm thế nào để thêm sản phẩm mới vào hệ thống?',
    answer: 'Để thêm sản phẩm mới, bạn truy cập vào menu "Sản phẩm", click nút "Thêm sản phẩm mới", điền đầy đủ thông tin như tên sản phẩm, giá bán, số lượng, hình ảnh và các thuộc tính chi tiết (thương hiệu, màu sắc, RAM, ROM, chip, v.v.), sau đó nhấn "Lưu".'
  },
  {
    question: 'Cách tạo đơn hàng mới như thế nào?',
    answer: 'Bạn vào mục "Bán hàng" hoặc "POS", chọn sản phẩm muốn bán, nhập số lượng, có thể áp dụng giảm giá nếu có, chọn phương thức thanh toán và nhấn "Thanh toán". Hệ thống sẽ tự động tạo hóa đơn.'
  },
  {
    question: 'Làm sao để quản lý phiếu giảm giá và khuyến mãi?',
    answer: 'Bạn vào menu "Giảm giá" > "Phiếu giảm giá" để tạo các mã giảm giá. Bạn có thể thiết lập phần trăm giảm giá, giá trị giảm tối đa, ngày hiệu lực và số lượng sử dụng. Sau đó tạo "Đợt giảm giá" để áp dụng hàng loạt cho nhiều sản phẩm.'
  },
  {
    question: 'Hệ thống có hỗ trợ quản lý nhân viên không?',
    answer: 'Có, bạn vào menu "Nhân viên" để xem danh sách, thêm mới, chỉnh sửa thông tin nhân viên. Bạn có thể phân quyền cho từng nhân viên và theo dõi hiệu suất làm việc của họ.'
  },
  {
    question: 'Làm thế nào để xem báo cáo và thống kê doanh thu?',
    answer: 'Bạn vào menu "Thống kê" để xem các báo cáo chi tiết về doanh thu, sản phẩm bán chạy, khách hàng, nhân viên. Có thể lọc theo thời gian (ngày, tuần, tháng, năm) và xuất ra file Excel để phân tích.'
  },
  {
    question: 'Cách quản lý ca làm việc và giao ca?',
    answer: 'Bạn vào menu "Quản lý ca làm việc" để tạo ca làm việc, sau đó vào "Phân ca" để gán nhân viên vào các ca. Khi kết thúc ca, nhân viên vào "Giao ca" để cập nhật số tiền fall hiện, doanh thu và ghi chú.'
  },
  {
    question: 'Làm sao để thêm hoặc sửa thông tin khách hàng?',
    answer: 'Vào menu "Khách hàng", bạn có thể xem danh sách, thêm khách hàng mới hoặc chỉnh sửa thông tin của khách hàng hiện có. Hệ thống cũng lưu lịch sử mua hàng của mỗi khách hàng.'
  },
  {
    question: 'Hệ thống có hỗ trợ chat/ hỗ trợ khách hàng không?',
    answer: 'Có, bạn vào "Chat Hỗ Trợ" để nhắn tin với khách hàng, trả lời các câu hỏi và tư vấn sản phẩm. Hệ thống cũng tích hợp chatbot tự động để hỗ trợ 24/7.'
  },
  {
    question: 'Tôi có thể xuất dữ liệu ra Excel không?',
    answer: 'Có, hầu hết các trang danh sách (sản phẩm, khách hàng, đơn hàng, v.v.) đều có nút "Xuất Excel" để tải dữ liệu về máy tính phục vụ cho việc báo cáo và phân tích.'
  },
  {
    question: 'Làm thế nào để thêm thuộc tính mới cho sản phẩm?',
    answer: 'Vào menu "Thuộc tính", bạn có thể thêm các thuộc tính như: Hãng, Hệ điều hành, Màu sắc, Màn hình, Chip, RAM, ROM, CPU, GPU, Pin, Camera trước/sau. Sau đó các thuộc tính này sẽ xuất hiện khi thêm/sửa sản phẩm.'
  },
  {
    question: 'Hệ thống có giới hạn số lượng sản phẩm hay khách hàng không?',
    answer: 'Không, hệ thống PhoniX POS không giới hạn số lượng sản phẩm, khách hàng hay đơn hàng. Bạn có thể sử dụng thoải mái cho cửa hàng của mình.'
  }
])

const toggleFaq = (index) => {
  if (activeIndex.value === index) {
    activeIndex.value = null
  } else {
    activeIndex.value = index
  }
}
</script>

<style scoped>
.faq-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
}

.faq-main {
  margin-top: 80px;
}

.faq-container {
  max-width: 100%;
  margin: 0 auto;
  padding: 32px 40px;
}

.faq-header {
  text-align: center;
  margin-bottom: 3rem;
  padding: 0;
}

.faq-title {
  font-size: 2.5rem;
  font-weight: 800;
  color: #1f2937;
  margin-bottom: 1rem;
}

.faq-subtitle {
  font-size: 1.125rem;
  color: #6b7280;
}

.faq-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 0;
}

.faq-item {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.07);
  border: 1px solid #e5e7eb;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
}

.faq-item:hover {
  box-shadow: 0 8px 15px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.faq-question {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  font-size: 1.125rem;
  font-weight: 600;
  color: #1f2937;
}

.faq-icon {
  width: 1.5rem;
  height: 1.5rem;
  color: #6b7280;
  transition: transform 0.3s ease;
}

.faq-icon.rotated {
  transform: rotate(180deg);
}

.faq-answer {
  max-height: 0;
  overflow: hidden;
  transition: max-height 0.3s ease, padding 0.3s ease;
  background: #f8f9fa;
}

.faq-answer.expanded {
  max-height: 500px;
  padding: 0 1.5rem 1.5rem 1.5rem;
}

.faq-answer p {
  margin: 0;
  color: #4b5563;
  line-height: 1.6;
}

/* Responsive */
@media (max-width: 768px) {
  .faq-main {
    padding: 1rem;
  }
  
  .faq-title {
    font-size: 2rem;
  }
  
  .faq-question {
    font-size: 1rem;
    padding: 1rem;
  }
}
</style>
