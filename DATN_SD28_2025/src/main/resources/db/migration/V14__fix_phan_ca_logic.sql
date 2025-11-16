-- =====================================================
-- SỬA LẠI LOGIC PHÂN CA ĐỂ ĐÚNG VỚI HỆ THỐNG
-- =====================================================

-- 1. Reset các phân ca có trạng thái không hợp lý về "Chưa bắt đầu"
-- Logic: Nếu trang_thai = 1 (Đang làm) nhưng gio_bat_dau_thuc_te IS NULL 
--        => Reset về trang_thai = 0 (Chưa bắt đầu)

UPDATE phan_ca
SET trang_thai = 0,
    gio_bat_dau_thuc_te = NULL,
    gio_ket_thuc_thuc_te = NULL,
    nguoi_cap_nhat = 'SYSTEM',
    ngay_cap_nhat = GETDATE()
WHERE trang_thai = 1 
  AND gio_bat_dau_thuc_te IS NULL;

PRINT 'Đã reset ' + CAST(@@ROWCOUNT AS VARCHAR) + ' phân ca về trạng thái "Chưa bắt đầu" (trang_thai = 0)';

-- 2. Reset các phân ca có trang_thai = 2 (Đã kết thúc) nhưng gio_ket_thuc_thuc_te IS NULL
--    => Reset về trang_thai = 0 (Chưa bắt đầu)

UPDATE pc
SET pc.trang_thai = 0,
    pc.gio_bat_dau_thuc_te = NULL,
    pc.gio_ket_thuc_thuc_te = NULL,
    pc.nguoi_cap_nhat = 'SYSTEM',
    pc.ngay_cap_nhat = GETDATE()
FROM phan_ca pc
WHERE pc.trang_thai = 2 
  AND pc.gio_ket_thuc_thuc_te IS NULL;

PRINT 'Đã reset ' + CAST(@@ROWCOUNT AS VARCHAR) + ' phân ca "Đã kết thúc" không hợp lý về trạng thái "Chưa bắt đầu"';

-- 3. Đảm bảo các phân ca hôm nay có trang_thai = 0 nếu chưa bắt đầu
--    (Chỉ áp dụng cho các phân ca của ngày hôm nay)

UPDATE phan_ca
SET trang_thai = 0,
    gio_bat_dau_thuc_te = NULL,
    gio_ket_thuc_thuc_te = NULL,
    nguoi_cap_nhat = 'SYSTEM',
    ngay_cap_nhat = GETDATE()
WHERE ngay_lam_viec = CAST(GETDATE() AS DATE)
  AND trang_thai = 1
  AND gio_bat_dau_thuc_te IS NULL;

PRINT 'Đã reset các phân ca hôm nay về trạng thái "Chưa bắt đầu"';

-- 4. Hiển thị kết quả để kiểm tra
SELECT 
    pc.id,
    pc.nhan_vien_id,
    nv.ho_ten AS nhan_vien,
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
    END AS trang_thai_text,
    pc.gio_bat_dau_thuc_te,
    pc.gio_ket_thuc_thuc_te,
    pc.nguoi_tao,
    pc.nguoi_cap_nhat
FROM phan_ca pc
LEFT JOIN nhan_vien nv ON pc.nhan_vien_id = nv.id
LEFT JOIN ca c ON pc.ca_id = c.id
WHERE pc.ngay_lam_viec >= CAST(GETDATE() AS DATE)
ORDER BY pc.ngay_lam_viec DESC, c.gio_bat_dau;

PRINT 'Migration hoàn tất!';
