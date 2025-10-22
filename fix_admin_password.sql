-- Script để fix mật khẩu admin
USE PhoniXDB1;
GO

-- Kiểm tra mật khẩu hiện tại
SELECT tai_khoan, mat_khau, 
       CASE 
           WHEN mat_khau LIKE '{bcrypt}%' THEN 'BCrypt Encoded'
           WHEN mat_khau LIKE '{noop}%' THEN 'Plain Text with noop prefix'
           WHEN mat_khau LIKE '$2a$%' THEN 'BCrypt without prefix'
           ELSE 'Plain Text'
       END as password_type
FROM nhan_vien 
WHERE tai_khoan = 'admin';

-- Cập nhật mật khẩu admin thành 123456 với prefix {noop}
UPDATE nhan_vien 
SET mat_khau = '{noop}123456'
WHERE email = 'admin@phonestore.com';

-- Kiểm tra kết quả
SELECT tai_khoan, email, mat_khau, 
       CASE 
           WHEN mat_khau LIKE '{bcrypt}%' THEN 'BCrypt Encoded'
           WHEN mat_khau LIKE '{noop}%' THEN 'Plain Text with noop prefix'
           WHEN mat_khau LIKE '$2a$%' THEN 'BCrypt without prefix'
           ELSE 'Plain Text'
       END as password_type
FROM nhan_vien 
WHERE email = 'admin@phonestore.com';
