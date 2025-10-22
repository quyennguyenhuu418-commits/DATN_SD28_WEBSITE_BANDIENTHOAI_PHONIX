-- Script để update mật khẩu từ plain text sang BCrypt hoặc thêm prefix {noop}
-- Chạy script này để xử lý tất cả mật khẩu trong database

-- CÁCH 1: Thêm prefix {noop} cho plain text passwords (Khuyến nghị cho development)
UPDATE nhan_vien 
SET mat_khau = '{noop}' + mat_khau
WHERE mat_khau NOT LIKE '{%}%' AND mat_khau NOT LIKE '$2a$%';

-- CÁCH 2: Mã hóa mật khẩu "123456" bằng BCrypt (Cho production)
-- UPDATE nhan_vien 
-- SET mat_khau = '{bcrypt}$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi'
-- WHERE mat_khau = '123456';

-- CÁCH 3: Mã hóa tất cả plain text passwords bằng BCrypt
-- UPDATE nhan_vien 
-- SET mat_khau = '{bcrypt}' + '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi'
-- WHERE mat_khau = '123456';

-- Kiểm tra kết quả
SELECT tai_khoan, mat_khau, 
       CASE 
           WHEN mat_khau LIKE '{bcrypt}%' THEN 'BCrypt Encoded'
           WHEN mat_khau LIKE '{noop}%' THEN 'Plain Text with noop prefix'
           WHEN mat_khau LIKE '$2a$%' THEN 'BCrypt without prefix'
           ELSE 'Plain Text'
       END as password_type
FROM nhan_vien 
ORDER BY tai_khoan;
