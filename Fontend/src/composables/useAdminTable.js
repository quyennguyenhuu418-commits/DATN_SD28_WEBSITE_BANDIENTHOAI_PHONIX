// Common composable for admin table functionality
import { ref, computed } from 'vue'

export function useAdminTable(initialItemsPerPage = 10) {
  // Reactive state
  const searchQuery = ref('')
  const currentPage = ref(1)
  const itemsPerPage = ref(initialItemsPerPage)
  const data = ref([])

  // Computed properties
  const filteredData = computed(() => {
    if (!searchQuery.value.trim()) {
      return data.value
    }
    
    const query = searchQuery.value.toLowerCase().trim()
    return data.value.filter(item => {
      // Search in common fields
      const searchableFields = [
        item.ten || item.tenHeDieuHanh || item.tenMau || item.kichThuoc || item.thongSo || item.dungLuongPin || item.tenChip || item.tenCpu || item.tenGpu || item.loaiSim || item.tenRam || item.tenChip,
        item.maHang || item.maHeDieuHanh || item.maMau || item.maManHinh || item.maCameraTruoc || item.maCameraSau || item.maPin || item.maChip || item.maCpu || item.maGpu || item.maSim || item.maRam || item.maRom,
        item.moTa || item.xuatXu || item.doPhanGiai || item.dungLuong || item.dungLuong,
        item.tenSanPham || item.maSanPham
      ]
      
      return searchableFields.some(field => 
        field && field.toString().toLowerCase().includes(query)
      )
    })
  })

  const totalPages = computed(() => {
    return Math.ceil(filteredData.value.length / itemsPerPage.value)
  })

  const paginatedData = computed(() => {
    const start = (currentPage.value - 1) * itemsPerPage.value
    const end = start + itemsPerPage.value
    return filteredData.value.slice(start, end)
  })

  const visiblePages = computed(() => {
    const pages = []
    const total = totalPages.value
    const current = currentPage.value
    
    if (total <= 7) {
      // Show all pages if total is 7 or less
      for (let i = 1; i <= total; i++) {
        pages.push(i)
      }
    } else {
      // Show first page
      pages.push(1)
      
      if (current > 4) {
        pages.push('...')
      }
      
      // Show pages around current page
      const start = Math.max(2, current - 1)
      const end = Math.min(total - 1, current + 1)
      
      for (let i = start; i <= end; i++) {
        if (!pages.includes(i)) {
          pages.push(i)
        }
      }
      
      if (current < total - 3) {
        pages.push('...')
      }
      
      // Show last page
      if (total > 1) {
        pages.push(total)
      }
    }
    
    return pages
  })

  // Methods
  const handleSearch = () => {
    currentPage.value = 1 // Reset to first page when searching
  }

  const resetFilter = () => {
    searchQuery.value = ''
    currentPage.value = 1
  }

  const goToPage = (page) => {
    if (page >= 1 && page <= totalPages.value && page !== currentPage.value) {
      currentPage.value = page
    }
  }

  const handleItemsPerPageChange = () => {
    currentPage.value = 1 // Reset to first page when changing items per page
  }

  const setData = (newData) => {
    data.value = newData
    currentPage.value = 1 // Reset to first page when data changes
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

  return {
    // State
    searchQuery,
    currentPage,
    itemsPerPage,
    data,
    
    // Computed
    filteredData,
    totalPages,
    paginatedData,
    visiblePages,
    
    // Methods
    handleSearch,
    resetFilter,
    goToPage,
    handleItemsPerPageChange,
    setData,
    formatDate
  }
}
