# HỆ THỐNG QUẢN LÝ GIAO CA - PHONIX STORE

## 📋 Tổng Quan

Hệ thống quản lý giao ca được thiết kế để đơn giản hóa việc giao ca cho website bán điện thoại thông minh PhoniX Store, tập trung vào hai mục tiêu chính:

1. **Kiểm soát chặt chẽ lịch làm việc của nhân viên**
2. **Đảm bảo việc bàn giao các chỉ số kinh doanh và tiền thu được diễn ra minh bạch, chính xác**

## 🎯 Vai Trò & Trách Nhiệm

### Vai Trò Nhân Viên
- Thực hiện công việc được giao trong ca
- Ghi nhận các chỉ số kinh doanh và sự kiện quan trọng
- Hoàn thành biên bản bàn giao ca vào cuối giờ làm việc
- Bàn giao trực tiếp cho nhân viên ca sau

### Vai Trò Quản Lý
- Xếp lịch và phân ca cho nhân viên hàng tuần/tháng
- Giám sát, kiểm tra các báo cáo giao ca hàng ngày
- Phân tích hiệu suất theo ca và đưa ra quyết định cải tiến
- Giải quyết các vấn đề tồn đọng hoặc ngoại lệ được báo cáo

## 🔄 Luồng Giao Ca Chuẩn (4 Bước)

### 1. Đầu Ca: Nhận Bàn Giao & Chuẩn Bị
- Nhân viên đọc báo cáo của ca trước
- Kiểm tra các công việc tồn đọng
- Nắm mục tiêu và các chỉ số quan trọng cần theo dõi trong ca

### 2. Trong Ca: Thực Hiện & Ghi Nhận
- Tập trung xử lý công việc
- Ghi nhận lại các số liệu phát sinh (doanh thu, đơn hàng)
- Ghi nhận các sự cố bất thường (nếu có) vào file nháp hoặc trực tiếp vào biểu mẫu giao ca

### 3. Cuối Ca: Tổng Hợp & Hoàn Thành Báo Cáo
- Dành 15-30 phút cuối ca để tổng hợp số liệu
- Điền đầy đủ thông tin vào "Biên bản Bàn giao ca"
- Đảm bảo mọi thông tin đều chính xác và rõ ràng

### 4. Bàn Giao: Xác Nhận & Kết Thúc Ca
- Gửi biên bản cho nhân viên ca sau
- Trao đổi nhanh về các điểm chính
- Sau khi ca sau xác nhận đã nhận đủ thông tin, nhân viên kết thúc ca
- Quản lý sẽ nhận thông báo và rà soát báo cáo sau đó

## 🗄️ Cấu Trúc Database

### Bảng Chính

#### 1. `ca` - Quản lý ca làm việc
```sql
- id: Khóa chính
- ma_ca: Mã ca (unique)
- ten_ca: Tên ca (Ca Sáng, Ca Chiều, Ca Tối, Ca Đêm)
- mo_ta: Mô tả ca
- gio_bat_dau: Giờ bắt đầu
- gio_ket_thuc: Giờ kết thúc
- trang_thai: Trạng thái (1: Hoạt động, 0: Tạm dừng)
```

#### 2. `phan_ca` - Phân ca cho nhân viên
```sql
- id: Khóa chính
- nhan_vien_id: ID nhân viên
- ca_id: ID ca
- ngay_lam_viec: Ngày làm việc
- trang_thai: Trạng thái (0: Chưa bắt đầu, 1: Đang làm, 2: Đã kết thúc, 3: Vắng mặt)
- gio_bat_dau_thuc_te: Giờ bắt đầu thực tế
- gio_ket_thuc_thuc_te: Giờ kết thúc thực tế
```

#### 3. `giao_ca` - Quản lý giao ca và bàn giao tiền mặt
```sql
- id: Khóa chính
- ma_giao_ca: Mã giao ca (unique)
- phan_ca_id: ID phân ca
- nhan_vien_giao_id: ID nhân viên giao
- nhan_vien_nhan_id: ID nhân viên nhận
- ngay_giao_ca: Ngày giao ca
- so_tien_dau_ca: Số tiền đầu ca
- so_tien_cuoi_ca: Số tiền cuối ca
- so_tien_thu_them: Số tiền thu thêm
- so_tien_chi_ra: Số tiền chi ra
- chenh_lech: Chênh lệch tiền mặt
- tong_doanh_thu: Tổng doanh thu ca
- so_don_hang: Số đơn hàng
- trang_thai: Trạng thái (0: Chờ xác nhận, 1: Đã xác nhận, 2: Đã hủy)
```

#### 4. `chi_tiet_giao_ca` - Chi tiết các chỉ số kinh doanh
```sql
- id: Khóa chính
- giao_ca_id: ID giao ca
- loai_chi_tiet: Loại chi tiết (DOANH_THU, DON_HANG, TIEN_MAT, SU_CO, CONG_VIEC)
- ten_chi_tiet: Tên chi tiết
- gia_tri: Giá trị
- so_luong: Số lượng
- mo_ta: Mô tả
```

#### 5. `lich_su_giao_ca` - Lịch sử thay đổi giao ca
```sql
- id: Khóa chính
- giao_ca_id: ID giao ca
- hanh_dong: Hành động (TAO_MOI, CAP_NHAT, XAC_NHAN, HUY)
- nguoi_thuc_hien_id: ID người thực hiện
- noi_dung_thay_doi: Nội dung thay đổi
- thoi_gian: Thời gian
```

#### 6. `cau_hinh_giao_ca` - Cấu hình hệ thống
```sql
- id: Khóa chính
- ten_cau_hinh: Tên cấu hình
- gia_tri: Giá trị
- mo_ta: Mô tả
- loai_cau_hinh: Loại cấu hình (TEXT, NUMBER, BOOLEAN, JSON)
```

## 🚀 API Endpoints

### Ca Làm Việc
```
GET    /api/ca                    - Lấy danh sách tất cả ca
GET    /api/ca/active             - Lấy danh sách ca đang hoạt động
GET    /api/ca/current            - Lấy ca hiện tại đang hoạt động
GET    /api/ca/{id}               - Lấy thông tin ca theo ID
POST   /api/ca                    - Tạo ca mới
PUT    /api/ca/{id}               - Cập nhật ca
DELETE /api/ca/{id}               - Xóa ca
GET    /api/ca/search?tenCa=...   - Tìm kiếm ca theo tên
```

### Giao Ca
```
GET    /api/giao-ca                           - Lấy danh sách giao ca
GET    /api/giao-ca/{id}                      - Lấy thông tin giao ca theo ID
POST   /api/giao-ca                           - Tạo giao ca mới
POST   /api/giao-ca/create                    - Tạo giao ca (business method)
PUT    /api/giao-ca/{id}                      - Cập nhật giao ca
PUT    /api/giao-ca/{id}/confirm              - Xác nhận giao ca
PUT    /api/giao-ca/{id}/cancel               - Hủy giao ca
DELETE /api/giao-ca/{id}                      - Xóa giao ca

# Query endpoints
GET    /api/giao-ca/phan-ca/{phanCaId}        - Lấy giao ca theo phân ca
GET    /api/giao-ca/nhan-vien-giao/{nhanVienId} - Lấy giao ca theo người giao
GET    /api/giao-ca/nhan-vien-nhan/{nhanVienId} - Lấy giao ca theo người nhận
GET    /api/giao-ca/trang-thai/{trangThai}    - Lấy giao ca theo trạng thái
GET    /api/giao-ca/nhan-vien/{nhanVienId}    - Lấy giao ca theo nhân viên
GET    /api/giao-ca/ca/{caId}                 - Lấy giao ca theo ca
GET    /api/giao-ca/date-range                - Lấy giao ca theo khoảng thời gian
GET    /api/giao-ca/nhan-vien/{nhanVienId}/pending - Lấy giao ca chờ xác nhận của nhân viên
GET    /api/giao-ca/nhan-vien/{nhanVienId}/pending-for - Lấy giao ca chờ nhân viên xác nhận

# Statistics endpoints
GET    /api/giao-ca/nhan-vien/{nhanVienId}/revenue - Tổng doanh thu theo nhân viên
GET    /api/giao-ca/ca/{caId}/revenue         - Tổng doanh thu theo ca
GET    /api/giao-ca/nhan-vien/{nhanVienId}/count - Đếm giao ca theo nhân viên
GET    /api/giao-ca/ca/{caId}/count           - Đếm giao ca theo ca
```

## 📊 Các Chỉ Số Kinh Doanh Được Theo Dõi

### 1. Doanh Thu
- Tổng doanh thu ca
- Doanh thu theo phương thức thanh toán (tiền mặt, chuyển khoản)
- So sánh doanh thu giữa các ca

### 2. Đơn Hàng
- Số lượng đơn hàng
- Số đơn hàng thanh toán tiền mặt
- Số đơn hàng thanh toán chuyển khoản
- Tỷ lệ hoàn thành đơn hàng

### 3. Tiền Mặt
- Số tiền đầu ca
- Số tiền cuối ca
- Số tiền thu thêm trong ca
- Số tiền chi ra trong ca
- Chênh lệch tiền mặt (tự động tính)

### 4. Sự Cố & Công Việc
- Sự cố bất thường xảy ra
- Công việc tồn đọng
- Báo cáo công việc chi tiết

## 🔧 Tính Năng Nổi Bật

### 1. Quản Lý Tiền Mặt Thông Minh
- Theo dõi tiền đầu ca, cuối ca
- Tự động tính toán chênh lệch
- Báo cáo thu chi chi tiết
- Cảnh báo khi có chênh lệch bất thường

### 2. Phân Ca Thông Minh
- Kiểm tra trùng lịch nhân viên
- Phân ca theo ngày/tuần/tháng
- Quản lý trạng thái ca real-time
- Thông báo ca sắp tới

### 3. Giao Ca An Toàn
- Xác nhận giao ca 2 chiều
- Theo dõi người giao/nhận
- Ghi chú chi tiết
- Lịch sử thay đổi đầy đủ

### 4. Báo Cáo & Thống Kê
- Báo cáo doanh thu theo ca/nhân viên
- Thống kê hiệu suất làm việc
- Phân tích xu hướng kinh doanh
- Export dữ liệu Excel/PDF

## 🛠️ Cài Đặt & Triển Khai

### 1. Yêu Cầu Hệ Thống
- Java 17+
- Spring Boot 3.x
- MySQL 8.0+
- Maven 3.6+

### 2. Cài Đặt Database
```bash
# Chạy migration để tạo bảng
mysql -u username -p database_name < src/main/resources/db/migration/V8__create_shift_management_tables.sql
```

### 3. Cấu Hình Application
```properties
# application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/phonix_store
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=validate
```

### 4. Chạy Ứng Dụng
```bash
mvn spring-boot:run
```

## 📱 Sử Dụng Hệ Thống

### 1. Thiết Lập Ban Đầu
1. Tạo các ca làm việc (Ca Sáng, Ca Chiều, Ca Tối, Ca Đêm)
2. Phân ca cho nhân viên theo lịch
3. Cấu hình các tham số hệ thống

### 2. Quy Trình Giao Ca Hàng Ngày

#### Bước 1: Nhân viên bắt đầu ca
```http
POST /api/giao-ca/create
{
  "phanCaId": 1,
  "nhanVienGiaoId": 5,
  "soTienDauCa": 1000000,
  "ghiChu": "Bắt đầu ca sáng"
}
```

#### Bước 2: Ghi nhận chỉ số trong ca
```http
POST /api/chi-tiet-giao-ca
{
  "giaoCaId": 1,
  "loaiChiTiet": "DOANH_THU",
  "tenChiTiet": "Bán iPhone 15",
  "giaTri": 25000000,
  "soLuong": 1
}
```

#### Bước 3: Kết thúc ca và xác nhận giao ca
```http
PUT /api/giao-ca/1/confirm?nhanVienNhanId=6
{
  "soTienCuoiCa": 1200000,
  "soTienThuThem": 200000,
  "soTienChiRa": 0,
  "tongDoanhThu": 25000000,
  "soDonHang": 1,
  "soDonHangThanhToanTienMat": 1,
  "soDonHangThanhToanChuyenKhoan": 0,
  "baoCaoCongViec": "Hoàn thành bán 1 iPhone 15",
  "suCoBatThuong": "Không có",
  "congViecTonDong": "Không có"
}
```

### 3. Theo Dõi & Báo Cáo
- Xem dashboard tổng quan
- Kiểm tra ca đang hoạt động
- Xem lịch sử giao ca
- Tạo báo cáo doanh thu

## 🔒 Bảo Mật

### 1. Xác Thực & Phân Quyền
- JWT token authentication
- Role-based access control
- API rate limiting

### 2. Kiểm Soát Dữ Liệu
- Validation đầy đủ
- Audit trail cho mọi thay đổi
- Backup dữ liệu định kỳ

### 3. Bảo Mật Giao Ca
- Xác nhận 2 chiều
- Mã hóa thông tin nhạy cảm
- Log chi tiết mọi hoạt động

## 🚀 Mở Rộng Tương Lai

### 1. Tính Năng Nâng Cao
- **Mobile App**: Ứng dụng di động cho nhân viên
- **Real-time Notifications**: Thông báo real-time
- **AI Analytics**: Phân tích dữ liệu thông minh
- **Integration**: Tích hợp với hệ thống POS

### 2. Báo Cáo Nâng Cao
- Báo cáo doanh thu theo thời gian thực
- Phân tích hiệu suất nhân viên
- Dự báo doanh thu
- Dashboard tương tác

### 3. Tự Động Hóa
- Tự động phân ca dựa trên lịch sử
- Cảnh báo tự động khi có bất thường
- Tự động tạo báo cáo
- Tích hợp với hệ thống chấm công

## 📞 Hỗ Trợ

Nếu có vấn đề hoặc cần hỗ trợ, vui lòng liên hệ:
- **Email**: support@phonixstore.com
- **Hotline**: 1900-xxxx
- **Documentation**: [Link to docs]

---

**PhoniX Store - Hệ Thống Quản Lý Giao Ca Thông Minh** 🏪📱








































