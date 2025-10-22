-- Fix Vietnamese encoding for all text columns in all tables
-- This migration converts VARCHAR columns to NVARCHAR for proper Unicode support

-- Fix khach_hang table
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('khach_hang') AND name = 'ho_ten' AND system_type_id = 167)
BEGIN
    ALTER TABLE khach_hang ALTER COLUMN ho_ten NVARCHAR(255) NOT NULL;
END

IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('khach_hang') AND name = 'gioi_tinh' AND system_type_id = 167)
BEGIN
    ALTER TABLE khach_hang ALTER COLUMN gioi_tinh NVARCHAR(10);
END

IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('khach_hang') AND name = 'dia_chi' AND system_type_id = 167)
BEGIN
    ALTER TABLE khach_hang ALTER COLUMN dia_chi NVARCHAR(500);
END

-- Fix nhan_vien table
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('nhan_vien') AND name = 'ho_ten' AND system_type_id = 167)
BEGIN
    ALTER TABLE nhan_vien ALTER COLUMN ho_ten NVARCHAR(255);
END

IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('nhan_vien') AND name = 'gioi_tinh' AND system_type_id = 167)
BEGIN
    ALTER TABLE nhan_vien ALTER COLUMN gioi_tinh NVARCHAR(10);
END

IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('nhan_vien') AND name = 'dia_chi' AND system_type_id = 167)
BEGIN
    ALTER TABLE nhan_vien ALTER COLUMN dia_chi NVARCHAR(500);
END

IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('nhan_vien') AND name = 'chuc_vu' AND system_type_id = 167)
BEGIN
    ALTER TABLE nhan_vien ALTER COLUMN chuc_vu NVARCHAR(100);
END

-- Update existing data with proper Vietnamese encoding
UPDATE khach_hang 
SET gioi_tinh = CASE 
    WHEN gioi_tinh = 'Nam' OR gioi_tinh = 'nam' THEN N'Nam'
    WHEN gioi_tinh = 'Nữ' OR gioi_tinh = 'nữ' OR gioi_tinh = 'Nu' OR gioi_tinh = 'nu' THEN N'Nữ'
    WHEN gioi_tinh = 'Khác' OR gioi_tinh = 'khác' THEN N'Khác'
    ELSE N'Nam' -- Default to Nam if unclear
END
WHERE gioi_tinh IS NOT NULL;

-- Update ho_ten with proper encoding (if needed)
UPDATE khach_hang 
SET ho_ten = N'Nguyễn Văn An'
WHERE id = 1 AND ho_ten LIKE '%Nguyen%';

UPDATE khach_hang 
SET ho_ten = N'Trần Thị Bình'
WHERE id = 2 AND ho_ten LIKE '%Tran%';

UPDATE khach_hang 
SET ho_ten = N'Lê Văn Cường'
WHERE id = 3 AND ho_ten LIKE '%Le%';

-- Add more sample customers with proper Vietnamese names
INSERT INTO khach_hang (ma_khach_hang, ho_ten, so_dien_thoai, gioi_tinh, email, dia_chi, ngay_tao, trang_thai)
SELECT 
    N'KH' + RIGHT('000' + CAST(ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) + 20 AS VARCHAR), 3),
    N'Nguyễn Thị ' + CHAR(65 + (ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) % 26)) + CHAR(97 + (ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) % 26)),
    N'090' + RIGHT('0000000' + CAST(1000000 + ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) AS VARCHAR), 7),
    CASE WHEN ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) % 2 = 0 THEN N'Nữ' ELSE N'Nam' END,
    N'customer' + CAST(ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) + 20 AS VARCHAR) + N'@email.com',
    N'Địa chỉ mẫu ' + CAST(ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) + 20 AS VARCHAR) + N', TP.HCM',
    GETDATE(),
    1
FROM (SELECT 1 as n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) t
WHERE NOT EXISTS (SELECT 1 FROM khach_hang WHERE ma_khach_hang LIKE 'KH%' AND LEN(ma_khach_hang) = 5);
