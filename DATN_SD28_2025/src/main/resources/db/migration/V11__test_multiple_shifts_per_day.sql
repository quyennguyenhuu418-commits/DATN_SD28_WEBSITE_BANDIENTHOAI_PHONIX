-- =====================================================
-- TEST: PHÂN NHIỀU CA TRONG 1 NGÀY CHO CÙNG 1 NHÂN VIÊN
-- =====================================================

-- Lấy ID của nhân viên và các ca
DECLARE @nhan_vien_id INT = (SELECT TOP 1 id FROM nhan_vien ORDER BY id);
DECLARE @ca_sang_id INT = (SELECT id FROM ca WHERE ma_ca = 'CA_SANG');
DECLARE @ca_chieu_id INT = (SELECT id FROM ca WHERE ma_ca = 'CA_CHIEU');
DECLARE @ca_toi_id INT = (SELECT id FROM ca WHERE ma_ca = 'CA_TOI');

-- Kiểm tra xem có nhân viên và ca không
IF @nhan_vien_id IS NOT NULL AND @ca_sang_id IS NOT NULL
BEGIN
    -- Xóa dữ liệu cũ để test
    DELETE FROM phan_ca WHERE nhan_vien_id = @nhan_vien_id AND ngay_lam_viec = CAST(GETDATE() AS DATE);
    
    -- Thêm phân ca cho cùng 1 nhân viên làm nhiều ca trong 1 ngày
    INSERT INTO phan_ca (nhan_vien_id, ca_id, ngay_lam_viec, trang_thai, nguoi_tao) VALUES
    -- Ca sáng (07:00-12:00) - đang làm
    (@nhan_vien_id, @ca_sang_id, CAST(GETDATE() AS DATE), 1, 'SYSTEM'),
    -- Ca chiều (12:00-17:00) - chưa bắt đầu  
    (@nhan_vien_id, @ca_chieu_id, CAST(GETDATE() AS DATE), 0, 'SYSTEM'),
    -- Ca tối (17:00-22:00) - chưa bắt đầu
    (@nhan_vien_id, @ca_toi_id, CAST(GETDATE() AS DATE), 0, 'SYSTEM');
    
    PRINT 'Đã thêm dữ liệu test: 1 nhân viên làm 3 ca trong 1 ngày!';
    
    -- Hiển thị kết quả
    SELECT 
        pc.id,
        pc.nhan_vien_id,
        nv.ho_ten AS nhan_vien_ten,
        pc.ca_id,
        c.ten_ca,
        c.gio_bat_dau,
        c.gio_ket_thuc,
        pc.ngay_lam_viec,
        pc.trang_thai,
        CASE pc.trang_thai
            WHEN 0 THEN N'Chưa bắt đầu'
            WHEN 1 THEN N'Đang làm'
            WHEN 2 THEN N'Đã kết thúc'
            WHEN 3 THEN N'Vắng mặt'
            ELSE N'Không xác định'
        END AS trang_thai_text
    FROM phan_ca pc
    LEFT JOIN nhan_vien nv ON pc.nhan_vien_id = nv.id
    LEFT JOIN ca c ON pc.ca_id = c.id
    WHERE pc.nhan_vien_id = @nhan_vien_id 
      AND pc.ngay_lam_viec = CAST(GETDATE() AS DATE)
    ORDER BY c.gio_bat_dau;
END
ELSE
BEGIN
    PRINT 'Không tìm thấy nhân viên hoặc ca. Vui lòng kiểm tra dữ liệu.';
END;









































