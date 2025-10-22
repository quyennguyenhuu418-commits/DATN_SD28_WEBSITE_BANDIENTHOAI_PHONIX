-- Script để cập nhật tất cả mật khẩu thành {noop} prefix
USE PhoniXDB1;
GO

-- Cập nhật tất cả mật khẩu chưa có prefix thành {noop}
UPDATE nhan_vien
SET mat_khau = '{noop}' + mat_khau
WHERE mat_khau NOT LIKE '{%}%';

-- Kiểm tra kết quả
SELECT 
    tai_khoan,
    email,
    mat_khau,
    CASE
        WHEN mat_khau LIKE '{bcrypt}%' THEN 'BCrypt Encoded'
        WHEN mat_khau LIKE '{noop}%' THEN 'Plain Text with noop prefix'
        WHEN mat_khau LIKE '$2a$%' THEN 'BCrypt without prefix'
        ELSE 'Plain Text'
    END as password_type
FROM nhan_vien
ORDER BY tai_khoan;

PRINT 'Tất cả mật khẩu đã được cập nhật thành {noop} prefix!';
