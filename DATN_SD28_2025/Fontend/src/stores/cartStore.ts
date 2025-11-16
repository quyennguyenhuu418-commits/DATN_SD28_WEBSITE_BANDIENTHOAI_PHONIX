import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface CartItem {
  id: number
  chiTietSanPhamId: number
  tenSanPham: string
  gia: number
  hinhAnh: string
  tenRam?: string
  tenRom?: string
  tenMauSac?: string
  soLuongTon: number
  quantity: number
}

export const useCartStore = defineStore('cart', () => {
  // State
  const items = ref<CartItem[]>([])
  const wishlist = ref<number[]>([])

  // Load from localStorage
  const loadFromStorage = () => {
    try {
      const savedCart = localStorage.getItem('phonix_cart')
      const savedWishlist = localStorage.getItem('phonix_wishlist')
      
      if (savedCart) {
        items.value = JSON.parse(savedCart)
      }
      
      if (savedWishlist) {
        wishlist.value = JSON.parse(savedWishlist)
      }
    } catch (error) {
      console.error('Error loading cart from storage:', error)
    }
  }

  // Save to localStorage
  const saveToStorage = () => {
    try {
      localStorage.setItem('phonix_cart', JSON.stringify(items.value))
      localStorage.setItem('phonix_wishlist', JSON.stringify(wishlist.value))
    } catch (error) {
      console.error('Error saving cart to storage:', error)
    }
  }

  // Computed
  const itemCount = computed(() => {
    return items.value.reduce((total, item) => total + item.quantity, 0)
  })

  const totalPrice = computed(() => {
    return items.value.reduce((total, item) => total + (item.gia * item.quantity), 0)
  })

  const wishlistCount = computed(() => wishlist.value.length)

  // Actions
  const addItem = (product: any) => {
    const existingItem = items.value.find(
      item => item.chiTietSanPhamId === product.chiTietSanPhamId
    )

    if (existingItem) {
      // Check stock
      if (existingItem.quantity < product.soLuongTon) {
        existingItem.quantity++
      } else {
        throw new Error('Đã đạt số lượng tối đa có thể mua')
      }
    } else {
      items.value.push({
        id: product.id,
        chiTietSanPhamId: product.chiTietSanPhamId,
        tenSanPham: product.tenSanPham,
        gia: product.gia,
        hinhAnh: product.hinhAnh,
        tenRam: product.tenRam,
        tenRom: product.tenRom,
        tenMauSac: product.tenMauSac,
        soLuongTon: product.soLuongTon,
        quantity: 1
      })
    }

    saveToStorage()
  }

  const removeItem = (chiTietSanPhamId: number) => {
    const index = items.value.findIndex(item => item.chiTietSanPhamId === chiTietSanPhamId)
    if (index !== -1) {
      items.value.splice(index, 1)
      saveToStorage()
    }
  }

  const updateQuantity = (chiTietSanPhamId: number, quantity: number) => {
    const item = items.value.find(item => item.chiTietSanPhamId === chiTietSanPhamId)
    if (item) {
      if (quantity <= 0) {
        removeItem(chiTietSanPhamId)
      } else if (quantity <= item.soLuongTon) {
        item.quantity = quantity
        saveToStorage()
      } else {
        throw new Error('Số lượng vượt quá tồn kho')
      }
    }
  }

  const increaseQuantity = (chiTietSanPhamId: number) => {
    const item = items.value.find(item => item.chiTietSanPhamId === chiTietSanPhamId)
    if (item && item.quantity < item.soLuongTon) {
      item.quantity++
      saveToStorage()
    } else {
      throw new Error('Đã đạt số lượng tối đa')
    }
  }

  const decreaseQuantity = (chiTietSanPhamId: number) => {
    const item = items.value.find(item => item.chiTietSanPhamId === chiTietSanPhamId)
    if (item) {
      if (item.quantity > 1) {
        item.quantity--
        saveToStorage()
      } else {
        removeItem(chiTietSanPhamId)
      }
    }
  }

  const clearCart = () => {
    items.value = []
    saveToStorage()
  }

  const setSelectedItems = (selectedItems: CartItem[]) => {
    items.value = selectedItems
    saveToStorage()
  }

  // Wishlist actions
  const addToWishlist = (productId: number) => {
    if (!wishlist.value.includes(productId)) {
      wishlist.value.push(productId)
      saveToStorage()
    }
  }

  const removeFromWishlist = (productId: number) => {
    const index = wishlist.value.indexOf(productId)
    if (index !== -1) {
      wishlist.value.splice(index, 1)
      saveToStorage()
    }
  }

  const toggleWishlist = (productId: number) => {
    if (wishlist.value.includes(productId)) {
      removeFromWishlist(productId)
      return false
    } else {
      addToWishlist(productId)
      return true
    }
  }

  const isInWishlist = (productId: number) => {
    return wishlist.value.includes(productId)
  }

  const clearWishlist = () => {
    wishlist.value = []
    saveToStorage()
  }

  // Initialize
  loadFromStorage()

  return {
    // State
    items,
    wishlist,
    
    // Computed
    itemCount,
    totalPrice,
    wishlistCount,
    
    // Actions
    addItem,
    removeItem,
    updateQuantity,
    increaseQuantity,
    decreaseQuantity,
    clearCart,
    setSelectedItems,
    addToWishlist,
    removeFromWishlist,
    toggleWishlist,
    isInWishlist,
    clearWishlist
  }
})







