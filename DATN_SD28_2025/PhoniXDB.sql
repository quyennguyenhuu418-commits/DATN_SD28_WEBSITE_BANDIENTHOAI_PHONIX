-- Tạo Database
CREATE DATABASE PhoniXDB1;
GO

USE PhoniXDB1;
GO

-- Tạo bảng danh_muc
CREATE TABLE danh_muc (
    id INT PRIMARY KEY IDENTITY(1,1),
    ma_danh_muc NVARCHAR(50),
    ten_danh_muc NVARCHAR(255),
    ngay_tao DATETIME DEFAULT GETDATE(),
    ngay_cap_nhat DATETIME DEFAULT GETDATE(),
    trang_thai INT DEFAULT 1
);

-- Tạo bảng hang
CREATE TABLE hang (
    id INT PRIMARY KEY IDENTITY(1,1),
    ten NVARCHAR(255),
    xuat_xu NVARCHAR(255),
    mo_ta NVARCHAR(MAX),
    ngay_tao DATETIME DEFAULT GETDATE(),
    ngay_cap_nhat DATETIME DEFAULT GETDATE(),
    trang_thai INT DEFAULT 1
);

-- Tạo bảng man_hinh
CREATE TABLE man_hinh (
    id INT PRIMARY KEY IDENTITY(1,1),
    ma_man_hinh NVARCHAR(50),
    kich_thuoc NVARCHAR(50),
    cong_nghe NVARCHAR(255),
    do_phan_giai NVARCHAR(50),
    tan_so_quet NVARCHAR(50),
    kieu_man_hinh NVARCHAR(255),
    mo_ta NVARCHAR(MAX),
    ngay_tao DATETIME DEFAULT GETDATE(),
    ngay_cap_nhat DATETIME DEFAULT GETDATE(),
    trang_thai INT DEFAULT 1
);

-- Tạo bảng camera_truoc
CREATE TABLE camera_truoc (
    id INT PRIMARY KEY IDENTITY(1,1),
    ma_camera NVARCHAR(50),
    thong_so NVARCHAR(255),
    mo_ta NVARCHAR(MAX),
    ngay_tao DATETIME DEFAULT GETDATE(),
    ngay_cap_nhat DATETIME DEFAULT GETDATE(),
    trang_thai INT DEFAULT 1
);

-- Tạo bảng camera_sau
CREATE TABLE camera_sau (
    id INT PRIMARY KEY IDENTITY(1,1),
    ma_camera NVARCHAR(50),
    thong_so NVARCHAR(255),
    mo_ta NVARCHAR(MAX),
    ngay_tao DATETIME DEFAULT GETDATE(),
    ngay_cap_nhat DATETIME DEFAULT GETDATE(),
    trang_thai INT DEFAULT 1
);

-- Tạo bảng chip
CREATE TABLE chip (
    id INT PRIMARY KEY IDENTITY(1,1),
    ma_chip NVARCHAR(50),
    ten_chip NVARCHAR(255),
    mo_ta NVARCHAR(MAX),
    ngay_tao DATETIME DEFAULT GETDATE(),
    ngay_cap_nhat DATETIME DEFAULT GETDATE(),
    trang_thai INT DEFAULT 1
);

-- Tạo bảng gpu
CREATE TABLE gpu (
    id INT PRIMARY KEY IDENTITY(1,1),
    ma_gpu NVARCHAR(50),
    ten_gpu NVARCHAR(255),
    mo_ta NVARCHAR(MAX),
    ngay_tao DATETIME DEFAULT GETDATE(),
    ngay_cap_nhat DATETIME DEFAULT GETDATE(),
    trang_thai INT DEFAULT 1
);

-- Tạo bảng sim
CREATE TABLE sim (
    id INT PRIMARY KEY IDENTITY(1,1),
    loai_sim NVARCHAR(255),
    mo_ta NVARCHAR(MAX),
    ngay_tao DATETIME DEFAULT GETDATE(),
    ngay_cap_nhat DATETIME DEFAULT GETDATE(),
    trang_thai INT DEFAULT 1
);

-- Tạo bảng he_dieu_hanh
CREATE TABLE he_dieu_hanh (
    id INT PRIMARY KEY IDENTITY(1,1),
    ma_he_dieu_hanh NVARCHAR(50),
    ten_he_dieu_hanh NVARCHAR(255),
    mo_ta NVARCHAR(MAX),
    ngay_tao DATETIME DEFAULT GETDATE(),
    ngay_cap_nhat DATETIME DEFAULT GETDATE(),
    trang_thai INT DEFAULT 1
);

-- Tạo bảng cpu
CREATE TABLE cpu (
    id INT PRIMARY KEY IDENTITY(1,1),
    ma_cpu NVARCHAR(50),
    ten_cpu NVARCHAR(255),
    mo_ta NVARCHAR(MAX),
    ngay_tao DATETIME DEFAULT GETDATE(),
    ngay_cap_nhat DATETIME DEFAULT GETDATE(),
    trang_thai INT DEFAULT 1
);

-- Tạo bảng pin
CREATE TABLE pin (
    id INT PRIMARY KEY IDENTITY(1,1),
    ma_pin NVARCHAR(50),
    dung_luong_pin NVARCHAR(50),
    cong_nghe_sac NVARCHAR(255),
    mo_ta NVARCHAR(MAX),
    ngay_tao DATETIME DEFAULT GETDATE(),
    ngay_cap_nhat DATETIME DEFAULT GETDATE(),
    trang_thai INT DEFAULT 1
);

-- Tạo bảng ram
CREATE TABLE ram (
    id INT PRIMARY KEY IDENTITY(1,1),
    ma_ram NVARCHAR(50),
    ten_ram NVARCHAR(255),
    mo_ta NVARCHAR(MAX),
    ngay_tao DATETIME DEFAULT GETDATE(),
    ngay_cap_nhat DATETIME DEFAULT GETDATE(),
    trang_thai INT DEFAULT 1
);

-- Tạo bảng rom
CREATE TABLE rom (
    id INT PRIMARY KEY IDENTITY(1,1),
    ma_rom NVARCHAR(50),
    dung_luong NVARCHAR(50),
    mo_ta NVARCHAR(MAX),
    trang_thai INT DEFAULT 1,
    ngay_tao DATETIME DEFAULT GETDATE(),
    ngay_cap_nhat DATETIME DEFAULT GETDATE()
);

-- Tạo bảng mau_sac
CREATE TABLE mau_sac (
    id INT PRIMARY KEY IDENTITY(1,1),
    ma_mau NVARCHAR(50),
    ten_mau NVARCHAR(255),
    mo_ta NVARCHAR(MAX),
    ngay_tao DATETIME DEFAULT GETDATE(),
    ngay_cap_nhat DATETIME DEFAULT GETDATE(),
    trang_thai INT DEFAULT 1
);

-- Tạo bảng san_pham
CREATE TABLE san_pham (
    id INT PRIMARY KEY IDENTITY(1,1),
    id_danh_muc INT,
    id_hang INT,
    id_man_hinh INT,
    id_camera_truoc INT,
    id_camera_sau INT,
    id_chip INT,
    id_gpu INT,
    id_sim INT,
    id_he_dieu_hanh INT,
    id_cpu INT,
    id_pin INT,
    thiet_ke NVARCHAR(255),
    kich_thuoc NVARCHAR(100),
    ma_san_pham NVARCHAR(50),
    ten_san_pham NVARCHAR(255),
    mo_ta NVARCHAR(MAX),
    ngay_tao DATETIME DEFAULT GETDATE(),
    ngay_cap_nhat DATETIME DEFAULT GETDATE(),
    nguoi_tao NVARCHAR(100),
    nguoi_cap_nhat NVARCHAR(100),
    trang_thai INT DEFAULT 1,
    FOREIGN KEY (id_danh_muc) REFERENCES danh_muc(id),
    FOREIGN KEY (id_hang) REFERENCES hang(id),
    FOREIGN KEY (id_man_hinh) REFERENCES man_hinh(id),
    FOREIGN KEY (id_camera_truoc) REFERENCES camera_truoc(id),
    FOREIGN KEY (id_camera_sau) REFERENCES camera_sau(id),
    FOREIGN KEY (id_chip) REFERENCES chip(id),
    FOREIGN KEY (id_gpu) REFERENCES gpu(id),
    FOREIGN KEY (id_sim) REFERENCES sim(id),
    FOREIGN KEY (id_he_dieu_hanh) REFERENCES he_dieu_hanh(id),
    FOREIGN KEY (id_cpu) REFERENCES cpu(id),
    FOREIGN KEY (id_pin) REFERENCES pin(id)
);

-- Tạo bảng chi_tiet_san_pham
CREATE TABLE chi_tiet_san_pham (
    id INT PRIMARY KEY IDENTITY(1,1),
    id_sp INT,
    rom_id INT,
    ram_id INT,
    mau_sac_id INT,
    ma_ctsp NVARCHAR(50),
    gia_nhap DECIMAL(18,2),
    gia_ban DECIMAL(18,2),
    so_luong INT,
    ghi_chu NVARCHAR(MAX),
    ngay_tao DATETIME DEFAULT GETDATE(),
    ngay_cap_nhat DATETIME DEFAULT GETDATE(),
    nguoi_tao NVARCHAR(100),
    nguoi_cap_nhat NVARCHAR(100),
    trang_thai INT DEFAULT 1,
    FOREIGN KEY (id_sp) REFERENCES san_pham(id),
    FOREIGN KEY (rom_id) REFERENCES rom(id),
    FOREIGN KEY (ram_id) REFERENCES ram(id),
    FOREIGN KEY (mau_sac_id) REFERENCES mau_sac(id)
);

-- Tạo bảng khach_hang
CREATE TABLE khach_hang (
    id INT PRIMARY KEY IDENTITY(1,1),
    ma_khach_hang NVARCHAR(50),
    ho_ten NVARCHAR(255),
    so_dien_thoai NVARCHAR(20),
    tai_khoan NVARCHAR(100),
    mat_khau NVARCHAR(255),
    ngay_sinh DATE,
    gioi_tinh NVARCHAR(10),
    email NVARCHAR(255),
    ngay_tao DATETIME DEFAULT GETDATE(),
    ngay_cap_nhat DATETIME DEFAULT GETDATE(),
    trang_thai INT DEFAULT 1,
    nguoi_tao NVARCHAR(100),
    nguoi_cap_nhat NVARCHAR(100)
);

-- Tạo bảng nhan_vien
CREATE TABLE nhan_vien (
    id INT PRIMARY KEY IDENTITY(1,1),
    ma_nhan_vien NVARCHAR(50),
    ho_ten NVARCHAR(255),
    so_dien_thoai NVARCHAR(20),
    ngay_sinh DATE,
    tai_khoan NVARCHAR(100),
    mat_khau NVARCHAR(255),
    gioi_tinh NVARCHAR(10),
    dia_chi NVARCHAR(MAX),
    email NVARCHAR(255),
    chuc_vu NVARCHAR(100),
    anh_dai_dien NVARCHAR(MAX),
    ngay_tao DATETIME DEFAULT GETDATE(),
    ngay_cap_nhat DATETIME DEFAULT GETDATE(),
    trang_thai INT DEFAULT 1
);

-- Tạo bảng phieu_giam_gia
CREATE TABLE phieu_giam_gia (
    id INT PRIMARY KEY IDENTITY(1,1),
    ma_phieu_giam_gia NVARCHAR(50),
    ten_phieu_giam_gia NVARCHAR(255),
    loai_phieu_giam_gia NVARCHAR(50),
    gia_tri_giam_gia DECIMAL(18,2),
    so_tien_giam_toi_da DECIMAL(18,2),
    hoa_don_toi_thieu DECIMAL(18,2),
    so_luong_dung INT,
    ngay_bat_dau DATETIME,
    ngay_ket_thuc DATETIME,
    rieng_tu BIT,
    mo_ta NVARCHAR(MAX),
    ngay_tao DATETIME DEFAULT GETDATE(),
    ngay_cap_nhat DATETIME DEFAULT GETDATE(),
    nguoi_tao NVARCHAR(100),
    nguoi_cap_nhat NVARCHAR(100),
    trang_thai INT DEFAULT 1
);

-- Tạo bảng hoa_don
CREATE TABLE hoa_don (
    id INT PRIMARY KEY IDENTITY(1,1),
    id_phieu_giam_gia INT,
    id_khach_hang INT,
    id_nhan_vien INT,
    ma_hoa_don NVARCHAR(50),
    ten_khach_hang NVARCHAR(255),
    so_dien_thoai NVARCHAR(20),
    dia_chi NVARCHAR(MAX),
    tong_tien DECIMAL(18,2),
    tong_tien_sau_giam DECIMAL(18,2),
    loai_hoa_don NVARCHAR(50),
    ghi_chu NVARCHAR(MAX),
    ngay_thanh_toan DATETIME,
    ngay_tao DATETIME DEFAULT GETDATE(),
    ngay_cap_nhat DATETIME DEFAULT GETDATE(),
    nguoi_tao NVARCHAR(100),
    nguoi_cap_nhat NVARCHAR(100),
    trang_thai INT DEFAULT 1,
    FOREIGN KEY (id_phieu_giam_gia) REFERENCES phieu_giam_gia(id),
    FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id),
    FOREIGN KEY (id_nhan_vien) REFERENCES nhan_vien(id)
);

-- Tạo bảng hoa_don_chi_tiet
CREATE TABLE hoa_don_chi_tiet (
    id INT PRIMARY KEY IDENTITY(1,1),
    id_hoa_don INT,
    id_ctsp INT,
    don_gia DECIMAL(18,2),
    thanh_tien DECIMAL(18,2),
    trang_thai INT DEFAULT 1,
    FOREIGN KEY (id_hoa_don) REFERENCES hoa_don(id),
    FOREIGN KEY (id_ctsp) REFERENCES chi_tiet_san_pham(id)
);

-- Tạo bảng phuong_thuc_thanh_toan
CREATE TABLE phuong_thuc_thanh_toan (
    id INT PRIMARY KEY IDENTITY(1,1),
    ten_phuong_thuc NVARCHAR(255),
    loai_hinh_thuc NVARCHAR(100),
    ngay_tao DATETIME DEFAULT GETDATE(),
    ngay_cap_nhat DATETIME DEFAULT GETDATE(),
    trang_thai INT DEFAULT 1
);

-- Tạo bảng chi_tiet_thanh_toan
CREATE TABLE chi_tiet_thanh_toan (
    id INT PRIMARY KEY IDENTITY(1,1),
    id_don_hang INT,
    id_phuong_thuc_thanh_toan INT,
    ma_giao_dich NVARCHAR(100),
    so_tien DECIMAL(18,2),
    ngay_thanh_toan DATETIME,
    trang_thai INT DEFAULT 1,
    ngay_tao DATETIME DEFAULT GETDATE(),
    ngay_cap_nhat DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (id_don_hang) REFERENCES hoa_don(id),
    FOREIGN KEY (id_phuong_thuc_thanh_toan) REFERENCES phuong_thuc_thanh_toan(id)
);

-- Tạo bảng khuyen_mai
CREATE TABLE khuyen_mai (
    id INT PRIMARY KEY IDENTITY(1,1),
    ma_khuyen_mai NVARCHAR(50),
    ten_khuyen_mai NVARCHAR(255),
    mo_ta NVARCHAR(MAX),
    muc_do_uu_tien INT,
    phan_tram_giam DECIMAL(5,2),
    giam_toi_da DECIMAL(18,2),
    ngay_bat_dau DATETIME,
    ngay_ket_thuc DATETIME,
    ngay_tao DATETIME DEFAULT GETDATE(),
    ngay_cap_nhat DATETIME DEFAULT GETDATE(),
    nguoi_tao NVARCHAR(100),
    nguoi_cap_nhat NVARCHAR(100),
    trang_thai INT DEFAULT 1
);

-- Tạo bảng chi_tiet_khuyen_mai
CREATE TABLE chi_tiet_khuyen_mai (
    id INT PRIMARY KEY IDENTITY(1,1),
    id_ctsp INT,
    id_khuyen_mai INT,
    ngay_ap_dung DATETIME,
    muc_do_uu_tien INT,
    phan_tram_giam_gia DECIMAL(5,2),
    FOREIGN KEY (id_ctsp) REFERENCES chi_tiet_san_pham(id),
    FOREIGN KEY (id_khuyen_mai) REFERENCES khuyen_mai(id)
);

-- Tạo bảng hinh_anh
CREATE TABLE hinh_anh (
    id INT PRIMARY KEY IDENTITY(1,1),
    id_ctsp INT,
    url_anh NVARCHAR(MAX),
    ngay_tao DATETIME DEFAULT GETDATE(),
    ngay_sua DATETIME DEFAULT GETDATE(),
    trang_thai INT DEFAULT 1,
    FOREIGN KEY (id_ctsp) REFERENCES chi_tiet_san_pham(id)
);

-- Tạo bảng user_dia_chi
CREATE TABLE user_dia_chi (
    id INT PRIMARY KEY IDENTITY(1,1),
    id_dia_chi INT,
    id_user INT,
    loai_dia_chi NVARCHAR(50),
    mac_dinh BIT,
    ngay_tao DATETIME DEFAULT GETDATE(),
    ngay_cap_nhat DATETIME DEFAULT GETDATE(),
    trang_thai INT DEFAULT 1,
    FOREIGN KEY (id_user) REFERENCES khach_hang(id)
);

-- Tạo bảng imei
CREATE TABLE imei (
    id INT PRIMARY KEY IDENTITY(1,1),
    id_ctsp INT,
    imei NVARCHAR(50),
    ngay_tao DATETIME DEFAULT GETDATE(),
    ngay_cap_nhat DATETIME DEFAULT GETDATE(),
    trang_thai INT DEFAULT 1,
    FOREIGN KEY (id_ctsp) REFERENCES chi_tiet_san_pham(id)
);

-- Tạo bảng imei_da_ban
CREATE TABLE imei_da_ban (
    id INT PRIMARY KEY IDENTITY(1,1),
    id_hoa_don_chi_tiet INT,
    imei NVARCHAR(50),
    trang_thai INT DEFAULT 1,
    FOREIGN KEY (id_hoa_don_chi_tiet) REFERENCES hoa_don_chi_tiet(id)
);

-- Tạo bảng khach_hang_giam_gia
CREATE TABLE khach_hang_giam_gia (
    id INT PRIMARY KEY IDENTITY(1,1),
    id_khach_hang INT,
    id_phieu_giam_gia INT,
    nguoi_su_dung NVARCHAR(100),
    ngay_cap DATETIME,
    trang_thai INT DEFAULT 1,
    FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(id),
    FOREIGN KEY (id_phieu_giam_gia) REFERENCES phieu_giam_gia(id)
);

-- =================== INSERT DỮ LIỆU MẪU ===================

-- danh_muc
INSERT INTO danh_muc(ma_danh_muc, ten_danh_muc)
VALUES
(N'DM001', N'Điện thoại'),
(N'DM002', N'Tablet'),
(N'DM003', N'Laptop'),
(N'DM004', N'Phụ kiện'),
(N'DM005', N'Đồng hồ thông minh');

-- hang
INSERT INTO hang(ten, xuat_xu, mo_ta)
VALUES
(N'Apple', N'Mỹ', N'Thương hiệu công nghệ hàng đầu'),
(N'Samsung', N'Hàn Quốc', N'Tập đoàn điện tử đa quốc gia'),
(N'Xiaomi', N'Trung Quốc', N'Thương hiệu công nghệ giá rẻ'),
(N'Vivo', N'Trung Quốc', N'Chuyên về smartphone'),
(N'OPPO', N'Trung Quốc', N'Thương hiệu điện thoại phổ biến');

-- man_hinh
INSERT INTO man_hinh(ma_man_hinh, kich_thuoc, cong_nghe, do_phan_giai, tan_so_quet, kieu_man_hinh, mo_ta)
VALUES
(N'MH001', N'6.1 inch', N'OLED', N'1170 x 2532', N'60Hz', N'Tai thỏ', N'Màn hình Super Retina XDR'),
(N'MH002', N'6.2 inch', N'Dynamic AMOLED', N'1080 x 2400', N'120Hz', N'Đục lỗ', N'Màn hình Infinity-O'),
(N'MH003', N'6.43 inch', N'AMOLED', N'1080 x 2400', N'60Hz', N'Đục lỗ', N'Màn hình DotDisplay'),
(N'MH004', N'6.44 inch', N'AMOLED', N'1080 x 2400', N'90Hz', N'Đục lỗ', N'Màn hình FHD+'),
(N'MH005', N'6.43 inch', N'AMOLED', N'1080 x 2400', N'60Hz', N'Đục lỗ', N'Màn hình ColorOS');

-- camera_truoc
INSERT INTO camera_truoc(ma_camera, thong_so, mo_ta)
VALUES
(N'CT001', N'12MP, f/2.2', N'Camera TrueDepth'),
(N'CT002', N'10MP, f/2.2', N'Camera Dual Pixel'),
(N'CT003', N'13MP, f/2.5', N'Camera selfie AI'),
(N'CT004', N'44MP, f/2.0', N'Camera selfie Eye Autofocus'),
(N'CT005', N'16MP, f/2.4', N'Camera selfie AI Beauty');

-- camera_sau
INSERT INTO camera_sau(ma_camera, thong_so, mo_ta)
VALUES
(N'CS001', N'12MP + 12MP', N'Camera kép góc rộng và siêu rộng'),
(N'CS002', N'64MP + 12MP + 5MP', N'Camera chính + góc siêu rộng + macro'),
(N'CS003', N'48MP + 8MP + 2MP + 2MP', N'Camera chính + góc rộng + macro + độ sâu'),
(N'CS004', N'64MP + 8MP + 2MP', N'Camera chính + góc rộng + macro'),
(N'CS005', N'48MP + 8MP + 2MP', N'Camera chính + góc siêu rộng + macro');

-- chip
INSERT INTO chip(ma_chip, ten_chip, mo_ta)
VALUES
(N'CHIP001', N'Apple A14 Bionic', N'Chip 5nm hiệu năng cao'),
(N'CHIP002', N'Exynos 2100', N'Chip 5nm của Samsung'),
(N'CHIP003', N'Snapdragon 678', N'Chip tầm trung'),
(N'CHIP004', N'MediaTek Dimensity 800U', N'Chip 5G tầm trung'),
(N'CHIP005', N'Snapdragon 662', N'Chip tiết kiệm pin');

-- gpu
INSERT INTO gpu(ma_gpu, ten_gpu, mo_ta)
VALUES
(N'GPU001', N'Apple GPU 4-core', N'GPU tích hợp Apple'),
(N'GPU002', N'Mali-G78 MP14', N'GPU của ARM'),
(N'GPU003', N'Adreno 612', N'GPU Qualcomm'),
(N'GPU004', N'Mali-G57 MC3', N'GPU MediaTek'),
(N'GPU005', N'Adreno 610', N'GPU tiết kiệm pin');

-- sim
INSERT INTO sim(loai_sim, mo_ta)
VALUES
(N'Nano SIM + eSIM', N'Hỗ trợ 2 SIM'),
(N'2 Nano SIM', N'Hỗ trợ 2 SIM vật lý'),
(N'2 Nano SIM', N'Dual SIM Dual Standby'),
(N'2 Nano SIM', N'Hỗ trợ 4G'),
(N'2 Nano SIM', N'Dual SIM');

-- he_dieu_hanh
INSERT INTO he_dieu_hanh(ma_he_dieu_hanh, ten_he_dieu_hanh, mo_ta)
VALUES
(N'HDH001', N'Android 11', N'Hệ điều hành mở'),
(N'HDH002', N'iOS 14', N'Hệ điều hành của Apple'),
(N'HDH003', N'One UI 3.1', N'Giao diện Samsung'),
(N'HDH004', N'MIUI 12', N'Giao diện Xiaomi'),
(N'HDH005', N'Funtouch OS 11.1', N'Giao diện Vivo');

-- cpu
INSERT INTO cpu(ma_cpu, ten_cpu, mo_ta)
VALUES
(N'CPU001', N'6-core', N'2 nhân hiệu năng cao + 4 nhân tiết kiệm'),
(N'CPU002', N'8-core', N'1 + 3 + 4 nhân'),
(N'CPU003', N'8-core Kryo 460', N'CPU Qualcomm'),
(N'CPU004', N'8-core', N'CPU MediaTek'),
(N'CPU005', N'8-core Kryo 260', N'CPU tầm trung');

-- pin
INSERT INTO pin(ma_pin, dung_luong_pin, cong_nghe_sac, mo_ta)
VALUES
(N'PIN001', N'2815 mAh', N'Sạc nhanh 20W', N'Hỗ trợ sạc không dây'),
(N'PIN002', N'4000 mAh', N'Sạc nhanh 25W', N'Sạc không dây 15W'),
(N'PIN003', N'5000 mAh', N'Sạc nhanh 33W', N'Pin lớn'),
(N'PIN004', N'4000 mAh', N'Sạc nhanh 33W', N'Sạc đầy trong 63 phút'),
(N'PIN005', N'5000 mAh', N'Sạc nhanh 30W', N'Pin trâu');

-- ram
INSERT INTO ram(ma_ram, ten_ram, mo_ta)
VALUES
(N'RAM001', N'4GB', N'RAM DDR4'),
(N'RAM002', N'6GB', N'RAM LPDDR5'),
(N'RAM003', N'8GB', N'RAM LPDDR4X'),
(N'RAM004', N'12GB', N'RAM LPDDR5'),
(N'RAM005', N'4GB', N'RAM LPDDR4X');

-- rom
INSERT INTO rom(ma_rom, dung_luong, mo_ta)
VALUES
(N'ROM001', N'64GB', N'Bộ nhớ UFS 2.1'),
(N'ROM002', N'128GB', N'Bộ nhớ UFS 3.1'),
(N'ROM003', N'256GB', N'Bộ nhớ UFS 2.2'),
(N'ROM004', N'512GB', N'Bộ nhớ UFS 3.1'),
(N'ROM005', N'128GB', N'Bộ nhớ eMMC 5.1');

-- mau_sac
INSERT INTO mau_sac(ma_mau, ten_mau, mo_ta)
VALUES
(N'MS001', N'Đen', N'Màu đen sang trọng'),
(N'MS002', N'Trắng', N'Màu trắng tinh khôi'),
(N'MS003', N'Xanh dương', N'Màu xanh navy'),
(N'MS004', N'Hồng', N'Màu hồng pastel'),
(N'MS005', N'Tím', N'Màu tím gradient');

-- san_pham
INSERT INTO san_pham(id_danh_muc, id_hang, id_man_hinh, id_camera_truoc, id_camera_sau, id_chip, id_gpu, id_sim, id_he_dieu_hanh, id_cpu, id_pin, ma_san_pham, ten_san_pham, thiet_ke, kich_thuoc, nguoi_tao)
VALUES
(1, 1, 1, 1, 1, 1, 3, 1, 2, 1, 1, N'SP001', N'iPhone 12', N'Glass', N'146.7 x 71.5', N'admin'),
(1, 2, 2, 2, 2, 2, 1, 2, 1, 2, 2, N'SP002', N'Samsung Galaxy S21', N'Metal', N'151.7 x 71.2', N'admin'),
(1, 3, 3, 3, 3, 3, 2, 2, 4, 3, 3, N'SP003', N'Xiaomi Redmi Note 10', N'Plastic', N'160 x 75', N'admin'),
(1, 4, 4, 4, 4, 4, 4, 2, 1, 4, 4, N'SP004', N'Vivo V21', N'Slim', N'158 x 72', N'admin'),
(1, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, N'SP005', N'OPPO A95', N'Poly', N'162 x 76', N'admin');

-- chi_tiet_san_pham (không cần nhập so_luong, trigger sẽ tự động tính)
INSERT INTO chi_tiet_san_pham(id_sp, rom_id, ram_id, mau_sac_id, ma_ctsp, gia_nhap, gia_ban, ghi_chu, nguoi_tao)
VALUES
(1, 1, 1, 1, N'CTSP001', 18000000, 20000000, N'iPhone 12 64GB Đen', N'admin'),
(2, 2, 2, 2, N'CTSP002', 15000000, 17000000, N'Samsung S21 128GB Trắng', N'admin'),
(3, 3, 3, 3, N'CTSP003', 4500000, 5500000, N'Redmi Note 10 256GB Xanh', N'admin'),
(4, 1, 1, 4, N'CTSP004', 6000000, 7000000, N'Vivo V21 64GB Hồng', N'admin'),
(5, 5, 5, 5, N'CTSP005', 5000000, 6000000, N'OPPO A95 128GB Tím', N'admin');

-- khach_hang
INSERT INTO khach_hang(ma_khach_hang, ho_ten, so_dien_thoai, tai_khoan, mat_khau, ngay_sinh, gioi_tinh, email, nguoi_tao, ngay_tao)
VALUES
(N'KH001', N'Nguyễn Văn A', N'0901234567', N'nguyenvana', N'123456', '1990-01-15', N'Nam', N'nguyenvana@gmail.com', N'admin', '2025-01-15'),
(N'KH002', N'Trần Thị B', N'0912345678', N'tranthib', N'123456', '1992-05-20', N'Nữ', N'tranthib@gmail.com', N'admin', '2025-01-16'),
(N'KH003', N'Lê Văn C', N'0923456789', N'levanc', N'123456', '1988-08-10', N'Nam', N'levanc@gmail.com', N'admin', '2025-01-20'),
(N'KH004', N'Phạm Thị D', N'0934567890', N'phamthid', N'123456', '1995-12-25', N'Nữ', N'phamthid@gmail.com', N'admin', '2025-01-22'),
(N'KH005', N'Hoàng Văn E', N'0945678901', N'hoangvane', N'123456', '1993-03-18', N'Nam', N'hoangvane@gmail.com', N'admin', '2025-01-25'),
-- Thêm khách hàng mới
(N'KH006', N'Nguyễn Văn F', N'0901111111', N'nguyenvanf', N'123456', '1991-02-14', N'Nam', N'nguyenvanf@gmail.com', N'admin', '2025-01-15'),
(N'KH007', N'Trần Thị G', N'0912222222', N'tranthig', N'123456', '1993-06-25', N'Nữ', N'tranthig@gmail.com', N'admin', '2025-01-16'),
(N'KH008', N'Lê Văn H', N'0923333333', N'levanh', N'123456', '1989-09-12', N'Nam', N'levanh@gmail.com', N'admin', '2025-01-17'),
(N'KH009', N'Phạm Thị I', N'0934444444', N'phamthii', N'123456', '1996-11-30', N'Nữ', N'phamthii@gmail.com', N'admin', '2025-01-18'),
(N'KH010', N'Hoàng Văn J', N'0945555555', N'hoangvanj', N'123456', '1994-04-22', N'Nam', N'hoangvanj@gmail.com', N'admin', '2025-01-19');

-- nhan_vien
INSERT INTO nhan_vien(ma_nhan_vien, ho_ten, so_dien_thoai, ngay_sinh, tai_khoan, mat_khau, gioi_tinh, dia_chi, email, chuc_vu)
VALUES
(N'NV001', N'Trần Văn Admin', N'0987654321', '1985-06-15', N'admin', N'admin123', N'Nam', N'Hà Nội', N'admin@phonestore.com', N'Quản lý'),
(N'NV002', N'Nguyễn Thị Thu', N'0976543210', '1990-08-20', N'thunguyen', N'123456', N'Nữ', N'Hà Nội', N'thu@phonestore.com', N'Nhân viên bán hàng'),
(N'NV003', N'Lê Minh Tuấn', N'0965432109', '1992-03-10', N'tuanle', N'123456', N'Nam', N'Hải Phòng', N'tuan@phonestore.com', N'Nhân viên bán hàng'),
(N'NV004', N'Phạm Văn Long', N'0954321098', '1988-11-05', N'longpham', N'123456', N'Nam', N'Đà Nẵng', N'long@phonestore.com', N'Nhân viên kho'),
(N'NV005', N'Vũ Thị Hoa', N'0943210987', '1994-07-22', N'hoavu', N'123456', N'Nữ', N'TP.HCM', N'hoa@phonestore.com', N'Kế toán');

-- phieu_giam_gia
INSERT INTO phieu_giam_gia(ma_phieu_giam_gia, ten_phieu_giam_gia, loai_phieu_giam_gia, gia_tri_giam_gia, so_tien_giam_toi_da, hoa_don_toi_thieu, so_luong_dung, ngay_bat_dau, ngay_ket_thuc, rieng_tu, nguoi_tao)
VALUES
(N'PGG001', N'Giảm giá 10%', N'Phần trăm', 10, 500000, 2000000, 100, '2025-01-01', '2025-12-31', 0, N'admin'),
(N'PGG002', N'Giảm 200K', N'Số tiền', 200000, 200000, 1000000, 50, '2025-01-01', '2025-06-30', 0, N'admin'),
(N'PGG003', N'Giảm giá 15%', N'Phần trăm', 15, 1000000, 5000000, 30, '2025-03-01', '2025-09-30', 1, N'admin'),
(N'PGG004', N'Giảm 500K', N'Số tiền', 500000, 500000, 10000000, 20, '2025-01-15', '2025-12-31', 1, N'admin'),
(N'PGG005', N'Giảm giá 20%', N'Phần trăm', 20, 2000000, 8000000, 10, '2025-06-01', '2025-08-31', 0, N'admin');

-- hoa_don
INSERT INTO hoa_don(id_phieu_giam_gia, id_khach_hang, id_nhan_vien, ma_hoa_don, ten_khach_hang, so_dien_thoai, dia_chi, tong_tien, tong_tien_sau_giam, loai_hoa_don, ghi_chu, ngay_thanh_toan, nguoi_tao, trang_thai, ngay_tao)
VALUES
-- HÓA ĐƠN ĐÃ THANH TOÁN (trang_thai = 1) - DỮ LIỆU GẦN ĐÂY
(1, 1, 2, N'HD001', N'Nguyễn Văn A', N'0901234567', N'Hà Nội', 20000000, 19500000, N'Bán lẻ', N'Khách hàng thân thiết', '2025-10-10', N'admin', 1, '2025-10-10'),
(2, 2, 2, N'HD002', N'Trần Thị B', N'0912345678', N'Hải Phòng', 17000000, 16800000, N'Bán lẻ', N'', '2025-10-11', N'admin', 1, '2025-10-11'),
(NULL, 3, 3, N'HD003', N'Lê Văn C', N'0923456789', N'Đà Nẵng', 5500000, 5500000, N'Bán lẻ', N'', '2025-10-12', N'admin', 1, '2025-10-12'),
(NULL, 4, 3, N'HD004', N'Phạm Thị D', N'0934567890', N'TP.HCM', 7000000, 7000000, N'Online', N'Giao hàng tận nơi', '2025-10-13', N'admin', 1, '2025-10-13'),
(NULL, 5, 2, N'HD005', N'Hoàng Văn E', N'0945678901', N'Cần Thơ', 6000000, 6000000, N'Online', N'', '2025-10-14', N'admin', 1, '2025-10-14'),
-- HÓA ĐƠN HOÀN THÀNH (trang_thai = 4) - DỮ LIỆU CŨ
(1, 1, 2, N'HD006', N'Nguyễn Văn F', N'0901111111', N'Hà Nội', 15000000, 14500000, N'Bán lẻ', N'Khách hàng VIP', '2025-01-15', N'admin', 4, '2025-01-15'),
(2, 2, 2, N'HD007', N'Trần Thị G', N'0912222222', N'Hải Phòng', 12000000, 11800000, N'Bán lẻ', N'', '2025-01-16', N'admin', 4, '2025-01-16'),
(NULL, 3, 3, N'HD008', N'Lê Văn H', N'0923333333', N'Đà Nẵng', 8000000, 8000000, N'Bán lẻ', N'', '2025-01-17', N'admin', 4, '2025-01-17'),
(NULL, 4, 3, N'HD009', N'Phạm Thị I', N'0934444444', N'TP.HCM', 9000000, 9000000, N'Online', N'Giao hàng nhanh', '2025-01-18', N'admin', 4, '2025-01-18'),
(NULL, 5, 2, N'HD010', N'Hoàng Văn J', N'0945555555', N'Cần Thơ', 11000000, 11000000, N'Online', N'', '2025-01-19', N'admin', 4, '2025-01-19');

-- hoa_don_chi_tiet
INSERT INTO hoa_don_chi_tiet(hoa_don_id, san_pham_id, so_luong, don_gia, thanh_tien)
VALUES
-- Chi tiết cho hóa đơn đã thanh toán (trang_thai = 1)
(1, 1, 1, 20000000, 20000000),
(2, 2, 1, 17000000, 17000000),
(3, 3, 1, 5500000, 5500000),
(4, 4, 1, 7000000, 7000000),
(5, 5, 1, 6000000, 6000000),
-- Chi tiết cho hóa đơn hoàn thành (trang_thai = 4)
(6, 1, 1, 15000000, 15000000),
(7, 2, 1, 12000000, 12000000),
(8, 3, 1, 8000000, 8000000),
(9, 4, 1, 9000000, 9000000),
(10, 5, 1, 11000000, 11000000);

-- phuong_thuc_thanh_toan
INSERT INTO phuong_thuc_thanh_toan(ten_phuong_thuc, loai_hinh_thuc)
VALUES
(N'Tiền mặt', N'Trực tiếp'),
(N'Chuyển khoản', N'Ngân hàng'),
(N'Thẻ tín dụng', N'Ngân hàng'),
(N'Ví điện tử MoMo', N'Ví điện tử'),
(N'Ví điện tử ZaloPay', N'Ví điện tử');

-- chi_tiet_thanh_toan
INSERT INTO chi_tiet_thanh_toan(id_don_hang, id_phuong_thuc_thanh_toan, ma_giao_dich, so_tien, ngay_thanh_toan)
VALUES
(1, 1, N'TM001', 19500000, '2025-01-15'),
(2, 2, N'CK001', 16800000, '2025-01-16'),
(3, 1, N'TM002', 5500000, '2025-01-20'),
(4, 4, N'MOMO001', 7000000, '2025-01-22'),
(5, 5, N'ZALO001', 6000000, '2025-01-25'),
-- Thanh toán cho hóa đơn mới
(6, 1, N'TM003', 14500000, '2025-01-15'),
(7, 2, N'CK002', 11800000, '2025-01-16'),
(8, 1, N'TM004', 8000000, '2025-01-17'),
(9, 4, N'MOMO002', 9000000, '2025-01-18'),
(10, 5, N'ZALO002', 11000000, '2025-01-19');

-- khuyen_mai
INSERT INTO khuyen_mai(ma_khuyen_mai, ten_khuyen_mai, mo_ta, muc_do_uu_tien, phan_tram_giam, giam_toi_da, ngay_bat_dau, ngay_ket_thuc, nguoi_tao)
VALUES
(N'KM001', N'Khuyến mãi Tết 2025', N'Giảm giá dịp Tết Nguyên Đán', 1, 15, 2000000, '2025-01-20', '2025-02-10', N'admin'),
(N'KM002', N'Khuyến mãi 30/4', N'Giảm giá dịp lễ 30/4', 2, 10, 1000000, '2025-04-25', '2025-05-05', N'admin'),
(N'KM003', N'Black Friday', N'Giảm giá mạnh cuối năm', 1, 25, 5000000, '2025-11-20', '2025-11-30', N'admin'),
(N'KM004', N'Sinh nhật cửa hàng', N'Ưu đãi sinh nhật 3 năm', 1, 20, 3000000, '2025-07-01', '2025-07-15', N'admin'),
(N'KM005', N'Khai trương chi nhánh mới', N'Giảm giá khai trương', 3, 12, 1500000, '2025-10-01', '2025-10-10', N'admin');

-- chi_tiet_khuyen_mai
INSERT INTO chi_tiet_khuyen_mai(id_ctsp, id_khuyen_mai, ngay_ap_dung, muc_do_uu_tien, phan_tram_giam_gia)
VALUES
(1, 1, '2025-01-20', 1, 15),
(2, 1, '2025-01-20', 1, 15),
(3, 2, '2025-04-25', 2, 10),
(4, 3, '2025-11-20', 1, 25),
(5, 4, '2025-07-01', 1, 20);

-- hinh_anh
INSERT INTO hinh_anh(id_ctsp, url_anh)
VALUES
(1, N'https://example.com/iphone12_black.jpg'),
(2, N'https://example.com/samsung_s21_white.jpg'),
(3, N'https://example.com/redmi_note10_blue.jpg'),
(4, N'https://example.com/vivo_v21_pink.jpg'),
(5, N'https://example.com/oppo_a95_purple.jpg');

-- imei
INSERT INTO imei(id_ctsp, imei)
VALUES
(1, N'356789012345671'),
(1, N'356789012345672'),
(2, N'356789012345681'),
(3, N'356789012345691'),
(4, N'356789012345701');

-- imei_da_ban
INSERT INTO imei_da_ban(id_hoa_don_chi_tiet, imei)
VALUES
(1, N'356789012345671'),
(2, N'356789012345681'),
(3, N'356789012345691'),
(4, N'356789012345701'),
(5, N'356789012345711');

-- user_dia_chi
INSERT INTO user_dia_chi(id_dia_chi, id_user, loai_dia_chi, mac_dinh)
VALUES
(1, 1, N'Nhà riêng', 1),
(2, 2, N'Công ty', 0),
(3, 3, N'Nhà riêng', 1),
(4, 4, N'Nhà riêng', 1),
(5, 5, N'Công ty', 0);

-- khach_hang_giam_gia
INSERT INTO khach_hang_giam_gia(id_khach_hang, id_phieu_giam_gia, nguoi_su_dung, ngay_cap)
VALUES
(1, 1, N'Nguyễn Văn A', '2025-09-15'),
(2, 2, N'Trần Thị B', '2025-09-16'),
(3, 3, N'Lê Văn C', '2025-09-10'),
(4, 4, N'Phạm Thị D', '2025-09-12'),
(5, 5, N'Hoàng Văn E', '2025-09-14');

-- =================== TRIGGERS ===================

-- Trigger tự động cập nhật số lượng trong chi_tiet_san_pham dựa trên IMEI
CREATE TRIGGER trg_UpdateSoLuongFromIMEI
ON imei
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Cập nhật số lượng cho các chi tiết sản phẩm bị ảnh hưởng bởi INSERT hoặc UPDATE
    IF EXISTS (SELECT 1 FROM inserted)
    BEGIN
        UPDATE ctsp
        SET so_luong = (
            SELECT COUNT(*) 
            FROM imei 
            WHERE id_ctsp = ctsp.id 
            AND trang_thai = 1
        )
        FROM chi_tiet_san_pham ctsp
        INNER JOIN inserted i ON ctsp.id = i.id_ctsp;
    END
    
    -- Cập nhật số lượng cho các chi tiết sản phẩm bị ảnh hưởng bởi DELETE
    IF EXISTS (SELECT 1 FROM deleted)
    BEGIN
        UPDATE ctsp
        SET so_luong = (
            SELECT COUNT(*) 
            FROM imei 
            WHERE id_ctsp = ctsp.id 
            AND trang_thai = 1
        )
        FROM chi_tiet_san_pham ctsp
        INNER JOIN deleted d ON ctsp.id = d.id_ctsp;
    END
END;
GO



PRINT N'=== ĐÃ TẠO DATABASE, INSERT DỮ LIỆU MẪU VÀ TẠO TRIGGERS THÀNH CÔNG ===';
PRINT N'';
PRINT N'TRIGGERS ĐÃ TẠO:';
PRINT N'1. trg_UpdateSoLuongFromIMEI - Tự động cập nhật số lượng khi thêm/sửa/xóa IMEI';
PRINT N'2. trg_UpdateSoLuongWhenIMEISold - Cập nhật số lượng khi IMEI được bán';
PRINT N'3. trg_UpdateSoLuongWhenIMEIRestored - Cập nhật số lượng khi khôi phục IMEI';
PRINT N'';
PRINT N'Số lượng trong chi_tiet_san_pham sẽ tự động được tính từ số IMEI có trạng thái = 1';
GO