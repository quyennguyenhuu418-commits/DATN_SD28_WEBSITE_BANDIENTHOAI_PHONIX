<template>
  <AdminTable
    :data="reviews"
    :columns="columns"
    title="Quản Lý Đánh Giá"
    titleIcon="⭐"
    entityName="Đánh giá"
    searchPlaceholder="Tìm kiếm theo tên người dùng hoặc nội dung..."
    :customStatusOptions="customStatusOptions"
    :hideDefaultStatusOptions="['inactive']"
    @openForm="openForm"
    @toggleStatus="toggleReviewStatus"
  >

    <template #action-buttons="{ item }">
      <div class="action-buttons">
        <div class="status-toggle">
          <label class="toggle-switch" :title="getToggleTooltip(item)">
            <input
              type="checkbox"
              :checked="getToggleChecked(item)"
              @change="toggleReviewStatus(item)"
              :disabled="isUpdatingStatus"
            />
            <span class="toggle-slider"></span>
          </label>
        </div>
      </div>
    </template>

    <!-- Custom rating cell -->
    <template #cell-rating="{ item }">
      <div class="rating-display">
        <span class="rating-stars" :title="`${item.rating}/5 sao`">
          <span v-for="i in 5" :key="i" :class="{ 'star-filled': i <= item.rating, 'star-empty': i > item.rating }">
            ★
          </span>
        </span>
        <span class="rating-number">{{ item.rating }}/5</span>
      </div>
    </template>

    <!-- Custom status cell -->
    <template #cell-trangThai="{ item }">
      <span :class="getReviewStatusClass(item)" class="review-status-text">
        {{ getReviewStatusText(item) }}
      </span>
    </template>

  </AdminTable>


  <FormModal
    :show="showForm"
    :title="editingReview ? 'Sửa Đánh giá' : 'Thêm Đánh giá'"
    :fields="reviewFields"
    :initial-data="editingReview ? {
      idSanPham: editingReview.idSanPham,
      idNguoiDung: editingReview.idNguoiDung,
      tenNguoiDung: editingReview.tenNguoiDung,
      rating: editingReview.rating,
      comment: editingReview.comment,
      trangThai: editingReview.trangThai
    } : undefined"
    @submit="handleFormSubmit"
    @cancel="showForm = false"
  />

  <Toast ref="toastRef" />
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import api from '@/services/api'
import FormModal from '@/components/FormModal.vue'
import Toast from '@/components/Toast.vue'
import AdminTable from '@/components/AdminTable.vue'

interface Review {
  reviewId: number
  idSanPham: number
  idNguoiDung?: number
  tenNguoiDung?: string
  rating: number
  comment: string
  ngayTao: string
  trangThai: number // 0: CHO_DUYET, 1: DA_DUYET, 2: TU_CHOI
  ngayDuyet?: string
  ghiChuDuyet?: string
}

const reviews = ref<Review[]>([])
const loading = ref(false)
const showForm = ref(false)
const editingReview = ref<Review | null>(null)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)
const isUpdatingStatus = ref(false)

// Custom status options for reviews
const customStatusOptions = [
  { value: 'pending', label: 'Chờ duyệt', statusValue: 0 },
  { value: 'rejected', label: 'Từ chối', statusValue: 2 }
]

// Table columns configuration
const columns = [
  { key: 'reviewId', label: 'ID', class: 'id-col', type: 'code' as const },
  { key: 'tenNguoiDung', label: 'Người đánh giá', class: 'user-col' },
  { key: 'idSanPham', label: 'Sản phẩm ID', class: 'product-col' },
  { key: 'rating', label: 'Đánh giá', class: 'rating-col' },
  { key: 'comment', label: 'Nội dung', class: 'comment-col' },
  { key: 'ngayTao', label: 'Ngày tạo', class: 'date-col', type: 'date' as const },
  { key: 'trangThai', label: 'Trạng thái', class: 'status-col', type: 'status' as const }
]


const reviewFields = [
  { key: 'idSanPham', label: 'ID Sản phẩm', type: 'number' as const, required: true },
  { key: 'idNguoiDung', label: 'ID Người dùng', type: 'number' as const },
  { key: 'tenNguoiDung', label: 'Tên người dùng', type: 'text' as const },
  { key: 'rating', label: 'Đánh giá', type: 'rating' as const, required: true },
  { key: 'comment', label: 'Nội dung', type: 'textarea' as const, required: true },
  {
    key: 'trangThai',
    label: 'Trạng thái',
    type: 'radio' as const,
    options: [
      { value: 0, label: 'Chờ duyệt' },
      { value: 1, label: 'Đã duyệt' }
    ]
  }
]

async function loadReviews() {
  loading.value = true
  try {
    // Load tất cả reviews (bao gồm cả đã duyệt, từ chối, chờ duyệt)
    const { data } = await api.get<Review[]>('/api/reviews/admin/all')
    console.log('All reviews:', data)
    console.log('Reviews count:', data?.length)

    // Debug: Log each review status
    if (data && data.length > 0) {
      data.forEach((review, index) => {
        console.log(`Review ${index + 1}:`, {
          id: review.reviewId,
          status: review.trangThai,
          name: review.tenNguoiDung,
          fullReview: review
        })

        // Debug toggle state
        const toggleState = review.trangThai === 1 // 1: DA_DUYET
        console.log(`Toggle state for review ${review.reviewId}:`, toggleState, '(should be true for DA_DUYET)')
      })
    }

    reviews.value = data
  } catch (error) {
    console.error('Error loading reviews:', error)
    reviews.value = []
  } finally {
    loading.value = false
  }
}


function openForm(review?: Review) {
  editingReview.value = review || null
  showForm.value = true
}

async function handleFormSubmit(data: any) {
  try {
    if (editingReview.value) {
      await api.put(`/api/reviews/${editingReview.value.reviewId}`, data)
      toastRef.value?.success('Thành công', 'Cập nhật đánh giá thành công!')
    } else {
      await api.post('/api/reviews', data)
      toastRef.value?.success('Thành công', 'Thêm đánh giá thành công!')
    }
    showForm.value = false
    await loadReviews()
  } catch (error: any) {
    console.error('Lỗi khi lưu:', error)
    if (error.response?.data) {
      toastRef.value?.error('Lỗi lưu Đánh giá', error.response.data)
    } else {
      toastRef.value?.error('Lỗi lưu Đánh giá', 'Có lỗi xảy ra khi lưu đánh giá')
    }
  }
}




// Toggle trạng thái đánh giá
async function toggleReviewStatus(review: Review) {
  console.log('Toggle called for review:', review.reviewId, 'Current status:', review.trangThai)

  // Tránh duplicate nếu đang xử lý
  if (isUpdatingStatus.value) {
    console.log('Already updating, skipping...')
    return
  }

  isUpdatingStatus.value = true
  try {
    let newStatus: number
    let action: string
    let message: string

    const userName = review.tenNguoiDung || 'Khách'
    const currentStatus = review.trangThai ?? 0 // Default to 0: CHO_DUYET if undefined

    console.log('Processing toggle for:', userName, 'Current status:', currentStatus)

    if (currentStatus === 0) { // CHO_DUYET
      // Duyệt đánh giá mới
      newStatus = 1 // DA_DUYET
      action = 'duyet'
      message = `Đã duyệt đánh giá của "${userName}"`
    } else if (currentStatus === 1) { // DA_DUYET
      // Từ chối đánh giá đã duyệt
      newStatus = 2 // TU_CHOI
      action = 'tu-choi'
      message = `Đã từ chối đánh giá của "${userName}"`
    } else if (currentStatus === 2) { // TU_CHOI
      // Duyệt lại đánh giá đã từ chối (bỏ qua chờ duyệt)
      newStatus = 1 // DA_DUYET
      action = 'duyet'
      message = `Đã duyệt lại đánh giá của "${userName}"`
    } else {
      // Fallback: treat as new review
      newStatus = 1 // DA_DUYET
      action = 'duyet'
      message = `Đã duyệt đánh giá của "${userName}"`
    }

    if (action === 'duyet') {
      console.log('Calling approve API for review:', review.reviewId)
      const response = await api.post(`/api/reviews/admin/${review.reviewId}/duyet`, { ghiChu: 'Đã duyệt bởi admin' })
      console.log('Approve API response:', response.data)
    } else if (action === 'tu-choi') {
      console.log('Calling reject API for review:', review.reviewId)
      const response = await api.post(`/api/reviews/admin/${review.reviewId}/tu-choi`, { ghiChu: 'Từ chối bởi admin' })
      console.log('Reject API response:', response.data)
    }

    // Update local data
    const index = reviews.value.findIndex(r => r.reviewId === review.reviewId)
    if (index !== -1) {
      console.log('Updating review at index:', index, 'from', review.trangThai, 'to', newStatus)
      reviews.value[index].trangThai = newStatus
    } else {
      console.error('Review not found in local data:', review.reviewId)
    }

    const statusText = newStatus === 1 ? 'Đã duyệt' : 'Đã từ chối'
    const statusColor = newStatus === 1 ? '#10b981' : '#ef4444'

    // Hiển thị toast với thông tin đầy đủ
    toastRef.value?.success('Thành công', `${message} - <span style="color: ${statusColor}; font-weight: bold;">${statusText}</span>`)
  } catch (error: any) {
    console.error('Lỗi khi cập nhật trạng thái:', error)
    toastRef.value?.error('Lỗi cập nhật', 'Không thể cập nhật trạng thái đánh giá')
  } finally {
    isUpdatingStatus.value = false
  }
}



// Helper functions for toggle
function getToggleChecked(item: Review): boolean {
  return item.trangThai === 1 // DA_DUYET
}

function getToggleTooltip(item: Review): string {
  const status = item.trangThai ?? 0 // Default to CHO_DUYET
  switch (status) {
    case 0: // CHO_DUYET
      return 'Duyệt đánh giá mới'
    case 1: // DA_DUYET
      return 'Từ chối đánh giá'
    case 2: // TU_CHOI
      return 'Duyệt lại đánh giá'
    default:
      return 'Chuyển trạng thái'
  }
}

// Helper functions for review status display
function getReviewStatusText(item: Review): string {
  const status = item.trangThai ?? 0
  switch (status) {
    case 0: // CHO_DUYET
      return '⏳ Chờ duyệt'
    case 1: // DA_DUYET
      return '✅ Đã duyệt'
    case 2: // TU_CHOI
      return '❌ Đã từ chối'
    default:
      return '❓ Không xác định'
  }
}

function getReviewStatusClass(item: Review): string {
  const status = item.trangThai ?? 0
  switch (status) {
    case 0: // CHO_DUYET
      return 'review-status-cho-duyet'
    case 1: // DA_DUYET
      return 'review-status-da-duyet'
    case 2: // TU_CHOI
      return 'review-status-tu-choi'
    default:
      return 'review-status-unknown'
  }
}

onMounted(loadReviews)
</script>

<style scoped>
@import '@/styles/admin-layout.css';

/* Custom column widths for Review page */
:deep(.data-table .id-col) {
  width: 60px;
  min-width: 60px;
}

:deep(.data-table .user-col) {
  width: 150px;
  min-width: 150px;
}

:deep(.data-table .product-col) {
  width: 100px;
  min-width: 100px;
}

:deep(.data-table .rating-col) {
  width: 80px;
  min-width: 80px;
  text-align: center;
}

:deep(.data-table .comment-col) {
  width: 200px;
  min-width: 200px;
  max-width: 300px;
  word-wrap: break-word;
}

:deep(.data-table .date-col) {
  width: 120px;
  min-width: 120px;
}

:deep(.data-table .status-col) {
  width: 100px;
  min-width: 100px;
}

:deep(.data-table .action-col) {
  width: 180px;
  min-width: 180px;
  text-align: center;
}

/* Improve action buttons spacing */
:deep(.edit-btn, .delete-btn) {
  margin: 0 4px;
  padding: 6px 10px;
  font-size: 14px;
}

/* Rating stars styling */
:deep(.rating-col) {
  color: #ffc107;
  font-weight: bold;
}


/* Custom rating display for AdminReviewPage */
.rating-display {
  display: flex;
  align-items: center;
  gap: 8px;
}

.rating-stars {
  display: flex;
  gap: 1px;
}

.rating-stars .star-filled {
  color: #ffc107;
  font-size: 16px;
}

.rating-stars .star-empty {
  color: #e5e7eb;
  font-size: 16px;
}

.rating-number {
  font-size: 12px;
  color: #6b7280;
  font-weight: 500;
}


/* Toggle switch styles */
.status-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.action-buttons {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.toggle-switch {
  position: relative;
  display: inline-block;
  width: 50px;
  height: 24px;
  cursor: pointer;
}

.toggle-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  transition: .4s;
  border-radius: 24px;
}

.toggle-slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: .4s;
  border-radius: 50%;
}

input:checked + .toggle-slider {
  background-color: #10b981;
}

input:checked + .toggle-slider:before {
  transform: translateX(26px);
}



/* Review status styles - riêng cho AdminReviewPage */
.review-status-text {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 8px;
  border-radius: 12px;
  display: inline-block;
  text-align: center;
  min-width: 80px;
}

.review-status-cho-duyet {
  background: #fef3c7;
  color: #f59e0b;
  border: 1px solid #f59e0b;
}

.review-status-da-duyet {
  background: #d1fae5;
  color: #10b981;
  border: 1px solid #10b981;
}

.review-status-tu-choi {
  background: #fee2e2;
  color: #ef4444;
  border: 1px solid #ef4444;
}

.review-status-unknown {
  background: #f3f4f6;
  color: #6b7280;
  border: 1px solid #d1d5db;
}
</style>
