# 🚀 Dashboard System - Hướng dẫn sử dụng

## 📋 Tổng quan

Dashboard system đã được làm lại hoàn toàn với:
- **Backend**: 11 API endpoints chuyên nghiệp
- **Frontend**: UI/UX hiện đại với Chart.js
- **Database**: Queries tối ưu cho SQL Server
- **Real-time Data**: Load data thật từ database

## 🎯 Tính năng chính

### 📊 Metrics Dashboard
- **Tổng doanh thu** với % thay đổi
- **Số đơn hàng** với trend analysis
- **Sản phẩm bán ra** với tracking
- **Khách hàng mới** với growth rate

### 📈 Charts & Visualizations
- **Revenue Chart**: Biểu đồ doanh thu 7 ngày
- **Orders Chart**: Biểu đồ đơn hàng
- **Products Chart**: Biểu đồ sản phẩm bán chạy
- **Order Status Chart**: Phân bố trạng thái đơn hàng
- **Customers Chart**: Biểu đồ khách hàng mới

### 🔧 Quick Actions
- Quản lý sản phẩm
- Quản lý đơn hàng
- Quản lý khách hàng
- Quản lý nhân viên

### 📋 Data Sections
- **Recent Orders**: 5 đơn hàng gần nhất
- **Top Products**: 5 sản phẩm bán chạy nhất

## 🛠️ Backend APIs

### Core Endpoints
```
GET /api/dashboard/overview?timeRange=7days
GET /api/dashboard/stats
GET /api/dashboard/summary?timeRange=7days
```

### Chart Data Endpoints
```
GET /api/dashboard/revenue-chart?timeRange=7days
GET /api/dashboard/orders-chart?timeRange=7days
GET /api/dashboard/products-chart?timeRange=7days
GET /api/dashboard/order-status-chart?timeRange=7days
GET /api/dashboard/customers-chart?timeRange=30days
```

### Data Endpoints
```
GET /api/dashboard/recent-orders?limit=5
GET /api/dashboard/top-products?limit=5&timeRange=7days
GET /api/dashboard/staff-performance?timeRange=7days
```

## 🎨 Frontend Features

### UI/UX Design
- **Color Scheme**: Orange (#ff8c42) và White
- **Responsive**: Mobile-first design
- **Animations**: Smooth transitions và hover effects
- **Charts**: Interactive Chart.js visualizations

### Components
- **PosHeader**: Navigation header
- **Metrics Cards**: Key performance indicators
- **Quick Actions**: Fast navigation
- **Data Tables**: Recent orders và top products
- **Charts**: Real-time data visualization

## 🚀 Cách sử dụng

### 1. Khởi động Backend
```bash
cd "C:\Users\QUYÊN\Desktop\000000\DATN_SD28_2025"
mvn spring-boot:run
```

### 2. Khởi động Frontend
```bash
cd Fontend
npm run serve
```

### 3. Test APIs
Mở `test-dashboard-api.html` trong browser để test tất cả APIs.

### 4. Truy cập Dashboard
- URL: `http://localhost:8081/dashboard`
- Hoặc click vào "Dashboard" trong menu

## 📊 Data Flow

### 1. API Call Flow
```
Frontend → DashboardController → DashboardService → Repository → Database
```

### 2. Fallback System
```
Primary API → Basic Calculation → Default Data
```

### 3. Real-time Updates
- Click "Làm mới dữ liệu" để refresh
- Auto-load khi vào trang
- Error handling với fallback data

## 🔧 Configuration

### Time Ranges
- `7days`: 7 ngày gần nhất
- `30days`: 30 ngày gần nhất  
- `1year`: 1 năm gần nhất

### Chart Types
- **Line Chart**: Revenue trends
- **Bar Chart**: Orders comparison
- **Doughnut Chart**: Status distribution

## 🐛 Troubleshooting

### Common Issues

1. **API 404 Error**
   - Kiểm tra backend đã chạy chưa
   - Kiểm tra CORS configuration

2. **Data không hiển thị**
   - Kiểm tra database connection
   - Xem console log để debug

3. **Chart không render**
   - Kiểm tra Chart.js đã import
   - Kiểm tra canvas element

### Debug Steps

1. Mở Developer Tools (F12)
2. Xem Console tab để check errors
3. Xem Network tab để check API calls
4. Sử dụng `test-dashboard-api.html` để test APIs

## 📈 Performance

### Optimizations
- **Parallel API calls** với Promise.all
- **Caching** cho static data
- **Lazy loading** cho charts
- **Responsive images** và icons

### Database Queries
- **Indexed queries** cho performance
- **Native SQL** cho complex operations
- **Pagination** cho large datasets

## 🎯 Future Enhancements

### Planned Features
- Real-time notifications
- Export data to Excel/PDF
- Custom date range picker
- Advanced filtering options
- Mobile app integration

### Technical Improvements
- Redis caching
- WebSocket real-time updates
- Advanced analytics
- Machine learning insights

## 📞 Support

Nếu gặp vấn đề:
1. Check console logs
2. Test APIs với `test-dashboard-api.html`
3. Kiểm tra database connection
4. Restart backend server

---

**Dashboard System v2.0** - Professional, Modern, and Fully Functional! 🚀
