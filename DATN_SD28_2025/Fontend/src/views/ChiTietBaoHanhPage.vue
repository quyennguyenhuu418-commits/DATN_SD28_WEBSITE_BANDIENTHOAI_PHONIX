<template>
  <div class="bao-hanh-detail-page">
    <PosHeader />
    <Toast ref="toastRef" />
    
    <div class="detail-container">
      <div class="detail-header">
        <div class="header-left">
          <button class="btn-back" @click="goBack">
            <FontAwesomeIcon :icon="['fas', 'arrow-left']" />
            Quay lại
          </button>
          <h1>Chi tiết Phiếu Bảo hành</h1>
        </div>
        <div class="header-actions" v-if="phieuBaoHanh && !loading">
          <span class="status-badge-large" :class="getStatusClass(phieuBaoHanh.trangThai)">
            {{ getStatusText(phieuBaoHanh.trangThai) }}
          </span>
        </div>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>Đang tải thông tin phiếu bảo hành...</p>
      </div>

      <!-- Error -->
      <div v-else-if="error" class="error-container">
        <div class="error-icon">
          <FontAwesomeIcon :icon="['fas', 'exclamation-triangle']" />
        </div>
        <h3>Không thể tải thông tin phiếu bảo hành</h3>
        <p>{{ error }}</p>
        <button class="btn-retry" @click="loadPhieuBaoHanh">
          <FontAwesomeIcon :icon="['fas', 'redo']" />
          Thử lại
        </button>
      </div>

      <!-- Content -->
      <div v-else-if="phieuBaoHanh" class="detail-content">
        <!-- Thông tin cơ bản -->
        <div class="detail-card">
          <div class="card-header">
            <h2>Thông tin Cơ bản</h2>
          </div>
          <div class="card-body">
            <div class="info-grid">
              <div class="info-item">
                <label>Mã phiếu:</label>
                <span class="info-value">{{ phieuBaoHanh.maPhieu }}</span>
              </div>
              <div class="info-item">
                <label>Tên khách hàng:</label>
                <span class="info-value">{{ phieuBaoHanh.tenKhachHang }}</span>
              </div>
              <div class="info-item">
                <label>Số điện thoại:</label>
                <span class="info-value">{{ phieuBaoHanh.soDienThoai }}</span>
              </div>
              <div class="info-item">
                <label>Tên sản phẩm:</label>
                <span class="info-value">{{ phieuBaoHanh.tenSanPham }}</span>
              </div>
              <div class="info-item">
                <label>IMEI/Serial:</label>
                <span class="info-value">{{ phieuBaoHanh.imeiSerial || '-' }}</span>
              </div>
              <div class="info-item">
                <label>Ngày nhận:</label>
                <span class="info-value">{{ formatDate(phieuBaoHanh.ngayNhan) }}</span>
              </div>
              <div class="info-item">
                <label>Ngày hẹn trả:</label>
                <span class="info-value">{{ formatDate(phieuBaoHanh.ngayHenTraDuKien) }}</span>
              </div>
              <div class="info-item">
                <label>Ngày trả thực tế:</label>
                <span class="info-value">{{ formatDate(phieuBaoHanh.ngayTraThucTe) || 'Chưa trả' }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Tình trạng tiếp nhận -->
        <div class="detail-card">
          <div class="card-header">
            <h2>Tình trạng Tiếp nhận</h2>
          </div>
          <div class="card-body">
            <div class="info-item full-width">
              <label>Mô tả lỗi (theo khách hàng):</label>
              <div class="info-value-text">{{ phieuBaoHanh.moTaLoiKhachHang || '-' }}</div>
            </div>
            <div class="info-item full-width">
              <label>Mô tả lỗi (nhân viên ghi nhận):</label>
              <div class="info-value-text">{{ phieuBaoHanh.moTaLoiNhanVien || '-' }}</div>
            </div>
            <div class="info-item full-width">
              <label>Tình trạng vật lý:</label>
              <div class="info-value-text">{{ phieuBaoHanh.tinhTrangVatLy || '-' }}</div>
            </div>
            <div class="info-item full-width">
              <label>Phụ kiện đi kèm:</label>
              <div class="info-value-text">{{ phieuBaoHanh.phuKienDiKem || '-' }}</div>
            </div>
          </div>
        </div>

        <!-- Đánh giá điều kiện bảo hành -->
        <div class="detail-card" v-if="phieuBaoHanh.trangThai >= 1">
          <div class="card-header">
            <h2>Đánh giá Điều kiện Bảo hành</h2>
          </div>
          <div class="card-body">
            <div class="info-grid">
              <div class="info-item">
                <label>Đủ điều kiện:</label>
                <span class="info-value">
                  <span :class="phieuBaoHanh.duDieuKienBaoHanh ? 'badge-success' : 'badge-danger'">
                    {{ phieuBaoHanh.duDieuKienBaoHanh ? 'Có' : 'Không' }}
                  </span>
                </span>
              </div>
              <div class="info-item full-width" v-if="phieuBaoHanh.lyDoKhongDuDieuKien">
                <label>Lý do không đủ điều kiện:</label>
                <div class="info-value-text">{{ phieuBaoHanh.lyDoKhongDuDieuKien }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Xử lý phiếu -->
        <div class="detail-card" v-if="phieuBaoHanh.trangThai >= 3">
          <div class="card-header">
            <h2>Kết quả Xử lý</h2>
          </div>
          <div class="card-body">
            <div class="info-grid">
              <div class="info-item">
                <label>Hướng xử lý:</label>
                <span class="info-value">
                  {{ phieuBaoHanh.huongXuLy === 'SUA_TAI_CUA_HANG' ? 'Sửa tại cửa hàng' : 'Gửi TTBH hãng' }}
                </span>
              </div>
              <div class="info-item full-width" v-if="phieuBaoHanh.noiDungSuaChua">
                <label>Nội dung sửa chữa:</label>
                <div class="info-value-text">{{ phieuBaoHanh.noiDungSuaChua }}</div>
              </div>
              <div class="info-item full-width" v-if="phieuBaoHanh.ghiChuKyThuatVien">
                <label>Ghi chú kỹ thuật viên:</label>
                <div class="info-value-text">{{ phieuBaoHanh.ghiChuKyThuatVien }}</div>
              </div>
              <div class="info-item" v-if="phieuBaoHanh.chiPhiSuaChua">
                <label>Chi phí sửa chữa:</label>
                <span class="info-value">{{ formatCurrency(phieuBaoHanh.chiPhiSuaChua) }}</span>
              </div>
              <div class="info-item" v-if="phieuBaoHanh.khachDaThanhToan">
                <label>Khách đã thanh toán:</label>
                <span class="info-value">{{ formatCurrency(phieuBaoHanh.khachDaThanhToan) }}</span>
              </div>
              <div class="info-item" v-if="phieuBaoHanh.ttbhHang">
                <label>TTBH hãng:</label>
                <span class="info-value">{{ phieuBaoHanh.ttbhHang }}</span>
              </div>
              <div class="info-item" v-if="phieuBaoHanh.maBaoHanhHang">
                <label>Mã bảo hành hãng:</label>
                <span class="info-value">{{ phieuBaoHanh.maBaoHanhHang }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Lịch sử xử lý -->
        <div class="detail-card">
          <div class="card-header">
            <h2>Lịch sử Xử lý</h2>
          </div>
          <div class="card-body">
            <div v-if="loadingHistory" class="loading-history">
              <div class="loading-spinner"></div>
              <p>Đang tải lịch sử...</p>
            </div>
            <div v-else-if="lichSuXuLy.length === 0" class="no-history">
              <p>Chưa có lịch sử xử lý</p>
            </div>
            <div v-else class="history-timeline">
              <div v-for="(item, index) in lichSuXuLy" :key="item.id" class="history-item">
                <div class="history-time">{{ formatDateTime(item.thoiGian) }}</div>
                <div class="history-content">
                  <div class="history-action">{{ item.hanhDongText || item.hanhDong }}</div>
                  <div class="history-desc">{{ item.noiDungXuLy }}</div>
                  <div class="history-staff" v-if="item.tenNhanVienThucHien">
                    <FontAwesomeIcon :icon="['fas', 'user']" />
                    {{ item.tenNhanVienThucHien }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Action Buttons based on status -->
        <div class="action-section" v-if="phieuBaoHanh.trangThai < 9">
          <!-- Bước 1: Kiểm tra điều kiện bảo hành (trạng thái 0) -->
          <div v-if="phieuBaoHanh.trangThai === 0" class="action-group">
            <h3>Bước 2: Đánh giá sơ bộ và xác minh điều kiện bảo hành</h3>
            <div class="action-buttons">
              <button class="btn-action btn-success" @click="showKiemTraDieuKien = true">
                <FontAwesomeIcon :icon="['fas', 'check-circle']" />
                Kiểm tra Điều kiện
              </button>
            </div>
          </div>

          <!-- Bước 2: Chọn hướng xử lý (trạng thái 1 - đủ điều kiện) -->
          <div v-if="phieuBaoHanh.trangThai === 1" class="action-group">
            <h3>Bước 3: Chọn Hướng Xử lý</h3>
            <div class="action-buttons">
              <button class="btn-action btn-primary" @click="showSuaNoiBo = true">
                <FontAwesomeIcon :icon="['fas', 'tools']" />
                Sửa tại Cửa hàng
              </button>
              <button class="btn-action btn-warning" @click="showGuiTTBH = true">
                <FontAwesomeIcon :icon="['fas', 'truck']" />
                Gửi TTBH Hãng
              </button>
            </div>
          </div>

          <!-- Bước 3: Sửa chữa nội bộ (trạng thái 3) -->
          <div v-if="phieuBaoHanh.trangThai === 3 && phieuBaoHanh.huongXuLy === 'SUA_TAI_CUA_HANG'" class="action-group">
            <h3>Sửa chữa Nội bộ</h3>
            <div class="action-buttons">
              <button class="btn-action btn-primary" @click="showKiemTraQC = true">
                <FontAwesomeIcon :icon="['fas', 'clipboard-check']" />
                Kiểm tra QC
              </button>
            </div>
          </div>

          <!-- Bước 4: Nhận từ TTBH (trạng thái 4 - đã gửi) -->
          <div v-if="phieuBaoHanh.trangThai === 4" class="action-group">
            <h3>Nhận lại từ TTBH Hãng</h3>
            <div class="action-buttons">
              <button class="btn-action btn-primary" @click="showNhanTuTTBH = true">
                <FontAwesomeIcon :icon="['fas', 'inbox']" />
                Nhận từ TTBH
              </button>
            </div>
          </div>

          <!-- Bước 5: QC sau nhận từ TTBH (trạng thái 5) -->
          <div v-if="phieuBaoHanh.trangThai === 5" class="action-group">
            <h3>Kiểm tra QC sau khi nhận từ TTBH</h3>
            <div class="action-buttons">
              <button class="btn-action btn-primary" @click="showKiemTraQC = true">
                <FontAwesomeIcon :icon="['fas', 'clipboard-check']" />
                Kiểm tra QC
              </button>
            </div>
          </div>

          <!-- Bước 6: Trả máy (trạng thái 7 - đã sửa xong) -->
          <div v-if="phieuBaoHanh.trangThai === 7" class="action-group">
            <h3>Trả máy cho Khách hàng</h3>
            <div class="action-buttons">
              <button class="btn-action btn-success" @click="traMay">
                <FontAwesomeIcon :icon="['fas', 'hand-holding']" />
                Trả máy
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modals for actions -->
    <!-- Modal: Kiểm tra điều kiện -->
    <div v-if="showKiemTraDieuKien" class="modal-overlay" @click.self="showKiemTraDieuKien = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>Kiểm tra Điều kiện Bảo hành</h3>
          <button class="modal-close" @click="showKiemTraDieuKien = false">
            <FontAwesomeIcon :icon="['fas', 'times']" />
          </button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>Đánh giá:</label>
            <select v-model="kiemTraForm.duDieuKien" class="form-input">
              <option :value="true">Đủ điều kiện bảo hành</option>
              <option :value="false">Không đủ điều kiện bảo hành</option>
            </select>
          </div>
          <div class="form-group" v-if="!kiemTraForm.duDieuKien">
            <label>Lý do không đủ điều kiện *:</label>
            <textarea 
              v-model="kiemTraForm.lyDo" 
              class="form-textarea" 
              rows="4"
              placeholder="Nhập lý do không đủ điều kiện bảo hành"
              required
            ></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="showKiemTraDieuKien = false">Hủy</button>
          <button class="btn-submit" @click="kiemTraDieuKien">Xác nhận</button>
        </div>
      </div>
    </div>

    <!-- Modal: Sửa nội bộ -->
    <div v-if="showSuaNoiBo" class="modal-overlay" @click.self="showSuaNoiBo = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>Sửa chữa Nội bộ</h3>
          <button class="modal-close" @click="showSuaNoiBo = false">
            <FontAwesomeIcon :icon="['fas', 'times']" />
          </button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>Nội dung sửa chữa *:</label>
            <textarea 
              v-model="suaNoiBoForm.noiDungSuaChua" 
              class="form-textarea" 
              rows="4"
              placeholder="Mô tả chi tiết nội dung sửa chữa"
              required
            ></textarea>
          </div>
          <div class="form-group">
            <label>Ghi chú:</label>
            <textarea 
              v-model="suaNoiBoForm.ghiChu" 
              class="form-textarea" 
              rows="3"
              placeholder="Ghi chú thêm (nếu có)"
            ></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="showSuaNoiBo = false">Hủy</button>
          <button class="btn-submit" @click="suaNoiBo">Xác nhận</button>
        </div>
      </div>
    </div>

    <!-- Modal: Gửi TTBH -->
    <div v-if="showGuiTTBH" class="modal-overlay" @click.self="showGuiTTBH = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>Gửi đến TTBH Hãng</h3>
          <button class="modal-close" @click="showGuiTTBH = false">
            <FontAwesomeIcon :icon="['fas', 'times']" />
          </button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>Tên TTBH hãng *:</label>
            <input 
              v-model="guiTTBHForm.ttbhHang" 
              class="form-input"
              placeholder="Nhập tên trung tâm bảo hành"
              required
            />
          </div>
          <div class="form-group">
            <label>Mã bảo hành hãng:</label>
            <input 
              v-model="guiTTBHForm.maBaoHanhHang" 
              class="form-input"
              placeholder="Nhập mã bảo hành của hãng (nếu có)"
            />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="showGuiTTBH = false">Hủy</button>
          <button class="btn-submit" @click="guiTTBH">Xác nhận</button>
        </div>
      </div>
    </div>

    <!-- Modal: Nhận từ TTBH -->
    <div v-if="showNhanTuTTBH" class="modal-overlay" @click.self="showNhanTuTTBH = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>Nhận lại từ TTBH Hãng</h3>
          <button class="modal-close" @click="showNhanTuTTBH = false">
            <FontAwesomeIcon :icon="['fas', 'times']" />
          </button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>Nội dung sửa chữa *:</label>
            <textarea 
              v-model="nhanTuTTBHForm.noiDungSuaChua" 
              class="form-textarea" 
              rows="4"
              placeholder="Mô tả nội dung sửa chữa từ TTBH"
              required
            ></textarea>
          </div>
          <div class="form-group">
            <label>Ghi chú:</label>
            <textarea 
              v-model="nhanTuTTBHForm.ghiChu" 
              class="form-textarea" 
              rows="3"
              placeholder="Ghi chú thêm"
            ></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="showNhanTuTTBH = false">Hủy</button>
          <button class="btn-submit" @click="nhanTuTTBH">Xác nhận</button>
        </div>
      </div>
    </div>

    <!-- Modal: Kiểm tra QC -->
    <div v-if="showKiemTraQC" class="modal-overlay" @click.self="showKiemTraQC = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>Kiểm tra QC</h3>
          <button class="modal-close" @click="showKiemTraQC = false">
            <FontAwesomeIcon :icon="['fas', 'times']" />
          </button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>Kết quả QC *:</label>
            <select v-model="qcForm.qcPass" class="form-input" required>
              <option :value="true">Pass - Đã sửa xong</option>
              <option :value="false">Fail - Cần sửa lại</option>
            </select>
          </div>
          <div class="form-group">
            <label>Ghi chú *:</label>
            <textarea 
              v-model="qcForm.ghiChu" 
              class="form-textarea" 
              rows="4"
              placeholder="Mô tả kết quả kiểm tra QC"
              required
            ></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="showKiemTraQC = false">Hủy</button>
          <button class="btn-submit" @click="kiemTraQC">Xác nhận</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { FontAwesomeIcon } from '@/plugins/fontawesome'
import PosHeader from '@/components/PosHeader.vue'
import Toast from '@/components/Toast.vue'
import api from '@/services/api'

const router = useRouter()
const route = useRoute()
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

const phieuBaoHanh = ref<any>(null)
const lichSuXuLy = ref<any[]>([])
const loading = ref(false)
const loadingHistory = ref(false)
const error = ref('')

// Modal states
const showKiemTraDieuKien = ref(false)
const showSuaNoiBo = ref(false)
const showGuiTTBH = ref(false)
const showNhanTuTTBH = ref(false)
const showKiemTraQC = ref(false)

// Form data
const kiemTraForm = ref({
  duDieuKien: true,
  lyDo: ''
})

const suaNoiBoForm = ref({
  noiDungSuaChua: '',
  ghiChu: ''
})

const guiTTBHForm = ref({
  ttbhHang: '',
  maBaoHanhHang: ''
})

const nhanTuTTBHForm = ref({
  noiDungSuaChua: '',
  ghiChu: ''
})

const qcForm = ref({
  qcPass: true,
  ghiChu: ''
})

function goBack() {
  router.push('/bao-hanh')
}

function formatDate(dateString?: string): string {
  if (!dateString) return '-'
  try {
    return new Date(dateString).toLocaleDateString('vi-VN')
  } catch {
    return dateString
  }
}

function formatDateTime(dateString?: string): string {
  if (!dateString) return '-'
  try {
    const date = new Date(dateString)
    return date.toLocaleString('vi-VN')
  } catch {
    return dateString
  }
}

function formatCurrency(amount?: number): string {
  if (amount === null || amount === undefined) return '0 VNĐ'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount)
}

function getStatusText(trangThai: number): string {
  const statusMap: Record<number, string> = {
    0: 'Mới tiếp nhận',
    1: 'Đủ điều kiện',
    2: 'Không đủ điều kiện',
    3: 'Đang sửa chữa nội bộ',
    4: 'Đã gửi TTBH hãng',
    5: 'Đã nhận từ TTBH',
    6: 'Đang kiểm tra QC',
    7: 'Đã sửa xong',
    8: 'Đã trả khách',
    9: 'Hoàn tất'
  }
  return statusMap[trangThai] || 'Không xác định'
}

function getStatusClass(trangThai: number): string {
  const classMap: Record<number, string> = {
    0: 'status-new',
    1: 'status-eligible',
    2: 'status-not-eligible',
    3: 'status-repairing',
    4: 'status-sent',
    5: 'status-received',
    6: 'status-qc',
    7: 'status-done',
    8: 'status-returned',
    9: 'status-completed'
  }
  return classMap[trangThai] || 'status-unknown'
}

async function loadPhieuBaoHanh() {
  loading.value = true
  error.value = ''
  try {
    const id = route.params.id
    const response = await api.get(`/api/bao-hanh/${id}`)
    phieuBaoHanh.value = response.data
    await loadLichSuXuLy()
  } catch (err: any) {
    error.value = err.response?.data?.message || err.message || 'Không thể tải thông tin phiếu bảo hành'
    toastRef.value?.error('Lỗi', error.value)
  } finally {
    loading.value = false
  }
}

async function loadLichSuXuLy() {
  if (!phieuBaoHanh.value?.id) return
  
  loadingHistory.value = true
  try {
    const response = await api.get(`/api/bao-hanh/${phieuBaoHanh.value.id}/lich-su`)
    lichSuXuLy.value = response.data || []
  } catch (err: any) {
    console.error('Error loading lich su:', err)
  } finally {
    loadingHistory.value = false
  }
}

async function kiemTraDieuKien() {
  if (!kiemTraForm.value.duDieuKien && !kiemTraForm.value.lyDo?.trim()) {
    toastRef.value?.error('Lỗi', 'Vui lòng nhập lý do khi không đủ điều kiện')
    return
  }

  try {
    await api.post(`/api/bao-hanh/${phieuBaoHanh.value.id}/kiem-tra-dieu-kien`, {
      duDieuKien: kiemTraForm.value.duDieuKien,
      lyDo: kiemTraForm.value.lyDo
    })
    
    showKiemTraDieuKien.value = false
    toastRef.value?.success('Thành công', 'Đã cập nhật đánh giá điều kiện bảo hành')
    await loadPhieuBaoHanh()
  } catch (err: any) {
    toastRef.value?.error('Lỗi', 'Không thể cập nhật: ' + (err.response?.data?.message || err.message))
  }
}

async function suaNoiBo() {
  if (!suaNoiBoForm.value.noiDungSuaChua?.trim()) {
    toastRef.value?.error('Lỗi', 'Vui lòng nhập nội dung sửa chữa')
    return
  }

  try {
    await api.post(`/api/bao-hanh/${phieuBaoHanh.value.id}/sua-noi-bo`, {
      noiDungSuaChua: suaNoiBoForm.value.noiDungSuaChua,
      ghiChu: suaNoiBoForm.value.ghiChu
    })
    
    showSuaNoiBo.value = false
    suaNoiBoForm.value = { noiDungSuaChua: '', ghiChu: '' }
    toastRef.value?.success('Thành công', 'Đã cập nhật thông tin sửa chữa nội bộ')
    await loadPhieuBaoHanh()
  } catch (err: any) {
    toastRef.value?.error('Lỗi', 'Không thể cập nhật: ' + (err.response?.data?.message || err.message))
  }
}

async function guiTTBH() {
  if (!guiTTBHForm.value.ttbhHang?.trim()) {
    toastRef.value?.error('Lỗi', 'Vui lòng nhập tên TTBH hãng')
    return
  }

  try {
    await api.post(`/api/bao-hanh/${phieuBaoHanh.value.id}/gui-ttbh`, {
      ttbhHang: guiTTBHForm.value.ttbhHang,
      maBaoHanhHang: guiTTBHForm.value.maBaoHanhHang
    })
    
    showGuiTTBH.value = false
    guiTTBHForm.value = { ttbhHang: '', maBaoHanhHang: '' }
    toastRef.value?.success('Thành công', 'Đã gửi thông tin đến TTBH hãng')
    await loadPhieuBaoHanh()
  } catch (err: any) {
    toastRef.value?.error('Lỗi', 'Không thể gửi: ' + (err.response?.data?.message || err.message))
  }
}

async function nhanTuTTBH() {
  if (!nhanTuTTBHForm.value.noiDungSuaChua?.trim()) {
    toastRef.value?.error('Lỗi', 'Vui lòng nhập nội dung sửa chữa')
    return
  }

  try {
    await api.post(`/api/bao-hanh/${phieuBaoHanh.value.id}/nhan-tu-ttbh`, {
      noiDungSuaChua: nhanTuTTBHForm.value.noiDungSuaChua,
      ghiChu: nhanTuTTBHForm.value.ghiChu
    })
    
    showNhanTuTTBH.value = false
    nhanTuTTBHForm.value = { noiDungSuaChua: '', ghiChu: '' }
    toastRef.value?.success('Thành công', 'Đã cập nhật thông tin nhận từ TTBH')
    await loadPhieuBaoHanh()
  } catch (err: any) {
    toastRef.value?.error('Lỗi', 'Không thể cập nhật: ' + (err.response?.data?.message || err.message))
  }
}

async function kiemTraQC() {
  if (!qcForm.value.ghiChu?.trim()) {
    toastRef.value?.error('Lỗi', 'Vui lòng nhập ghi chú QC')
    return
  }

  try {
    await api.post(`/api/bao-hanh/${phieuBaoHanh.value.id}/kiem-tra-qc`, {
      qcPass: qcForm.value.qcPass,
      ghiChu: qcForm.value.ghiChu
    })
    
    showKiemTraQC.value = false
    qcForm.value = { qcPass: true, ghiChu: '' }
    toastRef.value?.success('Thành công', 'Đã cập nhật kết quả QC')
    await loadPhieuBaoHanh()
  } catch (err: any) {
    toastRef.value?.error('Lỗi', 'Không thể cập nhật: ' + (err.response?.data?.message || err.message))
  }
}

async function traMay() {
  if (confirm('Bạn có chắc chắn muốn trả máy cho khách hàng?')) {
    try {
      await api.post(`/api/bao-hanh/${phieuBaoHanh.value.id}/tra-may`, {
        ghiChu: 'Đã trả máy cho khách hàng'
      })
      
      toastRef.value?.success('Thành công', 'Đã cập nhật trạng thái trả máy')
      await loadPhieuBaoHanh()
    } catch (err: any) {
      toastRef.value?.error('Lỗi', 'Không thể cập nhật: ' + (err.response?.data?.message || err.message))
    }
  }
}

onMounted(() => {
  loadPhieuBaoHanh()
})
</script>

<style scoped>
.bao-hanh-detail-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20px;
}

.detail-container {
  max-width: 1200px;
  margin: 0 auto;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.btn-back {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background-color: #6c757d;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.btn-back:hover {
  background-color: #5a6268;
}

.detail-header h1 {
  margin: 0;
  color: #333;
  font-size: 24px;
}

.status-badge-large {
  padding: 8px 20px;
  border-radius: 20px;
  font-weight: 600;
  font-size: 14px;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  overflow: hidden;
}

.card-header {
  padding: 15px 20px;
  background-color: #f8f9fa;
  border-bottom: 2px solid #e0e0e0;
}

.card-header h2 {
  margin: 0;
  color: #333;
  font-size: 18px;
}

.card-body {
  padding: 20px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 15px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-item label {
  font-weight: 500;
  color: #666;
  font-size: 14px;
}

.info-value {
  color: #333;
  font-size: 15px;
}

.info-value-text {
  color: #333;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.badge-success {
  padding: 4px 12px;
  background-color: #d4edda;
  color: #155724;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.badge-danger {
  padding: 4px 12px;
  background-color: #f8d7da;
  color: #721c24;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.history-timeline {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.history-item {
  padding: 15px;
  background-color: #f8f9fa;
  border-left: 4px solid #2196F3;
  border-radius: 4px;
}

.history-time {
  font-size: 12px;
  color: #666;
  margin-bottom: 5px;
}

.history-action {
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
}

.history-desc {
  color: #666;
  font-size: 14px;
  margin-bottom: 5px;
}

.history-staff {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #999;
}

.action-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.action-group {
  margin-bottom: 20px;
}

.action-group h3 {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 16px;
}

.action-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.btn-action {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
}

.btn-success {
  background-color: #4CAF50;
  color: white;
}

.btn-success:hover {
  background-color: #45a049;
}

.btn-primary {
  background-color: #2196F3;
  color: white;
}

.btn-primary:hover {
  background-color: #1976D2;
}

.btn-warning {
  background-color: #FF9800;
  color: white;
}

.btn-warning:hover {
  background-color: #F57C00;
}

/* Modal styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  max-width: 600px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.modal-header h3 {
  margin: 0;
  color: #333;
}

.modal-close {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #666;
}

.modal-body {
  padding: 20px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 20px;
  border-top: 1px solid #e0e0e0;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
  color: #555;
}

.form-input,
.form-textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.form-textarea {
  resize: vertical;
  font-family: inherit;
}

.btn-cancel,
.btn-submit {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
}

.btn-cancel {
  background-color: #6c757d;
  color: white;
}

.btn-submit {
  background-color: #4CAF50;
  color: white;
}

.btn-cancel:hover {
  background-color: #5a6268;
}

.btn-submit:hover {
  background-color: #45a049;
}

.loading-container,
.error-container {
  text-align: center;
  padding: 40px;
  background: white;
  border-radius: 8px;
}

.loading-spinner {
  border: 4px solid #f3f3f3;
  border-top: 4px solid #2196F3;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Status badge classes */
.status-new { background-color: #E3F2FD; color: #1976D2; }
.status-eligible { background-color: #E8F5E9; color: #388E3C; }
.status-not-eligible { background-color: #FFEBEE; color: #C62828; }
.status-repairing { background-color: #FFF3E0; color: #F57C00; }
.status-sent { background-color: #E1BEE7; color: #7B1FA2; }
.status-received { background-color: #BBDEFB; color: #1565C0; }
.status-qc { background-color: #F3E5F5; color: #6A1B9A; }
.status-done { background-color: #C8E6C9; color: #2E7D32; }
.status-returned { background-color: #B2DFDB; color: #00695C; }
.status-completed { background-color: #A5D6A7; color: #1B5E20; }
</style>

