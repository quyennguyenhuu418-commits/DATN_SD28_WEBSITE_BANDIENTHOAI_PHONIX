-- Script để cập nhật role của admin thành ADMIN
USE PhoniXDB1;
GO

-- Kiểm tra role hiện tại
SELECT tai_khoan, email, chuc_vu, 
       CASE 
           WHEN chuc_vu = 'Quản lý' THEN 'MANAGER'
           WHEN chuc_vu = 'Quản trị viên' THEN 'ADMIN'
           WHEN chuc_vu = 'Nhân viên bán hàng' THEN 'STAFF'
           ELSE 'UNKNOWN'
       END as mapped_role
FROM nhan_vien 
WHERE email = 'admin@phonestore.com';

-- Cập nhật role admin thành 'Quản trị viên' (ADMIN)
UPDATE nhan_vien 
SET chuc_vu = 'Quản trị viên'
WHERE email = 'admin@phonestore.com';

-- Kiểm tra kết quả
SELECT tai_khoan, email, chuc_vu, 
       CASE 
           WHEN chuc_vu = 'Quản lý' THEN 'MANAGER'
           WHEN chuc_vu = 'Quản trị viên' THEN 'ADMIN'
           WHEN chuc_vu = 'Nhân viên bán hàng' THEN 'STAFF'
           ELSE 'UNKNOWN'
       END as mapped_role
FROM nhan_vien 
WHERE email = 'admin@phonestore.com';
