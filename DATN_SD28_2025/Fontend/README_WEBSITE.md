# Hướng dẫn chạy Website (Port 5174)

## Quan trọng:
- Website chạy trên **port 5174**
- Website **KHÔNG YÊU CẦU ĐĂNG NHẬP** - tất cả routes đều public
- Admin chạy trên **port 5173** - yêu cầu đăng nhập

## Cách chạy:

### Chạy Website (port 5174):
```bash
npm run dev:website
```

Sau đó truy cập: **http://localhost:5174**

### Chạy Admin (port 5173):
```bash
npm run dev:admin
```

Sau đó truy cập: **http://localhost:5173**

### Chạy cả 2 cùng lúc:
```bash
npm run dev:all
```

## Kiểm tra:
- Nếu thấy log `✅ WebsiteAuthGuard` trong console → Đúng (website)
- Nếu thấy log `AuthGuard` (không có Website) → Sai (đang chạy admin router)

## Lưu ý:
- Đảm bảo backend đang chạy trên port 8080
- Nếu bị redirect về login khi vào website → Đang chạy sai entry point

