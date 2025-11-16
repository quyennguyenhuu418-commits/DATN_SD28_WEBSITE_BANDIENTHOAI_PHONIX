-- Migration to update dot_giam_gia_san_pham table to use chi_tiet_san_pham instead of san_pham

-- First, drop the existing foreign key constraint
ALTER TABLE dot_giam_gia_san_pham DROP CONSTRAINT IF EXISTS fk_dot_giam_gia_san_pham_san_pham;

-- Add new column for chi_tiet_san_pham
ALTER TABLE dot_giam_gia_san_pham ADD COLUMN id_chi_tiet_san_pham INTEGER;

-- Add foreign key constraint for chi_tiet_san_pham
ALTER TABLE dot_giam_gia_san_pham 
ADD CONSTRAINT fk_dot_giam_gia_san_pham_chi_tiet_san_pham 
FOREIGN KEY (id_chi_tiet_san_pham) REFERENCES chi_tiet_san_pham(id);

-- Migrate existing data: for each san_pham, find the first chi_tiet_san_pham and use it
UPDATE dot_giam_gia_san_pham 
SET id_chi_tiet_san_pham = (
    SELECT MIN(ctsp.id) 
    FROM chi_tiet_san_pham ctsp 
    WHERE ctsp.id_sp = dot_giam_gia_san_pham.id_san_pham
)
WHERE id_chi_tiet_san_pham IS NULL;

-- Make the new column NOT NULL
ALTER TABLE dot_giam_gia_san_pham ALTER COLUMN id_chi_tiet_san_pham INTEGER NOT NULL;

-- Drop the old constraints and indexes first
ALTER TABLE dot_giam_gia_san_pham DROP CONSTRAINT IF EXISTS UQ_khuyen_mai_san_pham;
DROP INDEX IF EXISTS idx_dot_giam_gia_san_pham_san_pham ON dot_giam_gia_san_pham;
ALTER TABLE dot_giam_gia_san_pham DROP CONSTRAINT IF EXISTS FK__dot_giam___id_sa__13F1F5EB;

-- Drop the old column
ALTER TABLE dot_giam_gia_san_pham DROP COLUMN id_san_pham;

-- Add unique constraint for khuyen_mai + chi_tiet_san_pham
ALTER TABLE dot_giam_gia_san_pham 
ADD CONSTRAINT uk_khuyen_mai_chi_tiet_san_pham 
UNIQUE (id_khuyen_mai, id_chi_tiet_san_pham);
