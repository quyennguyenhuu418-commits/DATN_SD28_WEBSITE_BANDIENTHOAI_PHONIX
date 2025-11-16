# 🔴 LỖI DATABASE CẦN KHẮC PHỤC

## 📋 Tổng quan
Tài liệu này liệt kê các lỗi database được phát hiện từ backend logs. Các lỗi này cần được khắc phục để ứng dụng hoạt động bình thường.

---

## ❌ LỖI 1: Bảng `phan_ca` không tồn tại

### Mô tả lỗi:
```
Invalid object name 'phan_ca'
SQL Error: 208, SQLState: S0002
```

### Nguyên nhân:
- Entity `PhanCa` có annotation `@Table(name = "phan_ca")` nhưng database không có bảng này
- Migration script `V8__create_shift_management_tables.sql` có thể chưa được chạy hoặc bị lỗi

### Ảnh hưởng:
- ❌ Endpoint `/api/auth/pending-shift-handover` bị lỗi
- ❌ Tất cả các chức năng liên quan đến quản lý ca làm việc không hoạt động
- ❌ Service `GiaoCaServiceImpl.getPendingGiaoCaForNhanVienId()` không thể tìm ca làm việc

### Cách khắc phục:

#### Option 1: Chạy Migration Script (Khuyến nghị)
```sql
-- Kiểm tra xem migration V8 đã chạy chưa
SELECT * FROM flyway_schema_history WHERE script = 'V8__create_shift_management_tables.sql';

-- Nếu chưa chạy, chạy thủ công script V8__create_shift_management_tables.sql
```

#### Option 2: Tạo bảng thủ công
```sql
CREATE TABLE phan_ca (
    id INT IDENTITY(1,1) PRIMARY KEY,
    nhan_vien_id INT NOT NULL,
    ca_id INT NOT NULL,
    ngay_lam_viec DATE NOT NULL,
    trang_thai INT,
    gio_bat_dau_thuc_te DATETIME2,
    gio_ket_thuc_thuc_te DATETIME2,
    ghi_chu NVARCHAR(500),
    ngay_tao DATETIME2,
    ngay_cap_nhat DATETIME2,
    nguoi_tao NVARCHAR(100),
    nguoi_cap_nhat NVARCHAR(100),
    CONSTRAINT UQ_phan_ca_nhan_vien_ca_ngay UNIQUE (nhan_vien_id, ca_id, ngay_lam_viec)
);

-- Tạo index để tối ưu query
CREATE INDEX IX_phan_ca_nhan_vien_id ON phan_ca(nhan_vien_id);
CREATE INDEX IX_phan_ca_ngay_lam_viec ON phan_ca(ngay_lam_viec);
CREATE INDEX IX_phan_ca_nhan_vien_ngay ON phan_ca(nhan_vien_id, ngay_lam_viec);
```

---

## ❌ LỖI 2: Cột `ma_hex` không tồn tại trong bảng `mau_sac`

### Mô tả lỗi:
```
Invalid column name 'ma_hex'
SQL Error: 207, SQLState: S0001
SQL: select ms1_0.id,ms1_0.ma_hex,ms1_0.ma_mau,ms1_0.mo_ta,ms1_0.ngay_cap_nhat,ms1_0.ngay_tao,ms1_0.ten_mau,ms1_0.trang_thai from mau_sac ms1_0 where ms1_0.id=?
```

### Nguyên nhân:
- Entity `MauSac` có field `maHex` với `@Column(name = "ma_hex")` nhưng database không có cột này
- Có thể là migration script chưa được tạo hoặc chưa chạy
- Database schema cũ hơn so với code mới

### Ảnh hưởng:
- ❌ Endpoint `/api/san-pham-pos` bị lỗi khi load danh sách sản phẩm
- ❌ Không thể hiển thị mã màu hex cho màu sắc
- ❌ Service `SanPhamPosServiceImpl.convertToDTO()` bị lỗi khi convert `MauSac`

### Cách khắc phục:

#### Option 1: Thêm cột `ma_hex` vào bảng `mau_sac` (Khuyến nghị)
```sql
-- Kiểm tra cấu trúc bảng hiện tại
SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'mau_sac';

-- Thêm cột ma_hex nếu chưa có
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'mau_sac' AND COLUMN_NAME = 'ma_hex'
)
BEGIN
    ALTER TABLE mau_sac 
    ADD ma_hex VARCHAR(7) NULL;
    
    PRINT 'Đã thêm cột ma_hex vào bảng mau_sac';
END
ELSE
BEGIN
    PRINT 'Cột ma_hex đã tồn tại';
END
```

#### Option 2: Tạo Migration Script mới
Tạo file migration mới: `V17__add_ma_hex_to_mau_sac.sql`
```sql
-- Migration: Thêm cột ma_hex vào bảng mau_sac
ALTER TABLE mau_sac 
ADD ma_hex VARCHAR(7) NULL;

-- Có thể cập nhật dữ liệu mẫu nếu cần
-- UPDATE mau_sac SET ma_hex = '#FF0000' WHERE ten_mau = 'Đỏ';
```

#### Option 3: Tạm thời xóa field `maHex` từ Entity (Không khuyến nghị)
Nếu không cần sử dụng `maHex`, có thể comment hoặc xóa field này trong `MauSac.java`:
```java
// @Column(name = "ma_hex", length = 7)
// private String maHex;
```

---

## 📝 Ghi chú

1. **Kiểm tra Flyway Migration Status:**
   ```sql
   SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC;
   ```

2. **Kiểm tra cấu trúc bảng:**
   ```sql
   -- Kiểm tra bảng phan_ca
   SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'phan_ca';
   
   -- Kiểm tra cột trong mau_sac
   SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE 
   FROM INFORMATION_SCHEMA.COLUMNS 
   WHERE TABLE_NAME = 'mau_sac' 
   ORDER BY ORDINAL_POSITION;
   ```

3. **Sau khi sửa:**
   - Restart Spring Boot application
   - Kiểm tra lại logs để đảm bảo không còn lỗi
   - Test các endpoint bị ảnh hưởng

---

## ✅ Checklist khắc phục

- [ ] Kiểm tra migration script `V8__create_shift_management_tables.sql` đã chạy chưa
- [ ] Tạo bảng `phan_ca` nếu chưa có
- [ ] Thêm cột `ma_hex` vào bảng `mau_sac`
- [x] **Đã sửa:** Thêm `spring.datasource.hikari.auto-commit=false` vào `application.properties`
- [x] **Đã sửa:** Sử dụng `@Transactional(propagation = Propagation.NOT_SUPPORTED)` để tắt transaction cho read-only operation
- [x] **Đã sửa:** Cải thiện error handling trong `getAllProductsForPos()` và `convertToDTO()`
- [ ] Restart backend application
- [ ] Test endpoint `/api/auth/pending-shift-handover`
- [ ] Test endpoint `/api/san-pham-pos`
- [ ] Kiểm tra logs không còn lỗi database

---

## ❌ LỖI 3: Transaction Rollback Error - AutoCommit Mode

### Mô tả lỗi:
```
Unable to rollback against JDBC Connection
Cannot invoke a rollback operation when the AutoCommit mode is set to "true".
```

### Nguyên nhân:
- HikariCP connection pool đang có `auto-commit=true` mặc định
- Khi có exception trong transaction, Hibernate cố rollback nhưng connection đang ở chế độ auto-commit
- Spring Transaction Manager không thể quản lý transaction đúng cách

### Ảnh hưởng:
- ❌ Endpoint `/api/san-pham-pos` bị lỗi transaction rollback
- ❌ Không thể commit hoặc rollback transaction
- ❌ Tất cả các thao tác database có thể bị ảnh hưởng

### Cách khắc phục:

**✅ Đã sửa:**
1. Thêm `spring.datasource.hikari.auto-commit=false` vào `application.properties`
2. Thêm `noRollbackFor = {SQLGrammarException.class}` vào `@Transactional` của `SanPhamPosServiceImpl`
3. Cải thiện error handling trong `convertToDTO()` để tránh exception lan ra ngoài

**Các thay đổi:**

#### 1. application.properties
```properties
# Disable auto-commit to allow proper transaction management
spring.datasource.hikari.auto-commit=false
```

#### 2. SanPhamPosServiceImpl.java
```java
// Thêm noRollbackFor để không rollback transaction khi có SQLGrammarException
@Transactional(noRollbackFor = {org.hibernate.exception.SQLGrammarException.class})
public class SanPhamPosServiceImpl implements SanPhamPosService {
    
    // Cải thiện error handling trong convertToDTO
    try {
        if (chiTiet.getMauSac() != null) {
            dto.setTenMauSac(chiTiet.getMauSac().getTenMau());
        }
    } catch (Exception e) {
        // Set giá trị mặc định nếu có lỗi
        dto.setTenMauSac("N/A");
    }
    
    // Xử lý từng item riêng lẻ để tránh toàn bộ transaction bị rollback
    .map(ct -> {
        try {
            return convertToDTO(ct);
        } catch (Exception e) {
            return null; // Skip item có lỗi
        }
    })
}
```

**Sau khi sửa:**
1. Restart Spring Boot application
2. Lỗi transaction rollback sẽ được khắc phục
3. Endpoint sẽ trả về danh sách sản phẩm hợp lệ (bỏ qua các item có lỗi)

---

## ❌ LỖI 4: Transaction Silently Rolled Back - SQLGrammarException

### Mô tả lỗi:
```
Transaction silently rolled back because it has been marked as rollback-only
UnexpectedRollbackException
```

### Nguyên nhân:
- **DO DATABASE THIẾU DỮ LIỆU/SCHEMA:** Lỗi này xảy ra khi:
  1. Có exception trong transaction (như `SQLGrammarException` do thiếu cột `ma_hex`)
  2. Hibernate đánh dấu transaction là `rollback-only` khi exception xảy ra trong Hibernate query
  3. Code vẫn cố commit transaction → Lỗi "silently rolled back"
  4. Exception được catch trong `convertToDTO()` nhưng transaction đã bị đánh dấu rollback

### Ảnh hưởng:
- ❌ Endpoint `/api/san-pham-pos` bị lỗi transaction rollback
- ❌ Không thể lấy danh sách sản phẩm cho POS
- ❌ Toàn bộ transaction bị rollback ngay cả khi chỉ 1 item có lỗi

### Cách khắc phục:

**✅ Đã sửa:**
1. Sử dụng `@Transactional(propagation = Propagation.NOT_SUPPORTED)` để tắt transaction cho read-only operation
2. Wrap toàn bộ method trong try-catch để return empty list khi có lỗi
3. Xử lý từng item riêng lẻ trong stream.map để tránh toàn bộ transaction bị rollback

**Các thay đổi:**

#### SanPhamPosServiceImpl.java
```java
// Tắt transaction hoàn toàn cho read-only operation
@Override
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public List<SanPhamPosDTO> getAllProductsForPos() {
    try {
        // ... code ...
        return result;
    } catch (Exception e) {
        // Return empty list thay vì throw exception
        return new ArrayList<>();
    }
}
```

**Lưu ý quan trọng:**
- **Lỗi này CHỦ YẾU DO DATABASE THIẾU SCHEMA:**
  - Thiếu cột `ma_hex` trong bảng `mau_sac` → Gây ra `SQLGrammarException`
  - Khi Hibernate cố load `MauSac` entity, nó query cột `ma_hex` → Exception → Transaction bị đánh dấu rollback-only
  
- **Giải pháp tạm thời:** Đã sửa code để không sử dụng transaction cho read-only operation
- **Giải pháp vĩnh viễn:** Cần sửa database schema (thêm cột `ma_hex` vào bảng `mau_sac`)

**Sau khi sửa:**
1. Restart Spring Boot application
2. Endpoint sẽ trả về empty list nếu có lỗi database
3. **QUAN TRỌNG:** Cần sửa database schema để khắc phục hoàn toàn

---

**Ngày tạo:** 2025-01-04  
**Ngày cập nhật:** 2025-01-04  
**Phiên bản:** 1.1

