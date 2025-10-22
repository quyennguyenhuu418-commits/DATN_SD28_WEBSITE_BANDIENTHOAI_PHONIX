# Hướng dẫn cài đặt OTP với Redis

## 🚀 **Có 3 cách để chạy OTP system:**

### **Cách 1: Sử dụng Redis (Khuyến nghị cho Production)**

#### **Option A: Cài đặt Redis trên Windows**
```bash
# Chạy script tự động
install_redis_windows.bat

# Hoặc cài đặt thủ công:
# 1. Tải Redis for Windows từ: https://github.com/microsoftarchive/redis/releases
# 2. Cài đặt và chạy Redis service
# 3. Redis sẽ chạy trên localhost:6379
```

#### **Option B: Sử dụng Docker**
```bash
# Chạy Redis với Docker
docker-compose -f docker-compose-redis.yml up -d

# Kiểm tra Redis đang chạy
docker ps
```

#### **Cấu hình Application**
```properties
# Trong application.properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=
spring.data.redis.database=0
```

### **Cách 2: Sử dụng In-Memory (Khuyến nghị cho Development)**

#### **Thay đổi AuthController**
```java
// Thay đổi từ:
@Autowired
private RedisOtpService redisOtpService;

// Thành:
@Autowired
private InMemoryOtpService inMemoryOtpService;
```

#### **Cập nhật các method calls**
```java
// Thay đổi tất cả redisOtpService thành inMemoryOtpService
String otpCode = inMemoryOtpService.createOtp(request.getEmail());
if (!inMemoryOtpService.validateOtp(request.getEmail(), request.getOtp())) {
    // ...
}
```

### **Cách 3: Sử dụng Database (Hiện tại)**

Giữ nguyên code hiện tại với `OtpService` và `OtpCode` entity.

## 🔧 **So sánh các phương pháp:**

| Phương pháp | Ưu điểm | Nhược điểm | Phù hợp |
|-------------|---------|------------|---------|
| **Redis** | - Nhanh nhất<br>- Tự động expire<br>- Scalable | - Cần cài Redis<br>- Phức tạp hơn | Production |
| **In-Memory** | - Đơn giản<br>- Không cần DB<br>- Nhanh | - Mất data khi restart<br>- Không scalable | Development |
| **Database** | - Persistent<br>- Đơn giản<br>- Có sẵn | - Chậm hơn<br>- Cần cleanup | Small scale |

## 🚀 **Khuyến nghị:**

### **Cho Development:**
- Sử dụng **In-Memory** - đơn giản, nhanh chóng

### **Cho Production:**
- Sử dụng **Redis** - hiệu suất cao, scalable

## 📝 **Cách chuyển đổi:**

### **Từ Database sang Redis:**
1. Cài đặt Redis
2. Thay `OtpService` → `RedisOtpService` trong AuthController
3. Xóa bảng `otp_codes` (không cần nữa)

### **Từ Database sang In-Memory:**
1. Thay `OtpService` → `InMemoryOtpService` trong AuthController
2. Xóa bảng `otp_codes` (không cần nữa)

## 🔍 **Test OTP System:**

```bash
# Test forgot password
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@phonestore.com"}'

# Test reset password
curl -X POST http://localhost:8080/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@phonestore.com",
    "otp": "123456",
    "newPassword": "newpass123",
    "confirmPassword": "newpass123"
  }'
```

## 🎯 **Kết luận:**

- **Redis**: Tốt nhất cho production, cần cài đặt thêm
- **In-Memory**: Tốt nhất cho development, đơn giản nhất
- **Database**: Đơn giản nhưng chậm hơn, cần cleanup

Chọn phương pháp phù hợp với nhu cầu của bạn! 🚀
