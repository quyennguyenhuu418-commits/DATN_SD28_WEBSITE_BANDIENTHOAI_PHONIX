-- Script để kiểm tra và sửa lỗi duplicate voucher usage
-- Chạy script này để kiểm tra và sửa các voucher đã bị đánh dấu sử dụng nhiều lần

-- 1. Kiểm tra các voucher đã bị giảm số lượng quá mức (có thể do duplicate usage)
SELECT 
    id,
    ma_phieu_giam_gia,
    so_luong_dung,
    trang_thai,
    ngay_cap_nhat
FROM phieu_giam_gia 
WHERE so_luong_dung < 0 
ORDER BY so_luong_dung ASC;

-- 2. Kiểm tra các voucher có số lượng = 0 nhưng vẫn đang active
SELECT 
    id,
    ma_phieu_giam_gia,
    so_luong_dung,
    trang_thai,
    ngay_cap_nhat
FROM phieu_giam_gia 
WHERE so_luong_dung = 0 AND trang_thai = 1;

-- 3. Sửa các voucher có số lượng âm (khôi phục về 0)
UPDATE phieu_giam_gia 
SET so_luong_dung = 0,
    trang_thai = 0,
    ngay_cap_nhat = NOW()
WHERE so_luong_dung < 0;

-- 4. Kiểm tra các hóa đơn có voucher được sử dụng trong khoảng thời gian gần đây
-- (để xác định các trường hợp có thể bị duplicate)
SELECT 
    hd.id,
    hd.ma_hoa_don,
    hd.id_phieu_giam_gia,
    pg.ma_phieu_giam_gia,
    hd.ngay_tao,
    hd.ghi_chu
FROM hoa_don hd
LEFT JOIN phieu_giam_gia pg ON hd.id_phieu_giam_gia = pg.id
WHERE hd.id_phieu_giam_gia IS NOT NULL 
    AND hd.ngay_tao >= DATE_SUB(NOW(), INTERVAL 7 DAY)
ORDER BY hd.ngay_tao DESC;

-- 5. Kiểm tra các hóa đơn có ghi chú chứa "VNPAY_RETURN_PROCESSING" hoặc "ZALOPAY_RETURN_PROCESSING"
-- (để xác định các đơn hàng được tạo từ payment return)
SELECT 
    id,
    ma_hoa_don,
    id_phieu_giam_gia,
    ghi_chu,
    ngay_tao
FROM hoa_don 
WHERE ghi_chu LIKE '%VNPAY_RETURN_PROCESSING%' 
   OR ghi_chu LIKE '%ZALOPAY_RETURN_PROCESSING%'
ORDER BY ngay_tao DESC;

-- 6. Thống kê tổng quan về voucher usage
SELECT 
    COUNT(*) as total_vouchers,
    SUM(CASE WHEN trang_thai = 1 THEN 1 ELSE 0 END) as active_vouchers,
    SUM(CASE WHEN trang_thai = 0 THEN 1 ELSE 0 END) as inactive_vouchers,
    SUM(CASE WHEN so_luong_dung <= 0 THEN 1 ELSE 0 END) as exhausted_vouchers,
    SUM(CASE WHEN so_luong_dung < 0 THEN 1 ELSE 0 END) as negative_quantity_vouchers
FROM phieu_giam_gia;

-- 7. Kiểm tra các voucher riêng tư có thể bị ảnh hưởng
SELECT 
    pg.id,
    pg.ma_phieu_giam_gia,
    pg.so_luong_dung,
    pg.trang_thai,
    COUNT(kgg.id) as assigned_customers
FROM phieu_giam_gia pg
LEFT JOIN khach_hang_giam_gia kgg ON pg.id = kgg.id_phieu_giam_gia
WHERE pg.rieng_tu = 1
GROUP BY pg.id, pg.ma_phieu_giam_gia, pg.so_luong_dung, pg.trang_thai
ORDER BY pg.so_luong_dung ASC;

-- 8. Backup script để khôi phục nếu cần
-- Lưu ý: Chỉ chạy khi cần thiết và đã backup dữ liệu
/*
-- Khôi phục số lượng voucher về giá trị ban đầu (cần điều chỉnh theo dữ liệu thực tế)
UPDATE phieu_giam_gia 
SET so_luong_dung = 10,  -- Thay đổi số lượng ban đầu tùy theo voucher
    trang_thai = 1,
    ngay_cap_nhat = NOW()
WHERE ma_phieu_giam_gia = 'VC_4';  -- Thay đổi mã voucher cụ thể
*/
