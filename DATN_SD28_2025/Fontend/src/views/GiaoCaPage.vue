<template>
  <div class="page dark-mode-transition">
    <PosHeader />
    
    <div class="content">
      <Toast ref="toastRef" />

      <!-- Giao Ca Card -->
      <div class="card giao-ca-card">
        <div class="card-header">
          <h2>Giao Ca</h2>
          <button @click="goToCreateGiaoCa" class="btn-giao-ca">
            <font-awesome-icon :icon="['fas', 'exchange-alt']" />
            Giao Ca
          </button>
        </div>
        <p class="card-description">Thực hiện giao ca giữa các nhân viên</p>
      </div>

      <!-- Current Shift Info -->
      <div v-if="currentPhanCa || showEmptyShiftCard" class="card current-shift-card">
        <div class="card-header">
          <h2>
            <font-awesome-icon :icon="['fas', 'clock']" />
            Ca Hiện Tại
          </h2>
          <div class="card-actions">
            <!-- Nút Kết thúc ca - hiển thị khi đang làm (status = 1) -->
            <button 
              v-if="currentPhanCa && currentPhanCa.trangThai === 1" 
              @click="endShift" 
              class="btn-end-shift"
              :disabled="loading"
              title="Kết thúc ca làm việc"
            >
              <font-awesome-icon :icon="['fas', 'stop']" />
              Kết thúc ca
            </button>
            <!-- Nút Giao ca - hiển thị khi ca ĐÃ KẾT THÚC (status = 2) -->
            <button 
              v-if="currentPhanCa && currentPhanCa.trangThai === 2" 
              @click="goToCreateGiaoCa" 
              class="btn-giao-ca-shift"
              :disabled="loading"
              title="Giao ca với nhân viên khác"
            >
              <font-awesome-icon :icon="['fas', 'exchange-alt']" />
              Giao ca với nhân viên khác
            </button>
          </div>
        </div>
        <div class="card-content">
          <div v-if="currentPhanCa" class="shift-info">
            <div class="shift-details">
              <div class="shift-item">
                <label>Nhân viên:</label>
                <span class="value">{{ currentPhanCa.nhanVien?.hoTen || 'N/A' }}</span>
              </div>
              <div class="shift-item">
                <label>Ca làm việc:</label>
                <span class="value">{{ currentPhanCa.ca?.tenCa || 'N/A' }}</span>
              </div>
              <div class="shift-item">
                <label>Thời gian:</label>
                <span class="value">
                  <span v-if="currentPhanCa.ca">
                    {{ formatTime(currentPhanCa.ca.gioBatDau) }} - {{ formatTime(currentPhanCa.ca.gioKetThuc) }}
                  </span>
                  <span v-else>N/A</span>
                </span>
              </div>
              <div class="shift-item">
                <label>Ngày:</label>
                <span class="value">{{ formatDate(currentPhanCa.ngayLamViec) }}</span>
              </div>
              <div v-if="currentPhanCa.gioBatDauThucTe" class="shift-item">
                <label>Bắt đầu lúc:</label>
                <span class="value">{{ formatDateTime(currentPhanCa.gioBatDauThucTe) }}</span>
              </div>
              <div v-if="currentPhanCa.gioKetThucThucTe" class="shift-item">
                <label>Kết thúc lúc:</label>
                <span class="value">{{ formatDateTime(currentPhanCa.gioKetThucThucTe) }}</span>
              </div>
            </div>
            <div class="shift-status">
              <span class="status-badge" :class="getStatusClass(currentPhanCa.trangThai)">
                {{ getStatusText(currentPhanCa.trangThai) }}
              </span>
            </div>
          </div>
          <div v-else class="empty-shift-info">
            <font-awesome-icon :icon="['fas', 'calendar-times']" />
            <p>Chưa có ca làm việc hôm nay</p>
            <p class="hint">Vui lòng liên hệ quản lý để được phân ca</p>
            <button class="btn-reload" @click="loadCurrentPhanCa" :disabled="loading">
              <font-awesome-icon :icon="['fas', 'sync-alt']" :class="{ 'spinning': loading }" />
              Tải lại
            </button>
          </div>
        </div>
      </div>

      <!-- Next Shift -->
      <div v-if="nextPhanCa" class="card next-shift-card">
        <div class="card-header">
          <h2>
            <font-awesome-icon :icon="['fas', 'clock']" />
            Ca Tiếp Theo
          </h2>
          <div class="card-actions">
            <!-- Đã bỏ nút "Bắt đầu ca" - ca sẽ tự động bắt đầu khi nhân viên xác nhận giao ca -->
          </div>
        </div>
        <div class="card-content">
          <div class="shift-info">
            <div class="shift-item">
              <label>Nhân viên:</label>
              <span class="value">{{ nextPhanCa.nhanVien?.hoTen || 'N/A' }}</span>
            </div>
            <div class="shift-item">
              <label>Ca làm việc:</label>
              <span class="value">{{ nextPhanCa.ca?.tenCa || 'N/A' }}</span>
            </div>
            <div class="shift-item">
              <label>Thời gian:</label>
              <span class="value">
                <span v-if="nextPhanCa.ca">
                  {{ formatTime(nextPhanCa.ca.gioBatDau) }} - {{ formatTime(nextPhanCa.ca.gioKetThuc) }}
                </span>
                <span v-else>N/A</span>
              </span>
            </div>
            <div class="shift-item">
              <label>Ngày:</label>
              <span class="value">{{ formatDate(nextPhanCa.ngayLamViec) }}</span>
            </div>
          </div>
          <div class="shift-status">
            <span class="status-badge" :class="getStatusClass(nextPhanCa.trangThai)">
              {{ getStatusText(nextPhanCa.trangThai) }}
            </span>
          </div>
        </div>
      </div>

      <!-- Recent Giao Ca -->
      <div v-if="recentGiaoCa && recentGiaoCa.length > 0" class="card recent-giao-ca-card">
        <div class="card-header">
          <h2>
            <font-awesome-icon :icon="['fas', 'history']" />
            Lịch Sử Giao Ca Gần Đây
          </h2>
        </div>
        <div class="card-content">
          <div class="giao-ca-list">
            <div 
              v-for="giaoCa in recentGiaoCa" 
              :key="giaoCa.id" 
              class="giao-ca-item"
              @click="viewGiaoCa(giaoCa.id)"
            >
              <div class="giao-ca-info">
                <div class="giao-ca-header">
                  <span class="ma-giao-ca">{{ giaoCa.maGiaoCa }}</span>
                  <span class="status-badge" :class="getGiaoCaStatusClass(giaoCa.trangThai)">
                    {{ getGiaoCaStatusText(giaoCa.trangThai) }}
                  </span>
                </div>
                <div class="giao-ca-details">
                  <span>Ngày: {{ formatDateTime(giaoCa.ngayGiaoCa) }}</span>
                  <span>Tiền đầu ca: {{ formatCurrency(giaoCa.soTienDauCa) }}</span>
                  <span>Tiền cuối ca: {{ formatCurrency(giaoCa.soTienCuoiCa) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/services/api'
import Toast from '@/components/Toast.vue'
import PosHeader from '@/components/PosHeader.vue'
import { useAuthStore } from '@/stores/authStore'

const router = useRouter()
const authStore = useAuthStore()

interface PhanCa {
  id: number
  nhanVienId: number
  caId: number
  ngayLamViec: string
  trangThai: number | null
  gioBatDauThucTe?: string | null
  gioKetThucThucTe?: string | null
  ghiChu?: string | null
  nhanVien?: {
    id: number
    hoTen: string
  }
  ca?: {
    id: number
    tenCa: string
    gioBatDau: string
    gioKetThuc: string
  }
}

interface GiaoCa {
  id: number
  maGiaoCa: string
  ngayGiaoCa: string
  soTienDauCa: number
  soTienCuoiCa: number
  trangThai: number
}

const currentPhanCa = ref<PhanCa | null>(null)
const nextPhanCa = ref<PhanCa | null>(null)
const recentGiaoCa = ref<GiaoCa[]>([])
const showEmptyShiftCard = ref(false)
const loading = ref(false)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

// Functions
function formatDate(date: string) {
  if (!date) return 'N/A'
  const d = new Date(date)
  return d.toLocaleDateString('vi-VN')
}

function formatTime(time: string) {
  if (!time) return 'N/A'
  return time.substring(0, 5)
}

function formatDateTime(dateTime: string) {
  if (!dateTime) return 'N/A'
  const d = new Date(dateTime)
  return d.toLocaleString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function formatCurrency(amount: number | null | undefined) {
  if (amount === null || amount === undefined) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}

function getStatusText(trangThai: number | null): string {
  if (trangThai === null) return 'Chưa bắt đầu'
  switch (trangThai) {
    case 0: return 'Chưa bắt đầu'
    case 1: return 'Đang làm'
    case 2: return 'Đã kết thúc'
    case 3: return 'Vắng mặt'
    default: return 'Không xác định'
  }
}

function getStatusClass(trangThai: number | null): string {
  if (trangThai === null) return 'status-pending'
  switch (trangThai) {
    case 0: return 'status-pending'
    case 1: return 'status-active'
    case 2: return 'status-finished'
    case 3: return 'status-absent'
    default: return 'status-unknown'
  }
}

function getGiaoCaStatusText(trangThai: number): string {
  switch (trangThai) {
    case 0: return 'Chờ xác nhận'
    case 1: return 'Đã xác nhận'
    case 2: return 'Đã hủy'
    default: return 'Không xác định'
  }
}

function getGiaoCaStatusClass(trangThai: number): string {
  switch (trangThai) {
    case 0: return 'status-pending'
    case 1: return 'status-active'
    case 2: return 'status-finished'
    default: return 'status-unknown'
  }
}

function isShiftTimePassed(phanCa: PhanCa): boolean {
  if (!phanCa.ca || !phanCa.ngayLamViec) return false
  
  const now = new Date()
  const today = now.toISOString().split('T')[0]
  
  if (phanCa.ngayLamViec < today) return true
  if (phanCa.ngayLamViec > today) return false
  
  const endTime = phanCa.ca.gioKetThuc
  if (!endTime) return false
  
  const [hours, minutes] = endTime.split(':').map(Number)
  const shiftEnd = new Date()
  shiftEnd.setHours(hours, minutes, 0, 0)
  
  return now > shiftEnd
}

async function loadCurrentPhanCa() {
  try {
    const today = new Date().toISOString().split('T')[0]
    
    console.log('[GiaoCaPage] ==========================================')
    console.log('[GiaoCaPage] Đang tải phân ca hiện tại cho nhân viên:', authStore.user?.id)
    console.log('[GiaoCaPage] Ngày hôm nay:', today)
    
    if (!authStore.user?.id) {
      console.log('[GiaoCaPage] ❌ Không có user ID')
      currentPhanCa.value = null
      showEmptyShiftCard.value = true
      return
    }
    
    // LOGIC MỚI: "Ca Hiện Tại" = ca đang làm (status = 1) hoặc đã kết thúc (status = 2)
    // KHÔNG bao giờ hiển thị ca chưa bắt đầu (status = 0) trong "Ca Hiện Tại"
    // Ca chưa bắt đầu sẽ được hiển thị ở "Ca Tiếp Theo"
    
    try {
      // Bước 1: Lấy tất cả phân ca hôm nay của nhân viên này
      const response = await api.get(`/api/phan-ca/nhan-vien/${authStore.user.id}/date-range?startDate=${today}&endDate=${today}`)
      const phanCaList: PhanCa[] = response.data || []
      
      console.log('[GiaoCaPage] Tìm thấy', phanCaList.length, 'phân ca từ date-range')
      
      if (phanCaList.length === 0) {
        console.log('[GiaoCaPage] ❌ Không có phân ca nào hôm nay')
        currentPhanCa.value = null
        showEmptyShiftCard.value = true
        return
      }
      
      // Debug: In tất cả phân ca
      phanCaList.forEach((pc: PhanCa, index: number) => {
        console.log(`[GiaoCaPage] Phân ca ${index + 1}:`, {
          id: pc.id,
          ca: pc.ca?.tenCa || 'N/A',
          status: pc.trangThai,
          gioBatDau: pc.ca?.gioBatDau || 'N/A',
          ngayLamViec: pc.ngayLamViec
        })
      })
      
      // Bước 1: Tìm phân ca ĐANG LÀM (status = 1) - ưu tiên cao nhất
      const activeShifts = phanCaList.filter((pc: PhanCa) => pc.trangThai === 1)
      
      console.log('[GiaoCaPage] Tìm thấy', activeShifts.length, 'phân ca đang làm')
      
      if (activeShifts.length > 0) {
        // Chọn ca đang làm đầu tiên
        const activeShift = activeShifts.sort((a, b) => {
          const timeA = a.ca?.gioBatDau || ''
          const timeB = b.ca?.gioBatDau || ''
          return timeA.localeCompare(timeB)
        })[0]
        
        currentPhanCa.value = activeShift
        showEmptyShiftCard.value = false
        console.log('[GiaoCaPage] ✅ Chọn phân ca đang làm (CA HIỆN TẠI):', {
          id: activeShift.id,
          ca: activeShift.ca?.tenCa,
          status: activeShift.trangThai
        })
        console.log('[GiaoCaPage] ✅ Nút "Kết thúc ca" sẽ hiển thị')
        return
      }
      
      // Bước 2: Nếu không có ca đang làm, tìm phân ca ĐÃ KẾT THÚC gần nhất (status = 2) - để giao ca
      const finishedShifts = phanCaList.filter((pc: PhanCa) => pc.trangThai === 2)
      console.log('[GiaoCaPage] Tìm thấy', finishedShifts.length, 'phân ca đã kết thúc')
      
      if (finishedShifts.length > 0) {
        // Chọn phân ca đã kết thúc gần nhất (sort theo gioKetThucThucTe giảm dần)
        const latestFinishedShift = finishedShifts.sort((a, b) => {
          const timeA = a.gioKetThucThucTe || ''
          const timeB = b.gioKetThucThucTe || ''
          return timeB.localeCompare(timeA) // Giảm dần (mới nhất trước)
        })[0]
        
        currentPhanCa.value = latestFinishedShift
        showEmptyShiftCard.value = false
        console.log('[GiaoCaPage] ✅ Hiển thị phân ca đã kết thúc gần nhất (CA HIỆN TẠI):', {
          id: latestFinishedShift.id,
          ca: latestFinishedShift.ca?.tenCa,
          status: latestFinishedShift.trangThai
        })
        console.log('[GiaoCaPage] ✅ Nút "Giao ca" sẽ hiển thị')
        return
      }
      
      // Bước 3: Nếu không có ca đang làm hoặc đã kết thúc, thì không có "Ca Hiện Tại"
      console.log('[GiaoCaPage] ⚠️ Không có ca đang làm hoặc đã kết thúc - không hiển thị "Ca Hiện Tại"')
      console.log('[GiaoCaPage] ℹ️ Ca chưa bắt đầu sẽ được hiển thị ở "Ca Tiếp Theo"')
      
      console.log('[GiaoCaPage] ❌ Không tìm thấy phân ca phù hợp')
    } catch (err: any) {
      console.error('[GiaoCaPage] ❌ Lỗi khi tải phân ca từ date-range:', err)
      
      // Thử API today-shift như fallback
      try {
        const { data: todayShift } = await api.get(`/api/phan-ca/today-shift?nhanVienId=${authStore.user.id}`)
        if (todayShift && !isShiftTimePassed(todayShift)) {
          currentPhanCa.value = todayShift
          showEmptyShiftCard.value = false
          console.log('[GiaoCaPage] ✅ Fallback: Tìm thấy từ today-shift:', todayShift.id)
          return
        }
      } catch (todayErr: any) {
        console.log('[GiaoCaPage] Fallback today-shift cũng không tìm thấy')
      }
    }
    
    currentPhanCa.value = null
    showEmptyShiftCard.value = true
    console.log('[GiaoCaPage] ❌ Không có phân ca nào, hiển thị empty state')
    console.log('[GiaoCaPage] ==========================================')
  } catch (error: any) {
    console.error('[GiaoCaPage] ❌ Lỗi khi tải ca hiện tại:', error)
    if (error.response?.status === 401) {
      router.push('/login')
    }
    currentPhanCa.value = null
    showEmptyShiftCard.value = false
  }
}

async function loadNextPhanCa() {
  try {
    // LOGIC MỚI: "Ca Tiếp Theo" = ca chưa bắt đầu tiếp theo (status = 0)
    // Nếu có ca hiện tại, tìm ca chưa bắt đầu tiếp theo sau ca đó
    // Nếu không có ca hiện tại, tìm ca chưa bắt đầu sớm nhất từ hôm nay
    
    if (currentPhanCa.value && currentPhanCa.value.id) {
      console.log('[GiaoCaPage] Tìm ca tiếp theo (chưa bắt đầu) sau ca hiện tại ID:', currentPhanCa.value.id)
      const response = await api.get(`/api/phan-ca/next-shift?phanCaId=${currentPhanCa.value.id}`)
      
      if (response.data) {
        // Kiểm tra lại: đảm bảo là ca chưa bắt đầu (trangThai = 0)
        if (response.data.trangThai !== null && response.data.trangThai !== 0) {
          console.warn('[GiaoCaPage] ⚠️ Ca tiếp theo không phải chưa bắt đầu (trangThai = ' + response.data.trangThai + '), bỏ qua')
          nextPhanCa.value = null
        } else if (response.data.id === currentPhanCa.value.id) {
          console.warn('[GiaoCaPage] ⚠️ Ca tiếp theo trùng với ca hiện tại, bỏ qua')
          nextPhanCa.value = null
        } else {
          nextPhanCa.value = response.data
          console.log('[GiaoCaPage] ✅ Tìm thấy ca tiếp theo (CHƯA BẮT ĐẦU):', {
            id: response.data.id,
            ca: response.data.ca?.tenCa,
            ngay: response.data.ngayLamViec,
            trangThai: response.data.trangThai
          })
        }
      } else {
        nextPhanCa.value = null
        console.log('[GiaoCaPage] Không tìm thấy ca tiếp theo')
      }
    } else {
      // Nếu không có ca hiện tại, tìm ca chưa bắt đầu sớm nhất từ hôm nay
      console.log('[GiaoCaPage] Không có ca hiện tại, tìm ca chưa bắt đầu sớm nhất từ hôm nay')
      const today = new Date().toISOString().split('T')[0]
      
      try {
        // Lấy tất cả phân ca từ hôm nay của nhân viên này
        const response = await api.get(`/api/phan-ca/nhan-vien/${authStore.user.id}/date-range?startDate=${today}&endDate=${today}`)
        const phanCaList: PhanCa[] = response.data || []
        
        // Tìm ca chưa bắt đầu sớm nhất (status = 0 hoặc null)
        const unstartedShifts = phanCaList.filter((pc: PhanCa) => 
          (pc.trangThai === 0 || pc.trangThai === null) &&
          !isShiftTimePassed(pc)
        )
        
        if (unstartedShifts.length > 0) {
          const earliestShift = unstartedShifts.sort((a, b) => {
            const timeA = a.ca?.gioBatDau || ''
            const timeB = b.ca?.gioBatDau || ''
            return timeA.localeCompare(timeB)
          })[0]
          
          nextPhanCa.value = earliestShift
          console.log('[GiaoCaPage] ✅ Tìm thấy ca chưa bắt đầu sớm nhất (CA TIẾP THEO):', {
            id: earliestShift.id,
            ca: earliestShift.ca?.tenCa,
            ngay: earliestShift.ngayLamViec,
            trangThai: earliestShift.trangThai
          })
        } else {
          nextPhanCa.value = null
          console.log('[GiaoCaPage] Không tìm thấy ca chưa bắt đầu từ hôm nay')
        }
      } catch (err: any) {
        console.error('[GiaoCaPage] Lỗi khi tải ca chưa bắt đầu:', err)
        nextPhanCa.value = null
      }
    }
  } catch (error: any) {
    if (error.response?.status !== 404) {
      console.error('[GiaoCaPage] Lỗi khi tải ca tiếp theo:', error)
    }
    nextPhanCa.value = null
  }
}

async function loadRecentGiaoCa() {
  try {
    const { data } = await api.get('/api/giao-ca/recent?limit=5')
    recentGiaoCa.value = data || []
  } catch (error) {
    console.error('Lỗi khi tải lịch sử giao ca:', error)
  }
}

async function startNextShift() {
  if (!nextPhanCa.value) {
    toastRef.value?.warning('Cảnh báo', 'Không tìm thấy phân ca để bắt đầu')
    return
  }
  
  console.log('[GiaoCaPage] Bắt đầu ca tiếp theo:', nextPhanCa.value.id)
  
  loading.value = true
  try {
    const { data } = await api.put(`/api/phan-ca/${nextPhanCa.value.id}/start`)
    console.log('[GiaoCaPage] API start trả về:', data)
    toastRef.value?.success('Thành công', 'Đã bắt đầu ca làm việc!')
    // Reload cả ca hiện tại và ca tiếp theo
    await Promise.all([
      loadCurrentPhanCa(),
      loadNextPhanCa()
    ])
  } catch (error: any) {
    console.error('[GiaoCaPage] Lỗi khi bắt đầu ca:', error)
    const errorMessage = error.response?.data?.message || error.message || 'Không thể bắt đầu ca'
    toastRef.value?.error('Lỗi', errorMessage)
  } finally {
    loading.value = false
  }
}

async function endShift() {
  if (!currentPhanCa.value) {
    toastRef.value?.warning('Cảnh báo', 'Không tìm thấy phân ca để kết thúc')
    return
  }
  
  if (!confirm(`Bạn có chắc chắn muốn kết thúc ca làm việc: ${currentPhanCa.value.ca?.tenCa}?`)) {
    return
  }
  
  console.log('[GiaoCaPage] Kết thúc ca:', currentPhanCa.value.id)
  
  loading.value = true
  try {
    const { data } = await api.put(`/api/phan-ca/${currentPhanCa.value.id}/end`)
    console.log('[GiaoCaPage] API end trả về:', data)
    currentPhanCa.value = data
    toastRef.value?.success('Thành công', 'Đã kết thúc ca làm việc!')
    await loadCurrentPhanCa() // Reload để cập nhật UI
  } catch (error: any) {
    console.error('[GiaoCaPage] Lỗi khi kết thúc ca:', error)
    const errorMessage = error.response?.data?.message || error.message || 'Không thể kết thúc ca'
    toastRef.value?.error('Lỗi', errorMessage)
  } finally {
    loading.value = false
  }
}

function goToCreateGiaoCa() {
  if (currentPhanCa.value && currentPhanCa.value.trangThai === 2) {
    // Khi ca đã kết thúc, truyền thông tin phân ca hiện tại
    router.push({
      path: '/giao-ca/create',
      query: {
        phanCaId: currentPhanCa.value.id.toString(),
        nhanVienGiaoId: currentPhanCa.value.nhanVienId.toString()
      }
    })
  } else {
    router.push('/giao-ca/create')
  }
}

function viewGiaoCa(id: number) {
  router.push(`/giao-ca/detail/${id}`)
}

// Watch currentPhanCa để reload ca tiếp theo khi ca hiện tại thay đổi
watch(currentPhanCa, async (newPhanCa, oldPhanCa) => {
  if (newPhanCa && newPhanCa.id) {
    console.log('[GiaoCaPage] Ca hiện tại đã thay đổi, reload ca tiếp theo')
    await loadNextPhanCa()
  }
}, { immediate: false })

onMounted(async () => {
  await Promise.all([
    loadCurrentPhanCa(),
    loadNextPhanCa(),
    loadRecentGiaoCa()
  ])
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}

.content {
  padding: 20px;
  width: 100%;
  max-width: 100%;
  margin: 0 auto;
}

.card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.card-header h2 {
  margin: 0;
  color: #333;
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-description {
  color: #666;
  margin: 0;
}

.btn-giao-ca {
  background: #ff9800;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.btn-giao-ca:hover {
  background: #f57c00;
}

.current-shift-card {
  border-left: 4px solid #007bff;
}

.card-actions {
  display: flex;
  gap: 10px;
}

.btn-start-shift {
  background: #28a745;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.btn-start-shift:hover:not(:disabled) {
  background: #218838;
}

.btn-start-shift:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-end-shift {
  background: #dc3545;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.btn-end-shift:hover:not(:disabled) {
  background: #c82333;
}

.btn-end-shift:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-giao-ca-shift {
  background: #17a2b8;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  transition: background-color 0.3s ease;
}

.btn-giao-ca-shift:hover:not(:disabled) {
  background: #138496;
}

.btn-giao-ca-shift:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.shift-info {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 20px;
}

.shift-details {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 15px;
}

.shift-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.shift-item label {
  font-weight: 500;
  color: #666;
  font-size: 14px;
}

.shift-item .value {
  font-size: 16px;
  color: #333;
}

.shift-status {
  display: flex;
  align-items: flex-start;
}

.status-badge {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
}

.status-pending {
  background: #fff3cd;
  color: #856404;
}

.status-active {
  background: #d4edda;
  color: #155724;
}

.status-finished {
  background: #f8d7da;
  color: #721c24;
}

.status-absent {
  background: #d1ecf1;
  color: #0c5460;
}

.empty-shift-info {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.empty-shift-info svg {
  font-size: 48px;
  margin-bottom: 15px;
  opacity: 0.5;
}

.empty-shift-info p {
  margin: 10px 0;
}

.empty-shift-info .hint {
  font-size: 14px;
  color: #999;
}

.btn-reload {
  margin-top: 15px;
  padding: 10px 20px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  margin-left: auto;
  margin-right: auto;
}

.btn-reload:hover:not(:disabled) {
  background: #0056b3;
}

.btn-reload:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.next-shift-card {
  border-left: 4px solid #17a2b8;
}

.recent-giao-ca-card {
  border-left: 4px solid #6c757d;
}

.giao-ca-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.giao-ca-item {
  padding: 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.giao-ca-item:hover {
  background: #f8f9fa;
  border-color: #007bff;
}

.giao-ca-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.ma-giao-ca {
  font-weight: 600;
  color: #333;
}

.giao-ca-details {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #666;
}
</style>

