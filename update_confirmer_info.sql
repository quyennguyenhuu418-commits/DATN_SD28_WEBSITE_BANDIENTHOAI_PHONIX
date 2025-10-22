-- Script để cập nhật thông tin người xác nhận cho các giao ca hiện có
-- Cập nhật nguoiCapNhat từ 'SYSTEM' thành tên thực tế của nhân viên

UPDATE gc
SET gc.nguoi_cap_nhat = nv.ho_ten
FROM giao_ca gc
JOIN nhan_vien nv ON gc.nhan_vien_nhan_id = nv.id
WHERE gc.trang_thai = 1  -- Chỉ cập nhật giao ca đã xác nhận
AND gc.nguoi_cap_nhat = 'SYSTEM';

-- Kiểm tra kết quả
SELECT 
    gc.id,
    gc.ma_giao_ca,
    gc.nhan_vien_nhan_id,
    gc.nguoi_cap_nhat,
    nv.ho_ten as nhan_vien_ten,
    gc.thoi_gian_xac_nhan
FROM giao_ca gc
LEFT JOIN nhan_vien nv ON gc.nhan_vien_nhan_id = nv.id
WHERE gc.trang_thai = 1
ORDER BY gc.ngay_giao_ca DESC 
LIMIT 5;
