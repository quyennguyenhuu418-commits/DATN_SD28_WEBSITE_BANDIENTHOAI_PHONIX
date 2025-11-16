-- Tạo bảng lưu lịch sử thay đổi trạng thái hóa đơn
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[trang_thai_tracking]') AND type in (N'U'))
BEGIN
    CREATE TABLE trang_thai_tracking (
        id INT IDENTITY(1,1) PRIMARY KEY,
        hoa_don_id INT NOT NULL,
        trang_thai INT NOT NULL,
        ten_trang_thai NVARCHAR(255),
        thoi_gian DATETIME2 NOT NULL DEFAULT GETDATE(),
        mo_ta NVARCHAR(MAX),
        nguoi_thuc_hien NVARCHAR(255),
        created_at DATETIME2 DEFAULT GETDATE(),
        updated_at DATETIME2 DEFAULT GETDATE(),
        
        CONSTRAINT FK_trang_thai_tracking_hoa_don FOREIGN KEY (hoa_don_id) REFERENCES hoa_don(id) ON DELETE CASCADE
    );
    
    CREATE INDEX idx_trang_thai_tracking_hoa_don_id ON trang_thai_tracking(hoa_don_id);
    CREATE INDEX idx_trang_thai_tracking_trang_thai ON trang_thai_tracking(trang_thai);
    CREATE INDEX idx_trang_thai_tracking_thoi_gian ON trang_thai_tracking(thoi_gian);
END;







