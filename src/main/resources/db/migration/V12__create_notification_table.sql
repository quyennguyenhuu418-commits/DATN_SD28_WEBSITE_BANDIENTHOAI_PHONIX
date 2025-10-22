-- Tạo bảng thông báo
CREATE TABLE thong_bao (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    tieu_de NVARCHAR(255) NOT NULL,
    noi_dung NTEXT,
    loai_thong_bao NVARCHAR(50) NOT NULL,
    trang_thai INT NOT NULL DEFAULT 0, -- 0: chưa đọc, 1: đã đọc
    ngay_tao DATETIME2 NOT NULL DEFAULT GETDATE(),
    ngay_cap_nhat DATETIME2,
    id_tham_chieu BIGINT, -- ID của đơn hàng, sản phẩm, etc.
    duong_dan NVARCHAR(500) -- Link đến trang chi tiết
);

-- Tạo index cho hiệu suất
CREATE INDEX IX_thong_bao_trang_thai ON thong_bao(trang_thai);
CREATE INDEX IX_thong_bao_ngay_tao ON thong_bao(ngay_tao);
CREATE INDEX IX_thong_bao_loai_thong_bao ON thong_bao(loai_thong_bao);

-- Thêm dữ liệu mẫu
INSERT INTO thong_bao (tieu_de, noi_dung, loai_thong_bao, trang_thai, ngay_tao, id_tham_chieu, duong_dan) VALUES
('Chào mừng đến với hệ thống', 'Hệ thống quản lý đã được khởi tạo thành công!', 'SYSTEM', 0, GETDATE(), NULL, NULL),
('Đơn hàng mới: HD001', 'Khách hàng Nguyễn Văn A đã đặt đơn hàng với tổng tiền 1,500,000 VNĐ', 'ORDER_NEW', 0, GETDATE(), 1, '/don-hang'),
('Sản phẩm mới: iPhone 15', 'Sản phẩm iPhone 15 đã được thêm vào danh mục Điện thoại', 'PRODUCT_NEW', 0, GETDATE(), 1, '/san-pham'),
('Khách hàng mới: Trần Thị B', 'Khách hàng Trần Thị B (tranthib@email.com) đã đăng ký tài khoản', 'CUSTOMER_NEW', 0, GETDATE(), 1, '/khach-hang'),
('Nhân viên mới: Lê Văn C', 'Nhân viên Lê Văn C (levanc@company.com) đã được thêm vào hệ thống với chức vụ Nhân viên bán hàng', 'EMPLOYEE_NEW', 0, GETDATE(), 1, '/nhan-vien');
