# Hướng dẫn chạy Admin và Website

## Cấu hình Port

- **Port 5173**: Admin Panel (Quản lý)
- **Port 5174**: Website (Cửa hàng)

## Cách chạy

### Chạy riêng lẻ

```bash
# Chạy Admin (port 5173)
npm run dev:admin

# Chạy Website (port 5174)
npm run dev:website
```

### Chạy đồng thời cả 2

```bash
npm run dev:all
```

## Truy cập

- **Admin**: http://localhost:5173/index-admin.html hoặc http://localhost:5173/
- **Website**: http://localhost:5174/index-website.html hoặc http://localhost:5174/

## Lưu ý

- Hai port chạy độc lập, không liên quan đến nhau
- Chỉ dùng chung Backend API (port 8080)
- Mỗi port có router riêng:
  - Admin: `src/router/adminRouter.js`
  - Website: `src/router/websiteRouter.js`
- Mỗi port có entry point riêng:
  - Admin: `src/main-admin.js` → `index-admin.html`
  - Website: `src/main-website.js` → `index-website.html`

## Troubleshooting

Nếu thấy route `/dat-hang` trên port 5173 (Admin):
1. Đảm bảo đang chạy đúng script: `npm run dev:admin`
2. Kiểm tra console log có hiển thị "✅ Admin HTML Plugin: Serving index-admin.html"
3. Xóa cache và restart: `rm -rf node_modules/.vite && npm run dev:admin`

