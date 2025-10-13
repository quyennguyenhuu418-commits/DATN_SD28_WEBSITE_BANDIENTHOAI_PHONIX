-- Add sample addresses to existing customers who don't have addresses yet
UPDATE khach_hang 
SET dia_chi = CASE 
    WHEN id = 1 AND (dia_chi IS NULL OR dia_chi = '') THEN '123 Đường Lê Lợi, Phường Bến Nghé, Quận 1, TP.HCM'
    WHEN id = 2 AND (dia_chi IS NULL OR dia_chi = '') THEN '456 Đường Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP.HCM'
    WHEN id = 3 AND (dia_chi IS NULL OR dia_chi = '') THEN '789 Đường Đồng Khởi, Phường Bến Nghé, Quận 1, TP.HCM'
    WHEN id = 4 AND (dia_chi IS NULL OR dia_chi = '') THEN '321 Đường Pasteur, Phường 6, Quận 3, TP.HCM'
    WHEN id = 5 AND (dia_chi IS NULL OR dia_chi = '') THEN '654 Đường Võ Văn Tần, Phường 6, Quận 3, TP.HCM'
    WHEN id = 6 AND (dia_chi IS NULL OR dia_chi = '') THEN '987 Đường Lý Tự Trọng, Phường Bến Nghé, Quận 1, TP.HCM'
    WHEN id = 7 AND (dia_chi IS NULL OR dia_chi = '') THEN '147 Đường Nguyễn Thị Minh Khai, Phường 6, Quận 3, TP.HCM'
    WHEN id = 8 AND (dia_chi IS NULL OR dia_chi = '') THEN '258 Đường Cách Mạng Tháng 8, Phường 10, Quận 3, TP.HCM'
    WHEN id = 9 AND (dia_chi IS NULL OR dia_chi = '') THEN '369 Đường Lê Văn Sỹ, Phường 1, Quận Tân Bình, TP.HCM'
    WHEN id = 10 AND (dia_chi IS NULL OR dia_chi = '') THEN '741 Đường Cộng Hòa, Phường 4, Quận Tân Bình, TP.HCM'
    WHEN id = 11 AND (dia_chi IS NULL OR dia_chi = '') THEN '852 Đường Trường Chinh, Phường 12, Quận Tân Bình, TP.HCM'
    WHEN id = 12 AND (dia_chi IS NULL OR dia_chi = '') THEN '963 Đường Lạc Long Quân, Phường 1, Quận 11, TP.HCM'
    WHEN id = 13 AND (dia_chi IS NULL OR dia_chi = '') THEN '159 Đường Lý Thường Kiệt, Phường 7, Quận 10, TP.HCM'
    WHEN id = 14 AND (dia_chi IS NULL OR dia_chi = '') THEN '357 Đường Sư Vạn Hạnh, Phường 12, Quận 10, TP.HCM'
    WHEN id = 15 AND (dia_chi IS NULL OR dia_chi = '') THEN '468 Đường Nguyễn Oanh, Phường 17, Quận Gò Vấp, TP.HCM'
    WHEN id = 16 AND (dia_chi IS NULL OR dia_chi = '') THEN '579 Đường Quang Trung, Phường 10, Quận Gò Vấp, TP.HCM'
    WHEN id = 17 AND (dia_chi IS NULL OR dia_chi = '') THEN '680 Đường Nguyễn Văn Cừ, Phường 4, Quận 5, TP.HCM'
    WHEN id = 18 AND (dia_chi IS NULL OR dia_chi = '') THEN '791 Đường Trần Hưng Đạo, Phường 11, Quận 5, TP.HCM'
    WHEN id = 19 AND (dia_chi IS NULL OR dia_chi = '') THEN '802 Đường Nguyễn Trãi, Phường 7, Quận 5, TP.HCM'
    WHEN id = 20 AND (dia_chi IS NULL OR dia_chi = '') THEN '913 Đường Lê Hồng Phong, Phường 1, Quận 5, TP.HCM'
    ELSE dia_chi
END
WHERE id <= 20;

-- Set default address for customers without addresses
UPDATE khach_hang 
SET dia_chi = 'Chưa cập nhật địa chỉ'
WHERE dia_chi IS NULL OR dia_chi = '';


