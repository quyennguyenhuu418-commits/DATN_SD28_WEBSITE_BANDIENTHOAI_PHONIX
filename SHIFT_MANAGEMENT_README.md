# Hệ Thống Quản Lý Ca Làm Việc

## Tổng Quan
Hệ thống quản lý ca làm việc cho phép:
- Quản lý các ca làm việc (Ca Sáng, Ca Chiều, Ca Tối, Ca Đêm)
- Phân ca làm việc cho nhân viên
- Giao ca và bàn giao tiền mặt
- Theo dõi tình hình ca làm việc

## Cấu Trúc Database

### 1. Bảng `ca`
- Quản lý thông tin các ca làm việc
- Các trường: id, ten_ca, mo_ta, gio_bat_dau, gio_ket_thuc, trang_thai

### 2. Bảng `phan_ca`
- Quản lý việc phân ca cho nhân viên
- Các trường: id, nhan_vien_id, ca_id, ngay_lam_viec, trang_thai, ghi_chu

### 3. Bảng `giao_ca`
- Quản lý việc giao ca và bàn giao tiền mặt
- Các trường: id, ca_id, nhan_vien_giao_id, nhan_vien_nhan_id, ngay_giao_ca, so_tien_*, chenh_lech, ghi_chu, trang_thai

## API Endpoints

### Ca Làm Việc
- `GET /api/ca` - Lấy danh sách tất cả ca
- `GET /api/ca/active` - Lấy danh sách ca đang hoạt động
- `GET /api/ca/{id}` - Lấy thông tin ca theo ID
- `POST /api/ca` - Tạo ca mới
- `PUT /api/ca/{id}` - Cập nhật ca
- `DELETE /api/ca/{id}` - Xóa ca

### Giao Ca
- `GET /api/giao-ca` - Lấy danh sách giao ca
- `GET /api/giao-ca/ca/{caId}` - Lấy giao ca theo ca
- `GET /api/giao-ca/nhan-vien-giao/{nhanVienId}` - Lấy giao ca theo người giao
- `GET /api/giao-ca/nhan-vien-nhan/{nhanVienId}` - Lấy giao ca theo người nhận
- `POST /api/giao-ca` - Tạo giao ca mới
- `PUT /api/giao-ca/{id}/confirm` - Xác nhận giao ca
- `PUT /api/giao-ca/{id}/cancel` - Hủy giao ca

### Phân Ca
- `GET /api/phan-ca` - Lấy danh sách phân ca
- `GET /api/phan-ca/nhan-vien/{nhanVienId}` - Lấy phân ca theo nhân viên
- `GET /api/phan-ca/ca/{caId}` - Lấy phân ca theo ca
- `POST /api/phan-ca` - Tạo phân ca mới
- `PUT /api/phan-ca/{id}` - Cập nhật phân ca
- `DELETE /api/phan-ca/{id}` - Xóa phân ca
- `PUT /api/phan-ca/{id}/start` - Bắt đầu ca
- `PUT /api/phan-ca/{id}/end` - Kết thúc ca

## Frontend Pages

### 1. Dashboard Ca Làm Việc (`/ca-dashboard`)
- Tổng quan tình hình ca làm việc
- Thống kê số liệu
- Ca đang hoạt động
- Giao ca gần đây
- Thao tác nhanh

### 2. Quản Lý Ca (`/ca`)
- Danh sách ca làm việc
- Thêm/sửa/xóa ca
- Tìm kiếm và lọc ca

### 3. Giao Ca (`/giao-ca`)
- Danh sách giao ca
- Thực hiện giao ca mới
- Xác nhận/hủy giao ca
- Theo dõi tiền mặt

### 4. Phân Ca (`/phan-ca`)
- Danh sách phân ca
- Phân ca cho nhân viên
- Bắt đầu/kết thúc ca
- Quản lý lịch làm việc

## Cách Sử Dụng

### 1. Thiết Lập Ban Đầu
1. Chạy file SQL `create-shift-tables.sql` để tạo bảng
2. Thêm dữ liệu ca làm việc mẫu
3. Cấu hình nhân viên trong hệ thống

### 2. Quản Lý Ca Làm Việc
1. Vào trang `/ca` để quản lý ca
2. Thêm ca mới với thời gian bắt đầu/kết thúc
3. Thiết lập trạng thái hoạt động

### 3. Phân Ca Cho Nhân Viên
1. Vào trang `/phan-ca`
2. Chọn nhân viên và ca làm việc
3. Chọn ngày làm việc
4. Lưu phân ca

### 4. Giao Ca
1. Vào trang `/giao-ca`
2. Chọn ca và người nhận
3. Nhập thông tin tiền mặt
4. Thực hiện giao ca

### 5. Theo Dõi
1. Vào trang `/ca-dashboard` để xem tổng quan
2. Kiểm tra ca đang hoạt động
3. Xem lịch sử giao ca

## Tính Năng Nổi Bật

### 1. Quản Lý Tiền Mặt
- Theo dõi tiền đầu ca, cuối ca
- Tính toán chênh lệch
- Báo cáo thu chi

### 2. Phân Ca Thông Minh
- Kiểm tra trùng lịch
- Phân ca theo ngày
- Quản lý trạng thái ca

### 3. Giao Ca An Toàn
- Xác nhận giao ca
- Theo dõi người giao/nhận
- Ghi chú chi tiết

### 4. Dashboard Tổng Quan
- Thống kê real-time
- Ca đang hoạt động
- Báo cáo nhanh

## Lưu Ý Kỹ Thuật

### 1. Database
- Sử dụng foreign key constraints
- Index cho các trường tìm kiếm
- Unique constraint cho phân ca

### 2. API
- Validation đầy đủ
- Error handling
- Logging chi tiết

### 3. Frontend
- Responsive design
- Real-time updates
- User-friendly interface

## Mở Rộng Tương Lai

1. **Báo Cáo Nâng Cao**
   - Báo cáo doanh thu theo ca
   - Phân tích hiệu suất nhân viên
   - Thống kê chi tiết

2. **Tích Hợp POS**
   - Đồng bộ với hệ thống bán hàng
   - Tự động tính toán tiền mặt
   - Báo cáo doanh thu

3. **Thông Báo**
   - Nhắc nhở giao ca
   - Thông báo ca sắp tới
   - Cảnh báo bất thường

4. **Mobile App**
   - Ứng dụng di động
   - Giao ca nhanh
   - Theo dõi real-time

## Hỗ Trợ

Nếu có vấn đề hoặc cần hỗ trợ, vui lòng liên hệ team phát triển.

