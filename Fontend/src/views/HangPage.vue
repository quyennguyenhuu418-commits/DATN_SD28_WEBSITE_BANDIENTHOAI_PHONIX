<template>
  <AdminTable
    :data="hangs"
    :columns="hangColumns"
    title="Danh Sách Hãng"
    titleIcon="📋"
    entityName="hãng"
    searchPlaceholder="Tìm kiếm theo tên hãng..."
    @openForm="openForm"
    @deleteItem="deleteHang"
    @exportExcel="exportExcel"
    @toggleStatus="toggleHangStatus"
  >
    <!-- Custom cell for hang name -->
    <template #cell-ten="{ item }">
      <div class="hang-name">
        <span class="font-medium">{{ item.ten }}</span>
      </div>
    </template>

    <!-- Custom cell for images -->
    <template #cell-hinhAnh="{ item }">
      <div v-if="item.hinhAnhs && item.hinhAnhs.length > 0" class="hang-images">
        <img 
          v-for="(hinh, index) in item.hinhAnhs.slice(0, 3)" 
          :key="index"
          :src="hinh.urlAnh" 
          :alt="item.ten"
          class="hang-thumbnail"
          @error="handleImageError"
          @click="openImageModal(item.hinhAnhs, index)"
        />
        <span v-if="item.hinhAnhs.length > 3" class="more-images" @click="openImageModal(item.hinhAnhs, 0)">
          +{{ item.hinhAnhs.length - 3 }}
        </span>
      </div>
      <div v-else class="no-image">
        <span class="no-image-text">Chưa có hình ảnh</span>
      </div>
    </template>

    <!-- Custom cell for ngay tao -->
    <template #cell-ngayTao="{ item }">
      <span class="ngay-tao">{{ formatDate(item.ngayTao) }}</span>
    </template>

    <!-- Custom action buttons -->
    <template #action-buttons="{ item }">
      <div class="action-buttons">
        <button class="edit-btn" @click="openForm(item)">
          <img src="/src/assets/edit.png" alt="Sửa" class="action-icon" />
        </button>
        <div class="status-toggle">
          <label class="toggle-switch" :title="getToggleTooltip(item)">
            <input 
              type="checkbox" 
              :checked="(item.trangThai || 0) === 1"
              @change="toggleItemStatus(item)"
              :disabled="isUpdatingStatus"
            />
            <span class="toggle-slider"></span>
          </label>
        </div>
      </div>
    </template>
  </AdminTable>

  <!-- Confirm Modal -->
  <ConfirmModal
    :show="showConfirmModal"
    :title="confirmTitle"
    :message="confirmMessage"
    @confirm="handleConfirm"
    @cancel="handleCancel"
  />

  <FormModal
    :show="showForm"
    :title="editingHang ? 'Sửa Hãng' : 'Thêm Hãng'"
    :fields="hangFields"
    :initial-data="editingHang ? {
      ten: editingHang.ten,
      xuatXu: editingHang.xuatXu || '',
      moTa: editingHang.moTa || '',
      trangThai: editingHang.trangThai
    } : undefined"
    @submit="handleFormSubmit"
    @cancel="showForm = false"
  >
    <!-- Custom slot for image upload -->
    <template #custom-fields>
      <div class="image-upload-section">
        <label class="form-label">Hình ảnh hãng</label>
        
        <!-- Hiển thị ảnh cũ nếu đang chỉnh sửa -->
        <div v-if="editingHang && editingHang.hinhAnhs && editingHang.hinhAnhs.length > 0" class="existing-images">
          <h4 class="existing-images-title">Ảnh hiện tại:</h4>
          <div class="existing-images-grid">
            <div v-for="(hinh, index) in editingHang.hinhAnhs" :key="hinh.id" class="existing-image-item">
              <img 
                :src="hinh.urlAnh" 
                :alt="`Ảnh ${index + 1}`"
                class="existing-image"
                @click="openImageModal(editingHang.hinhAnhs, index)"
              />
              <button 
                @click.stop="removeExistingImage(hinh.id, index)" 
                class="remove-existing-image"
                title="Xóa ảnh này"
              >
                ×
              </button>
            </div>
          </div>
        </div>
        
        <div class="image-upload-area">
          <div 
            class="upload-zone" 
            @click="triggerFileInput"
            @dragover.prevent 
            @drop.prevent="handleDrop"
          >
            <div class="upload-placeholder">
              <div class="upload-icon">📁</div>
              <p class="upload-hint">Kéo thả hình ảnh vào đây hoặc click để chọn</p>
              <p class="upload-hint">Hỗ trợ: JPG, PNG, GIF (tối đa 5MB)</p>
            </div>
          </div>
          <input 
            type="file" 
            ref="fileInput"
            multiple 
            accept="image/*"
            @change="handleFileSelect"
            style="display: none"
          />
          <div v-if="selectedFiles.length > 0" class="selected-files">
            <h4 class="new-images-title">Ảnh mới sẽ thêm:</h4>
            <div v-for="(file, index) in selectedFiles" :key="index" class="file-preview">
              <img 
                v-if="file.preview" 
                :src="file.preview" 
                :alt="file.name"
                class="preview-image"
              />
              <div class="file-info">
                <div class="file-name">{{ file.name }}</div>
                <div class="file-size">{{ formatFileSize(file.size) }}</div>
              </div>
              <button @click="removeFile(index)" class="remove-file">×</button>
            </div>
          </div>
        </div>
      </div>
    </template>
  </FormModal>

  <Toast ref="toastRef" />

  <!-- Image View Modal -->
  <div v-if="showImageModal" class="image-modal-overlay" @click="closeImageModal">
    <div class="image-modal" @click.stop>
      <div class="image-modal-header">
        <h3>Hình ảnh hãng</h3>
        <button class="close-btn" @click="closeImageModal">×</button>
      </div>
      <div class="image-modal-content">
        <div class="image-gallery">
          <img 
            v-for="(hinh, index) in currentImages" 
            :key="index"
            :src="hinh.urlAnh" 
            :alt="`Hình ${index + 1}`"
            class="gallery-image"
            :class="{ active: currentImageIndex === index }"
            @click="setCurrentImage(index)"
          />
        </div>
        <div class="main-image-container">
          <img 
            v-if="currentImages && currentImages.length > 0"
            :src="currentImages[currentImageIndex]?.urlAnh" 
            :alt="`Hình ${currentImageIndex + 1}`"
            class="main-image"
            @error="handleImageError"
          />
          <div v-else class="no-image-placeholder">
            <span>Không có hình ảnh</span>
          </div>
        </div>
        <div class="image-navigation" v-if="currentImages && currentImages.length > 1">
          <button 
            class="nav-btn prev-btn" 
            @click="previousImage"
            :disabled="currentImageIndex === 0"
          >
            ‹
          </button>
          <span class="image-counter">
            {{ currentImageIndex + 1 }} / {{ currentImages.length }}
          </span>
          <button 
            class="nav-btn next-btn" 
            @click="nextImage"
            :disabled="currentImageIndex === currentImages.length - 1"
          >
            ›
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import api from '@/services/api'
import AdminTable from '@/components/AdminTable.vue'
import ConfirmModal from '@/components/ConfirmModal.vue'
import FormModal from '@/components/FormModal.vue'
import Toast from '@/components/Toast.vue'
import '@/styles/admin-layout.css'

interface HinhAnh {
  id: number
  urlAnh: string
  ngayTao: string
  ngaySua: string
  trangThai: number
}

interface Hang {
  id: number
  ten: string
  xuatXu?: string
  moTa?: string
  ngayTao?: string
  ngayCapNhat?: string
  trangThai: number
  hinhAnhs?: HinhAnh[]
}

const hangs = ref<Hang[]>([])
const loading = ref(false)
const showForm = ref(false)
const editingHang = ref<Hang | null>(null)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)
const fileInput = ref<HTMLInputElement | null>(null)

// Image modal state
const showImageModal = ref(false)
const currentImages = ref<HinhAnh[]>([])
const currentImageIndex = ref(0)

// Image upload state
const selectedFiles = ref([])
const isUploading = ref(false)

// Images to be deleted (temporary removal)
const imagesToDelete = ref([])

// Confirm modal state
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const pendingAction = ref<(() => void) | null>(null)

const hangColumns = [
  { key: 'ten', label: 'Tên Hãng', class: 'name-col' },
  { key: 'xuatXu', label: 'Xuất xứ', class: 'origin-col' },
  { key: 'hinhAnh', label: 'Hình ảnh', class: 'image-col' },
  { key: 'moTa', label: 'Mô tả', class: 'desc-col' },
  { key: 'ngayTao', label: 'Ngày tạo', class: 'date-col', type: 'date' as const },
  { key: 'trangThai', label: 'Trạng thái', class: 'status-col', type: 'status' as const }
]

const hangFields = [
  { key: 'ten', label: 'Tên Hãng', type: 'text' as const, required: true },
  { key: 'xuatXu', label: 'Xuất xứ', type: 'text' as const },
  { key: 'moTa', label: 'Mô tả', type: 'textarea' as const },
  { key: 'trangThai', label: 'Hoạt động', type: 'checkbox' as const }
]

async function loadHangs() {
  loading.value = true
  try {
    const { data } = await api.get<Hang[]>('/api/hang')
    hangs.value = data
  } finally {
    loading.value = false
  }
}

function openForm(hang?: Hang) {
  editingHang.value = hang || null
  selectedFiles.value = [] // Reset selected files when opening form
  imagesToDelete.value = [] // Reset images to delete when opening form
  showForm.value = true
}

async function handleFormSubmit(data: any) {
  try {
    let hangId: number
    
    if (editingHang.value) {
      await api.put(`/api/hang/${editingHang.value.id}`, data)
      hangId = editingHang.value.id
      showToast('Cập nhật hãng thành công!', 'success')
    } else {
      const response = await api.post('/api/hang', data)
      hangId = response.data.id
      showToast('Thêm hãng thành công!', 'success')
    }
    
    // Xóa ảnh đã đánh dấu xóa
    if (imagesToDelete.value.length > 0) {
      await deleteMarkedImages()
    }
    
    // Upload hình ảnh nếu có
    if (selectedFiles.value.length > 0) {
      await uploadImagesForHang(hangId)
    }
    
    await loadHangs()
    showForm.value = false
    editingHang.value = null
    selectedFiles.value = []
    imagesToDelete.value = []
  } catch (error: any) {
    console.error('Lỗi khi lưu:', error)
    showToast('Lỗi khi lưu hãng: ' + (error.response?.data?.message || error.message), 'error')
  }
}

function deleteHang(id: number) {
  const hang = hangs.value.find(h => h.id === id)
  confirmTitle.value = 'Xác nhận xóa Hãng'
  confirmMessage.value = `Bạn có chắc chắn muốn xóa hãng "${hang?.ten || 'này'}"? Hành động này không thể hoàn tác.`
  pendingAction.value = () => performDelete(id)
  showConfirmModal.value = true
}

async function performDelete(id: number) {
  try {
    await api.delete(`/api/hang/${id}`)
    toastRef.value?.success('Thành công', 'Xóa hãng thành công!')
    await loadHangs()
  } catch (error: any) {
    console.error('Lỗi khi xóa:', error)
    if (error.response?.data) {
      toastRef.value?.error('Không thể xóa', error.response.data)
    } else {
      toastRef.value?.error('Lỗi xóa Hãng', 'Có lỗi xảy ra khi xóa hãng')
    }
  }
}

function handleConfirm() {
  if (pendingAction.value) {
    pendingAction.value()
  }
  showConfirmModal.value = false
  pendingAction.value = null
}

function handleCancel() {
  showConfirmModal.value = false
  pendingAction.value = null
}

async function toggleHangStatus(hang: Hang) {
  try {
    const newStatus = hang.trangThai === 1 ? 0 : 1
    await api.put(`/api/hang/${hang.id}/status`, { trangThai: newStatus })
    
    // Update local data
    const index = hangs.value.findIndex(h => h.id === hang.id)
    if (index !== -1) {
      hangs.value[index].trangThai = newStatus
    }
  } catch (error: any) {
    console.error('Lỗi khi cập nhật trạng thái:', error)
    if (error.response?.data) {
      toastRef.value?.error('Lỗi cập nhật trạng thái', error.response.data)
    } else {
      toastRef.value?.error('Lỗi cập nhật trạng thái', 'Có lỗi xảy ra khi cập nhật trạng thái')
    }
  }
}

async function exportExcel() {
  try {
    const response = await api.get('/api/hang/export', { responseType: 'blob' })
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', 'danh_sach_hang.xlsx')
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
    toastRef.value?.success('Thành công', 'Xuất Excel thành công!')
  } catch (error: any) {
    console.error('Lỗi khi xuất Excel:', error)
    toastRef.value?.error('Lỗi xuất Excel', 'Có lỗi xảy ra khi xuất file Excel')
  }
}

// Image upload methods
const triggerFileInput = () => {
  const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement
  fileInput?.click()
}

const handleFileSelect = (event) => {
  const files = Array.from(event.target.files)
  processFiles(files)
}

const handleDrop = (event) => {
  const files = Array.from(event.dataTransfer.files)
  processFiles(files)
}

const processFiles = (files) => {
  const imageFiles = files.filter(file => file.type.startsWith('image/'))
  
  imageFiles.forEach(file => {
    if (file.size > 5 * 1024 * 1024) { // 5MB limit
      showToast(`File ${file.name} quá lớn (tối đa 5MB)`, 'warning')
      return
    }
    
    const reader = new FileReader()
    reader.onload = (e) => {
      selectedFiles.value.push({
        file,
        name: file.name,
        size: file.size,
        preview: e.target.result
      })
    }
    reader.readAsDataURL(file)
  })
}

const removeFile = (index) => {
  selectedFiles.value.splice(index, 1)
}

const removeExistingImage = (imageId, index) => {
  // Thêm ảnh vào danh sách sẽ xóa (chỉ xóa tạm thời khỏi UI)
  imagesToDelete.value.push(imageId)
  
  // Xóa ảnh khỏi danh sách hiện tại (chỉ trong UI)
  if (editingHang.value && editingHang.value.hinhAnhs) {
    editingHang.value.hinhAnhs.splice(index, 1)
  }
  
  showToast('Ảnh sẽ được xóa khi bạn xác nhận!', 'info')
}

const deleteMarkedImages = async () => {
  try {
    for (const imageId of imagesToDelete.value) {
      await api.delete(`/api/hinh-anh/${imageId}`)
    }
    showToast(`Đã xóa ${imagesToDelete.value.length} ảnh!`, 'success')
  } catch (error: any) {
    console.error('Lỗi khi xóa ảnh:', error)
    showToast('Lỗi khi xóa ảnh: ' + (error.response?.data?.message || error.message), 'error')
  }
}

const uploadImagesForHang = async (hangId: number) => {
  if (selectedFiles.value.length === 0) return
  
  isUploading.value = true
  
  try {
    for (const fileData of selectedFiles.value) {
      const formData = new FormData()
      formData.append('file', fileData.file)
      formData.append('idHang', hangId.toString())
      
      await api.post('/api/upload/image/hang', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
    }
    
    showToast(`Upload thành công ${selectedFiles.value.length} hình ảnh!`, 'success')
  } catch (error: any) {
    console.error('Lỗi khi upload hình ảnh:', error)
    showToast('Lỗi khi upload hình ảnh: ' + (error.response?.data?.message || error.message), 'error')
  } finally {
    isUploading.value = false
  }
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (error) {
    return '-'
  }
}

const handleImageError = (event) => {
  event.target.style.display = 'none'
}

// Image modal methods
const openImageModal = (images, startIndex = 0) => {
  currentImages.value = images
  currentImageIndex.value = startIndex
  showImageModal.value = true
}

const closeImageModal = () => {
  showImageModal.value = false
  currentImages.value = []
  currentImageIndex.value = 0
}

const setCurrentImage = (index) => {
  currentImageIndex.value = index
}

const previousImage = () => {
  if (currentImageIndex.value > 0) {
    currentImageIndex.value--
  }
}

const nextImage = () => {
  if (currentImageIndex.value < currentImages.value.length - 1) {
    currentImageIndex.value++
  }
}

const showToast = (message, type = 'success') => {
  if (toastRef.value) {
    const title = type === 'success' ? 'Thành công' : type === 'error' ? 'Lỗi' : type === 'warning' ? 'Cảnh báo' : 'Thông báo'
    if (type === 'success') {
      toastRef.value.success(title, message, 3000)
    } else if (type === 'error') {
      toastRef.value.error(title, message, 3000)
    } else if (type === 'warning') {
      toastRef.value.warning(title, message, 3000)
    } else {
      toastRef.value.info(title, message, 3000)
    }
  }
}

// Status toggle methods
const isUpdatingStatus = ref(false)

const toggleItemStatus = async (item: any) => {
  if (isUpdatingStatus.value) return
  
  isUpdatingStatus.value = true
  
  try {
    const newStatus = item.trangThai === 1 ? 0 : 1
    await api.put(`/api/hang/${item.id}`, { trangThai: newStatus })
    await loadHangs()
    
    const statusText = newStatus === 1 ? 'Hoạt động' : 'Ngừng hoạt động'
    showToast(`Đã chuyển hãng "${item.ten}" sang trạng thái ${statusText}`, 'success')
  } catch (error: any) {
    console.error('Lỗi khi cập nhật trạng thái:', error)
    showToast('Lỗi khi cập nhật trạng thái: ' + (error.response?.data?.message || error.message), 'error')
  } finally {
    isUpdatingStatus.value = false
  }
}

const getToggleTooltip = (item: any) => {
  const status = item.trangThai || 0
  return status === 1 ? 'Chuyển sang ngừng hoạt động' : 'Chuyển sang hoạt động'
}

onMounted(() => {
  loadHangs()
})
</script>

<style scoped>
/* Custom column widths for Hang page */
:deep(.data-table .code-col) {
  width: 120px;
  min-width: 120px;
}

:deep(.data-table .name-col) {
  min-width: 150px;
}

:deep(.data-table .origin-col) {
  min-width: 120px;
}

:deep(.data-table .desc-col) {
  min-width: 200px;
  max-width: 300px;
  word-wrap: break-word;
}

:deep(.data-table .status-col) {
  width: 120px;
  min-width: 120px;
  text-align: center;
}

:deep(.data-table .date-col) {
  width: 120px;
  min-width: 120px;
  text-align: center;
  font-size: 13px;
}

:deep(.data-table .action-col) {
  width: 100px;
  min-width: 100px;
  text-align: center;
}

/* Image column styles */
.hang-images {
  display: flex;
  gap: 4px;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
}

.hang-thumbnail {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #ddd;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.hang-thumbnail:hover {
  transform: scale(1.1);
  border-color: #007bff;
}

.more-images {
  font-size: 11px;
  color: #666;
  background: #f0f0f0;
  padding: 2px 6px;
  border-radius: 10px;
  font-weight: 500;
}

.no-image {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 40px;
  background: #f8f9fa;
  border: 1px dashed #ddd;
  border-radius: 4px;
}

.no-image-text {
  font-size: 11px;
  color: #999;
  font-style: italic;
}

.ngay-tao {
  font-size: 12px;
  color: #666;
}

/* Form Image Upload Styles */
.image-upload-section {
  margin-top: 20px;
  padding: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background: #f9f9f9;
}

/* Existing Images Styles */
.existing-images {
  margin-bottom: 20px;
  padding: 15px;
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
}

.existing-images-title {
  margin: 0 0 15px 0;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.existing-images-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 10px;
}

.existing-image-item {
  position: relative;
  display: inline-block;
}

.existing-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
  border: 2px solid #ddd;
  cursor: pointer;
  transition: all 0.2s ease;
}

.existing-image:hover {
  border-color: #007bff;
  transform: scale(1.05);
}

.remove-existing-image {
  position: absolute;
  top: -8px;
  right: -8px;
  background: #dc3545;
  color: white;
  border: none;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: bold;
  transition: all 0.2s ease;
  box-shadow: 0 2px 4px rgba(0,0,0,0.2);
}

.remove-existing-image:hover {
  background: #c82333;
  transform: scale(1.1);
}

.new-images-title {
  margin: 15px 0 10px 0;
  font-size: 14px;
  font-weight: 600;
  color: #007bff;
}

.form-label {
  display: block;
  margin-bottom: 10px;
  font-weight: 500;
  color: #333;
}

.image-upload-area {
  width: 100%;
}

.upload-zone {
  border: 2px dashed #ddd;
  border-radius: 8px;
  padding: 40px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fafafa;
}

.upload-zone:hover {
  border-color: #007bff;
  background: #f0f8ff;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.upload-icon {
  font-size: 48px;
  opacity: 0.5;
}

.upload-hint {
  font-size: 12px;
  color: #666;
  margin: 0;
}

.selected-files {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.file-preview {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: white;
}

.preview-image {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
}

.file-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.file-name {
  font-weight: 500;
  font-size: 14px;
}

.file-size {
  font-size: 12px;
  color: #666;
}

.remove-file {
  background: #dc3545;
  color: white;
  border: none;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.remove-file:hover {
  background: #c82333;
}

/* Action buttons */
.action-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: center;
}

.edit-btn {
  background: none;
  border: none;
  padding: 8px;
  margin: 0 4px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.edit-btn:hover {
  background-color: #e3f2fd;
  transform: translateY(-1px);
}

.action-icon {
  width: 18px;
  height: 18px;
  object-fit: contain;
}

/* Toggle Switch Styles */
.status-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
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

.toggle-switch input:checked + .toggle-slider {
  background-color: #28a745;
}

.toggle-switch input:checked + .toggle-slider:before {
  transform: translateX(26px);
}

.toggle-switch input:disabled + .toggle-slider {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Image Modal Styles */
.image-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.image-modal {
  background: white;
  border-radius: 12px;
  max-width: 90vw;
  max-height: 90vh;
  width: 800px;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
}

.image-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
  background: #f8f9fa;
}

.image-modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s ease;
}

.close-btn:hover {
  background: #f0f0f0;
  color: #333;
}

.image-modal-content {
  padding: 20px;
}

.image-gallery {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  overflow-x: auto;
  padding-bottom: 8px;
}

.gallery-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.gallery-image:hover {
  border-color: #007bff;
  transform: scale(1.05);
}

.gallery-image.active {
  border-color: #007bff;
  box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.2);
}

.main-image-container {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 20px;
  min-height: 400px;
  background: #f8f9fa;
  border-radius: 8px;
  overflow: hidden;
}

.main-image {
  max-width: 100%;
  max-height: 400px;
  object-fit: contain;
  border-radius: 8px;
}

.no-image-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #666;
  font-style: italic;
}

.image-navigation {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
}

.nav-btn {
  background: #007bff;
  color: white;
  border: none;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 18px;
  font-weight: bold;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-btn:hover:not(:disabled) {
  background: #0056b3;
  transform: scale(1.1);
}

.nav-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: none;
}

.image-counter {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}
</style>