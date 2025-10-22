-- Script để kiểm tra và sửa thông tin nhan_vien_giao_id trong giao_ca
-- Kiểm tra dữ liệu hiện tại
SELECT 
    gc.id,
    gc.ma_giao_ca,
    gc.nhan_vien_giao_id,
    gc.nhan_vien_nhan_id,
    pc.nhan_vien_id as phan_ca_nhan_vien_id,
    nv_giao.ho_ten as nhan_vien_giao_ten,
    nv_nhan.ho_ten as nhan_vien_nhan_ten,
    nv_phan_ca.ho_ten as phan_ca_nhan_vien_ten
FROM giao_ca gc
LEFT JOIN phan_ca pc ON gc.phan_ca_id = pc.id
LEFT JOIN nhan_vien nv_giao ON gc.nhan_vien_giao_id = nv_giao.id
LEFT JOIN nhan_vien nv_nhan ON gc.nhan_vien_nhan_id = nv_nhan.id
LEFT JOIN nhan_vien nv_phan_ca ON pc.nhan_vien_id = nv_phan_ca.id
ORDER BY gc.ngay_giao_ca DESC 
LIMIT 10;

-- Cập nhật nhan_vien_giao_id dựa trên phan_ca nếu cần
-- (Chỉ chạy nếu nhan_vien_giao_id khác với phan_ca.nhan_vien_id)
UPDATE gc
SET gc.nhan_vien_giao_id = pc.nhan_vien_id
FROM giao_ca gc
JOIN phan_ca pc ON gc.phan_ca_id = pc.id
WHERE gc.nhan_vien_giao_id != pc.nhan_vien_id
AND gc.trang_thai = 1;  -- Chỉ cập nhật giao ca đã xác nhận

-- Kiểm tra kết quả sau khi cập nhật
SELECT 
    gc.id,
    gc.ma_giao_ca,
    gc.nhan_vien_giao_id,
    gc.nhan_vien_nhan_id,
    nv_giao.ho_ten as nhan_vien_giao_ten,
    nv_nhan.ho_ten as nhan_vien_nhan_ten
FROM giao_ca gc
LEFT JOIN nhan_vien nv_giao ON gc.nhan_vien_giao_id = nv_giao.id
LEFT JOIN nhan_vien nv_nhan ON gc.nhan_vien_nhan_id = nv_nhan.id
WHERE gc.trang_thai = 1
ORDER BY gc.ngay_giao_ca DESC 
LIMIT 5;
image.png