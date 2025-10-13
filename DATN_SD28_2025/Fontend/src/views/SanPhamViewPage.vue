<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/services/api'

interface SanPhamDetail {
  id: number
  maSanPham: string
  tenSanPham: string
  moTa?: string
  thietKe?: string
  kichThuoc?: string
  ngayTao?: string
  ngayCapNhat?: string
  trangThai: number
  tenDanhMuc?: string
  tenHang?: string
  tenManHinh?: string
  tenCameraTruoc?: string
  tenCameraSau?: string
  tenChip?: string
  tenGpu?: string
  tenSim?: string
  tenHeDieuHanh?: string
  tenCpu?: string
  tenPin?: string
  tongImei?: number
  variants?: VariantDetail[]
}

interface VariantDetail {
  id: number
  idRam: number
  idRom: number
  idMauSac: number
  soLuong: number
  donGia: number
  giaNhap?: number
  ghiChu?: string
  tenRam?: string
  tenRom?: string
  tenMauSac?: string
  imeis?: string[]
  imageUrls?: string[]
}

const route = useRoute()
const router = useRouter()
const productId = computed(() => route.params.id as string)
const product = ref<SanPhamDetail | null>(null)
const loading = ref(false)

async function loadProductDetail() {
  if (!productId.value) return
  
  loading.value = true
  try {
    const { data } = await api.get<SanPhamDetail>(`/api/san-pham/${productId.value}/view`)
    product.value = data
  } catch (error) {
    console.error('Lỗi khi tải chi tiết sản phẩm:', error)
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push({ name: 'san-pham' })
}

function editProduct() {
  router.push({ name: 'san-pham-form', params: { id: productId.value } })
}

function formatPrice(price: number) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

function getStatusText(status: number) {
  return status === 1 ? 'Hoạt động' : 'Đã đóng'
}

function getStatusClass(status: number) {
  return status === 1 ? 'status-active' : 'status-inactive'
}

function getStockStatus(quantity: number) {
  return quantity > 0 ? 'Còn hàng' : 'Hết hàng'
}

function getStockClass(quantity: number) {
  return quantity > 0 ? 'stock-in' : 'stock-out'
}

function createFullImageUrl(url: string): string {
  // Nếu URL đã có protocol, trả về nguyên
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  // Nếu URL bắt đầu với /, thêm domain
  if (url.startsWith('/')) {
    return `http://localhost:8080${url}`
  }
  // Nếu không, thêm domain và /
  return `http://localhost:8080/${url}`
}

onMounted(() => {
  loadProductDetail()
})
</script>

<template>
  <div class="page">
    <!-- Header -->
    <div class="header">
      <div class="header-left">
        <button class="btn-back" @click="goBack">← Quay lại</button>
        <h1>Chi Tiết Sản Phẩm</h1>
      </div>
      <div class="header-actions">
        <button class="btn-edit" @click="editProduct">✎ Chỉnh sửa</button>
      </div>
    </div>

    <div v-if="loading" class="loading">
      Đang tải...
    </div>

    <div v-else-if="product" class="content">
      <!-- Product Info Card -->
      <div class="info-card">
        <div class="card-header">
          <h2>Thông tin cơ bản</h2>
        </div>
        <div class="card-content">
          <div class="info-grid">
            <div class="info-item">
              <label>Mã sản phẩm:</label>
              <span>{{ product.maSanPham }}</span>
            </div>
            <div class="info-item">
              <label>Tên sản phẩm:</label>
              <span class="product-name">{{ product.tenSanPham }}</span>
            </div>
            <div class="info-item">
              <label>Hãng:</label>
              <span>{{ product.tenHang || '-' }}</span>
            </div>
            <div class="info-item">
              <label>Danh mục:</label>
              <span>{{ product.tenDanhMuc || '-' }}</span>
            </div>
            <div class="info-item">
              <label>Hệ điều hành:</label>
              <span>{{ product.tenHeDieuHanh || '-' }}</span>
            </div>
            <div class="info-item">
              <label>Màn hình:</label>
              <span>{{ product.tenManHinh || '-' }}</span>
            </div>
            <div class="info-item">
              <label>Camera trước:</label>
              <span>{{ product.tenCameraTruoc || '-' }}</span>
            </div>
            <div class="info-item">
              <label>Camera sau:</label>
              <span>{{ product.tenCameraSau || '-' }}</span>
            </div>
            <div class="info-item">
              <label>Chip:</label>
              <span>{{ product.tenChip || '-' }}</span>
            </div>
            <div class="info-item">
              <label>GPU:</label>
              <span>{{ product.tenGpu || '-' }}</span>
            </div>
            <div class="info-item">
              <label>CPU:</label>
              <span>{{ product.tenCpu || '-' }}</span>
            </div>
            <div class="info-item">
              <label>Pin:</label>
              <span>{{ product.tenPin || '-' }}</span>
            </div>
            <div class="info-item">
              <label>Sim:</label>
              <span>{{ product.tenSim || '-' }}</span>
            </div>
            <div class="info-item">
              <label>Thiết kế:</label>
              <span>{{ product.thietKe || '-' }}</span>
            </div>
            <div class="info-item">
              <label>Kích thước:</label>
              <span>{{ product.kichThuoc || '-' }}</span>
            </div>
            <div class="info-item">
              <label>Tổng số lượng:</label>
              <span class="quantity">{{ product.tongImei || 0 }}</span>
            </div>
            <div class="info-item">
              <label>Trạng thái:</label>
              <span :class="getStatusClass(product.trangThai)">
                {{ getStatusText(product.trangThai) }}
              </span>
            </div>
          </div>
          
          <div class="description">
            <label>Mô tả sản phẩm:</label>
            <div class="description-content">
              <p v-if="product.moTa">{{ product.moTa }}</p>
              <p v-else class="no-description">Chưa có mô tả cho sản phẩm này</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Variants Card -->
      <div v-if="product.variants && product.variants.length > 0" class="variants-card">
        <div class="card-header">
          <h2>Phiên bản sản phẩm</h2>
        </div>
        <div class="card-content">
          <div class="variants-grid">
            <div v-for="(variant, index) in product.variants" :key="variant.id" class="variant-item">
              <div class="variant-header">
                <h3>Phiên bản {{ index + 1 }}</h3>
                <span :class="getStockClass(variant.soLuong)">
                  {{ getStockStatus(variant.soLuong) }}
                </span>
              </div>
              
              <div class="variant-info">
                <div class="variant-specs">
                  <div class="spec-item">
                    <label>RAM:</label>
                    <span>{{ variant.tenRam || '-' }}</span>
                  </div>
                  <div class="spec-item">
                    <label>ROM:</label>
                    <span>{{ variant.tenRom || '-' }}</span>
                  </div>
                  <div class="spec-item">
                    <label>Màu sắc:</label>
                    <span>{{ variant.tenMauSac || '-' }}</span>
                  </div>
                  <div class="spec-item">
                    <label>Số lượng:</label>
                    <span class="quantity">{{ variant.soLuong }}</span>
                  </div>
                  <div class="spec-item">
                    <label>Đơn giá:</label>
                    <span class="price">{{ formatPrice(variant.donGia) }}</span>
                  </div>
                  <div class="spec-item">
                    <label>Giá nhập:</label>
                    <span class="price">{{ variant.giaNhap ? formatPrice(variant.giaNhap) : 'Chưa cập nhật' }}</span>
                  </div>
                </div>
                
                <!-- Ghi chú -->
                <div class="variant-note">
                  <label>Ghi chú:</label>
                  <p>{{ variant.ghiChu || 'Không có ghi chú' }}</p>
                </div>
                
                <!-- Images -->
                <div v-if="variant.imageUrls && variant.imageUrls.length > 0" class="variant-images">
                  <label>Hình ảnh:</label>
                  <div class="images-grid">
                    <div v-for="(imageUrl, imgIndex) in variant.imageUrls" :key="imgIndex" class="image-item">
                      <img :src="createFullImageUrl(imageUrl)" :alt="`${variant.tenMauSac} - ${imgIndex + 1}`" />
                    </div>
                  </div>
                </div>
                
                <!-- IMEIs -->
                <div v-if="variant.imeis && variant.imeis.length > 0" class="variant-imeis">
                  <label>IMEI ({{ variant.imeis.length }}):</label>
                  <div class="imeis-list">
                    <span v-for="(imei, imeiIndex) in variant.imeis" :key="imeiIndex" class="imei-item">
                      {{ imei }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="error">
      Không tìm thấy sản phẩm
    </div>
  </div>
</template>

<style scoped>
.page {
  padding: 20px;
  background: #f5f5f5;
  min-height: 100vh;
}

/* Header */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.btn-back {
  background: #6c757d;
  color: white;
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-back:hover {
  background: #5a6268;
}

.header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.btn-edit {
  background: #28a745;
  color: white;
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-edit:hover {
  background: #218838;
}

/* Loading & Error */
.loading, .error {
  text-align: center;
  padding: 40px;
  font-size: 16px;
  color: #6c757d;
}

/* Cards */
.info-card, .variants-card {
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.card-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e0e0e0;
  background: #f8f9fa;
  border-radius: 8px 8px 0 0;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.card-content {
  padding: 20px;
}

/* Info Grid */
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item label {
  font-size: 14px;
  font-weight: 500;
  color: #666;
}

.info-item span {
  font-size: 14px;
  color: #333;
}

.product-name {
  color: #007bff;
  font-weight: 600;
}

.quantity {
  color: #28a745;
  font-weight: 600;
}

.price {
  color: #dc3545;
  font-weight: 600;
}

.status-active {
  color: #28a745;
  font-weight: 600;
}

.status-inactive {
  color: #dc3545;
  font-weight: 600;
}

.stock-in {
  background: #28a745;
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.stock-out {
  background: #dc3545;
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

/* Description */
.description {
  margin-top: 20px;
}

.description label {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  display: block;
  margin-bottom: 12px;
  border-bottom: 2px solid #007bff;
  padding-bottom: 4px;
}

.description-content {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  border-left: 4px solid #007bff;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.description-content p {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  margin: 0;
  text-align: justify;
}

.description-content .no-description {
  color: #6c757d;
  font-style: italic;
  text-align: center;
  padding: 20px;
}

/* Variants */
.variants-grid {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.variant-item {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
}

.variant-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f8f9fa;
  border-bottom: 1px solid #e0e0e0;
}

.variant-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.variant-info {
  padding: 16px;
}

.variant-specs {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.spec-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.spec-item label {
  font-size: 12px;
  font-weight: 500;
  color: #666;
}

.spec-item span {
  font-size: 14px;
  color: #333;
}

/* Images */
.variant-images {
  margin-bottom: 16px;
}

.variant-images label {
  font-size: 14px;
  font-weight: 500;
  color: #666;
  display: block;
  margin-bottom: 8px;
}

.images-grid {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.image-item {
  width: 80px;
  height: 80px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
}

.image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* Variant Note */
.variant-note {
  margin-bottom: 16px;
}

.variant-note label {
  font-size: 14px;
  font-weight: 500;
  color: #666;
  display: block;
  margin-bottom: 8px;
}

.variant-note p {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
  margin: 0;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 4px;
  border-left: 3px solid #007bff;
}

/* IMEIs */
.variant-imeis {
  margin-bottom: 16px;
}

.variant-imeis label {
  font-size: 14px;
  font-weight: 500;
  color: #666;
  display: block;
  margin-bottom: 8px;
}

.imeis-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.imei-item {
  background: #f8f9fa;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #333;
  border: 1px solid #e0e0e0;
}
</style>
