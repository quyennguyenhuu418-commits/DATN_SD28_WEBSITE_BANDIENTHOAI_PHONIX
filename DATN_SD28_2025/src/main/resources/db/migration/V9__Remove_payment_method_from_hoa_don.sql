-- Remove phuong_thuc_thanh_toan column from hoa_don table
-- Payment method information will be stored in chi_tiet_thanh_toan table instead

-- Drop the column if it exists
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('hoa_don') AND name = 'phuong_thuc_thanh_toan')
BEGIN
    ALTER TABLE hoa_don DROP COLUMN phuong_thuc_thanh_toan;
END
