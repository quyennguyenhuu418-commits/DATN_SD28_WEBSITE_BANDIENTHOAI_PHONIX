-- =====================================================
-- HỆ THỐNG QUẢN LÝ GIAO CA - DATABASE SCHEMA
-- =====================================================

-- 1. Bảng CA - Quản lý các ca làm việc
CREATE TABLE ca (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_ca VARCHAR(20) NOT NULL UNIQUE,
    ten_ca NVARCHAR(100) NOT NULL,
    mo_ta NVARCHAR(500),
    gio_bat_dau TIME NOT NULL,
    gio_ket_thuc TIME NOT NULL,
    trang_thai INT DEFAULT 1, -- 1: Hoạt động, 0: Tạm dừng
    ngay_tao DATETIME2 DEFAULT GETDATE(),
    ngay_cap_nhat DATETIME2 DEFAULT GETDATE(),
    nguoi_tao NVARCHAR(255),
    nguoi_cap_nhat NVARCHAR(255)
);

-- 2. Bảng PHAN_CA - Phân ca làm việc cho nhân viên
CREATE TABLE phan_ca (
    id INT IDENTITY(1,1) PRIMARY KEY,
    nhan_vien_id INT NOT NULL,
    ca_id INT NOT NULL,
    ngay_lam_viec DATE NOT NULL,
    trang_thai INT DEFAULT 0, -- 0: Chưa bắt đầu, 1: Đang làm, 2: Đã kết thúc, 3: Vắng mặt
    gio_bat_dau_thuc_te DATETIME2,
    gio_ket_thuc_thuc_te DATETIME2,
    ghi_chu NVARCHAR(500),
    ngay_tao DATETIME2 DEFAULT GETDATE(),
    ngay_cap_nhat DATETIME2 DEFAULT GETDATE(),
    nguoi_tao NVARCHAR(255),
    nguoi_cap_nhat NVARCHAR(255),
    
    FOREIGN KEY (nhan_vien_id) REFERENCES nhan_vien(id),
    FOREIGN KEY (ca_id) REFERENCES ca(id),
    UNIQUE (nhan_vien_id, ca_id, ngay_lam_viec)
);

-- 3. Bảng GIAO_CA - Quản lý việc giao ca và bàn giao tiền mặt
CREATE TABLE giao_ca (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_giao_ca VARCHAR(50) NOT NULL UNIQUE,
    phan_ca_id INT NOT NULL,
    nhan_vien_giao_id INT NOT NULL,
    nhan_vien_nhan_id INT,
    ngay_giao_ca DATETIME2 NOT NULL,
    
    -- Thông tin tiền mặt
    so_tien_dau_ca DECIMAL(18,2) DEFAULT 0,
    so_tien_cuoi_ca DECIMAL(18,2) DEFAULT 0,
    so_tien_thu_them DECIMAL(18,2) DEFAULT 0,
    so_tien_chi_ra DECIMAL(18,2) DEFAULT 0,
    chenh_lech DECIMAL(18,2) DEFAULT 0,
    
    -- Thông tin doanh thu ca
    tong_doanh_thu DECIMAL(18,2) DEFAULT 0,
    so_don_hang INT DEFAULT 0,
    so_don_hang_thanh_toan_tien_mat INT DEFAULT 0,
    so_don_hang_thanh_toan_chuyen_khoan INT DEFAULT 0,
    
    -- Trạng thái giao ca
    trang_thai INT DEFAULT 0, -- 0: Chờ xác nhận, 1: Đã xác nhận, 2: Đã hủy
    thoi_gian_xac_nhan DATETIME2,
    
    -- Ghi chú và báo cáo
    ghi_chu NVARCHAR(1000),
    bao_cao_cong_viec NVARCHAR(2000),
    su_co_bat_thuong NVARCHAR(1000),
    cong_viec_ton_dong NVARCHAR(1000),
    
    ngay_tao DATETIME2 DEFAULT GETDATE(),
    ngay_cap_nhat DATETIME2 DEFAULT GETDATE(),
    nguoi_tao NVARCHAR(255),
    nguoi_cap_nhat NVARCHAR(255),
    
    FOREIGN KEY (phan_ca_id) REFERENCES phan_ca(id),
    FOREIGN KEY (nhan_vien_giao_id) REFERENCES nhan_vien(id),
    FOREIGN KEY (nhan_vien_nhan_id) REFERENCES nhan_vien(id)
);

-- 4. Bảng CHI_TIET_GIAO_CA - Chi tiết các chỉ số kinh doanh trong ca
CREATE TABLE chi_tiet_giao_ca (
    id INT IDENTITY(1,1) PRIMARY KEY,
    giao_ca_id INT NOT NULL,
    loai_chi_tiet VARCHAR(50) NOT NULL, -- DOANH_THU, DON_HANG, TIEN_MAT, SU_CO, CONG_VIEC
    ten_chi_tiet NVARCHAR(255) NOT NULL,
    gia_tri DECIMAL(18,2),
    so_luong INT,
    mo_ta NVARCHAR(500),
    thoi_gian DATETIME2,
    trang_thai INT DEFAULT 1,
    
    ngay_tao DATETIME2 DEFAULT GETDATE(),
    ngay_cap_nhat DATETIME2 DEFAULT GETDATE(),
    
    FOREIGN KEY (giao_ca_id) REFERENCES giao_ca(id)
);

-- 5. Bảng LICH_SU_GIAO_CA - Lưu trữ lịch sử các thay đổi giao ca
CREATE TABLE lich_su_giao_ca (
    id INT IDENTITY(1,1) PRIMARY KEY,
    giao_ca_id INT NOT NULL,
    hanh_dong VARCHAR(50) NOT NULL, -- TAO_MOI, CAP_NHAT, XAC_NHAN, HUY
    nguoi_thuc_hien_id INT NOT NULL,
    noi_dung_thay_doi NVARCHAR(1000),
    thoi_gian DATETIME2 DEFAULT GETDATE(),
    
    FOREIGN KEY (giao_ca_id) REFERENCES giao_ca(id),
    FOREIGN KEY (nguoi_thuc_hien_id) REFERENCES nhan_vien(id)
);

-- 6. Bảng CAU_HINH_GIAO_CA - Cấu hình hệ thống giao ca
CREATE TABLE cau_hinh_giao_ca (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ten_cau_hinh VARCHAR(100) NOT NULL UNIQUE,
    gia_tri NVARCHAR(500),
    mo_ta NVARCHAR(500),
    loai_cau_hinh VARCHAR(50), -- TEXT, NUMBER, BOOLEAN, JSON
    trang_thai INT DEFAULT 1,
    
    ngay_tao DATETIME2 DEFAULT GETDATE(),
    ngay_cap_nhat DATETIME2 DEFAULT GETDATE(),
    nguoi_tao NVARCHAR(255),
    nguoi_cap_nhat NVARCHAR(255)
);

-- =====================================================
-- TẠO INDEX ĐỂ TỐI ƯU HIỆU SUẤT
-- =====================================================

-- Index cho bảng phan_ca
CREATE INDEX IX_phan_ca_nhan_vien_ngay ON phan_ca(nhan_vien_id, ngay_lam_viec);
CREATE INDEX IX_phan_ca_ca_ngay ON phan_ca(ca_id, ngay_lam_viec);
CREATE INDEX IX_phan_ca_trang_thai ON phan_ca(trang_thai);

-- Index cho bảng giao_ca
CREATE INDEX IX_giao_ca_phan_ca ON giao_ca(phan_ca_id);
CREATE INDEX IX_giao_ca_nhan_vien_giao ON giao_ca(nhan_vien_giao_id);
CREATE INDEX IX_giao_ca_nhan_vien_nhan ON giao_ca(nhan_vien_nhan_id);
CREATE INDEX IX_giao_ca_ngay_giao ON giao_ca(ngay_giao_ca);
CREATE INDEX IX_giao_ca_trang_thai ON giao_ca(trang_thai);

-- Index cho bảng chi_tiet_giao_ca
CREATE INDEX IX_chi_tiet_giao_ca_giao_ca ON chi_tiet_giao_ca(giao_ca_id);
CREATE INDEX IX_chi_tiet_giao_ca_loai ON chi_tiet_giao_ca(loai_chi_tiet);

-- Index cho bảng lich_su_giao_ca
CREATE INDEX IX_lich_su_giao_ca_giao_ca ON lich_su_giao_ca(giao_ca_id);
CREATE INDEX IX_lich_su_giao_ca_thoi_gian ON lich_su_giao_ca(thoi_gian);

-- =====================================================
-- INSERT DỮ LIỆU MẪU
-- =====================================================

-- Thêm các ca làm việc mẫu
INSERT INTO ca (ma_ca, ten_ca, mo_ta, gio_bat_dau, gio_ket_thuc, nguoi_tao) VALUES
('CA_SANG', N'Ca Sáng', N'Ca làm việc buổi sáng', '07:00:00', '12:00:00', 'SYSTEM'),
('CA_CHIEU', N'Ca Chiều', N'Ca làm việc buổi chiều', '12:00:00', '17:00:00', 'SYSTEM'),
('CA_TOI', N'Ca Tối', N'Ca làm việc buổi tối', '17:00:00', '22:00:00', 'SYSTEM'),
('CA_DEM', N'Ca Đêm', N'Ca làm việc ban đêm', '22:00:00', '07:00:00', 'SYSTEM');

-- Thêm cấu hình mặc định
INSERT INTO cau_hinh_giao_ca (ten_cau_hinh, gia_tri, mo_ta, loai_cau_hinh, nguoi_tao) VALUES
('THOI_GIAN_CHO_XAC_NHAN', '30', N'Thời gian chờ xác nhận giao ca (phút)', 'NUMBER', 'SYSTEM'),
('SO_TIEN_CHENH_LECH_TOI_DA', '100000', N'Số tiền chênh lệch tối đa cho phép (VND)', 'NUMBER', 'SYSTEM'),
('GUI_EMAIL_THONG_BAO', 'true', N'Gửi email thông báo giao ca', 'BOOLEAN', 'SYSTEM'),
('THONG_BAO_CA_SAP_TOI', '15', N'Thông báo ca sắp tới (phút)', 'NUMBER', 'SYSTEM');


-- =====================================================
-- TẠO TRIGGER ĐỂ TỰ ĐỘNG TÍNH TOÁN CHÊNH LỆCH
-- =====================================================

CREATE TRIGGER TR_giao_ca_tinh_chenh_lech
ON giao_ca
AFTER INSERT, UPDATE
AS
BEGIN
    UPDATE giao_ca 
    SET chenh_lech = (so_tien_cuoi_ca - so_tien_dau_ca) - (so_tien_thu_them - so_tien_chi_ra)
    WHERE id IN (SELECT id FROM inserted);
END;

-- =====================================================
-- TẠO VIEW ĐỂ BÁO CÁO
-- =====================================================

-- View tổng quan giao ca
CREATE VIEW vw_tong_quan_giao_ca AS
SELECT 
    gc.id,
    gc.ma_giao_ca,
    nv_giao.ho_ten AS nhan_vien_giao,
    nv_nhan.ho_ten AS nhan_vien_nhan,
    c.ten_ca,
    pc.ngay_lam_viec,
    gc.ngay_giao_ca,
    gc.tong_doanh_thu,
    gc.so_don_hang,
    gc.so_tien_dau_ca,
    gc.so_tien_cuoi_ca,
    gc.chenh_lech,
    gc.trang_thai,
    CASE gc.trang_thai
        WHEN 0 THEN N'Chờ xác nhận'
        WHEN 1 THEN N'Đã xác nhận'
        WHEN 2 THEN N'Đã hủy'
        ELSE N'Không xác định'
    END AS trang_thai_text
FROM giao_ca gc
LEFT JOIN phan_ca pc ON gc.phan_ca_id = pc.id
LEFT JOIN ca c ON pc.ca_id = c.id
LEFT JOIN nhan_vien nv_giao ON gc.nhan_vien_giao_id = nv_giao.id
LEFT JOIN nhan_vien nv_nhan ON gc.nhan_vien_nhan_id = nv_nhan.id;

-- View báo cáo doanh thu theo ca
CREATE VIEW vw_bao_cao_doanh_thu_ca AS
SELECT 
    c.ten_ca,
    pc.ngay_lam_viec,
    COUNT(gc.id) AS so_lan_giao_ca,
    SUM(gc.tong_doanh_thu) AS tong_doanh_thu,
    SUM(gc.so_don_hang) AS tong_don_hang,
    AVG(gc.tong_doanh_thu) AS doanh_thu_trung_binh,
    SUM(gc.chenh_lech) AS tong_chenh_lech
FROM giao_ca gc
INNER JOIN phan_ca pc ON gc.phan_ca_id = pc.id
INNER JOIN ca c ON pc.ca_id = c.id
WHERE gc.trang_thai = 1
GROUP BY c.ten_ca, pc.ngay_lam_viec, c.id;

-- =====================================================
-- TẠO STORED PROCEDURE
-- =====================================================

-- Stored procedure tạo giao ca mới
CREATE PROCEDURE sp_tao_giao_ca_moi
    @phan_ca_id INT,
    @nhan_vien_giao_id INT,
    @so_tien_dau_ca DECIMAL(18,2) = 0,
    @ghi_chu NVARCHAR(1000) = NULL
AS
BEGIN
    DECLARE @ma_giao_ca VARCHAR(50) = 'GC' + FORMAT(GETDATE(), 'yyyyMMddHHmmss');
    
    INSERT INTO giao_ca (
        ma_giao_ca, phan_ca_id, nhan_vien_giao_id, 
        ngay_giao_ca, so_tien_dau_ca, ghi_chu, nguoi_tao
    )
    VALUES (
        @ma_giao_ca, @phan_ca_id, @nhan_vien_giao_id,
        GETDATE(), @so_tien_dau_ca, @ghi_chu, 'SYSTEM'
    );
    
    SELECT SCOPE_IDENTITY() AS giao_ca_id;
END;

-- Stored procedure xác nhận giao ca
CREATE PROCEDURE sp_xac_nhan_giao_ca
    @giao_ca_id INT,
    @nhan_vien_nhan_id INT,
    @so_tien_cuoi_ca DECIMAL(18,2),
    @so_tien_thu_them DECIMAL(18,2) = 0,
    @so_tien_chi_ra DECIMAL(18,2) = 0,
    @tong_doanh_thu DECIMAL(18,2) = 0,
    @so_don_hang INT = 0,
    @so_don_hang_tien_mat INT = 0,
    @so_don_hang_chuyen_khoan INT = 0,
    @bao_cao_cong_viec NVARCHAR(2000) = NULL,
    @su_co_bat_thuong NVARCHAR(1000) = NULL,
    @cong_viec_ton_dong NVARCHAR(1000) = NULL
AS
BEGIN
    UPDATE giao_ca 
    SET 
        nhan_vien_nhan_id = @nhan_vien_nhan_id,
        so_tien_cuoi_ca = @so_tien_cuoi_ca,
        so_tien_thu_them = @so_tien_thu_them,
        so_tien_chi_ra = @so_tien_chi_ra,
        tong_doanh_thu = @tong_doanh_thu,
        so_don_hang = @so_don_hang,
        so_don_hang_thanh_toan_tien_mat = @so_don_hang_tien_mat,
        so_don_hang_thanh_toan_chuyen_khoan = @so_don_hang_chuyen_khoan,
        bao_cao_cong_viec = @bao_cao_cong_viec,
        su_co_bat_thuong = @su_co_bat_thuong,
        cong_viec_ton_dong = @cong_viec_ton_dong,
        trang_thai = 1,
        thoi_gian_xac_nhan = GETDATE(),
        ngay_cap_nhat = GETDATE(),
        nguoi_cap_nhat = 'SYSTEM'
    WHERE id = @giao_ca_id;
    
    -- Ghi lịch sử
    INSERT INTO lich_su_giao_ca (giao_ca_id, hanh_dong, nguoi_thuc_hien_id, noi_dung_thay_doi)
    VALUES (@giao_ca_id, 'XAC_NHAN', @nhan_vien_nhan_id, N'Xác nhận giao ca thành công');
END;
