# Hệ Thống Chat WebSocket - Hướng Dẫn Sử Dụng

## Tổng Quan

Hệ thống chat WebSocket được thiết kế để cho phép khách hàng chat trực tiếp với nhân viên hỗ trợ thông qua giao diện web. Hệ thống bao gồm:

- **Customer Chat Widget**: Widget chat cho khách hàng
- **Staff Dashboard**: Dashboard cho nhân viên hỗ trợ
- **WebSocket Backend**: Xử lý real-time communication
- **Database Integration**: Lưu trữ thông tin chat và khách hàng

## Kiến Trúc Hệ Thống

```
Frontend (Vue.js)
├── CustomerChatWidget.vue - Widget chat cho khách hàng
├── StaffChatDashboard.vue - Dashboard cho nhân viên
└── WebSocket Utils - Quản lý kết nối WebSocket

Backend (Spring Boot)
├── WebSocketConfig.java - Cấu hình WebSocket
├── CustomerChatController.java - Controller xử lý chat
├── CustomerChatService.java - Service quản lý chat sessions
└── DTOs - Data Transfer Objects

Database
├── Customer Chat Sessions
├── Staff Notifications
└── Chat Messages
```

## Cài Đặt và Chạy

### Backend (Spring Boot)

1. **Dependencies đã được thêm vào `pom.xml`:**
   ```xml
   <dependency>
       <groupId>org.springframework</groupId>
       <artifactId>spring-messaging</artifactId>
   </dependency>
   ```

2. **Chạy ứng dụng Spring Boot:**
   ```bash
   mvn spring-boot:run
   ```

3. **WebSocket endpoints:**
   - `/ws` - Endpoint chính
   - `/ws/customer` - Endpoint cho khách hàng
   - `/ws/staff` - Endpoint cho nhân viên

### Frontend (Vue.js)

1. **Dependencies đã được cài đặt:**
   ```bash
   npm install sockjs-client stompjs
   ```

2. **Chạy ứng dụng Vue:**
   ```bash
   npm run dev
   ```

3. **Truy cập:**
   - Giao diện chính: `http://localhost:3000`
   - Staff Dashboard: `http://localhost:3000/staff-chat`

## Cách Sử Dụng

### Cho Khách Hàng

1. **Mở Chat Widget:**
   - Widget chat xuất hiện ở góc phải màn hình
   - Click vào nút "Chat với nhân viên" để mở

2. **Điền Thông Tin:**
   - Tên (bắt buộc)
   - Email (tùy chọn)
   - Số điện thoại (bắt buộc)
   - Giới tính (tùy chọn)
   - Tin nhắn ban đầu (tùy chọn)

3. **Bắt Đầu Chat:**
   - Click "BẮT ĐẦU TRÒ CHUYỆN"
   - Chờ nhân viên hỗ trợ kết nối
   - Gửi tin nhắn khi đã được kết nối

### Cho Nhân Viên

1. **Truy Cập Dashboard:**
   - Vào menu "Chat Hỗ Trợ" trong sidebar
   - Hoặc truy cập trực tiếp `/staff-chat`

2. **Đăng Nhập:**
   - Nhập tên nhân viên khi được yêu cầu
   - Hệ thống sẽ tự động kết nối

3. **Quản Lý Khách Hàng:**
   - Xem danh sách khách hàng chờ hỗ trợ
   - Click vào khách hàng để phân công
   - Chat trực tiếp với khách hàng

4. **Thống Kê:**
   - Số khách hàng chờ
   - Số cuộc trò chuyện đang diễn ra
   - Số nhân viên online

## API Endpoints

### WebSocket Messages

#### Customer Messages
- `/app/customer/join` - Khách hàng tham gia chat
- `/app/customer/message` - Gửi tin nhắn từ khách hàng
- `/app/customer/disconnect` - Khách hàng ngắt kết nối

#### Staff Messages
- `/app/staff/join` - Nhân viên tham gia hệ thống
- `/app/staff/leave` - Nhân viên rời khỏi hệ thống
- `/app/staff/assign` - Phân công khách hàng cho nhân viên
- `/app/staff/message` - Gửi tin nhắn từ nhân viên

### REST Endpoints

- `GET /api/chat/waiting-customers` - Lấy danh sách khách hàng chờ
- `GET /api/chat/stats` - Lấy thống kê hệ thống

## Cấu Hình

### WebSocket Configuration

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    // Cấu hình message broker và endpoints
}
```

### Frontend Configuration

```javascript
// Kết nối WebSocket
const socket = new SockJS('/ws/customer')
const stompClient = Stomp.over(socket)
```

## Tính Năng Chính

### Real-time Communication
- Chat real-time giữa khách hàng và nhân viên
- Thông báo tức thì khi có khách hàng mới
- Cập nhật trạng thái online/offline

### Queue Management
- Hàng đợi khách hàng chờ hỗ trợ
- Phân công tự động hoặc thủ công
- Quản lý tải công việc

### Notifications
- Thông báo cho nhân viên khi có khách hàng mới
- Thông báo trạng thái kết nối
- Thông báo tin nhắn mới

### Analytics
- Thống kê số lượng khách hàng
- Thống kê nhân viên online
- Thời gian phản hồi

## Xử Lý Lỗi

### Common Issues

1. **WebSocket Connection Failed:**
   - Kiểm tra backend đang chạy
   - Kiểm tra CORS configuration
   - Kiểm tra firewall settings

2. **Messages Not Received:**
   - Kiểm tra subscription topics
   - Kiểm tra message format
   - Kiểm tra network connection

3. **Staff Cannot See Customers:**
   - Kiểm tra staff đã join chưa
   - Kiểm tra permissions
   - Kiểm tra database connection

### Debug Mode

```javascript
// Enable debug logs
stompClient.debug = (str) => {
  console.log('STOMP: ' + str)
}
```

## Bảo Mật

### Authentication
- Staff authentication (có thể tích hợp với hệ thống auth hiện tại)
- Session management
- Rate limiting

### Data Protection
- Mã hóa tin nhắn nhạy cảm
- Logging và audit trail
- GDPR compliance

## Mở Rộng

### Tính Năng Có Thể Thêm
- File sharing
- Voice messages
- Video calls
- Chat history
- AI chatbot integration
- Multi-language support

### Integration
- CRM system
- Ticketing system
- Analytics platform
- Notification services

## Troubleshooting

### Backend Issues
```bash
# Check logs
tail -f logs/application.log

# Check WebSocket connections
netstat -an | grep 8080
```

### Frontend Issues
```javascript
// Check WebSocket connection
console.log('Connected:', stompClient.connected)

// Check subscriptions
console.log('Subscriptions:', subscriptions)
```

## Support

Nếu gặp vấn đề, vui lòng:
1. Kiểm tra logs
2. Kiểm tra network connection
3. Restart services
4. Liên hệ team phát triển

---

**Lưu ý:** Hệ thống này được thiết kế cho môi trường development. Để sử dụng production, cần thêm các tính năng bảo mật và tối ưu hóa performance.


