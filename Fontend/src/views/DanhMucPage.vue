<template>
  <AdminTable
    :data="danhMucs"
    :columns="columns"
    title="Danh Sách Danh Mục"
    title-icon="📁"
    entity-name="danh mục"
    search-placeholder="Tìm kiếm theo tên danh mục..."
    @open-form="openForm"
    @delete-item="deleteDanhMuc"
    @export-excel="exportExcel"
    @toggle-status="toggleDanhMucStatus"
  >
    <!-- Custom cell for danh muc name -->
    <template #cell-tenDanhMuc="{ item }">
      <div class="danh-muc-name">
        <span class="font-medium">{{ item.tenDanhMuc }}</span>
      </div>
    </template>

    <!-- Custom cell for images -->
    <template #cell-hinhAnh="{ item }">
      <div v-if="item.hinhAnhs && item.hinhAnhs.length > 0" class="danh-muc-images">
        <img 
          v-for="(hinh, index) in item.hinhAnhs.slice(0, 3)" 
          :key="index"
          :src="hinh.urlAnh" 
          :alt="item.tenDanhMuc"
          class="danh-muc-thumbnail"
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

    <!-- Custom cell for ma danh muc -->
    <template #cell-maDanhMuc="{ item }">
      <span class="ma-danh-muc">{{ item.maDanhMuc || `DM${String(item.id).padStart(5, '0')}` }}</span>
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
    :title="editingDanhMuc ? 'Sửa Danh Mục' : 'Thêm Danh Mục'"
    :fields="danhMucFields"
    :initial-data="editingDanhMuc ? {
      maDanhMuc: editingDanhMuc.maDanhMuc,
      tenDanhMuc: editingDanhMuc.tenDanhMuc,
      trangThai: editingDanhMuc.trangThai
    } : undefined"
    @submit="handleFormSubmit"
    @cancel="showForm = false"
  >
    <!-- Custom slot for image upload -->
    <template #custom-fields>
      <div class="image-upload-section">
        <label class="form-label">Hình ảnh danh mục</label>
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


  <!-- Toast Component -->
  <Toast ref="toastRef" />

  <!-- Image View Modal -->
  <div v-if="showImageModal" class="image-modal-overlay" @click="closeImageModal">
    <div class="image-modal" @click.stop>
      <div class="image-modal-header">
        <h3>Hình ảnh danh mục</h3>
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
import ConfirmModal from '@/components/ConfirmModal.vue'
import FormModal from '@/components/FormModal.vue'
import Toast from '@/components/Toast.vue'
import AdminTable from '@/components/AdminTable.vue'

interface HinhAnh {
  id: number
  urlAnh: string
  ngayTao: string
  ngaySua: string
  trangThai: number
}

interface DanhMuc {
  id: number
  maDanhMuc: string
  tenDanhMuc: string
  ngayTao?: string
  ngayCapNhat?: string
  trangThai: number
  hinhAnhs?: HinhAnh[]
}

const danhMucs = ref<DanhMuc[]>([])
const loading = ref(false)
const showForm = ref(false)
const editingDanhMuc = ref<DanhMuc | null>(null)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)
const fileInput = ref<HTMLInputElement | null>(null)

// Image modal state
const showImageModal = ref(false)
const currentImages = ref<HinhAnh[]>([])
const currentImageIndex = ref(0)

// Table columns configuration
const columns = [
  { key: 'maDanhMuc', label: 'Mã', class: 'code-col', type: 'code' as const },
  { key: 'tenDanhMuc', label: 'Tên Danh Mục', class: 'name-col' },
  { key: 'hinhAnh', label: 'Hình ảnh', class: 'image-col' },
  { key: 'ngayTao', label: 'Ngày tạo', class: 'date-col', type: 'date' as const },
  { key: 'trangThai', label: 'Trạng thái', class: 'status-col', type: 'status' as const }
]

// Confirm modal state
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const pendingAction = ref<(() => void) | null>(null)

const danhMucFields = [
  { key: 'maDanhMuc', label: 'Mã Danh Mục', type: 'text' as const, required: false },
  { key: 'tenDanhMuc', label: 'Tên Danh Mục', type: 'text' as const, required: true },
  { key: 'trangThai', label: 'Hoạt động', type: 'checkbox' as const }
]

// Image upload state
const selectedFiles = ref([])
const isUploading = ref(false)

// Methods
const loadDanhMucs = async () => {
  try {
    const { data } = await api.get('/api/danh-muc')
    danhMucs.value = data
  } catch (error) {
    console.error('Lỗi khi tải danh sách danh mục:', error)
    showToast('Lỗi khi tải danh sách danh mục', 'error')
  }
}

const openForm = (danhMuc: DanhMuc | null = null) => {
  editingDanhMuc.value = danhMuc
  showForm.value = true
}

const handleFormSubmit = async (formData: any) => {
  try {
    let danhMucId: number
    
    if (editingDanhMuc.value) {
      await api.put(`/api/danh-muc/${editingDanhMuc.value.id}`, formData)
      danhMucId = editingDanhMuc.value.id
      showToast('Cập nhật danh mục thành công!', 'success')
    } else {
      const response = await api.post('/api/danh-muc', formData)
      danhMucId = response.data.id
      showToast('Thêm danh mục thành công!', 'success')
    }
    
    // Upload hình ảnh nếu có
    if (selectedFiles.value.length > 0) {
      await uploadImagesForDanhMuc(danhMucId)
    }
    
    await loadDanhMucs()
    showForm.value = false
    editingDanhMuc.value = null
    selectedFiles.value = []
  } catch (error: any) {
    console.error('Lỗi khi lưu danh mục:', error)
    showToast('Lỗi khi lưu danh mục: ' + (error.response?.data?.message || error.message), 'error')
  }
}

const deleteDanhMuc = (danhMuc: DanhMuc) => {
  confirmTitle.value = 'Xác nhận xóa'
  confirmMessage.value = `Bạn có chắc chắn muốn xóa danh mục "${danhMuc.tenDanhMuc}"?`
  pendingAction.value = async () => {
    try {
      await api.delete(`/api/danh-muc/${danhMuc.id}`)
      showToast('Xóa danh mục thành công!', 'success')
      await loadDanhMucs()
    } catch (error: any) {
      console.error('Lỗi khi xóa danh mục:', error)
      showToast('Lỗi khi xóa danh mục: ' + (error.response?.data?.message || error.message), 'error')
    }
  }
  showConfirmModal.value = true
}

const toggleDanhMucStatus = async (danhMuc: DanhMuc) => {
  try {
    const newStatus = danhMuc.trangThai === 1 ? 0 : 1
    await api.put(`/api/danh-muc/${danhMuc.id}`, { trangThai: newStatus })
    await loadDanhMucs()
    const statusText = newStatus === 1 ? 'Hoạt động' : 'Ngừng hoạt động'
    showToast(`Đã chuyển danh mục "${danhMuc.tenDanhMuc}" sang trạng thái ${statusText}`, 'success')
  } catch (error: any) {
    console.error('Lỗi khi cập nhật trạng thái:', error)
    showToast('Lỗi khi cập nhật trạng thái: ' + (error.response?.data?.message || error.message), 'error')
  }
}

const exportExcel = () => {
  // Implement Excel export logic here
  showToast('Chức năng xuất Excel đang được phát triển', 'info')
}

const handleConfirm = () => {
  if (pendingAction.value) {
    pendingAction.value()
  }
  showConfirmModal.value = false
  pendingAction.value = null
}

const handleCancel = () => {
  showConfirmModal.value = false
  pendingAction.value = null
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


const uploadImagesForDanhMuc = async (danhMucId: number) => {
  if (selectedFiles.value.length === 0) return
  
  isUploading.value = true
  
  try {
    for (const fileData of selectedFiles.value) {
      const formData = new FormData()
      formData.append('file', fileData.file)
      formData.append('idDanhMuc', danhMucId.toString())
      
      await api.post('/api/upload/image/danh-muc', formData, {
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
    toastRef.value[type](title, message, 3000)
  }
}


// Status toggle methods
const isUpdatingStatus = ref(false)

const toggleItemStatus = async (item: any) => {
  if (isUpdatingStatus.value) return
  
  isUpdatingStatus.value = true
  
  try {
    const newStatus = item.trangThai === 1 ? 0 : 1
    await api.put(`/api/danh-muc/${item.id}`, { trangThai: newStatus })
    await loadDanhMucs()
    
    const statusText = newStatus === 1 ? 'Hoạt động' : 'Ngừng hoạt động'
    showToast(`Đã chuyển danh mục "${item.tenDanhMuc}" sang trạng thái ${statusText}`, 'success')
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

const getStatusClass = (item: any) => {
  const status = item.trangThai || 0
  return status === 1 ? 'status-active' : 'status-inactive'
}

const getStatusText = (item: any) => {
  const status = item.trangThai || 0
  return status === 1 ? 'Hoạt động' : 'Ngừng hoạt động'
}

onMounted(() => {
  loadDanhMucs()
})
</script>

<style scoped>
.danh-muc-name {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.danh-muc-images {
  display: flex;
  gap: 4px;
  align-items: center;
  margin-top: 4px;
}

.danh-muc-thumbnail {
  width: 32px;
  height: 32px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #ddd;
}

.more-images {
  font-size: 12px;
  color: #666;
  background: #f0f0f0;
  padding: 2px 6px;
  border-radius: 10px;
}

.ma-danh-muc {
  font-family: 'Courier New', monospace;
  background: #f8f9fa;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.ngay-tao {
  font-size: 12px;
  color: #666;
}

/* Image column styles */
.danh-muc-images {
  display: flex;
  gap: 4px;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
}

.danh-muc-thumbnail {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #ddd;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.danh-muc-thumbnail:hover {
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

/* Image Upload Modal Styles */
.image-upload-modal {
  width: 600px;
  max-width: 90vw;
}

.upload-section {
  margin-bottom: 20px;
}

.upload-area {
  border: 2px dashed #ddd;
  border-radius: 8px;
  padding: 40px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fafafa;
}

.upload-area:hover {
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

.btn-primary {
  background: #007bff;
  color: white;
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-primary:disabled {
  background: #6c757d;
  cursor: not-allowed;
}

.btn-secondary {
  background: #6c757d;
  color: white;
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  max-height: 90vh;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.modal-close {
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
}

.modal-body {
  padding: 20px;
  overflow-y: auto;
  flex: 1;
  min-height: 0;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #eee;
  flex-shrink: 0;
}

/* Action buttons */
.action-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: center;
}

.edit-btn, .upload-btn {
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

.upload-btn:hover {
  background-color: #f0f9ff;
  transform: translateY(-1px);
}

.action-icon {
  width: 18px;
  height: 18px;
  object-fit: contain;
}

/* Status styles */
.status-active {
  background: #28a745;
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-inactive {
  background: #dc3545;
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
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

/* Form Image Upload Styles */
.image-upload-section {
  margin-top: 20px;
  padding: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background: #f9f9f9;
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
