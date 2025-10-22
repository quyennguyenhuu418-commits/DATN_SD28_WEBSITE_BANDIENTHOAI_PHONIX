# WebSocket Debug Guide

## 🔍 Cách Debug WebSocket Connection

### 1. Kiểm tra Backend Logs

Khi chạy Spring Boot, bạn sẽ thấy logs như:
```
Received customer join request: {sessionId=customer_123, customerName=Test, ...}
Processing customer: Test with session: customer_123
Sending new customer notification to staff
Sending waiting customers update: 1 customers

Received staff join request: {staffId=staff_456, staffName=Quyên}
Staff joining: Quyên with ID: staff_456
```

### 2. Kiểm tra Frontend Console

**Customer Side:**
```javascript
// Mở DevTools Console
Sending customer join data: {sessionId: "customer_123", customerName: "Test", ...}
Connected to WebSocket
```

**Staff Side:**
```javascript
// Mở DevTools Console
Staff connected to WebSocket
Received staff notification: {"type":"new_customer","sessionId":"customer_123",...}
Received staff notification: {"type":"waiting_customers_update","waitingCustomers":[...],...}
```

### 3. Kiểm tra Network Tab

1. Mở DevTools → Network tab
2. Filter by "WS" (WebSocket)
3. Bạn sẽ thấy:
   - `/ws/customer` - Customer connection
   - `/ws/staff` - Staff connection

### 4. Kiểm tra WebSocket Messages

1. Mở DevTools → Network tab
2. Click vào WebSocket connection
3. Xem "Messages" tab
4. Bạn sẽ thấy các messages được gửi/nhận

## 🐛 Common Issues & Solutions

### Issue 1: "global is not defined"
**Solution:** Đã sửa bằng polyfill và dynamic import

### Issue 2: Customer connects but staff doesn't see
**Possible causes:**
1. Staff chưa join hệ thống
2. WebSocket subscription không đúng
3. Backend không gửi message đúng topic

**Debug steps:**
1. Kiểm tra staff đã join chưa (console log)
2. Kiểm tra backend logs có gửi notification không
3. Kiểm tra frontend có nhận message không

### Issue 3: Staff joins but doesn't see waiting customers
**Possible causes:**
1. Customer chưa join
2. Message format không đúng
3. Frontend không xử lý message đúng

**Debug steps:**
1. Kiểm tra customer đã join chưa
2. Kiểm tra message format trong console
3. Kiểm tra handleNotification method

## 🧪 Test Flow

### Step 1: Start Backend
```bash
mvn spring-boot:run
```

### Step 2: Start Frontend
```bash
cd Fontend
npm run dev
```

### Step 3: Test Customer
1. Mở `http://localhost:3000`
2. Click "Chat với nhân viên"
3. Điền thông tin và click "BẮT ĐẦU TRÒ CHUYỆN"
4. Kiểm tra console logs

### Step 4: Test Staff
1. Mở `http://localhost:3000/staff-chat`
2. Nhập tên nhân viên
3. Kiểm tra console logs
4. Xem có khách hàng xuất hiện không

## 📊 Expected Behavior

### Customer Side:
- ✅ WebSocket connects successfully
- ✅ Sends join message with customer data
- ✅ Receives confirmation message
- ✅ Shows "Đã kết nối thành công!"

### Staff Side:
- ✅ WebSocket connects successfully
- ✅ Sends staff join message
- ✅ Receives waiting customers list
- ✅ Shows customer in "Khách hàng chờ hỗ trợ"
- ✅ Statistics update (count > 0)

### Backend:
- ✅ Receives customer join request
- ✅ Processes customer data
- ✅ Sends notification to staff
- ✅ Updates waiting customers list
- ✅ Receives staff join request
- ✅ Sends current waiting customers to staff

## 🔧 Manual Testing

### Test 1: Customer Join
```bash
# Check backend logs
tail -f logs/application.log | grep "customer join"
```

### Test 2: Staff Join
```bash
# Check backend logs
tail -f logs/application.log | grep "staff join"
```

### Test 3: WebSocket Messages
```javascript
// In browser console
// Check if WebSocket is connected
console.log('WebSocket connected:', stompClient.connected)
```

## 📝 Debug Checklist

- [ ] Backend đang chạy trên port 8080
- [ ] Frontend đang chạy trên port 3000
- [ ] WebSocket proxy config đúng trong vite.config.js
- [ ] CORS config đúng trong Spring Boot
- [ ] Console logs hiển thị đúng
- [ ] Network tab shows WebSocket connections
- [ ] Messages được gửi/nhận đúng format


