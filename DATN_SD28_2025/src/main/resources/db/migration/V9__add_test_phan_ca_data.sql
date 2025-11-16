-- =====================================================
-- THÊM DỮ LIỆU TEST CHO PHAN_CA
-- =====================================================

-- Thêm dữ liệu mẫu cho phan_ca (phân ca)
-- Lấy ID của nhân viên đầu tiên (giả sử có ít nhất 1 nhân viên)
DECLARE @nhan_vien_id INT = (SELECT TOP 1 id FROM nhan_vien ORDER BY id);
DECLARE @ca_sang_id INT = (SELECT id FROM ca WHERE ma_ca = 'CA_SANG');
DECLARE @ca_chieu_id INT = (SELECT id FROM ca WHERE ma_ca = 'CA_CHIEU');
DECLARE @ca_toi_id INT = (SELECT id FROM ca WHERE ma_ca = 'CA_TOI');

-- Kiểm tra xem có nhân viên và ca không
IF @nhan_vien_id IS NOT NULL AND @ca_sang_id IS NOT NULL
BEGIN
    -- Thêm phân ca cho hôm nay và ngày mai
    INSERT INTO phan_ca (nhan_vien_id, ca_id, ngay_lam_viec, trang_thai, nguoi_tao) VALUES
    -- Hôm nay
    (@nhan_vien_id, @ca_sang_id, CAST(GETDATE() AS DATE), 1, 'SYSTEM'), -- Ca sáng đang làm
    (@nhan_vien_id, @ca_chieu_id, CAST(GETDATE() AS DATE), 0, 'SYSTEM'), -- Ca chiều chưa bắt đầu
    (@nhan_vien_id, @ca_toi_id, CAST(GETDATE() AS DATE), 0, 'SYSTEM'), -- Ca tối chưa bắt đầu
    -- Ngày mai
    (@nhan_vien_id, @ca_sang_id, CAST(DATEADD(DAY, 1, GETDATE()) AS DATE), 0, 'SYSTEM'),
    (@nhan_vien_id, @ca_chieu_id, CAST(DATEADD(DAY, 1, GETDATE()) AS DATE), 0, 'SYSTEM'),
    (@nhan_vien_id, @ca_toi_id, CAST(DATEADD(DAY, 1, GETDATE()) AS DATE), 0, 'SYSTEM');
    
    PRINT 'Đã thêm dữ liệu test cho phan_ca thành công!';
END
ELSE
BEGIN
    PRINT 'Không tìm thấy nhân viên hoặc ca. Vui lòng kiểm tra dữ liệu.';
END;









































