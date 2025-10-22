import { defineStore } from 'pinia'
import api from '@/services/api'

export interface SanPham {
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
}

export const useSanPhamStore = defineStore('sanPhamStore', {
  state: () => ({
    items: [] as SanPham[],
    loading: false,
  }),
  actions: {
    async fetchAll() {
      this.loading = true
      try {
        const { data } = await api.get<SanPham[]>('/api/san-pham')
        this.items = data
      } finally {
        this.loading = false
      }
    },
  },
})


