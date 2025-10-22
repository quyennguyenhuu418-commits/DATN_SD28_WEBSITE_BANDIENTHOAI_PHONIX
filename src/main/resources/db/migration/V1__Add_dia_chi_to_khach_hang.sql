-- Add dia_chi column to khach_hang table
ALTER TABLE khach_hang ADD COLUMN dia_chi NVARCHAR(500);

-- Update existing customers with sample addresses
UPDATE khach_hang 
SET dia_chi = CASE 
    WHEN id = 1 THEN '123 Đường Lê Lợi, Phường Bến Nghé, Quận 1, TP.HCM'
    WHEN id = 2 THEN '456 Đường Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP.HCM'
    WHEN id = 3 THEN '789 Đường Đồng Khởi, Phường Bến Nghé, Quận 1, TP.HCM'
    WHEN id = 4 THEN '321 Đường Pasteur, Phường 6, Quận 3, TP.HCM'
    WHEN id = 5 THEN '654 Đường Võ Văn Tần, Phường 6, Quận 3, TP.HCM'
    WHEN id = 6 THEN '987 Đường Lý Tự Trọng, Phường Bến Nghé, Quận 1, TP.HCM'
    WHEN id = 7 THEN '147 Đường Nguyễn Thị Minh Khai, Phường 6, Quận 3, TP.HCM'
    WHEN id = 8 THEN '258 Đường Cách Mạng Tháng 8, Phường 10, Quận 3, TP.HCM'
    WHEN id = 9 THEN '369 Đường Lê Văn Sỹ, Phường 1, Quận Tân Bình, TP.HCM'
    WHEN id = 10 THEN '741 Đường Cộng Hòa, Phường 4, Quận Tân Bình, TP.HCM'
    WHEN id = 11 THEN '852 Đường Trường Chinh, Phường 12, Quận Tân Bình, TP.HCM'
    WHEN id = 12 THEN '963 Đường Lạc Long Quân, Phường 1, Quận 11, TP.HCM'
    WHEN id = 13 THEN '159 Đường Lý Thường Kiệt, Phường 7, Quận 10, TP.HCM'
    WHEN id = 14 THEN '357 Đường Sư Vạn Hạnh, Phường 12, Quận 10, TP.HCM'
    WHEN id = 15 THEN '468 Đường Nguyễn Oanh, Phường 17, Quận Gò Vấp, TP.HCM'
    WHEN id = 16 THEN '579 Đường Quang Trung, Phường 10, Quận Gò Vấp, TP.HCM'
    WHEN id = 17 THEN '680 Đường Nguyễn Văn Cừ, Phường 4, Quận 5, TP.HCM'
    WHEN id = 18 THEN '791 Đường Trần Hưng Đạo, Phường 11, Quận 5, TP.HCM'
    WHEN id = 19 THEN '802 Đường Nguyễn Trãi, Phường 7, Quận 5, TP.HCM'
    WHEN id = 20 THEN '913 Đường Lê Hồng Phong, Phường 1, Quận 5, TP.HCM'
    ELSE 'Chưa cập nhật địa chỉ'
END
WHERE id <= 20;

-- Add more sample customers with addresses if needed
INSERT INTO khach_hang (ma_khach_hang, ho_ten, so_dien_thoai, email, gioi_tinh, dia_chi, ngay_tao, trang_thai, nguoi_tao)
VALUES 
('KH021', 'Nguyễn Văn An', '0901234567', 'an.nguyen@email.com', 'Nam', '123 Đường 3/2, Phường 12, Quận 10, TP.HCM', GETDATE(), 1, 'admin'),
('KH022', 'Trần Thị Bình', '0901234568', 'binh.tran@email.com', 'Nữ', '456 Đường Lý Thái Tổ, Phường 1, Quận 10, TP.HCM', GETDATE(), 1, 'admin'),
('KH023', 'Lê Văn Cường', '0901234569', 'cuong.le@email.com', 'Nam', '789 Đường Nguyễn Thị Định, Phường Thạnh Mỹ Lợi, Quận 2, TP.HCM', GETDATE(), 1, 'admin'),
('KH024', 'Phạm Thị Dung', '0901234570', 'dung.pham@email.com', 'Nữ', '321 Đường Nguyễn Duy Trinh, Phường Bình Trưng Tây, Quận 2, TP.HCM', GETDATE(), 1, 'admin'),
('KH025', 'Hoàng Văn Em', '0901234571', 'em.hoang@email.com', 'Nam', '654 Đường Lê Văn Việt, Phường Hiệp Phú, Quận 9, TP.HCM', GETDATE(), 1, 'admin'),
('KH026', 'Vũ Thị Phương', '0901234572', 'phuong.vu@email.com', 'Nữ', '987 Đường Đỗ Xuân Hợp, Phường Phước Long A, Quận 9, TP.HCM', GETDATE(), 1, 'admin'),
('KH027', 'Đặng Văn Giang', '0901234573', 'giang.dang@email.com', 'Nam', '147 Đường Nguyễn Xiển, Phường Long Thạnh Mỹ, Quận 9, TP.HCM', GETDATE(), 1, 'admin'),
('KH028', 'Bùi Thị Hoa', '0901234574', 'hoa.bui@email.com', 'Nữ', '258 Đường Tân Sơn Nhì, Phường Tân Sơn Nhì, Quận Tân Phú, TP.HCM', GETDATE(), 1, 'admin'),
('KH029', 'Đinh Văn Ích', '0901234575', 'ich.dinh@email.com', 'Nam', '369 Đường Tân Hương, Phường Tân Quý, Quận Tân Phú, TP.HCM', GETDATE(), 1, 'admin'),
('KH030', 'Ngô Thị Kim', '0901234576', 'kim.ngo@email.com', 'Nữ', '741 Đường Tân Kỳ Tân Quý, Phường Sơn Kỳ, Quận Tân Phú, TP.HCM', GETDATE(), 1, 'admin');
