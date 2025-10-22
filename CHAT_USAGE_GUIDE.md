# Hướng Dẫn Sử Dụng Chat System

## 🎯 Cách Sử Dụng

### Cho Khách Hàng:

1. **Mở Chat:**
   - Click vào nút đỏ "Chat với nhân viên" ở góc phải màn hình
   - Điền thông tin: Tên, SĐT (bắt buộc), Email, Giới tính (tùy chọn)
   - Click "BẮT ĐẦU TRÒ CHUYỆN"

2. **Chat:**
   - Gõ tin nhắn vào ô "Nhập tin nhắn..."
   - Nhấn Enter hoặc click nút gửi
   - Chờ nhân viên phản hồi

3. **Đóng Chat:**
   - Click nút "X" ở góc phải header chat
   - Chat sẽ đóng và hiện lại nút mở chat

### Cho Nhân Viên:

1. **Truy Cập Dashboard:**
   - Vào menu "Chat Hỗ Trợ" trong sidebar
   - Hoặc truy cập `/staff-chat`

2. **Xem Khách Hàng:**
   - Danh sách "Khách hàng chờ hỗ trợ" ở bên trái
   - Click vào khách hàng để phân công

3. **Chat Với Khách Hàng:**
   - Click vào khách hàng trong danh sách
   - Khung chat sẽ mở ở giữa màn hình
   - Gõ tin nhắn và gửi

4. **Quản Lý Chat:**
   - Nút "−" để thu nhỏ chat
   - Nút "×" để đóng chat
   - Chat sẽ hiển thị trong "Cuộc trò chuyện đang diễn ra"

## 🔧 Tính Năng

### Khách Hàng:
- ✅ Form đăng ký chat với thông tin cơ bản
- ✅ Real-time messaging
- ✅ Thông báo tin nhắn mới (chấm đỏ)
- ✅ Nút đóng chat rõ ràng
- ✅ Trạng thái kết nối

### Nhân Viên:
- ✅ Dashboard tổng quan với thống kê
- ✅ Danh sách khách hàng chờ
- ✅ Khung chat riêng cho từng khách hàng
- ✅ Nút thu nhỏ và đóng chat
- ✅ Thông báo real-time
- ✅ Quản lý nhiều cuộc trò chuyện

## 🚀 Cách Test

1. **Mở 2 tab trình duyệt:**
   - Tab 1: Giao diện chính (khách hàng)
   - Tab 2: `/staff-chat` (nhân viên)

2. **Test Flow:**
   - Tab 1: Mở chat widget, điền thông tin, bắt đầu chat
   - Tab 2: Xem khách hàng xuất hiện, click để phân công
   - Chat giữa 2 tab

## 🐛 Troubleshooting

### Lỗi "global is not defined":
- Đã sửa bằng polyfill và dynamic import
- Restart dev server nếu vẫn lỗi

### WebSocket không kết nối:
- Kiểm tra backend đang chạy
- Kiểm tra proxy config trong vite.config.js

### Chat không hiển thị:
- Kiểm tra console errors
- Kiểm tra network tab trong DevTools

## 📱 Responsive

- Chat widget responsive trên mobile
- Staff dashboard responsive
- Chat window có thể thu nhỏ

## 🎨 UI/UX

- Màu đỏ chủ đạo cho chat
- Animation mượt mà
- Thông báo rõ ràng
- Icons FontAwesome
- Gradient backgrounds


