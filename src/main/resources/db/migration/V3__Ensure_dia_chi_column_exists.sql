-- Ensure dia_chi column exists and has proper constraints
-- This migration is safe to run multiple times

-- Add dia_chi column if it doesn't exist
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'khach_hang' 
        AND column_name = 'dia_chi'
    ) THEN
        ALTER TABLE khach_hang ADD COLUMN dia_chi NVARCHAR(500);
    END IF;
END $$;

-- Update any NULL addresses with default value
UPDATE khach_hang 
SET dia_chi = 'Chưa cập nhật địa chỉ'
WHERE dia_chi IS NULL OR dia_chi = '';

-- Add more sample customers with diverse addresses
INSERT INTO khach_hang (ma_khach_hang, ho_ten, so_dien_thoai, email, gioi_tinh, dia_chi, ngay_tao, trang_thai, nguoi_tao)
SELECT * FROM (VALUES 
    ('KH031', 'Nguyễn Văn Long', '0901234577', 'long.nguyen@email.com', 'Nam', '123 Đường Võ Văn Kiệt, Phường Cầu Kho, Quận 1, TP.HCM', GETDATE(), 1, 'admin'),
    ('KH032', 'Trần Thị Mai', '0901234578', 'mai.tran@email.com', 'Nữ', '456 Đường Nguyễn Thị Nghĩa, Phường Tân Định, Quận 1, TP.HCM', GETDATE(), 1, 'admin'),
    ('KH033', 'Lê Văn Nam', '0901234579', 'nam.le@email.com', 'Nam', '789 Đường Điện Biên Phủ, Phường 25, Quận Bình Thạnh, TP.HCM', GETDATE(), 1, 'admin'),
    ('KH034', 'Phạm Thị Oanh', '0901234580', 'oanh.pham@email.com', 'Nữ', '321 Đường Xô Viết Nghệ Tĩnh, Phường 21, Quận Bình Thạnh, TP.HCM', GETDATE(), 1, 'admin'),
    ('KH035', 'Hoàng Văn Phúc', '0901234581', 'phuc.hoang@email.com', 'Nam', '654 Đường Nguyễn Văn Đậu, Phường 5, Quận Bình Thạnh, TP.HCM', GETDATE(), 1, 'admin'),
    ('KH036', 'Vũ Thị Quỳnh', '0901234582', 'quynh.vu@email.com', 'Nữ', '987 Đường Lê Quang Định, Phường 14, Quận Bình Thạnh, TP.HCM', GETDATE(), 1, 'admin'),
    ('KH037', 'Đặng Văn Rồng', '0901234583', 'rong.dang@email.com', 'Nam', '147 Đường Đinh Tiên Hoàng, Phường 1, Quận Bình Thạnh, TP.HCM', GETDATE(), 1, 'admin'),
    ('KH038', 'Bùi Thị Sương', '0901234584', 'suong.bui@email.com', 'Nữ', '258 Đường Phan Đăng Lưu, Phường 3, Quận Bình Thạnh, TP.HCM', GETDATE(), 1, 'admin'),
    ('KH039', 'Đinh Văn Tùng', '0901234585', 'tung.dinh@email.com', 'Nam', '369 Đường Nguyễn Hữu Cảnh, Phường 22, Quận Bình Thạnh, TP.HCM', GETDATE(), 1, 'admin'),
    ('KH040', 'Ngô Thị Uyên', '0901234586', 'uyen.ngo@email.com', 'Nữ', '741 Đường Ung Văn Khiêm, Phường 25, Quận Bình Thạnh, TP.HCM', GETDATE(), 1, 'admin')
) AS new_customers(ma_khach_hang, ho_ten, so_dien_thoai, email, gioi_tinh, dia_chi, ngay_tao, trang_thai, nguoi_tao)
WHERE NOT EXISTS (
    SELECT 1 FROM khach_hang 
    WHERE ma_khach_hang = new_customers.ma_khach_hang
);
