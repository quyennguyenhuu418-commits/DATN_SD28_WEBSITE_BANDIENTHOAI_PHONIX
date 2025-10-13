-- Fix dia_chi column encoding from VARCHAR to NVARCHAR
-- This migration fixes the encoding issue for Vietnamese characters

-- Check if column exists and is VARCHAR, then alter to NVARCHAR
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('khach_hang') AND name = 'dia_chi' AND system_type_id = 167)
BEGIN
    -- Column exists and is VARCHAR, alter to NVARCHAR
    ALTER TABLE khach_hang ALTER COLUMN dia_chi NVARCHAR(500);
END

-- Update existing data with proper Vietnamese encoding
UPDATE khach_hang 
SET dia_chi = CASE 
    WHEN id = 1 THEN N'123 Đường Lê Lợi, Phường Bến Nghé, Quận 1, TP.HCM'
    WHEN id = 2 THEN N'456 Đường Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP.HCM'
    WHEN id = 3 THEN N'789 Đường Đồng Khởi, Phường Bến Nghé, Quận 1, TP.HCM'
    WHEN id = 4 THEN N'321 Đường Pasteur, Phường 6, Quận 3, TP.HCM'
    WHEN id = 5 THEN N'654 Đường Võ Văn Tần, Phường 6, Quận 3, TP.HCM'
    WHEN id = 6 THEN N'987 Đường Lý Tự Trọng, Phường Bến Nghé, Quận 1, TP.HCM'
    WHEN id = 7 THEN N'147 Đường Nguyễn Thị Minh Khai, Phường 6, Quận 3, TP.HCM'
    WHEN id = 8 THEN N'258 Đường Cách Mạng Tháng 8, Phường 10, Quận 3, TP.HCM'
    WHEN id = 9 THEN N'369 Đường Lê Văn Sỹ, Phường 1, Quận Tân Bình, TP.HCM'
    WHEN id = 10 THEN N'741 Đường Cộng Hòa, Phường 4, Quận Tân Bình, TP.HCM'
    WHEN id = 11 THEN N'852 Đường Trường Chinh, Phường 12, Quận Tân Bình, TP.HCM'
    WHEN id = 12 THEN N'963 Đường Lạc Long Quân, Phường 1, Quận 11, TP.HCM'
    WHEN id = 13 THEN N'159 Đường Lý Thường Kiệt, Phường 7, Quận 10, TP.HCM'
    WHEN id = 14 THEN N'357 Đường Sư Vạn Hạnh, Phường 12, Quận 10, TP.HCM'
    WHEN id = 15 THEN N'468 Đường Nguyễn Oanh, Phường 17, Quận Gò Vấp, TP.HCM'
    WHEN id = 16 THEN N'579 Đường Quang Trung, Phường 10, Quận Gò Vấp, TP.HCM'
    WHEN id = 17 THEN N'680 Đường Nguyễn Văn Cừ, Phường 4, Quận 5, TP.HCM'
    WHEN id = 18 THEN N'791 Đường Trần Hưng Đạo, Phường 11, Quận 5, TP.HCM'
    WHEN id = 19 THEN N'802 Đường Nguyễn Trãi, Phường 7, Quận 5, TP.HCM'
    WHEN id = 20 THEN N'913 Đường Lê Hồng Phong, Phường 1, Quận 5, TP.HCM'
    ELSE N'Chưa cập nhật địa chỉ'
END
WHERE id <= 20;

-- Set default address for customers without addresses
UPDATE khach_hang 
SET dia_chi = N'Chưa cập nhật địa chỉ'
WHERE dia_chi IS NULL OR dia_chi = '';


