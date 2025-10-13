-- Thêm các cột mới vào bảng khach_hang_giam_gia
ALTER TABLE khach_hang_giam_gia 
ADD COLUMN da_su_dung BIT DEFAULT 0,
ADD COLUMN ngay_su_dung DATETIME NULL,
ADD COLUMN so_tien_giam DOUBLE NULL;

-- Cập nhật dữ liệu hiện tại
UPDATE khach_hang_giam_gia SET da_su_dung = 0 WHERE da_su_dung IS NULL;




