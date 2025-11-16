-- Migration: Thêm cột ma_hex vào bảng mau_sac
-- Mô tả: Entity MauSac có field maHex nhưng database thiếu cột này

-- Kiểm tra và thêm cột ma_hex nếu chưa tồn tại
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'mau_sac' AND COLUMN_NAME = 'ma_hex'
)
BEGIN
    ALTER TABLE mau_sac 
    ADD ma_hex VARCHAR(7) NULL;
    
    PRINT 'Đã thêm cột ma_hex vào bảng mau_sac';
END
ELSE
BEGIN
    PRINT 'Cột ma_hex đã tồn tại trong bảng mau_sac';
END







