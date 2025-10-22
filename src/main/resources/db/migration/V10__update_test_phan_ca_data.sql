-- =====================================================
-- CẬP NHẬT DỮ LIỆU TEST CHO PHAN_CA
-- =====================================================

-- Thêm một ca chưa bắt đầu (trang_thai = 0) cho hôm nay
-- Lấy ID của nhân viên và ca
DECLARE @nhan_vien_id INT = (SELECT TOP 1 id FROM nhan_vien ORDER BY id);
DECLARE @ca_chieu_id INT = (SELECT id FROM ca WHERE ma_ca = 'CA_CHIEU');
DECLARE @ca_toi_id INT = (SELECT id FROM ca WHERE ma_ca = 'CA_TOI');

-- Kiểm tra xem có nhân viên và ca không
IF @nhan_vien_id IS NOT NULL AND @ca_chieu_id IS NOT NULL
BEGIN
    -- Thêm ca chiều chưa bắt đầu (trang_thai = 0)
    INSERT INTO phan_ca (nhan_vien_id, ca_id, ngay_lam_viec, trang_thai, nguoi_tao) VALUES
    (@nhan_vien_id, @ca_chieu_id, CAST(GETDATE() AS DATE), 0, 'SYSTEM');
    
    -- Thêm ca tối chưa bắt đầu (trang_thai = 0)
    INSERT INTO phan_ca (nhan_vien_id, ca_id, ngay_lam_viec, trang_thai, nguoi_tao) VALUES
    (@nhan_vien_id, @ca_toi_id, CAST(GETDATE() AS DATE), 0, 'SYSTEM');
    
    PRINT 'Đã thêm ca chưa bắt đầu (trang_thai = 0) thành công!';
    
    -- Hiển thị dữ liệu để kiểm tra
    SELECT 
        pc.id,
        pc.nhan_vien_id,
        pc.ca_id,
        c.ten_ca,
        pc.ngay_lam_viec,
        pc.trang_thai,
        CASE pc.trang_thai
            WHEN 0 THEN N'Chưa bắt đầu'
            WHEN 1 THEN N'Đang làm'
            WHEN 2 THEN N'Đã kết thúc'
            WHEN 3 THEN N'Vắng mặt'
            ELSE N'Không xác định'
        END AS trang_thai_text,
        c.gio_bat_dau,
        c.gio_ket_thuc
    FROM phan_ca pc
    LEFT JOIN ca c ON pc.ca_id = c.id
    WHERE pc.ngay_lam_viec = CAST(GETDATE() AS DATE)
    ORDER BY c.gio_bat_dau;
END
ELSE
BEGIN
    PRINT 'Không tìm thấy nhân viên hoặc ca. Vui lòng kiểm tra dữ liệu.';
END;

























