<template>
  <div class="page">
    <PosHeader />

    <div class="content">
      <div class="header">
        <div class="header-left">
          <button class="btn-back" @click="goBack">
            <span class="back-icon">←</span>
            Quay lại
          </button>
          <h1>Thêm Nhân Viên</h1>
        </div>
      </div>

      <Toast ref="toastRef" />

      <div class="form-container">
        <!-- Combined Section: Image Upload and QR Scanner -->
        <div class="combined-section">
          <!-- Ảnh Nhân Viên (Left) -->
          <div class="image-upload-area">
            <div class="image-preview" v-if="selectedImage">
              <div v-if="imageLoading" class="image-loading">
                <div class="loading-spinner"></div>
                <p>Đang tải ảnh...</p>
              </div>
              <img
                v-else-if="!imageError"
                :src="selectedImage"
                alt="Ảnh nhân viên"
                class="preview-image"
                @error="handleImageError"
                @load="handleImageLoad"
                loading="lazy"
              />
              <div v-else class="image-error">
                <i class="fas fa-exclamation-triangle"></i>
                <p>Lỗi tải ảnh</p>
              </div>
              <button type="button" class="btn-remove-image" @click="removeImage">×</button>
            </div>
            <div class="image-upload-placeholder" v-else>
              <i class="fas fa-user-circle"></i>
              <p>Chưa có ảnh</p>
            </div>

            <div class="image-upload-controls">
              <input
                type="file"
                id="employeeImage"
                ref="imageInput"
                @change="handleImageUpload"
                accept="image/*"
                class="image-input"
              />
              <label for="employeeImage" class="btn-upload-image">
                <i class="fas fa-upload"></i>
                Tải ảnh lên
              </label>
              <div class="image-upload-info">
                <small>Định dạng: JPG, PNG, GIF (Tối đa 5MB)</small>
              </div>
            </div>

            <div v-if="errors.anhDaiDien" class="error-message">
              {{ errors.anhDaiDien }}
            </div>
          </div>

          <!-- QR Scanner Section (Right) -->
          <div class="qr-scanner-area">
            <div class="scanner-header">
              <h3>Quét QR Căn Cước Công Dân</h3>
              <p>Quét mã QR trên căn cước công dân để tự động điền thông tin</p>
            </div>

            <div class="scanner-controls">
              <button
                type="button"
                class="btn-qr-scan"
                @click="toggleQRScanner"
                :disabled="loading"
              >
                <i class="fas fa-qrcode"></i>
                {{ isScanning ? 'Dừng quét' : 'Bắt đầu quét QR' }}
              </button>

              <button
                type="button"
                class="btn-upload-qr"
                @click="triggerQRImageUpload"
                :disabled="loading"
              >
                <i class="fas fa-upload"></i>
                Upload ảnh QR
              </button>

              <button
                type="button"
                class="btn-clear-qr"
                @click="clearQRData"
                v-if="qrDataExtracted"
              >
                <i class="fas fa-times"></i>
                Xóa dữ liệu QR
              </button>
            </div>

            <!-- QR Scanner Modal -->
            <div v-if="isScanning" class="qr-scanner-modal" @click="closeQRScanner">
              <div class="qr-scanner-content" @click.stop.prevent>
                <div class="scanner-header-modal">
                  <h4>Quét QR Căn Cước Công Dân</h4>
                  <button class="btn-close" @click="closeQRScanner" title="Đóng">×</button>
                </div>

                <div class="scanner-video-container">
                  <video ref="videoRef" autoplay playsinline></video>
                  <canvas ref="canvasRef" style="display: none"></canvas>
                  <div class="scanner-overlay">
                    <div class="scanner-frame"></div>
                    <p class="scanner-instruction">Đưa camera vào mã QR trên căn cước công dân</p>
                  </div>
                </div>

                <div class="scanner-status">
                  <div v-if="scanningStatus" class="status-message">
                    {{ scanningStatus }}
                  </div>
                </div>
              </div>
            </div>

            <!-- QR Data Preview -->
            <div v-if="qrDataExtracted" class="qr-data-preview">
              <h4>Dữ liệu đã trích xuất từ QR:</h4>
              <div class="qr-data-grid">
                <div class="qr-data-item">
                  <label>Số căn cước:</label>
                  <span>{{ qrData.cccd }}</span>
                </div>
                <div class="qr-data-item">
                  <label>Họ tên:</label>
                  <span>{{ qrData.hoTen }}</span>
                </div>
                <div class="qr-data-item">
                  <label>Ngày sinh:</label>
                  <span>{{ qrData.ngaySinh }}</span>
                </div>
                <div class="qr-data-item">
                  <label>Giới tính:</label>
                  <span>{{ qrData.gioiTinh }}</span>
                </div>
                <div class="qr-data-item">
                  <label>Địa chỉ:</label>
                  <span>{{ qrData.diaChi }}</span>
                </div>
              </div>
              <button type="button" class="btn-apply-qr" @click="applyQRData">
                <i class="fas fa-check"></i>
                Áp dụng dữ liệu vào form
              </button>
            </div>
          </div>
        </div>

        <!-- Hidden QR Image Upload Input -->
        <input
          type="file"
          id="qrImageUpload"
          ref="qrImageInput"
          @change="handleQRImageUpload"
          accept="image/*"
          style="display: none"
        />

        <form @submit.prevent="saveNhanVien" class="staff-form" enctype="multipart/form-data">
          <div class="form-grid">
            <!-- Mã Nhân Viên (Auto-generated) -->
            <div class="form-group">
              <label for="maNhanVien" class="form-label"> Mã Nhân Viên </label>
              <input
                id="maNhanVien"
                v-model="maNhanVien"
                type="text"
                class="form-input"
                placeholder="Mã nhân viên sẽ được tự động sinh"
                readonly
                disabled
              />
              <div class="form-help">Mã nhân viên sẽ được tự động tạo khi lưu</div>
            </div>

            <!-- Họ Tên -->
            <div class="form-group">
              <label for="hoTen" class="form-label"> Họ Tên <span class="required">*</span> </label>
              <input
                id="hoTen"
                v-model="formData.hoTen"
                type="text"
                class="form-input"
                placeholder="Nhập họ và tên đầy đủ"
                maxlength="255"
                required
              />
              <div v-if="errors.hoTen" class="error-message">
                {{ errors.hoTen }}
              </div>
            </div>

            <!-- Số Căn Cước -->
            <div class="form-group">
              <label for="cccd" class="form-label"> Số Căn Cước </label>
              <input
                id="cccd"
                v-model="formData.cccd"
                type="text"
                class="form-input"
                placeholder="Nhập số căn cước công dân (12 chữ số)"
                maxlength="20"
                pattern="^[0-9]{12}$"
              />
              <div v-if="errors.cccd" class="error-message">
                {{ errors.cccd }}
              </div>
            </div>

            <!-- Số Điện Thoại -->
            <div class="form-group">
              <label for="soDienThoai" class="form-label">
                Số Điện Thoại <span class="required">*</span>
              </label>
              <input
                id="soDienThoai"
                v-model="formData.soDienThoai"
                type="text"
                class="form-input"
                placeholder="Nhập số điện thoại (10-11 chữ số)"
                pattern="^[0-9]{10,11}$"
                required
              />
              <div v-if="errors.soDienThoai" class="error-message">
                {{ errors.soDienThoai }}
              </div>
            </div>

            <!-- Ngày Sinh -->
            <div class="form-group">
              <label for="ngaySinh" class="form-label">
                Ngày Sinh <span class="required">*</span>
              </label>
              <input
                id="ngaySinh"
                v-model="formData.ngaySinh"
                type="date"
                class="form-input"
                required
              />
              <div v-if="errors.ngaySinh" class="error-message">
                {{ errors.ngaySinh }}
              </div>
            </div>

            <!-- Giới Tính -->
            <div class="form-group">
              <label class="form-label">
                Giới Tính <span class="required">*</span>
              </label>
              <div class="radio-group">
                <label class="radio-option">
                  <input 
                    type="radio" 
                    v-model="formData.gioiTinh" 
                    value="Nam" 
                    class="radio-input"
                    required
                  />
                  <span class="radio-label">Nam</span>
                </label>
                <label class="radio-option">
                  <input 
                    type="radio" 
                    v-model="formData.gioiTinh" 
                    value="Nữ" 
                    class="radio-input"
                    required
                  />
                  <span class="radio-label">Nữ</span>
                </label>
              </div>
              <div v-if="errors.gioiTinh" class="error-message">
                {{ errors.gioiTinh }}
              </div>
            </div>

            <!-- Chức Vụ -->
            <div class="form-group">
              <label for="chucVu" class="form-label">
                Chức Vụ <span class="required">*</span>
              </label>
              <select id="chucVu" v-model="formData.chucVu" class="form-select" required>
                <option value="">Chọn chức vụ</option>
                <option value="Quản lý">Quản lý</option>
                <option value="Nhân viên">Nhân viên</option>
              </select>
              <div v-if="errors.chucVu" class="error-message">
                {{ errors.chucVu }}
              </div>
            </div>

            <!-- Email -->
            <div class="form-group">
              <label for="email" class="form-label"> Email <span class="required">*</span> </label>
              <input
                id="email"
                v-model="formData.email"
                type="email"
                class="form-input"
                placeholder="Nhập email hợp lệ"
                maxlength="255"
                required
              />
              <div v-if="errors.email" class="error-message">
                {{ errors.email }}
              </div>
            </div>
          </div>

          <!-- Địa Chỉ (Full width) -->
          <div class="form-group full-width">
            <label for="diaChi" class="form-label"> Địa Chỉ <span class="required">*</span> </label>
            <textarea
              id="diaChi"
              v-model="formData.diaChi"
              class="form-textarea"
              placeholder="Nhập địa chỉ đầy đủ"
              maxlength="1000"
              rows="3"
              required
            ></textarea>
            <div v-if="errors.diaChi" class="error-message">
              {{ errors.diaChi }}
            </div>
          </div>

          <!-- Form Actions -->
          <div class="form-actions">
            <button type="button" class="btn-secondary" @click="goBack">Hủy</button>
            <button type="submit" class="btn-primary" :disabled="loading">
              <span v-if="loading" class="loading-spinner"></span>
              {{ loading ? 'Đang lưu...' : 'Thêm Nhân Viên' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onUnmounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import Toast from '@/components/Toast.vue'
import PosHeader from '@/components/PosHeader.vue'
import api from '@/services/api'
import { maNhanVienService } from '@/services/maNhanVienService'
import jsQR from 'jsqr'

const router = useRouter()
const toastRef = ref()
const loading = ref(false)

// Image upload variables
const selectedImage = ref<string | null>(null)
const selectedFile = ref<File | null>(null)
const imageInput = ref<HTMLInputElement>()
const imageLoading = ref<boolean>(false)
const imageError = ref<boolean>(false)

// QR Scanner variables
const isScanning = ref(false)
const qrDataExtracted = ref(false)
const scanningStatus = ref('')
const videoRef = ref<HTMLVideoElement>()
const canvasRef = ref<HTMLCanvasElement>()
const stream = ref<MediaStream | null>(null)
const scanInterval = ref<number | NodeJS.Timeout | null>(null)
const qrImageInput = ref<HTMLInputElement>()

// QR Data structure
const qrData = reactive({
  cccd: '',
  hoTen: '',
  ngaySinh: '',
  gioiTinh: '',
  diaChi: '',
})

// Form data
const formData = reactive({
  hoTen: '',
  cccd: '',
  soDienThoai: '',
  ngaySinh: '',
  gioiTinh: '',
  diaChi: '',
  email: '',
  chucVu: '',
  anhDaiDien: null as File | string | null,
})

// Auto-generated staff code (for display only)
const maNhanVien = ref('')

// Error handling
const errors = reactive({
  hoTen: '',
  cccd: '',
  soDienThoai: '',
  ngaySinh: '',
  gioiTinh: '',
  diaChi: '',
  email: '',
  chucVu: '',
  anhDaiDien: '',
})

// Clear errors
function clearErrors() {
  Object.keys(errors).forEach((key) => {
    errors[key] = ''
  })
}

// Get role display name from chuc_vu
function getRoleDisplayName(chucVu: string): string {
  if (!chucVu || chucVu.trim() === '') {
    return 'Chưa xác định'
  }

  const chucVuLower = chucVu.toLowerCase().trim()

  // Admin positions
  if (
    chucVuLower.includes('quản trị') ||
    chucVuLower.includes('admin') ||
    chucVuLower.includes('administrator')
  ) {
    return 'Quản trị viên'
  }

  // Manager positions
  if (
    chucVuLower.includes('quản lý') ||
    chucVuLower.includes('manager') ||
    chucVuLower.includes('giám đốc') ||
    chucVuLower.includes('director') ||
    chucVuLower.includes('trưởng phòng')
  ) {
    return 'Quản lý'
  }

  // Staff positions
  return 'Nhân viên'
}

// Watch for chuc_vu changes to show role preview
watch(
  () => formData.chucVu,
  (newChucVu) => {
    if (newChucVu) {
      const roleDisplay = getRoleDisplayName(newChucVu)
      toastRef.value?.info(
        'Thông báo',
        `Quyền hạn sẽ được cấp: "${roleDisplay}" dựa trên chức vụ "${newChucVu}"`,
      )
    }
  },
)

// Go back to staff list
function goBack() {
  router.push('/nhan-vien')
}

// Image upload functions - Updated to match product image upload flow
async function handleImageUpload(event: Event) {
  const target = event.target as HTMLInputElement
  if (!target || !target.files) return

  const file = target.files[0]
  if (!file) return

  // Reset states
  imageError.value = false
  imageLoading.value = true

  // Validate file type
  if (!file.type.startsWith('image/')) {
    errors.anhDaiDien = 'Vui lòng chọn file ảnh hợp lệ'
    toastRef.value?.error('Lỗi', 'Vui lòng chọn file ảnh hợp lệ')
    imageLoading.value = false
    return
  }

  // Validate file size (5MB)
  if (file.size > 5 * 1024 * 1024) {
    errors.anhDaiDien = 'Kích thước file không được vượt quá 5MB'
    toastRef.value?.error('Lỗi', 'Kích thước file không được vượt quá 5MB')
    imageLoading.value = false
    return
  }

  // Clear previous errors
  errors.anhDaiDien = ''

  try {
    // Upload image immediately like product images
    const formDataUpload = new FormData()
    formDataUpload.append('file', file)

    const response = await api.post('/api/upload/avatar', formDataUpload, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })

    if (response.data.url) {
      // Store the uploaded image URL instead of file
      selectedImage.value = getImageUrl(response.data.url) // Use getImageUrl to create full URL
      selectedFile.value = file
      formData.anhDaiDien = response.data.url // Store URL instead of file
      imageLoading.value = false

      console.log('Image uploaded successfully:', response.data.url)
      toastRef.value?.success('Thành công', 'Upload ảnh thành công!')
    } else {
      throw new Error('No URL returned from server')
    }
  } catch (error) {
    console.error('Error uploading image:', error)
    imageError.value = true
    imageLoading.value = false

    if (error.response?.status === 413) {
      toastRef.value?.error('Lỗi', 'File quá lớn! Vui lòng chọn file nhỏ hơn 5MB.')
    } else {
      toastRef.value?.error(
        'Lỗi',
        'Lỗi upload ảnh: ' + (error.response?.data?.error || error.message),
      )
    }
  }

  // Clear input để có thể chọn lại file cùng tên
  target.value = ''
}

async function removeImage() {
  // If there's an uploaded image, try to delete it from server
  if (
    formData.anhDaiDien &&
    typeof formData.anhDaiDien === 'string' &&
    formData.anhDaiDien.startsWith('/uploads/avatar/')
  ) {
    try {
      const filename = formData.anhDaiDien.split('/').pop()
      if (filename) {
        await api.delete(`/api/upload/avatar/${filename}`)
        console.log('Image deleted from server:', filename)
      }
    } catch (error) {
      console.error('Error deleting image from server:', error)
      // Don't show error to user as the image is already removed from UI
    }
  }

  // Clear all image data
  selectedImage.value = null
  selectedFile.value = null
  formData.anhDaiDien = null
  errors.anhDaiDien = ''
  imageError.value = false
  imageLoading.value = false

  // Reset file input
  if (imageInput.value) {
    imageInput.value.value = ''
  }
}

// Helper function to generate image URL
// Get image URL - use static resource serving (like product images)
function getImageUrl(imagePath: string | null): string {
  if (!imagePath) return ''

  // If it's already a data URL, return as is
  if (imagePath.startsWith('data:')) {
    return imagePath
  }

  // If it's already a full URL, return as is
  if (imagePath.startsWith('http')) {
    return imagePath
  }

  const baseUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'

  // If path already starts with /uploads/, use it directly
  if (imagePath.startsWith('/uploads/')) {
    return `${baseUrl}${imagePath}`
  }

  // If path doesn't start with /uploads/, add it
  return `${baseUrl}/uploads/${imagePath}`
}

// Handle image error - simplified like ChiTietNhanVienPage
function handleImageError(event: Event) {
  console.error('Image load error:', event)
  console.error('Failed to load image:', selectedImage.value)

  // Set error state immediately
  imageError.value = true
  imageLoading.value = false

  // Don't retry - show fallback
  console.log('Image failed to load, showing fallback')
}

// Handle image load success
function handleImageLoad(event: Event) {
  console.log('Image loaded successfully:', event)
  imageError.value = false
  imageLoading.value = false
}

// QR Scanner functions
async function toggleQRScanner() {
  if (isScanning.value) {
    stopQRScanner()
  } else {
    // Set scanning to true first to render modal
    isScanning.value = true
    // Wait for modal to be rendered
    await nextTick()
    await startQRScanner()
  }
}

// Ensure video element is ready
function ensureVideoElement() {
  console.log('Checking video element...')
  console.log('VideoRef.value:', videoRef.value)
  console.log('VideoRef type:', typeof videoRef.value)

  if (!videoRef.value) {
    console.error('VideoRef is null')
    return false
  }

  // Check if video element is in DOM
  if (!document.contains(videoRef.value)) {
    console.error('Video element not in DOM')
    console.log('Video element:', videoRef.value)
    console.log('Video element parent:', videoRef.value.parentElement)
    return false
  }

  console.log('Video element is ready')
  return true
}

// Create video element dynamically if needed
function createVideoElement() {
  console.log('Creating video element...')

  // Wait for modal to be rendered
  const videoContainer = document.querySelector('.scanner-video-container')
  if (!videoContainer) {
    console.error('Video container not found, modal might not be rendered yet')
    return false
  }

  console.log('Video container found:', videoContainer)

  // Remove existing video if any
  const existingVideo = videoContainer.querySelector('video')
  if (existingVideo) {
    console.log('Removing existing video:', existingVideo)
    existingVideo.remove()
  }

  // Create new video element
  const video = document.createElement('video')
  video.autoplay = true
  video.playsInline = true
  video.style.width = '100%'
  video.style.height = '100%'
  video.style.objectFit = 'cover'
  video.style.background = '#000'

  // Add to container
  videoContainer.appendChild(video)

  // Update ref
  videoRef.value = video

  console.log('Created new video element:', video)
  console.log('VideoRef updated:', videoRef.value)
  return true
}

async function startQRScanner() {
  try {
    scanningStatus.value = 'Đang khởi tạo camera...'

    // Check if getUserMedia is supported
    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
      throw new Error('Camera không được hỗ trợ trên trình duyệt này')
    }

    // Check camera availability
    scanningStatus.value = 'Đang kiểm tra camera...'
    try {
      const devices = await navigator.mediaDevices.enumerateDevices()
      const videoDevices = devices.filter((device) => device.kind === 'videoinput')

      if (videoDevices.length === 0) {
        throw new Error('Không tìm thấy camera trên thiết bị')
      }

      console.log('Available cameras:', videoDevices)
    } catch (error) {
      console.error('Error checking camera:', error)
      throw new Error('Không thể kiểm tra camera')
    }

    // Request camera access with fallback options
    let constraints = {
      video: {
        facingMode: 'environment', // Use back camera
        width: { ideal: 1280 },
        height: { ideal: 720 },
      },
    }

    try {
      scanningStatus.value = 'Đang kết nối camera...'
      stream.value = await navigator.mediaDevices.getUserMedia(constraints)
      console.log('Camera access granted')
    } catch (backCameraError) {
      console.log('Back camera failed, trying front camera:', backCameraError)
      // Fallback to front camera
      constraints = {
        video: {
          facingMode: 'user', // Use front camera
          width: { ideal: 1280 },
          height: { ideal: 720 },
        },
      }
      scanningStatus.value = 'Đang thử camera trước...'
      stream.value = await navigator.mediaDevices.getUserMedia(constraints)
      console.log('Front camera access granted')
    }

    // Ensure video element is ready after getting camera access
    if (!ensureVideoElement()) {
      console.log('Video element not ready, trying to create one...')

      // Wait for modal to be rendered using nextTick
      await nextTick()

      // Try to create video element dynamically
      if (!createVideoElement()) {
        // Try again after a longer wait
        await new Promise((resolve) => setTimeout(resolve, 500) as any)
        if (!createVideoElement()) {
          throw new Error('Không thể tạo video element')
        }
      }
    }

    console.log('VideoRef:', videoRef.value)
    console.log('Stream:', stream.value)

    if (!videoRef.value) {
      throw new Error('Video element không tồn tại')
    }

    if (!stream.value) {
      throw new Error('Camera stream không tồn tại')
    }

    // Set video source
    videoRef.value.srcObject = stream.value

    // Wait for video to be ready
    videoRef.value.onloadedmetadata = () => {
      console.log('Video metadata loaded')
      videoRef.value
        .play()
        .then(() => {
          console.log('Video started playing')
          scanningStatus.value = 'Đang quét QR code...'
          startScanningLoop()
        })
        .catch((playError) => {
          console.error('Error playing video:', playError)
          scanningStatus.value = 'Lỗi phát video'
          toastRef.value?.error('Lỗi', 'Không thể phát video từ camera')
        })
    }

    videoRef.value.onerror = (error) => {
      console.error('Video error:', error)
      scanningStatus.value = 'Lỗi video'
      toastRef.value?.error('Lỗi', 'Lỗi hiển thị video từ camera')
    }

    // Add timeout for video loading
    setTimeout(() => {
      if (stream.value && videoRef.value) {
        console.log('Video loading timeout, trying to play manually')
        if (videoRef.value.readyState >= 2) {
          videoRef.value
            .play()
            .then(() => {
              scanningStatus.value = 'Đang quét QR code...'
              startScanningLoop()
            })
            .catch((error) => {
              console.error('Manual play failed:', error)
            })
        }
      }
    }, 3000)
  } catch (error) {
    console.error('Error accessing camera:', error)
    scanningStatus.value = 'Không thể truy cập camera'

    let errorMessage = 'Không thể truy cập camera'
    if (error.name === 'NotAllowedError') {
      errorMessage = 'Bị từ chối quyền truy cập camera. Vui lòng cho phép quyền camera và thử lại.'
    } else if (error.name === 'NotFoundError') {
      errorMessage = 'Không tìm thấy camera trên thiết bị.'
    } else if (error.name === 'NotSupportedError') {
      errorMessage = 'Trình duyệt không hỗ trợ truy cập camera.'
    } else if (error.name === 'NotReadableError') {
      errorMessage = 'Camera đang được sử dụng bởi ứng dụng khác.'
    }

    toastRef.value?.error('Lỗi Camera', errorMessage)
    stopQRScanner()
  }
}

function stopQRScanner() {
  isScanning.value = false
  scanningStatus.value = ''

  if (scanInterval.value) {
    clearInterval(scanInterval.value)
    scanInterval.value = null
  }

  if (stream.value) {
    stream.value.getTracks().forEach((track) => track.stop())
    stream.value = null
  }

  if (videoRef.value) {
    videoRef.value.srcObject = null
  }
}

function closeQRScanner() {
  stopQRScanner()
}

function startScanningLoop() {
  scanInterval.value = setInterval(async () => {
    if (videoRef.value && canvasRef.value) {
      const canvas = canvasRef.value
      const video = videoRef.value
      const context = canvas.getContext('2d')

      if (context && video.videoWidth > 0 && video.videoHeight > 0) {
        canvas.width = video.videoWidth
        canvas.height = video.videoHeight
        context.drawImage(video, 0, 0, canvas.width, canvas.height)

        // Try to decode QR code
        try {
          const imageData = context.getImageData(0, 0, canvas.width, canvas.height)
          const qrCode = decodeQRCode(imageData)

          if (qrCode) {
            console.log('QR Code detected:', qrCode)
            scanningStatus.value = 'Đã quét thành công! Đang đóng...'
            await processQRCode(qrCode)

            // Auto close scanner after successful scan
            setTimeout(() => {
              scanningStatus.value = 'Đang đóng scanner...'
              stopQRScanner()
            }, 1500) // Close after 1.5 seconds delay
          }
        } catch (error) {
          console.log('QR scan attempt failed:', error)
        }
      }
    }
  }, 100) // Scan every 100ms
}

function decodeQRCode(imageData: ImageData): string | null {
  try {
    // Use jsQR library to decode QR code
    const code = jsQR(imageData.data, imageData.width, imageData.height)

    if (code) {
      console.log('QR Code detected:', code.data)
      return code.data
    }

    return null
  } catch (error) {
    console.error('Error decoding QR code:', error)
    return null
  }
}

async function processQRCodeWithBackend(qrText: string) {
  try {
    console.log('Sending QR text to backend:', qrText)
    console.log('QR text length:', qrText.length)
    console.log('QR text type:', typeof qrText)

    const response = await api.post('/api/nhan-vien/process-qr', {
      qrText: qrText,
    })

    console.log('Backend response:', response.data)
    return response.data
  } catch (error) {
    console.error('Error processing QR with backend:', error)
    console.error('Error response:', error.response?.data)
    throw error
  }
}

async function processQRCode(qrText: string) {
  try {
    scanningStatus.value = 'Đang xử lý dữ liệu QR...'

    // Process QR with backend
    const data = await processQRCodeWithBackend(qrText)

    // Extract data from QR code
    qrData.cccd = data.cccd || ''
    qrData.hoTen = data.hoTen || ''
    qrData.ngaySinh = data.ngaySinh || ''
    qrData.gioiTinh = data.gioiTinh || ''
    qrData.diaChi = data.diaChi || ''

    qrDataExtracted.value = true
    scanningStatus.value = 'Đã quét thành công!'

    // Auto apply QR data to form
    applyQRData()

    toastRef.value?.success('Thành công', 'Đã quét và áp dụng dữ liệu QR code thành công!')
  } catch (error) {
    console.error('Error processing QR code:', error)
    scanningStatus.value = 'Lỗi xử lý QR code'

    if (error.response?.data?.error) {
      toastRef.value?.error('Lỗi', error.response.data.error)
    } else {
      toastRef.value?.error('Lỗi', 'Không thể xử lý dữ liệu QR code')
    }
  }
}

function applyQRData() {
  console.log('=== APPLYING QR DATA ===')
  console.log('QR Data received:', qrData)
  console.log('Current form data before apply:', {
    cccd: formData.cccd,
    hoTen: formData.hoTen,
    ngaySinh: formData.ngaySinh,
    gioiTinh: formData.gioiTinh,
    diaChi: formData.diaChi,
  })

  let appliedCount = 0

  // Apply QR data to form with validation
  if (qrData.cccd && qrData.cccd.trim() !== '') {
    formData.cccd = qrData.cccd.trim()
    appliedCount++
    console.log('✅ Applied CCCD:', formData.cccd)
  } else {
    console.log('❌ No CCCD data to apply')
  }

  // Validate HoTen - reject phone numbers, old CMND numbers and invalid data
  if (qrData.hoTen && qrData.hoTen.trim() !== '') {
    const hoTen = qrData.hoTen.trim()
    console.log(
      'Checking HoTen:',
      hoTen,
      'Length:',
      hoTen.length,
      'Is digits:',
      /^\d{9,12}$/.test(hoTen),
    )
    // Check if it's a phone number (all digits, 10-11 characters) or old CMND (9-12 digits)
    if (!/^\d{9,12}$/.test(hoTen) && hoTen.length > 1) {
      formData.hoTen = hoTen
      appliedCount++
      console.log('✅ Applied HoTen:', formData.hoTen)
    } else {
      console.log('❌ Rejected invalid HoTen (phone number or old CMND):', hoTen)
    }
  } else {
    console.log('❌ No HoTen data to apply')
  }

  // Validate NgaySinh - accept multiple date formats
  if (qrData.ngaySinh && qrData.ngaySinh.trim() !== '') {
    const ngaySinh = qrData.ngaySinh.trim()
    console.log(
      'Checking NgaySinh:',
      ngaySinh,
      'Format valid:',
      /^\d{2}\/\d{2}\/\d{4}$/.test(ngaySinh),
    )
    // Check if it's in DD/MM/YYYY format or other valid date formats
    if (
      /^\d{2}\/\d{2}\/\d{4}$/.test(ngaySinh) ||
      /^\d{2}-\d{2}-\d{4}$/.test(ngaySinh) ||
      /^\d{4}-\d{2}-\d{2}$/.test(ngaySinh) ||
      /^\d{8}$/.test(ngaySinh)
    ) {
      formData.ngaySinh = ngaySinh
      appliedCount++
      console.log('✅ Applied NgaySinh:', formData.ngaySinh)
    } else {
      console.log('❌ Rejected invalid NgaySinh format:', ngaySinh)
    }
  } else {
    console.log('❌ No NgaySinh data to apply')
  }

  // Validate GioiTinh - accept multiple gender formats
  if (qrData.gioiTinh && qrData.gioiTinh.trim() !== '') {
    const gioiTinh = qrData.gioiTinh.trim()
    console.log(
      'Checking GioiTinh:',
      gioiTinh,
      'Is valid:',
      ['Nam', 'Nữ', 'Male', 'Female', 'M', 'F', '1', '0'].includes(gioiTinh),
    )
    // Accept various gender formats
    if (['Nam', 'Nữ', 'Male', 'Female', 'M', 'F', '1', '0'].includes(gioiTinh)) {
      // Convert to standard format
      let standardGender = gioiTinh
      if (gioiTinh === 'M' || gioiTinh === '1') standardGender = 'Nam'
      if (gioiTinh === 'F' || gioiTinh === '0') standardGender = 'Nữ'
      if (gioiTinh === 'Male') standardGender = 'Nam'
      if (gioiTinh === 'Female') standardGender = 'Nữ'

      formData.gioiTinh = standardGender
      appliedCount++
      console.log('✅ Applied GioiTinh:', formData.gioiTinh)
    } else {
      console.log('❌ Rejected invalid GioiTinh:', gioiTinh)
    }
  } else {
    console.log('❌ No GioiTinh data to apply')
  }

  // Validate DiaChi - accept most address formats
  if (qrData.diaChi && qrData.diaChi.trim() !== '') {
    const diaChi = qrData.diaChi.trim()
    const wordCount = diaChi.split(' ').length
    console.log(
      'Checking DiaChi:',
      diaChi,
      'Word count:',
      wordCount,
      'Is single word:',
      wordCount === 1,
    )
    // Accept most address formats, only reject obvious gender words
    if (
      diaChi !== 'Nam' &&
      diaChi !== 'Nữ' &&
      diaChi !== 'Male' &&
      diaChi !== 'Female' &&
      diaChi !== 'M' &&
      diaChi !== 'F' &&
      diaChi !== '1' &&
      diaChi !== '0'
    ) {
      formData.diaChi = diaChi
      appliedCount++
      console.log('✅ Applied DiaChi:', formData.diaChi)
    } else {
      console.log('❌ Rejected invalid DiaChi (gender word):', diaChi)
    }
  } else {
    console.log('❌ No DiaChi data to apply')
  }

  console.log('📊 Applied', appliedCount, 'fields successfully')
  console.log('Final form data after QR apply:', {
    cccd: formData.cccd,
    hoTen: formData.hoTen,
    ngaySinh: formData.ngaySinh,
    gioiTinh: formData.gioiTinh,
    diaChi: formData.diaChi,
  })

  // Show toast with applied count
  if (appliedCount > 0) {
    toastRef.value?.success('Thành công', `Đã áp dụng ${appliedCount} trường dữ liệu từ QR code!`)
  } else {
    toastRef.value?.warning('Cảnh báo', 'Không có dữ liệu hợp lệ nào được áp dụng từ QR code!')
    // If no data was applied, try force apply
    console.log('No data applied, trying force apply...')
    forceApplyAllData()
  }

  // Clear QR data after applying
  clearQRData()
}

// Force apply all data function
function forceApplyAllData() {
  console.log('=== FORCE APPLYING ALL DATA ===')
  console.log('QR Data for force apply:', qrData)

  let forceAppliedCount = 0

  // Force apply all data without validation
  if (qrData.cccd && qrData.cccd.trim() !== '') {
    formData.cccd = qrData.cccd.trim()
    forceAppliedCount++
    console.log('🔧 Force applied CCCD:', formData.cccd)
  }

  if (qrData.hoTen && qrData.hoTen.trim() !== '') {
    formData.hoTen = qrData.hoTen.trim()
    forceAppliedCount++
    console.log('🔧 Force applied HoTen:', formData.hoTen)
  }

  if (qrData.ngaySinh && qrData.ngaySinh.trim() !== '') {
    formData.ngaySinh = qrData.ngaySinh.trim()
    forceAppliedCount++
    console.log('🔧 Force applied NgaySinh:', formData.ngaySinh)
  }

  if (qrData.gioiTinh && qrData.gioiTinh.trim() !== '') {
    formData.gioiTinh = qrData.gioiTinh.trim()
    forceAppliedCount++
    console.log('🔧 Force applied GioiTinh:', formData.gioiTinh)
  }

  if (qrData.diaChi && qrData.diaChi.trim() !== '') {
    formData.diaChi = qrData.diaChi.trim()
    forceAppliedCount++
    console.log('🔧 Force applied DiaChi:', formData.diaChi)
  }

  console.log('🔧 Force applied', forceAppliedCount, 'fields')
  console.log('Form data after force apply:', {
    cccd: formData.cccd,
    hoTen: formData.hoTen,
    ngaySinh: formData.ngaySinh,
    gioiTinh: formData.gioiTinh,
    diaChi: formData.diaChi,
  })

  if (forceAppliedCount > 0) {
    toastRef.value?.success('Thành công', `Đã force apply ${forceAppliedCount} trường dữ liệu!`)
  }
}

// Clear invalid data function
function clearInvalidData() {
  console.log('=== CLEARING INVALID DATA ===')

  // Clear phone numbers from HoTen field
  if (formData.hoTen && /^\d{9,12}$/.test(formData.hoTen.trim())) {
    console.log('Clearing phone number from HoTen:', formData.hoTen)
    formData.hoTen = ''
  }

  // Clear single words from DiaChi field
  if (formData.diaChi && formData.diaChi.trim().split(' ').length === 1) {
    console.log('Clearing single word from DiaChi:', formData.diaChi)
    formData.diaChi = ''
  }

  // Clear invalid date formats
  if (formData.ngaySinh && !/^\d{2}\/\d{2}\/\d{4}$/.test(formData.ngaySinh.trim())) {
    console.log('Clearing invalid date format:', formData.ngaySinh)
    formData.ngaySinh = ''
  }

  // Clear invalid gender values
  if (formData.gioiTinh && !['Nam', 'Nữ', 'Male', 'Female'].includes(formData.gioiTinh.trim())) {
    console.log('Clearing invalid gender:', formData.gioiTinh)
    formData.gioiTinh = ''
  }

  console.log('Form data after clearing invalid data:', {
    cccd: formData.cccd,
    hoTen: formData.hoTen,
    ngaySinh: formData.ngaySinh,
    gioiTinh: formData.gioiTinh,
    diaChi: formData.diaChi,
  })

  toastRef.value?.success('Thành công', 'Đã xóa dữ liệu không hợp lệ!')
}

// Manual apply QR data function
function manualApplyQRData() {
  console.log('=== MANUAL APPLY QR DATA ===')
  console.log('Current qrData:', qrData)

  if (!qrDataExtracted.value) {
    toastRef.value?.warning('Cảnh báo', 'Không có dữ liệu QR để áp dụng!')
    return
  }

  // Force apply data without validation for debugging
  if (qrData.cccd) {
    formData.cccd = qrData.cccd.trim()
    console.log('Force applied CCCD:', formData.cccd)
  }

  if (qrData.hoTen) {
    formData.hoTen = qrData.hoTen.trim()
    console.log('Force applied HoTen:', formData.hoTen)
  }

  if (qrData.ngaySinh) {
    formData.ngaySinh = qrData.ngaySinh.trim()
    console.log('Force applied NgaySinh:', formData.ngaySinh)
  }

  if (qrData.gioiTinh) {
    formData.gioiTinh = qrData.gioiTinh.trim()
    console.log('Force applied GioiTinh:', formData.gioiTinh)
  }

  if (qrData.diaChi) {
    formData.diaChi = qrData.diaChi.trim()
    console.log('Force applied DiaChi:', formData.diaChi)
  }

  console.log('Form data after manual apply:', {
    cccd: formData.cccd,
    hoTen: formData.hoTen,
    ngaySinh: formData.ngaySinh,
    gioiTinh: formData.gioiTinh,
    diaChi: formData.diaChi,
  })

  toastRef.value?.success('Thành công', 'Đã áp dụng dữ liệu QR thủ công!')
}

function clearQRData() {
  qrDataExtracted.value = false
  qrData.cccd = ''
  qrData.hoTen = ''
  qrData.ngaySinh = ''
  qrData.gioiTinh = ''
  qrData.diaChi = ''
}

// QR Image Upload functions
function triggerQRImageUpload() {
  if (qrImageInput.value) {
    qrImageInput.value.click()
  }
}

async function handleQRImageUpload(event: Event) {
  const target = event.target as HTMLInputElement
  if (!target || !target.files) return

  const file = target.files[0]
  if (!file) return

  // Validate file type
  if (!file.type.startsWith('image/')) {
    toastRef.value?.error('Lỗi', 'Vui lòng chọn file ảnh hợp lệ')
    return
  }

  // Validate file size (5MB)
  if (file.size > 5 * 1024 * 1024) {
    toastRef.value?.error('Lỗi', 'Kích thước file không được vượt quá 5MB')
    return
  }

  try {
    // Show loading status
    scanningStatus.value = 'Đang xử lý ảnh QR...'

    // Create FormData
    const formData = new FormData()
    formData.append('file', file)

    // Send to backend
    const response = await api.post('/api/nhan-vien/process-qr-image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })

    if (response.data) {
      // Extract data from response
      qrData.cccd = response.data.cccd || ''
      qrData.hoTen = response.data.hoTen || ''
      qrData.ngaySinh = response.data.ngaySinh || ''
      qrData.gioiTinh = response.data.gioiTinh || ''
      qrData.diaChi = response.data.diaChi || ''

      qrDataExtracted.value = true
      scanningStatus.value = 'Đã xử lý ảnh QR thành công!'

      // Auto apply QR data to form
      console.log('Auto applying QR data...')
      console.log('QR data before apply:', qrData)
      applyQRData()

      toastRef.value?.success('Thành công', 'Đã xử lý ảnh QR code thành công!')
    } else {
      throw new Error('No data returned from server')
    }
  } catch (error) {
    console.error('Error processing QR image:', error)
    console.error('Error response:', error.response?.data)
    console.error('Error status:', error.response?.status)
    console.error('Error headers:', error.response?.headers)
    scanningStatus.value = 'Lỗi xử lý ảnh QR'

    // Log full error details
    console.error('Full error object:', {
      message: error.message,
      code: error.code,
      status: error.response?.status,
      statusText: error.response?.statusText,
      data: error.response?.data,
      config: error.config,
    })

    if (error.response?.data?.error) {
      const errorMessage = error.response.data.error
      const suggestions = error.response.data.suggestions
      const details = error.response.data.details
      const rawData = error.response.data.rawData

      console.error('Error details:', { errorMessage, suggestions, details, rawData })

      let fullErrorMessage = errorMessage
      if (suggestions) {
        fullErrorMessage += '\n\n' + suggestions
      }
      if (details) {
        fullErrorMessage += `\n\nChi tiết: ${details}`
      }
      if (rawData) {
        fullErrorMessage += `\nDữ liệu thô: ${rawData}`
      }

      toastRef.value?.error('Lỗi QR Code', fullErrorMessage)
    } else {
      // Show more detailed error information
      let errorMsg = `Không thể xử lý ảnh QR code: ${error.message}`
      if (error.response?.status) {
        errorMsg += `\nStatus: ${error.response.status}`
      }
      if (error.response?.data) {
        errorMsg += `\nResponse: ${JSON.stringify(error.response.data)}`
      }

      toastRef.value?.error('Lỗi', errorMsg)
    }
  }

  // Clear input
  target.value = ''
}

// Test upload function
async function testUpload() {
  // Create a test file input
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'

  input.onchange = async (event) => {
    const target = event.target as HTMLInputElement
    if (!target || !target.files) return

    const file = target.files[0]
    if (!file) return

    try {
      console.log('Testing upload with file:', file.name, file.size, file.type)

      const formData = new FormData()
      formData.append('file', file)

      const response = await api.post('/api/nhan-vien/test-upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      })

      console.log('Test upload response:', response.data)
      toastRef.value?.success('Test Upload', 'Upload test thành công!')
    } catch (error) {
      console.error('Test upload error:', error)
      console.error('Error response:', error.response?.data)

      if (error.response?.data?.error) {
        toastRef.value?.error('Test Upload Error', error.response.data.error)
      } else {
        toastRef.value?.error('Test Upload Error', 'Lỗi test upload: ' + error.message)
      }
    }
  }

  input.click()
}

// Debug upload function
async function debugUpload() {
  // Create a test file input
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'

  input.onchange = async (event) => {
    const target = event.target as HTMLInputElement
    if (!target || !target.files) return

    const file = target.files[0]
    if (!file) return

    try {
      console.log('Debug upload with file:', {
        name: file.name,
        size: file.size,
        type: file.type,
        lastModified: file.lastModified,
      })

      const formData = new FormData()
      formData.append('file', file)

      console.log('FormData created:', formData)
      console.log('FormData entries:')
      for (let [key, value] of formData.entries()) {
        console.log(key, value)
      }

      const response = await api.post('/api/nhan-vien/debug-upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      })

      console.log('Debug upload response:', response.data)
      toastRef.value?.success('Debug Upload', 'Debug upload thành công!')
    } catch (error) {
      console.error('Debug upload error:', error)
      console.error('Error response:', error.response?.data)

      if (error.response?.data?.error) {
        toastRef.value?.error('Debug Upload Error', error.response.data.error)
      } else {
        toastRef.value?.error('Debug Upload Error', 'Lỗi debug upload: ' + error.message)
      }
    }
  }

  input.click()
}

// View QR Data function
async function viewQRData() {
  // Create a file input
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'

  input.onchange = async (event) => {
    const target = event.target as HTMLInputElement
    if (!target || !target.files) return

    const file = target.files[0]
    if (!file) return

    try {
      console.log('Viewing QR data from file:', {
        name: file.name,
        size: file.size,
        type: file.type,
      })

      const formData = new FormData()
      formData.append('file', file)

      // Send to backend to decode QR code
      const response = await api.post('/api/nhan-vien/process-qr-image', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      })

      console.log('QR Data Response:', response.data)

      // Display QR data in a detailed format
      let qrDataDisplay = '=== DỮ LIỆU QR CODE ===\n\n'

      if (response.data) {
        // Check validation status
        const validationStatus = response.data.validationStatus || 'UNKNOWN'
        const validationMessage = response.data.validationMessage || 'Không xác định'

        qrDataDisplay += `🔍 TRẠNG THÁI: ${validationStatus}\n`
        qrDataDisplay += `📝 THÔNG BÁO: ${validationMessage}\n\n`

        qrDataDisplay += '📋 THÔNG TIN CCCD:\n'
        qrDataDisplay += `• Số CCCD: ${response.data.cccd || 'N/A'}\n`
        qrDataDisplay += `• Họ tên: ${response.data.hoTen || 'N/A'}\n`
        qrDataDisplay += `• Ngày sinh: ${response.data.ngaySinh || 'N/A'}\n`
        qrDataDisplay += `• Giới tính: ${response.data.gioiTinh || 'N/A'}\n`
        qrDataDisplay += `• Địa chỉ: ${response.data.diaChi || 'N/A'}\n`
        qrDataDisplay += `• Quốc tịch: ${response.data.quocTich || 'N/A'}\n`
        qrDataDisplay += `• Nơi cấp: ${response.data.noiCap || 'N/A'}\n`
        qrDataDisplay += `• Ngày cấp: ${response.data.ngayCap || 'N/A'}\n\n`

        qrDataDisplay += '📄 DỮ LIỆU THÔ:\n'
        qrDataDisplay += `${response.data.rawData || response.data.qrText || 'N/A'}\n\n`

        if (response.data.qrTextLength) {
          qrDataDisplay += `📊 ĐỘ DÀI: ${response.data.qrTextLength} ký tự\n\n`
        }

        // Show all available data
        qrDataDisplay += '🔧 TẤT CẢ DỮ LIỆU:\n'
        for (const [key, value] of Object.entries(response.data)) {
          if (key !== 'rawData' && key !== 'qrText') {
            qrDataDisplay += `• ${key}: ${value}\n`
          }
        }
      } else {
        qrDataDisplay += '❌ Không có dữ liệu được trả về'
      }

      // Show in alert for easy viewing
      alert(qrDataDisplay)

      // Also show in toast
      toastRef.value?.success('QR Data', 'Đã xem dữ liệu QR code thành công!')
    } catch (error) {
      console.error('Error viewing QR data:', error)
      console.error('Error response:', error.response?.data)

      let errorDisplay = '=== LỖI XỬ LÝ QR CODE ===\n\n'

      if (error.response?.data?.error) {
        errorDisplay += `❌ Lỗi: ${error.response.data.error}\n`
        if (error.response.data.details) {
          errorDisplay += `📋 Chi tiết: ${error.response.data.details}\n`
        }
        if (error.response.data.rawData) {
          errorDisplay += `📄 Dữ liệu thô: ${error.response.data.rawData}\n`
        }
      } else {
        errorDisplay += `❌ Lỗi: ${error.message}\n`
        if (error.response?.status) {
          errorDisplay += `📊 Status: ${error.response.status}\n`
        }
      }

      // Show error in alert
      alert(errorDisplay)

      // Show error toast
      toastRef.value?.error('QR Data Error', 'Lỗi khi xem dữ liệu QR code')
    }
  }

  input.click()
}

// Cleanup on component unmount
onUnmounted(() => {
  stopQRScanner()
})

// Save staff
async function saveNhanVien() {
  clearErrors()
  loading.value = true

  try {
    // Image is already uploaded, just use the URL
    let imagePath = null
    if (formData.anhDaiDien) {
      if (typeof formData.anhDaiDien === 'string') {
        // It's already a URL from previous upload
        imagePath = formData.anhDaiDien
        console.log('Using existing image URL:', imagePath)
      } else {
        // Fallback: if it's still a file, upload it (shouldn't happen with new flow)
        console.warn('Unexpected file type, uploading...')
        try {
          const formDataUpload = new FormData()
          formDataUpload.append('file', formData.anhDaiDien)

          const uploadResponse = await api.post('/api/upload/avatar', formDataUpload, {
            headers: {
              'Content-Type': 'multipart/form-data',
            },
          })

          imagePath = uploadResponse.data.filename
          console.log('Fallback image upload successful:', imagePath)
        } catch (uploadError) {
          console.error('Error uploading image:', uploadError)
          toastRef.value?.error('Lỗi', 'Không thể upload ảnh đại diện')
          return
        }
      }
    }

    // Prepare JSON data with image path
    const { anhDaiDien, ...dataWithoutImage } = formData

    // Auto-generate taiKhoan and matKhau
    const taiKhoan = formData.email || `nv${Date.now()}`
    const matKhau = '123456' // Default password

    // Let backend auto-generate maNhanVien
    const requestData = {
      ...dataWithoutImage,
      anhDaiDien: imagePath,
      taiKhoan: taiKhoan,
      matKhau: matKhau,
      // maNhanVien: removed - let backend generate it
      trangThai: 1, // Active status for new employees
    }

    console.log('Sending create data:', requestData)
    console.log('Form data keys:', Object.keys(formData))
    console.log('Request data keys:', Object.keys(requestData))
    console.log('Request data values:', requestData)
    console.log('Request data JSON:', JSON.stringify(requestData, null, 2))

    const response = await api.post('/api/nhan-vien', requestData, {
      headers: {
        'Content-Type': 'application/json',
      },
    })

    // Display the generated staff code
    if (response.data && response.data.maNhanVien) {
      maNhanVien.value = response.data.maNhanVien
    }

    toastRef.value?.success('Thành công', 'Thêm nhân viên thành công!')

    // Clean up uploaded image if exists
    if (
      formData.anhDaiDien &&
      typeof formData.anhDaiDien === 'string' &&
      formData.anhDaiDien.startsWith('/uploads/avatar/')
    ) {
      try {
        const filename = formData.anhDaiDien.split('/').pop()
        if (filename) {
          await api.delete(`/api/upload/avatar/${filename}`)
          console.log('Cleaned up uploaded image:', filename)
        }
      } catch (error) {
        console.error('Error cleaning up image:', error)
      }
    }

    // Reset form
    Object.keys(formData).forEach((key) => {
      if (key === 'anhDaiDien') {
        formData[key] = null
      } else {
        formData[key] = ''
      }
    })

    // Reset image preview
    selectedImage.value = null
    selectedFile.value = null
    if (imageInput.value) {
      imageInput.value.value = ''
    }

    // Go back after 2 seconds to show the generated code
    setTimeout(() => {
      goBack()
    }, 2000) as any
  } catch (error) {
    console.error('Error saving nhan vien:', error)
    console.error('Error response:', error.response?.data)
    console.error('Error status:', error.response?.status)

    if (error.response?.status === 400) {
      const errorData = error.response.data
      console.error('400 Error details:', errorData)

      // Handle validation errors
      if (errorData.errors) {
        Object.keys(errorData.errors).forEach((field) => {
          if (errors.hasOwnProperty(field)) {
            errors[field] = errorData.errors[field]
          }
        })
        toastRef.value?.error('Lỗi', 'Vui lòng kiểm tra lại thông tin')
      } else {
        toastRef.value?.error('Lỗi', errorData?.error || 'Không thể thêm nhân viên')
      }
    } else {
      toastRef.value?.error('Lỗi', 'Không thể thêm nhân viên')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page {
  padding: 20px;
  padding-top: 80px;
  width: 100%;
  max-width: none;
}

.content {
  margin-top: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.btn-back {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: #6c757d;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
  font-size: 13px;
}

.btn-back:hover {
  background: #5a6268;
}

.back-icon {
  font-size: 14px;
  font-weight: bold;
}

h1 {
  margin: 0;
  color: #333;
  font-size: 24px;
  font-weight: 600;
}

.form-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 20px;
  width: 100%;
  max-width: none;
  margin: 0;
}

.staff-form {
  width: 100%;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-label {
  font-weight: 600;
  margin-bottom: 8px;
  color: #333;
  font-size: 14px;
}

.required {
  color: #f97316;
}

.form-input,
.form-select,
.form-textarea {
  padding: 10px 14px;
  border: 2px solid #e1e5e9;
  border-radius: 6px;
  font-size: 14px;
  transition:
    border-color 0.3s,
    box-shadow 0.3s;
  background: white;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: #ea580c;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.error-message {
  color: #dc3545;
  font-size: 12px;
  margin-top: 4px;
  font-weight: 500;
}

.form-help {
  color: #6c757d;
  font-size: 12px;
  margin-top: 4px;
  font-style: italic;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding-top: 15px;
  border-top: 1px solid #e1e5e9;
}

.btn-primary,
.btn-secondary {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-primary {
  background: #f97316 !important;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #ea580c !important;
}

.btn-primary:disabled {
  background: #6c757d;
  cursor: not-allowed;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #5a6268;
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid transparent;
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* QR Scanner Styles */
.qr-scanner-area {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding-right: 10px;
}

.scanner-header h3 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 20px;
  font-weight: 600;
}

.scanner-header p {
  margin: 0 0 20px 0;
  color: #6c757d;
  font-size: 14px;
}

.scanner-controls {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.btn-qr-scan,
.btn-upload-qr,
.btn-clear-qr,
.btn-apply-qr {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-qr-scan {
  background: #f97316;
  color: white;
}

.btn-qr-scan:hover:not(:disabled) {
  background: #ea580c;
  transform: translateY(-1px);
}

.btn-qr-scan:disabled {
  background: #6c757d;
  cursor: not-allowed;
}

.btn-upload-qr {
  background: #28a745;
  color: white;
}

.btn-upload-qr:hover:not(:disabled) {
  background: #218838;
  transform: translateY(-1px);
}

.btn-upload-qr:disabled {
  background: #6c757d;
  cursor: not-allowed;
}

.btn-clear-qr {
  background: #dc3545;
  color: white;
}

.btn-clear-qr:hover {
  background: #c82333;
  transform: translateY(-1px);
}

.btn-apply-qr {
  background: #f97316;
  color: white;
  margin-top: 16px;
}

.btn-apply-qr:hover {
  background: #ea580c;
  transform: translateY(-1px);
}

/* QR Scanner Modal */
.qr-scanner-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.qr-scanner-content {
  background: white;
  border-radius: 12px;
  padding: 24px;
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}

.scanner-header-modal {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.scanner-header-modal h4 {
  margin: 0;
  color: #333;
  font-size: 18px;
  font-weight: 600;
}

.btn-close {
  background: #e41717;
  color: white;
  border: none;
  border-radius: 4px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 18px;
  font-weight: bold;
  line-height: 1;
}

.btn-close:hover {
  background: #c82333;
  transform: scale(1.1);
}

.btn-close:focus {
  outline: none;
  box-shadow: 0 0 0 3px rgba(220, 53, 69, 0.25);
}

.scanner-video-container {
  position: relative;
  width: 100%;
  height: 300px;
  border-radius: 8px;
  overflow: hidden;
  background: #000;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.scanner-video-container video {
  width: 100%;
  height: 100%;
  object-fit: cover;
  background: #000;
}

.scanner-video-container video:not([srcObject]) {
  background: #333;
}

.scanner-video-container::before {
  content: 'Đang khởi tạo camera...';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: white;
  font-size: 16px;
  z-index: 1;
  pointer-events: none;
}

.scanner-video-container video[srcObject] + .scanner-overlay::before {
  display: none;
}

.scanner-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}

.scanner-frame {
  width: 200px;
  height: 200px;
  border: 3px solid #28a745;
  border-radius: 12px;
  position: relative;
  animation: pulse 2s infinite;
}

.scanner-frame::before,
.scanner-frame::after {
  content: '';
  position: absolute;
  width: 20px;
  height: 20px;
  border: 3px solid #28a745;
}

.scanner-frame::before {
  top: -3px;
  left: -3px;
  border-right: none;
  border-bottom: none;
}

.scanner-frame::after {
  bottom: -3px;
  right: -3px;
  border-left: none;
  border-top: none;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

.scanner-instruction {
  color: white;
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  background: rgba(0, 0, 0, 0.7);
  padding: 8px 16px;
  border-radius: 20px;
}

.scanner-status {
  text-align: center;
}

.status-message {
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

/* QR Data Preview */
.qr-data-preview {
  background: #e8f5e8;
  border: 1px solid #c3e6cb;
  border-radius: 8px;
  padding: 20px;
  margin-top: 20px;
}

.qr-data-preview h4 {
  margin: 0 0 16px 0;
  color: #155724;
  font-size: 16px;
  font-weight: 600;
}

.qr-data-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.qr-data-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.qr-data-item label {
  font-size: 12px;
  font-weight: 600;
  color: #6c757d;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.qr-data-item span {
  font-size: 14px;
  color: #333;
  font-weight: 500;
  padding: 8px 12px;
  background: white;
  border-radius: 6px;
  border: 1px solid #dee2e6;
}

/* Responsive */
@media (max-width: 1400px) {
  .form-grid {
    grid-template-columns: repeat(5, 1fr);
  }
}

@media (max-width: 1200px) {
  .form-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 900px) {
  .form-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 700px) {
  .form-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .header-left {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .form-actions {
    flex-direction: column;
  }

  .btn-primary,
  .btn-secondary {
    width: 100%;
    justify-content: center;
  }

  .scanner-controls {
    flex-direction: column;
  }

  .btn-qr-scan,
  .btn-clear-qr,
  .btn-apply-qr {
    width: 100%;
    justify-content: center;
  }

  .qr-scanner-content {
    width: 95%;
    padding: 16px;
  }

  .scanner-video-container {
    height: 250px;
  }

  .scanner-frame {
    width: 150px;
    height: 150px;
  }

  .qr-data-grid {
    grid-template-columns: 1fr;
  }
}

/* Combined Section Layout */
.combined-section {
  display: grid;
  grid-template-columns: 0.8fr 1.2fr;
  gap: 20px;
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 12px;
  border: 2px dashed #dee2e6;
}

/* Image Upload Styles */
.image-upload-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  gap: 12px;
}

.image-upload-container {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.image-preview {
  position: relative;
  width: 140px;
  height: 140px;
  border-radius: 12px;
  overflow: hidden;
  border: 3px solid #f97316;
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.2);
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #6b7280;
}

.image-loading .loading-spinner {
  width: 24px;
  height: 24px;
  border: 2px solid #e5e7eb;
  border-top: 2px solid #f97316;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 8px;
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #ef4444;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 8px;
}

.image-error i {
  font-size: 24px;
  margin-bottom: 8px;
}

.image-error p {
  font-size: 12px;
  margin: 0;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.btn-remove-image {
  position: absolute;
  top: -4px;
  right: -4px;
  width: 24px;
  height: 24px;
  background: transparent;
  color: #333;
  border: none;
  border-radius: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 18px;
  font-weight: bold;
  line-height: 1;
  transition: all 0.3s ease;
  box-shadow: none;
}

.btn-remove-image:hover {
  background: transparent;
  color: #000;
  transform: scale(1.2);
  box-shadow: none;
}

.image-upload-placeholder {
  width: 140px;
  height: 140px;
  border: 2px dashed #6c757d;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: white;
  color: #6c757d;
  transition: all 0.3s ease;
}

.image-upload-placeholder:hover {
  border-color: #f97316;
  color: #f97316;
}

.image-upload-placeholder i {
  font-size: 32px;
  margin-bottom: 8px;
}

.image-upload-placeholder p {
  margin: 0;
  font-size: 12px;
  font-weight: 500;
}

.image-upload-controls {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
  text-align: center;
}

.image-input {
  display: none;
}

.btn-upload-image {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #f97316;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
  justify-content: center;
}

.btn-upload-image:hover {
  background: #ea580c;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.3);
}

.btn-upload-image i {
  font-size: 16px;
}

.image-upload-info {
  color: #6c757d;
  font-size: 12px;
  text-align: center;
}

.image-upload-info small {
  font-style: italic;
}

/* Radio Group Styles */
.radio-group {
  display: flex;
  gap: 20px;
  width: 100%;
  margin-top: 8px;
  min-height: 60px;
}

.radio-option {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 16px 24px;
  border: 2px solid #e1e5e9;
  border-radius: 8px;
  background: #fff;
  transition: all 0.3s ease;
  flex: 1;
  justify-content: center;
  min-height: 60px;
  font-size: 16px;
}

.radio-option:hover {
  border-color: #f97316;
  background: #fef3f0;
}

.radio-option:has(.radio-input:checked) {
  border-color: #f97316;
  background: #fef3f0;
  box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.1);
}

.radio-input {
  margin: 0;
  width: 18px;
  height: 18px;
  accent-color: #f97316;
  cursor: pointer;
}

.radio-label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  cursor: pointer;
  user-select: none;
}

/* Responsive Radio Group */
@media (max-width: 768px) {
  .radio-group {
    flex-direction: column;
    gap: 12px;
  }
  
  .radio-option {
    justify-content: flex-start;
    padding: 16px;
  }
}

/* Responsive Layout */
@media (max-width: 768px) {
  .combined-section {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .image-upload-area {
    min-height: 180px;
  }

  .image-preview,
  .image-upload-placeholder {
    width: 100px;
    height: 100px;
  }
}
</style>





