import { useAuthStore } from '@/stores/authStore'

// Website Auth Guard - KHÔNG YÊU CẦU ĐĂNG NHẬP cho bất kỳ route nào
// Tất cả routes website đều public, nhưng vẫn cho phép đăng nhập nếu muốn
export const websiteAuthGuard = (to, from, next) => {
  // TẤT CẢ routes trong website router đều được phép truy cập
  // Không cần kiểm tra authentication
  // Nếu user đã đăng nhập, vẫn có thể sử dụng các tính năng yêu cầu đăng nhập
  
  console.log('✅ WebsiteAuthGuard - Route:', to.path, '| Allowing access (all website routes are public)')
  
  // LUÔN cho phép truy cập, không cần kiểm tra gì cả
  next()
}

export const setupWebsiteAuthGuard = (router) => {
  router.beforeEach(websiteAuthGuard)
}

