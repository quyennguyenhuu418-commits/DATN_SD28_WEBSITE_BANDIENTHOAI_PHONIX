-- Test API endpoint để kiểm tra doanh thu
-- Chạy script này sau khi restart backend

-- 1. Kiểm tra tổng doanh thu từ database
SELECT 
    'Database Total Revenue' as source,
    SUM(tong_tien_sau_giam) as total_revenue
FROM hoa_don 
WHERE trang_thai = 1;

-- 2. Kiểm tra doanh thu theo ngày để so sánh
SELECT 
    CAST(ngay_tao AS DATE) as date,
    COUNT(*) as order_count,
    SUM(tong_tien_sau_giam) as daily_revenue
FROM hoa_don 
WHERE trang_thai = 1
GROUP BY CAST(ngay_tao AS DATE)
ORDER BY date;

-- 3. Kiểm tra chi tiết hóa đơn
SELECT 
    ma_hoa_don,
    tong_tien as original_amount,
    tong_tien_sau_giam as final_amount,
    ngay_tao,
    trang_thai
FROM hoa_don 
WHERE trang_thai = 1
ORDER BY ngay_tao DESC;
