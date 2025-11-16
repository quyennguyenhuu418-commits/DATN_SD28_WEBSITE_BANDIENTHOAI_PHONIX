-- =====================================================
-- HỆ THỐNG QUẢN LÝ BẢO HÀNH - DATABASE SCHEMA
-- =====================================================

-- 1. Bảng PHIEU_BAO_HANH - Phiếu tiếp nhận bảo hành
CREATE TABLE phieu_bao_hanh (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_phieu VARCHAR(50) NOT NULL UNIQUE,
    
    -- Thông tin khách hàng
    id_khach_hang INT,
    ten_khach_hang NVARCHAR(255),
    so_dien_thoai VARCHAR(15),
    
    -- Thông tin sản phẩm
    id_san_pham INT,
    id_chi_tiet_san_pham INT,
    id_hoa_don INT, -- Hóa đơn mua hàng gốc
    ten_san_pham NVARCHAR(255),
    imei_serial VARCHAR(50),
    
    -- Tình trạng tiếp nhận
    mo_ta_loi_khach_hang NTEXT,
    mo_ta_loi_nhan_vien NTEXT,
    tinh_trang_vat_ly NTEXT, -- Trầy xước, cấn móp, v.v.
    phu_kien_di_kem NVARCHAR(500), -- Sạc, cáp, hộp, v.v.
    
    -- Đánh giá điều kiện bảo hành
    du_dieu_kien_bao_hanh BIT, -- true: đủ điều kiện, false: không đủ
    ly_do_khong_du_dieu_kien NTEXT,
    
    -- Hướng xử lý
    huong_xu_ly VARCHAR(50), -- "SUA_TAI_CUA_HANG", "GUI_TTBH_HANG"
    
    -- Kết quả xử lý
    noi_dung_sua_chua NTEXT,
    ghi_chu_ky_thuat_vien NTEXT,
    chi_phi_sua_chua DECIMAL(18,2),
    khach_da_thanh_toan DECIMAL(18,2),
    
    -- Thông tin TTBH hãng (nếu gửi)
    ttbh_hang NVARCHAR(255), -- Tên trung tâm bảo hành hãng
    ma_bao_hanh_hang VARCHAR(100), -- Mã bảo hành của hãng
    
    -- Nhân viên xử lý
    id_nhan_vien_tiep_nhan INT, -- Nhân viên tiếp nhận
    id_nhan_vien_ky_thuat INT, -- Kỹ thuật viên xử lý
    id_nhan_vien_tra_may INT, -- Nhân viên trả máy
    
    -- Thời gian
    ngay_nhan DATE,
    ngay_hen_tra_du_kien DATE,
    ngay_tra_thuc_te DATE,
    
    -- Trạng thái
    trang_thai INT DEFAULT 0, 
    -- 0: Mới tiếp nhận / Đang kiểm tra điều kiện
    -- 1: Đủ điều kiện bảo hành
    -- 2: Không đủ điều kiện bảo hành
    -- 3: Đang sửa chữa nội bộ
    -- 4: Đã gửi TTBH hãng
    -- 5: Đã nhận từ TTBH
    -- 6: Đang kiểm tra QC
    -- 7: Đã sửa xong
    -- 8: Đã trả khách
    -- 9: Hoàn tất
    
    ngay_tao DATETIME2 DEFAULT GETDATE(),
    ngay_cap_nhat DATETIME2 DEFAULT GETDATE(),
    nguoi_tao NVARCHAR(255),
    nguoi_cap_nhat NVARCHAR(255),
    
    FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id),
    FOREIGN KEY (id_san_pham) REFERENCES san_pham(id),
    FOREIGN KEY (id_chi_tiet_san_pham) REFERENCES chi_tiet_san_pham(id),
    FOREIGN KEY (id_hoa_don) REFERENCES hoa_don(id),
    FOREIGN KEY (id_nhan_vien_tiep_nhan) REFERENCES nhan_vien(id),
    FOREIGN KEY (id_nhan_vien_ky_thuat) REFERENCES nhan_vien(id),
    FOREIGN KEY (id_nhan_vien_tra_may) REFERENCES nhan_vien(id)
);

-- 2. Bảng LICH_SU_XU_LY_BAO_HANH - Lịch sử xử lý bảo hành
CREATE TABLE lich_su_xu_ly_bao_hanh (
    id INT IDENTITY(1,1) PRIMARY KEY,
    id_phieu_bao_hanh INT NOT NULL,
    thoi_gian DATETIME2 NOT NULL DEFAULT GETDATE(),
    id_nhan_vien_thuc_hien INT,
    ten_nhan_vien_thuc_hien NVARCHAR(255),
    hanh_dong VARCHAR(100),
    -- "TIEP_NHAN", "KIEM_TRA_DIEU_KIEN", "DU_DIEU_KIEN", "KHONG_DU_DIEU_KIEN",
    -- "CHAN_DOAN_LOI", "SUA_NOI_BO", "GUI_TTBH", "NHAN_TU_TTBH", 
    -- "KIEM_TRA_QC", "TRA_MAY", "HOAN_TAT"
    noi_dung_xu_ly NTEXT, -- Mô tả chi tiết hành động
    chi_phi_phat_sinh DECIMAL(18,2),
    linh_kien_thay_the NVARCHAR(500), -- Danh sách linh kiện đã thay thế
    ghi_chu NTEXT,
    trang_thai_truoc INT,
    trang_thai_sau INT,
    ngay_tao DATETIME2 DEFAULT GETDATE(),
    
    FOREIGN KEY (id_phieu_bao_hanh) REFERENCES phieu_bao_hanh(id),
    FOREIGN KEY (id_nhan_vien_thuc_hien) REFERENCES nhan_vien(id)
);

-- =====================================================
-- TẠO INDEX ĐỂ TỐI ƯU HIỆU SUẤT
-- =====================================================

-- Index cho bảng phieu_bao_hanh
CREATE INDEX IX_phieu_bao_hanh_khach_hang ON phieu_bao_hanh(id_khach_hang);
CREATE INDEX IX_phieu_bao_hanh_trang_thai ON phieu_bao_hanh(trang_thai);
CREATE INDEX IX_phieu_bao_hanh_ngay_nhan ON phieu_bao_hanh(ngay_nhan);
CREATE INDEX IX_phieu_bao_hanh_imei ON phieu_bao_hanh(imei_serial);
CREATE INDEX IX_phieu_bao_hanh_hoa_don ON phieu_bao_hanh(id_hoa_don);
CREATE INDEX IX_phieu_bao_hanh_nhan_vien_tiep_nhan ON phieu_bao_hanh(id_nhan_vien_tiep_nhan);

-- Index cho bảng lich_su_xu_ly_bao_hanh
CREATE INDEX IX_lich_su_bao_hanh_phieu ON lich_su_xu_ly_bao_hanh(id_phieu_bao_hanh);
CREATE INDEX IX_lich_su_bao_hanh_thoi_gian ON lich_su_xu_ly_bao_hanh(thoi_gian);
CREATE INDEX IX_lich_su_bao_hanh_nhan_vien ON lich_su_xu_ly_bao_hanh(id_nhan_vien_thuc_hien);

-- =====================================================
-- TẠO VIEW ĐỂ BÁO CÁO
-- =====================================================

-- View tổng quan phiếu bảo hành
CREATE VIEW vw_tong_quan_bao_hanh AS
SELECT 
    pbh.id,
    pbh.ma_phieu,
    pbh.ten_khach_hang,
    pbh.so_dien_thoai,
    pbh.ten_san_pham,
    pbh.imei_serial,
    pbh.ngay_nhan,
    pbh.ngay_hen_tra_du_kien,
    pbh.ngay_tra_thuc_te,
    pbh.trang_thai,
    CASE pbh.trang_thai
        WHEN 0 THEN N'Mới tiếp nhận'
        WHEN 1 THEN N'Đủ điều kiện bảo hành'
        WHEN 2 THEN N'Không đủ điều kiện'
        WHEN 3 THEN N'Đang sửa chữa nội bộ'
        WHEN 4 THEN N'Đã gửi TTBH hãng'
        WHEN 5 THEN N'Đã nhận từ TTBH'
        WHEN 6 THEN N'Đang kiểm tra QC'
        WHEN 7 THEN N'Đã sửa xong'
        WHEN 8 THEN N'Đã trả khách'
        WHEN 9 THEN N'Hoàn tất'
        ELSE N'Không xác định'
    END AS trang_thai_text,
    pbh.huong_xu_ly,
    CASE pbh.huong_xu_ly
        WHEN 'SUA_TAI_CUA_HANG' THEN N'Sửa tại cửa hàng'
        WHEN 'GUI_TTBH_HANG' THEN N'Gửi TTBH hãng'
        ELSE N'Chưa xác định'
    END AS huong_xu_ly_text,
    nv_tiep_nhan.ho_ten AS ten_nhan_vien_tiep_nhan,
    nv_ky_thuat.ho_ten AS ten_nhan_vien_ky_thuat,
    nv_tra_may.ho_ten AS ten_nhan_vien_tra_may,
    pbh.du_dieu_kien_bao_hanh,
    pbh.chi_phi_sua_chua,
    pbh.khach_da_thanh_toan
FROM phieu_bao_hanh pbh
LEFT JOIN nhan_vien nv_tiep_nhan ON pbh.id_nhan_vien_tiep_nhan = nv_tiep_nhan.id
LEFT JOIN nhan_vien nv_ky_thuat ON pbh.id_nhan_vien_ky_thuat = nv_ky_thuat.id
LEFT JOIN nhan_vien nv_tra_may ON pbh.id_nhan_vien_tra_may = nv_tra_may.id;

-- View thống kê bảo hành theo trạng thái
CREATE VIEW vw_thong_ke_bao_hanh AS
SELECT 
    trang_thai,
    CASE trang_thai
        WHEN 0 THEN N'Mới tiếp nhận'
        WHEN 1 THEN N'Đủ điều kiện bảo hành'
        WHEN 2 THEN N'Không đủ điều kiện'
        WHEN 3 THEN N'Đang sửa chữa nội bộ'
        WHEN 4 THEN N'Đã gửi TTBH hãng'
        WHEN 5 THEN N'Đã nhận từ TTBH'
        WHEN 6 THEN N'Đang kiểm tra QC'
        WHEN 7 THEN N'Đã sửa xong'
        WHEN 8 THEN N'Đã trả khách'
        WHEN 9 THEN N'Hoàn tất'
        ELSE N'Không xác định'
    END AS trang_thai_text,
    COUNT(*) AS so_luong,
    SUM(chi_phi_sua_chua) AS tong_chi_phi,
    SUM(khach_da_thanh_toan) AS tong_thanh_toan
FROM phieu_bao_hanh
GROUP BY trang_thai;

-- =====================================================
-- TẠO STORED PROCEDURE
-- =====================================================

-- Stored procedure tạo phiếu bảo hành mới
CREATE PROCEDURE sp_tao_phieu_bao_hanh_moi
    @id_khach_hang INT,
    @ten_khach_hang NVARCHAR(255),
    @so_dien_thoai VARCHAR(15),
    @id_san_pham INT = NULL,
    @id_chi_tiet_san_pham INT = NULL,
    @id_hoa_don INT = NULL,
    @ten_san_pham NVARCHAR(255),
    @imei_serial VARCHAR(50) = NULL,
    @mo_ta_loi_khach_hang NTEXT = NULL,
    @mo_ta_loi_nhan_vien NTEXT = NULL,
    @tinh_trang_vat_ly NTEXT = NULL,
    @phu_kien_di_kem NVARCHAR(500) = NULL,
    @ngay_hen_tra_du_kien DATE = NULL,
    @id_nhan_vien_tiep_nhan INT,
    @nguoi_tao NVARCHAR(255)
AS
BEGIN
    DECLARE @ma_phieu VARCHAR(50) = 'BH-' + FORMAT(GETDATE(), 'yyyyMMdd') + '-' + 
                                    RIGHT('0000' + CAST(ABS(CHECKSUM(NEWID())) % 10000 AS VARCHAR), 4);
    
    INSERT INTO phieu_bao_hanh (
        ma_phieu, id_khach_hang, ten_khach_hang, so_dien_thoai,
        id_san_pham, id_chi_tiet_san_pham, id_hoa_don, ten_san_pham, imei_serial,
        mo_ta_loi_khach_hang, mo_ta_loi_nhan_vien, tinh_trang_vat_ly, phu_kien_di_kem,
        ngay_nhan, ngay_hen_tra_du_kien, id_nhan_vien_tiep_nhan, trang_thai,
        nguoi_tao, nguoi_cap_nhat
    )
    VALUES (
        @ma_phieu, @id_khach_hang, @ten_khach_hang, @so_dien_thoai,
        @id_san_pham, @id_chi_tiet_san_pham, @id_hoa_don, @ten_san_pham, @imei_serial,
        @mo_ta_loi_khach_hang, @mo_ta_loi_nhan_vien, @tinh_trang_vat_ly, @phu_kien_di_kem,
        CAST(GETDATE() AS DATE), @ngay_hen_tra_du_kien, @id_nhan_vien_tiep_nhan, 0,
        @nguoi_tao, @nguoi_tao
    );
    
    DECLARE @phieu_id INT = SCOPE_IDENTITY();
    
    -- Tạo lịch sử tiếp nhận
    INSERT INTO lich_su_xu_ly_bao_hanh (
        id_phieu_bao_hanh, thoi_gian, id_nhan_vien_thuc_hien, 
        ten_nhan_vien_thuc_hien, hanh_dong, noi_dung_xu_ly, 
        trang_thai_truoc, trang_thai_sau
    )
    SELECT 
        @phieu_id, GETDATE(), @id_nhan_vien_tiep_nhan,
        nv.ho_ten, 'TIEP_NHAN', N'Tiếp nhận yêu cầu bảo hành',
        NULL, 0
    FROM nhan_vien nv WHERE nv.id = @id_nhan_vien_tiep_nhan;
    
    SELECT @phieu_id AS phieu_bao_hanh_id;
END;

